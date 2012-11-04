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

public class ExperimentEntryProcessor {

	HashMap<Long, String> entries;
	private ArrayList<Long> keys;
	private String fileName;

	public ExperimentEntryProcessor(String appLog) {

		fileName = appLog;

		entries = new HashMap<Long, String>();

		// populates the hash map
		try {
			this.getExperimentEntryMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Set<Long> ks = entries.keySet();
		keys = new ArrayList<Long>(ks);

		Collections.sort(keys);
	}

	public void getExperimentEntryMap() throws FileNotFoundException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				if(this.matchesApplicationLog(line)) {
					
					String exp = line.split(" ")[1];
					entries.put(Long.parseLong(exp.split(",")[3]), 
							exp.split(",")[0].concat(","+exp.split(",")[1].concat(","+exp.split(",")[2])));

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// # orch,2,50,1349926584487
	private final String applicationLogPattern = "^# (orch|chor),(\\d+),(\\d+),(\\d+)";

	public boolean matchesApplicationLog(String line) {
		return line.matches(applicationLogPattern);
	}

	public String getParams(long l) {

		// if exact value
		if(keys.contains(l)) return entries.get(l);

		// search key range
		int index = 0;
		while (keys.get(index) < l && index < keys.size()) index++;

		// time greater than all in keys (POSSIBLE result of the last experiment)
		// TODO: limit the time after last key
		if(index >= keys.size()) return entries.get(keys.get(keys.size()-1));

		return entries.get(keys.get(index-1));

	}

	// Test purpose
	public HashMap<Long, String> getExperimentEntries() {
		return entries;
	}

	// Test purpose
	public ArrayList<Long> getEntriesKey() {
		return keys;
	}

	public Date getDateInMillis(String date) {
		// apache tomcat date example -> 16/Oct/2012:00:00:08

		date = date.replaceFirst(":", " ");

		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss");

		try {
			Date ds = format.parse(date);
			return ds;
		} catch (ParseException e) {
			return null;
		}

	}

	private final String tomcatPattern = "" +
			"^(\\d{1,3}\\.){3}\\d{1,3} - - \\[(\\d){2}/(\\w){3}/(\\d){4}(:\\d{2}){3} -\\d{4}\\] " +
			"\"POST (.+) (HTTP/1\\.1)\"( \\d+){3}";

	public boolean matchesTomcatPattern(String string) {
		return string.matches(tomcatPattern);
	}
}
