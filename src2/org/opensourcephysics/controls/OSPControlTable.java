/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.opensourcephysics.numerics.DoubleArray;
import org.opensourcephysics.numerics.IntegerArray;

/**
 *
 * OSPControlTable automatically converts strings, such "pi" or "sqrt(2)" to numbers when
 * getDouble and getInt are invoked.
 *
 * @author W. Christian
 * @version 1.1
 */
public class OSPControlTable extends XMLTable implements Control {
  static Color ERROR_COLOR = Color.PINK;
  private HashMap<String, Double> valueCache = new HashMap<String, Double>();
  private boolean lockValues = false;
  private DecimalFormat format;

  /**
   * Constructs OSPControlTable and creates an XMLControlElement.
   */
  public OSPControlTable() {
    this(new XMLControlElement());
  }

  /**
   * Constructs OSPControlTable with the given control.
   * @param control XMLControlElement
   */
  public OSPControlTable(XMLControlElement control) {
    super(control);
  }

  /**
   * Locks the control's interface. Values sent to the control will not
   * update the display until the control is unlocked.
   *
   * @param lock boolean
   */
  public void setLockValues(boolean lock) {
    tableModel.control.setLockValues(lock);
    lockValues = lock;
    if(!lockValues) {
      refresh();
    }
  }

  /**
   *  Adds a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, Object val) {
    if(getBackgroundColor(par)==ERROR_COLOR) {
      setBackgroundColor(par, Color.WHITE);
    }
    tableModel.control.setValue(par, val);
    if(!lockValues) {
      refresh();
    }
  }

  /**
   * Sets the format pattern used for floating point numbers.
   * @param pattern String
   */
  public void setDecimalFormat(String pattern) {
    if(pattern==null) {
      format = null;
    } else {
      format = new DecimalFormat(pattern);
    }
  }

  /**
   *  Adds an initial value of a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, double val) {
    if(format==null) {
      setValue(par, Double.toString(val));
    } else {
      setValue(par, format.format(val));
    }
    if(!Double.isNaN(val)) {
      valueCache.put(par, new Double(val)); // store last good value
    }
  }

  /**
   *  Adds an initial value of a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, int val) {
    setValue(par, Integer.toString(val));
    valueCache.put(par, new Double(val)); // store last good value
  }

  public void setValue(String par, boolean val) {
    if(getBackgroundColor(par)==ERROR_COLOR) {
      setBackgroundColor(par, Color.WHITE);
    }
    tableModel.control.setValue(par, val);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   * @param  par
   * @return int the value of of the parameter
   */
  public int getInt(String par) {
    String str = tableModel.control.getString(par);
    if(str==null) {
      str = getObject(par).toString();
    }
    // special handling for OSPCombo
    if(tableModel.control.getPropertyType(par).equals("object")) { //$NON-NLS-1$
      XMLControl c = tableModel.control.getChildControl(par);
      if(c.getObjectClass()==OSPCombo.class) {
        OSPCombo combo = (OSPCombo) c.loadObject(null);
        return combo.getSelectedIndex();
      }
    }
    if(str==null) {
      setBackgroundColor(par, ERROR_COLOR);
      refresh();
      if(valueCache.containsKey(par)) {
        return(int) valueCache.get(par).doubleValue();
      }
      return 0;
    }
    Color color = cellColors.get(par);
    boolean editable = isEditable(par);
    try {
      int val = Integer.parseInt(par);
      if(editable&&(color!=Color.WHITE)) {                             // background is not correct so change it
        setBackgroundColor(par, Color.WHITE);
        refresh();
      } else if(!editable&&(color!=Control.NOT_EDITABLE_BACKGROUND)) { // background is not correct so change it
        setBackgroundColor(par, Control.NOT_EDITABLE_BACKGROUND);
        refresh();
      }
      valueCache.put(par, new Double(val));
      return val;
    } catch(NumberFormatException ex) {}
    try {
      int val = (int) Double.parseDouble(par);
      if(editable&&(color!=Color.WHITE)) {                             // background is not correct so change it
        setBackgroundColor(par, Color.WHITE);
        refresh();
      } else if(!editable&&(color!=Control.NOT_EDITABLE_BACKGROUND)) { // background is not correct so change it
        setBackgroundColor(par, Control.NOT_EDITABLE_BACKGROUND);
        refresh();
      }
      valueCache.put(par, new Double(val));
      return val;
    } catch(NumberFormatException ex) {}
    double dval = org.opensourcephysics.numerics.Util.evalMath(str);
    if(Double.isNaN(dval)&&(color!=ERROR_COLOR)) {
      setBackgroundColor(par, ERROR_COLOR);
      refresh();
      if(valueCache.containsKey(par)) {
        return(int) valueCache.get(par).doubleValue();
      }
      return 0;
    }
    if(editable&&(color!=Color.WHITE)) {                             // background is not correct so change it
      setBackgroundColor(par, Color.WHITE);
      refresh();
    } else if(!editable&&(color!=Control.NOT_EDITABLE_BACKGROUND)) { // background is not correct so change it
      setBackgroundColor(par, Control.NOT_EDITABLE_BACKGROUND);
      refresh();
    }
    valueCache.put(par, new Double(dval));
    return(int) dval;
  }

  /**
   * Test if the last "get" method produced an input error.
   *
   * @param par String
   * @return boolean
   */
  public boolean inputError(String par) {
    return getBackgroundColor(par)==ERROR_COLOR;
  }

  /**
   *  Reads a double value from the table.
   *
   * @param    par String the parameter key
   * @return   double the value of of the parameter
   */
  public double getDouble(String par) {
    String str = tableModel.control.getString(par);
    if(str==null) {
      str = getObject(par).toString();
    }
    if(str==null) {
      setBackgroundColor(par, ERROR_COLOR);
      refresh();
      if(valueCache.containsKey(par)) {
        return valueCache.get(par).doubleValue();
      }
      return 0;
    }
    Color color = cellColors.get(par);
    boolean editable = isEditable(par);
    try {
      double val = Double.parseDouble(str);
      if(editable&&(color!=Color.WHITE)) {                             // background is not correct so change it
        setBackgroundColor(par, Color.WHITE);
        refresh();
      } else if(!editable&&(color!=Control.NOT_EDITABLE_BACKGROUND)) { // background is not correct so change it
        setBackgroundColor(par, Control.NOT_EDITABLE_BACKGROUND);
        refresh();
      }
      valueCache.put(par, new Double(val));
      return val;
    } catch(NumberFormatException ex) {}
    double val = org.opensourcephysics.numerics.Util.evalMath(str);
    if(Double.isNaN(val)&&(color!=ERROR_COLOR)) {                    // string is not a valid number
      setBackgroundColor(par, ERROR_COLOR);
      refresh();
    } else if(editable&&(color!=Color.WHITE)) {                      // background is not correct so change it
      setBackgroundColor(par, Color.WHITE);
      refresh();
    } else if(!editable&&(color!=Control.NOT_EDITABLE_BACKGROUND)) { // background is not correct so change it
      setBackgroundColor(par, Control.NOT_EDITABLE_BACKGROUND);
      refresh();
    }
    if(Double.isNaN(val)&&valueCache.containsKey(par)) {
      val = valueCache.get(par).doubleValue();
    } else {
      valueCache.put(par, new Double(val));
    }
    return val;
  }

  /**
   * Gets the object with the specified property name.
   * Throws an UnsupportedOperationException if the named object has not been stored.
   *
   * @param  par
   * @return the object
   */
  public Object getObject(String par) throws UnsupportedOperationException {
    return tableModel.control.getObject(par);
  }

  public String getString(String par) {
    return tableModel.control.getString(par);
  }

  public boolean getBoolean(String par) {
    return tableModel.control.getBoolean(par);
  }

  public Collection<String> getPropertyNames() {
    return tableModel.control.getPropertyNames();
  }

  /**
   *  Removes a parameter from the table.
   *
   * @param  par  the parameter name
   */
  public void removeParameter(String par) {
    tableModel.control.setValue(par, null);
    setBackgroundColor(par, Color.WHITE);
  }

  public void println(String s) {
    tableModel.control.println(s);
  }

  public void println() {
    tableModel.control.println();
  }

  public void print(String s) {
    tableModel.control.print(s);
  }

  public void clearMessages() {
    tableModel.control.clearMessages();
  }

  public void clearValues() {
    tableModel.control.clearValues();
  }

  public void calculationDone(String message) {
    if(message!=null) {
      tableModel.control.calculationDone(message);
    }
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new OSPControlTableLoader();
  }

  /**
* A class to save and load data for OSPControls.
*/
  static class OSPControlTableLoader implements XML.ObjectLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param prefsXMLControl the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl xmlControl, Object obj) {
      OSPControlTable controlTable = (OSPControlTable) obj;
      Iterator<String> it = controlTable.getPropertyNames().iterator();
      while(it.hasNext()) {
        String name = it.next();
        Object val = controlTable.getObject(name);
        if(val.getClass()==DoubleArray.class) {
          xmlControl.setValue(name, ((DoubleArray) val).getArray());
        } else if(val.getClass()==IntegerArray.class) {
          xmlControl.setValue(name, ((IntegerArray) val).getArray());
        } else if(val.getClass()==Boolean.class) {
          xmlControl.setValue(name, ((Boolean) val).booleanValue());
        } else if(val.getClass()==Double.class) {
          xmlControl.setValue(name, ((Double) val).doubleValue());
        } else if(val.getClass()==Integer.class) {
          xmlControl.setValue(name, ((Integer) val).intValue());
        } else if(val.getClass().isArray()) {
          xmlControl.setValue(name, val);
        } else {
          xmlControl.setValue(name, val);
        }
      }
    }

    /**
     * Creates an OSPControlTable object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new OSPControlTable();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      OSPControlTable controlTable = (OSPControlTable) obj;
      // iterate over properties and add them to table model
      Iterator<String> it = control.getPropertyNames().iterator();
      controlTable.setLockValues(true);
      while(it.hasNext()) {
        String name = it.next();
        if(control.getPropertyType(name).equals("string")) {         //$NON-NLS-1$
          controlTable.setValue(name, control.getString(name));
        } else if(control.getPropertyType(name).equals("int")) {     //$NON-NLS-1$
          controlTable.setValue(name, control.getInt(name));
        } else if(control.getPropertyType(name).equals("double")) {  //$NON-NLS-1$
          controlTable.setValue(name, control.getDouble(name));
        } else if(control.getPropertyType(name).equals("boolean")) { //$NON-NLS-1$
          controlTable.setValue(name, control.getBoolean(name));
        } else {
          controlTable.setValue(name, control.getObject(name));
        }
      }
      controlTable.setLockValues(false);
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
