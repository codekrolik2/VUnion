package com.github.bamirov.vunion.graphstream.serialization;

import java.util.Map.Entry;

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
	
	public abstract String vToString(V version);
}
