/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * Title:        Trail
 * Description:  A trail of pixels on the screen.  This object is often used to
 * show the path of a moving object.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class Trail extends AbstractTrail implements LogMeasurable {
  protected GeneralPath generalPath = new GeneralPath();
  boolean connected = true;

  /**
   * Adds a point to the trail.
   * @param x double
   * @param y double
   */
  public synchronized void addPoint(double x, double y) {
    if(closed) {
      throw new IllegalStateException("Cannot add points to a closed trail."); //$NON-NLS-1$
    }
    if(!connected||(numpts==0)) {
      generalPath.moveTo((float) x, (float) y);
    }
    generalPath.lineTo((float) x, (float) y);
    xmin = Math.min(xmin, x);
    xmax = Math.max(xmax, x);
    if(x>0) {
      xminLogscale = Math.min(xminLogscale, x);
      xmaxLogscale = Math.max(xmaxLogscale, x);
    }
    ymin = Math.min(ymin, y);
    ymax = Math.max(ymax, y);
    if(y>0) {
      yminLogscale = Math.min(yminLogscale, y);
      ymaxLogscale = Math.max(ymaxLogscale, y);
    }
    numpts++;
  }

  /**
   * Starts a new trail segment by moving to a new point without drawing.
   * @param x double
   * @param y double
   */
  public synchronized void moveToPoint(double x, double y) {
    generalPath.moveTo((float) x, (float) y);
    xmin = Math.min(xmin, x);
    xmax = Math.max(xmax, x);
    if(x>0) {
      xminLogscale = Math.min(xminLogscale, x);
      xmaxLogscale = Math.max(xmaxLogscale, x);
    }
    ymin = Math.min(ymin, y);
    ymax = Math.max(ymax, y);
    if(y>0) {
      yminLogscale = Math.min(yminLogscale, y);
      ymaxLogscale = Math.max(ymaxLogscale, y);
    }
    numpts++;
  }

  /**
   * Closes the path by connecting the first point to the last point.
   * Points cannot be added to a closed path;
   */
  public void closeTrail() {
    closed = true;
    generalPath.closePath();
  }

  /**
   * Sets the connectd flag.
   *
   * Successive points are connetected by straight lines.
   * Each point is marked as a colored pixel if the trail is not connected.
   *
   * @param connected boolean
   */
  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  /**
   * Gets the connected flag.
   *
   * @param connected boolean
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Clears all points from the trail.
   */
  public synchronized void clear() {
    closed = false;
    numpts = 0;
    xmax = xmaxLogscale = -Double.MAX_VALUE;
    ymax = ymaxLogscale = -Double.MAX_VALUE;
    xmin = xminLogscale = Double.MAX_VALUE;
    ymin = yminLogscale = Double.MAX_VALUE;
    generalPath.reset();
  }

  /**
   * Draws the trail on the panel.
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(numpts==0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(color);
    // transform from world to pixel coordinates
    Shape s = generalPath.createTransformedShape(panel.getPixelTransform());
    if(drawingStroke!=null) {
      Stroke stroke = g2.getStroke();
      g2.setStroke(drawingStroke);
      g2.draw(s);
      g2.setStroke(stroke);
    } else {
      g2.draw(s);
    }
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load Dataset data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Trail trail = (Trail) obj;
      control.setValue("connected", trail.connected);      //$NON-NLS-1$
      control.setValue("color", trail.color);              //$NON-NLS-1$
      control.setValue("number of pts", trail.numpts);     //$NON-NLS-1$
      control.setValue("general path", trail.generalPath); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Trail();
    }

    public Object loadObject(XMLControl control, Object obj) {
      Trail trail = (Trail) obj;
      trail.connected = control.getBoolean("connected");                   //$NON-NLS-1$
      trail.color = (Color) control.getObject("color");                    //$NON-NLS-1$
      trail.numpts = control.getInt("number of pts");                      //$NON-NLS-1$
      trail.generalPath = (GeneralPath) control.getObject("general path"); //$NON-NLS-1$
      return obj;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
