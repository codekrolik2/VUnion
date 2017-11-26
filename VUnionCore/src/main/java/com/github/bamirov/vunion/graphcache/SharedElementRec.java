package com.github.bamirov.vunion.graphcache;

import com.github.bamirov.vunion.graph.VElement;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SharedElementRec<V extends Comparable<V>, I> {
	protected int refCount = 0;
	@NonNull protected VElement<V, I> element;
}
