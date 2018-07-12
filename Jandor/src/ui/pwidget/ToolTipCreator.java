package ui.pwidget;


import java.awt.Component;
import java.awt.Container;



public abstract class ToolTipCreator {

	protected String title;
	protected boolean allowWindow;
	protected boolean allowLeftClick = true;
	
	public ToolTipCreator() {
		this(null, true);
	}
	
	public ToolTipCreator(boolean allowWindow) {
		this(null, allowWindow);
	}
	
	public ToolTipCreator(String title) {
		this(title, true);
	}
	
	public ToolTipCreator(String title, boolean allowWindow) {
		this.title = title;
		this.allowWindow = allowWindow;
	}
	
	protected abstract Component createContent(boolean isWindowed);
	
	public boolean isAllowLeftClick() {
		return allowLeftClick;
	}
	
	public void setAllowLeftClick(boolean allow) {
		this.allowLeftClick = allow;
	}
	
	public Component create() {
		return create(true);
	}
	
	public Component create(boolean showMessageText) {
		return createContent(!showMessageText);
	}
	
	/**
	 * @param compInFrame A component anywhere inside the frame where the tab needs
	 * 	                  to spawn. It can even be the frame itself.
	 */
	public void openTab(Component compInFrame) {
		JUtil.openTab(compInFrame, getTitle(), (Container) create(false));
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAllowWindow() {
		return allowWindow;
	}

	public void setAllowWindow(boolean allowWindow) {
		this.allowWindow = allowWindow;
	}
	
}
