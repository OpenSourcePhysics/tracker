/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
/*----------------------------------------------------------------------------------------*
 * Projector.java version 1.7                                                 Nov  8 1996 *
 * Projector.java version 1.71                                                May 14 1997 *
 *                                                                                        *
 * Copyright (c) 1996 Yanto Suryono. All Rights Reserved.                                 *
 *                                                                                        *
 * Permission to use, copy, and distribute this software for NON-COMMERCIAL purposes      *
 * and without fee is hereby granted provided that this copyright notice appears in all   *
 * copies and this software is not modified in any way                                    *
 *                                                                                        *
 * Please send bug reports and/or corrections/suggestions to                              *
 * Yanto Suryono <d0771@cranesv.egg.kushiro-ct.ac.jp>                                     *
 *----------------------------------------------------------------------------------------*/

import java.awt.Point;
import java.awt.Rectangle;

/**
 * The class <code>Projector</code> projects points in 3D space to 2D space.
 *
 * @author  Yanto Suryono
 * @version 1.71, 17 May 1997
 */
public final class SurfacePlotProjector {
  private double scale_x, scale_y, scale_z;    // 3D scaling factor
  private double distance;                     // distance to object
  private double _2D_scale;                    // 2D scaling factor
  private double rotation, elevation;          // rotation and elevation angle
  private double sin_rotation, cos_rotation;   // sin and cos of rotation angle
  private double sin_elevation, cos_elevation; // sin and cos of elevation angle
  private int _2D_trans_x, _2D_trans_y;        // 2D translation
  private int x1, x2, y1, y2;                  // projection area
  private int center_x, center_y;              // center of projection area
  private int trans_x, trans_y;
  private double factor;
  private double sx_cos, sy_cos, sz_cos;
  private double sx_sin, sy_sin, sz_sin;
  private final double DEGTORAD = Math.PI/180;

  /**
   * The constructor of <code>Projector</code>.
   */
  SurfacePlotProjector() {
    setScaling(1);
    setRotationAngle(0);
    setElevationAngle(0);
    setDistance(10);
    set2DScaling(1);
    set2DTranslation(0, 0);
  }

  /**
   * Sets the projection area.
   *
   * @param r the projection area
   */
  public void setProjectionArea(Rectangle r) {
    x1 = r.x;
    x2 = x1+r.width;
    y1 = r.y;
    y2 = y1+r.height;
    center_x = (x1+x2)/2;
    center_y = (y1+y2)/2;
    trans_x = center_x+_2D_trans_x;
    trans_y = center_y+_2D_trans_y;
  }

  /**
   * Sets the rotation angle.
   *
   * @param angle the rotation angle in degrees
   */
  public void setRotationAngle(double angle) {
    rotation = angle;
    sin_rotation = Math.sin(angle*DEGTORAD);
    cos_rotation = Math.cos(angle*DEGTORAD);
    sx_cos = -scale_x*cos_rotation;
    sx_sin = -scale_x*sin_rotation;
    sy_cos = -scale_y*cos_rotation;
    sy_sin = scale_y*sin_rotation;
  }

  /**
   * Gets current rotation angle.
   *
   * @return the rotation angle in degrees.
   */
  public double getRotationAngle() {
    return rotation;
  }

  /**
   * Gets the sine of rotation angle.
   *
   * @return the sine of rotation angle
   */
  public double getSinRotationAngle() {
    return sin_rotation;
  }

  /**
   * Gets the cosine of rotation angle.
   *
   * @return the cosine of rotation angle
   */
  public double getCosRotationAngle() {
    return cos_rotation;
  }

  /**
   * Sets the elevation angle.
   *
   * @param angle the elevation angle in degrees
   */
  public void setElevationAngle(double angle) {
    elevation = angle;
    sin_elevation = Math.sin(angle*DEGTORAD);
    cos_elevation = Math.cos(angle*DEGTORAD);
    sz_cos = scale_z*cos_elevation;
    sz_sin = scale_z*sin_elevation;
  }

  /**
   * Gets current elevation angle.
   *
   * @return the elevation angle in degrees.
   */
  public double getElevationAngle() {
    return elevation;
  }

  /**
   * Gets the sine of elevation angle.
   *
   * @return the sine of elevation angle
   */
  public double getSinElevationAngle() {
    return sin_elevation;
  }

  /**
   * Gets the cosine of elevation angle.
   *
   * @return the cosine of elevation angle
   */
  public double getCosElevationAngle() {
    return cos_elevation;
  }

  /**
   * Sets the projector distance.
   *
   * @param new_distance the new distance
   */
  public void setDistance(double new_distance) {
    distance = new_distance;
    factor = distance*_2D_scale;
  }

  /**
   * Gets the projector distance.
   *
   * @return the projector distance
   */
  public double getDistance() {
    return distance;
  }

  /**
   * Sets the scaling factor in x direction.
   *
   * @param scaling the scaling factor
   */
  public void setXScaling(double scaling) {
    scale_x = scaling;
    sx_cos = -scale_x*cos_rotation;
    sx_sin = -scale_x*sin_rotation;
  }

  /**
   * Gets the scaling factor in x direction.
   *
   * @return the scaling factor
   */
  public double getXScaling() {
    return scale_x;
  }

  /**
   * Sets the scaling factor in y direction.
   *
   * @param scaling the scaling factor
   */
  public void setYScaling(double scaling) {
    scale_y = scaling;
    sy_cos = -scale_y*cos_rotation;
    sy_sin = scale_y*sin_rotation;
  }

  /**
   * Gets the scaling factor in y direction.
   *
   * @return the scaling factor
   */
  public double getYScaling() {
    return scale_y;
  }

  /**
   * Sets the scaling factor in z direction.
   *
   * @param scaling the scaling factor
   */
  public void setZScaling(double scaling) {
    scale_z = scaling;
    sz_cos = scale_z*cos_elevation;
    sz_sin = scale_z*sin_elevation;
  }

  /**
   * Gets the scaling factor in z direction.
   *
   * @return the scaling factor
   */
  public double getZScaling() {
    return scale_z;
  }

  /**
   * Sets the scaling factor in all direction.
   *
   * @param x the scaling factor in x direction
   * @param y the scaling factor in y direction
   * @param z the scaling factor in z direction
   */
  public void setScaling(double x, double y, double z) {
    scale_x = x;
    scale_y = y;
    scale_z = z;
    sx_cos = -scale_x*cos_rotation;
    sx_sin = -scale_x*sin_rotation;
    sy_cos = -scale_y*cos_rotation;
    sy_sin = scale_y*sin_rotation;
    sz_cos = scale_z*cos_elevation;
    sz_sin = scale_z*sin_elevation;
  }

  /**
   * Sets the same scaling factor for all direction.
   *
   * @param scaling the scaling factor
   */
  public void setScaling(double scaling) {
    scale_x = scale_y = scale_z = scaling;
    sx_cos = -scale_x*cos_rotation;
    sx_sin = -scale_x*sin_rotation;
    sy_cos = -scale_y*cos_rotation;
    sy_sin = scale_y*sin_rotation;
    sz_cos = scale_z*cos_elevation;
    sz_sin = scale_z*sin_elevation;
  }

  /**
   * Sets the 2D scaling factor.
   *
   * @param scaling the scaling factor
   */
  public void set2DScaling(double scaling) {
    _2D_scale = scaling;
    factor = distance*_2D_scale;
  }

  /**
   * Gets the 2D scaling factor.
   *
   * @return the scaling factor
   */
  public double get2DScaling() {
    return _2D_scale;
  }

  /**
   * Sets the 2D translation.
   *
   * @param x the x translation
   * @param y the y translation
   */
  public void set2DTranslation(int x, int y) {
    _2D_trans_x = x;
    _2D_trans_y = y;
    trans_x = center_x+_2D_trans_x;
    trans_y = center_y+_2D_trans_y;
  }

  /**
   * Sets the 2D x translation.
   *
   * @param x the x translation
   */
  public void set2D_xTranslation(int x) {
    _2D_trans_x = x;
    trans_x = center_x+_2D_trans_x;
  }

  /**
   * Gets the 2D x translation.
   *
   * @return the x translation
   */
  public int get2D_xTranslation() {
    return _2D_trans_x;
  }

  /**
   * Sets the 2D y translation.
   *
   * @param y the y translation
   */
  public void set2D_yTranslation(int y) {
    _2D_trans_y = y;
    trans_y = center_y+_2D_trans_y;
  }

  /**
   * Gets the 2D y translation.
   *
   * @return the y translation
   */
  public int get2D_yTranslation() {
    return _2D_trans_y;
  }

  /**
   * Projects 3D points.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   */
  public final Point project(double x, double y, double z) {
    double temp;
    // rotates
    temp = x;
    x = x*sx_cos+y*sy_sin;
    y = temp*sx_sin+y*sy_cos;
    // elevates and projects
    temp = factor/(y*cos_elevation-z*sz_sin+distance);
    return new Point((int) (Math.round(x*temp)+trans_x), (int) (Math.round((y*sin_elevation+z*sz_cos)*-temp)+trans_y));
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
