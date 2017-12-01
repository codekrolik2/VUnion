package com.github.bamirov.vunion.graphcache;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graph.filter.IGraphDiffFilter;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.version.VGraphVersion;

public interface IGraphCache<V extends Comparable<V>, I> {
	VGraphVersion<V> getGraphVersion();
	
	VGraphDiff<V, I> getGraphDiff(VGraphVersion<V> from, IGraphDiffFilter<V, I> diffFilter) throws GraphMismatchException;
	void applyGraphDiff(VGraphDiff<V, I> diff) throws GraphMismatchException;
}
