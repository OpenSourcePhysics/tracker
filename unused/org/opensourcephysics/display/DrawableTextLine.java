/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * DrawableTextLine draws short Strings with subscripts and superscripts.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DrawableTextLine extends TextLine implements Drawable {
  double x, y;
  double theta = 0;
  protected boolean pixelXY = false; // x and y are given in pixels

  /**
   * Constructs a DrawableTextLine with the given text and location.
   *
   * @param text String
   * @param x double
   * @param y double
   */
  public DrawableTextLine(String text, double x, double y) {
    super(text);
    this.x = x;
    this.y = y;
    color = Color.BLACK;
  }

  /**
   * Sets the pixelPosition flag.
   *
   * Pixels are used to position the object.
   *
   * @param enable boolean
   */
  public void setPixelXY(boolean enable) {
    this.pixelXY = enable;
  }

  /**
   * Sets the x coordinate.
   *
   * @param x double
   */
  public void setX(double x) {
    this.x = x;
  }

  /**
   * Sets the angle.
   *
   * @param theta double
   */
  public void setTheta(double theta) {
    this.theta = theta;
  }

  /**
   * Gets the x coordinate.
   *
   * @return double
   */
  public double getX() {
    return x;
  }

  /**
   * Sets the y coordinate.
   *
   * @param y double
   */
  public void setY(double y) {
    this.y = y;
  }

  /**
   * Gets the y coordinate.
   *
   * @return double
   */
  public double getY() {
    return y;
  }

  /**
 * Draws the TextLine.
 *
 * @param panel DrawingPanel
 * @param g Graphics
 */
  public void draw(DrawingPanel panel, Graphics g) {
    if((text==null)||text.equals("")) { //$NON-NLS-1$
      return;
    }
    Font oldFont = g.getFont();
    if(this.pixelXY) {
      drawWithPix(panel, g);
    } else {
      drawWithWorld(panel, g);
    }
    g.setFont(oldFont);
  }

/**
* Draws the TextLine using world units for x and y.
*
* @param panel DrawingPanel
* @param g Graphics
*/
  
  void drawWithPix(DrawingPanel panel, Graphics g) {
	  if(OSPRuntime.isMac()){
		  drawWithPixMac( panel,  g);
	  }else{
		  drawWithPixWindows( panel,  g);	 
	  }
  }
  
  private void drawWithPixWindows(DrawingPanel panel, Graphics g) {
    if(theta!=0) {
      ((Graphics2D) g).transform(AffineTransform.getRotateInstance(-theta, x, y));
      drawText(g, (int) x, (int) y);
      ((Graphics2D) g).transform(AffineTransform.getRotateInstance(theta, x, y));
    } else {
      drawText(g, (int) x, (int) y);
    }
  }
  
  private void drawWithPixMac(DrawingPanel panel, Graphics g) {
	if(theta==0){
		drawWithPixWindows( panel,  g);
		return;
	}
    int w=g.getFontMetrics().stringWidth(text)+7;
    int h=g.getFontMetrics().getHeight()+10;
    BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    Graphics2D imageGraphics=image.createGraphics();
    imageGraphics.setFont(g.getFont());
    //imageGraphics.setColor(Color.RED);  // debug
    //imageGraphics.fillRect(0, 0, w, h);
    imageGraphics.setColor(Color.BLACK);
    drawText(imageGraphics, w/2-2, h-5);
    imageGraphics.dispose();
    Graphics2D g2d=(Graphics2D) g;
    g2d.translate(x-h-2,y+w/2);
    AffineTransform at= AffineTransform.getRotateInstance(-theta, 0, 0);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.drawImage(image,at, panel);
    g2d.translate(-x+h+2,-y-w/2);
   }
 
  /**
   * Draws the TextLine using world units for x and y.
   *
   * @param panel DrawingPanel
   * @param g Graphics
   */
  void drawWithWorld(DrawingPanel panel, Graphics g) {
	  if(OSPRuntime.isMac()){
		  drawWithWorldMac( panel,  g);
	  }else{  // windows
		  drawWithWorldWindows( panel,  g);	 
	  }
  }

  private void drawWithWorldMac(DrawingPanel panel, Graphics g) {
	if(theta==0){
		drawWithWorldWindows( panel,  g);
		return;
	}
    int w=g.getFontMetrics().stringWidth(text)+7;
    int h=g.getFontMetrics().getHeight()+10;
    BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    Graphics2D imageGraphics=image.createGraphics();
    imageGraphics.setColor(Color.BLACK);
    imageGraphics.setFont(g.getFont());
    drawText(imageGraphics, w/2-2, h-5);
    imageGraphics.dispose();
    Graphics2D g2d=(Graphics2D) g;
    Point2D pt = new Point2D.Double(x, y);
    pt = panel.getPixelTransform().transform(pt, pt);
    g2d.translate(pt.getX()-h-2,pt.getY()+w/2);
    AffineTransform at= AffineTransform.getRotateInstance(-theta, 0, 0);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.drawImage(image,at, panel);
    g2d.translate(-pt.getX()+h+2,-pt.getY()-w/2);
  }
  
  /**
   * Draws the TextLine using world units for x and y.
   *
   * @param panel DrawingPanel
   * @param g Graphics
   */
  private void drawWithWorldWindows(DrawingPanel panel, Graphics g) {
    Point2D pt = new Point2D.Double(x, y);
    pt = panel.getPixelTransform().transform(pt, pt);
    if(theta!=0) {
      ((Graphics2D) g).transform(AffineTransform.getRotateInstance(-theta, pt.getX(), pt.getY()));
      drawText(g, (int) pt.getX(), (int) pt.getY());
      ((Graphics2D) g).transform(AffineTransform.getRotateInstance(theta, pt.getX(), pt.getY()));
    } else {
      drawText(g, (int) pt.getX(), (int) pt.getY());
    }
  }

  /**
 * Gets the XML object loader for this class.
 * @return ObjectLoader
 */
  public static XML.ObjectLoader getLoader() {
    return new DrawableTextLineLoader();
  }

  /**
   * A class to save and load InteractiveArrow in an XMLControl.
   */
  protected static class DrawableTextLineLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      DrawableTextLine drawableTextLine = (DrawableTextLine) obj;
      control.setValue("text", drawableTextLine.getText());         //$NON-NLS-1$
      control.setValue("x", drawableTextLine.x);                    //$NON-NLS-1$
      control.setValue("y", drawableTextLine.y);                    //$NON-NLS-1$
      control.setValue("theta", drawableTextLine.theta);            //$NON-NLS-1$
      control.setValue("color", drawableTextLine.color);            //$NON-NLS-1$
      control.setValue("pixel position", drawableTextLine.pixelXY); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new DrawableTextLine("", 0, 0); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      DrawableTextLine drawableTextLine = (DrawableTextLine) obj;
      drawableTextLine.x = control.getDouble("x");                     //$NON-NLS-1$
      drawableTextLine.y = control.getDouble("y");                     //$NON-NLS-1$
      drawableTextLine.theta = control.getDouble("theta");             //$NON-NLS-1$
      drawableTextLine.pixelXY = control.getBoolean("pixel position"); //$NON-NLS-1$
      drawableTextLine.setText(control.getString("text")); //$NON-NLS-1$
      drawableTextLine.color = (Color) control.getObject("color"); //$NON-NLS-1$
      return obj;
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
