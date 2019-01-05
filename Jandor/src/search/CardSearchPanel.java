 package search;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import deck.Card;
import deck.Deck;
import jackson.AllCardsJson.CardJson;
import ui.AutoComboBox;
import ui.JandorCombo;
import ui.ProgressBar.ProgressTask;
import ui.pwidget.PSimpleCombo;
import ui.pwidget.PTextField;
import util.CardUtil;
import util.MtgJsonUtil;

public abstract class CardSearchPanel extends SearchPanel<CardJson, Deck> {

	private static final long serialVersionUID = 1L;

	private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
	static {

		DEFAULT_FIELDS.add("");
		DEFAULT_FIELDS.add("general");
		DEFAULT_FIELDS.add("name");
		DEFAULT_FIELDS.add("text");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("supertypes");
		DEFAULT_FIELDS.add("types");
		DEFAULT_FIELDS.add("subtypes");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("colors");
		DEFAULT_FIELDS.add("colorIdentity");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add(MtgJsonUtil.cmc);
		DEFAULT_FIELDS.add("X or *");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("power");
		DEFAULT_FIELDS.add("toughness");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("set");
		DEFAULT_FIELDS.add("rarity");
		DEFAULT_FIELDS.add(MtgJsonUtil.flavorText);
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("layout");
	}

	private static final List<String> DEFAULT_FIELD_NAMES = new ArrayList<String>();
	static {

		DEFAULT_FIELD_NAMES.add("");
		DEFAULT_FIELD_NAMES.add("All Text");
		DEFAULT_FIELD_NAMES.add("Name Text");
		DEFAULT_FIELD_NAMES.add("Ability Text");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Supertype");
		DEFAULT_FIELD_NAMES.add("Type");
		DEFAULT_FIELD_NAMES.add("Subtype");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Colors");
		DEFAULT_FIELD_NAMES.add("Color Identity");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Mana Cost");
		DEFAULT_FIELD_NAMES.add("Mana X or *");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Power");
		DEFAULT_FIELD_NAMES.add("Toughness");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Set");
		DEFAULT_FIELD_NAMES.add("Rarity");
		DEFAULT_FIELD_NAMES.add("Flavor Text");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Layout");
	}

	private static Pattern caseInsensitivePattern(String regex) {
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	protected List<Card> cardsToSearch;

	public CardSearchPanel() {
		this(null);
	}

	public CardSearchPanel(List<Card> cardsToSearch) {
		this.cardsToSearch = cardsToSearch;
	}

	public void setCardsToSearch(List<Card> cardsToSearch) {
		this.cardsToSearch = cardsToSearch;
	}

	@Override
	protected boolean match(CardJson info, String att, JComponent editor) throws Exception {
		att = DEFAULT_FIELDS.get(DEFAULT_FIELD_NAMES.indexOf(att));

		if(att.equals("")) {
			return true;
		}

		if(att.equals("set")) {
			att = "printings";
		}

		if(info.types != null) {
			List<String> types = info.types;
			if(types.size() == 0) {
				return false;
			}
			Set<String> allTypes = CardUtil.getValues("types");
			for(String type : types) {
				if(!allTypes.contains(type)) {
					return false;
				}
			}
		} else {
			return false;
		}

		if(!att.equals("printings")) {
			List<String> sets = info.printings;
			boolean isSilly = false;
			boolean isAlsoNotSilly = false;
			for(String set : sets) {
				if(set.equals("UNG") || set.equals("UNH") || set.equals("UGL") || set.equals("UST")) {
					isSilly = true;
				} else {
					isAlsoNotSilly = true;
				}
			}

			if(isSilly && !isAlsoNotSilly) {
				return false;
			}
		}

		if(att.equals("power") || att.equals("toughness") || att.equals(MtgJsonUtil.cmc)) {
			return matchInt(info, att, editor);
		} else if(att.equals("name") || att.equals("subtype") || att.equals("text") || att.equals(MtgJsonUtil.flavorText)) {
			return matchString(info, att, editor);
		} else if(att.equals("types") || att.equals("supertypes") || att.equals("subtypes") || att.equals("rarity") || att.equals("set") || att.equals("printings") || att.equals("layout")) {
			return matchSet(info, att, editor);
		} else if(att.equals("colors") || att.equals("colorIdentity")) {
			return matchColor(info, att, editor);
		} else if(att.equals("X or *")) {
			return matchXOrStar(info, att, editor);
		} else if(att.equals("general")) {
			return matchStringGeneral(info, att, editor);
		}

		return true;
	}

	private boolean matchStringGeneral(CardJson info, String att, JComponent editor) {
		//String jsonValue = info.toString().toLowerCase();
		//String formattedValue = new Card(info.getString("name")).getToolTipText(0, false).toLowerCase();
		String formattedValue = new Card(info.name).getSearchableString();
		List<String> editorText = getMatchableTokens((PTextField) editor);
		for(String s : editorText) {
			if(!matchToken(s, formattedValue)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchToken(String tok, String... fullText) {
		if(tok.startsWith("'") && tok.endsWith("'")) {
			tok = tok.substring(1, tok.length() - 1);
			Pattern p = caseInsensitivePattern(tok);
			return matchToken(tok, p, fullText);
		} else {
			return matchToken(tok, null, fullText);
		}
	}

	private boolean matchToken(String tok, Pattern pattern, String... fullText) {
		if(tok != null) {
			tok = tok.toLowerCase();
		}
		if(pattern != null) {
			if(fullText.length == 0) {
				return false;
			}
			for(String v : fullText) {
				Matcher m = pattern.matcher(v);
				if(m.find()) {
					return true;
				}
			}
			return false;
		} else if(tok.startsWith("!")) {
			tok = tok.substring(1, tok.length());
			if(fullText.length == 0) {
				return true;
			}

			for(String v : fullText) {
				if(v.contains(tok)) {
					return false;
				}
			}
			return true;
		} else {
			if(fullText.length == 0) {
				return false;
			}
			for(String v : fullText) {
				if(v.contains(tok)) {
					return true;
				}
			}
			return false;
		}
	}

	private List<String> getMatchableTokens(PTextField textField) {
		return getMatchableTokens(textField.getText().toLowerCase());
	}

	private List<String> getMatchableTokens(String rawText) {
		List<String> list = new ArrayList<String>();
		Matcher m = caseInsensitivePattern("('.+?'|\".+?\"|[^\"]\\S*)\\s*").matcher(rawText);
		while (m.find()) {
			String s = m.group(1);
			if(s.startsWith("\"") && s.endsWith("\"")) {
				s = s.substring(1, s.length() - 1);
			} else if(s.startsWith("'") && s.endsWith("'")) {
				s = s.substring(0, s.length());
			}
			list.add(s);
		}
		return list;
	}

	private boolean matchXOrStar(CardJson info, String att, JComponent editor) {
		String value = info.getString(att);
		if(value == null) {
			return ((XOrStarPanel) editor).match("");
		}
		return ((XOrStarPanel) editor).match(value); // XXX This method was really weird before.
	}

	private boolean matchColor(CardJson info, String att, JComponent editor) {
		String colors = info.colors == null ? "" : StringUtils.join(info.colors, ",");
		String manaCost = info.manaCost == null ? "" : info.manaCost;
		return ((ManaPanel) editor).match(colors, manaCost);
	}

	private boolean matchSet(CardJson info, String att, JComponent editor) {
		Object selectedItem = ((JComboBox) editor).getSelectedItem();
		if(selectedItem == null || selectedItem.equals("")) {
			return true;
		}

		Object obj = info.get(att);
		List set = null;
		if(obj == null) {
			set = new ArrayList<>();
		} else if(obj instanceof String) {
			set = new ArrayList<>();
			set.add(obj);
		} else if(obj instanceof List) {
			set = (List) obj;
		} else {
			return false;
		}

		String value = unfilter(att, ((JComboBox) editor).getSelectedItem().toString()).toLowerCase();
		if(value.equals("")) {
			return true;
		}

		for(Object o : set) {
			if(String.valueOf(o).toLowerCase().equals(value)) {
				return true;
			}
		}
		return false;
	}

	private boolean matchString(CardJson info, String att, JComponent editor) {
		String value = info.getString(att);
		if(value == null) {
			value = "";
		} else {
			value = value.toLowerCase();
		}
		List<String> editorText = getMatchableTokens((PTextField) editor);
		for(String s : editorText) {
			if(!matchToken(s, value)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchInt(CardJson info, String att, JComponent editor) {
		String value = info.getString(att);
		if(value == null) {
			return ((NumberPanel) editor).match(0);
		}

		// Special case for split cards and converted manacost
		if(att.equals(MtgJsonUtil.cmc) && CardUtil.isSplit(info)) {
			return matchSplitCmc(info, editor);
		}

		return ((NumberPanel) editor).match(toInt(value));
	}

	private boolean matchSplitCmc(CardJson info, JComponent editor) {
		List<String> names = info.names;
		int allCmc = 0;
		for(String name : names) {
			CardJson cardInfo = CardUtil.getCardInfo(name);
			int cmc = (int) cardInfo.convertedManaCost;

			// Allow ourselves to match on either individual cards in
			// the split card, in addition to the some of the two cards
			if(((NumberPanel) editor).match(cmc)) {
				return true;
			}

			allCmc += cmc;
		}
		return ((NumberPanel) editor).match(allCmc);
	}

	private int toInt(Object val) {
		if(val == null) {
			return 0;
		}
		if(val instanceof String) {
			if(val.equals("*")) {
				val = 0;
			} else {

				if(((String) val).contains("-")) {
					val = ((String) val).replace("-", "");
				}

				if(((String) val).contains("+")) {
					val = ((String) val).replace("+", "");
				}

				if(((String) val).contains("*")) {
					val = ((String) val).replace("*", "");
				}

			}
		}
		int intVal = (int) Double.valueOf(val.toString()).doubleValue();
		return intVal;
	}

	@Override
	protected Deck search(SearchNode<CardJson> rootNode, ProgressTask task) {
		Deck deck = new Deck();
		try {
			List<String> cardNames;
			if(cardsToSearch != null) {
				cardNames = new ArrayList<String>();
				for(Card card : cardsToSearch) {
					cardNames.add(card.getName());
				}
			} else {
				cardNames = CardUtil.getAllCardNames();
			}
			int total = cardNames.size();
			int i = 0;
			for(String cardName : cardNames) {
				CardJson info = CardUtil.getCardInfo(cardName);
				if(rootNode.evaluate(info)) {
					deck.add(new Card(cardName));
				}
				task.setWorkerProgress(i++, total);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deck;
	}

	@Override
	public JComponent buildEditor(String att) {
		att = DEFAULT_FIELDS.get(DEFAULT_FIELD_NAMES.indexOf(att));

		JComponent editor;

		if(att.equals("power") || att.equals("toughness") || att.equals(MtgJsonUtil.cmc)) {
			editor = new NumberPanel();
			((NumberPanel) editor).getSpinner().getTextField().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					search();
				}

			});
		} else if(att.equals("name") || att.equals("subtype") || att.equals("text") || att.equals(MtgJsonUtil.flavorText) || att.equals(" ") || att.equals("general")){
			editor = new PTextField();
			((PTextField) editor).addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					search();
				}

			});
		} else if(att.equals("types") || att.equals("supertypes") || att.equals("subtypes") || att.equals("rarity") || att.equals("set") || att.equals("layout")) {
			final List<String> items = new ArrayList<String>();
			for(String value : CardUtil.getValues(att)) {
				items.add(StringUtils.capitalize(filter(att, value)));
			}
			Collections.sort(items);
			items.add(0, "");

			if(att.equals("subtypes")) {


		        final AutoComboBox<String> searchCombo = new AutoComboBox<String>() {

					@Override
					public Collection<String> getSearchCollection(String searchString) {
						return items;
					}

					@Override
					public String toString(String searchedObject) {
						return searchedObject;
					}

					@Override
					public void handleFound(String cardName) {

					}

					@Override
					public String buildTooltip(String selectedItem) {
						return null;
					}

		        };
		        searchCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		        searchCombo.setMaximumRowCount(20);
		        searchCombo.getSearchHandler().setMinSearchCharacters(0);

		        searchCombo.getTextField().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						search();
					}

				});

		        editor = searchCombo;

			} else {

				editor = new PSimpleCombo(items) {

					@Override
					protected void handleItemSelected(ItemEvent event, Object item) {

					}

				};

			}
		} else if(att.equals("colors") || att.equals("colorIdentity")) {
			editor = new ManaPanel();
			//editor.setPreferredSize(new Dimension(375, 20));
			return editor;
			//editor = new JCheckBox("Colors");
			//editor.setOpaque(false);
			//editor.setForeground(Color.WHITE);
		} else if(att.equals("X or *")) {
			editor = new XOrStarPanel();
		} else {
			return new JLabel("");
		}

		editor.setPreferredSize(new Dimension(200, 20));
		return editor;
	}

	private String filter(String att, String value) {
		if(att.equals("set") || att.equals("printings")) {
			return CardUtil.getSetName(value);
		}
		return value;
	}

	private String unfilter(String att, String value) {
		if(att.equals("set") || att.equals("printings")) {
			return CardUtil.getSetId(value);
		}
		return value;
	}

	@Override
	public List<String> getAttributes() {
		return DEFAULT_FIELD_NAMES;
	}

	@Override
	public String getDefaultAttribute() {
		return "";
	}


	@Override
	public void setToDefaultSingle() {
		rootNode.setChildren(null);
		rootNode.addChild();
		rootNode.getChildren().get(0).getAttributeCombo().setSelectedItem("All Text");
	}

	@Override
	public void setToDefaultMulti() {
		rootNode.setChildren(null);

		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();
		rootNode.addChild();

		int i = 0;
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Name");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("All Text");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Supertype");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Type");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Subtype");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Colors");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Mana Cost");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Power");
		rootNode.getChildren().get(i++).getAttributeCombo().setSelectedItem("Toughness");
	}

}

