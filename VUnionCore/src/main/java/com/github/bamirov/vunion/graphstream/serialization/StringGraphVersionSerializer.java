package com.github.bamirov.vunion.graphstream.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.github.bamirov.vunion.exceptions.MalformedVersionException;
import com.github.bamirov.vunion.version.VGraphVersion;

public abstract class StringGraphVersionSerializer<V extends Comparable<V>> {
	public String serializeGraphVersion(VGraphVersion<V> version) {
		StringBuilder graphVersion = new StringBuilder();
		
		graphVersion.append("[");
		
		if (version.getGraphVersion().isPresent()) {
			graphVersion.append(vToString(version.getGraphVersion().get()));
			
			if (!version.getSubgraphVersions().isEmpty())
				graphVersion.append(",");
		}
		
		boolean addComma = false;
		
		for (Entry<String, V> ent : version.getSubgraphVersions().entrySet()) {
			if (addComma)
				graphVersion.append(",");
			else
				addComma = true;
			
			String subgraph = ent.getKey();
			V subgraphVersion = ent.getValue();
			
			graphVersion.append(subgraph).append(":");
			graphVersion.append(vToString(subgraphVersion));
		}
		
		graphVersion.append("]");
		
		return graphVersion.toString();
	}
	
	public VGraphVersion<V> deserializeGraphVersion(String versionStr) throws MalformedVersionException {
		if ((versionStr.charAt(0) != '[') || (versionStr.charAt(versionStr.length() - 1) != ']'))
			versionStr = versionStr.trim();
		
		if ((versionStr.charAt(0) != '[') || (versionStr.charAt(versionStr.length() - 1) != ']'))
			throw new MalformedVersionException("Graph Version should be enclosed in []");
		
		String versionsStr = versionStr.substring(1, versionStr.length() - 1).trim();
		if (versionsStr.isEmpty())
			return VGraphVersion.<V>builder().build();
		
		String[] versions = versionsStr.split(",");
		
		Optional<V> graphVersion;
		int start = 0;
		if (!versions[0].contains(":")) {
			graphVersion = Optional.of(stringToV(versions[0]));
			start = 1;
		} else
			graphVersion = Optional.empty();
		
		Map<String, V> subgraphVersions = new HashMap<>();
		for (int i = start; i < versions.length; i++) {
			String[] parts = versions[i].split(":");
			
			String subgraph = parts[0];
			V version = stringToV(parts[1]);
			
			subgraphVersions.put(subgraph, version);
		}
		
		VGraphVersion<V> v = new VGraphVersion<V>(graphVersion, subgraphVersions);
		return v;
	}
	
	protected abstract String vToString(V version);
	
	protected abstract V stringToV(String version);
}
