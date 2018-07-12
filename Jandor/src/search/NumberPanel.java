package search;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;

import json.JSONException;
import ui.pwidget.PPanel;
import ui.pwidget.PSimpleCombo;
import ui.pwidget.PSpinner;

public class NumberPanel extends PPanel {

	protected PSpinner spinner;
	protected PSimpleCombo combo;
	
	public NumberPanel() {
		super();
		init();
	}
	
	public PSpinner getSpinner() {
		return spinner;
	}
	
	protected void init() {
		List<String> items = new ArrayList<String>();
		items.add("<");
		items.add("<=");
		items.add("=");
		items.add(">=");
		items.add(">");
		combo = new PSimpleCombo(items) {

			@Override
			protected void handleItemSelected(ItemEvent event, Object item) {
				
			}
			
		};
		combo.setSelectedItem("=");
		combo.setPreferredSize(new Dimension(50, 22));
		combo.setMinimumSize(new Dimension(50, 22));
		
		spinner = new PSpinner(0, 0, 99) {

			@Override
			protected void handleChange(int value) {
				
			}
			
		};
		spinner.setPreferredSize(new Dimension(150, 20));
		spinner.setMinimumSize(new Dimension(150, 20));
		
		c.weaken();
		add(combo, c);
		c.gridx++;
		add(spinner, c);
		c.gridx++;
		c.strengthen();
		add(Box.createHorizontalStrut(1), c);
	}
	
	public boolean match(int value) throws JSONException {
		int valueB = ((Integer) spinner.getValue()).intValue();
		String op = combo.getSelectedItem().toString();
		if(op.equals("=")) {
			return value == valueB;
		} else if(op.equals("<")) {
			return value < valueB;
		} else if(op.equals("<=")) {
			return value <= valueB;
		} else if(op.equals(">")) {
			return value > valueB;
		} else if(op.equals(">=")) {
			return value >= valueB;
		} 
		return false;
	}
	
}
