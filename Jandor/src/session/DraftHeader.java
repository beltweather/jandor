package session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import util.DebugUtil;
import util.FileUtil;
import util.LoginUtil;
import util.UserUtil;

@XmlRootElement
public class DraftHeader extends SessionData {

	public static final int TYPE_BOOSTER = 0;
	public static final int TYPE_RANDOM = 1;
	public static final int TYPE_SET_BOOSTER = 2;
	
	private long timeFirstCreated;
	@Deprecated
	private String author;
	private String authorGUID;
	private String authorUsername;
	private int packs;
	private int lands;
	private int mythics;
	private int rares;
	private int uncommons;
	private int commons;
	private boolean includeMythicsAsRares;
	private boolean includeLandsAsRarities;
	private boolean includeFoils;
	private int type;
	private int totalCards;
	private int deckId;
	private int turn;
	private int round;
	private boolean finished;
	@Deprecated
	private List<String> userEmails = new ArrayList<String>();
	private List<String> userGUIDs = new ArrayList<String>();
	private List<String> setPacks = new ArrayList<String>();
	
	public DraftHeader() {
		super();
	}
	
	public DraftHeader(int id) {
		super(id);
	}
	
	public long getTimeFirstCreated() {
		return timeFirstCreated;
	}

	public void setTimeFirstCreated(long timeFirstCreated) {
		this.timeFirstCreated = timeFirstCreated;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getPacks() {
		return packs;
	}

	public void setPacks(int packs) {
		this.packs = packs;
	}

	public int getLands() {
		return lands;
	}

	public void setLands(int lands) {
		this.lands = lands;
	}

	public int getMythics() {
		return mythics;
	}

	public void setMythics(int mythics) {
		this.mythics = mythics;
	}

	public int getRares() {
		return rares;
	}

	public void setRares(int rares) {
		this.rares = rares;
	}

	public int getUncommons() {
		return uncommons;
	}

	public void setUncommons(int uncommons) {
		this.uncommons = uncommons;
	}

	public int getCommons() {
		return commons;
	}

	public void setCommons(int commons) {
		this.commons = commons;
	}

	public boolean isIncludeMythicsAsRares() {
		return includeMythicsAsRares;
	}

	public void setIncludeMythicsAsRares(boolean includeMythicsAsRares) {
		this.includeMythicsAsRares = includeMythicsAsRares;
	}

	public boolean isIncludeLandsAsRarities() {
		return includeLandsAsRarities;
	}

	public void setIncludeLandsAsRarities(boolean includeLandsAsRarities) {
		this.includeLandsAsRarities = includeLandsAsRarities;
	}

	public boolean isIncludeFoils() {
		return includeFoils;
	}

	public void setIncludeFoils(boolean includeFoils) {
		this.includeFoils = includeFoils;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTotalCards() {
		return totalCards;
	}

	public void setTotalCards(int totalCards) {
		this.totalCards = totalCards;
	}

	public int getDeckId() {
		return deckId;
	}

	public void setDeckId(int deckId) {
		this.deckId = deckId;
	}

	public List<String> getUserGUIDs() {
		return userGUIDs;
	}
	
	public void setUserGUIDs(List<String> userGUIDs) {
		this.userGUIDs = userGUIDs;
	}

	@Deprecated
	public List<String> getUserEmails() {
		return userEmails;
	}

	@Deprecated
	public void setUserEmails(List<String> userEmails) {
		this.userEmails = userEmails;
	}

	public List<String> getSetPacks() {
		return setPacks;
	}

	public void setSetPacks(List<String> setPacks) {
		this.setPacks = setPacks;
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
	
	@Override
	public File getFolder() {
		return FileUtil.getDraftHeaderFolder();
	}
	
	@Override
	public void delete() {
		super.delete();
		DraftContent content = Session.getInstance().getDraftContent(getId());
		if(content != null) {
			content.delete();
		}
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	private void incrementTurn() {
		turn++;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
	private void incrementRound() {
		round++;
	}
	
	/**
	 * This keeps track of what turn and action we're on, allowing us to know what
	 * boosters to expect.
	 */
	public void cardSelected() {
		incrementTurn();
		if(turn >= totalCards) {
			turn = 0;
			incrementRound();
		}
		if(round == packs) {
			setFinished(true);
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public boolean isValidCurrentBooster(int boosterId) {
		return isValidCurrentBooster(Session.getInstance().getBoosterHeader(boosterId));
	}
	
	public boolean isValidCurrentBooster(BoosterHeader booster) {
		return !isFinished() && booster.getDraftId() == getId() && booster != null && booster.getRound() == getRound() && booster.getTurn() == getTurn();
	}
	
	public boolean needsToCreateBooster() {
		return turn == 0;
	}
	
	public User getReceiverUser() {
		User user = LoginUtil.getUser();
		String myGUID = user.getGUID();
		int myIndex = userGUIDs.indexOf(myGUID);
		
		// Move left if round is even, or right if not
		int nextIndex;
		if(round % 2 == 0) {
			nextIndex = myIndex - 1;
			if(nextIndex < 0) {
				nextIndex = userGUIDs.size() - 1;
			}
		} else {
			nextIndex = myIndex + 1;
			if(nextIndex >= userGUIDs.size()) {
				nextIndex = 0;
			}
		}
		return UserUtil.getUserByGUID(userGUIDs.get(nextIndex));
	}
	
	@Deprecated
	public String getReceiverEmail() {
		User user = LoginUtil.getUser();
		String myEmail = user.getEmail();
		int myIndex = userEmails.indexOf(myEmail);
		
		// Move left if round is even, or right if not
		int nextIndex;
		if(round % 2 == 0) {
			nextIndex = myIndex - 1;
			if(nextIndex < 0) {
				nextIndex = userEmails.size() - 1;
			}
		} else {
			nextIndex = myIndex + 1;
			if(nextIndex >= userEmails.size()) {
				nextIndex = 0;
			}
		}
		return userEmails.get(nextIndex);
	}
	
}
