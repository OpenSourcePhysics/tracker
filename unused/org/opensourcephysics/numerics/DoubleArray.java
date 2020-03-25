/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.text.DecimalFormat;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * DoubleArray stores an array of doubles as a string and as an array.
*/
public class DoubleArray {
  public static int NumberFormatError = 1;
  public static int ArrayIndexOutOfBoundsError = 2;
  protected DecimalFormat format = new DecimalFormat("0.00");       // display format //$NON-NLS-1$
  protected DecimalFormat formatExp = new DecimalFormat("0.00#E0"); // display format //$NON-NLS-1$
  protected double[] array;
  protected String defaultString;
  protected double[] defaultArray;
  protected int errorcode = 0;

  /**
   * Creates a DoubleArray of the given length with all elements set to zero.
   *
   * The length of the arry cannot be changed.
   *
   * @param n
   */
  public DoubleArray(int n) {
    array = new double[n];
    defaultArray = array;
  }

  /**
   * Creates a DoubleArray of the given length with all elements set to zero.
   *
   * The length of the arry cannot be changed.
   *
   * @param array
   */
  public DoubleArray(double[] array) {
    defaultArray = array.clone();
    this.array = defaultArray;
  }

  /**
   * Creates an arry with the given string being the default string.
   *
   * The given string determines the length of the array.  This lenght cannot be changed.
   * @param str
   * @throws NumberFormatException
   */
  public DoubleArray(String str) throws NumberFormatException {
    this.array = toDouble(str);
    defaultString = str;
    defaultArray = array;
  }

  /**
   * Creates a DecimalFormat for printing array elements using the given pattern and the symbols
   * for the default locale.
   *
   * @see java.text.DecimalFormat
   */
  public void setDecimalFormat(String pattern) {
    format = new DecimalFormat(pattern);
    formatExp = format;
  }

  /**
   * Gets the default array.
   *
   * The default is used if the input string is not valid due to a number format exception or an
   * array length exception.
   *
   * @return string
   */
  public String getDefault() {
    return defaultString;
  }

  /**
   * Converts the array to a comma delimited string enclosed in braces.
   *
   * @return string
   */
  public String toString() {
    if(errorcode>0) {
      return defaultString;
    }
    String str = "{"; //$NON-NLS-1$
    for(int i = 0, n = array.length; i<n; i++) {
      str += ((Math.abs(array[i])<0.1)||(Math.abs(array[i])>1000)) ? formatExp.format(array[i]) : format.format(array[i]);
      if(i<n-1) {
        str += ", "; //$NON-NLS-1$
      }
    }
    str += "}"; //$NON-NLS-1$
    return str;
  }

  /**
   * Gets the error code
   * @return
   */
  public int getError() {
    return errorcode;
  }

  /**
   * Converts the string to an array and returns the array.
   *
   * If the conversion fails, the error code is set and default array is returned.
   *
   * @return double[] the converted array
   */
  public double[] getArray(String str) {
    set(str);
    return array;
  }

  /**
   * Gets the array of doubles.
   * @return
   */
  public double[] getArray() {
    return array;
  }

  /**
   * Sets the array to the given string.
   * @return true if successful; false otherwise
   */
  public boolean set(String str) {
    errorcode = 0;
    try {
      array = toDouble(str);
    } catch(NumberFormatException ex) {
      errorcode = NumberFormatError;
      array = toDouble(defaultString);
      return false;
    } catch(ArrayIndexOutOfBoundsException ex) {
      errorcode = ArrayIndexOutOfBoundsError;
      array = toDouble(defaultString);
      return false;
    }
    return true;
  }

  public void setDefaultArray(double[] array) {
    defaultArray = array.clone();
    this.array = defaultArray;
  }

  /**
   * Converts a comma delimited string enclosed in braces to an array.
   *
   * v={1.5, 2.0, -3.2} would returns an array with componets (1.5,2.0,-3.2).
   * @param str
   * @return
   */
  protected double[] toDouble(String str) throws ArrayIndexOutOfBoundsException {
    if(str==null) {
      str = "{}"; //$NON-NLS-1$
    }
    double[] array = null;
    int start = str.indexOf("{")+1; //$NON-NLS-1$
    int end = str.indexOf("}");     //$NON-NLS-1$
    if(end-start<=0) {
      errorcode = ArrayIndexOutOfBoundsError;
      return defaultArray;
    }
    String[] s = str.substring(start, end).split(","); //$NON-NLS-1$
    if((this.array!=null)&&(this.array.length!=s.length)) {
      throw new ArrayIndexOutOfBoundsException("Array length cannot be changed in DoubleArray. "+str); //$NON-NLS-1$
    }
    array = new double[s.length];
    for(int i = 0, n = s.length; i<n; i++) {
      try {
        array[i] = Double.parseDouble(s[i]);
      } catch(NumberFormatException ex) {
        errorcode = NumberFormatError;
      }
    }
    return array;
  }

  /*
   * Tests this class.
   * @param args  command line parameters
   */
  /*
  public static void main(String[] args) {
    DoubleArray a = new DoubleArray("{1.5, 2.0, -3.2}");
    System.out.println(a);
    a.set("{3.0, -500, 0}");    // set new value
    System.out.println(a);
    a.set("{xx.5, 2.0, -3.2}"); // array set to default due to error in input
    System.out.println(a);
    a.set("{3.0, -500, 0, 0}"); // array set to default due to error in input
  }*/

  /**
 * Returns an XML.ObjectLoader to save and load object data.
 *
 * @return the XML.ObjectLoader
 */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load DoubleArray data.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves DoubleArray data in an XMLControl.
     *
     * @param control the control
     * @param obj the DrawingPanel to save
     */
    public void saveObject(XMLControl control, Object obj) {
      DoubleArray array = (DoubleArray) obj;
      control.setValue("data", array.getArray()); //$NON-NLS-1$
    }

    /**
     * Creates a DoubleArray.
     *
     * @param control the control
     * @return the newly created panel
     */
    public Object createObject(XMLControl control) {
      return new DoubleArray((double[]) control.getObject("data")); //$NON-NLS-1$
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      DoubleArray dataArray = (DoubleArray) obj;
      double[] data = (double[]) control.getObject("data"); //$NON-NLS-1$
      dataArray.array = data;
      dataArray.defaultArray = data;
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
