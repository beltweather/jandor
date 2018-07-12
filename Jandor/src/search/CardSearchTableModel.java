package search;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;

import ui.pwidget.PTable;
import ui.pwidget.PTableKey;
import ui.pwidget.PTableModel;
import deck.Card;
import deck.Deck;

public class CardSearchTableModel extends PTableModel {
	
	protected Deck deck;
	
	public CardSearchTableModel() {
		super();
		setData(new Deck());
	}
	
	public Deck getDeck() {
		return deck;
	}
	
	@Override
	public void setData(Object rawData) {
		this.deck = (Deck) rawData;
		
		int size = Math.min(5000, deck.size());
		
		Object[][] data = new Object[size][];
		int cols = 3;
		
		Object[] e = new Object[cols];
		
		int i = 0;
		for(Card card : deck) {
			card.getToolTipLabel();
			
			e = new Object[cols]; 
			e[0] = card;
			e[1] = card;
			e[2] = card;
			//e[3] = card;
			data[i++] = e;
			if(i >= size) {
				break;
			}
		}
		
		List<Object> colDesc = new ArrayList<Object>();
		colDesc.add("Card");
		colDesc.add("Text");
		colDesc.add("Add");
		//colDesc.add("Casting Cost");
		//colDesc.add("Card Set");
		setDataVector(data, colDesc.toArray());
	}

	@Override
	public TableCellRenderer createRenderer() {
		return new CardSearchTableRenderer();
	}

	@Override
	public PTableKey createTableKey() {
		PTableKey key = new PTableKey();
		return key;
	}

	@Override
	public JComponent createTooltipContent(PTable table, int row, int column) {
		return null;
	}
	
}
