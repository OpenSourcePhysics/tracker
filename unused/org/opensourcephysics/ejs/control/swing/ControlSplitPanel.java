/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JSplitPane;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable SplitPanel
 */
public class ControlSplitPanel extends ControlContainer {
  protected JSplitPane splitpanel;
  private boolean hasOne = false;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlSplitPanel
   * @param _visual
   */
  public ControlSplitPanel(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JSplitPane) {
      splitpanel = (JSplitPane) _visual;
    } else {
      splitpanel = new JSplitPane();
      splitpanel.setOneTouchExpandable(true);
      splitpanel.setDividerLocation(-1);
    }
    return splitpanel;
  }

  public void reset() {
    splitpanel.setDividerLocation(-1);
  }

  public void add(ControlElement _child) {
    if(hasOne) {
      splitpanel.setBottomComponent(_child.getComponent());
      splitpanel.setDividerLocation(-1);
    } else {
      splitpanel.setTopComponent(_child.getComponent());
      splitpanel.setDividerLocation(-1);
      hasOne = true;
    }
    if(_child instanceof ControlRadioButton) {
      radioButtons.add(_child);
      ((ControlRadioButton) _child).setParent(this);
    }
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("orientation"); //$NON-NLS-1$
      infoList.add("expandable");  //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("orientation")) { //$NON-NLS-1$
      return "Orientation|int";           //$NON-NLS-1$
    }
    if(_property.equals("expandable")) { //$NON-NLS-1$
      return "boolean";                  //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant(String _propertyType, String _value) {
    if(_value==null) {
      return null;
    }
    if(_propertyType.indexOf("Orientation")>=0) { //$NON-NLS-1$
      _value = _value.trim().toLowerCase();
      if(_value.equals("vertical")) {             //$NON-NLS-1$
        return new IntegerValue(JSplitPane.VERTICAL_SPLIT);
      } else if(_value.equals("horizontal")) {    //$NON-NLS-1$
        return new IntegerValue(JSplitPane.HORIZONTAL_SPLIT);
      }
    }
    return super.parseConstant(_propertyType, _value);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         if(splitpanel.getOrientation()!=_value.getInteger()) {
           splitpanel.setOrientation(_value.getInteger());
         }
         break;
       case 1 :
         splitpanel.setOneTouchExpandable(_value.getBoolean());
         break;
       default :
         super.setValue(_index-2, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         splitpanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
         break;
       case 1 :
         splitpanel.setOneTouchExpandable(true);
         break;
       default :
         super.setDefaultValue(_index-2);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
       case 1 :
         return null;
       default :
         return super.getValue(_index-2);
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
