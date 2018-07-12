package search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import util.ImageUtil;
import util.ManaUtil;

public class ManaPanel extends PPanel {
	
	protected PCheckBox whiteCheck;
	protected PCheckBox blueCheck;
	protected PCheckBox blackCheck;
	protected PCheckBox redCheck;
	protected PCheckBox greenCheck;
	protected PCheckBox colorlessCheck;
	protected PCheckBox xCheck;
	
	protected JToggleButton anyButton;
	protected JToggleButton exactlyButton;
	protected boolean enableListeners = true;
	
	public ManaPanel() {
		this(true);
	}
	
	public ManaPanel(boolean showButtons) {
		super();
		enableListeners = false;
		init(showButtons);
		enableListeners = true;
	}
	
	protected void init(boolean showButtons) {
		whiteCheck = new PCheckBox("");
		whiteCheck.setSelected(true);
		JLabel whiteLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("W"), "W")));
		whiteLabel.setToolTipText("Card cost has white");
		
		blueCheck = new PCheckBox("");
		blueCheck.setSelected(true);
		JLabel blueLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("U"), "U")));
		blueLabel.setToolTipText("Card cost has blue");
		
		blackCheck = new PCheckBox("");
		blackCheck.setSelected(true);
		JLabel blackLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("B"), "B")));
		blackLabel.setToolTipText("Card cost has black");
		
		redCheck = new PCheckBox("");
		redCheck.setSelected(true);
		JLabel redLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("R"), "R")));
		redLabel.setToolTipText("Card cost has red");
		
		greenCheck = new PCheckBox("");
		greenCheck.setSelected(true);
		JLabel greenLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("G"), "G")));
		greenLabel.setToolTipText("Card cost has green");
		
		colorlessCheck = new PCheckBox("");
		colorlessCheck.setSelected(true);
		JLabel colorlessLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("C"), "C")));
		colorlessLabel.setToolTipText("Card must be completely colorless.");
		
		xCheck = new PCheckBox("");
		xCheck.setSelected(false);
		JLabel xLabel = new JLabel(new ImageIcon(ImageUtil.readImage(ManaUtil.getSmallUrl("X"), "X")));
		xLabel.setToolTipText("<html>If checked, card cost MUST have X.<br>If unchecked, card cost MAY have X.</html>");
		
		anyButton = new JToggleButton("Has any of these");
		anyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				anyButton.setText(anyButton.isSelected() ? "Has all of these" : "Has any of these");
			}
			
		});

		exactlyButton = new JToggleButton("and nothing else");
		exactlyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exactlyButton.setText(exactlyButton.isSelected() ? "and possibly more" : "and nothing else");
			}
			
		});
		
		whiteCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(whiteCheck);
			}
			
		});

		blueCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(blueCheck);
			}
			
		});
		
		blackCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(blackCheck);
			}
			
		});
		
		redCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(redCheck);
			}
			
		});
		
		greenCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(greenCheck);
			}
			
		});
		
		colorlessCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(colorlessCheck);
			}
			
		});
		
		xCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				handleChangeInternal(xCheck);
			}
			
		});
		
		c.insets(0, 10);
		c.weaken();
		add(whiteCheck, c);
		c.insets();
		c.gridx++;
		add(whiteLabel, c);
		
		c.gridx++;
		c.insets(0, 10);
		add(blueCheck, c);
		c.insets();
		c.gridx++;
		add(blueLabel, c);
		
		c.gridx++;
		c.insets(0, 10);
		add(blackCheck, c);
		c.insets();
		c.gridx++;
		add(blackLabel, c);
		
		c.gridx++;
		c.insets(0, 10);
		add(redCheck, c);
		c.insets();
		c.gridx++;
		add(redLabel, c);
		
		c.gridx++;
		c.insets(0, 10);
		add(greenCheck, c);
		c.insets();
		c.gridx++;
		add(greenLabel, c);
		
		c.gridx++;
		c.insets(0, 10);
		add(colorlessCheck, c);
		c.insets();
		c.gridx++;
		add(colorlessLabel, c);
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 10;
		PPanel optionPanel = new PPanel();
		add(optionPanel, c);
		c.gridwidth = 1;
		
		if(showButtons) {
			c.gridx = 10;
			c.insets(0, 10);
			add(xCheck, c);
			c.insets();
			c.gridx++;
			add(xLabel, c);
			optionPanel.c.insets();
			optionPanel.add(anyButton, optionPanel.c);
			optionPanel.c.gridx++;
			optionPanel.add(exactlyButton, optionPanel.c);
		}
		
		c.strengthen();
		add(Box.createHorizontalStrut(1), c);
	}
	
	private void handleChangeInternal(PCheckBox check) {
		if(enableListeners) {
			handleChange(check);
		}
	}

	/**
	 * Override this to handle when a check changes
	 */
	public void handleChange(PCheckBox check) {
		
	}
	
	public void uncheckAll() {
		enableListeners = false;
		whiteCheck.setSelected(false);
		blueCheck.setSelected(false);
		blackCheck.setSelected(false);
		redCheck.setSelected(false);
		greenCheck.setSelected(false);
		colorlessCheck.setSelected(false);
		xCheck.setSelected(false);
		enableListeners = true;
	}
	
	public String getSelectedManaString() {
		StringBuilder sb = new StringBuilder();
		
		if(whiteCheck.isSelected()) {
			sb.append("W");
		}
		if(blueCheck.isSelected()) {
			sb.append("U");
		}
		if(blackCheck.isSelected()) {
			sb.append("B");
		}
		if(redCheck.isSelected()) {
			sb.append("R");
		}
		if(greenCheck.isSelected()) {
			sb.append("G");
		}
		if(colorlessCheck.isSelected()) {
			sb.append("C");
		}
		if(xCheck.isSelected()) {
			sb.append("X");
		}
	
		return sb.toString();
	}	
	
	public void setSelectedManaString(String manaString) {
		if(manaString == null) {
			manaString = "";
		}
		enableListeners = false;
		whiteCheck.setSelected(manaString.contains("W"));
		blueCheck.setSelected(manaString.contains("U"));
		blackCheck.setSelected(manaString.contains("B"));
		redCheck.setSelected(manaString.contains("R"));
		greenCheck.setSelected(manaString.contains("G"));
		colorlessCheck.setSelected(manaString.contains("C"));
		xCheck.setSelected(manaString.contains("X"));
		enableListeners = true;
	}
	
	public boolean match(String colors, String manaCost) {
		boolean exact = !exactlyButton.isSelected();
		boolean any = !anyButton.isSelected();
		
		// Handle case where colors may not reflect manacost
		if(colors.isEmpty() && !manaCost.isEmpty()) {
			colors = ManaUtil.manaToColors(manaCost).toString();
		}
		
		int matchCount = 0;
		int falseMatchCount = 0;
		int targetMatchCount = 0;
		
		if(whiteCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasWhite(colors)) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasWhite(colors)) {
				falseMatchCount++;
			}
		}

		if(blueCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasBlue(colors)) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasBlue(colors)) {
				falseMatchCount++;
			}
		}
		
		if(blackCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasBlack(colors)) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasBlack(colors)) {
				falseMatchCount++;
			}
		}
		
		if(redCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasRed(colors)) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasRed(colors)) {
				falseMatchCount++;
			}
		}
		
		if(greenCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasGreen(colors)) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasGreen(colors)) {
				falseMatchCount++;
			}
		}
		
		boolean colorless = matchCount == 0 && falseMatchCount == 0;
		if(colorless) {
			return colorlessCheck.isSelected();
		}
		
		if(xCheck.isSelected()) {
			targetMatchCount++;
			if(ManaUtil.hasManaSymbol(manaCost, "X")) {
				matchCount++;
			}
		} else {
			if(ManaUtil.hasManaSymbol(manaCost, "X")) {
				//falseMatchCount++;
			}
		}
		
		boolean returnValue;
		if(any) {
			if(exact) {
				returnValue = (matchCount > 0 || targetMatchCount == 0) && falseMatchCount == 0; 
			} else {
				returnValue = (matchCount >= 0 || targetMatchCount == 0);
			}
		} else {
			if(exact) {
				returnValue = matchCount == targetMatchCount && falseMatchCount == 0;
			} else {
				returnValue = matchCount >= targetMatchCount;
			}
		}
		
		if(returnValue) {
			return true;
		}
		return false;
	};

}
