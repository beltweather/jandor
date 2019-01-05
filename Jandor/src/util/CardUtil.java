package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import deck.Card;
import jackson.AllCardsJson;
import jackson.AllCardsJson.CardJson;
import jackson.AllSetsJson;
import jackson.AllSetsJson.SetCardJson;
import jackson.AllSetsJson.SetJson;
import jackson.JacksonUtil;

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
		bannedSets.add("PZ2");
		bannedSets.add("PAL04");
	}

	private static Set<String> cardKeysWithValues = new LinkedHashSet<String>();
	static {
		cardKeysWithValues.add("subtypes");
		cardKeysWithValues.add("supertypes");
		cardKeysWithValues.add("types");
		cardKeysWithValues.add("manaCost");
		cardKeysWithValues.add("layout");
	}

	private static Set<String> setKeysWithValues = new LinkedHashSet<String>();
	static {
		setKeysWithValues.add(MtgJsonUtil.setCode);
	}

	private static Set<String> setCardKeysWithValues = new LinkedHashSet<String>();
	static {
		setCardKeysWithValues.add("rarity");
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

	private static AllSetsJson allSetsJson = null;
	private static AllCardsJson allCardsJson = null;
	private static List<String> allCardNames = new ArrayList<String>();
	private static Map<String, String> cardNamesByLowerCase = new HashMap<String, String>();
	private static List<String> cardAttributes = null;
	private static Map<String, HashSet<String>> valuesByKey = new HashMap<String, HashSet<String>>();

	public static void init() {
		loadAllSets();
		loadAllCards();
		recordValues();
		loadImages();
	}

	private static Map<Class, Map<String, Field>> fieldsByKey = new HashMap<>();
	private static Field getField(Object obj, String fieldName) {
		Class klass = obj.getClass();
		if(!fieldsByKey.containsKey(klass)) {
			fieldsByKey.put(klass, new HashMap<String, Field>());
		}
		if(!fieldsByKey.get(klass).containsKey(fieldName)) {
			Field field = null;
			try {
				field = klass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			fieldsByKey.get(klass).put(fieldName, field);
		}
		return fieldsByKey.get(klass).get(fieldName);
	}

	private static Object getValue(Object obj, String fieldName) {
		Field field = getField(obj, fieldName);
		if(field == null) {
			return null;
		}
		try {
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void recordValues(CardJson card) {
		for(String key : cardKeysWithValues) {
			Object value = getValue(card, key);
			if(value == null) {
				continue;
			}

			if(key.equals("manaCost")) {
				List<String> mana = ManaUtil.jsonToGatherer((String) value);
				for(String m : mana) {
					recordValue(key, m);
				}
			} else {
				recordValue(key, value);
			}
		}
	}

	private static void recordValues(SetJson set) {
		for(SetCardJson card : set.cards) {
			recordValues(card);
		}
		for(String key : setKeysWithValues) {
			Object value = null;
			if(key.equals(MtgJsonUtil.setCode)) {
				value = getValue(set, "code");
			} else {
				value = getValue(set, key);
			}
			if(value == null) {
				continue;
			}
			recordValue(key, value);
		}
	}

	private static void recordValues(SetCardJson card) {
		for(String key : setCardKeysWithValues) {
			Object value = getValue(card, key);
			if(value == null) {
				continue;
			}
			recordValue(key, value);
		}
	}

	private static void recordValue(String key, Object value) {
		if(value instanceof List) {
			recordValue(key, (List) value);
		} else {
			recordValue(key, String.valueOf(value));
		}
	}

	private static void recordValue(String key, List value) {
		for(Object item : value) {
			if(item instanceof List) {
				recordValue(key, (List) item);
			} else {
				recordValue(key, String.valueOf(item));
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
		allCardsJson = JacksonUtil.readExternal(AllCardsJson.class, FileUtil.RESOURCE_CARDS_LESS_JSONS);
		allCardsJson.init(allSetsJson);
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

	public static boolean hasType(Card card, String type) {
		List<String> types = card.getTypes();
		for(String t : types) {
			if(t.equals(type)) {
				return true;
			}
		}
		return false;
	}

	private static void recordValues() {
		top: for(String name : allCardsJson.keySet()) {
			CardJson info = allCardsJson.get(name);
			cardNamesByLowerCase.put(name.toLowerCase(), name);

			if(info.types != null) {
				for(String type : info.types) {
					if(type.equals("Vanguard") || type.equals("Scheme") || type.equals("Plane")) {
						continue top;
					}
				}
			}

			if(info.printings != null) {
				if(info.printings.size() == 1) {
					for(String set : info.printings) {
						if(set.equals("UNH") || set.equals("UGL") || set.equals("UST")) {
							continue top;
						}
					}
				}
			}

			allCardNames.add(name);
			recordValues(info);
		}

		for(SetJson set : allSetsJson.values()) {
			recordValues(set);
		}

	}

	private static void loadAllSets() {
		allSetsJson = JacksonUtil.readExternal(AllSetsJson.class, FileUtil.RESOURCE_SETS_LESS_JSONS);
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

	public static SetCardJson getSetCardInfo(Card card) {
		return getSetCardInfo(card, false);
	}

	public static SetCardJson getSetCardInfo(Card card, String set) {
		return getSetCardInfo(card, false, set);
	}

	public static SetCardJson getSetCardInfo(Card card, boolean random) {
		return getSetCardInfo(card, random, null);
	}

	public static SetCardJson getSetCardInfo(Card card, boolean random, String set) {
		if(card == null || allSetsJson == null) {
			return null;
		}
		return getSetCardInfo(card.getName(), random, set);
	}

	public static SetCardJson getSetCardInfo(String cardName) {
		return getSetCardInfo(cardName, false);
	}

	public static SetCardJson getSetCardInfo(String cardName, String set) {
		return getSetCardInfo(cardName, false, set);
	}

	public static SetCardJson getSetCardInfo(String cardName, boolean random) {
		return getSetCardInfo(cardName, random, null);
	}

	public static SetCardJson getSetCardInfo(String origCardName, boolean random, String set) {
		String cardName = toCardName(origCardName);
		if(cardName == null || allSetsJson == null) {
			return null;
		}

		if(set != null) {
			if(allSetsJson.containsKey(set)) {
				if(allSetsJson.get(set).cardsByName.containsKey(cardName)) {
					return allSetsJson.get(set).cardsByName.get(cardName);
				}
			}
			return null;
		}

		List<String> sets = getCardSets(cardName);
		List<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < sets.size(); i++) {
			indices.add(i);
		}
		if(random) {
			Collections.shuffle(indices);
		} else {
			Collections.reverse(indices);
		}

		boolean onlyBanned = true;
		for(String s : sets) {
			if(!bannedSets.contains(s)) {
				onlyBanned = false;
				break;
			}
		}

		SetCardJson bestGuess = null;
		for(int i = 0; i < sets.size(); i++) {
			String s = sets.get(indices.get(i));
			if(allSetsJson.containsKey(s) && (onlyBanned || sets.size() == 1 || !bannedSets.contains(s))) {
				SetCardJson card;
				if(allSetsJson.get(s).cardsByName.containsKey(cardName)) {
					card = allSetsJson.get(s).cardsByName.get(cardName);
				} else {
					continue;
				}
				if(card.multiverseId != 0 || i == sets.size() - 1 || onlyBanned) {
					card.set = s;
					return card;
				} else {
					card.set = s;
					bestGuess = card;
				}
			}
		}

		if(bestGuess != null) {
			return bestGuess;
		}

		return null;
	}

	public static List<SetCardJson> getCards(String setId) {
		return allSetsJson.get(setId).cards;
	}

	public static List<Object> getBooster(String setId) {
		return allSetsJson.get(setId).boosterV3;
	}

	public static String getSetName(String set) {
		if(allSetsJson.containsKey(set)) {
			return allSetsJson.get(set).name;
		}
		return set;
	}

	public static String getSetId(String setName) {
		for(SetJson set : allSetsJson.values()) {
			if(set.name != null && set.name.equals(setName)) {
				return set.code;
			}
		}
		return null;
	}

	public static boolean isLand(String cardName) {
		List<String> types = getCardInfo(cardName).types;
		for(String type : types) {
			if(type.equals("Land")) {
				return true;
			}
		}
		return false;
	}

	public static CardJson getCardInfo(Card card) {
		if(card == null || allCardsJson == null) {
			return null;
		}
		return getCardInfo(card.getName());
	}

	public static CardJson getCardInfo(String origCardName) {
		String cardName = toCardName(origCardName);
		if(cardName == null || allCardsJson == null || !allCardsJson.containsKey(cardName)) {
			return null;
		}
		return allCardsJson.get(cardName);
	}

	public static boolean exists(Card card) {
		if(card == null || allCardsJson == null) {
			return false;
		}
		return exists(card.getName());
	}

	public static boolean exists(String origCardName) {
		String cardName = toCardName(origCardName);
		return cardName != null && allCardsJson != null && allCardsJson.containsKey(cardName);
	}

	public static List<String> getCardSets(String cardName) {
		CardJson card = getCardInfo(cardName);
		if(card == null) {
			return null;
		}
		return card.printings;
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
			for(Field field : CardJson.class.getDeclaredFields()) {
				att.add(field.getName());
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

	public static boolean isDoubleFaced(CardJson info) {
		return info.layout != null && info.layout.equals("double-faced");
	}

	public static boolean isSplit(CardJson info) {
		return info.layout != null && (info.layout.equals("split") || info.layout.equals("aftermath"));
	}

}
