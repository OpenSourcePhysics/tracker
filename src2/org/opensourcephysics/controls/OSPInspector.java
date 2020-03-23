/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JDialog;

/**
 * Inspects an object's state using XML.
 * @author W. Christian
 * @version 1.0
 */
public class OSPInspector {
  Object obj;
  XMLControlElement xml = new XMLControlElement();
  String shortObjectName = "null";                 //$NON-NLS-1$
  Color highlightColor = new Color(224, 255, 224); // light green

  protected OSPInspector(Object obj) {
    this.obj = obj;
    String name = obj.getClass().getName();
    shortObjectName = name.substring(1+name.lastIndexOf(".")); //$NON-NLS-1$
    xml.saveObject(obj);
  }

  /**
   * Gets an  OSPInspector if the oject has an XMLLoader.
   * @param obj Object
   * @return OSPInspector
   */
  public synchronized static OSPInspector getInspector(Object obj) {
    XML.ObjectLoader loader = XML.getLoader(obj.getClass());
    if((loader.getClass()==XMLLoader.class)||( // don't use inspector for defaulf loader
      obj instanceof Double)||(obj instanceof Integer)||(obj instanceof Boolean)) {
      return null;                             // no inspector for default loader or for wrappers for primative data types
    }
    return new OSPInspector(obj);
  }

  /**
   * Gets the short name of the object that is being inspected.
   *
   * @return String
   */
  public String getShortObjectName() {
    return shortObjectName;
  }

  /**
   * Gets the XML string for the object being inspected.
   *
   * @return String
   */
  public String toXML() {
    return xml.toXML();
  }

  /**
   * Shows the inspector.
   *
   * @return Object the object being inspected.
   */
  public Object show() {
    xml.saveObject(obj); // needed in case object has been recently modified
    JDialog dialog = new JDialog((java.awt.Frame) null, true);
    dialog.setContentPane(new XMLTreePanel(xml));
    dialog.setSize(new Dimension(600, 300));
    dialog.setTitle(shortObjectName+" "+ControlsRes.getString("OSPInspector.Title")); //$NON-NLS-1$ //$NON-NLS-2$
    dialog.setVisible(true);
    obj = xml.loadObject(null);
    return obj;
  }
  /*
     // test program
     public static void main(String[] args){
        org.opensourcephysics.display.Circle circle = new org.opensourcephysics.display.Circle();
        OSPInspector inspector = new OSPInspector(circle);
        inspector.show();
        System.out.println("x="+circle.getX());
        Color color=new Color(255,100,5);
        inspector = new OSPInspector(color);
        color=(Color)inspector.show();
        System.out.println("red="+color.getRed());
     }
  */

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
