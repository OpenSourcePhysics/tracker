/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d.utils;
public class CylinderUtils extends ShapeUtils {
  static public double[][][] createStandardCylinder(int nr, int nu, int nz, double angle1, double angle2, boolean top, boolean bottom, boolean left, boolean right) {
    int totalN = nu*nz;
    if(bottom) {
      totalN += nr*nu;
    }
    if(top) {
      totalN += nr*nu;
    }
    if(Math.abs(angle2-angle1)<360) {
      if(left) {
        totalN += nr*nz;
      }
      if(right) {
        totalN += nr*nz;
      }
    }
    double[][][] data = new double[totalN][4][3];
    // Compute sines and cosines
    double[] cosu = new double[nu+1], sinu = new double[nu+1];
    for(int u = 0; u<=nu; u++) {   // compute sines and cosines
      double angle = ((nu-u)*angle1+u*angle2)*TO_RADIANS/nu;
      cosu[u] = Math.cos(angle)/2; // The /2 is because the element is centered
      sinu[u] = Math.sin(angle)/2;
    }
    // Now compute the tiles
    int tile = 0;
    double[] center = new double[] {-vectorz[0]/2, -vectorz[1]/2, -vectorz[2]/2};
    {                                       // Tiles along the z axis
      double aux = 1.0/nz;
      for(int j = 0; j<nz; j++) {
        for(int u = 0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
          for(int k = 0; k<3; k++) {
            data[tile][0][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+j*aux*vectorz[k];
            data[tile][1][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+j*aux*vectorz[k];
            data[tile][2][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+(j+1)*aux*vectorz[k];
            data[tile][3][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+(j+1)*aux*vectorz[k];
          }
        }
      }
    }
    if(bottom) {                                                            // Tiles at bottom
      //int ref = 0;                                                    // not used
      for(int u = 0; u<nu; u++) {
        for(int i = 0; i<nr; i++, tile++) {
          for(int k = 0; k<3; k++) {
            data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr;       // should be ref+u
            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
            data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr;       // should be ref+u
          }
        }
      }
    }
    if(top) { // Tiles at top
      int ref = nu*(nz-1);
      center[0] = vectorz[0];
      center[1] = vectorz[1];
      center[2] = vectorz[2]-0.5;
      for(int u = 0; u<nu; u++) {
        for(int i = 0; i<nr; i++, tile++) {
          for(int k = 0; k<3; k++) {
            data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
            data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
          }
        }
      }
    }
    if(Math.abs(angle2-angle1)<360) { // No need to close left or right if the Cylinder is 'round' enough
      center[0] = -vectorz[0]/2;
      center[1] = -vectorz[1]/2;
      center[2] = -vectorz[2]/2;
      if(right) {                     // Tiles at right
        double aux = 1.0/nz;
        for(int j = 0; j<nz; j++) {
          for(int i = 0; i<nr; i++, tile++) {
            for(int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+j*aux*vectorz[k];
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+j*aux*vectorz[k];
              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
              data[tile][3][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
            }
          }
        }
      }
      if(left) {                      // Tiles at left
        double aux = 1.0/nz;
        int ref = nu-1;
        for(int j = 0; j<nz; j++) {
          for(int i = 0; i<nr; i++, tile++) {
            for(int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+j*aux*vectorz[k];
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+j*aux*vectorz[k];
              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
              data[tile][3][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
            }
          }
        }
      }
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
