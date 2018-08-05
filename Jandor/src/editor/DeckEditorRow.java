package editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import deck.Card;
import deck.Deck;
import ui.AutoComboBox;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.ColorUtil;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import ui.pwidget.PSpinner;
import ui.view.DeckEditorView;
import ui.view.DraftEditorView;
import util.CardUtil;
import util.ManaUtil;

public class DeckEditorRow extends PPanel {

	private PSpinner countSpinner;
	private AutoComboBox<String> cardCombo;
	private JLabel colorLabel;
	private DeckEditorView view;
	private Deck deck;
	private Deck otherDeck;
	private JandorButton removeButton;
	private JandorButton moveButton;
	private String cardName;
	private String otherDeckName;
	
	public DeckEditorRow(DeckEditorView view, Deck deck) {
		this(view, deck, 0, "");
	}
	
	public DeckEditorRow(DeckEditorView view, Deck deck, int count, String cardName) {
		this(view, deck, count, cardName, null, null);
	}
	
	public DeckEditorRow(DeckEditorView view, Deck deck, int count, String cardName, String otherDeckName, Deck otherDeck) {
		super();
		this.view = view;
		this.deck = deck;
		this.cardName = cardName;
		this.otherDeckName = otherDeckName;
		this.otherDeck = otherDeck;
		init(count);
	}
	
	protected void init(int count) {
		removeButton = JUtil.buildCloseButton();
		removeButton.setFocusable(false);
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
			
		});
		
		if(otherDeckName != null) {
			moveButton = new JandorButton(otherDeckName);
			moveButton.setFocusable(false);
			moveButton.hide();
			moveButton.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					moveToDeck();
				}
				
			});
		}
		
		colorLabel = new JLabel("");
		colorLabel.setFocusable(false);
        //colorLabel.setOpaque(true);
        //colorLabel.setPreferredSize(new Dimension(14, 18));
        //colorLabel.setBorder(BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_0));
		
		countSpinner = new PSpinner(count, isDraft() && !CardUtil.isBasicLandName(cardName) ? 0 : 1, 99) {

			@Override
			protected void handleChange(int value) {
				if(!cardCombo.getTextField().isEditable()) {
					if(isDraft() && !CardUtil.isBasicLandName(cardName)) {
						// Removing from main deck
						if(!isSideboard()) {
							int deckCount = deck.getCount(cardName);
							int sbCount = view.getDeck().getSideboard().getCount(cardName);
							sbCount += deckCount - value;
							
							if(sbCount < 0) {
								countSpinner.setValue(deckCount);
								return;
							}
							
							view.setCardCount(deck, cardName, value);
							view.setCardCount(view.getDeck().getSideboard(), cardName, sbCount);
						// Removing from sideboard
						} else {
							int sbCount = deck.getCount(cardName);
							int deckCount = view.getDeck().getCount(cardName);
							deckCount += sbCount - value;

							if(deckCount < 0) {
								countSpinner.setValue(sbCount);
								return;
							}
							
							view.setCardCount(deck, cardName, value);
							view.setCardCount(view.getDeck(), cardName, deckCount);
						}
						view.rebuildDeckRows();
					}
					
					view.setCardCount(deck, cardName, value);
				}
			}
			
		};
		//countSpinner.setBorder(BorderFactory.createEmptyBorder());
		//countSpinner.setArrowColor(ColorUtil.DARK_GRAY_3);
		
		//countSpinner.setShowArrows(false);
		
		cardCombo = new AutoComboBox<String>() {

			@Override
			public Collection<String> getSearchCollection(String searchString) {
				if(isDraft()) {
					return CardUtil.getAllBasicLandNames();
				} 
				return CardUtil.getAllCardNames();
			}

			@Override
			public String toString(String searchedObject) {
				return searchedObject;
			}

			@Override
			public void handleFound(String cardName) {
				if(!getText().equals(cardName)) {
					cardCombo.getTextField().setText(cardName);
					updateComboColor();
				}
			}

			@Override
			public String buildTooltip(String selectedItem) {
				if(selectedItem == null) {
					selectedItem = getText();
				}
				
				//String tt = new Card(clean(selectedItem)).getToolTipText();
				Card card = new Card(clean(selectedItem));
				if(card.getCardInfo() == null) {
					String cardName = selectedItem;
					return "There is no card named \"" + cardName + "\"";
				}
				String tt = "<html>" + card.getImageHtml() + "</html>";
				updateColorLabelTooltip();
				return tt;
			}
			
        };
        cardCombo.getTextField().setText(cardName);
        cardCombo.setSelectedItem(cardName);
        cardCombo.updateTooltip();
        //cardCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        cardCombo.getTextField().getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateComboColor();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateComboColor();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateComboColor();
			}
        	
        });
        
        cardCombo.getTextField().setEditable(false);
        cardCombo.getTextField().setFocusable(false);
        cardCombo.setFocusable(false);
        
        Dimension dim = new Dimension(200,cardCombo.getPreferredSize().height);
        cardCombo.setPreferredSize(dim);
        cardCombo.setMaximumSize(dim);
        updateComboColor();
        
        c.insets(0, 0, 0, 10);
        add(removeButton, c);
        c.gridx++;
        c.strengthen();
        add(countSpinner, c);
        c.gridx++;
        add(cardCombo, c);
        c.gridx++;
        c.insets(0, 5);
        add(colorLabel, c);
        c.gridx++;
        if(otherDeckName != null) {
        	add(moveButton, c);
        	c.gridx++;
        }
        c.insets(0, 10);
        c.weaken();
        
        updateColorLabelTooltip();
	}
	
	public void updateColorLabelTooltip() {
		if(hasCard()) {
			Card card = getCard();
			if(card.getManaCost() != null) {
				colorLabel.setText("<html>" + ManaUtil.insertManaSymbols(ManaUtil.toManaHtml(getCard().getManaCost())) + "</html>");
			} else {
				colorLabel.setText("");
			}
		} else {
			//colorLabel.setToolTipText("There is no card named \"" + getText() + "\"");
		}
		
		// Super weird size fix, but technically works
		if(view != null && view.getParent() != null && view.getParent().getParent() != null && view.getParent().getParent().getParent() != null) {
			view.getParent().getParent().getParent().revalidate();
		}
	}
	
	public void hideColorLabel() {
		colorLabel.setVisible(false);
	}
	
	public boolean isDraft() {
		return view instanceof DraftEditorView;
	}
	
	public boolean isSideboard() {
		return !view.getDeck().equals(deck);
	}
	
	public JandorButton getRemoveButton() {
		return removeButton;
	}
	
	public JandorButton getMoveButton() {
		return moveButton;
	}
	
	public void remove() {
		if(isDraft() && !CardUtil.isBasicLandName(cardName)) {
			// Removing from main deck
			if(!isSideboard()) {
				int oldCount = deck.getCount(cardName);
				view.setCardCount(deck, cardName, 0);
				view.setCardCount(view.getDeck().getSideboard(), cardName, oldCount);
			// Removing from sideboard
			} else {
				int oldCount = deck.getCount(cardName);
				view.setCardCount(deck, cardName, 0);
				view.setCardCount(view.getDeck(), cardName, oldCount);
			}
			view.rebuildDeckRows();
		} else {
			view.setCardCount(deck, cardName, 0);
			view.rebuildDeckRows();
		}
	}
	
	public void moveToDeck() {
		if(otherDeck == null) {
			return;
		}
		int count = getCount();
		view.setCardCount(deck, cardName, 0);
		view.setCardCount(otherDeck, cardName, count);
		view.rebuildDeckRows();
		view.flagModified();
	}
	
	public AutoComboBox<String> getCardCombo() {
		return cardCombo;
	}
	
	private void updateComboColor() {
		if(hasCard()) {
			colorLabel.setBackground(ManaUtil.getColor(getCard()));
		} else {
			colorLabel.setBackground(ColorUtil.DARK_GRAY_3);
		}
		view.revalidate();
		updateColorLabelTooltip();
	}
	
	public int getCount() {
		return (Integer) countSpinner.getValue();
	}
	
	public Card getCard() {
		if(!hasCard()) {
			return null;
		}
		return new Card(getText());
	}
	
	public String clean(String text) {
		/*if(text == null) {
			return null;
		}
		text = text.toLowerCase().trim();
		String[] toks = text.split(" ");
		String cleanText = "";
		for(String tok : toks) {
			if(tok.length() < 2) {
				cleanText += tok.toUpperCase() + " ";
			} else {
				cleanText += tok.substring(0, 1).toUpperCase() + tok.substring(1) + " ";
			}
		}
		cleanText = cleanText.trim();
		return cleanText;*/
		String cardName = CardUtil.toCardName(text);
		if(cardName == null) {
			return text;
		}
		return cardName;
	}
	
	public String getText() {
		return clean(cardCombo.getTextField().getText());
	}
	
	public boolean hasCard() {
		return CardUtil.getCardInfo(getText()) != null;
	}
	
	public GlassPane buildGlassPane() {
		GlassPane gp = new GlassPane(this) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				removeButton.show();
				if(moveButton != null) {
					moveButton.show();
				}
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeButton.hide();
				if(moveButton != null) {
					moveButton.hide();
				}
				super.mouseExited(e);
			}
				
		};
		return gp;
	}
}
