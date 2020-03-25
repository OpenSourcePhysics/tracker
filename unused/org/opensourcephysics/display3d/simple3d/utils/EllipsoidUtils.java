/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d.utils;
public class EllipsoidUtils extends ShapeUtils {
  static public double[][][] createStandardEllipsoid(int nr, int nu, int nv, double angleu1, double angleu2, double anglev1, double anglev2, boolean top, boolean bottom, boolean left, boolean right) {
    int totalN = nu*nv;
    if(Math.abs(anglev2-anglev1)<180) {
      if(bottom) {
        totalN += nr*nu;
      }
      if(top) {
        totalN += nr*nu;
      }
    }
    if(Math.abs(angleu2-angleu1)<360) {
      if(left) {
        totalN += nr*nv;
      }
      if(right) {
        totalN += nr*nv;
      }
    }
    double[][][] data = new double[totalN][4][3];
    // Compute sines and cosines
    double[] cosu = new double[nu+1], sinu = new double[nu+1];
    double[] cosv = new double[nv+1], sinv = new double[nv+1];
    for(int u = 0; u<=nu; u++) {
      double angle = ((nu-u)*angleu1+u*angleu2)*TO_RADIANS/nu;
      cosu[u] = Math.cos(angle);
      sinu[u] = Math.sin(angle);
    }
    for(int v = 0; v<=nv; v++) {
      double angle = ((nv-v)*anglev1+v*anglev2)*TO_RADIANS/nv;
      cosv[v] = Math.cos(angle)/2; // /2 because the size is the diameter
      sinv[v] = Math.sin(angle)/2;
    }
    // Now compute the tiles
    int tile = 0;
    double[] center = new double[] {0, 0, 0};
    {                                       // Tiles along the z axis
      for(int v = 0; v<nv; v++) {
        for(int u = 0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
          for(int k = 0; k<3; k++) {
            data[tile][0][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
            data[tile][1][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
            data[tile][2][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
            data[tile][3][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
          }
        }
      }
    }
    // Note : the computations below are valid only for the given vectorx, vectory and vectorz
    if(Math.abs(anglev2-anglev1)<180) {                                       // No need to close top or bottom is the sphere is 'round' enough
      if(bottom) {                                                            // Tiles at bottom
        center[2] = sinv[0];
        // int ref=0; // not used
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
      if(top) {                                                               // Tiles at top
        center[2] = sinv[nv];
        int ref = nu*(nv-1);
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
    }
    if(Math.abs(angleu2-angleu1)<360) { // No need to close left or right if the sphere is 'round' enough
      // System.out.println ("Computing lateral tiles");
      double[] nextCenter = new double[] {0, 0, 0};
      if(right) {                       // Tiles at right
        int ref = 0;
        for(int j = 0; j<nv; j++, ref += nu) {
          center[2] = sinv[j];
          nextCenter[2] = sinv[j+1];
          for(int i = 0; i<nr; i++, tile++) {
            for(int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][0][k])/nr;
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][0][k])/nr;
              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][3][k])/nr;
              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][3][k])/nr;
            }
          }
        }
      }
      if(left) {                        // Tiles at left
        int ref = nu-1;
        for(int j = 0; j<nv; j++, ref += nu) {
          center[2] = sinv[j];
          nextCenter[2] = sinv[j+1];
          for(int i = 0; i<nr; i++, tile++) {
            for(int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr;
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr;
              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][2][k])/nr;
              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][2][k])/nr;
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
