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
		
		//InputStream is = ClassLoader.getSystemResourceAsStream("filtered_log");
		
		
		
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
	    
	    String line = null;
	    
	    try {
			while ((line = reader.readLine()) != null) {
				String exp = line.split(" ")[1];
				entries.put(Long.parseLong(exp.split(",")[3]), 
						exp.split(",")[0].concat(","+exp.split(",")[1].concat(","+exp.split(",")[2])));
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
