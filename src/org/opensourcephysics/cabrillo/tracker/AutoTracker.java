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

/**
 * A class to automatically track a feature of interest in a video.
 * This uses a TemplateMatcher to find a match to the feature in each frame
 * and, if found, marks the active track at the target location.
 *
 * @author Douglas Brown
 */
public class AutoTracker implements Interactive, Trackable, PropertyChangeListener {

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
	private static final float[] DOTTED_LINE = new float[] {2, 2};
	private static final float[] DASHED_LINE = new float[] {2, 8};
	private static NumberFormat format = NumberFormat.getNumberInstance();
	private static double cornerFactor = 0.9;
	private static BasicStroke solidBold = new BasicStroke(2), solid = new BasicStroke();
	private static BasicStroke dotted, dashed;
	private static int maxEvolveRate = 100; // percent
	private static int defaultEvolveRate = maxEvolveRate/5;
	private static Icon searchIcon, stopIcon, graySearchIcon;
	private static double[] defaultMaskSize = {9, 9};
	private static double[] defaultSearchSize = {40, 40};
	private static int templateIconMagnification = 2;
	private static int predictionLookback = 4;
  static boolean neverPause = true;

  static {
  	dotted = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,8,DOTTED_LINE,0);
  	dashed = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,8,DASHED_LINE,0);
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
  private int trackID;
  private Wizard wizard;
  private Shape match = new Ellipse2D.Double();
  private double minMaskRadius = 4;
  private Handle maskHandle = new Handle();
  private Corner maskCorner = new Corner();
  private TPoint maskCenter = new TPoint();
  private Handle searchHandle = new Handle();
  private Corner searchCorner = new Corner();
  private TPoint searchCenter = new TPoint();
  private TPoint predictedTarget = new TPoint();
  private Rectangle2D searchRect2D = new Rectangle2D.Double();
  private Shape searchShape, maskShape, matchShape;
  private Shape searchHitShape, maskHitShape;
  private Mark mark; // draws the mask, target and/or search area
  private Point[] screenPoints = {new Point()}; // used for footprints
  private boolean maskVisible, targetVisible, searchVisible;
  private Runnable stepper;
  private boolean stepping, active, paused, marking, lookAhead=true;
  private int goodMatch=4, possibleMatch=1, evolveAlpha=63, autoskipCount=2;
  private int autoskipsRemained = 0;
  /* trackFrameData maps tracks to indexFrameData which maps point index
  to frameData which maps frame number to individual FrameData objects */
  private Map<TTrack, Map<Integer, Map<Integer, FrameData>>> trackFrameData
			= new HashMap<TTrack, Map<Integer, Map<Integer, FrameData>>>();
  private int lineSpread = -1; // positive for 1D, negative for 2D tracking
  private boolean isInteracting;
  private double[][] derivatives1 = new double[predictionLookback-1][];
  private double[][] derivatives2 = new double[predictionLookback-1][];
  private double[][] derivatives3 = new double[predictionLookback-1][];


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
    stepper = new Runnable() {
      public void run() {
        TTrack track = getTrack();
      	if (!active || track==null) {
      		return;
      	}
      	// if never pausing, don't look ahead
      	boolean moveSearchArea = !neverPause;
  	    if (markCurrentFrame(moveSearchArea) || neverPause) {
  	    	// successfully found/marked a good match
  	    	if (!canStep()) { // reached the end
  	    		stop(true, true);
  	    		return;
  	    	}
    	    if (stepping) { // move to the next step
    	    	wizard.refreshInfo();
    	    	repaint();
    	    	trackerPanel.getPlayer().step();
    	    	return;
    	    }
    	    // not stepping, so stop
    	    stop(true, true);
  	    }
  	    else { // failed to find or mark a match, so pause or stop
  	    	if (!stepping)
  	    		stop(true, false);
  	    	else {
	  	    	paused = true;
	  	  		if (track instanceof PointMass) {
	  	  			PointMass pointMass = (PointMass)track;
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
    // place near top right corner of frame
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		TFrame frame = trackerPanel.getTFrame();
		Point frameLoc = frame.getLocationOnScreen();
		int w = wizard.getWidth()+8;
		int x = Math.min(screen.width-w, frameLoc.x+frame.getWidth()-w);
		int y = trackerPanel.getLocationOnScreen().y;
    wizard.setLocation(x, y);
  }

  /**
   * Sets the track to mark when matches are found.
   *
   * @param newTrack the track
   */
  protected void setTrack(TTrack newTrack) {
  	if (newTrack!=null && !newTrack.isAutoTrackable())
  		newTrack = null;
    TTrack track = getTrack();
  	if (track==newTrack) return;
  	if (track!=null) {
    	track.removePropertyChangeListener("step", this); //$NON-NLS-1$
    	track.removePropertyChangeListener("name", this); //$NON-NLS-1$
    	track.removePropertyChangeListener("color", this); //$NON-NLS-1$
    	track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
  	}
  	track = newTrack;
  	if (track!=null) {
  		trackID = track.getID();
  		trackerPanel.setSelectedTrack(track);
	  	track.addPropertyChangeListener("step", this); //$NON-NLS-1$
	  	track.addPropertyChangeListener("name", this); //$NON-NLS-1$
    	track.addPropertyChangeListener("color", this); //$NON-NLS-1$
    	track.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
	  	track.setVisible(true);
			int n = trackerPanel.getFrameNumber();
	    FrameData frame = getFrame(n);
			TPoint[] searchPts = frame.getSearchPoints(true);
			if (searchPts != null)
				setSearchPoints(searchPts[0], searchPts[1]);
  	}
  	else {
  		trackID = -1;
  	}
  	wizard.refreshGUI();
  }

  /**
   * Adds a key frame for a given track point and mask center position.
   *
   * @param p the track point
   * @param x the mask center x
   * @param y the mask center y
   */
  protected void addKeyFrame(TPoint p, double x, double y) {
  	int n = trackerPanel.getFrameNumber();
  	Target target = new Target();
  	Shape mask = new Ellipse2D.Double();
  	maskCenter.setLocation(x, y);
  	maskCorner.setLocation(x+defaultMaskSize[0], y+defaultMaskSize[1]);
  	searchCenter.setLocation(x, y);
  	searchCorner.setLocation(x+defaultSearchSize[0], y+defaultSearchSize[1]);
  	Map<Integer, FrameData> frames = getFrameData();
  	KeyFrame keyFrame = new KeyFrame(p, mask, target);
  	frames.put(n, keyFrame);
  	clearSearchPointsDownstream();
  	refreshSearchRect();
    refreshKeyFrame(keyFrame);
    getWizard().setVisible(true);
//  getWizard().refreshGUI();
//  search(false, false); // don't skip this frame and don't keep stepping
    trackerPanel.repaint();
  }

  /**
   * Starts the search process.
   *
   * @param startWithThis true to search the current frame
   * @param keepGoing true to continue stepping after the first search
   */
  protected void search(boolean startWithThis, boolean keepGoing) {
  	stepping = stepping || keepGoing;
  	wizard.changed = false;
  	active = true; // actively searching
  	paused = false;
    if (!startWithThis || markCurrentFrame(false) || neverPause) {
    	if (canStep() && (!startWithThis||stepping)) {
	    	trackerPanel.getPlayer().step();
	    	return;
	    }
    	if (startWithThis && !stepping) { // mark this frame only
    		active = false;
    	}
	    // reached end frame, so stop
    	else {
    		stop(true, true);
    	}
    }
    else {
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
   * @param now true to stop now
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
				PointMass pointMass = (PointMass)track;
				pointMass.updateDerivatives();
			}
	  	track.firePropertyChange("steps", null, null); //$NON-NLS-1$
  	}
  }

  /**
   * Marks a new step in the current frame if a match is found.
   *
   * @param predictLoc true to use look-ahead prediction for setting the search loc
   * @return true if a new step was marked or skipped automatically
   */
  public boolean markCurrentFrame(boolean predictLoc) {
    TTrack track = getTrack();
  	if (track==null) return false;
  	trackerPanel.setSelectedTrack(track);
    int n = trackerPanel.getFrameNumber();
    FrameData frame = getFrame(n);
    KeyFrame keyFrame = frame.getKeyFrame();
    if (keyFrame!=null && !track.isStepComplete(n)) {
	    TPoint p = findMatchTarget(predictLoc);
	    double[] peakWidthAndHeight = frame.getMatchWidthAndHeight();
	    if (p != null
                && (Double.isInfinite(peakWidthAndHeight[1])
                || (peakWidthAndHeight[1] >= goodMatch))
        ) {
  			marking = true;
  			track.autoTrackerMarking = track.isAutoAdvance();
  			p = track.autoMarkAt(n, p.x, p.y);
  			frame.setAutoMarkPoint(p);
  			track.autoTrackerMarking = false;
  			// We can perform autoskips if needed
  			autoskipsRemained = autoskipCount;
	    	return true;
	    }
		if (p==null) {
			if(autoskipsRemained > 0){
				autoskipsRemained--;
				track.skippedStepWarningSuppress = true;
				return true;
			}
			frame.setMatchIcon(null);
		}
    }
  	return false;
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
  	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  	int stepNumber = clip.frameToStep(frameNumber);

  	// get position data at previous steps
  	TPoint[] prevPoints = new TPoint[predictionLookback];
    TTrack track = getTrack();
  	if (stepNumber>0 && track!=null) {
  		for (int j = 0; j<predictionLookback; j++) {
  			if (stepNumber-j-1 >= 0) {
  				int n = clip.stepToFrame(stepNumber-j-1);
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
  	if (!lookAhead || prevPoints[1]==null) {
  		// no recent velocity or acceleration data available
    	success = true;
  	}

  	if (!success) {
	  	// get derivatives
	  	double[][] veloc = getDerivatives(prevPoints, 1);
	  	double[][] accel = getDerivatives(prevPoints, 2);
	   	double[][] jerk = getDerivatives(prevPoints, 3);

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
    	BufferedImage image = trackerPanel.getVideo().getImage();
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

  /**
   * Finds the match target, if any. Also saves search center and corner.
   *
   * @param predict true to predict the location before searching
   * @return the match target, or null if no match is found
   */
  public TPoint findMatchTarget(boolean predict) {
  	int n = trackerPanel.getFrameNumber();
  	FrameData frame = getFrame(n);
  	// if predicting, move searchRect to predicted location
  	if (predict) {
  		TPoint prediction = getPredictedMatchTarget(n);
  		if (prediction != null) {
  			TPoint p = getMatchCenter(prediction);
  			setSearchPoints(p, null);
  		}
  	}
  	// save search center and corner points
  	TPoint[] pts = new TPoint[] {new TPoint(searchCenter), new TPoint(searchCorner)};
  	frame.setSearchPoints(pts);
  	return findMatchTarget(getSearchRect());
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
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
  	// don't draw this unless wizard is visible and a video exists
    TTrack track = getTrack();
    if (track==null || wizard==null
    		|| !wizard.isVisible() || trackerPanel.getVideo()==null) {
    	maskVisible = targetVisible = searchVisible = false;
    	return;
    }
    if (wizard != null && wizard.isVisible()
    		&& !maskVisible && !targetVisible && !searchVisible) {
    	wizard.refreshGUI();
    	maskVisible = targetVisible = searchVisible = true;
    }
  	Graphics2D g2 = (Graphics2D)g;
    if (getMark() != null) {
    	mark.draw(g2, false);
    }
  }

  /**
   * Finds the TPoint, if any, located at a specified pixel position.
   * May return null.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the TPoint, or null if none
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	isInteracting = false;
  	int n = trackerPanel.getFrameNumber();
  	KeyFrame keyFrame = getFrame(n).getKeyFrame();
  	if (keyFrame==null || !wizard.isVisible() || trackerPanel.getVideo()==null) {
  		return null;
  	}
    hitRect.setLocation(xpix - hitRect.width/2, ypix - hitRect.height/2);
    if (targetVisible) {
    	Target target = keyFrame.getTarget();
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
		if (getTrack()==track) {
  		int n = track.trackerPanel.getFrameNumber();
  		AutoTracker.FrameData frame = getFrame(n);
  		return frame!=null && frame==frame.getKeyFrame() && isInteracting();
		}
		return false;
  }

  /**
   * Gets the TemplateMatcher. May return null.
   *
   * @return the template matcher
   */
  public TemplateMatcher getTemplateMatcher() {
  	if (trackerPanel==null) return null;
  	int n = trackerPanel.getFrameNumber();
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
  	if (moveRectIntoImage(searchRect2D)) { // true if moved
  		// set search center and corner locations to reflect new searchRect
  		searchCenter.setLocation(searchRect2D.getCenterX(), searchRect2D.getCenterY());
  		searchCorner.setLocation(searchRect2D.getMaxX(), searchRect2D.getMaxY());
  	}

  	// save the search points in the current frame
  	int n = trackerPanel.getFrameNumber();
		FrameData frame = getFrame(n);
  	TPoint[] pts = new TPoint[] {new TPoint(searchCenter), new TPoint(searchCorner)};
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
    	BufferedImage image = trackerPanel.getVideo().getImage();
    	int w = image.getWidth();
    	int h = image.getHeight();
  		int setbackX = searchRect2D.getBounds().width/2;
  		int setbackY = searchRect2D.getBounds().height/2;
  		center.x = Math.max(center.x, setbackX);
  		center.x = Math.min(center.x, w-setbackX);
  		center.y = Math.max(center.y, setbackY);
  		center.y = Math.min(center.y, h-setbackY);
  		// move rectangle to new center
	  	double dx = center.x - searchCenter.x;
	  	double dy = center.y - searchCenter.y;
	    searchCenter.x += dx;
	  	searchCenter.y += dy;
	  	searchCorner.x += dx;
	  	searchCorner.y += dy;
  	}
  	else {
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
		int n = trackerPanel.getFrameNumber();
    FrameData frame = getFrame(n);
  	KeyFrame keyFrame = frame.getKeyFrame();

		if (name.equals("selectedpoint")) { //$NON-NLS-1$
			boolean needsRepaint = false;
			TPoint prev = (TPoint)e.getOldValue();
			if (wizard.isVisible()) {
				if (prev instanceof Corner && keyFrame!=null) {
					needsRepaint = true;
					// restore corner positions
					Shape mask = keyFrame.getMask();
			  	if (mask instanceof Ellipse2D.Double) {
			  		Ellipse2D.Double circle = (Ellipse2D.Double)mask;
			  		maskCorner.x = maskCenter.x + circle.width/(2*cornerFactor);
			  		maskCorner.y = maskCenter.y + circle.height/(2*cornerFactor);
			  	}
			  	searchCorner.x = searchRect2D.getMaxX();
			  	searchCorner.y = searchRect2D.getMaxY();
				}
				else if (prev instanceof Handle || prev instanceof Target) {
	    		needsRepaint = true;
	    	}
			}
    	Step step = trackerPanel.getSelectedStep();
			TPoint next = (TPoint)e.getNewValue();
			if (next==maskHandle || next==maskCorner
    			|| next==searchHandle || next==searchCorner
    			|| (keyFrame!=null && next==keyFrame.getTarget())) {
    		trackerPanel.setSelectedTrack(track);
    		needsRepaint = true;
    	}
    	else if (next!=null && step!=null && step.getTrack()==track) {
    		int i = step.getPointIndex(next);
    		if (i>-1 && i!=track.getTargetIndex()) {
        	track.setTargetIndex(i);

    			// get frame for new index and reposition search points and mask
    	    frame = getFrame(n);
    			TPoint[] searchPts = frame.getSearchPoints(true);
    			if (searchPts != null)
    				setSearchPoints(searchPts[0], searchPts[1]);

    			keyFrame = frame.getKeyFrame();
    			if (keyFrame!=null) {
	        	maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
	        	maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
    			}

    			wizard.refreshGUI();
	    		needsRepaint = true;
    		}
    	}
			if (needsRepaint) repaint();
		}
		else if (name.equals("selectedtrack") && wizard!=null) { //$NON-NLS-1$
			wizard.refreshGUI();
		}
		else if (name.equals("track") && e.getOldValue()!=null) { //$NON-NLS-1$
			// track has been deleted
			TTrack deletedTrack = (TTrack)e.getOldValue();
	  	trackFrameData.remove(deletedTrack);
	  	if (deletedTrack==track) {
	  		setTrack(null);
	  	}
		}
		else if (name.equals("clear")) { //$NON-NLS-1$
			// tracks have been cleared
	  	trackFrameData.clear();
	  	setTrack(null);
		}

		if (wizard==null || !wizard.isVisible()) return;

		if (name.equals("video") || name.equals("name") //$NON-NLS-1$ //$NON-NLS-2$
				|| name.equals("color") || name.equals("footprint")) { //$NON-NLS-1$ //$NON-NLS-2$
    	wizard.refreshGUI();
		}
		else if (track==null && name.equals("stepnumber")) { //$NON-NLS-1$
			wizard.refreshGUI();
		}

    if (track==null || trackerPanel.getVideo()==null) {
    	return;
    }
		if (name.equals("step") && wizard.isVisible()) { //$NON-NLS-1$
			if (!marking) { // not marked by this autotracker
				n = ((Integer)e.getNewValue()).intValue();
	      frame = getFrame(n);
	      frame.decided = true; // point dragged by user?
				if (track.getStep(n)==null) { // step was deleted
					frame.clear();
				}
				else if (!frame.isKeyFrame()) { // step was marked or moved
    			frame.setMatchIcon(null);
					paused = false;
				}
			}
			wizard.refreshGUI();
			marking = false;
		}
		else if (name.equals("stepnumber")) { //$NON-NLS-1$
			TPoint[] searchPts = frame.getSearchPoints(true);
			if (searchPts != null)
				setSearchPoints(searchPts[0], searchPts[1]);
			else if (lookAhead && keyFrame!=null) {
				TPoint prediction = getPredictedMatchTarget(n);
	  		if (prediction != null) {
	  			setSearchPoints(getMatchCenter(prediction), null);
	  	  	// save search center and corner points
	  	  	TPoint[] pts = new TPoint[] {new TPoint(searchCenter), new TPoint(searchCorner)};
	      	frame.setSearchPoints(pts);
	  		}
	  		else {
	  			repaint();
	  		}
			}
			if (active && !paused) { // actively tracking
	      if (SwingUtilities.isEventDispatchThread())
	      	stepper.run();
	      else
	      	SwingUtilities.invokeLater(stepper);
			}
			else if (stepping) { // user set the frame number, so stop stepping
				stop(true, false);
			}
			else wizard.refreshGUI();
		}
	}

  // implements Interactive & Measurable methods
  public void setEnabled(boolean enabled) {}
  public boolean isEnabled() {return true;}
  public void setXY(double x, double y) {}
  public void setX(double x) {}
  public void setY(double y) {}
  public double getX() {return 0;}
  public double getY() {return 0;}
  public double getXMin() {return 0;}
  public double getXMax() {return 0;}
  public double getYMin() {return 0;}
  public double getYMax() {return 0;}
  public boolean isMeasured() {return false;}

//_______________________________ protected methods _________________________

  /**
   * Finds the target for the best match found within the specified
   * searchRect. Also saves match width, height, center and corner.
   *
   * @param searchRect the search rectangle
   * @return the target, or null if no match found
   */
  protected TPoint findMatchTarget(Rectangle searchRect) {
  	Video video = trackerPanel.getVideo();
  	if (video == null) return null;
    TemplateMatcher matcher = getTemplateMatcher();
    if (matcher == null) return null;
  	int n = trackerPanel.getFrameNumber();
  	FrameData frame = getFrame(n);
  	frame.decided = false; // default

  	// set template to be matched
    matcher.setTemplate(frame.getTemplateToMatch());

    // get location, width and height of match
  	TPoint p = null;
  	BufferedImage image = video.getImage();
  	if (lineSpread>=0) {
	  	double theta = trackerPanel.getCoords().getAngle(n);
	  	double x0 = trackerPanel.getCoords().getOriginX(n);
	  	double y0 = trackerPanel.getCoords().getOriginY(n);
	  	p = matcher.getMatchLocation(image, searchRect, x0, y0, theta, lineSpread); // may be null
  	}
  	else {
  		p = matcher.getMatchLocation(image, searchRect); // may be null
  	}
  	double[] matchWidthAndHeight = matcher.getMatchWidthAndHeight();
  	if (matchWidthAndHeight[1]<goodMatch && frame.isAutoMarked()) {
  		frame.trackPoint = null;
  	}

  	// save match data and searched frames
  	frame.setMatchWidthAndHeight(matchWidthAndHeight);
  	frame.searched = true;
    // if p is null or match is poor, then clear match points
  	if (p==null || matchWidthAndHeight[1] < possibleMatch) {
        frame.setMatchPoints(null);
  		return null;
  	}

    // successfully found good or possible match: save match data
    BufferedImage match = matcher.getMatchImage();
    BufferedImage img = createMagnifiedImage(match);
  	frame.setMatchIcon(new ImageIcon(img));
	Rectangle rect = frame.getKeyFrame().getMask().getBounds();
	TPoint center = new TPoint(p.x+maskCenter.x-rect.getX(), p.y+maskCenter.y-rect.getY());
	TPoint corner = new TPoint(
	    center.x+cornerFactor*(maskCorner.x-maskCenter.x),
        center.y+cornerFactor*(maskCorner.y-maskCenter.y)
    );
    frame.setMatchPoints(new TPoint[] {center, corner, p});

    // if good match found then build evolved template and return match target
  	if (matchWidthAndHeight[1]>=goodMatch) {
  		buildEvolvedTemplate(frame);
  		return getMatchTarget(center);
  	}

    return null;
  }

  /**
   * Builds an evolved template based on data in the specified FrameData
   * and the current video image.
   *
   * @param frame the FrameData frame
   */
  protected void buildEvolvedTemplate(FrameData frame) {
  	TPoint[] matchPts = frame.getMatchPoints();
  	if (matchPts==null) return; // can't build template without a match
//  	System.out.println("building evolved for "+frame.getFrameNumber());
    TemplateMatcher matcher = getTemplateMatcher();
    matcher.setTemplate(frame.getTemplate());
    matcher.setWorkingPixels(frame.getWorkingPixels());
		Rectangle rect = frame.getKeyFrame().getMask().getBounds();
		// get new image to rebuild template
		int x = (int)Math.round(matchPts[2].getX());
		int y = (int)Math.round(matchPts[2].getY());
		BufferedImage source = trackerPanel.getVideo().getImage();
		BufferedImage matchImage = new BufferedImage(
				rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
		matchImage.createGraphics().drawImage(source, -x, -y, null);
		matcher.buildTemplate(matchImage, evolveAlpha, 0);
		matcher.setIndex(frame.getFrameNumber());
	}


  /**
   * Creates a TemplateMatcher based on the current image and mask.
   *
   * @return a newly created template matcher, or null if no video image exists
   */
  protected TemplateMatcher createTemplateMatcher() {
  	Video video = trackerPanel.getVideo();
    int n = trackerPanel.getFrameNumber();
    FrameData frame = getFrame(n);
    KeyFrame keyFrame = frame.getKeyFrame();
  	if (video!=null && keyFrame!=null) {
  		// create template image
  		Shape mask = keyFrame.getMask();
  		BufferedImage source = video.getImage();
  		Rectangle rect = mask.getBounds();
  		BufferedImage templateImage = new BufferedImage(
  				rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
  		templateImage.createGraphics().drawImage(source, -rect.x, -rect.y, null);
  		// translate mask to (0, 0) relative to template
      	transform.setToTranslation(-rect.x, -rect.y);
      	Shape templateRegion = transform.createTransformedShape(mask);
  		return new TemplateMatcher(templateImage, templateRegion);
  	}
  	return null;
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

  protected int getIndex(TPoint p) {
  	int n = p.getFrameNumber(trackerPanel);
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

  protected TTrack getTrack() {
    return TTrack.getTrack(trackID);
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
  	trackFrameData.clear();
  	wizard.dispose();
  	trackerPanel.autoTracker = null;
  	trackerPanel = null;
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }

  /**
   * Gets the drawing mark.
   *
   * @return the mark
   */
  protected Mark getMark() {
    int n = trackerPanel.getFrameNumber();
    FrameData frame = getFrame(n);
    KeyFrame keyFrame = frame.getKeyFrame();
    final TTrack track = getTrack();
  	if (track==null || keyFrame==null) return null;
    if (mark==null) {
    	int k = getStatusCode(n);
    	// refresh target icon on wizard label
      	Color c = track.getFootprint().getColor();
		target_footprint.setColor(c);
		inactive_target_footprint.setColor(c);
		corner_footprint.setColor(c);
    	// define marks for center, corners, target and selection
    	Mark searchCornerMark=null, maskCornerMark=null,
    			targetMark=null, selectionMark=null;
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
      	selectionPt = maskVisible? maskHandle.getScreenPosition(trackerPanel): null;
      else if (selection == searchHandle)
      	selectionPt = searchVisible? searchHandle.getScreenPosition(trackerPanel): null;
      // create mask corner mark
      if (frame.isKeyFrame()) {
      	maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
      	maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
      }
      screenPoints[0] = maskCorner.getScreenPosition(trackerPanel);
      if (selection == maskCorner) {
      	selectionPt = maskVisible? screenPoints[0]: null;
      }
      else {
      	maskCornerMark = corner_footprint.getMark(screenPoints);
      }
      // create search corner mark
      screenPoints[0] = searchCorner.getScreenPosition(trackerPanel);
      if (selection == searchCorner) {
      	selectionPt = searchVisible? screenPoints[0]: null;
      }
      else {
      	searchCornerMark = corner_footprint.getMark(screenPoints);
      }
      // create target mark
      screenPoints[0] = keyFrame.getTarget().getScreenPosition(trackerPanel);
      if (selection == keyFrame.getTarget())
      	selectionPt = targetVisible? screenPoints[0]: null;
      else {
	     	targetMark = target_footprint.getMark(screenPoints);
      }
      // if a match has been found, create match shapes
      TPoint[] matchPts = frame.getMatchPoints();
      if (matchPts == null || frame.isKeyFrame() || k==5)
      	matchShape = null;
      else {
      	Point p1 = matchPts[0].getScreenPosition(trackerPanel);
      	Point p2 = maskCenter.getScreenPosition(trackerPanel);
      	transform.setToTranslation(p1.x-p2.x, p1.y-p2.y);
        matchShape = toScreen.createTransformedShape(getMatchShape(matchPts));
        screenPoints[0] = getMatchTarget(matchPts[0]).getScreenPosition(trackerPanel);
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
          BasicStroke stroke = (BasicStroke)g.getStroke();
          int n = trackerPanel.getFrameNumber();
          FrameData frame = getFrame(n);
          boolean isKeyFrame = frame!=null && frame.isKeyFrame();
          if (targetVisible) {
	          // draw the target
	          if (isKeyFrame) {
		          if (markTarget != null)
		          	markTarget.draw(g, false);
	          }
          }
          if (matchShape!=null && !isKeyFrame) {
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
   * Returns the target for a specified match center point.
   *
   * @param center the center point
   * @return the target
   */
  protected TPoint getMatchTarget(TPoint center) {
  	int n = trackerPanel.getFrameNumber();
    double[] offset = getFrame(n).getTargetOffset();
  	return new TPoint(center.x+offset[0], center.y+offset[1]);
  }

  /**
   * Returns the center point for a specified match target.
   *
   * @param target the target
   * @return the center
   */
  protected TPoint getMatchCenter(TPoint target) {
  	int n = trackerPanel.getFrameNumber();
    double[] offset = getFrame(n).getTargetOffset();
  	return new TPoint(target.x-offset[0], target.y-offset[1]);
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
		Map<Integer, FrameData> frameData = getFrameData();
		KeyFrame keyFrame = null;
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		for (int i: frameData.keySet()) {
			FrameData frame = frameData.get(i);
			frame.clear();
			if (keyFrame==null && frame.isKeyFrame())
				keyFrame = (KeyFrame)frame;
			toRemove.add(i);
		}
		for (int i: toRemove) {
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
	  if (keyFrame!=null) {
	    int n = keyFrame.getFrameNumber();
	    VideoPlayer player = trackerPanel.getPlayer();
	    player.setStepNumber(player.getVideoClip().frameToStep(n));
	  }
  	repaint();
  }

  /**
   * Refreshes the key frame to reflect current center and corner positions.
   *
   * @param keyFrame the KeyFrame
   */
  protected void refreshKeyFrame(KeyFrame keyFrame) {
  	Shape mask = keyFrame.getMask();
  	if (mask instanceof Ellipse2D.Double) {
  		// prevent the mask from being too small to contain any pixels
      keyFrame.getMaskPoints()[0].setLocation(maskCenter);
      keyFrame.getMaskPoints()[1].setLocation(maskCorner);
  		Ellipse2D.Double ellipse = (Ellipse2D.Double)mask;
			double sin = maskCenter.sin(maskCorner);
			double cos = maskCenter.cos(maskCorner);
			if (Double.isNaN(sin)) {
				sin = -0.707;
				cos = 0.707;
			}
  		double d = Math.max(minMaskRadius, maskCenter.distance(maskCorner));
  		double dx = d*cornerFactor*cos;
  		double dy = -d*cornerFactor*sin;
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
  	wizard.replaceIcons(keyFrame);
	  // get the marked point and set target position AFTER refreshing keyFrame
  	TPoint p = keyFrame.getMarkedPoint();
  	if (p!=null)
  		keyFrame.getTarget().setXY(p.getX(), p.getY());
    search(true, false); // search this frame only
		repaint();
		wizard.repaint();
  }

  protected BufferedImage createMagnifiedImage(BufferedImage source) {
		BufferedImage image = new BufferedImage(
				templateIconMagnification*source.getWidth(),
				templateIconMagnification*source.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(source, 0, 0, image.getWidth(), image.getHeight(), null);
		return image;
  }

  /**
   * Gets the match shape for the specified center and frame corner positions.
   *
   * @param pts TPoint[] {center, frame corner}
   * @return a shape suitable for drawing
   */
  protected Shape getMatchShape(TPoint[] pts) {
  	if (match instanceof Ellipse2D.Double) {
  		Ellipse2D.Double ellipse = (Ellipse2D.Double)match;
  		ellipse.setFrameFromCenter(pts[0], pts[1]);
  		return ellipse;
  	}
  	return null;
  }

  /**
   * Determines the status code for a given frame. The status codes are:
   *   0: a key frame
   *   1: automarked with a good match
   *   2: possible match, not marked
   *   3: searched but no match found
   *   4: unable to search--search area outside image or x-axis
   *   5: manually marked by the user
   *   6: match accepted by the user
   *   7: never searched
   *   8: possible match but previously marked
   *   9: no match found but previously marked
   *   10: calibration tool possible match
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
    		if (widthAndHeight[1]> goodMatch) return 1; // automarked with good match
    		return 6; // accepted by user
    	}
    	// not automarked
      TTrack track = getTrack();
    	boolean isCalibrationTool = track instanceof CoordAxes
    			|| track instanceof OffsetOrigin
    			|| track instanceof Calibration;
    	if (track instanceof TapeMeasure) {
    		TapeMeasure tape = (TapeMeasure)track;
    		isCalibrationTool = !tape.isReadOnly();
    	}
  		if (frame.searched) {
	    	if (isCalibrationTool) {
		    	if (widthAndHeight[1]>possibleMatch) return 8; // possible match for calibration
		    	return 9; // no match found, existing mark or calibration
	    	}
	    	if (frame.decided) return 5; // manually marked by user
	    	if (widthAndHeight[1]>possibleMatch) return 8; // possible match, already marked
	    	return 9; // no match found, existing mark or calibration
  		}
    	return 7; // never searched
    }
  	if (frame.searched) { // frame unmarked but searched
  		if (widthAndHeight[1]<possibleMatch) return 3; // no match found
  		return 2; // possible match found but not marked
  	}
  	// frame is unmarked and unsearched
		if (widthAndHeight==null) return 7; // never searched
		return 4; // tried but unable to search
  }

  protected boolean canStep() {
  	VideoPlayer player = trackerPanel.getPlayer();
    int stepNumber = player.getStepNumber();
  	if(!player.getVideoClip().reverse) {
		int endStepNumber = player.getVideoClip().getStepCount() - 1;
		return stepNumber < endStepNumber;
	}else{
  		return stepNumber > 0;
	}

  }

  protected boolean isDrawingKeyFrameFor(TTrack track, int index) {
  	int n = trackerPanel.getFrameNumber();
  	if (getTrack()==track && wizard.isVisible() && getFrame(n).isKeyFrame()) {
  		FrameData frame = getFrame(n);
  		return frame.getIndex()==index;
  	}
  	return false;
  }

  /**
   * Clears search points in frames downstream of the current frame number.
   */
  protected void clearSearchPointsDownstream() {
  	int n = trackerPanel.getFrameNumber();
		Map<Integer, FrameData> frameData = getFrameData();
		for (Integer i: frameData.keySet()) {
			if (i<=n) continue;
			FrameData frame = frameData.get(i);
			if (frame.isKeyFrame()) // only to the next key frame
				break;
			frame.setSearchPoints(null);
		}

  }

  protected boolean moveRectIntoImage(Rectangle2D searchRect) {
  	// if needed, modify search rectangle to keep it within the video image
  	BufferedImage image = trackerPanel.getVideo().getImage();
  	int w = image.getWidth();
  	int h = image.getHeight();
		Point2D corner = new Point2D.Double(searchRect.getX(), searchRect.getY());
		Dimension dim = new Dimension((int)searchRect.getWidth(), (int)searchRect.getHeight());

  	boolean changed = false;
		// reduce size if needed
		if (w < dim.width || h < dim.height) {
			changed = true;
			dim.setSize(Math.min(w, dim.width), Math.min(h, dim.height));
			searchRect.setFrame(corner, dim);
		}

		// move corner point if needed
		double x = Math.max(0, corner.getX());
		x = Math.min(x, w-dim.width);
		double y = Math.max(0, corner.getY());
		y = Math.min(y, h-dim.height);
		if (x!=corner.getX() || y!=corner.getY()) {
			changed = true;
			corner.setLocation(x, y);
			searchRect.setFrame(corner, dim);
		}

		return changed;
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
  protected double[][] getDerivatives(TPoint[] positions, int order) {
  	// return null if insufficient data
  	if (positions.length<order+1) return null;

  	if (order==1) { // velocity
  		for (int i=0; i<derivatives1.length; i++) {
  			if (i>=positions.length-1) {
  				derivatives1[i] = null;
  				continue;
  			}
  			TPoint loc0 = positions[i+1];
  			TPoint loc1 = positions[i];
  			if (loc0==null || loc1==null) {
  				derivatives1[i] = null;
  				continue;
  			}
  			double x = loc1.getX() -loc0.getX();
  			double y = loc1.getY() -loc0.getY();
  			derivatives1[i] = new double[] {x, y};
  		}
  		return derivatives1;
  	}
  	else if (order==2) { // acceleration
  		for (int i=0; i<derivatives2.length; i++) {
  			if (i>=positions.length-2) {
  				derivatives2[i] = null;
  				continue;
  			}
  			TPoint loc0 = positions[i+2];
  			TPoint loc1 = positions[i+1];
  			TPoint loc2 = positions[i];
  			if (loc0==null || loc1==null || loc2==null) {
  				derivatives2[i] = null;
  				continue;
  			}
  			double x = loc2.getX() - 2*loc1.getX() + loc0.getX();
  			double y = loc2.getY() - 2*loc1.getY() + loc0.getY();
  			derivatives2[i] = new double[] {x, y};
  		}
  		return derivatives2;
   	}
  	else if (order==3) { // jerk
  		for (int i=0; i<derivatives3.length; i++) {
  			if (i>=positions.length-3) {
  				derivatives3[i] = null;
  				continue;
  			}
  			TPoint loc0 = positions[i+3];
  			TPoint loc1 = positions[i+2];
  			TPoint loc2 = positions[i+1];
  			TPoint loc3 = positions[i];
  			if (loc0==null || loc1==null || loc2==null || loc3==null) {
  				derivatives3[i] = null;
  				continue;
  			}
  			double x = loc3.getX() - 3*loc2.getX() + 3*loc1.getX() - loc0.getX();
  			double y = loc3.getY() - 3*loc2.getY() + 3*loc1.getY() - loc0.getY();
  			derivatives3[i] = new double[] {x, y};
  		}
  		return derivatives3;
  	}
  	return null;
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
    	double dx = x-getX();
    	double dy = y-getY();
    	super.setXY(x, y);
	    	int n = trackerPanel.getFrameNumber();
	    if (this == searchHandle) {
	    	searchCenter.x += dx;
	    	searchCenter.y += dy;
	    	searchCorner.x += dx;
	    	searchCorner.y += dy;
	    	refreshSearchRect();
        wizard.setChanged();
    	}
	    else {
	    	maskCenter.x += dx;
	    	maskCenter.y += dy;
	    	maskCorner.x += dx;
	    	maskCorner.y += dy;
	      KeyFrame keyFrame = getFrame(n).getKeyFrame();
	      keyFrame.getMaskPoints()[0].setLocation(maskCenter);
	      keyFrame.getMaskPoints()[1].setLocation(maskCorner);
	      Target target = keyFrame.getTarget();
	      keyFrame.setTargetOffset(target.x-maskCenter.x, target.y-maskCenter.y);
	      refreshKeyFrame(keyFrame);
	    }
      clearSearchPointsDownstream();
    }

    /**
     * Sets the location of this point to the specified screen position.
     *
     * @param x the x screen position
     * @param y the y screen position
     * @param vidPanel the trackerPanel doing the drawing
     */
    public void setScreenLocation(int x, int y, VideoPanel vidPanel) {
      if (screenPt == null) screenPt = new Point();
      if (worldPt == null) worldPt = new Point2D.Double();
      screenPt.setLocation(x, y);
      AffineTransform toScreen = vidPanel.getPixelTransform();
      try {
        toScreen.inverseTransform(screenPt, worldPt);
      } catch(NoninvertibleTransformException ex) {
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
    	int n = trackerPanel.getFrameNumber();
      if (this == searchCorner) {
      	refreshSearchRect();
        wizard.setChanged();
      }
      else {
        refreshKeyFrame(getFrame(n).getKeyFrame());
      }
      clearSearchPointsDownstream();
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
      keyFrame.setTargetOffset(x-maskCenter.x, y-maskCenter.y);
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
			// refresh icons
			setMatchIcon(null);
  		BufferedImage img = createMagnifiedImage(template);
	  	setTemplateIcon(new ImageIcon(img));
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
  		if (isKeyFrame()) return false;
			TemplateMatcher matcher = getTemplateMatcher();
			if (matcher==null) return false;
  		boolean different = matcher.getAlphas()[0]!=templateAlpha
  				|| matcher.hashCode()!=matcherHashCode;
  		boolean appropriate = matcher.getIndex()<frameNum;
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
  		if (!inherit || searchPoints!=null || this.isKeyFrame()) return searchPoints;
      Map<Integer, FrameData> frames = getFrameData(index);
    	for (int i=frameNum; i>=0; i--) {
    		FrameData frame = frames.get(i);
    		if (frame!=null) {
    			if (frame.searchPoints!=null || frame.isKeyFrame()) {
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
		  if (!trackerPanel.getPlayer().getVideoClip().reverse) {
			  for (int i = frameNum; i >= 0; i--) {
				  FrameData frame = frames.get(i);
				  if (frame != null && frame.isKeyFrame())
					  return (KeyFrame) frame;
			  }
		  } else {
			  int fin = trackerPanel.getPlayer().getVideoClip().getFrameCount();
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

    boolean isAutoMarked() {
    	if (autoMarkLoc==null || trackPoint==null) return false;
    	if (trackPoint instanceof CoordAxes.AnglePoint) {
      	ImageCoordSystem coords = trackerPanel.getCoords();
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
  	private Target target;
  	private TPoint[] maskPoints = {new TPoint(), new TPoint()};
  	private TemplateMatcher matcher;

  	KeyFrame(TPoint keyPt, Shape mask, Target target) {
  		super(AutoTracker.this.getIndex(keyPt), keyPt.getFrameNumber(trackerPanel));
  		this.mask = mask;
  		this.target = target;
  		maskPoints[0].setLocation(maskCenter);
  		maskPoints[1].setLocation(maskCorner);
  	}

    boolean isKeyFrame() {
    	return true;
    }

    Shape getMask() {
  		return mask;
    }

    Target getTarget() {
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
    protected JToolBar templateToolbar, searchToolbar, targetToolbar, imageToolbar, trackToolbar, autoskipToolbar;
    private JPanel startPanel, followupPanel, infoPanel, northPanel, targetPanel;
    private JLabel templateImageLabel, matchImageLabel, acceptLabel, templateLabel;
    private JLabel frameLabel, evolveRateLabel, searchLabel, targetLabel;
    private JLabel pointLabel, trackLabel;
    protected Dimension textPaneSize;
    private JCheckBox lookAheadCheckbox, oneDCheckbox;
    private Object mouseOverObj;
    private MouseAdapter mouseOverListener;
    private Timer timer;
    private boolean ignoreChanges, isPrevValid, prevLookAhead, prevOneD;
    private int prevEvolution;

    private JLabel autoskipLabel;
    private TallSpinner autoskipSpinner;

    /**
     * Constructs a Wizard.
     */
    public Wizard() {
      super(trackerPanel.getTFrame(), false);
      createGUI();
      pack();
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
        }
        else {
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
      }
      else {
      	TTrack track = trackerPanel.getSelectedTrack();
      	if (track!=null) setTrack(track);
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
      Object[] buttons = new Object[] {acceptButton, skipButton};
      FontSizer.setFonts(buttons, FontSizer.getLevel());
      //  	private JComboBox trackDropdown, pointDropdown;
  		JComboBox[] dropdowns = new JComboBox[] {trackDropdown, pointDropdown};
  		for (JComboBox next: dropdowns) {
  			int n = next.getSelectedIndex();
  			Object[] items = new Object[next.getItemCount()];
  			for (int i=0; i<items.length; i++) {
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

    protected void setAlphaFromRate(int evolveRate) {
    	double max = maxEvolveRate;
    	int alpha = (int)(1.0*evolveRate*255/max);
    	if (evolveRate>=max) alpha = 255;
    	if (evolveRate<=0) alpha = 0;
    	evolveAlpha=alpha;
    }

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
        				int n = trackerPanel.getPlayer().getStepNumber()-5;
        				trackerPanel.getPlayer().setStepNumber(n);
        			}
        			else trackerPanel.getPlayer().back();
        			break;
        		case KeyEvent.VK_PAGE_DOWN:
        			if (e.isShiftDown()) {
        				int n = trackerPanel.getPlayer().getStepNumber()+5;
        				trackerPanel.getPlayer().setStepNumber(n);
        			}
        			else trackerPanel.getPlayer().step();
        			break;
        		case KeyEvent.VK_HOME:
        			trackerPanel.getPlayer().setStepNumber(0);
        			break;
        		case KeyEvent.VK_END:
        			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
        			trackerPanel.getPlayer().setStepNumber(clip.getStepCount()-1);
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
        	if (e.getKeyCode()==KeyEvent.VK_SHIFT) {
        		trackerPanel.isShiftKeyDown = false;
        		startButton.setText(stepping?
    					TrackerRes.getString("AutoTracker.Wizard.Button.Stop"): //$NON-NLS-1$
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
      		Component c = (Component)e.getSource();
      		while (c.getParent()!=null) {
      			if (c==templateToolbar
      					|| c==searchToolbar
      					|| c==targetToolbar
      					|| c==imageToolbar) {
      				mouseOverObj = c;
      				isInteracting = c==targetToolbar;
      				isInteracting = true;
      				break;
      			}
      			c = c.getParent();
      		}
      		if (mouseOverObj==null) {
      			// refresh immediately
	          refreshInfo();
	          refreshDrawingFlags();
	          erase();
	          trackerPanel.repaint();
      		}
      		else {
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
          if (track!=null) trackerPanel.setSelectedTrack(track);
      	}
      });
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);

      // create trackDropdown early since need it for spinners
      trackDropdown = new JComboBox() {
        public Dimension getPreferredSize() {
      		Dimension dim = super.getPreferredSize();
      		dim.height-=1;
      		return dim;
        }
      };
      trackDropdown.addMouseListener(mouseOverListener);
      for (int i = 0; i<trackDropdown.getComponentCount(); i++) {
      	trackDropdown.getComponent(i).addMouseListener(mouseOverListener);
      }
      trackDropdown.setRenderer(new TrackRenderer());
      trackDropdown.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if ("refresh".equals(trackDropdown.getName())) return; //$NON-NLS-1$
          Object[] item = (Object[])trackDropdown.getSelectedItem();
          if(item!=null) {
          	for (TTrack next: trackerPanel.getTracks()) {
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
        	}
        	else search(true, true); // search this frame and keep stepping
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
        	neverPause = (e.getModifiers()&0x01)==1; // shift key down
        	if (neverPause && !stepping) {
        		// show popup menu
        		if (popup==null) {
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
        	}
        	else {
	        	searchAction.actionPerformed(e);
        	}
        }
      });
      startButton.addKeyListener(kl);
      startButton.addMouseMotionListener(new MouseAdapter() {
      	@Override
      	public void mouseMoved(MouseEvent e) {
      		startButton.setText(e.isShiftDown() && !stepping?
      				TrackerRes.getString("AutoTracker.Wizard.Button.Options"): //$NON-NLS-1$
      					stepping?
  								TrackerRes.getString("AutoTracker.Wizard.Button.Stop"): //$NON-NLS-1$
  								TrackerRes.getString("AutoTracker.Wizard.Button.Search")); //$NON-NLS-1$);
      	}
      });
      startPanel.add(startButton);
      searchThisButton = new JButton();
      searchThisButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	neverPause = (e.getModifiers()>16);
        	search(true, false); // search this frame and stop
        }
      });
      searchThisButton.addKeyListener(kl);
      startPanel.add(searchThisButton);
      searchNextButton = new JButton();
      searchNextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	neverPause = (e.getModifiers()>16);
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

      SpinnerModel model = new SpinnerNumberModel(defaultEvolveRate, 0, maxEvolveRate, maxEvolveRate/20);
      evolveSpinner = new TallSpinner(model, trackDropdown);
      evolveSpinner.addMouseListenerToAll(mouseOverListener);
      evolveSpinner.getTextField().setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
      	public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
      		JFormattedTextField.AbstractFormatter formatter
      				= new JFormattedTextField.AbstractFormatter() {
      			public String valueToString(Object value) throws ParseException {
      				return value.toString()+"%"; //$NON-NLS-1$
      			}
      			public Object stringToValue(String text) throws ParseException {
      				return Integer.parseInt(text.substring(0, text.length()-1));
      			}
      		};
      		return formatter;
      	}
      });

      ChangeListener listener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        	if (ignoreChanges) return;
        	Integer i = (Integer)evolveSpinner.getValue();
        	setAlphaFromRate(i);
        	int n = trackerPanel.getFrameNumber();
        	FrameData frame = getFrame(n);
        	buildEvolvedTemplate(frame);
        	if (frame.isKeyFrame())
        		refreshKeyFrame((KeyFrame)frame);
    			stop(true, false);
        	setChanged();
        }
      };
      evolveSpinner.addChangeListener(listener);
      setAlphaFromRate((Integer)evolveSpinner.getValue());

      acceptLabel = new JLabel();
      acceptLabel.setOpaque(false);
      acceptLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      model = new SpinnerNumberModel(goodMatch, possibleMatch, 10, 1);
      acceptSpinner = new TallSpinner(model, trackDropdown);
      acceptSpinner.addMouseListenerToAll(mouseOverListener);
      listener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        	goodMatch = (Integer)acceptSpinner.getValue();
        	setChanged();
        }
      };
      acceptSpinner.addChangeListener(listener);

      autoskipLabel = new JLabel();
      autoskipLabel.setOpaque(false);
      autoskipLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      model = new SpinnerNumberModel(autoskipCount, 0, 10, 1);
      autoskipSpinner = new TallSpinner(model, trackDropdown);
      autoskipSpinner.addMouseListenerToAll(mouseOverListener);
      listener = new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
              autoskipCount = (Integer)autoskipSpinner.getValue();
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
      oneDCheckbox.setSelected(lineSpread>=0);
      oneDCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	lineSpread = oneDCheckbox.isSelected()? 0: -1;
        	setChanged();
        	if (oneDCheckbox.isSelected()) {
        		int n = trackerPanel.getFrameNumber();
        		CoordAxes axes = trackerPanel.getAxes();
        		KeyFrame frame = getFrame(n).getKeyFrame();
        		if (frame!=null) {
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
      lookAheadCheckbox.setSelected(lookAhead);
      lookAheadCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	lookAhead = lookAheadCheckbox.isSelected();
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
      for (int i = 0; i<pointDropdown.getComponentCount(); i++) {
      	pointDropdown.getComponent(i).addMouseListener(mouseOverListener);
      }
      pointDropdown.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if ("refresh".equals(pointDropdown.getName())) return; //$NON-NLS-1$
          String item = (String)pointDropdown.getSelectedItem();
          if(item!=null) {
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
        	int n = trackerPanel.getFrameNumber();
          FrameData frame = getFrame(n);
          // build evolved template
          TemplateMatcher matcher = getTemplateMatcher();
        	matcher.setTemplate(frame.getTemplate());
        	matcher.setWorkingPixels(frame.getWorkingPixels());
        	buildEvolvedTemplate(frame);
        	// mark the target
    			marking = true;
        	TPoint p = getMatchTarget(frame.getMatchPoints()[0]);
          TTrack track = getTrack();
    			TPoint target = track.autoMarkAt(n, p.x, p.y);
    			frame.setAutoMarkPoint(target);
    			frame.decided = true;
    	    if (stepping && canStep()) {
    	    	paused = false;
	  	    	trackerPanel.getPlayer().step();
    	    }
    	    else {
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
//    		frame.setMatchIcon(null);
    		// step to the next frame if possible
    	    if (canStep()) {
    	    	paused = false;
	  	    	trackerPanel.getPlayer().step();
    	    }
    	    else {
	        	stop(true, false);
    	    }
        }
      });
      skipButton.addKeyListener(kl);

      final Action deleteKeyFrameAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        	int n = trackerPanel.getFrameNumber();
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
        	if (keyFrame!=null) { // earlier keyframe exists
	        	maskCenter.setLocation(keyFrame.getMaskPoints()[0]);
	        	maskCorner.setLocation(keyFrame.getMaskPoints()[1]);
        	}
        	else { // no earlier key frame, so clear all matches up to nextKey
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
        	refreshGUI();
        	AutoTracker.this.repaint();
        	trackerPanel.repaint();
        }
      };

    	final Action deleteThisAction = new AbstractAction() {
      	public void actionPerformed(ActionEvent e) {
      		// clear this match and step
        	int n = trackerPanel.getFrameNumber();
      		Map<Integer, FrameData> frameData = getFrameData();
      		FrameData frame = frameData.get(n);
      		if (!frame.isKeyFrame()) {
	        	frameData.get(n).clear();
	        	frameData.remove(n);
      		}
      		else {
      			frame.clear();
      		}

          TTrack track = getTrack();
        	boolean isAlwaysMarked = track.steps.isAutofill() || track instanceof CoordAxes;
        	if (!isAlwaysMarked && track.getSteps().length>n)
        		track.getSteps()[n] = null;
        	refreshGUI();
        	AutoTracker.this.repaint();
        	track.dataValid = false;
			  	track.firePropertyChange("data", null, track); //$NON-NLS-1$
      	}
    	};

    	final Action deleteLaterAction = new AbstractAction() {
      	public void actionPerformed(ActionEvent e) {
      		// clear later matches and steps
      		int n = trackerPanel.getFrameNumber();
      		ArrayList<Integer> toRemove = new ArrayList<Integer>();
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
        	refreshGUI();
        	AutoTracker.this.repaint();
        	track.dataValid = false;
			  	track.firePropertyChange("data", null, track); //$NON-NLS-1$
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

        	// now build the popup menu with suitable delete items
          JPopupMenu popup = new JPopupMenu();
          if (isKeyFrame) {
	          JMenuItem item = new JMenuItem(TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThisKeyFrame"));  //$NON-NLS-1$
	          popup.add(item);
	          item.addActionListener(deleteKeyFrameAction);
          }
          if (hasThis) {
	          JMenuItem item = new JMenuItem(isAlwaysMarked?
	          		TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThisMatch"): //$NON-NLS-1$
	          		TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteThis")); //$NON-NLS-1$
	          popup.add(item);
	          item.addActionListener(deleteThisAction);
          }
          if (hasLater) {
	          JMenuItem item = new JMenuItem(isAlwaysMarked?
	          		TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteLaterMatches"): //$NON-NLS-1$
	          		TrackerRes.getString("AutoTracker.Wizard.Menuitem.DeleteLater")); //$NON-NLS-1$
	          popup.add(item);
	          item.addActionListener(deleteLaterAction);
          }
          if (stepCount>0 && !(stepCount==1 && hasThis)) {
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
        	ArrayList<Integer> keyFrames = new ArrayList<Integer>();
      		Map<Integer, FrameData> frameData = getFrameData();
      		for (Integer i: frameData.keySet()) {
      			FrameData frame = frameData.get(i);
      			if (frame.isKeyFrame())
      				keyFrames.add(i);
      		}
        	Action keyAction = new AbstractAction() {
          	public void actionPerformed(ActionEvent e) {
	      			int i = Integer.parseInt(e.getActionCommand());
		        	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		        	trackerPanel.getPlayer().setStepNumber(clip.frameToStep(i));
           	}
        	};
          JPopupMenu popup = new JPopupMenu();
          for (Integer i: keyFrames) {
          	String s = TrackerRes.getString("AutoTracker.Label.Frame"); //$NON-NLS-1$
	          JMenuItem item = new JMenuItem(s+" "+i); //$NON-NLS-1$
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
      		if (textPaneSize!=null) return textPaneSize;
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
	  			if (track!=null) {
	  				int index = track.getTargetIndex();
	  				title += ": "+track.getName()+" "+track.getTargetDescription(index); //$NON-NLS-1$ //$NON-NLS-2$
	  			}
	  			setTitle(title);

		      frameLabel.setText(TrackerRes.getString("AutoTracker.Label.Frame")+" "+n+":"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		      searchLabel.setText(
		      		TrackerRes.getString("AutoTracker.Label.Search") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		      targetLabel.setText(
		      		TrackerRes.getString("AutoTracker.Label.Target")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		      templateLabel.setText(
		      		TrackerRes.getString("AutoTracker.Label.Template")+":"); //$NON-NLS-1$ //$NON-NLS-2$
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
		      matchImageLabel.setText(frame.getMatchIcon()==null? null:
		    		TrackerRes.getString("AutoTracker.Label.Match")); //$NON-NLS-1$
		      templateImageLabel.setText(keyFrame==null? null:
		      	TrackerRes.getString("AutoTracker.Label.Template")); //$NON-NLS-1$

		      if (trackerPanel.getVideo()!=null) {
		    		boolean running = stepping && !paused;
		            startButton.setIcon(stepping? stopIcon: searchIcon);
                    startButton.setText(stepping?
                            TrackerRes.getString("AutoTracker.Wizard.Button.Stop"):  //$NON-NLS-1$
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
		      Rectangle2D rect = font.getStringBounds(searchLabel.getText()+"   ", frc); //$NON-NLS-1$
		      w = Math.max(w, (int) rect.getWidth()+4);
		      rect = font.getStringBounds(frameLabel.getText()+"   ", frc); //$NON-NLS-1$
		      w = Math.max(w, (int) rect.getWidth()+4);
		      rect = font.getStringBounds(templateLabel.getText()+"   ", frc); //$NON-NLS-1$
		      w = Math.max(w, (int) rect.getWidth()+4);
		      rect = font.getStringBounds(targetLabel.getText()+"   ", frc); //$NON-NLS-1$
		      w = Math.max(w, (int) rect.getWidth()+4);
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
    	    int code = getStatusCode(n);
    	    KeyFrame keyFrame = frame.getKeyFrame();
	  			boolean initialized = keyFrame!=null && track!=null;
	  			boolean notStepping = paused || !stepping;
	  			boolean stable = frame.searched && !frame.newTemplateExists();
	  			boolean canSearchThis = !stable || code==5 || (changed&&code!=0) || (frame==keyFrame && frame.getMarkedPoint()==null);
        	startButton.setEnabled(initialized);
        	searchThisButton.setEnabled(initialized && notStepping && canSearchThis);
        	searchNextButton.setEnabled(initialized && canStep() && notStepping);

        	// refresh template image labels and panel
      		if (templateImageLabel.getIcon()==null && matchImageLabel.getIcon()==null) {
      			templateImageLabel.setText(TrackerRes.getString("AutoTracker.Label.NoTemplate")); //$NON-NLS-1$
      			matchImageLabel.setText(null);
      			imageToolbar.setPreferredSize(templateToolbar.getPreferredSize());
      			templateImageLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
      		}
      		else {
      			imageToolbar.setPreferredSize(null);
      			templateImageLabel.setBorder(null);
      		}

        	// refresh the delete and keyframe buttons
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

		      deleteButton.setEnabled(deleteButtonEnabled);
		      keyFrameButton.setEnabled(keyFrame!=null);

					// rebuild followup panel
					followupPanel.removeAll();
					if (code==2 || code==8) { // possible match
						acceptButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Accept")); //$NON-NLS-1$
          	followupPanel.add(acceptButton);
					}
					if (code==2 || code==3 || code==4 || code==8 || code==9) { // searched but not automarked
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
      if (mouseOverObj==templateToolbar || mouseOverObj==imageToolbar) {
      	// show mask and search
      	maskVisible = true;
      	targetVisible = searchVisible = false;
      }
      else if (mouseOverObj==targetToolbar) {
      	// show target
      	targetVisible = true;
      	searchVisible = maskVisible = false;
      }
      else if (mouseOverObj==searchToolbar) {
      	// show searchRect and mask
      	searchVisible = true;
      	targetVisible = maskVisible = false;
      }
      else {
      	searchVisible = targetVisible = maskVisible = true;
      }
    }

    /**
     * Refreshes the visible components of this wizard.
     */
    protected void refreshGUI() {
        TTrack track = getTrack();
    	if (track!=null && this.isVisible()) {
            track.setMarkByDefault(false);
        }
    	Runnable runner = new Runnable() {
    		public void run() {
    			if (trackerPanel==null) return;
    			refreshDropdowns();
                refreshStrings();
                refreshIcons();
                refreshButtons();
                refreshInfo();
                refreshDrawingFlags();
                pack();
                if (textPaneSize==null) {
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
      for (TTrack next: trackerPanel.getTracks()) {
      	if (!next.isAutoTrackable()) continue;
      	Icon icon = next.getFootprint().getIcon(21, 16);
        Object[] item = new Object[] {icon, next.getName()};
        trackDropdown.addItem(item);
        if (next==track) {
        	toSelect = item;
        }
      }
      if (track==null) {
      	Object[] emptyItem = new Object[] {null, "           "}; //$NON-NLS-1$
      	trackDropdown.insertItemAt(emptyItem, 0);
      	toSelect = emptyItem;
      }
      // select desired item
      if (toSelect!=null) {
      	trackDropdown.setSelectedItem(toSelect);
      }
      trackDropdown.setName(null);

      // refresh pointDropdown
      toSelect = null;
      pointDropdown.setName("refresh"); //$NON-NLS-1$
      pointDropdown.removeAllItems();
	  	if (track!=null) {
	  		int target = track.getTargetIndex();
	  		toSelect = track.getTargetDescription(target);
		  	for (int i = 0; i<track.getStepLength(); i++) {
		  		String s = track.getTargetDescription(i);
		  		if (track.isAutoTrackable(i) && s!=null) {
		      	pointDropdown.addItem(s);
		  		}
		  	}
	  	}
	  	else {
      	pointDropdown.addItem("         "); //$NON-NLS-1$
	  	}
	  	if (toSelect!=null) {
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
    			if (getTemplateMatcher()==null || track==null) {
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
  	    	if (icon==null) {
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
    			if (trackerPanel.getVideo()==null || track==null) {
    				templateImageLabel.setIcon(null);
    				matchImageLabel.setIcon(null);
    				return;
    			}
    			keyFrame.setTemplateMatcher(null); // triggers creation of new matcher
    			TemplateMatcher matcher = getTemplateMatcher(); // creates new matcher
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
      if (track==null) {
      	textPane.setText(TrackerRes.getString("AutoTracker.Info.SelectTrack")); //$NON-NLS-1$
      	return;
    	}

      // blue instructions if no key frame
      int n = trackerPanel.getFrameNumber();
      FrameData frame = getFrame(n);
      KeyFrame keyFrame = frame.getKeyFrame();
      if (keyFrame==null) {
      	String s = TrackerRes.getString("AutoTracker.Info.GetStarted"); //$NON-NLS-1$
      	s += " "+TrackerRes.getString("AutoTracker.Info.MouseOver.Instructions"); //$NON-NLS-1$ //$NON-NLS-2$
      	textPane.setText(s);
      	if (mouseOverObj==null) return;
      }

      // colored instructions if mouseOverObj not null
      textPane.setForeground(new Color(140, 80, 80));
      if (mouseOverObj==templateToolbar || mouseOverObj==imageToolbar) {
	    	textPane.setText(getTemplateInstructions());
	      return;
      }
      if (mouseOverObj==targetToolbar) {
	    	textPane.setText(getTargetInstructions());
	      return;
      }
      if (mouseOverObj==searchToolbar) {
	    	textPane.setText(getSearchInstructions());
	      return;
      }

    	//  actively searching: show frame status
      textPane.setForeground(Color.blue);
      int code = getStatusCode(n);
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
     	if (lineSpread>=0)
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
     * @param code the status code (integer 0-9)
     * @param n the frame number
     * @param peakWidthAndHeight the match data
     * @return the status text
     */
    protected String getStatusInfo(int code, int n, double[] peakWidthAndHeight) {
    	StringBuffer buf = new StringBuffer();
    	buf.append(TrackerRes.getString("AutoTracker.Info.Frame")+" "+n); //$NON-NLS-1$ //$NON-NLS-2$
    	switch(code) {
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
	      	buf.append(" ("+TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
	      	buf.append(" "+format.format(peakWidthAndHeight[1])+"): "); //$NON-NLS-1$ //$NON-NLS-2$
	      	buf.append(TrackerRes.getString("AutoTracker.Info.Match")); //$NON-NLS-1$
	      	break;
	    	case 2: // possible match was found, not marked
	      	textPane.setForeground(Color.red);
	      	buf.append(" ("+TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
	      	buf.append(" "+format.format(peakWidthAndHeight[1])+"): "); //$NON-NLS-1$ //$NON-NLS-2$
	      	if (lineSpread>=0) {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.PossibleOnAxis")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Accept")); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.Possible")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Accept")); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
      		if (canStep())
      			buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
	    	case 3: // no match was found
	      	textPane.setForeground(Color.red);
	      	buf.append(": "); //$NON-NLS-1$
	      	if (lineSpread>=0) {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
      		if (canStep())
      			buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
	    	case 4: // searchRect failed (no video image or x-axis inside)
	      	textPane.setForeground(Color.red);
	      	buf.append(": "); //$NON-NLS-1$
	      	if (lineSpread>=0) { // 1D tracking
		      	buf.append(TrackerRes.getString("AutoTracker.Info.OutsideXAxis")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else { // 2D tracking
		      	buf.append(TrackerRes.getString("AutoTracker.Info.Outside")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Mark")); //$NON-NLS-1$ //$NON-NLS-2$
      		if (canStep())
      			buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Skip")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
	    	case 5: // target marked manually
	      	textPane.setForeground(Color.blue);
	      	buf.append(": "); //$NON-NLS-1$
	      	buf.append(TrackerRes.getString("AutoTracker.Info.MarkedByUser")); //$NON-NLS-1$
	      	break;
	    	case 6: // match accepted
	      	textPane.setForeground(Color.green.darker());
	      	buf.append(" ("+TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
	      	buf.append(" "+format.format(peakWidthAndHeight[1])+"): "); //$NON-NLS-1$ //$NON-NLS-2$
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
	      	buf.append(" ("+TrackerRes.getString("AutoTracker.Info.MatchScore")); //$NON-NLS-1$ //$NON-NLS-2$
	      	buf.append(" "+format.format(peakWidthAndHeight[1])+"): "); //$NON-NLS-1$ //$NON-NLS-2$
	      	if (lineSpread>=0) {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.PossibleReplace")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Replace")); //$NON-NLS-1$ //$NON-NLS-2$
	      		buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.PossibleReplace")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Replace")); //$NON-NLS-1$ //$NON-NLS-2$
	      		buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
	    	case 9: //  no match found, existing mark or calibration tool
	      	textPane.setForeground(Color.red);
	      	buf.append(": "); //$NON-NLS-1$
	      	if (lineSpread>=0) {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
      		if (canStep())
	      		buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
	    	case 10: // no match found, already marked
	      	textPane.setForeground(Color.red);
	      	buf.append(": "); //$NON-NLS-1$
	      	if (lineSpread>=0) {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatchOnAxis")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.RetryOnAxis")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
	      	else {
		      	buf.append(TrackerRes.getString("AutoTracker.Info.NoMatch")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
				  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Retry")); //$NON-NLS-1$ //$NON-NLS-2$
	      	}
      		if (canStep())
	      		buf.append("\n"+TrackerRes.getString("AutoTracker.Info.Keep")); //$NON-NLS-1$ //$NON-NLS-2$
			  	buf.append("\n"+TrackerRes.getString("AutoTracker.Info.NewKeyFrame")); //$NON-NLS-1$ //$NON-NLS-2$
	      	break;
    	}
    	return buf.toString();
    }

    protected void prepareForFixedSearch(boolean fixed) {
    	ignoreChanges = true;
    	if (fixed) {
    		prevEvolution = (Integer)evolveSpinner.getValue();
    		prevLookAhead = lookAheadCheckbox.isSelected();
    		prevOneD = oneDCheckbox.isSelected();
    		isPrevValid = true;
    		evolveSpinner.setValue(0);
    		lookAheadCheckbox.setSelected(false);
    		oneDCheckbox.setSelected(false);
    	}
    	else if (isPrevValid) {
    		isPrevValid = false;
    		evolveSpinner.setValue(prevEvolution);
    		lookAheadCheckbox.setSelected(prevLookAhead);
    		oneDCheckbox.setSelected(prevOneD);
    	}
    	evolveSpinner.setEnabled(!fixed);
    	evolveRateLabel.setEnabled(!fixed);
    	lookAheadCheckbox.setEnabled(!fixed);
  		oneDCheckbox.setEnabled(!fixed);
      JFormattedTextField tf = ((JSpinner.DefaultEditor)evolveSpinner.getEditor()).getTextField();
    	tf.setDisabledTextColor(fixed? Color.GRAY.brighter(): Color.BLACK);
    	ignoreChanges = false;
    }

  }

  /**
   * Gets the match data as a delimited string with "columns" for frame number, match score,
   * target x and target y.
   */
  protected String getMatchDataString() {
  	// create string buffer to collect match score data
    StringBuffer buf = new StringBuffer();
    buf.append(getTrack().getName()+"_"+wizard.pointDropdown.getSelectedItem()); //$NON-NLS-1$
    buf.append(XML.NEW_LINE);
    buf.append(TrackerRes.getString("ThumbnailDialog.Label.FrameNumber")+TrackerIO.getDelimiter()+TrackerRes.getString("AutoTracker.Match.Score")); //$NON-NLS-1$ //$NON-NLS-2$
    String tar = "_"+TrackerRes.getString("AutoTracker.Label.Target").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
    buf.append(TrackerIO.getDelimiter()+"x"+tar+TrackerIO.getDelimiter()+"y"+tar); //$NON-NLS-1$ //$NON-NLS-2$
    buf.append(XML.NEW_LINE);
  	Map<Integer, FrameData> frameData = getFrameData();
  	NumberFormat scoreFormat = NumberFormat.getInstance();
  	scoreFormat.setMaximumFractionDigits(1);
  	scoreFormat.setMinimumFractionDigits(1);
  	DecimalFormat xFormat = (DecimalFormat)NumberFormat.getInstance();
  	DecimalFormat yFormat = (DecimalFormat)NumberFormat.getInstance();
  	DataTable table = null;
  	TableCellRenderer xRenderer = null, yRenderer = null;
  	TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
    TreeMap<Integer, TableTrackView> dataViews = menubar.getDataViews();
    for (int key: dataViews.keySet()) {
  		TableTrackView view = dataViews.get(key);
  		if (view.getTrack()==getTrack()) {
    		table = view.getDataTable();
      	String pattern = table.getFormatPattern("x"); //$NON-NLS-1$
      	if (pattern==null || pattern.equals("")) { //$NON-NLS-1$
        	xRenderer = table.getDefaultRenderer(Double.class);
      	}
       	else {
       		xFormat.applyPattern(pattern);
       	}
      	pattern = table.getFormatPattern("y"); //$NON-NLS-1$
      	if (pattern==null || pattern.equals("")) { //$NON-NLS-1$
        	yRenderer = table.getDefaultRenderer(Double.class);
      	}
       	else {
       		yFormat.applyPattern(pattern);
       	}
      	break;
  		}
    }
  	for (Integer i: frameData.keySet()) {
  		FrameData next = frameData.get(i);
  		if (next==null || next.getMatchWidthAndHeight()==null) continue;
  		double score = next.getMatchWidthAndHeight()[1];
  		String value = Double.isInfinite(score)? String.valueOf(score): scoreFormat.format(score);
      buf.append(next.getFrameNumber()+TrackerIO.getDelimiter()+value);
      TPoint[] pts = next.getMatchPoints();
      if (pts!=null) {
      	TPoint p = pts[0]; // center of the match
      	p = getMatchTarget(p); // target position
      	Point2D pt = p.getWorldPosition(trackerPanel);
      	String xval = xFormat.format(pt.getX());
      	String yval = yFormat.format(pt.getY());
      	if (xRenderer!=null) {
	        Component c = xRenderer.getTableCellRendererComponent(table, pt.getX(), false, false, 0, 0);
	        if (c instanceof JLabel) {
	        	xval = ((JLabel)c).getText().trim();
	        }
      	}
      	if (yRenderer!=null) {
	        Component c = yRenderer.getTableCellRendererComponent(table, pt.getY(), false, false, 0, 0);
	        if (c instanceof JLabel) {
	        	yval = ((JLabel)c).getText().trim();
	        }
      	}
      	buf.append(TrackerIO.getDelimiter()+xval+TrackerIO.getDelimiter()+yval);
      }
      buf.append(XML.NEW_LINE);
  	}
  	return buf.toString();
  }

}
