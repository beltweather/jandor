package session;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Tag extends SessionData {
	
	public static final int ALL_ID = 0;
	public static final int INBOX_ID = 1;
	
	public static Tag createDefaultTag(String name) {
		Tag tag = new Tag();
		tag.newId();
		tag.setName(name);
		return tag;
	}
	
	private String name;
	private boolean show = true;
	
	public Tag() {
		super();
	}
	
	public Tag(int id) {
		super(id);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}
	
	@Override
	public File getFolder() {
		return FileUtil.getTagFolder();
	}

	@Override
	public String toString() {
		return name;
	}
	
}
