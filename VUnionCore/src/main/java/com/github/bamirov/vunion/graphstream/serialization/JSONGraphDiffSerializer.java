package com.github.bamirov.vunion.graphstream.serialization;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDestroyedRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VLinkedElementUpdate;
import com.github.bamirov.vunion.graphstream.VLinkUpdate;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphElementRecord;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;

public abstract class JSONGraphDiffSerializer<V extends Comparable<V>, I> {
	public StringGraphVersionSerializer<V> versionSerializer;
	
	public JSONObject serializeGraphDiff(VGraphDiff<V, I> diff) {
		JSONObject graphObject = new JSONObject();
		
		graphObject.put("from", versionSerializer.serializeGraphVersion(diff.getFrom()));
		graphObject.put("graphName", diff.getGraphName());
		
		if (diff.getVertexes().isPresent())
			if (!diff.getVertexes().get().isEmpty())
				graphObject.put("vertexes", serializeVertexes(diff.getVertexes().get()));
		
		if (diff.getEdges().isPresent())
			if (!diff.getEdges().get().isEmpty())
				graphObject.put("edges", serializeEdges(diff.getEdges().get()));
		
		if (diff.getSubgraphs().isPresent())
			if (!diff.getSubgraphs().get().isEmpty())
				graphObject.put("subgraphs", serializeSubgraphs(diff.getSubgraphs().get()));
		
		if (diff.getSubgraphSync().isPresent())
			graphObject.put("subgraphSync", serializeSubgraphSync(diff.getSubgraphSync().get()));
		
		if (diff.getDestroyedRecord().isPresent())
			graphObject.put("destroyedRecord", serializeDestroyedRecord(diff.getDestroyedRecord().get()));
		
		if (diff.getGraphElementRecord().isPresent())
			graphObject.put("graphElementRecord", serializeGraphElementRecord(diff.getGraphElementRecord().get()));
			
		return graphObject;
	}
	
	private JSONObject serializeSubgraphSync(VSubgraphSyncRecord<V> subgraphSync) {
		JSONObject subgraphSyncJSON = new JSONObject(); 
		
		subgraphSyncJSON.put("subgraphSyncVersion", versionSerializer.vToString(subgraphSync.getSubgraphSyncVersion()));
		
		JSONArray subgraphNames = new JSONArray();
		
		if (subgraphSync.getSubgraphNames() != null)
			for (String name : subgraphSync.getSubgraphNames())
				subgraphNames.put(name);
		
		subgraphSyncJSON.put("subgraphNames", subgraphNames);
		
		return subgraphSyncJSON;
	}
	
	private JSONObject serializeDestroyedRecord(VGraphDestroyedRecord<V> destroyedRecord) {
		JSONObject destroyedRecordJSON = new JSONObject();
		
		destroyedRecordJSON.put("destroyReviveVersion", 
				versionSerializer.vToString(destroyedRecord.getDestroyReviveVersion()));
		destroyedRecordJSON.put("isDestroyed", destroyedRecord.isDestroyed());
		
		return destroyedRecordJSON;
	}
	
	private JSONObject serializeGraphElementRecord(VGraphElementRecord<V, I> graphElementRecord) {
		JSONObject graphElementRecordJSON = new JSONObject();

		graphElementRecordJSON.put("graphElementUpdateVersion", 
				versionSerializer.vToString(graphElementRecord.getGraphElementUpdateVersion()));
		
		if (graphElementRecord.getGraphElement().isPresent())
			graphElementRecordJSON.put("graphElement", serializeElement(graphElementRecord.getGraphElement().get()));
		
		return graphElementRecordJSON;
	}
	
	private JSONArray serializeSubgraphs(List<VSubgraphDiff<V, I>> subgraphs) {
		JSONArray subgraphsJSON = new JSONArray();
		
		for (VSubgraphDiff<V, I> subgraph : subgraphs) {
			JSONObject subgraphJSON = serializeSubgraph(subgraph);
			subgraphsJSON.put(subgraphJSON);
		}
		
		return subgraphsJSON;
	}
	
	private JSONObject serializeSubgraph(VSubgraphDiff<V, I> subgraph) {
		JSONObject subgraphJSON = new JSONObject();
		
		subgraphJSON.put("name", subgraph.getName());
		subgraphJSON.put("subgraphVersionTo", versionSerializer.vToString(subgraph.getSubgraphVersionTo()));
		
		if ((subgraph.getLinkUpdates() != null) && (!subgraph.getLinkUpdates().isEmpty())) {
			JSONArray updates = new JSONArray();
			for (VLinkDiff<V, I> link : subgraph.getLinkUpdates())
				updates.put(serializeLink(link));
			subgraphJSON.put("linkUpdates", updates);
		}
		
		if (subgraph.getElementSync().isPresent())
			subgraphJSON.put("elementSync", serializeElementSyncRecord(subgraph.getElementSync().get()));
		
		if (subgraph.getSubgraphElementRecord().isPresent())
			subgraphJSON.put("subgraphElementRecord", 
					serializeSubgraphElementRecord(subgraph.getSubgraphElementRecord().get()));
		
		return subgraphJSON;
	}
	
	private JSONObject serializeSubgraphElementRecord(VSubgraphElementRecord<V, I> subgraphElementRecord) {
		JSONObject subgraphElementJSON = new JSONObject();
		
		subgraphElementJSON.put("subgraphElementUpdateVersion", 
				versionSerializer.vToString(subgraphElementRecord.getSubgraphElementUpdateVersion()));
		
		if (subgraphElementRecord.getSubgraphElement().isPresent())
			subgraphElementJSON.put("subgraphElement", 
					serializeElement(subgraphElementRecord.getSubgraphElement().get()));
		
		return subgraphElementJSON;
	}
	
	private JSONObject serializeElementSyncRecord(VElementSyncRecord<V, I> elementSync) {
		JSONObject elementSyncJSON = new JSONObject();
		
		elementSyncJSON.put("elementSyncVersion", versionSerializer.vToString(elementSync.getElementSyncVersion()));
		
		JSONArray elementIdsArray = new JSONArray();
		
		if (elementSync.getElementIds() != null)
			for (I id : elementSync.getElementIds())
				elementIdsArray.put(iToString(id));
		
		elementSyncJSON.put("elementIds", elementIdsArray);
		
		return elementSyncJSON;
	}
	
	private JSONObject serializeLink(VLinkDiff<V, I> link) {
		JSONObject linkJSON = new JSONObject();
		
		linkJSON.put("linkId", iToString(link.getLinkId()));
		
		if (link.getLinkUpdate().isPresent())
			linkJSON.put("linkUpdate", serializeLinkUpdate(link.getLinkUpdate().get()));
		
		if (link.getLinkedElementUpdate().isPresent())
			linkJSON.put("linkedElementUpdate", serializeLinkElementUpdate(link.getLinkedElementUpdate().get()));
		
		return linkJSON;
	}
	
	private JSONObject serializeLinkUpdate(VLinkUpdate<V, I> linkUpdate) {
		JSONObject linkUpdateJSON = serializeElement(linkUpdate);
		linkUpdateJSON.put("isTombstone", linkUpdate.isTombstone());
		
		return linkUpdateJSON;
	}
	
	private JSONObject serializeLinkElementUpdate(VLinkedElementUpdate<V, I> linkElementUpdate) {
		JSONObject linkElementUpdateJSON = new JSONObject();
		
		linkElementUpdateJSON.put("linkedElementId", iToString(linkElementUpdate.getLinkedElementId()));
		linkElementUpdateJSON.put("linkedElementVersion", 
				versionSerializer.vToString(linkElementUpdate.getLinkedElementVersion()));
		
		return linkElementUpdateJSON;
	}
	
	private JSONArray serializeEdges(Map<I, VEdge<V, I>> edges) {
		JSONArray edgesJSON = null;
		if ((edges != null) && (!edges.isEmpty())) {
			edgesJSON = new JSONArray();
			
			for (VEdge<V, I> edge : edges.values()) {
				JSONObject element = serializeElement(edge);
				
				element.put("edgeTypeId", iToString(edge.getEdgeTypeId()));
				element.put("vertexFromId", iToString(edge.getVertexFromId()));
				element.put("vertexToId", iToString(edge.getVertexToId()));
				element.put("isDirected", edge.isDirected());
				
				edgesJSON.put(element);
			}
		}
		
		return edgesJSON;
	}
	
	private JSONArray serializeVertexes(Map<I, VVertex<V, I>> vertexes) {
		JSONArray vertexesJSON = null;
		if ((vertexes != null) && (!vertexes.isEmpty())) {
			vertexesJSON = new JSONArray();
			
			for (VVertex<V, I> vertex : vertexes.values()) {
				JSONObject element = serializeElement(vertex);
				element.put("vertexTypeId", iToString(vertex.getVertexTypeId()));
				
				vertexesJSON.put(element);
			}
		}
		
		return vertexesJSON;
	}
	
	private JSONObject serializeElement(VElement<V, I> element) {
		JSONObject elementJson = new JSONObject();
		
		elementJson.put("elementId", iToString(element.getElementId()));
		elementJson.put("version", versionSerializer.vToString(element.getVersion()));
		
		if (element.getKey().isPresent())
			elementJson.put("key", element.getKey().get());
		
		elementJson.put("content", element.getContent());
		
		return elementJson;
	}

	protected abstract String iToString(I id);
}
