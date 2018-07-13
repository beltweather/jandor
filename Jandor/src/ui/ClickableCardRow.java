package ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import canvas.CardLayer;
import deck.Card;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import util.ImageUtil;
import zone.ZoneType;

public class ClickableCardRow extends PPanel implements MouseListener {

	protected JLabel clickLabel;
	protected CardLabel label;
	
	protected CardLayer layer;
	protected Card card;
	
	public ClickableCardRow(CardLayer layer, Card card) {
		super();
		this.layer = layer;
		this.card = card;
		init();
	}
	
	private int getIndex() {
		if(layer == null) {
			return 0;
		}
		return layer.getCardZoneManager().getZone(card.getZoneType()).indexOf(card) + 1;
	}
	
	private void handleAddToDeck() {
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
	}
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		label = new CardLabel(card);
		label.setShowFullCard(true);
		label.disableTooltip();
		clickLabel = new JLabel();
		clickLabel.setText("(add to hand)");
		clickLabel.setVisible(false);
		
		label.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				handleAddToDeck();
			}
			
		});
		
		c.anchor = G.NORTH;
		c.strengthen();
		c.fill = G.HORIZONTAL;
		add(label, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.weaken();
		c.gridx = 0;
		c.gridy++;
		add(clickLabel, c);
		
		addMouseListener(this);
		label.addMouseListener(this);
		clickLabel.addMouseListener(this);
	}
	
	public void update() {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		clickLabel.setVisible(true);
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		clickLabel.setVisible(false);
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
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
		
	public void handleMovedToHand(ClickableCardRow row) {
		
	}
}
