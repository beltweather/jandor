package util;

import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

import com.google.api.services.drive.model.File;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailSSLSocketFactory;

import deck.Card;
import deck.Deck;
import mail.JandorMessage;
import session.BoosterContent;
import session.BoosterHeader;
import session.DeckContent;
import session.DeckHeader;
import session.DraftContent;
import session.DraftHeader;
import session.User;
import ui.ProgressBar.ProgressTask;
import ui.view.DeckEditorView;
import util.event.SessionEvent;
import util.event.SessionEventManager;

public class MailUtil {

	public static void init() {
		//startListening(); // We won't rely on email anymore, so no need to listen, although maybe we listen in a different way
	}

	private static final List<String> savedMessages = new ArrayList<String>();

	private MailUtil() {}

	public static User getUser() {
		return LoginUtil.getUser();
	}

	public static boolean sendToDriveInbox(User user, String filePrefix, String text) {
		return sendToDriveInbox(Collections.singletonList(user), filePrefix, text, false);
	}

	public static boolean sendToDriveInbox(User user, String filePrefix, String text, boolean isHtml) {
		return sendToDriveInbox(Collections.singletonList(user), filePrefix, text, isHtml);
	}

	public static boolean sendToDriveInbox(List<User> users, String filePrefix, String text) {
		return sendToDriveInbox(users, filePrefix, text, false);
	}

	public static boolean sendToDriveInbox(List<User> users, String filePrefix, String text, boolean isHtml) {
		int success = 0;
		for(User user : users) {
			/*if(user.equals(LoginUtil.getUser())) {
				success++;
				continue;
			}*/

			if(DriveUtil.createFile(IDUtil.generateGUID(filePrefix), text, user.getInboxFolderId()) != null) {
				success++;
			}
		}
		return success == users.size();
	}

	public static boolean sendToDriveBackup(String backupFolderId, String filePrefix, String text) {
		return DriveUtil.createFile(IDUtil.generateGUID(filePrefix), text, backupFolderId) != null;
	}

	public static boolean send(String toEmail, String subject, String text) {
		return send(Collections.singletonList(toEmail), subject, text, false);
	}

	public static boolean send(String toEmail, String subject, String text, boolean isHtml) {
		return send(Collections.singletonList(toEmail), subject, text, isHtml);
	}

	public static boolean send(List<String> toEmails, String subject, String text) {
		return send(toEmails, subject, text, false);
	}

	public static boolean send(List<String> toEmails, String subject, String text, boolean isHtml) {
		if(toEmails.size() == 0) {
			return false;
		}

		Session session = getSessionForSending();
		try {
			User user = getUser();
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(getUserEmailForSending(user)));
			boolean first = true;
			for(String toEmail : toEmails) {
				if(first) {
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
					first = false;
				} else {
					message.addRecipient(Message.RecipientType.CC, new InternetAddress(toEmail));
				}
			}
			message.setSubject(subject);
			if(isHtml) {
				message.setContent(text, "text/html; charset=utf-8");
			} else {
				message.setText(text);
			}
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String getUserEmailForSending(User user) {
		return "jandor.saddlebags@gmail.com";
	}

	public static String getUserEmailPasswordForSending(User user) {
		return "jandor88";
	}

	public static boolean isValidUser() {
		User user = getUser();
		if(user == null) {
			return false;
		}
		return isValid(getUserEmailForSending(user), getUserEmailPasswordForSending(user));
	}

	public static boolean isValid(String email, String password) {
		try {
			Session emailSession = getSessionForReceiving();
			String host = "imap.gmail.com";
			Store store = emailSession.getStore("imaps");
			store.connect(host, email, password);
		} catch (NoSuchProviderException e) {
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static List<JandorMessage> receive() {
		List<JandorMessage> jandorMessages = new ArrayList<JandorMessage>();
		User user = getUser();

		try {
			Session emailSession = getSessionForReceiving();
			String host = "imap.gmail.com";

			//create the POP3 store object and connect with the pop server
			Store store = emailSession.getStore("imaps");
			store.connect(host, getUserEmailForSending(user), getUserEmailPasswordForSending(user));

			//create the folder object and open it
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_WRITE);

			jandorMessages = getJandorMessages(emailFolder);

			//close the store and folder objects
			emailFolder.close(true);
			store.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jandorMessages;
	}

	private static List<JandorMessage> getJandorMessages(Folder emailFolder) throws MessagingException, IOException {
		List<JandorMessage> jandorMessages = new ArrayList<JandorMessage>();

		// retrieve the messages from the folder in an array and print it
		Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

		System.out.println("messages.length---" + messages.length);

		for (int i = 0; i < messages.length; i++) {
		   Message message = messages[i];
		   if(JandorMessage.isJandorMessage(message)) {
			   jandorMessages.add(new JandorMessage(message));
		   } else {
			   message.setFlag(Flag.DELETED, true);
		   }
		}

		return jandorMessages;
	}

	private static Session getSessionForSending() {
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		if(sf != null) {
			sf.setTrustAllHosts(true);
			properties.put("mail.smtp.ssl.socketFactory", sf);
		}

		final User user = getUser();
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUserEmailForSending(user), getUserEmailPasswordForSending(user));
			}

		});

		return session;
	}

	private static Session getSessionForReceiving() {
		String host = "imap.gmail.com";
		Properties properties = new Properties();
		properties.put("mail.imap.host", host);
		properties.put("mail.imap.port", "995");
		properties.put("mail.imap.starttls.enable", "true");

		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		if(sf != null) {
			sf.setTrustAllHosts(true);
			properties.put("mail.imaps.ssl.socketFactory", sf);
		}

		Session session = Session.getDefaultInstance(properties);
		return session;
	}

	private static void printMessage(JandorMessage message) {
		System.out.println("---------------------------------");
		System.out.println(message);
	}

	public static boolean sendDeck(String toEmail, int deckId, String message) {
		Deck deck = session.Session.getInstance().getDeck(deckId);
		return send(toEmail, "Jandor: Deck (" + deck.getName() + ")", toDeckXML(deckId, message));
	}

	public static boolean sendDeck(User user, int deckId, String message) {
		return DriveUtil.createFile(IDUtil.generateGUID(IDUtil.PREFIX_DECK), toDeckXML(deckId, message), user.getInboxFolderId()) != null;
	}

	public static String toDeckXML(int deckId) {
		return toDeckXML(deckId, null);
	}

	public static String toDeckXML(int deckId, String message) {
		DeckHeader header = session.Session.getInstance().getDeckHeader(deckId);
		DeckContent content = session.Session.getInstance().getDeckContent(deckId);

		// Temporarily add this message as the not for this header, then revert it.
		String note = header.getNote();
		if(note != null && !note.isEmpty()) {
			header.setNote(note + "\n\nSender says: \"" + message + "\"");
		} else {
			header.setNote("Sender says: \"" + message + "\"");
		}
		List<Integer> tagIds = header.getTagIds();
		header.setTagIds(new ArrayList<Integer>());
		String author = header.getAuthor();
		String authorGUID = header.getAuthorGUID();
		String authorUsername = header.getAuthorUsername();
		header.setAuthor(getUser().getEmail());
		header.setAuthorGUID(getUser().getGUID());
		header.setAuthorUsername(getUser().getUsername());

		StringWriter stringWriter = new StringWriter();
		JAXBUtil.marshal(header, stringWriter);
		JAXBUtil.marshal(content, stringWriter);

		header.setTagIds(tagIds);
		header.setAuthor(author);
		header.setAuthorGUID(authorGUID);
		header.setAuthorUsername(authorUsername);

		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	public static String toDeckHtml(int deckId) {
		Deck deck = session.Session.getInstance().getDeck(deckId);
		if(deck == null) {
			return null;
		}

		ShuffleUtil.shuffle(ShuffleType.MANA_LH, deck);
		Map<Card, Integer> cards = deck.getCountsByCard();
		StringBuilder sb = new StringBuilder();
		sb.append("<h3>" + deck.getName() + "</h3>");

		Set<Card> usedCards = new HashSet<Card>();
		for(String type : DeckEditorView.types) {
			int total = 0;
			boolean useType = false;
			List<Card> cardsForType = new ArrayList<Card>();
			for(Card card : cards.keySet()) {
				if(!CardUtil.hasType(card, type) || usedCards.contains(card) || (CardUtil.hasType(card, "Land") && !type.equals("Land"))) {
					continue;
				}
				usedCards.add(card);
				useType = true;
				total += cards.get(card);
				cardsForType.add(card);
			}

			if(useType) {
				sb.append("<b>" + type + "s (" + total + ")" + "</b><br>");
				for(Card card : cardsForType) {
					sb.append(cards.get(card) + " " + CardUtil.toGathererLink(card.getMultiverseId(), card.getFullName()) + "<br>");
				}
				sb.append("<br>");
			}
		}

		return sb.toString();
	}

	public static boolean sendDraftToDrive(List<User> users, int draftId) {
		if(DebugUtil.OFFLINE_MODE || users == null || users.size() == 0) {
			return false;
		}

		DraftHeader header = session.Session.getInstance().getDraftHeader(draftId);
		DraftContent content = session.Session.getInstance().getDraftContent(draftId);

		StringWriter stringWriter = new StringWriter();
		JAXBUtil.marshal(header, stringWriter);
		JAXBUtil.marshal(content, stringWriter);
		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sendToDriveInbox(users, IDUtil.PREFIX_DRAFT, stringWriter.toString());
	}

	public static boolean sendBoosterToDrive(User user, int boosterId) {
		if(user == null) {
			return false;
		}

		BoosterHeader header = session.Session.getInstance().getBoosterHeader(boosterId);
		BoosterContent content = session.Session.getInstance().getBoosterContent(boosterId);

		StringWriter stringWriter = new StringWriter();
		JAXBUtil.marshal(header, stringWriter);
		JAXBUtil.marshal(content, stringWriter);
		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sendToDriveInbox(user, IDUtil.PREFIX_BOOSTER, stringWriter.toString());
	}

	@Deprecated
	public static boolean sendDraftToEmail(List<User> users, List<String> toEmails, int draftId) {
		if(toEmails.size() == 0) {
			return false;
		}

		DraftHeader header = session.Session.getInstance().getDraftHeader(draftId);
		DraftContent content = session.Session.getInstance().getDraftContent(draftId);

		StringWriter stringWriter = new StringWriter();
		JAXBUtil.marshal(header, stringWriter);
		JAXBUtil.marshal(content, stringWriter);
		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return send(toEmails, "Jandor: Draft (" + draftId + ")", stringWriter.toString());
	}

	@Deprecated
	public static boolean sendBoosterToEmail(String toEmail, int boosterId) {
		if(toEmail == null || toEmail.isEmpty()) {
			return false;
		}

		BoosterHeader header = session.Session.getInstance().getBoosterHeader(boosterId);
		BoosterContent content = session.Session.getInstance().getBoosterContent(boosterId);

		StringWriter stringWriter = new StringWriter();
		JAXBUtil.marshal(header, stringWriter);
		JAXBUtil.marshal(content, stringWriter);
		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return send(toEmail, "Jandor: Booster (" + boosterId + ")", stringWriter.toString());
	}

	public static synchronized boolean saveMessage(JandorMessage jandorMessage) {
		if(hasRead(jandorMessage)) {
			return false;
		}
		markAsRead(jandorMessage);

		if(jandorMessage.isDeck()) {

			DeckHeader header = null;
			DeckContent content = null;

			try {
				header = (DeckHeader) JAXBUtil.unmarshal(DeckHeader.class, jandorMessage.getHeaderString());
				content = (DeckContent) JAXBUtil.unmarshal(DeckContent.class, jandorMessage.getContentString());
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}

			header.newId();
			content.setId(header.getId());
			header.setInbox(true);
			header.save();
			content.save();

			jandorMessage.setDataId(header.getId());

		} else if(jandorMessage.isDraft()) {

			DraftHeader header = null;
			DraftContent content = null;

			try {
				header = (DraftHeader) JAXBUtil.unmarshal(DraftHeader.class, jandorMessage.getHeaderString());
				content = (DraftContent) JAXBUtil.unmarshal(DraftContent.class, jandorMessage.getContentString());
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}

			header.save();
			content.save();

			jandorMessage.setDataId(header.getId());

		} else if(jandorMessage.isBooster()) {

			BoosterHeader header = null;
			BoosterContent content = null;

			try {
				header = (BoosterHeader) JAXBUtil.unmarshal(BoosterHeader.class, jandorMessage.getHeaderString());
				content = (BoosterContent) JAXBUtil.unmarshal(BoosterContent.class, jandorMessage.getContentString());
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}

			header.newId();
			content.setId(header.getId());
			header.save();
			content.save();

			jandorMessage.setDataId(header.getId());

		} /*else if(jandorMessage.isCardLayer()) {
			String userGUID = jandorMessage.getFile().getName().split(":")[1];
			FriendUtil.updateConnectedView(userGUID, UserUtil.getUserByGUID(userGUID).getUsername(), jandorMessage.getContent());
		} else if(jandorMessage.isInvite()) {
			String userGUID = jandorMessage.getContent();
			User user = UserUtil.getUserByGUID(userGUID);
			boolean response = JUtil.showConfirmYesNoDialog(null, "Accept Invite", "User \"" + user.getUsername() + "\" would like to connect. Will you accept?");
			if(response) {
				FriendUtil.connectToFriend(user);
			}
			String inviteGUID = IDUtil.extractPrefixGUID(jandorMessage.getFile().getName());
			MailUtil.sendToDriveInbox(user, IDUtil.toInviteResponsePrefix(inviteGUID), String.valueOf(response));

		} else if(jandorMessage.isInviteResponse()) {
			String inviteGUID = IDUtil.extractPrefixGUID(jandorMessage.getFile().getName());
			FriendUtil.setInviteResponse(inviteGUID, Boolean.valueOf(jandorMessage.getContent()));
		}*/

		return true;
	}

	public static void receiveFilesFromDrive(List<File> files) {
		receiveFilesFromDrive(true, files);
	}

	public static void receiveFilesFromDrive(boolean newThread, final List<File> files) {
		/*if(!isValidUser()) {
			System.err.println("Cannot receive decks. User is not valid!");
			return;
		}*/

		System.out.println("Receiving decks!");
		if(newThread) {
			Thread th = new Thread(new Runnable() {
			      public void run() {
			    	  receiveFilesInnerFromDrive(files);
			      }
			});
			th.start();
		} else {
			receiveFilesInnerFromDrive(files);
		}
	}

	public static void receiveFilesInnerFromDrive(List<File> files) {
		SessionEventManager.disableEvents();
		User user = LoginUtil.getUser();

		List<JandorMessage> deckMessages = new ArrayList<JandorMessage>();
		List<JandorMessage> draftMessages = new ArrayList<JandorMessage>();
		List<JandorMessage> boosterMessages = new ArrayList<JandorMessage>();

		List<JandorMessage> messages = new ArrayList<JandorMessage>();
		try {
			if(files == null) {
				files = DriveUtil.getFilesInFolder(user.getInboxFolderId());
			}
			for(File file : files) {
				if(isDeck(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_DECK));
				} else if(isDraft(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_DRAFT));
				} else if(isBooster(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_BOOSTER));
				} else if(isCardLayer(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_CARDLAYER));
				} else if(isInvite(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_INVITE));
				} else if(isInviteResponse(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_INVITE_RESPONSE));
				}
				messages.get(messages.size() - 1).setFile(file);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(JandorMessage message : messages) {
			if(saveMessage(message)) {
				DriveUtil.deleteFile(message.getFrom());
			}
			if(message.isDeck()) {
				deckMessages.add(message);
			} else if(message.isDraft()) {
				draftMessages.add(message);
			} else if(message.isBooster()) {
				boosterMessages.add(message);
			} else if(message.isCardLayer()) {

			} else if(message.isInvite()) {

			} else if(message.isInviteResponse()) {

			}
		}
		SessionEventManager.enableEvents();
		if(deckMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(DeckHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, deckMessages);
		}
		if(draftMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(DraftHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, draftMessages);
		}
		if(boosterMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(BoosterHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, boosterMessages);
		}
	}

	public static int receiveFilesFromBackupFolder(ProgressTask task) {
		SessionEventManager.disableEvents();
		User user = LoginUtil.getUser();

		List<JandorMessage> deckMessages = new ArrayList<JandorMessage>();
		List<JandorMessage> messages = new ArrayList<JandorMessage>();
		int deckCount = 0;
		int success = 0;
		String backupFolderId = user.getBackupFolderId();
		int progressTotal = 0;
		try {
			List<File> files = DriveUtil.getFilesInFolder(backupFolderId);
			progressTotal = files.size() * 2;
			for(File file : files) {
				if(isDeck(file)) {
					messages.add(new JandorMessage(file.getId(), "Jandor " + file.getId(), DriveUtil.getFileContent(file.getId()), new Date(0), JandorMessage.TYPE_DECK));
					deckCount++;
					task.setWorkerProgress(deckCount, progressTotal);
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(JandorMessage message : messages) {
			if(saveMessage(message)) {
				success++;
				DriveUtil.deleteFile(message.getFrom());
				task.setWorkerProgress(success + deckCount, progressTotal);
			}
			if(message.isDeck()) {
				deckMessages.add(message);
			}
		}
		SessionEventManager.enableEvents();
		if(deckMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(DeckHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, deckMessages);
		}

		// Delete backup on restore, on second thought, let's keep it around
		//if(deckCount == success) {
		//	DriveUtil.deleteFile(backupFolderId);
		//}

		return deckCount;
	}

	public static boolean isDeck(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_DECK);
	}

	public static boolean isDraft(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_DRAFT);
	}

	public static boolean isBooster(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_BOOSTER);
	}

	public static boolean isCardLayer(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_CARDLAYER);
	}

	public static boolean isInvite(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_INVITE);
	}

	public static boolean isInviteResponse(File file) {
		return file != null && file.getName().startsWith(IDUtil.PREFIX_INVITE_RESPONSE);
	}

	@Deprecated
	public static void receiveDecksInnerFromEmail() {
		SessionEventManager.disableEvents();
		List<JandorMessage> deckMessages = new ArrayList<JandorMessage>();
		List<JandorMessage> draftMessages = new ArrayList<JandorMessage>();
		List<JandorMessage> boosterMessages = new ArrayList<JandorMessage>();

		for(JandorMessage message : receive()) {
			saveMessage(message);
			if(message.isDeck()) {
				deckMessages.add(message);
			} else if(message.isDraft()) {
				draftMessages.add(message);
			} else if(message.isBooster()) {
				boosterMessages.add(message);
			}
		}
		SessionEventManager.enableEvents();
		if(deckMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(DeckHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, deckMessages);
		}
		if(draftMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(DraftHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, draftMessages);
		}
		if(boosterMessages.size() > 0) {
			SessionEventManager.fireMessageEvent(BoosterHeader.class, IDUtil.NONE, SessionEvent.TYPE_CREATED, boosterMessages);
		}
	}

	private static synchronized boolean hasRead(JandorMessage message) {
		String key = message.getSubject() + ":" + message.getDate().getTime();
		return savedMessages.contains(key);
	}

	private static synchronized void markAsRead(JandorMessage message) {
		String key = message.getSubject() + ":" + message.getDate().getTime();
		if(!savedMessages.contains(key)) {
			savedMessages.add(key);
		}
	}

	@Deprecated
	public static void startListeningToEmail() {
		User user = getUser();

		try {
			Session emailSession = getSessionForReceiving();
			String host = "imap.gmail.com";

			//create the POP3 store object and connect with the pop server
			Store store = emailSession.getStore("imaps");
			store.connect(host, getUserEmailForSending(user), getUserEmailPasswordForSending(user));

			//create the folder object and open it
			IMAPFolder emailFolder = (IMAPFolder) store.getFolder("INBOX");
			emailFolder.open(Folder.READ_WRITE);

			emailFolder.addMessageCountListener(new MessageCountAdapter() {

				public void messagesAdded(MessageCountEvent ev) {
					System.out.println("Message added!");
					receiveDecksInnerFromEmail();
				}

			});

			// We need to create a new thread to keep alive the connection
		    Thread t = new Thread(
		    	new InboxListener(emailFolder), "IdleConnectionInboxListener"
		    );
		    t.start();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class InboxListener implements Runnable {

		private IMAPFolder folder;

		public InboxListener(IMAPFolder folder) {
			this.folder = folder;
		}

		@Override
		public void run() {
			// We need to create a new thread to keep alive the connection
		    Thread t = new Thread(
		        new KeepAliveRunnable(folder), "IdleConnectionKeepAlive"
		    );

		    t.start();

		    while (!Thread.interrupted()) {
		        System.out.println("Listening for Mail");
		        try {
		            folder.idle();
		        } catch (MessagingException e) {
		        	e.printStackTrace();
		        	throw new RuntimeException(e);
		        }
		    }

		    // Shutdown keep alive thread
		    if (t.isAlive()) {
		        t.interrupt();
		    }
		}
	}

	/**
	 * Runnable used to keep alive the connection to the IMAP server
	 */
	private static class KeepAliveRunnable implements Runnable {

	    private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes

	    private IMAPFolder folder;

	    public KeepAliveRunnable(IMAPFolder folder) {
	        this.folder = folder;
	    }

	    @Override
	    public void run() {
	        while (!Thread.interrupted()) {
	            try {
	                Thread.sleep(KEEP_ALIVE_FREQ);

	                // Perform a NOOP just to keep alive the connection
	                System.out.println("Performing a NOOP to keep alive the connection");
	                folder.doCommand(new IMAPFolder.ProtocolCommand() {
	                    public Object doCommand(IMAPProtocol p)
	                            throws ProtocolException {
	                        p.simpleCommand("NOOP", null);
	                        return null;
	                    }
	                });
	            } catch (InterruptedException e) {
	                // Ignore, just aborting the thread...
	            } catch (MessagingException e) {
	                // Shouldn't really happen...
	            	e.printStackTrace();
	            }
	        }
	    }
	}

	public static void main(String[] args) {
		if(send("jandor.jmharter88@gmail.com", "Jandor: Deck", "[deck content]")) {
			System.out.println("Sent");
		} else {
			System.out.println("Failed to Send");
		}
		for(JandorMessage message : receive()) {
			printMessage(message);
		}
	}
}
