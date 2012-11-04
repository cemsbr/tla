package br.usp.ime.tla;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class LogProcessor {

	private final String applicationLogPattern = "^# (orch|chor),(\\d+),(\\d+),(\\d+)";
	private final String tomcatPattern = ""
			+ "^(\\d{1,3}\\.){3}\\d{1,3} - - \\[(\\d){2}/(\\w){3}/(\\d){4}(:\\d{2}){3} -\\d{4}\\] "
			+ "\"POST (.+) (HTTP/1\\.1)\"( \\d+){3}";

	HashMap<Long, String> entries;
	private ArrayList<Long> keys;
	private String fileName;

	public LogProcessor(String appLog) {
		fileName = appLog;
		populatesLogMap();
		setExperimentKeys();
	}

	/*
	 * Creates a key array list with entry map keys to maintain ir sorted
	 */
	private void setExperimentKeys() {
		Set<Long> ks = entries.keySet();
		keys = new ArrayList<Long>(ks);
		Collections.sort(keys);
	}

	/*
	 * Auxiliar method to populate the entry map
	 */
	private void populatesLogMap() {
		entries = new HashMap<Long, String>();

		try {
			this.getExperimentEntryMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Reads the file entered as the first argument of TomcatLogAnalyzer, and
	 * parses it to ge a Map of entries
	 */
	public void getExperimentEntryMap() throws FileNotFoundException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName)));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (this.matchesApplicationLog(line)) {

					String exp = line.split(" ")[1];
					entries.put(
							Long.parseLong(exp.split(",")[3]),
							exp.split(",")[0].concat(","
									+ exp.split(",")[1].concat(","
											+ exp.split(",")[2])));

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @param string an entry of experiment log
	 * 
	 * @return true if string matches # orch|chor,n,n,long
	 */
	public boolean matchesApplicationLog(String line) {
		return line.matches(applicationLogPattern);
	}

	/*
	 * Returns a string representation of an instance of experiment
	 * 
	 * @param l a long representing a date in millis
	 * 
	 * @return a string like orch,1,100
	 */
	public String getParams(long l) {
		if (keys.contains(l))
			return entries.get(l);

		int index = 0;
		while (keys.get(index) < l && index < keys.size())
			index++;

		if (index >= keys.size())
			return entries.get(keys.get(keys.size() - 1));

		return entries.get(keys.get(index - 1));
	}

	/*
	 * @param date a string representation of a date e.g. 16/Oct/2012:00:00:08
	 * 
	 * @return Date object if date can be converted to Date and null otherwise
	 */
	public Date getDateInMillis(String date) {
		date = date.replaceFirst(":", " ");
		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss");
		try {
			Date ds = format.parse(date);
			return ds;
		} catch (ParseException e) {
			return null;
		}
	}

	/*
	 * @param string an entry of apache tomcat log
	 * 
	 * @return true if string matches tomcatPattern
	 */
	public boolean matchesTomcatPattern(String string) {
		return string.matches(tomcatPattern);
	}

	/*
	 * Used only to test
	 */
	public HashMap<Long, String> getExperimentEntries() {
		return entries;
	}

	/*
	 * Used only to test
	 */
	public ArrayList<Long> getEntriesKey() {
		return keys;
	}
}
