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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

/**
 * Draws a coil spring in a drawing panel.
 * @author F. Esquembre
 * @author W. Christian
 * @version 1.0
 */
public class Spring implements Measurable {
  private GeneralPath springPath = new GeneralPath();
  // Configuration variables
  protected boolean thinExtremes = true;
  protected boolean visible = true;
  protected int loops = -1, pointsPerLoop = -1;            // -1 to make sure the arrays are allocated
  protected float x = 0.0f, y = 0.0f;
  protected float sizex = 0.1f, sizey = 0.0f;

  /**
   * The radius of the spring (normal to its direction)
   */
  protected float radius = 0.1f;
  protected float solenoid = 0.0f;
  protected Color edgeColor = Color.BLACK;
  protected Stroke edgeStroke = new BasicStroke(1.0f);
  // Implementation variables
  protected boolean hasChanged = true, zeroLength = false; // Whether the element should recompute data that depends on position, size, scale, resolution, ...
  private int segments = 0;
  private float xPoints[] = null, yPoints[] = null;

  /**
   * Constructs a 0.1 radius Spring.
   */
  public Spring() {
    this(0.1);
  }

  /**
   * Special constructor that allows to specify the radius of the spring
   * @param _radius the radius of the spring (normal to its direction)
   */
  public Spring(double _radius) {
    setRadius(_radius);
    setResolution(8, 15);
  }

  // -------------------------------------
  // Configuration methods
  // -------------------------------------

  /**
   * Sets the X position of the origin of the spring
   * @param x double
   */
  public void setX(double x) {
    this.x = (float) x;
    hasChanged = true;
  }

  /**
   * Gets the X position of the origin of the spring
   * @return double
   */
  public double getX() {
    return this.x;
  }

  /**
   * Sets the Y position of the origin of the spring
   * @param y double
   */
  public void setY(double y) {
    this.y = (float) y;
    hasChanged = true;
  }

  /**
   * Gets the Y position of the origin of the spring
   * @return double
   */
  public double getY() {
    return this.y;
  }

  /**
   * Sets the position of the origin of the spring
   * @param x double
   * @param y double
   */
  public void setXY(double x, double y) {
    this.x = (float) x;
    this.y = (float) y;
    hasChanged = true;
  }

  /**
   * Sets the X size of the spring
   * @param sizeX double
   */
  public void setSizeX(double sizeX) {
    this.sizex = (float) sizeX;
    hasChanged = true;
  }

  /**
   * Gets the X size of the spring
   * @return double
   */
  public double getSizeX() {
    return this.sizex;
  }

  /**
   * Sets the Y size of the spring
   * @param sizeY double
   */
  public void setSizeY(double sizeY) {
    this.sizey = (float) sizeY;
    hasChanged = true;
  }

  /**
   * Gets the Y size of the spring
   * @return double
   */
  public double getSizeY() {
    return this.sizey;
  }

  /**
   * Sets the size of the spring
   * @param sizeX double
   * @param sizeY double
   */
  public void setSizeXY(double sizeX, double sizeY) {
    this.sizex = (float) sizeX;
    this.sizey = (float) sizeY;
    hasChanged = true;
  }

  /**
   * Set the radius of the spring.
   * @param _radius the radius of the spring (normal to its direction)
   */
  public void setRadius(double radius) {
    this.radius = (float) radius;
    hasChanged = true;
  }

  /**
   * Get the radius of the spring.
   */
  public double getRadius() {
    return this.radius;
  }

  /**
   * Sets the visibiliby of the spring
   * @param _visible boolean
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setEdgeColor(Color color) {
    this.edgeColor = color;
  }

  public Color getEdgeColor() {
    return this.edgeColor;
  }

  public void setEdgeStroke(Stroke stroke) {
    this.edgeStroke = stroke;
  }

  public Stroke getEdgeStroke() {
    return this.edgeStroke;
  }

  /**
   * Sets the number of spires and points per spire used to draw the spring
   * @param nLoops int
   * @param nPointsPerLoop int
   */
  public void setResolution(int nLoops, int nPointsPerLoop) {
    if((nLoops==loops)&&(nPointsPerLoop==pointsPerLoop)) {
      return; // No need to reallocate arrays
    }
    this.loops = nLoops;
    this.pointsPerLoop = nPointsPerLoop;
    segments = loops*pointsPerLoop;
    int n = segments+1;
    xPoints = new float[n];
    yPoints = new float[n];
    hasChanged = true;
  }

  public int getLoops() {
    return this.loops;
  }

  public int getPointsPerLoop() {
    return this.pointsPerLoop;
  }

  /**
   * Sets a double factor that makes the spring look like a solenoid by
   * causing the spires to go back and forth
   * Default is 0, which makes a standard spring
   * @param factor double
   */
  public void setSolenoid(double factor) {
    solenoid = (float) factor;
    hasChanged = true;
  }

  /**
   * Whether the spring should show thin extremes. Default is true
   * @param thin boolean
   */
  public void setThinExtremes(boolean thin) {
    thinExtremes = thin;
    hasChanged = true;
  }

  /**
   * Implementation of Drawable.
   * @param panel DrawingPanel
   * @param g Graphics
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(hasChanged) {
      computePoints();
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setStroke(edgeStroke);
    g2.setColor(edgeColor);
    if(zeroLength) {
      int a = panel.xToPix(x), b = panel.yToPix(y);
      g2.drawLine(a, b, a, b);
      return;
    }
    Shape s = springPath.createTransformedShape(panel.getPixelTransform());
    g2.draw(s);
  }

  // -------------------------------------
  // Implementation of Measured3D
  // -------------------------------------
  public boolean isMeasured() {
    return visible;
  }

  public double getXMin() {
    return(sizex>0) ? x : x+sizex;
  }

  public double getXMax() {
    return(sizex>0) ? x+sizex : x;
  }

  public double getYMin() {
    return(sizey>0) ? y : y+sizey;
  }

  public double getYMax() {
    return(sizey>0) ? y+sizey : y;
  }

  // -------------------------------------
  //  Private methods
  // -------------------------------------
  private void computeGeneralPath() {
    if((xPoints==null)||(xPoints.length<2)) {
      return;
    }
    int n = xPoints.length;
    springPath = new GeneralPath();
    springPath.moveTo(xPoints[0], yPoints[0]);
    for(int i = 1; i<n; i++) {
      springPath.lineTo(xPoints[i], yPoints[i]);
    }
  }

  private void computePoints() {
    float length = sizex*sizex+sizey*sizey;
    if(length==0) {
      zeroLength = true;
      return;
    }
    zeroLength = false;
    length = (float) Math.sqrt(length);
    float u2x = -sizey/length, u2y = sizex/length;
    float delta = (float) (2.0f*Math.PI/pointsPerLoop);
    if(radius<0) {
      delta *= -1;
    }
    int pre = pointsPerLoop/2;
    for(int i = 0; i<=segments; i++) {
      int k;
      if(thinExtremes) {
        if(i<pre) {
          k = 0;
        } else if(i<pointsPerLoop) {
          k = i-pre;
        } else if(i>(segments-pre)) {
          k = 0;
        } else if(i>(segments-pointsPerLoop)) {
          k = segments-i-pre;
        } else {
          k = pre;
        }
      } else {
        k = pre;
      }
      float angle = (float) (Math.PI/2+i*delta);
      float cos = (float) Math.cos(angle); //, sin = Math.sin(angle);
      xPoints[i] = (x+i*sizex/segments+k*radius*cos*u2x/pre);
      yPoints[i] = (y+i*sizey/segments+k*radius*cos*u2y/pre);
      if(solenoid!=0.0) {
        double cte = k*Math.cos(i*2*Math.PI/pointsPerLoop)/pre;
        xPoints[i] += solenoid*cte*sizex;
        yPoints[i] += solenoid*cte*sizey;
      }
    }
    computeGeneralPath();
    hasChanged = false;
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
