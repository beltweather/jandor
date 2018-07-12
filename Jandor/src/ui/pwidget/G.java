package ui.pwidget;

import java.awt.GridBagConstraints;
import java.awt.Insets;


public class G extends GridBagConstraints {

	public static G c() {
		return JUtil.gbc();
	}
	
	public G() {
		reset();
	}
	
	public void strengthen() {
		JUtil.strengthen(this);
	}
	
	public void weaken() {
		JUtil.weaken(this);
	}
	
	public void insets(int... i) {
		switch(i.length) {
			case 0:
				insets = new Insets(0,0,0,0);
				break;
			case 1:
				insets = new Insets(i[0],0,0,0);
				break;
			case 2:
				insets = new Insets(i[0], i[1], 0, 0);
				break;
			case 3:
				insets = new Insets(i[0], i[1], i[2], 0);
				break;
			case 4:
			default:
				insets = new Insets(i[0], i[1], i[2], i[3]);
		}
	}
	
	public void allInsets(int i) {
		insets = new Insets(i,i,i,i);
	}
	
	public void reset() {
		gridx = 0;
		gridy = 0;
		gridwidth = 1;
		gridheight = 1;
		anchor = G.CENTER;
		insets();
		weaken();
	}
	
}
