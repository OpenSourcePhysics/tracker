/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.text.DecimalFormat;

/**
 * A utility class for numerical analysis.
 * This class cannot be subclassed or instantiated because all methods are static.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public final class Util {
  public static final double SQRT2PI = Math.sqrt(2*Math.PI);
  public static final double LOG10 = Math.log(10);

  /** The default precision for numerical analysis. */
  public static final double defaultNumericalPrecision = Math.sqrt(Double.MIN_VALUE);

  /** Parser for simple arithmetic expressions. */
  private static SuryonoParser parser = new SuryonoParser(0);     // parser without variables
  // standard output formats
  static DecimalFormat format2 = new DecimalFormat("#0.00");      //$NON-NLS-1$
  static DecimalFormat format3 = new DecimalFormat("#0.000");     //$NON-NLS-1$
  static DecimalFormat format4 = new DecimalFormat("#0.0000");    //$NON-NLS-1$
  static DecimalFormat format_E2 = new DecimalFormat("0.00E0");   //$NON-NLS-1$
  static DecimalFormat format_E3 = new DecimalFormat("0.000E0");  //$NON-NLS-1$
  static DecimalFormat format_E4 = new DecimalFormat("0.0000E0"); //$NON-NLS-1$

  private Util() {}                                               // prohibit instantiation because all methods are static

  /**
   * Convert a double to a string, printing two decimal places.
   * @param d  Input double
   */
  static public String f2(double d) {
    return format2.format(d);
  }

  /**
   * Convert a double to a string, printing three decimal places.
   * @param d  Input double
   */
  static public String f3(double d) {
    return format3.format(d);
  }

  /**
   * Convert a double to a string, printing four decimal places.
   * @param d  Input double
   */
  static public String f4(double d) {
    return format4.format(d);
  }

  /**
   * Computes the relativePrecision except near zero where the absolute precision is returned.
   *
   * @param epsilon the absolute error
   * @param result  the result
   *
   * @return the relative error
   */
  public static double relativePrecision(final double epsilon, final double result) {
    return(result>defaultNumericalPrecision) ? epsilon/result : epsilon;
  }

  /**
   * Checks if an array is sorted.
   * Returns:
   *   Positive integer if array is sorted in increasing value.
   *   Negative integer if array is sorted in decreasing value.
   *   Zero if array is not sorted.
   *
   * @param array double[]
   * @return int 1,0,-1 based on sorting
   */
  public static int checkSorting(double[] array) {
    int sign = (array[0]<=array[array.length-1]) ? 1 : -1;
    for(int i = 1, n = array.length; i<n; i++) {
      switch(sign) {
         case -1 :
           if(array[i-1]<array[i]) {
             return 0; // not sorted
           }
           break;
         case 1 :
           if(array[i-1]>array[i]) {
             return 0; // not sorted
           }
      }
    }
    return sign;
  }

  /**
   * Gets the approximate range of a function within the given domain.
   *
   * The range is deterermiend by evaluating the function at n points and finding the minimum and maximum values.
   *
   * @param f Function
   * @param a double
   * @param b double
   * @param n int
   * @return double[]
   */
  public static double[] getRange(Function f, double a, double b, int n) {
    double min = f.evaluate(a);
    double max = f.evaluate(a);
    double x = a, dx = (b-a)/(n-1);
    for(int i = 1; i<n; i++) {
      double y = f.evaluate(x);
      min = Math.min(min, y);
      max = Math.max(max, y);
      x += dx;
    }
    return new double[] {min, max};
  }

  /**
   * Fills the given double[2][n] array with x and f(x) values on interval [start, stop].
   *
   * @param f Function
   * @param start double
   * @param stop double
   * @param data double[][]
   * @return double[][]
   */
  static public double[][] functionFill(Function f, double start, double stop, double[][] data) {
    double dx = 1;
    int n = data[0].length;
    if(n>1) {
      dx = (stop-start)/(n-1);
    }
    double x = start;
    for(int i = 0; i<n; i++) {
      data[0][i] = x;
      data[1][i] = f.evaluate(x);
      x += dx;
    }
    return data;
  }

  /**
   * Fills the given double[n] array with f(x) on the interval [start, stop].
   *
   * @param f Function
   * @param start double starting value of x
   * @param stop double  stopping value of x
   * @param data double[]
   * @return double[]
   */
  static public double[] functionFill(Function f, double start, double stop, double[] data) {
    double dx = 1;
    int n = data.length;
    if(n>1) {
      dx = (stop-start)/(n-1);
    }
    double x = start;
    for(int i = 0; i<n; i++) {
      data[i] = f.evaluate(x);
      x += dx;
    }
    return data;
  }

  /**
   * Computes the average value of a subset of an array.
   *
   * @param array the data to be averaged
   * @param start the index of the first point to be averaged
   * @param num   the total number of points to be averaged
   * @return
   */
  public static double computeAverage(double[] array, int start, int num) {
    double sum = 0;
    for(int i = start, stop = start+num; i<stop; i++) {
      sum += array[i];
    }
    return sum/num;
  }

  /**
   * Creates a function having a constant value.
   * @param c
   * @return
   */
  public static Function constantFunction(final double c) {
    return new Function() {
      public double evaluate(final double x) {
        return c;
      }

    };
  }

  /**
   * Creates a linear function with the given slope and intercept.
   *
   * @param m double  slope
   * @param b double  intercept
   * @return Function
   */
  public static Function linearFunction(final double m, final double b) {
    return new Function() {
      public double evaluate(final double x) {
        return m*x+b;
      }

    };
  }

  /**
   * Creates a Guassian (normal) distribution function.
   *
   * The distribution is normalized.
   * The full width at half maximum is 2*sigma*Math.sqrt(2 Math.log(2)) ~ 2.3548*sigma
   *
   * @param x0 double  center of the distribution
   * @param sigma double width of the distributuon
   * @return Function
   */
  public static Function gaussian(final double x0, final double sigma) {
    final double s2 = 2*sigma*sigma;
    return new Function() {
      public double evaluate(double x) {
        return Math.exp(-(x-x0)*(x-x0)/s2)/sigma/SQRT2PI;
      }

    };
  }

  /**
   * Evalautes a mathematical expression without variables.
   * @param str String
   * @return double
   */
  public static synchronized double evalMath(String str) {
    try {
      parser.parse(str);
      return parser.evaluate();
    } catch(ParserException ex) {}
    return Double.NaN;
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
