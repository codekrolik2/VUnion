package com.github.bamirov.vunion.graph.filter;

import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VVertex;

public interface IGraphDiffFilter<V extends Comparable<V>, I> {
	boolean isSubgraphAllowed(String subgraphName);
	
	boolean isVertexTypeAllowed(String subgraphName, String vertexTypeName);
	boolean isEdgeTypeAllowed(String subgraphName, String edgeTypeName);
	
	/**
	 * VertexType check is not performed by this method. 
	 * Should be done separately by calling isVertexTypeAllowed.
	 * 
	 * @param subgraphName
	 * @param vertexTypeName
	 * @param vertex
	 * @return
	 */
	boolean isVertexAllowed(String subgraphName, VVertex<V, I> vertex);
	
	/**
	 * EdgeType check is not performed by this method.
	 * Should be done separately by calling isEdgeTypeAllowed.
	 * 
	 * @param subgraphName
	 * @param edgeTypeName
	 * @param edge
	 * @return
	 */
	boolean isEdgeAllowed(String subgraphName, VEdge<V, I> edge);
}
