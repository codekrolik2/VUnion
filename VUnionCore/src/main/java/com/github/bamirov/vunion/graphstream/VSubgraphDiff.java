package com.github.bamirov.vunion.graphstream;

import java.util.Map;
import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class VSubgraphDiff<V extends Comparable<V>, I> {
	@NonNull
	protected String name;
	
	//We can't always calculate subgraphDiff TO version, because some advanced filters may exist,
	//e.g. Filters that would filter links and other elements out, instead of whole subgraphs.
	@NonNull
	protected V subgraphVersionTo;
	
	@NonNull
	protected Optional<Map<I, VLinkDiff<V, I>>> linkUpdatesByElementId;

	@NonNull
	protected Optional<VElementSyncRecord<V, I>> elementSync;
	
	@NonNull
	protected Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord;
	
	public VSubgraphDiff(String name, V subgraphVersionTo, Optional<Map<I, VLinkDiff<V, I>>> linkUpdatesByElementId,
			Optional<VElementSyncRecord<V, I>> elementSync, Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord) {
		if (name == null)
			throw new NullPointerException("name");
		else
			this.name = name;
			
		if (subgraphVersionTo == null)
			throw new NullPointerException("subgraphVersionTo");
		else
			this.subgraphVersionTo = subgraphVersionTo;
		
		this.linkUpdatesByElementId = linkUpdatesByElementId == null ? Optional.empty() : linkUpdatesByElementId;
		this.elementSync = elementSync == null ? Optional.empty() : elementSync;
		this.subgraphElementRecord = subgraphElementRecord == null ? Optional.empty() : subgraphElementRecord;
	}
}
