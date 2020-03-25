/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * Matrix2DTransformation implements 2D affine transformations
 */
public class Matrix2DTransformation implements MatrixTransformation {
  private double[] origin = new double[] {0, 0};
  private AffineTransform originTransform = new AffineTransform();
  private AffineTransform internalTransform = new AffineTransform();
  private AffineTransform originInverseTransform = new AffineTransform();
  private AffineTransform totalTransform = new AffineTransform();

  /**
   * Constructs a 2D transformation using the given matrix.
   *
   * Affine transformations can be applied to 2D coordinates.
   * A 2 by 3 matrix sets the rotation and shear.
   * A null matrix sets the transformation to the identity transformation.
   *
   * @param matrix double[][]
   */
  public Matrix2DTransformation(double[][] matrix) {
    if(matrix!=null) {
      internalTransform.setTransform(matrix[0][0], matrix[1][0], matrix[0][1], matrix[1][1], matrix[0][2], matrix[1][2]);
    }
    update();
  }

  /**
   * Constructor Matrix2DTransformation
   * @param transform
   */
  public Matrix2DTransformation(AffineTransform transform) {
    internalTransform.setTransform(transform);
    update();
  }

  /**
   * Creates a 2D transformation representing a rotation about the origin by the given angle.
   *
   * @param theta double
   *
   * @return Matrix2DTransformation
   */
  public static Matrix2DTransformation rotation(double theta) {
    return new Matrix2DTransformation(AffineTransform.getRotateInstance(theta));
  }

  /**
   * Creates a 2D transformation representing a rotation about the origin by the given angle around
   * the given axis.
   *
   * @param theta double
   * @param anchorx double
   * @param anchory double
   * @return Matrix2DTransformation
   */
  public static Matrix2DTransformation rotation(double theta, double anchorx, double anchory) {
    return new Matrix2DTransformation(AffineTransform.getRotateInstance(theta, anchorx, anchory));
  }

  public AffineTransform getTotalTransform() {
    return totalTransform;
  }

  /**
   * Provides a copy of this transformation.
   */
  public Object clone() {
    return new Matrix2DTransformation(internalTransform);
  }

  /**
* Gets the direct homogeneous affine transformation flattened into a 1-d arrray.
*
* If the mat parameter is null a double[6] array is created;
* otherwise the given array is used.
*
* @param mat double[] optional matrix
* @return double[] the matrix
*/
  public final double[] getFlatMatrix(double[] mat) {
    if(mat==null) {
      mat = new double[6];
    }
    internalTransform.getMatrix(mat);
    return mat;
  }

  /**
   * Instantiates a rotation that aligns the first vector with the second vector.
   *
   * @param v1 double[]
   * @param v2 double[]
   * @return Matrix2DTransformation
   */
  public static Matrix2DTransformation createAlignmentTransformation(double[] v1, double v2[]) {
    return new Matrix2DTransformation(AffineTransform.getRotateInstance(Math.atan2(v2[1], v2[0])-Math.atan2(v1[1], v1[0])));
  }

  /**
   *  Computes this internalTransform as a concatenation of the origin and the internal internalTransform
   */
  private void update() {
    totalTransform.setTransform(originTransform);
    totalTransform.concatenate(internalTransform);
    totalTransform.concatenate(originInverseTransform);
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param ox double
   * @param oy double
   */
  public void setOrigin(double ox, double oy) {
    origin[0] = ox;
    origin[1] = oy;
    originTransform = AffineTransform.getTranslateInstance(ox, oy);
    originInverseTransform = AffineTransform.getTranslateInstance(-ox, -oy);
    update();
  }

  /**
   * Sets the origin for this rotation.
   *
   * @param origin double[] the new origin
   * @return double[]
   */
  public double[] setOrigin(double[] origin) {
    setOrigin(origin[0], origin[1]);
    return origin;
  }

  /**
   * Multiplies (concatenates) this transformation matrix with the given transformation.
   *
   * @param trans Matrix2DTransformation
   */
  public final void multiply(Matrix2DTransformation trans) {
    internalTransform.concatenate(trans.internalTransform);
    update();
  }

  /**
   * Multiplies this rotation matrix by the given matrix.
   *
   * @param mat double[][]
   */
  public final void multiply(double[][] mat) {
    internalTransform.concatenate(new Matrix2DTransformation(mat).internalTransform);
    update();
  }

  /**
   * Transforms the given point+.
   *
   * @param point the coordinates to be transformed
   */
  public double[] direct(double[] point) {
    totalTransform.transform(point, 0, point, 0, 1);
    return point;
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
    try {
      totalTransform.inverseTransform(point, 0, point, 0, 1);
      return point;
    } catch(java.awt.geom.NoninvertibleTransformException exc) {
      throw new UnsupportedOperationException("The inverse matrix does not exist."); //$NON-NLS-1$
    }
  }

  public static XML.ObjectLoader getLoader() {
    return new Matrix2DTransformationLoader();
  }

  protected static class Matrix2DTransformationLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Matrix2DTransformation transf = (Matrix2DTransformation) obj;
      control.setValue("matrix", transf.getFlatMatrix(null)); //$NON-NLS-1$
      control.setValue("origin x", transf.origin);            //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Matrix2DTransformation(new AffineTransform());
    }

    public Object loadObject(XMLControl control, Object obj) {
      Matrix2DTransformation transf = (Matrix2DTransformation) obj;
      transf.internalTransform.setTransform(new AffineTransform((double[]) control.getObject("matrix"))); //$NON-NLS-1$
      transf.setOrigin((double[]) control.getObject("origin"));                                           //$NON-NLS-1$
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
