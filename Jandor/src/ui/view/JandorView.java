package ui.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ui.pwidget.CloseListener;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.pwidget.PPanel;
import util.ShuffleType;
import accordion.PAccordion;
import accordion.PAccordionData;
import deck.Card;
import deck.Deck;

public abstract class JandorView extends PPanel implements CloseListener {

	public static final String DEFAULT_TITLE = "Card View";

	protected Deck deck;
	protected ShuffleType shuffleType = ShuffleType.PLAYER;
	protected boolean enableListeners = true;
	protected String name;
	protected String openedFileName = null;
	protected boolean modified = false;
	protected PAccordionData accordionData = null;
	
	protected List<JandorView> linkedViews = new ArrayList<JandorView>();
	
	public JandorView(String name) {
		this(name, true);
	}
	
	public JandorView(String name, boolean enableListeners) {
		super();
		this.name = name;
		this.enableListeners = enableListeners;
		init();
	}
	
	private void init() {
		
	}
	
	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
		rebuild();
	}
	
	public void redraw() {
		rebuild();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean hasOpenedFile() {
		return openedFileName != null;
	}
	
	public String getOpenedFileName() {
		return openedFileName;
	}
	
	public String getSimpleOpenedFileName() {
		if(!hasOpenedFile()) {
			return null;
		}
		return new File(openedFileName).getName();
	}
	
	public void setOpenedFileName(String openedFileName) {
		this.openedFileName = openedFileName;
	}
	
	/**
	 * Override this to handle adding a card
	 */
	public void addCard(Card card, boolean sideboard) {
		if(sideboard) {
			if(deck.hasSideboard()) {
				deck.getSideboard().add(card);
			}
		} else {
			deck.add(card);
		}
		flagModified();
		redraw();
	}
	
	public void linkView(JandorView view) {
		if(!linkedViews.contains(view)) {
			linkedViews.add(view);
			if(!view.linkedViews.contains(this)) {
				view.linkedViews.add(this);
			}
		}
	}
	
	public boolean hasLinkedViews() {
		return linkedViews.size() > 0;
	}
	
	public List<JandorView> getLinkedViews() {
		return linkedViews;
	}
	
	protected abstract void rebuild();
	
	public abstract void reset();
	
	public boolean isModified() {
		return modified;
	}
	
	public void flagModified() {
		modified = true;
		handleModified(modified);
		for(JandorView view : linkedViews) {
			view.modified = true;
			view.handleModified(view.modified);
		}
		JUtil.getFrame(this).refreshTitle();
	}
	
	/**
	 * Override this to respond to modification
	 */
	public void handleModified(boolean isModified) {
		
	}
	
	public void clearModified() {
		modified = false;
		handleModified(modified);
		for(JandorView view : linkedViews) {
			view.modified = false;
			view.handleModified(view.modified);
		}
		JandorTabFrame frame = JUtil.getFrame(this);
		if(frame != null) {
			frame.refreshTitle();
		}
	}
	
	public boolean hasAccordionData() {
		return accordionData != null;
	}
	
	public PAccordionData getAccordionData() {
		return accordionData;
	}
	
	public void setAccordionData(PAccordionData accordionData) {
		this.accordionData = accordionData;
	}
	
	public PAccordion getAccordion() {
		if(hasAccordionData()) {
			return getAccordionData().getAccordionPanel().getAccordion();
		}
		return null;
	}
		
	public JandorTabFrame getFrame() {
		return JUtil.getFrame(this);
	}
	
}
