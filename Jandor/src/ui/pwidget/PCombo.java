package ui.pwidget;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;


public abstract class PCombo extends JComboBox<String> {

	public static final String NONE = "";
	
	protected PPanel parentPanel;
	private boolean ignoreItemListener = false;
	private int preferredHeight = 20;

	public PCombo(PPanel parentPanel) {
		this(parentPanel, true);
	}
	
	public PCombo(PPanel parentPanel, boolean doInit) {
		super();
		this.parentPanel = parentPanel;
		if(doInit) {
			init();
		}
	}
	
	protected void init() {
		setUI(new ColorArrowUI());
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setBackground(ColorUtil.DARK_GRAY_0);
		setFocusable(false);

		fillWithDefaultItems();
		
		addItemListener(new ItemListener(){
			
			@Override
		    public void itemStateChanged(ItemEvent event) {
				if(ignoreItemListener) {
					return;
				}
				
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Object item = event.getItem();
					handleItemSelected(event, item);
				}
				repaintTeamView();
			}
		});
		
		
		addPopupMenuListener(new PopupMenuListener(){

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				repaintTeamView();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				repaintTeamView();
			}
			
		});
		setRenderer(createRenderer());
	}
	
	protected abstract void handleItemSelected(ItemEvent event, Object item);
	
	protected abstract ListCellRenderer<? super String> createRenderer();
	
	protected abstract void fillWithDefaultItems();
	
	public abstract void reset();

	private void repaintTeamView() {
		if(parentPanel == null /*|| parentPanel.getTeamView() == null */) {
			return;
		}
		/*parentPanel.getTeamView().repaint();*/
	}
	
	@Override
	public void hidePopup() {
		super.hidePopup();
		repaintTeamView();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int) super.getPreferredSize().getWidth(), (int) preferredHeight);
	}
	
	public void setPreferredWidth(int width) {
		super.setPreferredSize(new Dimension(width, (int) preferredHeight));
	}
	
	public static class ColorArrowUI extends BasicComboBoxUI {

	    @Override protected JButton createArrowButton() {
	    	PButton flatButton = new PButton();
	    	flatButton.setIcon(JUtil.getArrowIcon(7, 7, Color.WHITE, JUtil.DOWN));
	    	flatButton.setCornerRadius(0);
	    	flatButton.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.WHITE));
	    	flatButton.setFocusPainted(true);
	    	return flatButton;
	    }
	    
	    @Override
		protected ComboPopup createPopup() {
			return new CustomComboPopup(comboBox);
		}
	    
	}
	
	private static class CustomComboPopup extends BasicComboPopup {

		public CustomComboPopup(JComboBox combo) {
			super(combo);
		}

		@Override
		protected JScrollPane createScroller() {
		    return new PScrollPane(list);
		}
		
	}
	
	public void setIgnoreItemListener(boolean ignoreItemListener) {
		this.ignoreItemListener = ignoreItemListener;
	}
	
    public void setSelectedItem(Object object, boolean ignoreItemListener) {
    	boolean oldIgnoreItemListener = this.ignoreItemListener;
    	setIgnoreItemListener(ignoreItemListener);
    	setSelectedItem(object);
    	setIgnoreItemListener(oldIgnoreItemListener);
    }
    
 }
