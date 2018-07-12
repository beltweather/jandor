package search;

import java.awt.Component;

import javax.swing.Box;

import ui.pwidget.PPanel;
import ui.pwidget.PTableModel;

public abstract class FakeTable extends PPanel {
	
	private PTableModel model;
	
	public FakeTable(PTableModel model) {
		super();
		this.model = model;
		rebuild();
	}
	
	public PTableModel getModel() {
		return model;
	}
	
	public void rebuild() {
		removeAll();	
		c.reset();
		c.weaken();
		
		for(int row = 0; row < model.getRowCount(); row++) {
			for(int col = 0; col < model.getColumnCount(); col++) {
				Component comp = buildComponent(row, col, model.getValueAt(row, col));
				add(comp, c);
				c.gridx++;
			}
			c.gridy++;
			c.gridx = 0;
		}
		
		c.strengthen();
		c.gridx = model.getColumnCount();
		add(Box.createHorizontalStrut(1), c);
	}
	
	public abstract Component buildComponent(int row, int col, Object value);
	
}
