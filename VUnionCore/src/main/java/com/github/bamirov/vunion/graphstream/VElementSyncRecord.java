package com.github.bamirov.vunion.graphstream;

import java.util.List;

import lombok.Getter;

@Getter
public class VElementSyncRecord<V extends Comparable<V>, I> {
	protected V elementSyncVersion;
	protected List<I> elementIds;
}
