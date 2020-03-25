/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.InteractiveCircle;
import org.opensourcephysics.display.Measurable;
import org.opensourcephysics.display.PlottingPanel;

/**
 * A DrawingFrame that displays particles.
 *
 * Particle locations are specified using a Point2D.
 * The default particle drawing shape is a circle.
 *
 * @author W. Christian
 * @version 1.0
 */
public class ParticleFrame extends DrawingFrame {
  ArrayList<Particles> partlist = new ArrayList<Particles>();

  /**
   * Constructs the ParticleFrame with the given labels and frame title.
   *
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public ParticleFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    setTitle(frameTitle);
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Ensures capacity
   *
   * @param  index
   */
  protected Particles checkIndex(int index) {
    while(index>=partlist.size()) {
      Particles p = new Particles();
      partlist.add(p);
      ((InteractiveCircle) p.shape).color = DisplayColors.getLineColor(partlist.indexOf(p));
      addDrawable(p);
    }
    return partlist.get(index);
  }

  /**
   * Adds a particle to the frame.
   *
   * @param  i
   * @param point
   */
  public void addParticle(int i, Point2D point) {
    checkIndex(i).addParticle(point);
  }

  /**
   * Adds an array of particles to the frame.
   *
   * @param  i
   * @param points
   */
  public void addParicle(int i, Point2D[] points) {
    checkIndex(i).addParticles(points);
  }

  /**
   * Sets the drawing shape for the particles.
   *
   * @param  i
   * @param shape Interactive
   */
  public void setDrawingShape(int i, Interactive shape) {
    checkIndex(i).shape = shape;
  }

  /**
   * Cleares drawable objects added by the user from this frame and clears the particles.
   */
  public void clearDrawables() {
    clearData();
    drawingPanel.clear(); // removes drawables added by user
  }

  /**
   * Clears all particles from this frame.
   */
  public void clearData() {
    Iterator<Particles> it = partlist.iterator();
    while(it.hasNext()) { // clears the data from the arrays
      Particles p = it.next();
      (p).clear();
      drawingPanel.removeDrawable(p);
    }
    partlist.clear();
    drawingPanel.invalidateImage();
  }

  class Particles implements Drawable, Measurable {
    Interactive shape = new InteractiveCircle();
    private ArrayList<Point2D> pointList = new ArrayList<Point2D>();
    double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE, ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;

    Particles() {
      shape.setEnabled(false); // default cannot drag particles
    }

    void addParticle(Point2D point) {
      synchronized(pointList) {
        xmax = Math.max(xmax, point.getX());
        xmin = Math.min(xmin, point.getX());
        ymax = Math.max(ymax, point.getY());
        ymin = Math.min(ymin, point.getY());
        pointList.add(point);
      }
    }

    void addParticles(Point2D[] points) {
      if(points==null) {
        return;
      }
      for(int i = 0, n = points.length; i<n; i++) {
        xmax = Math.max(xmax, points[i].getX());
        xmin = Math.min(xmin, points[i].getX());
        ymax = Math.max(ymax, points[i].getY());
        ymin = Math.min(ymin, points[i].getY());
      }
      synchronized(pointList) {
        for(int i = 0, n = points.length; i<n; i++) {
          pointList.add(points[i]);
        }
      }
    }

    void clear() {
      synchronized(pointList) {
        pointList.clear();
        xmin = Double.MAX_VALUE;
        xmax = -Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        ymax = -Double.MAX_VALUE;
      }
    }

    /**
     * Draws the particles.
     *
     * @param panel DrawingPanel
     * @param g Graphics
     */
    public void draw(DrawingPanel panel, Graphics g) {
      synchronized(pointList) {
        Iterator<Point2D> it = pointList.iterator();
        while(it.hasNext()) {
          Point2D point = (it.next());
          shape.setXY(point.getX(), point.getY());
          shape.draw(panel, g);
        }
      }
    }

    /**
     * getXMax
     *
     * @return double
     */
    public double getXMax() {
      return xmax;
    }

    /**
     * getXMin
     *
     * @return double
     */
    public double getXMin() {
      return xmin;
    }

    /**
     * getYMax
     *
     * @return double
     */
    public double getYMax() {
      return ymax;
    }

    /**
     * getYMin
     *
     * @return double
     */
    public double getYMin() {
      return ymin;
    }

    /**
     * There is a measure if there is at least one particle.
     *
     * @return boolean
     */
    public boolean isMeasured() {
      synchronized(pointList) {
        return(pointList.size()>0) ? true : false;
      }
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
