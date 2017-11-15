package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VSubgraph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class VSubgraphElementRecord<V extends Comparable<V>, I> {
	protected V subgraphElementUpdateVersion;
	protected Optional<VSubgraph<V, I>> subgraphElement;
	
	public VSubgraphElementRecord(V subgraphElementUpdateVersion, Optional<VSubgraph<V, I>> subgraphElement) {
		this.subgraphElementUpdateVersion = subgraphElementUpdateVersion;
		this.subgraphElement = subgraphElement == null ? Optional.empty() : subgraphElement;
	}
}
