package ui.pwidget;


import java.awt.Image;

import javax.swing.ImageIcon;




public class PImage extends PLabel {

	public PImage() {
		super();
	}
	
	public PImage(Image img) {
		super(new ImageIcon(img));
	}
	
	public void setImage(Image img) {
		setIcon(new ImageIcon(img));
	}

}
