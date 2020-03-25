/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * A Dataset that can highlight selected points.
 *
 * @author     Doug Brown
 * @created    Dec 14 2005
 * @version    1.0
 */
public class HighlightableDataset extends Dataset implements Interactive {
  // instance fields
  boolean[] highlighted = new boolean[1]; // true if highlighted
  boolean[] previous;
  Color highlightColor = new Color(255, 255, 0, 128);
  Shape highlightShape;
  Shape[] hitShapes = new Shape[0];
  int hitIndex = -1;
  double[][] screenCoordinates = new double[2][];

  /**
   * Default constructor.
   */
  public HighlightableDataset() {
    super();
  }

  /**
   * Constructor specifying the marker color.
   *
   * @param  markerColor marker color
   */
  public HighlightableDataset(Color markerColor) {
    super(markerColor);
  }

  /**
   * Constructor specifying the marker color, line color, and whether
   * points are connected.
   *
   * @param  markerColor marker color
   * @param  lineColor line color
   * @param  connected true to connect points with line
   */
  public HighlightableDataset(Color markerColor, Color lineColor, boolean connected) {
    super(markerColor, lineColor, connected);
  }

  /**
   * Appends an (x,y) datum to the Dataset.
   *
   * @param  x the x value
   * @param  y the y value
   */
  public void append(double x, double y) {
    super.append(x, y);
    adjustCapacity(xpoints.length);
  }

  /**
   * Appends (x,y) arrays to the Dataset.
   *
   * @param  xarray the x array
   * @param  yarray the y array
   */
  public void append(double[] xarray, double[] yarray) {
    super.append(xarray, yarray);
    adjustCapacity(xpoints.length);
  }

  /**
   * Clear all data from this Dataset.
   */
  public void clear() {
    super.clear();
    previous = highlighted;
    highlighted = new boolean[xpoints.length];
  }

  /**
   * Restores previous highlights.
   */
  public void restoreHighlights() {
    if((previous!=null)&&(previous.length==highlighted.length)) {
      highlighted = previous;
    }
  }

  /**
   * Clears highlights.
   */
  public void clearHighlights() {
    for(int i = 0; i<highlighted.length; i++) {
      highlighted[i] = false;
    }
  }

  /**
   * Sets the highlighted flag for the specified point.
   *
   * @param i the array index
   * @param highlight true to highlight the point
   */
  public void setHighlighted(int i, boolean highlight) {
    if(i>=highlighted.length) {
      adjustCapacity(i+1);
    }
    highlighted[i] = highlight;
  }

  /**
   * Gets the highlighted flag for the specified point.
   *
   * @param i the array index
   * @return true if point is highlighted
   */
  public boolean isHighlighted(int i) {
    if(i>=highlighted.length) {
      adjustCapacity(i+1);
    }
    return highlighted[i];
  }

  /**
   * Sets the highlight color.
   *
   * @param color the color
   */
  public void setHighlightColor(Color color) {
    highlightColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);
  }

  /**
   * Move an out-of-place datum into its correct position.
   *
   * @param  loc the datum
   */
  protected void moveDatum(int loc) {
    super.moveDatum(loc);
  }

  /**
   * Sets the highlighted array size to larger of xpoints.length and minLength.
   *
   * @param minLength minimum capacity required
   */
  private synchronized void adjustCapacity(int minLength) {
    int len = Math.max(xpoints.length, minLength);
    if(highlighted.length==len) {
      return;
    }
    boolean[] temp = highlighted;
    highlighted = new boolean[len];
    int count = Math.min(temp.length, len);
    System.arraycopy(temp, 0, highlighted, 0, count);
  }

  /**
   * Draw this Dataset in the drawing panel.
   *
   * @param  drawingPanel the drawing panel
   * @param  g the graphics
   */
  public void draw(DrawingPanel drawingPanel, Graphics g) {
    super.draw(drawingPanel, g);
    Graphics2D g2 = (Graphics2D) g;
    int offset = getMarkerSize()+4;
    int edge = 2*offset;
    // increase the clip to include the entire highlight
    Shape clipShape = g2.getClip();
    g2.setClip(drawingPanel.leftGutter-offset-1, drawingPanel.topGutter-offset-1, drawingPanel.getWidth()-drawingPanel.leftGutter-drawingPanel.rightGutter+2+2*offset, drawingPanel.getHeight()-drawingPanel.bottomGutter-drawingPanel.topGutter+2+2*offset);
    Rectangle viewRect = drawingPanel.getViewRect();
    if(viewRect!=null) { // decrease the clip if we are in a scroll pane
      g2.clipRect(viewRect.x, viewRect.y, viewRect.x+viewRect.width, viewRect.y+viewRect.height);
    }
    hitShapes = new Shape[index];
    double[] xValues = getXPoints();
    double[] yValues = getYPoints();
    if (screenCoordinates[0]==null || screenCoordinates[0].length!=index) {
    	screenCoordinates[0] = new double[index];
    	screenCoordinates[1] = new double[index];
    }
    for(int i = 0; i<index; i++) {
      if(Double.isNaN(yValues[i])) {
      	screenCoordinates[1][i] = Double.NaN;
        continue;
      }
      double xp = drawingPanel.xToPix(xValues[i]);
      double yp = drawingPanel.yToPix(yValues[i]);
      screenCoordinates[0][i] = xp;
      screenCoordinates[1][i] = yp;
      hitShapes[i] = new Rectangle2D.Double(xp-offset, yp-offset, edge, edge);
      if(!isHighlighted(i)) {
        continue;
      }
      g2.setColor(highlightColor);
      g2.fill(hitShapes[i]);
    }
    g2.setClip(clipShape); // restore the original clip
  }

  /**
   * Returns the Interactive object at a specified pixel position.
   * Implements Interactive.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the object
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    // return hits only within active plot area 
    int l = panel.getLeftGutter();
    int r = panel.getRightGutter();
    int t = panel.getTopGutter();
    int b = panel.getBottomGutter();
    Dimension dim = panel.getSize();
    if((xpix<l)||(xpix>dim.width-r)) {
      return null;
    }
    if((ypix<t)||(ypix>dim.height-b)) {
      return null;
    }
    hitIndex = -1;
    for (int i = 0; i<hitShapes.length; i++) {
      if(hitShapes[i]!=null && hitShapes[i].contains(xpix, ypix)) {
        hitIndex = i;
        return this;
      }
    }
    return null;
  }

  /**
   * Gets the most recent hit index.
   *
   * @return the hit index 
   */
  public int getHitIndex() {
    return hitIndex;
  }

  /**
   * Gets the screen coordinates of all data points.
   *
   * @return screen coordinates 
   */
  public double[][] getScreenCoordinates() {
    return screenCoordinates;
  }

  /**
   * Implements Interactive.
   *
   * @param enabled ignored
   */
  public void setEnabled(boolean enabled) {}

  /**
   * Implements Interactive.
   *
   * @return true
   */
  public boolean isEnabled() {
    return true;
  }

  /**
   * Implements Interactive.
   *
   * @param x ignored
   * @param y ignored
   */
  public void setXY(double x, double y) {}

  /**
   * Implements Interactive.
   *
   * @param x ignored
   */
  public void setX(double x) {}

  /**
   * Implements Interactive.
   *
   * @param y ignored
   */
  public void setY(double y) {}

  /**
   * Implements Interactive.
   *
   * @return the x value at the current hit index, or Double.NaN if none
   */
  public double getX() {
    if(hitIndex>-1) {
      return xpoints[hitIndex];
    }
    return Double.NaN;
  }

  /**
   * Implements Interactive.
   *
   * @return the y value at the current hit index, or Double.NaN if none
   */
  public double getY() {
    if(hitIndex>-1) {
      return ypoints[hitIndex];
    }
    return Double.NaN;
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
      XML.getLoader(Dataset.class).saveObject(control, obj);
      HighlightableDataset data = (HighlightableDataset) obj;
      control.setValue("highlighted", data.highlighted); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new HighlightableDataset();
    }

    public Object loadObject(XMLControl control, Object obj) {
      XML.getLoader(Dataset.class).loadObject(control, obj);
      HighlightableDataset data = (HighlightableDataset) obj;
      boolean[] highlighted = (boolean[]) control.getObject("highlighted"); //$NON-NLS-1$
      if(highlighted!=null) {
        data.highlighted = highlighted;
      }
      return data;
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
