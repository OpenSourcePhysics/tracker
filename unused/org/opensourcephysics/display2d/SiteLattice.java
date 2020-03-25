/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.Measurable;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 *  A SiteLattice displays an array where each array element can assume one of 256
 *  values. Array values are drawn as non-overlapping circles.
 *
 * Values can be set between -128 and 127. Because byte values larger
 *  than 127 overflow to negative, values can also be set between 0 and 255. The
 *  lattice is drawn as an array of rectangles to distinguish between the two
 *  possible values.
 *
 * @author     Wolfgang Christian
 * @author     Joshua Gould
 * @created    May 21, 2003
 * @version    1.0
 */
public class SiteLattice extends Grid implements Measurable, ByteLattice {
  boolean visible = true; // shadow super.visible
  Color[] colors = new Color[256];
  byte[][] data;
  int sx, sy;
  private JFrame legendFrame;

  /**
   *  Constructs a Site lattice with the given size. Site values are -128 to 127.
   *
   * @param sx  sites in x direction
   * @param sy  sites in y direction
   */
  public SiteLattice(int sx, int sy) {
    super(sx-1, sy-1); // number of cells in one less than number of sites
    this.sx = sx;
    this.sy = sy;
    createDefaultColors();
    data = new byte[sx][sy]; // site array
    color = Color.lightGray;
  }

  public void resizeLattice(int _nx, int _ny) {
    sx = _nx;
    sy = _ny;
    nx = sx-1; // in Grid
    ny = sy-1; // in Grid
    setMinMax(xmin, xmax, ymin, ymax);
    data = new byte[sx][sy]; // site array
  }

  /**
   * Creates a new CellLattice containing the same data as this lattice.
   */
  public ByteLattice createCellLattice() {
    CellLattice lattice = new CellLattice(sx, sy);
    lattice.setBlock(data);
    lattice.setMinMax(getXMin(), getXMax(), getYMin(), getYMax());
    lattice.setColorPalette(colors);
    return lattice;
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx() {
    return sx; // number of sites, not number of grids
  }

  /**
   * Gets the number of y entries.
   * @return ny
   */
  public int getNy() {
    return sy; // number of sites, not number of grids
  }

  /**
   * Sets the visibility of the lattice.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   *  Draws the lattice and the grid.
   *
   * @param  panel
   * @param  g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    super.draw(panel, g);
    double x = xmin;
    double y = ymin;
    int xradius = (int) Math.min(Math.abs(panel.getXPixPerUnit()*dx), 4); // not larger than 4
    xradius = Math.max(1, xradius); // minimum radius is 1 pixel
    int yradius = (int) Math.min(Math.abs(panel.getYPixPerUnit()*dy), 4); // not larger than 4
    yradius = Math.max(1, yradius); // minimum radius is 1 pixel
    for(int ix = 0; ix<sx; ix++) {
      for(int iy = 0; iy<sy; iy++) {
        int val = data[ix][iy]&0xFF;
        g.setColor(colors[val]);
        g.fillOval(panel.xToPix(x)-xradius, panel.yToPix(y)-yradius, 2*xradius, 2*yradius);
        if((xradius>3)&&(yradius>3)) {
          g.setColor(color);
          g.drawOval(panel.xToPix(x)-xradius, panel.yToPix(y)-yradius, 2*xradius, 2*yradius);
        }
        y += dy;
      }
      x += dx;
      y = ymin;
    }
  }

  /**
   * Sets a block of data to new values.
   *
   * The lattice is resized to fit the new data if needed.
   *
   * @param val
   */
  public void setAll(byte val[][]) {
    if((getNx()!=val.length)||(getNy()!=val[0].length)) {
      resizeLattice(val.length, val[0].length);
    }
    setBlock(0, 0, val);
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
    setAll(val);
    setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   *  Sets a block of cells using byte values.
   *
   * @param ix_offset int
   * @param iy_offset int
   * @param val byte[][]
   */
  public void setBlock(int ix_offset, int iy_offset, byte val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>sy)) {
      throw new IllegalArgumentException("Row offset "+iy_offset+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ix_offset<0)||(ix_offset+val.length>sx)) {
      throw new IllegalArgumentException("Column offset "+ix_offset+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        data[ix][iy] = val[ix-ix_offset][iy-iy_offset];
      }
    }
  }

  /**
   *  Sets a block of cells using integer values.
   *
   * @param ix_offset int
   * @param iy_offset int
   * @param val int[][]
   */
  public void setBlock(int ix_offset, int iy_offset, int val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>sy)) {
      throw new IllegalArgumentException("Row offset "+iy_offset+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ix_offset<0)||(ix_offset+val.length>sx)) {
      throw new IllegalArgumentException("Column offset "+ix_offset+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        data[ix][iy] = (byte) val[ix-ix_offset][iy-iy_offset];
      }
    }
  }

  /**
   *  Sets a block of cells to new values.
   *
   * @param  val
   */
  public void setBlock(byte val[][]) {
    setBlock(0, 0, val);
  }

  /**
   * Sets a column to new values.
   *
   * @param ix the x index of the column
   * @param iy_offset the y offset in the column
   * @param val values in column
   */
  public void setCol(int ix, int iy_offset, byte val[]) {
    if((iy_offset<0)||(iy_offset+val.length>sy)) {
      throw new IllegalArgumentException("Row offset "+iy_offset+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ix<0)||(ix>=sx)) {
      throw new IllegalArgumentException("Column index "+ix+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    for(int iy = iy_offset, my = val.length+iy_offset; iy<my; iy++) {
      data[ix][iy] = val[iy-iy_offset];
    }
  }

  /**
   * Sets a row to new values.
   *
   * @param iy  the y index of the row
   * @param ix_offset the x offset in the row
   * @param val
   */
  public void setRow(int iy, int ix_offset, byte val[]) {
    if((iy<0)||(iy>=sy)) {
      throw new IllegalArgumentException("Y index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>sx)) {
      throw new IllegalArgumentException("X offset out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int xindex = ix_offset, mx = val.length+ix_offset; xindex<mx; xindex++) {
      data[xindex][iy] = val[xindex-ix_offset];
    }
  }

  /**
   * Sets the given x,y location to a new value.
   *
   * @param ix
   * @param iy
   * @param val
   */
  public void setValue(int ix, int iy, byte val) {
    if((iy<0)||(iy>=sy)) {
      throw new IllegalArgumentException("Row index "+iy+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ix<0)||(ix>=sx)) {
      throw new IllegalArgumentException("Column index "+ix+" out of range."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    data[ix][iy] = val;
  }

  /**
   *  Gets a lattice site value.
   *
   * @param  row
   * @param  col
   * @return      the cell value.
   */
  public byte getValue(int col, int row) {
    return data[col][row];
  }

  /**
   * Determines the lattice index (row-major order) from given x and y world coordinates
   * Returns -1 if the world coordinates are outside the lattice.
   *
   * @param x
   * @param y
   * @return index
   */
  public int indexFromPoint(double x, double y) {
    int nx = getNx();
    int ny = getNy();
    double xMin = getXMin();
    double xMax = getXMax();
    double yMin = getYMin();
    double yMax = getYMax();
    double deltaX = (x-xMin)/(xMax-xMin);
    double deltaY = (y-yMin)/(yMax-yMin);
    int ix = (int) (deltaX*nx);
    int iy = (int) (deltaY*ny);
    if((ix<0)||(iy<0)||(ix>=nx)||(iy>=ny)) {
      return -1;
    }
    return iy*nx+ix;
  }

  /**
   * Gets closest index from the given x world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  public int xToIndex(double x) {
    int nx = getNx();
    double xMin = getXMin();
    double xMax = getXMax();
    double deltaX = (x-xMin)/(xMax-xMin);
    int ix = (int) (deltaX*nx);
    if(ix<0) {
      return 0;
    }
    if(ix>=nx) {
      return nx-1;
    }
    return ix;
  }

  /**
   * Gets closest index from the given y world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  public int yToIndex(double y) {
    int ny = getNy();
    double yMin = getYMin();
    double yMax = getYMax();
    double deltaY = (y-yMin)/(yMax-yMin);
    int iy = (int) (deltaY*ny);
    if(iy<0) {
      return 0;
    }
    if(iy>=ny) {
      return ny-1;
    }
    return iy;
  }

  /**
   * Sets the visibility of the sites.
   *
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setShowVisible(boolean isVisible) {
    visible = isVisible; // note that we are shadowing super.visible
  }

  /**
   * Sets the visibility of the grid connecting the sites.
   *
   * @param  showGridLines
   */
  public void setShowGridLines(boolean showGridLines) {
    super.visible = showGridLines;
  }

  /** Randomizes the lattice values. */
  public void randomize() {
    Random random = new Random();
    for(int rindex = 0, nr = data[0].length; rindex<nr; rindex++) {
      for(int cindex = 0, nc = data.length; cindex<nc; cindex++) {
        data[cindex][rindex] = (byte) random.nextInt(256);
      }
    }
  }

  /**
   * Shows the color associated with each value.
   * @return the JFrame containing the legend
   */
  public JFrame showLegend() {
    InteractivePanel dp = new InteractivePanel();
    dp.setPreferredSize(new java.awt.Dimension(300, 66));
    dp.setPreferredGutters(0, 0, 0, 35);
    dp.setClipAtGutter(false);
    if((legendFrame==null)||!legendFrame.isDisplayable()) {
      legendFrame = new JFrame(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    }
    legendFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    legendFrame.setResizable(false);
    legendFrame.setContentPane(dp);
    CellLattice lattice = new CellLattice(256, 1);
    lattice.setMinMax(-128, 127, 0, 1);
    byte[][] data = new byte[256][1];
    for(int i = 0; i<256; i++) {
      data[i][0] = (byte) (-128+i);
    }
    lattice.setBlock(0, 0, data);
    dp.addDrawable(lattice);
    XAxis xaxis = new XAxis(""); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setLocation(-0.5);
    xaxis.setEnabled(true);
    dp.addDrawable(xaxis);
    legendFrame.pack();
    legendFrame.setVisible(true);
    return legendFrame;
  }

  /**
   *  Sets the color palette.
   *
   * @param  _colors
   */
  public void setColorPalette(Color[] _colors) {
    int n = Math.min(256, _colors.length);
    for(int i = 0; i<n; i++) {
      colors[i] = _colors[i];
    }
    for(int i = n; i<256; i++) {
      colors[i] = Color.black;
    }
  }

  /**
   *  Sets the grid line color.
   *
   * @param  _color
   */
  public void setGridLineColor(Color _color) {
    color = _color;
  }

  /**
   *  Sets the color for a single index.
   *
   * @param  i
   * @param  color
   */
  public void setIndexedColor(int i, Color color) {
    // i = i % colors.length;
    i = (i+256)%colors.length;
    colors[i] = color;
  }

  /**
   * Method isMeasured
   *
   * @return measured flag
   */
  public boolean isMeasured() {
    return true; // we always have data
  }

  /**
   * Method getXMin
   * @return x min
   */
  public double getXMin() {
    return xmin-dx/2;
  }

  /**
   * Method getXMax
   * @return x max
   */
  public double getXMax() {
    return xmax+dx/2;
  }

  /**
   * Method getYMin
   * @return y min
   */
  public double getYMin() {
    return ymin-dy/2;
  }

  /**
   * Method getYMax
   * @return y max
   */
  public double getYMax() {
    return ymax+dy/2;
  }

  public void setXMin(double _value) {
    xmin = _value+dy/2;
    setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setXMax(double _value) {
    xmax = _value-dy/2;
    setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setYMin(double _value) {
    ymin = _value+dy/2;
    setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setYMax(double _value) {
    ymax = _value-dy/2;
    setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Creates the default palette.
   */
  public void createDefaultColors() {
    for(int i = 0; i<256; i++) {
      double x = (i<128) ? (i-100)/255.0 : -1;
      double val = Math.exp(-x*x*8);
      int red = (int) (255*val);   // red
      x = (i<128) ? i/255.0 : (255-i)/255.0;
      val = Math.exp(-x*x*8);
      int green = (int) (255*val); // green
      x = (i<128) ? -1 : (i-156)/255.0;
      val = Math.exp(-x*x*8);
      int blue = (int) (255*val);  // blue
      colors[i] = new Color(red, green, blue);
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
