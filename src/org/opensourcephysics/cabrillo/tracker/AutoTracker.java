/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;

import org.opensourcephysics.cabrillo.tracker.AutoTrackerCore.KeyFrame;
import org.opensourcephysics.cabrillo.tracker.AutoTrackerCore.FrameData;


/**
 * A class to automatically track a feature of interest in a video.
 * This uses a TemplateMatcher to find a match to the feature in each frame
 * and, if found, marks the active track at the target location.
 *
 * @author Douglas Brown
 */
public class AutoTracker implements Trackable, PropertyChangeListener {

	// static fields
	private static Rectangle hitRect = new Rectangle(-4, -4, 8, 8);
	private static TPoint hitPt = new TPoint();
	private static Shape selectionShape;
	private static AffineTransform transform = new AffineTransform();
	private static Footprint target_footprint
			= PointShapeFootprint.getFootprint("Footprint.BoldCrosshair"); //$NON-NLS-1$
	private static Footprint inactive_target_footprint
			= PointShapeFootprint.getFootprint("Footprint.Crosshair"); //$NON-NLS-1$
	private static Footprint corner_footprint
			= PointShapeFootprint.getFootprint("Footprint.SolidSquare"); //$NON-NLS-1$
	private static final float[] DOTTED_LINE = new float[]{2, 2};
	private static final float[] DASHED_LINE = new float[]{2, 8};
	private static NumberFormat format = NumberFormat.getNumberInstance();
	private static double cornerFactor = 0.9;
	private static BasicStroke solidBold = new BasicStroke(2), solid = new BasicStroke();
	private static BasicStroke dotted, dashed;
	private static int defaultEvolveRate = AutoTrackerOptions.maxEvolveRate / 5;
	private static Icon searchIcon, stopIcon, graySearchIcon;
	private static double[] defaultSearchSize = {40, 40};
	static boolean neverPause = true; // TODO: to core, make not static

	static {
		//TODO: can it be moved to initialization of static members?
		dotted = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, DOTTED_LINE, 0);
		dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, DASHED_LINE, 0);
		selectionShape = solidBold.createStrokedShape(hitRect);
		format.setMinimumIntegerDigits(1);
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(1);
		String path = "/org/opensourcephysics/cabrillo/tracker/resources/images/green_light.gif";  //$NON-NLS-1$
		searchIcon = ResourceLoader.getIcon(path);
		path = "/org/opensourcephysics/cabrillo/tracker/resources/images/red_light.gif";  //$NON-NLS-1$
		stopIcon = ResourceLoader.getIcon(path);
		path = "/org/opensourcephysics/cabrillo/tracker/resources/images/gray_light.gif";  //$NON-NLS-1$
		graySearchIcon = ResourceLoader.getIcon(path);
	}

	// instance fields
	private TrackerPanel trackerPanel;
	private Wizard wizard;
	private double minMaskRadius = 4;
	private Handle maskHandle = new Handle();
	private Corner maskCorner = new Corner();
	private TPoint maskCenter = new TPoint();
	private Handle searchHandle = new Handle();
	private Corner searchCorner = new Corner();
	private TPoint searchCenter = new TPoint();
	private Rectangle2D searchRect2D = new Rectangle2D.Double();
	private Shape searchShape, maskShape, matchShape;
	private Shape searchHitShape, maskHitShape;
	private Mark mark; // draws the mask, target and/or search area
	private Point[] screenPoints = {new Point()}; // used for footprints
	private boolean maskVisible, targetVisible, searchVisible;
	private Runnable stepper;
	private boolean stepping, active, paused, marking;
	private int autoskipsRemained = 0;
	private boolean isInteracting;

	private AutoTrackerControl control = new TrackerPanelControl();
	private AutoTrackerFeedback feedback = new TrackerPanelFeedback();
	public AutoTrackerCore core = new AutoTrackerCore(control, feedback);

	private AutoTrackerOptions options = null;

	/**
	 * Constructs an AutoTracker for a specified TrackerPanel.
	 *
	 * @param panel the TrackerPanel
	 */
	public AutoTracker(TrackerPanel panel) {
		trackerPanel = panel;
		trackerPanel.addDrawable(this);
		trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("clear", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("video", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$

		options = core.options; // TODO: do we need this?

		options.changes.addPropertyChangeListener("maskWidth", propertyChangeEvent -> refreshCurrentMask());
		options.changes.addPropertyChangeListener("maskHeight", propertyChangeEvent -> refreshCurrentMask());
		options.changes.addPropertyChangeListener("maskShapeType", propertyChangeEvent ->
				refreshKeyFrame(core.getFrame(control.getFrameNumber()).getKeyFrame()));


		stepper = new Runnable() {
			public void run() {
				TTrack track = getTrack();
				if (!active || track == null) {
					return;
				}
				// if never pausing, don't look ahead
				boolean moveSearchArea = !neverPause;
				if (markCurrentFrame(moveSearchArea) || neverPause) {
					// successfully found/marked a good match
					if (!control.canStep()) { // reached the end
						stop(true, true);
						return;
					}
					if (stepping) { // move to the next step
						wizard.refreshInfo();
						repaint();
						control.step();
						return;
					}
					// not stepping, so stop
					stop(true, true);
				} else { // failed to find or mark a match, so pause or stop
					if (!stepping)
						stop(true, false);
					else {
						paused = true;
						if (track instanceof PointMass) {
							PointMass pointMass = (PointMass) track;
							pointMass.updateDerivatives();
						}
						track.firePropertyChange("steps", null, null); //$NON-NLS-1$
						wizard.refreshGUI();
					}
				}
				repaint();
			}
		};
		wizard = new Wizard();
	}


	/**
	 * For external purposes
	 * TODO: should be redundant
	 *
	 * @return current track
	 */
	public TTrack getTrack() {
		return core.getTrack();
	}

	public FrameData getFrame(int n) {
		return core.getFrame(n);
	}

	/**
	 * Sets the track to mark when matches are found.
	 *
	 * @param newTrack the track
	 */
	protected void setTrack(TTrack newTrack) {
		if (newTrack != null && !newTrack.isAutoTrackable())
			newTrack = null;
		TTrack track = getTrack();
		if (track == newTrack)
			return;
		feedback.onTrackUnbind(track);
		track = newTrack;
		if (track != null) {
			core.trackID = track.getID();
			feedback.setSelectedTrack(track);
			feedback.onTrackBind(track);
			int n = control.getFrameNumber();
			FrameData frame = getFrame(n);
			TPoint[] searchPts = frame.getSearchPoints(true);
			if (searchPts != null)
				setSearchPoints(searchPts[0], searchPts[1]);
		} else {
			core.trackID = -1;
		}
		feedback.onSetTrack();
	}

	/**
	 * Starts the search process.
	 *
	 * @param startWithThis true to search the current frame
	 * @param keepGoing     true to continue stepping after the first search
	 */
	protected void search(boolean startWithThis, boolean keepGoing) {
		stepping = stepping || keepGoing;
		wizard.changed = false;
		active = true; // actively searching
		paused = false;
		if (!startWithThis || markCurrentFrame(false) || neverPause) {
			if (control.canStep() && (!startWithThis || stepping)) {
				control.step();
				return;
			}
			if (startWithThis && !stepping) { // mark this frame only
				active = false;
			}
			// reached end frame, so stop
			else {
				stop(true, true);
			}
		} else {
			// tried to mark this frame and failed
			paused = true;
		}
		getWizard().refreshGUI();
		getWizard().helpButton.requestFocusInWindow();
		repaint();
	}

	/**
	 * Stops the search process.
	 *
	 * @param now    true to stop now
	 * @param update true to update derivatives
	 */
	protected void stop(boolean now, boolean update) {
		stepping = false; // don't keep stepping
		active = !now && !paused;
		paused = false;
		wizard.prepareForFixedSearch(false);
		wizard.refreshGUI();
		if (update) {
			TTrack track = getTrack();
			if (track instanceof PointMass) {
				PointMass pointMass = (PointMass) track;
				pointMass.updateDerivatives();
			}
			track.firePropertyChange("steps", null, null); //$NON-NLS-1$
		}
	}

	/**
	 * Called when a position has been marked
	 *
	 * @param n Frame number
	 * @param p Marked point
	 * @return
	 */
	public boolean onMarked(int n, TPoint p) {
		TTrack track = getTrack();
		if (track == null) return false;
		track.autoTrackerMarking = track.isAutoAdvance();
		p = track.autoMarkAt(n, p.x, p.y);
		getFrame(n).setAutoMarkPoint(p);
		track.autoTrackerMarking = false;
		return true;
	}

	/**
	 * Called when a frame has been skipped
	 *
	 * @param n Frame number
	 * @return
	 */
	public boolean onSkipped(int n) {
		getTrack().skippedStepWarningSuppress = true;
		return true;
	}

	/**
	 * @param n Frame number
	 * @return true if current frame should not be marked
	 */
	public boolean isStepComplete(int n) {
		return getTrack().isStepComplete(n);
	}

	public boolean prepareMarking(int frameNumber) {
		TTrack track = getTrack();
		if (track == null) return false;
		feedback.setSelectedTrack(track);
		return true;
	}

	/**
	 * Marks a new step in the current frame if a match is found.
	 *
	 * @param predictLoc true to use look-ahead prediction for setting the search loc
	 * @return true if a new step was marked or skipped automatically
	 */
	public boolean markCurrentFrame(boolean predictLoc) {
		int n = control.getFrameNumber();
		if (!prepareMarking(n)) {
			return false;
		}
		FrameData frame = getFrame(n);
		KeyFrame keyFrame = frame.getKeyFrame();
		if (keyFrame != null && !isStepComplete(n)) {
			TPoint p = findMatchTarget(predictLoc);
			double[] peakWidthAndHeight = frame.getMatchWidthAndHeight();
			if (p != null
					&& (Double.isInfinite(peakWidthAndHeight[1])
					|| options.isMatchGood(peakWidthAndHeight[1]))
			) {
				marking = true;

				boolean result = onMarked(n, p);

				// We can perform autoskips if needed
				autoskipsRemained = options.getAutoskipCount();
				return result;
			}
			if (p == null) {
				if (autoskipsRemained > 0) {
					autoskipsRemained--;
					return onSkipped(n);
				}
				frame.setMatchIcon(null);
			}
		}
		return false;
	}

	/**
	 * Finds the match target, if any. Also saves search center and corner.
	 *
	 * @param predict true to predict the location before searching
	 * @return the match target, or null if no match is found
	 */
	public TPoint findMatchTarget(boolean predict) {
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		// if predicting, move searchRect to predicted location
		if (predict) {
			TPoint prediction = core.getPredictedMatchTarget(n);
			if (prediction != null) {
				TPoint p = core.getMatchCenter(prediction);
				setSearchPoints(p, null);
			}
		}
		// save search center and corner points
		TPoint[] pts = new TPoint[]{new TPoint(searchCenter), new TPoint(searchCorner)};
		frame.setSearchPoints(pts);
		return findMatchTarget(getSearchRect());
	}

	public boolean isMarked(int frameNumber) {
		TTrack track = getTrack();
		return track != null && track.getStep(frameNumber) != null;
	}


	/**
	 * Gets the wizard.
	 *
	 * @return the wizard
	 */
	public Wizard getWizard() {
		return wizard;
	}

	/**
	 * Draws this object.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param g     the graphics context on which to draw
	 */
	public void draw(DrawingPanel panel, Graphics g) {
		// don't draw this unless wizard is visible and a video exists
		TTrack track = getTrack();
		if (track == null || wizard == null
				|| !wizard.isVisible() || trackerPanel.getVideo() == null) {
			maskVisible = targetVisible = searchVisible = false;
			return;
		}
		if (wizard != null && wizard.isVisible()
				&& !maskVisible && !targetVisible && !searchVisible) {
			wizard.refreshGUI();
			maskVisible = targetVisible = searchVisible = true;
		}
		Graphics2D g2 = (Graphics2D) g;
		if (getMark() != null) {
			mark.draw(g2, false);
		}
	}

	/**
	 * Finds the TPoint, if any, located at a specified pixel position.
	 * May return null.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position
	 * @param ypix  the y pixel position
	 * @return the TPoint, or null if none
	 */
	public Interactive findInteractive(
			DrawingPanel panel, int xpix, int ypix) {
		isInteracting = false;
		int n = trackerPanel.getFrameNumber();
		KeyFrame keyFrame = getFrame(n).getKeyFrame();
		if (keyFrame == null || !wizard.isVisible() || trackerPanel.getVideo() == null) {
			return null;
		}
		hitRect.setLocation(xpix - hitRect.width / 2, ypix - hitRect.height / 2);
		if (targetVisible) {
			TPoint target = keyFrame.getTarget();
			if (hitRect.contains(target.getScreenPosition(trackerPanel))) {
				isInteracting = true;
				return target;
			}
		}
		hitPt.setLocation(xpix, ypix);
		if (searchVisible) {
			if (hitRect.contains(searchCorner.getScreenPosition(trackerPanel))) {
				return searchCorner;
			}
			if (searchHitShape.intersects(hitRect)) {
				return searchHandle;
			}
		}
		if (maskVisible) {
			if (hitRect.contains(maskCorner.getScreenPosition(trackerPanel))) {
				return maskCorner;
			}
			if (maskHitShape.intersects(hitRect)) {
				return maskHandle;
			}
		}
		return null;
	}

	/**
	 * Determines if this autotracker is in active use
	 *
	 * @return true if active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Determines if this autotracker is interacting (via the mouse)
	 *
	 * @return true if interacting
	 */
	public boolean isInteracting() {
		return isInteracting;
	}

	/**
	 * Determines if this autotracker is interacting with a specified track
	 *
	 * @param track the track
	 * @return true if interacting
	 */
	public boolean isInteracting(TTrack track) {
		if (getTrack() == track) {
			int n = track.trackerPanel.getFrameNumber();
			FrameData frame = getFrame(n);
			return frame != null && frame == frame.getKeyFrame() && isInteracting();
		}
		return false;
	}

	/**
	 * Gets the search rectangle.
	 *
	 * @return the search rectangle
	 */
	public Rectangle getSearchRect() {
		return searchRect2D.getBounds();
	}

	/**
	 * Refreshes the search rectangle based on the current center and corner points.
	 */
	public void refreshSearchRect() {
		// set searchRect according to current search center and corner
		searchRect2D.setFrameFromCenter(searchCenter, searchCorner);
		// move searchRect into the video image if needed
		if (BufferedImageUtils.moveRectIntoImage(searchRect2D, control.getImage())) { // true if moved
			// set search center and corner locations to reflect new searchRect
			searchCenter.setLocation(searchRect2D.getCenterX(), searchRect2D.getCenterY());
			searchCorner.setLocation(searchRect2D.getMaxX(), searchRect2D.getMaxY());
		}

		// save the search points in the current frame
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		TPoint[] pts = new TPoint[]{new TPoint(searchCenter), new TPoint(searchCorner)};
		frame.setSearchPoints(pts);
		repaint();
	}

	/**
	 * Sets the position of the center and corner of the search rectangle.
	 * If the corner is null, the search rectangle is moved but not resized,
	 * and the entire rectangle is kept within the image.
	 *
	 * @param center the desired center position
	 * @param corner the desired corner position (may be null)
	 */
	protected void setSearchPoints(TPoint center, TPoint corner) {
		if (corner == null) {
			// make sure search rectangle is within the video image
			BufferedImage image = control.getImage();
			int w = image.getWidth();
			int h = image.getHeight();
			int setbackX = searchRect2D.getBounds().width / 2;
			int setbackY = searchRect2D.getBounds().height / 2;
			center.x = Math.max(center.x, setbackX);
			center.x = Math.min(center.x, w - setbackX);
			center.y = Math.max(center.y, setbackY);
			center.y = Math.min(center.y, h - setbackY);
			// move rectangle to new center
			double dx = center.x - searchCenter.x;
			double dy = center.y - searchCenter.y;
			searchCenter.x += dx;
			searchCenter.y += dy;
			searchCorner.x += dx;
			searchCorner.y += dy;
		} else {
			searchCenter.setLocation(center);
			searchCorner.setLocation(corner);
		}
		refreshSearchRect();
	}

	/**
	 * Responds to property change events. AutoTracker listens for the following
	 * events: "step" from tracks, "selectedpoint", "selectedtrack", "video" and "stepnumber"
	 * from TrackerPanel.
	 *
	 * @param e the property change event
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		TTrack track = getTrack();
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		KeyFrame keyFrame = frame.getKeyFrame();

		if (name.equals("selectedpoint")) { //$NON-NLS-1$
			boolean needsRepaint = false;
			TPoint prev = (TPoint) e.getOldValue();
			if (wizard.isVisible()) {
				if (prev instanceof Corner && keyFrame != null) {
					needsRepaint = true;
					// restore corner positions
					Shape mask = keyFrame.getMask();
					if (mask instanceof RectangularShape) {
						RectangularShape circle = (RectangularShape) mask;
						maskCorner.x = maskCenter.x + circle.getWidth() / (2 * cornerFactor);
						maskCorner.y = maskCenter.y + circle.getHeight() / (2 * cornerFactor);
					}
					searchCorner.x = searchRect2D.getMaxX();
					searchCorner.y = searchRect2D.getMaxY();
				} else if (prev instanceof Handle || prev instanceof Target) {
					needsRepaint = true;
				}
			}
			Step step = trackerPanel.getSelectedStep();
			TPoint next = (TPoint) e.getNewValue();
			if (next == maskHandle || next == maskCorner
					|| next == searchHandle || next == searchCorner
					|| (keyFrame != null && next == keyFrame.getTarget())) {
				trackerPanel.setSelectedTrack(track);
				needsRepaint = true;
			} else if (next != null && step != null && step.getTrack() == track) {
				int i = step.getPointIndex(next);
				if (i > -1 && i != track.getTargetIndex()) {
					track.setTargetIndex(i);

					// get frame for new index and reposition search points and mask
					frame = getFrame(n);
					TPoint[] searchPts = frame.getSearchPoints(true);
					if (searchPts != null)
						setSearchPoints(searchPts[0], searchPts[1]);

					keyFrame = frame.getKeyFrame();
					if (keyFrame != null) {
						maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
						maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
					}

					wizard.refreshGUI();
					needsRepaint = true;
				}
			}
			if (needsRepaint) repaint();
		} else if (name.equals("selectedtrack") && wizard != null) { //$NON-NLS-1$
			wizard.refreshGUI();
		} else if (name.equals("track") && e.getOldValue() != null) { //$NON-NLS-1$
			// track has been deleted
			TTrack deletedTrack = (TTrack) e.getOldValue();
			// TODO: move the trackFrameData operations to core
			core.trackFrameData.remove(deletedTrack);
			if (deletedTrack == track) {
				setTrack(null);
			}
		} else if (name.equals("clear")) { //$NON-NLS-1$
			// tracks have been cleared
			core.trackFrameData.clear();
			setTrack(null);
		}

		if (wizard == null || !wizard.isVisible()) return;

		if (name.equals("video") || name.equals("name") //$NON-NLS-1$ //$NON-NLS-2$
				|| name.equals("color") || name.equals("footprint")) { //$NON-NLS-1$ //$NON-NLS-2$
			wizard.refreshGUI();
		} else if (track == null && name.equals("stepnumber")) { //$NON-NLS-1$
			wizard.refreshGUI();
		}

		if (track == null || trackerPanel.getVideo() == null) {
			return;
		}
		if (name.equals("step") && wizard.isVisible()) { //$NON-NLS-1$
			if (!marking) { // not marked by this autotracker
				n = ((Integer) e.getNewValue()).intValue();
				frame = getFrame(n);
				frame.decided = true; // point dragged by user?
				if (track.getStep(n) == null) { // step was deleted
					frame.clear();
				} else if (!frame.isKeyFrame()) { // step was marked or moved
					frame.setMatchIcon(null);
					paused = false;
				}
			}
			wizard.refreshGUI();
			marking = false;
		} else if (name.equals("stepnumber")) { //$NON-NLS-1$
			TPoint[] searchPts = frame.getSearchPoints(true);
			if (searchPts != null)
				setSearchPoints(searchPts[0], searchPts[1]);
			else if (options.isLookAhead() && keyFrame != null) {
				TPoint prediction = core.getPredictedMatchTarget(n);
				if (prediction != null) {
					setSearchPoints(core.getMatchCenter(prediction), null);
					// save search center and corner points
					TPoint[] pts = new TPoint[]{new TPoint(searchCenter), new TPoint(searchCorner)};
					frame.setSearchPoints(pts);
				} else {
					repaint();
				}
			}
			if (active && !paused) { // actively tracking
				if (SwingUtilities.isEventDispatchThread())
					stepper.run();
				else
					SwingUtilities.invokeLater(stepper);
			} else if (stepping) { // user set the frame number, so stop stepping
				stop(true, false);
			} else wizard.refreshGUI();
		}
	}

//_______________________________ protected methods _________________________

	/**
	 * Finds the target for the best match found within the specified
	 * searchRect. Also saves match width, height, center and corner.
	 *
	 * @param searchRect the search rectangle
	 * @return the target, or null if no match found
	 */
	protected TPoint findMatchTarget(Rectangle searchRect) {
		if (!control.isVideoValid()) return null;
		TemplateMatcher matcher = core.getTemplateMatcher();
		if (matcher == null) return null;
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		frame.decided = false; // default

		// set template to be matched
		matcher.setTemplate(frame.getTemplateToMatch());

		// get location, width and height of match
		TPoint p = null;
		BufferedImage image = control.getImage();
		if (options.getLineSpread() >= 0) {
			double theta = control.getCoords().getAngle(n);
			double x0 = control.getCoords().getOriginX(n);
			double y0 = control.getCoords().getOriginY(n);
			p = matcher.getMatchLocation(image, searchRect, x0, y0, theta, options.getLineSpread()); // may be null
		} else {
			p = matcher.getMatchLocation(image, searchRect); // may be null
		}
		double[] matchWidthAndHeight = matcher.getMatchWidthAndHeight();
		if (!options.isMatchGood(matchWidthAndHeight[1]) && frame.isAutoMarked()) {
			frame.trackPoint = null;
		}

		// save match data and searched frames
		frame.setMatchWidthAndHeight(matchWidthAndHeight);
		frame.searched = true;
		// if p is null or match is poor, then clear match points
		if (p == null || !options.isMatchPossible(matchWidthAndHeight[1])) {
			frame.setMatchPoints(null);
			return null;
		}

		// successfully found good or possible match: save match data
		frame.setMatchImage(matcher.getMatchImage());
		Rectangle rect = frame.getKeyFrame().getMask().getBounds();
		TPoint center = new TPoint(p.x + maskCenter.x - rect.getX(), p.y + maskCenter.y - rect.getY());
		TPoint corner = new TPoint(
				center.x + options.getMaskWidth() / 2,
				center.y + options.getMaskHeight() / 2
		);
		frame.setMatchPoints(new TPoint[]{center, corner, p});

		// if good match found then build evolved template and return match target
		if (options.isMatchGood(matchWidthAndHeight[1])) {
			core.buildEvolvedTemplate(frame);
			return core.getMatchTarget(center);
		}

		return null;
	}

	/**
	 * Refreshes current position of the mask corner
	 * according to current position of mask center
	 * and the information from AutoTrackerOptions object.
	 */
	private void refreshCurrentMask() {
		maskCorner.setXY(
				maskCenter.x + options.getMaskWidth() / (2 * cornerFactor),
				maskCenter.y + options.getMaskHeight() / (2 * cornerFactor)
		);
	}


	/**
	 * Erases the current mark.
	 */
	protected void erase() {
		if (mark != null)
			trackerPanel.addDirtyRegion(mark.getBounds(false)); // old bounds
		mark = null;
	}

	/**
	 * Repaints this object.
	 */
	protected void repaint() {
		erase();
		if (getMark() != null)
			trackerPanel.addDirtyRegion(mark.getBounds(false)); // new bounds
		trackerPanel.repaintDirtyRegion();
	}

	/**
	 * Disposes of this autotracker.
	 */
	protected void dispose() {
		trackerPanel.removeDrawable(this);
		trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
		setTrack(null);
		core.trackFrameData.clear();
		wizard.dispose();
		trackerPanel.autoTracker = null;
		trackerPanel = null;
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Gets the drawing mark.
	 *
	 * @return the mark
	 */
	protected Mark getMark() {
		int n = control.getFrameNumber();
		FrameData frame = getFrame(n);
		KeyFrame keyFrame = frame.getKeyFrame();
		final TTrack track = getTrack();
		if (track == null || keyFrame == null) return null;
		if (mark == null) {
			int k = core.getStatusCode(n);
			// refresh target icon on wizard label
			Color c = track.getFootprint().getColor();
			target_footprint.setColor(c);
			inactive_target_footprint.setColor(c);
			corner_footprint.setColor(c);
			// define marks for center, corners, target and selection
			Mark searchCornerMark = null, maskCornerMark = null,
					targetMark = null, selectionMark = null;
			// set up transform
			AffineTransform toScreen = trackerPanel.getPixelTransform();
			if (!trackerPanel.isDrawingInImageSpace()) {
				toScreen.concatenate(trackerPanel.getCoords().getToWorldTransform(n));
			}
			// get selected point and define the corresponding screen point
			final TPoint selection = trackerPanel.getSelectedPoint();
			Point selectionPt = null;
			// refresh search and mask draw and hit shapes
			try {
				searchShape = toScreen.createTransformedShape(searchRect2D);
				searchHitShape = solid.createStrokedShape(searchShape);
				maskShape = toScreen.createTransformedShape(keyFrame.getMask());
				maskHitShape = solid.createStrokedShape(maskShape);
			} catch (Exception e) {
				return null;
			}
			// check to see if a handle is selected
			if (selection == maskHandle)
				selectionPt = maskVisible ? maskHandle.getScreenPosition(trackerPanel) : null;
			else if (selection == searchHandle)
				selectionPt = searchVisible ? searchHandle.getScreenPosition(trackerPanel) : null;
			// create mask corner mark
			if (frame.isKeyFrame()) {
				maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
				maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
			}
			screenPoints[0] = maskCorner.getScreenPosition(trackerPanel);
			if (selection == maskCorner) {
				selectionPt = maskVisible ? screenPoints[0] : null;
			} else {
				maskCornerMark = corner_footprint.getMark(screenPoints);
			}
			// create search corner mark
			screenPoints[0] = searchCorner.getScreenPosition(trackerPanel);
			if (selection == searchCorner) {
				selectionPt = searchVisible ? screenPoints[0] : null;
			} else {
				searchCornerMark = corner_footprint.getMark(screenPoints);
			}
			// create target mark
			screenPoints[0] = keyFrame.getTarget().getScreenPosition(trackerPanel);
			if (selection == keyFrame.getTarget())
				selectionPt = targetVisible ? screenPoints[0] : null;
			else {
				targetMark = target_footprint.getMark(screenPoints);
			}
			// if a match has been found, create match shapes
			TPoint[] matchPts = frame.getMatchPoints();
			if (matchPts == null || frame.isKeyFrame() || k == 5)
				matchShape = null;
			else {
				Point p1 = matchPts[0].getScreenPosition(trackerPanel);
				Point p2 = maskCenter.getScreenPosition(trackerPanel);
				transform.setToTranslation(p1.x - p2.x, p1.y - p2.y);
				matchShape = toScreen.createTransformedShape(getMatchShape(matchPts, frame));
				screenPoints[0] = core.getMatchTarget(matchPts[0]).getScreenPosition(trackerPanel);
//      	matchTargetMark = inactive_target_footprint.getMark(screenPoints);
			}
			// if anything is selected, create a selection mark
			if (selectionPt != null) {
				transform.setToTranslation(selectionPt.x, selectionPt.y);
				final Shape selectedShape
						= transform.createTransformedShape(selectionShape);
				selectionMark = new Mark() {
					public void draw(Graphics2D g, boolean highlighted) {
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
						g.fill(selectedShape);
					}

					public Rectangle getBounds(boolean highlighted) {
						return selectedShape.getBounds();
					}
				};
			}
			// create final mark
			final Mark markMaskCorner = maskCornerMark;
			final Mark markSearchCorner = searchCornerMark;
			final Mark markTarget = targetMark;
			final Mark markSelection = selectionMark;
			mark = new Mark() {
				public void draw(Graphics2D g, boolean highlighted) {
					Paint gpaint = g.getPaint();
					Color c = track.getFootprint().getColor();
					g.setPaint(c);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					BasicStroke stroke = (BasicStroke) g.getStroke();
					int n = control.getFrameNumber();
					FrameData frame = getFrame(n);
					boolean isKeyFrame = frame != null && frame.isKeyFrame();
					if (targetVisible) {
						// draw the target
						if (isKeyFrame) {
							if (markTarget != null)
								markTarget.draw(g, false);
						}
					}
					if (matchShape != null && !isKeyFrame) {
						g.setStroke(dotted);
						g.draw(matchShape);
					}
					if (maskVisible && isKeyFrame) {
						g.setStroke(stroke);
						g.draw(maskShape);
						if (markMaskCorner != null)
							markMaskCorner.draw(g, false);
					}
					if (searchVisible || !isKeyFrame) {
						// draw the searchRect
						g.setStroke(dashed);
						g.draw(searchShape);
						if (markSearchCorner != null)
							markSearchCorner.draw(g, false);
					}
					// draw the selected point
					if (markSelection != null) markSelection.draw(g, false);
					g.setStroke(stroke);
					g.setPaint(gpaint);
				}

				public Rectangle getBounds(boolean highlighted) {
					Rectangle bounds = searchShape.getBounds();
					if (markMaskCorner != null) bounds.add(markMaskCorner.getBounds(highlighted));
					if (markSearchCorner != null) bounds.add(markSearchCorner.getBounds(highlighted));
					if (markTarget != null) bounds.add(markTarget.getBounds(highlighted));
					if (markSelection != null) bounds.add(markSelection.getBounds(highlighted));
					if (maskVisible) bounds.add(maskShape.getBounds());
					if (matchShape != null) {
						bounds.add(matchShape.getBounds());
//        		bounds.add(matchTargetMark.getBounds(highlighted));
					}
					return bounds;
				}
			};
		}
		return mark;
	}


	/**
	 * Deletes the match data at a specified frame number.
	 *
	 * @param n the frame number
	 */
	protected void delete(int n) {
		trackerPanel.repaint();
		FrameData frame = getFrame(n);
		frame.clear();
	}

	/**
	 * Clears all existing steps and match data for the current point index.
	 */
	protected void reset() {
		mark = null;
		// clear all frames and identify the key frame
		Map<Integer, FrameData> frameData = core.getFrameData();
		KeyFrame keyFrame = null;
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		for (int i : frameData.keySet()) {
			FrameData frame = frameData.get(i);
			frame.clear();
			if (keyFrame == null && frame.isKeyFrame())
				keyFrame = (KeyFrame) frame;
			toRemove.add(i);
		}
		for (int i : toRemove) {
			frameData.remove(i);
		}
		// delete all steps unless always marked
		TTrack track = getTrack();
		boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
		if (!isAlwaysMarked) {
			for (int n = 0; n < track.getSteps().length; n++) {
				track.steps.setStep(n, null);
			}
		}
		stop(true, true);
		// set the step number to key frame
		if (keyFrame != null) {
			int n = keyFrame.getFrameNumber();
			VideoPlayer player = trackerPanel.getPlayer();
			player.setStepNumber(player.getVideoClip().frameToStep(n));
		}
		repaint();
	}


	// GUI ?

	/**
	 * Refreshes the key frame to reflect current center and corner positions.
	 *
	 * @param keyFrame the KeyFrame
	 */
	protected void refreshKeyFrame(KeyFrame keyFrame) {
		Shape mask = keyFrame.getMask();
		if (mask instanceof RectangularShape) {
			// prevent the mask from being too small to contain any pixels
			keyFrame.getMaskPoints()[0].setLocation(maskCenter);
			keyFrame.getMaskPoints()[1].setLocation(maskCorner);
			RectangularShape ellipse = (RectangularShape) mask;
			double sin = maskCenter.sin(maskCorner);
			double cos = maskCenter.cos(maskCorner);
			if (Double.isNaN(sin)) {
				sin = -0.707;
				cos = 0.707;
			}
			double d = Math.max(minMaskRadius, maskCenter.distance(maskCorner));
			double dx = d * cornerFactor * cos;
			double dy = -d * cornerFactor * sin;
			if (Math.abs(dx) < 1) {
				if (dx > 0) dx = 1;
				else dx = -1;
			}
			if (Math.abs(dy) < 1) {
				if (dy > 0) dy = 1;
				else dy = -1;
			}
			ellipse.setFrameFromCenter(maskCenter.x, maskCenter.y,
					maskCenter.x + dx, maskCenter.y + dy);
		}
		//options.setMaskWidth (2*(maskCorner.x- maskCenter.x));
		//options.setMaskHeight(2*(maskCorner.y- maskCenter.y));
		wizard.replaceIcons(keyFrame);
		// get the marked point and set target position AFTER refreshing keyFrame
		TPoint p = keyFrame.getMarkedPoint();
		if (p != null)
			keyFrame.getTarget().setXY(p.getX(), p.getY());
		search(true, false); // search this frame only
		repaint();
		wizard.repaint();
	}


	// GUI

	/**
	 * Gets the match shape for the specified center and frame corner positions.
	 *
	 * @param pts   TPoint[] {center, frame corner}
	 * @param frame
	 * @return a shape suitable for drawing
	 */
	protected Shape getMatchShape(TPoint[] pts, FrameData frame) {
		Shape match = frame.getKeyFrame().getMask();
		if (match instanceof RectangularShape) {
			RectangularShape ellipse = (RectangularShape) ((RectangularShape) match).clone();
			ellipse.setFrameFromCenter(pts[0], pts[1]);
			return ellipse;
		}
		return null;
	}

	// GUI
	protected boolean isDrawingKeyFrameFor(TTrack track, int index) {
		int n = control.getFrameNumber();
		if (getTrack() == track && wizard.isVisible() && getFrame(n).isKeyFrame()) {
			FrameData frame = getFrame(n);
			return frame.getIndex() == index;
		}
		return false;
	}

//____________________ inner TPoint classes ______________________

	/**
	 * An edge point used for translation.
	 */
	protected class Handle extends TPoint {

		/**
		 * Overrides TPoint setXY method.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public void setXY(double x, double y) {
			double dx = x - getX();
			double dy = y - getY();
			super.setXY(x, y);
			if (this == searchHandle) {
				searchCenter.x += dx;
				searchCenter.y += dy;
				searchCorner.x += dx;
				searchCorner.y += dy;
				refreshSearchRect();
				wizard.setChanged();
			} else {
				maskCenter.x += dx;
				maskCenter.y += dy;
				maskCorner.x += dx;
				maskCorner.y += dy;
				int n = trackerPanel.getFrameNumber();
				KeyFrame keyFrame = getFrame(n).getKeyFrame();
				keyFrame.getMaskPoints()[0].setLocation(maskCenter);
				keyFrame.getMaskPoints()[1].setLocation(maskCorner);
				TPoint target = keyFrame.getTarget();
				keyFrame.setTargetOffset(target.x - maskCenter.x, target.y - maskCenter.y);
				refreshKeyFrame(keyFrame);
			}
			core.clearSearchPointsDownstream();
		}

		/**
		 * Sets the location of this point to the specified screen position.
		 *
		 * @param x        the x screen position
		 * @param y        the y screen position
		 * @param vidPanel the trackerPanel doing the drawing
		 */
		public void setScreenLocation(int x, int y, VideoPanel vidPanel) {
			if (screenPt == null) screenPt = new Point();
			if (worldPt == null) worldPt = new Point2D.Double();
			screenPt.setLocation(x, y);
			AffineTransform toScreen = vidPanel.getPixelTransform();
			try {
				toScreen.inverseTransform(screenPt, worldPt);
			} catch (NoninvertibleTransformException ex) {
				ex.printStackTrace();
			}
			setLocation(worldPt);
			repaint();
		}
	}

	/**
	 * A corner point used for resizing.
	 */
	protected class Corner extends TPoint {

		/**
		 * Overrides TPoint setXY method.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public void setXY(double x, double y) {
			super.setXY(x, y);
			if (this == searchCorner) {
				refreshSearchRect();
				wizard.setChanged();
			} else { // this == maskCorner
				options.setMaskWidth((maskCorner.x - maskCenter.x) * (2 * cornerFactor));
				options.setMaskHeight((maskCorner.y - maskCenter.y) * (2 * cornerFactor));
				int n = trackerPanel.getFrameNumber();
				refreshKeyFrame(getFrame(n).getKeyFrame());
			}
			core.clearSearchPointsDownstream();
		}
	}

	/**
	 * A point that defines the target location relative to the mask center.
	 * Tracks are "marked" at this point when auto-tracking.
	 */
	protected class Target extends TPoint {

		/**
		 * Overrides TPoint setXY method.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public void setXY(double x, double y) {
			super.setXY(x, y);
			int n = trackerPanel.getFrameNumber();
			FrameData frame = getFrame(n);
			KeyFrame keyFrame = frame.getKeyFrame();
			keyFrame.setTargetOffset(x - maskCenter.x, y - maskCenter.y);
			TTrack track = getTrack();
			track.autoTrackerMarking = track.isAutoAdvance();
			TPoint p = track.autoMarkAt(n, getX(), getY());
			frame.setAutoMarkPoint(p);
			track.autoTrackerMarking = false;
			repaint();
			track.repaint();
		}
	}

	/**
	 * A wizard to guide users of AutoTracker.
	 */
	protected class Wizard extends JDialog
			implements PropertyChangeListener {

		// instance fields
		private JButton startButton, searchNextButton, searchThisButton;
		private JPopupMenu popup;
		private JButton closeButton, helpButton, deleteButton, keyFrameButton;
		private JButton acceptButton, skipButton;
		private TallSpinner evolveSpinner, acceptSpinner;
		private JComboBox trackDropdown, pointDropdown;
		private boolean isVisible, changed, hidePopup;
		private JTextArea textPane;
		protected JToolBar templateToolbar, geometryToolbar, searchToolbar, targetToolbar, imageToolbar, trackToolbar, autoskipToolbar;
		private JPanel startPanel, followupPanel, infoPanel, northPanel, targetPanel;
		private JLabel templateImageLabel, matchImageLabel, acceptLabel, templateLabel;
		private JLabel frameLabel, evolveRateLabel, searchLabel, targetLabel;
		private JLabel pointLabel, trackLabel;
		protected Dimension textPaneSize;
		private JCheckBox lookAheadCheckbox, oneDCheckbox, rectShapeCheckbox;
		private Object mouseOverObj;
		private MouseAdapter mouseOverListener;
		private Timer timer;
		private boolean ignoreChanges, isPrevValid, prevLookAhead, prevOneD;
		private int prevEvolution;

		private JLabel autoskipLabel;
		private TallSpinner autoskipSpinner;

		private JLabel templateWidthLabel, templateHeightLabel;
		private TallSpinner templateWidthSpinner, templateHeightSpinner;

		/**
		 * Constructs a Wizard.
		 */
		public Wizard() {
			super(trackerPanel.getTFrame(), false);
			createGUI();
			pack();
			locateOnFrame();
		}

		private void locateOnFrame() {
			// place near top right corner of frame
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			TFrame frame = trackerPanel.getTFrame();
			Point frameLoc = frame.getLocationOnScreen();
			int w = getWidth() + 8;
			int x = Math.min(screen.width - w, frameLoc.x + frame.getWidth() - w);
			int y = trackerPanel.getLocationOnScreen().y;
			setLocation(x, y);
		}


		/**
		 * Responds to property change events. This listens for "tab" from TFrame.
		 *
		 * @param e the property change event
		 */
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
				if (trackerPanel != null && e.getNewValue() == trackerPanel) {
					setVisible(isVisible);
				} else {
					boolean vis = isVisible;
					setVisible(false);
					isVisible = vis;
				}
			}
		}

		/**
		 * Sets the changed flag
		 */
		public void setChanged() {
			if (!changed) {
				changed = true;
				refreshGUI();
			}
		}


		/**
		 * Overrides JDialog setVisible method.
		 *
		 * @param vis true to show this inspector
		 */
		public void setVisible(boolean vis) {
			super.setVisible(vis);
			TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
			toolbar.autotrackerButton.setSelected(vis);
			isVisible = vis;
			if (!vis) {
				erase();
				trackerPanel.repaintDirtyRegion();
			} else {
				TTrack track = trackerPanel.getSelectedTrack();
				if (track != null) setTrack(track);
			}
			refreshGUI();
		}

		/**
		 * Sets the font level.
		 *
		 * @param level the desired font level
		 */
		public void setFontLevel(int level) {
			FontSizer.setFonts(this, FontSizer.getLevel());
			Object[] buttons = new Object[]{acceptButton, skipButton};
			FontSizer.setFonts(buttons, FontSizer.getLevel());
			//  	private JComboBox trackDropdown, pointDropdown;
			JComboBox[] dropdowns = new JComboBox[]{trackDropdown, pointDropdown};
			for (JComboBox next : dropdowns) {
				int n = next.getSelectedIndex();
				Object[] items = new Object[next.getItemCount()];
				for (int i = 0; i < items.length; i++) {
					items[i] = next.getItemAt(i);
				}
				DefaultComboBoxModel model = new DefaultComboBoxModel(items);
				next.setModel(model);
				next.setSelectedItem(n);
			}
			refreshStrings(); // also resets label sizes
			pack();

		}

		@Override
		public void dispose() {
			trackerPanel.getTFrame().removePropertyChangeListener("tab", this); //$NON-NLS-1$
			timer.stop();
			timer = null;
			super.dispose();
		}

		//_____________________________ protected methods ____________________________


		/**
		 * Creates the visible components.
		 */
		protected void createGUI() {
			TFrame frame = trackerPanel.getTFrame();
			if (frame != null) {
				frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
			}
			//setResizable(false);
			KeyListener kl = new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (!trackerPanel.getPlayer().isEnabled()) return;
					switch (e.getKeyCode()) {
						case KeyEvent.VK_PAGE_UP:
							if (e.isShiftDown()) {
								int n = trackerPanel.getPlayer().getStepNumber() - 5;
								trackerPanel.getPlayer().setStepNumber(n);
							} else trackerPanel.getPlayer().back();
							break;
						case KeyEvent.VK_PAGE_DOWN:
							if (e.isShiftDown()) {
								int n = trackerPanel.getPlayer().getStepNumber() + 5;
								trackerPanel.getPlayer().setStepNumber(n);
							} else trackerPanel.getPlayer().step();
							break;
						case KeyEvent.VK_HOME:
							trackerPanel.getPlayer().setStepNumber(0);
							break;
						case KeyEvent.VK_END:
							VideoClip clip = trackerPanel.getPlayer().getVideoClip();
							trackerPanel.getPlayer().setStepNumber(clip.getStepCount() - 1);
							break;
						case KeyEvent.VK_SHIFT:
							if (!stepping) {
								startButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Options")); //$NON-NLS-1$);
							}
							break;
					}
				}

				public void keyReleased(KeyEvent e) {
					// handle shift key release when wizard takes focus from TrackerPanel
					if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						trackerPanel.isShiftKeyDown = false;
						startButton.setText(stepping ?
								TrackerRes.getString("AutoTracker.Wizard.Button.Stop") : //$NON-NLS-1$
								TrackerRes.getString("AutoTracker.Wizard.Button.Search")); //$NON-NLS-1$);
					}
				}
			};

			int delay = 500; // 1/2 second delay for mouseover action
			timer = new Timer(delay, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refreshInfo();
					refreshDrawingFlags();
					erase();
					trackerPanel.repaint();
				}
			});
			timer.setInitialDelay(delay);

			mouseOverListener = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					Component c = (Component) e.getSource();
					while (c.getParent() != null) {
						if (c == templateToolbar
								|| c == searchToolbar
								|| c == targetToolbar
								|| c == imageToolbar) {
							mouseOverObj = c;
							isInteracting = c == targetToolbar;
							isInteracting = true;
							break;
						}
						c = c.getParent();
					}
					if (mouseOverObj == null) {
						// refresh immediately
						refreshInfo();
						refreshDrawingFlags();
						erase();
						trackerPanel.repaint();
					} else {
						// restart timer to refresh
						timer.restart();
					}
				}

				public void mouseExited(MouseEvent e) {
					// restart timer to refresh
					timer.restart();
					mouseOverObj = null;
					isInteracting = false;
				}
			};
			addWindowFocusListener(new java.awt.event.WindowAdapter() {
				public void windowGainedFocus(java.awt.event.WindowEvent e) {
					TTrack track = getTrack();
					if (track != null) trackerPanel.setSelectedTrack(track);
				}
			});
			JPanel contentPane = new JPanel(new BorderLayout());
			setContentPane(contentPane);

			// create trackDropdown early since need it for spinners
			trackDropdown = new JComboBox() {
				public Dimension getPreferredSize() {
					Dimension dim = super.getPreferredSize();
					dim.height -= 1;
					return dim;
				}
			};
			trackDropdown.addMouseListener(mouseOverListener);
			for (int i = 0; i < trackDropdown.getComponentCount(); i++) {
				trackDropdown.getComponent(i).addMouseListener(mouseOverListener);
			}
			trackDropdown.setRenderer(new TrackRenderer());
			trackDropdown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ("refresh".equals(trackDropdown.getName())) return; //$NON-NLS-1$
					Object[] item = (Object[]) trackDropdown.getSelectedItem();
					if (item != null) {
						for (TTrack next : trackerPanel.getTracks()) {
							if (item[1].equals(next.getName())) {
								stop(true, false);
								setTrack(next);
								refreshGUI();
							}
						}
					}
				}
			});

			// create start panel
			startPanel = new JPanel();
			startButton = new JButton();
			startButton.setDisabledIcon(graySearchIcon);
			final ActionListener searchAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					hidePopup = false;
					if (stepping) {
						stop(false, false); // stop after the next search
					} else search(true, true); // search this frame and keep stepping
				}
			};

			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (hidePopup) {
						popup.setVisible(false);
						hidePopup = false;
						return;
					}
					// set "neverPause" flag
					neverPause = (e.getModifiers() & 0x01) == 1; // shift key down
					if (neverPause && !stepping) {
						// show popup menu
						if (popup == null) {
							popup = new JPopupMenu();
							JMenuItem item = new JMenuItem(TrackerRes.getString("AutoTracker.Wizard.Menuitem.SearchFixed"));  //$NON-NLS-1$
							item.setToolTipText(TrackerRes.getString("AutoTracker.Wizard.MenuItem.SearchFixed.Tooltip")); //$NON-NLS-1$
							item.addActionListener(searchAction);
							item.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseEntered(MouseEvent e) {
									prepareForFixedSearch(true);
								}

								@Override
								public void mouseExited(MouseEvent e) {
									prepareForFixedSearch(false);
								}
							});
							popup.add(item);
							popup.addSeparator();
							item = new JMenuItem(TrackerRes.getString("AutoTracker.Wizard.Menuitem.CopyMatchScores"));  //$NON-NLS-1$
							item.setToolTipText(TrackerRes.getString("AutoTracker.Wizard.MenuItem.CopyMatchScores.Tooltip")); //$NON-NLS-1$
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									hidePopup = false;
									// get match score data string
									String matchScore = getMatchDataString();
									// copy to the clipboard
									Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
									StringSelection stringSelection = new StringSelection(matchScore);
									clipboard.setContents(stringSelection, stringSelection);
								}
							});
							popup.add(item);
						}
						hidePopup = true;
						FontSizer.setFonts(popup, FontSizer.getLevel());
						popup.show(startButton, 0, startButton.getHeight());
					} else {
						searchAction.actionPerformed(e);
					}
				}
			});
			startButton.addKeyListener(kl);
			startButton.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					startButton.setText(e.isShiftDown() && !stepping ?
							TrackerRes.getString("AutoTracker.Wizard.Button.Options") : //$NON-NLS-1$
							stepping ?
									TrackerRes.getString("AutoTracker.Wizard.Button.Stop") : //$NON-NLS-1$
									TrackerRes.getString("AutoTracker.Wizard.Button.Search")); //$NON-NLS-1$);
				}
			});
			startPanel.add(startButton);
			searchThisButton = new JButton();
			searchThisButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					neverPause = (e.getModifiers() > 16);
					search(true, false); // search this frame and stop
				}
			});
			searchThisButton.addKeyListener(kl);
			startPanel.add(searchThisButton);
			searchNextButton = new JButton();
			searchNextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					neverPause = (e.getModifiers() > 16);
					search(false, false); // search next frame and stop
				}
			});
			searchNextButton.addKeyListener(kl);
			startPanel.add(searchNextButton);

			// create followup panel
			followupPanel = new JPanel();
			followupPanel.setBorder(BorderFactory.createEmptyBorder());
			followupPanel.setOpaque(false);

			// create template image toolbar
			imageToolbar = new JToolBar();
			imageToolbar.setFloatable(false);
			frameLabel = new JLabel();
			frameLabel.setOpaque(false);
			frameLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

			templateImageLabel = new JLabel();
			templateImageLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			templateImageLabel.setIconTextGap(3);
			templateImageLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			templateImageLabel.addMouseListener(mouseOverListener);

			matchImageLabel = new JLabel();
			matchImageLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			matchImageLabel.setIconTextGap(3);
			matchImageLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			matchImageLabel.addMouseListener(mouseOverListener);

			JPanel flowpanel = new JPanel();
			flowpanel.setOpaque(false);
			flowpanel.setBorder(BorderFactory.createEmptyBorder());
			flowpanel.add(templateImageLabel);
			flowpanel.add(matchImageLabel);
			imageToolbar.add(frameLabel);
			imageToolbar.add(flowpanel);
			imageToolbar.addMouseListener(mouseOverListener);

			// create template toolbar
			templateToolbar = new JToolBar();
			templateToolbar.setFloatable(false);
			templateToolbar.addMouseListener(mouseOverListener);
			templateLabel = new JLabel();
			templateLabel.setOpaque(false);
			templateLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			evolveRateLabel = new JLabel();
			evolveRateLabel.setOpaque(false);
			evolveRateLabel.addMouseListener(mouseOverListener);

			SpinnerModel model = new SpinnerNumberModel(defaultEvolveRate, 0, options.maxEvolveRate, options.maxEvolveRate / 20);
			evolveSpinner = new TallSpinner(model, trackDropdown);
			evolveSpinner.addMouseListenerToAll(mouseOverListener);
			evolveSpinner.getTextField().setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
				public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
					JFormattedTextField.AbstractFormatter formatter
							= new JFormattedTextField.AbstractFormatter() {
						public String valueToString(Object value) throws ParseException {
							return value.toString() + "%"; //$NON-NLS-1$
						}

						public Object stringToValue(String text) throws ParseException {
							return Integer.parseInt(text.substring(0, text.length() - 1));
						}
					};
					return formatter;
				}
			});

			ChangeListener listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (ignoreChanges) return;
					Integer i = (Integer) evolveSpinner.getValue();
					options.setEvolveAlphaFromRate(i);
					int n = trackerPanel.getFrameNumber();
					FrameData frame = getFrame(n);
					core.buildEvolvedTemplate(frame);
					if (frame.isKeyFrame())
						refreshKeyFrame((KeyFrame) frame);
					stop(true, false);
					setChanged();
				}
			};
			evolveSpinner.addChangeListener(listener);
			options.setEvolveAlphaFromRate((Integer) evolveSpinner.getValue()); // TODO: redundant?

			acceptLabel = new JLabel();
			acceptLabel.setOpaque(false);
			acceptLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			model = new SpinnerNumberModel(options.getGoodMatch(), options.getPossibleMatch(), 10, 1);
			acceptSpinner = new TallSpinner(model, trackDropdown);
			acceptSpinner.addMouseListenerToAll(mouseOverListener);
			listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					options.setGoodMatch((Integer) acceptSpinner.getValue()); // TODO: accept strings
					setChanged();
				}
			};
			acceptSpinner.addChangeListener(listener);

			templateWidthLabel = new JLabel("Width");
			templateWidthLabel.setOpaque(false);
			templateWidthLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			model = new SpinnerNumberModel(options.getMaskWidth(), 1, 1000, 1);
			templateWidthSpinner = new TallSpinner(model, trackDropdown);
			templateWidthSpinner.addMouseListenerToAll(mouseOverListener);
			listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					options.setMaskWidth((Double) templateWidthSpinner.getValue()); // TODO: accept strings
					setChanged();
				}
			};
			templateWidthSpinner.addChangeListener(listener);
			templateWidthSpinner.getTextField().setEnabled(true);
			options.changes.addPropertyChangeListener("maskWidth", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
					templateWidthSpinner.setValue(options.getMaskWidth());
				}
			});

			templateHeightLabel = new JLabel("Height");
			templateHeightLabel.setOpaque(false);
			templateHeightLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			model = new SpinnerNumberModel(options.getMaskHeight(), 1, 1000, 1);
			templateHeightSpinner = new TallSpinner(model, trackDropdown);
			templateHeightSpinner.addMouseListenerToAll(mouseOverListener);
			listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					options.setMaskHeight((Double) templateHeightSpinner.getValue()); // TODO: accept strings
					setChanged();
				}
			};
			templateHeightSpinner.addChangeListener(listener);
			templateHeightSpinner.getTextField().setEnabled(true);
			options.changes.addPropertyChangeListener("maskHeight", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
					templateHeightSpinner.setValue(options.getMaskHeight());
				}
			});

			rectShapeCheckbox = new JCheckBox("Rectangular");
			rectShapeCheckbox.addMouseListener(mouseOverListener);
			rectShapeCheckbox.setOpaque(false);
			rectShapeCheckbox.setSelected(options.getMaskShapeType() == 1);
			rectShapeCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					options.setMaskShapeType(rectShapeCheckbox.isSelected() ? 1 : 0);
					setChanged();
				}
			});
			geometryToolbar = new JToolBar();
			geometryToolbar.setFloatable(false);
			geometryToolbar.addMouseListener(mouseOverListener);

			JPanel geompanel = new JPanel();
			geometryToolbar.add(geompanel);
			geompanel.add(templateWidthLabel);
			geompanel.add(templateWidthSpinner);
			geompanel.add(templateHeightLabel);
			geompanel.add(templateHeightSpinner);
			geompanel.add(rectShapeCheckbox);

			autoskipLabel = new JLabel();
			autoskipLabel.setOpaque(false);
			autoskipLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			model = new SpinnerNumberModel(options.getAutoskipCount(), 0, 10, 1);
			autoskipSpinner = new TallSpinner(model, trackDropdown);
			autoskipSpinner.addMouseListenerToAll(mouseOverListener);
			listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					options.setAutoskipCount((Integer) autoskipSpinner.getValue());
					setChanged();
				}
			};
			autoskipSpinner.addChangeListener(listener);


			flowpanel = new JPanel();
			flowpanel.setOpaque(false);
			flowpanel.add(evolveRateLabel);
			flowpanel.add(evolveSpinner);
			flowpanel.add(acceptLabel);
			flowpanel.add(acceptSpinner);

			templateToolbar.add(templateLabel);
			templateToolbar.add(flowpanel);

			autoskipToolbar = new JToolBar();
			autoskipToolbar.setFloatable(false);
			autoskipToolbar.addMouseListener(mouseOverListener);
			flowpanel = new JPanel();
			flowpanel.setOpaque(false);
			flowpanel.add(autoskipLabel);
			flowpanel.add(autoskipSpinner);
			autoskipToolbar.add(flowpanel);


			// create search toolbar
			searchToolbar = new JToolBar();
			searchToolbar.setFloatable(false);
			searchToolbar.addMouseListener(mouseOverListener);
			searchLabel = new JLabel();
			searchLabel.setOpaque(false);
			searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			oneDCheckbox = new JCheckBox();
			oneDCheckbox.addMouseListener(mouseOverListener);
			oneDCheckbox.setOpaque(false);
			oneDCheckbox.setSelected(options.getLineSpread() >= 0);
			oneDCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					options.setLineSpread(oneDCheckbox.isSelected() ? 0 : -1);
					setChanged();
					if (oneDCheckbox.isSelected()) {
						int n = trackerPanel.getFrameNumber();
						CoordAxes axes = trackerPanel.getAxes();
						KeyFrame frame = getFrame(n).getKeyFrame();
						if (frame != null) {
							n = frame.getFrameNumber();
							TPoint[] maskPts = frame.getMaskPoints();
							axes.getOrigin().setXY(maskPts[0].x, maskPts[0].y);
						}
						axes.setVisible(true);
					}
					trackerPanel.repaint();
				}
			});
			lookAheadCheckbox = new JCheckBox();
			lookAheadCheckbox.addMouseListener(mouseOverListener);
			lookAheadCheckbox.setOpaque(false);
			lookAheadCheckbox.setSelected(options.isLookAhead());
			lookAheadCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					options.setLookAhead(lookAheadCheckbox.isSelected());
					setChanged();
				}
			});
			flowpanel = new JPanel();
			flowpanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
			flowpanel.setOpaque(false);
			flowpanel.add(oneDCheckbox);
			flowpanel.add(lookAheadCheckbox);
			searchToolbar.add(searchLabel);
			searchToolbar.add(flowpanel);

			// create target toolbar
			targetToolbar = new JToolBar();
			targetToolbar.setFloatable(false);
			targetToolbar.addMouseListener(mouseOverListener);
			targetLabel = new JLabel();
			targetLabel.setOpaque(false);
			targetLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

			trackLabel = new JLabel();
			trackLabel.setOpaque(false);

			pointLabel = new JLabel();
			pointLabel.setOpaque(false);
			pointLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

			pointDropdown = new JComboBox() {
				public Dimension getPreferredSize() {
					Dimension dim = super.getPreferredSize();
					dim.height = trackDropdown.getPreferredSize().height;
					return dim;
				}
			};
			pointDropdown.addMouseListener(mouseOverListener);
			for (int i = 0; i < pointDropdown.getComponentCount(); i++) {
				pointDropdown.getComponent(i).addMouseListener(mouseOverListener);
			}
			pointDropdown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ("refresh".equals(pointDropdown.getName())) return; //$NON-NLS-1$
					String item = (String) pointDropdown.getSelectedItem();
					if (item != null) {
						stop(true, false);
						TTrack track = getTrack();
						track.setTargetIndex(item);
						int n = trackerPanel.getFrameNumber();
						FrameData frame = getFrame(n);
						TPoint[] searchPts = frame.getSearchPoints(true);
						if (searchPts != null)
							setSearchPoints(searchPts[0], searchPts[1]);
						refreshGUI();
					}
				}
			});

			targetPanel = new JPanel();
			targetPanel.setOpaque(false);
			targetPanel.add(trackLabel);
			targetPanel.add(trackDropdown);
			targetPanel.add(pointLabel);
			targetPanel.add(pointDropdown);
			targetToolbar.add(targetLabel);
			targetToolbar.add(targetPanel);

			// create text area for hints
			textPane = new JTextArea();
			textPane.setEditable(false);
			textPane.setLineWrap(true);
			textPane.setWrapStyleWord(true);
			textPane.setBorder(BorderFactory.createEmptyBorder());
			textPane.setForeground(Color.blue);
			textPane.addKeyListener(kl);
			textPane.addMouseListener(mouseOverListener);

			// create buttons
			closeButton = new JButton();
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop(true, true); // stop after the next search
					setVisible(false);
				}
			});
			closeButton.addKeyListener(kl);

			helpButton = new JButton();
			helpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					trackerPanel.getTFrame().showHelp("autotracker", 0); //$NON-NLS-1$
				}
			});
			helpButton.addKeyListener(kl);

			acceptButton = new JButton();
			acceptButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					marking = true;
					int n = control.getFrameNumber();
					core.forceAccept(n);
					if (stepping && control.canStep()) {
						paused = false;
						control.step();
					} else {
						stop(true, true);
					}
				}
			});
			acceptButton.addKeyListener(kl);

			skipButton = new JButton();
			skipButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// set decided flag
					int n = trackerPanel.getFrameNumber();
					FrameData frame = getFrame(n);
					frame.decided = true;
					// eliminate match icon?
					//frame.setMatchIcon(null);
					// step to the next frame if possible
					if (control.canStep()) {
						paused = false;
						control.step();
					} else {
						stop(true, false);
					}
				}
			});
			skipButton.addKeyListener(kl);

			final Action deleteKeyFrameAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					KeyFrame keyFrame = core.deleteKeyFrame(control.getFrameNumber());

					if (keyFrame != null) { // earlier keyframe exists
						maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
						maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
					}

					refreshGUI();
					AutoTracker.this.repaint();
					trackerPanel.repaint();
				}
			};

			final Action deleteThisAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					// clear this match and step
					int n = trackerPanel.getFrameNumber();
					core.deleteFrame(n);
					refreshGUI();
					AutoTracker.this.repaint();
				}
			};

			final Action deleteLaterAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					// clear later matches and steps
					int n = trackerPanel.getFrameNumber();
					core.deleteLater(n);
					refreshGUI();
					AutoTracker.this.repaint();
				}
			};

			final Action deleteAllAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					// clears all matches and steps
					reset();
				}
			};

			deleteButton = new JButton();
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// first determine what can be deleted
					int n = trackerPanel.getFrameNumber();
					boolean[] summary = core.getDeletableSummary(n);
					boolean
							isAlwaysMarked = summary[0],
							isKeyFrame = summary[1],
							hasThis = summary[2],
							hasLater = summary[3],
							hasNotOnlyThis = summary[4];

					// now build the popup menu with suitable delete items
					JPopupMenu popup = new JPopupMenu();
					if (isKeyFrame) {
						JMenuItem item = new JMenuItem(TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThisKeyFrame"));  //$NON-NLS-1$
						popup.add(item);
						item.addActionListener(deleteKeyFrameAction);
					}
					if (hasThis) {
						JMenuItem item = new JMenuItem(isAlwaysMarked ?
								TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThisMatch") : //$NON-NLS-1$
								TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThis")); //$NON-NLS-1$
						popup.add(item);
						item.addActionListener(deleteThisAction);
					}
					if (hasLater) {
						JMenuItem item = new JMenuItem(isAlwaysMarked ?
								TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteLaterMatches") : //$NON-NLS-1$
								TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteLater")); //$NON-NLS-1$
						popup.add(item);
						item.addActionListener(deleteLaterAction);
					}
					if (hasNotOnlyThis) {
						JMenuItem item = new JMenuItem(TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteAll")); //$NON-NLS-1$
						popup.add(item);
						item.addActionListener(deleteAllAction);
					}
					FontSizer.setFonts(popup, FontSizer.getLevel());
					popup.show(deleteButton, 0, deleteButton.getHeight());
				}
			});
			deleteButton.addKeyListener(kl);

			keyFrameButton = new JButton();
			keyFrameButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// find all key frames
					ArrayList<Integer> keyFrames = core.listKeyFrames();
					Action keyAction = new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							int i = Integer.parseInt(e.getActionCommand());
							VideoClip clip = trackerPanel.getPlayer().getVideoClip();
							trackerPanel.getPlayer().setStepNumber(clip.frameToStep(i));
						}
					};
					JPopupMenu popup = new JPopupMenu();
					for (Integer i : keyFrames) {
						String s = TrackerRes.getString("AutoTracker.Label.Frame"); //$NON-NLS-1$
						JMenuItem item = new JMenuItem(s + " " + i); //$NON-NLS-1$
						item.addActionListener(keyAction);
						item.setActionCommand(String.valueOf(i));
						popup.add(item);
					}
					FontSizer.setFonts(popup, FontSizer.getLevel());
					popup.show(keyFrameButton, 0, keyFrameButton.getHeight());
				}
			});
			keyFrameButton.addKeyListener(kl);

			// assemble content
			infoPanel = new JPanel(new BorderLayout()) {
				public Dimension getPreferredSize() {
					if (textPaneSize != null) return textPaneSize;
					return super.getPreferredSize();
				}
			};
			Border empty = BorderFactory.createEmptyBorder(4, 6, 4, 6);
			Border etch = BorderFactory.createEtchedBorder();
			infoPanel.setBorder(BorderFactory.createCompoundBorder(etch, empty));
			infoPanel.setBackground(textPane.getBackground());
			infoPanel.add(textPane, BorderLayout.CENTER);
			infoPanel.add(followupPanel, BorderLayout.SOUTH);

			JPanel controlPanel = new JPanel(new GridLayout(0, 1));
			controlPanel.add(templateToolbar);
			controlPanel.add(geometryToolbar);
			controlPanel.add(autoskipToolbar);
			controlPanel.add(searchToolbar);
			controlPanel.add(targetToolbar);

			northPanel = new JPanel(new BorderLayout());
			northPanel.add(startPanel, BorderLayout.NORTH);
			northPanel.add(imageToolbar, BorderLayout.SOUTH);

			JPanel center = new JPanel(new BorderLayout());
			center.add(controlPanel, BorderLayout.NORTH);
			center.add(infoPanel, BorderLayout.CENTER);

			JPanel south = new JPanel(new FlowLayout());
			south.add(helpButton);
			south.add(keyFrameButton);
			south.add(deleteButton);
			south.add(closeButton);

			contentPane.add(northPanel, BorderLayout.NORTH);
			contentPane.add(center, BorderLayout.CENTER);
			contentPane.add(south, BorderLayout.SOUTH);

			refreshGUI();
		}

		/**
		 * Refreshes the preferred size of the text pane.
		 */
		protected void refreshTextPaneSize() {
			textPaneSize = null;
			followupPanel.removeAll();
			followupPanel.add(acceptButton);
			textPane.setText(getTemplateInstructions());
			Dimension dim = infoPanel.getPreferredSize();
			textPane.setText(getTargetInstructions());
			dim.height = Math.max(dim.height, infoPanel.getPreferredSize().height);
			textPane.setText(getSearchInstructions());
			dim.height = Math.max(dim.height, infoPanel.getPreferredSize().height);
			dim.height += 6;
			textPaneSize = dim;
			refreshButtons();
			refreshInfo();
		}

		/**
		 * Refreshes the titles and labels.
		 */
		protected void refreshStrings() {
			Runnable runner = new Runnable() {
				public void run() {
					int n = trackerPanel.getFrameNumber();
					FrameData frame = getFrame(n);
					FrameData keyFrame = frame.getKeyFrame();

					// set titles and labels of GUI elements
					String title = TrackerRes.getString("AutoTracker.Wizard.Title"); //$NON-NLS-1$
					TTrack track = getTrack();
					if (track != null) {
						int index = track.getTargetIndex();
						title += ": " + track.getName() + " " + track.getTargetDescription(index); //$NON-NLS-1$ //$NON-NLS-2$
					}
					setTitle(title);

					frameLabel.setText(TrackerRes.getString("AutoTracker.Label.Frame") + " " + n + ":"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					searchLabel.setText(
							TrackerRes.getString("AutoTracker.Label.Search") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
					targetLabel.setText(
							TrackerRes.getString("AutoTracker.Label.Target") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
					templateLabel.setText(
							TrackerRes.getString("AutoTracker.Label.Template") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
					acceptLabel.setText(TrackerRes.getString("AutoTracker.Label.Automark")); //$NON-NLS-1$
					trackLabel.setText(TrackerRes.getString("AutoTracker.Label.Track")); //$NON-NLS-1$
					pointLabel.setText(TrackerRes.getString("AutoTracker.Label.Point")); //$NON-NLS-1$
					evolveRateLabel.setText(TrackerRes.getString("AutoTracker.Label.EvolutionRate")); //$NON-NLS-1$
					autoskipLabel.setText(TrackerRes.getString("AutoTracker.Label.Autoskip")); //$NON-NLS-1$
					closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
					helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$
					acceptButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Accept")); //$NON-NLS-1$
					keyFrameButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.ShowKeyFrame")); //$NON-NLS-1$
					deleteButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Delete")); //$NON-NLS-1$
					oneDCheckbox.setText(TrackerRes.getString("AutoTracker.Wizard.Checkbox.XAxis")); //$NON-NLS-1$
					lookAheadCheckbox.setText(TrackerRes.getString("AutoTracker.Wizard.Checkbox.LookAhead")); //$NON-NLS-1$
					matchImageLabel.setText(frame.getMatchIcon() == null ? null :
							TrackerRes.getString("AutoTracker.Label.Match")); //$NON-NLS-1$
					templateImageLabel.setText(keyFrame == null ? null :
							TrackerRes.getString("AutoTracker.Label.Template")); //$NON-NLS-1$

					if (trackerPanel.getVideo() != null) {
						boolean running = stepping && !paused;
						startButton.setIcon(stepping ? stopIcon : searchIcon);
						startButton.setText(stepping ?
								TrackerRes.getString("AutoTracker.Wizard.Button.Stop") :  //$NON-NLS-1$
								TrackerRes.getString("AutoTracker.Wizard.Button.Search") //$NON-NLS-1$
						);
						startButton.setToolTipText(TrackerRes.getString("AutoTracker.Wizard.Button.Search.Tooltip")); //$NON-NLS-1$
						FontSizer.setFonts(startButton, FontSizer.getLevel());
						searchThisButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.SearchThis")); //$NON-NLS-1$
						searchThisButton.setEnabled(!running);
						searchThisButton.setToolTipText(TrackerRes.getString("AutoTracker.Wizard.Button.SearchThis.Tooltip")); //$NON-NLS-1$
						searchNextButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.SearchNext")); //$NON-NLS-1$
						searchNextButton.setEnabled(!running);
						searchNextButton.setToolTipText(TrackerRes.getString("AutoTracker.Wizard.Button.SearchNext.Tooltip")); //$NON-NLS-1$
					}

					// set label sizes
					FontRenderContext frc = new FontRenderContext(null, false, false);
					Font font = frameLabel.getFont();
					int w = 0;
					Rectangle2D rect = font.getStringBounds(searchLabel.getText() + "   ", frc); //$NON-NLS-1$
					w = Math.max(w, (int) rect.getWidth() + 4);
					rect = font.getStringBounds(frameLabel.getText() + "   ", frc); //$NON-NLS-1$
					w = Math.max(w, (int) rect.getWidth() + 4);
					rect = font.getStringBounds(templateLabel.getText() + "   ", frc); //$NON-NLS-1$
					w = Math.max(w, (int) rect.getWidth() + 4);
					rect = font.getStringBounds(targetLabel.getText() + "   ", frc); //$NON-NLS-1$
					w = Math.max(w, (int) rect.getWidth() + 4);
					Dimension labelSize = new Dimension(w, 20);
					frameLabel.setPreferredSize(labelSize);
					templateLabel.setPreferredSize(labelSize);
					searchLabel.setPreferredSize(labelSize);
					targetLabel.setPreferredSize(labelSize);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) runner.run();
			else SwingUtilities.invokeLater(runner);
		}

		/**
		 * Refreshes the buttons and layout.
		 */
		protected void refreshButtons() {
			Runnable runner = new Runnable() {
				public void run() {
					int n = trackerPanel.getFrameNumber();
					FrameData frame = getFrame(n);
					TTrack track = getTrack();

					// enable the search buttons
					int code = core.getStatusCode(n);
					KeyFrame keyFrame = frame.getKeyFrame();
					boolean initialized = keyFrame != null && track != null;
					boolean notStepping = paused || !stepping;
					boolean stable = frame.searched && !frame.newTemplateExists();
					boolean canSearchThis = !stable || code == 5 || (changed && code != 0) || (frame == keyFrame && frame.getMarkedPoint() == null);
					startButton.setEnabled(initialized);
					searchThisButton.setEnabled(initialized && notStepping && canSearchThis);
					searchNextButton.setEnabled(initialized && control.canStep() && notStepping);

					// refresh template image labels and panel
					if (templateImageLabel.getIcon() == null && matchImageLabel.getIcon() == null) {
						templateImageLabel.setText(TrackerRes.getString("AutoTracker.Label.NoTemplate")); //$NON-NLS-1$
						matchImageLabel.setText(null);
						imageToolbar.setPreferredSize(templateToolbar.getPreferredSize());
						templateImageLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
					} else {
						imageToolbar.setPreferredSize(null);
						templateImageLabel.setBorder(null);
					}

					// refresh the delete and keyframe buttons

					deleteButton.setEnabled(core.isDeletable(n));
					keyFrameButton.setEnabled(keyFrame != null);

					// rebuild followup panel
					followupPanel.removeAll();
					if (code == 2 || code == 8) { // possible match
						acceptButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Accept")); //$NON-NLS-1$
						followupPanel.add(acceptButton);
					}
					if (code == 2 || code == 3 || code == 4 || code == 8 || code == 9) { // searched but not automarked
						skipButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Skip")); //$NON-NLS-1$
						followupPanel.add(skipButton);
					}
					repaint();
				}
			};
			if (SwingUtilities.isEventDispatchThread()) runner.run();
			else SwingUtilities.invokeLater(runner);
		}

		/**
		 * Refreshes the drawing flags.
		 */
		protected void refreshDrawingFlags() {
			// refresh drawing flags
			if (mouseOverObj == templateToolbar || mouseOverObj == imageToolbar) {
				// show mask and search
				maskVisible = true;
				targetVisible = searchVisible = false;
			} else if (mouseOverObj == targetToolbar) {
				// show target
				targetVisible = true;
				searchVisible = maskVisible = false;
			} else if (mouseOverObj == searchToolbar) {
				// show searchRect and mask
				searchVisible = true;
				targetVisible = maskVisible = false;
			} else {
				searchVisible = targetVisible = maskVisible = true;
			}
		}

		/**
		 * Refreshes the visible components of this wizard.
		 */
		protected void refreshGUI() {
			TTrack track = getTrack();
			if (track != null && this.isVisible()) {
				track.setMarkByDefault(false);
			}
			Runnable runner = new Runnable() {
				public void run() {
					if (trackerPanel == null) return;
					refreshDropdowns();
					refreshStrings();
					refreshIcons();
					refreshButtons();
					refreshInfo();
					refreshDrawingFlags();
					pack();
					if (textPaneSize == null) {
						refreshTextPaneSize();
					}
				}
			};
			if (SwingUtilities.isEventDispatchThread()) runner.run();
			else SwingUtilities.invokeLater(runner);
		}

		/**
		 * Refreshes the dropdown lists.
		 */
		protected void refreshDropdowns() {
			// refresh trackDropdown
			Object toSelect = null;
			trackDropdown.setName("refresh"); //$NON-NLS-1$
			trackDropdown.removeAllItems();
			TTrack track = getTrack();
			for (TTrack next : trackerPanel.getTracks()) {
				if (!next.isAutoTrackable()) continue;
				Icon icon = next.getFootprint().getIcon(21, 16);
				Object[] item = new Object[]{icon, next.getName()};
				trackDropdown.addItem(item);
				if (next == track) {
					toSelect = item;
				}
			}
			if (track == null) {
				Object[] emptyItem = new Object[]{null, "           "}; //$NON-NLS-1$
				trackDropdown.insertItemAt(emptyItem, 0);
				toSelect = emptyItem;
			}
			// select desired item
			if (toSelect != null) {
				trackDropdown.setSelectedItem(toSelect);
			}
			trackDropdown.setName(null);

			// refresh pointDropdown
			toSelect = null;
			pointDropdown.setName("refresh"); //$NON-NLS-1$
			pointDropdown.removeAllItems();
			if (track != null) {
				int target = track.getTargetIndex();
				toSelect = track.getTargetDescription(target);
				for (int i = 0; i < track.getStepLength(); i++) {
					String s = track.getTargetDescription(i);
					if (track.isAutoTrackable(i) && s != null) {
						pointDropdown.addItem(s);
					}
				}
			} else {
				pointDropdown.addItem("         "); //$NON-NLS-1$
			}
			if (toSelect != null) {
				pointDropdown.setSelectedItem(toSelect);
			}
			pointDropdown.setName(""); //$NON-NLS-1$

			Runnable runner = new Runnable() {
				public void run() {
					startButton.requestFocusInWindow();
				}
			};
			SwingUtilities.invokeLater(runner);
		}

		/**
		 * Refreshes the template icons.
		 */
		protected void refreshIcons() {
			Runnable runner = new Runnable() {
				public void run() {
					TTrack track = getTrack();
					if (core.getTemplateMatcher() == null || track == null) {
						templateImageLabel.setIcon(null);
						matchImageLabel.setIcon(null);
						return;
					}
					int n = trackerPanel.getFrameNumber();
					FrameData frame = getFrame(n);
					// set match icon
					Icon icon = frame.getMatchIcon();
					matchImageLabel.setIcon(icon);
					// set template icon
					icon = frame.getTemplateIcon();
					if (icon == null) {
						frame.getTemplateToMatch(); // loads new template and sets template icon
						icon = frame.getTemplateIcon();
					}
					templateImageLabel.setIcon(icon);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) runner.run();
			else SwingUtilities.invokeLater(runner);

		}

		/**
		 * Replaces the template icons with new ones.
		 *
		 * @param keyFrame the key frame with the template matcher
		 */
		protected void replaceIcons(final KeyFrame keyFrame) {
			Runnable runner = new Runnable() {
				public void run() {
					TTrack track = getTrack();
					if (trackerPanel.getVideo() == null || track == null) {
						templateImageLabel.setIcon(null);
						matchImageLabel.setIcon(null);
						return;
					}
					keyFrame.setTemplateMatcher(null); // triggers creation of new matcher
					TemplateMatcher matcher = core.getTemplateMatcher(); // creates new matcher
					if (matcher != null) {
						// initialize keyFrame and matcher
						keyFrame.setTemplate(matcher); // also sets template icon
						Icon icon = keyFrame.getTemplateIcon();
						keyFrame.setMatchIcon(icon);
						matchImageLabel.setIcon(icon);
						templateImageLabel.setIcon(icon);
						pack();
					}
				}
			};
			if (SwingUtilities.isEventDispatchThread()) runner.run();
			else SwingUtilities.invokeLater(runner);

		}

		/**
		 * Refreshes the info displayed in the textpane.
		 */
		protected void refreshInfo() {
			// red warning if no video
			if (trackerPanel.getVideo() == null) {
				textPane.setForeground(Color.red);
				textPane.setText(TrackerRes.getString("AutoTracker.Info.NoVideo")); //$NON-NLS-1$
				return;
			}

			// blue instructions if no track
			textPane.setForeground(Color.blue);
			TTrack track = getTrack();
			if (track == null) {
				textPane.setText(TrackerRes.getString("AutoTracker.Info.SelectTrack")); //$NON-NLS-1$
				return;
			}

			// blue instructions if no key frame
			int n = trackerPanel.getFrameNumber();
			FrameData frame = getFrame(n);
			KeyFrame keyFrame = frame.getKeyFrame();
			if (keyFrame == null) {
				String s = TrackerRes.getString("AutoTracker.Info.GetStarted"); //$NON-NLS-1$
				s += " " + TrackerRes.getString("AutoTracker.Info.MouseOver.Instructions"); //$NON-NLS-1$ //$NON-NLS-2$
				textPane.setText(s);
				if (mouseOverObj == null) return;
			}

			// colored instructions if mouseOverObj not null
			textPane.setForeground(new Color(140, 80, 80));
			if (mouseOverObj == templateToolbar || mouseOverObj == imageToolbar) {
				textPane.setText(getTemplateInstructions());
				return;
			}
			if (mouseOverObj == targetToolbar) {
				textPane.setText(getTargetInstructions());
				return;
			}
			if (mouseOverObj == searchToolbar) {
				textPane.setText(getSearchInstructions());
				return;
			}

			//  actively searching: show frame status
			textPane.setForeground(Color.blue);
			int code = core.getStatusCode(n);
			double[] peakWidthAndHeight = frame.getMatchWidthAndHeight();
			textPane.setText(getStatusInfo(code, n, peakWidthAndHeight));
		}

		/**
		 * Returns the template instructions.
		 *
		 * @return the instructions
		 */
		protected String getTemplateInstructions() {
			StringBuffer buf = new StringBuffer();
			buf.append(TrackerRes.getString("AutoTracker.Info.Mask1")); //$NON-NLS-1$
			buf.append(" "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.GetStarted")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Mask2")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Title.Settings")); //$NON-NLS-1$
			buf.append(": "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Mask.Instructions")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Title.Tip")); //$NON-NLS-1$
			buf.append(": "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Mask.Tip")); //$NON-NLS-1$
			return buf.toString();
		}

		/**
		 * Returns the search instructions.
		 *
		 * @return the instructions
		 */
		protected String getSearchInstructions() {
			StringBuffer buf = new StringBuffer();
			if (options.getLineSpread() >= 0)
				buf.append(TrackerRes.getString("AutoTracker.Info.SearchOnAxis")); //$NON-NLS-1$
			else
				buf.append(TrackerRes.getString("AutoTracker.Info.Search")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Title.Settings")); //$NON-NLS-1$
			buf.append(": "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Search.Instructions")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Title.Tip")); //$NON-NLS-1$
			buf.append(": "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Search.Tip")); //$NON-NLS-1$
			return buf.toString();
		}

		/**
		 * Returns the target instructions.
		 *
		 * @return the instructions
		 */
		protected String getTargetInstructions() {
			StringBuffer buf = new StringBuffer();
			buf.append(TrackerRes.getString("AutoTracker.Info.Target")); //$NON-NLS-1$
			buf.append("\n\n"); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Title.Settings")); //$NON-NLS-1$
			buf.append(": "); //$NON-NLS-1$
			buf.append(TrackerRes.getString("AutoTracker.Info.Target.Instructions")); //$NON-NLS-1$
			return buf.toString();
		}

		/**
		 * Returns the status text for a given frame number and status code.
		 *
		 * @param code               the status code (integer 0-9)
		 * @param n                  the frame number
		 * @param peakWidthAndHeight the match data
		 * @return the status text
		 */
		protected String getStatusInfo(int code, int n, double[] peakWidthAndHeight) {
			StringBuffer buf = new StringBuffer();
			buf.append(TrackerRes.getString("AutoTracker.Info.Frame") + " " + n); //$NON-NLS-1$ //$NON-NLS-2$
			switch (code) {
				//TODO: enum for codes, remove magic numbers
				case 0: // keyframe
					textPane.setForeground(Color.blue);
					buf.append(" ("); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.KeyFrame").toLowerCase()); //$NON-NLS-1$
					buf.append("): "); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.KeyFrame.Instructions1")); //$NON-NLS-1$
					buf.append("\n\n"); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.KeyFrame.Instructions2")); //$NON-NLS-1$
					buf.append(" "); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.MouseOver.Instructions")); //$NON-NLS-1$
					break;
				case 1: // good match was found and marked
					textPane.setForeground(Color.green.darker());
					buf.append(" (" + TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(" " + format.format(peakWidthAndHeight[1]) + "): "); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(TrackerRes.getString("AutoTracker.Info.Match")); //$NON-NLS-1$
					break;
				case 2: // possible match was found, not marked
					textPane.setForeground(Color.red);
					buf.append(" (" + TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(" " + format.format(peakWidthAndHeight[1]) + "): "); //$NON-NLS-1$ //$NON-NLS-2$
					if (options.getLineSpread() >= 0) {
						buf.append(TrackerRes.getString("AutoTracker.Info.PossibleOnAxis") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Accept")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append(TrackerRes.getString("AutoTracker.Info.Possible") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Accept")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
					if (control.canStep())
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 3: // no match was found
					textPane.setForeground(Color.red);
					buf.append(": "); //$NON-NLS-1$
					if (options.getLineSpread() >= 0) {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
					if (control.canStep())
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 4: // searchRect failed (no video image or x-axis inside)
					textPane.setForeground(Color.red);
					buf.append(": "); //$NON-NLS-1$
					if (options.getLineSpread() >= 0) { // 1D tracking
						buf.append(TrackerRes.getString("AutoTracker.Info.OutsideXAxis") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else { // 2D tracking
						buf.append(TrackerRes.getString("AutoTracker.Info.Outside") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
					if (control.canStep())
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 5: // target marked manually
					textPane.setForeground(Color.blue);
					buf.append(": "); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.MarkedByUser")); //$NON-NLS-1$
					break;
				case 6: // match accepted
					textPane.setForeground(Color.green.darker());
					buf.append(" (" + TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(" " + format.format(peakWidthAndHeight[1]) + "): "); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(TrackerRes.getString("AutoTracker.Info.Accepted")); //$NON-NLS-1$
					break;
				case 7: // not searched or marked
					textPane.setForeground(Color.blue);
					buf.append(" ("); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.Unsearched")); //$NON-NLS-1$
					buf.append("): "); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.Instructions")); //$NON-NLS-1$
					buf.append(" "); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.GetStarted")); //$NON-NLS-1$
					buf.append("\n\n"); //$NON-NLS-1$
					buf.append(TrackerRes.getString("AutoTracker.Info.MouseOver.Instructions")); //$NON-NLS-1$
					break;
				case 8: // possible match found, existing mark or calibration tool
					textPane.setForeground(Color.blue);
					buf.append(" (" + TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append(" " + format.format(peakWidthAndHeight[1]) + "): "); //$NON-NLS-1$ //$NON-NLS-2$
					if (options.getLineSpread() >= 0) {
						buf.append(TrackerRes.getString("AutoTracker.Info.PossibleReplace") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Replace")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append(TrackerRes.getString("AutoTracker.Info.PossibleReplace") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Replace")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 9: //  no match found, existing mark or calibration tool
					textPane.setForeground(Color.red);
					buf.append(": "); //$NON-NLS-1$
					if (options.getLineSpread() >= 0) {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (control.canStep())
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 10: // no match found, already marked
					textPane.setForeground(Color.red);
					buf.append(": "); //$NON-NLS-1$
					if (options.getLineSpread() >= 0) {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (control.canStep())
						buf.append("\n" + TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("\n" + TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
			}
			return buf.toString();
		}


		/**
		 * Gets the match data as a delimited string with "columns" for frame number, match score,
		 * target x and target y.
		 */
		protected String getMatchDataString() {
			// create string buffer to collect match score data
			StringBuffer buf = new StringBuffer();
			buf.append(getTrack().getName() + "_" + wizard.pointDropdown.getSelectedItem()); //$NON-NLS-1$
			buf.append(XML.NEW_LINE);
			buf.append(TrackerRes.getString("ThumbnailDialog.Label.FrameNumber") + TrackerIO.getDelimiter() + TrackerRes.getString("AutoTracker.Match.Score")); //$NON-NLS-1$ //$NON-NLS-2$
			String tar = "_" + TrackerRes.getString("AutoTracker.Label.Target").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(TrackerIO.getDelimiter() + "x" + tar + TrackerIO.getDelimiter() + "y" + tar); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(XML.NEW_LINE);
			Map<Integer, FrameData> frameData = core.getFrameData();
			NumberFormat scoreFormat = NumberFormat.getInstance();
			scoreFormat.setMaximumFractionDigits(1);
			scoreFormat.setMinimumFractionDigits(1);
			DecimalFormat xFormat = (DecimalFormat) NumberFormat.getInstance();
			DecimalFormat yFormat = (DecimalFormat) NumberFormat.getInstance();
			DataTable table = null;
			TableCellRenderer xRenderer = null, yRenderer = null;
			TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
			TreeMap<Integer, TableTrackView> dataViews = menubar.getDataViews();
			for (int key : dataViews.keySet()) {
				TableTrackView view = dataViews.get(key);
				if (view.getTrack() == getTrack()) {
					table = view.getDataTable();
					String pattern = table.getFormatPattern("x"); //$NON-NLS-1$
					if (pattern == null || pattern.equals("")) { //$NON-NLS-1$
						xRenderer = table.getDefaultRenderer(Double.class);
					} else {
						xFormat.applyPattern(pattern);
					}
					pattern = table.getFormatPattern("y"); //$NON-NLS-1$
					if (pattern == null || pattern.equals("")) { //$NON-NLS-1$
						yRenderer = table.getDefaultRenderer(Double.class);
					} else {
						yFormat.applyPattern(pattern);
					}
					break;
				}
			}
			for (Integer i : frameData.keySet()) {
				FrameData next = frameData.get(i);
				if (next == null || next.getMatchWidthAndHeight() == null) continue;
				double score = next.getMatchWidthAndHeight()[1];
				String value = Double.isInfinite(score) ? String.valueOf(score) : scoreFormat.format(score);
				buf.append(next.getFrameNumber() + TrackerIO.getDelimiter() + value);
				TPoint[] pts = next.getMatchPoints();
				if (pts != null) {
					TPoint p = pts[0]; // center of the match
					p = core.getMatchTarget(p); // target position
					Point2D pt = p.getWorldPosition(trackerPanel);
					String xval = xFormat.format(pt.getX());
					String yval = yFormat.format(pt.getY());
					if (xRenderer != null) {
						Component c = xRenderer.getTableCellRendererComponent(table, pt.getX(), false, false, 0, 0);
						if (c instanceof JLabel) {
							xval = ((JLabel) c).getText().trim();
						}
					}
					if (yRenderer != null) {
						Component c = yRenderer.getTableCellRendererComponent(table, pt.getY(), false, false, 0, 0);
						if (c instanceof JLabel) {
							yval = ((JLabel) c).getText().trim();
						}
					}
					buf.append(TrackerIO.getDelimiter() + xval + TrackerIO.getDelimiter() + yval);
				}
				buf.append(XML.NEW_LINE);
			}
			return buf.toString();
		}


		protected void prepareForFixedSearch(boolean fixed) {
			ignoreChanges = true;
			if (fixed) {
				prevEvolution = (Integer) evolveSpinner.getValue();
				prevLookAhead = lookAheadCheckbox.isSelected();
				prevOneD = oneDCheckbox.isSelected();
				isPrevValid = true;
				evolveSpinner.setValue(0);
				lookAheadCheckbox.setSelected(false);
				oneDCheckbox.setSelected(false);
			} else if (isPrevValid) {
				isPrevValid = false;
				evolveSpinner.setValue(prevEvolution);
				lookAheadCheckbox.setSelected(prevLookAhead);
				oneDCheckbox.setSelected(prevOneD);
			}
			evolveSpinner.setEnabled(!fixed);
			evolveRateLabel.setEnabled(!fixed);
			lookAheadCheckbox.setEnabled(!fixed);
			oneDCheckbox.setEnabled(!fixed);
			JFormattedTextField tf = ((JSpinner.DefaultEditor) evolveSpinner.getEditor()).getTextField();
			tf.setDisabledTextColor(fixed ? Color.GRAY.brighter() : Color.BLACK);
			ignoreChanges = false;
		}

	}

	//TODO: move to a separate class in the same package
	protected class TrackerPanelControl implements AutoTrackerControl {

		@Override
		public void step() {
			trackerPanel.getPlayer().step();
		}

		@Override
		public int getFrameNumber() {
			return trackerPanel.getFrameNumber();
		}

		@Override
		public int getFrameNumber(TPoint p) {
			return p.getFrameNumber(trackerPanel);
		}

		@Override
		public BufferedImage getImage() {
			return trackerPanel.getVideo().getImage();
		}

		@Override
		public boolean canStep() {
			VideoPlayer player = trackerPanel.getPlayer();
			int stepNumber = player.getStepNumber();
			if (!player.getVideoClip().reverse) {
				int endStepNumber = player.getVideoClip().getStepCount() - 1;
				return stepNumber < endStepNumber;
			} else {
				return stepNumber > 0;
			}
		}

		@Override
		public boolean isReverse() {
			return trackerPanel.getPlayer().getVideoClip().reverse;
		}

		@Override
		public int stepToFrame(int stepNumber) {
			return trackerPanel.getPlayer().getVideoClip().stepToFrame(stepNumber);
		}

		@Override
		public int frameToStep(int frameNumber) {
			return trackerPanel.getPlayer().getVideoClip().frameToStep(frameNumber);
		}

		@Override
		public int getFrameCount() {
			return trackerPanel.getPlayer().getVideoClip().getFrameCount();
		}

		@Override
		public ImageCoordSystem getCoords() {
			return trackerPanel.getCoords();
		}

		@Override
		public boolean isVideoValid() {
			return trackerPanel.getVideo() != null;
		}

	}

	class TrackerPanelFeedback extends AutoTrackerFeedback {
		@Override
		public void setSelectedTrack(TTrack track) {
			trackerPanel.setSelectedTrack(track);
		}

		@Override
		public void onBeforeAddKeyframe(double x, double y) {
			Target target = new Target();
			maskCenter.setLocation(x, y);
			maskCorner.setLocation(x + options.getMaskWidth() / 2, y + options.getMaskHeight() / 2);
			searchCenter.setLocation(x, y);
			searchCorner.setLocation(x + defaultSearchSize[0], y + defaultSearchSize[1]);
		}

		@Override
		public void onAfterAddKeyframe(KeyFrame keyFrame) {
			refreshSearchRect();
			refreshKeyFrame(keyFrame);
			getWizard().setVisible(true);
			//getWizard().refreshGUI();
			//search(false, false); // don't skip this frame and don't keep stepping
			trackerPanel.repaint();

		}

		@Override
		public void onSetTrack() {
			wizard.refreshGUI();
		}

		@Override
		public void onTrackUnbind(TTrack track) {
			if (track != null) {
				track.removePropertyChangeListener("step", AutoTracker.this); //$NON-NLS-1$
				track.removePropertyChangeListener("name", AutoTracker.this); //$NON-NLS-1$
				track.removePropertyChangeListener("color", AutoTracker.this); //$NON-NLS-1$
				track.removePropertyChangeListener("footprint", AutoTracker.this); //$NON-NLS-1$
			}
		}

		@Override
		public void onTrackBind(TTrack track) {
			if (track != null) {
				track.addPropertyChangeListener("step", AutoTracker.this); //$NON-NLS-1$
				track.addPropertyChangeListener("name", AutoTracker.this); //$NON-NLS-1$
				track.addPropertyChangeListener("color", AutoTracker.this); //$NON-NLS-1$
				track.addPropertyChangeListener("footprint", AutoTracker.this); //$NON-NLS-1$
				track.setVisible(true);
			}

		}
	}

}
