/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
/*----------------------------------------------------------------------------------------*
 * Parser.java version 1.0                                                    Jun 16 1996 *
 * Parser.java version 2.0                                                    Aug 25 1996 *
 * Parser.java version 2.1                                                    Oct 14 1996 *
 * Parser.java version 2.11                                                   Oct 25 1996 *
 * Parser.java version 2.2                                                    Nov  8 1996 *
 * Parser.java version 3.0                                                    May 17 1997 *
 * Parser.java version 3.01                                                   Oct 18 2001 *
 *                                                                                        *
 * Parser.java version 4.0                                                    Oct 25 2001 *
 *                                                                                        *
 * Copyright (c) 1996 Yanto Suryono. All Rights Reserved.                                 *
 * Version 4 Modifications by Wolfgang Christian                                          *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *                                                                                        *
 *----------------------------------------------------------------------------------------*/
import java.util.Hashtable;
import java.util.Vector;

/**
 * The class <code>Parser</code> is a mathematical expression parser.<p>
 * Example of code that uses this class:<p>
 *
 * <pre>
 * Parser parser = new Parser(1);    // creates parser with one variable
 * parser.defineVariable(1,"x");     // lets the variable be 'x'
 * parser.define("sin(x)/x");        // defines function: sin(x)/x
 * parser.parse();                   // parses the function
 *
 * // calculates: sin(x)/x with x = -5.0 .. +5.0 in 20 steps
 * // and prints the result to standard output.
 *
 * float result;
 * for (i=-10; i <= 10; i++) {
 *   parser.setVariable(1,(float)i/2.0f);
 *   result = parser.evaluate();
 *   System.out.println(result);
 * }
 * </pre>
 */
public final class SuryonoParser extends MathExpParser {
  // global variables
  private int var_count;                           // number of variables
  private String var_name[];                       // variables' name
  private double var_value[];                      // value of variables
  private double number[];                         // numeric constants in defined function
  private String function = "";                    // function definition //$NON-NLS-1$
  private String postfix_code = "";                // the postfix code //$NON-NLS-1$
  private boolean valid = false;                   // postfix code status
  private int error;                               // error code of last process
  private boolean ISBOOLEAN = false;               // boolean flag
  private boolean INRELATION = false;              // relation flag
  // variables used during parsing
  private int position;                            // parsing pointer
  private int start;                               // starting position of identifier
  private int num;                                 // number of numeric constants
  private char character;                          // current character
  // variables used during evaluating
  private boolean radian;                          // radian unit flag
  private int numberindex;                         // pointer to numbers/constants bank
  private double[] refvalue = null;                // value of references
  // private  static final int MAX_NUM     = 100;  // max numeric constants  // changed by W. Christian
  private static final int MAX_NUM = 200;          // max numeric constants
  // private  static final int NO_FUNCS      = 24;   // no. of built-in functions
  // changed from 24 function by W. Christian to add step and random function
  private static final int NO_FUNCS = 26;          // no. of built-in functions
  private static final int NO_EXT_FUNCS = 4;       // no. of extended functions
  private static final int STACK_SIZE = 50;        // evaluation stack size
  private double[] stack = new double[STACK_SIZE]; // moved by W. Christian from evaluate to global variables for speed
  // constants
  private static final double DEGTORAD = Math.PI/180;
  private static final double LOG10 = Math.log(10);
  // references - version 3.0
  private Hashtable<String, String> references = null;
  private Vector<String> refnames = null;
  // error codes

  /**
   * No error.
   *
   * Moved to superclass by W. Christian
   */
  // public   static final int NO_ERROR             =  0;

  /**
   * Syntax error.
   *
   * Moved to superclass by W. Christian
   */
  // public   static final int SYNTAX_ERROR         =  1;

  /**
   * Parentheses expected.
   */
  public static final int PAREN_EXPECTED = 2;

  /**
   * Attempt to evaluate an uncompiled function.
   */
  public static final int UNCOMPILED_FUNCTION = 3;

  /**
   * Expression expected.
   */
  public static final int EXPRESSION_EXPECTED = 4;

  /**
   * Unknown identifier.
   */
  public static final int UNKNOWN_IDENTIFIER = 5;

  /**
   * Operator expected.
   */
  public static final int OPERATOR_EXPECTED = 6;

  /**
   * Parenthesis mismatch.
   */
  public static final int PAREN_NOT_MATCH = 7;

  /**
   * Code damaged.
   */
  public static final int CODE_DAMAGED = 8;

  /**
   * Stack overflow.
   */
  public static final int STACK_OVERFLOW = 9;

  /**
   * Too many constants.
   */
  public static final int TOO_MANY_CONSTS = 10;

  /**
   * Comma expected.
   */
  public static final int COMMA_EXPECTED = 11;

  /**
   * Invalid operand.
   */
  public static final int INVALID_OPERAND = 12;

  /**
   * Invalid operator.
   */
  public static final int INVALID_OPERATOR = 13;

  /**
   * No function definition to parse.
   */
  public static final int NO_FUNC_DEFINITION = 14;

  /**
   * Referenced name could not be found.
   */
  public static final int REF_NAME_EXPECTED = 15;
  // postfix codes
  private static final int FUNC_OFFSET = 1000;
  private static final int EXT_FUNC_OFFSET = FUNC_OFFSET+NO_FUNCS;
  private static final int VAR_OFFSET = 2000;
  private static final int REF_OFFSET = 3000;
  private static final char PI_CODE = (char) 253;
  private static final char E_CODE = (char) 254;
  private static final char NUMERIC = (char) 255;
  // Jump, followed by n : Displacement
  private static final char JUMP_CODE = (char) 1;
  // Relation less than (<)
  private static final char LESS_THAN = (char) 2;
  // Relation greater than (>)
  private static final char GREATER_THAN = (char) 3;
  // Relation less than or equal (<=)
  private static final char LESS_EQUAL = (char) 4;
  // Relation greater than or equal (>=)
  private static final char GREATER_EQUAL = (char) 5;
  // Relation not equal (<>)
  private static final char NOT_EQUAL = (char) 6;
  // Relation equal (=)
  private static final char EQUAL = (char) 7;
  // Conditional statement IF, followed by a conditional block :
  // * Displacement (Used to jump to condition FALSE code)
  // * Condition TRUE code
  // * Jump to next code outside conditional block
  // * Condition FALSE code
  // * ENDIF
  private static final char IF_CODE = (char) 8;
  private static final char ENDIF = (char) 9;
  private static final char AND_CODE = (char) 10;  // Boolean AND
  private static final char OR_CODE = (char) 11;   // Boolean OR
  private static final char NOT_CODE = (char) 12;  // Boolean NOT
  // built in functions
  private String funcname[] = {
		  "sin", "cos", "tan", "ln", "log", "abs", "int", "frac", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh", "ceil", "floor", "round", "exp", "sqr", "sqrt", "sign", "step","random"                                       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$ //$NON-NLS-22$ //$NON-NLS-23$ //$NON-NLS-24$ //$NON-NLS-25$ //$NON-NLS-26$
  };
  // extended functions
  private String extfunc[] = {"min", "max", "mod", "atan2"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  // set when evaluate() method converts NaN to zero--added by D Brown 15 Sep 2010
  private boolean isNaN;

  /**
   * The constructor of <code>Parser</code>.
   *
   * Added by W. Christian to make it easy to construct a parser for with one variable.
   *
   * @param f function
   * @param v variable
   *
   * @throws ParserException
   */
  public SuryonoParser(String f, String v) throws ParserException {
    this(1);
    defineVariable(1, v); // lets the variable be v
    define(f);            // defines function: f
    parse();              // parses the function
    if(getErrorCode()!=NO_ERROR) {
      String msg = "Error in function string: "+f;    //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+getErrorString();      //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition(); //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /**
   * The constructor of <code>Parser</code>.
   *
   * Added by W. Christian to make it easy to construct a parser for with two variables.
   *
   * @param f the function
   * @param v1 variable 1
   * @param v2 variable 2
   * @throws ParserException
   */
  public SuryonoParser(String f, String v1, String v2) throws ParserException {
    this(2);
    defineVariable(1, v1);
    defineVariable(2, v2);
    define(f); // defines function: f
    parse();   // parses the function
    if(getErrorCode()!=NO_ERROR) {
      String msg = "Error in function string: "+f;    //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+getErrorString();      //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition(); //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /**
   * The constructor of <code>Parser</code>.
   *
   * Added by W. Christian to make it easy to construct a parser for with multiple variables.
   *
   * @param f the function
   * @param v variables
   * @throws ParserException
   */
  public SuryonoParser(String f, String[] v) throws ParserException {
    this(v.length);
    for(int i = 0; i<v.length; i++) {
      defineVariable(i+1, v[i]);
    }
    define(f); // defines function: f
    parse();   // parses the function
    if(getErrorCode()!=NO_ERROR) {
      String msg = "Error in function string: "+f;    //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+getErrorString();      //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition(); //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /**
   * The constructor of <code>Parser</code>.
   *
   * @param variablecount the number of variables
   */
  public SuryonoParser(int variablecount) {
    var_count = variablecount;
    references = new Hashtable<String, String>();
    refnames = new Vector<String>();
    radian = true;
    // arrays are much faster than vectors (IMHO)
    var_name = new String[variablecount];
    var_value = new double[variablecount];
    number = new double[MAX_NUM];
  }

  /**
   * Sets the funtion to zero.
   */
  public void setToZero() {
    try {
      setFunction("0"); //$NON-NLS-1$
    } catch(ParserException ex) {}
  }

  boolean appendVariables = false;

  /**
   * Sets the angle unit to radian. Default upon construction.
   */
  public void useRadian() {
    radian = true;
  }

  /**
   * Sets the angle unit to degree.
   */
  public void useDegree() {
    radian = false;
  }

  /**
   * Remove any escape sequences such as \, and replace with the character.
   *  by W. Christian.
   *
   * @param function the function
   *
   * @return the new function
   */
  private String removeEscapeCharacter(String str) {
    if((str==null)||(str.length()<1)) {
      return str;
    }
    StringBuffer sb = new StringBuffer(str.length());
    for(int i = 0; i<str.length(); i++) {
      if(str.charAt(i)!='\\') {
        sb.append(str.charAt(i));
      }
    }
    return sb.toString();
  }

  /**
   * Sets the variable names.
   * Nothing happens if variable index > number of variables.
   *
   * @param index the variable index (one based)
   * @param name  the variable name
   */
  public void defineVariable(int index, String name) {
    if(index>var_count) {
      return;
    }
    var_name[index-1] = name;
  }

  /**
   * Sets the variable value.
   * The variable is accessed by index.
   * Nothing happens if variable index > number of variables.
   *
   * @param index the variable index (one based)
   * @param value the variable value
   */
  public void setVariable(int index, double value) {
    if(index>var_count) {
      return;
    }
    var_value[index-1] = value;
  }

  /**
   * Sets the variable value.
   * The variable is accessed by name.
   * Nothing happens if variable could not be found.
   *
   * @param name  the variable name
   * @param value the variable value
   */
  public void setVariable(String name, double value) {
    for(int i = 0; i<var_count; i++) {
      if(var_name[i].equals(name)) {
        var_value[i] = value;
        break;
      }
    }
  }

  /**
   * Defines a function. Current postfix code becomes invalid.
   *
   * @param definition the function definition
   */
  public void define(String definition) {
    function = definition;
    function.toLowerCase();
    function = removeEscapeCharacter(function); // added by W. Christian
    valid = false;
  }

  /**
   * Parses defined function.
   */
  public void parse(String function) throws ParserException {
    define(function);
    parse();
    if(getErrorCode()!=NO_ERROR) {
      String msg = "Error in function string: "+function; //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+getErrorString();          //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition();     //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /**
   * Parses a function looking for unknown variables.  Unknown tokens are used to create the variable list in the order
   * that they are found.
   */
  public String[] parseUnknown(String function) throws ParserException {
    var_name = new String[0];
    var_value = new double[0];
    var_count = 0;
    appendVariables = true;
    define(function);
    parse();
    if(getErrorCode()!=NO_ERROR) {
      String msg = "Error in function string: "+function; //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+getErrorString();          //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition();     //$NON-NLS-1$
      appendVariables = false;
      throw new ParserException(msg);
    }
    appendVariables = false;
    return var_name;
  }

  public String[] getVariableNames() {
    return var_name;
  }

  /**
   * Returns all built-in and extended function names.
   * Added by D. Brown 06 Jul 2008
   *
   * @return array of function names
   */
  public String[] getFunctionNames() {
    int len = funcname.length;
    String[] names = new String[len+extfunc.length];
    System.arraycopy(funcname, 0, names, 0, len);
    System.arraycopy(extfunc, 0, names, len, extfunc.length);
    return names;
  }

  /**
   * Parses defined function.
   */
  public void parse() {
    String allFunction = new String(function);
    String orgFunction = new String(function);
    int index;
    if(valid) {
      return;
    }
    num = 0;
    error = NO_ERROR;
    references.clear();
    refnames.removeAllElements();
    while((index = allFunction.lastIndexOf(";"))!=-1) { //$NON-NLS-1$
      function = allFunction.substring(index+1)+')';
      allFunction = allFunction.substring(0, index++);
      // references are of form:   refname1:reffunc1;refname2:reffunc2;...
      String refname = null;
      int separator = function.indexOf(":");            //$NON-NLS-1$
      if(separator==-1) {
        error = NO_FUNC_DEFINITION;
        for(position = 0; position<function.length(); position++) {
          if(function.charAt(position)!=' ') {
            break;
          }
        }
        position++;
      } else {
        refname = function.substring(0, separator);
        function = function.substring(separator+1);
        refname = refname.trim();
        if(refname.equals("")) {                        //$NON-NLS-1$
          error = REF_NAME_EXPECTED;
          position = 1;
        } else {
          index += ++separator;
          parseSubFunction();
        }
      }
      if(error!=NO_ERROR) {
        position += index;
        break;
      }
      references.put(refname, postfix_code);
      refnames.addElement(refname);
    }
    if(error==NO_ERROR) {
      function = allFunction+')';
      parseSubFunction();
    }
    function = orgFunction;
    valid = (error==NO_ERROR);
  }

  public double evaluate(double x, double y)
  // added by Wolfgang Christian to make it easier to call parser.
  {
    if(var_count!=2) {
      return 0;
    }
    var_value[0] = x;
    var_value[1] = y;
    return evaluate();
  }

  public double evaluate(double x, double y, double z)
  // added by Wolfgang Christian to make it easier to call parser.
  {
    if(var_count!=3) {
      return 0;
    }
    var_value[0] = x;
    var_value[1] = y;
    var_value[2] = z;
    return evaluate();
  }

  public double evaluate(double x)
  // added by Wolfgang Christian to make it easier to call parser.
  {
    if(var_count!=1) {
      return 0;
    }
    var_value[0] = x;
    return evaluate();
  }

  public double evaluate(double[] v)
  // added by Wolfgang Christian to make it easier to call parser with an array.
  {
    if(var_value.length!=v.length) {
      System.out.println("JEParser Error: incorrect number of variables."); //$NON-NLS-1$
      return 0;
    }
    System.arraycopy(v, 0, var_value, 0, v.length);
    return evaluate();
  }

  /**
   * Evaluates compiled function.
   *
   * @return the result of the function
   */
  public double evaluate() {
    int size = refnames.size();
    double result;
    if(!valid) {
      error = UNCOMPILED_FUNCTION;
      return 0;
    }
    error = NO_ERROR;
    numberindex = 0;
    if(size!=0) {
      String orgPFC = postfix_code;
      refvalue = new double[size];
      for(int i = 0; i<refnames.size(); i++) {
        String name = refnames.elementAt(i);
        postfix_code = references.get(name);
        result = evaluateSubFunction();
        if(error!=NO_ERROR) {
          postfix_code = orgPFC;
          refvalue = null;
          return result;
        }
        refvalue[i] = result;
      }
      postfix_code = orgPFC;
    }
    result = evaluateSubFunction();
    refvalue = null;
    // added by D Brown to flag NaN results
    isNaN = Double.isNaN(result);
    // added by W. Christian to trap for NaN
    if(isNaN) {
      result = 0.0;
    }
    return result;
  }

  /**
   * Determines if last evaluation resulted in NaN. Added by D Brown 15 Sep 2010.
   *
   * @return true if result was converted from NaN to zero
   */
  public boolean evaluatedToNaN() {
    return isNaN;
  }

  /**
   * Gets error code of last operation.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return error;
  }

  /**
   * Gets error string/message of last operation.
   *
   * @return the error string
   */
  public String getErrorString() {
    return toErrorString(error);
  }

  /**
   * Gets error position. Valid only if error code != NO_ERROR
   *
   * @return error position (one based)
   */
  public int getErrorPosition() {
    return position;
  }

  /**
   * Converts error code to error string.
   *
   * @return the error string
   */
  public static String toErrorString(int errorcode) {
    String s = ""; //$NON-NLS-1$
    switch(errorcode) {
       case NO_ERROR :
         s = "no error";                              //$NON-NLS-1$
         break;
       case SYNTAX_ERROR :
         s = "syntax error";                          //$NON-NLS-1$
         break;
       case PAREN_EXPECTED :
         s = "parenthesis expected";                  //$NON-NLS-1$
         break;
       case UNCOMPILED_FUNCTION :
         s = "uncompiled function";                   //$NON-NLS-1$
         break;
       case EXPRESSION_EXPECTED :
         s = "expression expected";                   //$NON-NLS-1$
         break;
       case UNKNOWN_IDENTIFIER :
         s = "unknown identifier";                    //$NON-NLS-1$
         break;
       case OPERATOR_EXPECTED :
         s = "operator expected";                     //$NON-NLS-1$
         break;
       case PAREN_NOT_MATCH :
         s = "parentheses not match";                 //$NON-NLS-1$
         break;
       case CODE_DAMAGED :
         s = "internal code damaged";                 //$NON-NLS-1$
         break;
       case STACK_OVERFLOW :
         s = "execution stack overflow";              //$NON-NLS-1$
         break;
       case TOO_MANY_CONSTS :
         s = "too many constants";                    //$NON-NLS-1$
         break;
       case COMMA_EXPECTED :
         s = "comma expected";                        //$NON-NLS-1$
         break;
       case INVALID_OPERAND :
         s = "invalid operand type";                  //$NON-NLS-1$
         break;
       case INVALID_OPERATOR :
         s = "invalid operator";                      //$NON-NLS-1$
         break;
       case NO_FUNC_DEFINITION :
         s = "bad reference definition (: expected)"; //$NON-NLS-1$
         break;
       case REF_NAME_EXPECTED :
         s = "reference name expected";               //$NON-NLS-1$
         break;
    }
    return s;
  }

  /**
   * Gets function string of last operation.
   *
   * Added by W. Christian to implement the MathExpParser interface.
   *
   * @return the function string
   */
  public String getFunction() {
    return function;
  }

  /**
   * Parse the function string using the existing variables.
   *
   * Added by W. Christian to implement the MathExpParser interface.
   */
  public void setFunction(String funcStr) throws ParserException {
    function = funcStr;
    define(function);
    parse();
    if(error!=NO_ERROR) {
      String msg = "Error in function string: "+funcStr; //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+toErrorString(error);     //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition();    //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /**
   * Parse the function string using new variable names.
   *
   * Added by W. Christian to implement the MathExpParser interface.
   */
  public void setFunction(String funcStr, String[] vars) throws ParserException {
    function = funcStr;
    if(vars.length!=var_count) {
      var_count = vars.length;
      references.clear();
      refnames.clear();
      var_name = new String[var_count];
      var_value = new double[var_count];
    }
    for(int i = 0; i<vars.length; i++) {
      defineVariable(i+1, vars[i]);
    }
    define(function);
    parse();
    if(error!=NO_ERROR) {
      String msg = "Error in function string: "+funcStr; //$NON-NLS-1$
      msg = msg+'\n'+"Error: "+toErrorString(error);     //$NON-NLS-1$
      msg = msg+'\n'+"Position: "+getErrorPosition();    //$NON-NLS-1$
      throw new ParserException(msg);
    }
  }

  /*----------------------------------------------------------------------------------------*
   *                            Private methods begin here                                  *
   *----------------------------------------------------------------------------------------*/

  /**
   * Advances parsing pointer, skips pass all white spaces.
   *
   * @exception ParserException
   */
  private void skipSpaces() throws ParserException {
    try {
      while(function.charAt(position-1)==' ') {
        position++;
      }
      character = function.charAt(position-1);
    } catch(StringIndexOutOfBoundsException e) {
      throw new ParserException(PAREN_NOT_MATCH);
    }
  }

  /**
   * Advances parsing pointer, gets next character.
   *
   * @exception ParserException
   */
  private void getNextCharacter() throws ParserException {
    position++;
    try {
      character = function.charAt(position-1);
    } catch(StringIndexOutOfBoundsException e) {
      throw new ParserException(PAREN_NOT_MATCH);
    }
  }

  /**
   * Appends postfix code to compiled code.
   *
   * @param code the postfix code to append
   */
  private void addCode(char code) {
    postfix_code += code;
  }

  /**
   * Scans a number. Valid format: xxx[.xxx[e[+|-]xxx]]
   *
   * @exception ParserException
   */
  private void scanNumber() throws ParserException {
    // changed by W. Christian to parse numbers with leading zeros.
    String numstr = ""; //$NON-NLS-1$
    double value;
    if(num==MAX_NUM) {
      throw new ParserException(TOO_MANY_CONSTS);
    }
    if(character!='.') { // added by W. Christian
      do {
        numstr += character;
        getNextCharacter();
      } while((character>='0')&&(character<='9'));
    } else {
      numstr += '0';
    }                    // added by W. Christian
    if(character=='.') {
      do {
        numstr += character;
        getNextCharacter();
      } while((character>='0')&&(character<='9'));
    }
    //    if(character=='e') {
    if((character=='e')||(character=='E')) { // changed by Doug Brown May 2007
      numstr += character;
      getNextCharacter();
      if((character=='+')||(character=='-')) {
        numstr += character;
        getNextCharacter();
      }
      while((character>='0')&&(character<='9')) {
        numstr += character;
        getNextCharacter();
      }
    }
    try {
      value = Double.valueOf(numstr).doubleValue();
    } catch(NumberFormatException e) {
      position = start;
      throw new ParserException(SYNTAX_ERROR);
    }
    number[num++] = value;
    addCode(NUMERIC);
  }

  /**
   * Scans a non-numerical identifier. Can be function call,
   * variable, reference, etc.
   *
   * @exception ParserException
   */
  private void scanNonNumeric() throws ParserException {
    String stream = ""; //$NON-NLS-1$
    if((character=='*')||(character=='/')||(character=='^')||(character==')')||(character==',')||(character=='<')||(character=='>')||(character=='=')||(character=='&')||(character=='|')) {
      throw new ParserException(SYNTAX_ERROR);
    }
    do {
      stream += character;
      getNextCharacter();
    } while(!((character==' ')||(character=='+')||(character=='-')||(character=='*')||(character=='/')||(character=='^')||(character=='(')||(character==')')||(character==',')||(character=='<')||(character=='>')||(character=='=')||(character=='&')||(character=='|')));
    if(stream.equals("pi")) {       //$NON-NLS-1$
      addCode(PI_CODE);
      return;
    } else if(stream.equals("e")) { //$NON-NLS-1$
      addCode(E_CODE);
      return;
    }
    // if
    if(stream.equals("if")) { //$NON-NLS-1$
      skipSpaces();
      if(character!='(') {
        throw new ParserException(PAREN_EXPECTED);
      }
      scanAndParse();
      if(character!=',') {
        throw new ParserException(COMMA_EXPECTED);
      }
      addCode(IF_CODE);
      String savecode = new String(postfix_code);
      postfix_code = "";      //$NON-NLS-1$
      scanAndParse();
      if(character!=',') {
        throw new ParserException(COMMA_EXPECTED);
      }
      addCode(JUMP_CODE);
      savecode += (char) (postfix_code.length()+2);
      savecode += postfix_code;
      postfix_code = "";      //$NON-NLS-1$
      scanAndParse();
      if(character!=')') {
        throw new ParserException(PAREN_EXPECTED);
      }
      savecode += (char) (postfix_code.length()+1);
      savecode += postfix_code;
      postfix_code = new String(savecode);
      getNextCharacter();
      return;
    }
    // built-in function
    for(int i = 0; i<NO_FUNCS; i++) {
      if(stream.equals(funcname[i])) {
        skipSpaces();
        if(character!='(') {
          throw new ParserException(PAREN_EXPECTED);
        }
        scanAndParse();
        if(character!=')') {
          throw new ParserException(PAREN_EXPECTED);
        }
        getNextCharacter();
        addCode((char) (i+FUNC_OFFSET));
        return;
      }
    }
    // extended functions
    for(int i = 0; i<NO_EXT_FUNCS; i++) {
      if(stream.equals(extfunc[i])) {
        skipSpaces();
        if(character!='(') {
          throw new ParserException(PAREN_EXPECTED);
        }
        scanAndParse();
        if(character!=',') {
          throw new ParserException(COMMA_EXPECTED);
        }
        String savecode = new String(postfix_code);
        postfix_code = ""; //$NON-NLS-1$
        scanAndParse();
        if(character!=')') {
          throw new ParserException(PAREN_EXPECTED);
        }
        getNextCharacter();
        savecode += postfix_code;
        postfix_code = new String(savecode);
        addCode((char) (i+EXT_FUNC_OFFSET));
        return;
      }
    }
    // registered variables
    for(int i = 0; i<var_count; i++) {
      if(stream.equals(var_name[i])) {
        addCode((char) (i+VAR_OFFSET));
        return;
      }
    }
    // references
    int index = refnames.indexOf(stream);
    if(index!=-1) {
      addCode((char) (index+REF_OFFSET));
      return;
    }
    // appendVariables option added by W. Christian
    if(appendVariables&&append(stream)) {
      return;
    }
    position = start;
    throw new ParserException(UNKNOWN_IDENTIFIER);
  }

  // W. Christian addition to automatically add variables
  private boolean append(String stream) {
    String[] var_name2 = new String[var_count+1];
    double[] var_value2 = new double[var_count+1];
    System.arraycopy(var_name, 0, var_name2, 0, var_count);
    System.arraycopy(var_value, 0, var_value2, 0, var_count);
    var_name2[var_count] = stream;
    var_name = var_name2;
    var_value = var_value2;
    var_count++;
    // System.out.println("appended=" + stream);
    for(int i = 0; i<var_count; i++) {
      if(stream.equals(var_name[i])) {
        addCode((char) (i+VAR_OFFSET));
        return true;
      }
    }
    return false;
  }

  /**
   * Gets an identifier starting from current parsing pointer.
   *
   * @return whether the identifier should be negated
   * @exception ParserException
   */
  private boolean getIdentifier() throws ParserException {
    boolean negate = false;
    getNextCharacter();
    skipSpaces();
    if(character=='!') {
      getNextCharacter();
      skipSpaces();
      if(character!='(') {
        throw new ParserException(PAREN_EXPECTED);
      }
      scanAndParse();
      if(character!=')') {
        throw new ParserException(PAREN_EXPECTED);
      }
      if(!ISBOOLEAN) {
        throw new ParserException(INVALID_OPERAND);
      }
      addCode(NOT_CODE);
      getNextCharacter();
      return false;
    }
    ISBOOLEAN = false;
    while((character=='+')||(character=='-')) {
      if(character=='-') {
        negate = !negate;
      }
      getNextCharacter();
      skipSpaces();
    }
    start = position;
    // if ((character >= '0') && (character <= '9'))    changed be W. Christian to handle leanding zeros.
    if(((character>='0')&&(character<='9'))||(character=='.')) {
      scanNumber();
    } else if(character=='(') {
      scanAndParse();
      getNextCharacter();
    } else {
      scanNonNumeric();
    }
    skipSpaces();
    return(negate);
  }

  /**
   * Scans arithmetic level 3 (highest). Power arithmetics.
   *
   * @exception ParserException
   */
  private void arithmeticLevel3() throws ParserException {
    boolean negate;
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    negate = getIdentifier();
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    if(character=='^') {
      arithmeticLevel3();
    }
    addCode('^');
    if(negate) {
      addCode('_');
    }
  }

  /**
   * Scans arithmetic level 2. Multiplications and divisions.
   *
   * @exception ParserException
   */
  private void arithmeticLevel2() throws ParserException {
    boolean negate;
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    do {
      char operator = character;
      negate = getIdentifier();
      if(ISBOOLEAN) {
        throw new ParserException(INVALID_OPERAND);
      }
      if(character=='^') {
        arithmeticLevel3();
      }
      if(negate) {
        addCode('_');
      }
      addCode(operator);
    } while((character=='*')||(character=='/'));
  }

  /**
   * Scans arithmetic level 1 (lowest).
   * Additions and substractions.
   *
   * @exception ParserException
   */
  private void arithmeticLevel1() throws ParserException {
    boolean negate;
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    do {
      char operator = character;
      negate = getIdentifier();
      if(ISBOOLEAN) {
        throw new ParserException(INVALID_OPERAND);
      }
      if(character=='^') {
        arithmeticLevel3();
        if(negate) {
          addCode('_');
        }
      } else if((character=='*')||(character=='/')) {
        if(negate) {
          addCode('_');
        }
        arithmeticLevel2();
      }
      addCode(operator);
    } while((character=='+')||(character=='-'));
  }

  /**
   * Scans relation level.
   *
   * @exception ParserException
   */
  private void relationLevel() throws ParserException {
    char code = (char) 0;
    if(INRELATION) {
      throw new ParserException(INVALID_OPERATOR);
    }
    INRELATION = true;
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    switch(character) {
       case '=' :
         code = EQUAL;
         break;
       case '<' :
         code = LESS_THAN;
         getNextCharacter();
         if(character=='>') {
           code = NOT_EQUAL;
         } else if(character=='=') {
           code = LESS_EQUAL;
         } else {
           position--;
         }
         break;
       case '>' :
         code = GREATER_THAN;
         getNextCharacter();
         if(character=='=') {
           code = GREATER_EQUAL;
         } else {
           position--;
         }
         break;
    }
    scanAndParse();
    INRELATION = false;
    if(ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    addCode(code);
    ISBOOLEAN = true;
  }

  /**
   * Scans boolean level.
   *
   * @exception ParserException
   */
  private void booleanLevel() throws ParserException {
    if(!ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    char operator = character;
    scanAndParse();
    if(!ISBOOLEAN) {
      throw new ParserException(INVALID_OPERAND);
    }
    switch(operator) {
       case '&' :
         addCode(AND_CODE);
         break;
       case '|' :
         addCode(OR_CODE);
         break;
    }
  }

  /**
   * Main method of scanning and parsing process.
   *
   * @exception ParserException
   */
  private void scanAndParse() throws ParserException {
    boolean negate;
    negate = getIdentifier();
    if((character!='^')&&(negate)) {
      addCode('_');
    }
    do {
      switch(character) {
         case '+' :
         case '-' :
           arithmeticLevel1();
           break;
         case '*' :
         case '/' :
           arithmeticLevel2();
           break;
         case '^' :
           arithmeticLevel3();
           if(negate) {
             addCode('_');
           }
           break;
         case ',' :
         case ')' :
           return;
         case '=' :
         case '<' :
         case '>' :
           relationLevel();
           break;
         case '&' :
         case '|' :
           booleanLevel();
           break;
         default :
           throw new ParserException(OPERATOR_EXPECTED);
      }
    } while(true);
  }

  /**
   * Parses subfunction.
   */
  private void parseSubFunction() {
    position = 0;
    postfix_code = ""; //$NON-NLS-1$
    INRELATION = false;
    ISBOOLEAN = false;
    try {
      scanAndParse();
    } catch(ParserException e) {
      error = e.getErrorCode();
      if((error==SYNTAX_ERROR)&&(postfix_code=="")) { //$NON-NLS-1$
        error = EXPRESSION_EXPECTED;
      }
    }
    if((error==NO_ERROR)&&(position!=function.length())) {
      error = PAREN_NOT_MATCH;
    }
  }

  /**
   * Built-in one parameter function call.
   *
   * @return the function result
   * @param  function  the function index
   * @param  parameter the parameter to the function
   */
  private double builtInFunction(int function, double parameter) {
    switch(function) {
       case 0 :
         if(radian) {
           return Math.sin(parameter);
         }
         return Math.sin(parameter*DEGTORAD);
       case 1 :
         if(radian) {
           return Math.cos(parameter);
         }
         return Math.cos(parameter*DEGTORAD);
       case 2 :
         if(radian) {
           return Math.tan(parameter);
         }
         return Math.tan(parameter*DEGTORAD);
       case 3 :
         return Math.log(parameter);
       case 4 :
         return Math.log(parameter)/LOG10;
       case 5 :
         return Math.abs(parameter);
       case 6 :
         return Math.rint(parameter);
       case 7 :
         return parameter-Math.rint(parameter);
       case 8 :
         if(radian) {
           return Math.asin(parameter);
         }
         return Math.asin(parameter)/DEGTORAD;
       case 9 :
         if(radian) {
           return Math.acos(parameter);
         }
         return Math.acos(parameter)/DEGTORAD;
       case 10 :
         if(radian) {
           return Math.atan(parameter);
         }
         return Math.atan(parameter)/DEGTORAD;
       case 11 :
         return(Math.exp(parameter)-Math.exp(-parameter))/2;
       case 12 :
         return(Math.exp(parameter)+Math.exp(-parameter))/2;
       case 13 :
         double a = Math.exp(parameter);
         double b = Math.exp(-parameter);
         return(a-b)/(a+b);
       case 14 :
         return Math.log(parameter+Math.sqrt(parameter*parameter+1));
       case 15 :
         return Math.log(parameter+Math.sqrt(parameter*parameter-1));
       case 16 :
         return Math.log((1+parameter)/(1-parameter))/2;
       case 17 :
         return Math.ceil(parameter);
       case 18 :
         return Math.floor(parameter);
       case 19 :
         return Math.round(parameter);
       case 20 :
         return Math.exp(parameter);
       case 21 :
         return parameter*parameter;
       case 22 :
         return Math.sqrt(parameter);
       case 23 :
         if(parameter==0.0d) {
           return 0;
         } else if(parameter>0.0d) {
           return 1;
         } else {
           return -1;
         }
       case 24 :
         if(parameter<0) {
           return 0;
         }
         return 1;                       // added by W. Christian for step function
       case 25 :
         return parameter*Math.random(); // added by W. Christian for random function
       default :
         error = CODE_DAMAGED;
         return Double.NaN;
    }
  }

  /**
   * Built-in two parameters extended function call.
   *
   * @return the function result
   * @param  function  the function index
   * @param  param1    the first parameter to the function
   * @param  param2    the second parameter to the function
   */
  private double builtInExtFunction(int function, double param1, double param2) {
    switch(function) {
       case 0 :
         return Math.min(param1, param2);
       case 1 :
         return Math.max(param1, param2);
       case 2 :
         return Math.IEEEremainder(param1, param2);
       case 3 :
         return Math.atan2(param1, param2);
       default :
         error = CODE_DAMAGED;
         return Double.NaN;
    }
  }

  /**
   * Evaluates subfunction.
   *
   * @return the result of the subfunction
   */
  private double evaluateSubFunction() {
    // double stack[];  moved by W. Christian
    int stack_pointer = -1;
    int code_pointer = 0;
    int destination;
    char code;
    // stack = new double[STACK_SIZE];  moved by W. Christian
    int codeLength = postfix_code.length(); // added bt W. Christian to check the length.
    while(true) {
      try {
        if(code_pointer==codeLength) {
          return stack[0];                         // added by W. Christian.  Do not use doing an Exception!
        }
        code = postfix_code.charAt(code_pointer++);
      } catch(StringIndexOutOfBoundsException e) {
        return stack[0];
      }
      try {
        switch(code) {
           case '+' :
             stack[stack_pointer-1] += stack[stack_pointer];
             stack_pointer--;
             break;
           case '-' :
             stack[stack_pointer-1] -= stack[stack_pointer];
             stack_pointer--;
             break;
           case '*' :
             stack[stack_pointer-1] *= stack[stack_pointer];
             stack_pointer--;
             break;
           case '/' :
             if(stack[stack_pointer]!=0) {
               stack[stack_pointer-1] /= stack[stack_pointer];
             } else {
               stack[stack_pointer-1] /= 1.0e-128; // added by W.Christian to trap for divide by zero.
             }
             stack_pointer--;
             break;
           case '^' :
             stack[stack_pointer-1] = Math.pow(stack[stack_pointer-1], stack[stack_pointer]);
             stack_pointer--;
             break;
           case '_' :
             stack[stack_pointer] = -stack[stack_pointer];
             break;
           case JUMP_CODE :
             destination = code_pointer+postfix_code.charAt(code_pointer++);
             while(code_pointer<destination) {
               if(postfix_code.charAt(code_pointer++)==NUMERIC) {
                 numberindex++;
               }
             }
             break;
           case LESS_THAN :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]<stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case GREATER_THAN :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]>stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case LESS_EQUAL :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]<=stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case GREATER_EQUAL :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]>=stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case EQUAL :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]==stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case NOT_EQUAL :
             stack_pointer--;
             stack[stack_pointer] = (stack[stack_pointer]!=stack[stack_pointer+1]) ? 1.0 : 0.0;
             break;
           case IF_CODE :
             if(stack[stack_pointer--]==0.0) {
               destination = code_pointer+postfix_code.charAt(code_pointer++);
               while(code_pointer<destination) {
                 if(postfix_code.charAt(code_pointer++)==NUMERIC) {
                   numberindex++;
                 }
               }
             } else {
               code_pointer++;
             }
             break;
           case ENDIF :
             break;                                // same as NOP
           case AND_CODE :
             stack_pointer--;
             if((stack[stack_pointer]!=0.0)&&(stack[stack_pointer+1]!=0.0)) {
               stack[stack_pointer] = 1.0;
             } else {
               stack[stack_pointer] = 0.0;
             }
             break;
           case OR_CODE :
             stack_pointer--;
             if((stack[stack_pointer]!=0.0)||(stack[stack_pointer+1]!=0.0)) {
               stack[stack_pointer] = 1.0;
             } else {
               stack[stack_pointer] = 0.0;
             }
             break;
           case NOT_CODE :
             stack[stack_pointer] = (stack[stack_pointer]==0.0) ? 1.0 : 0.0;
             break;
           case NUMERIC :
             stack[++stack_pointer] = number[numberindex++];
             break;
           case PI_CODE :
             stack[++stack_pointer] = Math.PI;
             break;
           case E_CODE :
             stack[++stack_pointer] = Math.E;
             break;
           default :
             if(code>=REF_OFFSET) {
               stack[++stack_pointer] = refvalue[code-REF_OFFSET];
             } else if(code>=VAR_OFFSET) {
               stack[++stack_pointer] = var_value[code-VAR_OFFSET];
             } else if(code>=EXT_FUNC_OFFSET) {
               stack[stack_pointer-1] = builtInExtFunction(code-EXT_FUNC_OFFSET, stack[stack_pointer-1], stack[stack_pointer]);
               stack_pointer--;
             } else if(code>=FUNC_OFFSET) {
               stack[stack_pointer] = builtInFunction(code-FUNC_OFFSET, stack[stack_pointer]);
             } else {
               error = CODE_DAMAGED;
               return Double.NaN;
             }
        }
      } catch(ArrayIndexOutOfBoundsException oe) {
        error = STACK_OVERFLOW;
        return Double.NaN;
      } catch(NullPointerException ne) {
        error = CODE_DAMAGED;
        return Double.NaN;
      }
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
