/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import org.opensourcephysics.display.DrawableTextLine;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.tools.FontSizer;

/**
 * An abstract class for axes that defines font and title accessor methods.
 * <p>Copyright (c) 2017</p>
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractAxes implements DrawableAxes {
  /** default gutters. */
  protected int defaultLeftGutter = 45;
  protected int defaultTopGutter = 25;
  protected int defaultRightGutter = 25;
  protected int defaultBottomGutter = 45;
  protected boolean visible = true;
  protected Font titleFont = new Font("Dialog", Font.BOLD, 14);          //$NON-NLS-1$
  protected Font labelFont = new Font("SansSerif", Font.PLAIN, 9);       //$NON-NLS-1$
  protected Font superscriptFont = new Font("SansSerif", Font.PLAIN, 9); //$NON-NLS-1$
  protected DecimalFormat labelFormat = new DecimalFormat("0.0");        //$NON-NLS-1$
  protected Color gridcolor = Color.lightGray;
  protected Color interiorColor = Color.white;
  protected DrawableTextLine titleLine = new DrawableTextLine("", 0, 0); //$NON-NLS-1$
  protected DrawingPanel drawingPanel;

  /**
   * Creates axes that will display themselves within the given drawing panel.
   *
   * @param drawingPanel DrawingPanel
   */
  public AbstractAxes(DrawingPanel drawingPanel) {
    this.drawingPanel = drawingPanel;
    resizeFonts(FontSizer.getFactor(FontSizer.getLevel()), drawingPanel);
    // Changes font size
    FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        resizeFonts(FontSizer.getFactor(FontSizer.getLevel()), AbstractAxes.this.drawingPanel);
      }

    });
  }

  /**
   * Sets gutters that give the best appearance.
   * @param left
   * @param top
   * @param right
   * @param bottom
   */
  public void setDefaultGutters(int left, int top, int right, int bottom) {
    defaultLeftGutter = left;
    defaultTopGutter = top;
    defaultRightGutter = right;
    defaultBottomGutter = bottom;
  }

  public void resetPanelGutters() {
    drawingPanel.setPreferredGutters(defaultLeftGutter, defaultTopGutter, defaultRightGutter, defaultBottomGutter);
  }

  /**
   * Sets the visibility of the axes.
   *
   * @param isVisible true if the axes are visible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Gets the visibility of the axes.
   *
   * @return true if the axes is drawn
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   *  Sets the interior background color.
   *
   * @param  color  The new interiorBackground value
   */
  public void setInteriorBackground(Color color) {
    interiorColor = color;
  }

  /**
   *  Gets the color of the interior of the axes.
   *
   * @return Color
   */
  public Color getInteriorBackground() {
    return interiorColor;
  }

  /**
   * Resizes fonts by the specified factor.
   *
   * @param factor the factor
   * @param panel the drawing panel on which these axes are drawn
   */
  public void resizeFonts(double factor, DrawingPanel panel) {
    labelFont = FontSizer.getResizedFont(labelFont, factor);
    superscriptFont = FontSizer.getResizedFont(superscriptFont, factor);
    titleFont = FontSizer.getResizedFont(titleFont, factor);
    titleLine.setFont(titleFont);
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
