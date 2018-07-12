package session;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;
import deck.Deck;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BoosterContent extends DeckContent {

	public BoosterContent() {
		super();
	}
		
	public BoosterContent(int id) {
		super(id);
	}
	
	public BoosterContent(int id, Deck deck) {
		super(id, deck);
	}
	
	@Override
	public File getFolder() {
		return FileUtil.getBoosterContentFolder();
	}	
	
}
