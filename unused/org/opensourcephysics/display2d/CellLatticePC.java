/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 * A CellLattice displays an array where each array element can assume one of 256 values.
 * Array values are drawn using an image raster.  Each cell is a single pixel using an image that mathches the
 *  the lattice dimension.  The image is then scaled  so that every pixel is drawn as a rectangle.
 *
 * Values can be set between -128 and 127.  Because byte values larger than 127 overflow
 * to negative, values can also be set between 0 and 255.
 *
 * The lattice is drawn as an array of rectangles to distinguish between the two possible values.
 *
 * @author     Wolfgang Christian
 * @created    February 11, 2003
 * @version    1.0
 */
public class CellLatticePC extends MeasuredImage implements ByteLattice {
  // static final int ZERO = 0;
  WritableRaster raster;
  Grid grid;
  int ny, nx;
  int[][] rgb = new int[256][3];
  byte[][] data;
  private JFrame legendFrame;

  /**
   * Constructs a cell lattice.
   *
   * Cell values are -128 to 127.
   *
   */
  public CellLatticePC() {
    this(1, 1);
  }

  /**
   * Constructs a cell lattice with the given size.
   *
   * Cell values are -128 to 127.
   *
   * @param _nx the number of values in x direction
   * @param _ny the number of values in y direction
   */
  public CellLatticePC(int _nx, int _ny) {
    ny = Math.max(1, _ny);
    nx = Math.max(1, _nx);
    createDefaultColors();
    data = new byte[nx][ny];
    image = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_RGB);
    raster = image.getRaster();
    xmin = 0;
    xmax = nx;
    ymin = 0;
    ymax = ny;
    grid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    grid.setColor(Color.lightGray);
    // set all pixels in the raster to correspond to the zero color
    for(int ix = 0; ix<nx; ix++) {
      for(int iy = 0; iy<ny; iy++) {
        raster.setPixel(ix, iy, rgb[0]);
      }
    }
  }

  /**
   * Creates a new SiteLattice containing the same data as this lattice.
   */
  public SiteLattice createSiteLattice() {
    SiteLattice lattice = new SiteLattice(nx, ny);
    lattice.setBlock(data);
    lattice.setMinMax(getXMin(), getXMax(), getYMin(), getYMax());
    Color[] colors = new Color[rgb.length];
    for(int i = 0; i<colors.length; i++) {
      colors[i] = new Color(rgb[i][0], rgb[i][1], rgb[i][2]);
    }
    lattice.setColorPalette(colors);
    return lattice;
  }

  /**
   * Resizes the lattice using the given number of x and y entries.
   * @param _nx the number of x entries
   * @param _ny the number of y entries
   */
  public void resizeLattice(int _nx, int _ny) {
    ny = _ny;
    nx = _nx;
    data = new byte[nx][ny];
    image = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_RGB);
    raster = image.getRaster();
    Grid oldGrid = grid;
    grid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    if(oldGrid!=null) {
      grid.setColor(oldGrid.getColor());
      grid.setVisible(oldGrid.isVisible());
    }
    // set all pixels in the raster to correspond to the zero color
    for(int ix = 0; ix<nx; ix++) {
      for(int iy = 0; iy<ny; iy++) {
        raster.setPixel(ix, iy, rgb[0]);
      }
    }
    setMinMax(xmin, xmax, ymin, ymax);
    //setMinMax(0, nx, 0, ny);
  }
  
  public void setXMin(double _value) {
    super.setXMin(_value);
	grid.setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setXMax(double _value) {
	super.setXMax(_value);
    grid.setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setYMin(double _value) {
	super.setYMin(_value);
    grid.setMinMax(xmin, xmax, ymin, ymax);
  }

  public void setYMax(double _value) {
    super.setYMax(_value);
	grid.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx() {
    return nx;
  }

  /**
   * Gets the number of y entries.
   * @return ny
   */
  public int getNy() {
    return ny;
  }

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
  public void setMinMax(double xmin, double xmax, double ymin, double ymax) {
    super.setMinMax(xmin, xmax, ymin, ymax);
    grid.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Draws the lattice and the grid.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    super.draw(panel, g);
    grid.draw(panel, g);
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
   * Sets a block of data starting at (0,0) to new values.
   *
   * @param val
   */
  public void setBlock(byte val[][]) {
    setBlock(0, 0, val);
  }

  /**
   * Sets a block of data to byte values.
   *
   * @param ix_offset  the x offset into the lattice
   * @param iy_offset  the y offset into the lattice
   * @param val the new values
   */
  public void setBlock(int ix_offset, int iy_offset, byte val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Y index out of range in byte lattice setSiteBlock."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("X index out of range in byte lattice setSiteBlock."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        data[ix][iy] = val[ix-ix_offset][iy-iy_offset];
        raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]);
      }
    }
  }

  /**
   * Sets a block of data to integer values.
   *
   * @param ix_offset  the x offset into the lattice
   * @param iy_offset  the y offset into the lattice
   * @param val the new values
   */
  public void setBlock(int ix_offset, int iy_offset, int val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Y index out of range in byte lattice setSiteBlock."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("X index out of range in byte lattice setSiteBlock."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        data[ix][iy] = (byte) val[ix-ix_offset][iy-iy_offset];
        raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]);
      }
    }
  }

  /**
   * Sets a column to new values.
   *
   * @param ix the x index of the column
   * @param iy_offset the y offset in the column
   * @param val values in column
   */
  public void setCol(int ix, int iy_offset, byte val[]) {
    if((iy_offset<0)||(iy_offset+val.length>ny)) {
      throw new IllegalArgumentException("Y offset out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("X index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val.length+iy_offset; iy<my; iy++) {
      data[ix][iy] = val[iy-iy_offset];
      raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]);
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
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("Y index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("X offset out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int xindex = ix_offset, mx = val.length+ix_offset; xindex<mx; xindex++) {
      data[xindex][iy] = val[xindex-ix_offset];
      raster.setPixel(xindex, ny-iy-1, rgb[data[xindex][iy]&0xFF]);
    }
  }

  /**
   * Sets the given x,y location to a value.
   *
   * @param ix
   * @param iy
   * @param val
   */
  public void setValue(int ix, int iy, byte val) {
    data[ix][iy] = val;
    raster.setPixel(ix, ny-iy-1, rgb[val&0xFF]);
  }

  /**
   * Gets a value from the given location.
   *
   * @param ix
   * @param iy
   * @return the value.
   */
  public byte getValue(int ix, int iy) {
    return data[ix][iy];
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
   * Outlines the lattice boundaries with a grid.
   *
   * @param showGridLines
   */
  public void setShowGridLines(boolean showGridLines) {
    grid.setVisible(showGridLines);
  }

  /**
   * Randomizes the lattice values.
   */
  public void randomize() {
    Random random = new Random();
    for(int iy = 0, my = data[0].length; iy<my; iy++) {
      for(int ix = 0, mx = data.length; ix<mx; ix++) {
        data[ix][iy] = (byte) random.nextInt(256);
        raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]); // sets the image pixel
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
    Color[] colors = new Color[256];
    for(int i = 0; i<256; i++) {
      colors[i] = new Color(rgb[i][0], rgb[i][1], rgb[i][2]);
    }
    lattice.setColorPalette(colors);
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
   * Sets the color palette.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    for(int i = 0, n = Math.min(256, colors.length); i<n; i++) {
      rgb[i][0] = colors[i].getRed();
      rgb[i][1] = colors[i].getGreen();
      rgb[i][2] = colors[i].getBlue();
    }
    for(int i = colors.length; i<256; i++) {
      rgb[i][0] = 0;
      rgb[i][1] = 0;
      rgb[i][2] = 0;
    }
    // set pixels in the raster to correspond to the new color
    for(int ix = 0; ix<nx; ix++) {
      for(int iy = 0; iy<ny; iy++) {
        raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]);
      }
    }
  }

  /**
   * Sets the grid color.
   * @param color
   */
  public void setGridLineColor(Color color) {
    grid.setColor(color);
  }

  /**
   * Sets the color for a single index.
   * @param i
   * @param color
   */
  public void setIndexedColor(int i, Color color) {
    // i = i % rgb.length;
    i = (i+256)%rgb.length;
    rgb[i][0] = color.getRed();
    rgb[i][1] = color.getGreen();
    rgb[i][2] = color.getBlue();
    // set pixels in the raster to correspond to the new color
    for(int ix = 0; ix<nx; ix++) {
      for(int iy = 0; iy<ny; iy++) {
        raster.setPixel(ix, ny-iy-1, rgb[data[ix][iy]&0xFF]);
      }
    }
  }

  /**
   * Creates the default palette.
   */
  public void createDefaultColors() {
    for(int i = 0; i<256; i++) {
      double x = (i<128) ? (i-100)/255.0 : -1;
      double val = Math.exp(-x*x*8);
      rgb[i][0] = (int) (255*val); // red
      x = (i<128) ? i/255.0 : (255-i)/255.0;
      val = Math.exp(-x*x*8);
      rgb[i][1] = (int) (255*val); // green
      x = (i<128) ? -1 : (i-156)/255.0;
      val = Math.exp(-x*x*8);
      rgb[i][2] = (int) (255*val); // blue
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
