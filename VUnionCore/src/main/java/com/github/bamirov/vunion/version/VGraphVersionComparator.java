package com.github.bamirov.vunion.version;

import java.util.Map.Entry;
import java.util.Optional;

public class VGraphVersionComparator<V extends Comparable<V>> {
	public boolean hasUpdatesSince(VGraphVersion<V> version, VGraphVersion<V> sinceVersion) {
		if (largerThan(version.getGraphVersion(), sinceVersion.getGraphVersion())) {
			return true;
		} else {
			if (largerThan(sinceVersion.getGraphVersion(), version.getGraphVersion())) {
				//At least one of version.SubgraphVersions
				//should be ( > sinceVersion.getGraphVersion) AND ( > corresponding SubgraphVersion)
				for (Entry<String, V> ent : version.getSubgraphVersions().entrySet()) {
					if ((largerThan(ent.getValue(), sinceVersion.getGraphVersion().get()))
						&&
						(largerThan(ent.getValue(), sinceVersion.getSubgraphVersions().get(ent.getKey()))))
						return true;
				}
			} else {
				//At least one of version.SubgraphVersions should be ( > corresponding SubgraphVersion)
				for (Entry<String, V> ent : version.getSubgraphVersions().entrySet()) {
					if (largerThan(ent.getValue(), sinceVersion.getSubgraphVersions().get(ent.getKey())))
						return true;
				}
			}
		}
		return false;
	}
	
	protected boolean largerThan(Optional<V> v1, Optional<V> v2) { 
		if ((v1 == null) || (!v1.isPresent()))
			return false;
		if ((v2 == null) || (!v2.isPresent()))
			return true;
		return largerThan(v1.get(), v2.get());
	}
	
	protected boolean largerThan(V v1, V v2) {
		if (v1 == null)
			return false;
		if (v2 == null)
			return true;
		return (v1.compareTo(v2) > 0);
	}
}
