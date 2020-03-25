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
import javax.swing.JFrame;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;
import org.opensourcephysics.display.MeasuredImage;

/**
 * ComplexGridPlot plots a complex scalar field by coloring pixels a buffered image.
 *
 * The buffered image is scaled before it is copied to a drawing panel.
 *
 * @author     Wolfgang Christian
 * @created    February 15, 2003
 * @version    1.0
 */
public class ComplexGridPlot extends MeasuredImage implements Plot2D {
  boolean autoscaleZ = true;
  GridData griddata;
  int[] rgbData;
  Grid grid;
  ComplexColorMapper colorMap;
  private int ampIndex = 0; // amplitude index
  private int reIndex = 1;  // real index
  private int imIndex = 2;  // imaginary index

  /**
   * Constructs the ComplexGridPlot without data.
   */
  public ComplexGridPlot() {
    this(null);
  }

  /**
   * Constructs the ComplexGridPlot using the given 2d datset.
   * @param _griddata
   */
  public ComplexGridPlot(GridData _griddata) {
    griddata = _griddata;
    colorMap = new ComplexColorMapper(1);
    if(griddata==null) {
      return;
    }
    setGridData(griddata);
  }

  /**
   * Gets the GridData object.
   * @return GridData
   */
  public GridData getGridData() {
    return griddata;
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
   * Sets the data to new values.
   *
   * The grid is resized to fit the new data if needed.
   *
   * @param obj
   */
  public void setAll(Object obj) {
    double[][][] val = (double[][][]) obj;
    copyComplexData(val);
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
    double[][][] val = (double[][][]) obj;
    copyComplexData(val);
    if(griddata.isCellData()) {
      griddata.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      griddata.setScale(xmin, xmax, ymin, ymax);
    }
    update();
  }

  private void copyComplexData(double vals[][][]) {
    if((griddata!=null)&&!(griddata instanceof ArrayData)) {
      throw new IllegalStateException("SetAll only supports ArrayData for data storage."); //$NON-NLS-1$
    }
    if((griddata==null)||(griddata.getNx()!=vals[0].length)||(griddata.getNy()!=vals[0][0].length)) {
      griddata = new ArrayData(vals[0].length, vals[0][0].length, 3);
      setGridData(griddata);
    }
    double[][] mag = griddata.getData()[0];
    double[][] reData = griddata.getData()[1];
    double[][] imData = griddata.getData()[2];
    // current grid has correct size
    int ny = vals[0][0].length;
    for(int i = 0, nx = vals[0].length; i<nx; i++) {
      System.arraycopy(vals[0][i], 0, reData[i], 0, ny);
      System.arraycopy(vals[1][i], 0, imData[i], 0, ny);
      for(int j = 0; j<ny; j++) {
        mag[i][j] = Math.sqrt(vals[0][i][j]*vals[0][i][j]+vals[1][i][j]*vals[1][i][j]);
      }
    }
  }

  public void setGridData(GridData _griddata) {
    griddata = _griddata;
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    rgbData = new int[nx*ny];
    image = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);
    Grid newgrid = new Grid(nx, ny);
    if(grid!=null) {
      newgrid.setColor(grid.getColor());
      newgrid.setVisible(grid.isVisible());
    } else {
      newgrid.setColor(Color.lightGray);
    }
    grid = newgrid;
    update();
  }

  /**
   * Shows a legend of phase angle and color.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z will span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor not supported
   * @param ceil  ceiling value
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    autoscaleZ = isAutoscale;
    if(autoscaleZ) {
      update();
    } else {
      colorMap.setScale(ceil);
    }
  }
  
  /**
   * Forces the z-scale to be symmetric about zero.
   * Not applicable in complex map because amplitude is always positive
   *
   * @param symmetric
   */
  public void setSymmetricZ(boolean symmetric){

  }
  
  /**
   * Gets the symmetric z flag.  
   */
  public boolean isSymmetricZ(){
	  return false;
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
    return 0;
  }

  /**
   * Gets the ceiling for scaling the z data.
   * @return double
   */
  public double getCeiling() {
    return colorMap.getCeil();
  }

  /**
   * Sets the floor and ceiling colors.
   *
   * @param floorColor  not supported
   * @param ceilColor   ceiling color
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    colorMap.setCeilColor(ceilColor);
  }

  /**
   * Shows the grid lines if set to true.
   * @param  showGrid
   */
  public void setShowGridLines(boolean showGrid) {
    if(grid==null) {
      grid = new Grid(1, 1);
    }
    grid.setVisible(showGrid);
  }

  /**
   * Updates in response to changes in the data.
   */
  public void update() {
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      colorMap.setScale(minmax[1]);
    }
    recolorImage();
  }

  /**
   * Expands the magnitude scale so as to enhance values close to zero.
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
    double[] samples = new double[3];
    if(griddata instanceof GridPointData) {
      int ampIndex = this.ampIndex+2;
      int reIndex = this.reIndex+2;
      int imIndex = this.imIndex+2;
      for(int iy = 0, count = 0; iy<ny; iy++) {
        for(int ix = 0; ix<nx; ix++) {
          samples[0] = data[ix][iy][ampIndex];
          samples[1] = data[ix][iy][reIndex];
          samples[2] = data[ix][iy][imIndex];
          rgbData[count] = colorMap.samplesToColor(samples).getRGB();
          count++;
        }
      }
    } else if(griddata instanceof ArrayData) {
      for(int iy = 0, count = 0; iy<ny; iy++) {
        for(int ix = 0; ix<nx; ix++) {
          samples[0] = data[ampIndex][ix][iy];
          samples[1] = data[reIndex][ix][iy];
          samples[2] = data[imIndex][ix][iy];
          rgbData[count] = colorMap.samplesToColor(samples).getRGB();
          count++;
        }
      }
    }
    image.setRGB(0, 0, nx, ny, rgbData, 0, nx);
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
    super.draw(panel, g); // draws the image
    grid.draw(panel, g);
  }

  /**
   * Setting the color palette is not supported.  The complex palette always maps phase to color.
   * @param colors
   */
  public void setColorPalette(Color[] colors) {}

  /**
   * Setting the palette is not supported.   The complex palette always maps phase to color.
   * @param type
   */
  public void setPaletteType(int type) {
    // only phase phase palette is available
  }

  public void setGridLineColor(Color c) {
    if(grid==null) {
      grid = new Grid(1, 1);
    }
    grid.setColor(c);
  }

  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0]; // amplitude index
    reIndex = indexes[1];  // real index
    imIndex = indexes[2];  // imaginary index
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public Object createObject(XMLControl control) {
        return new ComplexGridPlot(null);
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
