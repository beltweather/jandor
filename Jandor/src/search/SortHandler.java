package search;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import ui.pwidget.ColorUtil;
import ui.pwidget.PCombo.ColorArrowUI;
import ui.pwidget.PPanel;
import util.ShuffleType;

public abstract class SortHandler extends PPanel {

	private static final long serialVersionUID = 1L;
	
	public static List<ShuffleType> DEFAULT_SHUFFLE_TYPES = new ArrayList<ShuffleType>();
	static {
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.AZ);
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.ZA);
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.MANA_LH);
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.MANA_HL);
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.RARITY_LH);
		DEFAULT_SHUFFLE_TYPES.add(ShuffleType.RARITY_HL);
	}

	protected List<ShuffleType> shuffleTypes;
	protected JComboBox<ShuffleType> fieldCombo;
	protected ShuffleType defaultShuffleType;
	protected String labelText;

	public SortHandler(String labelText) {
		this(null, labelText);
	}
	
	public SortHandler(ShuffleType defaultShuffleType, String labelText) {
		this(DEFAULT_SHUFFLE_TYPES, defaultShuffleType, labelText);
	}
	
	public SortHandler(List<ShuffleType> fields, ShuffleType defaultShuffleType, String labelText) {
		this.shuffleTypes = fields;
		this.defaultShuffleType = defaultShuffleType;
		this.labelText = labelText;
		init();
	}
	
	protected void init() {
		fieldCombo = new JComboBox<ShuffleType>();
		fieldCombo.setUI(new ColorArrowUI());
		fieldCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		fieldCombo.setBackground(ColorUtil.DARK_GRAY_0);
		fieldCombo.setFocusable(false);
		
		for(ShuffleType type : shuffleTypes) {
			fieldCombo.addItem(type);
		}
		
		if(defaultShuffleType != null) {
			fieldCombo.setSelectedItem(defaultShuffleType);
		}
		
		fieldCombo.addItemListener(new ItemListener(){
			
			@Override
		    public void itemStateChanged(ItemEvent event) {
				handleSort((ShuffleType) fieldCombo.getSelectedItem());
			}
			
		});
		
		JLabel label = new JLabel(labelText + " ");
		label.setForeground(Color.WHITE);
		
		add(label, c);
		c.gridx++;
		add(fieldCombo, c);
	}
	
	public ShuffleType getShuffleType() {
		return (ShuffleType) fieldCombo.getSelectedItem();
	}
	
	protected abstract void handleSort(ShuffleType field);
	
}
