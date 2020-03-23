/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

public class InteractiveLabel extends MeasuredCircle implements Interactive {
  public static final int TOP_LEFT_LOCATION = 0;
  public static final int CENTER_LOCATION = 1;
  public static final int TOP_CENTER_LOCATION = 2;
  public int connection_location = CENTER_LOCATION;
  boolean enabled = true;
  protected String text = null;
  protected Font font;
  protected String fontname = "TimesRoman"; // The logical name of the font to use //$NON-NLS-1$
  protected int fontsize = 14;              // The font size
  protected int fontstyle = Font.PLAIN;     // The font style
  Box box = new Box();

  /**
   * Constructor InteractiveLabel
   */
  public InteractiveLabel() {
    super(0, 0);
    color = Color.YELLOW;
    pixRadius = 1;
    font = new Font(fontname, fontstyle, fontsize);
  }

  /**
   * Constructor InteractiveLabel
   * @param str
   */
  public InteractiveLabel(String str) {
    this();
    text = TeXParser.parseTeX(str);
  }

  public void setXY(double _x, double _y) {
    x = _x;
    y = _y;
  }

  public void setText(String _text) {
    text = TeXParser.parseTeX(_text);
  }

  public void setText(String _text, double _x, double _y) {
    x = _x;
    y = _y;
    text = TeXParser.parseTeX(_text);
  }

  public void resetBoxSize() {
    box.boxHeight = 0;
    box.boxWidth = 0;
  }

  /**
   * Sets the location of the connection point.
   * Location values are:TOP_LEFT_LOCATION, TOP_CENTER_LOCATION
   *
   * @param location int
   */
  public void setConnectionPoint(int location) {
    connection_location = location;
  }

  /**
   * Sets the label's offset in the x direction.
   * @param offset int
   */
  public void setOffsetX(int offset) {
    box.xoffset = offset;
  }

  /**
   * Gets the label's offset in the x direction.
   * @param offset int
   */
  public int getOffsetX() {
    return box.xoffset;
  }

  /**
   * Sets the label's offset in the y direction.
   * @param offset int
   */
  public void setOffsetY(int offset) {
    box.yoffset = offset;
  }

  /**
   * Gets the label's offset in the y direction.
   * @param offset int
   */
  public int getOffsetY() {
    return box.yoffset;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    String tempText = text; // local reference for thread safety
    if(tempText==null) {
      return;
    }
    super.draw(panel, g);
    box.computeBoxMetrics(panel, g);
    int xpix = panel.xToPix(x);
    int ypix = panel.yToPix(y);
    g.setColor(Color.YELLOW);
    g.drawLine(xpix, ypix, box.connectX, box.connectY);
    g.setColor(Color.BLACK);
    box.draw(panel, g);
  }

  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(box.isInside(panel, xpix, ypix)) {
      return box;
    }
    return null;
  }

  /**
   * Checks to see if this object is enabled and if the pixel coordinates are inside the drawable.
   *
   * @param panel
   * @param xpix
   * @param ypix
   * @return true if the pixel coordinates are inside; false otherwise
   */
  public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
    return box.isInside(panel, xpix, ypix);
  }

  /**
   * Enables mouse interactions
   * @param enabled boolean
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    box.setEnabled(enabled);
  }

  /**
   * Gets the mouse interaction enabled property.
   * @return boolean
   */
  public boolean isEnabled() {
    return enabled;
  }

  class Box extends AbstractInteractive {
    int leftPix, topPix;
    int boxHeight = 0, boxWidth = 0; // the box width and height
    int xpix = 0, ypix = 0;
    int xoffset = 0, yoffset = 0;
    int connectX, connectY;
    DrawingPanel panel;

    /**
     * Draws the box and the text.
     *
     * @param panel DrawingPanel
     * @param g Graphics
     */
    public void draw(DrawingPanel panel, Graphics g) {
      this.panel = panel;
      String tempText = text; // local reference for thread safety
      if(tempText==null) {
        return;
      }
      Graphics2D g2 = (Graphics2D) g;
      g2.setColor(color);
      Font oldFont = g2.getFont();
      g2.setFont(font);
      Shape clipShape = g2.getClip();
      g2.setClip(0, 0, panel.getWidth(), panel.getHeight());
      g2.setColor(Color.YELLOW);
      g2.fillRect(leftPix, topPix, boxWidth, boxHeight);
      g2.setColor(Color.BLACK);
      g2.drawRect(leftPix, topPix, boxWidth, boxHeight);
      g2.drawString(tempText, leftPix+3, topPix+boxHeight-2);
      g2.setFont(oldFont);
      g2.setClip(clipShape);
    }

    void computeBoxMetrics(DrawingPanel panel, Graphics g) {
      String tempText = text; // local reference for thread safety
      if((tempText==null)||(panel==null)) {
        return;
      }
      Graphics2D g2 = (Graphics2D) g;
      Font oldFont = g2.getFont();
      g2.setFont(font);
      FontMetrics fm = g2.getFontMetrics();
      int sh = fm.getAscent()+2;           // current string height
      int sw = fm.stringWidth(tempText)+6; // current string width
      boxHeight = Math.max(boxHeight, sh);
      boxWidth = Math.max(boxWidth, sw);
      xpix = panel.xToPix(InteractiveLabel.this.x);
      ypix = panel.yToPix(InteractiveLabel.this.y);
      connectX = leftPix = xpix+xoffset;
      connectY = topPix = ypix+yoffset;
      if(InteractiveLabel.this.connection_location==TOP_CENTER_LOCATION) {
        connectX += boxWidth/2;
      } else if(InteractiveLabel.this.connection_location==CENTER_LOCATION) {
        connectX += boxWidth/2;
        connectY += boxHeight/2;
      }
      g2.setFont(oldFont);
    }

    public void setXY(double x, double y) {
      if(panel==null) {
        return;
      }
      int x1 = panel.xToPix(InteractiveLabel.this.x);
      int x2 = panel.xToPix(x);
      xoffset = x2-x1;
      int y1 = panel.yToPix(InteractiveLabel.this.y);
      int y2 = panel.yToPix(y);
      yoffset = y2-y1;
    }

    public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
      if((xpix>=leftPix)&&(xpix<=leftPix+boxWidth)&&(ypix>=topPix)&&(ypix<=topPix+boxHeight)) {
        return true;
      }
      return false;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
