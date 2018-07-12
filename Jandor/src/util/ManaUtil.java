package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.JSONArray;
import json.JSONException;
import run.Jandor;
import ui.pwidget.ColorUtil;
import deck.Card;

public class ManaUtil {

	public static final String SIZE_SMALL = "small";
	public static final String SIZE_MEDIUM = "medium";
	public static final String SIZE_LARGE = "large";
	
	public static final String WHITE = "W";
	public static final String BLUE = "U";
	public static final String BLACK = "B";
	public static final String RED = "R";
	public static final String GREEN = "G";
	public static final String COLORLESS = "C";
	public static final String X = "X";
	
	private static final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	private static final Pattern patternMana = Pattern.compile("(.*\\{t\\}: add )(.*?)( to your mana pool.*)");
	private static final Pattern patternPayMana = Pattern.compile("(.*)(\\{.*\\})(, \\{t\\}: add )(.*?)( to your mana pool.*)");
		
	private static final List<String> colorOrder = new ArrayList<String>();
	static {
		colorOrder.add("W");
		colorOrder.add("U");
		colorOrder.add("B");
		colorOrder.add("R");
		colorOrder.add("G");
		colorOrder.add("C");
	}
	
	private ManaUtil() {}
	
	public static String getSmallHtml(String gathererString) {
		return getHtml(SIZE_SMALL, gathererString);
	}
	
	public static String getMediumHtml(String gathererString) {
		return getHtml(SIZE_SMALL, gathererString);
	}
	
	public static String getLargeHtml(String gathererString) {
		return getHtml(SIZE_SMALL, gathererString);
	}
	
	public static String getHtml(String size, String gathererString) {
		return "<img src=\"" + ImageUtil.getSymbolUrl(gathererString, size).toString() + "\"/>";
	}
	
	public static String getSmallUrl(String gathererString) {
		return getUrl(SIZE_SMALL, gathererString);
	}
	
	public static String getMediumUrl(String gathererString) {
		return getUrl(SIZE_SMALL, gathererString);
	}
	
	public static String getLargeUrl(String gathererString) {
		return getUrl(SIZE_SMALL, gathererString);
	}
	
	public static String getUrl(String size, String gathererString) {
		return ImageUtil.getSymbolUrl(gathererString, size).toString();
	}
	
	public static List<String> jsonToGatherer(String jsonString) {
		if(jsonString == null) {
			return null;
		}
		
		String[] toks = jsonString.replace("{", "-").replace("}", "-").replace("/", "").split("--");
		List<String> mana = new ArrayList<String>();
		for(String tok : toks) {
			tok = tok.replace("-", "");
			mana.add(tok);
		}

		return mana;
	}
	
	public static JSONArray manaToColors(String manaString) {
		JSONArray array = new JSONArray();
		if(hasWhite(manaString)) {
			array.put("White");
		}
		if(hasBlue(manaString)) {
			array.put("Blue");
		}
		if(hasBlack(manaString)) {
			array.put("Black");
		}
		if(hasRed(manaString)) {
			array.put("Red");
		}
		if(hasGreen(manaString)) {
			array.put("Green");
		}
		return array;
	}
	
	public static String toManaHtml(List<String> manaCost) {
		StringBuilder sb = new StringBuilder();
		for(String mana : manaCost) {
			sb.append("<span>" + ManaUtil.getSmallHtml(mana) + "</span>");
		}
		return sb.toString();
	}
	
	public static String insertManaSymbols(String text) {
		String newText = text;
		Matcher matcher = pattern.matcher(text);
		while(matcher.find()) {
			String manaStr = matcher.group(1);
			String cleanManaStr = jsonToGatherer(manaStr).get(0);
			if(manaStr.equals("T")) {
				cleanManaStr = "tap";
			} else if(manaStr.equals("Q")) {
				cleanManaStr = "untap";
			}
			newText = newText.replace("{" + manaStr + "}", getSmallHtml(cleanManaStr));
		}
		return newText;
	}
	
	public static boolean hasWhite(String mana) {
		return hasManaSymbol(mana, "W") || hasManaSymbol(mana, "White");
	}
	
	public static boolean hasBlue(String mana) {
		return hasManaSymbol(mana, "U") || hasManaSymbol(mana, "Blue");
	}
	
	public static boolean hasBlack(String mana) {
		return (hasManaSymbol(mana, "B") && !hasManaSymbol(mana, "Blue")) || hasManaSymbol(mana, "Black");
	}
	
	public static boolean hasRed(String mana) {
		return hasManaSymbol(mana, "R") || hasManaSymbol(mana, "Red");
	}
	
	public static boolean hasGreen(String mana) {
		return hasManaSymbol(mana, "G") || hasManaSymbol(mana, "Green");
	}
	
	public static boolean hasManaSymbol(String manaJson, String symbol) {
		return manaJson.contains(symbol);
	}
	
	public static boolean isColorless(String mana) {
		return !hasWhite(mana) && !hasBlue(mana) && !hasBlack(mana) && !hasRed(mana) && !hasGreen(mana);
	}
	
	public static Color getColor(Card card) {
		if(card == null || card.getCardInfo() == null || !card.getCardInfo().has("colors")) {
			return ColorUtil.MANA_NONE;
		}
		try {
			return getColor(card.getCardInfo().getJSONArray("colors").toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ColorUtil.MANA_NONE;
	}

	public static Color getColor(String colors) {
		if(colors == null) {
			return ColorUtil.MANA_NONE;
		}
		if(colors.contains(",")) {
			return ColorUtil.MANA_MULTI;
		}
		if(hasWhite(colors)) {
			return ColorUtil.MANA_WHITE;
		}
		if(hasBlue(colors)) {
			return ColorUtil.MANA_BLUE;
		}
		if(hasBlack(colors)) {
			return ColorUtil.MANA_BLACK;
		}
		if(hasRed(colors)) {
			return ColorUtil.MANA_RED;
		}
		if(hasGreen(colors)) {
			return ColorUtil.MANA_GREEN;
		}
		return ColorUtil.MANA_NONE;
	}
	
	public static String getColorName(Card card) {
		return getColorName(getColor(card));
	}
	
	public static String getColorName(Color manaColor) {
		if(manaColor.equals(ColorUtil.MANA_MULTI)) {
			return "gold";
		}
		if(manaColor.equals(ColorUtil.MANA_WHITE)) {
			return "white";
		}
		if(manaColor.equals(ColorUtil.MANA_BLUE)) {
			return "blue";
		}
		if(manaColor.equals(ColorUtil.MANA_BLACK)) {
			return "black";
		}
		if(manaColor.equals(ColorUtil.MANA_RED)) {
			return "red";
		}
		if(manaColor.equals(ColorUtil.MANA_GREEN)) {
			return "green";
		}
		return "gray";
	}
	
	public static int indexOfColorCharacter(String colorCharacter) {
		if(colorOrder.contains(colorCharacter)) {
			return colorOrder.indexOf(colorCharacter);
		}
		return Integer.MAX_VALUE;
	}
	
	public static List<String> getColorLetters() {
		return new ArrayList<String>(colorOrder);
	}
	
	public static int compare(String colorStringA, String colorStringB) {
		String numA = "";
		String numB = "";
		for(int i = 0; i < colorStringA.length(); i++) {
			numA += indexOfColorCharacter(colorStringA.charAt(i) + "");
		}
		for(int i = 0; i < colorStringB.length(); i++) {
			numB += indexOfColorCharacter(colorStringB.charAt(i) + "");
		}
		if(numA.length() < numB.length()) {
			return -1;
		}
		if(numA.length() > numB.length()) {
			return 1;
		}
		return numA.compareTo(numB);
	}
	
	public static boolean isManaCard(Card card) {
		String text = card.getText();
		if(text == null || text.isEmpty()) {
			return false;
		}
		Matcher m = patternMana.matcher(text.toLowerCase());
		return m.find();
	}
	
	public static boolean isPayForManaCard(Card card) {
		String text = card.getText();
		if(text == null || text.isEmpty()) {
			return false;
		}
		Matcher m = patternPayMana.matcher(text.toLowerCase());
		return m.find();
	}
	
	public static String getManaText(Card card) {
		String text = card.getText();
		Matcher m = patternMana.matcher(text.toLowerCase());
		String group = null;
		try {
			while(m.find()) {
				if(group == null) {
					group = m.group(2);
				} else {
					group += ";" + m.group(2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return group;
		}
	}
	
	public static String getPayForManaText(Card card) {
		String text = card.getText();
		Matcher m = patternMana.matcher(text.toLowerCase());
		String group = null;
		try {
			while(m.find()) {
				String payText = m.group(0);
				Matcher mp = patternPayMana.matcher(payText);
				boolean found = false;
				while(mp.find()) {
					if(group == null) {
						group = mp.group(2);
					} else {
						group += ";" + mp.group(2);
					}
					found = true;
				}
				if(!found) {
					if(group == null) {
						group = "_";
					} else {
						group += ";_";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return group;
		}
	}
	
	public static void main(String[] args) {
		Jandor.init();
		System.out.println("start.");
		List<String> mana = new ArrayList<String>();
		for(String name : CardUtil.getAllCardNames()) {
			Card card = new Card(name);
			if(isManaCard(card)) {
				String text = getManaText(card);
				if(isPayForManaCard(card)) {
					String payText = getPayForManaText(card);
					text += "|" + payText;
					if(!mana.contains(text)) {
						mana.add(card.getText() + "|" + text);
					}
				}
			}
		}
		Collections.sort(mana, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
			
		});
		for(String m : mana) {
			String[] toks = m.split("\\|");
			String text = toks[0];
			String[] cost = toks[1].split(";");
			String[] pay = toks[2].split(";");
			
			String s = text + " ==> ";
			for(int i = 0; i < pay.length; i++) {
				String p = pay[i];
				if(i != 0) {
					s += "; ";
				}
				if(p.isEmpty()) {
					s += cost[i];
				} else {
					s += "pay " + p + " -> " + cost[i];
				}
			}
			System.out.println(s + "\n");
		}
		System.out.println("end.");
	}
}
