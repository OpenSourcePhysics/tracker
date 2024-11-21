/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import org.opensourcephysics.display.OSPRuntime;

/**
 * This Icon centers and fills the shape specified in its constructor.
 *
 * @author Douglas Brown
 */
public class ShapeIcon implements Icon {

  // instance fields
  private int w;
  private int h;
  private MultiShape shape;
  private MultiShape decoration;
  private Color color = Color.black;
  private Color decoColor = Color.black;
  private double offsetX;	// centers the shape horizontally
  private double offsetY;	// centers the shape vertically
  private BasicStroke stroke;

  /**
   * Constructs a ShapeIcon.
   *
   * @param shape the shape to draw
   * @param decoration a decorating shape to draw
   * @param width width of the icon
   * @param height height of the icon
   */
  public ShapeIcon(MultiShape shape, MultiShape decoration, int width, int height) {
    w = width;
    h = height;
    this.shape = shape;
    this.decoration = decoration;
    Rectangle rect = shape==null? new Rectangle(): shape.getBounds();
    if (decoration!=null)
    	rect = rect.union(decoration.getBounds());
    offsetX = w/2 - rect.width/2 - rect.x;
    offsetY = h/2 - rect.height/2 - rect.y;
  }

  /**
   * Constructs a ShapeIcon.
   *
   * @param shape the shape to draw
   * @param width width of the icon
   * @param height height of the icon
   */
  public ShapeIcon(MultiShape shape, int width, int height) {
  	this(shape, null, width, height);
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Sets the colors.
   *
   * @param color the desired color
   * @param decorationColor the desired decoration color
   */
  public void setColor(Color color, Color decorationColor) {
    this.color = color;
    decoColor = decorationColor;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke
   */
  public void setStroke(BasicStroke stroke) {
    this.stroke = stroke;
  }

  /**
   * Gets the icon width.
   *
   * @return the icon width
   */
  @Override
public int getIconWidth() {
    return w;
  }

  /**
   * Gets the icon height.
   *
   * @return the icon height
   */
  @Override
public int getIconHeight() {
    return h;
  }

  /**
   * Paints the icon.
   *
   * @param c the component on which it is painted
   * @param _g the graphics context
   * @param x the x coordinate of the icon
   * @param y the y coordinate of the icon
   */
  @Override
public void paintIcon(Component c, Graphics _g, int x, int y) {
  	if (shape == null && decoration == null)
  		return;
    Graphics2D g = (Graphics2D)_g.create();
    AffineTransform at = AffineTransform.getTranslateInstance(
                         x + offsetX, y + offsetY);

    // render shape(s)
    g.setPaint(color);
    if (OSPRuntime.setRenderingHints) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.clipRect(x, y, w, h);

    // paint shape, if any
    if (shape!=null) {
    	if (stroke != null ) {
  			g.setStroke(stroke);
  		}
			shape.transform(at).draw(g);
    }
    
    // paint decoration, if any
    if (decoration != null) {
      g.setPaint(decoColor);
      decoration.transform(at).draw(g);
    }
    // restore graphics paint and clip
    g.dispose();
  }
}
