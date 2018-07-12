package util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class DebugUtil {

	public static boolean OFFLINE_MODE = false;
	public static final boolean CACHE_IMAGES_FOR_OFFLINE = true;
	
	private DebugUtil() {}
	
	public static void init() {
		detectOffline();
	}
	
	private static void detectOffline() {
		String urlString = ImageUtil.getUrl(1);
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL(urlString));
		} catch (IOException e) {
			image = null;
		}
		
		// If we couldn't load this image from a url, assume we are offline.
		// Note that if we are already in OFFLINE_MODE, we don't toggle it
		// on even if we're able to load the image.
		if(image == null) {
			OFFLINE_MODE = true;
		}
	}
	
}
