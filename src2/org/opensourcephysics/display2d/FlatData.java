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
 * FlatData stores numeric data for 2d visualizations using a single array.
 * Components are stored in the array in row-major order.
 *
 * Data components can represent almost anything. For example, we store an n by m grid of complex numbers as follows:
 * <br>
 * <pre>
 * <code>data=new double [2*n*m]<\code>
 * <\pre>
 *
 * @author     Wolfgang Christian
 * @created    Jan 2, 2004
 * @version    1.0
 */
public class FlatData implements GridData {
  protected double[] data;
  protected double left, right, bottom, top;
  protected double dx = 0, dy = 0;
  protected boolean cellData = false;
  protected String[] names;
  int nx, ny, stride;

  /**
   * FlatData constructor.  The data array will contain ncomponents*ix*iy values.
   *
   * @param ix  the number of x values
   * @param iy  the number of y values
   * @param ncomponents the number of components
   */
  public FlatData(int ix, int iy, int ncomponents) {
    if((iy<1)||(ix<1)) {
      throw new IllegalArgumentException("Number of dataset rows and columns must be positive. Your row="+iy+"  col="+ix); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((ncomponents<1)) {
      throw new IllegalArgumentException("Number of 2d data components must be positive. Your ncomponents="+ncomponents); //$NON-NLS-1$
    }
    nx = ix;
    ny = iy;
    stride = ncomponents;
    data = new double[ncomponents*ix*iy]; // x, y, and components
    setScale(0, ix, 0, iy);
    names = new String[ncomponents];
    for(int i = 0; i<ncomponents; i++) {
      names[i] = "Component_"+i; //$NON-NLS-1$
    }
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
    return stride;
  }

  /**
   * Sets the left, right, bottom, and top of the grid data using a lattice model.
   * Lattice model XY coordinates are the edges of each cell and NOT the center.
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
    int ix = this.nx;
    int iy = this.ny;
    dx = 0; // special case if #col==1
    if(ix>1) {
      dx = (right-left)/(ix-1);
    }
    dy = 0; // special ase if #row==1
    if(iy>1) {
      dy = (bottom-top)/(iy-1); // note that dy is usualy negative
    }
    if(dx==0) {
      left -= 0.5;
      right += 0.5;
    }
    if(dy==0) {
      bottom -= 0.5;
      top += 0.5;
    }
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
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("x index out of range in getValue"); //$NON-NLS-1$
    }
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("y index out of range in getValue"); //$NON-NLS-1$
    }
    return data[(iy*nx+ix)*stride+component];
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
    if((ix<0)||(ix>=nx)) {
      throw new IllegalArgumentException("x index out of range in getValue"); //$NON-NLS-1$
    }
    if((iy<0)||(iy>=ny)) {
      throw new IllegalArgumentException("y index out of range in getValue"); //$NON-NLS-1$
    }
    data[(iy*nx+ix)*stride+component] = value;
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx() {
    return nx;
  }

  /**
   * Gets the number of y entries.
   * @return ny
   */
  public int getNy() {
    return ny;
  }

  /**
   * Sets the left, right, bottom, and top of the grid data using a cell model.
   * Cell model XY coordinates are centered on each cell NOT along the edges.
   *
   * @param _left
   * @param _right
   * @param _bottom
   * @param _top
   */
  public void setCellScale(double _left, double _right, double _bottom, double _top) {
    cellData = true;
    int ix = nx;
    int iy = ny;
    dx = 0; // special case if #col==1
    if(ix>1) {
      dx = (_right-_left)/ix;
    }
    dy = 0; // special ase if #row==1
    if(iy>1) {
      dy = (_bottom-_top)/iy; // note that dy is usualy negative
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
    double delta = (nx>1) ? (xmax-xmin)/(nx-1)/2 : 0;
    xmin -= delta;
    xmax += delta;
    delta = (ny>1) ? (ymax-ymin)/(ny-1)/2 : 0;
    ymin -= delta;
    ymax += delta;
    setCellScale(xmin, xmax, ymin, ymax);
  }

  /**
   * Estimates the value of a component at an untabulated point, (x,y).
   *
   * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the grid boundaries, the gradient changes discontinuously
   * at the grid-square boundaries.
   *
   * @param x  the untabulated x
   * @param y  the untabulated y
   * @param index
   * @return the interpolated sample
   */
  public double interpolate(double x, double y, int index) {
    int ix = (int) ((x-left)/dx);
    ix = Math.max(0, ix);
    ix = Math.min(nx-2, ix);
    int iy = -(int) ((top-y)/dy);
    iy = Math.max(0, iy);
    iy = Math.min(ny-2, iy);
    double t = (x-left)/dx-ix;
    double u = -(top-y)/dy-iy;
    if(ix<0) {
      int i = nx*iy*stride+index;
      return(1-u)*data[i]+u*data[i+nx*stride];
    } else if(iy<0) {
      int i = ix*stride+index;
      return(1-t)*data[i]+t*data[i+stride];
    } else {
      int i = nx*iy*stride+ix*stride+index;
      return(1-t)*(1-u)*data[i]+t*(1-u)*data[i+stride]+t*u*data[i+nx*stride+stride]+(1-t)*u*data[i+nx*stride];
    }
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
    int ix = (int) ((x-left)/dx);
    ix = Math.max(0, ix);
    ix = Math.min(nx-2, ix);
    int iy = -(int) ((top-y)/dy);
    iy = Math.max(0, iy);
    iy = Math.min(ny-2, iy);
    // special case if there is only one row and one column
    if((ix<0)&&(iy<0)) {
      for(int i = 0, n = indexes.length; i<n; i++) {
        values[i] = data[indexes[i]];
      }
      return values;
    } else if(ix<0) {
      double u = -(top-y)/dy-iy;
      for(int i = 0, n = indexes.length; i<n; i++) {
        int ii = nx*iy*stride+indexes[i];
        values[i] = (1-u)*data[ii]+u*data[ii+stride];
      }
      return values;
    } else if(iy<0) {
      double t = (x-left)/dx-ix;
      for(int i = 0, n = indexes.length; i<n; i++) {
        int ii = ix*stride+indexes[i];
        values[i] = (1-t)*data[ii]+t*data[ii+stride];
      }
      return values;
    }
    double t = (x-left)/dx-ix;
    double u = -(top-y)/dy-iy;
    for(int i = 0, n = indexes.length; i<n; i++) {
      int index = indexes[i];
      int ii = nx*iy*stride+ix*stride+index;
      values[i] = (1-t)*(1-u)*data[ii]+t*(1-u)*data[ii+stride]+t*u*data[ii+nx*stride+stride]+(1-t)*u*data[ii+nx*stride];
    }
    return values;
  }

  /**
   * Gets the array containing the data.
   *
   * @return the data
   */
  public double[][][] getData() {
    double[][][] data = new double[1][1][];
    data[0][0] = this.data;
    return data;
  }

  /**
   * Gets the minimum and maximum values of the n-th component.
   *
   * @param n the component
   * @return {zmin,zmax}
   */
  public double[] getZRange(int n) {
    double zmin = data[n];
    double zmax = zmin;
    for(int j = 0; j<ny; j++) {
      int index = j*nx+n;
      for(int i = 0; n<nx; i++) {
        double v = data[index+i];
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
    return(data==null) ? Double.NaN : left+dx*i;
  }

  /**
 * Gets the y coordinate for the given index.
 *
 * @param i int
 * @return double the y coordinate
 */
  public double indexToY(int i) {
    return(data==null) ? Double.NaN : top+dy*i;
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
      FlatData gpd = (FlatData) obj;
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
      return new FlatData(1, 1, 1);
    }

    public Object loadObject(XMLControl control, Object obj) {
      FlatData gpd = (FlatData) obj;
      double[] data = (double[]) control.getObject("data"); //$NON-NLS-1$
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
