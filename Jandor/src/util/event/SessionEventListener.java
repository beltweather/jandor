package util.event;


public abstract class SessionEventListener {
	
	private Object owner;
	
	public SessionEventListener(Object owner) {
		this.owner = owner;
	}
	
	public Object getOwner() {
		return owner;
	}
	
	public void clearOwner() {
		owner = null;
	}
	
	public abstract void handleEvent(SessionEvent event);

}
