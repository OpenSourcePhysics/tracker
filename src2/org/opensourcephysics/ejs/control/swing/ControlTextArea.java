/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
// import org.opensourcephysics.ejs.control.ControlElement;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable text. It has no internal value, nor can trigger
 * any action.
 */
public class ControlTextArea extends ControlSwingElement {
  static String _RETURN_ = System.getProperty("line.separator"); //$NON-NLS-1$
  protected JTextArea textarea;
  private javax.swing.JScrollPane panel;
  private TitledBorder titledBorder;
  private EtchedBorder etchedBorder;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlTextArea
   * @param _visual
   */
  public ControlTextArea(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JTextArea) {
      textarea = (JTextArea) _visual;
    } else {
      textarea = new JTextArea(5, 5);
      textarea.setEditable(false);
    }
    panel = new javax.swing.JScrollPane(textarea);
    etchedBorder = new EtchedBorder(EtchedBorder.LOWERED);
    titledBorder = new TitledBorder(etchedBorder, ""); //$NON-NLS-1$
    titledBorder.setTitleJustification(TitledBorder.CENTER);
    panel.setBorder(etchedBorder);
    return textarea;
  }

  public java.awt.Component getComponent() {
    return panel;
  }

  public void reset() {
    textarea.setText(""); //$NON-NLS-1$
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("title"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("title")) { //$NON-NLS-1$
      return "String TRANSLATABLE"; //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 : // title
         if(!_value.getString().equals(titledBorder.getTitle())) {
           titledBorder.setTitle(_value.getString());
           panel.setBorder(titledBorder);
           panel.repaint();
         }
         break;
       default :
         super.setValue(_index-1, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 : // title
         panel.setBorder(etchedBorder);
         panel.repaint();
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

  // ------------------------------------------------
  // Output
  // ------------------------------------------------
  public void clear() {
    textarea.setText(""); //$NON-NLS-1$
    textarea.setCaretPosition(textarea.getText().length());
  }

  public void println(String s) {
    print(s+_RETURN_);
  }

  public void print(String s) {
    textarea.append(s);
    textarea.setCaretPosition(textarea.getText().length());
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
