package br.usp.ime.tla;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomcatLogParser {
	private static final String REGEX = "^(\\d{1,3}\\.){3}\\d{1,3} - - \\[((\\d){2}/(\\w){3}/(\\d){4}(:\\d{2}){3}) -\\d{4}\\] "
			+ "\"POST /(\\w+)/.+ HTTP/1\\.1\"( \\d+){2} (\\d+)";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd/MMM/yyyy:HH:mm:ss", Locale.getDefault());

	private Matcher matcher;

	public boolean setLine(final String line) {
		matcher = PATTERN.matcher(line);
		return matcher.find();
	}

	/*
	 * Returns without taking into account the time zone
	 * 
	 * @return Date
	 */
	public Date getDate() throws ParseException {
		return dateFormat.parse(matcher.group(2));
	}

	public String getServiceName() {
		return matcher.group(7);
	}

	public int getResponseTime() {
		return Integer.parseInt(matcher.group(9));
	}
}