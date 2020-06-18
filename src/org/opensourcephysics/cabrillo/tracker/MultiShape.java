package org.opensourcephysics.cabrillo.tracker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * A simple class that allows for a set of shapes, each of which can be
 * designated to be filled or drawn. Avoids use of Area, which is not usable in
 * JavaScript, since that digs too deeply into the pixelization of the Java2D
 * rendering system. HTML5 does all the pixelation for us.
 * 
 * The constructor allows for any number of shapes.
 * 
 * Optionally follow that with .andFills(boolean...) to fill specific shapes.
 * For example, a filled arrow:
 * 
 * new MultiShape(shaft, head).addFill(false, true);
 * 
 * Or no filling at all:
 * 
 * new MultiShape(shaft, head)
 * 
 * multiShape.addFill can have as many arguments as needed to end with "true".
 * So that filled arrow could also be:
 * 
 * new MultiShape(head, shaft).addFill(true);
 * 
 * 
 * Then just use multiShape.draw(g), not g.fill(shape), as g.fill(shape) will
 * just fill the first shape.
 * 
 * 
 * Works in Java and JavaScript identically.
 * 
 * 
 * @author hansonr
 *
 */
class MultiShape implements Shape {

	private Shape[] shapes;
	private boolean[] fills;
	private Stroke[] strokes;
	
  /**
   * Constructor.
   *
   * @param shapes the shapes
   */
	public MultiShape(Shape... shapes) {
		this.shapes = shapes;
	}

  /**
   * Fills shapes rather than drawing them.
   *
   * @param fills true elements fill the corresponding shape in the constructor
   * @return this MultiShape
   */
	public MultiShape andFill(boolean... fills) {
		this.fills = fills;
		return this;
	}

  /**
   * Specifies Strokes to use when drawing.
   *
   * @param strokes the Strokes for the corresponding shapes in the constructor
   * @return this MultiShape
   */
	public MultiShape andStroke(Stroke... strokes) {
		this.strokes = strokes;
		return this;
	}

	@Override
	public Rectangle getBounds() {
		Rectangle r = shapes[0].getBounds();
		for (int i = shapes.length; --i >= 1;)
			r = r.union(shapes[i].getBounds());
		return r;
	}

	@Override
	public Rectangle2D getBounds2D() {
		Rectangle2D r = shapes[0].getBounds();
		for (int i = shapes.length; --i >= 1;)
			r = r.createUnion(shapes[i].getBounds());
		return r;
	}

	@Override
	public boolean contains(double x, double y) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].contains(x, y))
				return true;
		return false;
	}

	@Override
	public boolean contains(Point2D p) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].contains(p))
				return true;
		return false;
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].intersects(x, y, w, h))
				return true;
		return false;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].intersects(r))
				return true;
		return false;
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].contains(x, y, w, h))
				return true;
		return false;
	}

	@Override
	public boolean contains(Rectangle2D r) {
		for (int i = shapes.length; --i >= 0;)
			if (shapes[i].contains(r))
				return true;
		return false;
	}

	
	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return shapes[0].getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return shapes[0].getPathIterator(at, flatness);
	}

  /**
   * Draws the shapes.
   *
   * @param g graphics context
   */
	public void draw(Graphics2D g) {
		for (int i = shapes.length; --i >= 0;) {
			if (shapes[i] instanceof MultiShape) {
				((MultiShape) shapes[i]).draw(g);
			}
			else if (fills != null && i < fills.length && fills[i])
				g.fill(shapes[i]);
			else if (strokes != null && i < strokes.length && strokes[i] != null) {
				Stroke gstroke = g.getStroke();
				g.setStroke(strokes[i]);
				g.draw(shapes[i]);
				g.setStroke(gstroke);
			} else
				g.draw(shapes[i]);
		}		
	}
	
  /**
   * Transforms the shapes.
   *
   * @param transform the AffineTransform
   * @return a new transformed MultiShape
   */
	public MultiShape transform(AffineTransform transform) {
		Shape[] transformedShapes = new Shape[shapes.length];
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof MultiShape) {
				transformedShapes[i] = ((MultiShape) shapes[i]).transform(transform);
			} else {
				transformedShapes[i] = transform.createTransformedShape(shapes[i]);
			}
		}
		MultiShape shape = new MultiShape(transformedShapes);
		if (fills != null)
			shape.andFill(fills);
		if (strokes != null)
			shape.andStroke(strokes);
		return shape;
	}
	
  /**
   * Adds a draw shape.
   *
   * @param shape the shape to add
   * @param stroke the draw Stroke, may be null
   * @return this MultiShape
   */
	public MultiShape addDrawShape(Shape shape, Stroke stroke) {
		if (shape != null) {
			int newLength = shapes.length + 1;
			shapes = Arrays.copyOf(shapes, newLength);
			shapes[newLength-1] = shape;
			if (stroke != null) {
				if (strokes != null) {
					strokes = Arrays.copyOf(strokes, newLength);
				}
				else {
					strokes = new Stroke[newLength];
				}
				strokes[newLength-1] = stroke;
			}
		}
		return this;
	}
	
  /**
   * Adds a fill shape.
   *
   * @param shape the shape to add
   * @return this MultiShape
   */
	public MultiShape addFillShape(Shape shape) {
		if (shape != null) {
			int newLength = shapes.length + 1;
			shapes = Arrays.copyOf(shapes, newLength);
			shapes[newLength-1] = shape;
			if (fills != null) {
				fills = Arrays.copyOf(fills, newLength);
			}
			else {
				fills = new boolean[newLength];
			}
			fills[newLength-1] = true;
		}
		return this;
	}
	
}