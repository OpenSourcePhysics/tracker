/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * A 3-element vector that is represented by double-precision floating point
 * x,y,z coordinates.
 */
public class Vec3D {
  public double x, y, z;

  /**
   * Constructs and initializes a Vector3d from the specified xyz coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   */
  public Vec3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Constructs and initializes a Vector3d from the array of length 3.
   * @param v the array of length 3 containing xyz in order
   */
  public Vec3D(double[] v) {
    x = v[0];
    y = v[1];
    z = v[2];
  }

  /**
   * Constructs and initializes a Vector3d from the specified Vector3d.
   * @param v1 the Vector3d containing the initialization x y z data
   */
  public Vec3D(Vec3D v1) {
    x = v1.x;
    y = v1.y;
    z = v1.z;
  }

  /**
   * Constructs and initializes it to (0,0,0).
   */
  public Vec3D() {
    this(0, 0, 0);
  }

  /**
   *  Sets this vector to the vector subtraction of vectors v1 and v2.
   *  @param v1 the first vector
   *  @param v2 the second vector
   */
  public final void subtract(Vec3D v1, Vec3D v2) {
    this.x = v1.x-v2.x;
    this.y = v1.y-v2.y;
    this.z = v1.z-v2.z;
  }

  /**
   *  Sets this vector to the vector addition of vectors v1 and v2.
   *  @param v1 the first vector
   *  @param v2 the second vector
   */
  public final void add(Vec3D v1, Vec3D v2) {
    this.x = v1.x+v2.x;
    this.y = v1.y+v2.y;
    this.z = v1.z+v2.z;
  }

  /**
   *  Sets this vector to the vector cross product of vectors v1 and v2.
   *  @param v1 the first vector
   *  @param v2 the second vector
   */
  public void cross(Vec3D v1, Vec3D v2) {
    double x, y;
    x = v1.y*v2.z-v1.z*v2.y;
    y = v2.x*v1.z-v2.z*v1.x;
    this.z = v1.x*v2.y-v1.y*v2.x;
    this.x = x;
    this.y = y;
  }

  /**
   *  Sets this vector to the multiplication of vector v1 and a scalar number
   *  @param v1 the vector
   *  @param number to multiply v1
   */
  public void multiply(Vec3D v1, double number) {
    this.x = v1.x*number;
    this.y = v1.y*number;
    this.z = v1.z*number;
  }

  /**
   * Normalizes this vector in place.
   */
  public final void normalize() {
    double norm = this.x*this.x+this.y*this.y+this.z*this.z;
    if(norm<Util.defaultNumericalPrecision) {
      return; // vector is zero
    }
    if(norm==1) {
      return; // often doesn't happen due to roundoff
    }
    norm = 1/Math.sqrt(norm); // expensive operation
    this.x *= norm;
    this.y *= norm;
    this.z *= norm;
  }

  /**
   * Returns the dot product of this vector and vector v1.
   * @param v1 the other vector
   * @return the dot product of this and v1
   */
  public final double dot(Vec3D v1) {
    return(this.x*v1.x+this.y*v1.y+this.z*v1.z);
  }

  /**
   * Returns the squared magnitude of this vector.
   * @return the squared magnitude
   */
  public final double magnitudeSquared() {
    return(this.x*this.x+this.y*this.y+this.z*this.z);
  }

  /**
   * Returns the magnitude of this vector.
   * @return the magnitude
   */
  public final double magnitude() {
    return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z);
  }

  /**
   *    Returns the angle in radians between this vector and the vector
   *    parameter; the return value is constrained to the range [0,PI].
   *    @param v1    the other vector
   *    @return   the angle in radians in the range [0,PI]
   */
  public final double angle(Vec3D v1) {
    double vDot = this.dot(v1)/(this.magnitude()*v1.magnitude());
    if(vDot<-1.0) {
      vDot = -1.0;
    }
    if(vDot>1.0) {
      vDot = 1.0;
    }
    return((Math.acos(vDot)));
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
