package ui.pwidget;


import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;



public class PRadio extends JRadioButton {

		private String id;
	
		public PRadio(String text) {
			super(text);
			this.id = text;
			init();
		}
		
		public void setShowText(boolean showText) {
			if(showText) {
				setText(id);
			} else {
				setText("");
			}
		}
		
		public String getId() {
			return id;
		}
		
		private void init() {
			setOpaque(false);
			setForeground(Color.WHITE);
			setFont(getFont().deriveFont(15f));
			setSelectedIcon(new ImageIcon(JUtil.getPokeballIconImage()));
			setIcon(new ImageIcon(JUtil.getPokeballOutlineIconImage()));
			setFocusPainted(false);
		}
	
}
