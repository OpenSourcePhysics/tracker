/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert Hanson
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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;

/**
 * A MultiLineFootprint returns a set of line segments for a Point array of any
 * length > 1.
 *
 * @author Douglas Brown
 */
public class MultiLineFootprint extends LineFootprint {

	// instance fields

	protected boolean closed;

	/**
	 * Constructs a MultiLineFootprint.
	 *
	 * @param name the name
	 */
	public MultiLineFootprint(String name) {
		super(name);
		hitShapes = new Shape[0];
	}

	/**
	 * Gets a predefined MultiLineFootprint.
	 *
	 * @param name the name of the footprint
	 * @return the footprint
	 */
	public static MultiLineFootprint getFootprint(String name) {
		return (MultiLineFootprint) getFootprint(footprints, name);
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
	@Override
	public MultiShape getShape(Point[] points, int scale) {
		if (stroke == null || stroke.getLineWidth() != scale * baseStroke.getLineWidth()) {
			stroke = new BasicStroke(scale * baseStroke.getLineWidth());
		}
			
		MultiShape drawShape = new MultiShape();
  	for (int i=0; i<points.length-1; i++) {
      Point p1 = points[i];
      Point p2 = points[i+1];
      if (p1==null || p2==null) continue;
      line.setLine(p1, p2);
      drawShape.addDrawShape((Line2D)line.clone(), null);
  	}
  	if (closed && points.length>2 && points[0]!=null && points[points.length-1]!=null) {
      line.setLine(points[points.length-1], points[0]);
      drawShape.addDrawShape((Line2D)line.clone(), null);
  	}
    return drawShape;
	}

	// static fields
	private static Collection<LineFootprint> footprints = new HashSet<LineFootprint>();

	// static constants
	private static final MultiLineFootprint MULTILINE;
	private static final MultiLineFootprint BOLD_MULTILINE;

	// static initializers
	static {

		// LINE
		MULTILINE = new MultiLineFootprint("Footprint.MultiLine"); //$NON-NLS-1$
		footprints.add(MULTILINE);

		// BOLD_LINE
		BOLD_MULTILINE = new MultiLineFootprint("Footprint.BoldMultiLine"); //$NON-NLS-1$
		BOLD_MULTILINE.setStroke(new BasicStroke(2));
		footprints.add(BOLD_MULTILINE);

	}
}
