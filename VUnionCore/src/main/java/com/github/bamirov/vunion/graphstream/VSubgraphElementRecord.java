package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VSubgraph;

import lombok.Getter;

@Getter
public class VSubgraphElementRecord<V extends Comparable<V>, I> {
	protected V subgraphElementUpdateVersion;
	protected Optional<VSubgraph<V, I>> subgraphElement;
}
