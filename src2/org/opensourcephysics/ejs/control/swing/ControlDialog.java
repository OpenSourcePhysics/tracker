/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JDialog;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable Dialog.
 */
public class ControlDialog extends ControlWindow {
  protected JDialog dialog;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlDialog
   * @param _visual
   */
  public ControlDialog(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    return createDialog(_visual, null);
  }

  // This is a very special case
  public void replaceVisual(java.awt.Frame _owner) {
    myVisual = createDialog(null, _owner);
  }

  // This is a very special case
  private java.awt.Component createDialog(Object _visual, java.awt.Frame _owner) {
    startingup = true;
    if(_visual instanceof JDialog) {
      dialog = (JDialog) _visual;
    } else {
      if(_owner!=null) {
        dialog = new JDialog(_owner);
      } else {
        dialog = new JDialog();
      }
      dialog.getContentPane().setLayout(new java.awt.BorderLayout());
    }
    internalValue = new BooleanValue(true);
    // setProperty ("visible","true");
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        internalValue.value = false;
        variableChanged(ControlWindow.VISIBLE+2, internalValue);
      }

    });
    return dialog.getContentPane();
  }

  public java.awt.Component getComponent() {
    return dialog;
  }

  public java.awt.Container getContainer() {
    return dialog.getContentPane();
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("title");     //$NON-NLS-1$
      infoList.add("resizable"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("title")) { //$NON-NLS-1$
      return "String TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("resizable")) { //$NON-NLS-1$
      return "boolean BASIC";           //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :                                               // title
         String ejsWindow = getProperty("_ejs_window_");      //$NON-NLS-1$
         if(ejsWindow!=null) {
           dialog.setTitle(_value.getString()+" "+ejsWindow); //$NON-NLS-1$
         } else {
           dialog.setTitle(_value.getString());
         }
         break;
       case 1 :
         dialog.setResizable(_value.getBoolean());
         break;
       default :
         super.setValue(_index-2, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :                                          // title
         String ejsWindow = getProperty("_ejs_window_"); //$NON-NLS-1$
         if(ejsWindow!=null) {
           dialog.setTitle(ejsWindow);
         } else {
           dialog.setTitle("");                          //$NON-NLS-1$
         }
         break;
       case 1 :
         dialog.setResizable(true);
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
