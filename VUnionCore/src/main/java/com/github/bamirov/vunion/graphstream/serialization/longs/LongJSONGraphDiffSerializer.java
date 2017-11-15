package com.github.bamirov.vunion.graphstream.serialization.longs;

import com.github.bamirov.vunion.graphstream.serialization.JSONGraphDiffSerializer;
import com.github.bamirov.vunion.graphstream.serialization.StringGraphVersionSerializer;

public class LongJSONGraphDiffSerializer extends JSONGraphDiffSerializer<Long, Long> {
	public LongJSONGraphDiffSerializer(StringGraphVersionSerializer<Long> versionSerializer) {
		super(versionSerializer);
	}

	@Override
	protected String iToString(Long id) {
		return id.toString();
	}

	@Override
	protected Long stringToI(String id) {
		return Long.parseLong(id);
	}
}
