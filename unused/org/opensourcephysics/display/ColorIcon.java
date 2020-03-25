/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import javax.swing.Icon;

/**
 * This Icon fills or outlines itself with the color specified in its constructor.
 *
 * @author Douglas Brown
 */
public class ColorIcon implements Icon {
  // instance fields
  private int w;
  private int h;
  private Color color;
  private Color outline;
  private int lineWidth = 1;

  /**
   * Constructs a ColorIcon.
   *
   * @param color color of the icon
   * @param width width of the icon
   * @param height height of the icon
   */
  public ColorIcon(Color color, int width, int height) {
    w = width;
    h = height;
    setColor(color);
  }

  /**
   * Constructs an outlined ColorIcon.
   *
   * @param fillColor fill color of the icon
   * @param outlineColor outline color of the icon
   * @param width width of the icon
   * @param height height of the icon
   */
  public ColorIcon(Color fillColor, Color outlineColor, int width, int height) {
    w = width;
    h = height;
    outline = outlineColor;
    setColor(fillColor);
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Gets the icon width.
   *
   * @return the icon width
   */
  public int getIconWidth() {
    return w;
  }

  /**
   * Gets the icon height.
   *
   * @return the icon height
   */
  public int getIconHeight() {
    return h;
  }

  /**
   * Paints the icon.
   *
   * @param c the component on which it is painted
   * @param _g the graphics context
   * @param x the x coordinate of the icon
   * @param y the y coordinate of the icon
   */
  public void paintIcon(Component c, Graphics _g, int x, int y) {
    Graphics2D g = (Graphics2D) _g;
    Rectangle rect = new Rectangle(x, y, w, h);
    Paint gPaint = g.getPaint();
    if (outline!=null) {
	    g.setPaint(outline);
	    g.fill(rect);
    	rect.setFrame(x+lineWidth, y+lineWidth, w-2*lineWidth, h-2*lineWidth);
    }
    g.setPaint(color);
    g.fill(rect);
    // restore graphics paint
    g.setPaint(gPaint);
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
