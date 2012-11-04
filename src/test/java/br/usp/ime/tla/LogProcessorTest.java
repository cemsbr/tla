package br.usp.ime.tla;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class LogProcessorTest {

	private LogProcessor entries;

	@Before
	public void setUp() {
		entries = new LogProcessor("src/test/resources/filtered_log");
	}

	@Test
	public void testGetExperimentEntries() {
		assertEquals("orch,3,100",
				entries.getExperimentEntries().get(1349926852787L));
	}

	@Test
	public void testGetParamsWithExactTime() {
		assertEquals("orch,2,100", entries.getParams(1350177735140L));
	}

	@Test
	public void testGetParams() {

		assertEquals("chor,1,100", entries.getParams(1350395751281L));
		assertEquals("chor,1,100", entries.getParams(1350395753590L));

		assertEquals("chor,1,150", entries.getParams(1350395753591L));
		assertEquals("chor,1,150", entries.getParams(1350395753592L));
		assertEquals("chor,1,150", entries.getParams(1350395756409L));

	}

	@Test
	public void testSortedKeyArray() {

		for (int i = 0; i < entries.getEntriesKey().size() - 2; i++) {
			assertTrue(entries.getEntriesKey().get(i) < entries.getEntriesKey()
					.get(i + 1));
		}
	}

	@Test
	public void testFormatDateFromTomcat() {
		String s = "16/Oct/2012:00:00:08";
		Date date = entries.getDateInMillis(s);

		assertEquals("Tue Oct 16 00:00:08 BRT 2012", date.toString());
	}

	@Test
	public void testLogTomcatMatching() {
		assertTrue(entries.matchesTomcatPattern(
				"198.55.32.149 - - [16/Oct/2012:00:00:08 -0700] \"POST /registry/endpoint HTTP/1.1\" 200 590 1"));
		assertTrue(entries.matchesTomcatPattern(
				"198.55.32.149 - - [10/Oct/2012:05:26:28 -0700] \"POST /supermarket3/orchestration HTTP/1.1\" 200 2264 4"));
		assertFalse(entries.matchesTomcatPattern(
				"178.237.128.23 - tomcat [10/Oct/2012:04:12:12 -0700] \"HEAD /manager/html HTTP/1.0\" 200 - 26"));
		assertFalse(entries.matchesTomcatPattern(
				"198.55.32.149 - - [10/Oct/2012:05:26:26 -0700] \"GET /supermarket3/orchestration?wsdl HTTP/1.1\" 200 2171 41"));
		assertFalse(entries.matchesTomcatPattern(
				"178.237.128.23 - - [10/Oct/2012:04:12:12 -0700] \"HEAD /manager/html HTTP/1.0\" 401 - 1"));
	}
	
	@Test
	public void testLogApplicationMatching() {
		assertTrue(entries.matchesApplicationLog("# orch,2,50,1349926584487"));
		assertTrue(entries.matchesApplicationLog("# orch,1,300,1350296418527"));
		assertTrue(entries.matchesApplicationLog("# chor,4,800,1350298025478"));
		assertTrue(entries.matchesApplicationLog("# chor,1,1000,1350190614970"));
		assertFalse(entries.matchesApplicationLog("# warmup"));
	}
}