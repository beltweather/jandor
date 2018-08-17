package deck;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import json.JSONException;
import json.JSONObject;
import util.ApprenticeUtil;
import util.JSONUtil;
import util.ShuffleType;
import util.ShuffleUtil;

public class CardList extends RenderableList<Card> {

	private static final long serialVersionUID = 1L;

	public CardList() {
		this(null);
	}
	
	public CardList(List<Card> cards) {
		super(cards);
	}
	
	public Map<Card, Integer> getCountsByCard() {
		Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
		CardList d = new CardList(getCopy());
		String commanderName = null;
		for(Card card : d) {
			String c = card.getName();
			if(!counts.containsKey(c)) {
				counts.put(c, 1);
			} else {
				counts.put(c, counts.get(c) + 1);
			}
			if(card.isCommander()) {
				commanderName = c;
			}
		}
		
		Map<Card, Integer> countsByCard = new LinkedHashMap<Card, Integer>();
		for(String name : counts.keySet()) {
			Card card = new Card(name);
			if(commanderName != null && name.equals(commanderName)) {
				card.setCommander(true);
			}
			countsByCard.put(card, counts.get(name));
		}
		return countsByCard;
	}
	
	public int getMaxConvertedManaCost() {
		int cmc = 0;
		for(Card card : this) {
			if(card.getConvertedManaCost() > cmc) {
				cmc = card.getConvertedManaCost();
			}
		}
		return cmc;
	}
	
	public void shuffle() {
		ShuffleUtil.shuffle(ShuffleType.PLAYER, this);
	}
	
	public void shuffle(ShuffleType shuffleType) {
		ShuffleUtil.shuffle(shuffleType, this);
	}
	
	public static void main1(String[] args) {
		String filename = "AllCards.json";
		JSONObject json = JSONUtil.toJSON(filename);
		try {
			System.out.println(json.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filename = "X:/Users/Jon/Downloads/R Walls 20.dec";
		Deck deck = ApprenticeUtil.toDeck(filename);
		Map<Card, Integer> counts = deck.getCountsByCard();
			for(Card c : counts.keySet()) {
				System.out.println(deck.getCount(c) + " " + c.getName());
				System.out.println("Set: " + c.getSets().toString());
				System.out.println("Info: \n" + c.getCardInfo());
			}
	}
}
