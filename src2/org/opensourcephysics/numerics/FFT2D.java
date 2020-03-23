/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * FFT2D computes the FFT of 2 dimensional complex, double precision data.
 *
 * This class has been copied from Bruce Miller's FFT package for use in the
 * Open Source Physics Project.  The original package contains code for other transformations
 * and other data types.
 *
 * The data is stored in a 1-dimensional array in Row-Major order.
 * The physical layout in the array data, of the mathematical data d[i,j] is as follows:
 * <PRE>
 *    Re(d[i,j]) = data[i*rowspan + 2*j]
 *    Im(d[i,j]) = data[i*rowspan + 2*j + 1]
 * </PRE>
 *     where <code>rowspan</code> must be at least 2*ncols (it defaults to 2*ncols).
 * The transformed data is returned in the original data array in
 * <a href="package-summary.html#wraparound">wrap-around</A> order along each dimension.
 *
 * @author Bruce R. Miller bruce.miller@nist.gov
 * @author Contribution of the National Institute of Standards and Technology,
 * @author not subject to copyright.
 */
public class FFT2D {
  static final double PI2 = 2*Math.PI;
  int nrows;
  int ncols;
  FFT rowFFT, colFFT;
  double[] acol, ccol;

  /**
   * Create an FFT for transforming nrows*ncols points of Complex, double precision
   *  data.
   * @param nrows
   * @param ncols
   */
  public FFT2D(int nrows, int ncols) {
    if((nrows<=0)||(ncols<=0)) {
      throw new IllegalArgumentException("The array dimensions >=0 : "+nrows+","+ncols); //$NON-NLS-1$ //$NON-NLS-2$
    }
    this.nrows = nrows;
    this.ncols = ncols;
    acol = new double[2*nrows];
    if(nrows%2==1) {
      ccol = new double[2*nrows]; // temp storage for center column if nrows is odd
    }
    rowFFT = new FFT(ncols);
    colFFT = ((nrows==ncols) ? rowFFT : new FFT(nrows));
  }

  protected void checkData(double data[], int rowspan) {
    if(rowspan<2*ncols) {
      throw new IllegalArgumentException("The row span "+rowspan+"is shorter than the row length "+2*ncols); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if(nrows*rowspan>data.length) {
      throw new IllegalArgumentException("The data array is too small for "+nrows+"x"+rowspan+" data.length="+data.length); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  /**
   * Compute the Fast Fourier Transform of data leaving the result in data.
   *  The array data must be dimensioned (at least) 2*nrows*ncols, consisting of
   *  alternating real and imaginary parts.
   * @param data
   */
  public void transform(double data[]) {
    transform_internal(data, 2*ncols);
  }

  /**
   * Compute the Fast Fourier Transform of data leaving the result in data.
   *  The array data must be dimensioned (at least) 2*nrows*ncols, consisting of
   *  alternating real and imaginary parts.
   * @param data
   * @param rowspan
   */
  void transform_internal(double data[], int rowspan) {
    checkData(data, rowspan);
    for(int i = 0; i<nrows; i++) {
      rowFFT.transform_internal(data, i*rowspan, 2, FFT.FORWARD);
    }
    for(int j = 0; j<ncols; j++) {
      colFFT.transform_internal(data, 2*j, rowspan, FFT.FORWARD);
    }
  }

  /**
   * Compute the (unnomalized) inverse FFT of data, leaving it in place.
   * @param data
   */
  public void backtransform(double data[]) {
    backtransform_internal(data, 2*ncols);
  }

  /**
   * Compute the (unnomalized) inverse FFT of data, leaving it in place.
   * @param data
   * @param rowspan
   */
  void backtransform_internal(double data[], int rowspan) {
    checkData(data, rowspan);
    for(int j = 0; j<ncols; j++) {
      colFFT.transform_internal(data, 2*j, rowspan, FFT.BACKWARD);
    }
    for(int i = 0; i<nrows; i++) {
      rowFFT.transform_internal(data, i*rowspan, 2, FFT.BACKWARD);
    }
  }

  /**
   * Compute the (nomalized) inverse FFT of data, leaving it in place.
   * @param data
   */
  public void inverse(double data[]) {
    inverse_internal(data, 2*ncols);
  }

  /**
   * Compute the (nomalized) inverse FFT of data, leaving it in place.
   * @param data
   * @param rowspan
   */
  void inverse_internal(double data[], int rowspan) {
    backtransform_internal(data, rowspan);
    double norm = 1.0/((double) nrows*ncols);
    for(int i = 0; i<nrows; i++) {
      data[i] *= norm;
    }
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * Data are separated by delta and there are n data points.
   *
   * @param delta
   * @param n  size of frequency array
   * @return  array of frequencies
   */
  double[] getNaturalFreq(double delta, int n) {
    double[] freq = new double[n];
    double f = -0.5/delta, df = -2*f/(n-n%2);
    for(int i = 0; i<n; i++) {
      freq[i] = f;
      f += df;
    }
    return freq;
  }

  /**
   * Gets the minimum frequency given the domain and the number of points.
   *
   * @param min double
   * @param max double
   * @param n int
   * @return double
   */
  public double getFreqMin(double min, double max, int n) {
    return -(n/2)/(max-min);
  }

  /**
   * Gets the maximum frequency given the domain and the number of points.
   *
   * @param min double
   * @param max double
   * @param n int
   * @return double
   */
  public double getFreqMax(double min, double max, int n) {
    return((n+1)/2-1)/(max-min);
  }

  /**
   * Gets an array containing the mode numbers in natural order.
   *
   * @return array of mode numbers
   */
  public double[] getNaturalModes(int n) {
    double[] bins = new double[n];
    double w = -(n-n%2)/2;
    for(int i = 0; i<n; i++) {
      bins[i] = w;
      w++;
    }
    return bins;
  }

  /**
   * Gets an array containing the mode numbers in wrap-around order.
   *
   * @return array of mode numbers
   */
  public double[] getWrappedModes(int n) {
    double[] bins = new double[n];
    for(int i = 0; i<n; i++) {
      bins[i] = (i<(n+1)/2) ? i : (i-n);
    }
    return bins;
  }

  /**
   * Gets an array containing the angular frequencies (wavenumbers) in natural order.
   * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
   *
   * @param xmin
   * @param xmax
   * @return the array of frequencies
   */
  public double[] getWrappedOmegaX(double xmin, double xmax) {
    return getWrappedFreq((xmax-xmin)/(nrows-nrows%2)/PI2, nrows);
  }

  /**
 * Gets an array containing the angular frequencies (wavenumbers) in natural order.
 * The first data point is at ymin and the last data point is at ymax.
 *
 * @param ymin
 * @param ymax
 * @return the array of frequencies
 */
  public double[] getWrappedOmegaY(double ymin, double ymax) {
    return getWrappedFreq((ymax-ymin)/(ncols-ncols%2)/PI2, ncols);
  }

  /**
   * Gets an array containing the frequencies in wrap-around order.
   * Samples in the data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getWrappedFreq(double delta, int n) {
    double[] freq = new double[n];
    double f = -0.5/delta, df = -2*f/(n-n%2);
    for(int i = 0; i<n; i++) {
      freq[i] = (i<(n+1)/2) ? i*df : (i-n)*df;
    }
    return freq;
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * Data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalFreqX(double delta) {
    return getNaturalFreq(delta, nrows);
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
   *
   * @param xmin
   * @param xmax
   * @return the array of frequencies
   */
  public double[] getNaturalFreqX(double xmin, double xmax) {
    return getNaturalFreq((xmax-xmin)/(nrows-nrows%2), nrows);
  }

  /**
   * Gets an array containing the angular frequencies (wavenumbers) in natural order.
   * Data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalOmegaX(double delta) {
    return getNaturalFreq(delta/PI2, nrows);
  }

  /**
   * Gets an array containing the angular frequencies (wavenumbers) in natural order.
   * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
   *
   * @param xmin
   * @param xmax
   * @return the array of frequencies
   */
  public double[] getNaturalOmegaX(double xmin, double xmax) {
    return getNaturalFreq((xmax-xmin)/(nrows-nrows%2)/PI2, nrows);
  }

  /**
 * Gets an array containing the frequencies in natural order if samples in the orginal data are
 * separated by delta in y.
 *
 * @param delta
 * @return the array of frequencies
 */
  public double[] getNaturalFreqY(double delta) {
    return getNaturalFreq(delta, ncols);
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * The first data point is at ymin and the last data point is at ymax.
   *
   * @param ymin
   * @param ymax
   * @return the array of frequencies
   */
  public double[] getNaturalFreqY(double ymin, double ymax) {
    return getNaturalFreq((ymax-ymin)/(ncols-ncols%2), ncols);
  }

  /**
   * Gets an array containing the frequencies in natural order if samples in the orginal data are
   * separated by delta in y.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalOmegaY(double delta) {
    return getNaturalFreq(delta/PI2, ncols);
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * The first data point is at ymin and the last data point is at ymax.
   *
   * @param ymin
   * @param ymax
   * @return the array of frequencies
   */
  public double[] getNaturalOmegaY(double ymin, double ymax) {
    return getNaturalFreq((ymax-ymin)/(ncols-ncols%2)/PI2, ncols);
  }

  /**
   * Reorder and normalize the transformed data from most negative frequency
   * to most positive frequency leaving the result in data.
   * @param data
   */
  public void toNaturalOrder(double data[]) {
    if(ccol!=null) {
      System.arraycopy(data, (ncols/2)*acol.length, ccol, 0, ccol.length); // save center column if ncols is odd
    }
    for(int i = 0; i<ncols/2; i++) {                                                      // swap columns
      int offset = i*acol.length;
      System.arraycopy(data, offset, acol, 0, acol.length);                               // save the i-th column data
      System.arraycopy(data, (ncols/2+i+ncols%2)*acol.length, data, offset, acol.length); // replace the i-th column data
      System.arraycopy(acol, 0, data, (ncols/2+i)*acol.length, acol.length);
    }
    if(ccol!=null) {
      System.arraycopy(ccol, 0, data, data.length-ccol.length, ccol.length);
    }
    for(int i = 0; i<ncols; i++) {                          // swap rows
      int n = acol.length/2;
      int offset = i*acol.length;
      System.arraycopy(data, offset, acol, 0, acol.length); // save a column of data
      System.arraycopy(acol, n+n%2, data, offset, n-n%2);   // copy the second half of the data into the first half
      System.arraycopy(acol, 0, data, offset+n-n%2, n+n%2); // copy the first half of the data into the second half
    }
    double norm = 1.0/((double) nrows*ncols);
    for(int i = 0, n = data.length; i<n; i++) { // normalize
      data[i] *= norm;
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
