package contacts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JLabel;

import session.Contact;
import session.Session;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PPasswordField;
import ui.pwidget.PTextField;
import util.MailUtil;

@Deprecated
public class LoginContactDialog extends PPanel {

	protected PTextField nicknameText;
	protected PTextField emailText;
	protected PPasswordField passwordText;
	protected PTextField notificationText;
	protected boolean isModify = false;
	
	public LoginContactDialog() {
		super();
		init();
	}
	
	private void init() {
		nicknameText = new PTextField();
		emailText = new PTextField();
		passwordText = new PPasswordField();
		notificationText = new PTextField();
		final PCheckBox showPasswordCheck = new PCheckBox("Show Password");
		showPasswordCheck.setFont(new JLabel().getFont());
		showPasswordCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				passwordText.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
			}
			
		});
		
		Contact user = Session.getInstance().getContact();
		if(user != null) {
			isModify = true;
			nicknameText.setText(user.getNickname());
			emailText.setText(user.getJandorEmail());
			passwordText.setText(user.getJandorEmailPassword());
			notificationText.setText(user.getNotificationEmail());
		}
		
		Dimension dim = new Dimension(200, 20);
		nicknameText.setPreferredSize(dim);
		emailText.setPreferredSize(dim);
		passwordText.setPreferredSize(dim);
		notificationText.setPreferredSize(dim);
		
		c.weaken();
		c.anchor = G.EAST;

		c.insets(0,5);
		addc(new JLabel("Nickname"));
		c.gridx++;
		addc(nicknameText);
		c.gridx++;
		addc(info("Nickname", "The name Jandor will use to refer to you."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(20);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		
		c.insets(0,5);
		addc(new JLabel("<html><u>Junk</u> Gmail</html>"));
		c.gridx++;
		addc(emailText);
		c.gridx++;
		addc(info("Junk Email", "The address of a junk Gmail account specifically created for Jandor. " + 
				"You will not need to ever login to this account or check it through Gmail, but Jandor will use " +
				"it to send decks to you from other users. This is the email address you should tell other users " +
				"to use when sending decks to you."));
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
		addc(info("Jandor Email", "The password to your junk Gmail account. Make sure the password is also a junk password and not one used " + 
								"for any of your other important accounts."));
		c.gridx = 1;
		c.gridy++;
		addc(showPasswordCheck);
		
		c.gridx = 0;
		c.gridy++;
		
		c.insets(20);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		
		/*c.insets(0,5);
		addc(new JLabel("Notification Email"));
		c.gridx++;
		addc(notificationText);
		c.gridx++;
		addc(info("Notification Email", "(Optional) An email address where notifications will be sent whenever a new deck is sent to you. " +
				"Unlike the above address, this address does not need to be a junk gmail address and can be any personal email address used " + 
				"for other things besides Jandor. When someone sends you a deck, a short notification email will be sent to this address " +
				"telling you to check Jandor. Leaving this field blank will ensure no notifications will be sent to you."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx = 0;
		c.gridy++;
		
		c.insets(0,5);
		addc(new JLabel("(Optional)"));*/
		
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
	
	private Contact buildContact() {
		String nickname = nicknameText.getText().trim();
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		if(nickname == null || nickname.isEmpty()) {
			nickname = email;
		}
		
		if(nickname.isEmpty()) {
			Contact u = Session.getInstance().getContact();
			if(u != null) {
				u.delete();
			}
			return null;
		}
		
		Contact user = new Contact(Contact.USER_ID);
		user.setNickname(nickname);
		user.setJandorEmail(email);
		user.setJandorEmailPassword(password);
		user.setDefaulUser(true);
		user.save();
		
		return user;
	}
	
	private boolean isContactValid() {
		String email = emailText.getText().trim();
		String password = String.valueOf(passwordText.getPassword()).trim();
		if(email.isEmpty() && password.isEmpty()) {
			return true;
		}
		return MailUtil.isValid(email, password);
	}
	
	public Contact showDialog() {
		if(JUtil.showConfirmDialog(null, isModify ? "Change User Info" : "Register User", this)) {
			if(!isContactValid()) {
				JUtil.showWarningDialog(null, "Invalid Email Credentials", buildLabel("The email address and password combination you entered are incorrect. Either enter a new address and password combination or leave them both blank."));
				return showDialog();
			}
			return buildContact();
		}
		return Session.getInstance().getContact();
	}
	
}
