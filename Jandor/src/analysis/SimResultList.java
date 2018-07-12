package analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class SimResultList extends ArrayList<SimResult> {

	public SimResultList() {}
	
	public SimResult getBestResult() {
		double minScore = Double.MAX_VALUE;
		List<SimResult> tiedResults = new ArrayList<SimResult>(); 
		for(SimResult result : this) {
			double score = result.getScore();
			if(score < minScore) {
				minScore = score;
				tiedResults.clear();
				tiedResults.add(result);
			} else if(score == minScore) {
				tiedResults.add(result);
			}
		}
		
		return maybeBreakTie(tiedResults);
	}
	
	private SimResult maybeBreakTie(List<SimResult> tiedResults) {
		if(tiedResults.size() == 0) {
			return null;
		}
		
		if(tiedResults.size() == 1) {
			return tiedResults.get(0);
		}
		
		return breakTie(tiedResults);
	}

	public abstract SimResult breakTie(List<SimResult> tiedResults);
	
	@Deprecated
	public void squishScores() {
		double avg = 0;
		for(SimResult result : this) {
			avg += result.getScore();
		}
		avg = SimResult.modifyScore(avg / (double) size());
		
		for(SimResult result : this) {
			if(Math.abs(result.getScore() - avg) <= 1) {
				//result.setScore(avg);
			}
		}
	}
	
}
