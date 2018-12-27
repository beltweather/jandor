package jackson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jackson.AllCardsJson.CardJson;
import jackson.AllSetsJson.SetJson;
import util.CardUtil;

public class AllCardsJson extends HashMap<String, CardJson>  {

	private static final long serialVersionUID = 1L;

	public static final class CardJson {

		public List<String> colorIdentity;
		public List<String> colors;
		public double convertedManaCost;
		public String layout;
		public String loyalty;
		public String manaCost;
		public String name;
		public List<String> names;
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

		public String getString(String fieldName) {
			Object value = get(fieldName);
			if(value == null) {
				return null;
			}
			return String.valueOf(value);
		}

		public Object get(String fieldName) {
			try {
				Field field = CardJson.class.getDeclaredField(fieldName);
				if(field == null) {
					return null;
				}
				return field.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	@JsonIgnore
	private boolean isInit = false;

	public void init(AllSetsJson sets) {
		if(isInit) {
			return;
		}
		fixNames();
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

	protected void fixNames() {
		Set<String> names = new HashSet<>(keySet());
		for(String name : names) {
			CardJson card = get(name);
			String cleanName = CardUtil.clean(name);
			if(CardUtil.isWeirdName(cleanName)) {
				System.err.println("Found Weird Card Name: " + cleanName);
			}
			if(!cleanName.equals(name)) {
				card.name = cleanName;
				remove(name);
				put(cleanName, card);
			}
			if(card.names != null && card.names.size() > 0) {
				for(int i = 0; i < card.names.size(); i++) {
					String n = card.names.get(i);
					String cleanN = CardUtil.clean(n);
					if(!cleanN.equals(n)) {
						card.names.set(i, cleanN);
					}
				}
			}
		}
	}

}
