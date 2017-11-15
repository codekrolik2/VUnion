package com.github.bamirov.vunion.graphstream.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.github.bamirov.vunion.exceptions.MalformedVersionException;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.serialization.longs.LongJSONGraphDiffSerializer;
import com.github.bamirov.vunion.graphstream.serialization.longs.LongStringGraphVersionSerializer;

public class GraphSerializerTest {
	private GraphDiffCreator graphDiffCreator = new GraphDiffCreator();
	private GraphJSONStrDiffCreator graphJSONStrDiffCreator = new GraphJSONStrDiffCreator();
	
	private LongStringGraphVersionSerializer graphVersionSerializer = new LongStringGraphVersionSerializer();
	private LongJSONGraphDiffSerializer graphDiffSerializer = new LongJSONGraphDiffSerializer(graphVersionSerializer);
	
	private void compareDiff(String diffStr, VGraphDiff<Long, Long> graphDiff) throws JSONException, MalformedVersionException {
		JSONObject diffJSON = new JSONObject(diffStr);
		VGraphDiff<Long, Long> otherDiff = graphDiffSerializer.deserializeGraphDiff(diffJSON);
		assertEquals(graphDiff, otherDiff);
		
		VGraphDiff<Long, Long> otherDiff2 = graphDiffSerializer.deserializeGraphDiff(
				graphDiffSerializer.serializeGraphDiff(graphDiff));
		
		assertEquals(graphDiff, otherDiff2);
	}
	
	@Test
	public void testDiff1() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff1();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff1();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff2() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff2();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff2();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff3() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff3();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff3();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff4() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff4();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff4();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff5() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff5();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff5();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff6() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff6();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff6();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff71() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff71();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff71();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff72() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff72();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff72();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff81() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff81();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff81();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff82() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff82();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff82();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff9() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff9();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff9();
		
		compareDiff(diffStr, graphDiff);
	}
	
	@Test
	public void testDiff10() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff10();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff10();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff11() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff11();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff11();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff13() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff13();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff13();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff14() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff14();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff14();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff15() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff15();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff15();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff161() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff161();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff161();
		
		compareDiff(diffStr, graphDiff);
	}

	@Test
	public void testDiff162() throws JSONException, MalformedVersionException {
		String diffStr = graphJSONStrDiffCreator.createDiff162();
		VGraphDiff<Long, Long> graphDiff = graphDiffCreator.createDiff162();
		
		compareDiff(diffStr, graphDiff);
	}
}
