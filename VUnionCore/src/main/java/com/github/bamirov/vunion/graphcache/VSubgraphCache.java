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
	protected String name;
	
	//subgraphVersionTo is always max subgraphVersion
	protected V subgraphVersionTo;
	//subgraphVersionTo will not necessarily corellate with existing links,
	//because I want to allow filters that will filter out links and elements inside subgraphs 
	
	protected Optional<V> elementSyncVersion = Optional.empty();
	
	protected TreeMap<V, VLink<V, I>> subgraphTimeline = new TreeMap<>();
	protected Map<I, VLink<V, I>> subgraphLinksByElementId = new HashMap<>();
	
	protected Map<I, Set<I>> vertexesByType = new HashMap<>();
	protected Map<I, Set<I>> edgesByType = new HashMap<>();
	
	protected Optional<SubgraphElementRecord<V, I>> subgraphElementRecord = Optional.empty();
	
	//TODO: this method is probably not needed, since subgraphVersionTo is always max subgraphVersion
	public V getSubgraphVersion() {
		@SuppressWarnings("unchecked")
		List<V> list = (List<V>)Arrays.asList(new Object[] {
				subgraphVersionTo,
				elementSyncVersion.isPresent() ? elementSyncVersion.get() : null,
				subgraphElementRecord.isPresent() ? subgraphElementRecord.get().subgraphElementVersion : null
		});
		return Collections.max(list);
	}
	
	protected void removeLinkInfo(VLink<V, I> cacheLink, VGraphCache<V, I> graphCache) {
		subgraphTimeline.remove(cacheLink.getTimelineVersion());
		subgraphLinksByElementId.remove(cacheLink.getElementId());
		
		SharedElementRec<V, I> sharedElementRec = graphCache.sharedElements.get(cacheLink.getElementId());
		
		VElement<V, I> linkedElement = sharedElementRec.getElement();
		removeElementFromTypeIndex(linkedElement);
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
		
		VElement<V, I> linkedElement = null;
		if (graphDiff.getEdges().isPresent())
			linkedElement = graphDiff.getEdges().get().get(newCacheLink.getElementId());
		
		if ((linkedElement == null) && (graphDiff.getVertexes().isPresent()))
			linkedElement = graphDiff.getVertexes().get().get(newCacheLink.getElementId());

		//If element is VVertex or VEdge, add to TypeIndex
		if (linkedElement != null)
			addElementToTypeIndex(linkedElement);
	}

	protected void addElementToTypeIndex(VElement<V, I> linkedElement) {
		if (linkedElement instanceof VVertex) {
			Set<I> vertexes = vertexesByType.get(((VVertex<V, I>)linkedElement).getVertexTypeId());
			
			if (vertexes == null) {
				vertexes = new HashSet<>();
				vertexesByType.put(((VVertex<V, I>)linkedElement).getVertexTypeId(), vertexes);
			}
			
			vertexes.remove(linkedElement.getElementId());
		} else if (linkedElement instanceof VEdge) {
			Set<I> edges = edgesByType.get(((VEdge<V, I>)linkedElement).getEdgeTypeId());
			
			if (edges == null) {
				edges = new HashSet<>();
				edgesByType.put(((VEdge<V, I>)linkedElement).getEdgeTypeId(), edges);
			}

			edges.remove(linkedElement.getElementId());
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
				SubgraphElementRecord<V, I> subgraphElementRec = subgraphElementRecord.get();
				
				subgraphElementRec.subgraphElementVersion = diffSeRecord.getSubgraphElementUpdateVersion();
				subgraphElementRec.subgraphElement = diffSeRecord.getSubgraphElement();
			} else {
				SubgraphElementRecord<V, I> seRecord = new SubgraphElementRecord<>(diffSeRecord.getSubgraphElementUpdateVersion(), 
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
}
