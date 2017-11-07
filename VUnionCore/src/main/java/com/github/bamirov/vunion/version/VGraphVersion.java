package com.github.bamirov.vunion.version;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@AllArgsConstructor
@Builder
@Getter
public class VGraphVersion<V extends Comparable<V>> {
	private Optional<V> graphVersion;
	
	@Singular
	private Map<String, V> subgraphVersions;
	
	public static <S extends Comparable<S>> VGraphVersion<S> getEmptyGraphVersion() {
		return new VGraphVersion<S>(null, null);
	}
}
