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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a step for RGBRegion. It measures video image RGB data within a defined shape.
 *
 * @author Douglas Brown
 */
public class RGBStep extends Step {

  // static fields
	protected static GeneralPath crosshair;
  protected static AlphaComposite composite = AlphaComposite.getInstance(
  		AlphaComposite.SRC_OVER, (float) 0.1);
  protected static TPoint marker;

	
	// instance fields
  protected Position position;
  protected RGBRegion rgbRegion;
  protected int width = RGBRegion.defaultEdgeLength, height = RGBRegion.defaultEdgeLength;
  protected Map<Integer, Shape> panelHitShapes = new HashMap<Integer, Shape>();
  protected Map<Integer, Shape[]> polygonHitShapes = new HashMap<Integer, Shape[]>();
	protected double[] rgbData = new double[8];
	protected boolean dataValid = false;
	protected BasicStroke stroke;
	protected Shape rgbShape;
	protected Polygon2D polygon;

  static {
  	crosshair = new GeneralPath();
  	crosshair.moveTo(0, -3);
  	crosshair.lineTo(0, 3);
  	crosshair.moveTo(-3, 0);
  	crosshair.lineTo(3, 0);
    marker = new TPoint();
  }

  /**
   * Constructs a RGBStep with specified coordinates in image space
   * and width and height in pixels.
   *
   * @param track the track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   * @param w the width
   * @param h the height
   */
  public RGBStep(RGBRegion track, int n, double x, double y, int w, int h) {
    super(track, n);
    width = w;
    height = h;
    rgbRegion = track;
    position = new Position(x, y);
    position.setStepEditTrigger(true);
    points = new TPoint[] {position, track.vertexHandle};
    screenPoints = new Point[getLength()];
    valid = true;
  }

  /**
   * Gets the position TPoint.
   *
   * @return the position TPoint
   */
  public TPoint getPosition() {
    return position;
  }

  /**
   * Overrides Step findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the TPoint that is hit, or null
   */
  @Override
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    Shape hitShape = panelHitShapes.get(trackerPanel.getID());
    if (hitShape != null && hitShape.intersects(hitRect)) 
    	return position;
    if (!isPolygonClosed()) {
	    Shape[] polyshapes = polygonHitShapes.get(trackerPanel.getID());
	    if (polyshapes != null) {
	    	for (int i = 0; i < polyshapes.length; i++) {
	    		if (polyshapes[i].intersects(hitRect)) {
	    			rgbRegion.prepareVertexHandle(this, i);
	    			return rgbRegion.vertexHandle;
	    		}
	    	}
	    }
    }
    return null;
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
    Graphics2D g = (Graphics2D)_g;
    Mark mark = getMark(trackerPanel);
    if (mark != null) {
      mark.draw(g, false);
    }
    // get the RGB data now since this step is being drawn
    // note this method does nothing once RGB data is valid
    getRGBData(trackerPanel);
  }

	/**
	 * Gets the default point. This returns RGBRegion.vertexHandle
	 * when a polygon is being edited.
	 *
	 * @return the default TPoint
	 */
	@Override
	public TPoint getDefaultPoint() {
		if (rgbRegion.shapeType == RGBRegion.SHAPE_POLYGON
				&& polygon != null 
				&& !polygon.isClosed()
				&& polygon.vertices.size() > 1) {
			return rgbRegion.vertexHandle;
		}
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
  	BasicStroke baseStroke = footprint.getStroke();
    int scale = FontSizer.getIntegerFactor();
    float size = Math.min(width, height);
    float lineWidth = Math.min(scale*baseStroke.getLineWidth(), size/3);
    lineWidth = Math.max(lineWidth, baseStroke.getLineWidth());
  	if (stroke==null || stroke.getLineWidth()!=lineWidth) {
  		stroke = new BasicStroke(lineWidth);
  	}
    Mark mark = panelMarks.get(trackerPanel.getID());
    if (mark == null) {
      trackerPanel.getPixelTransform(transform);
      if (!trackerPanel.isDrawingInImageSpace()) {
        transform.concatenate(trackerPanel.getCoords().getToWorldTransform(n));
      }
    	// make region of interest
      Shape region = getRGBShape(position);
      final Shape rgn = transform.createTransformedShape(region);
      
    	polygonHitShapes.remove(trackerPanel.getID());
      if (rgbRegion.shapeType == RGBRegion.SHAPE_POLYGON && polygon != null) {
      	int n = polygon.vertices.size() - (polygon.isClosed()? 2: 1);
      	if (n > 0) {
	      	Shape[] polyshapes = new Shape[n];
	      	for (int i = 0; i < n; i++) {
	      		Point2D pt = polygon.vertices.get(i + 1);
	      		marker.setLocation(position.getX() + pt.getX(), position.getY() + pt.getY());
	      		Point p = marker.getScreenPosition(trackerPanel);
	          transform.setToTranslation(p.x, p.y);
	          polyshapes[i] = transform.createTransformedShape(crosshair);
	      	}
	      	polygonHitShapes.put(trackerPanel.getID(), polyshapes);
      	}
      }
      // center of circle is crosshair or selectionShape
    	Point p = position.getScreenPosition(trackerPanel);
      transform.setToTranslation(p.x, p.y);
      final Shape cross = transform.createTransformedShape(crosshair);
      transform.scale(scale, scale);
      Shape square = position == trackerPanel.getSelectedPoint()?
      	transform.createTransformedShape(selectionShape): null;
      	
      if (rgbRegion.vertexHandle == trackerPanel.getSelectedPoint()) {
      	p = rgbRegion.vertexHandle.getScreenPosition(trackerPanel);
        transform.setToTranslation(p.x, p.y);
        transform.scale(scale, scale);
        square = transform.createTransformedShape(selectionShape);
      }
      final Shape selection = square;

      mark = new Mark() {
        @Override
        public void draw(Graphics2D g, boolean highlighted) {
          Paint gpaint = g.getPaint();
          g.setPaint(footprint.getColor());
          if (OSPRuntime.setRenderingHints) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
          		RenderingHints.VALUE_ANTIALIAS_ON);
          if (selection != null) {
						g.setStroke(selectionStroke);
						g.draw(selection);
						g.setStroke(stroke);
          }
          else {
	          g.setStroke(stroke);
          	g.draw(cross);
          }
          if (rgn != null) {
	          g.draw(rgn);
	          if (rgbRegion.shapeType == RGBRegion.SHAPE_POLYGON && !isPolygonClosed()) {
	            g.setComposite(composite);
	            g.fill(rgn);
	          }
          }
          g.setPaint(gpaint);
        }
      };
      panelMarks.put(trackerPanel.getID(), mark);
      // center is also the hit shape
      panelHitShapes.put(trackerPanel.getID(), cross);
    }
    return mark;
  }

  /**
   * Sets the shape size.
   *
   * @param h the height
   * @param w the width
   */
  public void setShapeSize(double w, double h) {
  	height = (int) h;
  	width = (int) w;
  	rgbShape = null;
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  @Override
public Object clone() {
    RGBStep step = (RGBStep)super.clone();
    if (step != null) {
      step.panelHitShapes = new HashMap<Integer, Shape>();
      step.polygonHitShapes = new HashMap<Integer, Shape[]>();
      step.points[0] = step.position = step.new Position(
      			position.getX(), position.getY());
      step.points[1] = rgbRegion.vertexHandle;
      step.position.setStepEditTrigger(true);
      step.rgbData = new double[8];
      step.dataValid = false;
    }
    return step;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  @Override
  public String toString() {
    return "RGBStep " + n //$NON-NLS-1$
           + " [" + format.format(position.x) //$NON-NLS-1$
           + ", " + format.format(position.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  /**
   * Gets the RGB shape after moving it to a given point.
   *
   * @param pt the point
   * @return the shape
   */
  protected Shape getRGBShape(TPoint pt) {
  	if (rgbShape == null) {
  		createRGBShape(pt);
  	}
  	AffineTransform transform = AffineTransform.getTranslateInstance(pt.getX(), pt.getY());
  	return transform.createTransformedShape(rgbShape);
  }
  
  private void createRGBShape(TPoint pt) {
  	if (rgbRegion.shapeType == RGBRegion.SHAPE_ELLIPSE)
  		rgbShape = new Ellipse2D.Double(-width/2, -height/2, width, height);
  	else if (rgbRegion.shapeType == RGBRegion.SHAPE_POLYGON) {
  		if (polygon == null) {
	  		polygon = new Polygon2D();
	  		append(pt.x, pt.y);
  		}
  		rgbShape = polygon;
  	}
  	else
  		rgbShape = new Rectangle2D.Double(-width/2, -height/2, width, height);
  }
  
  protected void append(double x, double y) {
  	if (polygon == null)
  		polygon = new Polygon2D();
  	polygon.add(x - position.x, y - position.y);
  	if (polygon.vertices.size() < 4 && rgbRegion.tp != null) {
  		rgbRegion.tp.getTrackBar(false).refresh();
  	}
  }
  
  protected boolean isPolygonClosed() {
  	return polygon != null && polygon.isClosed();
  }
  
  protected int getPolygonVertexCount() {
  	return polygon == null? 0: polygon.vertices.size();
  }
  
  protected double[][] getPolygonVertices() {
  	if (polygon == null)
  		return null;
  	return polygon.getVertices();
  }
  
  protected void setPolygonVertices(double[][] vertices) {
  	if (polygon == null)
  		polygon = new Polygon2D();
  	else {
  		polygon.reset();
  		polygon.vertices.clear();
  	}
  	for (int i = 0; i < vertices.length; i++) {
  		if (vertices[i] == null)
  			continue;
  		polygon.add(vertices[i][0], vertices[i][1]);
  	}
  }
  
  /**
   * Gets the RGB data. Return array is {R,G,B,luma,pixels}
   *
   * @param trackerPanel the tracker panel
   * @return an integer array of data values
   */
  public double[] getRGBData(TrackerPanel trackerPanel) {
		Video vid = trackerPanel.getVideo();
		if (vid == null || !vid.isVisible()) return null;
  	if (!dataValid && trackerPanel.getFrameNumber() == n) {
	    BufferedImage image = vid.getImage();
	    if (image != null 
	    			&& image.getType() == BufferedImage.TYPE_INT_RGB) {
	    	RGBStep step = rgbRegion.isFixedPosition()? 
	    				(RGBStep)rgbRegion.getStep(0): this;	
	    	TPoint pt = step.getPosition();
	      Shape region = getRGBShape(pt);
	      if (region == null)
	      	return null;
	      Rectangle rect = region.getBounds();
	      int h = rect.height;
	      int w = rect.width;
        // locate starting pixel
        int x0 = rect.x;
        int y0 = rect.y;
        Point2D centerPt = new Point2D.Double();
	      try {
	        int[] pixels = new int[h*w];
	        int n = 0, r = 0, g = 0, b = 0, r2 = 0, g2 = 0, b2 = 0;
	        // fill pixels array with pixel data
	        image.getRaster().getDataElements(x0, y0, w, h, pixels);
	        // step thru pixels horizontally
	        for (int i = 0; i < w; i++) {
	          // step vertically
	          for (int j = 0; j < h; j++) {
	          	// include pixel if center is inside region
	          	centerPt.setLocation(x0+i+.5, y0+j+.5);
	          	// if image video, check that centerPt is within image bounds
	        		if (vid.getTypeName().equals(VideoType.TYPE_IMAGE)) {
	        			ImageVideo iVid = (ImageVideo) vid;
	        			int wid = iVid.getRGBSize().width;
	        			int ht = iVid.getRGBSize().height;
	        			if (wid < centerPt.getX() || ht < centerPt.getY())
	        				return null;
	        		}
	          	if (region.contains(centerPt)) {
		            int pixel = pixels[i + j*w];
		            n++; // pixel count
		            int rp = (pixel >> 16) & 0xff; // red
		            r += rp;
		            r2 += rp * rp;
		            int gp = (pixel >> 8) & 0xff; // green
		            g += gp;
		            g2 += gp * gp;
		            int bp = (pixel) & 0xff; // blue
		            b += bp;
		            b2 += bp * bp;		            
	          	}
	          }
	        }
	        if (n == 0) return null;
	        double rMean = 1.0*r/n;
	        double rSD = n == 1? Double.NaN: Math.sqrt((r2 - r*rMean) / (n - 1));
	        double gMean = 1.0*g/n;
	        double gSD = n == 1? Double.NaN: Math.sqrt((g2 - g*gMean) / (n - 1));
	        double bMean = 1.0*b/n;
	        double bSD = n == 1? Double.NaN: Math.sqrt((b2 - b*bMean) / (n - 1));
	        rgbData[0] = rMean;
	        rgbData[1] = gMean;
	        rgbData[2] = bMean;
	        rgbData[3] = RGBRegion.getLuma(rMean, gMean, bMean);
	        rgbData[4] = n;
	        rgbData[5] = rSD;
	        rgbData[6] = gSD;
	        rgbData[7] = bSD;
	  	    dataValid = true;
	      } catch(ArrayIndexOutOfBoundsException ex) {return null;}
	    }
  	}
    dataVisible = true;
    return rgbData;
  }

//____________________ inner Position class ______________________

  protected class Position extends TPoint {

    /**
     * Constructs a Position with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Position(double x, double y) {
      super(x, y);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    @Override
    public void setXY(double x, double y) {
    	TTrack track = getTrack();
      if (track.isLocked()) return;
      if (rgbRegion.isFixedPosition()) {
      	RGBStep step = (RGBStep)rgbRegion.steps.getStep(0);
      	step.getPosition().setLocation(x, y); // set location of step 0
  	    step.erase();
      	rgbRegion.refreshStep(RGBStep.this); // set location of this step
    		rgbRegion.clearData(); // all data is invalid
      }
      else {
      	setLocation(x, y);
      	rgbRegion.keyFrames.add(n);
        dataValid = false; // this step's data is invalid      
    	}      
      repaint();
      track.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n)); //$NON-NLS-1$
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    @Override
    public void showCoordinates(VideoPanel vidPanel) {
      // put values into x and y fields
    	TTrack track = getTrack();
      Point2D p = getWorldPosition(vidPanel);
      track.xField.setValue(p.getX());
      track.yField.setValue(p.getY());
      super.showCoordinates(vidPanel);
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel being drawn
     * @return the frame number
     */
    @Override
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }
    
    @Override 
		public void setAdjusting(boolean adjusting, MouseEvent e) {
    	if (!adjusting)
				rgbRegion.checkPolygonEditing();
    }
  }

//__________________________ static methods & classes ___________________________
  
  protected static class Polygon2D extends Path2D.Double {
  	
  	static double[] pts = new double[6];
  	ArrayList<Point2D> vertices = new ArrayList<Point2D>();
  	
  	protected Polygon2D copy() {
  		Polygon2D copy = new Polygon2D();
    	PathIterator it = getPathIterator(null);
  		while(!it.isDone()) {
  			it.currentSegment(pts);
  			copy.add(pts[0], pts[1]);
  			it.next();
  		}
  		return copy;
  	}
  	
  	protected double[][] getVertices() {
    	PathIterator it = getPathIterator(null);
  		double[][] result = new double[vertices.size()][];
  		int i = 0;
  		while(!it.isDone()) {
  			it.currentSegment(pts);
  			result[i] = new double[] {pts[0], pts[1]};
  			it.next();
  			i++;
  		}
  		return result;
  	}
  	
    protected void remove(int vertex) {
    	if (vertices.size() < 2 || vertices.size() < vertex + 1)
    		return;
  		reset();
  		vertices.remove(vertex + 1);
  		ArrayList<Point2D> array = new ArrayList<Point2D>(vertices);
  		vertices.clear();
  		int n = array.size();
    	for (int i = 0; i < n; i++) {
    		Point2D next = array.get(i);
    		add(next.getX(), next.getY());
    	}
    }
    
    protected boolean isClosed() {
    	if (vertices.size() < 3) return false;
    	// closed if last vertex is at (0, 0)
    	Point2D pt = vertices.get(vertices.size() - 1);
    	return pt.getX() == 0 && pt.getY() == 0;
    }
    
    protected void modify() {
     	ArrayList<Point2D> array = new ArrayList<Point2D>(vertices);
    	reset();
    	vertices.clear();
  		int n = array.size();
    	for (int i = 0; i < n; i++) {
    		Point2D next = array.get(i);
    		add(next.getX(), next.getY());
    	}
    }
    
    protected void setClosed(boolean close) {
    	if (close && (isClosed() || vertices.size() < 3))
    		return;
    	if (!close && vertices.size() <= 1)
    		return;
    	if (close)
    		add(0, 0);
    	else {
    		reset();
    		ArrayList<Point2D> array = new ArrayList<Point2D>(vertices);
    		vertices.clear();
    		int n = array.size() - 1;
      	for (int i = 0; i < n; i++) {
      		Point2D next = array.get(i);
      		add(next.getX(), next.getY());
      	}    		
    	}
    }
  	
    protected void add(double x, double y) {
    	if (getCurrentPoint() == null)
    		moveTo(x, y);
    	else
    		lineTo(x, y);
    	vertices.add(getCurrentPoint());
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
    @Override
	public void saveObject(XMLControl control, Object obj) {
      RGBStep step = (RGBStep) obj;
      control.setValue("position", new double[] {step.position.x, step.position.y});
      control.setValue("size", new int[] {step.width, step.height});
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
     * @param obj the object
     * @return the loaded object
     */
    @Override
	public Object loadObject(XMLControl control, Object obj) {
    	RGBStep step = (RGBStep) obj;
      double[] position = (double[]) control.getObject("position");
      step.position.setXY(position[0], position[1]);
      int[] size = (int[]) control.getObject("size");
      step.setShapeSize(size[0],  size[1]);
      step.dataValid = false;
      step.rgbRegion.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(step.n)); //$NON-NLS-1$
    	return obj;
    }
  }
}

