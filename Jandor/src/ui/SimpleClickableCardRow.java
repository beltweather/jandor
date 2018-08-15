package ui;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;

import deck.Card;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import zone.ZoneType;

public class SimpleClickableCardRow extends PPanel implements MouseListener {

	protected Card card;
	protected CardLabel label;
	
	public SimpleClickableCardRow(Card card) {
		super();
		this.card = card;
		init();
	}
	
	public Card getCard() {
		return card;
	}
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		label = new CardLabel(card);
		label.setShowFullCard(true);
		label.disableTooltip();
		
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
		c.insets(0);
		addc(Box.createVerticalStrut(60));
		c.gridx++;
		fill();
		
		label.addMouseListener(this);
	}
	
	public void update() {
		
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
		
	public void handleMovedToZone(SimpleClickableCardRow row, ZoneType zone) {
		
	}
	
	public void handleMovedToTopOrBottom(SimpleClickableCardRow row, ZoneType zone, boolean top) {
		
	}
}
