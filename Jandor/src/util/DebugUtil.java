package util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ui.pwidget.JUtil;

public class DebugUtil {

	public static boolean OFFLINE_MODE = false;
	public static boolean IMAGES_OFFLINE_MODE = false;
	public static final boolean CACHE_IMAGES_FOR_OFFLINE = true;

	private DebugUtil() {}

	public static void init() {
		trustAllCerts();
		detectOffline();
	}

	private static void trustAllCerts() {
		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public X509Certificate[] getAcceptedIssuers() { return null; }
		        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
		        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		    }
		};

		// Activate the new trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {

		}
	}

	private static void detectOffline() {
		String urlString = ImageUtil.getUrl(1);
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL(urlString));
		} catch (IOException e) {
			image = null;
		}

		if(image == null) {
			System.out.println("Failed to load test image \"" + urlString + "\". Navigate to that location in a web browser to see if it exists.");
			IMAGES_OFFLINE_MODE = true;
			JUtil.showWarningDialog(null, "Cannot Download Images", "Could not connect to image service. You will not be able to see images for cards that you haven't already cached.");
		}

		// Test we can get our user data
		boolean success = UserUtil.getUsers().size() > 0;

		// If we couldn't load this image from a url, assume we are offline.
		// Note that if we are already in OFFLINE_MODE, we don't toggle it
		// on even if we're able to load the image.
		if(!success) {
			OFFLINE_MODE = true;
			System.out.println("Something is wrong, running in offline mode.");
			JUtil.showWarningDialog(null, "Running in Offline Mode", "Could not connect to services. Running in offline mode. You will not be able to send decks, play against friends, or any other online features.");
		}
	}

}
