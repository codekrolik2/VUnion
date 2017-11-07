package com.github.bamirov.vunion.graphstream;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VSubgraphSyncRecord<V extends Comparable<V>> {
	protected V subgraphSyncVersion;
	protected List<String> subgraphNames;
}
