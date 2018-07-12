package session;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;

@XmlRootElement
public class Preferences extends SessionData {

	private boolean lightView = true;
	
	public Preferences() {
		super(00000);
	}

	@Override
	public File getFolder() {
		return FileUtil.getPreferencesFolder();
	}

	public boolean isLightView() {
		return lightView;
	}

	public void setLightView(boolean lightView) {
		this.lightView = lightView;
	}
	
}
