package com.github.bamirov.vunion.graphstream;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.version.VGraphVersion;

import lombok.Getter;

@Getter
public class VGraphDiff<V extends Comparable<V>, I> {
	protected VGraphVersion<V> from;
	
	//"TO" isn't really needed because it can be calculated by updating "FROM"
	//Assuming that filters filter out subgraphs from both Diff and Version.  
	//protected VGraphVersion<V> to;
	
	protected String graphName;
	
	protected Optional<Map<I, VVertex<V, I>>> vertexes;
	protected Optional<Map<I, VEdge<V, I>>> edges;
	
	protected Optional<List<VSubgraphDiff<V, I>>> subgraphs;
	protected Optional<VSubgraphSyncRecord<V>> subgraphSync;
	
	protected Optional<VGraphDestroyedRecord<V>> destroyedRecord;

	protected Optional<VGraphElementRecord<V, I>> graphElementRecord;
}
