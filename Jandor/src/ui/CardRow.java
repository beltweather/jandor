package ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import util.ImageUtil;
import zone.ZoneType;
import canvas.CardLayer;
import deck.Card;
import deck.CardList;

public class CardRow extends PPanel implements MouseListener {

	protected JButton addButton;
	protected CardLabel label;
	protected JLabel indexLabel;
	
	protected CardLayer layer;
	protected Card card;
	
	public CardRow(CardLayer layer, Card card) {
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
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		indexLabel = new JLabel(getIndex() + ": ");
		indexLabel.setForeground(Color.WHITE);
		label = new CardLabel(card);
		addButton = new JButton(ImageUtil.getImageIcon("back-small.png"));
		addButton.setText("to hand");
		addButton.setVisible(false);
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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
				comp.remove(CardRow.this);
				comp.revalidate();
				handleMovedToHand(CardRow.this);
				layer.repaint();
			}
			
		});
		
		c.anchor = G.WEST;
		c.insets = new Insets(5, 10, 6, 0);
		c.weaken();
		add(indexLabel, c);
		c.gridx++;
		c.strengthen();
		c.fill = G.HORIZONTAL;
		add(label, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.weaken();
		c.gridx++;
		add(addButton, c);
		
		addMouseListener(this);
		indexLabel.addMouseListener(this);
		label.addMouseListener(this);
		addButton.addMouseListener(this);
	}
	
	public void update() {
		indexLabel.setText(getIndex() + ": ");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		addButton.setVisible(true);
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		addButton.setVisible(false);
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
		
	public void handleMovedToHand(CardRow row) {
		
	}
}
