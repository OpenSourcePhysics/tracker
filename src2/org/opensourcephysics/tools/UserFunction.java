/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.util.TreeMap;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.numerics.MultiVarFunction;
import org.opensourcephysics.numerics.ParsedMultiVarFunction;
import org.opensourcephysics.numerics.ParserException;

/**
 * A known function for which the expression and parameters are user-editable.
 *
 * @author Douglas Brown
 */
public class UserFunction implements KnownFunction, MultiVarFunction, Cloneable {
  // static constants
  protected static String[] dummyVars = {"'", "@",       //$NON-NLS-1$ //$NON-NLS-2$
                                         "`", "~", "#"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  // instance fields
  protected String name;
  protected String[] paramNames = new String[0];
  protected double[] paramValues = new double[0];
  protected String[] paramDescriptions = new String[0];
  protected String[] functionNames = new String[0];
  protected String expression = "0";                     //$NON-NLS-1$
  protected String inputString = "0";                    //$NON-NLS-1$
  protected ParsedMultiVarFunction function = null;
  protected String[] vars = {"x"};                       //$NON-NLS-1$
  protected UserFunction[] references = new UserFunction[0];
  protected boolean nameEditable = true;
  protected String description;
  protected KnownPolynomial polynomial;

  /**
   * Constructor.
   *
   * @param name the function name
   */
  public UserFunction(String name) {
    setName(name);
    try {
      function = new ParsedMultiVarFunction("0", new String[0]); //$NON-NLS-1$
      functionNames = function.getFunctionNames();
    } catch(ParserException ex) {
      /** empty block */
    }
  }

  /**
   * Constructor that copies a KnownPolynomial.
   *
   * @param poly the KnownPolynomial
   */
  public UserFunction(KnownPolynomial poly) {
    this(poly.getName());
    polynomial = poly;
    // set up name and description
		setName(poly.getName());
    setDescription(poly.getDescription());
    
    // set up parameters
    String[] params = new String[poly.getParameterCount()];
    double[] paramValues = new double[poly.getParameterCount()];
    String[] desc = new String[poly.getParameterCount()];
    for (int i=0; i<params.length; i++) {
    	params[i] = poly.getParameterName(i);
    	paramValues[i] = poly.getParameterValue(i);
    	desc[i] = poly.getParameterDescription(i);
    }
  	setParameters(params, paramValues, desc);
  	
  	// set expression
    setExpression(poly.getExpression("x"), new String[] {"x"}); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name) {
    if(!isNameEditable()) {
      return;
    }
    this.name = name;
  }

  /**
   * Returns true if the name is user-editable.
   *
   * @return true if editable
   */
  public boolean isNameEditable() {
    return nameEditable;
  }

  /**
   * Sets the name editable property.
   *
   * @param editable true if editable
   */
  public void setNameEditable(boolean editable) {
    nameEditable = editable;
  }

  /**
   * Gets the current independent variable.
   *
   * @return the variable name
   */
  public String getIndependentVariable() {
    return vars[0];
  }

  /**
   * Gets the current independent variables.
   *
   * @return the variable names
   */
  public String[] getIndependentVariables() {
    return vars;
  }

  /**
   * Gets the expression.
   *
   * @return the expression
   */
  public String getInputString() {
    // replace dummys with var names
    String s = inputString;
    for(int i = 0; i<vars.length; i++) {
      s = s.replaceAll(dummyVars[i], vars[i]);
    }
    return s;
  }

  /**
   * Gets the expression using the current variables.
   *
   * @return the expression
   */
  public String getExpression() {
    // replace dummys with var names
    String s = expression;
    for(int i = 0; i<vars.length; i++) {
      s = s.replaceAll(dummyVars[i], vars[i]);
    }
    return s;
  }

  /**
   * Gets the expression and sets the independent variable.
   *
   * @param indepVarName the name of the independent variable
   * @return the expression
   */
  public String getExpression(String indepVarName) {
    vars = new String[] {indepVarName};
    return getExpression();
  }

  /**
   * Gets the expression and sets the independent variables.
   *
   * @param varNames the name of the independent variables
   * @return the expression
   */
  public String getExpression(String[] varNames) {
    vars = varNames;
    return getExpression();
  }

  /**
   * Gets the full expression using the current variables.
   *
   * @param varNames the name of the independent variables
   * @return the expression
   */
  public String getFullExpression(String[] varNames) {
    String s = getExpression(varNames);
    // replace references with their full expressions in parentheses
    for(UserFunction f : references) {
      s = s.replaceAll(f.getName(), "("+f.getFullExpression(varNames)+")"); //$NON-NLS-1$//$NON-NLS-2$
    }
    return s;
  }

  /**
   * Sets the expression.
   *
   * @param exp a parsable expression of the parameters and variables
   * @param varNames the names of the independent variables
   * @return true if successfully parsed
   */
  public boolean setExpression(String exp, String[] varNames) {
    vars = varNames;
    String[] names = new String[references.length+paramNames.length+vars.length];
    for(int i = 0; i<paramNames.length; i++) {
      names[i+vars.length] = paramNames[i];
    }
    for(int i = 0; i<references.length; i++) {
      names[i+vars.length+paramNames.length] = references[i].getName();
    }
    // replace indep vars with dummys starting with longest names
    java.util.ArrayList<String> sorted = new java.util.ArrayList<String>();
    sorted.add(vars[0]);
    // fill and sort the sorted list
    for(int i = 1; i<vars.length; i++) {
      int size = sorted.size();
      for(int j = 0; j<size; j++) {
        if(vars[i].length()>sorted.get(j).toString().length()) {
          sorted.add(j, vars[i]);
          break;
        } else if(j==size-1) {
          sorted.add(vars[i]);
        }
      }
    }
    // replace strings in both expression and names in sorted list order
    for(int k = 0; k<sorted.size(); k++) {
      String next = sorted.get(k).toString();
      for(int i = 0; i<vars.length; i++) {
        if(next.equals(vars[i])) {
          exp = exp.replaceAll(vars[i], dummyVars[i]);
          names[i] = dummyVars[i];
          for(int j = vars.length; j<names.length; j++) {
            names[j] = names[j].replaceAll(vars[i], dummyVars[i]);
          }
          // replace modified function names with originals
          for(int j = 0; j<functionNames.length; j++) {
            String modified = functionNames[j].replaceAll(vars[i], dummyVars[i]);
            if(!modified.equals(functionNames[j])) {
              exp = exp.replaceAll(modified, functionNames[j]);
            }
          }
        }
      }
    }
    // replace modified names with originals
    for(int i = 0; i<paramNames.length; i++) {
      exp = exp.replaceAll(names[vars.length+i], paramNames[i]);
      names[vars.length+i] = paramNames[i];
    }
    for(int i = 0; i<references.length; i++) {
      exp = exp.replaceAll(names[vars.length+paramNames.length+i], references[i].getName());
      names[vars.length+paramNames.length+i] = references[i].getName();
    }
    inputString = exp;
    // try to parse expression
    try {
      function = new ParsedMultiVarFunction(exp, names);
      // successful, so save expression unless it contains "="
      if(exp.indexOf("=")==-1) {                           //$NON-NLS-1$
        expression = exp;
        return true;
      }
    } catch(ParserException ex) {
      try {
        function = new ParsedMultiVarFunction("0", names); //$NON-NLS-1$
      } catch(ParserException ex2) {
        /** empty block */
      }
      expression = "0";                                    //$NON-NLS-1$
    }
    return false;
  }

  /**
   * Gets the parameter count.
   * @return the number of parameters
   */
  public int getParameterCount() {
    return paramNames.length;
  }

  /**
   * Gets a parameter name.
   *
   * @param i the parameter index
   * @return the name of the parameter
   */
  public String getParameterName(int i) {
    return paramNames[i];
  }

  /**
   * Gets a parameter value.
   *
   * @param i the parameter index
   * @return the value of the parameter
   */
  public double getParameterValue(int i) {
    return paramValues[i];
  }

  /**
   * Sets a parameter value.
   *
   * @param i the parameter index
   * @param value the value
   */
  public void setParameterValue(int i, double value) {
    paramValues[i] = value;
  }

  /**
   * Sets the parameters.
   *
   * @param names the parameter names
   * @param values the parameter values
   */
  public void setParameters(String[] names, double[] values) {
    paramNames = names;
    paramValues = values;
  }

  /**
   * Sets the parameters.
   *
   * @param names the parameter names
   * @param values the parameter values
   * @param descriptions the parameter descriptions
   */
  public void setParameters(String[] names, double[] values, String[] descriptions) {
    paramNames = names;
    paramValues = values;
    if (descriptions!=null) {
      paramDescriptions = descriptions;
    }
  }

  /**
   * Sets the parameters of reference functions to those of this function.
   */
  public void updateReferenceParameters() {
    for(UserFunction next : references) {
      next.setParameters(paramNames, paramValues, paramDescriptions);
      next.updateReferenceParameters();
    }
  }

  /**
   * Sets the reference functions.
   *
   * @param functions the functions referenced by this one
   */
  public void setReferences(UserFunction[] functions) {
    references = functions;
  }

  /**
   * Gets the description of this function. May return null.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this function.
   *
   * @param desc the description
   */
  public void setDescription(String desc) {
    description = desc;
  }
  
  /**
   * Gets a parameter description. May be null.
   *
   * @param i the parameter index
   * @return the description of the parameter (may be null)
   */
  public String getParameterDescription(int i) {
  	if (i>=paramDescriptions.length) return null;
  	return paramDescriptions[i];
  }

  /**
   * Returns function names.
   * Added by D. Brown 10 Dec 2015
   *
   * @return array of parser function names
   */
  public String[] getFunctionNames() {
    return functionNames;
  }

  /**
   * Evaluates the function for a single variable x.
   *
   * @param x
   * @return f(x)
   */
  public double evaluate(double x) {
    if(function==null) {
      return Double.NaN;
    }
    double[] supportValues = evaluateSupportFunctions(x);
    int n = supportValues.length+paramValues.length+1;
    double[] values = new double[n];
    values[0] = x;
    System.arraycopy(paramValues, 0, values, 1, paramValues.length);
    System.arraycopy(supportValues, 0, values, 1+paramValues.length, supportValues.length);
    return function.evaluate(values);
  }

  /**
   * Evaluates the function for a variables array x.
   *
   * @param x
   * @return f(x)
   */
  public double evaluate(double[] x) {
    if(function==null) {
      return Double.NaN;
    }
    double[] support = evaluateSupportFunctions(x);
    int n = support.length+paramValues.length+x.length;
    double[] values = new double[n];
    System.arraycopy(x, 0, values, 0, x.length);
    System.arraycopy(paramValues, 0, values, x.length, paramValues.length);
    System.arraycopy(support, 0, values, x.length+paramValues.length, support.length);
    return function.evaluate(values);
  }

  /**
   * Determines if last evaluation resulted in NaN.
   *
   * @return true if result was converted from NaN to zero
   */
  public boolean evaluatedToNaN() {
  	return function==null? false: function.evaluatedToNaN();
  }

  /**
   * Returns a clone of this UserFunction.
   *
   * @return the clone
   */
  public UserFunction clone() {
    UserFunction f = new UserFunction(name);
    f.setDescription(description);
    f.setNameEditable(nameEditable);
    f.setParameters(paramNames, paramValues, paramDescriptions);
    UserFunction[] refs = new UserFunction[references.length];
    for(int i = 0; i<refs.length; i++) {
      refs[i] = references[i].clone();
    }
    f.setReferences(refs);
    f.setExpression(inputString, vars);
    f.polynomial = polynomial==null? null: polynomial.clone();
    return f;
  }

  /**
   * Determines if another KnownFunction is the same as this one.
   *
   * @param f the KnownFunction to test
   * @return true if equal
   */
  @Override
  public boolean equals(Object f) {
  	if (!(f instanceof UserFunction)) return false;
  	UserFunction uf = (UserFunction)f;
  	if (!getName().equals(uf.getName())) return false;
  	if (!getInputString().equals(uf.getInputString())) return false;
  	int n = getParameterCount();
  	if (n!=uf.getParameterCount()) return false;
  	for (int i=0; i<n; i++) {
  		if (!getParameterName(i).equals(uf.getParameterName(i))) return false;
  	}
  	// ignore descriptions and parameter values
  	return true;
  }
  /**
   * Updates the associated polynomial, if any, with this functions current properties.
   * 
   * @return true if updated
   */
  public boolean updatePolynomial() {
  	if (polynomial==null) return false;
    // update name and description
  	// see if function name is different than default polynomial name
		polynomial.setName(this.getName());
		polynomial.setDescription(this.getDescription());
    
    // update parameters
    polynomial.setParameters(paramNames, paramValues, paramDescriptions);  
    return true;
  }
  
  /**
   * Replaces a parameter name with a new one in the function expression.
   *
   * @param oldName the existing parameter name
   * @param newName the new parameter name
   * @return the modified expression, or null if failed
   */
  protected String replaceParameterNameInExpression(String oldName, String newName) {
		String[] varNames = getIndependentVariables();
		String expression = getInputString();
		TreeMap<String, String> replacements = new TreeMap<String, String>();
		
		expression = replaceInExpression(oldName, newName, expression, replacements);
		if (expression==null) return null;
		
		// restore dummy names
		for (String key: replacements.keySet()) {
			if (key.equals(newName)) continue;
			expression = expression.replaceAll(key, replacements.get(key));
		}
		if (setExpression(expression, varNames)) return expression;
		return null;
  }
  
  /**
   * Replaces a parameter name with a new one in a specified expression.
   *
   * @param oldName the existing parameter name
   * @param newName the new parameter name
   * @param expression the expression to modify
   * @param replacements a map of parameter replacements already made
   * @return the modified expression, or null if failed
   */
  private String replaceInExpression(String oldName, String newName, 
  		String expression, TreeMap<String, String> replacements) {
		if (replacements.values().contains(oldName)) return expression;
		
  	for (int i=0; i<getParameterCount(); i++) {
			String nextParam = getParameterName(i);
			if (oldName.equals(nextParam) || newName.equals(nextParam)) continue;
			if (nextParam.contains(oldName)) {
				// replace nextParam with dummy name
				int k = 0;
				for (int j=0; j<dummyVars.length; j++) {
					if (dummyVars[j].equals(newName)) {
						k = j+1;
						break;
					}
				}
				if (k>=dummyVars.length) return null;
				expression = replaceInExpression(nextParam, dummyVars[k], expression, replacements);
				if (expression==null) return null;
			}
		}
  	
		expression = expression.replaceAll(oldName, newName);
		replacements.put(newName, oldName);
		return expression;
  }
  
  /**
   * Evaluates the support functions for a single variable x.
   *
   * @param x
   * @return double[] of values
   */
  protected double[] evaluateSupportFunctions(double x) {
    double[] values = new double[references.length];
    for(int i = 0; i<values.length; i++) {
      values[i] = references[i].evaluate(x);
    }
    return values;
  }

  /**
   * Evaluates the support functions for a variables array x.
   *
   * @param x
   * @return double[] of values
   */
  protected double[] evaluateSupportFunctions(double[] x) {
    double[] values = new double[references.length];
    for(int i = 0; i<values.length; i++) {
      values[i] = references[i].evaluate(x);
    }
    return values;
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
   * A class to save and load UserFunction data in an XMLControl.
   */
  protected static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      UserFunction f = (UserFunction) obj;
      control.setValue("name", f.getName());                      //$NON-NLS-1$
      control.setValue("description", f.getDescription());        //$NON-NLS-1$
      control.setValue("name_editable", f.isNameEditable());      //$NON-NLS-1$
      control.setValue("parameter_names", f.paramNames);          //$NON-NLS-1$
      control.setValue("parameter_values", f.paramValues);        //$NON-NLS-1$
      control.setValue("parameter_descriptions", f.paramDescriptions);        //$NON-NLS-1$
      control.setValue("variables", f.getIndependentVariables()); //$NON-NLS-1$
      control.setValue("expression", f.getInputString());         //$NON-NLS-1$
      if (f.polynomial!=null) {
        control.setValue("polynomial", f.polynomial.getCoefficients());  //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control) {
      String name = control.getString("name"); //$NON-NLS-1$
      return new UserFunction(name);
    }

    public Object loadObject(XMLControl control, Object obj) {
      UserFunction f = (UserFunction) obj;
      f.setName(control.getString("name"));               //$NON-NLS-1$
      f.setDescription(control.getString("description")); //$NON-NLS-1$
      if(control.getPropertyNames().contains("name_editable")) { //$NON-NLS-1$
        f.setNameEditable(control.getBoolean("name_editable"));  //$NON-NLS-1$
      }
      String[] names = (String[]) control.getObject("parameter_names");   //$NON-NLS-1$
      if(names!=null) {
        double[] values = (double[]) control.getObject("parameter_values"); //$NON-NLS-1$
        String[] desc = (String[]) control.getObject("parameter_descriptions");   //$NON-NLS-1$
        f.setParameters(names, values, desc);
      }
      String[] vars = (String[]) control.getObject("variables"); //$NON-NLS-1$
      if(vars==null) {                              // for legacy code
        String var = control.getString("variable"); //$NON-NLS-1$
        vars = new String[] {var};
      }
      f.setExpression(control.getString("expression"), vars); //$NON-NLS-1$
      double[] coeff = (double[])control.getObject("polynomial"); //$NON-NLS-1$
      if (coeff!=null) {
      	f.polynomial = new KnownPolynomial(coeff);
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
