package user;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import session.Session;
import session.User;
import sheets.domain.UserSheet;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PLinkButton;
import ui.pwidget.PPanel;
import ui.pwidget.PPasswordField;
import ui.pwidget.PTextField;
import util.DebugUtil;
import util.IDUtil;
import util.LoginUtil;

public class EditUserDialog extends PPanel {

	protected PTextField guidText;
	protected PTextField usernameText;
	protected PPasswordField passwordText;
	protected PPasswordField newPasswordText;
	protected PPasswordField newPasswordAgainText;
	protected PTextField firstNameText;
	protected PTextField lastNameText;
	protected PTextField emailText;
	
	public EditUserDialog() {
		super();
		init();
	}
	
	private void init() {
		guidText = new PTextField();
		usernameText = new PTextField();
		firstNameText = new PTextField();
		lastNameText = new PTextField();
		emailText = new PTextField();
		passwordText = new PPasswordField();
		newPasswordText = new PPasswordField();
		newPasswordAgainText = new PPasswordField();
		
		PButton logoutButton = new PLinkButton("Logout");
		logoutButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Window w = SwingUtilities.getWindowAncestor(EditUserDialog.this);
				w.setVisible(false);
				LoginUtil.logout();
				LoginUserDialog dialog = new LoginUserDialog();
				dialog.showDialog();
			}
			
		});
		
		final PCheckBox showPasswordCheck = new PCheckBox("Show Password");
		showPasswordCheck.setFont(new JLabel().getFont());
		showPasswordCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				passwordText.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
			}
			
		});
		
		User user = Session.getInstance().getUser();
		if(user != null) {
			guidText.setText(user.getGUID());
			usernameText.setText(user.getUsername());
			firstNameText.setText(user.getFirstName());
			lastNameText.setText(user.getLastName());
			emailText.setText(user.getEmail());
			passwordText.setText(user.getPassword());
		} else {
			guidText.setText(IDUtil.generateGUID(IDUtil.PREFIX_USER));
		}
		
		Dimension dim = new Dimension(200, 20);
		guidText.setPreferredSize(dim);
		usernameText.setPreferredSize(dim);
		firstNameText.setPreferredSize(dim);
		lastNameText.setPreferredSize(dim);
		emailText.setPreferredSize(dim);
		passwordText.setPreferredSize(dim);
		newPasswordText.setPreferredSize(dim);
		newPasswordAgainText.setPreferredSize(dim);
		
		c.weaken();
		c.anchor = G.EAST;

		c.insets(0,5);
		addc(new JLabel("Username"));
		c.gridx++;
		addc(usernameText);
		c.gridx++;
		addc(info("Username", "The name Jandor will use to refer to you."));
		c.gridx++;
		addc(logoutButton);
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(5);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		c.insets(0,5);
		addc(new JLabel("Password"));
		c.gridx++;
		addc(passwordText);
		c.gridx++;
		addc(info("Password", "The password to your Jandor account so no other user can change your credentials."));
		c.gridx = 0;
		c.gridy++;
		
		c.insets(5);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		c.insets(0,5);
		addc(new JLabel("New Password"));
		c.gridx++;
		addc(newPasswordText);
		c.gridx++;
		addc(info("New Password", "A new password for your Jandor account."));
		c.gridx = 0;
		c.gridy++;
		
		c.insets(5);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		c.insets(0,5);
		addc(new JLabel("New Password (again)"));
		c.gridx++;
		addc(newPasswordAgainText);
		c.gridx++;
		addc(info("New Password (again)", "A new password for you Jandor account that must match the new password above."));
		c.gridx = 1;
		c.gridy++;
		addc(showPasswordCheck);
		c.gridx = 0;
		c.gridy++;
		
		c.insets(10,5);
		addc(new JLabel("First Name"));
		c.gridx++;
		addc(firstNameText);
		c.gridx++;
		addc(info("Last Name", "Your first name."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(5,5);
		addc(new JLabel("Last Name"));
		c.gridx++;
		addc(lastNameText);
		c.gridx++;
		addc(info("First Name", "Your last name."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(5,5);
		addc(new JLabel("Email"));
		c.gridx++;
		addc(emailText);
		c.gridx++;
		addc(info("Email", "An email address where notifications will be sent whenever a new deck is sent to you. " +
				"When someone sends you a deck, a short notification email will be sent to this address " +
				"telling you to check Jandor. Leaving this field blank will ensure no notifications will be sent to you."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(20);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		
	}
	
	private JLabel buildLabel(String text) {
		return new JLabel("<html><div width=\"200px\">" + text + "</div></html>");
	}
	
	private JLabel info(final String title, final String info) {
		PButton button = new PButton("?");
		button.setPreferredSize(new Dimension(20, 20));

		JLabel label = new JLabel("?");
		label.setPreferredSize(new Dimension(20, 20));
		label.setToolTipText("<html><div width=\"200px\">" + info + "</div></html>");
		
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JUtil.showMessageDialog(null, title, "<html><div width=\"400px\">" + info + "</div></html>");
			}
			
		});
		
		return label;
	}
	
	private User buildUser() {
		UserSheet data = new UserSheet();
		String guid = guidText.getText().trim();
		String username = usernameText.getText().trim();
		String firstName = firstNameText.getText().trim();
		String lastName = lastNameText.getText().trim();
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		if(username == null || username.isEmpty()) {
			username = email;
		}
		
		if(username.isEmpty()) {
			User u = Session.getInstance().getUser();
			if(u != null) {
				u.delete();
			}
			return null;
		}
		
		// Verify user
		if(!DebugUtil.OFFLINE_MODE) {
	 		if(data.exists("GUID", guid)) {
				String sheetPassword = data.getValue("GUID", guid, "Password");
				if(!password.equals(sheetPassword)) {
					JOptionPane.showMessageDialog(null, "The current password is incorrect for your user.", "Could Not Update User", JOptionPane.WARNING_MESSAGE);
					return null;
				}
			}
		}
		
		User user = new User(User.USER_ID);
		user.setGUID(guid);
		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setPassword(password);
		user.setDefaultUser(true);
		user.save();
		
		// Save to database online
		if(!DebugUtil.OFFLINE_MODE) {
			if(data.exists("GUID", guid)) {
				int rowIndex = data.getRowIndex("GUID", guid);
				data.setRow(rowIndex, guid, username, password, firstName, lastName, email);
			} else {
				data.appendRow(guid, username, password, firstName, lastName, email);
			}
			data.write();
		}
		
		return user;
	}
	
	private boolean isUserValid() {
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		if(email.isEmpty() && password.isEmpty()) {
			return true;
		} else if(email.isEmpty() || password.isEmpty()) {
			return false;
		}
		return true; // XXX Check for user existence in spreadsheet
	}
	
	public User showDialog() {
		if(JUtil.showConfirmDialog(null, "Edit User Info", this)) {
			if(!isUserValid()) {
				JUtil.showWarningDialog(null, "Invalid Email Credentials", buildLabel("The email address and password combination you entered are incorrect. Either enter a new address and password combination or leave them both blank."));
				return showDialog();
			}
			return buildUser();
		}
		return Session.getInstance().getUser();
	}
	
}
