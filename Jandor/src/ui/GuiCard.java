package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import ui.pwidget.ColorUtil;
import ui.pwidget.PPanel;
import util.CardUtil;
import util.ManaUtil;
import deck.Card;

public class GuiCard extends PPanel {
	
	private Card card;
	private int width;
	private int height;
	
	private JLabel nameLabel = new JLabel();
	private JLabel manaLabel = new JLabel();
	private JLabel typeLabel = new JLabel();
	private JLabel textLabel = new JLabel();
	private JLabel ptLabel = new JLabel();
	
	public GuiCard(String name, int width, int height) {
		this(new Card(name), width, height);
	}
	
	public GuiCard(Card card, int width, int height) {
		this.card = card;
		this.width = width;
		this.height = height;
		init();
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	@Override
	public Dimension getSize() {
		return new Dimension(width, height);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(width, height);
	}
	
	public Card getCard() {
		return card;
	}
	
	protected void init() {
		int width = -1;
		boolean insertImages = true;
		
		nameLabel = new JLabel();
		manaLabel = new JLabel();
		typeLabel = new JLabel();
		textLabel = new JLabel();
		ptLabel = new JLabel();
		
		String name = card.getName();
		String text = card.getText();
		List<String> manaCost = card.getManaCost();
		String type = card.getType();
		String power = card.getPower();
		String toughness = card.getToughness();
		String loyalty = card.getLoyalty();
		String set = card.getSetName();
		
		nameLabel.setText("<html><b>" + name + "</b></html>");
		
		if(manaCost != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(" ");
			if(insertImages) {
				for(String mana : manaCost) {
					sb.append("<span>" + ManaUtil.getSmallHtml(mana) + "</span>");
				}
			} else {
				for(String mana : manaCost) {
					sb.append("<span>" + "{" + mana + "}" + "</span>");
				}
			}
			manaLabel.setText("<html>" + sb.toString() + "</html>");
		}
		
		if(type != null) {
			typeLabel.setText("<html><span>" + type + "</span><hr></html>");
		}
		
		if(power != null) {
			ptLabel.setText("<html><span> (" + power + "/" + toughness + ")</span></html>");
		} else if(loyalty != null) {
			ptLabel.setText("<html><span> (" + loyalty + ")</span></html>");
		}
		
		if(text != null) {
			if(insertImages) {
				text = CardUtil.toHtml(text);
			}
			if(width == -1) {
				textLabel.setText("<html><div>" + text + "</div></html>");
			} else {
				textLabel.setText("<html><div \"width=" + width + "px\">" + text + "</div></html>");
			}
		}
		
		c.strengthen();
		addc(nameLabel);
		c.gridx++;
		addc(manaLabel);
		c.gridx--;
		c.gridy++;
		c.gridwidth = 2;
		addc(typeLabel);
		c.gridy++;
		addc(textLabel);
		c.gridy++;
		addc(ptLabel);
		c.gridwidth = 1;
	}
	
	private JLabel initLabel(JLabel label, Color backgroundColor, int w, int h, int y) {
		return initLabel(label, backgroundColor, w, h, y, 25);
	}
	
	private JLabel initLabel(JLabel label, Color backgroundColor, int w, int h, int y, int border) {
		return initLabel(label, backgroundColor, w, h, y, border, false);
	}
	
	private JLabel initLabel(JLabel label, Color backgroundColor, int w, int h, int y, int border, boolean rightAlign) {
		label.setForeground(ColorUtil.getBestForegroundColor(backgroundColor));
		label.setBorder(new EmptyBorder(y + border, border, border, border));
		label.setSize(w, h);
		label.setVerticalAlignment(JLabel.TOP);
		label.setHorizontalAlignment(rightAlign ? JLabel.RIGHT : JLabel.LEFT);
		return label;
	}
	
	public void printLabels(Graphics g, Color backgroundColor, int w, int h) {
		int b = 25;

		initLabel(nameLabel, backgroundColor, w, h, 0, b);
		initLabel(manaLabel, backgroundColor, w, h, 0, b, true);
		initLabel(typeLabel, backgroundColor, w, h, 25, b);
		initLabel(textLabel, backgroundColor, w, h, 50, b);
		initLabel(ptLabel, backgroundColor, w, h, h - b - 50, b, true);
		
		nameLabel.print(g);
		manaLabel.print(g);
		typeLabel.print(g);
		textLabel.print(g);
		ptLabel.print(g);
	}

}
