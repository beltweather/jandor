package draft;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import session.Contact;
import session.DeckHeader;
import session.DraftContent;
import session.DraftHeader;
import session.Session;
import session.User;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PSpinner;
import ui.view.CollectionEditorView;
import ui.view.DeckEditorView;
import ui.view.DraftEditorView;
import user.UserSearchPanel;
import util.CardUtil;
import util.DebugUtil;
import util.DraftUtil;
import util.IDUtil;
import util.LoginUtil;
import util.MailUtil;
import accordion.PAccordion;
import accordion.PAccordionPanel;
import deck.Deck;

public class ConfigureDraftDialog extends PPanel {

	protected int deckId;
	protected JComboBox<String> typeCombo;
	protected List<UserSearchPanel> userPanels = new ArrayList<UserSearchPanel>();
	protected List<PButton> removeButtons = new ArrayList<PButton>();
	protected List<SetPanel> setPanels = new ArrayList<SetPanel>();
	protected List<PButton> setRemoveButtons = new ArrayList<PButton>();
	protected PPanel contactsPanel;
	protected PPanel setPanel;
	protected PPanel setWrapperPanel;
	protected PPanel boosterPanel;
	protected PPanel randomPanel;
	
	protected PSpinner mythicSpinner;
	protected PSpinner rareSpinner;
	protected PSpinner uncommonSpinner;
	protected PSpinner commonSpinner;
	protected PSpinner landSpinner;
	
	protected PSpinner boosterSpinner;
	protected PSpinner randomSpinner;
	
	protected JLabel mythicLabel;
	protected JLabel rareLabel;
	protected JLabel uncommonLabel;
	protected JLabel commonLabel;
	protected JLabel landLabel;
	
	protected PCheckBox landCheck;
	protected PCheckBox mythicCheck;
	protected PCheckBox foilCheck;
	protected CollectionEditorView view;
	
	public ConfigureDraftDialog(CollectionEditorView view) {
		this(view, IDUtil.NONE);
	}
	
	public ConfigureDraftDialog(CollectionEditorView view , int deckId) {
		super();
		this.view = view;
		this.deckId = deckId;
		init();
	}
	
	private boolean hasDeck() {
		return deckId != IDUtil.NONE;
	}
	
	private void init() {
		typeCombo = new JComboBox<String>();
		
		if(hasDeck()) {
			typeCombo.addItem("Booster");
			typeCombo.addItem("Random");
			typeCombo.setSelectedItem("Booster");
		} else {
			typeCombo.addItem("Set Booster");
			typeCombo.setSelectedItem("Set Booster");
		}
		
		typeCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean isSetBooster = isSetBooster();
				boolean isBooster = isBooster();
				
				if(isSetBooster) {
					setWrapperPanel.setVisible(true);
					boosterPanel.setVisible(false);
					randomPanel.setVisible(false);
				} else if(isBooster) {
					setWrapperPanel.setVisible(false);
					boosterPanel.setVisible(true);
					randomPanel.setVisible(false);
				} else {
					setWrapperPanel.setVisible(false);
					boosterPanel.setVisible(false);
					randomPanel.setVisible(true);
				}
				
				resize();
			}
			
		});
		
		setPanel = new PPanel();
		
		boosterPanel = new PPanel();
		boosterSpinner = buildSpinner();
		boosterSpinner.setValue(3);
		
		randomPanel = new PPanel();
		randomSpinner = buildSpinner();
		randomSpinner.setValue(14);
		randomPanel.addc(new JLabel("Total Cards: "));
		randomPanel.c.gridx++;
		randomPanel.addc(randomSpinner);
		
		contactsPanel = new PPanel();
		
		PButton addContactButton = new PButton("+");
		addContactButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				appendContactPanel();
				resize();
			}
			
		});
		addContactButton.setPreferredSize(new Dimension(16,16));
		
		PButton addSetButton = new PButton("+");
		addSetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				appendSetPanel();
				resize();
			}
			
		});
		addSetButton.setPreferredSize(new Dimension(16,16));
		
		PPanel configurePanel = new PPanel();
		configurePanel.c.anchor = G.WEST;
		configurePanel.c.insets(10, 10, 10, 10);
		configurePanel.addc(new JLabel("Type of Packs:"));
		configurePanel.c.gridx++;
		configurePanel.addc(typeCombo);
		configurePanel.c.gridx++;
		
		if(hasDeck()) {
			configurePanel.addc(new JLabel("Total Packs:"));
			configurePanel.c.gridx++;
			configurePanel.addc(boosterSpinner);
		}
		
		configurePanel.c.gridx++;
		configurePanel.c.strengthen();
		configurePanel.addcStrut();
		configurePanel.c.weaken();
		configurePanel.c.gridx -= 3;
		configurePanel.c.gridy++;
		configurePanel.c.gridwidth = 4;
		
		setWrapperPanel = new PPanel();
		setWrapperPanel.c.anchor = G.SOUTH;
		setWrapperPanel.addc(setPanel);
		setWrapperPanel.c.gridx++;
		setWrapperPanel.c.insets(0, 20);
		setWrapperPanel.addc(addSetButton);
		
		configurePanel.addc(setWrapperPanel);
		configurePanel.c.gridy++;
		configurePanel.addc(boosterPanel);
		configurePanel.c.gridy++;
		configurePanel.addc(randomPanel);

		boolean isSetBooster = isSetBooster();
		boolean isBooster = isBooster();
		
		if(isSetBooster) {
			setWrapperPanel.setVisible(true);
			boosterPanel.setVisible(false);
			randomPanel.setVisible(false);
		} else if(isBooster) {
			setWrapperPanel.setVisible(false);
			boosterPanel.setVisible(true);
			randomPanel.setVisible(false);
		} else {
			setWrapperPanel.setVisible(false);
			boosterPanel.setVisible(false);
			randomPanel.setVisible(true);
		}
		
		c.weaken();
		c.anchor = G.SOUTHWEST;
		addc(configurePanel);
		c.insets(10, 10);
		c.gridy++;
		addc(new JLabel("Send an Invite"));
		c.gridy++;
		c.gridwidth = 2;
		addc(contactsPanel);
		c.gridx++;
		addc(addContactButton);
		c.gridx--;
		c.gridy++;
		c.strengthen();
		addcStrut();
		
		initSetPanels();
		initBoosterPanel();
		initContactPanels();
	}
	
	private boolean isBooster() {
		String type = typeCombo.getSelectedItem().toString();
		return type.equals("Booster");
	}
	
	private boolean isSetBooster() {
		String type = typeCombo.getSelectedItem().toString();
		return type.equals("Set Booster");
	}
	
	private void resize() {
		revalidate();
		repaint();
		getParentDialog().revalidate();
		getParentDialog().pack();
		getParentDialog().repaint();
	}
	
	private JDialog getParentDialog() {
		Component parent = SwingUtilities.getRoot(this);
		if(parent instanceof JDialog) {
			return (JDialog) parent;
		}
		return null;
	}
	
	private void initBoosterPanel() {
		mythicSpinner = buildSpinner();
		rareSpinner = buildSpinner();
		uncommonSpinner = buildSpinner();
		commonSpinner = buildSpinner();
		landSpinner = buildSpinner();
		
		rareSpinner.setValue(1);
		uncommonSpinner.setValue(3);
		commonSpinner.setValue(10);
		
		landCheck = new PCheckBox("Include Non-Basic Lands with Rares and Uncommons");
		landCheck.setSelected(true);
		landCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				landLabel.setVisible(!landCheck.isSelected());
				landSpinner.setVisible(!landCheck.isSelected());
				resize();
			}
			
		});
		
		mythicCheck = new PCheckBox("Include Mythics with Rares");
		mythicCheck.setSelected(true);
		mythicCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				mythicLabel.setVisible(!mythicCheck.isSelected());
				mythicSpinner.setVisible(!mythicCheck.isSelected());
				resize();
			}
			
		});
		
		foilCheck = new PCheckBox("Include Foils");
		foilCheck.setSelected(true);
		foilCheck.setToolTipText("A common will be replaced by a random card 25% of the time.");
		
		boosterPanel.c.anchor = G.EAST;
		boosterPanel.c.insets(10);
		landLabel = addSpinner("Non-Basic Lands: ", landSpinner);
		mythicLabel = addSpinner("Mythics: ", mythicSpinner);
		rareLabel = addSpinner("Rares: ", rareSpinner);
		uncommonLabel = addSpinner("Uncommons: ", uncommonSpinner);
		commonLabel = addSpinner("Commons: ", commonSpinner);
		
		boosterPanel.c.strengthen();
		boosterPanel.c.gridx = 2;
		boosterPanel.addcStrut();
		
		boosterPanel.c.weaken();
		boosterPanel.c.gridx = 0;
		boosterPanel.c.anchor = G.WEST;
		boosterPanel.c.gridwidth = 3;
		boosterPanel.addc(mythicCheck);
		boosterPanel.c.gridy++;
		boosterPanel.addc(landCheck);
		boosterPanel.c.gridy++;
		boosterPanel.addc(foilCheck);
		
		mythicSpinner.setVisible(false);
		mythicLabel.setVisible(false);
		landSpinner.setVisible(false);
		landLabel.setVisible(false);
	}
	
	private PSpinner buildSpinner() {
		return new PSpinner(0, 0, 99) {

			@Override
			protected void handleChange(int value) {
				
			}
			
		};
	}
	
	private JLabel addSpinner(String name, PSpinner spinner) {
		JLabel label = new JLabel(name);
		boosterPanel.addc(label);
		boosterPanel.c.gridx++;
		boosterPanel.addc(spinner);
		boosterPanel.c.gridx--;
		boosterPanel.c.gridy++;
		return label;
	}
	
	private void initContactPanels() {
		appendContactPanel();
	}
	
	private void appendContactPanel() {
		final UserSearchPanel p = new UserSearchPanel();
		final PButton removeButton = new PButton("-");
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				contactsPanel.remove(p);
				userPanels.remove(p);
				contactsPanel.remove(removeButton);
				removeButtons.remove(removeButton);
				resize();
				
				if(removeButtons.size() == 1) {
					removeButtons.get(0).setVisible(false);
				}
			}
			
		});
		removeButton.setPreferredSize(new Dimension(16, 16));
		removeButtons.add(removeButton);
		userPanels.add(p);
			
		contactsPanel.c.gridy++;
		contactsPanel.c.insets(10);
		contactsPanel.addc(removeButton);
		contactsPanel.c.gridx++;
		contactsPanel.c.insets(10, 10);
		contactsPanel.addc(p);
		contactsPanel.c.insets();
		contactsPanel.c.gridx--;
		
		if(removeButtons.size() == 1) {
			removeButtons.get(0).setVisible(false);
		} else {
			for(PButton button : removeButtons) {
				button.setVisible(true);
			}
		}
	}
	
	private void initSetPanels() {
		appendSetPanel();
	}
	
	private void appendSetPanel() {
		final SetPanel p = new SetPanel(setPanels.size() == 0 ? 3 : 1);
		final PButton removeButton = new PButton("-");
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPanel.remove(p);
				setPanels.remove(p);
				setPanel.remove(removeButton);
				setRemoveButtons.remove(removeButton);
				resize();
				
				if(setRemoveButtons.size() == 1) {
					setRemoveButtons.get(0).setVisible(false);
				}
			}
			
		});
		removeButton.setPreferredSize(new Dimension(16, 16));
		setRemoveButtons.add(removeButton);
		setPanels.add(p);
			
		setPanel.c.gridy++;
		setPanel.c.insets(10);
		setPanel.addc(removeButton);
		setPanel.c.gridx++;
		setPanel.c.insets(10, 10);
		setPanel.addc(p);
		setPanel.c.insets();
		setPanel.c.gridx--;
		
		if(setRemoveButtons.size() == 1) {
			setRemoveButtons.get(0).setVisible(false);
		} else {
			for(PButton button : setRemoveButtons) {
				button.setVisible(true);
			}
		}
	}
	
	public DeckHeader getDeckHeader() {
		return Session.getInstance().getDeckHeader(deckId);
	}
	
	private JLabel buildLabel(String text) {
		return new JLabel("<html><div width=\"200px\">" + text + "</div></html>");
	}
	
	private int initDraft(List<User> users, List<String> toEmails) {
		User user = LoginUtil.getUser();
		
		List<String> userGUIDs = new ArrayList<String>();
		userGUIDs.add(user.getGUID());
		for(User u : users) {
			userGUIDs.add(u.getGUID());
		}
		
		// Create Draft object based on spinners
		DraftHeader draft = new DraftHeader();
		draft.newId();
		draft.setTimeFirstCreated(System.currentTimeMillis());
		draft.setAuthor(user.getEmail());
		draft.setAuthorGUID(user.getGUID());
		draft.setAuthorUsername(user.getUsername());
		draft.setDeckId(deckId);
		draft.setTurn(0);
		draft.setRound(0);
		draft.setFinished(false);
		List<String> userEmails = new ArrayList<String>();
		userEmails.add(user.getEmail());
		userEmails.addAll(toEmails);
		draft.setUserGUIDs(userGUIDs);
		draft.setUserEmails(userEmails);
		String typeStr = typeCombo.getSelectedItem().toString();
		if(typeStr.equals("Set Booster")) {
			draft.setType(DraftHeader.TYPE_SET_BOOSTER);
		} else if(typeStr.equals("Booster")) {
			draft.setType(DraftHeader.TYPE_BOOSTER);
		} else {
			draft.setType(DraftHeader.TYPE_RANDOM);
		}
		
		if(draft.getType() == DraftHeader.TYPE_BOOSTER || draft.getType() == DraftHeader.TYPE_RANDOM) {
		
			draft.setPacks(boosterSpinner.getIntValue());
			draft.setLands(landSpinner.getIntValue());
			draft.setMythics(mythicSpinner.getIntValue());
			draft.setRares(rareSpinner.getIntValue());
			draft.setUncommons(uncommonSpinner.getIntValue());
			draft.setCommons(commonSpinner.getIntValue());
			draft.setIncludeFoils(foilCheck.isSelected());
			draft.setIncludeLandsAsRarities(landCheck.isSelected());
			draft.setIncludeMythicsAsRares(mythicCheck.isSelected());
			
		} else if(draft.getType() == DraftHeader.TYPE_SET_BOOSTER) {
		
			List<String> setPacks = new ArrayList<String>();
			for(SetPanel setPanel : setPanels) {
				String set = setPanel.getSetName();
				if(!set.isEmpty()) {
					for(int i = 0; i < setPanel.getPackCount(); i++) {
						setPacks.add(CardUtil.getSetId(set));
					}
				}
			}
			draft.setSetPacks(setPacks);
			draft.setPacks(draft.getSetPacks().size());
		}
		
		int totalCards = 0;
		if(draft.getType() == DraftHeader.TYPE_RANDOM) {
			totalCards = randomSpinner.getIntValue();
		} else if(draft.getType() == DraftHeader.TYPE_BOOSTER) {
			totalCards += draft.getCommons();
			totalCards += draft.getUncommons();
			totalCards += draft.getRares();
			if(!draft.isIncludeMythicsAsRares()) {
				totalCards += draft.getMythics();
			}
			if(!draft.isIncludeLandsAsRarities()) {
				totalCards += draft.getLands();
			}
		} else if(draft.getType() == DraftHeader.TYPE_SET_BOOSTER) {
			totalCards = CardUtil.getBooster(draft.getSetPacks().get(0)).length(); // XXX Not quite right because of different set combinations
		}
		draft.setTotalCards(totalCards);
		
		DraftContent content = new DraftContent(draft.getId(), hasDeck() ? Session.getInstance().getDeck(deckId) : new Deck("Set Draft"));
		
		// Save Draft object
		draft.save();
		content.save();
		
		return draft.getId();
	}
	
	public void showDialog() {
		User user = LoginUtil.getUser();
		if(DebugUtil.OFFLINE_MODE) {
			JUtil.showWarningDialog(null, "Cannot Draft Offline", buildLabel("Cannot draft while offline. Please connect and try again."));
			return;
		}
		
		if(!user.hasEmail()) {
			JUtil.showWarningDialog(null, "No User Registered", buildLabel("Please register in the top right of the Jandor window to start a draft."));
			return;
		}
		
		DeckHeader header = getDeckHeader();
		if(JUtil.showConfirmDialog(null, header == null ? "Start Draft from Set" : "Start Draft from Deck \"" + header.getName() + "\"", this)) {
			List<User> users = new ArrayList<User>();
			List<String> toEmails = new ArrayList<String>();
			for(UserSearchPanel p : userPanels) {
				String text = p.getText();
				if(text.isEmpty()) {
					continue;
				}
				User pUser = p.getUser();
				if(pUser == null) {
					JUtil.showWarningDialog(null, "Invalid Recipient Email", buildLabel("Jandor cannot find the user \"" + text + "\" you've listed. Please make sure you're sending to an existing user."));
					showDialog();
					return;
				}
				users.add(pUser);
				toEmails.add(pUser.getEmail());
			}
			
			DraftUtil.cleanupOldDrafts();
			
			int draftId = initDraft(users, toEmails);
			
			// Open draft view
			PAccordion accordion = view.getAccordion();
			for(PAccordionPanel p : accordion.getAccordionPanels()) {
				p.contract();
			}
			DeckEditorView deckEditorView = DraftEditorView.addDraftEditorView(accordion, draftId, IDUtil.NONE, view);
			accordion.rebuild();
			deckEditorView.flagModified();
			
			// Send draft invitation to all contacts and listen
			MailUtil.sendDraftToDrive(users, draftId);
		}
	}
	
}
