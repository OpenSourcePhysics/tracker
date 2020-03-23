/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JLabel;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable Label. It has no internal value, nor can trigger
 * any action.
 */
public class ControlLabel extends ControlSwingElement {
  protected JLabel label;
  private String imageFile = null;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlLabel
   * @param _visual
   */
  public ControlLabel(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JLabel) {
      label = (JLabel) _visual;
    } else {
      label = new JLabel();
    }
    return label;
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
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         label.setText(_value.getString());
         break;    // text
       case 1 :    // image
         if(_value.getString().equals(imageFile)) {
           return; // no need to do it again
         }
         label.setIcon(getIcon(imageFile = _value.getString()));
         break;
       case 2 :
         label.setHorizontalAlignment(_value.getInteger());
         break;    // alignment
       default :
         super.setValue(_index-3, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         label.setText(""); //$NON-NLS-1$
         break;
       case 1 :
         label.setIcon(null);
         imageFile = null;
         break;
       case 2 :
         label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         break;
       default :
         super.setDefaultValue(_index-3);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
       case 1 :
       case 2 :
         return null;
       default :
         return super.getValue(_index-3);
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
