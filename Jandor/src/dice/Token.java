package dice;

import java.awt.Color;
import java.awt.Rectangle;

import canvas.IRenderable;
import canvas.Location;

public class Token extends Die {

	private static final long serialVersionUID = 1L;

	protected int secondValue;
	
	public Token(Color color, int value) {
		super(0, 10, color, value);
		secondValue = value;
		this.renderer = new TokenRenderer(this);
		this.cycles = true;
	}
	
	public int getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(int value) {
		this.secondValue = value;
	}

	
	public void incrementSecond() {
		if(cycles && secondValue == maxValue + minValue) {
			secondValue = minValue;
		} else {
			secondValue++;
		}
	}
	
	public void decrementSecond() {
		if(cycles && secondValue == minValue) {
			secondValue = maxValue + minValue;
		} else {
			secondValue--;
		}
	}
	
	@Override
	public IRenderable<Die> copyRenderable() {
		return new Token(color, value);
	}
	
	public void handleLeftClick(Location pointInCanvas) {
		Rectangle rect = getRenderer().getBounds().getBounds();
		int clickX = (int) pointInCanvas.getScreenX() - (int) rect.getX();
		int clickY = (int) pointInCanvas.getScreenY() - (int) rect.getY();
		handleLeftClick(clickX, clickY);
	}
	
	public void handleLeftClick(int clickX, int clickY) {
		if(clickX < getRenderer().getBounds().getBounds().getWidth() / 2) {
			increment();
		} else {
			incrementSecond();
		}
	}
}
