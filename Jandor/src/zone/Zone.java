package zone;

import java.util.List;

import canvas.IRenderable;
import canvas.Location;
import deck.RenderableList;

public class Zone<T extends IRenderable> extends RenderableList<T> implements IRenderable<Zone> {

	protected ZoneType type;
	protected Location location;
	protected int width;
	protected int height;
	protected ZoneRenderer renderer;
	protected Class<T> objectClass;
	
	public Zone(ZoneType type, Class<T> objectClass) {
		this(type, objectClass, null);
	}
	
	public Zone(ZoneType type, Class<T> objectClass, List<T> objects) {
		super(objects);
		this.type = type;
		this.objectClass = objectClass;
		this.renderer = new ZoneRenderer(this);
	}
	
	public ZoneType getType() {
		return type;
	}
	
	@Override
	public ZoneRenderer getRenderer() {
		return renderer;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public boolean overlaps(Location location) {
		return getRenderer().overlaps(location);
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public IRenderable<Zone> copyRenderable() {
		return null;
	}
	
	public Class getObjectClass() {
		return objectClass;
	}

	@Override
	public String toString() {
		return "Zone: " + type.toString() + " (" + size() + ")";
	}
}

