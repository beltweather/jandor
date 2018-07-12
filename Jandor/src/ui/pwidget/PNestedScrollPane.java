package ui.pwidget;

import java.awt.Dimension;

import javax.swing.JComponent;

public class PNestedScrollPane extends PScrollPane {

	public PNestedScrollPane(JComponent view) {
		super(view);
	}
	
	@Override
	public Dimension getPreferredSize() {
		if(getParent() == null) {
			return super.getPreferredSize();
		}
		return new Dimension((int) super.getPreferredSize().width, getHeight());
	}
	
}
