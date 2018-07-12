package session;


public class CardContent {

	private String name;
	private int count;
	private boolean sideboard;
	
	public CardContent() {
		
	}
	
	public CardContent(String name, int count, boolean sideboard) {
		this.name = name;
		this.count = count;
		this.sideboard = sideboard;
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

	
}
