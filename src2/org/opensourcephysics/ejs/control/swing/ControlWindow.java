/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import org.opensourcephysics.ejs.control.NeedsUpdate;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable Window. Base class for Frame and Dialog
 */
public abstract class ControlWindow extends ControlContainer implements NeedsUpdate {
  static public final int NAME = ControlSwingElement.NAME+3;       // shadows superclass field
  static public final int VISIBLE = ControlSwingElement.VISIBLE+3; // shadows superclass field
  static private final int SIZE = ControlSwingElement.SIZE+3;      // shadows superclass field
  protected BooleanValue internalValue;
  private LayoutManager myLayout = null;
  private Point myLocation = null;
  private Dimension mySize = null;
  protected boolean waitForReset = false, startingup = true, shouldShow = true;

  /**
   * Constructor ControlWindow
   * @param _visual
   */
  public ControlWindow(Object _visual) {
    super(_visual);
  }

  public void dispose() {
    ((Window) getComponent()).dispose();
  }

  public void show() {
    // ((Window) getComponent()).show();
    if(startingup) {
      shouldShow = true;
      if(waitForReset) {
        return;
      }
    }
    Window w = (Window) getComponent();
    if(w.isShowing()) {
      // empty  // System.out.println("Window "+this+" is showing "+w.isShowing());
    } else {
      w.setVisible(true);
    }
  }

  public void hide() {
    // ((Window) getComponent()).show();
    if(startingup) {
      shouldShow = false;
      if(waitForReset) {
        return;
      }
    }
    Window w = (Window) getComponent();
    if(w.isShowing()) {
      w.setVisible(false);
    }
  }

  public void destroy() {
    dispose();
    super.destroy();
  }

  public void setWaitForReset(boolean _option) {
    waitForReset = _option;
    if(waitForReset) {
      ((Window) getComponent()).setVisible(false);
    }
  }

  public void reset() {
    startingup = false;
    if(shouldShow) {
      show(); // ((Window) getComponent()).show();
    } else {
      hide(); // ((Window) getComponent()).hide();
    }
    super.reset();
  }

  public void update() { // Ensure it will be updated
    startingup = false;
    // super.update();
  }

  public void adjustSize() { // overrides its super
    String size = getProperty("size"); //$NON-NLS-1$
    ((Window) getComponent()).validate();
    if((size!=null)&&size.trim().toLowerCase().equals("pack")) { //$NON-NLS-1$
      ((Window) getComponent()).pack();
    } else {
      super.adjustSize();
    }
  }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("layout");       //$NON-NLS-1$
      infoList.add("location");     //$NON-NLS-1$
      infoList.add("waitForReset"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("location")) { //$NON-NLS-1$
      return "Point|Object";           //$NON-NLS-1$
    }
    if(_property.equals("layout")) { //$NON-NLS-1$
      return "Layout|Object";        //$NON-NLS-1$
    }
    if(_property.equals("waitForReset")) { //$NON-NLS-1$
      return "boolean HIDDEN";             //$NON-NLS-1$
    }
    if(_property.equals("tooltip")) { //$NON-NLS-1$
      return "String HIDDEN";         //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :                                                                   // layout
         if(_value.getObject() instanceof LayoutManager) {
           LayoutManager layout = (LayoutManager) _value.getObject();
           if(layout!=myLayout) {
             getContainer().setLayout(myLayout = layout);
           }
           ((Container) getComponent()).validate();
         }
         break;
       case 1 :                                                                   // location
         if(_value.getObject() instanceof Point) {
           Point pos = (Point) _value.getObject();
           if(pos.equals(myLocation)) {
             return;
           }
           getComponent().setLocation(myLocation = pos);
         }
         break;
       case 2 :
         setWaitForReset(_value.getBoolean());
         break;
       case VISIBLE :                                                             // Overrides its super 'visible'
         internalValue.value = _value.getBoolean();
         if(internalValue.value) {
           show();
         } else {
           hide();
         }
         break;
       case SIZE :                                                                // // Overrides its super 'size'
         java.awt.Dimension size = null;
         if((_value instanceof StringValue)&&"pack".equals(_value.getString())) { //$NON-NLS-1$
           ((Window) getComponent()).pack();
           size = getComponent().getSize();
         } else if(_value.getObject() instanceof Dimension) {
           size = (Dimension) _value.getObject();
           if(size.equals(mySize)) {
             return;
           }
           ((javax.swing.JComponent) getContainer()).setPreferredSize(mySize = size);
           ((Container) getComponent()).validate();
           ((Window) getComponent()).pack();
         } else {
           return;
         }
         String loc = getProperty("location");                                    //$NON-NLS-1$
         if((loc!=null)&&(loc.trim().toLowerCase().equals("center"))) {           //$NON-NLS-1$
           Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
           getComponent().setLocation((dim.width-size.width)/2, (dim.height-size.height)/2);
         }
         break;
       default :
         super.setValue(_index-3, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         getContainer().setLayout(myLayout = new BorderLayout());
         ((Container) getComponent()).validate();
         break;
       case 1 :
         getComponent().setLocation(myLocation = new Point(0, 0));
         break;
       case 2 :
         setWaitForReset(false);
         break;
       case VISIBLE :                                                   // Overrides its super 'visible'
         internalValue.value = true;
         show();
         break;
       case SIZE :                                                      // // Overrides its super 'size'
         ((Window) getComponent()).pack();
         Dimension size = getComponent().getSize();
         String loc = getProperty("location");                          //$NON-NLS-1$
         if((loc!=null)&&(loc.trim().toLowerCase().equals("center"))) { //$NON-NLS-1$
           Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
           getComponent().setLocation((dim.width-size.width)/2, (dim.height-size.height)/2);
         }
         break;
       default :
         super.setDefaultValue(_index-3);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
         return internalValue;
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
