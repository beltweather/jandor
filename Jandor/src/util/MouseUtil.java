package util;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

public class MouseUtil {

	private MouseUtil() {}
	
	public static boolean isLeft(MouseEvent e) {
		if(SystemUtil.isMac()) {
			if(SwingUtilities.isLeftMouseButton(e) && !e.isMetaDown() && !e.isShiftDown()) {
				return true;
			}
			return false;
		}
		return SwingUtilities.isLeftMouseButton(e);
	}
	
	public static boolean isRight(MouseEvent e) { // meta
		if(SystemUtil.isMac()) {
			if(SwingUtilities.isLeftMouseButton(e) && e.isMetaDown() && !e.isShiftDown()) {
				return true;
			}
			return false;
		}
		return SwingUtilities.isRightMouseButton(e);
	}
	
	public static boolean isMiddle(MouseEvent e) { // shift
		if(SystemUtil.isMac()) {
			if(SwingUtilities.isLeftMouseButton(e) && !e.isMetaDown() && e.isShiftDown()) {
				return true;
			}
			return false;
		}
		return SwingUtilities.isMiddleMouseButton(e);
	}
	
}
