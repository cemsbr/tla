package br.usp.ime.tla;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
	private final List<Integer> values = new ArrayList<Integer>();
	private double mean = 0;

	public void addValue(final int value) {
		updateMean(value);
		values.add(value);
	}

	private void updateMean(final int value) {
		final double size = values.size();
		// TODO Throw/catch values.size() == Integer.MAX_VALUE
		mean = mean * (size / (size + 1)) + value / (size + 1);
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
		double squaredSum = 0;
		for (Integer value : values) {
			squaredSum += ((value - mean) * (value - mean));
		}
		final double variance = squaredSum / (values.size() - 1);
		final double stdDev = Math.sqrt(variance);

		return stdDev;
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