/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.cabrillo.tracker;

import java.beans.PropertyChangeEvent;

import javax.swing.Icon;

import org.opensourcephysics.tools.*;

/**
 * This is a FunctionPanel for particle models.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class ModelFunctionPanel extends FunctionPanel {

	InitialValueEditor initEditor;
	ParticleModel model;
	
  /**
   * Constructor with user function editor.
   *
   * @param editor the user function editor
   * @param track a ParticleModel
   */
  public ModelFunctionPanel(UserFunctionEditor editor, ParticleModel track) {
  	super(editor);
  	model = track;
  	setName(track.getName("model")); //$NON-NLS-1$
  }

	/**
	 * Gets the function type.
	 * 
	 * @return a string describing the type of function
	 */
	public String getLabel() {
		return TrackerRes.getString("ModelFunctionPanel.Label"); //$NON-NLS-1$
	}

  /**
   * Gets the display name for the FunctionTool dropdown.
   *
   * @return the display name
   */
  public String getDisplayName() {
  	if (model != null)
  		return model.getDisplayName();
  	return super.getDisplayName();
  }

  /**
   * Returns the function editor.
   *
   * @return UserFunctionEditor
   */
  public UserFunctionEditor getUserFunctionEditor() {
    return (UserFunctionEditor)functionEditor;
  }

  /**
   * Gets the initial value editor.
   *
   * @return the initial value editor
   */
  public InitialValueEditor getInitEditor() {
    return initEditor;
  }

	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		super.createGUI();
		initEditor = new InitialValueEditor(getParamEditor());
		box.add(initEditor, 1);
		initEditor.addPropertyChangeListener(this);
		paramEditor.addPropertyChangeListener(initEditor);
		functionEditor.addPropertyChangeListener(initEditor);
		initEditor.addPropertyChangeListener(paramEditor);
		initEditor.addPropertyChangeListener(functionEditor);
		FunctionEditor[] editors = new FunctionEditor[] {functionEditor, initEditor};
		paramEditor.setFunctionEditors(editors);
	}
	
  /**
   * Gets an Icon associated with this panel, if any.
   * 
   * @return the icon
   */
  public Icon getIcon() {
  	if (model != null)
  		return model.getIcon(21, 16, "model"); //$NON-NLS-1$
    return null;
  }

  /**
	 * Refreshes the GUI.
	 */
  protected void refreshGUI() {
  	super.refreshGUI();
    initEditor.refreshGUI();
  }

	/**
   * Refreshes the functions.
   */
  protected void refreshFunctions() {
  	if (paramEditor != null) {
    	UserFunction[] functions = ((UserFunctionEditor)functionEditor).getMainFunctions();
  		for (int i = 0; i < functions.length; i++) {
  			functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
  		}		
    	functions = ((UserFunctionEditor)functionEditor).getSupportFunctions();
    	for (int i = 0; i < functions.length; i++) {
    		functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(), paramEditor.getDescriptions());
    	}		
  	}
  	// evaluate the initial values 
    initEditor.evaluateAll();
  	// evaluate the functions 
    functionEditor.evaluateAll();
  }

	/**
	 * Clears the selection.
	 */
	protected void clearSelection() {
		super.clearSelection();
  	initEditor.getTable().clearSelection();
	}
	
  /**
   * Disposes of this panel.
   */
  protected void dispose() {
  	if (paramEditor==null) {
  		// already disposed!
  		return;
  	}
		initEditor.removePropertyChangeListener(this);
		paramEditor.removePropertyChangeListener(initEditor);
		functionEditor.removePropertyChangeListener(initEditor);
		initEditor.removePropertyChangeListener(paramEditor);
		initEditor.removePropertyChangeListener(functionEditor);
    initEditor.setFunctionPanel(null);
		initEditor.setFunctionEditors(null);
		paramEditor.setFunctionEditors(null);
    model = null;
    super.dispose();
  }

	/**
	 * Tabs to the next editor.
	 * 
	 * @param editor the current editor
	 */
	protected void tabToNext(FunctionEditor editor) {
		if (editor == paramEditor) {
			initEditor.getTable().requestFocusInWindow();
		}
		else super.tabToNext(editor);
	}

  /**
   * Listens for property changes "edit" and "function"
   *
   * @param e the event
   */
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		String name = e.getPropertyName();
 	  if (e.getSource() == paramEditor && name.equals("edit")) { //$NON-NLS-1$
	  	initEditor.getTable().selectOnFocus = false;
 	  }
	}
	
	@Override
  protected boolean hasInvalidExpressions() {
    return functionEditor.containsInvalidExpressions() 
    		|| paramEditor.containsInvalidExpressions()
    		|| initEditor.containsInvalidExpressions();
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 3 of the License,
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
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
