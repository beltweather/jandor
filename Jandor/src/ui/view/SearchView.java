package ui.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import accordion.PAccordion;
import accordion.PAccordionData;
import accordion.PAccordionPanel;
import deck.Card;
import deck.Deck;
import search.CardSearchFakeTable;
import search.CardSearchPanel;
import search.PageHandler;
import search.SearchPanel;
import search.SortHandler;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PTableModel;
import util.ImageUtil;
import util.ShuffleType;
import util.ShuffleUtil;

public class SearchView extends JandorView {

	public static final String DEFAULT_TITLE = "Search";

	public static void addSearchView(PAccordion accordion) {
		addSearchView(accordion, null);
	}

	public static void addSearchView(PAccordion accordion, DeckEditorView parent) {
		if(parent != null) {
			parent.getAccordionData().getAccordionPanel().contractChildren();
		}

		final PAccordionData searchViewData = new PAccordionData("Search");
		final PAccordionData searchResultData = new PAccordionData("Results");

		SearchView searchView = new SearchView("Search", false) {

			@Override
			public void handleResults(Deck results) {
				super.handleResults(results);
				if(results.size() == 0) {
					searchResultData.getAccordionPanel().contract();
					searchViewData.getAccordionPanel().expand();
				} else {
					searchResultData.getAccordionPanel().expand();
					searchViewData.getAccordionPanel().contract();
				}
			}

		};

		searchViewData.setComponent(searchView);
		searchViewData.setDefaultExpanded(true);
		searchViewData.setRemoveable(true);
		searchResultData.setComponent(searchView.getSearchTable());
		searchResultData.setFooterComponent(searchView.getPageFooter());
		searchResultData.setDefaultExpanded(false);
		searchResultData.setRemoveable(false);

		if(parent != null) {
			searchView.getSearchTable().setAddToDeck(true);
			parent.linkView(searchView);
		}

		accordion.add(searchViewData, parent == null? null : parent.getAccordionData());
		accordion.add(searchResultData, searchViewData);
	}

	protected CardSearchPanel searchPanel;
	protected CardSearchFakeTable searchTable;
	protected PageHandler<Card> pageHandler;
	protected SortHandler sortHandler;
	protected PPanel pageFooter;
	protected PPanel optionsPanel;
	protected PCheckBox searchDeck;
	protected PCheckBox searchSideboard;
	protected boolean defaultSearchDeck = true;
	protected boolean defaultSearchSideboard = true;

	private boolean enableResultsListener = true;

	public SearchView(boolean embedded) {
		this(DEFAULT_TITLE, embedded);
	}

	public SearchView(String name, boolean embedded) {
		super(name, true);
		rebuild();
	}

	@Override
	public void handleClosed() {
		for(JandorView view : getLinkedViews()) {
			if(view instanceof DeckEditorView) {
				((DeckEditorView) view).setFilterByDeckAndSideboard(null);
			}
		}
	}

	@Override
	protected void rebuild() {
		removeAll();

		final PPanel pageHandlerPanel = new PPanel();

		searchTable = new CardSearchFakeTable() {

			@Override
			public void addCard(Card card, boolean sideboard) {
				for(JandorView view : getLinkedViews()) {
					view.addCard(card, sideboard);
				}
			}

		};
		final PTableModel model = searchTable.getModel();

		searchPanel = new CardSearchPanel() {

			@Override
			protected void handleResults(Deck results) {
				if(results == null) {
					results = new Deck();
				}
				ShuffleUtil.shuffle(sortHandler.getShuffleType(), results);
				pageHandler = new PageHandler<Card>(results) {

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
					SearchView.this.handleResults(results);
				}
			}

		};

		optionsPanel = new PPanel();
		searchDeck = new PCheckBox("Search Deck", defaultSearchDeck);
		searchDeck.addChangeListener((e) -> { defaultSearchDeck = searchDeck.isSelected(); });

		searchSideboard = new PCheckBox("Search Sideboard", defaultSearchSideboard);
		searchSideboard.addChangeListener((e) -> { defaultSearchSideboard = searchSideboard.isSelected(); });

		optionsPanel.weaken();
		optionsPanel.addc(searchDeck);
		optionsPanel.right();
		optionsPanel.addc(searchSideboard);

		sortHandler = new SortHandler("Sort Results by") {

			@Override
			protected void handleSort(ShuffleType type) {
				searchPanel.search();
			}

		};
		sortHandler.setVisible(false);

		if(pageFooter == null) {
			pageFooter = new PPanel();
		} else {
			pageFooter.removeAll();
			c.reset();
		}
		pageFooter.strengthen();
		pageFooter.add(sortHandler, pageFooter.c);
		pageFooter.right();
		pageFooter.add(pageHandlerPanel, pageFooter.c);

		PButton addAllToDeck = new PButton("+ All Deck");
		addAllToDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int size = pageHandler.getAllItems().size();
				if(size > 1000) {
					JUtil.showWarningDialog(null, "Cannot Add All Cards to Deck", "This search contains too many cards to add all at once. Trying adding 1000 or less cards at a time.");
					return;
				}
				if(JUtil.showConfirmDialog(null, "Add All Cards to Deck", new JLabel("Add all " + size + " cards to your deck?"))) {
					for(Card card : pageHandler.getAllItems()) {
						for(JandorView view : getLinkedViews()) {
							Deck deck = view.getDeck();
							deck.add(card);
						}
					}
					for(JandorView view : getLinkedViews()) {
						if(view instanceof DeckEditorView) {
							((DeckEditorView) view).rebuildDeckRows();
						} else {
							view.redraw();
						}
						view.flagModified();
					}
				}
			}

		});

		PButton addAllToSideboard = new PButton("+ All Sideboard");
		addAllToSideboard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int size = pageHandler.getAllItems().size();
				if(size > 1000) {
					JUtil.showWarningDialog(null, "Cannot Add All Cards to Sideboard", "This search contains too many cards to add all at once. Trying adding 1000 or less cards at a time.");
					return;
				}
				if(JUtil.showConfirmDialog(null, "Add All Cards to Sideboard", new JLabel("Add all " + pageHandler.getAllItems().size() + " cards to your sideboard?"))) {
					for(Card card : pageHandler.getAllItems()) {
						for(JandorView view : getLinkedViews()) {
							Deck deck = view.getDeck();
							if(deck.hasSideboard()) {
								deck.getSideboard().add(card);
							}
						}
					}
					for(JandorView view : getLinkedViews()) {
						if(view instanceof DeckEditorView) {
							((DeckEditorView) view).rebuildDeckRows();
						} else {
							view.redraw();
						}
						view.flagModified();
					}
				}
			}

		});

		pageFooter.weaken();
		pageFooter.right();
		pageFooter.addc(addAllToDeck);
		pageFooter.right();
		pageFooter.c.insets(0,5);
		pageFooter.addc(addAllToSideboard);

		PPanel p = new PPanel();
		p.weaken();
		p.addc(searchPanel);
		p.down();
		p.addc(optionsPanel);
		p.down();
		p.fill();

		strengthen();
		addc(p);

		enableResultsListener = false;
		searchPanel.clearSearch();
		enableResultsListener = true;
	}

	public PPanel getPageFooter() {
		return pageFooter;
	}

	public SearchPanel getSearchPanel() {
		return searchPanel;
	}

	public CardSearchFakeTable getSearchTable() {
		return searchTable;
	}

	/**
	 * Override this method as a listener for when results change
	 */
	public void handleResults(Deck results) {
		if(searchDeck.isSelected() && searchSideboard.isSelected()) {
			for(JandorView view : getLinkedViews()) {
				if(view instanceof DeckEditorView) {
					((DeckEditorView) view).setFilterByDeckAndSideboard(results);
				}
			}
		} else if(searchDeck.isSelected()) {
			for(JandorView view : getLinkedViews()) {
				if(view instanceof DeckEditorView) {
					((DeckEditorView) view).setFilterByDeck(results);
				}
			}
		} else if(searchSideboard.isSelected()) {
			for(JandorView view : getLinkedViews()) {
				if(view instanceof DeckEditorView) {
					((DeckEditorView) view).setFilterBySideboard(results);
				}
			}
		}
	}

	@Override
	public void reset() {
		for(JandorView view : getLinkedViews()) {
			if(view instanceof DeckEditorView) {
				((DeckEditorView) view).setFilterBySideboard(null);
			}
		}
	}

}