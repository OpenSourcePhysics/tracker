package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.TemplateMatcher;
import org.opensourcephysics.media.core.Video;

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
		Shape mask = new Ellipse2D.Double();
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

		Icon getTemplateIcon() {
			return templateIcon;
		}

		void setTemplateIcon(Icon icon) {
			templateIcon = icon;
		}

		Icon getMatchIcon() {
			return matchIcon;
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

			feedback.onTemplateSetForFrame(this, template);
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
