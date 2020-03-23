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

public class TextBox implements Drawable {
  public static final int COORDINATE_PLACEMENT = 0;
  public static final int PIXEL_PLACEMENT = 1;
  public static final int RELATIVE_PLACEMENT = 2;
  public static final int BOTTOM_LEFT_PLACEMENT = 3;
  public static final int TOP_LEFT_PLACEMENT = 4;
  public static final int BOTTOM_RIGHT_PLACEMENT = 5;
  public static final int TOP_RIGHT_PLACEMENT = 6;
  public static final int BOTTOM_LEFT_GUTTER_PLACEMENT = 7;
  public static final int TOP_LEFT_GUTTER_PLACEMENT = 8;
  public static final int BOTTOM_RIGHT_GUTTER_PLACEMENT = 9;
  public static final int TOP_RIGHT_GUTTER_PLACEMENT = 10;
  public static final int TOP_RIGHT_ALIGNMENT = 0;
  public static final int TOP_CENTER_ALIGNMENT = 1;
  public int placement_mode = COORDINATE_PLACEMENT;
  public int alignment_mode = TOP_RIGHT_ALIGNMENT;
  public int xoffset = 0, yoffset = 0;
  protected String text = null;
  protected Font font;
  protected String fontname = "TimesRoman";  // The logical name of the font to use //$NON-NLS-1$
  protected int fontsize = 14;               // The font size
  protected int fontstyle = Font.PLAIN;      // The font style
  protected Color color = Color.black;
  protected double x, y;
  protected int xpix = 0, ypix = 0;
  protected int boxHeight = 0, boxWidth = 0; // the box width and height

  /**
   * Constructor TextBox
   */
  public TextBox() {
    font = new Font(fontname, fontstyle, fontsize);
  }

  /**
   * Constructor TextBox
   * @param str
   */
  public TextBox(String str) {
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
    boxHeight = 0;
    boxWidth = 0;
  }

  public void draw(DrawingPanel panel, Graphics g) {
    String tempText = text; // local reference for thread safety
    if(tempText==null) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(color);
    Font oldFont = g2.getFont();
    g2.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    int sh = fm.getAscent()+2;           // current string height
    int sw = fm.stringWidth(tempText)+6; // current string width
    boxHeight = Math.max(boxHeight, sh);
    boxWidth = Math.max(boxWidth, sw);
    switch(placement_mode) {
       case PIXEL_PLACEMENT :
         xpix = (int) x;
         ypix = (int) y;
         break;
       case RELATIVE_PLACEMENT :
         xpix = (int) (x*panel.getWidth());
         ypix = (int) ((1-y)*panel.getHeight());
         break;
       case TOP_LEFT_PLACEMENT :
         xpix = 0;
         ypix = 0;
         break;
       case TOP_LEFT_GUTTER_PLACEMENT :
         xpix = panel.getLeftGutter();
         ypix = panel.getTopGutter();
         break;
       case BOTTOM_LEFT_PLACEMENT :
         xpix = 0;
         ypix = panel.getHeight()-boxHeight-yoffset-1;
         break;
       case BOTTOM_LEFT_GUTTER_PLACEMENT :
         xpix = panel.getLeftGutter();
         ypix = panel.getHeight()-boxHeight-yoffset-1-panel.getBottomGutter();
         break;
       case TOP_RIGHT_PLACEMENT :
         xpix = panel.getWidth()-boxWidth-1;
         ypix = 0;
         break;
       case TOP_RIGHT_GUTTER_PLACEMENT :
         xpix = panel.getWidth()-boxWidth-1-panel.getRightGutter();
         ypix = panel.getTopGutter();
         break;
       case BOTTOM_RIGHT_PLACEMENT :
         xpix = panel.getWidth()-boxWidth-1;
         ypix = panel.getHeight()-boxHeight-yoffset-1;
         break;
       case BOTTOM_RIGHT_GUTTER_PLACEMENT :
         xpix = panel.getWidth()-boxWidth-1-panel.getRightGutter();
         ypix = panel.getHeight()-boxHeight-yoffset-1-panel.getBottomGutter();
         break;
       default :
         xpix = panel.xToPix(x);
         ypix = panel.yToPix(y);
         break;
    }
    int xoffset = this.xoffset, yoffset = this.yoffset;
    if(this.alignment_mode==TOP_CENTER_ALIGNMENT) {
      xoffset -= boxWidth/2;
    }
    Shape clipShape = g2.getClip();
    g2.setClip(0, 0, panel.getWidth(), panel.getHeight());
    g2.setColor(Color.yellow);
    g2.fillRect(xpix+xoffset, ypix+yoffset, boxWidth, boxHeight);
    g2.setColor(Color.black);
    g2.drawRect(xpix+xoffset, ypix+yoffset, boxWidth, boxHeight);
    g2.drawString(tempText, xpix+3+xoffset, ypix+boxHeight-2+yoffset);
    g2.setFont(oldFont);
    g2.setClip(clipShape);
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
