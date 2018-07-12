package contacts;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import session.Contact;
import session.Session;
import ui.AutoComboBox;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;

@Deprecated
public class ContactSearchPanel extends PPanel {

	protected AutoComboBox<Contact> contactCombo;
	
	public ContactSearchPanel() {
		init();
	}
	
	private void init() {
		JLabel toLabel = new JLabel("To:");
		
		contactCombo = new AutoComboBox<Contact>() {

			@Override
			public String buildTooltip(Contact selectedItem) {
				return null;
			}

			@Override
			public Collection<Contact> getSearchCollection(String searchString) {
				return Session.getInstance().getContacts(true);
			}

			@Override
			public String toString(Contact searchedObject) {
				return searchedObject.getNickname() + " " + searchedObject.getJandorEmail();
			}

			@Override
			public void handleFound(Contact found) {
				
			}
		
		};
		contactCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		contactCombo.setPreferredSize(new Dimension(400,20));
		contactCombo.getTextField().setText("");
		
		PPanel toPanel = new PPanel();
		toPanel.addc(contactCombo);
		toPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
	
		c.weaken();
		c.anchor = G.NORTHEAST;
		addc(toLabel);
		c.gridx++;
		c.insets(0,5);
		addc(toPanel);
	}
	
	public String getText() {
		return contactCombo.getTextField().getText();
	}

	public void setText(String text) {
		contactCombo.getTextField().setText(text);
	}
	
	public Contact getContact() {
		Contact contact = (Contact) contactCombo.getSelectedItem();
		if(contact == null) {
			String text = contactCombo.getTextField().getText();
			contact = Session.getInstance().getContactByEmail(text);
			if(contact == null) {
				contact = Session.getInstance().getContactByName(text);
			}
			if(contact != null) {
				if(JUtil.showConfirmDialog(this, "Verify Contact", new JLabel("Is \"" + contact + "\" the intended recipient?"))) {
					return contact;
				}
				return null;
			}
		}
		return contact;
	}
	
	public boolean isValidContact() {
		return getContact() != null;
	}
	
}
