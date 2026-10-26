package org.opensourcephysics.cabrillo.tracker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.TPoint;

/**
 * This is a marking tool that applies mouse or touch track-marking actions 
 * to a crosshair at the center of an adjustable magnifier.
 * 
 * @author Doug Brown
 */
public class Crosshair extends TPoint implements PropertyChangeListener {
	
	private static final Color paleBody, body, paleWhite, moreWhite, paleBlue, blue, dark;
	static {
    paleBody = new Color(160, 160, 255, 40);
    body = new Color(160, 160, 255, 80);
    paleWhite = new Color(255, 255, 255, 80);
    moreWhite = new Color(255, 255, 255, 150);
    paleBlue = new Color(50, 50, 255, 70);
    blue = new Color(50, 50, 255, 140);		
    dark = new Color(50, 50, 50, 200);		
	}
	private static double toRadians = Math.PI/180;
		
	private Ellipse2D.Double outerCircle, innerCircle;
	private Arc2D.Double innerArc, outerArc;
	private int panelID;
	private TFrame frame;
	private Handle handle;
	private ZoomControl zoomControl;
	private Control marker;
	private Color markerEdgeColor = paleBlue, dialEdgeColor = paleWhite;
	private TPoint markedPoint;
	private Step markedStep;
	private boolean refreshing, visible, isMarking, wasOn, isOn;
	private int outerR = 110, innerR = 40, markerRadialWidth = 30, markerDegreeWidth = 45;
	private int dialRadialWidth = 20, dialDegreeWidth = 90;
	private double currentDialAngle; // ccw from +horizontal
	private int centerR = 3;
	private Path2D dialSector, ringPath, markingSector;
	private Stroke width1, width2, width3;
	private Point2D screenPt2D;
	private MyMouseListener myMouseListener;
	private double zoomFactor = 1, zoomPowerMax = 2, zoomPowerMin = 0;
	private Timer wasOnTimer;
	private String hint, selectedName;
	private JPopupMenu popup;
		
	public Crosshair(TrackerPanel panel) {
		panelID = panel.getID();
		frame = panel.getTFrame();
		outerCircle = new Ellipse2D.Double();
		innerCircle = new Ellipse2D.Double();
		innerArc = new Arc2D.Double();
		outerArc = new Arc2D.Double();
		handle = new Handle();
		zoomControl = new ZoomControl();
		marker = new Control();
		screenPt2D = new Point2D.Double();
		ringPath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		dialSector = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		markingSector = new Path2D.Double(Path2D.WIND_EVEN_ODD);
    width1 = new BasicStroke(1.0f);
    width2 = new BasicStroke(2.0f);
    width3 = new BasicStroke(3.0f);
    panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, this);
    panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this);
    panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this);
    myMouseListener = new MyMouseListener();
    setZoomFactor(Math.pow(2, (zoomPowerMax-zoomPowerMin)/2));
    
    wasOnTimer = new Timer(300, e -> { wasOn = false; });
    popup = new JPopupMenu();
    // center on panel mat
    Rectangle rect = panel.getMat().getBounds();    
    setXY(rect.getWidth() / 2, rect.getHeight() / 2);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (!isVisible()) return;
		// typical sequence of selected points when marking:
		// handle-marker-markedPoint-null
		if (e.getPropertyName().equals(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT)) {
			Object nu = e.getNewValue();
			Object old = e.getOldValue();
			boolean isSelected = nu==marker || nu==handle || nu==zoomControl 
					|| (nu==markedPoint && nu!= null);
			boolean isUnselected = !isSelected && (old==marker || old==handle || old==zoomControl) 
					&& (nu == null || nu!=markedPoint);
			// set wasOn so this can be reselected when stepping quickly to new frame
			// but start timer to set wasOn false after short time so NOT reselected if not on
			wasOn = old==marker || old==handle || old==zoomControl 
					|| (old==markedPoint && old != null);
			wasOnTimer.restart();
			
			isOn = (wasOn || isSelected) && !isUnselected;
						
			if (nu != zoomControl) {
				TFrame.repaintT(panel());
			}
			if (wasOn && !isOn) {
	    	// see if selected track is polygon RGBRegion
				TTrack track = panel().getSelectedTrack();
				if (track != null && track.ttype == TTrack.TYPE_RGBREGION) {
					RGBRegion reg = (RGBRegion)track;
					if (reg.shapeType == RGBRegion.SHAPE_POLYGON) {
						RGBStep step = (RGBStep)reg.getStep(panel().getFrameNumber());
						if (step != null && !step.isPolygonClosed()) {
							reg.checkPolygonEditing();
						}
					}
				}
			}
		}
		else if (e.getPropertyName().equals(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER)) {
			// reselect the handle if this was on previously (a short time ago)
			if (wasOn) {
				panel().setSelectedPoint(handle);
			}
		}
		else if (e.getPropertyName().equals(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK)) {
			setMarkedPoint(null);
			// if newly selected track is markable, turn on
			TTrack track = panel().getSelectedTrack();
			if (track!= null) {
				isOn = true;
			}
		}
	}

	private TrackerPanel panel() {
		return frame.getTrackerPanelForID(panelID);		
	}
	
	protected JPopupMenu getPopup() {
		popup.removeAll();
		// add items
		TMenuBar menubar = frame.getMenuBar(panelID, false);
		JMenuItem item = menubar.getMenuItem("track_crosshairItem"); //$NON-NLS-1$
		item.setSelected(panel().getCrosshair().isVisible());
		popup.add(item);
		return popup;
	}
	
	protected String getMessage() {
		if (!isVisible()) return "";
		return TrackerRes.getString("TMenuBar.MenuItem.Crosshair")
				+ selectedName + " (" + hint + ")";
	}
	
	protected TPoint getMarkedPoint() {
		return markedPoint;
	}
	
	/**
	 * Refreshes the path for a dial or marking sector.
	 *
	 * @param path the path to refresh
	 * @param center screen position of center
	 * @param start the start angle in degrees ccw from horizontal right
	 * @param ang the sweep angle in degrees
	 */
	private void refreshSector(Path2D path, Point center, double start, double ang, int radialW) {
		outerArc.setArcByCenter(center.x, center.y, innerR+radialW, start, ang, Arc2D.OPEN);
		innerArc.setArcByCenter(center.x, center.y, innerR, start + ang, -ang, Arc2D.OPEN);		
		path.reset();
		path.append(outerArc, true);
		path.append(innerArc, true);
		path.closePath();
	}

	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (!isVisible()) return;
		
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
    Point p = getScreenPosition(panel());
		
		// refresh ring path and fill it
		outerCircle.setFrameFromCenter(p.x, p.y, p.x-outerR, p.y-outerR);
		innerCircle.setFrameFromCenter(p.x, p.y, p.x-innerR, p.y-innerR);
		ringPath.reset();
    ringPath.append(outerCircle, false);
    ringPath.append(innerCircle, false);
		
		g2d.setColor(isOn? body: paleBody);
    g2d.fill(ringPath);
    
    if (isOn) {
    	// draw dial sector
    	int ang = (180-dialDegreeWidth)/2;
    	refreshSector(dialSector, p, ang, dialDegreeWidth, dialRadialWidth); 
      g2d.setColor(paleBody);
      g2d.fill(dialSector);
    	g2d.setColor(dialEdgeColor);
      g2d.draw(dialSector);
      
  		// draw marking sector
    	ang = (180-markerDegreeWidth)/2;
      refreshSector(markingSector, p, 180 + ang, markerDegreeWidth, markerRadialWidth);
      if (panel().getSelectedTrack() != null) {
	  		g2d.setColor(paleBlue);
	      g2d.fill(markingSector);
	    	g2d.setColor(markerEdgeColor);
	      g2d.draw(markingSector);
      }
      
      // draw dial indicator
      int x1 = (int)(p.getX()+(innerR+3)*Math.cos(currentDialAngle));
      int y1 = (int)(p.getY()-(innerR+3)*Math.sin(currentDialAngle));
      int x2 = (int)(p.getX()+(innerR+dialRadialWidth-3)*Math.cos(currentDialAngle));
      int y2 = (int)(p.getY()-(innerR+dialRadialWidth-3)*Math.sin(currentDialAngle));
      g2d.setColor(blue);
      g2d.setStroke(width3);
      g2d.drawLine(x1, y1, x2, y2);
      g2d.setColor(Color.white);
      g2d.setStroke(width1);
      g2d.drawLine(x1, y1, x2, y2);
 
      // draw the magnified video image
			if (panel().getVideo() != null) {
				BufferedImage image = panel().getVideo().getImage();
	      // get a new Graphics2D for the lens
	      Graphics2D lensG2d = (Graphics2D) g2d.create();
	      
	      // apply the circular clip
	      lensG2d.setClip(innerCircle);
	
	      // set up the transformation matrix
	  		toScreen.transform(this, screenPt2D);
	      lensG2d.translate(screenPt2D.getX(), screenPt2D.getY());
	      double scale = getZoomFactor() * panel().getMagnification();
	      lensG2d.scale(scale, scale);
	      lensG2d.translate(-this.x, -this.y);
	
	      // draw the image
	      lensG2d.drawImage(image, 0, 0, null);
	      lensG2d.dispose();
			}
	
	    // draw crosshair with light outline
	    g2d.setColor(moreWhite);
	    g2d.setStroke(width2);
	    g2d.drawLine(p.x-innerR+2, p.y, p.x-centerR-2, p.y);
	    g2d.drawLine(p.x+centerR+2, p.y, p.x+innerR-2, p.y);
	    g2d.drawLine(p.x, p.y+innerR-2, p.x, p.y+centerR+2);
	    g2d.drawLine(p.x, p.y-centerR-2, p.x, p.y-innerR+2);
	    g2d.setColor(dark);
	    g2d.setStroke(width1);
	    g2d.drawLine(p.x-innerR, p.y, p.x-centerR, p.y);
	    g2d.drawLine(p.x+centerR, p.y, p.x+innerR, p.y);
	    g2d.drawLine(p.x, p.y+innerR, p.x, p.y+centerR);
	    g2d.drawLine(p.x, p.y-centerR, p.x, p.y-innerR);
    }

    // draw inner/outer edges last
    g2d.setColor(isOn? Color.white: paleWhite);
    g2d.draw(outerCircle);
    g2d.draw(innerCircle);
    
    // draw marked step last
    if (markedStep != null) {
    	markedStep.draw(panel, g2d);
    }
    
	}

	@Override
	public double getXMin() {
		return outerCircle.getMinX();
	}

	@Override
	public double getXMax() {
		return outerCircle.getMaxX();
	}

	@Override
	public double getYMin() {
		return outerCircle.getMinY();
	}

	@Override
	public double getYMax() {
		return outerCircle.getMaxY();
	}

	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		if (!isVisible()) return null;
		
		if (isOn && markingSector.contains(xpix, ypix)
				&& panel().getSelectedTrack() != null) {
			refreshing = false;
			hint = TrackerRes.getString("Crosshair.Marker.Hint");
			selectedName = " "+TrackerRes.getString("Crosshair.Marker.Name");
			if (markerEdgeColor != Color.white) {
				markerEdgeColor = Color.white;
				TFrame.repaintT(panel());
			}
			return marker;			
		}
		if (markerEdgeColor != paleWhite) {
			markerEdgeColor = paleWhite;
			TFrame.repaintT(panel());
		}

		if (dialSector.contains(xpix, ypix)) {
			refreshing = true;
			zoomControl.setScreenPosition(xpix, ypix, panel());
			refreshing = false;
			hint = isOn? TrackerRes.getString("Crosshair.Zoom.Hint"):
				TrackerRes.getString("Crosshair.Handle.Hint");
			selectedName = isOn? " "+TrackerRes.getString("Crosshair.Zoom.Name"): "";
			if (dialEdgeColor != Color.white) {
				dialEdgeColor = Color.white;
				TFrame.repaintT(panel());
			}
			return zoomControl;			
		}
		if (dialEdgeColor != paleWhite) {
			dialEdgeColor = paleWhite;
			TFrame.repaintT(panel());
		}
		boolean isIncompleteTape = panel().getSelectedTrack() != null
				&& panel().getSelectedTrack().ttype == TTrack.TYPE_TAPEMEASURE;
		if (isIncompleteTape) {
			TapeMeasure tape = (TapeMeasure)panel().getSelectedTrack();
			isIncompleteTape = tape.isIncomplete;
		}
		
		if (outerCircle.contains(xpix, ypix) && !isIncompleteTape) {
			refreshing = true;
			handle.setScreenPosition(xpix, ypix, panel());
			refreshing = false;
			hint = panel().getSelectedTrack() != null?
					TrackerRes.getString("Crosshair.Handle.Hint"):
				TrackerRes.getString("Crosshair.NoSelectedTrack.Hint");
			selectedName = "";
			return handle;
		}
		return null;
	}
	
	public boolean contains(MouseEvent e) {
		return outerCircle.contains(e.getX(), e.getY());
	}

	public void setVisible(boolean vis) {
		visible = vis;
		if (vis) {
			panel().addMouseListener(myMouseListener);
			panel().addMouseMotionListener(myMouseListener);
			panel().addDrawable(this);
			isOn = true;
		}
		else {
			panel().removeMouseListener(myMouseListener);
			panel().removeMouseMotionListener(myMouseListener);
			panel().removeDrawable(this);
			panel().setMessage("");
			isOn = false;
			TPoint p = panel().getSelectedPoint();
			if (p==handle || p==marker)
				panel().setSelectedPoint(null);
		}
		TFrame.repaintT(panel());
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public boolean isMarking() {
		return isMarking;
	}
	
	public boolean isOn() {
		return isOn;
	}
	
	protected void setMarkedPoint(TPoint p) {
		markedPoint = p;
		markedStep = null;
    if (markedPoint != null) {
    	panel().setSelectedPoint(markedPoint);
			TTrack track = panel().getSelectedTrack();
			if (track != null) {
				markedStep = track.getStep(markedPoint, panel());
			}
    }
    TFrame.repaintT(panel());
	}
	
	protected double getZoomFactor() {
		return zoomFactor;
	}

	protected void setZoomFactor(double zoom) {
		zoomFactor = Math.max(zoom, Math.pow(2, zoomPowerMin));
		zoomFactor = Math.min(zoomFactor, Math.pow(2, zoomPowerMax));	
		// set dial angle
		double powerFraction = (Math.log(zoomFactor) / (Math.log(2)) / (zoomPowerMax-zoomPowerMin));
  	double dialStartDegrees = (180+dialDegreeWidth) / 2;
  	double dialAngleDegrees = dialStartDegrees - powerFraction * dialDegreeWidth;
		currentDialAngle = dialAngleDegrees*toRadians;
		TFrame.repaintT(panel());
	}
	
	class Control extends TPoint {
		
		public Crosshair getTarget() {
      // returns the enclosing instance
      return Crosshair.this; 
		}

		@Override
		public String toString() {
			String s = this==marker? "marker ":
				this==handle? "handle ":
				"zoom ";
			return s + super.toString();
		}		
		
	}

	
	class Handle extends Control {

		/**
		 * Overrides TPoint setXY method to move the target.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		@Override
		public void setXY(double x, double y) {
			// first move this handle
			double dx = x - getX();
			double dy = y - getY();
			setLocation(x, y);
			// move the target and markedPoint unless refreshing
			Crosshair target = getTarget(); 
			if (!refreshing) {
				target.setXY(target.getX()+dx, target.getY()+dy);
				// move the markedPoint if marking
				if (isMarking && markedPoint != null) {
		      if (panel().getSelectedTrack()!= null
		      		&& panel().getSelectedTrack().ttype == TTrack.TYPE_TAPEMEASURE) {
		      	TapeMeasure tape = (TapeMeasure)panel().getSelectedTrack();
		      	TapeStep step = (TapeStep)tape.getStep(panel().getFrameNumber());
		      	if (step != null) {
		      		markedPoint = step.getEnd2();
		      	}
		      }
					markedPoint.setLocation(target);
				}
			}
		}
		
	}

	class ZoomControl extends Control {

		/**
		 * Overrides TPoint setXY method to set the zoomFactor.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		@Override
		public void setXY(double x, double y) {
			setLocation(x, y);
			if (!refreshing) {
				// change the zoomFactor
				// get dial angle from TPoints, but subtract from pi to flip y-axis 
				double ang = Math.PI - this.angle(getTarget());
				if (ang > 3*Math.PI/2) ang = 0;
				setZoomFactor(getZoomFactor(ang));
			}
		}	
		
		private double getZoomFactor(double dialAngle) {
			double dialDegrees = dialAngle/toRadians;
	  	double dialStartDegrees = (180+dialDegreeWidth) / 2;
			double powerFraction = (dialStartDegrees-dialDegrees) / dialDegreeWidth;
			double power = powerFraction * (zoomPowerMax - zoomPowerMin);
			power = Math.max(Math.min(power, zoomPowerMax), zoomPowerMin);
			return Math.pow(2, power);
		}
		
	}
	
	private class MyMouseListener extends MouseAdapter {
				
		@Override
		public void mousePressed(MouseEvent e) {
      if (isMarking) {
      	// this is AFTER TMouseHandler has marked a point
  			if (markedPoint != null) {
		      if (panel().getSelectedTrack()!= null
		      		&& panel().getSelectedTrack().ttype == TTrack.TYPE_TAPEMEASURE) {
			      if (markedStep != null) {
		      		markedPoint = ((TapeStep)markedStep).getEnd2();
		      	}
		      }
  				markedPoint.setXY(getX(), getY());
  				markedPoint.showCoordinates(panel());
  			}
        return; 
      }	

  		// reset markedPoint
      setMarkedPoint(null);
			if (dialSector.contains(e.getX(), e.getY())) {
				zoomControl.setXY(zoomControl.getX(), zoomControl.getY());
			}
			else if (panel().getSelectedPoint()==marker
					&& markingSector.contains(e.getX(), e.getY())) {
        // set marking true and dispatch a new translated event
        try {
          isMarking = true;
	        panel().dispatchEvent(getTranslatedEvent(e));
        } finally {
        	// refresh handle and select it 
    			refreshing = true;
    			handle.setScreenPosition(e.getX(), e.getY(), panel());
    			refreshing = false;        	
  				panel().setSelectedPoint(handle);
        }
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
    	isMarking = false;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
      // set end position of incomplete TapeMeasure
      if (panel().getSelectedTrack()!= null
      		&& panel().getSelectedTrack().ttype == TTrack.TYPE_TAPEMEASURE) {
      	TapeMeasure tape = (TapeMeasure)panel().getSelectedTrack();
       	if (tape.isIncomplete) {
      		// move the handle to mouse (also moves target), move end2 to (this) target
					// this call refreshes the position of end2 but leaves tape incomplete
    			double x = panel().getMouseX();
    			double y = panel().getMouseY();
    			handle.setXY(x, y); 
					tape.createStep(panel().getFrameNumber(), 0, 0, getX(), getY());
      	}
      }
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (markedPoint != null) {
				// markedPoint could have been dragged by TMouseHandler
				markedPoint.setXY(getX(), getY());
				markedPoint.showCoordinates(panel());
			}
		}
		
		private MouseEvent getTranslatedEvent(MouseEvent e) {
      // return a copy of the event with target screen position
	    Point p = getScreenPosition(panel());
      return new MouseEvent(
              panel(),
              e.getID(),
              e.getWhen(),
              e.getModifiersEx(),
              p.x,       // target x position
              p.y,       // target y position
              e.getClickCount(),
              e.isPopupTrigger(),
              e.getButton()
      );
			
		}
		
	}

}
