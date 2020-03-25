/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DisplayColors;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: A Segment using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementTrail extends Element implements org.opensourcephysics.display3d.core.ElementTrail {
  // Configuration variables
  private boolean connected = true;
  private int maximum = 0;
  private String[] inputLabels = new String[] {"x", "y", "z"}; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
  // Implementation variables
  private TrailPoint[] points = null;
  protected ArrayList<TrailPoint> list = new ArrayList<TrailPoint>();
  private TrailPoint ghostPoint = new TrailPoint(Double.NaN, Double.NaN, Double.NaN, true);

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  public void addPoint(double x, double y, double z) {
    addPoint(x, y, z, this.connected);
  }

  public void addPoint(double[] point) {
    addPoint(point[0], point[1], point[2], this.connected);
  }

  public void moveToPoint(double x, double y, double z) {
    addPoint(x, y, z, false);
  }

  public void setMaximumPoints(int maximum) {
    this.maximum = maximum;
  }

  public int getMaximumPoints() {
    return this.maximum;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public boolean isConnected() {
    return this.connected;
  }

  public synchronized void clear() {
    synchronized(list) {
      list.clear();
    }
    points = new TrailPoint[0];
    ghostPoint.xp = Double.NaN;
  }

  public void setXLabel(String _label) {
    inputLabels[0] = _label;
  }

  public void setYLabel(String _label) {
    inputLabels[1] = _label;
  }

  public void setZLabel(String _label) {
    inputLabels[2] = _label;
  }

  private void addPoint(double _x, double _y, double _z, boolean _c) {
    synchronized(list) {
      if((maximum>0)&&(list.size()>=maximum)) {
        list.remove(0);
      }
      TrailPoint point = new TrailPoint(_x, _y, _z, _c);
      list.add(point);
      if(getDrawingPanel3D()!=null) {
        point.transformAndProject();
      }
    }
  }

  public int getNumberOfPoints() {
    return list.size();
  }

  public void setGhostPoint(double[] _point, boolean _connected) {
    if(_point==null) {
      ghostPoint.xp = Double.NaN;
    } else {
      ghostPoint.xp = _point[0];
      ghostPoint.yp = _point[1];
      ghostPoint.zp = _point[2];
      ghostPoint.connected = _connected;
      if(getDrawingPanel3D()!=null) {
        ghostPoint.transformAndProject();
      }
    }
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  private void preparePoints() {
    boolean hasGhost = !Double.isNaN(ghostPoint.xp);
    int n = hasGhost ? list.size()+1 : list.size();
    points = new TrailPoint[n];
    int index = 0;
    for(TrailPoint point : list) {
      points[index] = point;
      point.setIndex(index);
      index++;
    }
    if(hasGhost) {
      points[index] = ghostPoint;
      ghostPoint.setIndex(index);
    }
  }

  Object3D[] getObjects3D() {
    synchronized(list) {
      if(!isReallyVisible()||(list.size()<=0)) {
        return null;
      }
      preparePoints();
    }
    if(hasChanged()) {
      transformAndProjectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    return points;
  }

  void draw(Graphics2D _g2, int _index) {
    TrailPoint point = points[_index];
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), point.getDistance());
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(theColor);
    if((_index==0)||!point.connected) {
      _g2.drawLine((int) point.pixel[0], (int) point.pixel[1], (int) point.pixel[0], (int) point.pixel[1]);
    } else {
      TrailPoint pointPrev = points[_index-1];
      _g2.drawLine((int) point.pixel[0], (int) point.pixel[1], (int) pointPrev.pixel[0], (int) pointPrev.pixel[1]);
    }
  }

  void drawQuickly(Graphics2D _g2) {
    synchronized(list) {
      if(!isReallyVisible()||(list.size()<=0)) {
        return;
      }
      preparePoints();
    }
    if(hasChanged()) {
      transformAndProjectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(getRealStyle().getLineColor());
    TrailPoint point = points[0];
    int aPrev = (int) point.pixel[0], bPrev = (int) point.pixel[1];
    _g2.drawLine(aPrev, bPrev, aPrev, bPrev);
    for(int i = 1, n = points.length; i<n; i++) { // The order is relevant
      point = points[i];
      if(point.connected) {
        _g2.drawLine((int) point.pixel[0], (int) point.pixel[1], aPrev, bPrev);
      } else {
        _g2.drawLine((int) point.pixel[0], (int) point.pixel[1], (int) point.pixel[0], (int) point.pixel[1]);
      }
      aPrev = (int) point.pixel[0];
      bPrev = (int) point.pixel[1];
    }
  }

  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    synchronized(list) {
      if(!isReallyVisible()||(list.size()<=0)) {
        return;
      }
      preparePoints();
    }
    for(int i = 0, n = points.length; i<n; i++) {
      aPoint[0] = points[i].xp;
      aPoint[1] = points[i].yp;
      aPoint[2] = points[i].zp;
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

  // -------------------------------------
  // Private methods
  // -------------------------------------
  synchronized void transformAndProjectPoints() {
    for(int i = 0, n = points.length; i<n; i++) {
      points[i].transformAndProject();
    }
    setNeedToProject(false);
    setElementChanged(false);
  }

  synchronized void projectPoints() {
    for(int i = 0, n = points.length; i<n; i++) {
      points[i].transformAndProject();
    }
    setNeedToProject(false);
  }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------
  protected int datasetID = hashCode();
  //  private Dataset dataset = null;

  public void setID(int id) {
    datasetID = id;
  }

  public int getID() {
    return datasetID;
  }

  public double[][] getData2D() {
    synchronized(list) {
      preparePoints();
    }
    double[][] data = new double[3][points.length];
    for(int i = 0, n = points.length; i<n; i++) {
      data[0][i] = points[i].xp;
      data[1][i] = points[i].yp;
      data[2][i] = points[i].zp;
    }
    return data;
  }

  public double[][][] getData3D() {
    return null;
  }

  public String[] getColumnNames() {
    return inputLabels;
  }

  public Color[] getLineColors() {
    return new Color[] {DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), DisplayColors.getLineColor(2)};
  }

  public Color[] getFillColors() {
    return new Color[] {getStyle().getFillColor(), getStyle().getFillColor(), getStyle().getFillColor()};
  }

  public java.util.List<Data> getDataList() {
    return null;
  }

  public java.util.ArrayList<Dataset> getDatasets() {
    return null;
  }
  //    double[][] data = getData2D();
  //    if (dataset==null) dataset = new Dataset();
  //    else dataset.clear();
  //    dataset.setName(getName());
  //    dataset.setConnected (connected);
  //    dataset.setLineColor(getLineColor());
  //    dataset.setMarkerShape(Dataset.SQUARE);
  //    dataset.setMarkerColor(getFillColor(),getLineColor());
  //    for (int i=0,n=data.length; i<n; i++) dataset.append(data[i][0], data[i][1]);
  //    java.util.ArrayList<Dataset> datasetList = new java.util.ArrayList<Dataset>();
  //    datasetList.add(dataset);
  //    return datasetList;    
  //  }

  // ----------------------------------------------------
  // A class for the individual points of the trail
  // ----------------------------------------------------
  private class TrailPoint extends Object3D {
    boolean connected; // shadows ElementTrail field
    private double xp, yp, zp;
    private double[] coordinates = new double[3];
    double[] pixel = new double[3];

    TrailPoint(double _x, double _y, double _z, boolean _c) {
      super(ElementTrail.this, -1); // Same index for all points. Will be changed in getObjects3D[]
      switch(getAxesMode()) {
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XYZ :
           xp = _x;
           yp = _y;
           zp = _z;
           break;
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
           xp = _x;
           zp = _y;
           yp = _z;
           break;
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
           yp = _x;
           xp = _y;
           zp = _z;
           break;
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
           zp = _x;
           xp = _y;
           yp = _z;
           break;
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
           yp = _x;
           zp = _y;
           xp = _z;
           break;
         case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
           zp = _x;
           yp = _y;
           xp = _z;
           break;
         default :
           xp = _x;
           yp = _y;
           zp = _z;
           break;
      }
      connected = _c;
    }

    void transformAndProject() {
      coordinates[0] = xp;
      coordinates[1] = yp;
      coordinates[2] = zp;
      sizeAndToSpaceFrame(coordinates);
      getDrawingPanel3D().project(coordinates, pixel);
      super.setDistance(pixel[2]*getStyle().getDepthFactor());
    }

  } // End of class TrailPoint

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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementTrail.Loader {
    public Object createObject(XMLControl control) {
      return new ElementTrail();
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
