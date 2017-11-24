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
import com.github.bamirov.vunion.graph.VGraphElement;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersion.VGraphVersionBuilder;

import lombok.Getter;

@Getter
public class VGraphCache<V extends Comparable<V>, I> implements IGraphCache<V, I> {
	protected String graphName;
	
	protected Optional<V> subgraphSyncVersion = null;
	
	protected Optional<V> destroyRecoverVersion = null;
	protected boolean isDestroyed = false;
	
	protected Optional<VGraphElement<V, I>> graphElement = Optional.empty();

	protected Map<I, SharedElementRec<V, I>> sharedElements = new HashMap<>();
	
	protected Map<String, VSubgraphCache<V, I>> subgraphs = new HashMap<>();

	protected ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
	
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
	
	
	
	protected boolean subgraphDiffElementExists(I elementId, VSubgraphDiff<V, I> sgDiff) {
		if (sgDiff.getLinkUpdatesByElementId().isPresent())
			return sgDiff.getLinkUpdatesByElementId().get().containsKey(elementId);
		return false;
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
