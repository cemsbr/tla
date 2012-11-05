package br.usp.ime.tla;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ExperimentLogParserTest {

	private ExperimentLogParser parser;

	@Before
	public void setUp() throws IOException {
		parser = new ExperimentLogParser("src/test/resources/filtered_log");
	}

	@Test
	public void testLineParsing() {
		assertEquals("orch,3,100", parser.getTypes().get(1349926852787L));
	}

	@Test(expected = ExperimentNotFoundException.class)
	public void testLowerThanLastTime() throws ExperimentNotFoundException {
		assertEquals("chor,1,500", parser.getType(42L));
	}

	@Test
	public void testTimeHigherThanLast() throws ExperimentNotFoundException {
		assertEquals("orch,1,1000", parser.getType(2350422372069L));
	}

	@Test
	public void testParamsByExactTime() throws ExperimentNotFoundException {
		assertEquals("orch,1,50", parser.getType(1349926295888L));
		assertEquals("orch,1,100", parser.getType(1349926297945L));
	}

	@Test
	public void testGetParams() throws ExperimentNotFoundException {
		assertEquals("orch,1,50", parser.getType(1349926295889L));
		assertEquals("orch,1,100", parser.getType(1349926297946L));
		assertEquals("orch,1,150", parser.getType(1349926301360L));
	}

	@Test
	public void testArbitraryStartByExactTime()
			throws ExperimentNotFoundException {
		assertEquals("orch,1,1000", parser.getType(1349926522010L));
	}

	@Test
	public void testArbitraryStart() throws ExperimentNotFoundException {
		assertEquals("orch,1,1000", parser.getType(1349926522011L));
	}

	@Test
	public void testSortedKeyArray() {
		final List<Long> sortedKeys = parser.getSortedTimes();
		for (int i = 0; i < sortedKeys.size() - 2; i++) {
			assertTrue(sortedKeys.get(i) < sortedKeys.get(i + 1));
		}
	}

	@Test
	public void testLineMatching() {
		assertTrue(parser.matchesLogPattern("# orch,2,50,1349926584487"));
		assertTrue(parser.matchesLogPattern("# orch,1,300,1350296418527"));
		assertTrue(parser.matchesLogPattern("# chor,4,800,1350298025478"));
		assertTrue(parser.matchesLogPattern("# chor,1,1000,1350190614970"));
		assertFalse(parser.matchesLogPattern("# warmup"));
	}
}
