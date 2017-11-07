package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VElement<V extends Comparable<V>, I> {
	protected final I elementId;
	protected V version;
	
	protected Optional<String> key;
	protected String content;
	
	//Neither graphId nor graphName is required here, since we operate on graph diffs and caches anyway, 
	//and graph elements/other contents can't cross the graph boundary.
	//protected I graphId;
}
