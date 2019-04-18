package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import session.User;
import sheets.domain.UserSheet;

public class UserUtil {

	private static UserSheet userSheet;

	private static UserSheet getUserSheet() {
		if(userSheet == null) {
			userSheet = new UserSheet();
		}
		return userSheet;
	}

	private UserUtil() {}

	public static Map<String, User> getUsersByUsername() {
		UserSheet sheet = getUserSheet();
		Map<String, User> usersByUsername = new HashMap<String, User>();
		for(User user : sheet.getUsers()) {
			usersByUsername.put(user.getUsername(), user);
		}
		return usersByUsername;
	}

	public static List<User> getUsers() {
		return getUserSheet().getUsers();
	}

	public static User getUserByUsername(String username) {
		return getUserSheet().getUserByUsername(username);
	}

	public static User getUserByGUID(String guid) {
		return getUserSheet().getUserByGUID(guid);
	}

	public static User getUserByEmail(String email) {
		return getUserSheet().getUserByEmail(email);
	}

}
