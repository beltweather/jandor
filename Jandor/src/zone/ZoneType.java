package zone;

import java.util.ArrayList;
import java.util.List;

public enum ZoneType {

	DECK("Deck"), GRAVEYARD("Graveyard"), HAND("Hand"), EXILE("Exile"), COMMANDER("Commander", false), BATTLEFIELD("Battlefield", false, true), NONE("None", false);
	
	public static List<ZoneType> getSortedValues() {
		List<ZoneType> zones = new ArrayList<ZoneType>();
		zones.add(DECK);
		zones.add(GRAVEYARD);
		zones.add(HAND);
		zones.add(EXILE);
		zones.add(COMMANDER);
		zones.add(BATTLEFIELD);
		zones.add(NONE);
		return zones;
	}
	
	private String prettyString;
	private boolean countable;
	private boolean transformed;
	
	private ZoneType(String prettyString) {
		this(prettyString, true);
	}
	
	private ZoneType(String prettyString, boolean countable) {
		this(prettyString, countable, false);
	}
	
	private ZoneType(String prettyString, boolean countable, boolean transformed) {
		this.prettyString = prettyString;
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
	
	public String toString() {
		return getPrettyString();
	}
	
}