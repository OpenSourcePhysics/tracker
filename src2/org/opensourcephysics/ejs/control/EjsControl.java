/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.util.Collection;
import org.opensourcephysics.controls.ParsableTextArea;
import org.opensourcephysics.ejs.control.swing.ControlInputArea;
import org.opensourcephysics.ejs.control.swing.ControlTextArea;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A blend of GroupControl and org.opensourcephysics.control.Control
 */
public class EjsControl extends GroupControl implements org.opensourcephysics.controls.Control {
  static String _RETURN_ = System.getProperty("line.separator"); //$NON-NLS-1$
  private ControlTextArea messageArea = null;
  private ParsableTextArea inputArea = null;
  private StringValue strValue = new StringValue("");            //$NON-NLS-1$

  /**
   * The EjsControl constructor.
   * @param _simulation
   */
  public EjsControl(Object _simulation) {
    super(_simulation);
  }

  /**
   * Constructor EjsControl
   * @param _simulation
   * @param _replaceName
   * @param _replaceOwnerFrame
   */
  public EjsControl(Object _simulation, String _replaceName, java.awt.Frame _replaceOwnerFrame) {
    super(_simulation, _replaceName, _replaceOwnerFrame);
  }

  /**
   * Constructor EjsControl
   */
  public EjsControl() {
    super();
  }

  // ----------------------------------------------
  // Creation of particular control elements
  // ----------------------------------------------
  public ControlElement addObject(Object _object, String _classname, String _propList) {
    ControlElement control = super.addObject(_object, _classname, _propList);
    if(control instanceof ControlTextArea) {
      messageArea = (ControlTextArea) control;
    } else if(control instanceof ControlInputArea) {
      inputArea = (ParsableTextArea) ((ControlInputArea) control).getVisual();
    }
    return control;
  }

  public void reset() {
    clearValues();
    clearMessages();
    super.reset();
  }

  // public void clearInputArea() { if (inputArea!=null) inputArea.setText(""); }
  // ------------------------------------------
  // Implementation of the Control interface
  // ------------------------------------------

  /**
 * Locks the control's interface. Values sent to the control will not
 * update the display until the control is unlocked.
 *
 * @param lock boolean
 */
  public void setLockValues(boolean lock) {}

  /**
 *  Reads the current property names.
 *
 * @return      the property names
 */
  public Collection<String> getPropertyNames() {
    return variableTable.keySet();
  }

  public void clearValues() {
    if(inputArea!=null) {
      inputArea.setText(""); //$NON-NLS-1$
      inputArea.setCaretPosition(inputArea.getText().length());
    }
  }

  public void clearMessages() {
    if(messageArea!=null) {
      messageArea.clear();
    }
  }

  public void println(String s) {
    print(s+_RETURN_);
  }

  public void println() {
    println(""); //$NON-NLS-1$
  }

  public void print(String s) {
    if(messageArea!=null) {
      messageArea.print(s);
    } else {
      System.out.print(s);
    }
  }

  public void calculationDone(String message) {
    println(message);
  }

  // Set and get values
  public void setValue(String _variable, Value _value) {
    if(!isVariableRegistered(_variable)&&(inputArea!=null)) {
      inputArea.setValue(_variable, _value.getString());
    } else {
      super.setValue(_variable, _value);
    }
  }

  public Value getValue(String _variable) {
    if(!isVariableRegistered(_variable)&&(inputArea!=null)) {
      try {
        strValue.value = inputArea.getValue(_variable);
        return strValue;
      } catch(org.opensourcephysics.controls.VariableNotFoundException e) {
        // println(e.getMessage());
      }
    }
    return super.getValue(_variable);
  }

} // end of class

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
