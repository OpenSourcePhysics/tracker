/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * HistogramDataset creates a histogram of appended data points.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class HistogramDataset extends Dataset {
  double min, max;
  double binSize = 1;
  int n;
  double[] binVals;
  double[] xVals;
  int counts;
  int missedCounts;

  /**
   * Constructor HistogramDataset
   * @param binMin
   * @param binMax
   * @param binSize
   */
  public HistogramDataset(double binMin, double binMax, double binSize) {
    super();
    setMarkerShape(Dataset.POST);
    setBinWidth(binMin, binMax, binSize);
  }

  /**
   * Constructs a HistogramDataset for values from 0 through 100.
   */
  protected HistogramDataset() {
    this(0, 100, 1);
  }

  /**
   * Appends an (x,y) datum to the Stipchart.
   *
   * @param  x
   * @param  y
   */
  public void append(double x, double y) {
    int index = (int) ((x-min)/binSize);
    if((index<0)||(index>=n)) {
      missedCounts++;
    } else {
      counts++;
      binVals[index] += y;
      ymax = Math.max(binVals[index], ymax);
      ymin = Math.min(binVals[index], ymin);
      //  xpoints do not change; ypoints has been set so just copy the new data
      System.arraycopy(binVals, 0, ypoints, 0, n);
      if(isConnected()) {
        recalculatePath();
      }
    }
  }

  /**
   *  Appends (x,y) data-arrays to the Stipchart.
   *
   * @param  xpoints
   * @param  ypoints
   */
  public void append(double[] xpoints, double[] ypoints) {
    for(int j = 0, nj = xpoints.length; j<nj; j++) { // bin all the points
      int index = (int) ((xpoints[j]-min)/binSize);
      if((index<0)||(index>=n)) {
        missedCounts++;
      } else {
        counts++;
        binVals[index] += ypoints[j];
        ymax = Math.max(binVals[index], ymax);
        ymin = Math.min(binVals[index], ymin);
      }
    }
    //  xpoints do not change; ypoints has been set so just copy the new data
    System.arraycopy(binVals, 0, this.ypoints, 0, n);
    if(isConnected()) {
      recalculatePath();
    }
  }

  /**
   *  Gets the x world coordinate for the left hand side of the panel.
   *
   * @return    xmin
   */
  public double getXMin() {
    return min;
  }

  /**
   *  Gets the x world coordinate for the right hand side of the panel.
   *
   * @return    xmax
   */
  public double getXMax() {
    return max;
  }

  public void setBinWidth(double binMin, double binMax, double binSize) {
    counts = 0;
    missedCounts = 0;
    min = binMin;
    max = binMax;
    this.binSize = binSize;
    n = (int) ((binMax-binMin)/binSize);
    binVals = new double[n];
    xVals = new double[n];
    double x = min+binSize/2;
    for(int i = 0; i<n; i++) {
      xVals[i] = x;
      x += this.binSize;
    }
    super.clear();
    super.append(xVals, binVals);
  }

  /**
   *  Clears data from the histogram.
   */
  public void clear() {
    for(int i = 0; i<n; i++) {
      binVals[i] = 0;
    }
    counts = 0;
    missedCounts = 0;
    //  xpoints do not change; ypoints has been set so just copy the new data
    ymax = 0;
    ymin = 0;
    if(n==0) {
      return;
    }
    System.arraycopy(binVals, 0, ypoints, 0, n);
    if(isConnected()) {
      recalculatePath();
    }
  }

  /**
 * Returns the XML.ObjectLoader for this class.
 *
 * @return the object loader
 */
  public static XML.ObjectLoader getLoader() {
    return new HistogramDatasetLoader();
  }

  protected static class HistogramDatasetLoader extends Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      HistogramDataset dataset = (HistogramDataset) obj;
      control.setValue("min", dataset.min);                    //$NON-NLS-1$
      control.setValue("max", dataset.max);                    //$NON-NLS-1$
      control.setValue("bin_size", dataset.binSize);           //$NON-NLS-1$
      control.setValue("number_of_bins", dataset.n);           //$NON-NLS-1$
      control.setValue("bin_vals", dataset.binVals);           //$NON-NLS-1$
      control.setValue("x_vals", dataset.xVals);               //$NON-NLS-1$
      control.setValue("counts", dataset.counts);              //$NON-NLS-1$
      control.setValue("missed_counts", dataset.missedCounts); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      HistogramDataset dataset = (HistogramDataset) obj;
      dataset.setBinWidth(control.getDouble("min"), control.getDouble("max"), control.getDouble("bin_size")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      dataset.binVals = (double[]) control.getObject("bin_vals"); //$NON-NLS-1$
      dataset.xVals = (double[]) control.getObject("x_vals");     //$NON-NLS-1$
      dataset.counts = control.getInt("counts");                  //$NON-NLS-1$
      dataset.missedCounts = control.getInt("missed_counts");     //$NON-NLS-1$
      if(dataset.n==0) {
        return obj;
      }
      System.arraycopy(dataset.xVals, 0, dataset.xpoints, 0, dataset.n);
      System.arraycopy(dataset.binVals, 0, dataset.ypoints, 0, dataset.n);
      if(dataset.isConnected()) {
        dataset.recalculatePath();
      }
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
