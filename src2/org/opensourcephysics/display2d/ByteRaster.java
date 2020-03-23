/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.Dimensioned;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 * A ByteRaster contains an array of bytes where each byte represents an image pixel.
 *
 * The image dimensions are the same as the dimensions of the byte array.
 *
 * @author     Wolfgang Christian
 * @created    May 21, 2003
 * @version    1.0
 */
public class ByteRaster extends MeasuredImage implements Dimensioned, ByteLattice {
  boolean allowRescale=false;
  WritableRaster raster;
  ColorModel colorModel;
  byte[] packedData;
  int ny, nx;
  Dimension dimension;
  protected double scaleFactor = 1;
  Color gridColor = Color.lightGray;
  boolean showGrid = false;
  byte[] reds = new byte[256];
  byte[] greens = new byte[256];
  byte[] blues = new byte[256];
  private JFrame legendFrame;

  /**
   * Constructs a byte raster with the given size.
   *
   * Unsigned cell values are 0 to 255. Signed cell values are -128 to 127.
   *
   * @param _nx the number of values in x direction
   * @param _ny the number of values in y direction
   */
  public ByteRaster(int _nx, int _ny) {
    ny = _ny;
    nx = _nx;
    dimension = new Dimension(nx-1, ny-1); // decrease by one to fit inside axes
    int len = nx*ny;
    packedData = new byte[len];
    DataBuffer databuffer = new DataBufferByte(packedData, len);
    raster = Raster.createPackedRaster(databuffer, nx, ny, 8, null);
    colorModel = createColorModel();
    image = new BufferedImage(colorModel, raster, false, null);
    xmin = 0;
    xmax = nx;
    ymin = 0;
    ymax = ny;
  }

  /**
   * Resizes the raster using the given number of x and y entries.
   * Implementation of ByteLattice interface.
   *
   * @param nx the number of x entries
   * @param ny the number of y entries
   */
  public void resizeLattice(int nx, int ny) {
    resizeRaster(nx, ny);
    xmin = 0;
    xmax = nx;
    ymin = 0;
    ymax = ny;
  }
  
  private boolean isUnderEjs = false;

  public void setUnderEjs(boolean underEjs) {
    isUnderEjs = underEjs;
  }

  /**
   * Resizes the raster using the given number of x and y entries.
   * @param _nx the number of x entries
   * @param _ny the number of y entries
   */
  public void resizeRaster(int _nx, int _ny) {
    ny = _ny;
    nx = _nx;
    dimension = new Dimension(nx-1, ny-1); // decrease by one to fit inside axes
    int len = nx*ny;
    packedData = new byte[len];
    DataBuffer databuffer = new DataBufferByte(packedData, len);
    raster = Raster.createPackedRaster(databuffer, nx, ny, 8, null);
    image = new BufferedImage(colorModel, raster, false, null);
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
   * Randomizes the lattice values.
   */
  public void randomize() {
    Random random = new Random();
    random.nextBytes(packedData);
  }

  /**
   * Gets the dimension of the lattice in pixel units.
   *
   * @param panel
   * @return the dimension
   */
  public Dimension getInterior(DrawingPanel panel) {
	if(allowRescale) return null;
    float availableWidth = panel.getWidth()-panel.getLeftGutter()-panel.getRightGutter()-1;
    float availableHeight = panel.getHeight()-panel.getTopGutter()-panel.getBottomGutter()-1;
    scaleFactor = Math.min(availableWidth/dimension.width, availableHeight/dimension.height);
    if(scaleFactor>1) {
      scaleFactor = 1;
      return dimension;
    }
    return new Dimension((int) (scaleFactor*(nx-0.5)), (int) (scaleFactor*(ny-0.5)));
  }
  
  /**
   * Draws the image on the panel.
   *
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(allowRescale){
    	super.draw(panel, g);
    }else{  // raster will be drawn without scale distortion
      if((scaleFactor<1)&&!isUnderEjs) {
        g.drawImage(image.getScaledInstance((int) (scaleFactor*image.getWidth()), (int) (scaleFactor*image.getHeight()), java.awt.Image.SCALE_REPLICATE), panel.getLeftGutter(), panel.getTopGutter(), panel);
      } else {
        g.drawImage(image, panel.getLeftGutter(), panel.getTopGutter(), panel);
      }
    }
    if(showGrid) {
       g.setColor(gridColor);
      g.drawRect(panel.getLeftGutter(), panel.getTopGutter(), (int) dimension.getWidth(), (int) dimension.getHeight());
    }
  }
  
  /**
   * Image can rescale within drawing panel.
   * @param allow
   */
  public void setAllowRescale(boolean allow){
	  allowRescale=allow;
  }
  
 /**
 * Get the allowRescale flag.
 * 
 * @return true if image will rescale
 */
  public boolean getAllowRescale(){
	  return allowRescale;
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
   * Sets a block of values starting at location (0,0).
   *
   * A pixel is set to 1 if the value is >0; the cell is set to zero otherwise
   * @param val
   */
  public void setBlock(byte val[][]) {
    setBlock(0, 0, val);
  }

  /**
   * Sets a block of values using byte data.
   *
   * A pixel is set to 1 if the value is >0; the cell is set to zero otherwise
   * @param ix_offset
   * @param iy_offset
   * @param val
   */
  public void setBlock(int ix_offset, int iy_offset, byte val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Row index out of range in byte raster setBlock."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in byte raster setBlock."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        packedData[(ny-iy-1)*nx+ix] = val[ix-ix_offset][iy-iy_offset];
      }
    }
  }

  /**
   * Sets a block of values using integer data.
   *
   * A pixel is set to 1 if the value is >0; the cell is set to zero otherwise
   * @param ix_offset
   * @param iy_offset
   * @param val
   */
  public void setBlock(int ix_offset, int iy_offset, int val[][]) {
    if((iy_offset<0)||(iy_offset+val[0].length>ny)) {
      throw new IllegalArgumentException("Row index out of range in byte raster setBlock."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in byte raster setBlock."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val[0].length+iy_offset; iy<my; iy++) {
      for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
        packedData[(ny-iy-1)*nx+ix] = (byte) val[ix-ix_offset][iy-iy_offset]; // only change = typecast
      }
    }
  }

  /**
   * Sets a column of values.
   *
   * @param ix the x index of the column
   * @param iy_offset the y offset in the column
   * @param val values in column
   */
  public void setCol(int ix, int iy_offset, byte val[]) {
    if((iy_offset<0)||(iy_offset+val.length>ny)) {
      throw new IllegalArgumentException("Row index out of range in byte raster setCol."); //$NON-NLS-1$
    }
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("Column index out of range in byte raster setCol."); //$NON-NLS-1$
    }
    for(int iy = iy_offset, my = val.length+iy_offset; iy<my; iy++) {
      packedData[(ny-iy-1)*nx+ix] = val[iy-iy_offset];
    }
  }

  /**
   * Sets a row of cells to new values starting at the given column.
   *
   * A cell is set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param iy  the row that will be set
   * @param ix_offset  the offset
   * @param val the value
   */
  public void setRow(int iy, int ix_offset, byte val[]) {
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((ix_offset<0)||(ix_offset+val.length>nx)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int ix = ix_offset, mx = val.length+ix_offset; ix<mx; ix++) {
      packedData[(ny-iy-1)*nx+ix] = val[ix-ix_offset];
    }
  }

  /**
   * Sets a pixel at the given location to a new value.
   *
   * @param ix
   * @param iy
   * @param val
   */
  public void setValue(int ix, int iy, byte val) {
    packedData[(ny-iy-1)*nx+ix] = val;
  }

  /**
   * Gets a raster value from the given location.
   *
   * @param ix
   * @param iy
   * @return the cell value.
   */
  public byte getValue(int ix, int iy) {
    return packedData[(ny-iy-1)*nx+ix];
  }

  /**
   * Sets the black and white palette.
   */
  public void setBWPalette() {
    Color[] bwPalette = new Color[256];
    for(int i = 0; i<256; i++) {
      bwPalette[i] = new Color(i, i, i);
    }
    setColorPalette(bwPalette);
  }

  /**
   * Sets the color palette to the given array of colors.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    int numColors = colors.length;
    reds = new byte[numColors];
    greens = new byte[numColors];
    blues = new byte[numColors];
    for(int i = 0; i<numColors; i++) {
      reds[i] = (byte) colors[i].getRed();
      greens[i] = (byte) colors[i].getGreen();
      blues[i] = (byte) colors[i].getBlue();
    }
    colorModel = new IndexColorModel(8, numColors, reds, greens, blues);
    image = new BufferedImage(colorModel, raster, false, null);
  }

  /**
   * Gets the current palette.
   * @return byte[][]
   */
  public byte[][] getColorPalette() {
    byte[][] palette = new byte[3][];
    palette[0] = reds;
    palette[1] = greens;
    palette[2] = blues;
    return palette;
  }

  /**
   * Sets the default palette.
   */
  public void createDefaultColors() {
    colorModel = createColorModel();
    image = new BufferedImage(colorModel, raster, false, null);
  }

  /**
   * Sets the color for a single index.
   *
   * @param i
   * @param color
   */
  public void setIndexedColor(int i, Color color) {
    // i         = i % reds.length;
    i = (i+256)%reds.length;
    reds[i] = (byte) color.getRed();
    greens[i] = (byte) color.getGreen();
    blues[i] = (byte) color.getBlue();
    colorModel = new IndexColorModel(8, 256, reds, greens, blues);
    image = new BufferedImage(colorModel, raster, false, null);
  }

  /**
   * Shows the color associated with each value.
   */
  public JFrame showLegend() {
    InteractivePanel dp = new InteractivePanel();
    dp.setPreferredSize(new java.awt.Dimension(300, 80));
    dp.setPreferredGutters(0, 0, 0, 35);
    dp.setClipAtGutter(false);
    if((legendFrame==null)||!legendFrame.isDisplayable()) {
      legendFrame = new JFrame(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    }
    legendFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    legendFrame.setResizable(false);
    legendFrame.setContentPane(dp);
    ByteRaster byteRaster = new ByteRaster(256, 20);
    byteRaster.setMinMax(-128, 127, 0, 1);
    byte[][] data = new byte[256][20];
    for(int i = 0; i<256; i++) {
      for(int j = 0; j<20; j++) {
        data[i][j] = (byte) (-128+i);
      }
    }
    byteRaster.setBlock(0, 0, data);
    Color[] colors = new Color[256];
    for(int i = 0; i<256; i++) {
      colors[(128+i)%256] = new Color((256+reds[i])%256, (256+greens[i])%256, (256+blues[i])%256);
    }
    byteRaster.setColorPalette(colors);
    dp.addDrawable(byteRaster);
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
   * Outlines the lattice boundaries with a grid.
   *
   * @param showGridLines
   */
  public void setShowGridLines(boolean showGridLines) {
    showGrid = showGridLines;
  }

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c) {
    gridColor = c;
  }

  /**
   *  Creates the default color model.
   *
   * @return
   */
  ColorModel createColorModel() {
    reds = new byte[256];
    greens = new byte[256];
    blues = new byte[256];
    for(int i = 0; i<256; i++) {
      double x = (i<128) ? (i-100)/255.0 : -1;
      double val = Math.exp(-x*x*8);
      reds[i] = (byte) (255*val);
      x = (i<128) ? i/255.0 : (255-i)/255.0;
      val = Math.exp(-x*x*8);
      greens[i] = (byte) (255*val);
      x = (i<128) ? -1 : (i-156)/255.0;
      val = Math.exp(-x*x*8);
      blues[i] = (byte) (255*val);
    }
    ColorModel colorModel = new IndexColorModel(8, 256, reds, greens, blues);
    return colorModel;
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
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  public double indexToX(int i) {
    double xMin = getXMin();
    double xMax = getXMax();
    return xMin+i*(xMax-xMin)/getNx();
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
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  public double indexToY(int i) {
    double yMin = getYMin();
    double yMax = getYMax();
    return yMin+i*(yMax-yMin)/getNy();
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
