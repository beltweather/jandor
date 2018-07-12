package ui.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import session.DeckHeader;
import session.Session;
import ui.pwidget.PPanel;
import util.ShuffleType;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;
import accordion.PAccordion;
import accordion.PAccordionData;
import analysis.LandSimulation;
import analysis.SimResult;
import analysis.SimResultList;
import deck.Card;

public class DeckAnalysisView extends JandorView {

	public static final String DEFAULT_TITLE = "Analysis";
	
	public static void addAnalysisView(PAccordion accordion, DeckEditorView parent) {
		parent.getAccordionData().getAccordionPanel().contractAll();

		final PAccordionData analViewData = new PAccordionData("Analyze");
		DeckAnalysisView analView = new DeckAnalysisView("Analyze", parent.deckId);
		analViewData.setComponent(analView);
		analViewData.setDefaultExpanded(true);
		analViewData.setRemoveable(true);
		
		analView.setDeck(parent.getDeck()); // XXX Change this to deck id
		accordion.add(analViewData, parent.getAccordionData());
	}
	
	protected JComboBox<ShuffleType> shuffleTypeCombo;
	protected JCheckBox showManaScrewCheck;
	protected boolean showManaScrew = true;
	protected int deckId;
	
	public DeckAnalysisView(String name, int deckId) {
		super(name);
		this.deckId = deckId;
		rebuild();
		
		SessionEventManager.addListener(DeckHeader.class, deckId, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				rebuild();
			}
			
		});
	}
	
	@Override
	protected void rebuild() {
		removeAll();
		deck = Session.getInstance().getDeck(deckId);
		
		int handSize = 7;

		c.strengthen();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(20, 5, 20, 5);
		
		if(shuffleTypeCombo != null) {
			shuffleType = (ShuffleType) shuffleTypeCombo.getSelectedItem();
		}
		deck.shuffle(shuffleType);
		
		if(showManaScrewCheck != null) {
			showManaScrew = showManaScrewCheck.isSelected();
		}
		
		shuffleTypeCombo = new JComboBox<ShuffleType>();
		shuffleTypeCombo.addItem(ShuffleType.PLAYER);
		shuffleTypeCombo.addItem(ShuffleType.RANDOM);
		shuffleTypeCombo.setSelectedItem(shuffleType);
		
		showManaScrewCheck = new JCheckBox("Show Mana Screw");
		showManaScrewCheck.setOpaque(false);
		showManaScrewCheck.setForeground(Color.WHITE);
		showManaScrewCheck.setSelected(showManaScrew);
		showManaScrewCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				redraw();
			}
			
		});
		showManaScrewCheck.setToolTipText("<html><div width='200px'>If enabled, will highlight turns in red where you are behind on land drops, unless you already have as many lands as the converted mana cost of the highest mana cost card in your deck.</div></html>");

		JButton shuffle = new JButton("Redraw");
		shuffle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				redraw();
			}
			
		});
		
		JPanel p = new JPanel();
		p.add(shuffle);
		p.add(shuffleTypeCombo);
		p.add(showManaScrewCheck);
		
		c.gridx = 2;
		c.gridwidth = 3;
		c.insets = new Insets(20, -30, 100, 5);
		c.anchor = GridBagConstraints.CENTER;
		add(p, c);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(20, 5, 20, 5);
		c.gridx = 0;
		c.gridy++;
		
		int maxCmc = deck.getMaxConvertedManaCost();
		
		int landCount = 0;
		List<Card> cards = deck;
		
		if(deck.size() < handSize) {
			return;
		}
		
		List<Card> hand = new ArrayList<Card>(deck.subList(0, handSize));
		Collections.sort(hand, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				if(o1.isLand() && !o2.isLand()) {
					return -1;
				} else if(!o1.isLand() && o2.isLand()) {
					return 1;
				}
				
				if(o1.getConvertedManaCost() == o2.getConvertedManaCost()) {
					return o1.getName().compareTo(o2.getName());
				}
				return o1.getConvertedManaCost() - o2.getConvertedManaCost();
			}
			
		});
		
		for(int i = 0; i < handSize; i++) {
			
			Card card = hand.get(i);
			if(card.isLand()) {
				landCount++;
			}
			
			JLabel cardLabel = new JLabel(new ImageIcon(card.getImage()));
			if(i == 0) {
				c.insets = new Insets(20, 50, 20, 0);
				add(cardLabel, c);
				c.insets = new Insets(20, 5, 20, 5);
			} else if(i == handSize - 1) {
				c.insets = new Insets(20, 5, 20, 50);
				add(cardLabel, c);
				c.insets = new Insets(20, 5, 20, 5);
			} else {
				add(cardLabel, c);
			}
			c.gridx++;
		}
		c.gridx = 2;
		c.gridy++;
		c.insets = new Insets(0,5,0,5);
		

		int rows = 18;
		int cols = (int) Math.round(cards.size() / (double) rows);
		int startCol = (handSize - cols) / 2;
		int startRow = c.gridy;
		c.gridx = startCol;
		for(int i = handSize; i < cards.size(); i++) {
			Card card = cards.get(i);
			if(card.isLand()) {
				landCount++;
			}
			int turn = i - handSize + 1;
			String turnStr;
			if(turn < 10) {
				turnStr = "0" + turn;
			} else {
				turnStr = "" + turn;
			}
			JLabel cardLabel = new JLabel(turnStr + ": " + card.getName() + (card.isLand() ? "" : ""));
			if(turn > landCount && landCount < maxCmc && showManaScrew) {
				cardLabel.setForeground(Color.RED);
			} else {
				cardLabel.setForeground(Color.WHITE);
			}
			setTooltip(cardLabel, card);
			add(cardLabel, c);
			
			c.gridy++;
			if(c.gridy - startRow >= rows) {
				c.gridy = startRow;
				c.gridx++;
			}
			
		}
		
		revalidate();
	}
	
	private void setTooltip(JComponent comp, Card card) {
		final String html = "<html><body>" + card.getImageHtml() + "</body></html>";
		comp.setToolTipText(html);
	}

	@Override
	public void reset() {
		rebuild();
	}

	@Override
	public void handleClosed() {
		SessionEventManager.removeListeners(this);
	}
	
}
