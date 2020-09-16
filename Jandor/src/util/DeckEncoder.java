package util;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JTextArea;

import deck.Card;
import deck.Deck;
import jackson.AllCardsJson.CardJson;
import run.Jandor;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;

public class DeckEncoder {

	private static final int DIGIT_COUNT = 2;
	private static List<String> CHAR_SET;

	private static List<Integer> UNIQUE_MULTIVERSE_IDS;
	private static Map<Integer, Integer> INDICES_BY_MULTIVERSE_ID;

	// Uncomment this to use hard-coded character set
	static {
		String CHARS = "!\"#$%&'()*+,./:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
		//String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzªµºÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ`~!@#$%^&*()_-+={}[]\\|:;\"'?/>.<,";
		//String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzªµºÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ";
		//String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		List<String> chars = new ArrayList<>();
		for(int i = 0; i < CHARS.length(); i++) {
			chars.add("" + CHARS.charAt(i));
		}
		CHAR_SET = chars;
	}

	// Uncomment this to derive the character set
	/*static {
		List<String> chars = new ArrayList<>();
		for(int i = 0; i < 256 && chars.size() < 999; i++) {
			String s = toASCII(i);
			if(isAlphabetic(s) && !isUndesirable(s)) {
				chars.add(s);
			}
		}
		CHAR_SET = chars;
	}*/

	private DeckEncoder() {}

	private static void initUniqueMultiverseIds() {
		List<Integer> uniqueMultiverseIds = new ArrayList<>();
		Map<Integer, Integer> indicesByMultiverseId = new HashMap<>();
		Map<Integer, List<Integer>> multiverseIdsByUniqueMultiverseId = new HashMap<>();
		for(String name : CardUtil.getAllCardNames()) {
			List<Integer> multiverseIds = new ArrayList<>();
			CardJson info = CardUtil.getCardInfo(name);
			for(List<Integer> ids : info.multiverseIdsBySetCode.values()) {
				multiverseIds.addAll(ids);
			}
			multiverseIds.sort((a, b) -> a - b);
			if(multiverseIds.size() > 0) {
				uniqueMultiverseIds.add(multiverseIds.get(0));
				multiverseIdsByUniqueMultiverseId.put(multiverseIds.get(0), multiverseIds);
			}
		}
		uniqueMultiverseIds.sort((a, b) -> a - b);
		UNIQUE_MULTIVERSE_IDS = uniqueMultiverseIds;

		for(int i = 0 ; i < uniqueMultiverseIds.size(); i++) {
			int uniqueMultiverseId = uniqueMultiverseIds.get(i);
			for(int mId : multiverseIdsByUniqueMultiverseId.get(uniqueMultiverseId)) {
				indicesByMultiverseId.put(mId, i);
			}
		}
		INDICES_BY_MULTIVERSE_ID = indicesByMultiverseId;
	}

	public static int getIndexFromMultiverseId(int multiverseId) {
		if(INDICES_BY_MULTIVERSE_ID == null) {
			initUniqueMultiverseIds();
		}
		return INDICES_BY_MULTIVERSE_ID.get(multiverseId);
	}

	public static int getMultiverseIdFromIndex(int index) {
		if(INDICES_BY_MULTIVERSE_ID == null) {
			initUniqueMultiverseIds();
		}
		return UNIQUE_MULTIVERSE_IDS.get(index);
	}

	public static String toASCII(int n) {
		return Character.toString((char) n);
	}

	public static List<Integer> toDigitsInBase(int n, int base) {
		List<Integer> digits = new ArrayList<>();

		int q = n;
		int r = 0;
		while(q > 0) {
			r = q % base;
			digits.add(0, r);
			q = q / base;
		}

		return digits;
	}

	public static boolean isNumeric(String s) {
		switch(s) {
			case "0":
			case "1":
			case "2":
			case "3":
			case "4":
			case "5":
			case "6":
			case "7":
			case "8":
			case "9":
				return true;
			default:
				return false;
		}
	}

	public static boolean isAlphabetic(String s) {
		return Character.isAlphabetic(s.charAt(0));
	}

	public static boolean isUndesirable(String s) {
		switch(s) {
			case "\n":
			case "\r":
				return true;
			default:
				return false;
		}
	}

	public static String fromDigitToCharSet(int n) {
		if(n < 0) {
			System.err.println("Trying to convert " + n + " to our char set. Using 0 instead");
			n = 0;
		}
		if(n >= CHAR_SET.size()) {
			System.err.println("Trying to convert " + n + " to our char set. Using " + (CHAR_SET.size()-1) + " instead");
			n = CHAR_SET.size() - 1;
		}
		return CHAR_SET.get(n);
	}

	public static String getCharSetZero() {
		return CHAR_SET.get(0);
	}

	public static Integer fromCharSetToDigit(String c) {
		return CHAR_SET.indexOf("" + c);
	}

	public static Integer fromCharSet(String chars) {
		List<Integer> digits = new ArrayList<>();
		for(int i = 0; i < chars.length(); i++) {
			digits.add(fromCharSetToDigit("" + chars.charAt(i)));
		}

		int n = 0;
		for(int i = digits.size() - 1; i >= 0; i--) {
			n += digits.get(i) * Math.pow(CHAR_SET.size(), digits.size() - 1 - i);
		}
		return n;
	}

	public static String toCharSetString(int n) {
		return toCharSetString(toDigitsInBase(n, CHAR_SET.size()));
	}

	public static String toCharSetString(List<Integer> charSetDigits) {
		StringBuilder sb = new StringBuilder();
		for(Integer n : charSetDigits) {
			sb.append(fromDigitToCharSet(n));
		}
		String charSetStr = sb.toString();
		while(charSetStr.length() < DIGIT_COUNT) {
			charSetStr = getCharSetZero() + charSetStr;
		}
		return charSetStr;
	}

	public static String toSquare(String s) {
		return toRect(s, 1.0/2.4);
	}

	public static String toRect(String s, double ratio) {
		int n = s.length();
		int x = (int) Math.round(n / Math.sqrt(n*ratio));
		return DeckEncoder.splitLines(s, x);
	}

	public static String splitLines(String s, int lineLength) {
		StringBuilder sb = new StringBuilder();
		int n = 0;
		for(int i = 0; i < s.length(); i++) {
			sb.append(s.charAt(i));
			n++;
			if(n == lineLength) {
				sb.append("\n");
				n = 0;
			}
		}
		return sb.toString();
	}

	public static String encode(Deck deck) {
		return encode(deck, 4);
	}

	private static List<Integer> getSortedCounts(Collection<Integer> counts, int defaultCount) {
		List<Integer> sortedCounts = new ArrayList<>(counts);
		sortedCounts.sort((a,b) -> a == defaultCount ? -1 : (b == defaultCount ? 1 : a - b));
		return sortedCounts;
	}

	private static String encode(Deck deck, int defaultCount) {
		Map<Integer, List<Card>> cardsByCount = deck.getCardsByCount();
		StringBuilder sb = new StringBuilder();

		for(int count : getSortedCounts(cardsByCount.keySet(), defaultCount)) {
			if(count != defaultCount) {
				sb.append(count);
			}
			for(Card card : cardsByCount.get(count)) {
				sb.append(toCharSetString(getIndexFromMultiverseId(card.getMultiverseId())));
			}
		}
		String e = sb.toString();
		if(!deck.hasSideboard()) {
			return e;
		}
		return e + "-" + encode(deck.getSideboard(), 1);
	}

	private static void add(Deck deck, String count, String code) {
		String name = CardUtil.getCardName(getMultiverseIdFromIndex(fromCharSet(code)));
		if(name != null) {
			deck.add(new Card(name), Integer.valueOf(count));
		}
	}


	public static Deck decode(String encodedDeck) {
		try {
			return decode(encodedDeck, 4);
		} catch(Exception e) {
			return null;
		}
	}

	private static Deck decode(String encodedDeck, int defaultCount) {
		String[] toks = encodedDeck.replace("\n", "").split("-");
		String deckStr = toks[0];

		// Assume if we don't start with a number that the number is default.
		if(!isNumeric("" + deckStr.charAt(0))) {
			deckStr = defaultCount + deckStr;
		}

		Deck deck = new Deck();

		boolean parsingNum = true;
		String num = "";
		String code = "";
		for(int i = 0; i < deckStr.length(); i++) {
			String c = "" + deckStr.charAt(i);

			if(!parsingNum && isNumeric(c)) {
				parsingNum = true;
				num = c;
			} else if(parsingNum && isNumeric(c)) {
				num += c;
			} else if(parsingNum && !isNumeric(c)) {
				parsingNum = false;
				code += c;
			} else if(!parsingNum) {
				code += c;
			}

			if(code.length() == DIGIT_COUNT) {
				add(deck, num, code);
				code = "";
			}
		}

		String sideStr = toks.length > 1 ? toks[1] : null;
		if(sideStr != null) {
			deck.setSideboard(decode(sideStr, 1));
		} else {
			deck.setSideboard(new Deck());
		}

		return deck;
	}

	private static void copyToClipboard(String s) {
		StringSelection stringSelection = new StringSelection(s);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private static String readFromClipboard() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void showEncodeDialog(Component parent, Deck deck) {
		String encoded = DeckEncoder.encode(deck);
		copyToClipboard(encoded);

		PPanel p = new PPanel();
		JTextArea area = new JTextArea(5, 30);
		area.setLineWrap(true);
		area.setMargin(new Insets(10,10,10,10));
		area.setText(encoded);
		area.setEditable(false);

		PButton copyButton = new PButton("Copy to Clipboard");
		PCheckBox sbCheck = new PCheckBox("Include Sideboard", true);
		sbCheck.addActionListener((e) -> {
			String code;
			if(sbCheck.isSelected()) {
				code = DeckEncoder.encode(deck);
			} else {
				Deck copy = deck.copyRenderable();
				copy.setSideboard(null);
				code = DeckEncoder.encode(copy);
			}
			copyToClipboard(code);
			area.setText(code);
			area.selectAll();
		});

		p.addc(new PScrollPane(area));
		p.c.insets(10);
		p.down();
		p.addc(sbCheck);
		p.down();
		p.addc(copyButton);
		p.down();
		p.fill();

		p.revalidate();

		JDialog dialog = JUtil.buildBlankDialog(parent, (deck.getName() == null ? "Untitled" : deck.getName()) + " Deck Code", p);
		dialog.setSize(400, 150 + area.getHeight());

		copyButton.addActionListener((e) -> {
			copyToClipboard(area.getText());
			dialog.setVisible(false);
		});

		area.selectAll();
		dialog.setVisible(true);
	}

	public static interface OnAddDeck {

		public boolean onAdd(Deck deck);

	}

	public static void showDecodeDialog(Component parent, OnAddDeck onAddDeck) {
		String encoded = readFromClipboard();

		PPanel p = new PPanel();
		JTextArea area = new JTextArea(5, 30);
		area.setLineWrap(true);
		area.setMargin(new Insets(10,10,10,10));
		area.setText(encoded);

		PButton addButton = new PButton("Create Deck");
		PButton cancelButton = new PButton("Cancel");
		p.addc(new PScrollPane(area));
		p.c.insets(10);
		p.down();

		PPanel bp = new PPanel();
		bp.addc(addButton);
		bp.c.insets(0, 10);
		bp.right();
		bp.addc(cancelButton);

		p.addc(bp);
		p.left();
		p.down();
		p.fill();

		p.revalidate();

		JDialog dialog = JUtil.buildBlankDialog(parent, "Add Deck by Code", p);
		dialog.setSize(400, 100 + area.getHeight());

		addButton.addActionListener((e) -> {
			Deck deck = DeckEncoder.decode(area.getText());
			if(onAddDeck.onAdd(deck)) {
				dialog.setVisible(false);
			}
		});

		cancelButton.addActionListener((e) -> {
			dialog.setVisible(false);
		});

		area.selectAll();
		dialog.setVisible(true);
	}

	public static void main(String[] arg) {

		Jandor.init();
		System.out.println("Card Names: " + CardUtil.getAllCardNames().size());

		List<Integer> originalMultiverseIds = new ArrayList<>();
		for(String name : CardUtil.getAllCardNames()) {
			List<Integer> multiverseIds = new ArrayList<>();
			CardJson info = CardUtil.getCardInfo(name);
			for(List<Integer> ids : info.multiverseIdsBySetCode.values()) {
				multiverseIds.addAll(ids);
			}
			multiverseIds.sort((a, b) -> a - b);
			if(multiverseIds.size() > 0) {
				originalMultiverseIds.add(multiverseIds.get(0));
			} else {
				int j = 0;
			}
		}
		originalMultiverseIds.sort((a, b) -> a - b);

		for(int i = 0; i < originalMultiverseIds.size(); i++) {
			System.out.println(i + ": " + originalMultiverseIds.get(i));
		}

		/*System.out.println("There are " + CHAR_SET.size() + " unique characters");

		StringBuilder cs = new StringBuilder();
		for(String s : CHAR_SET) {
			cs.append(s);
		}
		System.out.println("Char Set: " + cs);

		int c = 4;
		int n = 987654;
		//System.out.println(c + "-" + n + ": " + c + toCharSetString(n));

		List<String> cards = new ArrayList<String>();
		cards.add("4-34531");
		cards.add("4-345382");
		cards.add("4-349383");
		cards.add("4-245320");
		cards.add("4-145387");
		cards.add("4-145387");
		cards.add("4-345327");
		cards.add("12-45387");
		cards.add("10-343387");
		cards.add("4-745347");
		cards.add("4-345387");
		cards.add("4-345327");
		cards.add("4-45387");
		cards.add("4-343387");
		cards.add("4-745347");
		cards.add("4-345387");

		StringBuilder essbee = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		for(String card : cards) {
			String[] toks = card.split("-");
			sb.append(toks[0] + toCharSetString(Integer.valueOf(toks[1])));
			essbee.append(toks[0] + toks[1]);
		}
		System.out.println(sb.toString());
		System.out.println(essbee.toString());*/

		/*int maxLength = 0;
		for(int i = 0; i < 1000000; i++) {
			String s = toCharSetString(i);
			if(s.length() > maxLength) {
				maxLength = s.length();
			}
			//System.out.println(i + ": " + s);
		}*/
		//System.out.println("Max length: " + maxLength);

		/*StringBuilder sb = new StringBuilder();
		for(int i = 33; i < 256; i++) {
			if(i == 45 || i == 127 || (i >= 48 && i <= 57)) {
				continue;
			}
			sb.append(toASCII(i));
		}
		System.out.println(sb);*/


	}

	// Each multiverseID gets 3 characters exactly
	// If no number precedes them, 4 copies is assumed.


}
