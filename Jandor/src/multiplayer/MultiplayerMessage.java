package multiplayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import deck.RenderableList;

public class MultiplayerMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final RenderableList<?> empty = new RenderableList();
	
	private RenderableList<?> renderables = empty;
	private boolean handViewable = false;
	private boolean deckViewable = false;
	private boolean graveyardViewable = true;
	private boolean exileViewable = true;
	private int lifeTotal = 20;
	private Map<String, Integer> commanderDamageByGUID = new HashMap<String, Integer>();
	
	public MultiplayerMessage() {
		
	}
	
	public RenderableList<?> getAllObjects() {
		return renderables;
	}
	
	public boolean isHandViewable() {
		return handViewable;
	}
	
	public boolean isDeckViewable() {
		return deckViewable;
	}
	
	public boolean isGraveyardViewable() {
		return graveyardViewable;
	}
	
	public boolean isExileViewable() {
		return exileViewable;
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
	
	public MultiplayerMessage setHandViewable(boolean handViewable) {
		this.handViewable = handViewable;
		return this;
	}
	
	public MultiplayerMessage setDeckViewable(boolean deckViewable) {
		this.deckViewable = deckViewable;
		return this;
	}
	
	public MultiplayerMessage setGraveyardViewable(boolean graveyardViewable) {
		this.graveyardViewable = graveyardViewable;
		return this;
	}
	
	public MultiplayerMessage setExileViewable(boolean exileViewable) {
		this.exileViewable = exileViewable;
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
}
