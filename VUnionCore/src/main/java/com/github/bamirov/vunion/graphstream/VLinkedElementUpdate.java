package com.github.bamirov.vunion.graphstream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VLinkedElementUpdate<V extends Comparable<V>, I> {
	private final I linkedElementId;
	private final V linkedElementVersion;
}
