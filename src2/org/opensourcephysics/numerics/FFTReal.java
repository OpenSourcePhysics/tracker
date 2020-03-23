/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * FFTReal computes the discrete Fourier coefficients
 *          a[0], ...., a[N/2]  and  b[1], ...., b[N/2 - 1]
 * of the discrete partial Fourier sum
 *    a[0] + a[1]*cos(N/2*omega*x)
 *    + Sum (k=1,2,...,N/2-1) (a[2*k] * cos(k * omega * x) + a[2*k+1] * sin(k * omega * x))
 * given real functional values y[0], ...., y[N-1].
 *
 * Adapted by W. Christian for use in the OSP project.
 *
 * @author Bruce R. Miller bruce.miller@nist.gov
 * @author Contribution of the National Institute of Standards and Technology,
 * @author Derived from GSL (Gnu Scientific Library)
 * @author GSL's FFT Code by Brian Gough bjg@vvv.lanl.gov
 * @author Since GSL is released under
 * @author <H HREF="http://www.gnu.org/copyleft/gpl.html">GPL</A>,
 * @author this class must also be.
 *
 * @version 1.0
 */
public class FFTReal {
  static final double PI2 = 2*Math.PI;
  int n;               // number of data points
  FFT fft = new FFT(); // complex fft that does the computation

  /**
   * Constructs a real FFT transformation for n data points.
   */
  public FFTReal() {
    setN(2); // avoids null pointer exceptions
  }

  /**
   * Constructs a real FFT transformation for n data points.
   *
   * @param n the number of data points
   */
  public FFTReal(int n) {
    setN(n);
  }

  /**
   * Sets the number of data points.
   *
   * @param n int
   */
  public void setN(int n) {
    if(n%2!=0) {
      throw new IllegalArgumentException(n+" is not even"); //$NON-NLS-1$
    }
    this.n = n;
    fft.setN(n/2);
  }

  /**
   * Gets the number of data points.
   *
   * @return int
   */
  public int getN() {
    return n;
  }

  /**
   * Computes the Fast Fourier Transform of the data leaving the result in data.
   *
   * The given array is returned after it has been transformed.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] transform(double data[]) {
    if(data.length!=n) {
      setN(data.length);
    }
    fft.transform(data);
    shuffle(data, +1);
    return data;
  }

  /**
   * Computes the (unnomalized) inverse FFT of data, leaving it in place.
   *
   * The given array is returned after it has been transformed.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] backtransform(double data[]) {
    if(data.length!=n) {
      setN(data.length);
    }
    shuffle(data, -1);
    fft.backtransform(data);
    return data;
  }

  /**
   * Computes the (nomalized) inverse FFT of data, leaving it in place.
   *
   * The given array is returned after it has been transformed.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] inverse(double data[]) {
    backtransform(data);
    /* normalize inverse fft with 2/n */
    double norm = 2.0/(n);
    for(int i = 0; i<n; i++) {
      data[i] *= norm;
    }
    return data;
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * Data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalFreq(double delta) {
    int n = this.n/2;
    double[] freq = new double[n];
    double f = 0, df = 0.5/n/delta;
    for(int i = 0; i<n; i++) {
      freq[i] = f;
      f += df;
    }
    return freq;
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
   *
   * @param xmin
   * @param xmax
   * @return the array of frequencies
   */
  public double[] getNaturalFreq(double xmin, double xmax) {
    return getNaturalFreq((xmax-xmin)/(n-n%2));
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * Data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalOmega(double delta) {
    return getNaturalFreq(delta/PI2);
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
   *
   * @param xmin
   * @param xmax
   * @return the array of frequencies
   */
  public double[] getNaturalOmega(double xmin, double xmax) {
    return getNaturalFreq((xmax-xmin)/(n-n%2)/PI2);
  }

  /**
   * Rearrage the coefficients.
   * @param data double[]
   * @param sign int
   */
  private void shuffle(double data[], int sign) {
    int nh = n/2;
    int nq = n/4;
    if(n==6) {
      nq = 2;
    }
    double c1 = 0.5, c2 = -0.5*sign;
    double theta = sign*Math.PI/nh;
    double wtemp = Math.sin(0.5*theta);
    double wpr = -2.0*wtemp*wtemp;
    double wpi = -Math.sin(theta);
    double wr = 1.0+wpr;
    double wi = wpi;
    for(int i = 1; i<nq; i++) {
      int i1 = 2*i;
      int i3 = n-i1;
      double h1r = c1*(data[i1]+data[i3]);
      double h1i = c1*(data[i1+1]-data[i3+1]);
      double h2r = -c2*(data[i1+1]+data[i3+1]);
      double h2i = c2*(data[i1]-data[i3]);
      data[i1] = h1r+wr*h2r-wi*h2i;
      data[i1+1] = h1i+wr*h2i+wi*h2r;
      data[i3] = h1r-wr*h2r+wi*h2i;
      data[i3+1] = -h1i+wr*h2i+wi*h2r;
      wtemp = wr;
      wr += wtemp*wpr-wi*wpi;
      wi += wtemp*wpi+wi*wpr;
    }
    double d0 = data[0];
    if(sign==1) {
      data[0] = d0+data[1];
      data[1] = d0-data[1];
    } else {
      data[0] = c1*(d0+data[1]);
      data[1] = c1*(d0-data[1]);
    }
    if(n%4==0) {
      data[nh+1] *= -1;
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
