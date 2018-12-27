package deck;

import java.util.List;
import java.util.Map;

import session.BoosterContent;
import session.BoosterHeader;
import session.DeckContent;
import session.DeckHeader;
import util.ApprenticeUtil;

public class Deck extends CardList {

	protected String name;
	protected Deck sideboard;

	public Deck() {
		this(null, (List<Card>) null);
	}

	public Deck(String name) {
		this(name, null);
	}

	public Deck(List<Card> cards) {
		this(null, cards);
	}

	public Deck(String name, List<Card> cards) {
		super(cards);
		this.name = name;
	}

	public Deck(DeckHeader header, DeckContent content) {
		this(header.getName(), content.getDeckCardList());
		Deck sideboard = new Deck(header.getName() + " - Sideboard", content.getSideboardCardList());
		setSideboard(sideboard);
	}

	public Deck(BoosterHeader header, BoosterContent content) {
		this("Booster", content.getDeckCardList());
		Deck sideboard = new Deck("Booster - Sideboard", content.getSideboardCardList());
		setSideboard(sideboard);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Deck copyRenderable() {
		return new Deck(name, getCopy());
	}

	public boolean hasSideboard() {
		return sideboard != null;
	}

	public Deck getSideboard() {
		return sideboard;
	}

	public void setSideboard(Deck sideboard) {
		this.sideboard = sideboard;
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
