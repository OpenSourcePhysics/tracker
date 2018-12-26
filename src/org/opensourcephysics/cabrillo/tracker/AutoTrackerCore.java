package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.media.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AutoTrackerCore {
	private static int templateIconMagnification = 2;

	public AutoTrackerOptions options;
	private AutoTrackerControl control;
	private AutoTrackerFeedback feedback;

	public int trackID;
	/* trackFrameData maps tracks to indexFrameData which maps point index
	to frameData which maps frame number to individual FrameData objects */
	public Map<TTrack, Map<Integer, Map<Integer, FrameData>>> trackFrameData
			= new HashMap<TTrack, Map<Integer, Map<Integer, FrameData>>>();

	public AutoTrackerCore(AutoTrackerControl c, AutoTrackerFeedback f){
		control = c;
		feedback = f;
		options = new AutoTrackerOptions();
	}


	public TTrack getTrack() {
		return TTrack.getTrack(trackID);
	}


	/**
	 * Clears search points in frames downstream of the current frame number.
	 */
	protected void clearSearchPointsDownstream() {
		int n = control.getFrameNumber();
		Map<Integer, FrameData> frameData = getFrameData();
		for (Integer i: frameData.keySet()) {
			if (i<=n) continue;
			FrameData frame = frameData.get(i);
			if (frame.isKeyFrame()) // only to the next key frame
				break;
			frame.setSearchPoints(null);
		}

	}

	/**
	 * Adds a key frame for a given track point and mask center position.
	 *
	 * @param p the track point
	 * @param x the mask center x
	 * @param y the mask center y
	 */
	protected void addKeyFrame(TPoint p, double x, double y) {
		feedback.onBeforeAddKeyframe(x,y);

		int n = control.getFrameNumber();
		Shape mask = options.getMaskShape();
		Map<Integer, FrameData> frames = getFrameData();
		KeyFrame keyFrame = new KeyFrame(
				p,
				mask,
				new TPoint(), // TODO: create options.targetOffset and use it! Currently target == (0,0)
				getIndex(p),
				new TPoint(x, y),
				new TPoint(x+options.getMaskWidth()/2, y+options.getMaskHeight()/2)
		);
		frames.put(n, keyFrame);
		clearSearchPointsDownstream();

		feedback.onAfterAddKeyframe(keyFrame);
	}



	/**
	 * Creates a TemplateMatcher based on the current image and mask.
	 *
	 * @return a newly created template matcher, or null if no video image exists
	 */
	protected TemplateMatcher createTemplateMatcher() {
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		KeyFrame keyFrame = frame.getKeyFrame();
		if (control.isVideoValid() && keyFrame!=null) {
			// create template image
			Shape mask = keyFrame.getMask();
			BufferedImage source = control.getImage();
			Rectangle rect = mask.getBounds();
			BufferedImage templateImage = new BufferedImage(
					rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
			templateImage.createGraphics().drawImage(source, -rect.x, -rect.y, null);
			// translate mask to (0, 0) relative to template
			AffineTransform transform = new AffineTransform();
			transform.setToTranslation(-rect.x, -rect.y);
			Shape templateRegion = transform.createTransformedShape(mask);
			return new TemplateMatcher(templateImage, templateRegion);
		}
		return null;
	}

	/**
	 * Gets the TemplateMatcher. May return null.
	 *
	 * @return the template matcher
	 */
	public TemplateMatcher getTemplateMatcher() {
		if (control==null) return null;
		int n = control.getFrameNumber();
		KeyFrame keyFrame = getFrame(n).getKeyFrame();
		if (keyFrame==null)
			return null;
		if (keyFrame.getTemplateMatcher() == null) {
			TemplateMatcher matcher = createTemplateMatcher(); // still null if no video
			keyFrame.setTemplateMatcher(matcher);
		}
		return keyFrame.getTemplateMatcher();
	}

	/**
	 * Builds an evolved template based on data in the specified FrameData
	 * and the current video image.
	 *
	 * @param frame the FrameData frame
	 */
	protected void buildEvolvedTemplate(FrameData frame) {
		TPoint[] matchPts = frame.getMatchPoints();
		if (matchPts == null) return; // can't build template without a match
//  	System.out.println("building evolved for "+frame.getFrameNumber());
		TemplateMatcher matcher = getTemplateMatcher();
		matcher.setTemplate(frame.getTemplate());
		matcher.setWorkingPixels(frame.getWorkingPixels());
		Rectangle rect = frame.getKeyFrame().getMask().getBounds();
		// get new image to rebuild template
		int x = (int) Math.round(matchPts[2].getX());
		int y = (int) Math.round(matchPts[2].getY());
		BufferedImage source = control.getImage();
		BufferedImage matchImage = new BufferedImage(
				rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
		matchImage.createGraphics().drawImage(source, -x, -y, null);
		matcher.buildTemplate(matchImage, options.getEvolveAlpha(), 0);
		matcher.setIndex(frame.getFrameNumber());
	}

	public void forceAccept(int frameNumber) {
		FrameData frame = getFrame(frameNumber);
		// build evolved template
		TemplateMatcher matcher = getTemplateMatcher();
		matcher.setTemplate(frame.getTemplate());
		matcher.setWorkingPixels(frame.getWorkingPixels());
		buildEvolvedTemplate(frame);
		// mark the target
		TPoint p = getMatchTarget(frame.getMatchPoints()[0]);
		TTrack track = getTrack();
		TPoint target = track.autoMarkAt(frameNumber, p.x, p.y);
		frame.setAutoMarkPoint(target);
		frame.decided = true;
	}


	/**
	 * Gets the available derivatives of the specified order. These are NOT time
	 * derivatives, but simply differences in pixel units: order 1 is deltaPosition,
	 * order 2 is change in deltaPosition, order 3 is change in order 2. Note the
	 * TPoint positions are in image units, not world units.
	 *
	 * @param positions an array of positions
	 * @param order may be 1 (v), 2 (a) or 3 (jerk)
	 * @return the derivative data
	 */
	public static double[][] getDerivatives(TPoint[] positions, int order, int lookback) {
		// return null if insufficient data
		if (positions.length<order+1) return null;

		double[][] derivatives = new double[lookback][];
		if (order==1) { // velocity
			for (int i=0; i<derivatives.length; i++) {
				if (i>=positions.length-1) {
					derivatives[i] = null;
					continue;
				}
				TPoint loc0 = positions[i+1];
				TPoint loc1 = positions[i];
				if (loc0==null || loc1==null) {
					derivatives[i] = null;
					continue;
				}
				double x = loc1.getX() -loc0.getX();
				double y = loc1.getY() -loc0.getY();
				derivatives[i] = new double[] {x, y};
			}
			return derivatives;
		}
		else if (order==2) { // acceleration
			for (int i=0; i<derivatives.length; i++) {
				if (i>=positions.length-2) {
					derivatives[i] = null;
					continue;
				}
				TPoint loc0 = positions[i+2];
				TPoint loc1 = positions[i+1];
				TPoint loc2 = positions[i];
				if (loc0==null || loc1==null || loc2==null) {
					derivatives[i] = null;
					continue;
				}
				double x = loc2.getX() - 2*loc1.getX() + loc0.getX();
				double y = loc2.getY() - 2*loc1.getY() + loc0.getY();
				derivatives[i] = new double[] {x, y};
			}
			return derivatives;
		}
		else if (order==3) { // jerk
			for (int i=0; i<derivatives.length; i++) {
				if (i>=positions.length-3) {
					derivatives[i] = null;
					continue;
				}
				TPoint loc0 = positions[i+3];
				TPoint loc1 = positions[i+2];
				TPoint loc2 = positions[i+1];
				TPoint loc3 = positions[i];
				if (loc0==null || loc1==null || loc2==null || loc3==null) {
					derivatives[i] = null;
					continue;
				}
				double x = loc3.getX() - 3*loc2.getX() + 3*loc1.getX() - loc0.getX();
				double y = loc3.getY() - 3*loc2.getY() + 3*loc1.getY() - loc0.getY();
				derivatives[i] = new double[] {x, y};
			}
			return derivatives;
		}
		return null;
	}

	/**
	 * Returns the target for a specified match center point.
	 *
	 * @param center the center point
	 * @return the target
	 */
	public TPoint getMatchTarget(TPoint center) {
		int n = control.getFrameNumber();
		double[] offset = getFrame(n).getTargetOffset();
		return new TPoint(center.x+offset[0], center.y+offset[1]);
	}

	/**
	 * Returns the center point for a specified match target.
	 *
	 * @param target the target
	 * @return the center
	 */
	public TPoint getMatchCenter(TPoint target) {
		int n = control.getFrameNumber();
		double[] offset = getFrame(n).getTargetOffset();
		return new TPoint(target.x-offset[0], target.y-offset[1]);
	}

	/**
	 * Gets the predicted target point in a specified video frame,
	 * based on previously marked steps.
	 *
	 * @param frameNumber the frame number
	 * @return the predicted target
	 */
	public TPoint getPredictedMatchTarget(int frameNumber) {
		boolean success = false;
		int stepNumber = control.frameToStep(frameNumber);
		TPoint predictedTarget = new TPoint();

		// get position data at previous steps
		TPoint[] prevPoints = new TPoint[options.getPredictionLookback()];
		TTrack track = getTrack();
		if (stepNumber>0 && track!=null) {
			for (int j = 0; j<options.getPredictionLookback(); j++) {
				if (stepNumber-j-1 >= 0) {
					int n = control.stepToFrame(stepNumber-j-1);
					FrameData frame = getFrame(n);
					if (track.steps.isAutofill() && !frame.searched)
						prevPoints[j] = null;
					else {
						prevPoints[j] = frame.getMarkedPoint();
					}
				}
			}
		}

		// return null (no prediction) if there is no recent position data
		if (prevPoints[0]==null)
			return null;

		// set predictedTarget to prev position
		predictedTarget.setLocation(prevPoints[0].getX(), prevPoints[0].getY());
		if (!options.isLookAhead() || prevPoints[1]==null) {
			// no recent velocity or acceleration data available
			success = true;
		}

		if (!success) {
			// get derivatives
			double[][] veloc = getDerivatives(prevPoints, 1, options.getPredictionLookback());
			double[][] accel = getDerivatives(prevPoints, 2, options.getPredictionLookback());
			double[][] jerk = getDerivatives(prevPoints, 3, options.getPredictionLookback());

			double vxmax=0, vxmean=0, vymax=0, vymean=0;
			int n = 0;
			for (int i=0; i< veloc.length; i++) {
				if (veloc[i]!=null) {
					n++;
					vxmax = Math.max(vxmax, Math.abs(veloc[i][0]));
					vxmean += veloc[i][0];
					vymax = Math.max(vymax, Math.abs(veloc[i][1]));
					vymean += veloc[i][1];
				}
			}
			vxmean = Math.abs(vxmean/n);
			vymean = Math.abs(vymean/n);

			double axmax=0, axmean=0, aymax=0, aymean=0;
			n = 0;
			for (int i=0; i< accel.length; i++) {
				if (accel[i]!=null) {
					n++;
					axmax = Math.max(axmax, Math.abs(accel[i][0]));
					axmean += accel[i][0];
					aymax = Math.max(aymax, Math.abs(accel[i][1]));
					aymean += accel[i][1];
				}
			}
			axmean = Math.abs(axmean/n);
			aymean = Math.abs(aymean/n);

			double jxmax=0, jxmean=0, jymax=0, jymean=0;
			n = 0;
			for (int i=0; i< jerk.length; i++) {
				if (jerk[i]!=null) {
					n++;
					jxmax = Math.max(jxmax, Math.abs(jerk[i][0]));
					jxmean += jerk[i][0];
					jymax = Math.max(jymax, Math.abs(jerk[i][1]));
					jymean += jerk[i][1];
				}
			}
			jxmean = Math.abs(jxmean/n);
			jymean = Math.abs(jymean/n);

			boolean xVelocValid = prevPoints[2]==null || Math.abs(accel[0][0])<vxmean;
			boolean yVelocValid = prevPoints[2]==null || Math.abs(accel[0][1])<vymean;
			boolean xAccelValid = prevPoints[2]!=null && (prevPoints[3]==null || Math.abs(jerk[0][0])<axmean);
			boolean yAccelValid = prevPoints[2]!=null && (prevPoints[3]==null || Math.abs(jerk[0][1])<aymean);
//			boolean velocValid = prevPoints[2]==null || (accel[0][0]<vxmean && accel[0][1]<vymean);
//			boolean accelValid = prevPoints[2]!=null && (prevPoints[3]==null || (jerk[0][0]<axmean && jerk[0][1]<aymean));

			if (xAccelValid) {
				// base x-coordinate prediction on acceleration
				TPoint loc0 = prevPoints[2];
				TPoint loc1 = prevPoints[1];
				TPoint loc2 = prevPoints[0];
				double x = 3*loc2.getX() - 3*loc1.getX() + loc0.getX();
				predictedTarget.setLocation(x, predictedTarget.y);
				success = true;
			}
			else if (xVelocValid) {
				// else base x-coordinate prediction on velocity
				TPoint loc0 = prevPoints[1];
				TPoint loc1 = prevPoints[0];
				double x = 2*loc1.getX() -loc0.getX();
				predictedTarget.setLocation(x, predictedTarget.y);
				success = true;
			}
			if (yAccelValid) {
				// base y-coordinate prediction on acceleration
				TPoint loc0 = prevPoints[2];
				TPoint loc1 = prevPoints[1];
				TPoint loc2 = prevPoints[0];
				double y = 3*loc2.getY() - 3*loc1.getY() + loc0.getY();
				predictedTarget.setLocation(predictedTarget.x, y);
				success = true;
			}
			else if (yVelocValid) {
				// else base y-coordinate prediction on velocity
				TPoint loc0 = prevPoints[1];
				TPoint loc1 = prevPoints[0];
				double y = 2*loc1.getY() -loc0.getY();
				predictedTarget.setLocation(predictedTarget.x, y);
				success = true;
			}
//			if (accelValid) {
//				// base prediction on acceleration
//				TPoint loc0 = prevPoints[2];
//				TPoint loc1 = prevPoints[1];
//				TPoint loc2 = prevPoints[0];
//				double x = 3*loc2.getX() - 3*loc1.getX() + loc0.getX();
//				double y = 3*loc2.getY() - 3*loc1.getY() + loc0.getY();
//	  		predictedTarget.setLocation(x, y);
//	    	success = true;
//			}
//			else if (velocValid) {
//				// else base prediction on velocity
//				TPoint loc0 = prevPoints[1];
//				TPoint loc1 = prevPoints[0];
//				double x = 2*loc1.getX() -loc0.getX();
//				double y = 2*loc1.getY() -loc0.getY();
//	  		predictedTarget.setLocation(x, y);
//	    	success = true;
//			}
		}
		if (success) {
			// make sure prediction is within the video image
			BufferedImage image = control.getImage();
			int w = image.getWidth();
			int h = image.getHeight();
			predictedTarget.x = Math.max(predictedTarget.x, 0);
			predictedTarget.x = Math.min(predictedTarget.x, w);
			predictedTarget.y = Math.max(predictedTarget.y, 0);
			predictedTarget.y = Math.min(predictedTarget.y, h);
			return predictedTarget;
		}
		return null;
	}

	public boolean[] getDeletableSummary(int n){
		TTrack track = getTrack();
		boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
		boolean hasThis = false;
		boolean isKeyFrame = getFrame(n).isKeyFrame();

		// count steps and look for this and later points/matches
		int stepCount = 0;
		boolean hasLater = false;
		if (isAlwaysMarked) {
			Map<Integer, FrameData> frameData = getFrameData();
			for (Integer i: frameData.keySet()) {
				FrameData frame = frameData.get(i);
				if (frame.trackPoint==null) continue;
				hasLater = hasLater || i>n;
				hasThis = hasThis || i==n;
				stepCount++;
			}
		}
		else {
			hasThis = track.getStep(n)!=null;
			Step[] steps = track.getSteps();
			for (int i = 0; i< steps.length; i++) {
				if (steps[i]!=null) {
					hasLater = hasLater || i>n;
					stepCount++;
				}
			}
		}
		return new boolean[]{
				isAlwaysMarked,
				isKeyFrame,
				hasThis,
				hasLater,
				stepCount>0 && !(stepCount==1 && hasThis)
		};
	}


	public void deleteLater(int n){
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		// TODO: to core!
		Map<Integer, FrameData> frameData = getFrameData();
		for (int i: frameData.keySet()) {
			if (i<=n) continue;
			FrameData frame = frameData.get(i);
			frame.clear();
			toRemove.add(i);
		}
		for (int i: toRemove) {
			frameData.remove(i);
		}
		TTrack track = getTrack();
		boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
		if (!isAlwaysMarked) {
			Step[] steps = track.getSteps();
			for (int i = n+1; i < steps.length; i++) {
				steps[i] = null;
			}
		}
		track.dataValid = false;
		track.firePropertyChange("data", null, track); //$NON-NLS-1$

	}

	public void deleteFrame(int n){
		Map<Integer, FrameData> frameData = getFrameData();
		FrameData frame = frameData.get(n);
		if (!frame.isKeyFrame()) {
			frameData.get(n).clear();
			frameData.remove(n);
		} else {
			frame.clear();
		}

		TTrack track = getTrack();
		boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
		if (!isAlwaysMarked && track.getSteps().length>n)
			track.getSteps()[n] = null;
		track.dataValid = false;
		track.firePropertyChange("data", null, track); //$NON-NLS-1$
	}

	/**
	 * @return previous keyFrame, if any
	 */
	public KeyFrame deleteKeyFrame(int n){
		KeyFrame keyFrame = getFrame(n).getKeyFrame();
		Map<Integer, FrameData> frameData = getFrameData();
		int nextKey = -1; // later key frame, if any

		// if this is first key frame, look for later one
		for (Integer i: frameData.keySet()) {
			FrameData frame = frameData.get(i);
			if (frame.isKeyFrame()) { // found first key frame
				if (frame==keyFrame) {
					// we are deleting the first key frame, so find the next, then confirm with user
					for (int j: frameData.keySet()) {
						if (j>i) {
							FrameData next = frameData.get(j);
							if (next.isKeyFrame()) {
								nextKey = j;
								break;
							}
						}
					}
					break;
				}
			}
		}

		// replace keyframe with non-key frame
		FrameData newFrame = new FrameData(keyFrame);
		frameData.put(n, newFrame);

		// get earlier keyframe, if any
		keyFrame = getFrame(n).getKeyFrame();
		if (keyFrame==null) { // no earlier key frame, so clear all matches up to nextKey
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			for (int i: frameData.keySet()) {
				if (nextKey>-1 && i>=nextKey) break;
				FrameData frame = frameData.get(i);
				frame.clear();
				toRemove.add(i);
			}
			for (int i: toRemove) {
				frameData.remove(i);
			}
		}

		TTrack track = getTrack();
		if (track.getStep(n)==null) {
			FrameData frame = getFrame(n);
			if (frame!=null) {
				frame.setTemplateIcon(null);
				frame.setSearchPoints(null);
			}
			for (int i: frameData.keySet()) {
				if (i<=n) continue;
				frame = frameData.get(i);
				if (!frame.isKeyFrame() && track.getStep(i)==null)
					frame.clear();
			}
		}
		return keyFrame;
	}

	public ArrayList<Integer> listKeyFrames(){
		ArrayList<Integer> keyFrames = new ArrayList<Integer>();
		Map<Integer, FrameData> frameData = getFrameData();
		for (Integer i : frameData.keySet()) {
			FrameData frame = frameData.get(i);
			if (frame.isKeyFrame())
				keyFrames.add(i);
		}
		return keyFrames;
	}

	boolean isDeletable(int n){
		FrameData frame = getFrame(n);
		TTrack track = getTrack();
		KeyFrame keyFrame = frame.getKeyFrame();

		boolean deleteButtonEnabled = track!=null;
		if (deleteButtonEnabled) {
			boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
			if (isAlwaysMarked) {
				boolean hasFrameData = false;
				Map<Integer, FrameData> frameData = getFrameData();
				for (Integer i: frameData.keySet()) {
					FrameData next = frameData.get(i);
					if (next.trackPoint!=null) {
						hasFrameData = true;
						break;
					}
				}
				deleteButtonEnabled = hasFrameData || frame==keyFrame;
			}
			else {
				deleteButtonEnabled = frame==keyFrame || !track.isEmpty();
			}
		}
		return deleteButtonEnabled;
	}


	/**
	 * Determines the status code for a given frame. The status codes are:
	 * 0: a key frame
	 * 1: automarked with a good match
	 * 2: possible match, not marked
	 * 3: searched but no match found
	 * 4: unable to search--search area outside image or x-axis
	 * 5: manually marked by the user
	 * 6: match accepted by the user
	 * 7: never searched
	 * 8: possible match but previously marked
	 * 9: no match found but previously marked
	 * 10: calibration tool possible match
	 *
	 * @param n the frame number
	 * @return the status code
	 */
	protected int getStatusCode(int n) {
		FrameData frame = getFrame(n);
		if (frame.isKeyFrame()) return 0; // key frame
		double[] widthAndHeight = frame.getMatchWidthAndHeight();
		if (frame.isMarked()) { // frame is marked (includes always-marked tracks like axes, calibration points, etc)
			if (frame.isAutoMarked()) { // automarked
				return options.isMatchGood(widthAndHeight[1]) ?
						1 : // automarked with good match
						6; // accepted by user
			}
			// not automarked
			TTrack track = getTrack();
			boolean isCalibrationTool = track instanceof CoordAxes
					|| track instanceof OffsetOrigin
					|| track instanceof Calibration;
			if (track instanceof TapeMeasure) {
				TapeMeasure tape = (TapeMeasure) track;
				isCalibrationTool = !tape.isReadOnly();
			}
			if (frame.searched) {
				if (isCalibrationTool) {
					return options.isMatchPossible(widthAndHeight[1]) ?
							8 : // possible match for calibration
							9; // no match found, existing mark or calibration
				}
				if (frame.decided)
					return 5; // manually marked by user
				return options.isMatchPossible(widthAndHeight[1]) ?
						8 : // possible match, already marked
						9; // no match found, existing mark or calibration
			}
			return 7; // never searched
		}
		if (frame.searched) { // frame unmarked but searched
			return options.isMatchPossible(widthAndHeight[1]) ?
					2 : // possible match found but not marked
					3; // no match found
		}
		// frame is unmarked and unsearched
		if (widthAndHeight == null) return 7; // never searched
		return 4; // tried but unable to search
	}






	// indexFrameData maps point index to frameData
	protected Map<Integer, Map<Integer, FrameData>> getIndexFrameData() {
		TTrack track = getTrack();
		Map<Integer, Map<Integer, FrameData>> indexFrameData  = trackFrameData.get(track);
		if (indexFrameData==null) {
			indexFrameData = new TreeMap<Integer, Map<Integer, FrameData>>();
			trackFrameData.put(track, indexFrameData);
		}
		return indexFrameData;
	}

	// frameData maps frame number to individual FrameData objects
	protected Map<Integer, FrameData> getFrameData(int index) {
		Map<Integer, FrameData> frameData = getIndexFrameData().get(index);
		if (frameData==null) {
			frameData = new TreeMap<Integer, FrameData>();
			getIndexFrameData().put(index, frameData);
		}
		return frameData;
	}

	protected Map<Integer, FrameData> getFrameData() {
		TTrack track = getTrack();
		int index = track==null? 0: track.getTargetIndex();
		return getFrameData(index);
	}

	protected FrameData getFrame(int frameNumber) {
		FrameData frame = getFrameData().get(frameNumber);
		if (frame==null) {
			TTrack track = getTrack();
			int index = track==null? 0: track.getTargetIndex();
			frame = new FrameData(index, frameNumber);
			getFrameData().put(frameNumber, frame);
		}
		return frame;
	}

	public int getIndex(TPoint p) {
		int n = control.getFrameNumber(p);
		TTrack track = getTrack();
		Step step = track.getStep(n); // non-null if marked
		if (step!=null) {
			for (int i=0; i< step.points.length; i++) {
				if (p.equals(step.points[i])) {
					return i;
				}
			}
		}
		return -1;
	}


	/**
	 * A class to hold frame data.
	 */
	protected class FrameData {

		private int index, frameNum, templateAlpha, matcherHashCode;
		private double[] targetOffset = {0, 0};
		private double[] matchWidthAndHeight;
		private TPoint[] matchPoints;
		private TPoint[] searchPoints;
		TPoint trackPoint;
		private double[] autoMarkLoc;
		private BufferedImage template;
		private Icon templateIcon; // shows template used for search
		private Icon matchIcon; // only if match is found
		boolean searched; // true when searched
		boolean decided; // true when accepted, skipped or marked point is dragged; assumed false for calibration tools and axes
		int[] workingPixels;

		FrameData(int pointIndex, int frameNumber) {
			index = pointIndex;
			frameNum = frameNumber;
		}

		FrameData(KeyFrame keyFrame) {
			index = keyFrame.getIndex();
			frameNum = keyFrame.getFrameNumber();
			matchWidthAndHeight = keyFrame.getMatchWidthAndHeight();
			matchPoints = keyFrame.getMatchPoints();
			searchPoints = keyFrame.getSearchPoints(false);
			targetOffset = keyFrame.getTargetOffset();
			matchIcon = keyFrame.getMatchIcon();
			templateIcon = keyFrame.getTemplateIcon();
			autoMarkLoc = keyFrame.getAutoMarkLoc();
			trackPoint = keyFrame.trackPoint;
			searched = keyFrame.searched;
		}

		int getFrameNumber() {
			return frameNum;
		}


		protected ImageIcon createMagnifiedIcon(BufferedImage source) {
			return new ImageIcon(
					BufferedImageUtils.createMagnifiedImage(
							source,
							templateIconMagnification,
							BufferedImage.TYPE_INT_ARGB
					)
			);
		}

		Icon getTemplateIcon() {
			return templateIcon;
		}

		void setTemplateImage(BufferedImage image) {
			setTemplateIcon(createMagnifiedIcon(image));
		}

		void setTemplateIcon(Icon icon) {
			templateIcon = icon;
		}

		Icon getMatchIcon() {
			return matchIcon;
		}

		void setMatchImage(BufferedImage image) {
			setMatchIcon(createMagnifiedIcon(image));
		}

		void setMatchIcon(Icon icon) {
			matchIcon = icon;
		}
		/**
		 * Sets the template to the current template of a TemplateMatcher.
		 *
		 * @param matcher the template matcher
		 */
		void setTemplate(TemplateMatcher matcher) {
			template = matcher.getTemplate();
			templateAlpha = matcher.getAlphas()[0];
			workingPixels = matcher.getWorkingPixels(workingPixels);
			matcherHashCode = matcher.hashCode();

			// refresh icons
			setMatchIcon(null);
			setTemplateImage(template);
		}

		/**
		 * Returns the template to match. Replaces the existing template if
		 * a new one exists.
		 */
		BufferedImage getTemplateToMatch() {
			if (template==null || newTemplateExists()) {
				// replace current template with new one
				setTemplate(getTemplateMatcher());
			}
			return template;
		}

		/**
		 * Returns true if the evolved template is both different and appropriate.
		 */
		boolean newTemplateExists() {
			if (isKeyFrame())
				return false;
			TemplateMatcher matcher = getTemplateMatcher();
			if (matcher == null)
				return false;
			boolean different = matcher.getAlphas()[0] != templateAlpha
					|| matcher.hashCode() != matcherHashCode;
			// TODO: reverse ?
			boolean appropriate = matcher.getIndex() < frameNum;
			return different && appropriate;
		}

		/**
		 * Returns the previously matched template.
		 */
		BufferedImage getTemplate() {
			return template;
		}

		/**
		 * Returns the working pixels used to generate the current template.
		 */
		int[] getWorkingPixels() {
			return workingPixels;
		}

		TemplateMatcher getTemplateMatcher() {
			KeyFrame frame = getKeyFrame();
			return frame==null? null: frame.matcher;
		}

		void setTargetOffset(double dx, double dy) {
			targetOffset = new double[] {dx, dy};
		}

		double[] getTargetOffset() {
			if (this.isKeyFrame())
				return targetOffset;
			return getKeyFrame().getTargetOffset();
		}

		void setSearchPoints(TPoint[] points) {
			searchPoints = points;
		}

		TPoint[] getSearchPoints(boolean inherit) {
			if (!inherit || searchPoints != null || this.isKeyFrame()) return searchPoints;
			Map<Integer, FrameData> frames = getFrameData(index);
			//TODO: reverse?
			for (int i = frameNum; i >= 0; i--) {
				FrameData frame = frames.get(i);
				if (frame != null) {
					if (frame.searchPoints != null || frame.isKeyFrame()) {
						return frame.searchPoints;
					}
				}
			}
			return null;
		}

		void setMatchPoints(TPoint[] points) {
			matchPoints = points;
		}

		TPoint[] getMatchPoints() {
			return matchPoints;
		}

		void setMatchWidthAndHeight(double[] matchData) {
			matchWidthAndHeight = matchData;
		}

		double[] getMatchWidthAndHeight() {
			return matchWidthAndHeight;
		}

		KeyFrame getKeyFrame() {
			if (this.isKeyFrame()) return (KeyFrame) this;
			Map<Integer, FrameData> frames = getFrameData(index);
			if (!control.isReverse()) {
				for (int i = frameNum; i >= 0; i--) {
					FrameData frame = frames.get(i);
					if (frame != null && frame.isKeyFrame())
						return (KeyFrame) frame;
				}
			} else {
				int fin = control.getFrameCount();
				for (int i = frameNum; i < fin; i++) {
					FrameData frame = frames.get(i);
					if (frame != null && frame.isKeyFrame())
						return (KeyFrame) frame;
				}
			}
			return null;
		}

		int getIndex() {
			return index;
		}

		boolean isMarked() {
			TTrack track = getTrack();
			return track!=null && track.getStep(frameNum)!=null;
		}

		//TODO: to be splitted and overridden?
		boolean isAutoMarked() {
			if (autoMarkLoc==null || trackPoint==null) return false;
			if (trackPoint instanceof CoordAxes.AnglePoint) {
				ImageCoordSystem coords = control.getCoords();
				double theta = coords.getAngle(frameNum);
				CoordAxes.AnglePoint p = (CoordAxes.AnglePoint)trackPoint;
				return Math.abs(theta-p.getAngle())<0.001;
			}
			// return false if trackPoint has moved from marked location by more than 0.01 pixels
			return Math.abs(autoMarkLoc[0]-trackPoint.getX())<0.01
					&& Math.abs(autoMarkLoc[1]-trackPoint.getY())<0.01;
		}

		void setAutoMarkPoint(TPoint point) {
			trackPoint = point;
			autoMarkLoc = point==null? null: new double[] {point.getX(), point.getY()};
		}

		double[] getAutoMarkLoc() {
			return autoMarkLoc;
		}

		boolean isKeyFrame() {
			return false;
		}

		TPoint getMarkedPoint() {
			if (!isMarked()) return null;
			if (trackPoint!=null) return trackPoint;
			TTrack track = getTrack();
			return track.getMarkedPoint(frameNum, index);
		}

		void clear() {
			matchPoints = null;
			matchWidthAndHeight = null;
			matchIcon = null;
			autoMarkLoc = null;
			searched = false;
			decided = false;
			trackPoint = null;
			workingPixels = null;
			matcherHashCode = 0;
			if (!isKeyFrame()) {
				searchPoints = null;
				templateIcon = null;
				templateAlpha = 0;
				template = null;
			}
		}
	}

	/**
	 * A class to hold keyframe data.
	 */
	protected class KeyFrame extends FrameData {

		private Shape mask;
		private TPoint target;
		private TPoint[] maskPoints = {new TPoint(), new TPoint()};
		private TemplateMatcher matcher;

		KeyFrame(TPoint keyPt, Shape mask, TPoint target, int index, TPoint center, TPoint corner) {
			super(index, control.getFrameNumber(keyPt));
			this.mask = mask;
			this.target = target;
			// TODO: calculate corner using mask and center
			maskPoints[0].setLocation(center);
			maskPoints[1].setLocation(corner);
		}

		boolean isKeyFrame() {
			return true;
		}

		Shape getMask() {
			return mask;
		}

		TPoint getTarget() {
			return target;
		}

		TPoint[] getMaskPoints() {
			return maskPoints;
		}

		void setTemplateMatcher(TemplateMatcher matcher) {
			this.matcher = matcher;
		}

		boolean isFirstKeyFrame() {
			Map<Integer, FrameData> frames = getFrameData(getIndex());
			for (int i=getFrameNumber()-1; i>=0; i--) {
				FrameData frame = frames.get(i);
				if (frame!=null && frame.isKeyFrame())
					return false;
			}
			return true;
		}

	}



}
