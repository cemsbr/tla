package br.usp.ime.tla;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
	private final List<Integer> values = new ArrayList<Integer>();
	private double mean = 0;

	public void addValue(final int value) {
		values.add(value);
		updateMean(value);
	}

	private void updateMean(final int value) {
		final double size = values.size();
		mean = mean * ((size - 1) / size) + value / size;
	}

	public double getMean() {
		return mean;
	}

	/**
	 * Uses denominator n-1 like in R
	 * 
	 * @param mean
	 * @return
	 */
	public double getStdDev() {
		double variance = 0;
		for (Integer value : values) {
			variance += ((value - mean) * (value - mean)) / (values.size() - 1);
		}
		return Math.sqrt(variance);
	}

	/**
	 * 
	 * @param stdDev
	 * @return Confidence Interval of 95%
	 */
	public double getCI(final double stdDev) {
		return 1.96 * stdDev / Math.sqrt(values.size());
	}

	/**
	 * 
	 * @return Confidence Interval of 95%
	 */
	public double getCI() {
		return getCI(getStdDev());
	}
}