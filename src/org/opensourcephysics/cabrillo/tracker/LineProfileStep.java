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

import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a step for a LineProfile. It is used for obtaining
 * line profile data from a video image.
 *
 * @author Douglas Brown
 */
public class LineProfileStep extends Step {
	
	static TPoint center = new TPoint();

  // instance fields
  protected TPoint lineEnd0;
  protected TPoint lineEnd1;
  protected Handle handle;
  protected boolean endsEnabled = true;
  protected Map<TrackerPanel, Shape> end0Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> end1Shapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> shaftShapes = new HashMap<TrackerPanel, Shape>();
  protected LineProfile line;
  protected Corner[][] corners; // corners at ends 0,1 of sweep lines 0,1
  protected GridIntersection[] endX; // end 0,1 x-intersections
  protected GridIntersection[] endY; // end 0,1 y-intersections
  protected GridIntersection[][] sweepX; // x-intersections along sweep line 0,1
  protected GridIntersection[][] sweepY; // y-intersections along sweep line 0,1
  protected TreeSet<Intersection> sorter = new TreeSet<Intersection>(); // used to sort Intersections
  protected ArrayList<GridVertex> vertices = new ArrayList<GridVertex>(); // GridVertex collection
  private double sin, cos; // sine and cosine of line profile angle
  private double xMin, xMax, yMin, yMax; // borders of the current probe
  private TreeSet<GridSegment> xSegments = new TreeSet<GridSegment>(); // vertical GridSegments
  private TreeSet<GridSegment> ySegments = new TreeSet<GridSegment>(); // horizontal GridSegments
  private int leadingIndex; // toggles between 0,1 to leapfrog 
  private Intersection[] polygon = new Intersection[8]; // polygon shape vertices
  private Point polyLoc = new Point();
  private double[] quadAreas = new double[4]; // used for GridVertex quadrant areas

  /**
   * Constructs a LineProfileStep with specified end point coordinates in image
   * space.
   *
   * @param track the track
   * @param n the frame number
   * @param x1 the x coordinate of end 1
   * @param y1 the y coordinate of end 1
   * @param x2 the x coordinate of end 2
   * @param y2 the y coordinate of end 2
   */
  public LineProfileStep(LineProfile track, int n, 
  				double x1, double y1, double x2, double y2) {
    super(track, n);
    line = track;
    lineEnd0 = new LineEnd(x1, y1);
    lineEnd0.setTrackEditTrigger(false); // to prevent undo when first marked?
    lineEnd1 = new LineEnd(x2, y2);
    handle = new Handle((x1+x2)/2, (y1+y2)/2);
    points = new TPoint[] {lineEnd0, lineEnd1, handle};
    screenPoints = new Point[getLength()];
    xSegments = new TreeSet<GridSegment>();
    ySegments = new TreeSet<GridSegment>();
    corners = new Corner[2][2];
//    leadingCorners = new Corner[2];
//    trailingCorners = new Corner[2];
    endX = new GridIntersection[2];
    endY = new GridIntersection[2];
    for (int i = 0; i < 2; i++) {
    	corners[0][i] = new Corner();
    	corners[1][i] = new Corner();
//      leadingCorners[i] = new Corner();
//      trailingCorners[i] = new Corner();
    	endX[i] = new GridIntersection(0, 0, true); // vertical
    	endY[i] = new GridIntersection(0, 0, false); // horizontal
    }
  }

  /**
   * Gets end 1.
   *
   * @return end 1
   */
  public TPoint getLineEnd0() {
    return lineEnd0;
  }

  /**
   * Gets end 2.
   *
   * @return end 2
   */
  public TPoint getLineEnd1() {
    return lineEnd1;
  }

  /**
   * Gets the center handle.
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
   * @return the TPoint that is hit, or null
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    Shape hitShape;
    if (endsEnabled) {
      hitShape = end0Shapes.get(trackerPanel);
      if (hitShape != null && hitShape.intersects(hitRect)) return lineEnd0;
      hitShape = end1Shapes.get(trackerPanel);
      if (hitShape != null && hitShape.intersects(hitRect)) return lineEnd1;
    }
    hitShape = shaftShapes.get(trackerPanel);
    if (hitShape != null && hitShape.intersects(hitRect)) {
      return handle;
    }
    return null;
  }

  /**
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    // draw the line profile
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    getMark(trackerPanel).draw(g, false);
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
      if (footprint instanceof OutlineFootprint) {
        OutlineFootprint outline = (OutlineFootprint)footprint;
        LineProfile profile = (LineProfile)track;
        int spread = profile.getSpread();
        double factor = trackerPanel.getXPixPerUnit();
        if (!trackerPanel.isDrawingInImageSpace()) {
          int n = trackerPanel.getFrameNumber();
          factor = factor / trackerPanel.getCoords().getScaleX(n);
        }
        int i = (int)(factor * (0.5 + spread));
        outline.setSpread(i);
      }
      // get new mark
      selection = trackerPanel.getSelectedPoint();
      int pointNumber = 0;
      Point p = null;
      Shape s = null;
      for (int i = 0; i < points.length; i++) {
        screenPoints[i] = points[i].getScreenPosition(trackerPanel);
        if (selection == points[i]) {
          p = screenPoints[i];
          pointNumber = i;
        }
      }
      mark = footprint.getMark(screenPoints);
      if (p != null) {
        transform.setToTranslation(p.x, p.y);
        s = transform.createTransformedShape(selectionShape);
        final Color color = footprint.getColor();
        final Mark stepMark = mark;
        final Shape selectedShape = s;
        mark = new Mark() {
          public void draw(Graphics2D g, boolean highlighted) {
            stepMark.draw(g, false);
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
      marks.put(trackerPanel, mark);
      // get new hit shapes
      Shape[] shapes = footprint.getHitShapes();
      end0Shapes.put(trackerPanel, shapes[0]);
      end1Shapes.put(trackerPanel, shapes[1]);
      if (s != null && pointNumber == 2) {
        shaftShapes.put(trackerPanel, s);
      }
      else {
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
    return bounds;
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public Object clone() {
    LineProfileStep step = (LineProfileStep)super.clone();
    if (step != null) {
      step.points[0] = step.lineEnd0 = step.new LineEnd(lineEnd0.getX(), lineEnd0.getY());
      step.points[1] = step.lineEnd1 = step.new LineEnd(lineEnd1.getX(), lineEnd1.getY());
      step.points[2] = step.handle = step.new Handle(handle.getX(), handle.getY());
      step.end0Shapes = new HashMap<TrackerPanel, Shape>();
      step.end1Shapes = new HashMap<TrackerPanel, Shape>();
      step.shaftShapes = new HashMap<TrackerPanel, Shape>();
      step.endX = new GridIntersection[2];
      step.endY = new GridIntersection[2];
      for (int i = 0; i < 2; i++) {
      	step.endX[i] = new GridIntersection(0, 0, true); // vertical
      	step.endY[i] = new GridIntersection(0, 0, false); // horizontal
      }
    }
    return step;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "LineProfileStep " + n //$NON-NLS-1$
           + " [" + format.format(lineEnd0.x) //$NON-NLS-1$
           + ", " + format.format(lineEnd0.y) //$NON-NLS-1$
           + ", " + format.format(lineEnd1.x) //$NON-NLS-1$
           + ", " + format.format(lineEnd1.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Gets the line profile data.
   *
   * @param trackerPanel the tracker panel
   * @return an integer array of values for image pixels along the line
   */
  public double[][] getProfileData(TrackerPanel trackerPanel) {
    if (trackerPanel.getVideo() == null) return null;
    LineProfile profile = (LineProfile)getTrack();
    int frame = trackerPanel.getFrameNumber();
    double angle = trackerPanel.getCoords().getAngle(frame);
    if (profile.isHorizontal || Math.abs(Math.sin(angle)) < .00001) {
    	return getHorizontalProfileData(trackerPanel);
    }
    return getTiltedProfileData(trackerPanel);
  }

  /**
   * Rotates this step about its center to keep it either horizontal
   * or parallel to the x-axis.
   */
  protected void rotate() {
  	if (line.trackerPanel==null) return;
  	double theta_x = line.trackerPanel.getCoords().getAngle(n);
  	if (line.isHorizontal) theta_x = 0;
    double theta_step = lineEnd0.angle(lineEnd1);
    if (theta_step>Math.PI/2 || theta_step<-Math.PI/2) {
    	theta_step = lineEnd1.angle(lineEnd0);
    }
    double theta = theta_x+theta_step;
    if (Math.abs(theta) > .0000001) {
	    center.center(lineEnd0, lineEnd1);
	    AffineTransform transform = 
	    	AffineTransform.getRotateInstance(-theta, center.x, center.y);    
	    transform.transform(lineEnd0, lineEnd0);
	    transform.transform(lineEnd1, lineEnd1);
	    erase();
    }
  }

  //_________________________ private methods ________________________

  /**
   * Gets the tilted line profile data.
   *
   * @param trackerPanel the tracker panel
   * @return an integer array of values for image pixels along the line
   */
  private double[][] getTiltedProfileData(TrackerPanel trackerPanel) {
    double length = lineEnd0.distance(lineEnd1);
    if (length < 1) return null;
    BufferedImage image = trackerPanel.getVideo().getImage();
    if (image != null && image.getType() == BufferedImage.TYPE_INT_RGB) {
    	Shape bounds = new Rectangle(image.getWidth(), image.getHeight());
      // get line profile width and angle/slope data
      int width = 1 + 2*line.getSpread();
      int len = (int)Math.floor(length); // length of line profile data array
      double theta = lineEnd0.angle(lineEnd1);
      cos = Math.cos(theta);
      sin = Math.sin(theta);
      // determine min and max values and set initial corner positions
      double dx = width*sin/2.0; 
      double dy = width*cos/2.0;
      // set corners at lineEnd0
      corners[0][0].x = lineEnd0.x-dx; corners[0][0].y = lineEnd0.y+dy;
      corners[0][1].x = lineEnd0.x+dx; corners[0][1].y = lineEnd0.y-dy;
      // step the corners forward until both are inside image bounds
      while (len > 0 && (!bounds.contains(corners[0][0]) || !bounds.contains(corners[0][1]) )) {
      	len--;
      	corners[0][0].x += cos;
      	corners[0][0].y += sin;
      	corners[0][1].x += cos;
      	corners[0][1].y += sin;
      }
      // set corners at lineEnd1
      corners[1][0].x = lineEnd1.x-dx; corners[1][0].y = lineEnd1.y+dy;
      corners[1][1].x = lineEnd1.x+dx; corners[1][1].y = lineEnd1.y-dy;
      // step the corners back until inside bounds
      while (len > 0 && (!bounds.contains(corners[1][0]) || !bounds.contains(corners[1][1]) )) {
      	len--;
      	corners[1][0].x -= cos;
      	corners[1][0].y -= sin;
      	corners[1][1].x -= cos;
      	corners[1][1].y -= sin;
      }
      if (len < 1) return null;
      // get the min and max bounds
      xMin = xMax = corners[0][0].x;
      yMin = yMax = corners[0][0].y;
      for (int i = 0; i < 2; i++) {
      	for (int j = 0; j < 2; j++) {
	      	xMin = Math.min(xMin, corners[i][j].x);
	      	yMin = Math.min(yMin, corners[i][j].y);
	      	xMax = Math.max(xMax, corners[i][j].x);
	      	yMax = Math.max(yMax, corners[i][j].y);
      	}
      }
      double[][] values = new double[7][len];
      // use min and max to determine image pixels needed
      int pixXMin = (int)Math.floor(xMin);
      int pixYMin = (int)Math.floor(yMin);
      int pixXMax = (int)Math.ceil(xMax);
      int pixYMax = (int)Math.ceil(yMax);
      int w = pixXMax-pixXMin;
      int h = pixYMax-pixYMin;
      int[] pixels = new int[w * h];
      // prepare GridIntersection arrays if needed
      if (sweepX == null || sweepX[0].length < width) {
      	sweepX = new GridIntersection[2][width];
      	sweepY = new GridIntersection[2][width];
      	for (int i = 0; i < width; i++) {
      		sweepX[0][i] = new GridIntersection(0, 0, true);
      		sweepX[1][i] = new GridIntersection(0, 0, true);
      		sweepY[0][i] = new GridIntersection(0, 0, false);
      		sweepY[1][i] = new GridIntersection(0, 0, false);
      	}
      }
      // find initial "leading edge" intersections
      leadingIndex = 0;
      findLeadingIntersections();
      Point2D imagePixel = new Point2D.Double();
      Point2D worldPixel = new Point2D.Double();
      try {
        // get affine transform
        int n = trackerPanel.getFrameNumber();
        AffineTransform at = trackerPanel.getCoords().getToWorldTransform(n);
        // fill pixels array with pixel data
        image.getRaster().getDataElements(pixXMin, pixYMin, w, h, pixels);
        // step along length of the line
        for (int i = 0; i < len; i++) {
        	Corner end0 = corners[leadingIndex][0];
        	Corner end1 = corners[leadingIndex][1];
        	// leading index is toggled at every step
        	// so leading becomes trailing edge data
          leadingIndex = leadingIndex == 0? 1: 0;
        	// set new corner positions
          corners[leadingIndex][0].x = end0.x+cos; 
          corners[leadingIndex][0].y = end0.y+sin; 
          corners[leadingIndex][1].x = end1.x+cos; 
          corners[leadingIndex][1].y = end1.y+sin;           
	        // find new bounds of corners
          xMin = xMax = corners[0][0].x;
          yMin = yMax = corners[0][0].y;
          for (int k = 0; k < 2; k++) {
          	for (int j = 0; j < 2; j++) {
    	      	xMin = Math.min(xMin, corners[k][j].x);
    	      	yMin = Math.min(yMin, corners[k][j].y);
    	      	xMax = Math.max(xMax, corners[k][j].x);
    	      	yMax = Math.max(yMax, corners[k][j].y);
          	}
          }
          // get position data at center of bounds
          imagePixel.setLocation((xMax+xMin)/2, (yMax+yMin)/2);
          at.transform(imagePixel, worldPixel);
          values[0][i] = worldPixel.getX();
          values[1][i] = worldPixel.getY();
          // make areas array
          int minCol = (int)Math.floor(xMin);
          int minRow = (int)Math.floor(yMin);
          int colCount = (int)Math.ceil(xMax) - minCol;
          int rowCount = (int)Math.ceil(yMax) - minRow;
          double[][] areas = new double[colCount][rowCount];
          // find and link leading intersections to corners
          findLeadingIntersections();
          // find and link end intersections to corners
          findEndIntersections();
          // find grid segments and vertices
        	findGridSegments();
          findGridVertices();
          // define local rgb and area accumulators 
          double area = 0, red = 0, green = 0, blue = 0;
          double a;
          int column, row;
        	// step through vertices, if any  
          if (!vertices.isEmpty()) {
	          Iterator<GridVertex> it = vertices.iterator();
	          while (it.hasNext()) {
	          	// fill areas array from quadrant areas  
	          	GridVertex next = it.next();
	          	column = (int)next.x - minCol;
	          	row = (int)next.y - minRow;
	            // set initial quadrant areas
	            quadAreas[0] = areas[column][row];
	            quadAreas[1] = column > 0? areas[column-1][row]: 1;
	            quadAreas[2] = row > 0 && column > 0? areas[column-1][row-1]: 1;
	            quadAreas[3] = row > 0? areas[column][row-1]: 1;
	            getAreas(next, quadAreas);
	          	areas[column][row] = quadAreas[0];
	          	if (column > 0) areas[column-1][row] = quadAreas[1];
	          	if (row > 0) {
	          		areas[column][row-1] = quadAreas[3];
	            	if (column > 0) areas[column-1][row-1] = quadAreas[2];
	          	}
	          }
          }
          else { // no vertices found: single pixel case
          	// pick any grid segment and find areas on both sides
	      		GridSegment seg = xSegments.iterator().next();
        		a = getArea(seg.lower, seg.higher);
          	column = polyLoc.x - minCol;
          	row = polyLoc.y - minRow;
          	if (a > 0) areas[column][row] = a;
            a = getArea(seg.higher, seg.lower);
          	column = polyLoc.x - minCol;
          	row = polyLoc.y - minRow;
            if (a > 0) areas[column][row] = a;
          } 
          // in all cases find corner areas
	        for (int j = 0; j < 2; j++) {
		        for (int k = 0; k < 2; k++) {
			    		a = getArea(corners[j][k]);
	          	column = polyLoc.x - minCol;
	          	row = polyLoc.y - minRow;
	            if (a > 0) areas[column][row] = a;
		        }
	        }          
          // find total area and area-weighted RGB values
          for (int ro = 0; ro < areas[0].length; ro++) {
          	for (int col = 0; col < areas.length; col++) {
          		int pixCol = col + minCol - pixXMin;
          		int pixRow = ro + minRow - pixYMin;
          		int pixIndex = pixCol + pixRow*w;
	            int pixel = pixels[pixIndex];
	            int r = (pixel >> 16) & 0xff; // red
	            int g = (pixel >> 8) & 0xff; // green
	            int b = (pixel) & 0xff; // blue
          		a = areas[col][ro];
          		red += a*r;
          		green += a*g;
          		blue += a*b;
          		area += a;
          	}
          }  
        	if (area == 0) return null;
	        values[2][i] = red = red / area;
	        values[3][i] = green = green / area;
	        values[4][i] = blue = blue / area;
	        values[5][i] = RGBRegion.getLuma(red, green, blue);
	        values[6][i] = width; // should equal 2*spread + 1
        }
      } catch(ArrayIndexOutOfBoundsException ex) {
      	ex.printStackTrace();
      	return null;
      }
      return values;
    }
  	return null;
  }
  /**
   * Finds intersections between the leading sweep and pixel grid lines.
   */
  private void findLeadingIntersections() {
  	sorter.clear();
  	// identify end corners and get bounds of leading edge
  	sorter.add(corners[leadingIndex][0]);
  	sorter.add(corners[leadingIndex][1]);
  	Iterator<Intersection> it = sorter.iterator();
  	Corner minXCorner = (Corner)it.next();
  	Corner maxXCorner = (Corner)it.next();
  	// set corner hi/lo links to null
  	minXCorner.lowerX = null;
  	maxXCorner.higherX = null;
    // equation of line is y = y0 + slope*x
		double slope = -cos/sin;
  	double y0 = corners[leadingIndex][0].y - slope*corners[leadingIndex][0].x;
  	// add x-intersections
  	int n = 0;
  	for (int i = (int)Math.ceil(minXCorner.x); i <= (int)Math.floor(maxXCorner.x); i++) {
  		// x = i
  		double y = slope*i+y0;
  		sweepX[leadingIndex][n].setLocation(i, y); 
  		sorter.add(sweepX[leadingIndex][n]);
  		n++;
  	}
  	// set unused intersections to NaN
  	for (;n < sweepX[leadingIndex].length; n++) 
  		sweepX[leadingIndex][n].setLocation(Double.NaN, Double.NaN); 
  	// add y-intersections
  	double ymin = Math.min(minXCorner.y, maxXCorner.y);
    double ymax = Math.max(minXCorner.y, maxXCorner.y);
  	n = 0;
  	for (int i = (int)Math.ceil(ymin); i <= (int)Math.floor(ymax); i++) {
  		// y = i
  		double x = (i-y0)/slope;
  		sweepY[leadingIndex][n].setLocation(x, i); 
  		sorter.add(sweepY[leadingIndex][n]);
  		n++;
  	}
  	// set unused intersections to NaN
  	for (;n < sweepY[leadingIndex].length; n++) 
  		sweepY[leadingIndex][n].setLocation(Double.NaN, Double.NaN);
  	
  	// link intersections and end corners in x order
  	it = sorter.iterator();
  	Intersection prev = null;
  	while (it.hasNext()) {
  		Intersection next = it.next();
  		if (prev != null) {
    		prev.higherX = next;
    		next.lowerX = prev;
  		}
  		prev = next;
  	}
  }
  
  /**
   * Finds the x and y grid segments.
   */
  private void findGridSegments() {
  	// x-grid (vertical) segments
  	xSegments.clear();
    sorter.clear();
    // collect all x intersections
    for (int i = 0; i < 2; i++) {
      if (!Double.isNaN(endX[i].x)) sorter.add(endX[i]);
    }
    for (int i = 0; i < sweepX[0].length; i++) {
    	for (int j = 0; j < 2; j++) {
    		if (!Double.isNaN(sweepX[j][i].x)) sorter.add(sweepX[j][i]);
    	}
    }
  	// iterate and pair off
  	GridIntersection end0 = null; // one end of a grid segment
  	for (Iterator<Intersection> it = sorter.iterator(); it.hasNext();) {
  		GridIntersection next = (GridIntersection)it.next();
			if (end0 == null) end0 = next;
			else {
				xSegments.add(new GridSegment(next, end0));
				end0 = null;
			}
  	}
  	// y-grid (horizontal) segments
  	ySegments.clear();
    sorter.clear();
    // collect all y intersections but reverse x-y values for sorting
    for (int i = 0; i < 2; i++) {
      if (!Double.isNaN(endY[i].y)) {
      	double val = endY[i].y;
      	endY[i].setLocation(val, endY[i].x);
      	sorter.add(endY[i]);
      }
    }
    for (int i = 0; i < sweepY[0].length; i++) {
    	for (int j = 0; j < 2; j++) {
    		if (!Double.isNaN(sweepY[j][i].y)) {
        	double val = sweepY[j][i].y;
        	sweepY[j][i].setLocation(val, sweepY[j][i].x);
    			sorter.add(sweepY[j][i]);
    		}
    	}
    }
  	// iterate and pair off
  	end0 = null;
  	for (Iterator<Intersection> it = sorter.iterator(); it.hasNext();) {
  		GridIntersection next = (GridIntersection)it.next();
			double val = next.y;
    	next.setLocation(val, next.x);
			if (end0 == null) {
				end0 = next;
			}
			else {
				ySegments.add(new GridSegment(next, end0));
				end0 = null;
			}
  	}
  }
  
  /**
   * Fills the vertices array with xy grid vertices that lie inside the probe area.
   */
  private void findGridVertices() {
  	vertices.clear();
   	// for each x-segment, find all intersecting y-segments
  	Iterator<GridSegment> xIt = xSegments.iterator();
  	while (xIt.hasNext()) {
  		GridSegment nextX = xIt.next();
  		Iterator<GridSegment> yIt = ySegments.iterator();
  		while (yIt.hasNext()) {
  			GridSegment nextY = yIt.next();
  			if (nextY.higher.x < nextX.value) // y segment's max x is too small
  				continue;  			
  			if (nextY.lower.x > nextX.value) // y segment's min x is too big
  				break;  
  			// only need to check x segment
  			if (nextX.lower.y < nextY.value && nextX.higher.y > nextY.value) {
  				vertices.add(new GridVertex(nextX, nextY));
  			}
  		}
  	}
  	// sort the vertices
  	Collections.sort(vertices);
  	// trim vertex segments longer than 1 pixel
  	Iterator<GridVertex> it = vertices.iterator();
  	GridVertex prev = null;
  	while (it.hasNext()) {
  		GridVertex next = it.next();
  		if (prev != null) {
  			if (prev.distance(next) == 1) { // one pixel away
  				if (prev.x == next.x) { // next is above or below
  					prev.isVertical = next.isVertical = true;
  					if (next.y - prev.y > 0) { // next is above
  						GridSegment segment = new GridSegment(prev.vert.lower, next);
  						prev.setVerticalSegment(segment);  						
  						segment = new GridSegment(next.vert.higher, prev);
  						next.setVerticalSegment(segment);  						
  					}
  					else { // next is below
  						GridSegment segment = new GridSegment(prev.vert.higher, next);
  						prev.setVerticalSegment(segment);  						
  						segment = new GridSegment(next.vert.lower, prev);
  						next.setVerticalSegment(segment);  						
  					}
  				}
  				else { // next is to right
  					prev.isVertical = next.isVertical = false;
  					GridSegment segment = new GridSegment(prev.horz.lower, next);
						prev.setHorizontalSegment(segment);  						
						segment = new GridSegment(next.horz.higher, prev);
						next.setHorizontalSegment(segment);  
  				}
  			}
  		}
  		prev = next;
  	}
  }
  
  /**
   * Finds the end intersections.
   */
  private void findEndIntersections() {
  	int trailingIndex = leadingIndex == 0? 1: 0;
    for (int j = 0; j < 2; j++) { // j is end #
    	sorter.clear();
    	sorter.add(corners[0][j]);
    	sorter.add(corners[1][j]);
      double intercept = corners[trailingIndex][j].y - sin*corners[trailingIndex][j].x/cos;
      // x intersection
      double grid = cos > 0? Math.ceil(corners[trailingIndex][j].x): Math.ceil(corners[leadingIndex][j].x);
      double larger = cos > 0?  corners[leadingIndex][j].x: corners[trailingIndex][j].x;
      if (grid < larger) {
      	double y = (sin*grid/cos)+intercept;
      	endX[j].setLocation(grid, y);
      	sorter.add(endX[j]);
      }
      else endX[j].setLocation(Double.NaN, Double.NaN); // indicates none found
      // y intersection
      grid = sin > 0? Math.ceil(corners[trailingIndex][j].y): Math.ceil(corners[leadingIndex][j].y);
      larger = sin > 0?  corners[leadingIndex][j].y: corners[trailingIndex][j].y;
      if (grid < larger) {
      	double x = (grid-intercept)*cos/sin;
      	endY[j].setLocation(x, grid);
      	sorter.add(endY[j]);
      }
      else endY[j].setLocation(Double.NaN, Double.NaN); // indicates none found
      // link end intersections and corners
    	Iterator<Intersection> it = sorter.iterator();
    	Intersection prev = null;
    	while (it.hasNext()) {
    		Intersection next = it.next();
    		if (prev != null) {
      		if (prev instanceof Corner) { // prev is min x corner
      			Corner corner = (Corner)prev;
      			corner.end = next;
        		if (corner.higherX == null || Double.isNaN(corner.higherX.x)) {
        			corner.higherX = next;
        		}
      		}
      		else { // prev is not a corner
      			prev.higherX = next;
      		}
      		if (next instanceof Corner) { // next is max x corner
      			Corner corner = (Corner)next;
      			corner.end = prev;
        		if (corner.lowerX == null || Double.isNaN(corner.lowerX.x)) 
        			corner.lowerX = prev;
      		}
      		else { // next is not a corner
      			next.lowerX = prev;
      		}
    		}
    		prev = next;
    	}
    }
  }

  /**
   * Gets the next ccw intersection given the current and previous intersection.
   */
  private Intersection getNext(Intersection current, Intersection prev) {
  	Intersection next = null;
  	if (current instanceof GridVertex) {
  		// always goes ccw (for rh coordinates)
  		GridVertex vertex = (GridVertex)current;
  		if (prev == vertex.horz.lower) return vertex.vert.higher;
  		if (prev == vertex.horz.higher) return vertex.vert.lower;
  		if (prev == vertex.vert.lower) return vertex.horz.lower;
  		next = vertex.horz.higher;
  	}
  	if (current instanceof Corner) {
  		Corner corner = (Corner)current;
  		// if prev is the end, next is either higherX or lowerX
  		if (prev.compareTo(corner.end) == 0) {
  			// next is higherX if it exists, is not NaN, and is higher
    		if (corner.higherX != null && !Double.isNaN(corner.higherX.x)
    						&& corner.higherX.compareTo(corner) > 0)
    			next = corner.higherX;
    		else if (corner.lowerX != null && !Double.isNaN(corner.lowerX.x)
    						&& corner.lowerX.compareTo(corner) < 0)
     			next = corner.lowerX;
  		}
  		// else next is the end
  		else {
  			next = corner.end;
  		}
  	}
  	if (next == null && current instanceof GridIntersection) {
  		GridIntersection grid = (GridIntersection)current;
  		if (prev == grid.segment.vertex) {
  			GridVertex vertex = (GridVertex)prev;
  			if (grid == vertex.vert.higher) 
  				next = grid.lowerX;
  			else if (grid == vertex.vert.lower) 
  				next = grid.higherX;
  			else if (grid == vertex.horz.higher) { // go toward higher y
  				if (grid.higherX.y > grid.y) 
  					next = grid.higherX;
  				else next = grid.lowerX;
  			}
  			else if (grid.higherX.y < grid.y) // go toward lower y
  				next = grid.higherX;
  			else next = grid.lowerX;
  		}
  		else if (grid.segment.vertex == null) {
  			if (prev == grid.segment.lower) {
  				if (grid.isVertical) 
  					next = grid.lowerX;
  				else next = grid.higherX.y > grid.y? grid.higherX: grid.lowerX; 
  			}
  			else if (prev == grid.segment.higher) {
  				if (grid.isVertical) 
  					next = grid.higherX;
  				else next = grid.higherX.y < grid.y? grid.higherX: grid.lowerX; 
  			}
  			else next = grid == grid.segment.higher? grid.segment.lower: grid.segment.higher;
  		}
  		else next = grid.segment.vertex;
  	}
  	// find previous and next angles
  	if (next == null) return null;
  	double thetaPrev = Math.atan2(
  					current.getY() - prev.getY(), current.getX() - prev.getX());
  	double thetaNext = Math.atan2(
  					next.getY() - current.getY(), next.getX() - current.getX());
  	double delta = thetaNext-thetaPrev;
  	if ((delta > 0 && delta < Math.PI) || delta < -Math.PI)
  		return next;
  	return null;
  }
  
  /**
   * Finds the areas in each quadrant of the specified GridVertex.
   * Skips areas that have already been determined.
   * 
   * @param vertex the GridVertex
   * @param knowns double[4] array with zero or positive values
   */
  private double[] getAreas(GridVertex vertex, double[] knownValues) {
  	// get area in each zero-valued index (quadrant)
  	for (int i = 0; i < 4; i++) {
  		double area = knownValues[i];
  		if (area == 0) knownValues[i] = getArea(vertex, i);
  	}
  	return knownValues;
  }
  
  /**
   * Gets the area of the specified GridVertex quadrant.
   */
  private double getArea(GridVertex vertex, int quadrant) {
  	// quadrant is defined by the axis along which the "current" intersection is chosen
  	Intersection current = vertex.horz.higher; // quadrant 0
  	switch (quadrant) {
  		case 1: {
  			current = vertex.vert.higher; // quadrant 1
  			break;
  		}
  		case 2: {
  			current = vertex.horz.lower; // quadrant 2
  			break;
  		}
  		case 3: {
  			current = vertex.vert.lower; // quadrant 3
  			break;
  		}
  	}  	
  	return getArea(vertex, current);
  }
  
  /**
   * Gets the area associated with a corner.
   */
  private double getArea(Corner corner) {
  	double a = getArea(corner, corner.end); 
  	if (a == 0) {
  		getArea(corner.end, corner);
  	}
  	return a > 0? a: getArea(corner.end, corner);
  }
  
  /**
   * Gets the ccw polygon area defined by a pair of intersections.
   */
  private double getArea(Intersection start, Intersection next) {
  	polygon[0] = start; 
  	Intersection prev = start;
  	// construct polygon
  	int n = 1;
  	while (next != null && next != start) {
  		polygon[n++] = next;
    	Intersection ondeck = getNext(next, prev);
    	prev = next;
    	next = ondeck;
  	}
  	// find area and location of polygon
  	double area = 0;
    int col = (int)polygon[0].x;
  	int row = (int)polygon[0].y;
  	boolean noCorner = true;
  	for (int i = 0; i < n; i++) {
  		int below = i-1 < 0? n-1: i-1;
  		int above = i+1 > n-1? 0: i+1;
  		area += polygon[i].x * (polygon[above].y - polygon[below].y);
  		if (polygon[i] instanceof Corner) {
  			Corner corner = (Corner)polygon[i];
	  		col = (int)corner.x;
	  		row = (int)corner.y;
	  		noCorner = false;
  		}
  		if (noCorner) {  			
  			col = Math.min(col, (int)polygon[i].x);
  			row = Math.min(row, (int)polygon[i].y);
  		}
  	}
  	area /= 2;
  	// set polyLoc position to current polygon pixel
  	polyLoc.setLocation(col, row);
  	return area;
  }
  
 
  /**
   * Gets the line profile data for a horizontal line.
   *
   * @param trackerPanel the tracker panel
   * @return an integer array of values for image pixels along the line
   */
  private double[][] getHorizontalProfileData(TrackerPanel trackerPanel) {
    if (trackerPanel.getVideo() == null) return null;
    int spread = line.getSpread();
    // get line end points
    int x0 = Math.min((int)lineEnd0.getX(), (int)lineEnd1.getX());
    x0 = Math.max(x0, 0);
    int x1 = Math.max((int)lineEnd0.getX(), (int)lineEnd1.getX());
    x1 = Math.min(x1, (int)trackerPanel.getImageWidth());
    int length = x1 - x0;
    if (length <= 0) return null;
    int width = 1 + 2*spread;
    int[] pixels = new int[length * width];
    int[] r = new int[width];
    int[] g = new int[width];
    int[] b = new int[width];
    double[][]values = new double[10][length];
    Point2D imagePixel = new Point2D.Double();
    Point2D worldPixel = new Point2D.Double();
    BufferedImage image = trackerPanel.getVideo().getImage();
    if (image != null && image.getType() == BufferedImage.TYPE_INT_RGB) {
      try {
        // locate starting pixel
        int y = (int)lineEnd0.getY();
        int y0 = y - spread;
        // get affine transform
        int n = trackerPanel.getFrameNumber();
        AffineTransform at = trackerPanel.getCoords().getToWorldTransform(n);
        // fill pixels array with pixel data
        image.getRaster().getDataElements(x0, y0, length, width, pixels);
        // step along length of the line
        for (int i = 0; i < length; i++) {
          // step through pixels across line width at each point
          for (int j = 0; j < width; j++) {
            // if in the center, get world x of center of pixel
            if (j == spread) {
              imagePixel.setLocation(x0 + i + 0.5, y + 0.5);
              at.transform(imagePixel, worldPixel);
              values[0][i] = worldPixel.getX();
              values[1][i] = worldPixel.getY();
            }
            int pixel = pixels[i + j*length];
            r[j] = (pixel >> 16) & 0xff; // red
            g[j] = (pixel >> 8) & 0xff; // green
            b[j] = (pixel) & 0xff; // blue
          }
          // get average values across spread
          double rMean = 0, gMean = 0, bMean = 0, total = 0;
          for (int j = 0; j < r.length; j++) {
            rMean += r[j];
            gMean += g[j];
            bMean += b[j];
            total++;
          }
          values[2][i] = rMean = rMean / total; // red 0-255
          values[3][i] = gMean = gMean / total; // green 0-255
          values[4][i] = bMean = bMean / total; // blue 0-255
          values[5][i] = RGBRegion.getLuma(rMean, gMean, bMean);
          values[6][i] = total; // total weight in pixels
        }
      } catch(ArrayIndexOutOfBoundsException ex) {return null;}
    }
    return values;
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 3;
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
      setTrackEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method to move both ends.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (track.isLocked()) return;
      if (!line.isFixed()) {
      	line.keyFrames.add(n);
      }
      double dx = x - getX();
      double dy = y - getY();
      if (line.isFixed()) {
      	LineProfileStep step = (LineProfileStep)line.steps.getStep(0);
      	step.lineEnd0.setLocation(lineEnd0.getX()+dx, lineEnd0.getY()+dy);
      	step.lineEnd1.setLocation(lineEnd1.getX()+dx, lineEnd1.getY()+dy);
      	step.handle.setLocation(x, y);
  	    step.erase();
  	    line.refreshStep(LineProfileStep.this); // sets properties of this step
      }
      else {
      	lineEnd0.setLocation(lineEnd0.getX()+dx, lineEnd0.getY()+dy);
      	lineEnd1.setLocation(lineEnd1.getX()+dx, lineEnd1.getY()+dy);
        setLocation(x, y);
      	line.keyFrames.add(n);
    	}      
      repaint();
      track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
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
    	setPositionOnLine(xScreen, yScreen, trackerPanel, lineEnd0, lineEnd1);
    }
  }

  //______________________ inner LineEnd class ________________________

  class LineEnd extends TPoint {

    /**
     * Constructs a LineEnd with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public LineEnd(double x, double y) {
      super(x, y);
      setTrackEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY to allow only movement parallel to the x-axis,
     * or horizontal if profile is alwaysHorizontal
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (track.isLocked()) return;
      // skip if track has not been added to TrackerPanel (eg when loading)
      if (track.trackerPanel == null) {
      	super.setXY(x, y);
      	return;
      }
      // find the mouse displacement (shared hypoteneuse) 
      double dx = x-getX();
      double dy = y-getY();
      double hyp = Math.sqrt(dx*dx+dy*dy);
      // get angles of x-axis and mouse displacement from horizontal 
      double theta1 = -track.trackerPanel.getCoords().getAngle(n);
      LineProfile profile = (LineProfile)getTrack();
      if (profile.isHorizontal) theta1 = 0;
      double theta2 = Math.atan2(dy, dx);
      // find angle between mouse displacement and x-axis
      double theta = theta1-theta2;
      // find distance mouse moved along x-axis
      double d = hyp*Math.cos(theta);
      // find dx and dy and set position
      dx = d*Math.cos(theta1);
      dy = d*Math.sin(theta1);      
      if (line.isFixed()) {
      	LineProfileStep step = (LineProfileStep)line.steps.getStep(0);
      	TPoint target = this==lineEnd0? step.lineEnd0: step.lineEnd1;
      	target.setLocation(getX()+dx, getY()+dy); // set property of step 0
  	    step.erase();
  	    line.refreshStep(LineProfileStep.this); // sets properties of this step
      }
      else {
      	setLocation(getX()+dx, getY()+dy);
      	line.keyFrames.add(n);
    	}      
      repaint();
      track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
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
  }
  
  //______________________ Intersection classes ______________________________
  
  /** 
   * The classes below help measure the tilted line profile. How they work:
   * 
   * 1. A line profile generates data for regions that are 1 pixel apart along the line
   * 2. Region dimensions are 1 pixel by (2s + 1) pixels where s is the spread
   * 3. Regions are measured in a probe of width 1.0 pixel and length (2s+1) pixels
   * 4. The probe has a position (image position of center) and tilt angle
   * 5. The probe has a leading edge, trailing edge and two 1-pixel-long ends
   * 6. Measured rgb values depend on overlapping areas between probe and image pixel grid
   * 7. Every area is a polygon made of pixel grid lines and probe perimeter lines
   * 8. Every point on a polygon is an Intersection of type: 
   *    (a) intersection of a probe edge or end with the grid (GridIntersection)
   *    (b) grid vertex located inside the probe (GridVertex)
   *    (c) probe corner (Corner)
   * 9. Every GridVertex consists of an x and y GridSegment. 
   * 10. Every GridSegment has a lower and higher Intersection.  
   * 11. Every polygon (except corner orphan) includes one or more grid vertices
   * 12. Find pixel areas by stepping thru grid vertices and finding quadrant areas
   * 13. Find quadrant areas by stepping ccw along linked intersections back to start
   * 14. Ignore corner orphans for now--not significant since rgb are area-weighted
   */

    /**
     * A class to hold intersections that are connected to other intersections. 
     * Sorts first by x, then increasing y if tan > 0, otherwise decreasing y
     */
    class Intersection extends Point2D.Double implements Comparable<Intersection> {
    	Intersection lowerX; // null if first in line
    	Intersection higherX; // null if last in line
    	Intersection(double x, double y) {
    		super(x, y);
    	}
    	// returns +1 if this is higher than the comparison intersection
    	public int compareTo(Intersection other) {
    		double dx = x - other.x;
    		if (dx == 0) {
    			double dy = y - other.y;
    			if (cos/sin < 0)	return dy == 0? 0: dy > 0? +1: -1;
    			return dy == 0? 0: dy < 0? +1: -1;
    		}
    		return dx > 0? +1: -1;
    	}
    }

    //______________________ inner Corner class ________________________
    
    /**
     * A corner point is connected to one end and one sweep intersection. 
     */
    class Corner extends Intersection {
    	Intersection end; // nearest intersection along end
    	Corner() {super(0, 0);}  	
    }

    //______________________ inner GridIntersection class ________________________
    
    /**
     * A class to hold intersections with grid lines. Every grid intersection
     * connects to a grid vertex.
     */
    class GridIntersection extends Intersection {
    	boolean isVertical; // true if intersected grid line is vertical
    	GridSegment segment; // segment for which this intersection is one end 
    	GridIntersection(double x, double y, boolean vert) {
    		super(x, y);
    		isVertical = vert;
    	}
    }

    //______________________ inner GridSegment class ________________________
   
    /**
     * A class to hold grid segments. Each segment has an integer value
     * and double min and max extents.
     */
    class GridSegment implements Comparable<GridSegment> {
    	double value; // grid line value
    	GridIntersection lower, higher; // ends of this segment
    	GridVertex vertex; // vertex for which this is one axis (may be null)
    	// constructor
    	GridSegment(GridIntersection end0, GridIntersection end1) {
    		boolean vert = end0.isVertical;
    		if (vert) {
    			value = end0.x;
    			boolean end0Smaller = end0.y < end1.y;
    			lower = end0Smaller? end0: end1;
    			higher = end0Smaller? end1: end0;
    		}
    		else {
    			value = end0.y;
    			boolean end0Smaller = end0.x < end1.x;
    			lower = end0Smaller? end0: end1;
    			higher = end0Smaller? end1: end0;
    		}
    		lower.segment = this;
    		higher.segment = this;
    	}
    	// The compareTo method orders by x value
    	public int compareTo(GridSegment other) {
    		if (lower.isVertical)
    			return (int)(this.value - other.value);
    		double dx = this.lower.x - other.lower.x;
    		return dx == 0? 0: dx > 0? 1: -1;
    	}
    }

    /**
     * A class to hold grid vertices. Each vertext is two GridSegments that intersect.
     */
    class GridVertex extends GridIntersection {
    	GridSegment vert, horz; // segments that intersect to form this vertex
    	GridVertex(GridSegment xSegment, GridSegment ySegment) {
    		super(xSegment.value, ySegment.value, true);
    		setVerticalSegment(xSegment);
    		setHorizontalSegment(ySegment);
    	}
    	void setVerticalSegment(GridSegment segment) {
    		vert = segment;
    		vert.vertex = this;
    	}
    	void setHorizontalSegment(GridSegment segment) {
    		horz = segment;
    		horz.vertex = this;
    	}
    }

}

