package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VSubgraph<V extends Comparable<V>, I> extends VElement<V, I> {
	@Builder
	public VSubgraph(I elementId, V version, Optional<String> key, String content) {
		super(elementId, version, key, content);
	}
}
