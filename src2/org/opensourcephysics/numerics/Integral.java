/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Class Integral defines various integration algorithms.
 * This class cannot be subclassed or instantiated because all methods are static.
 *
 * @author Wolfgang Christian
 */
public final class Integral {
  static final int MAX_ITERATIONS = 15;

  private Integral() {} // prohibit instantiation because all methods are static

  /**
   * Integrates the function using the trapezoidal method.
   *
   * @param f the function
   * @param start the first ordinate.
   * @param stop the last ordinate.
   * @param n the number of partitions
   * @param tol  relative tolerance
   *
   * @return the integral
   */
  public static double trapezoidal(final Function f, double start, double stop, int n, final double tol) {
    double step = stop-start;
    int sign = (step<0) ? -1 : 1;
    if(sign<1) {
      step = -step;
      double temp = start;
      start = stop;
      stop = temp;
    }
    int iterations = 0;
    double sum = (f.evaluate(stop)+f.evaluate(start))*step*0.5;
    double oldSum;
    do {
      oldSum = sum;
      double x = start+0.5*step;
      double newSum = 0;
      while(x<stop) {
        newSum += f.evaluate(x);
        x += step;
      }
      sum = (step*newSum+sum)*0.5;
      step *= 0.5;
      iterations++;
      n /= 2;
    } while((n>0)||((iterations<MAX_ITERATIONS)&&(Util.relativePrecision(Math.abs(sum-oldSum), sum)>tol)));
    return sign*sum;
  }

  /**
   * Numerical integration using Simpson's rule.
   *
   * @param f a function.
   * @param start the first ordinate.
   * @param stop the last ordinate.
   * @param n the number of partitions
   * @return the integral
   */
  public static double simpson(final Function f, final double start, final double stop, final int n) throws IllegalArgumentException {
    if(n%2!=0) {
      throw new IllegalArgumentException("Number of partitions must be even in Simpson's method."); //$NON-NLS-1$
    }
    double sumOdd = 0.0, sumEven = 0.0, x = start;
    final double h = (stop-start)/(2*n);
    for(int i = 0; i<n-1; i++) {
      sumOdd += f.evaluate(x+h);
      sumEven += f.evaluate(x+2*h);
      x += 2.0*h;
    }
    sumOdd += f.evaluate(x+h);
    return h/3.0*(f.evaluate(start)+4.0*sumOdd+2.0*sumEven+f.evaluate(stop));
  }

  /**
   * Numerical integration using Simpson's rule.
   *
   * @param f the function
   * @param start the first ordinate.
   * @param stop the last ordinate.
   * @param n minimum number of partitions
   * @param tol  relative tolerance
   *
   * @return the integral
   */
  public static double simpson(final Function f, double start, double stop, int n, final double tol) {
    double step = stop-start;
    int sign = (step<0) ? -1 : 1;
    if(sign<1) {
      step = -step;
      double temp = start;
      start = stop;
      stop = temp;
    }
    int iterations = 0;
    double sum = (f.evaluate(stop)+f.evaluate(start))*step*0.5;
    double result = sum;
    double oldSum, oldResult = result;
    do {
      double x = start+0.5*step;
      oldSum = sum;
      double newSum = 0;
      while(x<stop) {
        newSum += f.evaluate(x);
        x += step;
      }
      sum = (step*newSum+sum)*0.5;
      step *= 0.5;
      iterations++;
      oldResult = result;
      result = (4*sum-oldSum)/3.0;
      n /= 2;
    } while((n>0)||((iterations<MAX_ITERATIONS)&&(Util.relativePrecision(Math.abs(result-oldResult), result)>tol)));
    return sign*result;
  }

  /**
   * Integrates the function using Romberg's algorithm based on Richardson's deferred approach.
   *
   * @param f the function
   * @param a
   * @param b
   * @param n
   * @param tol tolerance
   *
   * @return the integral
   */
  static public double romberg(Function f, double a, double b, int n, double tol) {
    if(a==b) {
      return(0);
    }
    if(tol<=0) {
      return Double.NaN; // eps must be positive
    }
    double[] coef = new double[MAX_ITERATIONS];
    double h = (b-a)/n; // starting value for step size
    // first row
    coef[0] = .5*(f.evaluate(a)+f.evaluate(b));
    for(int k = 1; k<n; k++) {
      coef[0] += f.evaluate(a+k*h);
    }
    coef[0] *= h;
    for(int j = 1; j<MAX_ITERATIONS; j++) {
      h /= 2;
      double c0 = coef[0];
      coef[0] = coef[j] = 0;
      for(int k = 0; k<n; k++) { /* further quadrature */
        coef[0] += f.evaluate(a+(2*k+1)*h);
      }
      coef[0] = .5*c0+h*coef[0];
      int inc = 1;
      for(int k = 1; k<=j; k++) {
        inc *= 4;
        double Lk = coef[k];
        coef[k] = (inc*coef[k-1]-c0)/(inc-1);
        c0 = Lk;
      }
      if(Util.relativePrecision(Math.abs(coef[j]-coef[j-1]), coef[j])<tol) {
        // Math.abs(coef[j] - coef[j - 1]) is est error
        return coef[j];
      }
      n *= 2;
    }
    return Double.NaN; /* accuracy not achieved */
  }

  /**
   * Uses Simpson's rule to find the area of an array representing a
   * function that's been evaluated at N intervals of size h, where N is
   * an odd integer.
   *
   * Example of usage:
   *    int N=27;
   *    x=new double[N]; f=new double[N];
   *    double a=0, b=5, h=(b-a)/(N-1);
   *    for (int i=0; i< N;i++){
   *      x[i]=a+i*h;
   *      f[i]=x[i]*Math.exp(x[i]);
   *    }
   *     double sum=Simp.Simp(f,h);
   *
   * Results: sum=594.6615858178942
   * @Author J E Hasbun 2007
   */
  public static double simpson(double f[], double h) {
    //Simpson's rule for numerical integration
    //f is an odd array of evaluated functions in steps h
    int ip = f.length; //must be an odd number
    double sumOdd = 0.0, sumEven = 0.0;
    for(int i = 1; i<ip-1; i += 2) {
      sumOdd += f[i];
      sumEven += f[i+1];
    }
    return(4.0*sumOdd+2.0*sumEven+f[0]-f[ip-1])*h/3.0;
  }

  /**
   * Computes the integral of the function using an ODE solver.
   *
   * @param f the function
   * @param start
   * @param stop
   * @param tol  relative tolerance
   *
   * @return the integral
   */
  static public double ode(final Function f, final double start, final double stop, final double tol) {
    ODE ode = new FunctionRate(f, start);
    RK45MultiStep ode_method = new RK45MultiStep(ode); // mimics a fixed size solver
    // ODEMultistepSolver ode_method= new ODEMultistepSolver(ode);  // mimics a fixed size solver
    ode_method.setTolerance(tol);
    ode_method.initialize(stop-start); // a fixed step size method
    ode_method.step();
    return ode.getState()[0];
  }

  /**
   * Fills a data array with the integral of the given function.
   *
   * @param f Function    to be integrated
   * @param start double  start of integral
   * @param stop double   end of integral
   * @param tol double    computation tolerance
   * @param n int         number of data points
   * @return double[][]
   */
  static public double[][] fillArray(final Function f, final double start, final double stop, final double tol, final int n) {
    double[][] data = new double[2][n];
    return fillArray(f, start, stop, tol, data);
  }

  /**
   * Fills the given data array with the intgral of the given function.
   * @param f Function   to be integrated
   * @param start double start of integral
   * @param stop double  end of integral
   * @param tol double   computation tolerance
   * @param data double[][]
   * @return double[][]
   */
  static public double[][] fillArray(Function f, double start, double stop, double tol, double[][] data) {
    ODE ode = new FunctionRate(f, start);
    ODEAdaptiveSolver ode_method = new ODEMultistepSolver(ode); // must be a fixed step size algorithm
    ode_method.setTolerance(tol);
    double dx = 1;
    int n = data[0].length;
    if(n>1) {
      dx = (stop-start)/(n-1);
    }
    ode_method.setStepSize(dx);
    for(int i = 0; i<n; i++) {
      data[0][i] = ode.getState()[1];
      data[1][i] = ode.getState()[0];
      ode_method.step();
    }
    return data;
  }

  static private final class FunctionRate implements ODE {
    double state[];
    Function f;

    private FunctionRate(Function _f, double start) {
      state = new double[2];
      state[0] = 0;     // integral
      state[1] = start; // independent variable
      f = _f;
    }

    public double[] getState() {
      return state;
    }

    public void getRate(double[] state, double rate[]) {
      rate[0] = f.evaluate(state[1]); // integral
      rate[1] = 1;                    // indepenent variable
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
