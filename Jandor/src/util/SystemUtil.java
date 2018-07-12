package util;

public class SystemUtil {
	
	private static Boolean mac = null;
	
	public static boolean isMac() {
		if(mac == null) {
			String OS = System.getProperty("os.name").toLowerCase();
			mac = OS.contains("os") || OS.contains("mac") || OS.contains("darwin");
		}
		return mac;
	} 
	
	private SystemUtil() {}
	
}
