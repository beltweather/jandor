package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import ui.pwidget.ColorUtil;

public class UIManagerUtil {

	private UIManagerUtil() {}
	
	public static void init() {
		try {
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		UIManager.getLookAndFeelDefaults().put("Panel.background", ColorUtil.DARK_GRAY_3);//new Color(207, 207, 207));
		UIManager.getLookAndFeelDefaults().put("Label.foreground", Color.WHITE);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		//ToolTipManager.sharedInstance().setInitialDelay(0);
		UIManager.put("ToolTip.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("ToolTip.foreground", Color.WHITE);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		Border margin = new EmptyBorder(10,10,10,10);
		UIManager.put("ToolTip.border", new CompoundBorder(border, margin));
		
		UIManager.put("ComboBox.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("ComboBox.foreground", Color.WHITE);
		
		UIManager.put("Spinner.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("Spinner.foreground", Color.WHITE);

		UIManager.put("TextField.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("TextField.foreground", Color.WHITE);
		UIManager.put("TextField.caretForeground", Color.WHITE);
		UIManager.put("TextField.selectionBackground", Color.WHITE);
		
		UIManager.put("PasswordField.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("PasswordField.foreground", Color.WHITE);
		UIManager.put("PasswordField.caretForeground", Color.WHITE);
		UIManager.put("PasswordField.selectionBackground", Color.WHITE);
		
		UIManager.put("TextArea.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("TextArea.foreground", Color.WHITE);
		UIManager.put("TextArea.caretForeground", Color.WHITE);
		UIManager.put("TextArea.selectionBackground", Color.WHITE);
		
		UIManager.put("ScrollPane.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("ScrollPane.foreground", Color.GREEN);
		UIManager.put("ScrollBar.background", ColorUtil.DARK_GRAY_0);
		UIManager.put("ScrollBar.foreground", Color.BLUE);
		UIManager.put("ScrollBar.thumb", ColorUtil.DARK_GRAY_0);
		UIManager.put("ScrollBar.thumbDarkShadow", Color.WHITE);
		UIManager.put("ScrollBar.thumbHighlight", ColorUtil.DARK_GRAY_0);
		UIManager.put("ScrollBar.thumbShadow", ColorUtil.DARK_GRAY_0);
		UIManager.put("ScrollBar.track", ColorUtil.DARK_GRAY_1);
		UIManager.put("ScrollBar.trackHighlight", ColorUtil.DARK_GRAY_1);
		
		UIManager.put("TabbedPane.focus", ColorUtil.DARK_GRAY_3); 
		UIManager.put("TabbedPane.contentAreaColor", ColorUtil.DARK_GRAY_3); 
		UIManager.put("TabbedPane.selected", ColorUtil.DARK_GRAY_3);
		
		UIManager.put("TabbedPane.background", ColorUtil.DARK_GRAY_1);
		UIManager.put("TabbedPane.borderHightlightColor", ColorUtil.TRANSPARENT); 
		
		UIManager.put("TabbedPane.shadow", ColorUtil.DARK_GRAY_0);      
		UIManager.put("TabbedPane.darkShadow", Color.BLACK);
		
		UIManager.put("OptionPane.foreground", Color.WHITE);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		UIManager.put("OptionPane.background", ColorUtil.DARK_GRAY_3);
		
		UIManager.put("Button.foreground", Color.WHITE);
		Color top = ColorUtil.DARK_GRAY_0;
		Color bot = ColorUtil.darker(ColorUtil.DARK_GRAY_0);
		List<Object> gradients = new ArrayList<Object>(5);
        gradients.add(0.3f);
        gradients.add(0.0f);
        gradients.add(top);
        gradients.add(top);
        gradients.add(bot);
		UIManager.put("Button.gradient", gradients);
		
		UIManager.put("Button.focus", ColorUtil.LIGHT_GRAY_0);
		UIManager.put("Button.darkShadow", Color.BLACK);
		UIManager.put("Button.select", ColorUtil.DARK_GRAY_1);

		UIManager.put("Button.border", BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
																		  BorderFactory.createEmptyBorder(2, 4, 2, 4)));
		
		UIManager.put("ToggleButton.foreground", Color.WHITE);
		UIManager.put("ToggleButton.gradient", gradients);
		
		UIManager.put("ToggleButton.focus", ColorUtil.LIGHT_GRAY_0);
		UIManager.put("ToggleButton.darkShadow", Color.BLACK);
		UIManager.put("ToggleButton.select", ColorUtil.DARK_GRAY_1);

		UIManager.put("ToggleButton.border", BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
																		  BorderFactory.createEmptyBorder(2, 4, 2, 4)));
		
		UIManager.put("MenuBar.border", ColorUtil.DARK_GRAY_0);
		UIManager.put("Menu.border", ColorUtil.DARK_GRAY_0);
		UIManager.put("MenuItem.border", ColorUtil.DARK_GRAY_0);
		UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(ColorUtil.DARK_GRAY_3));
		UIManager.put("Separator.foreground", ColorUtil.DARK_GRAY_3);
		
		/*for(Object key : UIManager.getLookAndFeel().getDefaults().keySet()) {
			if(!key.toString().contains("Button") || !UIManager.get(key).toString().contains("Color")) {
				continue;
			}
		    System.out.println(key + " = " + UIManager.get(key));
		}*/
	}
	
}
