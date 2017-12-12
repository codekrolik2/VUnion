package com.github.bamirov.vunion.graphcache;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.bamirov.vunion.exceptions.GraphMismatchException;
import com.github.bamirov.vunion.graphstream.VGraphDiff;
import com.github.bamirov.vunion.graphstream.serialization.GraphDiffCreator;
import com.github.bamirov.vunion.version.VGraphVersion;

//TODO: test internal cache collections state and refcount after each update
public class CacheTest {
	private static GraphDiffCreator graphDiffCreator;
	private static VGraphCache<Long, Long> graphCache;
	
	@BeforeClass
	public static void init() {
		graphDiffCreator = new GraphDiffCreator();
		graphCache = new VGraphCache<>(GraphDiffCreator.graphName, true);
	}
	
	private void testDiff(VGraphDiff<Long, Long> graphDiff) throws GraphMismatchException {
		VGraphVersion<Long> version = graphCache.getGraphVersion();
		
		graphCache.applyGraphDiff(graphDiff);
		VGraphDiff<Long, Long> cacheDiff = graphCache.getGraphDiff(version);
		
		assertEquals(graphDiff, cacheDiff);
	}
	
	@Test
	public void testDiff010() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff1());
	}
	
	@Test
	public void testDiff020() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff2());
	}
	
	@Test
	public void testDiff030() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff3());
	}
	
	@Test
	public void testDiff040() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff4());
	}
	
	@Test
	public void testDiff050() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff5());
	}
	
	@Test
	public void testDiff060() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff6());
	}
	
	@Test
	public void testDiff071() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff71());
	}
	
	@Test
	public void testDiff072() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff72());
	}

	@Test
	public void testDiff081() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff81());
	}

	@Test
	public void testDiff082() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff82());
	}

	@Test
	public void testDiff090() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff9());
	}
	
	@Test
	public void testDiff100() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff10());
	}

	@Test
	public void testDiff110() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff11());
	}

	@Test
	public void testDiff130() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff13());
	}

	@Test
	public void testDiff140() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff14());
	}

	@Test
	public void testDiff150() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff15());
	}

	@Test
	public void testDiff161() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff161());
	}

	@Test
	public void testDiff162() throws GraphMismatchException {
		testDiff(graphDiffCreator.createDiff162());
	}
}
