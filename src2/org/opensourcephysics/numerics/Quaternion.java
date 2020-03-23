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
 * Quaternion models a unit quaternion and implements quaternion arithmetic.
 */
public class Quaternion implements MatrixTransformation {
  static final double SQRT2 = Math.sqrt(2);
  protected double q0, q1, q2, q3;         // quaternion components
  protected double ox = 0, oy = 0, oz = 0; // origin for this rotation

  /**
   * Constructs and initializes quaternion from the specified components.
   *
   * @param q0 double
   * @param q1 double
   * @param q2 double
   * @param q3 double
   */
  public Quaternion(double q0, double q1, double q2, double q3) {
    this.q0 = q0;
    this.q1 = q1;
    this.q2 = q2;
    this.q3 = q3;
    normalize();
  }

  /**
   * Constructs and initializes quaternion from the specified components.
   *
   * @param q0 double
   * @param vector sets with q1:vector.x, q2:vector.y, q3:vector.z
   */
  public Quaternion(double q0, Vec3D vector) {
    this(q0, vector.x, vector.y, vector.z);
  }

  /**
   * Constructs and initializes a quaternion from the array of length 4.
   * @param q the array of length 4 containing q0, q1, q2, q3
   */
  public Quaternion(double[] q) {
    q0 = q[0];
    q1 = q[1];
    q2 = q[2];
    q3 = q[3];
    normalize();
  }

  /**
   * Constructs and initializes a Quaternion with the same values as the given quaternion.
   * @param q the Vector3d containing the initialization x y z data
   */
  public Quaternion(Quaternion q) {
    q0 = q.q0;
    q1 = q.q1;
    q2 = q.q2;
    q3 = q.q3;
    normalize();
  }

  /**
   * Constructs and initializes a unit quaternion (1,0,0,0).
   */
  public Quaternion() {
    this(1, 0, 0, 0);
  }

  /**
   * Instantiates a quaternion that aligns the first vector with the second vector.
   *
   * @param v1 double[]
   * @param v2 double[]
   * @return Quaternion
   */
  public static Quaternion createAlignmentTransformation(double[] v1, double v2[]) {
    v1 = VectorMath.normalize(v1.clone());
    v2 = VectorMath.normalize(v2.clone());
    double[] r = VectorMath.cross3D(v1, v2);
    double s = Math.sqrt(2*(1+VectorMath.dot(v1, v2)));
    return new Quaternion(s/2, r[0]/s, r[1]/s, r[2]/s);
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param ox double
   * @param oy double
   * @param oz double
   */
  public void setOrigin(double ox, double oy, double oz) {
    this.ox = ox;
    this.oy = oy;
    this.oz = oz;
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param origin double[] the new origin
   * @return double[]
   */
  public double[] setOrigin(double[] origin) {
    this.ox = origin[0];
    this.oy = origin[1];
    this.oz = origin[2];
    return origin;
  }
  
  
  /**
   * Returns the origin for this rotation
   * @return
   */
  public double[] getOrigin() {
    return new double[] { ox,oy,oz };
  }

  /**
   * Gets the direct rotation matrix of this quaternion rotation.
   * Assumes that the quaternion has been normalized.
   *
   * If the mat parameter is null a double[3][3] array is created;
   * otherwise the given array is used.
   *
   * @param mat double[][] optional matrix
   * @return double[][] the matrix
   */
  public final double[][] getRotationMatrix(double[][] mat) {
    double q0q0 = q0*q0, q0q1 = q0*q1, q0q2 = q0*q2, q0q3 = q0*q3;
    double q1q1 = q1*q1, q1q2 = q1*q2, q1q3 = q1*q3;
    double q2q2 = q2*q2, q2q3 = q2*q3;
    double q3q3 = q3*q3;
    if(mat==null) {
      mat = new double[3][3];
    }
    mat[0][0] = (q0q0+q1q1-q2q2-q3q3);
    mat[0][1] = 2*(-q0q3+q1q2);
    mat[0][2] = 2*(q0q2+q1q3);
    mat[1][0] = 2*(q0q3+q1q2);
    mat[1][1] = (q0q0-q1q1+q2q2-q3q3);
    mat[1][2] = 2*(-q0q1+q2q3);
    mat[2][0] = 2*(-q0q2+q1q3);
    mat[2][1] = 2*(q0q1+q2q3);
    mat[2][2] = (q0q0-q1q1-q2q2+q3q3);
    return mat;
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
    double q0q0 = q0*q0, q0q1 = q0*q1, q0q2 = q0*q2, q0q3 = q0*q3;
    double q1q1 = q1*q1, q1q2 = q1*q2, q1q3 = q1*q3;
    double q2q2 = q2*q2, q2q3 = q2*q3;
    double q3q3 = q3*q3;
    if(mat==null) {
      mat = new double[16];
    }
    mat[0] = (q0q0+q1q1-q2q2-q3q3);
    mat[4] = 2*(-q0q3+q1q2);
    mat[8] = 2*(q0q2+q1q3);
    mat[1] = 2*(q0q3+q1q2);
    mat[5] = (q0q0-q1q1+q2q2-q3q3);
    mat[9] = 2*(-q0q1+q2q3);
    mat[2] = 2*(-q0q2+q1q3);
    mat[3] = 0;
    mat[6] = 2*(q0q1+q2q3);
    mat[7] = 0;
    mat[10] = (q0q0-q1q1-q2q2+q3q3);
    mat[11] = 0;
    mat[12] = ox-ox*mat[0]-oy*mat[4]-oz*mat[8];
    mat[13] = oy-ox*mat[1]-oy*mat[5]-oz*mat[9];
    mat[14] = oz-ox*mat[2]-oy*mat[6]-oz*mat[10];
    mat[15] = 1;
    return mat;
  }

  /**
   * Gets the Quaternion coordinates.
   * @return double[]
   */
  public double[] getCoordinates() {
    return new double[] {q0, q1, q2, q3};
  }

  /**
   * Sets the quaternion coordinates.
   *
   * @param q0 double
   * @param q1 double
   * @param q2 double
   * @param q3 double
   */
  public void setCoordinates(double q0, double q1, double q2, double q3) {
    this.q0 = q0;
    this.q1 = q1;
    this.q2 = q2;
    this.q3 = q3;
    normalize();
  }

  /**
   * Sets the quaternion coordinates from the array of length 4.
   *
   * @param q the array of length 4 containing q0, q1, q2, q3
   */
  public double[] setCoordinates(double[] q) {
    q0 = q[0];
    q1 = q[1];
    q2 = q[2];
    q3 = q[3];
    normalize();
    return q;
  }

  /**
   * Normalizes this quaternion in place.
   */
  public final void normalize() {
    double norm = q0*q0+q1*q1+q2*q2+q3*q3;
    if(norm==1) {
      return; // often doesn't happen due to roundoff
    }
    norm = 1.0/Math.sqrt(norm); // expensive operation
    q0 *= norm;
    q1 *= norm;
    q2 *= norm;
    q3 *= norm;
  }

  /**
   * Conjugates this quaternion in place.
   */
  public final void conjugate() {
    q1 = -q1;
    q2 = -q2;
    q3 = -q3;
  }

  /**
   * Adds this quaternion to the given quaternion.
   *
   * @param q Quaternion
   */
  public final void add(Quaternion q) {
    q0 += this.q0;
    q1 += this.q1;
    q2 += this.q2;
    q3 += this.q3;
  }

  /**
   * Subtracts this quaternion from the given quaternion.
   *
   * @param q Quaternion
   */
  public final void subtract(Quaternion q) {
    q0 -= this.q0;
    q1 -= this.q1;
    q2 -= this.q2;
    q3 -= this.q3;
  }

  /**
   * Multiplies this quaternion with the given quaternion.
   *
   * @param q Quaternion
   */
  public final void multiply(Quaternion q) {
    double w = q0*q.q0-q1*q.q1-q2*q.q2-q3*q.q3;
    double x = q3*q.q2-q2*q.q3+q1*q.q0+q0*q.q1;
    double y = q1*q.q3-q3*q.q1+q2*q.q0+q0*q.q2;
    double z = q2*q.q1-q1*q.q2+q3*q.q0+q0*q.q3;
    q0 = w;
    q1 = x;
    q2 = y;
    q3 = z;
    normalize();
  }

  /**
   * Returns the dot product of this quaternion and quaternion q.
   * @param q the other quaternion
   * @return the dot product of this and q
   */
  public final double dot(Quaternion q) {
    return(q0*q.q0+q1*q.q1+q2*q.q2+q3*q.q3);
  }

  /**
   * Returns the squared magnitude of this vector.
   * @return the squared magnitude
   */
  public final double magnitudeSquared() {
    return(q0*q0+q1*q1+q2*q2+q3*q3);
  }

  /**
   * Returns the magnitude of this quaternion.
   * @return the magnitude
   */
  public final double magnitude() {
    return Math.sqrt(q0*q0+q1*q1+q2*q2+q3*q3);
  }

  /**
   *    Returns the angle in radians between this quaternion and the given
   *    quaternion.
   *
   *    @param q    the other quaternion
   *    @return   the angle in radians in the range [0,PI]
   */
  public final double angle(Quaternion q) {
    double norm1 = Math.sqrt(q1*q1+q2*q2+q3*q3);             // normalization for vector part of this quaternion
    double norm2 = Math.sqrt(q.q1*q.q1+q.q2*q.q2+q.q3*q.q3); // normazliation for vector part for given quaternion
    double w = Math.sqrt(1+(q1*q.q1+q2*q.q2+q3*q.q3)/norm1/norm2);
    return(2*Math.acos(w/SQRT2));
  }

  /**
   * Instaniates a quaterion whose components are identical to this quaterion.
   * @return Object
   */
  public Object clone() {
    Quaternion q = new Quaternion(q0, q1, q2, q3);
    q.setOrigin(ox, oy, oz);
    return q;
  }

  /**
   * Transforms (rotates) the coordinates of the given point.
   *
   * @param p double[]
   * @return double[]
   */
  public double[] direct(double[] p) { // assumes quaternion is normalized
    p[0] -= ox;
    p[1] -= oy;
    p[2] -= oz;
    double pMult = 2*q0*q0-1;
    double vMult = 2*(q1*p[0]+q2*p[1]+q3*p[2]);
    double crossMult = 2*q0;
    double x = pMult*p[0]+vMult*q1+crossMult*(q2*p[2]-q3*p[1]);
    double y = pMult*p[1]+vMult*q2+crossMult*(q3*p[0]-q1*p[2]);
    p[2] = pMult*p[2]+vMult*q3+crossMult*(q1*p[1]-q2*p[0])+oz;
    p[0] = x+ox;
    p[1] = y+oy;
    return p;
  }

  public double[] inverse(double[] p) throws UnsupportedOperationException { // assumes quaternion is normalized
    p[0] -= ox;
    p[1] -= oy;
    p[2] -= oz;
    double pMult = 2*q0*q0-1;
    double vMult = 2*(q1*p[0]+q2*p[1]+q3*p[2]);
    double crossMult = -2*q0; // inverse transformation changes sign of cross term
    double x = pMult*p[0]+vMult*q1+crossMult*(q2*p[2]-q3*p[1]);
    double y = pMult*p[1]+vMult*q2+crossMult*(q3*p[0]-q1*p[2]);
    p[2] = pMult*p[2]+vMult*q3+crossMult*(q1*p[1]-q2*p[0])+oz;
    p[0] = x+ox;
    p[1] = y+oy;
    return p;
  }

  public static XML.ObjectLoader getLoader() {
    return new QuaternionLoader();
  }

  protected static class QuaternionLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Quaternion qr = (Quaternion) obj;
      control.setValue("q0", qr.q0); //$NON-NLS-1$
      control.setValue("q1", qr.q1); //$NON-NLS-1$
      control.setValue("q2", qr.q2); //$NON-NLS-1$
      control.setValue("q3", qr.q3); //$NON-NLS-1$
      control.setValue("ox", qr.ox); //$NON-NLS-1$
      control.setValue("oy", qr.oy); //$NON-NLS-1$
      control.setValue("oz", qr.oz); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Quaternion();
    }

    public Object loadObject(XMLControl control, Object obj) {
      Quaternion qr = (Quaternion) obj;
      double q0 = control.getDouble("q0"); //$NON-NLS-1$
      double q1 = control.getDouble("q0"); //$NON-NLS-1$
      double q2 = control.getDouble("q0"); //$NON-NLS-1$
      double q3 = control.getDouble("q0"); //$NON-NLS-1$
      qr.setCoordinates(q0, q1, q2, q3);
      double ox = control.getDouble("ox"); //$NON-NLS-1$
      double oy = control.getDouble("oy"); //$NON-NLS-1$
      double oz = control.getDouble("oz"); //$NON-NLS-1$
      qr.setOrigin(ox, oy, oz);
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
