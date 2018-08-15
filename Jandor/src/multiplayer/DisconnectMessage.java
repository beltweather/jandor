package multiplayer;

import java.io.Serializable;

public class DisconnectMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String disconnectGUID;
	
	public DisconnectMessage(String disconnectGUID) {
		this.disconnectGUID = disconnectGUID;
	}
	
	public String getDisconnectGUID() {
		return disconnectGUID;
	}
	
}
