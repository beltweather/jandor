package draft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import deck.Card;
import deck.Deck;
import jackson.AllSetsJson.SetCardJson;
import json.JSONArray;
import json.JSONException;
import session.BoosterContent;
import session.BoosterHeader;
import session.DraftHeader;
import session.Session;
import session.User;
import util.CardUtil;
import util.LoginUtil;

public class SetBoosterBuilder implements IBoosterBuilder {

	protected int draftId;
	protected List<Card> commons;
	protected List<Card> uncommons;
	protected List<Card> rares;
	protected List<Card> mythics;
	protected List<Card> nonbasicLands;
	protected List<Card> doubleFaced;
	protected List<Card> draftMatters;
	protected Random random = new Random();

	protected String setId;
	protected Deck deck;

	public SetBoosterBuilder(int draftId) {
		this.draftId = draftId;
	}

	protected void initDeck() {
		List<String> setPacks = getDraftHeader().getSetPacks();
		setId = setPacks.get(getDraftHeader().getRound());
		deck = new Deck(setId);
		List<SetCardJson> setCards = CardUtil.getCards(setId);
		for(SetCardJson setCard : setCards) {
			String name = setCard.name;
			Card card = new Card(name);
			if(card.isLand() && CardUtil.isBasicLandName(name)) {
				continue;
			}
			card.setSet(setId);
			deck.add(card);
		}

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
		doubleFaced = new ArrayList<Card>();
		draftMatters = new ArrayList<Card>();

		for(Card card : deck) {
			if(card.isCommon()) {
				commons.add(card);
			} else if(card.isUncommon()) {
				uncommons.add(card);
			} else if(card.isRare()) {
				rares.add(card);
			} else if(card.isMythic()) {
				mythics.add(card);
			}

			if(card.canTransform()) {
				doubleFaced.add(card);
			}

			if(card.isLand() && !CardUtil.isBasicLandName(card.getName())) {
				nonbasicLands.add(card);
			}

			if(isDraftMatters(card)) {
				draftMatters.add(card);
			}
		}
	}

	private boolean isDraftMatters(Card card) {
		return (card.getText() != null && card.getText().toLowerCase().contains("draft")) || card.getType().equalsIgnoreCase("Conspiracy");
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
		return deck;
	}

	public int buildBooster() {
		initDeck();

		DraftHeader draftHeader = getDraftHeader();
		List<Object> boosterArray = CardUtil.getBooster(setId);
		boolean addedFoil = false;
		Deck booster = new Deck("Booster");
		for(int i = 0; i < boosterArray.size(); i++) {
			Object obj;
				obj = boosterArray.get(i);
			String cardType;
			if(obj instanceof String) {
				cardType = obj.toString();
				if(cardType.equals("land") || cardType.equals("marketing")) {
					continue;
				}
			// For now assume all arrays are rare/mythic
			} else {
				cardType = "rare";
			}

			if(cardType.equals("common")) {
				if(!addedFoil && random.nextInt() % 4 == 0) {
					booster.add(getRandomCard(booster, deck));
					addedFoil = true;
				} else {
					booster.add(getRandomCard(booster, commons));
				}
			} else if(cardType.equals("uncommon")) {
				booster.add(getRandomCard(booster, uncommons));
			} else if(cardType.equals("rare")) {
				if(mythics.size() > 0 && random.nextInt() % 8 == 0) {
					booster.add(getRandomCard(booster, mythics));
				} else {
					booster.add(getRandomCard(booster, rares));
				}
			} else if(cardType.equals("mythic rare")) {
				booster.add(getRandomCard(booster, mythics));
			} else if(cardType.equals("double faced")) {
				booster.add(getRandomCard(booster, doubleFaced));
			} else if(cardType.equals("draft-matters")) {
				booster.add(getRandomCard(booster, draftMatters));

			// If we don't recognize the type, just add a random card
			} else {
				booster.add(getRandomCard(booster, deck));
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
