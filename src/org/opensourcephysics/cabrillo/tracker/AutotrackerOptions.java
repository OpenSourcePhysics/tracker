package org.opensourcephysics.cabrillo.tracker;

public class AutotrackerOptions {
	private int goodMatch=4, possibleMatch=1, evolveAlpha=63, autoskipCount=2;
	private boolean lookAhead=true;
	public static final int maxEvolveRate = 100;

	public int getGoodMatch() {
		return goodMatch;
	}

	public void setGoodMatch(int goodMatch) {
		this.goodMatch = goodMatch;
	}

	public boolean isMatchGood(double match){
		return match > goodMatch;
	}
	public int getPossibleMatch() {
		return possibleMatch;
	}

	public void setPossibleMatch(int possibleMatch) {
		this.possibleMatch = possibleMatch;
	}

	public boolean isMatchPossible(double match){
		return match > possibleMatch;
	}

	public int getEvolveAlpha() {
		return evolveAlpha;
	}

	public void setEvolveAlpha(int evolveAlpha) {
		this.evolveAlpha = evolveAlpha;
	}

	protected void setEvolveAlphaFromRate(int evolveRate) {
		double max = maxEvolveRate;
		int alpha = (int)(1.0*evolveRate*255/max);
		if (evolveRate>=max) alpha = 255;
		if (evolveRate<=0) alpha = 0;
		evolveAlpha=alpha;
	}

	public int getAutoskipCount() {
		return autoskipCount;
	}

	public void setAutoskipCount(int autoskipCount) {
		this.autoskipCount = autoskipCount;
	}

	public boolean isLookAhead() {
		return lookAhead;
	}

	public void setLookAhead(boolean lookAhead) {
		this.lookAhead = lookAhead;
	}
}
