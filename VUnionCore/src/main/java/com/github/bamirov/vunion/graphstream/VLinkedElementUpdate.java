package com.github.bamirov.vunion.graphstream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VLinkedElementUpdate<V extends Comparable<V>, I> {
	private final I linkedElementId;
	private final V linkedElementVersion;
}
