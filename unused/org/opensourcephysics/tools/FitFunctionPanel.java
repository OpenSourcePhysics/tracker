/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.PropertyChangeEvent;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a FunctionPanel used to manage fits for a DatasetCurveFitter.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class FitFunctionPanel extends FunctionPanel {
	
	protected String originalName;
	
  /**
   * Constructor with user function editor. The editor must be preloaded
   * with at least one main function (the fit function);
   *
   * @param editor the user function editor
   */
  public FitFunctionPanel(UserFunctionEditor editor) {
    super(editor);
    UserFunction[] functions = editor.getMainFunctions();
    int n = 0;
    for(int j = 0; j<functions.length; j++) {
      for(int i = 0; i<functions[j].getParameterCount(); i++) {
        if(paramEditor.getObject(functions[j].getParameterName(i))==null) {
          Parameter param = new Parameter(functions[j].getParameterName(i), 
          		String.valueOf(functions[j].getParameterValue(i)),
          		functions[j].getParameterDescription(i));
          paramEditor.addObject(param, n++, false, false);
        }
      }
    }
    refreshFunctions();
    addForbiddenNames(new String[] {getFitFunction().getIndependentVariable()});
    setName(getFitFunction().getName());
  }

  /**
   * Returns the fit function editor.
   *
   * @return the editor
   */
  public UserFunctionEditor getFitFunctionEditor() {
    return(UserFunctionEditor) functionEditor;
  }

  /**
   * Returns the fit function.
   *
   * @return the fit UserFunction
   */
  public UserFunction getFitFunction() {
    return((UserFunctionEditor) functionEditor).getMainFunctions()[0];
  }

  /**
   * Returns the support functions.
   *
   * @return the fit UserFunction
   */
  public UserFunction[] getSupportFunctions() {
    return((UserFunctionEditor) functionEditor).getSupportFunctions();
  }

  /**
   * Gets an appropriate label for the FunctionTool dropdown.
   *
   * @return a label string
   */
  public String getLabel() {
    return ToolsRes.getString("FitFunctionPanel.Label"); //$NON-NLS-1$
  }

  /**
   * Listens for property change "edit".
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("edit") && functionTool!=null) { //$NON-NLS-1$
      UserFunctionEditor ufe = (UserFunctionEditor) functionEditor;
      UserFunction[] functions = ufe.getMainFunctions();
      if (functions!=null && functions.length>0) {
	      if (e.getSource()==functionEditor
	      		&& functions[0].getName().equals(e.getOldValue())) {
	        // rename this panel
	        functionTool.renamePanel(this.getName(), getFitFunction().getName());
	        if(e.getNewValue() instanceof FunctionEditor.DefaultEdit) {
	          FunctionEditor.DefaultEdit edit = (FunctionEditor.DefaultEdit) e.getNewValue();
	          functionEditor.getTable().selectCell(edit.undoRow, edit.undoCol);
	        }
		      functionTool.refreshGUI();
	      }
	      super.propertyChange(e);

	      if (functions[0].polynomial!=null) {
	        functionTool.firePropertyChange("function", null, functions[0].getName()); //$NON-NLS-1$
	      }
	      return;
      }
    }
    else if (e.getPropertyName().equals("description") && functionTool!=null) { //$NON-NLS-1$
      super.propertyChange(e);
      functionTool.firePropertyChange("function", null, getFitFunction().getName()); //$NON-NLS-1$
      return;
    }
    super.propertyChange(e);
  }

  /**
   * Refreshes the functions.
   */
  protected void refreshFunctions() {
    if(paramEditor!=null) {
      UserFunction[] functions = ((UserFunctionEditor) functionEditor).getMainFunctions();
      for(int i = 0; i<functions.length; i++) {
        functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
      }
      functions = ((UserFunctionEditor) functionEditor).getSupportFunctions();
      for(int i = 0; i<functions.length; i++) {
        functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
      }
    }
    // evaluate the functions 
    functionEditor.evaluateAll();
  }

  /**
   * Refreshes the parameters.
   */
  protected void refreshParameters() {
    if(paramEditor!=null) {
      UserFunction f = getFitFunction();
      paramEditor.refreshParametersFromFunction(f);
    }
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
      FitFunctionPanel panel = (FitFunctionPanel) obj;
      // add name and description for list chooser
      control.setValue("name", panel.getName()); //$NON-NLS-1$
      panel.setDescription("y = "+panel.getFitFunction().getExpression("x"));  //$NON-NLS-1$//$NON-NLS-2$
      control.setValue("description", panel.getDescription()); //$NON-NLS-1$
      Parameter[] params = panel.getParamEditor().getParameters();
      control.setValue("user_parameters", params);                       //$NON-NLS-1$
      control.setValue("function_editor", panel.getFitFunctionEditor()); //$NON-NLS-1$
      control.setValue("original_name", panel.originalName); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      UserFunctionEditor editor = (UserFunctionEditor) control.getObject("function_editor"); //$NON-NLS-1$
      return new FitFunctionPanel(editor);
    }

    public Object loadObject(XMLControl control, Object obj) {
      FitFunctionPanel panel = (FitFunctionPanel) obj;
      Parameter[] params = (Parameter[]) control.getObject("user_parameters"); //$NON-NLS-1$
      panel.getParamEditor().setParameters(params);
      panel.getFitFunctionEditor().parametersValid = false;
      panel.getFitFunctionEditor().evaluateAll();
      panel.originalName = control.getString("original_name"); //$NON-NLS-1$
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
