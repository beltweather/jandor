package ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ComboListener extends KeyAdapter {
	
	@SuppressWarnings("rawtypes")
	private JComboBox cbListener;
	
	@SuppressWarnings("rawtypes")
	private Vector vector;
	
	@SuppressWarnings("rawtypes")
	public ComboListener(JComboBox cbListenerParam, Vector vectorParam) {
	    cbListener = cbListenerParam;
	    vector = vectorParam;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void keyReleased(KeyEvent key) {
		String text = ((JTextField)key.getSource()).getText();
		cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
        cbListener.setSelectedIndex(-1);
        ((JTextField)cbListener.getEditor().getEditorComponent()).setText(text);
        cbListener.showPopup();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getFilteredList(String text) {
	    Vector v = new Vector();
	    for(int a = 0;a<vector.size();a++) {
	        if(vector.get(a).toString().toLowerCase().startsWith(text.toLowerCase())) {
	            v.add(vector.get(a).toString());
	        }
	    }
	    return v;
	}

}
