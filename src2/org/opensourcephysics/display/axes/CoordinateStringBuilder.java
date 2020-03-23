/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.TeXParser;

/**
 * Builds a coordinate string from a mouse event for an axis type.
 */
public abstract class CoordinateStringBuilder {
  protected DecimalFormat scientificFormat = new DecimalFormat("0.###E0"); // coordinate display format for message box. //$NON-NLS-1$
  protected DecimalFormat decimalFormat = new DecimalFormat("0.00"); // coordinate display format for message box. //$NON-NLS-1$
  protected String xLabel = "x=";   //$NON-NLS-1$
  protected String yLabel = "  y="; //$NON-NLS-1$

  public void setCoordinateLabels(String xLabel, String yLabel) {
    this.xLabel = TeXParser.parseTeX(xLabel);
    this.yLabel = TeXParser.parseTeX(yLabel);
  }

  /**
   * Converts a the pixel coordinates in a mouse event into world coordinates and
   * return these coordinates in a string.
   *
   * @param e the mouse event
   * @return the coordinate string
   */
  public abstract String getCoordinateString(DrawingPanel panel, MouseEvent e);

  /**
   * Creates the default builder for cartesian coordiantes.
   *
   * @return CoordinateStringBuilder
   */
  public static CoordinateStringBuilder createCartesian() {
    return new CartesianCoordinateStringBuilder();
  }

  /**
   * Creates the default builder for polar coordinates.
   *
   * @return CoordinateStringBuilder
   */
  public static CoordinateStringBuilder createPolar() {
    return new PolarCoordinateStringBuilder();
  }

  /**
   * Creates the default builder for polar coordinates.
   *
   * @param rLabel String
   * @param phiLabel String
   * @param phiOffset double
   * @return CoordinateStringBuilder
   */
  public static CoordinateStringBuilder createPolar(String rLabel, String phiLabel, double phiOffset) {
    return new PolarCoordinateStringBuilder(rLabel, phiLabel, phiOffset);
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
