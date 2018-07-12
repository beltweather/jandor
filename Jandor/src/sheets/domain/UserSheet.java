package sheets.domain;

import java.util.ArrayList;
import java.util.List;

import session.User;
import util.DriveUtil;
import util.IDUtil;
import util.MailUtil;
import drive.DefaultSheet;

public class UserSheet extends DefaultSheet {
	
	public UserSheet() {
		this(true);
	}
	
	public UserSheet(boolean read) {
		super(DriveUtil.SHEET_ID_USERS, DriveUtil.TAB_NAME_USERS, DriveUtil.START_RANGE_USERS, DriveUtil.END_RANGE_USERS, read);
	}
	
	public boolean hasUser(String username) {
		return exists("Username", username);
	}
	
	public boolean isValidUser(User user) {
		if(user == null) {
			return false;
		}
		return isValidUser(user.getUsername(), user.getPassword());
	}
	
	public boolean isValidUser(String username, String password) {
		if(!hasUser(username)) {
			return false;
		}
		return getValue("Username", username, "Password").equals(password);
	} 
	
	public void createUser(User user) {
		// Add user info to user's sheet
		appendRow(user.getGUID(), user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(), user.getEmail());
		write();
		
		// Create inbox folder for user
		DriveUtil.createFolder(DriveUtil.toInboxFolderName(user), DriveUtil.toFileId("Inboxes"));
	}
	
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		boolean first = true;
		for(List<String> row : getRows()) {
			if(first) {
				first = false;
				continue;
			}
			users.add(new User(row));
		}
		return users;
	}
	
	public User getUserByUsername(String username) {
		List<String> row = getRow("Username", username);
		return row == null ? null : new User(row);
	}
	
	public User getUserByGUID(String guid) {
		List<String> row = getRow("GUID", guid);
		return row == null ? null : new User(row);
	}
	
}
