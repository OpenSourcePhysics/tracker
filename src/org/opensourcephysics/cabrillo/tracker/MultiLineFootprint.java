/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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
 * <http://physlets.org/tracker/>.

 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

import org.opensourcephysics.tools.FontSizer;

/**
 * A MultiLineFootprint returns a set of line segments for a Point array of any length > 1.
 *
 * @author Douglas Brown
 */
public class MultiLineFootprint implements Footprint, Cloneable {

  // instance fields
  protected String name;
  protected AffineTransform transform = new AffineTransform();
  protected BasicStroke baseStroke = new BasicStroke();
  protected BasicStroke stroke;
  protected Color color = Color.black;
  protected GeneralPath path = new GeneralPath();
  protected Line2D line = new Line2D.Double();
  protected Shape[] hitShapes = new Shape[0];
  protected boolean closed = false;

  /**
   * Constructs a LineFootprint.
   *
   * @param name the name
   */
  public MultiLineFootprint(String name) {
    this.name = name;
  }

  /**
   * Gets a predefined MultiLineFootprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static MultiLineFootprint getFootprint(String name) {
    Iterator<MultiLineFootprint> it = footprints.iterator();
    while(it.hasNext()) {
    	MultiLineFootprint footprint = it.next();
      if (name == footprint.getName()) try {
        return (MultiLineFootprint)footprint.clone();
      } catch(CloneNotSupportedException ex) {ex.printStackTrace();}
    }
    return null;
  }

  /**
   * Gets the name of this footprint.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the display name of the footprint.
   *
   * @return the localized display name
   */
  public String getDisplayName() {
  	return TrackerRes.getString(name);
  }

  /**
   * Gets the minimum point array length required by this footprint.
   *
   * @return the length
   */
  public int getLength() {
    return 1;
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {
    int scale = FontSizer.getIntegerFactor();
    w *= scale;
    h *= scale;
    Point[] points = new Point[] {new Point(), new Point(w - 2, 2 - h)};
    Shape shape = getShape(points);
    ShapeIcon icon = new ShapeIcon(shape, w, h);
    icon.setColor(color);
    return icon;
  }

  /**
   * Gets the footprint mark.
   *
   * @param points a Point array
   * @return the mark
   */
  public Mark getMark(Point[] points) {
    final Shape shape = getShape(points);
    final Color color = this.color;
    return new Mark() {
      public void draw(Graphics2D g, boolean highlighted) {
        Color gcolor = g.getColor();
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(shape);
        g.setColor(gcolor);
      }

      public Rectangle getBounds(boolean highlighted) {
        return shape.getBounds();
      }
    };
  }

  /**
   * Gets the hit shapes. This return an empty array.
   *
   * @return the hit shapes
   */
  public Shape[] getHitShapes() {
    return hitShapes;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    this.baseStroke = new BasicStroke(stroke.getLineWidth(),
                                  BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_MITER,
                                  8,
                                  stroke.getDashArray(),
                                  stroke.getDashPhase());
  }

  /**
   * Gets the stroke.
   *
   * @return the stroke
   */
  public BasicStroke getStroke() {
    return baseStroke;
  }

  /**
   * Sets the dash array.
   *
   * @param dashArray the desired dash array
   */
  public void setDashArray(float[] dashArray) {
    setStroke(new BasicStroke(baseStroke.getLineWidth(),
                              BasicStroke.CAP_BUTT,
                              BasicStroke.JOIN_MITER,
                              8,
                              dashArray,
                              baseStroke.getDashPhase()));
  }

  /**
   * Sets the line width.
   *
   * @param w the desired line width
   */
  public void setLineWidth(double w) {
    baseStroke = new BasicStroke((float)w,
                              BasicStroke.CAP_BUTT,
                              BasicStroke.JOIN_MITER,
                              8,
                              baseStroke.getDashArray(),
                              baseStroke.getDashPhase());
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
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }
  
  /**
   * Determine if this draws closed paths.
   *
   * @return true if closed
   */
  public boolean isClosed() {
  	return closed;
  }
  
  /**
   * Sets the closed property.
   *
   * @param true to draw closed paths
   */
  public void setClosed(boolean closed) {
  	this.closed = closed;
  }

  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    int scale = FontSizer.getIntegerFactor();
  	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
  	}
  	Area area = new Area();
  	for (int i=0; i<points.length-1; i++) {
      Point p1 = points[i];
      Point p2 = points[i+1];
      if (p1==null || p2==null) continue;
      line.setLine(p1, p2);
      area.add(new Area(stroke.createStrokedShape(line)));
  	}
  	if (closed && points.length>2 && points[0]!=null && points[points.length-1]!=null) {
      line.setLine(points[points.length-1], points[0]);
      area.add(new Area(stroke.createStrokedShape(line)));
  	}
    return area;
  }

  // static fields
  private static Collection<MultiLineFootprint> footprints = new HashSet<MultiLineFootprint>();

  // static constants  
  private static final MultiLineFootprint LINE;
  private static final MultiLineFootprint BOLD_LINE;

  // static initializers
  static {

    // LINE
    LINE = new MultiLineFootprint("Footprint.Lines"); //$NON-NLS-1$
    footprints.add(LINE);

    // BOLD_LINE
    BOLD_LINE = new MultiLineFootprint("Footprint.BoldLines"); //$NON-NLS-1$
    BOLD_LINE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_LINE);

  }
}

