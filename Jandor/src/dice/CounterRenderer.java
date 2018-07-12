package dice;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import util.ImageUtil;
import util.ShapeUtil;
import canvas.AbstractRenderer;
import canvas.CardLayer;
import canvas.Location;

public class CounterRenderer extends AbstractRenderer<Die> {

	protected Location textLocation;
	protected Font font;
	
	public CounterRenderer() {
		this(null);
	}
	
	public CounterRenderer(Die counter) {
		super(counter);
	}
	
	@Override
	public Shape computeBounds() {
		if(getScreenX() == -1 || getScreenY() == -1) {
			return null;
		}
		getImage(); // For width and height
		Shape s;
		
		int x = getScreenX();
		int y = getScreenY();
		
		int imgW = origImageW;
		int imgH = origImageH;
		
		int w = imgW;
		int h = imgH;
		
		s = new Ellipse2D.Float(x, y, imgW, imgH);
		
		if(getAngle() != 0) {
			s = ShapeUtil.rotate(s, getAngle());
		}
		
		font = new Font("Helvetica", Font.BOLD, (int) Math.round(imgH * 0.44));
		textLocation = new Location(x + w/2, y + h/2);
				
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
		return "counter";
	}

	@Override
	public String getBackImageAlias() {
		return "counter";
	}
	
	@Override
	public void render(CardLayer layer, Graphics2D g, Die counter, Location location) {
		BufferedImage img = getImage();
		g.drawImage(img, getScreenX(), getScreenY(), img.getWidth(), img.getHeight(), null);
		
		if(isHovered()) {
			g.setColor(new Color(255,255,255,20));
			g.fill(getBounds());
			
			/*g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(1));
			g.draw(getBounds());*/
		}
		
		g.setFont(font);
		String text;
		
		if(counter.isHideValue()) {
			text = "e/o";
		} else if(counter.getValue() == 0) {
			text = "";
		} else if(counter.getValue() > 0) {
			text = "" + counter.getValue();
		} else {
			text = "" + counter.getValue();
		}
		
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D textBounds = fontMetrics.getStringBounds(text, g);
		int textW = (int) textBounds.getWidth();
		
		g.setColor(Color.DARK_GRAY);
		g.drawString(text, textLocation.getScreenX() - textW/2 + 1, textLocation.getScreenY() + fontMetrics.getDescent() + 1);
		g.setColor(Color.WHITE);
		g.drawString(text, textLocation.getScreenX() - textW/2, textLocation.getScreenY() + fontMetrics.getDescent());
	}
	
	
}
