package zone;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import canvas.Canvas;
import canvas.CardLayer;
import canvas.IRenderable;
import canvas.Location;

public class ZoneManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<ZoneType, Zone> zones = new LinkedHashMap<ZoneType, Zone>();
	
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
	
	public void addZone(ZoneType type) {
		if(!zones.containsKey(type)) {
			zones.put(type, new Zone<IRenderable>(type, IRenderable.class));
		}
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
	
	public <T extends IRenderable> void setZones(Canvas canvas, CardLayer layer, boolean isDragging, List<T> objects) {	
		setZones(canvas, layer, isDragging, objects, true);
	}
	
	public <T extends IRenderable> void setZones(Canvas canvas, CardLayer layer, boolean isDragging, List<T> objects, boolean clearZones) {
		if(clearZones) {
			clearZones();
		}
		if(objects == null) {
			return;
		}
		
		for(IRenderable obj : objects) {
			Zone zone = findClosestZone(canvas, layer, isDragging && layer != null && layer.getHandler().isDragged(obj), obj);
			if(zone != null) {
				zone.add(obj);
				obj.getRenderer().setZoneType(zone.getType());
			}
		}
	}
	
	public Zone findClosestZone(Canvas canvas, CardLayer layer, boolean isDragging, IRenderable obj) {
		Location center;
		Location centerNoTransform;
		if(isDragging && layer.getHandler().getDragEnd() != null) {
			center = layer.getHandler().getDragEnd();
			centerNoTransform = center;
		} else {
			center = new Location((int) Math.round(obj.getRenderer().getScreenX() + obj.getRenderer().getWidth() / 2.0), 
								  (int) Math.round(obj.getRenderer().getScreenY() + obj.getRenderer().getHeight() / 2.0));
			centerNoTransform = center;
			if(obj.getRenderer().isTransformedProjection()) {
				centerNoTransform = canvas.getZoom().transform(center);
			}
		}
		
		
		if(!isDragging || (isDragging && !obj.getRenderer().hasMovedEnoughToFindNewZone(centerNoTransform))) {
			if(obj.getRenderer().getZoneType() == null || obj.getRenderer().getZoneType() == ZoneType.NONE) {
				return zones.get(ZoneType.BATTLEFIELD);
			}
			return zones.get(obj.getRenderer().getZoneType());
		}
		
		Class klass = obj.getClass();
		for(Zone zone : zones.values()) {
			Class zoneKlass = zone.getObjectClass();
			
			// We'll use battlefield as the default, so we never need to check if it's there,
			// we just need to check that it's not in any other zone.
			if(zone.getType() == ZoneType.BATTLEFIELD) {
				continue;
			}
			
			if(zone.getType() == ZoneType.COMMANDER && layer.isCommander() && !layer.getCommander().equals(obj)) {
				continue;
			}
			
			if(zoneKlass.isAssignableFrom(klass) && zone.overlaps(centerNoTransform)) {
				obj.getRenderer().setZoneChangeLocation(centerNoTransform);
				return zone;
			}
		}
		
		// Return battlefield as the default zone when not in any other.
		obj.getRenderer().setZoneChangeLocation(centerNoTransform);
		return zones.get(ZoneType.BATTLEFIELD);
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
