package session;


public class CardContent {

	private String name;
	private int count;
	private boolean sideboard;
	private boolean commander;
	
	public CardContent() {
		
	}
	
	public CardContent(String name, int count, boolean sideboard) {
		this(name, count, sideboard, false);
	}
	
	public CardContent(String name, int count, boolean sideboard, boolean commander) {
		this.name = name;
		this.count = count;
		this.sideboard = sideboard;
		this.commander = commander;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isSideboard() {
		return sideboard;
	}

	public void setSideboard(boolean sideboard) {
		this.sideboard = sideboard;
	}

	public boolean isCommander() {
		return commander;
	}
	
	public void setCommander(boolean commander) {
		this.commander = commander;
	}
	
}
