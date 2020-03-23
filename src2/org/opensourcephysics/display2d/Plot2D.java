/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import javax.swing.JFrame;
import org.opensourcephysics.display.Measurable;

/**
 * The Plot2D interface defines common methods for 2d-plotting such as a GridPlot,
 * a VectorPlot, or a ContourPlot.
 *
 * Data must be stored in a GridData object.
 *
 * @author     Wolfgang Christian
 * @created    May 28, 2003
 * @version    1.0
 */
public interface Plot2D extends Measurable {
  static public final int GRID_PLOT = 0;
  static public final int INTERPOLATED_PLOT = 1;
  static public final int CONTOUR_PLOT = 2;
  static public final int SURFACE_PLOT = 3;

  /**
   * Sets the data to new values.
   *
   * The grid is resized to fit the new data if needed.
   *
   * @param val an array of new values
   */
  public void setAll(Object val);

  /**
   * Sets the values and the scale.
   *
   * The grid is resized to fit the new data if needed.
   *
   * @param obj array of new values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(Object obj, double xmin, double xmax, double ymin, double ymax);

  /**
   * Sets the data storage to the given value.
   *
   * @param _griddata
   */
  public void setGridData(GridData _griddata);

  /**
   * Gets the GridData object.
   * @return GridData
   */
  public GridData getGridData();

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  public double indexToX(int i);

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  public double indexToY(int i);

  /**
   * Gets closest index from the given x  world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  public int xToIndex(double x);

  /**
   * Gets closest index from the given y  world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  public int yToIndex(double y);

  /**
   * Gets the autoscale flag for z.
   *
   * @return boolean
   */
  public boolean isAutoscaleZ();

  /**
   * Gets the floor for scaling the z data.
   * @return double
   */
  public double getFloor();

  /**
   * Gets the ceiling for scaling the z data.
   * @return double
   */
  public double getCeiling();

  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z are span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil);
  
  /**
   * Forces the z-scale to be symmetric about zero.
   * Forces zmax to be positive and zmin=-zmax when in autoscale mode.
   *
   * @param symmetric
   */
  public void setSymmetricZ(boolean symmetric);
  
  /**
   * Gets the symmetric z flag.  
   */
  public boolean isSymmetricZ();

  /**
   * Sets the floor and ceiling colors.
   *
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor);

  /**
   * Sets the colors that will be used between the floor and ceiling values.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors);

  /**
   * Determines the palette type that will be used.
   * @param type
   */
  public void setPaletteType(int type);

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c);

  /**
   * Outlines the data grid's boundaries.
   *
   * @param showGrid
   */
  public void setShowGridLines(boolean showGrid);

  /**
   * Shows how values map to colors.
   */
  public JFrame showLegend();

  /**
   * Sets the visibility of the plot.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible);

  /**
   * Sets the indexes for the data components that will be plotted.
   *
   * Indexes determine the postion of the amplitude, phase, x-component, and y-component
   * data in the data array.  The amplitude index is usually the first index.
   *
   * @param indexes the sample-component indexes
   */
  public void setIndexes(int[] indexes);

  /**
   * Updates this object's state using new data values.
   *
   * Update should be invoked if the data in the PointData object changes or if the z scale
   * of the PointData object changes.
   *
   */
  public void update();

  /**
   * Expands the z scale so as to enhance values close to zero.
   *
   * @param expanded boolean
   * @param expansionFactor double
   */
  public void setExpandedZ(boolean expanded, double expansionFactor);

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
