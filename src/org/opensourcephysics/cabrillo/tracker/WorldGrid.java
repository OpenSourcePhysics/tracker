/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;

/**
 * This draws a world grid on a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class WorldGrid implements Trackable {
	
	private static final float[] DASHED_LINE = new float[] {1, 8};
	private static final float[] DOTTED_LINE = new float[] {2, 2};
	private static Stroke dashed, dotted;
	private static int defaultAlpha = 128;
	private static Color defaultColor = new Color(128, 128, 128, defaultAlpha);
	
	ArrayList<Line2D> dashedLines = new ArrayList<Line2D>();
	ArrayList<Line2D> dottedLines = new ArrayList<Line2D>();
	TPoint[] viewCorners = {new TPoint(), new TPoint(), new TPoint(), new TPoint()};
	Point2D[] worldCorners = new Point2D[4];
	TPoint[] lineEnds = {new TPoint(), new TPoint()};
	double[] minMaxWorldValues = new double[4];
	int[] minMaxIndices = new int[4];
	boolean showMajorX=true, showMinorX=true, showMajorY=true, showMinorY=true;
	private int alpha = defaultAlpha;
	private Color lineColor = defaultColor;
	private boolean visible;

	/**
	 * Constructor.
	 *
	 * @param panel a TrackerPanel
	 */
	public WorldGrid() {
  	dashed = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,8,DASHED_LINE,0);
  	dotted = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,8,DOTTED_LINE,0);
	}
	
	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (!visible || (!showMajorX && !showMajorY)) return;
		Graphics2D g2 = (Graphics2D)g;
		TrackerPanel trackerPanel = (TrackerPanel)panel;
		// find world coordinates of corners of the visible trackerPanel view
		Rectangle rect = trackerPanel.getVisibleRect();
		for (int i=0; i<4; i++) {
			switch (i) {
				case 0: // top left
					viewCorners[i].setScreenPosition(rect.x, rect.y, trackerPanel);
					break;
				case 1: // top right
					viewCorners[i].setScreenPosition(rect.x+rect.width, rect.y, trackerPanel);
					break;
				case 2: // bottom left
					viewCorners[i].setScreenPosition(rect.x, rect.y+rect.height, trackerPanel);
					break;
				default: // bottom right
					viewCorners[i].setScreenPosition(rect.x+rect.width, rect.y+rect.height, trackerPanel);
			}
			worldCorners[i] = viewCorners[i].getWorldPosition(trackerPanel);
			if (i==0) {
				minMaxWorldValues[0] = minMaxWorldValues[1] = worldCorners[i].getX();
				minMaxWorldValues[2] = minMaxWorldValues[3] = worldCorners[i].getY();
			}
			else {
				minMaxWorldValues[0] = Math.min(minMaxWorldValues[0], worldCorners[i].getX()); // xMin
				minMaxWorldValues[1] = Math.max(minMaxWorldValues[1], worldCorners[i].getX()); // xMax
				minMaxWorldValues[2] = Math.min(minMaxWorldValues[2], worldCorners[i].getY()); // yMin
				minMaxWorldValues[3] = Math.max(minMaxWorldValues[3], worldCorners[i].getY()); // yMax				
			}
		}
		
		// determine a world delta between lines at least 30 pixels apart
		int lineCount = Math.min(60, rect.width/25);
		// make a first approximation
		double delta = (minMaxWorldValues[1]-minMaxWorldValues[0])/lineCount;
		
		// find power of ten
		double pow = 1;
		while (pow*10 < delta) pow *= 10;
		while (pow > delta) pow /= 10;

		// get "significand" and increase to nearest 2, 5 or 10
		double significand = delta/pow; // number between 1 and 10
		int minorSpacing = 10;
		int majorSpacing = 100;
		if (significand<2) {
			minorSpacing = 2;
			majorSpacing = 10;
		}
		else if (significand<5) {
			minorSpacing = 5;
			majorSpacing = 10;
		}
		
		// determine final value of delta
		delta = minorSpacing*pow;
		
		// determine which lines to draw
		for (int i=0; i<4; i++) {
			minMaxIndices[i] = (int)(minMaxWorldValues[i]/delta);
		}
		
		dashedLines.clear();
		dottedLines.clear();
		
		// create x-grid lines parallel to y-axis
		if (showMajorX) {
			for (int i=minMaxIndices[0]-1; i<minMaxIndices[1]+1; i++) {
				boolean isMajor = (i*minorSpacing)%majorSpacing==0;
				if (!isMajor && !showMinorX) 
					continue;
				ArrayList<Line2D> lines = isMajor? dottedLines: dashedLines;
				double x = i*delta;
				Line2D line = new Line2D.Double();
				lines.add(line);
				// set line end points to world positions (x, yMin) and (x, yMax)
				lineEnds[0].setWorldPosition(x, minMaxWorldValues[2], trackerPanel);
				lineEnds[1].setWorldPosition(x, minMaxWorldValues[3], trackerPanel);
				// set Line2D to screen coordinates
				line.setLine(lineEnds[0].getScreenPosition(trackerPanel), lineEnds[1].getScreenPosition(trackerPanel));
			}
		}
		
		// create y-grid lines parallel to x-axis
		if (showMajorY) {
			for (int i=minMaxIndices[2]-1; i<minMaxIndices[3]+1; i++) {
				boolean isMajor = (i*minorSpacing)%majorSpacing==0;
				if (!isMajor && !showMinorY) 
					continue;
				ArrayList<Line2D> lines = isMajor? dottedLines: dashedLines;
				double y = i*delta;
				Line2D line = new Line2D.Double();
				lines.add(line);
				// set line end points to world positions (xMin, y) and (xMax, y)
				lineEnds[0].setWorldPosition(minMaxWorldValues[0], y, trackerPanel);
				lineEnds[1].setWorldPosition(minMaxWorldValues[1], y, trackerPanel);
				// set Line2D to screen coordinates
				line.setLine(lineEnds[0].getScreenPosition(trackerPanel), lineEnds[1].getScreenPosition(trackerPanel));
			}
		}
		
		// prepare to draw lines
		// save original color and stroke
		Color color = g2.getColor();
		Stroke stroke = g2.getStroke();
		
		// set color and stroke for dashed lines
		g2.setPaint(lineColor);
		g2.setStroke(dashed);
		for (int i=0; i<dashedLines.size(); i++) {
	    g2.draw(dashedLines.get(i));
		}
		
		// set color and stroke for dotted lines
		g2.setStroke(dotted);
		for (int i=0; i<dottedLines.size(); i++) {
	    g2.draw(dottedLines.get(i));
		}
		
		// restore original color and stroke
		g2.setStroke(stroke);
		g2.setColor(color);
	}
	
	/**
	 * Gets the color.
	 *
	 * @return the line color
	 */
	public Color getColor() {
		return lineColor;
	}
	
	/**
	 * Sets the color.
	 *
	 * @param color the desired line color
	 */
	public void setColor(Color color) {
		if (color!=null) {
			lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
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
		setColor(lineColor);
	}
	
  /**
   * Sets the visibility of the axes.
   *
   * @param isVisible true if the axes are visible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Gets the visibility of the axes.
   *
   * @return true if the axes is drawn
   */
  public boolean isVisible() {
    return visible;
  }

	/**
	 * Determines if the color or alpha has been customized.
	 *
	 * @return true if a custom color or alpha has been set
	 */
	public boolean isCustom() {
		return !defaultColor.equals(lineColor);
	}
	
	/**
	 * Sets the visibility of the major x grid lines.
	 *
	 * @param visible true to display the major x grid lines
	 */
	public void setMajorXGridVisible(boolean visible) {
		showMajorX = visible;
	}

	/**
	 * Sets the visibility of the minor x grid lines.
	 *
	 * @param visible true to display the minor x grid lines
	 */
	public void setMinorXGridVisible(boolean visible) {
		showMinorX = visible;
	}

	/**
	 * Sets the visibility of the major y grid lines.
	 *
	 * @param visible true to display the major y grid lines
	 */
	public void setMajorYGridVisible(boolean visible) {
		showMajorY = visible;
	}

	/**
	 * Sets the visibility of the minor y grid lines.
	 *
	 * @param visible true to display the minor y grid lines
	 */
	public void setMinorYGridVisible(boolean visible) {
		showMinorY = visible;
	}
	
}
