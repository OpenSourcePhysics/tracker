/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * An InteractiveTextLine is a single line of text that can be moved and rotated like other interactive shapes.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class InteractiveTextLine extends InteractiveShape {
  protected TextLine textLine;
  boolean dirty = true;
  double sx, sy;
  Rectangle2D boundsRect = new Rectangle2D.Float(0, 0, 0, 0);

  /**
   * Constructs an interactive TextLinme with the given text and location.
   *
   * @param text String
   * @param x double
   * @param y double
   */
  public InteractiveTextLine(String text, double x, double y) {
    super(null, x, y);
    textLine = new TextLine(text);
    textLine.setJustification(TextLine.CENTER);
    color = Color.BLACK;
  }

  /**
   * Sets the justification to center, left, or right.
   *
   * <code>TextLine.CENTER, TextLine.LEFT, TextLine.RIGHT </code>
   *
   * @param justification int
   */
  public void setJustification(int justification) {
    textLine.setJustification(justification);
  }

  /**
   * Sets the text to be displayed.
   *
   * @param text String
   */
  public void setText(String text) {
    textLine.setText(text);
    dirty = true;
  }

  /**
   * Gets the text to be dispalyed.
   *
   * @return String
   */
  public String getText() {
    return textLine.getText();
  }

  /**
   * Sets the font used to display the text.
   *
   * @param font Font
   */
  public void setFont(Font font) {
    textLine.setFont(font);
    dirty = true;
  }

  /**
   * Gets the font used to display the text.
   *
   * @return Font
   */
  public Font getFont() {
    return textLine.getFont();
  }

  /**
   * Determines if the shape is enabled and if the given pixel coordinates are within the image.
   *
   * @param panel DrawingPanel
   * @param xpix int
   * @param ypix int
   * @return boolean
   */
  public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
    if((textLine==null)||!enabled) {
      return false;
    }
    if((Math.abs(panel.xToPix(x)-xpix)<10)&&(Math.abs(panel.yToPix(y)-ypix)<10)) {
      return true;
    }
    return false;
  }

  private void checkBounds(Graphics g) {
    if(dirty||(toPixels.getScaleX()!=sx)||(toPixels.getScaleY()!=sy)) {
      boundsRect = textLine.getStringBounds(g);
      sx = toPixels.getScaleX();
      sy = toPixels.getScaleY();
      dirty = false;
    }
  }

  /**
   * Draws the text.
   *
   * @param panel  the world in which the arrow is viewed
   * @param g  the graphics context upon which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(textLine.getText().trim().equals("")) { //$NON-NLS-1$
      return;
    }
    textLine.setColor(color);
    toPixels = panel.getPixelTransform();
    checkBounds(g);
    Point2D pt = new Point2D.Double(x, y);
    pt = toPixels.transform(pt, pt);
    Graphics2D g2 = (Graphics2D) g;
    g2.translate(pt.getX(), pt.getY());
    g2.rotate(-theta);
    textLine.drawText(g2, (int) boundsRect.getX(), (int) boundsRect.getY());
    g2.rotate(theta);
    g2.translate(-pt.getX(), -pt.getY());
  }

  /**
   * Gets the XML object loader for this class.
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new InteractiveTextLineLoader();
  }

  /**
   * A class to save and load InteractiveArrow in an XMLControl.
   */
  protected static class InteractiveTextLineLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      InteractiveTextLine interactiveTextLine = (InteractiveTextLine) obj;
      control.setValue("text", interactiveTextLine.getText());           //$NON-NLS-1$
      control.setValue("x", interactiveTextLine.x);                      //$NON-NLS-1$
      control.setValue("y", interactiveTextLine.y);                      //$NON-NLS-1$
      control.setValue("is enabled", interactiveTextLine.isEnabled());   //$NON-NLS-1$
      control.setValue("is measured", interactiveTextLine.isMeasured()); //$NON-NLS-1$
      control.setValue("color", interactiveTextLine.color);              //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new InteractiveTextLine("", 0, 0); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      InteractiveTextLine interactiveTextLine = (InteractiveTextLine) obj;
      double x = control.getDouble("x"); //$NON-NLS-1$
      double y = control.getDouble("y"); //$NON-NLS-1$
      interactiveTextLine.setText(control.getString("text")); //$NON-NLS-1$
      interactiveTextLine.enabled = control.getBoolean("is enabled");        //$NON-NLS-1$
      interactiveTextLine.enableMeasure = control.getBoolean("is measured"); //$NON-NLS-1$
      interactiveTextLine.color = (Color) control.getObject("color");        //$NON-NLS-1$
      interactiveTextLine.setXY(x, y);
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
