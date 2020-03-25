/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.event.MouseEvent;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.InteractivePanel;

/**
 * Builds a coordinate string from a mouse event for an axis type.
 */
public class PolarCoordinateStringBuilder extends CoordinateStringBuilder {
  protected String rLabel = "r=";      //$NON-NLS-1$
  protected String phiLabel = " phi="; //$NON-NLS-1$
  protected double sin = 0, cos = 1;

  /**
   * Constructor PolarCoordinateStringBuilder
   */
  public PolarCoordinateStringBuilder() {
    this("r=", "  phi="); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Constructor PolarCoordinateStringBuilder
   * @param rLabel
   * @param phiLabel
   * @param phiZero
   */
  public PolarCoordinateStringBuilder(String rLabel, String phiLabel, double phiZero) {
    this(rLabel, phiLabel);
    sin = -Math.sin(phiZero);
    cos = Math.cos(phiZero);
  }

  /**
   * Constructor PolarCoordinateStringBuilder
   * @param rLabel
   * @param phiLabel
   */
  public PolarCoordinateStringBuilder(String rLabel, String phiLabel) {
    this.rLabel = rLabel;
    this.phiLabel = phiLabel;
  }

  public void setCoordinateLabels(String rLabel, String phiLabel) {
    this.rLabel = rLabel;
    this.phiLabel = phiLabel;
  }

  /**
   * Converts a the pixel coordinates in a mouse event into world coordinates and
   * return these coordinates in a string.
   *
   * @param e the mouse event
   * @return the coordinate string
   */
  public String getCoordinateString(DrawingPanel panel, MouseEvent e) {
    double x = panel.pixToX(e.getPoint().x);
    double y = panel.pixToY(e.getPoint().y);
    if((panel instanceof InteractivePanel)&&((InteractivePanel) panel).getCurrentDraggable()!=null) {
      x = ((InteractivePanel) panel).getCurrentDraggable().getX();
      y = ((InteractivePanel) panel).getCurrentDraggable().getY();
    }
    double r = Math.sqrt(x*x+y*y);
    String msg;
    if((r>100)||(r<0.01)) {
      msg = rLabel+scientificFormat.format((float) r);
    } else {
      msg = rLabel+decimalFormat.format((float) r);
    }
    msg += phiLabel+decimalFormat.format(180*(float) Math.atan2(x*sin+y*cos, x*cos-y*sin)/Math.PI);
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
