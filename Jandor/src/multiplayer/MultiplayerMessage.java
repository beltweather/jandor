package multiplayer;

import java.io.Serializable;

import deck.RenderableList;

public class MultiplayerMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static MultiplayerMessage getDisconnectMessage(String disconnectGUID) {
		return new MultiplayerMessage(true, disconnectGUID);
	}
	
	private RenderableList<?> renderables;
	private boolean handViewable;
	private boolean deckViewable;
	private boolean graveyardViewable;
	private boolean exileViewable;
	private boolean disconnect = false;
	private String disconnectGUID = null;
	
	public MultiplayerMessage() {
		this(null);
	}
	
	private MultiplayerMessage(boolean disconnect, String disconnectGUID) {
		renderables = new RenderableList();
		this.disconnect = true;
		this.disconnectGUID = disconnectGUID;
	}
	
	public MultiplayerMessage(RenderableList<?> renderables) {
		this(renderables, false, false, true, true);
	}
	
	public MultiplayerMessage(RenderableList<?> renderables, boolean handViewable, boolean deckViewable, boolean graveyardViewable, boolean exileViewable) {
		this.renderables = renderables;
		this.handViewable = handViewable;
		this.deckViewable = deckViewable;
		this.graveyardViewable = graveyardViewable;
		this.exileViewable = exileViewable;
	}
	
	public RenderableList<?> getAllObjects() {
		return renderables;
	}
	
	public boolean isHandViewable() {
		return handViewable;
	}
	
	public boolean isDeckViewable() {
		return deckViewable;
	}
	
	public boolean isGraveyardViewable() {
		return graveyardViewable;
	}
	
	public boolean isExileViewable() {
		return exileViewable;
	}
	
	public boolean isDisconnect() {
		return disconnect;
	}
	
	public String getDisconnectGUID() {
		return disconnectGUID;
	}
	
}
