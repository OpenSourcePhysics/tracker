/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.analysis;
import java.awt.Color;
import org.opensourcephysics.display.ComplexDataset;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.numerics.FFTReal;

/**
 * FourierAnalysis adds gutter points to real data before performing a fast Fourier transform.
 * Gutter points increase the number points in order to approximate a nonperiodic function.
 *
 * The FFT output is phase shifted to account for the fact that the FFT basis functions are
 * defined on [0, 2*pi].
 *
 * @author W. Christian
 * @version 1.0
 */
public class FourierSinCosAnalysis implements Data {
  static final double PI2 = 2*Math.PI;
  FFTReal fft = new FFTReal();
  double[] fftData, omega, freqs;
  private double[] cosVec, sinVec, gutterVec;
  ComplexDataset[] complexDatasets = new ComplexDataset[1];
  Dataset[] realDatasets = new Dataset[3];
  boolean radians = false;
  private String name = "Fourier Analysis Sin/Cos Data"; //$NON-NLS-1$
  protected int datasetID = hashCode();

  /**
   * Fourier analyzes the given data y[] after adding gutter points at the start and end of the z[] array.
   *
   * @param x double[]
   * @param y double[]
   * @param gutter int
   * @return double[] the Fourier spectrum
   */
  public double[] doAnalysis(double[] x, double[] y, int gutter) {
    int offset = y.length%2; // zero if even number of points; one if odd number of points
    fftData = new double[y.length+2*gutter-offset];
    gutterVec = new double[gutter];
    System.arraycopy(y, 0, fftData, gutter, y.length-offset);
    fft.transform(fftData); // Computes the FFT of data leaving the result in fft_pts.
    double dx = x[1]-x[0];
    double xmin = x[0]-gutter*dx;
    double xmax = x[x.length-1-offset]+(gutter+1)*dx;
    omega = fft.getNaturalOmega(xmin, xmax);
    freqs = fft.getNaturalFreq(xmin, xmax);
    cosVec = new double[omega.length];
    sinVec = new double[omega.length];
    double norm = 2.0/y.length;
    //double norm=2.0/fftData.length;
    for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
      cosVec[i] = norm*Math.cos(omega[i]*xmin);
      sinVec[i] = norm*Math.sin(omega[i]*xmin);
    }
    cosVec[0] *= 0.5; // constant coefficient has factor of 1/2.
    sinVec[0] *= 0.5;
    for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
      double re = fftData[2*i];
      double im = fftData[2*i+1];
      fftData[2*i] = re*cosVec[i]+im*sinVec[i];    // cos coefficient
      fftData[2*i+1] = -im*cosVec[i]+re*sinVec[i]; // sin coefficient
    }
    return fftData;
  }

  /**
   * Some elements (a Group, for instance) do not contain data, but a list of subelements which do.
   * This method is used by Data displaying tools to create as many pages as needed.
   * @return A list of DataInformation elements, null if the element itself is a DataInformation
   */
  public java.util.List<Data> getDataList() {
    return null;
  }

  /**
   * Repeats the Fourier analysis of the real data y[] with the previously set scale and gutter.
   *
   * @param y double[]
   * @return double[] the Fourier sin/cos coefficients
   */
  public double[] repeatAnalysis(double[] y) {
    int offset = y.length%2; // zero if even number of points; one if odd number of points
    if(fftData==null) {
      int n = y.length-offset;
      double[] x = new double[n];
      double x0 = 0, dx = 1.0/n;
      for(int i = 0; i<n; i++) {
        x[i] = x0;
        x0 += dx;
      }
      doAnalysis(x, y, 0);
    }
    System.arraycopy(gutterVec, 0, fftData, 0, gutterVec.length);                                 // zero the left gutter
    System.arraycopy(gutterVec, 0, fftData, fftData.length-1-gutterVec.length, gutterVec.length); // zero the right gutter
    System.arraycopy(y, 0, fftData, gutterVec.length, y.length-offset);
    fft.transform(fftData); // Computes the FFT of data leaving the result in fft_pts.
    for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
      double re = fftData[2*i];
      double im = fftData[2*i+1];
      fftData[2*i] = re*cosVec[i]+im*sinVec[i];
      fftData[2*i+1] = im*cosVec[i]-re*sinVec[i];
    }
    return fftData;
  }

  /**
   * Gets the angular frequencies of the Fourier spectrum.
   * @return double[]
   */
  public double[] getNaturalOmega() {
    return omega;
  }

  /**
   * Gets the frequencies of the Fourier spectrum.
   * @return double[]
   */
  public double[] getNaturalFreq() {
    return freqs;
  }

  /**
   * Sets the radians flag for the frequency values of datasets.
   * Dataset x-values are either frequencies (cycles) or angular frequencies (radians) depending
   * on the value of the radians flag.
   *
   * @param radians boolean
   */
  public void useRadians(boolean radians) {
    this.radians = radians;
  }

  /**
   * Gets the radians flag.
   * Radians is true if the dataset uses angular frequency as the x-coordinate.
   *
   * @return boolean
   */
  public boolean isRadians() {
    return radians;
  }

  /**
   * Gets the datasets that contain the result of the last Fourier analysis.
   * The power spectrum is contained in the first dataset.
   * Sine coefficients are contained in the second dataset.
   * Cosine coefficients are in the third dataset.
   *
   * Dataset x-values are either frequencies (cycles) or angular frequencies (radians) depending
   * on the value of the radians flag.
   *
   * @return list of Datasets
   */
  public java.util.ArrayList<Dataset> getDatasets() {
    java.util.ArrayList<Dataset> list = new java.util.ArrayList<Dataset>();
    if(fftData==null) {
      return list;
    }
    if(realDatasets[0]==null) {
      realDatasets[0] = new Dataset();
      realDatasets[0].setXYColumnNames(DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        DisplayRes.getString("FourierSinCosAnalysis.Column.Power"),                              //$NON-NLS-1$
          DisplayRes.getString("FourierSinCosAnalysis.PowerSpectrum"));                          //$NON-NLS-1$
      realDatasets[0].setLineColor(Color.GREEN.darker());
      realDatasets[0].setMarkerColor(Color.GREEN.darker());
      realDatasets[0].setMarkerShape(Dataset.BAR);
      realDatasets[0].setMarkerSize(4);
      realDatasets[1] = new Dataset();
      realDatasets[1].setXYColumnNames(DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        DisplayRes.getString("FourierSinCosAnalysis.Column.Cosine"),                             //$NON-NLS-1$
          DisplayRes.getString("FourierSinCosAnalysis.CosineCoefficients"));                     //$NON-NLS-1$
      realDatasets[1].setLineColor(Color.CYAN.darker());
      realDatasets[1].setMarkerColor(Color.CYAN.darker());
      realDatasets[1].setMarkerShape(Dataset.BAR);
      realDatasets[1].setMarkerSize(4);
      realDatasets[2] = new Dataset();
      realDatasets[2].setXYColumnNames(DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        DisplayRes.getString("FourierSinCosAnalysis.Column.Sine"),                               //$NON-NLS-1$
          DisplayRes.getString("FourierSinCosAnalysis.SineCoefficients"));                       //$NON-NLS-1$
      realDatasets[2].setLineColor(Color.BLUE.darker());
      realDatasets[2].setMarkerColor(Color.BLUE.darker());
      realDatasets[2].setMarkerShape(Dataset.BAR);
      realDatasets[2].setMarkerSize(4);
    } else {
      realDatasets[0].clear();
      realDatasets[1].clear();
      realDatasets[2].clear();
    }
    if(radians) {
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
        double cos = fftData[2*i], sin = fftData[2*i+1];
        realDatasets[0].append(omega[i], sin*sin+cos*cos);
        realDatasets[1].append(omega[i], sin);
        realDatasets[2].append(omega[i], cos);
      }
    } else {
      for(int i = 0, nFreqs = freqs.length; i<nFreqs; i++) {
        double sin = fftData[2*i], cos = fftData[2*i+1];
        realDatasets[0].append(freqs[i], sin*sin+cos*cos);
        realDatasets[1].append(freqs[i], sin);
        realDatasets[2].append(freqs[i], cos);
      }
    }
    list.add(realDatasets[0]);
    list.add(realDatasets[1]);
    list.add(realDatasets[2]);
    return list;
  }

  /**
   * Sets a name that can be used to identify the dataset.
   *
   * @param name String
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The column names to be used in the data display tool
   * @return
   */
  public String[] getColumnNames() {
    return new String[] {name};
  }

  /**
   * Gets the dataset name.
   *
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Line colors for Data interface.
   * @return
   */
  public java.awt.Color[] getLineColors() {
    return null;
  }

  /**
   * Fill colors for Data interface.
   * @return
   */
  public java.awt.Color[] getFillColors() {
    return null;
  }

  /**
   * Gets the frequencies, power, cos, and sin coefficients.
   * @return double[][]
   */
  public double[][] getData2D() {
    if(fftData==null) {
      return null;
    }
    double[][] data = new double[4][];
    int n = fftData.length/2;
    data[1] = new double[n];
    data[2] = new double[n];
    data[3] = new double[n];
    for(int i = 0; i<n; i++) {
      double cos = fftData[2*i], sin = fftData[2*i+1];
      data[1][i] = sin*sin+cos*cos;
      data[2][i] = cos;
      data[3][i] = sin;
    }
    if(radians) {
      data[0] = omega;
    } else {
      data[0] = freqs;
    }
    return data;
  }

  /**
   * 3D data is not available.
   *
   * @return double[][][]
   */
  public double[][][] getData3D() {
    return null;
  }

  /**
   * Sets the ID number of this Data.
   *
   * @param id the ID number
   */
  public void setID(int id) {
    datasetID = id;
  }

  /**
   * Returns a unique identifier for this Data.
   *
   * @return the ID number
   */
  public int getID() {
    return datasetID;
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
