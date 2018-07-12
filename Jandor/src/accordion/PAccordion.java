package accordion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ui.pwidget.CloseListener;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import ui.pwidget.PTabPane;
import ui.view.DeckEditorView;
import ui.view.SearchView;
import util.CardUtil;
import util.UIManagerUtil;

public class PAccordion extends PPanel implements CloseListener {

	protected List<PAccordionPanel> accordionPanels;
	protected PPanel contentPanel;
	private boolean built = false;
	
	public PAccordion() {
		super();
		this.accordionPanels = new ArrayList<PAccordionPanel>();
	}
	
	public void add(String text, JComponent component, PAccordionData parentData) {
		add(new PAccordionData(text, component), parentData);
	}
	
	public void add(String text, JComponent component, boolean child) {
		add(new PAccordionData(text, component), child);
	}
	
	public void add(PAccordionData accordionData, boolean child) {
		if(accordionPanels.size() > 0 && child) {
			add(accordionData, accordionPanels.get(accordionPanels.size() - 1).getAccordionData());
		} else {
			add(accordionData, null);
		}
	}
	
	public void add(PAccordionData accordionData, PAccordionData parentData) {
		if(parentData != null) {
			parentData.addChild(accordionData);
		}
		
		PAccordionPanel accordionPanel = new PAccordionPanel(this, accordionData);
		if(!accordionPanels.contains(accordionPanel)) {
			if(accordionData.hasParent()) {
				if(accordionData.getParent().getChildren().size() == 1) {
					int idx = accordionPanels.indexOf(accordionData.getParent().getAccordionPanel()) + 1;
					if(idx < accordionPanels.size()) {
						accordionPanels.add(idx, accordionPanel);
					} else {
						accordionPanels.add(accordionPanel);
					}
				} else {
					int siblingIdx = accordionData.getParent().getChildren().indexOf(accordionData) - 1;
					int idx = -1;
					if(siblingIdx < 0) {
						idx = accordionPanels.indexOf(accordionData.getParent().getAccordionPanel()) + 1;
					} else {
						
						PAccordionData siblingData = accordionData.getParent().getChildren().get(siblingIdx);
						while(siblingData.hasChildren()) {
							siblingData = siblingData.getChildren().get(siblingData.getChildren().size() - 1);
						}
						idx = accordionPanels.indexOf(siblingData.getAccordionPanel()) + 1;
						
					}
					if(idx < accordionPanels.size()) {
						accordionPanels.add(idx, accordionPanel);
					} else {
						accordionPanels.add(accordionPanel);
					}
				}
			} else {
				accordionPanels.add(accordionPanel);
			}
			
		}
	}
	
	protected void removeAccordionPanel(PAccordionPanel a) {
		a.handleClosed();
		
		accordionPanels.remove(a);
		contentPanel.remove(a);
		contentPanel.revalidate();
		
		if(a.getAccordionData().hasParent()) {
			a.getAccordionData().getParent().removeChild(a.getAccordionData());
		}
		if(a.getAccordionData().hasChildren()) {
			List<PAccordionData> children = new ArrayList<PAccordionData>(a.getAccordionData().getChildren()); 
			for(PAccordionData cd : children) {
				removeAccordionPanel(cd.getAccordionPanel());
			}
		}
		if(accordionPanels.size() == 0) {
			if(getParent().getParent() instanceof PTabPane) {
				PTabPane tabPane = (PTabPane) getParent().getParent();
				tabPane.close(tabPane, this);
			}
		} else if(accordionPanels.size() == 1) {
			accordionPanels.get(0).expand();
		}
	}

	public void rebuild() {
		removeAll();
		c.reset();
		build();
		revalidate();
	}
	
	public void build() {
		contentPanel = new PPanel();
		contentPanel.c.anchor = G.NORTHWEST;
		contentPanel.c.strengthen();
		contentPanel.c.weightx = 0.0001;
		contentPanel.c.fill = G.VERTICAL;
		for(PAccordionPanel accordionPanel : accordionPanels) {
			contentPanel.add(accordionPanel, contentPanel.c);
			contentPanel.c.gridx++;
		}
		contentPanel.c.strengthen();
		contentPanel.add(Box.createHorizontalStrut(1), contentPanel.c);
		
		PScrollPane scrollPane = new PScrollPane(contentPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		c.strengthen();
		add(scrollPane, c);
		built = true;
	}
	
	private static JComponent buildBox(int w, int h, Color color) {
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setBackground(color);
		p.setPreferredSize(new Dimension(w, h));
		return JUtil.lockSize(p);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if(!built) {
			System.err.println("Warning: Accordion GUI has not had \"build()\" called!");
		}
		super.paintComponent(g);
	}
	
	public List<PAccordionPanel> getAccordionPanels() {
		return accordionPanels;
	}

	public static void main(String[] args) {
		UIManagerUtil.init();
		CardUtil.init();
		
		PAccordion a = new PAccordion();
		//a.addView("A", buildBox(800, 1200, Color.RED));
		//a.addView("B", buildBox(800, 1200, Color.GREEN));
		//a.addView("C", buildBox(800, 1200, Color.BLUE));
		
		a.add("Decks", buildBox(400, 1200, Color.RED));
		a.add("Biovisionary", new DeckEditorView());
		a.add("Search", new SearchView("Deck Search", false));
		
		a.build();
		
		JUtil.popupWindow("Accordion", a);
	}

	@Override
	public void handleClosed() {
		for(PAccordionPanel p : accordionPanels) {
			p.handleClosed();
		}
	}
}
