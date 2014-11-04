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

import java.util.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * This is a Step for a TapeMeasure. It is used for measuring
 * distances and angles and for setting the scale and angle of 
 * an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class TapeStep extends Step {

  // static fields
	protected static TPoint endPoint1 = new TPoint(); // used for layout position
  protected static TPoint endPoint2 = new TPoint(); // used for layout position

  // instance fields
  protected TapeMeasure tape;
  protected TPoint end1;
  protected TPoint end2;
  protected TPoint middle;
  protected Handle handle;
  protected double worldLength;
  protected double xAxisToTapeAngle, tapeAngle;
  protected boolean endsEnabled = true;
  protected boolean drawLayoutBounds;
  protected Map<TrackerPanel, Shape> end1Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> end2Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> shaftShapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, TextLayout> textLayouts = new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layoutBounds = new HashMap<TrackerPanel, Rectangle>();

  /**
   * Constructs a TapeStep with specified end point coordinates in image space.
   *
   * @param track the track
   * @param n the frame number
   * @param x1 the x coordinate of end 1
   * @param y1 the y coordinate of end 1
   * @param x2 the x coordinate of end 2
   * @param y2 the y coordinate of end 2
   */
  public TapeStep(TapeMeasure track, int n, double x1, double y1,
                    double x2, double y2) {
    super(track, n);
    tape = track;
    end1 = new Tip(x1, y1);
    end1.setTrackEditTrigger(true);
    end2 = new Tip(x2, y2);
    end2.setTrackEditTrigger(true);
    middle = new TPoint(x1, y1); // used for layout position
    handle = new Handle((x1+x2)/2, (y1+y2)/2);
    handle.setTrackEditTrigger(true);
    points = new TPoint[] {end1, end2, handle, middle};
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
  public void setFootprint(Footprint footprint) {
    if (footprint.getLength() >= 2)
      super.setFootprint(footprint);
  }

  /**
   * Overrides Step findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the Interactive that is hit, or null
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    boolean drawLayout = false;
    Shape hitShape;
    Interactive hit = null;
    if (endsEnabled) {
      hitShape = end1Shapes.get(trackerPanel);
      if (hitShape != null && hitShape.intersects(hitRect)) 
      	hit = end1;
      hitShape = end2Shapes.get(trackerPanel);
      if (hit == null && hitShape != null && hitShape.intersects(hitRect)) 
      	hit = end2;
    }
    hitShape = shaftShapes.get(trackerPanel);
    if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
      hit = handle;
    }
    Rectangle layoutRect = layoutBounds.get(trackerPanel);
    if (hit == null && layoutRect != null 
    		&& layoutRect.intersects(hitRect)) {
//      drawLayout = !tape.readOnly;
      drawLayout = true;
      hit = tape;
    }    
    if (drawLayout != drawLayoutBounds) {
    	drawLayoutBounds = drawLayout;
      repaint(trackerPanel);
    }
  	
  	if (end1.isAttached() && (hit==end1 || hit==handle)) return null;
  	if (end2.isAttached() && (hit==end2 || hit==handle)) return null;

    return hit;
  }

  /**
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    // draw the tape
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    getMark(trackerPanel).draw(g, false);
    Paint gpaint = g.getPaint();
    g.setPaint(footprint.getColor());
    // draw the text layout unless editing
    if (!tape.editing) {
      TextLayout layout = textLayouts.get(trackerPanel);
      Point p = getLayoutPosition(trackerPanel, layout);
      Font gfont = g.getFont();
      g.setFont(textLayoutFont);
      layout.draw(g, p.x, p.y);
      g.setFont(gfont);
      if (drawLayoutBounds && tape.isFieldsEnabled()) {
      	Rectangle rect = layoutBounds.get(trackerPanel);
      	g.drawRect(rect.x-2, rect.y-3, rect.width+5, rect.height+5);
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
  public TPoint getDefaultPoint() {
  	TPoint p = tape.trackerPanel.getSelectedPoint();
    if (p==points[0]) return points[0];
    if (p==points[1]) return points[1];
    return points[defaultIndex];
  }

  /**
   * Overrides Step getMark method.
   *
   * @param trackerPanel the tracker panel
   * @return the mark
   */
  protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    TPoint selection = null;
    if (mark == null) {
      // adjust tips if stick mode
    	if (tape.isStickMode()) {
    		adjustTipsToLength();
    	}
      selection = trackerPanel.getSelectedPoint();
      // get screen points
      Point p = null;
      for (int i = 0; i < points.length; i++) {
        screenPoints[i] = points[i].getScreenPosition(trackerPanel);
        if (selection == points[i]) p = screenPoints[i];
      }
      // create mark
      mark = footprint.getMark(screenPoints);
      if (p != null) {
        final Color color = footprint.getColor();
        final Mark stepMark = mark;
        transform.setToTranslation(p.x, p.y);
        final Shape selectedShape
          	= transform.createTransformedShape(selectionShape);
        mark = new Mark() {
          public void draw(Graphics2D g, boolean highlighted) {
            stepMark.draw(g, false);
            Paint gpaint = g.getPaint();
            g.setPaint(color);
            if (selectedShape != null) g.fill(selectedShape);
            g.setPaint(gpaint);
          }

          public Rectangle getBounds(boolean highlighted) {
            Rectangle bounds = stepMark.getBounds(false);
            if (selectedShape != null)
            	bounds.add(selectedShape.getBounds());
            return bounds;
          }
        };
      }
      marks.put(trackerPanel, mark);
      
      // get new hit shapes
      Shape[] shapes = footprint.getHitShapes();
      end1Shapes.put(trackerPanel, shapes[0]);
      end2Shapes.put(trackerPanel, shapes[1]);
      shaftShapes.put(trackerPanel, shapes[2]);
      // get new text layout
      double tapeLength = getTapeLength(!tape.isStickMode());
      String s = tape.getFormattedLength(tapeLength);
      TextLayout layout = new TextLayout(s, textLayoutFont, frc);
      textLayouts.put(trackerPanel, layout);
      // get layout position (bottom left corner of text)
      p = getLayoutPosition(trackerPanel, layout);
      Rectangle bounds = layoutBounds.get(trackerPanel);
      if (bounds == null) {
        bounds = new Rectangle();
        layoutBounds.put(trackerPanel, bounds);
      }
      Rectangle2D rect = layout.getBounds();
      // set bounds (top left corner and size)
      bounds.setRect(p.x, p.y - rect.getHeight(),
                     rect.getWidth(), rect.getHeight());
    }
    return mark;
  }

  /**
   * Overrides Step getBounds method.
   *
   * @param trackerPanel the tracker panel drawing the step
   * @return the bounding rectangle
   */
  public Rectangle getBounds(TrackerPanel trackerPanel) {
    Rectangle bounds = getMark(trackerPanel).getBounds(false);
    bounds.add(layoutBounds.get(trackerPanel));
    bounds.grow(2, 2);
    return bounds;
  }

  /**
   * Gets the scaled world length of this tape, with side effect of updating
   * angle and length fields.
   * 
   * @param fromEnds true if calculated from the current tip positions
   * @return the length in world units
   */
  public double getTapeLength(boolean fromEnds) {
    double scaleX = 1;
    double scaleY = 1;
    double axisTiltAngle = 0;
    if (tape.trackerPanel != null) {
      scaleX = tape.trackerPanel.getCoords().getScaleX(n);
      scaleY = tape.trackerPanel.getCoords().getScaleY(n);
      axisTiltAngle = tape.trackerPanel.getCoords().getAngle(n);
    }
    double dx = (end2.getX() - end1.getX()) / scaleX;
    double dy = (end1.getY() - end2.getY()) / scaleY;
    tapeAngle = Math.atan2(dy, dx);
    xAxisToTapeAngle = tapeAngle - axisTiltAngle;
    tape.angleField.setValue(xAxisToTapeAngle);   
  	double length = fromEnds? Math.sqrt(dx*dx + dy*dy): worldLength;
  	tape.magField.setValue(length);
    return length;
  }

  /**
   * Gets the world angle of this tape. 
   * Call this method only after getTapeLength(), which does the real work.
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
    if (tape.isLocked() || tape.trackerPanel == null) return;
    length = Math.abs(length);
    length = Math.max(length, TapeMeasure.MIN_LENGTH);
    double factor = getTapeLength(!tape.isStickMode()) / length;
    if (factor==1) return;
    
    XMLControl trackControl = new XMLControlElement(tape);
    if (tape.isReadOnly()) {
//      tape.lengthKeyFrames.add(n);
    	worldLength = length;
    	adjustTipsToLength();
    	tape.repaint(this);
    	Undo.postTrackEdit(tape, trackControl);
    	return;
    }
    
    if (tape.isStickMode()) {    
	    if (tape.isFixedLength()) {
	    	TapeStep step = (TapeStep)tape.steps.getStep(0);
	    	step.worldLength = length;
	    }
	    else {
	      tape.lengthKeyFrames.add(n);
	      worldLength = length;
	    }
    }
    double scaleX = factor * tape.trackerPanel.getCoords().getScaleX(n);
    double scaleY = factor * tape.trackerPanel.getCoords().getScaleY(n);
    XMLControl coordsControl = new XMLControlElement(tape.trackerPanel.getCoords());
    tape.isStepChangingScale = true;
    tape.trackerPanel.getCoords().setScaleXY(n, scaleX, scaleY);
    tape.isStepChangingScale = false;
    if (tape.isStickMode()) {
    	Undo.postTrackAndCoordsEdit(tape, trackControl, coordsControl);
    }
    else {
    	Undo.postCoordsEdit(tape.trackerPanel, coordsControl);
    }
    erase();
  }

  /**
   * Sets the world angle of this tape.
   *
   * @param theta the angle in radians
   */
  public void setTapeAngle(double theta) {
    if (tape.isLocked() || tape.trackerPanel == null) return;
    
    if (tape.isReadOnly()) {
      XMLControl trackControl = new XMLControlElement(tape);
    	xAxisToTapeAngle = theta;
    	adjustTipsToAngle();
    	tape.repaint(this);
    	Undo.postTrackEdit(tape, trackControl);
    	return;
    }

    double dTheta = theta - xAxisToTapeAngle;
    ImageCoordSystem coords = tape.trackerPanel.getCoords();
    XMLControl state = new XMLControlElement(coords);
    double angle = coords.getAngle(n);
    coords.setAngle(n, angle-dTheta);
    Undo.postCoordsEdit(tape.trackerPanel, state);
  }
  
  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public Object clone() {
    TapeStep step = (TapeStep)super.clone();
    if (step != null) {
      step.points[0] = step.end1 = step.new Tip(end1.getX(), end1.getY());
      step.points[1] = step.end2 = step.new Tip(end2.getX(), end2.getY());
      step.points[2] = step.handle = step.new Handle(handle.getX(), handle.getY());
      step.points[3] = step.middle = new TPoint(middle.getX(), middle.getY());
      step.end1.setTrackEditTrigger(true);
      step.end2.setTrackEditTrigger(true);
      step.handle.setTrackEditTrigger(true);
      step.end1Shapes = new HashMap<TrackerPanel, Shape>();
      step.end2Shapes = new HashMap<TrackerPanel, Shape>();
      step.shaftShapes = new HashMap<TrackerPanel, Shape>();
      step.textLayouts = new HashMap<TrackerPanel, TextLayout>();
      step.layoutBounds = new HashMap<TrackerPanel, Rectangle>();
      step.worldLength = worldLength;
    }
    return step;
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
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
    return 4;
  }

  //__________________________ private and protected methods __________________________

  /**
   * Moves the tips of the tape to display current worldLength.
   */
  protected void adjustTipsToLength() {
  	double sin = end1.sin(end2);
  	double cos = end1.cos(end2);
  	double d = end1.distance(end2);
  	double factor = worldLength/getTapeLength(true);
  	
  	// special case: d==0 must be corrected
    if (d==0) {
    	sin = 0;
    	cos = 1;
    	d = 1;
      double scaleX = tape.trackerPanel.getCoords().getScaleX(n);
    	factor = worldLength*scaleX;
    }
  	
  	// if either end is selected or attached to a point mass, keep that end fixed
  	TPoint p = tape.trackerPanel.getSelectedPoint();
  	if (end1.isAttached())
  		p = end1;
  	else if (end2.isAttached())
  		p = end2;
  	
  	if (p instanceof Tip) {
  		// keep end fixed
  		if (p==end1) {
  	  	double x = end1.getX()+cos*d*factor;
  	  	double y = end1.getY()-sin*d*factor;
		    end2.setLocation(x, y);
  		}
  		else {
  	  	double x = end2.getX()-cos*d*factor;
  	  	double y = end2.getY()+sin*d*factor;
		    end1.setLocation(x, y);
  		}
  	}
  	else if (p==handle) {
  		// keep handle fixed
    	d = handle.distance(end1);
    	if (d==0) d = 0.5; // special case
	  	double x = handle.getX()-cos*d*factor;
	  	double y = handle.getY()+sin*d*factor;
	    end1.setLocation(x, y);
    	d = handle.distance(end2);
    	if (d==0) d = 0.5; // special case
	  	x = handle.getX()+cos*d*factor;
	  	y = handle.getY()-sin*d*factor;
	    end2.setLocation(x, y);  		
  	}
  	else {
  		// keep middle fixed
	  	middle.center(end1, end2);
	  	double x1 = middle.getX()-cos*d*factor/2;
	  	double y1 = middle.getY()+sin*d*factor/2;
	  	double x2 = middle.getX()+cos*d*factor/2;
	  	double y2 = middle.getY()-sin*d*factor/2;
	    end1.setLocation(x1, y1);
	    end2.setLocation(x2, y2);
  	}
  }

  /**
   * Moves the tips of the tape to display current xAxisToTapeAngle.
   */
  protected void adjustTipsToAngle() {
    double axisTiltAngle = tape.trackerPanel.getCoords().getAngle(n);
    tapeAngle = xAxisToTapeAngle + axisTiltAngle;
  	double sin = Math.sin(tapeAngle);
  	double cos = Math.cos(tapeAngle);
  	double d = end1.distance(end2);
  	
  	// if either end is selected or attached to a point mass, rotate about that end
  	TPoint p = tape.trackerPanel.getSelectedPoint();
  	if (end1.isAttached())
  		p = end1;
  	else if (end2.isAttached())
  		p = end2;
  	
  	if (p instanceof Tip) {
  		// rotate about an end
  		if (p==end1) {
  	  	double x = end1.getX()+cos*d;
  	  	double y = end1.getY()-sin*d;
		    end2.setLocation(x, y);
  		}
  		else {
  	  	double x = end2.getX()-cos*d;
  	  	double y = end2.getY()+sin*d;
		    end1.setLocation(x, y);
  		}
  	}
  	else if (p==handle) {
  		// rotate about the handle
    	d = handle.distance(end1);
	  	double x = handle.getX()-cos*d;
	  	double y = handle.getY()+sin*d;
	    end1.setLocation(x, y);
    	d = handle.distance(end2);
	  	x = handle.getX()+cos*d;
	  	y = handle.getY()-sin*d;
	    end2.setLocation(x, y);  		  		
  	}
  	else {
  		// rotate about the middle
	  	middle.center(end1, end2);
	  	double x1 = middle.getX()-cos*d/2;
	  	double y1 = middle.getY()+sin*d/2;
	  	double x2 = middle.getX()+cos*d/2;
	  	double y2 = middle.getY()-sin*d/2;
	    end1.setLocation(x1, y1);
	    end2.setLocation(x2, y2);
  	}
  }

  /**
   * Gets TextLayout screen position. Unlike most positions,
   * this one refers to the lower left corner of the text.
   *
   * @param trackerPanel the tracker panel
   * @param layout the text layout
   * @return the screen position point
   */
  private Point getLayoutPosition(TrackerPanel trackerPanel,
                                  TextLayout layout) {
    middle.center(end1, end2);
    Point p = middle.getScreenPosition(trackerPanel);
    Rectangle2D bounds = layout.getBounds();
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
    double cos = endPoint2.cos(endPoint1);
    double sin = endPoint2.sin(endPoint1);
    double d = 6 + Math.abs(w*sin/2) + Math.abs(h*cos/2);
    if (cos >= 0) {   // first/fourth quadrants
      p.setLocation((int)(p.x - d*sin - w/2), (int)(p.y - d*cos + h/2));
    }
    else {            // second/third quadrants
      p.setLocation((int)(p.x + d*sin - w/2), (int)(p.y + d*cos + h/2));
    }
    return p;
  }

  //______________________ inner Handle class ________________________

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
    public void setXY(double x, double y) {
      if (track.locked) return;
      double dx = x - getX();
      double dy = y - getY();
    	setLocation(x, y);
      
      if (tape.isFixedPosition()) {
      	TapeStep step = (TapeStep)tape.steps.getStep(0);
        step.end1.setLocation(end1.getX() + dx, end1.getY() + dy);
        step.end2.setLocation(end2.getX() + dx, end2.getY() + dy);
  	    step.erase();
  	    tape.refreshStep(TapeStep.this); // sets properties of this step
      }
      else {
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
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }

    /**
     * Sets the position of this handle on the shaft nearest the specified
     * screen position.
     *
     * @param xScreen the x screen position
     * @param yScreen the y screen position
     * @param trackerPanel the trackerPanel drawing this step
     */
    public void setPositionOnShaft(int xScreen, int yScreen, TrackerPanel trackerPanel) {
    	setPositionOnLine(xScreen, yScreen, trackerPanel, end1, end2);
    }

  }

  //______________________ inner Tip class ________________________

  class Tip extends TPoint {

    private double lastX, lastY;
    
    /**
     * Constructs a Tip with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Tip(double x, double y) {
      super(x, y);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (track.locked) return;
      if (tape.isStickMode() && isAdjusting()) {
      	lastX = x;
      	lastY = y;
      }
      
      if (tape.isFixedPosition()) {
      	// first set properties of step 0
      	TapeStep step = (TapeStep)tape.steps.getStep(0);
      	if (this==end1) {
      		step.end1.setLocation(x, y);
      		step.end2.setLocation(end2);
      	}
      	else {
       		step.end2.setLocation(x, y);
      		step.end1.setLocation(end1);
      	}
  	    step.erase();
  	    tape.refreshStep(TapeStep.this); // sets properties of this step
      }
      else {
      	setLocation(x, y);
      	tape.keyFrames.add(n);
    	}      
      
      if (tape.isStickMode()) {
        ImageCoordSystem coords = tape.trackerPanel.getCoords();
        coords.setAdjusting(isAdjusting());
	      double newLength = getTapeLength(true); // newly calculated
	      double factor = newLength/getTapeLength(false); // current worldLength
	      double scaleX = factor * coords.getScaleX(n);
	      double scaleY = factor * coords.getScaleY(n);
	      tape.isStepChangingScale = true;
	      tape.trackerPanel.getCoords().setScaleXY(n, scaleX, scaleY);
	      tape.isStepChangingScale = false;
      }
      tape.dataValid = false;
      tape.firePropertyChange("data", null, tape); //$NON-NLS-1$
      repaint();
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing the step
     * @return the containing TapeStep frame number
     */
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }
    
    /**
     * Overrides TPoint method.
     *
     * @param adjusting true if being dragged
     */
    public void setAdjusting(boolean adjusting) {
    	if (tape.isStickMode()) {
	    	boolean wasAdjusting = isAdjusting();
	    	super.setAdjusting(adjusting);
	    	if (wasAdjusting && !adjusting) {
	    		setXY(lastX, lastY);
	    	}
    	}
    	else super.setAdjusting(adjusting);
    }
    
    public boolean isCoordsEditTrigger() {
    	return tape.isStickMode();
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
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
    	TapeStep step = (TapeStep) obj;
    	double[] data = new double[] {step.getEnd1().x, step.getEnd1().y,
    			step.getEnd2().x, step.getEnd2().y};
      control.setValue("end_positions", data); //$NON-NLS-1$
      control.setValue("worldlength", step.worldLength); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	// this loader is not intended to be used to create new steps,
    	// but only for undo/redo step edits.
      return null;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	TapeStep step = (TapeStep)obj;
      double[] data = (double[])control.getObject("end_positions"); //$NON-NLS-1$
      step.getEnd1().setLocation(data[0], data[1]);
  		step.getEnd2().setLocation(data[2], data[3]);
      step.worldLength = control.getDouble("worldlength"); //$NON-NLS-1$
    	return obj;
    }
  }
}

