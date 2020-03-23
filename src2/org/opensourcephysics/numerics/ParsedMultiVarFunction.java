/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * ParsedMultiVarFunction defines a function of multiple variables using a String.
 *
 * This function is immutable.  That is, once an instance is created with a particular
 * function string, the function cannot be changed.  Because immutable
 * objects cannot change, they are thread safe and can be freely shared in a Java
 * program.
 *
 * @author Wolfgang Christian
 */
public final class ParsedMultiVarFunction implements MultiVarFunction {
  private final String fStr;
  private final MultiVarFunction function;
  private String[] functionNames;

  /**
   * Constructs a ParsedFunction from the given string and independent variable.
   *
   * @param _fStr the function
   * @param var the independent variable
   * @throws ParserException
   */
  public ParsedMultiVarFunction(String _fStr, String[] var) throws ParserException {
    fStr = _fStr;
    SuryonoParser parser = null;
    parser = new SuryonoParser(fStr, var);
    function = parser;
    functionNames = parser.getFunctionNames();
  }

  /**
   * Evaluates the function, f.
   *
   * @param x the value of the independent variable
   *
   * @return the value of the function
   */
  public double evaluate(double[] x) {
    return function.evaluate(x);
  }

  /**
   * Represents the function as a string.
   *
   * @return the string
   */
  public String toString() {
    return "f(x) = "+fStr; //$NON-NLS-1$
  }

  /**
   * Returns function names.
   * Added by D. Brown 06 Jul 2008
   *
   * @return array of function names
   */
  public String[] getFunctionNames() {
    return functionNames;
  }

  /**
   * Determines if last evaluation resulted in NaN. Added by D Brown 15 Sep 2010.
   *
   * @return true if result was converted from NaN to zero
   */
  public boolean evaluatedToNaN() {
  	if (function instanceof SuryonoParser) {
  		return ((SuryonoParser)function).evaluatedToNaN();
  	}
  	return false;
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
