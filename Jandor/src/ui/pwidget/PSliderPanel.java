package ui.pwidget;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class PSliderPanel extends PPanel {

	private PRangeSlider slider;
	private PSpinner lowSpin;
	private PSpinner highSpin;

	private boolean handleEvents = true;
	
	public PSliderPanel(int min, int max) {
		this(min, max, min, max);
	}
	
	public PSliderPanel(int lowDefault, int highDefault, int min, int max) {
		super();
		init(lowDefault, highDefault, min, max);
	}
	
	private void init(int lowDefault, int highDefault, int min, int max) {
		slider = new PRangeSlider();
		slider.setMinimum(min);
		slider.setMaximum(max);
		slider.setValue(lowDefault);
		slider.setUpperValue(highDefault);
		
		lowSpin = new PSpinner(lowDefault, min, max) {

			@Override
			protected void handleChange(int value) {
				PSliderPanel.this.internalHandleChange(lowSpin);
			}
			
		};		
		highSpin = new PSpinner(highDefault, min, max) {

			@Override
			protected void handleChange(int value) {
				PSliderPanel.this.internalHandleChange(highSpin);
			}
			
		};
		
		slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	internalHandleChange(null);
            }
        });
		
		Dimension d = new Dimension(40, lowSpin.getPreferredSize().height);
		lowSpin.setPreferredSize(d);
		highSpin.setPreferredSize(d);
		slider.setMinimumSize(new Dimension(150, slider.getPreferredSize().height));
		
		add(lowSpin, c);
		c.gridx++;
		c.insets = new Insets(0, 5, 0, 0);
		add(slider, c);
		c.gridx++;
		add(highSpin, c);
	}
	
	public int getLowerValue() {
		return slider.getValue();
	}
	
	public int getUpperValue() {
		return slider.getUpperValue();
	}
	
	private void internalHandleChange(PSpinner spinner) {
		if(!handleEvents) {
			return;
		}
		
		handleEvents = false;
		if(spinner == null) {
			lowSpin.setValue(slider.getValue());
			highSpin.setValue(slider.getUpperValue());
		} else if(spinner == lowSpin) {
			slider.setValue(lowSpin.getIntValue());
		} else {
			slider.setUpperValue(highSpin.getIntValue());
		}
		handleChange();

		handleEvents = true;
	}
	
	protected abstract void handleChange();
	
}
