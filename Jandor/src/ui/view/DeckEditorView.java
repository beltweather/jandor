package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import accordion.PAccordion;
import accordion.PAccordionData;
import accordion.PAccordionPanel;
import analysis.SimPanel;
import deck.Card;
import deck.Deck;
import draft.ConfigureDraftDialog;
import editor.DeckEditorRow;
import editor.TagDialog;
import editor.TagLabel;
import run.Jandor;
import search.ManaPanel;
import search.SortHandler;
import session.DeckContent;
import session.DeckHeader;
import session.Session;
import session.Tag;
import ui.GlassPane;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import ui.pwidget.PTabPane;
import ui.pwidget.PTextField;
import util.CardUtil;
import util.DeckEncoder;
import util.IDUtil;
import util.ImageUtil;
import util.PriceUtil;
import util.ShuffleType;
import util.ShuffleUtil;
import util.TaskUtil;
import util.TimeUtil;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;

public class DeckEditorView extends JandorView {

	public static final String DEFAULT_TITLE = "Editor";

	private static boolean defaultShowPrice = false;
	private static boolean defaultShowFullCards = false;

	public static boolean isDefaultShowPrice() {
		return defaultShowPrice;
	}

	public static void setDefaultShowPrice(boolean show) {
		defaultShowPrice = show;
	}

	public static boolean isDefaultShowFullCards() {
		return defaultShowFullCards;
	}

	public static void setDefaultShowFullCards(boolean show) {
		defaultShowFullCards = show;
	}

	public static final List<String> types = new ArrayList<String>();
	static {
		types.add("Creature");
		types.add("Instant");
		types.add("Sorcery");
		types.add("Enchantment");
		types.add("Artifact");
		types.add("Planeswalker");
		types.add("Land");
	}

	public static String getUniqueName(String defaultName) {
		return getUniqueName(defaultName, false);
	}

	public static String getUniqueName(String defaultName, boolean wholeStepsOnly) {
		if(!Session.getInstance().hasDeckHeader(defaultName)) {
			return defaultName;
		}

		String baseName;
		int value;
		if(defaultName.matches("^.*\\d\\.\\d$")) {
			baseName = defaultName.substring(0, defaultName.length() - 3);
			value = Integer.valueOf(defaultName.charAt(baseName.length()) + "") + 1;
		} else if(defaultName.matches("^.*\\d\\.\\d\\.[a-z]$")) {
			baseName = defaultName.substring(0, defaultName.length() - 5);
			value = Integer.valueOf(defaultName.charAt(baseName.length()) + "") + 1;
		} else {
			baseName = defaultName + " ";
			value = 2;
		}
		defaultName = baseName + value + ".0";

		if(wholeStepsOnly) {
			while(Session.getInstance().hasDeckHeader(defaultName)) {
				defaultName = baseName + ++value + ".0";
			}
		} else {
			int letter = 98;
			while(Session.getInstance().hasDeckHeader(defaultName)) {
				defaultName = baseName + value + ".0." + (char) letter++;
			}
		}

		return defaultName;
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, CollectionEditorView parent) {
		return addDeckEditorView(accordion, IDUtil.NONE, parent);
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, int deckId, CollectionEditorView parent) {
		DeckEditorView deckEditorView = new DeckEditorView(deckId);
		return addDeckEditorView(accordion, deckEditorView, parent);
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, DeckEditorView deckEditorView, CollectionEditorView parent) {
		PAccordionData deckEditorData = new PAccordionData(deckEditorView.getName(), deckEditorView);
		deckEditorData.setHeaderComponent(deckEditorView.getPageHeader());
		deckEditorData.setFooterComponent(deckEditorView.getPageFooter());
		deckEditorData.setRemoveable(true);

		accordion.add(deckEditorData, parent == null ? null : parent.getAccordionData());

		return deckEditorView;
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, PAccordionData parent) {
		return addDeckEditorView(accordion, IDUtil.NONE, parent);
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, int deckId, PAccordionData parent) {
		DeckEditorView deckEditorView = new DeckEditorView(deckId);
		return addDeckEditorView(accordion, deckEditorView, parent);
	}

	public static DeckEditorView addDeckEditorView(PAccordion accordion, DeckEditorView deckEditorView, PAccordionData parent) {
		PAccordionData deckEditorData = new PAccordionData(deckEditorView.getName(), deckEditorView);
		deckEditorData.setHeaderComponent(deckEditorView.getPageHeader());
		deckEditorData.setFooterComponent(deckEditorView.getPageFooter());
		deckEditorData.setRemoveable(true);

		accordion.add(deckEditorData, parent);

		return deckEditorView;
	}

	protected Map<String, Map<String, JLabel>> typeLabels = new HashMap<String, Map<String, JLabel>>();
	protected Map<String, JLabel> totalLabels = new HashMap<String, JLabel>();
	protected JLabel priceLabel;
	protected SortHandler sortHandler;
	protected PPanel deckPanel;
	protected PPanel sideboardPanel;
	protected PPanel headerPanel;
	protected PPanel topInfoPanel;
	protected PPanel bottomInfoPanel;
	protected PButton saveButton;
	protected PButton renameButton;
	protected PButton playButton;
	protected PButton searchButton;
	protected PButton analButton;
	protected PButton tagsButton;
	protected PButton copyButton;
	protected PButton clearButton;
	protected PButton draftButton;
	protected PButton highlanderButton;
	protected PButton editDraftButton;
	protected PButton editDeckButton;
	protected PButton encodeButton;
	protected PCheckBox showPriceCheck;
	protected PCheckBox showFullCardsCheck;
	protected PPanel commanderPanel;
	protected DeckEditorRow commanderEditor;
	protected JLabel commanderLabel;
	protected Map<String, DeckEditorRow> addRowsByTitle = new HashMap<String, DeckEditorRow>();

	protected DeckHeader deckHeader;
	protected DeckContent deckContent;

	protected int deckId;
	protected boolean ignoreNextSave = false;
	protected boolean showPrice = isDefaultShowPrice();
	protected boolean showFullCards = isDefaultShowFullCards();

	protected Deck filterByDeck = null;
	protected Deck filterBySideboard = null;

	private static String toDeckName(int deckId) {
		DeckHeader header = Session.getInstance().getDeckHeader(deckId);
		if(header == null) {
			return "Untitled";
		}
		return header.getName();
	}

	public DeckEditorView() {
		this(IDUtil.NONE);
	}

	public DeckEditorView(int deckId) {
		super(toDeckName(deckId), true);
		this.deckId = deckId;
		initDeckHeader();
		initDeckContent();
		initDeck();
		updateCopies();
		rebuild();
		SessionEventManager.addListener(DeckHeader.class, deckId, SessionEvent.TYPE_DELETED, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				getAccordionData().getAccordionPanel().remove();
			}

		});

		SessionEventManager.addListener(DeckHeader.class, deckId, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				if(event.isType(SessionEvent.TYPE_DELETED)) {
					return;
				}

				if(ignoreNextSave) {
					ignoreNextSave = false;
					return;
				}

				if(DeckEditorView.this.deckId == IDUtil.NONE) {
					return;
				}

				deckHeader = Session.getInstance().getDeckHeader(DeckEditorView.this.deckId);
				deckContent = Session.getInstance().getDeckContent(DeckEditorView.this.deckId);

				updateCopies();
				initDeck();
				rebuild();
				clearModified();
			}

		});

	}

	public void initDeckHeader() {
		if(deckHeader == null) {
			DeckHeader header = Session.getInstance().getDeckHeader(deckId);
			if(header == null) {
				header = DeckHeader.createDefaultHeader();
				deckId = header.getId();
			}
			deckHeader = header;
		}
	}

	public void initDeckContent() {
		if(deckContent == null) {
			DeckContent content = Session.getInstance().getDeckContent(deckId);
			if(content == null) {
				content = DeckContent.createDefaultContent(deckId);
			}
			deckContent = content;
		}
	}

	public void initDeck() {
		deck = new Deck(deckHeader, deckContent);
	}

	public DeckHeader getDeckHeader() {
		return deckHeader;
	}

	public DeckContent getDeckContent() {
		return deckContent;
	}

	public boolean isShowPrice() {
		return showPrice;
	}

	public void setShowPrice(boolean showPrice) {
		this.showPrice = showPrice;
	}

	public boolean isShowFullCards() {
		return showFullCards;
	}

	public void setShowFullCards(boolean showFullCards) {
		this.showFullCards = showFullCards;
	}

	@Override
	public void handleClosed() {
		if(isModified() && JUtil.showConfirmYesNoDialog(this, "Closing Editor \"" + deckHeader.getName() + "\"", "Would you like to save your unsaved changes?")) {
			save();
		}
		SessionEventManager.removeListeners(this);
	}

	@Override
	protected void rebuild() {
		removeAll();
		c.reset();

		if(sortHandler == null) {
			sortHandler = new SortHandler(ShuffleType.MANA_LH, "Sort Deck by") {

				@Override
				protected void handleSort(ShuffleType type) {
					//rebuild();
					rebuildDeckRows();
				}

			};
		}

		topInfoPanel = new PPanel();
		bottomInfoPanel = new PPanel();
		deckPanel = new PPanel();
		sideboardPanel = new PPanel();
		rebuildInfoPanel();
		rebuildDeckRows();

		if(saveButton == null) {

			saveButton = new PButton("Save");
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}

			});
			saveButton.setEnabled(false);

			copyButton = new PButton("Copy");
			copyButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					copy();
				}

			});

			encodeButton = new PButton("Deck Code");
			encodeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					encode();
				}

			});

			editDraftButton = new PButton("Edit as Draft");
			editDraftButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					editAsDraft();
				}

			});

			highlanderButton = new PButton("To Highlander");
			highlanderButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					toHighlander();
				}

			});
			highlanderButton.setToolTipText("There can be only one!");

			showPriceCheck = new PCheckBox("Show Price");
			showPriceCheck.setSelected(isShowPrice());
			showPriceCheck.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setDefaultShowPrice(showPriceCheck.isSelected());
					setShowPrice(showPriceCheck.isSelected());
					syncPrices(true);
				}

			});

			showFullCardsCheck = new PCheckBox("Show Full Cards");
			showFullCardsCheck.setSelected(isShowFullCards());
			showFullCardsCheck.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setDefaultShowFullCards(showFullCardsCheck.isSelected());
					setShowFullCards(showFullCardsCheck.isSelected());
					rebuild();
				}

			});

			editDeckButton = new PButton("Edit as Deck");
			editDeckButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					editAsDeck();
				}

			});
			editDeckButton.setVisible(false);

			clearButton = new PButton("Clear");
			clearButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(JUtil.showConfirmDialog(null, "Clear All Cards in Deck", "Are you sure you want to remove all cards from this deck and sideboard?")) {
						clear();
					}
				}

			});

			renameButton = new PButton("Rename");
			renameButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rename();
				}

			});
			renameButton.setEnabled(true);

			tagsButton = new PButton("Edit Tags");
			tagsButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					editTags();
				}

			});
			tagsButton.setEnabled(true);

			searchButton = new PButton("Search");
			searchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					PAccordion accordion = getAccordion();
					SearchView.addSearchView(accordion, DeckEditorView.this);
					accordion.rebuild();
				}

			});
			searchButton.setEnabled(true);

			analButton = new PButton("Analyze");
			analButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(isModified()) {
						save();
					}

					PAccordion accordion = getAccordion();
					DeckAnalysisView.addAnalysisView(accordion, DeckEditorView.this);
					accordion.rebuild();
				}

			});
			analButton.setEnabled(true);

			draftButton = new PButton("Draft");
			draftButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					draft();
				}

			});
			draftButton.setEnabled(true);

			playButton = new PButton("Play");
			playButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(isModified()) {
						save();
					}

					JandorTabFrame frame = JUtil.getFrame(DeckEditorView.this);
					PTabPane tabPane = frame.getTabPane();

					BoardView boardView = new BoardView(BoardView.DEFAULT_TITLE);
					boardView.setDeckId(deckId);
					boardView.setDeck(getDeck().copyRenderable());
					tabPane.addTab(JandorTabFrame.toBoardTitle(DeckEditorView.this), boardView);
					tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
				}

			});
			playButton.setEnabled(true);

			headerPanel = new PPanel();
			headerPanel.c.strengthen();
			headerPanel.c.insets(0, 5);
			headerPanel.c.weightx = 0.01;
			headerPanel.c.anchor = G.NORTHEAST;
			headerPanel.addc(saveButton);
			headerPanel.c.gridx++;
			headerPanel.addc(copyButton);
			headerPanel.c.gridx++;
			headerPanel.addc(encodeButton);
			headerPanel.c.gridx++;
			headerPanel.addc(clearButton);
			headerPanel.c.gridx++;
			headerPanel.addc(editDraftButton);
			headerPanel.c.gridx++;
			headerPanel.addc(highlanderButton);
			headerPanel.c.gridx++;
			headerPanel.addc(showPriceCheck);
			headerPanel.c.gridx++;
			headerPanel.addc(showFullCardsCheck);
			headerPanel.c.gridx++;
			headerPanel.addc(editDeckButton);
			headerPanel.c.weightx = 1.0;
			headerPanel.c.gridx++;
			headerPanel.addc(Box.createHorizontalStrut(1));
			headerPanel.c.weightx = 0.01;
			headerPanel.c.gridx++;
			headerPanel.addc(draftButton);
			headerPanel.c.gridx++;
			headerPanel.addc(analButton);
			headerPanel.c.gridx++;
			headerPanel.addc(searchButton);
			headerPanel.c.gridx++;
			headerPanel.addc(playButton);
			headerPanel.c.insets(5);
			headerPanel.c.gridy++;
			headerPanel.addcStrut();

		}

		PPanel p = new PPanel();
		p.c.anchor = G.NORTHWEST;
		p.c.fill = G.VERTICAL;
		p.add(deckPanel, p.c);
		p.c.gridy++;
		p.c.weaken();
		p.c.fill = G.VERTICAL;
		p.c.gridy--;
		p.c.gridx++;
		p.c.insets(0, 0, 0, 30);
		p.add(sideboardPanel, p.c);

		c.anchor = G.CENTER;
		c.strengthen();
		c.insets(20, 20);
		c.gridwidth = 3;
		c.weighty = 0.01;
		c.insets(0, 20);
		c.weighty = 1.0;
		add(p, c);
		c.gridy++;
		c.weighty = 0.01;

		PPanel centeredPanel = new PPanel();
		JLabel detailsLabel = new JLabel("Details");
		detailsLabel.setFont(detailsLabel.getFont().deriveFont(20f));
		centeredPanel.addc(detailsLabel);
		centeredPanel.c.gridy++;
		centeredPanel.c.insets(30);
		centeredPanel.addc(topInfoPanel);

		PPanel centeredCommanderPanel = new PPanel();
		if(commanderLabel == null) {
			commanderLabel = new JLabel("Commander: None");
		}
		centeredCommanderPanel.addc(commanderLabel);
		centeredCommanderPanel.c.gridy++;
		centeredCommanderPanel.c.insets(10);
		centeredCommanderPanel.addc(commanderPanel);

		c.insets(40);
		add(centeredPanel, c);
		c.gridy++;
		c.insets(20, 0, 20);
		add(centeredCommanderPanel, c);
		c.gridy++;
		c.insets(20, 0, 20);
		add(bottomInfoPanel, c);

		c.gridy++;
		SimPanel simPanel = new SimPanel(deckId);
		addc(simPanel);

		revalidate();

		syncPrices(false);
	}

	protected void syncPrices(boolean force) {
		// Fire another rebuild for price related things
		if(isShowPrice()) {
			PriceUtil.fetchPrices(deck, (List<Card> cards) -> {
				if(cards.size() > 0) {
					System.out.println("Updated the price of " + cards.size() + " cards. Rebuilding again.");
					TaskUtil.runSwing(() -> {
						rebuildDeckRows();
					});
				} else if(force) {
					TaskUtil.runSwing(() -> {
						rebuildDeckRows();
					});
				}
			});
		} else if(force) {
			rebuildDeckRows();
		}
	}

	protected PTextField nameText;
	protected PTextField authorText;
	protected JTextArea noteText;
	protected ManaPanel colorPanel;
	protected PButton addTagButton;
	protected PPanel tagPanel;
	protected JLabel infoLabel;

	public void rebuildInfoPanel() {
		topInfoPanel.removeAll();
		topInfoPanel.c.reset();
		bottomInfoPanel.removeAll();
		bottomInfoPanel.c.reset();

		nameText = new PTextField();
		authorText = new PTextField();
		authorText.setEditable(false);
		authorText.setBorder(null);
		noteText = new JTextArea();
		colorPanel = new ManaPanel(false) {

			@Override
			public void handleChange(PCheckBox check) {
				String manaString = colorPanel.getSelectedManaString();
				deckHeader.setColors(manaString);
				flagModified();
			}

		};
		colorPanel.setSelectedManaString(deckHeader.getColors());
		rebuildTagPanel();

		Dimension dim = new Dimension(200, 20);
		nameText.setPreferredSize(dim);
		authorText.setPreferredSize(dim);
		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);
		noteText.setMargin(new Insets(5,5,5,5));

		nameText.setText(deckHeader.getName());
		authorText.setText(" " + deckHeader.getAuthorFormatted());
		noteText.setText(deckHeader.getNote());

		nameText.getDocument().addDocumentListener(new DocumentListener() {

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        	deckHeader.setName(nameText.getText());
	        	setName(deckHeader.getName());
				getAccordionData().setText(deckHeader.getName());
	        	flagModified();
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	deckHeader.setName(nameText.getText());
	        	setName(deckHeader.getName());
				getAccordionData().setText(deckHeader.getName());
	        	flagModified();
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	deckHeader.setName(nameText.getText());
	        	setName(deckHeader.getName());
				getAccordionData().setText(deckHeader.getName());
	        	flagModified();
	        }

		});

		/*authorText.getDocument().addDocumentListener(new DocumentListener() {

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        	deckHeader.setAuthor(authorText.getText());
	        	flagModified();
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	deckHeader.setAuthor(authorText.getText());
	        	flagModified();
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	deckHeader.setAuthor(authorText.getText());
	        	flagModified();
	        }

		});*/

		noteText.getDocument().addDocumentListener(new DocumentListener() {

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        	deckHeader.setNote(noteText.getText());
	        	flagModified();
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	deckHeader.setNote(noteText.getText());
	        	flagModified();
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	deckHeader.setNote(noteText.getText());
	        	flagModified();
	        }

		});

		infoLabel = new JLabel();
		updateInfoLabel();

		topInfoPanel.c.anchor = G.WEST;
		topInfoPanel.c.weightx = 0.01;
		topInfoPanel.c.weighty = 0.01;

		topInfoPanel.c.gridx = 0;
		topInfoPanel.addc(new JLabel("Name"));
		topInfoPanel.c.gridx++;
		topInfoPanel.addc(nameText);
		topInfoPanel.c.gridx++;
		topInfoPanel.c.insets(0, 20);
		topInfoPanel.addc(new JLabel("Colors"));
		topInfoPanel.c.insets();
		topInfoPanel.c.gridx++;
		topInfoPanel.addc(colorPanel);
		topInfoPanel.c.gridy++;
		topInfoPanel.c.insets(5);

		topInfoPanel.c.gridx = 0;
		topInfoPanel.addc(new JLabel("Created by "));
		topInfoPanel.c.gridx++;
		topInfoPanel.addc(authorText);
		topInfoPanel.c.gridx++;
		topInfoPanel.c.insets(0, 20);
		topInfoPanel.c.gridwidth = 2;
		topInfoPanel.c.gridheight = 2;
		topInfoPanel.addc(tagPanel);
		topInfoPanel.c.gridwidth = 1;
		topInfoPanel.c.gridheight = 1;
		topInfoPanel.c.insets(0, 0);
		topInfoPanel.c.gridy++;

		topInfoPanel.c.gridx = 3;
		topInfoPanel.c.weightx = 1.0;
		topInfoPanel.c.weighty = 1.0;
		topInfoPanel.addc(Box.createHorizontalStrut(1));

		PScrollPane areaScrollPane = new PScrollPane(noteText);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(400, 100));
		areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		bottomInfoPanel.addc(new JLabel("Comments"));
		bottomInfoPanel.c.gridy++;
		bottomInfoPanel.c.insets(10);
		bottomInfoPanel.addc(areaScrollPane);
		bottomInfoPanel.c.gridy++;
		bottomInfoPanel.addc(infoLabel);

	}

	public void updateInfoLabel() {
		DeckHeader header = getDeckHeader();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>First Created:</b> " + TimeUtil.toFormattedDate(header.getTimeFirstCreated()) + " | ");
		sb.append("<b>Last Modified:</b> " + TimeUtil.toFormattedDate(header.getTimeLastModified()) + " | ");
		sb.append("<b>Revisions:</b> " + header.getRevision());
		sb.append("</html>");
		infoLabel.setText(sb.toString());
	}

	public void rebuildTagPanel() {
		if(tagPanel == null) {
			tagPanel = new PPanel();
		} else {
			tagPanel.removeAll();
		}
		addTagButton = new PButton("+ Tag");
		addTagButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TagDialog dialog = new TagDialog(DeckEditorView.this, deckId) {

					@Override
					public void handleTagAdded(Tag tag) {
						deckHeader.addTag(tag);
						flagModified();
						rebuildTagPanel();
						tagPanel.revalidate();
						tagPanel.repaint();
					}

				};
				dialog.showDialog();
			}

		});

		List<Tag> tags = new ArrayList<Tag>();
		for(int tagId : deckHeader.getTagIds()) {
			Tag tag = Session.getInstance().getTag(tagId);
			tags.add(tag);
		}
		Collections.sort(tags, new Comparator<Tag>() {

			@Override
			public int compare(Tag tagA, Tag tagB) {
				return tagA.getName().compareTo(tagB.getName());
			}

		});
		tagPanel.c.weightx = 0.01;
		tagPanel.c.insets(10,5);
		tagPanel.addc(new JLabel(""));
		tagPanel.c.gridx++;
		for(Tag tag : tags) {
			TagLabel tagLabel = new TagLabel(tag.getId()) {

				@Override
				public void handleRemove(int tagId) {
					deckHeader.removeTagId(tagId);
					flagModified();
					rebuildTagPanel();
					tagPanel.revalidate();
					tagPanel.repaint();
				}

			};
			tagPanel.addc(tagLabel);
			GlassPane gp = tagLabel.buildGlassPane();
			tagPanel.addc(gp);
			tagPanel.setComponentZOrder(gp, 1);
			tagPanel.c.gridx++;
		}
		tagPanel.c.insets(0,5);

		tagPanel.addc(addTagButton);
		tagPanel.c.gridx++;
		tagPanel.c.weightx = 1.0;
		tagPanel.addc(Box.createHorizontalStrut(1));
	}

	public void rebuildDeckRows() {
		boolean revalidate = false;
		if(deckPanel != null) {
			deckPanel.removeAll();
			deckPanel.c.reset();
			revalidate = true;
		}
		if(sideboardPanel != null) {
			sideboardPanel.removeAll();
			sideboardPanel.c.reset();
		}

		buildEditor(getDeckText(), deck, deckPanel, 4, "<html>&rarr;</html>", deck.getSideboard(), filterByDeck);
		buildEditor(getSideboardText(), deck.getSideboard(), sideboardPanel, 1, "<html>&larr;</html>", deck, filterBySideboard);

		if(revalidate) {
			deckPanel.revalidate();
			sideboardPanel.revalidate();
			revalidate();
			repaint();
		}

		syncPrices(false);
	}

	protected String getDeckText() {
		return "Deck";
	}

	protected String getSideboardText() {
		return "Sideboard";
	}

	public void updateCopies() {
		if(deckHeader != null) {
			deckHeader = deckHeader.copy();
		}
		if(deckContent != null) {
			deckContent = deckContent.copy();
		}
	}

	public void save() {
		ignoreNextSave = true;
		deckContent.setFromDeck(deck);
		deckContent.save();
		deckHeader.save();
		clearModified();
		updateCopies();
	}

	public void copy() {
		String defaultName = getUniqueName(deckHeader.getName());
		String name = JUtil.showInputDialog(JUtil.getFrame(this), "Copy Deck \"" + deckHeader.getName() + "\"", "Copy deck\"" + deckHeader.getName() + "\" as", defaultName);
		if(name == null) {
			return;
		}
		if(name.isEmpty()) {
			name = "Untitled";
		}

		DeckHeader copyHeader = deckHeader.copy();
		DeckContent copyContent = deckContent.copy();
		copyHeader.newId();
		copyHeader.setName(name);
		copyContent.setId(copyHeader.getId());
		copyHeader.save();
		copyContent.save();

		PAccordion accordion = getAccordion();
		for(PAccordionPanel p : accordion.getAccordionPanels()) {
			p.contract();
		}
		DeckEditorView.addDeckEditorView(accordion, copyHeader.getId(), (CollectionEditorView) getAccordionData().getParent().getComponent());
		accordion.rebuild();
	}

	public void encode() {
		DeckEncoder.showEncodeDialog(this, deck);
	}

	public void clear() {
		deck.clear();
		if(deck.hasSideboard()) {
			deck.getSideboard().clear();
		}
		rebuildDeckRows();
		flagModified();
	}

	public void rename() {
		String text = JUtil.showInputDialog(this, "Rename Deck \"" + deckHeader.getName() + "\"", "", deckHeader.getName());
		if(text == null) {
			return;
		}
		if(!text.equals(deckHeader.getName())) {
			deckHeader.setName(text);
			setName(deckHeader.getName());
			getAccordionData().setText(deckHeader.getName());
			flagModified();
		}
	}

	public void draft() {
		if(!isModified() || JUtil.showConfirmYesNoDialog(this, "About to Draft from Deck \"" + deckHeader.getName() + "\"", "You must first save if you want to draft. Would you like to save your unsaved changes?")) {
			if(isModified()) {
				save();
			}
			ConfigureDraftDialog dialog = new ConfigureDraftDialog((CollectionEditorView) getAccordionData().getParent().getComponent(), deckId);
			dialog.showDialog();
		}
	}

	public void editTags() {

	}

	protected void buildEditor(final String title, final Deck deck, PPanel p, int defaultCardCountPerRow, final String otherDeckName, final Deck otherDeck, final Deck filterBy) {
		if(deck == null) {
			return;
		}

		Card commander = null;
		for(Card card : deck) {
			if(card.getCardInfo() == null) {
				System.out.println("No card info for card: " + card.getName());
			}
			if(card.isCommander()) {
				commander = card;
			}
		}

		ImageUtil.cacheImageInBackground(deck, 1.0, null);

		// Use last value as default if present
		DeckEditorRow oldAddRow = addRowsByTitle.get(title);
		if(oldAddRow != null) {
			defaultCardCountPerRow = oldAddRow.getCount();
		}

		DeckEditorRow addRow = new DeckEditorRow(this, deck, defaultCardCountPerRow, "");
		addRow.getCardCombo().getTextField().setEditable(true);
		addRow.getCardCombo().getTextField().setFocusable(true);
		addRow.getCardCombo().getTextField().setBorder(BorderFactory.createLineBorder(Color.WHITE));
		addRow.hideColorLabel();

		addRowsByTitle.put(title, addRow);

		PButton addButton = new PButton("+");
		addButton.setFocusable(false);
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DeckEditorRow addRow = addRowsByTitle.get(title);
				if(addRow.hasCard() && addRow.getCount() > 0) {
					deck.add(addRow.getCard(), addRow.getCount());
					rebuildDeckRows();
					flagModified();

					// We now have a new row, make sure it gets focus
					addRow = addRowsByTitle.get(title);
					addRow.getCardCombo().getTextField().requestFocusInWindow();
				}
			}

		});
		addButton.setPreferredSize(new Dimension(20, 20));

		addRow.getCardCombo().getTextField().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DeckEditorRow addRow = addRowsByTitle.get(title);
				if(addRow.getCardCombo().getSelectedIndex() > -1 && addRow.getCardCombo().isPopupVisible()) {
					return;
				}
				if(addRow.hasCard() && addRow.getCount() > 0) {
					deck.add(addRow.getCard(), addRow.getCount());
					rebuildDeckRows();
					flagModified();

					// We now have a new row, make sure it gets focus
					addRow = addRowsByTitle.get(title);
					addRow.getCardCombo().getTextField().requestFocusInWindow();
				}
			}

		});

		if(deck.getSideboard() != null) {
			commanderEditor = new DeckEditorRow(this, deck, 1, "");
			commanderEditor.getCardCombo().getTextField().setEditable(true);
			commanderEditor.getCardCombo().getTextField().setFocusable(true);
			commanderEditor.getCardCombo().getTextField().setBorder(BorderFactory.createLineBorder(Color.WHITE));
			commanderEditor.hideColorLabel();
			commanderEditor.hideSpinner();
			commanderEditor.getCardCombo().getTextField().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(commanderEditor.getCardCombo().getSelectedIndex() > -1 && commanderEditor.getCardCombo().isPopupVisible()) {
						return;
					}
					if(commanderEditor.hasCard()) {
						Card commander = commanderEditor.getCard();

						boolean newCommander = false;
						boolean hadCommander = false;
						for(Card card : new ArrayList<Card>(deck)) {
							if(card.isCommander()) {
								hadCommander = true;
								if(!card.getName().equals(commander.getName())) {
									deck.remove(card);
									newCommander = true;
								}
							}
						}

						if(!hadCommander || newCommander) {
							commander.setCommander(true);
							deck.add(commander, 1);
							commanderLabel.setText("Commander: " + commander.getName());
							rebuildDeckRows();
							flagModified();
							commanderEditor.getCardCombo().getTextField().requestFocusInWindow();
						}

					} else {
						boolean hadCommander = false;
						for(Card card : new ArrayList<Card>(deck)) {
							if(card.isCommander()) {
								hadCommander = true;
								deck.remove(card);
							}
						}
						commanderLabel.setText("Commander: None");
						if(hadCommander) {
							flagModified();
						}
					}
				}

			});

			if(commanderPanel == null) {
				commanderPanel = new PPanel();
			} else {
				commanderPanel.clear();
			}
			commanderPanel.addc(commanderEditor);
		}
		if(commander != null) {
			if(commanderLabel == null) {
				commanderLabel = new JLabel();
			}
			commanderLabel.setText("Commander: " + commander.getName());
		}

		ShuffleUtil.shuffle(sortHandler.getShuffleType(), deck);
		Map<Card, Integer> cards = deck.getCountsByCard();

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleLabel.getFont().deriveFont(20f));

		JLabel addLabel = new JLabel("Add");
		addLabel.setFocusable(false);

		p.c.gridx++;
		p.c.anchor = G.CENTER;
		p.c.insets(10, 0, 10, 0);
		p.add(titleLabel, p.c);
		p.c.gridy++;
		p.c.insets(10,0,10,0);
		p.add(addLabel, p.c);
		p.c.anchor = G.WEST;
		p.c.gridx--;
		p.c.gridy++;
		p.c.insets(0,0,10,-70);
		p.add(addButton, p.c);
		p.c.gridx++;
		p.c.insets(0,0,10,0);
		p.add(addRow, p.c);
		p.c.insets();
		p.c.gridy++;
		p.c.anchor = G.WEST;

		p.c.insets(0,0,10,0);

		typeLabels.put(deck.getName(), new HashMap<String, JLabel>());
		int allTotal = 0;
		Set<Card> usedCards = new HashSet<Card>();
		for(String type : types) {
			boolean useType = false;
			int j = p.c.gridy++;
			int total = 0;

			for(final Card card : cards.keySet()) {
				if(commander != null && card.getName().equals(commander.getName())) {
					continue;
				}

				if(!CardUtil.hasType(card, type) || usedCards.contains(card) || (CardUtil.hasType(card, "Land") && !type.equals("Land"))) {
					continue;
				}
				usedCards.add(card);
				int count = cards.get(card);
				total += count;
				DeckEditorRow row = new DeckEditorRow(this, deck, count, card.getName(), otherDeckName, otherDeck);

				if(filterBy != null && filterBy.getCount(card.getName()) > 0) {
					row.setBorder(BorderFactory.createLineBorder(Color.GREEN));
				}

				p.add(row, p.c);
				GlassPane gp = row.buildGlassPane();
				p.add(gp, p.c);
				p.setComponentZOrder(gp, 1);
				p.c.gridy++;
				useType = true;
			}
			allTotal += total;

			if(useType) {
				int newJ = p.c.gridy;
				p.c.gridy = j;
				JLabel typeLabel = new JLabel(type + "s (" + total + ")");
				typeLabel.setFocusable(false);
				p.c.anchor = G.CENTER;
				p.add(typeLabel, p.c);
				p.c.anchor = G.WEST;
				typeLabels.get(deck.getName()).put(type, typeLabel);
				p.c.gridy = newJ;
			}
		}

		JLabel cardTotalLabel = new JLabel("Cards (" + allTotal + ")");
		cardTotalLabel.setFocusable(false);
		totalLabels.put(deck.getName(), cardTotalLabel);

		priceLabel = new JLabel(getDeckPriceText(deck));
		priceLabel.setForeground(PriceUtil.PRICE_COLOR);
		priceLabel.setFocusable(false);

		p.c.gridy++;
		p.c.anchor = G.CENTER;
		p.c.insets(20, 0, 0, 0);
		p.add(totalLabels.get(deck.getName()), p.c);
		p.c.gridy++;
		p.add(priceLabel, p.c);
		p.c.gridy++;

		p.c.insets(50);
		p.c.anchor = G.WEST;
		p.c.insets();
		p.c.gridy++;
		p.c.gridx++;
		p.c.strengthen();
		p.add(Box.createHorizontalStrut(1), p.c);
	}

	public String getDeckPriceText(Deck deck) {
		if(!isShowPrice()) {
			return "";
		}
		double price = 0;
		Map<Card, Integer> cards = deck.getCountsByCard();
		for(Card card : cards.keySet()) {
			if(card.getPriceInfo().price < 0) {
				continue;
			}
			price += card.getPriceInfo().price * cards.get(card);
		}
		return PriceUtil.formatPrice(price);
	}

	@Override
	public void handleModified(boolean isModified) {
		saveButton.setEnabled(modified);

		if(hasAccordionData()) {
			getAccordionData().setText((isModified() ? "*" : "") + getName());
			getAccordionData().getAccordionPanel().getExpandButton().setText(getAccordionData().getFormattedText());

			if(!getAccordionData().getAccordionPanel().isExpanded()) {
				getAccordionData().getAccordionPanel().expand();
			}
		}
	}

	public void setCardCount(Deck deck, String cardName, int newCount) {
		int oldCount = deck.getCount(cardName);
		if(newCount == oldCount) {
			return;
		}
		if(newCount > oldCount) {
			deck.add(new Card(cardName), newCount - oldCount);
		} else {
			int removeCount = oldCount - newCount;
			Iterator<Card> it = deck.iterator();
			while(removeCount > 0) {
				Card card = it.next();
				if(card.getName().equals(cardName)) {
					it.remove();
					removeCount--;
				}
			}
		}
		flagModified();
		updateCardData(deck);
	}

	public void updateCardData(Deck deck) {
		Map<Card, Integer> cards = deck.getCountsByCard();
		Set<Card> usedCards = new HashSet<Card>();
		int allTotal = 0;
		for(String type : types) {
			if(!typeLabels.get(deck.getName()).containsKey(type)) {
				continue;
			}

			JLabel typeLabel = typeLabels.get(deck.getName()).get(type);
			int total = 0;

			for(Card card : cards.keySet()) {
				if(!CardUtil.hasType(card, type) || usedCards.contains(card) || (CardUtil.hasType(card, "Land") && !type.equals("Land")) || card.isCommander()) {
					continue;
				}
				usedCards.add(card);
				int count = cards.get(card);
				total += count;
			}

			typeLabel.setText(type + "s (" + total + ")");
			typeLabel.setVisible(total > 0);
			allTotal += total;
		}
		totalLabels.get(deck.getName()).setText("Cards (" + allTotal + ")");
		priceLabel.setText(getDeckPriceText(deck));
	}

	public PPanel getPageHeader() {
		return headerPanel;
	}

	public PPanel getPageFooter() {
		PPanel p = new PPanel();
		p.c.insets(10,0,10);
		p.add(sortHandler, p.c);
		return p;
	}

	@Override
	public void reset() {

	}

	@Override
	public void addCard(Card card, boolean sideboard) {
		if(sideboard) {
			if(deck.hasSideboard()) {
				deck.getSideboard().add(card);
			}
		} else {
			deck.add(card);
		}
		rebuildDeckRows();
		flagModified();
	}

	public void editAsDraft() {
		DraftEditorView.addDraftEditorView(getAccordion(), IDUtil.NONE, deckId, getAccordionData().getParent());
		getAccordionData().getAccordionPanel().remove();
		getAccordion().rebuild();
	}

	public void toHighlander() {
		for(Card card : new ArrayList<Card>(deck)) {
			if(card.isBasicLand()) {
				continue;
			}
			setCardCount(deck, card.getName(), 1);
		}
		for(Card card : new ArrayList<Card>(deck.getSideboard())) {
			if(card.isBasicLand()) {
				continue;
			}
			setCardCount(deck.getSideboard(), card.getName(), 1);
		}
		rebuildDeckRows();
		flagModified();
	}

	public void editAsDeck() {
		DeckEditorView.addDeckEditorView(getAccordion(), deckId, getAccordionData().getParent());
		getAccordionData().getAccordionPanel().remove();
		getAccordion().rebuild();
	}

	public void setFilterByDeck(Deck filterByDeck) {
		this.filterByDeck = filterByDeck;

		SwingUtilities.invokeLater(() -> {
			rebuildDeckRows();
		});
	}

	public void setFilterBySideboard(Deck filterBySideboard) {
		this.filterBySideboard = filterBySideboard;

		SwingUtilities.invokeLater(() -> {
			rebuildDeckRows();
		});
	}

	public void setFilterByDeckAndSideboard(Deck filterBy) {
		this.filterByDeck = filterBy;
		this.filterBySideboard = filterBy;

		SwingUtilities.invokeLater(() -> {
			rebuildDeckRows();
		});
	}

	public static void main(String[] args) {
		Jandor.init();

		Deck deckA = new Deck("Biovisionary");
		Deck deckB = new Deck("Lifegain");
		deckA.setSideboard(new Deck());
		deckB.setSideboard(new Deck());

		PAccordion accordion = new PAccordion();
		//addDeckEditorView(accordion, deckA, true);
		//addDeckEditorView(accordion, deckB, true);
		accordion.build();

		JUtil.popupWindow("Edit Deck", accordion);
	}

}
