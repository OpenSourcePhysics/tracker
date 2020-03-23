/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * A text line that is offset relative to a drawing panel's display area.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class TitleText extends DrawableTextLine {
  public static final int CENTER = 0; // shadows superclass field
  public static final int BOTTOM = 1;
  public static final int LEFT = 2;   // shadows superclass field
  public static final int TOP = 3;
  public static final int RIGHT = 4;  // shadows superclass field
  public static final int CUSTOM = 5;
  int location = TOP;
  int xoff = 0, yoff = 0;             // offsets
  boolean dirty = true;

  /**
   * TitleText draws a TextLine relative to a drawing panel's edge.
   *
   * @param text String
   */
  public TitleText(String text) {
    super(text, 0, 0);
    setFont(new Font("TimesRoman", Font.BOLD, 14)); //$NON-NLS-1$
    setJustification(TextLine.CENTER);
  }

  /**
   * Sets the x and y offsets.
   *
   * @param xoff double
   * @param yoff double
   */
  public void setOffsets(int xoff, int yoff) {
    this.xoff = xoff;
    this.yoff = yoff;
  }

  /**
   * Sets the location of the text relative to a display area edge
   * @param location int
   */
  public void setLocation(int location) {
    this.location = location;
    switch(location) {
       case LEFT :
       case RIGHT :
         theta = Math.PI/2;
         break;
       default :
         theta = 0;
    }
  }

  /**
 * Sets the font used to display the text.
 *
 * @param font Font
 */
  public void setFont(Font font) {
    super.setFont(font);
    dirty = true;
  }

  /**
   * Sets the text to be displayed.
   *
   * @param text String
   */
  public void setText(String text) {
    super.setText(text);
    dirty = true;
  }

  /**
 * Draws the TextLine offset from the location.
 *
 * @param panel DrawingPanel
 * @param g Graphics
 */
  public void draw(DrawingPanel panel, Graphics g) {
    if(dirty) {
      parseText(g);
      dirty = false;
    }
    int xpix = 0, ypix = 0;
    switch(location) {
       case CENTER :
         xpix = panel.getLeftGutter()+(panel.width-panel.getLeftGutter()-panel.getRightGutter())/2;
         ypix = panel.getTopGutter()+(panel.height-panel.getTopGutter()-panel.getBottomGutter())/2;
         break;
       case BOTTOM :
         xpix = panel.getLeftGutter()+(panel.width-panel.leftGutter-panel.rightGutter)/2;
         ypix = (panel.getBottomGutter()>height+yoff) ?       // is gutter large enough?
           panel.getHeight()-panel.bottomGutter+yoff+height : // draw in bottom gutter
             panel.getHeight()-panel.bottomGutter-yoff;       // draw in display area
         break;
       case LEFT :
         xpix = (panel.leftGutter>height+xoff) ?              // is left gutter large enought?
           panel.leftGutter-xoff :                            // draw in left of gutter
             panel.leftGutter+xoff+height;                    // draw in display area
         ypix = panel.getTopGutter()+(panel.height-panel.getTopGutter()-panel.getBottomGutter())/2;
         break;
       default :
       case TOP :
         xpix = panel.getLeftGutter()+(panel.width-panel.leftGutter-panel.rightGutter)/2;
         ypix = (panel.getTopGutter()>ascent+yoff) ?          // is gutter large enough?
           panel.getTopGutter()-yoff-descent-1 :              // draw in gutter
             panel.getTopGutter()+yoff+ascent+1;              // draw in display area
         break;
       case RIGHT :
         xpix = (panel.rightGutter>height+xoff) ?             // is right gutter large enought?
           panel.width-panel.leftGutter+xoff+height :         // draw in left of gutter
             panel.width-panel.leftGutter-xoff;               // draw in display area
         ypix = panel.getTopGutter()+(panel.height-panel.getTopGutter()-panel.getBottomGutter())/2;
         break;
       case CUSTOM :
         xpix = xoff;
         ypix = yoff;
         break;
    }
    Graphics2D g2 = (Graphics2D) g;
    Shape currentClip = g2.getClip();
    Rectangle viewRect = panel.getViewRect();
    if(viewRect==null) {
      g2.setClip(0, 0, panel.getWidth(), panel.getHeight());
    } else { // set the clip to the entire viewport
      g2.setClip(viewRect.x, viewRect.y, viewRect.x+viewRect.width, viewRect.y+viewRect.height);
    }
    drawText(g, xpix, ypix);
    g2.setClip(currentClip); // restore the original clipping
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
