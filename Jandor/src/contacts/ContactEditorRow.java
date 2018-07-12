package contacts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicButtonUI;

import session.Contact;
import session.Session;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import util.ImageUtil;

@Deprecated
public class ContactEditorRow extends PPanel {

	protected int contactId;
	protected ContactsDialog contactsDialog;
		
	protected JandorButton removeButton;
	protected JandorButton nicknameButton;
	protected JLabel emailLabel;
	protected JLabel notificationLabel;
	
	public ContactEditorRow(ContactsDialog contactsDialog, int contactId) {
		super();
		this.contactsDialog = contactsDialog;
		this.contactId = contactId;
		init();
	}
	
	protected void init() {
		final Contact contact = getContact();

		removeButton = JUtil.buildCloseButton();
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JUtil.showConfirmYesNoCancelDialog(contactsDialog, "Delete Contact \"" + contact.getNickname() + "\"", "Are you sure you want to permanently delete this contact?")) {
					remove();
				}
			}
			
		});
		
		nicknameButton = new JandorButton(contact.getNickname());
		nicknameButton.setShowTextAlways(true);
		nicknameButton.hide();
		nicknameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ContactDialog dialog = new ContactDialog(contactId);
				dialog.showDialog();
			}
			
		});
		
		nicknameButton.setToolTipText(buildTooltip());
		
		emailLabel = new JLabel(contact.getJandorEmail());
		notificationLabel = new JLabel("(" + contact.getNotificationEmail() + ")");
		
		c.anchor = G.CENTER;
		c.weaken();
		c.insets(0, 5, 0, 5);
        add(removeButton, c);
        c.insets(0, 0, 0, 5);
        c.gridx++;
        add(nicknameButton, c);
        c.gridx++;
        c.strengthen();
        addc(Box.createHorizontalStrut(1));
        c.weaken();
        c.gridx++;
        add(emailLabel, c);
        //c.gridx++;
        //add(notificationLabel, c);
        
	}

	public Contact getContact() {
		return Session.getInstance().getContact(contactId);
	}
	
	private String buildTooltip() {
		Contact contact = getContact();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>Nickname:</b> " + contact.getNickname());
		sb.append("<hr>");
		sb.append("<b>Jandor Email:</b> " + contact.getJandorEmail());
		sb.append("<br>");
		sb.append("<b>Notification Email:</b> " + contact.getNotificationEmail());
		sb.append("</html>");
		return sb.toString();
	}
	
	public JandorButton getRemoveButton() {
		return removeButton;
	}
	
	public void remove() {
		getContact().delete();
	}
	
	public GlassPane buildGlassPane() {
		GlassPane gp = new GlassPane(this) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				removeButton.show();
				nicknameButton.show();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeButton.hide();
				nicknameButton.hide();
				super.mouseExited(e);
			}
				
		};
		return gp;
	}
}
