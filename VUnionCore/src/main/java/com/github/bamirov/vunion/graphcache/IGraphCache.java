package com.github.bamirov.vunion.graphcache;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.version.VGraphVersion;

public interface IGraphCache<V extends Comparable<V>, I> {
	VGraphVersion<V> getVersion();
	
	VGraphDiff<V, I> getDiff(VGraphVersion<V> from);
	void applyDiff(VGraphDiff<V, I> diff) throws GraphMismatchException;
}
