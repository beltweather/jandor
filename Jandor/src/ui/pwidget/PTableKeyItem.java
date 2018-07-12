package ui.pwidget;



import javax.swing.JLabel;

import canvas.Canvas;



public class PTableKeyItem extends PPanel {
	
	private Canvas icon;
	private String text;
	
	public PTableKeyItem(Canvas icon, String text) {
		super();
		this.icon = icon;
		this.text = text;
		init();
	}

	private void init() {
		c.fill = G.NONE;
		add(icon, c);
		c.gridy++;
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(15f));
		add(label, c);
	}

}
