package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

	private TimeUtil() {}
	
	public static String toFormattedDate(long timestamp) {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
		Date date = new Date(timestamp);
		return formatter.format(date);
	}
	
}
