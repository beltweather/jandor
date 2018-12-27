package draft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import session.BoosterContent;
import session.BoosterHeader;
import session.DraftHeader;
import session.Session;
import session.User;
import util.CardUtil;
import util.LoginUtil;
import deck.Card;
import deck.Deck;

public class CustomBoosterBuilder implements IBoosterBuilder {

	protected int draftId;
	protected List<Card> commons;
	protected List<Card> uncommons;
	protected List<Card> rares;
	protected List<Card> mythics;
	protected List<Card> nonbasicLands;
	protected Random random = new Random();

	public CustomBoosterBuilder(int draftId) {
		this.draftId = draftId;
		init();
	}

	protected void init() {
		Deck deck = getDeck().copyRenderable();
		String set = findSet();

		// Remove the transformed version of any double face cards.
		Iterator<Card> it = deck.iterator();
		while(it.hasNext()) {
			Card card = it.next();
			if(card.canTransform() && !card.isFirstTransform()) {
				it.remove();
			}
		}

		commons = new ArrayList<Card>();
		uncommons = new ArrayList<Card>();
		rares = new ArrayList<Card>();
		mythics = new ArrayList<Card>();
		nonbasicLands = new ArrayList<Card>();

		for(Card card : deck) {
			if(set != null) {
				card.setSet(set);
			}

			if(card.isCommon()) {
				commons.add(card);
			} else if(card.isUncommon()) {
				uncommons.add(card);
			} else if(card.isRare()) {
				rares.add(card);
			} else if(card.isMythic()) {
				mythics.add(card);
			}

			if(card.isLand() && !CardUtil.isBasicLandName(card.getName())) {
				nonbasicLands.add(card);
			}
		}
	}

	private Card getRandomCard(Deck booster, List<Card> cards) {
		return getRandomCard(booster, cards, true);
	}

	private Card getRandomCard(Deck booster, List<Card> cards, boolean includeLands) {
		if(cards.isEmpty()) {
			return null;
		}
		Card card = null;
		while(card == null || (booster != null && booster.contains(card)) || CardUtil.isBasicLandName(card.getName())) {
			card = cards.get(random.nextInt(cards.size()));
			while(!includeLands && card.isLand()) {
				card = cards.get(random.nextInt(cards.size()));
			}
		}
		return card;
	}

	public DraftHeader getDraftHeader() {
		return Session.getInstance().getDraftHeader(draftId);
	}

	public Deck getDeck() {
		return Session.getInstance().getDeck(getDraftHeader().getId());
	}

	public String findSet() {
		Deck deck = getDeck();
		if(deck.size() == 0) {
			return null;
		}
		List<String> sets = deck.get(0).getSets();
		for(Card card : deck) {
			sets.retainAll(card.getSets());
		}
		if(sets.size() == 0) {
			return null;
		}
		return sets.get(0);
	}

	public int buildBooster() {
		Deck deck = getDeck();
		DraftHeader draftHeader = getDraftHeader();

		// Use the numbers in the draft header to construct the booster
		Deck booster = new Deck("Booster");
		if(draftHeader.getType() == DraftHeader.TYPE_RANDOM) {
			for(int i = 0; i < draftHeader.getTotalCards(); i++) {
				booster.add(getRandomCard(booster, deck, draftHeader.isIncludeLandsAsRarities()));
			}
		} else {
			if(draftHeader.isIncludeFoils() && random.nextInt() % 4 == 0 && draftHeader.getCommons() > 0) {
				booster.add(getRandomCard(null, deck));
				for(int i = 0; i < draftHeader.getCommons() - 1; i++) {
					booster.add(getRandomCard(booster, commons, draftHeader.isIncludeLandsAsRarities()));
				}
			} else {
				for(int i = 0; i < draftHeader.getCommons(); i++) {
					booster.add(getRandomCard(booster, commons, draftHeader.isIncludeLandsAsRarities()));
				}
			}

			for(int i = 0; i < draftHeader.getUncommons(); i++) {
				booster.add(getRandomCard(booster, uncommons, draftHeader.isIncludeLandsAsRarities()));
			}

			for(int i = 0; i < draftHeader.getRares(); i++) {
				if(draftHeader.isIncludeMythicsAsRares() && random.nextInt() % 8 == 0 && mythics.size() > 0) {
					booster.add(getRandomCard(booster, mythics, draftHeader.isIncludeLandsAsRarities()));
				} else {
					booster.add(getRandomCard(booster, rares, draftHeader.isIncludeLandsAsRarities()));
				}
			}

			if(!draftHeader.isIncludeLandsAsRarities() && nonbasicLands.size() > 0) {
				for(int i = 0; i < draftHeader.getLands(); i++) {
					booster.add(getRandomCard(booster, nonbasicLands, true));
				}
			}
		}

		Iterator<Card> it = booster.iterator();
		while(it.hasNext()) {
			if(it.next() == null) {
				it.remove();
			}
		}

		// Fill up booster so its size is right
		int totalCards = draftHeader.getTotalCards();
		for(int i = booster.size(); i < totalCards; i++) {
			booster.add(getRandomCard(booster, deck, draftHeader.isIncludeLandsAsRarities()));
		}

		User user = LoginUtil.getUser();
		BoosterHeader boosterHeader = new BoosterHeader();
		boosterHeader.newId();
		boosterHeader.setAuthor(user.getEmail());
		boosterHeader.setAuthorGUID(user.getGUID());
		boosterHeader.setAuthorUsername(user.getUsername());
		boosterHeader.setTimeFirstCreated(System.currentTimeMillis());
		boosterHeader.setDraftId(draftId);
		boosterHeader.setRound(draftHeader.getRound());
		boosterHeader.setTurn(draftHeader.getTurn());

		BoosterContent boosterContent = new BoosterContent();
		boosterContent.setId(boosterHeader.getId());
		boosterContent.setFromDeck(booster);

		boosterHeader.save();
		boosterContent.save();

		return boosterHeader.getId();
	}

}
