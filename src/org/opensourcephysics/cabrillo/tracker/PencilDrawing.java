/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Trail;

/**
 * A PencilDrawing is a freeform line.
 *
 * @author Douglas Brown
 */
public class PencilDrawing extends Trail {
	
  static {
  	XML.setLoader(PencilDrawing.class, PencilDrawing.getLoader());
  }

	private ArrayList<double[]> pointArray = new ArrayList<double[]>();
	private double[] coords = new double[6]; // used to get path points for XMLLoader
	
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
		Graphics2D g2 = (Graphics2D)g;
		Color c = g2.getColor();
		super.draw(panel, g);
		// restore color
		g2.setColor(c);
	}
	
  /**
   * Gets the points that define the GeneralPath.
   *
   * @return double[][], each point is double[] {Seg_type, x, y}
   */
	public double[][] getPathPoints() {
		pointArray.clear();
		for (PathIterator pi = generalPath.getPathIterator(null); !pi.isDone(); pi.next()) {
	    int type = pi.currentSegment(coords); // type will be SEG_LINETO or SEG_MOVETO
	    if (type==PathIterator.SEG_LINETO) {
	    	pointArray.add(new double[] {type, coords[0], coords[1]});
	    }
		}
		return pointArray.toArray(new double[pointArray.size()][3]);
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
    }

    @Override
    public Object createObject(XMLControl control) {
      return new PencilDrawing();
    }

    @Override
    public Object loadObject(XMLControl control, Object obj) {
    	PencilDrawing drawing = (PencilDrawing) obj;
    	drawing.color = new Color(control.getInt("colorRGB")); //$NON-NLS-1$
    	double[][] points = (double[][])control.getObject("points"); //$NON-NLS-1$
    	for (double[] point: points) {
    		if (point.length==3) {
	    		if (point[0]==PathIterator.SEG_LINETO) {
	    			drawing.addPoint(point[1], point[2]);
	    		}
    		}
    		else { // legacy points included only SEG_LINETO
    			drawing.addPoint(point[0], point[1]);
    		}
    	}
      return drawing;
    }
  }

}
