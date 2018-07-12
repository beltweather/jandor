package accordion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.pwidget.CloseListener;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PNestedScrollPane;
import ui.pwidget.PPanel;
import ui.view.JandorView;
import util.ImageUtil;

public class PAccordionPanel extends PPanel implements CloseListener { 

	protected PAccordion accordion;
	protected PAccordionData accordionData;
	protected PAccordionButton expandButton;
	protected PPanel borderPanel;
	protected PNestedScrollPane scrollPane;
	
	protected PAccordionPanel(PAccordion accordion, PAccordionData accordionData) {
		super();
		this.accordion = accordion;
		this.accordionData = accordionData;
		this.accordionData.setAccordionPanel(this);
		init();
	}
	
	public PAccordionData getAccordionData() {
		return accordionData;
	}
	
	public PAccordion getAccordion() {
		return accordion;
	}
	
	private void init() {

		scrollPane = new PNestedScrollPane(accordionData.getComponent());
		
		borderPanel = new PPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.add(scrollPane, BorderLayout.CENTER);
		
		if(accordionData.hasHeaderComponent()) {
			borderPanel.add(accordionData.getHeaderComponent(), BorderLayout.NORTH);
		}
		
		if(accordionData.hasFooterComponent()) {
			borderPanel.add(accordionData.getFooterComponent(), BorderLayout.SOUTH);
		}
		
		// DEBUG
		//borderPanel.add(buildBox(200, 100, Color.GREEN), BorderLayout.NORTH);
		//borderPanel.add(buildBox(200, 100, Color.GREEN), BorderLayout.SOUTH);
		
		final boolean removeable = accordionData.isRemoveable();
		
		final PButton closeButton = new PButton();
		if(removeable) {
			closeButton.setIcon(ImageUtil.getImageIcon("close_button.png"));
			closeButton.setRolloverIcon(ImageUtil.getImageIcon("close_button_full.png"));
			closeButton.setPressedIcon(ImageUtil.getImageIcon("close_button_full_down.png"));
		} else {
			closeButton.setEnabled(false);
		}
		
		closeButton.setCornerRadius(0);
		closeButton.setPreferredSize(new Dimension(30, 30));
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
			
		});
		
		expandButton = new PAccordionButton(accordionData) {

			@Override
			public void actionPerformed(boolean nowExpanded) {
				if(nowExpanded) {
					borderPanel.setVisible(true);
				} else {
					borderPanel.setVisible(false);
				}
				revalidate();
			}
			
		};
		
		if(!accordionData.isDefaultExpanded()) {
			borderPanel.setVisible(false);
		}
		
		c.weaken();
		
		c.weighty = 0.0001;
		PPanel depthSpacer = new PPanel();
		depthSpacer.setOpaque(true);
		depthSpacer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		depthSpacer.setBackground(ColorUtil.DARK_GRAY_0);
		depthSpacer.setPreferredSize(new Dimension(30, getAccordionData().getDepth() * 30 + (removeable ? 0 : 30)));
		
		if(removeable) {
			JPanel p = JUtil.lockSize(closeButton); 
			closeButton.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					ButtonModel model = (ButtonModel) e.getSource();
					setMarkedForRemoval(model.isRollover());
				}

			});
		
			add(p, c);
			c.gridy++;
		}
		
		add(JUtil.lockSize(depthSpacer), c);
		c.gridy++;
		
		c.fill = G.VERTICAL;
		c.weighty = 1.0;
		
		add(expandButton, c);
		c.gridy = 0;
		c.gridx++;
		c.gridheight = removeable ? 3 : 2;
		c.strengthen();
		c.insets(10, 0, 10, 10); // Margin around components in this panel
		add(borderPanel, c);
	}
	
	public void remove() {
		if(accordionData.isRemoveable()) {
			boolean doRemove = true;
			if(accordionData.getComponent() instanceof JandorView) {
				JandorView view = (JandorView) accordionData.getComponent();
				if(view.isModified()) {
					doRemove = JUtil.showConfirmYesNoCancelDialog(view, "Close " + view.getName(), "Are you sure you want to close " + view.getName() + "?");
				}
			}
			if(doRemove) {
				accordion.removeAccordionPanel(PAccordionPanel.this);
			}
		}
	}
	
	protected void setMarkedForRemoval(boolean remove) {
		expandButton.setMarkedForRemoval(remove);
		for(PAccordionData childData : getAccordionData().getChildren()) {
			childData.getAccordionPanel().setMarkedForRemoval(remove);
		}
		expandButton.repaint();	
	}
	
	private static JComponent buildBox(int w, int h, Color color) {
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setBackground(color);
		p.setPreferredSize(new Dimension(w, h));
		return JUtil.lockSize(p);
	}
	
	public void expand() {
		expandButton.setExpanded(true);
		borderPanel.setVisible(true);
		revalidate();
	}
	
	public boolean isExpanded() {
		return expandButton.expanded;
	}
	
	public void contract() {
		expandButton.setExpanded(false);
		borderPanel.setVisible(false);
		revalidate();
	}
	
	public void contractAll() {
		expandButton.setExpanded(false);
		borderPanel.setVisible(false);
		revalidate();
		for(PAccordionData data : accordionData.getChildren()) {
			data.getAccordionPanel().contractAll();
		}
	}
	
	public void contractChildren() {
		for(PAccordionData data : accordionData.getChildren()) {
			data.getAccordionPanel().contractAll();
		}
	}
	
	public int getId() {
		return accordionData == null ? -1 : accordionData.getId();
	}
	
	public PAccordionButton getExpandButton() {
		return expandButton;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public JScrollPane getParentScrollPane() {
		return (JScrollPane) getParent().getParent().getParent();
	}
	
	
	@Override
	public int hashCode() {
		return getId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PAccordionPanel)) {
			return false;
		}
		return ((PAccordionPanel) obj).getId() == getId();
	}

	@Override
	public void handleClosed() {
		if(accordionData.getComponent() instanceof CloseListener) {
			((CloseListener) accordionData.getComponent()).handleClosed();
		}
	}
}
