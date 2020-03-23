/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.Utils;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * <code>ControlSwingElement</code> is a base class for an object that
 * displays a visual java.awt.Component.
 * <p>
 * @see     java.awt.Component
 * @see     org.opensourcephysics.ejs.control.ControlElement
 */
public abstract class ControlSwingElement extends ControlElement {
  // Important: if you change the order of the properties you must up date
  // these constants accordingly!!!
  // These constants are her for use of any subclass that overrides the
  // setValue() method for any of these properties
  static public final int NAME = 0;                   // The name of the element // shadows superclass field
  static public final int POSITION = 1;               // The position in its parent
  static public final int PARENT = 2;                 // Its parent
  static public final int ENABLED = 3;                // Whetehr it is responsive or not
  static public final int VISIBLE = 4;                // The visibility
  static public final int SIZE = 5;                   // The size
  static public final int FOREGROUND = 6;             // The foreground color
  static public final int BACKGROUND = 7;             // The background color
  static public final int FONT = 8;                   // The font
  static public final int TOOLTIP = 9;                // The tooltip
  // Particular types of actions
  static public final int ACTION_PRESS = 10;
  static public final int ACTION_ON = 20;
  static public final int ACTION_OFF = 21;
  static private ArrayList<String> myInfoList = null; // The list of registered properties
  protected Component myVisual;                       // The visual element to display
  private Color myDefaultBkgd = null, myDefaultFrgd = null;
  private Font myDefaultFont = null;
  private Dimension mySize = null;

  // ------------------------------------------------
  // Static constants and constructor
  // ------------------------------------------------
  // public ControlSwingElement() { this(null); }

  /**
   * Instantiates an object that wrapps a Swing JComponent of this type.
   * If an object of an appropriate class is provided, it is used as
   * the visual.
   * @param _visual The javax.swing.JComponent to be wrapped
   */
  public ControlSwingElement(Object _visual) {
    super(_visual);
    myVisual = createVisual(_visual);
    super.myObject = myVisual;
    myDefaultFrgd = myVisual.getForeground();
    myDefaultBkgd = myVisual.getBackground();
    myDefaultFont = myVisual.getFont();
    if(myVisual instanceof JComponent) {
      mySize = ((JComponent) myVisual).getPreferredSize();
    }
  }

  // ------------------------------------------------
  // Visual components
  // ------------------------------------------------

  /**
   * Creates the visual component of this <code>ControlElement</code>,
   * the one you can configure graphically.
   * If an object of an appropriate class is provided, it is used as
   * the visual.
   * @param _visual The javax.swing.JComponent to be wrapped
   */
  abstract protected Component createVisual(Object _visual);

  /**
   * Returns the visual component of this <code>ControlElement</code>,
   * the one you can configure graphically.
   */
  final public Component getVisual() {
    return myVisual;
  }

  /**
   * Returns the component of this <code>ControlElement</code>,
   * the one that is added to a container.
   */
  public Component getComponent() {
    return myVisual;
  }
  // This one is not final becuase, although this is the usual behaviour,
  // there are exceptions. F. i., when embedding the visual into a JScrollPane

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------

  /**
   * Returns the list of all properties that can be set for this
   * ControlElement.
   * Subclasses that add properties should extend this table.
   * Order is crucial here: Both for the presentation in an editor (f.i. ViewElement)
   * and for the setValue() method.
   */
  // Important: Order is crucial!!! if you change the order of the properties
  // you must up date the constants at the beginning of this file accordingly!!!
  public ArrayList<String> getPropertyList() {
    if(myInfoList==null) {
      myInfoList = new ArrayList<String>();
      myInfoList.add("name");       //$NON-NLS-1$
      myInfoList.add("position");   //$NON-NLS-1$
      myInfoList.add("parent");     //$NON-NLS-1$
      myInfoList.add("enabled");    //$NON-NLS-1$
      myInfoList.add("visible");    //$NON-NLS-1$
      myInfoList.add("size");       //$NON-NLS-1$
      myInfoList.add("foreground"); //$NON-NLS-1$
      myInfoList.add("background"); //$NON-NLS-1$
      myInfoList.add("font");       //$NON-NLS-1$
      myInfoList.add("tooltip");    //$NON-NLS-1$
    }
    return myInfoList;
  }

  /**
   * Returns information about a given property.
   * Subclasses that add properties should extend this table.
   * <ll>
   *   <li> The first keyword is ALWAYS the type.
   *   <li> The keyword <b>CONSTANT</b> applies to properties that can not be
   *     changed using the setValue() methods
   *   <li> The keyword <b>BASIC</b> is used by Ejs to group properties to the left
   *     hand side of the property editor
   *   <li> The keyword <b>HIDDEN</b> is used by Ejs so that it does not display
   *     an entry in the editor field
   *  </ll>
   */
  // Order in the implementation is irrelevant.
  public String getPropertyInfo(String _property) {
    if(_property.equals("name")) {             //$NON-NLS-1$
      return "String         CONSTANT HIDDEN"; //$NON-NLS-1$
    }
    if(_property.equals("position")) {                   //$NON-NLS-1$
      return "Position       CONSTANT PREVIOUS HIDDEN "; //$NON-NLS-1$
    }
    if(_property.equals("parent")) {           //$NON-NLS-1$
      return "ControlElement CONSTANT HIDDEN"; //$NON-NLS-1$
    }
    if(_property.equals("enabled")) {         //$NON-NLS-1$
      return "boolean          BASIC HIDDEN"; //$NON-NLS-1$
    }
    if(_property.equals("visible")) {         //$NON-NLS-1$
      return "boolean          BASIC HIDDEN"; //$NON-NLS-1$
    }
    if(_property.equals("size")) {     //$NON-NLS-1$
      return "Dimension|Object BASIC"; //$NON-NLS-1$
    }
    if(_property.equals("foreground")) { //$NON-NLS-1$
      return "Color|Object     BASIC";   //$NON-NLS-1$
    }
    if(_property.equals("background")) { //$NON-NLS-1$
      return "Color|Object     BASIC";   //$NON-NLS-1$
    }
    if(_property.equals("font")) {     //$NON-NLS-1$
      return "Font|Object      BASIC"; //$NON-NLS-1$
    }
    if(_property.equals("tooltip")) {               //$NON-NLS-1$
      return "String           BASIC TRANSLATABLE"; //$NON-NLS-1$
    }
    return null;
  }

  /**
   * Checks if a value can be considered a valid constant value for a property
   * If not, it returns null, meaning the value can be considered to be
   * a GroupVariable or a primitive constant.
   * This method implements more cases than really needed for the base class.
   * This is in order to save repetitions in swing subclasses.
   * @param     String _property The property name
   * @param     String _value The proposed value for the property
   */
  public Value parseConstant(String _propertyType, String _value) {
    if(_value==null) {
      return null;
    }
    Value constantValue;
    if(_propertyType.indexOf("Alignment")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.alignmentConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Dimension")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.dimensionConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Layout")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.layoutConstant(((ControlContainer) this).getContainer(), _value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Orientation")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.orientationConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Placement")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.placementConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    if(_propertyType.indexOf("Point")>=0) { //$NON-NLS-1$
      constantValue = ConstantParser.pointConstant(_value);
      if(constantValue!=null) {
        return constantValue;
      }
    }
    return super.parseConstant(_propertyType, _value);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------

  /**
   * Sets the value of the registered variables.
   * Subclasses with internal values should extend this
   * @param _index   A keyword index that distinguishes among variables
   * @param _value The object holding the value for the variable.
   */
  public void setValue(int _index, Value _value) {
    // System.out.println ("Setting property #"+_index+" to "+_value.toString());
    switch(_index) {
       case NAME :
         super.setValue(ControlElement.NAME, _value);
         getComponent().setName(_value.toString());                                                              // Added for WC on 051209
         break;
       case POSITION : {
         ControlElement parent = myGroup.getElement(getProperty("parent"));                                      //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).remove(this);
         }
         myPropertiesTable.put("position", _value.toString());                                                   //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).add(this);
         }
       }
       break;
       case PARENT : {
         ControlElement parent = myGroup.getElement(getProperty("parent"));                                      //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).remove(this);
         }
         parent = myGroup.getElement(_value.toString());
         if(parent==null) {
           if(!(this instanceof ControlWindow)) {
             System.err.println(getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString()); //$NON-NLS-1$ //$NON-NLS-2$
           }
         } else {
           if(parent instanceof ControlContainer) {
             ((ControlContainer) parent).add(this);
           } else {
             System.err.println(getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlContainer"); //$NON-NLS-1$ //$NON-NLS-2$
           }
         }
       }
       break;
       case ENABLED :
         getVisual().setEnabled(_value.getBoolean());
         break;                                                                      // enabled
       case VISIBLE :
         getVisual().setVisible(_value.getBoolean());
         break;                                                                      // visible
       case SIZE :                                                                   // Size (myVisual is necessarily a JComponent)
         if(getComponent() instanceof JComponent) {
           Dimension size = (Dimension) _value.getObject();
           if((size.width==mySize.width)&&(size.height==mySize.height)) {
             return;                                                                 // Do not waste time
           }
           ((JComponent) getComponent()).setPreferredSize(mySize = size);
           if(this instanceof ControlContainer) {
             ((ControlContainer) this).getContainer().validate();
           }
           ControlElement parentElement = myGroup.getElement(getProperty("parent")); //$NON-NLS-1$
           if(parentElement!=null) {
             ((ControlContainer) parentElement).adjustSize();
           }
         }
         break;
       case FOREGROUND :                                                             // Foreground (not much time is wasted if the color is the same)
         if(_value.getObject() instanceof Color) {
           getVisual().setForeground((Color) _value.getObject());
         }
         break;
       case BACKGROUND :                                                             // Background
         if(_value.getObject() instanceof Color) {
           getVisual().setBackground((Color) _value.getObject());
         }
         break;
       case FONT :                                                                   // Font
         if(_value.getObject() instanceof Font) {
           getVisual().setFont((Font) _value.getObject());
         }
         break;
       case TOOLTIP :                                                                // Tooltip
         if(getVisual() instanceof JComponent) {
           ((JComponent) getVisual()).setToolTipText(_value.toString());
         }
         break;
       default :                                                                     // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue(int _index) {
    // System.out.println ("Setting default value for property #"+_index);
    switch(_index) {
       case NAME :
         super.setDefaultValue(ControlElement.NAME);
         break;
       case POSITION : {
         ControlElement parent = myGroup.getElement(getProperty("parent"));          //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).remove(this);
         }
         myPropertiesTable.remove("position");                                       //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).add(this);
         }
       }
       break;
       case PARENT : {
         ControlElement parent = myGroup.getElement(getProperty("parent"));          //$NON-NLS-1$
         if((parent!=null)&&(parent instanceof ControlContainer)) {
           ((ControlContainer) parent).remove(this);
         }
       }
       break;
       case ENABLED :
         getVisual().setEnabled(true);
         break;
       case VISIBLE :
         getVisual().setVisible(true);
         break;
       case SIZE :                                                                   // Size (getComponent() is necessarily a JComponent)
         if(getComponent() instanceof JComponent) {
           ((JComponent) getComponent()).setPreferredSize(null);                     // ask the UI for the default
           if(this instanceof ControlContainer) {
             ((ControlContainer) this).getContainer().validate();
           }
           ControlElement parentElement = myGroup.getElement(getProperty("parent")); //$NON-NLS-1$
           if(parentElement!=null) {
             ((ControlContainer) parentElement).adjustSize();
           }
         }
         break;
       case FOREGROUND :
         getVisual().setForeground(myDefaultFrgd);
         break;
       case BACKGROUND :
         getVisual().setBackground(myDefaultBkgd);
         break;
       case FONT :
         getVisual().setFont(myDefaultFont);
         break;
       case TOOLTIP :
         if(getComponent() instanceof JComponent) {
           ((JComponent) getVisual()).setToolTipText(null);
         }
         break;
       default :
         break;
    }
  }

  /**
   * Gets the value of any internal variable.
   * Subclasses with internal values should extend this
   * @param _index   A keyword index that distinguishes among variables
   * @return Value _value The object holding the value for the variable.
   */
  public Value getValue(int _index) {
    return null; // Any of these properties can be modified by the element
  }

  // ------------------------------------------------
  // A utility for subclasses that require an icon
  // ------------------------------------------------
  protected javax.swing.ImageIcon getIcon(String _iconFile) {
    javax.swing.ImageIcon icon;
    if(getProperty("_ejs_codebase")!=null) {                      //$NON-NLS-1$
      icon = Utils.icon(getProperty("_ejs_codebase"), _iconFile); //$NON-NLS-1$
    } else if((getSimulation()!=null)&&(getSimulation().getCodebase()!=null)) {
      icon = Utils.icon(getSimulation().getCodebase().toString(), _iconFile);
    } else {
      icon = Utils.icon(null, _iconFile);
    }
    return icon;
  }

} // End of Class

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
