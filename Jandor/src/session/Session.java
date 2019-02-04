package session;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.FileUtil;
import util.IDUtil;
import util.JAXBUtil;
import deck.Card;
import deck.Deck;



public class Session {

	private static Session session;

	public synchronized static Session getInstance() {
		if(session == null) {
			session = new Session();
			session.createFolders();
			session.cacheData();
		}
		return session;
	}

	public static void init() {
		getInstance();
	}

	private static Deck buildDeck(DeckHeader header, DeckContent content) {
		Deck deck = new Deck();
		deck.setName(header.getName());
		deck.setSideboard(new Deck(header.getName() + " - Sideboard"));
		for(CardContent cardContent : content.getCards()) {
			Card card = new Card(cardContent.getName());
			if(cardContent.isSideboard()) {
				deck.getSideboard().add(card, cardContent.getCount());
			} else {
				deck.add(card, cardContent.getCount());
			}
		}
		return deck;
	}

	private static Deck buildDeck(DraftHeader header, DraftContent content) {
		Deck deck = new Deck();
		deck.setName("Draft");
		deck.setSideboard(new Deck("Draft - Sideboard"));
		for(CardContent cardContent : content.getCards()) {
			Card card = new Card(cardContent.getName());
			if(cardContent.isSideboard()) {
				deck.getSideboard().add(card, cardContent.getCount());
			} else {
				deck.add(card, cardContent.getCount());
			}
		}
		return deck;
	}

	private static <T extends SessionData> T newReference(Class<? extends T> klass, int id) {
		T obj = null;
		try {
			obj = (T) klass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if(obj == null) {
			return null;
		}
		obj.setId(id);
		return obj;
	}

	Map<Integer, DeckHeader> headers = new HashMap<Integer, DeckHeader>();
	Map<Integer, DeckContent> contents = new HashMap<Integer, DeckContent>();

	Map<Integer, DraftHeader> draftHeaders = new HashMap<Integer, DraftHeader>();
	Map<Integer, DraftContent> draftContents = new HashMap<Integer, DraftContent>();

	Map<Integer, BoosterHeader> boosterHeaders = new HashMap<Integer, BoosterHeader>();
	Map<Integer, BoosterContent> boosterContents = new HashMap<Integer, BoosterContent>();

	Map<Integer, Tag> tags = new HashMap<Integer, Tag>();
	Map<Integer, List<Integer>> headerIdsByTagId = new HashMap<Integer, List<Integer>>();
	Map<Integer, Contact> contacts = new HashMap<Integer, Contact>();
	Map<Integer, User> users = new HashMap<Integer, User>();

	private Preferences preferences = null;

	private Session() {}

	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	private Object add(Map data, File file) {
		SessionData obj = (SessionData) FileUtil.readXML(file);
		data.put(obj.getId(), obj);
		return obj;
	}

	private synchronized void createFolders() {
		// Create folders if they don't exist
		FileUtil.getHeaderFolder();
		FileUtil.getContentFolder();
		FileUtil.getDraftHeaderFolder();
		FileUtil.getDraftContentFolder();
		FileUtil.getBoosterHeaderFolder();
		FileUtil.getBoosterContentFolder();
		FileUtil.getTagFolder();
		FileUtil.getContactFolder();
		FileUtil.getUserFolder();
		FileUtil.getPreferencesFolder();
	}

	private synchronized void cacheData() {
		// Cache all headers
		for(File file : FileUtil.getHeaderFiles()) {
			add(headers, file);
		}

		// Cache all tags
		for(File file : FileUtil.getTagFiles()) {
			Tag tag = (Tag) add(tags, file);
			getDeckHeaderIdsWithTag(tag);
		}

		if(!hasTag(Tag.ALL_ID)) {
			Tag tag = new Tag(Tag.ALL_ID);
			tag.setName("All");
			tag.save();
		}

		if(!hasTag(Tag.INBOX_ID)) {
			Tag tag = new Tag(Tag.INBOX_ID);
			tag.setName("Inbox");
			tag.save();
		}

		for(String author : getAuthors()) {
			if(!hasTag(author)) {
				Tag tag = new Tag();
				tag.newId();
				tag.setName(author);
				tag.save();
			}
		}

		// Cache all contacts
		for(File file : FileUtil.getContactFiles()) {
			add(contacts, file);
		}

		// Cache all users
		for(File file : FileUtil.getUserFiles()) {
			add(users, file);
		}

		// Cache all draft headers
		for(File file : FileUtil.getDraftHeaderFiles()) {
			add(draftHeaders, file);
		}

		// Cache all booster headers
		for(File file : FileUtil.getBoosterHeaderFiles()) {
			add(boosterHeaders, file);
		}

		// Cache all booster headers
		for(File file : FileUtil.getPreferenceFiles()) {
			preferences = (Preferences) FileUtil.readXML(file);
		}

	}

	public Preferences getPreferences() {
		if(preferences == null) {
			preferences = new Preferences();
			preferences.save();
		}
		return preferences;
	}

	public synchronized boolean hasDeckHeader(int id) {
		return headers.containsKey(id);
	}

	public synchronized boolean hasDraftHeader(int id) {
		return draftHeaders.containsKey(id);
	}

	public synchronized boolean hasBoosterHeader(int id) {
		return boosterHeaders.containsKey(id);
	}

	public synchronized boolean hasTag(int id) {
		return tags.containsKey(id);
	}

	public synchronized boolean hasTag(String tagName) {
		for(Tag tag : tags.values()) {
			if(tag.getName().equals(tagName)) {
				return true;
			}
		}
		return false;
	}

	public synchronized Tag getTag(String tagName) {
		for(Tag tag : tags.values()) {
			if(tag.getName().equalsIgnoreCase(tagName)) {
				return tag;
			}
		}
		return null;
	}

	public synchronized int getTagIdForAuthor(String authorFormatted) {
		Tag tag = getTag(authorFormatted);
		if(tag != null) {
			return tag.getId();
		}
		return 0;
	}

	public synchronized void rebuildHeadersByTagIdCache() {
		headerIdsByTagId.clear();
	}

	public synchronized boolean hasDeckContent(int id) {
		DeckContent content = newReference(DeckContent.class, id);
		return content.exists();
	}

	public synchronized boolean hasDraftContent(int id) {
		DraftContent draftContent = newReference(DraftContent.class, id);
		return draftContent.exists();
	}

	public synchronized boolean hasBoosterContent(int id) {
		BoosterContent boosterContent = newReference(BoosterContent.class, id);
		return boosterContent.exists();
	}

	public synchronized DeckHeader getDeckHeader(int id) {
		if(!hasDeckHeader(id)) {
			return null;
		}
		return headers.get(id);
	}

	public synchronized DraftHeader getDraftHeader(int id) {
		if(!hasDraftHeader(id)) {
			return null;
		}
		return draftHeaders.get(id);
	}

	public synchronized BoosterHeader getBoosterHeader(int id) {
		if(!hasBoosterHeader(id)) {
			return null;
		}
		return boosterHeaders.get(id);
	}

	public synchronized boolean hasDeckHeader(String name) {
		for(DeckHeader header : headers.values()) {
			if(header.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public synchronized Tag getTag(int id) {
		if(!hasTag(id)) {
			return null;
		}
		return tags.get(id);
	}

	public synchronized DeckContent getDeckContent(int id) {
		if(!hasDeckContent(id)) {
			return null;
		}
		if(!contents.containsKey(id)) {
			add(contents, newReference(DeckContent.class, id).getFile());
		}
		return contents.get(id);
	}

	public synchronized DraftContent getDraftContent(int id) {
		if(!hasDraftContent(id)) {
			return null;
		}
		if(!draftContents.containsKey(id)) {
			add(draftContents, newReference(DraftContent.class, id).getFile());
		}
		return draftContents.get(id);
	}

	public synchronized BoosterContent getBoosterContent(int id) {
		if(!hasBoosterContent(id)) {
			return null;
		}
		if(!boosterContents.containsKey(id)) {
			add(boosterContents, newReference(BoosterContent.class, id).getFile());
		}
		return boosterContents.get(id);
	}

	public synchronized List<DeckHeader> getDeckHeadersWithTagId(int tagId) {
		return getDeckHeadersWithTagId(tagId, false);
	}

	public synchronized List<DeckHeader> getDeckHeadersWithTagId(int tagId, boolean ignoreInbox) {
		List<DeckHeader> tagIdHeaders = new ArrayList<DeckHeader>();
		for(DeckHeader header : headers.values()) {
			if(header.hasTagId(tagId) && (!ignoreInbox || (ignoreInbox && !header.isInbox()))) {
				tagIdHeaders.add(header);
			}
		}
		return tagIdHeaders;
	}

	public synchronized List<DeckHeader> getDeckHeadersInInbox() {
		List<DeckHeader> tagIdHeaders = new ArrayList<DeckHeader>();
		for(DeckHeader header : headers.values()) {
			if(header.isInbox()) {
				tagIdHeaders.add(header);
			}
		}
		return tagIdHeaders;
	}

	public synchronized List<Integer> getDeckHeaderIdsWithTagId(int tagId) {
		List<Integer> tagIdHeaders = new ArrayList<Integer>();

		if(headerIdsByTagId.containsKey(tagId)) {
			return new ArrayList<Integer>(headerIdsByTagId.get(tagId));
		}

		for(DeckHeader header : headers.values()) {
			if(header.hasTagId(tagId)) {
				tagIdHeaders.add(header.getId());
			}
		}
		headerIdsByTagId.put(tagId, tagIdHeaders);

		return tagIdHeaders;
	}

	public synchronized List<DeckHeader> getDeckHeadersWithTag(Tag tag) {
		if(tag == null) {
			return new ArrayList<DeckHeader>();
		}
		return getDeckHeadersWithTagId(tag.getId());
	}

	public synchronized List<Integer> getDeckHeaderIdsWithTag(Tag tag) {
		if(tag == null) {
			return new ArrayList<Integer>();
		}
		return getDeckHeaderIdsWithTagId(tag.getId());
	}

	public synchronized List<Integer> getDeckIds() {
		return new ArrayList<Integer>(headers.keySet());
	}

	public synchronized boolean hasContact() {
		return hasContact(Contact.USER_ID);
	}

	public synchronized boolean hasContact(int id) {
		return contacts.containsKey(id);
	}

	public synchronized boolean hasContactEmail() {
		return hasContact(Contact.USER_ID) && !getContact(Contact.USER_ID).getJandorEmail().isEmpty();
	}

	public synchronized Contact getContact() {
		return getContact(Contact.USER_ID);
	}

	public synchronized Contact getContact(int id) {
		if(!hasContact(id)) {
			return null;
		}
		return contacts.get(id);
	}

	public synchronized Contact getContactByName(String name) {
		for(Contact contact : contacts.values()) {
			if(contact.getNickname().equalsIgnoreCase(name)) {
				return contact;
			}
		}
		return null;
	}

	public synchronized Contact getContactByEmail(String email) {
		for(Contact contact : contacts.values()) {
			if(contact.getJandorEmail().equals(email)) {
				return contact;
			}
		}
		return null;
	}

	public synchronized void removeContact(Contact contact) {
		if(contact == null) {
			return;
		}

		int id = contact.getId();
		if(!contacts.containsKey(id)) {
			return;
		}
		contacts.remove(id);
	}

	public synchronized List<Contact> getContacts() {
		return new ArrayList<Contact>(contacts.values());
	}

	public synchronized List<Contact> getContacts(boolean ignoreUser) {
		List<Contact> t = new ArrayList<Contact>(contacts.values());
		if(ignoreUser && hasContact(Contact.USER_ID)) {
			t.remove(getContact(Contact.USER_ID));
		}
		return t;
	}

	public synchronized boolean hasUser() {
		return hasUser(User.USER_ID);
	}

	public synchronized boolean hasUser(int id) {
		return users.containsKey(id);
	}

	public synchronized boolean hasUserEmail() {
		return hasUser(User.USER_ID) && !getUser(User.USER_ID).getEmail().isEmpty();
	}

	public synchronized User getUser() {
		return getUser(User.USER_ID);
	}

	public synchronized User getUser(int id) {
		if(!hasUser(id)) {
			return null;
		}
		return users.get(id);
	}

	public synchronized User getUserByName(String name) {
		for(User user : users.values()) {
			if(user.getUsername().equalsIgnoreCase(name)) {
				return user;
			}
		}
		return null;
	}

	public synchronized User getUserByEmail(String email) {
		for(User user : users.values()) {
			if(user.getEmail().equals(email)) {
				return user;
			}
		}
		return null;
	}

	public synchronized void removeUser(User user) {
		if(user == null) {
			return;
		}

		int id = user.getId();
		if(!users.containsKey(id)) {
			return;
		}
		users.remove(id);
	}

	public synchronized List<DeckHeader> getDeckHeaders() {
		return new ArrayList<DeckHeader>(headers.values());
	}

	public synchronized List<DraftHeader> getDraftHeaders() {
		return new ArrayList<DraftHeader>(draftHeaders.values());
	}

	public synchronized List<BoosterHeader> getBoosterHeaders() {
		return new ArrayList<BoosterHeader>(boosterHeaders.values());
	}

	public synchronized List<Tag> getTags() {
		return getTags(false);
	}

	public synchronized List<Tag> getTags(boolean ignoreDefaults) {
		List<Tag> t = new ArrayList<Tag>(tags.values());
		if(ignoreDefaults) {
			t.remove(getTag(Tag.ALL_ID));
			t.remove(getTag(Tag.INBOX_ID));
		}
		return t;
	}

	public synchronized List<String> getAuthors() {
		List<String> authors = new ArrayList<>();
		for(int id : getDeckIds()) {
			DeckHeader header = getDeckHeader(id);
			String author = header.getAuthorFormatted();
			if(!authors.contains(author)) {
				authors.add(author);
			}
		}
		return authors;
	}

	public synchronized Deck getDeck(int id) {
		if(!hasDeckHeader(id) || !hasDeckContent(id)) {
			if(!hasDraftHeader(id) || !hasDraftContent(id)) {
				return null;
			}
			return buildDeck(getDraftHeader(id), getDraftContent(id));
		}
		return buildDeck(getDeckHeader(id), getDeckContent(id));
	}

	public synchronized void removeDeckHeader(DeckHeader header) {
		if(header == null) {
			return;
		}

		int id = header.getId();
		if(!headers.containsKey(id)) {
			return;
		}
		headers.remove(id);
		for(int tagId : header.getTagIds()) {
			headerIdsByTagId.remove(tagId);
		}
	}

	public synchronized void removeDraftHeader(DraftHeader header) {
		if(header == null) {
			return;
		}

		int id = header.getId();
		if(!draftHeaders.containsKey(id)) {
			return;
		}
		draftHeaders.remove(id);
	}

	public synchronized void removeBoosterHeader(BoosterHeader header) {
		if(header == null) {
			return;
		}

		int id = header.getId();
		if(!boosterHeaders.containsKey(id)) {
			return;
		}
		boosterHeaders.remove(id);
	}

	public synchronized void removeTag(Tag tag) {
		if(tag == null) {
			return;
		}

		int id = tag.getId();
		if(!tags.containsKey(id)) {
			return;
		}

		tags.remove(id);
		for(DeckHeader header : getDeckHeadersWithTagId(id)) {
			header.removeTagId(id);
			header.save();
		}
		headerIdsByTagId.remove(id);
	}

	public synchronized void removeDeckContent(DeckContent content) {
		if(content == null) {
			return;
		}

		int id = content.getId();
		if(contents.containsKey(id)) {
			contents.remove(id);
		}
	}

	public synchronized void removeDraftContent(DraftContent content) {
		if(content == null) {
			return;
		}

		int id = content.getId();
		if(draftContents.containsKey(id)) {
			draftContents.remove(id);
		}
	}

	public synchronized void removeBoosterContent(BoosterContent content) {
		if(content == null) {
			return;
		}

		int id = content.getId();
		if(boosterContents.containsKey(id)) {
			boosterContents.remove(id);
		}
	}

	public synchronized void cache(SessionData data) {
		if(data == null) {
			return;
		}

		if(data instanceof DeckHeader) {
			headers.put(data.getId(), (DeckHeader) data);
		} else if(data instanceof DeckContent) {
			contents.put(data.getId(), (DeckContent) data);
		} else if(data instanceof Contact) {
			contacts.put(data.getId(), (Contact) data);
		} else if(data instanceof User) {
			users.put(data.getId(), (User) data);
		} else if(data instanceof Tag) {
			tags.put(data.getId(), (Tag) data);
		} else if(data instanceof DraftHeader) {
			draftHeaders.put(data.getId(), (DraftHeader) data);
		} else if(data instanceof DraftContent) {
			draftContents.put(data.getId(), (DraftContent) data);
		} else if(data instanceof BoosterHeader) {
			boosterHeaders.put(data.getId(), (BoosterHeader) data);
		} else if(data instanceof BoosterContent) {
			boosterContents.put(data.getId(), (BoosterContent) data);
		}

		rebuildHeadersByTagIdCache();
	}

	public synchronized void delete(SessionData data) {
		if(data == null) {
			return;
		}

		if(data instanceof DeckHeader) {
			removeDeckHeader((DeckHeader) data);
		} else if(data instanceof DeckContent) {
			removeDeckContent((DeckContent) data);
		} else if(data instanceof Contact) {
			removeContact((Contact) data);
		} else if(data instanceof Tag) {
			removeTag((Tag) data);
		} else if(data instanceof DraftHeader) {
			removeDraftHeader((DraftHeader) data);
		} else if(data instanceof DraftContent) {
			removeDraftContent((DraftContent) data);
		}

		rebuildHeadersByTagIdCache();
	}

	public synchronized SessionData find(int sessionDataId) {
		if(sessionDataId == IDUtil.NONE) {
			return null;
		}

		if(headers.containsKey(sessionDataId)) {
			return headers.get(sessionDataId);
		}

		if(contents.containsKey(sessionDataId)) {
			return contents.get(sessionDataId);
		}

		if(draftHeaders.containsKey(sessionDataId)) {
			return draftHeaders.get(sessionDataId);
		}

		if(draftContents.containsKey(sessionDataId)) {
			return draftContents.get(sessionDataId);
		}

		if(boosterHeaders.containsKey(sessionDataId)) {
			return boosterHeaders.get(sessionDataId);
		}

		if(boosterContents.containsKey(sessionDataId)) {
			return boosterContents.get(sessionDataId);
		}

		if(contacts.containsKey(sessionDataId)) {
			return contacts.get(sessionDataId);
		}

		if(users.containsKey(sessionDataId)) {
			return users.get(sessionDataId);
		}

		if(tags.containsKey(sessionDataId)) {
			return tags.get(sessionDataId);
		}

		return null;
	}

	public synchronized int importDeck(Deck deck) {
		if(deck == null) {
			return -1;
		}
		DeckHeader header = new DeckHeader(deck);
		DeckContent content = new DeckContent(header, deck);
		header.save();
		content.save();
		return header.getId();
	}

	public static void main(String[] args) {
		Session session = Session.getInstance();
		for(DeckHeader h : session.getDeckHeaders()) {
			JAXBUtil.print(h);
		}
	}
}
