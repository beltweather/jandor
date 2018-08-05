package search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import util.ImageUtil;
import deck.Card;

public abstract class CardSearchFakeTable extends FakeTable {

	private boolean addToDeck = false;
	private double scale = 0.4;
	private boolean isDraft;
	
	public CardSearchFakeTable() {
		this(false);
	}
	
	public CardSearchFakeTable(boolean isDraft) {
		super(new CardSearchTableModel());
		this.isDraft = isDraft;
	}
	
	public boolean isAddToDeck() {
		return addToDeck;
	}
	
	public void setAddToDeck(boolean addToDeck) {
		this.addToDeck = addToDeck;
		rebuild();
	}
	
	@Override
	public Component buildComponent(int row, int col, Object value) {
		return buildComponent(row, col, value, true);
	}

	public Component buildComponent(int row, int col, Object value, boolean includeTransform) {
		if(col == 2) {
			if(!addToDeck) {
				return new JLabel("");
			}
			
			final Card card = (Card) value;
			PPanel p = new PPanel();
			p.setOpaque(true);
			if(row % 2 == 0) {
				p.setBackground(ColorUtil.DARK_GRAY_3);
			} else {
				p.setBackground(ColorUtil.DARK_GRAY_2);
			}
			
			JButton button = new PButton("+ Deck");
			button.setPreferredSize(new Dimension(60, 20));
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					addCard(card, false);
				}
				
			});
			
			JButton sButton = new PButton(isDraft ? "+ Draft" : "+ Sideboard");
			sButton.setPreferredSize(new Dimension(90, 20));
			sButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					addCard(card, true);
				}
				
			});
			
			p.c.allInsets(10);
			if(!isDraft) {
				p.add(button, p.c);
			}
			p.c.insets(10,0,10,30);
			p.c.gridx++;
			p.add(sButton, p.c);
			return p;
		}
		
		c.fill = G.BOTH;
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		if(row % 2 == 0) {
			label.setBackground(ColorUtil.DARK_GRAY_3);
		} else {
			label.setBackground(ColorUtil.DARK_GRAY_2);
		}
		
		label.setIcon(null);
		Card card = (Card) value;
		label.setToolTipText(null);
		switch(col) {
			case 0:
				label.setToolTipText("<html>" + card.getImageHtml() + "</html>");
				if(ImageUtil.isCached(card.getImageUrl(), scale)) {
					label.setIcon(new ImageIcon(card.getImage(scale)));
				} else {
					label.setIcon(new ImageIcon(ImageUtil.readImage(card.getBackImageUrl(), scale, "back")));
					ImageUtil.addImageCacheListener(card.getImageUrl(), label, scale);
				}
				label.setText("");
				
				if(card.canTransform() && includeTransform) {
					JLabel labelT = (JLabel) buildComponent(row, col, card.getTransformCard(), false);
					PPanel panel = new PPanel();
					panel.addc(label);
					panel.c.gridx++;
					panel.c.insets(0,
								   -labelT.getIcon().getIconWidth() + 5,
								   0,
								   0);
					panel.addc(labelT);
					panel.setOpaque(true);
					if(row % 2 == 0) {
						panel.setBackground(ColorUtil.DARK_GRAY_3);
					} else {
						panel.setBackground(ColorUtil.DARK_GRAY_2);
					}
					return panel;
				}
				
				break;
			case 1:
				label.setText(card.getToolTipText(466));
				break;
			default:
				label.setText("");
				break;
		}
		
		return label;
	}
	
	public abstract void addCard(Card card, boolean sideboard);

}
