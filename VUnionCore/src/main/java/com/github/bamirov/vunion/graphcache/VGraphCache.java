package com.github.bamirov.vunion.graphcache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.exceptions.GraphVersionMismatchException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VGraph;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VLinkUpdate;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphElementRecord;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;
import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersion.VGraphVersionBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class VGraphCache<V extends Comparable<V>, I> implements IGraphCache<V, I> {
	@RequiredArgsConstructor
	protected class SharedElementRec {
		int refCount = 0;
		@NonNull VElement<V, I> vertex;
	}
	
	protected String graphName;
	
	protected V subgraphSyncVersion = null;
	protected V destroyRecoverVersion = null;
	protected boolean isDestroyed = false;
	
	protected Map<I, SharedElementRec> elements = new HashMap<>();
	
	protected Map<String, VSubgraphCache<V, I>> subgraphs = new HashMap<>();

	protected Optional<VGraph<V, I>> graphElement = Optional.empty();
	
	protected ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
	
	protected V getGraphMaxVersion() {
		V graphVersion = getGraphUpdateVersion();
		
		for (Entry<String, VSubgraphCache<V, I>> ent : subgraphs.entrySet())
			if (graphVersion == null)
				graphVersion = ent.getValue().getSubgraphVersion();
			else
				graphVersion = (graphVersion.compareTo(ent.getValue().getSubgraphVersion()) >= 0) 
						? graphVersion 
						: ent.getValue().getSubgraphVersion();
		
		return graphVersion;
	}
	
	protected V getGraphUpdateVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphSyncVersion, 
				destroyRecoverVersion,
				graphElement.isPresent() ? graphElement.get() : null
		});
		return Collections.max(list);
	}
	
	@Override
	public VGraphVersion<V> getGraphVersion() {
		updateLock.readLock().lock();
		try {
			V graphVersion = getGraphUpdateVersion();
			
			VGraphVersionBuilder<V> builder = VGraphVersion.<V>builder().
					graphVersion(graphVersion == null ? Optional.empty() : Optional.of(graphVersion));
			for (Entry<String, VSubgraphCache<V, I>> ent : subgraphs.entrySet())
				builder.subgraphVersion(ent.getKey(), ent.getValue().getSubgraphVersion());
			
			return builder.build();
		} finally {
			updateLock.readLock().unlock();
		}
	}

	private void addToBuilder(StringBuilder builder, String str) {
		if (builder.length() != 0)
			builder.append(", ");
		builder.append(str);
	}
	
	protected void sanityCheckGraphDiff(VGraphDiff<V, I> diff) throws GraphVersionMismatchException, GraphMismatchException {
		//1. Graph name must match
		if (!graphName.equals(diff.getGraphName()))
			throw new GraphMismatchException(
					String.format("Graph name doesn't match: diff [%s] cache [%s]", 
							diff.getGraphName(), graphName));
		
		//2. Cache version must equal to Diff version from
		VGraphVersion<V> cacheVersion = getGraphVersion();
		if (!diff.getFrom().equals(cacheVersion))
			throw new GraphVersionMismatchException(cacheVersion, 
				String.format("Cache/diff version mismatch: [diff from: [%s]; cache: [%s]] ", 
						diff.getFrom().toString(), cacheVersion.toString())
			);
		
		//3. If graph is destroyed, no data should be present in the Diff other than Destroyed record
		V maxGraphVersion = getGraphMaxVersion();
		if (diff.getDestroyedRecord().isPresent()) {
			if (diff.getDestroyedRecord().get().isDestroyed()) {
				StringBuilder builder = new StringBuilder();
				
				if (diff.getVertexTypes().isPresent()) addToBuilder(builder, "VertexTypes");
				if (diff.getVertexes().isPresent()) addToBuilder(builder, "Vertexes");
				if (diff.getEdgeTypes().isPresent()) addToBuilder(builder, "EdgeTypes");
				if (diff.getEdges().isPresent()) addToBuilder(builder, "Edges");
				if (diff.getSubgraphs().isPresent()) addToBuilder(builder, "Subgraphs");
				if (diff.getSubgraphSync().isPresent()) addToBuilder(builder, "SubgraphSync");
				if (diff.getGraphElementRecord().isPresent()) addToBuilder(builder, "GraphElementRecord");
				
				if (builder.length() != 0) {
					throw new GraphMismatchException(
						String.format("DestroyedRecord inconsistent isDestroyed set to true, but the following data exists: [%s]",
								builder.toString())
					);
				}
			}
			
			//NB: two [isDestroyed:true] diffs can be received in a row, in case Recovery event in the middle was missed
			//NB: two [isDestroyed:false] diffs can be received in a row, in case Destroyed event in the middle was missed
			
			if ((maxGraphVersion != null) && (diff.getDestroyedRecord().get().getDestroyRecoverVersion().compareTo(maxGraphVersion) <= 0)) {
				throw new GraphMismatchException(
						String.format("DestroyedRecord inconsistent: [DestroyRecoverVersion: [%s] <= cache GraphUpdateVersion: [%s]]",
								diff.getDestroyedRecord().get().getDestroyRecoverVersion().toString(), 
								maxGraphVersion.toString())
					);
			}
		}
		
		//4. Diff: Graph element version should be equal to VGraphElementRecord.getGraphElementUpdateVersion
		//	If cache graph element exists, its version should be < Diff Graph element version
		//	TODO: Diff Graph element version should be > cache Graph version
		if (diff.getGraphElementRecord().isPresent()) {
			VGraphElementRecord<V, I> ger = diff.getGraphElementRecord().get();
			
			if (ger.getGraphElement().isPresent()) {
				if (!ger.getGraphElement().get().getVersion().equals(ger.getGraphElementUpdateVersion())) {
					throw new GraphMismatchException(
						String.format("GraphElementRecord inconsistent: [GraphElementUpdateVersion: [%s] != GraphElement.Version: [%s]]",
								ger.getGraphElementUpdateVersion().toString(), 
								ger.getGraphElement().get().getVersion().toString())
					);
				}
			}
			
			if (graphElement.isPresent()) {
				if (ger.getGraphElementUpdateVersion().compareTo(graphElement.get().getVersion()) <= 0) {
					throw new GraphMismatchException(
						String.format("GraphElementRecord inconsistent: [GraphElementUpdateVersion: [%s] <= cache GraphElement.Version: [%s]]",
								ger.getGraphElementUpdateVersion().toString(), 
								graphElement.get().getVersion().toString())
					);
				}
			}

			if ((maxGraphVersion != null) && (graphElement.get().getVersion().compareTo(maxGraphVersion) <= 0)) {
				//
			}
		}

		Map<String, VSubgraphDiff<V, I>> newNames = new HashMap<>();
		if (diff.getSubgraphs().isPresent())
			for (VSubgraphDiff<V, I> sg : diff.getSubgraphs().get())
				newNames.put(sg.getName(), sg);
		
		//5. Diff SubgraphSyncRecord version should be > Cache subgraphSyncVersion
		//	All subgraph names should exist either in Cache or in Diff
		//	TODO: All subgraphs that are removed from cache should not exist in diff
		if (diff.getSubgraphSync().isPresent()) {
			VSubgraphSyncRecord<V> subgraphSyncRecord = diff.getSubgraphSync().get();
			
			if ((subgraphSyncVersion != null) && (subgraphSyncRecord.getSubgraphSyncVersion().compareTo(subgraphSyncVersion) <= 0)) {
				throw new GraphMismatchException(
					String.format("VSubgraphSyncRecord inconsistent: [GraphElementUpdateVersion: [%s] <= cache SubgraphSyncVersion: [%s]]",
							subgraphSyncRecord.getSubgraphSyncVersion().toString(), 
							subgraphSyncVersion)
				);
			}
			
			List<String> names = subgraphSyncRecord.getSubgraphNames();
			if (!names.isEmpty()) {
				for (String name : names) {
					if ((!subgraphs.containsKey(name)) && (!newNames.containsKey(name)))
						throw new GraphMismatchException(
								String.format("VSubgraphSyncRecord inconsistent: [Subgraph name doesn't exist in cache or diff : [%s]]", name)
						);
				}
			}
		}
		
		Map<I, SharedElementRec> newElements = new HashMap<>();
		if (diff.getVertexTypes().isPresent())
			for (Entry<I, VVertexType<V, I>> ent : diff.getVertexTypes().get().entrySet())
				newElements.put(ent.getKey(), new SharedElementRec(ent.getValue()));
		if (diff.getVertexes().isPresent())
			for (Entry<I, VVertex<V, I>> ent : diff.getVertexes().get().entrySet())
				newElements.put(ent.getKey(), new SharedElementRec(ent.getValue()));
		if (diff.getEdgeTypes().isPresent())
			for (Entry<I, VEdgeType<V, I>> ent : diff.getEdgeTypes().get().entrySet())
				newElements.put(ent.getKey(), new SharedElementRec(ent.getValue()));
		if (diff.getEdges().isPresent())
			for (Entry<I, VEdge<V, I>> ent : diff.getEdges().get().entrySet())
				newElements.put(ent.getKey(), new SharedElementRec(ent.getValue()));
		
		//6. Subgraph links check
		if (diff.getSubgraphs().isPresent()) {
			List<VSubgraphDiff<V, I>> subgraphList = diff.getSubgraphs().get();
			
			for (VSubgraphDiff<V, I> subgraph : subgraphList) {
				VSubgraphCache<V, I> subgraphCache = subgraphs.get(subgraph.getName());
				
				//6.1 Diff Subgraph version To should be > corresponding Cache Subgraph version 
				if (subgraphCache != null) {
					if (subgraph.getSubgraphVersionTo().compareTo(subgraphCache.getSubgraphVersion()) <= 0) {
						throw new GraphMismatchException(
							String.format("Subgraph updates inconsistent: [Subgraph [%s]: SubgraphVersion: [%s] <= cache SubgraphVersion: [%s]]",
									subgraph.getName(),
									subgraph.getSubgraphVersionTo().toString(), 
									subgraphCache.getSubgraphVersion().toString())
						);
					}
				}
				
				//6.2 Diff: Subgraph element version should be equal to VSubgraphElementRecord.getSubgraphElementRecord
				//	If Cache Subgraph element exists, its version should be < Diff Subgraph element version
				//	TODO: Diff Subgraph element version should be > cache Subgraph version
				if (subgraph.getSubgraphElementRecord().isPresent()) {
					VSubgraphElementRecord<V, I> subgraphElementRecord = subgraph.getSubgraphElementRecord().get();
					
					if (subgraphElementRecord.getSubgraphElement().isPresent()) {
						if (!subgraphElementRecord.getSubgraphElement().get().getVersion().equals(subgraphElementRecord.getSubgraphElementUpdateVersion())) {
							throw new GraphMismatchException(
								String.format("SubgraphElementRecord inconsistent: [Subgraph [%s]: SubgraphElementUpdateVersion: [%s] != SubgraphElement.Version: [%s]]",
										subgraphElementRecord.getSubgraphElementUpdateVersion().toString(), 
										subgraphElementRecord.getSubgraphElement().get().getVersion().toString())
							);
						}
					}
					
					if (subgraphCache != null) {
						if (subgraphCache.getSubgraphElement().isPresent()) {
							if (subgraphElementRecord.getSubgraphElementUpdateVersion().compareTo(subgraphCache.getSubgraphElement().get().getVersion()) <= 0) {
								throw new GraphMismatchException(
									String.format("SubgraphElementRecord inconsistent: [Subgraph [%s]: SubgraphElementUpdateVersion: [%s] <= cache SubgraphElement.Version: [%s]]",
											subgraph.getName(),
											subgraphElementRecord.getSubgraphElementUpdateVersion().toString(), 
											subgraphCache.getSubgraphElement().get().getVersion().toString())
								);
							}
						}
						
						if (subgraphElementRecord.getSubgraphElementUpdateVersion().compareTo(subgraphCache.getSubgraphVersion()) <= 0) {
							//
						}
					}
				}
				
				//6.3 Various links/shared elements integrity checks
				//TODO: tombstoned links checks
				if (subgraph.getLinkUpdates().isPresent()) {
					List<VLinkDiff<V, I>> linkUpdates = subgraph.getLinkUpdates().get();
					
					for (VLinkDiff<V, I> linkUpdate : linkUpdates) {
						if (linkUpdate.getLinkedElementUpdate().isPresent()) {
							if (!linkUpdate.getLinkUpdate().isPresent()) {
								//TODO: No link info - link should exist in cache, element should exist in cache, element should exist in diff
							} else {
								//TODO: Link info + elementInfo - element should exist in diff
							}
							
							//TODO: Vertex addition checks
							//TODO: Edge addition checks
						} else {
							if (linkUpdate.getLinkUpdate().isPresent()) {
								//TODO: No element info - link should exist in cache
							} else {
								//TODO: No link info, no element info - error
							}
						}
						
						//TODO: Check elements versions to be > subgraph cache versions
						//TODO: Mark diff elements as referenced
					}
				}
				
				//6.4 Deleted links checks
				if (subgraph.getElementSync().isPresent()) {
					VElementSyncRecord<V, I> elementSync = subgraph.getElementSync();
				}
			}
			
			//TODO: Check that all diff elements were referenced at least once
		}
	}
	
	@Override
	public void applyGraphDiff(VGraphDiff<V, I> diff) throws GraphMismatchException {
		//TODO: if Recovery, remove all data (except for delete info)
		//sanityCheckGraphDiff(diff);
		//TODO: if Destroyed, remove all data (except for delete info)
		
		// TODO Auto-generated method stub
	}
	
	@Override
	public VGraphDiff<V, I> getGraphDiff(VGraphVersion<V> from) {
		// TODO Auto-generated method stub

		//For test purposes, sanityCheckGraphDiff(...) before returning
		
		return null;
	}
}
