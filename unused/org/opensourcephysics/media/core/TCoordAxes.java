/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This draws the worldspace origin and axes of an image coordinate system.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TCoordAxes extends TShape {
  // instance fields
  protected VideoPanel vidPanel;
  protected GeneralPath axes = new GeneralPath();
  protected Origin origin = new Origin();
  protected boolean originEnabled = true;
  protected boolean xaxisEnabled = true;
  protected GeneralPath originShape = new GeneralPath();
  protected GeneralPath xaxis = new GeneralPath();

  /**
   * Constructs a TCoordAxes object for the specified video panel.
   *
   * @param panel the video panel
   */
  public TCoordAxes(VideoPanel panel) {
    vidPanel = panel;
    setStroke(new BasicStroke(2));
    setColor(new Color(153, 0, 0));
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
   * Overrides TShape draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(panel==vidPanel) {
      super.draw(panel, g);
    }
  }

  /**
   * Overrides TPoint setXY method. Sets the angle of the x axis.
   *
   * @param x the x position
   * @param y the y position
   */
  public void setXY(double x, double y) {
    super.setXY(x, y);
    double cos = origin.cos(this);
    double sin = origin.sin(this);
    int n = vidPanel.getFrameNumber();
    vidPanel.getCoords().setCosineSine(n, cos, sin);
  }

  /**
   * Overrides TShape setStroke method.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if(stroke!=null) {
      this.stroke = stroke;
    }
  }

  /**
   * Sets whether the origin responds to mouse hits.
   *
   * @param enabled <code>true</code> if origin responds to mouse hits.
   */
  public void setOriginEnabled(boolean enabled) {
    originEnabled = enabled;
  }

  /**
   * Gets whether the origin responds to mouse hits.
   *
   * @return <code>true</code> if the origin responds to mouse hits.
   */
  public boolean isOriginEnabled() {
    return originEnabled;
  }

  /**
   * Sets whether the x-axis responds to mouse hits.
   *
   * @param enabled <code>true</code> if x-axis responds to mouse hits.
   */
  public void setXAxisEnabled(boolean enabled) {
    xaxisEnabled = enabled;
  }

  /**
   * Gets whether the x-axis responds to mouse hits.
   *
   * @return <code>true</code> if the x-axis responds to mouse hits.
   */
  public boolean isXAxisEnabled() {
    return xaxisEnabled;
  }

  /**
   * Overrides TShape findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the interactive drawable object
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(panel!=vidPanel) {
      return null;
    }
    if(!isEnabled()||!isVisible()) {
      return null;
    }
    setHitRectCenter(xpix, ypix);
    if(originEnabled&&originShape.intersects(hitRect)) {
      return origin;
    }
    if(xaxisEnabled&&xaxis.intersects(hitRect)) {
      return this;
    }
    return null;
  }

  /**
   * Returns a String describing this object.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "Coordinate axes"; //$NON-NLS-1$
  }

  //________________________ protected methods ________________________

  /**
   * Overrides TShape getShape method.
   *
   * @param vidPanel the video panel
   * @return the axes shape
   */
  protected Shape getShape(VideoPanel vidPanel) {
    ImageCoordSystem coords = vidPanel.getCoords();
    int n = getFrameNumber(vidPanel);
    int w = vidPanel.getWidth();
    int h = vidPanel.getHeight();
    double d = Math.max(2*w, 2*h);
    double x = coords.getOriginX(n);
    double y = coords.getOriginY(n);
    origin.setLocation(x, y);
    double sin = coords.getSine(n);
    double cos = coords.getCosine(n);
    float wx, wy, dcos, dsin, bcos, bsin;
    if(vidPanel.isDrawingInImageSpace()) {
      wx = vidPanel.xToPix(x);
      wy = vidPanel.yToPix(y);
      dcos = (float) (d*cos);
      dsin = (float) (d*sin);
      bcos = (float) (3*cos);
      bsin = (float) (3*sin);
    } else {
      wx = vidPanel.xToPix(coords.imageToWorldX(n, x, y));
      wy = vidPanel.yToPix(coords.imageToWorldY(n, x, y));
      dcos = (float) d;
      dsin = 0;
      bcos = 3;
      bsin = 0;
    }
    axes.reset();
    axes.moveTo(wx-dcos, wy+dsin);
    axes.lineTo(wx+dcos, wy-dsin);
    axes.moveTo(wx-dsin, wy-dcos);
    axes.lineTo(wx+dsin, wy+dcos);
    axes.moveTo(wx+5*bcos-bsin, wy-5*bsin-bcos);
    axes.lineTo(wx+5*bcos+bsin, wy-5*bsin+bcos);
    originShape.reset();
    originShape.moveTo(wx-3*bcos, wy+3*bsin);
    originShape.lineTo(wx+3*bcos, wy-3*bsin);
    originShape.moveTo(wx-3*bsin, wy-3*bcos);
    originShape.lineTo(wx+3*bsin, wy+3*bcos);
    xaxis.reset();
    xaxis.moveTo(wx+5*bcos, wy-5*bsin);
    xaxis.lineTo(wx+dcos, wy-dsin);
    return stroke.createStrokedShape(axes);
  }

  // ______________________ inner Origin class ________________________

  /**
   * Inner class used to set the origin.
   */
  private class Origin extends TPoint {
    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setXY(double x, double y) {
      super.setXY(x, y);
      int n = getFrameNumber(vidPanel);
      vidPanel.getCoords().setOriginXY(n, x, y);
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
