package com.github.bamirov.vunion.graphcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;
import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersion.VGraphVersionBuilder;

import lombok.Getter;

@Getter
public class VGraphCache<V extends Comparable<V>, I> implements IGraphCache<V, I> {
	protected GraphDiffChecker<V, I> graphDiffChecker = new GraphDiffChecker<V, I>();

	//-----------------------------
	
	protected String graphName;
	
	protected Optional<V> subgraphSyncVersion = Optional.empty();
	
	protected Optional<V> destroyRecoverVersion = Optional.empty();
	protected boolean isDestroyed = false;
	
	protected Optional<VGraphElementRecord<V, I>> graphElementRecord = Optional.empty();

	protected Map<I, SharedElementRec<V, I>> sharedElements = new HashMap<>();
	
	protected Map<String, VSubgraphCache<V, I>> subgraphs = new HashMap<>();

	protected ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
	
	
	
	protected V getGraphUpdateVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphSyncVersion, 
				destroyRecoverVersion,
				graphElementRecord.isPresent() ? graphElementRecord.get().getGraphElementUpdateVersion() : null
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
	
	protected boolean subgraphDiffElementExists(I elementId, VSubgraphDiff<V, I> sgDiff) {
		if (sgDiff.getLinkUpdatesByElementId().isPresent())
			return sgDiff.getLinkUpdatesByElementId().get().containsKey(elementId);
		return false;
	}
	
	public void removeSubgraphCache(VSubgraphCache<V, I> subgraphCache, List<I> orphanElements) {
		for (VLink<V, I> link : subgraphCache.getSubgraphLinksByElementId().values())
			decrementReferenceCount(link.getElementId());
		
		subgraphs.remove(subgraphCache.name);
	}
	
	@Override
	public void applyGraphDiff(VGraphDiff<V, I> diff) throws GraphMismatchException {
		//1. Subgraph name must match
		if (!diff.getGraphName().equals(graphName))
			throw new GraphMismatchException(
					String.format("Graph name doesn't match: diff [%s] cache [%s]", 
							diff.getGraphName(), graphName));
		
		//2. Update destroyed status
		if (diff.getDestroyedRecord().isPresent()) {
			destroyRecoverVersion = Optional.of(diff.getDestroyedRecord().get().getDestroyRecoverVersion());
			isDestroyed = diff.getDestroyedRecord().get().isDestroyed();
			
			//Remove all data (except for delete info)
			graphElementRecord = Optional.empty();
			sharedElements.clear();
			subgraphs.clear();
		}
		
		//3. Check graph diff
		//TODO: Check graph diff - after cache is updated, fix
		graphDiffChecker.sanityCheckGraphDiff(this, diff);
		
		/*
		  TODO: This is how it initially was, do I need it here or above?
		  
		 if (diff.getDestroyedRecord().isPresent()) {
			//If destroyed record is present, remove all data (except for destroyed info)
			graphElement = Optional.empty();
			sharedElements.clear();
			subgraphs.clear();
		}*/
		
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

		List<I> orphanElements = new ArrayList<I>();
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
				removeSubgraphCache(subgraphCache, orphanElements);
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
				subgraphCache.applySubgraphDiff(subgraphDiff, diff, this, orphanElements);
			}
		}
	}
	
	@Override
	public VGraphDiff<V, I> getGraphDiff(VGraphVersion<V> from) {
		// TODO Auto-generated method stub
		
		//For test purposes, sanityCheckGraphDiff(...) before returning
		
		return null;
	}
	
	public void incrementReferenceCount(VElement<V, I> linkedElement) {
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
	
	public void decrementReferenceCount(I elementId) {
		SharedElementRec<V, I> elementRec = sharedElements.get(elementId);
		elementRec.refCount--;
		if (elementRec.refCount == 0)
			sharedElements.remove(elementId);
	}
}
