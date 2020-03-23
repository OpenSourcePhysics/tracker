/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*

    Adapted to OSP by Javier E. Hasbun (2009) with added functionality
    from the original JEP - Java Math Expression Parser 2.24 of Nathan Funk.
  <P>
    @Copyright (c) 2017
    This software is to support the Open Source Physics library
    http://www.opensourcephysics.org under the terms of the GNU General Public
    License (GPL) as published by the Free Software Foundation.

 **/
package org.opensourcephysics.numerics;

/**
 * Class description
 *
*/
public class Complex {
  /** the real component */
  private double re;

  /** the imaginary component */
  private double im;

  //------------------------------------------------------------------------
  // Constructors

  /**
   * Default constructor.
   */
  public Complex() {
    re = 0;
    im = 0;
  }

  /**
   * Constructor from a single double value. The complex number is
   * initialized with the real component equal to the parameter, and
   * the imaginary component equal to zero.
   * @param re_in
   */
  public Complex(double re_in) {
    re = re_in;
    im = 0;
  }

  /**
   * Construct from a Number. This constructor uses the doubleValue()
   * method of the parameter to initialize the real component of the
   * complex number. The imaginary component is initialized to zero.
   * @param re_in
   */
  public Complex(Number re_in) {
    re = re_in.doubleValue();
    im = 0;
  }

  /**
   * Copy constructor
   * @param z
   */
  public Complex(Complex z) {
    re = z.re;
    im = z.im;
  }

  /**
   * Initialize the real and imaginary components to the values given
   * by the parameters.
   * @param re_in
   * @param im_in
   */
  public Complex(double re_in, double im_in) {
    re = re_in;
    im = im_in;
  }

  /**
   * Convert Cartesian to polar
   */
  public Complex polar() {
    double r = StrictMath.sqrt(re*re+im*im);
    double a = StrictMath.atan2(im, re);
    return new Complex(r, a);
  }

  /**
   * Convert polar to Cartesian
   */
  public Complex cartesian() {
    return new Complex(re*StrictMath.cos(im), re*StrictMath.sin(im));
  }

  /**
   * Returns the real component of this object
   */
  public double re() {
    return re;
  }

  /**

   * Returns the imaginary component of this object

   */
  public double im() {
    return im;
  }

  /**
   * Copies the values from the parameter object to this object
   */
  public void set(Complex z) {
    re = z.re;
    im = z.im;
  }

  /**
   * Sets the real and imaginary values of the object.
   */
  public void set(double re_in, double im_in) {
    re = re_in;
    im = im_in;
  }

  /**
   * Sets the real component of the object
   */
  public void setRe(double re_in) {
    re = re_in;
  }

  /**
   * Sets the imaginary component of the object
   */
  public void setIm(double im_in) {
    im = im_in;
  }

  //------------------------------------------------------------------------
  // Various functions

  /**
   * Compares this object with the Complex number given as parameter
   * <pre>b</pre>. The <pre>tolerance</pre> parameter is the radius
   * within which the <pre>b</pre> number must lie for the two
   * complex numbers to be considered equal.
   *
   * @return <pre>true</pre> if the complex number are considered equal,
   * <pre>false</pre> otherwise.
   */
  public boolean equals(Complex b, double tolerance) {
    double temp1 = (re-b.re);
    double temp2 = (im-b.im);
    return(temp1*temp1+temp2*temp2)<=tolerance*tolerance;
  }

  /**
   * Returns the value of this complex number as a string in the format:
   * <pre>(real, imaginary)</pre>.
   */
  public String toString() {
    return "("+re+", "+im+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Returns the absolute value of the complex number.
   */
  public double abs() {
    double absRe = Math.abs(re);
    double absIm = Math.abs(im);
    if((absRe==0)&&(absIm==0)) {
      return 0;
    } else if(absRe>absIm) {
      double temp = absIm/absRe;
      return absRe*Math.sqrt(1+temp*temp);
    } else {
      double temp = absRe/absIm;
      return absIm*Math.sqrt(1+temp*temp);
    }
  }

  /**

   * Returns the square of the absolute value (re*re+im*im).

   */
  public double abs2() {
    return re*re+im*im;
  }

  /**
   * Returns the magnitude of the complex number
   */
  public double mag() {
    return StrictMath.sqrt(re*re+im*im);
  }

  /**
   * Returns the argument of this complex number (Math.atan2(re,im))
   */
  public double arg() {
    return Math.atan2(im, re);
  }

  /** Convert text representation to a Complex.
   *  input format  (real_double,imaginary_double)
   */
  public static Complex parseComplex(String s) {
    int from = s.indexOf('(');
    if(from==-1) {
      return null;
    }
    int to = s.indexOf(',', from);
    double x = Double.parseDouble(s.substring(from+1, to));
    from = to;
    to = s.indexOf(')', from);
    double y = Double.parseDouble(s.substring(from+1, to));
    return new Complex(x, y);
  }

  /**
   * Returns the sum of two complex numbers
   */
  public Complex add(Complex z) {
    return new Complex(re+z.re, im+z.im);
  }

  /**
   * Returns the sum of a complex number and a double
   */
  public Complex add(double d) {
    return new Complex(re+d, im);
  }

  /**
   * Returns the subtraction of complex z from a complex number
   */
  public Complex subtract(Complex z) {
    return new Complex(re-z.re, im-z.im);
  }

  /**
   * Subtracts the double d from the complex number
   * Returns the subtraction of complex z from a complex number
   */
  public Complex subtract(double d) {
    return new Complex(re-d, im);
  }

  /**
   * Returns the negative value of this complex number.
   */
  public Complex neg() {
    return new Complex(-re, -im);
  }

  /**
   * Multiplies the complex number with a double value.
   * @return The result of the multiplication

   */
  public Complex mul(double b) {
    return new Complex(re*b, im*b);
  }

  /**
   * Multiplies the complex number with another complex value.
   * @return The result of the multiplication
   */
  public Complex mul(Complex b) {
    return new Complex(re*b.re-im*b.im, im*b.re+re*b.im);
  }

  /**
   * Returns the result of dividing this complex number by the parameter.
   */
  public Complex div(Complex b) {
    // Adapted from Numerical Recipes in C - The Art of Scientific Computing
    // ISBN 0-521-43108-5
    double resRe, resIm;
    double r, den;
    if(Math.abs(b.re)>=Math.abs(b.im)) {
      r = b.im/b.re;
      den = b.re+r*b.im;
      resRe = (re+r*im)/den;
      resIm = (im-r*re)/den;
    } else {
      r = b.re/b.im;
      den = b.im+r*b.re;
      resRe = (re*r+im)/den;
      resIm = (im*r-re)/den;
    }
    return new Complex(resRe, resIm);
  }

  /**

   * Devide the complex number by a double value.

   * @return The result of the division

   */
  public Complex div(double d) {
    return new Complex(re/d, im/d);
  }

  /**

   * Invert the complex number.

   * @return The result of the inversion

   */

  /**  */
  public Complex invert() {
    double r = re*re+im*im;
    return new Complex(re/r, -im/r);
  }

  /**
   * Conjugats the complex number
   */
  public Complex conjugate() {
    return new Complex(re, -im);
  }

  /**
   * Returns the value of this complex number raised to the power
   * of a real component (in double precision).<p>
   * This method considers special cases where a simpler algorithm
   * would return "ugly" results.<br>
   * For example when the expression (-1e40)^0.5 is evaluated without
   * considering the special case, the argument of the base is the
   * double number closest to pi. When sin and cos are used for the
   * final evaluation of the result, the slight difference of the
   * argument from pi causes a non-zero value for the real component
   * of the result. Because the value of the base is so high, the error
   * is magnified.Although the error is normal for floating
   * point calculations, the consideration of commonly occurring special
   * cases improves the accuracy and esthetics of the results.<p>
   * If you know a more elegant way to solve this problem, please let
   * me know at nathanfunk@hotmail.com .

   */
  public Complex power(double exponent) {
    // z^exp = abs(z)^exp * (cos(exp*arg(z)) + i*sin(exp*arg(z)))
    double scalar = Math.pow(abs(), exponent);
    boolean specialCase = false;
    int factor = 0;
    // consider special cases to avoid floating point errors
    // for power expressions such as (-1e20)^2
    if((im==0)&&(re<0)) {
      specialCase = true;
      factor = 2;
    }
    if((re==0)&&(im>0)) {
      specialCase = true;
      factor = 1;
    }
    if((re==0)&&(im<0)) {
      specialCase = true;
      factor = -1;
    }
    if(specialCase&&(factor*exponent==(int) (factor*exponent))) {
      short[] cSin = {0, 1, 0, -1}; // sin of 0, pi/2, pi, 3pi/2
      short[] cCos = {1, 0, -1, 0}; // cos of 0, pi/2, pi, 3pi/2
      int x = ((int) (factor*exponent))%4;
      if(x<0) {
        x = 4+x;
      }
      return new Complex(scalar*cCos[x], scalar*cSin[x]);
    }
    double temp = exponent*arg();
    return new Complex(scalar*Math.cos(temp), scalar*Math.sin(temp));
  }

  /**
   * Returns the value of this complex number raised to the power of
   * a complex exponent
   */
  public Complex power(Complex exponent) {
    if(exponent.im==0) {
      return power(exponent.re);
    }
    double temp1Re = Math.log(abs());
    double temp1Im = arg();
    double temp2Re = (temp1Re*exponent.re)-(temp1Im*exponent.im);
    double temp2Im = (temp1Re*exponent.im)+(temp1Im*exponent.re);
    double scalar = Math.exp(temp2Re);
    return new Complex(scalar*Math.cos(temp2Im), scalar*Math.sin(temp2Im));
  }

  /**
   * Returns e to the power of the complex number
   */
  public Complex exp() {
    double exp_x = StrictMath.exp(re);
    return new Complex(exp_x*StrictMath.cos(im), exp_x*StrictMath.sin(im));
  }

  /**
   * Returns the logarithm of this complex number.
   */
  public Complex log() {
    return new Complex(Math.log(abs()), arg());
  }

  /**
   * Calculates the square root of this object.
   */
  public Complex sqrt() {
    Complex c;
    double absRe, absIm, w, r;
    if((re==0)&&(im==0)) {
      c = new Complex(0, 0);
    } else {
      absRe = Math.abs(re);
      absIm = Math.abs(im);
      if(absRe>=absIm) {
        r = absIm/absRe;
        w = Math.sqrt(absRe)*Math.sqrt(0.5*(1.0+Math.sqrt(1.0+r*r)));
      } else {
        r = absRe/absIm;
        w = Math.sqrt(absIm)*Math.sqrt(0.5*(r+Math.sqrt(1.0+r*r)));
      }
      if(re>=0) {
        c = new Complex(w, im/(2.0*w));
      } else {
        if(im<0) {
          w = -w;
        }
        c = new Complex(im/(2.0*w), w);
      }
    }
    return c;
  }

  //------------------------------------------------------------------------
  // Trigonometric functions

  /**
   * Returns the sine of this complex number.
   */
  public Complex sin() {
    double izRe, izIm;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    double scalar;
    //  sin(z)  =  ( exp(i*z) - exp(-i*z) ) / (2*i)
    izRe = -im;
    izIm = re;
    // first exp
    scalar = Math.exp(izRe);
    temp1Re = scalar*Math.cos(izIm);
    temp1Im = scalar*Math.sin(izIm);
    // second exp
    scalar = Math.exp(-izRe);
    temp2Re = scalar*Math.cos(-izIm);
    temp2Im = scalar*Math.sin(-izIm);
    temp1Re -= temp2Re;
    temp1Im -= temp2Im;
    return new Complex(0.5*temp1Im, -0.5*temp1Re);
  }

  /**

   * Returns the cosine of this complex number.

   */
  public Complex cos() {
    double izRe, izIm;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    double scalar;
    //  cos(z)  =  ( exp(i*z) + exp(-i*z) ) / 2
    izRe = -im;
    izIm = re;
    // first exp
    scalar = Math.exp(izRe);
    temp1Re = scalar*Math.cos(izIm);
    temp1Im = scalar*Math.sin(izIm);
    // second exp
    scalar = Math.exp(-izRe);
    temp2Re = scalar*Math.cos(-izIm);
    temp2Im = scalar*Math.sin(-izIm);
    temp1Re += temp2Re;
    temp1Im += temp2Im;
    return new Complex(0.5*temp1Re, 0.5*temp1Im);
  }

  /**
   * Returns the tangent of this complex number.
   */
  public Complex tan() {
    // tan(z) = sin(z)/cos(z)
    double izRe, izIm;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    double scalar;
    Complex sinResult, cosResult;
    //  sin(z)  =  ( exp(i*z) - exp(-i*z) ) / (2*i)
    izRe = -im;
    izIm = re;
    // first exp
    scalar = Math.exp(izRe);
    temp1Re = scalar*Math.cos(izIm);
    temp1Im = scalar*Math.sin(izIm);
    // second exp
    scalar = Math.exp(-izRe);
    temp2Re = scalar*Math.cos(-izIm);
    temp2Im = scalar*Math.sin(-izIm);
    temp1Re -= temp2Re;
    temp1Im -= temp2Im;
    sinResult = new Complex(0.5*temp1Re, 0.5*temp1Im);
    //  cos(z)  =  ( exp(i*z) + exp(-i*z) ) / 2
    izRe = -im;
    izIm = re;
    // first exp
    scalar = Math.exp(izRe);
    temp1Re = scalar*Math.cos(izIm);
    temp1Im = scalar*Math.sin(izIm);
    // second exp
    scalar = Math.exp(-izRe);
    temp2Re = scalar*Math.cos(-izIm);
    temp2Im = scalar*Math.sin(-izIm);
    temp1Re += temp2Re;
    temp1Im += temp2Im;
    cosResult = new Complex(0.5*temp1Re, 0.5*temp1Im);
    return sinResult.div(cosResult);
  }

  //------------------------------------------------------------------------
  // Inverse trigonometric functions
  public Complex asin() {
    Complex result;
    double tempRe, tempIm;
    //  asin(z)  =  -i * log(i*z + sqrt(1 - z*z))
    tempRe = 1.0-((re*re)-(im*im));
    tempIm = 0.0-((re*im)+(im*re));
    result = new Complex(tempRe, tempIm);
    result = result.sqrt();
    result.re += -im;
    result.im += re;
    tempRe = Math.log(result.abs());
    tempIm = result.arg();
    result.re = tempIm;
    result.im = -tempRe;
    return result;
  }

  public Complex acos() {
    Complex result;
    double tempRe, tempIm;
    //  acos(z)  =  -i * log( z + i * sqrt(1 - z*z) )
    tempRe = 1.0-((re*re)-(im*im));
    tempIm = 0.0-((re*im)+(im*re));
    result = new Complex(tempRe, tempIm);
    result = result.sqrt();
    tempRe = -result.im;
    tempIm = result.re;
    result.re = re+tempRe;
    result.im = im+tempIm;
    tempRe = Math.log(result.abs());
    tempIm = result.arg();
    result.re = tempIm;
    result.im = -tempRe;
    return result;
  }

  public Complex atan() {
    // atan(z) = -i/2 * log((i-z)/(i+z))
    double tempRe, tempIm;
    Complex result = new Complex(-re, 1.0-im);
    tempRe = re;
    tempIm = 1.0+im;
    result = result.div(new Complex(tempRe, tempIm));
    tempRe = Math.log(result.abs());
    tempIm = result.arg();
    result.re = 0.5*tempIm;
    result.im = -0.5*tempRe;
    return result;
  }

  //------------------------------------------------------------------------
  // Hyperbolic trigonometric functions
  public Complex sinh() {
    double scalar;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    //  sinh(z)  =  ( exp(z) - exp(-z) ) / 2
    // first exp
    scalar = Math.exp(re);
    temp1Re = scalar*Math.cos(im);
    temp1Im = scalar*Math.sin(im);
    // second exp
    scalar = Math.exp(-re);
    temp2Re = scalar*Math.cos(-im);
    temp2Im = scalar*Math.sin(-im);
    temp1Re -= temp2Re;
    temp1Im -= temp2Im;
    return new Complex(0.5*temp1Re, 0.5*temp1Im);
  }

  public Complex cosh() {
    double scalar;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    //  cosh(z)  =  ( exp(z) + exp(-z) ) / 2
    // first exp
    scalar = Math.exp(re);
    temp1Re = scalar*Math.cos(im);
    temp1Im = scalar*Math.sin(im);
    // second exp
    scalar = Math.exp(-re);
    temp2Re = scalar*Math.cos(-im);
    temp2Im = scalar*Math.sin(-im);
    temp1Re += temp2Re;
    temp1Im += temp2Im;
    return new Complex(0.5*temp1Re, 0.5*temp1Im);
  }

  public Complex tanh() {
    double scalar;
    double temp1Re, temp1Im;
    double temp2Re, temp2Im;
    Complex sinRes, cosRes;
    //  tanh(z)  =  sinh(z) / cosh(z)
    scalar = Math.exp(re);
    temp1Re = scalar*Math.cos(im);
    temp1Im = scalar*Math.sin(im);
    scalar = Math.exp(-re);
    temp2Re = scalar*Math.cos(-im);
    temp2Im = scalar*Math.sin(-im);
    temp1Re -= temp2Re;
    temp1Im -= temp2Im;
    sinRes = new Complex(0.5*temp1Re, 0.5*temp1Im);
    scalar = Math.exp(re);
    temp1Re = scalar*Math.cos(im);
    temp1Im = scalar*Math.sin(im);
    scalar = Math.exp(-re);
    temp2Re = scalar*Math.cos(-im);
    temp2Im = scalar*Math.sin(-im);
    temp1Re += temp2Re;
    temp1Im += temp2Im;
    cosRes = new Complex(0.5*temp1Re, 0.5*temp1Im);
    return sinRes.div(cosRes);
  }

  //------------------------------------------------------------------------
  // Inverse hyperbolic trigonometric functions
  public Complex asinh() {
    Complex result;
    //  asinh(z)  =  log(z + sqrt(z*z + 1))
    result = new Complex(((re*re)-(im*im))+1, (re*im)+(im*re));
    result = result.sqrt();
    result.re += re;
    result.im += im;
    double temp = result.arg();
    result.re = Math.log(result.abs());
    result.im = temp;
    return result;
  }

  public Complex acosh() {
    Complex result;
    //  acosh(z)  =  log(z + sqrt(z*z - 1))
    result = new Complex(((re*re)-(im*im))-1, (re*im)+(im*re));
    result = result.sqrt();
    result.re += re;
    result.im += im;
    double temp = result.arg();
    result.re = Math.log(result.abs());
    result.im = temp;
    return result;
  }

  public Complex atanh() {
    //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )
    double tempRe, tempIm;
    Complex result = new Complex(1.0+re, im);
    tempRe = 1.0-re;
    tempIm = -im;
    result = result.div(new Complex(tempRe, tempIm));
    tempRe = Math.log(result.abs());
    tempIm = result.arg();
    result.re = 0.5*tempRe;
    result.im = 0.5*tempIm;
    return result;
  }

  /**
   * Returns <tt>true</tt> if either the real or imaginary component of this
   * <tt>Complex</tt> is an infinite value.
   * <p>
   * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is infinite; <tt>false</tt>, otherwise.
   * <p>
   */
  public boolean isInfinite() {
    return(Double.isInfinite(re)||Double.isInfinite(im));
  } // end isInfinite()

  /**
   * Returns <tt>true</tt> if either the real or imaginary component of this
   * <tt>Complex</tt> is a Not-a-Number (<tt>NaN</tt>) value.
   * <p>
   * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is <tt>NaN</tt>; <tt>false</tt>, otherwise.
   * <p>
   */
  public boolean isNaN() {
    return(Double.isNaN(re)||Double.isNaN(im));
  } // end isNaN()

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
