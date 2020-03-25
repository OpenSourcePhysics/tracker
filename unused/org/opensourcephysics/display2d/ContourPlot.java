/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;

/**
 * ContourPlot draws a contour plot of a scalar field.
 *
 * Contour uses code from the Surface Plotter package by Yanto Suryono.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class ContourPlot implements Plot2D {
  private GridData griddata;
  private Color lineColor = new Color(0, 64, 0);  // dark green
  private boolean visible = true;
  private int contour_lines = 12;                 // number of contour lines
  private boolean showContourLines = true;
  private boolean showColoredLevels = true;       // fill with colors
  private double contour_stepz;                   // contour spacing
  private int[] xpoints = new int[8];
  private int[] ypoints = new int[8];
  private int[] contour_x = new int[8];
  private int[] contour_y = new int[8];
  private double[] delta = new double[4];
  private double[] intersection = new double[4];
  private double[][] contour_vertex = new double[4][3];
  private ContourAccumulator accumulator = new ContourAccumulator();
  private double zmin = 0, zmax = 1.0;            // the range for contour levels
  private boolean autoscaleZ = true;
  private boolean symmetricZ=false;
  protected ZExpansion zMap = null;
  protected ColorMapper colorMap = new ColorMapper(contour_lines, zmin, zmax, ColorMapper.SPECTRUM);
  private Color[] contourColors = new Color[contour_lines+2];
  private double[][] internalData = new double[1][1];
  private int ampIndex = 0;                       // amplitude index
  private int nx = 0, ny = 0;
  private int maxGridSize = 48;
  protected boolean interpolateLargeGrids = true; // interpolates a large grid onto a smaller grid to speed the computation of contour lines

  /**
   * Constructs a ContourPlot without any data.
   *
   */
  public ContourPlot() {}

  /**
   * Constructs a ContourPlot that renders the given GridData.
   *
   * @param  _griddata data storage
   */
  public ContourPlot(GridData _griddata) {
    griddata = _griddata;
    if(griddata==null) {
      return;
    }
    nx = (interpolateLargeGrids&&(griddata.getNx()>maxGridSize)) ? 32 : griddata.getNx();
    ny = (interpolateLargeGrids&&(griddata.getNy()>maxGridSize)) ? 32 : griddata.getNy();
    internalData = new double[nx][ny];
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
   * @param obj double[][] the new values
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
   * Gets the GridData object.
   * @return GridData
   */
  public GridData getGridData() {
    return griddata;
  }

  /**
   * Sets the data storage to the given value.
   *
   * @param _griddata
   */
  public void setGridData(GridData _griddata) {
    griddata = _griddata;
    if(griddata==null) {
      return;
    }
    nx = (interpolateLargeGrids&&(griddata.getNx()>maxGridSize)) ? 32 : griddata.getNx();
    ny = (interpolateLargeGrids&&(griddata.getNy()>maxGridSize)) ? 32 : griddata.getNy();
    internalData = new double[nx][ny];
  }

  /**
   * Sets the visibility of the contour plot.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Shows how values map to colors.
   */
  public JFrame showLegend() {
    return colorMap.showLegend(zMap);
  }

  /**
   * Shows the contour lines.
   *
   * @param showLines
   */
  public void setShowGridLines(boolean showLines) {
    showContourLines = showLines;
  }

  /**
   * Sets the contour line color.
   * The default line color is dark green.
   * @param color
   */
  public void setGridLineColor(Color color) {
    lineColor = color;
  }
  
  public void setShowColorLevels(boolean show){
	  showColoredLevels=show;  
  }

  /**
   * Paint the contour.
   * @param g
   */
  public synchronized void draw(DrawingPanel panel, Graphics g) {
    if(!visible||(griddata==null)) {
      return;
    }
    if(!autoscaleZ && showColoredLevels) {
      g.setColor(colorMap.getFloorColor());
      int w = panel.getWidth()-panel.getLeftGutter()-panel.getRightGutter();
      int h = panel.getHeight()-panel.getTopGutter()-panel.getBottomGutter();
      g.fillRect(panel.getLeftGutter(), panel.getTopGutter(), Math.max(w, 0), Math.max(h, 0));
    }
    accumulator.clearAccumulator();
    contour_stepz = (zmax-zmin)/(contour_lines+1);
    double z = zmin;
    for(int c = 0; c<contourColors.length; c++) {
      if(!autoscaleZ&&(c==contourColors.length-1)) {
        contourColors[c] = colorMap.getCeilColor();
      } else {
        contourColors[c] = colorMap.doubleToColor(z);
      }
      z += contour_stepz;
    }
    double x = griddata.getLeft(), dx = (griddata.getRight()-griddata.getLeft())/(nx-1);
    double y = griddata.getTop(), dy = -(griddata.getTop()-griddata.getBottom())/(ny-1);
    for(int i = 0, mx = internalData.length-1; i<mx; i++) {
      y = griddata.getTop();
      for(int j = 0, my = internalData[0].length-1; j<my; j++) {
        contour_vertex[0][0] = x;
        contour_vertex[0][1] = y;
        contour_vertex[0][2] = internalData[i][j];
        contour_vertex[1][0] = x;
        contour_vertex[1][1] = y+dy;
        contour_vertex[1][2] = internalData[i][j+1];
        contour_vertex[2][0] = x+dx;
        contour_vertex[2][1] = y+dy;
        contour_vertex[2][2] = internalData[i+1][j+1];
        contour_vertex[3][0] = x+dx;
        contour_vertex[3][1] = y;
        contour_vertex[3][2] = internalData[i+1][j];
        createContour(panel, g);
        y += dy;
      }
      x += dx;
    }
    if(showContourLines) {
      g.setColor(lineColor);
      accumulator.drawAll(g);
      int lpix = panel.xToPix(griddata.getLeft());
      int tpix = panel.yToPix(griddata.getTop());
      int rpix = panel.xToPix(griddata.getRight());
      int bpix = panel.yToPix(griddata.getBottom());
      g.drawRect(Math.min(lpix, rpix), Math.min(tpix, bpix), Math.abs(lpix-rpix), Math.abs(tpix-bpix));
    }
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values.
   *
   * If autoscaling is true, then the min and max values of z are set using the data.
   * If autoscaling is false, then floor and ceiling values become the max and min.
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
      zmax = ceil;
      zmin = floor;
      if(zMap!=null) {
        zMap.setMinMax(zmin, zmax);
      }
      colorMap.setScale(zmin, zmax);
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
   * Sets flag to interpolates a large grid onto a smaller grid to speed the computation of contour lines.
   *
   * @param interpolate boolean
   */
  public void setInterpolateLargeGrids(boolean interpolate) {
    interpolateLargeGrids = interpolate;
  }

  /**
   * Retruns true if plot interpolates a large grid onto a smaller grid to speed the computation of contour lines.
   * @return boolean
   */
  public boolean isInterpolateLargeGrids() {
    return interpolateLargeGrids;
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
      zMap.setMinMax(zmin, zmax);
    } else {
      zMap = null;
    }
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
   * Updates the contour data.
   */
  public void update() {
    if(griddata==null) {
      return;
    }
    if((interpolateLargeGrids&&(nx!=griddata.getNx()))||(ny!=griddata.getNy())) {
      updateInterpolated(griddata);
    } else {
      updateDirect(griddata);
    }
    colorMap.updateLegend(zMap);
  }

  /**
   * Updates the internal data by interpolating large grids onto a smaller array.
   */
  void updateInterpolated(GridData griddata) {
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      if(symmetricZ){
     	 zmax=Math.max(Math.abs(minmax[1]),Math.abs(minmax[0]));
     	 zmin=-zmax;
      }else{
         zmax = minmax[1];
         zmin = minmax[0];
      }
      if(zMap!=null) {
        zMap.setMinMax(zmin, zmax);
      }
      colorMap.setScale(zmin, zmax);
    }
    double x = griddata.getLeft(), dx = (griddata.getRight()-griddata.getLeft())/(nx-1);
    double y = griddata.getTop(), dy = -(griddata.getTop()-griddata.getBottom())/(ny-1);
    for(int i = 0; i<nx; i++) {
      y = griddata.getTop();
      for(int j = 0; j<ny; j++) {
        internalData[i][j] = griddata.interpolate(x, y, ampIndex);
        if(zMap!=null) {
          internalData[i][j] = zMap.evaluate(internalData[i][j]);
        }
        y += dy;
      }
      x += dx;
    }
  }

  /**
   * Updates the contour data my directly copying values.
   */
  void updateDirect(GridData griddata) {
    if(griddata==null) {
      return;
    }
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      if(symmetricZ){
      	 zmax=Math.max(Math.abs(minmax[1]),Math.abs(minmax[0]));
      	 zmin=-zmax;
      }else{
          zmax = minmax[1];
          zmin = minmax[0];
      }
      if(zMap!=null) {
        zMap.setMinMax(zmin, zmax);
      }
      colorMap.setScale(zmin, zmax);
    }
    if(griddata instanceof ArrayData) {
      double[][] arrayData = griddata.getData()[ampIndex];
      for(int i = 0; i<nx; i++) { // copy the rows
        System.arraycopy(arrayData[i], 0, internalData[i], 0, ny);
        if(zMap!=null) {
          for(int j = 0; j<ny; j++) {
            internalData[i][j] = zMap.evaluate(internalData[i][j]);
          }
        }
      }
    } else if(griddata instanceof GridPointData) {
      double[][][] ptdata = griddata.getData();
      for(int i = 0, nx = ptdata.length; i<nx; i++) {
        for(int j = 0, ny = ptdata[0].length; j<ny; j++) {
          internalData[i][j] = ptdata[i][j][2+ampIndex];
          if(zMap!=null) {
            internalData[i][j] = zMap.evaluate(internalData[i][j]);
          }
        }
      }
    }
  }

  /**
   * Creates contour plot of a single area division. Called by
   * <code>draw</code> method
   *
   * @see #draw
   */
  private final void createContour(DrawingPanel panel, Graphics g) {
    double z = zmin;
    xpoints[0] = panel.xToPix(contour_vertex[0][0])+1;
    xpoints[2] = panel.xToPix(contour_vertex[1][0])+1;
    xpoints[4] = panel.xToPix(contour_vertex[2][0])+1;
    xpoints[6] = panel.xToPix(contour_vertex[3][0])+1;
    xpoints[1] = xpoints[3] = xpoints[5] = xpoints[7] = -1;
    ypoints[0] = panel.yToPix(contour_vertex[0][1])+1;
    ypoints[4] = panel.yToPix(contour_vertex[2][1])+1;
    ypoints[2] = ypoints[3] = panel.yToPix(contour_vertex[1][1])+1;
    ypoints[6] = ypoints[7] = panel.yToPix(contour_vertex[3][1])+1;
    int xmin = xpoints[0];
    int xmax = xpoints[4];
    for(int counter = 0; counter<=contour_lines+1; counter++) {
      // Analyzes edges
      for(int edge = 0; edge<4; edge++) {
        int index = (edge<<1)+1;
        int nextedge = (edge+1)&3;
        if(z>contour_vertex[edge][2]) {
          xpoints[index-1] = -2;
          if(z>contour_vertex[nextedge][2]) {
            xpoints[(index+1)&7] = -2;
            xpoints[index] = -2;
          }
        } else if(z>contour_vertex[nextedge][2]) {
          xpoints[(index+1)&7] = -2;
        }
        if(xpoints[index]!=-2) {
          if(xpoints[index]!=-1) {
            intersection[edge] += delta[edge];
            if((index==1)||(index==5)) {
              ypoints[index] = panel.yToPix(intersection[edge])+1;
            } else {
              xpoints[index] = panel.xToPix(intersection[edge])+1;
            }
          } else {
            if((z>contour_vertex[edge][2])||(z>contour_vertex[nextedge][2])) {
              switch(index) {
                 case 1 :
                   delta[edge] = (contour_vertex[nextedge][1]-contour_vertex[edge][1])*contour_stepz/(contour_vertex[nextedge][2]-contour_vertex[edge][2]);
                   intersection[edge] = (contour_vertex[nextedge][1]*(z-contour_vertex[edge][2])+contour_vertex[edge][1]*(contour_vertex[nextedge][2]-z))/(contour_vertex[nextedge][2]-contour_vertex[edge][2]);
                   xpoints[index] = xmin;
                   ypoints[index] = panel.yToPix(intersection[edge])+1;
                   break;
                 case 3 :
                   delta[edge] = (contour_vertex[nextedge][0]-contour_vertex[edge][0])*contour_stepz/(contour_vertex[nextedge][2]-contour_vertex[edge][2]);
                   intersection[edge] = (contour_vertex[nextedge][0]*(z-contour_vertex[edge][2])+contour_vertex[edge][0]*(contour_vertex[nextedge][2]-z))/(contour_vertex[nextedge][2]-contour_vertex[edge][2]);
                   xpoints[index] = panel.xToPix(intersection[edge])+1;
                   break;
                 case 5 :
                   delta[edge] = (contour_vertex[edge][1]-contour_vertex[nextedge][1])*contour_stepz/(contour_vertex[edge][2]-contour_vertex[nextedge][2]);
                   intersection[edge] = (contour_vertex[edge][1]*(z-contour_vertex[nextedge][2])+contour_vertex[nextedge][1]*(contour_vertex[edge][2]-z))/(contour_vertex[edge][2]-contour_vertex[nextedge][2]);
                   xpoints[index] = xmax;
                   ypoints[index] = panel.yToPix(intersection[edge])+1;
                   break;
                 case 7 :
                   delta[edge] = (contour_vertex[edge][0]-contour_vertex[nextedge][0])*contour_stepz/(contour_vertex[edge][2]-contour_vertex[nextedge][2]);
                   intersection[edge] = (contour_vertex[edge][0]*(z-contour_vertex[nextedge][2])+contour_vertex[nextedge][0]*(contour_vertex[edge][2]-z))/(contour_vertex[edge][2]-contour_vertex[nextedge][2]);
                   xpoints[index] = panel.xToPix(intersection[edge])+1;
                   break;
              }
            }
          }
        }
      }
      // Creates polygon
      int contour_n = 0;
      for(int index = 0; index<8; index++) {
        if(xpoints[index]>=0) {
          contour_x[contour_n] = xpoints[index];
          contour_y[contour_n] = ypoints[index];
          contour_n++;
        }
      }
      if(showColoredLevels&&(colorMap.getPaletteType()!=ColorMapper.WIREFRAME)) {
        g.setColor(contourColors[counter]);
        if(contour_n>0) {
          g.fillPolygon(contour_x, contour_y, contour_n);
        }
      }
      // Creates contour lines
      if(showContourLines) {
        int x = -1;
        int y = -1;
        for(int index = 1; index<8; index += 2) {
          if(xpoints[index]>=0) {
            if(x!=-1) {
              accumulator.addLine(x, y, xpoints[index], ypoints[index]);
            }
            x = xpoints[index];
            y = ypoints[index];
          }
        }
        if((xpoints[1]>0)&&(x!=-1)) {
          accumulator.addLine(x, y, xpoints[1], ypoints[1]);
        }
      }
      if(contour_n<3) {
        break;
      }
      z += contour_stepz;
    }
  }

  /**
   * Determines the palette type that will be used.
   *
   * @param colors Color[]
   */
  public void setColorPalette(Color[] colors) {
    colorMap.setColorPalette(colors);
  }

  /**
   * Sets the type of palette.
   *
   * Palette types are defined in the ColorMapper class and include:  SPECTRUM, GRAYSCALE, and DUALSHADE.
   *
   * @param mode
   */
  public void setPaletteType(int mode) {
    colorMap.setPaletteType(mode);
  }

  /**
   * Sets the floor, ceiling, and line colors.
   *
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    colorMap.setFloorCeilColor(floorColor, ceilColor);
  }

  /**
   * Sets the indexes for the data components that will be plotted.
   *
   * @param indexes the sample-component indexes
   */
  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0];
  }

  /**
   *   Sets the number of contour levels.
   *
   * @param n number of levels.
   */
  public void setNumberOfLevels(int n) { // Create colors array
    contour_lines = n;
    colorMap.setNumberOfColors(n); // Paco changed next line by this one
    contourColors = new Color[contour_lines+2];
  }

  /* The following methods are requried for the measurable interface */
  public double getXMin() {
    return griddata.getLeft();
  }

  public double getXMax() {
    return griddata.getRight();
  }

  public double getYMin() {
    return griddata.getBottom();
  }

  public double getYMax() {
    return griddata.getTop();
  }

  public boolean isMeasured() {
    return griddata!=null;
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public void saveObject(XMLControl control, Object obj) {
        super.saveObject(control, obj);
        ContourPlot plot = (ContourPlot) obj;
        control.setValue("line color", plot.lineColor); //$NON-NLS-1$
        control.setValue("color map", plot.colorMap);   //$NON-NLS-1$
      }
      public Object createObject(XMLControl control) {
        return new ContourPlot(null);
      }
      public Object loadObject(XMLControl control, Object obj) {
        super.loadObject(control, obj);
        ContourPlot plot = (ContourPlot) obj;
        plot.lineColor = (Color) control.getObject("line color");     //$NON-NLS-1$
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
