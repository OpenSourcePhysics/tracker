/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * This is the basic class for all Elements which consist of a sequence
 * of 3D colored tiles: Ellipsoid, Cylinder, Box, ...
 * A tile is basically a collection of rectangles.
 * NOT YET FINISHED!!!!!
 */
public abstract class AbstractTile extends Element {
  // Configuration variables
  protected int numberOfTiles = 0;
  protected double corners[][][] = null;        // the numberOfTiles x vertex x 3  (vertex = 4 for 4-sided tiles
  private boolean drawQuickInterior = false;
  private int interiorTransparency = 128;
  //  private double displacementFactor = 1.0;
  // for z-coded colors

  private boolean levelBelowWhenEqual = true;
  private double levelx = 0.0, levely = 0.0, levelz = 0.0, leveldx = 0.0, leveldy = 0.0, leveldz = 1.0;
  private double[] levelZ = null;
  private Color[] levelColors = null;
  // Implementation variables
  private int a[][] = null, b[][] = null;
  private double[] pixel = new double[3];       // The output for all projections
  private double[] center = new double[3];
  private double[] pixelOrigin = new double[3]; // The projection of the origin
  private Object3D[] objects = null;

  // ----------------------------------------------
  // Configuration
  // See also below for everything related to the use of z-coded color
  // ----------------------------------------------

  /**
   * Sets an optional displacement factor to apply to the tiles when computing
   * their distance to the eye.
   * Setting it to a number bigger that 1 (say 1.03) is useful when you want
   * to draw anything on top of an object.
   * @param factor the desired displacement factor
   *
  public void setDisplacementFactor(double factor) {
    this.displacementFactor = factor;
  }

  /**
   * Gets the displacement factor
   * @return the current displacement factor
   * @see #setDisplacementFactor()
   *
  public double getDisplacementFactor() {
    return this.displacementFactor;
  }
*/

  /**
   * Draw a transparent interior when in quickDraw mode.
   * Default is <b>false</b>
   * @param draw the value desired
   * @param transparency the desired level of transparency (from 0=fully transparent to 255=opaque)
   */
  public void setDrawQuickInterior(boolean draw, int transparency) {
    this.drawQuickInterior = draw;
    this.interiorTransparency = Math.max(0, Math.min(transparency, 255));
  }

  /**
   * Whether a value equal to one of the thresholds should be drawn using the color
   * below or above
   * @param belowWhenEqual <b>true</b> to use the color below, <b>false</b> to use teh color above
   */
  public void setColorBelowWhenEqual(boolean belowWhenEqual) {
    this.levelBelowWhenEqual = belowWhenEqual;
  }

  /**
   * Sets the origin and direction of the color change.
   * Default is (0,0,0) and (0,0,1), giving z-coded regions
   *
   * @param origin double[]
   * @param direction double[]
   */
  public void setColorOriginAndDirection(double[] origin, double[] direction) {
    levelx = origin[0];
    levely = origin[1];
    levelz = origin[2];
    leveldx = direction[0];
    leveldy = direction[1];
    leveldz = direction[2];
  }

  /**
   * Set the levels and color for regional color separation
   * @param thresholds an array on n doubles that separate the n+1 regions.
   * <b>null</b> for no region separation
   * @param colors an array on n+1 colors, one for each of the regions
   */
  public void setColorRegions(double thresholds[], Color colors[]) {
    if((thresholds==null)||(colors==null)) {
      levelZ = null;
      levelColors = null;
      return;
    }
    levelZ = new double[thresholds.length];
    levelColors = new Color[thresholds.length+1];
    for(int i = 0; i<thresholds.length; i++) {
      levelZ[i] = thresholds[i];
    }
    for(int i = 0; i<thresholds.length+1; i++) {
      if(i<colors.length) {
        levelColors[i] = colors[i];
      } else {
        levelColors[i] = colors[colors.length-1];
      }
    }
    setElementChanged(true);
  }

  // ----------------------------------------------
  // part of the abstract methods in Element
  // ----------------------------------------------
  Object3D[] getObjects3D() {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()) {
      computeCorners();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    if(numberOfTiles<1) {
      return null;
    }
    return objects;
  }

  void draw(Graphics2D _g2, int _index) {
    if(levelZ!=null) {
      drawColorCoded(_g2, _index);
      return;
    }
    int sides = corners[_index].length;
    // Allow the panel to adjust color according to depth
    if(getRealStyle().isDrawingFill()) { // First fill the inside
      _g2.setPaint(getDrawingPanel3D().projectColor(getRealStyle().getFillColor(), objects[_index].getDistance()));
      _g2.fillPolygon(a[_index], b[_index], sides);
    }
    if(getRealStyle().isDrawingLines()) {
      _g2.setStroke(getRealStyle().getLineStroke());
      _g2.setColor(getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[_index].getDistance()));
      _g2.drawPolygon(a[_index], b[_index], sides);
    }
  }

  void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()) {
      return;
    }
    if(hasChanged()) {
      computeCorners();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    if(numberOfTiles<1) {
      return;
    }
    _g2.setStroke(getRealStyle().getLineStroke());
    if(getRealStyle().isDrawingFill()||drawQuickInterior) {
      Color fillColor = getRealStyle().getFillColor();
      if(drawQuickInterior&&(fillColor.getAlpha()>interiorTransparency)) {
        fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), interiorTransparency);
      }
      _g2.setPaint(fillColor);
      for(int i = 0; i<numberOfTiles; i++) {
        _g2.fillPolygon(a[i], b[i], corners[i].length);
      }
    }
    if(getRealStyle().isDrawingLines()) {
      _g2.setColor(getRealStyle().getLineColor());
      for(int i = 0; i<numberOfTiles; i++) {
        _g2.drawPolygon(a[i], b[i], corners[i].length);
      }
    }
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  protected InteractionTarget getTargetHit(int x, int y) {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()) {
      computeCorners();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    if(numberOfTiles<1) {
      return null;
    }
    if(targetPosition.isEnabled()&&(Math.abs(pixelOrigin[0]-x)<SENSIBILITY)&&(Math.abs(pixelOrigin[1]-y)<SENSIBILITY)) {
      return targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------

  /**
   * This will be used by subclasses whenever there is a need to recompute
   * the actual values of the corners before drawing.
   * Synchronization is recomended.
   */
  abstract protected void computeCorners();

  protected void setCorners(double[][][] _data) {
    corners = _data;
    if(corners==null) {
      numberOfTiles = 0;
      a = null;
      b = null;
      return;
    }
    numberOfTiles = corners.length;
    a = new int[numberOfTiles][];
    b = new int[numberOfTiles][];
    objects = new Object3D[numberOfTiles];
    for(int i = 0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      a[i] = new int[sides];
      b[i] = new int[sides];
      objects[i] = new Object3D(this, i);
    }
  }

  protected void projectPoints() {
    for(int i = 0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      for(int k = 0; k<3; k++) {
        center[k] = 0.0;                                            // Reset coordinates of the center
      }
      for(int j = 0; j<sides; j++) {
        getDrawingPanel3D().project(corners[i][j], pixel);          // Project each corner
        a[i][j] = (int) pixel[0];
        b[i][j] = (int) pixel[1];
        for(int k = 0; k<3; k++) {
          center[k] += corners[i][j][k];                            // Add to the coordinates of the center
        }
      }
      for(int k = 0; k<3; k++) {
        center[k] /= sides;
      }
      getDrawingPanel3D().project(center, pixel);                   // Project the center and take it
      objects[i].setDistance(pixel[2]*getStyle().getDepthFactor()); // as reference for the distance
    }
    getDrawingPanel3D().project(getHotSpot(targetPosition), pixelOrigin);
    setNeedToProject(false);
  }

  // ----------------------------------------------
  // Everything related to the use of z-coded color
  // ----------------------------------------------
  private double levelScalarProduct(double point[]) {
    return(point[0]-levelx)*leveldx+(point[1]-levely)*leveldy+(point[2]-levelz)*leveldz;
  }

  private void drawColorCoded(Graphics2D _g2, int _index) {
    int sides = corners[_index].length;
    // Compute in which region is each point
    int region[] = new int[sides];
    if(levelBelowWhenEqual) {
      for(int j = 0; j<sides; j++) {
        region[j] = 0;
        double level = levelScalarProduct(corners[_index][j]);
        for(int k = levelZ.length-1; k>=0; k--) {     // for each level
          if(level>levelZ[k]) {
            region[j] = k+1;
            break;
          }
        }
      }
    } else {
      for(int j = 0; j<sides; j++) {
        region[j] = levelZ.length;
        double level = levelScalarProduct(corners[_index][j]);
        for(int k = 0, l = levelZ.length; k<l; k++) { // for each level
          if(level<levelZ[k]) {
            region[j] = k;
            break;
          }
        }
      }
    }
    // Compute the subpoligon in each region
    int newCornersA[] = new int[sides*2];
    int newCornersB[] = new int[sides*2];
    for(int k = 0, l = levelZ.length; k<=l; k++) {     // for each level
      int newCornersCounter = 0;
      for(int j = 0; j<sides; j++) {                   // for each point
        int next = (j+1)%sides;
        if((region[j]<=k)&&(region[next]>=k)) {        // intersection bottom-up
          if(region[j]==k) {
            newCornersA[newCornersCounter] = a[_index][j];
            newCornersB[newCornersCounter] = b[_index][j];
            newCornersCounter++;
          } else {                                     // It started further down
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k-1]-t)/(levelScalarProduct(corners[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
          if(region[next]>k) {                         // This segment contributes with a second point
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k]-t)/(levelScalarProduct(corners[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
        } else if((region[j]>=k)&&(region[next]<=k)) { // intersection top-down
          if(region[j]==k) {
            newCornersA[newCornersCounter] = a[_index][j];
            newCornersB[newCornersCounter] = b[_index][j];
            newCornersCounter++;
          } else {                                     // It started further up
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k]-t)/(levelScalarProduct(corners[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
          if(region[next]<k) {                         // This segment contributes with a second point
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k-1]-t)/(levelScalarProduct(corners[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
        }
      }
      if(newCornersCounter>0) {                        // Draw the subpoligon
        Color theFillColor = levelColors[k];
        // if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor((Color) theFillPattern,objects[_index].distance);
        _g2.setPaint(theFillColor);
        _g2.fillPolygon(newCornersA, newCornersB, newCornersCounter);
      }
    }
    _g2.setColor(getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[_index].getDistance()));
    _g2.setStroke(getRealStyle().getLineStroke());
    _g2.drawPolygon(a[_index], b[_index], sides);
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
