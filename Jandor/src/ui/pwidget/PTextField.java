package ui.pwidget;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;




public class PTextField extends JTextField {

	protected PPanel parentPanel;
	protected boolean hasChangesToAccept = false;
	
	public PTextField() {
		this(null);
	}
	
	public PTextField(PPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
		init();
	}
	
	private void init() {
		setPreferredSize(new Dimension(125, 25));
		setMinimumSize(new Dimension(125, 25));
		setMargin(new Insets(0, 3, 0, 0));
		
		addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent arg0) {
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				loseFocusCallback();
			}
			
		});
		
		addActionListener(new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	accept();
		    }
		    
		});
		
	}
	
	protected void loseFocusCallback() {
		
	}
	
	protected void accept() {
		hasChangesToAccept = false;
		if(parentPanel != null) {
			//parentPanel.update();
		} else {
			loseFocusCallback();
		}
	}
	
}
