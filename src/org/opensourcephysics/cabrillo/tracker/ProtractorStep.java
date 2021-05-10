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

import java.util.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * This is a Step for a Protractor. It is used for measuring angles.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class ProtractorStep extends Step {
	
  protected static AffineTransform transform = new AffineTransform();
	protected static TPoint endPoint1 = new TPoint(); // used for layout position
  protected static TPoint endPoint2 = new TPoint(); // used for layout position
  protected static TPoint middle = new TPoint(); // used for layout position
  protected static NumberField formatField = new NumberField(1);

  // instance fields
  protected Protractor protractor;
  protected TPoint vertex, end1, end2; 
  protected Handle handle;
  protected Rotator rotator;
  protected double line1Angle, line2Angle; // in radians
  protected boolean endsEnabled = true, drawArcCircle;
  protected boolean drawLayoutBounds, drawLayout1, drawLayout2;
  protected MultiShape vertexCircle;
  protected Map<TrackerPanel, Shape> vertexShapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> end1Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> end2Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> line1Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> line2Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> rotatorShapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, TextLayout> textLayouts = new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layoutBounds = new HashMap<TrackerPanel, Rectangle>();
  protected Map<TrackerPanel, TextLayout> textLayouts1 = new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layout1Bounds = new HashMap<TrackerPanel, Rectangle>();
  protected Map<TrackerPanel, TextLayout> textLayouts2 = new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layout2Bounds = new HashMap<TrackerPanel, Rectangle>();
  protected Shape selectedShape;
  
  /**
   * Constructs a ProtractorStep with specified end point coordinates in image space.
   *
   * @param track the track
   * @param n the frame number
   * @param x1 the x coordinate of end 1
   * @param y1 the y coordinate of end 1
   * @param x2 the x coordinate of end 2
   * @param y2 the y coordinate of end 2
   */
  public ProtractorStep(Protractor track, int n, double x1, double y1,
                    double x2, double y2) {
    super(track, n);
    protractor = track;
    vertex = new Tip(x1, y1);
    end1 = new Tip(x2, y2);
    double x = (x1+x2)/2;
    double y = y1-(x2-x1)*Math.sin(Math.PI/3);
    end2 = new Tip(x, y);
    handle = new Handle((x1+x2)/2, (y1+y2)/2);
    rotator = new Rotator();
    points = new TPoint[] {vertex, end1, end2, handle, rotator};
    screenPoints = new Point[getLength()];
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
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the Interactive that is hit, or null
   */
  @Override
public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    boolean isWorldView = panel instanceof WorldTView;
    setHitRectCenter(xpix, ypix);
    Shape hitShape;
    Interactive hit = null;
    ProtractorFootprint footprint = null;
    boolean drawLayout = false, draw1 = false, draw2 = false;
    if (protractor.getFootprint()!=null 
    		&& protractor.getFootprint() instanceof ProtractorFootprint) {
    	footprint = (ProtractorFootprint)protractor.getFootprint();
    }
    boolean isRulerVisible = protractor.ruler != null && protractor.ruler.isVisible();
    if (endsEnabled) {
      hitShape = vertexShapes.get(trackerPanel);
      if (!vertex.isAttached() && hitShape != null && hitShape.intersects(hitRect)) {
      	hit = vertex;
      	if (vertexCircle==null && footprint!=null) {
      		vertexCircle = footprint.getCircleShape(vertex.getScreenPosition(trackerPanel));
      	}
      }
      // clear vertex highlight shape when no longer needed
      if (hit==null && vertexCircle!=null) {
      	vertexCircle = null;
      }
      hitShape = end1Shapes.get(trackerPanel);
      if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
        hit = end1;
        draw1 = !isRulerVisible;
      }
      hitShape = end2Shapes.get(trackerPanel);
      if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
      	hit = end2;
      	draw2 = !isRulerVisible;
      }
    }
    hitShape = rotatorShapes.get(trackerPanel);
    if (!end1.isAttached() && !end2.isAttached() && hit==null && hitShape!=null && hitShape.intersects(hitRect)) {
      hit = rotator;
    }
    if (hit == null && trackerPanel.getSelectedPoint()==rotator 
    		&& selectedShape != null && selectedShape.intersects(hitRect)) {
      hit = rotator;
    }
    if (hit==rotator && trackerPanel.getSelectedPoint()!=rotator && !isWorldView) {
      rotator.setScreenCoords(xpix, ypix);
    }
    drawArcCircle = hit==rotator || trackerPanel.getSelectedPoint()==rotator;
    hitShape = line1Shapes.get(trackerPanel);
    if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
      hit = handle;
      handle.setHandleEnd(end1);
    }
    hitShape = line2Shapes.get(trackerPanel);
    if (hit == null && hitShape != null && hitShape.intersects(hitRect)) {
      hit = handle;
      handle.setHandleEnd(end2);
    }
		if (hit == null && protractor.ruler != null && protractor.ruler.isVisible()) {
			hit = protractor.ruler.findInteractive(trackerPanel, hitRect);
		}
    Rectangle layoutRect = layoutBounds.get(trackerPanel);
    if (hit == null && layoutRect != null 
    		&& layoutRect.intersects(hitRect)) {
      drawLayout = true;
      hit = protractor;
    }
    drawLayoutBounds = drawLayout;
    drawLayout1 = draw1;
    drawLayout2 = draw2;
 
  	if (end1.isAttached() && (hit==end1 || hit==handle || hit==rotator)) return null;
  	if (end2.isAttached() && (hit==end2 || hit==handle || hit==rotator)) return null;
  	if (vertex.isAttached() && (hit==vertex || hit==handle)) return null;
  	
    return hit;
  }

  /**
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  @Override
public void draw(DrawingPanel panel, Graphics _g) {
    // draw the mark
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    boolean isWorldView = panel instanceof WorldTView;
    Graphics2D g = (Graphics2D)_g;
    getMark(trackerPanel).draw(g, false);
    Paint gpaint = g.getPaint();
    g.setPaint(footprint.getColor());
    Font gfont = g.getFont();
    g.setFont(TFrame.textLayoutFont);
    // draw the text layout if not editing and not world view
    if (!protractor.editing && !isWorldView) {
	    TextLayout layout = textLayouts.get(trackerPanel);
			Rectangle bounds = layoutBounds.get(trackerPanel);
			g.setFont(TFrame.textLayoutFont);
			layout.draw(g, bounds.x, bounds.y + bounds.height);
			g.setFont(gfont);
			if (drawLayoutBounds) {
				g.drawRect(bounds.x - 2, bounds.y - 3, bounds.width + 6, bounds.height + 5);
			}
    }
    // draw the vertex circle only if vertex not selected
  	if (trackerPanel.getSelectedPoint()==vertex)
  		vertexCircle = null;
    if (vertexCircle!=null && !isWorldView) {
    	vertexCircle.draw(g);
    }
    
    // draw arm length layouts if visible
    if (drawLayout1 && !isWorldView) {
	    TextLayout layout = textLayouts1.get(trackerPanel);
	    Point p = getLayoutPosition(trackerPanel, layout, end1);
	    layout.draw(g, p.x, p.y);
    }
    if (drawLayout2 && !isWorldView) {
	    TextLayout layout = textLayouts2.get(trackerPanel);
	    Point p = getLayoutPosition(trackerPanel, layout, end2);
	    layout.draw(g, p.x, p.y);
    }

    g.setFont(gfont);
    g.setPaint(gpaint);
  }

  /**
   * Overrides Step getMark method.
   *
   * @param trackerPanel the tracker panel
   * @return the mark
   */
  @Override
protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    if (mark == null) {
      getProtractorAngle(true); // updates angle display
      ProtractorFootprint pFootprint = (ProtractorFootprint)footprint;
      TPoint selection = trackerPanel.getSelectedPoint();
      boolean isWorldView = trackerPanel instanceof WorldTView;
      
      // get screen points
      Point p = null;
      for (int i = 0; i < points.length; i++) {
        screenPoints[i] = points[i].getScreenPosition(trackerPanel);
        if (selection==points[i]) p = screenPoints[i];
      }
			
			// create mark to draw ruler
			Mark rulerMark = protractor.ruler != null && protractor.ruler.isVisible()? 
					protractor.ruler.getMark(trackerPanel, n): null;
      
      // create footprint mark
			pFootprint.setArcVisible(!isWorldView);
      Mark stepMark = pFootprint.getMark(screenPoints);
      
      // create arcCircle
      MultiShape arcCircle = isWorldView? null:
      	selection==rotator? pFootprint.getArcAdjustShape(screenPoints[0], screenPoints[4]):
      	pFootprint.getArcAdjustShape(screenPoints[0], null);
      
      // create selectedShape if point is selected
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
          stepMark.draw(g, false);
					Paint gpaint = g.getPaint();
					Stroke gstroke = g.getStroke();
					g.setPaint(footprint.getColor());
					if (rulerMark != null) {
						rulerMark.draw(g, false);
					}
					if (arcCircle != null && drawArcCircle) {
						arcCircle.draw(g);
					}
          if (selectedShape != null && !isWorldView) {
						g.setStroke(selectionStroke);
						g.draw(selectedShape);
          }
					g.setPaint(gpaint);
					g.setStroke(gstroke);
        }
      };
      marks.put(trackerPanel, mark);
      
      // get new hit shapes
      Shape[] shapes = footprint.getHitShapes();
      vertexShapes.put(trackerPanel, shapes[0]);
      end1Shapes.put(trackerPanel, shapes[1]);
      end2Shapes.put(trackerPanel, shapes[2]);
      line1Shapes.put(trackerPanel, shapes[3]);
      line2Shapes.put(trackerPanel, shapes[4]);
      rotatorShapes.put(trackerPanel, shapes[5]);
      // get new text layouts
      String s = protractor.angleField.getText();
      TextLayout layout = new TextLayout(s, TFrame.textLayoutFont, OSPRuntime.frc);
      textLayouts.put(trackerPanel, layout);
      // get layout position (bottom left corner of text)
      p = getLayoutPosition(trackerPanel, layout, vertex);
      Rectangle bounds = layoutBounds.get(trackerPanel);
      if (bounds == null) {
        bounds = new Rectangle();
        layoutBounds.put(trackerPanel, bounds);
      }
      Rectangle2D rect = layout.getBounds();
      // set bounds (top left corner and size)
      bounds.setRect(p.x, p.y - rect.getHeight(),
                     rect.getWidth(), rect.getHeight());
      
      for (int k=0; k<2; k++) {
      	TPoint end = k==0? end1: end2;
      	Map<TrackerPanel, TextLayout> layouts = k==0? textLayouts1: textLayouts2;
        Map<TrackerPanel, Rectangle> lBounds = k==0? layout1Bounds: layout2Bounds;
	      s = getFormattedLength(end);
	      s += trackerPanel.getUnits(protractor, Protractor.dataVariables[2+k]);    
	      layout = new TextLayout(s, TFrame.textLayoutFont, OSPRuntime.frc);
	      layouts.put(trackerPanel, layout);
	      p = getLayoutPosition(trackerPanel, layout, end);
	      bounds = lBounds.get(trackerPanel);
	      if (bounds == null) {
	        bounds = new Rectangle();
	        lBounds.put(trackerPanel, bounds);
	      }
	      rect = layout.getBounds();
	      // set bounds (top left corner and size)
	      bounds.setRect(p.x, p.y - rect.getHeight(),
	                     rect.getWidth(), rect.getHeight());
      }
    }
    return mark;
  }

  /**
   * Formats the specified length value.
   *
   * @param length the length value to format
   * @return the formatted length string
   */
  public String getFormattedLength(TPoint end) {
  	double length = getArmLength(end);
    if (protractor.trackerPanel.getFrameNumber()==n) {
	  	NumberField field = end==end1? getTrack().xField: getTrack().yField;
	    field.setValue(length);
	    return field.format(length);
    }
    formatField.setFixedPattern(getTrack().xField.getFixedPattern());
    formatField.setFormatFor(length);
    return formatField.format(length);
  }

  /**
   * Gets the protractor angle. 
   *
   * @param refreshField true to refresh the protractor angleField
   * @return the angle in radians
   */
  public double getProtractorAngle(boolean refreshField) {
    line1Angle = -vertex.angle(end1);
    line2Angle = -vertex.angle(end2);
    double theta = line2Angle-line1Angle;
    if (theta > Math.PI) theta -= 2*Math.PI;
    if (theta < -Math.PI) theta += 2*Math.PI;
    if (refreshField && protractor.trackerPanel.getFrameNumber()==n) {    	
    	protractor.angleField.setValue(theta);
    }
    return theta;
  }

  /**
   * Sets the protractor angle of this tape.
   *
   * @param theta the angle in radians
   */
  public void setProtractorAngle(double theta) {
    if (protractor.isLocked() || protractor.trackerPanel == null) return;
    XMLControl state = new XMLControlElement(protractor);
    theta += line1Angle;
    // move line2 to new angle at same distance from vertex
    double d = end2.distance(vertex);
    double dx = d*Math.cos(theta);
    double dy = -d*Math.sin(theta);
    end2.setLocation(vertex.x+dx, vertex.y+dy);
   repaint();
		Undo.postTrackEdit(protractor, state);
  }

  /**
   * Gets the world length of the base or arm. 
   * 
   * @param end TPoint end1 or end2
   * @return the length in world units
   */
  public double getArmLength(TPoint end) {
    if (protractor.trackerPanel== null) return 1.0;
    double scaleX = protractor.trackerPanel.getCoords().getScaleX(n);
    double scaleY = protractor.trackerPanel.getCoords().getScaleY(n);
    double dx = (vertex.getX() - end.getX()) / scaleX;
    double dy = (end.getY() - vertex.getY()) / scaleY;
  	return Math.sqrt(dx*dx + dy*dy);
  }

  /**
   * Sets the arm length of this tape.
   *
   * @param end the arm end
   * @param length the desired length in world units
   */
  public void setArmLength(TPoint end, double length) {
    if (protractor.isLocked() || protractor.trackerPanel == null) return;
    XMLControl state = new XMLControlElement(protractor);
    // move end to new distance from vertex
    double scaleX = protractor.trackerPanel.getCoords().getScaleX(n);
    double scaleY = protractor.trackerPanel.getCoords().getScaleY(n);
    double dx = length*vertex.cos(end) * scaleX;
    double dy = -length*vertex.sin(end) * scaleY;
    end.setXY(vertex.x+dx, vertex.y+dy);
    repaint();
		Undo.postTrackEdit(protractor, state);
  }

  /**
   * Moves the protractor so the vertex is at the specified position.
   *
   * @param x
   * @param y
   */
  protected void moveVertexTo(double x, double y) {
    if (protractor.isLocked() || protractor.trackerPanel == null) return;
    // determine how far to move
    double dx = x - vertex.x;
    double dy = y - vertex.y;
    handle.setXY(handle.x + dx, handle.y + dy);
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  @Override
public Object clone() {
    ProtractorStep step = (ProtractorStep)super.clone();
    if (step != null) {
      step.points[0] = step.vertex = step.new Tip(vertex.getX(), vertex.getY());
      step.points[1] = step.end1 = step.new Tip(end1.getX(), end1.getY());
      step.points[2] = step.end2 = step.new Tip(end2.getX(), end2.getY());
      step.points[3] = step.handle = step.new Handle(handle.getX(), handle.getY());
      step.points[4] = step.rotator = step.new Rotator();
      step.vertex.setTrackEditTrigger(true);
      step.end1.setTrackEditTrigger(true);
      step.end2.setTrackEditTrigger(true);
      step.handle.setTrackEditTrigger(true);
      step.vertexShapes = new HashMap<TrackerPanel, Shape>();
      step.end1Shapes = new HashMap<TrackerPanel, Shape>();
      step.end2Shapes = new HashMap<TrackerPanel, Shape>();
      step.line1Shapes = new HashMap<TrackerPanel, Shape>();
      step.line2Shapes = new HashMap<TrackerPanel, Shape>();
      step.rotatorShapes = new HashMap<TrackerPanel, Shape>();
      step.textLayouts = new HashMap<TrackerPanel, TextLayout>();
      step.layoutBounds = new HashMap<TrackerPanel, Rectangle>();
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
    return "ProtractorStep"; //$NON-NLS-1$
  }

  /**
   * Returns the frame number.
   *
   * @return the frame number
   */
  public int n() {
    if (protractor.isFixedPosition() && protractor.trackerPanel != null)
      return protractor.trackerPanel.getFrameNumber();
    return n;
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 5;
  }

  //__________________________ private methods __________________________

  /**
   * Gets TextLayout screen position. Unlike most positions,
   * this one refers to the lower left corner of the text.
   *
   * @param trackerPanel the tracker panel
   * @param layout the text layout
   * @param end the vertex, end1 or end2
   * @return the screen position point
   */
  private Point getLayoutPosition(TrackerPanel trackerPanel,
                                  TextLayout layout, TPoint end) {
    double scale = FontSizer.getFactor();
    Rectangle2D bounds = layout.getBounds();
    double w = bounds.getWidth();
    double h = bounds.getHeight();
  	if (end==vertex) {
	    Point p = vertex.getScreenPosition(trackerPanel);
	    double angle = line1Angle - Math.PI / 2;
	    double sin = Math.sin(angle);
	    double cos = Math.cos(angle);
			double halfhsin = h * sin / 2;
			double halfwcos = w * cos / 2;
			double d = Math.sqrt((halfhsin*halfhsin) + (halfwcos*halfwcos)) + 8; 
			if (protractor.ruler != null && protractor.ruler.isVisible() && getProtractorAngle(false) < 0) 
				p.setLocation((int)(p.x - d*cos - w/2), (int)(p.y + d*sin + h/2));
			else
				p.setLocation((int)(p.x + d*cos - w/2), (int)(p.y - d*sin + h/2));
	    return p;
  	}
    middle.center(end, vertex);
    Point p = middle.getScreenPosition(trackerPanel);
    endPoint1.setLocation(end);
    endPoint2.setLocation(vertex);
    double cos = endPoint2.cos(endPoint1);
    double sin = endPoint2.sin(endPoint1);
    double d = scale*6 + Math.abs(w*sin/2) + Math.abs(h*cos/2);
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
  	
  	TPoint end;

    /**
     * Constructs a Handle with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Handle(double x, double y) {
      super(x, y);
      setTrackEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method to move tip, tail and arm.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    @Override
	public void setXY(double x, double y) {
      if (getTrack().locked) return;
      double dx = x - getX();
      double dy = y - getY();
      setLocation(x, y);
      
      if (protractor.isFixedPosition()) { // set properties of step 0
      	ProtractorStep step = (ProtractorStep)protractor.steps.getStep(0);
        step.vertex.setLocation(vertex.getX() + dx, vertex.getY() + dy);
        step.end2.setLocation(end2.getX() + dx, end2.getY() + dy);
        step.end1.setLocation(end1.getX() + dx, end1.getY() + dy);
  	    step.erase();
  	    protractor.refreshStep(ProtractorStep.this); // sets properties of this step
      }
      else {
        vertex.setLocation(vertex.getX() + dx, vertex.getY() + dy);
        end2.setLocation(end2.getX() + dx, end2.getY() + dy);
        end1.setLocation(end1.getX() + dx, end1.getY() + dy);
      	protractor.keyFrames.add(n);
    	}      
     repaint();
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing the step
     * @return the containing ProtractorStep frame number
     */
    @Override
	public int getFrameNumber(VideoPanel vidPanel) {
      return n();
    }

    /**
     * Sets the position of this handle on the line nearest the specified
     * screen position.
     *
     * @param xScreen the x screen position
     * @param yScreen the y screen position
     * @param trackerPanel the trackerPanel drawing this step
     */
    @Override
	public void setPositionOnLine(int xScreen, int yScreen, TrackerPanel trackerPanel) {
      // determine which line is closest
    	setPositionOnLine(xScreen, yScreen, trackerPanel, vertex, end);
    	repaint();
    }
    
    protected void setHandleEnd(TPoint end) {
    	this.end = end;
    }

  }

  //______________________ inner Tip class ________________________

  class Tip extends TPoint {

    /**
     * Constructs a Tip with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Tip(double x, double y) {
      super(x, y);
      setTrackEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    @Override
	public void setXY(double x, double y) {
      if (getTrack().locked) return;
      // keep distance from vertex >= 2*R
      if (this!=vertex) {
	      double d = vertex.distance(x, y);
	      if (d>0.01) {
		      int r = ProtractorFootprint.arcRadius;
		      if (d<2*r) {
		      	x = vertex.getX() + 2*r*(x-vertex.getX())/d;
		      	y = vertex.getY() + 2*r*(y-vertex.getY())/d;
		      }
	      }
      }
      if (protractor.isFixedPosition()) {
      	ProtractorStep step = (ProtractorStep)protractor.steps.getStep(0);
      	TPoint target = this==end1? step.end1: this==end2? step.end2: step.vertex;
      	target.setLocation(x, y); // set property of step 0
  	    step.erase();
  	    protractor.refreshStep(ProtractorStep.this); // sets properties of this step
      }
      else {
      	setLocation(x, y);
      	protractor.keyFrames.add(n);
    	}      
      repaint();
      protractor.invalidateData(protractor);
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing the step
     * @return the containing ProtractorStep frame number
     */
    @Override
    public int getFrameNumber(VideoPanel vidPanel) {
      return n();
    }
    
		/**
		 * Sets the adjusting flag.
		 *
		 * @param adjusting true if being dragged
		 */
		@Override
		public void setAdjusting(boolean adjusting) {
			if (!adjusting && !isAdjusting())
				return;
			super.setAdjusting(adjusting);
			if (!adjusting) {
				protractor.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n())); // $NON-NLS-1$
			}
		}


    
//    public void setLocation(double x, double y) {
//    	super.setLocation(x, y);
//    	protractor.dataValid = false;
//    	protractor.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, protractor);
//    }
  }
  
  //______________________ inner Rotator class ________________________

  class Rotator extends TPoint {
  	TPoint pt = new TPoint(); // for setting screen position
  	
    /**
     * Constructs a Rotator.
     */
    public Rotator() {
      super();
      setTrackEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    @Override
	public void setXY(double x, double y) {
      if (getTrack().locked) return;
      super.setXY(x, y);
      // rotate the protractor so midline passes thru rotator
      double theta = -vertex.angle(this);
      double arc = line2Angle-line1Angle;
      if (arc > Math.PI) arc -= 2*Math.PI;
      if (arc < -Math.PI) arc += 2*Math.PI;      
      double midline = line1Angle+arc/2;
      transform.setToRotation(midline-theta, vertex.x, vertex.y);
      
      if (protractor.isFixedPosition()) {
      	ProtractorStep step = (ProtractorStep)protractor.steps.getStep(0);
        transform.transform(step.end1, step.end1);
        transform.transform(step.end2, step.end2);
  	    step.erase();
  	    protractor.refreshStep(ProtractorStep.this); // sets properties of this step
      }
      else {
        transform.transform(end1, end1);
        transform.transform(end2, end2);
      	protractor.keyFrames.add(n);
    	}      
      
//      // show arc highlight shape
//      ProtractorFootprint footprint = null;
//      if (protractor.getFootprint()!=null 
//      		&& protractor.getFootprint() instanceof ProtractorFootprint) {
//      	footprint = (ProtractorFootprint)protractor.getFootprint();
//      	TTrack track = getTrack();
//	  		Point p1 = vertex.getScreenPosition(track.trackerPanel);
//	  		Point p2 = this.getScreenPosition(track.trackerPanel);
//	  		arcHighlight = footprint.getArcAdjustShape(p1, p2);
//      }
      getTrack().getStep(n).repaint();
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing the step
     * @return the containing ProtractorStep frame number
     */
    @Override
	public int getFrameNumber(VideoPanel vidPanel) {
      return n();
    }
    
    protected void setScreenCoords(int x, int y) {
    	pt.setScreenPosition(x, y, protractor.trackerPanel);
    	this.setLocation(pt);
    }
    
  }

}

