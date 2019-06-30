package ui.pwidget;
import java.awt.Color;

import javax.swing.JCheckBox;


public class PCheckBox extends JCheckBox {

	public PCheckBox(String text) {
		this(text, false);
	}

	public PCheckBox(String text, boolean selected) {
		super(text);
		init();
		setSelected(selected);
	}

	private void init() {
		setOpaque(false);
		setForeground(Color.WHITE);
		setFocusPainted(false);
	}

}
