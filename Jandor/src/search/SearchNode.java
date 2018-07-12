package search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import ui.JandorButton;
import ui.JandorCombo;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import util.ImageUtil;

public class SearchNode<T> extends PPanel {

	public static final int INDENT = 31;
	
	private static final long serialVersionUID = 1L;
	
	protected PPanel andOrPanel;
	protected PPanel removeButtonPanel;
	
	protected JandorButton removeButton;
	protected JToggleButton andOrButton;
	
	protected JandorButton addAndButton;
	protected JandorButton addOrButton;
	protected Component strut;
	protected PPanel indentPanel;
	
	protected SearchPanel<T,?> searchPanel;
	
	protected SearchNode<T> parentNode;
	protected List<SearchNode<T>> children;
	
	protected boolean isAnd;
	
	protected JComboBox<String> attributeCombo;
	protected JComponent editor;
	protected PPanel editorPanel;
	protected JToggleButton notButton;
	
	public SearchNode(SearchPanel<T,?> panel) {
		this(panel, true);
	}
	
	public SearchNode(SearchPanel<T,?> panel, boolean isAnd) {
		super();
		this.searchPanel = panel;
		this.isAnd = isAnd;
		children = new ArrayList<SearchNode<T>>();
		init();
	}
	
	protected void init() {
		indentPanel = new PPanel();
		andOrPanel = new PPanel();
		removeButtonPanel = new PPanel();
		
		addAndButton = new JandorButton("and");
		addAndButton.hide();
		addAndButton.setIcon(ImageUtil.getImageIcon("bars.png"));
		addAndButton.setPreferredSize(new Dimension(50, 15));
		addAndButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addAnd();
			}
			
		});
		
		addOrButton = new JandorButton("or");
		addOrButton.hide();
		addOrButton.setIcon(ImageUtil.getImageIcon("tab-bars.png"));
		addOrButton.setPreferredSize(new Dimension(50, 15));
		addOrButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addOr();
			}
			
		});
		
		removeButton = JUtil.buildCloseButton();
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
			
		});
		
		andOrButton = new JToggleButton("and");
		andOrButton.setPreferredSize(new Dimension(31, 20));
		updateAndOrButton();
		andOrButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isAnd = !andOrButton.isSelected();
				updateAndOrButton();	
				if(hasParentNode()) {
					for(SearchNode<T> node : getParentNode().getChildren()) {
						if(node.equals(SearchNode.this)) {
							continue;
						}
						node.getAndOrButton().setSelected(!isAnd);
						node.isAnd = isAnd;
						node.updateAndOrButton();
					}
				}
			}
			
		});
		
		attributeCombo = new JandorCombo(searchPanel.getAttributes());
		attributeCombo.setMaximumRowCount(attributeCombo.getItemCount());
		attributeCombo.setPreferredSize(new Dimension(100, 20));
		if(searchPanel.getDefaultAttribute() != null) {
			attributeCombo.setSelectedItem(searchPanel.getDefaultAttribute());
		}
		
		attributeCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateAttributeEditor();
			}
			
		});
		
		editorPanel = new PPanel();
		
		notButton = new JToggleButton("with");
		notButton.setPreferredSize(new Dimension(52, 20));
		notButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				notButton.setText(notButton.isSelected() ? "without" : "with");
			}
			
		});
		updateAttributeEditor();
		
		buildLayout();
	}
	
	protected void buildLayout() {
		c.insets();
		c.weaken();
		c.insets(0, 0, 0, 10);
		add(removeButtonPanel, c);
		c.insets();
		c.gridx++;
		add(indentPanel, c);
		c.gridx++;
		add(andOrPanel, c);
		c.gridx++;
		add(notButton, c);
		c.gridx++;
		add(attributeCombo, c);
		c.gridx++;
		add(editorPanel, c);
		c.gridx++;
		c.insets(0, 5, 0, 0);
		add(addAndButton, c);
		c.insets(0, 2);
		c.gridx++;
		add(addOrButton, c);
		c.insets();
		c.gridx++;
		c.strengthen();
		c.gridy++;
		add(Box.createHorizontalStrut(1), c);
	}
	
	public void handlePosition() {
		if(isSingleton()) {
			isAnd = true;
		}
		
		updateAndOrButton();		
		
		removeButtonPanel.removeAll();
		if(isSingleton()) {
			removeButtonPanel.add(Box.createHorizontalStrut(16));
		} else {
			removeButtonPanel.add(removeButton);
		}
		
		indentPanel.removeAll();
		int indentCount = getDepth() - 1;
		if(!isSingleton() && isFirstSibling()) {
			indentCount -= 1;
		} else if(isSingleton()) {
			indentCount = 0;
		}
		indentPanel.add(Box.createHorizontalStrut(INDENT*indentCount));
	
		if(isFirstSibling() && ((hasParentNode() && getParentNode().isRoot()) || !isOnlySibling())) {
			notButton.setPreferredSize(new Dimension(83, 20));
		} else {
			notButton.setPreferredSize(new Dimension(52, 20));
		}
		
		andOrPanel.removeAll();
		if(!isSingleton()) {
			if(isFirstSibling() && hasParentNode() && !getParentNode().isSingleton()) {
				andOrPanel.add(getParentNode().getAndOrButton());
			} else if(!isFirstSibling()) {
				andOrPanel.add(andOrButton);
			}
		}
	}
	
	public void cleanupChildren() {
		SearchNode<T> node = this;
		while(node.getChildrenCount() == 1) {
			node = node.getChildren().get(0);
		}
		if(!node.equals(this)) {
			clearChildren();
			addChild(node);
		}
	}
	
	public boolean isSingleton() {
		if(isRoot()) {
			return true;
		}
		if(!isOnlySibling()) {
			return false;
		}
		SearchNode<T> p = getParentNode();
		while(p != null) {
			if(!p.isOnlySibling()) {
				return false;
			} 
			p = p.getParentNode(); 
		}
		return true;
	}
	
	public void updateAndOrButton() {
		if(!andOrButton.isSelected() && !isAnd) {
			andOrButton.setSelected(!isAnd);
		} else if(andOrButton.isSelected() && isAnd) {
			andOrButton.setSelected(isAnd);
		}
		andOrButton.setText(andOrButton.isSelected() ? "or" : "and");
		
		addAndButton.setIcon(ImageUtil.getImageIcon(isAnd ? "bars.png" : "tab-bars.png"));
		addOrButton.setIcon(ImageUtil.getImageIcon(isAnd ? "tab-bars.png" : "bars.png"));
	}
	
	public JButton getRemoveButton() {
		return removeButton;
	}
	
	public JToggleButton getAndOrButton() {
		return andOrButton;
	}
	
	public void add(SearchNode<T> node) {
		addChild(node);
		searchPanel.update();
	}
	
	public void remove() {
		if(hasParentNode()) {
			SearchNode<T> parentNode = getParentNode();
			removeButton.setVisible(false);
			parentNode.removeChild(this);
			searchPanel.update();
			if(!parentNode.hasChildren()) {
				parentNode.remove();
			}
		}
	}
	
	public SearchNode<T> getParentNode() {
		return parentNode;
	}
	
	public boolean hasParentNode() {
		return parentNode != null;
	}
	
	public void setParentNode(SearchNode<T> parent) {
		this.parentNode = parent;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public void addChild(SearchNode<T> child) {
		children.add(child);
		child.setParentNode(this);
	}
	
	public int indexOfChild(SearchNode<T> child) {
		return children.indexOf(child);
	}
	
	public SearchNode<T> addChild(int i, SearchNode<T> child) {
		if(i == children.size()) {
			children.add(child);
		} else {
			children.add(i, child);
		}
		child.setParentNode(this);
		return child;
	}
	
	public void removeChild(SearchNode<T> child) {
		children.remove(child);
		child.setParentNode(null);
	}
	
	public List<SearchNode<T>> getChildren() {
		return children;
	}
	
	public int getChildrenCount() {
		return children.size();
	}
	
	public void clearChildren() {
		for(SearchNode<T> child : children) {
			child.setParentNode(null);
		}
		children.clear();
	}
	
	public void setChildren(List<SearchNode<T>> children) {
		clearChildren();
		if(children == null) {
			return;
		}
		for(SearchNode<T> child : children) {
			addChild(child);
		}
	}
	
	public boolean isRoot() {
		return !hasParentNode();
	}
	
	public boolean isFirstSibling() {
		return isRoot() || getParentNode().indexOfChild(this) == 0;
	}
	
	public boolean isLastSibling() {
		return isRoot() || getParentNode().indexOfChild(this) == getParentNode().getChildrenCount() - 1;
	}
	
	public boolean isOnlySibling() {
		return isRoot() || getParentNode().getChildrenCount() == 1;
	}
	
	public SearchNode<T> addParent() {
		if(isRoot()) {
			SearchNode<T> parent = new SearchNode<T>(searchPanel);
			parent.addChild(this);
			searchPanel.setRootNode(parent);
			searchPanel.update();
			return parent;
		}
		
		SearchNode<T> parent = getParentNode();
		SearchNode<T> newParent = new SearchNode<T>(searchPanel);
		int idx = parent.indexOfChild(this);
		parent.addChild(idx, newParent);
		parent.removeChild(this);
		newParent.addChild(this);
		searchPanel.update();
		
		return newParent;
	}
	
	public SearchNode<T> addSibling() {
		return addSibling(true);
	}
	
	public SearchNode<T> addSibling(boolean isAnd) {
		if(isRoot()) {
			return null;
		}
		int idx = getParentNode().indexOfChild(this) + 1;
		getParentNode().addChild(idx, isAnd);
		searchPanel.update();
		return getParentNode().getChildren().get(idx);
	}
	
	public SearchNode<T> addChild() {
		return addChild(true);
	}
	
	public SearchNode<T> addChild(boolean isAnd) {
		return addChild(children.size(), isAnd);
	}
	
	public SearchNode<T> addChild(int idx, boolean isAnd) {
		addChild(idx, new SearchNode<T>(searchPanel, isAnd));
		searchPanel.update();
		return children.get(idx);
	}
	
	public void addOr() {
		if(isRoot()) {
			addChild(false);
			return;
		}
		if(!isAnd) {
			addSibling(false);
			return;
		}
		SearchNode<T> sibling = addSibling(true);
		sibling.addChild(false);
		sibling.addChild(false);
	}
	
	public void addAnd() {
		if(isRoot()) {
			addChild(true);
			return;
		}
		if(isAnd) {
			addSibling(true);
			return;
		}
		SearchNode<T> sibling = addSibling(false);
		sibling.addChild(true);
		sibling.addChild(true);
	}

	public int getDepth() {
		int depth = 0;
		SearchNode<T> n = this;
		while(n.hasParentNode()) {
			n = n.getParentNode();
			depth++;
		}
		return depth;
	}
	
	public boolean evaluate(T obj) throws Exception {
		if(!hasChildren()) {
			boolean match = isMatch(obj);
			if(notButton.isSelected()) {
				return !match;
			}
			return match;
		}
		for(SearchNode<T> child : children) {
			boolean match = child.evaluate(obj);
			if(child.isAnd && !match) {
				return false;
			} else if(!child.isAnd && match) {
				return true;
			}
		}
		return children.get(0).isAnd; // Being clever here
	}
	
	public boolean isMatch(T obj) throws Exception {
		return searchPanel.match(obj, attributeCombo.getSelectedItem().toString(), editor);
	}

	protected void updateAttributeEditor() {
		String att = attributeCombo.getSelectedItem().toString();
		editor = searchPanel.buildEditor(att);
		if(editor == null) {
			return;
		}
		
		editorPanel.removeAll();
		editorPanel.c.reset();
		editorPanel.add(editor, editorPanel.c);
		
		searchPanel.revalidate();
	}
	
	public JComboBox<String> getAttributeCombo() {
		return attributeCombo;
	}

}
