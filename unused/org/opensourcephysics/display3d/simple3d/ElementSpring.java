/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.Color;
import java.awt.Graphics2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display3d.simple3d.utils.VectorAlgebra;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: A Segment using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementSpring extends Element implements org.opensourcephysics.display3d.core.ElementSpring {
  // Configuration variables
  private double radius = 0.1;
  // Implementation variables
  private int loops = -1, pointsPerLoop = -1; // Make sure arrays are allocated
  private int segments = 0;
  private int aPoints[] = null, bPoints[] = null;
  private double points[][] = null;
  private double pixel[] = new double[3];     // The output for all projections
  private Object3D[] objects = null;          // The Objects3D of this Element

  {                                                     // Initialization block
    getStyle().setResolution(new Resolution(8, 15, 1)); // the 1 is meaningless
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  public void setRadius(double radius) {
    this.radius = radius;
    setElementChanged(true);
  }

  public double getRadius() {
    return this.radius;
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  Object3D[] getObjects3D() {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()) {
      computePoints();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    return objects;
  }

  void draw(Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[_index].getDistance());
    _g2.setColor(theColor);
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.drawLine(aPoints[_index], bPoints[_index], aPoints[_index+1], bPoints[_index+1]);
  }

  void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()) {
      return;
    }
    if(hasChanged()) {
      computePoints();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(getRealStyle().getLineColor());
    _g2.drawPolyline(aPoints, bPoints, segments+1);
  }

  void getExtrema(double[] min, double[] max) {
    min[0] = 0;
    max[0] = 1;
    min[1] = 0;
    max[1] = 1;
    min[2] = 0;
    max[2] = 1;
    sizeAndToSpaceFrame(min);
    sizeAndToSpaceFrame(max);
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  protected InteractionTarget getTargetHit(int x, int y) {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()) {
      computePoints();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    if(targetPosition.isEnabled()&&(Math.abs(aPoints[0]-x)<SENSIBILITY)&&(Math.abs(bPoints[0]-y)<SENSIBILITY)) {
      return targetPosition;
    }
    if(targetSize.isEnabled()&&(Math.abs(aPoints[segments]-x)<SENSIBILITY)&&(Math.abs(bPoints[segments]-y)<SENSIBILITY)) {
      return targetSize;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  void projectPoints() {
    for(int i = 0; i<segments; i++) {
      getDrawingPanel3D().project(points[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      objects[i].setDistance(pixel[2]*getStyle().getDepthFactor()); // distance is given by the first point
    }
    getDrawingPanel3D().project(points[segments], pixel);
    aPoints[segments] = (int) pixel[0];
    bPoints[segments] = (int) pixel[1];
    setNeedToProject(false);
  }

  private void computePoints() {
    int theLoops = loops, thePPL = pointsPerLoop;
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theLoops = Math.max(res.getN1(), 0);
           thePPL = Math.max(res.getN2(), 1);
           break;
      }
    }
    if((theLoops==loops)&&(thePPL==pointsPerLoop)) {
      // empty       // No need to reallocate arrays
    } else { // Reallocate arrays
      loops = theLoops;
      pointsPerLoop = thePPL;
      segments = loops*pointsPerLoop+3;
      points = new double[segments+1][3];
      aPoints = new int[segments+1];
      bPoints = new int[segments+1];
      objects = new Object3D[segments];
      for(int i = 0; i<segments; i++) {
        objects[i] = new Object3D(this, i);
      }
    }
    double[] size = new double[] {getSizeX(), getSizeY(), getSizeZ()};
    double[] u1 = VectorAlgebra.normalTo(size);
    double[] u2 = VectorAlgebra.normalize(VectorAlgebra.crossProduct(size, u1));
    double delta = 2.0*Math.PI/pointsPerLoop;
    int pre = pointsPerLoop/2;
    for(int i = 0; i<=segments; i++) {
      int k;
      if(i<pre) {
        k = 0;
      } else if(i<pointsPerLoop) {
        k = i-pre;
      } else if(i>(segments-pre)) {
        k = 0;
      } else if(i>(segments-pointsPerLoop)) {
        k = segments-i-pre;
      } else {
        k = pre;
      }
      double angle = i*delta;
      double cos = Math.cos(angle), sin = Math.sin(angle);
      points[i][0] = i*getSizeX()/segments+k*radius*(cos*u1[0]+sin*u2[0])/pre;
      points[i][1] = i*getSizeY()/segments+k*radius*(cos*u1[1]+sin*u2[1])/pre;
      points[i][2] = i*getSizeZ()/segments+k*radius*(cos*u1[2]+sin*u2[2])/pre;
      toSpaceFrame(points[i]); // apply the transformation(s)
    }
    setElementChanged(false);
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  static private class Loader extends org.opensourcephysics.display3d.core.ElementSpring.Loader {
    public Object createObject(XMLControl control) {
      return new ElementSpring();
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
