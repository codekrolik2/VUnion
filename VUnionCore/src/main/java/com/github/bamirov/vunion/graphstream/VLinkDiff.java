package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class VLinkDiff<V extends Comparable<V>, I> {
	private final I linkId;
	
	private final Optional<VLinkUpdate<V, I>> linkUpdate;
	private final Optional<VLinkedElementUpdate<V, I>> linkedElementUpdate;
}
