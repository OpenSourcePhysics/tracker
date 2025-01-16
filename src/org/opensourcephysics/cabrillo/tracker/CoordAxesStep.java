/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a Step for a CoordAxes. It is used for displaying the axes and for
 * setting the origin, angle and/or scale of an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class CoordAxesStep extends Step {

	// instance fields
	private Origin origin;
	private Handle handle;
	private boolean originEnabled = true;
	private boolean handleEnabled = true;
	private Map<Integer, Shape> panelHandleShapes = new HashMap<Integer, Shape>();
	private GeneralPath path = new GeneralPath();

	/**
	 * Constructs an AxesStep.
	 *
	 * @param track the track
	 * @param n     the frame number
	 */
	public CoordAxesStep(CoordAxes track, int n) {
		super(track, n);
		origin = new Origin();
		origin.setCoordsEditTrigger(true);
		handle = new Handle();
		handle.setCoordsEditTrigger(true);
		points = new TPoint[] { origin, handle }; // origin is "default" point
		screenPoints = new Point[1];
	}

	/**
	 * Gets the origin.
	 *
	 * @return the origin
	 */
	public TPoint getOrigin() {
		return origin;
	}

	/**
	 * Gets the handle.
	 *
	 * @return the origin
	 */
	public TPoint getHandle() {
		return handle;
	}

	/**
	 * /** Enables and disables the interactivity of the origin.
	 *
	 * @param enabled <code>true</code> to enable the origin
	 */
	public void setOriginEnabled(boolean enabled) {
		originEnabled = enabled;
	}

	/**
	 * Gets whether the origin is enabled.
	 *
	 * @return <code>true</code> if the origin is enabled
	 */
	public boolean isOriginEnabled() {
		return originEnabled;
	}

	/**
	 * Enables and disables the interactivity of the handle.
	 *
	 * @param enabled <code>true</code> to enable the handle
	 */
	public void setHandleEnabled(boolean enabled) {
		handleEnabled = enabled;
	}

	/**
	 * Gets whether the handle is enabled.
	 *
	 * @return <code>true</code> if the handle is enabled
	 */
	public boolean isHandleEnabled() {
		return handleEnabled;
	}

	/**
	 * Overrides Step findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position
	 * @param ypix  the y pixel position
	 * @return the TPoint that is hit, or null
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		setHitRectCenter(xpix, ypix);
		TTrack track = getTrack();
		AutoTracker autoTracker = (track.tp == null ? null : track.tp.getAutoTracker(false));
		if (handleEnabled) {
			Shape hitShape = panelHandleShapes.get(trackerPanel.getID());
			if (hitShape != null && hitShape.intersects(hitRect)) {
//				if (handle.x == 0 || handle.y == 0)
				handle.setPositionOnLine(xpix, ypix, trackerPanel);
				return (autoTracker != null 
						&& autoTracker.getTrack() == track 
						&& track.getTargetIndex() == 1
						&& autoTracker.isOnKeyFrame(track.tp) ? null : handle);
			}
		}
		if (originEnabled && !track.isLocked() && super.findInteractive(panel, xpix, ypix) == origin) {
			return (autoTracker != null 
					&& autoTracker.getTrack() == track 
					&& track.getTargetIndex() == 0
					&& autoTracker.isOnKeyFrame(track.tp) ? null : origin); 
		}
		return null;
	}

	/**
	 * Overrides Step draw method.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		TTrack track = getTrack();
		if (track.tp == panel) {
			AutoTracker autoTracker = track.tp.getAutoTracker(false);
			if (autoTracker != null && autoTracker.isInteracting(track))
				return;
		}
		// draw the axes
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Graphics2D g = (Graphics2D) _g;
		getMark(trackerPanel).draw(g, false); // no highlight
	}

	/**
	 * Overrides Step getMark method.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the mark
	 */
	@Override
	protected Mark getMark(TrackerPanel trackerPanel) {
		Mark mark = panelMarks.get(trackerPanel.getID());
		if (mark == null) {
			TPoint selection = trackerPanel.getSelectedPoint();
			// set origin location to coords origin
			ImageCoordSystem coords = trackerPanel.getCoords();
			int n = trackerPanel.getFrameNumber();
			TTrack track = getTrack();
			if (track.tp != null)
				n = track.tp.getFrameNumber();
			double x = coords.getOriginX(n);
			double y = coords.getOriginY(n);
			// check for NaN (may occur after video is loaded in Tracker online)
			if (Double.isNaN(x) || Double.isNaN(y)) {
				x = trackerPanel.getImageWidth() / 2;
				y = trackerPanel.getImageHeight() / 2;
				coords.setOriginXY(n, x, y);
			}
			origin.setLocation(x, y);
			// get default axes shape and handle hit shape (positive x-axis)
			Point p0 = screenPoints[0] = origin.getScreenPosition(trackerPanel);
			MultiShape axesShape = footprint.getShape(screenPoints, FontSizer.getIntegerFactor());
			path.reset();
			path.moveTo(p0.x + 15, p0.y);
			path.lineTo(p0.x + 500, p0.y);
			Shape hitShape = path;
			// rotate axes and x-axis hit shape about origin if drawing in image space
			if (trackerPanel.isDrawingInImageSpace()) {
				double angle = coords.getAngle(n);
				transform.setToRotation(-angle, p0.x, p0.y);
				axesShape = axesShape.transform(transform);
				hitShape = transform.createTransformedShape(hitShape);
			}
			panelHandleShapes.put(trackerPanel.getID(), hitShape);
			// get selected point shape, if any
			int scale = FontSizer.getIntegerFactor();
			Shape selectedShape = null;
			if (selection == origin) {
				transform.setToTranslation(p0.x, p0.y);
				if (scale > 1) {
					transform.scale(scale, scale);
				}
				selectedShape = transform.createTransformedShape(selectionShape);
			} else if (selection == handle) {
				Point p1 = handle.getScreenPosition(trackerPanel);
				transform.setToTranslation(p1.x, p1.y);
				if (scale > 1) {
					transform.scale(scale, scale);
				}
				selectedShape = transform.createTransformedShape(selectionShape);
			}
			
			// create mark to draw fillShapes
			Color color = footprint.getColor();
			MultiShape shape = selectedShape == null? new MultiShape(axesShape).andFill(true)
					: new MultiShape(axesShape, selectedShape).andFill(true).andStroke(null, selectionStroke);
			
			// create mark to draw grid
			CoordAxes axes = (CoordAxes)track;
			Mark gridMark = axes.grid.getMark(trackerPanel);

			mark = new Mark() {
				@Override
				public void draw(Graphics2D g, boolean highlighted) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setPaint(color);
					if (OSPRuntime.setRenderingHints)
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					// draw the grid first so axes are on top
					if (axes.gridVisible) {
						gridMark.draw(g2, false);
					}
					shape.draw(g2);
					g2.dispose();
				}
			};
			panelMarks.put(trackerPanel.getID(), mark);
		}
		return mark;
	}

	/**
	 * Overrides Step getPointIndex method.
	 *
	 * @return the index, or -1 if not found
	 */
	@Override
	public int getPointIndex(TPoint p) {
		int i = super.getPointIndex(p);
		if (i == -1) {
			if (p instanceof CoordAxes.OriginPoint)
				return 0;
			if (p instanceof CoordAxes.AnglePoint)
				return 1;
		}
		return i;
	}

	/**
	 * Clones this Step.
	 *
	 * @return a clone of this step
	 */
	@Override
	public Object clone() {
		CoordAxesStep step = (CoordAxesStep) super.clone();
		if (step != null) {
			step.panelHandleShapes = new HashMap<Integer, Shape>();
		}
		return step;
	}

	/**
	 * Returns a String describing this.
	 *
	 * @return a descriptive string
	 */
	@Override
	public String toString() {
		return "CoordAxesStep " + n; //$NON-NLS-1$
	}

	/**
	 * Gets the step length.
	 *
	 * @return the length of the points array
	 */
	public static int getLength() {
		return 2;
	}

	@Override
	protected void dispose() {
		panelHandleShapes.clear();
		super.dispose();
	}

	// ______________________ inner Origin class ________________________

	/**
	 * Inner class used to set the origin.
	 */
	class Origin extends TPoint {

		/**
		 * Overrides TPoint setXY method.
		 *
		 * @param x the x position
		 * @param y the y position
		 */
		@Override
		public void setXY(double x, double y) {
			CoordAxes axes = (CoordAxes) getTrack();
			if (axes.isLocked())
				return;
			if (isAdjusting()) {
				prevX = x;
				prevY = y;
			}
			super.setXY(x, y);
			TrackerPanel panel = axes.tp;
			if (panel != null) {
				ImageCoordSystem coords = panel.getCoords();
				coords.setAdjusting(isAdjusting());
				int n = panel.getFrameNumber();
				coords.setOriginXY(n, x, y);
				axes.xField.setValue(coords.getOriginX(n));
				axes.yField.setValue(coords.getOriginY(n));
			}
			if (isAdjusting()) {
				repaint();
			}
		}
		
		@Override
		public double getX() {
			return super.getX();
		}

		/**
		 * Overrides TPoint method.
		 *
		 * @param adjusting true if being dragged
		 */
		@Override
		public void setAdjusting(boolean adjusting, MouseEvent e) {
			boolean wasAdjusting = isAdjusting();
			if (wasAdjusting == adjusting)
				return;
			super.setAdjusting(adjusting, e);
			if (!wasAdjusting) {
				prevX = x;
				prevY = y;
			} else if (!java.lang.Double.isNaN(prevX)) {
				setXY(prevX, prevY);
				TTrack track = getTrack();
				track.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, track.tp.getFrameNumber()); //$NON-NLS-1$
			}
		}

	}

	// ______________________ inner Handle class ________________________

	class Handle extends TPoint {

		// instance fields
		private double angleIncrement = 0;
		protected Point2D.Double p = new Point2D.Double();

		/**
		 * Overrides TPoint setXY method to set the angle of the x axis.
		 *
		 * @param x the x position
		 * @param y the y position
		 */
		@Override
		public void setXY(double x, double y) {
			TTrack track = getTrack();
			if (track.isLocked())
				return;
			CoordAxes coordAxes = (CoordAxes) track;
			if (coordAxes.tp == null) {
				super.setXY(x, y);
				return;
			}
			if (angleIncrement >= Math.PI / 180) { // 1 degree of arc
				// place handle at same distance from origin at closest permitted angle
				p.setLocation(x, y);
				double d = origin.distance(p);
				double theta = origin.angle(p);
				int i = Math.round((float) (theta / angleIncrement));
				theta = i * angleIncrement;
				x = origin.getX() + d * Math.cos(theta);
				y = origin.getY() + d * Math.sin(theta);
			}
			if (isAdjusting()) {
				prevX = x;
				prevY = y;
			}
			super.setXY(x, y);
			double cos = origin.cos(this);
			double sin = origin.sin(this);
			ImageCoordSystem coords = coordAxes.tp.getCoords();
			coords.setAdjusting(isAdjusting());
			int n = coordAxes.tp.getFrameNumber();
			coords.setCosineSine(n, cos, sin);
			coordAxes.angleField.setValue(coords.getAngle(n));
			angleIncrement = 0;
			if (isAdjusting()) {
				repaint();
			}
		}

		/**
		 * Overrides TPoint setScreenPosition method.
		 *
		 * @param x        the screen x coordinate
		 * @param y        the screen y coordinate
		 * @param vidPanel the video panel
		 * @param e        the input event making the request
		 */
		@Override
		public void setScreenPosition(int x, int y, VideoPanel vidPanel, InputEvent e) {
			if (e == null) {
				angleIncrement = 0;
			} else if (e.isShiftDown()) {
				angleIncrement = Math.PI / 36; // 5 degrees
			} else {
				angleIncrement = 0;
			}
			setScreenPosition(x, y, vidPanel);
		}

		/**
		 * Overrides TPoint showCoordinates method so handle position can be set to
		 * mouse position when first selecting this handle
		 *
		 * @param vidPanel the video panel
		 */
		@Override
		public void showCoordinates(VideoPanel vidPanel) {
			if (vidPanel instanceof TrackerPanel) {
				TrackerPanel trackerPanel = (TrackerPanel) vidPanel;
				if (!(this == trackerPanel.getSelectedPoint())) {
					// start by setting location to mouse point
					setLocation(vidPanel.getMouseX(), vidPanel.getMouseY());
					// then move to nearest point on x-axis
					Point2D p = getWorldPosition(vidPanel);
					p.setLocation(p.getX(), 0); // move to y = 0
					int n = vidPanel.getFrameNumber();
					AffineTransform toImage = vidPanel.getCoords().getToImageTransform(n);
					toImage.transform(p, p);
					setLocation(p);
				}
			}
			super.showCoordinates(vidPanel);
		}

		/**
		 * Overrides TPoint method.
		 *
		 * @param adjusting true if being dragged
		 */
		@Override
		public void setAdjusting(boolean adjusting, MouseEvent e) {
			boolean wasAdjusting = isAdjusting();
			super.setAdjusting(adjusting, e);
			if (wasAdjusting && !adjusting && !java.lang.Double.isNaN(prevX)) {
				if (e != null && e.getID() != MouseEvent.MOUSE_RELEASED) {
					// first time selected
					if (prevX == 0 && prevY == 0) {
						angleIncrement = Math.PI / 9; // keep it at 0!
						setScreenPosition(e.getX(), e.getY(), CoordAxesStep.this.getTrack().tp);						
					}
				}
				else {
					setXY(prevX, prevY);
					TTrack track = getTrack();
					track.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, track.tp.getFrameNumber()); //$NON-NLS-1$
				}
			}
		}
		
		/**
		 * Sets the position of this handle on the line nearest the specified screen
		 * position.
		 *
		 * @param xScreen      the x screen position
		 * @param yScreen      the y screen position
		 * @param trackerPanel the trackerPanel drawing this step
		 */
		public void setPositionOnLine(int xScreen, int yScreen, TrackerPanel trackerPanel) {
			double d = 100;
			double theta = trackerPanel.getCoords().getAngle(trackerPanel.getFrameNumber());
			p.setLocation(origin.x + d*Math.cos(theta), origin.y - d*Math.sin(theta));
			TPoint endPt = new TPoint(p);
			setPositionOnLine(xScreen, yScreen, trackerPanel, origin, endPt);
			repaint();
		}

	}
}
