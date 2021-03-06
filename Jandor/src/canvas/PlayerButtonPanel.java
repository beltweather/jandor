package canvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import session.User;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PLabel;
import ui.pwidget.PPanel;
import ui.pwidget.PSpinner;
import util.FriendUtil;
import util.ImageUtil;
import util.UserUtil;

public class PlayerButtonPanel extends AbstractCardLayerButtonPanel {
	
	private PCheckBox handViewableCheck;
	private PCheckBox deckViewableCheck;
	private PCheckBox graveyardViewableCheck;
	private PCheckBox exileViewableCheck;
	private PCheckBox opponentBoxCheck;
	
	private PSpinner lifeSpinner;
	
	private Map<String, Integer> commanderDamageByGUID;
	private Map<String, PSpinner> commanderDamageSpinnersByGUID;
	
	public PlayerButtonPanel(CardLayer layer) {
		super(layer);
	}
	
	@Override
	protected void init() {
		int topMargin = 5;
		int buttonMargin = 10;
		
		boolean handViewable = false;
		boolean deckViewable = false;
		boolean graveyardViewable = true;
		boolean exileViewable = true;
		boolean opponentBoxViewable = true;
		if(handViewableCheck != null) {
			handViewable = handViewableCheck.isSelected();
		}
		if(deckViewableCheck != null) {
			deckViewable = deckViewableCheck.isSelected();
		}
		if(graveyardViewableCheck != null) {
			graveyardViewable = graveyardViewableCheck.isSelected();
		}
		if(exileViewableCheck != null) {
			exileViewable = exileViewableCheck.isSelected();
		}
		if(opponentBoxCheck != null) {
			opponentBoxViewable = opponentBoxCheck.isSelected();
		}
		
		PButton untap = new PButton("Untap");
		untap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.untap();
			}
			
		});
		
		PButton draw = new PButton("Draw");
		draw.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.draw();
			}
			
		});
		
		PButton shuffle = new PButton("Shuffle");
		shuffle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.shuffle();
			}
			
		});
		
		PButton discardRandom = new PButton("Discard Random");
		discardRandom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.discardRandom();
			}
			
		});

		PButton newGame = new PButton("New Game");
		newGame.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JUtil.showConfirmYesNoCancelDialog(layer.getCanvas(), "Start New Game", "Are you sure you want to start a new game?")) {
					layer.reset();
				}
			}
			
		});
		
		PButton invite = new PButton("+ Invite");
		invite.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.getMenuBar().actionViewFriends();
			}
			
		});
		
		List<String> guids = new ArrayList<String>(FriendUtil.getConnectedUserGUIDS());
		Collections.sort(guids, new Comparator<String>() {

			@Override
			public int compare(String guidA, String guidB) {
				return UserUtil.getUserByGUID(guidA).getFirstName().compareTo(
					   UserUtil.getUserByGUID(guidB).getFirstName());
			}
			
		});

		PPanel disconnectPanel = new PPanel();
		PPanel commanderPanel = new PPanel();
		
		if(commanderDamageSpinnersByGUID == null) {
			commanderDamageSpinnersByGUID = new HashMap<String, PSpinner>();
			commanderDamageByGUID = new HashMap<String, Integer>();
		} else {
			commanderDamageSpinnersByGUID.clear();
			commanderDamageByGUID.clear();
		}
		
		boolean first = true;
		for(final String guid : guids) {
			final User user = UserUtil.getUserByGUID(guid);
			PButton db = new PButton(user.getFirstName());
			db.setIcon(new ImageIcon(ImageUtil.getCloseIcon()));
			
			db.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(JUtil.showConfirmDialog(PlayerButtonPanel.this, "Disconnect from " + user.getFirstName(), "Disconnect from " + user.getFirstName() + " " + user.getLastName() + "?")) {
						FriendUtil.disconnectFromFriend(guid);
						rebuild();
					}
				}
				
			});
			
			disconnectPanel.addc(db);
			disconnectPanel.c.insets(0,10);
			disconnectPanel.c.gridx++;
			
			if(!commanderDamageByGUID.containsKey(guid)) {
				commanderDamageByGUID.put(guid, 21);
			}
			
			PSpinner commanderLifeSpinner = new PSpinner(commanderDamageByGUID.get(guid), 0, 21) {

				@Override
				protected void handleChange(int value) {
					commanderDamageByGUID.put(guid, value);
					layer.flagChange();
				}
				
			};
			
			commanderPanel.addc(new PLabel((first ? "Commander Damage from " : ", ") + user.getInitials() + ": "));
			commanderPanel.c.gridx++;
			commanderPanel.c.insets(0,0,0,0);
			commanderPanel.addc(commanderLifeSpinner);
			commanderPanel.c.gridx++;
			commanderPanel.c.insets();
			
			commanderDamageSpinnersByGUID.put(guid, commanderLifeSpinner);
			
			if(first) {
				first = false;
			}
		}
		
		handViewableCheck = new PCheckBox("Reveal Hand");
		deckViewableCheck = new PCheckBox("Reveal Deck");
		graveyardViewableCheck = new PCheckBox("Reveal Graveyard");
		exileViewableCheck = new PCheckBox("Reveal Exiled");
		opponentBoxCheck = new PCheckBox("Outline Opponent's View");
		
		handViewableCheck.setSelected(handViewable);
		deckViewableCheck.setSelected(deckViewable);
		graveyardViewableCheck.setSelected(graveyardViewable);
		exileViewableCheck.setSelected(exileViewable);
		opponentBoxCheck.setSelected(opponentBoxViewable);
		
		handViewableCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.flagChange();
			}
			
		});
		
		deckViewableCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.flagChange();
			}
			
		});
		
		graveyardViewableCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.flagChange();
			}
			
		});
		
		exileViewableCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.flagChange();
			}
			
		});
		
		opponentBoxCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.repaint();
			}
			
		});

		lifeSpinner = new PSpinner(20, 0, Integer.MAX_VALUE) {

			@Override
			protected void handleChange(int value) {
				layer.flagChange();
			} 
			
		};
		
		
		lifeSpinner.setPreferredWidth(70);
		
		PPanel metaPanel = new PPanel();
		metaPanel.c.insets(topMargin);
		metaPanel.addc(newGame);
		metaPanel.c.insets(topMargin, buttonMargin);
		metaPanel.c.gridx++;
		metaPanel.addc(invite);
		metaPanel.c.gridx++;
		metaPanel.addc(disconnectPanel);
		metaPanel.c.insets(0,20);
		metaPanel.c.gridx++;
		metaPanel.addc(deckViewableCheck);
		metaPanel.c.insets(0,buttonMargin);
		metaPanel.c.gridx++;
		metaPanel.addc(handViewableCheck);
		metaPanel.c.gridx++;
		metaPanel.addc(exileViewableCheck);
		metaPanel.c.gridx++;
		metaPanel.c.insets(0,2*buttonMargin);
		metaPanel.addc(opponentBoxCheck);
		
		PPanel mainPanel = new PPanel();
		
		if(layer.isCommander()) {
			mainPanel.c.insets(topMargin, 0, 0, 30);
			mainPanel.addc(commanderPanel);
			mainPanel.c.gridx++;
		}
		
		mainPanel.c.insets(topMargin);
		mainPanel.addc(new PLabel("Life: "));
		mainPanel.c.gridx++;
		mainPanel.c.insets(topMargin, 0, 0, 20);
		mainPanel.addc(lifeSpinner);
		mainPanel.c.insets(topMargin, buttonMargin);
		mainPanel.c.gridx++;
		mainPanel.addc(untap);
		mainPanel.c.gridx++;
		mainPanel.addc(draw);
		mainPanel.c.gridx++;
		mainPanel.addc(shuffle);
		mainPanel.c.gridx++;
		mainPanel.addc(discardRandom);
		
		PPanel subPanel = new PPanel();
		subPanel.c.insets(topMargin, 0);
		subPanel.addc(shuffle);
		subPanel.c.insets(topMargin, buttonMargin);
		subPanel.c.gridx++;
		subPanel.addc(discardRandom);
		
		c.weaken();
		add(metaPanel, c);
		c.gridx++;
		fill();
		c.gridx++;
		c.weaken();
		add(mainPanel, c);
		//c.insets(0, 400);
		c.gridx++;
		fill();
		c.gridx++;
		add(subPanel, c);
	}
	
	public boolean isHandViewable() {
		return handViewableCheck == null ? false : handViewableCheck.isSelected();
	}
	
	public boolean isDeckViewable() {
		return deckViewableCheck == null ? false : deckViewableCheck.isSelected();
	}
	
	public boolean isGraveyardViewable() {
		return graveyardViewableCheck == null ? false : graveyardViewableCheck.isSelected();
	}
	
	public boolean isExileViewable() {
		return exileViewableCheck == null ? false : exileViewableCheck.isSelected();
	}
	
	public boolean isOpponentBoxViewable() {
		return opponentBoxCheck == null ? false : opponentBoxCheck.isSelected();
	}
	
	public int getLifeTotal() {
		return lifeSpinner.getIntValue();
	}
	
	public void setLifeTotal(int lifeTotal) {
		this.lifeSpinner.setValue(lifeTotal);
	}
	
	public Map<String, Integer> getCommanderDamageByGUID() {
		return this.commanderDamageByGUID;
	}
}
