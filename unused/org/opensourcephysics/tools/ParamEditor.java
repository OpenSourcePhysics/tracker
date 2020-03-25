/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.util.ArrayList;
import java.util.List;

import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DatasetManager;

/**
 * A FunctionEditor for Parameters.
 *
 * @author Douglas Brown
 */
public class ParamEditor extends FunctionEditor {
  protected double[] paramValues = new double[0];
  private DatasetManager data;
  private FunctionEditor[] functionEditors;
  protected String[] paramDescriptions = new String[0];

  /**
   * Default constructor
   */
  public ParamEditor() {
    super();
    paramEditor = this;
  }

  /**
   * Constructor using a DatasetManager to define initial parameters
   *
   * @param input the DatasetManager
   */
  public ParamEditor(DatasetManager input) {
    this();
    data = input;
    loadParametersFromData();
  }

  /**
   * Gets an array containing copies of the current parameters.
   *
   * @return an array of Parameters
   */
  public Parameter[] getParameters() {
    Parameter[] params = new Parameter[objects.size()];
    for(int i = 0; i<objects.size(); i++) {
      Parameter next = (Parameter) objects.get(i);
      params[i] = new Parameter(next.paramName, next.expression);
      params[i].setExpressionEditable(next.isExpressionEditable());
      params[i].setNameEditable(next.isNameEditable());
      params[i].setDescription(next.getDescription());
      params[i].value = next.value;
    }
    return params;
  }

  /**
   * Replaces the current parameters with new ones.
   *
   * @param params an array of Parameters
   */
  public void setParameters(Parameter[] params) {
    List<Object> list = new ArrayList<Object>();
    for(int i = 0; i<params.length; i++) {
      list.add(params[i]);
    }
    setObjects(list);
  }

  /**
   * Sets the function editors that use these parameters.
   *
   * @param editors an array of FunctionEditors
   */
  public void setFunctionEditors(FunctionEditor[] editors) {
    functionEditors = editors;
    if (functionEditors==null) {
    	paramEditor = null;
    }
  }

  /**
   * Gets the current parameter values.
   *
   * @return an array of values
   */
  public double[] getValues() {
    return paramValues;
  }

  /**
   * Gets the current parameter descriptions.
   *
   * @return an array of descriptions
   */
  public String[] getDescriptions() {
    return paramDescriptions;
  }

  /**
   * Returns the name of the object.
   *
   * @param obj the object
   * @return the name
   */
  public String getName(Object obj) {
    return(obj==null) ? null : ((Parameter) obj).paramName;
  }

  /**
   * Returns the expression of the object.
   *
   * @param obj the object
   * @return the expression
   */
  public String getExpression(Object obj) {
    return(obj==null) ? null : ((Parameter) obj).expression;
  }

  /**
   * Returns the description of the object.
   *
   * @param obj the object
   * @return the description
   */
  public String getDescription(Object obj) {
    return (obj==null)? null: ((Parameter)obj).getDescription();
  }

  /**
   * Sets the description of an object.
   *
   * @param obj the object
   * @param desc the description
   */
  public void setDescription(Object obj, String desc) {
  	if (obj!=null) {
  		Parameter p = (Parameter)obj;
    	if (desc!=null && desc.trim().equals("")) { //$NON-NLS-1$
    		desc = null;
    	}
  		p.setDescription(desc);
//      for(int i = 0; i<objects.size(); i++) {
//        p = (Parameter) objects.get(i);
//        paramValues[i] = p.getValue();
//        paramDescriptions[i] = p.getDescription();
//      }
  		super.setDescription(obj, desc);
  	}
  }

  /**
   * Sets the description of the named parameter, if any.
   *
   * @param name the name
   * @param description the description
   */
  public void setDescription(String name, String description) {
    for(Object obj: objects) {
      Parameter param = (Parameter)obj;
      if (param.getName().equals(name)) {
      	setDescription(obj, description);
      	break;
      }
    }
  }

  /**
   * Returns a tooltip for the object.
   *
   * @param obj the object
   * @return the tooltip
   */
  public String getTooltip(Object obj) {
    String s = ((Parameter) obj).getDescription();
    if (s==null) {
      s = ToolsRes.getString("ParamEditor.Table.Cell.Name.Tooltip"); //$NON-NLS-1$
    	s += " ("+ToolsRes.getString("FunctionEditor.Tooltip.HowToEdit")+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return s;
  }

  /**
   * Determines if an object's name is editable.
   *
   * @param obj the object
   * @return true if the name is editable
   */
  public boolean isNameEditable(Object obj) {
    return((Parameter) obj).isNameEditable();
  }

  /**
   * Determines if an object's expression is editable.
   *
   * @param obj the object
   * @return true if the expression is editable
   */
  public boolean isExpressionEditable(Object obj) {
    return((Parameter) obj).isExpressionEditable();
  }

  /**
   * Evaluates an object.
   */
  protected void evaluateObject(Object obj) {
    Parameter p = (Parameter) obj;
    p.evaluate(objects);
  }

  /**
   * Evaluates parameters that depend on the named parameter.
   *
   * @param seed the independent parameter
   * @return a list of evaluated dependent parameters
   */
  public ArrayList<Parameter> evaluateDependents(Parameter seed) {
    ArrayList<Parameter> temp = new ArrayList<Parameter>();
    ArrayList<Parameter> toRemove = new ArrayList<Parameter>();
    for(int i = 0; i<evaluate.size(); i++) {
      Parameter param = (Parameter) evaluate.get(i);
      if(param.paramName.equals(seed.paramName)) {
        temp.add(seed);
        toRemove.add(seed);
        for(int j = i+1; j<evaluate.size(); j++) {
          Parameter p = (Parameter) evaluate.get(j);
          temp.add(new Parameter(p.paramName, p.expression));
        }
        // evaluate temp list
        for(int j = 0; j<temp.size(); j++) {
          // for each parameter, evaluate and set paramValues element
          Parameter p = temp.get(j);
          p.evaluate(temp);
          if(getReferences(p.getName(), null).isEmpty()) {
            toRemove.add(p);
          }
        }
        temp.removeAll(toRemove);
        return temp;
      }
    }
    return temp;
  }

  /**
   * Evaluates all current objects.
   */
  public void evaluateAll() {
    super.evaluateAll();
    if(this.getClass()!=ParamEditor.class) {
      return;
    }
    if(paramValues.length!=objects.size()) {
      paramValues = new double[objects.size()];
    }
    for(int i = 0; i<evaluate.size(); i++) {
      Parameter p = (Parameter) evaluate.get(i);
      p.evaluate(objects);
    }
    if(paramDescriptions.length!=objects.size()) {
    	paramDescriptions = new String[objects.size()];
    }
    for(int i = 0; i<objects.size(); i++) {
      Parameter p = (Parameter) objects.get(i);
      paramValues[i] = p.getValue();
      paramDescriptions[i] = p.getDescription();
    }
  }
  
  /**
   * Returns true if a name is already in use.
   *
   * @param obj the object (may be null)
   * @param name the proposed name for the object
   * @return true if duplicate
   */
  protected boolean isDisallowedName(Object obj, String name) {
    boolean disallowed = super.isDisallowedName(obj, name);
    // added following line so leaving object name unchanged is not disallowed
    if (!disallowed && obj!=null && getName(obj).equals(name)) return false;
    if(functionEditors!=null) {
      for(int i = 0; i<functionEditors.length; i++) {
        disallowed = disallowed||functionEditors[i].isDisallowedName(null, name);
      }
    }
    return disallowed;
  }

  @Override
  protected boolean isValidExpression(String expression) {
  	Parameter p = new Parameter("xxzz", expression); //$NON-NLS-1$
  	return !Double.isNaN(p.evaluate(objects));
  }

  /**
   * Pastes the clipboard contents.
   */
  protected void paste() {
    XMLControl[] controls = getClipboardContents();
    if(controls==null) {
      return;
    }
    for(int i = 0; i<controls.length; i++) {
      // create a new object
      Parameter param = (Parameter) controls[i].loadObject(null);
      param.setNameEditable(true);
      param.setExpressionEditable(true);
      addObject(param, true);
    }
    evaluateAll();
  }

  /**
   * Returns true if the object expression is invalid.
   */
  protected boolean isInvalidExpression(Object obj) {
    return Double.isNaN(((Parameter) obj).getValue());
  }

  /**
   * Creates an object with specified name and expression.
   * This always returns a new Parameter but copies the editable properties.
   *
   * @param name the name
   * @param expression the expression
   * @param obj ignored
   * @return the object
   */
  protected Object createObject(String name, String expression, Object obj) {
    Parameter original = (Parameter) obj;
    if((original!=null)&&original.paramName.equals(name)&&original.expression.equals(expression)) {
      return original;
    }
    Parameter p = new Parameter(name, expression);
    if(original!=null) {
      p.setExpressionEditable(original.isExpressionEditable());
      p.setNameEditable(original.isNameEditable());
      p.setDescription(original.getDescription());
    }
    return p;
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    super.refreshGUI();
    newButton.setToolTipText(ToolsRes.getString("ParamEditor.Button.New.Tooltip"));              //$NON-NLS-1$
    titledBorder.setTitle(ToolsRes.getString("ParamEditor.Border.Title")); //$NON-NLS-1$
  }

  /**
   * Loads parameters from the current datasetManager.
   */
  public void loadParametersFromData() {
    if(data==null) return;
    String[] names = data.getConstantNames();
    for (String name: names) {
    	String expression = data.getConstantExpression(name);
      Parameter p = (Parameter) getObject(name);
      if(p==null) {
        p = new Parameter(name, expression);
        p.setDescription(data.getConstantDescription(name));
        addObject(p, false);
      }
      else {
      	setExpression(name, expression, false);
      }
    }
  }

  /**
   * Refreshes the parameters associated with a user function.
   */
  protected void refreshParametersFromFunction(UserFunction f) {
    // identify values that have changed
    for(int i = 0; i<f.getParameterCount(); i++) {
      String name = f.getParameterName(i);
      String val = String.valueOf(f.getParameterValue(i));
      Parameter p = (Parameter) getObject(name);
      if(p==null) {
        p = new Parameter(name, val);
        p.setNameEditable(false);
        p.setExpressionEditable(false);
        addObject(p, false);
      }
      // change parameter value
      else {
        setExpression(name, val, false);
      }
    }
  }

  /**
   * Returns the default name for newly created objects.
   */
  protected String getDefaultName() {
    return ToolsRes.getString("ParamEditor.New.Name.Default"); //$NON-NLS-1$
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
