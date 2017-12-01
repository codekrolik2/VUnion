Diff filter abstraction defines the following ways of filtering Graph Diff/stream elements.

1) Filter Subgraphs by name
2) Filter VertexTypes and corresponding Vertexes by name
3) Filter EdgeTypes and corresponding Edges by name
4) Filter Edges
5) Filter Vertexes

	public interface IGraphDiffFilter<V extends Comparable<V>, I> {
		boolean isSubgraphAllowed(String subgraphName);
	
		boolean isVertexTypeAllowed(String subgraphName, String vertexTypeName);
		boolean isEdgeTypeAllowed(String subgraphName, String edgeTypeName);
	
		boolean isVertexAllowed(String subgraphName, String vertexTypeName, VVertex<V, I> vertex);
		boolean isEdgeAllowed(String subgraphName, String edgeTypeName, VEdge<V, I> edge);
	}

Important points about filtering:
1) Filtering subgraphs will affect both GraphDiff and Version.
Filtered out subgraphs will be excluded from both.
TODO: implement version comparator with filter

2) Filtered VertexTypes and EdgeTypes with corresponding Vertexes and Edges will be excluded from Diffs.
VertexTypes and EdgeTypes can't be filtered out without corresponding Vertexes and Edges because that will result in existence of Vertexes/Edges of wissing types.

3) Filtered out Edges will be excluded from diffs. 
This won't automatically filter out any EdgeTypes and may result in orphaned EdgeTypes with no Edges attached.
Filtering out EdgeTypes should be done using EdgeTypes filters.

4) Filtered out Vertexes will be excluded from diffs. 
This won't automatically filter out any EdgeTypes and may result in orphaned EdgeTypes with no Edges attached.
Filtering out EdgeTypes should be done using EdgeTypes filters.

5) Filtering out VertexTypes and Vertexes might result in orphaned Edges, i.e. Edges with From or/and To Vertexes missing from diff/cache. Since VertexTypes and Vertex filters may be arbitrary, there is no elegant way to address this problem that I can think of now.
However, in case VertexTypes/Vertex filters are not used, we still want to use Edge consistency check mechanisms.
For that reason, GraphDiffChecker has flag "relaxedEdgeCheck" that allows to bypass the check for Edge's From/To Vertex consistency. This flag should only be set when VertexTypes/Vertex filter is implemented and described Edge From/To Vertex inconsistency is expected, otherwise the check should be kept enabled.
