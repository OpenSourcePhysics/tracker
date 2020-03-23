/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.text.DecimalFormat;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A bar that display double values. The value cannot be changed
 */
public class ControlBar extends ControlSwingElement {
  static private final int RESOLUTION = 100000;
  protected JProgressBar bar;
  private double scale, minimum = 0.0, maximum = 1.0, variable = 0.0;
  private DecimalFormat format;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlBar
   * @param _visual
   */
  public ControlBar(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JProgressBar) {
      bar = (JProgressBar) _visual;
    } else {
      bar = new JProgressBar(SwingConstants.HORIZONTAL);
      bar.setBorderPainted(true);
      bar.setStringPainted(false);
    }
    bar.setMinimum(0);
    bar.setMaximum(RESOLUTION);
    minimum = 0.0;
    maximum = 1.0;
    variable = bar.getValue();
    scale = RESOLUTION*(maximum-minimum);
    format = null;
    bar.setValue((int) ((variable-minimum)*scale));
    return bar;
  }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("variable");    //$NON-NLS-1$
      infoList.add("minimum");     //$NON-NLS-1$
      infoList.add("maximum");     //$NON-NLS-1$
      infoList.add("format");      //$NON-NLS-1$
      infoList.add("orientation"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("variable")) { //$NON-NLS-1$
      return "int|double";             //$NON-NLS-1$
    }
    if(_property.equals("minimum")) { //$NON-NLS-1$
      return "int|double";            //$NON-NLS-1$
    }
    if(_property.equals("maximum")) { //$NON-NLS-1$
      return "int|double";            //$NON-NLS-1$
    }
    if(_property.equals("format")) {       //$NON-NLS-1$
      return "Format|Object TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("orientation")) { //$NON-NLS-1$
      return "Orientation|int";           //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         setValue(_value.getDouble());
         break;
       case 1 :
         setMinimum(_value.getDouble());
         break;
       case 2 :
         setMaximum(_value.getDouble());
         break;
       case 3 : {
         DecimalFormat newFormat;
         if(_value.getObject() instanceof DecimalFormat) {
           newFormat = (DecimalFormat) _value.getObject();
         } else {
           newFormat = null;
         }
         if(format==newFormat) {
           return; // and save time
         }
         format = newFormat;
         if(format!=null) {
           bar.setString(format.format(variable));
           bar.setStringPainted(true);
         } else {
           bar.setStringPainted(false);
         }
       }
       break;
       case 4 :
         if(bar.getOrientation()!=_value.getInteger()) {
           bar.setOrientation(_value.getInteger());
         }
         break;
       default :
         super.setValue(_index-5, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         break; // Do nothing
       case 1 :
         setMinimum(0.0);
         break;
       case 2 :
         setMaximum(1.0);
         break;
       case 3 :
         format = null;
         bar.setStringPainted(false);
         break;
       case 4 :
         bar.setOrientation(SwingConstants.HORIZONTAL);
         break;
       default :
         super.setDefaultValue(_index-5);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
       case 1 :
       case 2 :
       case 3 :
       case 4 :
         return null;
       default :
         return super.getValue(_index-5);
    }
  }

  // -------------- private methods -----------
  private void setValue(double val) {
    if(val==variable) {
      return;
    }
    variable = val;
    bar.setValue((int) ((variable-minimum)*scale));
    if(format!=null) {
      bar.setString(format.format(variable));
    }
  }

  private void setMinimum(double val) {
    if(val==minimum) {
      return;
    }
    minimum = val;
    if(minimum>=maximum) {
      maximum = minimum+1.0;
    }
    scale = 1.0*RESOLUTION/(maximum-minimum);
    bar.setValue((int) ((variable-minimum)*scale));
    if(format!=null) {
      bar.setString(format.format(variable));
    }
  }

  private void setMaximum(double val) {
    if(val==maximum) {
      return;
    }
    maximum = val;
    if(minimum>=maximum) {
      minimum = maximum-1.0;
    }
    scale = 1.0*RESOLUTION/(maximum-minimum);
    bar.setValue((int) ((variable-minimum)*scale));
    if(format!=null) {
      bar.setString(format.format(variable));
    }
  }

}

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
