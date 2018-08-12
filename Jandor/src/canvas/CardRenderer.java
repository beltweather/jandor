package canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import util.ImageUtil;
import util.ShapeUtil;
import zone.ZoneType;
import deck.Card;

public class CardRenderer extends AbstractRenderer<Card> {
	
	public static boolean RENDER_BOUNDS = false;
	public static boolean RENDER_ZONE_TYPE = false;
	
	private transient CardLayer lastCardLayer = null;
	
	public CardRenderer() {
		this(null);
	}
	
	public CardRenderer(Card card) {
		super(card);
	}
	
	public Card getCard() {
		return getObject();
	}
	
	public String getImageUrl() {
		return ImageUtil.getUrl(getCard().getMultiverseId());
	}
	
	public String getBackImageUrl() {
		return ImageUtil.getUrl(-1);
	}
	
	public String getImageAlias() {
		return getCard().getName();
	}
	
	public String getBackImageAlias() {
		return "back";
	}

	@Override
	protected Shape computeBounds() {
		/*if(getScreenX() == -1 || getScreenY() == -1) {
			return null;
		}*/
		getImage(); // For width and height
		Shape s = new RoundRectangle2D.Float(getScreenX(), getScreenY(), origImageW, origImageH, ImageUtil.getCardCornerRadius(getScale()), ImageUtil.getCardCornerRadius(getScale()));
		if(getAngle() != 0) {
			s = ShapeUtil.rotate(s, getAngle());
		}
		return s;
	}
	
	@Override
	public void render(CardLayer layer, Graphics2D g, Card card, Location location) {
		lastCardLayer = layer;
		if(card == null) {
			return;
		}
		
		BufferedImage img = getImage(!hideCard(layer, card));
		Rectangle bounds = getBounds().getBounds();
		g.drawImage(img, (int) bounds.getX(), (int) bounds.getY(), null);
		
		if(isHovered() || RENDER_BOUNDS) {
			g.setColor(new Color(255,255,255,20));
			g.fill(getBounds());
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(1));
			if(RENDER_BOUNDS) {
				g.draw(getBounds());
			}
		}
		
		if(RENDER_ZONE_TYPE) {
			Color c;
			switch(getCard().getZoneType()) {
				case DECK:
					c = Color.RED;
					break;
				case GRAVEYARD:
					c = Color.ORANGE;
					break;
				case HAND:
					c = Color.YELLOW;
					break;
				case BATTLEFIELD:
					c = Color.BLUE;
					break;
				case EXILE:
					c = Color.PINK;
					break;
				case COMMANDER:
					c = Color.MAGENTA;
				default:
					c = null;
					break;
			}
			if(c != null) {
				g.setColor(c);
				g.setStroke(new BasicStroke(3));
				g.draw(getBounds());
			}
		}
		
	}
	
	public boolean hideCard(CardLayer layer, Card card) {

		if(!layer.isHideHand()) {
			return false;
		}
		
		boolean dragging = layer.getHandler().isDragged(card);
		if(!dragging) {
			for(CardLayer l : layer.getSyncedLayers()) {
				if(l.getHandler().isDragged(card)) {
					dragging = true; 
					break;
				}
			}
		}
		if(dragging) {
			if(card.getLastZoneType() == ZoneType.BATTLEFIELD || card.getLastZoneType() == ZoneType.GRAVEYARD) {
				return false;
			}
			return true;
		}
		return card.getZoneType() == ZoneType.HAND;
	}
	
	@Override
	public void toggleFaceUp() {
		this.faceUp = !this.faceUp;
	}

}
