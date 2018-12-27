package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import deck.Card;
import jackson.AllCardsJson;
import jackson.AllCardsJson.CardJson;
import jackson.AllSetsJson;
import jackson.JacksonUtil;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class CardUtil {

	private CardUtil() {}

	private static final String GATHERER_URL = "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=";

	private static Set<String> bannedSets = new HashSet<String>();
	static {
		bannedSets.add("1ED");
		bannedSets.add("2ED");
		bannedSets.add("3ED");
		bannedSets.add("4ED");
		bannedSets.add("5ED");
		bannedSets.add("6ED");
		bannedSets.add("7ED");
		bannedSets.add("8ED");
		bannedSets.add("UGL");
		bannedSets.add("UNG");
		bannedSets.add("UNH");
		bannedSets.add("S00");
		bannedSets.add("VAN");
		bannedSets.add("HOP");
		bannedSets.add("ARC");
		bannedSets.add("PC2");
		bannedSets.add("CNS");
		bannedSets.add("UST");
	}

	private static Set<String> keysWithValues = new LinkedHashSet<String>();
	static {
		keysWithValues.add("subtypes");
		keysWithValues.add("supertypes");
		keysWithValues.add("types");
		keysWithValues.add("rarity"); // XXX ??? Get these from caching set data probably
		keysWithValues.add("set"); // XXX ??? Get these from caching set data probably
		keysWithValues.add("manaCost");
		keysWithValues.add("layout");
	}

	private static List<String> basicLandNames = new ArrayList<String>();
	static {
		basicLandNames.add("Plains");
		basicLandNames.add("Island");
		basicLandNames.add("Swamp");
		basicLandNames.add("Mountain");
		basicLandNames.add("Forest");
	}

	private static List<String> rarityOrder = new ArrayList<String>();
	static {
		rarityOrder.add("Common");
		rarityOrder.add("Uncommon");
		rarityOrder.add("Rare");
		rarityOrder.add("Mythic Rare");
		rarityOrder.add("Special");
	}


	private static JSONObject allSets = null;
	private static JSONObject allCards = null;
	private static AllSetsJson allSetsJson = null;
	private static AllCardsJson allCardsJson = null;
	private static List<String> allCardNames = new ArrayList<String>();
	private static Map<String, String> cardNamesByLowerCase = new HashMap<String, String>();
	private static List<String> cardAttributes = null;
	private static Map<String, HashSet<String>> valuesByKey = new HashMap<String, HashSet<String>>();

	public static void init() {
		loadAllSets();
		loadAllCards();
		fixCards();
		loadImages();
	}

	/*static {
		loadAllSets();
		loadAllCards();
		fixCards();
	}*/

	/*private static void recordValuesOld(JSONObject obj) throws JSONException {
		JSONObject setObj = getSetCardInfo(obj.getString("name"));
		boolean trashy = false;
		/if(obj.has("printings")) {
			JSONArray sets = obj.getJSONArray("printings");
			for(int i = 0; i < sets.length(); i++) {
				String set = sets.getString(i);
				if(bannedSets.contains(set)) {
					trashy = true;
					break;
				}
			}
		}/
		for(String key : keysWithValues) {
			if(trashy && !key.equals("set")) {
				continue;
			}
			Object value = null;
			if(obj.has(key)) {
				value = obj.get(key);
			} else if(setObj != null && setObj.has(key)) {
				value = setObj.get(key);
			} else {
				continue;
			}

			if(key.equals("manaCost")) {
				List<String> mana = ManaUtil.jsonToGatherer((String) value);
				for(String m : mana) {
					recordValue(key, m);
				}
			} else if(value instanceof String) {
				recordValue(key, (String) value);
			} else if(value instanceof JSONArray) {
				for(int i = 0; i < ((JSONArray) value).length(); i++) {
					recordValue(key, ((JSONArray) value).getString(i));
				}
			}
		}
	}*/

	private static Map<String, Field> fieldsByKey = new HashMap<>();

	private static void recordValues(CardJson card) throws JSONException {

		for(String key : keysWithValues) {
			if(!fieldsByKey.containsKey(key)) {
				Field field = null;
				try {
					field = CardJson.class.getDeclaredField(key);
				} catch (NoSuchFieldException | SecurityException e) {
					e.printStackTrace();
				}
				fieldsByKey.put(key, field);
			}

			Field field = fieldsByKey.get(key);
			if(field == null) {
				continue;
			}

			Object value = null;
			try {
				value = field.get(card);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

			if(value == null) {
				continue;
			}

			if(key.equals("manaCost")) {
				List<String> mana = ManaUtil.jsonToGatherer((String) value);
				for(String m : mana) {
					recordValue(key, m);
				}
			} else if(value instanceof String) {
				recordValue(key, (String) value);
			} else if(value instanceof List) {
				for(Object item : (List) value) {
					recordValue(key, String.valueOf(item));
				}
			}
		}
	}

	private static void recordValue(String key, String value) {
		if(value.equals("Phenomenon") || value.equals("Special")) {
			return;
		}

		if(!valuesByKey.containsKey(key)) {
			valuesByKey.put(key, new HashSet<String>());
		}
		if(!valuesByKey.get(key).contains(value)) {
			valuesByKey.get(key).add(value);
		}
	}

	public static Set<String> getValues(String attribute) {
		if(!valuesByKey.containsKey(attribute)) {
			return new HashSet<String>();
		}
		return valuesByKey.get(attribute);
	}

	private static void loadAllCards() {
		//allCards = JSONUtil.toJSON(FileUtil.RESOURCE_CARDS_JSONS);
		allCardsJson = JacksonUtil.readExternal(AllCardsJson.class, FileUtil.RESOURCE_CARDS_LESS_JSONS);
	}

	public static String toCardName(String cardName) {
		return toCardName(cardName, false);
	}

	public static String toCardName(String cardName, boolean findClosest) {
		if(cardName == null || cardName.length() == 0) {
			return null;
		}
		cardName = cardName.toLowerCase();
		if(!cardNamesByLowerCase.containsKey(cardName)) {
			if(findClosest) {
				return findClosestCardName(cardName);
			}
			return null;
		}
		return cardNamesByLowerCase.get(cardName);
	}

	private static String findClosestCardName(String cardName) {
		cardName = cardName.toLowerCase();
		String closestName = null;
		int minDistance = Integer.MAX_VALUE;
		for(String name : cardNamesByLowerCase.keySet()) {
			int distance = StringUtils.getLevenshteinDistance(cardName, name);
			if(distance < minDistance) {
				closestName = name;
				minDistance = distance;
			}
		}
		if(closestName == null) {
			return null;
		}
		return cardNamesByLowerCase.get(closestName);
	}

	public static boolean hasType(Card card, String type) throws JSONException {
		JSONArray types = card.getTypes();
		for(int i = 0; i < types.length(); i++) {
			if(types.getString(i).equals(type)) {
				return true;
			}
		}
		return false;
	}

	/*private static void recordValuesOld() {
		Iterator it = allCards.keys();
		try {
			top: while(it.hasNext()) {
				String name = it.next().toString();
				JSONObject info = allCards.getJSONObject(name);
				cardNamesByLowerCase.put(name.toLowerCase(), name);

				if(info.has("types")) {
					JSONArray types = info.getJSONArray("types");
					for(int i = 0; i < types.length(); i++) {
						String type = types.getString(i);
						if(type.equals("Vanguard") || type.equals("Scheme") || /type.equals("Conspiracy") ||/ type.equals("Plane")) {
							//it.remove();
							continue top;
						}
					}
				}

				if(info.has("printings")) {
					JSONArray sets = info.getJSONArray("printings");
					if(sets.length() == 1) {
						for(int i = 0; i < sets.length(); i++) {
							String set = sets.getString(i);
							if(set.equals("UNH") || set.equals("UGL") || set.equals("UST")/ || set.equals("pCEL")/) {
								//it.remove();
								continue top;
							}
						}
					}
				}

				allCardNames.add(name);
				recordValues(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}*/

	private static void recordValues() {
		Iterator it = allCardsJson.keySet().iterator();
		try {
			top: while(it.hasNext()) {
				String name = it.next().toString();
				CardJson info = allCardsJson.get(name);
				cardNamesByLowerCase.put(name.toLowerCase(), name);

				if(info.types != null) {
					for(String type : info.types) {
						if(type.equals("Vanguard") || type.equals("Scheme") || /*type.equals("Conspiracy") ||*/ type.equals("Plane")) {
							//it.remove();
							continue top;
						}
					}
				}

				if(info.printings != null) {
					if(info.printings.size() == 1) {
						for(String set : info.printings) {
							if(set.equals("UNH") || set.equals("UGL") || set.equals("UST")/* || set.equals("pCEL")*/) {
								//it.remove();
								continue top;
							}
						}
					}
				}

				allCardNames.add(name);
				recordValues(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void loadAllSets() {
		allSets = JSONUtil.toJSON(FileUtil.RESOURCE_SETS_JSONS);
	}

	private static void loadImages() {
		for(String mana : valuesByKey.get("manaCost")) {
			ImageUtil.readImage(ImageUtil.getSymbolUrl(mana, ManaUtil.SIZE_SMALL), mana);
		}
	}

	public static List<String> getAllCardNames() {
		return allCardNames;
	}

	public static List<String> getAllBasicLandNames() {
		return basicLandNames;
	}

	public static boolean isBasicLandName(String cardName) {
		for(String landName : basicLandNames) {
			if(landName.equalsIgnoreCase(cardName)) {
				return true;
			}
		}
		return false;
	}

	private static void fixCards() {
		Iterator setIds = allSets.keys();
		try {
			while(setIds.hasNext()) {
				String setId = setIds.next().toString();
				JSONObject set = allSets.getJSONObject(setId);
				JSONArray cards = set.getJSONArray("cards");
				JSONObject cardsByName = new JSONObject();
				for(int i = 0; i < cards.length(); i++) {
					JSONObject card = cards.getJSONObject(i);
					String name = CardUtil.clean(card.getString("name"));

					if(isWeirdName(name)) {
						System.err.println("Found Weird Card Name: " + name);
					}

					card.put("name", name);
					cardsByName.put(name, card);
				}
				set.put("cards", cardsByName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		recordValues();
	}

	public static JSONObject getSetCardInfo(Card card) {
		return getSetCardInfo(card, false);
	}

	public static JSONObject getSetCardInfo(Card card, String set) {
		return getSetCardInfo(card, false, set);
	}

	public static JSONObject getSetCardInfo(Card card, boolean random) {
		return getSetCardInfo(card, random, null);
	}

	public static JSONObject getSetCardInfo(Card card, boolean random, String set) {
		if(card == null || allSets == null) {
			return null;
		}
		return getSetCardInfo(card.getName(), random, set);
	}

	public static JSONObject getSetCardInfo(String cardName) {
		return getSetCardInfo(cardName, false);
	}

	public static JSONObject getSetCardInfo(String cardName, String set) {
		return getSetCardInfo(cardName, false, set);
	}

	public static JSONObject getSetCardInfo(String cardName, boolean random) {
		return getSetCardInfo(cardName, random, null);
	}

	public static JSONObject getSetCardInfo(String origCardName, boolean random, String set) {
		String cardName = toCardName(origCardName);
		if(cardName == null || allSets == null) {
			return null;
		}
		try {
			if(set != null) {
				if(allSets.has(set)) {
					if(allSets.getJSONObject(set).getJSONObject("cards").has(cardName)) {
						return allSets.getJSONObject(set).getJSONObject("cards").getJSONObject(cardName);
					}
				}
				return null;
			}

			JSONArray sets = getCardSets(cardName);
			List<Integer> indices = new ArrayList<Integer>();
			for(int i = 0; i < sets.length(); i++) {
				indices.add(i);
			}
			if(random) {
				Collections.shuffle(indices);
			} else {
				Collections.reverse(indices);
			}

			for(int i = 0; i < sets.length(); i++) {
				String s = sets.getString(indices.get(i));
				if(allSets.has(s) && (sets.length() == 1 || !bannedSets.contains(s))) {
					JSONObject card;
					if(allSets.getJSONObject(s).getJSONObject("cards").has(cardName)) {
						card = allSets.getJSONObject(s).getJSONObject("cards").getJSONObject(cardName);
					} else {
						continue;
					}
					if(card.has(MtgJsonUtil.multiverseId) || i == sets.length() - 1) {
						card.put("set", s);
						return card;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject getCards(String setId) {
		try {
			return allSets.getJSONObject(setId).getJSONObject("cards");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray getBooster(String setId) {
		try {
			return allSets.getJSONObject(setId).getJSONArray(MtgJsonUtil.booster);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSetName(String set) {
		if(allSets.has(set)) {
			try {
				JSONObject setJson = allSets.getJSONObject(set);
				if(setJson.has("name")) {
					return setJson.getString("name");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return set;
	}

	public static String getSetId(String setName) {
		Iterator keys = allSets.keys();
		try {
			while(keys.hasNext()) {
				String key = keys.next().toString();
				JSONObject setJson = allSets.getJSONObject(key);
				if(setJson.has("name") && setJson.getString("name").equals(setName)) {
					return key;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isLand(String cardName) {
		try {
		JSONArray types = getCardInfo(cardName).getJSONArray("types");
			for(int i = 0; i < types.length(); i++) {
				if(types.getString(i).equals("Land")) {
					return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static JSONObject getCardInfo(Card card) {
		if(card == null || allCards == null) {
			return null;
		}
		return getCardInfo(card.getName());
	}

	public static JSONObject getCardInfo(String origCardName) {
		String cardName = toCardName(origCardName);
		if(cardName == null || allCards == null) {
			return null;
		}
		try {
			if(allCards.has(cardName)) {
				return allCards.getJSONObject(cardName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean exists(Card card) {
		if(card == null || allCards == null) {
			return false;
		}
		return exists(card.getName());
	}

	public static boolean exists(String origCardName) {
		String cardName = toCardName(origCardName);
		if(cardName == null || allCards == null) {
			return false;
		}
		return allCards.has(cardName);
	}

	public static JSONArray getCardSets(Card card) {
		if(card == null || allCards == null) {
			return null;
		}
		return getCardSets(card.getName());
	}

	public static JSONArray getCardSets(String cardName) {
		if(cardName == null || allCards == null) {
			return null;
		}
		try {
			return getCardInfo(cardName).getJSONArray("printings");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String clean(String name) {
		if(name == null) {
			return null;
		}

		String cleanName = name
					.replace("Ã»", "u")
					.replace("âˆ’", "-")
					.replace("Ã†", "Æ")
					.replace("Æ", "Ae")
					.replace("â\u20ac\u201d", "-");

		return cleanName;
	}

	public static boolean isWeirdName(String name) {
		return !name.matches(".*[a-zA-Z_].*");
	}

	public static List<String> getCardAttributes() {
		if(cardAttributes == null) {
			CardUtil.loadAllCards();
			List<String> att = new ArrayList<String>();
			JSONObject info = getCardInfo("Ornithopter");
			Iterator it = info.keys();
			while(it.hasNext()) {
				att.add(it.next().toString());
			}
			cardAttributes = att;
		}
		return cardAttributes;
	}

	public static int getRarityOrder(String rarity) {
		if(rarity == null || rarity.isEmpty()) {
			return -1;
		}
		return rarityOrder.indexOf(rarity);
	}

	public static String toHtml(String text) {
		return ManaUtil.insertManaSymbols(text.replace("\n", "<br>"));
	}

	public static String toGathererLink(int multiverseId, String linkText) {
		return toGathererLink(String.valueOf(multiverseId), linkText);
	}

	public static String toGathererLink(String multiverseId, String linkText) {
		return "<a href=\"" + GATHERER_URL + multiverseId + "\">" + linkText + "</a>";
	}

	public static boolean isDoubleFaced(JSONObject info) throws JSONException {
		return info.has("layout") && info.get("layout").equals("double-faced");
	}

	public static boolean isSplit(JSONObject info) throws JSONException {
		return info.has("layout") && (info.get("layout").equals("split") || info.get("layout").equals("aftermath"));
	}

}
