/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import org.opensourcephysics.display.Drawable;

/**
 * DrawableAxes defines axes that render themselves in a drawing panel.
 *
 * Axes use the plotting panel's gutters, pixel scale, and affine
 * transformation.
 *
 * @author W. Christian
 */
public interface DrawableAxes extends Drawable {
  /**
   * Sets the x label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  s the label
   * @param font_name an optional font name
   */
  void setXLabel(String s, String font_name);

  /**
   * Gets the x axis label.
   *
   * @return String
   */
  String getXLabel();

  /**
   * Sets the y label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  s the label
   * @param font_name an optional font name
   */
  void setYLabel(String s, String font_name);

  /**
   * Gets the y axis label.
   *
   * @return String
   */
  String getYLabel();

  /**
   * Sets the title that will be drawn within the drawing panel.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param s the title
   * @param font_name an optional font name
   */
  void setTitle(String s, String font_name);

  /**
   * Gets the x title.
   *
   * @return String
   */
  String getTitle();

  /**
   * Sets the visibility of the axes.
   *
   * @param isVisible true if the axes are visible
   */
  void setVisible(boolean isVisible);

  /**
   * Sets the interior background color.
   */
  void setInteriorBackground(Color color);

  /**
   * Gets the interior background color.
   */
  Color getInteriorBackground();

  /**
   * Shows a grid line for every x axis major tickmark.
   */
  void setShowMajorXGrid(boolean showGrid);

  /**
   * Shows a grid line for every x axis minor tickmark.
   */
  void setShowMinorXGrid(boolean showGrid);

  /**
   * Shows a grid line for every y axis major tickmark.
   */
  void setShowMajorYGrid(boolean showGrid);

  /**
   * Shows a grid line for every y axis minor tickmark.
   */
  void setShowMinorYGrid(boolean showGrid);

  /**
   * Resizes fonts by the specified factor.
   *
   * @param factor the factor
   * @param panel the drawing panel on which these axes are drawn
   */
  void resizeFonts(double factor, org.opensourcephysics.display.DrawingPanel panel);

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
