package accordion;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import ui.pwidget.JUtil;
import ui.view.JandorView;

public class PAccordionData {
	
	private static int idGenerator = 0;
	private static int nextId() {
		return idGenerator++;
	}

	private static String getWhiteSpace(int depth) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
	
	protected int id; 
	protected String text;
	protected JComponent component;
	protected JComponent headerComponent;
	protected JComponent footerComponent;
	protected boolean defaultExpanded = true;
	
	protected boolean removeable = true;
	protected PAccordionData parent;
	protected List<PAccordionData> children = new ArrayList<PAccordionData>();
	
	protected PAccordionPanel accordionPanel;
	
	public PAccordionData() {
		this.id = nextId();
	}
	
	public PAccordionData(String text) {
		this(text, null);
	}

	public PAccordionData(String text, JComponent component) {
		this.id = nextId();
		this.text = text;
		setComponent(component);
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public PAccordionData getParent() {
		return parent;
	}

	public void setParent(PAccordionData parent) {
		this.parent = parent;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}

	public List<PAccordionData> getChildren() {
		return children;
	}

	public void setChildren(List<PAccordionData> children) {
		this.children = children;
	}
	
	public void addChild(PAccordionData child) {
		if(!children.contains(child)) {
			children.add(child);
		}
		child.setParent(this);
	}
	
	public void removeChild(PAccordionData child) {
		if(children.contains(child)) {
			children.remove(child);
		}
		child.setParent(null);
	}

	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
		if(component != null && component instanceof JandorView) {
			((JandorView) component).setAccordionData(this);
		}
	}
	
	public boolean hasHeaderComponent() {
		return headerComponent != null;
	}
	
	public JComponent getHeaderComponent() {
		return headerComponent;
	}

	public void setHeaderComponent(JComponent component) {
		this.headerComponent = component;
	}
	
	public boolean hasFooterComponent() {
		return footerComponent != null;
	}
	
	public JComponent getFooterComponent() {
		return footerComponent;
	}

	public void setFooterComponent(JComponent component) {
		this.footerComponent = component;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}
	
	public String getFormattedText() {
		int extraDepth = removeable ? 0 : 1; 
		return JUtil.toVerticalText("  " /*+ getWhiteSpace(getDepth() + extraDepth)*/ + text.replace(".dec", ""));
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public boolean isRemoveable() {
		return removeable;
	}

	public void setRemoveable(boolean removeable) {
		this.removeable = removeable;
	}
	
	public PAccordionPanel getAccordionPanel() {
		return accordionPanel;
	}

	public void setAccordionPanel(PAccordionPanel accordionPanel) {
		this.accordionPanel = accordionPanel;
	}
	
	public boolean isDefaultExpanded() {
		return defaultExpanded;
	}
	
	public void setDefaultExpanded(boolean defaultExpanded) {
		this.defaultExpanded = defaultExpanded;
	}
	
	public int getDepth() {
		if(!hasParent()) {
			return 0;
		}
		return getParent().getDepth() + 1;
	}
	
	@Override
	public int hashCode() {
		return getId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PAccordionData)) {
			return false;
		}
		return ((PAccordionData) obj).getId() == getId();
	}
	
}
