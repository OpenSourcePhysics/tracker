/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

/**
 * This draws ruled lines and labels for a protractor.
 *
 * @author Douglas Brown
 */
public abstract class Ruler {
	
	protected static final float[] DASHED_LINE = new float[] { 4, 8 };
	protected static final int MAX_RULER_SIZE = 200;
	protected static final int DEFAULT_RULER_SIZE = 30;
	protected static final double MAX_RULER_LINE_SPACING = 100;
	protected static final double MIN_RULER_LINE_SPACING = 4;
	protected static final int NUMBER_OF_LEVELS = 3;
	protected static final int MIN_INSET_PER_LEVEL = 5;
	protected static final int DEFAULT_LABEL_GAP = 12;
	protected static final int DEFAULT_LINE_GAP = 4;

	protected InputTrack track;
	protected BasicStroke[] baseStrokes;
	protected BasicStroke[] strokes;
	protected BasicStroke dashedStroke;
	protected Color[] colors = new Color[NUMBER_OF_LEVELS];
	protected int alpha = 255;
	protected ArrayList<ArrayList<Line2D>> lines;
	protected MultiShape[] multiShapes = new MultiShape[NUMBER_OF_LEVELS];
	protected ArrayList<Label> labelMarks;
	protected Handle handle;
	protected TPoint utilityPoint;
	protected boolean visible, hitShapeVisible;
	protected double rulerSize;
	protected double rulerLineSpacing = MIN_RULER_LINE_SPACING;
	protected int insetPerLevel = MIN_INSET_PER_LEVEL;
	protected int labelGap = DEFAULT_LABEL_GAP;
	protected int lineGap = DEFAULT_LINE_GAP;
	protected AffineTransform transform = new AffineTransform();
	protected AffineTransform labelTransform = new AffineTransform();
	protected DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
	protected Double previousDistFromLineEnd;
	protected double previousLineSpacing, previousAngle;
	protected int prevSigFigs = 0;

	/**
	 * Constructor.
	 * 
	 * @param track a TapeMeasure or Protractor
	 */
	public Ruler(InputTrack track) {
		this.track = track;
		handle = new Handle();
		utilityPoint = new TPoint();
		lines = new ArrayList<ArrayList<Line2D>>();
		labelMarks =  new ArrayList<Label>();
		for (int i = 0; i < NUMBER_OF_LEVELS; i++) {
			lines.add(new ArrayList<Line2D>());
		}
		setRulerSize(DEFAULT_RULER_SIZE);
		setStrokeWidth(1);
		setColor(track.getColor());
	}
	
	/**
	 * Gets the mark to draw for a given frame.
	 * 
	 * @param trackerPanel the TrackerPanel to draw on
	 * @param n the frame number
	 * @return the Mark
	 */
	protected abstract Mark getMark(TrackerPanel trackerPanel, int n);
	
	/**
	 * Returns the Handle if this Ruler's hit shape intersects the supplied Rectangle.
	 * 
	 * @param trackerPanel the TrackerPanel
	 * @param hitRect the Rectangle
	 * @return the Handle if hit
	 */
	protected Interactive findInteractive(TrackerPanel trackerPanel, Rectangle hitRect) {
		// subclasses override if needed
		return null;
	}

	/**
	 * Set the handle position in image units.
	 * 
	 * @param x
	 * @param y
	 */
	protected void setHandleXY(double x, double y) {
		// subclasses override if needed
	}

	/**
	 * Gets the handle, used to adjust the ruler size
	 *
	 * @return the Handle TPoint
	 */
	public Handle getHandle() {
		return handle;
	}

	/**
	 * Sets the visibility of the hit shape
	 *
	 * @param vis true to draw the hit shape
	 */
	protected void setHitShapeVisible(boolean vis) {
		this.hitShapeVisible = vis && !track.isLocked();
	}
	
	/**
	 * Sets the stroke width.
	 *
	 * @param width the desired width
	 */
	public void setStrokeWidth(float width) {
		baseStrokes = new BasicStroke[] {new BasicStroke(width), new BasicStroke(width), new BasicStroke(width)};
		dashedStroke = new BasicStroke(width, 
				BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 
				8, DASHED_LINE, 0);
		strokes = new BasicStroke[baseStrokes.length];
	}

	/**
	 * Gets the stroke width.
	 *
	 * @return the width
	 */
	public float getStrokeWidth() {
		return baseStrokes[0].getLineWidth();
	}

	/**
	 * Refreshes the strokes.
	 */
	public void refreshStrokes() {
		int scale = FontSizer.getIntegerFactor();
		if (strokes[0] == null || strokes[0].getLineWidth() != scale * baseStrokes[0].getLineWidth()) {
			for (int i = 0; i < strokes.length; i++) {
				strokes[i] = new BasicStroke(scale * baseStrokes[i].getLineWidth());
			}
			dashedStroke = new BasicStroke(scale * baseStrokes[0].getLineWidth(), 
					BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_MITER, 
					8, DASHED_LINE, 0);		
		}
	}

	/**
	 * Sets the ruler size. May be positive or negative.
	 *
	 * @param size the desired size
	 */
	public void setRulerSize(double size) {
		int minSize = NUMBER_OF_LEVELS * MIN_INSET_PER_LEVEL;
		// add hysteresis to prevent too much jumping
		if (Math.abs(size) < minSize)
			return;
		rulerSize = size >= 0? 
				Math.max(Math.min(size, MAX_RULER_SIZE), minSize): 
				Math.min(Math.max(size, -MAX_RULER_SIZE), -minSize);
		insetPerLevel = Math.max((int) (Math.abs(rulerSize) / NUMBER_OF_LEVELS), MIN_INSET_PER_LEVEL);
	}

	/**
	 * Gets the ruler size--width for tapes, radius for protractors.
	 *
	 * @return the size
	 */
	public double getRulerSize() {
		return rulerSize;
	}
	
	/**
	 * Sets the ruler line spacing. The line spacing is the minimum allowed screen distance between
	 * ruler lines--the actual spacing depends on the scale.
	 *
	 * @param space the desired spacing
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
			for (int i = 0; i < colors.length; i++) {
				int alfa = alpha - i * alpha / (colors.length + 1);
				colors[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa);
			}
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

	/**
	 * Formats the specified value.
	 *
	 * @param value the value to format
	 * @param min the smallest value expected, used to determine format pattern
	 * @return the formatted value string
	 */
	public String getFormattedValue(double value, double min) {
		if (value == 0)
			return "0";
		int sigfigs = min >= 1000? 4: min >= 1? 0: min >= .1? 1: min >= .01? 2: min >= .001? 3: 4;
		if (prevSigFigs != sigfigs) {
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
			case 3:
				format.applyPattern(NumberField.DECIMAL_3_PATTERN);
				break;
			default:
				format.applyPattern("0E0");
			}
		}
		prevSigFigs = sigfigs;
		return format.format(value);
	}
	
	/**
	 * Gets the perpendicular screen distance from a Point to the tape or protractor base line.
	 *
	 * @return the distance in screen pixels
	 */
	protected double getScreenDistanceToBase(Point p) {			
    TPoint[] pts = track.getStep(track.trackerPanel.getFrameNumber()).getPoints();
		Point p1 = pts[0].getScreenPosition(track.trackerPanel);
		Point p2 = pts[1].getScreenPosition(track.trackerPanel);
		Line2D tapeLine = new Line2D.Double(p1, p2);
		return tapeLine.ptLineDist(p.x, p.y);
	}
	
	/**
	 * Determines if a TPoint p is to the left of the line defined by end1 and end2.
	 * 
	 * @param p the TPoint to test
	 * @param end1 one end of the line
	 * @param end2 second end of the line
	 * @return true if to the left
	 */
	protected boolean isLeft(TPoint p, TPoint end1, TPoint end2) {
    return ((end2.x - end1.x)*(p.y - end1.y) - (end2.y - end1.y)*(p.x - end1.x)) > 0;
 	}
	
	// ______________________ inner Handle class ________________________

	class Handle extends TPoint {
		
		private Handle() {
			super(0, 0);
		}

		@Override
		public void setXY(double x, double y) {
			setHandleXY(x, y); // handled by subclasses
		}
		
		protected void setScreenLocation(int xpix, int ypix, TrackerPanel trackerPanel) {
			utilityPoint.setScreenPosition(xpix, ypix, trackerPanel);
			setLocation(utilityPoint.x, utilityPoint.y);
		}
		
		@Override
		public void setAdjusting(boolean adjust) {
			if (adjust == isAdjusting)
				return;
			super.setAdjusting(adjust);
			if (isAdjusting) {
				previousDistFromLineEnd = null;
			}
		}
		
	}
	
	// ______________________ inner Label class ________________________

	class Label {
		int x, y;
		String text;
		Double rotation;
		
		Label(String s, double x, double y) {
			text = s;
			this.x = (int)x;
			this.y = (int)y;
		}
		
		void draw(Graphics2D g) {
			Graphics2D g2 = (Graphics2D) g.create();
	    FontMetrics metrics = g2.getFontMetrics();
	    int x1 = x - metrics.stringWidth(text) / 2;
	    int y1 = y - metrics.getHeight() / 2 + metrics.getAscent();
	    if (rotation != null) {
	  		labelTransform.setToRotation(rotation, x, y);		
				AffineTransform t = g2.getTransform();
				t.concatenate(labelTransform);
				g2.setTransform(t);
	    }
	    g2.drawString(text, x1, y1);
	    g2.dispose();
		}
	}

}
