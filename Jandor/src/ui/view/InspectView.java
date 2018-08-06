package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

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
	
	public InspectView(String name, CardLayer layer, ZoneType zoneType) {
		super(name, true);
		this.layer = layer;
		this.zoneType = zoneType;
		rebuild();
	}

	@Override
	public void handleClosed() {
		if(shuffle == null) {
			return;
		}
		
		if(shuffle.isSelected()) {
			layer.shuffleCards(layer.getCardZoneManager().getZone(zoneType), true);
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
		Zone zone = layer.getCardZoneManager().getZone(zoneType);
		final List<ClickableCardRow> cardRows = new ArrayList<ClickableCardRow>();
		List<Card> cards = new ArrayList<Card>();
		for(Object o : zone) {
			Card card = (Card) o;
			cards.add(card);
			ClickableCardRow row = new ClickableCardRow(layer, card) {
				
				@Override
				public void handleMovedToZone(ClickableCardRow r, ZoneType zone) {
					List<Card> cardsToSearch = new ArrayList<Card>();
					for(ClickableCardRow row : cardRows) {
						if(row.equals(r)) {
							continue;
						}
						cardsToSearch.add(row.getCard());
						row.update();
					}
					cardRows.remove(r);
					cardSearchPanel.setCardsToSearch(cardsToSearch);
				}
				
			};
			cardPanel.add(row, cardPanel.c);
			cardPanel.c.gridx++;
			
			cardRows.add(row);
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
		shuffle.setSelected(true);
		
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
		addc(cardSearchPanel);
	}

	@Override
	public void reset() {
		
	}

}
