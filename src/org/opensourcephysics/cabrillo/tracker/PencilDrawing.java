/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2017  Douglas Brown
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.media.core.Trackable;

/**
 * A PencilDrawing is a Trackable Trail.
 *
 * @author Douglas Brown
 */
public class PencilDrawing extends Trail implements Trackable {
	
  protected static Color[] pencilColors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
		Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.WHITE};
  
  static {
  	XML.setLoader(PencilDrawing.class, getLoader());
  }

	public boolean visible = true;
	ArrayList<double[]> pointArray = new ArrayList<double[]>();
	double[] coords = new double[6];
	
  /**
   * 
   * Constructs a PencilDrawing with the default color.
   */
	PencilDrawing() {
		setStroke(new BasicStroke(2));
	}

  /**
   * 
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
  	if (!visible) return;
  	TrackerPanel trackerPanel = (TrackerPanel)panel;
  	if (trackerPanel.isDrawingInImageSpace()) {
  		super.draw(panel, g);
  	}
  }
	
	public double[][] getPoints() {
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
    public void saveObject(XMLControl control, Object obj) {
    	PencilDrawing drawing = (PencilDrawing) obj;
      control.setValue("colorRGB", drawing.color.getRGB()); //$NON-NLS-1$
      control.setValue("points", drawing.getPoints()); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new PencilDrawing();
    }

    public Object loadObject(XMLControl control, Object obj) {
    	PencilDrawing drawing = (PencilDrawing) obj;
    	drawing.color = new Color(control.getInt("colorRGB")); //$NON-NLS-1$
    	double[][] points = (double[][])control.getObject("points"); //$NON-NLS-1$
    	for (double[] point: points) {
    		if (point[0]==PathIterator.SEG_LINETO) {
    			drawing.addPoint(point[1], point[2]);
    		}
    	}
      return drawing;
    }

  }

}
