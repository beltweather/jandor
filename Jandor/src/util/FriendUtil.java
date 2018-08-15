package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import canvas.CardLayer;
import multiplayer.MultiplayerConnection;
import multiplayer.MultiplayerMessage;
import redis.BinarySubscriber;
import session.User;
import ui.ProgressBar.ProgressTask;
import ui.ProgressDialog;
import ui.pwidget.JUtil;

public class FriendUtil {
	
	private static Map<String, MultiplayerConnection> connectedByUserGUID = new HashMap<String, MultiplayerConnection>();
	private static Map<String, Boolean> inviteResponsesByGUID = new HashMap<String, Boolean>();
	private static long INVITE_TIMEOUT = 20000;
	private static MultiplayerConnection latestInviteConnection;
	
	public static Collection<String> getConnectedUserGUIDS() {
		return connectedByUserGUID.keySet();
	}
	
	public static boolean isConnected() {
		return connectedByUserGUID.size() > 0;
	}
	
	public synchronized static void clearInviteResponses() {
		inviteResponsesByGUID.clear();
	}
	
	public synchronized static void setInviteResponse(String inviteGUID, Boolean inviteResponse) {
		inviteResponsesByGUID.put(inviteGUID, inviteResponse);
	}
	
	public synchronized static Boolean getInviteResponse(String inviteGUID) {
		if(!inviteResponsesByGUID.containsKey(inviteGUID)) {
			return null;
		}
		return inviteResponsesByGUID.get(inviteGUID);
	}
	
	private FriendUtil() {}
	
	public static MultiplayerConnection inviteFriend(final User user) {
		if(!JUtil.showConfirmDialog(null, "Connect to Friend", "Share your active board view with user \"" + user.getUsername() + "\" and view their board?")) {
			return null;
		}

		latestInviteConnection = null;
		clearInviteResponses();
		ProgressDialog dialog = new ProgressDialog("Waiting for Friend \"" + user.getUsername() + "\"") {
			
			@Override
			public void run(ProgressTask task) {
				System.out.println("Sending invite to user \"" + user.getUsername() + "\"");
				getProgressBar().setIndeterminate(true);
				getProgressBar().setStringPainted(false);
				
				MessageUtil.sendInvite(user.getGUID());
			}

			@Override
			public void finished(ProgressTask task) {
				closeDialog();
			}
			
		};
		dialog.showDialog();
		return latestInviteConnection;
	}
	
	public static MultiplayerConnection connectToFriend(final User user) {
		if(user == null) {
			return null;
		}
		
		MultiplayerConnection connection = new MultiplayerConnection(user);
		connectedByUserGUID.put(user.getGUID(), connection);
		
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
			layer.getPlayerButtonPanel().rebuild();
			layer.getOpponentButtonPanel().rebuild();
			layer.repaint();
		}
		
		JedisUtil.subscribe(JedisUtil.toStreamChannel(user), new BinarySubscriber() {

			@Override
			public void handleOnMessage(byte[] channel, byte[] message) {
				FriendUtil.updateConnectedView(user.getGUID(), user.getUsername(), message);
			}
			
		});
		MessageUtil.startStreaming();
		
		return connection;
	}

	public static void disconnectFromFriend(MultiplayerConnection connection) {
		String userGUID = null;
		for(String guid : connectedByUserGUID.keySet()) {
			if(connectedByUserGUID.get(guid).equals(connection)) {
				userGUID = guid;
				break;
			}
		}
		if(userGUID == null) {
			return;
		}
		disconnectFromFriend(userGUID);
	}
	
	public static void disconnectFromFriend(String userGUID) {
		disconnectFromFriend(userGUID, false);
	}
	
	public static void disconnectFromFriend(String userGUID, boolean opponentDisconnectedFirst) {
		if(userGUID == null) {
			return;
		}
		
		if(LoginUtil.isLoggedIn() && !opponentDisconnectedFirst) {
			String loginUserGUID = LoginUtil.getUser().getGUID();
			byte[] channel = JedisUtil.toBytes(JedisUtil.toStreamChannel(loginUserGUID));
			JedisUtil.publish(channel, SerializationUtil.toBytes(MultiplayerMessage.getDisconnectMessage(userGUID)));
		}
		
		connectedByUserGUID.remove(userGUID);
		
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
			layer.setOpponentMessage(null);
			layer.getPlayerButtonPanel().rebuild();
			layer.getOpponentButtonPanel().rebuild();
			layer.repaint();
		}
		
		JedisUtil.unsubscribe(JedisUtil.toStreamChannel(userGUID));
		
		if(opponentDisconnectedFirst) {
			JUtil.showMessageDialog(null, "Disconnected from Friend", "Friend \"" + UserUtil.getUserByGUID(userGUID).getUsername() + "\" disconnected from you.");
		} else {
			JUtil.showMessageDialog(null, "Disconnected from Friend", "Disconnected from friend \"" + UserUtil.getUserByGUID(userGUID).getUsername() + "\"");
		}
	}
	
	public static void disconnectFromFriend(User user) {
		if(user == null || !connectedByUserGUID.containsKey(user.getGUID())) {
			return;
		}
		connectedByUserGUID.remove(user.getGUID());
		JUtil.showMessageDialog(null, "Disconnected from Friend", "Disconnected from friend \"" + UserUtil.getUserByGUID(user.getGUID()).getUsername() + "\"");
	}
	
	public static void updateConnectedView(String userGUID, MultiplayerMessage message) {
		if(!connectedByUserGUID.containsKey(userGUID)) {
			return;
		}
		MultiplayerConnection connection = connectedByUserGUID.get(userGUID);
		connection.setMessage(message);
	}
	
	public static void updateConnectedView(String userGUID, String currentUsername, String serializedRenderables) {
		if(!connectedByUserGUID.containsKey(userGUID)) {
			return;
		}
		
		MultiplayerConnection connection = connectedByUserGUID.get(userGUID);
		MultiplayerMessage message = (MultiplayerMessage) SerializationUtil.fromString(serializedRenderables);
		connection.setMessage(message);
		
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
			String opponentGUID = layer.getOpponentButtonPanel().getOpponentGUID();
			if(opponentGUID != null && opponentGUID.equals(userGUID)) {
				layer.setOpponentMessage(message);
				layer.getOpponentButtonPanel().updateLabels();
				layer.repaint();
			}
		}
	}
	
	public static void updateConnectedView(String userGUID, String currentUsername, byte[] serializedRenderables) {
		if(!connectedByUserGUID.containsKey(userGUID)) {
			return;
		}
		MultiplayerConnection connection = connectedByUserGUID.get(userGUID);
		MultiplayerMessage message = (MultiplayerMessage) SerializationUtil.fromBytes(serializedRenderables);
		connection.setMessage(message);
		
		if(message.isDisconnect()) {
			if(LoginUtil.isLoggedIn() && message.getDisconnectGUID().equals(LoginUtil.getUser().getGUID())) {
				disconnectFromFriend(userGUID, true);
			}
			return;
		}
		
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
			String opponentGUID = layer.getOpponentButtonPanel().getOpponentGUID();
			if(opponentGUID != null && opponentGUID.equals(userGUID)) {
				layer.setOpponentMessage(message);
				layer.getOpponentButtonPanel().updateLabels();
				layer.repaint();
			}
		}
	}
	
	public static MultiplayerMessage getOpponentMessage(String userGUID) {
		if(!connectedByUserGUID.containsKey(userGUID)) {
			return null;
		}
		return connectedByUserGUID.get(userGUID).getMessage();
	}
	
}
