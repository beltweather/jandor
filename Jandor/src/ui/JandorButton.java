package ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import ui.pwidget.PButton;

public class JandorButton extends PButton implements MouseListener {
	
	protected float alpha = 1.0f;
	protected boolean showOnHoverOnly = false;
	protected boolean showTextAlways = false;

	public JandorButton() {
		this("");
	}
	
	public JandorButton(String text) {
		super(text);
		addMouseListener(this);
	}
	
	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public void hide() {
		alpha = 0;
		
		if(showTextAlways) {
			setShowBackground(false);
		}
		
		repaint();
	}
	
	public void show() {
		alpha = 1f;
		
		if(showTextAlways) {
			setShowBackground(true);
		}
		
		repaint();
	}
	
	public void setShowOnHoverOnly(boolean showOnHoverOnly) {
		this.showOnHoverOnly = showOnHoverOnly;
		if(showOnHoverOnly) {
			hide();
		} else {
			show();
		}
	}
	
	public boolean isShowTextAlways() {
		return showTextAlways;
	}

	public void setShowTextAlways(boolean showTextAlways) {
		this.showTextAlways = showTextAlways;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, showTextAlways ? 1.0f : alpha));
		super.paintComponent(g);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		if(showOnHoverOnly) {
			show();
		}
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if(showOnHoverOnly) {
			hide();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		show();
	}
	
}
