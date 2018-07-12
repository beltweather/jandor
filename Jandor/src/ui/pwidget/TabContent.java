package ui.pwidget;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class TabContent extends JPanel {

	public static TabContent wrap(Component component) {
		if(component == null) {
			return null;
		}
		if(component instanceof TabContent) {
			return (TabContent) component;
		}
		return new TabContent(component);
	}
	
	private JComponent component;

	private TabContent(Component component) {
		this((JComponent) component);
	}
	
	private TabContent(JComponent component) {
		super(new BorderLayout());
		add(component, BorderLayout.CENTER);
		this.component = component;
	}

	public JComponent getContent() {
		return component;
	}

}
