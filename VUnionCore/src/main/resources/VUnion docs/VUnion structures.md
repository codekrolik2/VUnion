VGraph structure

	VElement<V, I> {
		I elementId;
		V version;
	
		Optional<String> key;
		String content;
	}

	VGraph<V, I> extends VElement<V, I> { }
	VSubgraph<V, I> extends VElement<V, I> { }

	VVertex<V, I> extends VElement<V, I> {
		String vertexType;
	}

	VEdge<V, I> extends VElement<V, I> {
		String edgeType;
		I vertexFromId;
		I vertexToId;
		boolean isDirected;
	}

	VLink<V, I> extends VElement<V, I> {
		I linkElementId;
		V linkElementVersion;
		boolean isTombstone;
	}

---

VGraphVersion structure

	VGraphVersion<V> {
		V graphVersion;
		Map<String, V> subgraphVersions;
	}

---

VUnion diff structure
	
	VUnionDiff<V, I> {
		List<VGraphDiff<V, I>> graphs;
	}
	
	VGraphDiff<V, I> {
		VGraphVersion<V> from;
		String graphName;
		
		Map<I, VVertex<V, I>> vertexes;
		Map<I, VEdge<V, I>> edges;
		
		List<VSubgraphDiff<V, I>> subgraphs;
		Optional<VSubgraphSyncRecord<V>> subgraphSync;
		
		Optional<VGraphDestroyedRecord<V>> destroyedRecord;
		
		Optional<VGraphElementRecord<V, I>> graphElementRecord;
	}
	
	VSubgraphSyncRecord<V> {
		V subgraphSyncVersion;
		List<String> subgraphNames;
	}
	
	VGraphDestroyedRecord<V> {
		V destroyRecoverVersion;
		boolean isDestroyed;
	}
	
	VGraphElementRecord<V, I> {
		V graphElementUpdateVersion;
		Optional<VGraph<V, I>> graphElement;
	}
	
	VSubgraphDiff<V, I> {
		String name;
		V subgraphVersionTo;
		
		List<VLinkDiff<V, I>> updates;
		
		Optional<VElementSyncRecord<V, I>> elementSync;
		
		Optional<VSubgraphElementRecord<V, I>> subgraphElementRecord;
	}
	
	VElementSyncRecord<V, I> {
		V elementSyncVersion;
		List<I> elementIds;
	}
	
	VSubgraphElementRecord<V, I> {
		V subgraphElementUpdateVersion;
		Optional<VSubgraph<V, I>> subgraphElement;
	}

	VLinkDiff<V, I> {
		I id;
		Optional<VLinkUpdate<V>> linkUpdate;
		Optional<VLinkElementUpdate<V, I>> linkElementUpdate;
	}
	
	//VElement + VLink.isTombstone
	VLinkUpdate<V> {
		Optional<String> key;
		V version;
		String content;
		boolean isTombstone;
	}

	//VLink.linkElementId, VLink.linkElementVersion
	VLinkElementInfo<V> {
		I linkElementId;
		V linkElementVersion;
	}

