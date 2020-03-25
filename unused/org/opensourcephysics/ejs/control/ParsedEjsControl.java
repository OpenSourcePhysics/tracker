/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import org.opensourcephysics.controls.SimControl;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * An Ejs control that behaves like a standard OSP control insofar as it parses mathematical expressions
 * stored as strings to produce integers and doubles.
 *
 * @author W. Christian
 * @version 1.0
 */
public class ParsedEjsControl extends EjsControl implements SimControl {
  /**
   * Constructor ParsedEjsControl
   * @param simulation
   */
  public ParsedEjsControl(Object simulation) {
    super(simulation);
  }

  /**
   * Gets the double keyed to this value.
   *
   * String values are converted to double using a math expression parser.
   *
   * @param var String
   * @return double
   */
  public double getDouble(String var) {
    Value value = getValue(var);
    if(value instanceof DoubleValue) {
      return super.getDouble(var);
    } else if(value instanceof IntegerValue) {
      return super.getInt(var);
    } else {
      String str = super.getString(var);
      try {
        return Double.parseDouble(str);
      } catch(NumberFormatException ex) {
        return org.opensourcephysics.numerics.Util.evalMath(str);
      }
    }
  }

  /**
   * Gets the object keyed to the variable.
   * @param var String
   * @return Object
   */
  public Object getObject(String var) {
    Value value = getValue(var);
    if(value==null) {
      return null;
    } else if(value instanceof IntegerValue) {
      return new Integer(super.getInt(var));
    } else if(value instanceof DoubleValue) {
      return new Double(super.getDouble(var));
    } else if(value instanceof BooleanValue) {
      return new Boolean(super.getBoolean(var));
    } else if(value instanceof StringValue) {
      return super.getString(var);
    }
    return super.getObject(var);
  }

  /**
   * Gets the integer keyed to this value.
   *
   * String values are converted to int using a math expression parser.
   *
   * @param var String
   * @return double
   */
  public int getInt(String var) {
    Value value = getValue(var);
    if(value instanceof IntegerValue) {
      return super.getInt(var);
    } else if(value instanceof DoubleValue) {
      return(int) super.getDouble(var);
    } else {
      String str = super.getString(var);
      try {
        return Integer.parseInt(str);
      } catch(NumberFormatException ex) {
        return(int) org.opensourcephysics.numerics.Util.evalMath(str);
      }
    }
  }

  // Ejs Control properties are set within the model.  Variables can be changed at any time.
  public void removeParameter(String name) {
    setValue(name, (Object) null);
    variableTable.remove(name);
  }

  public void setAdjustableValue(String name, boolean val) {
    setValue(name, val);
  }

  public void setAdjustableValue(String name, double val) {
    setValue(name, val);
  }

  public void setAdjustableValue(String name, int val) {
    setValue(name, val);
  }

  public void setAdjustableValue(String name, Object val) {
    setValue(name, val);
  }

  public void setParameterToFixed(String name, boolean fixed) {
    //  Do nothing here.  Model should set visual element's enabled and editable property.
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
