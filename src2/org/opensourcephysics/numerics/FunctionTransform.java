/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
    Fix the following methods: inverseTransform, createInverse
*/
package org.opensourcephysics.numerics;

/**
 * Title:        InvertibleFunction
 * Description:  An invertible function of one variable.
 */
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Class description
 *
*/
public final class FunctionTransform extends AffineTransform {
  double m00;
  double m10 = 1; // identity transform by default
  double m01 = 1;
  double m11;
  double m02;
  double m12;
  double[] flatmatrix = new double[6];
  InvertibleFunction xFunction;
  InvertibleFunction yFunction;
  boolean applyXFunction = false;
  boolean applyYFunction = false;

  /**
   * Constructor FunctionTransform
   */
  public FunctionTransform() {
    super();
  }

  /**
   * Constructor FunctionTransform
   * @param m00
   * @param m10
   * @param m01
   * @param m11
   * @param m02
   * @param m12
   */
  public FunctionTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
    super(m00, m10, m01, m11, m02, m12);
    this.m00 = m00;
    this.m10 = m10;
    this.m01 = m01;
    this.m11 = m11;
    this.m02 = m02;
    this.m12 = m12;
  }

  /*
      Sets the x function to the specified parameter.
      @param x the new x function. Can not be null.
    */
  public void setXFunction(InvertibleFunction x) {
    if(x==null) {
      throw new NullPointerException("x function can not be null."); //$NON-NLS-1$
    }
    xFunction = x;
  }

  /*
      Sets the y function to the specified parameter.
      @param y the new y function. Can not be null.
    */
  public void setYFunction(InvertibleFunction y) {
    if(y==null) {
      throw new NullPointerException("y function can not be null."); //$NON-NLS-1$
    }
    yFunction = y;
  }

  /*
      sets whether this FunctionTransform applies the x function
    */
  public void setApplyXFunction(boolean b) {
    applyXFunction = b;
  }

  /*
      sets whether this FunctionTransform applies the y function
    */
  public void setApplyYFunction(boolean b) {
    applyYFunction = b;
  }

  public void translate(double tx, double ty) {
    super.translate(tx, ty);
    updateMatrix();
  }

  public void rotate(double theta) {
    super.rotate(theta);
    updateMatrix();
  }

  public void rotate(double theta, double x, double y) {
    super.rotate(theta, x, y);
    updateMatrix();
  }

  public void scale(double sx, double sy) {
    super.scale(sx, sy);
    updateMatrix();
  }

  public void shear(double shx, double shy) {
    super.shear(shx, shy);
    updateMatrix();
  }

  public void setToIdentity() {
    super.setToIdentity();
    updateMatrix();
  }

  public void setToTranslation(double tx, double ty) {
    super.setToTranslation(tx, ty);
    updateMatrix();
  }

  public void setToRotation(double theta) {
    super.setToRotation(theta);
    updateMatrix();
  }

  public void setToRotation(double theta, double x, double y) {
    super.setToRotation(theta, x, y);
    updateMatrix();
  }

  public void setToScale(double sx, double sy) {
    super.setToScale(sx, sy);
    updateMatrix();
  }

  public void setToShear(double shx, double shy) {
    super.setToShear(shx, shy);
    updateMatrix();
  }

  public void setTransform(AffineTransform Tx) {
    super.setTransform(Tx);
    updateMatrix();
  }

  public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
    super.setTransform(m00, m10, m01, m11, m02, m12);
    updateMatrix();
  }

  /*
      Concatenates this FunctionTransform with the given AffineTransform as specified in AffineTransform. Note-The if specified parameter is a FunctionTransform, the function is ignored.
    */
  public void concatenate(AffineTransform Tx) {
    super.concatenate(Tx);
    updateMatrix();
  }

  /*
      Pre-concatenates this FunctionTransform with the given AffineTransform as specified in AffineTransform. Note-The if specified parameter is a FunctionTransform, the function is ignored.
    */
  public void preConcatenate(AffineTransform Tx) {
    super.preConcatenate(Tx);
    updateMatrix();
  }

  public AffineTransform createInverse() throws NoninvertibleTransformException { // FIX_ME
    AffineTransform at = super.createInverse();
    FunctionTransform ft = new FunctionTransform();
    ft.setTransform(at);
    final InvertibleFunction xFunction = new InvertibleFunction() {
      public double evaluate(double x) {
        return FunctionTransform.this.xFunction.getInverse(x);
      }
      public double getInverse(double y) {
        return FunctionTransform.this.xFunction.evaluate(y);
      }

    };
    final InvertibleFunction yFunction = new InvertibleFunction() {
      public double evaluate(double x) {
        return FunctionTransform.this.yFunction.getInverse(x);
      }
      public double getInverse(double y) {
        return FunctionTransform.this.yFunction.evaluate(y);
      }

    };
    ft.setXFunction(xFunction);
    ft.setYFunction(yFunction);
    return ft;
  }

  public Point2D transform(Point2D ptSrc, Point2D ptDst) {
    if(ptDst==null) {
      if(ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    // Copy source coords into local variables in case src == dst
    double x = ptSrc.getX();
    double y = ptSrc.getY();
    if(applyXFunction) {
      x = xFunction.evaluate(x);
    }
    if(applyYFunction) {
      y = yFunction.evaluate(y);
    }
    ptDst.setLocation(x*m00+y*m01+m02, x*m10+y*m11+m12);
    return ptDst;
  }

  public void transform(Point2D[] ptSrc, int srcOff, Point2D[] ptDst, int dstOff, int numPts) {
    while(--numPts>=0) {
      // Copy source coords into local variables in case src == dst
      Point2D src = ptSrc[srcOff++];
      double x = src.getX();
      double y = src.getY();
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      Point2D dst = ptDst[dstOff++];
      if(dst==null) {
        if(src instanceof Point2D.Double) {
          dst = new Point2D.Double();
        } else {
          dst = new Point2D.Float();
        }
        ptDst[dstOff-1] = dst;
      }
      dst.setLocation(x*m00+y*m01+m02, x*m10+y*m11+m12);
    }
  }

  public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
    double M00;
    double M01;
    double M02;
    double M10;
    double M11;
    double M12; // For caching
    if((dstPts==srcPts)&&(dstOff>srcOff)&&(dstOff<srcOff+numPts*2)) {
      // If the arrays overlap partially with the destination higher
      // than the source and we transform the coordinates normally
      // we would overwrite some of the later source coordinates
      // with results of previous transformations.
      // To get around this we use arraycopy to copy the points
      // to their final destination with correct overwrite
      // handling and then transform them in place in the new
      // safer location.
      System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*2);
      // srcPts = dstPts;       // They are known to be equal.
      srcOff = dstOff;
    }
    M00 = m00;
    M01 = m01;
    M02 = m02;
    M10 = m10;
    M11 = m11;
    M12 = m12;
    while(--numPts>=0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      // W. Christian
      // Java 1.3 bug in Windows VM
      // the following two lines may cause a crash while drawing shapes if |dstPts| is very large
      dstPts[dstOff++] = (float) (M00*x+M01*y+M02);
      dstPts[dstOff++] = (float) (M10*x+M11*y+M12);
    }
  }

  public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
    double M00;
    double M01;
    double M02;
    double M10;
    double M11;
    double M12; // For caching
    if((dstPts==srcPts)&&(dstOff>srcOff)&&(dstOff<srcOff+numPts*2)) {
      // If the arrays overlap partially with the destination higher
      // than the source and we transform the coordinates normally
      // we would overwrite some of the later source coordinates
      // with results of previous transformations.
      // To get around this we use arraycopy to copy the points
      // to their final destination with correct overwrite
      // handling and then transform them in place in the new
      // safer location.
      System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*2);
      // srcPts = dstPts;       // They are known to be equal.
      srcOff = dstOff;
    }
    M00 = m00;
    M01 = m01;
    M02 = m02;
    M10 = m10;
    M11 = m11;
    M12 = m12;
    while(--numPts>=0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      // W. Christian
      // Java 1.3 bug in Windows VM
      // the following two lines may cause a crash while drawing shapes if |dstPts| is very large
      dstPts[dstOff++] = M00*x+M01*y+M02;
      dstPts[dstOff++] = M10*x+M11*y+M12;
    }
  }

  public void transform(float[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
    double M00;
    double M01;
    double M02;
    double M10;
    double M11;
    double M12; // For caching
    M00 = m00;
    M01 = m01;
    M02 = m02;
    M10 = m10;
    M11 = m11;
    M12 = m12;
    while(--numPts>=0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      // W. Christian
      // Java 1.3 bug in Windows VM
      // the following two lines may cause a crash while drawing shapes if |dstPts| is very large
      dstPts[dstOff++] = M00*x+M01*y+M02;
      dstPts[dstOff++] = M10*x+M11*y+M12;
    }
  }

  public void transform(double[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
    double M00;
    double M01;
    double M02;
    double M10;
    double M11;
    double M12; // For caching
    M00 = m00;
    M01 = m01;
    M02 = m02;
    M10 = m10;
    M11 = m11;
    M12 = m12;
    while(--numPts>=0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      // W. Christian
      // Java 1.3 bug in Windows VM
      // the following two lines cause a crash while drawing shapes if |dstPts| is very large
      dstPts[dstOff++] = (float) (M00*x+M01*y+M02);
      dstPts[dstOff++] = (float) (M10*x+M11*y+M12);
    }
  }

  public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst) throws NoninvertibleTransformException { // FIX_ME
    if(ptDst==null) {
      if(ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    //FIX ME
    return ptDst;
  }

  public void inverseTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) // FIX_ME
    throws NoninvertibleTransformException {
    if((dstPts==srcPts)&&(dstOff>srcOff)&&(dstOff<srcOff+numPts*2)) {
      // If the arrays overlap partially with the destination higher
      // than the source and we transform the coordinates normally
      // we would overwrite some of the later source coordinates
      // with results of previous transformations.
      // To get around this we use arraycopy to copy the points
      // to their final destination with correct overwrite
      // handling and then transform them in place in the new
      // safer location.
      System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*2);
      // srcPts = dstPts;       // They are known to be equal.
      srcOff = dstOff;
    }
    double det = m00*m11-m01*m10;
    if(Math.abs(det)<=Double.MIN_VALUE) {
      throw new NoninvertibleTransformException("Determinant is "+det); //$NON-NLS-1$
    }
    // newx = M00 * x + M01 * y + M02;
    /*
        dstPts[dstOff++] = (float) (M10 * x + M11 * y + M12);
        while(--numPts >= 0) {
        double x = srcPts[srcOff++];
        double y = srcPts[srcOff++];
        if(applyXFunction) {
        x = xFunction.getInverse(x);
        }
        if(applyYFunction) {
        y = yFunction.getInverse(y);
        }
        double div = m00
        (x-m02)/m00
        z = M00 * x + M01 * y + M02;
        x * m11 - y * m01) / M00 * M11 - M01 * M10
        x -=- m02;
        x/m01
        x/m00
        y -= m12;
        ptDst.setLocation(x / m00, y / m11);
        dstPts[dstOff++] = M00 * x + M01 * y + M02;
        dstPts[dstOff++] = M10 * x + M11 * y + M12;
        }
      */
  }

  public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst) {
    if(ptDst==null) {
      if(ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    // Copy source coords into local variables in case src == dst
    double x = ptSrc.getX();
    double y = ptSrc.getY();
    if(applyXFunction) {
      x = xFunction.evaluate(x);
    }
    if(applyYFunction) {
      y = yFunction.evaluate(y);
    }
    ptDst.setLocation(x*m00+y*m01, x*m10+y*m11);
    return ptDst;
  }

  public void deltaTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
    double M00;
    double M01;
    double M10;
    double M11; // For caching
    if((dstPts==srcPts)&&(dstOff>srcOff)&&(dstOff<srcOff+numPts*2)) {
      // If the arrays overlap partially with the destination higher
      // than the source and we transform the coordinates normally
      // we would overwrite some of the later source coordinates
      // with results of previous transformations.
      // To get around this we use arraycopy to copy the points
      // to their final destination with correct overwrite
      // handling and then transform them in place in the new
      // safer location.
      System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*2);
      // srcPts = dstPts;       // They are known to be equal.
      srcOff = dstOff;
    }
    M00 = m00;
    M01 = m01;
    M10 = m10;
    M11 = m11;
    while(--numPts>=0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      if(applyXFunction) {
        x = xFunction.evaluate(x);
      }
      if(applyYFunction) {
        y = yFunction.evaluate(y);
      }
      dstPts[dstOff++] = x*M00+y*M01;
      dstPts[dstOff++] = x*M10+y*M11;
    }
  }

  public boolean equals(Object obj) {
    if(obj instanceof FunctionTransform) {
      FunctionTransform a = (FunctionTransform) obj;
      double[] matrix = new double[6];
      a.getMatrix(matrix);
      if((m00==matrix[0])&&(m01==matrix[1])&&(m02==matrix[2])&&(m10==matrix[3])&&(m11==matrix[4])&&(m12==matrix[5])) {
        if((applyXFunction==a.applyXFunction)&&(applyYFunction==a.applyYFunction)) {
          return(xFunction.getClass()==a.xFunction.getClass())&&(yFunction.getClass()==a.yFunction.getClass());
        }
      }
    } else if(obj instanceof AffineTransform) {
      if(!applyXFunction&&!applyYFunction) {
        return super.equals(obj);
      }
    }
    return false;
  }

  private void updateMatrix() {
    getMatrix(flatmatrix);
    m00 = flatmatrix[0];
    m10 = flatmatrix[1];
    m01 = flatmatrix[2];
    m11 = flatmatrix[3];
    m02 = flatmatrix[4];
    m12 = flatmatrix[5];
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
