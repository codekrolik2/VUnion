package com.github.bamirov.vunion.graphstream;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class VSubgraphSyncRecord<V extends Comparable<V>> {
	protected V subgraphSyncVersion;
	@Singular
	protected List<String> subgraphNames;
}
