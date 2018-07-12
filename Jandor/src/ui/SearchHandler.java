package ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JTextField;

import deck.Card;

public class SearchHandler<T> extends KeyAdapter {

	public static final int DEFAULT_RESULTS_LIMIT = 300;
	
	protected int resultsLimit = DEFAULT_RESULTS_LIMIT;
	protected int minSearchCharacters = 1;
	protected AutoComboBox<T> combo;
	protected String lastText = null;
	
	public SearchHandler(AutoComboBox<T> combo) {
		this.combo = combo;
	}
	
	public int getResultsLimit() {
		return resultsLimit;
	}

	public void setResultsLimit(int resultsLimit) {
		this.resultsLimit = resultsLimit;
	}

	public int getMinSearchCharacters() {
		return minSearchCharacters;
	}

	public void setMinSearchCharacters(int minSearchCharacters) {
		this.minSearchCharacters = minSearchCharacters;
	}

	/**
	 * Override this method to use a different match criteria then string ignore case
	 * @param searchedObject
	 * @param searchString Lowercase search string
	 * @return Whether this is a match.
	 */
	protected boolean matches(T searchedObject, String searchString, boolean startsWith) {
		return match(clean(combo.toString(searchedObject)), searchString, startsWith);
	}
	
	/**
	 * Override this to change how strings are matched, such as startsWith, endsWith, contains, ect...
	 */
	protected boolean match(String cleanString, String cleanSearchString, boolean startsWith) {
		if(startsWith) {
			return cleanString.startsWith(cleanSearchString);
		}
		return cleanString.contains(cleanSearchString);
	}
	
	/**
	 * Override this to change how strings are cleaned up for fair comparison
	 */
	protected String clean(String s) {
		if(s == null) {
			return s;
		}
		return s.toLowerCase();
	}
	
	public Vector<T> find(String searchString) {
		Vector<T> resultsContains = new Vector<T>();
		Vector<T> resultsStarts = new Vector<T>();
		if(searchString == null || searchString.length() < minSearchCharacters) {
			return resultsContains;
		}
		
		searchString = clean(searchString);
		for(T obj : combo.getSearchCollection(searchString)) {
			if(matches(obj, searchString, true)) {
				resultsStarts.add(obj);
			} else if(matches(obj, searchString, false)) {
				resultsContains.add(obj);
			}
			if(resultsStarts.size() == resultsLimit) {
				return resultsStarts;
			} 
			
			/*else if(resultsContains.size() == resultsLimit) {
				return resultsContains;
			} else if(resultsStarts.size() + resultsContains.size() == resultsLimit) {
				break;
			}*/
		}
		
		Vector<T> results = new Vector<T>(resultsLimit);
		results.addAll(resultsStarts);
		results.addAll(resultsContains);
		if(results.size() > resultsLimit) {
			results.trimToSize();
		}
		return results;
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		int code = key.getKeyCode();
		if(key.isActionKey() || code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_ALT || code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_META) {
			return;
		}
		
		String text = ((JTextField)key.getSource()).getText();
		if(!clean(text).equals(clean(lastText))) {
			lastText = text;
			combo.setItems(find(text));
			((JTextField) combo.getEditor().getEditorComponent()).setText(text);
			combo.updateTooltip();
		}
		
		if(combo.getItemCount() > 0) {
			if(code == KeyEvent.VK_ENTER) {
				if(combo.isPopupVisible()) {
					combo.hidePopup();
				}
				combo.handleFound((T) combo.getSelectedItem());
			} else if(!combo.isPopupVisible()) {
	        	combo.showPopup();
			}
		}
	}
}
	
