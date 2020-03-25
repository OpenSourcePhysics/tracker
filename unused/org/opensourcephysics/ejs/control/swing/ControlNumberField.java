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
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A textfield to display double values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlNumberField extends ControlSwingElement {
  static private final int VARIABLE = 0;
  static private final int BACKGROUND = ControlSwingElement.BACKGROUND+5;                                     // shadows superclass field
  static protected final java.text.DecimalFormat defaultFormat = new java.text.DecimalFormat("0.000;-0.000"); //$NON-NLS-1$
  protected JTextField textfield;
  protected DoubleValue internalValue;
  protected double defaultValue;
  protected boolean defaultValueSet;
  protected java.text.DecimalFormat format;
  protected Color defaultColor, editingColor, errorColor;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlNumberField
   * @param _visual
   */
  public ControlNumberField(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JTextField) {
      textfield = (JTextField) _visual;
    } else {
      textfield = new JTextField();
    }
    format = defaultFormat;
    defaultValue = 0.0;
    defaultValueSet = false;
    internalValue = new DoubleValue(defaultValue);
    textfield.setText(format.format(internalValue.value));
    textfield.addActionListener(new MyActionListener());
    textfield.addKeyListener(new MyKeyListener());
    decideColors(textfield.getBackground());
    return textfield;
  }

  public void reset() {
    if(defaultValueSet) {
      setTheValue(defaultValue);
      setInternalValue(defaultValue);
    }
  }

  private void setTheValue(double _value) {
    if(_value!=internalValue.value) {
      internalValue.value = _value;
      textfield.setText(format.format(_value));
      setColor(defaultColor);
    }
  }

  protected void setInternalValue(double _value) {
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
      infoList.add("format");   //$NON-NLS-1$
      infoList.add("action");   //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("variable")) { //$NON-NLS-1$
      return "int|double";             //$NON-NLS-1$
    }
    if(_property.equals("value")) { //$NON-NLS-1$
      return "int|double";          //$NON-NLS-1$
    }
    if(_property.equals("editable")) { //$NON-NLS-1$
      return "boolean";                //$NON-NLS-1$
    }
    if(_property.equals("format")) {       //$NON-NLS-1$
      return "Format|Object TRANSLATABLE"; //$NON-NLS-1$
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
         setTheValue(_value.getDouble());
         break;
       case 1 :
         defaultValueSet = true;
         defaultValue = _value.getDouble();
         setActive(false);
         reset();
         setActive(true);
         break;
       case 2 :
         textfield.setEditable(_value.getBoolean());
         break;
       case 3 :
         if(_value.getObject() instanceof java.text.DecimalFormat) {
           if(format==(java.text.DecimalFormat) _value.getObject()) {
             return;
           }
           format = (java.text.DecimalFormat) _value.getObject();
           setActive(false);
           try {
             setInternalValue(format.parse(textfield.getText()).doubleValue());
           } catch(Exception exc) {}
           setActive(true);
           textfield.setText(format.format(internalValue.value));
         }
         break;
       case 4 :                                                      // action
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         addAction(ControlElement.ACTION, _value.getString());
         break;
       case BACKGROUND :
         super.setValue(ControlSwingElement.BACKGROUND, _value);
         decideColors(getVisual().getBackground());
         break;
       default :
         super.setValue(_index-5, _value);
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
         format = defaultFormat;
         setActive(false);
         try {
           setInternalValue(format.parse(textfield.getText()).doubleValue());
         } catch(Exception exc) {}
         setActive(true);
         textfield.setText(format.format(internalValue.value));
         break;
       case 4 :
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         break;
       case BACKGROUND :
         super.setDefaultValue(ControlSwingElement.BACKGROUND);
         decideColors(getVisual().getBackground());
         break;
       default :
         super.setDefaultValue(_index-5);
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
       case 4 :
         return null;
       default :
         return super.getValue(_index-5);
    }
  }

  // -------------------------------------
  // Private methods and inner classes
  // -------------------------------------
  protected void setColor(Color aColor) {
    if(textfield.isEditable()) {
      getVisual().setBackground(aColor);
    }
  }

  protected void decideColors(Color aColor) {
    if(aColor==null) {
      return;
    }
    defaultColor = aColor;
    if(defaultColor.equals(Color.yellow)) {
      editingColor = Color.orange;
    } else {
      editingColor = Color.yellow;
    }
    if(defaultColor.equals(Color.red)) {
      errorColor = Color.magenta;
    } else {
      errorColor = Color.red;
    }
  }

  private class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent _e) {
      // System.out.println ("Action for "+textfield.getText());
      setColor(defaultColor);
      try {
        setInternalValue(format.parse(textfield.getText()).doubleValue());
      } catch(Exception exc) {
        setColor(errorColor);
        //exc.printStackTrace(System.err);
      }
    }

  }

  protected class MyKeyListener implements java.awt.event.KeyListener {
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
      // System.out.println ("Key Event "+_n+" for "+textfield.getText());
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
