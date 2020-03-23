/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;

/**
 * Utility class for two dimensional visualizations.
 *
 * @author     Wolfgang Christian
 * @created    Feb 20, 2003
 * @version    1.0
 */
public class Util2D {
  private Util2D() { // prohibit instantiation
  }

  /**
   * Calculates the Laplacian of a 2d scalar field.
   * @param input
   * @param multiplier
   * @return GridPointData
   */
  public static GridPointData laplacian(GridPointData input, double multiplier) {
    //   algorithm to be written
    /*
      double[][][] indata = input.getData();
      int nr = indata.length;
      int nc = indata[0].length;
      GridPointData output = input.createGridPointData(1);
      output.left = input.left;
      output.right = input.right;
      output.top = input.top;
      output.bottom = input.bottom;
      output.dx = input.dx;
      output.dy = input.dy;
      double[][][] outdata = output.getData();
      double dx2 = 2*input.dx/multiplier;
      double dy2 = 2*input.dy/multiplier;
      double dudx, dudy;
      return output; */
    return null;
  }

  /**
   * Calculates the divergence of a 2d vector field and multiplies the divergence by a constant.
   * @param input
   * @param multiplier
   * @return GridPointData
   */
  public static GridPointData divergence(GridPointData input, double multiplier) {
    double[][][] indata = input.getData();
    int nx = indata.length;
    int ny = indata[0].length;
    GridPointData output = input.createGridPointData(1);
    output.left = input.left;
    output.right = input.right;
    output.top = input.top;
    output.bottom = input.bottom;
    output.dx = input.dx;
    output.dy = input.dy;
    double[][][] outdata = output.getData();
    double dx2 = 2*input.dx/multiplier;
    double dy2 = 2*input.dy/multiplier;
    double dudx, dudy;
    for(int i = 1; i<nx-1; i++) {
      for(int j = 1; j<ny-1; j++) {
        dudx = (indata[i+1][j][2]-indata[i-1][j][2])/dx2;
        dudy = (indata[i][j+1][2]-indata[i][j-1][2])/dy2;
        outdata[i][j][0] = indata[i][j][0];
        outdata[i][j][1] = indata[i][j][1];
        outdata[i][j][2] = dudx+dudy;
      }
    }
    // top row
    for(int i = 1; i<nx-1; i++) {
      dudx = (indata[i+1][0][2]-indata[i-1][0][2])/dx2;
      // 2d order forward difference
      dudy = (-3*indata[i][0][2]+4*indata[i][1][2]-indata[i][2][2])/dy2;
      outdata[i][0][2] = dudx+dudy;
    }
    // bottom row
    int my = ny-1;
    for(int i = 1; i<nx-1; i++) {
      dudx = (indata[i+1][my][2]-indata[i-1][my][2])/dx2;
      // 2d order barkward difference
      dudy = (+3*indata[i][my][2]-4*indata[i][my-1][2]+indata[i][my-2][2])/dy2;
      outdata[i][my][2] = dudx+dudy;
    }
    // left column
    for(int j = 1; j<ny-1; j++) {
      // 2d order forward difference
      dudx = (-3*indata[0][j][2]+4*indata[1][j][2]-indata[2][j][2])/dx2;
      dudy = (indata[0][j+1][2]-indata[0][j-1][2])/dy2;
      outdata[0][j][2] = dudx+dudy;
    }
    // right column
    int mx = nx-1;
    for(int j = 1; j<ny-1; j++) {
      // 2d order backward difference
      dudx = (+3*indata[mx][j][2]-4*indata[mx-1][j][2]+indata[mx-2][j][2])/dx2;
      dudy = (indata[mx][j+1][2]-indata[mx][j-1][2])/dy2;
      outdata[mx][j][2] = dudx+dudy;
    }
    // left top corner
    dudx = (outdata[1][0][2]*outdata[1][0][3]+outdata[0][1][2]*outdata[0][1][3])/2;
    dudy = (outdata[1][0][2]*outdata[1][0][4]+outdata[0][1][2]*outdata[0][1][4])/2;
    outdata[0][0][2] = dudx+dudy;
    // left bottom corner
    dudx = (outdata[0][my-1][2]*outdata[0][my-1][3]+outdata[1][my][2]*outdata[1][my][3])/2;
    dudy = (outdata[0][my-1][2]*outdata[0][my-1][4]+outdata[1][my][2]*outdata[1][my][4])/2;
    outdata[0][my][2] = dudx+dudy;
    // right top corner
    dudx = (outdata[mx][1][2]*outdata[mx][1][3]+outdata[mx-1][0][2]*outdata[mx-1][0][3])/2;
    dudy = (outdata[mx][1][2]*outdata[mx][1][4]+outdata[mx-1][0][2]*outdata[mx-1][0][4])/2;
    outdata[mx][0][2] = dudx+dudy;
    // right bottom corner
    dudx = (outdata[mx][my-1][2]*outdata[mx][my-1][3]+outdata[mx-1][my][2]*outdata[mx-1][my][3])/2;
    dudy = (outdata[mx][my-1][2]*outdata[mx][my-1][4]+outdata[mx-1][my][2]*outdata[mx-1][my][4])/2;
    outdata[mx][my][2] = dudx+dudy;
    return output;
  }

  /**
   * Calculates the gradient of a 2d scalar field and multiplies the gradient by a constant.
   * @param input
   * @param multiplier
   * @return GridPointData
   */
  public static GridPointData gradient(GridPointData input, double multiplier) {
    double[][][] indata = input.getData();
    int nx = indata.length;
    int ny = indata[0].length;
    GridPointData output = input.createGridPointData(3);
    output.left = input.left;
    output.right = input.right;
    output.top = input.top;
    output.bottom = input.bottom;
    output.dx = input.dx;
    output.dy = input.dy;
    double[][][] outdata = output.getData();
    double dx2 = 2*input.dx/multiplier;
    double dy2 = 2*input.dy/multiplier;
    double dudx, dudy, mag;
    for(int i = 1; i<nx-1; i++) {
      for(int j = 1; j<ny-1; j++) {
        dudx = (indata[i+1][j][2]-indata[i-1][j][2])/dx2;
        dudy = (indata[i][j+1][2]-indata[i][j-1][2])/dy2;
        mag = Math.sqrt(dudx*dudx+dudy*dudy);
        outdata[i][j][0] = indata[i][j][0];
        outdata[i][j][1] = indata[i][j][1];
        outdata[i][j][2] = mag;
        outdata[i][j][3] = dudx/mag;
        outdata[i][j][4] = dudy/mag;
      }
    }
    // top row
    for(int i = 1; i<nx-1; i++) {
      dudx = (indata[i+1][0][2]-indata[i-1][0][2])/dx2;
      // 2d order forward difference
      dudy = (-3*indata[i][0][2]+4*indata[i][1][2]-indata[i][2][2])/dy2;
      mag = Math.sqrt(dudx*dudx+dudy*dudy);
      outdata[i][0][2] = mag;
      outdata[i][0][3] = dudx/mag;
      outdata[i][0][4] = dudy/mag;
    }
    // bottom row
    int my = ny-1;
    for(int i = 1; i<nx-1; i++) {
      dudx = (indata[i+1][my][2]-indata[i-1][my][2])/dx2;
      // 2d order barkward difference
      dudy = (+3*indata[i][my][2]-4*indata[i][my-1][2]+indata[i][my-2][2])/dy2;
      mag = Math.sqrt(dudx*dudx+dudy*dudy);
      outdata[i][my][2] = mag;
      outdata[i][my][3] = dudx/mag;
      outdata[i][my][4] = dudy/mag;
    }
    // left column
    for(int j = 1; j<ny-1; j++) {
      // 2d order forward difference
      dudx = (-3*indata[0][j][2]+4*indata[1][j][2]-indata[2][j][2])/dx2;
      dudy = (indata[0][j+1][2]-indata[0][j-1][2])/dy2;
      mag = Math.sqrt(dudx*dudx+dudy*dudy);
      outdata[0][j][2] = mag;
      outdata[0][j][3] = dudx/mag;
      outdata[0][j][4] = dudy/mag;
    }
    // right column
    int mx = nx-1;
    for(int j = 1; j<ny-1; j++) {
      // 2d order backward difference
      dudx = (+3*indata[mx][j][2]-4*indata[mx-1][j][2]+indata[mx-2][j][2])/dx2;
      dudy = (indata[mx][j+1][2]-indata[mx][j-1][2])/dy2;
      mag = Math.sqrt(dudx*dudx+dudy*dudy);
      outdata[mx][j][2] = mag;
      outdata[mx][j][3] = dudx/mag;
      outdata[mx][j][4] = dudy/mag;
    }
    // left top corner
    dudx = (outdata[1][0][2]*outdata[1][0][3]+outdata[0][1][2]*outdata[0][1][3])/2;
    dudy = (outdata[1][0][2]*outdata[1][0][4]+outdata[0][1][2]*outdata[0][1][4])/2;
    mag = Math.sqrt(dudx*dudx+dudy*dudy);
    outdata[0][0][2] = mag;
    outdata[0][0][3] = dudx/mag;
    outdata[0][0][4] = dudy/mag;
    // left bottom corner
    dudx = (outdata[0][my-1][2]*outdata[0][my-1][3]+outdata[1][my][2]*outdata[1][my][3])/2;
    dudy = (outdata[0][my-1][2]*outdata[0][my-1][4]+outdata[1][my][2]*outdata[1][my][4])/2;
    mag = Math.sqrt(dudx*dudx+dudy*dudy);
    outdata[0][my][2] = mag;
    outdata[0][my][3] = dudx/mag;
    outdata[0][my][4] = dudy/mag;
    // right top corner
    dudx = (outdata[mx][1][2]*outdata[mx][1][3]+outdata[mx-1][0][2]*outdata[mx-1][0][3])/2;
    dudy = (outdata[mx][1][2]*outdata[mx][1][4]+outdata[mx-1][0][2]*outdata[mx-1][0][4])/2;
    mag = Math.sqrt(dudx*dudx+dudy*dudy);
    outdata[mx][0][2] = mag;
    outdata[mx][0][3] = dudx/mag;
    outdata[mx][0][4] = dudy/mag;
    // right bottom corner
    dudx = (outdata[mx][my-1][2]*outdata[mx][my-1][3]+outdata[mx-1][my][2]*outdata[mx-1][my][3])/2;
    dudy = (outdata[mx][my-1][2]*outdata[mx][my-1][4]+outdata[mx-1][my][2]*outdata[mx-1][my][4])/2;
    mag = Math.sqrt(dudx*dudx+dudy*dudy);
    outdata[mx][my][2] = mag;
    outdata[mx][my][3] = dudx/mag;
    outdata[mx][my][4] = dudy/mag;
    return output;
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
