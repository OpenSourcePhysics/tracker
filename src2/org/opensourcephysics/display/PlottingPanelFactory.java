/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import org.opensourcephysics.display.axes.AxisFactory;
import org.opensourcephysics.display.axes.PolarAxes;
import org.opensourcephysics.display.axes.PolarType1;
import org.opensourcephysics.display.axes.PolarType2;

public class PlottingPanelFactory {
  private PlottingPanelFactory() {}

  /**
   *  Constructs a new PlottingPanel with cartesian type 1 axes using the given X axis label, Y axis
   *  label, and plot title.
   *
   * @param  xlabel      The X axis label.
   * @param  ylabel      The Y axis label.
   * @param  plotTitle   The plot title.
   */
  public static PlottingPanel createType1(String xlabel, String ylabel, String plotTitle) {
    PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
    panel.axes = AxisFactory.createAxesType1(panel);
    panel.axes.setXLabel(xlabel, null);
    panel.axes.setYLabel(ylabel, null);
    panel.axes.setTitle(plotTitle, null);
    return panel;
  }

  /**
*  Constructs a new PlottingPanel with cartesian type 2 axes using the given X axis label, Y axis
*  label, and plot title.
*
* @param  xlabel      The X axis label.
* @param  ylabel      The Y axis label.
* @param  plotTitle   The plot title.
*/
  public static PlottingPanel createType2(String xlabel, String ylabel, String plotTitle) {
    PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
    panel.axes = AxisFactory.createAxesType2(panel);
    panel.axes.setXLabel(xlabel, null);
    panel.axes.setYLabel(ylabel, null);
    panel.axes.setTitle(plotTitle, null);
    return panel;
  }

  /**
   *  Constructs a new PlottingPanel with polar type 1 axes using the given title.
   *
   * @param  plotTitle   the plot title.
   */
  public static PlottingPanel createPolarType1(String plotTitle, double deltaR) {
    PlottingPanel panel = new PlottingPanel(null, null, plotTitle);
    PolarAxes axes = new PolarType1(panel);
    axes.setDeltaR(deltaR);
    axes.setDeltaTheta(Math.PI/8); // spokes are separate by PI/8
    panel.setTitle(plotTitle);
    panel.setSquareAspect(true);
    return panel;
  }

  /**
*  Constructs a new PlottingPanel with polar type 2 axes using the given title.
*
* @param  plotTitle   the plot title.
*/
  public static PlottingPanel createPolarType2(String plotTitle, double deltaR) {
    PlottingPanel panel = new PlottingPanel(null, null, plotTitle);
    PolarAxes axes = new PolarType2(panel);
    axes.setDeltaR(deltaR);        // circles are separated by one unit
    axes.setDeltaTheta(Math.PI/8); // spokes are separate by PI/8
    panel.setTitle(plotTitle);
    panel.setSquareAspect(true);
    return panel;
  }

  /**
   *  Constructs a new PlottingPanel with cartesian type 3 axes using the given X axis label, Y axis
   *  label, and plot title.
   *
   * @param  xlabel      The X axis label.
   * @param  ylabel      The Y axis label.
   * @param  plotTitle   The plot title.
   */
  public static PlottingPanel createType3(String xlabel, String ylabel, String plotTitle) {
    PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
    panel.axes = AxisFactory.createAxesType3(panel);
    panel.axes.setXLabel(xlabel, null);
    panel.axes.setYLabel(ylabel, null);
    panel.axes.setTitle(plotTitle, null);
    return panel;
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
