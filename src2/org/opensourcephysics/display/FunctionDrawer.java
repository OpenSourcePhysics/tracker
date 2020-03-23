/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.numerics.Function;

/**
 * FunctionDrawer draws a function from xmin to xmax.
*
 * The function will be evaluated at every screen pixel unless the domain is set
 * using the initialize method.
 *
 * @author       Wolfgang Christian
 * @author       Joshua Gould
 * @version 1.0
 */
public class FunctionDrawer implements Drawable, Measurable, Function {
  protected double[] xrange = new double[2];
  protected double[] yrange = new double[2];
  protected int numpts = 0;
  protected GeneralPath generalPath = new GeneralPath();
  protected Function function;
  protected boolean filled = false;
  protected boolean measured = false; // set to true if function has been initialized.
  public Color color = Color.black;
  public boolean functionChanged = false;

  /**
   * Contstucts a FunctionDrawer with optimum resolution.
   *
   * @param   f the function that will be drawn.
   */
  public FunctionDrawer(Function f) {
    function = f;
  }

  /**
   * Creates the function drawer and initialzies the domain with the given values.
   * @param f Function
   * @param xmin double
   * @param xmax double
   * @param numpts int
   * @param filled boolean  fills the area under the curve with the drawing color when true
   */
  public FunctionDrawer(Function f, double xmin, double xmax, int numpts, boolean filled) {
    this(f);
    initialize(xmin, xmax, numpts, filled);
  }

  /**
   * Evalutes the function.
   * @param x
   * @return value of the function
   */
  public double evaluate(double x) {
    return function.evaluate(x);
  }

  /**
   * Initialize the function range and the number of display points.
   * @param xmin  the beginning value of the range.
   * @param xmax  the ending value for the range
   * @param numpts the number of points to display
   * @param filled fills the area under the curve with the drawing color when true
   */
  public void initialize(double xmin, double xmax, int numpts, boolean filled) {
    if(numpts<1) {
      return;
    }
    this.filled = filled;
    this.xrange[0] = xmin;
    this.xrange[1] = xmax;
    this.numpts = numpts;
    generalPath.reset();
    if(numpts<1) {
      return;
    }
    yrange[0] = function.evaluate(xmin);
    yrange[1] = yrange[0]; // starting values for ymin and ymax
    if(filled) {
      generalPath.moveTo((float) xrange[0], 0);
      generalPath.lineTo((float) xrange[0], (float) yrange[0]);
    } else {
      generalPath.moveTo((float) xrange[0], (float) yrange[0]);
    }
    double x = xrange[0];
    double dx = (xmax-xmin)/(numpts);
    for(int i = 0; i<numpts; i++) {
      x = x+dx;
      double y = function.evaluate(x);
      generalPath.lineTo((float) x, (float) y);
      if(y<yrange[0]) {
        yrange[0] = y; // the minimum value
      }
      if(y>yrange[1]) {
        yrange[1] = y; // the maximum value
      }
    }
    if(filled) {
      generalPath.lineTo((float) x, 0);
      generalPath.closePath();
    }
    measured = true;
  }

  /**
   * Gets the general path that draws this function.
   * @return GeneralPath
   */
  public GeneralPath getPath() {
    return(GeneralPath) (generalPath.clone());
  }

  /**
   * Get the range of x values over which the function has been evaluated.
   *
   * @return double[2]      the xmin and xmax values
   */
  public double[] getXRange() {
    return xrange;
  }

  /**
   * Get the minimum and maximum y values for the function.
   *
   * @return double[2]      the ymin and ymax values
   */
  public double[] getYRange() {
    return yrange;
  }

  protected void checkRange(DrawingPanel panel) {
    // check to see if the range or function has changed
    if((xrange[0]==panel.getXMin())&&(xrange[1]==panel.getXMax())&&(numpts==panel.getWidth())&&!functionChanged) {
      return;
    }
    functionChanged = false;
    xrange[0] = panel.getXMin();
    xrange[1] = panel.getXMax();
    numpts = panel.getWidth();
    generalPath.reset();
    if(numpts<1) {
      return;
    }
    yrange[0] = function.evaluate(xrange[0]);
    yrange[1] = yrange[0]; // starting values for ymin and ymax
    if(filled) {
      generalPath.moveTo((float) xrange[0], 0);
      generalPath.lineTo((float) xrange[0], (float) yrange[0]);
    } else {
      generalPath.moveTo((float) xrange[0], (float) yrange[0]);
    }
    double x = xrange[0];
    double dx = (xrange[1]-xrange[0])/(numpts);
    for(int i = 0; i<numpts; i++) {
      x = x+dx;
      double y = function.evaluate(x);
      if(!Double.isNaN(x)&&!Double.isNaN(y)) {
        y = Math.min(y, 1.0e+12);
        y = Math.max(y, -1.0e+12);
        generalPath.lineTo((float) x, (float) y);
        if(y<yrange[0]) {
          yrange[0] = y; // the minimum value
        }
        if(y>yrange[1]) {
          yrange[1] = y; // the maximum value
        }
      }
    }
    if(filled) {
      generalPath.lineTo((float) x, 0);
      generalPath.closePath();
    }
  }

  /**
   * Draw the function on a drawing panel.
   * @param panel  the drawing panel
   * @param g      the graphics context
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!measured) {
      checkRange(panel);
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(color);
    // transform from world to pixel coordinates
    Shape s = generalPath.createTransformedShape(panel.getPixelTransform());
    if(filled) {
      g2.fill(s);
      g2.draw(s);
    } else {
      g2.draw(s);
    }
  }

  /**
   * Fills the area under the curve when true.
   * @param _filled boolean
   */
  public void setFilled(boolean _filled) {
    filled = _filled;
  }

  /**
   * Sets the drawing color.
   * @param c Color
   */
  public void setColor(Color c) {
    color = c;
  }

  // Implementation of measured interface.
  public boolean isMeasured() {
    return measured;
  }

  public double getXMin() {
    return xrange[0];
  }

  public double getXMax() {
    return xrange[1];
  }

  public double getYMin() {
    return yrange[0];
  }

  public double getYMax() {
    return yrange[1];
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
