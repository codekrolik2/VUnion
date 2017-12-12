package com.github.bamirov.vunion.graph;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
public class VLink<V extends Comparable<V>, I> extends VElement<V, I> {
	protected final I linkedElementId;
	protected V linkedElementVersion;
	//TODO: mb String subgraph?
	//protected final I subgraphId;
	protected boolean isTombstone;
	
	public VLink(I elementId, V version, Optional<String> key, String content, 
			I linkedElementId, V linkedElementVersion, /*I subgraphId,*/ boolean isTombstone) {
		super(elementId, version, key, content);
		this.linkedElementId = linkedElementId;
		this.linkedElementVersion = linkedElementVersion;
		//this.subgraphId = subgraphId;
		this.isTombstone = isTombstone;
	}
	
	public V getTimelineVersion() {
		if (linkedElementVersion == null)
			return version;
		if (version == null)
			return linkedElementVersion;
		
		return version.compareTo(linkedElementVersion) > 0 ? version : linkedElementVersion;
	}
}
