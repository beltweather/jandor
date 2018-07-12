package ui.pwidget;


import java.awt.Insets;




public class PTableKey extends PPanel {

	public PTableKey() {
		super();
		init();
	}

	private void init() {
		c.fill = G.NONE;
	}
	
	public void addKeyItem(PTableKeyItem item) {
		add(item, c);
		c.gridx++;
		c.insets = new Insets(0, 10, 0, 0);
	}
	
}
