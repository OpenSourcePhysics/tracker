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
 * <p>Title: ElementSegment</p>
 * <p>Description: A Segment using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementSegment extends Element implements org.opensourcephysics.display3d.core.ElementSegment {
  /* Implementation variables */
  protected int div = -1;                         // divisions of the segment. -1 to make sure new arrays are allocated
  protected int aCoord[] = null, bCoord[] = null; // The integer coordinates of the projected points
  protected Object3D[] objects = null;            // The Objects3D for this Drawable3D
  private double points[][] = null;               // coordinates for the 3D points of the segment and its subdivisions
  private double[] coordinates = new double[3];   // the input for all projections
  private double[] pixel = new double[3];         // The output for all projections

  {
    getStyle().setRelativePosition(org.opensourcephysics.display3d.core.Style.NORTH_EAST);
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  Object3D[] getObjects3D() {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()) {
      computeDivisions();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    return objects;
  }

  void draw(Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[_index].getDistance());
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(theColor);
    _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index+1], bCoord[_index+1]);
  }

  synchronized void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()) {
      return;
    }
    if(hasChanged()) {
      computeDivisions();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.setColor(getRealStyle().getLineColor());
    _g2.drawLine(aCoord[0], bCoord[0], aCoord[div], bCoord[div]);
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
      computeDivisions();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    if(targetPosition.isEnabled()&&(Math.abs(aCoord[0]-x)<SENSIBILITY)&&(Math.abs(bCoord[0]-y)<SENSIBILITY)) {
      return targetPosition;
    }
    if(targetSize.isEnabled()&&(Math.abs(aCoord[div]-x)<SENSIBILITY)&&(Math.abs(bCoord[div]-y)<SENSIBILITY)) {
      return targetSize;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  void projectPoints() {
    for(int i = 0; i<div; i++) {
      getDrawingPanel3D().project(points[i], pixel);
      aCoord[i] = (int) pixel[0];
      bCoord[i] = (int) pixel[1];
      for(int j = 0; j<3; j++) {
        coordinates[j] = (points[i][j]+points[i+1][j])/2; // The middle point
      }
      getDrawingPanel3D().project(coordinates, pixel);
      objects[i].setDistance(pixel[2]*getStyle().getDepthFactor());
    }
    // Project last point
    getDrawingPanel3D().project(points[div], pixel);
    aCoord[div] = (int) pixel[0];
    bCoord[div] = (int) pixel[1];
    setNeedToProject(false);
  }

  final void computeDivisions() {
    int theDiv = 1;
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.MAX_LENGTH :
           theDiv = Math.max((int) Math.round(0.49+getDiagonalSize()/res.getMaxLength()), 1);
           break;
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theDiv = Math.max(res.getN1(), 1);
           break;
      }
    }
    if(div!=theDiv) { // Reallocate arrays
      div = theDiv;
      points = new double[div+1][3];
      aCoord = new int[div+1];
      bCoord = new int[div+1];
      objects = new Object3D[div];
      for(int i = 0; i<div; i++) {
        objects[i] = new Object3D(this, i);
      }
    }
    double first = 0, last = 1;
    switch(getStyle().getRelativePosition()) {
       default :
         first = 0;
         last = 1;
         break;
       case org.opensourcephysics.display3d.core.Style.CENTERED :
         first = -0.5;
         last = 0.5;
         break;
       case org.opensourcephysics.display3d.core.Style.SOUTH_WEST :
         first = 1;
         last = 0;
         break;
    }
    points[0][0] = points[0][1] = points[0][2] = first;
    points[div][0] = points[div][1] = points[div][2] = last;
    double delta = (last-first)/div;
    for(int i = 1; i<div; i++) {
      points[i][0] = points[i][1] = points[i][2] = first+i*delta;
    }
    for(int i = 0; i<=div; i++) {
      sizeAndToSpaceFrame(points[i]); // apply the transformation(s)
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementSegment.Loader {
    public Object createObject(XMLControl control) {
      return new ElementSegment();
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
