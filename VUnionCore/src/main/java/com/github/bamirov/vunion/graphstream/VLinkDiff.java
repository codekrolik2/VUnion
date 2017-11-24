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
	private final I linkedElementId;
	
	private final Optional<VLinkUpdate<V, I>> linkUpdate;
	private final Optional<V> linkedElementVersionUpdate;
	
	public VLinkDiff(I linkId, I linkedElementId, Optional<VLinkUpdate<V, I>> linkUpdate, 
			Optional<V> linkedElementVersionUpdate) {
		this.linkId = linkId;
		this.linkedElementId = linkedElementId;
		this.linkUpdate = linkUpdate == null ? Optional.empty() : linkUpdate;
		this.linkedElementVersionUpdate = linkedElementVersionUpdate == null ? Optional.empty() : linkedElementVersionUpdate;
	}
}
