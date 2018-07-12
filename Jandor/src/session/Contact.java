package session;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;

@XmlRootElement
public class Contact extends SessionData {

	public static final int USER_ID = 2;
	
	private String nickname; // user name
	private String jandorEmailPassword; // user password
	private String firstName;
	private String lastName;
	private String notificationEmail;
	
	private String jandorEmail; // unused
	
	private boolean defaulUser;
	
	public Contact() {
		super();
	}
	
	public Contact(int id) {
		super(id);
	}
	
	public String getJandorEmail() {
		return jandorEmail;
	}

	public void setJandorEmail(String jandorEmail) {
		this.jandorEmail = jandorEmail;
	}

	public String getJandorEmailPassword() {
		return jandorEmailPassword;
	}

	public void setJandorEmailPassword(String jandorEmailPassword) {
		this.jandorEmailPassword = jandorEmailPassword;
	}

	public String getNotificationEmail() {
		return notificationEmail;
	}

	public void setNotificationEmail(String notificationEmail) {
		this.notificationEmail = notificationEmail;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean isDefaulUser() {
		return defaulUser;
	}

	public void setDefaulUser(boolean defaulUser) {
		this.defaulUser = defaulUser;
	}

	@Override
	public File getFolder() {
		return FileUtil.getContactFolder();
	}

	@Override
	public String toString() {
		return getNickname() + " (" + getJandorEmail() + ")";
	}
	
}
