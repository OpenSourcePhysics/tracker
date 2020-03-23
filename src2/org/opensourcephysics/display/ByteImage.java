/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.OSPRuntime;

/**
 * A ByteImage contains an array of bytes int[row][col] 
 * where each integer represents an image pixel.  The row index determines the y-location
 * of the pixel and the col index determines the x-location in the drawing panel.
 * 
 * @author Wolfgang Christian
 * @created March 3, 2012
 * @version 1.0
 */
public class ByteImage implements Measurable {
	byte[] imagePixels; // array that gets mapped onto the image
	MemoryImageSource imageSource;  // object that converts the array to an image
	Image image;       // image to be rendered in drawing panel
	int nrow, ncol;    // number of rows and column in array
	double xmin, xmax, ymin, ymax; // drawing scale
	boolean visible = true;
	boolean dirtyImage=true;  // true if array elements have changed
	
	/**
	 * Gets a two-color ByteImage with 0 -> red and 1 -> blue.
	 */
	static public ByteImage getBinaryImage(byte[][] data){
	  ColorModel colorModel = new IndexColorModel(1, 2, 
			  new byte[] {(byte) 255, (byte) 0}, 
			  new byte[] {(byte) 0, (byte) 0}, 
			  new byte[] {(byte) 0, (byte) 255});
	  return  new ByteImage(colorModel, data);
	}
	
	
	// Gets an ByteImage with the given color palette and data.
	static public ByteImage getColorImage(Color[] colors, byte[][] data){
		int n=colors.length;
	    byte [] reds = new byte[n];
	    byte [] greens = new byte[n];
	    byte [] blues = new byte[n];
	    for(int i = 0; i<n; i++) {
	      reds[i] = (byte) (colors[i].getRed());
	      greens[i] = (byte) (colors[i].getGreen());
	      blues[i] = (byte) (colors[i].getBlue());
	    }
	    ColorModel colorModel = new IndexColorModel(8, n, reds, greens, blues);
		  return  new ByteImage(colorModel, data);
	}

	/**
	 * Constructs ByteImage with the given data.
	 * 
	 * @param data 
	 */
	public ByteImage(byte[][] data) {
	    byte [] reds = new byte[256];
	    byte [] greens = new byte[256];
	    byte [] blues = new byte[256];
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
		nrow = data.length;
		ncol = data[0].length;
		imagePixels = new byte[nrow * ncol];
		for (int i = 0; i < nrow; i++) {
			byte[] row = data[i];
			System.arraycopy(row, 0, imagePixels, i * ncol, ncol);
		}
		imageSource = new MemoryImageSource(ncol, nrow,colorModel, imagePixels, 0, ncol);
		imageSource.setAnimated(true);
		image = Toolkit.getDefaultToolkit().createImage(imageSource);
		dirtyImage=false;
		xmin = 0;
		xmax = ncol;
		ymin = nrow;
		ymax = 0; // zero is on top
	}
	
	/**
	 * Constructs IntegerImage with the given ColorModel and data.
	 * 
	 * @param colorModel
	 * @param data 
	 */
	public ByteImage(ColorModel colorModel, byte[][] data) {
		nrow = data.length;
		ncol = data[0].length;
		imagePixels = new byte[nrow * ncol];
		for (int i = 0; i < nrow; i++) {
			byte[] row = data[i];
			System.arraycopy(row, 0, imagePixels, i * ncol, ncol);
		}
		imageSource = new MemoryImageSource(ncol, nrow,colorModel, imagePixels, 0, ncol);
		imageSource.setAnimated(true);
		image = Toolkit.getDefaultToolkit().createImage(imageSource);
		dirtyImage=false;
		xmin = 0;
		xmax = ncol;
		ymin = nrow;
		ymax = 0; // zero is on top
	}
	
	/**
	 * Sets new values assuming that the integer array has not changed.
	 * 
	 * @param val
	 */
	public void updateImage(byte[][] val) {
	  for (int i = 0; i < nrow; i++) {
		  byte[] row = val[i];
			System.arraycopy(row, 0, imagePixels, i * ncol, ncol);
	  }
      // image width is ncol and image height is nrow 
	  imageSource.newPixels(0, 0,ncol,nrow);  // not needed?
	  dirtyImage=true;
	}

	/**
	 * Sets an offset block to new values.
	 * 
	 * @param row_offset
	 * @param col_offset
	 * @param val
	 */
	public void setBlock(int row_offset, int col_offset, byte[][] val) {
		if(val==null) return;
		int block_nrow = val.length;
		int block_ncol = val[0].length;
		if ((row_offset < 0) || (row_offset + block_nrow > nrow)) {
			throw new IllegalArgumentException(
					"Row index out of range in IntegerImage setBlock."); //$NON-NLS-1$
		}
		if ((col_offset < 0) || (col_offset + block_ncol > ncol)) {
			throw new IllegalArgumentException(
					"Column index out of range in IntegerImage setBlock."); //$NON-NLS-1$
		}
		for (int ir =0; ir < block_nrow; ir++) {
			byte[] row=val[ir];
			int index = (ir+row_offset) * ncol +col_offset;
			System.arraycopy(row, 0, imagePixels, index, block_ncol);
		}
		// image width is ncol and image height is nrow 
		// imageSource.newPixels(col_offset, row_offset,block_ncol,block_nrow);  // not needed?
		dirtyImage=true;
	}

	/**
	 * Sets array elements in a row to new values.
	 * 
	 * @param row
	 * @param val
	 */
	public void setRow(int row, byte[] val) {
		if(val==null) return;
		if ((row < 0) || (row >= nrow)) {
			throw new IllegalArgumentException("Row index out of range in IntegerImage setRow."); //$NON-NLS-1$
		}
		if (val.length > ncol) {
			throw new IllegalArgumentException(
					"Column index out of range in IntegerImage setRow."); //$NON-NLS-1$
		}
		System.arraycopy(val, 0, imagePixels, row * ncol, val.length);
		// image width is ncol and image height is nrow 
		// imageSource.newPixels(0,row,val.length,1);  // not needed?
		dirtyImage=true;
	}

	/**
	 * Sets a column to new values.
	 * 
	 * @param col
	 * @param val
	 */
	public void setCol(int col, byte[] val) {
		if(val==null) return;
		if (val.length > nrow) {
			throw new IllegalArgumentException(
					"Row index out of range in IntegerImage setCol."); //$NON-NLS-1$
		}
		if ((col < 0) || (col >= ncol)) {
			throw new IllegalArgumentException(
					"Column index out of range in IntegerImage setCol."); //$NON-NLS-1$
		}
		for (int rindex = 0, nr = val.length; rindex < nr; rindex++) {
			imagePixels[rindex * ncol + col] = val[rindex];
		}
		// image width is ncol and image height is nrow 
		// imageSource.newPixels(col,0,1,val.length);  // not needed?
		dirtyImage=true;
	}

	/**
	 * Sets a cell to a new value.
	 */
	public void setCell(int row, int col, byte val) {
		imagePixels[row * ncol + col]=val;
		imageSource.newPixels(row, col,1,1);
		dirtyImage=true;
	}

	/**
	 * Draws the image.
	 * 
	 * @param panel
	 * @param g
	 */

	public void draw(DrawingPanel panel, Graphics g) {
		if (!visible) {
			return;
		}
		if(dirtyImage){
		  image = Toolkit.getDefaultToolkit().createImage(imageSource);
		}
		if (image == null) {
			panel.setMessage(DisplayRes.getString("Null Image")); //$NON-NLS-1$
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform gat = g2.getTransform(); // save graphics transform
		RenderingHints hints = g2.getRenderingHints();
		if (!OSPRuntime.isMac()) { // Rendering hint bug in Mac Snow Leopard
			g2.setRenderingHint(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_DISABLE);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		double sx = (xmax - xmin) * panel.xPixPerUnit / ncol;
		double sy = (ymax - ymin) * panel.yPixPerUnit / nrow;
		// translate origin to pixel location of (xmin,ymax)
		g2.transform(AffineTransform.getTranslateInstance(panel.leftGutter
				+ panel.xPixPerUnit * (xmin - panel.xmin), panel.topGutter
				+ panel.yPixPerUnit * (panel.ymax - ymax)));
		g2.transform(AffineTransform.getScaleInstance(sx, sy));  // scales image to world units
		g2.drawImage(image, 0, 0, panel);
		g2.setTransform(gat); // restore graphics conext
		g2.setRenderingHints(hints); // restore the hints
	}

	public boolean isMeasured() {
		if (image == null) {
			return false;
		}
		return true;
	}

	public double getXMin() {
		return xmin;
	}

	public double getXMax() {
		return xmax;
	}

	public double getYMin() {
		return ymin;
	}

	public double getYMax() {
		return ymax;
	}

	public void setXMin(double _xmin) {
		xmin = _xmin;
	}

	public void setXMax(double _xmax) {
		xmax = _xmax;
	}

	public void setYMin(double _ymin) {
		ymin = _ymin;
	}

	public void setYMax(double _ymax) {
		ymax = _ymax;
	}

	public void setMinMax(double _xmin, double _xmax, double _ymin, double _ymax) {
		xmin = _xmin;
		xmax = _xmax;
		ymin = _ymin;
		ymax = _ymax;
	}
}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2017 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
