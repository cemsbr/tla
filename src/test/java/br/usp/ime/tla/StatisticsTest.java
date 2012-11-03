package br.usp.ime.tla;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class StatisticsTest {
	private Statistics stats;
	private final static double EPS = 0.001;

	@Before
	public void setStatistics() {
		stats = new Statistics();
	}

	@Test
	public void testMean() {
		addAndAssert(0, 0);
		addAndAssert(2, 1);
		addAndAssert(3, 1.666);
	}

	private void addAndAssert(final int value, final double expected) {
		stats.addValue(value);
		assertEquals(expected, stats.getMean(), EPS);
	}

	@Test
	public void testStandardDeviation() {
		final int[] values = { 2, 4, 4, 4, 5, 5, 7, 9 };
		addValues(values);

		assertEquals(2.13809, stats.getStdDev(), 0.00001);
	}

	private void addValues(final int[] values) {
		for (int value : values) {
			stats.addValue(value);
		}
	}

	@Test
	public void testConfidenceInterval() {
		final int[] values = { 2, 4, 4, 4, 5, 5, 7, 9 };
		addValues(values);

		final double expected = 1.96 * stats.getStdDev() / Math.sqrt(8);
		assertEquals(expected, stats.getCI(), 0.000001);
	}

	@Test
	public void testMeanOverflow() {
		stats.addValue(Integer.MAX_VALUE - 1);
		stats.addValue(Integer.MAX_VALUE - 1);
		stats.addValue(Integer.MAX_VALUE - 1);

		assertEquals(Integer.MAX_VALUE - 1, stats.getMean(), 0.000001);
	}
}