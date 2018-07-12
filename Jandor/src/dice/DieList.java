package dice;

import java.util.List;

import deck.RenderableList;

public class DieList extends RenderableList<Die> {

	public DieList() {
		this(null);
	}
	
	public DieList(List<Die> dice) {
		super(dice);
	}
	
}
