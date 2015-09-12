/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

/**
 * This implements an algorithm for finding a second derivative.
 * 
 * Derivation of the 5-point algorithm:
 * 		1. Find least-square fit of parabola y = a + b*x + c*x^2.
 * 		2. Use eqn 3-86 from Parratt's Probability and Experimental Errors in Science (1961) to find best fit parameters.
 * 		3. Use n = 5 so the values of x are -2 thru +2 (units of delta_t), y are corresponding positions. 
 * 		4. Plug in to obtain c = (2*x[i+2] - x[i+1] - 2*x[i] - x[i-1] + 2*x[i-2])/14.
 * 		5. Use derivative expressions y' = b + 2cx and y'' = 2c, and scale y'' by dt^2. 
 * 		6. Final result is accel[i] = (2*x[i+2] - x[i+1] - 2*x[i] - x[i-1] + 2*x[i-2])/(7*dt^2).
 * 
 * Other possible 5-point expressions (finite difference equations)--not used since more sensitive to noisy data(?):
 * 		1. accel[i] = (x[i+2] - 2*x[i] + x[i-2]) / (4*dt^2) 
 * 		2. accel[i] = (-x[i+2] +16*x[i+1] - 30*x[i] +16*x[i-1] - x[i-2]) / (12*dt^2)
 *
 * @author Douglas Brown
 */
public class SecondDerivative implements Derivative {

  // instance fields
  private int spill, start, step, count;
  private double[] xDeriv, yDeriv = new double[0];
  private Object[] result = new Object[4];

  /**
   * Evaluates the derivative.
   * 
   * Input data:
   *    data[0] = parameters (int[] {spill, start, stepsize, count})
   *    data[1] = xData (double[])
   *    data[2] = yData (double[])
   *    data[3] = validData (boolean[])
   *    
   * Returned result:
   *    result[0] = null
   *    result[1] = null
   *    result[2] = xDeriv (double[]) (invalid values are NaN)
   *    result[3] = yDeriv (double[]) (invalid values are NaN)
   *
   * @param data the input data
   * @return Object[] the result
   */
  public Object[] evaluate(Object[] data) {
    int[] params = (int[])data[0];
    spill = params[0];
    start = params[1];
    step = params[2];
    count = params[3];
    double[] x = (double[])data[1];
    double[] y = (double[])data[2];
    boolean[] valid = (boolean[])data[3];
    if (yDeriv.length != x.length) {
      result[2] = xDeriv = new double[x.length];
      result[3] = yDeriv = new double[x.length];
    }

    // get upper and lower index checking limits
    int lower = start;
    int upper = Math.min(start + step*(count-1), x.length);

    // find a at each step index from lower to upper
    outer:
    for (int i = lower; i <= upper; i+=step) {

      // derivative at i will be valid only if all step positions
      // between i-spill*step and i+spill*step are valid
      for (int j = i - spill*step; j <= i + spill*step; j+=step) {
        if (j < 0 || j >= valid.length || !valid[j])
        if (j < 0 || j >= valid.length || !valid[j]) {
        	if (i<valid.length) {
        		xDeriv[i] = Double.NaN;
        		yDeriv[i] = Double.NaN;
        	}
          continue outer;
        }
      }

      // use second derivative algorithm
      if (spill == 1) {
        xDeriv[i] = (+ x[i - step]
                     - 2 * x[i]
                     + x[i + step]);
        yDeriv[i] = (+ y[i - step]
                     - 2 * y[i]
                     + y[i + step]);
      } else {
        xDeriv[i] = (+ 2 * x[i - 2*step]
                     - x[i - step]
                     - 2 * x[i]
                     - x[i + step]
                     + 2 * x[i + 2*step]) / 7;
        yDeriv[i] = (+ 2 * y[i - 2*step]
                     - y[i - step]
                     - 2 * y[i]
                     - y[i + step]
                     + 2 * y[i + 2*step]) / 7;
      }

    }
    return result;
  }
}
