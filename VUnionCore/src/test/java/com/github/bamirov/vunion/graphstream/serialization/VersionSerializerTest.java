package com.github.bamirov.vunion.graphstream.serialization;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.github.bamirov.vunion.exceptions.MalformedVersionException;
import com.github.bamirov.vunion.graphstream.serialization.longs.LongStringGraphVersionSerializer;
import com.github.bamirov.vunion.version.VGraphVersion;

public class VersionSerializerTest {
	@Test
	public void testVersionSerializer() throws MalformedVersionException {
		LongStringGraphVersionSerializer versionSerializer = new LongStringGraphVersionSerializer();
		
		String versionStr1 = "[]";
		String versionStr2 = "[12]";
		String versionStr3 = "[sg1:14,sg2:16]";
		String versionStr4 = "[12,sg1:14,sg2:16]";
		
		String versionStr5 = " [12,sg1:14,sg2:16] ";
		String versionStr6 = " [12] ";
		String versionStr7 = " [sg1:14,sg2:16] ";
		
		VGraphVersion<Long> version1 = versionSerializer.deserializeGraphVersion(versionStr1);
		VGraphVersion<Long> version2 = versionSerializer.deserializeGraphVersion(versionStr2);
		VGraphVersion<Long> version3 = versionSerializer.deserializeGraphVersion(versionStr3);
		VGraphVersion<Long> version4 = versionSerializer.deserializeGraphVersion(versionStr4);
		
		VGraphVersion<Long> version5 = versionSerializer.deserializeGraphVersion(versionStr5);
		VGraphVersion<Long> version6 = versionSerializer.deserializeGraphVersion(versionStr6);
		VGraphVersion<Long> version7 = versionSerializer.deserializeGraphVersion(versionStr7);
		
		VGraphVersion<Long> version1Tst = VGraphVersion.getEmptyGraphVersion();
		VGraphVersion<Long> version2Tst = VGraphVersion.<Long>builder().graphVersion(Optional.of(12L)).build();
		VGraphVersion<Long> version3Tst = VGraphVersion.<Long>builder().subgraphVersion("sg1", 14L)
				.subgraphVersion("sg2", 16L).build();
		VGraphVersion<Long> version4Tst = VGraphVersion.<Long>builder().subgraphVersion("sg1", 14L)
				.subgraphVersion("sg2", 16L).graphVersion(Optional.of(12L)).build();
		
		assertEquals(version1Tst, version1);
		assertEquals(version2Tst, version2);
		assertEquals(version3Tst, version3);
		assertEquals(version4Tst, version4);
		
		assertEquals(version5, version4Tst);
		assertEquals(version6, version2Tst);
		assertEquals(version7, version3Tst);
		
		VGraphVersion<Long> version1Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version1));
		VGraphVersion<Long> version2Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version2));
		VGraphVersion<Long> version3Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version3));
		VGraphVersion<Long> version4Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version4));
		
		VGraphVersion<Long> version5Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version5));
		VGraphVersion<Long> version6Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version6));
		VGraphVersion<Long> version7Prime = versionSerializer.deserializeGraphVersion(
				versionSerializer.serializeGraphVersion(version7));
		
		assertEquals(version1Prime, version1Prime);
		assertEquals(version2Prime, version2Prime);
		assertEquals(version3Prime, version3Prime);
		assertEquals(version4Prime, version4Prime);
		assertEquals(version5Prime, version5Prime);
		assertEquals(version6Prime, version6Prime);
		assertEquals(version7Prime, version7Prime);
	}
}
