/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

// Note 1 : comment "AMAVP" stands for "Accept methods as variable properties"
// stands for the BIG change when I first introduced
// expressions as possible values for the properties
package org.opensourcephysics.ejs.control;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.ejs.Simulation;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.ExpressionValue;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.ObjectValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * <code>ControlElement</code> is a base class for an object that
 * can be managed using a series of configurable properties, hold actions
 * that when invoked graphically call other objects' methods, and be
 * responsible for the display and change of one or more internal variables.
 * <p>
 * <code>ControlElement</code>s can be included into a GroupControl,
 * thus acting in a coordinated way.
 * <p>
 * In fact, the best way to use a <code>ControlElement</code>, is to include
 * it into a GroupControl and then configure it using the
 * <code>setProperty()</code> method.
 * <p>
 * After this, the value common to several of these ControlElements can be
 * set and retrieved using a single setValue() or getValue() call from the
 * ControlGroup.
 * <p>
 * You can also add any action you want to a ControlElement, but it is the
 * implementing class' responsibility to trigger an action in response
 * to a user's gesture (with the mouse or keyboard)
 * <p>
 * @see     GroupControl
 */
public abstract class ControlElement {
  protected GroupControl myGroup = null;                                                   // The group of ControlElements with which I share variables
  protected Hashtable<String, String> myPropertiesTable = new Hashtable<String, String>(); // A place to hold any property
  protected Object myObject = null;                                                            // The wrapped object
  private boolean myActiveState = true;                                                        // Whether I am active or not
  private Vector<MethodWithOneParameter> myActionsList = new Vector<MethodWithOneParameter>(); // My list of actions
  private GroupVariable[] myProperties = null;                                                 // The variables for the registered properties
  private String[] myPropertiesNames = null;                                                   // The names of the registered properties
  protected boolean isUnderEjs = false;
  MethodWithOneParameter[] myMethodsForProperties = null;                                      // AMAVP
  ExpressionValue[] myExpressionsForProperties = null;                                         // AMAVP
  // ------------------------------------------------
  // Static constants and constructor
  // ------------------------------------------------
  public static final int NAME = 0;                                                            // The name of the element
  public static final int ACTION = 0;
  public static final int VARIABLE_CHANGED = 1;
  public static final int METHOD_FOR_VARIABLE = 2;                                             // AMAVP
  public static final String METHOD_TRIGGER = "_expr_";                                        // AMAVP //$NON-NLS-1$

  /**
   * Constructor ControlElement
   * @param _object
   */
  public ControlElement(Object _object) {
    // Create the list of registered properties
    ArrayList<?> info = getPropertyList();
    myObject = _object;
    myPropertiesNames = new String[info.size()];
    myProperties = new GroupVariable[info.size()];
    myMethodsForProperties = new MethodWithOneParameter[info.size()]; // AMAVP
    myExpressionsForProperties = new ExpressionValue[info.size()];    // AMAVP
    for(int i = 0; i<info.size(); i++) {
      String property = (String) info.get(i);
      myPropertiesNames[i] = property;
      myProperties[i] = null;
      myMethodsForProperties[i] = null; // AMAVP
      myExpressionsForProperties[i] = null;
    }
  }

  public Object getObject() {
    return myObject;
  }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------

  /**
   * Returns the list of all properties that can be set for this
   * ControlElement.
   * Subclasses that add properties should implement this.
   * Order is crucial here: Both for the presentation in an editor
   * (f.i. ViewElement) and for the setValue() method.
   */
  public abstract ArrayList<String> getPropertyList();

  /**
   * Returns information about a given property.
   * Subclasses that add properties should implement this.
   * Order in the implementation is irrelevant.
   * <ll>
   *   <li> The first keyword is ALWAYS the type. If more than one type is
   *     accepted, they are separated by | (do NOT use spaces!)
   *   <li> The keyword <b>CONSTANT</b> applies to properties that can not be
   *     changed using the setValue() methods
   *   <li> The keyword <b>VARIABLE_EXPECTED</b> is used when a String could be
   *     accepted, but a variable has priority. In this case, a String requires
   *     using inverted commas or quotes
   *   <li> The keyword <b>NotTrimmed</b> specifies that leading or trailing
   *     spaces must be respected when present. This is useful for labels or
   *     titles, for instance
   *   <li> The keyword <b>BASIC</b> is used by Ejs to group properties to the left
   *     hand side of the property editor
   *   <li> The keyword <b>HIDDEN</b> is used by Ejs so that it does not display
   *     an entry in the editor field
   *   <li> The keywords <b>PREVIOUS</b> and <b>POSTPROCESS</b> indicate that,
   *     when setting several properties at once (using setProperties()) the
   *     property must be process before, resp. after, the others
   *  </ll>
   */
  public abstract String getPropertyInfo(String _property);

  /**
   * Checks if a value can be considered a valid constant value for a property
   * If not, it returns null, meaning the value can be considered to be
   * a GroupVariable
   * @param     String _property The property name
   * @param     String _value The proposed value for the property
   */
  public Value parseConstant(String _propertyType, String _value) {
    if(_value==null) {
      return null;
    }
    Value constantValue;
    if(_propertyType.indexOf("boolean")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.booleanConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Color")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.colorConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("File")>=0) {       //$NON-NLS-1$
      String codebase = null;
      if(getProperty("_ejs_codebase")!=null) {   //$NON-NLS-1$
        codebase = getProperty("_ejs_codebase"); //$NON-NLS-1$
      } else if((getSimulation()!=null)&&(getSimulation().getCodebase()!=null)) {
        codebase = getSimulation().getCodebase().toString();
      }
      if(Utils.fileExists(codebase, _value)) {
        return new StringValue(_value);
      }
    }
    if(_propertyType.indexOf("Font")>=0) { //$NON-NLS-1$
      java.awt.Font currentFont = null;
      if(getVisual()!=null) {
        currentFont = getVisual().getFont();
      }
      constantValue = ConstantParser.fontConstant(currentFont, _value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Format")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.formatConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if((_propertyType.indexOf("Margins")>=0)||(_propertyType.indexOf("Rectangle")>=0)) { //$NON-NLS-1$ //$NON-NLS-2$
      constantValue = ConstantParser.rectangleConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    return null;
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------

  /**
   * Sets the value of the registered variables.
   * Subclasses with internal values should extend this
   * Order is crucial here: it must match exactly that of the getPropertyList()
   * method.
   * @param int _index   A keyword index that distinguishes among variables
   * @param Value _value The object holding the value for the variable.
   */
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case NAME :
         if(myGroup!=null) {
           myGroup.rename(this, _value.toString());
         }
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case NAME :
         if(myGroup!=null) {
           myGroup.rename(this, null);
         }
         break;
    }
  }

  /**
   * Gets the value of any internal variable.
   * Subclasses with internal values should extend this
   * @param int _index   A keyword index that distinguishes among variables
   * @return Value _value The object holding the value for the variable.
   */
  public Value getValue(int _index) {
    return null;
  }

  // -------------------------------------------
  // Methods that deal with properties
  // -------------------------------------------

  /**
   * Sets a property for this <code>ControlElement</code>. Implementing
   * classes are responsible of deciding (by declaring them in the
   * getPropertyList() method) what properties turn into visual
   * changes, or different behaviour, of the ControlElement.
   * <p>
   * However, every propery is accepted, even if it is not meaningful for a
   * particular implementation of this interface. This can serve as a
   * repository of information for future use.
   * <p>
   * Implementing classes should make sure that the following
   * requirements are met:
   * <ll>
   *   <li> Properties can be set in any order. The final result
   *        should not depend on the order. Exceptions must be
   *        explicitly documented.
   *   <li> Any property can be modified. If so, the old value,
   *        and whatever meaning it had, is superseded by the
   *        new one. If the new one is null, the old one is simply removed
   *        and setDefaultValue(index) is called in case a precise default
   *        value should be used.
   *   <li> When the element is part of a GroupControl, final users should
   *        not use this setProperty method directly, but go through the
   *        corresponding method of the group.
   * </ll>
   * @return    This same element. This is useful to nest more
   *            than one call to <code>setProperty</code>
   * @param     String _property The property name
   * @param     String _value    The value desired for the property
   * @see       GroupControl
   */
  // This one is not final because a few of the subclasses
  // (f. i. ControlContainer and ControlTrace) need to overwrite it
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if(_property.equals("_ejs_")) { //$NON-NLS-1$
      isUnderEjs = true;
    }
    // Let's see if the proposed property is registered as a real property
    int index = propertyIndex(_property);
    if(index<0) {
      // It is not a registered property. Store the value but do not call setValue()
      if(_value==null) {
        myPropertiesTable.remove(_property);
      } else {
        myPropertiesTable.put(_property, _value);
      }
      return this;
    }
    // The property is registered. Unregister and call setValue
    myMethodsForProperties[index] = null;     // AMAVP
    myExpressionsForProperties[index] = null; // AMAVP
    if(myProperties[index]!=null) { // remove from the list of listeners for this GroupVariable
      myProperties[index].removeElementListener(this, index);
      myProperties[index] = null;
    }
    if(_value==null) {                     // Treat the easy case separately, so that to avoid a lot of 'if (null)' checks
      if(myProperties[index]!=null) {      // remove from the list of listeners for this GroupVariable
        myProperties[index].removeElementListener(this, index);
        myProperties[index] = null;
      }
      setDefaultValue(index);              // use a default value
      myPropertiesTable.remove(_property); // remove the property
      return this;
    }
    // From now on, the value is, necessarily, not null
    // Some properties should not be trimmed ('text', for instance)
    if(!propertyIsTypeOf(_property, "NotTrimmed")) { //$NON-NLS-1$
      _value = _value.trim();
    }
    String originalValue = _value;
    // Because of backwards compatibility with version 3.01 or earlier
    // There might be confusion with constant strings versus variable names
    // This is the reason for most of the following block
    // From this version on, it is recommended that constant strings should be
    // delimited by either ' or "
    Value constantValue = null;
    if(_value.startsWith("%")&&_value.endsWith("%")&&(_value.length()>2)) {        //$NON-NLS-1$ //$NON-NLS-2$
      _value = _value.substring(1, _value.length()-1);                             // Force a variable or method
    } else if(_value.startsWith("@")&&_value.endsWith("@")&&(_value.length()>2)) { //$NON-NLS-1$ //$NON-NLS-2$
      // empty                                                                                                     // Do nothing for parsed expressions
    } else if(_value.startsWith("#")&&_value.endsWith("#")&&(_value.length()>2)) { //$NON-NLS-1$ //$NON-NLS-2$
      // empty                                                                                                     // Do nothing for variables such as f()
    } else {
      if(_value.startsWith("\"")||_value.startsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
        // empty                                                                                                   // It IS a constant String, don't try anything else
      } else {
        // First look for a CONSTANT property that can not be associated to GroupVariables
        if(propertyIsTypeOf(_property, "CONSTANT")) {                                                       //$NON-NLS-1$
          constantValue = new StringValue(_value);
        }
        // Check for String properties
        if(constantValue==null) {
          if(propertyType(_property).equals("String")&&!propertyIsTypeOf(_property, "VARIABLE_EXPECTED")) { // See TextField f.i. //$NON-NLS-1$ //$NON-NLS-2$
            constantValue = new StringValue(_value);
          }
        }
      }
      //
      // End of the compatibility block
      //
      // Now try the particular parser
      // The particular parser comes first because it can discriminate between
      // a real String and a File, f.i.
      if(constantValue==null) {
        constantValue = parseConstant(propertyType(_property), _value);
      }
      // Finally the standard parser
      if(constantValue==null) {
        constantValue = Value.parseConstantOrArray(_value, true); // silentMode
      }
    }
    if(constantValue!=null) { // Just set the value for this property
      // System.out.println ("property = "+_property+" = "+_value+" of the element "+this.toString()+"  is a constant!");
      if((constantValue instanceof StringValue)&&propertyIsTypeOf(_property, "TRANSLATABLE") // Apply Translator //$NON-NLS-1$
        &&(OSPRuntime.getTranslator()!=null)) {    // added by D Brown 2007-10-17
        Object target = null;
        if(myGroup!=null) {
          target = myGroup.getTarget("_default_"); //$NON-NLS-1$
        }
        String translated = OSPRuntime.getTranslator().getProperty(target.getClass(), constantValue.getString());
        if(!constantValue.getString().equals(translated)) {
          constantValue = new StringValue(translated);
        }
      }
      setValue(index, constantValue);
    } else {                                       // Associate the property with a GroupVariable or Method for later use
      // System.out.println (_value+" for the property "+_property+" of the element "+this.toString()+"  is a variable!: "+originalValue);
      // if (myProperties[index]!=null) { // remove from the list of listeners for this GroupVariable
      // myProperties[index].removeElementListener(this,index);
      // myProperties[index] = null;
      // }
      if(myGroup!=null) {
        boolean isNormalVariable = true, isExpression = false;
        if(_value.startsWith("#")&&_value.endsWith("#")&&(_value.length()>2)) {                   //$NON-NLS-1$ //$NON-NLS-2$
          _value = _value.substring(1, _value.length()-1);
          isNormalVariable = true;
        } else if(_value.startsWith("@")&&_value.endsWith("@")&&(_value.length()>2)) {            //$NON-NLS-1$ //$NON-NLS-2$
          _value = _value.substring(1, _value.length()-1);
          originalValue = _value;
          isNormalVariable = false;
          isExpression = true;
        } else if(_value.indexOf('(')>=0) {
          isNormalVariable = false;                                                               // It mist be a method
        }
        // Begin --- AMAVP
        if(isNormalVariable) {                                                                    // Connect a variable property with a normal variable name
          // This is what would normally happen under Ejs with expressions
          Value newValue = null;
          // If not under Ejs, get the actual value and use it when you register
          // to the group. This is arguable...
          if(getProperty("_ejs_")==null) {                                                        //$NON-NLS-1$
            newValue = getValue(index);
          }
          if(newValue==null) {
            // if      (propertyIsTypeOf(_property,"[]"))      newValue = new ObjectValue(null);
            // else
            if(propertyIsTypeOf(_property, "double")) {                                           //$NON-NLS-1$
              newValue = new DoubleValue(0.0);
            } else if(propertyIsTypeOf(_property, "boolean")) {                                   //$NON-NLS-1$
              newValue = new BooleanValue(false);
            } else if(propertyIsTypeOf(_property, "int")) {                                       //$NON-NLS-1$
              newValue = new IntegerValue(0);
            } else if(propertyIsTypeOf(_property, "String")) {                                    //$NON-NLS-1$
              newValue = new StringValue(_value);
            } else {
              newValue = new ObjectValue(null);
            }
          }
          myProperties[index] = myGroup.registerVariable(_value, this, index, newValue);
        } else if(isExpression) {                                                                 // Connect a variable property to an expression
          String returnType = null;
          if(propertyIsTypeOf(_property, "double")) {                                             //$NON-NLS-1$
            returnType = "double";                                                                //$NON-NLS-1$
          } else if(propertyIsTypeOf(_property, "boolean")) {                                     //$NON-NLS-1$
            returnType = "boolean";                                                               //$NON-NLS-1$
          } else if(propertyIsTypeOf(_property, "int")) {                                         //$NON-NLS-1$
            returnType = "int";                                                                   //$NON-NLS-1$
          } else if(propertyIsTypeOf(_property, "String")) {                                      //$NON-NLS-1$
            returnType = "String";                                                                //$NON-NLS-1$
          } else if(propertyIsTypeOf(_property, "Action")) {                                      //$NON-NLS-1$
            returnType = "Action";                                                                //$NON-NLS-1$
          } else {
            System.out.println("Error for property "+_property+" of the element "+this.toString() //$NON-NLS-1$ //$NON-NLS-2$
                               +". Cannot be set to : "+originalValue); //$NON-NLS-1$
            myPropertiesTable.put(_property, originalValue);
            return this;
          }
          if(!returnType.equals("Action")) {                            //$NON-NLS-1$
            myExpressionsForProperties[index] = new ExpressionValue(_value, myGroup);
            myGroup.methodTriggerVariable.addElementListener(this, index);
            myProperties[index] = myGroup.methodTriggerVariable;
          }
        } else {                                                        // Connect a variable property to a method
          // System.out.println ("Connecting property "+_property+" to method '"+_value+"' for the element "+this.toString());
          // Under Ejs do something reasonable.
          // For instance Labels need a string to size themselves properly
          if(getProperty("_ejs_")!=null) {                                                          // Do nothing in Ejs //$NON-NLS-1$
            // System.out.println ("Under ejs");
          } else {
            String returnType = null;
            if(propertyIsTypeOf(_property, "double")) {                                             //$NON-NLS-1$
              returnType = "double";                                                                //$NON-NLS-1$
            } else if(propertyIsTypeOf(_property, "boolean")) {                                     //$NON-NLS-1$
              returnType = "boolean";                                                               //$NON-NLS-1$
            } else if(propertyIsTypeOf(_property, "int")) {                                         //$NON-NLS-1$
              returnType = "int";                                                                   //$NON-NLS-1$
              // else if (propertyIsTypeOf(_property,"byte"))    returnType = "byte";
            } else if(propertyIsTypeOf(_property, "String")) {                                      //$NON-NLS-1$
              returnType = "String";                                                                //$NON-NLS-1$
            } else {
              System.out.println("Error for property "+_property+" of the element "+this.toString() //$NON-NLS-1$ //$NON-NLS-2$
                                 +". Cannot be set to : "+originalValue);                                //$NON-NLS-1$
              myPropertiesTable.put(_property, originalValue);
              return this;
            }
            // Resolve for non-default target
            String[] parts = MethodWithOneParameter.splitMethodName(_value);
            if(parts==null) {
              System.err.println(getClass().getName()+" : Error! method <"+originalValue+"> not found"); //$NON-NLS-1$ //$NON-NLS-2$
              myPropertiesTable.put(_property, originalValue);
              return this;
            }
            if(parts[0]==null) {
              parts[0] = "_default_";                                                                  //$NON-NLS-1$
            }
            Object target = myGroup.getTarget(parts[0]);
            if(target==null) {
              System.err.println(getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
              myPropertiesTable.put(_property, originalValue);
              return this;
            }
            if(parts[2]==null) {
              _value = parts[1]+"()";                                                                                                //$NON-NLS-1$
            } else {
              _value = parts[1]+"("+parts[2]+")";                                                                                    //$NON-NLS-1$ //$NON-NLS-2$
            }
            myMethodsForProperties[index] = new MethodWithOneParameter(METHOD_FOR_VARIABLE, target, _value, returnType, null, this); // Pass the element itself Jan 31st 2004 Paco
            // Register the property of this element to a standard boolean (why not?) variable
            // myGroup.update() will take care of triggering the method
            myGroup.methodTriggerVariable.addElementListener(this, index);
            myProperties[index] = myGroup.methodTriggerVariable;
            // myProperties[index] = myGroup.registerVariable (METHOD_TRIGGER,this,index,new BooleanValue(false));
          } // End of the real part, i.e. not under Ejs
        }   // End --- AMAVP
      }
    }
    myPropertiesTable.put(_property, originalValue);
    return this;
  }

  /**
   * Sets more than one property at once. The pairs
   * <code>property=value</code> must be separated by ';'.
   * If any value has a ';' in it, then it must be set
   * in a separate <code>setProperty</code> call.
   * @return    This same element. This is useful to nest more
   *            than one call to <code>setProperties</code>
   * @param     String _propertyList The list of properties and Values
   *            to be set
   */
  final public ControlElement setProperties(String _propertyList) {
    Hashtable<String, String> propTable = new Hashtable<String, String>();
    StringTokenizer tkn = new StringTokenizer(_propertyList, ";"); //$NON-NLS-1$
    while(tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if(token.trim().length()<=0) {
        continue;
      }
      int index = token.indexOf("=");                                                                   //$NON-NLS-1$
      if(index<0) {
        System.err.println(getClass().getName()+" : Error! Token <"+token+"> invalid for "+toString()); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        propTable.put(token.substring(0, index).trim(), token.substring(index+1));
      }
    }
    return setProperties(propTable);
  }

  // This is necessary just to make sure that some properties are processed
  // first and some others (such as 'value') last
  private void preprocess(String _property, Hashtable<String, String> _propertyTable) {
    String value = _propertyTable.get(_property);
    if(value!=null) {
      setProperty(_property, value);
      _propertyTable.remove(_property);
    }
  }

  private ControlElement setProperties(Hashtable<String, String> _propertyTable) {
    // _ejs_ is used by Ejs to signal that the element is working under it.
    // This has some consequences in the behaviour of some properties
    // (f.i. 'exit' on a Frame will not exit the application)
    preprocess("_ejs_", _propertyTable); //$NON-NLS-1$
    Hashtable<String, String> postTable = new Hashtable<String, String>();
    for(Enumeration<String> e = _propertyTable.keys(); e.hasMoreElements(); ) {
      String key = e.nextElement();
      // Some need to be processed before the others
      if(propertyIsTypeOf(key, "PREVIOUS")) {           //$NON-NLS-1$
        preprocess(key, _propertyTable);
        // And some need to be the last ones
      } else if(propertyIsTypeOf(key, "POSTPROCESS")) { //$NON-NLS-1$
        String value = _propertyTable.get(key);
        _propertyTable.remove(key);
        postTable.put(key, value);
      }
    }
    // Process the normal ones
    for(Enumeration<String> e = _propertyTable.keys(); e.hasMoreElements(); ) {
      String key = e.nextElement();
      setProperty(key, _propertyTable.get(key));
    }
    // Finally proccess those which need to be the last ones
    for(Enumeration<String> e = postTable.keys(); e.hasMoreElements(); ) {
      String key = e.nextElement();
      setProperty(key, postTable.get(key));
    }
    return this;
  }

  /**
   * Returns the value of a property.
   * @param     String _property The property name
   */
  final public String getProperty(String _property) {
    return myPropertiesTable.get(_property);
  }

  /**
   * Returns whether a property information contains a given keyword in its preamble
   * @param     String _property The property name
   * @param     String _keyword The keyword to look for
   */
  final public boolean propertyIsTypeOf(String _property, String _keyword) {
    String info = getPropertyInfo(_property);
    if(info==null) {
      return false;
    }
    if(info.toLowerCase().indexOf(_keyword.toLowerCase())>=0) {
      return true;
    }
    return false;
  }

  /**
   * Returns the type of the property
   * @param     String _property The property name
   * @return    String The type of the property
   */
  final public String propertyType(String _property) {
    String info = getPropertyInfo(_property);
    if(info==null) {
      return "double"; //$NON-NLS-1$
    }
    StringTokenizer tkn = new StringTokenizer(info, " "); //$NON-NLS-1$
    if(tkn.countTokens()>=1) {
      return tkn.nextToken();
    }
    return "double"; //$NON-NLS-1$
  }

  /**
   * Provided for backwards compatibiliy only
   */
  public java.awt.Component getComponent() {
    return null;
  }

  /**
   * Provided for backwards compatibiliy only
   */
  public java.awt.Component getVisual() {
    return null;
  }

  /**
   * resets the element
   */
  public void reset() {}

  /**
   * initializes the element. A kind of soft reset()
   */
  public void initialize() {}

  /**
   * refresh the element
   */
  // final public void  update() { } Moved to interface NeedsUpdate

  /**
   * Returns the integer index of a given variable property
   */
  private int propertyIndex(String _property) {
    if(myPropertiesNames!=null) {
      for(int i = 0; i<myPropertiesNames.length; i++) {
        if(myPropertiesNames[i].equals(_property)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Whether the element implements a given property
   * @param _property the property
   */
  public boolean implementsProperty(String _property) {
    return(propertyIndex(_property)>=0);
  }

  /**
   * Clear all registered internal variable properties
   */
  final public void variablePropertiesClear() {
    if(myPropertiesNames!=null) {
      for(int i = 0; i<myPropertiesNames.length; i++) {
        setProperty(myPropertiesNames[i], null);
      }
    }
  }

  /**
   *  Reports its  name, if it has been set. If not, returns
   *  a standard value.
   */
  public String toString() {
    String name = myPropertiesTable.get("name"); //$NON-NLS-1$
    if(name!=null) {
      return name;
    }
    String text = this.getClass().getName();
    int index = text.lastIndexOf("."); //$NON-NLS-1$
    if(index>=0) {
      text = text.substring(index+1);
    }
    return "Unnamed element of type "+text; //$NON-NLS-1$
  }

  /**
   * Clears any trace of myself (specially in the group)
   */
  public void destroy() {
    setProperty("parent", null); //$NON-NLS-1$
    if(myProperties!=null) {
      for(int i = 0; i<myProperties.length; i++) {
        if(myProperties[i]!=null) {
          myProperties[i].removeElementListener(this, i);
        }
      }
    }
  }

  // ------------------------------------------------
  // Actions
  // ------------------------------------------------

  /**
   * Defines a generic action that can be invoked from this
   * <code>ControlElement</code>. It is the responsibility of implementing
   * classes to decide what actions types can be invoked and how.
   * <p>
   * If the method field is not a valid method for this target object
   * it will ignore the command (and perhaps print an error message).
   * <p>
   * @return    This same element. This is useful to nest it with
   *    other calls to <code>setProperty</code> or <code>adAction</code>.
   * @param     int _type      The action type
   * @param     Object _target The object whose method will be invoked
   * @param     String _method The method to call in the target object.
   * The method can accept a single CONSTANT parameter, either boolean, int,
   * double or String. See MethodWithOneParameter for more details.
   */
  final public ControlElement addAction(int _type, Object _target, String _method) {
    myActionsList.addElement(new MethodWithOneParameter(_type, _target, _method, null, null, this)); // null = void, null = no2nd action
    return this;
  }

  /**
   * This is an advanced form of addAction that allows for nested actions
   */
  final public ControlElement addAction(int _type, Object _target, String _method, MethodWithOneParameter _secondAction) {
    myActionsList.addElement(new MethodWithOneParameter(_type, _target, _method, null, _secondAction, this)); // null = void
    return this;
  }

  /**
   * Similar to the other addAction but extracts the target from the method,
   * which must be of the form 'target.method:optional parameter', where
   * target has been previously added to the list of targets of the group.
   */
  final public ControlElement addAction(int _type, String _method) {
    // A special entry point for Ejs
    if(getProperty("_ejs_")!=null) {              //$NON-NLS-1$
      _method = "_ejs_.execute(\""+_method+"\")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    Object target = null;
    MethodWithOneParameter secondAction = null;
    String parts[] = MethodWithOneParameter.splitMethodName(_method);
    if(parts==null) {
      System.err.println(getClass().getName()+" : Error! Method <"+_method+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
      return this;
    }
    if(parts[0]==null) {
      parts[0] = "_default_"; //$NON-NLS-1$
    }
    if(myGroup!=null) {
      target = myGroup.getTarget(parts[0]);
      // Only ACTIONs can have a second ACTION
      if((_type==ACTION)&&(getProperty("_ejs_SecondAction_")!=null)&&(myGroup.getTarget("_default_")!=null)) { //$NON-NLS-1$ //$NON-NLS-2$
        secondAction = new MethodWithOneParameter(_type, myGroup.getTarget("_default_"), //$NON-NLS-1$
          getProperty("_ejs_SecondAction_"), null, null, this);                          // null = void , null= no 2nd action //$NON-NLS-1$
      }
    }
    if(target==null) {
      System.err.println(getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
      return this;
    }
    if(parts[2]==null) {
      return addAction(_type, target, parts[1]+"()", secondAction); //$NON-NLS-1$
    }
    return addAction(_type, target, parts[1]+"("+parts[2]+")", secondAction); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Removes an action. If the action does not exists, it does nothing.
   * <p>
   * @param     int _type      The action type
   * @param     Object _target The object whose method will be invoked
   * @param     String _method The method to call in the target object.
   * @see addAction(int,Object,String)
   */
  final public void removeAction(int _type, Object _target, String _method) {
    if(_method==null) {
      return;
    }
    for(Enumeration<MethodWithOneParameter> e = myActionsList.elements(); e.hasMoreElements(); ) {
      MethodWithOneParameter meth = e.nextElement();
      if(meth.equals(_type, _target, _method)) {
        if(!myActionsList.removeElement(meth)) {
          System.err.println(getClass().getName()+": Error! Action "+_method+" not removed"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return;
      }
    }
  }

  /**
   * Similar to removeAction but extracts the target from the method
   */
  final public void removeAction(int _type, String _method) {
    if(_method==null) {
      return;
    }
    // A special entry point for Ejs
    if(getProperty("_ejs_")!=null) {              //$NON-NLS-1$
      _method = "_ejs_.execute(\""+_method+"\")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    String parts[] = MethodWithOneParameter.splitMethodName(_method);
    if(parts==null) {
      System.err.println(getClass().getName()+" : Error! Method <"+_method+"> not removed"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if(parts[0]==null) {
      parts[0] = "_default_"; //$NON-NLS-1$
    }
    Object target = null;
    if(myGroup!=null) {
      target = myGroup.getTarget(parts[0]);
    }
    if(target==null) {
      System.err.println(getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    removeAction(_type, target, parts[1]+"("+parts[2]+")"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Invokes all actions of type ACTION
   */
  final public void invokeActions() {
    invokeActions(ControlElement.ACTION);
  }

  /**
   * Invokes all actions of this BasicControl of a given type
   * @param     int _type  The action type
   */
  final public void invokeActions(int _type) {
    if(myActiveState) {
      for(Enumeration<MethodWithOneParameter> e = myActionsList.elements(); e.hasMoreElements(); ) {
        (e.nextElement()).invoke(_type, this);
      }
    }
    // Next line (for _model actions only!) would make unnecesary the trick of the secondActions!
    // I still use this choice because otherwise a button that calls simulation.step() would update twice
    // if (myGroup!=null && myGroup.getSimulation()!=null) myGroup.getSimulation().update();
  }

  /**
   * Reports changes of internal variables but simulation doesn't update
   * Needed by RadioButtons
   * @param     int _variableIndex the index of the internal variable that changed
   * @param     Value _value the new value for the variable
   */
  final public void variableChangedDoNotUpdate(int _variableIndex, Value _value) {
    // Changing the order of next two sentences is important!!!!
    if((myGroup!=null)&&(myProperties!=null)) {
      myGroup.variableChanged(myProperties[_variableIndex], this, _value);
    }
    if(myActiveState) {
      for(Enumeration<MethodWithOneParameter> e = myActionsList.elements(); e.hasMoreElements(); ) {
        MethodWithOneParameter method = e.nextElement();
        method.invoke(ControlElement.VARIABLE_CHANGED, this);
      }
    }
  }

  /**
   * Reports changes of internal variables
   * @param     int _variableIndex the index of the internal variable that changed
   * @param     Value _value the new value for the variable
   */
  final public void variableChanged(int _variableIndex, Value _value) {
    if(myMethodsForProperties[_variableIndex]!=null) { // AMAVP
      // System.out.println ("Do not update because of method "+myMethodsForProperties[_variableIndex].toString());
      return;
    }
    variableChangedDoNotUpdate(_variableIndex, _value);
    // Next line should apply only to model actions, but these are the only ones expected.
    // Assigning a slider a simulation.pause() would trigger a lot of update()s!
    if((myGroup!=null)&&(myGroup.getSimulation()!=null)) {
      myGroup.getSimulation().update();
    }
  }

  /**
   * Reports changes of more than one internal variables
   * @param     int[] _variableIndexes the indexes of the internal variables that changed
   * @param     Value[] _value the new values for the variables
   */
  final public void variablesChanged(int[] _variableIndex, Value[] _value) {
    boolean doMore = false;
    if((myGroup!=null)&&(myProperties!=null)) {
      for(int i = 0; i<_variableIndex.length; i++) {
        if(myMethodsForProperties[_variableIndex[i]]==null) { // AMAVP
          // System.out.println ("Do not update this one because of method "+myMethodsForProperties[_variableIndex[i]].toString());
          myGroup.variableChanged(myProperties[_variableIndex[i]], this, _value[i]);
          doMore = true;
        }
      }
    }
    if(!doMore) {
      return; // AMAVP Nothing has changed
    }
    if(myActiveState) {
      for(Enumeration<MethodWithOneParameter> e = myActionsList.elements(); e.hasMoreElements(); ) {
        MethodWithOneParameter method = e.nextElement();
        method.invoke(ControlElement.VARIABLE_CHANGED, this);
      }
    }
    if((myGroup!=null)&&(myGroup.getSimulation()!=null)) {
      myGroup.getSimulation().update();
    }
  }

  /**
   * Sets whether a <code>ControlElement</code> actually invokes actions.
   * The default is true.
   * @param   boolean _active Whether it is active
   */
  final public void setActive(boolean _act) {
    myActiveState = _act;
  }

  /**
   * Returns the active status of the <code>ControlElement</code>.
   */
  final public boolean isActive() {
    return myActiveState;
  }

  // ------------------------------------------------
  // Group behavior
  // ------------------------------------------------

  /**
   * Sets the GroupControl in which to operate
   * @param GroupControl _group   The GroupControl
   */
  final public void setGroup(GroupControl _group) {
    myGroup = _group;
  }

  /**
   * Gets the GroupControl in which it operates
   * @return the GroupControl
   */
  final public GroupControl getGroup() {
    return myGroup;
  }

  /**
   * Gets the Simulation in which it runs
   * @return the Simulation
   */
  final public Simulation getSimulation() {
    if(myGroup==null) {
      return null;
    }
    return myGroup.getSimulation();
  }

} // End of Class

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
