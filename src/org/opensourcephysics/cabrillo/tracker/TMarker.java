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
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Timer;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.TPoint;

public class TMarker extends TPoint implements PropertyChangeListener {
	
	private static final Color paleBody, body, paleWhite, white, paleDark, dark, paleGreen;
	static {
    paleBody = new Color(160, 160, 255, 40);
    body = new Color(160, 160, 255, 80);
    paleWhite = new Color(255, 255, 255, 80);
    white = new Color(255, 255, 255, 200);
    paleDark = new Color(50, 50, 50, 70);
    dark = new Color(50, 50, 50, 200);		
    paleGreen = new Color(50, 255, 50, 150);		
	}
		
	private Ellipse2D.Double outerCircle, innerCircle;
	private Arc2D.Double innerArc, outerArc;
	private int panelID;
	private TFrame frame;
	protected Handle handle;
	protected ZoomControl zoomControl;
	protected Marker marker;
	protected Color markerColor = paleDark;
	protected TPoint markedPoint;
	protected boolean refreshing, visible, isMarking, wasOn, on;
	private int outerR = 80, innerR = 30, wedgeWidth = 15;
	private double dialStart = 30, currentDialAngle;
	private int centerR = 3;
	private Path2D dialWedgePath, ringPath, markingWedgePath;
	private Stroke width1, width2, width3;
	private Point2D screenPt2D;
	private MouseListener myMouseListener;
	private double zoomFactor = 1, zoomMax = 4, zoomMin = 1;
	private Timer wasOnTimer;
	private String hint;
		
	public TMarker(TrackerPanel panel) {
		panelID = panel.getID();
		frame = panel.getTFrame();
		outerCircle = new Ellipse2D.Double();
		innerCircle = new Ellipse2D.Double();
		innerArc = new Arc2D.Double();
		outerArc = new Arc2D.Double();
		handle = new Handle();
		zoomControl = new ZoomControl();
		marker = new Marker();
		screenPt2D = new Point2D.Double();
		ringPath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		dialWedgePath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		markingWedgePath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
    width1 = new BasicStroke(1.0f);
    width2 = new BasicStroke(2.0f);
    width3 = new BasicStroke(3.0f);
    panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this);
    panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this);
		createMyMouseListener();
    setZoomFactor(zoomMin);
    wasOnTimer = new Timer(300, e -> { wasOn = false; });
    // center on panel mat
    Rectangle rect = panel.getMat().getBounds();    
    setXY(rect.getWidth() / 2, rect.getHeight() / 2);
	}
	
	private void createMyMouseListener() {
    myMouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
        if (isMarking) {
          return; 
        }	
        if (outerCircle.contains(e.getX(), e.getY())
        		&& panel().getSelectedTrack() != null 
        		&& !panel().getSelectedTrack().isMarking) {
        	panel().getSelectedTrack().setMarking(true);
        }
				if (dialWedgePath.contains(e.getX(), e.getY())) {
					zoomControl.setXY(zoomControl.getX(), zoomControl.getY());
				}
				else if (panel().getSelectedPoint()==marker
						&& markingWedgePath.contains(e.getX(), e.getY())) {
			    Point p = getScreenPosition(panel());
	        // create a copy of the event with target pixel positions
	        MouseEvent forwardedEvent = new MouseEvent(
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

	        // dispatch the new event to the TrackerPanel's listeners
          try {
	          isMarking = true;
	          markedPoint = null;
  	        panel().dispatchEvent(forwardedEvent);
	        } finally {
	            // reset isMarking in a finally block
	        	isMarking = false;
	        	panel().setSelectedPoint(null);
	        }
				}
			}
		};
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
			boolean isUnselected = (old==marker || old==handle || old==zoomControl) 
					&& nu == null;
			// set wasOn so this can be reselected when stepping quickly to new frame
			// but start timer to set wasOn false after short time so NOT reselected if not on
			wasOn = old==marker || old==handle || old==zoomControl 
					|| (old==markedPoint && old!= null);
			wasOnTimer.restart();
			
			on = (wasOn || isSelected) && !isUnselected && panel().getSelectedTrack()!=null;
						
			if (nu != zoomControl) {
				TFrame.repaintT(panel());
			}
		}
		else if (e.getPropertyName().equals(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER)) {
			// reselect the handle if this was on previously (a short time ago)
			if (wasOn) {
				panel().setSelectedPoint(handle);
			}
		}
	}

	private TrackerPanel panel() {
		return frame.getTrackerPanelForID(panelID);		
	}
	
	protected double getZoomFactor() {
		return zoomFactor;
	}

	protected double setZoomFactor(double zoom) {
		zoomFactor = Math.max(zoom, zoomMin);
		zoomFactor = Math.min(zoomFactor, zoomMax);	
		double power = Math.log(zoomFactor) / Math.log(2);
		double theta = power * Math.PI / 3;
		currentDialAngle = (180-dialStart)*Math.PI/180 - theta;
		return zoomFactor;
	}
	
	protected String getMessage() {
		if (panel().getSelectedTrack() == null)
			return TrackerRes.getString("TMarker.NoSelectedTrack.Hint");
		return hint;
	}
	
	/**
	 * Refreshes the path for a wedge shape: dial wedge and marking wedge.
	 *
	 * @param path the path to refresh
	 * @param center screen position of center
	 * @param start the start angle in degrees ccw from horizontal right
	 * @param ang the sweep angle in degrees
	 * @param roundEnds true to round the wedge ends
	 */
	private void refreshWedgePath(Path2D path, Point center, double start, double ang, boolean roundEnds) {
		outerArc.setArcByCenter(center.x, center.y, innerR+wedgeWidth, start, ang, Arc2D.OPEN);
		innerArc.setArcByCenter(center.x, center.y, innerR, start + ang, -ang, Arc2D.OPEN);		
		path.reset();
		path.append(outerArc, true);
		if (roundEnds) {
			// pig
			// at start and end angle of innerArc, find points on inner circle
	    Point p = getScreenPosition(panel());
      int x = (int)(p.getX()+(innerR)*Math.cos(start*Math.PI/180));
      int y = (int)(p.getY()-(innerR)*Math.sin(start*Math.PI/180));

			
			// use point as the center of an arc with radius wedgeWidth
			
			outerArc.setArcByCenter(x, y, wedgeWidth, 0, Math.PI, Arc2D.OPEN);			
			path.append(outerArc, true);
		}
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
		
		g2d.setColor(on? body: paleBody);
    g2d.fill(ringPath);
    
    if (on) {
    	// draw dial
    	refreshWedgePath(dialWedgePath, p, dialStart, 120, false);   
    	g2d.setColor(paleWhite);
      g2d.draw(dialWedgePath);
      
  		// draw marking wedge
      refreshWedgePath(markingWedgePath, p, 210, 120, false);
  		g2d.setColor(markerColor);
      g2d.fill(markingWedgePath);
    	g2d.setColor(paleWhite);
      g2d.draw(markingWedgePath);
      
      // draw zoom dial indicator
      g2d.setColor(white);
      g2d.setStroke(width3);
      int x1 = (int)(p.getX()+(innerR+3)*Math.cos(currentDialAngle));
      int y1 = (int)(p.getY()-(innerR+3)*Math.sin(currentDialAngle));
      int x2 = (int)(p.getX()+(innerR+wedgeWidth-3)*Math.cos(currentDialAngle));
      int y2 = (int)(p.getY()-(innerR+wedgeWidth-3)*Math.sin(currentDialAngle));
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
	    g2d.setColor(white);
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
    g2d.setColor(on? white: paleWhite);
    g2d.draw(outerCircle);
    g2d.draw(innerCircle);
    
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
		
		if (on && markingWedgePath.contains(xpix, ypix)
				&& panel().getSelectedTrack() != null) {
			refreshing = false;
			hint = TrackerRes.getString("TMarker.Marker.Hint");
			if (markerColor != paleGreen) {
				markerColor = paleGreen;
				TFrame.repaintT(panel());
			}
			return marker;			
		}
		if (markerColor != paleDark) {
			markerColor = paleDark;
			TFrame.repaintT(panel());
		}
		markerColor = paleDark;
		if (dialWedgePath.contains(xpix, ypix)) {
			refreshing = true;
			zoomControl.setScreenPosition(xpix, ypix, panel());
			refreshing = false;
			hint = TrackerRes.getString("TMarker.Zoom.Hint");
			return zoomControl;			
		}
		if (outerCircle.contains(xpix, ypix)) {
			refreshing = true;
			handle.setScreenPosition(xpix, ypix, panel());
			refreshing = false;
			hint = TrackerRes.getString("TMarker.Handle.Hint");
			return handle;
		}
		return null;
	}

	@Override
	public void setXY(double x, double y) {
		super.setXY(x, y);
	}
	
	public void setVisible(boolean vis) {
		visible = vis;
		if (vis)
			panel().addMouseListener(myMouseListener);
		else
			panel().removeMouseListener(myMouseListener);
		TFrame.repaintT(panel());
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	class Member extends TPoint {
		
		public TMarker getTarget() {
      // returns the enclosing instance
      return TMarker.this; 
		}

	}

	
	class Handle extends Member {

		/**
		 * Overrides TPoint setXY method to move this marker.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */
		@Override
		public void setXY(double x, double y) {
			double dx = x - getX();
			double dy = y - getY();
			setLocation(x, y);
			if (!refreshing) {
				// move the target
				TMarker tm = TMarker.this; 
				tm.setXY(tm.getX()+dx, tm.getY()+dy);
			}
		}
		
		@Override
		public String toString() {
			return "handle ";
		}
		
	}

	class ZoomControl extends Member {

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
				double theta = TMarker.this.angle(this) + 5*Math.PI/6;
				currentDialAngle = (180-dialStart)*Math.PI/180 - theta;
				double power = 3 * theta / Math.PI;
				zoomFactor = Math.pow(2, power);
				zoomFactor = Math.max(zoomFactor, zoomMin);
				zoomFactor = Math.min(zoomFactor, zoomMax);
				setZoomFactor(zoomFactor);
				TFrame.repaintT(panel());
			}
		}	
		
		@Override
		public String toString() {
			return "zoom ";
		}
		
	}
	
	class Marker extends Member {

		@Override
		public String toString() {
			return "marker ";
		}
		
	}

}
