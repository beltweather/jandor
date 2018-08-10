package zone;

import java.util.ArrayList;
import java.util.List;

public enum ZoneType {

	DECK, GRAVEYARD, HAND, EXILE, COMMANDER(false), BATTLEFIELD(false), NONE(false);
	
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
	
	private ZoneType() {
		this(true);
	}
	
	private ZoneType(boolean countable) {
		this.countable = countable;
	}

	public boolean isCountable() {
		return countable;
	}
	
}