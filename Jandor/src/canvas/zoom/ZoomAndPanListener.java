package canvas.zoom;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.Serializable;

import canvas.Location;
import util.MouseUtil;
import util.ZoomUtil;

/**
 * Listener that can be attached to a Component to implement Zoom and Pan functionality.
 */
public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener, Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_MIN_ZOOM_LEVEL = -3;//-20;
	public static final int DEFAULT_MAX_ZOOM_LEVEL = 5;//20;
	public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 1.2;

	private Component targetComponent;

	private int zoomLevel = 0;
	private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
	private int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
	private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;

	private Point dragStartScreen;
	private Point dragEndScreen;
	private AffineTransform coordTransform = new AffineTransform();
	private double rotationAngle = 0;
	
	private boolean moveCameraEnabled = true;
	private boolean zoomCameraEnabled = true;
	private boolean zoomAtCursorPosition = true;
	
	public ZoomAndPanListener(Component targetComponent) {
		this.targetComponent = targetComponent;
		addListenersToComponent(targetComponent);
	}

	public ZoomAndPanListener(Component targetComponent, int minZoomLevel, int maxZoomLevel, double zoomMultiplicationFactor) {
		this.targetComponent = targetComponent;
		this.minZoomLevel = minZoomLevel;
		this.maxZoomLevel = maxZoomLevel;
		this.zoomMultiplicationFactor = zoomMultiplicationFactor;
		addListenersToComponent(targetComponent);
	}
	
	private void addListenersToComponent(Component targetComponent) {
		targetComponent.addMouseListener(this);
		targetComponent.addMouseMotionListener(this);
		targetComponent.addMouseWheelListener(this);
	}
	
	public boolean isMoveCameraEnabled() {
		return moveCameraEnabled;
	}
	
	public void setMoveCameraEnabled(boolean moveCameraEnabled) {
		this.moveCameraEnabled = moveCameraEnabled;
	}
	
	public boolean isZoomCameraEnabled() {
		return zoomCameraEnabled;
	}
	
	public void setZoomCameraEnabled(boolean zoomCameraEnabled) {
		this.zoomCameraEnabled = zoomCameraEnabled;
	}
	
	public boolean isZoomAtCursorPosition() {
		return zoomAtCursorPosition;
	}
	
	public void setZoomAtCursorPosition(boolean zoomAtCursorPosition) {
		this.zoomAtCursorPosition = zoomAtCursorPosition;
	}
	
	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		if(!MouseUtil.isMiddle(e)) {
			return;
		}
		dragStartScreen = e.getPoint();
		dragEndScreen = null;
	}

	public void mouseReleased(MouseEvent e) {
		//        moveCamera(e);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if(!MouseUtil.isMiddle(e)) {
			return;
		}
		moveCamera(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		//        System.out.println("============= Zoom camera ============");
		zoomCamera(e);
	}

	private void moveCamera(MouseEvent e) {
		if(!isMoveCameraEnabled()) {
			return;
		}
		//        System.out.println("============= Move camera ============");
		try {
			dragEndScreen = e.getPoint();
			Point2D.Float dragStart = transformPoint(dragStartScreen);
			Point2D.Float dragEnd = transformPoint(dragEndScreen);
			double dx = dragEnd.getX() - dragStart.getX();
			double dy = dragEnd.getY() - dragStart.getY();
			coordTransform.translate(dx, dy);
			dragStartScreen = dragEndScreen;
			dragEndScreen = null;
			handleMoveCamera();
			targetComponent.repaint();
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}
	
	private void zoomCamera(MouseWheelEvent e) {
		if(!isZoomCameraEnabled()) {
			return;
		}
		
		int wheelRotation = e.getWheelRotation();
		Point p = e.getPoint();
		
		if(!isZoomAtCursorPosition()) {
			p = new Point(0, 0);
		}
		
		zoomCamera(wheelRotation, p, e.isControlDown(), e.isShiftDown());
	}
	
	public void virtualZoomIn() {
		virtualZoomIn(0, 0);
	}
	
	public void virtualZoomIn(int x, int y) {
		zoomCamera(-1, new Point(x, y), false, false);
	}
	
	public void virtualZoomOut() {
		virtualZoomOut(0, 0);
	}
	
	public void virtualZoomOut(int x, int y) {
		zoomCamera(1, new Point(x, y), false, false);
	}
	
	private void zoomCamera(int wheelRotation, Point p, boolean isControlDown, boolean isShiftDown) {
		if(!isZoomCameraEnabled()) {
			return;
		}
		try {
			if(isControlDown || isShiftDown) {
				Point2D p1 = transformPoint(p);
				double deltaTheta = Math.PI/10;
				if(wheelRotation > 0) {
					coordTransform.rotate(deltaTheta, p1.getX(), p1.getY());
					rotationAngle += deltaTheta;
				} else {
					coordTransform.rotate(-deltaTheta, p1.getX(), p1.getY());
					rotationAngle -= deltaTheta;
				}
				targetComponent.repaint();
			} else if (wheelRotation > 0) {
				if (zoomLevel < maxZoomLevel) {
					zoomLevel++;
					Point2D p1 = transformPoint(p);
					coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
					Point2D p2 = transformPoint(p);
					coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
					//coordTransform.rotate(-10);
					handleZoomCamera();
					targetComponent.repaint();
				}
			} else {
				if (zoomLevel > minZoomLevel) {
					zoomLevel--;
					Point2D p1 = transformPoint(p);
					coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
					Point2D p2 = transformPoint(p);
					coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
					//coordTransform.rotate(10);
					handleZoomCamera();
					targetComponent.repaint();
				}
			}
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}
	
	protected void handleMoveCamera() {
		
	}
	
	protected void handleZoomCamera() {
		
	}

	private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
		//        System.out.println("Model -> Screen Transformation:");
//		        showMatrix(coordTransform);
		        AffineTransform inverse = coordTransform.createInverse();
//		        System.out.println("Screen -> Model Transformation:");
//		        showMatrix(inverse);

		        Point2D.Float p2 = new Point2D.Float();
		        inverse.transform(p1, p2);
		        return p2;
	}

	private void showMatrix(AffineTransform at) {
		double[] matrix = new double[6];
		at.getMatrix(matrix);  // { m00 m10 m01 m11 m02 m12 }
		int[] loRow = {0, 0, 1};
		for (int i = 0; i < 2; i++) {
			System.out.print("[ ");
			for (int j = i; j < matrix.length; j += 2) {
				System.out.printf("%5.1f ", matrix[j]);
			}
			System.out.print("]\n");
		}
		System.out.print("[ ");
		for (int i = 0; i < loRow.length; i++) {
			System.out.printf("%3d   ", loRow);

		}

		System.out.print("]\n");

		System.out.println("---------------------");

	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public AffineTransform getCoordTransform() {
		return coordTransform;
	}

	public void setCoordTransform(AffineTransform coordTransform) {
		this.coordTransform = coordTransform;
	}

	private boolean init = true;
	private AffineTransform lastTransform = null;
	public void transform(Graphics2D g) {
		lastTransform = (AffineTransform) g.getTransform().clone();
		if (init) {
			init = false;
			setCoordTransform(g.getTransform());
			handleInit();
		} else {
			g.setTransform(getCoordTransform());
		}
	}
	
	public void handleInit() {
		
	}
	
	public void revert(Graphics2D g) {
		if(lastTransform != null) {
			g.setTransform(lastTransform);
		}
	}
	
	// Util Methods
	public Shape transform(Shape shape) {
		return ZoomUtil.transform(shape, this);
	}
	
	public Shape inverseTransform(Shape shape) {
		return ZoomUtil.inverseTransform(shape, this);
	}
	
	public Location transform(Location location) {
		return ZoomUtil.transform(location, this);
	}
	
	public Location inverseTransform(Location location) {
		return ZoomUtil.inverseTransform(location, this);
	}
	
	public Location transform(int x, int y) {
		return ZoomUtil.transform(x, y, this);
	}
	
	public Location inverseTransform(int x, int y) {
		return ZoomUtil.inverseTransform(x, y, this);
	}
	
	public Rectangle transformDimensions(int width, int height) {
		return ZoomUtil.transformDimensions(width, height, this);
	}
	
	public int transformDimension(int dim) {
		return ZoomUtil.transformDimension(dim, this);
	}
	
	public Rectangle inverseTransformDimensions(int width, int height) {
		return ZoomUtil.inverseTransformDimensions(width, height, this);
	}
	
	public int inverseTransformDimension(int dim) {
		return ZoomUtil.inverseTransformDimension(dim, this);
	}
	
	public double getRotationAngleRadians() {
		return rotationAngle;
	}
	
}