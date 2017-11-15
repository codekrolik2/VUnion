package com.github.bamirov.vunion.graphstream.serialization;

public class GraphJSONStrDiffCreator {
	public String createDiff1() {
		return "{\n" + 
				"		\"from\" : \"[]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"vertexTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"1\",\n" + 
				"				\"version\" : \"1\",\n" + 
				"				\"key\" : \"vertexTypeKey1\",\n" + 
				"				\"content\" : \"<sample vertex type>\",\n" + 
				"				\n" + 
				"				\"vertexTypeName\" : \"vertexType0\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"vertexes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"2\",\n" + 
				"				\"version\" : \"2\",\n" + 
				"				\"key\" : \"vertexKey1\",\n" + 
				"				\"content\" : \"<sample vertex content>\",\n" + 
				"				\n" + 
				"				\"vertexTypeId\" : \"1\"\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"elementId\" : \"5\",\n" + 
				"				\"version\" : \"5\",\n" + 
				"				\"key\" : \"vertexKey2\",\n" + 
				"				\"content\" : \"<sample vertex content>\",\n" + 
				"				\n" + 
				"				\"vertexTypeId\" : \"1\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" :  \"6\",\n" + 
				"				\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"3\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"3\",\n" + 
				"							\"key\" : \"linkKey1\",\n" + 
				"							\"version\" : \"3\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"1\",\n" + 
				"							\"linkedElementVersion\" : \"1\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"4\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"4\",\n" + 
				"							\"key\" : \"linkKey2\",\n" + 
				"							\"version\" : \"4\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"2\",\n" + 
				"							\"linkedElementVersion\" : \"2\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"6\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"6\",\n" + 
				"							\"key\" : \"linkKey3\",\n" + 
				"							\"version\" : \"6\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"5\",\n" + 
				"							\"linkedElementVersion\" : \"5\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff2() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:6]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"edgeTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"7\",\n" + 
				"				\"version\" : \"7\",\n" + 
				"				\"key\" : \"edgeTypeKey1\",\n" + 
				"				\"content\" : \"<sample edge type>\",\n" + 
				"				\n" + 
				"				\"edgeTypeName\" : \"edgeType0\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"edges\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"8\",\n" + 
				"				\"version\" : \"8\",\n" + 
				"	\n" + 
				"				\"key\" : \"edgeKey1\",\n" + 
				"				\"content\" : \"<sample edge content>\",\n" + 
				"				\n" + 
				"				\"edgeTypeId\" : \"7\",\n" + 
				"				\"vertexFromId\" : \"2\",\n" + 
				"				\"vertexToId\" : \"5\",\n" + 
				"				\"isDirected\" : false\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"10\",\n" + 
				"				 \n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"9\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"9\",\n" + 
				"							\"key\" : \"linkKey4\",\n" + 
				"							\"version\" : \"9\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"7\",\n" + 
				"							\"linkedElementVersion\" : \"7\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"10\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"10\",\n" + 
				"							\"key\" : \"linkKey5\",\n" + 
				"							\"version\" : \"10\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"8\",\n" + 
				"							\"linkedElementVersion\" : \"8\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff3() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:10]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"vertexTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"1\",\n" + 
				"				\"version\" : \"1\",\n" + 
				"				\"key\" : \"vertexTypeKey1\",\n" + 
				"				\"content\" : \"<sample vertex type>\",\n" + 
				"				\n" + 
				"				\"vertexTypeName\" : \"vertexType0\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"vertexes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"2\",\n" + 
				"				\"version\" : \"2\",\n" + 
				"				\"key\" : \"vertexKey1\",\n" + 
				"				\"content\" : \"<sample vertex content>\",\n" + 
				"				\n" + 
				"				\"vertexTypeId\" : \"1\"\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"elementId\" : \"5\",\n" + 
				"				\"version\" : \"5\",\n" + 
				"				\"key\" : \"vertexKey2\",\n" + 
				"				\"content\" : \"<sample vertex content>\",\n" + 
				"				\n" + 
				"				\"vertexTypeId\" : \"1\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"edgeTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"7\",\n" + 
				"				\"version\" : \"7\",\n" + 
				"				\"key\" : \"edgeTypeKey1\",\n" + 
				"				\"content\" : \"<sample edge type>\",\n" + 
				"				\n" + 
				"				\"edgeTypeName\" : \"edgeType0\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"edges\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"8\",\n" + 
				"				\"version\" : \"8\",\n" + 
				"	\n" + 
				"				\"key\" : \"edgeKey1\",\n" + 
				"				\"content\" : \"<sample edge content>\",\n" + 
				"				\n" + 
				"				\"edgeTypeId\" : \"7\",\n" + 
				"				\"vertexFromId\" : \"2\",\n" + 
				"				\"vertexToId\" : \"5\",\n" + 
				"				\"isDirected\" : false\n" + 
				"			}\n" + 
				"		],\n" + 
				"\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"15\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"11\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"11\",\n" + 
				"							\"key\" : \"linkKey6\",\n" + 
				"							\"version\" : \"11\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"1\",\n" + 
				"							\"linkedElementVersion\" : \"1\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"12\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"12\",\n" + 
				"							\"key\" : \"linkKey7\",\n" + 
				"							\"version\" : \"12\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"2\",\n" + 
				"							\"linkedElementVersion\" : \"2\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"13\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"13\",\n" + 
				"							\"key\" : \"linkKey8\",\n" + 
				"							\"version\" : \"13\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"5\",\n" + 
				"							\"linkedElementVersion\" : \"5\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"14\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"14\",\n" + 
				"							\"key\" : \"linkKey9\",\n" + 
				"							\"version\" : \"14\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"7\",\n" + 
				"							\"linkedElementVersion\" : \"7\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"					{\n" + 
				"						\"linkId\" : \"15\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"15\",\n" + 
				"							\"key\" : \"linkKey10\",\n" + 
				"							\"version\" : \"15\",\n" + 
				"							\"content\" : \"<sample link content>\",\n" + 
				"							\"isTombstone\" : false\n" + 
				"						},\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"8\",\n" + 
				"							\"linkedElementVersion\" : \"8\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}

	public String createDiff4() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:10,subgraph1:15]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"vertexes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"2\",\n" + 
				"				\"version\" : \"16\",\n" + 
				"				\"key\" : \"vertexKey1-1\",\n" + 
				"				\"content\" : \"<sample vertex content-1>\",\n" + 
				"				\n" + 
				"				\"vertexTypeId\" : \"1\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"16\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"4\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"2\",\n" + 
				"							\"linkedElementVersion\" : \"16\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"16\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"12\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"2\",\n" + 
				"							\"linkedElementVersion\" : \"16\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}

	public String createDiff5() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:16,subgraph1:16]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"edges\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"8\",\n" + 
				"				\"version\" : \"17\",\n" + 
				"	\n" + 
				"				\"key\" : \"edgeKey1-1\",\n" + 
				"				\"content\" : \"<sample edge content-1>\",\n" + 
				"				\n" + 
				"				\"edgeTypeId\" : \"7\",\n" + 
				"				\"vertexFromId\" : \"2\",\n" + 
				"				\"vertexToId\" : \"5\",\n" + 
				"				\"isDirected\" : true\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"17\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"10\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"8\",\n" + 
				"							\"linkedElementVersion\" : \"17\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"17\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"15\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"8\",\n" + 
				"							\"linkedElementVersion\" : \"17\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}

	public String createDiff6() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:17,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"18\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"10\",\n" + 
				"						\"linkUpdate\" : {\n" + 
				"							\"elementId\" : \"10\",\n" + 
				"							\"key\" : \"linkKey5-1\",\n" + 
				"							\"version\" : \"18\",\n" + 
				"							\"content\" : \"<sample link content-1>\",\n" + 
				"							\"isTombstone\" : true\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff71() {
		return "{\n" + 
				"		\"from\" : \"[subgraph0:18,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"graphElementRecord\" : {\n" + 
				"			\"graphElementUpdateVersion\" : \"19\",\n" + 
				"			\"graphElement\" : {\n" + 
				"				\"elementId\" : \"16\",\n" + 
				"				\"version\" : \"19\",\n" + 
				"\n" + 
				"				\"key\" : \"graphElementKey1\",\n" + 
				"				\"content\" : \"<sample graph element content>\"\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}";
	}
	
	public String createDiff72() {
		return "{\n" + 
				"		\"from\" : \"[19,subgraph0:18,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"graphElementRecord\" : {\n" + 
				"			\"graphElementUpdateVersion\" : \"20\",\n" + 
				"			\"graphElement\" : {\n" + 
				"				\"elementId\" : \"16\",\n" + 
				"				\"version\" : \"20\",\n" + 
				"\n" + 
				"				\"key\" : \"graphElementKey1-1\",\n" + 
				"				\"content\" : \"<sample graph element content-1>\"\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}";
	}
	
	public String createDiff81() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:18,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"21\",\n" + 
				"			\n" + 
				"				\"subgraphElementRecord\" : {\n" + 
				"					\"subgraphElementUpdateVersion\" : \"21\",\n" + 
				"					\"subgraphElement\" : {\n" + 
				"						\"elementId\" : \"17\",\n" + 
				"						\"version\" : \"21\",\n" + 
				"\n" + 
				"						\"key\" : \"subgraphElementKey1\",\n" + 
				"						\"content\" : \"<sample subgraph element content>\"\n" + 
				"					}\n" + 
				"				}\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff82() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:21,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"22\",\n" + 
				"			\n" + 
				"				\"subgraphElementRecord\" : {\n" + 
				"					\"subgraphElementUpdateVersion\" : \"22\",\n" + 
				"					\"subgraphElement\" : {\n" + 
				"						\"elementId\" : \"17\",\n" + 
				"						\"version\" : \"22\",\n" + 
				"\n" + 
				"						\"key\" : \"subgraphElementKey1-1\",\n" + 
				"						\"content\" : \"<sample subgraph element content-1>\"\n" + 
				"					}\n" + 
				"				}\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}

	public String createDiff9() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:22,subgraph1:17]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"vertexTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"1\",\n" + 
				"				\"version\" : \"23\",\n" + 
				"				\"key\" : \"vertexTypeKey1-1\",\n" + 
				"				\"content\" : \"<sample vertex type-1>\",\n" + 
				"				\n" + 
				"				\"vertexTypeName\" : \"vertexType0-1\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"23\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"3\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"1\",\n" + 
				"							\"linkedElementVersion\" : \"23\"\n" + 
				"						}\n" + 
				"					},\n" + 
				"				]\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"23\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"11\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"1\",\n" + 
				"							\"linkedElementVersion\" : \"23\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			},\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff10() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:23,subgraph1:23]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"edgeTypes\" : [\n" + 
				"			{\n" + 
				"				\"elementId\" : \"7\",\n" + 
				"				\"version\" : \"24\",\n" + 
				"				\"key\" : \"edgeTypeKey1-1\",\n" + 
				"				\"content\" : \"<sample edge type-1>\",\n" + 
				"				\n" + 
				"				\"edgeTypeName\" : \"edgeType0-1\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"24\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"9\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"7\",\n" + 
				"							\"linkedElementVersion\" : \"24\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			},\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"24\",\n" + 
				"			\n" + 
				"				\"linkUpdates\" : [\n" + 
				"					{\n" + 
				"						\"linkId\" : \"14\",\n" + 
				"						\"linkedElementUpdate\" : {\n" + 
				"							\"linkedElementId\" : \"7\",\n" + 
				"							\"linkedElementVersion\" : \"24\"\n" + 
				"						}\n" + 
				"					}\n" + 
				"				]\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}

	public String createDiff11() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:24,subgraph1:24]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph1\",\n" + 
				"				\"subgraphVersionTo\" : \"25\",\n" + 
				"			\n" + 
				"				\"elementSync\" : {\n" + 
				"					\"elementSyncVersion\" : \"25\",\n" + 
				"					\"elementIds\" : [ \"11\", \"12\", \"13\", \"14\" ]\n" + 
				"				}\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff13() {
		return "{\n" + 
				"		\"from\" : \"[20,subgraph0:24,subgraph1:25]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"subgraphSync\" : {\n" + 
				"			\"subgraphSyncVersion\" : \"26\",\n" + 
				"			\"subgraphNames\" : [ \"subgraph0\" ]\n" + 
				"		}\n" + 
				"	}";
	}
	
	public String createDiff14() {
		return "{\n" + 
				"		\"from\" : \"[26,subgraph0:24]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"graphElementRecord\" : {\n" + 
				"			\"graphElementUpdateVersion\" : \"27\"\n" + 
				"		}\n" + 
				"	}";
	}
	
	public String createDiff15() {
		return "{\n" + 
				"		\"from\" : \"[27,subgraph0:24]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"subgraphs\" : [\n" + 
				"			{\n" + 
				"				\"name\" : \"subgraph0\",\n" + 
				"				\"subgraphVersionTo\" : \"28\",\n" + 
				"				\n" + 
				"				\"subgraphElementRecord\" : {\n" + 
				"					\"subgraphElementUpdateVersion\" : \"28\"\n" + 
				"				}\n" + 
				"			}\n" + 
				"		]\n" + 
				"	}";
	}
	
	public String createDiff161() {
		return "{\n" + 
				"		\"from\" : \"[27,subgraph0:28]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"destroyedRecord\" : {\n" + 
				"			\"destroyRecoverVersion\" : \"29\",\n" + 
				"			\"isDestroyed\" : true\n" + 
				"		}\n" + 
				"	}";
	}
	
	public String createDiff162() {
		//TODO: +the entire graph contents, as if requested (from == [])
		return "{\n" + 
				"		\"from\" : \"[29]\",\n" + 
				"		\"graphName\" : \"graph0\",\n" + 
				"		\n" + 
				"		\"destroyedRecord\" : {\n" + 
				"			\"destroyRecoverVersion\" : \"30\",\n" + 
				"			\"isDestroyed\" : false\n" + 
				"		},\n" + 
				"	}";
	}
}
