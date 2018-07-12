package ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import deck.Card;

public class AutoListCellRenderer<T> extends DefaultListCellRenderer {
	
	private AutoComboBox<T> combo;
	
	public AutoListCellRenderer(AutoComboBox<T> combo) {
		this.combo = combo;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		((AutoListCellRenderer<T>) c).setToolTipText(combo.buildTooltip((T) value));
		return c;
	}

}
