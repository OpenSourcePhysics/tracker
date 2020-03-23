/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * GridPointData stores numeric data on a scaled rectangular grid using an array of points.
 *
 * Every grid point contains the x and y coordinates and one or more components.
 * The first component is usually the magnitude of the quantity of interest.
 *
 * Components can represent almost anything. For example, we often use color-coded
 * arrows to display vector fields. The arrows's color is the first sample and its vertical and
 * horizonal components are the second and third components.  This data is stored
 * in an internal array as follows:
 * <br>
 * <pre>
 * <code>data=new double [n][m][5]<\code>
 * <code>vertex=data[n][m]<\code>
 *
 * <code>vertex[0] = x  <\code>
 * <code>vertex[1] = y  <\code>
 * <code>vertex[2] = val_1  <\code>
 * <code>vertex[3] = val_2  <\code>
 * <code>vertex[4] = val_3  <\code>
 * <\pre>
 *
 * @author     Wolfgang Christian
 * @created    Feb 3, 2004
 * @version    1.1
 */
public class GridPointData implements GridData {
  protected double[][][] data;
  protected double left, right, bottom, top;
  protected double dx = 0, dy = 0;
  protected boolean cellData = false;
  protected String[] names;

  /**
   * Constructor Data2D
   *
   * @param ix
   * @param iy
   * @param ncomponents
   */
  public GridPointData(int ix, int iy, int ncomponents) {
    if((iy<1)||(ix<1)) {
      throw new IllegalArgumentException("Number of dataset rows and columns must be positive. Your row="+iy+"  col="+ix); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ncomponents<1)) {
      throw new IllegalArgumentException("Number of 2d data components must be positive. Your ncomponents="+ncomponents); //$NON-NLS-1$
    }
    data = new double[ix][iy][ncomponents+2]; // x, y, and components
    setScale(0, ix, 0, iy);
    names = new String[ncomponents];
    for(int i = 0; i<ncomponents; i++) {
      names[i] = "Component_"+i; //$NON-NLS-1$
    }
  }

  /**
   * Creates a new GridPointData object with the same grid points and the given number of components.
   *
   * @param ncomponents number of samples dataset.
   * @return the newly created  Data2D
   */
  public GridPointData createGridPointData(int ncomponents) {
    GridPointData data2d = new GridPointData(data.length, data[0].length, ncomponents+2);
    data2d.setScale(left, right, bottom, top);
    return data2d;
  }

  /**
   * Sets the name of the component.
   *
   * @param i int the component index
   * @param name String
   */
  public void setComponentName(int i, String name) {
    names[i] = name;
  }

  /**
   * Gets the name of the component,
   * @param i int the component index
   * @return String the name
   */
  public String getComponentName(int i) {
    return names[i];
  }

  /**
   * Gets the number of data components.
   *
   * @return int
   */
  public int getComponentCount() {
    return data[0][0].length-2;
  }

  /**
   * Sets the left, right, bottom, and top of the grid data using a lattice model.
   *
   * @param _left
   * @param _right
   * @param _bottom
   * @param _top
   */
  public void setScale(double _left, double _right, double _bottom, double _top) {
    cellData = false;
    left = _left;
    right = _right;
    bottom = _bottom;
    top = _top;
    int ix = data.length;
    int iy = data[0].length;
    dx = 0; // special case if #col==1
    if(ix>1) {
      dx = (right-left)/(ix-1);
    }
    dy = 0; // special ase if #row==1
    if(iy>1) {
      dy = (bottom-top)/(iy-1); // note that dy is usually negative because pixel cooridinates decrease as the world units increase.
    }
    double x = left;
    for(int i = 0; i<ix; i++) {
      double y = top;
      for(int j = 0; j<iy; j++) {
        data[i][j][0] = x; // x location
        data[i][j][1] = y; // y location
        y += dy;           // iy loop
      }
      x += dx;             // ix loop
    }
  }

  /**
   * Sets the left, right, bottom, and top of the grid data using a cell model.
   *
   * Coordinates are centered on each cell and will NOT include the edges.
   *
   * @param _left
   * @param _right
   * @param _bottom
   * @param _top
   */
  public void setCellScale(double _left, double _right, double _bottom, double _top) {
    cellData = true;
    int nx = data.length;
    int ny = data[0].length;
    dx = 0; // special case if #col==1
    if(nx>1) {
      dx = (_right-_left)/(nx);
    }
    dy = 0; // special ase if #row==1
    if(ny>1) {
      dy = (_bottom-_top)/(ny); // note that dy is usualy negative
    }
    double x = _left+dx/2;
    for(int i = 0; i<nx; i++) {
      double y = _top+dy/2;
      for(int j = 0; j<ny; j++) {
        data[i][j][0] = x; // x location
        data[i][j][1] = y; // y location
        y += dy;           // iy loop
      }
      x += dx;             // ix loop
    }
    left = _left+dx/2;
    right = _right-dx/2;
    bottom = _bottom-dy/2;
    top = _top+dy/2;
  }

  /**
   * Sets the grid such that the centers of the corner cells match the given coordinates.
   *
   * Coordinates are centered on each cell and the bounds are ouside the max and min values.
   *
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setCenteredCellScale(double xmin, double xmax, double ymin, double ymax) {
    int nx = data.length;
    int ny = data[0].length;
    double delta = (nx>1) ? (xmax-xmin)/(nx-1)/2 : 0;
    xmin -= delta;
    xmax += delta;
    delta = (ny>1) ? (ymax-ymin)/(ny-1)/2 : 0;
    ymin -= delta;
    ymax += delta;
    setCellScale(xmin, xmax, ymin, ymax);
  }

  /**
   * Gets the cellData flag.
   *
   * @return true if cell data.
   */
  public boolean isCellData() {
    return cellData;
  }

  /**
   * Gets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @return the value.
   */
  public double getValue(int ix, int iy, int component) {
    return data[ix][iy][component+2];
  }

  /**
   * Sets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @param value
   */
  public void setValue(int ix, int iy, int component, double value) {
    data[ix][iy][component+2] = value;
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx() {
    return data.length;
  }

  /**
   * Gets the number of y entries.
   * @return ny
   */
  public int getNy() {
    return data[0].length;
  }

  /**
   * Gets the minimum and maximum values of the n-th component.
   *
   * @param n the component
   * @return {zmin,zmax}
   */
  public double[] getZRange(int n) {
    int index = 2+n;
    double zmin = data[0][0][index];
    double zmax = zmin;
    for(int i = 0, mx = data.length; i<mx; i++) {
      for(int j = 0, my = data[0].length; j<my; j++) {
        double v = data[i][j][index];
        if(v>zmax) {
          zmax = v;
        }
        if(v<zmin) {
          zmin = v;
        }
      }
    }
    return new double[] {zmin, zmax};
  }

  /**
   * Gets the vertex closest to the specified location
   * @param x
   * @param y
   *
   * @return vertex array
   */
  public double[] getVertex(double x, double y) {
    int nx = (int) Math.floor((x-left)/dx);
    nx = Math.max(0, nx);             // cannot be less than 0
    nx = Math.min(nx, data.length-1); // cannot be greater than last element
    int ny = (int) Math.floor(-(top-y)/dy);
    ny = Math.max(0, ny);                // cannot be less than 0
    ny = Math.min(ny, data[0].length-1); // cannot be greater than last element
    return data[nx][ny];
  }

  /**
   * Estimates the value of a component at an untabulated point, (x,y).
   *
   * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the grid boundaries, the gradient changes discontinuously
   * at the grid square boundaries.
   *
   * @param x  the untabulated x
   * @param y  the untabulated y
   * @param index the component index
   * @return the interpolated sample
   */
  public double interpolate(double x, double y, int index) {
    int ix = (int) ((x-data[0][0][0])/dx);
    ix = Math.max(0, ix);
    ix = Math.min(data.length-2, ix);
    int iy = (int) ((y-data[0][0][1])/dy);
    iy = Math.max(0, iy);
    iy = Math.min(data[0].length-2, iy);
    double t = (x-data[ix][iy][0])/dx;
    double u = (y-data[ix][iy][1])/dy;
    index += 2;
    return(1-t)*(1-u)*data[ix][iy][index]+t*(1-u)*data[ix+1][iy][index]+t*u*data[ix+1][iy+1][index]+(1-t)*u*data[ix][iy+1][index];
  }

  /**
   * Estimates multiple sample components at an untabulated point, (x,y).
   *
   * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the grid boundaries, the gradient changes discontinuously
   * at the grid square boundaries.
   *
   * @param x  untabulated x
   * @param y  untabulated y
   * @param indexes to be interpolated
   * @param values array will contain the interpolated values
   * @return the interpolated array
   */
  public double[] interpolate(double x, double y, int[] indexes, double[] values) {
    int ix = (int) ((x-data[0][0][0])/dx);
    ix = Math.max(0, ix);
    ix = Math.min(data.length-2, ix);
    int iy = (int) ((y-data[0][0][1])/dy);
    iy = Math.max(0, iy);
    iy = Math.min(data[0].length-2, iy);
    double t = (x-data[ix][iy][0])/dx;
    double u = (y-data[ix][iy][1])/dy;
    for(int i = 0, n = indexes.length; i<n; i++) {
      int index = indexes[i]+2;
      values[i] = (1-t)*(1-u)*data[ix][iy][index]+t*(1-u)*data[ix+1][iy][index]+t*u*data[ix+1][iy+1][index]+(1-t)*u*data[ix][iy+1][index];
    }
    return values;
  }

  /**
   * Gets the array containing the data.
   *
   * @return the data
   */
  public double[][][] getData() {
    return data;
  }

  /**
   * Sets the array containing the data.
   *
   * Use with caution.  This method is included for backward compatibility.  Users are responsible for setting the x and y coordinate values.
   * Users are also responsible for synchronization with clients.
   */
  public void setData(double[][][] newdata) {
    data = newdata;
    int nx = data.length-1;
    int ny = data[0].length-1;
    left = data[0][0][0];
    right = data[nx][ny][0];
    top = data[0][0][1];
    bottom = data[nx][ny][1];
    dx = (right-left)/nx;
    dy = (bottom-top)/ny;
    cellData = false;
  }

  /**
   * Gets the x value for the first column in the grid.
   * @return  the leftmost x value
   */
  public final double getLeft() {
    return left;
  }

  /**
   * Gets the x value for the right column in the grid.
   * @return  the rightmost x value
   */
  public final double getRight() {
    return right;
  }

  /**
   * Gets the y value for the first row of the grid.
   * @return  the topmost y value
   */
  public final double getTop() {
    return top;
  }

  /**
   * Gets the y value for the last row of the grid.
   * @return the bottommost y value
   */
  public final double getBottom() {
    return bottom;
  }

  /**
   * Gets the change in x between grid columns.
   * @return the bottommost y value
   */
  public final double getDx() {
    return dx;
  }

  /**
   * Gets the change in y between grid rows.
   * @return the bottommost y value
   */
  public final double getDy() {
    return dy;
  }

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  public double indexToX(int i) {
    return((data==null)||(i>=data.length)) ? Double.NaN : data[i][0][0]; // use stored x value from first row
  }

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  public double indexToY(int i) {
    return((data==null)||(i>=data[0].length)) ? Double.NaN : data[0][i][1]; // use stored y value from first column
  }

  /**
   * Gets closest index from the given x  world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  public int xToIndex(double x) {
    if(data==null) {
      return 0;
    }
    int nx = getNx();
    double dx = (right-left)/nx;
    int i = (int) ((x-left)/dx);
    if(i<0) {
      return 0;
    }
    if(i>=nx) {
      return nx-1;
    }
    return i;
  }

  /**
   * Gets closest index from the given y  world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  public int yToIndex(double y) {
    if(data==null) {
      return 0;
    }
    int ny = getNy();
    double dy = (top-bottom)/ny;
    int i = (int) ((top-y)/dy);
    if(i<0) {
      return 0;
    }
    if(i>=ny) {
      return ny-1;
    }
    return i;
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load Dataset data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      GridPointData gpd = (GridPointData) obj;
      control.setValue("left", gpd.left);             //$NON-NLS-1$
      control.setValue("right", gpd.right);           //$NON-NLS-1$
      control.setValue("bottom", gpd.bottom);         //$NON-NLS-1$
      control.setValue("top", gpd.top);               //$NON-NLS-1$
      control.setValue("dx", gpd.dx);                 //$NON-NLS-1$
      control.setValue("dy", gpd.dy);                 //$NON-NLS-1$
      control.setValue("is cell data", gpd.cellData); //$NON-NLS-1$
      control.setValue("data", gpd.data);             //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new GridPointData(1, 1, 1);
    }

    public Object loadObject(XMLControl control, Object obj) {
      GridPointData gpd = (GridPointData) obj;
      double[][][] data = (double[][][]) control.getObject("data"); //$NON-NLS-1$
      gpd.data = data;
      gpd.left = control.getDouble("left");              //$NON-NLS-1$
      gpd.right = control.getDouble("right");            //$NON-NLS-1$
      gpd.bottom = control.getDouble("bottom");          //$NON-NLS-1$
      gpd.top = control.getDouble("top");                //$NON-NLS-1$
      gpd.dx = control.getDouble("dx");                  //$NON-NLS-1$
      gpd.dy = control.getDouble("dy");                  //$NON-NLS-1$
      gpd.cellData = control.getBoolean("is cell data"); //$NON-NLS-1$
      return obj;
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
