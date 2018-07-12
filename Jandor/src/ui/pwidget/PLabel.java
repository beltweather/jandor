package ui.pwidget;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;




public class PLabel extends JLabel /*implements ICreatedToolTip*/ {

	private Component tooltipPanel;
	private boolean showTooltip;
	private JToolTip tooltip;
	private ToolTipCreator creator;
	
	public PLabel() {
		super();
		init();
	}
	
	public PLabel(Icon icon) {
		super(icon);
		init();
	}
		
	public PLabel(Icon icon, int horizontalAlignment) {
		super(icon, horizontalAlignment);
		init();
	}

	public PLabel(String text) {
		super(text);
		init();
	}

	public PLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		init();
	}
	
	public PLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		init();
	}

	private void init() {
		setToolTipText("");
		showTooltip = true;
	}
	
	/*@Override
	public JToolTip createToolTip() {
		if(tooltip != null) {
			return tooltip;
		}
		
		if(!showTooltip || creator == null) {// || tooltipPanel == null) {
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
	}
	
	@Override
	public Point getToolTipLocation(MouseEvent e) {
		if(tooltipPanel == null) {
			return e.getPoint();
		}
		
		return JUtil.fitOnScreen(e, tooltipPanel);
	}
	
	public PLabel setShowTooltip(boolean showTooltip) {
		this.showTooltip = showTooltip;
		if(showTooltip) {
			setToolTipText("");
		} else {
			setToolTipText(null);
		}
		return this;
	}

	@Override
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
