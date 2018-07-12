package ui.pwidget;
import java.awt.Color;

import javax.swing.JCheckBox;


public class PCheckBox extends JCheckBox {

	public PCheckBox(String text) {
		super(text);
		init();
	}
	
	private void init() {
		setOpaque(false);
		setForeground(Color.WHITE);
		setFocusPainted(false);
	}
	
}
