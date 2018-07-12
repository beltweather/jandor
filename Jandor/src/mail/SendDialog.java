package mail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import session.DeckHeader;
import session.Session;
import session.User;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PCheckBox;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import user.UserSearchPanel;
import util.LoginUtil;
import util.MailUtil;

public class SendDialog extends PPanel {

	protected int deckId;
	protected JTextArea noteText;
	protected UserSearchPanel userCombo;
	protected String message;
	protected String to;
	protected PCheckBox notifyCheck;
	
	public SendDialog(int deckId) {
		this(deckId, "", "");
	}
	
	public SendDialog(int deckId, String to, String message) {
		super();
		this.deckId = deckId;
		this.to = to;
		this.message = message;
		init();
	}
	
	private void init() {
		DeckHeader header = getDeckHeader();
		
		JLabel toLabel = new JLabel("To:");
		
		userCombo = new UserSearchPanel();
		userCombo.setText(to);
		
		noteText = new JTextArea();
		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);
		noteText.setMargin(new Insets(5,5,5,5));
		noteText.setText(message);
	
		final PScrollPane areaScrollPane = new PScrollPane(noteText);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(400, 100));
		areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		
		final JLabel messageLabel = new JLabel("Message:"); 
		
		notifyCheck = new PCheckBox("Send Notification Email");
		notifyCheck.setSelected(true);
		notifyCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				
			}
			
		});
		
		c.weaken();
		c.anchor = G.NORTHEAST;
		c.gridwidth = 2;
		addc(userCombo);
		c.gridwidth = 1;
		c.gridy++;
		c.insets(20);
		addcStrut();
		c.insets();
		c.gridy++;
		addc(messageLabel);
		c.gridx++;
		c.insets(0,5);
		addc(areaScrollPane);
		c.gridy++;
		addc(notifyCheck);
		c.gridy++;
		c.strengthen();
		addcStrut();
	}
	
	public DeckHeader getDeckHeader() {
		return Session.getInstance().getDeckHeader(deckId);
	}
	
	private JLabel buildLabel(String text) {
		return new JLabel("<html><div width=\"200px\">" + text + "</div></html>");
	}
	
	public void showDialog() {
		if(!LoginUtil.isLoggedIn()) {
			JUtil.showWarningDialog(null, "No User Logged In", buildLabel("Please login in the top right of the Jandor window to send decks to other users."));
			return;
		}
		
		DeckHeader header = getDeckHeader();
		if(JUtil.showConfirmDialog(null, "Send Deck \"" + header.getName() + "\"", this)) {
			to = userCombo.getText();
			message = noteText.getText();
			
			User user = userCombo.getUser();
			if(user == null) {
				JUtil.showWarningDialog(null, "Invalid Recipient Email", buildLabel("Jandor cannot find the user you've listed. Please make sure you're sending to an existing user."));
				showDialog();
				return;
			}
			
			boolean sentDeck;
			boolean sentNotification = true;
			boolean tryNotification;
			
			sentDeck = MailUtil.sendDeck(user, deckId, message);
			
			tryNotification = sentDeck && user.getEmail() != null && !user.getEmail().isEmpty() && notifyCheck.isSelected() && LoginUtil.getUser().getEmail() != null && !LoginUtil.getUser().getEmail().isEmpty();
			if(tryNotification) {
				String userName = LoginUtil.getUser().getUsername();
				String note = ""; 
				if(!message.isEmpty()) {
					note += message.replace("\n", "<br>");  
					message += "<br>- " + LoginUtil.getUser().getUsername();
				}
				String html = MailUtil.toDeckHtml(deckId);
				note += "<br><br>" + html;
				note += "<br><i>Please open Jandor and check your inbox for this deck.</i>";
				note = "<html>" + note + "</html>";
				
				List<String> emails = new ArrayList<String>();
				emails.add(user.getEmail());
				emails.add(LoginUtil.getUser().getEmail());
				
				sentNotification = MailUtil.send(emails, "[Jandor] " + userName + " added \"" + header.getName() + "\" to " + LoginUtil.getUser().getUsername() + "'s saddlebag!", note, true);
			}
			
			String info = "";
			boolean success = sentDeck && (!tryNotification || sentNotification);
			
			if(sentDeck) {
				info += "Successfuly sent the deck \"" + header.getName() + "\" to " + user.getUsername();
				if(tryNotification) {
					if(sentNotification) {
						//info += " and a notification to " + user.getEmail();
					} else {
						//info += " but couldn't send a notification to " + user.getEmail();
					}
				} 
			} else {
				info += "Couldn't send the deck \"" + header.getName() + "\" to " + user.getUsername();
			}
			
			if(success) {
				JUtil.showMessageDialog(null, "Successfully Sent Deck \"" + header.getName() + "\"", info + ".");
			} else {
				JUtil.showWarningDialog(null, "Could Not Send Deck \"" + header.getName() + "\"", info + ".");
				showDialog();
			}
		}
	}
	
}
