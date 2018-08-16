package dice;

import java.awt.Color;

import canvas.IRenderable;

public class Counter extends Die {

	private static final long serialVersionUID = 1L;

	public Counter(Color color, int value) {
		super(Integer.MIN_VALUE, Integer.MAX_VALUE, color, value);
		this.renderer = new CounterRenderer(this);
		this.cycles = false;
	}

	@Override
	public IRenderable<Die> copyRenderable() {
		return new Counter(color, value);
	}
}
