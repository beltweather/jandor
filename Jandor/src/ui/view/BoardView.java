package ui.view;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLayeredPane;

import canvas.Canvas;
import canvas.CardLayer;
import canvas.LightCardLayer;
import deck.Deck;
import session.DeckHeader;
import session.Session;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PTabPane;
import util.FriendUtil;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;

public class BoardView extends JandorView {
	
	public static final String DEFAULT_TITLE = "Board";
	public static final int DEFAULT_WIDTH = 800; //1915; //1435; //1915; 
	public static final int DEFAULT_HEIGHT = 600; //974; //794; //974;
	
	protected Canvas canvas;
	protected CardLayer cardLayer;
	protected int deckId;
	
	public BoardView(String name) {
		this(name, true);
	}
	
	public BoardView(String name, boolean enableListeners) {
		super(name, enableListeners);
		rebuild();
	}

	public void setDeckId(final int deckId) {
		this.deckId = deckId;
		SessionEventManager.removeListeners(this);
		SessionEventManager.addListener(DeckHeader.class, deckId, SessionEvent.TYPE_DELETED, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				PTabPane pane = JUtil.getTabPane(BoardView.this);
				int i = pane.indexOfComponent(BoardView.this.getParent());
				pane.removeTabAt(i);
			}
			
		});
		
		SessionEventManager.addListener(DeckHeader.class, deckId, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				if(event.isType(SessionEvent.TYPE_DELETED)) {
					return;
				}
				
				Deck deck = new Deck(Session.getInstance().getDeckHeader(deckId), Session.getInstance().getDeckContent(deckId));
				setDeck(deck.copyRenderable());	
			}
			
		});
	}
	
	@Override
	protected void rebuild() {
		removeAll();
		if(deck == null) {
			deck = new Deck("Empty");
			//String filename = "X:/Users/Jon/Downloads/Inkfathom Witch.dec";
			//deck = ApprenticeUtil.toDeck(filename);
		}
		deck.shuffle(shuffleType);
		
		canvas = new Canvas();
		canvas.setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		//canvas.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		
		List<CardLayer> syncedLayers = null;
		if(cardLayer != null) {
			syncedLayers = cardLayer.getSyncedLayers();
			CardLayer.unregister(cardLayer);
		}
		
		//cardLayer = new CardLayer(canvas, deck.getCopy(), enableListeners);
		if(enableListeners) {
			cardLayer = new CardLayer(canvas, deck.getCopy(), enableListeners);
		} else {
			cardLayer = new LightCardLayer(canvas);
		}
		
		CardLayer.register(cardLayer);
		
		canvas.addLayer(cardLayer);
		
		if(syncedLayers != null) {
			for(CardLayer layer : syncedLayers) {
				cardLayer.syncLayer(layer);
			}
		}
		
		c.weaken();
		c.fill = G.HORIZONTAL;
		addc(cardLayer.getOpponentButtonPanel());
		c.gridy++;
		c.strengthen();
		addc(canvas);
		c.gridy++;
		c.weaken();
		c.fill = G.HORIZONTAL;
		addc(cardLayer.getButtonPanel());
		
		revalidate();
	}
	
	public CardLayer getCardLayer() {
		return cardLayer;
	}

	@Override
	public void reset() {
		cardLayer.reset();
	}

	@Override
	public void handleClosed() {
		cardLayer.handleClosed();
		SessionEventManager.removeListeners(this);
		FriendUtil.disconnectFromFriend(this);
	}
	
}
