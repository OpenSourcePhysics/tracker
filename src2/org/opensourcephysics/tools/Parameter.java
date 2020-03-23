/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.util.List;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.numerics.MultiVarFunction;
import org.opensourcephysics.numerics.ParsedMultiVarFunction;
import org.opensourcephysics.numerics.ParserException;

/**
 * This represents a parameter expression that is parsed and
 * evaluated as a function of other parameters.
 *
 *
 * @author Douglas Brown
 */
public class Parameter {
  final String paramName;    // name of this parameter
  final String expression;   // Suryono parser expression
  String description;        // optional description of this parameter
  double value = Double.NaN; // current value of this parameter
  boolean expressionEditable = true;
  boolean nameEditable = true;

  /**
   * Constructor with name and function.
   *
   * @param name the name
   * @param function the function (parser expression)
   */
  public Parameter(String name, String function) {
    paramName = name;
    expression = function;
  }

  /**
   * Constructor with name, function and description.
   *
   * @param name the name
   * @param function the function (parser expression)
   * @param desc the description
   */
  public Parameter(String name, String function, String desc) {
    this(name, function);
    setDescription(desc);
  }

  /**
   * Gets the name of this parameter.
   *
   * @return the name
   */
  public String getName() {
    return paramName;
  }

  /**
   * Gets the expression for this parameter.
   *
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  /**
   * Gets the description of this parameter. May return null.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this parameter.
   *
   * @param desc the description
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * Gets the current value of this parameter.
   *
   * @return the value (may be NaN)
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns true if this parameter's expression is user-editable.
   *
   * @return true if editable
   */
  public boolean isExpressionEditable() {
    return expressionEditable;
  }

  /**
   * Sets the expression editable property.
   *
   * @param edit true if editable
   */
  public void setExpressionEditable(boolean edit) {
    expressionEditable = edit;
  }

  /**
   * Returns true if this parameter's name is user-editable.
   *
   * @return true if editable
   */
  public boolean isNameEditable() {
    return nameEditable;
  }

  /**
   * Sets the name editable property.
   *
   * @param edit true if editable
   */
  public void setNameEditable(boolean edit) {
    nameEditable = edit;
  }

  /**
   * Determines if this is equal to another parameter.
   *
   * @param obj another object
   * @return true if equal
   */
  public boolean equals(Object obj) {
    if(obj instanceof Parameter) {
      Parameter p = (Parameter) obj;
      return p.getName().equals(paramName)&&p.getExpression().equals(expression)&&(p.isExpressionEditable()==expressionEditable)&&(p.isNameEditable()==nameEditable);
    }
    return false;
  }

  /**
   * Determines the value of this parameter based on input parameter values.
   *
   * @param parameters the input parameters
   * @return the value (may be NaN)
   */
  protected double evaluate(List<?> parameters) {
    int n = parameters.contains(this) ? parameters.size()-1 : parameters.size();
    Parameter[] array = new Parameter[n];
    int j = 0;
    for(int i = 0; i<parameters.size(); i++) {
      Parameter next = (Parameter) parameters.get(i);
      if(next==this) {
        continue;
      }
      array[j++] = next;
    }
    return evaluate(array);
  }

  /**
   * Determines the value of this parameter based on input parameter values.
   * This method assumes the array of parameters does not include this.
   *
   * @param parameters the input parameters
   * @return the value (may be NaN)
   */
  public double evaluate(Parameter[] parameters) {
    int n = parameters.length;
    String[] names = new String[n];
    double[] values = new double[n];
    for(int i = 0; i<n; i++) {
      names[i] = parameters[i].paramName;
      values[i] = parameters[i].value;
    }
    try {
      String express = expression;
      // Suryono parser accepts only periods as decimal separators
      // but don't make substitutions in "if" statements since they use commas
      if(express.indexOf("if")==-1) { //$NON-NLS-1$
      	express = express.replaceAll(",", "."); //$NON-NLS-1$ //$NON-NLS-2$
      }
      MultiVarFunction f = new ParsedMultiVarFunction(express, names);
      value = f.evaluate(values);
    } catch(ParserException ex) {
      value = Double.NaN;
    }
    return value;
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      Parameter p = (Parameter) obj;
      control.setValue("name", p.getName());                  //$NON-NLS-1$
      control.setValue("function", p.getExpression());        //$NON-NLS-1$
      control.setValue("editable", p.isExpressionEditable()); //$NON-NLS-1$
      control.setValue("name_editable", p.isNameEditable());  //$NON-NLS-1$
      control.setValue("description", p.getDescription());    //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the control with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      String name = control.getString("name");  //$NON-NLS-1$
      String f = control.getString("function"); //$NON-NLS-1$
      return new Parameter(name, f);
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      Parameter p = (Parameter) obj;
      if(control.getPropertyNames().contains("editable")) {      //$NON-NLS-1$
        p.setExpressionEditable(control.getBoolean("editable")); //$NON-NLS-1$
      }
      if(control.getPropertyNames().contains("name_editable")) { //$NON-NLS-1$
        p.setNameEditable(control.getBoolean("name_editable"));  //$NON-NLS-1$
      }
      p.setDescription(control.getString("description")); //$NON-NLS-1$
      return obj;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
