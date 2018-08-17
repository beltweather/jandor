package deck;

import java.io.Serializable;
import java.util.List;

import canvas.IRenderable;

public interface IRenderableList<T extends IRenderable<?>> extends List<T>, Iterable<T>, Serializable {

	public void add(T obj, int numberOfCopies);
	
	public void move(T obj, int index);
	
	public void remove(T obj);
	
	public void remove(T obj, boolean all);

	public void set(List<T> objects);
	
	public RenderableList<T> getCopy();
	
	public boolean hasDuplicate(T obj);
	
	public boolean hasDuplicate(String objName);
	
	public int getCount(T obj);
	
	public int getCount(String objName);

	public RenderableList<T> getShallowCopySortedByZIndex();
	
}
