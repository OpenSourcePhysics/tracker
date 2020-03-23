/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * The TextPanel renders text in a component.
 * The text is surrounded by a border.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class TextPanel extends JPanel {
  protected static Dimension ZEROSIZE = new Dimension(0, 0);
  protected String text = "";                     //$NON-NLS-1$
  protected Font font;
  protected String fontname = "TimesRoman";       // The logical name of the font to use //$NON-NLS-1$
  protected int fontsize = 14;                    // The font size
  protected int fontstyle = Font.PLAIN;           // The font style
  protected Color textColor = Color.black;        // text color
  protected Color backgroundColor = Color.yellow; // background color
  protected Dimension dim = ZEROSIZE;

  /**
   * Constructs a TextPanel and places it within the given drawing panel.
   */
  public TextPanel() {
    setBackground(backgroundColor);
    font = new Font(fontname, fontstyle, fontsize);
  }

  /**
   * Constructs a TextPanel and places it within the given drawing panel.
   *
   * @param text the text to be displayed
   */
  public TextPanel(String text) {
    this();
    setText(text);
  }

  /**
   * Sets the text.
   *
   * The position is ignored if the location is set to a corner.
   *
   * @param _text
   */
  public void setText(String _text) {
    _text = TeXParser.parseTeX(_text);
    if(text==_text) {
      return;
    }
    text = _text;
    if(text==null) {
      text = ""; //$NON-NLS-1$
    }
    final Container c = this.getParent();
    if(c==null) {
      return;
    }
    Runnable runner = new Runnable() {
      public synchronized void run() {
        if(c.getLayout() instanceof OSPLayout) {
          ((OSPLayout) c.getLayout()).quickLayout(c, TextPanel.this);
          repaint();
        } else {
          c.validate();
        }
      }

    };
    if(SwingUtilities.isEventDispatchThread()) {
      runner.run();
    } else {
      SwingUtilities.invokeLater(runner);
    }
  }
  
  /**
   * Sets the font. Added by Doug Brown Feb 2018
   * @param font the font
   */
  public void setMessageFont(Font font) {
  	this.font = font;
  }

  /**
   * Gets the preferred size of this component.
   * @return a dimension object indicating this component's preferred size
   * @see #getMinimumSize
   * @see LayoutManager
   */
  public Dimension getPreferredSize() {
    Container c = this.getParent();
    String text = this.text; // local reference for thread safety
    if((c==null)||text.equals("")) { //$NON-NLS-1$
      return ZEROSIZE;
    }
    Graphics2D g2 = (Graphics2D) c.getGraphics();
    if(g2==null) {
      return ZEROSIZE;
    }
    Font oldFont = g2.getFont();
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics();
    int boxHeight = fm.getAscent()+4;      // current string height
    int boxWidth = fm.stringWidth(text)+6; // current string width
    g2.setFont(oldFont);
    g2.dispose();
    return new Dimension(boxWidth, boxHeight);
  }

  /**
   * Paints this component.
   * @param g
   */
  public void paintComponent(Graphics g) {
    String text = this.text; // local reference for thread safety
    if(!dim.equals(getPreferredSize())) {
      dim = getPreferredSize();
      setSize(dim);
    }
    if(text.equals("")||!isVisible()) { //$NON-NLS-1$
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    //super.paintComponent(g);
    int w = getWidth();
    int h = getHeight();
    Color oldColor=g2.getColor();  // save current color
    Font oldFont = g2.getFont();   // save current font
    g2.setColor(backgroundColor);
    g2.fillRect(0, 0, w, h);
    g2.setColor(textColor);
    g2.setFont(font);
    g2.drawString(text, 3, h-4);
    g2.setColor(Color.black);
    g2.drawRect(0, 0, w-1, h-1);
    g2.setFont(oldFont);   // restore the old font
    g2.setColor(oldColor); // restore the old color
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
