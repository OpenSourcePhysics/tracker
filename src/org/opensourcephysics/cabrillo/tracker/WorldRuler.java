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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

/**
 * This draws ruled lines and labels for a tape measure.
 *
 * @author Douglas Brown
 */
public class WorldRuler extends Ruler {
	
	private static final int DEFAULT_DROPEND_SIZE = 12;
	private static final int MIN_RULER_LABEL_SPACING = 50;
	
	private HashMap<Integer, Line2D> hitLines = new HashMap<Integer, Line2D>();	
	private int dropEndSize = DEFAULT_DROPEND_SIZE;

	/**
	 * Constructor.
	 * 
	 * @param tape a tape measure
	 */
	public WorldRuler(TapeMeasure tape) {
		super(tape);
		setLineSpacing(2 * Ruler.MIN_RULER_LINE_SPACING);
	}
	
	@Override
	protected Mark getMark(TrackerPanel trackerPanel, int n) {
		if (trackerPanel instanceof WorldTView)
			return null;
		refreshStrokes();
		format.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
		// get ends and length of the track
		TPoint pt0 = track.getStep(n).getPoints()[0];
		TPoint pt1 = track.getStep(n).getPoints()[1];
		double length = pt0.distance(pt1);
		double tapeSin = pt0.sin(pt1);
		double tapeCos = pt0.cos(pt1);
		Point screen0 = pt0.getScreenPosition(trackerPanel);
		Point screen1 = pt1.getScreenPosition(trackerPanel);
		double screenLength = screen0.distance(screen1);
		Point2D world0 = pt0.getWorldPosition(trackerPanel);
		Point2D world1 = pt1.getWorldPosition(trackerPanel);
		double worldLength = world0.distance(world1);

		// make a first approximation for world delta between lines
		double zoomFactor = trackerPanel.getXPixPerUnit();
		double delta = rulerLineSpacing / (zoomFactor * track.trackerPanel.getCoords().getScaleX(n));

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
		
		
		int majorSpacing = spacing == 2? 25: 10;
		int halfSpacing = spacing == 5? 2: 5;

		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).clear();
		}
		labelMarks.clear();
		int inset = rulerSize > 0? insetPerLevel: -insetPerLevel;
		int gap = rulerSize > 0? lineGap: -lineGap;
		

		double formatMinValue = majorSpacing * delta;
		double coordsCos = track.trackerPanel.getCoords().getCosine(n);
		double coordsSin = track.trackerPanel.getCoords().getSine(n);
		int prevLabelIndex = 0;
		double factor = FontSizer.getFactor();
		// create vertical ruler lines, then rotate
		int lineCount = (int) Math.round(worldLength / delta) + 1;
		for (int i = 0; i < lineCount; i++) {
			int level = i % majorSpacing == 0? 0: i % halfSpacing == 0? 1: 2;
			if (lineCount-1 < majorSpacing && level > 0) {
				level--;
				formatMinValue = halfSpacing * delta;				
			}
			
			ArrayList<Line2D> drawLines = lines.get(level);
			double lineLength = rulerSize - inset*level; // negative for downward lines			
			double x = world0.getX() + i * delta * coordsCos;
			double y = world0.getY() - i * delta * coordsSin;
			
			Line2D line = new Line2D.Double();
			drawLines.add(line);
			utilityPoint.setWorldPosition(x, y, trackerPanel);
			Point base = utilityPoint.getScreenPosition(trackerPanel);
			// draw drop end at 0 if track footprint is a line
			boolean isLineFootprint = track.getFootprint().getClass() == LineFootprint.class;
			int drop = rulerSize > 0? dropEndSize: -dropEndSize;
			double bottom = i == 0 && isLineFootprint? base.y + drop: base.y - gap;
			line.setLine(base.x, bottom, base.x, base.y - gap - lineLength);
			
			// create the labelMark for major lines--BEFORE drawing second drop end
			if (level == 0) {				
				double screenDelta = (i - prevLabelIndex) * delta * screenLength / worldLength;
				if (i == 0 || screenDelta > factor * MIN_RULER_LABEL_SPACING) {
					String s = getFormattedValue(i * delta, formatMinValue);
					s += trackerPanel.getUnits(track, TapeMeasure.dataVariables[1]);
					double offset = rulerSize > 0? 
							gap + lineLength + factor * labelGap: 
							gap + lineLength - factor * labelGap;
					Label label = new Label(s, base.x, base.y - offset);
					label.rotation = tapeCos < 0? Math.PI: null;
					labelMarks.add(label);
					prevLabelIndex = i;
				}
			}		
			
			// if line footprint, draw second drop end
			if (i == lineCount - 1 && isLineFootprint) {
				line = new Line2D.Double();
				lines.get(0).add(line);
				x = world0.getX() + worldLength * coordsCos;
				y = world0.getY() - worldLength * coordsSin;
				utilityPoint.setWorldPosition(x, y, trackerPanel);
				base = utilityPoint.getScreenPosition(trackerPanel);
				line.setLine(base.x, base.y + drop, base.x, base.y);
			}
			
		}
		// set up hit line to produce unrotated MultiShape for drawing with rotation
		Line2D hitLine = getHitLine(n);
		double hitDistScreen = rulerSize >= 0? 
				lineGap + rulerSize:
				rulerSize - lineGap;
		utilityPoint.setWorldPosition(world0.getX(), world0.getY(), trackerPanel);
		Point base = utilityPoint.getScreenPosition(trackerPanel);
		hitLine.setLine(base.x, base.y - hitDistScreen, base.x + screenLength, base.y - hitDistScreen);
		Shape hitDrawShape = (Shape)hitLine.clone();
					
		// rotate around screen position of end 1
		Point p = pt0.getScreenPosition(trackerPanel);
		double theta = pt0.angle(pt1);
		transform.setToRotation(theta, p.x, p.y);		

		// set lineEnds for handle
		double hitDist = hitDistScreen * length / screenLength;
		utilityPoint.setLocation(pt0.x - hitDist * tapeSin, pt0.y - hitDist * tapeCos);
		p.setLocation(utilityPoint.getScreenPosition(trackerPanel));
		utilityPoint.setLocation(pt1.x - hitDist * tapeSin, pt1.y - hitDist * tapeCos);
		hitLine.setLine(p, utilityPoint.getScreenPosition(trackerPanel));
		
		// assemble multishapes
		for (int i = 0; i < lines.size(); i++) {
			ArrayList<Line2D> drawLines = lines.get(i);
			Line2D[] lineArray = drawLines.toArray(new Line2D[drawLines.size()]);
			Stroke[] lineStrokes = new Stroke[lineArray.length];
			Arrays.fill(lineStrokes, strokes[i]);
			multiShapes[i] = new MultiShape(lineArray).andStroke(lineStrokes).transform(transform);			
		}

		MultiShape hitMultiShape = new MultiShape(hitDrawShape).andStroke(dashedStroke).transform(transform);
		MultiShape[] myShapes = Arrays.copyOf(multiShapes, multiShapes.length);
		AffineTransform myTransform = new AffineTransform(transform);
		Label[] myLabels = labelMarks.toArray(new Label[labelMarks.size()]);
		// return the mark
		return new Mark() {
			@Override
			public void draw(Graphics2D g, boolean highlighted) {
				Graphics2D g2 = (Graphics2D) g.create();
				if (OSPRuntime.setRenderingHints)
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				for (int i = 0; i < myShapes.length; i++) {
					g2.setPaint(colors[i]);
					myShapes[i].draw(g2);
				}
				g2.setPaint(colors[0]);
				if (hitShapeVisible) {
					hitMultiShape.draw(g2);
				}
				// draw text marks
				AffineTransform t = g2.getTransform();
				t.concatenate(myTransform);
				g2.setTransform(t);
				for (int i = 0; i < myLabels.length; i++) {
					myLabels[i].draw(g2);
				}
				g2.dispose();
			}
		};
	}
	
	/**
	 * Gets the hit line.
	 *
	 * @param frameNumber the frame number
	 * @return a Line2D
	 */
	private Line2D getHitLine(int frameNumber) {
		Line2D hitLine = hitLines.get(frameNumber);
		if (hitLine == null) {
			hitLine = new Line2D.Double();
			hitLines.put(frameNumber, hitLine);
		}
		return hitLine;
	}
	
	@Override
	protected Interactive findInteractive(TrackerPanel trackerPanel, Rectangle hitRect) {
		Interactive hit = null;
		int n = trackerPanel.getFrameNumber();
		if (getHitLine(n).intersects(hitRect)) {
			hit = getHandle();
		}
		return hit;
	}
	
	@Override
	protected void setHandleXY(double x, double y) {
		TPoint handle = getHandle();
		handle.setLocation(x, y);
		if (track.trackerPanel != null && track.trackerPanel.getSelectedPoint() == handle) {
			int n = track.trackerPanel.getFrameNumber();
			Point p = new Point(handle.getScreenPosition(track.trackerPanel));
			
			// find distance from tape, set ruler width
			double dist = getScreenDistanceToBase(p);
			boolean isLeft = isLeft(track.getStep(n).getPoints()[0],
	    		track.getStep(track.trackerPanel.getFrameNumber()).getPoints()[1],
	    		handle);
			
			setRulerSize(isLeft? 
					lineGap - dist: 
					dist - lineGap);
			
			// find distance from hit line end, set rulerSpacing
			Point2D end = getHitLine(n).getP1();
			dist = p.distance(end);
			if (previousDistFromLineEnd == null) {					
				previousDistFromLineEnd = dist;
				previousLineSpacing = rulerLineSpacing;
				previousAngle = Math.atan2(p.y - end.getY(), p.x - end.getX());
			}
			else {
				double delta = dist - previousDistFromLineEnd;
				// check for angle change to see if handle position is negative relative to ruler
				double angle = Math.atan2(p.y - end.getY(), p.x - end.getX());
				if (Math.abs(angle-previousAngle) > 1)
					delta = -delta;
				setLineSpacing(previousLineSpacing + (int) (delta / 3));
			}

			track.repaint();				
		}		
	}

}
