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

/**
 * <p>Title: ElementPolygon</p>
 * <p>Description: A Polygon using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementPoints extends Element implements org.opensourcephysics.display3d.core.ElementPoints {
  // Configuration variables
  private double coordinates[][] = new double[0][0];
  // Implementation variables
  private int aPoints[] = null, bPoints[] = null;
  private double[][] transformedCoordinates = new double[0][0];
  private double origin[] = new double[3];      // Origin coordinates, required for interaction
  private double pixel[] = new double[3];       // Output of panel's projections
  private double originpixel[] = new double[3]; // Projection of the origin, required for interaction
  protected Object3D[] pointObjects = null;     // Objects3D for each of the points

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  public void setData(double[][] data) {
    if(coordinates.length!=data.length) {
      int n = data.length;
      coordinates = new double[n][3];
      transformedCoordinates = new double[n][3];
      aPoints = new int[n];
      bPoints = new int[n];
      pointObjects = new Object3D[n];
      for(int i = 0; i<n; i++) {
        pointObjects[i] = new Object3D(this, i);
      }
    }
    for(int i = 0, n = data.length; i<n; i++) {
      System.arraycopy(data[i], 0, coordinates[i], 0, 3);
    }
    setElementChanged(true);
  }

  public double[][] getData() {
    double[][] data = new double[coordinates.length][3];
    for(int i = 0, n = coordinates.length; i<n; i++) {
      System.arraycopy(coordinates[i], 0, data[i], 0, 3);
    }
    return data;
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    for(int i = 0, n = coordinates.length; i<n; i++) {
      System.arraycopy(coordinates[i], 0, aPoint, 0, 3);
      sizeAndToSpaceFrame(aPoint);
      minX = Math.min(minX, aPoint[0]);
      maxX = Math.max(maxX, aPoint[0]);
      minY = Math.min(minY, aPoint[1]);
      maxY = Math.max(maxY, aPoint[1]);
      minZ = Math.min(minZ, aPoint[2]);
      maxZ = Math.max(maxZ, aPoint[2]);
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
  }

  Object3D[] getObjects3D() {
    if(!isReallyVisible()||(coordinates.length==0)) {
      return null;
    }
    if(hasChanged()) {
      transformAndProject();
    } else if(needsToProject()) {
      project();
    }
    return pointObjects;
  }

  void draw(Graphics2D _g2, int _index) {
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), pointObjects[_index].getDistance());
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(theColor);
    _g2.drawLine(aPoints[_index], bPoints[_index], aPoints[_index], bPoints[_index]); // a segment from it to itself
  }

  void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()||(coordinates.length==0)) {
      return;
    }
    if(hasChanged()) {
      transformAndProject();
    } else if(needsToProject()) {
      project();
    }
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(getRealStyle().getLineColor());
    for(int i = 0, n = coordinates.length; i<n; i++) {
      _g2.drawLine(aPoints[i], bPoints[i], aPoints[i], bPoints[i]); // a segment from it to itself
    }
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  protected InteractionTarget getTargetHit(int x, int y) {
    if(!isReallyVisible()||(coordinates.length==0)) {
      return null;
    }
    if(hasChanged()) {
      transformAndProject();
    } else if(needsToProject()) {
      project();
    }
    if(targetPosition.isEnabled()&&(Math.abs(originpixel[0]-x)<SENSIBILITY)&&(Math.abs(originpixel[1]-y)<SENSIBILITY)) {
      return targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  void transformAndProject() {
    // Compute the origin projection. Reuse center
    origin[0] = origin[1] = origin[2] = 0.0;
    sizeAndToSpaceFrame(origin);
    getDrawingPanel3D().project(origin, originpixel);
    for(int i = 0, n = coordinates.length; i<n; i++) {
      System.arraycopy(coordinates[i], 0, transformedCoordinates[i], 0, 3);
      sizeAndToSpaceFrame(transformedCoordinates[i]);
      getDrawingPanel3D().project(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      pointObjects[i].setDistance(pixel[2]*getStyle().getDepthFactor());
    }
    setElementChanged(false);
    setNeedToProject(false);
  }

  void project() {
    for(int i = 0, n = coordinates.length; i<n; i++) {
      getDrawingPanel3D().project(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      pointObjects[i].setDistance(pixel[2]*getStyle().getDepthFactor());
    }
    setNeedToProject(false);
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementPoints.Loader {
    public Object createObject(XMLControl control) {
      return new ElementPoints();
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
