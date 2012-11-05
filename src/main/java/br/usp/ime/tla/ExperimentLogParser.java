package br.usp.ime.tla;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperimentLogParser {
	private static final String REGEX = "^# ((orch|chor),\\d+,\\d+),(\\d+)";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private static final Map<Long, String> TYPES = new HashMap<Long, String>();
	private List<Long> sortedTimes;
	private String curType;
	private long start, nextStart;

	public ExperimentLogParser(final String expTypeLog) throws IOException {
		parseLog(expTypeLog);
		setSortedTimes();
		setExperiment(0);
	}

	/*
	 * Creates a key array list with entry map keys to maintain it sorted
	 */
	private void setSortedTimes() {
		final Set<Long> times = TYPES.keySet();
		sortedTimes = new ArrayList<Long>(times);
		Collections.sort(sortedTimes);
	}

	/*
	 * Reads the file entered as the first argument of TomcatLogAnalyzer, and
	 * parses it to ge a Map of entries
	 */
	private void parseLog(final String filename) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		String line;
		while ((line = reader.readLine()) != null) {
			parseLogLine(line);
		}
		reader.close();
	}

	private void parseLogLine(final String line) {
		final Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			final Long time = Long.parseLong(matcher.group(3));
			final String expType = matcher.group(1);
			TYPES.put(time, expType);
		}
	}

	/*
	 * @param string an entry of experiment log
	 * 
	 * @return true if string matches # orch|chor,n,n,long
	 */
	public boolean matchesLogPattern(final String line) {
		return PATTERN.matcher(line).find();
	}

	/*
	 * Returns a string representation of an instance of experiment
	 * 
	 * @param l a long representing a date in millis
	 * 
	 * @return a string like orch,1,100
	 */
	public String getType(final long time) throws ExperimentNotFoundException {
		if (time < sortedTimes.get(0)) {
			throw new ExperimentNotFoundException();
		}

		String type;
		if (time >= start && time < nextStart) {
			type = curType;
		} else {
			type = searchType(time);
		}

		return type;
	}

	private String searchType(final long time) {
		final int startTimeIndex = searchStartTimeIndex(time, 0,
				sortedTimes.size() - 1);
		setExperiment(startTimeIndex);
		final long startTime = sortedTimes.get(startTimeIndex);
		return TYPES.get(startTime);
	}

	private int searchStartTimeIndex(final long time, final int left,
			final int right) {
		if (right - left <= 1) {
			return left;
		}

		final int middle = (left + right) / 2;
		if (time >= sortedTimes.get(middle)) {
			return searchStartTimeIndex(time, middle, right);
		}
		return searchStartTimeIndex(time, left, middle);
	}

	private void setExperiment(final int index) {
		start = sortedTimes.get(index);

		if (index == sortedTimes.size() - 1) {
			nextStart = Integer.MAX_VALUE;
		} else {
			nextStart = sortedTimes.get(index + 1);
		}

		curType = TYPES.get(start);
	}

	/*
	 * Used only to test
	 */
	public Map<Long, String> getTypes() {
		return TYPES;
	}

	/*
	 * Used only to test
	 */
	public List<Long> getSortedTimes() {
		return sortedTimes;
	}
}