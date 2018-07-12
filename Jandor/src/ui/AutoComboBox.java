package ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import ui.pwidget.ColorUtil;
import ui.pwidget.PScrollPane.PScrollBarUI;

public abstract class AutoComboBox<T> extends JComboBox<T> {

	private static final long serialVersionUID = 1L;
	
	protected SearchHandler<T> handler;
	
	public AutoComboBox() {
		this(null, true);
	}
	
	public AutoComboBox(SearchHandler<T> handler, boolean hideArrow) {
		super();
		setEditable(true);
		
		if(hideArrow) {
			setUI(new BasicComboBoxUI() {
	            @Override
	            protected JButton createArrowButton() {
	                return new JButton() {
	                    @Override
	                    public int getWidth() {
	                        return 0;
	                    }
	                };
	            }
	        });
		}
		
		remove(this.getComponent(0));
        setMaximumRowCount(20);
        setItems(new Vector<T>());
	    JTextField text = (JTextField) this.getEditor().getEditorComponent();
	    text.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	    text.setText("");
	    text.setFocusable(true);
	    this.handler = handler;
	    if(this.handler == null) {
	    	this.handler = new SearchHandler<T>(this);
	    }
	    text.addKeyListener(this.handler);
	    
	    setRenderer(new AutoListCellRenderer<T>(this));
	
	    addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	updateTooltip();
	        }
	    });
	    
	    addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				JComboBox comboBox = (JComboBox) e.getSource();
				Object popup = comboBox.getUI().getAccessibleChild(comboBox, 0);
				Component c = ((Container) popup).getComponent(0);
				if (c instanceof JScrollPane) {
					JScrollPane scrollpane = (JScrollPane) c;
					scrollpane.getVerticalScrollBar().setUI(new PScrollBarUI());
					scrollpane.getViewport().setBackground(ColorUtil.DARK_GRAY_3);
					scrollpane.getVerticalScrollBar().setUnitIncrement(32);
					setBorder(null);
				}
			}
	    	
	    });
	}
	
	public JTextField getTextField() {
		return (JTextField) this.getEditor().getEditorComponent();
	}
	
	public SearchHandler<T> getSearchHandler() {
		return handler;
	}
	
	public void setItems(Vector<T> items) {
		 setModel(new DefaultComboBoxModel<T>(items));
		 setSelectedIndex(-1);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateTooltip() {
		Object obj = getSelectedItem();
		if(obj == null) {
			setToolTipText(null);
			return;
		}
		
		Class klass = (Class<T>)
                ((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
		if(klass.isAssignableFrom(obj.getClass())) {
			setToolTipText(buildTooltip((T) obj));
		} else {
			setToolTipText(null);
		}
	} 
	
	public abstract String buildTooltip(T selectedItem);
	
	public abstract Collection<T> getSearchCollection(String searchString);
	
	public abstract String toString(T searchedObject); 
	
	public abstract void handleFound(T found);
	
}
