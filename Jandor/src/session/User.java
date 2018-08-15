package session;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import util.DriveUtil;
import util.FileUtil;
import util.LoginUtil;

@XmlRootElement
public class User extends SessionData {

	public static final int USER_ID = 2;
	
	/**
	 * Generated user id. This will always stay the same for this user, even
	 * when other things about them change.
	 */
	private String guid;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	
	private transient String initials;
	
	private boolean defaultUser;
	
	public User() {
		super();
	}
	
	public User(int id) {
		super(id);
	}
	
	public User(String... values) {
		this(Arrays.asList(values));
	}
	
	public User(List<String> values) {
		super();
		setGUID(values.get(0));
		setUsername(values.get(1));
		setPassword(values.get(2));
		setFirstName(values.get(3));
		setLastName(values.get(4));
		setEmail(values.get(5));
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getInitials() {
		return getInitials(false);
	}
	
	public String getInitials(boolean period) {
		if(initials == null) {
			initials = firstName.substring(0,1).toUpperCase() + (period ? ". " : "") + lastName.substring(0,1).toUpperCase() + (period ? "." : "");
		}
		return initials;
	}
	
	public boolean isDefaultUser() {
		return defaultUser;
	}

	public void setDefaultUser(boolean defaultUser) {
		this.defaultUser = defaultUser;
	}

	@Override
	public File getFolder() {
		return FileUtil.getUserFolder();
	}

	@Override
	public String toString() {
		return getUsername() + " (" + getEmail() + ")";
	}

	public String getGUID() {
		return guid;
	}

	public void setGUID(String guid) {
		this.guid = guid;
	}

	@XmlTransient
	public String getInboxFolderId() {
		return DriveUtil.toInboxFolderId(this);
	}

	@XmlTransient
	public String getBackupFolderId() {
		return DriveUtil.toBackupFolderId(this);
	}
	
	public boolean hasEmail() {
		return email != null && !email.isEmpty();
	}
	
	public boolean isLoggedIn() {
		return LoginUtil.isLoggedIn() && LoginUtil.getUser().equals(this);
	}
	
	private boolean safeEquals(Object a, Object b) {
		if(a == null && b == null) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof User)) {
			return false;
		}
		User u = (User) o;
		return safeEquals(guid, u.guid) && 
			   safeEquals(username, u.username) &&
			   safeEquals(password, u.password) &&
			   safeEquals(firstName, u.firstName) &&
			   safeEquals(lastName, u.lastName) &&
			   safeEquals(email, u.email);
 	}
	
}
