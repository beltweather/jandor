package ui;

import java.awt.event.ItemEvent;
import java.util.List;

import ui.pwidget.PSimpleCombo;

public class JandorCombo extends PSimpleCombo {
	
	private int lastSelectedIndex = 0;
	
	public JandorCombo(List<String> items) {
		super(items);
	}

	@Override
	protected void handleItemSelected(ItemEvent event, Object item) {
		if(item.equals(PSimpleCombo.SEPARATOR)) {
			setSelectedIndex(lastSelectedIndex);
		} else {
			lastSelectedIndex = getSelectedIndex();
		}
	}
	
}
