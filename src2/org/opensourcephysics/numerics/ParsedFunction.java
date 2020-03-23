/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * ParsedFunction defines a function of a single varianble using a String.
 *
 * This function is immutable.  That is, once an instance is created with a particular
 * function string, the function cannot be changed.  Because immutable
 * objects cannot change, they are thread safe and can be freely shared in a Java
 * program.
 *
 * @author Wolfgang Christian
 */
public final class ParsedFunction implements Function {
  private final String fStr;
  private final Function function;

  /**
   * Constructs a function x with from the given string.
   *
   * @param fStr the function
   * @throws ParserException
   */
  public ParsedFunction(String fStr) throws ParserException {
    this(fStr, "x"); //$NON-NLS-1$
  }

  /**
   * Constructs a ParsedFunction from the given string and independent variable.
   *
   * @param _fStr the function
   * @param var the independent variable
   * @throws ParserException
   */
  public ParsedFunction(String _fStr, String var) throws ParserException {
    fStr = _fStr;
    SuryonoParser parser = null;
    parser = new SuryonoParser(fStr, var);
    function = parser;
  }

  /**
   * Evaluates the function, f.
   *
   * @param x the value of the independent variable
   *
   * @return the value of the function
   */
  public double evaluate(double x) {
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
