package com.github.bamirov.vunion.graphstream.serialization.longs;

import com.github.bamirov.vunion.graphstream.serialization.StringGraphVersionSerializer;

public class LongStringGraphVersionSerializer extends StringGraphVersionSerializer<Long> {
	@Override
	protected String vToString(Long version) {
		return version.toString();
	}

	@Override
	protected Long stringToV(String version) {
		return Long.parseLong(version);
	}
}
