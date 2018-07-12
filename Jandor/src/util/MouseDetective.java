package util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseDetective implements MouseListener, MouseMotionListener, KeyListener  {

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		printButton(e, "Dragged");
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {
		printButton(e, "Pressed");
		if(e.getClickCount() > 1) {
			System.out.println("[Double Click]!");
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		printButton(e, "Pressed");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		printButton(e, "Released");		
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	private void printButton(MouseEvent e, String label) {
		System.out.println(label + ": button "  + e.getButton() + " " + getModifierString(e));
	}

	private String getModifierString(MouseEvent e) {
		String s = "";
		if(e.isControlDown()) {
			s += "(ctrl)";
		}

		if(e.isShiftDown()) {
			s += "(shift)";
		}
		
		if(e.isAltDown()) {
			s += "(alt)";
		}
		
		if(e.isMetaDown()) {
			s += "(meta)";
		}
		return s;
	}

}
