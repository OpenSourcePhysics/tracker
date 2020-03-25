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
 * GridPlot plots a scalar field by coloring pixels using a buffered image.
 *
 * The buffered image is scaled before it is copied to a drawing panel.
 *
 * @author     Wolfgang Christian
 * @created    February 15, 2003
 * @version    1.0
 */
public class GridPlot extends MeasuredImage implements Plot2D {
  boolean autoscaleZ = true;
  boolean symmetricZ = false;
  GridData griddata;
  int[] rgbData;
  Grid grid;
  ColorMapper colorMap;
  private int ampIndex = 0; // amplitude index

  /**
   * Constructs the GridPlot using the given griddata.
   *
   * @param griddata the point data
   */
  public GridPlot(GridData griddata) {
    setGridData(griddata);
  }

  /**
   * Constructs a GridPlot without any data.
   */
  public GridPlot() {}

  /**
   * Sets the indexes for the data component that will be plotted.
   *
   * @param indexes the sample-component
   */
  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0];
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
    if(colorMap==null) {
      colorMap = new ColorMapper(100, -1, 1, ColorMapper.SPECTRUM);
    }
    if(griddata==null) {
      return;
    }
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    rgbData = new int[nx*ny];
    image = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);
    Grid newgrid = new Grid(nx, ny);
    newgrid.setColor(Color.lightGray);
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
   * Shows how values map to colors.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
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
    if(autoscaleZ) {
      update();
    } else {
      colorMap.setScale(floor, ceil);
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
   * Outlines the data grid's boundaries.
   *
   * @param showGrid
   */
  public void setShowGridLines(boolean showGrid) {
    if(grid!=null) {
      grid.setVisible(showGrid);
    }
  }

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c) {
    if(grid!=null) {
      grid.setColor(c);
    }
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
   * Updates this object's state using new data values.
   *
   * Update should be invoked if the data in the PointData object changes or if the z scale
   * of the PointData object changes.
   *
   */
  public void update() {
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
    if(grid!=null) {
      grid.setMinMax(xmin, xmax, ymin, ymax);
    }
    double[][][] data = griddata.getData();
    int nx = griddata.getNx();
    int ny = griddata.getNy();
    if(griddata instanceof GridPointData) {
      int index = ampIndex+2;
      for(int j = 0, count = 0; j<ny; j++) {
        for(int i = 0; i<nx; i++) {
          rgbData[count] = colorMap.doubleToColor(data[i][j][index]).getRGB();
          count++;
        }
      }
      image.setRGB(0, 0, nx, ny, rgbData, 0, nx);
    } else if(griddata instanceof ArrayData) {
      for(int j = 0, count = 0; j<ny; j++) {
        for(int i = 0; i<nx; i++) {
          rgbData[count] = colorMap.doubleToColor(data[ampIndex][i][j]).getRGB();
          count++;
        }
      }
      image.setRGB(0, 0, nx, ny, rgbData, 0, nx);
    } else if(griddata instanceof FlatData) {
      int stride = data[0][0].length/(nx*ny);
      for(int j = 0, count = 0; j<ny; j++) {
        int offset = j*nx*stride;
        for(int i = 0; i<nx; i++) {
          rgbData[count] = colorMap.doubleToColor(data[0][0][offset+i*stride+ampIndex]).getRGB();
          count++;
        }
      }
      image.setRGB(0, 0, nx, ny, rgbData, 0, nx);
    }
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
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public Object createObject(XMLControl control) {
        return new GridPlot(null);
      }
      public void saveObject(XMLControl control, Object obj) {
        super.saveObject(control, obj);
        GridPlot plot = (GridPlot) obj;
        control.setValue("color map", plot.colorMap); //$NON-NLS-1$
      }
      public Object loadObject(XMLControl control, Object obj) {
        super.loadObject(control, obj);
        GridPlot plot = (GridPlot) obj;
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
 *
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
