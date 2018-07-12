package canvas.animation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canvas.Canvas;
import canvas.IRenderable;
import deck.Card;

public class SpinAnimator<T extends IRenderable> extends Animator<T> {
	
	private static final int DEFAULT_DELTA_ANGLE = 10;
	private static final int DEFAULT_TARGET_ANGLE = 0;
	private static final boolean DEFAULT_CLOCKWISE = true;
	private static final int FINAL_ANGLE_NONE = Integer.MIN_VALUE;
	
	private boolean clockwise;
	private int targetAngle;
	private int deltaAngle;
	private int finalAngle;
	
	private Map<T, Integer> targetAngleByObject = new HashMap<T, Integer>();

	public SpinAnimator(Canvas canvas, T obj) {
		this(canvas, Arrays.asList(obj));
	}
	
	public SpinAnimator(Canvas canvas, T obj, int targetAngle) {
		this(canvas, Arrays.asList(obj), targetAngle);
	}
	
	public SpinAnimator(Canvas canvas, T obj, int targetAngle, boolean clockwise) {
		this(canvas, Arrays.asList(obj), targetAngle, clockwise);
	}
	
	public SpinAnimator(Canvas canvas, T obj, int targetAngle, boolean clockwise, int deltaAngle) {
		this(canvas, Arrays.asList(obj), targetAngle, clockwise, deltaAngle, FINAL_ANGLE_NONE);
	}
	
	public SpinAnimator(Canvas canvas, List<T> objects) {
		this(canvas, objects, DEFAULT_TARGET_ANGLE);
	}
	
	public SpinAnimator(Canvas canvas, List<T> objects, int targetAngle) {
		this(canvas, objects, targetAngle, DEFAULT_CLOCKWISE);
	}
	
	public SpinAnimator(Canvas canvas, List<T> objects, int targetAngle, boolean clockwise) {
		this(canvas, objects, targetAngle, clockwise, DEFAULT_DELTA_ANGLE, FINAL_ANGLE_NONE);
	}
	
	public SpinAnimator(Canvas canvas, List<T> objects, int spinAngle, boolean clockwise, int deltaAngle, int finalAngle) {
		super(canvas, objects);
		this.targetAngle = spinAngle;
		this.clockwise = clockwise;
		this.deltaAngle = deltaAngle;
		this.finalAngle = finalAngle;
		this.setMaxSteps(spinAngle / deltaAngle);
	}
	
	@Override
	public void startUpdate(T obj, int step) {
		targetAngleByObject.put(obj, finalAngle == FINAL_ANGLE_NONE ? (obj.getRenderer().getAngle() + targetAngle) : finalAngle);
	}
	
	@Override
	public void stopUpdate(T obj, int step) {
		obj.getRenderer().setAngle(targetAngleByObject.get(obj));
	}
	
	@Override
	public boolean update(T obj, int step) {
		
		if(clockwise) {
			obj.getRenderer().incrementAngle(deltaAngle);
		} else {
			obj.getRenderer().decrementAngle(deltaAngle);
		}
		
		return false;
	}

}
