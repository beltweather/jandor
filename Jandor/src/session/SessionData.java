package session;

import java.io.File;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import util.FileUtil;
import util.IDUtil;
import util.JAXBUtil;
import util.VersionUtil;
import util.event.SessionEvent;
import util.event.SessionEventManager;

public abstract class SessionData implements Serializable {
	
	protected int id;
	
	@XmlTransient
	protected boolean dirty = false;
	
	protected String version = null;
	
	public SessionData() {

	}
	
	public SessionData(int id) {
		this.id = id;
	}
	
	public void newId() {
		id = IDUtil.newId();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@XmlElement
	public String getFileName() {
		return getElementName() + "-" + getId() + ".xml";
	}
	
	@XmlElement
	public String getVersion() {
		return version == null ? VersionUtil.VERSION : version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getElementName() {
		return getClass().getSimpleName();
	}
	
	public abstract File getFolder();
	
	public File getFile() {
		return FileUtil.toFile(getFolder(), getFileName());
	}
	
	public void save() {
		JAXBUtil.marshal(this, getFile());
		boolean create = Session.getInstance().find(getId()) == null;
		Session.getInstance().cache(this);
		flagClean();
	
		SessionEventManager.fireEvent(getClass(), id, create ? SessionEvent.TYPE_CREATED : SessionEvent.TYPE_MODIFIED);
	}
	
	public void delete() {
		Session.getInstance().delete(this);
		File file = getFile();
		file.delete();
		SessionEventManager.fireEvent(getClass(), id, SessionEvent.TYPE_DELETED);
	}
	
	public boolean exists() {
		return getFile().exists();
	}
	
	public void flagDirty() {
		dirty = true;
	}
	
	public void flagClean() {
		dirty = false;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
