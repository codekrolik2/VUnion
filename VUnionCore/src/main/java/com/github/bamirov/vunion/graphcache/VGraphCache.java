package com.github.bamirov.vunion.graphcache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graph.filter.IGraphDiffFilter;
import com.github.bamirov.vunion.graphstream.VGraphDestroyedRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;
import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersion.VGraphVersionBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VGraphCache<V extends Comparable<V>, I> implements IGraphCache<V, I> {
	protected GraphDiffChecker<V, I> graphDiffChecker = new GraphDiffChecker<V, I>();

	//-----------------------------
	
	protected final String graphName;

	//I.e. VertexFrom and VertexTo of an Edge must exist 
	protected final boolean areEdgesConsistent;
	
	protected Optional<V> subgraphSyncVersion = Optional.empty();
	
	protected Optional<V> destroyRecoverVersion = Optional.empty();
	protected boolean isDestroyed = false;
	
	protected Optional<VGraphElementRecord<V, I>> graphElementRecord = Optional.empty();

	protected Map<I, SharedElementRec<V, I>> sharedElements = new HashMap<>();
	
	protected Map<String, VSubgraphCache<V, I>> subgraphs = new HashMap<>();

	protected ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
	
	protected VComparator<V> vComparator = new VComparator<>();
	
	protected V getGraphUpdateVersion() {
		V max = null;
		
		if (subgraphSyncVersion.isPresent())
			if (vComparator.compare(subgraphSyncVersion.get(), max) > 0)
				max = subgraphSyncVersion.get();
		if (destroyRecoverVersion.isPresent())
			if (vComparator.compare(destroyRecoverVersion.get(), max) > 0)
				max = destroyRecoverVersion.get();
		if (graphElementRecord.isPresent())
			if (vComparator.compare(graphElementRecord.get().getGraphElementUpdateVersion(), max) > 0)
				max = graphElementRecord.get().getGraphElementUpdateVersion();
		
		return max;
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
	
	protected boolean subgraphDiffElementExists(I elementId, VSubgraphDiff<V, I> sgDiff) {
		if (sgDiff.getLinkUpdatesByElementId().isPresent())
			return sgDiff.getLinkUpdatesByElementId().get().containsKey(elementId);
		return false;
	}
	
	protected void removeSubgraphCache(VSubgraphCache<V, I> subgraphCache) {
		for (VLink<V, I> link : subgraphCache.getSubgraphLinksByElementId().values())
			decrementReferenceCount(link.getLinkedElementId());
		
		subgraphs.remove(subgraphCache.name);
	}
	
	@Override
	public void applyGraphDiff(VGraphDiff<V, I> diff) throws GraphMismatchException {
		updateLock.writeLock().lock();
		try {
			//1. Subgraph name must match
			if (!diff.getGraphName().equals(graphName))
				throw new GraphMismatchException(
						String.format("Graph name doesn't match: diff [%s] cache [%s]", 
								diff.getGraphName(), graphName));
			
			//2. Check graph diff
			//TODO: Check graph diff - after cache is updated, fix
			graphDiffChecker.sanityCheckGraphDiff(this, diff, areEdgesConsistent);

			//3. Update destroyed status
			if (diff.getDestroyedRecord().isPresent()) {
				destroyRecoverVersion = Optional.of(diff.getDestroyedRecord().get().getDestroyRecoverVersion());
				isDestroyed = diff.getDestroyedRecord().get().isDestroyed();
				
				//Remove all data (except for delete info)
				subgraphSyncVersion = Optional.empty();
				graphElementRecord = Optional.empty();
				sharedElements.clear();
				subgraphs.clear();
			}
			
			//4. Update graph element
			if (diff.getGraphElementRecord().isPresent()) {
				VGraphElementRecord<V, I> diffGERecord = diff.getGraphElementRecord().get();
				
				if (graphElementRecord.isPresent()) {
					VGraphElementRecord<V, I> graphElementRec = graphElementRecord.get();
					
					graphElementRec.setGraphElementUpdateVersion(diffGERecord.getGraphElementUpdateVersion());
					graphElementRec.setGraphElement(diffGERecord.getGraphElement());
				} else {
					VGraphElementRecord<V, I> geRecord = new VGraphElementRecord<>(diffGERecord.getGraphElementUpdateVersion(), 
							diffGERecord.getGraphElement());
					graphElementRecord = Optional.of(geRecord);
				}
			}
			
			//5. Subgraphs sync
			if (diff.getSubgraphSync().isPresent()) {
				VSubgraphSyncRecord<V> ss = diff.getSubgraphSync().get();
				Set<String> activeSubgraphs = ss.getSubgraphNames();
				V subgraphSyncV = ss.getSubgraphSyncVersion();
				
				subgraphSyncVersion = Optional.of(subgraphSyncV);
				
				List<VSubgraphCache<V, I>> removedSubgraphCaches = new ArrayList<VSubgraphCache<V, I>>();
				for (VSubgraphCache<V, I> subgraphCache : subgraphs.values())
					if (!activeSubgraphs.contains(subgraphCache.getName()))
						removedSubgraphCaches.add(subgraphCache);
				
				//remove graphs, decrement reference count on shared elements
				for (VSubgraphCache<V, I> subgraphCache : removedSubgraphCaches)
					removeSubgraphCache(subgraphCache);
			}
			
			//6. Apply subgraphs diffs
			if (diff.getSubgraphs().isPresent()) {
				Map<String, VSubgraphDiff<V, I>> subgraphMap = diff.getSubgraphs().get();
				
				for (VSubgraphDiff<V, I> subgraphDiff : subgraphMap.values()) {
					VSubgraphCache<V, I> subgraphCache = subgraphs.get(subgraphDiff.getName());
					
					if (subgraphCache == null) {
						subgraphCache = new VSubgraphCache<V, I>(subgraphDiff.getName());
						subgraphs.put(subgraphDiff.getName(), subgraphCache);
					}
					
					//add reference count on shared elements
					subgraphCache.applySubgraphDiff(subgraphDiff, diff, this);
				}
			}
		} finally {
			updateLock.writeLock().unlock();
		}
	}
	
	@Override
	public VGraphDiff<V, I> getGraphDiff(VGraphVersion<V> originalFrom) throws GraphMismatchException {
		return getGraphDiff(originalFrom, Optional.empty());
	}
	
	@Override
	public VGraphDiff<V, I> getGraphDiff(VGraphVersion<V> originalFrom, Optional<IGraphDiffFilter<V, I>> diffFilter) throws GraphMismatchException {
		updateLock.readLock().lock();
		try {
			VGraphVersion<V> from = originalFrom;
			
			Optional<VGraphDestroyedRecord<V>> destroyedRecord = Optional.empty();
			Optional<VGraphElementRecord<V, I>> graphElementRecordOpt = Optional.empty();
			Optional<VSubgraphSyncRecord<V>> subgraphSync = Optional.empty();
			Optional<Map<String, VSubgraphDiff<V, I>>> subgraphsOpt = Optional.empty();
			
			Optional<Map<I, VVertexType<V, I>>> vertexTypes = Optional.empty();
			Optional<Map<I, VVertex<V, I>>> vertexes = Optional.empty();
			Optional<Map<I, VEdgeType<V, I>>> edgeTypes = Optional.empty();
			Optional<Map<I, VEdge<V, I>>> edges = Optional.empty();
			
			if (destroyRecoverVersion.isPresent() && (vComparator.compare(destroyRecoverVersion, from.getGraphVersion()) > 0)) {
				destroyedRecord = Optional.of(
					new VGraphDestroyedRecord<V>(destroyRecoverVersion.get(), 
							isDestroyed())
				);
				
				from = VGraphVersion.getEmptyGraphVersion();
			}
			
			if (!isDestroyed()) {
				if (graphElementRecord.isPresent()) {
					if (vComparator.compare(graphElementRecord.get().getGraphElementUpdateVersion(), from.getGraphVersion()) > 0) {
						graphElementRecordOpt = Optional.of(new VGraphElementRecord<V, I>(
								graphElementRecord.get().getGraphElementUpdateVersion(),
								graphElementRecord.get().getGraphElement()
							));
					}
				}
				
				if (subgraphSyncVersion.isPresent()) {
					if (vComparator.compare(subgraphSyncVersion.get(), from.getGraphVersion()) > 0) {
						Set<String> subgraphNames = new HashSet<>();
						for(String key : subgraphs.keySet())
							subgraphNames.add(key);
						
						subgraphSync = Optional.of(new VSubgraphSyncRecord<V>(subgraphSyncVersion.get(), subgraphNames));
					}
				}
				
				Set<I> includedElements = new HashSet<>();
				if (!subgraphs.isEmpty()) {
					Map<String, VSubgraphDiff<V, I>> subgraphsMap = new HashMap<>();
					ElementTypeFilterInfo<I> typeFilterInfo = new ElementTypeFilterInfo<>(); 
					
					for (VSubgraphCache<V, I> subgraphCache : subgraphs.values()) {
						//1) Filter Subgraphs by name
						if (!diffFilter.isPresent() || diffFilter.get().isSubgraphAllowed(subgraphCache.getName())) {
							V subgraphVersion = from.getSubgraphVersions().get(subgraphCache.getName());
							
							Optional<VSubgraphDiff<V, I>> subgraphDiff = subgraphCache.getDiff(this, subgraphVersion, diffFilter, typeFilterInfo);
							if (subgraphDiff.isPresent()) {
								subgraphsMap.put(subgraphCache.getName(), subgraphDiff.get());
								
								if (subgraphDiff.get().getLinkUpdatesByElementId().isPresent())
								for (Entry<I, VLinkDiff<V, I>> ent : subgraphDiff.get().getLinkUpdatesByElementId().get().entrySet()) {
									I elementId = ent.getKey();
									VLinkDiff<V, I> val = ent.getValue();
									
									if (val.getLinkedElementVersionUpdate().isPresent())
										includedElements.add(elementId);
								}
							}
						}
					}
					
					if (!subgraphsMap.isEmpty())
						subgraphsOpt = Optional.of(subgraphsMap);
				}
				
				Map<I, VVertexType<V, I>> vertexTypesMap = new HashMap<>();
				Map<I, VVertex<V, I>> vertexesMap = new HashMap<>();
				Map<I, VEdgeType<V, I>> edgeTypesMap = new HashMap<>();
				Map<I, VEdge<V, I>> edgesMap = new HashMap<>();
				
				for (I elementId : includedElements) {
					SharedElementRec<V, I> seRec = sharedElements.get(elementId);
					if (seRec.element instanceof VVertexType)
						vertexTypesMap.put(elementId, (VVertexType<V, I>)seRec.element);
					else if (seRec.element instanceof VVertex)
						vertexesMap.put(elementId, (VVertex<V, I>)seRec.element);
					else if (seRec.element instanceof VEdgeType)
						edgeTypesMap.put(elementId, (VEdgeType<V, I>)seRec.element);
					else if (seRec.element instanceof VEdge)
						edgesMap.put(elementId, (VEdge<V, I>)seRec.element);
				}
				
				if (!vertexTypesMap.isEmpty()) vertexTypes = Optional.of(vertexTypesMap);
				if (!vertexesMap.isEmpty()) vertexes = Optional.of(vertexesMap);
				if (!edgeTypesMap.isEmpty()) edgeTypes = Optional.of(edgeTypesMap);
				if (!edgesMap.isEmpty()) edges = Optional.of(edgesMap);
			}
			
			VGraphDiff<V, I> diff = new VGraphDiff<>(originalFrom, graphName, vertexTypes, vertexes,
					edgeTypes, edges, subgraphsOpt, subgraphSync, destroyedRecord, graphElementRecordOpt);
			
			return diff;
		} finally {
			updateLock.readLock().unlock();
		}
	}
	
	protected void incrementReferenceCount(VElement<V, I> linkedElement) {
		SharedElementRec<V, I> elementRec = sharedElements.get(linkedElement.getElementId());
		
		if (elementRec != null) {
			elementRec.element = linkedElement;
			elementRec.refCount++;
		} else {
			elementRec = new SharedElementRec<V, I>(linkedElement);
			sharedElements.put(linkedElement.getElementId(), elementRec);
			elementRec.refCount++;
		}
	}
	
	protected void decrementReferenceCount(I elementId) {
		SharedElementRec<V, I> elementRec = sharedElements.get(elementId);
		elementRec.refCount--;
		if (elementRec.refCount == 0)
			sharedElements.remove(elementId);
	}
}
