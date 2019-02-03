package ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import canvas.CardLayer;
import deck.Card;
import deck.Deck;
import search.CardSearchPanel;
import ui.ClickableCardRow;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import zone.Zone;
import zone.ZoneType;

public class InspectView extends JandorView {

	protected ZoneType zoneType;
	protected CardLayer layer;
	protected JCheckBox shuffle;
	protected CardSearchPanel cardSearchPanel;
	protected int topCount;
	protected List<Card> topDeckCards = new ArrayList<Card>();
	protected List<Card> bottomDeckCards = new ArrayList<Card>();
	protected boolean reverse = false;

	public InspectView(String name, CardLayer layer, ZoneType zoneType) {
		this(name, layer, zoneType, -1);
	}

	public InspectView(String name, CardLayer layer, ZoneType zoneType, int topCount) {
		this(name, layer, zoneType, topCount, false);
	}

	public InspectView(String name, CardLayer layer, ZoneType zoneType, int topCount, boolean reverse) {
		super(name, true);
		this.layer = layer;
		this.zoneType = zoneType;
		this.topCount = topCount;
		this.reverse = reverse;
		rebuild();
	}

	@Override
	public void handleClosed() {
		if(shuffle == null) {
			return;
		}

		if(shuffle.isSelected()) {
			layer.shuffleCards(layer.getCardZoneManager().getZone(zoneType), true, true);
			for(Card card : topDeckCards) {
				layer.move(card, 0);
			}
			for(Card card : bottomDeckCards) {
				layer.move(card, -1);
			}
		}
	}

	@Override
	protected void rebuild() {
		removeAll();
		PPanel cardPanel = new PPanel();

		if(layer.getCardZoneManager().getZone(zoneType).size() == 0) {
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
		List<Card> zone = layer.getCardZoneManager().getZone(zoneType);
		final List<ClickableCardRow> cardRows = new ArrayList<ClickableCardRow>();
		List<Card> cards = new ArrayList<Card>();
		int i = 0;

		if(reverse) {
			zone = new ArrayList<Card>(zone);
			Collections.reverse(zone);
		}

		for(Object o : zone) {
			if(topCount > 0 && i >= topCount) {
				break;
			}
			Card card = (Card) o;
			cards.add(card);
			ClickableCardRow row = new ClickableCardRow(layer, card) {

				@Override
				public void handleMovedToZone(ClickableCardRow r, ZoneType zone) {
					List<Card> cardsToSearch = new ArrayList<Card>();
					cardRows.remove(r);
					for(ClickableCardRow row : cardRows) {
						cardsToSearch.add(row.getCard());
						row.update();
					}
					cardSearchPanel.setCardsToSearch(cardsToSearch);

					if(cardRows.size() == 0) {
						Component root = SwingUtilities.getRoot(InspectView.this);
						if(root != null) {
							root.setVisible(false);
						}
					}
				}

				@Override
				public void handleMovedToTopOrBottom(ClickableCardRow row, ZoneType zone, boolean top) {
					if(top) {
						topDeckCards.add(row.getCard());
					} else {
						bottomDeckCards.add(row.getCard());
					}
				}

			};
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
				for(ClickableCardRow row : cardRows) {
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

		if(zoneType == ZoneType.DECK) {
			checkPanel.add(shuffle, checkPanel.c);
		} else {
			shuffle.setSelected(false);
		}

		cardSearchPanel = new CardSearchPanel(cards) {

			@Override
			protected void handleResults(Deck results) {
				if(results == null) {
					return;
				}
				for(ClickableCardRow row : cardRows) {
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
