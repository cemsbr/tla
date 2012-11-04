package br.usp.ime.tla;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogProcessor {

	private static final String EXP_TYPE_REGEX = "^# ((orch|chor),\\d+,\\d+),(\\d+)";
	private static final String TOMCAT_REGEX = "^(\\d{1,3}\\.){3}\\d{1,3} - - \\[(\\d){2}/(\\w){3}/(\\d){4}(:\\d{2}){3} -\\d{4}\\] "
			+ "\"POST (.+) (HTTP/1\\.1)\"( \\d+){3}";

	private static final Pattern EXP_PAT = Pattern.compile(EXP_TYPE_REGEX);
	private static final Pattern TOMCAT_PAT = Pattern.compile(TOMCAT_REGEX);

	private static final Map<Long, String> EXP_TYPES = new HashMap<Long, String>();
	private List<Long> sortedExpTimes;

	private final SimpleDateFormat tomcatDateF = new SimpleDateFormat(
			"dd/MMM/yyyy:HH:mm:ss");

	public LogProcessor(final String expTypeLog) throws IOException {
		parseExperimentLog(expTypeLog);
		setExperimentData();
	}

	/*
	 * Creates a key array list with entry map keys to maintain it sorted
	 */
	private void setExperimentData() {
		final Set<Long> times = EXP_TYPES.keySet();
		sortedExpTimes = new ArrayList<Long>(times);
		Collections.sort(sortedExpTimes);
	}

	/*
	 * Reads the file entered as the first argument of TomcatLogAnalyzer, and
	 * parses it to ge a Map of entries
	 */
	public void parseExperimentLog(final String filename) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		String line;
		while ((line = reader.readLine()) != null) {
			parseExperimentTypeLogLine(line);
		}
		reader.close();
	}

	private void parseExperimentTypeLogLine(final String line) {
		final Matcher matcher = EXP_PAT.matcher(line);
		if (matcher.find()) {
			final Long time = Long.parseLong(matcher.group(3));
			final String expType = matcher.group(1);
			EXP_TYPES.put(time, expType);
		}
	}

	/*
	 * @param string an entry of experiment log
	 * 
	 * @return true if string matches # orch|chor,n,n,long
	 */
	public boolean matchesExperimentTypeLog(final String line) {
		return EXP_PAT.matcher(line).find();
	}

	/*
	 * Returns a string representation of an instance of experiment
	 * 
	 * @param l a long representing a date in millis
	 * 
	 * @return a string like orch,1,100
	 */
	public String getExperimentType(final long time) {
		Long startTime;

		if (EXP_TYPES.containsKey(time)) {
			startTime = time;
		} else {
			startTime = searchExpStartTime(time);
		}

		return EXP_TYPES.get(startTime);
	}

	private long searchExpStartTime(final long time) {
		int index = 0;

		while (sortedExpTimes.get(index) < time
				&& index < sortedExpTimes.size()) {
			index++;
		}

		if (index >= sortedExpTimes.size()) {
			index = sortedExpTimes.size() - 1;
		} else {
			index--;
		}

		return sortedExpTimes.get(index);
	}

	/*
	 * @param date a string representation of a date e.g. 16/Oct/2012:00:00:08
	 * 
	 * @return Date object if date can be converted to Date and null otherwise
	 */
	public Date getDateInMillis(final String tomcatDate) throws ParseException {
		return tomcatDateF.parse(tomcatDate);
	}

	/*
	 * @param string an entry of apache tomcat log
	 * 
	 * @return true if string matches tomcatPattern
	 */
	public boolean matchesTomcatPattern(final String line) {
		return TOMCAT_PAT.matcher(line).find();
	}

	/*
	 * Used only to test
	 */
	public Map<Long, String> getExperimentEntries() {
		return EXP_TYPES;
	}

	/*
	 * Used only to test
	 */
	public List<Long> getSortedExpTimes() {
		return sortedExpTimes;
	}
}