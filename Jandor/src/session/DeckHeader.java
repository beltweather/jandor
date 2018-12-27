package session;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import deck.Card;
import deck.Deck;
import util.DebugUtil;
import util.FileUtil;
import util.LoginUtil;
import util.ManaUtil;
import util.UserUtil;

@XmlRootElement
public class DeckHeader extends SessionData {

	public static DeckHeader createDefaultHeader() {
		User user = LoginUtil.getUser();

		DeckHeader header = new DeckHeader();
		header.newId();
		header.setName("Untitled");
		header.setAuthor(user.getEmail());
		header.setAuthorGUID(user.getGUID());
		header.setAuthorUsername(user.getUsername());
		header.setTimeFirstCreated(System.currentTimeMillis());
		header.setTimeLastModified(System.currentTimeMillis());
		return header;
	}

	public static final String PATH = "";

	private String name;
	private long timeLastModified = 0;
	private long timeFirstCreated = 0;
	private String colors = "";
	@Deprecated
	private String author = "";
	private String authorGUID = "";
	private String authorUsername = "";
	private String note = "";
	private boolean isNew = false;
	private int revision = 0;
	private List<Integer> tagIds = new ArrayList<Integer>();
	private boolean inbox = false;
	// filename & version need added to xml

	public DeckHeader() {
		super();
	}

	public DeckHeader(int id) {
		super(id);
	}

	public DeckHeader(Deck deck) {
		if(deck == null) {
			return;
		}

		User user = LoginUtil.getUser();
		newId();
		name = deck.getName();
		timeFirstCreated = System.currentTimeMillis();
		timeLastModified = timeFirstCreated;
		colors = deriveManaCostColors(deck);
		author = user.getEmail();
		authorGUID = user.getGUID();
		authorUsername = user.getUsername();
		note = "";
		isNew = true;
		revision = 0;
		inbox = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public void setTimeLastModified(long timeLastModified) {
		this.timeLastModified = timeLastModified;
	}

	public long getTimeFirstCreated() {
		return timeFirstCreated;
	}

	public void setTimeFirstCreated(long timeFirstCreated) {
		this.timeFirstCreated = timeFirstCreated;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	@Deprecated
	public String getAuthor() {
		return author;
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

	@Deprecated
	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@XmlElement(name="tagId")
	public List<Integer> getTagIds() {
		return tagIds;
	}

	public void setTagIds(List<Integer> tagIds) {
		this.tagIds = tagIds;
	}

	public void addTagId(int id) {
		if(!tagIds.contains(id)) {
			tagIds.add(id);
		}
	}

	public void removeTagId(int id) {
		if(tagIds.contains(id)) {
			tagIds.remove((Integer) id);
		}
	}

	public void addTag(Tag tag) {
		if(tag != null) {
			addTagId(tag.getId());
		}
	}

	public void removeTag(Tag tag) {
		if(tag != null) {
			removeTagId(tag.getId());
		}
	}

	public boolean hasTag(Tag tag) {
		if(tag == null) {
			return false;
		}
		return hasTagId(tag.getId());
	}

	public boolean hasTagId(int tagId) {
		if(tagId == Tag.ALL_ID) {
			return true;
		}
		if(tagId == Tag.INBOX_ID) {
			return inbox;
		}
		return tagIds.contains(tagId);
	}

	public void clearTags() {
		tagIds.clear();
	}

	public void setTags(List<Tag> tags) {
		tagIds.clear();
		for(Tag tag : tags) {
			tagIds.add(tag.getId());
		}
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public boolean isInbox() {
		return inbox;
	}

	public void setInbox(boolean inbox) {
		this.inbox = inbox;
	}

	@Override
	public File getFolder() {
		return FileUtil.getHeaderFolder();
	}

	@Override
	public void save() {
		revision++;
		setTimeLastModified(System.currentTimeMillis());
		super.save();
		for(int tagId : tagIds) {
			Tag tag = Session.getInstance().getTag(tagId);
			if(tag.isDirty()) {
				tag.save();
			}
		}
	}

	@Override
	public void delete() {
		super.delete();
		DeckContent content = Session.getInstance().getDeckContent(getId());
		if(content != null) {
			content.delete();
		}
	}

	public DeckHeader copy() {
		DeckHeader copy = new DeckHeader();
		copy.id = id;
		copy.name = name;
		copy.timeFirstCreated = timeFirstCreated;
		copy.timeLastModified = timeLastModified;
		copy.colors = colors;
		copy.author = author;
		copy.authorGUID = authorGUID;
		copy.authorUsername = authorUsername;
		copy.note = note;
		copy.isNew = isNew;
		copy.revision = revision;
		copy.inbox = inbox;
		copy.tagIds = new ArrayList<Integer>(tagIds);
		return copy;
	}

	private String deriveColorIdentity(Deck deck) {
		List<String> colors = new ArrayList<String>();
		for(Card card : deck) {
			if(card.isLand()) {
				continue;
			}

			List<String> colorIdentity = card.getColorIdentity();
			if(colorIdentity == null) {
				continue;
			}
			for(String color : colorIdentity) {
				if(!colors.contains(color)) {
					colors.add(color);
				}
			}
		}

		Collections.sort(colors, new Comparator<String>() {

			@Override
			public int compare(String colorA, String colorB) {
				return ManaUtil.indexOfColorCharacter(colorA) - ManaUtil.indexOfColorCharacter(colorB);
			}

		});

		String colorStr = "";
		for(String color : colors) {
			colorStr += color;
		}

		return colorStr;
	}

	private String deriveManaCostColors(Deck deck) {
		List<String> colorLetters = ManaUtil.getColorLetters();
		List<String> colors = new ArrayList<String>();
		List<String> hybridMana = new ArrayList<String>();

		for(Card card : deck) {
			if(card.isLand()) {
				continue;
			}

			List<String> manaCost = card.getManaCost();
			if(manaCost == null) {
				continue;
			}

			for(String mana : manaCost) {
				if(mana.length() == 1) {
					if(colorLetters.contains(mana) && !colors.contains(mana)) {
						colors.add(mana);
					}
				} else {
					if(!hybridMana.contains(mana)) {
						hybridMana.add(mana);
					}
				}
			}
		}

		for(String mana : hybridMana) {
			boolean hasAlready = false;
			for(int i = 0; i < mana.length(); i++) {
				String m = mana.charAt(i) + "";
				if(!colorLetters.contains(m)) {
					continue;
				}
				if(colors.contains(m)) {
					hasAlready = true;
					break;
				}
			}
			if(!hasAlready) {
				for(int i = 0; i < mana.length(); i++) {
					String m = mana.charAt(i) + "";
					if(colorLetters.contains(m) && !colors.contains(m)) {
						colors.add(m);
					}
				}
			}
		}

		Collections.sort(colors, new Comparator<String>() {

			@Override
			public int compare(String colorA, String colorB) {
				return ManaUtil.indexOfColorCharacter(colorA) - ManaUtil.indexOfColorCharacter(colorB);
			}

		});

		String colorStr = "";
		for(String color : colors) {
			colorStr += color;
		}

		return colorStr;
	}

	public static void main(String[] args) {
		for(int i = 0; i < 100; i++) {
			DeckHeader header = new DeckHeader();
			header.newId();
			//header.setId(12345);
			header.setName("Jandor");
			header.setTimeFirstCreated(System.currentTimeMillis());
			header.setTimeLastModified(System.currentTimeMillis());
			header.setColors("UR");
			header.setAuthor("jandor.jmharter88@gmail.com");
			header.setNote("My newewst deck ever!");
			header.setInbox(false);

			Tag tag1 = new Tag(11111);
			tag1.setName("My tag 1");
			tag1.save();

			Tag tag2 = new Tag(22222);
			tag2.setName("My tag 2");
			tag2.save();

			Tag tag3 = new Tag(33333);
			tag3.setName("My tag 1");
			tag3.save();

			header.addTag(tag1);
			header.addTag(tag2);
			header.addTag(tag3);

			header.setNew(true);
			header.setRevision(12);

			header.save();
			System.out.println("Saved header to file!");

			DeckContent content = new DeckContent(header.getId());
			content.addCard(new CardContent("Llanowar Elves", 4, false));
			content.addCard(new CardContent("Mountain", 12, false));
			content.addCard(new CardContent("Plains", 13, true));
			content.save();
			System.out.println("Saved content to file!");

			/*Deck deck = Session.getInstance().getDeck(header.getId());
			System.out.println(deck);

			tag3.delete();

			for(Tag tag : Session.getInstance().getTags()) {
				System.out.println(tag.getFileName() + " remain");
			}

			for(DeckHeader h : Session.getInstance().getDeckHeadersWithTag(tag2)) {
				System.out.println(h.getFileName() + " uses " + tag2.getFileName());
			}*/
		}
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

}
