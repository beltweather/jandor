package ui.pwidget;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicButtonUI;

import ui.JandorButton;
import util.ImageUtil;
import accordion.PAccordionPanel;



public class JUtil {
	
	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	public static G gbc() {
		G c = new G();
		c.gridx = 0;
		c.gridy = 0;
		weaken(c);
		return c;
	}
	
	public static void weaken(GridBagConstraints c) {
		c.weightx = 0.01;
		c.weighty = 0.01;
		c.fill = G.NONE;
	}
	
	public static void strengthen(GridBagConstraints c) {
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = G.BOTH;
	}
	
	public static Insets insets(int n) {
		return new Insets(n, n, n, n);
	}
	
	public static Insets noInsets() {
		return new Insets(0, 0, 0, 0);
	}
	
	public static JPanel colorPanel(Color color) {
		JPanel p = new JPanel();
		p.setBackground(color);
		return p;
	}
	
	public static JPanel vFlowPanel() {
		JPanel p = clearPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		return p;
	}
	
	public static JPanel clearPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(null);
		p.setOpaque(false);
		return p;
	}
	
	public static JandorButton buildCloseButton() {
		JandorButton button = new JandorButton();
		ImageIcon icon = new ImageIcon(ImageUtil.getCloseIcon());
	
		button.setIcon(icon);
        button.setRolloverIcon(new ImageIcon(ImageUtil.getCloseIconFull()));
        button.setPressedIcon(new ImageIcon(ImageUtil.getCloseIconFullDown()));
        button.setRolloverEnabled(true);
        
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        button.setUI(new BasicButtonUI());
        button.setBorderPainted(false);
        button.setBackground(ColorUtil.DARK_GRAY_3);
	
        return button;
	}
	
	public static String toVerticalText(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><div align=\"center\">"); 
		for(int i = 0; i < text.length(); i++) {
			if(i == 0) {
				sb.append(text.charAt(i));
			} else {
				sb.append("<br>" + text.charAt(i));
			}
		}
		sb.append("</div></html>");
		return sb.toString();
	}
	
	public static Point fitOnScreen(MouseEvent e, Component compToFit) {
		// Creat constants
		int illegalMargin = 50;
		int margin = 60;
		int yOffset = 20;
		int maxOffsetFromCursor = 100;
		int tooltipHeight = compToFit.getHeight();
		int tooltipWidth = compToFit.getWidth();
		
		// Component that is being moused over
		JComponent comp = (JComponent) e.getSource();
		Component f = (Component) SwingUtilities.getRoot(comp);
		Point frame = f.getLocationOnScreen();

		// Translate location on screen to local coordiantes (subtraction)
		// and local coordinate to location on screen (addition)
		double tranX = e.getLocationOnScreen().getX() - e.getX();
		double tranY = e.getLocationOnScreen().getY() - e.getY();
		Point tran = new Point((int) tranX, (int) tranY);
		
		// Get the mouse on screen
		Point mouse = e.getLocationOnScreen();
		mouse.y += yOffset; // Add offset just to make sure mouse can't be inside
		
		// Get top left corner of the component that's being moused over in
		Point compTopLeft = comp.getLocationOnScreen();
				
		// Project to the right
		double scootRight = comp.getWidth() + compTopLeft.getX() + margin;
		scootRight = Math.min(scootRight, mouse.getX() + maxOffsetFromCursor);
		Point right = newPoint(scootRight, mouse.getY());
		
		// Check if still on screen
		boolean onScreen = right.getX() + tooltipWidth < frame.getX() + f.getWidth() - illegalMargin;
		
		// If not, project left
		Point locationOfTooltip;
		if(!onScreen) {
			
			// Project to the left
			double scootLeft = compTopLeft.getX() - tooltipWidth - margin;
			scootLeft = Math.max(scootLeft, mouse.getX() - tooltipWidth - maxOffsetFromCursor);
			Point left = newPoint(scootLeft, mouse.getY());
			
			locationOfTooltip = left;
			
		} else {
			locationOfTooltip = right;
		}
		
		// Now check top and bottom of window, if the tooltip would extend beyond
		// it put it a margin away
		if(locationOfTooltip.getY() < frame.getY() + illegalMargin) {
			locationOfTooltip.y = frame.y + margin;
		} else if(locationOfTooltip.getY() + tooltipHeight > frame.getY() + f.getHeight() - illegalMargin) {
			locationOfTooltip.y = frame.y + f.getHeight() - tooltipHeight - margin;
		}

		// Now check left and right of window, if the tooltip would extend beyond
		// it put it a margin away
		if(locationOfTooltip.getX() < frame.getX() + illegalMargin) {
			locationOfTooltip.x = frame.x + margin;
		} else if(locationOfTooltip.getX() + tooltipWidth > frame.getX() + f.getWidth() - illegalMargin) {
			locationOfTooltip.x = frame.x + f.getWidth() - tooltipWidth - margin;
		}
		
		return subtract(locationOfTooltip, tran);
	}
	
	private static Point newPoint(double x, double y) {
		return new Point((int) x, (int) y);
	}

	private static Point add(Point p, double x, double y) {
		p.x += x;
		p.y += y;
		return p;
	}
	
	private static Point subtract(Point p, double x, double y) {
		p.x += x;
		p.y += y;
		return p;
	}
	
	private static Point add(Point p1, Point p2) {
		p1.x += p2.x;
		p1.y += p2.y;
		return p1;
	}
	
	private static Point subtract(Point p1, Point p2) {
		p1.x -= p2.x;
		p1.y -= p2.y;
		return p1;
	}
	
	/*public static Point fitOnScreen(Object source, Point locationOnScreen, int width, int height) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int bottomMargin = 16;
		
		Component component = (Component) source;
        JFrame f = (JFrame) SwingUtilities.getRoot(component);
		
		screenSize = new Dimension((int) Math.min(screenSize.getWidth(), f.getX() + f.getWidth() - bottomMargin),
								   (int) Math.min(screenSize.getHeight(), f.getY() + f.getHeight() - bottomMargin));
		
		double x = (locationOnScreen.getX() + width) > screenSize.getWidth() ? screenSize.getWidth() - width :
																			   locationOnScreen.getX();
		
		double y = (locationOnScreen.getY() + height) > screenSize.getHeight() ? screenSize.getHeight() - height :
			   																	 locationOnScreen.getY();
		
		if(x < 0 || y < 0) {
			return locationOnScreen;
		}
		
		return new Point((int) x, (int) y);
	}*/
	
	/*
	 
		double diffX = e.getLocationOnScreen().getX() - e.getX();
		double diffY = e.getLocationOnScreen().getY() - e.getY();
		
		Point fixedOnScreen = JUtil.fitOnScreen(e.getSource(), new Point((int)(e.getX() + diffX + getRowHeight()), 
				                                          (int)(e.getY() + diffY + getRowHeight())), 
				                                tooltipPanel.getWidth(), 
				                                tooltipPanel.getHeight());
		
	 */
	
	public static RoundPanel frame(BufferedImage img) {
		return frame(new PImage(img));
	}
	
	public static RoundPanel frame(PImage imgLabel) {
		RoundPanel borderPanel = new RoundPanel();
		borderPanel.setBackground(Color.WHITE);
		borderPanel.setBorderColor(Color.WHITE);
		borderPanel.setCornerRadius(5);
		
		GridBagConstraints c = JUtil.gbc();
		
		fill(borderPanel, c);
		c.gridx++;
		c.weightx = 0.01;
		c.insets = new Insets(20, 40, 20, 40);
		borderPanel.add(imgLabel, c);
		c.gridx++;
		fill(borderPanel, c);
		
		return borderPanel;
	}
	
	public static JLabel centerLabel(String text) {
		return centerLabel(text, null);
	}
	
	public static JLabel centerLabel(String text, Color color) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.CENTER);
		if(color != null) {
			label.setForeground(color);
		}
		return label;
	}
	
	public static JLabel rightLabelSmall(String text) {
		return rightLabel(text, 50);
	}

	public static JLabel rightLabel(String text, int width) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.RIGHT);
		label.setPreferredSize(new Dimension(width, 20));
		return label;
	}
	
	public static String bold(String text) {
		return "<html><b>" + text + "</b></html>";
	}
	
	public static String sizeText(String text, int width) {
		return "<html><div width=" + width + ">" + text + "</div></html>";
	}
	
	public static int getTextWidth(JLabel label) {
		return label.getFontMetrics(label.getFont()).stringWidth(label.getText());
	}
	
	public static int getWidth(Graphics g, String text) {
		return g.getFontMetrics().stringWidth(text);
	}
	
	public static int getHeight(Graphics g) {
		return g.getFontMetrics().getHeight();
	}
	
	public static void makeBold(Graphics g) {
		Font font = g.getFont();
		font = new Font(font.getName(), Font.BOLD, font.getSize());
		g.setFont(font);
	}
	
	public static Container getTopLevelContainer(JComponent c) {
		Container con = c.getParent();
		if(con == null) {
			return con;
		}
		while(con.getParent() != null) {
			con = con.getParent();
		}
		return con;
	}
	
	public static JandorTabFrame getFrame(Component component) {
		return (JandorTabFrame) SwingUtilities.getRoot(component);
	}
	
	public static PTabPane getTabPane(Component component) {
		return ((JandorTabFrame) SwingUtilities.getRoot(component)).getTabPane();
	}
	
	public static PAccordionPanel getAccordionPanel(Component component) {
		while(component.getParent() != null) {
			if(component.getParent() instanceof PAccordionPanel) {
				return (PAccordionPanel) component.getParent();
			}
			component = component.getParent();
		}
		return null;
	}

	public static void repaintFrame(Component component) {
		getFrame(component).repaint();
	}
	
	public static ImageIcon getArrowIcon(int w, int h, Color color, int direction) {
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		Polygon p = new Polygon();

		if(direction == DOWN) { 
			p.addPoint(0, 0);
			p.addPoint(w, 0);
			p.addPoint(w/2, h-1);
		} else if(direction == UP) {
			p.addPoint(0, h-1);
			p.addPoint(w, h-1);
			p.addPoint(w/2, 0);
		} else if(direction == RIGHT) {
			p.addPoint(0, 0);
			p.addPoint(0, h-1);
			p.addPoint(w-1, h/2);
		} else if(direction == LEFT) {
			p.addPoint(w-1, 0);
			p.addPoint(w-1, h-1);
			p.addPoint(0, h/2);
		}
		
		Graphics2D g = img.createGraphics();
		g.setColor(color);
		antialiasing(g);
		g.fillPolygon(p);
		g.dispose();
		
		return new ImageIcon(img);
	}
	
	public static void antialiasing(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	public static Image getPokeballIconImage() {
		
		int w = 21;
		int h = 21;
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Color c = ColorUtil.TABLE_BAD_COLOR;
		
		Graphics2D g = img.createGraphics();
		antialiasing(g);
		g.setColor(c);
		
		ColorUtil.radialGradientTwoShades(g, 0, 0, w, h, w);
		
		g.fillOval(0, 0, w-1, h-1);
		g.setColor(Color.WHITE);
		//g.setPaint(null);
		ColorUtil.radialGradientTwoShades(g, 0, 0, w, h, w);
		
		g.fillArc(2, 2, w-5, h-5, 0, -180);
		g.setColor(c);
		ColorUtil.radialGradientTwoShades(g, 0, 0, w, h, w);
		
		g.fillOval(6, 6, 8, 8);
		g.dispose();
		
		return img;
	}
	
	public static Image getPokeballOutlineIconImage() {
		
		int w = 21;
		int h = 21;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		antialiasing(g);
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		g.drawOval(1, 1, w-3, h-3);
		g.dispose();
		
		return img;
	}
	
	public static void fill(JComponent comp, GridBagConstraints c) {
		double weightx = c.weightx;
		double weighty = c.weighty;
		int fill = c.fill;
		Insets in = c.insets;
		c.insets = noInsets();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		comp.add(Box.createHorizontalStrut(1), c);
		c.fill = fill;
		c.weightx = weightx;
		c.weighty = weighty;
		c.insets = in;
	}

	public static void hFill(PPanel p) {
		hFill(p, p.c);
	}
	
	public static void hFill(JComponent comp, GridBagConstraints c) {
		c.weightx = 1.0;
		comp.add(Box.createHorizontalStrut(1), c);
		c.weightx = 0.01;
	}
	
	public static void vFill(PPanel p) {
		hFill(p, p.c);
	}
	
	public static void vFill(JComponent comp, GridBagConstraints c) {
		c.weighty = 1.0;
		comp.add(Box.createHorizontalStrut(1), c);
		c.weighty = 0.01;
	}

	public static void vhFill(PPanel p) {
		hFill(p, p.c);
	}
	
	public static void vhFill(JComponent comp, GridBagConstraints c) {
		strengthen(c);
		comp.add(Box.createHorizontalStrut(1), c);
		weaken(c);
	}
	
	public static String proper(String s) {
		if(s == null || s.equals("")) {
			return "";
		}
		
		String[] toks = s.split(" ");
		String p = "";
		boolean first = true;
		for(String tok : toks) {
			if(!first) {
				p += " ";
			} else {
				first = false;
			}
			if(tok.length() == 1) {
				p += tok.toUpperCase();
			} else { 
				p += tok.substring(0, 1).toUpperCase() + tok.substring(1).toLowerCase();
			}
		}
		return p;
	}
	
	public static List<PRadio> radioGroup(List<String> names, ItemListener itemListener) {
		return radioGroup(names.toArray(new String[]{}), itemListener);
	}
	
	public static List<PRadio> radioGroup(String[] names, ItemListener itemListener) {
		List<PRadio> radios = new ArrayList<PRadio>();
		ButtonGroup g = new ButtonGroup();
		for(String name : names) {
			PRadio radio = new PRadio(name);
			radio.addItemListener(itemListener);
			g.add(radio);
			radios.add(radio);
		}
		return radios;
	}
	
	public static JLabel label(String text, float height) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(height));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}
	
	public static JPanel lockSize(JComponent c) {
		JPanel p = new PPanel();
		p.setPreferredSize(c.getPreferredSize());
		c.setMaximumSize(c.getPreferredSize());
		c.setMinimumSize(c.getMinimumSize());
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(c);
		return p;
	}
	
	public static JToolTip getCurrentTooltip() {
		return getCurrentTooltip(true);
	}
	
	public static JToolTip getCurrentTooltip(boolean hide) {
		Object obj = null;
		try {
			
			Field fieldTipShowing = ToolTipManager.class.getDeclaredField("tipShowing");
			fieldTipShowing.setAccessible(true);
			Boolean tipShowing = (Boolean) fieldTipShowing.get(ToolTipManager.sharedInstance());
			if(!tipShowing) {
				return null;
			}
			
			Field fieldTip = ToolTipManager.class.getDeclaredField("tip");
			fieldTip.setAccessible(true);
			obj = fieldTip.get(ToolTipManager.sharedInstance());
			
		/*	if(hide) {
				/*for(Method m : ToolTipManager.class.getDeclaredMethods()) {
					System.out.println(m.getName());
				}*/
				
				/*Method methodHideTip = ToolTipManager.class.getDeclaredMethod("hide", new Class[] {});
				methodHideTip.setAccessible(true);
				methodHideTip.invoke(ToolTipManager.sharedInstance(), new Object[] {});
			}*/

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} /*catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}*/
		if(obj == null) {
			return null;
		}
		return (JToolTip) obj;
	}
	
	public static void clearCurrentTooltip() {
		try {
			
			Field fieldTip = ToolTipManager.class.getDeclaredField("tip");
			fieldTip.setAccessible(true);
			fieldTip.set(ToolTipManager.sharedInstance(), null);
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static void popupWindow(String title, Container content) {
		popupWindow(title, content, false);
	}
	
	public static JandorTabFrame popupWindow(String title, Container content, boolean simple) {
		//new PFrame(title, content).setVisible(true);
	
		if(simple) {
			JandorTabFrame f = new JandorTabFrame(false);
			f.setContentPane(content);
			f.pack();
			f.setTitle(title);
			f.setVisible(true);
			return f;
		} else {
			JandorTabFrame f = new JandorTabFrame();
			f.getTabPane().addTab(title, content);
			f.pack();
			f.setVisible(true);
			return f;
		}
	}
	
	public static void openTab(Component comp, String title, Container content) {
		PTabPane tabPane = getTabPane(comp);
		if(tabPane == null) {
			popupWindow(title, content);
			return;
		} else {
			tabPane.addTab(title, content);
			tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
		}
		
	}
	
	public static boolean showConfirmYesNoDialog(Component parent, String title, String message) {
		return JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
	
	public static boolean showConfirmYesNoCancelDialog(Component parent, String title, String message) {
		return JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), message, title, JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION;
	}
	
	public static boolean showConfirmDialog(Component parent, String title, String text) {
		return showConfirmDialog(SwingUtilities.getRoot(parent), title, new JLabel(text));
	}
	
	public static boolean showConfirmDialog(Component parent, String title, Component component) {
		return JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), component, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
	}
	
	public static boolean showWarningDialog(Component parent, String title, Component component) {
		return JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), component, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
	}
	
	public static boolean showWarningDialog(Component parent, String title, String message) {
		 return JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
	}
	
	public static void showMessageDialog(Component parent, String title, String message) {
		 JOptionPane.showMessageDialog(SwingUtilities.getRoot(parent), message, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public static String showInputDialog(Component parent, String title, String message, String defaultText) {
		Object obj = JOptionPane.showInputDialog(SwingUtilities.getRoot(parent), message, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultText);
		if(obj == null) {
			return null;
		}
		return obj.toString();
	}
	
	public static int showDialog(Component parent, String title, Component component) {
		//JOptionPane.showMessageDialog(parent, component, title, JOptionPane.DEFAULT_OPTION);
		int input = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), component, title, -1, JOptionPane.PLAIN_MESSAGE);
		if(component instanceof CloseListener) {
			((CloseListener) component).handleClosed();
		}
		return input;
	}
	
	public static JDialog buildBlankDialog(Component parent, String title, Component component) {
		final JOptionPane optionPane = new JOptionPane(component, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		final JDialog dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - dialog.getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - dialog.getHeight()/2);
		return dialog;
	}
	
	public static <T> List<T> newList(T... items) {
		List<T> list = new ArrayList<T>();
		for(T item : items) {
			list.add(item);
		}
		return list;
	}
	
}
