package com.github.bamirov.vunion.graphstream;

import java.util.List;
import java.util.Optional;

import lombok.Getter;

@Getter
public class VSubgraphDiff<V extends Comparable<V>, I> {
	protected String name;
	
	//We can't always calculate subgraphDiff TO version, because some advanced filters may exist,
	//e.g. Filters that would filter links and other elements out, instead of whole subgraphs.
	protected V subgraphVersionTo;
	
	protected List<VLinkDiff<V, I>> linkUpdates;

	protected Optional<VElementSyncRecord<V, I>> elementSync;
	
	protected Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord;
}
