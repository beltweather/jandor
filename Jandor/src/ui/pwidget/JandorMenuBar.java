package ui.pwidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import accordion.PAccordion;
import accordion.PAccordionPanel;
import canvas.CardLayer;
import deck.Card;
import deck.Deck;
import jackson.AllCardsJson.CardJson;
import session.Session;
import ui.AutoComboBox;
import ui.view.AboutView;
import ui.view.BoardView;
import ui.view.CollectionEditorView;
import ui.view.ControlView;
import ui.view.DeckEditorView;
import ui.view.InspectView;
import ui.view.JandorView;
import ui.view.SearchView;
import ui.view.SimpleInspectView;
import user.EditUserDialog;
import user.LoginUserDialog;
import user.UsersDialog;
import util.ApprenticeUtil;
import util.BackupUtil;
import util.CardUtil;
import util.DebugUtil;
import util.FileUtil;
import util.ImageUtil;
import util.LoginUtil;
import util.VersionUtil;
import zone.ZoneType;

public class JandorMenuBar extends JMenuBar {

	private JandorTabFrame frame;
	protected JCheckBox shareCheck;

	public JandorMenuBar(JandorTabFrame frame) {
		super();
		this.frame = frame;
		init();
	}

	private void init() {
		final PTabPane tabPane = frame.getTabPane();

        // Define and add two drop down menu to the menubar
        JMenu fileMenu = new JMenu("File");
        add(fileMenu);

        JMenu tabMenu = new JMenu("Tabs");
        add(tabMenu);

        JMenu viewMenu = new JMenu("View");
        add(viewMenu);

        JMenu searchMenu = new JMenu("Inspect");
        add(searchMenu);

        JMenu friendsMenu = new JMenu("Friends");
        add(friendsMenu);

        JMenu helpMenu = new JMenu("Help");
        add(helpMenu);

        // Create and add simple menu item to one of the drop down menu
        final JMenuItem newAction = new JMenuItem("New Game");
        JMenuItem openAction = new JMenuItem("Open...");
        final JMenuItem importAction = new JMenuItem("Import...");
        final JMenuItem saveAction = new JMenuItem("Save");
        final JMenuItem saveAsAction = new JMenuItem("Save As...");
        final JMenuItem sendAction = new JMenuItem("Email");
        JMenuItem exitAction = new JMenuItem("Exit");

        // Set keys
        setKey(newAction, KeyEvent.VK_N);
        setKey(openAction, KeyEvent.VK_O);
        setKey(saveAction, KeyEvent.VK_S);
        setKey(exitAction, KeyEvent.VK_Q);

        // Create a ButtonGroup and add both radio Button to it. Only one radio
        // button in a ButtonGroup can be selected at a time.

        fileMenu.add(newAction);
        /*fileMenu.addSeparator();
        fileMenu.add(openAction);
        fileMenu.add(importAction);
        fileMenu.addSeparator();
        fileMenu.add(saveAction);
        fileMenu.add(saveAsAction);
        fileMenu.addSeparator();
        fileMenu.add(sendAction);*/
        fileMenu.addSeparator();
        fileMenu.add(exitAction);

        final JCheckBoxMenuItem lightBoard = new JCheckBoxMenuItem("Light Background");
        lightBoard.setSelected(Session.getInstance().getPreferences().isLightView());
        viewMenu.add(lightBoard);

        final JCheckBoxMenuItem cardCounts = new JCheckBoxMenuItem("Card Counts");
        lightBoard.setSelected(Session.getInstance().getPreferences().isShowCardCounts());
        viewMenu.add(cardCounts);

        //JMenuItem addContactAction = new JMenuItem("New Contact");
        JMenuItem viewFriendsAction = new JMenuItem("View Friends");

        //contactsMenu.add(addContactAction);
        //contactsMenu.addSeparator();
        friendsMenu.add(viewFriendsAction);

        JMenuItem boardAction = new JMenuItem("New Board Tab");
        //final JCheckBoxMenuItem sharedBoardAction = new JCheckBoxMenuItem("Share Screen");
        JMenuItem searchTabAction = new JMenuItem("New Advanced Search Tab");
        JMenuItem deckEditTabAction = new JMenuItem("New Collection Tab");
        JMenuItem resetAction = new JMenuItem("Reset");

        //tabMenu.add(sharedBoardAction);
        //tabMenu.addSeparator();
        //tabMenu.add(boardAction);
        tabMenu.add(deckEditTabAction);
        //tabMenu.add(searchTabAction);
        tabMenu.addSeparator();
        tabMenu.add(resetAction);

        final JMenuItem searchDeck = new JMenuItem("Deck...");
        final JMenuItem searchGraveyard = new JMenuItem("Graveyard...");
        final JMenuItem searchExile = new JMenuItem("Exiled...");

        searchMenu.add(searchDeck);
        searchMenu.add(searchGraveyard);
        searchMenu.add(searchExile);

        JMenuItem controls = new JMenuItem("Controls...");
        JMenuItem repair = new JMenuItem("Repair...");
        JMenu cloud = new JMenu("Cloud");
        JMenuItem backup = new JMenuItem("Backup...");
        JMenuItem restore = new JMenuItem("Restore...");
        JMenuItem about = new JMenuItem("About...");

        cloud.add(backup);
        cloud.add(restore);

        helpMenu.add(controls);
        helpMenu.add(repair);
        helpMenu.add(cloud);
        helpMenu.add(about);

        fileMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent e) {
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuSelected(MenuEvent e) {
				newAction.setEnabled(hasJandorView() && getJandorView() instanceof BoardView);
			}

        });

        searchMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent e) {
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuSelected(MenuEvent e) {
				boolean enabled = hasJandorView() && getJandorView() instanceof BoardView;
				searchDeck.setEnabled(enabled);
				searchGraveyard.setEnabled(enabled);
				searchExile.setEnabled(enabled);
			}

        });

        newAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionNew();
			}

	    });

        openAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionOpen();
			}

	    });

        importAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionImport();
			}

	    });

        saveAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(hasJandorView()) {
					actionSave();
				}
			}

	    });

        saveAsAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(hasJandorView()) {
					actionSaveAs();
				}
			}

	    });

        sendAction.addActionListener(new ActionListener() {

   			@Override
   			public void actionPerformed(ActionEvent e) {
   				if(hasJandorView()) {
   					actionSend();
   				}
   			}

   	    });

        exitAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

	    });

        /*addContactAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ContactDialog dialog = new ContactDialog();
				dialog.showDialog();
			}

        });*/

        lightBoard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayer.setLightView(lightBoard.isSelected());
				Session.getInstance().getPreferences().setLightView(lightBoard.isSelected());
				Session.getInstance().getPreferences().save();
			}

        });

        cardCounts.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayer.setShowCardCounts(cardCounts.isSelected());
				Session.getInstance().getPreferences().setShowCardCounts(cardCounts.isSelected());
				Session.getInstance().getPreferences().save();
			}

        });

        viewFriendsAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionViewFriends();
			}

        });

        boardAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tabPane.addTab(BoardView.DEFAULT_TITLE, new BoardView(BoardView.DEFAULT_TITLE));
 				tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
			}

	    });

        /*sharedBoardAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionShareBoardOld(sharedBoardAction.isSelected());
			}

	    });*/

        searchTabAction.addActionListener(new ActionListener() {

 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PAccordion accordion = new PAccordion();
 				SearchView.addSearchView(accordion);
 				accordion.build();
 				tabPane.addTab(SearchView.DEFAULT_TITLE, accordion);
 				tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
 			}

     	});

        deckEditTabAction.addActionListener(new ActionListener() {

 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PAccordion accordion = new PAccordion();
 				CollectionEditorView.addCollectionEditorView(accordion);
 				accordion.build();

 				tabPane.addTab(CollectionEditorView.DEFAULT_TITLE, accordion);
 				tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
 			}

     	});

        resetAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JandorTabFrame.confirmReset(JandorMenuBar.this)) {
					JandorTabFrame.reset();
				}
			}

	    });

        // Add listener for menu open
        fileMenu.addMenuListener(new MenuListener() {

            @Override
            public void menuSelected(MenuEvent e) {
				boolean enable = hasJandorView() && getJandorView() instanceof DeckEditorView;
				saveAction.setEnabled(enable);
				saveAsAction.setEnabled(enable);
				sendAction.setEnabled(enable);
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }

        });

        searchDeck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionSearchDeck();
			}

	    });

        searchGraveyard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionSearchGraveyard();
			}

	    });

        searchExile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionSearchExile();
			}

	    });

        controls.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JUtil.showDialog(null, "Jandor - Controls", new ControlView());
			}

	    });

        repair.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!LoginUtil.isLoggedIn()) {
					JUtil.showMessageDialog(null, "Jandor - Cannot Repair", "Please login to repair Jandor files.");
					return;
				}
				if(JUtil.showConfirmDialog(null, "Jandor - Repair", "Repair current data files to match Jandor's latest version?")) {
					VersionUtil.update();
					JUtil.showMessageDialog(null, "Jandor - Repair Finished!", "Please restart Jandor.");
				}
			}

	    });

        backup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BackupUtil.backup();
			}

	    });

        restore.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BackupUtil.restore();
			}

	    });

        about.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JUtil.showDialog(null, "Jandor - About", new AboutView());
			}

	    });

        initMenuWidgets();
	}

	private void setKey(JMenuItem item, int key) {
		 KeyStroke ctrl = KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	     item.setAccelerator(ctrl);
	}

	private void initMenuWidgets() {
		final PTabPane tabPane = frame.getTabPane();

		shareCheck = new JCheckBox("Share Screen");
		shareCheck.setOpaque(false);
		shareCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionShareBoardOld(shareCheck.isSelected());
			}

		});

        PButton shareButton = new PButton("Share Screen");
        shareButton.setPreferredSize(new Dimension(100, 18));
        shareButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				actionShareBoard();
			}

        });

        JLabel searchLabel = new JLabel("Card Search");
        searchLabel.setForeground(Color.BLACK);

        PButton advancedButton = new PButton("Advanced Search");
        advancedButton.setIcon(ImageUtil.getImageIcon("search.png"));
        advancedButton.setPreferredSize(new Dimension(131, 18));
        advancedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PAccordion accordion = new PAccordion();
 				SearchView.addSearchView(accordion);
 				accordion.build();
 				tabPane.addTab(SearchView.DEFAULT_TITLE, accordion);
 				tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
			}

        });

        final AutoComboBox<String> searchCombo = new AutoComboBox<String>() {

			@Override
			public Collection<String> getSearchCollection(String searchString) {
				return CardUtil.getAllCardNames();
			}

			@Override
			public String toString(String searchedObject) {
				return searchedObject;
			}

			@Override
			public void handleFound(String cardName) {
				CardJson cardInfo = CardUtil.getCardInfo(cardName);
				if(cardInfo != null && hasJandorView()) {
					JandorView view = getJandorView();
					if(view instanceof BoardView) {
						((BoardView) view).getCardLayer().createCard(cardName);
					}
				}
			}

			@Override
			public String buildTooltip(String selectedItem) {
				return new Card(selectedItem).getToolTipText();
			}

        };
        Dimension dim = new Dimension(150,searchCombo.getPreferredSize().height);
        searchCombo.setPreferredSize(dim);
        searchCombo.setMaximumSize(dim);

        JButton addButton = new JButton("+");
        addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				searchCombo.handleFound((String) searchCombo.getSelectedItem());
			}

        });
        addButton.setPreferredSize(new Dimension(addButton.getPreferredSize().width, 18));

        final PButton loginButton = new PButton(LoginUtil.isLoggedIn() ? LoginUtil.getUser().getUsername() : "Login") {

        	@Override
			public Dimension getPreferredSize() {
				return new Dimension((int) super.getPreferredSize().width, 18);
			}

        };
        loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(LoginUtil.isLoggedIn()) {
					EditUserDialog dialog = new EditUserDialog();
					dialog.showDialog();
				} else {
					LoginUserDialog dialog = new LoginUserDialog();
					dialog.showDialog();
				}
				loginButton.setText(LoginUtil.isLoggedIn() ? LoginUtil.getUser().getUsername() : "Login");
			}

        });

        // Add other tools
        add(new PPanel());

        // Add search panel
        PPanel p = new PPanel();
        p.c.anchor = G.EAST;
        p.c.insets = new Insets(2, 0, 2, 0);
        p.c.strengthen();
        p.add(Box.createHorizontalStrut(1), p.c);
        p.c.gridx++;
        p.c.weaken();
        p.c.insets = new Insets(2, 0, 2, 30);
        p.add(shareButton, p.c);
        p.c.insets = new Insets(2, 0, 2, 0);
        p.c.gridx++;
        p.add(searchLabel, p.c);
        p.c.gridx++;
        p.c.insets = new Insets(2, 10, 2, 0);
        p.add(searchCombo, p.c);
        p.c.gridx++;
        p.c.insets = new Insets(2, -10, 2, 10);
        p.add(addButton, p.c);
        p.c.insets(2, 0, 2, 10);
        p.c.gridx++;
        p.add(advancedButton, p.c);
        p.c.gridx++;
        p.addc(loginButton);
        add(p);

	}

	private boolean hasJandorView() {
		return getJandorView() != null;
	}

	private JandorView getJandorView() {
		if(frame.getTabPane().getSelectedComponent() == null) {
			return null;
		}
		Component comp = ((JComponent) frame.getTabPane().getSelectedComponent()).getComponent(0);
		if(comp instanceof JandorView) {
			return (JandorView) comp;
		}
		if(comp instanceof PAccordion) {
			PAccordion accordion = (PAccordion) comp;
			for(PAccordionPanel p : accordion.getAccordionPanels()) {
				JComponent c = p.getAccordionData().getComponent();
				if(c instanceof JandorView) {
					return (JandorView) c;
				}
			}
		}
		return null;
	}

	private List<JandorMenuBar> getOtherMenuBars() {
		List<JandorMenuBar> bars = new LinkedList<JandorMenuBar>();
		for(JandorTabFrame frame : JandorTabFrame.getAllFrames()) {
			if(!frame.getJMenuBar().equals(this)) {
				bars.add((JandorMenuBar) frame.getJMenuBar());
			}
		}
		return bars;
	}

	private JFileChooser getFileChooser() {
		return FileUtil.getFileChooser();
	}

	public boolean actionNew() {
		if(hasJandorView()) {
			getJandorView().reset();
		}
		return true;
	}

	public boolean actionOpen() {
		File file = chooseFile(null, true);
		if(file == null) {
			return false;
		}

		String filename = file.getAbsolutePath();
		Deck deck = ApprenticeUtil.toDeck(filename);
		if(hasJandorView()) {
			getJandorView().setDeck(deck);
			updateOpenedFile(file, getJandorView());
		}
		return true;
	}

	public boolean actionImport() {
		File file = chooseFile(null, true);
		if(file == null) {
			return false;
		}

		String filename = file.getAbsolutePath();
		Deck deck = ApprenticeUtil.toDeck(filename);
		if(hasJandorView()) {
			getJandorView().setDeck(deck);
			updateOpenedFile(file, getJandorView());
		}
		Session.getInstance().importDeck(deck);
		return true;
	}

	public boolean actionClose() {
		System.exit(0);
		return true;
	}

	public String getOpenFileName() {
		return getJandorView().getOpenedFileName();
	}

	public void actionSave() {
		actionSave(getJandorView());
	}

	public void actionSave(JandorView view) {
		if(view.getOpenedFileName() != null) {
			actionSaveAs(new File(view.getOpenedFileName()), view);
			return;
		}
		actionSaveAs(view);
	}

	public void actionSaveAs() {
		actionSaveAs(getJandorView());
	}

	public void actionSaveAs(JandorView view) {
		actionSaveAs(chooseFile(view, false), view);
	}

	public void actionSaveAs(File file, JandorView view) {
		if(file == null || view == null || view.getDeck() == null) {
			return;
		}

		BufferedWriter writer = null;
        try {
        	writer = new BufferedWriter(new FileWriter(file));
        	ApprenticeUtil.toFile(writer, view.getDeck());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            	writer.close();
            } catch (Exception e) {}
        }
		updateOpenedFile(file, view);
	}

	public void actionSend() {
		actionSend(getJandorView());
	}

	public void actionSend(JandorView view) {
		//MailUtil.sendDeck("jandor.jmharter88@gmail.com", view.getDeck());
		//for(JandorMessage message : MailUtil.receive()) {
		//	MailUtil.saveMessage(message);
		//}
	}

	@Deprecated
	public void actionShareBoardOld(boolean selected) {
		if(!(getJandorView() instanceof BoardView)) {
			return;
		}

		if(!selected) {
			List<CardLayer> layers = new ArrayList<CardLayer>(((BoardView) getJandorView()).getCardLayer().getSyncedLayers());
			for(CardLayer layer : layers) {
				JUtil.getFrame(layer.getCanvas()).close();
			}
		} else {
			BoardView boardView = new BoardView("Jandor - Opponent Board View" + (getJandorView().hasOpenedFile() ? " - " + getJandorView().getSimpleOpenedFileName() : ""), false);
			boardView.getCardLayer().setHideHand(true);
			boardView.getCardLayer().syncLayer(((BoardView) getJandorView()).getCardLayer());
			((BoardView) getJandorView()).getCardLayer().synchronize();
			JUtil.popupWindow(boardView.getName(), boardView, true);
		}
	}

	public void actionShareBoard() {
		if(!(getJandorView() instanceof BoardView)) {
			JUtil.showMessageDialog(JUtil.getFrame(getJandorView()), "Cannot Share Screen", "Please select a Board Tab and try again.");
			return;
		}

		List<CardLayer> layers = new ArrayList<CardLayer>(((BoardView) getJandorView()).getCardLayer().getSyncedLayers());
		for(CardLayer layer : layers) {
			layer.handleClosed();
		}

		String deckName = ((BoardView) getJandorView()).getDeck().getName();
		String title = "Jandor - Opponent Board View (" + deckName + ")";

		BoardView boardView = new BoardView(title, false);
		boardView.getCardLayer().setHideHand(true);
		boardView.getCardLayer().syncLayer(((BoardView) getJandorView()).getCardLayer());
		((BoardView) getJandorView()).getCardLayer().synchronize();

		if(JandorTabFrame.hasShareFrame()) {
			JandorTabFrame shareFrame = JandorTabFrame.getShareFrame();
			shareFrame.setTitle(title);
			shareFrame.setContentPane(boardView);
			shareFrame.revalidate();
			shareFrame.repaint();
		} else {
			JandorTabFrame.setShareFrame(JUtil.popupWindow(boardView.getName(), boardView, true));
		}
	}

	public void actionSearchDeck() {
		if(!hasJandorView() || !(getJandorView() instanceof BoardView)) {
			return;
		}

		PPanel p = new PPanel();

		PButton top = new PButton("Top");
		top.setWidth(70);

		final PSpinner topSpinner = new PSpinner(1, 1, 9999) {

			@Override
			protected void handleChange(int value) {

			}

		};

		PButton bottom = new PButton("Bottom");
		bottom.setWidth(70);

		PButton all = new PButton("All");
		all.setWidth(70);

		p.fill();
		p.c.gridx++;
		p.c.gridx++;
		p.c.insets(0, 0, 10, 0);
		p.addc(Box.createHorizontalStrut(1));
		//p.addc(new PLabel("View"));
		p.c.gridx--;
		p.c.gridy++;
		p.c.insets(0, 0, 5, 0);
		p.addc(top);
		p.c.gridx++;
		p.c.insets(0, 5, 5, 0);
		p.addc(topSpinner);
		p.c.gridx++;
		p.c.insets(0, 0, 5, 0);
		p.addc(bottom);
		p.c.gridx = 2;
		p.c.gridwidth = 1;
		p.c.gridy++;
		p.c.insets(15, 0, 5, 0);
		p.addc(all);
		p.c.gridwidth = 1;
		p.c.gridx = 3;
		p.fill();

		final JDialog d = JUtil.buildBlankDialog(frame, "Inspect Deck - View", p);

		top.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int topCount = (int) topSpinner.getValue();
				BoardView boardView = (BoardView) getJandorView();
				String title = JUtil.getFrame(boardView).getTitle() + " - Deck";
				InspectView inspectView = new InspectView(title, boardView.getCardLayer(), ZoneType.DECK, topCount);
				JUtil.showDialog(boardView, title, inspectView);
				d.setVisible(false);
			}

		});

		bottom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int topCount = (int) topSpinner.getValue();
				BoardView boardView = (BoardView) getJandorView();
				String title = JUtil.getFrame(boardView).getTitle() + " - Deck";
				InspectView inspectView = new InspectView(title, boardView.getCardLayer(), ZoneType.DECK, topCount, true);
				JUtil.showDialog(boardView, title, inspectView);
				d.setVisible(false);
			}

		});

		all.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BoardView boardView = (BoardView) getJandorView();
				String title = JUtil.getFrame(boardView).getTitle() + " - Deck";
				InspectView inspectView = new InspectView(title, boardView.getCardLayer(), ZoneType.DECK);
				JUtil.showDialog(boardView, title, inspectView);
				d.setVisible(false);
			}

		});

		d.setVisible(true);
	}

	public void actionSearch(String title, List<Card> cards) {
		if(!hasJandorView() || !(getJandorView() instanceof BoardView)) {
			return;
		}
		BoardView boardView = (BoardView) getJandorView();
		String fullTitle = JUtil.getFrame(boardView).getTitle() + " - " + title;
		SimpleInspectView inspectView = new SimpleInspectView(fullTitle, cards);
		JUtil.showDialog(boardView, fullTitle, inspectView);
	}

	public void actionSearchGraveyard() {
		if(!hasJandorView() || !(getJandorView() instanceof BoardView)) {
			return;
		}
		BoardView boardView = (BoardView) getJandorView();
		String title = JUtil.getFrame(boardView).getTitle() + " - Graveyard";
		InspectView inspectView = new InspectView(title, boardView.getCardLayer(), ZoneType.GRAVEYARD);
		JUtil.showDialog(boardView, title, inspectView);
	}

	public void actionSearchExile() {
		if(!hasJandorView() || !(getJandorView() instanceof BoardView)) {
			return;
		}
		BoardView boardView = (BoardView) getJandorView();
		String title = JUtil.getFrame(boardView).getTitle() + " - Exile";
		InspectView inspectView = new InspectView(title, boardView.getCardLayer(), ZoneType.EXILE);
		JUtil.showDialog(boardView, title, inspectView);
	}

	public void actionViewFriends() {
		//ContactsDialog dialog = new ContactsDialog();
		if(DebugUtil.OFFLINE_MODE) {
			JUtil.showMessageDialog(null, "View Friends", "Cannot view users while offline. Please go online to view users.");
			return;
		}
		UsersDialog dialog = new UsersDialog();
		dialog.showDialog();
	}

	public static File chooseFile(Component parent, boolean open) {
		final JFileChooser fc = FileUtil.getFileChooser();
		int returnVal = open ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent); // XXX Add frame as parent
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(!open && !file.getAbsolutePath().endsWith(".dec")) {
				file = new File(file.getAbsoluteFile() + ".dec");
				fc.setSelectedFile(file);
			}
			return file;
		}
		return null;
	}

	public void setShareScreen(boolean shareScreen) {
		shareCheck.setSelected(shareScreen);
	}

	private void updateOpenedFile(File file, JandorView view) {
		if(view == null) {
			return;
		}

		view.setOpenedFileName(file.getAbsolutePath());
		if(view instanceof BoardView) {
			BoardView bView = (BoardView) getJandorView();
			CardLayer layer = bView.getCardLayer();
			for(CardLayer l : layer.getSyncedLayers()) {
				JUtil.getFrame(l.getCanvas()).setTitle("Jandor - Opponent Board View - " + bView.getSimpleOpenedFileName());
			}
		}
		view.clearModified();
		//JUtil.getFrame(getJandorView()).refreshTitle();
	}

}
