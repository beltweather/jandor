package util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

	private ZipUtil() {}

	public static List<String> unzip(File zipFile, File outputFolder) {
		return unzip(zipFile == null ? null : zipFile.getAbsolutePath(), outputFolder == null ? null : outputFolder.getAbsolutePath());
	}

	public static List<String> unzip(String zipFilename, String outputFoldername) {

		List<String> fileList = null;

	    byte[] buffer = new byte[1024];

	    try{

	    	//create output directory is not exists
	    	File folder = new File(outputFoldername);
	    	if(!folder.exists()){
	    		folder.mkdir();
	    	}

	    	//get the zip file content
	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilename));
	    	//get the zipped file list entry
	    	ZipEntry ze = zis.getNextEntry();

	    	while(ze!=null){

	    	   String fileName = ze.getName();
	           File newFile = new File(outputFoldername + File.separator + fileName);

	           //System.out.println("file unzip : "+ newFile.getAbsoluteFile());

	           //create all non exists folders
	           //else you will hit FileNotFoundException for compressed folder
	           new File(newFile.getParent()).mkdirs();

	           FileOutputStream fos = new FileOutputStream(newFile);

	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	       		fos.write(buffer, 0, len);
	            }

	            fos.close();
	            ze = zis.getNextEntry();
	    	}

	        zis.closeEntry();
	    	zis.close();

	    	//System.out.println("Done");

	    } catch(Exception ex) {
	       ex.printStackTrace();
	    }

	    return fileList;
   }

}
