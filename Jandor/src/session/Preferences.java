package session;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

import util.FileUtil;

@XmlRootElement
public class Preferences extends SessionData {

	private boolean lightView = true;
	private boolean showCardCounts = true;
	
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
	
	public boolean isShowCardCounts() {
		return showCardCounts;
	}
	
	public void setShowCardCounts(boolean showCardCounts) {
		this.showCardCounts = showCardCounts;
	}
	
}
