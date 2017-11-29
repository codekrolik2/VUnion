package com.github.bamirov.vunion.graphcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graph.VEdge;
import com.github.bamirov.vunion.graph.VElement;
import com.github.bamirov.vunion.graph.VLink;
import com.github.bamirov.vunion.graph.VVertex;
import com.github.bamirov.vunion.graphstream.VElementSyncRecord;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.VLinkDiff;
import com.github.bamirov.vunion.graphstream.VLinkUpdate;
import com.github.bamirov.vunion.graphstream.VSubgraphDiff;
import com.github.bamirov.vunion.graphstream.VSubgraphElementRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
class VSubgraphCache<V extends Comparable<V>, I> {
	protected final String name;
	
	//subgraphVersionTo is always max subgraphVersion
	protected V subgraphVersionTo;
	//subgraphVersionTo will not necessarily corellate with existing links,
	//because I want to allow filters that will filter out links and elements inside subgraphs 
	
	protected Optional<V> elementSyncVersion = Optional.empty();
	
	protected TreeMap<V, VLink<V, I>> subgraphTimeline = new TreeMap<>();
	protected Map<I, VLink<V, I>> subgraphLinksByElementId = new HashMap<>();
	
	protected Map<I, Set<I>> vertexesByType = new HashMap<>();
	protected Map<I, Set<I>> edgesByType = new HashMap<>();
	
	protected Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord = Optional.empty();
	
	protected VComparator<V> vComparator = new VComparator<>();
	
	//TODO: this method is probably not needed, since subgraphVersionTo is always max subgraphVersion
	public V getSubgraphVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphVersionTo,
				elementSyncVersion.isPresent() ? elementSyncVersion.get() : null,
				subgraphElementRecord.isPresent() ? subgraphElementRecord.get().getSubgraphElementUpdateVersion() : null
		});
		return Collections.max(list);
	}
	
	protected void removeLinkInfo(VLink<V, I> cacheLink, VGraphCache<V, I> graphCache) {
		subgraphTimeline.remove(cacheLink.getTimelineVersion());
		subgraphLinksByElementId.remove(cacheLink.getElementId());
		
		SharedElementRec<V, I> sharedElementRec = graphCache.sharedElements.get(cacheLink.getElementId());
		
		VElement<V, I> linkedElement = sharedElementRec.getElement();
		removeElementFromTypeIndex(linkedElement);
		
		//decrement reference count on shared elements
		graphCache.decrementReferenceCount(cacheLink.getElementId());
	}
	
	protected void removeElementFromTypeIndex(VElement<V, I> linkedElement) {
		if (linkedElement instanceof VVertex) {
			Set<I> vertexes = vertexesByType.get(((VVertex<V, I>)linkedElement).getVertexTypeId());
			vertexes.remove(linkedElement.getElementId());
			if (vertexes.isEmpty())
				vertexesByType.remove(((VVertex<V, I>)linkedElement).getVertexTypeId());
		} else if (linkedElement instanceof VEdge) {
			Set<I> edges = edgesByType.get(((VEdge<V, I>)linkedElement).getEdgeTypeId());
			edges.remove(linkedElement.getElementId());
			if (edges.isEmpty())
				edgesByType.remove(((VEdge<V, I>)linkedElement).getEdgeTypeId());
		}
	}
	
	protected void addLinkInfo(VLink<V, I> newCacheLink, VSubgraphDiff<V, I> diff, VGraphDiff<V, I> graphDiff, 
			VGraphCache<V, I> graphCache) {
		subgraphTimeline.put(newCacheLink.getTimelineVersion(), newCacheLink);
		subgraphLinksByElementId.put(newCacheLink.getElementId(), newCacheLink);
		
		VElement<V, I> linkedElement = graphDiff.getElement(newCacheLink.getElementId());

		//If element is VVertex or VEdge, add to TypeIndex
		addElementToTypeIndex(linkedElement);
		
		//increment reference count on shared elements
		graphCache.incrementReferenceCount(linkedElement);
	}

	protected void addElementToTypeIndex(VElement<V, I> linkedElement) {
		if (linkedElement instanceof VVertex) {
			Set<I> vertexes = vertexesByType.get(((VVertex<V, I>)linkedElement).getVertexTypeId());
			
			if (vertexes == null) {
				vertexes = new HashSet<>();
				vertexesByType.put(((VVertex<V, I>)linkedElement).getVertexTypeId(), vertexes);
			}
			
			vertexes.add(linkedElement.getElementId());
		} else if (linkedElement instanceof VEdge) {
			Set<I> edges = edgesByType.get(((VEdge<V, I>)linkedElement).getEdgeTypeId());
			
			if (edges == null) {
				edges = new HashSet<>();
				edgesByType.put(((VEdge<V, I>)linkedElement).getEdgeTypeId(), edges);
			}
			
			edges.add(linkedElement.getElementId());
		}
	}
	
	public void applySubgraphDiff(VSubgraphDiff<V, I> diff, VGraphDiff<V, I> graphDiff, 
			VGraphCache<V, I> graphCache) throws GraphMismatchException {
		//1. Subgraph name must match
		if (!diff.getName().equals(name))
			throw new GraphMismatchException(
					String.format("Subgraph name doesn't match: diff [%s] cache [%s]", 
							diff.getName(), name));
		
		//2. Update SubgraphVersionTo
		subgraphVersionTo = diff.getSubgraphVersionTo();
		
		//3. Update subgraph element
		if (diff.getSubgraphElementRecord().isPresent()) {
			VSubgraphElementRecord<V, I> diffSeRecord = diff.getSubgraphElementRecord().get();
			
			if (subgraphElementRecord.isPresent()) {
				VSubgraphElementRecord<V, I> subgraphElementRec = subgraphElementRecord.get();
				
				subgraphElementRec.setSubgraphElementUpdateVersion(diffSeRecord.getSubgraphElementUpdateVersion());
				subgraphElementRec.setSubgraphElement(diffSeRecord.getSubgraphElement());
			} else {
				VSubgraphElementRecord<V, I> seRecord = new VSubgraphElementRecord<>(diffSeRecord.getSubgraphElementUpdateVersion(), 
						diffSeRecord.getSubgraphElement());
				subgraphElementRecord = Optional.of(seRecord);
			}
		}
		
		//4. Element Sync
		if (diff.getElementSync().isPresent()) {
			Set<I> activeLinks = diff.getElementSync().get().getElementIds();
			V elementSyncV = diff.getElementSync().get().getElementSyncVersion();
			
			elementSyncVersion = Optional.of(elementSyncV);
			
			List<VLink<V, I>> removedLinks = new ArrayList<VLink<V, I>>();
			for (VLink<V, I> link : subgraphLinksByElementId.values())
				if (!activeLinks.contains(link.getElementId()))
					removedLinks.add(link);
			
			for (VLink<V, I> link : removedLinks)
				removeLinkInfo(link, graphCache);			
		}
		
		//5. Link updates
		if (diff.getLinkUpdatesByElementId().isPresent())
		for (VLinkDiff<V, I> linkDiff : diff.getLinkUpdatesByElementId().get().values()) {
			VLink<V, I> oldCacheLink = subgraphLinksByElementId.get(linkDiff.getLinkedElementId());
			
			if ((oldCacheLink != null) && (!oldCacheLink.getElementId().equals(linkDiff.getLinkId()))) {
				//N.B. The idea is that if a link is tombstoned, it should be untombstoned instead of creating 
				//a new link for the same element.
				//Creation of new link can only happen following deletion of the old link.  
				throw new GraphMismatchException(
						String.format("LinkUpdates error: received a LinkUpdate with new LinkId, but old link wasn't deleted. "
								+ "Subgraph [%s] ElementId [%s] LinkId [%s] old LinkId [%s]",
								name,
								linkDiff.getLinkedElementId().toString(),
								linkDiff.getLinkId().toString(),
								oldCacheLink.getElementId().toString())
					);
			}
			
			if (oldCacheLink == null) {
				//5.1. Create new cache link
				I elementId = linkDiff.getLinkId();
				I linkElementId = linkDiff.getLinkedElementId();
				
				VLinkUpdate<V, I> linkUpdate = linkDiff.getLinkUpdate().get();
				V version = linkUpdate.getVersion();
				Optional<String> key = linkUpdate.getKey();
				String content = linkUpdate.getContent();
				boolean isTombstone = linkUpdate.isTombstone();
				
				V linkElementVersion = linkDiff.getLinkedElementVersionUpdate().get();
				
				VLink<V, I> newCacheLink = new VLink<V, I>(elementId, version, key, content,
						linkElementId, linkElementVersion, isTombstone);
				
				addLinkInfo(newCacheLink, diff, graphDiff, graphCache);
			} else {
				//5.2. Update existing cache link

				//Update timeline - remove old timeline record
				subgraphTimeline.remove(oldCacheLink.getTimelineVersion());
				
				if (linkDiff.getLinkUpdate().isPresent()) {
					VLinkUpdate<V, I> linkUpdate = linkDiff.getLinkUpdate().get();
					V version = linkUpdate.getVersion();
					Optional<String> key = linkUpdate.getKey();
					String content = linkUpdate.getContent();
					boolean isTombstone = linkUpdate.isTombstone();
					
					oldCacheLink.setVersion(version);
					oldCacheLink.setKey(key);
					oldCacheLink.setContent(content);
					oldCacheLink.setTombstone(isTombstone);
				}
				
				if (linkDiff.getLinkedElementVersionUpdate().isPresent()) {
					V linkElementVersion = linkDiff.getLinkedElementVersionUpdate().get();
					oldCacheLink.setLinkElementVersion(linkElementVersion);
					
					//Check if VertexType/EdgeType was updated
					SharedElementRec<V, I> sharedElementRec = graphCache.sharedElements.get(oldCacheLink.getElementId());
					VElement<V, I> oldElement = sharedElementRec.getElement();
					
					VElement<V, I> newElement = null;
					if (graphDiff.getEdges().isPresent())
						newElement = graphDiff.getEdges().get().get(oldCacheLink.getElementId());
					
					if ((newElement == null) && (graphDiff.getVertexes().isPresent()))
						newElement = graphDiff.getVertexes().get().get(oldCacheLink.getElementId());
					
					//If element is VVertex or VEdge, check if updates are needed for TypeIndex
					if (newElement != null) {
						if (!newElement.getClass().equals(oldElement.getClass())) {
							removeElementFromTypeIndex(oldElement);
							addElementToTypeIndex(newElement);
						}
					}
				}
				
				//Update timeline - add updated record to timeline
				subgraphTimeline.put(oldCacheLink.getTimelineVersion(), oldCacheLink);
			}
		}
	}
	
	public VSubgraphDiff<V, I> getDiff(V subgraphVersionFrom) {
		Optional<VSubgraphElementRecord<V, I>> diffSubgraphElementRecord = Optional.empty();
		if (subgraphElementRecord.isPresent()) {
			VSubgraphElementRecord<V, I> subgraphElementRec = subgraphElementRecord.get();
			if (vComparator.compare(subgraphElementRec.getSubgraphElementUpdateVersion(), subgraphVersionFrom) > 0) {
				diffSubgraphElementRecord = Optional.of(new VSubgraphElementRecord<V, I>(
					subgraphElementRec.getSubgraphElementUpdateVersion(),
					subgraphElementRec.getSubgraphElement()
				));
			}
		}
		
		Optional<VElementSyncRecord<V, I>> elementSync = Optional.empty();
		if (elementSyncVersion.isPresent()) {
			if (vComparator.compare(elementSyncVersion.get(), subgraphVersionFrom) > 0) {
				V elementSyncV = elementSyncVersion.get();
				Set<I> elementIds = new HashSet<I>();
				elementIds.addAll(subgraphLinksByElementId.keySet());
				
				VElementSyncRecord<V, I> elementSyncRecord = new VElementSyncRecord<>(elementSyncV, elementIds);
				elementSync = Optional.of(elementSyncRecord);
			}
		}
		
		Optional<Map<I, VLinkDiff<V, I>>> linkUpdatesByElementId = Optional.empty();
		Map<V, VLink<V, I>> tailMap = subgraphVersionFrom == null ?
				subgraphTimeline
				: subgraphTimeline.tailMap(subgraphVersionFrom, false);
		
		if (!tailMap.isEmpty()) {
			Map<I, VLinkDiff<V, I>> diffLinkUpdatesByElementIdMap = new HashMap<>();
			for (VLink<V, I> link : tailMap.values()) {
				I linkId = link.getElementId();
				I linkedElementId = link.getLinkElementId();
				
				Optional<VLinkUpdate<V, I>> linkUpdateOpt = Optional.empty();
				Optional<V> linkedElementVersionUpdate = Optional.empty();
				
				if (vComparator.compare(link.getVersion(), subgraphVersionFrom) > 0) {
					V version = link.getVersion();
					Optional<String> key = link.getKey();
					String content = link.getContent();
					boolean isTombstone = link.isTombstone();
					
					VLinkUpdate<V, I> linkUpdate = new VLinkUpdate<V, I>(linkId, version, key, content, isTombstone);
					linkUpdateOpt = Optional.of(linkUpdate);
				}
				
				if (vComparator.compare(link.getLinkElementVersion(), subgraphVersionFrom) > 0)
					linkedElementVersionUpdate = Optional.of(link.getLinkElementVersion());
				
				VLinkDiff<V, I> linkDiff = new VLinkDiff<>(linkId, linkedElementId, linkUpdateOpt, linkedElementVersionUpdate);
				diffLinkUpdatesByElementIdMap.put(linkedElementId, linkDiff);
			}
		}
		
		VSubgraphDiff<V, I> subgraphDiff = new VSubgraphDiff<V, I>(name, subgraphVersionTo, 
				linkUpdatesByElementId, elementSync, diffSubgraphElementRecord);
		return subgraphDiff;
	}
}
