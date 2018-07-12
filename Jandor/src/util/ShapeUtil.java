package util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class ShapeUtil {

	private ShapeUtil() {}
	
	public static Shape rotate(Shape shape, int angle) {
		int w = (int) shape.getBounds().getWidth();
		int h = (int) shape.getBounds().getHeight();
		int x = (int) shape.getBounds().getX() + w/2;
		int y = (int) shape.getBounds().getY() + h/2;
		
		AffineTransform at = new AffineTransform();
		at.translate(-x, -y);
		shape = at.createTransformedShape(shape);
		
		at = new AffineTransform();
		at.rotate(Math.toRadians(angle));
		shape = at.createTransformedShape(shape);
		
		//w = (int) shape.getBounds().getWidth();
		//h = (int) shape.getBounds().getHeight();
		//x = (int) shape.getBounds().getX() + w/2;
		//y = (int) shape.getBounds().getY() + h/2;
		
		at = new AffineTransform();
		at.translate(x, y);
		shape = at.createTransformedShape(shape);

		return shape;
	}
	
	public static int toPositiveAngle(int angle) {
		while(angle < 0) {
			angle += 360;
		}
		return angle % 360;
	}
}
