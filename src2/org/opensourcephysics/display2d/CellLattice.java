/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import org.opensourcephysics.display.DrawingPanel;

/**
 *  A CellLattice that displays an array where each array element can assume one of 256
 *  values.
 *
 * Values can be set between -128 and 127. Because byte values larger
 *  than 127 overflow to negative, values can also be set between 0 and 255. The
 *  lattice is drawn as an array of rectangles to distinguish between the two
 *  possible values.
 *
 * @author     Wolfgang Christian
 * @created    July 3, 2005
 * @version    1.0
 */
public class CellLattice implements ByteLattice {
  ByteLattice lattice = null;
  static String osName;

  static {
    try {
      osName = System.getProperty("os.name", ""); //$NON-NLS-1$ //$NON-NLS-2$
    } catch(Exception ex) {
      osName = "";                                //$NON-NLS-1$
    }
  }

  /**
   * Constructor CellLattice
   */
  public CellLattice() {
    if(osName.indexOf("Mac")>-1) { //$NON-NLS-1$
      lattice = new CellLatticeOSX();
    } else {
      lattice = new CellLatticePC();
    }
  }

  /**
   * Constructor CellLattice
   * @param nx
   * @param ny
   */
  public CellLattice(int nx, int ny) {
    if(osName.indexOf("Mac")>-1) { //$NON-NLS-1$
      lattice = new CellLatticeOSX(nx, ny);
    } else {
      lattice = new CellLatticePC(nx, ny);
    }
  }

  public double getXMin() {
    return lattice.getXMin();
  }

  public double getXMax() {
    return lattice.getXMax();
  }

  public double getYMin() {
    return lattice.getYMin();
  }

  public double getYMax() {
    return lattice.getYMax();
  }

  public boolean isMeasured() {
    return lattice.isMeasured();
  }

  public void draw(DrawingPanel panel, Graphics g) {
    lattice.draw(panel, g);
  }

  public int getNx() {
    return lattice.getNx();
  }

  public int getNy() {
    return lattice.getNy();
  }

  public int indexFromPoint(double x, double y) {
    return lattice.indexFromPoint(x, y);
  }

  public int xToIndex(double x) {
    return lattice.xToIndex(x);
  }

  public int yToIndex(double y) {
    return lattice.yToIndex(y);
  }

  public byte getValue(int ix, int iy) {
    return lattice.getValue(ix, iy);
  }

  public void setValue(int ix, int iy, byte val) {
    lattice.setValue(ix, iy, val);
  }

  public void randomize() {
    lattice.randomize();
  }

  public void resizeLattice(int nx, int ny) {
    lattice.resizeLattice(nx, ny);
  }

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
  public void setAll(byte val[][], double xmin, double xmax, double ymin, double ymax) {
    lattice.setAll(val, xmin, xmax, ymin, ymax);
  }

  public void setBlock(int ix_offset, int iy_offset, byte[][] val) {
    lattice.setBlock(ix_offset, iy_offset, val);
  }

  public void setBlock(byte[][] val) {
    lattice.setBlock(val);
  }

  public void setCol(int ix, int iy_offset, byte[] val) {
    lattice.setCol(ix, iy_offset, val);
  }

  public void setRow(int iy, int ix_offset, byte[] val) {
    lattice.setRow(iy, ix_offset, val);
  }

  public void setShowGridLines(boolean show) {
    lattice.setShowGridLines(show);
  }

  public void setGridLineColor(Color c) {
    lattice.setGridLineColor(c);
  }

  public JFrame showLegend() {
    return lattice.showLegend();
  }

  public void setVisible(boolean isVisible) {
    lattice.setVisible(isVisible);
  }

  public void setColorPalette(Color[] colors) {
    lattice.setColorPalette(colors);
  }

  public void setIndexedColor(int i, Color color) {
    lattice.setIndexedColor(i, color);
  }

  public void setMinMax(double xmin, double xmax, double ymin, double ymax) {
    lattice.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Creates a new SiteLattice containing the same data as this lattice.
   */
  public SiteLattice createSiteLattice() {
    if(osName.indexOf("Mac")>-1) { //$NON-NLS-1$
      return((CellLatticeOSX) lattice).createSiteLattice();
    }
    return((CellLatticePC) lattice).createSiteLattice();
  }

  /**
   *  Sets a block of cells using integer values.
   *
   * @param ix_offset int
   * @param iy_offset int
   * @param val int[][]
   */
  public void setBlock(int ix_offset, int iy_offset, int val[][]) {
    if(osName.indexOf("Mac")>-1) { //$NON-NLS-1$
      ((CellLatticeOSX) lattice).setBlock(ix_offset, iy_offset, val);
    } else {
      ((CellLatticePC) lattice).setBlock(ix_offset, iy_offset, val);
    }
  }

  public void setXMin(double xmin) {
    lattice.setXMin(xmin);
  }

  public void setXMax(double xmax) {
    lattice.setXMax(xmax);
  }

  public void setYMin(double ymin) {
    lattice.setYMin(ymin);
  }

  public void setYMax(double ymax) {
    lattice.setYMax(ymax);
  }

  public void createDefaultColors() {
    lattice.createDefaultColors();
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
