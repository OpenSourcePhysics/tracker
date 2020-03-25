/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.value;
import org.opensourcephysics.ejs.control.GroupControl;

/**
 * A <code>ExpressionValue</code> is a <code>Value</code> object that
 * holds an expression is parsed into a double.
 * <p>
 * @see     Value
 */
public class ExpressionValue extends Value {
  private String expression;
  private GroupControl group;
  private ParserSuryono parser;
  private String vars[];
  // Now consider the case when it is an array

  private boolean isArray;
  private ParserSuryono[] arrayParser;
  private String[][] arrayVars;
  private double[] arrayValues;

  /**
   * Constructor ExpressionValue
   * @param _expression
   * @param _group
   */
  public ExpressionValue(String _expression, GroupControl _group) {
    group = _group;
    expression = new String(_expression.trim());
    processExpression();
  }

  public boolean getBoolean() {
    return(getDouble()!=0);
  }

  public int getInteger() {
    return(int) getDouble();
  }

  public double getDouble() {
    for(int i = 0, n = vars.length; i<n; i++) {
      parser.setVariable(i, group.getDouble(vars[i]));
    }
    return parser.evaluate();
  }

  public String getString() {
    return String.valueOf(getDouble());
  }

  public Object getObject() {
    if(isArray) {
      for(int k = 0, m = arrayVars.length; k<m; k++) {
        for(int i = 0, n = arrayVars[k].length; i<n; i++) {
          arrayParser[k].setVariable(i, group.getDouble(arrayVars[k][i]));
        }
        arrayValues[k] = arrayParser[k].evaluate();
      }
      return arrayValues;
    }
    return null;
  }

  public void setExpression(String _expression) {
    expression = new String(_expression.trim());
    processExpression();
  }

  public void copyValue(Value _source) {
    if(_source instanceof ExpressionValue) {
      expression = new String(((ExpressionValue) _source).expression);
    } else {
      expression = new String(_source.getString());
    }
    processExpression();
  }

  public Value cloneValue() {
    return new ExpressionValue(expression, group);
  }

  private void processExpression() {
    if(expression.startsWith("{")&&expression.endsWith("}")) { // An explicit array of constants //$NON-NLS-1$ //$NON-NLS-2$
      String text = expression.substring(1, expression.length()-1);
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(text, ","); //$NON-NLS-1$
      int dim = tkn.countTokens();
      arrayParser = new ParserSuryono[dim];
      arrayVars = new String[dim][];
      arrayValues = new double[dim];
      isArray = true;
      // Prepare the parsers
      int k = 0;
      while(tkn.hasMoreTokens()) {
        String token = tkn.nextToken();
        arrayVars[k] = ParserSuryono.getVariableList(token);
        arrayParser[k] = new ParserSuryono(arrayVars[k].length);
        for(int i = 0, n = arrayVars[k].length; i<n; i++) {
          arrayParser[k].defineVariable(i, arrayVars[k][i]);
        }
        arrayParser[k].define(token);
        arrayParser[k].parse();
        k++;
      }
    } else {                                                                    // A single variable or expression. Taken to be double
      vars = ParserSuryono.getVariableList(expression);
      parser = new ParserSuryono(vars.length);
      for(int i = 0, n = vars.length; i<n; i++) {
        parser.defineVariable(i, vars[i]);
      }
      parser.define(expression);
      parser.parse();
      isArray = false;
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
