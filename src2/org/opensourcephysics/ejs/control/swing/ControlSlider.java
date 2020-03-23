/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A slider to display double values. When the value is changing it
 * invokes all VARIABLE_CHANGED actions. When the value is finally set,
 * it invokes all ACTION actions.
 */
public class ControlSlider extends ControlSwingElement {
  static private final int RESOLUTION = 100000;
  static private final int VARIABLE = 0;
  protected JSlider slider;
  private DoubleValue internalValue;
  private boolean recalculate = true, defaultValueSet;
  private int ticks = 0;
  private double defaultValue, scale, minimum = 0.0, maximum = 1.0;
  private TitledBorder titledBorder;
  private EtchedBorder etchedBorder;
  private java.text.DecimalFormat format = null, ticksFormat = null;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlSlider
   * @param _visual
   */
  public ControlSlider(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JSlider) {
      slider = (JSlider) _visual;
    } else {
      slider = new JSlider();
      slider.setPaintLabels(false);
      slider.setPaintTicks(false);
      slider.setPaintTrack(true);
    }
    slider.setMinimum(0);
    slider.setMaximum(RESOLUTION);
    slider.setValue(0);
    etchedBorder = new EtchedBorder(EtchedBorder.LOWERED);
    titledBorder = new TitledBorder(etchedBorder, ""); //$NON-NLS-1$
    titledBorder.setTitleJustification(TitledBorder.CENTER);
    slider.setBorder(etchedBorder);
    defaultValue = 0.0;
    defaultValueSet = false;
    internalValue = new DoubleValue(defaultValue);
    minimum = 0.0;
    maximum = 1.0;
    scale = RESOLUTION*(maximum-minimum);
    setMaximum(maximum);
    internalValue.value = minimum+slider.getValue()/scale;
    slider.addChangeListener(new MyChangeListener());
    slider.addMouseListener(new MyMouseListener());
    return slider;
  }

  private void setTheValue(double val) {
    internalValue.value = val;
    recalculate = false;
    slider.setValue((int) ((internalValue.value-minimum)*scale));
    recalculate = true;
    if(format!=null) {
      titledBorder.setTitle(format.format(internalValue.value));
      slider.repaint();
    }
  }

  public void reset() {
    if(defaultValueSet) {
      setTheValue(defaultValue);
      variableChanged(VARIABLE, internalValue);
    }
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("variable");    //$NON-NLS-1$
      infoList.add("value");       //$NON-NLS-1$
      infoList.add("minimum");     //$NON-NLS-1$
      infoList.add("maximum");     //$NON-NLS-1$
      infoList.add("pressaction"); //$NON-NLS-1$
      infoList.add("dragaction");  //$NON-NLS-1$
      infoList.add("action");      //$NON-NLS-1$
      infoList.add("format");      //$NON-NLS-1$
      infoList.add("ticks");       //$NON-NLS-1$
      infoList.add("ticksFormat"); //$NON-NLS-1$
      infoList.add("closest");     //$NON-NLS-1$
      infoList.add("orientation"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("variable")) { //$NON-NLS-1$
      return "int|double";             //$NON-NLS-1$
    }
    if(_property.equals("value")) { //$NON-NLS-1$
      return "int|double";          //$NON-NLS-1$
    }
    if(_property.equals("minimum")) { //$NON-NLS-1$
      return "int|double";            //$NON-NLS-1$
    }
    if(_property.equals("maximum")) { //$NON-NLS-1$
      return "int|double";            //$NON-NLS-1$
    }
    if(_property.equals("pressaction")) { //$NON-NLS-1$
      return "Action CONSTANT";           //$NON-NLS-1$
    }
    if(_property.equals("dragaction")) { //$NON-NLS-1$
      return "Action CONSTANT";          //$NON-NLS-1$
    }
    if(_property.equals("action")) { //$NON-NLS-1$
      return "Action CONSTANT";      //$NON-NLS-1$
    }
    if(_property.equals("format")) {       //$NON-NLS-1$
      return "Format|Object TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("ticks")) { //$NON-NLS-1$
      return "int    BASIC";        //$NON-NLS-1$
    }
    if(_property.equals("ticksFormat")) {        //$NON-NLS-1$
      return "Format|Object BASIC TRANSLATABLE"; //$NON-NLS-1$
    }
    if(_property.equals("closest")) { //$NON-NLS-1$
      return "boolean BASIC";         //$NON-NLS-1$
    }
    if(_property.equals("orientation")) { //$NON-NLS-1$
      return "Orientation|int BASIC";     //$NON-NLS-1$
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
       case VARIABLE :
         if(internalValue.value!=_value.getDouble()) {
           setTheValue(_value.getDouble());
         }
         break;
       case 1 :
         defaultValueSet = true;
         defaultValue = _value.getDouble();
         setActive(false);
         reset();
         setActive(true);
         break;
       case 2 :
         setMinimum(_value.getDouble());
         break;
       case 3 :
         setMaximum(_value.getDouble());
         break;
       case 4 :                                                                      // pressaction
         removeAction(ControlSwingElement.ACTION_PRESS, getProperty("pressaction")); //$NON-NLS-1$
         addAction(ControlSwingElement.ACTION_PRESS, _value.getString());
         break;
       case 5 :                                                                      // dragaction
         removeAction(ControlElement.VARIABLE_CHANGED, getProperty("dragaction"));   //$NON-NLS-1$
         addAction(ControlElement.VARIABLE_CHANGED, _value.getString());
         break;
       case 6 :                                                                      // pressaction
         removeAction(ControlElement.ACTION, getProperty("action"));                 //$NON-NLS-1$
         addAction(ControlElement.ACTION, _value.getString());
         break;
       case 7 :
         if(_value.getObject() instanceof java.text.DecimalFormat) {
           if(format==(java.text.DecimalFormat) _value.getObject()) {
             return;
           }
           format = (java.text.DecimalFormat) _value.getObject();
           titledBorder.setTitle(format.format(internalValue.value));
           slider.setBorder(titledBorder);
         }
         break;
       case 8 :
         if(_value.getInteger()!=ticks) {
           ticks = _value.getInteger();
           setTicks();
         }
         break;
       case 9 :
         if(_value.getObject() instanceof java.text.DecimalFormat) {
           if(ticksFormat==(java.text.DecimalFormat) _value.getObject()) {
             return;
           }
           ticksFormat = (java.text.DecimalFormat) _value.getObject();
           slider.setPaintLabels(true);
           setTicks();
         }
         break;
       case 10 :
         slider.setSnapToTicks(_value.getBoolean());
         break;
       case 11 :
         if(slider.getOrientation()!=_value.getInteger()) {
           slider.setOrientation(_value.getInteger());
         }
         break;
       default :
         super.setValue(_index-12, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case VARIABLE :
         break;                                                                      // Do nothing
       case 1 :
         defaultValueSet = false;
         break;
       case 2 :
         setMinimum(0.0);
         break;
       case 3 :
         setMaximum(1.0);
         break;
       case 4 :
         removeAction(ControlSwingElement.ACTION_PRESS, getProperty("pressaction")); //$NON-NLS-1$
         break;
       case 5 :
         removeAction(ControlElement.VARIABLE_CHANGED, getProperty("dragaction"));   //$NON-NLS-1$
         break;
       case 6 :
         removeAction(ControlElement.ACTION, getProperty("action"));                 //$NON-NLS-1$
         break;
       case 7 :
         format = null;
         slider.setBorder(etchedBorder);
         break;
       case 8 :
         ticks = 0;
         setTicks();
         break;
       case 9 :
         ticksFormat = null;
         slider.setPaintLabels(false);
         setTicks();
         break;
       case 10 :
         slider.setSnapToTicks(false);
         break;
       case 11 :
         slider.setOrientation(SwingConstants.HORIZONTAL);
         break;
       default :
         super.setDefaultValue(_index-12);
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
       case 6 :
       case 7 :
       case 8 :
       case 9 :
       case 10 :
       case 11 :
         return null;
       default :
         return super.getValue(_index-12);
    }
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  private void setTicks() {
    if(ticks<2) {
      slider.setPaintTicks(false);
      return;
    }
    int spacing = RESOLUTION/(ticks-1);
    slider.setMinorTickSpacing(spacing);
    slider.setMajorTickSpacing(2*spacing);
    slider.setPaintTicks(true);
    if(ticksFormat!=null) {
      java.util.Hashtable<Integer, JLabel> table = new java.util.Hashtable<Integer, JLabel>();
      for(int i = 0; i<=RESOLUTION; i += 2*spacing) {
        table.put(new Integer(i), new javax.swing.JLabel(ticksFormat.format(minimum+i/scale)));
      }
      slider.setLabelTable(table);
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
    // internalValue.value = minimum;
    scale = 1.0*RESOLUTION/(maximum-minimum);
    setTicks();
    setTheValue(internalValue.value);
  }

  private void setMaximum(double val) {
    if(val==maximum) {
      return;
    }
    maximum = val;
    if(minimum>=maximum) {
      minimum = maximum-1.0;
    }
    // internalValue.value = minimum;
    scale = 1.0*RESOLUTION/(maximum-minimum);
    setTicks();
    setTheValue(internalValue.value);
  }

  // -------------------------------------
  // Inner classes
  // -------------------------------------
  private class MyChangeListener implements javax.swing.event.ChangeListener {
    public void stateChanged(javax.swing.event.ChangeEvent e) {
      if(recalculate) {
        double value = minimum+slider.getValue()/scale;
        // if (internalValue.value==value) return;
        internalValue.value = value;
        if(format!=null) {
          titledBorder.setTitle(format.format(internalValue.value));
          slider.repaint();
        }
        variableChanged(VARIABLE, internalValue);
      }
    }

  }

  private class MyMouseListener extends java.awt.event.MouseAdapter {
    public void mousePressed(java.awt.event.MouseEvent evt) {
      invokeActions(ControlSwingElement.ACTION_PRESS);
    }

    public void mouseReleased(java.awt.event.MouseEvent evt) {
      invokeActions(ControlElement.ACTION);
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
