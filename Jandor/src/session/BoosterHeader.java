package session;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

import util.DebugUtil;
import util.FileUtil;
import util.UserUtil;

@XmlRootElement
public class BoosterHeader extends SessionData {

	private long timeFirstCreated;
	@Deprecated
	private String author;
	private String authorGUID;
	private String authorUsername;
	private int draftId;
	private int round;
	private int turn;
	
	public BoosterHeader() {
		super();
	}
	
	public BoosterHeader(int id) {
		super(id);
	}
	
	public long getTimeFirstCreated() {
		return timeFirstCreated;
	}

	public void setTimeFirstCreated(long timeFirstCreated) {
		this.timeFirstCreated = timeFirstCreated;
	}

	@Deprecated
	public String getAuthor() {
		return author;
	}

	@Deprecated
	public void setAuthor(String author) {
		this.author = author;
	}

	public int getDraftId() {
		return draftId;
	}

	public void setDraftId(int draftId) {
		this.draftId = draftId;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	public String getAuthorGUID() {
		return authorGUID;
	}

	public void setAuthorGUID(String authorGUID) {
		this.authorGUID = authorGUID;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public void setAuthorUsername(String authorUsername) {
		this.authorUsername = authorUsername;
	}
	
	@Override
	public File getFolder() {
		return FileUtil.getBoosterHeaderFolder();
	}
	
	@Override
	public void delete() {
		super.delete();
		BoosterContent content = Session.getInstance().getBoosterContent(getId());
		if(content != null) {
			content.delete();
		}
	}
	
	public String getAuthorFormatted() {
		return getAuthorFormatted(false);
	}
	
	public String getAuthorFormatted(boolean fullName) {
		if(DebugUtil.OFFLINE_MODE || authorGUID == null || authorGUID.isEmpty()) {
			if(authorUsername != null && !authorUsername.isEmpty()) {
				return authorUsername;
			}
			if(author != null && !author.isEmpty()) {
				return author;
			}
			return "Unknown";
		}
		
		User author = UserUtil.getUserByGUID(authorGUID);
		if(author == null) {
			return "Unknown";
		}
		
		return fullName ? author.getFirstName() + " " + author.getLastName() : author.getUsername();
	}
	
}
