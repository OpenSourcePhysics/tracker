/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d.utils;
public class TetrahedronUtils extends ShapeUtils {
  //Static varibles for tetrahedron
  private static final double SQRT3 = Math.sqrt(3.0);
  private static final double HEIGHT = Math.sqrt(6.0)/3.0f;
  private static final double XCENTER = SQRT3/6.0f;
  private static final double ZCENTER = HEIGHT/3.0f;

  //Tetrahedron
  static public double[][][] createStandardTetrahedron(boolean top, boolean bottom, double height) {
    int totalN = 4;  //number of tiles
    int pointsN = 4; //number of points
    if(!Double.isNaN(height)) {
      pointsN += 2;
      if(top&&bottom) {
        totalN = 5;
      } else if(!top&&bottom) {
        totalN = 4;
      } else if(!top&&!bottom) {
        totalN = 3;
      } else {
        totalN = 4;
      }
    }
    if(!bottom&&Double.isNaN(height)) {
      totalN = 3;
    }
    double[][][] data = new double[totalN][4][3];
    double[][] points = new double[pointsN][3];
    //Base points
    points[0][0] = XCENTER;
    points[0][1] = 0.5f;
    points[0][2] = -ZCENTER; //p1
    points[1][0] = XCENTER;
    points[1][1] = -0.5f;
    points[1][2] = -ZCENTER; //p2
    points[2][0] = -XCENTER*2.0f;
    points[2][1] = 0.0f;
    points[2][2] = -ZCENTER; //p3
    if(Double.isNaN(height)) {
      points[3][0] = 0.0f;
      points[3][1] = 0.0f;
      points[3][2] = HEIGHT-ZCENTER; //p4
      if(bottom) {
        int[] serie = {0, 1, 3, 3, 0, 3, 2, 2, 1, 2, 3, 3, 0, 2, 1, 1};
        /*p1, p2, p4, p4   // front face
            p1, p4, p3, p3     // left, back face
            p2, p3, p4, p4     // right, back face
            p1, p3, p2, p2     // bottom face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      } else {
        int[] serie = {0, 1, 3, 3, 0, 3, 2, 2, 1, 2, 3, 3};
        /*p1, p2, p4, p4  // front face
            p1, p4, p3, p3    // left, back face
            p2, p3, p4, p4    // right, back face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
    }
    if(!Double.isNaN(height)) {
      points[3][0] = XCENTER*(1-height);
      points[3][1] = 0.5f-0.5f*height;
      points[3][2] = HEIGHT*height-ZCENTER; //p4
      points[4][0] = XCENTER*(1-height);
      points[4][1] = -0.5f+0.5f*height;
      points[4][2] = HEIGHT*height-ZCENTER; //p5
      points[5][0] = -XCENTER*2.0f*(1-height);
      points[5][1] = 0.0f;
      points[5][2] = HEIGHT*height-ZCENTER; //p6
      if(top&&bottom) {
        int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 0, 1, 2, 2, 3, 5, 4, 4};
        /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face
            p1, p2, p3, p3,      // bottom face
          p4, p6, p5, p5,     // top face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
      if(!top&&bottom) {
        int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 0, 1, 2, 2};
        /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face
            p1, p2, p3, p3,      // bottom face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
      if(!top&&!bottom) {
        int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2};
        /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
      if(top&&!bottom) {
        int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 3, 5, 4, 4};
        /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face
          p4, p6, p5, p5,     // top face*/
        for(int i = 0; i<totalN; i++) {
          for(int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
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
