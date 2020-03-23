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
 * The ByteLattice interface defines a lattice visualization component where each
 * array element can assume one of 256 values.
 *
 * Known implementations are: ByteRaster, CellLattice, and SiteLattice.
 *
 * @author     Wolfgang Christian
 * @created    May 27, 2003
 * @version    1.0
 */
public interface ByteLattice extends Measurable {
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
   * Determines the lattice index (row-major order) from given x and y world coordinates.
   * Returns -1 if the world coordinates are outside the lattice.
   *
   * @param x
   * @param y
   * @return index
   */
  public int indexFromPoint(double x, double y);

  /**
   * Gets closest index from the given x world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  public int xToIndex(double x);

  /**
   * Gets closest index from the given y world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  public int yToIndex(double y);

  /**
   * Gets a value from the given location.
   *
   * @param ix
   * @param iy
   * @return the value.
   */
  public byte getValue(int ix, int iy);

  /**
   * Sets the given x,y location to a value.
   *
   * @param ix
   * @param iy
   * @param val
   */
  public void setValue(int ix, int iy, byte val);

  /**
   * Randomizes the values.
   */
  public void randomize();

  /**
   * Resizes the lattice.
   *
   * @param _nx
   * @param _ny
   */
  public void resizeLattice(int _nx, int _ny);

  /**
* Sets the lattice values and scale.
*
* The lattice is resized to fit the new data if needed.
*
* @param val int[][] the new values
* @param xmin double
* @param xmax double
* @param ymin double
* @param ymax double
*/
  public void setAll(byte val[][], double xmin, double xmax, double ymin, double ymax);

  /**
   * Sets a block of data to new values.
   *
   * @param ix_offset  the x offset into the lattice
   * @param iy_offset  the y offset into the lattice
   * @param val
   */
  public void setBlock(int ix_offset, int iy_offset, byte val[][]);

  /**
   * Sets a block of data starting at (0,0) to new values.
   *
   * @param val
   */
  public void setBlock(byte val[][]);

  /**
   * Sets a column to new values.
   *
   * @param ix the x index of the column
   * @param iy_offset the y offset in the column
   * @param val values in column
   */
  public void setCol(int ix, int iy_offset, byte val[]);

  /**
   * Sets a row to new values.
   *
   * @param iy  the y index of the row
   * @param ix_offset the x offset in the row
   * @param val
   */
  public void setRow(int iy, int ix_offset, byte val[]);

  /**
   * Outlines the lattice boundaries with a grid.
   *
   * @param showGridLines
   */
  public void setShowGridLines(boolean showGridLines);

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c);

  /**
   * Shows the color associated with each value.
   * @return the JFrame containing the legend
   */
  public JFrame showLegend();

  /**
   * Sets the visibility of the lattice.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible);

  /**
   * Sets the color palette.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors);

  /**
   * Sets the color for a single index.
   * @param i
   * @param color
   */
  public void setIndexedColor(int i, Color color);

  /**
   * Assigns a scale to the lattice in world units.
   *
   * This method does not change lattice values; it assigns units corners of the lattice.
   *
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setMinMax(double xmin, double xmax, double ymin, double ymax);

  /**
   * Sets xmin.
   * @param xmin double
   */
  public void setXMin(double xmin);

  /**
   * Sets xmax.
   * @param xmax double
   */
  public void setXMax(double xmax);

  /**
   * Sets ymin.
   * @param ymin double
   */
  public void setYMin(double ymin);

  /**
   * Sets ymax.
   * @param ymax double
   */
  public void setYMax(double ymax);

  /**
   * Creates the default palette.
   */
  public void createDefaultColors();

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
