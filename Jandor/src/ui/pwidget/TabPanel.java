package ui.pwidget;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TabPanel extends JPanel implements CloseListener {
		
	private JButton button = new JButton();
	private JLabel label = new JLabel();
	
	public TabPanel() {
		super();
	}
		
	public JButton getButton() {
		return button;
	}
	
	public JLabel getLabel() {
		return label;
	}
	
	public void setTitle(String title) {
		label.setText(title);
	}

	@Override
	public void handleClosed() {
		
	}
	
}
