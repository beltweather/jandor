package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import ui.CardRow;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import zone.Zone;
import zone.ZoneType;
import canvas.CardLayer;
import deck.Card;

public class OldInspectView extends JandorView {

	protected ZoneType zoneType;
	protected CardLayer layer;
	protected JCheckBox shuffle;
	
	public OldInspectView(String name, CardLayer layer, ZoneType zoneType) {
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
		
		JLabel labelTop = new JLabel("Top");
		labelTop.setHorizontalAlignment(JLabel.CENTER);
		labelTop.setForeground(Color.BLACK);
		cardPanel.c.anchor = G.CENTER;
		cardPanel.add(labelTop, cardPanel.c);
		cardPanel.c.gridy++;
		
		cardPanel.c.strengthen();
		cardPanel.c.weighty = 0.01;
		cardPanel.c.fill = G.HORIZONTAL;
		cardPanel.c.anchor = G.WEST;
		Zone zone = layer.getCardZoneManager().getZone(zoneType);
		final List<CardRow> cardRows = new ArrayList<CardRow>();
		for(Object o : zone) {
			Card card = (Card) o;
			CardRow row = new CardRow(layer, card) {
				
				@Override
				public void handleMovedToHand(CardRow r) {
					for(CardRow row : cardRows) {
						if(row.equals(r)) {
							continue;
						}
						row.update();
					}
					cardRows.remove(r);
				}
				
			};
			cardPanel.add(row, cardPanel.c);
			cardPanel.c.gridy++;
			
			cardRows.add(row);
		}
		
		JLabel labelBottom = new JLabel("Bottom");
		labelBottom.setHorizontalAlignment(JLabel.CENTER);
		labelBottom.setForeground(Color.BLACK);
		cardPanel.c.anchor = G.CENTER;
		cardPanel.add(labelBottom, cardPanel.c);
		cardPanel.c.gridy++;
		cardPanel.c.strengthen();
		
		final JCheckBox showImages = new JCheckBox("Show Full Cards");
		showImages.setOpaque(false);
		showImages.setForeground(Color.BLACK);
		showImages.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for(CardRow row : cardRows) {
					row.setShowFullCard(showImages.isSelected());
				}
			}
			
		});
		
		shuffle = new JCheckBox("Shuffle");
		shuffle.setOpaque(false);
		shuffle.setForeground(Color.BLACK);
		shuffle.setSelected(true);
		
		PPanel checkPanel = new PPanel();
		checkPanel.add(showImages, checkPanel.c);
		checkPanel.c.gridx++;
		
		if(zoneType == ZoneType.DECK) {
			checkPanel.add(shuffle, checkPanel.c);
		} else {
			shuffle.setSelected(false);
		}
		
		cardPanel.add(Box.createHorizontalStrut(1), cardPanel.c);

		c.strengthen();
		add(new PScrollPane(cardPanel, new Dimension(400, 600)), c);
		c.gridy++;
		c.anchor = G.CENTER;
		c.fill = G.NONE;
		add(checkPanel, c);
	}

	@Override
	public void reset() {
		
	}

}