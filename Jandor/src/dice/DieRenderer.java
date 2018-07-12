package dice;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import util.ImageUtil;
import util.ShapeUtil;
import canvas.AbstractRenderer;
import canvas.CardLayer;
import canvas.Location;

public class DieRenderer extends AbstractRenderer<Die> {
	
	protected Location textLocation;
	protected Font font;
	
	public DieRenderer() {
		this(null);
	}
	
	public DieRenderer(Die die) {
		super(die);
	}
	
	public Die getDie() {
		return getObject();
	}
	
	@Override
	public Shape computeBounds() {
		if(getScreenX() == -1 || getScreenY() == -1) {
			return null;
		}
		getImage(); // For width and height
		Shape s;
		
		int imgW = origImageW;
		int imgH = origImageH;
		
		int w = (int) Math.round(imgW*0.92);
		int h = (int) Math.round(imgH*0.94);
		
		int x = getScreenX() + (imgW - w)/2; 
		int y = getScreenY() + (imgH - h)/2;
		
		int s0 = (int) Math.round(0.45*h);
		int s1 = (int) Math.round(0.43*h);
		
		Polygon p = new Polygon();
		p.addPoint(x+w/2, y);
		p.addPoint(x+w, y+s0);
		p.addPoint(x+w, y+h-s1);
		p.addPoint(x+w/2, y+h);
		p.addPoint(x, y+h-s1);
		p.addPoint(x, y+s0);
		p.addPoint(x+w/2, y);
		s = p;
		if(getAngle() != 0) {
			s = ShapeUtil.rotate(s, getAngle());
		}
		
		font = new Font("Helvetica", Font.BOLD, (int) Math.round(imgH * 0.30));
		textLocation = new Location(x + w/2, y + h/2);
				
		return s;
	}

	@Override
	public String getImageUrl() {
		return ImageUtil.getDieIconUrl(getDie());
	}
		
	@Override
	public String getBackImageUrl() {
		return ImageUtil.getDieIconUrl(getDie());
	}

	@Override
	public String getImageAlias() {
		return "d" + getDie().getMaxValue();
	}

	@Override
	public String getBackImageAlias() {
		return "d" + getDie().getMaxValue();
	}
	
	@Override
	public void render(CardLayer layer, Graphics2D g, Die die, Location location) {
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
		String text = die.isHideValue() ? "?" : die.getValue() + "";
		int textW = (int) g.getFontMetrics().getStringBounds(text, g).getWidth();
		
		g.setColor(Color.DARK_GRAY);
		g.drawString(text, textLocation.getScreenX() - textW/2 + 1, textLocation.getScreenY() + 1);
		g.setColor(Color.WHITE);
		g.drawString(text, textLocation.getScreenX() - textW/2, textLocation.getScreenY());
	}

}
