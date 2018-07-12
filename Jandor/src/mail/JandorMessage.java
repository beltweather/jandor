package mail;

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.google.api.services.drive.model.File;

public class JandorMessage {
	
	public static boolean isJandorMessage(Message message) throws MessagingException, IOException {
		return isDeck(message) || isDraft(message) || isBooster(message);
	}
	
	private static boolean isDeck(Message message) throws MessagingException, IOException {
		return message != null && message.getSubject().contains("Jandor") && 
				message.getContent().toString().contains("<deckHeader>") && message.getContent().toString().contains("</deckContent>");
	}
	
	private static boolean isDraft(Message message) throws MessagingException, IOException {
		return message != null && message.getSubject().contains("Jandor") && 
				message.getContent().toString().contains("<draftHeader>") && message.getContent().toString().contains("</draftContent>");
	}
	
	private static boolean isBooster(Message message) throws MessagingException, IOException {
		return message != null && message.getSubject().contains("Jandor") && 
				message.getContent().toString().contains("<boosterHeader>") && message.getContent().toString().contains("</boosterContent>");
	}
	
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_DECK = 0;
	public static final int TYPE_DRAFT = 1;
	public static final int TYPE_BOOSTER = 2;
	public static final int TYPE_CARDLAYER = 3;
	public static final int TYPE_INVITE = 4;
	public static final int TYPE_INVITE_RESPONSE = 5;
	
	private String from;
	private String subject;
	private String content;
	private Date date;
	private int type;
	private int dataId;
	private File file;
	
	public JandorMessage(String from, String subject, String content, Date date, int type) throws MessagingException, IOException {
		this.from = from;
		this.subject = subject;
		this.content = content;
		this.date = date;
		this.type = type;
	}
	
	public JandorMessage(Message message) throws MessagingException, IOException {
		this.from = message.getFrom()[0].toString();
		this.subject = message.getSubject();
		this.content = message.getContent().toString();
		this.date = message.getSentDate();
		if(isDeck(message)) {
			this.type = TYPE_DECK;
		} else if(isDraft(message)) {
			this.type = TYPE_DRAFT;
		} else if(isBooster(message)) {
			this.type = TYPE_BOOSTER;
		} else {
			this.type = TYPE_UNKNOWN;
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public boolean isDeck() {
		return type == TYPE_DECK;
	}
	
	public boolean isDraft() {
		return type == TYPE_DRAFT;
	}
	
	public boolean isBooster() {
		return type == TYPE_BOOSTER;
	}
	
	public boolean isCardLayer() {
		return type == TYPE_CARDLAYER;
	}
	
	public boolean isInvite() {
		return type == TYPE_INVITE;
	}
	
	public boolean isInviteResponse() {
		return type == TYPE_INVITE_RESPONSE;
	}
	
	public int getType() {
		return type;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getContent() {
		return content;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getSubjectName() {
		int start = subject.indexOf("(");
		int end = subject.lastIndexOf(")");
		return subject.substring(start+1, end);
	}
	
	@Override
	public String toString() {
		return "Subject: " + getSubject() + "\n" +
			   "From: " + getFrom() + "\n" +
			   "Text: " + getContent() + "\n" +
			   "Date: " + getDate();
	}
	
	public String getHeaderTag(boolean isClose) {
		if(isDeck()) {
			return "<" + (isClose ? "/" : "") + "deckHeader>";
		} else if(isDraft()) {
			return "<" + (isClose ? "/" : "") + "draftHeader>";
		} else if(isBooster()) {
			return "<" + (isClose ? "/" : "") + "boosterHeader>";
		}
		return "";
	}
	
	public String getContentTag(boolean isClose) {
		if(isDeck()) {
			return "<" + (isClose ? "/" : "") + "deckContent>";
		} else if(isDraft()) {
			return "<" + (isClose ? "/" : "") + "draftContent>";
		} else if(isBooster()) {
			return "<" + (isClose ? "/" : "") + "boosterContent>";
		}
		return "";
	}
	
	public String getHeaderString() {
		int start = getContent().indexOf(getHeaderTag(false));
		int end = getContent().indexOf(getHeaderTag(true)) + getHeaderTag(true).length();
		return getContent().substring(start, end);
	}
	
	public String getContentString() {
		int start = getContent().indexOf(getContentTag(false));
		int end = getContent().indexOf(getContentTag(true)) + getContentTag(true).length();
		return getContent().substring(start, end);
	}

	public int getDataId() {
		return dataId;
	}
	
	public void setDataId(int dataId) {
		this.dataId = dataId;
	}
	
}
