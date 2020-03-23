/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * Abstract superclass for Drawables (children of ControlDrawableParent)
 */
public abstract class ControlDrawable extends ControlElement {
  public static final int NAME = 0;   // The name of the element // shadows superclass field
  public static final int PARENT = 1; // The parent of the element
  protected ControlDrawablesParent myParent;
  private Drawable myDrawable = null; // change with care

  /**
   * Constructor and utilities
   * @param _drawable
   */
  public ControlDrawable(Object _drawable) {
    super(_drawable);
    myDrawable = createDrawable(_drawable);
    super.myObject = myDrawable;
  }

  protected abstract Drawable createDrawable(Object drawable);

  final public Drawable getDrawable() {
    return myDrawable;
  }
  // use with care. Some may need it (like ByteRaster f.i.)

  final public void setDrawable(Drawable _dr) {
    myDrawable = _dr;
  }

  // This one is not final because of ControlZSurfacePlot, f. i.
  public void setParent(ControlDrawablesParent _dp) {
    // System.out.println ("Setting parent of "+this+" to "+_dp);
    if(myParent!=null) {
      ((DrawingPanel) myParent.getVisual()).removeDrawable(myDrawable);
      if(this instanceof NeedsPreUpdate) {
        myParent.removeFromPreupdateList((NeedsPreUpdate) this);
      }
    }
    if(_dp!=null) {
      ((DrawingPanel) _dp.getVisual()).addDrawable(myDrawable);
      ((DrawingPanel) _dp.getVisual()).render();
      if(this instanceof NeedsPreUpdate) {
        _dp.addToPreupdateList((NeedsPreUpdate) this);
      }
      myParent = _dp;
    }
  }

  final public ControlDrawablesParent getParent() {
    return myParent;
  }

  public void destroy() {
    super.destroy();
    if(myParent!=null) {
      ((DrawingPanel) myParent.getVisual()).render();
    }
  }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() { // This eliminates any previous property
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("name");   //$NON-NLS-1$
      infoList.add("parent"); //$NON-NLS-1$
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("name")) {             //$NON-NLS-1$
      return "String         CONSTANT HIDDEN"; //$NON-NLS-1$
    }
    if(_property.equals("parent")) {           //$NON-NLS-1$
      return "ControlElement CONSTANT HIDDEN"; //$NON-NLS-1$
    }
    return null;
  }

  // ------------------------------------------------
  // Variables
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case NAME :
         super.setValue(ControlElement.NAME, _value);
         break;
       case PARENT : {
         ControlElement parent = myGroup.getElement(getProperty("parent"));                                    //$NON-NLS-1$
         if(parent!=null) {
           setParent(null);
         }
         parent = myGroup.getElement(_value.toString());
         if(parent==null) {
           System.err.println(getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString()); //$NON-NLS-1$ //$NON-NLS-2$
         } else {
           if(parent instanceof ControlDrawablesParent) {
             setParent((ControlDrawablesParent) parent);
           } else {
             System.err.println(getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlDrawablesParent"); //$NON-NLS-1$ //$NON-NLS-2$
           }
         }
       }
       break;
       default : // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case NAME :
         super.setDefaultValue(ControlElement.NAME);
         break;
       case PARENT : {
         ControlElement parent = myGroup.getElement(getProperty("parent")); //$NON-NLS-1$
         if(parent!=null) {
           setParent(null);
         }
       }
       break;
       default :
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       default :
         return null;
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
