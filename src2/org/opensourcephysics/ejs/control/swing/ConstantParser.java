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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.StringTokenizer;
import javax.swing.SwingConstants;
import org.opensourcephysics.ejs.control.value.IntegerValue;
import org.opensourcephysics.ejs.control.value.ObjectValue;
import org.opensourcephysics.ejs.control.value.StringValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * This class provides static methods that parse a string and return
 * a Value with the corresponding type and value, ready to be used by
 * the setValue() method of ControlElements
 */
public class ConstantParser extends org.opensourcephysics.ejs.control.ConstantParser {
  // -------------- public methods -----------
  static public Value pointConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if("center".equals(_value)) { //$NON-NLS-1$
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      return new ObjectValue(new Point(d.width/2, d.height/2));
    }
    if(_value.indexOf(',')<0) {
      return null; // No commas, not a valid constant
    }
    try {                                                   // x,y
      StringTokenizer t = new StringTokenizer(_value, ","); //$NON-NLS-1$
      int x = Integer.parseInt(t.nextToken());
      int y = Integer.parseInt(t.nextToken());
      return new ObjectValue(new Point(x, y));
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  static public Value dimensionConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if("pack".equals(_value)) {       //$NON-NLS-1$
      return new StringValue("pack"); //$NON-NLS-1$
    }
    if(_value.indexOf(',')<0) {
      return null; // No commas, not a valid constant
    }
    try {                                                   // w,h
      StringTokenizer t = new StringTokenizer(_value, ","); //$NON-NLS-1$
      int w = Integer.parseInt(t.nextToken());
      int h = Integer.parseInt(t.nextToken());
      return new ObjectValue(new Dimension(w, h));
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  static public Value placementConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if(_value.equals("bottom")) {       //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.BOTTOM);
    } else if(_value.equals("left")) {  //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.LEFT);
    } else if(_value.equals("right")) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.RIGHT);
    } else if(_value.equals("top")) {   //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.TOP);
    } else {
      return null;
    }
  }

  static public Value layoutConstant(Container _container, String _value) {
    _value = _value.trim().toLowerCase();
    StringTokenizer tkn = new StringTokenizer(_value, ":,"); //$NON-NLS-1$
    String type = tkn.nextToken();
    if(type.equals("flow")) {                   // java.awt.FlowLayout //$NON-NLS-1$
      if(tkn.hasMoreTokens()) {
        try {
          int align;
          String alignStr = tkn.nextToken();
          if(alignStr.equals("left")) {         //$NON-NLS-1$
            align = FlowLayout.LEFT;
          } else if(alignStr.equals("right")) { //$NON-NLS-1$
            align = FlowLayout.RIGHT;
          } else {
            align = FlowLayout.CENTER;
          }
          if(tkn.hasMoreTokens()) {
            int hgap = Integer.parseInt(tkn.nextToken());
            int vgap = Integer.parseInt(tkn.nextToken());
            return new ObjectValue(new FlowLayout(align, hgap, vgap));
          }
          return new ObjectValue(new FlowLayout(align));
        } catch(Exception exc) {
          exc.printStackTrace();
        }
      }
      return new ObjectValue(new FlowLayout());
    }
    if(type.equals("grid")) { // java.awt.GridLayout //$NON-NLS-1$
      if(tkn.hasMoreTokens()) {
        try {
          int rows = Integer.parseInt(tkn.nextToken());
          int cols = Integer.parseInt(tkn.nextToken());
          if(tkn.hasMoreTokens()) {
            int hgap = Integer.parseInt(tkn.nextToken());
            int vgap = Integer.parseInt(tkn.nextToken());
            return new ObjectValue(new GridLayout(rows, cols, hgap, vgap));
          }
          return new ObjectValue(new GridLayout(rows, cols));
        } catch(Exception exc) {
          exc.printStackTrace();
        }
      }
      return new ObjectValue(new GridLayout());
    }
    if(type.equals("border")) { // java.awt.BorderLayout //$NON-NLS-1$
      if(tkn.hasMoreTokens()) {
        try {
          int hgap = Integer.parseInt(tkn.nextToken());
          int vgap = Integer.parseInt(tkn.nextToken());
          return new ObjectValue(new BorderLayout(hgap, vgap));
        } catch(Exception exc) {
          exc.printStackTrace();
        }
      }
      return new ObjectValue(new BorderLayout());
    }
    if(type.equals("hbox")) { //$NON-NLS-1$
      return new ObjectValue(new javax.swing.BoxLayout(_container, javax.swing.BoxLayout.X_AXIS));
    }
    if(type.equals("vbox")) { //$NON-NLS-1$
      return new ObjectValue(new javax.swing.BoxLayout(_container, javax.swing.BoxLayout.Y_AXIS));
    }
    return null;
  }

  static public Value constraintsConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if(_value.equals("north")) { //$NON-NLS-1$
      return new StringValue(BorderLayout.NORTH);
    }
    if(_value.equals("south")) { //$NON-NLS-1$
      return new StringValue(BorderLayout.SOUTH);
    }
    if(_value.equals("east")) { //$NON-NLS-1$
      return new StringValue(BorderLayout.EAST);
    }
    if(_value.equals("west")) { //$NON-NLS-1$
      return new StringValue(BorderLayout.WEST);
    }
    if(_value.equals("center")) { //$NON-NLS-1$
      return new StringValue(BorderLayout.CENTER);
    }
    return new StringValue(BorderLayout.CENTER);
  }

  static public Value orientationConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if(_value.equals("vertical")) { //$NON-NLS-1$
      return new IntegerValue(SwingConstants.VERTICAL);
    }
    return new IntegerValue(SwingConstants.HORIZONTAL);
  }

  static public Value alignmentConstant(String _value) {
    _value = _value.trim().toLowerCase();
    if(_value.indexOf("top")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.TOP);
    }
    if(_value.indexOf("center")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.CENTER);
    }
    if(_value.indexOf("bottom")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.BOTTOM);
    }
    if(_value.indexOf("left")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.LEFT);
    }
    if(_value.indexOf("right")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.RIGHT);
    }
    if(_value.indexOf("leading")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.LEADING);
    }
    if(_value.indexOf("trailing")!=-1) { //$NON-NLS-1$
      return new IntegerValue(javax.swing.SwingConstants.TRAILING);
    }
    return new IntegerValue(javax.swing.SwingConstants.CENTER);
  }

} // end of class

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
