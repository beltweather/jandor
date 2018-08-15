package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import deck.Card;
import deck.CardList;
import deck.Deck;
import search.CardSearchPanel;
import ui.SimpleClickableCardRow;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import util.ShuffleType;
import util.ShuffleUtil;

public class SimpleInspectView extends JandorView {
	
	protected CardList cards;
	protected JCheckBox shuffle;
	protected CardSearchPanel cardSearchPanel;
	protected int topCount;
	protected List<Card> topDeckCards = new ArrayList<Card>();
	protected List<Card> bottomDeckCards = new ArrayList<Card>();
	
	public SimpleInspectView(String name, List<Card> cards) {
		this(name, cards, -1);
	}
	
	public SimpleInspectView(String name, List<Card> cards, int topCount) {
		super(name, true);
		this.cards = new CardList(cards);
		this.topCount = topCount;
		rebuild();
	}

	@Override
	public void handleClosed() {
		if(shuffle == null) {
			return;
		}
		
		if(shuffle.isSelected()) {
			ShuffleUtil.shuffle(ShuffleType.RANDOM, cards);
			for(Card card : topDeckCards) {
				cards.move(card, 0);
			}
			for(Card card : bottomDeckCards) {
				cards.move(card, -1);
			}
		}
	}

	@Override
	protected void rebuild() {
		removeAll();
		PPanel cardPanel = new PPanel();
		
		if(cards.size() == 0) {
			JLabel label = new JLabel("There are no cards to inspect.");
			label.setForeground(Color.WHITE);
			add(label);
			return;
		}
		
		cardPanel.c.weighty = 0.01;
		cardPanel.c.fill = G.HORIZONTAL;
		cardPanel.c.anchor = G.CENTER;
		
		cardPanel.c.strengthen();
		cardPanel.add(Box.createHorizontalStrut(1), cardPanel.c);
		cardPanel.c.gridx++;
		
		cardPanel.c.weaken();
		final List<SimpleClickableCardRow> cardRows = new ArrayList<SimpleClickableCardRow>();
		int i = 0;
		for(Card card : cards) {
			if(topCount > 0 && i >= topCount) {
				break;
			}
			SimpleClickableCardRow row = new SimpleClickableCardRow(card);
			cardPanel.add(row, cardPanel.c);
			cardPanel.c.gridx++;
			cardRows.add(row);
			i++;
		}
		
		cardPanel.c.gridx++;
		cardPanel.c.strengthen();
		cardPanel.add(Box.createHorizontalStrut(1), cardPanel.c);
		
		final JCheckBox showImages = new JCheckBox("Show Full Cards");
		showImages.setOpaque(false);
		showImages.setForeground(Color.BLACK);
		showImages.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for(SimpleClickableCardRow row : cardRows) {
					row.setShowFullCard(showImages.isSelected());
				}
			}
			
		});
		
		shuffle = new JCheckBox("Shuffle");
		shuffle.setOpaque(false);
		shuffle.setForeground(Color.BLACK);
		shuffle.setSelected(topCount <= 0);
		
		PPanel checkPanel = new PPanel();
		//checkPanel.add(showImages, checkPanel.c);
		checkPanel.c.gridx++;
		
		shuffle.setSelected(false);
		
		cardSearchPanel = new CardSearchPanel(cards) {

			@Override
			protected void handleResults(Deck results) {
				if(results == null) {
					return;
				}
				for(SimpleClickableCardRow row : cardRows) {
					if(results.getCount(row.getCard().getName()) > 0) {
						row.setVisible(true);
					} else {
						row.setVisible(false);
					}
				}
				repaint();
			}
			
		};
		
		cardPanel.add(Box.createHorizontalStrut(1), cardPanel.c);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		
		int windowX = (int) Math.min(Math.round(width * 0.8), 1400);
		int windowY = 400;
		
		c.strengthen();
		add(new PScrollPane(cardPanel, new Dimension(windowX, windowY), true), c);
		c.gridy++;
		c.anchor = G.CENTER;
		c.fill = G.NONE;
		add(checkPanel, c);
		c.gridy++;
		addc(new PScrollPane(new PPanel(cardSearchPanel), new Dimension(windowX, 150), true));
	}

	@Override
	public void reset() {
		
	}

}
