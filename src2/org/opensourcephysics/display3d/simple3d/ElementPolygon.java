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
public class ElementPolygon extends Element implements org.opensourcephysics.display3d.core.ElementPolygon {
  // Configuration variables
  private boolean closed = true;
  private double coordinates[][] = new double[0][0];
  // Implementation variables
  private int aPoints[] = null, bPoints[] = null;
  private double[][] transformedCoordinates = new double[0][0];
  private double center[] = new double[3];                                     // The center of the poligon
  private double pixel[] = new double[3];                                      // Output of panel's projections
  private double originpixel[] = new double[3];                                // Projection of the origin, required for interaction
  protected Object3D[] lineObjects = null;                                     // Objects3D for each of the lines
  protected Object3D[] closedObject = new Object3D[] {new Object3D(this, -1)}; // A special object for a closed poligon

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  public boolean isClosed() {
    return this.closed;
  }

  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data array corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * The data array is copied, so subsequence changes to the original
   * array do not affect the polygon, until this setData() methos is invoked.
   * @param data double[][] the double[nPoints][3] array with the data
   */
  public void setData(double[][] data) {
    if(coordinates.length!=data.length) {
      int n = data.length;
      coordinates = new double[n][3];
      transformedCoordinates = new double[n][3];
      aPoints = new int[n];
      bPoints = new int[n];
      lineObjects = new Object3D[n];
      for(int i = 0; i<n; i++) {
        lineObjects[i] = new Object3D(this, i);
      }
    }
    for(int i = 0, n = data.length; i<n; i++) {
      System.arraycopy(data[i], 0, coordinates[i], 0, 3);
    }
    setElementChanged(true);
  }

  public void setData(double[] xArray, double[] yArray, double[] zArray) {
    if((xArray==null)||(yArray==null)||(zArray==null)) {
      return;
    }
    int n = Math.max(xArray.length, Math.max(yArray.length, zArray.length));
    if(coordinates.length!=n) {
      coordinates = new double[n][3];
      transformedCoordinates = new double[n][3];
      aPoints = new int[n];
      bPoints = new int[n];
      lineObjects = new Object3D[n];
      for(int i = 0; i<n; i++) {
        lineObjects[i] = new Object3D(this, i);
      }
    }
    if((xArray.length==yArray.length)&&(xArray.length==zArray.length)) {
      for(int i = 0; i<n; i++) {
        coordinates[i][0] = xArray[i];
        coordinates[i][1] = yArray[i];
        coordinates[i][2] = zArray[i];
      }
    } else {
      double lastX = xArray[xArray.length-1];
      double lastY = yArray[yArray.length-1];
      double lastZ = zArray[zArray.length-1];
      for(int i = 0; i<n; i++) {
        coordinates[i][0] = (i<xArray.length) ? xArray[i] : lastX;
        coordinates[i][1] = (i<yArray.length) ? yArray[i] : lastY;
        coordinates[i][2] = (i<zArray.length) ? zArray[i] : lastZ;
      }
    }
    setElementChanged(true);
  }

  /**
   * Gets (a copy of) the data of the points for the polygon
   * @return double[][] the double[nPoints][3] array with the data
   */
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
    if(closed&&getRealStyle().isDrawingFill()) {
      return closedObject;
    }
    return lineObjects;
  }

  void draw(Graphics2D _g2, int _index) {
    if(_index<0) { // Interior ==> closed = true and fillPattern!=null
      Color theFillColor = getDrawingPanel3D().projectColor(getRealStyle().getFillColor(), closedObject[0].getDistance());
      _g2.setPaint(theFillColor);
      _g2.fillPolygon(aPoints, bPoints, aPoints.length);
      if(getRealStyle().isDrawingLines()) {
        Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), closedObject[0].getDistance());
        _g2.setStroke(getRealStyle().getLineStroke());
        _g2.setColor(theColor);
        int n = aPoints.length-1;
        for(int i = 0; i<n; i++) {
          _g2.drawLine(aPoints[i], bPoints[i], aPoints[i+1], bPoints[i+1]);
        }
        _g2.drawLine(aPoints[n], bPoints[n], aPoints[0], bPoints[0]);
      }
      return;
    }
    if(!getRealStyle().isDrawingLines()) {
      return;
    }
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), lineObjects[_index].getDistance());
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(theColor);
    int sides = aPoints.length-1;
    if(_index<sides) {
      _g2.drawLine(aPoints[_index], bPoints[_index], aPoints[_index+1], bPoints[_index+1]); // regular segment
    } else {
      _g2.drawLine(aPoints[sides], bPoints[sides], aPoints[0], bPoints[0]);                 // if (_index==sides) { // Last closing segment
    }
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
    _g2.drawPolyline(aPoints, bPoints, aPoints.length);
    int sides = aPoints.length-1;
    if(closed) {
      _g2.drawLine(aPoints[sides], bPoints[sides], aPoints[0], bPoints[0]);
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
    center[0] = center[1] = center[2] = 0.0;
    sizeAndToSpaceFrame(center);
    getDrawingPanel3D().project(center, originpixel);
    // Now use "center" for the real center
    center[0] = center[1] = center[2] = 0.0;
    for(int i = 0, n = coordinates.length; i<n; i++) {
      for(int k = 0; k<3; k++) {
        center[k] += coordinates[i][k];
        transformedCoordinates[i][k] = coordinates[i][k];
      }
      sizeAndToSpaceFrame(transformedCoordinates[i]);
      getDrawingPanel3D().project(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      lineObjects[i].setDistance(pixel[2]*getStyle().getDepthFactor());
    }
    // last Segment
    if(!closed) {
      lineObjects[coordinates.length-1].setDistance(Double.NaN);
    }
    // The interior
    for(int k = 0; k<3; k++) {
      center[k] /= coordinates.length;
    }
    if(closed&&getRealStyle().isDrawingFill()) {
      getDrawingPanel3D().project(center, pixel);
      closedObject[0].setDistance(pixel[2]*getStyle().getDepthFactor());
    } else {
      closedObject[0].setDistance(Double.NaN); // Will not be drawn
    }
    setElementChanged(false);
    setNeedToProject(false);
  }

  void project() {
    for(int i = 0, n = coordinates.length; i<n; i++) {
      getDrawingPanel3D().project(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      lineObjects[i].setDistance(pixel[2]*getStyle().getDepthFactor());
    }
    if(!closed) {
      lineObjects[coordinates.length-1].setDistance(Double.NaN);
    }
    // The interior
    if(closed&&getRealStyle().isDrawingFill()) {
      getDrawingPanel3D().project(center, pixel);
      closedObject[0].setDistance(pixel[2]*getStyle().getDepthFactor());
    } else {
      closedObject[0].setDistance(Double.NaN); // Will not be drawn
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementPolygon.Loader {
    public Object createObject(XMLControl control) {
      return new ElementPolygon();
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
