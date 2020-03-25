/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.Dataset;

/**
 * DataColumn is a Dataset that represents a single column in a DataToolTable.
 * The x-column name is always "row" and the x-points are always row numbers.
 * Y-values may be shifted from their raw values. All values have the same shift.
 * 
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataColumn extends Dataset {
  boolean deletable = false;
  double shift;
  boolean shifted = false;

  /**
   * Constructs a DataColumn.
   */
  public DataColumn() {
    super.setXColumnVisible(false);
    super.setXYColumnNames("row", this.getYColumnName()); //$NON-NLS-1$
  }

  /**
   * Sets the y-column points. The x-column points are always row numbers.
   *
   * @param yPoints the y-column data points
   */
  public void setPoints(double[] yPoints) {
    clear();
    double[] rows = DataTool.getRowArray(yPoints.length);
    append(rows, yPoints);
  }

  /**
   * Overrides Dataset.setXYColumnNames method. The x-column name is always "row".
   *
   * @param xName ignored
   * @param yName the y-column name
   */
  public void setXYColumnNames(String xName, String yName) {
    super.setXYColumnNames("row", yName); //$NON-NLS-1$
  }

  /**
   * Overrides Dataset.setXYColumnNames method. The x-column name is always "row".
   *
   * @param xName ignored
   * @param yName the y-column name
   * @param name the dataset name
   */
  public void setXYColumnNames(String xName, String yName, String name) {
    super.setXYColumnNames("row", yName, name); //$NON-NLS-1$
  }

  /**
   * Overrides Dataset.setXColumnVisible method. The x-column is never visible.
   *
   * @param b ignored
   */
  public void setXColumnVisible(boolean b) {}
  
  /**
   * Gets a copy of the ypoints array, with shift added if shifted.
   *
   * @return the y points (may be shifted)
   */
  @Override
  public double[] getYPoints() {
    double[] temp = new double[index];
    for (int i=0; i<index; i++) {
    	temp[i] = isShifted()? ypoints[i]+shift: ypoints[i];
    }
    return temp;
  }

  /**
   * Sets the shifted property to shift the values of all elements.
   *
   * @param shift true to shift the values
   */
  public void setShifted(boolean shift) {
  	shifted = shift;
  }
  
  /**
   * Gets the shifted property.
   *
   * @return true if values are shifted
   */
  public boolean isShifted() {
  	return shifted;
  }
  
  /**
   * Sets the shift used to shift the values of all elements.
   *
   * @param shift the shift
   * @return true if shift was changed
   */
  public boolean setShift(double shift) {
  	if (this.shift==shift) return false;
  	this.shift = shift;
  	return true;
  }
  
  /**
   * Gets the shift used to shift the values of all elements.
   *
   * @return the shift
   */
  public double getShift() {
  	return shift;
  }
  
  public boolean setShiftedValue(int i, double value) {
  	if (i<0 || i>=getIndex()) return false;
  	double d = value-ypoints[i];
  	if (!Double.isNaN(d)) {
  		return setShift(d);
  	}
  	return false;
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load DataColumn data in an XMLControl.
   */
  protected static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      DataColumn column = (DataColumn) obj;
      double shift = column.getShift();
      if (shift!=0) {
        control.setValue("shift", shift); //$NON-NLS-1$
      	// temporarily set shift to zero
        column.shift = 0;
      }
      Dataset.getLoader().saveObject(control, column);
      column.shift = shift;
      if(column.deletable) {
        control.setValue("deletable", true); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control) {
      return new DataColumn();
    }

    public Object loadObject(XMLControl control, Object obj) {
      DataColumn column = (DataColumn) obj;
      Dataset.getLoader().loadObject(control, column);
      if (control.getPropertyNames().contains("shift")) { //$NON-NLS-1$
      	column.shift = control.getDouble("shift"); //$NON-NLS-1$
      }
      column.deletable = control.getBoolean("deletable"); //$NON-NLS-1$
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
