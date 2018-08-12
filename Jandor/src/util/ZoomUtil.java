package util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import canvas.Location;
import canvas.zoom.ZoomAndPanListener;

public class ZoomUtil {

	//private static ZoomAndPanListener instance = null;
	
	//public static ZoomAndPanListener newInstance(Component component) {
	//	instance = new ZoomAndPanListener(component);
	//	return instance;
	//}
	
	public static Shape transform(Shape shape, ZoomAndPanListener instance) {
		if(instance == null) {
			return null;
		}
		return instance.getCoordTransform().createTransformedShape(shape);
	}
	
	public static Shape inverseTransform(Shape shape, ZoomAndPanListener instance) {
		if(instance == null) {
			return null;
		}
		try {
			return instance.getCoordTransform().createInverse().createTransformedShape(shape);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return shape;
	}
	
	public static Location transform(Location location, ZoomAndPanListener instance) {
		return transform(location.getScreenX(), location.getScreenY(), instance);
	}
	
	public static Location inverseTransform(Location location, ZoomAndPanListener instance) {
		return inverseTransform(location.getScreenX(), location.getScreenY(), instance);
	}
	
	public static Location transform(int x, int y, ZoomAndPanListener instance) {
		Point2D pSource = new Point2D.Float(x, y);
		Point2D pDest = new Point2D.Float();
		instance.getCoordTransform().transform(pSource, pDest);
		return new Location((int) pDest.getX(), (int) pDest.getY());
	}
	
	public static Location inverseTransform(int x, int y, ZoomAndPanListener instance) {
		Point2D pSource = new Point2D.Float(x, y);
		Point2D pDest = new Point2D.Float();
		try {
			instance.getCoordTransform().createInverse().transform(pSource, pDest);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return new Location((int) pDest.getX(), (int) pDest.getY());
	}
	
	public static Rectangle transformDimensions(int width, int height, ZoomAndPanListener instance) {
		return transform(new Rectangle(0, 0, width, height), instance).getBounds();
	}
	
	public static int transformDimension(int dim, ZoomAndPanListener instance) {
		return (int) transformDimensions(dim, dim, instance).getWidth();
	}
	
	public static Rectangle inverseTransformDimensions(int width, int height, ZoomAndPanListener instance) {
		return inverseTransform(new Rectangle(0, 0, width, height), instance).getBounds();
	}
	
	public static int inverseTransformDimension(int dim, ZoomAndPanListener instance) {
		return (int) inverseTransformDimensions(dim, dim, instance).getWidth();
	}
	
}
