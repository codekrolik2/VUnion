package com.github.bamirov.vunion.graphstream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VGraphDestroyedRecord<V extends Comparable<V>> {
	protected V destroyReviveVersion;
	protected boolean isDestroyed;
}
