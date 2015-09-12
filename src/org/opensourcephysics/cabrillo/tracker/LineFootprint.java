/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.

 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

/**
 * A LineFootprint returns a line shape for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class LineFootprint implements Footprint, Cloneable {

  // instance fields
  protected String name;
  protected Shape highlight;
  protected AffineTransform transform = new AffineTransform();
  protected BasicStroke stroke = new BasicStroke();
  protected Color color = Color.black;
  protected GeneralPath path = new GeneralPath();
  protected Line2D line = new Line2D.Double();
  protected Shape[] hitShapes = new Shape[3];

  /**
   * Constructs a LineFootprint.
   *
   * @param name the name
   */
  public LineFootprint(String name) {
    this.name = name;
  }

  /**
   * Gets a predefined LineFootprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static Footprint getFootprint(String name) {
    Iterator<LineFootprint> it = footprints.iterator();
    while(it.hasNext()) {
      LineFootprint footprint = it.next();
      if (name == footprint.getName()) try {
        return (LineFootprint)footprint.clone();
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
    return 2;
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {
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
    final Shape highlight = this.highlight;
    final Color color = this.color;
    return new Mark() {
      public void draw(Graphics2D g, boolean highlighted) {
        Color gcolor = g.getColor();
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(shape);
        if (highlighted && highlight!=null) {
        	g.fill(highlight);
        }
        g.setColor(gcolor);
      }

      public Rectangle getBounds(boolean highlighted) {
        return shape.getBounds();
      }
    };
  }

  /**
   * Gets the hit shapes. Shape[0] is for p0, Shape[1] for p1
   * and Shape[2] for the line
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
    this.stroke = new BasicStroke(stroke.getLineWidth(),
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
    return stroke;
  }

  /**
   * Sets the dash array.
   *
   * @param dashArray the desired dash array
   */
  public void setDashArray(float[] dashArray) {
    setStroke(new BasicStroke(stroke.getLineWidth(),
                              BasicStroke.CAP_BUTT,
                              BasicStroke.JOIN_MITER,
                              8,
                              dashArray,
                              stroke.getDashPhase()));
  }

  /**
   * Sets the line width.
   *
   * @param w the desired line width
   */
  public void setLineWidth(double w) {
    stroke = new BasicStroke((float)w,
                              BasicStroke.CAP_BUTT,
                              BasicStroke.JOIN_MITER,
                              8,
                              stroke.getDashArray(),
                              stroke.getDashPhase());
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
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    Point p1 = points[0];
    Point p2 = points[1];
    line.setLine(p1, p2);
    hitShapes[0] = new Rectangle(p1.x-1, p1.y-1, 2, 2); // for p1
    hitShapes[1] = new Rectangle(p2.x-1, p2.y-1, 2, 2); // for p2
    hitShapes[2] = (Line2D.Double)line.clone();         // for line
    return stroke.createStrokedShape(line);
  }

  // static fields
  private static Collection<LineFootprint> footprints = new HashSet<LineFootprint>();

  // static constants  
  /** A dashed line pattern */
  public static final float[] DASHED_LINE = new float[] {10, 4};
  /** A dotted line pattern */
  public static final float[] DOTTED_LINE = new float[] {2, 1};
  protected static final Shape HIGHLIGHT;
  private static final LineFootprint LINE;
  private static final LineFootprint BOLD_LINE;
  private static final LineFootprint OUTLINE;
  private static final LineFootprint BOLD_OUTLINE;
  private static final LineFootprint DOUBLE_ARROW;
  private static final LineFootprint BOLD_DOUBLE_ARROW;
  private static final ArrowFootprint ARROW;
  private static final ArrowFootprint BOLD_ARROW;
  private static final ArrowFootprint BIG_ARROW;
  private static final ArrowFootprint DASH_ARROW;
  private static final ArrowFootprint BOLD_DASH_ARROW;
  private static final ArrowFootprint BIG_DASH_ARROW;
  private static final DoubleCrosshairFootprint DOUBLE_TARGET;
  private static final DoubleCrosshairFootprint BOLD_DOUBLE_TARGET;

  // static initializers
  static {
    // HIGHLIGHT
    Ellipse2D circle = new Ellipse2D.Double();
    circle.setFrame(-3, -3, 6, 6);
    Stroke stroke = new BasicStroke(2);
    HIGHLIGHT = stroke.createStrokedShape(circle);

    // LINE
    LINE = new LineFootprint("Footprint.Line"); //$NON-NLS-1$
    footprints.add(LINE);

    // BOLD_LINE
    BOLD_LINE = new LineFootprint("Footprint.BoldLine"); //$NON-NLS-1$
    BOLD_LINE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_LINE);

    // OUTLINE
    OUTLINE = new OutlineFootprint("Footprint.Outline"); //$NON-NLS-1$
    footprints.add(OUTLINE);

    // BOLD_OUTLINE
    BOLD_OUTLINE = new OutlineFootprint("Footprint.BoldOutline"); //$NON-NLS-1$
    BOLD_OUTLINE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_OUTLINE);

    // DOUBLE_ARROW
    DOUBLE_ARROW = new DoubleArrowFootprint("Footprint.DoubleArrow"); //$NON-NLS-1$
    footprints.add(DOUBLE_ARROW);

    // BOLD_DOUBLE_ARROW
    BOLD_DOUBLE_ARROW = new DoubleArrowFootprint("Footprint.BoldDoubleArrow"); //$NON-NLS-1$
    BOLD_DOUBLE_ARROW.setStroke(new BasicStroke(2));
    footprints.add(BOLD_DOUBLE_ARROW);

    // ARROW
    ARROW = new ArrowFootprint("Footprint.Arrow"); //$NON-NLS-1$
    footprints.add(ARROW);

    // BOLD_ARROW
    BOLD_ARROW = new ArrowFootprint("Footprint.BoldArrow"); //$NON-NLS-1$
    BOLD_ARROW.setStroke(new BasicStroke(2));
    footprints.add(BOLD_ARROW);

    // BIG_ARROW
    BIG_ARROW = new ArrowFootprint("Footprint.BigArrow"); //$NON-NLS-1$
    BIG_ARROW.setStroke(new BasicStroke(4));
    BIG_ARROW.setTipLength(32);
    footprints.add(BIG_ARROW);

    // DASH_ARROW
    DASH_ARROW = new ArrowFootprint("Footprint.DashArrow"); //$NON-NLS-1$
    DASH_ARROW.setDashArray(DASHED_LINE);
    footprints.add(DASH_ARROW);

    // BOLD_DASH_ARROW
    BOLD_DASH_ARROW = new ArrowFootprint("Footprint.BoldDashArrow"); //$NON-NLS-1$
    BOLD_DASH_ARROW.setStroke(new BasicStroke(2));
    BOLD_DASH_ARROW.setDashArray(DASHED_LINE);
    footprints.add(BOLD_DASH_ARROW);

    // BIG_DASH_ARROW
    BIG_DASH_ARROW = new ArrowFootprint("Footprint.BigDashArrow"); //$NON-NLS-1$
    BIG_DASH_ARROW.setStroke(new BasicStroke(4));
    BIG_DASH_ARROW.setDashArray(DASHED_LINE);
    BIG_DASH_ARROW.setTipLength(32);
    footprints.add(BIG_DASH_ARROW);

    // DOUBLE_TARGET
    DOUBLE_TARGET = new DoubleCrosshairFootprint("Footprint.DoubleTarget"); //$NON-NLS-1$
    footprints.add(DOUBLE_TARGET);

    // BOLD_DOUBLE_TARGET
    BOLD_DOUBLE_TARGET = new DoubleCrosshairFootprint("Footprint.BoldDoubleTarget"); //$NON-NLS-1$
    BOLD_DOUBLE_TARGET.setStroke(new BasicStroke(2));
    footprints.add(BOLD_DOUBLE_TARGET);

  }
}

