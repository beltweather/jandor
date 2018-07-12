package search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import json.JSONObject;

import ui.GlassPane;
import ui.ProgressBar;
import ui.ProgressBar.ProgressTask;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import ui.view.QuickView;
import util.CardUtil;
import util.ImageUtil;
import util.UIManagerUtil;
import deck.Deck;

public abstract class SearchPanel<T, R> extends PPanel {

	private static final long serialVersionUID = 1L;

	protected SearchNode<T> rootNode;
	protected JButton evalButton;
	protected ProgressBar progressBar;
	protected JLabel evalLabel;
	protected JButton multiButton;
	protected JButton singleButton;
	protected JToggleButton hideButton;
	protected boolean fullHideText = false;
	
	public SearchPanel() {
		super();
		init();
	}
	
	private void init() {
		evalButton = new JButton("Search");
		evalButton.setIcon(ImageUtil.getImageIcon("search.png"));
		/*evalButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				search();
			}
			
		});*/
		
		progressBar = new ProgressBar(evalButton) {

			@Override
			public void run(ProgressTask task) {
				search(task);
			}

			@Override
			public void finished(ProgressTask task) {
				
			}
			
		};
		progressBar.setPreferredSize(new Dimension(100, 20));
		
		multiButton = new JButton("Multi");
		multiButton.setIcon(ImageUtil.getImageIcon("close_button.png"));
		multiButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setToDefaultMulti();
				clearSearch();
			}
			
		});
		
		singleButton = new JButton("Single");
		singleButton.setIcon(ImageUtil.getImageIcon("close_button.png"));
		singleButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setToDefaultSingle();
				clearSearch();
			}
			
		});
		
		hideButton = new JToggleButton("Hide");
		hideButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSearchVisiblity(hideButton.isSelected());
			}
			
		});
		
		evalLabel = new JLabel("");
		rootNode = new SearchNode<T>(this);
		setToDefaultSingle();
	}
	
	public void hideSearch() {
		updateSearchVisiblity(true);
	}
	
	private void updateSearchVisiblity(boolean hide) {
		if(hideButton.isSelected() != hide) {
			hideButton.setSelected(hide);
		}
		updateHideText();
		boolean visible = !hideButton.isSelected();
		for(Component comp : getComponents()) {
			if(comp.equals(hideButton)) {
				continue;
			}
			comp.setVisible(visible);
		}
		if(visible) {
			setPreferredSize(null);
			revalidate();
		} else {
			setPreferredSize(new Dimension(fullHideText ? 100 : 50, 0));
		}
	}
	
	public void setFullHideText(boolean fullHideText) {
		this.fullHideText = fullHideText;
		updateHideText();
	}
	
	private void updateHideText() {
		if(hideButton.isSelected()) {
			hideButton.setText(fullHideText ? "Show Search" : "Show");
		} else {
			hideButton.setText(fullHideText ? "Hide Search" : "Hide");
		}
	}
	
	public void update() {
		rebuild();
		revalidate();
		repaint();
	}
	
	public void setRootNode(SearchNode<T> rootNode) {
		this.rootNode = rootNode;
	}
	
	private void rebuild() {
		rootNode.cleanupChildren();
		removeAll();
		c.reset();
		c.weaken();
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = G.NORTHWEST;
	
		//c.insets(10,0,10);
		//add(hideButton, c);
		c.insets(20);
		add(Box.createHorizontalStrut(1), c);
		
		c.insets();
		c.gridy++;
		addEditor(0, rootNode);
		c.insets(10, 27);
		
		PPanel evalPanel = new PPanel();
		evalPanel.c.weaken();
		evalPanel.add(evalButton, evalPanel.c);
		evalPanel.c.gridy++;
		evalPanel.c.gridwidth = 2;
		evalPanel.c.strengthen();
		evalPanel.c.insets(10, 0);
		evalPanel.add(progressBar, evalPanel.c);
		evalPanel.c.weaken();
		evalPanel.c.gridwidth = 1;
		evalPanel.c.gridy--;
		evalPanel.c.gridx++;
		evalPanel.c.insets(0, 5);
		evalPanel.add(buildInfoButton(), evalPanel.c);
		evalPanel.c.insets(0, 20);
		evalPanel.c.gridx++;
		evalLabel.setPreferredSize(new Dimension(165, 20));
		evalPanel.add(evalLabel, evalPanel.c);
		evalPanel.c.gridx++;
		evalPanel.c.insets();
		evalPanel.c.gridx++;
		evalPanel.add(singleButton, evalPanel.c);
		evalPanel.c.insets(0, 10);
		evalPanel.c.gridx++;
		evalPanel.add(multiButton, evalPanel.c);
		evalPanel.c.gridy++;
		evalPanel.c.anchor = G.EAST;
		evalPanel.c.insets(10);
		
		add(evalPanel, c);
		c.insets();
		c.gridy++;
		c.strengthen();
		c.gridx = 0;
		add(Box.createHorizontalStrut(1), c);
	}
	
	private	JLabel buildInfoButton() {
		JLabel label = new JLabel("?");
		label.setPreferredSize(new Dimension(20, 20));
		label.setToolTipText("<html><div width=\"200px\">For any text search, case doesn't matter. Wrap mana symbols in \"{}\" (eg. {W}, {U}, {B}, {R}, {G}, {C}, {X}, {2}). Text with spaces will be searched on in any order. Wrap text in quotes to search on it as a continuous chunk (eg. \"Whenever a creature\"). Place \"!\" in front of a word to search for things that don't have that word (eg. !trample). Place \"!\" in front of the first word in a quoted block to search for things that don't have that chunk of text (eg. \"!return a creature\"). Search for power and toughness just as it appears in plain text (eg. 1/3, */4). Search with regular expressions by wrapping your expression in ' (eg. '.*\\badd\\b.*\\bto your mana\\b.*').</div></html>");
		return label;
	}
	
	private void addEditor(int depth, final SearchNode<T> node) {
		node.handlePosition();
		
		if(!node.hasChildren()) {
			add(node, c);
			GlassPane gp = buildGlassPane(node);
			add(gp, c);
			setComponentZOrder(gp, 1);
			c.gridy++;
		}
		
		for(SearchNode<T> child : node.getChildren()) {
			addEditor(depth + 1, child);
		}
	}
	
	private GlassPane buildGlassPane(final SearchNode<T> node) {
		GlassPane gp = new GlassPane(node) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				node.addAndButton.show();
				node.addOrButton.show();
				node.removeButton.show();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				node.addAndButton.hide();
				node.addOrButton.hide();
				node.removeButton.hide();
				super.mouseExited(e);
			}
			
		};
		return gp;
	}
	
	public void search() {
		progressBar.trigger();
	}
	
	private void search(ProgressTask task) {
		R results = search(rootNode, task);
		if(results instanceof Collection) {
			evalLabel.setText("Found " + ((Collection) results).size() + " Results");
		} else {
			evalLabel.setText("");
		}
		handleResults(results);
	}
	
	public void clearSearch() {
		evalLabel.setText("");
		handleResults(null);
	}
	
	public abstract List<String> getAttributes();

	public abstract String getDefaultAttribute();
	
	public abstract JComponent buildEditor(String att);
	
	protected abstract boolean match(T obj, String att, JComponent editor) throws Exception;
	
	protected abstract R search(SearchNode<T> rootNode, ProgressTask task);
	
	protected abstract void handleResults(R results);
	
	public static void main(String[] args) {
		UIManagerUtil.init();
		CardUtil.init();
		
		CardSearchPanel p = new CardSearchPanel() {

			@Override
			protected void handleResults(Deck results) {
				Deck deck = (Deck) results;
				if(deck.size() <= 300) {
					Deck limitDeck = new Deck();
					for(int i = 0; i < Math.min(15, deck.size()); i++) {
						limitDeck.add(deck.get(i));
					}
					QuickView view = new QuickView("Search Results", limitDeck);
					JUtil.popupWindow("Search Results", view);
				} else {
					System.out.println("Too Many Results: " + deck.size());
				}
			}
			
		};
		JUtil.popupWindow("Search", p);
	}
	
	public void setToDefaultMulti() {
		setToDefaultSingle();
	}
	
	public void setToDefaultSingle() {
		rootNode.setChildren(null);
		rootNode.addChild();
		update();
	}
	
}
