package canvas;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;

import deck.Card;
import deck.CardList;
import multiplayer.MultiplayerMessage;
import session.User;
import ui.pwidget.PButton;
import ui.pwidget.PLabel;
import ui.pwidget.PPanel;
import ui.pwidget.PRadio;
import util.FriendUtil;
import util.UserUtil;
import zone.ZoneType;

public class OpponentButtonPanel extends AbstractCardLayerButtonPanel {
	
	private ButtonGroup opponentRadioGroup;
	private Map<String, PRadio> opponentRadiosByGUID;
	
	private Map<ZoneType, PLabel> labelsByZone;
	private Map<ZoneType, PButton> buttonsByZone;
	private Map<ZoneType, Integer> countsByZone;
	private Map<ZoneType, Boolean> viewableByZone;
	
	private Map<String, Integer> commanderDamageByGUID = new HashMap<String, Integer>();
	
	private int lifeTotal;
	private PLabel lifeTotalLabel;
	
	private PLabel commanderDamageLabel;
	
	public OpponentButtonPanel(CardLayer layer) {
		super(layer);
	}
	
	@Override
	protected void init() {
		String previousGUID = getOpponentGUID();

		PPanel buttonPanel = new PPanel();

		List<String> guids = new ArrayList<String>(FriendUtil.getConnectedUserGUIDS());
		Collections.sort(guids, new Comparator<String>() {

			@Override
			public int compare(String guidA, String guidB) {
				return UserUtil.getUserByGUID(guidA).getFirstName().compareTo(
					   UserUtil.getUserByGUID(guidB).getFirstName());
			}
			
		});
		PRadio radioNone = new PRadio("None");
		radioNone.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.setOpponentMessage(null);
				updateLabels();
				layer.repaint();
			}
			
		});
		
		opponentRadiosByGUID = new HashMap<String, PRadio>();
		opponentRadioGroup = new ButtonGroup();
		opponentRadioGroup.add(radioNone);
		
		buttonPanel.c.insets = new Insets(0,5,0,0);
		buttonPanel.addc(new PLabel("Opponent:"));
		buttonPanel.c.gridx++;
		buttonPanel.addc(radioNone);
		buttonPanel.c.gridx++;
		for(final String guid : guids) {
			final PRadio radio = new PRadio(UserUtil.getUserByGUID(guid).getFirstName());
			radio.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					layer.setOpponentMessage(FriendUtil.getOpponentMessage(guid));
					updateLabels();
					layer.repaint();
				}
				
			});
			
			opponentRadioGroup.add(radio);
			buttonPanel.addc(radio);
			buttonPanel.c.gridx++;
			opponentRadiosByGUID.put(guid, radio);
		
			if(previousGUID == null || (previousGUID != null && guid.equals(previousGUID))) {
				previousGUID = guid;
				radio.setSelected(true);
			}
		}
		radioNone.setSelected(previousGUID == null);
		
		PPanel otherPanel = new PPanel();
		PPanel zonePanel = new PPanel();
		
		lifeTotalLabel = new PLabel("");
		commanderDamageLabel = new PLabel("");
		
		zonePanel.c.insets(0, 10, 0, 20);
		zonePanel.addc(commanderDamageLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(lifeTotalLabel);
		zonePanel.c.insets(0,10);
		
		labelsByZone = new HashMap<ZoneType, PLabel>();
		buttonsByZone = new HashMap<ZoneType, PButton>();
		countsByZone = new HashMap<ZoneType, Integer>();
		viewableByZone = new HashMap<ZoneType, Boolean>();
		
		addZone(zonePanel, ZoneType.DECK, true);
		addZone(zonePanel, ZoneType.HAND, true);
		addZone(zonePanel, ZoneType.GRAVEYARD, true);
		addZone(zonePanel, ZoneType.EXILE, true);
		addZone(zonePanel, ZoneType.BATTLEFIELD, true);
		addZone(zonePanel, ZoneType.COMMANDER, true);
		
		addc(otherPanel);
		c.gridx++;
		fill();
		c.gridx++;
		addc(zonePanel);
		c.gridx++;
		fill();
		c.gridx++;
		addc(buttonPanel);
		
		String guid = getOpponentGUID();
		if(guid != null) {
			layer.setOpponentMessage(FriendUtil.getOpponentMessage(guid));
			layer.repaint();
		}

		updateLabels();
	}
	
	private void addZone(PPanel zonePanel, final ZoneType zone, boolean isButton) {
		PLabel label = new PLabel(zone + ": " + 0);
		PButton button = new PButton(zone + ": " + 0);
		
		labelsByZone.put(zone, label);
		buttonsByZone.put(zone, button);
		
		if(isButton) {
			label.setVisible(false);
		} else {
			button.setVisible(false);
		}
		
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				viewZone(zone);
			}
			
		});
		
		zonePanel.c.gridx++;
		zonePanel.addc(label);
		zonePanel.c.gridx++;
		zonePanel.addc(button);
	}

	private void viewZone(ZoneType zone) {
		MultiplayerMessage message = layer.getOpponentMessage();
		if(message == null) {
			return;
		}
		CardList cards = new CardList();
		for(IRenderable r : message.getAllObjects()) {
			if(r instanceof Card && r.getRenderer().getZoneType() == zone) {
				if(zone == ZoneType.BATTLEFIELD && !r.getRenderer().isFaceUp()) {
					continue;
				}
				cards.add((Card) r);
			}
		}
		layer.getMenuBar().actionSearch(zone.toString(), cards);
	}
	
	public void updateLabels() {
		MultiplayerMessage message = layer.getOpponentMessage();
		if(message == null) {
			
			for(ZoneType zone : labelsByZone.keySet()) {
				labelsByZone.get(zone).setVisible(false);
				buttonsByZone.get(zone).setVisible(false);
			}
			
			lifeTotalLabel.setVisible(false);
			commanderDamageLabel.setVisible(false);
			return;
		}
		
		lifeTotalLabel.setVisible(true);
		commanderDamageLabel.setVisible(layer.isCommander());
		
		boolean shouldRevalidate = false;
		for(ZoneType zone : message.getViewableByZone().keySet()) {
			boolean viewable = message.isViewable(zone);
			if(!viewableByZone.containsKey(zone) || viewableByZone.get(zone) != viewable) {
				viewableByZone.put(zone, message.isViewable(zone));
				shouldRevalidate = true;
			}
			labelsByZone.get(zone).setVisible(!viewable);
			buttonsByZone.get(zone).setVisible(viewable);
		}
		
		Map<ZoneType, Integer> newCountsByZone = new HashMap<ZoneType, Integer>();
		for(IRenderable r : message.getAllObjects()) {
			if(r instanceof Card) {
				ZoneType z = r.getRenderer().getZoneType();
				if(!newCountsByZone.containsKey(z)) {
					newCountsByZone.put(z, 1);
				} else {
					newCountsByZone.put(z, newCountsByZone.get(z) + 1);
				}
			}
		}
		
		for(ZoneType zone : labelsByZone.keySet()) {
			int oldCount = (countsByZone.containsKey(zone) ? countsByZone.get(zone) : 0);
			int newCount = (newCountsByZone.containsKey(zone) ? newCountsByZone.get(zone) : 0);
			if(oldCount != newCount) {
				countsByZone.put(zone, newCount);
				labelsByZone.get(zone).setText(zone + ": " + newCount);
				buttonsByZone.get(zone).setText(zone + ": " + newCount);
				shouldRevalidate = true;
			}
		}
		
		boolean shouldUpdateComm = false;
		if(commanderDamageByGUID == null) {
			commanderDamageByGUID = new HashMap<String, Integer>();
		}
		
		if(message.getCommanderDamageByGUID() != null) {
			if(commanderDamageByGUID.size() != message.getCommanderDamageByGUID().size()) {
				shouldUpdateComm = true;
			} else {
				for(String guid : message.getCommanderDamageByGUID().keySet()) {
					if(!commanderDamageByGUID.containsKey(guid)) {
						shouldUpdateComm = true;
					} else if(commanderDamageByGUID.get(guid) != message.getCommanderDamageByGUID().get(guid)) {
						shouldUpdateComm = true;
					}
				}
			}
		}
		
		if(shouldUpdateComm) {
			commanderDamageByGUID = message.getCommanderDamageByGUID();
			List<String> guids = new ArrayList<String>(commanderDamageByGUID.keySet());
			Collections.sort(guids, new Comparator<String>() {

				@Override
				public int compare(String guidA, String guidB) {
					User userA = UserUtil.getUserByGUID(guidA);
					User userB = UserUtil.getUserByGUID(guidB);
					if(userA == null && userB == null) {
						return 0;
					} else if(userA == null) {
						return -1;
					} else if(userB == null) {
						return 1;
					}
					return userA.getFirstName().compareTo(userB.getFirstName());
				}
				
			});
			
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for(String guid : guids) {
				User user = UserUtil.getUserByGUID(guid);
				if(user == null) {
					continue;
				}
				if(first) {
					first = false;
					sb.append("Commander Damage from ");
				} else {
					sb.append(", ");
				}
				sb.append(user.getInitials() + ": " + commanderDamageByGUID.get(guid));
			}
			commanderDamageLabel.setText(sb.toString());
			shouldRevalidate = true;
		}
		
		if(message.getLifeTotal() != lifeTotal) {
			lifeTotal = message.getLifeTotal();
			lifeTotalLabel.setText("Life: " + lifeTotal);
			shouldRevalidate = true;
		}
		
		if(shouldRevalidate) {
			revalidate();
		}
		
	}
	
	public String getOpponentGUID() {
		if(opponentRadiosByGUID == null) {
			return null;
		}
		for(String guid : opponentRadiosByGUID.keySet()) {
			if(opponentRadiosByGUID.get(guid).isSelected()) {
				return guid;
			}
		}
		return null;
	}
	
	public void setOpponentGUID(String guid) {
		if(opponentRadiosByGUID.containsKey(guid)) {
			opponentRadiosByGUID.get(guid).setSelected(true);
		}
	}
	
}
