package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import com.github.bamirov.vunion.graph.VElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VLinkUpdate<V extends Comparable<V>, I> extends VElement<V, I> {
	private final boolean isTombstone;

	public VLinkUpdate(I elementId, V version, Optional<String> key, String content, 
			boolean isTombstone) {
		super(elementId, version, key, content);
		this.isTombstone = isTombstone;
	}
}
