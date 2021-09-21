/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

/**
 * A Step is associated with a single frame of a TTrack. It contains an array of
 * TPoints that define its image data and a Footprint that determines its screen
 * appearance. This is an abstract class and cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
public abstract class Step implements Cloneable {

	// static fields
	protected final static Rectangle hitRect = new Rectangle(-4, -4, 8, 8);
	protected static Shape selectionShape;
	protected static Stroke selectionStroke;
	protected final static AffineTransform transform = new AffineTransform();
	protected final static NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
	static {
		selectionStroke = new BasicStroke(2);
		selectionShape = (Rectangle)hitRect.clone();
		format.setMinimumIntegerDigits(1);
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(2);
	}

	// instance fields
	protected int trackID; // ID number of track this belongs to
	protected int n; // frame number
	protected Footprint footprint; // determines appearance
	protected TPoint[] points; // defines image data
	protected Point[] screenPoints; // for transform conversions
	protected boolean valid; // invalid until drawn except for point mass
	protected Map<Integer, Mark> panelMarks // tracker panel to Mark
			= new HashMap<Integer, Mark>();
	protected int defaultIndex = 0; // array index of default TPoint
	protected boolean dataVisible = true; // true if visible in plots, tables
	public int type = Step.TYPE_UNKNOWN;
	public final static int TYPE_ACCELERATION = 2;
	public final static int TYPE_VELOCITY = 1;
	public final static int TYPE_UNKNOWN = 0;

	/**
	 * Constructs a Step with the specified frame number.
	 *
	 * @param track the track
	 * @param n     the frame number
	 */
	protected Step(TTrack track, int n) {
		trackID = track.getID();
		this.n = n;
	}

	/**
	 * Gets the frame number.
	 *
	 * @return the frame number
	 */
	public int getFrameNumber() {
		return n;
	}

	/**
	 * Sets the footprint.
	 *
	 * @param footprint the footprint
	 */
	public void setFootprint(Footprint footprint) {
		this.footprint = footprint;
	}

	/**
	 * Gets the track.
	 *
	 * @return the track
	 */
	public TTrack getTrack() {
		return TTrack.getTrack(trackID);
	}

	/**
	 * Gets the array of TPoints contained in this step.
	 *
	 * @return the TPoints array
	 */
	public TPoint[] getPoints() {
		return points;
	}

	/**
	 * Gets the index of a point in the points[] array.
	 * 
	 * @param p the point
	 * @return the index, or -1 if not found
	 */
	public int getPointIndex(TPoint p) {
		for (int i = 0; i < points.length; i++) {
			if (points[i] == p)
				return i;
		}
		return -1;
	}

	/**
	 * Gets the default point. The default point is the point initially selected
	 * when the step is created.
	 *
	 * @return the default TPoint
	 */
	public TPoint getDefaultPoint() {
		return points[defaultIndex];
	}

	/**
	 * Sets the default point index. This defines the index of the points array used
	 * to get the point initially selected when the step is created.
	 *
	 * @param index the index
	 */
	public void setDefaultPointIndex(int index) {
		index = Math.min(index, points.length - 1);
		defaultIndex = Math.max(0, index);
	}

	/**
	 * Erases this on the specified tracker panel. Erasing adds the current bounds
	 * to the dirty region and nulls the step's mark to trigger creation of a new
	 * one.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void erase(Integer panelID) {
		if (panelMarks.get(panelID) == null)
			return; // already dirty
		panel(panelID).addDirtyRegion(null);//getBounds(trackerPanel)); // old bounds
		panelMarks.put(panelID, null); // triggers new mark
	}

	/**
	 * Erases and remarks this on the specified tracker panel. Remarking creates a
	 * new mark for the step and adds both the old and new bounds to the tracker
	 * panel's dirty region.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void remark(Integer panelID) {
		erase(panelID);
		panel(panelID).addDirtyRegion(null);//getBounds(trackerPanel)); // new bounds
	}

	/**
	 * Repaints this on the specified tracker panel. Repainting a step first remarks
	 * it and then requests a repaint of the panel's dirty region.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void repaint(Integer panelID) {
		remark(panelID);
		panel(panelID).repaintDirtyRegion();
	}

	/**
	 * Erases this on all tracker panels.
	 */
	public void erase() {
		if (panelMarks.isEmpty())
			return;
		Iterator<Integer> panelIDs = panelMarks.keySet().iterator();
		while (panelIDs.hasNext())
			erase(panelIDs.next());
	}

	/**
	 * Remarks this on all tracker panels.
	 */
	public void remark() {
		if (panelMarks.isEmpty())
			return;
		Iterator<Integer> panelIDs = panelMarks.keySet().iterator();
		while (panelIDs.hasNext())
			remark(panelIDs.next());
	}

	/**
	 * Repaints this on all tracker panels.
	 */
	public void repaint() {
		Iterator<Integer> panelIDs = panelMarks.keySet().iterator();
		while (panelIDs.hasNext())
			repaint(panelIDs.next());
	}

	private TrackerPanel panel(Integer panelID) {
		if (getTrack() == null)
			System.out.println("OHOH");
		return getTrack().panel(panelID);
	}

	/**
	 * Disposes of this step.
	 */
	protected void dispose() {
		panelMarks.clear();
	}

	/**
	 * Draws this step.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param g     the graphics context on which to draw
	 */
	public void draw(DrawingPanel panel, Graphics g) {
		TTrack track = getTrack();
		AutoTracker t;
		if (track.tp == panel && (t = track.tp.getAutoTracker(false))
				!= null && t.isInteracting(track)) {
			return;
		}
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		boolean highlighted = (trackerPanel.getFrameNumber() == n);
		if (trackerPanel.autoTracker != null && trackerPanel.autoTracker.getWizard().isVisible()
				&& trackerPanel.autoTracker.getTrack() == track) {
			highlighted = false;
		}
		getMark(trackerPanel).draw((Graphics2D) g, highlighted);
	}

	/**
	 * Finds the Interactive located at the specified pixel position.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position
	 * @param ypix  the y pixel position
	 * @return the TPoint that is hit, or null
	 */
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		TTrack track = getTrack();
		boolean highlighted = track.tp.getFrameNumber() == getFrameNumber();
		AutoTracker autoTracker = track.tp.getAutoTracker(false);
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		setHitRectCenter(xpix, ypix);
		for (int i = 0; i < points.length; i++) {
			if (points[i] == null || Double.isNaN(points[i].getX()))
				continue;
			if (hitRect.contains(points[i].getScreenPosition(trackerPanel))) {
				if (highlighted && autoTracker != null && autoTracker.isDrawingKeyFrameFor(track, i))
					return null;
				return points[i];
			}
		}
		return null;
	}

	/**
	 * Gets the mark for the specified panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the mark
	 */
	protected Mark getMark(TrackerPanel trackerPanel) {
		Mark mark = panelMarks.get(trackerPanel.getID());
		TPoint selection = null;
		if (mark == null) {
			selection = trackerPanel.getSelectedPoint();
			Point p = null;
			valid = true; // assume true
			for (int n = 0; n < points.length; n++) {
				if (!valid)
					continue;
				// determine if point is valid (ie not NaN)
				valid = valid && !Double.isNaN(points[n].getX()) && !Double.isNaN(points[n].getY());
				screenPoints[n] = points[n].getScreenPosition(trackerPanel);
				if (valid && selection == points[n])
					p = screenPoints[n];
			}
			mark = footprint.getMark(screenPoints);
			
			if (p != null) { // point is selected, so draw selection shape
				transform.setToTranslation(p.x, p.y);
				int scale = FontSizer.getIntegerFactor();
				if (scale > 1) {
					transform.scale(scale, scale);
				}
				Color color = footprint.getColor();
				Mark stepMark = mark;
				MultiShape selectedShape = new MultiShape(transform.createTransformedShape(selectionShape)).andStroke(selectionStroke);
				mark = new Mark() {
					@Override
					public void draw(Graphics2D g, boolean highlighted) {
						stepMark.draw(g, false);
						Paint gpaint = g.getPaint();
						g.setPaint(color);
						selectedShape.draw(g);
						g.setPaint(gpaint);
					}
				};
			}
			final Mark theMark = mark;
			mark = new Mark() {
				@Override
				public void draw(Graphics2D g, boolean highlighted) {
					if (!valid)
						return;
					theMark.draw(g, false);
				}
			};
			panelMarks.put(trackerPanel.getID(), mark);
		}
		return mark;
	}

	/**
	 * Returns a String describing this step.
	 *
	 * @return a descriptive string
	 */
	@Override
	public String toString() {
		return "Step " + n; //$NON-NLS-1$
	}

	/**
	 * Clones this Step.
	 *
	 * @return a clone of this step
	 */
	@Override
	public Object clone() {
		try {
			Step step = (Step) super.clone();
			step.points = new TPoint[points.length];
			step.screenPoints = new Point[points.length];
			step.panelMarks = new HashMap<Integer, Mark>();
			return step;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Centers the hit testing rectangle on the specified screen point.
	 *
	 * @param xpix the x pixel position
	 * @param ypix the y pixel position
	 */
	protected void setHitRectCenter(int xpix, int ypix) {
		hitRect.setLocation(xpix - hitRect.width / 2, ypix - hitRect.height / 2);
	}

	/**
	 * Gets the step length. Default length is 1.
	 *
	 * @return the length of the points array
	 */
	public static int getLength() {
		return 1;
	}

	/**
	 * An inner superclass of all handles.
	 */
	@SuppressWarnings("serial")
	static class Handle extends TPoint {

		/**
		 * Constructs a Handle with specified image coordinates.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public Handle(double x, double y) {
			super(x, y);
		}

		/**
		 * Sets the position of this handle on the line nearest the specified screen
		 * position. Subclasses must override.
		 *
		 * @param xScreen      the x screen position
		 * @param yScreen      the y screen position
		 * @param trackerPanel the trackerPanel drawing this step
		 */
		public void setPositionOnLine(int xScreen, int yScreen, TrackerPanel trackerPanel) {
		}

	}
}
