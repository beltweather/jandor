package analysis;

import java.util.ArrayList;
import java.util.List;

import run.Jandor;

import util.CardUtil;
import util.ManaUtil;
import deck.Card;

public class LandModel {

	private Card card;
	private List<String> mana;
	private List<String> manaPayment;
	
	public LandModel(Card card) {
		this.card = card;
		init();
	}
	
	private void init() {
		mana = new ArrayList<String>();
		manaPayment = new ArrayList<String>();
		if(!ManaUtil.isManaCard(card)) {
			return;
		}
		
		String rawManaText = ManaUtil.getManaText(card);
		String rawManaPayText = ManaUtil.getPayForManaText(card);
		
		if(!rawManaText.contains(";")) {
			mana.add(rawManaText);
			manaPayment.add(rawManaPayText.equals("_") ? "" : rawManaPayText);
			return;
		}
		
		String[] manaToks = rawManaText.split(";");
		String[] payToks = rawManaPayText.split(";");
		
		if(manaToks.length != payToks.length) {
			System.err.println("Did not detect the same amount of mana lines: " + card.getName() + " " + card.getText());
			return;
		}
		
		for(String m : manaToks) {
			mana.add(m);
		}
		
		for(String p : payToks) {
			manaPayment.add(p.equals("_") ? "" : p);
		}
	}
	
	public int getManaSourceCount() {
		return mana.size();
	}
	
	public String getManaSource(int i) {
		return mana.get(i);
	}
	
	public boolean hasManaSourcePayment(int i) {
		return !manaPayment.get(i).isEmpty();
	}
	
	public String getManaSourcePayment(int i) {
		return manaPayment.get(i);
	}

	public boolean isBasic() {
		return card.getText() == null;
	}
	
	public boolean isColorless() {
		return card.getColorIdentity() == null;
	}
	
	public boolean isNormal() {
		return getManaSourceCount() > 0;
	}
	
	public boolean isComplex() {
		return !isNormal() && !isBasic();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(!isNormal()) {
			if(isBasic()) {
				if(isColorless()) {
					sb.append("{c} (" + card.getName() + ")");
				} else {
					sb.append(card.getColorIdentity().toString().replace("[", "").replace("]", "") + " (" + card.getName() + ")");
				}
			} else {
				sb.append(card.getName() + " is not a typical mana source.\n");
				sb.append(card.getText());
			}
			return sb.toString();
		}
		
		for(int i = 0; i < getManaSourceCount(); i++) {
			String m = getManaSource(i);
			String p = getManaSourcePayment(i);
			if(i != 0) {
				sb.append("; ");
			}
			if(p.isEmpty()) {
				sb.append(m);
			} else {
				sb.append("pay " + p + " -> " + m);
			}
 			
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Jandor.init();
		for(String cardName : CardUtil.getAllCardNames()) {
			Card card = new Card(cardName);
			if(!card.isLand()) {
				continue;
			}
			LandModel model = new LandModel(card);
			System.out.println(model.toString());
		}
	}
	
}
