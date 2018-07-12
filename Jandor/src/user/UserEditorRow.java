package user;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JLabel;

import session.User;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.G;
import ui.pwidget.PPanel;
import util.FriendUtil;

public class UserEditorRow extends PPanel {

	protected User user;
		
	protected JandorButton connectButton;
	protected JandorButton nicknameButton;
	protected JLabel detailsLabel;
	protected JLabel usernameLabel;
	
	public UserEditorRow(User user) {
		super();
		this.user = user;
		init();
	}
	
	protected void init() {
		connectButton = new JandorButton("Connect");
		connectButton.hide();
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FriendUtil.inviteFriend(user);
			}
			
		});
		connectButton.setToolTipText("Share your active board with this friend and view their's.");
		
		usernameLabel = new JLabel(user.getUsername());
		detailsLabel = new JLabel(user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
		
		c.anchor = G.CENTER;
		c.weaken();
		c.insets(0, 5, 0, 5);
        add(connectButton, c);
        c.insets(0, 0, 0, 5);
        c.gridx++;
        add(usernameLabel, c);
        c.gridx++;
        c.strengthen();
        addc(Box.createHorizontalStrut(1));
        c.gridx++;
        c.weaken();
        add(detailsLabel, c);
	}

	public User getUser() {
		return user;
	}
	
	public JandorButton getRemoveButton() {
		return connectButton;
	}
	
	public GlassPane buildGlassPane() {
		GlassPane gp = new GlassPane(this) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				connectButton.show();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				connectButton.hide();
				super.mouseExited(e);
			}
				
		};
		return gp;
	}
}
