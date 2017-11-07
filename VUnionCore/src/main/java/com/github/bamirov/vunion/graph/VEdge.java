package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VEdge<V extends Comparable<V>, I> extends VElement<V, I> {
	protected final I edgeTypeId;
	protected final I vertexFromId;
	protected final I vertexToId;
	protected boolean isDirected;
	
	public VEdge(I elementId, V lastVersion, Optional<String> key, String content, 
			I edgeTypeId, I vertexFromId, I vertexToId, boolean isDirected) {
		super(elementId, lastVersion, key, content);
		this.edgeTypeId = edgeTypeId;
		this.vertexFromId = vertexFromId;
		this.vertexToId = vertexToId;
		this.isDirected = isDirected;
	}
}
