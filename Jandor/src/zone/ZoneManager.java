package zone;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import canvas.IRenderable;
import canvas.Location;

public class ZoneManager implements Serializable {

	private static final long serialVersionUID = 1L;

	Map<ZoneType, Zone> zones = new LinkedHashMap<ZoneType, Zone>();
	
	public ZoneManager() {
		for(ZoneType type : ZoneType.getSortedValues()) {
			zones.put(type, new Zone<IRenderable>(type, IRenderable.class));
		}
	}
	
	public Set<ZoneType> getZoneTypes() {
		return zones.keySet();
	}
	
	public Collection<Zone> getZones() {
		return zones.values();
	}
	
	public Zone getZone(ZoneType type) {
		return zones.get(type);
	}
	
	public void removeZone(ZoneType type) {
		if(zones.containsKey(type)) {
			zones.remove(type);
		}
	}
	
	public void clearZone(ZoneType type) {
		getZone(type).clear();
	}
	
	public void clearZones() {
		for(ZoneType type : zones.keySet()) {
			clearZone(type);
		}
	}
	
	public <T extends IRenderable> void setZones(List<T> objects) {	
		setZones(objects, true);
	}
	
	public <T extends IRenderable> void setZones(List<T> objects, boolean clearZones) {
		if(clearZones) {
			clearZones();
		}
		if(objects == null) {
			return;
		}
		
		for(IRenderable obj : objects) {
			Zone zone = findClosestZone(obj);
			if(zone != null) {
				zone.add(obj);
				obj.getRenderer().setZoneType(zone.getType());
			}
		}
	}
	
	public Zone findClosestZone(IRenderable obj) {
		Location center = new Location(obj.getRenderer().getScreenX() + obj.getRenderer().getWidth() / 2, 
									   obj.getRenderer().getScreenY() + obj.getRenderer().getHeight() / 2);
		
		Class klass = obj.getClass();
		for(Zone zone : zones.values()) {
			Class zoneKlass = zone.getObjectClass();
			if(zoneKlass.isAssignableFrom(klass) && zone.overlaps(center)) {
				return zone;
			}
		}
		
		return null;
	}
	
	public Zone getZone(IRenderable obj) {
		for(Zone zone : zones.values()) {
			if(zone.hasDuplicate(obj)) {
				return zone;
			}
		}
		return null;
	}
	
}
