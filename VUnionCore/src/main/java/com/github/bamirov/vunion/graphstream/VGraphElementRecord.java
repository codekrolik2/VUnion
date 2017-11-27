package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VGraphElement;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class VGraphElementRecord<V extends Comparable<V>, I> {
	protected V graphElementUpdateVersion;
	protected Optional<VGraphElement<V, I>> graphElement;
	
	@Builder
	public VGraphElementRecord(V graphElementUpdateVersion, Optional<VGraphElement<V, I>> graphElement) {
		this.graphElementUpdateVersion = graphElementUpdateVersion;
		this.graphElement = graphElement == null ? Optional.empty() : graphElement;
	}
}
