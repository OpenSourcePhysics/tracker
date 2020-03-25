/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

public class VisualizationHints implements org.opensourcephysics.display3d.core.VisualizationHints {
  static final int HINT_DECORATION_TYPE = 0;
  static final int HINT_REMOVE_HIDDEN_LINES = 1;
  static final int HINT_ALLOW_QUICK_REDRAW = 2;
  static final int HINT_USE_COLOR_DEPTH = 3;
  static final int HINT_CURSOR_TYPE = 4;
  static final int HINT_SHOW_COORDINATES = 5;
  static final int HINT_AXES_LABELS = 6;
  static final int HINT_ANY = 7;
  // Configuration variables
  private boolean removeHiddenLines = true, allowQuickRedraw = true, useColorDepth = true;
  private int cursorType = CURSOR_XYZ, showCoordinates = org.opensourcephysics.display3d.core.DrawingPanel3D.BOTTOM_LEFT;
  private int decorationType = DECORATION_CUBE;
  private String formatX = "x = 0.00;x = -0.00";              //$NON-NLS-1$
  private String formatY = "y = 0.00;y = -0.00";              //$NON-NLS-1$
  private String formatZ = "z = 0.00;z = -0.00";              //$NON-NLS-1$
  private String[] axesLabels = new String[] {"X", "Y", "Z"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  // Implementation variables
  private NumberFormat theFormatX = new DecimalFormat(formatX);
  private NumberFormat theFormatY = new DecimalFormat(formatY);
  private NumberFormat theFormatZ = new DecimalFormat(formatZ);

  /**
   * The DrawingPanel3D to which it belongs.
   * This is needed to report to it any change that implies a call to update()
   */
  private DrawingPanel3D panel;

  /**
   * Package-private constructor
   * VisualizationHints objects are obtained from DrawingPanel3Ds using the
   * getVisualizationHints() method.
   * @param _panel DrawingPanel3D
   * @see DrawingPanel3D
   */
  VisualizationHints(DrawingPanel3D _panel) {
    this.panel = _panel;
  }

  /**
   * Only for the use of the XMLLoader for DrawingPanel3D!
   * Sets the panel of these hints
   * @param aPanel DrawingPanel3D
   */
  void setPanel(DrawingPanel3D aPanel) {
    this.panel = aPanel;
  }

  public void setCursorType(int _type) {
    this.cursorType = _type;
    if(panel!=null) {
      panel.hintChanged(HINT_CURSOR_TYPE);
    }
  }

  final public int getCursorType() {
    return this.cursorType;
  }

  public void setDecorationType(int _value) {
    this.decorationType = _value;
    if(panel!=null) {
      panel.hintChanged(HINT_DECORATION_TYPE);
    }
  }

  final public int getDecorationType() {
    return this.decorationType;
  }

  final public void setAxesLabels(String[] labels) {
    axesLabels = labels;
    if(panel!=null) {
      panel.hintChanged(HINT_AXES_LABELS);
    }
  }

  final public String[] getAxesLabels() {
    return axesLabels;
  }

  public void setRemoveHiddenLines(boolean _value) {
    this.removeHiddenLines = _value;
    if(panel!=null) {
      panel.hintChanged(HINT_REMOVE_HIDDEN_LINES);
    }
  }

  final public boolean isRemoveHiddenLines() {
    return this.removeHiddenLines;
  }

  public void setAllowQuickRedraw(boolean _value) {
    this.allowQuickRedraw = _value;
    if(panel!=null) {
      panel.hintChanged(HINT_ALLOW_QUICK_REDRAW);
    }
  }

  final public boolean isAllowQuickRedraw() {
    return this.allowQuickRedraw;
  }

  public void setUseColorDepth(boolean _value) {
    this.useColorDepth = _value;
    if(panel!=null) {
      panel.hintChanged(HINT_USE_COLOR_DEPTH);
    }
  }

  final public boolean isUseColorDepth() {
    return this.useColorDepth;
  }

  public void setShowCoordinates(int location) {
    showCoordinates = location;
    if(panel!=null) {
      panel.hintChanged(HINT_SHOW_COORDINATES);
    }
  }

  public int getShowCoordinates() {
    return showCoordinates;
  }

  public void setXFormat(String format) {
    formatX = format;
    if(formatX!=null) {
      theFormatX = new java.text.DecimalFormat(formatX);
    }
  }

  public String getXFormat() {
    return formatX;
  }

  public void setYFormat(String format) {
    formatY = format;
    if(formatY!=null) {
      theFormatY = new java.text.DecimalFormat(formatY);
    }
  }

  public String getYFormat() {
    return formatY;
  }

  public void setZFormat(String format) {
    formatZ = format;
    if(formatZ!=null) {
      theFormatZ = new java.text.DecimalFormat(formatZ);
    }
  }

  public String getZFormat() {
    return formatZ;
  }

  void displayPosition(int projectionMode, double[] point) {
    if(showCoordinates<0) {
      return;
    }
    if(point==null) {
      panel.setMessage(null, showCoordinates);
      return;
    }
    String text = ""; //$NON-NLS-1$
    switch(projectionMode) {
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XY :
         if(formatX!=null) {
           text = theFormatX.format(point[0]);
         }
         if(formatY!=null) {
           text += ", "+theFormatY.format(point[1]); //$NON-NLS-1$
         }
         break;
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XZ :
         if(formatX!=null) {
           text = theFormatX.format(point[0]);
         }
         if(formatZ!=null) {
           text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
         }
         break;
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_YZ :
         if(formatY!=null) {
           text = theFormatY.format(point[1]);
         }
         if(formatZ!=null) {
           text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
         }
         break;
       default :
         if(formatX!=null) {
           text = theFormatX.format(point[0]);
         }
         if(formatY!=null) {
           text += ", "+theFormatY.format(point[1]); //$NON-NLS-1$
         }
         if(formatZ!=null) {
           text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
         }
         break;
    }
    if(text.startsWith(", ")) { //$NON-NLS-1$
      text = text.substring(2);
    }
    panel.setMessage(text, showCoordinates);
  }

  public void copyFrom(org.opensourcephysics.display3d.core.VisualizationHints hints) {
    decorationType = hints.getDecorationType();
    cursorType = hints.getCursorType();
    axesLabels = hints.getAxesLabels();
    this.removeHiddenLines = hints.isRemoveHiddenLines();
    this.allowQuickRedraw = hints.isAllowQuickRedraw();
    this.useColorDepth = hints.isUseColorDepth();
    this.showCoordinates = hints.getShowCoordinates();
    formatX = hints.getXFormat();
    if(formatX!=null) {
      theFormatX = new java.text.DecimalFormat(formatX);
    }
    formatZ = hints.getYFormat();
    if(formatY!=null) {
      theFormatY = new java.text.DecimalFormat(formatY);
    }
    formatZ = hints.getZFormat();
    if(formatZ!=null) {
      theFormatZ = new java.text.DecimalFormat(formatZ);
    }
    panel.hintChanged(HINT_ANY);
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public static XML.ObjectLoader getLoader() {
    return new VisualizationHintsLoader();
  }

  protected static class VisualizationHintsLoader extends org.opensourcephysics.display3d.core.VisualizationHints.Loader {
    public Object createObject(XMLControl control) {
      return new VisualizationHints((DrawingPanel3D) null);
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
