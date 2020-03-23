/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurabel TabbedPanel
 */
public class ControlTabbedPanel extends ControlContainer {
  protected JTabbedPane tabbedpanel;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlTabbedPanel
   * @param _visual
   */
  public ControlTabbedPanel(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JTabbedPane) {
      tabbedpanel = (JTabbedPane) _visual;
    } else {
      tabbedpanel = new JTabbedPane(SwingConstants.TOP);
    }
    return tabbedpanel;
  }

  public void add(ControlElement _child) {
    String header = _child.getProperty("name"); //$NON-NLS-1$
    if(header!=null) {
      tabbedpanel.add(_child.getComponent(), header);
    } else {
      tabbedpanel.add(_child.getComponent(), "   "); //$NON-NLS-1$
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
      infoList.add("placement"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("placement")) { //$NON-NLS-1$
      return "Placement|int";           //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         if(tabbedpanel.getTabPlacement()!=_value.getInteger()) {
           tabbedpanel.setTabPlacement(_value.getInteger());
         }
         break;
       default :
         super.setValue(_index-1, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         tabbedpanel.setTabPlacement(javax.swing.SwingConstants.TOP);
         break;
       default :
         super.setDefaultValue(_index-1);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
         return null;
       default :
         return super.getValue(_index-1);
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
