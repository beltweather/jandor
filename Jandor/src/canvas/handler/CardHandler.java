package canvas.handler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.ShuffleUtil;
import zone.ZoneManager;
import zone.ZoneType;
import canvas.CardLayer;
import deck.Card;

@Deprecated
public class CardHandler extends MouseHandler<CardLayer, Card> {

	public CardHandler(MouseHandlerManager manager, CardLayer layer) {
		super(Card.class, manager, layer);
	}
	
	@Override
	public List<Card> getObjects() {
		return getLayer().getAllCards();
	}

	@Override
	public void mouseClickedLeft(MouseEvent e, Card c) {
		if(hasSelected() && !e.isControlDown()) {
			clearSelected();
			repaint();
			return;
		}
		
		if(isSelected(c) && e.isControlDown()) {
			unselect(c);
			repaint();
			return;
		}
		if(e.isControlDown()) {
			select(c);
		} else if(c != null) {
			getLayer().handleMoved(Arrays.asList(c), false);
			getLayer().move(c, 0);
		}
		repaint();
	}

	@Override
	public void mouseClickedRight(MouseEvent e, Card c) {
		if(c == null) {
			repaint();
			return;
		}

		if(isSelected(c)) {
			int tappedCount = 0;
			int untappedCount = 0;
			for(Card s : getSelected()) {
				if(s.getZoneType() == ZoneType.BATTLEFIELD) {
					if(s.isTapped()) {
						tappedCount++;
					} else {
						untappedCount++;
					}
				}
			}
			boolean tap = tappedCount < untappedCount;
			for(Card s : getSelected()) {
				if(s.getZoneType() == ZoneType.BATTLEFIELD) {
					s.setTapped(tap);
				}
			}
			List<Card> selected = new ArrayList<Card>(getSelected());
			ShuffleUtil.positionSort(selected);
			for(Card s : selected) {
				getLayer().move(s, 0);
			}
		} else if(c.getZoneType() == ZoneType.BATTLEFIELD) {
			c.setTapped(!c.isTapped());
		}
		repaint();
	}

	@Override
	public void mouseClickedMiddle(MouseEvent e, Card c) {
		if(c == null) {
			repaint();
			return;
		}
		
		if(isSelected(c)) {
			for(Card s : getSelected()) {
				s.toggleFaceUp();
			}
		} else {
			c.toggleFaceUp();
		}
		repaint();
	}

	@Override
	public void mouseStartDragLeft(MouseEvent e, Card card) {
		if(card != null) {
			card.rememberLastZoneType();
		}
		if(card == null || isDraggingObjectSelection()) {
			for(Card c : getSelected()) {
				c.rememberLastZoneType();
			}
		}
		getLayer().getShuffleGesture().clear();
		getLayer().getShuffleGesture().addNewLocation(e);
		if(dragObject != null && !isSelected(dragObject)) {
			e.consume();
		}
	}
	
	@Override
	public void mouseDraggedOverRight(MouseEvent e, Card c) {
		if(c != null && c.getZoneType() == ZoneType.BATTLEFIELD) {
			c.setTapped(!c.isTapped());
		}		
	}

	@Override
	public void mouseDraggedOverMiddle(MouseEvent e, Card c) {
		if(c != null && c.getZoneType() != ZoneType.GRAVEYARD) {
			c.setFaceUp(!c.isFaceUp());
		}
	}

	@Override
	public void mouseStopDragLeft(MouseEvent e, List<Card> dragCards) {
		ShuffleUtil.positionSort(dragCards);
		for(Card c : dragCards) {
			getLayer().move(c, 0);
		}
		if(dragCards.size() > 0) {
			getLayer().handleMoved(dragCards, false);
		}
		e.consume();
	}

	@Override
	public void mouseDraggedLeft(MouseEvent e) {
		if(isDragging()) {
			getLayer().getShuffleGesture().addNewLocation(e);	
		}
	}

	@Override
	public void moveObject(Card card, int index) {
		getLayer().move(card, index);
	}

	@Override
	public void keyPressed(KeyEvent e, int code) {
		switch(code) {
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			if(hasSelected()) {
				for(Card c : getSelected()) {
					getLayer().remove(c);
				}
				clearSelected();
				repaint();
			}
			break;
		case KeyEvent.VK_S:
			getLayer().spin(isDragging() ? getDragged() : getSelected(), 360);
			break;
		case KeyEvent.VK_R:
			getLayer().rotate(10);
			break;
		case KeyEvent.VK_E:
			getLayer().rotate(-10);
			break;
		case KeyEvent.VK_A:
			if(e.isControlDown()) {
				clearSelected();
				for(Card card : new ArrayList<Card>(getLayer().getAllCards())) {
					select(card);
				}
				repaint();
			}
			break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, Card obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e, Card obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e, Card obj) {
		// TODO Auto-generated method stub
		
	}


}
