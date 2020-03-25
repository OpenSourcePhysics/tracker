/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.awt.Color;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A combobox to display string options. When the selected option changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlComboBox extends ControlSwingElement {
  static private final int VARIABLE = 0;
  //  static private final int BACKGROUND = ControlSwingElement.BACKGROUND+6; // shadows superclass field, also unused
  static private final int FOREGROUND = ControlSwingElement.FOREGROUND+6; // shadows superclass field
  protected JComboBox combo;
  private java.awt.Component editorComponent;
  private String optionsString;
  private StringValue internalValue;
  private boolean defaultValueSet, defaultEditable, doNotUpdate = false;
  private String defaultValue;
  private Color defaultColor, editingColor;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlComboBox
   * @param _visual
   */
  public ControlComboBox(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JComboBox) {
      combo = (JComboBox) _visual;
    } else {
      combo = new JComboBox();
    }
    defaultEditable = combo.isEditable();
    combo.addActionListener(new MyActionListener());
    editorComponent = combo.getEditor().getEditorComponent();
    editorComponent.addKeyListener(new MyKeyListener());
    defaultValue = ""; //$NON-NLS-1$
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
    decideColors(editorComponent.getBackground());
    return combo;
  }

  public void reset() {
    if(defaultValueSet) {
      setTheValue(defaultValue);
      setInternalValue(defaultValue);
    }
  }

  private void setTheValue(String _value) {
    if((internalValue.value!=null)&&internalValue.value.equals(_value)) {
      return;
    }
    combo.setSelectedItem(internalValue.value = _value);
    setColor(defaultColor);
  }

  private void setInternalValue(String _value) {
    internalValue.value = _value;
    variableChanged(VARIABLE, internalValue);
    invokeActions();
  }

  private void setTheOptions(String _options) {
    if(_options==null) {
      if(optionsString!=null) {
        combo.removeAllItems();
        optionsString = null;
      }
      return;
    }
    if(_options.equals(optionsString)) {
      return;
    }
    doNotUpdate = true;
    combo.removeAllItems();
    StringTokenizer tkn = new StringTokenizer(_options, ";"); //$NON-NLS-1$
    while(tkn.hasMoreTokens()) {
      combo.addItem(tkn.nextToken());
    }
    optionsString = _options;
    doNotUpdate = false;
    if(combo.getItemCount()>0) {
      setTheValue(combo.getItemAt(0).toString());
    }
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("variable");       //$NON-NLS-1$
      infoList.add("options");        //$NON-NLS-1$
      infoList.add("value");          //$NON-NLS-1$
      infoList.add("editable");       //$NON-NLS-1$
      infoList.add("editBackground"); //$NON-NLS-1$
      infoList.add("action");         //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("variable")) {   //$NON-NLS-1$
      return "String VARIABLE_EXPECTED"; //$NON-NLS-1$
    }
    if(_property.equals("options")) {        //$NON-NLS-1$
      return "String PREVIOUS TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("value")) { //$NON-NLS-1$
      return "String CONSTANT";     //$NON-NLS-1$
    }
    if(_property.equals("editable")) { //$NON-NLS-1$
      return "boolean";                //$NON-NLS-1$
    }
    if(_property.equals("editBackground")) { //$NON-NLS-1$
      return "Color|Object";                 //$NON-NLS-1$
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
         setTheOptions(_value.getString());
         break;
       case 2 :
         defaultValueSet = true;
         defaultValue = _value.getString();
         setActive(false);
         reset();
         setActive(true);
         break;
       case 3 :
         combo.setEditable(_value.getBoolean());
         break;
       case 4 :
         if(_value.getObject() instanceof Color) {
           editorComponent.setBackground((Color) _value.getObject());
         }
         decideColors(editorComponent.getBackground());
         break;
       case 5 :                                                      // action
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         addAction(ControlElement.ACTION, _value.getString());
         break;
       default :
         super.setValue(_index-6, _value);
         break;
       case FOREGROUND :
         super.setValue(ControlSwingElement.FOREGROUND, _value);
         if(_value.getObject() instanceof Color) {
           editorComponent.setForeground((Color) _value.getObject());
         }
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case VARIABLE :
         break;                                                      // Do nothing
       case 1 :
         setTheOptions(null);
         break;
       case 2 :
         defaultValueSet = false;
         break;
       case 3 :
         combo.setEditable(defaultEditable);
         break;
       case 4 :
         editorComponent.setBackground(Color.white);
         decideColors(editorComponent.getBackground());
         break;
       case 5 :
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         break;
       default :
         super.setDefaultValue(_index-6);
         break;
       case FOREGROUND :
         super.setDefaultValue(ControlSwingElement.FOREGROUND);
         editorComponent.setForeground(Color.black);
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
       case 5 :
         return null;
       default :
         return super.getValue(_index-6);
    }
  }

  // -------------------------------------
  // Private methods and inner classes
  // -------------------------------------
  private void setColor(Color aColor) {
    if(combo.isEditable()) {
      editorComponent.setBackground(aColor);
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
      if(doNotUpdate) {
        return;
      }
      setInternalValue((String) combo.getSelectedItem());
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
      if(!combo.isEditable()) {
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
