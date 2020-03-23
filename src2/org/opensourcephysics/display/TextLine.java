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
import java.awt.geom.Rectangle2D;
import java.util.Stack;
import java.util.Vector;

/*
**************************************************************************
**
**    Class  TextLine
**
**************************************************************************
**    Copyright (C) 1996 Leigh Brookshaw
**
**    This program is free software; you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation; either version 2 of the License, or
**    (at your option) any later version.
*/

/**
 * TextLine is designed to bundle together all the information required
 * to draw short Strings with subscripts and superscripts.
 *
 * TextLine was modified by W. Christian to add Greek characters using TeX notation.
 *
 * @author  Leigh Brookshaw
 */
public class TextLine {
  /**
   * Center the Text over the point
   */
  public final static int CENTER = 0;

  /**
   * Position the Text to the Left of the  point
   */
  public final static int LEFT = 1;

  /**
   * Position the Text to the Right of the  point
   */
  public final static int RIGHT = 2;

  /**
   * Format to use when parsing a double
   */
  public final static int SCIENTIFIC = 1;

  /**
   * Format to use when parsing a double
   */
  public final static int ALGEBRAIC = 2;
  /*
  ** Minimum Point size allowed for script characters
  */

  final static int MINIMUM_SIZE = 6;
  /*
  **********************
  **
  ** Protected Variables
  **
  *********************/

  /**
   * Decrease in size of successive script levels
   */
  protected double script_fraction = 0.8;

  /**
   * Superscript offset
   */
  protected double sup_offset = 0.6;

  /**
   * Subscript offset
   */
  protected double sub_offset = 0.7;

  /**
   * Font to use for text
   */
  protected Font font = null;

  /**
   * Text color
   */
  protected Color color = null;

  /**
   * Background Color
   */
  protected Color background = null;

  /**
   * The text to display
   */
  protected String text = null;

  /**
   * The logical name of the font to use
   */
  protected String fontname = "TimesRoman"; //$NON-NLS-1$

  /**
   * The font size
   */
  protected int fontsize = 0;

  /**
   * The font style
   */
  protected int fontstyle = Font.PLAIN;

  /**
   * Text justification. Either CENTER, LEFT or RIGHT
   */
  protected int justification = LEFT;

  /**
   * The width of the text using the current Font
   */
  protected int width = 0;

  /**
   * The ascent using the current font
   */
  protected int ascent = 0;

  /**
   * The maximum ascent using the current font
   */
  protected int maxAscent = 0;

  /**
   * The descent using the current font
   */
  protected int descent = 0;

  /**
   * The maximum descent using the current font
   */
  protected int maxDescent = 0;

  /**
   * The height using the current font ie ascent+descent+leading
   */
  protected int height = 0;

  /**
   * The leading using the current font
   */
  protected int leading = 0;

  /**
   * Has the string been parsed! This only needs to be done once
   * unless the font is altered.
   */
  protected boolean parse = true;

  /**
   * Local graphics context.
   */
  protected Graphics lg = null;

  /**
   * The parsed string. Each element in the vector represents
   * a change of context in the string ie font change and offset.
   */
  protected Vector<TextState> list = new Vector<TextState>(8, 4);
  /*
  **********************
  **
  ** Constructors
  **
  *********************/

  /**
   * Instantiate the class
   */
  public TextLine() {}

  /**
   * Instantiate the class.
   * @param s String to parse.
   */
  public TextLine(String s) {
    this.text = TeXParser.parseTeX(s);
  }

  /**
   * Instantiate the class
   * @param s String to parse.
   * @param f Font to use.
   */
  public TextLine(String s, Font f) {
    this(s);
    font = f;
    if(font==null) {
      return;
    }
    fontname = f.getName();
    fontstyle = f.getStyle();
    fontsize = f.getSize();
  }

  /**
   * Instantiate the class
   * @param s String to parse.
   * @param f Font to use.
   * @param c Color to use
   * @param j Justification
   */
  public TextLine(String s, Font f, Color c, int j) {
    this(s, f);
    color = c;
    justification = j;
  }

  /**
   * Instantiate the class
   * @param s String to parse.
   * @param c Color to use
   */
  public TextLine(String s, Color c) {
    this(s);
    color = c;
  }

  /**
   * Instantiate the class
   * @param f Font to use.
   * @param c Color to use
   * @param j Justification
   */
  public TextLine(Font f, Color c, int j) {
    font = f;
    color = c;
    justification = j;
    if(font==null) {
      return;
    }
    fontname = f.getName();
    fontstyle = f.getStyle();
    fontsize = f.getSize();
  }

  /*
  *****************
  **
  ** Public Methods
  **
  *****************/

  /**
   * Create a New Textline object copying the state of the existing
   * object into the new one. The state of the class is the color,
   * font, and justification ie everything but the string.
   */
  public TextLine copyState() {
    return new TextLine(font, color, justification);
  }

  /**
   * Copy the state of the parsed Textline into the existing
   * object.
   * @param t The TextLine to get the state information from.
   */
  public void copyState(TextLine t) {
    if(t==null) {
      return;
    }
    font = t.getFont();
    color = t.getColor();
    justification = t.getJustification();
    if(font==null) {
      return;
    }
    fontname = font.getName();
    fontstyle = font.getStyle();
    fontsize = font.getSize();
    parse = true;
  }

  /**
   * Set the Font to use with the class
   * @param f Font
   */
  public void setFont(Font f) {
    if(f==null) {
      return;
    }
    font = f;
    fontname = f.getName();
    fontstyle = f.getStyle();
    fontsize = f.getSize();
    parse = true;
  }

  /**
   * Set the String to use with the class
   * @param s String
   */
  public void setText(String s) {
    text = TeXParser.parseTeX(s);
    parse = true;
  }

  /**
   * Set the Color to use with the class
   * @param c Color
   */
  public void setColor(Color c) {
    color = c;
  }

  /**
   * Set the Background Color to use with the class
   * @param c Color
   */
  public void setBackground(Color c) {
    background = c;
  }

  /**
   * Set the Justification to use with the class
   * @param i Justification
   */
  public void setJustification(int i) {
    switch(i) {
       case CENTER :
         justification = CENTER;
         break;
       case LEFT :
       default :
         justification = LEFT;
         break;
       case RIGHT :
         justification = RIGHT;
         break;
    }
  }

  /**
   * @return the font the class is using
   */
  public Font getFont() {
    return font;
  }

  /**
   * @return the String the class is using.
   */
  public String getText() {
    return text;
  }

  /**
   * @return the Color the class is using.
   */
  public Color getColor() {
    return color;
  }

  /**
   * @return the Background Color the class is using.
   */
  public Color getBackground() {
    return background;
  }

  /**
   * @return the Justification the class is using.
   */
  public int getJustification() {
    return justification;
  }

  /**
   * @param g Graphics context.
   * @return the Fontmetrics the class is using.
   */
  public FontMetrics getFM(Graphics g) {
    if(g==null) {
      return null;
    }
    if(font==null) {
      return g.getFontMetrics();
    }
    return g.getFontMetrics(font);
  }

  /**
   * @param g Graphics context.
   * @param ch The character.
   * @return the width of the character.
   */
  public int charWidth(Graphics g, char ch) {
    FontMetrics fm;
    if(g==null) {
      return 0;
    }
    if(font==null) {
      fm = g.getFontMetrics();
    } else {
      fm = g.getFontMetrics(font);
    }
    return fm.charWidth(ch);
  }

  /**
   * Returns the bounding box for this string.
   * @param g Graphics
   * @return Rectangle2D
   */
  public Rectangle2D getStringBounds(Graphics g) {
    parseText(g);
    return new Rectangle2D.Float(0, 0, width, height);
  }

  /**
   * @param g Graphics context.
   * @return the width of the parsed text.
   */
  public int getWidth(Graphics g) {
    parseText(g);
    return width;
  }

  /**
   * @param g Graphics context.
   * @return the height of the parsed text.
   */
  public int getHeight(Graphics g) {
    parseText(g);
    return height;
  }

  /**
   * @param g Graphics context.
   * @return the ascent of the parsed text.
   */
  public int getAscent(Graphics g) {
    if(g==null) {
      return 0;
    }
    parseText(g);
    return ascent;
  }

  /**
   * @param g Graphics context.
   * @return the maximum ascent of the parsed text.
   */
  public int getMaxAscent(Graphics g) {
    if(g==null) {
      return 0;
    }
    parseText(g);
    return maxAscent;
  }

  /**
   * @param g Graphics context.
   * @return the descent of the parsed text.
   */
  public int getDescent(Graphics g) {
    if(g==null) {
      return 0;
    }
    parseText(g);
    return descent;
  }

  /**
   * @param g Graphics context.
   * @return the maximum descent of the parsed text.
   */
  public int getMaxDescent(Graphics g) {
    if(g==null) {
      return 0;
    }
    parseText(g);
    return maxDescent;
  }

  /**
   * @param g Graphics context.
   * @return the leading of the parsed text.
   */
  public int getLeading(Graphics g) {
    if(g==null) {
      return 0;
    }
    parseText(g);
    return leading;
  }

  /**
   * parse the text. When the text is parsed the width, height, leading
   * are all calculated. The text will only be truly parsed if
   * the graphics context has changed or the text has changed or
   * the font has changed. Otherwise nothing is done when this
   * method is called.
   * @param g Graphics context.
   */
  public void parseText(Graphics g) {
    TextState current = new TextState();
    char ch;
    Stack<TextState> state = new Stack<TextState>();
    int w = 0;
    if(lg!=g) {
      parse = true;
    }
    lg = g;
    if(!parse) {
      return;
    }
    parse = false;
    width = 0;
    leading = 0;
    ascent = 0;
    descent = 0;
    height = 0;
    maxAscent = 0;
    maxDescent = 0;
    if((text==null)||(g==null)) {
      return;
    }
    list.removeAllElements();
    if(font==null) {
      current.f = g.getFont();
    } else {
      current.f = font;
    }
    state.push(current);
    list.addElement(current);
    for(int i = 0; i<text.length(); i++) {
      ch = text.charAt(i);
      switch(ch) {
         case '$' :
           i++;
           if(i<text.length()) {
             current.s.append(text.charAt(i));
           }
           break;
         /*
         **                    Push the current state onto the state stack
         **                    and start a new storage string
         */
         case '{' :
           w = current.getWidth(g);
           if(!current.isEmpty()) {
             current = current.copyState();
             list.addElement(current);
           }
           state.push(current);
           current.x += w;
           break;
         /*
         **                    Pop the state off the state stack and set the current
         **                    state to the top of the state stack
         */
         case '}' :
           w = current.x+current.getWidth(g);
           state.pop();
           current = state.peek().copyState();
           list.addElement(current);
           current.x = w;
           break;
         case '^' :
           w = current.getWidth(g);
           if(!current.isEmpty()) {
             current = current.copyState();
             list.addElement(current);
           }
           current.f = getScriptFont(current.f);
           current.x += w;
           current.y -= (int) ((current.getAscent(g))*sup_offset+0.5);
           break;
         case '_' :
           w = current.getWidth(g);
           if(!current.isEmpty()) {
             current = current.copyState();
             list.addElement(current);
           }
           current.f = getScriptFont(current.f);
           current.x += w;
           current.y += (int) ((current.getDescent(g))*sub_offset+0.5);
           break;
         default :
           current.s.append(ch);
           break;
      }
    }
    Vector<TextState> vec; // added by W. Christian in case a parse is called during drawing.
    synchronized(list) {
      vec = new Vector<TextState>(list);
    } // added by W. Christian
    for(int i = 0; i<vec.size(); i++) {
      current = (vec.elementAt(i));
      if(!current.isEmpty()) {
        width += current.getWidth(g);
        ascent = Math.max(ascent, Math.abs(current.y)+current.getAscent(g));
        descent = Math.max(descent, Math.abs(current.y)+current.getDescent(g));
        leading = Math.max(leading, current.getLeading(g));
        maxDescent = Math.max(maxDescent, Math.abs(current.y)+current.getMaxDescent(g));
        maxAscent = Math.max(maxAscent, Math.abs(current.y)+current.getMaxAscent(g));
      }
    }
    height = ascent+descent+leading;
    return;
  }

  /**
   * @return true if the text has never been set or is null
   */
  public boolean isNull() {
    return(text==null);
  }

  /**
   * Parse the text then draw it.
   * @param g Graphics context
   * @param x pixel position of the text
   * @param y pixel position of the text
   * @param j justification of the text
   */
  public void drawText(Graphics g, int x, int y, int j) {
    justification = j;
    if(g==null) {
      return;
    }
    drawText(g, x, y);
  }

  /**
   * Parse the text then draw it without any rotation.
   * @param g Graphics context
   * @param x pixel position of the text
   * @param y pixel position of the text
   */
  public void drawText(Graphics g, int x, int y) {
    TextState ts;
    int xoffset = x;
    int yoffset = y;
    if((g==null)||(text==null)) {
      return;
    }
    Graphics lg = g.create();
    if(lg==null) {
      return; // added by W. Christian
    }
    parseText(g);
    if(justification==CENTER) {
      xoffset = x-width/2;
    } else if(justification==RIGHT) {
      xoffset = x-width;
    }
    if(background!=null) {
      lg.setColor(background);
      lg.fillRect(xoffset, yoffset-ascent, width, height);
      lg.setColor(g.getColor());
    }
    if(font!=null) {
      lg.setFont(font);
    }
    if(color!=null) {
      lg.setColor(color);
    }
    Vector<TextState> vec; // added by W. Christian in case a parse is called during drawing.
    synchronized(list) {
      vec = new Vector<TextState>(list);
    } // added by W. Christian
    for(int i = 0; i<vec.size(); i++) {
      ts = (vec.elementAt(i));
      if(ts.f!=null) {
        lg.setFont(ts.f);
      }
      if(ts.s!=null) {
        lg.drawString(ts.toString(), ts.x+xoffset, ts.y+yoffset);
      }
    }
    lg.dispose();
    lg = null;
  }

  /**
   * @return Logical font name of the set font
   */
  public String getFontName() {
    return fontname;
  }

  /**
   * @return Style of the set font
   */
  public int getFontStyle() {
    return fontstyle;
  }

  /**
   * @return Size of the set font
   */
  public int getFontSize() {
    return fontsize;
  }

  /**
   * Set the Logical font name of the current font
   * @param s Logical font name.
   */
  public void setFontName(String s) {
    if(s==null) {
      return;
    }
    fontname = s;
    rebuildFont();
  }

  /**
   * Set the Font style of the current font
   * @param i Font style.
   */
  public void setFontStyle(int i) {
    fontstyle = i;
    rebuildFont();
  }

  /**
   * Set the Font size of the current font
   * @param i Font size.
   */
  public void setFontSize(int i) {
    fontsize = i;
    rebuildFont();
  }

  /*
  ** Rebuild the font using the current fontname, fontstyle, and fontsize.
  */
  private void rebuildFont() {
    parse = true;
    if((fontsize<=0)||(fontname==null)) {
      font = null;
    } else {
      font = new Font(fontname, fontstyle, fontsize);
    }
  }

  /**
   * @param f Font
   * @return The script font version of the parsed font using the
   *        script_fraction variable.
   */
  public Font getScriptFont(Font f) {
    int size;
    if(f==null) {
      return f;
    }
    size = f.getSize();
    if(size<=MINIMUM_SIZE) {
      return f;
    }
    size = (int) ((f.getSize())*script_fraction+0.5);
    if(size<=MINIMUM_SIZE) {
      return f;
    }
    return new Font(f.getName(), f.getStyle(), size);
  }

  /**
   * Parse a double value. Precision is 6 figures, with 7 significant
   * figures.
   * @param d double to parse
   * return <i>true</i> if the parse was successful
   */
  public boolean parseDouble(double d) {
    return parseDouble(d, 7, 6, ALGEBRAIC);
  }

  /**
   * Parse a double value. Number of significant figures is 1 greater than
   * the precision.
   * @param d double to parse
   * @param p precision of the number
   * return <i>true</i> if the parse was successful
   */
  public boolean parseDouble(double d, int p) {
    return parseDouble(d, p+1, p, ALGEBRAIC);
  }

  /**
   * Parse a double value
   * @param d double to parse
   * @param n number of significant figures
   * @param p precision of the number
   * @param f format of the number scientific, algebraic etc.
   * return <i>true</i> if the parse was successful
   */
  public boolean parseDouble(double d, int n, int p, int f) {
    double x = d;
    int left = n-p;
    double right = 0;
    int power;
    int exponent;
    int i;
    StringBuffer s = new StringBuffer(n+4);
    if(left<0) {
      System.out.println("TextLine.parseDouble: Precision > significant figures!"); //$NON-NLS-1$
      return false;
    }
    if(d<0.0) {
      x = -d;
      s.append("-"); //$NON-NLS-1$
    }
    // System.out.println("parseDouble: value = "+x);
    if(d==0.0) {
      exponent = 0;
    } else {
      exponent = (int) (Math.floor(TextLine.log10(x)));
    }
    // System.out.println("parseDouble: exponent = "+exponent);
    power = exponent-(left-1);
    // System.out.println("parseDouble: power = "+power);
    if(power<0) {
      for(i = power; i<0; i++) {
        x *= 10.0;
      }
    } else {
      for(i = 0; i<power; i++) {
        x /= 10.0;
      }
    }
    // System.out.println("parseDouble: adjusted value = "+x);
    left = (int) x;
    s.append(left);
    // System.out.println("parseDouble: left = "+left);
    if(p>0) {
      s.append('.');
      right = x-left;
      for(i = 0; i<p; i++) {
        right *= 10;
        // if(i==p-1) right += 0.5;
        // s.append((int)(right));
        // right -= (int)right;
        int digit = (int) Math.round(right);
        s.append(digit);
        right -= digit;
      }
    }
    // System.out.println("parseDouble: right = "+right);
    if(power!=0) {
      if(f==SCIENTIFIC) {
        s.append('E');
        if(power<0) {
          s.append('-');
        } else {
          s.append('+');
        }
        power = Math.abs(power);
        if(power>9) {
          s.append(power);
        } else {
          s.append('0');
          s.append(power);
        }
      } else {
        s.append("x10{^"); //$NON-NLS-1$
        s.append(power);
        s.append("}");     //$NON-NLS-1$
      }
    }
    setText(s.toString());
    return true;
  }

  /**
   * @param x a double value
   * @return The log<sub>10</sub>
   */
  static public double log10(double x) throws ArithmeticException {
    if(x<=0.0) {
      throw new ArithmeticException("range exception"); //$NON-NLS-1$
    }
    return Math.log(x)/2.30258509299404568401;
  }

}

/**
 * A structure class used exclusively with the TextLine class.
 * When the Text changes state (new font, new color, new offset)
 * then this class holds the information plus the substring
 * that the state pertains to.
 */
class TextState extends Object {
  Font f = null;
  StringBuffer s = null;
  int x = 0;
  int y = 0;

  /**
   * Constructor TextState
   */
  public TextState() {
    s = new StringBuffer();
  }

  public TextState copyAll() {
    TextState tmp = copyState();
    if(s.length()==0) {
      return tmp;
    }
    for(int i = 0; i<s.length(); i++) {
      tmp.s.append(s.charAt(i));
    }
    return tmp;
  }

  public TextState copyState() {
    TextState tmp = new TextState();
    tmp.f = f;
    tmp.x = x;
    tmp.y = y;
    return tmp;
  }

  public String toString() {
    return s.toString();
  }

  public boolean isEmpty() {
    return(s.length()==0);
  }

  public int getWidth(Graphics g) {
    if((g==null)||(f==null)||(s.length()==0)) {
      return 0;
    }
    return g.getFontMetrics(f).stringWidth(s.toString());
  }

  public int getHeight(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getHeight();
  }

  public int getAscent(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getAscent();
  }

  public int getDescent(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getDescent();
  }

  public int getMaxAscent(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getMaxAscent();
  }

  public int getMaxDescent(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getMaxDescent();
  }

  public int getLeading(Graphics g) {
    if((g==null)||(f==null)) {
      return 0;
    }
    return g.getFontMetrics(f).getLeading();
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
