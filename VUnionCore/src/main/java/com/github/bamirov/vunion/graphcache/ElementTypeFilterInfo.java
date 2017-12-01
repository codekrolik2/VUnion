package com.github.bamirov.vunion.graphcache;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ElementTypeFilterInfo<I> {
	protected Set<I> allowedVertexTypes = new HashSet<>();
	protected Set<I> allowedEdgeTypes = new HashSet<>();
	protected Set<I> prohibitedVertexTypes = new HashSet<>();
	protected Set<I> prohibitedEdgeTypes = new HashSet<>();
}
