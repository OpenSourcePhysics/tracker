/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Measurable;

/**
 * A PencilDrawing is a freeform line, arrow or ellipse.
 *
 * @author Douglas Brown
 */
public class PencilDrawing implements Drawable, Measurable {
	
	protected static final int STYLE_ARROW = 0;
	protected static final int STYLE_ELLIPSE = 1;
	protected static final int STYLE_TRAIL = 2;
	protected static final int MAX_LENGTH = 80;
	protected static final int MIN_LENGTH = 10;
	
  static {
  	XML.setLoader(PencilDrawing.class, PencilDrawing.getLoader());
  }

  protected Color color = Color.black;
  private int style;
  private int numpts = 0; 
	private ArrayList<double[]> pointArray = new ArrayList<double[]>();
	private double[] coords = new double[6]; // used to get path points for XMLLoader
  private double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
  private double ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
  private Stroke drawingStroke;
  private GeneralPath generalPath = new GeneralPath();
  private Ellipse2D ellipse;
  private Line2D[] arrowhead;
	protected int arrowheadLength = 20;
	
  /**
   * Constructs a PencilDrawing with the default color and stroke.
   */
	private PencilDrawing() {
		setStroke(PencilDrawer.lightStroke);
	}

  /**
   * Constructs a PencilDrawing with a specified color.
   *
   * @param c a Color
   */
	PencilDrawing(Color c) {
		this();
		color = c;
	}
	
	@Override
	public void draw(DrawingPanel panel, Graphics g) {
    if(numpts==0)
      return;
		Graphics2D g2 = (Graphics2D)g;
		Color c = g2.getColor();
    Stroke stroke = g2.getStroke();
    
    g2.setColor(color);
    g2.setStroke(drawingStroke);
  	switch(style) {
  	case STYLE_ARROW:
  		if (arrowhead == null)
  			drawArrow();
	    Shape s = panel.transformShape(arrowhead[0]);
      g2.draw(s);
	    s = panel.transformShape(arrowhead[1]);
      g2.draw(s);
      // drop thru to draw shaft
  	case STYLE_TRAIL:
      s = panel.transformPath(generalPath);
      g2.draw(s);
  		break;
		case STYLE_ELLIPSE:
	    s = panel.transformShape(ellipse);
	    g2.draw(s);
  	}
    // restore graphics
    g2.setStroke(stroke);
		g2.setColor(c);
	}
	
  /**
   * Sets the style. Defined styles are STYLE_TRAIL, STYLE_ARROW, STYLE_ELLIPSE
   *
   * @param newStyle one of the defined styles
   */
	public void setStyle(int newStyle) {
		if (newStyle < 0 || newStyle > 2)
			return;
		style = newStyle;
	}
	
  /**
   * Sets the arrowhead length
   *
   * @param length a length
   */
	public void setArrowheadLength(int length) {
		arrowheadLength = Math.min(Math.max(length, MIN_LENGTH), MAX_LENGTH);
	}
	
  /**
   * Gets the number of points stored in the trail.
   * @return int
   */
  public int getPointCount() {
    return numpts;
  }

  /**
   * Gets the drawing stroke.
   * @return Stroke
   */
  public Stroke getStroke() {
    return drawingStroke;
  }

  /**
   * Sets the drawing stroke.
   * @param stroke Stroke
   */
  public void setStroke(Stroke stroke) {
    drawingStroke = stroke;
  }
  
  /**
   * Clears all points from the trail.
   */
  public void clear() {
    numpts = 0;
    xmax = -Double.MAX_VALUE;
    ymax = -Double.MAX_VALUE;
    xmin = Double.MAX_VALUE;
    ymin = Double.MAX_VALUE;
    generalPath.reset();
  }

  /**
   * Marks a new point and draws a shape of the current style.
   * 
   * @param x double
   * @param y double
   */
  public void markPoint(double x, double y) {
  	addPoint(x, y);
  	switch(style) {
	  	case STYLE_TRAIL:
	  		break;
	  	case STYLE_ARROW:
	  		drawArrow();
	  		break;
			case STYLE_ELLIPSE:
				drawCircle();
  	}
  }

	@Override
	public double getXMin() {
		return xmin;
	}

	@Override
	public double getXMax() {
		return xmax;
	}

	@Override
	public double getYMin() {
		return ymin;
	}

	@Override
	public double getYMax() {
		return ymax;
	}

	@Override
	public boolean isMeasured() {
		return numpts > 0;
	}
	
//  /**
//   * Starts a new drawing by moving to a new point.
//   * 
//   * @param x double
//   * @param y double
//   */
//  private void moveToPoint(double x, double y) {
//    generalPath.moveTo((float) x, (float) y);
//    xmin = Math.min(xmin, x);
//    xmax = Math.max(xmax, x);
//    ymin = Math.min(ymin, y);
//    ymax = Math.max(ymax, y);
//    numpts++;
//  }
//
  /**
   * Adds a point to the drawing.
   * 
   * @param x double
   * @param y double
   */
  private void addPoint(double x, double y) {
    if(numpts == 0) {
      generalPath.moveTo((float) x, (float) y);
    }
    generalPath.lineTo((float) x, (float) y);
    xmin = Math.min(xmin, x);
    xmax = Math.max(xmax, x);
    ymin = Math.min(ymin, y);
    ymax = Math.max(ymax, y);
    numpts++;
  }
		
  /**
   * Gets the points that define the GeneralPath.
   *
   * @return double[][], each point is double[] {Seg_type, x, y}
   */
	private double[][] getPathPoints() {
		pointArray.clear();
		for (PathIterator pi = generalPath.getPathIterator(null); !pi.isDone(); pi.next()) {
	    int type = pi.currentSegment(coords); // type will be SEG_LINETO or SEG_MOVETO
	    if (type==PathIterator.SEG_LINETO) {
	    	pointArray.add(new double[] {coords[0], coords[1]});
	    }
		}
		return pointArray.toArray(new double[pointArray.size()][3]);
	}
	
	private double[][] getEnds() {
		double[][] pts = getPathPoints();
		double[][] ends = new double[][] {{pts[0][0], pts[0][1]}, {pts[numpts-1][0], pts[numpts-1][1]}};
		clear();
		addPoint(ends[0][0], ends[0][1]);
		addPoint(ends[1][0], ends[1][1]);
		return ends;
	}
	
	private void drawArrow() {
		if (numpts < 2) return;
    if (arrowhead == null)
  		arrowhead = new Line2D.Double[] {new Line2D.Double(), new Line2D.Double()};
		double[][] ends = getEnds();
		double xTip = ends[1][0], yTip = ends[1][1];
    double theta = Math.atan2(ends[0][1] - ends[1][1], ends[0][0] - ends[1][0]);
    double x = xTip + arrowheadLength * Math.cos(theta + 0.5);
    double y = yTip + arrowheadLength * Math.sin(theta + 0.5);
    arrowhead[0].setLine(ends[1][0], ends[1][1], x, y);
    x = xTip + arrowheadLength * Math.cos(theta - 0.5);
    y = yTip + arrowheadLength * Math.sin(theta - 0.5);
    arrowhead[1].setLine(ends[1][0], ends[1][1], x, y);
	}
		
	private void drawCircle() {
		if (numpts < 2) return;
		if (ellipse == null)
			ellipse = new Ellipse2D.Double();
		double[][] ends = getEnds();
		ellipse.setFrameFromDiagonal(ends[0][0], ends[0][1], ends[1][0], ends[1][1]);
	}
	
  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load TDrawing data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    @Override
    public void saveObject(XMLControl control, Object obj) {
    	PencilDrawing drawing = (PencilDrawing) obj;
      control.setValue("colorRGB", drawing.color.getRGB()); //$NON-NLS-1$
      control.setValue("points", drawing.getPathPoints()); //$NON-NLS-1$
      control.setValue("style", drawing.style); //$NON-NLS-1$
      if (drawing.style == STYLE_ARROW)
        control.setValue("arrowhead", drawing.arrowheadLength); //$NON-NLS-1$      	
    }

    @Override
    public Object createObject(XMLControl control) {
      return new PencilDrawing();
    }

    @Override
    public Object loadObject(XMLControl control, Object obj) {
    	PencilDrawing drawing = (PencilDrawing) obj;
			if (control.getPropertyNamesRaw().contains("style")) //$NON-NLS-1$
				drawing.setStyle(control.getInt("style"));
      if (control.getPropertyNamesRaw().contains("arrowhead"))
      	drawing.arrowheadLength = control.getInt("arrowhead");
    	drawing.color = new Color(control.getInt("colorRGB")); //$NON-NLS-1$
    	double[][] points = (double[][])control.getObject("points"); //$NON-NLS-1$
    	for (double[] point: points) {
    		if (point.length==3) {
	    		drawing.markPoint(point[1], point[2]);
    		}
    		else {
    			drawing.markPoint(point[0], point[1]);
    		}
    	}
      return drawing;
    }
  }

}
