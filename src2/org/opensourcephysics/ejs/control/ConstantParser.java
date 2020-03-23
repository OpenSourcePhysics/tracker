/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.StringTokenizer;
import org.opensourcephysics.ejs.control.value.BooleanValue;
import org.opensourcephysics.ejs.control.value.ObjectValue;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * This class provides static methods that parse a string and return
 * a Value with the corresponding type and value, ready to be used by
 * the setValue() method of ControlElements
 */
public class ConstantParser {
  // -------------- public variables and methods -----------
  public static final java.awt.Color NULL_COLOR = new java.awt.Color(0, 0, 0, 0);
  static private Font defaultFont = new Font("Dialog", 12, Font.PLAIN); //$NON-NLS-1$

  static public Value fontConstant(Font _currentFont, String _value) {
    if(_value.indexOf(',')<0) {
      return null; // No commas, not a valid constant
    }
    if(_currentFont==null) {
      _currentFont = defaultFont;
    }
    int style = _currentFont.getStyle();
    int size = _currentFont.getSize();
    String name = null;
    StringTokenizer t = new StringTokenizer(_value, ",", true); //$NON-NLS-1$
    if(t.hasMoreTokens()) {
      name = t.nextToken();
      if(name.equals(",")) { //$NON-NLS-1$
        name = _currentFont.getName();
      } else if(t.hasMoreTokens()) {
        t.nextToken();       // read out next ','
      }
    } else {
      name = _currentFont.getName();
    }
    if(t.hasMoreTokens()) {
      String styleStr = t.nextToken().toLowerCase();
      style = _currentFont.getStyle();
      if(!styleStr.equals(",")) {            //$NON-NLS-1$
        if(styleStr.indexOf("plain")!=-1) {  //$NON-NLS-1$
          style = java.awt.Font.PLAIN;
        }
        if(styleStr.indexOf("bold")!=-1) {   //$NON-NLS-1$
          style = java.awt.Font.BOLD;
        }
        if(styleStr.indexOf("italic")!=-1) { //$NON-NLS-1$
          style = style|java.awt.Font.ITALIC;
        }
        if(t.hasMoreTokens()) {
          t.nextToken();                     // read out next ','
        }
      }
    }
    if(t.hasMoreTokens()) {
      try {
        size = Integer.parseInt(t.nextToken());
      } catch(Exception exc) {
        size = _currentFont.getSize();
      }
    }
    return new ObjectValue(new Font(name, style, size));
  }

  static public Value booleanConstant(String _value) {
    if(_value.equals("true")) { //$NON-NLS-1$
      return new BooleanValue(true);
    }
    if(_value.equals("false")) { //$NON-NLS-1$
      return new BooleanValue(false);
    }
    return null; // Not a valid constant
  }

  static public Value colorConstant(String _value) {
    if(_value.indexOf(',')>=0) {                               // format is red,green,blue
      try {
        StringTokenizer t = new StringTokenizer(_value, ":,"); //$NON-NLS-1$
        int r = Integer.parseInt(t.nextToken());
        int g = Integer.parseInt(t.nextToken());
        int b = Integer.parseInt(t.nextToken());
        int alpha;
        if(t.hasMoreTokens()) {
          alpha = Integer.parseInt(t.nextToken());
        } else {
          alpha = 255;
        }
        if(r<0) {
          r = 0;
        } else if(r>255) {
          r = 255;
        }
        if(g<0) {
          g = 0;
        } else if(g>255) {
          g = 255;
        }
        if(b<0) {
          b = 0;
        } else if(b>255) {
          b = 255;
        }
        if(alpha<0) {
          alpha = 0;
        } else if(alpha>255) {
          alpha = 255;
        }
        return new ObjectValue(new Color(r, g, b, alpha));
      } catch(Exception exc) {
        exc.printStackTrace();
        return null;
      }
    }
    if(_value.equals("null")||_value.equals("none")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(NULL_COLOR);
    }
    if(_value.equals("black")||_value.equals("Color.black")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.black);
    }
    if(_value.equals("blue")||_value.equals("Color.blue")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.blue);
    }
    if(_value.equals("cyan")||_value.equals("Color.cyan")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.cyan);
    }
    if(_value.equals("darkGray")||_value.equals("Color.darkGray")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.darkGray);
    }
    if(_value.equals("gray")||_value.equals("Color.gray")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.gray);
    }
    if(_value.equals("green")||_value.equals("Color.green")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.green);
    }
    if(_value.equals("lightGray")||_value.equals("Color.lightGray")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.lightGray);
    }
    if(_value.equals("magenta")||_value.equals("Color.magenta")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.magenta);
    }
    if(_value.equals("orange")||_value.equals("Color.orange")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.orange);
    }
    if(_value.equals("pink")||_value.equals("Color.pink")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.pink);
    }
    if(_value.equals("red")||_value.equals("Color.red")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.red);
    }
    if(_value.equals("white")||_value.equals("Color.white")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.white);
    }
    if(_value.equals("yellow")||_value.equals("Color.yellow")) { //$NON-NLS-1$ //$NON-NLS-2$
      return new ObjectValue(Color.yellow);
    }
    return null; // Not a valid constant
  }

  static public Value formatConstant(String _value) {
    if(_value.indexOf(";")==-1) {                                             // FKH 021103 //$NON-NLS-1$
      int id1 = _value.indexOf("0"), id2 = _value.indexOf("#"), id = -1;      //$NON-NLS-1$ //$NON-NLS-2$
      if((id1>0)&&(id2>0)) {
        id = (id1<id2) ? id1 : id2;
      } else if(id1>0) {
        id = id1;
      } else if(id2>0) {
        id = id2;
      }
      if(id>0) {
        _value = _value+";"+_value.substring(0, id)+"-"+_value.substring(id); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }                                                                         // endFKH 021103
    try {
      return new ObjectValue(new java.text.DecimalFormat(_value));
    } catch(IllegalArgumentException _exc) {
      return null;
    }
  }

  static public Value rectangleConstant(String _value) {
    if(_value.indexOf(',')<0) {
      return null; // No commas, not a valid constant
    }
    try {                                                   // x,y,w,h
      StringTokenizer t = new StringTokenizer(_value, ","); //$NON-NLS-1$
      int x = Integer.parseInt(t.nextToken());
      int y = Integer.parseInt(t.nextToken());
      int w = Integer.parseInt(t.nextToken());
      int h = Integer.parseInt(t.nextToken());
      return new ObjectValue(new Rectangle(x, y, w, h));
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
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
