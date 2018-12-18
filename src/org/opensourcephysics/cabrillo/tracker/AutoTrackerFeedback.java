package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.cabrillo.tracker.AutoTracker.KeyFrame;

/**
 * @author Nikolai Avdeev aka NickKolok
 */
public class AutoTrackerFeedback {
	public void setSelectedTrack(TTrack track){}

	public void onBeforeAddKeyframe(double x, double y){}
	public void onAfterAddKeyframe(KeyFrame keyFrame){}
}
