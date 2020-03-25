/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.value;

/**
 * @see     Value
 */
public class ObjectValue extends Value {
  public Object value;

  /**
   * Constructor ObjectValue
   * @param _val
   */
  public ObjectValue(Object _val) {
    value = _val;
  }

  public boolean getBoolean() {
    if(value==null) {
      return false;
    }
    return value.toString().equals("true"); //$NON-NLS-1$
  }

  public int getInteger() {
    return(int) Math.round(getDouble());
  }

  public double getDouble() {
    try {
      return Double.valueOf(value.toString()).doubleValue();
    } catch(NumberFormatException exc) {
      return 0.0;
    }
  }

  public String getString() {
    if(value==null) {
      return null;
    }
    return value.toString();
  }

  public Object getObject() {
    return value;
  }
  // public void copyInto (double[] array) {
  // double[] data = (double[]) value;
  // int n = data.length;
  // if (array.length<n) n = array.length;
  // System.arraycopy(data,0,array,0,n);
  // }
  //
  // public void copyInto (double[][] array) {
  // double[][] data = (double[][]) value;
  // int n = data.length;
  // if (array.length<n) n = array.length;
  // for (int i=0; i<n; i++) {
  // int ni = data[i].length;
  // if (array[i].length<ni) ni = array[i].length;
  // System.arraycopy(data[i],0,array[i],0,ni);
  // }
  // }

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
