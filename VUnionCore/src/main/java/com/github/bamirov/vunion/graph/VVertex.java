package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VVertex<V extends Comparable<V>, I> extends VElement<V, I> {
	protected final I vertexTypeId;

	@Builder
	public VVertex(I elementId, V version, Optional<String> key, String content, 
			I vertexTypeId) {
		super(elementId, version, key, content);
		this.vertexTypeId = vertexTypeId;
	}
}
