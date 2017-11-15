package com.github.bamirov.vunion.version;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.bamirov.vunion.graphstream.serialization.StringGraphVersionSerializer;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
@EqualsAndHashCode
public class VGraphVersion<V extends Comparable<V>> {
	@SuppressWarnings("rawtypes")
	public static class LocalStringGraphVersionSerializer extends StringGraphVersionSerializer {
		@Override
		protected String vToString(Comparable version) {
			return version.toString();
		}

		@Override
		protected Comparable stringToV(String version) {
			return null;
		}
	}
	
	public static final LocalStringGraphVersionSerializer serializer = new LocalStringGraphVersionSerializer();
	
	private Optional<V> graphVersion;
	
	@Singular
	private Map<String, V> subgraphVersions;
	
	public VGraphVersion(Optional<V> graphVersion, Map<String, V> subgraphVersions) {
		this.graphVersion = (graphVersion == null) ? Optional.empty() : graphVersion;
		this.subgraphVersions = subgraphVersions;
	}
	
	public static <S extends Comparable<S>> VGraphVersion<S> getEmptyGraphVersion() {
		return new VGraphVersion<S>(null, new HashMap<>());
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		return serializer.serializeGraphVersion(this);
	}
}
