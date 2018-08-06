package ui.pwidget;


import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

public class PPanel extends JPanel implements MouseListener /*implements ICreatedToolTip*/ {

	private static final long serialVersionUID = 1L;

	private Component tooltipPanel;
	private boolean showTooltip;
	private JToolTip tooltip;
	private ToolTipCreator creator; 
	
	public G c;
		
	public PPanel() {
		super(new GridBagLayout());
		setOpaque(false);
		setBorder(null);
		c = G.c();
		init();
		addMouseListener(this);
	}
	
	public PPanel(Component comp) {
		this();
		addc(comp);
	}
	
	private void init() {
		setToolTipText(null);
		showTooltip = false;
	}
	
	public void fill() {
		JUtil.fill(this, c);
	}
		
	public void clear() {
		removeAll();
		c = G.c();
	}
	
	public void addc(Component component) {
		add(component, c);
	}
	
	public void addcStrut() {
		addc(Box.createHorizontalStrut(1));
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	
	/*@Override
	public JToolTip createToolTip() {
		if(tooltip != null) {
			return tooltip;
		}
		
		if(!showTooltip || creator == null) {
			return new JToolTip();
		}
		
		tooltip = new JToolTip() {
			
			@Override
			public Dimension getPreferredSize() {
				if (getLayout() != null) {
			        return getLayout().preferredLayoutSize(this);
			    }
			    return super.getPreferredSize();
			}
			
		};
		
		tooltipPanel = creator.create();
		
		tooltip.setComponent(this);
		tooltip.setLayout(new GridBagLayout());
		tooltip.setBackground(new Color(0, 0, 0, 0));
		tooltip.setOpaque(false);
		tooltip.setBorder(null);
		tooltip.add(tooltipPanel, JUtil.gbc());
		
		return tooltip;
	}*/
	
	/*@Override
	public Point getToolTipLocation(MouseEvent e) {
		if(tooltipPanel == null) {
			return e.getPoint();
		}
		
		return JUtil.fitOnScreen(e, tooltipPanel);
	}*/
	
	/*public PPanel setShowTooltip(boolean showTooltip) {
		this.showTooltip = showTooltip;
		if(showTooltip) {
			setToolTipText("");
		} else {
			setToolTipText(null);
		}
		return this;
	}*/

	/*@Override
	public void setToolTipCreator(ToolTipCreator creator) {
		this.creator = creator;
		this.tooltip = null;
		this.setShowTooltip(creator != null);
	}

	@Override
	public ToolTipCreator getToolTipCreator() {
		return creator;
	}*/
	
	
	
}
