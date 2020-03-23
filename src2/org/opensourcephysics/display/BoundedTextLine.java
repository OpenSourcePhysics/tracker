/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A BoundedTextLine is a line of text that can be rotated and scaled using a mouse.
 *
 * @author W. Christian
 * @version 1.0
 */
public class BoundedTextLine extends BoundedImage {
  Font defaultFont = new Font("Dialog", Font.BOLD, 12); //$NON-NLS-1$
  TextLine textLine;
  int desent = 0;
  int gutter = 6;                                       // gutter is needed for superscripts and subscripts

  /**
   * Constructor BoundedTextLine
   * @param text
   * @param x
   * @param y
   */
  public BoundedTextLine(String text, double x, double y) {
    super(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), x, y);
    textLine = new TextLine(text);
    textLine.setFont(defaultFont);
    color = Color.BLACK;
  }

  /**
   * Sets the font text font.
   *
   * @param font Font
   */
  public void setFont(Font font) {
    textLine.setFont(font);
  }

  /**
   * Gets the font.
   *
   * @return Font
   */
  public Font getFont() {
    return textLine.getFont();
  }

  void checkImageSize(DrawingPanel panel, Graphics g) {
    Font oldFont = g.getFont();
    Rectangle2D rect = oldFont.getStringBounds(textLine.text, ((Graphics2D) g).getFontRenderContext());
    gutter = (int) rect.getHeight()/2; // gutter for superscript and subscripts
    if((image.getWidth(null)!=(int) rect.getWidth()+1)||(image.getHeight(null)!=(int) rect.getHeight()+1+gutter)) {
      image = new BufferedImage((int) rect.getWidth()+1, (int) rect.getHeight()+1+gutter, BufferedImage.TYPE_INT_ARGB);
      width = image.getWidth(null);
      height = image.getHeight(null);
      desent = -(int) rect.getY();
    }
    g.setFont(oldFont);
  }

  /**
   * Draws the image.
   *
   * @param panel  the world in which the arrow is viewed
   * @param g  the graphics context upon which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    checkImageSize(panel, g);
    Graphics ig = image.getGraphics();
    textLine.setColor(color);
    textLine.drawText(ig, 0, desent+gutter/2);
    ig.dispose();
    Composite composite = ((Graphics2D) g).getComposite();
    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
    super.draw(panel, g);
    ((Graphics2D) g).setComposite(composite);
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
