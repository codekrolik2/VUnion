package com.github.bamirov.vunion.graphcache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VSubgraphElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
class VSubgraphCache<V extends Comparable<V>, I> {
	protected String name;
	
	//subgraphVersionTo is always max subgraphVersion
	protected V subgraphVersionTo;
	//subgraphVersionTo will not necessarily corellate with existing links,
	//because I want to allow filters that will filter out links and elements inside subgraphs 
	
	protected Optional<V> elementSyncVersion;
	
	protected TreeMap<V, VLink<V, I>> subgraphTimeline = new TreeMap<>();
	protected Map<I, VLink<V, I>> subgraphLinksByElementId = new HashMap<>();
	
	protected Map<I, Set<I>> vertexesByType = new HashMap<>();
	protected Map<I, Set<I>> edgesByType = new HashMap<>();
	
	protected Optional<VSubgraphElement<V, I>> subgraphElement;

	//TODO: this is probably not needed, since subgraphVersionTo is always max subgraphVersion
	public V getSubgraphVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphVersionTo,
				elementSyncVersion.isPresent() ? elementSyncVersion.get() : null,
				subgraphElement.isPresent() ? subgraphElement.get() : null
		});
		return Collections.max(list);
	}
}
