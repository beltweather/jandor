package ui;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;

import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import deck.Card;

public class SearchRow extends PPanel implements MouseListener {

	protected CardLabel label;
	
	protected Card card;
	
	public SearchRow(Card card) {
		super();
		this.card = card;
		init();
	}
	
	private void init() {
		setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		
		label = new CardLabel(card);
		label.setText(card.getToolTipText(800));
		
		c.anchor = G.WEST;
		c.insets = new Insets(5, 10, 6, 0);
		c.strengthen();
		c.fill = G.HORIZONTAL;
		add(label, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.weaken();
		c.gridx++;
		
		addMouseListener(this);
		label.addMouseListener(this);
	}
	
	public void update() {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

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
