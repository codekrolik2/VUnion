package com.github.bamirov.vunion.graphstream.serialization;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Optional;

import com.github.bamirov.vunion.version.VGraphVersion;
import com.github.bamirov.vunion.version.VGraphVersionComparator;

public class VersionComparatorTest {
	private VGraphVersionComparator<Long> comparator = new VGraphVersionComparator<Long>();
	
	/*
		
		VGraphVersion<Long> version1Tst = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2Tst = VGraphVersion.<Long>builder().graphVersion(Optional.of(12L)).build();
		VGraphVersion<Long> version3Tst = VGraphVersion.<Long>builder().subgraphVersion("sg1", 14L)
				.subgraphVersion("sg2", 16L).build();
		VGraphVersion<Long> version4Tst = VGraphVersion.<Long>builder().subgraphVersion("sg1", 14L)
				.subgraphVersion("sg2", 16L).graphVersion(Optional.of(12L)).build();
		
	*/
	
	@Test
	public void test1() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.subgraphVersion("sg1", 25L)
				.subgraphVersion("sg2", 30L)
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.subgraphVersion("sg1", 35L)
				.subgraphVersion("sg2", 20L)
				.build();
		
		assertTrue(comparator.hasUpdatesSince(version1, version2));
		assertTrue(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test2() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(19L))
				.subgraphVersion("sg1", 25L)
				.subgraphVersion("sg2", 30L)
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(19L))
				.subgraphVersion("sg1", 25L)
				.build();
		
		assertTrue(comparator.hasUpdatesSince(version1, version2));
		assertFalse(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test3() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(19L))
				.subgraphVersion("sg1", 25L)
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(19L))
				.subgraphVersion("sg1", 25L)
				.build();
		
		assertFalse(comparator.hasUpdatesSince(version1, version2));
		assertFalse(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test4() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(20L))
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(15L))
				.subgraphVersion("sg1", 16L)
				.subgraphVersion("sg2", 17L)
				.build();
		
		assertTrue(comparator.hasUpdatesSince(version1, version2));
		assertFalse(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test5() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(20L))
				.subgraphVersion("sg1", 16L)
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(15L))
				.subgraphVersion("sg1", 16L)
				.subgraphVersion("sg2", 17L)
				.build();
		
		assertTrue(comparator.hasUpdatesSince(version1, version2));
		assertFalse(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test6() {
		VGraphVersion<Long> version1 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(20L))
				.build();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(15L))
				.subgraphVersion("sg1", 21L)
				.build();
		
		assertTrue(comparator.hasUpdatesSince(version1, version2));
		assertTrue(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test7() {
		VGraphVersion<Long> version1 = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2 = VGraphVersion.getEmptyGraphVersion();
		
		assertFalse(comparator.hasUpdatesSince(version1, version2));
		assertFalse(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test8() {
		VGraphVersion<Long> version1 = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(15L))
				.build();
		
		assertFalse(comparator.hasUpdatesSince(version1, version2));
		assertTrue(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test9() {
		VGraphVersion<Long> version1 = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.subgraphVersion("sg1", 21L)
				.build();
		
		assertFalse(comparator.hasUpdatesSince(version1, version2));
		assertTrue(comparator.hasUpdatesSince(version2, version1));
	}
	
	@Test
	public void test10() {
		VGraphVersion<Long> version1 = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2 = VGraphVersion.<Long>builder()
				.graphVersion(Optional.of(15L))
				.subgraphVersion("sg1", 21L)
				.build();
		
		assertFalse(comparator.hasUpdatesSince(version1, version2));
		assertTrue(comparator.hasUpdatesSince(version2, version1));
	}
}
