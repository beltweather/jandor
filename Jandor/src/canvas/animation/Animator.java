package canvas.animation;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import canvas.Canvas;
import canvas.IRenderable;

public abstract class Animator<T extends IRenderable> extends TimerTask {
	
	private static final int DEFAULT_TIME = 5;
	
	protected Timer timer;
	protected Component canvas;
	protected List<T> objects;
	protected List<T> animateObjects;
	protected int step;
	protected int maxSteps = -1;
	protected boolean multiUse = false;
	int delay = DEFAULT_TIME;
	
	public Animator(Canvas canvas, T obj) {
		this(canvas, obj, -1);
	}
	
	public Animator(Component canvas, List<T> objects) {
		this(canvas, objects, -1);
	}
	
	public Animator(Component canvas, T obj, int maxSteps) {
		this(canvas, Arrays.asList(obj), maxSteps);
	}
	
	public Animator(Component canvas, List<T> objects, int maxSteps) {
		this.canvas = canvas;
		this.objects = objects;
		this.maxSteps = maxSteps;
		timer = new Timer(true);
	}
	
	public void setDelay(int timeMS) {
		this.delay = timeMS;
	}
	
	public void start() {
		step = 0;
		animateObjects = new ArrayList<T>(objects);
		startUpdate();
		for(T obj : animateObjects) {
			startUpdate(obj, step);
		}
		if(canvas != null) {
			canvas.repaint();
		}
		timer.schedule(this, 0, delay);
	}
	
	public void stop() {
		cancel();
		animateObjects.clear();
		for(T obj : objects) {
			stopUpdate(obj, step - 1);
		}
		stopUpdate();
		if(canvas != null) {
			canvas.repaint();
		}
		step = 0;
		if(!multiUse) {
			timer.cancel();
		}
	}
	
	public int getMaxSteps() {
		return maxSteps;
	}
	
	public boolean hasMaxSteps() {
		return maxSteps > -1;
	}
	
	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}
	
	public void clear() {
		objects.clear();
	}
	
	public void add(T obj) {
		objects.add(obj);
	}
	
	public void remove(T obj) {
		objects.remove(obj);
	}
	
	public void set(List<T> objects) {
		this.objects = objects;
	}
	
	public boolean isMultiUse() {
		return multiUse;
	}

	public void setMultiUse(boolean multiUse) {
		this.multiUse = multiUse;
	}
	
	/**
	 * Update the given obj.
	 * @return True if you should stop.
	 */
	public abstract boolean update(T obj, int step);
	
	public void startUpdate(T obj, int step) {}
	
	public void stopUpdate(T obj, int step) {}
	
	public void startUpdate() {}
	
	public void stopUpdate() {}
	
	@Override
	public void run() {
		Iterator<T> it = animateObjects.iterator();
		while(it.hasNext()) {
			T obj = it.next();
			if(update(obj, step)) {
				it.remove();
			}
		}
		
		step++;
		if(animateObjects.size() == 0 || (hasMaxSteps() && step == maxSteps)) {
			stop();
		}
		
		if(canvas != null) {
			canvas.repaint();
		}
	}
}