package multiplayer;

import session.User;

public class MultiplayerConnection {

	private MultiplayerMessage message;
	private User user;
	
	public MultiplayerConnection(User user) {
		this.user = user;
	}
	
	public String getGUID() {
		return user.getGUID();
	}
	
	public User getUser() {
		return user;
	}
	
	public MultiplayerMessage getMessage() {
		return message;
	}
	
	public void setMessage(MultiplayerMessage message) {
		this.message = message;
	}
	
}
