/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;

/**
 * The GridData interface defines methods for objects that store
 * data on a grid.
 *
 * @author     Wolfgang Christian
 * @created    Dec 28, 2003
 * @version    1.0
 */
public interface GridData {
  /**
   * Estimates the value of a component at an untabulated point, (x,y).
   *
   * Interpolate often uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the cell boundaries, the gradient changes discontinuously
   * at the cell boundaries.
   *
   * @param x  the untabulated x
   * @param y  the untabulated y
   * @param component the component index
   * @return the interpolated sample
   */
  public double interpolate(double x, double y, int component);

  /**
 * Estimates multiple sample components at an untabulated point, (x,y).
 *
 * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
 * function is continous across the cell boundaries, the gradient changes discontinuously
 * at the cell boundaries.
 *
 * @param x  the untabulated x
 * @param y  the untabulated y
 * @param indexes to be interpolated
 * @param values the array that will contain the interpolated values
 * @return the interpolated array
 */
  public double[] interpolate(double x, double y, int[] indexes, double[] values);

  /**
   * Sets the the grid data using a lattice model and clears the cellScale flag.
   *
   * The left, right, top, and bottom bounds match the max and min values.
   *
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setScale(double xmin, double xmax, double ymin, double ymax);

  /**
   * Sets the left, right, bottom, and top bounds of the grid using a cell model and sets the cellScale flag.
   *
   * Coordinates are centered on each cell and are inside the bounds.
   *
   * @param left
   * @param right
   * @param bottom
   * @param top
   */
  public void setCellScale(double left, double right, double bottom, double top);

  /**
   * Sets the grid such the centers of the corner cells match the given coordinates.
   *
   * Coordinates are centered on each cell and the bounds are ouside the max and min values.
   *
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setCenteredCellScale(double xmin, double xmax, double ymin, double ymax);

  /**
   * Gets the cellData flag.
   *
   * @return true if cell data.
   */
  public boolean isCellData();

  /**
   * Sets the name of the component.
   *
   * @param component int
   * @param name String
   */
  public void setComponentName(int component, String name);

  /**
   * Gets the name of the component
   * @param i int
   * @return String
   */
  public String getComponentName(int i);

  /**
   * Gets the number of data components.
   *
   * @return int
   */
  public int getComponentCount();

  /**
   * Gets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @return the value.
   */
  public double getValue(int ix, int iy, int component);

  /**
   * Sets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @param value
   */
  public void setValue(int ix, int iy, int component, double value);

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx();

  /**
   * Gets the number of y entries.
   * @return ny
   */
  public int getNy();

  /**
   * Gets the array containing the data.
   *
   * @return the data
   */
  public double[][][] getData();

  /**
   * Gets the minimum and maximum values of the n-th component.
   * @return {zmin,zmax}
   */
  public double[] getZRange(int n);

  /**
   * Gets the x value for the first column in the grid.
   * @return  the leftmost x value
   */
  public double getLeft();

  /**
   * Gets the x value for the right column in the grid.
   * @return  the rightmost x value
   */
  public double getRight();

  /**
   * Gets the y value for the first row of the grid.
   * @return  the topmost y value
   */
  public double getTop();

  /**
   * Gets the y value for the last row of the grid.
   * @return the bottommost y value
   */
  public double getBottom();

  /**
   * Gets the change in x between grid columns moving from right to left.
   * @return the change in x
   */
  public double getDx();

  /**
   * Gets the change in y between grid rows moving from top to bottom.
   * The change in y is usually negative because y values usually decrease as pixel coordinates increase from top to bottom on a computer screen.
   * @return the change in y
   */
  public double getDy();

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
