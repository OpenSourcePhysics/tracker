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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;

/**
 * This draws ruled lines and labels for a tape measure.
 *
 * @author Douglas Brown
 */
public class WorldRuler {
	
	private static final int DEFAULT_RULER_WIDTH = 20;
	private static final float[] DASHED_LINE = new float[] { 3, 9 };
	private static final int RULER_LABEL_GAP = 10, RULER_TAPE_GAP = 1;
	private static final int MAX_RULER_WIDTH = 200;
	private static final int MIN_INSET_PER_LEVEL = 5;
	private static final int NUMBER_OF_LEVELS = 3;
	private static final double MAX_RULER_LINE_SPACING = 100;
	private static final double DEFAULT_RULER_LINE_SPACING = 8;
	private static final double MIN_RULER_LINE_SPACING = 5;
	private static final int DEFAULT_DROPEND_SIZE = 12;
	

	private TapeMeasure tape;
	private ArrayList<ArrayList<Line2D>> lines;
	private BasicStroke[] strokes;
	private Color[] colors = new Color[NUMBER_OF_LEVELS];
	private MultiShape[] multiShapes = new MultiShape[NUMBER_OF_LEVELS];
	private ArrayList<TextMark> textMarks;
	private Handle handle;
	private TPoint[] lineEnds;
	private BasicStroke dashedStroke;
	private int alpha = 255;
	private boolean visible, active;
	private double rulerWidth;
	protected double rulerLineSpacing = DEFAULT_RULER_LINE_SPACING;
	private int insetPerLevel = MIN_INSET_PER_LEVEL;
	private AffineTransform transform = new AffineTransform();
  private HashMap<Integer, Line2D> hitLines = new HashMap<Integer, Line2D>();
	protected Footprint[] footprints;
	private int dropEndSize = DEFAULT_DROPEND_SIZE;
	protected DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
	private Double previousDistFromLineEnd;
	private double previousLineSpacing, previousAngle;

	/**
	 * Constructor.
	 * 
	 * @param tape a tape measure
	 */
	public WorldRuler(TapeMeasure tape) {
		this.tape = tape;
		handle = new Handle();
		lineEnds = new TPoint[] {new TPoint(), new TPoint()};
		lines = new ArrayList<ArrayList<Line2D>>();
		textMarks =  new ArrayList<TextMark>();
		for (int i = 0; i < NUMBER_OF_LEVELS; i++) {
			lines.add(new ArrayList<Line2D>());
		}
		setRulerWidth(DEFAULT_RULER_WIDTH);
		setStrokeWidth(1);
		footprints = new Footprint[] { LineFootprint.getFootprint("Footprint.Line"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.BoldLine") }; //$NON-NLS-1$
	}
	
	/**
	 * Gets the mark to draw.
	 * 
	 * @param trackerPanel the panel to draw on
	 */
	protected Mark getMark(TrackerPanel trackerPanel) {
		if (trackerPanel instanceof WorldTView)
			return null;
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

		// make a first approximation for world delta between lines
		double zoomFactor = trackerPanel.getXPixPerUnit();
//		if (trackerPanel instanceof WorldTView) {
//			zoomFactor = ((WorldTView)trackerPanel).getTrackerPanel().getXPixPerUnit();
//		}
		double delta = rulerLineSpacing / (zoomFactor * tape.trackerPanel.getCoords().getScaleX(n));

		// find power of ten
		double power = 1;
		while (power * 10 < delta)
			power *= 10;
		while (power > delta)
			power /= 10;

		// get "significand" 
		double significand = delta / power; // number between 1 and 10
		// increase spacing to nearest 2, 5 or 10
		int spacing = significand <= 2? 2: significand <= 5? 5: 10;
		// determine final value of delta
		delta = spacing * power;
		
		
		int majorSpacing = significand <= 2? 25: 10;
		int halfSpacing = significand > 2 && significand <= 5? 2: 5;

		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).clear();
		}
		textMarks.clear();
		int inset = rulerWidth > 0? insetPerLevel: -insetPerLevel;
		int gap = rulerWidth > 0? RULER_TAPE_GAP: -RULER_TAPE_GAP;
		

		double minRulerMarking = majorSpacing * delta;
		// create vertical ruler lines, then rotate
		int lineCount = (int) Math.round(worldLength / delta) + 1;
		OSPLog.debug("pig "+worldLength+"  "+delta+"  "+worldLength / delta+"   "+lineCount);
		for (int i = 0; i < lineCount; i++) {
			int level = i % majorSpacing == 0? 0: i % halfSpacing == 0? 1: 2;
			if (lineCount-1 < majorSpacing && level > 0) {
				level--;
				minRulerMarking = halfSpacing * delta;				
			}
			ArrayList<Line2D> drawLines = lines.get(level);
			double lineLength = rulerWidth - inset*level;
			
			double cos = tape.trackerPanel.getCoords().getCosine(n);
			double sin = tape.trackerPanel.getCoords().getSine(n);
			double x = world1.getX() + i * delta * cos;
			double y = world1.getY() - i * delta * sin;
			
			Line2D line = new Line2D.Double();
			drawLines.add(line);
			lineEnds[0].setWorldPosition(x, y, trackerPanel);
			Point base = lineEnds[0].getScreenPosition(trackerPanel);
			// draw drop end at 0 if not a stick
			double bottom = i == 0 && !tape.isStickMode()? base.y +dropEndSize: base.y - gap;
			line.setLine(base.x, bottom, base.x, base.y - gap - lineLength);
			
			// if not stick, draw second drop end
			if (i == lineCount - 1 && !tape.isStickMode()) {
				line = new Line2D.Double();
				lines.get(0).add(line);
				x = world1.getX() + worldLength * cos;
				y = world1.getY() - worldLength * sin;
				lineEnds[0].setWorldPosition(x, y, trackerPanel);
				base = lineEnds[0].getScreenPosition(trackerPanel);
				line.setLine(base.x, base.y +dropEndSize, base.x, base.y);
			}
			
			// create the textMark to label the major lines
			if (level == 0) {
				String s = getFormattedLength(i * delta, minRulerMarking);
				s += trackerPanel.getUnits(tape, TapeMeasure.dataVariables[1]);
				textMarks.add(new TextMark(s, base.x, base.y - gap - lineLength - 10));
			}			
		}
		// set up hit line to produce unrotated MultiShape for drawing with rotation
		Line2D hitLine = getHitLine(n);
		double hitDistScreen = rulerWidth >= 0? 
				RULER_TAPE_GAP + rulerWidth:
				rulerWidth - RULER_TAPE_GAP;
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
		for (int i = 0; i < lines.size(); i++) {
			ArrayList<Line2D> drawLines = lines.get(i);
			Line2D[] lineArray = drawLines.toArray(new Line2D[drawLines.size()]);
			Stroke[] lineStrokes = new Stroke[lineArray.length];
			Arrays.fill(lineStrokes, strokes[i]);
			multiShapes[i] = new MultiShape(lineArray).andStroke(lineStrokes).transform(transform);			
		}

		MultiShape hitMultiShape = new MultiShape(hitDrawshape).andStroke(dashedStroke).transform(transform);
		
		// return the mark
		return new Mark() {
			@Override
			public void draw(Graphics2D g, boolean highlighted) {
				Graphics2D g2 = (Graphics2D) g.create();
				if (OSPRuntime.setRenderingHints)
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				for (int i = 0; i < multiShapes.length; i++) {
					g2.setPaint(colors[i]);
					multiShapes[i].draw(g2);
				}
				g2.setPaint(colors[0]);
				if (active) {
					hitMultiShape.draw(g2);
				}
				// draw text marks
				AffineTransform t = g2.getTransform();
				t.concatenate(transform);
				g2.setTransform(t);
				for (int i = 0; i < textMarks.size(); i++) {
					textMarks.get(i).draw(g2);
				}
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
	 * Gets the hit line.
	 *
	 * @param frameNumber the frame number
	 * @return a Line2D
	 */
	public Line2D getHitLine(int frameNumber) {
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
	protected void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Sets the stroke width.
	 *
	 * @param width the desired width
	 */
	public void setStrokeWidth(float width) {
		strokes = new BasicStroke[] {new BasicStroke(width), new BasicStroke(width), new BasicStroke(width)};
		dashedStroke = new BasicStroke(width, 
				BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 
				8, DASHED_LINE, 0);		
	}

	/**
	 * Gets the stroke width.
	 *
	 * @return the width
	 */
	public float getStrokeWidth() {
		return strokes[0].getLineWidth();
	}

	/**
	 * Sets the ruler width. May be positive or negative.
	 *
	 * @param width the desired width
	 */
	public void setRulerWidth(double width) {
		int minWidth = NUMBER_OF_LEVELS * MIN_INSET_PER_LEVEL;
		rulerWidth = width >= 0? 
				Math.max(Math.min(width, MAX_RULER_WIDTH), minWidth): 
				Math.min(Math.max(width, -MAX_RULER_WIDTH), -minWidth);
		insetPerLevel = Math.max((int) (Math.abs(rulerWidth) / NUMBER_OF_LEVELS), MIN_INSET_PER_LEVEL);
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
	 * Sets the ruler line spacing.
	 *
	 * @param space the desired (minimum) spacing
	 */
	public void setLineSpacing(double space) {
		rulerLineSpacing = Math.min(Math.max(space, MIN_RULER_LINE_SPACING), MAX_RULER_LINE_SPACING);
	}

	/**
	 * Gets the ruler line spacing.
	 *
	 * @return the spacing
	 */
	public double getLineSpacing() {
		return rulerLineSpacing;
	}

	/**
	 * Gets the color.
	 *
	 * @return the line color
	 */
	public Color getColor() {
		return colors[0];
	}

	/**
	 * Sets the color.
	 *
	 * @param color the desired line color
	 */
	public void setColor(Color color) {
		if (color != null) {
			colors[0] = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
			colors[1] = new Color(color.getRed(), color.getGreen(), color.getBlue(), 3*alpha/4);
			colors[2] = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha/2);
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
		setColor(colors[1]);
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
	
	public Footprint getFootprint() {
		return tape.isStickMode()? tape.getFootprint(): 
			getStrokeWidth() == 1? footprints[0]: footprints[1];
	}

	/**
	 * Gets the format.
	 *
	 * @return the format
	 */
	public DecimalFormat getFormat() {
		format.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
		return format;
	}

	/**
	 * Formats the specified length.
	 *
	 * @param length the value to format
	 * @param min the smallest value expected
	 * @return the formatted length string
	 */
	public String getFormattedLength(double length, double min) {
		int sigfigs = min >= 1? 0: min >= .1? 1: min >= .01? 2: 3;
		switch (sigfigs) {
		case 0:
			format.applyPattern(NumberField.INTEGER_PATTERN);
			break;
		case 1:
			format.applyPattern(NumberField.DECIMAL_1_PATTERN);
			break;
		case 2:
			format.applyPattern(NumberField.DECIMAL_2_PATTERN);
			break;
		default:
			format.applyPattern(NumberField.DECIMAL_3_PATTERN);
		}
		return format.format(length);
	}

	/**
	 * Gets TextLayout screen position. Unlike most positions, this one refers to
	 * the lower left corner of the text.
	 *
	 * @param trackerPanel the tracker panel
	 * @param bounds the bounds of the text layout
	 * @param pt1 TPoint used to get angle
	 * @param pt2 TPoint used to get angle
	 * @param startingPoint position is located relative to this point
	 * @return the screen position for the layout
	 */
	protected Point getLayoutPosition(TrackerPanel trackerPanel, 
			Rectangle2D bounds, TPoint pt1, TPoint pt2, Point startingPoint) {
//		double w = bounds.getWidth();
//		double h = bounds.getHeight();
//		endPoint1.setLocation(end1);
//		endPoint2.setLocation(end2);
//		// the following code is to determine the position on a world view
//		if (!trackerPanel.isDrawingInImageSpace()) {
//			AffineTransform at = trackerPanel.getCoords().getToWorldTransform(n);
//			at.transform(endPoint1, endPoint1);
//			endPoint1.y = -endPoint1.y;
//			at.transform(endPoint2, endPoint2);
//			endPoint2.y = -endPoint2.y;
//		}
		
		
		double cos = pt1.cos(pt2);
		double sin = pt1.sin(pt2);
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		double halfwsin = w * sin / 2;
		double halfhcos = h * cos / 2;
		
		// find distance from end to layout center
		double d = Math.sqrt((halfwsin*halfwsin) + (halfhcos*halfhcos)) + RULER_LABEL_GAP;
		d += RULER_TAPE_GAP + Math.abs(rulerWidth);
		
		// set location relative to tape end
		TPoint tapeEnd = tape.getStep(trackerPanel.getFrameNumber()).getPoints()[1];
		Point p = startingPoint!= null? startingPoint: tapeEnd.getScreenPosition(trackerPanel);
		if (rulerWidth > 0)
			p.setLocation((int) (p.x - d * sin - w / 2), (int) (p.y - d * cos + h / 2));
		else
			p.setLocation((int) (p.x + d * sin - w / 2), (int) (p.y + d * cos + h / 2));
		return p;
	}
	
	// ______________________ inner Handle class ________________________

	class Handle extends Step.Handle {
		
		private Line2D tapeLine = new Line2D.Double();
		
		private Handle() {
			super(0, 0);
		}

		@Override
		public void setXY(double x, double y) {
			setLocation(x, y);
			if (tape.trackerPanel != null && tape.trackerPanel.getSelectedPoint() == this) {
				int n = tape.trackerPanel.getFrameNumber();
				Point p = new Point(getScreenPosition(tape.trackerPanel));
				
				// find distance from tape, set ruler width
				double dist = getScreenDistanceToTape(p);
				boolean isLeft = isLeft(tape.getStep(n).getPoints()[0],
		    		tape.getStep(tape.trackerPanel.getFrameNumber()).getPoints()[1],
		    		handle);
				
				setRulerWidth(isLeft? 
						RULER_TAPE_GAP - dist: 
						dist - RULER_TAPE_GAP);
				
				// find distance from hit line end, set rulerSpacing
				Point2D end = getHitLine(n).getP1();
				dist = p.distance(end);
				if (previousDistFromLineEnd == null) {					
					previousDistFromLineEnd = dist;
					previousLineSpacing = rulerLineSpacing;
					previousAngle = Math.atan2(p.y - end.getY(), p.x - end.getX());
				}
				else {
					double angle = Math.atan2(p.y - end.getY(), p.x - end.getX());
					double angleChange = Math.abs(angle-previousAngle);
					double delta = angleChange < 1? dist - previousDistFromLineEnd: -dist - previousDistFromLineEnd;					
					setLineSpacing(previousLineSpacing + (int) (delta / 5));
				}
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
				previousDistFromLineEnd = null;
				tape.repaint();
			}
		}
		
		/**
		 * Gets the perpendicular screen distance from this handle to the tape.
		 *
		 * @return the distance in screen pixels
		 */
		private double getScreenDistanceToTape(Point p) {			
	    TPoint[] pts = tape.getStep(tape.trackerPanel.getFrameNumber()).getPoints();
			Point p1 = pts[0].getScreenPosition(tape.trackerPanel);
			Point p2 = pts[1].getScreenPosition(tape.trackerPanel);
			tapeLine.setLine(p1, p2);
			return tapeLine.ptLineDist(p.x, p.y);
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
	
	// ______________________ inner TextMark class ________________________

	class TextMark {
		int x, y;
		String text;
		
		TextMark(String s, int x, double y) {
			text = s;
			this.x = x;
			this.y = (int)y;
		}
		
		void draw(Graphics2D g) {
	    FontMetrics metrics = g.getFontMetrics();
	    int x1 = x - metrics.stringWidth(text) / 2;
	    int y1 = y - metrics.getHeight() / 2 + metrics.getAscent();
	    g.drawString(text, x1, y1);
		}
	}

}
