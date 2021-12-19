/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <https://www.compadre.org/osp/>
 */

package org.opensourcephysics.cabrillo.tracker;

import java.beans.PropertyChangeEvent;

import javax.swing.Icon;

import org.opensourcephysics.tools.*;

/**
 * A subclass of FunctionPanel specifically for particle models.
 * 
 * <code>
     ModelFunctionPanel
        AnalyticFunctionPanel
        DynamicFunctionPanel
        ParticleDataTrackFunctionPanel
 </code>
 *
 *Created by their respective ParticleModel constructors (including ParticleDataTrack)
 * 
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class ModelFunctionPanel extends FunctionPanel {

	InitialValueEditor initEditor;
	ParticleModel model;

	/**
	 * Constructor with user function editor.
	 *
	 * @param editor the user function editor
	 * @param track  a ParticleModel
	 */
	public ModelFunctionPanel(UserFunctionEditor editor, ParticleModel track) {
		super(editor);
		model = track;
		setName(track.getName("model")); //$NON-NLS-1$
	}


	@Override
	protected void init() {
		super.init();
		initEditor = new InitialValueEditor(getParamEditor());
		initEditor.addPropertyChangeListener(this);
		paramEditor.addPropertyChangeListener(initEditor);
		functionEditor.addPropertyChangeListener(initEditor);
		initEditor.addPropertyChangeListener(paramEditor);
		initEditor.addPropertyChangeListener(functionEditor);
		FunctionEditor[] editors = new FunctionEditor[] { functionEditor, initEditor };
		paramEditor.setFunctionEditors(editors);
	}

	@Override
	public void checkGUI() {
		if (haveGUI())
			return;
		super.checkGUI();
		initEditor.checkGUI();
		paramEditor.checkGUI();
		functionEditor.checkGUI();
	}

	/**
	 * Creates the GUI.
	 */
	@Override
	protected void createGUI() {
		super.createGUI();
		box.add(initEditor, 1);
	}

	/**
	 * Refreshes the GUI.
	 */
	@Override
	protected void refreshGUI() {
		if (!haveGUI())
			return;
		super.refreshGUI();
		initEditor.refreshGUI();
	}


	/**
	 * Gets the function type.
	 * 
	 * @return a string describing the type of function
	 */
	@Override
	public String getLabel() {
		return TrackerRes.getString("ModelFunctionPanel.Label"); //$NON-NLS-1$
	}

	/**
	 * Gets the display name for the FunctionTool dropdown.
	 *
	 * @return the display name
	 */
	@Override
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
		return (UserFunctionEditor) functionEditor;
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
	 * Gets an Icon associated with this panel, if any.
	 * 
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		if (model != null)
			return model.getIcon(21, 16, "model"); //$NON-NLS-1$
		return null;
	}

	/**
	 * Refreshes the functions.
	 */
	@Override
	protected void refreshFunctions() {
		if (paramEditor != null) {
			UserFunction[] functions = ((UserFunctionEditor) functionEditor).getMainFunctions();
			for (int i = 0; i < functions.length; i++) {
				functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(),
						paramEditor.getDescriptions());
			}
			functions = ((UserFunctionEditor) functionEditor).getSupportFunctions();
			for (int i = 0; i < functions.length; i++) {
				functions[i].setParameters(paramEditor.getNames(), paramEditor.getValues(),
						paramEditor.getDescriptions());
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
	@Override
	protected void clearSelection() {
		super.clearSelection();
		if (initEditor != null)
			initEditor.getTable().clearSelection();
	}

	/**
	 * Disposes of this panel.
	 */
	@Override
	protected void dispose() {
		if (paramEditor == null) {
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
	@Override
	protected void tabToNext(FunctionEditor editor) {
		if (editor == paramEditor) {
			initEditor.getTable().requestFocusInWindow();
		} else
			super.tabToNext(editor);
	}

	/**
	 * Listens for property changes "edit" and "function"
	 *
	 * @param e the event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		switch (e.getPropertyName()) {
		case FunctionEditor.PROPERTY_FUNCTIONEDITOR_EDIT:
			if (e.getSource() == paramEditor) {
				initEditor.getTable().selectOnFocus = false;
			}
			break;
		case FunctionEditor.PROPERTY_FUNCTIONEDITOR_ANGLESINRADIANS:
			if (model.tp != null) {
				model.tframe.setAnglesInRadians((Boolean) e.getNewValue());
				break;
			}
		}
	}

	@Override
	protected boolean hasInvalidExpressions() {
		return super.hasInvalidExpressions() || initEditor.containsInvalidExpressions();
	}

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 3 of the License,
 * or(at your option) any later version.
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2021 The Open Source Physics project
 * https://www.compadre.org/osp
 */
