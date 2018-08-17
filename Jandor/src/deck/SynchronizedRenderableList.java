package deck;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import canvas.IRenderable;

public class SynchronizedRenderableList<T extends IRenderable<?>> extends CopyOnWriteArrayList<T> implements IRenderableList<T> {

	private static final long serialVersionUID = 1L;
	
	public int screenW = 0;
	public int screenH = 0;
	
	public SynchronizedRenderableList() {
		this(null);
	}
	
	public SynchronizedRenderableList(List<T> objects) {
		if(objects != null) {
			addAll(objects);
		}
	}
	
	public void add(T obj, int numberOfCopies) {
		if(obj == null || numberOfCopies < 1) {
			return;
		}
		for(int i = 0; i < numberOfCopies; i++) {
			if(i == 0) {
				add(obj);
			} else {
				add((T) obj.copyRenderable());
			}
		}
	}
	
	public void move(T obj, int index) {
		if(index < 0) {
			index = size() + index;
		}
		if(obj == null || index < 0 || index >= size()) {
			return;
		}
		remove(obj);
		add(index, obj);
	}
	
	public void remove(T obj) {
		remove(obj, false);
	}
	
	public void remove(T obj, boolean all) {
		if(obj == null) {
			return;
		}
		
		Iterator<T> it = iterator();
		if(all) {
			while(it.hasNext()) {
				T c = it.next();
				if(c.equals(obj)) {
					super.remove(obj);
					//it.remove();
				}
			}
		} else {
			while(it.hasNext()) {
				T c = it.next();
				if(c == obj) {
					//it.remove();
					super.remove(c);
					break;
				}
			}
		}
	}
	
	public void set(List<T> objects) {
		this.clear();
		if(objects != null) {
			this.addAll(objects);
		}
	}
	
	public RenderableList<T> getCopy() {
		RenderableList<T> copies = new RenderableList<T>();
		for(T c : this) {	
			copies.add((T) c.copyRenderable());
		}
		return copies;
	}
	
	public boolean hasDuplicate(T obj) {
		if(obj == null) {
			return false;
		}
		return hasDuplicate(obj.getName());
	}
	
	public boolean hasDuplicate(String objName) {
		if(objName == null) {
			return false;
		}
		for(T c : this) {
			if(c.getName() != null && c.getName().equals(objName)) {
				return true;
			}
		}
		return false;
	}
	
	public int getCount(T obj) {
		if(obj == null) {
			return 0;
		}
		return getCount(obj.getName());
	}
	
	public int getCount(String objName) {
		if(objName == null) {
			return 0;
		}
		int i = 0;
		for(T c : this) {
			if(c.getName() != null && c.getName().equals(objName)) {
				i++;
			}
		}
		return i;
	}

	public RenderableList<T> getShallowCopySortedByZIndex() {
		RenderableList<T> list = new RenderableList<T>(this);
		Collections.sort(this, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return o1.getRenderer().getZIndex() - o2.getRenderer().getZIndex();
			}
			
		});
		return list;
	}
}
