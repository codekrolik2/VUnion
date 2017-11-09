TODO: consider making "subgraphs.last_version" calculated - problem : lost updates


Graph / GraphVersion update mechanics.

---

1) GraphVersion and its representation in DB

Let's describe version update mechanics in DB for different updates. Please note, that more than one different types of content update may result in the same exact action for version update. Another thing to keep in mind is that since we have MySQL Galera cluster as our database, there are two layers of transaction - one is the behaviour a standalone MySQL server would have, another is Galera transaction resolution on the network level.
Althought MySQL Galera Cluster seems to be the primary DB option for our graphs, it would be limiting for proxying to completely exclude the persistence option on regular standalone SQL databases, like typical MySQL without replication or SQLite. In this case Galera replication layer will be nonexistent, and relying just on Galera behaviours and properties to guarantee data consistency wouldn't make much sense.
For that reason we will review two layers of replication separately and design our algorithms in such a way that they'd guarantee Graph consistency for concurrent updates in both layers of replication.

Graph version consists of a single Graph update version and 0 or more Subgraph update versions.

	[<graphV>,subgraph1:<subgraph1V>,subgraph2:<subgraph2V>]

In DB graph versions are represented as three different fields in graph records:
1. graphs.subgraph_delete_version
	 - last time a subgraph was deleted
2. graphs.destroy_revive_version
	 - last time a graph was destroyed or revived
3. graphs.graph_element_update_version
	 - last time a graph element was created/updated/deleted
	
Subgraph versions are represented as three different fields in subgraph records:
1. subgraphs.last_version
	- last time subgraph links or linked shared elements (vertexes, edges, vertex types, edge types) were updated
2. subgraphs.last_delete_version
	- last time a subgraph link was deleted
3. subgraphs.subgraph_element_update_version
	- last time a subgraph element was created/updated/deleted

Since all those versions are retrieved from the same version generator, producing unique monotonically increasing sequence per Graph, both Graph update version and Subgraph update versions in GraphVersion are simply calculated as maximum version out of three applicable versions.

---

2) Subgraph updates

Subgraph updates come in 3 different forms: subgraph links or linked elements update, subgraph link deletion and subgraph element update. All three updates can be viewed as local to subgraph, eve though shared elements updates result in update of version of all subgraphs they're linked to, because overall subgraph version update doesn't mean any updates in any other versions. If other subgraphs are affected, their versions will be updated separately.

In this case what needs to be done is to ensure that monotonically increasing sequence of version generator is properly projected to subgraph version sequence (which must be a subsequence of version generator sequence).

2.1. Subgraph-level race conditions

- Local transaction level: 
Locking a subraph record with "SELECT ... FOR UPDATE" prior to version generation should be enough to ensure correctness of sequence on local level.

- Galera transaction level: 
In case two concurrent transactions will attempt to update the same subgraph record (DB row), only one of them will succeed, since a certification failure happens when two threads writing to two different nodes in a Galera cluster attempt to UPDATE the same set of rows in the same table during the same time interval. That guarantees that all subgraph version updates will happen in sequence that we project via transaction boundaries.

Updates to different subgraphs, however, may happen in arbitrary order, but what's important is that version of a subgraph will only grow monotonically and won't suddenly jump back because of some race conditions.

2.2. Element-level race conditions

Let's consider an element-level race condition: consider a shared element ELM1 (v25), attached to subgraphs SG1 and SG2.
Thread1 is updating a shared element ELM1, while Thread2 is linking ELM2 to a subgraph SG3.

Thread1 [1] : Lock ELM1
Thread1 [2] : Find linked subgraphs (SG1, SG2)
Thread1 [3] : Lock SG1, SG2
Thread1 [4] : Update ELM1 -> v30
Thread1 [5] : Update SG1.last_version -> v30
Thread1 [6] : Update SG2.last_version -> v30
	
Thread2 [1] : Lock SG3
Thread2 [2] : Lock ELM1 & Read ELM1 version - v25
Thread2 [3] : Create link LNK1 (to SG3) -> v27
Thread2 [4] : Update SG3.last_version -> v27

So the end result will be as follows:
ELM1 v30
SG1 v30
SG2 v30 
SG3 v27,
a clear discrepancy between SG3 version and its content.

It might seem, that Thread1 [1] and Thread2 [2] both locking ELM1 will make impossible for such race condition to happen, however, note how those transaction don't share any rows that they modify. SELECT ... FOR UPDATE lock will work only for transactions running on the same server, but in case of Galera replication this command won't span multiple nodes, so it's effectively nonexistent.

This can be addressed in the following way:
Let's consider action of linking a shared element to a subgraph - element update. In this case we will run null-update that would update shared element's version, and on Galera transaction level this will be enough to avoid race conditions.
The side effect of this approach is that it will result in updating of versions of all subgraphs this shared element was previously attached to, even though other subgraphs won't recieve any meaningful content update.

Thread2 in this case will work as follows:

Thread2 [1] : Lock ELM1
Thread2 [2] : Find linked subgraphs (SG1, SG2)
Thread2 [3] : Lock SG1, SG2
Thread1 [4] : Update ELM1 -> v27
Thread2 [5] : Update SG1.last_version -> v27
Thread2 [6] : Update SG2.last_version -> v27
Thread2 [7] : Create link LNK1 (to SG3) -> v28
Thread2 [8] : Update SG3.last_version -> v28

Race condition that can be caused by deleting a link and updating a linked element is less dangerous, worst-case scenario is that subgraph that is deleting an element will receive a redundant version update.

3) Graph updates
Subgraph updates come in 3 different forms: subgraph deletion, graph destruction/revival and graph element update.

Only graph element updates are local to Graph, both other types of updates are connected to altering subgraph structure.
In case of destruction/revival it may not necessarily be physical destruction of DB records, which can still be kept for history or Graph recovery purposes, but for stream/caching it's physical destruction of all contents, except for Graph record itself.

Similarly to subgraphs, race conditions between graph altering operations on graph level are handled by graph row locking (SELECT ... FOR UPDATE)  on local transaction level, and by the fact that the same graph record will be updated on Galera transaction level.
Since there is only one graph in a Graph Diff/Source DB, there will be no Subgraph-level race conditions pertaining to graph operations. However, let's review how graph-level updates work with subgraph-level updates.

3.1. Subgraph deletion
Deleting a subgraph is connected to actual deletion or subgraph record and all its links from DB (alternatively - marking that record as deleted).
Deletion will conflict with other updates on both local transaction level and Galera transaction level, so there will be no race conditions.

Alternatively - subgraph record could be just marked as deleted.
For local transaction level it should be enough to lock the record of a subgraph and check for deleted flag, if it's used in the schema. On Galera transaction level concurrent update will also create conflict.

3.2. Graph destruction and recovery.
Destruction of a graph essentially means all its contents become invisible to stream clients and proxies.
That doesn't mean all contents will be deleted from the database, because recovery is an option. 
To denote the destroyed state of a graph, field graphs.destroyed is set.
If graph is in destroyed state, it's diff is reduced to 

	VGraphDestroyedRecord<V> {
		V destroyRecoverVersion;
		boolean isDestroyed;
	}

GraphVersion is reduced to graph update version as well, all subgraph versions are removed from it.
Those changes, however, only exist from stream perspective. DB will actually keep all the graph contents, moreover, updates can be made to those contents the same way as if a graph was in normal state. Those updates will be retrieved and streamed if/whenever the graph will be recovered.
Since destruction is a reversible operation, it's important to preserve version consistency even in presence of destruction/recovery events. So it's important to make sure no client will get a version of subgraph larger than the destruction version of graph itself. The same applies to graph recovery process.

The simplest way to achieve that would be to lock all subgraphs and update all their versions to destruction version, to effectively eliminate the possibility of concurrent version change, that would give a certain subgraph a version higher than destruction version prior to finalization of destruction transaction, because that race condition might mean some of the clients would get such version, meaning of which is not defined (see GraphVersion comparison below).

In this way, destruction version defines a boundary which guarantees that updates that happen after it won't be sent to any client, unless the graph is recovered.

3.3. GraphVersion comparison

Theoretically, since GraphVersions are composite and the order of version updates is not necessarily defined by order of events, there is no clear notion of simple comparison between two versions.

a. Let's consider the following versions: V1=[19,SG1:25,SG2:30] and V2=[SG1:35,SG2:20]. There is no reversible notion of "greater than" or "less than", because Graph update version and SG2 version are larger in the V1, however SG1 version is larger in V2.
Therefore it's more suitable to use a notion of "has updates since".
In this example V1 has updates since V2, as well as V2 has updates since V1.

b. However, in the following example: V1=[19,SG1:25,SG2:30] and V2=[19,SG1:25]
V2 has updates since V1, but V1 doesn't have updates since V2.

c. Also, when versions are equal: V1=[19,SG1:25] and V2=[19,SG1:25]
neither V2 has updates since V1, nor V1 has updates since V2.

c. Graph version by itself also provides information about possible updates: V1=[19,SG1:25] and V2=[15,SG1:25]
In this example V1 has updates since V2, but V2 doesn't have updates since V1.

Let's review more complex examples, connected to deletion of subgraphs and graph destruction.
All the examples assume both versions belonging to the same graph, using the same stream filter.
Disclaimer: This is just a draft and the following info might change.

d. V1=[20], V2=[15,SG1=16,SG2=17]
In this case V1 doesn't have updates since V2, while V2 has updates since V1.
That comes from the notion of Graph update version referring to graph destruction / subgraph deletion.
The fact V1's Graph update version is larger than V2's together with absense of SG1 and SG2 versions from V1 means either that the graph was destroyed, or that both subgraphs SG1 and SG2 were deleted.

e. V1=[20,SG1=16], V2=[15,SG1=16,SG2=17]
In this case V1 doesn't have updates since V2, while V2 has updates since V1.
Same logic as in (d), except in this case it's clearly deletion of SG2 (because a destroyed graph doesn't show any subgraph versions)

f. V1=[20], V2=[15,SG1=21]
In this case V1 has updates since V2, as well as V2 has updates since V1.
Just as before, V1's graph update version = 20 means that before version 20 subgraphs might've been deleted, however SG1 version is 21, which means that whatever updates SG1 has, occured after 20 and therefore missing from V1, since it doesn't have a corresponding version record to SG1.
