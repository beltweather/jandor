package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import redis.BinarySubscriber;
import session.User;
import ui.ProgressBar.ProgressTask;
import ui.ProgressDialog;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.pwidget.PTabPane;
import ui.view.BoardView;
import canvas.CardLayer;
import canvas.LightCardLayer;

public class FriendUtil {

	private static Map<String, BoardView> connectedViewsByUserGUID = new HashMap<String, BoardView>();
	private static Map<String, Boolean> inviteResponsesByGUID = new HashMap<String, Boolean>();
	private static long INVITE_TIMEOUT = 20000;
	private static BoardView latestInviteBoardView;
	
	public static Collection<String> getConnectedUserGUIDS() {
		return connectedViewsByUserGUID.keySet();
	}
	
	public static boolean isConnected() {
		return connectedViewsByUserGUID.size() > 0;
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
	
	public static BoardView inviteFriend(final User user) {
		if(!JUtil.showConfirmDialog(null, "Connect to Friend", "Share your active board view with user \"" + user.getUsername() + "\" and view their board?")) {
			return null;
		}

		latestInviteBoardView = null;
		clearInviteResponses();
		ProgressDialog dialog = new ProgressDialog("Waiting for Friend \"" + user.getUsername() + "\"") {
			
			@Override
			public void run(ProgressTask task) {
				System.out.println("Sending invite to user \"" + user.getUsername() + "\"");
				getProgressBar().setIndeterminate(true);
				getProgressBar().setStringPainted(false);
				
				MessageUtil.sendInvite(user.getGUID());
				
				//String prefix = IDUtil.getUniquePrefix(IDUtil.PREFIX_INVITE);
				//String guid = IDUtil.extractPrefixGUID(prefix);
				/*MailUtil.sendToDriveInbox(user, prefix, LoginUtil.getUser().getGUID());
				long inviteStartTime = System.currentTimeMillis();
				try {
					while(getInviteResponse(guid) == null && (System.currentTimeMillis() - inviteStartTime) < INVITE_TIMEOUT) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Boolean inviteResponse = getInviteResponse(guid);
				if(inviteResponse != null && inviteResponse.booleanValue()) {
					latestInviteBoardView = connectToFriend(user);
				} else {
					if(inviteResponse == null) {
						JUtil.showMessageDialog(null, "Could Not Connect", "User \"" + user.getUsername() + "\" did not respond. Please try again.");
					} else {
						JUtil.showMessageDialog(null, "Could Not Connect", "User \"" + user.getUsername() + "\" declined the invite.");
					}
				}*/
			}

			@Override
			public void finished(ProgressTask task) {
				closeDialog();
			}
			
		};
		dialog.showDialog();
		return latestInviteBoardView;
	}
	
	public static BoardView connectToFriend(final User user) {
		if(user == null) {
			return null;
		}
		
		String title = "Board - " + user.getUsername();
		
		BoardView boardView = new BoardView(title, false);
		boardView.getCardLayer().setHideHand(true);
		connectedViewsByUserGUID.put(user.getGUID(), boardView);
		
		PTabPane tabPane = JandorTabFrame.getSingleFrame().getTabPane();
		tabPane.addTab(title, boardView);
		tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
		
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
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
		
		return boardView;
	}
	
	public static void disconnectFromFriend(BoardView view) {
		String userGUID = null;
		for(String guid : connectedViewsByUserGUID.keySet()) {
			if(connectedViewsByUserGUID.get(guid).equals(view)) {
				userGUID = guid;
				break;
			}
		}
		if(userGUID != null) {
			connectedViewsByUserGUID.remove(userGUID);
			
			for(CardLayer layer : CardLayer.getAllCardLayers()) {
				layer.getOpponentButtonPanel().rebuild();
				layer.opponentLayer = null;
				layer.repaint();
			}
			
			JedisUtil.unsubscribe(JedisUtil.toStreamChannel(userGUID));
			JUtil.showMessageDialog(null, "Disconnected from Friend", "Disconnected from friend \"" + UserUtil.getUserByGUID(userGUID).getUsername() + "\"");
		}
	}
	
	public static void disconnectFromFriend(User user) {
		if(user == null || !connectedViewsByUserGUID.containsKey(user.getGUID())) {
			return;
		}
		connectedViewsByUserGUID.remove(user.getGUID());
		JUtil.showMessageDialog(null, "Disconnected from Friend", "Disconnected from friend \"" + UserUtil.getUserByGUID(user.getGUID()).getUsername() + "\"");
	}
	
	public static void updateConnectedView(String userGUID, CardLayer layer) {
		if(!connectedViewsByUserGUID.containsKey(userGUID)) {
			return;
		}
		BoardView boardView = connectedViewsByUserGUID.get(userGUID);
		boardView.getCardLayer().setFromCardLayerShallowCopy(layer);
	}
	
	public static void updateConnectedView(String userGUID, String currentUsername, String serializedRenderables) {
		if(!connectedViewsByUserGUID.containsKey(userGUID)) {
			return;
		}
		BoardView boardView = connectedViewsByUserGUID.get(userGUID);
		boardView.getCardLayer().setCurrentUsername(currentUsername);
		((LightCardLayer) boardView.getCardLayer()).setSerializedCardList(serializedRenderables);
		boardView.repaint();
	}
	
	public static void updateConnectedView(String userGUID, String currentUsername, byte[] serializedRenderables) {
		if(!connectedViewsByUserGUID.containsKey(userGUID)) {
			return;
		}
		BoardView boardView = connectedViewsByUserGUID.get(userGUID);
		boardView.getCardLayer().setCurrentUsername(currentUsername);
		((LightCardLayer) boardView.getCardLayer()).setSerializedCardList(serializedRenderables);
		
		// XXX Gross code that needs to be changed and removed!
		for(CardLayer layer : CardLayer.getAllCardLayers()) {
			String opponentGUID = layer.getOpponentButtonPanel().getOpponentGUID();
			if(opponentGUID != null && opponentGUID.equals(userGUID)) {
				layer.opponentLayer = boardView.getCardLayer();
				layer.getOpponentButtonPanel().updateLabels();
				layer.repaint();
			}
		}
		
		boardView.repaint();
	}
	
	public static CardLayer getFriendLayer(String userGUID) {
		if(!connectedViewsByUserGUID.containsKey(userGUID)) {
			return null;
		}
		BoardView boardView = connectedViewsByUserGUID.get(userGUID);
		return boardView.getCardLayer();
	}
	
}
