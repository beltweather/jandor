package jackson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jackson.AllSetsJson.SetJson;

public class AllSetsJson extends HashMap<String, SetJson> {

	private static final long serialVersionUID = 1L;

	public static final class SetJson {

		public List<Object> boosterV3;
		public List<SetCardJson> cards;
		public String code;
		public String name;
		public String releaseDate;
		public String type;

		@JsonIgnore
		public Map<String, List<Integer>> multiverseIdsByName;

		protected void cache() {
			multiverseIdsByName = new HashMap<>();
			for(SetCardJson card : cards) {
				if(!multiverseIdsByName.containsKey(card.name)) {
					multiverseIdsByName.put(card.name, new ArrayList<Integer>(1));
				}
				multiverseIdsByName.get(card.name).add(card.multiverseId);
			}
		}

	}

	public static final class SetCardJson {

		public String name;
		public int multiverseId;

	}

	@JsonIgnore
	private boolean isInit = false;

	public void init() {
		if(isInit) {
			return;
		}
		fixKeys();
		cache();
		isInit = true;
	}

	protected void fixKeys() {
		Set<SetJson> values = new HashSet<>(values());
		clear();
		for(SetJson set : values) {
			if(set.type.equals("promo")) {
				continue;
			}
			put(set.code.toLowerCase(), set);
		}
	}

	protected void cache() {
		for(SetJson set : values()) {
			set.cache();
		}
	}

}
