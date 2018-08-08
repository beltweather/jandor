package canvas.handler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.ShuffleUtil;
import zone.ZoneType;
import canvas.CardLayer;
import canvas.IRenderable;
import canvas.IRenderer;
import deck.Card;
import dice.Counter;
import dice.D10;
import dice.Die;
import dice.Token;

public class RenderableHandler extends MouseHandler<CardLayer, IRenderable> {

	public RenderableHandler(MouseHandlerManager manager, CardLayer layer) {
		super(manager, layer);
	}
	
	public boolean canEdit() {
		return !layer.isOpponentView();
	}

	@Override
	public List<IRenderable> getObjects() {
		return getLayer().getAllObjects();
	}
	
	@Override
	public List<IRenderable> getViewOrderedObjects() {
		List<IRenderable> objects = new ArrayList<IRenderable>();
		objects.addAll(layer.getCounters());
		objects.addAll(layer.getD10s());
		objects.addAll(layer.getTokens());
		objects.addAll(layer.getAllCards());
		objects.addAll(layer.getExtraRenderables());
		return objects;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(!canEdit()) {
			return;
		}
		
		super.mousePressed(e);
	}

	@Override
	public void findDragObject(MouseEvent e) {
		if(!canEdit()) {
			return;
		}
		
		super.findDragObject(e);
	}
	
	@Override
	public void mouseClickedLeft(MouseEvent e, IRenderable c) {
		if(!canEdit()) {
			return;
		}
		
		if(hasSelected() && !e.isControlDown() && (c == null || !isSelected(c))) {
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
			if(c instanceof Die) {
				if(isSelected(c)) {
					for(IRenderable r : getSelected()) {
						if(r instanceof Token) {
							((Token) r).increment();
						} else if(r instanceof Die) {
							((Die) r).increment();
						}
					}
				} else {
					if(c instanceof Token) {
						((Token) c).handleLeftClick(e.getPoint());
					} else if(c instanceof Die) {
						((Die) c).increment();
					}
				}
			}
			
			if(e.getClickCount() > 1) {
				getLayer().handleDoubleLeftClick(c);
			}
		}
		repaint();
	}

	@Override
	public void mouseClickedRight(MouseEvent e, IRenderable c) {
		if(!canEdit()) {
			return;
		}
		
		if(c == null) {
			repaint();
			return;
		}

		if(isSelected(c)) {
			int tappedCount = 0;
			int untappedCount = 0;
			for(IRenderable s : getSelected()) {
				if(s instanceof Card || s instanceof Token) {
					if(s.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
						if(s.getRenderer().isTapped()) {
							tappedCount++;
						} else {
							untappedCount++;
						}
					}
				} else {
					((Die) s).decrement();
				}
			}
			boolean tap = tappedCount < untappedCount;
			for(IRenderable s : getSelected()) {
				if(s instanceof Card || s instanceof Token) {
					if(s.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
						s.getRenderer().setTapped(tap);
					}
				}
			}
			List<IRenderable> selected = new ArrayList<IRenderable>(getSelected());
			ShuffleUtil.positionSort(selected);
			for(IRenderable s : selected) {
				getLayer().move(s, 0);
			}
		} else {
			
			if(c instanceof Card || c instanceof Token) {
				if(c.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
					c.getRenderer().setTapped(!c.getRenderer().isTapped());
				}
			} else {
				
				((Die) c).decrement();
			}
		}
		
		getLayer().flagChange();
		repaint();
	}

	@Override
	public void mouseClickedMiddle(MouseEvent e, IRenderable c) {
		if(!canEdit()) {
			return;
		}
		
		if(c == null) {
			repaint();
			return;
		}
		
		if(isSelected(c)) {
			for(IRenderable s : getSelected()) {
				if(s instanceof Card) {
					s.getRenderer().toggleFaceUp();
				} else {
					((Die) s).nextColor();
				}
			}
		} else {
			if(c instanceof Card) {
				c.getRenderer().toggleFaceUp();
			} else {
				((Die) c).nextColor();
			}
		}

		getLayer().flagChange();
		repaint();
	}

	@Override
	public void mouseStartDragLeft(MouseEvent e, IRenderable card) {
		if(!canEdit()) {
			return;
		}
		
		if(card != null) {
			card.getRenderer().rememberLastZoneType();
		}
		if(card == null || isDraggingObjectSelection()) {
			for(IRenderable c : getSelected()) {
				c.getRenderer().rememberLastZoneType();
			}
		}
		getLayer().getShuffleGesture().clear();
		getLayer().getShuffleGesture().addNewLocation(e);
	}
	
	@Override
	public void mouseDraggedOverRight(MouseEvent e, IRenderable c) {
		if(!canEdit()) {
			return;
		}
		
		if(c != null && c.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
			c.getRenderer().setTapped(!c.getRenderer().isTapped());
			getLayer().flagChange();
		}		
	}

	@Override
	public void mouseDraggedOverMiddle(MouseEvent e, IRenderable c) {
		if(!canEdit()) {
			return;
		}
		
		if(c != null && c.getRenderer().getZoneType() != ZoneType.GRAVEYARD) {
			c.getRenderer().setFaceUp(!c.getRenderer().isFaceUp());
			getLayer().flagChange();
		}
	}

	@Override
	public void mouseStopDragLeft(MouseEvent e, List<IRenderable> dragCards) {
		if(!canEdit()) {
			return;
		}
		
		ShuffleUtil.positionSort(dragCards);
		for(IRenderable c : dragCards) {
			getLayer().move(c, 0);
			if(c instanceof Die && !(c instanceof Token)) {
				Die die = (Die) c;
				die.showValue();
				
				if(die.getRenderer().hasParent() && !die.getRenderer().getParent().overlaps(die.getRenderer().getLocation())) {
					die.getRenderer().removeFromParent();
				}
				
				for(Die token : layer.getTokens()) {
					if(token.getRenderer().getZoneType() == ZoneType.BATTLEFIELD && token.getRenderer().overlaps(die.getRenderer().getLocation())) {
						token.getRenderer().addChild(die.getRenderer());
						break;
					}
				}
				
				for(Card card : layer.getAllCards()) {
					if(card.getZoneType() == ZoneType.BATTLEFIELD && card.getRenderer().overlaps(die.getRenderer().getLocation())) {
						card.addChild(die.getRenderer());
						break;
					}
				}
			} else if(c instanceof Card || c instanceof Token) {
				if(c.getRenderer().getZoneType() == ZoneType.BATTLEFIELD) {
					List<IRenderer> dice = new ArrayList<IRenderer>(c.getRenderer().getChildren());
					for(IRenderer die : dice) {
						if(!c.getRenderer().overlaps(die.getLocation())) {
							die.removeFromParent();
						}
					}
					
					int index = getIndex(c);
					for(Die die : layer.getD10s()) {
						if((!die.getRenderer().hasParent() || getIndex((IRenderable) die.getRenderer().getParent().getObject()) > index) && c.getRenderer().overlaps(die.getRenderer().getLocation())) {
							c.getRenderer().addChild(die.getRenderer());
							break;
						}
					}
					
					for(Die die : layer.getCounters()) {
						if((!die.getRenderer().hasParent() || getIndex((IRenderable) die.getRenderer().getParent().getObject()) > index) && c.getRenderer().overlaps(die.getRenderer().getLocation())) {
							c.getRenderer().addChild(die.getRenderer());
							break;
						}
					}
					
					for(Die die : layer.getTokens()) {
						if((!die.getRenderer().hasParent() || getIndex((IRenderable) die.getRenderer().getParent().getObject()) > index) && c.getRenderer().overlaps(die.getRenderer().getLocation())) {
							c.getRenderer().addChild(die.getRenderer());
							break;
						}
					}
				}
			}
		}
		if(dragCards.size() > 0) {
			getLayer().handleMoved(dragCards, false);
			getLayer().flagChange();
		}
	}
	
	private int getIndex(IRenderable obj) {
		int diceSize = layer.getD10s().size() + layer.getCounters().size();
		if(obj instanceof Card) {
			return layer.getAllCards().indexOf(obj) + diceSize + layer.getTokens().size();
		} else if(obj instanceof Token) {
			return layer.getTokens().indexOf(obj) + diceSize;
		}
		return -1;
	}

	@Override
	public void mouseDraggedLeft(MouseEvent e) {
		if(!canEdit()) {
			return;
		}
		
		if(isDragging()) {
			getLayer().getShuffleGesture().addNewLocation(e);	
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e, IRenderable obj) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e, IRenderable obj) {
		obj.getRenderer().setHovered(true);
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e, IRenderable obj) {
		obj.getRenderer().setHovered(false);
		repaint();
	}

	@Override
	public void moveObject(IRenderable card, int index) {
		if(!canEdit()) {
			return;
		}
		
		getLayer().move(card, index);
	}

	@Override
	public void keyPressed(KeyEvent e, int code) {
		if(!canEdit()) {
			return;
		}
		
		switch(code) {
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			if(hasSelected() && !isDragging()) {
				for(IRenderable c : getSelected()) {
					if(c instanceof Die && c.getRenderer().getZoneType() == ZoneType.GRAVEYARD) {
						continue;
					}
					getLayer().remove(c);
				}
				clearSelected();
				repaint();
			}
			break;
		case KeyEvent.VK_A:
			if(e.isControlDown()) {
				clearSelected();
				for(IRenderable card : new ArrayList<IRenderable>(getLayer().getAllObjects())) {
					select(card);
				}
				repaint();
			}
			break;
		case KeyEvent.VK_S:
			layer.shuffleCards(getSelectedCards(), false, false);
			List<Die> dice = getDraggedDice();
			for(Die die : dice) {
				if(die instanceof D10) {
					die.roll();
				} else if(die instanceof Counter) {
					die.roll(1,2);
				}
			}
			break;
		}
		
	}
	
	public List<Card> getDraggedCards() {
		List<Card> cards = new ArrayList<Card>();
		for(IRenderable obj : getDragged()) {
			if(obj instanceof Card) {
				cards.add((Card) obj);
			}
		}
		return cards;
	}
	
	public List<Card> getSelectedCards() {
		List<Card> cards = new ArrayList<Card>();
		for(IRenderable obj : getSelected()) {
			if(obj instanceof Card) {
				cards.add((Card) obj);
			}
		}
		return cards;
	}
	
	public List<Die> getDraggedDice() {
		List<Die> dice = new ArrayList<Die>();
		for(IRenderable obj : getDragged()) {
			if(obj instanceof Die) {
				dice.add((Die) obj);
			}
		}
		return dice;
	}
	
	public List<Die> getSelectedDice() {
		List<Die> dice = new ArrayList<Die>();
		for(IRenderable obj : getSelected()) {
			if(obj instanceof Die) {
				dice.add((Die) obj);
			}
		}
		return dice;
	}
	
}
