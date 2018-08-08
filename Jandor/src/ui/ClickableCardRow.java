package ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;

import canvas.CardLayer;
import deck.Card;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import zone.ZoneType;

public class ClickableCardRow extends PPanel implements MouseListener {

	protected CardLabel label;
	
	protected CardLayer layer;
	protected Card card;
	
	protected PPanel buttonPanel;
	
	public ClickableCardRow(CardLayer layer, Card card) {
		super();
		this.layer = layer;
		this.card = card;
		init();
	}
	
	public Card getCard() {
		return card;
	}
	
	private int getIndex() {
		if(layer == null) {
			return 0;
		}
		return layer.getCardZoneManager().getZone(card.getZoneType()).indexOf(card) + 1;
	}
	
	private void handleAddToZone(ZoneType zone) {
		if(layer == null) {
			return;
		}
		
		card.rememberLastZoneType();
		layer.getCardZoneManager().getZone(card.getZoneType()).remove(card);
		layer.getCardZoneManager().getZone(zone).add(card);
		card.setZoneType(zone);
		layer.handleMoved(Arrays.asList(card), false);
		layer.repaint();
		Container comp = getParent();
		comp.remove(ClickableCardRow.this);
		comp.revalidate();
		handleMovedToZone(ClickableCardRow.this, zone);
		layer.repaint();
	}
	
	
	/*private void handleAddToDeck() {
		if(layer == null) {
			return;
		}
		
		card.rememberLastZoneType();
		layer.getCardZoneManager().getZone(card.getZoneType()).remove(card);
		layer.getCardZoneManager().getZone(ZoneType.HAND).add(card);
		card.setZoneType(ZoneType.HAND);
		layer.handleMoved(Arrays.asList(card), false);
		layer.repaint();
		Container comp = getParent();
		comp.remove(ClickableCardRow.this);
		comp.revalidate();
		handleMovedToHand(ClickableCardRow.this);
		layer.repaint();
	}*/
	
	private PPanel buildButtonPanel() {
		PPanel p = new PPanel();
		
		PButton hand = new PButton("+ Hand");
		PButton topDeck = new PButton("+ Top Deck");
		PButton bottomDeck = new PButton("+ Bottom Deck");
		PButton graveyard = new PButton("+ Graveyard");
		PButton exile = new PButton("+ Exile");
		PButton more = new PButton(" More... ");
		
		hand.addMouseListener(this);
		topDeck.addMouseListener(this);
		bottomDeck.addMouseListener(this);
		graveyard.addMouseListener(this);
		exile.addMouseListener(this);
		more.addMouseListener(this);
		
		p.c.anchor = GridBagConstraints.CENTER;
		p.c.strengthen();
		p.c.insets(0, 0, 5, 0);
		p.addc(hand);
		p.c.gridx++;
		p.c.insets(0, 5, 5, 0);
		p.addc(graveyard);
		p.c.gridx++;
		p.c.insets(0, 5, 5, 0);
		p.addc(exile);
		p.c.gridx = 0;
		p.c.gridy++;
		p.c.insets(0, 0, 5, 0);
		p.addc(topDeck);
		p.c.gridx++;
		p.c.insets(0, 5, 5, 0);
		p.addc(bottomDeck);
		
		hand.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.HAND);
			}
			
		});
		
		topDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.DECK);
				layer.move(card, 0);
				handleMovedToTopOrBottom(ClickableCardRow.this, ZoneType.DECK, true);
			}
			
		});
		
		bottomDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.DECK);
				layer.move(card, layer.getCardZoneManager().getZone(ZoneType.DECK).size());
				handleMovedToTopOrBottom(ClickableCardRow.this, ZoneType.DECK, false);
			}
			
		});
		
		graveyard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.GRAVEYARD);
			}
			
		});
		
		exile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.EXILE);
			}
			
		});
		
		more.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleMore();
			}
			
		});
		
		return p;
	}
	
	private void handleMore() {
		PPanel p = new PPanel();
			
		final JDialog d = JUtil.buildBlankDialog(this, card.getName() + " - More Actions", p);
		d.setVisible(true);
	}
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		label = new CardLabel(card);
		label.setShowFullCard(true);
		label.disableTooltip();
		
		buttonPanel = buildButtonPanel();
		buttonPanel.setVisible(false);
		
		c.anchor = G.NORTH;
		c.strengthen();
		c.fill = G.HORIZONTAL;
		c.gridwidth = 5;
		add(label, c);
		c.gridwidth = 1;
		c.insets = new Insets(5, 0, 0, 0);
		c.weaken();
		c.gridx = 0;
		c.gridy++;
		
		fill();
		c.gridx++;
		c.weaken();
		addc(buttonPanel);
		c.gridx++;
		c.insets(0);
		addc(Box.createVerticalStrut(60));
		c.gridx++;
		fill();
		
		addMouseListener(this);
		label.addMouseListener(this);
		buttonPanel.addMouseListener(this);
	}
	
	public void update() {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		buttonPanel.setVisible(true);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		buttonPanel.setVisible(false);
	}
	
	public void setShowFullCard(boolean showFullCard) {
		label.setShowFullCard(showFullCard);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
		
	public void handleMovedToZone(ClickableCardRow row, ZoneType zone) {
		
	}
	
	public void handleMovedToTopOrBottom(ClickableCardRow row, ZoneType zone, boolean top) {
		
	}
}
