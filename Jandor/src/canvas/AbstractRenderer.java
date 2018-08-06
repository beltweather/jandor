package canvas;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import util.ImageUtil;
import util.ShapeUtil;
import zone.ZoneType;

public abstract class AbstractRenderer<T> implements IRenderer<T> {
	
	public static final int TAP_ANGLE = 45;
	public static final int FULL_SCALE = -1;
	
	protected Location location = new Location(0, 0);
	protected T object;
	protected Shape bounds = null;
	protected boolean needsRecomputeBounds = true;
	protected double scale = 1.0;
	protected int angle = 0;
	protected int origImageW = -1;
	protected int origImageH = -1;
	protected boolean canDrag = true;
	protected boolean faceUp = true;
	protected ZoneType zoneType = ZoneType.NONE;
	protected ZoneType lastZoneType = null;
	protected boolean pendingZoneChange = false;
	protected boolean tapped;
	protected boolean hovered;
	protected int zIndex = 0;
	protected boolean visible = true;
	
	protected IRenderer parent;
	protected List<IRenderer> children = new ArrayList<IRenderer>();
	
	public AbstractRenderer(T object) {
		setObject(object);
	}
	
	@Override
	public T getObject() {
		return object;
	}
	
	@Override
	public void setObject(T object) {
		this.object = object;
	}
	
	@Override
	public int getScreenX() {
		return location.getScreenX();
	}
	
	@Override
	public int getScreenY() {
		return location.getScreenY();
	}
	
	@Override
	public void setScreenX(int screenX) {
		location.setScreenX(screenX);
		flagRecomputeBounds();
	}
	
	@Override
	public void setScreenY(int screenY) {
		location.setScreenY(screenY);
		flagRecomputeBounds();
	}
	
	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(int screenX, int screenY) {
		location.setScreenX(screenX);
		location.setScreenY(screenY);
		flagRecomputeBounds();
	}

	@Override
	public void setLocation(Location location) {
		this.location = location;
		flagRecomputeBounds();
	}
	
	@Override
	public Shape getBounds() {
		if(bounds == null) {
			bounds = computeBounds();
		}
		return bounds;
	}
	
	@Override
	public void flagRecomputeBounds() {
		needsRecomputeBounds = true;
	}
	
	@Override
	public void recomputeBounds() {
		recomputeBounds(false);
	}
	
	public void recomputeBounds(boolean force) {
		if(needsRecomputeBounds || force) {
			bounds = computeBounds();
			needsRecomputeBounds = false;
		}
	}
	
	protected abstract Shape computeBounds();
	
	@Override
	public boolean overlaps(Location location) {
		if(getBounds() == null) {
			return false;
		}
		/*if(getBounds().getBounds().getX() == -1 || getBounds().getBounds().getY() == -1) {
			System.err.println("Cannot determine overlaps without position, but will try anyway.");
			bounds = null;
		}*/
		return getBounds().contains(location.toPoint());
	}
	
	@Override
	public int getWidth() {
		return (int) getBounds().getBounds().getWidth();
	}
	
	@Override
	public int getHeight() {
		return (int) getBounds().getBounds().getHeight();
	}
	
	@Override
	public double getScale() {
		return scale;
	}

	@Override
	public void setScale(double scale) {
		if(this.scale == scale) {
			return;
		}

		this.scale = scale;
		flagRecomputeBounds();
	}
	
	public Location getCenter() {
		Rectangle bounds = getBounds().getBounds();
		return new Location(getScreenX() + bounds.width / 2, getScreenY() + bounds.height / 2);
	}
	
	@Override
	public void restoreScale() {
		this.scale = 1.0;
		flagRecomputeBounds();
	}
	
	@Override
	public boolean isScaleRestored() {
		return scale == 1.0;
	}

	@Override
	public void incrementAngle(int deltaAngle) {
		setAngle(angle + ShapeUtil.toPositiveAngle(deltaAngle));
	}
	
	@Override
	public void decrementAngle(int deltaAngle) {
		setAngle(angle - ShapeUtil.toPositiveAngle(deltaAngle));
	}
	
	@Override
	public int getAngle() {
		return angle;
	}
	
	@Override
	public void setAngle(int angle) {
		angle = ShapeUtil.toPositiveAngle(angle);
		if(this.angle != angle) {
			this.angle = angle;
			flagRecomputeBounds();
		}
	}

	@Override
	public boolean isAngleRestored() {
		return isTapped() ? getAngle() == CardRenderer.TAP_ANGLE : getAngle() == 0;
	}
	
	@Override
	public void restoreAngle() {
		if(isTapped()) {
			angle = TAP_ANGLE;
		} else {
			angle = 0;
		}
		flagRecomputeBounds();
	}
	
	@Override
	public boolean isCanDrag() {
		return canDrag;
	}

	@Override
	public void setCanDrag(boolean canDrag) {
		this.canDrag = canDrag;
	}

	@Override
	public boolean isFaceUp() {
		return faceUp;
	}
	
	@Override
	public void toggleFaceUp() {
		this.faceUp = !this.faceUp;
	}
	
	@Override
	public void setFaceUp(boolean faceUp) {
		this.faceUp = faceUp;
	}
	
	@Override
	public ZoneType getZoneType() {
		return zoneType;
	}
	
	@Override
	public void setZoneType(ZoneType zoneType) {
		if(this.zoneType != zoneType) {
			this.zoneType = zoneType;
			flagPendingZoneChange();
		}
	}
	
	@Override
	public boolean hasChangedZones() {
		return lastZoneType != null && lastZoneType != zoneType;
	}
	
	@Override
	public ZoneType getLastZoneType() {
		return lastZoneType;
	}
	
	@Override
	public void rememberLastZoneType() {
		lastZoneType = zoneType; 
	}
	
	@Override
	public void forgetLastZoneType() {
		lastZoneType = null;
	}
		
	@Override
	public void flagPendingZoneChange() {
		pendingZoneChange = true;
	}
	
	@Override
	public void clearPendingFlagZoneChange() {
		pendingZoneChange = false;
	}
	
	@Override
	public boolean hasPendingZoneChange() {
		return pendingZoneChange;
	}
	
	@Override
	public boolean isTapped() {
		return tapped;
	}
	
	@Override
	public void setTapped(boolean tapped) {
		if(tapped && !this.tapped) {
			angle = TAP_ANGLE;
		} else if(!tapped && this.tapped) {
			angle = 0;
		}
		this.tapped = tapped;
		flagRecomputeBounds();
	}
	
	@Override
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}
	
	@Override
	public boolean isHovered() {
		return hovered;
	}
	
	@Override
	public BufferedImage getImage() {
		return getImage(true, getScale());
	}
	
	@Override
	public BufferedImage getImage(double scale) {
		return getImage(true, scale);
	}
	
	@Override
	public BufferedImage getImage(boolean faceUp) {
		return getImage(faceUp, getScale());
	}
	
	@Override
	public BufferedImage getImage(boolean faceUp, double scale) {
		boolean full = scale == FULL_SCALE;
		BufferedImage img;
		img = loadImage(isFaceUp() && faceUp, full ? 1.0 : scale * ImageUtil.getScale());
		if(img == null) {
			return null;
		}
		if(img.getWidth() != origImageW || img.getHeight() != origImageH) {
			flagRecomputeBounds();
		}
		origImageW = img.getWidth();
		origImageH = img.getHeight();
		if(getAngle() != 0) {
			img = ImageUtil.rotate(img, getAngle());
		}
		return img;
	}
	
	protected BufferedImage loadImage(boolean faceUp, double scale) {
		if(!faceUp) {
			return ImageUtil.readImage(getBackImageUrl(), scale, getBackImageAlias());
		}
		return ImageUtil.readImage(getImageUrl(), scale, getImageAlias());
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public void clearChildren() {
		children.clear();
	}
	
	public void addChild(IRenderer obj) {
		if(!children.contains(obj)) {
			obj.removeFromParent();
			children.add(obj);
			obj.setParent(this);
		}
	}
	
	public void removeChild(IRenderer obj) {
		if(children.contains(obj)) {
			children.remove(obj);
			obj.setParent(null);
		}
	}
	
	public List<IRenderer> getChildren() {
		return children;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public IRenderer getParent() {
		return parent;
	}
	
	public void setParent(IRenderer parent) {
		this.parent = parent;
	}
	
	public void removeFromParent() {
		if(hasParent()) {
			parent.removeChild(this);
		}
	}
	
	public void removeChildren() {
		List<IRenderer> childs = new ArrayList<IRenderer>(children);
		for(IRenderer child : childs) {
			child.removeFromParent();
		}
	}
	
	public int getZIndex() {
		return zIndex;
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public void paintComponent(CardLayer layer, Graphics2D g, int width, int height) {
		recomputeBounds();
		if(visible) {
			render(layer, g, getObject(), getLocation());
		}
		zIndex = layer.nextZIndex();
	}
	
	@Override
	public abstract void render(CardLayer layer, Graphics2D g, T object, Location location);
	
}

