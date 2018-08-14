package canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import canvas.animation.Animator;
import canvas.animation.SpinAnimator;
import canvas.gesture.Gesture;
import canvas.gesture.ShakeGesture;
import canvas.handler.MouseHandlerManager;
import canvas.handler.RenderableHandler;
import deck.Card;
import deck.CardList;
import deck.RenderableList;
import dice.Counter;
import dice.D10;
import dice.Die;
import dice.DieList;
import dice.Token;
import session.Session;
import ui.pwidget.CloseListener;
import ui.pwidget.ColorUtil;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.pwidget.JandorMenuBar;
import ui.pwidget.PPanel;
import util.ImageUtil;
import util.SerializationUtil;
import util.ShapeUtil;
import util.ShuffleType;
import util.ShuffleUtil;
import zone.Zone;
import zone.ZoneManager;
import zone.ZoneRenderer;
import zone.ZoneType;

public class CardLayer implements ICanvasLayer, CloseListener, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final int DRAG_MODE_CARD = 0;
	private static final int DRAG_MODE_SELECT = 1;
	
	public static final Color COLOR_SELECT = new Color(107, 241, 107); //new Color(74, 181, 74); // Purple // new Color(128, 128, 192); // Gold // new Color(239, 216, 80);
	public static final int STROKE_SELECT = 3;
	public static final Color DEFAULT_BACKGROUND_COLOR = new Color(124, 124, 124);
	
	protected static String BACKGROUND_FILENAME = "background-0-dark.png";
	protected static String OPPONENT_BACKGROUND_FILENAME = "background-0-light.png";
	
	protected static boolean SHOW_CARD_COUNTS = false;
	protected static boolean LIGHT_VIEW = false;
	
	public static boolean isLightView() {
		return LIGHT_VIEW;
	}
	
	public static boolean isShowCardCounts() {
		return SHOW_CARD_COUNTS;
	}
	
	public static void setLightView(boolean lightView) {
		LIGHT_VIEW = lightView;
		if(lightView) {
			BACKGROUND_FILENAME = "background-0-light.png";
			OPPONENT_BACKGROUND_FILENAME = "background-0-dark.png";
		} else {
			BACKGROUND_FILENAME = "background-0-dark.png";
			OPPONENT_BACKGROUND_FILENAME = "background-0-light.png";
		}
		CardLayer layer = getActiveCardLayer();
		if(layer != null) {
			layer.repaint();
		}
	}

	public static void setShowCardCounts(boolean showCardCounts) {
		SHOW_CARD_COUNTS = showCardCounts;
		CardLayer layer = getActiveCardLayer();
		if(layer != null) {
			layer.repaint();
		}
	}
	
	private static final Set<String> loadedCardNames = new HashSet<String>();
	
	private static CardLayer activeCardLayer = null;
	
	public static CardLayer getActiveCardLayer() {
		return activeCardLayer;
	}
	
	public static void clearActiveCardLayer() {
		activeCardLayer = null;
	}
	
	private static List<CardLayer> allCardLayers = new ArrayList<CardLayer>();
	
	public static void register(CardLayer layer) {
		if(!allCardLayers.contains(layer)) {
			allCardLayers.add(layer);
		}
	}
	
	public static void unregister(CardLayer layer) {
		if(allCardLayers.contains(layer)) {
			allCardLayers.remove(layer);
		}
	}
	
	public static List<CardLayer> getAllCardLayers() {
		return new ArrayList<CardLayer>(allCardLayers);
	}
	
	// SERIALIZED FIELDS
	
	protected int zIndex = 0;
	
	protected int screenW = 0;
	protected int screenH = 0;
	
	private DieList d10s = new DieList();
	private DieList counters = new DieList();
	private DieList tokens = new DieList();
	
	private Card loadingCard;
	private Animator loading; // XXX Hard to serialize

	protected MouseHandlerManager handlerManager = new MouseHandlerManager();
	private RenderableHandler handler;
	
	private CardList allCards = new CardList();
	private RenderableList<IRenderable> allObjects;
	
	private String currentUsername = null;

	// TRANSIENT FIELDS
	
	protected transient String backgroundFileName = "background-0-dark.png";
	
	private transient boolean hideHand = false;
	
	private transient Gesture shuffleGesture = null;
	
	private transient boolean changed = false;
	
	private transient Canvas canvas;
	
	private transient CardList battlefieldCards = new CardList();
	private transient CardList normalCards = new CardList();
	private transient CardList draggedCards = new CardList();
	
	private transient DieList battlefieldDice = new DieList();
	private transient DieList normalDice = new DieList();
	private transient DieList draggedDice = new DieList();
	
	private transient List<Card> pendingCards;
	
	private transient boolean opponentView = true;
	
	private transient ZoneManager cardZoneManager;
	private transient ZoneManager dieZoneManager;
	
	private transient List<CardLayer> syncedLayers = new ArrayList<CardLayer>();
	
	public transient CardLayer opponentLayer = null;
	
	private transient boolean initialized;
	
	private transient List<Card> cardsToInitialize;
	
	private transient CardList originalCards;
	
	private transient boolean newGame;
	
	private transient PlayerCardLayerButtonPanel playerButtonPanel;

	private transient OpponentCardLayerButtonPanel opponentButtonPanel;
	
	public CardLayer(Canvas canvas, RenderableList<Card> cards, boolean enableListeners) {
		init(canvas, cards, enableListeners);
	}
	
	private void init(Canvas canvas, RenderableList<Card> cards, boolean enableListeners) {
		this.canvas = canvas;
		
		originalCards = new CardList(cards.getCopy());
		
		allObjects = new RenderableList<IRenderable>();
		allCards = new CardList();

		d10s = new DieList();
		counters = new DieList();
		tokens = new DieList();
		
		cardsToInitialize = cards;
		initialized = false;
		
		if(handler == null) {
			opponentView = !enableListeners;
			handler = new RenderableHandler(handlerManager, this);
			handlerManager.add(handler);
		}
		
		cardZoneManager = new ZoneManager();
		dieZoneManager = new ZoneManager();
		
		dieZoneManager.removeZone(ZoneType.EXILE);
		dieZoneManager.getZone(ZoneType.DECK).getRenderer().setSnapAnchor(ZoneRenderer.ANCHOR_BOTTOM);
		
		shuffleGesture = buildShuffleGesture();
		
		zIndex = 0;
		screenW = 0;
		screenH = 0;
		
		loadingCard = null;
		loading = null; // XXX Hard to serialize

		changed = false;
		
		battlefieldCards = new CardList();
		normalCards = new CardList();
		draggedCards = new CardList();
		
		battlefieldDice = new DieList();
		normalDice = new DieList();
		draggedDice = new DieList();
		
		pendingCards = null;
		
		initialized = false;

		setLightView(Session.getInstance().getPreferences().isLightView());
		setShowCardCounts(Session.getInstance().getPreferences().isShowCardCounts());

		if(playerButtonPanel == null) {
			initButtonPanel();
			initOpponentButtonPanel();
		}
		
		adjustCameraForOpponent();
	}
	
	public void adjustCameraForOpponent() {
		if(this.opponentLayer != null) {
			//getCanvas().getZoom().setZoomLevel(ZoomAndPanListener.DEFAULT_MIN_ZOOM_LEVEL + 1);
		}
	}
	
	private void initDice(boolean hasCommander) {
		if(opponentView) {
			return;
		}
		
		Die die0 = new D10(Die.DEFAULT_LIFE_COLOR, hasCommander ? 4 : 2);
		d10s.add(die0);
		
		Die die1 = new D10(Die.DEFAULT_LIFE_COLOR, 0);
		d10s.add(die1);
		
		Die die2 = new D10(Color.WHITE, 0);
		d10s.add(die2);
		
		Die counter = new Counter(Color.WHITE, 0);
		counters.add(counter);
		
		Die token = new Token(Color.WHITE, 10);
		token.getRenderer().setScale(0.1);
		tokens.add(token);
		
		updateZoneBounds();
		
		moveToDeck(die0);
		moveToDeck(die1);
		
		moveToGraveyard(die2);
		moveToGraveyard(counter);
		moveToGraveyard(token);
		
		if(hasCommander) {
			Die cDie0 = new D10(Die.DEFAULT_COMMANDER_COLOR, 2);
			d10s.add(cDie0);
			moveToDeck(cDie0);	
			
			Die cDie1 = new D10(Die.DEFAULT_COMMANDER_COLOR, 1);
			d10s.add(cDie1);
			moveToDeck(cDie1);
			
			Die commanderCounter = new Counter(Die.DEFAULT_COMMANDER_COLOR, 0);
			counters.add(commanderCounter);
			moveToCommander(commanderCounter);
		}
		
		handleMoved(d10s, false, false);
		handleMoved(counters, false, false);
		handleMoved(tokens, false, false);
		
		allObjects.addAll(tokens);
		allObjects.addAll(d10s);
		allObjects.addAll(counters);
	}
	
	private void moveToGraveyard(Die die) {
		die.getRenderer().setZoneType(ZoneType.BATTLEFIELD);
		die.getRenderer().rememberLastZoneType();
		die.getRenderer().setZoneType(ZoneType.GRAVEYARD);
	}
	
	private void moveToCommander(Die die) {
		die.getRenderer().setZoneType(ZoneType.BATTLEFIELD);
		die.getRenderer().rememberLastZoneType();
		die.getRenderer().setZoneType(ZoneType.COMMANDER);
	}
	
	private void moveToDeck(Die die) {
		die.getRenderer().setZoneType(ZoneType.BATTLEFIELD);
		die.getRenderer().rememberLastZoneType();
		die.getRenderer().setZoneType(ZoneType.DECK);
		dieZoneManager.getZone(ZoneType.DECK).getRenderer().bottom(this, die, false, 10);
		dieZoneManager.getZone(ZoneType.DECK).add(die);
	}
	
	
	private void moveToZone(Card card, ZoneType zone) {
		moveToZone(card, zone, true);
	}
	
	private void moveToZone(Card card, ZoneType zone, boolean animate) {
		card.rememberLastZoneType();
		cardZoneManager.getZone(card.getZoneType()).remove(card);
		cardZoneManager.getZone(zone).add(card);
		card.setZoneType(zone);
		handleMoved(Arrays.asList(card), false, animate);
		repaint();
	}

	private void moveToZone(List<Card> cards, ZoneType zone, boolean animate, boolean isDragging) {
		for(Card card : cards) {
			card.rememberLastZoneType();
			cardZoneManager.getZone(card.getZoneType()).remove(card);
			cardZoneManager.getZone(zone).add(card);
			card.setZoneType(zone);
		}
		handleMoved(cards, isDragging, animate);
		repaint();
	}
	
	private void loadCards(final List<Card> newCards) {
		if(newCards.size() == 0) {
			updateZoneBounds();
			return;
		}
		
		boolean needsLoading = false;
		for(Card card : newCards) {
			String name = card.getName() + ":" + card.getSet();
			if(!loadedCardNames.contains(name)) {
				needsLoading = true;
				break;
			}
		}
		final boolean needsLoadingCard = needsLoading;
		
		Animator cardLoader = new Animator<Card>(canvas, newCards) {

			@Override
			public boolean update(Card card, int step) {
				card.getSetCardInfo();
				String name = card.getName() + ":" + card.getSet();
				if(!loadedCardNames.contains(name)) {
					card.getImage(true, 0.7);
					loadedCardNames.add(name);
				}
				card.setLocation(new Location(0, 0));
				card.setZoneType(ZoneType.BATTLEFIELD);
				card.rememberLastZoneType();
				if(card.isCommander()) {
					card.setZoneType(ZoneType.COMMANDER);
				} else {
					card.setZoneType(ZoneType.DECK);
				}
				flagChange();
				return true;
			}
			
			@Override
			public void startUpdate() {
				if(needsLoadingCard) {
					showLoadingCard();
				}
			}
			
			@Override
			public void stopUpdate() {
				setPendingCards(newCards);
				if(needsLoadingCard) {
					hideLoadingCard();
				}
				flagChange();
				canvas.repaint();
			}
			
		};
		cardLoader.setMaxSteps(1);
		cardLoader.start();
		
	}
	
	public void createCard(String cardName) {
		Card card = new Card(cardName);
		if(card.getCardInfo() == null) {
			return;
		}
		card.getRenderer().setScreenX(screenW - (int) card.getBounds().getBounds().getWidth() - 10);
		card.getRenderer().setScreenY(10);
		card.setZoneType(ZoneType.BATTLEFIELD);
		if(hasPendingCards()) {
			pendingCards.add(card);
		} else {
			setPendingCards(Arrays.asList(card));
		}
		repaint();
	}
	
	private void setPendingCards(List<Card> cards) {
		pendingCards = cards;
	}
	
	private boolean hasPendingCards() {
		return pendingCards != null;
	}
	
	private List<Card> pullPendingCards() {
		if(!hasPendingCards()) {
			return null;
		}
		List<Card> cards = pendingCards;
		pendingCards = null;
		return cards;
	}
	
	private void showLoadingCard() {
		if(isLoading()) {
			return;
		}
		
		loadingCard = new Card("Jandor's Saddlebags");
		loadingCard.setFaceUp(false);
		loadingCard.setScreenX(screenW/2 - 111);
		loadingCard.setScreenY(screenH/2 - 155);
		
		loading = new Animator<Card>(canvas, loadingCard) {

			@Override
			public boolean update(Card card, int step) {
				card.incrementAngle(1);
				flagChange();
				return false;
			}
			
		};
		loading.start();
	}
	
	private void hideLoadingCard() {
		if(!isLoading()) {
			return;
		}
		
		loading.stop();
		loading = null;
		loadingCard = null;
	}
	
	private boolean isLoading() {
		return loading != null;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	protected BufferedImage getBackground() {
		backgroundFileName = BACKGROUND_FILENAME;
		if(backgroundFileName == null) {
			return null;
		}
		return ImageUtil.readImage(ImageUtil.getResourceUrl(backgroundFileName), backgroundFileName);
	}
	
	@Override
	public void paintComponent(Graphics2D g, int width, int height) {
		update(width, height);
		/*if(opponentView) {
			if(screenW <= 0 && screenH <= 0) {
				screenW = width;
				screenH = height;
				//paintOpponentViewBanner(g, width, height);
				return;
			}
			g.setClip(0, 0, screenW, screenH);
		}*/
		
		determineCardsAndDiceDrawOrder(g, width, height);
		
		clearZIndex();
		getCanvas().getZoom().revert(g);
		if(!opponentView) {
			paintBackground(g, screenW, screenH);
		}
		paintBattlefield(g, screenW, screenH);
		getCanvas().getZoom().transform(g);
		paintBattlefieldCardsAndDice(g, screenW, screenH);
		getCanvas().getZoom().revert(g);
		paintZones(g, screenW, screenH);
		getCanvas().getZoom().transform(g);
		paintNormalCardsAndDice(g, screenW, screenH);
		paintDraggedCardsAndDice(g, screenW, screenH);
		getCanvas().getZoom().revert(g);
		paintDrag(g, screenW, screenH);
		paintLoading(g, screenW, screenH);
		getCanvas().getZoom().transform(g);
		
		if(opponentView) {// && height > screenH) {
			paintOpponentViewBanner(g, width, height);
		} 
		
		/*else if(opponentLayer != null) {
			opponentLayer.paintComponent(g, width, height);
		}*/
	}
	
	private void paintOpponentViewBanner(Graphics g, int width, int height) {
		/*g.setClip(0, 0, width, height);
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("Helvetica", Font.BOLD, 65));
		Rectangle2D bounds = g.getFontMetrics().getStringBounds("OPPONENT VIEW", g);
		g.drawString("OPPONENT VIEW", screenW / 2 - 274, screenH + 65);*/
		
		g.setClip(0, 0, width, height);
		g.setColor(new Color(255,0,0,100));
		g.fillRect(0, 0, width, 50);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Helvetica", Font.BOLD, 20));
		String s = currentUsername == null ? "Watching: Opponent" : "Watching: " + currentUsername;
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, screenW - ((int) bounds.getWidth()) - 20, 30);
	}
	
	protected void paintBackground(Graphics g, int width, int height) {
		BufferedImage img = getBackground();
		if(img == null) {
			return;
		}
		g.setColor(DEFAULT_BACKGROUND_COLOR);
		g.fillRect(0, 0, width, height);
		g.drawImage(img, 0, 0, null);
		
	}
	
	private void paintDrag(Graphics2D g, int width, int height) {
		if(!handler.isDragging()) {
			return;
		}
		
		Location dragStart = handler.getDragStart();
		Location dragEnd = handler.getDragEnd();
		
		if(handler.getDragMode() == DRAG_MODE_SELECT && dragEnd != null) {
			g.setColor(COLOR_SELECT);
			g.setStroke(new BasicStroke(STROKE_SELECT));
			int x = dragStart.getScreenX();
			int y = dragStart.getScreenY();
			int targetX = dragEnd.getScreenX();
			int targetY = dragEnd.getScreenY();
			
			int w = Math.abs(x - targetX);
			int h = Math.abs(y - targetY); 

			g.drawRect(Math.min(x, targetX), Math.min(y, targetY), w, h);
		}

	}
	
	private void determineCardsAndDiceDrawOrder(Graphics2D g, int width, int height) {
		battlefieldCards.clear();
		normalCards.clear();
		draggedCards.clear();
		
		battlefieldDice.clear();
		normalDice.clear();
		draggedDice.clear();
		
		for(int i = getAllCards().size() - 1; i >= 0; i--) {
			Card c = getAllCards().get(i);
			if(handler.isDragged(c)) {
				draggedCards.add(c);
				if(c.hasChildren()) {
					for(IRenderer child : c.getChildren()) {
						draggedDice.add((Die) child.getObject());
					}
				}
			} else if(c.getZoneType() == ZoneType.BATTLEFIELD) {
				battlefieldCards.add(c);
			} else {
				normalCards.add(c);
			}
		}
		
		for(int i = tokens.size() - 1; i >= 0; i--) {
			Die d = tokens.get(i);
			if(handler.isDragged(d)) {
				draggedDice.add(d);
				if(d.getRenderer().hasChildren()) {
					for(IRenderer child : d.getRenderer().getChildren()) {
						draggedDice.add((Die) child.getObject());
					}
				}
			} else if(d.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
				battlefieldDice.add(d);
			} else {
				normalDice.add(d);
			}
		}
		
		for(int i = d10s.size() - 1; i >= 0; i--) {
			Die d = d10s.get(i);
			if(handler.isDragged(d)) {
				draggedDice.add(d);
			} else if(d.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
				battlefieldDice.add(d);
			} else {
				normalDice.add(d);
			}
		}
		
		for(int i = counters.size() - 1; i >= 0; i--) {
			Die d = counters.get(i);
			if(handler.isDragged(d)) {
				draggedDice.add(d);
			} else if(d.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
				battlefieldDice.add(d);
			} else {
				normalDice.add(d);
			}
		}
		
	}
	
	private void paintBattlefieldCardsAndDice(Graphics2D g, int width, int height) {
		paintOpponentCardsAndDice(g, width, height);
		
		for(Card c : battlefieldCards) {
			paintCard(g, width, height, c);
		}
		
		for(Die d : battlefieldDice) {
			paintDie(g, width, height, d);
		}
		
	}
	
	private void paintOpponentCardsAndDice(Graphics2D g, int width, int height) {
		if(opponentView || opponentLayer == null) {
			return;
		}
		
		g.rotate(Math.PI, width/2, height/2);
		g.translate(0, height/2);
		boolean hasRenderables = false;
		for(IRenderable r : opponentLayer.getAllObjects()) {
			if(r.getRenderer().getZoneType() != ZoneType.BATTLEFIELD || !r.getRenderer().isTransformedProjection()) {
				continue;
			}
			if(r instanceof Card) {
				hasRenderables = true;
				paintCard(g, width, height, (Card) r);
			} else if(r instanceof Die) {
				hasRenderables = true;
				paintDie(g, width, height, (Die) r);
			}
		}
		g.translate(0, -height/2);
		g.rotate(-Math.PI, width/2, height/2);
		
		if(!hasRenderables) {
			getCanvas().getZoom().revert(g);
			g.setColor(Color.RED);
			Font f = g.getFont();
			g.setFont(f.deriveFont(30f));
			String s = "Opponent has no cards on the battlefield";
			g.drawString(s, (width - g.getFontMetrics(g.getFont()).stringWidth(s))/2, 50);
			g.setFont(f);
			getCanvas().getZoom().transform(g);
		}
		
	}
	
	private void paintNormalCardsAndDice(Graphics2D g, int width, int height) {
		for(Card c : normalCards) {
			paintCard(g, width, height, c);
		}
		
		for(Die d : normalDice) {
			paintDie(g, width, height, d);
		}
	}
	
	private void paintDraggedCardsAndDice(Graphics2D g, int width, int height) {
		for(Card c : draggedCards) {
			paintCard(g, width, height, c);
		}
		
		for(Die d : draggedDice) {
			paintDie(g, width, height, d);
		}
	}
	
	private void paintCard(Graphics2D g, int width, int height, Card card) {
		boolean revert = !card.isTransformedProjection();
		if(revert) {
			getCanvas().getZoom().revert(g);
		}
		card.paintComponent(this, g, width, height);
		if(handler.isSelected(card)) {
			paintSelected(g, card);
		}
		if(revert) {
			getCanvas().getZoom().transform(g);
		}
	}
	
	private void paintSelected(Graphics2D g, IRenderable renderable) {
		g.setColor(COLOR_SELECT);
		g.setStroke(new BasicStroke(STROKE_SELECT));
		g.draw(renderable.getRenderer().getBounds());
	}
	
	protected void paintBattlefield(Graphics2D g, int width, int height) {
		for(Zone zone : cardZoneManager.getZones()) {
			cardZoneManager.getZone(ZoneType.BATTLEFIELD).getRenderer().paintComponent(this, g, width, height);
		}
	}
	
	protected void paintZones(Graphics2D g, int width, int height) {
		for(Zone zone : cardZoneManager.getZones()) {
			if(zone.getType() == ZoneType.BATTLEFIELD) {
				continue;
			}
			zone.getRenderer().paintComponent(this, g, width, height);
		}
	}
	
	private void paintLoading(Graphics2D g, int width, int height) {
		if(!isLoading()) {
			return;
		}
		paintCard(g, width, height, loadingCard);
		g.setColor(Color.DARK_GRAY);
		g.setFont(new Font("Helvetica", Font.BOLD, 40));
		g.drawString("Loading Deck...", screenW/2 - 150, screenH/2 - 250);
	}
	
	/*private void paintDice(Graphics2D g, int width, int height) {
		for(int i = tokens.size() - 1; i >= 0; i--) {
			Die d = tokens.get(i);
			if(handler.isDragged(d)) {
				dicePaintSecond.add(d);
				if(d.getRenderer().hasChildren()) {
					for(IRenderer child : d.getRenderer().getChildren()) {
						dicePaintSecond.add((Die) child.getObject());
					}
				}
			} else {
				paintDie(g, width, height, d);
			}
		}
		
		for(int i = d10s.size() - 1; i >= 0; i--) {
			Die d = d10s.get(i);
			if(handler.isDragged(d)) {
				dicePaintSecond.add(d);
			} else {
				paintDie(g, width, height, d);
			}
		}
		
		for(int i = counters.size() - 1; i >= 0; i--) {
			Die d = counters.get(i);
			if(handler.isDragged(d)) {
				dicePaintSecond.add(d);
			} else {
				paintDie(g, width, height, d);
			}
		}
		
	}*/
	
	private void paintDie(Graphics2D g, int width, int height, Die die) {
		boolean revert = !die.getRenderer().isTransformedProjection();
		if(revert) {
			getCanvas().getZoom().revert(g);
		}
		die.getRenderer().paintComponent(this, g, width, height);
		if(handler.isSelected(die)) {
			paintSelected(g, die);
		} 
		if(revert) {
			getCanvas().getZoom().transform(g);
		}
	}
	
	protected void update(int width, int height) {
		/*Location dim = getCanvas().getZoom().inverseTransform(width, height);
		width = dim.getScreenX();
		height = dim.getScreenY();*/
		
		int lastScreenW = screenW;
		int lastScreenH = screenH;
		//if(!opponentView) {
			screenW = width;
			screenH = height;
		//}
		
		if(hasPendingCards()) {
			List<Card> cards = pullPendingCards();
			allCards.addAll(cards);
			allObjects.clear();
			allObjects.addAll(allCards);
			allObjects.addAll(tokens);
			allObjects.addAll(d10s);
			allObjects.addAll(counters);
			updateZoneBounds();
			handleMoved(getAllCards(), false, false);
			
			if(newGame) {
				newGame = false;
				shuffleDeckAndDrawStartingHand();
			}
		} else {
			updateZoneBounds();
		}
		
		boolean screenSizeChanged = screenW != lastScreenW || screenH != lastScreenH;
		
		if(screenSizeChanged) {
			int dx = screenW - lastScreenW;
			int dy = screenH - lastScreenH;
			
			for(Card card : getAllCards()) {
				if(dx != 0) {
					if(card.getRenderer().getZoneType() == ZoneType.GRAVEYARD || card.getRenderer().getZoneType() == ZoneType.COMMANDER) {
						card.getRenderer().setScreenX(Math.max(0, card.getRenderer().getScreenX() + dx));
					} else if(card.getRenderer().getZoneType() == ZoneType.HAND) {
						card.getRenderer().setScreenX(Math.max(0, card.getRenderer().getScreenX() + dx/2));
					}
				}
				if(dy != 0) {
					card.getRenderer().setScreenY(Math.max(0, card.getRenderer().getScreenY() + dy));
				}
			}
		
			for(Die die : d10s) {
				if(dx != 0 && (die.getRenderer().getZoneType() == ZoneType.GRAVEYARD || die.getRenderer().getZoneType() == ZoneType.COMMANDER)) {
					die.getRenderer().setScreenX(Math.max(0, die.getRenderer().getScreenX() + dx));
				}
				if(dy != 0) {
					die.getRenderer().setScreenY(Math.max(0, die.getRenderer().getScreenY() + dy));
				}
			}
		
			for(Die die : counters) {
				if(dx != 0 && (die.getRenderer().getZoneType() == ZoneType.GRAVEYARD || die.getRenderer().getZoneType() == ZoneType.COMMANDER)) {
					die.getRenderer().setScreenX(Math.max(0, die.getRenderer().getScreenX() + dx));
				}
				if(dy != 0) {
					die.getRenderer().setScreenY(Math.max(0, die.getRenderer().getScreenY() + dy));
				}
			}
			
			for(Die die : tokens) {
				if(dx != 0 && (die.getRenderer().getZoneType() == ZoneType.GRAVEYARD || die.getRenderer().getZoneType() == ZoneType.COMMANDER)) {
					die.getRenderer().setScreenX(Math.max(0, die.getRenderer().getScreenX() + dx));
				}
				if(dy != 0) {
					die.getRenderer().setScreenY(Math.max(0, die.getRenderer().getScreenY() + dy));
				}
			}
		
		}
		
		screenSizeChanged = false;
		
		boolean isDragging = handler.isDragging();
		cardZoneManager.setZones(getCanvas(), this, isDragging, getAllCards());
		dieZoneManager.setZones(getCanvas(), this, isDragging, d10s);
		dieZoneManager.setZones(getCanvas(), this, isDragging, counters, false);
		dieZoneManager.setZones(getCanvas(), this, isDragging, tokens, false);
		
		if(isDragging) {
			handleMoved(allObjects, isDragging);
		}
		
		synchronize();
		
		if(!initialized) {
			initialized = true;
			newGame = true;
			
			boolean hasCommander = false;
			for(Card c : cardsToInitialize) {
				if(c.isCommander()) {
					hasCommander = true;
					break;
				}
			}
			if(!hasCommander && cardZoneManager.getZone(ZoneType.COMMANDER) != null) {
				cardZoneManager.removeZone(ZoneType.COMMANDER);
			} else if(hasCommander && cardZoneManager.getZone(ZoneType.COMMANDER) == null) {
				cardZoneManager.addZone(ZoneType.COMMANDER);
			}
			
			initDice(hasCommander);
			loadCards(cardsToInitialize);
			cardsToInitialize = null;
		}
	}

	@Override
	public void repaint() {
		canvas.repaint();
	}
	
	public void handleDoubleLeftClick(IRenderable r) {
		if(!(r instanceof Card)) {
			return;
		}

		if(r.getRenderer().getZoneType() == ZoneType.DECK) {
			getMenuBar().actionSearchDeck();
		} else if(r.getRenderer().getZoneType() == ZoneType.GRAVEYARD) {
			getMenuBar().actionSearchGraveyard();
		} else if(r.getRenderer().getZoneType() == ZoneType.EXILE) {
			getMenuBar().actionSearchExile();
		}
	}
	
	public void handleMoved(List objects, boolean isDragging) {
		handleMoved(objects, isDragging, true);
	}
	
	private void handleMoved(List objects, boolean isDragging, boolean animate) {
		Die lastD10 = null;
		Die lastCounter = null;
		Die lastToken = null;
		boolean hasD10 = false;
		boolean hasCounter = false;
		boolean hasToken = false;
		
		for(Object obj : objects) {
			if(obj instanceof Card) {
				handleMoved((Card) obj, isDragging, animate);
			} else {
				if(((Die) obj).getRenderer().getLastZoneType() == ZoneType.GRAVEYARD) {
					if(obj instanceof D10) {
						lastD10 = (Die) obj;
						if(!isDragging && lastD10.getRenderer().getZoneType() == ZoneType.HAND) {
							hasD10 = true;
						}
					} else if(obj instanceof Counter) {
						lastCounter = (Die) obj;
						if(!isDragging && lastCounter.getRenderer().getZoneType() == ZoneType.HAND) {
							hasCounter = true;
						}
					} else if(obj instanceof Token) {
						lastToken = (Die) obj;
						if(!isDragging && lastToken.getRenderer().getZoneType() == ZoneType.HAND) {
							hasToken = true;
						}
					}
				}
				handleMoved((Die) obj, isDragging, animate);
			}
		}
		for(Zone zone : cardZoneManager.getZones()) {
			if(animate && zone.getRenderer().isShouldFan()) {
				zone.getRenderer().fan(this, zone, animate);
			}
		}
		for(Zone zone : dieZoneManager.getZones()) {
			if(animate && zone.getRenderer().isShouldFan()) {
				zone.getRenderer().fan(this, zone, animate);
			}
		}
		
		// Remove redundant dice
		if(!opponentView && !isDragging) {
			List<Die> diceToRemove = new ArrayList<Die>();
			
			for(Object die : dieZoneManager.getZone(ZoneType.GRAVEYARD)) {
				if(!hasD10 && die instanceof D10) {
					hasD10 = true;
				} else if(!hasCounter && die instanceof Counter) {
					hasCounter = true;
				} else if(!hasToken && die instanceof Token) {
					hasToken = true;
				} else if(hasD10 && die instanceof D10) {
					diceToRemove.add((Die) die);
				} else if(hasCounter && die instanceof Counter) {
					diceToRemove.add((Die) die);
				} else if(hasToken && die instanceof Token) {
					diceToRemove.add((Die) die);
				}
			}
			for(Die die : diceToRemove) {
				remove(die);
				dieZoneManager.getZone(ZoneType.GRAVEYARD).remove(die);
			}
			
			if(lastD10 != null && !hasD10) {
				Die die = new D10(lastD10.getColor(), lastD10.getValue());
				d10s.add(die);
				allObjects.add(die);
				moveToGraveyard(die);
				handleMoved(die, false, false);
			}
			
			if(lastCounter != null && !hasCounter) {
				Die die = new Counter(lastCounter.getColor(), lastCounter.getValue());
				counters.add(die);
				allObjects.add(die);
				moveToGraveyard(die);
				handleMoved(die, false, false);
			}
			
			if(lastToken != null && !hasToken) {
				Die die = new Token(lastToken.getColor(), lastToken.getValue());
				die.getRenderer().setScale(0.1);
				tokens.add(die);
				allObjects.add(die);
				moveToGraveyard(die);
				handleMoved(die, false, false);
			}
		}
		
		/*if(!isDragging && objects.size() > 0) {
			flagChange();
		}*/
		
		canvas.repaint();
	}
	
	private Die findDie(Location location) {
		for(Die d : d10s) {
			if(d.getRenderer().overlaps(location)) {
				return d;
			}
		}
		return null;
	}
	
	private Die findDie(MouseEvent e) {
		return findDie(new Location(e));
	}
	
	public synchronized void revalidate() {
		update(screenW, screenH);
		canvas.revalidate();
	}
	
	public void clear() {
		clear(false);
	}
	
	public void clear(boolean ignoreChange) {
		getAllCards().clear();
		handler.clear();
		if(!ignoreChange) {
			repaint();
		}
	}
	
	public void reset() {
		CardList cards = new CardList(originalCards);
		cards.shuffle();
		init(canvas, cards, !opponentView);
	}
	
	public void shuffleDeckAndDrawStartingHand() {
		List<Card> cardsToShuffle = new ArrayList<Card>();
		List<Card> cardsToDraw = new ArrayList<Card>();
		
		boolean hasCommander = false;
		allCards.shuffle();
		int maxHandSize = 7;
		int handSize = 0;
		for(Card card : allCards) {
 			card.setTapped(false);
			if(card.isCommander()) {
				hasCommander = true;
				card.setFaceUp(true);
				moveToZone(card, ZoneType.COMMANDER);
			} else {
				if(handSize < maxHandSize) {
					card.setFaceUp(true);
					cardsToDraw.add(card);
					handSize++;
				} else {
					card.setFaceUp(false);
					cardsToShuffle.add(card);
				}
			}
			card.setTransformedProjection(false);
		}
		
		if(!hasCommander && cardZoneManager.getZone(ZoneType.COMMANDER) != null) {
			cardZoneManager.removeZone(ZoneType.COMMANDER);
		} else if(hasCommander && cardZoneManager.getZone(ZoneType.COMMANDER) == null) {
			cardZoneManager.addZone(ZoneType.COMMANDER);
		}
		
		shuffleCards(cardsToShuffle, false, true);
		
		cardZoneManager.getZone(ZoneType.HAND).clear();
		moveToZone(cardsToDraw, ZoneType.HAND, false, true);
		cardZoneManager.getZone(ZoneType.HAND).getRenderer().fan(this, cardsToDraw, true);
		
		cardZoneManager.getZone(ZoneType.DECK).clear();
		moveToZone(cardsToShuffle, ZoneType.DECK, false, true);
		
		flagChange();
	}
	
	public CardLayer copy() {
		CardLayer d = new CardLayer(canvas, this.getAllCards(), opponentView);
		return d;
	}
	
	public void setFromCardLayer(CardLayer c) {
		clear(true);
		allCards.set(c.allCards.getCopy());
		revalidate();
	}
	
	private List<Card> copyCards(CardLayer layer) {
		List<Card> copies = new ArrayList<Card>();
		for(Card c : getAllCards()) {
			Card copy = c.copyRenderable();
			copies.add(copy);
		}
		return copies;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CardLayer)) {
			return false;
		}
		
		CardLayer d = (CardLayer) obj;
		return areCardsEquals(d);
	}

	private boolean safeEquals(Object obj1, Object obj2) {
		if(obj1 == null && obj2 == null) {
			return true;
		}
		if(obj1 == null || obj2 == null) {
			return false;
		}
		return obj1.equals(obj2);
	}
	
	private boolean areCardsEquals(CardLayer d) {
		if(getAllCards().size() != d.getAllCards().size()) {
			return false;
		}
		
		for(Card c : getAllCards()) {
			if(!d.getAllCards().contains(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	public void flagChange() {
		changed = true;
		//view.recordChange();
		//refreshTitle();
		if(!isOpponentView()) {
			//System.out.println("Active layer change detected. " + System.currentTimeMillis());
			CardLayer.activeCardLayer = this;
		}
	}
	
	public void clearChange() {
		changed = false;
		refreshTitle();
	}
	
	private void refreshTitle() {
		//view.getFrame().refreshTitle();
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	// Animation methods
	
	public void rotate(int angle) {
		if(!handler.isDragging() && handler.getSelected().size() == 0) {
			return;
		}
		
		final boolean right = angle >= 9;
		angle = ShapeUtil.toPositiveAngle(Math.abs(angle));
		
		Animator ca = new Animator<Card>(canvas, handler.isDragging() ? handler.getDraggedCards() : handler.getSelectedCards(), angle) {

			@Override
			public boolean update(Card card, int step) {
				if(right) {
					card.incrementAngle(1);
				} else {
					card.decrementAngle(1);
				}
				flagChange();
				return false;
			}

		};
		ca.start();
		
	}
	
	public void spin(Card card) {
		spin(card, 0, false);
	}
	
	public void spin(Card card, final int targetAngle) {
		spin(Arrays.asList(card), targetAngle);
	}
	
	public void spin(List<Card> cards, final int targetAngle) {
		spin(cards, targetAngle, true, 5);
	}
	
	public void spin(Card card, final int targetAngle, final boolean clockwise) {
		spin(Arrays.asList(card), targetAngle, clockwise, 5);
	}
	
	public void spin(List<Card> cards, final int targetAngle, final boolean clockwise, int deltaAngle) {
		boolean same = true;
		for(Card card : cards) {
			if(card.getAngle() != targetAngle) {
				same = false;
			}
		}
		if(same) {
			return;
		}
		
		Animator ca = new SpinAnimator(canvas, cards, targetAngle, clockwise, deltaAngle, 0);
		ca.start();
	}
	
	// Gesture Methods
	
	private Gesture buildShuffleGesture() {
		Gesture gesture = new ShakeGesture() {

			@Override
			public void performAction() {
				if(handler.isDragging()) {
					shuffleCards(handler.getSelectedCards(), false, true);
					List<Die> dice = handler.getDraggedDice();
					for(Die die : dice) {
						if(die instanceof D10) {
							die.roll();
						} else if(die instanceof Counter) {
							die.roll(1,2);
						}
					}
				}
			}
			
		};
		
		return gesture;
	}

	public void shuffleCards(List<Card> cardsToShuffle, boolean forceIntoDeck, boolean animate) {
		// Do nothing for empty card list
		CardList cardList = new CardList(cardsToShuffle);
		if(cardList.size() == 0) {
			return;
		}
		
		// Get all cards by the last zone they were in
		Map<ZoneType, List<Card>> cardsByZoneType = new HashMap<ZoneType, List<Card>>();
		for(Card c : cardList) {
			ZoneType zt = c.getLastZoneType();
			if(zt == null && c.getZoneType() == ZoneType.DECK) {
				zt = ZoneType.DECK;
			}
			if(!cardsByZoneType.containsKey(zt)) {
				cardsByZoneType.put(zt, new ArrayList<Card>());
			}
			cardsByZoneType.get(zt).add(c);
		}
		
		// See if any card is in the deck based on zone
		boolean hasDeck = cardsByZoneType.containsKey(ZoneType.DECK) || forceIntoDeck;
		
		// If there was a card in the deck, everything gets moved to the deck
		if(hasDeck) {
			for(Card c : cardList) {
				c.setZoneType(ZoneType.DECK);
			}
			
			// Spin the cards
			if(animate) {
				spin(cardList, 360, true, 10);
			}
			
			// Stop drag, snapping them to the deck
			if(!forceIntoDeck) {
				handler.stopDrag(ZoneType.DECK);
			}
			
			// Clear out all selected
			handler.clearSelected();
		
		// If we don't have a deck, stop dragging these and clear the selection
		} else {
		
			// Stop drag, snapping them to the zone they're in
			handler.stopDrag();

			// Clear out all selected
			handler.clearSelected();
		}
		
		if(forceIntoDeck) {
			handleMoved(cardList, false, animate);
		}
		
		// Now re-zone the cards based on their new zone
		cardsByZoneType = new HashMap<ZoneType, List<Card>>();
		for(Card c : cardList) {
			ZoneType zt = c.getZoneType();
			if(!cardsByZoneType.containsKey(zt)) {
				cardsByZoneType.put(zt, new ArrayList<Card>());
			}
			cardsByZoneType.get(zt).add(c);
		}
		
		// Shuffle each zone
		for(ZoneType type : cardsByZoneType.keySet()) {
			List<Card> cards = cardsByZoneType.get(type);
			
			// Shuffle deck by reordering them in the stack
			if(type == ZoneType.DECK) {
				CardList cList = new CardList(cards);
				ShuffleUtil.shuffle(ShuffleType.PLAYER, cList);
				for(Card c : cList) {
					allCards.move(c, 0);
				}
				
			// Shuffle all other zones by swapping their positions
			} else {
				ShuffleUtil.positionShuffle(cards);
				
				// Fan out battlefield cards
				if(type == ZoneType.BATTLEFIELD) {
					cardZoneManager.getZone(ZoneType.BATTLEFIELD).getRenderer().fan(CardLayer.this, cards, true);
				}
			}
			
		}
	}

	public void remove(IRenderable renderable) {
		if(renderable == null) {
			return;
		}
		
		if(renderable instanceof Card) {
			allCards.remove((Card) renderable);
		} else if(renderable instanceof D10) {
			d10s.remove(renderable);
		} else if(renderable instanceof Counter) {
			counters.remove(renderable);
		} else if(renderable instanceof Token) {
			tokens.remove(renderable);
		}
		
		if(renderable instanceof Die) {
			((Die) renderable).getRenderer().removeFromParent();
		}
		
		if(allObjects.contains(renderable)) {
			allObjects.remove(renderable);
		}
	}
	
	public void move(IRenderable renderable, int index) {
		if(renderable == null) {
			return;
		}
		if(renderable instanceof Card) {
			allCards.move((Card) renderable, index);
		} else if(renderable instanceof D10) {
			d10s.move((Die) renderable, index);
		} else if(renderable instanceof Counter) {
			counters.move((Die) renderable, index);
		} else if(renderable instanceof Token) {
			tokens.move((Die) renderable, index);
		}
	}
	
	// Zone Methods
	
	public void updateZoneBounds() {
		int zw = (int) Math.round(300 * ImageUtil.getScale());
		int zh = (int) Math.round(350 * ImageUtil.getScale());
		for(ZoneType type : cardZoneManager.getZoneTypes()) {
			updateZoneBounds(cardZoneManager.getZone(type), zw, zh);
		}
		for(ZoneType type : dieZoneManager.getZoneTypes()) {
			updateZoneBounds(dieZoneManager.getZone(type), zw, zh);
		}
	}
	
	private void updateZoneBounds(Zone zone, int zw, int zh) {
		switch(zone.getType()) {
			case DECK:
				zone.setLocation(new Location(0, screenH - zh));
				zone.setWidth(zw);
				zone.setHeight(zh);
				break;
			case GRAVEYARD:
				zone.setLocation(new Location(screenW - zw, screenH - zh));
				zone.setWidth(zw);
				zone.setHeight(zh);
				break;
			case HAND:
				zone.setLocation(new Location(zw, screenH - zh + 200));
				zone.setWidth(screenW - zw - zw);
				zone.setHeight(zh);
				break;
			case BATTLEFIELD:
				zone.setLocation(new Location(0, 0));
				zone.setWidth(screenW);
				zone.setHeight(screenH - zh);
				break;
			case EXILE:
				zone.setLocation(new Location(0, 0));
				zone.setWidth(zw/2);
				zone.setHeight(zh/2);
				break;
			case COMMANDER:
				zone.setLocation(new Location(screenW - zw/2, 0));
				zone.setWidth(zw/2);
				zone.setHeight(zh/2);
				break;
			default:
				break;
		}
		zone.getRenderer().recomputeBounds(true);
	}
	
	private void handleObjectMoved(Card card, boolean isDragging) {
		handleMoved(card, isDragging, true);
	}
	
	public void handleMoved(Card card, boolean isDragging, boolean animate) {
		ZoneType type = card.getZoneType();
		Zone zone = cardZoneManager.getZone(type);
		boolean zoneChanged = (!isDragging && card.hasChangedZones()) || (card.hasPendingZoneChange());
	
		switch(type) {
			case DECK:
				if(zoneChanged) {
					card.setFaceUp(false);
					card.setTapped(false);
					card.setScale(0.5);//0.7);
					card.restoreAngle();
				}
				if(!isDragging) {
					zone.getRenderer().center(this, card, animate);
				}
				break;
			case GRAVEYARD:
				if(zoneChanged) {
					if(!isDragging || !isHideHand()) {
						card.setFaceUp(true);
					}
					card.setTapped(false);
					card.setScale(0.5);//0.7);
					card.setAngle(180 + ShuffleUtil.randInt(-10, 10));
				}
				if(!isDragging) {
					zone.getRenderer().center(this, card, animate);
				}
				break;
			case HAND:
				if(zoneChanged) {
					if(!isDragging || !isHideHand()) {
						card.setFaceUp(true);
					}
					card.setTapped(false);
					card.restoreScale();
					card.restoreAngle();
				}
				if(!isDragging) {
					zone.getRenderer().setShouldFan(true);
				}
				break;
			case BATTLEFIELD:
				if(zoneChanged) {
					/*if((!isDragging || !isHideHand()) && card.getLastZoneType() != ZoneType.DECK && card.getLastZoneType() != ZoneType.EXILE) {
						card.setFaceUp(true);
					}*/
					//card.setScale(0.7);
					card.restoreScale();
					card.restoreAngle();
				}
				break;
			case EXILE:
				if(zoneChanged) {
					if(!isDragging || !isHideHand()) {
						card.setFaceUp(false);
					}
					card.setTapped(false);
					card.setScale(0.2);
					card.setAngle(ShuffleUtil.randInt(-10, 10));
					//card.restoreAngle();
				}
				if(!isDragging) {
					zone.getRenderer().center(this, card, animate);
				}
				break;
			case COMMANDER:
				if(zoneChanged) {
					if(!isDragging || !isHideHand()) {
						card.setFaceUp(true);
					}
					card.setTapped(false);
					card.setScale(0.2);
					card.setAngle(0);
				}
				if(!isDragging) {
					zone.getRenderer().center(this, card, animate);
				}
				break;
			default:
				break;
		}
		
		if(zoneChanged) {
			card.recomputeBounds();
		}
		
		if(!isDragging) {
			if(zoneChanged && card.getLastZoneType() == ZoneType.HAND) {
				cardZoneManager.getZone(ZoneType.HAND).getRenderer().setShouldFan(true);
			}
			if(card.getZoneType() != ZoneType.BATTLEFIELD) {
				List<IRenderer> children = new ArrayList<IRenderer>(card.getChildren());
				card.removeChildren();
				List<IRenderable> childrenObjects = new ArrayList<IRenderable>();
				for(IRenderer child : children) {
					childrenObjects.add((IRenderable) child.getObject());
				}
				handleMoved(childrenObjects, false, animate);
			}
			card.forgetLastZoneType();
		} else {
			card.clearPendingFlagZoneChange();
		}
		
	}
	
	private void handleObjectMoved(Die die, boolean isDragging) {
		handleMoved(die, isDragging, true);
	}
	
	private void handleMoved(Die dieObject, boolean isDragging, boolean animate) {
		IRenderer<Die> die = dieObject.getRenderer();
		ZoneType type = die.getZoneType();
		Zone zone = dieZoneManager.getZone(type);
		boolean zoneChanged = (!isDragging && die.hasChangedZones()) || (die.hasPendingZoneChange());
		
		switch(type) {
			case DECK:
				if(zoneChanged) {
					die.recomputeBounds();
					if(dieObject instanceof Token) {
						die.setScale(0.1);
					} else {
						die.restoreScale();
					}
					die.restoreAngle();
				}
				if(!isDragging) {
					if(dieObject instanceof Token) {
						die.setZoneType(ZoneType.GRAVEYARD);
						dieZoneManager.getZone(ZoneType.DECK).remove(die);
						dieZoneManager.getZone(ZoneType.GRAVEYARD).add(die);
						dieZoneManager.getZone(ZoneType.GRAVEYARD).getRenderer().bottomLeft(this, dieObject, animate, 60, 10);
					} else {
						zone.getRenderer().setShouldFan(true);
					}
				}
				break;
			case GRAVEYARD:
				if(zoneChanged) {
					die.recomputeBounds();
					if(dieObject instanceof Token) {
						die.setScale(0.1);
					} else {
						die.restoreScale();
					}
					die.restoreAngle();
				}
				if(!isDragging) {
					if(dieObject instanceof D10) {
						zone.getRenderer().bottomLeft(this, dieObject, animate, 25, 40);
					} else if(dieObject instanceof Counter) {
						zone.getRenderer().bottomLeft(this, dieObject, animate, 5, 10);
					} else if(dieObject instanceof Token) {
						die.setTapped(false);
						zone.getRenderer().bottomLeft(this, dieObject, animate, 60, 10);
					}
				}
				break;
			case HAND:
				if(zoneChanged) {
					die.recomputeBounds();
					if(dieObject instanceof Token) {
						die.setScale(0.1);
					} else {
						die.restoreScale();
					}
					die.restoreAngle();
				}
				if(!isDragging) {
					die.setZoneType(ZoneType.GRAVEYARD);
					dieZoneManager.getZone(ZoneType.HAND).remove(die);
					dieZoneManager.getZone(ZoneType.GRAVEYARD).add(die);
					if(dieObject instanceof D10) {
						dieZoneManager.getZone(ZoneType.GRAVEYARD).getRenderer().bottomLeft(this, dieObject, animate, 25, 40);
					} else if(dieObject instanceof Counter) {
						dieZoneManager.getZone(ZoneType.GRAVEYARD).getRenderer().bottomLeft(this, dieObject, animate, 5, 10);
					} else if(dieObject instanceof Token) {
						dieZoneManager.getZone(ZoneType.GRAVEYARD).getRenderer().bottomLeft(this, dieObject, animate, 60, 10);
					}
				}
				break;
			case BATTLEFIELD:
				if(zoneChanged) {
					die.recomputeBounds();
					if(dieObject instanceof Token) {
						die.setScale(0.7);
					} else {	
						die.restoreScale();
					}
					die.restoreAngle();
				}
				break;
			case COMMANDER:
				if(zoneChanged) {
					die.recomputeBounds();
					if(dieObject instanceof Token) {
						die.setScale(0.1);
					} else {
						die.restoreScale();
					}
					die.restoreAngle();
				}
				if(!isDragging) {
					if(dieObject instanceof Token) {
						die.setZoneType(ZoneType.GRAVEYARD);
						dieZoneManager.getZone(ZoneType.DECK).remove(die);
						dieZoneManager.getZone(ZoneType.GRAVEYARD).add(die);
						dieZoneManager.getZone(ZoneType.GRAVEYARD).getRenderer().bottomLeft(this, dieObject, animate, 60, 10);
					} else {
						zone.getRenderer().bottom(this, dieObject, animate, 125);
					}
				}
				break;
			default:
				break;
		}
		
		if(!isDragging) {
			if(zoneChanged && die.getLastZoneType() == ZoneType.DECK) {
				dieZoneManager.getZone(ZoneType.DECK).getRenderer().setShouldFan(true);
			}
			if(dieObject instanceof Token && die.getZoneType() != ZoneType.BATTLEFIELD) {
				List<IRenderer> children = new ArrayList<IRenderer>(die.getChildren());
				die.removeChildren();
				List<IRenderable> childrenObjects = new ArrayList<IRenderable>();
				for(IRenderer child : children) {
					childrenObjects.add((IRenderable) child.getObject());
				}
				handleMoved(childrenObjects, false, true);
			}
			die.forgetLastZoneType();
		} else {
			die.clearPendingFlagZoneChange();
		}
		
	}
	
	public ZoneManager getCardZoneManager() {
		return cardZoneManager;
	}
	
	public ZoneManager getDieZoneManager() {
		return dieZoneManager;
	}
	
	public boolean isHideHand() {
		return hideHand;
	}

	public void setHideHand(boolean hideHand) {
		this.hideHand = hideHand;
	}
	
	public void syncLayer(CardLayer layer) {
		if(layer == null || layer == this) {
			return;
		}
		
		if(!syncedLayers.contains(layer)) {
			syncedLayers.add(layer);
		}
		if(!layer.syncedLayers.contains(this)) {
			layer.syncedLayers.add(this);
		}
	}
	
	public int getScreenWidth() {
		return screenW;
	}
	
	public int getScreenHeight() {
		return screenH;
	}
	
	public Gesture getShuffleGesture() {
		return shuffleGesture;
	}
	
	public RenderableHandler getHandler() {
		return handler;
	}
	
	public DieList getD10s() {
		return d10s;
	}
	
	public DieList getCounters() {
		return counters;
	}
	
	public DieList getTokens() {
		return tokens;
	}
	
	public List<CardLayer> getSyncedLayers() {
		return syncedLayers;
	}
	
	public void synchronize() {
		if(opponentView || syncedLayers.size() == 0) {
			return;
		}
		for(CardLayer layer : syncedLayers) {
			layer.setFromCardLayerShallowCopy(this);
		}
	}
	
	public void setFromCardLayerShallowCopy(CardLayer layer) {
		allObjects = layer.allObjects;
		allCards = layer.allCards;
		d10s = layer.d10s;
		counters = layer.counters;
		tokens = layer.tokens;
		handler = layer.handler;
		handlerManager = layer.handlerManager;
		loading = layer.loading;
		loadingCard = layer.loadingCard;
		screenW = layer.screenW;
		screenH = layer.screenH;
		currentUsername = layer.currentUsername;
		repaint();
	}
	
	public void unsyncLayers() {
		
	}
	
	public CardList getAllCards() {
		return allCards;
	}
	
	public RenderableList<IRenderable> getAllObjects() {
		return allObjects;
	}
	
	@Override
	public List getListeners() {
		List listeners = new ArrayList();
		//if(!opponentView) {
			listeners.add(handlerManager);
		//}
		return listeners;
	}

	public boolean isOpponentView() {
		return opponentView;
	}
	
	public void setOpponentView(boolean opponentView) {
		this.opponentView = opponentView;
	}

	@Override
	public void handleClosed() {
		if(opponentView) {
			Iterator<CardLayer> it = syncedLayers.iterator();
			while(it.hasNext()) {
				CardLayer layer = it.next();
				layer.syncedLayers.remove(this);
				it.remove();
				
				//JandorTabFrame frame = JUtil.getFrame(layer.getCanvas());
				//PMenuBar menu = (PMenuBar) frame.getJMenuBar();
				//menu.setShareScreen(false);
			}
		} else {
			Iterator<CardLayer> it = syncedLayers.iterator();
			while(it.hasNext()) {
				CardLayer layer = it.next();
				layer.syncedLayers.remove(this);
				JandorTabFrame shareFrame = JUtil.getFrame(layer.getCanvas());
				
				PPanel p = new PPanel();
				p.setOpaque(true);
				p.setBackground(ColorUtil.DARK_GRAY_1);
				JLabel l = new JLabel("Please select a Board Tab and click \"Share Screen.\"");
				l.setFont(l.getFont().deriveFont(20f));
				p.addc(l);
				
				shareFrame.setContentPane(p);
				shareFrame.revalidate();
				shareFrame.repaint();
				//JUtil.getFrame(layer.getCanvas()).close();
			}
		}
	}
	
	public JandorMenuBar getMenuBar() {
		JandorTabFrame frame = JUtil.getFrame(getCanvas());
		JandorMenuBar menu = (JandorMenuBar) frame.getJMenuBar();
		return menu;
	}

	public String getCurrentUsername() {
		return currentUsername;
	}

	public void setCurrentUsername(String currentUsername) {
		this.currentUsername = currentUsername;
	}
		
	public int getZIndex() {
		return zIndex;
	}
	
	public int nextZIndex() {
		return zIndex;
	}
	
	public void clearZIndex() {
		zIndex = 0;
	}
	
	public String getSerializedRenderablesString() {
		return SerializationUtil.toString(allObjects.getShallowCopySortedByZIndex());
	}
	
	public byte[] getSerializedRenderablesBytes() {
		RenderableList<IRenderable> renderableList = allObjects.getShallowCopySortedByZIndex();
		renderableList.screenW = screenW;
		renderableList.screenH = screenH;
		return SerializationUtil.toBytes(renderableList);
	}
	
	private static final RenderableList<IRenderable> empty = new RenderableList<IRenderable>();
	public RenderableList<IRenderable> getExtraRenderables() {
		return empty;
	}
	
	public void setOpponentLayer(CardLayer opponentLayer) {
		this.opponentLayer = opponentLayer;
	}
	
	private void initButtonPanel() {
		playerButtonPanel = new PlayerCardLayerButtonPanel(this);
	}
	
	public PlayerCardLayerButtonPanel getButtonPanel() {
		return playerButtonPanel;
	}
	
	private void initOpponentButtonPanel() {
		opponentButtonPanel = new OpponentCardLayerButtonPanel(this);
	}
	
	public OpponentCardLayerButtonPanel getOpponentButtonPanel() {
		return opponentButtonPanel;
	}
	
	public void untap() {
		boolean untapped = false;
		for(IRenderable r : getAllObjects()) {
			if(r.getRenderer().getZoneType() == ZoneType.BATTLEFIELD && r.getRenderer().isTapped()) {
				r.getRenderer().setTapped(false);
				untapped = true;
			}
		}
		if(untapped) {
			repaint();
		}
	}
	
	public void draw() {
		Zone deck = getCardZoneManager().getZone(ZoneType.DECK);
		if(deck.size() > 0) {
			moveToZone((Card) deck.get(0), ZoneType.HAND);
			repaint();
		}
	}
	
	public void shuffle() {
		shuffleCards(getCardZoneManager().getZone(ZoneType.DECK), false, true);
	}
	
	public void discardRandom() {
		Zone hand = getCardZoneManager().getZone(ZoneType.HAND);
		if(hand.size() > 0) {
			moveToZone((Card) hand.get(ShuffleUtil.randInt(hand.size()-1)), ZoneType.GRAVEYARD);
			repaint();
		}
	}
	
}