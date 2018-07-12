package ui.pwidget;


import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;



public abstract class PSimpleCombo extends PCombo {
	
	public static final String SEPARATOR = "---";

	private List<String> items;
	
	public PSimpleCombo() {
		this(null);
	}
	
	public PSimpleCombo(List<String> items) {
		super(null, false);
		this.items = items;
		this.init();
	}

	@Override
	protected ListCellRenderer<? super String> createRenderer() {
		return new CustomRenderer();
	}

	@Override
	protected void fillWithDefaultItems() {
		if(items != null) {
			for(String item : items) {
				addItem(item);
			}
		}
		setSelectedIndex(0);
	}

	@Override
	public void reset() {
		
	}
	
	private static class CustomRenderer implements ListCellRenderer {
		
		private JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if(value.equals(SEPARATOR)) {
				return separator;
			}
			
			if(value == null || value.equals("")) {
				value = " ";
			}
			JLabel label = new JLabel(value.toString());
			label.setHorizontalAlignment(JLabel.LEFT);
			label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			label.setOpaque(true);
			label.setBackground(ColorUtil.DARK_GRAY_0);
			if(isSelected) {
				label.setBackground(ColorUtil.DARK_GRAY_2);
			}
			return label;
		}
		
	}

}
