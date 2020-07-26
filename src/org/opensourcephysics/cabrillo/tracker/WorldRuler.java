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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.TPoint;

/**
 * This draws ruled lines and labels for a tape measure.
 *
 * @author Douglas Brown
 */
public class WorldRuler {
	
	private static final int DEFAULT_RULER_WIDTH = 20;
	private static final float[] DASHED_LINE = new float[] { 6, 6 };

	private TapeMeasure tape;
	private ArrayList<Line2D> majorLines, minorLines;
	private Stroke majorStroke, minorStroke, dashedStroke;
	boolean showMajorLines, showMinorLines;
	private Color majorLineColor, minorLineColor;
	private Handle handle;
	private TPoint[] lineEnds;
	private int alpha;
	private boolean visible, active;
	private int minRulerWidth = 10, maxRulerWidth = 200;
	private double rulerWidth;
	private int rulerLineSpacing = 8;
	private int majorMinorLineLengthDiff = 6;
	private int layoutToRulerGap = 10, rulerToTapeGap = 10;
	private AffineTransform transform = new AffineTransform();
  private HashMap<Integer, Line2D> hitLines = new HashMap<Integer, Line2D>();

	/**
	 * Constructor.
	 * 
	 * @param tape a tape measure
	 */
	public WorldRuler(TapeMeasure tape) {
		this.tape = tape;
		handle = new Handle();
		lineEnds = new TPoint[] {new TPoint(), new TPoint()};
		majorStroke = new BasicStroke(1);
		minorStroke = new BasicStroke(1);
		dashedStroke = new BasicStroke(2, 
				BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 
				8, DASHED_LINE, 0);
		showMajorLines = showMinorLines = true;
		majorLines = new ArrayList<Line2D>();
		minorLines = new ArrayList<Line2D>();
		alpha = 255;
		setRulerWidth(DEFAULT_RULER_WIDTH);
	}
	
	/**
	 * Gets the mark to draw.
	 * 
	 * @param trackerPanel the panel to draw on
	 */
	protected Mark getMark(TrackerPanel trackerPanel) {
		if (!showMajorLines)
			return new Mark() {
				@Override
				public void draw(Graphics2D g, boolean highlighted) {
			}
		};
		// get ends and length of the tape
		int n = trackerPanel.getFrameNumber();
		TPoint pt1 = tape.getStep(n).getPoints()[0];
		TPoint pt2 = tape.getStep(n).getPoints()[1];
		double length = pt1.distance(pt2);
		Point screen1 = pt1.getScreenPosition(trackerPanel);
		Point screen2 = pt2.getScreenPosition(trackerPanel);
		double screenLength = screen1.distance(screen2);
		Point2D world1 = pt1.getWorldPosition(trackerPanel);
		Point2D world2 = pt2.getWorldPosition(trackerPanel);
		double worldLength = world1.distance(world2);

		// determine a world delta between lines
		int lineCount = (int) (screenLength / rulerLineSpacing);
		// make a first approximation
		double delta = worldLength / lineCount; 

		// find power of ten
		double pow = 1;
		while (pow * 10 < delta)
			pow *= 10;
		while (pow > delta)
			pow /= 10;

		// get "significand" and increase to nearest 2, 5 or 10
		double significand = delta / pow; // number between 1 and 10
		int minorSpacing = 10;
		int majorSpacing = 100;
		if (significand < 2) {
			minorSpacing = 2;
			majorSpacing = 10;
		} else if (significand < 5) {
			minorSpacing = 5;
			majorSpacing = 10;
		}

		// determine final value of delta
		delta = minorSpacing * pow;

		majorLines.clear();
		minorLines.clear();
		int offset = rulerWidth < 0? -majorMinorLineLengthDiff: majorMinorLineLengthDiff;
		int gap = rulerToTapeGap*(offset / majorMinorLineLengthDiff); // just sets +/-

		// create vertical ruler lines
		long howMany = Math.round(worldLength / delta)+1;
		for (int i = 0; i < howMany; i++) {
			boolean isMajor = (i * minorSpacing) % majorSpacing == 0;
			if (!isMajor && !showMinorLines)
				continue;
			ArrayList<Line2D> lines = isMajor ? majorLines : minorLines;
			double lineLength = isMajor ? rulerWidth: rulerWidth - offset;
			
			double cos = tape.trackerPanel.getCoords().getCosine(n);
			double sin = tape.trackerPanel.getCoords().getSine(n);
			double x = world1.getX() + i * delta * cos;
			double y = world1.getY() - i * delta * sin;
			
			Line2D line = new Line2D.Double();
			lines.add(line);
			lineEnds[0].setWorldPosition(x, y, trackerPanel);
			Point base = lineEnds[0].getScreenPosition(trackerPanel);
			line.setLine(base.x, base.y - gap, base.x, base.y - gap - lineLength);
		}
		// set up hit line to produce unrotated MultiShape for drawing with rotation
		Line2D hitLine = getHitShape(n);
		double hitDistScreen = rulerWidth >= 0? 
				rulerToTapeGap + rulerWidth:
				rulerWidth - rulerToTapeGap;
		lineEnds[0].setWorldPosition(world1.getX(), world1.getY(), trackerPanel);
		Point base = lineEnds[0].getScreenPosition(trackerPanel);
		hitLine.setLine(base.x, base.y - hitDistScreen, base.x + screenLength, base.y - hitDistScreen);
		Shape hitDrawshape = (Shape)hitLine.clone();
					
		// rotate around screen position of end 1
		Point p = pt1.getScreenPosition(trackerPanel);
		double theta = pt1.angle(pt2);
		transform.setToRotation(theta, p.x, p.y);		

		// set lineEnds for handle
		double sin = pt1.sin(pt2);
		double cos = pt1.cos(pt2);
		double hitDist = hitDistScreen * length / screenLength;
		lineEnds[0].setLocation(pt1.x - hitDist * sin, pt1.y - hitDist * cos);
		lineEnds[1].setLocation(pt2.x - hitDist * sin, pt2.y - hitDist * cos);
		hitLine.setLine(lineEnds[0].getScreenPosition(trackerPanel), lineEnds[1].getScreenPosition(trackerPanel));
		
		// assemble multishapes
		Line2D[] majLines = majorLines.toArray(new Line2D[majorLines.size()]);
		Stroke[] majStrokes = new Stroke[majLines.length];
		Arrays.fill(majStrokes, majorStroke);
		MultiShape majorMultiShape = new MultiShape(majLines).andStroke(majStrokes).transform(transform);
		Line2D[] minLines = minorLines.toArray(new Line2D[minorLines.size()]);
		Stroke[] minStrokes = new Stroke[minLines.length];
		Arrays.fill(minStrokes, minorStroke);
		MultiShape minorMultiShape = new MultiShape(minLines).andStroke(minStrokes).transform(transform);
		MultiShape hitMultiShape = new MultiShape(hitDrawshape).andStroke(dashedStroke).transform(transform);
		
		// return the mark
		return new Mark() {
			@Override
			public void draw(Graphics2D g, boolean highlighted) {
				Graphics2D g2 = (Graphics2D) g.create();
				if (OSPRuntime.setRenderingHints)
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(majorLineColor);
				if (Math.abs(rulerWidth) >= minRulerWidth)
					majorMultiShape.draw(g2);
				g2.setPaint(minorLineColor);				
				if (Math.abs(rulerWidth) >= minRulerWidth)
					minorMultiShape.draw(g2);
				if (active)
					hitMultiShape.draw(g2);
				g2.dispose();
			}
		};
	}
	
	/**
	 * Gets the handle, used to adjust the ruler width
	 *
	 * @return the Handle TPoint
	 */
	public TPoint getHandle() {
		return handle;
	}

	/**
	 * Gets the hit shape.
	 *
	 * @return the hit shape
	 */
	public Line2D getHitShape(int frameNumber) {
		Line2D hitLine = hitLines.get(frameNumber);
		if (hitLine == null) {
			hitLine = new Line2D.Double();
			hitLines.put(frameNumber, hitLine);
		}
		return hitLine;
	}
	
	/**
	 * Sets the active flag. When true, the hitLine is drawn
	 *
	 * @param active true to draw the hitline
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets the ruler width. May be positive or negative.
	 *
	 * @param the desired width
	 */
	public void setRulerWidth(double width) {
		rulerWidth = width >= 0? 
				Math.min(width, maxRulerWidth) : 
				Math.max(width, -maxRulerWidth);
	}

	/**
	 * Gets the ruler width.
	 *
	 * @return the width
	 */
	public double getRulerWidth() {
		return rulerWidth;
	}

	/**
	 * Gets the color.
	 *
	 * @return the line color
	 */
	public Color getColor() {
		return majorLineColor;
	}

	/**
	 * Sets the color.
	 *
	 * @param color the desired line color
	 */
	public void setColor(Color color) {
		if (color != null) {
			majorLineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
			minorLineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha/2);
		}
	}

	/**
	 * Gets the alpha.
	 *
	 * @return the line color alpha
	 */
	public int getAlpha() {
		return alpha;
	}

	/**
	 * Sets the alpha.
	 *
	 * @param alpha the desired alpha
	 */
	public void setAlpha(int alpha) {
		alpha = Math.min(alpha, 255);
		alpha = Math.max(alpha, 0);
		this.alpha = alpha;
		setColor(majorLineColor);
	}

	/**
	 * Sets the visibility of the ruler.
	 *
	 * @param isVisible true to draw this ruler
	 */
	public void setVisible(boolean isVisible) {
		visible = isVisible;
	}

	/**
	 * Gets the visibility of the ruler.
	 *
	 * @return true if visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visibility of the minor x grid lines.
	 *
	 * @param visible true to display the minor x grid lines
	 */
	public void setMinorLinesVisible(boolean visible) {
		showMinorLines = visible;
	}

	/**
	 * Gets TextLayout screen position. Unlike most positions, this one refers to
	 * the lower left corner of the text.
	 *
	 * @param trackerPanel the tracker panel
	 * @param bounds the bounds of the text layout
	 * @param pt1 TPoint used to get angle
	 * @param pt2 TPoint used to get angle
	 * @return the screen position for the layout
	 */
	protected Point getLayoutPosition(TrackerPanel trackerPanel, 
			Rectangle2D bounds, TPoint pt1, TPoint pt2) {
		double cos = pt1.cos(pt2);
		double sin = pt1.sin(pt2);
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		double halfwsin = w * sin / 2;
		double halfhcos = h * cos / 2;
		
		// find distance from end to layout center
		double d = Math.sqrt((halfwsin*halfwsin) + (halfhcos*halfhcos)) + layoutToRulerGap;
		if (Math.abs(rulerWidth) >= minRulerWidth)
			d += rulerToTapeGap + Math.abs(rulerWidth);
		
		// set location relative to tape end
		TPoint tapeEnd = tape.getStep(trackerPanel.getFrameNumber()).getPoints()[1];
		Point p = tapeEnd.getScreenPosition(trackerPanel);
		if (rulerWidth > -6)
			p.setLocation((int) (p.x - d * sin - w / 2), (int) (p.y - d * cos + h / 2));
		else
			p.setLocation((int) (p.x + d * sin - w / 2), (int) (p.y + d * cos + h / 2));
		return p;
	}
	
	// ______________________ inner Handle class ________________________

	class Handle extends Step.Handle {
		
		private Handle() {
			super(0, 0);
		}

		@Override
		public void setXY(double x, double y) {
			setLocation(x, y);
			if (tape.trackerPanel != null && tape.trackerPanel.getSelectedPoint() == this) {

				// find distance from tape, set ruler width
				double dist = getScreenDistanceToTape();
				boolean isLeft = isLeft(tape.getStep(tape.trackerPanel.getFrameNumber()).getPoints()[0],
		    		tape.getStep(tape.trackerPanel.getFrameNumber()).getPoints()[1],
		    		handle);
				
				setRulerWidth(isLeft? 
						Math.min(rulerToTapeGap - dist, -minRulerWidth): 
						Math.max(dist - rulerToTapeGap, minRulerWidth));
				tape.repaint();				
			}
		}
		
		@Override
		public void setAdjusting(boolean adjust) {
			if (adjust == isAdjusting)
				return;
			super.setAdjusting(adjust);
			if (!adjust) {
				tape.trackerPanel.setSelectedPoint(null);
				tape.repaint();
			}
		}
		
		/**
		 * Gets the perpendicular screen distance from this handle to the tape.
		 *
		 * @return the distance in screen pixels
		 */
		private double getScreenDistanceToTape() {			
	    TPoint[] pts = tape.getStep(tape.trackerPanel.getFrameNumber()).getPoints();
			Point p = getScreenPosition(tape.trackerPanel);
			Point p1 = pts[0].getScreenPosition(tape.trackerPanel);
			Point p2 = pts[1].getScreenPosition(tape.trackerPanel);
			return shortestDistance(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
		}
		
		// shortest distance from point 3 to line between points 1 and 2
		private double shortestDistance(float x1, float y1, float x2, float y2, float x3, float y3) {
      float px=x2-x1;
      float py=y2-y1;
      float temp=(px*px)+(py*py);
      float u=((x3 - x1) * px + (y3 - y1) * py) / (temp);
      if (u>1)
        u=1;
      else if (u<0)
        u=0;
      float x = x1 + u * px;
      float y = y1 + u * py;

      float dx = x - x3;
      float dy = y - y3;
      double dist = Math.sqrt(dx*dx + dy*dy);
      return dist;
    }
		
		/**
		 * Determines if a TPoint c is to the left of the line defined by a and b.
		 * 
		 * @param a one end of the line
		 * @param b second end of the line
		 * @param c the point
		 * @return true if to the left
		 */
		private boolean isLeft(TPoint a, TPoint b, TPoint c) {
	    return ((b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x)) > 0;
	 	}
	}
}
