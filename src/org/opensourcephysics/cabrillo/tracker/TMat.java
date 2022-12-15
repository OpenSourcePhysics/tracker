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

import java.beans.*;
import java.awt.*;
import java.awt.geom.*;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a trackable rectangular background mat that draws itself on a tracker
 * panel behind the video.
 *
 * @author Douglas Brown
 */
public class TMat implements Measurable, Trackable, PropertyChangeListener {

	// instance fields
	private TFrame frame;
	private Integer panelID;
	/**
	 * the cleared rectangle behind the video
	 */
	private Rectangle mat;
	/**
	 * The bounds of the video after world transformation
	 */
	private Rectangle2D worldBounds;
	private Paint paint = Color.white;
	private boolean visible = true;
	private boolean isValidMeasure = false;
	private ImageCoordSystem coords;
	protected Rectangle2D drawnBounds;

	private AffineTransform trTM = new AffineTransform();
	private boolean haveVideo;

	/**
	 * Creates a mat for the specified tracker panel
	 *
	 * @param panel the tracker panel
	 */
	public TMat(TrackerPanel panel) {
		mat = new Rectangle();
		setTrackerPanel(panel);
		refresh();
	}

	public void setTrackerPanel(TrackerPanel panel) {
		if (panel == null || panelID == panel.getID())
			return;
		frame = panel.getTFrame();
		panelID = panel.getID();
		panel = frame.getTrackerPanelForID(panelID);
		panel.addPropertyChangeListener(Video.PROPERTY_VIDEO_COORDS, this); // $NON-NLS-1$
		refreshCoords(panel);
	}

	/**
	 * Draws the image mat on the panel.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param g     the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (!(panel instanceof VideoPanel) || !isVisible())
			return;
		VideoPanel vidPanel = (VideoPanel) panel;
		Graphics2D g2 = (Graphics2D) g.create();
		// transform world to screen
		g2.transform(vidPanel.getPixelTransform(trTM));
		// transform image to world if not drawing in image space
		if (!vidPanel.isDrawingInImageSpace()) {
			ImageCoordSystem coords = vidPanel.getCoords();
			int n = vidPanel.getFrameNumber();
			g2.transform(coords.getToWorldTransform(n));
		}
		// draw the mat
		g2.setPaint(paint);
		g2.fill(mat);
		// restore graphics transform and paint
		// save drawing bounds for rendering
		drawnBounds = vidPanel.transformShape(mat).getBounds2D();
		g2.dispose();
	}

	protected Rectangle2D getDrawingBounds() {
		return drawnBounds;
	}


	/**
	 * Gets the paint.
	 *
	 * @return the paint used to draw the mat
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * Sets the paint.
	 *
	 * @param paint the desired paint
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	/**
	 * Shows or hides this mat.
	 *
	 * @param visible <code>true</code> to show this mat.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Gets the visibility of this mat.
	 *
	 * @return <code>true</code> if this mat is visible
	 */
	public boolean isVisible() {
//    boolean noVid = (trackerPanel.getVideo() == null || !trackerPanel.getVideo().isVisible());
//    return noVid && visible;
		return visible;
	}

	/**
	 * Gets the minimum x needed to draw this object.
	 *
	 * @return minimum x
	 */
	@Override
	public double getXMin() {
		if (!isValidMeasure)
			getWorldBounds();
		return worldBounds.getMinX();
	}

	/**
	 * Gets the maximum x needed to draw this object.
	 *
	 * @return maximum x
	 */
	@Override
	public double getXMax() {
		if (!isValidMeasure)
			getWorldBounds();
		return worldBounds.getMaxX();
	}

	/**
	 * Gets the minimum y needed to draw this object.
	 *
	 * @return minimum y
	 */
	@Override
	public double getYMin() {
		if (!isValidMeasure)
			getWorldBounds();
		return worldBounds.getMinY();
	}

	/**
	 * Gets the maximum y needed to draw this object.
	 *
	 * @return maximum y
	 */
	@Override
	public double getYMax() {
		if (!isValidMeasure)
			getWorldBounds();
		return worldBounds.getMaxY();
	}

	/**
	 * Reports whether information is available to set min/max values.
	 *
	 * @return <code>true</code> if min/max values are valid
	 */
	@Override
	public boolean isMeasured() {
		return isVisible();
	}

	/**
	 * Refreshes this mat.
	 */
	public void refresh() {
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		refreshCoords(panel);
		refreshMat(panel);
	}

	/**
	 * Remove and re-add coords ImageCoordSystem.PROPERTY_COORDS_TRANSFORM listener.
	 * 
	 * @param panel
	 */
	private void refreshCoords(TrackerPanel panel) {
		if (coords != null)
			coords.removePropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this); // $NON-NLS-1$
		coords = panel.getCoords();
		coords.addPropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this); // $NON-NLS-1$
	}

	/**
	 * Ensure that the mat rectangle is valid.
	 * 
	 * @param trackerPanel
	 */
	private void refreshMat(TrackerPanel trackerPanel) {
		Rectangle mat0 = new Rectangle(mat);
		mat.width = (int) trackerPanel.getImageWidth();
		mat.height = (int) trackerPanel.getImageHeight();
		int w = (int) TrackerPanel.getDefaultImageWidth();
		int h = (int) TrackerPanel.getDefaultImageHeight();
		Video video = trackerPanel.getVideo();
		if (video != null) {
			// BH It is possible that this is executing prior to JSMovieVideo obtaining its
			// first image, but still having known width and height. So do not call getImage() here.
			boolean useRaw = video instanceof ImageVideo 
					&& video.getFilterStack().isEmpty();			
			Dimension d = video.getImageSize(!useRaw);
			if (d.width > 0) {
				haveVideo = true;
				w = d.width;
				h = d.height;
			}
		}
		mat.x = Math.min((w - mat.width) / 2, 0);
		mat.y = Math.min((h - mat.height) / 2, 0);
		if (!mat0.equals(mat)) {
			invalidate();
			trackerPanel.scale();
		}
	}

	protected void checkVideo(TrackerPanel panel) {
		if (!haveVideo && panel.getVideo() != null)
			refreshMat(panel);		
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			invalidate();
			break;
		case Video.PROPERTY_VIDEO_COORDS:
			refresh();
			break;
		}
	}

	/**
	 * Cleans up this mat
	 */
	public void cleanup() {
		if (frame != null) {
			frame.getTrackerPanelForID(panelID).removePropertyChangeListener("coords", this); //$NON-NLS-1$
			coords.removePropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this); // $NON-NLS-1$
			panelID = null;
			frame = null;
		}

	}

//_______________________________ private methods _________________________

	/**
	 * Gets the world bounds of the mat.
	 */
	private void getWorldBounds() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		ImageCoordSystem coords = trackerPanel.getCoords();
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int stepCount = clip.getStepCount();
		// initialize bounds
		AffineTransform at = coords.getToWorldTransform(clip.stepToFrame(0));
		worldBounds = at.createTransformedShape(mat).getBounds2D();
		// combine bounds from every step
		for (int n = 0; n < stepCount; n++) {
			at = coords.getToWorldTransform(clip.stepToFrame(n));
			worldBounds.add(at.createTransformedShape(mat).getBounds2D());
		}
		isValidMeasure = true;
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}

	protected Rectangle getBounds() {
		return mat;
	}

	public void invalidate() {
		isValidMeasure = false;
	}

}
