package org.opensourcephysics.cabrillo.tracker;

import java.beans.PropertyChangeSupport;

/**
 * This class contains options, which are used for AutoTracker,
 * and some basic logic to work with them.
 * The main purpose of this class is to separate user-defined options of AutoTracker
 * which refer (in most cases) to whole track or its big part
 * from internal variables, which AutoTracker uses and varies widely.
 * These options may be attached, for example, to a track,
 * or saved/loaded to/from .trk file,
 * if someone implements appropriate loaders ;-)
 *
 * @author Nikolai Avdeev aka NickKolok
 */
public class AutoTrackerOptions implements Cloneable {
	private int goodMatch=4, possibleMatch=1, evolveAlpha=63, autoskipCount=2;
	private int lineSpread = -1;  // positive for 1D, negative for 2D tracking
	private double maskWidth=16.0, maskHeight=16.0;
	private boolean lookAhead=true;
	public static final int maxEvolveRate = 100;

	// TODO: make private?
	public PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public int getGoodMatch() {
		return goodMatch;
	}

	public void setGoodMatch(int goodMatch) {
		int oldMatch = this.goodMatch;
		this.goodMatch = goodMatch;
		changes.firePropertyChange("goodMatch", oldMatch, goodMatch);
	}

	public boolean isMatchGood(double match){
		return match > goodMatch;
	}
	public int getPossibleMatch() {
		return possibleMatch;
	}

	public void setPossibleMatch(int possibleMatch) {
		int oldMatch = this.possibleMatch;
		this.possibleMatch = possibleMatch;
		changes.firePropertyChange("possibleMatch", oldMatch, possibleMatch);
	}

	public boolean isMatchPossible(double match){
		return match > possibleMatch;
	}

	public int getEvolveAlpha() {
		return evolveAlpha;
	}

	public void setEvolveAlpha(int evolveAlpha) {
		int oldAlpha = this.evolveAlpha;
		this.evolveAlpha = evolveAlpha;
		changes.firePropertyChange("evolveAlpha", oldAlpha, evolveAlpha);
	}

	protected void setEvolveAlphaFromRate(int evolveRate) {
		double max = maxEvolveRate;
		int alpha = (int)(1.0*evolveRate*255/max);
		if (evolveRate>=max) alpha = 255;
		if (evolveRate<=0) alpha = 0;
		setEvolveAlpha(alpha);
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

	public double getMaskWidth() {
		return maskWidth;
	}

	public void setMaskWidth(double maskWidth) {
		if(this.maskWidth == maskWidth){
			return;
		}
		double old = this.maskWidth;
		this.maskWidth = maskWidth;
		changes.firePropertyChange("maskWidth", old, this.maskWidth);
	}

	public double getMaskHeight() {
		return maskHeight;
	}

	public void setMaskHeight(double maskHeight) {
		if(this.maskHeight == maskHeight){
			return;
		}
		double old = this.maskHeight;
		this.maskHeight = maskHeight;
		changes.firePropertyChange("maskHeight", old, this.maskHeight);
	}

	public int getLineSpread() {
		return lineSpread;
	}

	public void setLineSpread(int spread) {
		if(this.lineSpread == spread){
			return;
		}
		double old = this.lineSpread;
		this.lineSpread = spread;
		changes.firePropertyChange("lineSpread", old, this.lineSpread);
	}

	// TODO: fire messages for all properties
	// TODO: cloning without cloning `changes`
	// TODO: fire message only if the property has been changed indeed
}
