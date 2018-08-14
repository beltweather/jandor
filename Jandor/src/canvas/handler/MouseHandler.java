package canvas.handler;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.MouseUtil;
import zone.ZoneManager;
import zone.ZoneType;
import canvas.Canvas;
import canvas.CardLayer;
import canvas.CardRenderer;
import canvas.ICanvasLayer;
import canvas.IRenderable;
import canvas.IRenderer;
import canvas.Location;
import deck.Card;

public abstract class MouseHandler<L extends ICanvasLayer, T extends IRenderable> implements MouseListener, MouseMotionListener, KeyListener, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int DRAG_MODE_CARD = 0;
	public static final int DRAG_MODE_SELECT = 1;
	
	protected T dragOverObject = null;
	protected T dragObject = null;
	protected Location lastDragEnd = null;
	protected Location dragEnd = null;
	protected Location dragStart = null;
	
	protected List<T> selectedObjects;
	protected int dragMode;
	protected boolean enableObjectDragging;
	protected boolean enableDragSelect;
	
	protected T lastMousedObject = null;
	
	protected MouseHandlerManager manager;
	protected L layer;
	
	public MouseHandler(MouseHandlerManager manager, L layer) {
		super();
		this.manager = manager;
		this.layer = layer;
		selectedObjects = new ArrayList<T>();
		dragMode = DRAG_MODE_CARD;
		enableObjectDragging = true;
		enableDragSelect = true;
	}
	
	public Canvas getCanvas() {
		return layer.getCanvas();
	}
	
	public L getLayer() {
		return layer;
	}
	
	public ZoneManager getZoneManager() {
		return layer.getCardZoneManager();
	}
	
	public void repaint() {
		getCanvas().repaint();
	}
	
	// Override methods
	
	public abstract List<T> getObjects();
	
	public List<T> getViewOrderedObjects() {
		return getObjects();
	}

	public abstract void mouseClickedLeft(MouseEvent e, T obj);
	
	public abstract void mouseClickedRight(MouseEvent e, T obj);
	
	public abstract void mouseClickedMiddle(MouseEvent e, T obj);
	
	public abstract void mouseStartDragLeft(MouseEvent e, T obj);

	public abstract void mouseDraggedLeft(MouseEvent e);

	public abstract void mouseDraggedOverRight(MouseEvent e, T obj);
	
	public abstract void mouseDraggedOverMiddle(MouseEvent e, T obj);
	
	public abstract void mouseStopDragLeft(MouseEvent e, List<T> dragObjects);
	
	public abstract void moveObject(T obj, int idx);
	
	public abstract void keyPressed(KeyEvent e, int code);
	
	public abstract void mouseMoved(MouseEvent e, T obj);

	public abstract void mouseEntered(MouseEvent e, T obj);

	public abstract void mouseExited(MouseEvent e, T obj);
	
	// MOUSE METHODS
	
	public void findDragObject(MouseEvent e) {
		setDragMode(e);
		
		if(MouseUtil.isLeft(e)) {
			T obj = find(e);
			if(obj == null || obj.getRenderer().isCanDrag()) {
				dragObject = obj;
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(isDragging()) {
			return;
		} 
		
		T obj = find(e);
		if(MouseUtil.isRight(e)) {
			mouseClickedRight(e, obj);
			return;
		} else if(MouseUtil.isMiddle(e)) {
			mouseClickedMiddle(e, obj);
			return;
		} else if(MouseUtil.isLeft(e)) {
			mouseClickedLeft(e, obj);
		}
	
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setDragMode(e);
		dragOverObject = null;
		
		if(MouseUtil.isLeft(e)) {
			dragObject = find(e);
			
			boolean managerDraggingSelected = manager.isDraggingSelection();
			
			if(dragObject == null && managerDraggingSelected) {
				mouseStartDragLeft(e, null);
			} else if(dragObject != null) {
				if(dragObject.getRenderer().isCanDrag()) {
					mouseStartDragLeft(e, dragObject);
				} else {
					dragObject = null;
				}
			}
			
			if(dragObject == null && !manager.isDragging() && !managerDraggingSelected && MouseUtil.isLeft(e) && enableDragSelect) {
				dragMode = DRAG_MODE_SELECT;
				dragStart = new Location(e.getX(), e.getY()); //inverse(new Location(e.getX(), e.getY()));
				for(T obj : getSelected()) {
					obj.getRenderer().rememberLastZoneType();
				}
			} else if(!enableObjectDragging && dragMode == DRAG_MODE_CARD) {
				dragMode = -1;
				dragObject = null;
			}
		}
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		stopDrag(e);
	}
	
	public void stopDrag() {
		stopDrag(null, null);
	}
	
	public void stopDrag(MouseEvent e) {
		stopDrag(e, null);
	}
	
	public void stopDrag(ZoneType zoneOverride) {
		stopDrag(null, zoneOverride);
	}
	
	public void stopDrag(MouseEvent e, ZoneType zoneOverride) {
		if(!isDragging() && !manager.isDraggingSelection()) {
			dragObject = null;
			dragStart = null;
			lastDragEnd = null;
			dragEnd = null;
			dragOverObject = null;
			return;
		}
		
		if(dragEnd != null) {
			
			List<T> dragObjects = new ArrayList<T>(getDragged());
			ZoneManager zoneManager = getLayer().getCardZoneManager();
			if(zoneOverride != null) {
				zoneManager.clearZones();
				for(T obj : dragObjects) {
					zoneManager.getZone(zoneOverride).add(obj);
					obj.getRenderer().setZoneType(zoneOverride);
				}
			} else {
				zoneManager.setZones(getCanvas(), null, false, getObjects());
			}
			mouseStopDragLeft(e, dragObjects);
		}
		
		if(e != null && dragMode == DRAG_MODE_SELECT) {
			selectFromDrag(e.isControlDown());
		} 
		
		dragObject = null;
		dragStart = null;
		lastDragEnd = null;
		dragEnd = null;
		dragOverObject = null;
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
		getCanvas().requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if(lastMousedObject != null) {
			mouseExited(e, lastMousedObject);
			lastMousedObject = null;
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		lastDragEnd = dragEnd;
		/*if(dragObject != null && !dragObject.getRenderer().isTransformedProjection()) {
			dragEnd = new Location(e.getX(), e.getY());
		} else {
			dragEnd = inverse(new Location(e.getX(), e.getY()));
		}*/
		dragEnd = new Location(e.getX(), e.getY());
		
		mouseDraggedLeft(e);
		updateDraggedObjectLocations();
		
		T obj = find(e);
		if(obj != dragOverObject) {
			if(MouseUtil.isRight(e)) {
				mouseDraggedOverRight(e, obj);
			} else if(MouseUtil.isMiddle(e)) {
				mouseDraggedOverMiddle(e, obj);
			}
			dragOverObject = obj;
		}
		
		repaint();
	}

	private boolean hideTooltip(T obj) {
		if(!obj.getRenderer().isFaceUp() || obj.getRenderer().getZoneType() == ZoneType.HAND) {
			return true;
		}
		if(layer instanceof CardLayer && obj instanceof Card && obj.getRenderer() instanceof CardRenderer) {
			return ((CardRenderer) obj.getRenderer()).hideCard((CardLayer) layer, (Card) obj);
		}
		return false;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		T obj = find(e);
		if(obj != null && !hideTooltip(obj)) {
			getCanvas().setToolTipText(obj.getToolTipText());
		} else {
			getCanvas().setToolTipText(null);
		}
		
		if(obj != lastMousedObject) {
			if(lastMousedObject != null) {
				mouseExited(e, lastMousedObject);
			}
			if(obj != null) {
				mouseEntered(e, obj);
			}
			lastMousedObject = obj;
		}
		
		mouseMoved(e, obj);
	}
	
	// KEYBOARD METHODS

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keyPressed(e, e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	
	// SEARCH METHODS
	
	public T find(MouseEvent e) {
		T obj = find(e, false);
		if(obj != null) {
			return obj;
		}
		return find(e, true);
	}
	
	public T find(MouseEvent e, boolean transformed) {
		Location location = new Location(e);
		Location inverseLocation = inverse(location);
		for(T obj : getViewOrderedObjects()) {
			if(obj.getRenderer().getZoneType().isTransformedProjection() && obj.getRenderer().overlaps(inverseLocation)) {
				return obj;
			} else if(!obj.getRenderer().getZoneType().isTransformedProjection() && obj.getRenderer().overlaps(location)) {
				return obj;
			}
		}
		return null;
	}
	
	public Location inverse(Location location) {
		return getCanvas().getZoom().inverseTransform(location);
	}
	
	// SELECT METHODS
	
	public boolean isSelected(T obj) {
		return selectedObjects.contains(obj);
	}
	
	public T select(T obj) {
		if(obj != null) {
			selectedObjects.add(obj);
			moveObject(obj, 0);
		}
		return obj;
	}
	
	public void unselect(T obj) {
		if(selectedObjects.contains(obj)) {
			selectedObjects.remove(obj);
		}
	}
	
	public boolean hasSelected() {
		return selectedObjects.size() > 0;
	}
	
	public List<T> getSelected() {
		return selectedObjects;
	}
	
	public void clearSelected() {
		selectedObjects.clear();
	}
	
	protected void selectFromDrag(boolean addToSelection) {
		if(dragStart == null || dragEnd == null) {
			return;
		}
		
		int x0 = dragStart.getScreenX();
		int y0 = dragStart.getScreenY();
		int x1 = dragEnd.getScreenX();
		int y1 = dragEnd.getScreenY();
		
		if(x0 > x1) {
			int temp = x0;
			x0 = x1;
			x1 = temp;
		}
		
		if(y0 > y1) {
			int temp = y0;
			y0 = y1;
			y1 = temp;
		}
		
		if(!addToSelection) {
			clearSelected();
		}
		
		Rectangle selectBox = new Rectangle(x0, y0, x1 - x0, y1 - y0);
		Rectangle tSelectBox = getCanvas().getZoom().inverseTransform(selectBox).getBounds();
		for(T obj : getObjects()) {
			IRenderer r = obj.getRenderer();
			
			if(r instanceof Card && r.getZoneType() == ZoneType.HAND && selectBox.intersects(r.getBounds().getBounds())) {
				selectedObjects.add(obj);
			} else if(r.isTransformedProjection() && tSelectBox.contains(r.getBounds().getBounds())) {
				selectedObjects.add(obj);
			} else if(!r.isTransformedProjection() && selectBox.contains(r.getBounds().getBounds())) {
				selectedObjects.add(obj);
			}
		}
		
	}
	
	public void setDragMode(MouseEvent e) {
		dragMode = DRAG_MODE_CARD;
	}
	
	public boolean isDragging() {
		if(dragMode == DRAG_MODE_SELECT && dragStart != null) {
			return true;
		}
		return dragObject != null && dragEnd != null;
	}
	
	public boolean isDragged(T obj) {
		if(!isDraggingObject()) {
			return false;
		}
		if(isDraggingObjectSelection() && isSelected(obj)) {
			return true;
		}
		if(dragObject.equals(obj)) {
			return true;
		}
		return false;
	}

	private void updateDraggedObjectLocations() {
		boolean isDraggingCard = isDraggingObject();
		boolean isDraggingSelection = isDraggingObjectSelection() || manager.isDraggingSelection();
		int	dragOffsetX = getDragScreenOffsetX();
		int	dragOffsetY = getDragScreenOffsetY();
		int tDragOffsetX = getTransformedDragScreenOffsetX();
		int tDragOffsetY = getTransformedDragScreenOffsetY();
		
		if(!isDraggingCard && !isDraggingSelection) {
			return;
		}
		
		for(T obj : getObjects()) {
			if(obj.equals(dragObject) || (isDraggingSelection && isSelected(obj))) {
				IRenderer r = obj.getRenderer();
				boolean transformed = r.isTransformedProjection();
				
				int x = r.getScreenX() + (transformed ? tDragOffsetX : dragOffsetX);
				int y = r.getScreenY() + (transformed ? tDragOffsetY : dragOffsetY);
				
				r.setScreenX(x);
				r.setScreenY(y);
				
				if(r.isTransformedProjection() && !r.getZoneType().isTransformedProjection()) {
					r.setLocation(getCanvas().getZoom().transform(r.getLocation()));
					r.setTransformedProjection(false);
				} else if(!r.isTransformedProjection() && r.getZoneType().isTransformedProjection()) {
					r.setLocation(getCanvas().getZoom().inverseTransform(r.getLocation()));
					r.setTransformedProjection(true);
				}
				
				if(r.hasChildren()) {
					for(Object o : r.getChildren()) {
						IRenderer child = (IRenderer) o;
						if(!isDragged((T) child.getObject())) {
							x = child.getScreenX() + (transformed ? tDragOffsetX : dragOffsetX);
							y = child.getScreenY() + (transformed ? tDragOffsetY : dragOffsetY);
							
							child.setScreenX(child.getScreenX() + dragOffsetX);
							child.setScreenY(child.getScreenY() + dragOffsetY);
							
							if(child.isTransformedProjection() && !child.getZoneType().isTransformedProjection()) {
								child.setLocation(getCanvas().getZoom().transform(child.getLocation()));
								child.setTransformedProjection(false);
							} else if(!child.isTransformedProjection() && child.getZoneType().isTransformedProjection()) {
								child.setLocation(getCanvas().getZoom().inverseTransform(child.getLocation()));
								child.setTransformedProjection(true);
							}
						}
					}
				}
				
			}
		}
		
		if(getLayer() instanceof CardLayer) {
			((CardLayer) getLayer()).flagChange();
		}
	}
	
	public boolean isDraggingObject() {
		return dragMode == DRAG_MODE_CARD && dragObject != null;
	}
	
	public boolean isDraggingObjectSelection() {
		return isDraggingObject() && isSelected(dragObject);
	}
	
	public List<T> getDragged() {
		List<T> dragObjects = new ArrayList<T>();
		if(!isDraggingObject()) {
			return dragObjects;
		}
		
		if(isDraggingObjectSelection()) {
			dragObjects.addAll(selectedObjects);
		} else {
			dragObjects.add(dragObject);
		}
		
		return dragObjects;
	}
	
	private int getDragScreenOffsetX() {
		if(dragEnd == null || lastDragEnd == null) {
			return 0;
		}
		return dragEnd.getScreenX() - lastDragEnd.getScreenX();
	}
	
	private int getDragScreenOffsetY() {
		if(dragEnd == null || lastDragEnd == null) {
			return 0;
		}
		return dragEnd.getScreenY() - lastDragEnd.getScreenY();
	}
	
	private int getTransformedDragScreenOffsetX() {
		if(dragEnd == null || lastDragEnd == null) {
			return 0;
		}
		return getCanvas().getZoom().inverseTransform(dragEnd.getScreenX(), 0).getScreenX() - 
			   getCanvas().getZoom().inverseTransform(lastDragEnd.getScreenX(), 0).getScreenX();
	}
	
	private int getTransformedDragScreenOffsetY() {
		if(dragEnd == null || lastDragEnd == null) {
			return 0;
		}
		return getCanvas().getZoom().inverseTransform(0, dragEnd.getScreenY()).getScreenY() - 
			   getCanvas().getZoom().inverseTransform(0, lastDragEnd.getScreenY()).getScreenY();
	}

	
	public void clear() {
		clear(false);
	}
	
	public void clear(boolean ignoreChange) {
		selectedObjects.clear();
		dragObject = null;
		if(!ignoreChange) {
			repaint();
		}
	}
	
	public boolean isEnableObjectDragging() {
		return enableObjectDragging;
	}

	public void setEnableObjectDragging(boolean enableObjectDragging) {
		this.enableObjectDragging = enableObjectDragging;
	}
	
	public int getDragMode() {
		return dragMode;
	}
	
	public Location getDragStart() {
		return dragStart;
	}
	
	public Location getDragEnd() {
		return dragEnd;
	}
	
}
