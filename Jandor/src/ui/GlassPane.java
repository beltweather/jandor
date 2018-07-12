package ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class GlassPane extends JComponent implements MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = 1L;
	
	private Container container;
	private Component lastComponent;

	public GlassPane(Container container) {
		super();
		this.container = container;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return container.getPreferredSize();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	private void redispatchMouseEvent(MouseEvent e) {
		Point glassPanePoint = e.getPoint();
		Container contentPane = container;
		GlassPane glassPane = this;
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, contentPane);
		
		if(containerPoint.x < 0) {
			containerPoint.x = 2;
		}
		
		if(containerPoint.x >= contentPane.getWidth()) {
			containerPoint.x = contentPane.getWidth() - 2;
		}
		
		if(containerPoint.y < 0) {
			containerPoint.y = 2;
		}
		
		if(containerPoint.y >= contentPane.getHeight()) {
			containerPoint.y = contentPane.getHeight() - 2;
		}
		
		if (containerPoint.y < 0) { //we're not in the content pane
		//Could have special code to handle mouse events over
		//the menu bar or non-system window decorations, such as
		//the ones provided by the Java look and feel.
		} else {
			//The mouse event is probably over the content pane.
			//Find out exactly which component it's over.
			Component component =
			SwingUtilities.getDeepestComponentAt(
			              container,
			              containerPoint.x,
			              containerPoint.y);
			
			if (component != null && !component.equals(this)) {
				
				//Forward events over the check box.
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
				
				if(e.getID() == MouseEvent.MOUSE_MOVED && !component.equals(lastComponent)) {
					
					Point lastComponentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, lastComponent);
					lastComponent.dispatchEvent(new MouseEvent(lastComponent,
		                           MouseEvent.MOUSE_EXITED,
		                           e.getWhen(),
		                           e.getModifiers(),
		                           lastComponentPoint.x,
		                           lastComponentPoint.y,
		                           e.getClickCount(),
		                           e.isPopupTrigger()));
					
					component.dispatchEvent(new MouseEvent(component,
	                           MouseEvent.MOUSE_ENTERED,
	                           e.getWhen(),
	                           e.getModifiers(),
	                           componentPoint.x,
	                           componentPoint.y,
	                           e.getClickCount(),
	                           e.isPopupTrigger()));
					
				} else {
					
					component.dispatchEvent(new MouseEvent(component,
					                           e.getID(),
					                           e.getWhen(),
					                           e.getModifiers(),
					                           componentPoint.x,
					                           componentPoint.y,
					                           e.getClickCount(),
					                           e.isPopupTrigger()));
				
				}
				
				lastComponent = component;
			
			}
		}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		redispatchMouseEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		redispatchMouseEvent(e);		
	}

}
