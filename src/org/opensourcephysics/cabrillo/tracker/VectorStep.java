/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2013  Douglas Brown
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

import java.beans.*;
import java.util.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a Step that represents a vector. It is used when tracking
 * vector objects (eg Force) or displaying the motion of a PointMass.
 *
 * @author Douglas Brown
 */
public class VectorStep extends Step
                        implements PropertyChangeListener {
  // static fields
  protected static boolean pointSnapEnabled = true;
  protected static boolean vectorSnapEnabled = true;
  protected static double snapDistance = 8;
  protected static Map<TrackerPanel, Set<VectorStep>> vectors 
  		= new HashMap<TrackerPanel, Set<VectorStep>>();
  protected static TPoint tipPoint = new TPoint(); // used for layout position
  protected static TPoint tailPoint = new TPoint(); // used for layout position

  // instance fields
  protected TPoint tail;
  protected TPoint tip;
  protected TPoint middle;
  protected Handle handle;
  protected Hinge hinge;
  protected int dx, dy; // used when snapped to origin
  protected boolean tipEnabled = true;
  protected Map<TrackerPanel, Shape> tipShapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> shaftShapes = new HashMap<TrackerPanel, Shape>();
  protected TPoint attachmentPoint; // tail is attached to this pt
  protected VectorChain chain;
  protected boolean brandNew = true;
  protected boolean firePropertyChangeEvents = false;
  protected boolean labelVisible = true;
  protected boolean rolloverVisible = false;
  protected boolean valid;
  protected Map<TrackerPanel, TextLayout> textLayouts = new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layoutBounds = new HashMap<TrackerPanel, Rectangle>();
  protected Font font;

  /**
   * Constructs a VectorStep with specified imagespace tail
   * coordinates and vector components.
   *
   * @param track the track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   * @param xc the x component
   * @param yc the y component
   */
  public VectorStep(TTrack track, int n, double x, double y,
                    double xc, double yc) {
    super(track, n);
    tail = new Handle(x, y);
    middle = new TPoint(x, y) { // used for layout positioning
      public int getFrameNumber(VideoPanel vidPanel) {
      	// needed to set layout position correctly on trails
        return VectorStep.this.n;
      }
    };
    tip = new Tip(x, y);
    handle = new Handle(x, y);
    handle.setStepEditTrigger(true);
    hinge = new Hinge(x, y);
    points = new TPoint[] {tip, tail, handle, hinge, middle};
    screenPoints = new Point[getLength()];
    tip.setLocation(x + xc, y + yc);
    font = track.xField.getFont();
  }

  /**
   * Gets the tip.
   *
   * @return the tip
   */
  public TPoint getTip() {
    return tip;
  }

  /**
   * Gets the tail.
   *
   * @return the tail
   */
  public TPoint getTail() {
    return tail;
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
   * Gets the hinge point.
   *
   * @return the hinge
   */
  public TPoint getHinge() {
    return hinge;
  }

  /**
   * Sets the x component.
   *
   * @param x the x component
   */
  public void setXComponent(double x) {
    tip.setX(tail.getX() + x);
  }

  /**
   * Sets the y component.
   *
   * @param y the y component
   */
  public void setYComponent(double y) {
    tip.setY(tail.getY() + y);
  }

  /**
   * Sets the x and y components.
   *
   * @param x the x component
   * @param y the y component
   */
  public void setXYComponents(double x, double y) {
    tip.setXY(tail.getX() + x, tail.getY() + y);
  }

  /**
   * Gets the x component.
   *
   * @return the x component
   */
  public double getXComponent() {
    return tip.getX() - tail.getX();
  }

  /**
   * Gets the y component.
   *
   * @return the y component
   */
  public double getYComponent() {
    return tip.getY() - tail.getY();
  }

  /**
   * Gets the vector label visibility.
   *
   * @return <code>true</code> if label is visible
   */
  public boolean isLabelVisible() {
    return labelVisible;
  }

  /**
   * Sets the vector label visibility.
   *
   * @param visible <code>true</code> to make label visible
   */
  public void setLabelVisible(boolean visible) {
    labelVisible = visible;
  }

  /**
   * Gets the rollover visibility.
   *
   * @return <code>true</code> if labels are visible on rollover only
   */
  public boolean isRolloverVisible() {
    return rolloverVisible;
  }

  /**
   * Sets the rollover visibility.
   *
   * @param visible <code>true</code> to make labels visible on rollover only
   */
  public void setRolloverVisible(boolean visible) {
    rolloverVisible = visible;
  }

  /**
   * Enables and disables snap-to-point.
   *
   * @param enabled <code>true</code> to enable snap-to-point
   */
  public static void setPointSnapEnabled(boolean enabled) {
    pointSnapEnabled = enabled;
  }

  /**
   * Gets whether snap-to-point is enabled.
   *
   * @return <code>true</code> if snap-to-point is enabled
   */
  public static boolean isPointSnapEnabled() {
    return pointSnapEnabled;
  }

  /**
   * Enables and disables snap-to-vector.
   *
   * @param enabled <code>true</code> to enable snap-to-vector
   */
  public static void setVectorSnapEnabled(boolean enabled) {
    vectorSnapEnabled = enabled;
  }

  /**
   * Gets whether snap-to-vector is enabled.
   *
   * @return <code>true</code> if snap-to-vector is enabled
   */
  public static boolean isVectorSnapEnabled() {
    return vectorSnapEnabled;
  }

  /**
   * Snaps to point or vector within snapDistance of tail.
   *
   * @param trackerPanel the tracker panel drawing this
   */
  public void snap(TrackerPanel trackerPanel) {
    TPoint p = null;
    if (pointSnapEnabled) {
      // snap to position
      if (track instanceof PointMass) {
        p = ((PositionStep)track.getStep(n)).getPosition();
        if (p.distance(tail) < snapDistance) {
          attach(p);
          return;
        }
      }
      // snap to origin if brand new or axes visible
      p = trackerPanel.getSnapPoint();
      CoordAxes axes = trackerPanel.getAxes();
      if (brandNew || (axes != null && axes.isVisible())) {
        if (p.distance(tail) < snapDistance) {
          attach(p);
          return;
        }
      }
    }
    if (vectorSnapEnabled) {
       // don't link sum vectors
      if (getTrack() instanceof VectorSum) return;
      // try to link to other vectors
      Set<VectorStep> c = vectors.get(trackerPanel);
      if (c != null) {
        Iterator<VectorStep> it = c.iterator();
        while(it.hasNext()) {
          VectorStep vec = it.next();
          if (!vec.valid) continue;
          if (!getTrack().isStepVisible(vec, trackerPanel)) continue;
          p = vec.getHinge();
          if (p.distance(tail) > snapDistance) continue;
          if (vec == this) continue;
          VectorChain chain = vec.getChain();
          if (chain == null) {
            chain = new VectorChain(vec);
            chain.add(this);
            break;
          }
          else if (chain.getEnd() == vec) {
            chain.add(this);
            break;
          }
        }
      }
    }
  }

  /**
   * Gets the vector chain containing this vector, if any.
   *
   * @return the chain
   */
  public VectorChain getChain() {
    return chain;
  }

  /**
   * Attaches the tail of this vector to the specified point.
   * Detaches if the point is null.
   *
   * @param pt the attachment point
   */
  public void attach(TPoint pt) {
    if (attachmentPoint != null)
      attachmentPoint.removePropertyChangeListener(this);
    attachmentPoint = pt;
    if (pt == null) return;
    pt.addPropertyChangeListener(this);
    if (pt.getX() != tail.getX() || pt.getY() != tail.getY()) {
      tail.setXY(pt.getX(), pt.getY());
    }
  }

  /**
   * Gets the attachment point.
   *
   * @return the attachment point
   */
  public TPoint getAttachmentPoint() {
    return attachmentPoint;
  }

  /**
   * Enables and disables the interactivity of the tip.
   *
   * @param enabled <code>true</code> to enable the tip
   */
  public void setTipEnabled(boolean enabled) {
    tipEnabled = enabled;
  }

  /**
   * Gets whether the tip is enabled.
   *
   * @return <code>true</code> if the tip is enabled
   */
  public boolean isTipEnabled() {
    return tipEnabled;
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
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    if (panel instanceof TrackerPanel) {
      TrackerPanel trackerPanel = (TrackerPanel) panel;
      Graphics2D g = (Graphics2D) _g;
      if (brandNew && !(trackerPanel instanceof WorldTView)) {
        snap(trackerPanel);
        brandNew = false;
      }
      super.draw(trackerPanel, g);
      Set<VectorStep> c = vectors.get(trackerPanel);
      if (c == null) {
        c = new HashSet<VectorStep>();
        vectors.put(trackerPanel, c);
      }
      c.add(this);
      if (labelVisible) {
        TextLayout layout = textLayouts.get(trackerPanel);
        Point p = getLayoutPosition(trackerPanel, layout);
        Paint gpaint = g.getPaint();
        Font gfont = g.getFont();
        g.setPaint(footprint.getColor());
        g.setFont(font);
        layout.draw(g, p.x, p.y);
        g.setPaint(gpaint);
        g.setFont(gfont);
      }
    }
  }

  /**
   * Overrides Step findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the TPoint that is hit, or null
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    // look for origin hit and return null so axes can get selected
    Point origin = trackerPanel.getSnapPoint().getScreenPosition(trackerPanel);
    if (hitRect.contains(origin)) return null;
    // look for shaft hit
    Shape hitShape = shaftShapes.get(trackerPanel);
    if (hitShape != null && hitShape.intersects(hitRect)) {
      if (rolloverVisible && !labelVisible) {
        labelVisible = true;
        this.repaint();
      }
      // alt or shift down to select tip of very short vectors
      if (!trackerPanel.mEvent.isAltDown() && !trackerPanel.mEvent.isShiftDown())
      	return handle;
    }
    if (tipEnabled) {
      // look for tip hit
      hitShape = tipShapes.get(trackerPanel);
      if (hitShape != null && hitShape.intersects(hitRect)) {
        return hinge;
      }
    }
    if (rolloverVisible && labelVisible) {
      labelVisible = false;
      this.repaint();
    }
    return null;
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
      tip.setLocation(tip.getX(), tip.getY()); // resets hinge position
      middle.center(hinge, tail);
      // determine if this step is selected
      selection = trackerPanel.getSelectedPoint();
      Point p = null;
      // get screen points
      valid = true;
      for (int n = 0; n < points.length; n++) {
      	valid = valid && !Double.isNaN(points[n].getX()) && !Double.isNaN(points[n].getY());
        screenPoints[n] = points[n].getScreenPosition(trackerPanel);
        if (selection == points[n]) p = screenPoints[n];
      }
      // move screen points on WorldTView if snapped to origin
    	if (trackerPanel instanceof WorldTView) {
    		WorldTView view = (WorldTView)trackerPanel;
    		if (attachmentPoint == view.getSnapPoint()) {
    			Point origin = view.getSnapPoint().getScreenPosition(view);
    			dx = origin.x - screenPoints[1].x;
    			dy = origin.y - screenPoints[1].y;
          for (int n = 0; n < screenPoints.length; n++) {
            screenPoints[n].x += dx;
            screenPoints[n].y += dy;
          }    			
          if (p != null) {
          	p.x += dx;
          	p.y += dy;
          }
    		}
      }
      // get new text layout
      // determine whether to show xMass
      TrackerPanel panel = trackerPanel;
      if (panel instanceof WorldTView) {
        panel = ((WorldTView)panel).getTrackerPanel();
      }
      boolean xMass = TToolBar.getToolbar(panel).xMassButton.isSelected();
      String s = track.getName() + " "; //$NON-NLS-1$
      if (track instanceof PointMass) {
        PointMass m = (PointMass)track;
        if (m.isVelocity(this)) {
          s = xMass? 
          	TrackerRes.getString("VectorStep.Label.Momentum") + " ": //$NON-NLS-1$ //$NON-NLS-2$  
          	TrackerRes.getString("VectorStep.Label.Velocity") + " "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if (m.isAcceleration(this)) {
          s = xMass? 
          	TrackerRes.getString("VectorStep.Label.NetForce") + " ": //$NON-NLS-1$ //$NON-NLS-2$ 
          	TrackerRes.getString("VectorStep.Label.Acceleration") + " "; //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      if (clip.getStepCount() != 1) {
        s += clip.frameToStep(getFrameNumber());
      }
      TextLayout layout = new TextLayout(s, font, frc);
      textLayouts.put(trackerPanel, layout);
      // get layout position (bottom left corner of text)
      Point lp = getLayoutPosition(trackerPanel, layout);
      Rectangle bounds = layoutBounds.get(trackerPanel);
      if (bounds == null) {
        bounds = new Rectangle();
        layoutBounds.put(trackerPanel, bounds);
      }
      Rectangle2D rect = layout.getBounds();
      // set bounds (top left corner and size)
      bounds.setRect(lp.x, lp.y - rect.getHeight(),
                     rect.getWidth(), rect.getHeight());
      // create basic vector mark
      mark = footprint.getMark(screenPoints);
      // if selected, draw selection shape on top of basic mark
      if (p != null) {
        transform.setToTranslation(p.x, p.y);
        final Color color = footprint.getColor();
        final Mark stepMark = mark;
        final Shape selectedShape
          = transform.createTransformedShape(selectionShape);
        mark = new Mark() {
          public void draw(Graphics2D g, boolean highlighted) {
            stepMark.draw(g, highlighted);
            Paint gpaint = g.getPaint();
            g.setPaint(color);
            g.fill(selectedShape);
            g.setPaint(gpaint);
          }

          public Rectangle getBounds(boolean highlighted) {
            Rectangle bounds = selectedShape.getBounds();
            bounds.add(stepMark.getBounds(false));
            return bounds;
          }
        };
      }
      final Mark theMark = mark;
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
        	if (!valid) return;
          theMark.draw(g, highlighted);
        }
        public Rectangle getBounds(boolean highlighted) {
          return theMark.getBounds(highlighted);
        }
      };
      marks.put(trackerPanel, mark);
      if (valid) {
	      Shape[] shapes = footprint.getHitShapes();
	      tipShapes.put(trackerPanel, shapes[0]);
	      shaftShapes.put(trackerPanel, shapes[2]);
      }
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
    return bounds;
  }

  /**
   * Responds to property change events. VectorStep receives the
   * following events: "location" from an attached TPoint.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource() == attachmentPoint)   // from TPoint
      tail.setXY(attachmentPoint.getX(), attachmentPoint.getY());
  }

  /**
   * Sets firePropertyChangeEvents flag.
   *
   * @param fireEvents <code>true</code> to request this to fire property
   * change events
   */
  public void setFirePropertyChangeEvents(boolean fireEvents) {
    firePropertyChangeEvents = fireEvents;
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public VectorStep clone() {
    VectorStep step = (VectorStep)super.clone();
    if (step != null) {
      step.points[0] = step.tip = step.new Tip(tip.getX(), tip.getY());
      step.points[1] = step.tail = step.new Handle(tail.getX(), tail.getY());
      step.points[2] = step.handle = step.new Handle(handle.getX(), handle.getY());
      step.points[3] = step.hinge = step.new Hinge(hinge.getX(), hinge.getY());
      step.points[4] = step.middle = new TPoint(middle.getX(), middle.getY()) {
        public int getFrameNumber(VideoPanel vidPanel) {
          return VectorStep.this.n;
        }
      };
      step.tipShapes = new HashMap<TrackerPanel, Shape>();
      step.shaftShapes = new HashMap<TrackerPanel, Shape>();
      step.textLayouts = new HashMap<TrackerPanel, TextLayout>();
      step.layoutBounds = new HashMap<TrackerPanel, Rectangle>();
      step.setFirePropertyChangeEvents(firePropertyChangeEvents);
    }
    return step;
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "VectorStep " + n //$NON-NLS-1$
            + " [" + format.format(tail.x) //$NON-NLS-1$
            + ", " + format.format(tail.y) //$NON-NLS-1$
            + ", " + format.format(getXComponent()) //$NON-NLS-1$
            + ", " + format.format(getYComponent()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Gets TextLayout screen position.
   *
   * @param trackerPanel the tracker panel
   * @param layout the text layout
   * @return the screen position point
   */
  private Point getLayoutPosition(TrackerPanel trackerPanel,
                                  TextLayout layout) {
    Point p = middle.getScreenPosition(trackerPanel);
    // move p on WorldTView if snapped to origin
  	if (trackerPanel instanceof WorldTView &&
  			attachmentPoint == ((WorldTView)trackerPanel).getSnapPoint()) {
  		p.x += dx;
      p.y += dy;
    }
    Rectangle2D bounds = layout.getBounds();
    double w = bounds.getWidth();
    double h = bounds.getHeight();
    // the following code is to find the line angle on a world view
  	tipPoint.setLocation(tip);
  	tailPoint.setLocation(tail);
    if (!trackerPanel.isDrawingInImageSpace()) {
    	AffineTransform at = trackerPanel.getCoords().getToWorldTransform(n);
    	at.transform(tipPoint, tipPoint);
    	tipPoint.y = -tipPoint.y;
    	at.transform(tailPoint, tailPoint);
    	tailPoint.y = -tailPoint.y;
    }
    double cos = tailPoint.cos(tipPoint);
    double sin = tailPoint.sin(tipPoint);
    double d = 4 + Math.abs(w*sin/2) + Math.abs(h*cos/2);
    if (cos >= 0)    // first/fourth quadrants
      p.setLocation((int)(p.x - d*sin - w/2), (int)(p.y - d*cos + h/2));
    else             // second/third quadrants
      p.setLocation((int)(p.x + d*sin - w/2), (int)(p.y + d*cos + h/2));
    return p;
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 5;
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
      setStepEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method to move both tip and tail.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      double dx = x - getX();
      double dy = y - getY();
      tail.setLocation(tail.getX() + dx, tail.getY() + dy);
      tip.setLocation(tip.getX() + dx, tip.getY() + dy);
      setLocation(x, y);
      // detach if moved away from attachment point
      if (attachmentPoint != null &&
          attachmentPoint.distance(tail) > 1) {
        // attempt to break chain if this is linked
        if (chain != null && attachmentPoint instanceof Hinge) {
          chain.breakAt(VectorStep.this);
        }
        else attach(null);
      }
      if (firePropertyChangeEvents)
        track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
      repaint();
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing this step
     * @return the containing VectorStep frame number
     */
    public int getFrameNumber(VideoPanel vidPanel) {
    	return n;
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      tip.showCoordinates(vidPanel);
      super.showCoordinates(vidPanel);
    }

    /**
     * Snaps to nearby attachment point, if any.
     *
     * @param trackerPanel the tracker panel with attachment points
     */
    public void snap(TrackerPanel trackerPanel) {
      VectorStep.this.snap(trackerPanel);
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
    	setPositionOnLine(xScreen, yScreen, trackerPanel, hinge, tail);
    }

    /**
     * Returns true if this vector is very short.
     *
     * @return true if short
     */
    public boolean isShort() {
    	return tip.distanceSq(tail) < 25;
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
    }

    /**
     * Overrides TPoint setLocation method.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setLocation(double x, double y) {
      super.setLocation(x, y);
      hinge.setHingeLocation();
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (track.isLocked())
        return;
      super.setXY(x, y);
      if (firePropertyChangeEvents)
        track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
      repaint();
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      vidPanel.hideMouseBox();
      // put vector components into x and y fields
      ImageCoordSystem coords = vidPanel.getCoords();
      double x = coords.imageToWorldXComponent(n, getXComponent(),
                                               getYComponent());
      double y = coords.imageToWorldYComponent(n, getXComponent(),
                                               getYComponent());
      if (track instanceof PointMass) {
        TrackerPanel trackerPanel = (TrackerPanel)vidPanel;
        PointMass m = (PointMass) track;
        if (m.isVelocity(VectorStep.this)) {
          double dt = vidPanel.getPlayer().getStepTime(1) / 1000;
          x = x / dt;
          y = y / dt;
        }
        else if (m.isAcceleration(VectorStep.this)) {
          double dt = vidPanel.getPlayer().getStepTime(1) / 1000;
          x = x / (dt * dt);
          y = y / (dt * dt);
        }
        if (TToolBar.getToolbar(trackerPanel).xMassButton.isSelected()) {
          x = m.getMass() * x;
          y = m.getMass() * y;
        }
      }
      track.xField.setValue(x);
      track.yField.setValue(y);
      track.magField.setValue(Math.sqrt(x * x + y * y));
      double theta = Math.atan2(y, x);
      track.angleField.setValue(theta);
      super.showCoordinates(vidPanel);
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing this step
     * @return the frame number
     */
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }
    
  }

//______________________ inner Hinge class ________________________

  class Hinge extends TPoint {

    /**
     * Constructs a Hinge with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Hinge(double x, double y) {
      super(x, y);
      setStepEditTrigger(true);
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      tip.showCoordinates(vidPanel);
      super.showCoordinates(vidPanel);
    }

    /**
     * Gets this hinge's parent vector step.
     *
     * @return the step
     */
    public VectorStep getStep() {
      return VectorStep.this;
    }

    /**
     * Sets the location relative to the tail and tip.
     */
    public void setHingeLocation() {
      double stretch = 1;
      if (footprint instanceof ArrowFootprint) {
        ArrowFootprint arrow = (ArrowFootprint) footprint;
        stretch = arrow.getStretch();
      }
      double x = getTail().getX() + stretch * (getTip().getX() - getTail().getX());
      double y = getTail().getY() + stretch * (getTip().getY() - getTail().getY());
      setLocation(x, y);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      double stretch = 1;
      if (footprint instanceof ArrowFootprint) {
        ArrowFootprint arrow = (ArrowFootprint) footprint;
        stretch = arrow.getStretch();
      }
      double xx = getTail().getX() + (x - getTail().getX()) / stretch;
      double yy = getTail().getY() + (y - getTail().getY()) / stretch;
      tip.setXY(xx, yy);
    }

    /**
     * Overrides TPoint setLocation method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setLocation(double x, double y) {
      super.setLocation(x, y);
      // move any linked vectors
      VectorChain chain = VectorStep.this.chain;
      if (chain != null) {
        int i = chain.indexOf(VectorStep.this);
        if (i < chain.size() - 1) {
          VectorStep vec = chain.get(i + 1);
          vec.getTail().setXY(x, y);
          return;
        }
      }
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel drawing this step
     * @return the frame number
     */
    public int getFrameNumber(VideoPanel vidPanel) {
    	return n;
    }
  }

//__________________________ static methods ___________________________

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
      VectorStep step = (VectorStep) obj;
      boolean snap = (step.attachmentPoint != null);
      if (snap) control.setValue("snap", snap); //$NON-NLS-1$
      control.setValue("xtail", step.getTail().x); //$NON-NLS-1$
      control.setValue("ytail", step.getTail().y); //$NON-NLS-1$
      if (!step.getTrack().isDependent() && 
      				!(step.getTrack() instanceof PointMass)) {
        control.setValue("xtip", step.getTip().x); //$NON-NLS-1$
        control.setValue("ytip", step.getTip().y); //$NON-NLS-1$      	
      }
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	// this loader is not intended to be used to create new steps,
    	// but only for undo/redo step edits. This is because the constructor
    	// requires knowledge of the Vector track this step belongs to.
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
      VectorStep step = (VectorStep) obj;
      double x = control.getDouble("xtail"); //$NON-NLS-1$
      double y = control.getDouble("ytail"); //$NON-NLS-1$
      step.getTail().setXY(x, y);
      if (!step.getTrack().isDependent() && 
      				!(step.getTrack() instanceof PointMass)) {
        x = control.getDouble("xtip"); //$NON-NLS-1$
        y = control.getDouble("ytip"); //$NON-NLS-1$
        step.getTip().setXY(x, y);
      }
      if (control.getBoolean("snap")) //$NON-NLS-1$
      	step.snap(step.getTrack().trackerPanel); 
      return obj;
    }
  }
}

