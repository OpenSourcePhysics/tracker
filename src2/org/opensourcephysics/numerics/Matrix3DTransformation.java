/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * Matrix3DTransformation implements 3D affine transformations using a matrix representation.
 */
public class Matrix3DTransformation implements MatrixTransformation {
  protected double[] origin = new double[3];      // origin for this rotation
  protected double[][] matrix = new double[3][3]; // the transformation matrix
  protected double[][] inverseMatrix = null;      // the inverse transformation matrix if it exists

  /**
   * Constructs a 3D transformation using the given matrix.
   *
   * Affine transformations can be applied to 3D coordinates.
   * A 3 by 3 matrix sets the rotation and shear.
   * A null matrix sets the transformation to the identity transformation.
   *
   * @param matrix double[][]
   */
  public Matrix3DTransformation(double[][] matrix) {
    if(matrix==null) { // identiy matrix
      this.matrix[0][0] = this.matrix[1][1] = this.matrix[2][2] = 1;
      return;
    }
    for(int i = 0; i<matrix.length; i++) { // loop over the rows
      System.arraycopy(matrix[i], 0, this.matrix[i], 0, matrix[i].length);
    }
  }

  /**
   * Creates a 3D transforamtion representing a rotation about the x axis by the given angle.
   *
   * @param theta double
   *
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation rotationX(double theta) {
    // same as Rotation(theta, new double[]{0, 0, 1});
    Matrix3DTransformation at = new Matrix3DTransformation(null); // creates matrix
    double[][] mat = at.matrix;
    double sin = Math.sin(theta);
    double cos = Math.cos(theta);
    // matrix elements not listed are zero
    mat[0][0] = 1;
    mat[1][1] = cos;
    mat[1][2] = -sin;
    mat[2][1] = sin;
    mat[2][2] = cos;
    // inverse matrix is null but we know what it is so create it
    double[][] inv = new double[3][3];
    at.inverseMatrix = inv;
    inv[0][0] = 1;
    inv[1][1] = cos;
    inv[1][2] = sin;
    inv[2][1] = -sin;
    inv[2][2] = cos;
    return at;
  }

  /**
   * Creates a 3D transforamtion representing a rotation about the y axis by the given angle.
   *
   * @param theta double
   *
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation rotationY(double theta) {
    // same as Rotation(theta, new double[]{0, 0, 1});
    Matrix3DTransformation at = new Matrix3DTransformation(null); // creates matrix
    double[][] mat = at.matrix;
    double sin = Math.sin(theta);
    double cos = Math.cos(theta);
    // matrix elements not listed are zero
    mat[1][1] = 1;
    mat[0][0] = cos;
    mat[0][2] = sin;
    mat[2][0] = -sin;
    mat[2][2] = cos;
    // inverse matrix is null but we know what it is so create it
    double[][] inv = new double[3][3];
    at.inverseMatrix = inv;
    // matrix elements not listed are zero
    inv[1][1] = 1;
    inv[0][0] = cos;
    inv[0][2] = -sin;
    inv[2][0] = sin;
    inv[2][2] = cos;
    return at;
  }

  /**
   * Creates a 3D transforamtion representing a rotation about the z axis by the given angle.
   *
   * @param theta double
   *
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation rotationZ(double theta) {
    // same as Rotation(theta, new double[]{0, 0, 1});
    Matrix3DTransformation at = new Matrix3DTransformation(null); // creates matrix
    double[][] mat = at.matrix;
    double sin = Math.sin(theta);
    double cos = Math.cos(theta);
    // matrix elements not listed are zero
    mat[0][0] = cos;
    mat[0][1] = -sin;
    mat[1][0] = sin;
    mat[1][1] = cos;
    mat[2][2] = 1;
    // inverse matrix is null but we know what it is so create it
    double[][] inv = new double[3][3];
    at.inverseMatrix = inv;
    inv[0][0] = cos;
    inv[0][1] = sin;
    inv[1][0] = -sin;
    inv[1][1] = cos;
    inv[2][2] = 1;
    return at;
  }

  /**
   * Creates a 3D transforamtion representing a rotation about the origin by the given angle around
   * the given axis.
   *
   * @param theta double
   * @param axis double[]
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation rotation(double theta, double[] axis) {
    Matrix3DTransformation at = new Matrix3DTransformation(null); // creates unit matrix
    double[][] mat = at.matrix;
    double x = axis[0], y = axis[1], z = axis[2];
    double norm = x*x+y*y+z*z;
    if(norm!=1) { // this usually doesn't happen because of roundoff but is worth a try
      norm = 1/Math.sqrt(norm);
      x *= norm;
      y *= norm;
      z *= norm;
    }
    double c = Math.cos(theta), s = Math.sin(theta);
    double t = 1-c;
    // matrix elements not listed are zero
    mat[0][0] = t*x*x+c;
    mat[0][1] = t*x*y-s*z;
    mat[0][2] = t*x*z+s*y;
    mat[1][0] = t*x*y+s*z;
    mat[1][1] = t*y*y+c;
    mat[1][2] = t*y*z-s*x;
    mat[2][0] = t*x*z-s*y;
    mat[2][1] = t*y*z+s*x;
    mat[2][2] = t*z*z+c;
    // inverse matrix is null but we know what it is so create it
    double[][] inv = new double[3][3];
    at.inverseMatrix = inv;
    inv[0][0] = mat[0][0];
    inv[1][0] = mat[0][1];
    inv[2][0] = mat[0][2];
    inv[0][1] = mat[1][0];
    inv[1][1] = mat[1][1];
    inv[2][1] = mat[1][2];
    inv[0][2] = mat[2][0];
    inv[1][2] = mat[2][1];
    inv[2][2] = mat[2][2];
    return at;
  }

  /**
   * Creates an transformation representing a rotation about the origin by the given quaternion.
   *
   * @param quaternion double[]
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation Quaternion(double[] quaternion) {
    return Quaternion(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
  }

  /**
   * Creates an AffineMatrix representing a rotation about the origin by the given quaternion components.
   *
   * @param q0
   * @param q1
   * @param q2
   * @param q3
   * @return Affine3DTransformation
   */
  public static Matrix3DTransformation Quaternion(double q0, double q1, double q2, double q3) {
    Matrix3DTransformation at = new Matrix3DTransformation(null);
    double[][] atMatrix = at.matrix;
    double norm = q0*q0+q1*q1+q2*q2+q3*q3;
    if(norm!=1) {               // this usually doesn't happen because of roundoff but is worth a try
      norm = 1/Math.sqrt(norm); // computationaly expensive
      q0 *= norm;
      q1 *= norm;
      q2 *= norm;
      q3 *= norm;
    }
    double q11 = 2*q1*q1, q22 = 2*q2*q2, q33 = 2*q3*q3;
    double q12 = 2*q1*q2, q13 = 2*q1*q3, q23 = 2*q2*q3;
    double q01 = 2*q0*q1, q02 = 2*q0*q2, q03 = 2*q0*q3;
    // matrix elements not listed are zero
    atMatrix[0][0] = 1-q22-q33;
    atMatrix[0][1] = q12-q03;
    atMatrix[0][2] = q13+q02;
    atMatrix[1][0] = q12+q03;
    atMatrix[1][1] = 1-q11-q33;
    atMatrix[1][2] = q23-q01;
    atMatrix[2][0] = q13-q02;
    atMatrix[2][1] = q23+q01;
    atMatrix[2][2] = 1-q11-q22;
    // inverse matrix is null but we know what it is so create it
    double[][] inv = new double[3][3];
    at.inverseMatrix = inv;
    inv[0][0] = atMatrix[0][0];
    inv[1][0] = atMatrix[0][1];
    inv[2][0] = atMatrix[0][2];
    inv[0][1] = atMatrix[1][0];
    inv[1][1] = atMatrix[1][1];
    inv[2][1] = atMatrix[1][2];
    inv[0][2] = atMatrix[2][0];
    inv[1][2] = atMatrix[2][1];
    inv[2][2] = atMatrix[2][2];
    return at;
  }

  /**
   * Sets the double[][] matrix array of the rotation
   * @param newMatrix
   * @returns true if there was a real change
   */
  public boolean setMatrix(double[][] newMatrix) {
    if (matrix.equals(newMatrix)) return false;
    for (int i = 0; i<3; i++) { // loop over the rows
      System.arraycopy(newMatrix[i], 0, this.matrix[i], 0, 3);
    }
    this.inverseMatrix = null;
    return true;
  }

  /**
   * Sets the matrix array of the rotation
   * @param matrix a double[9] array with the matrix components 
   * in this order {m[0][0],m[0][1],m[0][2],m[1][0],...}
   * @returns true if there was a real change
   */
  public boolean setMatrix(double[] newMatrix) {
    boolean changed = false;
    for (int i = 0,j=0; i<3; i++) { // loop over the rows
      if (matrix[i][0]!=newMatrix[j++]) { matrix[i][0] = newMatrix[j]; changed = true; }
      if (matrix[i][1]!=newMatrix[j++]) { matrix[i][1] = newMatrix[j]; changed = true; }
      if (matrix[i][2]!=newMatrix[j++]) { matrix[i][2] = newMatrix[j]; changed = true; }
    }
    if (changed) this.inverseMatrix = null;
    return changed;
  }
  
  /**
   * Provides a copy of this transformation.
   */
  public Object clone() {
    Matrix3DTransformation m = new Matrix3DTransformation(matrix);
    m.origin = origin.clone();
    if(inverseMatrix==null) {
      return m;
    }
    m.inverseMatrix = new double[3][3]; // inverse exists so clone it too
    for(int i = 0; i<inverseMatrix.length; i++) { // loop over the rows
      System.arraycopy(inverseMatrix[i], 0, m.inverseMatrix[i], 0, inverseMatrix[i].length);
    }
    return m;
  }

  /**
* Gets the direct homogeneous affine transformation flattened into a 1-d array.
*
* If the mat parameter is null a double[16] array is created;
* otherwise the given array is used.
*
* @param mat double[] optional matrix
* @return double[] the matrix
*/
  public final double[] getFlatMatrix(double[] mat) {
    if(mat==null) {
      mat = new double[16];
    }
    mat[0] = matrix[0][0];
    mat[4] = matrix[0][1];
    mat[8] = matrix[0][2];
    mat[1] = matrix[1][0];
    mat[5] = matrix[1][1];
    mat[9] = matrix[1][2];
    mat[2] = matrix[2][0];
    mat[3] = 0;
    mat[6] = matrix[2][1];
    mat[7] = 0;
    mat[10] = matrix[2][2];
    mat[11] = 0;
    mat[12] = origin[0];
    mat[13] = origin[1];
    mat[14] = origin[2];
    mat[15] = 1;
    return mat;
  }

  /**
   * Gets the direct homogeneous affine transformation flattened into a 1-d array,
   * ordered left to right, top to bottom
   *
   * If the mat parameter is null a double[16] array is created;
   * otherwise the given array is used.
   *
   * @param mat double[] optional matrix
   * @return double[] the matrix
   */
  public final double[] getTransposedFlatMatrix(double[] mat) {
    if(mat==null) {
      mat = new double[16];
    }
    mat[0] = matrix[0][0];
    mat[1] = matrix[0][1];
    mat[2] = matrix[0][2];
    mat[3] = 0;
    mat[4] = matrix[1][0];
    mat[5] = matrix[1][1];
    mat[6] = matrix[1][2];
    mat[7] = 0;
    mat[8] = matrix[2][0];
    mat[9] = matrix[2][1];
    mat[10] = matrix[2][2];
    mat[11] = 0;
    mat[12] = origin[0];
    mat[13] = origin[1];
    mat[14] = origin[2];
    mat[15] = 1;
    return mat;
  }

  public final double[] toQuaternion(double[] q) {
    double kx, ky, kz;
    double kx1, ky1, kz1;
    boolean add;
    double strace = Math.sqrt(matrix[0][0]+matrix[1][1]+matrix[2][2]+1)/2.0;
    kx = matrix[2][1]-matrix[1][2];
    ky = matrix[0][2]-matrix[2][0];
    kz = matrix[1][0]-matrix[0][1];
    if((matrix[0][0]>=matrix[1][1])&&(matrix[0][0]>=matrix[2][2])) {
      kx1 = matrix[0][0]-matrix[1][1]-matrix[2][2]+1;
      ky1 = matrix[1][0]+matrix[0][1];
      kz1 = matrix[2][0]+matrix[0][2];
      add = (kx>=0);
    } else if(matrix[1][1]>=matrix[2][2]) {
      kx1 = matrix[1][0]+matrix[0][1];
      ky1 = matrix[1][1]-matrix[0][0]-matrix[2][2]+1;
      kz1 = matrix[2][1]+matrix[1][2];
      add = (ky>=0);
    } else {
      kx1 = matrix[2][0]+matrix[0][2];
      ky1 = matrix[2][1]+matrix[1][2];
      kz1 = matrix[2][2]-matrix[0][0]-matrix[1][1]+1;
      add = (kz>=0);
    }
    if(add) {
      kx = kx+kx1;
      ky = ky+ky1;
      kz = kz+kz1;
    } else {
      kx = kx-kx1;
      ky = ky-ky1;
      kz = kz-kz1;
    }
    double nm = Math.sqrt(kx*kx+ky*ky+kz*kz);
    if(nm==0) {
      q[0] = 1;
      q[1] = 0;
      q[2] = 0;
      q[3] = 0;
    } else {
      double s = Math.sqrt(1-strace*strace)/nm;
      q[0] = strace;
      q[1] = s*kx;
      q[2] = s*ky;
      q[3] = s*kz;
    }
    return q;
  }

  /**
   * Instantiates a rotation that aligns the first vector with the second vector.
   *
   * @param v1 double[]
   * @param v2 double[]
   * @return Quaternion
   */
  public static Matrix3DTransformation createAlignmentTransformation(double[] v1, double v2[]) {
    v1 = VectorMath.normalize(v1.clone());
    v2 = VectorMath.normalize(v2.clone());
    double theta = Math.acos(VectorMath.dot(v1, v2));
    double[] axis = VectorMath.cross3D(v1, v2);
    return Matrix3DTransformation.rotation(theta, axis);
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param ox double
   * @param oy double
   * @param oz double
   */
  public void setOrigin(double ox, double oy, double oz) {
    origin[0] = ox;
    origin[1] = oy;
    origin[2] = oz;
  }

  /**
   * Multiplies (concatenates) this transformation matrix with the given transformation.
   *
   * @param trans Matrix3DTransformation
   */
  public final void multiply(Matrix3DTransformation trans) {
    multiply(trans.matrix);
  }

  /**
   * Multiplies this rotation matrix by the given matrix from the right.
   *
   * @param mat double[][]
   */
  public final void multiply(double[][] mat) {
    for(int i = 0, n = matrix.length; i<n; i++) {
      double[] row = matrix[i].clone();
      for(int j = 0, m = matrix[0].length; j<m; j++) {
        matrix[i][j] = 0;
        for(int k = 0; k<m; k++) {
          matrix[i][j] += row[k]*mat[k][j];
        }
      }
    }
    inverseMatrix = null;
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param origin double[] the new origin
   * @return double[]
   */
  public double[] setOrigin(double[] origin) {
    this.origin[0] = origin[0];
    this.origin[1] = origin[1];
    this.origin[2] = origin[2];
    return origin;
  }

  /**
   * Transforms the given point+.
   *
   * @param point the coordinates to be transformed
   */
  public double[] direct(double[] point) {
    point[0] -= origin[0];
    point[1] -= origin[1];
    point[2] -= origin[2];
    double[] tempPoint = point.clone();
    for(int i = 0, n = point.length; i<n; i++) {
      point[i] = origin[i];
      for(int j = 0; j<n; j++) {
        point[i] += matrix[i][j]*tempPoint[j];
      }
    }
    return point;
  }

  /**
   * Transforms the given matrix into the transformation's coordinate system.
   *
   * @param matrix to be transformed
   */
  public double[][] direct(double[][] mat) {
    if(inverseMatrix==null) {
      calcInverse();                                                                   // computes the inverse using LU decompostion
      if(inverseMatrix==null) {                                                        // inverse does not exist
        throw new UnsupportedOperationException("The inverse matrix does not exist."); //$NON-NLS-1$
      }
    }
    // multiple by inverse matrix from right
    for(int i = 0; i<3; i++) {     // row index
      double[] row = mat[i].clone();
      for(int j = 0; j<3; j++) {   // col index
        mat[i][j] = 0;
        for(int k = 0; k<3; k++) { // sum index
          mat[i][j] += row[k]*inverseMatrix[k][j];
        }
      }
    }
    // multiple by direct matrix from left
    for(int j = 0; j<3; j++) {     // col index
      double[] col = new double[] {mat[0][j], mat[1][j], mat[2][j]};
      for(int i = 0; i<3; i++) {   // row index
        mat[i][j] = 0;
        for(int k = 0; k<3; k++) { // sum index
          mat[i][j] += matrix[i][k]*col[k];
        }
      }
    }
    return mat;
  }

  /**
   * Transforms the given point using the inverse transformation (if it exists).
   *
   * If the transformation is not invertible, then a call to this
   * method must throw a UnsupportedOperationException exception.
   *
   * @param point the coordinates to be transformed
   */
  public double[] inverse(double[] point) throws UnsupportedOperationException {
    if(inverseMatrix==null) {
      calcInverse();                                                                   // computes the inverse using LU decompostion
      if(inverseMatrix==null) {                                                        // inverse does not exist
        throw new UnsupportedOperationException("The inverse matrix does not exist."); //$NON-NLS-1$
      }
    }
    point[0] -= origin[0];
    point[1] -= origin[1];
    point[2] -= origin[2];
    double[] tempPoint = point.clone();
    for(int i = 0, n = point.length; i<n; i++) {
      point[i] = origin[i];
      for(int j = 0; j<n; j++) {
        point[i] += inverseMatrix[i][j]*tempPoint[j];
      }
    }
    return point;
  }

  /**
 * Transforms the given matrix from the transformation's coordinate system.
 *
 * @param matrix to be transformed
 */
  public double[][] inverse(double[][] mat) {
    if(inverseMatrix==null) {
      calcInverse();                                                                   // computes the inverse using LU decompostion
      if(inverseMatrix==null) {                                                        // inverse does not exist
        throw new UnsupportedOperationException("The inverse matrix does not exist."); //$NON-NLS-1$
      }
    }
    // multiple by direct matrix from right
    for(int i = 0; i<3; i++) {     // row index
      double[] row = mat[i].clone();
      for(int j = 0; j<3; j++) {   // col index
        mat[i][j] = 0;
        for(int k = 0; k<3; k++) { // sum index
          mat[i][j] += row[k]*matrix[k][j];
        }
      }
    }
    // multiple by inverse matrix from left
    for(int j = 0; j<3; j++) {     // col index
      double[] col = new double[] {mat[0][j], mat[1][j], mat[2][j]};
      for(int i = 0; i<3; i++) {   // row index
        mat[i][j] = 0;
        for(int k = 0; k<3; k++) { // sum index
          mat[i][j] += inverseMatrix[i][k]*col[k];
        }
      }
    }
    return mat;
  }
  
  public double[] getOrigin(){return this.origin;}
    
 
  private void calcInverse() {
    LUPDecomposition lupd = new LUPDecomposition(matrix);
    inverseMatrix = lupd.inverseMatrixComponents();
  }

  public static XML.ObjectLoader getLoader() {
    return new Affine3DTransformationLoader();
  }

  protected static class Affine3DTransformationLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Matrix3DTransformation transf = (Matrix3DTransformation) obj;
      control.setValue("matrix", transf.matrix); //$NON-NLS-1$
      if(transf.inverseMatrix!=null) {
        control.setValue("inverse", transf.inverseMatrix); //$NON-NLS-1$
      }
      control.setValue("origin", transf.origin); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Matrix3DTransformation(null);
    }

    public Object loadObject(XMLControl control, Object obj) {
      Matrix3DTransformation transf = (Matrix3DTransformation) obj;
      transf.matrix = (double[][]) control.getObject("matrix");         //$NON-NLS-1$
      transf.inverseMatrix = (double[][]) control.getObject("inverse"); //$NON-NLS-1$
      transf.origin = (double[]) control.getObject("origin");           //$NON-NLS-1$
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
