package search;

import javax.swing.Box;
import javax.swing.JLabel;

import json.JSONException;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;

public class XOrStarPanel extends PPanel {

	protected PCheckBox starCheck;
	protected PCheckBox xCheck;
	
	public XOrStarPanel() {
		super();
		init();
	}
	
	protected void init() {
		starCheck = new PCheckBox("*");
		xCheck = new PCheckBox("X");
		c.weaken();
		c.insets(0, 10);
		add(xCheck, c);
		c.gridx++;
		add(starCheck, c);
		c.gridx++;
		c.strengthen();
		add(Box.createHorizontalStrut(1), c);
	}
	
	public boolean match(String manaCost) throws JSONException {
		if(manaCost == null) {
			return false;
		}
		return ((starCheck.isSelected() && manaCost.contains("*")) || !starCheck.isSelected()) &&
				((xCheck.isSelected() && manaCost.contains("X")) || !xCheck.isSelected());	
	}
	
}
