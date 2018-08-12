package dice;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import util.ImageUtil;
import util.ShapeUtil;
import canvas.AbstractRenderer;
import canvas.CardLayer;
import canvas.Location;

public class TokenRenderer extends AbstractRenderer<Die> {

	protected Location textLocation;
	protected Location secondTextLocation;
	protected Font font;
	
	public TokenRenderer() {
		this(null);
	}
	
	public TokenRenderer(Die counter) {
		super(counter);
	}
	
	@Override
	public Shape computeBounds() {
		/*if(getScreenX() == -1 || getScreenY() == -1) {
			return null;
		}*/
		getImage(); // For width and height
		Shape s;
		
		int x = getScreenX();
		int y = getScreenY();
		
		int imgW = origImageW;
		int imgH = origImageH;
		
		int w = imgW;
		int h = imgH;
		
		s = new Ellipse2D.Float(x, y, imgW, imgH);
		
		s = new RoundRectangle2D.Float(getScreenX(), getScreenY(), origImageW, origImageH, ImageUtil.getCardCornerRadius(getScale()), ImageUtil.getCardCornerRadius(getScale()));
		if(getAngle() != 0) {
			s = ShapeUtil.rotate(s, getAngle());
		}
		
		font = new Font("Helvetica", Font.BOLD, (int) Math.min(Math.round(imgH * 0.44), 40));
		textLocation = new Location(x + w/3, y + h/2); //new Location(x + w/2, y + h/2);
		secondTextLocation = new Location(x + 2*w/3, y + h/2); //new Location(x + w/2, y + h/2);
		
		return s;
	}

	@Override
	public String getImageUrl() {
		return ImageUtil.getDieIconUrl(getObject());
	}
		
	@Override
	public String getBackImageUrl() {
		return ImageUtil.getDieIconUrl(getObject());
	}

	@Override
	public String getImageAlias() {
		return "token";
	}

	@Override
	public String getBackImageAlias() {
		return "token";
	}
	
	@Override
	public void render(CardLayer layer, Graphics2D g, Die counter, Location location) {
		BufferedImage img = getImage();
		Rectangle rect = getBounds().getBounds();
		g.drawImage(img, (int) rect.getX(), (int) rect.getY(), null);
		
		if(isHovered()) {
			g.setColor(new Color(255,255,255,45));
			g.clipRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth()/2, (int) rect.getHeight());
			g.fill(getBounds());
			g.setClip(null);
			
			g.setColor(new Color(255,255,255,25));
			g.clipRect((int) rect.getX() + (int) rect.getWidth()/2, (int) rect.getY(), (int) rect.getWidth()/2, (int) rect.getHeight());
			g.fill(getBounds());
			g.setClip(null);
			
			/*g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(1));
			g.draw(getBounds());*/
		}
		
		g.setFont(font);
		String text;
		String secondText;
		
		Token token = (Token) counter;
		if(token.isHideValue()) {
			text = "e/o";
		} else if(token.getValue() == 10) {
			text = "";
		} else if(token.getValue() > 0) {
			text = "" + token.getValue();
		} else {
			text = "" + token.getValue();
		}
		
		if(token.isHideValue()) {
			secondText = "e/o";
		} else if(token.getSecondValue() == 10) {
			secondText = "";
		} else if(token.getSecondValue() > 0) {
			secondText = "" + token.getSecondValue();
		} else {
			secondText = "" + token.getSecondValue();
		}
		
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D textBounds = fontMetrics.getStringBounds(text, g);
		int textW = (int) textBounds.getWidth();
		
		g.setColor(Color.DARK_GRAY);
		g.drawString(text, textLocation.getScreenX() - textW/2 + 1, textLocation.getScreenY() + fontMetrics.getDescent() + 1);
		g.drawString(secondText, secondTextLocation.getScreenX() - textW/2 + 1, secondTextLocation.getScreenY() + fontMetrics.getDescent() + 1);
		g.setColor(Color.WHITE);
		g.drawString(text, textLocation.getScreenX() - textW/2, textLocation.getScreenY() + fontMetrics.getDescent());
		g.drawString(secondText, secondTextLocation.getScreenX() - textW/2, secondTextLocation.getScreenY() + fontMetrics.getDescent());
		
	}
	
}
