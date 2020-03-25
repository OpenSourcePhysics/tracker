/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * VectorMath is a utility class for vector math.
 *
 * Contains static methods for dot products, cross products, etc.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public final class VectorMath {
  private VectorMath() {} // all methods are static so prohibit instantiation

  /**
   * Add a vector times a scalar to a vector.
   * Elements in the first vector are modified and set equal to the sum.
   *
   * @param a  the first  vector
   * @param b  the second vector
   * @param c  the scalar multiplier
   *
   * @return double[] the first vector.
   */
  static public double[] plus(double[] a, double[] b, double c) {
    int aLength = a.length;
    if(aLength!=b.length) {
      throw new UnsupportedOperationException("ERROR: Vectors must be of equal length to add."); //$NON-NLS-1$
    }
    for(int i = 0; i<aLength; i++) {
      a[i] += c*b[i];
    }
    return a;
  }

  /**
   * Add two vectors.
   * Elements in the first vector are modified and set equal to the sum.
   *
   * @param a  the first  vector
   * @param b  the second vector
   *
   * @return double[] the first vector.
   */
  static public double[] plus(double[] a, double[] b) {
    int aLength = a.length;
    if(aLength!=b.length) {
      throw new UnsupportedOperationException("ERROR: Vectors must be of equal length to add."); //$NON-NLS-1$
    }
    for(int i = 0; i<aLength; i++) {
      a[i] += b[i];
    }
    return a;
  }

  static public double[] normalize(double[] a) {
    double mag = magnitude(a);
    if(mag==0) { // return unit vector along first direction
      a[0] = 1;
      return a;
    }
    a[0] /= mag;
    a[1] /= mag;
    a[2] /= mag;
    return a;
  }

  /**
   * Calculate the dot product of two vectors.
   * @param a  the first  vector
   * @param b  the second vector
   * @return the dot product
   */
  static public double dot(double[] a, double[] b) {
    int aLength = a.length;
    if(aLength!=b.length) {
      throw new UnsupportedOperationException("ERROR: Vectors must be of equal dimension in dot product."); //$NON-NLS-1$
    }
    double sum = 0;
    for(int i = 0; i<aLength; i++) {
      sum += a[i]*b[i];
    }
    return sum;
  }

  /**
   * Projects the first vector onto the second vector.
   * @param a  the first  vector
   * @param b  the second vector
   * @return the projection
   */
  static public double[] project(double[] a, double[] b) {
    int aLength = a.length;
    if(aLength!=b.length) {
      throw new UnsupportedOperationException("ERROR: Vectors must be of equal dimension to compute projection."); //$NON-NLS-1$
    }
    double[] result = b.clone();
    double asquared = 0;
    double dot = 0;
    for(int i = 0; i<aLength; i++) {
      dot += a[i]*b[i];
      asquared += a[i]*a[i];
    }
    dot /= asquared;
    for(int i = 0; i<aLength; i++) {
      result[i] /= dot;
    }
    return result;
  }

  /**
   * Computes the part of the first vector that is perpendicular to the second vector.
   * @param a  the first  vector
   * @param b  the second vector
   * @return the perpendicular part
   */
  static public double[] perp(double[] a, double[] b) {
    int aLength = a.length;
    if(aLength!=b.length) {
      throw new UnsupportedOperationException("ERROR: Vectors must be of equal dimension to find the perpendicular component."); //$NON-NLS-1$
    }
    double[] result = b.clone();
    double asquared = 0;
    double dot = 0;
    for(int i = 0; i<aLength; i++) {
      dot += a[i]*b[i];
      asquared += a[i]*a[i];
    }
    dot /= asquared;
    for(int i = 0; i<aLength; i++) {
      result[i] = a[i]-b[i]/dot;
    }
    return result;
  }

  /**
   * Computes the magnitdue squared of this vector.
   * The magnitude squared is dot product of a vector with itself.
   * @param a  the  vector
   * @return the magnitude squared
   */
  static public double magnitudeSquared(double[] a) {
    int aLength = a.length;
    double sum = 0;
    for(int i = 0; i<aLength; i++) {
      sum += a[i]*a[i];
    }
    return sum;
  }

  /**
   * Calculates the magnitude a vector.
   * @param a  the  vector
   * @return the magnitude
   */
  static public double magnitude(double[] a) {
    double sum = 0;
    for(int i = 0, n = a.length; i<n; i++) {
      sum += a[i]*a[i];
    }
    return Math.sqrt(sum);
  }

  /**
   *  Calculates the vector cross product of double[3] vectors v1 and v2.
   *  @param v1 the first vector
   *  @param v2 the second vector
   *
   *  @return double[] the 3D cross product
   */
  static public final double[] cross3D(double[] v1, double[] v2) {
    double[] v = new double[3];
    v[0] = v1[1]*v2[2]-v1[2]*v2[1];
    v[1] = v2[0]*v1[2]-v2[2]*v1[0];
    v[2] = v1[0]*v2[1]-v1[1]*v2[0];
    return v;
  }

  /**
   * Calculates the cross product of a double[2] vector in a plane and a vector perpendicular to that plane.
   *
   * Elements in the given vector are modified. The resulting vector componets are in the basis set of
   * the given vector.
   *
   * @param v  the vector in the plane
   * @param b  the vector perpendicular to the plane
   *
   * @return the cropss product
   */
  static public double[] cross2D(double[] v, double b) {
    if(v.length!=2) {
      throw new UnsupportedOperationException("ERROR: Cross2D product requires 2 component array."); //$NON-NLS-1$
    }
    double temp = v[0];
    v[0] = v[1]*b;
    v[1] = -temp*b;
    return v;
  }

  /**
   * Calculate the cross product of two-component vectors.
   * The result is the component perpendicular to the plane.
   *
   * @param a  the first  vector
   * @param b  the second vector
   * @return the cross product.
   */
  static public double cross2D(double[] a, double[] b) {
    if((a.length!=2)||(b.length!=2)) {
      throw new UnsupportedOperationException("ERROR: Cross2D product requires 2 component arrays."); //$NON-NLS-1$
    }
    return a[0]*b[1]-a[1]*b[0];
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
