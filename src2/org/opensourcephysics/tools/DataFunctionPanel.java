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
import org.opensourcephysics.display.DataFunction;
import org.opensourcephysics.display.DatasetManager;

/**
 * This is a FunctionPanel for DataFunctions.
 *
 * @author Douglas Brown
 */
public class DataFunctionPanel extends FunctionPanel {
  /**
   * Constructor with input data.
   *
   * @param input the input DatasetManager
   */
  public DataFunctionPanel(DatasetManager input) {
    this(new DataFunctionEditor(input));
  }

  /**
   * Constructor with function editor.
   *
   * @param editor a DataFunctionEditor
   */
  public DataFunctionPanel(DataFunctionEditor editor) {
    super(editor);
    String name = editor.getData().getName();
    setName(name.equals("") ? "data" : name); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns the DatasetManager.
   *
   * @return the DatasetManager
   */
  public DatasetManager getData() {
    return((DataFunctionEditor) functionEditor).getData();
  }

  /**
   * Gets a label for the FunctionTool spinner.
   *
   * @return a label string
   */
  public String getLabel() {
    return ToolsRes.getString("DataFunctionPanel.SpinnerLabel"); //$NON-NLS-1$
  }

  /**
   * Listens for property changes "edit" and "function"
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if(e.getPropertyName().equals("edit")) {                                         //$NON-NLS-1$
      refreshFunctions();
      super.propertyChange(e);
    } else if(e.getPropertyName().equals("function")) {                              //$NON-NLS-1$
      // function has been added or removed
      if(e.getNewValue()!=null) {                                                    // added
        DataFunction f = (DataFunction) e.getNewValue();
        getData().addDataset(f);
      } else if(e.getOldValue()!=null) {                                             // removed
        DataFunction f = (DataFunction) e.getOldValue();
        int i = getData().getDatasetIndex(f.getYColumnName());
        getData().removeDataset(i);
      }
      refreshFunctions();
      refreshGUI();
      if (functionTool!=null) {
	      functionTool.refreshGUI();
	      functionTool.firePropertyChange("function", e.getOldValue(), e.getNewValue()); //$NON-NLS-1$
      }
    }
  }

  /**
   * Refreshes the functions.
   */
  protected void refreshFunctions() {
    // set the constant values in the data
    for (String name: getData().getConstantNames()) {
    	getData().clearConstant(name);
    }
    Iterator<Object> it = paramEditor.getObjects().iterator();
    while(it.hasNext()) {
      Parameter p = (Parameter) it.next();
      String name = p.getName();
      double val = p.getValue();
      getData().setConstant(name, val, p.getExpression(), p.getDescription());
    }
    // evaluate the functions 
    functionEditor.evaluateAll();
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
      DataFunctionPanel panel = (DataFunctionPanel) obj;
      // save description--used by Tracker for class identification
      control.setValue("description", panel.getDescription()); //$NON-NLS-1$
      Parameter[] params = panel.getParamEditor().getParameters();
      control.setValue("user_parameters", params);                       //$NON-NLS-1$
      FunctionEditor editor = panel.getFunctionEditor();
      ArrayList<String[]> functions = new ArrayList<String[]>();
      for (Object next: editor.getObjects()) {
      	functions.add(new String[] {editor.getName(next), editor.getExpression(next)});
      }
      control.setValue("functions", functions); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
    	// DataFunctionPanel must be instantiated by application prior to loading
      return null;
    }

    public Object loadObject(XMLControl control, Object obj) {
    	DataFunctionPanel panel = (DataFunctionPanel) obj;
    	panel.setDescription(control.getString("description")); //$NON-NLS-1$
      Parameter[] params = (Parameter[]) control.getObject("user_parameters"); //$NON-NLS-1$
  		Parameter[] existing = panel.getParamEditor().getParameters();
      // add new parameters to existing
  		ArrayList<Parameter> allParams = new ArrayList<Parameter>();
  		ArrayList<String> names = new ArrayList<String>();
  		for (Parameter param: existing) {
  			allParams.add(param);
  			names.add(param.getName());
  		}
      for (Parameter param: params) {
      	if (names.contains(param.getName())) continue;
      	allParams.add(param);
      }
      params = allParams.toArray(new Parameter[allParams.size()]);
      panel.getParamEditor().setParameters(params);
      ArrayList<?> functionsToImport = (ArrayList<?>)control.getObject("functions"); //$NON-NLS-1$
      FunctionEditor editor = panel.getFunctionEditor();
      List<Object> existingFunctions = editor.getObjects();
      DatasetManager data = panel.getData();
      outer: for (Object next: functionsToImport) {
      	String[] function = (String[])next;
      	for (Object f: existingFunctions) {
      		DataFunction dataFunction = (DataFunction)f;
      		if (dataFunction.getYColumnName().equals(function[0]) && dataFunction.getExpression().equals(function[1])) {
      			continue outer;
      		}
      	}
      	DataFunction newFunction = new DataFunction(data, function[0], function[1]);
      	editor.addObject(newFunction, false);
      }
//      editor.evaluateAll();
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
