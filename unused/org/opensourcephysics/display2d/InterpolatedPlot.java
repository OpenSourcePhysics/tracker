/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
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
import javax.swing.JFrame;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.MeasuredImage;

/**
 * InterpolatedPlot creates an image of a scalar field by interpolating every
 * image pixel to an untabulated point (x,y) in the 2d data.  This interpolation smooths
 * the resulting image.
 *
 * @author     Wolfgang Christian
 * @created    February 2, 2003
 * @version    1.0
 */
public class InterpolatedPlot extends MeasuredImage implements Plot2D {
  GridData griddata;
  byte[][] rgbData;
  Grid grid;
  ColorMapper colorMap;
  boolean autoscaleZ = true;
  boolean symmetricZ = false;
  int ampIndex = 0; // amplitude index
  int leftPix, rightPix, topPix, bottomPix;
  int ixsize, iysize;
  double top, left, bottom, right;

  /**
   * Constructs an InterpolatedPlot without data.
   */
  public InterpolatedPlot() {
    this(null);
  }

  /**
   * Constructs the InterpolatedPlot using the given data storage.
   * @param _griddata
   */
  public InterpolatedPlot(GridData _griddata) {
    griddata = _griddata;
    colorMap = new ColorMapper(100, -1, 1, ColorMapper.SPECTRUM);
    if(griddata==null) {
      grid = new Grid(1, 1, xmin, xmax, ymin, ymax);
    } else {
      grid = new Grid(griddata.getNx(), griddata.getNy(), xmin, xmax, ymin, ymax);
    }
    grid.setColor(Color.lightGray);
    grid.setVisible(false);
    update();
  }

  /**
   * Gets the byte array of rgb colors
   * @return byte[][]
   */
  public byte[][] getRGBData() {
    return this.rgbData;
  }

  /**
   * Gets the GridData object.
   * @return GridData
   */
  public GridData getGridData() {
    return griddata;
  }

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  public double indexToX(int i) {
    return griddata.indexToX(i);
  }

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  public double indexToY(int i) {
    return griddata.indexToY(i);
  }

  /**
   * Gets closest index from the given x  world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  public int xToIndex(double x) {
    return griddata.xToIndex(x);
  }

  /**
   * Gets closest index from the given y  world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  public int yToIndex(double y) {
    return griddata.yToIndex(y);
  }

  /**
   * Sets the data to new values.
   *
   * The grid is resized to fit the new data if needed.
   *
   * @param obj
   */
  public void setAll(Object obj) {
    double[][] val = (double[][]) obj;
    copyData(val);
    update();
  }

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
  public void setAll(Object obj, double xmin, double xmax, double ymin, double ymax) {
    double[][] val = (double[][]) obj;
    copyData(val);
    if(griddata.isCellData()) {
      griddata.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      griddata.setScale(xmin, xmax, ymin, ymax);
    }
    update();
  }

  private void copyData(double val[][]) {
    if((griddata!=null)&&!(griddata instanceof ArrayData)) {
      throw new IllegalStateException("SetAll only supports ArrayData for data storage."); //$NON-NLS-1$
    }
    if((griddata==null)||(griddata.getNx()!=val.length)||(griddata.getNy()!=val[0].length)) {
      griddata = new ArrayData(val.length, val[0].length, 1);
      setGridData(griddata);
    }
    double[][] data = griddata.getData()[0];
    int ny = data[0].length;
    for(int i = 0, nx = data.length; i<nx; i++) {
      System.arraycopy(val[i], 0, data[i], 0, ny);
    }
  }

  /**
   * Sets the data to the given griddata.
   *
   * @param _griddata
   */
  public void setGridData(GridData _griddata) {
    griddata = _griddata;
    if(this.griddata==null) {
      return;
    }
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    Grid newGrid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    if(grid!=null) {
      newGrid.setColor(grid.getColor());
      newGrid.setVisible(grid.isVisible());
    }
    grid = newGrid;
    update();
  }

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
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    autoscaleZ = isAutoscale;
    if(!autoscaleZ) {
      colorMap.setScale(floor, ceil);
    }
    update(); // recolor the image with the new scale
  }
  
  /**
   * Forces the z-scale to be symmetric about zero.
   * Forces zmax to be positive and zmin=-zmax when in autoscale mode.
   *
   * @param symmetric
   */
  public void setSymmetricZ(boolean symmetric){
	  symmetricZ=symmetric;
  }
  
  /**
   * Gets the symmetric z flag.  
   */
  public boolean isSymmetricZ(){
	  return symmetricZ;
  }

  /**
   * Gets the autoscale flag for z.
   *
   * @return boolean
   */
  public boolean isAutoscaleZ() {
    return autoscaleZ;
  }

  /**
   * Gets the floor for scaling the z data.
   * @return double
   */
  public double getFloor() {
    return colorMap.getFloor();
  }

  /**
   * Gets the ceiling for scaling the z data.
   * @return double
   */
  public double getCeiling() {
    return colorMap.getCeil();
  }

  /**
   * Sets the show grid option.
   *
   * @param  showGrid
   */
  public void setShowGridLines(boolean showGrid) {
    grid.setVisible(showGrid);
  }

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c) {
    grid.setColor(c);
  }

  /**
   * Sets the indexes for the data component that will be plotted.
   *
   * @param indexes the sample-component
   */
  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0];
  }

  /**
   * Determines the palette type that will be used.
   * @param type
   */
  public void setPaletteType(int type) {
    colorMap.setPaletteType(type);
  }

  /**
   * Sets the colors that will be used between the floor and ceiling values.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    colorMap.setColorPalette(colors);
  }

  /**
   * Sets the floor and ceiling colors.
   *
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    colorMap.setFloorCeilColor(floorColor, ceilColor);
  }

  /**
 * Expands the z scale so as to enhance values close to zero.
 *
 * @param expanded boolean
 * @param expansionFactor double
 */
  public void setExpandedZ(boolean expanded, double expansionFactor) {
    if(expanded&&(expansionFactor>0)) {
      ZExpansion zMap = new ZExpansion(expansionFactor);
      colorMap.setZMap(zMap);
    } else {
      colorMap.setZMap(null);
    }
  }

  /**
   * Updates the buffered image using the data array.
   */
  public synchronized void update() {
    if(griddata==null) {
      return;
    }
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      double ceil = minmax[1];
      double floor = minmax[0];
      if(symmetricZ){
        ceil=Math.max(Math.abs(minmax[1]),Math.abs(minmax[0]));
        floor=-ceil;
      }
      colorMap.setScale(floor, ceil);
    }
    recolorImage();
    colorMap.updateLegend(null);
  }

  /**
   * Checks if the image is the correct size.
   */
  protected synchronized void checkImage(DrawingPanel panel) {
    int lPix, rPix, bPix, tPix;
    if(griddata.isCellData()) {
      double dx = griddata.getDx();
      double dy = griddata.getDy();
      lPix = panel.xToPix(griddata.getLeft()-dx/2);
      rPix = panel.xToPix(griddata.getRight()+dx/2);
      bPix = panel.yToPix(griddata.getBottom()+dy/2);
      tPix = panel.yToPix(griddata.getTop()-dy/2);
    } else {
      lPix = panel.xToPix(griddata.getLeft());
      rPix = panel.xToPix(griddata.getRight());
      bPix = panel.yToPix(griddata.getBottom());
      tPix = panel.yToPix(griddata.getTop());
    }
    leftPix = Math.min(lPix, rPix);
    rightPix = Math.max(lPix, rPix);
    bottomPix = Math.max(bPix, tPix);
    topPix = Math.min(bPix, tPix);
    ixsize = rightPix-leftPix+1;
    iysize = bottomPix-topPix+1;
    leftPix = Math.max(0, leftPix);
    rightPix = Math.min(rightPix, panel.getWidth());
    topPix = Math.max(0, topPix);
    bottomPix = Math.min(bottomPix, panel.getHeight());
    int row = bottomPix-topPix+1;
    int col = rightPix-leftPix+1;
    if((image!=null)&&(image.getWidth()==col)&&(image.getHeight()==row)&&(left==panel.pixToX(leftPix))&&(top==panel.pixToY(topPix))&&(bottom==panel.pixToX(bottomPix))&&(right==panel.pixToY(rightPix))) {
      return; // image exists, has the correct location, and is the correct size
    }
    left = panel.pixToX(leftPix);
    top = panel.pixToY(topPix);
    bottom = panel.pixToX(bottomPix);
    right = panel.pixToY(rightPix);
    if((image!=null)&&(image.getWidth()==col)&&(image.getHeight()==row)) {
      recolorImage();
      return; // image exists and is the correct size so recolor it
    }
    int size = row*col;
    if((size<4)||(row>4000)||(col>4000)) {
      image = null;
      return;
    }
    OSPLog.finer("InterpolatedPlot image created with row="+row+" and col="+col); //$NON-NLS-1$ //$NON-NLS-2$
    ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8}, false, // hasAlpha
      false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, col, row, col, new int[] {0, 1, 2}, new int[] {0, 0, 0});
    rgbData = new byte[3][size];
    DataBuffer databuffer = new DataBufferByte(rgbData, size);
    WritableRaster raster = Raster.createWritableRaster(csm, databuffer, new Point(0, 0));
    image = new BufferedImage(ccm, raster, false, null);
    recolorImage();
  }

  /**
   * Recolors the image pixels using the data array.
   */
  protected void recolorImage() {
    if(!visible) {
      return;
    }
    // local reference for thread safety
    GridData griddata = this.griddata;
    BufferedImage image = this.image;
    byte[][] rgbData = this.rgbData;
    if(griddata==null) {
      return;
    }
    if(griddata.isCellData()) {
      double dx = griddata.getDx();
      double dy = griddata.getDy();
      xmin = griddata.getLeft()-dx/2;
      xmax = griddata.getRight()+dx/2;
      ymin = griddata.getBottom()+dy/2;
      ymax = griddata.getTop()-dy/2;
    } else {
      xmin = griddata.getLeft();
      xmax = griddata.getRight();
      ymin = griddata.getBottom();
      ymax = griddata.getTop();
    }
    grid.setMinMax(xmin, xmax, ymin, ymax);
    if(image==null) {
      return;
    }
    if(rgbData[0].length!=image.getWidth()*image.getHeight()) {
      return;
    }
    byte[] rgb = new byte[3];
    double y = top;
    double dx = (xmax-xmin)/(ixsize-1);
    double dy = (ymin-ymax)/(iysize-1);
    if(griddata.getDx()<0) {
      dx = -dx;
    }
    if(griddata.getDy()>0) {
      dy = -dy;
    }
    int iw = image.getWidth();
    for(int i = 0, row = image.getHeight(); i<row; i++) {
      double x = left;
      for(int j = 0; j<iw; j++) {
        colorMap.doubleToComponents(griddata.interpolate(x, y, ampIndex), rgb);
        int index = i*iw+j;
        rgbData[0][index] = rgb[0]; // red
        rgbData[1][index] = rgb[1]; // green
        rgbData[2][index] = rgb[2]; // blue
        x += dx;
      }
      y += dy;
    }
  }

  /**
   * Shows how values map to colors.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
  }

  public boolean isMeasured() {
    return griddata!=null;
  }

  /**
   * Draws the image and the grid.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible||(griddata==null)) {
      return;
    }
    checkImage(panel);
    if(image!=null) {
      g.drawImage(image, leftPix, topPix, panel);
    }
    grid.draw(panel, g);
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public Object createObject(XMLControl control) {
        return new InterpolatedPlot(null);
      }
      public void saveObject(XMLControl control, Object obj) {
        super.saveObject(control, obj);
        InterpolatedPlot plot = (InterpolatedPlot) obj;
        control.setValue("color map", plot.colorMap); //$NON-NLS-1$
      }
      public Object loadObject(XMLControl control, Object obj) {
        super.loadObject(control, obj);
        InterpolatedPlot plot = (InterpolatedPlot) obj;
        plot.colorMap = (ColorMapper) control.getObject("color map"); //$NON-NLS-1$
        return plot;
      }

    };
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
