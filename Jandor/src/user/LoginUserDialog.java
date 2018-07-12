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
import ui.pwidget.PLinkButton;
import ui.pwidget.PPanel;
import ui.pwidget.PPasswordField;
import ui.pwidget.PTextField;
import util.DebugUtil;
import util.LoginUtil;
import util.MailUtil;
import util.DriveUtil;

public class LoginUserDialog extends PPanel {

	protected PTextField usernameText;
	protected PPasswordField passwordText;
	protected PButton registerButton;
	protected PButton forgotPasswordButton;
	
	public LoginUserDialog() {
		super();
		init();
	}
	
	private void init() {
		usernameText = new PTextField();
		passwordText = new PPasswordField();
		final PCheckBox showPasswordCheck = new PCheckBox("Show Password");
		showPasswordCheck.setFont(new JLabel().getFont());
		showPasswordCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				passwordText.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
			}
			
		});
		registerButton = new PLinkButton("Register");
		forgotPasswordButton = new PLinkButton("Forgot Password");
		
		registerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CreateUserDialog dialog = new CreateUserDialog();
				User user = dialog.showDialog();
				if(user != null) {
					usernameText.setText(user.getUsername());
					passwordText.setText(user.getPassword());
				}
			}
			
		});
		
		forgotPasswordButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				String username = usernameText.getText().trim();
				if(username.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter a username to retrieve password.", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				UserSheet data = new UserSheet(); 
				if(!data.exists("Username", username)) {
					JOptionPane.showMessageDialog(null, "There is no user named \"" + username + "\" in our system.", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				String email = data.getValue("Username", username, "Email");
				if(email.isEmpty()) {
					JOptionPane.showMessageDialog(null, "There is no email address listed for user \"" + username + "\" in our system.", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				// Send email
				if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Send your password to the email address \"" + email + "\"?", "Forgot Password", JOptionPane.YES_NO_CANCEL_OPTION)) {
					String password = data.getValue("Username", username, "Password");
					MailUtil.send(email, "[Jandor] Forgot Password", "Your password is \"" + password + "\".");
					JOptionPane.showMessageDialog(null, "Your password has been sent to your email address: \"" + email + "\".", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
				}

			}
			
		});
		
		User user = Session.getInstance().getUser();	
		if(user != null) {
			usernameText.setText(user.getUsername());
			passwordText.setText(user.getPassword());
		}
		
		Dimension dim = new Dimension(200, 20);
		usernameText.setPreferredSize(dim);
		passwordText.setPreferredSize(dim);
		
		c.weaken();
		c.anchor = G.EAST;

		c.insets(0,5);
		addc(new JLabel("Username"));
		c.gridx++;
		addc(usernameText);
		c.gridx++;
		addc(info("Username", "The name Jandor will use to refer to you."));
		c.gridx++;
		addc(registerButton);
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
		c.gridx++;
		addc(forgotPasswordButton);
		c.gridx = 1;
		c.gridy++;
		addc(showPasswordCheck);
		c.gridx = 0;
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
	
	private void warnBadLogin() {
		JOptionPane.showMessageDialog(null, "The current username and password do not match. Try again.", "Could Not Update User", JOptionPane.WARNING_MESSAGE);
	}
	
	private User buildUser() {
		if(DebugUtil.OFFLINE_MODE) {
			return null;
		}
		
		String username = usernameText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		
		// Verify user
		UserSheet data = new UserSheet();
		
		if(!data.exists("Username", username)) {
			warnBadLogin();
			return null;
		}
			
		String realPassword = data.getValue("Username", username, "Password");
		if(!password.equals(realPassword)) {
			warnBadLogin();
			return null;
		}
		
		String guid = data.getValue("Username", username, "GUID");
		String firstName = data.getValue("Username", username, "First Name");
		String lastName = data.getValue("Username", username, "Last Name");
		String email = data.getValue("Username", username, "Email");
		
		User user = new User(User.USER_ID);
		user.setGUID(guid);
		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setPassword(password);
		user.setDefaultUser(true);
		user.save();
		
		return user;
	}
	
	private boolean isUserValid() {
		String username = usernameText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		return !username.isEmpty() && !password.isEmpty(); 
	}
	
	public User showDialog() {
		if(JUtil.showConfirmDialog(null, "Login", this)) {
			if(!isUserValid()) {
				JUtil.showWarningDialog(null, "Invalid Login", buildLabel("You must enter a username and password to login."));
				return showDialog();
			}
			User user = buildUser();
			if(user == null) {
				return showDialog();
			}
			LoginUtil.login(user);
			return LoginUtil.getUser();
		}
		return LoginUtil.getUser();
	}
	
}
