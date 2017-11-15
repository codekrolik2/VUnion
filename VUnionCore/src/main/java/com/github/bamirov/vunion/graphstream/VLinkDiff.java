package com.github.bamirov.vunion.graphstream;

import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class VLinkDiff<V extends Comparable<V>, I> {
	private final I linkId;
	
	private final Optional<VLinkUpdate<V, I>> linkUpdate;
	private final Optional<VLinkedElementUpdate<V, I>> linkedElementUpdate;
	
	public VLinkDiff(I linkId, Optional<VLinkUpdate<V, I>> linkUpdate, 
			Optional<VLinkedElementUpdate<V, I>> linkedElementUpdate) {
		this.linkId = linkId;
		this.linkUpdate = linkUpdate == null ? Optional.empty() : linkUpdate;
		this.linkedElementUpdate = linkedElementUpdate == null ? Optional.empty() : linkedElementUpdate;
	}
}
