package accordion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ui.pwidget.ColorUtil;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;

public abstract class PAccordionButton extends PButton {

	private static final int DEFAULT_WIDTH = 30;
	private static final int TRIANGLE_SIDE = 10;

	protected PAccordionData accordionData;
	protected boolean expanded;
	private int lastDepth = -1;
	protected boolean markedForRemoval = false;
	
	protected PAccordionButton(PAccordionData accordionData) {
		super(accordionData.getFormattedText());
		this.accordionData = accordionData;
		expanded = accordionData.isDefaultExpanded();
		init();
	}
	
	private void init() {
		setCornerRadius(0);
		setMinimumSize(new Dimension(DEFAULT_WIDTH, 1));
		setMaximumSize(new Dimension(DEFAULT_WIDTH, 2000));
		setPreferredSize(new Dimension(DEFAULT_WIDTH, 1));
		setVerticalAlignment(JButton.TOP);
		
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				expanded = !expanded;
				PAccordionButton.this.actionPerformed(expanded);
			}
			
		});
		
	}
	
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public void updateColor() {
		if(lastDepth == -1 || lastDepth != accordionData.getDepth()) {
			Color bg = ColorUtil.DARK_GRAY_0;
			Color hg = ColorUtil.DARK_GRAY_1;
			Color pg = ColorUtil.DARK_GRAY_2;
			
			for(int i = 0; i < accordionData.getDepth(); i++) {
				bg = ColorUtil.brighter(bg);
				hg = ColorUtil.brighter(hg);
				pg = ColorUtil.brighter(pg);
			}
			
			setBackground(bg);
			setHoverBackground(hg);
			setPressedBackground(pg);
			
			lastDepth = accordionData.getDepth();
		}
	}
	
	public void setMarkedForRemoval(boolean markedForRemoval) {
		this.markedForRemoval = markedForRemoval;
	}
	
	public abstract void actionPerformed(boolean nowExpanded);
	
    @Override
    protected void paintComponent(Graphics g) {
    	updateColor();
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g;
    	g2.setColor(markedForRemoval ? ColorUtil.LIGHT_RED : Color.WHITE);
    	
    	int depthMargin = 0; // 29 * accordionData.getDepth();
    	
    	int t = TRIANGLE_SIDE;
    	int x = (DEFAULT_WIDTH - t) / 2;
    	int y = x + depthMargin;
    	/*if(!accordionData.isRemoveable()) {
    		y += DEFAULT_WIDTH;
    	}*/
    	Polygon p = new Polygon();
    	if(!expanded) {
    		p.addPoint(x, y);
    		p.addPoint(x + t, y);
    		p.addPoint(x + t/2, y + t);
    	} else {
    		p.addPoint(x, y);
    		p.addPoint(x + t, y + t/2);
    		p.addPoint(x, y + t);
    	}
    	
    	g2.fillPolygon(p);
    	
    	y = getHeight() - x; // - depthMargin;
    	p = new Polygon();
    	if(!expanded) {
    		p.addPoint(x, y);
    		p.addPoint(x + t, y);
    		p.addPoint(x + t/2, y - t);
    	} else {
    		p.addPoint(x, y);
    		p.addPoint(x + t, y - t/2);
    		p.addPoint(x, y - t);
    	}
    	
    	g2.fillPolygon(p);
    }
}
