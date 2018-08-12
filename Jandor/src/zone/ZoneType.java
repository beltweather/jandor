package zone;

import java.util.ArrayList;
import java.util.List;

public enum ZoneType {

	DECK, GRAVEYARD, HAND, EXILE, COMMANDER(false), BATTLEFIELD(false, true), NONE(false);
	
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
	
	private boolean countable;
	private boolean transformed;
	
	private ZoneType() {
		this(true);
	}
	
	private ZoneType(boolean countable) {
		this(countable, false);
	}
	
	private ZoneType(boolean countable, boolean transformed) {
		this.countable = countable;
		this.transformed = transformed;
	}

	public boolean isCountable() {
		return countable;
	}
	
	public boolean isTransformedProjection() {
		return transformed;
	}
	
}