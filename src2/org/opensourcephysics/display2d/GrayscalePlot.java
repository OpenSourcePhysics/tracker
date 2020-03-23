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
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 * GrayscalePlot renders 2d data as a grayscale image.
 *
 * A grayscale plot looks similar to a grid plot with a grayscale color palette.
 * However, it uses a different rendering model.
 *
 * @author     Wolfgang Christian
 * @created    February 2, 2003
 * @version    1.0
 */
public class GrayscalePlot extends MeasuredImage implements Plot2D {
  GridData griddata;
  double floor, ceil;
  boolean autoscaleZ = true;
  boolean symmetricZ=false;
  short[] bwData;           // 16 bits grayscale
  Grid grid;
  ZExpansion zMap = null;
  private int ampIndex = 0; // amplitude index
  private JFrame legendFrame;

  /**
   * Constructs a checker field with the given width and height.
   * @param griddata
   */
  public GrayscalePlot(GridData griddata) {
    setGridData(griddata);
    update();
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
    setMinMax(xmin, xmax, ymin, ymax);
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
   * Gets the GridData object.
   * @return GridData
   */
  public GridData getGridData() {
    return griddata;
  }

  /**
   * Sets the data storage to the given value.
   *
   * @param _griddata new data storage
   */
  public void setGridData(GridData _griddata) {
    griddata = _griddata;
    if(griddata==null) {
      return;
    }
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    int size = nx*ny;
    Grid newgrid = new Grid(nx, ny, xmin, xmax, ymin, ymax);
    if(grid!=null) {
      newgrid.setColor(grid.getColor());
      newgrid.setVisible(grid.isVisible());
    } else {
      newgrid.setColor(Color.pink);
    }
    grid = newgrid;
    ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] {16}, false, // hasAlpha
      false, // alspha premultiplied
        Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
    ComponentSampleModel csm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, nx, ny, 1, nx, new int[] {0});
    bwData = new short[size];
    DataBuffer databuffer = new DataBufferUShort(bwData, size);
    WritableRaster raster = Raster.createWritableRaster(csm, databuffer, new Point(0, 0));
    image = new BufferedImage(ccm, raster, true, null);
    xmin = griddata.getLeft();
    xmax = griddata.getRight();
    ymin = griddata.getBottom();
    ymax = griddata.getTop();
  }

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
    int numColors = 256;
    GridPointData pointdata = new GridPointData(numColors, 1, 1);
    double[][][] data = pointdata.getData();
    double delta = (ceil-floor)/(numColors);
    double cval = floor-delta/2;
    for(int i = 0, n = data.length; i<n; i++) {
      double c = cval;
      if(zMap!=null) {
        c = zMap.evaluate(c);
      }
      data[i][0][2] = c;
      cval += delta;
    }
    pointdata.setScale(floor-delta, ceil+delta, 0, 1);
    GrayscalePlot cb = new GrayscalePlot(pointdata);
    cb.setShowGridLines(false);
    cb.setAutoscaleZ(false, floor, ceil);
    cb.update();
    dp.addDrawable(cb);
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
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z are span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param _floor
   * @param _ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double _floor, double _ceil) {
    autoscaleZ = isAutoscale;
    if(autoscaleZ) {
      update();
    } else {
      floor = _floor;
      ceil = _ceil;
      if(zMap!=null) {
        zMap.setMinMax(floor, ceil);
      }
    }
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
    return floor;
  }

  /**
   * Gets the ceiling for scaling the z data.
   * @return double
   */
  public double getCeiling() {
    return ceil;
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
   * Expands the z scale so as to enhance values close to zero.
   *
   * @param expanded boolean
   * @param expansionFactor double
   */
  public void setExpandedZ(boolean expanded, double expansionFactor) {
    if(expanded&&(expansionFactor>0)) {
      zMap = new ZExpansion(expansionFactor);
      zMap.setMinMax(floor, ceil);
    } else {
      zMap = null;
    }
  }

  /**
   * Updates the buffered image using the data array.
   */
  public void update() {
    if(griddata==null) {
      return;
    }
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      if(symmetricZ){
       	 ceil=Math.max(Math.abs(minmax[1]),Math.abs(minmax[0]));
       	 floor=-ceil;
      }else{
           ceil = minmax[1];
           floor = minmax[0];
      }
      if(zMap!=null) {
        zMap.setMinMax(floor, ceil);
      }
    }
    recolorImage();
    if((legendFrame!=null)&&legendFrame.isDisplayable()) {
      showLegend();
    }
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
   * Recolors the image pixels using the data array.
   */
  protected void recolorImage() {
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
    double[][][] data = griddata.getData();
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    double zscale = 2*Short.MAX_VALUE/(ceil-floor);
    if(griddata instanceof GridPointData) {
      int index = ampIndex+2;
      for(int ix = 0; ix<nx; ix++) {
        for(int iy = 0; iy<ny; iy++) {
          double val = data[ix][iy][index];
          if(zMap!=null) {
            val = zMap.evaluate(val);
          }
          val = zscale*(val-floor);
          if(val<0) {
            bwData[iy*nx+ix] = 0;
          } else if(val>2*Short.MAX_VALUE) {
            bwData[iy*nx+ix] = (short) (2*Short.MAX_VALUE);
          } else {
            bwData[iy*nx+ix] = (short) val;
          }
        }
      }
    } else if(griddata instanceof ArrayData) {
      for(int ix = 0; ix<nx; ix++) {
        for(int iy = 0; iy<ny; iy++) {
          double val = data[ampIndex][ix][iy];
          if(zMap!=null) {
            val = zMap.evaluate(val);
          }
          val = zscale*(val-floor);
          if(val<0) {
            bwData[iy*nx+ix] = 0;
          } else if(val>2*Short.MAX_VALUE) {
            bwData[iy*nx+ix] = (short) (2*Short.MAX_VALUE);
          } else {
            bwData[iy*nx+ix] = (short) val;
          }
        }
      }
    }
  }

  /**
   * Draws the image and the grid.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(griddata==null) {
      return;
    }
    super.draw(panel, g); // draws the image
    grid.draw(panel, g);
  }

  /**
   * Floor and ceiling colors are not supported.
   * Floor is black; ceiling is white.
   *
   * @param floorColor Color
   * @param ceilColor Color
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {}

  /**
   * Setting the color palette is not supported.  Palette is gray scale.
   * @param colors
   */
  public void setColorPalette(Color[] colors) {}

  /**
   * Setting the color palette is not supported.  Palette is gray scale.
   * @param type
   */
  public void setPaletteType(int type) {}

  /**
   * Sets the grid color.
   *
   * @param c
   */
  public void setGridLineColor(Color c) {
    grid.setColor(c);
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public Object createObject(XMLControl control) {
        return new GrayscalePlot(null);
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
