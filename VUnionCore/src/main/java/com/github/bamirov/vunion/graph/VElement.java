package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class VElement<V extends Comparable<V>, I> {
	protected final I elementId;
	protected V version;
	
	protected Optional<String> key;
	protected String content;
	
	public VElement(I elementId, V version, Optional<String> key, String content) {
		this.elementId = elementId;
		this.version = version;
		this.key = key == null ? Optional.empty() : key;
		this.content = content;
	}
	
	//Neither graphId nor graphName is required here, since we operate on graph diffs and caches anyway, 
	//and graph elements/other contents can't cross the graph boundary.
	//protected I graphId;
}
