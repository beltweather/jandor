package canvas;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int screenX;
	private int screenY;
	
	public Location(MouseEvent e) {
		this(e.getX(), e.getY());
	}
	
	public Location(Point p) {
		this(p.getX(), p.getY());
	}
		
	public Location(double screenX, double screenY) {
		this((int) screenX, (int) screenY);
	} 
	
	public Location(int screenX, int screenY) {
		this.screenX = screenX;
		this.screenY = screenY;
	}
	
	public int getScreenX() {
		return screenX;
	}
	
	public int getScreenY() {
		return screenY;
	}
	
	public void setScreenX(int screenX) {
		this.screenX = screenX;
	}
	
	public void setScreenY(int screenY) {
		this.screenY = screenY;
	}
	
	public Point toPoint() {
		return new Point(screenX, screenY);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Location)) {
			return false;
		}
		Location l = (Location) obj;
		return screenX == l.screenX && screenY == l.screenY;
	}
	
}
