package ui.pwidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ui.view.BoardView;
import ui.view.CollectionEditorView;
import ui.view.JandorView;
import util.DebugUtil;
import util.ImageUtil;
import accordion.PAccordion;
import accordion.PAccordionPanel;
import deck.Deck;

public class JandorTabFrame extends JFrame implements CloseListener {

	private static List<JandorTabFrame> frames = new ArrayList<JandorTabFrame>();
	
	private static JandorTabFrame shareFrame = null;
	
	public static boolean hasShareFrame() {
		return shareFrame != null;
	}

	public static JandorTabFrame getShareFrame() {
		
		return shareFrame;
	}
	
	public static void setShareFrame(JandorTabFrame frame) {
		if(shareFrame != null && !shareFrame.equals(frame)) {
			closeShareFrame();
		}
		shareFrame = frame;
	}
	
	public static void closeShareFrame() {
		if(hasShareFrame()) {
			shareFrame.close();
			shareFrame = null;
		}
	}
	
	public static void clearShareFrame() {
		shareFrame = null;
	}
	
	public static boolean isSingleFrameOpen() {
		return frames.size() == 1;
	}
	
	public static JandorTabFrame getSingleFrame() {
		if(!isSingleFrameOpen()) {
			return null;
		}
		return frames.get(0);
	}
	
	public static List<JandorTabFrame> getAllFrames() {
		return frames;
	}
	
	public static boolean confirmReset(Component parent) {
		return JUtil.showConfirmYesNoCancelDialog(JUtil.getFrame(parent), "Reset Tabs", "All tabs will be closed and windows reduced to one. Do you want to continue?");
	}
	
	public static boolean reset() {
		List<JandorTabFrame> frameList = new ArrayList<JandorTabFrame>(frames);
		for(JandorTabFrame frame : frameList) {
			frame.close();
		}
		
		PAccordion accordion = new PAccordion();
		CollectionEditorView.addCollectionEditorView(accordion);
		accordion.build();
		JUtil.popupWindow(CollectionEditorView.DEFAULT_TITLE, accordion);
		return true;
	}
	
	public static final String DEFAULT_TITLE = "Jandor";
	public static final Dimension DEFAULT_SIZE = new Dimension(1600, 800);
	
	private PTabPane tabPane;
	
	private boolean showMenu = true;
	
	private static String getDefaultTitle() {
		String title;
		if(DebugUtil.OFFLINE_MODE) {
			title = DEFAULT_TITLE + " (Offline)";
		} else {
			title = DEFAULT_TITLE;
		}
		return title;
	}
	
    public JandorTabFrame() {
    	this(true);
    }
	
    public JandorTabFrame(boolean showMenu) {
        super(getDefaultTitle());
        this.showMenu = showMenu;
    	init();
    	frames.add(this);
    	addWindowListener(new java.awt.event.WindowAdapter() {
    	    @Override
    	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    	    	if(frames.contains(JandorTabFrame.this)) {
    	    		frames.remove(JandorTabFrame.this);
    	    	}
    	    	handleClosed();
    	    	if(frames.size() == 1 && hasShareFrame()) {
    	    		getShareFrame().close();
    	    	}
    	    	if(frames.size() == 0) {
    	    		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	    	}
    	    }
    	});
    }

    private void init() {

    	tabPane = new PTabPane(this);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(DEFAULT_SIZE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabPane, BorderLayout.CENTER);
        
        pack();
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		setIconImage(ImageUtil.getJandorIcon());

		if(showMenu) {
			setJMenuBar(new JandorMenuBar(this));
		}
		
		setMinimumSize(new Dimension(1100, 700));
		
		refreshTitle();
    }
    
    public void refreshTitle() { // XXX This method needs some serious looking at
    	Component comp = null;
    	JandorView view = null;
    	if(tabPane.getTabCount() == 0) {
    		comp = getContentPane();
    	} else if(tabPane.getTabCount() > 0) {
    		comp = ((JComponent) tabPane.getSelectedComponent()).getComponent(0);
    	}
    	
    	if(comp instanceof JandorView) {
    		view = (JandorView) comp;
    	} else if(comp instanceof PAccordion) {
    		for(PAccordionPanel p : ((PAccordion) comp).getAccordionPanels()) {
    			if(p.getAccordionData().getComponent() instanceof JandorView) {
    				view = (JandorView) p.getAccordionData().getComponent();
    				break;
    			}
    		}
    	}
    	
    	if(view != null && tabPane.getTabPanel(tabPane.getSelectedIndex()) != null) {
    		String fileName = view.hasOpenedFile() ? view.getOpenedFileName() : "Untitled.dec";
    		String simpleFileName = view.hasOpenedFile() ? view.getSimpleOpenedFileName() : "Untitled.dec";
    		
    		//setTitle(DEFAULT_TITLE + " - " + view.getName() + " - " + (view.isModified() ? "*" : "") + fileName);
    		//tabPane.getTabPanel(tabPane.getSelectedIndex()).setTitle(view.getName() + " - " + (view.isModified() ? "*" : "") + simpleFileName);
    		
    		if(view instanceof BoardView) {
    			tabPane.getTabPanel(tabPane.getSelectedIndex()).setTitle(toBoardTitle(view));
    		}

    		/*if(view.hasAccordionData()) {
    			view.getAccordionData().setText((view.isModified() ? "*" : "") + simpleFileName);
    			view.getAccordionData().getAccordionPanel().getExpandButton().setText(view.getAccordionData().getFormattedText());
    		}*/
    	} else {
    		//setTitle(DEFAULT_TITLE);
    	}
    }
    
    public static String toBoardTitle(JandorView view) {
    	if(view instanceof BoardView) {
    		BoardView boardView = (BoardView) view;
    		if(boardView.getCardLayer().isOpponentView()) {
    			return boardView.getName();
    		}
    	}
    	return toBoardTitle(view.getDeck());
    }
    
    public static String toBoardTitle(Deck deck) {
    	return BoardView.DEFAULT_TITLE + (deck != null  && deck.getName() != null ? " - " + deck.getName() : "");
    }

    public void close() {
    	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    public void handleClosed() {
    	if(hasShareFrame() && getShareFrame().equals(this)) {
    		clearShareFrame();
    	}
    	
    	if(!getContentPane().equals(tabPane)) {
    		if(getContentPane() instanceof CloseListener) {
    			((CloseListener) getContentPane()).handleClosed();
    			return;
    		}
    	}
    	
    	for(int i = 0; i < tabPane.getTabCount(); i++) {
    		Component c = tabPane.getTabComponentAt(i);
    		if(c instanceof CloseListener) {
    			((CloseListener) c).handleClosed();
    		}
    		PTabPane tabPane = (PTabPane) c.getParent().getParent();
    		Component tabComp = tabPane.getComponents()[0];
    		if(tabComp instanceof TabContent) {
	    		TabContent tabContent = (TabContent) tabComp;
	    		for(Component comp : tabContent.getComponents()) {
	    			if(comp instanceof CloseListener) {
	    				((CloseListener) comp).handleClosed();
	    			}
	    		}
    		} else {
    			System.err.println("[JandorTabFrame:223] Tried to cast component to tab content!");
    		}
    	}
    }
    
    public PTabPane getTabPane() {
    	return tabPane;
    }
    
}