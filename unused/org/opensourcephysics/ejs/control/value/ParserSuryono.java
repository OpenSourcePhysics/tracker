/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.value;
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
 * Adapted by Francisco Esquembre for his own use                                         *
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
 * parser.defineVariable(0,"x");     // lets the variable be 'x'
 * parser.define("Math.sin(x)/x");        // defines function: sin(x)/x
 * parser.parse();                   // parses the function

 IMPORTANT: Notice that my variables start at 0 in this version

 *
 * // calculates: sin(x)/x with x = -5.0 .. +5.0 in 20 steps
 * // and prints the result to standard output.
 *
 * double result;
 * for (i=-10; i <= 10; i++) {
 *   parser.setVariable(0,(double)i/2.0f);
 *   result = parser.evaluate();
 *   System.out.println(result);
 * }
 * </pre>
 */
public final class ParserSuryono {
  // global variables
  private int var_count;                                           // number of variables
  private String var_name[];                                       // variables' name
  private double var_value[];                                      // value of variables
  private double number[];                                         // numeric constants in defined function
  private String function = "";                                    // function definition //$NON-NLS-1$
  private String postfix_code = "";                                // the postfix code //$NON-NLS-1$
  private boolean valid = false;                                   // postfix code status
  private int error;                                               // error code of last process
  private boolean ISBOOLEAN = false;                               // boolean flag
  private boolean INRELATION = false;                              // relation flag
  // variables used during parsing
  private int position;                                            // parsing pointer
  private int start;                                               // starting position of identifier
  private int num;                                                 // number of numeric constants
  private char character;                                          // current character
  // variables used during evaluating
  private int numberindex;                                         // pointer to numbers/constants bank
  private double[] refvalue = null;                                // value of references
  // built in constants and functions
  static private final String constname[] = {"Math.E", "Math.PI"}; // Added by Paco //$NON-NLS-1$ //$NON-NLS-2$
  static private final double constvalue[] = {Math.E, Math.PI};
  static private final String funcnameNoParam[] =                  // Added by Paco
  {"Math.random"};                                                 //$NON-NLS-1$
  static private final String funcname[] =                         // Changed by Paco
  {
    "Math.abs", "Math.acos", "Math.asin", "Math.atan", "Math.ceil", "Math.cos", "Math.exp", "Math.floor", "Math.log", "Math.rint", "Math.round", "Math.sin", "Math.sqrt", "Math.tan", "Math.toDegrees", "Math.toRadians"                                               //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$
  };
  static private final String extfunc[] =                                   // Changed by Paco
  {"Math.atan2", "Math.IEEEremainder", "Math.max", "Math.min", "Math.pow"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  private static final int MAX_NUM = 200;                            // max numeric constants// Changed to 200 by W. Christian
  private static final int NO_CONST = constname.length;              // no. of built-in Constants // Paco
  private static final int NO_FUNCSNOPARAM = funcnameNoParam.length; // no. of built-in functions with no parameters // Paco
  private static final int NO_FUNCS = funcname.length;    // no. of built-in functions // Paco
  private static final int NO_EXT_FUNCS = extfunc.length; // no. of extended functions
  private static final int STACK_SIZE = 50;               // evaluation stack size
  private double[] stack = new double[STACK_SIZE];        // moved by W. Christian from evaluate to global variables for speed
  // references - version 3.0
  private Hashtable<String, String> references = null;
  private Vector<String> refnames = null;
  // error codes

  /**
   * No error.
   */
  public static final int NO_ERROR = 0;

  /**
   * Syntax error.
   */
  public static final int SYNTAX_ERROR = 1;

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
  private static final int FUNCNOPARAM_OFFSET = EXT_FUNC_OFFSET+NO_EXT_FUNCS;
  private static final int VAR_OFFSET = 2000;
  private static final int REF_OFFSET = 3000;
  private static final char CONST_OFFSET = (char) 253;    // Paco
  // private  static final char PI_CODE             = (char)253;
  // private  static final char E_CODE              = (char)254;

  private static final char NUMERIC = (char) 300;         // Paco changed from 255 to 300 to leave space for constants
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
  private static final char AND_CODE = (char) 10;         // Boolean AND
  private static final char OR_CODE = (char) 11;          // Boolean OR
  private static final char NOT_CODE = (char) 12;         // Boolean NOT

  /**
   * Wether a given token is a reserved word
   * @param token
   * @return
   */
  static public boolean isKeyword(String token) {         // Added by Paco
    try {
      Double.parseDouble(token);
      return true;
    } // It's a valid constant
      catch(Exception _exc) {}
    // Do nothing
    for(int i = 0; i<NO_CONST; i++) {
      if(token.equals(constname[i])) {
        return true;
      }
    }
    for(int i = 0; i<NO_FUNCS; i++) {
      if(token.equals(funcname[i])) {
        return true;
      }
    }
    for(int i = 0; i<NO_EXT_FUNCS; i++) {
      if(token.equals(extfunc[i])) {
        return true;
      }
    }
    for(int i = 0; i<NO_FUNCSNOPARAM; i++) {
      if(token.equals(funcnameNoParam[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets an expression and returns a String[] of the variables the expression will need.
   * This can be used to decide beforehand how many variables a new parser should have.
   */
  static public String[] getVariableList(String _expression) { // Added by Paco
    Vector<String> varlist = getVariableList(_expression, new Vector<String>());
    if(varlist.size()<1) {
      return new String[0];
    }
    return varlist.toArray(new String[1]);
  }

  /**
   * Gets an expression and a Vector of declared variables and updates the Vector with
   * extra variables the expression will need.
   * This can be used to decide beforehand how many variables a new parser should have.
   * @return the same Vector updated
   */
  static public Vector<String> getVariableList(String _expression, Vector<String> varlist) { // Added by Paco
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(_expression, "() \t+-*/,<>=&|"); //$NON-NLS-1$
    while(tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if(isKeyword(token)) {
        continue;
      }
      if(!varlist.contains(token)) {
        varlist.add(token);
      }
    }
    return varlist;
  }

  /**
   * The constructor of <code>Parser</code>.
   *
   * @param variablecount the number of variables
   */
  public ParserSuryono(int variablecount) {
    var_count = variablecount;
    references = new Hashtable<String, String>();
    refnames = new Vector<String>();
    // arrays are much faster than vectors (IMHO)
    var_name = new String[variablecount];
    var_value = new double[variablecount];
    number = new double[MAX_NUM];
  }

  /**
   * Sets the variable names.
   *
   * @param index the variable index (one based)
   * @param name  the variable name
   */
  public void defineVariable(int index, String name) {
    // if (index >= var_count) return; // Paco suppressed this check
    var_name[index] = name; // Paco changed from index-1
  }

  /**
   * Sets the variable value.
   * The variable is accessed by index.
   *
   * @param index the variable index (one based)
   * @param value the variable value
   */
  public void setVariable(int index, double value) {
    // if (index >= var_count) return; // Paco suppressed this check
    var_value[index] = value; // Paco changed from index-1
  }

  /**
   * Defines a function. Current postfix code becomes invalid.
   *
   * @param definition the function definition
   */
  public void define(String definition) {
    function = definition;
    // function.toLowerCase(); // removed by Paco
    // function=removeEscapeCharacter(function);  // added by W. Christian
    valid = false;
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
    // added by W. Christian to trap for NaN
    if(Double.isNaN(result)) {
      result = 0.0;
    }
    return result;
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
    if((character=='e')||(character=='E')) { // Paco added 'E'
      numstr += 'e';
      getNextCharacter();                    // Paco changes character to 'e'
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
    if((character=='*')||(character=='/')||
    // (character == '^') ||  Paco suppressed '^'
    (character==')')||(character==',')||(character=='<')||(character=='>')||(character=='=')||(character=='&')||(character=='|')) {
      throw new ParserException(SYNTAX_ERROR);
    }
    do {
      stream += character;
      getNextCharacter();
    } while(!((character==' ')||(character=='+')||(character=='-')||(character=='*')||(character=='/')||
    // (character == '^') || Paco suppressed '^'
    (character=='(')||(character==')')||(character==',')||(character=='<')||(character=='>')||(character=='=')||(character=='&')||(character=='|')));
    for(int i = 0; i<NO_CONST; i++) {
      if(stream.equals(constname[i])) {
        addCode((char) (i+CONST_OFFSET));
        return;
      }
    }
    /* Paco
        if (stream.equals("pi")) {
          addCode(PI_CODE); return;
        }
        else
        if (stream.equals("e")) {
          addCode(E_CODE); return;
        }

        // if

        if (stream.equals("if")) {
          skipSpaces();
          if (character != '(')
            throw new ParserException(PAREN_EXPECTED);
          scanAndParse();
          if (character != ',')
            throw new ParserException(COMMA_EXPECTED);
          addCode(IF_CODE);
          String savecode = new String(postfix_code);
          postfix_code = "";
          scanAndParse();
          if (character != ',')
            throw new ParserException(COMMA_EXPECTED);
          addCode(JUMP_CODE);
          savecode += (char)(postfix_code.length()+2);
          savecode += postfix_code;
          postfix_code = "";
          scanAndParse();
          if (character != ')')
            throw new ParserException(PAREN_EXPECTED);
          savecode += (char)(postfix_code.length()+1);
          savecode += postfix_code;
          postfix_code = new String(savecode);
          getNextCharacter();
          return;
        }
    */
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
    // built-in function with no parameters  // Added by Paco
    for(int i = 0; i<NO_FUNCSNOPARAM; i++) {
      if(stream.equals(funcnameNoParam[i])) {
        skipSpaces();
        if(character!='(') {
          throw new ParserException(PAREN_EXPECTED);
        }
        skipSpaces(); // These two lines is one of the differences for no parameters. Paco
        getNextCharacter();
        if(character!=')') {
          throw new ParserException(PAREN_EXPECTED);
        }
        getNextCharacter();
        addCode((char) (i+FUNCNOPARAM_OFFSET));
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
    position = start;
    throw new ParserException(UNKNOWN_IDENTIFIER);
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
    // if ((character >= '0') && (character <= '9'))    changed by W. Christian to handle leanding zeros.
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
  /*
      private void arithmeticLevel3() throws ParserException {
      boolean negate;

      if (ISBOOLEAN)
        throw new ParserException(INVALID_OPERAND);
      negate = getIdentifier();
      if (ISBOOLEAN)
        throw new ParserException(INVALID_OPERAND);
      if (character == '^') arithmeticLevel3();
      addCode('^');
      if (negate) addCode('_');
    }
  */

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
      // if (character == '^') arithmeticLevel3(); Paco
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
      /* Paco
       if (character == '^') {
        arithmeticLevel3();
        if (negate) addCode('_');
      }
      else
    */
      if((character=='*')||(character=='/')) {
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
    // if ((character != '^') && (negate)) addCode('_');  Paco
    if(negate) {
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
         /* Paco
         case '^':
             arithmeticLevel3();
             if (negate) addCode('_');
             break;
           */
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
  /*
      { "Math.abs",   "Math.acos" , "Math.asin" , "Math.atan", "Math.ceil"  ,
        "Math.cos",   "Math.exp"  , "Math.floor", "Math.log" , "Math.rint",
        "Math.round", "Math.sin"  , "Math.sqrt" , "Math.tan" , "Math.toDegrees",
        "Math.toRadians" };
  */
  // Changed completely by Paco. Use of if (radian) suppressed
  private double builtInFunction(int function, double parameter) {
    switch(function) {
       case 0 :
         return Math.abs(parameter);
       case 1 :
         return Math.acos(parameter);
       case 2 :
         return Math.asin(parameter);
       case 3 :
         return Math.atan(parameter);
       case 4 :
         return Math.ceil(parameter);
       case 5 :
         return Math.cos(parameter);
       case 6 :
         return Math.exp(parameter);
       case 7 :
         return Math.floor(parameter);
       case 8 :
         return Math.log(parameter);
       case 9 :
         return Math.rint(parameter);
       case 10 :
         return Math.round(parameter);
       case 11 :
         return Math.sin(parameter);
       case 12 :
         return Math.sqrt(parameter);
       case 13 :
         return Math.tan(parameter);
       case 14 :
         return Math.toDegrees(parameter);
       case 15 :
         return Math.toRadians(parameter);
       default :
         error = CODE_DAMAGED;
         return Double.NaN;
    }
  }

  private double builtInFunctionNoParam(int function) { // Paco
    switch(function) {
       case 0 :
         return Math.random();
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
  // { "Math.atan2", "Math.IEEEremainder", "Math.max", "Math.min", "Math.pow"};
  private double builtInExtFunction(int function, double param1, double param2) {
    switch(function) {
       case 0 :
         return Math.atan2(param1, param2);
       case 1 :
         return Math.IEEEremainder(param1, param2);
       case 2 :
         return Math.max(param1, param2);
       case 3 :
         return Math.min(param1, param2);
       case 4 :
         return Math.pow(param1, param2);
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
           /*
           case '^': stack[stack_pointer-1] = Math.pow(stack[stack_pointer-1],
                                                       stack[stack_pointer]);
                     stack_pointer--; break;
           */
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
           // case PI_CODE       : stack[++stack_pointer] = Math.PI; break; Paco
           // case E_CODE        : stack[++stack_pointer] = Math.E; break; Paco
           default :
             if(code>=REF_OFFSET) {
               stack[++stack_pointer] = refvalue[code-REF_OFFSET];
             } else if(code>=VAR_OFFSET) {
               stack[++stack_pointer] = var_value[code-VAR_OFFSET];
             } else if(code>=FUNCNOPARAM_OFFSET) { // Paco
               stack[++stack_pointer] =            // This is the other difference for no parameters ! Paco
                 builtInFunctionNoParam(code-FUNCNOPARAM_OFFSET);
             } else if(code>=EXT_FUNC_OFFSET) {
               stack[stack_pointer-1] = builtInExtFunction(code-EXT_FUNC_OFFSET, stack[stack_pointer-1], stack[stack_pointer]);
               stack_pointer--;
             } else if(code>=FUNC_OFFSET) {
               stack[stack_pointer] = builtInFunction(code-FUNC_OFFSET, stack[stack_pointer]);
             } else if(code>=CONST_OFFSET) { // Added by Paco for constants
               stack[++stack_pointer] = constvalue[code-CONST_OFFSET];
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
