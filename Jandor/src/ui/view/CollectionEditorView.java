package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;

import accordion.PAccordion;
import accordion.PAccordionData;
import accordion.PAccordionPanel;
import deck.Deck;
import draft.ConfigureDraftDialog;
import editor.CollectionEditorRow;
import editor.TagRow;
import session.Contact;
import session.DeckHeader;
import session.Session;
import session.Tag;
import ui.GlassPane;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import util.ApprenticeUtil;
import util.CardUtil;
import util.DebugUtil;
import util.DeckEncoder;
import util.FileUtil;
import util.IDUtil;
import util.LoginUtil;
import util.ManaUtil;
import util.UIManagerUtil;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;

public class CollectionEditorView extends JandorView {

	public static final String DEFAULT_TITLE = "Collection";

	public static void addCollectionEditorView(PAccordion accordion) {
		final PAccordionData collectionEditorData = new PAccordionData(DEFAULT_TITLE);

		CollectionEditorView collectionEditorView = new CollectionEditorView(DEFAULT_TITLE);
		collectionEditorData.setComponent(collectionEditorView);
		collectionEditorData.setRemoveable(false);
		collectionEditorData.setDefaultExpanded(true);
		collectionEditorData.setHeaderComponent(collectionEditorView.getPageHeader());
		collectionEditorData.setFooterComponent(collectionEditorView.getPageFooter());

		accordion.add(collectionEditorData, false);
	}

	protected PPanel tagPanel;
	protected PPanel deckPanel;
	protected int currentTagId = Tag.ALL_ID;
	protected List<TagRow> tagRows = new ArrayList<TagRow>();
	protected TagRow allRow;
	protected TagRow inboxRow;
	protected PPanel pageHeader;
	protected PPanel pageFooter;
	protected boolean showTags = false;
	protected List<Integer> selectedTagIds = new ArrayList<>();

	protected boolean enableEvents = true;

	public CollectionEditorView(String name) {
		super(name, true);
		rebuild();

		SessionEventManager.addListener(DeckHeader.class, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				if(!enableEvents) {
					return;
				}
				rebuild();
			}

		});

		SessionEventManager.addListener(Contact.class, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				if(!enableEvents) {
					return;
				}
				rebuild();
			}

		});

		SessionEventManager.addListener(Tag.class, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				if(!enableEvents) {
					return;
				}
				rebuild();
			}

		});

	}

	public void handleEvent() {
		rebuild();
	}

	public void enableEvents() {
		enableEvents = true;
	}

	public void disableEvents() {
		enableEvents = false;
	}

	@Override
	public void handleClosed() {
		SessionEventManager.removeListeners(this);
	}

	@Override
	protected void rebuild() {
		removeAll();
		c.reset();
		c.anchor = G.NORTHWEST;

		rebuildTagRows();
		rebuildDeckRows();

		c.weightx = 0.01;
		addc(tagPanel);
		c.gridx++;
		c.insets(0,10,0,20);
		c.weightx = 1.0;
		addc(deckPanel);
		c.insets(0,0,0,20);

		setCurrentMajorTagId(currentTagId);
		revalidate();

		pageHeader = new PPanel();
		pageHeader.c.anchor = G.NORTHWEST;
		pageHeader.c.insets = new Insets(0,5,20,0);
		pageHeader.c.weightx = 0.01;

		PButton importButton = new PButton("Import");
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				importDeck();
			}

		});

		PButton deckButton = new PButton("+ New Deck");

		deckButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PAccordion accordion = getAccordion();
				for(PAccordionPanel p : accordion.getAccordionPanels()) {
					p.contract();
				}
				DeckEditorView deckEditorView = DeckEditorView.addDeckEditorView(accordion, CollectionEditorView.this);
				accordion.rebuild();
				deckEditorView.flagModified();
			}

		});

		PButton decodeButton = new PButton("+ Deck by Code");

		decodeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DeckEncoder.showDecodeDialog(CollectionEditorView.this, (deck) -> {
					if(deck != null && deck.size() > 0) {
						PAccordion accordion = getAccordion();
						for(PAccordionPanel p : accordion.getAccordionPanels()) {
							p.contract();
						}
						DeckEditorView deckEditorView = DeckEditorView.addDeckEditorView(accordion, CollectionEditorView.this);
						deckEditorView.setDeck(deck);
						accordion.rebuild();
						deckEditorView.flagModified();
						return true;
					} else {
						JUtil.showWarningDialog(CollectionEditorView.this, "Could Not Decode Deck", "Could not find a valid deck string to decode. Please copy a deck string to the clipboard and try again.");
						return false;
					}
				});
			}

		});

		PButton draftButton = new PButton("+ New Set Draft");

		draftButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigureDraftDialog dialog = new ConfigureDraftDialog(CollectionEditorView.this);
				dialog.showDialog();
			}

		});

		pageHeader.addc(importButton);
		pageHeader.c.gridx++;
		pageHeader.c.weightx = 1.0;
		pageHeader.addc(Box.createHorizontalStrut(1));
		pageHeader.c.weightx = 0.01;
		pageHeader.c.gridx++;
		pageHeader.addc(draftButton);
		pageHeader.c.gridx++;
		pageHeader.addc(decodeButton);
		pageHeader.c.gridx++;
		pageHeader.addc(deckButton);

		pageFooter = new PPanel();
		final PCheckBox showTagsCheck = new PCheckBox("Show Tags");
		showTagsCheck.setSelected(showTags);
		showTagsCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				showTags = showTagsCheck.isSelected();
				rebuild();
			}

		});

		pageFooter.c.anchor = G.CENTER;
		pageFooter.addc(showTagsCheck);

		repaint();
	}

	public boolean isShowTags() {
		return showTags;
	}

	public PPanel getTagPanel() {
		return tagPanel;
	}

	public PPanel getDeckPanel() {
		return deckPanel;
	}

	public PPanel getPageHeader() {
		return pageHeader;
	}

	public PPanel getPageFooter() {
		return pageFooter;
	}

	public void handleTagRowClicked(int tagId) {
		if(tagId == Tag.INBOX_ID || tagId == Tag.ALL_ID) {
			setCurrentMajorTagId(tagId);
		} else {
			toggleMinorTag(tagId);
		}
	}

	protected void setCurrentMajorTagId(int tagId) {
		currentTagId = tagId;

		for(TagRow row : tagRows) {
			if(row.getTagId() != Tag.INBOX_ID && row.getTagId() != Tag.ALL_ID) {
				continue;
			}
			row.getTagButton().setSelected(row.getTagId() == tagId);
		}

		rebuildDeckRows();
	}

	protected void toggleMinorTag(int tagId) {
		if(tagId >= 0) {
			if(!selectedTagIds.contains(tagId)) {
				selectedTagIds.add(tagId);
			} else {
				selectedTagIds.remove(Integer.valueOf(tagId));
			}
		}

		for(TagRow row : tagRows) {
			if(row.getTagId() == Tag.INBOX_ID || row.getTagId() == Tag.ALL_ID) {
				continue;
			}
			row.getTagButton().setSelected(selectedTagIds.contains(row.getTagId()));
		}

		rebuildDeckRows();
	}

	public void rebuildTagRows() {
		if(tagPanel == null) {
			tagPanel = new PPanel();
		} else {
			tagPanel.removeAll();
			tagPanel.c.reset();
		}
		tagRows.clear();
		tagPanel.c.anchor = G.WEST;

		inboxRow = new TagRow(this, Tag.INBOX_ID);
		tagPanel.addc(inboxRow);
		tagRows.add(inboxRow);
		tagPanel.c.gridy++;

		allRow = new TagRow(this, Tag.ALL_ID);
		tagPanel.addc(allRow);
		tagRows.add(allRow);
		tagPanel.c.gridy++;

		PPanel sep = new PPanel();
		sep.setPreferredSize(new Dimension(120, 1));
		sep.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		tagPanel.addc(sep);

		tagPanel.c.gridy++;

		List<Tag> tags = Session.getInstance().getTags(true);
		Collections.sort(tags, new Comparator<Tag>() {

			@Override
			public int compare(Tag tagA, Tag tagB) {
				return tagA.getName().toLowerCase().compareTo(tagB.getName().toLowerCase());
			}

		});

		for(Tag tag : tags) {
			TagRow tagRow = new TagRow(this, tag);
			tagPanel.addc(tagRow);
			GlassPane gp = tagRow.buildGlassPane();
			tagPanel.addc(gp);
			tagPanel.setComponentZOrder(gp, 1);
			tagPanel.c.gridy++;
			tagRows.add(tagRow);
		}

		if(deckPanel != null) {
			setCurrentMajorTagId(currentTagId);
		}

	}

	private List<DeckHeader> filterBySelectedTags(List<DeckHeader> headers) {
		if(selectedTagIds.size() == 0) {
			return headers;
		}
		List<DeckHeader> filtered = new ArrayList<>();
		for(DeckHeader header : headers) {
			for(int tagId : selectedTagIds) {
				if(header.hasTagId(tagId)) {
					filtered.add(header);
				}
			}
		}
		return filtered;
	}

	public void rebuildDeckRows() {
		if(deckPanel == null) {
			deckPanel = new PPanel();
		} else {
			deckPanel.removeAll();
			deckPanel.c.reset();
		}
		deckPanel.c.anchor = G.WEST;
		deckPanel.c.strengthen();

		PPanel widthPanel = new PPanel();
		widthPanel.setPreferredSize(new Dimension(300, 30));

		deckPanel.addc(widthPanel);
		deckPanel.c.gridy++;

		boolean warn = false;
		if(!LoginUtil.isLoggedIn() && currentTagId == Tag.INBOX_ID) {
			JLabel deckLabel = new JLabel("<html><div width=\"200px\">You're not logged in. Please login in the top right of the window to receive decks.</div></html>");
			widthPanel.c.insets(0,0,10,0);
			widthPanel.setPreferredSize(new Dimension(300, 58));
			widthPanel.addc(deckLabel);
			warn = true;
		} else if(DebugUtil.OFFLINE_MODE && currentTagId == Tag.INBOX_ID) {
			JLabel deckLabel = new JLabel("<html><div width=\"200px\">You're not online. Please go online and login to receive decks.</div></html>");
			widthPanel.c.insets(0,0,10,0);
			widthPanel.setPreferredSize(new Dimension(300, 58));
			widthPanel.addc(deckLabel);
			warn = true;
		} else {
			JLabel deckLabel = new JLabel("Decks");
			deckLabel.setFont(deckLabel.getFont().deriveFont(15f));
			widthPanel.c.insets(0,0,10,0);
			widthPanel.addc(deckLabel);
		}

		if(currentTagId != IDUtil.NONE) {

			List<DeckHeader> headers;
			if(currentTagId == Tag.INBOX_ID) {
				headers = Session.getInstance().getDeckHeadersInInbox();
			} else {
				headers = Session.getInstance().getDeckHeadersWithTagId(currentTagId, currentTagId != Tag.INBOX_ID);
			}

			headers = filterBySelectedTags(headers);

			Collections.sort(headers, new Comparator<DeckHeader>() {

				@Override
				public int compare(DeckHeader headerA, DeckHeader headerB) {
					int i = ManaUtil.compare(headerA.getColors(), headerB.getColors());
					if(i == 0) {
						return headerA.getName().compareTo(headerB.getName());
					}
					return i;
				}

			});

			deckPanel.c.strengthen();
			int i = 0;
			for(DeckHeader header : headers) {
				CollectionEditorRow row = new CollectionEditorRow(this, header);
				deckPanel.addc(row);
				GlassPane gp = row.buildGlassPane();
				deckPanel.addc(gp);
				deckPanel.setComponentZOrder(gp, 1);
				deckPanel.c.gridx++;
				deckPanel.c.weaken();
				PPanel p = new PPanel();
				p.c.anchor = G.WEST;
				p.c.insets(0,0,0,20);
				p.addc(row.getAuthorLabel());
				deckPanel.c.strengthen();
				deckPanel.addc(p);
				deckPanel.c.gridx++;
				deckPanel.addc(Box.createHorizontalStrut(1));
				deckPanel.c.gridx-=2;
				deckPanel.c.gridy++;
				row.setOpaque(true);
				row.setBackground(i % 2 == 0 ? ColorUtil.DARK_GRAY_2 : ColorUtil.DARK_GRAY_3);
				p.setOpaque(true);
				p.setBackground(i++ % 2 == 0 ? ColorUtil.DARK_GRAY_2 : ColorUtil.DARK_GRAY_3);
			}

			if(headers.size() == 0 && !warn) {
				widthPanel.c.gridy++;
				widthPanel.setPreferredSize(new Dimension(300, 58));
				widthPanel.addc(new JLabel("None"));
			}

		}

		tagPanel.revalidate();
		deckPanel.revalidate();
		revalidate();

		// Super weird size fix, but technically works
		JandorView view = this;
		if(view != null && view.getParent() != null && view.getParent().getParent() != null && view.getParent().getParent().getParent() != null) {
			view.getParent().getParent().getParent().revalidate();
		}
		repaint();
	}

	public void importDeck() {
		File[] files = FileUtil.chooseFiles(this);
		for(File file : files) {
			String filename = file.getAbsolutePath();
			Deck deck = ApprenticeUtil.toDeck(filename);
			Session.getInstance().importDeck(deck);
		}
		currentTagId = Tag.ALL_ID;
		rebuild();
	}

	@Override
	public void reset() {

	}

	public static void main(String[] args) {
		UIManagerUtil.init();
		CardUtil.init();
		JUtil.popupWindow("Edit Collection", new CollectionEditorView("Edit Collection"));
	}

}
