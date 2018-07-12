package analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import deck.Card;

public class SimResult {

	public static double modifyScore(double score) {
		return Math.round(score);
	}
	
	private Map<String, Integer> countsByLand;
	private double score;
	private int targetDeckSize;
	
	public SimResult(int targetDeckSize) {
		this(targetDeckSize, null);
	}
	
	public SimResult(int targetDeckSize, List<Card> lands) {
		this(targetDeckSize, lands, Double.MAX_VALUE);
	}
	
	public SimResult(int targetDeckSize, List<Card> lands, double score) {
		this.targetDeckSize = targetDeckSize;
		this.countsByLand = new HashMap<String, Integer>();
		this.score = score;
		if(lands != null) {
			add(lands);
		}
	}
	
	public void add(String landName, int count) {
		countsByLand.put(landName, count);
	}
	
	public void add(List<Card> lands) {
		for(Card land : lands) {
			String name = land.getName();
			if(!countsByLand.containsKey(name)) {
				countsByLand.put(name, 1);
			} else {
				countsByLand.put(name, countsByLand.get(name) + 1);
			}
		}
	}
	
	public double getScore() {
		return modifyScore(score);
	}
	
	public double getRawScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public Map<String, Integer> getCountsByLand() {
		return countsByLand;
	}
	
	public int getTotalLandCount() {
		int count = 0;
		for(String land : countsByLand.keySet()) {
			count += countsByLand.get(land);
		}
		return count;
	}
	
	public int getTargetDeckSize() {
		return targetDeckSize;
	}
	
	public double getTotalLandPercent() {
		return getTotalLandCount() / (double) getTargetDeckSize();
	}
	
	public int getMaxSpecificLandCount() {
		int max = 0;
		for(int count : countsByLand.values()) {
			if(count > max) {
				max = count;
			}
		}
		return max;
	}
	
}
