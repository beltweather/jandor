package util;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import run.Jandor;

public class FileUtil {

	private FileUtil() {}
	
	public static final String ENV_JANDOR_HOME = "JANDOR_HOME";
	public static final String FOLDER_JANDOR_DATA = "Jandor-Data";
	public static final String RESOURCE_CARDS_JSONS = "AllCards-x.json";
	public static final String RESOURCE_SETS_JSONS = "AllSets.json";
	public static final String RESOURCE_MTG_JSON_VERSION = "mtg-json-version.txt";
	
	public static final String DEFAULT_EXT = "dec";
	private static final String DEFAULT_EXT_DESCRIPTION = "Apprentice Deck File (*.dec)";
	private static final JFileChooser chooser = new JFileChooser();
	static {
		chooser.setFileFilter(new FileNameExtensionFilter(DEFAULT_EXT_DESCRIPTION, DEFAULT_EXT));
	}
	
	public static void init() {
		initCredentialsFile();
	}
	
	private static void initCredentialsFile() {
		java.io.File credentialsFile = FileUtil.toFile(FileUtil.getCredentialsFolder(), "StoredCredential");
		if(!credentialsFile.exists()) {
			copy(FileUtil.class.getResourceAsStream("/StoredCredential"), credentialsFile.getPath());
		}
	}
	
	/**
     * Copy a file from source to destination.
     *
     * @param source
     *        the source
     * @param destination
     *        the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source , String destination) {
        boolean succeess = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
        	ex.printStackTrace();
        	succeess = false;
        }

        return succeess;

    }
	
	public static List<File> getFiles(File folder) {
		return Arrays.asList(folder.listFiles());
	}

	public static File getCredentialsFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Credentials");
	}
	
	public static File getDraftHeaderFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Drafts", "Headers");
	}
	
	public static File getDraftContentFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Drafts", "Content");
	}
	
	public static File getBoosterHeaderFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Boosters", "Headers");
	}
	
	public static File getBoosterContentFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Boosters", "Content");
	}
	
	public static File getHeaderFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Collection", "Headers");
	}
	
	public static File getTagFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Collection", "Tags");
	}
	
	public static File getContentFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Collection", "Content");
	}
	
	public static File getContactFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Contacts");
	}
	
	public static File getUserFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Users");
	}
	
	public static File getPreferencesFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Preferences");
	}
	
	public static File getCachedImagesFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "CachedImages");
	}
	
	public static File getExternalResourcesFolder() {
		return createFolder(FOLDER_JANDOR_DATA, "Resources");
	}
	
	public static List<File> getHeaderFiles() {
		return getFiles(getHeaderFolder());
	}
	
	public static List<File> getDraftHeaderFiles() {
		return getFiles(getDraftHeaderFolder());
	}
	
	public static List<File> getBoosterHeaderFiles() {
		return getFiles(getBoosterHeaderFolder());
	}
	
	public static List<File> getTagFiles() {
		return getFiles(getTagFolder());
	}
	
	public static List<File> getContentFiles() {
		return getFiles(getContentFolder());
	}
	
	public static List<File> getDraftContentFiles() {
		return getFiles(getDraftContentFolder());
	}
	
	public static List<File> getBoosterContentFiles() {
		return getFiles(getBoosterContentFolder());
	}
	
	public static List<File> getContactFiles() {
		return getFiles(getContactFolder());
	}
	
	public static List<File> getUserFiles() {
		return getFiles(getUserFolder());
	}
	
	public static List<File> getPreferenceFiles() {
		return getFiles(getPreferencesFolder());
	}
	
	public static File getPreferenceFile() {
		return toFile(getPreferencesFolder(), "Preferences-0");
	}
	
	public static List<File> getCachedImageFiles() {
		return getFiles(getCachedImagesFolder());
	}
	
	public static File getExternalResourcesFile(String filename) {
		return new File(getExternalResourcesFolder(), filename);
	}
	
	public static File getCachedImageFile(int multiverseId) {
		if(multiverseId == -1) {
			return null;
		}
		return new File(getCachedImagesFolder(), multiverseId + ".png");
	}
	
	public static JFileChooser getFileChooser() {
		return chooser;
	}
	
	public static File chooseFile(Component parent, boolean open) {
		final JFileChooser fc = FileUtil.getFileChooser(); 
		int returnVal = open ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(!open && !file.getAbsolutePath().endsWith(".dec")) {
				file = new File(file.getAbsoluteFile() + ".dec");
				fc.setSelectedFile(file);
			}
			return file;
		}
		return null;
	}
	
	public static File[] chooseFiles(Component parent) {
		final JFileChooser fc = FileUtil.getFileChooser();
		fc.setMultiSelectionEnabled(true);
		File[] files;
		if(fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			files = fc.getSelectedFiles();
		} else {
			files = new File[]{};
		}
		fc.setMultiSelectionEnabled(false);
		return files;
	}
	
	public static BufferedReader getReader(String filename) {
		BufferedReader reader = null;
		try {
        	File file = new File(filename);
        	reader = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            e.printStackTrace();
        } 
		
		return reader;
	}
	
	public static BufferedReader getResourceReader(String filename) {
		InputStream input = null;
		
		// If we have a copy of this resource in our external resources file,
		// use this one. Otherwise, use the one from our internal resources folder.
		// This allows the user to override resources by placing them in "Jandor-Data\Resources"
		File externalFile = getExternalResourcesFile(filename);
		if(externalFile != null && externalFile.exists()) {
			try {
				input = new FileInputStream(externalFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			input = Jandor.class.getResourceAsStream("/" + filename);
		}
		
		BufferedReader reader = null;
		try {
        	reader = new BufferedReader(new InputStreamReader(input));
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return reader;
	}
	
	public static List<String> getLines(BufferedReader reader) {
		if(reader == null) {
			return null;
		}
		List<String> lines = new ArrayList<String>();
		String line;
			
		try {
			
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return lines;
	}
	
	public static String getFirstLine(BufferedReader reader) {
		List<String> lines = getLines(reader);
		if(lines != null && lines.size() > 0) {
			return lines.get(0);
		}
		return null;
	}
	
	public static String getRoot() {
		String jandorHome = System.getenv(ENV_JANDOR_HOME);
		if(jandorHome != null) {
			return jandorHome;
		}
		return Jandor.class.getResource("/").getPath();
	}
	
	public static File toFile(String... fileName) {
		String fullFileName = "";
		for(String f : fileName) {
			if(fullFileName.length() == 0) {
				fullFileName = f;
			} else {
				fullFileName += File.separator + f;
			}
		}
		String path = getRoot() + File.separator + fullFileName;
		
		// This keeps the data folder out of the bin folder so it can't be blown away
		if(path.contains("Jandor/bin")) {
			path = path.replace("/Jandor/bin/", "");
		}
		
		return new File(path);
	}
	
	public static File toFile(File folder, String fileName) {
		return new File(folder.getAbsolutePath() + File.separator + fileName);
	}
	
	public static File createFolder(String... folderName) {
		File folder = null;
		for(int i = 0; i < folderName.length; i++) {
			folder = toFile(Arrays.copyOfRange(folderName, 0, i+1));
			if(!folder.exists()) {
				folder.mkdir();
			}
		}
		return folder;
	}
	
	public static Object readXML(File file) {
		if(file == null) {
			return null;
		}
		
		Object obj = null;
		try {
			return JAXBUtil.unmarshal(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public static void writeXML(File file, Object obj) {
		if(obj == null || file == null) {
			return; 
		}
		JAXBUtil.marshal(obj, file);
	}
	
	public static void write(BufferedReader fromReader, File toFile) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(toFile));
			String line = null;
			while ((line = fromReader.readLine()) != null) {
				writer.write(line);
				writer.newLine();
		    }
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void writeString(String text, String toFilename, boolean append) {
		writeString(text, toFilename == null ? null : new File(toFilename), append);
	}
	
	public static void writeString(String text, File toFile, boolean append) {
		if(text == null) {
			return;
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(toFile, append));
			writer.write(text);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
