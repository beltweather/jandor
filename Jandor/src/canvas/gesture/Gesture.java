package canvas.gesture;

import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import canvas.Location;

public abstract class Gesture implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Location> oldLocations = new ArrayList<Location>();
	private boolean performed = false;
	
	public Gesture() {}

	public void addNewLocation(MouseEvent e) {
		addNewLocation(new Location(e));
	}
	
	public void addNewLocation(Location location) {
		if(performed) {
			return;
		}
		
		if(oldLocations.size() > 0) {
			Location lastLocation = oldLocations.get(oldLocations.size() - 1);
			if(location.getScreenX() == lastLocation.getScreenX() &&
			   location.getScreenY() == lastLocation.getScreenY()) {
				return;
			}
		}
		
		oldLocations.add(location);
		maybePerformAction();
	}
	
	private void maybePerformAction() {
		if(oldLocations.size() > 0 && validateGesture(oldLocations)) {
			performAction();
			clear();
		}
	}
	
	public void clear() {
		oldLocations.clear();
	}
	
	public abstract boolean validateGesture(List<Location> locations);
	
	public abstract void performAction();

}
