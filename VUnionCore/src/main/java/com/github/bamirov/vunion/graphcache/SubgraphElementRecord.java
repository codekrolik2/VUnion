package com.github.bamirov.vunion.graphcache;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VSubgraphElement;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubgraphElementRecord<V extends Comparable<V>, I> {
	V subgraphElementVersion;
	Optional<VSubgraphElement<V, I>> subgraphElement;
}
