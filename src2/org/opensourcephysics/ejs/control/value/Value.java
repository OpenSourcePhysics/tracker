/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.value;

/**
 * A <code>Value</code> is an object that holds an internal (but public)
 * variable. This abstract class provides a unified way of acessing the
 * variable value. The fact the variable is public permits quick access
 * to it.
 * <p>
 * When using subclasses, it is a good idea to directly access the
 * internal variable or use the correct 'get' method in order to
 * increase speed.
 * <p>
 * Using <code>Number</code> is not suitable for two reasons:
 * <ll>
 *   <li> <code>Number</code> does not include Strings and Objects
 *   <li> <code>Number</code> does not allow direct access to the
 *        internal variable
 * </ll>
 * @see java.lang.Number
 */
public abstract class Value {
  /**
   * Returns the value of the variable as a boolean
   */
  public abstract boolean getBoolean();

  /**
   * Returns the value of the variable as a byte
   */
  // public byte getByte() { return (byte) getInteger(); }

  /**
   * Returns the value of the variable as an int
   */
  public abstract int getInteger();

  /**
   * Returns the value of the variable as a double
   */
  public abstract double getDouble();

  /**
   * Returns the value of the variable as a String
   */
  public abstract String getString();

  /**
   * Returns the value of the variable as an Object. Ideal for arrays!
   */
  public abstract Object getObject();

  /**
   * Copies one value into another
   */
  public void copyValue(Value _source) {
    if(this instanceof DoubleValue) {
      ((DoubleValue) this).value = _source.getDouble();
    } else if(this instanceof IntegerValue) {
      ((IntegerValue) this).value = _source.getInteger();
    } else if(this instanceof BooleanValue) {
      ((BooleanValue) this).value = _source.getBoolean();
    } else if(this instanceof StringValue) {
      ((StringValue) this).value = _source.getString();
    } else if(this instanceof ObjectValue) {
      ((ObjectValue) this).value = _source.getObject();
    }
  }

  /**
   * Clones one value into another
   */
  public Value cloneValue() {
    if(this instanceof DoubleValue) {
      return new DoubleValue(this.getDouble());
    }
    if(this instanceof IntegerValue) {
      return new IntegerValue(this.getInteger());
    }
    if(this instanceof BooleanValue) {
      return new BooleanValue(this.getBoolean());
    }
    if(this instanceof StringValue) {
      return new StringValue(this.getString());
    }
    if(this instanceof ObjectValue) {
      return new ObjectValue(this.getObject());
    }
    return null;
  }

  public String toString() {
    return getString();
  }

  static public Value parseConstantOrArray(String _input, boolean _silentMode) {
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(_input, ","); //$NON-NLS-1$
    int dim = tkn.countTokens();
    if(dim<=1) {
      return parseConstant(_input, _silentMode);
    }
    Value[] data = new Value[dim];
    boolean hasDoubles = false, hasInts = false, hasBooleans = false;
    for(int i = 0; i<dim; i++) {
      data[i] = parseConstant(tkn.nextToken(), _silentMode);
      if(data[i]==null) {
        return parseConstant(_input, _silentMode);
      }
      if(data[i] instanceof DoubleValue) {
        hasDoubles = true;
      } else if(data[i] instanceof IntegerValue) {
        hasInts = true;
      } else if(data[i] instanceof BooleanValue) {
        hasBooleans = true;
      }
    }
    if(hasDoubles) {
      double[] doubleArray = new double[dim];
      for(int i = 0; i<dim; i++) {
        doubleArray[i] = data[i].getDouble();
      }
      return new ObjectValue(doubleArray);
    } else if(hasInts) {
      int[] intArray = new int[dim];
      for(int i = 0; i<dim; i++) {
        intArray[i] = data[i].getInteger();
      }
      return new ObjectValue(intArray);
    } else if(hasBooleans) {
      boolean[] booleanArray = new boolean[dim];
      for(int i = 0; i<dim; i++) {
        booleanArray[i] = data[i].getBoolean();
      }
      return new ObjectValue(booleanArray);
    }
    return parseConstant(_input, _silentMode);
  }

  static public String removeScapes(String str) {
    String txt = ""; //$NON-NLS-1$
    int l = str.length();
    for(int i = 0; i<l; i++) {
      char c = str.charAt(i);
      if(c=='\\') {
        if(i==(l-1)) {
          return txt+c;
        }
        c = str.charAt(++i);
      }
      txt += c;
    }
    return txt;
  }

  static public Value parseConstant(String _input, boolean _silentMode) {
    _input = _input.trim();
    if(_input.length()<=0) {
      return null;
    }
    if(_input.startsWith("\"")) {  // "String" //$NON-NLS-1$
      if(_input.length()<=1) {
        return null;
      }
      if(!_input.endsWith("\"")) { //$NON-NLS-1$
        // if (!_silentMode) System.err.println ("Value : Error 1! Incorrect input to parse "+_input);
        return null;
      }
      return new StringValue(removeScapes(_input.substring(1, _input.length()-1)));
    }
    if(_input.startsWith("'")) {  // "String" //$NON-NLS-1$
      if(!_input.endsWith("'")) { //$NON-NLS-1$
        // if (!_silentMode) System.err.println ("Value : Error 1! Incorrect input to parse "+_input);
        return null;
      }
      return new StringValue(removeScapes(_input.substring(1, _input.length()-1)));
    }
    if(_input.equals("true")) { //$NON-NLS-1$
      return new BooleanValue(true);
    }
    if(_input.equals("false")) { //$NON-NLS-1$
      return new BooleanValue(false);
    }
    if(_input.indexOf('.')>=0) {                                                   // double
      try {
        double v = Double.parseDouble(_input);
        return new DoubleValue(v);
      } catch(Exception e) {
        if(!_silentMode) {
          System.err.println("Value : Error 2! Incorrect input to parse "+_input); //$NON-NLS-1$
        }
        return null;
      }
    }
    // int
    try {
      int i = Integer.parseInt(_input);
      return new IntegerValue(i);
    } catch(Exception e) {
      // Must be a variable : Do not complain!
      // System.err.println ("Value : Error 3! Incorrect input to parse "+_input);
      return null;
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
