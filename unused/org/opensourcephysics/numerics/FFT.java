/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * FFT computes FFT's of complex, double precision data of arbitrary length n.
 *
 *  This class has been copied from Bruce Miller's FFT package for use in the
 *  Open Source Physics Project.  The original package contains code for other transformations
 *  and other data types.
 *
 *  This class uses the Mixed Radix method; it has special methods to handle
 *  factors 2, 3, 4, 5, 6 and 7, as well as a general factor.
 *  <P>
 *  This algorithm appears to be faster than the Radix2 method, when both methods apply,
 *  but requires extra storage (which FFTComplex manages itself).
 *  <P>
 *  Complex data is represented by 2 double values in sequence: the real and imaginary
 *  parts.  Thus N data points are represented by a double array dimensioned to 2*N.
 *  The physical layout in the array data, of the mathematical data d[i] is as follows:
 * <PRE>
 *     Re(d[i]) = data[i0 + stride*i]
 *     Im(d[i]) = data[i0 + stride*i+1]
 * </PRE>
 *  The default offset, i0, is 0 and the stride is 2.
 *
 *  The transformed data is returned in the original data array in wrap-around order.
 *
 *  @author Bruce R. Miller bruce.miller@nist.gov
 *  @author Contribution of the National Institute of Standards and Technology,
 *  @author not subject to copyright.
 *  @author Derived from GSL (Gnu Scientific Library)
 *  @author GSL's FFT Code by Brian Gough bjg@vvv.lanl.gov
 *  @author Since GSL is released under
 *  @author <H HREF="http://www.gnu.org/copyleft/gpl.html">GPL</A>,
 *  @author this class must also be.
 */
public class FFT {
  static final double PI = Math.PI;
  static final double PI2 = 2*Math.PI;
  static final int FORWARD = -1;
  static final int BACKWARD = +1;
  int n;
  double scratch[];
  double norm = 1;

  /**
   * Constructs a complex FFT transformation for n complex data points.
   *
   * @param n the number of complex data points
   */
  public FFT(int n) {
    setN(n);
  }

  /**
   * Constructs a complex FFT transformation.
   */
  public FFT() {
    setN(1); // one data point to avoid null pointers
  }

  /**
   * Sets the number of complex data points.
   *
   * @param n int
   */
  public void setN(int n) {
    if(n<=0) {
      throw new IllegalArgumentException("The transform length must be >0 : "+n); //$NON-NLS-1$
    }
    this.n = n;
    norm = n;
    scratch = new double[2*n];
    setup_wavetable(n);
  }

  /**
   * Gets the number of complex data points.
   *
   * @return int
   */
  public int getN() {
    return n;
  }

  /**
   * Sets the normalization constant.
   *
   * The toNaturalOrder method normalizes data.
   *
   * @param norm double
   */
  public void setNormalization(double norm) {
    this.norm = norm;
  }

  /**
   * Gets the normalization constant.
   *
   * The toNaturalOrder method normalizes data.
   *
   * @return the normalization
   */
  public double getNormalization() {
    return norm;
  }

  /**
   * Computes the Fast Fourier Transform of data leaving the result in data.
   *
   * The given array is returned after it has been transformed.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] transform(double data[]) {
    if(data.length!=2*n) {
      if(data.length%2!=0) {
        throw new IllegalArgumentException("Number of points in array is not even"); //$NON-NLS-1$
      }
      setN(data.length/2);
    }
    transform_internal(data, 0, 2, FORWARD);
    return data;
  }

  /**
   * Computes the back Fast Fourier Transform of data leaving the result in data.
   * The given array is returned after it has been transformed.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] backtransform(double data[]) {
    if(data.length!=2*n) {
      if(data.length%2!=0) {
        throw new IllegalArgumentException("Number of points in array is not even"); //$NON-NLS-1$
      }
      setN(data.length/2);
    }
    transform_internal(data, 0, 2, BACKWARD);
    return data;
  }

  /**
   * Computes the (nomalized) inverse FFT of data, leaving it in place.
   * The frequency domain data must be in wrap-around order, and be stored
   * in the following locations:
   * <PRE>
   *    Re(D[i]) = data[i]
   *    Im(D[i]) = data[i+1]
   * </PRE>
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] inverse(double data[]) {
    backtransform(data);
    /* normalize inverse fft with 1/n */
    for(int i = 0, m = 2*n; i<m; i++) {
      data[i] /= n;
    }
    return data;
  }

  /**
   * Reorder the transformed data from most negative frequency
   * to most positive frequency leaving the result in data.
   *
   * Divides by the normalization to remove the FFT scaling.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] toNaturalOrder(double data[]) {
    System.arraycopy(data, 0, scratch, 0, 2*n);       // save the data
    System.arraycopy(scratch, n+n%2, data, 0, n-n%2); // copy the second half of the data into the first half
    System.arraycopy(scratch, 0, data, n-n%2, n+n%2); // copy the first half of the data into the second half
    if(norm==1) {
      return data;
    }
    for(int i = 0, m = 2*n; i<m; i++) { // normalize
      data[i] /= norm;
    }
    return data;
  }

  /**
   * Reorder the data using wraparound order.
   *
   * Multiplies by the normalization to reverse the toNaturalOrder method.
   *
   * @param data double[]  the data to be transformed
   * @return double[]      the data after the FFT
   */
  public double[] toWrapAroundOrder(double data[]) {
    System.arraycopy(data, 0, scratch, 0, 2*n);       // save the data
    System.arraycopy(scratch, n+n%2, data, 0, n-n%2); // copy the second half of the data into the first half
    System.arraycopy(scratch, 0, data, n-n%2, n+n%2); // copy the first half of the data into the second half
    if(norm==1) {
      return data;
    }
    for(int i = 0, m = 2*n; i<m; i++) { // remove normalization
      data[i] *= n;
    }
    return data;
  }

  /**
   * Gets an array containing the mode numbers in wrap-around order.
   *
   * @return the array of mode numbers
   */
  public double[] getWrappedModes() {
    double[] bins = new double[n];
    for(int i = 0; i<n; i++) {
      bins[i] = (i<(n+1)/2) ? i : (i-n);
    }
    return bins;
  }

  /**
   * Gets an array containing the angular frequencies (wavenumber) in wrap-around order.
   * Samples in the orginal data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getWrappedOmega(double delta) {
    return getWrappedFreq(delta/PI2);
  }

  /**
  * Gets an array containing the angular frequencies (wavenumber) in wrap-around order.
  * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
  *
  * @param xmin
  * @param xmax
  * @return the array of frequencies
  */
  public double[] getWrappedOmega(double xmin, double xmax) {
    return getWrappedFreq((xmax-xmin)/(n-n%2)/PI2);
  }

  /**
   * Gets an array containing the frequencies in wrap-around order.
   * Samples in the data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getWrappedFreq(double delta) {
    double[] freq = new double[n];
    double f = -0.5/delta, df = -2*f/(n-n%2);
    for(int i = 0; i<n; i++) {
      freq[i] = (i<(n+1)/2) ? i*df : (i-n)*df;
    }
    return freq;
  }

  /**
 * Gets an array containing the frequencies in wrap-around order.
 * The first data point is at xmin (tmin) and the last data point is at xmax (tmax).
 *
 * @param xmin
 * @param xmax
 * @return the array of frequencies
 */
  public double[] getWrappedFreq(double xmin, double xmax) {
    return getNaturalFreq((xmax-xmin)/(n-n%2));
  }

  /**
   * Gets an array containing the frequencies in natural order.
   * Data are separated by delta.
   *
   * @param delta
   * @return the array of frequencies
   */
  public double[] getNaturalFreq(double delta) {
    double[] freq = new double[n];
    double f = -0.5/delta, df = -2*f/(n-n%2);
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
  * Gets an array containing the mode numbers in natural order.
   *
  * @return the array of mode numbers
  */
  public double[] getNaturalModes() {
    double[] bins = new double[n];
    double w = -(n-n%2)/2;
    for(int i = 0; i<n; i++) {
      bins[i] = w;
      w++;
    }
    return bins;
  }

  /*
   * ______________________________________________________________________
   * Setting up the Wavetable
   */
  private int factors[];
  // Reversed the last 2 levels of the twiddle array compared to what the C version had.

  private double twiddle[][][];
  private int available_factors[] = {7, 6, 5, 4, 3, 2};

  private void setup_wavetable(int n) {
    if(n<=0) {
      throw new Error("length must be positive integer : "+n); //$NON-NLS-1$
    }
    this.n = n;
    factors = factor(n, available_factors);
    double d_theta = -2.0*PI/(n);
    int product = 1;
    twiddle = new double[factors.length][][];
    for(int i = 0; i<factors.length; i++) {
      int factor = factors[i];
      int product_1 = product;      /* product_1 = p_(i-1) */
      product *= factor;
      int q = n/product;
      twiddle[i] = new double[q+1][2*(factor-1)];
      double twid[][] = twiddle[i];
      for(int j = 1; j<factor; j++) {
        twid[0][2*(j-1)] = 1.0;
        twid[0][2*(j-1)+1] = 0.0;
      }
      for(int k = 1; k<=q; k++) {
        int m = 0;
        for(int j = 1; j<factor; j++) {
          // int m = (k*j*product_1) % n;
          m += k*product_1;
          m %= n;
          double theta = d_theta*m; /* d_theta*j*k*p_(i-1) */
          twid[k][2*(j-1)] = Math.cos(theta);
          twid[k][2*(j-1)+1] = Math.sin(theta);
        }
      }
    }
  }

  /*
   * ______________________________________________________________________
   * The main transformation driver
   */

  /**
   * Method transform_internal
   *
   * @param data
   * @param i0 offset
   * @param stride
   * @param sign  FORWARD or BACKWARD
   */
  void transform_internal(double data[], int i0, int stride, int sign) {
    if(n==1) {
      return; /* FFT of 1 data point is the identity */
    }
    double scratch[] = new double[2*n];
    int product = 1;
    int state = 0;
    double in[], out[];
    int istride, ostride;
    int in0, out0;
    for(int i = 0; i<factors.length; i++) {
      int factor = factors[i];
      product *= factor;
      if(state==0) {
        in = data;
        in0 = i0;
        istride = stride;
        out = scratch;
        out0 = 0;
        ostride = 2;
        state = 1;
      } else {
        in = scratch;
        in0 = 0;
        istride = 2;
        out = data;
        out0 = i0;
        ostride = stride;
        state = 0;
      }
      switch(factor) {
         case 2 :
           pass_2(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         case 3 :
           pass_3(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         case 4 :
           pass_4(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         case 5 :
           pass_5(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         case 6 :
           pass_6(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         case 7 :
           pass_7(i, in, in0, istride, out, out0, ostride, sign, product);
           break;
         default :
           pass_n(i, in, in0, istride, out, out0, ostride, sign, factor, product);
      }
    }
    if(state==1) { /* copy results back from scratch to data */
      for(int i = 0; i<n; i++) {
        data[i0+stride*i] = scratch[2*i];
        data[i0+stride*i+1] = scratch[2*i+1];
      }
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_2
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_2(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 2;
    int m = n/factor;
    int q = n/product;
    int product_1 = product/factor;
    int di = istride*m;
    int dj = ostride*product_1;
    int i = in0, j = out0;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w_real = twids[0];
      double w_imag = -sign*twids[1];
      for(k1 = 0; k1<product_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        i += istride;
        /* compute x = W(2) z */
        /* apply twiddle factors */
        /* out0 = 1 * (z0 + z1) */
        out[j] = z0_real+z1_real;
        out[j+1] = z0_imag+z1_imag;
        /* out1 = w * (z0 - z1) */
        x_real = z0_real-z1_real;
        x_imag = z0_imag-z1_imag;
        out[j+dj] = w_real*x_real-w_imag*x_imag;
        out[j+dj+1] = w_real*x_imag+w_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_3
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_3(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 3;
    int m = n/factor;
    int q = n/product;
    int product_1 = product/factor;
    //int jump = (factor-1)*product_1;
    double tau = sign*Math.sqrt(3.0)/2.0;
    int di = istride*m;
    int dj = ostride*product_1;
    int i = in0, j = out0;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w1_real = twids[0];
      double w1_imag = -sign*twids[1];
      double w2_real = twids[2];
      double w2_imag = -sign*twids[3];
      for(k1 = 0; k1<product_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        double z2_real = in[i+2*di];
        double z2_imag = in[i+2*di+1];
        i += istride;
        /* compute x = W(3) z */
        /* t1 = z1 + z2 */
        double t1_real = z1_real+z2_real;
        double t1_imag = z1_imag+z2_imag;
        /* t2 = z0 - t1/2 */
        double t2_real = z0_real-t1_real/2.0;
        double t2_imag = z0_imag-t1_imag/2.0;
        /* t3 = (+/-) sin(pi/3)*(z1 - z2) */
        double t3_real = tau*(z1_real-z2_real);
        double t3_imag = tau*(z1_imag-z2_imag);
        /* apply twiddle factors */
        /* out0 = 1 * (z0 + t1) */
        out[j] = z0_real+t1_real;
        out[j+1] = z0_imag+t1_imag;
        /* out1 = w1 * (t2 + i t3) */
        x_real = t2_real-t3_imag;
        x_imag = t2_imag+t3_real;
        out[j+dj] = w1_real*x_real-w1_imag*x_imag;
        out[j+dj+1] = w1_real*x_imag+w1_imag*x_real;
        /* out2 = w2 * (t2 - i t3) */
        x_real = t2_real+t3_imag;
        x_imag = t2_imag-t3_real;
        out[j+2*dj] = w2_real*x_real-w2_imag*x_imag;
        out[j+2*dj+1] = w2_real*x_imag+w2_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_4
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_4(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 4;
    int m = n/factor;
    int q = n/product;
    int p_1 = product/factor;
    //int jump = (factor-1)*p_1;
    int i = in0, j = out0;
    int di = istride*m;
    int dj = ostride*p_1;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w1_real = twids[0];
      double w1_imag = -sign*twids[1];
      double w2_real = twids[2];
      double w2_imag = -sign*twids[3];
      double w3_real = twids[4];
      double w3_imag = -sign*twids[5];
      for(k1 = 0; k1<p_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        double z2_real = in[i+2*di];
        double z2_imag = in[i+2*di+1];
        double z3_real = in[i+3*di];
        double z3_imag = in[i+3*di+1];
        i += istride;
        /* compute x = W(4) z */
        /* t1 = z0 + z2 */
        double t1_real = z0_real+z2_real;
        double t1_imag = z0_imag+z2_imag;
        /* t2 = z1 + z3 */
        double t2_real = z1_real+z3_real;
        double t2_imag = z1_imag+z3_imag;
        /* t3 = z0 - z2 */
        double t3_real = z0_real-z2_real;
        double t3_imag = z0_imag-z2_imag;
        /* t4 = (+/-) (z1 - z3) */
        double t4_real = sign*(z1_real-z3_real);
        double t4_imag = sign*(z1_imag-z3_imag);
        /* apply twiddle factors */
        /* out0 = 1 * (t1 + t2) */
        out[j] = t1_real+t2_real;
        out[j+1] = t1_imag+t2_imag;
        /* out1 = w1 * (t3 + i t4) */
        x_real = t3_real-t4_imag;
        x_imag = t3_imag+t4_real;
        out[j+dj] = w1_real*x_real-w1_imag*x_imag;
        out[j+dj+1] = w1_real*x_imag+w1_imag*x_real;
        /* out2 = w2 * (t1 - t2) */
        x_real = t1_real-t2_real;
        x_imag = t1_imag-t2_imag;
        out[j+2*dj] = w2_real*x_real-w2_imag*x_imag;
        out[j+2*dj+1] = w2_real*x_imag+w2_imag*x_real;
        /* out3 = w3 * (t3 - i t4) */
        x_real = t3_real+t4_imag;
        x_imag = t3_imag-t4_real;
        out[j+3*dj] = w3_real*x_real-w3_imag*x_imag;
        out[j+3*dj+1] = w3_real*x_imag+w3_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_5
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_5(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 5;
    int m = n/factor;
    int q = n/product;
    int p_1 = product/factor;
    //int jump = (factor-1)*p_1;
    double tau = (Math.sqrt(5.0)/4.0);
    double sin_2pi_by_5 = sign*Math.sin(2.0*PI/5.0);
    double sin_2pi_by_10 = sign*Math.sin(2.0*PI/10.0);
    int i = in0, j = out0;
    int di = istride*m;
    int dj = ostride*p_1;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w1_real = twids[0];
      double w1_imag = -sign*twids[1];
      double w2_real = twids[2];
      double w2_imag = -sign*twids[3];
      double w3_real = twids[4];
      double w3_imag = -sign*twids[5];
      double w4_real = twids[6];
      double w4_imag = -sign*twids[7];
      for(k1 = 0; k1<p_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        double z2_real = in[i+2*di];
        double z2_imag = in[i+2*di+1];
        double z3_real = in[i+3*di];
        double z3_imag = in[i+3*di+1];
        double z4_real = in[i+4*di];
        double z4_imag = in[i+4*di+1];
        i += istride;
        /* compute x = W(5) z */
        /* t1 = z1 + z4 */
        double t1_real = z1_real+z4_real;
        double t1_imag = z1_imag+z4_imag;
        /* t2 = z2 + z3 */
        double t2_real = z2_real+z3_real;
        double t2_imag = z2_imag+z3_imag;
        /* t3 = z1 - z4 */
        double t3_real = z1_real-z4_real;
        double t3_imag = z1_imag-z4_imag;
        /* t4 = z2 - z3 */
        double t4_real = z2_real-z3_real;
        double t4_imag = z2_imag-z3_imag;
        /* t5 = t1 + t2 */
        double t5_real = t1_real+t2_real;
        double t5_imag = t1_imag+t2_imag;
        /* t6 = (sqrt(5)/4)(t1 - t2) */
        double t6_real = tau*(t1_real-t2_real);
        double t6_imag = tau*(t1_imag-t2_imag);
        /* t7 = z0 - ((t5)/4) */
        double t7_real = z0_real-t5_real/4.0;
        double t7_imag = z0_imag-t5_imag/4.0;
        /* t8 = t7 + t6 */
        double t8_real = t7_real+t6_real;
        double t8_imag = t7_imag+t6_imag;
        /* t9 = t7 - t6 */
        double t9_real = t7_real-t6_real;
        double t9_imag = t7_imag-t6_imag;
        /* t10 = sin(2 pi/5) t3 + sin(2 pi/10) t4 */
        double t10_real = sin_2pi_by_5*t3_real+sin_2pi_by_10*t4_real;
        double t10_imag = sin_2pi_by_5*t3_imag+sin_2pi_by_10*t4_imag;
        /* t11 = sin(2 pi/10) t3 - sin(2 pi/5) t4 */
        double t11_real = sin_2pi_by_10*t3_real-sin_2pi_by_5*t4_real;
        double t11_imag = sin_2pi_by_10*t3_imag-sin_2pi_by_5*t4_imag;
        /* apply twiddle factors */
        /* out0 = 1 * (z0 + t5) */
        out[j] = z0_real+t5_real;
        out[j+1] = z0_imag+t5_imag;
        /* out1 = w1 * (t8 + i t10) */
        x_real = t8_real-t10_imag;
        x_imag = t8_imag+t10_real;
        out[j+dj] = w1_real*x_real-w1_imag*x_imag;
        out[j+dj+1] = w1_real*x_imag+w1_imag*x_real;
        /* out2 = w2 * (t9 + i t11) */
        x_real = t9_real-t11_imag;
        x_imag = t9_imag+t11_real;
        out[j+2*dj] = w2_real*x_real-w2_imag*x_imag;
        out[j+2*dj+1] = w2_real*x_imag+w2_imag*x_real;
        /* out3 = w3 * (t9 - i t11) */
        x_real = t9_real+t11_imag;
        x_imag = t9_imag-t11_real;
        out[j+3*dj] = w3_real*x_real-w3_imag*x_imag;
        out[j+3*dj+1] = w3_real*x_imag+w3_imag*x_real;
        /* out4 = w4 * (t8 - i t10) */
        x_real = t8_real+t10_imag;
        x_imag = t8_imag-t10_real;
        out[j+4*dj] = w4_real*x_real-w4_imag*x_imag;
        out[j+4*dj+1] = w4_real*x_imag+w4_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_6
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_6(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 6;
    int m = n/factor;
    int q = n/product;
    int p_1 = product/factor;
    //int jump = (factor-1)*p_1;
    double tau = sign*Math.sqrt(3.0)/2.0;
    int i = in0, j = out0;
    int di = istride*m;
    int dj = ostride*p_1;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w1_real = twids[0];
      double w1_imag = -sign*twids[1];
      double w2_real = twids[2];
      double w2_imag = -sign*twids[3];
      double w3_real = twids[4];
      double w3_imag = -sign*twids[5];
      double w4_real = twids[6];
      double w4_imag = -sign*twids[7];
      double w5_real = twids[8];
      double w5_imag = -sign*twids[9];
      for(k1 = 0; k1<p_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        double z2_real = in[i+2*di];
        double z2_imag = in[i+2*di+1];
        double z3_real = in[i+3*di];
        double z3_imag = in[i+3*di+1];
        double z4_real = in[i+4*di];
        double z4_imag = in[i+4*di+1];
        double z5_real = in[i+5*di];
        double z5_imag = in[i+5*di+1];
        i += istride;
        /* compute x = W(6) z */
        /*
         * W(6) is a combination of sums and differences of W(3) acting
         *  on the even and odd elements of z
         */
        /* ta1 = z2 + z4 */
        double ta1_real = z2_real+z4_real;
        double ta1_imag = z2_imag+z4_imag;
        /* ta2 = z0 - ta1/2 */
        double ta2_real = z0_real-ta1_real/2;
        double ta2_imag = z0_imag-ta1_imag/2;
        /* ta3 = (+/-) sin(pi/3)*(z2 - z4) */
        double ta3_real = tau*(z2_real-z4_real);
        double ta3_imag = tau*(z2_imag-z4_imag);
        /* a0 = z0 + ta1 */
        double a0_real = z0_real+ta1_real;
        double a0_imag = z0_imag+ta1_imag;
        /* a1 = ta2 + i ta3 */
        double a1_real = ta2_real-ta3_imag;
        double a1_imag = ta2_imag+ta3_real;
        /* a2 = ta2 - i ta3 */
        double a2_real = ta2_real+ta3_imag;
        double a2_imag = ta2_imag-ta3_real;
        /* tb1 = z5 + z1 */
        double tb1_real = z5_real+z1_real;
        double tb1_imag = z5_imag+z1_imag;
        /* tb2 = z3 - tb1/2 */
        double tb2_real = z3_real-tb1_real/2;
        double tb2_imag = z3_imag-tb1_imag/2;
        /* tb3 = (+/-) sin(pi/3)*(z5 - z1) */
        double tb3_real = tau*(z5_real-z1_real);
        double tb3_imag = tau*(z5_imag-z1_imag);
        /* b0 = z3 + tb1 */
        double b0_real = z3_real+tb1_real;
        double b0_imag = z3_imag+tb1_imag;
        /* b1 = tb2 + i tb3 */
        double b1_real = tb2_real-tb3_imag;
        double b1_imag = tb2_imag+tb3_real;
        /* b2 = tb2 - i tb3 */
        double b2_real = tb2_real+tb3_imag;
        double b2_imag = tb2_imag-tb3_real;
        /* apply twiddle factors */
        /* out0 = 1 * (a0 + b0) */
        out[j] = a0_real+b0_real;
        out[j+1] = a0_imag+b0_imag;
        /* out1 = w1 * (a1 - b1) */
        x_real = a1_real-b1_real;
        x_imag = a1_imag-b1_imag;
        out[j+dj] = w1_real*x_real-w1_imag*x_imag;
        out[j+dj+1] = w1_real*x_imag+w1_imag*x_real;
        /* out2 = w2 * (a2 + b2) */
        x_real = a2_real+b2_real;
        x_imag = a2_imag+b2_imag;
        out[j+2*dj] = w2_real*x_real-w2_imag*x_imag;
        out[j+2*dj+1] = w2_real*x_imag+w2_imag*x_real;
        /* out3 = w3 * (a0 - b0) */
        x_real = a0_real-b0_real;
        x_imag = a0_imag-b0_imag;
        out[j+3*dj] = w3_real*x_real-w3_imag*x_imag;
        out[j+3*dj+1] = w3_real*x_imag+w3_imag*x_real;
        /* out4 = w4 * (a1 + b1) */
        x_real = a1_real+b1_real;
        x_imag = a1_imag+b1_imag;
        out[j+4*dj] = w4_real*x_real-w4_imag*x_imag;
        out[j+4*dj+1] = w4_real*x_imag+w4_imag*x_real;
        /* out5 = w5 * (a2 - b2) */
        x_real = a2_real-b2_real;
        x_imag = a2_imag-b2_imag;
        out[j+5*dj] = w5_real*x_real-w5_imag*x_imag;
        out[j+5*dj+1] = w5_real*x_imag+w5_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_7
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param product
   */
  void pass_7(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int product) {
    int k, k1;
    int factor = 7;
    int m = n/factor;
    int q = n/product;
    int p_1 = product/factor;
    //int jump = (factor-1)*p_1;
    double c1 = Math.cos(1.0*2.0*PI/7.0);
    double c2 = Math.cos(2.0*2.0*PI/7.0);
    double c3 = Math.cos(3.0*2.0*PI/7.0);
    double s1 = (-sign)*Math.sin(1.0*2.0*PI/7.0);
    double s2 = (-sign)*Math.sin(2.0*2.0*PI/7.0);
    double s3 = (-sign)*Math.sin(3.0*2.0*PI/7.0);
    int i = in0, j = out0;
    int di = istride*m;
    int dj = ostride*p_1;
    double x_real, x_imag;
    for(k = 0; k<q; k++) {
      double twids[] = twiddle[fi][k];
      double w1_real = twids[0];
      double w1_imag = -sign*twids[1];
      double w2_real = twids[2];
      double w2_imag = -sign*twids[3];
      double w3_real = twids[4];
      double w3_imag = -sign*twids[5];
      double w4_real = twids[6];
      double w4_imag = -sign*twids[7];
      double w5_real = twids[8];
      double w5_imag = -sign*twids[9];
      double w6_real = twids[10];
      double w6_imag = -sign*twids[11];
      for(k1 = 0; k1<p_1; k1++) {
        double z0_real = in[i];
        double z0_imag = in[i+1];
        double z1_real = in[i+di];
        double z1_imag = in[i+di+1];
        double z2_real = in[i+2*di];
        double z2_imag = in[i+2*di+1];
        double z3_real = in[i+3*di];
        double z3_imag = in[i+3*di+1];
        double z4_real = in[i+4*di];
        double z4_imag = in[i+4*di+1];
        double z5_real = in[i+5*di];
        double z5_imag = in[i+5*di+1];
        double z6_real = in[i+6*di];
        double z6_imag = in[i+6*di+1];
        i += istride;
        /* compute x = W(7) z */
        /* t0 = z1 + z6 */
        double t0_real = z1_real+z6_real;
        double t0_imag = z1_imag+z6_imag;
        /* t1 = z1 - z6 */
        double t1_real = z1_real-z6_real;
        double t1_imag = z1_imag-z6_imag;
        /* t2 = z2 + z5 */
        double t2_real = z2_real+z5_real;
        double t2_imag = z2_imag+z5_imag;
        /* t3 = z2 - z5 */
        double t3_real = z2_real-z5_real;
        double t3_imag = z2_imag-z5_imag;
        /* t4 = z4 + z3 */
        double t4_real = z4_real+z3_real;
        double t4_imag = z4_imag+z3_imag;
        /* t5 = z4 - z3 */
        double t5_real = z4_real-z3_real;
        double t5_imag = z4_imag-z3_imag;
        /* t6 = t2 + t0 */
        double t6_real = t2_real+t0_real;
        double t6_imag = t2_imag+t0_imag;
        /* t7 = t5 + t3 */
        double t7_real = t5_real+t3_real;
        double t7_imag = t5_imag+t3_imag;
        /* b0 = z0 + t6 + t4 */
        double b0_real = z0_real+t6_real+t4_real;
        double b0_imag = z0_imag+t6_imag+t4_imag;
        /* b1 = ((cos(2pi/7) + cos(4pi/7) + cos(6pi/7))/3-1) (t6 + t4) */
        double b1_real = (((c1+c2+c3)/3.0-1.0)*(t6_real+t4_real));
        double b1_imag = (((c1+c2+c3)/3.0-1.0)*(t6_imag+t4_imag));
        /* b2 = ((2*cos(2pi/7) - cos(4pi/7) - cos(6pi/7))/3) (t0 - t4) */
        double b2_real = (((2.0*c1-c2-c3)/3.0)*(t0_real-t4_real));
        double b2_imag = (((2.0*c1-c2-c3)/3.0)*(t0_imag-t4_imag));
        /* b3 = ((cos(2pi/7) - 2*cos(4pi/7) + cos(6pi/7))/3) (t4 - t2) */
        double b3_real = (((c1-2.0*c2+c3)/3.0)*(t4_real-t2_real));
        double b3_imag = (((c1-2.0*c2+c3)/3.0)*(t4_imag-t2_imag));
        /* b4 = ((cos(2pi/7) + cos(4pi/7) - 2*cos(6pi/7))/3) (t2 - t0) */
        double b4_real = (((c1+c2-2.0*c3)/3.0)*(t2_real-t0_real));
        double b4_imag = (((c1+c2-2.0*c3)/3.0)*(t2_imag-t0_imag));
        /* b5 = sign * ((sin(2pi/7) + sin(4pi/7) - sin(6pi/7))/3) (t7 + t1) */
        double b5_real = ((s1+s2-s3)/3.0)*(t7_real+t1_real);
        double b5_imag = ((s1+s2-s3)/3.0)*(t7_imag+t1_imag);
        /* b6 = sign * ((2sin(2pi/7) - sin(4pi/7) + sin(6pi/7))/3) (t1 - t5) */
        double b6_real = ((2.0*s1-s2+s3)/3.0)*(t1_real-t5_real);
        double b6_imag = ((2.0*s1-s2+s3)/3.0)*(t1_imag-t5_imag);
        /* b7 = sign * ((sin(2pi/7) - 2sin(4pi/7) - sin(6pi/7))/3) (t5 - t3) */
        double b7_real = ((s1-2.0*s2-s3)/3.0)*(t5_real-t3_real);
        double b7_imag = ((s1-2.0*s2-s3)/3.0)*(t5_imag-t3_imag);
        /* b8 = sign * ((sin(2pi/7) + sin(4pi/7) + 2sin(6pi/7))/3) (t3 - t1) */
        double b8_real = ((s1+s2+2.0*s3)/3.0)*(t3_real-t1_real);
        double b8_imag = ((s1+s2+2.0*s3)/3.0)*(t3_imag-t1_imag);
        /* T0 = b0 + b1 */
        double T0_real = b0_real+b1_real;
        double T0_imag = b0_imag+b1_imag;
        /* T1 = b2 + b3 */
        double T1_real = b2_real+b3_real;
        double T1_imag = b2_imag+b3_imag;
        /* T2 = b4 - b3 */
        double T2_real = b4_real-b3_real;
        double T2_imag = b4_imag-b3_imag;
        /* T3 = -b2 - b4 */
        double T3_real = -b2_real-b4_real;
        double T3_imag = -b2_imag-b4_imag;
        /* T4 = b6 + b7 */
        double T4_real = b6_real+b7_real;
        double T4_imag = b6_imag+b7_imag;
        /* T5 = b8 - b7 */
        double T5_real = b8_real-b7_real;
        double T5_imag = b8_imag-b7_imag;
        /* T6 = -b8 - b6 */
        double T6_real = -b8_real-b6_real;
        double T6_imag = -b8_imag-b6_imag;
        /* T7 = T0 + T1 */
        double T7_real = T0_real+T1_real;
        double T7_imag = T0_imag+T1_imag;
        /* T8 = T0 + T2 */
        double T8_real = T0_real+T2_real;
        double T8_imag = T0_imag+T2_imag;
        /* T9 = T0 + T3 */
        double T9_real = T0_real+T3_real;
        double T9_imag = T0_imag+T3_imag;
        /* T10 = T4 + b5 */
        double T10_real = T4_real+b5_real;
        double T10_imag = T4_imag+b5_imag;
        /* T11 = T5 + b5 */
        double T11_real = T5_real+b5_real;
        double T11_imag = T5_imag+b5_imag;
        /* T12 = T6 + b5 */
        double T12_real = T6_real+b5_real;
        double T12_imag = T6_imag+b5_imag;
        /* apply twiddle factors */
        /* out0 = 1 * b0 */
        out[j] = b0_real;
        out[j+1] = b0_imag;
        /* out1 = w1 * (T7 - i T10) */
        x_real = T7_real+T10_imag;
        x_imag = T7_imag-T10_real;
        out[j+dj] = w1_real*x_real-w1_imag*x_imag;
        out[j+dj+1] = w1_real*x_imag+w1_imag*x_real;
        /* out2 = w2 * (T9 - i T12) */
        x_real = T9_real+T12_imag;
        x_imag = T9_imag-T12_real;
        out[j+2*dj] = w2_real*x_real-w2_imag*x_imag;
        out[j+2*dj+1] = w2_real*x_imag+w2_imag*x_real;
        /* out3 = w3 * (T8 + i T11) */
        x_real = T8_real-T11_imag;
        x_imag = T8_imag+T11_real;
        out[j+3*dj] = w3_real*x_real-w3_imag*x_imag;
        out[j+3*dj+1] = w3_real*x_imag+w3_imag*x_real;
        /* out4 = w4 * (T8 - i T11) */
        x_real = T8_real+T11_imag;
        x_imag = T8_imag-T11_real;
        out[j+4*dj] = w4_real*x_real-w4_imag*x_imag;
        out[j+4*dj+1] = w4_real*x_imag+w4_imag*x_real;
        /* out5 = w5 * (T9 + i T12) */
        x_real = T9_real-T12_imag;
        x_imag = T9_imag+T12_real;
        out[j+5*dj] = w5_real*x_real-w5_imag*x_imag;
        out[j+5*dj+1] = w5_real*x_imag+w5_imag*x_real;
        /* out6 = w6 * (T7 + i T10) */
        x_real = T7_real-T10_imag;
        x_imag = T7_imag+T10_real;
        out[j+6*dj] = w6_real*x_real-w6_imag*x_imag;
        out[j+6*dj+1] = w6_real*x_imag+w6_imag*x_real;
        j += ostride;
      }
      j += (factor-1)*dj;
    }
  }

  /* ______________________________________________________________________ */

  /**
   * Method pass_n
   *
   * @param fi
   * @param in
   * @param in0
   * @param istride
   * @param out
   * @param out0
   * @param ostride
   * @param sign
   * @param factor
   * @param product
   */
  void pass_n(int fi, double in[], int in0, int istride, double out[], int out0, int ostride, int sign, int factor, int product) {
    int i = 0, j = 0;
    int k, k1;
    int m = n/factor;
    int q = n/product;
    int p_1 = product/factor;
    int jump = (factor-1)*p_1;
    int e, e1;
    for(i = 0; i<m; i++) {
      out[out0+ostride*i] = in[in0+istride*i];
      out[out0+ostride*i+1] = in[in0+istride*i+1];
    }
    for(e = 1; e<(factor-1)/2+1; e++) {
      for(i = 0; i<m; i++) {
        int idx = i+e*m;
        int idxc = i+(factor-e)*m;
        out[out0+ostride*idx] = in[in0+istride*idx]+in[in0+istride*idxc];
        out[out0+ostride*idx+1] = in[in0+istride*idx+1]+in[in0+istride*idxc+1];
        out[out0+ostride*idxc] = in[in0+istride*idx]-in[in0+istride*idxc];
        out[out0+ostride*idxc+1] = in[in0+istride*idx+1]-in[in0+istride*idxc+1];
      }
    }
    /* e = 0 */
    for(i = 0; i<m; i++) {
      in[in0+istride*i] = out[out0+ostride*i];
      in[in0+istride*i+1] = out[out0+ostride*i+1];
    }
    for(e1 = 1; e1<(factor-1)/2+1; e1++) {
      for(i = 0; i<m; i++) {
        in[in0+istride*i] += out[out0+ostride*(i+e1*m)];
        in[in0+istride*i+1] += out[out0+ostride*(i+e1*m)+1];
      }
    }
    double twiddl[] = twiddle[fi][q];
    for(e = 1; e<(factor-1)/2+1; e++) {
      int idx = e;
      double w_real, w_imag;
      int em = e*m;
      int ecm = (factor-e)*m;
      for(i = 0; i<m; i++) {
        in[in0+istride*(i+em)] = out[out0+ostride*i];
        in[in0+istride*(i+em)+1] = out[out0+ostride*i+1];
        in[in0+istride*(i+ecm)] = out[out0+ostride*i];
        in[in0+istride*(i+ecm)+1] = out[out0+ostride*i+1];
      }
      for(e1 = 1; e1<(factor-1)/2+1; e1++) {
        if(idx==0) {
          w_real = 1;
          w_imag = 0;
        } else {
          w_real = twiddl[2*(idx-1)];
          w_imag = -sign*twiddl[2*(idx-1)+1];
        }
        for(i = 0; i<m; i++) {
          double ap = w_real*out[out0+ostride*(i+e1*m)];
          double am = w_imag*out[out0+ostride*(i+(factor-e1)*m)+1];
          double bp = w_real*out[out0+ostride*(i+e1*m)+1];
          double bm = w_imag*out[out0+ostride*(i+(factor-e1)*m)];
          in[in0+istride*(i+em)] += (ap-am);
          in[in0+istride*(i+em)+1] += (bp+bm);
          in[in0+istride*(i+ecm)] += (ap+am);
          in[in0+istride*(i+ecm)+1] += (bp-bm);
        }
        idx += e;
        idx %= factor;
      }
    }
    i = 0;
    j = 0;
    /* k = 0 */
    for(k1 = 0; k1<p_1; k1++) {
      out[out0+ostride*k1] = in[in0+istride*k1];
      out[out0+ostride*k1+1] = in[in0+istride*k1+1];
    }
    for(e1 = 1; e1<factor; e1++) {
      for(k1 = 0; k1<p_1; k1++) {
        out[out0+ostride*(k1+e1*p_1)] = in[in0+istride*(k1+e1*m)];
        out[out0+ostride*(k1+e1*p_1)+1] = in[in0+istride*(k1+e1*m)+1];
      }
    }
    i = p_1;
    j = product;
    for(k = 1; k<q; k++) {
      for(k1 = 0; k1<p_1; k1++) {
        out[out0+ostride*j] = in[in0+istride*i];
        out[out0+ostride*j+1] = in[in0+istride*i+1];
        i++;
        j++;
      }
      j += jump;
    }
    i = p_1;
    j = product;
    for(k = 1; k<q; k++) {
      twiddl = twiddle[fi][k];
      for(k1 = 0; k1<p_1; k1++) {
        for(e1 = 1; e1<factor; e1++) {
          double x_real = in[in0+istride*(i+e1*m)];
          double x_imag = in[in0+istride*(i+e1*m)+1];
          double w_real = twiddl[2*(e1-1)];
          double w_imag = -sign*twiddl[2*(e1-1)+1];
          out[out0+ostride*(j+e1*p_1)] = w_real*x_real-w_imag*x_imag;
          out[out0+ostride*(j+e1*p_1)+1] = w_real*x_imag+w_imag*x_real;
        }
        i++;
        j++;
      }
      j += jump;
    }
  }

  /**
   * Return the prime factors of n.
   * The method first extracts any factors in fromfactors, in order (which
   * needn't actually be prime).  Remaining factors in increasing order follow.
   * @param n
   * @param fromfactors
   *
   * @return
   */
  public static int[] factor(int n, int fromfactors[]) {
    int factors[] = new int[64]; // Cant be more than 64 factors.
    int nf = 0;
    int ntest = n;
    int factor;
    if(n<=0) {                                                    // Error case
      throw new Error("Number ("+n+") must be positive integer"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    /* deal with the preferred factors first */
    for(int i = 0; (i<fromfactors.length)&&(ntest!=1); i++) {
      factor = fromfactors[i];
      while((ntest%factor)==0) {
        ntest /= factor;
        factors[nf++] = factor;
      }
    }
    /* deal with any other even prime factors (there is only one) */
    factor = 2;
    while((ntest%factor)==0&&(ntest!=1)) {
      ntest /= factor;
      factors[nf++] = factor;
    }
    /* deal with any other odd prime factors */
    factor = 3;
    while(ntest!=1) {
      while((ntest%factor)!=0) {
        factor += 2;
      }
      ntest /= factor;
      factors[nf++] = factor;
    }
    /* check that the factorization is correct */
    int product = 1;
    for(int i = 0; i<nf; i++) {
      product *= factors[i];
    }
    if(product!=n) {
      throw new Error("factorization failed for "+n); //$NON-NLS-1$
    }
    /* Now, make an array of the right length containing the factors... */
    int f[] = new int[nf];
    System.arraycopy(factors, 0, f, 0, nf);
    return f;
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
