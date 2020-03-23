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
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 * A class which implements a line border on only the top and left hand side.
 *
 * @author Wolfgang Christian
 */
public class CellBorder extends AbstractBorder {
  protected int thickness;
  protected Color lineColor;

  /**
   * Creates a line border with the specified color and a
   * thickness = 1.
   * @param color the color for the border
   */
  public CellBorder(Color color) {
    this(color, 1);
  }

  /**
   * Creates a line border with the specified color and thickness.
   * @param color the color of the border
   * @param thickness the thickness of the border
   */
  public CellBorder(Color color, int thickness) {
    lineColor = color;
    this.thickness = thickness;
  }

  /**
   * Paints the border for the specified component with the
   * specified position and size.
   * @param c the component for which this border is being painted
   * @param g the paint graphics
   * @param x the x position of the painted border
   * @param y the y position of the painted border
   * @param width the width of the painted border
   * @param height the height of the painted border
   */
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Color oldColor = g.getColor();
    g.setColor(lineColor);
    for(int i = 0; i<thickness; i++) {
      g.drawLine(x, y+i, x+width-1, y+i);
      g.drawLine(x+i, y, x+i, y+height-1);
      //g.drawRect(x+i, y+i, width-i-i-1, height-i-i-1);
    }
    g.setColor(oldColor);
  }

  /**
   * Returns the insets of the border.
   * @param c the component for which this border insets value applies
   */
  public Insets getBorderInsets(Component c) {
    return new Insets(thickness+1, thickness+1, 1, 1);
  }

  /**
   * Reinitialize the insets parameter with this Border's current Insets.
   * @param c the component for which this border insets value applies
   * @param insets the object to be reinitialized
   */
  public Insets getBorderInsets(Component c, Insets insets) {
    insets.left = insets.top = thickness+1;
    insets.right = insets.bottom = 1;
    return insets;
  }

  /**
   * Returns the color of the border.
   */
  public Color getLineColor() {
    return lineColor;
  }

  /**
   * Returns the thickness of the border.
   */
  public int getThickness() {
    return thickness;
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
