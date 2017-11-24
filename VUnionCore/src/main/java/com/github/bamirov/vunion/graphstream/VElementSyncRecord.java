package com.github.bamirov.vunion.graphstream;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VElementSyncRecord<V extends Comparable<V>, I> {
	protected V elementSyncVersion;
	@Singular
	protected Set<I> elementIds;
}
