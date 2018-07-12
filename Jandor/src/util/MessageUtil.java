package util;

import canvas.CardLayer;
import redis.Subscriber;
import session.User;
import ui.pwidget.JUtil;

public class MessageUtil {
	
	private static final long STREAMING_RATE = 15;
	
	public static final String MESSAGE_DRAFT = "Draft";
	public static final String MESSAGE_BOOSTER = "Booster";
	public static final String MESSAGE_CARDLAYER = "CardLayer";
	public static final String MESSAGE_INVITE = "Invite";
	public static final String MESSAGE_INVITE_RESPONSE = "ResponseInvite";

	private MessageUtil() {}
	
	public static void subscribeToMessages(final String userGUID) {
		JedisUtil.subscribe(JedisUtil.toMessageChannel(userGUID), new Subscriber() {
		
			@Override
			public void onMessage(String channel, String message) {
				if(message.startsWith(MESSAGE_INVITE)) {
					handleInvite(message);
				} else if(message.startsWith(MESSAGE_INVITE_RESPONSE)) {
					handleInviteResponse(message);
				}
			}
			
		});
	}
	
	public static void unsubscribeFromMessages(String userGUID) {
		JedisUtil.unsubscribe(JedisUtil.toMessageChannel(userGUID));
	}
	
	public static void unsubscribeFromStream(String userGUID) {
		JedisUtil.unsubscribe(JedisUtil.toStreamChannel(userGUID));
	}
	
	public static void sendMessage(String toUserGUID, String type, String fromUserGUID, String message) {
		String fullMessage = type + ":" + fromUserGUID + ":" + message;
		JedisUtil.publish(JedisUtil.toMessageChannel(toUserGUID), fullMessage);
	}
	
	public static String sendInvite(String toUserGUID) {
		String inviteGUID = IDUtil.generateGUID();
		sendMessage(toUserGUID, MESSAGE_INVITE, LoginUtil.getUser().getGUID(), inviteGUID);
		return inviteGUID;
	}
	
	public static void sendInviteResponse(String toUserGUID, String inviteGUID, boolean response) {
		sendMessage(toUserGUID, MESSAGE_INVITE_RESPONSE, LoginUtil.getUser().getGUID(), inviteGUID + ":" + response);
	}
	
	public static void handleMessage(String message) {
		if(message == null) {
			return;
		}
		if(message.startsWith(MESSAGE_INVITE)) {
			handleInvite(message);
		} else if(message.startsWith(MESSAGE_INVITE_RESPONSE)) {
			handleInviteResponse(message);
		}
	}
	
	public static void handleInvite(String message) {
		String[] toks = message.split(":");
		String userGUID = toks[1];
		String inviteGUID = toks[2];
		User user = UserUtil.getUserByGUID(userGUID);
		boolean response = JUtil.showConfirmYesNoDialog(null, "Accept Invite", "User \"" + user.getUsername() + "\" would like to connect. Will you accept?");
		if(response) {
			FriendUtil.connectToFriend(user);
		}
		sendInviteResponse(userGUID, inviteGUID, response);
	}
	
	public static void handleInviteResponse(String message) {
		String[] toks = message.split(":");
		String userGUID = toks[1];
		String inviteGUID = toks[2];
		boolean yesNo = Boolean.valueOf(toks[3]);
		User user = UserUtil.getUserByGUID(userGUID);
		FriendUtil.setInviteResponse(inviteGUID, yesNo);
		if(yesNo) {
			FriendUtil.connectToFriend(user);
		}
	}
	
	public static void startStreaming() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}
	
		new Thread() {
	    		
    		@Override
			public void run() {
    			System.out.println("Started streaming user: " + LoginUtil.getUser().getUsername());
    			try {
    				String loginUserGUID = LoginUtil.getUser().getGUID();
    				byte[] channel = JedisUtil.toBytes(JedisUtil.toStreamChannel(loginUserGUID));
					while(FriendUtil.isConnected() && LoginUtil.isLoggedIn()) {
						long time = System.currentTimeMillis();
						CardLayer layer = CardLayer.getActiveCardLayer();
						if(layer != null) {
							CardLayer.clearActiveCardLayer();
							JedisUtil.publish(channel, layer.getSerializedRenderablesBytes());
						}
						long diff = STREAMING_RATE - (System.currentTimeMillis() - time);
						if(diff > 0) {
							Thread.sleep(diff);
						}
					}
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			System.out.println("Finished streaming user: " + LoginUtil.getUser().getUsername());
    		}
    		
	    }.start();
	}
	
}
