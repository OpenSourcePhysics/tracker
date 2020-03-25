/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.awt.Color;
import javax.swing.JTextField;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A textfield to display double values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlTextField extends ControlSwingElement {
  static private final int VARIABLE = 0;
  static private final int BACKGROUND = ControlSwingElement.BACKGROUND+4; // shadows superclass field
  protected JTextField textfield;
  private StringValue internalValue;
  private boolean defaultValueSet;
  private String defaultValue;
  private Color defaultColor, editingColor;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlTextField
   * @param _visual
   */
  public ControlTextField(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JTextField) {
      textfield = (JTextField) _visual;
    } else {
      textfield = new JTextField();
      textfield.setText(""); //$NON-NLS-1$
    }
    defaultValue = textfield.getText();
    textfield.addActionListener(new MyActionListener());
    textfield.addKeyListener(new MyKeyListener());
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
    decideColors(textfield.getBackground());
    return textfield;
  }

  public void reset() {
    if(defaultValueSet) {
      setTheValue(defaultValue);
      setInternalValue(defaultValue);
    }
  }

  private void setTheValue(String _value) {
    if(internalValue.value.equals(_value)) {
      return;
    }
    textfield.setText(internalValue.value = _value);
    setColor(defaultColor);
  }

  private void setInternalValue(String _value) {
    internalValue.value = _value;
    variableChanged(VARIABLE, internalValue);
    invokeActions();
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("variable"); //$NON-NLS-1$
      infoList.add("value");    //$NON-NLS-1$
      infoList.add("editable"); //$NON-NLS-1$
      infoList.add("action");   //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("variable")) {   //$NON-NLS-1$
      return "String VARIABLE_EXPECTED"; //$NON-NLS-1$
    }
    if(_property.equals("value")) { //$NON-NLS-1$
      return "String CONSTANT";     //$NON-NLS-1$
    }
    if(_property.equals("editable")) { //$NON-NLS-1$
      return "boolean";                //$NON-NLS-1$
    }
    if(_property.equals("action")) { //$NON-NLS-1$
      return "Action CONSTANT";      //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case VARIABLE :
         setTheValue(_value.getString());
         break;
       case 1 :
         defaultValueSet = true;
         defaultValue = _value.getString();
         setActive(false);
         reset();
         setActive(true);
         break;
       case 2 :
         textfield.setEditable(_value.getBoolean());
         break;
       case 3 :                                                      // action
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         addAction(ControlElement.ACTION, _value.getString());
         break;
       case BACKGROUND :
         super.setValue(ControlSwingElement.BACKGROUND, _value);
         decideColors(getVisual().getBackground());
         break;
       default :
         super.setValue(_index-4, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case VARIABLE :
         break;                                                      // Do nothing
       case 1 :
         defaultValueSet = false;
         break;
       case 2 :
         textfield.setEditable(true);
         break;
       case 3 :
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         break;
       case BACKGROUND :
         super.setDefaultValue(ControlSwingElement.BACKGROUND);
         decideColors(getVisual().getBackground());
         break;
       default :
         super.setDefaultValue(_index-4);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case VARIABLE :
         return internalValue;
       case 1 :
       case 2 :
       case 3 :
         return null;
       default :
         return super.getValue(_index-4);
    }
  }

  // -------------------------------------
  // Private methods and inner classes
  // -------------------------------------
  private void setColor(Color aColor) {
    if(textfield.isEditable()) {
      getVisual().setBackground(aColor);
    }
  }

  private void decideColors(Color aColor) {
    if(aColor==null) {
      return;
    }
    defaultColor = aColor;
    if(defaultColor.equals(Color.yellow)) {
      editingColor = Color.orange;
    } else {
      editingColor = Color.yellow;
    }
  }

  private class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent _e) {
      setInternalValue(textfield.getText());
      setColor(defaultColor);
    }

  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed(java.awt.event.KeyEvent _e) {
      processKeyEvent(_e, 0);
    }

    public void keyReleased(java.awt.event.KeyEvent _e) {
      processKeyEvent(_e, 1);
    }

    public void keyTyped(java.awt.event.KeyEvent _e) {
      processKeyEvent(_e, 2);
    }

    private void processKeyEvent(java.awt.event.KeyEvent _e, int _n) {
      if(!textfield.isEditable()) {
        return;
      }
      if(_e.getKeyChar()!='\n') {
        setColor(editingColor);
      }
      if(_e.getKeyCode()==27) {
        setValue(VARIABLE, internalValue);
      }
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
