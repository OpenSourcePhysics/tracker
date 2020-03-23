/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import org.opensourcephysics.display.DrawingPanel;

/**
 * ComplexInterpolatedPlot creates an image of a scalar field by inerpolating every
 * image pixel to an untabulated point (x,y) in the 2d data.  This interpolation smooths
 * the resulting image.
 *
 * @author     Wolfgang Christian
 * @created    February 2, 2003
 * @version    1.0
 */
public class ComplexContourPlot extends ComplexInterpolatedPlot {
  ContourPlot contour;
  boolean showContours = true;

  /**
   * Constructs the ComplexContourPlot using the given 2d datset.
   */
  public ComplexContourPlot() {
    super(null);
  }

  /**
   * Constructs the ComplexContourPlot using the given 2d datset.
   *
   * @param griddata GridData
   */
  public ComplexContourPlot(GridData griddata) {
    super(griddata);
    contour = new ContourPlot(griddata);
    contour.setPaletteType(ColorMapper.WIREFRAME);
    contour.setShowColorLevels(false);
    contour.setGridLineColor(Color.lightGray);
    contour.update();
  }

  public ContourPlot getContour() {
    return contour;
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z are span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    super.setAutoscaleZ(isAutoscale, ceil);
    contour.setAutoscaleZ(isAutoscale, floor, ceil);
  }

  /**
   * Updates the buffered image using the data array.
   */
  public void update() {
    super.update();
    if((contour!=null)&&showContours) {
      contour.update();
    }
  }

  /**
   * Sets the data storage to the given value.
   *
   * @param griddata
   */
  public void setGridData(GridData griddata) {
    super.setGridData(griddata);
    super.setShowGridLines(false);
    contour.setGridData(griddata);
  }

  /**
   * Sets the indexes for the data component that will be plotted.
   *
   * @param indexes the sample-component
   */
  public void setIndexes(int[] indexes) {
    super.setIndexes(indexes);
    contour.setIndexes(indexes);
  }

  /**
   * Sets the visibility of the contour plot.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Shows how values map to colors.
   */
  public JFrame showLegend() {
    return colorMap.showLegend();
  }

  /**
   * Shows the contour lines.
   *
   * @param show
   */
  public void setShowGridLines(boolean show) {
    contour.setShowGridLines(show);
  }

  /**
   * Setting the color palette is not supported.  The complex palette always maps phase to color.
   * @param colors
   */
  public void setColorPalette(Color[] colors) {}

  /**
   * Setting the palette is not supported.   The complex palette always maps phase to color.
   * @param type
   */
  public void setPaletteType(int type) {}

  /**
   * Sets the floor and ceiling colors.
   *
   * @param floorColor
   * @param ceilColor
   */
  public void setFloorCeilColor(Color floorColor, Color ceilColor) {
    super.setFloorCeilColor(floorColor, ceilColor);
  }

  /**
   * Sets the contour line color.
   * The default line color is dark green.
   * @param color
   */
  public void setGridLineColor(Color color) {
    contour.setGridLineColor(color);
  }

  /**
   * Draws the image and the grid.
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    super.draw(panel, g);
    if(showContours) {
      contour.draw(panel, g);
    }
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
