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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

/**
 * This draws ruled angle lines and labels for a Protractor.
 *
 * @author Douglas Brown
 */
public class AngleRuler extends Ruler {
	
	/**
	 * Constructor.
	 * 
	 * @param protractor a Protractor
	 */
	public AngleRuler(Protractor protractor) {
		super(protractor);
	}
	
	@Override
	protected Mark getMark(TrackerPanel trackerPanel, int n) {
		if (trackerPanel.isWorldPanel())
			return null;
		refreshStrokes();
		format.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
		// get ends and length of the protractor base [vertex, end1]
		TPoint vertex = track.getStep(n).getPoints()[0];
		TPoint baseEnd = track.getStep(n).getPoints()[1];
		Point screenVertex = vertex.getScreenPosition(trackerPanel);
		double screenRadius = screenVertex.distance(baseEnd.getScreenPosition(trackerPanel));
		
		// set up transform to rotate around screen position of vertex
		double baseAngle = -vertex.angle(baseEnd);
		transform.setToRotation(-baseAngle, screenVertex.x, screenVertex.y);	
		
		boolean isDegrees = !track.tframe.isAnglesInRadians();

		// determine world delta between lines
		double delta = rulerLineSpacing / screenRadius; // radians
		if (isDegrees)
			delta *= 180 / Math.PI;
		int majorSpacing = 0;
		int halfSpacing = 0;
		
		if (isDegrees && delta > 0.5) {
			
			// set the line pattern based on delta significand
			int spacing = 
					delta <= 1? 1: 
					delta <= 2? 2:
					delta <= 5? 5: 
					delta <= 10? 10: 
					delta <= 15? 15: 
					30;

			switch(spacing) {
			case 1:
				majorSpacing = 10;
				halfSpacing = 5;			
				break;
			case 2:
				majorSpacing = 15;
				halfSpacing = 5;			
				break;
			case 5:
				majorSpacing = 18;
				halfSpacing = 2;			
				break;
			case 10:
				majorSpacing = 9;
				halfSpacing = 1;			
				break;
			case 15:
				majorSpacing = 3;
				halfSpacing = 1;			
				break;
			case 30:
				majorSpacing = 3;
				halfSpacing = 1;			
				break;
			}
			// set final value of delta
			delta = spacing;
		}
		else {
			// set the line pattern based on significand as for WorldRuler
			// find power of ten
			double power = 1;
			while (power * 10 < delta)
				power *= 10;
			while (power > delta)
				power /= 10;

			// get significand (number between 1 and 10)
			double significand = delta / power;
			// increase spacing to nearest 2, 5 or 10
			int spacing = significand <= 2? 2: significand <= 5? 5: 10;
			// set major and half spacing pattern
			majorSpacing = spacing == 2? 25: 10;
			halfSpacing = spacing == 5? 2: 5;			
			// set final value of delta
			delta = spacing * power;
		}
		
		// determine arm angle and create line from arm end to edge if needed
		Shape armLine = null;
		ProtractorStep step = (ProtractorStep) track.getStep(n);
		TPoint armEnd = step.getPoints()[2];
		double armRadius = screenVertex.distance(armEnd.getScreenPosition(trackerPanel));
		double armAngle = step.getProtractorAngle(false);
		double cos = Math.cos(armAngle);
		double sin = Math.sin(armAngle);
		if (armRadius < screenRadius) {
			armLine = new Line2D.Double( 
					screenVertex.x + armRadius * cos, 
					screenVertex.y - armRadius * sin, 
					screenVertex.x + screenRadius * cos, 
					screenVertex.y - screenRadius * sin);
		}
		
		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).clear();
		}
		labelMarks.clear();
		int inset = rulerSize > 0? insetPerLevel: -insetPerLevel;		
		double factor = FontSizer.getFactor();
		double formatMinValue = majorSpacing * delta;
		double labelRadius = screenRadius + factor * labelGap;
		
		// create radial lines and labels, then rotate about vertex
		double ang = isDegrees? 180: Math.PI;
		int lineCount = (int) Math.ceil(ang / delta);
		for (int i = 1; i < lineCount; i++) {
			int level = i % majorSpacing == 0? 0: i % halfSpacing == 0? 1: 2;
			double angleInRadians = isDegrees? i * delta * Math.PI / 180:  i * delta;
			ArrayList<Line2D> drawLines = lines.get(level);
			double lineLength = rulerSize - inset*level;			
			double inside = screenRadius - lineLength;
			cos = Math.cos(angleInRadians);
			sin = Math.sin(angleInRadians);
			
			Line2D line = new Line2D.Double();
			drawLines.add(line);
			line.setLine(screenVertex.x + inside * cos, screenVertex.y - inside * sin, 
					screenVertex.x + screenRadius * cos, screenVertex.y - screenRadius * sin);
			
			line = new Line2D.Double();
			drawLines.add(line);
			line.setLine(screenVertex.x + inside * cos, screenVertex.y + inside * sin, 
					screenVertex.x + screenRadius * cos, screenVertex.y + screenRadius * sin);
			
			// create the labels for major lines
			if (level == 0) {				
					String s = getFormattedValue(i * delta, formatMinValue);
					if (isDegrees)
						s += Tracker.DEGREES;
					Label label = new Label(s, screenVertex.x + labelRadius * cos, 
							screenVertex.y - labelRadius * sin);
					// if angle is not between 0 and pi add extra flip to label
					label.rotation = (Math.PI / 2 - angleInRadians) + 
							(isLabelFlipped(baseAngle + angleInRadians)? Math.PI: 0);
					labelMarks.add(label);
					s = getFormattedValue(-i * delta, formatMinValue);
					if (isDegrees)
						s += Tracker.DEGREES;
					label = new Label(s, screenVertex.x + labelRadius * cos, 
							screenVertex.y + labelRadius * sin);
					label.rotation =  angleInRadians + Math.PI / 2 + 
							(isLabelFlipped(baseAngle - angleInRadians)? Math.PI: 0);
					labelMarks.add(label);
			}					
		}
		
		// create the labelMark for 0 and 180 degrees
		String s = getFormattedValue(0, formatMinValue);
		if (isDegrees)
			s += Tracker.DEGREES;
		Label label = new Label(s, screenVertex.x + labelRadius, screenVertex.y);
		label.rotation = isLabelFlipped(baseAngle)? -Math.PI / 2: Math.PI / 2;
		labelMarks.add(label);
		if (isDegrees) {
			s = getFormattedValue(180, formatMinValue);
			s += Tracker.DEGREES;
			label = new Label(s, screenVertex.x - labelRadius, screenVertex.y);
			label.rotation = isLabelFlipped(baseAngle - Math.PI )? 
					Math.PI / 2: -Math.PI / 2;
			labelMarks.add(label);
		}
		
		// set up edge, extended base and vertex shapes		
		Line2D vertexLine = new Line2D.Double(screenVertex.x, 
				screenVertex.y - insetPerLevel / 2, 
				screenVertex.x, screenVertex.y + insetPerLevel / 2);
		Line2D base = new Line2D.Double(screenVertex.x, screenVertex.y, 
				screenVertex.x - screenRadius, screenVertex.y);
		Arc2D arc = new Arc2D.Double();
		arc.setArcByCenter(screenVertex.x, screenVertex.y, screenRadius, 
				0, 360, Arc2D.OPEN);
		Stroke str = strokes[0];
		MultiShape arcMultiShape = armLine != null? 
				new MultiShape(arc, base, vertexLine, armLine).
					andStroke(str, str, str, dashedStroke).transform(transform):
				new MultiShape(arc, base, vertexLine).
					andStroke(str, str, str).transform(transform);

		// assemble multishapes
		for (int i = 0; i < lines.size(); i++) {
			ArrayList<Line2D> drawLines = lines.get(i);
			Line2D[] lineArray = drawLines.toArray(new Line2D[drawLines.size()]);
			Stroke[] lineStrokes = new Stroke[lineArray.length];
			Arrays.fill(lineStrokes, strokes[i]);
			multiShapes[i] = new MultiShape(lineArray).
					andStroke(lineStrokes).transform(transform);			
		}

		MultiShape[] myShapes = Arrays.copyOf(multiShapes, multiShapes.length);
		AffineTransform myTransform = new AffineTransform(transform);
		Label[] myLabels = labelMarks.toArray(new Label[labelMarks.size()]);
		// return the mark
		return new Mark() {
			@Override
			public void draw(Graphics2D g, boolean highlighted) {
				Graphics2D g2 = (Graphics2D) g.create();
				if (OSPRuntime.setRenderingHints)
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
							RenderingHints.VALUE_ANTIALIAS_ON);
				for (int i = 0; i < myShapes.length; i++) {
					g2.setPaint(colors[i]);
					myShapes[i].draw(g2);
				}
				g2.setPaint(colors[0]);
				arcMultiShape.draw(g2);

				// draw labels
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
	
	private boolean isLabelFlipped(double angle) {
		angle = angle < -Math.PI? angle + 2 * Math.PI: angle > Math.PI? angle - 2 * Math.PI: angle;
		double offset = 0.1; // about 6 degrees
		return (angle < -offset && angle > -Math.PI + offset);
	}
	
}
