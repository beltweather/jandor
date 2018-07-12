package ui.pwidget;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;





public class RoundPanel extends PPanel {

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_RIGHT = 2;
	public static final int BOTTOM_LEFT = 3;
	
	public static final int TOP = 0;
	public static final int RIGHT = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 3;
	
	private int[] padding = new int[4];
	private Color backgroundColor;
	private Color hoverBackgroundColor;
	private Color pressedBackgroundColor;
	private Color borderColor;
	private int[] cornerRadius = new int[4];
	
	private int gradientType = ColorUtil.GRAD_NONE;
	private Color gradColorTop;
	private Color gradColorBottom;

	public RoundPanel() {
		this(null);
	}
	
	public RoundPanel(JComponent comp) {
		this(comp, null);
	}
	
	public RoundPanel(JComponent comp, Color borderColor) {
		super();
		init();
		if(comp != null) {
			add(comp, c);
		}
		if(borderColor != null) {
			setBorderColor(borderColor);
		}
	}
	
	private void init() {
		super.setBackground(new Color(0, 0, 0, 0));
		setForeground(Color.WHITE);
		setBackground(Color.DARK_GRAY);
		setHoverBackground(new Color(84, 84, 84));
		setPressedBackground(new Color(104, 104, 104));
		setBorderColor(Color.BLACK);
		setCornerRadius(10);
		setPadding(10);
		setOpaque(true);
	}
	
    @Override
    protected void paintComponent(Graphics g) {
		g.setColor(backgroundColor);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if(gradientType != ColorUtil.GRAD_NONE) {
			Color top = gradColorTop == null ? backgroundColor : gradColorTop;
			Color bot = gradColorBottom == null ? ColorUtil.darker(backgroundColor) : gradColorBottom;
			ColorUtil.gradient(g, gradientType, top, bot, getWidth(), getHeight());
		}
		
		g.fillRoundRect(0, 0, getWidth(), getHeight(), getCornerRadius(0)*2, getCornerRadius(0)*2);
		if(borderColor != null) {
			g.setColor(borderColor);
			g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getCornerRadius(0)*2, getCornerRadius(0)*2);
		}

    }

 	@Override
 	public void setBackground(Color background) {
 		this.backgroundColor = background;
 	}
 	
    public Color getHoverBackground() {
        return hoverBackgroundColor;
    }

    public void setHoverBackground(Color hoverBackground) {
        this.hoverBackgroundColor = hoverBackground;     
    }

    public Color getPressedBackground() {
        return pressedBackgroundColor;
    }

    public void setPressedBackground(Color pressedBackground) {
        this.pressedBackgroundColor = pressedBackground;
    }
    
    public Color getBackgroundColor() {
    	return backgroundColor;
    }

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public int getCornerRadius(int corner) {
		return cornerRadius[corner];
	}
	
	public void setCornerRadius(int radius) {
		for(int i = 0; i < cornerRadius.length; i++) {
			cornerRadius[i] = radius;
		}
	}
	
	public void setRadius(int corner, int radius) {
		cornerRadius[corner] = radius;
	}
	
	public int getPadding(int side) {
		return padding[side];
	}
	
	public void setPadding(int padding) {
		for(int i = 0; i < this.padding.length; i++) {
			this.padding[i] = padding;
		}
		updateBorder();
	}
	
	public void setPadding(int side, int padding) {
		this.padding[side] = padding;
		updateBorder();
	}
	
	private void updateBorder() {
		setBorder(new EmptyBorder(getPadding(TOP), getPadding(LEFT), getPadding(BOTTOM), getPadding(RIGHT)));
	}

	public Color getGradColorTop() {
		return gradColorTop;
	}
	
	public void setGradient(Color color) {
		setGradient(color, color.darker());
	}
	
	public void setGradient(int gradientType, Color color) {
		setGradient(gradientType, color, color.darker());
	}

	public void setGradient(Color topColor, Color botColor) {
		setGradient(ColorUtil.GRAD_VERT, topColor, botColor);
	}
	
	public void setGradient(int gradientType, Color topColor, Color botColor) {
		setGradientType(gradientType);
		setGradColorTop(topColor);
		setGradColorBottom(botColor);
	}
	
	public void setGradColorTop(Color gradColorTop) {
		this.gradColorTop = gradColorTop;
	}

	public Color getGradColorBottom() {
		return gradColorBottom;
	}

	public void setGradColorBottom(Color gradColorBottom) {
		this.gradColorBottom = gradColorBottom;
	}
	
	public int getGradientType() {
		return gradientType;
	}

	public void setGradientType(int gradientType) {
		this.gradientType = gradientType;
	}

}
