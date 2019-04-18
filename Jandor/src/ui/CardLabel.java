package ui;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import util.ImageUtil;

import deck.Card;

public class CardLabel extends JLabel {

	public static final Color DEFAULT_COLOR = Color.WHITE;

	protected Card card;
	protected boolean showFullCard;

	public CardLabel(Card card) {
		super(card.getName());
		this.card = card;
		setForeground(DEFAULT_COLOR);
		enableTooltip();
	}

	public void setShowFullCard(boolean showFullCard) {
		this.showFullCard = showFullCard;

		if(this.showFullCard) {
			setText("");
			setIcon(new ImageIcon(ImageUtil.readImage(card.getImageUrl(), card.getName())));
		} else {
			setIcon(null);
			setText(card.getName());
		}

	}

	public void disableTooltip() {
		setToolTipText(null);
	}

	public void enableTooltip() {
		String html = "<html><body>" + card.getImageHtml() + "</body></html>";
		setToolTipText(html);
	}

}
