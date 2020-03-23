/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import org.opensourcephysics.display.DrawingPanel;

/**
 * AbstractPolarAxis implements methods common to all polar axes.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractPolarAxis extends AbstractAxes implements PolarAxes {
  static final double LOG10 = Math.log(10);
  static int MAJOR_TIC = 5;
  protected double dr = 1;
  protected double dtheta = Math.PI/8;
  protected boolean autospaceRings = true;

  /**
   * Creates polar axes that will display themselves within the given drawing panel.
   *
   * @param drawingPanel DrawingPanel
   */
  protected AbstractPolarAxis(DrawingPanel drawingPanel) {
    super(drawingPanel);
  }

  /**
   * Automatically sets the spacing of the radial grid.
   * @param autoscaleR
   */
  public void autospaceRings(boolean autospace) {
    this.autospaceRings = autospace;
  }

  /**
   * Gets the spacing of the radial grid.
   */
  public double getDeltaR() {
    return dr;
  }

  /**
   * Sets the spacing of the radial gridlines.
   * @param dr
   */
  public void setDeltaR(double dr) {
    this.dr = dr;
  }

  /**
   * Gets the spacing of the radial gridlines.
   */
  public double getDeltaTheta() {
    return dtheta;
  }

  /**
   * Sets the spacing of the radial gridlines.
   * @param dtheta in degree
   */
  public void setDeltaTheta(double dtheta) {
    this.dtheta = Math.abs(dtheta);
  }

  /**
   * Method setLabelFormat
   *
   * @param formatString
   */
  public void setLabelFormat(String formatString) {
    labelFormat = new DecimalFormat(formatString);
  }

  /**
   * Draws the spokes for the polar plot.
   * @param panel
   * @param g
   */
  protected void drawRAxis(double dr, double rmax, DrawingPanel panel, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g.setColor(gridcolor.darker());
    int x1 = panel.xToPix(0);
    int y1 = panel.yToPix(0);
    int x2 = panel.xToPix(rmax);
    g.drawLine(x1, y1, Math.min(x2, panel.getWidth()-panel.getRightGutter()), y1);
    FontMetrics fm = g2.getFontMetrics();
    int nLabels = (int) (panel.getXMax()/dr/MAJOR_TIC);
    int stride = (nLabels>3) ? 2 : 1;
    double rm = Math.min(rmax, panel.getXMax());
    for(double r = (nLabels>3) ? stride*MAJOR_TIC*dr : MAJOR_TIC*dr; r<=rm; r += (stride*MAJOR_TIC*dr)) {
      String label = getLabel(r);
      int sW = fm.stringWidth(label)+4;
      int sH = fm.getHeight();
      g2.setColor(new Color(247, 247, 247));
      int x0 = panel.xToPix(r), y0 = panel.yToPix(0);
      g2.fill(new Rectangle2D.Double(x0-sW/2, y0+3, sW, sH));
      g2.setColor(Color.black);
      g2.draw(new Rectangle2D.Double(x0-sW/2, y0+3, sW, sH));
      g2.setColor(drawingPanel.getForeground());
      g2.drawString(label, x0-sW/2+2, y0+1+sH);
    }
  }

  String getLabel(double r) {
    if(r>=10) {
      return Integer.toString((int) r);
    }
    return Double.toString(r);
  }

  /**
   * Draws the rings for the polar plot.
   * @param panel
   * @param g
   *
   * @return double the ring separation used
   */
  public double drawRings(double rmax, DrawingPanel panel, Graphics g) {
    double dr = Math.max(this.dr, 1.0e-9);
    if(autospaceRings) {
      int exponent = (int) (Math.log(rmax)/LOG10);
      double decade = Math.pow(10, exponent-1);
      dr = decade;
      while(rmax/dr>5*MAJOR_TIC) { // increase dr if we have more than 25 rings
        dr *= 2;
        if((dr/decade>3.5)&&(dr/decade<4.5)) {
          dr = 5*decade;
          decade *= 10;
        }
      }
    } else {
      int nrings = (int) (rmax/dr);
      while(nrings>10*MAJOR_TIC) {
        dr *= 2;
        nrings = (int) (rmax/dr);
      }
    }
    int xcenter = panel.xToPix(0);
    int ycenter = panel.yToPix(0);
    int xrad = (int) (panel.getXPixPerUnit()*rmax);
    int yrad = (int) (panel.getYPixPerUnit()*rmax);
    if(interiorColor!=null) {
      g.setColor(interiorColor);
      g.fillOval(xcenter-xrad, ycenter-yrad, 2*xrad, 2*yrad);
    }
    int counter = 0;
    for(double r = 0; r<=rmax; r += dr) {
      g.setColor(gridcolor);
      xrad = panel.xToPix(r)-xcenter;
      yrad = ycenter-panel.yToPix(r);
      if(counter%MAJOR_TIC==0) {
        g.setColor(gridcolor.darker());
      }
      g.drawOval(xcenter-xrad, ycenter-yrad, 2*xrad, 2*yrad);
      counter++;
    }
    return dr;
  }

  /**
   * Draws the spokes for the polar plot.
   * @param panel
   * @param g
   */
  public void drawSpokes(double rmax, DrawingPanel panel, Graphics g) {
    g.setColor(gridcolor);
    for(double theta = 0; theta<Math.PI; theta += dtheta) {
      int x1 = panel.xToPix(rmax*Math.cos(theta));
      int y1 = panel.yToPix(rmax*Math.sin(theta));
      int x2 = panel.xToPix(-rmax*Math.cos(theta));
      int y2 = panel.yToPix(-rmax*Math.sin(theta));
      g.drawLine(x1, y1, x2, y2);
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
