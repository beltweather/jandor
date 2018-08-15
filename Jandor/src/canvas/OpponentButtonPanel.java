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
	
	private PLabel deckLabel;
	private PLabel handLabel;
	private PLabel graveyardLabel;
	private PLabel exileLabel;
	
	private PButton deckButton;
	private PButton handButton;
	private PButton graveyardButton;
	private PButton exileButton;
	
	private int deckCount = 0;
	private int handCount = 0;
	private int graveyardCount = 0;
	private int exileCount = 0;
	
	private boolean handViewable = false;
	private boolean deckViewable = false;
	private boolean graveyardViewable = true;
	private boolean exileViewable = true;
	
	private int lifeTotal;
	private PLabel lifeTotalLabel;
	
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
		
		deckLabel = new PLabel("Deck: 0");
		handLabel = new PLabel("Hand: 0");
		graveyardLabel = new PLabel("Graveyard: 0");
		exileLabel = new PLabel("Exile: 0");
		deckButton = new PButton("Deck: 0");
		handButton = new PButton("Hand: 0");
		graveyardButton = new PButton("Graveyard: 0");
		exileButton = new PButton("Exhile: 0");
		
		graveyardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				viewZone(ZoneType.GRAVEYARD);
			}
			
		});
		
		exileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				viewZone(ZoneType.EXILE);
			}
			
		});
		
		deckButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				viewZone(ZoneType.DECK);
			}
			
		});
		
		handButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				viewZone(ZoneType.HAND);
			}
			
		});
		
		lifeTotalLabel = new PLabel("");
		
		zonePanel.c.insets(0,10, 0, 20);
		zonePanel.addc(lifeTotalLabel);
		zonePanel.c.insets(0,10);
		zonePanel.c.gridx++;
		zonePanel.addc(deckLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(deckButton);
		zonePanel.c.gridx++;
		zonePanel.addc(handLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(handButton);
		zonePanel.c.gridx++;
		zonePanel.addc(graveyardLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(graveyardButton);
		zonePanel.c.gridx++;
		zonePanel.addc(exileLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(exileButton);
		
		deckButton.setVisible(false);
		handButton.setVisible(false);
		graveyardLabel.setVisible(false);
		exileLabel.setVisible(false);
		
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

	private void viewZone(ZoneType zone) {
		MultiplayerMessage message = layer.getOpponentMessage();
		if(message == null) {
			return;
		}
		CardList cards = new CardList();
		for(IRenderable r : message.getAllObjects()) {
			if(r instanceof Card && r.getRenderer().getZoneType() == zone) {
				cards.add((Card) r);
			}
		}
		layer.getMenuBar().actionSearch(zone.toString(), cards);
	}
	
	public void updateLabels() {
		MultiplayerMessage message = layer.getOpponentMessage();
		if(message == null) {
			deckLabel.setVisible(false);
			handLabel.setVisible(false);
			deckButton.setVisible(false);
			handButton.setVisible(false);
			graveyardButton.setVisible(false);
			exileButton.setVisible(false);
			lifeTotalLabel.setVisible(false);
			return;
		}

		deckLabel.setVisible(!message.isDeckViewable());
		handLabel.setVisible(!message.isHandViewable());
		graveyardLabel.setVisible(!message.isGraveyardViewable());
		exileLabel.setVisible(!message.isExileViewable());
		deckButton.setVisible(message.isDeckViewable());
		handButton.setVisible(message.isHandViewable());
		graveyardButton.setVisible(message.isGraveyardViewable());
		exileButton.setVisible(message.isExileViewable());
		lifeTotalLabel.setVisible(true);
		
		boolean shouldRevalidate = false;
		if((handViewable != message.isHandViewable()) || 
		   (deckViewable != message.isDeckViewable()) ||
		   (graveyardViewable != message.isGraveyardViewable()) ||
		   (exileViewable != message.isExileViewable())) {
			shouldRevalidate = true;
		}
		
		handViewable = message.isHandViewable();
		deckViewable = message.isDeckViewable();
		graveyardViewable = message.isGraveyardViewable();
		exileViewable = message.isExileViewable();
		
		int newDeckCount = 0;
		int newHandCount = 0;
		int newGraveyardCount = 0;
		int newExileCount = 0;
		
		for(IRenderable r : message.getAllObjects()) {
			if(r instanceof Card) {
				ZoneType z = r.getRenderer().getZoneType();
				switch(z) {
					case DECK:
						newDeckCount++;
						break;
					case HAND:
						newHandCount++;
						break;
					case GRAVEYARD:
						newGraveyardCount++;
						break;
					case EXILE:
						newExileCount++;
						break;
					default:
						break;
				}
			}
		}
		
		if(deckCount != newDeckCount) {
			deckCount = newDeckCount;
			deckLabel.setText("Deck: " + newDeckCount);
			deckButton.setText("Deck: " + newDeckCount);
			shouldRevalidate = true;
		}
		
		if(handCount != newHandCount) {
			handCount = newHandCount;
			handLabel.setText("Hand: " + newHandCount);
			handButton.setText("Hand: " + newHandCount);
			shouldRevalidate = true;
		}
		
		if(graveyardCount != newGraveyardCount) {
			graveyardCount = newGraveyardCount;
			graveyardLabel.setText("Graveyard: " + newGraveyardCount);
			graveyardButton.setText("Graveyard: " + newGraveyardCount);
			shouldRevalidate = true;
		}
		
		if(exileCount != newExileCount) {
			exileCount = newExileCount;
			exileLabel.setText("Exile: " + newExileCount);
			exileButton.setText("Exile: " + newExileCount);
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
