package draft;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PSimpleCombo;
import ui.pwidget.PSpinner;
import util.CardUtil;

public class SetPanel extends PPanel {

	protected PSimpleCombo setCombo;
	protected PSpinner setSpinner;
	

	public SetPanel() {
		this(3);
	}
	
	public SetPanel(int defaultPacks) {
		init(defaultPacks);
	}
	
	private void init(int defaultPacks) {
		final List<String> items = new ArrayList<String>();
		for(String value : CardUtil.getValues("set")) {
			items.add(CardUtil.getSetName(value));
		}
		Collections.sort(items);
		items.add(0, "");
		
		setCombo = new PSimpleCombo(items) {

			@Override
			protected void handleItemSelected(ItemEvent event, Object item) {
					
			}
				
		};
		
		setSpinner = new PSpinner(defaultPacks, 1, 99) {

			@Override
			protected void handleChange(int value) {
				
			}
			
		};
		
		c.weaken();
		c.anchor = G.NORTHEAST;
		addc(new JLabel("Set: "));
		c.gridx++;
		c.insets(0,5);
		addc(setCombo);
		c.insets(0,10);
		c.gridx++;
		addc(new JLabel("Packs: "));
		c.gridx++;
		c.insets(0,5);
		addc(setSpinner);
	}
	
	public String getSetName() {
		return setCombo.getSelectedItem().toString();
	}
	
	public int getPackCount() {
		return setSpinner.getIntValue();
	}
	
}
