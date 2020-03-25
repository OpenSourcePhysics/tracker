/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Random;
import javax.swing.JFrame;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.OSPRuntime;

/**
 * A BinaryLattice is an array where each array element has a value of 0 or 1.
 *
 * The lattice is drawn as an array of rectangles to distinguish between the two possible values.
 *
 * @author     Wolfgang Christian
 * @created    February 11, 2003
 * @version    1.0
 */
public class BinaryLattice extends MeasuredImage implements ByteLattice {
  WritableRaster raster;
  Grid grid;
  byte[] packedData;
  int ny, nx;
  boolean visible = true;
  Color zeroColor = Color.red, oneColor = Color.blue;

  /**
   * Constructs a binary lattice with the given size.
   * @param _nx the number of values in x direction
   * @param _ny the number of values in y direction
   */
  public BinaryLattice(int _nx, int _ny) {
    ny = _ny;
    nx = _nx;
    int len = ((nx+7)/8)*ny; // each row starts on a byte boundary
    packedData = new byte[len];
    DataBuffer databuffer = new DataBufferByte(packedData, len);
    raster = Raster.createPackedRaster(databuffer, nx, ny, 1, null);
    // default colors are red and blue
    ColorModel colorModel = new IndexColorModel(1, 2, new byte[] {(byte) 255, (byte) 0}, new byte[] {(byte) 0, (byte) 0}, new byte[] {(byte) 0, (byte) 255});
    image = new BufferedImage(colorModel, raster, false, null);
    xmin = 0;
    xmax = nx;
    ymin = 0;
    ymax = ny;
    grid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    grid.setColor(Color.lightGray);
  }

  /**
   * Creates the default palette.
   */
  public void createDefaultColors() {
    zeroColor = Color.red;
    oneColor = Color.blue;
  }

  /**
   * Resize the lattice.
   * @param _nx number of x sites
   * @param _ny number of y sites
   */
  public void resizeLattice(int _nx, int _ny) {
    ny = _ny;
    nx = _nx;
    int len = ((nx+7)/8)*ny; // each row starts on a byte boundary
    packedData = new byte[len];
    DataBuffer databuffer = new DataBufferByte(packedData, len);
    raster = Raster.createPackedRaster(databuffer, nx, ny, 1, null);
    ColorModel colorModel = image.getColorModel();
    image = new BufferedImage(colorModel, raster, false, null);
    Color color = grid.getColor();
    grid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    setMinMax(xmin, xmax, ymin, ymax);
    grid.setColor(color);
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
   * Randomizes the lattice values.
   */
  public void randomize() {
    Random random = new Random();
    random.nextBytes(packedData);
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
    if (!OSPRuntime.isMac()) {  //Rendering hint bug in Mac Snow Leopard 
      Graphics2D g2 = ((Graphics2D) g);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
   * Scales the grid to the given values in world units.
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
   * Sets a block of cells to new values.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param row_offset
   * @param col_offset
   * @param val  the array of values
   */

  /**
   * Sets a block of cells to new values.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param x_offset int
   * @param y_offset int
   * @param val int[][]
   */
  public void setBlock(int x_offset, int y_offset, int val[][]) {
    if((y_offset<0)||(y_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((x_offset<0)||(x_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int iy = y_offset, my = val[0].length+y_offset; iy<my; iy++) {
      for(int ix = x_offset, mx = val.length+x_offset; ix<mx; ix++) {
        int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
        byte packedcell = packedData[arrayIndex];
        int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
        if(val[ix-x_offset][iy-y_offset]<=0) {
          packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
        } else {
          packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
        }
        packedData[arrayIndex] = packedcell;
      }
    }
  }

  /**
   * Sets a block of cells to new values.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param ix_offset
   * @param iy_offset
   * @param val  the value array
   */
  public void setBlock(int ix_offset, int iy_offset, byte val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
        byte packedcell = packedData[arrayIndex];
        int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
        if(val[ix-ix_offset][iy-iy_offset]<=0) {
          packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
        } else {
          packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
        }
        packedData[arrayIndex] = packedcell;
      }
    }
  }

  /**
   * Sets a block of data starting at (0,0) to new values.
   *
   * @param val
  */
  public void setBlock(byte[][] val) {
    setBlock(0, 0, val);
  }

  /**
   * Sets a column of cells to new values.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param ix
   * @param iy_offset
   * @param val  the array of values
   */
  public void setCol(int ix, int iy_offset, int val[]) {
    if((iy_offset<0)||(iy_offset+val.length>ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, nr = val.length+iy_offset; iy<nr; iy++) {
      int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
      if(val[iy-iy_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   * Sets a column of cells to new values.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param ix
   * @param iy_offset
   * @param val  the array of values
   */
  public void setCol(int ix, int iy_offset, byte val[]) {
    if((iy_offset<0)||(iy_offset+val.length>ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, nr = val.length+iy_offset; iy<nr; iy++) {
      int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
      if(val[iy-iy_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   * Sets a row of cells to new values starting at the given column.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param iy
   * @param ix_offset  the x offset
   * @param val the value array
   */
  public void setRow(int iy, int ix_offset, int val[]) {
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int ix = ix_offset, nc = val.length+ix_offset; ix<nc; ix++) {
      int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
      if(val[ix-ix_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   * Sets a row of cells to new values starting at the given column.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param iy
   * @param ix_offset  the x offset
   * @param val the value array
   */
  public void setRow(int iy, int ix_offset, byte val[]) {
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int ix = ix_offset, nc = val.length+ix_offset; ix<nc; ix++) {
      int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
      if(val[ix-ix_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   * Sets a cell at the given location to a new value.
   *
   * A cell should take on a value of 0 or 1.
   * @param ix
   * @param iy
   * @param val
   */
  public void setValue(int ix, int iy, int val) {
    if((iy<0)||(iy>=ny)||(ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("Cell row or column index out of range.  row="+iy+"  col="+ix); //$NON-NLS-1$ //$NON-NLS-2$
    }
    int arrayIndex = (ny-iy-1)*((nx+7)/8)+ix/8; // each row starts on a byte boundary
    byte packedcell = packedData[arrayIndex];
    int mask = 0x80>>>(ix%8);                   // start with 0x10000000 and shift right
    if(val<=0) {
      packedcell = (byte) (packedcell&~mask); // AND bitwise with mask complement will clear index bit
    } else {
      packedcell = (byte) (packedcell|mask);  // OR bitwise with mask operation will set index bit
    }
    packedData[arrayIndex] = packedcell;
  }

  public void setValue(int ix, int iy, byte val) {
    setValue(ix, iy, (int) val);
  }

  /**
   * Gets a value from the given location.
   *
   * Cell values are zero or one.
   *
   * @param ix
   * @param iy
   * @return the cell value.
   */
  public byte getValue(int ix, int iy) {
    byte packedcell = packedData[(ny-iy-1)*((nx+7)/8)+ix/8]; // each row starts on a byte boundary
    int mask = 0x80>>>(ix%8);                                // start with 0x10000000 and shift right
    if((packedcell&mask)>0) {
      return 1;
    }
    return 0;
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
   * Sets the show grid option.
   *
   * @param showGrid
   */
  public void setShowGrid(boolean showGrid) {
    grid.setVisible(showGrid);
  }

  /**
   * Sets color palette.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    zeroColor = colors[0];
    oneColor = colors[1];
    ColorModel colorModel = new IndexColorModel(1, 2, new byte[] {(byte) zeroColor.getRed(), (byte) oneColor.getRed()}, // red
      new byte[] {(byte) zeroColor.getGreen(), (byte) oneColor.getGreen()},  // green
        new byte[] {(byte) zeroColor.getBlue(), (byte) oneColor.getBlue()},  // blue
        new byte[] {(byte) zeroColor.getAlpha(), (byte) oneColor.getAlpha()}); // alpha
    image = new BufferedImage(colorModel, raster, false, null);
  }

  /**
   * Sets the color for a single index.
   * @param i
   * @param color
   */
  public void setIndexedColor(int i, Color color) {
    if(i==0) {
      zeroColor = color;
    } else {
      oneColor = color;
    }
    ColorModel colorModel = new IndexColorModel(1, 2, new byte[] {(byte) zeroColor.getRed(), (byte) oneColor.getRed()}, // red
      new byte[] {(byte) zeroColor.getGreen(), (byte) oneColor.getGreen()},  // green
      new byte[] {(byte) zeroColor.getBlue(),  (byte) oneColor.getBlue()}, // blue
      new byte[] {(byte) zeroColor.getAlpha(), (byte) oneColor.getAlpha()}); // alpha
    image = new BufferedImage(colorModel, raster, false, null);
  }

  /**
   * Sets the grid color.
   * @param color
   */
  public void setGridLineColor(Color color) {
    grid.setColor(color);
  }

  public void setShowGridLines(boolean showGridLines) {
    grid.setVisible(showGridLines);
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

  public JFrame showLegend() {
    return null;
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
