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

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import ui.AutoComboBox;
import ui.JandorCombo;
import ui.ProgressBar.ProgressTask;
import ui.pwidget.PSimpleCombo;
import ui.pwidget.PTextField;
import util.CardUtil;
import deck.Card;
import deck.Deck;

public abstract class CardSearchPanel extends SearchPanel<JSONObject, Deck> {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
	static {
		
		DEFAULT_FIELDS.add("");
		DEFAULT_FIELDS.add("general");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("name"); 
		DEFAULT_FIELDS.add("text");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("supertypes");
		DEFAULT_FIELDS.add("types");
		DEFAULT_FIELDS.add("subtypes");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("colors");
		DEFAULT_FIELDS.add("cmc");
		DEFAULT_FIELDS.add("X or *");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("power");
		DEFAULT_FIELDS.add("toughness");
		DEFAULT_FIELDS.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELDS.add("set");
		DEFAULT_FIELDS.add("colorIdentity");
		DEFAULT_FIELDS.add("rarity");
		DEFAULT_FIELDS.add("flavor");
	}
	
	private static final List<String> DEFAULT_FIELD_NAMES = new ArrayList<String>();
	static {
		
		DEFAULT_FIELD_NAMES.add("");
		DEFAULT_FIELD_NAMES.add("All Text");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Name"); 
		DEFAULT_FIELD_NAMES.add("Ability Text");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Supertype");
		DEFAULT_FIELD_NAMES.add("Type");
		DEFAULT_FIELD_NAMES.add("Subtype");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Colors");
		DEFAULT_FIELD_NAMES.add("Mana Cost");
		DEFAULT_FIELD_NAMES.add("Mana X or *");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Power");
		DEFAULT_FIELD_NAMES.add("Toughness");
		DEFAULT_FIELD_NAMES.add(JandorCombo.SEPARATOR);
		DEFAULT_FIELD_NAMES.add("Set");
		DEFAULT_FIELD_NAMES.add("Color Identity");
		DEFAULT_FIELD_NAMES.add("Rarity");
		DEFAULT_FIELD_NAMES.add("Flavor");
	}
	
	@Override
	protected boolean match(JSONObject info, String att, JComponent editor) throws Exception {
		att = DEFAULT_FIELDS.get(DEFAULT_FIELD_NAMES.indexOf(att));
		
		if(att.equals("")) {
			return true;
		}
		
		if(att.equals("set")) {
			att = "printings";
		}
		
		if(info.has("types")) {
			JSONArray types = info.getJSONArray("types");
			if(types.length() == 0) {
				return false;
			}
			Set<String> allTypes = CardUtil.getValues("types");
			for(int i = 0; i < types.length(); i++) {
				if(!allTypes.contains(types.getString(i))) {
					return false;
				}
			}
		} else {
			return false;
		}
		
		if(!att.equals("printings")) {
			JSONArray sets = info.getJSONArray("printings");
			for(int i = 0; i < sets.length(); i++) {
				String set = sets.getString(i);
				if(set.equals("UNG") || set.equals("UNH") || set.equals("UGL")) {
					return false;
				}
			}
		}
		
		if(att.equals("power") || att.equals("toughness") || att.equals("cmc")) {
			return matchInt(info, att, editor);
		} else if(att.equals("name") || att.equals("subtype") || att.equals("text") || att.equals("flavor")) {
			return matchString(info, att, editor);
		} else if(att.equals("types") || att.equals("supertypes") || att.equals("subtypes") || att.equals("rarity") || att.equals("set") || att.equals("printings")) {
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
	
	private boolean matchStringGeneral(JSONObject info, String att, JComponent editor) throws JSONException {
		//String jsonValue = info.toString().toLowerCase();
		//String formattedValue = new Card(info.getString("name")).getToolTipText(0, false).toLowerCase();
		String formattedValue = new Card(info.getString("name")).getSearchableString();
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
			Pattern p = Pattern.compile(tok);
			return matchToken(tok, p, fullText);
		} else {
			return matchToken(tok, null, fullText);
		}
	}
	
	private boolean matchToken(String tok, Pattern pattern, String... fullText) {
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
		Matcher m = Pattern.compile("('.+?'|\".+?\"|[^\"]\\S*)\\s*").matcher(rawText);
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
	
	private boolean matchXOrStar(JSONObject info, String att, JComponent editor) throws JSONException {
		if(!info.has(att)) {
			return ((XOrStarPanel) editor).match("");
		}
		return ((XOrStarPanel) editor).match(info.getJSONArray("manaCost").toString());
	}
	
	private boolean matchColor(JSONObject info, String att, JComponent editor) throws JSONException {
		String colors = info.has(att) ? info.getJSONArray(att).toString() : "";
		String manaCost = info.has("manaCost") ? info.getString("manaCost") : "";
		return ((ManaPanel) editor).match(colors, manaCost);
	}
	
	private boolean matchSet(JSONObject info, String att, JComponent editor) throws JSONException {
		if(((JComboBox) editor).getSelectedItem() == null || ((JComboBox) editor).getSelectedItem().equals("")) {
			return true;
		}
		
		JSONArray set = info.has(att) ? info.getJSONArray(att) : new JSONArray();
		String value = unfilter(att, ((JComboBox) editor).getSelectedItem().toString()).toLowerCase();
		if(value.equals("")) {
			return true;
		}
		
		for(int i = 0; i < set.length(); i++) {
			if(set.getString(i).toLowerCase().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean matchString(JSONObject info, String att, JComponent editor) throws JSONException {
		String value = info.has(att) ? info.getString(att).toLowerCase() : "";
		List<String> editorText = getMatchableTokens((PTextField) editor); 
		for(String s : editorText) {
			if(!matchToken(s, value)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean matchInt(JSONObject info, String att, JComponent editor) throws JSONException {
		if(!info.has(att)) {
			return ((NumberPanel) editor).match(0);
		}
		Object val = info.get(att);
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
		int intVal = Integer.valueOf(val.toString());
		return ((NumberPanel) editor).match(intVal);
	}

	@Override
	protected Deck search(SearchNode<JSONObject> rootNode, ProgressTask task) {
		Deck deck = new Deck();
		try {
			List<String> cardNames = CardUtil.getAllCardNames();
			int total = cardNames.size();
			int i = 0;
			for(String cardName : cardNames) {
				JSONObject info = CardUtil.getCardInfo(cardName);
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
		
		if(att.equals("power") || att.equals("toughness") || att.equals("cmc")) {
			editor = new NumberPanel();
			((NumberPanel) editor).getSpinner().getTextField().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					search();
				}
				
			});
		} else if(att.equals("name") || att.equals("subtype") || att.equals("text") || att.equals("flavor") || att.equals(" ") || att.equals("general")){
			editor = new PTextField();
			((PTextField) editor).addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					search();
				}
				
			});
		} else if(att.equals("types") || att.equals("supertypes") || att.equals("subtypes") || att.equals("rarity") || att.equals("set")) {
			final List<String> items = new ArrayList<String>();
			for(String value : CardUtil.getValues(att)) {
				items.add(filter(att, value));
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

