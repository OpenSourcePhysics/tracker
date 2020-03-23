/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Grid draws a rectangular grid on a data panel.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class Grid implements Drawable {
  protected int nx, ny; // number of cells in x and y directions
  protected double xmin, xmax, ymin, ymax;
  protected double dx, dy;
  protected Color color = new Color(200, 200, 200, 100);
  protected GeneralPath generalPath = new GeneralPath();
  protected boolean visible = true;

  /**
   * Constructs a square grid of the given size with a spacing of 1.
   * @param n number of cells on a side
   */
  public Grid(int n) {
    this(n, n, 0, n, 0, n);
  }

  /**
   * Constructs an (nx,ny) grid an x spacing of 1 and a y spacing of 1.
   *
   * @param nx  the number of grid lines in the x direction
   * @param ny  the number of grid lines in the y direction
   */
  public Grid(int nx, int ny) {
    this(nx, ny, 0, nx, 0, ny);
  }

  /**
   * Constructs a grid with the given number of x and y points and  the given range.
   *
   * @param _nx  the number of grid lines in the x direction
   * @param _ny  the number of grid lines in the y direction
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public Grid(int _nx, int _ny, double xmin, double xmax, double ymin, double ymax) {
    nx = _nx;
    ny = _ny;
    setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Sets the visible flag.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Checks if the grid is visible.
   *
   * @return true if visible; false otherwise
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Sets the drawing color.
   *
   * @param _color
   */
  public void setColor(Color _color) {
    color = _color;
  }

  /**
   * Gets the drawing color.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }

  /**
   * Gets the x separation between x gid lines.
   * @return dx
   */
  public double getDx() {
    return dx;
  }

  /**
   * Gets the minimum value of x.
   * @return xmin
   */
  public double getXMin() {
    return xmin;
  }

  /**
   * Gets the maximum value of x.
   * @return xamx
   */
  public double getXMax() {
    return xmax;
  }

  /**
   * Gets the minimum value of y.
   * @return ymin
   */
  public double getYMin() {
    return ymin;
  }

  /**
   * Gets the maximum value of y.
   * @return ymax
   */
  public double getYMax() {
    return ymax;
  }

  /**
   * Gets the y separation between x gid lines.
   * @return dy
   */
  public double getDy() {
    return dy;
  }

  /**
   * Assigns a scale to the grid  in world units.
   *
   * @param _xmin
   * @param _xmax
   * @param _ymin
   * @param _ymax
   */
  public void setMinMax(double _xmin, double _xmax, double _ymin, double _ymax) {
    generalPath.reset();
    xmin = (float) _xmin;
    xmax = (float) _xmax;
    ymin = (float) _ymin;
    ymax = (float) _ymax;
    if(nx>0) {
      dx = (float) ((xmax-xmin)/nx);
    } else {
      dx = 1;
    }
    if(ny>0) {
      dy = (float) ((ymax-ymin)/ny);
    } else {
      dy = 1;
    }
    if(!visible) { // don't calculate the general path unless this grid is visible
      return;
    }
    float y = (float) ymin;
    if(ny<=512) {
      for(int i = 0; i<=ny; i++) {
        generalPath.moveTo((float) xmin, y);
        generalPath.lineTo((float) xmax, y);
        y += dy;
      }
    }
    float x = (float) xmin;
    if(nx<=512) {
      for(int i = 0; i<=nx; i++) {
        generalPath.moveTo(x, (float) ymin);
        generalPath.lineTo(x, (float) ymax);
        x += dx;
      }
    }
  }

  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(Math.abs(panel.getXPixPerUnit()*(xmax-xmin)/nx)<4) {
      return;
    }
    if(Math.abs(panel.getYPixPerUnit()*(ymax-ymin)/ny)<4) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    AffineTransform at = panel.getPixelTransform();
    Shape s = generalPath.createTransformedShape(at);
    g2.setColor(color);
    g2.setStroke(new BasicStroke(2)); // BH just a bit better in JavaScript
    g2.draw(s);
    g2.setColor(Color.black);
  }

  /**
   * Gets the cell column and row index for the specified location
   */
  public int[] getCellPoint(double x, double y) {
    int xindex = 0;
    xindex = (int) Math.floor((x-xmin)/dx);
    xindex = Math.max(0, xindex);  // cannot be less than xmin
    xindex = Math.min(nx, xindex); // cannot be greater than xmax
    int yindex = 0;
    yindex = (int) Math.floor((y-ymin)/dy);
    yindex = Math.max(0, yindex);  // cannot be less than ymin
    yindex = Math.min(ny, yindex); // cannot be greater than ymax
    return new int[] {xindex, xindex};
  }

  /**
   * Gets the grid point closest to the specified location
   */
  public Point2D.Double getClosestGridPoint(double x, double y) {
    int index = 0;
    index = (int) Math.round((x-xmin)/dx);
    index = Math.max(0, index);  // cannot be less than xmin
    index = Math.min(nx, index); // cannot be greater than xmax
    double gridx = xmin+dx*index;
    index = (int) Math.round((y-ymin)/dy);
    index = Math.max(0, index);  // cannot be less than ymin
    index = Math.min(ny, index); // cannot be greater than ymax
    double gridy = ymin+dy*index;
    return new Point2D.Double(gridx, gridy);
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
