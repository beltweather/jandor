package ui.pwidget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.Point2D;

import util.ManaUtil;


public class ColorUtil {

	public static final int GRAD_NONE = 0;
	public static final int GRAD_HORZ = 1;
	public static final int GRAD_VERT = 2;
	public static final int GRAD_DIAG_TL_BR = 3;
	public static final int GRAD_DIAG_BL_TR = 4;
	
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	public static final Color LIGHT_GRAY_0 = Color.LIGHT_GRAY;
	public static final Color DARK_GRAY_0 = Color.DARK_GRAY;
	public static final Color DARK_GRAY_1 = new Color(84, 84, 84);
	public static final Color DARK_GRAY_2 = new Color(104, 104, 104);
	public static final Color DARK_GRAY_3 = new Color(124, 124, 124);

	public static final Color DARK_RED = new Color(84, 0, 0);
	public static final Color LIGHT_RED = new Color(242, 157, 147);
	
 	public static final Color BLACKOUT = new Color(255, 255, 255, 255);
	
	public static final Color STAT_HP = Color.RED;
	public static final Color STAT_ATTACK = new Color(240, 128, 48);
	public static final Color STAT_DEFENSE = new Color(248, 204, 48);
	public static final Color STAT_SP_ATTACK = new Color(104, 144, 240);
	public static final Color STAT_SP_DEFENSE = new Color(120, 200, 80);
	public static final Color STAT_SPEED = new Color(248, 88, 136);

	public static final Color STAT_HP_PALE = new Color(255, 89, 89);
	public static final Color STAT_ATTACK_PALE = new Color(245, 172, 120);
	public static final Color STAT_DEFENSE_PALE = new Color(250, 224, 120);
	public static final Color STAT_SP_ATTACK_PALE = new Color(157, 183, 245);
	public static final Color STAT_SP_DEFENSE_PALE = new Color(167, 219, 141);
	public static final Color STAT_SPEED_PALE = new Color(250, 146, 178);
	
	public static final Color TABLE_BAD_COLOR = new Color(179, 94, 96);
	public static final Color TABLE_GOOD_COLOR = Color.WHITE;//Color.DARK_GRAY.brighter();
	public static final Color TABLE_BG_COLOR = Color.DARK_GRAY;//Color.WHITE;
	public static final Color TABLE_GRID_COLOR = Color.DARK_GRAY.brighter();//LIGHT_GRAY_0;
	
	public static final Color RANGE_BAR_COLOR = new Color(197, 197, 197); 
	
	public static final Color MANA_WHITE = new Color(254, 254, 210);
	public static final Color MANA_BLUE = new Color(170, 226, 253);
	public static final Color MANA_BLACK = Color.BLACK;
	public static final Color MANA_RED = new Color(250, 171, 140);
	public static final Color MANA_GREEN = new Color(155, 211, 169);
	public static final Color MANA_MULTI = new Color(191,167,0);
	public static final Color MANA_NONE = DARK_GRAY_3;
	
	
	public static Color brighter(Color color) {
		float hsbVals[] = Color.RGBtoHSB(color.getRed(),
                						 color.getGreen(),
                						 color.getBlue(), null);
		return Color.getHSBColor( hsbVals[0], hsbVals[1], 0.5f * (1f + hsbVals[2]));
	}
	
	public static Color darker(Color color) {
		float hsbVals[] = Color.RGBtoHSB(color.getRed(),
				 color.getGreen(),
				 color.getBlue(), null);
		return Color.getHSBColor( hsbVals[0], hsbVals[1], 0.5f * hsbVals[2]);
	}
	
	public static Color getBestForegroundColor(Color backgroundColor) {
		if(backgroundColor.equals(MANA_WHITE)) {
			return Color.BLACK;
		} else if(backgroundColor.equals(MANA_BLUE)) {
			return Color.WHITE;
		} else if(backgroundColor.equals(MANA_RED)) {
			return Color.WHITE;
		} else if(backgroundColor.equals(MANA_BLACK)) {
			return Color.WHITE;
		} else if(backgroundColor.equals(MANA_GREEN)) {
			return Color.BLACK;
		} else if(backgroundColor.equals(MANA_NONE)) {
			return Color.WHITE;
		} else if(backgroundColor.equals(MANA_MULTI)) {
			return Color.BLACK;
		} 
		
		int d = 0;

	    // Counting the perceptive luminance - human eye favors green color... 
	    double a = 1 - ( 0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue())/255;

	    if (a < 0.10)
	       d = 0; // bright colors - black font
	    else
	       d = 255; // dark colors - white font

	    return new Color(d, d, d);
	}
	
	public static int getLuminance(Color color) {
		int argb = color.getRGB();
	    int lum= (   77  * ((argb>>16)&255) 
	               + 150 * ((argb>>8)&255) 
	               + 29  * ((argb)&255))>>8;
	    return lum;
	}
	
	public static GradientPaint gradient(int gradientType, Color colorTop, Color colorBottom, int width, int height) {
		return gradient(null, gradientType, colorTop, colorBottom, width, height);
	}
	
	public static GradientPaint gradient(Graphics g, int gradientType, Color colorTop, Color colorBottom, int width, int height) {
		GradientPaint gp;
		switch(gradientType) {
			case GRAD_DIAG_TL_BR:
				gp = new GradientPaint(0, 0, colorTop, width, height, colorBottom);
				break;
			case GRAD_DIAG_BL_TR:
				gp = new GradientPaint(0, height, colorTop, width, 0, colorBottom);
				break;
			case GRAD_HORZ:
				gp = new GradientPaint(0, 0, colorTop, width, 0, colorBottom);
				break;
			case GRAD_VERT:
			default:
				gp = new GradientPaint(0, 0, colorTop, 0, height, colorBottom);
				break;
		}
		if(g != null) {
			((Graphics2D) g).setPaint(gp);
		}
		return gp;
	}
	
	public static void radialGradient(Graphics2D g, int x, int y, int w, int h, int d) {
		float[] dist = {0.0f, 0.7f, 1.0f};
	    Color[] colors = {g.getColor().brighter(), g.getColor().darker(), g.getColor().darker().darker()};
	    RadialGradientPaint p = new RadialGradientPaint(new Point2D.Double(w/2, h/2), w/2, new Point2D.Double(x + d/4, y + d/4),
	                                 					dist, colors,
	                                 					CycleMethod.NO_CYCLE);
		g.setPaint(p);
	}
	
	public static void radialGradientMirrored(Graphics2D g, int x, int y, int w, int h, int d) {
		float[] dist = {0.0f, 0.7f, 1.0f};
	    Color[] colors = {g.getColor().brighter(), g.getColor().darker(), g.getColor().darker().darker()};
	    RadialGradientPaint p = new RadialGradientPaint(new Point2D.Double(w/2, h/2), w/2, new Point2D.Double(x + 3*d/4, y + d/4),
	                                 					dist, colors,
	                                 					CycleMethod.NO_CYCLE);
		g.setPaint(p);
	}
	
	public static void radialGradientTwoShades(Graphics2D g, int x, int y, int w, int h, int d) {
		float[] dist = {0.0f, 1.0f};
	    Color[] colors = {g.getColor().brighter(), g.getColor().darker()};
	    RadialGradientPaint p = new RadialGradientPaint(new Point2D.Double(w/2, h/2), w/2, new Point2D.Double(x + d/4, y + d/4),
	                                 					dist, colors,
	                                 					CycleMethod.NO_CYCLE);
		g.setPaint(p);
	}
	
	public static void main(String[] args) {
		System.out.println("White: " + getBestForegroundColor(ColorUtil.MANA_WHITE));
		System.out.println("Blue: " + getBestForegroundColor(ColorUtil.MANA_BLUE));
		System.out.println("Black: " + getBestForegroundColor(ColorUtil.MANA_BLACK));
		System.out.println("Red: " + getBestForegroundColor(ColorUtil.MANA_RED));
		System.out.println("Green: " + getBestForegroundColor(ColorUtil.MANA_GREEN));
		System.out.println("Gray: " + getBestForegroundColor(ColorUtil.MANA_NONE));
		System.out.println("Gold: " + getBestForegroundColor(ColorUtil.MANA_MULTI));

	}

}
