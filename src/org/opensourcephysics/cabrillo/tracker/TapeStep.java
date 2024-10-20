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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import org.opensourcephysics.display.OSPRuntime.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a Step for a TapeMeasure. It is used for measuring distances and
 * angles and for setting the scale and angle of an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TapeStep extends Step {

	// static fields
	protected static TPoint endPoint1 = new TPoint(); // used for layout position
	protected static TPoint endPoint2 = new TPoint(); // used for layout position

	// instance fields
	protected TapeMeasure tape;
	protected TPoint end1, end2, middle;
	protected Handle handle;
	protected Rotator rotator1, rotator2;
	protected double worldLength;
	protected double xAxisToTapeAngle, tapeAngle;
	protected boolean endsEnabled = true;
	protected boolean drawLayout, drawLayoutBounds;
	protected boolean adjustingTips;
	protected Map<Integer, Shape> panelEnd1Shapes = new HashMap<Integer, Shape>();
	protected Map<Integer, Shape> panelEnd2Shapes = new HashMap<Integer, Shape>();
	protected Map<Integer, Shape> panelShaftShapes = new HashMap<Integer, Shape>();
	protected Map<Integer, Shape[]> panelRotatorShapes = new HashMap<Integer, Shape[]>();
	protected Map<Integer, TextLayout> panelTextLayouts = new HashMap<Integer, TextLayout>();
	protected Map<Integer, Rectangle> panelLayoutBounds = new HashMap<Integer, Rectangle>();
  protected MultiShape[] rotatorDrawShapes = new MultiShape[2];
  protected Shape selectedShape;

	/**
	 * Constructs a TapeStep with specified end point coordinates in image space.
	 *
	 * @param track the track
	 * @param n     the frame number
	 * @param x1    the x coordinate of end 1
	 * @param y1    the y coordinate of end 1
	 * @param x2    the x coordinate of end 2
	 * @param y2    the y coordinate of end 2
	 */
	public TapeStep(TapeMeasure track, int n, double x1, double y1, double x2, double y2) {
		super(track, n);
		tape = track;
		end1 = new Tip(x1, y1);
		end1.setTrackEditTrigger(true);
		end2 = new Tip(x2, y2);
		end2.setTrackEditTrigger(true);
		middle = new TPoint(x1, y1); // used for layout position
		rotator1 = new Rotator();
		rotator2 = new Rotator();
		handle = new Handle((x1 + x2) / 2, (y1 + y2) / 2);
		handle.setTrackEditTrigger(true);
		points = new TPoint[] { end1, end2, handle, middle, rotator1, rotator2 };
		screenPoints = new Point[getLength()];
	}

	/**
	 * Gets end 1.
	 *
	 * @return end 1
	 */
	public TPoint getEnd1() {
		return end1;
	}

	/**
	 * Gets end 2.
	 *
	 * @return end 2
	 */
	public TPoint getEnd2() {
		return end2;
	}

	/**
	 * Gets the handle.
	 *
	 * @return the handle
	 */
	public TPoint getHandle() {
		return handle;
	}

	/**
	 * Enables and disables the interactivity of the ends.
	 *
	 * @param enabled <code>true</code> to enable the ends
	 */
	public void setEndsEnabled(boolean enabled) {
		endsEnabled = enabled;
	}

	/**
	 * Gets whether the ends are enabled.
	 *
	 * @return <code>true</code> if the ends are enabled
	 */
	public boolean isEndsEnabled() {
		return endsEnabled;
	}

	/**
	 * Overrides Step setFootprint method.
	 *
	 * @param footprint the footprint
	 */
	@Override
	public void setFootprint(Footprint footprint) {
		if (footprint.getLength() >= 2)
			super.setFootprint(footprint);
	}

	/**
	 * Overrides Step findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position
	 * @param ypix  the y pixel position
	 * @return the Interactive that is hit, or null
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {

		TrackerPanel trackerPanel = (TrackerPanel) panel;
		setHitRectCenter(xpix, ypix);
		boolean drawOutline = false;
    drawLayout = false;

		Shape hitShape;
		Interactive hit = null;
		// look for ends
		if (endsEnabled) {
			hitShape = panelEnd1Shapes.get(trackerPanel.getID());
			if (hitShape != null && hitShape.intersects(hitRect))
				hit = end1;
			hitShape = panelEnd2Shapes.get(trackerPanel.getID());
			if (hit == null && hitShape != null && hitShape.intersects(hitRect))
				hit = end2;
		}
		// look for shaft
		hitShape = panelShaftShapes.get(trackerPanel.getID());
		if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
			hit = handle;
			drawLayout = true;
		}
		// look for rotator hit
		Shape[] rotatorHitShapes = panelRotatorShapes.get(trackerPanel.getID());
		if (hit == null && rotatorHitShapes != null) {
			if (rotatorHitShapes[0].intersects(hitRect) && !end1.isAttached()) {
				hit = rotator1;
			} else if (rotatorHitShapes[1].intersects(hitRect) && !end2.isAttached()) {
				hit = rotator2;
			}
		}
		if (hit == null && selectedShape != null && selectedShape.intersects(hitRect)) {
			if (trackerPanel.getSelectedPoint() == rotator1 && !end1.isAttached())
				hit = rotator1;
			else if (trackerPanel.getSelectedPoint() == rotator2 && !end2.isAttached())
				hit = rotator2;
		}
		// create rotatorShape if hit is rotator
		if (hit == null) {
			// clear rotatorShape shape when no longer needed
			if (rotatorDrawShapes[0] != null && trackerPanel.getSelectedPoint() != rotator1) {
				rotatorDrawShapes[0] = null;
			}
			if (rotatorDrawShapes[1] != null && trackerPanel.getSelectedPoint() != rotator2) {
				rotatorDrawShapes[1] = null;
			}
			Rectangle layoutRect = panelLayoutBounds.get(trackerPanel.getID());
			if (layoutRect != null && layoutRect.intersects(hitRect)) {
				drawOutline = true;
				hit = tape;
				drawLayout = true;
			}
			if (hit == null && tape.ruler != null && tape.ruler.isVisible()) {
				hit = tape.ruler.findInteractive(trackerPanel, hitRect);
			}
		} else if ((hit == rotator1 || hit == rotator2) && trackerPanel.getSelectedPoint() != hit
				&& footprint != null) {
			((Rotator) hit).setScreenCoords(xpix, ypix);
			int index = (hit == rotator1 ? 0 : 1);
			rotatorDrawShapes[index] = ((LineFootprint) footprint).getRotatorShape(
					middle.getScreenPosition(trackerPanel), getRotatorLocation(index, trackerPanel), null);
		}
		if (drawOutline != drawLayoutBounds) {
			drawLayoutBounds = drawOutline;
		}

		// check for attached ends which cannot be dragged
		if (end1.isAttached() && (hit == end1 || hit == rotator1)
				|| end2.isAttached() && (hit == end2 || hit == rotator2)) // BH!! 2021.09.11 was rotator1
			return null;

		return hit;
	}

	/**
	 * Overrides Step draw method.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		// draw the tape
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Graphics2D g = (Graphics2D) _g;
		getMark(trackerPanel).draw(g, false);
		Paint gpaint = g.getPaint();
		g.setPaint(footprint.getColor());
		// draw the text layout if tape is selected unless editing
		if (!tape.editing) {
			if (drawLayout || trackerPanel.getSelectedTrack() == tape) {
				TextLayout layout = panelTextLayouts.get(trackerPanel.getID());
				Rectangle bounds = panelLayoutBounds.get(trackerPanel.getID());
				Font gfont = g.getFont();
				g.setFont(TFrame.textLayoutFont);
				layout.draw(g, bounds.x, bounds.y + bounds.height);
				g.setFont(gfont);
				if (drawLayoutBounds && tape.isFieldsEnabled()) {
					g.drawRect(bounds.x - 2, bounds.y - 3, bounds.width + 6, bounds.height + 5);
				}
			}
		}
		g.setPaint(gpaint);
	}

	/**
	 * Gets the default point. The default point is the point initially selected
	 * when the step is created. Overrides step getDefaultPoint method.
	 *
	 * @return the default TPoint
	 */
	@Override
	public TPoint getDefaultPoint() {
		if (tape.isIncomplete)
			return points[1];
		TPoint p = tape.tp.getSelectedPoint();
		if (p == points[0])
			return points[0];
		if (p == points[1])
			return points[1];
		return points[defaultIndex];
	}

	/**
	 * Overrides Step getMark method.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the mark
	 */
	@Override
	protected Mark getMark(TrackerPanel trackerPanel) {
		Mark mark = panelMarks.get(trackerPanel.getID());
		if (mark == null) {
      boolean isWorldView = trackerPanel.isWorldPanel();
			// adjust tips if stick mode
			if (tape.isStickMode() && !tape.isIncomplete) {
				adjustTipsToLength();
			}
			TPoint selection = trackerPanel.getSelectedPoint();
			
			// create mark to draw ruler
			Mark rulerMark = tape.ruler != null && tape.ruler.isVisible()? tape.ruler.getMark(trackerPanel, n): null;

			// get screen points
			Point p = null;
			for (int i = 0; i < points.length; i++) {
				screenPoints[i] = points[i].getScreenPosition(trackerPanel);
				if (selection == points[i])
					p = screenPoints[i];
			}
			if (p == null && tape.ruler != null && selection == tape.ruler.getHandle()) {
				p = selection.getScreenPosition(trackerPanel);
			}
      // refresh rotatorShape
      if (selection==rotator1 || selection==rotator2) {  
      	int index = selection==rotator1? 0: 1;
      	rotatorDrawShapes[index] = ((LineFootprint)footprint).getRotatorShape(
      			screenPoints[3], // middle
      			screenPoints[index], // end
      			screenPoints[4 + index]); // rotator
      }

			// create footprint mark
			Mark tapeMark = footprint.getMark(screenPoints);
			
      if (!isWorldView) {
			  if (p != null) {
			    transform.setToTranslation(p.x, p.y);
			    int scale = FontSizer.getIntegerFactor();
			    if (scale>1) {
			    	transform.scale(scale, scale);
			    }
			    selectedShape = transform.createTransformedShape(selectionShape);
			  }
			  else
			  	selectedShape = null;
      }
		 
			mark = new Mark() {
				@Override
				public void draw(Graphics2D g, boolean highlighted) {
					Paint gpaint = g.getPaint();
					Stroke gstroke = g.getStroke();
					tapeMark.draw(g, false);
					if (rulerMark != null) {
						rulerMark.draw(g, false);
					}
		      if (selectedShape != null) {
						g.setPaint(footprint.getColor());
						g.setStroke(selectionStroke);
						g.draw(selectedShape);
		      }
					for (int i = 0; i < rotatorDrawShapes.length; i++) {
						if (rotatorDrawShapes[i] != null && !tape.isLocked()) {
							g.setColor(tape.getColor());
							rotatorDrawShapes[i].draw(g);
						}
					}
					g.setStroke(gstroke);
					g.setPaint(gpaint);
				}
			};
			
			panelMarks.put(trackerPanel.getID(), mark);

			// get new hit shapes
			Shape[] shapes = footprint.getHitShapes();
			panelEnd1Shapes.put(trackerPanel.getID(), shapes[0]);
			panelEnd2Shapes.put(trackerPanel.getID(), shapes[1]);
			panelShaftShapes.put(trackerPanel.getID(), shapes[2]);
			if (shapes.length > 4 && shapes[3] != null && shapes[4] != null) {
				panelRotatorShapes.put(trackerPanel.getID(), new Shape[] {shapes[3], shapes[4]});
			}
			
			// get new text layout
			double tapeLength = getTapeLength(!tape.isStickMode() || tape.isIncomplete);
			if (tape.calibrationLength != null) {
				tapeLength = tape.calibrationLength;
			}
			String s = tape.getFormattedLength(tapeLength);
			s += trackerPanel.getUnits(tape, TapeMeasure.dataVariables[1]);

			TextLayout layout = new TextLayout(s, TFrame.textLayoutFont);
			panelTextLayouts.put(trackerPanel.getID(), layout);
			// get layout position (bottom left corner of text)
			Rectangle2D rect = layout.getBounds();
			p = getLayoutPosition(trackerPanel, rect);
			Rectangle bounds = panelLayoutBounds.get(trackerPanel.getID());
			if (bounds == null) {
				bounds = new Rectangle();
				panelLayoutBounds.put(trackerPanel.getID(), bounds);
			}
			// set bounds (top left corner and size)
			bounds.setRect(p.x, p.y - rect.getHeight(), rect.getWidth(), rect.getHeight());
		}
		return mark;
	}

	/**
	 * Gets the scaled world length of this tape, with side effect of updating angle
	 * and length fields.
	 * 
	 * @param fromEnds true if calculated from the current tip positions
	 * @return the length in world units
	 */
	public double getTapeLength(boolean fromEnds) {
		double scaleX = 1;
		double scaleY = 1;
		double axisTiltAngle = 0;
		if (tape.tp != null) {
			scaleX = tape.tp.getCoords().getScaleX(n);
			scaleY = tape.tp.getCoords().getScaleY(n);
			axisTiltAngle = tape.tp.getCoords().getAngle(n);
		}
		double dx = (end2.getX() - end1.getX()) / scaleX;
		double dy = (end1.getY() - end2.getY()) / scaleY;
		tapeAngle = Math.atan2(dy, dx);
		xAxisToTapeAngle = tapeAngle - axisTiltAngle;
		if (Double.isNaN(xAxisToTapeAngle)) {
			xAxisToTapeAngle = 0;
		}
		tape.angleField.setValue(xAxisToTapeAngle);
		double length = fromEnds ? Math.sqrt(dx * dx + dy * dy) : worldLength;
		tape.magField.setValue(length);
		return length;
	}

	/**
	 * Gets the world angle of this tape. Call this method only after
	 * getTapeLength(), which does the real work.
	 *
	 * @return the angle relative to the positive x-axis
	 */
	public double getTapeAngle() {
		return xAxisToTapeAngle;
	}

	/**
	 * Sets the world length of this tape and posts an undoable edit.
	 *
	 * @param length the length in world units
	 */
	public void setTapeLength(double length) {
		if (tape.isLocked() || tape.tp == null)
			return;
		length = Math.abs(length);
		length = Math.max(length, TapeMeasure.MIN_LENGTH);
		double factor = getTapeLength(!tape.isStickMode()) / length;
		if (factor == 1 || factor == 0 || Double.isInfinite(factor) || Double.isNaN(factor))
			return;

		XMLControl trackControl = new XMLControlElement(tape);
		if (tape.isReadOnly()) {
//      tape.lengthKeyFrames.add(n);
			worldLength = length;
			adjustTipsToLength();
			tape.repaintStep(this);
			Undo.postTrackEdit(tape, trackControl);
			return;
		}

		if (tape.isStickMode()) {
			if (tape.isFixedLength()) {
				TapeStep step = (TapeStep) tape.steps.getStep(0);
				step.worldLength = length;
			} else {
				tape.lengthKeyFrames.add(n);
				worldLength = length;
			}
		}
		ImageCoordSystem coords = tape.tp.getCoords();
		double scaleX = factor * coords.getScaleX(n);
		double scaleY = factor * coords.getScaleY(n);
		XMLControl coordsControl = new XMLControlElement(tape.tp.getCoords());
		tape.isStepChangingScale = true;
		coords.setScaleXY(n, scaleX, scaleY);
		tape.isStepChangingScale = false;
		if (tape.isStickMode()) {
			Undo.postTrackAndCoordsEdit(tape, trackControl, coordsControl);
		} else {
			Undo.postCoordsEdit(tape.tp, coordsControl);
		}
		erase();
	}

	/**
	 * Sets the world angle of this tape.
	 *
	 * @param theta the angle in radians
	 */
	public void setTapeAngle(double theta) {
		if (tape.isLocked() || tape.tp == null)
			return;

		if (tape.isReadOnly()) {
			// change tape angle, leave coordinate system alone
			XMLControl trackControl = new XMLControlElement(tape);
			xAxisToTapeAngle = theta;
			adjustTipsToAngle(null);
			tape.repaintStep(this);
			Undo.postTrackEdit(tape, trackControl);
			return;
		}
		// set coords angle without changing tape
		double dTheta = theta - xAxisToTapeAngle;
		ImageCoordSystem coords = tape.tp.getCoords();
		XMLControl state = new XMLControlElement(coords);
		double angle = coords.getAngle(n);
		coords.setAngle(n, angle - dTheta);
		Undo.postCoordsEdit(tape.tp, state);
		xAxisToTapeAngle = theta;
	}

	/**
	 * Sets the world angle of this tape.
	 *
	 * @param theta the angle in radians
	 * @param p the axis of rotate
	 */
	private void setTapeAngle(double theta, TPoint p) {
		if (tape.isLocked() || tape.tp == null)
			return;
		// change tape angle
		xAxisToTapeAngle = theta;
		adjustTipsToAngle(p);
		tape.repaintStep(this);
		return;
	}

	/**
	 * Clones this Step.
	 *
	 * @return a clone of this step
	 */
	@Override
	public Object clone() {
		TapeStep step = (TapeStep) super.clone();
		if (step != null) {
			step.points[0] = step.end1 = step.new Tip(end1.getX(), end1.getY());
			step.points[1] = step.end2 = step.new Tip(end2.getX(), end2.getY());
			step.points[2] = step.handle = step.new Handle(handle.getX(), handle.getY());
			step.points[3] = step.middle = new TPoint(middle.getX(), middle.getY());
			step.points[4] = step.rotator1 = step.new Rotator();
			step.points[5] = step.rotator2 = step.new Rotator();
			step.end1.setTrackEditTrigger(true);
			step.end2.setTrackEditTrigger(true);
			step.handle.setTrackEditTrigger(true);
			step.panelEnd1Shapes = new HashMap<Integer, Shape>();
			step.panelEnd2Shapes = new HashMap<Integer, Shape>();
			step.panelShaftShapes = new HashMap<Integer, Shape>();
			step.panelRotatorShapes = new HashMap<Integer, Shape[]>();
			step.panelTextLayouts = new HashMap<Integer, TextLayout>();
			step.panelLayoutBounds = new HashMap<Integer, Rectangle>();
			step.worldLength = worldLength;
		}
		return step;
	}

	/**
	 * Returns a String describing this.
	 *
	 * @return a descriptive string
	 */
	@Override
	public String toString() {
		return "TapeStep " + n //$NON-NLS-1$
				+ " [" + format.format(end1.x) //$NON-NLS-1$
				+ ", " + format.format(end1.y) //$NON-NLS-1$
				+ ", " + format.format(end2.x) //$NON-NLS-1$
				+ ", " + format.format(end2.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets the step length.
	 *
	 * @return the length of the points array
	 */
	public static int getLength() {
		return 6;
	}

	// __________________________ private and protected methods
	// __________________________

	/**
	 * Moves the tips of the tape to display current worldLength.
	 */
	protected void adjustTipsToLength() {
		if (adjustingTips)
			return;
		adjustingTips = true;
		
		double sin = end1.sin(end2);
		double cos = end1.cos(end2);
		double d = end1.distance(end2);
		double factor = worldLength / getTapeLength(true);

		// special case: d==0 must be corrected
		if (d == 0) {
			sin = 0;
			cos = 1;
			d = 1;
			double scaleX = tape.tp.getCoords().getScaleX(n);
			factor = worldLength * scaleX;
		}

		// if either end is selected or attached to a point mass, keep that end fixed
		TPoint p = tape.tp.getSelectedPoint();
		if (end1.isAttached())
			p = end1;
		else if (end2.isAttached())
			p = end2;

		if (p instanceof Tip) {
			// keep end fixed
			if (p == end1) {
				double x = end1.getX() + cos * d * factor;
				double y = end1.getY() - sin * d * factor;
				end2.setLocation(x, y);
			} else {
				double x = end2.getX() - cos * d * factor;
				double y = end2.getY() + sin * d * factor;
				end1.setLocation(x, y);
			}
		} else if (p == handle) {
			// be sure handle is on line between ends
			Point screenPt = handle.getScreenPosition(tape.tp);
			handle.setPositionOnLine(screenPt.x, screenPt.y, tape.tp);
			// keep handle fixed
			d = handle.distance(end1);
			if (d == 0)
				d = 0.5; // special case
			double x = handle.getX() - cos * d * factor;
			double y = handle.getY() + sin * d * factor;
			end1.setLocation(x, y);
			d = handle.distance(end2);
			if (d == 0)
				d = 0.5; // special case
			x = handle.getX() + cos * d * factor;
			y = handle.getY() - sin * d * factor;
			end2.setLocation(x, y);
		} else {
			// keep middle fixed
			middle.center(end1, end2);
			double x1 = middle.getX() - cos * d * factor / 2;
			double y1 = middle.getY() + sin * d * factor / 2;
			double x2 = middle.getX() + cos * d * factor / 2;
			double y2 = middle.getY() - sin * d * factor / 2;
			end1.setLocation(x1, y1);
			end2.setLocation(x2, y2);
		}
		adjustingTips = false;
	}

	/**
	 * Rotates the tips about a TPoint axis to display current xAxisToTapeAngle.
	 * If the TPoint is null the axis is determined from selection and attachment status.
	 * 
	 * @param p the TPoint, may be null
	 */
	protected void adjustTipsToAngle(TPoint p) {
		if (adjustingTips)
			return;
		if (end1.isAttached() && end2.isAttached())
			return;
		adjustingTips = true;
		// if either end attached to a point mass, rotate about that end
		if (end1.isAttached())
			p = end1;
		if (end2.isAttached())
			p = end2;
		if (p == null)
			p = tape.tp.getSelectedPoint();
		
		double axisTiltAngle = tape.tp.getCoords().getAngle(n);
		tapeAngle = xAxisToTapeAngle + axisTiltAngle;
		double sin = Math.sin(tapeAngle);
		double cos = Math.cos(tapeAngle);
		double d = end1.distance(end2);

		// rotate about an end
		if (p == end1) {
			double x = end1.getX() + cos * d;
			double y = end1.getY() - sin * d;
			end2.setLocation(x, y);
			repaint();
		} 
		else if (p == end2) {
			double x = end2.getX() - cos * d;
			double y = end2.getY() + sin * d;
			end1.setLocation(x, y);
		}
		else if (p == handle || p == rotator1 || p == rotator2) {
			
			// rotate about p
			double d1 = p.distance(end1);
			double d2 = p.distance(end2);
			
			if (d1 <= d && d2 <= d) {
				end1.setLocation(p.getX() - cos * d1, p.getY() + sin * d1);
				end2.setLocation(p.getX() + cos * d2, p.getY() - sin * d2);
			}
			else if (d1 > d) {
				end1.setLocation(p.getX() - cos * d1, p.getY() + sin * d1);
				end2.setLocation(p.getX() - cos * d2, p.getY() + sin * d2);
			}
			else {
				end1.setLocation(p.getX() + cos * d1, p.getY() - sin * d1);
				end2.setLocation(p.getX() + cos * d2, p.getY() - sin * d2);
			}
		} 
		else {
			// rotate about the middle
			middle.center(end1, end2);
			double x1 = middle.getX() - cos * d / 2;
			double y1 = middle.getY() + sin * d / 2;
			double x2 = middle.getX() + cos * d / 2;
			double y2 = middle.getY() - sin * d / 2;
			end1.setLocation(x1, y1);
			end2.setLocation(x2, y2);
		}
		adjustingTips = false;
	}

	/**
	 * Gets TextLayout screen position. Unlike most positions, this one refers to
	 * the lower left corner of the text.
	 *
	 * @param trackerPanel the tracker panel
	 * @param bounds TODO
	 * @return the screen position point
	 */
	private Point getLayoutPosition(TrackerPanel trackerPanel, Rectangle2D bounds) {
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		endPoint1.setLocation(end1);
		endPoint2.setLocation(end2);
		// the following code is to determine the position on a world view
		if (!trackerPanel.isDrawingInImageSpace()) {
			AffineTransform at = trackerPanel.getCoords().getToWorldTransform(n);
			at.transform(endPoint1, endPoint1);
			endPoint1.y = -endPoint1.y;
			at.transform(endPoint2, endPoint2);
			endPoint2.y = -endPoint2.y;
		}
		double cos = endPoint1.cos(endPoint2);
		double sin = endPoint1.sin(endPoint2);
		double halfwsin = w * sin / 2;
		double halfhcos = h * cos / 2;
		double d = Math.sqrt((halfwsin*halfwsin) + (halfhcos*halfhcos)) + 8; 
		// draw relative to center of tape
		middle.center(end1, end2);
		Point p = middle.getScreenPosition(trackerPanel);
		if (tape.ruler != null && tape.ruler.isVisible() && tape.ruler.getRulerSize() > 0)
		// draw below tape
			p.setLocation((int) (p.x + d * sin - w / 2), (int) (p.y + d * cos + h / 2));
		else
		// draw above tape
			p.setLocation((int) (p.x - d * sin - w / 2), (int) (p.y - d * cos + h / 2));
		return p;
	}
	
	private Point getRotatorLocation(int i, TrackerPanel trackerPanel) {
		Shape[] rotatorHitShapes = panelRotatorShapes.get(trackerPanel.getID());		
		Rectangle bounds = rotatorHitShapes[i].getBounds();
		return new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());
	}

	// ______________________ inner Handle class ________________________

	class Handle extends Step.Handle {

		/**
		 * Constructs a Handle with specified image coordinates.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public Handle(double x, double y) {
			super(x, y);
		}

		/**
		 * Overrides TPoint setXY method to move tip and tail.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		@Override
		public void setXY(double x, double y) {
			if (getTrack().locked)
				return;
    	if (end1.isAttached() || end2.isAttached())
	      return;
			double dx = x - getX();
			double dy = y - getY();
			setLocation(x, y);

			if (tape.isFixedPosition()) {
				TapeStep step = (TapeStep) tape.steps.getStep(0);
				step.end1.setLocation(end1.getX() + dx, end1.getY() + dy);
				step.end2.setLocation(end2.getX() + dx, end2.getY() + dy);
				step.erase();
				tape.refreshStep(TapeStep.this); // sets properties of this step
			} else {
				end1.setLocation(end1.getX() + dx, end1.getY() + dy);
				end2.setLocation(end2.getX() + dx, end2.getY() + dy);
				tape.keyFrames.add(n);
			}
			
			repaint();
		}

		/**
		 * Overrides TPoint getFrameNumber method.
		 *
		 * @param vidPanel the video panel drawing the step
		 * @return the containing TapeStep frame number
		 */
		@Override
		public int getFrameNumber(VideoPanel vidPanel) {
			return n;
		}

		/**
		 * Sets the position of this handle on the line nearest the specified screen
		 * position.
		 *
		 * @param xScreen      the x screen position
		 * @param yScreen      the y screen position
		 * @param trackerPanel the trackerPanel drawing this step
		 */
		@Override
		public void setPositionOnLine(int xScreen, int yScreen, TrackerPanel trackerPanel) {
			setPositionOnLine(xScreen, yScreen, trackerPanel, end1, end2);
			repaint();
		}

	}

	// ______________________ inner Tip class ________________________

	class Tip extends TPoint {

		/**
		 * Constructs a Tip with specified image coordinates.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		public Tip(double x, double y) {
			super(x, y);
		}

		@Override
		public void setXY(double x, double y) {
			if (getTrack().locked)
				return;
			if (tape.isStickMode() && isAdjusting()) {
				prevX = x;
				prevY = y;
			}

			if (tape.isFixedPosition()) {
				// first set properties of step 0
				TapeStep step = (TapeStep) tape.steps.getStep(0);
				if (this == end1) {
					step.end1.setLocation(x, y);
					step.end2.setLocation(end2);
				} else {
					step.end2.setLocation(x, y);
					step.end1.setLocation(end1);
				}
				step.erase();
				tape.refreshStep(TapeStep.this); // sets properties of this step
			} else {
				setLocation(x, y);
				tape.keyFrames.add(n);
			}

			if (tape.isStickMode() && worldLength > 0) {
				ImageCoordSystem coords = tape.tp.getCoords();
				coords.setAdjusting(isAdjusting());
				double newLength = getTapeLength(true); // newly calculated
				double factor = newLength / getTapeLength(false); // current worldLength
				double scaleX = factor * coords.getScaleX(n);
				double scaleY = factor * coords.getScaleY(n);
				tape.isStepChangingScale = true;
				tape.tp.getCoords().setScaleXY(n, scaleX, scaleY);
				tape.isStepChangingScale = false;
				// refresh other 
			}
			tape.invalidateData(tape);
			repaint();
		}

		@Override
		public int getFrameNumber(VideoPanel vidPanel) {
			return n;
		}

		@Override
		public void setAdjusting(boolean adjusting, MouseEvent e) {
			boolean wasAdjusting = isAdjusting();
			if (tape.isStickMode()) {
				super.setAdjusting(adjusting, e);
				if (wasAdjusting && !adjusting && !java.lang.Double.isNaN(prevX)) {
					setXY(prevX, prevY);
				}
			} else
				super.setAdjusting(adjusting, e);
			
			if (wasAdjusting && !adjusting) {
	      if (tape.isFixedPosition())
	      	tape.fireStepsChanged();
	      else
	      	tape.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
			}
		}

		@Override
		public boolean isCoordsEditTrigger() {
			return tape.isStickMode();
		}
		
	}

	// ______________________ inner Rotator class ________________________

	class Rotator extends TPoint {
		
  	TPoint pt = new TPoint(); // for setting screen position

		/**
		 * Constructor
		 */
		public Rotator() {
			super();
      setTrackEditTrigger(true);
		}

		@Override
		public void setXY(double x, double y) {
			if (getTrack().locked)
				return;
//			TapeStep step = tape.isFixedPosition()? (TapeStep) tape.steps.getStep(0): TapeStep.this;
			setLocation(x, y);
			if (tape.isFixedPosition()) {
				// first set properties of step 0
				TapeStep step = (TapeStep) tape.steps.getStep(0);
				Rotator rotator = this == rotator1? step.rotator1: step.rotator2;
				rotator.setLocation(x, y);
				double theta = this == rotator1?  rotator.angle(step.end2): step.end1.angle(rotator);
				theta += tape.tp.getCoords().getAngle(0);
				step.setTapeAngle(-theta, this == rotator1? step.end2: step.end1);
				step.erase();
//				tape.refreshStep(TapeStep.this); // sets properties of this step
			} else {
				double theta = this == rotator1?  this.angle(end2): end1.angle(this);
				theta += tape.tp.getCoords().getAngle(n);
				setTapeAngle(-theta, this == rotator1? end2: end1);
				tape.keyFrames.add(n);
			}
			
			erase();
			tape.invalidateData(tape);
		}

		@Override
		public int getFrameNumber(VideoPanel vidPanel) {
			return n;
		}

		@Override
		public void setAdjusting(boolean adjusting, MouseEvent e) {
			boolean wasAdjusting = isAdjusting();
			super.setAdjusting(adjusting, e);			
			if (wasAdjusting && !adjusting) {
	      if (tape.isFixedPosition())
	      	tape.fireStepsChanged();
	      else
	      	tape.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
			}
		}

		/**
		 * Sets screen coordinates without using setXY().
		 *
		 * @param x
		 * @param y
		 */
    protected void setScreenCoords(int x, int y) {
    	pt.setScreenPosition(x, y, tape.tp);
    	this.setLocation(pt);
    }
    
	}

	/**
	 * Returns an ObjectLoader to save and load data for this class.
	 *
	 * @return the object loader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load data for this class.
	 */
	static class Loader implements XML.ObjectLoader {

		/**
		 * Saves an object's data to an XMLControl.
		 *
		 * @param control the control to save to
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TapeStep step = (TapeStep) obj;
			double[] data = new double[] { step.getEnd1().x, step.getEnd1().y, step.getEnd2().x, step.getEnd2().y };
			control.setValue("end_positions", data); //$NON-NLS-1$
			control.setValue("worldlength", step.worldLength); //$NON-NLS-1$
		}

		/**
		 * Creates a new object with data from an XMLControl.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			// this loader is not intended to be used to create new steps,
			// but only for undo/redo step edits.
			return null;
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			TapeStep step = (TapeStep) obj;
			double[] data = (double[]) control.getObject("end_positions"); //$NON-NLS-1$
			step.getEnd1().setLocation(data[0], data[1]);
			step.getEnd2().setLocation(data[2], data[3]);
			step.worldLength = control.getDouble("worldlength"); //$NON-NLS-1$
			return obj;
		}
	}
}
