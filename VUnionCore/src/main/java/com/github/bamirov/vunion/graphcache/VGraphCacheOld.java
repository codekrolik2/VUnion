package com.github.bamirov.vunion.graphcache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.exceptions.GraphVersionMismatchException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VGraphElement;
import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersion.VGraphVersionBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class VGraphCacheOld {} /*<V extends Comparable<V>, I> implements IGraphCache<V, I> {
	@RequiredArgsConstructor
	protected class VertexRec {
		int refCount = 0;
		@NonNull VVertex<V, I> vertex;
	}
	
	@RequiredArgsConstructor
	protected class EdgeRec {
		int refCount = 0;
		@NonNull VEdge<V, I> edge;
	}

	@RequiredArgsConstructor
	protected class VertexTypeRec {
		int refCount = 0;
		@NonNull VVertexType<V, I> vertexType;
	}
	
	@RequiredArgsConstructor
	protected class EdgeTypeRec {
		int refCount = 0;
		@NonNull VEdgeType<V, I> edgeType;
	}
	
	protected String graphName;
	
	protected V subgraphSyncVersion = null;
	protected V destroyRecoverVersion = null;
	protected boolean isDestroyed = false;
	
	//TODO: consider this 
	protected Map<I, VertexRec> vertexes = new HashMap<>();
	protected Map<I, EdgeRec> edges = new HashMap<>();

	protected Map<I, VertexTypeRec> vertexTypes = new HashMap<>();
	protected Map<I, EdgeTypeRec> edgeTypes = new HashMap<>();
	//TODO: vs this
	//protected Map<I, ElementRec> elements = new HashMap<>();
	
	protected Map<String, VSubgraphCache<V, I>> subgraphs = new HashMap<>();

	protected Optional<VGraph<V, I>> graphElement = Optional.empty();
	
	protected ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();

	//-----------------------------------------
	
	public V getGraphVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphSyncVersion, 
				destroyRecoverVersion,
				graphElement.isPresent() ? graphElement.get() : null
		});
		return Collections.max(list);
	}
	
	//-----------------------------------------
	
	@Override
	public VGraphVersion<V> getVersion() {
		updateLock.readLock().lock();
		try {
			V graphVersion = getGraphVersion();
			
			VGraphVersionBuilder<V> builder = VGraphVersion.<V>builder().graphVersion(graphVersion);
			for (Entry<String, VSubgraphCache<V, I>> ent : subgraphs.entrySet()) {
				builder.subgraphVersion(ent.getKey(), ent.getValue().getSubgraphVersion());
			}
			
			return builder.build();
		} finally {
			updateLock.readLock().unlock();
		}
	}
	
	//TODO: first check the contents of the diff for consistency, then update
	
	@Override
	public void applyDiff(VGraphDiff<V, I> diff) throws GraphVersionMismatchException, GraphMismatchException {
		updateLock.writeLock().lock();
		try {
			VGraphVersion<V> cacheVersion = getVersion();
			if (!diff.getFrom().equals(cacheVersion))
				throw new GraphVersionMismatchException(cacheVersion, 
					String.format("Cache/diff version mismatch: [diff from V: [%s]; cache V: [%s]] ", 
							diff.getFrom().toString(), cacheVersion.toString())
				);
			
			if (!diff.getGraphName().equals(graphName))
				throw new GraphMismatchException(
					String.format("Graph name mismatch: diff [%s]; cache [%s]", diff.getGraphName(), graphName)
				);
			
			//TODO: (sanity) check that all diff.getVertexes() and diff.getEdges() are connected to link updates
			//No orphan Vertexes or Edges should be present in diffs.
			
			if (diff.getGraphElement().isPresent()) {
				if ((!graphElement.isPresent()) || 
					(diff.getGraphElement().get().getVersion().compareTo(graphElement.get().getVersion()) > 0)) {
					graphElement = diff.getGraphElement();
				} else
					throw new GraphMismatchException(
						String.format("Diff contains graphElement with old version: diff [%s]; cache [%s]",
							diff.getGraphElement().get().getVersion().toString(), graphElement.get().getVersion().toString())
					);
			}
			
			for (VSubgraphDiff<V, I> subgraph : diff.getSubgraphs())
				applySubgraphDiff(subgraph, diff.getVertexes(), diff.getEdges());
			
			if (diff.getSubgraphSync().isPresent()) {
				if ((subgraphSyncVersion == null) ||
					(diff.getSubgraphSync().get().getSubgraphSyncVersion().compareTo(subgraphSyncVersion) > 0)
					) {
					//TODO: sync subgraphs and elements
				} else
					throw new GraphMismatchException(
						String.format("Diff contains graphElement with old version: diff [%s]; cache [%s]",
							diff.getGraphElement().get().getVersion().toString(), graphElement.get().getVersion().toString())
					);
			}
		} finally {
			updateLock.writeLock().unlock();
		}
	}
	
	protected void applySubgraphDiff(VSubgraphDiff<V, I> subgraphDiff, Map<I, VVertex<V, I>> diffVertexes,
			Map<I, VEdge<V, I>> diffEdges) {
		VSubgraphCache<V, I> subgraphCache = subgraphs.get(subgraphDiff.getName());
		V subgraphCacheV = null;
		if (subgraphCache == null) {
			subgraphCache = new VSubgraphCache<V, I>();
			subgraphs.put(subgraphDiff.getName(), subgraphCache);
		} else {
			subgraphCacheV = subgraphCache.getSubgraphVersion();
			if (subgraphCache.getSubgraphVersion().compareTo(subgraphDiff.getSubgraphVersionTo()) >= 0) {
				throw new GraphMismatchException(
					String.format("Subgraph version mismatch: name [%s] cache V [%s] > diff V To [%s]",
						subgraphDiff.getName(), subgraphCache.getSubgraphVersion(), subgraphDiff.getSubgraphVersionTo()
					)
				);
			}
		}
		
		if (subgraphDiff.getSubgraphElement().isPresent()) {
			//Test SubgraphElement to have version larger than cache's SubgraphElement Version
			if (
				(subgraphCache.getSubgraphElement().isPresent())
				&&
				(subgraphDiff.getSubgraphElement().get().getVersion().compareTo(
				subgraphCache.getSubgraphElement().get().getVersion()) <= 0)
			)  
				throw new GraphMismatchException(
					String.format("Subgraph Diff contains subgraphElement with old element version: "
							+ "name: [%s] diff element V [%s]; cache element V [%s]",
						subgraphDiff.getName(),
						subgraphDiff.getSubgraphElement().get().getVersion().toString(), 
						subgraphCache.getSubgraphElement().get().getVersion().toString())
				);
			
			//Test SubgraphElement to have version larger than cacheVersion
			if (
				(subgraphCacheV != null)
				&&
				(subgraphDiff.getSubgraphElement().get().getVersion().compareTo(subgraphCacheV) <= 0)
			)
				throw new GraphMismatchException(
					String.format("Subgraph Diff contains subgraphElement with old element version: "
							+ "name: [%s] diff element V [%s]; cache V [%s]",
						subgraphDiff.getName(),
						subgraphDiff.getSubgraphElement().get().getVersion().toString(), 
						subgraphCacheV.toString())
				);
				
			subgraphCache.setSubgraphElement(subgraphDiff.getSubgraphElement());
		}
		
		for (VLinkDiff<V, I> linkUpdate : subgraphDiff.getUpdates()) {
			//Link Update can come in 3 forms:
			//1) Update of Link - in this case update.getLinkUpdate() will be present
			//2) Update of Linked element - in this case update.getLinkElementVersion() will be present
			//3) Update of both
			
			I id = linkUpdate.getId();
			I linkElementId = linkUpdate.getLinkElementId();
			
			TreeMap<V, VLink<V, I>> subgraphTimeline = subgraphCache.getSubgraphTimeline();
			Map<I, VLink<V, I>> linksByElementId = subgraphCache.getSubgraphLinksByElementId();
			
			VLink<V, I> link = linksByElementId.remove(linkElementId);
			if (link != null) {
				if (!id.equals(link.getElementId()))
					throw new GraphMismatchException(
							String.format("Different Id for link in the map and in the diff. "
								+ "id [%s]; linkElementId [%s]; link [%s]", id, linkElementId, link.toString()
							)
						);
				
				if (!linkElementId.equals(link.getLinkElementId()))
					throw new RuntimeException(
							String.format("FATAL: SubgraphCache internal structures desynchronization! "
								+ "Different LinkElementId for link in the map. "
								+ "id [%s]; linkElementId [%s]; link [%s]", id, linkElementId, link.toString()
							)
						);

				VLink<V, I> timelineLink = subgraphTimeline.remove(link.getTimelineVersion());
				
				if (timelineLink != link)
					throw new RuntimeException(
						String.format("FATAL: SubgraphCache internal structures desynchronization! "
							+ "Different VLink in tree and map. "
							+ "Tree [%s]; Map [%s]", timelineLink.toString(), link.toString()
						)
					);
			}
			
			boolean newLink = link == null;
			if (newLink) {
				if ((!linkUpdate.getLinkUpdate().isPresent()) || 
					(!linkUpdate.getLinkElementVersion().isPresent())) {
					throw new GraphMismatchException(
						String.format("Received partial link update for a nonexistent link: LinkUpdate [%s]", 
							linkUpdate.toString())
					);
				}

				V linkVersion = linkUpdate.getLinkUpdate().get().getVersion();
				Optional<String> key = linkUpdate.getLinkUpdate().get().getKey();
				String content = linkUpdate.getLinkUpdate().get().getContent();
				boolean isTombstone = linkUpdate.getLinkUpdate().get().isTombstone();
				
				V linkElementVersion = linkUpdate.getLinkElementVersion().get();
				
				//Check: Link should have version larger than subgraphVersion
				if (
						(subgraphCacheV != null)
						&&
						(linkVersion.compareTo(subgraphCacheV) <= 0)
					)
						throw new GraphMismatchException(
							String.format("Link has version smaller than subgraphVersion: "
									+ "name: [%s] linkId [%s] link V [%s]; cache V [%s]",
								subgraphDiff.getName(),
								id.toString(), 
								linkVersion.toString(), 
								subgraphCacheV.toString())
						);
				
				//Newly Linked Element's version doesn't have to be larger than subgraphVersion, 
				//because it could be an old element with old version just linked to the subgraph
				
				link = new VLink<V, I>(id, linkVersion, key, content, linkElementId, linkElementVersion, isTombstone);
			} else {
				if (linkUpdate.getLinkUpdate().isPresent()) {
					V linkVersion = linkUpdate.getLinkUpdate().get().getVersion();
					
					//Check: Link should have version larger than previous link version
					if (
							(subgraphCacheV != null)
							&&
							(linkVersion.compareTo(link.getVersion()) <= 0)
						)
							throw new GraphMismatchException(
								String.format("Link has version smaller than previous link version: "
										+ "name: [%s] linkId [%s] link V [%s]; previous link V [%s]",
									subgraphDiff.getName(),
									id.toString(), 
									linkVersion.toString(), 
									link.getVersion().toString())
							);
					
					//Check: Link should have version larger than subgraphVersion
					if (
							(subgraphCacheV != null)
							&&
							(linkVersion.compareTo(subgraphCacheV) <= 0)
						)
							throw new GraphMismatchException(
								String.format("Link has version smaller than subgraphVersion: "
										+ "name: [%s] linkId [%s] link V [%s]; cache V [%s]",
									subgraphDiff.getName(),
									id.toString(), 
									linkVersion.toString(), 
									subgraphCacheV.toString())
							);
					
					Optional<String> key = linkUpdate.getLinkUpdate().get().getKey();
					String content = linkUpdate.getLinkUpdate().get().getContent();
					boolean isTombstone = linkUpdate.getLinkUpdate().get().isTombstone();
					
					link.setVersion(linkVersion);
					link.setKey(key);
					link.setContent(content);
					link.setTombstone(isTombstone);
				}
				
				if (linkUpdate.getLinkElementVersion().isPresent()) {
					V linkElementVersion = linkUpdate.getLinkElementVersion().get();
					
					//Check: Linked element should have version larger than its previous version
					if (
							(subgraphCacheV != null)
							&&
							(linkElementVersion.compareTo(link.getLinkElementVersion()) <= 0)
						)
						throw new GraphMismatchException(
								String.format("Linked Element has version smaller than previous Linked Element version: "
										+ "name: [%s] linkId [%s] linked element V [%s]; cache V [%s]",
									subgraphDiff.getName(),
									id.toString(), 
									linkElementVersion.toString(), 
									link.getLinkElementVersion().toString())
							);
					
					//In this case linked element must have its version larger than subgraphVersion,
					//because its version update means that the element was updated since last diff retrieval
					//and must have a version larger than anything from that last retrieval.
					
					//Check: Linked element should have version larger than subgraphVersion
					if (
							(subgraphCacheV != null)
							&&
							(linkElementVersion.compareTo(subgraphCacheV) <= 0)
						)
						throw new GraphMismatchException(
								String.format("Linked Element has version smaller than subgraphVersion: "
										+ "name: [%s] linkId [%s] linked element V [%s]; cache V [%s]",
									subgraphDiff.getName(),
									id.toString(), 
									linkElementVersion.toString(), 
									subgraphCacheV.toString())
							);
					
					link.setLinkElementVersion(linkElementVersion);
				}
			}
			
			linksByElementId.put(linkElementId, link);
			subgraphTimeline.put(link.getTimelineVersion(), link);
			
			if (linkUpdate.getLinkElementVersion().isPresent()) {
				//try to update diffVertexes / diffEdges, get an element from the diff element Map
				VVertex<V, I> vertex = diffVertexes.get(link.getElementId());
				VEdge<V, I> edge = diffEdges.get(link.getElementId());
				
				//In this case we always want to have a linked element in either diffVertexes or diffEdges.
				//Even though such element (if newly added) might have a version behind our graph version,
				//and very well might be already present in the VGraphCache's vertexes or edges, it's hard 
				//to reason about whether it's there or not in presence of subgraph filters.
				//So it seems to me ATM more viable to just include it every time it's linked to a subgraph
				//than reasoning about whether other subgraphs of a particular client with particular filters
				//may or may not have previously retrieved the current version of said element.
				//Therefore, this check:
				if (vertex == null && edge == null) {
					throw new GraphMismatchException(
						String.format(
							"Updated Linked Element doesn't have an element itself in the diff: "
							+ "name [%s] linkId [%s] linked element Id [%s] linked element V [%s] ",
							subgraphDiff.getName(),
							id.toString(),
							link.getElementId().toString(),
							linkUpdate.getLinkElementVersion().get().toString()
						)
					);
				}
				
				//XXX: Below code duplication, IK
				if (vertex != null) {
					VertexRec vertexRec = vertexes.get(vertex.getElementId());
					
					if (vertexRec == null) {
						vertexRec = new VertexRec(vertex);
						vertexes.put(vertex.getElementId(), vertexRec);
					} else {
						if (vertex.getVersion().compareTo(vertexRec.vertex.getVersion()) == 0) {
							//Newly linked vertex might be already in the cache, if it was linked to another subgraph previously.
							//Updated vertex might be connected to multiple subgraphs at the same time, and previously updated in the
							//vertexes collection while processing the previous subgraph.
							//In both cases we need to check that there is no data deviation and vertexes with the same version
							//are the same.
							//Since for multiple subgraphs in the same diff the vertex would be the same exact object from diffVertexes map,
							//we use more strict reference equality. For newly linked vertexes it's not necessarily the case, since the existing  
							//vertex could've been created as a part of previous diff, so we use standard equals() method.
							if ((newLink && !vertex.equals(vertexRec.vertex)) || (!newLink && vertex != vertexRec.vertex)) 
								throw new GraphMismatchException(
									String.format("Linked vertex with the same version doesn't match local copy: newLink [%s] diff [%s] cache [%s]",
										Boolean.toString(newLink), vertex.toString(), vertexRec.vertex.toString()
									)
								);
						} else if (vertex.getVersion().compareTo(vertexRec.vertex.getVersion()) < 0) {
							throw new GraphMismatchException(
								String.format("Linked vertex has smaller version than local copy: id [%s] diffV [%s] cacheV [%s]",
									vertex.getElementId(),
									vertex.getVersion().toString(), 
									vertexRec.vertex.getVersion().toString()
								)
							);
						} else {//if (vertex.getVersion().compareTo(vertexRec.vertex.getVersion()) > 0)
							//If we have a newer version in the diff, just update the vertex
							vertexRec.vertex = vertex;
						}
					}
					
					if (newLink)
						vertexRec.refCount++;
				} else {//if (edge != null)
					EdgeRec edgeRec = edges.get(edge.getElementId());
					
					if (edgeRec == null) {
						edgeRec = new EdgeRec(edge);
						edges.put(edge.getElementId(), edgeRec);
					} else {
						if (edge.getVersion().compareTo(edgeRec.edge.getVersion()) == 0) {
							if ((newLink && !edge.equals(edgeRec.edge)) || (!newLink && edge != edgeRec.edge)) 
								throw new GraphMismatchException(
									String.format("Linked edge with the same version doesn't match local copy: newLink [%s] diff [%s] cache [%s]",
										Boolean.toString(newLink), edge.toString(), edgeRec.edge.toString()
									)
								);
						} else if (edge.getVersion().compareTo(edgeRec.edge.getVersion()) < 0) {
							throw new GraphMismatchException(
								String.format("Linked edge has smaller version than local copy: id [%s] diffV [%s] cacheV [%s]",
									edge.getElementId(),
									edge.getVersion().toString(), 
									edgeRec.edge.getVersion().toString()
								)
							);
						} else {//if (edge.getVersion().compareTo(edgeRec.edge.getVersion()) > 0)
							edgeRec.edge = edge;
						}
					}
					
					if (newLink)
						edgeRec.refCount++;
				}
				//XXX: Code duplication end
			}
		}
		
		protected Optional<VElementSyncRecord<V, I>> elementSync;
		//TODO: sync records
		//TODO: reduce ref counts and remove elements from VGraphCache's vertexes and edges if required
	}
	
	@Override
	public VGraphDiff<V, I> getDiff(VGraphVersion<V> from) {
		updateLock.readLock().lock();
		try {
			// TODO Auto-generated method stub
			return null;
		} finally {
			updateLock.readLock().unlock();
		}
	}
}
*/