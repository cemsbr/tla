package br.usp.ime.tla;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

public class TomcatLogParserTest {
	private static final TomcatLogParser PARSER = new TomcatLogParser();
	private static final String LINE = "198.55.33.11 - - [09/Oct/2012:02:47:08 -0700] \"POST /registry/endpoint HTTP/1.1\" 200 261 1";
	private static final String LINE2 = "198.55.33.13 - - [10/Oct/2012:05:26:28 -0700] \"POST /portal1/orchestration HTTP/1.1\" 200 736 9";

	@Test
	public void testLineMatching() {
		assertTrue(PARSER
				.setLine("198.55.32.149 - - [16/Oct/2012:00:00:08 -0700] \"POST /registry/endpoint HTTP/1.1\" 200 590 1"));
		assertTrue(PARSER
				.setLine("198.55.32.149 - - [10/Oct/2012:05:26:28 -0700] \"POST /supermarket3/orchestration HTTP/1.1\" 200 2264 4"));
		assertFalse(PARSER
				.setLine("178.237.128.23 - tomcat [10/Oct/2012:04:12:12 -0700] \"HEAD /manager/html HTTP/1.0\" 200 - 26"));
		assertFalse(PARSER
				.setLine("198.55.32.149 - - [10/Oct/2012:05:26:26 -0700] \"GET /supermarket3/orchestration?wsdl HTTP/1.1\" 200 2171 41"));
		assertFalse(PARSER
				.setLine("178.237.128.23 - - [10/Oct/2012:04:12:12 -0700] \"HEAD /manager/html HTTP/1.0\" 401 - 1"));
	}

	@Test
	public void testDate() throws ParseException {
		PARSER.setLine(LINE);
		final Date actual = PARSER.getDate();
		assertEquals("Tue Oct 09 02:47:08 BRT 2012", actual.toString());

		PARSER.setLine(LINE2);
		final Date actual2 = PARSER.getDate();
		assertEquals("Wed Oct 10 05:26:28 BRT 2012", actual2.toString());
	}

	@Test
	public void testResponseTime() {
		PARSER.setLine(LINE);
		assertEquals(1, PARSER.getResponseTime());

		PARSER.setLine(LINE2);
		assertEquals(9, PARSER.getResponseTime());
	}

	@Test
	public void testServiceName() {
		PARSER.setLine(LINE);
		assertEquals("registry", PARSER.getServiceName());

		PARSER.setLine(LINE2);
		assertEquals("portal1", PARSER.getServiceName());
	}
}