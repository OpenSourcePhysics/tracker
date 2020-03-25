/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.ejs.Simulation;
import org.opensourcephysics.ejs.control.swing.ControlContainer;
import org.opensourcephysics.ejs.control.swing.ControlDialog;
import org.opensourcephysics.ejs.control.swing.ControlFrame;
import org.opensourcephysics.ejs.control.swing.ControlWindow;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.ObjectValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A base class to group several ControlElements, connect them
 * to one or more target objects in a unified form, and build a
 * graphic interface with all of them.
 */
// Note for myself: This class is still very much dependent on the actual
// implementation of some of the particular subclasses of ControlElement,
// like ControlContainer, ControlFrame and ControlDialog.
public class GroupControl {
  public static final int DEBUG_NONE = 0;
  public static final int DEBUG_SET_AND_GET = 1;
  public static final int DEBUG_ELEMENTS = 2;
  public static final int DEBUG_CONTROL = 4;
  public static final int DEBUG_CONTROL_VERBOSE = 8;
  public static final int DEBUG_DRAWING = 16;
  public static final int DEBUG_DRAWING_VERBOSE = 32;
  public static final int DEBUG_SYSTEM = 64;
  public static final int DEBUG_SYSTEM_VERBOSE = 128;
  public static final int DEBUG_ALL = 255;
  private int debugLevel = 0;
  private String debugPrefix = "";            //$NON-NLS-1$
  protected String replaceOwnerName = null;
  private java.awt.Frame ownerFrame = null;
  protected java.awt.Frame replaceOwnerFrame = null;
  // private String     defaultPrefix = null;
  private Vector<String> prefixList = new Vector<String>();
  private Simulation mySimulation = null;
  private Hashtable<String, Object> targetTable = new Hashtable<String, Object>();
  private Hashtable<String, ControlElement> elementTable = new Hashtable<String, ControlElement>();
  Hashtable<String, GroupVariable> variableTable = new Hashtable<String, GroupVariable>();
  private Vector<ControlElement> elementList = new Vector<ControlElement>();
  private Vector<ControlElement> updateList = new Vector<ControlElement>();
  GroupVariable methodTriggerVariable = null; // AMAVP (See Note in ControlElement)

  /**
   * The default constructor.
   */
  public GroupControl() {
    debugPrefix = this.getClass().getName();
    int index = debugPrefix.lastIndexOf("."); //$NON-NLS-1$
    if(index>=0) {
      debugPrefix = debugPrefix.substring(index+1);
    }
    appendPrefixPath("org.opensourcephysics.ejs.control.swing.Control");      //$NON-NLS-1$
    appendPrefixPath("org.opensourcephysics.ejs.control.drawables.Control");  //$NON-NLS-1$
    appendPrefixPath("org.opensourcephysics.ejs.control.displayejs.Control"); //$NON-NLS-1$
    setValue(ControlElement.METHOD_TRIGGER, new BooleanValue(false));         // AMAVP
    methodTriggerVariable = variableTable.get(ControlElement.METHOD_TRIGGER); // AMAVP
  }

  /**
   * The constructor.
   * @param _target
   */
  public GroupControl(Object _target) {
    this();
    addTarget("_default_", _target); //$NON-NLS-1$
    if(_target instanceof Simulation) {
      setSimulation((Simulation) _target);
    }
  }

  /**
   * A specialized constructor for Ejs use.
   * This adds elements to it in the ususal way, but replaces a Frame element
   * of a given name by the prescribed frame.
   * @param _simulation
   * @param _replaceName
   * @param _replaceOwnerFrame
   */
  public GroupControl(Object _simulation, String _replaceName, java.awt.Frame _replaceOwnerFrame) {
    this(_simulation);
    replaceOwnerFrame(_replaceName, _replaceOwnerFrame);
  }

  // ------------------------------------------------
  // Preliminary things
  // ------------------------------------------------

  /**
   * Sets the owner frame for all subsequent Dialogs
   * @param     Frame _frame  The frame that should own next Dialogs
   *   (if there are Dialogs in this group)
   */
  public void setOwnerFrame(java.awt.Frame _frame) {
    ownerFrame = _frame;
  }

  /**
   * Returns the owner frame for all subsequent Dialogs
   */
  public java.awt.Frame getOwnerFrame() {
    return ownerFrame;
  }

  public void replaceOwnerFrame(String _replaceName, java.awt.Frame _replaceOwnerFrame) {
    replaceOwnerName = _replaceName;
    replaceOwnerFrame = _replaceOwnerFrame;
  }

  /**
   * Returns the name of the replacement for the owner frame for all subsequent Dialogs
   */
  public String getReplaceOwnerName() {
    return replaceOwnerName;
  }

  /**
   * Returns the replacement for the owner frame for all subsequent Dialogs
   */
  public java.awt.Frame getReplaceOwnerFrame() {
    return replaceOwnerFrame;
  }

  /**
   * Clears the list of default package name for unqualified elements
   */
  public void clearPrefixPath() {
    prefixList.clear();
  }

  /**
   * Adds a prefix 'path' for unqualified elements. Hence "Frame"
   * becomes _prefix + "Frame". The default list includes
   * "org.opensourcephysics.ejs.control.swing.Control" and
   * "org.opensourcephysics.ejs.control.drawables.Control"
   * @param     String _prefix  The prefix to be added to list
   */
  public void appendPrefixPath(String _prefix) {
    prefixList.add(_prefix);
  }

  /**
   * Returns the list (actually, a vector) of prefix
   */
  public Vector<String> getDefaultPrefixList() {
    return prefixList;
  }

  /**
   * Sets the simulation under which the control is running
   * This is used to up date the simulation whenever an Element changes a
   * variable (See variableChanged in ControlElement)
   * @param     Simulation _sim  The simulation
   */
  public void setSimulation(Simulation _sim) {
    mySimulation = _sim;
  }

  /**
   * Returns the simulation under which the control is running
   * This is used to up date the simulation whenever an Element changes a
   * variable (See variableChanged in ControlElement
   */
  public Simulation getSimulation() {
    return mySimulation;
  }

  /**
   * Sets the debug level
   * @param     int _level  The minimim level that should
   * produce debug mesagges. Must be one of
   * DEBUG_NONE, DEBUG_SET_AND_GET, DEBUG_ELEMENTS,
   * DEBUG_ALL=255
   */
  public void setDebugLevel(int _level) {
    debugLevel = _level;
  }

  /**
   * Returns the current debug level
   * @return     The actual minimim level that produces debug messages
   */
  public int getDebugLevel() {
    return debugLevel;
  }

  // ------------------------------------------------
  // Targets
  // ------------------------------------------------

  /**
   * Returns one of the registered target objects
   * @param   String _name  The name given to the target when it was added
   */
  public Object getTarget(String _name) {
    return targetTable.get(_name);
  }

  /**
   * Adds an object to be controlled. Actions can then refer to methods of the
   * form 'name.method:parameter'
   * @param   String _name  A name to refer to the target
   * @param   Object _target  A target object
   */
  public void addTarget(String _name, Object _target) {
    targetTable.put(_name, _target);
  }

  /**
   * Removes a target object
   */
  public void removeTarget(String _name) {
    targetTable.remove(_name);
  }

  // --------------------------------------------------------
  // Dealing with group variables
  // --------------------------------------------------------

  /**
   * Sets the group value for a variable. This includes the value in all
   * the elements of this group that are registered to this variable name.
   * @param   String _name  The variable name
   * @param   Value _value  The value as a <code>Value</code> object
   */
  public void setValue(String _name, Value _value) {
    // I have commented out the debug messages to improve performance (??).
    // Uncomment them if you want further debug messages
    // if ((debugLevel & DEBUG_SET_AND_GET)>0)
    // System.out.print(debugPrefix+": Setting value of <"+_name+"> to <"+_value+"> ...");
    // if (_name==null) return;
    GroupVariable variable = variableTable.get(_name);
    if(variable==null) {
      variable = new GroupVariable(_name, _value);
      variableTable.put(_name, variable);
      // if ((debugLevel & DEBUG_SET_AND_GET)>0) System.out.println("Created <"+_name+"> with value <"+_value+">");
    } else {
      variable.setValue(_value);
      variable.propagateValue(null);
      // if ((debugLevel & DEBUG_SET_AND_GET)>0) System.out.println("<"+_name+"> set to <"+_value+">");
    }
  }

  /**
   * Returns the group value of a variable.
   * @return the <code>Value</code> object of the variable. If the
   *         variable has never been set, it returns <b>null</b>.
   * @param  String _name  The variable name
   */
  public Value getValue(String _name) {
    // I have commented the debug messages out to improve performance (??).
    // Uncomment them if you want further debug messages
    // if ((debugLevel & DEBUG_SET_AND_GET)>0)
    // System.out.print(debugPrefix+": Getting value of <"+_name+ ">...");
    GroupVariable variable = variableTable.get(_name);
    if(variable==null) {
      // if ((debugLevel & DEBUG_SET_AND_GET)>0) System.out.println("<"+_name+"> has not been set!");
      return null;
    }
    // if ((debugLevel & DEBUG_SET_AND_GET)>0) System.out.println("<"+_name+"> found with value = <"+variable.value+">");
    return variable.getValue();
  }

  /**
   * Associates an element internal value with a variable name. Later on,
   * when the user sets the value for this variable, either
   * programmatically or by interaction with a given element,
   * all registered elements will be informed of the change.
   * Invoked by ControlElements when processing the 'variable'
   * property. Not to be used directly by users.
   * @param     String _name  The name of the variable
   * @param     ControlElement _element  The element to be registered
   * @param     int _index  An indentifier for the element internal value
   * @param     Value _value The initial value if the variable doesn't already exist
   */
  public GroupVariable registerVariable(String _name, ControlElement _element, int _index, Value _value) {
    // if ((debugLevel & DEBUG_SET_AND_GET)>0)
    // System.out.print(debugPrefix+": Registering variable <"+_name
    // + "> for element <"+_element+">  at index "+_index+" with value = "+_value);
    if(_name==null) {
      return null;
    }
    GroupVariable variable = variableTable.get(_name);
    if(variable==null) {
      variable = new GroupVariable(_name, _value);
      variableTable.put(_name, variable);
      if((debugLevel&DEBUG_SET_AND_GET)>0) {
        System.out.print("   Created new variable <"+_name+"> with value = <"+_value+"> ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
    // else variable.setValue(_value); // Commented means that the element takes
    // the actual value, whatever it is
    // System.out.println("   variable <"+_name+"> has value = <"+getValue(_name)+"> ...");
    if((debugLevel&DEBUG_SET_AND_GET)>0) {
      System.out.println("   Variable <"+_name+"> registered for element <"+_element+">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    variable.addElementListener(_element, _index);
    variable.propagateValue(null); // null implies that the element takes the actual value
    return variable;
  }

  /**
   * Tells whether a variable is associated to any element.
   * Invoked by EjsControl's method 'setValue/getValue'.
   * Not to be used directly by users.
   * @param     ControlElement _element  The element to be included
   * @param     String _variable  The variable name
   */
  public boolean isVariableRegistered(String _name) {
    if(_name==null) {
      return false;
    }
    return(variableTable.get(_name)!=null);
  }

  /**
   * Invoked by ControlElements when their internal variables change.
   * Not be used directly by users.
   */
  public void variableChanged(GroupVariable _variable, ControlElement _element, Value _value) {
    if(_variable==null) {
      return;
    }
    _variable.setValue(_value);
    _variable.propagateValue(_element);
    _variable.invokeListeners(_element); // Call any registered listener
  }

  public void addListener(String _name, String _method) {
    addListener(_name, _method, null);
  }

  /**
   * Instructs the group to invoke a method (with an optional parameter) when a
   * variable changes.
   * @param  String _name   The name of the variable that may change
   * @param  String _method  The method that should be called in the controlled
   * @param Object _anObject the object to pass in the special case the method is method(#CONTROL#)
   * object.
   */
  public void addListener(String _name, String _method, Object _anObject) {
    if((debugLevel&DEBUG_SET_AND_GET)>0) {
      System.out.print(debugPrefix+": Adding listener for variable <"+_name+"> to <"+_method+"> ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    if(_name==null) {
      return;
    }
    String[] parts = MethodWithOneParameter.splitMethodName(_method);
    if(parts==null) {
      System.err.println(getClass().getName()+" : Error! Listener <"+_method+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if(parts[0]==null) {
      parts[0] = "_default_"; //$NON-NLS-1$
    }
    Object target = getTarget(parts[0]);
    if(target==null) {
      System.err.println(getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    } else if((debugLevel&DEBUG_SET_AND_GET)>0) {
      System.out.print(debugPrefix+": Target <"+parts[0]+"> found. Method is <"+_method+"> ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    GroupVariable variable = variableTable.get(_name);
    if(variable==null) {
      variable = new GroupVariable(_name, doubleValue);
      variableTable.put(_name, variable);
      if((debugLevel&DEBUG_SET_AND_GET)>0) {
        System.out.print("   Created new variable <"+_name+"> for listener <"+_method+"> ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
    if(parts[2]==null) {
      variable.addListener(target, parts[1]+"()", _anObject);             //$NON-NLS-1$
    } else {
      variable.addListener(target, parts[1]+"("+parts[2]+")", _anObject); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  // --------------------------------------------------------
  // Adding and removing control elements
  // --------------------------------------------------------

  /**
   * Renaming a ControlElement
   * @param     String _name   The new name for the element.
   */
  public void rename(ControlElement _element, String _name) {
    String oldName = _element.getProperty("name"); //$NON-NLS-1$
    if(oldName!=null) {
      elementTable.remove(oldName);
    }
    if(_name!=null) {
      elementTable.put(_name, _element);
    }
  }

  /**
   * Creates a new ControlElement with a given name
   * This is a special feature that is used by LauncherApplet, so that
   * if the name coincides with a given one, a Frame becomes a Panel,
   * so that it can be captured!
   * @param     String _type   The class name of the new element.
   * @param     String _name   The name of the new element.
   */
  final public ControlElement addNamed(String _type, String _name) {
    String propertyList = "name="+_name; //$NON-NLS-1$
    if((replaceOwnerName==null)||!replaceOwnerName.equals(_name)) {
      return addObject(null, _type, propertyList);
    }
    if(_type.endsWith("ControlFrame")||_type.endsWith("ControlDrawingFrame")) {                     //$NON-NLS-1$ //$NON-NLS-2$
      setOwnerFrame(replaceOwnerFrame);
      return addObject(null, "org.opensourcephysics.ejs.control.swing.ControlPanel", propertyList); //$NON-NLS-1$
    }
    return addObject(null, _type, propertyList);
  }

  /**
   * Creates a new ControlElement
   * @param     String _type   The class name of the new element.
   * If it is not qualified, then it is given the prefix (see above)
   */
  final public ControlElement add(String _type) {
    return addObject(null, _type, null);
  }

  /**
   * Creates a new ControlElement and gives it a name
   * @param     String _type   The class name of the new element.
   * If it is not qualified, then it is given the default prefix (see above)
   * @param     String _propertyList A list of properties and Values
   *            to be set (see ControlElement.setProperties())
   */
  final public ControlElement add(String _type, String _propertyList) {
    return addObject(null, _type, _propertyList);
  }

  /**
   * Creates a new ControlElement that wrapps an existing object
   * If the object is not of the right class it will print a warning
   * and ignore the object provided.
   * @param     Object _object   The element to be wrapped
   * @param     String _type   The class name of the new element.
   * If it is not qualified, then it is given the prefix (see above)
   */
  final public ControlElement addObject(Object _object, String _type) {
    return addObject(_object, _type, null);
  }

  /**
   * Creates a new ControlElement that wrapps an existing object
   * If the object is not of the right class it will print a warning
   * and ignore the object provided.
   * @param     Object _object   The element to be wrapped
   * @param     String _type   The class name of the new element.
   *            If it is not qualified, then it is given the prefix (see above)
   * @param     String _propertyList A list of properties and Values
   *            to be set (see ControlElement.setProperties())
   */
  public ControlElement addObject(Object _object, String _type, String _propertyList) {
    ControlElement element = null;
    if((debugLevel&DEBUG_ELEMENTS)>0) {
      System.err.println(this.getClass().getName()+" Adding element of type <"+_type+"> with properties <"+_propertyList+">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if(_object!=null) {
        System.err.println(this.getClass().getName()+" using element "+_object); //$NON-NLS-1$
      }
    }
    if(_type.indexOf(".")<0) {                                             //$NON-NLS-1$
      for(Enumeration<String> e = prefixList.elements(); e.hasMoreElements()&&(element==null); ) {
        element = instantiateClass(_object, e.nextElement()+_type, false); // Silently
      }
    }
    if(element==null) {
      element = instantiateClass(_object, _type, true);
    }
    if(element==null) {
      return null;
    }
    // Frames become automatically ownerFrames for subsequent Dialogs
    if(element instanceof ControlFrame) {
      setOwnerFrame((java.awt.Frame) ((ControlFrame) element).getComponent());
    }
    // Use ownerFrame for Dialogs, if there is any
    if((element instanceof ControlDialog)&&(ownerFrame!=null)) {
      ((javax.swing.JDialog) ((ControlDialog) element).getComponent()).dispose();
      ((ControlDialog) element).replaceVisual(ownerFrame);
    }
    element.setGroup(this);
    elementList.add(element);
    if(element instanceof NeedsUpdate) {
      updateList.add(element);
    }
    if((debugLevel&DEBUG_ELEMENTS)>0) {
      System.err.println(this.getClass().getName()+" Setting properties to <"+_propertyList+">"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if(_propertyList!=null) {
      element.setProperties(_propertyList);
    }
    if(element instanceof ControlWindow) {       // Make windows visible by default
      if(element.getProperty("visible")==null) { //$NON-NLS-1$
        element.setProperty("visible", "true");  //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    return element;
  }

  private ControlElement instantiateClass(Object _object, String _classname, boolean _verbose) {
    if((debugLevel&DEBUG_ELEMENTS)>0) {
      System.err.println(this.getClass().getName()+": Trying to instantiate element of class "+_classname); //$NON-NLS-1$
      if(_object!=null) {
        System.err.println(this.getClass().getName()+" using element "+_object);                            //$NON-NLS-1$
      }
    }
    // if (_object!=null) // Even if the object to wrapp is null!
    try {
      Class<?> aClass = Class.forName(_classname);
      Class<?>[] c = {Object.class};
      Object[] o = {_object};
      java.lang.reflect.Constructor<?> constructor = aClass.getDeclaredConstructor(c);
      return(ControlElement) constructor.newInstance(o);
    } catch(Exception _exc) {
      if(_verbose) {
        _exc.printStackTrace();
        return null;
      } // Do not exit if in silent mode
    }
    // Try once more with the default constructor
    try {
      Class<?> aClass = Class.forName(_classname);
      return(ControlElement) aClass.newInstance();
    } catch(Exception _exc) {
      if(_verbose) {
        _exc.printStackTrace();
      }
      return null;
    }
  }

  /**
   * Returns a control element by name
   * @return    the ControlElement if found, null otherwise.
   * @param     String _name  The name of the control element
   */
  public ControlElement getElement(String _name) {
    if(_name==null) {
      return null;
    }
    ControlElement element = elementTable.get(_name);
    if(element==null) {
      // Commented out to improve performance
      // if ((debugLevel & DEBUG_ELEMENTS)>0) {
      // System.err.println(this.getClass().getName()+" Warning!: control element <"+_name+"> not found!");
      // System.err.println("  List of named elements follows: ");
      // for (Enumeration e = elementTable.elements() ; e.hasMoreElements() ;) {
      // element = (ControlElement) e.nextElement();
      // String name = element.getProperty("name");
      // if (name!=null) System.err.println("    "+name+"(class is "+element.getClass().getName()+")");
      // }
      // }
      return null;
    }
    return element;
  }

  // For backwards compatibility
  public ControlElement getControl(String _name) {
    return getElement(_name);
  }

  /**
   * Returns the visual of a control element by name
   * @return    the java.awt.Component visual of the element if found, null otherwise.
   * @param     String _name  The name of the control element
   */
  public java.awt.Component getVisual(String _name) {
    ControlElement element = getElement(_name);
    if(element==null) {
      return null;
    }
    return element.getVisual();
  }

  /**
   * Returns the component of a control element by name
   * @return    the java.awt.Component component of the element if found, null otherwise.
   * @param     String _name  The name of the control element
   */
  public java.awt.Component getComponent(String _name) {
    ControlElement element = getElement(_name);
    if(element==null) {
      return null;
    }
    return element.getComponent();
  }

  /**
   * Returns the container of a control element by name
   * @return    the java.awt.Container visual of the element if found, and the
   * element is a container, null otherwise.
   * @param     String _name  The name of the control element
   */
  public java.awt.Container getContainer(String _name) {
    ControlElement element = getElement(_name);
    if(element instanceof ControlContainer) {
      return((ControlContainer) element).getContainer();
    }
    return null;
  }

  /**
   * Completely destroy a ControlElement by name
   * @param     String _name  The name of the ControlElement to be destroyed
   */
  public void destroy(String _name) {
    destroy(getElement(_name), true);
  }

  /**
   * Completely destroy a ControlElement
   * @param     ControlElement _element The ControlElement to be destroyed
   */
  public void destroy(ControlElement _element) {
    destroy(_element, true);
  }

  /**
   * Reset all elements
   */
  public void reset() {
    for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
      // ControlElement ele = (ControlElement ) e.nextElement();
      // System.out.println ("Resettimng element "+ele);
      // ele.reset();
      e.nextElement().reset();
    }
  }

  /**
   * Initialize all elements
   */
  public void initialize() {
    for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
      e.nextElement().initialize();
    }
  }

  /**
   * Refresh all elements
   */
  public void update() {
    methodTriggerVariable.propagateValue(null); // AMAVP (See Note in ControlElement)
    // setValue (ControlElement.METHOD_TRIGGER,true);
    // for (Enumeration e=elementList.elements(); e.hasMoreElements(); ) ((ControlElement) e.nextElement()).update();
    for(Enumeration<ControlElement> e = updateList.elements(); e.hasMoreElements(); ) {
      ((NeedsUpdate) e.nextElement()).update();
    }
  }

  /**
   * Set the active state of all elements
   */
  public void setActive(boolean _active) {
    for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
      e.nextElement().setActive(_active);
    }
  }

  /**
   * Clears all variables
   */
  public void clearVariables() {
    variableTable.clear();
  }

  /**
   * Destroy all elements
   */
  public void clear() {
    variableTable.clear();
    setOwnerFrame(null);
    for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
      ControlElement element = e.nextElement();
      String parent = element.getProperty("parent"); //$NON-NLS-1$
      if(parent==null) {
        destroy(element, false);
      }
    }
    if((debugLevel&DEBUG_ELEMENTS)>0) {
      System.err.println(this.getClass().getName()+" Warning!: All element were destroyed!");        //$NON-NLS-1$
      System.err.println("  List of remaining elements follows: ");                                  //$NON-NLS-1$
      for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
        ControlElement element = e.nextElement();
        System.err.println("    "+element.toString()+"(class is "+element.getClass().getName()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
  }

  private void destroy(ControlElement _element, boolean _informMyParent) {
    if(_element==null) {
      return;
    }
    if(_informMyParent) {
      ControlElement parent = getElement(_element.getProperty("parent")); //$NON-NLS-1$
      if(parent!=null) {
        if(parent instanceof ControlContainer) {
          ((ControlContainer) parent).remove(_element);
        }
      } else {                                                            // It may have been added to a container programmatically
        java.awt.Container cont = _element.getComponent().getParent();
        if(cont!=null) {
          cont.remove(_element.getComponent());
          cont.validate();
          cont.repaint();
        }
      }
    }
    _element.variablePropertiesClear();
    String name = _element.getProperty("name"); //$NON-NLS-1$
    if(name!=null) {
      elementTable.remove(name);
    }
    elementList.remove(_element);
    if(_element instanceof NeedsUpdate) {
      updateList.remove(_element);
    }
    if(_element instanceof ControlContainer) {
      for(Enumeration<ControlElement> e = ((ControlContainer) _element).getChildren().elements(); e.hasMoreElements(); ) {
        ControlElement child = e.nextElement();
        destroy(child, false);
      }
    }
    if(_element instanceof ControlWindow) {
      ((ControlWindow) _element).dispose();
    }
  }

  /**
   * Returns the top-level ancestor of an element (either the
   * containing Window or Applet), or null if the element has not
   * been added to any container. If no element name is provided, the
   * first control element whose component is a Window is returned.
   * @return    the Container if found, null otherwise.
   * @param     String _name  The name of the control element
   */
  public java.awt.Container getTopLevelAncestor(String _name) {
    if(_name!=null) {
      ControlElement element = getElement(_name);
      java.awt.Component comp = element.getComponent();
      if(comp instanceof javax.swing.JComponent) {
        return((javax.swing.JComponent) comp).getTopLevelAncestor();
      }
    } else {
      for(Enumeration<ControlElement> e = elementList.elements(); e.hasMoreElements(); ) {
        ControlElement element = e.nextElement();
        java.awt.Component comp = element.getComponent();
        if(comp instanceof java.awt.Window) {
          return(java.awt.Window) comp;
        }
      }
    }
    return null;
  }

  // -------------------------------------------------------
  // Convenience methods
  // Half way to org.opensourcephysics.controls.Control
  // -------------------------------------------------------
  // For the custom methods
  private BooleanValue booleanValue = new BooleanValue(false);
  private IntegerValue integerValue = new IntegerValue(0);
  private DoubleValue doubleValue = new DoubleValue(0.0);
  private StringValue stringValue = new StringValue(""); //$NON-NLS-1$
  private ObjectValue objectValue = new ObjectValue(null);

  // --------- Setting different types of values ------

  /**
   * A convenience method to set a value to a boolean
   * @param _name
   * @param _value
   */
  public void setValue(String _name, boolean _value) {
    booleanValue.value = _value;
    setValue(_name, booleanValue);
  }

  /**
   * A convenience method to set a value to an int
   * @param _name
   * @param _value
   */
  public void setValue(String _name, int _value) {
    integerValue.value = _value;
    setValue(_name, integerValue);
  }

  /**
   * A convenience method to set a value to a double
   * @param _name
   * @param _value
   */
  public void setValue(String _name, double _value) {
    doubleValue.value = _value;
    setValue(_name, doubleValue);
  }

  /**
   * A convenience method to set a value to a String
   * @param _name
   * @param _value
   */
  public void setValue(String _name, String _value) {
    stringValue.value = _value;
    setValue(_name, stringValue);
  }

  /**
   * A convenience method to set a value to any Object
   * @param _name
   * @param _value
   */
  public void setValue(String _name, Object _value) {
    if(_value instanceof String) {
      setValue(_name, (String) _value);
    } else {
      objectValue.value = _value;
      setValue(_name, objectValue);
    }
  }

  // --------- Getting different types of values ------

  /**
   * A convenience method to get a value as a boolean
   * @param _name
   */
  public boolean getBoolean(String _name) {
    Value value = getValue(_name);
    if(value==null) {
      return false;
    }
    return value.getBoolean();
  }

  /**
   * A convenience method to get a value as an int
   * @param _name
   */
  public int getInt(String _name) {
    Value value = getValue(_name);
    if(value==null) {
      return 0;
    }
    return value.getInteger();
  }

  /**
   * A convenience method to get a value as a double
   * @param _name
   */
  public double getDouble(String _name) {
    Value value = getValue(_name);
    if(value==null) {
      return 0.0;
    }
    return value.getDouble();
  }

  /**
   * A convenience method to get a value as a String
   * @param _name
   */
  public String getString(String _name) {
    Value value = getValue(_name);
    if(value==null) {
      return ""; //$NON-NLS-1$
    }
    return value.getString();
  }

  /**
   * A convenience method to get a value as an Object
   * @param _name
   */
  public Object getObject(String _name) {
    Value value = getValue(_name);
    if(value==null) {
      return null;
    }
    return value.getObject();
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new GroupControlLoader();
  }

  /**
   * A class to save and load XML data for GroupControl.
   */
  static class GroupControlLoader implements XML.ObjectLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      GroupControl groupcontrol = (GroupControl) obj;
      Hashtable<String, GroupVariable> table = groupcontrol.variableTable;
      Iterator<String> it = table.keySet().iterator();
      while(it.hasNext()) {
        String name = it.next();
        if(!name.startsWith("_")) { // don't save ejs internal variables //$NON-NLS-1$
          if(groupcontrol.getObject(name).getClass().isArray()) {
            control.setValue(name, groupcontrol.getObject(name));
          } else {
            control.setValue(name, groupcontrol.getString(name));
          }
        }
      }
    }

    /**
     * Creates an object using data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new GroupControl(null);
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      GroupControl groupcontrol = (GroupControl) obj;
      Hashtable<String, GroupVariable> table = groupcontrol.variableTable;
      Iterator<String> it = table.keySet().iterator();
      while(it.hasNext()) {
        String name = it.next();
        if(control.getString(name)!=null) {
          groupcontrol.setValue(name, control.getString(name));
        } else if(control.getObject(name)!=null) {
          Object namedObj = control.getObject(name);
          if(namedObj instanceof java.awt.Color) {
            groupcontrol.setValue(name, namedObj);
          }
          if(namedObj.getClass().isArray()) {
            groupcontrol.setValue(name, namedObj);
          } else {
            groupcontrol.setValue(name, namedObj.toString());
          }
        }
      }
      return obj;
    }

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
