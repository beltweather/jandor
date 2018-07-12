package canvas.gesture;

import java.util.List;

import canvas.Location;

public abstract class ShakeGesture extends Gesture {
	
	@Override
	public boolean validateGesture(List<Location> locations) {
		if(locations.size() < 3) {
			return false;
		}
		
		if(locations.size() > 200) {
			clear();
			return false;
		}
		
		int minSignChanges = 5;
		
		int signChanges = 0;
		int lastXDiff = 0;
		int lastYDiff = 0;
		int xDiff = 0;
		int yDiff = 0;
		
		for(int i = 1; i < locations.size(); i++) {
			Location lastLocation = locations.get(i - 1);
			Location location = locations.get(i);
			xDiff = location.getScreenX() - lastLocation.getScreenX();
			yDiff = location.getScreenY() - lastLocation.getScreenY();
			
			if(i > 1 && Math.abs(xDiff) > Math.abs(yDiff)) {
				if((xDiff > 0 && lastXDiff < 0) || (xDiff < 0 && lastXDiff > 0)) {
					signChanges++;
				}
			}

			if(signChanges >= minSignChanges) {
				return true;
			}
			
			lastXDiff = xDiff;
			lastYDiff = yDiff;
		}
		
		Location lastLocation = null;
		for(Location location : locations) {
			if(lastLocation == null) {
				lastLocation = location;
				continue;
			}
		}
		return false;
	}

}
