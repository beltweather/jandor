package drive;

import java.util.List;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;

public abstract class FileListener {

	private String fileId;
	private boolean oneTime = false;
	
	public FileListener(String fileId) {
		this(fileId, false);
	}
	
	public FileListener(String fileId, boolean oneTime) {
		this.fileId = fileId;
		this.oneTime = oneTime;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public boolean isOneTime() {
		return oneTime;
	}
	
	public void callback(Change change) {
		callback(change, null);
	}
	
	public void callback(List<File> files) {
		callback(files);
	}
	 
	public abstract void callback(Change change, List<File> files);	
}
