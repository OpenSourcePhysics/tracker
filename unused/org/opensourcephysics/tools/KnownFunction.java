/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import org.opensourcephysics.numerics.Function;

/**
 * Title:        KnownFunction
 * Description:  A function that provides its name, expression and parameters to users.
 */
public interface KnownFunction extends Function {
  /**
   * Gets the parameter count.
   * @return the number of parameters
   */
  public int getParameterCount();

  /**
   * Gets a parameter name.
   *
   * @param i the parameter index
   * @return the name of the parameter
   */
  public String getParameterName(int i);

  /**
   * Gets a parameter value.
   *
   * @param i the parameter index
   * @return the value of the parameter
   */
  public double getParameterValue(int i);

  /**
   * Gets a parameter description. May be null.
   *
   * @param i the parameter index
   * @return the description of the parameter (may be null)
   */
  public String getParameterDescription(int i);

  /**
   * Sets a parameter value.
   *
   * @param i the parameter index
   * @param value the value
   */
  public void setParameterValue(int i, double value);

  /**
   * Sets the parameters.
   *
   * @param names the parameter names
   * @param values the parameter values
   * @param descriptions the parameter descriptions
   */
  public void setParameters(String[] names, double[] values, String[] descriptions);

  /**
   * Gets the equation.
   *
   * @param indepVarName the name of the independent variable
   * @return the equation
   */
  public String getExpression(String indepVarName);

  /**
   * Gets the name of the function.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name of the function.
   *
   * @param name the name
   */
  public void setName(String name);

  /**
   * Gets the description of the function.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description of the function.
   *
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Gets a clone of the function.
   *
   * @return the clone
   */
  public KnownFunction clone();

  /**
   * Determines if another Object is the same as this one.
   *
   * @param f the Object to test
   * @return true if equal
   */
  public boolean equals(Object f);

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
