package contacts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;

import session.Contact;
import session.Session;
import ui.GlassPane;
import ui.pwidget.CloseListener;
import ui.pwidget.ColorUtil;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;

@Deprecated
public class ContactsDialog extends PPanel implements CloseListener {

	protected PPanel contactPanel;
	
	public ContactsDialog() {
		super();
		init();
		SessionEventManager.addListener(Contact.class, SessionEvent.TYPE_ANY, new SessionEventListener(this) {

			@Override
			public void handleEvent(SessionEvent event) {
				rebuildContactRows();
			}
			
		});
	}
	
	private void init() {
		rebuildContactRows();
		addc(contactPanel);
	}
	
	public void rebuildContactRows() {
		if(contactPanel == null) {
			contactPanel = new PPanel();
		} else {
			contactPanel.removeAll();
			contactPanel.c.reset();
		}
		contactPanel.c.anchor = G.WEST;
		contactPanel.c.strengthen();
		
		PPanel widthPanel = new PPanel();
		widthPanel.setPreferredSize(new Dimension(300, 30));
		JLabel deckLabel = new JLabel("Contacts");
		deckLabel.setFont(deckLabel.getFont().deriveFont(15f));
		widthPanel.c.insets(0,0,10,0);
		widthPanel.addc(deckLabel);
		
		contactPanel.addc(widthPanel);
		contactPanel.c.gridy++;

		List<Contact> contacts = Session.getInstance().getContacts();
		Collections.sort(contacts, new Comparator<Contact>() {

			@Override
			public int compare(Contact contactA, Contact contactB) {
				int comp = contactA.getNickname().compareTo(contactB.getNickname());
				if(comp == 0) {
					return contactA.getJandorEmail().compareTo(contactB.getJandorEmail());
				}
				return comp;
			}
			
		});
		
		contactPanel.c.strengthen();
		int i = 0;
		for(Contact contact : contacts) {
			if(contact.getId() == Contact.USER_ID) {
				continue;
			}
			
			ContactEditorRow row = new ContactEditorRow(this, contact.getId());
			contactPanel.addc(row);
			GlassPane gp = row.buildGlassPane();
			contactPanel.addc(gp);
			contactPanel.setComponentZOrder(gp, 1);
			contactPanel.c.gridx++;
			contactPanel.c.weaken();
			//contactPanel.addc(row.getTagPanel());
			contactPanel.c.strengthen();
			contactPanel.c.gridx++;
			contactPanel.addc(Box.createHorizontalStrut(1));
			contactPanel.c.gridx-=2;
			contactPanel.c.gridy++;
			row.setOpaque(true);
			row.setBackground(i++ % 2 == 0 ? ColorUtil.DARK_GRAY_2 : ColorUtil.DARK_GRAY_3);
		}

		contactPanel.revalidate();
		revalidate();
		
		repaint();
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
	
	public void showDialog() {
		JUtil.showDialog(null, "Contacts", this);
	}

	@Override
	public void handleClosed() {
		SessionEventManager.removeListeners(this);
	}
	
}
