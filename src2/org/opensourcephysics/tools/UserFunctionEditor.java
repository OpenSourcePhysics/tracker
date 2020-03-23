/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * A FunctionEditor for UserFunctions.
 *
 * @author Douglas Brown
 */
public class UserFunctionEditor extends FunctionEditor {
  private UserFunction[] mainFunctions = new UserFunction[0];
  private String[] defaultVariableNames = new String[] {"x"}; //$NON-NLS-1$
  protected boolean parametersValid = true;

  /**
   * Constructor.
   */
  public UserFunctionEditor() {
    super();
  }

  /**
   * Returns the main user functions.
   *
   * @return UserFunction[]
   */
  public UserFunction[] getMainFunctions() {
    return mainFunctions;
  }

  /**
   * Sets the main user functions.
   *
   * @param functions UserFunction[]
   */
  public void setMainFunctions(UserFunction[] functions) {
    // remove existing main functions
    UserFunction[] f = getMainFunctions();
    for(UserFunction u : f) {
      objects.remove(u);
    }
    // add new main functions
    for(UserFunction u : functions) {
      addObject(u, false);
    }
    //    for (int i = 0; i < functions.length; i++) {
    //      boolean found = false;
    //      for (int row = 0; row < objects.size(); row++) {
    //        Object next = objects.get(row);
    //        if (getName(next).equals(functions[i].getName())) {
    //          objects.remove(next);
    //          objects.add(row, functions[i]);
    //          found = true;
    //        }
    //      }
    //      if (!found) addObject(functions[i], false);
    //    }
    mainFunctions = functions;
    setDefaultVariables(functions[0].getIndependentVariables());
  }

  /**
   * Returns supporting functions
   *
   * @return an array of UserFunctions
   */
  public UserFunction[] getSupportFunctions() {
    ArrayList<Object> temp = new ArrayList<Object>();
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      if(!isMainFunction(next)) {
        temp.add(next);
      }
    }
    return temp.toArray(new UserFunction[0]);
  }

  /**
   * Returns the name of the object.
   *
   * @param obj the object
   * @return the name
   */
  public String getName(Object obj) {
    return (obj==null)? null: ((UserFunction)obj).getName();
  }

  /**
   * Returns the expression of the object.
   *
   * @param obj the object
   * @return the expression
   */
  public String getExpression(Object obj) {
    return (obj==null)? null: ((UserFunction)obj).getInputString();
  }

  /**
   * Returns the description of the object.
   *
   * @param obj the object
   * @return the description
   */
  public String getDescription(Object obj) {
    return (obj==null)? null: ((UserFunction)obj).getDescription();
  }

  /**
   * Sets the description of the object.
   *
   * @param obj the object
   * @param desc the description
   */
  public void setDescription(Object obj, String desc) {
  	if (obj!=null) {
    	if (desc!=null && desc.trim().equals("")) { //$NON-NLS-1$
    		desc = null;
    	}
  		((UserFunction)obj).setDescription(desc);
  		super.setDescription(obj, desc);
  	}
  }

  /**
   * Determines if an object's name is editable.
   *
   * @param obj the object
   * @return true if the name is editable
   */
  public boolean isNameEditable(Object obj) {
    return((UserFunction) obj).isNameEditable();
  }

  /**
   * Determines if an object's expression is editable.
   *
   * @param obj the object
   * @return true if the expression is editable
   */
  public boolean isExpressionEditable(Object obj) {
  	UserFunction f = (UserFunction)obj;
  	if (f.polynomial!=null) return false;
    return true;
  }

  /**
   * Evaluates all current objects.
   */
  public void evaluateAll() {
    super.evaluateAll();
    ParamEditor paramEditor = getParamEditor();
    if(!parametersValid&&(paramEditor!=null)) {
      paramEditor.evaluateAll();
    }
    for(int i = 0; i<evaluate.size(); i++) {
      UserFunction f = (UserFunction) evaluate.get(i);
      if(!parametersValid&&(paramEditor!=null)) {
        f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
      }
      f.setExpression(f.getInputString(), f.getIndependentVariables());
    }
    parametersValid = true;
  }

  /**
   * Adds an object.
   *
   * @param obj the object
   * @param postEdit true to post an undoable edit
   */
  public Object addObject(Object obj, int row, boolean postEdit, boolean firePropertyChange) {
    obj = super.addObject(obj, row, postEdit, firePropertyChange);
    if(obj!=null) {
      firePropertyChange("function", null, obj); //$NON-NLS-1$
    }
    return obj;
  }

  /**
   * Removes an object.
   *
   * @param obj the object to remove
   * @param postEdit true to post an undoable edit
   * @return the removed object
   */
  public Object removeObject(Object obj, boolean postEdit) {
    obj = super.removeObject(obj, postEdit);
    if(obj!=null) {
      firePropertyChange("function", obj, null); //$NON-NLS-1$
    }
    return obj;
  }

  /**
   * Returns a tooltip for the object.
   *
   * @param obj the object
   * @return the tooltip
   */
  public String getTooltip(Object obj) {
    return (obj==null)? null: ((UserFunction)obj).getDescription();
  }

  /**
   * Responds to property change events.
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	String propName = e.getPropertyName();
    if (propName.equals("param_description")) { //$NON-NLS-1$
      // parameter description has changed
    	for (UserFunction f: getMainFunctions()) {
        f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
    	}
    	for (UserFunction f: getSupportFunctions()) {
        f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
    	}
    }
    else if (propName.equals("edit")) { //$NON-NLS-1$
      // parameter has changed
    	UserFunction[] mainFunctions = getMainFunctions();
    	if (mainFunctions.length>0) {
	    	String newName = (String)e.getOldValue();
	    	String oldName = null;
	    	Object obj = e.getNewValue();
	    	UserFunction func = getMainFunctions()[0];
	    	if (func.polynomial!=null && obj!=null) {
	    		if (obj instanceof DefaultEdit) {
		      	DefaultEdit edit = (DefaultEdit)obj;
		      	if (edit.editType!=FunctionEditor.NAME_EDIT) {
		      		super.propertyChange(e);
		      		return;
		      	}
		      	oldName = (String)edit.undoObj;
	    		}
	    		else if (obj instanceof String) {
	    			oldName = (String)obj;
	    		}
	    		if (oldName!=null) {
		      	for (UserFunction f: getMainFunctions()) {
		          f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
		          f.replaceParameterNameInExpression(oldName, newName);
		      	}
		      	for (UserFunction f: getSupportFunctions()) {
		          f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
		          f.replaceParameterNameInExpression(oldName, newName);
		      	}
	    		}
	    	}
    	}
    }
    
    super.propertyChange(e);
  }

  //_________________________ protected methods ___________________________

  /**
   * Determines if an object is important. Important objects cannot be cut
   * even if they are editable.
   *
   * @param obj the object
   * @return true if important
   */
  protected boolean isImportant(Object obj) {
    for(int i = 0; i<mainFunctions.length; i++) {
      if(mainFunctions[i]==obj) {
        return true;
      }
    }
    return false;
  }

  /**
   * Informs an object about other objects referenced in its expression.
   */
  protected void setReferences(Object obj, List<Object> ref) {
    UserFunction f = (UserFunction) obj;
    UserFunction[] references = ref.toArray(new UserFunction[0]);
    f.setReferences(references);
  }

  /**
   * Sets the default variable names.
   *
   * @param varNames the names
   */
  protected void setDefaultVariables(String[] varNames) {
    defaultVariableNames = varNames;
  }

  /**
   * Returns true if a name is forbidden or in use.
   *
   * @param obj the object (may be null)
   * @param name the proposed name for the object
   * @return true if disallowed
   */
  protected boolean isDisallowedName(Object obj, String name) {
    boolean disallowed = super.isDisallowedName(obj, name);
    if(obj!=null) {
      String var = ((UserFunction) obj).getIndependentVariable();
      disallowed = disallowed||var.equals(name);
    }
    
    if(disallowed) {
      return true;
    }
    // added following line so leaving object name unchanged is not disallowed
    if (obj!=null && getName(obj).equals(name)) return false;
    
    if (functionPanel instanceof FitFunctionPanel) {
      FitFunctionPanel fitPanel = (FitFunctionPanel) functionPanel;
      if(fitPanel.functionTool!=null) {
        String s = fitPanel.functionTool.getUniqueName(name);
        disallowed = !name.equals(s);
        for(DatasetCurveFitter next : fitPanel.functionTool.curveFitters) {
          if(disallowed) {
            return true;
          }
          disallowed = next.fitMap.keySet().contains(name);
        }
      }
    }
    return disallowed;
  }

  /**
   * Returns a String with the names of variables available for expressions.
   */
  protected String getVariablesString(String separator) {
    StringBuffer vars = new StringBuffer(""); //$NON-NLS-1$
    int init = vars.length();
    boolean firstItem = true;
    UserFunction f = (UserFunction) getSelectedObject();
    if(f!=null) {
      String[] s = f.getIndependentVariables();
      for(int i = 0; i<s.length; i++) {
        if(!firstItem) {
          vars.append(" "); //$NON-NLS-1$
        }
        vars.append(s[i]);
        firstItem = false;
      }
    }
    List<String> namesToSkip = new ArrayList<String>();
    namesToSkip.add(getName(getSelectedObject()));
    for(int i = 0; i<mainFunctions.length; i++) {
      namesToSkip.add(getName(mainFunctions[i]));
    }
    for(int i = 0; i<names.length; i++) {
      if(namesToSkip.contains(names[i])) {
        continue;
      }
      if(!firstItem) {
        vars.append(" "); //$NON-NLS-1$
      }
      vars.append(names[i]);
      firstItem = false;
    }
    // add parameters, if any
    String[] paramNames = paramEditor.getNames();
    for(int i = 0; i<paramNames.length; i++) {
      if(!firstItem) {
        vars.append(" "); //$NON-NLS-1$
      }
      vars.append(paramNames[i]);
      firstItem = false;
    }
    if(vars.length()==init) {
      return ToolsRes.getString("FunctionPanel.Instructions.Help"); //$NON-NLS-1$
    }
    return ToolsRes.getString("FunctionPanel.Instructions.ValueCell") //$NON-NLS-1$
           +separator+vars.toString(); 
  }

  /**
   * Returns true if the object's expression is invalid.
   */
  protected boolean isInvalidExpression(Object obj) {
    UserFunction f = (UserFunction) obj;
    return !f.getExpression().equals(f.getInputString());
  }

  /**
   * Creates an object with specified name and expression.
   * This modifies and returns the input UserFunction unless null.
   *
   * @param name the name
   * @param expression the expression
   * @param obj ignored
   * @return the object
   */
  protected Object createObject(String name, String expression, Object obj) {
    UserFunction f = (UserFunction) obj;
    if((f!=null)&&f.getName().equals(name)&&f.getInputString().equals(expression)) {
      return f;
    }
    if(f==null) {
      f = new UserFunction(name);
      f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
      f.setExpression(expression, defaultVariableNames);
    } else if(!f.getName().equals(name)) {
      f.setNameEditable(true);
      f.setName(name);
    } else {
      f.setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
      f.setExpression(expression, f.getIndependentVariables());
    }
    return f;
  }

  private boolean isMainFunction(Object obj) {
    for(int i = 0; i<mainFunctions.length; i++) {
      if(obj==mainFunctions[i]) {
        return true;
      }
    }
    return false;
  }

  //__________________________ static methods ___________________________

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {
    public void saveObject(XMLControl control, Object obj) {
      UserFunctionEditor editor = (UserFunctionEditor) obj;
      UserFunction[] functions = editor.getMainFunctions();
      control.setValue("main_functions", functions); //$NON-NLS-1$
      functions = editor.getSupportFunctions();
      if(functions.length>0) {
        control.setValue("support_functions", functions); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control) {
      return new UserFunctionEditor();
    }

    public Object loadObject(XMLControl control, Object obj) {
      UserFunctionEditor editor = (UserFunctionEditor) obj;
      UserFunction[] functions = (UserFunction[]) control.getObject("main_functions"); //$NON-NLS-1$
      editor.setMainFunctions(functions);
      int row = functions.length;
      functions = (UserFunction[]) control.getObject("support_functions"); //$NON-NLS-1$
      if(functions!=null) {
        for(int i = 0; i<functions.length; i++) {
          editor.addObject(functions[i], row+i, false, false);
        }
      }
      return obj;
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
