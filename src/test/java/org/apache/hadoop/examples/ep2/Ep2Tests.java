package org.apache.hadoop.examples.ep2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class Ep2Tests {
	
	private ExperimentEntryProcessor entries;
	
	@Before
	public void setUp() {		
		entries = new ExperimentEntryProcessor();
	}

	@Test
	public void testGetExperimentEntries() {
		assertEquals( "orch,3,100", entries.getExperimentEntries().get(1349926852787L) );
	}
	
	@Test
	public void testGetParamsWithExactTime() {
		assertEquals("orch,2,100", entries.getParams(1350177735140L) );
	}
	
	@Test
	public void testGetParams() {
		
		assertEquals( "chor,1,100", entries.getParams(1350395751281L) );
		assertEquals( "chor,1,100", entries.getParams(1350395753590L) );
		
		assertEquals( "chor,1,150", entries.getParams(1350395753591L) );
		assertEquals( "chor,1,150", entries.getParams(1350395753592L) );
		assertEquals( "chor,1,150", entries.getParams(1350395756409L) );
		
	}
	
	@Test
	public void testSortedKeyArray() {
		
		for (int i = 0; i < entries.getEntriesKey().size()-2; i++) {
			assertTrue(entries.getEntriesKey().get(i) < entries.getEntriesKey().get(i+1));
		}
 	}

}
