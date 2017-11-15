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
	protected final I linkElementId;
	protected V linkElementVersion;
	protected final I subgraphId;
	protected boolean isTombstone;
	
	public VLink(I elementId, V version, Optional<String> key, String content, 
			I linkElementId, V linkElementVersion, I subgraphId, boolean isTombstone) {
		super(elementId, version, key, content);
		this.linkElementId = linkElementId;
		this.linkElementVersion = linkElementVersion;
		this.subgraphId = subgraphId;
		this.isTombstone = isTombstone;
	}
	
	public V getTimelineVersion() {
		if (linkElementVersion == null)
			return version;
		if (version == null)
			return linkElementVersion;
		
		return version.compareTo(linkElementVersion) > 0 ? version : linkElementVersion;
	}
}
