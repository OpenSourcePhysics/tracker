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
 * Stripchart stores data having increasing x values.
 *
 * Only data within the interval [lastx-xrange, lastx] is retained.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class Stripchart extends Dataset {
  boolean rightToLeft = true;
  double xrange; // the range the independent variable
  double yrange; // the minimum range the dependent variable
  double lastx;
  boolean enabled = true;

  /**
   * Constructs a Stripchart witht he given ranges.
   * @param _xrange double
   * @param _yrange double
   */
  public Stripchart(double _xrange, double _yrange) {
    super();
    xrange = Math.abs(_xrange);
    yrange = Math.abs(_yrange);
  }

  /**
   * Constructs a Stripchart for use by the XML loader.
   */
  protected Stripchart() {
    this(1, 10);
  }

  /**
   * Sets the range of the stipchart.
   * @param _xrange double
   * @param _yrange double
   */
  public void setRange(double _xrange, double _yrange) {
    xrange = Math.abs(_xrange);
    yrange = Math.abs(_yrange);
  }

  public void enable(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Appends an (x,y) datum to the Stipchart.
   *
   * @param  x
   * @param  y
   */
  public void append(double x, double y) {
    if(!enabled) {
      super.append(x, y);
      return;
    }
    if((index!=0)&&(x<lastx)) {
      clear(); // x values are not increasing so clear and restart data collection
    }
    lastx = x;
    super.append(x, y);
    trim();
  }

  /**
   *  Appends (x,y) data-arrays to the Stipchart.
   *
   * @param  _xpoints
   * @param  _ypoints
   */
  public void append(double[] _xpoints, double[] _ypoints) {
    if(!enabled) {
      super.append(_xpoints, _ypoints);
      return;
    }
    if((index!=0)&&(_xpoints[0]<lastx)) {
      clear(); // new x values are not increasing so clear and restart data collection
    }
    for(int i = 1, n = _xpoints.length; i<n; i++) {
      if(_xpoints[i]<_xpoints[i-1]) { // x values are not increasing so clear and return without collecting data;
        clear();
        return;
      }
    }
    lastx = _xpoints[_xpoints.length-1];
    super.append(_xpoints, _ypoints);
    trim();
  }

  /**
   *  Clears all data from this Dataset.
   */
  public void clear() {
    super.clear();
    lastx = xpoints[0];
  }

  /**
   * Trims data points whose x values are outside the xrange from the dataset.
   */
  private void trim() {
    if((index>0)&&(xpoints[0]<lastx-xrange)) {
      int counter = 0;
      while((counter<index)&&(xpoints[counter]<lastx-xrange)) {
        counter++;
      }
      System.arraycopy(xpoints, counter, xpoints, 0, index-counter);
      System.arraycopy(ypoints, counter, ypoints, 0, index-counter);
      index = index-counter;
    }
    if(rightToLeft) {
      xmin = lastx-xrange;
    } else {
      xmin = lastx;
    }
    if(rightToLeft) {
      xmax = lastx;
    } else {
      xmax = lastx-xrange;
    }
    ymin = ymax = ypoints[0];
    for(int i = 1; i<index; i++) {
      ymin = Math.min(ymin, ypoints[i]);
      ymax = Math.max(ymax, ypoints[i]);
    }
    if(ymax-ymin<yrange) {
      ymin = (ymax+ymin-yrange)/2.0;
      ymax = (ymax+ymin+yrange)/2.0;
    }
    recalculatePath();
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new StripchartLoader();
  }

  protected static class StripchartLoader extends Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      Stripchart dataset = (Stripchart) obj;
      control.setValue("x_range", dataset.xrange);            //$NON-NLS-1$
      control.setValue("y_range", dataset.yrange);            //$NON-NLS-1$
      control.setValue("last_x", dataset.lastx);              //$NON-NLS-1$
      control.setValue("right_to_left", dataset.rightToLeft); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      Stripchart dataset = (Stripchart) obj;
      dataset.xrange = control.getDouble("x_range");             //$NON-NLS-1$
      dataset.yrange = control.getDouble("y_range");             //$NON-NLS-1$
      dataset.lastx = control.getDouble("last_x");               //$NON-NLS-1$
      dataset.rightToLeft = control.getBoolean("right_to_left"); //$NON-NLS-1$
      super.loadObject(control, obj);
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
