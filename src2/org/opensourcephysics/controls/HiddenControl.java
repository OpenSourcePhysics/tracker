/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashMap;

/**
 * A Control without a graphical user interface.
 *
 * @author       Joshua Gould
 * @version 1.0
 */
public class HiddenControl implements Control {
  HashMap<String, String> map = new HashMap<String, String>();
  NumberFormat numberFormat = NumberFormat.getInstance();

  /**
   * Locks the control's interface. Values sent to the control will not
   * update the display until the control is unlocked.
   *
   * @param lock boolean
   */
  public void setLockValues(boolean lock) {
    // this control does not have a user interface
  }

  /**
    *  Adds an initial value of a parameter to the input display. Input parameters
    *  should be read when the calculation is performed.
    *
    *@param  par  the parameter name
    *@param  val  the initial parameter value
    *@since
    */
  public void setValue(String par, Object val) {
    map.put(par, val.toString());
  }

  /**
   *  Add an initial boolean value of a parameter to the input display. Input
   *  parameters should be read when the calculation is performed.
   *
   *@param  par  the parameter name
   *@param  val  the initial parameter value
   *@since
   */
  public void setValue(String par, boolean val) {
    map.put(par, String.valueOf(val));
  }

  /**
   *  Add an initial value of a parameter to the input display. Input parameters
   *  should be read when the calculation is performed.
   *
   *@param  par  the parameter name
   *@param  val  the initial parameter value
   *@since
   */
  public void setValue(String par, double val) {
    map.put(par, Double.toString(val));
  }

  /**
   *  Add an initial value of a parameter to the input display. Input parameters
   *  should be read when the calculation is performed.
   *
   *@param  par  the parameter name
   *@param  val  the initial parameter value
   *@since
   */
  public void setValue(String par, int val) {
    map.put(par, Integer.toString(val));
  }

  public void scriptValue(String par, String val) {
    map.put(par, val);
  }

  /**



/**
* Gets the names of all properties stored in this control.
*
* @return List
*/
  public java.util.Collection<String> getPropertyNames() {
    return map.keySet();
  }

  /**
   *  Read a parameter value from the input display.
   *
   *@param  par
   *@return      double the value of of the parameter
   *@since
   */
  public double getDouble(String par) {
    String str = getString(par);
    if(str.equals("")) { //$NON-NLS-1$
      return 0;
    }
    try {
      ParsePosition parsePosition = new ParsePosition(0);
      double val = numberFormat.parse(str, parsePosition).doubleValue();
      if(str.length()==parsePosition.getIndex()) {
        println("Variable "+par+" is not a number"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return val;
    } catch(Exception e) {
      println("Variable "+par+" is not a number");   //$NON-NLS-1$ //$NON-NLS-2$
      return 0;
    }
  }

  /**
   *  Read a parameter value from the input display.
   *
   *@param  par
   *@return      int the value of of the parameter
   *@since
   */
  public int getInt(String par) {
    int val = (int) getDouble(par);
    return val;
  }

  /**
   * Gets the object with the specified property name.
   * Throws an UnsupportedOperationException if the named object has not been stored.
   *
   * @param name the name
   * @return the object
   */
  public Object getObject(String name) {
    return map.get(name);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   *@param  par  the parameter name
   *@return      String the value of of the parameter
   *@since
   */
  public String getString(String par) {
    String str = map.get(par);
    if(str==null) {
      println("Variable "+par+" not found."); //$NON-NLS-1$ //$NON-NLS-2$
      return "";                              //$NON-NLS-1$
    }
    return str;
  }

  /**
   *  Read a parameter value from the input display.
   *
   *@param  par  the parameter name
   *@return      the value of of the parameter
   *@since
   */
  public boolean getBoolean(String par) {
    String str = getString(par);
    if(str.equals("")) { //$NON-NLS-1$
      return false;
    }
    str = str.toLowerCase().trim();
    if(str.equals("true")) { //$NON-NLS-1$
      return true;
    }
    if(str.equals("false")) { //$NON-NLS-1$
      return false;
    }
    println("Error: Boolean variable must be true or false."); //$NON-NLS-1$
    return false;
  }

  public void println(String s) {
    System.out.println(s);
  }

  public void println() {
    System.out.println();
  }

  public void print(String s) {
    System.out.println(s);
  }

  public void clearMessages() {}

  public void calculationDone(String s) {}

  public void clearValues() {}

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
