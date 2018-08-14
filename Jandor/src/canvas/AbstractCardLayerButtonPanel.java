package canvas;

import ui.pwidget.PPanel;

public abstract class AbstractCardLayerButtonPanel extends PPanel {
	
	protected CardLayer layer;
	
	public AbstractCardLayerButtonPanel(CardLayer layer) {
		super();
		this.layer = layer;
		init();
	}
	
	public void rebuild() {
		removeAll();
		init();
		revalidate();
	}
	
	protected abstract void init();
	
}
