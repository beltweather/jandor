package user;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;

import session.Contact;
import session.User;
import ui.GlassPane;
import ui.pwidget.CloseListener;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import util.UserUtil;
import util.event.SessionEventManager;
import contacts.ContactEditorRow;

public class UsersDialog extends PPanel implements CloseListener {

	protected PPanel userPanel;
	
	public UsersDialog() {
		super();
		init();
	}
	
	private void init() {
		rebuildUserRows();
		addc(userPanel);
	}
	
	public void rebuildUserRows() {
		if(userPanel == null) {
			userPanel = new PPanel();
		} else {
			userPanel.removeAll();
			userPanel.c.reset();
		}
		userPanel.c.anchor = G.WEST;
		userPanel.c.strengthen();
		
		PPanel widthPanel = new PPanel();
		widthPanel.setPreferredSize(new Dimension(300, 30));
		JLabel deckLabel = new JLabel("Friends");
		deckLabel.setFont(deckLabel.getFont().deriveFont(15f));
		widthPanel.c.insets(0,0,10,0);
		widthPanel.addc(deckLabel);
		
		userPanel.addc(widthPanel);
		userPanel.c.gridy++;

		List<User> users = UserUtil.getUsers();
		Collections.sort(users, new Comparator<User>() {

			@Override
			public int compare(User userA, User userB) {
				int comp = userA.getUsername().compareTo(userB.getUsername());
				if(comp == 0) {
					return userA.getEmail().compareTo(userB.getEmail());
				}
				return comp;
			}
			
		});
		
		userPanel.c.strengthen();
		int i = 0;
		for(User user : users) {
			if(user.isLoggedIn()) {
				continue;
			}
			
			UserEditorRow row = new UserEditorRow(user);
			userPanel.addc(row);
			GlassPane gp = row.buildGlassPane();
			userPanel.addc(gp);
			userPanel.setComponentZOrder(gp, 1);
			userPanel.c.gridx++;
			userPanel.c.weaken();
			//contactPanel.addc(row.getTagPanel());
			userPanel.c.strengthen();
			userPanel.c.gridx++;
			userPanel.addc(Box.createHorizontalStrut(1));
			userPanel.c.gridx-=2;
			userPanel.c.gridy++;
			row.setOpaque(true);
			row.setBackground(i++ % 2 == 0 ? ColorUtil.DARK_GRAY_2 : ColorUtil.DARK_GRAY_3);
		}

		userPanel.revalidate();
		revalidate();
		
		repaint();
	}
	
	public void showDialog() {
		JUtil.showDialog(null, "View Friends", this);
	}

	@Override
	public void handleClosed() {
		SessionEventManager.removeListeners(this);
	}
	
}
