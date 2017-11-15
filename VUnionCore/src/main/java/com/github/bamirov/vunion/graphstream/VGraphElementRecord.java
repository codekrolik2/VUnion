package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VGraph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class VGraphElementRecord<V extends Comparable<V>, I> {
	protected V graphElementUpdateVersion;
	protected Optional<VGraph<V, I>> graphElement;
	
	@Builder
	public VGraphElementRecord(V graphElementUpdateVersion, Optional<VGraph<V, I>> graphElement) {
		this.graphElementUpdateVersion = graphElementUpdateVersion;
		this.graphElement = graphElement == null ? Optional.empty() : graphElement;
	}
}
