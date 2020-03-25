/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * MathExpParser defines an abstract super class for mathematical expression parsers.
 */
public abstract class MathExpParser implements Function, MultiVarFunction {
  /** No error. */
  public static final int NO_ERROR = 0;

  /**  Syntax error. */
  public static final int SYNTAX_ERROR = 1;

  /**
   * Parses the function string using existing variable names.
   *
   * @param funcStr the function to be parsed
   */
  public abstract void setFunction(String funcStr) throws ParserException;

  /**
   * Parses the function string using existing variable names.
   *
   * @param funcStr the function to be parsed
   * @param vars the function's variables
   */
  public abstract void setFunction(String funcStr, String[] vars) throws ParserException;

  /**
   * Gets the function string.
   */
  public abstract String getFunction();

  public static MathExpParser createParser() {
    return new SuryonoParser(0);
  }

  /**
   * Returns names of functions recognized by the parser.
   *
   * @return array of function names
   */
  public abstract String[] getFunctionNames();

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
