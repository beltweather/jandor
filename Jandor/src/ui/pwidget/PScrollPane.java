package ui.pwidget;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;



public class PScrollPane extends JScrollPane {


	public PScrollPane(JComponent view) {
		this(view, null);
	}
	
	public PScrollPane(JComponent view, Dimension preferredSize) {
		super(view, 
			  ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
			  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		init();
		if(preferredSize != null) {
			setPreferredSize(preferredSize);
		}
	}
	
	private void init() {
		getVerticalScrollBar().setUI(new PScrollBarUI());
		getHorizontalScrollBar().setUI(new PScrollBarUI(true));
		getViewport().setBackground(ColorUtil.DARK_GRAY_3);
		getVerticalScrollBar().setUnitIncrement(32);
		setBorder(null);
		
		addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			
			public void ancestorResized(HierarchyEvent e) {
				/*PScrollBarUI ui = (PScrollBarUI) getVerticalScrollBar().getUI();
				boolean hasThumb = ui.hasThumb();
				if(hasThumb == ui.isFaded()) {
					getVerticalScrollBar().setUI(new PScrollBarUI(!hasThumb));
					validate();
				}*/
				
				PScrollBarUI ui = (PScrollBarUI) getHorizontalScrollBar().getUI();
				boolean hasThumb = ui.hasThumb();
				if(hasThumb == ui.isFaded()) {
					getHorizontalScrollBar().setUI(new PScrollBarUI(!hasThumb));
					validate();
				}
			}
		
		});
	}
	
	public static class PScrollBarUI extends BasicScrollBarUI {
		
		protected boolean faded;
		
		public PScrollBarUI() {
			this(false);
		}
		
		public PScrollBarUI(boolean faded) {
			super();
			this.faded = faded;
		}
		
		public boolean isFaded() {
			return faded;
		}
		
		public boolean hasThumb() {
			return getThumbBounds().width > 0;
		}
		
		@Override
		protected JButton createDecreaseButton(int orientation) {
			int dir = scrollbar.getOrientation() == JScrollBar.VERTICAL ? JUtil.UP : JUtil.LEFT;
			
			Color c = faded ? Color.GRAY : Color.WHITE;
			
			PButton flatButton = new PButton();
			flatButton.setIcon(JUtil.getArrowIcon(7, 7, c, dir));
			flatButton.setCornerRadius(0);
			flatButton.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, c));
			flatButton.setPreferredSize(new Dimension(15, 15));
			flatButton.setFocusPainted(false);
			if(faded) {
				flatButton.setHoverBackground(flatButton.getBackground());
				flatButton.setPressedBackground(flatButton.getBackground());
				flatButton.setGradientType(ColorUtil.GRAD_NONE);
			}
			
			return flatButton;
		}

		@Override
		protected JButton createIncreaseButton(int orientation) {
			int dir = scrollbar.getOrientation() == JScrollBar.VERTICAL ? JUtil.DOWN : JUtil.RIGHT;
			
			Color c = faded ? Color.GRAY : Color.WHITE;
			
			PButton flatButton = new PButton();
			flatButton.setIcon(JUtil.getArrowIcon(7, 7, c, dir));
			flatButton.setCornerRadius(0);
			flatButton.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, c));
			flatButton.setPreferredSize(new Dimension(15, 15));
			flatButton.setFocusPainted(false);
			if(faded) {
				flatButton.setHoverBackground(flatButton.getBackground());
				flatButton.setPressedBackground(flatButton.getBackground());
				flatButton.setGradientType(ColorUtil.GRAD_NONE);
			}
			
			return flatButton;
		}

		@Override
		protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
			super.paintThumb(g, c, thumbBounds);
	
			if(thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
				return;
			}
			
			int w = thumbBounds.width;
			int h = thumbBounds.height;
	
			g.translate(thumbBounds.x, thumbBounds.y);
	
			g.setColor(thumbColor);
			if(isThumbRollover()) {
				g.setColor(g.getColor().brighter());
			}
	
			ColorUtil.gradient(g, ColorUtil.GRAD_HORZ, g.getColor(), g.getColor().darker(), w, h);
			g.fillRect(0, 0, w-1, h-1);
	
			((Graphics2D) g).setPaint(null);
	
			g.setColor(thumbDarkShadowColor);
			g.drawRect(0, 0, w-1, h-1);
			g.translate(-thumbBounds.x, -thumbBounds.y);
		}
		
	}
	
}
