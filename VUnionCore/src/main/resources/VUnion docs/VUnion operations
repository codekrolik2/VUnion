VGraph operations

Let's define abstract Counter, that produces globally monotonically increasing sequence.

	Counter<V> {
		V getNext();
	}

Counter will be used for version generation.

---

Lets describe operations on VGraph:

1) New vertex type, vertex +links to subgraph
Notes: VertexType should be created prior to vertex creation and linked to a subgraph prior to linking a vertex of that type.
It's also ok to link vertexes and vertex types to a subgraph together in the same transaction.

	{
		"from" : "[]",
		"graphName" : "graph0",

		"vertexTypes" : [
			{
				"elementId" : 1,
				"version" : 1,
				"key" : "vertexTypeKey1",
				"content" : "<sample edge type>",
				
				"vertexTypeName" : "vertexType0"
			}
		],
		
		"vertexes" : [
			{
				"elementId" : 2,
				"version" : 2,
				"key" : "vertexKey1",
				"content" : "<sample vertex content>",
				
				"vertexTypeId" : 1
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" :  4,
				
				"updates" : [
					{
						"id" : 3,
						"linkUpdate" : {
							"key" : "linkKey1",
							"version" : 3,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 1
						}
					},
					{
						"id" : 4,
						"linkUpdate" : {
							"key" : "linkKey2",
							"version" : 4,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 2,
							"linkElementVersion" : 2
						}
					}
				]
			}
		]
	}

---

2) New edge type, edge +links to subgraph
Notes: EdgeType should be created prior to edge creation and linked to a subgraph prior to linking an edge of that type.

Edge can only be linked if both of its vertexes are linked to subgraph.
It's also ok to link edges, edge types and corresponding vertexes to a subgraph together in the same transaction.

	{
		"from" : "[subgraph0:5]",
		"graphName" : "graph0",
		
		"edgeTypes" : [
			{
				"elementId" : 6,
				"version" : 6,
				"key" : "edgeTypeKey1",
				"content" : "<sample edge type>",
				
				"edgeTypeName" : "edgeType0"
			}
		],
		
		"edges" : [
			{
				"elementId" : 7,
				"version" : 7,
	
				"key" : "edgeKey1",
				"content" : "<sample edge content>",
				
				"edgeTypeId" : 6,
				"vertexFromId" : 2,
				"vertexToId" : 5,
				"isDirected" : false
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 9,
				 
				"updates" : [
					{
						"id" : 8,
						"linkUpdate" : {
							"key" : "linkKey3",
							"version" : 8,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 6,
							"linkElementVersion" : 6
						}
					},
					{
						"id" : 9,
						"linkUpdate" : {
							"key" : "linkKey4",
							"version" : 9,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 7,
							"linkElementVersion" : 7
						}
					}
				]
			}
		]
	}

---

3) New links for existing elements (vertexes, edges, vertex types, edge types) 
Notes: same constraints as in (1) apply to vertex linking, same constraints as in (2) apply to edge linking

In this case we always want to have a linked element in diff's vertexes/edges collection.
Even though such element (if newly added) might have a version behind our graph version, and very well might be already present in the VGraphCache's vertexes or edges, it's hard  to reason about whether it's there or not in presence of subgraph filters.

So it seems to me ATM more viable to just include it every time it's linked to a subgraph than reasoning about whether other subgraphs of a particular client with particular filters may or may not have previously retrieved the current version of said element.

Note how in our example although _subgraph0_ has version 5, and yet we have vertexes {1:v1}, {2:v2} and edge {4:v4} in our diff structure.
Since we know that those elements were previously linked to _subgraph0_, we can reason about them being already in the structure of the client cache, and based on that knowledge avoid sending them the second time. However, if, for example, our client filters out our stream to receive only updates for "subgraph1", the elements would appear new to client's cache and in that case it would be mandatory to have them in vertexes/edges arrays.
It's still possible to reason which elements exactly were filtered out, but it's more challenging and will require creating complicated filter-dependent diff retrieval, so as of now it seems as premature optimisation.
It seems most reasonable to address and optimize it later, during implementation of stream filters.

	{
		"from" : "[subgraph0:9]",
		"graphName" : "graph0",
		
		"vertexTypes" : [
			{
				"elementId" : 1,
				"version" : 1,
				"key" : "vertexTypeKey1",
				"content" : "<sample edge type>",
				
				"vertexTypeName" : "vertexType0"
			}
		],
		
		"vertexes" : [
			{
				"elementId" : 2,
				"version" : 2,
				"key" : "vertexKey1",
				"content" : "<sample vertex content>",
				
				"vertexTypeId" : 1
			},
			{
				"elementId" : 5,
				"version" : 5,
				"key" : "vertexKey2",
				"content" : "<sample vertex content>",
				
				"vertexTypeId" : 1
			}
		],
		
		"edgeTypes" : [
			{
				"elementId" : 6,
				"version" : 6,
				"key" : "edgeTypeKey1",
				"content" : "<sample edge type>",
				
				"edgeTypeName" : "edgeType0"
			}
		],
		
		"edges" : [
			{
				"elementId" : 7,
				"version" : 7,
	
				"key" : "edgeKey1",
				"content" : "<sample edge content>",
				
				"edgeTypeId" : 6,
				"vertexFromId" : 2,
				"vertexToId" : 5,
				"isDirected" : false
			}
		],

		"subgraphs" : [
			{
				"name" : "subgraph1",
				"subgraphVersionTo" : 14,
			
				"updates" : [
					{
						"id" : 10,
						"linkUpdate" : {
							"key" : "linkKey5",
							"version" : 10,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 1
						}
					},
					{
						"id" : 11,
						"linkUpdate" : {
							"key" : "linkKey6",
							"version" : 11,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 2,
							"linkElementVersion" : 2
						}
					},
					{
						"id" : 12,
						"linkUpdate" : {
							"key" : "linkKey7",
							"version" : 12,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 5,
							"linkElementVersion" : 5
						}
					},
					{
						"id" : 13,
						"linkUpdate" : {
							"key" : "linkKey8",
							"version" : 13,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 6,
							"linkElementVersion" : 6
						}
					},
					{
						"id" : 14,
						"linkUpdate" : {
							"key" : "linkKey9",
							"version" : 14,
							"content" : "<sample link content>",
							"isTombstone" : false
						},
						"linkElementVersion" : {
							"linkElementId" : 7,
							"linkElementVersion" : 7
						}
					},
				]
			}
		]
	}

---

4) Vertex updated.
All three options - content updated, key updated and type updated should return the whole updated vertex structure, as well as corresponding link updates for all linked subgraphs

	{
		"from" : "[subgraph0:9,subgraph1:14]",
		"graphName" : "graph0",

		"vertexes" : [
			{
				"elementId" : 2,
				"version" : 15,
				"key" : "vertexKey1-1",
				"content" : "<sample vertex content-1>",
				
				"vertexTypeId" : 1
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 15,
			
				"updates" : [
					{
						"id" : 4,
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 15
						}
					}
				]
			},
			{
				"name" : "subgraph1",
				"subgraphVersionTo" : 15,
			
				"updates" : [
					{
						"id" : 11,
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 15
						}
					}
				]
			}
		]
	}

---

5) Edge updated
All options - content updated, key updated, vertex from updated, vertex to updated, type updated and isDirected updated should return the whole updated edge structure, as well as corresponding link updates for all connected subgraphs

	{
		"from" : "[subgraph0:15,subgraph1:15]",
		"graphName" : "graph0",

		"edges" : [
			{
				"elementId" : 7,
				"version" : 16,
	
				"key" : "edgeKey1-1",
				"content" : "<sample edge content-1>",
				
				"edgeTypeId" : 6,
				"vertexFromId" : 1,
				"vertexToId" : 3,
				"isDirected" : true
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 16,
			
				"updates" : [
					{
						"id" : 5,
						"linkElementVersion" : {
							"linkElementId" : 7,
							"linkElementVersion" : 16
						}
					}
				]
			},
			{
				"name" : "subgraph1",
				"subgraphVersionTo" : 16,
			
				"updates" : [
					{
						"id" : 8,
						"linkElementVersion" : {
							"linkElementId" : 7,
							"linkElementVersion" : 16
						}
					}
				]
			}
		]
	}

---

6) Link updated
Notes:
a. All options - content updated, key updated and isTombstone updated should return the updated link structure

b. Marking link as tombstone: 
b.1. Vertex link can only be tombstoned if all connected edges are tombstoned.
Logically that refers to the fact that an active (not tombstoned) edge can't point to a tombstoned vertex.

b.2. Vertex type and Edge type. 
Vertex type link can only be tombstoned if all vertexes of that type are tombstoned.
Similarly, Edge type link can only be tombstoned if all edges of that type are tombstoned.
Logically that refers to the fact that an active (not tombstoned) vertex/edge can't be of a tombstoned type.

b.3. I.e. tombstone edges first, vertexes after, and then types 
It's also ok to tombstone connected edges, vertexes and types together in the same transaction.

c. Untombstone link: 
c.1. Edge/vertex links can only be untombstoned if corresponding Edge/Vertex types links are not tombstoned.
Logically that refers to the fact that an active (not tombstoned) vertex/edge can't be of a tombstoned type.

c.2. Edge link can only be untombstoned if no connected vertexes are tombstoned.
Logically that refers to the fact that an active (not tombstoned) edge can't point to a tombstoned vertex.

c.3. I.e. untombstone types first, vertexes after, and then edges (it's also ok to untombstone connected types, vertexes and edges together in the same transaction).

	{
		"from" : "[subgraph0:16,subgraph1:16]",
		"graphName" : "graph0",

		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 17,
			
				"updates" : [
					{
						"id" : 4,
						"linkUpdate" : {
							"key" : "linkKey1-1",
							"version" : 17,
							"content" : "<sample link content-1>",
							"isTombstone" : true
						}
					}
				]
			}
		]
	}

---
 
7) GraphElement
7.1 GraphElement created

	{
		"from" : "[subgraph0:17,subgraph1:16]",
		"graphName" : "graph0",

		"graphElementRecord" : {
			"graphElementUpdateVersion" : 18,
			"graphElement" : {
				"elementId" : 15,
				"version" : 18,

				"key" : "graphElementKey1",
				"content" : "<sample graph element content>"
			}
		}
	}

7.2 GraphElement updated

	{
		"from" : "[18,subgraph0:17,subgraph1:16]",
		"graphName" : "graph0",

		"graphElementRecord" : {
			"graphElementUpdateVersion" : 19,
			"graphElement" : {
				"elementId" : 15,
				"version" : 19,

				"key" : "graphElementKey1-1",
				"content" : "<sample graph element content-1>"
			}
		}
	}

---

8) SubgraphElement 
8.1 SubgraphElement created

	{
		"from" : "[19,subgraph0:17,subgraph1:16]",
		"graphName" : "graph0",

		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 20,
			
				"subgraphElementRecord" : {
					"subgraphElementUpdateVersion" : 20,
					"subgraphElement" : {
						"elementId" : 16,
						"version" : 20,

						"key" : "subgraphElementKey1",
						"content" : "<sample subgraph element content>"
					}
				}
			}
		]
	}

8.2 SubgraphElement updated

	{
		"from" : "[19,subgraph0:20,subgraph1:16]",
		"graphName" : "graph0",

		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 21,
			
				"subgraphElementRecord" : {
					"subgraphElementUpdateVersion" : 21,
					"subgraphElement" : {
						"elementId" : 16,
						"version" : 21,

						"key" : "subgraphElementKey1-1",
						"content" : "<sample subgraph element content-1>"
					}
				}
			}
		]
	}

---

9) Vertex Type renamed

	{
		"from" : "[19,subgraph0:21,subgraph1:16]",
		"graphName" : "graph0",

		"vertexTypes" : [
			{
				"elementId" : 1,
				"version" : 22,
				"key" : "vertexTypeKey1-1",
				"content" : "<sample edge type>",
				
				"vertexTypeName" : "vertexType0-1"
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 22,
			
				"updates" : [
					{
						"id" : 3,
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 22
						}
					},
				]
			},
			{
				"name" : "subgraph1",
				"subgraphVersionTo" : 22,
			
				"updates" : [
					{
						"id" : 10,
						"linkElementVersion" : {
							"linkElementId" : 1,
							"linkElementVersion" : 22
						}
					}
				]
			},
		]
	}

---

10) Edge Type renamed

	{
		"from" : "[19,subgraph0:22,subgraph1:22]",
		"graphName" : "graph0",

		"edgeTypes" : [
			{
				"elementId" : 6,
				"version" : 23,
				"key" : "edgeTypeKey1-1",
				"content" : "<sample edge type-1>",
				
				"edgeTypeName" : "edgeType2"
			}
		],
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 23,
			
				"updates" : [
					{
						"id" : 8,
						"linkElementVersion" : {
							"linkElementId" : 6,
							"linkElementVersion" : 23
						}
					}
				]
			},
			{
				"name" : "subgraph1",
				"subgraphVersionTo" : 23,
			
				"updates" : [
					{
						"id" : 13,
						"linkElementVersion" : {
							"linkElementId" : 6,
							"linkElementVersion" : 23
						}
					}
				]
			}
		]
	}

---

11) Delete link
Notes: 
a. Vertex type/edge type links can only be deleted if no corresponding vertexes/edges of that type are linked to subgraph (including tombstoned vertexes/edges)
Logically that refers to the fact that a vertex/edge (even tombstoned ones) can't be of nonexistent type, since tombstoned vertex/edge might be recovered.

b. Vertex link can only be deleted if no connected edges are linked to subgraph (including tombstoned edges)
Logically that refers to the fact that an edge (even a tombstoned one) can't point to a nonexistent vertex, since tombstoned edge might be recovered.

c. I.e. delete edges first, vertexes after, and then types (it's also ok to delete edges, vertexes and types together in the same transaction)

d. Involves sending element sync list to a given subgraph

e. deleting an element from all subgraphs effectively removes an attached element from active elements set, and therefore from RAM or DB cache.
Such element may still exist in SourceDB and later be relinked to some subgraph, but for all stream recepients (RAM cache; proxy DBs) is unreachable.

	//remove link id 14 / edge id 7
	{
		"from" : "[19,subgraph0:23,subgraph1:23]",
		"graphName" : "graph0",

		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 24,
			
				"elementSync" : {
					"elementSyncVersion" : 24,
					"elementIds" : [ 10, 11, 12, 13 ]
				}
			}
		]
	}

---

12) Delete vertex, edge, vertex type, edge type
Notes: 
a. essentially deleting all element's links to subgraphs
b. by doing so, creates orphaned elements that should be removed from cache, as described in 11.e

	See 11
	
---

13) Delete subgraph
Notes:
a. involves sending Subgraph sync list for a graph
b. Orphaned elements should be removed from cache, similarly to 11.e

	//remove subgraph1
	{
		"from" : "[19,subgraph0:24,subgraph1:23]",
		"graphName" : "graph0",
		
		"subgraphSync" : {
			"subgraphSyncVersion" : 25,
			"subgraphNames" : [ "subgraph0" ]
		}
	}

---

14) GraphElement deleted

	{
		"from" : "[19,subgraph0:25]",
		"graphName" : "graph0",
		
		"graphElementRecord" : {
			"graphElementUpdateVersion" : 26
		}
	}

---

15) SubgraphElement deleted

	{
		"from" : "[26,subgraph0:25]",
		"graphName" : "graph0",
		
		"subgraphs" : [
			{
				"name" : "subgraph0",
				"subgraphVersionTo" : 27,
				
				"subgraphElementRecord" : {
					"subgraphElementUpdateVersion" : 27
				}
			}
		]
	}

---

16) Destroy / Recover graph
Notes: Special rules apply to how graph diffs are constructed in presence of Destroy / Recover graph events.

a. If graph is destroyed and requester's family version is behind destroy version, the diff will contain only destroyed record (with field "isDestroyed" : true).
Client cache should completely wipe out all contents, including subgraphs, edge/vertex types and graph element, essentially everything other than graph record intself upon reception of such delta.
Effectively, the version is reduced to graph version, since all subgraphs will be deleted.

b. If graph is Recovered, and requester's family version is behind recover version, the diff will contain destroyed record (with field "isDestroyed" : false), and all the graph data to rebuild its cache, as if the request was made from minimal version ([]).
In this case all data destroyed when a graph destruction diff was received will be restored, since the entire graph will be included in the recovery diff.

c. It's possible that requester would receive the recovery diff (described in (b)), but never have received the destruction delta (described in (a)), due to some (connectivity) problems.
Considering that a graph can't be destroyed or recovered twice in a row, and destruction and recovery are strictly alternating events, and any graph recovery follows previous graph destruction - it's safe to assume that graph recovery that's described in recovery diff was preceded by destruction.
Therefore, to bring cache contents up to date, the following needs to be done:
	- graph destruction, as described in (a), followed by
	- graph recovery, as described in (b).

16.1 Destroy graph

	{
		"from" : "[19,subgraph0:27]",
		"graphName" : "graph0",
		
		"destroyedRecord" : {
			"destroyRecoverVersion" : 28,
			"isDestroyed" : true
		}
	}

16.2 Recover graph

	{
		"from" : "[28]",
		"graphName" : "graph0",
		
		"destroyedRecord" : {
			"destroyRecoverVersion" : 29,
			"isDestroyed" : false
		},
		
		//+the entire graph contents, as if requested (from == [])
	}
