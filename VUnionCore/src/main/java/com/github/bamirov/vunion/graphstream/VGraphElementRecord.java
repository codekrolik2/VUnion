package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VGraph;

import lombok.Getter;

@Getter
public class VGraphElementRecord<V extends Comparable<V>, I> {
	protected V graphElementUpdateVersion;
	protected Optional<VGraph<V, I>> graphElement;
}
