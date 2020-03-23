/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.lang.reflect.Method;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A class to store and invoke methods using reflection.
 */
public class MethodWithOneParameter {
  private String methodName;
  private int methodType;
  private Object targetObject = null;
  private Object[] parameterList = {};
  private Method methodToCall;
  private MethodWithOneParameter secondMethod = null;
  private Value returnValue = null; // AMAVP (See Note in ControlElement)

  /**
   * Equivalent to MethodWithOneParameter (_type, _target, _name, null);
   */
  // MethodWithOneParameter (int _type, Object _target, String _name) {
  // this(_type, _target, _name, null);
  // }

  /**
   * Creates a new method from the input parameters.
   * @param int _type An integer type to help create families of actions
   * @param Object _target The object that implements the method
   * @param String _name The description of the method.
   *   1.- If the method's parameter list is void, then you can specify either
   *       'method()' or just 'method'
   *   2.- If the method accepts a boolean, you can specify either 'method(true)'
   *       or 'method(false)'
   *   3.- If the method accepts a double, you can specify something like 'method(1.0)'
   *   4.- If the method accepts an integer, you can specify something like 'method(1)'
   *   5.- If the method accepts a String, you can specify something like 'method("my string")'
   *   In all cases, the first version is the recommended one.
   * @param MethodWithOneParameter _secondMethod A second action that will be invoked
   *   following this one. This is useful when you want to call more than one methods at once
   * @param _anObject and object for the very special case of method("#CONTROL#"); //Added on Jan 31st 2004
   */
  MethodWithOneParameter(int _type, Object _target, String _name, String _returnType, MethodWithOneParameter _secondMethod, Object _anObject) {
    Class<?>[] classList = {};
    Object parameter = null;
    Class<?> parameterClass = null;
    methodName = _name;
    methodType = _type;
    targetObject = _target;
    secondMethod = _secondMethod;
    String parts[] = splitMethodName(_name.trim());
    if(parts[2].equals("#CONTROL#")&&(_anObject!=null)) { //$NON-NLS-1$
      parameter = _anObject;
      parameterClass = _anObject.getClass();
      // System.out.println ("Class of OBJECT is "+parameterClass);
    } else {
      Value value = Value.parseConstant(parts[2], false); // NO silent mode
      if(value instanceof StringValue) {                  // method ("String")
        parameter = value.getString();
        parameterClass = _name.getClass();                // String
      } else if(value instanceof BooleanValue) {          // method (boolean)
        parameter = new Boolean(value.getBoolean());
        parameterClass = Boolean.TYPE;
      } else if(value instanceof DoubleValue) {           // method (double)
        parameter = new Double(value.getDouble());
        parameterClass = Double.TYPE;
      } else if(value instanceof IntegerValue) {          // method (int)
        parameter = new Integer(value.getInteger());
        parameterClass = Integer.TYPE;
      }
    }
    if(parameter!=null) {            // method(parameter);
      classList = new Class[1];
      classList[0] = parameterClass; // parameter
      parameterList = new Object[1];
      parameterList[0] = parameter;
    }
    methodToCall = resolveMethod(targetObject, parts[1], classList);
    if(methodToCall==null) {
      System.err.println(getClass().getName()+" : Error! Unable to find a suitable method "+methodName+" in class "+targetObject.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if(_returnType==null) {
      returnValue = null;                                 // AMAVP
    } else {
      _returnType = _returnType.trim().toLowerCase();
      if(_returnType.equals("double")) {                  //$NON-NLS-1$
        returnValue = new DoubleValue(0.0);
        // else if (_returnType.equals("byte"))    returnValue = new IntegerValue(0);
      } else if(_returnType.equals("int")) {              //$NON-NLS-1$
        returnValue = new IntegerValue(0);
      } else if(_returnType.equals("string")) {           //$NON-NLS-1$
        returnValue = new StringValue("");                //$NON-NLS-1$
      } else if(_returnType.equals("boolean")) {          //$NON-NLS-1$
        returnValue = new BooleanValue(false);
      } else {
        returnValue = null;                               // return type is void
      }
    }
  }

  public Value invoke(int _type, Object _callingObject) { // Modified for AMAVP
    if(methodType!=_type) {
      return null;
    }
    // System.out.println ("Invoking method "+this.methodName+" with Value "+parameterList);
    try {
      if(returnValue==null) { // void return type
        methodToCall.invoke(targetObject, parameterList);
      } else if(returnValue instanceof DoubleValue) {
        ((DoubleValue) returnValue).value = ((Double) methodToCall.invoke(targetObject, parameterList)).doubleValue();
      } else if(returnValue instanceof IntegerValue) {
        ((IntegerValue) returnValue).value = ((Integer) methodToCall.invoke(targetObject, parameterList)).intValue();
      } else if(returnValue instanceof BooleanValue) {
        ((BooleanValue) returnValue).value = ((Boolean) methodToCall.invoke(targetObject, parameterList)).booleanValue();
      } else if(returnValue instanceof StringValue) {
        ((StringValue) returnValue).value = methodToCall.invoke(targetObject, parameterList).toString();
      }
      if(secondMethod!=null) {
        secondMethod.invoke(_type, _callingObject);
      }
    } catch(Exception exc) {
      exc.printStackTrace(System.err);
      return null;
    }
    return returnValue;
  }

  public boolean equals(int _type, Object _target, String _name) {
    if(methodType!=_type) {
      return false;
    }
    if(targetObject!=_target) {
      return false;
    }
    return methodName.equals(_name);
  }

  public String toString() {
    return methodName;
  }

  static public Method resolveMethod(Object _target, String _name, Class<?>[] _classList) {
    java.lang.reflect.Method[] allMethods = _target.getClass().getMethods();
    for(int i = 0; i<allMethods.length; i++) {
      // Added by W. Christian:  SwingJS getMethods returns functions that end with $
      String jsName= _name;
      if(org.opensourcephysics.js.JSUtil.isJS && allMethods[i].getName().endsWith("$") && !_name.endsWith("$")){jsName= _name+"$";} // append $ to name if needed
      if(!allMethods[i].getName().equals(jsName)) {
        continue;
      }
      Class<?>[] parameters = allMethods[i].getParameterTypes();
      if(parameters.length!=_classList.length) {
        continue;
      }
      boolean fits = true;
      for(int j = 0; j<parameters.length; j++) {
        if(!parameters[j].isAssignableFrom(_classList[j])) {
          fits = false;
          break;
        }
      }
      if(fits) {
    	if(org.opensourcephysics.js.JSUtil.isJS){
        	java.lang.reflect.Method m=null;
        	try {
        	  m= _target.getClass().getMethod(_name, parameters); 
        	}catch(NoSuchMethodException nsme) {
        	      System.err.println("Error resolving method"+_name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        	return m;    		
    	}
        return allMethods[i]; //WC: SwingJS adds an extra $ to methods in this array
      }
    }
    return null;
  }

  static public String[] splitMethodName(String _inputName) {
    String part[] = new String[3];
    String restOfIt = _inputName;
    int index1 = _inputName.indexOf('.');
    int index2 = _inputName.indexOf('(');
    if((index1>0)&&((index2<0)||(index2>index1))) {
      part[0] = _inputName.substring(0, index1); // target
      restOfIt = _inputName.substring(index1+1); // rest of it
    } else {
      part[0] = null;                            // No target
    }
    index1 = restOfIt.indexOf("("); //$NON-NLS-1$
    if(index1<=0) {                                                               // format 'method'
      part[1] = restOfIt;
      part[2] = null;
    } else {
      // format 'method()' or 'method(parameter)'
      part[1] = restOfIt.substring(0, index1).trim();
      restOfIt = restOfIt.substring(index1);
      index2 = restOfIt.lastIndexOf(')');
      if(index2<0) {
        System.err.println(" : Error! Incorrect method description "+_inputName); //$NON-NLS-1$
        return null;
      }
      part[2] = restOfIt.substring(1, index2).trim();
    }
    // for (int i=0; i<part.length; i++) System.out.println ("Part["+i+"] = "+part[i]);
    return part;
  }

} // End of class

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
