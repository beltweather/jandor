package ui.pwidget;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;


public class PButton extends JButton {

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_RIGHT = 2;
	public static final int BOTTOM_LEFT = 3;

	private Color foregroundColor;
	private Color backgroundColor;
	private Color hoverBackgroundColor;
	private Color pressedBackgroundColor;
	private Color hoverForegroundColor;
	private Color pressedForegroundColor;
	private Color borderColor;
	private int[] cornerRadius = new int[4];

	private int gradientType = ColorUtil.GRAD_VERT;
	private Color gradColorTop;
	private Color gradColorBottom;

	private boolean showBackground = true;


	public PButton() {
		super();
		init();
	}

	public PButton(String text) {
		super(text);
		init();
	}

	public PButton(String text, ActionListener actionListener) {
		super(text);
		addActionListener(actionListener);
	}

	private void init() {
		super.setContentAreaFilled(false);
		setFocusPainted(false);
		super.setBackground(new Color(0, 0, 0, 0));
		setForeground(Color.WHITE);
		setHoverForeground(Color.WHITE);
		setPressedForeground(Color.WHITE);
		setBackground(ColorUtil.DARK_GRAY_0);
		setHoverBackground(ColorUtil.DARK_GRAY_1);
		setPressedBackground(ColorUtil.DARK_GRAY_2);
		setBorderColor(Color.BLACK);
		setCornerRadius(15);
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setMinimumSize(new Dimension(75, 25));
	}

    @Override
    protected void paintComponent(Graphics g) {
    	Color foreground = getForeground();
    	if (getModel().isPressed()) {
            setForeground(pressedForegroundColor);
        } else if (getModel().isRollover()) {
            setForeground(hoverForegroundColor);
        } else {
            setForeground(foregroundColor);
        }

        if(!showBackground) {
        	super.paintComponent(g);
        	setForeground(foreground);
        	return;
        }

    	 if (getModel().isPressed()) {
             g.setColor(pressedBackgroundColor);
         } else if (getModel().isRollover()) {
             g.setColor(hoverBackgroundColor);
         } else {
             g.setColor(backgroundColor);
         }

         if(gradientType != ColorUtil.GRAD_NONE) {
 			Color top = gradColorTop == null ? g.getColor() : gradColorTop;
 			Color bot = gradColorBottom == null ? ColorUtil.darker(g.getColor()) : gradColorBottom;
 			ColorUtil.gradient(g, gradientType, top, bot, getWidth(), getHeight());
 		 }

         ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.fillRoundRect(0, 0, getWidth(), getHeight(), getCornerRadius(0), getCornerRadius(0));
         if(borderColor != null) {
        	 g.setColor(borderColor);
        	 g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getCornerRadius(0), getCornerRadius(0));
         }

         super.paintComponent(g);
         setForeground(foreground);
     }

    @Override
    public void setForeground(Color fg) {
    	this.foregroundColor = fg;
    	super.setForeground(fg);
    }

    public boolean isShowBackground() {
    	return showBackground;
    }

    public void setShowBackground(boolean showBackground) {
    	this.showBackground = showBackground;
    }

 	@Override
 	public void setContentAreaFilled(boolean b) {}

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

    public Color getHoverForeground() {
        return hoverForegroundColor;
    }

    public void setHoverForeground(Color hoverForeground) {
        this.hoverForegroundColor = hoverForeground;
    }

    public Color getPressedForeground() {
        return pressedForegroundColor;
    }

    public void setPressedForeground(Color pressedForeground) {
        this.pressedForegroundColor = pressedForeground;
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

	public void setCornerRadius(int radius, int corner) {
		cornerRadius[corner] = radius;
	}

	public Color getGradColorTop() {
		return gradColorTop;
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

	public void setWidth(int width) {
		setPreferredSize(new Dimension(width, (int) getPreferredSize().getHeight()));
	}

}
