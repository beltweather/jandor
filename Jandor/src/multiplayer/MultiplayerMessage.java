package multiplayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import deck.RenderableList;
import zone.ZoneType;

public class MultiplayerMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final RenderableList<?> empty = new RenderableList();
	
	private RenderableList<?> renderables = empty;
	
	private Map<ZoneType, Boolean> viewableByZone = new HashMap<ZoneType, Boolean>();
	
	private int lifeTotal = 20;
	private Map<String, Integer> commanderDamageByGUID = new HashMap<String, Integer>();
	
	public MultiplayerMessage() {
		
	}
	
	public RenderableList<?> getAllObjects() {
		return renderables;
	}
	
	public Integer getLifeTotal() {
		return lifeTotal;
	}
	
	public Map<String, Integer> getCommanderDamageByGUID() {
		return commanderDamageByGUID;
	}
	
	public MultiplayerMessage setAllObjects(RenderableList<?> renderables) {
		this.renderables = renderables;
		return this;
	}

	public MultiplayerMessage setLifeTotal(int lifeTotal) {
		this.lifeTotal = lifeTotal;
		return this;
	}
	
	public MultiplayerMessage setCommanderDamage(String userGUID, int damage) {
		commanderDamageByGUID.put(userGUID, damage);
		return this;
	}
	
	public MultiplayerMessage setCommanderDamageByGUID(Map<String, Integer> commanderDamageByGUID) {
		this.commanderDamageByGUID = commanderDamageByGUID;
		return this;
	}
	
	public boolean isViewable(ZoneType zone) {
		return viewableByZone.containsKey(zone) ? viewableByZone.get(zone) : false;
	}
	
	public Map<ZoneType, Boolean> getViewableByZone() {
		return viewableByZone;
	}
	
	public MultiplayerMessage setViewable(ZoneType zone, boolean viewable) {
		viewableByZone.put(zone, viewable);
		return this;
	}
	
	public MultiplayerMessage setViewableByZone(Map<ZoneType, Boolean> viewableByZone) {
		this.viewableByZone = viewableByZone;
		return this;
	}
}
