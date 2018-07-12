package session;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;
import deck.Deck;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DraftContent extends DeckContent {

	public DraftContent() {
		super();
	}
		
	public DraftContent(int id) {
		super(id);
	}
	
	public DraftContent(int id, Deck deck) {
		super(id, deck);
	}
	
	@Override
	public File getFolder() {
		return FileUtil.getDraftContentFolder();
	}
	
}
