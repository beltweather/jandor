package search;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import ui.pwidget.ColorUtil;
import util.CompoundIcon;
import util.CompoundIcon.Axis;
import util.ImageUtil;
import deck.Card;

public class CardSearchTableRenderer implements TableCellRenderer {
	
	private JLabel label;
	
	public CardSearchTableRenderer() {
		label = new JLabel();
		label.setOpaque(true);
		Border margin = new EmptyBorder(0,10,0,10);
		label.setBorder(margin);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(row % 2 == 0) {
			label.setBackground(ColorUtil.DARK_GRAY_3);
		} else {
			label.setBackground(ColorUtil.DARK_GRAY_2);
		}
		
		label.setIcon(null);
		Card card = (Card) value;
		String tt = "<html>" + card.getImageHtml() + "</html>";
		label.setToolTipText(tt);
		switch(column) {
			case 0:
				Icon icon = null;
				if(card.canTransform()) {
					Icon iconA = null; 
					if(ImageUtil.isCached(card.getImageUrl(), 0.4)) {
						iconA = new ImageIcon(card.getImage(0.4));
					} else {
						iconA = new ImageIcon(ImageUtil.readImage(card.getBackImageUrl(), 0.4, "back"));
					}
					
					Card transform = card.getTransformCard();
					Icon iconB = null; 
					if(ImageUtil.isCached(transform.getImageUrl(), 0.4)) {
						iconB = new ImageIcon(transform.getImage(0.4));
					} else {
						iconB = new ImageIcon(ImageUtil.readImage(transform.getBackImageUrl(), 0.4, "back"));
					}
					
					icon = new CompoundIcon(Axis.X_AXIS, 0, CompoundIcon.RIGHT, CompoundIcon.BOTTOM, iconA, iconB);
				} else {
					if(ImageUtil.isCached(card.getImageUrl(), 0.4)) {
						icon = new ImageIcon(card.getImage(0.4));
					} else {
						icon = new ImageIcon(ImageUtil.readImage(card.getBackImageUrl(), 0.4, "back"));
					}
				}
				label.setIcon(icon);
				label.setText("");
				break;
			case 1:
				label.setText(card.getToolTipText(466));
				break;
			case 2:
				label.setText("");
				break;
			default:
				label.setText("");
				break;
		}
		
		return label;
	
	}
	
}
