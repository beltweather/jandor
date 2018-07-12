package dice;

import java.awt.Color;

import canvas.IRenderable;

public class D10 extends Die {

	public D10(Color color, int value) {
		super(0, 9, color, value);
	}
	
	@Override
	public IRenderable<Die> copyRenderable() {
		return new D10(color, value);
	}
}
