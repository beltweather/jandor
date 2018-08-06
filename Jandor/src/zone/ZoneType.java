package zone;

import java.util.ArrayList;
import java.util.List;

public enum ZoneType {

	DECK, GRAVEYARD, HAND, EXILE, COMMANDER, BATTLEFIELD, NONE;
	
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
	
}
