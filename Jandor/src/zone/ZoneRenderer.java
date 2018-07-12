package zone;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.List;

import util.ImageUtil;
import util.ShuffleUtil;
import canvas.AbstractRenderer;
import canvas.CardLayer;
import canvas.IRenderable;
import canvas.Location;
import canvas.animation.Animator;
import deck.Card;
import dice.Token;

public class ZoneRenderer extends AbstractRenderer<Zone> {

	public final int ANCHOR_CENTER = 0;
	public final static int ANCHOR_BOTTOM = 1;
	
	protected int snapAnchor = ANCHOR_CENTER;
	private boolean shouldFan = false;
	
	public ZoneRenderer(Zone zone) {
		super(zone);
	}
	
	public Zone getZone() {
		return getObject();
	}
	
	@Override
	protected Shape computeBounds() {
		Zone z = getZone();
		if(z.getLocation() == null) {
			return null;
		}
		return new Rectangle(z.getLocation().getScreenX(), z.getLocation().getScreenY(), z.getWidth(), z.getHeight());
	}

	@Override
	public void render(CardLayer layer, Graphics2D g, Zone object, Location location) {
		BufferedImage img = getImage();
		if(img == null) {
			return;
		}
		
		Shape bounds = getBounds();
		if(bounds == null) {
			return;
		}
		Rectangle b = bounds.getBounds();
		
		g.setStroke(new BasicStroke(1));
		g.setColor(CardLayer.DEFAULT_BACKGROUND_COLOR);
		
		int x = (int) (b.getX() + (b.getWidth() - img.getWidth()) / 2);
		int y = (int) (b.getY() + (b.getHeight() - img.getHeight()) / 2);
		g.drawImage(img, x, y, null);
		//g.draw(getBounds());
	}
	
	public Location getCenter() {
		return new Location(getZone().getLocation().getScreenX() + getZone().getWidth() / 2,
						    getZone().getLocation().getScreenY() + getZone().getHeight() / 2);
		
	}
	
	public void center(final CardLayer layer, IRenderable obj, boolean animate) {
		Zone z = getZone();
		Location newLocation = new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) / 2,
										    z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) / 2);
		snap(layer, obj, animate, newLocation);
	}
	
	public void bottomLeft(final CardLayer layer, IRenderable obj, boolean animate, int marginRight, int marginBottom) {
		Zone z = getZone();
		Location newLocation = new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) - marginRight,
										    z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) - marginBottom);
		snap(layer, obj, animate, newLocation);
	}
	
	public void bottom(final CardLayer layer, IRenderable obj, boolean animate, int marginBottom) {
		Zone z = getZone();
		Location newLocation = new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) / 2,
										    z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) - marginBottom);
		snap(layer, obj, animate, newLocation);
	}
	
	public void fanCard(final CardLayer layer, IRenderable obj, boolean animate) {
		Location newLocation = getFanCardLocation(layer, obj);
		snap(layer, obj, animate, newLocation);
	}
	
	public Location getFanCardAnchor(Zone z, IRenderable obj) {
		return new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) / 2,
					        z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) / 2);
	}
	
	private Location getFanCardLocation(CardLayer layer, IRenderable obj) {
		Zone z = getZone();
		Location newLocation;
		
		if(snapAnchor == ANCHOR_CENTER) {
			newLocation = new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) / 2,
									   z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) / 2);
		} else {
			newLocation = new Location(z.getLocation().getScreenX() + (z.getWidth() - obj.getRenderer().getWidth()) / 2,
									   z.getLocation().getScreenY() + (z.getHeight() - obj.getRenderer().getHeight()) - 10);
		}
		
		// No offset the obj from center given its index
		int index = z.indexOf(obj);
		int size = z.size();
		int buffer = obj.getRenderer().getWidth() / 2;
		int width = z.getWidth() - buffer *2;
		int maxCellWidth = width / size;
		int cellWidth = Math.min((int) (Math.max(obj.getRenderer().getWidth()*0.75, 50)), maxCellWidth);
		int offsetX = (index - size / 2) * cellWidth;
		if(size % 2 == 0) {
			offsetX += cellWidth / 2;
		}
		newLocation.setScreenX(newLocation.getScreenX() + offsetX);
		
		return newLocation;
	}
	
	public <T extends IRenderable> void fan(final CardLayer layer, List<T> objects, boolean animate) {
		ShuffleUtil.positionSort(objects);
		for(int i = 0; i < objects.size(); i++) {
			T obj = objects.get(i);
			if(obj instanceof Card) {
				layer.move((Card) obj, 0);
			}
			if(obj instanceof Token) {
				continue;
			}
			fanCard(layer, obj, animate);
		}
		shouldFan = false;
	}
	
	public void snap(final CardLayer layer, IRenderable obj, boolean animate, final Location newLocation) {
		if(obj == null) {
			return;
		}
		
		if(!animate) {
			obj.getRenderer().setLocation(newLocation);
			return;
		} 
		
		final int maxStep = 1000;
		
		Animator<IRenderable> animator = new Animator<IRenderable>(layer.getCanvas(), obj) {

			@Override
			public boolean update(IRenderable obj, int step) {
				Location oldLocation = obj.getRenderer().getLocation();
				Location vector = new Location(newLocation.getScreenX() - oldLocation.getScreenX(),
											   newLocation.getScreenY() - oldLocation.getScreenY());
				
				double mag = Math.sqrt(vector.getScreenX() * vector.getScreenX() + vector.getScreenY() * vector.getScreenY());
				if(mag <= 20) {
					obj.getRenderer().setLocation(newLocation);
					return true;
				}
				
				int length = (int) Math.min(mag, 20);
				obj.getRenderer().setScreenX((int) Math.round(length * vector.getScreenX() / mag) + obj.getRenderer().getScreenX());
				obj.getRenderer().setScreenY((int) Math.round(length * vector.getScreenY() / mag) + obj.getRenderer().getScreenY());
				
				layer.flagChange();
				return step >= maxStep;
			}

			@Override
			public void startUpdate(IRenderable obj, int step) {
				obj.getRenderer().setCanDrag(false);
			}

			@Override
			public void stopUpdate(IRenderable obj, int step) {
				obj.getRenderer().setCanDrag(true);
			}
			
		};
		animator.start();
		
	}

	public boolean isShouldFan() {
		return shouldFan;
	}
	
	public void setShouldFan(boolean shouldFan) {
		this.shouldFan = shouldFan;
	}

	public void setSnapAnchor(int anchor) {
		this.snapAnchor = anchor;
	}

	@Override
	public String getImageUrl() {
		switch(getZone().getType()) {
			case DECK:
				return ImageUtil.getResourceUrl("deck.png");
			case HAND:
				return ImageUtil.getResourceUrl("hand.png");
			case GRAVEYARD:
				return ImageUtil.getResourceUrl("graveyard.png");
			case EXILE:
				return ImageUtil.getResourceUrl("exile.png");
			default:
				return null;
		}
	}

	@Override
	public String getBackImageUrl() {
		return null;
	}

	@Override
	public String getImageAlias() {
		return null;
	}

	@Override
	public String getBackImageAlias() {
		return null;
	}
	

}
