package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtil {
	
	public static final String URL_MTG_JSON = "http://www.mtgjson.com";
	public static final String URL_MTG_JSON_SETS = URL_MTG_JSON + "/json/AllSets.json.zip";
	public static final String URL_MTG_JSON_CARDS = URL_MTG_JSON + "/json/AllCards-x.json.zip"; 
	
	private static final String SCRAPE_START_VERSION = "<div>Current version: <a href=\"/changelog.html\">";
	private static final String SCRAPE_END_VERSION = "</a></div>";
	
	public static void init() {
		downloadNewestJSONS();
	}
	
	private WebUtil() {}
	
	public static File download(String url, File outputFile) {
		return download(url, outputFile == null ? null : outputFile.getAbsolutePath());
	}
	
	public static File download(String url, String outputFilename) {
		URL website = null;
		try {
			website = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		if(website == null) {
			return null;
		}
		
		try (InputStream inputStream = website.openStream(); 
			 ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream); 
			 FileOutputStream fileOutputStream = new FileOutputStream(outputFilename)) { 
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File file = new File(outputFilename);
		if(file.exists()) {
			return file;
		}
		return null;
	}
	
	public static StringBuilder readHtml(String url) {
		URL website = null;
		try {
			website = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
        try(InputStream in = website.openStream();
        	InputStreamReader isr = new InputStreamReader(in);
        	BufferedReader br = new BufferedReader(isr)) {
        	String line;
        	while ((line = br.readLine()) != null) {
        		sb.append(line);
        		sb.append(System.lineSeparator());
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return sb;
	}
	
	public static String scrapeUrl(String url, String start, String end) {
		return scrape(readHtml(url).toString(), start, end);
	}
	
	public static String scrape(String html, String start, String end) {
		String regexString = Pattern.quote(start) + "(.*?)" + Pattern.quote(end);
		Pattern pattern = Pattern.compile(regexString);
		Matcher matcher = pattern.matcher(html);
		while(matcher.find()) {
			String text = matcher.group(1);
			if(text != null) {
				return text;
			}
		}
		return null;
	}
	
	public static int downloadNewestJSONS() {
		int error = 0;
		
		String jandorVersion = VersionUtil.readVersionFromFile();
		String mtgJsonVersion = scrapeUrl(URL_MTG_JSON, SCRAPE_START_VERSION, SCRAPE_END_VERSION);

		if(jandorVersion == null || mtgJsonVersion == null) {
			if(jandorVersion == null) {
				System.err.println("Could not determine mtgjson.com version being used by Jandor");
				error++;
			}
			if(mtgJsonVersion == null) {
				System.err.println("Could not determine current version used by mtgjson.com");
				error++;
			}
			return error;
		} else if(jandorVersion.equals(mtgJsonVersion)) {
			System.out.println("Jandor is up-to-date with mtgjson.com version " + mtgJsonVersion);
			return error;
		}
		
		System.out.println("Updating Jandor from mtgjson.com version " + jandorVersion + " to " + mtgJsonVersion);
		
		File setJsons = download(URL_MTG_JSON_SETS, FileUtil.toFile(FileUtil.getExternalResourcesFolder(), FileUtil.RESOURCE_SETS_JSONS + ".zip"));
		File cardJsons = download(URL_MTG_JSON_CARDS, FileUtil.toFile(FileUtil.getExternalResourcesFolder(), FileUtil.RESOURCE_CARDS_JSONS + ".zip"));
		
		if(setJsons == null) {
			error++;
			System.err.println("Downloading " + URL_MTG_JSON_SETS + " failed.");
		} else {
			ZipUtil.unzip(setJsons, FileUtil.getExternalResourcesFolder());
			setJsons.delete();
		}
		
		if(cardJsons == null) {
			error++;
			System.err.println("Downloading " + URL_MTG_JSON_CARDS + " failed.");
		} else {
			ZipUtil.unzip(cardJsons, FileUtil.getExternalResourcesFolder());
			cardJsons.delete();
		}
		
		if(mtgJsonVersion == null) {
			error++;
			System.err.println("Could not scrape version from " + URL_MTG_JSON);
		} else if(error == 0) {
			FileUtil.writeString(mtgJsonVersion, FileUtil.getExternalResourcesFolder() + File.separator + FileUtil.RESOURCE_MTG_JSON_VERSION, false);
		}
		
		return error;
	}

}
