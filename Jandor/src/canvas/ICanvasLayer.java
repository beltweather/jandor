package canvas;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.List;

import zone.ZoneManager;

public interface ICanvasLayer<T> extends Serializable {

	public void paintComponent(Graphics2D g, int width, int height);
	
	public void repaint();
	
	public Canvas getCanvas();
	
	public int getScreenWidth();
	
	public int getScreenHeight();
	
	public ZoneManager getCardZoneManager();
	
	public List<Object> getListeners();
	
	public void flagChange();
	
}
