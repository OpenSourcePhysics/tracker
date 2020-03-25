/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.opensourcephysics.display.Dimensioned;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.MeasuredImage;

/**
 * A IntegerRaster contains an array of integers where each integer represents an image pixel.
 *
 * Because the image created by a IntegerRaster cannot be resized, the image dimensions
 * are the same as the dimensions of the integer array.
 *
 * @author     Wolfgang Christian
 * @created    February 11, 2003
 * @version    1.0
 */
public class IntegerRaster extends MeasuredImage implements Dimensioned {
  public static int WHITE = 0xFFFFFF;
  WritableRaster raster;
  byte[][] rgbData;
  int nrow, ncol;
  boolean visible = true;
  Dimension dimension;
  protected double scaleFactor = 1;

  /**
   * Constructs IntegerRaster with the given size.
   * @param _nrow the number of rows
   * @param _ncol the number of columns
   */
  public IntegerRaster(int _nrow, int _ncol) {
    nrow = _nrow;
    ncol = _ncol;
    dimension = new Dimension(ncol, nrow);
    int size = nrow*ncol;
    ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8}, false, // hasAlpha
      false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, ncol, nrow, ncol, new int[] {0, 1, 2}, new int[] {0, 0, 0});
    rgbData = new byte[3][size];
    DataBuffer databuffer = new DataBufferByte(rgbData, size);
    WritableRaster raster = Raster.createWritableRaster(csm, databuffer, new Point(0, 0));
    image = new BufferedImage(ccm, raster, false, null);
    // col in x direction, row in y direction
    xmin = 0;
    xmax = ncol;
    ymin = nrow;
    ymax = 0; // zero is on top
  }

  /**
   * Sets a block of lattice cells to new values.
   *
   * @param row_offset
   * @param col_offset
   * @param val
   */
  public void setBlock(int row_offset, int col_offset, int val[][]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val[0].length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      for(int cindex = col_offset, nc = val[0].length+col_offset; cindex<nc; cindex++) {
        int index = rindex*ncol+cindex;
        int pixval = val[rindex-row_offset][cindex-col_offset];
        rgbData[0][index] = (byte) ((pixval>>16)&0xFF); // red
        rgbData[1][index] = (byte) ((pixval>>8)&0xFF);  // green
        rgbData[2][index] = (byte) ((pixval>>0)&0xFF);  // blue
      }
    }
  }

  /**
   * Sets a row of lattice cells to new values.
   *
   * @param row
   * @param col_offset
   * @param val
   */
  public void setRow(int row, int col_offset, int val[]) {
    if((row<0)||(row>=nrow)) {
      throw new IllegalArgumentException("Row index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val.length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    for(int cindex = col_offset, nc = val.length+col_offset; cindex<nc; cindex++) {
      int index = row*ncol+cindex;
      int pixval = val[cindex-col_offset];
      rgbData[0][index] = (byte) ((pixval>>16)&0xFF); // red
      rgbData[1][index] = (byte) ((pixval>>8)&0xFF);  // green
      rgbData[2][index] = (byte) ((pixval>>0)&0xFF);  // blue
    }
  }

  /**
 * Sets a column of lattice cells to new values.
 *
 * @param row_offset
 * @param col
 * @param val
 */
  public void setCol(int row_offset, int col, int val[]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    if((col<0)||(col>=ncol)) {
      throw new IllegalArgumentException("Column index out of range in integer raster setBlock."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      int index = rindex*ncol+col;
      int pixval = val[rindex-row_offset];
      rgbData[0][index] = (byte) ((pixval>>16)&0xFF); // red
      rgbData[1][index] = (byte) ((pixval>>8)&0xFF);  // green
      rgbData[2][index] = (byte) ((pixval>>0)&0xFF);  // blue
    }
  }

  /**
   * Sets a lattice cell to a new value.
   */
  public void setCell(int _row, int _col, int val) {
    int index = _row*ncol+_col;
    rgbData[0][index] = (byte) ((val>>16)&0xFF); // red
    rgbData[1][index] = (byte) ((val>>8)&0xFF);  // green
    rgbData[2][index] = (byte) ((val>>0)&0xFF);  // blue
  }

  /**
   * Gets a lattice cell value.
   *
   * @return the cell value.
   */
  public int getCell(int _row, int _col) {
    int index = _row*ncol+_col;
    return((rgbData[0][index]&0xFF)<<16)|((rgbData[1][index]&0xFF)<<8)|((rgbData[2][index]&0xFF)<<0);
  }

  /**
   * Gets the dimension of the lattice in pixel units.
   *
   * @param panel
   * @return the dimension
   */
  public Dimension getInterior(DrawingPanel panel) {
    float availableWidth = panel.getWidth()-panel.getLeftGutter()-panel.getRightGutter()-1;
    float availableHeight = panel.getHeight()-panel.getTopGutter()-panel.getBottomGutter()-1;
    scaleFactor = Math.min(availableWidth/dimension.width, availableHeight/dimension.height);
    if(scaleFactor>1) {
      scaleFactor = 1;
      return dimension;
    }
    return new Dimension((int) (scaleFactor*ncol), (int) (scaleFactor*nrow));
  }

  /**
   * Draws the image and the grid.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(scaleFactor<1) {
      g.drawImage(image.getScaledInstance((int) (scaleFactor*image.getWidth()), (int) (scaleFactor*image.getHeight()), java.awt.Image.SCALE_REPLICATE), panel.getLeftGutter(), panel.getTopGutter(), panel);
    } else {
      //g.drawImage(image, 1+panel.xToPix(xmin), 1+panel.yToPix(ymax), panel);
      g.drawImage(image, panel.getLeftGutter(), panel.getTopGutter(), panel);
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
