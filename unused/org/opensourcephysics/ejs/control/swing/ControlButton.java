/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JButton;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable control button. It will trigger an action when clicked.
 * It has no internal value.
 */
public class ControlButton extends ControlSwingElement {
  protected JButton button;
  private String imageFile = null;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlButton
   * @param _visual
   */
  public ControlButton(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JButton) {
      button = (JButton) _visual;
    } else {
      button = new JButton();
    }
    button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent _e) {
        invokeActions();
      }

    });
    return button;
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("text");      //$NON-NLS-1$
      infoList.add("image");     //$NON-NLS-1$
      infoList.add("alignment"); //$NON-NLS-1$
      infoList.add("action");    //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("text")) {             //$NON-NLS-1$
      return "String NotTrimmed TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("image")) { //$NON-NLS-1$
      return "File|String";         //$NON-NLS-1$
    }
    if(_property.equals("alignment")) { //$NON-NLS-1$
      return "Alignment|int";           //$NON-NLS-1$
    }
    if(_property.equals("action")) { //$NON-NLS-1$
      return "Action CONSTANT";      //$NON-NLS-1$
    }
    if(_property.equals("enabled")) { //$NON-NLS-1$
      return "boolean";               // Not hidden //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         button.setText(_value.getString());
         break;                                                      // text
       case 1 :                                                      // image
         if(_value.getString().equals(imageFile)) {
           return;                                                   // no need to do it again
         }
         button.setIcon(getIcon(imageFile = _value.getString()));
         break;
       case 2 :
         button.setHorizontalAlignment(_value.getInteger());
         break;                                                      // alignment
       case 3 :                                                      // action
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         addAction(ControlElement.ACTION, _value.getString());
         break;
       default :
         super.setValue(_index-4, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         button.setText("");                                         //$NON-NLS-1$
         break;
       case 1 :
         imageFile = null;
         button.setIcon(null);
         break;
       case 2 :
         button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         break;
       case 3 :
         removeAction(ControlElement.ACTION, getProperty("action")); //$NON-NLS-1$
         break;
       default :
         super.setDefaultValue(_index-4);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
       case 1 :
       case 2 :
       case 3 :
         return null;
       default :
         return super.getValue(_index-4);
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
