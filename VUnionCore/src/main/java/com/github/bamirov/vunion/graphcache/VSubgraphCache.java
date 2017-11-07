package com.github.bamirov.vunion.graphcache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VSubgraph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
class VSubgraphCache<V extends Comparable<V>, I> {
	protected String name;
	protected V maxElementVersion;
	//maxElementVersion will not necessarily corellate with existing links,
	//because I want to allow filters that will filter out links and elements inside subgraphs 
	
	protected V elementSyncVersion;
	
	protected TreeMap<V, VLink<V, I>> subgraphTimeline = new TreeMap<V, VLink<V, I>>();
	protected Map<I, VLink<V, I>> subgraphLinksByElementId = new HashMap<I, VLink<V, I>>();
	
	protected Optional<VSubgraph<V, I>> subgraphElement;

	public V getSubgraphVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				maxElementVersion,
				elementSyncVersion,
				subgraphElement.isPresent() ? subgraphElement.get() : null
		});
		return Collections.max(list);
	}
}
