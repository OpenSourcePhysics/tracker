/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Graphics;
import org.opensourcephysics.display.DrawingPanel;

/**
 * PolarAxes defines common polar coordinate methods.
 *
 * @author W. Christian
 */
public interface PolarAxes extends DrawableAxes {
  /**
   * Sets the spacing of the radial gridlines.
   * @param dr
   */
  public void setDeltaR(double dr);

  /**
   * Automatically sets the spacing of the radial gridlines.
   * @param dr
   */
  public void autospaceRings(boolean autoscale);

  /**
   * Gets the spacing of the radial gridlines.
   */
  public double getDeltaR();

  /**
   * Sets the spacing of the radial gridlines.
   * @param dtheta in degree
   */
  public void setDeltaTheta(double dtheta);

  /**
   * Gets the spacing of the radial gridlines.
   */
  public double getDeltaTheta();

  /**
   * Draws the rings for the polar plot.  The ring spacing used will depend on the resolution and the autoscale flag.
   * @param panel
   * @param g
   *
   * @return double the ring spacing that was used
   */
  public double drawRings(double rmax, DrawingPanel panel, Graphics g);

  /**
   * Draws the spokes for the polar plot.
   * @param panel
   * @param g
   */
  public void drawSpokes(double rmax, DrawingPanel panel, Graphics g);

  /**
   * Sets the interior background color.
   */
  public void setInteriorBackground(Color color);

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
