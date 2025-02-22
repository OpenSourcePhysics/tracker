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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.FontSizer;

/**
 * A PointShapeFootprint returns a shape for a Point[] of length 1.
 */
public class PointShapeFootprint implements Footprint, Cloneable {

  // instance fields
  protected String name;
  protected Shape shape;
  protected Shape highlight;
  protected AffineTransform transform = new AffineTransform();
  protected BasicStroke baseStroke = new BasicStroke();
  protected BasicStroke stroke;
  protected BasicStroke highlightStroke = new BasicStroke(2);
  protected Color color = Color.black;
  protected Shape[] hitShapes = new Shape[1];

  /**
   * Constructs a PointShapeFootprint with a point shape.
   *
   * @param name the name
   * @param shape point shape of the footprint
   */
  public PointShapeFootprint(String name, Shape shape) {
    this.name = name;
    this.shape = shape;
  }

  /**
   * Gets a named footprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static PointShapeFootprint getFootprint(String name) {
    Iterator<PointShapeFootprint> it = footprints.iterator();
    while(it.hasNext()) {
      PointShapeFootprint footprint = it.next();
      if (name == footprint.getName()) try {
        return (PointShapeFootprint)footprint.clone();
      } catch(CloneNotSupportedException ex) {ex.printStackTrace();}
    }
    return null;
  }

  /**
   * Gets the name of this footprint.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the display name of the footprint.
   *
   * @return the localized display name
   */
  @Override
  public String getDisplayName() {
  	return TrackerRes.getString(name);
  }

  /**
   * Gets the minimum point array length required by this footprint.
   *
   * @return the length
   */
  @Override
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
  @Override
  public ResizableIcon getIcon(int w, int h) {
	  MultiShape shape = getShape(new Point[] {new Point()}, 1);
	  ShapeIcon icon = new ShapeIcon(shape, w, h);
	  icon.setColor(color);
	  icon.setStroke(stroke);
	  return new ResizableIcon(icon);
  }

  /**
   * Gets the footprint mark.
   *
   * @param points a Point array
   * @return the mark
   */
  @Override
  public Mark getMark(Point[] points) {
    final MultiShape shape = getShape(points, FontSizer.getIntegerFactor());
    final Shape highlight = this.highlight;
    return new Mark() {
      @Override
      public void draw(Graphics2D g, boolean highlighted) {
        Paint gpaint = g.getPaint();
        Stroke gstroke = g.getStroke();
        if (stroke != null)
        	g.setStroke(stroke);
        g.setPaint(color);
        if (OSPRuntime.setRenderingHints) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        shape.draw(g);
        if (highlighted) {
        	g.setStroke(highlightStroke);
        	g.draw(highlight);
        }
        g.setPaint(gpaint);
        g.setStroke(gstroke);
      }
    };
  }

  /**
   * Gets the hit shapes.
   *
   * @return the hit shapes
   */
  @Override
  public Shape[] getHitShapes() {
    return hitShapes;
  }

  /**
   * Sets the shape.
   *
   * @param shape the desired shape
   */
  protected void setShape(Shape shape) {
  	if (shape != null)
  		this.shape = shape;
  }

  /**
   * Sets the stroke. May be set to null.
   *
   * @param stroke the desired stroke
   */
  @Override
  public void setStroke(BasicStroke stroke) {
    baseStroke = stroke;
  }

  /**
   * Gets the stroke. May return null;
   *
   * @return the stroke
   */
  @Override
  public BasicStroke getStroke() {
    return baseStroke;
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  @Override
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  @Override
  public Color getColor() {
    return color;
  }

  /**
   * Gets the fill shape for a specified point.
   *
   * @param points an array of points
   * @return the fill shape
   */
  @Override
  public MultiShape getShape(Point[] points, int scale) {
    Point p = points[0];
    transform.setToTranslation(p.x, p.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    Shape transformedShape = transform.createTransformedShape(shape);
    highlight = transform.createTransformedShape(HIGHLIGHT);
    if (baseStroke != null) {
    	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
    		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
    	}
    }
    hitShapes[0] = transformedShape;
    return stroke != null ? new MultiShape(transformedShape).andStroke(stroke)
    		: new MultiShape(transformedShape).andFill(true);
  }

  // static fields
  protected static Collection<PointShapeFootprint> footprints 
  		= new HashSet<PointShapeFootprint>();

  // static constants
  private static final Ellipse2D HIGHLIGHT;
  private static final PointShapeFootprint DIAMOND;
  private static final PointShapeFootprint BOLD_DIAMOND;
  private static final PointShapeFootprint SOLID_DIAMOND;
  private static final PointShapeFootprint TRIANGLE;
  private static final PointShapeFootprint BOLD_TRIANGLE;
  private static final PointShapeFootprint SOLID_TRIANGLE;
  private static final PointShapeFootprint CIRCLE;
  private static final PointShapeFootprint BOLD_CIRCLE;
  private static final PointShapeFootprint SOLID_CIRCLE;
  private static final PointShapeFootprint VERT_LINE;
  private static final PointShapeFootprint BOLD_VERT_LINE;
  private static final PointShapeFootprint HORZ_LINE;
  private static final PointShapeFootprint BOLD_HORZ_LINE;
  private static final PointShapeFootprint CROSSHAIR;
  private static final PointShapeFootprint BOLD_CROSSHAIR;
  private static final PointShapeFootprint SIMPLE_AXES;
  private static final PointShapeFootprint BOLD_SIMPLE_AXES;
  private static final PointShapeFootprint SMALL_SPOT;
  private static final PointShapeFootprint SMALL_CIRCLE;
  private static final PointShapeFootprint SOLID_SQUARE;
  private static final PointShapeFootprint VECTOR;
  private static final PointShapeFootprint BOLD_VECTOR;
  private static final PointShapeFootprint SHAPE;
  private static final PointShapeFootprint BOLD_SHAPE;

  // static initializers
  static {
    float w = 3000; // pixel length of axes and line shapes

    // HIGHLIGHT
    HIGHLIGHT = new Ellipse2D.Double();
    HIGHLIGHT.setFrame(-6, -6, 12, 12);

    // DIAMOND
    GeneralPath diamond = new GeneralPath();
    diamond.moveTo(-5, 0);
    diamond.lineTo(0, 5);
    diamond.lineTo(5.01f, 0);
    diamond.lineTo(0, -5);
    diamond.closePath();
    DIAMOND = new PointShapeFootprint("Footprint.Diamond", diamond); //$NON-NLS-1$
    footprints.add(DIAMOND);
    BOLD_DIAMOND = new PointShapeFootprint("Footprint.BoldDiamond", diamond); //$NON-NLS-1$
    BOLD_DIAMOND.setStroke(new BasicStroke(2));
    footprints.add(BOLD_DIAMOND);
    SOLID_DIAMOND = new PointShapeFootprint("Footprint.SolidDiamond", diamond); //$NON-NLS-1$
    SOLID_DIAMOND.setStroke(null);
    footprints.add(SOLID_DIAMOND);

    // TRIANGLE
    GeneralPath triangle = new GeneralPath();
    triangle.moveTo(0, -5);
    triangle.lineTo(4, 3);
    triangle.lineTo(-4, 3);
    triangle.closePath();
    TRIANGLE = new PointShapeFootprint("Footprint.Triangle", triangle); //$NON-NLS-1$
    footprints.add(TRIANGLE);
    BOLD_TRIANGLE = new PointShapeFootprint("Footprint.BoldTriangle", triangle); //$NON-NLS-1$
    BOLD_TRIANGLE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_TRIANGLE);
    SOLID_TRIANGLE = new PointShapeFootprint("Footprint.SolidTriangle", triangle); //$NON-NLS-1$
    SOLID_TRIANGLE.setStroke(null);
    footprints.add(SOLID_TRIANGLE);

    // CIRCLE
    Ellipse2D circle = new Ellipse2D.Double();
    circle.setFrame(-5, -5, 10, 10);
    CIRCLE = new PointShapeFootprint("Footprint.Circle", circle); //$NON-NLS-1$
    footprints.add(CIRCLE);
    BOLD_CIRCLE = new PointShapeFootprint("Footprint.BoldCircle", circle); //$NON-NLS-1$
    BOLD_CIRCLE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_CIRCLE);
    SOLID_CIRCLE = new PointShapeFootprint("Footprint.SolidCircle", circle); //$NON-NLS-1$
    SOLID_CIRCLE.setStroke(null);
    footprints.add(SOLID_CIRCLE);
    circle = new Ellipse2D.Double();
    circle.setFrame(-3, -3, 6, 6);
    SMALL_CIRCLE = new PointShapeFootprint("Footprint.SmallCircle", circle); //$NON-NLS-1$
    footprints.add(SMALL_CIRCLE);

    // SMALL_SPOT
    Ellipse2D smallSpot = new Ellipse2D.Double(-2, -2, 4, 4);
    SMALL_SPOT = new PointShapeFootprint("Footprint.Spot", smallSpot); //$NON-NLS-1$
    SMALL_SPOT.setStroke(null);
    footprints.add(SMALL_SPOT);

    // VERT_LINE
    GeneralPath vertLine = new GeneralPath();
    vertLine.moveTo(0, -w);
    vertLine.lineTo(0, w);
    vertLine.moveTo(-3, 0);
    vertLine.lineTo(3, 0);
    VERT_LINE = new PointShapeFootprint("Footprint.VerticalLine", vertLine); //$NON-NLS-1$
    footprints.add(VERT_LINE);
    BOLD_VERT_LINE = new PointShapeFootprint("Footprint.BoldVerticalLine", vertLine); //$NON-NLS-1$
    BOLD_VERT_LINE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_VERT_LINE);

    // HORZ_LINE
    GeneralPath horzLine = new GeneralPath();
    horzLine.moveTo(0, -3);
    horzLine.lineTo(0, 3);
    horzLine.moveTo(-w, 0);
    horzLine.lineTo(w, 0);
    HORZ_LINE = new PointShapeFootprint("Footprint.HorizontalLine", horzLine); //$NON-NLS-1$
    footprints.add(HORZ_LINE);
    BOLD_HORZ_LINE = new PointShapeFootprint("Footprint.BoldHorizontalLine", horzLine); //$NON-NLS-1$
    BOLD_HORZ_LINE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_HORZ_LINE);

    // CROSSHAIR
    GeneralPath crosshair = new GeneralPath();
    crosshair.moveTo(0, -4);
    crosshair.lineTo(0, 4);
    crosshair.moveTo( -4, 0);
    crosshair.lineTo(4, 0);
    CROSSHAIR = new PointShapeFootprint("Footprint.Crosshair", crosshair); //$NON-NLS-1$
    footprints.add(CROSSHAIR);
    BOLD_CROSSHAIR = new PointShapeFootprint("Footprint.BoldCrosshair", crosshair); //$NON-NLS-1$
    BOLD_CROSSHAIR.setStroke(new BasicStroke(2));
    footprints.add(BOLD_CROSSHAIR);

    // SIMPLE_AXES
    GeneralPath axes = new GeneralPath();
    axes.reset();
    axes.moveTo(w, 0);
    axes.lineTo(-w, 0);
    axes.moveTo(0, w);
    axes.lineTo(0, -w);
    axes.moveTo(15, 5); // x axis crosshair
    axes.lineTo(15, -5);
    SIMPLE_AXES = new PointShapeFootprint("Footprint.SimpleAxes", axes); //$NON-NLS-1$
    footprints.add(SIMPLE_AXES);
    BOLD_SIMPLE_AXES = new PointShapeFootprint("Footprint.BoldSimpleAxes", axes); //$NON-NLS-1$
    BOLD_SIMPLE_AXES.setStroke(new BasicStroke(2));
    footprints.add(BOLD_SIMPLE_AXES);
    
    // SQUARE
    Rectangle2D square = new Rectangle(-3, -3, 6, 6);
    SOLID_SQUARE = new PointShapeFootprint("Footprint.SolidSquare", square); //$NON-NLS-1$
    SOLID_SQUARE.setStroke(null);
    footprints.add(SOLID_SQUARE);
    
    // VECTOR
    VECTOR = new PositionVectorFootprint("Footprint.PositionVector", 1); //$NON-NLS-1$
    footprints.add(VECTOR);
    BOLD_VECTOR = new PositionVectorFootprint("Footprint.BoldPositionVector", 2); //$NON-NLS-1$
    footprints.add(BOLD_VECTOR);

    // SHAPE
    circle = new Ellipse2D.Double();
    circle.setFrame(-5, -5, 10, 10);
    SHAPE = new PointShapeFootprint("Footprint.Shape", circle); //$NON-NLS-1$
    footprints.add(SHAPE);
    BOLD_SHAPE = new PointShapeFootprint("Footprint.BoldShape", circle); //$NON-NLS-1$
    BOLD_SHAPE.setStroke(new BasicStroke(2));
    footprints.add(BOLD_SHAPE);
  }
}

