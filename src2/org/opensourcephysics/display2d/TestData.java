/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;

/**
 * Title:        TestData
 * Description:  Static methods to generate test data for various programs.
 * Copyright:    Copyright (c) 2005  Gould, Christian, and Tobochnik
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class TestData {
  /**
   * Dipole distribution with poles at +1 and -1 on the x axis.
   */
  public static double dipole(double x, double y) {
    double r1 = Math.sqrt((x-1)*(x-1)+y*y);
    double r2 = Math.sqrt((x+1)*(x+1)+y*y);
    if((r1==0)||(r2==0)) {
      return 0.0; // potential is undefined at r=0.
    }
    return 1/r1-1/r2;
  }

  /**
   * A 2D Gaussian distribution function centered at x=0 and y=0 and standard deviation of 1.
   */
  public static double gaussian(double x, double y, double sigma) {
    double rSqr = x*x+y*y;
    return Math.exp(-rSqr/sigma/sigma/2);
  }

  /**
   * Saddle function centered at x=0 and y=0.
   */
  public static double saddle(double x, double y) {
    double rSqr = x*x-y*y;
    return Math.exp(rSqr);
  }

  /**
   * Generate a dipole scalar field.
   */
  public static double[][][] dipoleScalarField(GridPointData pointdata) {
    double[][][] data = pointdata.getData();
    for(int i = 0, mx = data.length; i<mx; i++) {
      for(int j = 0, my = data[0].length; j<my; j++) {
        data[i][j][2] = dipole(data[i][j][0], data[i][j][1]);
      }
    }
    return data;
  }

  /**
   * Generate a gaussian scalar field.
   */
  public static double[][][] gaussianScalarField(GridPointData pointdata) {
    double[][][] data = pointdata.getData();
    for(int i = 0, mx = data.length; i<mx; i++) {
      for(int j = 0, my = data[0].length; j<my; j++) {
        data[i][j][2] = gaussian(data[i][j][0], data[i][j][1], 1);
      }
    }
    return data;
  }

  /**
   * Generate a random scalar field.
   */
  public static void randomScalarField(GridPointData pointdata) {
    double[][][] data = pointdata.getData();
    for(int i = 0, mx = data.length; i<mx; i++) {
      for(int j = 0, my = data[0].length; j<my; j++) {
        data[i][j][2] = Math.random();
      }
    }
  }

  /**
   * Dipole field with poles at +1 and -1 on the x axis.  Field strength is
   * proportional to 1/r.
   */
  public static double[] dipoleVector(double x, double y) {
    double[] vec = new double[2];
    double r1 = Math.sqrt((x-1)*(x-1)+y*y);
    double r2 = Math.sqrt((x+1)*(x+1)+y*y);
    if((r1==0)||(r2==0)) {
      return vec; // potential is undefined at r=0.
    }
    vec[0] = (x-1)/r1/r1-(x+1)/r2/r2;
    vec[1] = y/r1/r1-y/r2/r2;
    return vec;
  }

  public static double[][][] circulatingVectorField(int nx, int ny) {
    double[][][] data = new double[nx][ny][5]; // x, y, horizontal, vertical, color
    double xmin = -10, xmax = 10;
    double ymin = -10, ymax = 10;
    double x = xmin, y = ymin;
    double stepx = (xmax-xmin)/(nx-1);
    double stepy = (ymax-ymin)/(ny-1);
    for(int i = 0; i<nx; i++) {
      for(int j = 0; j<ny; j++) {
        data[i][j][0] = x;    // x location of field calculation
        data[i][j][1] = y;    // y location of field calculation
        double r = Math.sqrt(x*x+y*y);
        data[i][j][2] = -y/r; // x length
        data[i][j][3] = x/r;  // y length
        data[i][j][4] = r;    // magnitude of the field
        y += stepy;
      }
      y = ymin;
      x += stepx;
    }
    return data;
  }

  public static double[][][] dipoleVectorField(int nx, int ny, double xmin, double xmax, double ymin, double ymax) {
    double[][][] data = new double[nx][ny][5]; // x, y, horizontal, vertical, magnitude
    double x = xmin, y = ymin;
    double stepx = (xmax-xmin)/(nx-1);
    double stepy = (ymax-ymin)/(ny-1);
    for(int i = 0; i<nx; i++) {
      for(int j = 0; j<ny; j++) {
        data[i][j][0] = x;          // x location of field calculation
        data[i][j][1] = y;          // y location of field calculation
        double[] vec = dipoleVector(x, y);
        double mag = Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]);
        data[i][j][2] = vec[0]/mag; // x direction cosine
        data[i][j][3] = vec[1]/mag; // y direction cosine
        data[i][j][4] = mag;        // magnitude of the field
        y += stepy;
      }
      y = ymin;
      x += stepx;
    }
    return data;
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
