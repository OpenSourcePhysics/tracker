package org.opensourcephysics.cabrillo.tracker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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

	MultiShape(Shape... shapes) {
		this.shapes = shapes;
	}


	public MultiShape andFill(boolean... fills) {
		this.fills = fills;
		return this;
	}


	private Shape[] shapes;
	private boolean[] fills;
	
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

	public void draw(Graphics2D g) {
		for (int i = shapes.length; --i >= 0;) {
			if (fills != null && i < fills.length && fills[i])
				g.fill(shapes[i]);
			else
				g.draw(shapes[i]);
		}
		
	}
	
}