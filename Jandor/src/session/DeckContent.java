package session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;
import deck.Card;
import deck.Deck;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeckContent extends SessionData {

	public static DeckContent createDefaultContent(int id) {
		DeckContent content = new DeckContent(id);
		return content;
	}
	
	@XmlElement(name="card")
	public List<CardContent> cards = new ArrayList<CardContent>();
	
	public DeckContent() {
		super();
	}
		
	public DeckContent(int id) {
		super(id);
	}
	
	public DeckContent(DeckHeader header, Deck deck) {
		this(header.getId(), deck);
	}
	
	public DeckContent(int id, Deck deck) {
		this.id = id;
		setFromDeck(deck);
	}
	
	public void setFromDeck(Deck deck) {
		cards.clear();
		Map<Card, Integer> countsByCard = deck.getCountsByCard();
		for(Card card : countsByCard.keySet()) {
			addCard(new CardContent(card.getName(), countsByCard.get(card), false));
		}
		if(deck.getSideboard() != null) {
			countsByCard = deck.getSideboard().getCountsByCard();
			for(Card card : countsByCard.keySet()) {
				addCard(new CardContent(card.getName(), countsByCard.get(card), true));
			}
		}
	}
	
	public List<Card> getDeckCardList() {
		List<Card> cardList = new ArrayList<Card>();
		for(CardContent card : cards) {
			for(int i = 0; i < card.getCount(); i++) {
				if(card.isSideboard()) {
					continue;
				}
				Card newCard = new Card(card.getName());
				if(!newCard.exists()) {
					System.err.println("Could not find card \"" + card.getName() + "\". Please check that it is a real card and in the current dataset.");
					continue;
				}
				cardList.add(newCard);
			}
		}
		return cardList;
	}
	
	public List<Card> getSideboardCardList() {
		List<Card> cardList = new ArrayList<Card>();
		for(CardContent card : cards) {
			for(int i = 0; i < card.getCount(); i++) {
				if(!card.isSideboard()) {
					continue;
				}
				Card newCard = new Card(card.getName());
				if(!newCard.exists()) {
					System.err.println("Could not find card \"" + card.getName() + "\". Please check that it is a real card and in the current dataset.");
					continue;
				}
				cardList.add(newCard);
			}
		}
		return cardList;
	}
	
	public List<CardContent> getCards() {
		return cards;
	}
	
	public void setCards(List<CardContent> cards) {
		this.cards = new ArrayList<CardContent>(cards);
	}
	
	public void addCard(CardContent card) {
		cards.add(card);
	}
	
	public void clearCards() {
		cards.clear();
	}
	
	@Override
	public File getFolder() {
		return FileUtil.getContentFolder();
	}
	
	public DeckContent copy() {
		DeckContent copy = new DeckContent();
		copy.id = id;
		copy.cards = new ArrayList<CardContent>(cards);
		return copy;
	}

}
