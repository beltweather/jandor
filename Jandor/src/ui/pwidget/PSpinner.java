package ui.pwidget;



import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSpinnerUI;



public abstract class PSpinner extends JSpinner {

	public static final String NONE = "";
	
	protected PPanel parentPanel;
	private int preferredHeight = 20;
	private boolean showArrows = true;

	public PSpinner(int value, int min, int max) {
		super();
	    setModel(new SpinnerNumberModel(value, min, max, 1));
	    init();
	}
	
	protected void init() {
		setUI(new ColorArrowUI(Color.WHITE));
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setBackground(ColorUtil.DARK_GRAY_0);
		((JSpinner.NumberEditor) getEditor()).getTextField().setBackground(ColorUtil.DARK_GRAY_0);
		((JSpinner.NumberEditor) getEditor()).getTextField().setForeground(Color.WHITE);  
		((JSpinner.NumberEditor) getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
		((JSpinner.NumberEditor) getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		((JSpinner.NumberEditor) getEditor()).getTextField().setCaretColor(Color.WHITE);
		//setFocusable(false);
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				PSpinner.this.handleChange(getIntValue());
			}
	    
	    });
	}
	
	public void setShowArrows(boolean showArrows) {
		this.showArrows = showArrows;
		setUI(new ColorArrowUI(Color.WHITE));
	}
	
	public void setArrowColor(Color color) {
		setUI(new ColorArrowUI(color));
	}
	
	public JTextField getTextField() {
		return ((JSpinner.NumberEditor) getEditor()).getTextField();
	}
	
	public int getIntValue() {
		return ((Number) getValue()).intValue();
	}
	
	protected abstract void handleChange(int value);
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int) super.getPreferredSize().getWidth(), (int) preferredHeight);
	}
	
	public void setPreferredWidth(int width) {
		super.setPreferredSize(new Dimension(width, (int) preferredHeight));
	}
	
	private static class ColorArrowUI extends BasicSpinnerUI {
	   
		private Color color;
		
		public ColorArrowUI(Color color) {
			super();
			this.color = color;
		}
		
		 @Override 
		 protected Component createPreviousButton() {
			 if(!((PSpinner) spinner).showArrows) {
				 JLabel l = new JLabel("");
				 l.setPreferredSize(new Dimension(5,1));
				 return l;
			 }
			 PButton flatButton = new PButton();
			 flatButton.setIcon(JUtil.getArrowIcon(7, 5, color, JUtil.DOWN));
			 flatButton.setCornerRadius(0);
			 flatButton.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, color));
			 flatButton.setFocusPainted(true);
			 flatButton.setPreferredSize(new Dimension(15, 20));
			 installPreviousButtonListeners(flatButton);
			 return flatButton;
		 }
		    
		 @Override 
		 protected Component createNextButton() {
			 if(!((PSpinner) spinner).showArrows) {
				 return new JLabel("");
			 }
			 PButton flatButton = new PButton();
			 flatButton.setIcon(JUtil.getArrowIcon(7, 5, color, JUtil.UP));
			 flatButton.setCornerRadius(0);
			 flatButton.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, color));
			 flatButton.setFocusPainted(true);
			 flatButton.setPreferredSize(new Dimension(15, 20));
			 installNextButtonListeners(flatButton);
			 return flatButton;
		 }
		    
	}
	
 }