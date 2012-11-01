package org.apache.hadoop.examples.ep2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ExperimentEntryProcessor {
	
	HashMap<Long, String> entries;
	private ArrayList<Long> keys;
	
	public ExperimentEntryProcessor() {
		
		entries = new HashMap<Long, String>();
		
		// populates the hash map
		this.getExperimentEntryMap();
		
		Set<Long> ks = entries.keySet();
		keys = new ArrayList<Long>(ks);
		
		Collections.sort(keys);
	}
	
	public void getExperimentEntryMap() {
		
		InputStream is = ClassLoader.getSystemResourceAsStream("filtered_log");
		
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    
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

}
