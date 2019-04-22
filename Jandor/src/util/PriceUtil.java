package util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import deck.Card;
import deck.Deck;
import jackson.JacksonUtil;

public class PriceUtil {

	private PriceUtil() {}

	public static interface OnPriceFetchComplete {

		public void onPriceFetchComplete(List<Card> updatedCards);

	}

	public static void fetchPrices(Deck deck, OnPriceFetchComplete onComplete) {
		List<Card> cards = new ArrayList<Card>();
		cards.addAll(deck.getCountsByCard().keySet());
		if(deck.hasSideboard()) {
			cards.addAll(deck.getSideboard().getCountsByCard().keySet());
		}
		fetchPrices(cards, onComplete);
	}

	public static void fetchPrices(List<Card> cards, OnPriceFetchComplete onComplete) {
		/*List<Integer> productIds = new ArrayList<>();
		List<Card> productIdCards = new ArrayList<>();
		for(Card c : cards) {
			if(!c.hasPriceInfo() && c.getTCGPlayerProductId() != 0) {
				productIds.add(c.getTCGPlayerProductId());
				productIdCards.add(c);
			}
		}

		if(productIdCards.size() == 0) {
			if(onComplete != null) {
				onComplete.onPriceFetchComplete(productIdCards);
			}
			return;
		}*/

		List<Card> cardsToFetch = new ArrayList<>();
		for(Card card : cards) {
			if(!card.hasPriceInfo()) {
				card.getCardInfo().price = new PriceJson();
				cardsToFetch.add(card);
			}
		}

		fetchPricesInternal(cardsToFetch, onComplete);

		/*TaskUtil.run(() -> {

			String productIdStr = toListStr(productIds);

			URL url = null;
			try {
				url = new URL("http://api.tcgplayer.com/v1.19.0/pricing/product/" + productIdStr);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return;
			}
			doGetPrice(url, (response) -> {

				// Do a get to tcg player some how and then when it returns with results, parse them.
				String fakeResponse = getFakeResponse(productIds);

				PriceResponse priceResponse = JacksonUtil.read(PriceResponse.class, fakeResponse);
				if(priceResponse == null) {
					System.err.print("fetchPrices request returned null!");
					return;
				}

				if(!priceResponse.success) {
					System.err.println("fetchPrices returned with errors:");
					if(priceResponse.errors != null) {
						for(String error : priceResponse.errors) {
							System.err.println(error);
						}
					}
					return;
				}

				for(int i = 0; i < productIdCards.size(); i++) {
					Card card = productIdCards.get(i);
					PriceJson price = priceResponse.results.get(i);
					card.getCardInfo().price = price;
				}

				if(onComplete != null) {
					onComplete.onPriceFetchComplete(productIdCards);
				}

			});

		});*/
	}

	private static String toListStr(List objs) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object o : objs) {
			if(first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(o);
		}
		return sb.toString();
	}

	private static interface OnGetResponse {

		public void onGetResponse(Object response);

	}

	private static void doGetPrice(URL url, OnGetResponse onGetResponse) {
		onGetResponse.onGetResponse(null);
	}

	private static final class PriceResponse {

		public boolean success;
		public List<String> errors;
		public List<PriceJson> results;

	}

	public static final class PriceJson {

		public int productId;
		public double lowPrice;
		public double midPrice;
		public double highPrice;
		public double marketPrice;
		public double directLowPrice;
		public String subTypeName;

	}

	private static String getFakeResponse(List<Integer> productIds) {
		StringBuilder results = new StringBuilder();
		boolean first = true;
		for(Integer id : productIds) {
			if(first) {
				first = false;
			} else {
				results.append(", ");
			}

			//double marketPrice = Math.random() * 10;
			double marketPrice = 1.00;

			results.append(
				"{" +
			      "\"productId\": " + id + "," +
			      "\"lowPrice\": 1.23," +
			      "\"midPrice\": 2.34," +
			      "\"highPrice\": 3.45," +
			      "\"marketPrice\": " + marketPrice + "," +
			      "\"directLowPrice\": 0.12," +
			      "\"subTypeName\": \"string\"" +
			    "}");
		}
		String response = "{\"success\": true, \"errors\": [\"string\"], \"results\": [" + results + "]}";
		return response;
	}

	private static final class CollectionRequest {

		public List<CollectionIdentifier> identifiers = new ArrayList<>();

	}

	private static class CollectionIdentifier {

	}

	private static final class MultiverseIdentifier extends CollectionIdentifier {

		public MultiverseIdentifier(int multiverse_id) {
			this.multiverse_id = multiverse_id;
		}

		public int multiverse_id;

	}

	private static final class NameIdentifier extends CollectionIdentifier {

		public NameIdentifier(String name) {
			this.name = name;
		}

		public String name;

	}

	private static class CollectionResponse {

		public List<CollectionCard> data;

	}

	private static class CollectionCard {

		public String id;
		public List<Integer> multiverse_ids;
		public int tcgplayer_id;
		public double usd;

	}

	private static final int REQUEST_IDENTIFIER_LIMIT = 75;

	private static void fetchPricesInternal(List<Card> cards, OnPriceFetchComplete onComplete) {
		String url = "https://api.scryfall.com/cards/collection";

		List<CollectionRequest> requests = new ArrayList<>();
		final List<List<Card>> cardChunks = split(cards, REQUEST_IDENTIFIER_LIMIT);
		for(List<Card> chunk : cardChunks) {
			requests.add(toCollectionRequest(chunk));
		}
		WebUtil.doMultiPost(url, requests, (String responseStr) -> {

			List<CollectionResponse> responses = JacksonUtil.readList(CollectionResponse.class, responseStr);
			for(int i = 0; i < responses.size(); i++) {
				CollectionResponse response = responses.get(i);
				List<Card> chunk = cardChunks.get(i);
				handleResponse(chunk, response);
			}

			if(onComplete != null) {
				onComplete.onPriceFetchComplete(cards);
			}

		}, null);

		/*CollectionRequest request = toCollectionRequest(cards);
		WebUtil.doPost(url, request, (String responseStr) -> {
			CollectionResponse response = JacksonUtil.read(CollectionResponse.class, responseStr);

			handleResponse(cards, response);

			if(onComplete != null) {
				onComplete.onPriceFetchComplete(cards);
			}
		}, null);*/
	}

	private static CollectionRequest toCollectionRequest(List<Card> cards) {
		CollectionRequest request = new CollectionRequest();
		for(Card card : cards) {
			request.identifiers.add(new NameIdentifier(card.getName()));
		}
		return request;
	}

	private static List<List<Card>> split(List<Card> cards, int chunkSize) {
		List<List<Card>> cardChunks = new ArrayList<>();
		List<Card> chunk = new ArrayList<>();
		for(Card card : cards) {
			if(chunk.size() < chunkSize) {
				chunk.add(card);
			} else {
				cardChunks.add(chunk);
				chunk = new ArrayList<>();
			}
		}
		if(chunk.size() > 0) {
			cardChunks.add(chunk);
		}
		return cardChunks;
	}

	private static void handleResponse(List<Card> cards, CollectionResponse response) {
		for(int i = 0; i < response.data.size(); i++) {
			Card card = cards.get(i);
			double price = response.data.get(i).usd;
			card.getCardInfo().price.marketPrice = price;
		}
	}

}
