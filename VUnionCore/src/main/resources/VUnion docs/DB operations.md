DB operations

1) Create shared element (VertexType, Vertex, EdgeType, Edge)

	1. Generate new version V
	2. Create element -> V
	3. Commit

2) Create Subgraph Link - attach a shared element E to a subgraph NewSG

	1. Lock element
	2. Lock newSG
	3. **Perform consistency checks, if applicable**
	4. Find subgraphs linked to E
	5. Lock linked subgraphs and newSG
	6. Generate new version V1
	7. Update element -> V1
	8. Update linked subgraphs version -> V1
	9. Generate new version V2
	10. Create E - NewSG subgraph link -> V2
	11. Update NewSG version -> V2
	12. Commit - unlock element, linked subgraphs, NewSG

Consistency checks: Vertex

	1. Corresponding VertexType should be attached to subgraph SG

Consistency checks: Edge

	1. Corresponding EdgeType should be attached to subgraph SG
	2. Both vertexes that edge points to should be attached to subgraph SG

No checks required for linking EdgeTypes and VertexTypes.

3) Update element
To avoid excessive checks it makes sense to prohibit altering vertexes that edges point to, as well as changing element link points to.

	1. Lock element
	2. Find linked subgraphs
	3. Lock linked subgraphs
	4. Generate new version V
	5. Update element -> V
	6. Update linked subgraphs version -> V
	7. Commit - unlock element, linked subgraphs

4) Update Subgraph Link

	1. Lock Link
	2. Find linked subgraph
	3. Lock linked subgraph
	4. Generate new version V
	5. Update link element -> V
	6. Update linked subgraphs version -> V
	7. Commit - unlock element, linked subgraphs

5) Tombstone/delete subgraph link

	1. Lock Link
	2. Find linked subgraph
	3. **Perform consistency checks, if applicable**
	4. Lock linked subgraph
	5. Generate new version V
	6. Delete or Update tombstone status for link element -> V
	7. Update linked subgraphs version -> V
	8. Commit - unlock element, linked subgraphs

Consistency checks: VertexType

	1. No (not tombstoned) vertexes of VertexType should be attached to a subgraph

Consistency checks: EdgeType

	1. No (not tombstoned) edges of EdgeType should be attached to a subgraph

Consistency checks: Vertex

	1. No (not tombstoned) edges pointing to Vertex should be attached to a subgraph

No checks required for tombstoning/deleting Edge links.

6) Create/update/delete graph element

	1. Lock graph
	2. Generate new version V
	3. Create/update graph element -> V, or delete
	4. Update graph version -> V
	5. Commit - unlock graph

7) Create/update/delete subgraph element

	1. Lock subgraph
	2. Generate new version V
	3. Create/update subgraph element -> V, or delete
	4. Update subgraph version -> V
	5. Commit - unlock subgraph

8) Delete shared element (essentially deleting all subgraph links for the element)

	1. Delete all links (see 5)
	2. Delete element

9) Delete subgraph
	
	1. Lock graph
	2. Lock subgraph
	3. Generate new version V
	4. Delete subgraph and all links
	5. Update graph version -> V
	6. Commit - unlock graph

10) Destroy/Recover graph

	1. Lock graph
	2. Lock all subgraphs
	3. Generate new version V
	4. Update graph destroyed state
	5. Update graph version -> V
	5. Update subgraphs versions -> V
	6. Commit - unlock graph, unlock subgraphs
