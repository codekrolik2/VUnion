package com.github.bamirov.vunion.graphstream;

import java.util.Map;
import java.util.Optional;

import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graphstream.serialization.JSONGraphDiffSerializer;
import com.github.bamirov.vunion.version.VGraphVersion;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class VGraphDiff<V extends Comparable<V>, I> {
	@SuppressWarnings("rawtypes")
	public static class LocalJSONGraphDiffSerializer extends JSONGraphDiffSerializer {
		@SuppressWarnings("unchecked")
		public LocalJSONGraphDiffSerializer() {
			super(VGraphVersion.serializer);
		}

		@Override
		protected String iToString(Object id) {
			return id.toString();
		}

		@Override
		protected Object stringToI(String id) {
			return null;
		}
	}
	
	public static final LocalJSONGraphDiffSerializer serializer = new LocalJSONGraphDiffSerializer();
	
	@NonNull
	protected VGraphVersion<V> from;
	
	//"TO" isn't really needed because it can be calculated by updating "FROM"
	//Assuming that filters filter out subgraphs from both Diff and Version.
	//protected VGraphVersion<V> to;
	
	@NonNull
	protected String graphName;
	
	@NonNull
	protected Optional<Map<I, VVertexType<V, I>>> vertexTypes;
	
	@NonNull
	protected Optional<Map<I, VVertex<V, I>>> vertexes;
	
	@NonNull
	protected Optional<Map<I, VEdgeType<V, I>>> edgeTypes;
	
	@NonNull
	protected Optional<Map<I, VEdge<V, I>>> edges;
	
	@NonNull
	protected Optional<Map<String, VSubgraphDiff<V, I>>> subgraphs;

	@NonNull
	protected Optional<VSubgraphSyncRecord<V>> subgraphSync;
	
	@NonNull
	protected Optional<VGraphDestroyedRecord<V>> destroyedRecord;

	@NonNull
	protected Optional<VGraphElementRecord<V, I>> graphElementRecord;

	public VGraphDiff(VGraphVersion<V> from, String graphName, 
			Optional<Map<I, VVertexType<V, I>>> vertexTypes, Optional<Map<I, VVertex<V, I>>> vertexes,
			Optional<Map<I, VEdgeType<V, I>>> edgeTypes, Optional<Map<I, VEdge<V, I>>> edges,
			Optional<Map<String, VSubgraphDiff<V, I>>> subgraphs, Optional<VSubgraphSyncRecord<V>> subgraphSync, 
			Optional<VGraphDestroyedRecord<V>> destroyedRecord, Optional<VGraphElementRecord<V, I>> graphElementRecord) {
		this.from = (from == null) ? VGraphVersion.getEmptyGraphVersion() : from;
		
		if (graphName == null)
			throw new NullPointerException("graphName");
		else
			this.graphName = graphName;
		
		this.vertexTypes = (vertexTypes == null) ? Optional.empty() : vertexTypes;
		this.vertexes = (vertexes == null) ? Optional.empty() : vertexes;
		this.edgeTypes = (edgeTypes == null) ? Optional.empty() : edgeTypes;
		this.edges = (edges == null) ? Optional.empty() : edges;
		
		this.subgraphs = (subgraphs == null) ? Optional.empty() : subgraphs;
		this.subgraphSync = (subgraphSync == null) ? Optional.empty() : subgraphSync;
		this.destroyedRecord = (destroyedRecord == null) ? Optional.empty() : destroyedRecord;
		this.graphElementRecord = (graphElementRecord == null) ? Optional.empty() : graphElementRecord;
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		return serializer.serializeGraphDiff(this).toString();
	}

	public VElement<V, I> getElement(I elementId) {
		VElement<V, I> element = null;

		element = getVertexType(elementId);
		if (element != null) return element;
		
		element = getVertex(elementId);
		if (element != null) return element;
		
		element = getEdgeType(elementId);
		if (element != null) return element;
		
		element = getEdge(elementId);
		
		return element;
	}

	public VVertexType<V, I> getVertexType(I elementId) {
		if (getVertexTypes().isPresent())
			return getVertexTypes().get().get(elementId);
		return null;
	}
	
	public VVertex<V, I> getVertex(I elementId) {
		if (getVertexes().isPresent())
			return getVertexes().get().get(elementId);
		return null;
	}
	
	public VEdgeType<V, I> getEdgeType(I elementId) {
		if (getEdgeTypes().isPresent())
			return getEdgeTypes().get().get(elementId);
		return null;
	}
	
	public VEdge<V, I> getEdge(I elementId) {
		if (getEdges().isPresent())
			return getEdges().get().get(elementId);
		return null;
	}
}
