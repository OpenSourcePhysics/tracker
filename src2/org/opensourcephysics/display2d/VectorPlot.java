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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Grid;

/**
 * VectorPlot renders a vector field in a drawing panel using arrows centered on
 * each grid point in the GridPointData.
 *
 * The default representation of the vector field uses fixed length arrows to
 * show direction and color to show magnitude.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class VectorPlot implements Plot2D {
  public static final int STROKEDARROW = 0;
  public static final int FILLEDARROW = 1;
  private GeneralPath vectorpath;
  private int arrowType = STROKEDARROW; // draw the arrow with a solid arrowhead
  private boolean visible = true;
  private GridData griddata;
  private boolean autoscaleZ = true;
  private boolean scaleArrowToGrid = true;
  private VectorColorMapper colorMap;
  private int ampIndex = 0;             // amplitude index
  private int aIndex = 1;               // x componnet index
  private int bIndex = 2;               // y component index
  private double xmin, xmax, ymin, ymax;
  Grid grid;

  /**
   * Constructs a VectorPlot without data.
   */
  public VectorPlot() {
    this(null);
  }

  /**
   * Constructs a VectorPlot that renders the given grid data.
   *
   * @param  _griddata the data
   */
  public VectorPlot(GridData _griddata) {
    griddata = _griddata;
    colorMap = new VectorColorMapper(256, 1.0);
    if(griddata==null) {
      return;
    }
    grid = (griddata.isCellData()) ? new Grid(griddata.getNx(), griddata.getNy()) : new Grid(griddata.getNx()-1, griddata.getNy()-1);
    grid.setColor(Color.lightGray);
    grid.setVisible(false);
    update();
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
    copyVecData(val);
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
    copyVecData(val);
    if(griddata.isCellData()) {
      griddata.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      griddata.setScale(xmin, xmax, ymin, ymax);
    }
    update();
  }

  private void copyVecData(double vals[][][]) {
    if((griddata!=null)&&!(griddata instanceof ArrayData)) {
      throw new IllegalStateException("SetAll only supports ArrayData for data storage."); //$NON-NLS-1$
    }
    if((griddata==null)||(griddata.getNx()!=vals[0].length)||(griddata.getNy()!=vals[0][0].length)) {
      griddata = new ArrayData(vals[0].length, vals[0][0].length, 3);
      setGridData(griddata);
    }
    double[][] colorValue = griddata.getData()[0];
    double[][] xComp = griddata.getData()[1];
    double[][] yComp = griddata.getData()[2];
    int ny = vals[0][0].length;
    for(int i = 0, nx = vals[0].length; i<nx; i++) {
      for(int j = 0; j<ny; j++) {
        // map vector magniture to color
        colorValue[i][j] = Math.sqrt(vals[0][i][j]*vals[0][i][j]+vals[1][i][j]*vals[1][i][j]);
        // normalize vector lengths
        xComp[i][j] = (colorValue[i][j]==0) ? 0 : vals[0][i][j]/colorValue[i][j];
        yComp[i][j] = (colorValue[i][j]==0) ? 0 : vals[1][i][j]/colorValue[i][j];
      }
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
   * @param _griddata the new data storage
   */
  public void setGridData(GridData _griddata) {
    griddata = _griddata;
    if(griddata==null) {
      return;
    }
    Grid newgrid = (griddata.isCellData()) ? new Grid(griddata.getNx(), griddata.getNy()) : new Grid(griddata.getNx()-1, griddata.getNy()-1);
    newgrid.setColor(Color.lightGray);
    newgrid.setVisible(false);
    if(grid!=null) {
      newgrid.setColor(grid.getColor());
      newgrid.setVisible(grid.isVisible());
    }
    grid = newgrid;
  }

  /**
   * Sets the indexes for the data components that will be plotted.
   *
   * Indexes determine the postion of the amplitude, x-component, and y-component
   * in the data array.
   *
   * @param indexes the sample-component indexes
   */
  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0];
    aIndex = indexes[1];
    bIndex = indexes[2];
  }

  /**
   * Sets this vector field to draw vectors with filled shafts and arrowheads.
   *
   * @param type
   */
  public void setArrowType(int type) {
    arrowType = type;
  }

  /**
   * Sets the type of palette.
   *
   * Palette types are defined in the ColorMapper class and include:  SPECTRUM, BLACK, RED, and BLUE.
   * The default type is SPECTRUM.
   *
   * @param mode
   */
  public void setPaletteType(int mode) {
    colorMap.setPaletteType(mode);
  }

  /**
   * Sets the colors that will be used between the floor and ceiling values.
   * Not implemented in this class.
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    // not implemented
  }

  /**
   * Sets this vector field to be visible.
   * Drawing will be disabled if visible is false.
   *
   * @param vis
   */
  public void setVisible(boolean vis) {
    visible = vis;
  }

  /**
   * Outlines the data grid's boundaries.
   *
   * @param showGrid
   */
  public void setShowGridLines(boolean showGrid) {
    if(grid==null) {
      grid = new Grid(0);
    }
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
   * Draws this vector field in the given drawing panel.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible||(griddata==null)) {
      return;
    }
    if(grid.isVisible()) {
      // grid.setMinMax(xmin, xmax, ymin, ymax);
      grid.draw(panel, g);
    }
    colorMap.checkPallet(panel.getBackground());
    GridData griddata = this.griddata;
    double[][][] data = griddata.getData();
    double dx = griddata.getDx();
    double dy = griddata.getDy();
    double left = griddata.getLeft();
    double top = griddata.getTop();
    double aspectRatio = panel.getAspectRatio();
    float arrowLength = (float) Math.abs(panel.getYPixPerUnit()); // arrows will use panel scale
    if(scaleArrowToGrid) { // arrows will adjust size to fit grid
      arrowLength = Math.max(1, panel.getSize().width/(float) data.length/(float) aspectRatio-1);
      arrowLength = Math.min(18, arrowLength*0.72f);
    }
    switch(arrowType) {
       case STROKEDARROW :
         vectorpath = createVectorPath(arrowLength);
         break;
       case FILLEDARROW :
         vectorpath = createFilledVectorPath(arrowLength);
         break;
       default :
         vectorpath = createVectorPath(arrowLength);
    }
    int sgnx = (panel.getXPixPerUnit()<0) ? sgnx = -1 : 1;
    int sgny = (panel.getYPixPerUnit()<0) ? sgny = -1 : 1;
    double amp = 0, a = 0, b = 0, x = 0, y = 0;
    Color background = panel.getBackground();
    for(int i = 0, nx = griddata.getNx(); i<nx; i++) {
      for(int j = 0, ny = griddata.getNy(); j<ny; j++) {
        if(griddata instanceof GridPointData) {
          x = data[i][j][0];
          y = data[i][j][1];
          amp = data[i][j][ampIndex+2];
          a = data[i][j][aIndex+2];
          b = data[i][j][bIndex+2];
        } else if(griddata instanceof ArrayData) {
          x = left+i*dx;
          y = top+j*dy;
          amp = data[ampIndex][i][j];
          a = data[aIndex][i][j];
          b = data[bIndex][i][j];
        }
        // start in-line code for speed
        Graphics2D g2 = (Graphics2D) g;
        Color c = colorMap.doubleToColor(amp);
        if(background==c) {
          continue;
        }
        g2.setColor(colorMap.doubleToColor(amp));
        AffineTransform at = new AffineTransform(sgnx*aspectRatio*a, // cos
          -sgny*b,                                                   // -sin
            sgnx*aspectRatio*b,                                      // sin
              sgny*a,                                                // cos
                panel.xToPix(x),                                     // translation x
                  panel.yToPix(y)                                    // translation y
                    );
        Shape s = vectorpath.createTransformedShape(at);
        switch(arrowType) {
           case STROKEDARROW :
             g2.draw(s);
             break;
           case FILLEDARROW :
             g2.fill(s);
             break;
           default :
             g2.draw(s);
        }
        // end in-line code
      }
    }
  }

  /**
   * Sets the autoscale flag for the arrow length.
   * @param scaleToGrid
   */
  public void scaleArrowLenghToGrid(boolean scaleToGrid) {
    scaleArrowToGrid = scaleToGrid;
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
      colorMap.setScale(ceil);
    }
  }
  
  /**
   * Forces the z-scale to be symmetric about zero.
   * Not applicable in vector map because vector amplitude is always positive
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
    return colorMap.getCeiling();
  }

  /**
   * Sets the floor and ceiling colors.
   * Not implemented in this class.
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    // not implemented
  }

  /**
   * Shows how values map to colors.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
  }

  /**
   * Updates the vector field using the data array.
   */
  public void update() {
    if(griddata==null) {
      return;
    }
    if(autoscaleZ) {
      double[] minmax = griddata.getZRange(ampIndex);
      colorMap.setScale(minmax[1]);
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
  }

  /**
   * Expands the z scale so as to enhance values close to zero.
   *
   * @param expanded boolean
   * @param expansionFactor double
   */
  public void setExpandedZ(boolean expanded, double expansionFactor) {
    // does nothing for now.
  }

  //  /**
  //   * Draws the arrow.
  //   *
  //   * @param g  the graphics context upon which to draw
  //   * @param vertex the location of the arrow and its components
  //   */
  //  private void drawLine(Graphics2D g2, double[] vertex, DrawingPanel panel) {
  //    g2.setColor(colorMap.doubleToColor(vertex[2]));
  //    int pixx = panel.xToPix(vertex[0]);
  //    int pixy = panel.yToPix(vertex[1]);
  //    int pixa = panel.xToPix(vertex[0]+vertex[3]);
  //    int pixb = panel.yToPix(vertex[1]+vertex[4]);
  //    g2.drawLine(pixx, pixy, pixa, pixb);
  //  }
  //
  static GeneralPath createVectorPath(float size) {
    float head = Math.min(15, 1+size/5);
    GeneralPath path = new GeneralPath();
    path.moveTo(-size/2, 0);                       // start drawing at the base
    path.lineTo(size/2, 0);                        // line to the tip of the head
    path.lineTo(size/2-head, (float) 2.0*head/3);  // draw one side
    path.lineTo(size/2, 0);                        // back to the tip
    path.lineTo(size/2-head, (float) -2.0*head/3); // draw the other side
    return path;
  }

  static GeneralPath createFilledVectorPath(float size) {
    float head = Math.min(15, 1+size/5);
    GeneralPath path = new GeneralPath();
    path.moveTo(-size/2, 1);             // start drawing at the base
    path.lineTo(size/2-head, 1);         // line to base tip of the head
    path.lineTo(size/2-head, 2*head/3);  // draw to one side
    path.lineTo(size/2, 0);              // up to the tip
    path.lineTo(size/2-head, -2*head/3); // the other side
    path.lineTo(size/2-head, -1);        // back to base tip of the head
    path.moveTo(-size/2, -1);            // back to the base
    return path;
  }

  /* The following methods are requried for the measurable interface */
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
      public Object createObject(XMLControl control) {
        return new VectorPlot(null);
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
