package contacts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;

import session.Contact;
import session.Session;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import ui.pwidget.PTextField;
import util.IDUtil;

@Deprecated
public class ContactDialog extends PPanel {

	protected PTextField nicknameText;
	protected PTextField emailText;
	protected PTextField notificationText;
	protected boolean isModify = false;
	protected int contactId;
	
	public ContactDialog() {
		this(IDUtil.NONE);
	}
	
	public ContactDialog(int contactId) {
		super();
		this.contactId = contactId;
		init();
	}
	
	private void init() {
		nicknameText = new PTextField();
		emailText = new PTextField();
		notificationText = new PTextField();

		Contact contact = Session.getInstance().getContact(contactId);
		if(contact != null) {
			isModify = true;
			nicknameText.setText(contact.getNickname());
			emailText.setText(contact.getJandorEmail());
			notificationText.setText(contact.getNotificationEmail());
		}
		
		Dimension dim = new Dimension(200, 20);
		nicknameText.setPreferredSize(dim);
		emailText.setPreferredSize(dim);
		notificationText.setPreferredSize(dim);
		
		c.weaken();
		c.anchor = G.EAST;

		c.insets(0,5);
		addc(new JLabel("Nickname"));
		c.gridx++;
		addc(nicknameText);
		c.gridx++;
		addc(info("Nickname", "The name Jandor will use for this contact."));
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
		addc(new JLabel("<html><u>Jandor</u> Gmail</html>"));
		c.gridx++;
		addc(emailText);
		c.gridx++;
		addc(info("Junk Email", "This contact's junk Gmail account specifically created for receiving Jandor decks."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx=0;
		c.gridy++;
		
		c.insets(5);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		
		c.gridx = 0;
		c.gridy++;
		c.insets(20);
		addc(Box.createHorizontalStrut(1));
		c.gridy++;
		
		c.insets(0,5);
		addc(new JLabel("Notification Email"));
		c.gridx++;
		addc(notificationText);
		c.gridx++;
		addc(info("Notification Email", "(Optional) An email address where a notification email will be sent whenever you send a deck to this contact. " +
				"Unlike the above address, this address does not need to be a junk gmail address and can be any personal email address. Make sure to check " +
				"with this contact first as to whether it is ok to send notifications to this address."));
		c.gridx++;
		c.strengthen();
		addc(Box.createHorizontalStrut(1));
		c.weaken();
		c.gridx = 0;
		c.gridy++;
		
		c.insets(0,5);
		addc(new JLabel("(Optional)"));
		
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
		String notificationEmail = notificationText.getText().trim();
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
		
		Contact contact;
		if(contactId == IDUtil.NONE) {
			contact = new Contact();
			contact.newId();
		} else if(!Session.getInstance().hasContact(contactId)) {
			contact = new Contact(contactId);
		} else {
			contact = Session.getInstance().getContact(contactId);
		}
		
		contact.setNickname(nickname);
		contact.setJandorEmail(email);
		contact.setNotificationEmail(notificationEmail);
		contact.setDefaulUser(false);
		contact.save();
		
		return contact;
	}
	
	private boolean isEmailValid() {
		String email = emailText.getText().trim();
		if(email.isEmpty() || !email.contains("@gmail.com")) {
			return false;
		}
		return true;
	}
	
	private boolean isNotificationEmailValid() {
		String email = notificationText.getText().trim();
		if(email.isEmpty()) {
			return true;
		}
		if(!email.contains("@")) {
			return false;
		}
		return true;
	}
	
	private boolean isEmailUnique() {
		String email = emailText.getText().trim();
		for(Contact contact : Session.getInstance().getContacts()) {
			if(contact.getId() != Contact.USER_ID && contact.getJandorEmail().equals(email) && contact.getId() != contactId) {
				return false;
			}
		}
		return true;
	}
	
	public Contact showDialog() {
		if(JUtil.showConfirmDialog(null, isModify ? "Change Contact" : "Add Contact", this)) {
			if(!isEmailValid()) {
				JUtil.showWarningDialog(null, "Invalid Jandor Email", buildLabel("The Jandor email address given is invalid. Make sure it is a valid gmail address and try again."));
				return showDialog();
			}
			if(!isNotificationEmailValid()) {
				JUtil.showWarningDialog(null, "Invalid Notification Email", buildLabel("The notification email address given is invalid. Make sure it is a valid email address and try again."));
				return showDialog();
			}
			if(!isEmailUnique()) {
				JUtil.showWarningDialog(null, "Contact Already Exists", buildLabel("There is already a contact with the same Jandor email address. Please enter a new gmail address and try again."));
				return showDialog();
			}
			return buildContact();
		}
		return Session.getInstance().getContact();
	}
	
}
