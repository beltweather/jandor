package canvas;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

import zone.ZoneType;

public interface IRenderer<T> extends Serializable {

	public T getObject();
	
	public void setObject(T object);
	
	public int getScreenX();
	
	public int getScreenY();
	
	public void setScreenX(int screenX);
	
	public void setScreenY(int screenY);
	
	public Location getLocation();
	
	public void setLocation(int screenX, int screenY);
	
	public void setLocation(Location location);
	
	public Shape getBounds();
	
	public void flagRecomputeBounds();
	
	public void recomputeBounds();
	
	public int getWidth();
	
	public int getHeight();
	
	public boolean overlaps(Location location);
	
	public double getScale();

	public void setScale(double scale);
	
	public void restoreScale();
	
	public boolean isScaleRestored();
	
	public void incrementAngle(int deltaAngle);
	
	public void decrementAngle(int deltaAngle);
	
	public int getAngle();

	public void setAngle(int angle);
	
	public boolean isCanDrag();

	public void setCanDrag(boolean canDrag);
	
	public boolean isAngleRestored();
	
	public void restoreAngle();

	public boolean isFaceUp();
	
	public void setFaceUp(boolean faceUp);
	
	public void toggleFaceUp();
	
	public ZoneType getZoneType();

	public void setZoneType(ZoneType zoneType);
	
	public boolean hasChangedZones();
	
	public ZoneType getLastZoneType();
	
	public void rememberLastZoneType();
	
	public void forgetLastZoneType();
	
	public void flagPendingZoneChange();
	
	public void clearPendingFlagZoneChange();
	
	public boolean hasPendingZoneChange();
	
	public BufferedImage getImage();
	
	public BufferedImage getImage(double scale);
	
	public BufferedImage getImage(boolean faceUp);
	
	public BufferedImage getImage(boolean faceUp, double scale);
	
	public String getImageUrl();
	
	public String getBackImageUrl();
	
	public String getImageAlias();
	
	public String getBackImageAlias();
	
	public void paintComponent(CardLayer layer, Graphics2D g, int width, int height);
	
	/**
	 * Primary method to render the object his renderer represents at the given location.
	 */
	public void render(CardLayer layer, Graphics2D g, T object, Location location);

	public boolean isTapped();
	
	public void setTapped(boolean tapped);
	
	public boolean hasChildren();
	
	public void clearChildren();
	
	public void addChild(IRenderer obj);
	
	public void removeChild(IRenderer obj);
	
	public List<IRenderer> getChildren();
	
	public boolean hasParent();

	public IRenderer getParent();

	public void setParent(IRenderer parent);

	public void removeFromParent();
	
	public void removeChildren();
	
	public void setHovered(boolean hovered);
	
	public boolean isHovered();
	
	public int getZIndex();
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
}
