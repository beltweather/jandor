package ui.pwidget;

import java.awt.Color;

public class PLinkButton extends PButton {
	
	public PLinkButton(String text) {
		super("<html><u>" + text + "</u></html>");
		setShowBackground(false);
		setHoverForeground(Color.LIGHT_GRAY);
		setPressedForeground(Color.DARK_GRAY);
	}
	
}
