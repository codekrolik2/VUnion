package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VVertexType<V extends Comparable<V>, I> extends VElement<V, I> {
	protected final String vertexTypeName;

	public VVertexType(I elementId, V lastVersion, Optional<String> key, String content, 
			String vertexTypeName) {
		super(elementId, lastVersion, key, content);
		this.vertexTypeName = vertexTypeName;
	}
}
