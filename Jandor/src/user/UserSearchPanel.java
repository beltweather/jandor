package user;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import session.User;
import ui.AutoComboBox;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import util.UserUtil;

public class UserSearchPanel extends PPanel {

	protected AutoComboBox<User> userCombo;
	
	public UserSearchPanel() {
		init();
	}
	
	private void init() {
		JLabel toLabel = new JLabel("To:");
		
		userCombo = new AutoComboBox<User>() {

			@Override
			public String buildTooltip(User selectedItem) {
				return null;
			}

			@Override
			public Collection<User> getSearchCollection(String searchString) {
				return UserUtil.getUsers();
			}

			@Override
			public String toString(User searchedObject) {
				return searchedObject.getUsername() + " " + searchedObject.getEmail();
			}

			@Override
			public void handleFound(User found) {
				
			}
		
		};
		userCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		userCombo.setPreferredSize(new Dimension(400,20));
		userCombo.getTextField().setText("");
		
		PPanel toPanel = new PPanel();
		toPanel.addc(userCombo);
		toPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
	
		c.weaken();
		c.anchor = G.NORTHEAST;
		addc(toLabel);
		c.gridx++;
		c.insets(0,5);
		addc(toPanel);
	}
	
	public String getText() {
		return userCombo.getTextField().getText();
	}

	public void setText(String text) {
		userCombo.getTextField().setText(text);
	}
	
	public User getUser() {
		User user = (User) userCombo.getSelectedItem();
		if(user == null) {
			String text = userCombo.getTextField().getText();
			user = UserUtil.getUserByUsername(text);
			//if(user == null) {
				//user = UserUtil.getUserByName(text);
			//}
			if(user != null) {
				if(JUtil.showConfirmDialog(this, "Verify User", new JLabel("Is \"" + user + "\" the intended recipient?"))) {
					return user;
				}
				return null;
			}
		}
		return user;
	}
	
	public boolean isValidUser() {
		return getUser() != null;
	}
	
}
