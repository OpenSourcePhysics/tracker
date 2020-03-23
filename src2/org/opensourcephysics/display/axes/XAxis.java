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
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * XAxis: an x axis that knows how to draw itself in a drawing panel.
 * Copyright:    Copyright (c) 2005  Gould, Christian, and Tobochnik
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class XAxis extends XYAxis {
  /**
   * Constructor XAxis
   */
  public XAxis() {
    this("X Axis"); //$NON-NLS-1$
  }

  /**
   * Constructor XAxis
   * @param title
   */
  public XAxis(String title) {
    super();
    setTitle(title);
  }

  /**
   * Draws the axis in the drawing panel.
   * @param drawingPanel
   * @param g
   */
  public void draw(DrawingPanel drawingPanel, Graphics g) {
    int pixLoc = drawingPanel.yToPix(location);
    if(pixLoc<1) {
      location = drawingPanel.getYMin();
    }
    if(pixLoc>drawingPanel.getHeight()-1) {
      location = drawingPanel.getYMax();
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
      location = (drawingPanel.getYMax()+drawingPanel.getYMin())/2;
    }
    int xo = leftGutter;
    int yo = drawingPanel.yToPix(location);
    int w = drawingPanel.getWidth()-leftGutter-rightGutter;
    //int h = drawingPanel.getHeight()-bottomGutter-topGutter;
    g.drawLine(xo, yo, xo+w, yo);
    calculateLabels(drawingPanel.getXMin(), drawingPanel.getXMax(), 1+w/35);
    String[] temp_strings = label_string; // get a reference for thread safety
    double[] temp_values = label_value;   // get a reference for thread safety
    if(temp_strings.length!=temp_values.length) {
      return;
    }
    for(int i = 0, n = temp_strings.length; i<n; i++) {
      if(axisType==LINEAR) {
        int xpix = drawingPanel.xToPix(temp_values[i]*decade_multiplier);
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xpix, topGutter+1, xpix, drawingPanel.getHeight()-bottomGutter-1);
          g.setColor(foreground);
        }
        g.drawLine(xpix, yo-5, xpix, yo+5);
        sw = fm.stringWidth(temp_strings[i]);
        g.drawString(temp_strings[i], xpix-sw/2, yo+18);
      } else { // log axis
        int xpix = drawingPanel.xToPix(Math.pow(10, temp_values[i]*decade_multiplier));
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xpix, topGutter+1, xpix, drawingPanel.getHeight()-bottomGutter-1);
          g.setColor(foreground);
        }
        g.drawLine(xpix, yo-5, xpix, yo+5);
        sw = fm.stringWidth(logBase);
        drawMultiplier(xpix-sw/2, yo+18, (int) temp_values[i], (Graphics2D) g);
      }
    }
    int ypix = drawingPanel.getHeight()-Math.max(bottomGutter/2, 6);
    Graphics2D g2 = (Graphics2D) g;
    Font oldFont = g2.getFont();
    if((axisType==LINEAR)&&(label_exponent!=0)) {
      g2.setColor(Color.red);
      g2.drawString("x10", drawingPanel.getWidth()-36, ypix);               //$NON-NLS-1$
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9.0f));
      g2.drawString(""+label_exponent, drawingPanel.getWidth()-16, ypix-6); //$NON-NLS-1$
    }
    g2.setColor(Color.black);
    if(axisLabel!=null) {
      axisLabel.setX((drawingPanel.getXMax()+drawingPanel.getXMin())/2);
      axisLabel.setY(drawingPanel.getYMin()-20/drawingPanel.getYPixPerUnit());
      axisLabel.setColor(foreground);
      axisLabel.draw(drawingPanel, g2);
    }
    g2.setFont(oldFont);
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
    int w = drawingPanel.getWidth()-leftGutter-rightGutter;
    calculateLabels(drawingPanel.getXMin(), drawingPanel.getXMax(), 1+w/35);
    String[] temp_strings = label_string; // get a reference for thread safety
    double[] temp_values = label_value;   // get a reference for thread safety
    if(temp_strings.length!=temp_values.length) {
      return;
    }
    for(int i = 0, n = temp_strings.length; i<n; i++) {
      if(axisType==LINEAR) {
        int xpix = drawingPanel.xToPix(temp_values[i]*decade_multiplier);
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xpix, topGutter+1, xpix, yo);
          g.setColor(foreground);
        }
        g.drawLine(xpix, yo, xpix, yo+5);
        sw = fm.stringWidth(temp_strings[i]);
        g.drawString(temp_strings[i], xpix-sw/2, yo+18);
      } else { // log axis
        int xpix = drawingPanel.xToPix(Math.pow(10, temp_values[i]*decade_multiplier));
        if(showMajorGrid) {
          g.setColor(majorGridColor);
          g.drawLine(xpix, topGutter+1, xpix, yo);
          g.setColor(foreground);
        }
        g.drawLine(xpix, yo, xpix, yo+5);
        sw = fm.stringWidth(logBase);
        drawMultiplier(xpix-sw/2, yo+18, (int) temp_values[i], (Graphics2D) g);
      }
    }
    g.drawLine(xo, yo, xo+w, yo);
    int ypix = drawingPanel.getHeight()-Math.max(bottomGutter/2-15, 6);
    Graphics2D g2 = (Graphics2D) g;
    if((axisType==LINEAR)&&(label_exponent!=0)) {
      g2.setColor(Color.red);
      g2.drawString("x10", drawingPanel.getWidth()-36, ypix);               //$NON-NLS-1$
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9.0f));
      g2.drawString(""+label_exponent, drawingPanel.getWidth()-16, ypix-6); //$NON-NLS-1$
    }
    g2.setColor(Color.black);
    if(axisLabel!=null) {
      //axisLabel.setX((drawingPanel.getXMax()+drawingPanel.getXMin())/2);
      axisLabel.setX(drawingPanel.pixToX((drawingPanel.getWidth()+leftGutter-rightGutter)/2));
      //axisLabel.setY(drawingPanel.pixToY(drawingPanel.getHeight()-Math.max(bottomGutter/2-10, 10)));
      FontMetrics labelFontMetrics = drawingPanel.getFontMetrics(labelFont);
      axisLabel.setY(drawingPanel.pixToY(drawingPanel.getHeight()-Math.max(bottomGutter-2*labelFontMetrics.getHeight()-4, 10)));
      axisLabel.setColor(foreground);
      axisLabel.draw(drawingPanel, g2);
    }
  }

  // implements interactive interface
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!enabled) {
      return null;
    }
    if(Math.abs(panel.yToPix(location)-ypix)<2) {
      return this;
    }
    return null;
  }

  public void setXY(double x, double y) {
    location = y;
  }

  public void setY(double y) {
    location = y;
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
