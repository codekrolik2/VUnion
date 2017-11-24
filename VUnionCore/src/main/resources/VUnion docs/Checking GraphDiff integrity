This document describes logic implemented in GraphDiffChecker. (com.github.bamirov.vunion.graphcache.GraphDiffChecker)

VUnion operates on multiple assumptions for structuring diffs for streaming that has their roots in how cache data structures/DB structures are organized, DB transactions are processed and diffs are formed on both DB and cache level.
Monotonically increasing and consistent versioning is a key to avoid data corruption and prevent data update race conditions. However, it's not impossible to encounter malformed graph diffs in presense of which system's behaviour would become undefined.
There is variety of ways how diffs could be malformed. The role of GraphDiffChecker class is to detect and identify such problems, providing a meaningful error message tha can be used for debugging.

The following method of GraphDiffChecker is used to check the correctness of graph diff. 

	void sanityCheckGraphDiff(VGraphCache<V, I> cache, VGraphDiff<V, I> graphDiff)

Since diff describes transition of VGraph from one state to another, some data from previous state stored in VGraphCache is used for thorough checking.

Below is description of checks performed when "sanityCheckGraphDiff" is called.

---

1) Diff graph name must match Cache graph name.
Since VGraphCache is a second-level cache that is used to compose VUnionCache, an instance of VGraphCache is created upon reception of corresponding VGraphDiff. Graph name is assigned upon creation.

---

2) Graph Cache version must equal to Diff cache version from.
Since diff describes transition of VGraph from one state to another, "version from" in Diff must match current cache version exactly.

---

3) Graph destruction/recovery checks
3.1. If graph is destroyed, no data should be present in the Diff other than Destroyed record.
As described in [[Graph - GraphVersion update mechanics.md] - 3. Graph updates] - whole graph record is locked at destruction, so there will be no successful concurrent updates. Also, no updates are propagated after destruction, unless a graph is recovered.

In this case GraphDiffChecker is making sure that the following parts of diff don't exist: VertexTypes, Vertexes, EdgeTypes, Edges, Subgraphs, SubgraphSync, GraphElementRecord.

3.2. For the same reason described in [[Graph - GraphVersion update mechanics.md] - 3. Graph updates] Destroyed/Recover version should be > any cache version.
This is true for both destruction and recovery.

GraphDiffChecker calculates maximum update version that exists in GraphCache and compares it to Destroyed/Recover version from diff.

---

4) Graph element update checks
4.1. VGraphElementRecord consistency check
VGraphElementRecord consists of two parts: 

	V graphElementUpdateVersion;
	Optional<VGraph<V, I>> graphElement;

The field graphElement is optional, it's presence means creation/update of graph element, and it's absense means deletion of element. In case graphElement is present it's version should match VGraphElementRecord.graphElementUpdateVersion.

4.2. VGraphElementRecord.graphElementUpdateVersion should be > cache's GraphElement version.

4.3. VGraphElementRecord.graphElementUpdateVersion should be > Cache graph version.

---

5) SubgraphSyncRecord checks
5.1. Diff SubgraphSyncRecord version should be > Cache subgraphSyncVersion

5.2. All active subgraph names should exist either in Cache or in Diff

5.3. All subgraphs that are removed from cache (not exist in SubgraphSyncRecord) should not exist in diff

============================================================

6) Subgraph links check

---

6.1) Diff Subgraph version To should be > corresponding Cache Subgraph version

---

6.2) Subgraph element update checks
6.2.1. Diff subgraph Element version should be equal to VSubgraphElementRecord.getSubgraphElementRecord
Same as for GraphElement, VSubgraphElementRecord consists of two parts: 

	V subgraphElementUpdateVersion;
	Optional<VSubgraphElement<V, I>> subgraphElement;

The field subgraphElement is optional, it's presence means creation/update of subgraph element, and it's absense means deletion of element. In case subgraphElement is present it's version should match VSubgraphElementRecord.subgraphElement.

6.2.2. If Cache subgraph Element exists, its version should be < Diff Subgraph element version

6.2.3. Diff subgraph Element version should be > cache Subgraph version

---

6.3) Links/Shared Elements integrity checks

VLinkDiff has two optional fields, of which at least one should be present: 

	I linkId;
	I linkedElementId;
	Optional<VLinkUpdate<V, I>> linkUpdate;
	Optional<V> linkedElementVersionUpdate;


6.3.1. VLinkDiff / LinkUpdate elementId match check
If linkUpdate is present, linkUpdate.elementId should = VLinkDiff.linkId
The field linkUpdate.elementId exists to align with common Element serialization structure.


6.3.2. LinkedElementVersionUpdate checks

6.3.2.1. If LinkUpdate is present and LinkedElementVersionUpdate is not present - link should exist in cache

6.3.2.2. If LinkUpdate is present and LinkedElementVersionUpdate is not present - Cache link's ElementId should = VLinkDiff.linkId
Note that link.getElementId() for an update in general case doesn't have to equal linkUpdate.getLinkId()
Consider a situation when element is removed from subgraph and then reconnected.
However, in such scenario LinkedElementVersionUpdate will always be present.
If LinkedElementVersionUpdate is not present, the link update must refer to an existing link.

---

6.3.3 Tombstone marks check (linkUpdate.isTombstone)

6.3.3.1. Tombstoned element must exist either in diff or in cache

6.3.3.2. If a tombstoned element is EdgeType:
	No non-tombstoned Edges of this type should exist (diff of cache)

6.3.3.3. If a tombstoned element is VertexType:
	No non-tombstoned Vertexes of this type should exist (diff of cache)

6.3.3.4. If a tombstoned element is EdgeType:
	Both VertexFrom and VertexTo should be tombstoned (diff of cache)

---

6.3.4. If LinkUpdate is present, LinkUpdate.version should be > cache Subgraph version

6.3.5. Either linkUpdate or linkedElementVersionUpdate should be present

6.3.6. If LinkUpdate is not present and LinkedElementVersionUpdate is present - link should exist in cache

---

If LinkedElementVersionUpdate is present, check the following:

6.3.7. If LinkedElementVersionUpdate is present - shared element should exist in diff

6.3.8. If LinkedElementVersionUpdate is present - and shared element exists in cache - cache Element version should be < diff Element version

6.3.9. Extra checks for VVertex and VEdge addition
Find out if element is tombstoned prior to calling findElementInSubgraphDiffOrCache subroutine.
For tombstoned elements tombstoned status of connected elements will be ignored.

6.3.9.1. Vertex addition checks: VertexType should be linked to this subgraph (exist either in cache or diff)
Call findElementInSubgraphDiffOrCache on corresponding VertexType

6.3.9.2. Edge addition checks

EdgeType should be linked to this subgraph (exist either in cache or diff)
Call findElementInSubgraphDiffOrCache on corresponding EdgeType

Both Vertexes should be linked to this subgraph (exist either in cache or diff)
Call findElementInSubgraphDiffOrCache on VertexFrom
Call findElementInSubgraphDiffOrCache on VertexTo

6.3.10 Check linked element's versions to be > subgraph cache versions

6.3.11 Diff's Shared element's version should match with element version in link

---

6.4) Element Sync checks

4.1 Check that removed elements don't exist in diff

4.2. Check that all active elements exist in cache/diff

4.3. Element delete checks

4.3.1. EdgeType removal checks
No Edges of this type should exist (diff or cache, can be deleted by the same diff)

4.3.2. VertexType removal checks
No Vertexes of this type should exist (diff or cache, can be deleted by the same diff)

4.3.3. Edge removal checks
VertexFrom and VertexTo shouldn be deleted (diff or cache, can be deleted by the same diff)

============================================================

7) Subroutine: findElementInSubgraphDiffOrCache
Check elements in Diff and Cache, make sure element exist, is not tombstoned and is of right type.

7.1) If ElementLink exists in Diff

7.1.1. Either LinkUpdate or LinkedElementVersionUpdate should exist (this check is REDUNDANT, just to fail fast)

7.1.2. If LinkUpdate is present and if ignoreTombstoneStatus parameter == false - check that this element is not tombstoned

7.1.3. If LinkedElementVersionUpdate is present - check element in diff
Graph diff should have updated Element, and element should be of a provided type

7.2. Checking cache element: (tombstone checks will be performed if ignoreTombstoneStatus parameter == false)
a. If diff element doesn't exist, Element should exist in cache and not be marked as tombstoned.
b. If diff element exists and has LinkUpdate, but no LinkedElementVersionUpdate - LinkUpdate overrides
	cache link, but we still need to check that both link and element exist in cache, and element is an instance of provided type. 
c. If diff element exists and has LinkedElementVersionUpdate, but no LinkUpdate - LinkedElementVersionUpdate overrides
	cache element, but we still need to check that link exists in cache, and is not tombstoned
d. If diff element exists and has both LinkUpdate and LinkedElementVersionUpdate, cache data is fully overridden.
	That also might mean, that element is new to cache. In this case no checks are required.

7.3. Check that the element is not removed; If SubraphDiff's ElementSync isPresent, elementId should be present in its list of active elements 