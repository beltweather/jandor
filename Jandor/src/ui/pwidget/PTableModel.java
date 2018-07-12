package ui.pwidget;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


public abstract class PTableModel extends DefaultTableModel implements ICreatedToolTip {
	
	protected ToolTipCreator creator;
	
	public PTableModel() {
		super();
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	public abstract void setData(Object rawData);
	
	//public abstract boolean isHighGood();
	
	public abstract TableCellRenderer createRenderer();
	
	public abstract PTableKey createTableKey();
	
	public abstract JComponent createTooltipContent(PTable table, int row, int column);

	public void setToolTipCreator(ToolTipCreator creator) {
		this.creator = creator;
	}
	
	public ToolTipCreator getToolTipCreator() {
		return creator;
	}

}
