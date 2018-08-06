package ui;

import java.awt.Container;
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
	
	protected PButton addHandButton;
	protected PButton addOtherButton;
	
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
	
	private void handleOther() {
		PPanel p = new PPanel();
			
		PButton topDeck = new PButton("+ Top Deck");
		PButton bottomDeck = new PButton("+ Bottom Deck");
		PButton graveyard = new PButton("+ Graveyard");
		PButton exile = new PButton("+ Exile");
		PButton battlefield = new PButton("+ Battlefield");
		
		p.c.insets(0, 0, 5, 0);
		p.addc(topDeck);
		p.c.gridy++;
		p.addc(bottomDeck);
		p.c.gridy++;
		p.addc(graveyard);
		p.c.gridy++;
		p.addc(exile);
		//p.c.gridy++;
		//p.addc(battlefield);
		
		final JDialog d = JUtil.buildBlankDialog(this, card.getName() + " - More Actions", p);
		
		topDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.DECK);
				layer.move(card, 0);
				d.setVisible(false);
			}
			
		});
		
		bottomDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.DECK);
				layer.move(card, layer.getCardZoneManager().getZone(ZoneType.DECK).size());
				d.setVisible(false);
			}
			
		});
		
		graveyard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.GRAVEYARD);
				d.setVisible(false);
			}
			
		});
		
		exile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.EXILE);
				d.setVisible(false);
			}
			
		});
		
		battlefield.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.BATTLEFIELD);
				d.setVisible(false);
			}
			
		});
		
		d.setVisible(true);
	}
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		label = new CardLabel(card);
		label.setShowFullCard(true);
		label.disableTooltip();
		
		addHandButton = new PButton(" + Hand ");
		addOtherButton = new PButton(" More... ");
		
		addHandButton.setVisible(false);
		addOtherButton.setVisible(false);
		
		addHandButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleAddToZone(ZoneType.HAND);
			}
			
		});
		
		addOtherButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleOther();
			}
			
		});
		
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
		addc(addHandButton);
		c.gridx++;
		c.insets(0);
		addc(Box.createVerticalStrut(30));
		c.gridx++;
		c.insets = new Insets(5, 5, 0, 0);
		addc(addOtherButton);
		c.gridx++;
		fill();
		
		addMouseListener(this);
		label.addMouseListener(this);
		addHandButton.addMouseListener(this);
		addOtherButton.addMouseListener(this);
	}
	
	public void update() {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//clickLabel.setVisible(true);
		addHandButton.setVisible(true);
		addOtherButton.setVisible(true);
		//setBorder(BorderFactory.createLineBorder(Color.WHITE));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//clickLabel.setVisible(false);
		addHandButton.setVisible(false);
		addOtherButton.setVisible(false);
		//setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
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
}
