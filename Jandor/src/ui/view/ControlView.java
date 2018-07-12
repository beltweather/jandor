package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JLabel;

import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import util.SystemUtil;

public class ControlView extends JandorView {

	private PPanel p;
	
	public ControlView() {
		super("Jandor - Controls", true);
		rebuild();
	}

	@Override
	public void handleClosed() {
		
	}

	@Override
	protected void rebuild() {
		removeAll();
		p = new PPanel();
		
		addHeader("Cards");
		addLine("Move", "Drag cards with the left mouse button to move them. After the left button is released, cards will snap to their closest zone.");
		addLine("Tap", "Right click a card to tap or untap it, or hold right click and drag the mouse over any number of cards to tap or untap them all.");
		addLine("Flip", "Middle click a card to flip it over, or hold middle click and drag the mouse over any number of cards to flip them all.");
		addLine("Select", "Left click on an empty part of the screen and drag the mouse to draw a selection box. Everything inside this box will be selected. " +
							 "Cards that are selected can be dragged, tapped, and flipped all at once. Holding the \"ctrl\" key and clicking on a card will toggle its selection.");
		addLine("Shuffle", "While dragging a selection of cards, shake the mouse back and forth to shuffle them, or simply press the \"s\" key to shuffle all selected cards. Cards will be shuffled within their current zone. " + 
							 "Press \"ctrl+a\" to select everything in the window (including dice, counters, and tokens).");
		addLine("Delete", "Selecting one or more cards and hitting the \"backspace\" or \"delete\" key will delete them from the current game. Cards that belong in the deck will come back when a new game begins.");
		
		addHeader("Dice, Counters, & Tokens");
		addLine("Move/Select/Delete", "Interact with dice just as you would cards to move, select, and delete them.");
		addLine("Change Value", "Left click on a die or counter to increase its value by one. Right click on a die or counter to decrease its value by one. Counters have no upper or lower limits, but dice will cycle through their values.");
		addLine("Roll Dice", "While dragging one or more dice, shake the mouse back and forth to roll them. Their current number will be replaced with a question mark. Once they are set down, a randomly chosen value between 0 and 9 will be displayed.");
		addLine("Roll Counter", "While dragging one or more counter, shake the mouse back and forth to roll them. Their current number will be replaced with \"e/o\". Once they are set down, a randomly chosen value of 1 or 2 will be displayed.");
		addLine("Change Color", "Middle click on any die, counter, or token to change its color. Colors will be changed in WUBRG order.");
		
		c.strengthen();
		add(new PScrollPane(p, new Dimension(600, 650)), c);
	}
	
	private void addHeader(String header) {
		JLabel headerLabel = new JLabel("<html><h1>" + header + "</h1></html>");
		headerLabel.setForeground(Color.WHITE);
		p.c.gridwidth = 2;
		p.c.anchor = G.CENTER;
		p.add(headerLabel, p.c);
		p.c.gridy++;
		p.c.gridwidth = 1;
	}
	
	private void addLine(String name, String description) {
		JLabel nameLabel = new JLabel("<html><b>" + name + "</b></html>");
		nameLabel.setForeground(Color.WHITE);
		JLabel descriptionLabel = new JLabel("<html><div width=\"300px\">" + clean(description) + "</div></html>");
		descriptionLabel.setForeground(Color.WHITE);
		p.c.anchor = G.EAST;
		p.c.insets = new Insets(0, 0, 10, 30);
		p.add(nameLabel, p.c);
		p.c.insets = new Insets(0, 0, 10, 0);
		p.c.anchor = G.WEST;
		p.c.gridx++;
		p.add(descriptionLabel, p.c);
		p.c.gridx--;
		p.c.gridy++;
	}
	
	private String clean(String s) {
		if(SystemUtil.isMac()) {
			return s;
		}
		
		s = s.replace("Right", "Cmd+left");
		s = s.replace("right", "cmd+left");
		s = s.replace("Middle", "Shift+left");
		s = s.replace("middle", "shift+left");
		
		return s;
	}

	@Override
	public void reset() {
		
	}
	
}
