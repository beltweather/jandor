package zone;

import java.util.ArrayList;
import java.util.List;

public enum ZoneType {

	DECK("Deck", "deck.png"), 
	GRAVEYARD("Graveyard", "graveyard.png"), 
	HAND("Hand", "hand.png", false), 
	EXILE("Exile", "exile.png"), 
	COMMANDER("Command", "commander.png", false), 
	REVEAL("Revealed", "eye.png", true), 
	BATTLEFIELD("Battlefield", null, false, true), 
	NONE("None", null, false);
	
	public static List<ZoneType> getSortedValues() {
		List<ZoneType> zones = new ArrayList<ZoneType>();
		zones.add(DECK);
		zones.add(GRAVEYARD);
		zones.add(HAND);
		zones.add(EXILE);
		zones.add(COMMANDER);
		zones.add(REVEAL);
		zones.add(BATTLEFIELD);
		zones.add(NONE);
		return zones;
	}
	
	private String prettyString;
	private boolean countable;
	private boolean transformed;
	private String resourceName;
	
	private ZoneType(String prettyString, String resourceName) {
		this(prettyString, resourceName, true);
	}
	
	private ZoneType(String prettyString, String resourceName, boolean countable) {
		this(prettyString, resourceName, countable, false);
	}
	
	private ZoneType(String prettyString, String resourceName, boolean countable, boolean transformed) {
		this.prettyString = prettyString;
		this.resourceName = resourceName;
		this.countable = countable;
		this.transformed = transformed;
	}

	public boolean isCountable() {
		return countable;
	}
	
	public boolean isTransformedProjection() {
		return transformed;
	}
	
	public String getPrettyString() {
		return prettyString;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public boolean hasResourceName() {
		return resourceName != null;
	}
	
	public String toString() {
		return getPrettyString();
	}
	
}