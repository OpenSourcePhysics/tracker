/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

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
  protected Rectangle mat;
  private Rectangle2D bounds;
  private Paint paint = Color.white;
  private boolean visible = true;
  protected boolean isValidMeasure = false;
  private TrackerPanel trackerPanel;
  private ImageCoordSystem coords;
  protected Rectangle drawingBounds;

  /**
   * Creates a mat for the specified tracker panel
   *
   * @param panel the tracker panel
   */
  public TMat(TrackerPanel panel) {
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("coords", this); //$NON-NLS-1$
    coords = trackerPanel.getCoords();
    coords.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    mat = new Rectangle();
    trackerPanel.addDrawable(this);
    refresh();
  }

  /**
   * Draws the image mat on the panel.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if (!(panel instanceof VideoPanel) || !isVisible()) return;
    VideoPanel vidPanel = (VideoPanel) panel;
    Graphics2D g2 = (Graphics2D)g;
    // save graphics transform and paint
    AffineTransform gat = g2.getTransform();
    Paint gpaint = g2.getPaint();
    // transform world to screen
    g2.transform(vidPanel.getPixelTransform());
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
    g2.setTransform(gat);
    g2.setPaint(gpaint);
    // save drawing bounds for use when exporting videos
    Shape asDrawn = vidPanel.getPixelTransform().createTransformedShape(mat);
    Rectangle2D rect2D = asDrawn.getBounds2D();
    drawingBounds = new Rectangle((int)Math.round(rect2D.getMinX()), 
    		(int)Math.round(rect2D.getMinY()),(int)rect2D.getWidth(),(int)rect2D.getHeight());
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
  public double getXMin() {
    if (!isValidMeasure) getWorldBounds();
    return bounds.getMinX();
  }

  /**
   * Gets the maximum x needed to draw this object.
   *
   * @return maximum x
   */
  public double getXMax() {
    if (!isValidMeasure) getWorldBounds();
    return bounds.getMaxX();
  }

  /**
   * Gets the minimum y needed to draw this object.
   *
   * @return minimum y
   */
  public double getYMin() {
    if (!isValidMeasure) getWorldBounds();
    return bounds.getMinY();
  }

  /**
   * Gets the maximum y needed to draw this object.
   *
   * @return maximum y
   */
  public double getYMax() {
    if (!isValidMeasure) getWorldBounds();
    return bounds.getMaxY();
  }

  /**
   * Reports whether information is available to set min/max values.
   *
   * @return <code>true</code> if min/max values are valid
   */
  public boolean isMeasured() {
    return isVisible();
  }

  /**
   * Refreshes this mat.
   */
  public void refresh() {
    // remove and add coords "transform" listener
    coords.removePropertyChangeListener("transform", this); //$NON-NLS-1$
    coords = trackerPanel.getCoords();
    coords.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    mat.width = (int) trackerPanel.getImageWidth();
    mat.height = (int) trackerPanel.getImageHeight();
  	int w = (int)TrackerPanel.getDefaultImageWidth();
  	int h = (int)TrackerPanel.getDefaultImageHeight();
    Video video = trackerPanel.getVideo();
    if (video != null) {
    	if (video instanceof ImageVideo
    			&& video.getFilterStack().isEmpty()) {
    		Dimension dim = ((ImageVideo)video).getSize();
    		w = dim.width;
    		h = dim.height;
    	}
    	else {
        BufferedImage vidImage = video.getImage();
        if (vidImage != null) {
        	w = vidImage.getWidth();
        	h = vidImage.getHeight();
        }
    	}
    }
    mat.x = Math.min((w - mat.width)/2, 0);
    mat.y = Math.min((h - mat.height)/2, 0);
    isValidMeasure = false;
    trackerPanel.scale();
  }

  /**
   * Gets the x offset of this mat relative to the image origin 
   *
   * @return x offset
   */
  public double getXOffset() {
    return mat.x;
  }

  /**
   * Gets the y offset of this mat relative to the image origin 
   *
   * @return y offset
   */
  public double getYOffset() {
    return mat.y;
  }

  /**
   * Responds to property change events. TMat listens for the following
   * events: "transform" from the tracker panel's image coordinate system
   * and "coords" from the tracker panel
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("transform")) { //$NON-NLS-1$
      isValidMeasure = false;
    }
    else if (e.getPropertyName().equals("coords")) { //$NON-NLS-1$
      refresh();
    }
  }

  //_______________________________ private methods _________________________

  /**
   * Gets the world bounds of the mat.
   */
  private void getWorldBounds() {
    ImageCoordSystem coords = trackerPanel.getCoords();
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    int stepCount = clip.getStepCount();
    // initialize bounds
    AffineTransform at = coords.getToWorldTransform(clip.stepToFrame(0));
    bounds = at.createTransformedShape(mat).getBounds2D();
    // combine bounds from every step
    for (int n = 0; n < stepCount; n++) {
      at = coords.getToWorldTransform(clip.stepToFrame(n));
      bounds.add(at.createTransformedShape(mat).getBounds2D());
    }
    isValidMeasure = true;
  }

}
