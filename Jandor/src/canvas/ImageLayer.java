package canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import util.ImageUtil;
import zone.ZoneManager;

public class ImageLayer implements ICanvasLayer {
	
	private BufferedImage img = null;
	private int screenW;
	private int screenH;
	
	public ImageLayer() {
		this(null);
		screenW = -1;
		screenH = -1;
	}
	
	public ImageLayer(BufferedImage img) {
		this.img = img;
	}
	
	@Override
	public void paintComponent(Graphics2D g, int width, int height) {
		if(img == null) {
			g.setColor(new Color(100, 0, 0));
			g.fillRect(0, 0, width, height);
			return;
		}
		
		boolean rescale = false;
		if(screenW != width) {
			screenW = width;
			rescale = true;
		}
		
		if(screenH != height) {
			screenH = height;
			rescale = true;
		}
		
		if(rescale) {
			img = ImageUtil.scale(img, width, height);
		}
		
		g.drawImage(img, null, 0, 0);
	}

	@Override
	public void repaint() {
		
	}

	public BufferedImage getImage() {
		return img;
	}

	public void setImage(BufferedImage img) {
		this.img = img;
	}

	@Override
	public Canvas getCanvas() {
		return null;
	}

	@Override
	public int getScreenWidth() {
		return screenW;
	}

	@Override
	public int getScreenHeight() {
		return screenH;
	}

	@Override
	public ZoneManager getCardZoneManager() {
		return null;
	}

	@Override
	public List getListeners() {
		return new ArrayList();
	}

	@Override
	public void flagChange() {
		
	}
	
}
