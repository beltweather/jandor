package jackson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jackson.AllCardsJson.CardJson;
import jackson.AllSetsJson.SetJson;

public class AllCardsJson extends HashMap<String, CardJson>  {

	private static final long serialVersionUID = 1L;

	public static final class CardJson {

		public List<String> colorIdentity;
		public List<String> colors;
		public double convertedManaCost;
		public String layout;
		public String manaCost;
		public String name;
		public String power;
		public List<String> printings;
		public String rarity;
		public List<String> subtypes;
		public List<String> supertypes;
		public String text;
		public String toughness;
		public String type;
		public List<String> types;
		public String uuid;

		@JsonIgnore
		public Map<String, List<Integer>> multiverseIdsBySetCode;

	}

	@JsonIgnore
	private boolean isInit = false;

	public void init(AllSetsJson sets) {
		if(isInit) {
			return;
		}
		sets.init();
		cache(sets);
		isInit = true;
	}

	protected void cache(AllSetsJson sets) {
		for(CardJson card : values()) {
			card.multiverseIdsBySetCode = new HashMap<>();
			for(String code : card.printings) {
				SetJson set = sets.get(code.toUpperCase());
				if(set == null || set.multiverseIdsByName.get(card.name) == null) {
					continue;
				}
				card.multiverseIdsBySetCode.put(code, set.multiverseIdsByName.get(card.name));
			}
		}
	}

}
