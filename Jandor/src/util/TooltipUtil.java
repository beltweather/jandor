package util;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

public class TooltipUtil {

	private TooltipUtil() {}

	public static void showToolTip(JComponent component, MouseEvent e, String message) {
	   ToolTipManager manager = ToolTipManager.sharedInstance();
	   long time = System.currentTimeMillis() - manager.getInitialDelay() + 1;  // So that the tooltip will trigger immediately
	   //Point point = e.getLocationOnScreen();
	   //MouseEvent  = new MouseEvent(component, -1, time, 0, 0, 0, point.x, point.y, 1, false, 0);

	   ToolTipManager.
	      sharedInstance().
	      mouseMoved(e);
	}
	
}
