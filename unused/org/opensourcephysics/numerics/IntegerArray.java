/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * IntegerArray stores an array of doubles as a string and as an array.
*/
public class IntegerArray {
  public static int NumberFormatError = 1;
  public static int ArrayIndexOutOfBoundsError = 2;
  protected int[] array;
  protected String defaultString;
  protected int[] defaultArray;
  protected int errorcode = 0;

  /**
   * Creates a DoubleArray of the given length with all elements set to zero.
   *
   * The length of the arry cannot be changed.
   *
   * @param n
   */
  public IntegerArray(int n) {
    array = new int[n];
    defaultArray = array;
  }

  /**
   * Creates a DoubleArray of the given length with all elements set to zero.
   *
   * The length of the arry cannot be changed.
   *
   * @param array
   */
  public IntegerArray(int[] array) {
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
  public IntegerArray(String str) throws NumberFormatException {
    this.array = toInteger(str);
    defaultString = str;
    defaultArray = array;
  }

  /**
   * Gets the default array.
   *
   * The default is used if the input string is not valid due to a number format exception or an
   * array length exception.
   *
   * @return
   */
  public String getDefault() {
    return defaultString;
  }

  /**
   * Converts the array to a comma delimited string enclosed in braces.
   *
   * @return
   */
  public String toString() {
    if(errorcode>0) {
      return defaultString;
    }
    String str = "{"; //$NON-NLS-1$
    for(int i = 0, n = array.length; i<n; i++) {
      str += Integer.toString(array[i]);
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
  public int[] getArray(String str) {
    set(str);
    return array;
  }

  /**
   * Gets the array of doubles.
   * @return
   */
  public int[] getArray() {
    return array;
  }

  /**
   * Sets the array to the given string.
   * @return true if successful; false otherwise
   */
  public boolean set(String str) {
    errorcode = 0;
    try {
      array = toInteger(str);
    } catch(NumberFormatException ex) {
      errorcode = NumberFormatError;
      array = toInteger(defaultString);
      return false;
    } catch(ArrayIndexOutOfBoundsException ex) {
      errorcode = ArrayIndexOutOfBoundsError;
      array = toInteger(defaultString);
      return false;
    }
    return true;
  }

  public void setDefaultArray(int[] array) {
    defaultArray = array.clone();
    this.array = defaultArray;
  }

  /**
   * Converts a comma delimited string enclosed in braces to an array.
   *
   * v={1.5, 2.0, -3.2} would returns an array with componets (1.5,2.0,-3.2).
   * @param str
   * @return int array
   */
  protected int[] toInteger(String str) throws ArrayIndexOutOfBoundsException {
    int[] array = null;
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
    array = new int[s.length];
    for(int i = 0, n = s.length; i<n; i++) {
      try {
        array[i] = Integer.parseInt(s[i].trim());
      } catch(NumberFormatException ex) {
        array[i] = 0;
        errorcode = NumberFormatError;
      }
    }
    return array;
  }

  /**
 * Returns an XML.ObjectLoader to save and load object data.
 *
 * @return the XML.ObjectLoader
 */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load IntegerArray data.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves DoubleArray data in an XMLControl.
     *
     * @param control the control
     * @param obj the DrawingPanel to save
     */
    public void saveObject(XMLControl control, Object obj) {
      IntegerArray array = (IntegerArray) obj;
      control.setValue("data", array.getArray()); //$NON-NLS-1$
    }

    /**
     * Creates a DoubleArray.
     *
     * @param control the control
     * @return the newly created panel
     */
    public Object createObject(XMLControl control) {
      return new IntegerArray((int[]) control.getObject("data")); //$NON-NLS-1$
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      IntegerArray dataArray = (IntegerArray) obj;
      int[] data = (int[]) control.getObject("data"); //$NON-NLS-1$
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
