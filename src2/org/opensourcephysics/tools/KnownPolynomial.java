/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import org.opensourcephysics.numerics.PolynomialLeastSquareFit;

/**
 * A polynomial that implements KnownFunction. Limited to degree 5 or less.
 */
public class KnownPolynomial extends PolynomialLeastSquareFit implements KnownFunction {
  String name;
  String description;
	String[] paramNames = {"A", "B", "C",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                         "D", "E", "F"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  String[] paramDescriptions;

  KnownPolynomial(double[] xdata, double[] ydata, int degree) {
    super(xdata, ydata, degree);
  }

  KnownPolynomial(double[] coeffs) {
    super(coeffs);
  }

  /**
   * Gets the parameter count.
   * @return the number of parameters
   */
  public int getParameterCount() {
    return coefficients.length;
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
   * Gets a parameter description. May be null.
   *
   * @param i the parameter index
   * @return the description of the parameter (may be null)
   */
  public String getParameterDescription(int i) {
  	if (paramDescriptions!=null && paramDescriptions.length>i) {
  		return paramDescriptions[i];
  	}
  	if (getParameterCount()==2) {
  		if (i==0) return ToolsRes.getString("Function.Parameter.Slope.Description"); //$NON-NLS-1$
  		return ToolsRes.getString("Function.Parameter.Intercept.Description"); //$NON-NLS-1$
  	}
  	return null;
  }

  /**
   * Gets a parameter value.
   *
   * @param i the parameter index
   * @return the value of the parameter
   */
  public double getParameterValue(int i) {
    return coefficients[coefficients.length-i-1];
  }

  /**
   * Sets a parameter value.
   *
   * @param i the parameter index
   * @param value the value
   */
  public void setParameterValue(int i, double value) {
  	if (Double.isNaN(value)) return;
    coefficients[coefficients.length-i-1] = value;
  }

  /**
   * Sets the parameters.
   *
   * @param names the parameter names (may be null)
   * @param values the parameter values (may be null)
   * @param descriptions the parameter descriptions (may be null)
   */
  public void setParameters(String[] names, double[] values, String[] descriptions) {
  	if (names!=null) {
			for (int i=0; i<Math.min(names.length, getParameterCount()); i++) {
				if (names[i]==null || "".equals(names[i].trim())) continue; //$NON-NLS-1$
				paramNames[i] = names[i];
			}
  	}
  	paramDescriptions = descriptions;
  	if (values!=null) {
  		for (int i=0; i<Math.min(values.length, getParameterCount()); i++) {
  			setParameterValue(i, values[i]);
  		}
  	}
  }

  /**
   * Gets the expression.
   *
   * @param indepVarName the name of the independent variable
   * @return the equation expression
   */
  public String getExpression(String indepVarName) {
    StringBuffer eqn = new StringBuffer();
    int end = coefficients.length-1;
    for(int i = 0; i<=end; i++) {
      eqn.append(getParameterName(i));
      if(end-i>0) {
        eqn.append("*");   //$NON-NLS-1$
        eqn.append(indepVarName);
        if(end-i>1) {
          eqn.append("^"); //$NON-NLS-1$
          eqn.append(end-i);
        }
        eqn.append(" + "); //$NON-NLS-1$
      }
    }
    return eqn.toString();
  }

  /**
   * Gets the name of the function.
   *
   * @return the name
   */
  public String getName() {
  	if (name!=null) return name;
    return "Poly"+(getParameterCount()-1); //$NON-NLS-1$
  }
  
  /**
   * Sets the name of the function.
   *
   * @param aName the name
   */
  public void setName(String aName) {
  	if (aName!=null && !"".equals(aName.trim())) { //$NON-NLS-1$
  		name = aName;
  	}
  }

  /**
   * Gets the description of the function.
   *
   * @return the description
   */
  public String getDescription() {
  	if (description!=null && !"".equals(description.trim())) return description; //$NON-NLS-1$
  	return ToolsRes.getString("KnownPolynomial.Description")+" "+(getParameterCount()-1); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Sets the description of the function.
   *
   * @param aDescription the description
   */
  public void setDescription(String aDescription) {
  	description = aDescription;
  }

  /**
   * Gets a clone of this function.
   *
   * @return the clone
   */
  public KnownPolynomial clone() {
  	KnownPolynomial clone = new KnownPolynomial(coefficients);
  	
  	// set name and description
  	clone.setName(getName());
  	clone.setDescription(getDescription());

  	// set parameters
  	String[] names = new String[coefficients.length];
  	double[] values = new double[coefficients.length];
  	String[] desc = new String[coefficients.length];
  	for (int i=0; i< coefficients.length; i++) {
    	names[i] = getParameterName(i);
    	values[i] = getParameterValue(i);
    	desc[i] = getParameterDescription(i);
  	}
  	clone.setParameters(names, values, desc);
  	
  	return clone;
  }
  
  /**
   * Determines if another KnownFunction is the same as this one.
   *
   * @param f the KnownFunction to test
   * @return true if equal
   */
  @Override
  public boolean equals(Object f) {
  	if (!(f instanceof KnownPolynomial)) return false;
  	KnownPolynomial poly = (KnownPolynomial)f;
  	int n = getParameterCount();
  	if (n!=poly.getParameterCount()) return false;
  	for (int i=0; i<n; i++) {
  		if (!getParameterName(i).equals(poly.getParameterName(i))) return false;
  	}
  	// ignore descriptions and parameter values
  	return true;
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
