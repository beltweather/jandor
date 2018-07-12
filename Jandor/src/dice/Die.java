package dice;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import util.ShuffleUtil;
import canvas.IRenderable;
import canvas.IRenderer;
import deck.Card;

public class Die implements IRenderable<Die> {

	public static final Color DEFAULT_DIE_COLOR = Color.WHITE;
	public static final Color DEFAULT_LIFE_COLOR = Color.RED;
	
	public static final List<Color> colorOrder = new ArrayList<Color>();
	static {
		colorOrder.add(Color.WHITE);
		colorOrder.add(Color.BLUE);
		colorOrder.add(Color.BLACK);
		colorOrder.add(Color.RED);
		colorOrder.add(Color.GREEN);
		colorOrder.add(Color.GRAY);
		colorOrder.add(Color.YELLOW);
	}
	
	protected int maxValue;
	protected Color color;
	protected int value;
	protected int minValue;
	protected boolean cycles = true;
	protected IRenderer<Die> renderer;
	protected boolean hideValue = false;
	
	public Die(int minValue, int maxValue, Color color, int value) {
		this.setMinValue(minValue);
		this.setMaxValue(maxValue);
		this.setColor(color);
		this.setValue(value);
		renderer = new DieRenderer(this);
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	@Override
	public IRenderer<Die> getRenderer() {
		return renderer;
	}
	
	public void increment() {
		if(cycles && value == maxValue + minValue) {
			value = minValue;
		} else {
			value++;
		}
	}
	
	public void decrement() {
		if(cycles && value == minValue) {
			value = maxValue + minValue;
		} else {
			value--;
		}
	}
	

	public void roll() {
		roll(minValue, maxValue);
	}
	
	public void roll(int min, int max) {
		hideValue();
		setValue(ShuffleUtil.randInt(min, max));
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}
	
	public void hideValue() {
		this.hideValue = true;
	}
	
	public void showValue() {
		this.hideValue = false;
	}
	
	public boolean isHideValue() {
		return hideValue;
	}
	
	public void nextColor() {
		int idx = colorOrder.indexOf(getColor());
		if(idx < 0) {
			return;
		}
		if(idx == colorOrder.size() - 1) {
			idx = 0;
		} else {
			idx++;
		}
		setColor(colorOrder.get(idx));
	}

	@Override
	public IRenderable<Die> copyRenderable() {
		return new Die(minValue, maxValue, color, value);
	}
	
}
