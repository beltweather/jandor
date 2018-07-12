package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import canvas.IRenderable;
import deck.Card;
import deck.CardList;
import deck.RenderableList;

public class ShuffleUtil {
	
	private ShuffleUtil() {}
	
	private static Random r = new Random();
	
	public static int randInt(int max) {
		return randInt(0, max);
	}
	
	public static int randInt(int min, int max) {
		return r.nextInt((max - min) + 1) + min;
	}
	
	public static boolean randBoolean() {
		return r.nextBoolean();
	}
	
	public static void shuffle(ShuffleType shuffleType, CardList deck) {
		if(shuffleType == null || deck == null) {
			return;
		}
		switch(shuffleType) {
			case PLAYER:
				playerShuffle(deck);
				break;
			case RANDOM:
				randomShuffle(deck);
				break;
			case AZ:
				sortAZ(deck);
				break;
			case ZA:
				sortZA(deck);
				break;
			case MANA_LH:
				sortManaLH(deck);
				break;
			case MANA_HL:
				sortManaHL(deck);
				break;
			case RARITY_LH:
				sortRarityLH(deck);
				break;
			case RARITY_HL:
				sortRarityHL(deck);
				break;
		}
 	}
	
	private static void randomShuffle(CardList deck) {
		if(deck == null) {
			return;
		}
		Collections.shuffle(deck);
	}
	
	private static void playerShuffle(CardList deck) { // XXX Needs implementation
		if(deck == null) {
			return;
		}
		
		List<Card> nonLands = new ArrayList<Card>();
		List<Card> lands = new ArrayList<Card>();
		for(Card card : deck) {
			if(card.isLand()) {
				lands.add(card);
			} else {
				nonLands.add(card);
			}
		}
		Collections.shuffle(nonLands);
		Collections.shuffle(lands);
		
		if(lands.size() == 0) {
			randomShuffle(deck);
			return;
		}
		
		int landRatio = (int) Math.round(nonLands.size() / (double) lands.size());
		
		if(landRatio == 0) {
			randomShuffle(deck);
			return;
		}
		
		// Insert lands evenly
		int j = 0;
		List<Card> cards = new ArrayList<Card>();
		for(int i = 0; i < nonLands.size(); i++) {
			if(i % landRatio == 0 && j < lands.size()) {
				cards.add(lands.get(j++));
			} 
			cards.add(nonLands.get(i));
		}
		
		// Insert extra lands randomly
		while(j < lands.size()) {
			Card land = lands.get(j++);
			int index = randIndex(cards);
			cards.add(index, land);
		}
		
		// Shuffle random chunks
		int numShuffles = 3;
		int minChunkSize = 6;
		int maxChunkSize = 10;
		for(int i = 0; i < numShuffles; i++) {
			int index = randIndex(cards);
			int chunkSize = randChunk(cards, index, minChunkSize, maxChunkSize);
			List<Card> chunk = new ArrayList<Card>(cards.subList(index, index + chunkSize));
			for(int k = 0; k < chunkSize; k++) {
				cards.remove(index);
			}
			int newIndex = randIndex(cards);
			for(int k = chunk.size() - 1; k >= 0; k--) {
				cards.add(newIndex, chunk.get(k));
			}
		}
		
		deck.set(cards);
	}
	
	private static int randIndex(List cards) {
		if(cards.size() < 2) {
			return 0;
		}
		return randInt(0, cards.size() - 1);
	}
	
	private static int randChunk(List cards, int index, int minSize, int maxSize) {
		int size = (Math.abs(r.nextInt()) + minSize) % (maxSize + 1);
		if(index + size > cards.size()) {
			size = cards.size() - index;
		}
		return size;
	}
	
	private static void sortAZ(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
	}
	
	private static void sortZA(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				return o2.getName().compareTo(o1.getName());
			}
			
		});
	}
	
	private static void sortManaLH(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				int compare = o1.getConvertedManaCost() - o2.getConvertedManaCost();
				if(compare == 0) {
					return o1.getName().compareTo(o2.getName());
				}
				return compare;
			}
			
		});
	}

	private static void sortManaHL(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				int compare = o2.getConvertedManaCost() - o1.getConvertedManaCost();
				if(compare == 0) {
					return o2.getName().compareTo(o1.getName());
				}
				return compare;
			}
			
		});
	}
	
	private static void sortRarityLH(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				int compare = CardUtil.getRarityOrder(o1.getRarity()) - CardUtil.getRarityOrder(o2.getRarity());
				if(compare == 0) {
					return o1.getName().compareTo(o2.getName());
				}
				return compare;
			}
			
		});
	}

	private static void sortRarityHL(RenderableList deck) {
		if(deck == null) {
			return;
		}
		Collections.sort(deck, new Comparator<Card>() {

			@Override
			public int compare(Card o1, Card o2) {
				int compare = CardUtil.getRarityOrder(o2.getRarity()) - CardUtil.getRarityOrder(o1.getRarity());
				if(compare == 0) {
					return o2.getName().compareTo(o1.getName());
				}
				return compare;
			}
			
		});
	}
	
	public static <T extends IRenderable> void positionSort(List<T> cards) {
		Collections.sort(cards, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				if(o1.getRenderer().getScreenX() == o2.getRenderer().getScreenX()) {
					return o1.getRenderer().getScreenY() - o2.getRenderer().getScreenY();
				}
				return o1.getRenderer().getScreenX() - o2.getRenderer().getScreenX();
			}
			
		});
	}
	
	public static <T extends IRenderable> void positionShuffle(List<T> cards) {
		/*List<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < cards.size(); i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);
		for(int i = 0; i < cards.size(); i++) {
			int j = indices.get(i);
			Card a = cards.get(i);
			Card b = cards.get(j);
			int tempX = a.getScreenX();
			int tempY = a.getScreenY();
			a.setScreenX(b.getScreenX());
			a.setScreenY(b.getScreenY());
			b.setScreenX(tempX);
			b.setScreenY(tempY);
		}*/
		
		for (int i = cards.size(); i > 1; i--) {
			int idxA = i-1;
			int idxB = r.nextInt(i);
			T a = cards.get(idxA);
			T b = cards.get(idxB);
			int tempX = a.getRenderer().getScreenX();
			int tempY = a.getRenderer().getScreenY();
			a.getRenderer().setScreenX(b.getRenderer().getScreenX());
			a.getRenderer().setScreenY(b.getRenderer().getScreenY());
			b.getRenderer().setScreenX(tempX);
			b.getRenderer().setScreenY(tempY);
		}
	}
}
