package editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;

import mail.SendDialog;
import session.DeckHeader;
import session.Session;
import session.Tag;
import session.User;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.pwidget.PPanel;
import ui.pwidget.PTabPane;
import ui.view.BoardView;
import ui.view.CollectionEditorView;
import ui.view.DeckEditorView;
import util.DebugUtil;
import util.LoginUtil;
import util.ManaUtil;
import util.TimeUtil;
import accordion.PAccordion;
import accordion.PAccordionPanel;
import deck.Deck;

public class CollectionEditorRow extends PPanel {

	protected CollectionEditorView view;
	protected int deckHeaderId;
	protected JandorButton removeButton;
	protected JandorButton nameButton;
	protected JLabel authorLabel;
	protected JLabel colorLabel;
	protected JandorButton playButton;
	protected JandorButton shareButton;
	protected JandorButton copyButton;
	protected JandorButton tagButton;
	protected PPanel tagPanel;

	public CollectionEditorRow(CollectionEditorView view, DeckHeader header) {
		this(view, header.getId());
	}

	public CollectionEditorRow(CollectionEditorView view, int deckHeaderId) {
		super();
		this.view = view;
		this.deckHeaderId = deckHeaderId;
		init();
	}

	protected void init() {
		final DeckHeader header = getDeckHeader();

		removeButton = JUtil.buildCloseButton();
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JUtil.showConfirmYesNoCancelDialog(JUtil.getFrame(view), "Delete Deck \"" + header.getName() + "\"", "Are you sure you want to permanently delete this deck?")) {
					remove();
				}
			}

		});

		playButton = new JandorButton("Play");
		playButton.hide();
		playButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				play();
			}

		});

		copyButton = new JandorButton("Copy");
		copyButton.hide();
		copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}

		});

		shareButton = new JandorButton(header.isInbox() ? "Move" : "Send");
		shareButton.hide();
		shareButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(header.isInbox()) {
					moveToCollection();
				} else {
					share();
				}
			}

		});

		tagButton = new JandorButton("+ Tag");
		tagButton.hide();
		tagButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addTag();
			}

		});

		nameButton = new JandorButton(header.getName());
		nameButton.setShowTextAlways(true);
		nameButton.hide();
		nameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PAccordion accordion = view.getAccordion();
				for(PAccordionPanel p : accordion.getAccordionPanels()) {
					p.contract();
				}
				DeckEditorView.addDeckEditorView(accordion, deckHeaderId, view);
				accordion.rebuild();
			}

		});

		nameButton.setToolTipText(buildTooltip());

		User user = LoginUtil.getUser();
		authorLabel = new JLabel(header.getAuthor().isEmpty() || header.getAuthorGUID().equals(user.getGUID()) ? "" : "by " + header.getAuthorFormatted() + "");
		colorLabel = new JLabel("<html>" + getManaString() + "</html>");

		c.anchor = G.CENTER;
		c.weaken();
		c.insets(0, 5, 0, 5);
        add(removeButton, c);
        c.insets(0, 0, 0, 5);
        c.gridx++;
        add(nameButton, c);
        c.gridx++;
        add(shareButton, c);
        c.strengthen();
        c.gridx++;
        addc(Box.createHorizontalStrut(1));
        c.weaken();
        c.gridx++;
        add(colorLabel, c);
        c.gridx++;
        add(playButton, c);

        tagPanel = new PPanel();
        if(view.isShowTags()) {
        	tagPanel.addc(new JLabel(""));
        	tagPanel.c.gridx++;
        	tagPanel.c.insets(0,5);
        	List<Tag> tags = new ArrayList<Tag>();
        	for(int tagId : header.getTagIds()) {
        		tags.add(Session.getInstance().getTag(tagId));
        	}
        	Collections.sort(tags, new Comparator<Tag>() {

    			@Override
    			public int compare(Tag tagA, Tag tagB) {
    				return tagA.getName().compareTo(tagB.getName());
    			}

    		});
        	for(Tag tag : tags) {
        		TagLabel tagLabel = new TagLabel(tag.getId()) {

					@Override
					public void handleRemove(int tagId) {
						header.removeTagId(tagId);
						header.save();
					}

        		};
        		tagPanel.c.gridx++;
        		tagPanel.addc(tagLabel);
        		GlassPane gp = tagLabel.buildGlassPane();
        		tagPanel.addc(gp);
        		tagPanel.setComponentZOrder(gp, 1);
        	}

        	c.gridy++;
            c.gridx = 0;
            c.gridwidth = 10;
            c.anchor = G.EAST;
            c.insets(5, 0, 5, 0);
        	addc(tagPanel);

            c.insets(0, 5, 0, 5);
            c.gridx = 11;
            addc(tagButton);
        }

	}

	public JLabel getAuthorLabel() {
		return authorLabel;
	}

	public PPanel getTagPanel() {
		return tagPanel;
	}

	private String getManaString() {
		DeckHeader header = getDeckHeader();
		List<String> manaSymbols = new ArrayList<String>();
		for(int i = 0; i < header.getColors().length(); i++) {
			manaSymbols.add("" + header.getColors().charAt(i));
		}
		return header.getColors() == null || header.getColors().isEmpty() ? "" : ManaUtil.toManaHtml(manaSymbols);
	}

	public DeckHeader getDeckHeader() {
		return Session.getInstance().getDeckHeader(deckHeaderId);
	}

	private String buildTooltip() {
		DeckHeader header = getDeckHeader();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>" + header.getName() + "</b> " + getManaString());
		sb.append("<hr>");
		sb.append("<b>Author:</b> " + header.getAuthorFormatted(true));
		sb.append("<br>");
		sb.append("<b>First Created:</b> " + TimeUtil.toFormattedDate(header.getTimeFirstCreated()));
		sb.append("<br>");
		sb.append("<b>Last Modified:</b> " + TimeUtil.toFormattedDate(header.getTimeLastModified()));
		sb.append("<br>");
		sb.append("<b>Revisions:</b> " + header.getRevision());
		if(header.getNote() != null && !header.getNote().isEmpty()) {
			sb.append("<br>");
			sb.append("<br>");
			sb.append("<div width=\"400px\"><b>Comment:</b>");
			sb.append("<br>");
			sb.append(header.getNote() + "</div>");
		}

		if(header.getTagIds().size() > 0) {
			sb.append("<br>");
			sb.append("<br>");
			//sb.append("<b>Tags:</b>");
			for(int tagId : header.getTagIds()) {
				Tag tag = Session.getInstance().getTag(tagId);
				if(tag == null) {
					continue;
				}
				sb.append(" #" + tag.getName());
			}
		}
		sb.append("</html>");
		return sb.toString();
	}

	public JandorButton getRemoveButton() {
		return removeButton;
	}

	public void remove() {
		if(getDeckHeader() != null) {
			getDeckHeader().delete();
		}
		view.rebuildDeckRows();
	}

	public void play() {
		Deck deck = getDeck();

		JandorTabFrame frame = JUtil.getFrame(this);
		PTabPane tabPane = frame.getTabPane();
		BoardView boardView = new BoardView(BoardView.DEFAULT_TITLE);
		boardView.setDeck(deck.copyRenderable());
		boardView.setDeckId(deckHeaderId);
		tabPane.addTab(JandorTabFrame.toBoardTitle(deck), boardView);
		tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
	}

	public void share() {
		if(DebugUtil.isOfflineMode()) {
			JUtil.showWarningDialog(this, "Cannot Send Deck", "Currently running in offline mode. Cannot send decks.");
			return;
		}
		SendDialog dialog = new SendDialog(deckHeaderId);
		dialog.showDialog();
	}

	public void moveToCollection() {
		DeckHeader header = getDeckHeader();
		if(JUtil.showConfirmDialog(JUtil.getFrame(this), "Move Deck \"" + header.getName() + "\"", new JLabel("Move deck \"" + header.getName() + "\" to your collection?"))) {
			header.setInbox(false);
			header.save();
		}
	}

	public void copy() {

	}

	public void addTag() {
		TagDialog dialog = new TagDialog(view, deckHeaderId) {

			@Override
			public void handleTagAdded(Tag tag) {
				DeckHeader header = getDeckHeader();
				header.addTag(tag);
				header.save();
			}

		};
		dialog.showDialog();
	}

	public Deck getDeck() {
		return new Deck(getDeckHeader(), Session.getInstance().getDeckContent(deckHeaderId));
	}

	public GlassPane buildGlassPane() {
		GlassPane gp = new GlassPane(this) {

			@Override
			public void mouseEntered(MouseEvent e) {
				removeButton.show();
				nameButton.show();
				playButton.show();
				tagButton.show();
				shareButton.show();
				copyButton.show();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeButton.hide();
				nameButton.hide();
				playButton.hide();
				tagButton.hide();
				shareButton.hide();
				copyButton.hide();
				super.mouseExited(e);
			}

		};
		return gp;
	}
}
