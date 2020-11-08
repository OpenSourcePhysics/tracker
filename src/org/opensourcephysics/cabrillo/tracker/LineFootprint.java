/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.FontSizer;

/**
 * A LineFootprint returns a line shape for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class LineFootprint implements Footprint, Cloneable {

  private static Shape arrowhead;
  private static Arc2D arc = new Arc2D.Double(Arc2D.OPEN);
  protected static Line2D hitLine = new Line2D.Double();
  
	// instance fields
	protected String name;
	protected MultiShape highlight;
	protected AffineTransform transform = new AffineTransform();
	protected BasicStroke baseStroke = new BasicStroke();
	protected BasicStroke stroke;
	protected Color color = Color.black;
	protected GeneralPath path = new GeneralPath();
	protected Line2D line = new Line2D.Double();
	protected Shape[] hitShapes = new Shape[5];
	protected BasicStroke rotatorStroke;
	
	static {
  	
  	GeneralPath path = new GeneralPath();
  	path.moveTo(-6, 3);
  	path.lineTo(0, 0);
  	path.lineTo(-6, -3);
  	arrowhead = path;
	}

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
		return getFootprint(footprints, name);
	}
	
	protected static Footprint getFootprint(Collection<LineFootprint> footprints, String name) {
		Iterator<LineFootprint> it = footprints.iterator();
		while (it.hasNext()) {
			LineFootprint footprint = it.next();
			if (name == footprint.getName())
				try {
					return (LineFootprint) footprint.clone();
				} catch (CloneNotSupportedException ex) {
				}
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
		return 2;
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
		Point[] points = new Point[] { new Point(), new Point(w - 2, 2 - h) };
		MultiShape shape = getShape(points, 1);
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
		MultiShape shape = getShape(points, FontSizer.getIntegerFactor());
		MultiShape hilite = getHighlightShape();
		return new Mark() {

			@Override
			public void draw(Graphics2D g, boolean highlighted) {
				Color gcolor = g.getColor();
				Stroke gstroke = g.getStroke();
				g.setColor(color);
				g.setStroke(stroke);
				if (OSPRuntime.setRenderingHints)
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				shape.draw(g);
				if (highlighted && hilite != null) {
					hilite.draw(g);
				}
				g.setColor(gcolor);
				g.setStroke(gstroke);
			}


		};
	}

	/**
	 * Gets the hit shapes. Shape[0] is for p0, Shape[1] for p1 and Shape[2] for the
	 * line
	 *
	 * @return the hit shapes
	 */
	@Override
	public Shape[] getHitShapes() {
		return hitShapes;
	}

	/**
	 * Sets the stroke.
	 *
	 * @param stroke the desired stroke
	 */
	@Override
	public void setStroke(BasicStroke stroke) {
		if (stroke == null)
			return;
		this.baseStroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8,
				stroke.getDashArray(), stroke.getDashPhase());
	}

	/**
	 * Gets the stroke.
	 *
	 * @return the stroke
	 */
	@Override
	public BasicStroke getStroke() {
		return baseStroke;
	}

	/**
	 * Sets the dash array.
	 *
	 * @param dashArray the desired dash array
	 */
	public void setDashArray(float[] dashArray) {
		baseStroke = new BasicStroke(baseStroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, dashArray,
				baseStroke.getDashPhase());
	}

	/**
	 * Sets the line width.
	 *
	 * @param w the desired line width
	 */
	public void setLineWidth(double w) {
		baseStroke = new BasicStroke((float) w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8,
				baseStroke.getDashArray(), baseStroke.getDashPhase());
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
	 * Gets the shape of this footprint.
	 *
	 * @param points an array of Points
	 * @return the shape
	 */
	@Override
	public MultiShape getShape(Point[] points, int scale) {
		checkStrokes();
		float lineWidth = stroke.getLineWidth();
		Point p1 = points[0];
		Point p2 = points[1];
		// set up transform
		double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
		transform.setToRotation(theta, p2.x, p2.y);
		transform.translate(p2.x, p2.y);
		
		double d = p1.distance(p2); // length of the line
		//set up hit shapes
		hitShapes[0] = new Rectangle(p1.x - 1, p1.y - 1, 2, 2); // for p1
		hitShapes[1] = new Rectangle(p2.x - 1, p2.y - 1, 2, 2); // for p2
		line.setLine(p1, p2);

		double center = d / 2; // center point
		hitLine.setLine(center - 0.3 * d, 0, center + 0.3 * d, 0);
		hitShapes[2] = transform.createTransformedShape(hitLine); // for line		
		hitLine.setLine(center + 0.35 * d, 0, center + 0.45 * d, 0);
		hitShapes[3] = transform.createTransformedShape(hitLine); // for rotator0
		hitLine.setLine(center - 0.45 * d, 0, center - 0.35 * d, 0);
		hitShapes[4] = transform.createTransformedShape(hitLine); // for rotator1
		
		// set up draw shape
		synchronized (path) {
			path.reset();
			path.moveTo(0, 0.5*lineWidth);
			path.lineTo(d, 0.5*lineWidth);
			path.lineTo(d, -0.5*lineWidth);
			path.lineTo(0, -0.5*lineWidth);
			path.closePath();
		}
		return new MultiShape(transform.createTransformedShape(path)).andFill(true);
	}
	
	/**
	 * Gets the highlight shape of this footprint. This should be called immediately
	 * following getShape() as the highlight is created there.
	 *
	 * @return the shape
	 */
	public MultiShape getHighlightShape() {
		if (highlight != null) {
			return new MultiShape(highlight);
		}
		return null;
	}

		
	/**
   * Gets a rotator shape.
   *
   * @param center the screen point of line center
   * @param anchor the screen point of the anchor on the shaft
   * @param rotator the screen point of the rotator 
   * 
   * @return the rotator shape
   */
  public MultiShape getRotatorShape(Point center, Point anchor, Point rotator) {
  	// if rotator is null, draw arc at anchor
  	if (rotator == null) {
			int scale = FontSizer.getIntegerFactor();
	  	double r = 15 * scale;
	  	double ang = 50; // degrees to either side
	  	double arrowAngleOffset = 10;
	  	double d = center.distance(anchor);
	  	double sin = -(anchor.y - center.y) / d;
	  	double cos = (anchor.x - center.x) / d;
	  	double theta = 180 * Math.atan2(sin, cos) / Math.PI;
	  	arc.setArcByCenter(anchor.x - r * cos, anchor.y + r * sin, r, theta - ang, 2 * ang, Arc2D.OPEN);
	  	MultiShape toDraw = new MultiShape(arc).andStroke(stroke);
	  	// add arrow at arc ends
	    Point2D pt = arc.getEndPoint();
	    double rotationAngle = Math.PI * (theta + ang - arrowAngleOffset) / 180 + Math.PI/2;
	    transform.setToRotation(-rotationAngle, pt.getX(), pt.getY());
	    transform.translate(pt.getX(), pt.getY());
	    if (scale>1) {
	    	transform.scale(scale, scale);
	    }
	    toDraw.addDrawShape(transform.createTransformedShape(arrowhead), stroke);
	    pt = arc.getStartPoint();
	    rotationAngle = Math.PI * (theta - ang + arrowAngleOffset) / 180 - Math.PI/2;
	    transform.setToRotation(-rotationAngle, pt.getX(), pt.getY());
	    transform.translate(pt.getX(), pt.getY());
	    if (scale>1) {
	    	transform.scale(scale, scale);
	    }
	    toDraw.addDrawShape(transform.createTransformedShape(arrowhead), stroke);
	  	return toDraw;
  	}
  	// if rotator is non-null, draw dotted line from end to rotator if outside the line
  	if (rotator.distanceSq(center) > anchor.distanceSq(center)) {
		  Line2D line = new Line2D.Double(anchor.x, anchor.y, rotator.x, rotator.y);
		  return new MultiShape(line).andStroke(rotatorStroke);
  	}
  	return null;
  }

  protected void checkStrokes() {
		int scale = FontSizer.getIntegerFactor();
		if (stroke == null || stroke.getLineWidth() != scale * baseStroke.getLineWidth()) {
			stroke = new BasicStroke(scale * baseStroke.getLineWidth());
			rotatorStroke = new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          WIDE_DOTTED_LINE,
          stroke.getDashPhase());  
		}
  }

	// static fields
	private static Collection<LineFootprint> footprints = new HashSet<LineFootprint>();

	// static constants
	public static final float[] DASHED_LINE = new float[] { 10, 4 };
	public static final float[] DOTTED_LINE = new float[] { 2, 1 };
	public static final float[] WIDE_DOTTED_LINE = new float[] {2, 6};

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
