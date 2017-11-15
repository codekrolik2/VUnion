package com.github.bamirov.vunion.graphstream.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.bamirov.vunion.exceptions.MalformedVersionException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VGraph;
import com.github.bamirov.vunion.graph.VSubgraph;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDestroyedRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VLinkUpdate;
import com.github.bamirov.vunion.graphstream.VLinkedElementUpdate;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphElementRecord;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;
import com.github.bamirov.vunion.version.VGraphVersion;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class JSONGraphDiffSerializer<V extends Comparable<V>, I> {
	public StringGraphVersionSerializer<V> versionSerializer;
	
	//-----------------------------
	
	public VGraphDiff<V, I> deserializeGraphDiff(JSONObject diffJSON) throws JSONException, MalformedVersionException {
		VGraphVersion<V> from = versionSerializer.deserializeGraphVersion(diffJSON.getString("from"));
		String graphName = diffJSON.getString("graphName");
		
		Optional<Map<I, VVertexType<V, I>>> vertexTypesOpt;
		if (diffJSON.has("vertexTypes")) {
			Map<I, VVertexType<V, I>> vertexTypes = new HashMap<>();
			JSONArray vertexTypesJSON = diffJSON.getJSONArray("vertexTypes");
			
			for (int i = 0; i < vertexTypesJSON.length(); i++) {
				JSONObject vertexTypeJSON = vertexTypesJSON.getJSONObject(i);
				VVertexType<V, I> vertexType = deserializeVertexType(vertexTypeJSON);
				
				vertexTypes.put(vertexType.getElementId(), vertexType);
			}
			
			vertexTypesOpt = Optional.of(vertexTypes);
		} else
			vertexTypesOpt = Optional.empty();
		
		Optional<Map<I, VVertex<V, I>>> vertexesOpt;
		if (diffJSON.has("vertexes")) {
			Map<I, VVertex<V, I>> vertexes = new HashMap<>();
			JSONArray vertexesJSON = diffJSON.getJSONArray("vertexes");
			
			for (int i = 0; i < vertexesJSON.length(); i++) {
				JSONObject vertexJSON = vertexesJSON.getJSONObject(i);
				VVertex<V, I> vertex = deserializeVertex(vertexJSON);
				
				vertexes.put(vertex.getElementId(), vertex);
			}
			
			vertexesOpt = Optional.of(vertexes);
		} else
			vertexesOpt = Optional.empty();
		
		Optional<Map<I, VEdgeType<V, I>>> edgeTypesOpt;
		if (diffJSON.has("edgeTypes")) {
			Map<I, VEdgeType<V, I>> edgeTypes = new HashMap<>();
			JSONArray edgeTypesJSON = diffJSON.getJSONArray("edgeTypes");
			
			for (int i = 0; i < edgeTypesJSON.length(); i++) {
				JSONObject edgeTypeJSON = edgeTypesJSON.getJSONObject(i);
				VEdgeType<V, I> edgeType = deserializeEdgeType(edgeTypeJSON);
				
				edgeTypes.put(edgeType.getElementId(), edgeType);
			}
			
			edgeTypesOpt = Optional.of(edgeTypes);
		} else
			edgeTypesOpt = Optional.empty();
		
		Optional<Map<I, VEdge<V, I>>> edgesOpt;
		if (diffJSON.has("edges")) {
			Map<I, VEdge<V, I>> edges = new HashMap<>();
			JSONArray edgesJSON = diffJSON.getJSONArray("edges");
			
			for (int i = 0; i < edgesJSON.length(); i++) {
				JSONObject edgeJSON = edgesJSON.getJSONObject(i);
				VEdge<V, I> edge = deserializeEdge(edgeJSON);
				
				edges.put(edge.getElementId(), edge);
			}
			
			edgesOpt = Optional.of(edges);
		} else
			edgesOpt = Optional.empty();
		
		Optional<List<VSubgraphDiff<V, I>>> subgraphsOpt;
		if (diffJSON.has("subgraphs")) {
			List<VSubgraphDiff<V, I>> subgraphs = new ArrayList<>();
			JSONArray subgraphsJSON = diffJSON.getJSONArray("subgraphs");
			
			for (int i = 0; i < subgraphsJSON.length(); i++) {
				JSONObject subgraphJSON = subgraphsJSON.getJSONObject(i);
				VSubgraphDiff<V, I> subgraph = deserializeSubgraphDiff(subgraphJSON);
				
				subgraphs.add(subgraph);
			}
			
			subgraphsOpt = Optional.of(subgraphs);
		} else
			subgraphsOpt = Optional.empty();
		
		Optional<VSubgraphSyncRecord<V>> subgraphSyncOpt;
		if (diffJSON.has("subgraphSync")) {
			JSONObject subgraphSyncJSON = diffJSON.getJSONObject("subgraphSync");
			VSubgraphSyncRecord<V> subgraphSync = deserializeSubgraphSync(subgraphSyncJSON);
			subgraphSyncOpt = Optional.of(subgraphSync);
		} else
			subgraphSyncOpt = Optional.empty();
		
		Optional<VGraphDestroyedRecord<V>> destroyedRecordOpt;
		if (diffJSON.has("destroyedRecord")) {
			JSONObject destroyedRecordJSON = diffJSON.getJSONObject("destroyedRecord");
			VGraphDestroyedRecord<V> destroyedRecord = deserializeDestroyedRecord(destroyedRecordJSON);
			destroyedRecordOpt = Optional.of(destroyedRecord);
		} else
			destroyedRecordOpt = Optional.empty();
		
		Optional<VGraphElementRecord<V, I>> graphElementRecordOpt;
		if (diffJSON.has("graphElementRecord")) {
			JSONObject graphElementRecordJSON = diffJSON.getJSONObject("graphElementRecord");
			VGraphElementRecord<V, I> graphElementRecord = deserializeGraphElementRecord(graphElementRecordJSON);
			graphElementRecordOpt = Optional.of(graphElementRecord);
		} else
			graphElementRecordOpt = Optional.empty();
		
		VGraphDiff<V, I> diff = new VGraphDiff<>(from, graphName, vertexTypesOpt, vertexesOpt, 
				edgeTypesOpt, edgesOpt, subgraphsOpt, subgraphSyncOpt, destroyedRecordOpt, graphElementRecordOpt);
		
		return diff;
	}
	
	protected VSubgraphDiff<V, I> deserializeSubgraphDiff(JSONObject subgraphJSON) {
		String name = subgraphJSON.getString("name");
		V subgraphVersionTo = versionSerializer.stringToV(subgraphJSON.getString("subgraphVersionTo"));
		
		Optional<List<VLinkDiff<V, I>>> linkUpdatesOpt;
		if (subgraphJSON.has("linkUpdates")) {
			JSONArray linkUpdatesJSON = subgraphJSON.getJSONArray("linkUpdates");
			List<VLinkDiff<V, I>> linkUpdates = new ArrayList<VLinkDiff<V, I>>();
			
			for (int i = 0; i < linkUpdatesJSON.length(); i++) {
				JSONObject linkUpdateJSON = linkUpdatesJSON.getJSONObject(i);
				VLinkDiff<V, I> linkDiff = deserializeLinkDiff(linkUpdateJSON);
				linkUpdates.add(linkDiff);
			}
			
			linkUpdatesOpt = Optional.of(linkUpdates);
		} else
			linkUpdatesOpt = Optional.empty();
		
		Optional<VElementSyncRecord<V, I>> elementSync;
		if (subgraphJSON.has("elementSync")) {
			JSONObject elementSyncVersionJSON = subgraphJSON.getJSONObject("elementSync");
			
			VElementSyncRecord<V, I> elementSyncRecord = deserializeElementSyncRecord(elementSyncVersionJSON);
			elementSync = Optional.of(elementSyncRecord);
		} else
			elementSync = Optional.empty();
		
		Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord;
		if (subgraphJSON.has("subgraphElementRecord")) {
			JSONObject subgraphElementRecordJSON = subgraphJSON.getJSONObject("subgraphElementRecord");
			VSubgraphElementRecord<V, I> record = deserializeSubgraphElementRecordJSON(subgraphElementRecordJSON);
			
			subgraphElementRecord = Optional.of(record);
		} else
			subgraphElementRecord = Optional.empty();
		
		VSubgraphDiff<V, I> subgraph = new VSubgraphDiff<V, I>(name, subgraphVersionTo, linkUpdatesOpt,
				elementSync, subgraphElementRecord);
		return subgraph;
	}
	
	protected VSubgraphElementRecord<V, I> deserializeSubgraphElementRecordJSON(JSONObject elementSyncVersionJSON) {
		V subgraphElementUpdateVersion = 
				versionSerializer.stringToV(elementSyncVersionJSON.getString("subgraphElementUpdateVersion"));
		
		Optional<VSubgraph<V, I>> subgraphElement;
		if (elementSyncVersionJSON.has("subgraphElement")) {
			JSONObject subgraphElementJSON = elementSyncVersionJSON.getJSONObject("subgraphElement");
			VElement<V, I> tmp = deserializeElement(subgraphElementJSON);
			VSubgraph<V, I> vSubgraph = new VSubgraph<>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), tmp.getContent());
			
			subgraphElement = Optional.of(vSubgraph);
		} else
			subgraphElement = Optional.empty();
		
		
		VSubgraphElementRecord<V, I> record = new VSubgraphElementRecord<V, I>(subgraphElementUpdateVersion, subgraphElement);
		return record;
	}
	
	protected VElementSyncRecord<V, I> deserializeElementSyncRecord(JSONObject elementSyncVersionJSON) {
		V elementSyncVersion = versionSerializer.stringToV(elementSyncVersionJSON.getString("elementSyncVersion"));
		
		List<I> elementIds = new ArrayList<>();
		JSONArray arr = elementSyncVersionJSON.getJSONArray("elementIds");
		for (int i = 0; i < arr.length(); i++) {
			String elementId = arr.getString(i);
			elementIds.add(stringToI(elementId));
		}
		
		VElementSyncRecord<V, I> elementSyncRecord = new VElementSyncRecord<V, I>(elementSyncVersion, elementIds);
		return elementSyncRecord;
	}
	
	protected VLinkDiff<V, I> deserializeLinkDiff(JSONObject linkUpdateJSON) {
		I linkId = stringToI(linkUpdateJSON.getString("linkId"));
		
		Optional<VLinkUpdate<V, I>> linkUpdateOpt;
		if (linkUpdateJSON.has("linkUpdate")) {
			JSONObject linkUpdateJSONChild = linkUpdateJSON.getJSONObject("linkUpdate");
			boolean isTombstone = linkUpdateJSONChild.getBoolean("isTombstone");
			
			VElement<V, I> tmp = deserializeElement(linkUpdateJSONChild);
			
			VLinkUpdate<V, I> linkUpdate = new VLinkUpdate<V, I>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), 
					tmp.getContent(), isTombstone);
			linkUpdateOpt = Optional.of(linkUpdate);
		} else 
			linkUpdateOpt = Optional.empty();
		
		Optional<VLinkedElementUpdate<V, I>> linkedElementUpdateOpt;
		if (linkUpdateJSON.has("linkedElementUpdate")) {
			JSONObject linkedElementUpdateJSONChild = linkUpdateJSON.getJSONObject("linkedElementUpdate"); 
			I linkedElementId = stringToI(linkedElementUpdateJSONChild.getString("linkedElementId"));
			V linkedElementVersion = versionSerializer.stringToV(linkedElementUpdateJSONChild.getString("linkedElementVersion"));

			VLinkedElementUpdate<V, I> update = new VLinkedElementUpdate<>(linkedElementId, linkedElementVersion);
			linkedElementUpdateOpt = Optional.of(update);
		} else 
			linkedElementUpdateOpt = Optional.empty();
		
		VLinkDiff<V, I> diff = new VLinkDiff<>(linkId, linkUpdateOpt, linkedElementUpdateOpt);
		return diff;
	}
	
	protected VGraphElementRecord<V, I> deserializeGraphElementRecord(JSONObject graphElementRecordJSON) {
		V graphElementUpdateVersion = versionSerializer.stringToV(graphElementRecordJSON.getString("graphElementUpdateVersion"));
		Optional<VGraph<V, I>> graphElement;
		if (graphElementRecordJSON.has("graphElement")) {
			JSONObject graphElementJSON = graphElementRecordJSON.getJSONObject("graphElement");
			VElement<V, I> tmp = deserializeElement(graphElementJSON);
			VGraph<V, I> vGraph = new VGraph<>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), tmp.getContent());
			
			graphElement = Optional.of(vGraph);
		} else
			graphElement = Optional.empty();
		
		VGraphElementRecord<V, I> graphElementRecord = new VGraphElementRecord<>(graphElementUpdateVersion, graphElement);
		return graphElementRecord;
	}
	
	protected VGraphDestroyedRecord<V> deserializeDestroyedRecord(JSONObject destroyedRecordJSON) {
		V destroyRecoverVersion = versionSerializer.stringToV(destroyedRecordJSON.getString("destroyRecoverVersion"));
		boolean isDestroyed = destroyedRecordJSON.getBoolean("isDestroyed");
		
		VGraphDestroyedRecord<V> destroyedRecord = new VGraphDestroyedRecord<V>(destroyRecoverVersion, isDestroyed);
		return destroyedRecord;
	}
	
	protected VSubgraphSyncRecord<V> deserializeSubgraphSync(JSONObject subgraphSyncJSON) {
		V subgraphSyncVersion = versionSerializer.stringToV(subgraphSyncJSON.getString("subgraphSyncVersion"));
		JSONArray subgraphNamesJSON = subgraphSyncJSON.getJSONArray("subgraphNames");
		
		List<String> subgraphNames = new ArrayList<String>();
		for (int i = 0; i < subgraphNamesJSON.length(); i++) {
			String subgraphName = subgraphNamesJSON.getString(i);
			subgraphNames.add(subgraphName);
		}
		
		VSubgraphSyncRecord<V> subgraphSync = new VSubgraphSyncRecord<V>(subgraphSyncVersion, subgraphNames);
		return subgraphSync;
	}

	protected VVertex<V, I> deserializeVertex(JSONObject vertexJSON) {
		VElement<V, I> tmp = deserializeElement(vertexJSON);
		I vertexTypeId = stringToI(vertexJSON.getString("vertexTypeId"));
		
		VVertex<V, I> vertex = new VVertex<V, I>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), 
				tmp.getContent(), vertexTypeId);
		
		return vertex;
	}
	
	protected VEdge<V, I> deserializeEdge(JSONObject edgeJSON) {
		VElement<V, I> tmp = deserializeElement(edgeJSON);
		I edgeTypeId = stringToI(edgeJSON.getString("edgeTypeId"));
		I vertexFromId = stringToI(edgeJSON.getString("vertexFromId"));
		I vertexToId = stringToI(edgeJSON.getString("vertexToId"));
		boolean isDirected = edgeJSON.getBoolean("isDirected");
		
		VEdge<V, I> edge = new VEdge<V, I>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), 
				tmp.getContent(), edgeTypeId, vertexFromId, vertexToId, isDirected);
		
		return edge;
	}
	
	protected VVertexType<V, I> deserializeVertexType(JSONObject vertexTypeJSON) {
		VElement<V, I> tmp = deserializeElement(vertexTypeJSON);
		String vertexTypeName = vertexTypeJSON.getString("vertexTypeName");
		
		VVertexType<V, I> vertexType = new VVertexType<V, I>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), 
				tmp.getContent(), vertexTypeName);
		
		return vertexType;
	}
	
	protected VEdgeType<V, I> deserializeEdgeType(JSONObject edgeTypeJSON) {
		VElement<V, I> tmp = deserializeElement(edgeTypeJSON);
		String edgeTypeName = edgeTypeJSON.getString("edgeTypeName");
		
		VEdgeType<V, I> edgeType = new VEdgeType<V, I>(tmp.getElementId(), tmp.getVersion(), tmp.getKey(), 
				tmp.getContent(), edgeTypeName);
		
		return edgeType;
	}
	
	protected VElement<V, I> deserializeElement(JSONObject elementJSON) {
		I elementId = stringToI(elementJSON.getString("elementId"));
		V version = versionSerializer.stringToV(elementJSON.getString("version"));
		
		Optional<String> key;
		if (elementJSON.has("key"))
			key = Optional.of(elementJSON.getString("key"));
		else
			key = Optional.empty();
		
		String content = elementJSON.getString("content");
		
		VElement<V, I> element = new VElement<V, I>(elementId, version, key, content);
		return element;
	}
	
	//-----------------------------
	
	public JSONObject serializeGraphDiff(VGraphDiff<V, I> diff) {
		JSONObject graphObject = new JSONObject();
		
		graphObject.put("from", versionSerializer.serializeGraphVersion(diff.getFrom()));
		graphObject.put("graphName", diff.getGraphName());
		
		if (diff.getVertexTypes().isPresent())
			if (!diff.getVertexTypes().get().isEmpty())
				graphObject.put("vertexTypes", serializeVertexTypes(diff.getVertexTypes().get()));
				
		if (diff.getVertexes().isPresent())
			if (!diff.getVertexes().get().isEmpty())
				graphObject.put("vertexes", serializeVertexes(diff.getVertexes().get()));
		
		if (diff.getEdgeTypes().isPresent())
			if (!diff.getEdgeTypes().get().isEmpty())
				graphObject.put("edgeTypes", serializeEdgeTypes(diff.getEdgeTypes().get()));
		
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
	
	protected JSONObject serializeSubgraphSync(VSubgraphSyncRecord<V> subgraphSync) {
		JSONObject subgraphSyncJSON = new JSONObject(); 
		
		subgraphSyncJSON.put("subgraphSyncVersion", versionSerializer.vToString(subgraphSync.getSubgraphSyncVersion()));
		
		JSONArray subgraphNames = new JSONArray();
		
		if (subgraphSync.getSubgraphNames() != null)
			for (String name : subgraphSync.getSubgraphNames())
				subgraphNames.put(name);
		
		subgraphSyncJSON.put("subgraphNames", subgraphNames);
		
		return subgraphSyncJSON;
	}
	
	protected JSONObject serializeDestroyedRecord(VGraphDestroyedRecord<V> destroyedRecord) {
		JSONObject destroyedRecordJSON = new JSONObject();
		
		destroyedRecordJSON.put("destroyRecoverVersion", 
				versionSerializer.vToString(destroyedRecord.getDestroyRecoverVersion()));
		destroyedRecordJSON.put("isDestroyed", destroyedRecord.isDestroyed());
		
		return destroyedRecordJSON;
	}
	
	protected JSONObject serializeGraphElementRecord(VGraphElementRecord<V, I> graphElementRecord) {
		JSONObject graphElementRecordJSON = new JSONObject();

		graphElementRecordJSON.put("graphElementUpdateVersion", 
				versionSerializer.vToString(graphElementRecord.getGraphElementUpdateVersion()));
		
		if (graphElementRecord.getGraphElement().isPresent())
			graphElementRecordJSON.put("graphElement", serializeElement(graphElementRecord.getGraphElement().get()));
		
		return graphElementRecordJSON;
	}
	
	protected JSONArray serializeSubgraphs(List<VSubgraphDiff<V, I>> subgraphs) {
		JSONArray subgraphsJSON = new JSONArray();
		
		for (VSubgraphDiff<V, I> subgraph : subgraphs) {
			JSONObject subgraphJSON = serializeSubgraph(subgraph);
			subgraphsJSON.put(subgraphJSON);
		}
		
		return subgraphsJSON;
	}
	
	protected JSONObject serializeSubgraph(VSubgraphDiff<V, I> subgraph) {
		JSONObject subgraphJSON = new JSONObject();
		
		subgraphJSON.put("name", subgraph.getName());
		subgraphJSON.put("subgraphVersionTo", versionSerializer.vToString(subgraph.getSubgraphVersionTo()));
		
		if (subgraph.getLinkUpdates().isPresent())
		if (!subgraph.getLinkUpdates().get().isEmpty()) {
			JSONArray updates = new JSONArray();
			for (VLinkDiff<V, I> link : subgraph.getLinkUpdates().get())
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
	
	protected JSONObject serializeSubgraphElementRecord(VSubgraphElementRecord<V, I> subgraphElementRecord) {
		JSONObject subgraphElementJSON = new JSONObject();
		
		subgraphElementJSON.put("subgraphElementUpdateVersion", 
				versionSerializer.vToString(subgraphElementRecord.getSubgraphElementUpdateVersion()));
		
		if (subgraphElementRecord.getSubgraphElement().isPresent())
			subgraphElementJSON.put("subgraphElement", 
					serializeElement(subgraphElementRecord.getSubgraphElement().get()));
		
		return subgraphElementJSON;
	}
	
	protected JSONObject serializeElementSyncRecord(VElementSyncRecord<V, I> elementSync) {
		JSONObject elementSyncJSON = new JSONObject();
		
		elementSyncJSON.put("elementSyncVersion", versionSerializer.vToString(elementSync.getElementSyncVersion()));
		
		JSONArray elementIdsArray = new JSONArray();
		
		if (elementSync.getElementIds() != null)
			for (I id : elementSync.getElementIds())
				elementIdsArray.put(iToString(id));
		
		elementSyncJSON.put("elementIds", elementIdsArray);
		
		return elementSyncJSON;
	}
	
	protected JSONObject serializeLink(VLinkDiff<V, I> link) {
		JSONObject linkJSON = new JSONObject();
		
		linkJSON.put("linkId", iToString(link.getLinkId()));
		
		if (link.getLinkUpdate().isPresent())
			linkJSON.put("linkUpdate", serializeLinkUpdate(link.getLinkUpdate().get()));
		
		if (link.getLinkedElementUpdate().isPresent())
			linkJSON.put("linkedElementUpdate", serializeLinkElementUpdate(link.getLinkedElementUpdate().get()));
		
		return linkJSON;
	}
	
	protected JSONObject serializeLinkUpdate(VLinkUpdate<V, I> linkUpdate) {
		JSONObject linkUpdateJSON = serializeElement(linkUpdate);
		linkUpdateJSON.put("isTombstone", linkUpdate.isTombstone());
		
		return linkUpdateJSON;
	}
	
	protected JSONObject serializeLinkElementUpdate(VLinkedElementUpdate<V, I> linkElementUpdate) {
		JSONObject linkElementUpdateJSON = new JSONObject();
		
		linkElementUpdateJSON.put("linkedElementId", iToString(linkElementUpdate.getLinkedElementId()));
		linkElementUpdateJSON.put("linkedElementVersion", 
				versionSerializer.vToString(linkElementUpdate.getLinkedElementVersion()));
		
		return linkElementUpdateJSON;
	}
	
	protected JSONArray serializeEdges(Map<I, VEdge<V, I>> edges) {
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
	
	protected JSONArray serializeVertexes(Map<I, VVertex<V, I>> vertexes) {
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
	
	protected JSONArray serializeVertexTypes(Map<I, VVertexType<V, I>> vertexTypes) {
		JSONArray vertexTypesJSON = null;
		
		if ((vertexTypes != null) && (!vertexTypes.isEmpty())) {
			vertexTypesJSON = new JSONArray();
			
			for (VVertexType<V, I> vertexType : vertexTypes.values()) {
				JSONObject element = serializeElement(vertexType);
				element.put("vertexTypeName", vertexType.getVertexTypeName());
				
				vertexTypesJSON.put(element);
			}
		}
		
		return vertexTypesJSON;
	}
	
	protected JSONArray serializeEdgeTypes(Map<I, VEdgeType<V, I>> edgeTypes) {
		JSONArray edgeTypesJSON = null;
		
		if ((edgeTypes != null) && (!edgeTypes.isEmpty())) {
			edgeTypesJSON = new JSONArray();
			
			for (VEdgeType<V, I> edgeType : edgeTypes.values()) {
				JSONObject element = serializeElement(edgeType);
				element.put("edgeTypeName", edgeType.getEdgeTypeName());
				
				edgeTypesJSON.put(element);
			}
		}
		
		return edgeTypesJSON;
	}
	
	protected JSONObject serializeElement(VElement<V, I> element) {
		JSONObject elementJson = new JSONObject();
		
		elementJson.put("elementId", iToString(element.getElementId()));
		elementJson.put("version", versionSerializer.vToString(element.getVersion()));
		
		if (element.getKey().isPresent())
			elementJson.put("key", element.getKey().get());
		
		elementJson.put("content", element.getContent());
		
		return elementJson;
	}

	protected abstract String iToString(I id);
	protected abstract I stringToI(String id);
}
