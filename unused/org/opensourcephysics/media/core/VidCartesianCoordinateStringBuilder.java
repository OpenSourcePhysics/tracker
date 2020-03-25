/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.media.core;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.axes.CartesianCoordinateStringBuilder;

/**
 * A coordinate string builder for a video panel.
 */
public class VidCartesianCoordinateStringBuilder 
		extends CartesianCoordinateStringBuilder 
		implements XYCoordinateStringBuilder {
	
  protected VidCartesianCoordinateStringBuilder() {
    super();
  }

  protected VidCartesianCoordinateStringBuilder(String xLabel, String yLabel) {
    super(xLabel, yLabel);
  }

  /**
   * Converts the pixel coordinates in a mouse event into world coordinates and
   * return these coordinates in a string.
   *
   * @param panel the drawing panel
   * @param e the mouse event
   * @return the coordinate string
   */
  public String getCoordinateString(DrawingPanel panel, MouseEvent e) {
    if(!(panel instanceof VideoPanel)) {
      return super.getCoordinateString(panel, e);
    }
    VideoPanel vidPanel = (VideoPanel) panel;
    Point2D pt = vidPanel.getWorldMousePoint();
    return getCoordinateString(vidPanel, pt.getX(), pt.getY());
  }

  /**
   * Returns the specified xy coordinates in a string.
   *
   * @param x the x
   * @param y the y
   * @return the coordinate string
   */
  public String getCoordinateString(VideoPanel panel, double x, double y) {
    String msg;
    if((Math.abs(x)>100)||(Math.abs(x)<0.01)||(Math.abs(y)>100)||(Math.abs(y)<0.01)) {
	  	scientificFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
      msg = xLabel+scientificFormat.format((float) x)+yLabel+scientificFormat.format((float) y);
    } else {
	  	decimalFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
      msg = xLabel+decimalFormat.format((float) x)+yLabel+decimalFormat.format((float) y);
    }
    return msg;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
