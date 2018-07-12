package util;

import redis.Subscriber;
import session.Session;
import session.User;
import sheets.domain.UserSheet;
import drive.FileListener;

public class LoginUtil {

	private static User user = null;
	private static FileListener inboxListener = null;
	
	private static final String GUID_NO_LOGIN = "USER-NO-LOGIN";
	public static User NO_LOGIN = new User(GUID_NO_LOGIN, "Unknown", "Unknown", "Unknown", "Unknown", "Unknown");
	
	private LoginUtil() {}
	
	public static boolean login(User user) {
		logout();
		UserSheet users = new UserSheet();
		if(DebugUtil.OFFLINE_MODE || users.isValidUser(user)) {
			LoginUtil.user = user;
			if(!DebugUtil.OFFLINE_MODE) {
				/*inboxListener = new FileListener(user.getInboxFolderId()) {

					@Override
					public void callback(Change change, List<File> files) {
						MailUtil.receiveFilesFromDrive(files);
					}
					
				};*/
				
				MessageUtil.subscribeToMessages(user.getGUID());
				
				//DriveUtil.addFileListener(inboxListener);
			}
			return true;
		}
		return false;
	}
	
	public static void logout() {
		if(user != null && !DebugUtil.OFFLINE_MODE) {
			MessageUtil.unsubscribeFromMessages(user.getGUID());
			MessageUtil.unsubscribeFromStream(user.getGUID());
		}
		user = null;
		if(inboxListener != null) {
			//DriveUtil.removeFileListener(inboxListener);
			inboxListener = null;
		}
	}
	
	public static boolean isLoggedIn() {
		return user != null;
	}
	
	public static User getUser() {
		if(user == null) {
			return NO_LOGIN;
		}
		return user;
	}
	
	public static void init() {
		User user = Session.getInstance().getUser();
		if(user == null) {
			return;
		}
		login(user);
	}
	
}
