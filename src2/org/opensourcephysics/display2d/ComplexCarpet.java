/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
public class ComplexCarpet extends ComplexInterpolatedPlot {
  /**
   * Constructor ComplexCarpet
   * @param griddata
   */
  public ComplexCarpet(GridData griddata) {
    super(griddata);
    setShowGridLines(false);
  }

  public void clearData() {
    if(griddata instanceof ArrayData) {
      double[][][] data = griddata.getData();
      for(int ix = 0, nx = griddata.getNx(); ix<nx; ix++) {
        for(int iy = 0, ny = griddata.getNy(); iy<ny; iy++) {
          data[0][ix][iy] = 0;
          data[1][ix][iy] = 0;
          data[2][ix][iy] = 0;
        }
      }
    } else { // point data
      double[][][] data = griddata.getData();
      for(int ix = 0, nx = griddata.getNx(); ix<nx; ix++) {
        for(int iy = 0, ny = griddata.getNy(); iy<ny; iy++) {
          data[nx][ny][2] = 0;
          data[nx][ny][3] = 0;
          data[nx][ny][4] = 0;
        }
      }
    }
    update();
  }

  public void setTopRow(double[][] line) {
    if(image==null) {
      return;
    }
    if(rgbData[0].length!=image.getWidth()*image.getHeight()) {
      return;
    }
    if(griddata instanceof ArrayData) { // replace top row of array
      for(int c = 0; c<line.length; c++) {
        double[][] data = griddata.getData()[c];
        int len = data[0].length-1;
        for(int ix = 0, nx = data.length; ix<nx; ix++) {
          System.arraycopy(data[ix], 0, data[ix], 1, len);
          data[ix][0] = line[c][ix];
        }
      }
    } else {
      double[][][] data = griddata.getData();
      for(int ix = 0, nx = data.length; ix<nx; ix++) {
        int len = line.length;
        for(int ny = data[0].length-1, iy = ny; iy>0; iy--) {
          System.arraycopy(data[ix][iy-1], 2, data[ix][iy], 2, len);
        }
        for(int c = 0; c<len; c++) {
          data[ix][0][2+c] = line[c][ix];
        }
      }
    }
    double dy = (griddata.getBottom()-griddata.getTop())/(image.getHeight()-1);
    int nr = 1+(int) Math.abs(griddata.getDy()/dy);
    int offset = nr*image.getWidth();
    int length = rgbData[0].length-offset;
    System.arraycopy(rgbData[0], 0, rgbData[0], offset, length);
    System.arraycopy(rgbData[1], 0, rgbData[1], offset, length);
    System.arraycopy(rgbData[2], 0, rgbData[2], offset, length);
    double[] samples = new double[3];
    int[] indexes = new int[] {ampIndex, reIndex, imIndex};
    byte[] rgb = new byte[3];
    double y = griddata.getTop();
    double dx = (griddata.getRight()-griddata.getLeft())/(image.getWidth()-1);
    for(int j = 0, my = image.getHeight(); j<nr; j++) {
      double x = griddata.getLeft();
      for(int i = 0, mx = image.getWidth(); i<mx; i++) {
        colorMap.samplesToComponents(griddata.interpolate(x, y, indexes, samples), rgb);
        int index = (dy<0) ? j*mx+i : (my-j-1)*mx+i;
        rgbData[0][index] = rgb[0]; // red
        rgbData[1][index] = rgb[1]; // green
        rgbData[2][index] = rgb[2]; // blue
        x += dx;
      }
      y += dy;
    }
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    setAutoscaleZ(isAutoscale, ceil);
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
