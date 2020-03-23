/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TextLine;

public class PolarType2 extends AbstractPolarAxis implements PolarAxes {
  /**
 * Constructs polar coordinate axes for the given panel.
 *
 * @param panel PlottingPanel
 * @param rLabel
 * @param phiLabel
 * @param phiOffset double  offset the phi coordinate
 */
  public PolarType2(PlottingPanel panel, String rLabel, String phiLabel, double phiOffset) {
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
    panel.setAxes(this);
    panel.setCoordinateStringBuilder(CoordinateStringBuilder.createPolar(rLabel, phiLabel, phiOffset));
  }

  /**
   * Constructs polar coordinate axes for the given panel.
   *
   * @param panel PlottingPanel
   */
  public PolarType2(PlottingPanel panel) {
    this(panel, "r=", " phi=", 0); //$NON-NLS-1$ //$NON-NLS-2$
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
   * @param  label the label
   * @param s an optional font name
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
   * Shows a grid line for every x axis major tickmark.
   */
  public void setShowMajorXGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every x axis minor tickmark.
   */
  public void setShowMinorXGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every y axis major tickmark.
   */
  public void setShowMajorYGrid(boolean showGrid) {}

  /**
   * Shows a grid line for every y axis minor tickmark.
   */
  public void setShowMinorYGrid(boolean showGrid) {}

  /**
 * Draws a representation of an object in a drawing panel.
 * @param panel
 * @param g
 */
  public void draw(DrawingPanel panel, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Shape clipShape = g2.getClip();
    int gw = panel.getLeftGutter()+panel.getRightGutter();
    int gh = panel.getTopGutter()+panel.getLeftGutter();
    if(interiorColor!=null) {
      g.setColor(interiorColor);
      g.fillRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
      g.setColor(gridcolor);
      g.drawRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
    }
    g2.clipRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
    double rmax = Math.abs(panel.getXMax())+Math.abs(panel.getYMax());
    rmax = Math.max(rmax, Math.abs(panel.getXMax())+Math.abs(panel.getYMin()));
    rmax = Math.max(rmax, Math.abs(panel.getXMin())+Math.abs(panel.getYMax()));
    rmax = Math.max(rmax, Math.abs(panel.getXMin())+Math.abs(panel.getYMin()));
    double dr = drawRings(rmax, panel, g);
    drawSpokes(rmax, panel, g);
    g2.setClip(clipShape);
    drawRAxis(dr, rmax, panel, g);
    titleLine.setX((panel.getXMax()+panel.getXMin())/2);
    if(panel.getTopGutter()>20) {
      titleLine.setY(panel.getYMax()+5/panel.getYPixPerUnit());
    } else {
      titleLine.setY(panel.getYMax()-25/panel.getYPixPerUnit());
    }
    titleLine.setColor(panel.getForeground());
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
