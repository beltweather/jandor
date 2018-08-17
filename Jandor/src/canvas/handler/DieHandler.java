package canvas.handler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import canvas.CardLayer;
import deck.Card;
import dice.Die;

@Deprecated
public abstract class DieHandler extends MouseHandler<CardLayer, Die> {

	public DieHandler(MouseHandlerManager manager, CardLayer layer) {
		super(Die.class, manager, layer);
	}

	@Override
	public void mouseClickedLeft(MouseEvent e, Die d) {
		if(hasSelected() && !e.isControlDown()) {
			clearSelected();
		}

		if(isSelected(d) && e.isControlDown()) {
			unselect(d);
		}

		if(e.isControlDown()) {
			select(d);
		} else if(d != null) {
			getLayer().handleMoved(Arrays.asList(d), false);
			getLayer().move(d, 0);
		}
		
		if(d != null) {
			d.increment();
			
			getLayer().handleMoved(Arrays.asList(d), false);
			getLayer().move(d, 0);
			
			e.consume();
		}
		
		repaint();
	}

	@Override
	public void mouseClickedRight(MouseEvent e, Die d) {
		if(d != null) {
			d.decrement();
			repaint();
			e.consume();
		}
	}

	@Override
	public void mouseClickedMiddle(MouseEvent e, Die d) {
		if(d == null) {
			return;
		}
		
		if(isSelected(d)) {
			for(Die die : getSelected()) {
				die.nextColor();
			}
		} else {
			d.nextColor();
		}
		repaint();
		e.consume();
	}

	@Override
	public void mouseStartDragLeft(MouseEvent e, Die d) {
		if(d != null) {
			d.getRenderer().rememberLastZoneType();
		}
		if(d == null || isDraggingObjectSelection()) {
			for(Die die : getSelected()) {
				die.getRenderer().rememberLastZoneType();
			}
		}
		getLayer().getShuffleGesture().addNewLocation(e);
		getLayer().getShuffleGesture().clear();
		if(dragObject != null && !isSelected(dragObject)) {
			e.consume();
		}
	}

	@Override
	public void mouseDraggedLeft(MouseEvent e) {
		if(isDragging()) {
			getLayer().getShuffleGesture().addNewLocation(e);
		}
	}

	@Override
	public void mouseDraggedOverRight(MouseEvent e, Die d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDraggedOverMiddle(MouseEvent e, Die d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseStopDragLeft(MouseEvent e, List<Die> dragObjects) {
		for(Die d : dragObjects) {
			d.showValue();
		}
		if(dragObjects.size() > 0) {
			getLayer().handleMoved(dragObjects, false);
		}
	}

	@Override
	public void moveObject(Die d, int idx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e, int code) {
		// TODO Auto-generated method stub
		
	}

}
