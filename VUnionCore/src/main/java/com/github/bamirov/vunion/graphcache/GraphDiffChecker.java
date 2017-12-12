package com.github.bamirov.vunion.graphcache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.exceptions.GraphVersionMismatchException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VEdgeType;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graph.VVertexType;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VGraphElementRecord;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VLinkUpdate;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphElementRecord;
import com.github.bamirov.vunion.graphstream.VSubgraphSyncRecord;
import com.github.bamirov.vunion.version.VGraphVersion;

public class GraphDiffChecker<V extends Comparable<V>, I> {
	protected VComparator<V> vComparator = new VComparator<>();
	
	protected V getGraphMaxVersion(VGraphCache<V, I> cache) {
		V graphVersion = cache.getGraphUpdateVersion();
		
		for (Entry<String, VSubgraphCache<V, I>> ent : cache.getSubgraphs().entrySet())
			if (graphVersion == null)
				graphVersion = ent.getValue().getSubgraphVersion();
			else
				graphVersion = (vComparator.compare(graphVersion, ent.getValue().getSubgraphVersion()) >= 0) 
						? graphVersion 
						: ent.getValue().getSubgraphVersion();
		
		return graphVersion;
	}
	
	protected void addToBuilder(StringBuilder builder, String str) {
		if (builder.length() != 0)
			builder.append(", ");
		builder.append(str);
	}
	
	protected boolean elementExists(I elementId, VGraphDiff<V, I> diff, Map<I, SharedElementRec<V, I>> cacheSharedElements) {
		return ((diff.getElement(elementId) != null) || (cacheSharedElements.containsKey(elementId)));
	}
	
	protected boolean isElementDeleted(I elementId, VSubgraphDiff<V, I> subgraphDiff, VSubgraphCache<V, I> subgraphCache) throws GraphMismatchException {
		if (subgraphDiff.getElementSync().isPresent())
			return (!subgraphDiff.getElementSync().get().getElementIds().contains(elementId));
		
		VLinkDiff<V, I> linkDiff = null;
		if (subgraphDiff.getLinkUpdatesByElementId().isPresent())
			linkDiff = subgraphDiff.getLinkUpdatesByElementId().get().get(elementId);
		
		if (linkDiff != null) 
			return false;
		
		return (!subgraphCache.getSubgraphLinksByElementId().containsKey(elementId));
	}
	
	protected boolean isElementTombstonedOrDeleted(I elementId, VSubgraphDiff<V, I> subgraphDiff, VSubgraphCache<V, I> subgraphCache) throws GraphMismatchException {
		if (subgraphDiff.getElementSync().isPresent())
			if (!subgraphDiff.getElementSync().get().getElementIds().contains(elementId))
				return true;
		
		VLinkDiff<V, I> linkDiff = null;
		if (subgraphDiff.getLinkUpdatesByElementId().isPresent())
			linkDiff = subgraphDiff.getLinkUpdatesByElementId().get().get(elementId);
		
		if ((linkDiff != null) && (linkDiff.getLinkUpdate().isPresent())) {
			VLinkUpdate<V, I> linkUpdate = linkDiff.getLinkUpdate().get();
			return linkUpdate.isTombstone();
		}
		
		VLink<V,I> cacheLink = subgraphCache.getSubgraphLinksByElementId().get(elementId);
		
		if (cacheLink != null)
			return cacheLink.isTombstone();
		else
			throw new GraphMismatchException(
					String.format("LinkUpdates error: element to check not found in diff/cache. Subgraph [%s] ElementId [%s]",
							subgraphDiff.getName(),
							elementId.toString())
				);
	}
	
	protected void checkElement(Set<I> referencedElementsSet, VElement<V, I> elm) throws GraphMismatchException {
		if (!referencedElementsSet.contains(elm.getElementId())) {
			throw new GraphMismatchException(
					String.format("LinkUpdates error: Updated element doesn't exist in diff ElementId [%s]. ",
							elm.getElementId().toString()	
					)
				);
		}
	}
	
	protected void checkGraphDiffElements(Set<I> referencedElementsSet, VGraphDiff<V, I> diff) throws GraphMismatchException {
		int elementCount = 0;
		
		if (diff.getVertexTypes().isPresent()) {
			elementCount += diff.getVertexTypes().get().size();
			
			for (VVertexType<V, I> elm : diff.getVertexTypes().get().values())
				checkElement(referencedElementsSet, elm);
		}
		
		if (diff.getVertexes().isPresent()) {
			elementCount += diff.getVertexes().get().size();
			
			for (VVertex<V, I> elm : diff.getVertexes().get().values())
				checkElement(referencedElementsSet, elm);
		}
		
		if (diff.getEdgeTypes().isPresent()) {
			elementCount += diff.getEdgeTypes().get().size();
			
			for (VEdgeType<V, I> elm : diff.getEdgeTypes().get().values())
				checkElement(referencedElementsSet, elm);
		}
		
		if (diff.getEdges().isPresent()) {
			elementCount += diff.getEdges().get().size();
			
			for (VEdge<V, I> elm : diff.getEdges().get().values())
				checkElement(referencedElementsSet, elm);
		}
		
		if (elementCount != referencedElementsSet.size()) {
			throw new GraphMismatchException(
					String.format("LinkUpdates error: Unique updated elements count doesn't match diff elements count. Updated elements count [%s], diff elements count [%s].",
							elementCount,
							referencedElementsSet.size()
					)
				);
		}
	}
	
	protected void findElementInSubgraphDiffOrCache(I elementId, VSubgraphDiff<V, I> subgraphDiff, VGraphDiff<V, I> graphDiff,
			VSubgraphCache<V, I> subgraphCache, String elementLogName,
			@SuppressWarnings("rawtypes") Class elementClass, Map<I, SharedElementRec<V, I>> cacheSharedElements, boolean ignoreTombstoneStatus) throws GraphMismatchException {
		//Get Vertex type link from subgraph diff
		VLinkDiff<V, I> diffElementLink = subgraphDiff.getLinkUpdatesByElementId().get().get(elementId);
		
		boolean checkCacheElement = false;
		boolean checkCacheElementTombstoned = true;
		if (diffElementLink == null) {
			//If subgraph diff doesn't have element link, element link should exist in subgraph cache
			checkCacheElement = true;
			checkCacheElementTombstoned = true;
		} else {
			//1. If ElementLink exists in Diff
			
			//1.1. Either LinkUpdate or LinkedElementVersionUpdate should exist (this check is REDUNDANT, just to fail fast)
			if (!diffElementLink.getLinkUpdate().isPresent() && !diffElementLink.getLinkedElementVersionUpdate().isPresent()) {
				throw new GraphMismatchException(
						String.format("LinkUpdates error: %s : either LinkUpdate or LinkedElementUpdate should be present in diff. Subgraph [%s] ElementId [%s] LinkId [%s]",
								elementLogName,
								subgraphDiff.getName(),
								elementId.toString(),
								diffElementLink.getLinkId().toString())
					);
			}
			
			//1.2. If LinkUpdate is present and if ignoreTombstoneStatus parameter == false - check that this element is not tombstoned
			if (!ignoreTombstoneStatus)
			if (diffElementLink.getLinkUpdate().isPresent()) {
				if (diffElementLink.getLinkUpdate().get().isTombstone()) {
					throw new GraphMismatchException(
							String.format("LinkUpdates error: %s going to be tombstoned by subgraph LinkUpdate. Subgraph [%s] ElementId [%s] LinkId [%s]",
									elementLogName,
									subgraphDiff.getName(),
									elementId.toString(),
									diffElementLink.getLinkId().toString())
						);
				} else
					checkCacheElementTombstoned = false;
			}
			
			//1.3. If LinkedElementVersionUpdate is present - check element in diff
			//Graph diff should have updated Element, and element should be instance of provided type
			if (diffElementLink.getLinkedElementVersionUpdate().isPresent()) {
				//If subgraph diff has Element link, Element should also exist in graph diff
				VElement<V, I> elementFromDiff = graphDiff.getElement(elementId);
				
				//Element should also exist in graph diff
				if (elementFromDiff == null) {
					throw new GraphMismatchException(
							String.format("LinkUpdates error: %s is linked to a subgraph in diff, but Element doesn't exist in graph diff. " +
								"Subgraph [%s] ElementId [%s] diff LinkId [%s]",
									elementLogName,	
									subgraphDiff.getName(),
									elementId.toString(),
									diffElementLink.getLinkId().toString())
						);
				}
				
				//Element should be of class "elementClass"
				if (!elementClass.isInstance(elementFromDiff)) {
					throw new GraphMismatchException(
							String.format("LinkUpdates error: %s is linked to subgraph in diff, but the Id in diff belongs to an object of wrong type. " +
								"Subgraph [%s] ElementId [%s] diff LinkId [%s] expected Type [%s] actual Type [%s]",
									elementLogName,
									subgraphDiff.getName(),
									elementId.toString(),
									diffElementLink.getLinkId().toString(),
									elementClass.toString(),
									elementFromDiff.getClass().toString())
						);
				}
				checkCacheElement = false;
			} else
				checkCacheElement = true;
		}
		
		//2. Checking cache element: (tombstone checks will be performed if ignoreTombstoneStatus parameter = false)
		//a. If diff element doesn't exist, Element should exist in cache and not be marked as tombstoned.
		//b. If diff element exists and has LinkUpdate, but no LinkedElementVersionUpdate - LinkUpdate overrides
		//	cache link, but we still need to check that both link and element exist in cache, and element is an instance of provided type. 
		//c. If diff element exists and has LinkedElementVersionUpdate, but no LinkUpdate - LinkedElementVersionUpdate overrides
		//	cache element, but we still need to check that link exists in cache, and is not tombstoned
		//d. If diff element exists and has both LinkUpdate and LinkedElementVersionUpdate, cache data is fully overridden.
		//	That also might mean, that element is new to cache. In this case no checks are required.
		if (checkCacheElement || checkCacheElementTombstoned) {
			//Checking existing cache elements, since a new element would result in 
			//checkCacheElement = false; checkCacheElementTombstoned = false

			//Element link should exist in subgraph cache 
			VLink<V, I> cacheElementLink = subgraphCache.getSubgraphLinksByElementId().get(elementId);
			if (cacheElementLink == null) {
				throw new GraphMismatchException(
					String.format("LinkUpdates error: %s is not linked to a subgraph (in cache). " + 
						"Subgraph [%s] ElementId [%s]",
							elementLogName,
							subgraphDiff.getName(),
							elementId.toString())
				);
			}
			
			//If LinkedElementVersionUpdate doesn't exist
			if (checkCacheElement) {
				//Element should also exist in cache
				SharedElementRec<V, I> cacheElementRecord = cacheSharedElements.get(elementId);
				if (cacheElementRecord == null) {
					throw new GraphMismatchException(
						String.format("LinkUpdates error: %s is linked to a subgraph in cache (no link in diff), but Element doesn't exist in cache. " + 
							"Subgraph [%s] ElementId [%s] cache LinkId [%s]",
								elementLogName,	
								subgraphDiff.getName(),
								elementId.toString(),
								cacheElementLink.getElementId()
						)
					);
				}
				
				//Element should be of class "elementClass"
				if (!elementClass.isInstance(cacheElementRecord.element)) {
					throw new GraphMismatchException(
						String.format("LinkUpdates error: %s is linked to subgraph in cache, but the Id in diff belongs to an object of wrong type. " + 
							"Subgraph [%s] ElementId [%s] cache LinkId [%s] expected Type [%s] actual Type [%s]",
								elementLogName,	
								subgraphDiff.getName(),
								elementId.toString(),
								cacheElementLink.getElementId(),
								elementClass.toString(),
								cacheElementRecord.element.getClass().toString())
					);
				}
			}
			
			//If LinkUpdate doesn't exist
			if (!ignoreTombstoneStatus)
			if (checkCacheElementTombstoned) {
				//If applicable, check that this element is not tombstoned
				if (cacheElementLink.isTombstone()) {
					throw new GraphMismatchException(
							String.format("LinkUpdates error: %s is linked to a subgraph in cache (no link in diff), and is marked as tombstone. Subgraph [%s] ElementId [%s] LinkId [%s]",
									elementLogName,
									subgraphDiff.getName(),
									elementId.toString(),
									cacheElementLink.getElementId())
						);
				}
			}
		}
		
		//3. Check that the element is not removed; If SubraphDiff's ElementSync isPresent, elementId should be present in its list of active elements 
		if (subgraphDiff.getElementSync().isPresent())
			if (!subgraphDiff.getElementSync().get().getElementIds().contains(elementId)) {
				throw new GraphMismatchException(
						String.format("LinkUpdates error: %s going to be deleted by subgraph ElementSync. Subgraph [%s] ElementId [%s]",
								elementLogName,
								subgraphDiff.getName(),
								elementId.toString())
					);
			}
	}
	
	protected void sanityCheckGraphDiff(VGraphCache<V, I> cachePrm, VGraphDiff<V, I> graphDiff, boolean relaxedEdgeCheck) throws GraphVersionMismatchException, GraphMismatchException {
		String graphName = cachePrm.getGraphName();
		
		VGraphVersion<V> cacheVersion = cachePrm.getGraphVersion();
		
		V maxGraphVersion = getGraphMaxVersion(cachePrm);
		V cacheGraphVersion = cachePrm.getGraphUpdateVersion();
		Optional<V> cacheSubgraphSyncVersionOpt = cachePrm.getSubgraphSyncVersion();
		
		Optional<VGraphElementRecord<V, I>> cacheGraphElementOpt = cachePrm.getGraphElementRecord();
		Map<I, SharedElementRec<V, I>> cacheSharedElements = cachePrm.getSharedElements();
		Map<String, VSubgraphCache<V, I>> cacheSubgraphs = cachePrm.getSubgraphs();
		
		//2. Update destroyed status
		if (graphDiff.getDestroyedRecord().isPresent()) {
			cacheGraphElementOpt = Optional.empty();
			cacheSharedElements = new HashMap<>();
			cacheSubgraphs = new HashMap<>();
			cacheSubgraphSyncVersionOpt = Optional.empty();
			
			if (!graphDiff.getDestroyedRecord().get().isDestroyed())
				cacheGraphVersion = null;
		}
		
		
		//1. Graph name must match
		if (!graphName.equals(graphDiff.getGraphName()))
			throw new GraphMismatchException(
					String.format("Graph name doesn't match: diff [%s] cache [%s]", 
							graphDiff.getGraphName(), graphName));
		
		//2. Cache version must equal to Diff version from
		if (!graphDiff.getFrom().equals(cacheVersion))
			throw new GraphVersionMismatchException(cacheVersion, 
				String.format("Cache/diff version mismatch: [diff from: [%s]; cache: [%s]] ", 
						graphDiff.getFrom().toString(), cacheVersion.toString())
			);
		
		//3. If graph is destroyed, no data should be present in the Diff other than Destroyed record
		//Destroyed/Recover version should be > any cache version
		if (graphDiff.getDestroyedRecord().isPresent()) {
			if (graphDiff.getDestroyedRecord().get().isDestroyed()) {
				StringBuilder builder = new StringBuilder();
				
				if (graphDiff.getVertexTypes().isPresent()) addToBuilder(builder, "VertexTypes");
				if (graphDiff.getVertexes().isPresent()) addToBuilder(builder, "Vertexes");
				if (graphDiff.getEdgeTypes().isPresent()) addToBuilder(builder, "EdgeTypes");
				if (graphDiff.getEdges().isPresent()) addToBuilder(builder, "Edges");
				if (graphDiff.getSubgraphs().isPresent()) addToBuilder(builder, "Subgraphs");
				if (graphDiff.getSubgraphSync().isPresent()) addToBuilder(builder, "SubgraphSync");
				if (graphDiff.getGraphElementRecord().isPresent()) addToBuilder(builder, "GraphElementRecord");
				
				if (builder.length() != 0) {
					throw new GraphMismatchException(
						String.format("DestroyedRecord inconsistent isDestroyed set to true, but the following data exists: [%s]",
								builder.toString())
					);
				}
			}
			
			//NB: two [isDestroyed:true] diffs can be received in a row, in case Recovery event in the middle was missed
			//NB: two [isDestroyed:false] diffs can be received in a row, in case Destroyed event in the middle was missed
			
			if ((maxGraphVersion != null) && 
					(vComparator.compare(graphDiff.getDestroyedRecord().get().getDestroyRecoverVersion(), maxGraphVersion) <= 0)) {
				throw new GraphMismatchException(
						String.format("DestroyedRecord inconsistent: [DestroyRecoverVersion: [%s] <= cache GraphUpdateVersion: [%s]]",
								graphDiff.getDestroyedRecord().get().getDestroyRecoverVersion().toString(), 
								maxGraphVersion.toString())
					);
			}
		}
		
		//4. Diff: Graph element version should be equal to VGraphElementRecord.getGraphElementUpdateVersion
		//	If cache graph element exists, its version should be < Diff Graph element version
		//	Diff Graph element version should be > cache Graph version
		if (graphDiff.getGraphElementRecord().isPresent()) {
			VGraphElementRecord<V, I> ger = graphDiff.getGraphElementRecord().get();
			
			if (ger.getGraphElement().isPresent()) {
				if (!ger.getGraphElement().get().getVersion().equals(ger.getGraphElementUpdateVersion())) {
					throw new GraphMismatchException(
						String.format("GraphElementRecord inconsistent: [GraphElementUpdateVersion: [%s] != GraphElement.Version: [%s]]",
								ger.getGraphElementUpdateVersion().toString(),
								ger.getGraphElement().get().getVersion().toString())
					);
				}
			}
			
			if (cacheGraphElementOpt.isPresent()) {
				if (vComparator.compare(ger.getGraphElementUpdateVersion(), cacheGraphElementOpt.get().getGraphElementUpdateVersion()) <= 0) {
					throw new GraphMismatchException(
						String.format("GraphElementRecord inconsistent: [GraphElementUpdateVersion: [%s] <= cache GraphElement.Version: [%s]]",
								ger.getGraphElementUpdateVersion().toString(),
								cacheGraphElementOpt.get().getGraphElementUpdateVersion().toString())
					);
				}
			}
			
			if ((cacheGraphVersion != null) && 
					(vComparator.compare(ger.getGraphElementUpdateVersion(), cacheGraphVersion) <= 0)) {
				throw new GraphMismatchException(
					String.format("GraphElementRecord inconsistent: [GraphElementUpdateVersion: [%s] <= cache MaxGraphVersion: [%s]]",
							ger.getGraphElement().get().getVersion().toString(),
							cacheGraphVersion.toString())
				);
			}
		}
		
		//5. Diff SubgraphSyncRecord version should be > Cache subgraphSyncVersion
		//	All active subgraph names should exist either in Cache or in Diff
		//	All subgraphs that are removed from cache should not exist in diff
		if (graphDiff.getSubgraphSync().isPresent()) {
			VSubgraphSyncRecord<V> subgraphSyncRecord = graphDiff.getSubgraphSync().get();
			
			if ((cacheSubgraphSyncVersionOpt.isPresent()) && 
					(vComparator.compare(subgraphSyncRecord.getSubgraphSyncVersion(), cacheSubgraphSyncVersionOpt.get()) <= 0)) {
				throw new GraphMismatchException(
					String.format("VSubgraphSyncRecord inconsistent: [GraphElementUpdateVersion: [%s] <= cache SubgraphSyncVersion: [%s]]",
							subgraphSyncRecord.getSubgraphSyncVersion().toString(), 
							cacheSubgraphSyncVersionOpt)
				);
			}
			
			Set<String> activeSubgraphNames = new HashSet<>();
			activeSubgraphNames.addAll(subgraphSyncRecord.getSubgraphNames());
			
			for (String name : activeSubgraphNames) {
				if ((!cacheSubgraphs.containsKey(name)) && (
						graphDiff.getSubgraphs().isPresent()
							&&
						!graphDiff.getSubgraphs().get().containsKey(name)
					))
					throw new GraphMismatchException(
							String.format("VSubgraphSyncRecord inconsistent: [Subgraph name doesn't exist in cache or diff : [%s]]", name)
					);
			}
			
			if (graphDiff.getSubgraphs().isPresent()) {
				for (VSubgraphDiff<V, I> sg : graphDiff.getSubgraphs().get().values()) {
					if (!activeSubgraphNames.contains(sg.getName())) {
						throw new GraphMismatchException(
							String.format("VSubgraphSyncRecord inconsistent: [Subgraph that should be removed exist in diff : [%s]]", sg.getName())
						);
					}
				}
			}
		}
		
		//6. Subgraphs check
		if (graphDiff.getSubgraphs().isPresent()) {
			Set<I> referencedElementsSet = new HashSet<>();
			
			for (VSubgraphDiff<V, I> subgraphDiff : graphDiff.getSubgraphs().get().values()) {
				VSubgraphCache<V, I> subgraphCache = cacheSubgraphs.get(subgraphDiff.getName());
				if (subgraphCache == null) {
					subgraphCache = new VSubgraphCache<V, I>(subgraphDiff.getName());
					cacheSubgraphs.put(subgraphDiff.getName(), subgraphCache);
				}
				
				checkSubgraph(subgraphDiff, graphDiff, subgraphCache, cacheSharedElements, referencedElementsSet, relaxedEdgeCheck);
			}
			
			//Check that all diff elements were referenced at least once
			checkGraphDiffElements(referencedElementsSet, graphDiff);
		}
	}
	
	public void checkSubgraph(VSubgraphDiff<V, I> subgraphDiff, VGraphDiff<V, I> graphDiff, VSubgraphCache<V, I> subgraphCache, 
			Map<I, SharedElementRec<V, I>> cacheSharedElements, Set<I> referencedElementsSet, boolean relaxedEdgeCheck) throws GraphMismatchException {
		//0. Fill Diff Vertexes by Type, Diff Edges by Type
		Map<I, Set<I>> diffVertexesByType = new HashMap<>();
		Map<I, Set<I>> diffEdgesByType = new HashMap<>();
		Map<I, Set<I>> diffEdgesByVertex = new HashMap<>();
		
		if (subgraphDiff.getLinkUpdatesByElementId().isPresent()) {
			for (VLinkDiff<V, I> linkDiff : subgraphDiff.getLinkUpdatesByElementId().get().values()) {
				if (linkDiff.getLinkedElementVersionUpdate().isPresent()) {
					if (graphDiff.getVertexes().isPresent()) {
						VVertex<V, I> vertex = graphDiff.getVertexes().get().get(linkDiff.getLinkedElementId());
						if (vertex != null) {
							Set<I> typeVertexes = diffVertexesByType.get(vertex.getVertexTypeId());
							if (typeVertexes == null) {
								typeVertexes = new HashSet<>();
								diffVertexesByType.put(vertex.getVertexTypeId(), typeVertexes);
							}
							
							typeVertexes.add(vertex.getElementId());
						}
					}
					
					if (graphDiff.getEdges().isPresent()) {
						VEdge<V, I> edge = graphDiff.getEdges().get().get(linkDiff.getLinkedElementId());
						if (edge != null) {
							Set<I> typeEdges = diffEdgesByType.get(edge.getEdgeTypeId());
							if (typeEdges == null) {
								typeEdges = new HashSet<>();
								diffEdgesByType.put(edge.getEdgeTypeId(), typeEdges);
							}
							typeEdges.add(edge.getElementId());
							
							Set<I> fromVertexEdges = diffEdgesByVertex.get(edge.getVertexFromId());
							if (fromVertexEdges == null) {
								fromVertexEdges = new HashSet<>();
								diffEdgesByVertex.put(edge.getVertexFromId(), fromVertexEdges);
							}
							fromVertexEdges.add(edge.getElementId());
							
							Set<I> toVertexEdges = diffEdgesByVertex.get(edge.getVertexToId());
							if (toVertexEdges == null) {
								toVertexEdges = new HashSet<>();
								diffEdgesByVertex.put(edge.getVertexToId(), toVertexEdges);
							}
							toVertexEdges.add(edge.getElementId());
						}
					}
				}
			}
		}
		
		//1. Diff Subgraph version To should be > corresponding Cache Subgraph version 
		if (subgraphCache != null) {
			if (vComparator.compare(subgraphDiff.getSubgraphVersionTo(), subgraphCache.getSubgraphVersion()) <= 0) {
				throw new GraphMismatchException(
					String.format("Subgraph updates inconsistent: [Subgraph [%s]: SubgraphVersion: [%s] <= cache SubgraphVersion: [%s]]",
							subgraphDiff.getName(),
							subgraphDiff.getSubgraphVersionTo().toString(), 
							subgraphCache.getSubgraphVersion().toString())
				);
			}
		}
		
		//2. Diff: Subgraph element version should be equal to VSubgraphElementRecord.getSubgraphElementRecord
		//	If Cache Subgraph element exists, its version should be < Diff Subgraph element version
		//	Diff Subgraph element version should be > cache Subgraph version
		if (subgraphDiff.getSubgraphElementRecord().isPresent()) {
			VSubgraphElementRecord<V, I> subgraphElementRecord = subgraphDiff.getSubgraphElementRecord().get();
			
			if (subgraphElementRecord.getSubgraphElement().isPresent()) {
				if (!subgraphElementRecord.getSubgraphElement().get().getVersion().equals(subgraphElementRecord.getSubgraphElementUpdateVersion())) {
					throw new GraphMismatchException(
						String.format("SubgraphElementRecord inconsistent: [Subgraph [%s]: SubgraphElementUpdateVersion: [%s] != SubgraphElement.Version: [%s]]",
								subgraphDiff.getName(),
								subgraphElementRecord.getSubgraphElementUpdateVersion().toString(), 
								subgraphElementRecord.getSubgraphElement().get().getVersion().toString())
					);
				}
			}
			
			if (subgraphCache != null) {
				if (subgraphCache.getSubgraphElementRecord().isPresent()) {
					if (vComparator.compare(subgraphElementRecord.getSubgraphElementUpdateVersion(),
							subgraphCache.getSubgraphElementRecord().get().getSubgraphElementUpdateVersion()) <= 0) {
						throw new GraphMismatchException(
							String.format("SubgraphElementRecord inconsistent: [Subgraph [%s]: SubgraphElementUpdateVersion: [%s] <= cache SubgraphElement.Version: [%s]]",
									subgraphDiff.getName(),
									subgraphElementRecord.getSubgraphElementUpdateVersion().toString(), 
									subgraphCache.getSubgraphElementRecord().get().getSubgraphElementUpdateVersion().toString())
						);
					}
				}
				
				if (vComparator.compare(subgraphElementRecord.getSubgraphElementUpdateVersion(), subgraphCache.getSubgraphVersion()) <= 0) {
					throw new GraphMismatchException(
						String.format("SubgraphElementRecord inconsistent: [SubgraphElementUpdateVersion: [%s] <= cache SubgraphVersion: [%s]]",
								subgraphElementRecord.getSubgraphElementUpdateVersion().toString(),
								subgraphCache.getSubgraphVersion().toString())
					);
				}
			}
		}
		
		//3. Various links/shared elements integrity checks
		if (subgraphDiff.getLinkUpdatesByElementId().isPresent()) {
			Map<I, VLinkDiff<V, I>> linkUpdates = subgraphDiff.getLinkUpdatesByElementId().get();
			
			for (VLinkDiff<V, I> linkUpdate : linkUpdates.values()) {
				if (linkUpdate.getLinkUpdate().isPresent()) {
					//3.1 LinkUpdate.elementId should == LinkId
					if (!linkUpdate.getLinkUpdate().get().getElementId().equals(linkUpdate.getLinkId())) {
						throw new GraphMismatchException(
							String.format("LinkUpdates error: [LinkId [%s] != LinkUpdate.elementId [%s]]",
									linkUpdate.getLinkUpdate().get().getElementId().toString(),
									linkUpdate.getLinkId().toString())
						);
					}
					
					//3.2. Link should either exist in cache or has elementUpdate
					if (!linkUpdate.getLinkedElementVersionUpdate().isPresent()) {
						VLink<V, I> cacheLink = subgraphCache.getSubgraphLinksByElementId().get(linkUpdate.getLinkedElementId());
						//3.2.1. If LinkedElementVersionUpdate is not present - link should exist in cache
						if (cacheLink == null) {
							throw new GraphMismatchException(
								String.format("LinkUpdates error: Link for element should either exist in cache or has elementUpdate. subgraph [%s] elementId [%s]",
										subgraphDiff.getName(),
										linkUpdate.getLinkedElementId().toString())
							);
						}
						//3.2.2 Note that link.getElementId() for an update in general case doesn't have to equal linkUpdate.getLinkId()
						//Consider a situation when element is removed from subgraph and then reconnected.
						//
						//However, in such scenario LinkedElementVersionUpdate will always be present.
						//If LinkedElementVersionUpdate is not present, the link update must refer to an existing link.
						else if (!cacheLink.getElementId().equals(linkUpdate.getLinkId())) {
							throw new GraphMismatchException(
								String.format("LinkUpdates error: Unchanged link update changed its linkId. subgraph [%s] elementId [%s] diff linkId [%s] cache linkId [%s]",
										subgraphDiff.getName(),
										linkUpdate.getLinkedElementId().toString(),
										linkUpdate.getLinkId().toString(),
										cacheLink.getElementId().toString())
								);
						}
						
						//element should exist in cache (checked below)
					}
					
					//If Link has elementUpdate, element should exist in diff (checked below)
					
					//3.3. Tombstone mark checks
					if (linkUpdate.getLinkUpdate().get().isTombstone()) {
						I linkedElementId = linkUpdate.getLinkedElementId();
						VElement<V, I> elm = null;
						
						if (subgraphDiff.getLinkUpdatesByElementId().isPresent())
							elm = graphDiff.getElement(linkedElementId);
						
						if (elm == null) {
							SharedElementRec<V, I> elementRec = cacheSharedElements.get(linkedElementId);
							if (elementRec != null)
								elm = elementRec.element;
						}
						
						//3.3.1. Element should exist either in diff or cache
						if (elm == null) {
							throw new GraphMismatchException(
									String.format("LinkUpdates error: Linked element doesn't exist in diff/cache. Subgraph [%s] elementId [%s] diff linkId [%s]",
											subgraphDiff.getName(),
											linkUpdate.getLinkedElementId().toString(),
											linkUpdate.getLinkId().toString())
									);
						}
						
						if (elm instanceof VEdgeType) {
							//3.3.2. EdgeType tombstone mark checks
							//No non-tombstoned Edges of this type should exist (diff or cache)
							I edgeTypeId = elm.getElementId();
							Set<I> diffEdges = diffEdgesByType.get(edgeTypeId);
							Set<I> cacheEdges = subgraphCache.getEdgesByType().get(edgeTypeId);
							
							for (I cacheEdgeId : cacheEdges)
								if (!diffEdges.contains(cacheEdgeId))
									if (!isElementTombstonedOrDeleted(cacheEdgeId, subgraphDiff, subgraphCache)) {
										throw new GraphMismatchException(
												String.format("LinkUpdates error: EdgeType is tombstoned, but non-tombstoned edge of this type exists in cache. Subgraph [%s] edgeTypeId [%s] edgeId [%s]",
														subgraphDiff.getName(),
														edgeTypeId.toString(),
														cacheEdgeId.toString())
												);
									}
							
							for (I diffEdgeId : diffEdges)
								if (!isElementTombstonedOrDeleted(diffEdgeId, subgraphDiff, subgraphCache)) { 
									throw new GraphMismatchException(
											String.format("LinkUpdates error: EdgeType is tombstoned, but non-tombstoned edge of this type exists in diff. Subgraph [%s] edgeTypeId [%s] edgeId [%s]",
													subgraphDiff.getName(),
													edgeTypeId.toString(),
													diffEdgeId.toString())
											);
								}
						} else if (elm instanceof VVertexType) {
							//3.3.3. VertexType tombstone mark checks
							//No non-tombstoned Vertexes of this type should exist (diff or cache)
							I vertexTypeId = elm.getElementId();
							Set<I> diffVertexes = diffVertexesByType.get(vertexTypeId);
							Set<I> cacheVertexes = subgraphCache.getVertexesByType().get(vertexTypeId);
							
							for (I cacheVertexId : cacheVertexes)
								if (!diffVertexes.contains(cacheVertexId))
									if (!isElementTombstonedOrDeleted(cacheVertexId, subgraphDiff, subgraphCache)) {
										throw new GraphMismatchException(
												String.format("LinkUpdates error: VertexType is tombstoned, but non-tombstoned vertex of this type exists in cache. Subgraph [%s] vertexTypeId [%s] vertexId [%s]",
														subgraphDiff.getName(),
														vertexTypeId.toString(),
														cacheVertexId.toString())
												);
									}
							
							for (I diffVertexId : diffVertexes)
								if (!isElementTombstonedOrDeleted(diffVertexId, subgraphDiff, subgraphCache)) { 
									throw new GraphMismatchException(
											String.format("LinkUpdates error: VertexType is tombstoned, but non-tombstoned vertex of this type exists in diff. Subgraph [%s] vertexTypeId [%s] vertexId [%s]",
													subgraphDiff.getName(),
													vertexTypeId.toString(),
													diffVertexId.toString())
											);
								}
						} else if (elm instanceof VVertex) {
							//3.3.4. Edge tombstone checks
							VVertex<V, I> vertex = (VVertex<V, I>)elm;
							
							Set<I> edgesForVertex = diffEdgesByVertex.get(vertex.getElementId());
							
							for (I edgeId : edgesForVertex) {
								//All connected edges should be tombstoned (diff or cache)
								if (!isElementTombstonedOrDeleted(edgeId, subgraphDiff, subgraphCache))
									throw new GraphMismatchException(
											String.format("LinkUpdates error: Vertex can't be tombstoned if it's edges are not tombstoned. Subgraph [%s] vertex linkId [%s] vertexId [%s] edgeId [%s]",
													subgraphDiff.getName(),
													linkUpdate.getLinkedElementId().toString(),
													linkUpdate.getLinkId().toString(),
													edgeId.toString())
											);
							}
						}
					}
					
					//3.4. Check link version to be > cache Subgraph version
					if (vComparator.compare(linkUpdate.getLinkUpdate().get().getVersion(), subgraphCache.getSubgraphVersion()) <= 0) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: [LinkUpdate id [%s] version [%s] <= cache cache SubgraphVersion: [%s]]",
										linkUpdate.getLinkUpdate().get().getElementId().toString(),
										linkUpdate.getLinkUpdate().get().getVersion().toString(),
										subgraphCache.getSubgraphVersion().toString())
							);
					}
				} else {
					if (!linkUpdate.getLinkedElementVersionUpdate().isPresent()) {
						//3.5. No link info, no element info - error
						throw new GraphMismatchException(
							String.format("LinkUpdates error: either LinkUpdate or LinkedElementUpdate should be present. [%s]",
									linkUpdate.getLinkId().toString())
						);
					}
					
					//3.6. Link should exist in cache
					VLink<V, I> link = subgraphCache.getSubgraphLinksByElementId().get(linkUpdate.getLinkedElementId());
					if (link == null) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: received Element-only update for nonexistent Link. LinkId: [%s] ElementId: [%s]",
										linkUpdate.getLinkId().toString(),
										linkUpdate.getLinkedElementId().toString())
							);
					} else if (!link.getElementId().equals(linkUpdate.getLinkId())) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: received Element-only update linkId changed. diff LinkId: [%s] cache LinkId: [%s] ElementId: [%s]",
										linkUpdate.getLinkId().toString(),
										link.getElementId().toString(),
										linkUpdate.getLinkedElementId().toString())
							);
					}
					
					//element should exist in cache (checked below)
					//element should exist in diff (checked below)
				}
				
				if (linkUpdate.getLinkedElementVersionUpdate().isPresent()) {
					I linkId = linkUpdate.getLinkId();
					I linkedElmId = linkUpdate.getLinkedElementId();
					V linkedElmVersion = linkUpdate.getLinkedElementVersionUpdate().get();
					
					//add updated referenced element to check 
					referencedElementsSet.add(linkedElmId);
					
					VElement<V, I> linkedElmFromDiff = graphDiff.getElement(linkedElmId);
					
					SharedElementRec<V, I> sharedElement = cacheSharedElements.get(linkedElmId);
					
					/*!!!!!!!!!!!!!!!REDUNDANT!!!!!!!!!!!!!!!
					//For partial link updates, element should exist in cache
					if (!linkUpdate.getLinkUpdate().isPresent() || !linkUpdate.getLinkedElementVersionUpdate().isPresent()) {
						if (sharedElement == null) {
							throw new GraphMismatchException(
									String.format("LinkUpdates error: Partial update: element doesn't exist in cache. LinkId: [%s] ElementId: [%s]",
											linkId.toString(),
											linkedElmId.toString())
								);
						}
					}*/
					
					//3.7. element should exist in diff
					if (linkedElmFromDiff == null) {
						throw new GraphMismatchException(
							String.format("LinkUpdates error: Element update doesn't exist in diff. LinkId: [%s] ElementId: [%s]",
									linkId.toString(),
									linkedElmId.toString())
						);
					}
					
					//3.8. if element exists in cache, cache version should be <= diff version
					//Version can be equal if an existing element is being attached to a subgraph
					//TODO: redundant attachment checks
					if (sharedElement != null) {
						if (vComparator.compare(sharedElement.element.getVersion(), linkedElmVersion) > 0) {
							throw new GraphMismatchException(
									String.format("LinkUpdates error: Updated element's cache version [%s] >= updated element version [%s]. LinkId: [%s] ElementId: [%s]",
											sharedElement.element.getVersion(),
											linkedElmVersion,
											linkId.toString(),
											linkedElmId.toString())
								);
						}
					}
					
					//--------------------
					
					//We ignore tombstone status for future findElementInSubgraphDiffOrCache checks if element by itself is tombstoned 
					boolean ignoreTombstone;
					if (linkUpdate.getLinkUpdate().isPresent()) {
						ignoreTombstone = linkUpdate.getLinkUpdate().get().isTombstone();
					} else {
						//Existence of cacheLink was checked above at 3.6. (if LinkUpdate isNotPresent)
						VLink<V, I> cacheLink = subgraphCache.getSubgraphLinksByElementId().get(linkUpdate.getLinkedElementId());
						ignoreTombstone = cacheLink.isTombstone();
					}
					
					//3.9. Extra checks for VVertex and VEdge addition
					if (linkedElmFromDiff instanceof VVertex) {
						//3.9.1. Vertex addition checks: VertexType should be linked to this subgraph (exist either in cache or diff)
						VVertex<V, I> vertex = ((VVertex<V, I>)linkedElmFromDiff);
						I vertexTypeId = vertex.getVertexTypeId();
						
						findElementInSubgraphDiffOrCache(vertexTypeId, subgraphDiff, graphDiff, subgraphCache, 
								String.format("VertexType for Vertex [%s]", linkedElmFromDiff.getElementId().toString()),
								VVertexType.class, cacheSharedElements, ignoreTombstone);
					} else if (linkedElmFromDiff instanceof VEdge) {
						//3.9.2. Edge addition checks
						VEdge<V, I> edge = (VEdge<V, I>)linkedElmFromDiff;
						
						I edgeTypeId = edge.getEdgeTypeId();
						I vertexFromId = edge.getVertexFromId();
						I vertexToId = edge.getVertexToId();
						
						//Edge addition checks: EdgeType should be linked to this subgraph (exist either in cache or diff)
						findElementInSubgraphDiffOrCache(edgeTypeId, subgraphDiff, graphDiff, subgraphCache, 
								String.format("VertexType for Edge [%s]", linkedElmFromDiff.getElementId().toString()),
								VEdgeType.class, cacheSharedElements, ignoreTombstone);
						
						//Edge addition checks: both Vertexes should be linked to this subgraph (exist either in cache or diff)

						//For relaxedEdgeCheck - do not check vertex if it doesn't exist
						if ((!relaxedEdgeCheck) || (elementExists(vertexFromId, graphDiff, cacheSharedElements)))
						findElementInSubgraphDiffOrCache(vertexFromId, subgraphDiff, graphDiff, subgraphCache, 
								String.format("VertexFrom for Edge [%s]", linkedElmFromDiff.getElementId().toString()),
								VVertex.class, cacheSharedElements, ignoreTombstone);
						
						if ((!relaxedEdgeCheck) || (elementExists(vertexFromId, graphDiff, cacheSharedElements)))
						findElementInSubgraphDiffOrCache(vertexToId, subgraphDiff, graphDiff, subgraphCache, 
								String.format("VertexTo for Edge [%s]", linkedElmFromDiff.getElementId().toString()),
								VVertex.class, cacheSharedElements, ignoreTombstone);
					}
					
					//3.10 Check linked element's versions to be > subgraph cache versions
					if (vComparator.compare(linkedElmVersion, subgraphCache.getSubgraphVersion()) <= 0) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: [LinkUpdate id [%s] LinkedElement Id [%s] LinkedElement version [%s] <= cache SubgraphVersion: [%s]]",
										linkId.toString(),
										linkedElmId,
										linkedElmVersion.toString(),
										subgraphCache.getSubgraphVersion().toString())
							);
					}
					
					//3.11 Diff's Shared element's version should match with element version in link
					if (vComparator.compare(linkedElmFromDiff.getVersion(), linkedElmVersion) != 0) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: [LinkUpdate id [%s] LinkedElement Id [%s] LinkedElement version [%s] != diff ElementVersion: [%s]]",
										linkId.toString(),
										linkedElmId,
										linkedElmVersion.toString(),
										linkedElmFromDiff.getVersion().toString())
							);
					}
				}
			}
		}
		
		//4. Deleted links checks
		if (subgraphDiff.getElementSync().isPresent()) {
			VElementSyncRecord<V, I> elementSync = subgraphDiff.getElementSync().get();
			
			//4.1 Check that removed elements don't exist in diff
			if (subgraphDiff.getLinkUpdatesByElementId().isPresent()) {
				for (VLinkDiff<V, I> linkDiff : subgraphDiff.getLinkUpdatesByElementId().get().values()) {
					if (!elementSync.getElementIds().contains(linkDiff.getLinkedElementId())) {
						throw new GraphMismatchException(
								String.format("LinkUpdates error: Diff contains element that's supposed to be deleted. Subgraph [%s] elementId [%s]",
										subgraphDiff.getName(),
										linkDiff.getLinkedElementId().toString())
							);
					}
				}
			}
			
			//4.2. Check that all active elements exist in cache/diff
			for (I elementId : elementSync.getElementIds()) {
				boolean contains = false;
				
				if (subgraphDiff.getLinkUpdatesByElementId().isPresent())
					contains = subgraphDiff.getLinkUpdatesByElementId().get().containsKey(elementId);
				
				if (!contains)
					contains = subgraphCache.getSubgraphLinksByElementId().containsKey(elementId);
				
				if (!contains) {
					throw new GraphMismatchException(
							String.format("LinkUpdates error: Neither Diff nor cache contain active element. Subgraph [%s] elementId [%s]",
									subgraphDiff.getName(),
									elementId.toString())
						);
				}
			}
			
			//4.3. Element delete checks
			for (VLink<V, I> linkUpdate : subgraphCache.getSubgraphLinksByElementId().values()) {
				if (!elementSync.getElementIds().contains(linkUpdate.getLinkedElementId())) {
					SharedElementRec<V, I> removedElement = cacheSharedElements.get(linkUpdate.getLinkedElementId());
					VElement<V, I> elm = removedElement.element;
					
					if (elm instanceof VEdgeType) {
						//4.3.1. EdgeType removal checks
						//No Edges of this type should exist (diff or cache, can be deleted by the same diff)
						I edgeTypeId = elm.getElementId();
						
						Set<I> diffEdges = diffEdgesByType.get(edgeTypeId);
						Set<I> cacheEdges = subgraphCache.getEdgesByType().get(edgeTypeId);
						
						for (I cacheEdgeId : cacheEdges)
							if (!diffEdges.contains(cacheEdgeId))
								if (!isElementDeleted(cacheEdgeId, subgraphDiff, subgraphCache)) {
									throw new GraphMismatchException(
											String.format("LinkUpdates error: EdgeType is deleted, but non-deleted edge of this type exists in cache. Subgraph [%s] edgeTypeId [%s] edgeId [%s]",
													subgraphDiff.getName(),
													edgeTypeId.toString(),
													cacheEdgeId.toString())
											);
								}
						
						for (I diffEdgeId : diffEdges)
							if (!isElementDeleted(diffEdgeId, subgraphDiff, subgraphCache)) { 
								throw new GraphMismatchException(
										String.format("LinkUpdates error: EdgeType is deleted, but non-deleted edge of this type exists in diff. Subgraph [%s] edgeTypeId [%s] edgeId [%s]",
												subgraphDiff.getName(),
												edgeTypeId.toString(),
												diffEdgeId.toString())
										);
							}
					} else if (elm instanceof VVertexType) {
						//4.3.2. VertexType removal checks
						//No Vertexes of this type should exist (diff or cache, can be deleted by the same diff)
						I vertexTypeId = elm.getElementId();
						Set<I> diffVertexes = diffVertexesByType.get(vertexTypeId);
						Set<I> cacheVertexes = subgraphCache.getVertexesByType().get(vertexTypeId);
						
						for (I cacheVertexId : cacheVertexes)
							if (!diffVertexes.contains(cacheVertexId))
								if (!isElementDeleted(cacheVertexId, subgraphDiff, subgraphCache)) {
									throw new GraphMismatchException(
											String.format("LinkUpdates error: VertexType is deleted, but non-deleted vertex of this type exists in cache. Subgraph [%s] vertexTypeId [%s] vertexId [%s]",
													subgraphDiff.getName(),
													vertexTypeId.toString(),
													cacheVertexId.toString())
											);
								}
						
						for (I diffVertexId : diffVertexes)
							if (!isElementDeleted(diffVertexId, subgraphDiff, subgraphCache)) { 
								throw new GraphMismatchException(
										String.format("LinkUpdates error: VertexType is deleted, but non-deleted vertex of this type exists in diff. Subgraph [%s] vertexTypeId [%s] vertexId [%s]",
												subgraphDiff.getName(),
												vertexTypeId.toString(),
												diffVertexId.toString())
										);
							}
					} else if (elm instanceof VVertex) {
						//4.3.3. Vertex removal checks
						VVertex<V, I> vertex = (VVertex<V, I>)elm;
						
						Set<I> edgesForVertex = diffEdgesByVertex.get(vertex.getElementId());
						
						for (I edgeId : edgesForVertex) {
							if (!isElementDeleted(edgeId, subgraphDiff, subgraphCache))
								throw new GraphMismatchException(
										String.format("LinkUpdates error: Vertex can't be deleted if it's edges are not deleted. Subgraph [%s] vertex linkId [%s] vertexFromId [%s] edgeId [%s]",
												subgraphDiff.getName(),
												edgeId.toString(),
												linkUpdate.getElementId().toString(),
												edgeId.toString())
										);
						}
					}
				}
			}
		}
	}
}
