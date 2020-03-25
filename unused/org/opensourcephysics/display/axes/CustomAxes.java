/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TextLine;

public class CustomAxes extends AbstractAxes implements DrawableAxes {
  Color gridColor = Color.lightGray;
  ArrayList<Drawable> drawableList = new ArrayList<Drawable>(); // list of Drawable objects

  /**
   * Constructor CustomAxes
   *
   * @param panel
   */
  public CustomAxes(PlottingPanel panel) {
    super(panel);
    defaultLeftGutter = 25;
    defaultTopGutter = 25;
    defaultRightGutter = 25;
    defaultBottomGutter = 25;
    titleLine.setJustification(TextLine.CENTER);
    titleLine.setFont(titleFont);
    if(panel==null) {
      return;
    }
    panel.setPreferredGutters(defaultLeftGutter, defaultTopGutter, defaultRightGutter, defaultBottomGutter);
    panel.setCoordinateStringBuilder(CoordinateStringBuilder.createCartesian());
    panel.setAxes(this);
  }

  /**
   * Sets the x label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  s the label
   * @param font_name an optional font name
   */
  public void setXLabel(String s, String font_name) {}

  /**
   * Sets the y label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  s the label
   * @param font_name an optional font name
   */
  public void setYLabel(String s, String font_name) {}

  /**
 * Gets the x axis label.
 *
 * @return String
 */
  public String getXLabel() {
    return ""; //$NON-NLS-1$
  }

  /**
   * Gets the y axis label.
   *
   * @return String
   */
  public String getYLabel() {
    return ""; //$NON-NLS-1$
  }

  /**
   * Gets the title.
   *
   * @return String
   */
  public String getTitle() {
    return titleLine.getText();
  }

  /**
   * Set a title that will be drawn within the drawing panel.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  s the label
   * @param font_name an optional font name
   */
  public void setTitle(String s, String font_name) {
    titleLine.setText(s);
    if((font_name==null)||font_name.equals("")) { //$NON-NLS-1$
      return;
    }
    titleLine.setFont(Font.decode(font_name));
  }

  /**
   * Sets the x axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  public void setXLog(boolean isLog) {}

  /**
   * Sets the y axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  public void setYLog(boolean isLog) {}

  /**
   * Sets the visibility of the axes.
   *
   * @param isVisible true if the axes are visible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Sets the interior background color.
   * @param color
   */
  public void setInteriorBackground(Color color) {
    interiorColor = color;
  }

  /**
   * Shows a grid line for every x axis major tickmark.
   * @param showGrid
   */
  public void setShowMajorXGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every x axis minor tickmark.
   * @param showGrid
   */
  public void setShowMinorXGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every y axis major tickmark.
   * @param showGrid
   */
  public void setShowMajorYGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every y axis minor tickmark.
   * @param showGrid
   */
  public void setShowMinorYGrid(boolean showGrid) {}

  /**
   * Adds a drawable object to the drawable list.
   * @param drawable
   */
  public synchronized void addDrawable(Drawable drawable) {
    if((drawable!=null)&&!drawableList.contains(drawable)) {
      drawableList.add(drawable);
    }
  }

  /**
   * Draws the axes in a drawing panel.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(interiorColor!=panel.getBackground()) {
      g.setColor(interiorColor);
      int gw = panel.getLeftGutter()+panel.getRightGutter();
      int gh = panel.getTopGutter()+panel.getLeftGutter();
      g.fillRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
      g.setColor(gridColor);
      g.drawRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
    }
    Iterator<Drawable> it = drawableList.iterator();
    while(it.hasNext()) {
      Drawable drawable = it.next();
      drawable.draw(panel, g);
    }
    titleLine.setX((panel.getXMax()+panel.getXMin())/2);
    if(panel.getTopGutter()>20) {
      titleLine.setY(panel.getYMax()+5/panel.getYPixPerUnit());
    } else {
      titleLine.setY(panel.getYMax()-25/panel.getYPixPerUnit());
    }
    titleLine.draw(panel, g);
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
