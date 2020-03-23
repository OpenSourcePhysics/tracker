/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Dimension;
import java.util.List;

/**
 * A FunctionEditor for initial values.
 *
 * @author Douglas Brown
 */
public class InitialValueEditor extends ParamEditor {
  /**
   * Default constructor
   * @param editor
   */
  public InitialValueEditor(ParamEditor editor) {
    super();
    paramEditor = editor;
    setFunctionPanel(editor.getFunctionPanel());
  }

  /**
   * Determines if an object's name is editable.
   *
   * @param obj the object
   * @return always false
   */
  public boolean isNameEditable(Object obj) {
    return false;
  }

  /**
   * Override getPreferredSize().
   *
   * @return the table size, with adjustments
   */
  public Dimension getPreferredSize() {
    boolean hasButtons = false;
    for(java.awt.Component c : getComponents()) {
      if(c==buttonPanel) {
        hasButtons = true;
      }
    }
    if(hasButtons) {
      return super.getPreferredSize();
    }
    Dimension dim = table.getPreferredSize();
    dim.height += table.getTableHeader().getHeight();
    dim.height += 1.25*table.getRowHeight()+14;
    return dim;
  }

  @Override
  public Dimension getMaximumSize() {
  	Dimension dim = super.getMaximumSize();
  	dim.height = getPreferredSize().height;
    return dim;
  }

  /**
   * Evaluates all current objects.
   */
  public void evaluateAll() {
    super.evaluateAll();
    if(paramValues.length!=objects.size()) {
      paramValues = new double[objects.size()];
    }
    List<Object> params = paramEditor.getObjects();
    for(int i = 0; i<evaluate.size(); i++) {
      Parameter p = (Parameter) evaluate.get(i);
      p.evaluate(params);
    }
    for(int i = 0; i<objects.size(); i++) {
      Parameter p = (Parameter) objects.get(i);
      paramValues[i] = p.getValue();
    }
  }

  @Override
  protected boolean isValidExpression(String expression) {
  	Parameter p = new Parameter("xxzz", expression); //$NON-NLS-1$
  	return !Double.isNaN(p.evaluate(paramEditor.getObjects()));
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    super.createGUI();
    remove(buttonPanel);
  }

  /**
   * Refreshes the GUI.
   */
  public void refreshGUI() {
    super.refreshGUI();
    titledBorder.setTitle(ToolsRes.getString("InitialValueEditor.Border.Title")); //$NON-NLS-1$
  }

  /**
   * Returns a String with the names of variables available for expressions.
   * Only parameter names are available to initial values.
   */
  protected String getVariablesString(String separator) {
    StringBuffer vars = new StringBuffer(""); //$NON-NLS-1$
    int init = vars.length();
    int row = table.getSelectedRow();
    if (!"t".equals(table.getValueAt(row, 0))) { //$NON-NLS-1$
      // add parameters, if any
	    boolean firstItem = true;
      String[] paramNames = paramEditor.getNames();
      for(int i = 0; i<paramNames.length; i++) {
        if(!firstItem) {
          vars.append(" "); //$NON-NLS-1$
        }
        vars.append(paramNames[i]);
        firstItem = false;
      }
    }
    if(vars.length()==init) {
      return ToolsRes.getString("FunctionPanel.Instructions.Help"); //$NON-NLS-1$
    }
    return ToolsRes.getString("FunctionPanel.Instructions.ValueCell") //$NON-NLS-1$
           +separator+vars.toString(); 
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
