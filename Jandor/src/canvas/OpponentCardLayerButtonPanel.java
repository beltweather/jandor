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
import ui.pwidget.PButton;
import ui.pwidget.PLabel;
import ui.pwidget.PPanel;
import ui.pwidget.PRadio;
import util.FriendUtil;
import util.UserUtil;
import zone.ZoneType;

public class OpponentCardLayerButtonPanel extends AbstractCardLayerButtonPanel {
	
	private ButtonGroup opponentRadioGroup;
	private Map<String, PRadio> opponentRadiosByGUID;
	
	private PLabel deckLabel;
	private PLabel handLabel;
	private PButton graveyardLabel;
	private PButton exileLabel;
	
	private int deckCount = 0;
	private int handCount = 0;
	private int graveyardCount = 0;
	private int exileCount = 0;
	
	public OpponentCardLayerButtonPanel(CardLayer layer) {
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
				layer.opponentLayer = null;
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
					layer.opponentLayer = FriendUtil.getFriendLayer(guid);
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
		graveyardLabel = new PButton("Graveyard: 0");
		exileLabel = new PButton("Exhile: 0");
		
		graveyardLabel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(layer.opponentLayer == null) {
					return;
				}
				CardList graveyard = new CardList();
				for(IRenderable r : layer.opponentLayer.getAllObjects()) {
					if(r instanceof Card && r.getRenderer().getZoneType() == ZoneType.GRAVEYARD) {
						graveyard.add((Card) r);
					}
				}
				layer.getMenuBar().actionSearch("Graveyard", graveyard);
			}
			
		});
		
		exileLabel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(layer.opponentLayer == null) {
					return;
				}
				CardList exile = new CardList();
				for(IRenderable r : layer.opponentLayer.getAllObjects()) {
					if(r instanceof Card && r.getRenderer().getZoneType() == ZoneType.EXILE) {
						exile.add((Card) r);
					}
				}
				layer.getMenuBar().actionSearch("Exile", exile);
			}
			
		});
		
		zonePanel.addc(deckLabel);
		zonePanel.c.insets(0,10);
		zonePanel.c.gridx++;
		zonePanel.addc(handLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(graveyardLabel);
		zonePanel.c.gridx++;
		zonePanel.addc(exileLabel);
		
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
			layer.opponentLayer = FriendUtil.getFriendLayer(guid);
			layer.repaint();
		}

		updateLabels();
	}
	
	public void updateLabels() {
		if(layer.opponentLayer == null) {
			deckLabel.setVisible(false);
			handLabel.setVisible(false);
			graveyardLabel.setVisible(false);
			exileLabel.setVisible(false);
			return;
		}

		deckLabel.setVisible(true);
		handLabel.setVisible(true);
		graveyardLabel.setVisible(true);
		exileLabel.setVisible(true);
		
		int newDeckCount = 0;
		int newHandCount = 0;
		int newGraveyardCount = 0;
		int newExileCount = 0;
		
		for(IRenderable r : layer.opponentLayer.getAllObjects()) {
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
		}
		
		if(handCount != newHandCount) {
			handCount = newHandCount;
			handLabel.setText("Hand: " + newHandCount);
		}
		
		if(graveyardCount != newGraveyardCount) {
			graveyardCount = newGraveyardCount;
			graveyardLabel.setText("Graveyard: " + newGraveyardCount);
		}
		
		if(exileCount != newExileCount) {
			exileCount = newExileCount;
			exileLabel.setText("Exile: " + newExileCount);
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
