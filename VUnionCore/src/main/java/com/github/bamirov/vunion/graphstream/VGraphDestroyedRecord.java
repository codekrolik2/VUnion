package com.github.bamirov.vunion.graphstream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VGraphDestroyedRecord<V extends Comparable<V>> {
	protected V destroyRecoverVersion;
	protected boolean isDestroyed;
}
