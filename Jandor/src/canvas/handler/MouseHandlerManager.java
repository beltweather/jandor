package canvas.handler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MouseHandlerManager implements MouseListener, MouseMotionListener, KeyListener, Serializable {

	private static final long serialVersionUID = 1L;

	private List<MouseHandler> handlers = new ArrayList<MouseHandler>();
	
	public MouseHandlerManager() {}
	
	public void add(MouseHandler handler) {
		if(handler == null) {
			return;
		}
		if(!handlers.contains(handler)) {
			handlers.add(handler);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.keyTyped(e);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.keyPressed(e);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.keyReleased(e);
			}
		}		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseDragged(e);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseMoved(e);
			}
		}		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseClicked(e);
			}
		}		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			handler.findDragObject(e);
		}	
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mousePressed(e);
			} else {
				handler.dragObject = null;
			}
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseReleased(e);
			}
		}		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseEntered(e);
			}
		}		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		for(MouseHandler handler : handlers) {
			if(!e.isConsumed()) {
				handler.mouseExited(e);
			}
		}		
	}
	
	public boolean isDragging() {
		for(MouseHandler handler : handlers) {
			if(handler.isDraggingObject()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDraggingSelection() {
		for(MouseHandler handler : handlers) {
			if(handler.isDraggingObjectSelection()) {
				return true;
			}
		}
		return false;
	}
}
