package user;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import session.Session;
import session.User;
import sheets.domain.UserSheet;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PPasswordField;
import ui.pwidget.PTextField;
import util.DebugUtil;
import util.IDUtil;

public class CreateUserDialog extends PPanel {

	protected PTextField guidText;
	protected PTextField usernameText;
	protected PPasswordField passwordText;
	protected PPasswordField passwordAgainText;
	protected PTextField firstNameText;
	protected PTextField lastNameText;
	protected PTextField emailText;
	
	public CreateUserDialog() {
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
		final PCheckBox showPasswordCheck = new PCheckBox("Show Password");
		showPasswordCheck.setFont(new JLabel().getFont());
		showPasswordCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				passwordText.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
				passwordAgainText.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
			}
			
		});
		passwordAgainText = new PPasswordField();
		
		guidText.setText(IDUtil.generateGUID(IDUtil.PREFIX_USER));
		
		Dimension dim = new Dimension(200, 20);
		guidText.setPreferredSize(dim);
		usernameText.setPreferredSize(dim);
		firstNameText.setPreferredSize(dim);
		lastNameText.setPreferredSize(dim);
		emailText.setPreferredSize(dim);
		passwordText.setPreferredSize(dim);
		passwordAgainText.setPreferredSize(dim);
		
		c.weaken();
		c.anchor = G.EAST;

		c.insets(0,5);
		addc(new JLabel("Username"));
		c.gridx++;
		addc(usernameText);
		c.gridx++;
		addc(info("Username", "The name Jandor will use to refer to you."));
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
		addc(new JLabel("Password (again)"));
		c.gridx++;
		addc(passwordAgainText);
		c.gridx++;
		addc(info("Password (again)", "The password to your Jandor account entered a second time to avoid typos."));

		c.gridx = 1;
		c.gridy++;
		addc(showPasswordCheck);
		c.gridx = 0;
		c.gridy++;
		
		c.insets(5,5);
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
		String guid = guidText.getText().trim();
		String username = usernameText.getText().trim();
		String firstName = firstNameText.getText().trim();
		String lastName = lastNameText.getText().trim();
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		
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
		UserSheet data = new UserSheet();
		data.createUser(user);
		
		return user;
	}
	
	private boolean isUserValid() {
		String guid = guidText.getText().trim();
		String username = usernameText.getText().trim();
		String firstName = firstNameText.getText().trim();
		String lastName = lastNameText.getText().trim();
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		String passwordAgain = String.valueOf(passwordAgainText.getPassword()).trim();
		
		if(username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(null, "One or more required fields are empty.", "Could Not Register User", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		if(!password.equals(passwordAgain)) {
			JOptionPane.showMessageDialog(null, "The two password fields do not match.", "Could Not Register User", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		UserSheet data = new UserSheet();
		if(data.exists("Username", username)) {
			JOptionPane.showMessageDialog(null, "A user with the username \"" + username + "\" already exists.", "Could Not Register User", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public User showDialog() {
		if(DebugUtil.OFFLINE_MODE) {
			return null;
		}
		if(JUtil.showConfirmDialog(null, "Register User", this)) {
			if(!isUserValid()) {
				return showDialog();
			}
			return buildUser();
		}
		return Session.getInstance().getUser();
	}
	
}
