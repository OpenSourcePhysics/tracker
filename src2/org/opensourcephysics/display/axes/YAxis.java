/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * YAxis: a y axis that knows how to draw itself in a drawing panel.
 * Copyright:    Copyright (c) 2005  Gould, Christian, and Tobochnik
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class YAxis extends XYAxis {
  AffineTransform rot90 = AffineTransform.getRotateInstance(-Math.PI/2);

  /**
   * Constructor YAxis
   */
  public YAxis() {
    this("Y Axis"); //$NON-NLS-1$
  }

  /**
   * Constructor YAxis
   * @param title
   */
  public YAxis(String title) {
    super();
    setTitle(title);
    axisLabel.setTheta(Math.PI/2);
  }

  /**
   * Draws the axis in the drawing panel.
   * @param drawingPanel
   * @param g
   */
  public void draw(DrawingPanel drawingPanel, Graphics g) {
    int pixLoc = drawingPanel.xToPix(location);
    if(pixLoc<1) {
      location = drawingPanel.getXMin();
    }
    if(pixLoc>drawingPanel.getWidth()-1) {
      location = drawingPanel.getXMax();
    }
    Graphics2D g2 = (Graphics2D) g;
    Shape clipShape = g2.getClip();
    g2.clipRect(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
    switch(locationType) {
       case DRAW_AT_LOCATION :
       case DRAW_IN_DISPLAY :
         drawInsideDisplay(drawingPanel, g);
         break;
       case DRAW_IN_GUTTER :
         drawInsideGutter(drawingPanel, g);
         break;
       default :
         drawInsideDisplay(drawingPanel, g);
         break;
    }
    g2.setClip(clipShape);
  }

  private void drawInsideDisplay(DrawingPanel drawingPanel, Graphics g) {
    Color foreground = drawingPanel.getForeground();
    int bottomGutter = drawingPanel.getBottomGutter();
    int rightGutter = drawingPanel.getRightGutter();
    int leftGutter = drawingPanel.getLeftGutter();
    int topGutter = drawingPanel.getTopGutter();
    FontMetrics fm = g.getFontMetrics();
    int sw = 0; // the string width
    g.setColor(foreground);
    if(locationType==XYAxis.DRAW_IN_DISPLAY) {
      location = (drawingPanel.getXMax()+drawingPanel.getXMin())/2;
    }
    int xo = drawingPanel.xToPix(location);
    int yo = drawingPanel.getHeight()-bottomGutter;
    //int w = drawingPanel.getWidth()-leftGutter-rightGutter;
    int h = drawingPanel.getHeight()-bottomGutter-topGutter;
    calculateLabels(drawingPanel.getYMin(), drawingPanel.getYMax(), 1+h/35);
    String[] temp_strings = label_string; // get a reference for thread safety
    double[] temp_values = label_value;   // get a reference for thread safety
    if(temp_strings.length!=temp_values.length) {
      return;
    }
    for(int i = 0, n = temp_values.length; i<n; i++) {
      if(axisType==LINEAR) {
        int ypix = drawingPanel.yToPix(temp_values[i]*decade_multiplier);
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(leftGutter, ypix, drawingPanel.getWidth()-rightGutter-2, ypix);
          g.setColor(foreground);
        }
        g.drawLine(xo-5, ypix, xo+5, ypix);
        sw = fm.stringWidth(temp_strings[i]);
        g.drawString(temp_strings[i], xo-sw-7, ypix+5);
      } else { // log axis
        int ypix = drawingPanel.yToPix(Math.pow(10, temp_values[i]*decade_multiplier));
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(leftGutter, ypix, drawingPanel.getWidth()-rightGutter-2, ypix);
          g.setColor(foreground);
        }
        g.drawLine(xo-5, ypix, xo+5, ypix);
        sw = fm.stringWidth(logBase);
        drawMultiplier(xo-sw-7, ypix+5, (int) temp_values[i], (Graphics2D) g);
      }
    }
    g.drawLine(xo, yo, xo, yo-h);
    Graphics2D g2 = (Graphics2D) g;
    if((axisType==LINEAR)&&(label_exponent!=0)) {
      g2.setColor(Color.red);
      g2.drawString("x10", 5, 18);              //$NON-NLS-1$
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9.0f));
      g2.drawString(""+label_exponent, 25, 12); //$NON-NLS-1$
    }
    g2.setColor(Color.black);
    if(axisLabel!=null) {
      axisLabel.setY((drawingPanel.getYMax()+drawingPanel.getYMin())/2);
      // axisLabel.setX(drawingPanel.getXMin()-(leftGutter-20)/drawingPanel.getXPixPerUnit());
      axisLabel.setX(drawingPanel.pixToX(Math.max(leftGutter/2-10, 18)));
      axisLabel.setColor(foreground);
      axisLabel.draw(drawingPanel, g2);
    }
  }

  private void drawInsideGutter(DrawingPanel drawingPanel, Graphics g) {
    Color foreground = drawingPanel.getForeground();
    int bottomGutter = drawingPanel.getBottomGutter();
    int rightGutter = drawingPanel.getRightGutter();
    int leftGutter = drawingPanel.getLeftGutter();
    int topGutter = drawingPanel.getTopGutter();
    FontMetrics fm = g.getFontMetrics();
    int sw = 0; // the string width
    g.setColor(foreground);
    int xo = leftGutter;
    int yo = drawingPanel.getHeight()-bottomGutter-1;
    //int w = drawingPanel.getWidth()-leftGutter-rightGutter;
    int h = drawingPanel.getHeight()-bottomGutter-topGutter;
    g.drawLine(xo, yo, xo, yo-h);
    calculateLabels(drawingPanel.getYMin(), drawingPanel.getYMax(), 1+h/35);
    String[] temp_strings = label_string; // get a reference for thread safety
    double[] temp_values = label_value;   // get a reference for thread safety
    if(temp_strings.length!=temp_values.length) {
      return;
    }
    for(int i = 0, n = temp_values.length; i<n; i++) {
      if(axisType==LINEAR) {
        int ypix = drawingPanel.yToPix(temp_values[i]*decade_multiplier);
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xo, ypix, drawingPanel.getWidth()-rightGutter-2, ypix);
          g.setColor(foreground);
        }
        g.drawLine(xo-5, ypix, xo, ypix);
        sw = fm.stringWidth(temp_strings[i]);
        g.drawString(temp_strings[i], xo-sw-7, ypix+5);
      } else { // log axis
        int ypix = drawingPanel.yToPix(Math.pow(10, temp_values[i]*decade_multiplier));
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xo, ypix, drawingPanel.getWidth()-rightGutter-2, ypix);
          g.setColor(foreground);
        }
        g.drawLine(xo-5, ypix, xo, ypix);
        sw = fm.stringWidth(logBase);
        drawMultiplier(xo-sw-14, ypix+5, (int) temp_values[i], (Graphics2D) g);
      }
    }
    Graphics2D g2 = (Graphics2D) g;
    if((axisType==LINEAR)&&(label_exponent!=0)) {
      g2.setColor(Color.red);
      g2.drawString("x10", 5, 18);              //$NON-NLS-1$
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9.0f));
      g2.drawString(""+label_exponent, 25, 12); //$NON-NLS-1$
    }
    g2.setColor(Color.black);
    if(axisLabel!=null) {
      axisLabel.setY((drawingPanel.getYMax()+drawingPanel.getYMin())/2);
      // axisLabel.setX(drawingPanel.getXMin()-(leftGutter-20)/drawingPanel.getXPixPerUnit());
      axisLabel.setX(drawingPanel.pixToX(Math.max(leftGutter/2-10, 18)));
      axisLabel.setColor(foreground);
      axisLabel.draw(drawingPanel, g2);
    }
  }

  // implements interactive drawable interface
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!enabled) {
      return null;
    }
    if(Math.abs(panel.xToPix(location)-xpix)<2) {
      return this;
    }
    return null;
  }

  public void setXY(double x, double y) {
    location = x;
  }

  public void setX(double x) {
    location = x;
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
