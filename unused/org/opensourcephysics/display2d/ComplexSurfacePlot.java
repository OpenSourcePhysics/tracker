/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.False3D;

/**
 * SurfacePlot draws a 3D surface of a scalar field.
 * Surfaceplot uses code from the Surface Plotter package by Yanto Suryono.
 *
 * @author       Wolfgang Christian and Yanto Suryono
 * @version 1.0
 */
public class ComplexSurfacePlot implements Plot2D, False3D {
  boolean visible = true;
  ComplexColorMapper colorMap = new ComplexColorMapper(1);         // color map with ceiling=1.
  protected DecimalFormat labelFormat = new DecimalFormat("0.00"); //$NON-NLS-1$
  private static final int TOP = 0;
  private static final int CENTER = 1;
  //  // for splitting polygons
  //  private static final int UPPER = 1;
  //  private static final int COINCIDE = 0;
  //  private static final int LOWER = -1;

  /** Field INIT_CALC_DIV */
  public static final int INIT_CALC_DIV = 33;

  /** Field INIT_DISP_DIV */
  public static final int INIT_DISP_DIV = INIT_CALC_DIV;
  private int calc_divisions = INIT_CALC_DIV;                      // number of divisions to calculate
  private int disp_divisions = INIT_DISP_DIV;                      // number of divisions to calculate
  private int plot_mode = ColorMapper.SPECTRUM;
  private boolean isBoxed, isMesh, isScaleBox, isDisplayXY, isDisplayZ, isDisplayGrids;
  private double zmin = -2, zmax = 2;
  private boolean autoscaleZ = true;
  private boolean symmetricZ = false;
  private GridData griddata;
  //private double color_factor;
  private Point projection;
  private ComplexSurfaceVertex cop;                                // center of projection
  private ComplexSurfaceVertex[] vertexArray;                      // vertices array
  private final ComplexSurfaceVertex values1[] = new ComplexSurfaceVertex[4];
  // private final ComplexSurfaceVertex values2[] = new ComplexSurfaceVertex[4];
  // private double              color;                               // color of surface

  private Color line_color = Color.black;
  private int factor_x, factor_y;                                  // conversion factors
  private int t_x, t_y, t_z;                                       // determines ticks density
  //private boolean mouseDown = false;
  private int click_x, click_y;                                    // previous mouse cursor position
  private boolean invalidProjection = true;
  private int iwidth = 0, iheight = 0;                             // the width and height of the last drawing operation
  private double xmin, xmax, ymin, ymax;
  private int ampIndex = 0;                                        // amplitude index
  private int reIndex = 1;                                         // real index
  private int imIndex = 2;                                         // imaginary index
  // the following are needed by the SurfaceVertex

  SurfacePlotProjector projector;                                  // the projector
  double zminV, zmaxV, zfactorV;
  int master_project_indexV = 0;                                   // over 4 billion times to reset
  ZExpansion zMap;

  /**
   * Constructs the ComplexSurfacePlot without data.
   */
  public ComplexSurfacePlot() {
    this(null);
  }

  /**
   * ComplexSurfacePlot constructor with the given data model.
   *
   * @param _griddata GridData
   */
  public ComplexSurfacePlot(GridData _griddata) {
    griddata = _griddata;
    defaultVariables();
    autoscaleZ = true;
    projector = new SurfacePlotProjector();
    // projector.setDistance(70);
    projector.setDistance(200);
    // projector.set2DScaling(15);
    projector.set2DScaling(8);
    projector.setRotationAngle(125);
    projector.setElevationAngle(10);
    setGridData(_griddata);
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
  public void setGridData(GridData _griddata) throws IllegalArgumentException {
    griddata = _griddata;
    if(griddata==null) {
      return;
    }
    if(griddata.getNx()!=griddata.getNy()) {
      throw new IllegalArgumentException("SurfacePlot requires square grids."); //$NON-NLS-1$
    }
  }

  private void generateVerticesFromArray(ArrayData griddata) {
    double[][] ampdata = griddata.getData()[ampIndex];
    double[][] redata = griddata.getData()[reIndex];
    double[][] imdata = griddata.getData()[imIndex];
    int numRows = ampdata.length;
    int numCols = ampdata[0].length;
    calc_divisions = numRows-1;
    double xfactor = 20/(xmax-xmin);
    double yfactor = 20/(ymax-ymin);
    if((vertexArray==null)||(vertexArray.length!=numRows*numCols)) {
      vertexArray = new ComplexSurfaceVertex[numRows*numCols];
    }
    double dx = Math.abs(griddata.getDx());
    double dy = Math.abs(griddata.getDy());
    double x = xmin; // left;
    for(int ix = 0; ix<numCols; ix++) {
      double y = ymin; // bottom;
      for(int iy = 0; iy<numRows; iy++) {
        int iyd = (griddata.getDy()>0) ? iy : numCols-iy-1;
        int ixd = (griddata.getDx()>0) ? ix : numCols-ix-1;
        double zval = ampdata[ixd][iyd];
        if(zMap!=null) {
          zval = zMap.evaluate(zval);
        }
        vertexArray[ix*numRows+iy] = new ComplexSurfaceVertex(-10+(x-xmin)*xfactor, -10+(y-ymin)*yfactor, zval, redata[ixd][iyd], imdata[ixd][iyd], this);
        y += dy;
      }
      x += dx;
    }
    ampdata = null;
  }

  private void generateVerticesFromPoints(GridPointData griddata) throws IllegalArgumentException {
    double[][][] data = griddata.getData();
    int numRows = data.length;
    int numCols = data[0].length;
    calc_divisions = numRows-1;
    double xfactor = 20/(xmax-xmin);
    double yfactor = 20/(ymax-ymin);
    if((vertexArray==null)||(vertexArray.length!=numRows*numCols)) {
      vertexArray = new ComplexSurfaceVertex[numRows*numCols];
    }
    double dx = Math.abs(griddata.getDx());
    double dy = Math.abs(griddata.getDy());
    int ampIndex = this.ampIndex+2;
    int reIndex = this.reIndex+2;
    int imIndex = this.imIndex+2;
    double x = xmin; // left;
    for(int ix = 0; ix<numCols; ix++) {
      double y = ymin; // bottom;
      for(int iy = 0; iy<numRows; iy++) {
        double zval = data[ix][iy][ampIndex];
        if(zMap!=null) {
          zval = zMap.evaluate(zval);
        }
        vertexArray[ix*numRows+iy] = new ComplexSurfaceVertex(-10+(x-xmin)*xfactor, -10+(y-ymin)*yfactor, zval, data[ix][iy][reIndex], data[ix][iy][imIndex], this);
        y += dy;
      }
      x += dx;
    }
    data = null;
  }

  /**
   * Sets the indexes for the data components that will be plotted.
   *
   * Indexes determine the postion of the amplitude, real-component, and imaginary-component
   * in the data array.
   *
   * @param indexes the sample-component indexes
   */
  public void setIndexes(int[] indexes) {
    ampIndex = indexes[0];
    reIndex = indexes[1];
    imIndex = indexes[2];
  }

  /**
   * Method projectVertexArray
   *
   */
  void projectVertexArray() {
    ComplexSurfaceVertex[] tempArray = vertexArray; // reference to the array so it cannot change.
    if(tempArray==null) {
      return;
    }
    for(int i = 0, num = tempArray.length; i<num; i++) {
      tempArray[i].project();
    }
  }

  /**
   * Sets the colors that will be used between the floor and ceiling values.
   * Not implemented.   Color always maps to phase.
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    // Not implemented.   Color always maps to phase.
  }

  /**
   * Sets the visibility of the lattice.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Shows a legend of phase angle and color.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
  }

  /**
   * Outlines the data grid's boundaries.
   *
   * @param show
   */
  public void setShowGridLines(boolean show) {
    isMesh = show;
  }

  /**
   *  Sets the color for grid line boundaries
   *
   * @param  c
   */
  public void setGridLineColor(Color c) {
    line_color = c;
  }

  /**
   * Paint the surface.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible||(griddata==null)) {
      return;
    }
    projector.setProjectionArea(new Rectangle(0, 0, panel.getBounds().width, panel.getBounds().height));
    if(invalidProjection||(iwidth!=panel.getWidth())||(iheight!=panel.getHeight())) {
      master_project_indexV++;
      invalidProjection = false;
      projectVertexArray();
      iwidth = panel.getWidth();
      iheight = panel.getHeight();
    }
    plotSurface(g);
  }

  private void defaultVariables() {
    plot_mode = ColorMapper.SPECTRUM;
    isBoxed = true;
    isMesh = true;
    isScaleBox = false;
    isDisplayXY = true;
    isDisplayZ = true;
    isDisplayGrids = false;
  }

  /**
   * Determines whether a plane is plottable, i.e: does not have
   * invalid vertex.
   *
   * @return <code>true</code> if the plane is plottable,
   *         <code>false</code> otherwise
   * @param values vertices array of the plane
   */
  private final boolean plottable(ComplexSurfaceVertex[] values) {
    try {
      return(!values[0].isInvalid()&&!values[1].isInvalid()&&!values[2].isInvalid()&&!values[3].isInvalid());
    } catch(Exception ex) {}
    return false;
  }

  /**
   * Sets the axes scaling factor. Computes the proper axis lengths
   * based on the ratio of variable ranges. The axis lengths will
   * also affect the size of bounding box.
   */
  private final void setAxesScale() {
    double scale_x, scale_y, scale_z, divisor;
    int longest;
    if(!isScaleBox) {
      projector.setScaling(1);
      t_x = t_y = t_z = 4;
      return;
    }
    scale_x = xmax-xmin;
    scale_y = ymax-ymin;
    scale_z = zmax-zmin;
    if(scale_x<scale_y) {
      if(scale_y<scale_z) {
        longest = 3;
        divisor = scale_z;
      } else {
        longest = 2;
        divisor = scale_y;
      }
    } else {
      if(scale_x<scale_z) {
        longest = 3;
        divisor = scale_z;
      } else {
        longest = 1;
        divisor = scale_x;
      }
    }
    scale_x /= divisor;
    scale_y /= divisor;
    scale_z /= divisor;
    if((scale_x<0.2f)||(scale_y<0.2f)&&(scale_z<0.2f)) {
      switch(longest) {
         case 1 :
           if(scale_y<scale_z) {
             scale_y /= scale_z;
             scale_z = 1.0f;
           } else {
             scale_z /= scale_y;
             scale_y = 1.0f;
           }
           break;
         case 2 :
           if(scale_x<scale_z) {
             scale_x /= scale_z;
             scale_z = 1.0f;
           } else {
             scale_z /= scale_x;
             scale_x = 1.0f;
           }
           break;
         case 3 :
           if(scale_y<scale_x) {
             scale_y /= scale_x;
             scale_x = 1.0f;
           } else {
             scale_x /= scale_y;
             scale_y = 1.0f;
           }
           break;
      }
    }
    if(scale_x<0.2f) {
      scale_x = 1.0f;
    }
    projector.setXScaling(scale_x);
    if(scale_y<0.2f) {
      scale_y = 1.0f;
    }
    projector.setYScaling(scale_y);
    if(scale_z<0.2f) {
      scale_z = 1.0f;
    }
    projector.setZScaling(scale_z);
    if(scale_x<0.5f) {
      t_x = 8;
    } else {
      t_x = 4;
    }
    if(scale_y<0.5f) {
      t_y = 8;
    } else {
      t_y = 4;
    }
    if(scale_z<0.5f) {
      t_z = 8;
    } else {
      t_z = 4;
    }
  }

  /**
   * Gets the number of divisions to be displayed.
   * Automatically fixes invalid values.
   *
   * @return valid number of divisions to be displayed
   */
  private int getDispDivisions() {
    int plot_density;
    plot_density = disp_divisions;
    if(plot_density>calc_divisions) {
      plot_density = calc_divisions;
    }
    while((calc_divisions%plot_density)!=0) {
      plot_density++;
    }
    return plot_density;
  }

  /**
   * Creates a surface plot
   */
  private final void plotSurface(Graphics g) {
    double zi, zx;
    int sx, sy;
    int start_lx, end_lx;
    int start_ly, end_ly;
    zi = zmin;
    zx = zmax;
    int plot_density = getDispDivisions();
    int multiple_factor = calc_divisions/plot_density;
    disp_divisions = plot_density;
    zmin = zi;
    zmax = zx;
    /*
    color_factor = 0.8/(zmax-zmin);
    if((plot_mode==ColorMapper.DUALSHADE)||(plot_mode==ColorMapper.RED)||(plot_mode==ColorMapper.GREEN)||(plot_mode==ColorMapper.BLUE)) {
      color_factor *= 0.6/0.8;
    }*/
    if(vertexArray==null) {
      drawBoxGridsTicksLabels(g, false);
      drawBoundingBox(g);
      return;
    }
    if(plot_mode==ColorMapper.NORENDER) {
      drawBoxGridsTicksLabels(g, true);
      drawBoundingBox(g);
      return;
    }
    drawBoxGridsTicksLabels(g, false);
    // SurfaceVertex.setZRange(zmin,zmax);
    zmaxV = zmax;
    zminV = zmin;
    zfactorV = 20/(zmaxV-zminV);
    // direction test
    double distance = projector.getDistance()*projector.getCosElevationAngle();
    // cop : center of projection
    cop = new ComplexSurfaceVertex(distance*projector.getSinRotationAngle(), distance*projector.getCosRotationAngle(), projector.getDistance()*projector.getSinElevationAngle(), 1, 0, this);
    cop.transform();
    boolean inc_x = cop.x>0;
    boolean inc_y = cop.y>0;
    // critical = false;
    if(inc_x) {
      start_lx = 0;
      end_lx = calc_divisions;
      sx = multiple_factor;
    } else {
      start_lx = calc_divisions;
      end_lx = 0;
      sx = -multiple_factor;
    }
    if(inc_y) {
      start_ly = 0;
      end_ly = calc_divisions;
      sy = multiple_factor;
    } else {
      start_ly = calc_divisions;
      end_ly = 0;
      sy = -multiple_factor;
    }
    if((cop.x>10)||(cop.x<-10)) {
      if((cop.y>10)||(cop.y<-10)) {
        plotArea(g, start_lx, start_ly, end_lx, end_ly, sx, sy);
      } else {                      // split in y direction
        int split_y = (int) ((cop.y+10)*plot_density/20)*multiple_factor;
        plotArea(g, start_lx, 0, end_lx, split_y, sx, multiple_factor);
        plotArea(g, start_lx, calc_divisions, end_lx, split_y, sx, -multiple_factor);
      }
    } else {
      if((cop.y>10)||(cop.y<-10)) { // split in x direction
        int split_x = (int) ((cop.x+10)*plot_density/20)*multiple_factor;
        plotArea(g, 0, start_ly, split_x, end_ly, multiple_factor, sy);
        plotArea(g, calc_divisions, start_ly, split_x, end_ly, -multiple_factor, sy);
      } else {                      // split in both x and y directions
        int split_x = (int) ((cop.x+10)*plot_density/20)*multiple_factor;
        int split_y = (int) ((cop.y+10)*plot_density/20)*multiple_factor;
        // critical = true;
        plotArea(g, 0, 0, split_x, split_y, multiple_factor, multiple_factor);
        plotArea(g, 0, calc_divisions, split_x, split_y, multiple_factor, -multiple_factor);
        plotArea(g, calc_divisions, 0, split_x, split_y, -multiple_factor, multiple_factor);
        plotArea(g, calc_divisions, calc_divisions, split_x, split_y, -multiple_factor, -multiple_factor);
      }
    }
    if(isBoxed) {
      drawBoundingBox(g);
    }
  }

  private final int poly_x[] = new int[9];
  private final int poly_y[] = new int[9];

  /**
   * Plots a single plane
   *
   * @param vertex vertices array of the plane
   * @param verticescount number of vertices to process
   */
  private final void plotPlane(Graphics g, ComplexSurfaceVertex[] vertex, int verticescount) {
    double[] samples = new double[3];
    int count, loop, index;
    double re, im, result;
    boolean low1, low2;
    boolean valid1, valid2;
    if(verticescount<3) {
      return;
    }
    count = 0;
    //z = 0.0f;
    re = 0.0f;
    im = 0.0f;
    line_color = Color.black;
    low1 = (vertex[0].z<zmin);
    valid1 = !low1&&(vertex[0].z<=zmax);
    index = 1;
    for(loop = 0; loop<verticescount; loop++) {
      low2 = (vertex[index].z<zmin);
      valid2 = !low2&&(vertex[index].z<=zmax);
      if((valid1||valid2)||(low1^low2)) {
        if(!valid1) {
          if(low1) {
            result = zmin;
          } else {
            result = zmax;
          }
          double ratio = (result-vertex[index].z)/(vertex[loop].z-vertex[index].z);
          double new_x = ratio*(vertex[loop].x-vertex[index].x)+vertex[index].x;
          double new_y = ratio*(vertex[loop].y-vertex[index].y)+vertex[index].y;
          if(low1) {
            projection = projector.project(new_x, new_y, -10);
          } else {
            projection = projector.project(new_x, new_y, 10);
          }
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;
          count++;
          //z += result;
        }
        if(valid2) {
          projection = vertex[index].projection();
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;
          count++;
          //z += vertex[index].z;
          re += vertex[index].re;
          im += vertex[index].im;
        } else {
          if(low2) {
            result = zmin;
          } else {
            result = zmax;
          }
          double ratio = (result-vertex[loop].z)/(vertex[index].z-vertex[loop].z);
          double new_x = ratio*(vertex[index].x-vertex[loop].x)+vertex[loop].x;
          double new_y = ratio*(vertex[index].y-vertex[loop].y)+vertex[loop].y;
          if(low2) {
            projection = projector.project(new_x, new_y, -10);
          } else {
            projection = projector.project(new_x, new_y, 10);
          }
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;
          count++;
          //z += result;
        }
      }
      if(++index==verticescount) {
        index = 0;
      }
      valid1 = valid2;
      low1 = low2;
    }
    if(count>0) {
      switch(plot_mode) {
         case ColorMapper.NORENDER :
           g.setColor(Color.lightGray);
           break;
         default :
           samples[0] = 0.99;
           samples[1] = re;
           samples[2] = im;
           g.setColor(colorMap.samplesToColor(samples));
      }
      g.fillPolygon(poly_x, poly_y, count);
      g.setColor(line_color);
      if(isMesh) {
        poly_x[count] = poly_x[0];
        poly_y[count] = poly_y[0];
        count++;
        g.drawPolygon(poly_x, poly_y, count);
      }
    }
  }

  /**
   * Plots an area of group of planes
   *
   * @param start_lx start index in x direction
   * @param start_ly start index in y direction
   * @param end_lx   end index in x direction
   * @param end_ly   end index in y direction
   * @param sx       step in x direction
   * @param sy       step in y direction
   */
  private final void plotArea(Graphics g, int start_lx, int start_ly, int end_lx, int end_ly, int sx, int sy) {
    start_lx *= calc_divisions+1;
    sx *= calc_divisions+1;
    end_lx *= calc_divisions+1;
    int lx = start_lx;
    int ly = start_ly;
    while(ly!=end_ly) {
      values1[1] = vertexArray[lx+ly];
      values1[2] = vertexArray[lx+ly+sy];
      while(lx!=end_lx) {
        values1[0] = values1[1];
        values1[1] = vertexArray[lx+sx+ly];
        values1[3] = values1[2];
        values1[2] = vertexArray[lx+sx+ly+sy];
        if(plottable(values1)) {
          plotPlane(g, values1, 4);
        }
        lx += sx;
      }
      ly += sy;
      lx = start_lx;
    }
  }

  /**
   * Draws non-surface parts, i.e: bounding box, axis grids, axis ticks,
   * axis labels, base plane.
   *
   * @param g         the graphics context to draw
   * @param draw_axes if <code>true</code>, only draws base plane and z axis
   */
  private final void drawBoxGridsTicksLabels(Graphics g, boolean draw_axes) {
    Point projection, tickpos;
    boolean x_left = false, y_left = false;
    int x[], y[], i;
    x = new int[5];
    y = new int[5];
    if(projector==null) {
      return;
    }
    if(draw_axes) {
      drawBase(g, x, y);
      projection = projector.project(0, 0, -10);
      x[0] = projection.x;
      y[0] = projection.y;
      projection = projector.project(10.5f, 0, -10);
      g.drawLine(x[0], y[0], projection.x, projection.y);
      if(projection.x<x[0]) {
        outString(g, (int) (1.05*(projection.x-x[0]))+x[0], (int) (1.05*(projection.y-y[0]))+y[0], "x", Label.RIGHT, TOP); //$NON-NLS-1$
      } else {
        outString(g, (int) (1.05*(projection.x-x[0]))+x[0], (int) (1.05*(projection.y-y[0]))+y[0], "x", Label.LEFT, TOP); //$NON-NLS-1$
      }
      projection = projector.project(0, 11.5f, -10);
      g.drawLine(x[0], y[0], projection.x, projection.y);
      if(projection.x<x[0]) {
        outString(g, (int) (1.05*(projection.x-x[0]))+x[0], (int) (1.05*(projection.y-y[0]))+y[0], "y", Label.RIGHT, TOP); //$NON-NLS-1$
      } else {
        outString(g, (int) (1.05*(projection.x-x[0]))+x[0], (int) (1.05*(projection.y-y[0]))+y[0], "y", Label.LEFT, TOP); //$NON-NLS-1$
      }
      projection = projector.project(0, 0, 10.5f);
      g.drawLine(x[0], y[0], projection.x, projection.y);
      outString(g, (int) (1.05*(projection.x-x[0]))+x[0], (int) (1.05*(projection.y-y[0]))+y[0], "z", Label.CENTER, CENTER); //$NON-NLS-1$
    } else {
      factor_x = factor_y = 1;
      projection = projector.project(0, 0, -10);
      x[0] = projection.x;
      projection = projector.project(10.5f, 0, -10);
      y_left = projection.x>x[0];
      i = projection.y;
      projection = projector.project(-10.5f, 0, -10);
      if(projection.y>i) {
        factor_x = -1;
        y_left = projection.x>x[0];
      }
      projection = projector.project(0, 10.5f, -10);
      x_left = projection.x>x[0];
      i = projection.y;
      projection = projector.project(0, -10.5f, -10);
      if(projection.y>i) {
        factor_y = -1;
        x_left = projection.x>x[0];
      }
      setAxesScale();
      drawBase(g, x, y);
      if(isBoxed) {
        projection = projector.project(-factor_x*10, -factor_y*10, -10);
        x[0] = projection.x;
        y[0] = projection.y;
        projection = projector.project(-factor_x*10, -factor_y*10, 10);
        x[1] = projection.x;
        y[1] = projection.y;
        projection = projector.project(factor_x*10, -factor_y*10, 10);
        x[2] = projection.x;
        y[2] = projection.y;
        projection = projector.project(factor_x*10, -factor_y*10, -10);
        x[3] = projection.x;
        y[3] = projection.y;
        x[4] = x[0];
        y[4] = y[0];
        if(plot_mode!=ColorMapper.WIREFRAME) {
          if(plot_mode==ColorMapper.NORENDER) {
            g.setColor(Color.lightGray);
          } else {
            g.setColor(new Color(192, 220, 192));
          }
          g.fillPolygon(x, y, 4);
        }
        g.setColor(Color.black);
        g.drawPolygon(x, y, 5);
        projection = projector.project(-factor_x*10, factor_y*10, 10);
        x[2] = projection.x;
        y[2] = projection.y;
        projection = projector.project(-factor_x*10, factor_y*10, -10);
        x[3] = projection.x;
        y[3] = projection.y;
        x[4] = x[0];
        y[4] = y[0];
        if(plot_mode!=ColorMapper.WIREFRAME) {
          if(plot_mode==ColorMapper.NORENDER) {
            g.setColor(Color.lightGray);
          } else {
            g.setColor(new Color(192, 220, 192));
          }
          g.fillPolygon(x, y, 4);
        }
        g.setColor(Color.black);
        g.drawPolygon(x, y, 5);
      } else if(isDisplayZ) {
        projection = projector.project(factor_x*10, -factor_y*10, -10);
        x[0] = projection.x;
        y[0] = projection.y;
        projection = projector.project(factor_x*10, -factor_y*10, 10);
        g.drawLine(x[0], y[0], projection.x, projection.y);
        projection = projector.project(-factor_x*10, factor_y*10, -10);
        x[0] = projection.x;
        y[0] = projection.y;
        projection = projector.project(-factor_x*10, factor_y*10, 10);
        g.drawLine(x[0], y[0], projection.x, projection.y);
      }
      for(i = -9; i<=9; i++) {
        if(isDisplayXY||isDisplayGrids) {
          if(!isDisplayGrids||(i%(t_y/2)==0)||isDisplayXY) {
            if(isDisplayGrids&&(i%t_y==0)) {
              projection = projector.project(-factor_x*10, i, -10);
            } else {
              if(i%t_y!=0) {
                projection = projector.project(factor_x*9.8f, i, -10);
              } else {
                projection = projector.project(factor_x*9.5f, i, -10);
              }
            }
            tickpos = projector.project(factor_x*10, i, -10);
            g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
            if((i%t_y==0)&&isDisplayXY) {
              tickpos = projector.project(factor_x*10.5f, i, -10);
              if(y_left) {
                outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(ymax-ymin)+ymin, Label.LEFT, TOP);
              } else {
                outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(ymax-ymin)+ymin, Label.RIGHT, TOP);
              }
            }
          }
          if(!isDisplayGrids||(i%(t_x/2)==0)||isDisplayXY) {
            if(isDisplayGrids&&(i%t_x==0)) {
              projection = projector.project(i, -factor_y*10, -10);
            } else {
              if(i%t_x!=0) {
                projection = projector.project(i, factor_y*9.8f, -10);
              } else {
                projection = projector.project(i, factor_y*9.5f, -10);
              }
            }
            tickpos = projector.project(i, factor_y*10, -10);
            g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
            if((i%t_x==0)&&isDisplayXY) {
              tickpos = projector.project(i, factor_y*10.5f, -10);
              if(x_left) {
                outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(xmax-xmin)+xmin, Label.LEFT, TOP);
              } else {
                outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(xmax-xmin)+xmin, Label.RIGHT, TOP);
              }
            }
          }
        }
        if(isDisplayXY) {
          tickpos = projector.project(0, factor_y*14, -10);
          outString(g, tickpos.x, tickpos.y, "X", Label.CENTER, TOP); //$NON-NLS-1$
          tickpos = projector.project(factor_x*14, 0, -10);
          outString(g, tickpos.x, tickpos.y, "Y", Label.CENTER, TOP); //$NON-NLS-1$
        }
        // z grids and ticks
        if(isDisplayZ||(isDisplayGrids&&isBoxed)) {
          if(!isDisplayGrids||(i%(t_z/2)==0)||isDisplayZ) {
            if(isBoxed&&isDisplayGrids&&(i%t_z==0)) {
              projection = projector.project(-factor_x*10, -factor_y*10, i);
              tickpos = projector.project(-factor_x*10, factor_y*10, i);
            } else {
              if(i%t_z==0) {
                projection = projector.project(-factor_x*10, factor_y*9.5f, i);
              } else {
                projection = projector.project(-factor_x*10, factor_y*9.8f, i);
              }
              tickpos = projector.project(-factor_x*10, factor_y*10, i);
            }
            g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
            if(isDisplayZ) {
              tickpos = projector.project(-factor_x*10, factor_y*10.5f, i);
              if(i%t_z==0) {
                if(x_left) {
                  outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(zmax-zmin)+zmin, Label.LEFT, CENTER);
                } else {
                  outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(zmax-zmin)+zmin, Label.RIGHT, CENTER);
                }
              }
            }
            if(isDisplayGrids&&isBoxed&&(i%t_z==0)) {
              projection = projector.project(-factor_x*10, -factor_y*10, i);
              tickpos = projector.project(factor_x*10, -factor_y*10, i);
            } else {
              if(i%t_z==0) {
                projection = projector.project(factor_x*9.5f, -factor_y*10, i);
              } else {
                projection = projector.project(factor_x*9.8f, -factor_y*10, i);
              }
              tickpos = projector.project(factor_x*10, -factor_y*10, i);
            }
            g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
            if(isDisplayZ) {
              tickpos = projector.project(factor_x*10.5f, -factor_y*10, i);
              if(i%t_z==0) {
                if(y_left) {
                  outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(zmax-zmin)+zmin, Label.LEFT, CENTER);
                } else {
                  outFloat(g, tickpos.x, tickpos.y, (double) (i+10)/20*(zmax-zmin)+zmin, Label.RIGHT, CENTER);
                }
              }
            }
            if(isDisplayGrids&&isBoxed) {
              if(i%t_y==0) {
                projection = projector.project(-factor_x*10, i, -10);
                tickpos = projector.project(-factor_x*10, i, 10);
                g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
              }
              if(i%t_x==0) {
                projection = projector.project(i, -factor_y*10, -10);
                tickpos = projector.project(i, -factor_y*10, 10);
                g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Draws the base plane. The base plane is the x-y plane.
   *
   * @param g the graphics context to draw.
   * @param x used to retrieve x coordinates of drawn plane from this method.
   * @param y used to retrieve y coordinates of drawn plane from this method.
   */
  private final void drawBase(Graphics g, int[] x, int[] y) {
    Point projection = projector.project(-10, -10, -10);
    x[0] = projection.x;
    y[0] = projection.y;
    projection = projector.project(-10, 10, -10);
    x[1] = projection.x;
    y[1] = projection.y;
    projection = projector.project(10, 10, -10);
    x[2] = projection.x;
    y[2] = projection.y;
    projection = projector.project(10, -10, -10);
    x[3] = projection.x;
    y[3] = projection.y;
    x[4] = x[0];
    y[4] = y[0];
    if(plot_mode!=ColorMapper.WIREFRAME) {
      if(plot_mode==ColorMapper.NORENDER) {
        g.setColor(Color.lightGray);
      } else {
        g.setColor(new Color(192, 220, 192));
      }
      g.fillPolygon(x, y, 4);
    }
    g.setColor(Color.black);
    g.drawPolygon(x, y, 5);
  }

  /**
   * Draws the bounding box of surface.
   */
  private final void drawBoundingBox(Graphics g) {
    Point startingpoint, projection;
    startingpoint = projector.project(factor_x*10, factor_y*10, 10);
    g.setColor(Color.black);
    projection = projector.project(-factor_x*10, factor_y*10, 10);
    g.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    projection = projector.project(factor_x*10, -factor_y*10, 10);
    g.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    projection = projector.project(factor_x*10, factor_y*10, -10);
    g.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
  }

  /**
   * Draws string at the specified coordinates with the specified alignment.
   *
   * @param g       graphics context to draw
   * @param x       the x coordinate
   * @param y       the y coordinate
   * @param s       the string to draw
   * @param x_align the alignment in x direction
   * @param y_align the alignment in y direction
   */
  private final void outString(Graphics g, int x, int y, String s, int x_align, int y_align) {
    switch(y_align) {
       case TOP :
         y += g.getFontMetrics(g.getFont()).getAscent();
         break;
       case CENTER :
         y += g.getFontMetrics(g.getFont()).getAscent()/2;
         break;
    }
    switch(x_align) {
       case Label.LEFT :
         g.drawString(s, x, y);
         break;
       case Label.RIGHT :
         g.drawString(s, x-g.getFontMetrics(g.getFont()).stringWidth(s), y);
         break;
       case Label.CENTER :
         g.drawString(s, x-g.getFontMetrics(g.getFont()).stringWidth(s)/2, y);
         break;
    }
  }

  /**
   * Draws double at the specified coordinates with the specified alignment.
   *
   * @param g       graphics context to draw
   * @param x       the x coordinate
   * @param y       the y coordinate
   * @param f       the double to draw
   * @param x_align the alignment in x direction
   * @param y_align the alignment in y direction
   */
  private final void outFloat(Graphics g, int x, int y, double f, int x_align, int y_align) {
    // String s = Double.toString(f);
    String s = labelFormat.format(f);
    outString(g, x, y, s, x_align, y_align);
  }

  /**
   * Determines the palette type that will be used.
   * @param type
   */
  public void setPaletteType(int type) {
    plot_mode = type;
  }

  /**
   * Sets the format for the axis labels.
   *
   * For example, _format=0.000 will produce three digits to the right of decimal point
   *
   * @param _format the format string
   */
  public void setLabelFormat(String _format) {
    labelFormat = new DecimalFormat(_format);
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
    return zmin;
  }

  /**
   * Gets the ceiling for scaling the z data.
   * @return double
   */
  public double getCeiling() {
    return zmax;
  }

  /**
   * Sets the floor and ceiling colors.
   *
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    colorMap.setCeilColor(ceilColor);
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
   * Updates the surface plot using the current data.
   */
  public synchronized void update() {
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
    }
    double left = griddata.getLeft(), right = griddata.getRight(), top = griddata.getTop(), bottom = griddata.getBottom();
    xmin = Math.min(left, right);
    xmax = Math.max(left, right);
    ymin = Math.min(bottom, top);
    ymax = Math.max(bottom, top);
    if(griddata instanceof ArrayData) {
      generateVerticesFromArray((ArrayData) griddata);
    } else if(griddata instanceof GridPointData) {
      generateVerticesFromPoints((GridPointData) griddata);
    }
  }

  /**
   * Translates the view by the specified number of pixels.
   *
   * @param xpix the x translation in pixels
   * @param ypix the y translation in pixels
   */
  public void setTranslation(int xpix, int ypix) {
    projector.set2DTranslation(xpix, ypix);
  }

  /**
   * Sets the viewing rotation angle.
   *
   * @param angle the rotation angle in degrees
   */
  public void setRotationAngle(double angle) {
    projector.setRotationAngle(angle);
  }

  /**
   * Sets the viewing elevation angle.
   *
   * @param angle the elevation angle in degrees
   */
  public void setElevationAngle(double angle) {
    projector.setElevationAngle(angle);
  }

  /**
   * Sets the viewing distance.
   *
   * @param distance the distance
   */
  public void setDistance(double distance) {
    projector.setDistance(distance);
  }

  /**
   * Sets the 2D scaling factor.
   *
   * @param scale the scaling factor
   */
  public void set2DScaling(double scale) {
    projector.set2DScaling(scale);
  }

  /**
   * <code>mouseDown</code> event handler. Sets internal tracking variables
   * for dragging operations.
   *
   * @param e the event
   * @param drawingPanel
   *
   * @return mouse pressed flag
   */
  public boolean mousePressed(MouseEvent e, DrawingPanel drawingPanel) {
    click_x = e.getX();
    click_y = e.getY();
    //mouseDown = true;
    return true;
  }

  /**
   * Method mouseReleased
   *
   * @param e
   * @param drawingPanel
   */
  public void mouseReleased(MouseEvent e, DrawingPanel drawingPanel) {
    //mouseDown = false;
  }

  /**
   * <code>mouseDrag<code> event handler. Tracks dragging operations.
   * Checks the delay regeneration flag and does proper actions.
   *
   * @param e the event
   * @param drawingPanel
   */
  public void mouseDragged(MouseEvent e, DrawingPanel drawingPanel) {
    double new_value = 0.0;
    int x = e.getX();
    int y = e.getY();
    if(e.isControlDown()) {
      projector.set2D_xTranslation(projector.get2D_xTranslation()+(x-click_x));
      projector.set2D_yTranslation(projector.get2D_yTranslation()+(y-click_y));
    } else if(e.isShiftDown()) {
      new_value = projector.get2DScaling()+(y-click_y)*0.5;
      if(new_value>60.0f) {
        new_value = 60.0f;
      }
      if(new_value<2.0f) {
        new_value = 2.0f;
      }
      projector.set2DScaling(new_value);
    } else {
      new_value = projector.getRotationAngle()+(x-click_x);
      while(new_value>360) {
        new_value -= 360;
      }
      while(new_value<0) {
        new_value += 360;
      }
      projector.setRotationAngle(new_value);
      new_value = projector.getElevationAngle()+(y-click_y);
      if(new_value>90) {
        new_value = 90;
      } else if(new_value<0) {
        new_value = 0;
      }
      projector.setElevationAngle(new_value);
    }
    click_x = x;
    click_y = y;
    invalidProjection = true;
    drawingPanel.render();
  }

  /**
   * Gets the minimum x needed to draw this object.
   * @return minimum
   */
  public double getXMin() {
    return 0;
  }

  /**
   * Gets the maximum x needed to draw this object.
   * @return maximum
   */
  public double getXMax() {
    return 0;
  }

  /**
   * Gets the minimum y needed to draw this object.
   * @return minimum
   */
  public double getYMin() {
    return 0;
  }

  /**
   * Gets the maximum y needed to draw this object.
   * @return minimum
   */
  public double getYMax() {
    return 0;
  }

  /**
   * Determines if information is available to set min/max values.
   * X y values have no meaning for this plot.
   *
   * @return false
   */
  public boolean isMeasured() {
    return false;
  }

  /**
   * Gets an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Plot2DLoader() {
      public Object createObject(XMLControl control) {
        return new ComplexSurfacePlot(null);
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
