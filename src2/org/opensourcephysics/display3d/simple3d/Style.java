/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

public class Style implements org.opensourcephysics.display3d.core.Style {
  static final int STYLE_LINE_COLOR = 0;
  static final int STYLE_LINE_WIDTH = 1;
  static final int STYLE_FILL_COLOR = 2;
  static final int STYLE_RESOLUTION = 3;
  static final int STYLE_DRAWING_FILL = 4;
  static final int STYLE_DRAWING_LINES = 5;
  static final int STYLE_RELATIVE_POSITION = 6;
  // Configuration variables
  private boolean drawsFill = true, drawsLines = true;
  private Color lineColor = Color.black;
  private float lineWidth = 1.0f;
  private Color fillColor = Color.blue;
  private org.opensourcephysics.display3d.core.Resolution resolution = null;
  private double depthFactor = 1.0;

  /**
   * Indicates if the drawable should displace itself from the drawing point.
   * Standard values are provided as static class members. Default is CENTERED.
   */
  private int position = CENTERED;
  // Implementation variables
  private boolean drawFillsSet = false, drawLinesSet = false;
  private String textureFile1 = null;
  private String textureFile2 = null;
  private double transpTexture = Double.NaN;
  private boolean combineTexture = false;

  /**
   * The owner element. This is needed to report to the element any change.
   */
  private Element element = null;
  private Stroke lineStroke = new BasicStroke(lineWidth);

  /**
   * Package-private constructor
   * User must obtain Style objects from elements, by using the getStyle() method
   * @param _element Element
   */
  Style(Element _element) {
    this.element = _element;
  }

  /**
   * Sets the element. For the use of ElementLoader only.
   * @param _element Element
   */
  void setElement(Element _element) {
    this.element = _element;
  }

  public void setLineColor(Color _color) {
    if(_color==null) {
      return; // Ignore null colors
    }
    this.lineColor = _color;
    if(element!=null) {
      element.styleChanged(STYLE_LINE_COLOR);
    }
  }

  final public Color getLineColor() {
    return this.lineColor;
  }

  public void setLineWidth(float _width) {
    if(this.lineWidth==_width) {
      return;
    }
    this.lineStroke = new BasicStroke(this.lineWidth = _width);
    if(element!=null) {
      element.styleChanged(STYLE_LINE_WIDTH);
    }
  }

  final public float getLineWidth() {
    return this.lineWidth;
  }

  /**
   * Gets the Stroke derived from the line width
   * @return Stroke
   * @see java.awt.Stroke
   */
  final Stroke getLineStroke() {
    return this.lineStroke;
  }

  public void setFillColor(Color _color) {
    if(_color==null) {
      return; // Ignore null colors
    }
    this.fillColor = _color;
    if(element!=null) {
      element.styleChanged(STYLE_FILL_COLOR);
    }
  }

  final public Color getFillColor() {
    return this.fillColor;
  }

  public void setResolution(org.opensourcephysics.display3d.core.Resolution _res) {
    this.resolution = _res; // No need to clone. Resolution is unmutable
    if(element!=null) {
      element.styleChanged(STYLE_RESOLUTION);
    }
  }
  // No danger. Resolution is unmutable

  final public org.opensourcephysics.display3d.core.Resolution getResolution() {
    return this.resolution;
  }

  public boolean isDrawingFill() {
    return drawsFill;
  }

  public void setDrawingFill(boolean _drawsFill) {
    this.drawsFill = _drawsFill;
    drawFillsSet = true;
    if(element!=null) {
      element.styleChanged(STYLE_DRAWING_FILL);
    }
  }

  public boolean isDrawingLines() {
    return drawsLines;
  }

  public void setDrawingLines(boolean _drawsLines) {
    this.drawsLines = _drawsLines;
    drawLinesSet = true;
    if(element!=null) {
      element.styleChanged(STYLE_DRAWING_LINES);
    }
  }

  public void setDepthFactor(double factor) {
    this.depthFactor = factor;
  }

  public double getDepthFactor() {
    return this.depthFactor;
  }

  //CJB
  public void setTexture(String file1, String file2, double transparency, boolean combine) {
    textureFile1 = file1;
    textureFile2 = file2;
    this.transpTexture = transparency;
    this.combineTexture = combine;
  }

  public String[] getTextures() {
    return new String[] {textureFile1, textureFile2};
  }

  public double getTransparency() {
    return transpTexture;
  }

  public boolean getCombine() {
    return combineTexture;
  }
  //CJB

  final public void setRelativePosition(int _position) {
    this.position = _position;
    element.styleChanged(STYLE_RELATIVE_POSITION);
  }

  final public int getRelativePosition() {
    return this.position;
  }

  public void copyTo(org.opensourcephysics.display3d.core.Style target) {
    target.setDrawingFill(drawsFill);
    target.setDrawingLines(drawsLines);
    target.setLineColor(lineColor);
    target.setLineWidth(lineWidth);
    target.setFillColor(fillColor);
    target.setResolution(resolution);
    target.setDepthFactor(depthFactor);
    target.setRelativePosition(position);
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public static XML.ObjectLoader getLoader() {
    return new StyleLoader();
  }

  protected static class StyleLoader extends org.opensourcephysics.display3d.core.Style.Loader {
    public Object createObject(XMLControl control) {
      return new Style((Element) null);
    }

    public void saveObject(XMLControl control, Object obj) {
      Style style = (Style) obj;
      control.setValue("line color", style.getLineColor());  //$NON-NLS-1$
      control.setValue("line width", style.getLineWidth());  //$NON-NLS-1$
      control.setValue("fill color", style.getFillColor());  //$NON-NLS-1$
      control.setValue("resolution", style.getResolution()); //$NON-NLS-1$
      if(style.drawFillsSet) {
        control.setValue("drawing fill", style.isDrawingFill()); //$NON-NLS-1$
      }
      if(style.drawLinesSet) {
        control.setValue("drawing lines", style.isDrawingLines()); //$NON-NLS-1$
      }
    }

    public Object loadObject(XMLControl control, Object obj) {
      Style style = (Style) obj;
      style.setLineColor((Color) control.getObject("line color"));                                            //$NON-NLS-1$
      style.setLineWidth((float) control.getDouble("line width"));                                            //$NON-NLS-1$
      style.setFillColor((Color) control.getObject("fill color"));                                            //$NON-NLS-1$
      style.setResolution((org.opensourcephysics.display3d.core.Resolution) control.getObject("resolution")); //$NON-NLS-1$
      if(control.getPropertyType("drawing fill")!=null) {         //$NON-NLS-1$
        System.out.println("Reading drawFills");                  //$NON-NLS-1$
        style.setDrawingFill(control.getBoolean("drawing fill")); //$NON-NLS-1$
      } else {
        System.out.println("Not reading drawFills");              //$NON-NLS-1$
      }
      if(control.getPropertyType("drawing lines")!=null) {          //$NON-NLS-1$
        System.out.println("Reading drawLines");                    //$NON-NLS-1$
        style.setDrawingLines(control.getBoolean("drawing lines")); //$NON-NLS-1$
      } else {
        System.out.println("Not reading drawLines");                //$NON-NLS-1$
      }
      return obj;
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
