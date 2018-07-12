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
import ui.SearchRow;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import deck.Card;
import deck.Deck;

public class QuickView extends JandorView {

	public QuickView(String name, Deck deck) {
		super(name, true);
		this.deck = deck;
		rebuild();
	}

	@Override
	public void handleClosed() {

	}

	@Override
	protected void rebuild() {
		removeAll();
		PPanel cardPanel = new PPanel();
		
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
		final List<SearchRow> cardRows = new ArrayList<SearchRow>();
		if(deck != null) {
			for(Card card : deck) {
				SearchRow row = new SearchRow(card);
				cardPanel.add(row, cardPanel.c);
				cardPanel.c.gridy++;
				cardRows.add(row);
			}
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
				for(SearchRow row : cardRows) {
					row.setShowFullCard(showImages.isSelected());
				}
			}
			
		});
		
		PPanel checkPanel = new PPanel();
		checkPanel.add(showImages, checkPanel.c);
		checkPanel.c.gridx++;
		
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