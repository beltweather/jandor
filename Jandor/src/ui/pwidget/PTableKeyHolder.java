package ui.pwidget;


public class PTableKeyHolder extends PPanel {

	public void setTableKey(PTableKey tableKey) {
		c.fill = G.NONE;
		removeAll();
		add(tableKey, c);
		revalidate();
		repaint();
	}
	
}
