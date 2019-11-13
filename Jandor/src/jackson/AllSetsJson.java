package jackson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jackson.AllSetsJson.SetJson;
import jackson.JacksonUtil.UpperCaseDeserializer;

public class AllSetsJson extends HashMap<String, SetJson> {

	private static final long serialVersionUID = 1L;

	public static final class SetJson {

		public List<Object> boosterV3;
		public List<SetCardJson> cards;
		@JsonDeserialize(using = UpperCaseDeserializer.class)
		public String code;
		public String name;
		public String releaseDate;
		public String type;

		@JsonIgnore
		public Map<String, SetCardJson> cardsByName;
		@JsonIgnore
		public Map<String, List<Integer>> multiverseIdsByName;

		protected void cache() {
			multiverseIdsByName = new HashMap<>();
			cardsByName = new HashMap<>();
			Iterator<SetCardJson> it = cards.iterator();
			while(it.hasNext()) {
				SetCardJson card = it.next();
				if(card.multiverseId == 0) {
					it.remove();
					continue;
				}
				if(!multiverseIdsByName.containsKey(card.name)) {
					multiverseIdsByName.put(card.name, new ArrayList<Integer>(1));
				}
				multiverseIdsByName.get(card.name).add(card.multiverseId);
				cardsByName.put(card.name, card);
			}
		}

	}

	public static final class SetCardJson {

		public String name;
		public int multiverseId;
		public String rarity;

		@JsonIgnore
		public String set;

	}

	@JsonIgnore
	private boolean isInit = false;

	public void init() {
		if(isInit) {
			return;
		}
		//fixKeys();
		cache();
		isInit = true;
	}

	/*protected void fixKeys() {
		Set<SetJson> values = new HashSet<>(values());
		clear();
		for(SetJson set : values) {
			if(set.type.equals("promo")) {
				//continue;
			}
			set.code = set.code.toUpperCase();
			put(set.code.toUpperCase(), set);
		}
	}*/

	protected void cache() {
		for(SetJson set : values()) {
			set.cache();
		}
	}

}
