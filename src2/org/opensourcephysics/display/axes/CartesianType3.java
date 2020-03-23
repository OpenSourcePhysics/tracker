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
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TextLine;

/**
 * CartesianType3 draws draggable X and Y axes in the interior of a plotting panel.
 *
 * @author W. Christian
 */
public class CartesianType3 extends AbstractAxes implements CartesianAxes, Interactive {
  XAxis xaxis;
  YAxis yaxis;
  boolean enabled = true;
  boolean xlog = false, ylog = false; // Added by Paco

  /**
   * Constructs the XYAxes inside the drawing panel.
   *
   * Drawing panel gutters are set to 30, 30, 30, 30.
   *
   * @param  panel  the drawing panel that will use the axes
   */
  public CartesianType3(PlottingPanel panel) {
    super(panel);
    defaultLeftGutter = 30;
    defaultTopGutter = 30;
    defaultRightGutter = 30;
    defaultBottomGutter = 30;
    titleLine.setJustification(TextLine.CENTER);
    titleLine.setFont(titleFont);
    xaxis = new XAxis();
    yaxis = new YAxis();
    xaxis.setEnabled(true); // enable dragging
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    yaxis.setEnabled(true);
    yaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    if(panel==null) {
      return;
    }
    panel.setCoordinateStringBuilder(CoordinateStringBuilder.createCartesian());
    panel.setPreferredGutters(defaultLeftGutter, defaultTopGutter, defaultRightGutter, defaultBottomGutter);
    panel.setAxes(this);
  }

  /**
   * Sets the x label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param s the title
   * @param font_name an optional font name
   */
  public void setXLabel(String s, String font_name) {
    xaxis.setTitle(s, font_name);
  }

  /**
   * Sets the y label of the axes.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param s the title
   * @param font_name an optional font name
   */
  public void setYLabel(String s, String font_name) {
    yaxis.setTitle(s, font_name);
  }

  /**
   * Set a title that will be drawn within the drawing panel.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param s the title
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
   * Gets the x axis label.
   *
   * @return String
   */
  public String getXLabel() {
    return xaxis.axisLabel.getText();
  }

  /**
   * Gets the y axis label.
   *
   * @return String
   */
  public String getYLabel() {
    return yaxis.axisLabel.getText();
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
   * Sets the x axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  public void setXLog(boolean isLog) {
    xlog = isLog; // Added by Paco
    if(isLog) {
      xaxis.setAxisType(XYAxis.LOG10);
    } else {
      xaxis.setAxisType(XYAxis.LINEAR);
    }
  }

  /**
   * Sets the y axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  public void setYLog(boolean isLog) {
    ylog = isLog; // Added by Paco
    if(isLog) {
      yaxis.setAxisType(XYAxis.LOG10);
    } else {
      yaxis.setAxisType(XYAxis.LINEAR);
    }
  }

  public boolean isXLog() {
    return xlog;
  }

  public boolean isYLog() {
    return ylog;
  }

  /**
   * Draws the axes in the drawing panel.
   *
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(interiorColor!=null) {
      g.setColor(interiorColor);
      int gw = panel.getLeftGutter()+panel.getRightGutter();
      int gh = panel.getTopGutter()+panel.getBottomGutter();
      g.fillRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
      g.setColor(Color.lightGray);
      g.drawRect(panel.getLeftGutter(), panel.getTopGutter(), panel.getWidth()-gw, panel.getHeight()-gh);
    }
    xaxis.draw(panel, g);
    yaxis.draw(panel, g);
    titleLine.setX((panel.getXMax()+panel.getXMin())/2);
    if(panel.getTopGutter()>20) {
      titleLine.setY(panel.getYMax()+5/panel.getYPixPerUnit());
    } else {
      titleLine.setY(panel.getYMax()-25/panel.getYPixPerUnit());
    }
    titleLine.setColor(panel.getForeground());
    titleLine.draw(panel, g);
  }

  /**
   * Sets the interior background color.
   */
  public void setInteriorBackground(Color color) {
    interiorColor = color;
  }

  /**
   * Shows a grid line for every x axis major tickmark.
   */
  public void setShowMajorXGrid(boolean showGrid) {
    xaxis.setShowMajorGrid(showGrid);
    if(!showGrid) {
      setShowMinorXGrid(showGrid);
    }
  }

  /**
   * Shows a grid line for every x axis minor tickmark.
   */
  public void setShowMinorXGrid(boolean showGrid) {
    // minor grids not yet implemented
  }

  /**
   * Shows a grid line for every y axis major tickmark.
   */
  public void setShowMajorYGrid(boolean showGrid) {
    yaxis.setShowMajorGrid(showGrid);
    if(!showGrid) {
      setShowMinorYGrid(showGrid);
    }
  }

  /**
   * Shows a grid line for every y axis minor tickmark.
   */
  public void setShowMinorYGrid(boolean showGrid) {
    // minor grids not yet implemented
  }

  public void centerAxes(DrawingPanel panel) {
    xaxis.setLocation((panel.getYMax()+panel.getYMin())/2);
    yaxis.setLocation((panel.getXMax()+panel.getXMin())/2);
  }

  public void setEnabled(boolean _enabled) {
    enabled = _enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setXY(double x, double y) {
    xaxis.setLocation(y); // Added by Paco
    yaxis.setLocation(x); // Added by Paco
  }

  public void setX(double x) {
    yaxis.setLocation(x); // Added by Paco
  }

  public void setY(double y) {
    xaxis.setLocation(y); // Added by Paco
  }

  public double getX() {
    return 0;
  }

  public double getY() {
    return 0;
  }

  /**
   * Gets the minimum x needed to draw this object.
   * @return minimum
   */
  public double getXMin() {
    return 0;
  }

  /**
   * Gets the maximum x needed to draw this object.
   * @return maximum
   */
  public double getXMax() {
    return 0;
  }

  /**
   * Gets the minimum y needed to draw this object.
   * @return minimum
   */
  public double getYMin() {
    return 0;
  }

  /**
   * Gets the maximum y needed to draw this object.
   * @return minimum
   */
  public double getYMax() {
    return 0;
  }

  /**
   * Determines if information is available to set min/max values.
   * Objects that store data, Datasets for example, usually return false if data is null.
   *
   * @return true if min/max values are valid
   */
  public boolean isMeasured() {
    return false;
  } // the axes should not effect the scale

  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!visible) {
      return null;
    }
    if(xaxis.findInteractive(panel, xpix, ypix)!=null) {
      return xaxis;
    } else if(yaxis.findInteractive(panel, xpix, ypix)!=null) {
      return yaxis;
    } else {
      return null;
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
