package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import json.JSONException;
import deck.Card;
import deck.Deck;

public class ApprenticeUtil {

	public static final List<String> types = new ArrayList<String>();
	static {
		types.add("Creature");
		types.add("Instant");
		types.add("Sorcery");
		types.add("Enchantment");
		types.add("Artifact");
		types.add("Planeswalker");
		types.add("Land");
	}
	
	private ApprenticeUtil() {}
	
	public static Deck toDeck(String filename) {
		String deckName = new File(filename).getName().replace(".dec", "");
		return toDeck(deckName, FileUtil.getReader(filename));
	}
	
	public static Deck toDeck(String deckName, BufferedReader deckReader) {
		if(deckReader == null) {
			return null;
		}
		Deck deck = new Deck();
		Deck sideboard = new Deck();
		
		Deck targetDeck;
		String line;
		try {
			line = deckReader.readLine();
			while(line != null) {
				
				// Get name
				if(deck.getName() == null && line.contains("NAME:")) {
					line = line.replace("//", "").replace("NAME:", "").trim();
					deck.setName(line);
					sideboard.setName(line + " (Sideboard)");
				
				// Get card
				} else if(!line.startsWith("//") && !line.contains("<") && line.length() > 0) {
					line = line.trim();
					
					if(line.startsWith("SB:")) {
						line = line.replace("SB:", "").trim();
						targetDeck = sideboard;
					} else {
						targetDeck = deck;
					}
					
					int numberOfCopies = 0;
					
					try {
						numberOfCopies = Integer.valueOf(line.split(" ")[0]);
					} catch(NumberFormatException e) {
						line = deckReader.readLine();
						continue;
					}
					
					String name = CardUtil.toCardName(CardUtil.clean(line.substring(("" + numberOfCopies).length() + 1)), true);
					targetDeck.add(new Card(name), numberOfCopies);
					
					if(new Card(name).getCardInfo() == null) {
						System.out.println("No info for card: " + name);
					}
				}
				
				line = deckReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				deckReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		deck.setSideboard(sideboard);
		
		if(deck.getName() == null) {
			deck.setName(deckName);
			if(deck.getSideboard() != null) {
				deck.getSideboard().setName(deckName + " Sideboard");
			}
		}
		
		return deck;
	}
	
	public static void toFile(BufferedWriter deckWriter, Deck deck) {
		if(deck == null) {
			return;
		}
		
		Map<Card, Integer> cards = deck.getCountsByCard();
		try {
			Set<Card> usedCards = new HashSet<Card>();
			for(String type : types) {
				deckWriter.write("// " + type + "s\n");
				for(Card card : cards.keySet()) {
					if(!CardUtil.hasType(card, type) || usedCards.contains(card) || (CardUtil.hasType(card, "Land") && !type.equals("Land"))) {
						continue;
					}
					usedCards.add(card);
					int count = cards.get(card);
					deckWriter.write("\t\t" + count + " " + card.getName() + "\n");
				}
			}
		
			if(deck.hasSideboard()) {
				deckWriter.write("// Sideboard\n");
				cards = deck.getSideboard().getCountsByCard();
				for(Card card : cards.keySet()) {
					int count = cards.get(card);
					deckWriter.write("SB:\t" + count + " " + card.getName() + "\n");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
