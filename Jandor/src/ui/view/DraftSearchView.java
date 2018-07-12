package ui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import search.CardSearchFakeTable;
import search.PageHandler;
import search.SortHandler;
import session.BoosterContent;
import session.BoosterHeader;
import session.DraftContent;
import session.DraftHeader;
import session.Session;
import session.User;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import ui.pwidget.PTableModel;
import util.DraftUtil;
import util.IDUtil;
import util.ImageUtil;
import util.MailUtil;
import util.ShuffleType;
import util.ShuffleUtil;
import util.UserUtil;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;
import accordion.PAccordion;
import accordion.PAccordionData;
import accordion.PAccordionPanel;
import deck.Card;
import deck.Deck;
import draft.CustomBoosterBuilder;
import draft.IBoosterBuilder;
import draft.SetBoosterBuilder;

public class DraftSearchView extends JandorView {
	
	public static final String DEFAULT_TITLE = "Draft Cards";
	
	public static void addDraftSearchView(int draftId, PAccordion accordion) {
		addDraftSearchView(draftId, accordion, null);
	}
	
	public static void addDraftSearchView(int draftId, PAccordion accordion, DraftEditorView parent) {
		if(parent != null) {
			parent.getAccordionData().getAccordionPanel().contractChildren();
		}
		
		final PAccordionData searchResultData = new PAccordionData(DEFAULT_TITLE);
		
		DraftSearchView draftSearchView = new DraftSearchView(DEFAULT_TITLE, draftId);
		
		searchResultData.setComponent(draftSearchView);
		searchResultData.setFooterComponent(draftSearchView.getPageFooter());
		searchResultData.setDefaultExpanded(true);
		searchResultData.setRemoveable(false);
		
		if(parent != null) {
			draftSearchView.getSearchTable().setAddToDeck(true);
			parent.linkView(draftSearchView);
		}
			
		accordion.add(searchResultData, parent == null? null : parent.getAccordionData());
	}
	
	protected CardSearchFakeTable searchTable;
	protected PageHandler<Card> pageHandler;
	protected SortHandler sortHandler;
	protected PPanel pageFooter;
	protected Deck deck;
	protected PPanel waitingPanel;
	
	protected int draftId;
	protected int boosterId;
	
	private boolean enableResultsListener = true;
	
	public DraftSearchView(int draftId) {
		this(DEFAULT_TITLE, draftId);
	}
	
	public DraftSearchView(String name, int draftId) {
		super(name, true);
		this.draftId = draftId;
		rebuild();
		
		SessionEventManager.addListener(DraftHeader.class, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				// New booster event
			}
			
		});
		
		SessionEventManager.addListener(BoosterHeader.class, SessionEvent.TYPE_ALERT, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				handleNewBooster();
			}
			
		});
	}

	@Override
	public void handleClosed() {
		SessionEventManager.removeListeners(this);
	}

	@Override
	protected void rebuild() {
		removeAll();
		
		final PPanel pageHandlerPanel = new PPanel();
		
		searchTable = new CardSearchFakeTable(true) {

			@Override
			public void addCard(Card card, boolean sideboard) {
				if(JUtil.showConfirmDialog(null, "Draft Card \"" + card.getName() + "\"", "Draft \"" + card.getName() + "\" to your deck? This selection is final.")) {
					for(JandorView view : getLinkedViews()) {
						view.addCard(card, sideboard);
					}
					deck.remove(card);
					
					DraftHeader draftHeader = getDraftHeader();
					int oldBoosterId = boosterId;
					draftHeader.cardSelected();
					draftHeader.save();
					
					BoosterHeader boosterHeader = Session.getInstance().getBoosterHeader(oldBoosterId);
					boosterHeader.setRound(draftHeader.getRound());
					boosterHeader.setTurn(draftHeader.getTurn());
					BoosterContent boosterContent = Session.getInstance().getBoosterContent(oldBoosterId);
					boosterContent.setFromDeck(deck);
					boosterHeader.save();
					boosterContent.save();	
					
					// Hide cards in search view
					deck = new Deck("Empty");
					updateDeckView();
					
					if(draftHeader.isFinished()) {
						waitingPanel.setVisible(false);
						JUtil.showMessageDialog(this, "Finished Draft", "The draft is finished!");
					} else if(draftHeader.needsToCreateBooster()) {
						JUtil.showMessageDialog(this, "Opening Next Booster", "About to open a brand new booster.");
						IBoosterBuilder bb = draftHeader.getType() == DraftHeader.TYPE_SET_BOOSTER ? new SetBoosterBuilder(draftId) : new CustomBoosterBuilder(draftId);
						int boosterId = bb.buildBooster();
						if(boosterId != IDUtil.NONE) {
							setBooster(boosterId);
						}
					} else {
						MailUtil.sendBoosterToDrive(draftHeader.getReceiverUser(), oldBoosterId);
						handleNewBooster();
					}
					
				}
			}
			
		};
		final PTableModel model = searchTable.getModel();
		
		sortHandler = new SortHandler(ShuffleType.RARITY_HL, "Sort Results by") {

			@Override
			protected void handleSort(ShuffleType type) {
				ShuffleUtil.shuffle(sortHandler.getShuffleType(), deck);
				updateDeckView();
			}
			
		};
		sortHandler.setVisible(false);
		
		DraftHeader draftHeader = getDraftHeader();
		if(draftHeader != null && draftHeader.needsToCreateBooster()) {
			IBoosterBuilder bb = draftHeader.getType() == DraftHeader.TYPE_SET_BOOSTER ? new SetBoosterBuilder(draftId) : new CustomBoosterBuilder(draftId);
			int boosterId = bb.buildBooster();
			if(boosterId != IDUtil.NONE) {
				setBooster(boosterId, false);
			}
		}
		
		ShuffleUtil.shuffle(sortHandler.getShuffleType(), deck);
		pageHandler = new PageHandler<Card>(deck) {

			@Override
			protected void handlePageChange(List<Card> pageItems) {
				List<Card> cardsToCache = new ArrayList<Card>(pageItems);
				int idx = 0;
				for(Card card : pageItems) {
					if(card.canTransform()) {
						if(idx >= cardsToCache.size()) {
							idx = cardsToCache.size() - 1;
						}
						idx++;
						cardsToCache.add(idx, card.getTransformCard());
					}
					idx++;
				}
				ImageUtil.cacheImageInBackground(cardsToCache, 0.4, searchTable);
				model.setData(new Deck(pageItems));
				searchTable.rebuild();
				
				revalidate();
				PAccordionPanel panel = JUtil.getAccordionPanel(getSearchTable());
				if(panel != null) {
					panel.getScrollPane().getVerticalScrollBar().setValue(0);
				}
				sortHandler.setVisible(pageItems.size() > 0);
			}
			
		};
		
		pageHandlerPanel.removeAll();
		pageHandlerPanel.add(pageHandler);
		pageHandler.triggerChange();
		
		if(enableResultsListener) {
			DraftSearchView.this.handleResults(deck);
		}
			
		if(pageFooter == null) {
			pageFooter = new PPanel();
		} else {
			pageFooter.removeAll();
			c.reset();
		}
		pageFooter.c.strengthen();
		pageFooter.c.insets(0, 10);
		pageFooter.add(sortHandler, pageFooter.c);
		pageFooter.c.gridx++;
		pageFooter.c.insets();
		pageFooter.addc(pageHandlerPanel);
		
		waitingPanel = new PPanel();
		waitingPanel.c.weaken();
		waitingPanel.addc(new JLabel("Waiting for next booster pack."));
		waitingPanel.c.gridy++;
		JProgressBar waitingBar = new JProgressBar();
		waitingBar.setIndeterminate(true);
		
		PButton refreshButton = new PButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleNewBooster();
			}
			
		});
		
		waitingPanel.addc(waitingBar);
		waitingPanel.c.gridy++;
		waitingPanel.c.insets(10);
		waitingPanel.addc(refreshButton);
		waitingPanel.c.strengthen();
		waitingPanel.c.gridy++;
		waitingPanel.addcStrut();
		waitingPanel.setVisible(false);
		
		c.strengthen();
		add(searchTable, c);
		add(waitingPanel,c);
		
		enableResultsListener = false;
		enableResultsListener = true;
		
		revalidate();
		repaint();
	}
	
	protected void handleNewBooster() {
		int boosterId = DraftUtil.getCurrentBooster(draftId);
		if(boosterId != IDUtil.NONE) {
			BoosterHeader boosterHeader = Session.getInstance().getBoosterHeader(boosterId);
			JUtil.showMessageDialog(this, "Booster Received", "Received booster from " + boosterHeader.getAuthorFormatted() + ".");
			setBooster(boosterId);
		}
	}
	
	public void setBooster(int boosterId) {
		setBooster(boosterId, true);
	}
	
	public void setBooster(int boosterId, boolean updateDeckView) {
		this.boosterId = boosterId;
		BoosterHeader boosterHeader = Session.getInstance().getBoosterHeader(boosterId);
		BoosterContent boosterContent = Session.getInstance().getBoosterContent(boosterId);
		Deck booster = new Deck(boosterHeader, boosterContent);
		deck = booster;
		if(updateDeckView) {
			updateDeckView();
		}
	}
	
	protected void updateDeckView() {
		if(deck.size() == 0) {
			waitingPanel.setVisible(true);
			searchTable.setVisible(false);
		} else {
			waitingPanel.setVisible(false);
			searchTable.setVisible(true);
		}
		ShuffleUtil.shuffle(sortHandler.getShuffleType(), deck);
		pageHandler.setItems(deck);
		pageHandler.triggerChange();
	}
	
	public PPanel getPageFooter() {
		return pageFooter;
	}
	
	public CardSearchFakeTable getSearchTable() {
		return searchTable;
	}
	
	public DraftHeader getDraftHeader() {
		return Session.getInstance().getDraftHeader(draftId);
	}
	
	public DraftContent getDraftContent() {
		return Session.getInstance().getDraftContent(draftId);
	}
	
	/**
	 * Override this method as a listener for when results change
	 */
	public void handleResults(Deck results) {
		
	}
	
	@Override
	public void reset() {
		
	}

}