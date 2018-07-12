package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import run.Jandor;
import session.DeckHeader;
import session.Session;
import ui.ProgressBar.ProgressTask;
import util.ManaUtil;
import util.ShuffleType;
import util.ShuffleUtil;
import deck.Card;
import deck.Deck;

public class LandSimulation {
	
	public static final int DEFAULT_TARGET_SIZE = 60;
	public static final int MIN_LANDS = 16; // 25%
	public static final double MAX_LANDS = 26; //27; // 45%
	public static final int MAX_ITERATIONS = 10000;
	public static final String ALL_COLORED_LAND = "City of Brass";
	
	private Random random = new Random(System.currentTimeMillis());
	private int deckId;
	private boolean generic = true;
	private int targetSize;
	
	private SimResultList genericLandResults;
	private SimResultList basicLandResults;
	private SimResultList dualLandResults;
	private ProgressTask worker;
	
	public LandSimulation(int deckId) {
		this(deckId, DEFAULT_TARGET_SIZE);
	}
	
	public LandSimulation(int deckId, int targetSize) {
		this.deckId = deckId;
		this.targetSize = targetSize;
	}
	

	public SimResultList run() {
		return run(null);
	}
	
	public SimResultList run(ProgressTask worker) {
		this.worker = worker;
		genericLandResults = runGenericLands(); 
		basicLandResults = runBasicLands(genericLandResults);
		dualLandResults = runDualLands(basicLandResults);
		this.worker = null;
		return dualLandResults;
	}
	
	public SimResultList getGenericLandResults() {
		return genericLandResults;
	}
	
	public SimResultList getBasicLandResults() {
		return basicLandResults;
	}
	
	public SimResultList getDualLandResults() {
		return dualLandResults;
	}
	
	private void setProgress(double baseProgress, double perc) {
		setProgress(baseProgress, perc, 0.33);
	}
	
	private void setProgress(double baseProgress, double perc, double range) {
		if(worker == null) {
			return;
		}
		worker.setWorkerProgress((int) (100 * (baseProgress + perc * range)));
	}
	
	protected SimResultList runGenericLands() {
		setGeneric(true);
		Deck deck = getDeckCopy();
		removeLands(deck);
		
		DeckHeader header = Session.getInstance().getDeckHeader(deckId);
		String colors = header.getColors();
		boolean singleColor = colors.length() == 1;
		
		// For generic decks, choose the results with the middle deck size
		SimResultList results = new SimResultList() {

			@Override
			public SimResult breakTie(List<SimResult> tiedResults) {
				Collections.sort(tiedResults, new Comparator<SimResult>() {

					@Override
					public int compare(SimResult resultA, SimResult resultB) {
						return resultA.getTotalLandCount() - resultB.getTotalLandCount();
					}
					
				});
				
				int middleIdx = tiedResults.size() / 2;
				return tiedResults.get(middleIdx);
			}
			
		};
		
		double range = (double) MAX_LANDS - MIN_LANDS + 1;
		for(int landCount = MIN_LANDS; landCount <= MAX_LANDS; landCount++) {
			List<Card> lands = getLands(landCount);
			double score = score(deck, lands);
			results.add(new SimResult(targetSize, lands, score));
			setProgress(0, (landCount-MIN_LANDS) / range, singleColor ? 1.0 : 0.33);
		}
		return results;
	}
	
	/**
	 * @return The optimal number of basic lands for this deck, assuming 60 cards total. This may
	 *         mean that the number of cards in the deck needs to be reduced to match these lands.
	 */
	protected SimResultList runBasicLands(SimResultList genericResults) {
		int landTotal = genericResults.getBestResult().getTotalLandCount();
		setGeneric(false);
		Deck deck = getDeckCopy();
		removeLands(deck);

		DeckHeader header = Session.getInstance().getDeckHeader(deckId);
		String colors = header.getColors();
		if(colors.length() == 1) {
			return null;
		}
		
		// Break ties by the one that has the middle land spread
		SimResultList results = new SimResultList() {

			@Override
			public SimResult breakTie(List<SimResult> tiedResults) {
				Collections.sort(tiedResults, new Comparator<SimResult>() {

					@Override
					public int compare(SimResult resultA, SimResult resultB) {
						return resultA.getMaxSpecificLandCount() - resultB.getMaxSpecificLandCount();
					}
				
				});
				
				int middleIdx = tiedResults.size() / 2;
				return tiedResults.get(middleIdx);
			}
			
		};
		
		colors = colors.replace("C", "").replace("X", "");
		
		if(colors.length() == 2) {
		
			String typeA = colors.charAt(0) + "";
			String typeB = colors.charAt(1) + "";
			
			double range = landTotal - 1 - 1;
			for(int landACount = 1; landACount < landTotal - 1; landACount++) {
				int landBCount = landTotal - landACount;
				List<Card> lands = getLands(typeA, landACount);
				lands.addAll(getLands(typeB, landBCount));
				double score = score(deck, lands);
				results.add(new SimResult(targetSize, lands, score));
				setProgress(0.33, (landACount - 1) / range);
			}
			
			return results;
		}
		
		if(colors.length() == 3) {
			
			String typeA = colors.charAt(0) + "";
			String typeB = colors.charAt(1) + "";
			String typeC = colors.charAt(2) + "";
			
			for(int landACount = 1; landACount < landTotal - 2; landACount++) {
				for(int landBCount = 1; landBCount < landTotal - landACount - 1; landBCount++) {
					int landCCount = landTotal - landACount - landBCount;
					List<Card> lands = getLands(typeA, landACount);
					lands.addAll(getLands(typeB, landBCount));
					lands.addAll(getLands(typeC, landCCount));
					double score = score(deck, lands);
					results.add(new SimResult(targetSize, lands, score));
				}
				
			}
			
			return results;
		}
		
		if(colors.length() == 4) {
			
			String typeA = colors.charAt(0) + "";
			String typeB = colors.charAt(1) + "";
			String typeC = colors.charAt(2) + "";
			String typeD = colors.charAt(3) + "";
			
			for(int landACount = 1; landACount < landTotal - 3; landACount++) {
				for(int landBCount = 1; landBCount < landTotal - landACount - 2; landBCount++) {
					for(int landCCount = 1; landCCount < landTotal - landACount - landBCount - 1; landCCount++) {
						int landDCount = landTotal - landACount - landBCount - landCCount;
						List<Card> lands = getLands(typeA, landACount);
						lands.addAll(getLands(typeB, landBCount));
						lands.addAll(getLands(typeC, landCCount));
						lands.addAll(getLands(typeD, landDCount));
						double score = score(deck, lands);
						results.add(new SimResult(targetSize, lands, score));
					}
				}
			}
			
			return results;
		}
			
		if(colors.length() == 5) {
			
			String typeA = colors.charAt(0) + "";
			String typeB = colors.charAt(1) + "";
			String typeC = colors.charAt(2) + "";
			String typeD = colors.charAt(3) + "";
			String typeE = colors.charAt(3) + "";
			
			for(int landACount = 1; landACount < landTotal - 4; landACount++) {
				for(int landBCount = 1; landBCount < landTotal - landACount - 3; landBCount++) {
					for(int landCCount = 1; landCCount < landTotal - landACount - landBCount - 2; landCCount++) {
						for(int landDCount = 1; landDCount < landTotal - landACount - landBCount - landCCount - 1; landDCount++) {
							int landECount = landTotal - landACount - landBCount - landCCount - landDCount;
							List<Card> lands = getLands(typeA, landACount);
							lands.addAll(getLands(typeB, landBCount));
							lands.addAll(getLands(typeC, landCCount));
							lands.addAll(getLands(typeD, landDCount));
							lands.addAll(getLands(typeE, landECount));
							double score = score(deck, lands);
							results.add(new SimResult(targetSize, lands, score));
						}
					}
				}
			}
			
			return results;
		}
		
		return null;
	}
	
	/**
	 * @return The optimal number of basic and dual lands for this deck, assuming 60 cards total. This may
	 *         mean that the number of cards in the deck needs to be reduced to match these lands.
	 */
	protected SimResultList runDualLands(SimResultList basicResults) {
		if(basicResults == null) {
			return null;
		}
		SimResult bestBasicResult = basicResults.getBestResult();
		Map<String, Integer> countsByLand = bestBasicResult.getCountsByLand();
		int landCount = bestBasicResult.getTotalLandCount();
		setGeneric(false);
		Deck deck = getDeckCopy();
		removeLands(deck);
		
		SimResultList results = new SimResultList() {

			@Override
			public SimResult breakTie(List<SimResult> tiedResults) {
				Collections.sort(tiedResults, new Comparator<SimResult>() {
					
					@Override
					public int compare(SimResult resultA, SimResult resultB) {
						return resultA.getCountsByLand().get(ALL_COLORED_LAND) - resultB.getCountsByLand().get(ALL_COLORED_LAND);
					}
					
				});
				return tiedResults.get(0);
			}
			
		};
		
		// At best let up to half the cards be replaced by multi-mana cards;
		int colorCount = countsByLand.size();
		double range = (double) (landCount / 2) / colorCount;
		for(int i = 0; i < landCount / 2; i += colorCount) {
			List<Card> lands = new ArrayList<Card>();
			for(String land : countsByLand.keySet()) {
				int count = countsByLand.get(land);
				for(int j = 0; j < count - (i / colorCount); j++) {
					lands.add(new Card(land));
				}
			}
			
			while(lands.size() + i > landCount) {
				lands.remove(0);
			}
			while(lands.size() < landCount) {
				lands.add(new Card(ALL_COLORED_LAND));
			}
			
			double score = score(deck, lands);
			SimResult result = new SimResult(targetSize, lands, score);
			if(i == 0) {
				result.add(ALL_COLORED_LAND, 0);
			}
			results.add(result);
			setProgress(0.66, (i/colorCount) / range);
		}
		
		return results;
	}

	public boolean isGeneric() {
		return generic;
	}
	
	public void setGeneric(boolean generic) {
		this.generic = generic;
	}
	
	private double modifyScore(double score) {
		return Math.round(score);
	}
	
	private Deck getDeckCopy() {
		return Session.getInstance().getDeck(deckId).copyRenderable();
	}
	
	private int getRandomCardIndex(Deck deck) {
		return random.nextInt(deck.size());
	}
	
	private void removeRandomCard(Deck deck) {
		deck.remove(getRandomCardIndex(deck));
	}
	
	private void removeLands(Deck deck) {
		Iterator<Card> it = deck.iterator();
		while(it.hasNext()) {
			Card card = it.next();
			if(card.isLand()) {
				it.remove();
			}
		}
	}
	
	private void clipDeck(Deck deck, int targetSize) {
		while(deck.size() > targetSize) {
			removeRandomCard(deck);
		}
	}
	
	protected int calculateBestCount(Map<Integer, Double> scoresByCount) {
		double minScore = Double.MAX_VALUE;
		List<Integer> tiedLandCounts = new ArrayList<Integer>(); 
		for(int landCount : scoresByCount.keySet()) {
			double score = scoresByCount.get(landCount);
			score = modifyScore(score);
			if(score < minScore) {
				minScore = score;
				tiedLandCounts.clear();
				tiedLandCounts.add(landCount);
			} else if(score == minScore) {
				tiedLandCounts.add(landCount);
			}
		}
		
		int scoreLandCount = 0;
		for(Integer landCount : tiedLandCounts) {
			scoreLandCount += landCount;
		}
		scoreLandCount  = (int) Math.round(scoreLandCount / (double) tiedLandCounts.size());
		return scoreLandCount;
	}
	
	protected double score(Deck deck, List<Card> lands) {
		deck = deck.copyRenderable();
		deck.addAll(lands);
		
		int maxTurns = Math.min(deck.getMaxConvertedManaCost(), deck.size() - 1);
		List<Double> scores = new ArrayList<Double>();
		for(int i = 0; i < MAX_ITERATIONS; i++) {
			double manaDifference = 0; // This is the amount of mana needed for the cheapest card, or mana wasted by having cards not expensive enough
			ShuffleUtil.shuffle(ShuffleType.PLAYER, deck);
			List<Card> hand = new ArrayList<Card>();
			List<Card> landsInPlay = new ArrayList<Card>();
			// Add six cards to hand so on turn one, we start with 7
			
			for(int j = 0; j < 6; j++) {
				if(j >= deck.size()) {
					return 0;
				}
				hand.add(deck.get(j));
			}
			for(int j = 7; j <= maxTurns + 7; j++) {
				// Get how much mana you should have by this turn
				int onCurveMana = j - 6;
				
				if(j >= deck.size()) {
					return 0;
				}
				
				// Draw card
				hand.add(deck.get(j));
				
				// Play land if you can
				Iterator<Card> it = hand.iterator();
				boolean playedLand = false;
				while(it.hasNext()) {
					Card card = it.next();
					if(card.isLand()) {
						landsInPlay.add(card);
						it.remove();
						playedLand = true;
						break;
					}
				}
				int availableMana = landsInPlay.size();
				
				// add a small penalty for missing land drops
				if(!playedLand) {
					manaDifference += 1;
				}
				
				// Check cards in hand for greediest play
				List<Card> bestPlay = findBestPlay(hand, landsInPlay);
				
				// Find if there's a card that falls between these two
				int curveDiff = 0;
				boolean offCurve = false;
				for(Card card : hand) {
					int cc = card.getConvertedManaCost();
					if(cc > availableMana && cc <= onCurveMana) {
						offCurve = true;
						curveDiff = cc- availableMana; //(cc - availableMana) * 2;
					} else if(cc >= onCurveMana && cc <= availableMana) {
						
						boolean usingEqualValueCard = false;
						for(Card playCard : bestPlay) {
							if(playCard.equals(card) || playCard.getConvertedManaCost() >= cc) {
								usingEqualValueCard = true;
								break;
							}
						}
						
						// Heavy penalty likely due to mana screw
						if(!usingEqualValueCard) {
							offCurve = true;
							curveDiff = cc;
						}
					}
				}
				
				// Consider missing your curve as twice as bad as wasting mana 
				if(offCurve) {
					manaDifference += curveDiff;
					continue;
				}
				
				it = hand.iterator();
				int manaCost = 0;
				for(Card card : bestPlay) {
					manaCost += card.getConvertedManaCost();
					hand.remove(card);
				}
				int mDiff = Math.abs(availableMana - manaCost);
				
				if(offCurve) {
					mDiff = mDiff + curveDiff;
				} 
				
				manaDifference += mDiff;
			}
			
			scores.add(manaDifference);
		}
		
		Collections.sort(scores, new Comparator<Double>() {

			@Override
			public int compare(Double arg0, Double arg1) {
				return arg0.compareTo(arg1);
			}

		});
		
		int size = scores.size();
		int startIdx = (int) Math.round(0.1 * size);
		int endIdx = size - startIdx;
		int newSize = endIdx - startIdx;
		double totalScore = 0;
		for(int i = startIdx; i < endIdx; i++) {
			totalScore += scores.get(i);
		}
		
		return totalScore / (double) newSize;
	}
	
	/**
	 * Removes the used lands from this list to play the card and returns true, or returns false if it can't 
	 */
	private boolean removeLandsAndPlay(Card card, List<Card> landsInPlay) {
		int removeCount = -1;
		if(generic) {
			int cc = card.getConvertedManaCost();
			if(cc <= landsInPlay.size()) {
				for(int i = 0; i < cc; i++) {
					//landsInPlay.remove(0);
				}
				removeCount = cc;
			}
			//return true;
		}
		
		List<String> manaCost = card.getManaCost();
		List<String> realManaCost = new ArrayList<String>();
		for(String mana : manaCost) {
			if(ManaUtil.isColorless(mana)) {
				if(mana.equals("X")) {
					realManaCost.add("1");
				} else {
					Integer cost = Integer.valueOf(mana);
					for(int i = 0; i < cost; i++) {
						realManaCost.add("1");
					}
				}
			} else {
				realManaCost.add(mana);
			}
		}
		manaCost = realManaCost;
		
		Collections.sort(manaCost, new Comparator<String>() {

			@Override
			public int compare(String manaA, String manaB) {
				return ManaUtil.indexOfColorCharacter(manaA) - ManaUtil.indexOfColorCharacter(manaB);
			}
			
		});
		final String all = "[\"W\",\"U\",\"B\",\"R\",\"G\"]";
		Collections.sort(landsInPlay, new Comparator<Card>() {

			@Override
			public int compare(Card cardA, Card cardB) {
				String colorIdentityA = cardA.getName().equals(ALL_COLORED_LAND) ? all : cardA.getColorIdentity().toString();
				String colorIdentityB = cardB.getName().equals(ALL_COLORED_LAND) ? all : cardB.getColorIdentity().toString();
				return colorIdentityA.length() - colorIdentityB.length();
			}
	
		});
		List<Card> landsToUse = new ArrayList<Card>();
		main: for(String mana : manaCost) {
			Iterator<Card> it = landsInPlay.iterator();
			while(it.hasNext()) {
				Card land = it.next();
				if(landsToUse.contains(land)) {
					continue;
				}
				String colorIdentity = land.getName().equals(ALL_COLORED_LAND) ? all : land.getColorIdentity().toString();
				for(int i = 0; i < mana.length(); i++) {
					String m = mana.charAt(i) + "";
					if(generic || ManaUtil.isColorless(m) || colorIdentity.contains(m)) {
						landsToUse.add(land);
						continue main;
					}
				}
			}
		}
		
		if(removeCount != -1 && removeCount != landsToUse.size()) {
			System.err.println("Algorithms not the same!");
		}
		
		boolean match = landsToUse.size() == card.getConvertedManaCost();
		if(!match) {
			return false;
		}
		
		for(Card land : landsToUse) {
			landsInPlay.remove(land);
		}
		
		return true;
	}
	
	private List<Card> findBestPlay(List<Card> hand, List<Card> landsInPlay) {
		landsInPlay = new ArrayList<Card>(landsInPlay);
		int manaCount = landsInPlay.size();
		List<Card> maxBestPlay = null;
		if(landsInPlay.size() == 0) {
			return new ArrayList<Card>();
		}
		
		Collections.sort(hand, new Comparator<Card>() {

			@Override
			public int compare(Card cardA, Card cardB) {
				return cardB.getConvertedManaCost() - cardA.getConvertedManaCost();
			}
		
		});
		
		int maxManaDifference = Integer.MAX_VALUE;
		for(int i = 0; i < hand.size(); i++) {
			List<Card> bestPlay = new ArrayList<Card>();
			Card card = hand.get(i);
			if(card.isLand() || card.getConvertedManaCost() > manaCount) {
				continue;
			}
			
			if(removeLandsAndPlay(card, landsInPlay)) {
				bestPlay.add(card);
				if(landsInPlay.size() > 0) {
					List<Card> newHand = new ArrayList<Card>(hand);
					newHand.remove(card);
					bestPlay.addAll(findBestPlay(newHand, landsInPlay));
				}
			}
			
			int manaDifference = landsInPlay.size();
			if(manaDifference <= maxManaDifference || (manaDifference == maxManaDifference && (maxBestPlay == null || bestPlay.size() <= maxBestPlay.size()))) {
				maxBestPlay = bestPlay;
				maxManaDifference = manaDifference;
				break;
			}
		}
		
		if(maxBestPlay == null) {
			return new ArrayList<Card>();
		}
		
		return maxBestPlay;
	}

	private List<Card> getLands(int landCount) {
		List<Card> lands = new ArrayList<Card>();
		for(int i = 0; i < landCount; i++) {
			lands.add(new Card("Plains"));
		}
		return lands;
	}
	
	private String getLandName(String manaType) {
		if(manaType.equals("W")) {
			return "Plains";
		}
		if(manaType.equals("U")) {
			return "Island";
		}
		if(manaType.equals("B")) {
			return "Swamp";
		}
		if(manaType.equals("R")) {
			return "Mountain";
		}
		if(manaType.equals("G")) {
			return "Forest";
		}
		return "Wastes";
	}
	
	private List<Card> getLands(String typeA, int typeACount) {
		List<Card> lands = new ArrayList<Card>();
		String landA = getLandName(typeA);
		for(int i = 0; i < typeACount; i++) {
			lands.add(new Card(landA));
		}
		return lands;
	}
	
	protected Map<String, Integer> getColorCounts(int totalCount, int typeACount) {
		DeckHeader header = Session.getInstance().getDeckHeader(deckId);
		String colors = header.getColors();
		if(colors.length() == 1) {
			return null;
		}
		
		if(colors.length() == 2) {
		
			String typeA = colors.charAt(0) + "";
			String typeB = colors.charAt(1) + "";

			Map<String, Integer> landCounts = new HashMap<String, Integer>();
			landCounts.put(typeA, typeACount);
			landCounts.put(typeB, totalCount - typeACount);
			return landCounts;
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		Jandor.init();
		List<Integer> ids = new ArrayList<Integer>();
		//ids.add(43741); // Mefolk 95354 // Divinity 43741
		for(DeckHeader header : Session.getInstance().getDeckHeaders()) {
			ids.add(header.getId());
		}
		
		for(int deckId : ids) {
			Deck deck = Session.getInstance().getDeck(deckId);
			LandSimulation sim = new LandSimulation(deckId);
			SimResultList genericResults = sim.runGenericLands();
			SimResult bestGenericResult = genericResults.getBestResult();
			
			double rawLandPerc = bestGenericResult.getTotalLandPercent();
			double landPerc = Math.round(100 * rawLandPerc) / 100.0;
			Deck deckNoLands = deck.copyRenderable();
			sim.removeLands(deckNoLands);
	
			int oldLandCount = deck.size() - deckNoLands.size();
			int newLandCount = (int) Math.round(deck.size() * landPerc);
			
			double oldLandPerc = oldLandCount / (double) deck.size();
			
			oldLandPerc = Math.round(100 * oldLandPerc);
			landPerc = Math.round(100 * landPerc);
			double percDiff = landPerc - oldLandPerc;
			
			int landDiff = newLandCount - oldLandCount;
			System.out.println(deck.getName() + ":" + (landDiff >= 0 ? " +" : " -") + Math.abs(landDiff) + " land" + (Math.abs(landDiff) == 0 ? "" : "s") + "(land perc: " + landPerc + ", total lands: " + newLandCount + ")");
			
			SimResultList basicResults = sim.runBasicLands(genericResults);
			if(basicResults != null) {
				System.out.println("Basic Land Recommendation:");
				SimResult bestBasicResult = basicResults.getBestResult();
				for(String land : bestBasicResult.getCountsByLand().keySet()) {
					System.out.println(land + ": " + bestBasicResult.getCountsByLand().get(land));
				}
			
				SimResultList dualResults = sim.runDualLands(basicResults);
				if(dualResults != null) {
					System.out.println("Dual Land Recommendation:");
					for(SimResult result : dualResults) {
						System.out.println("City of Brass " + result.getCountsByLand().get(ALL_COLORED_LAND) + ": " + result.getScore());
					}
					
					System.out.println("Best Choice: ");
					SimResult bestDualResult = dualResults.getBestResult();
					for(String land : bestDualResult.getCountsByLand().keySet()) {
						System.out.println(land + ": " + bestDualResult.getCountsByLand().get(land));
					}
				}
			}
			
		}
	}
	
}
