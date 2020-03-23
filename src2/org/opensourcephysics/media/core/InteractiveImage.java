/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;

import org.opensourcephysics.display.Interactive;

/**
 * This defines methods used by interactive drawable images.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface InteractiveImage extends Interactive, DrawableImage {
  /**
   * Sets the relative aspect of the displayed image.
   * The pixel aspect of an image is the ratio of its pixel width to height.
   * Its world aspect is the ratio of width to height in world units.
   * For example, a 320 x 240 pixel image has a pixel aspect of 4/3.
   * If its relative aspect is set to 2, then the world aspect of the image will be 8/3.
   * This means that if the image width is set to 16, its height will be 6.
   * Conversely, if its height is set to 10, its width will be 8/3 x 10 = 26.666.
   *
   * @param relativeAspect the world aspect of the image relative to its pixel aspect.
   */
  public void setRelativeAspect(double relativeAspect);

  /**
   * Gets the relative aspect of the displayed image.
   *
   * @return the relative aspect of the displayed image
   * @see #setRelativeAspect
   */
  public double getRelativeAspect();

  /**
   * Sets the width of the image in world units. This method also sets
   * the height using the relative aspect.
   *
   * @param width the width in world units
   * @see #setRelativeAspect
   */
  public void setWidth(double width);

  /**
   * Gets the width of the image in world units.
   *
   * @return the width of the image
   */
  public double getWidth();

  /**
   * Sets the height of the image in world units. This method also sets
   * the width using the relative aspect.
   *
   * @param height the height in world units
   * @see #setRelativeAspect
   */
  public void setHeight(double height);

  /**
   * Gets the height of the image in world units.
   *
   * @return the height of the image
   */
  public double getHeight();

  /**
   * Sets the angle in radians of the image base measured ccw from the
   * world x-axis.
   *
   * @param theta the angle in radians
   */
  public void setAngle(double theta);

  /**
   * Gets the angle in radians of the image base measured ccw from the
   * world x-axis.
   *
   * @return the angle in radians
   */
  public double getAngle();

  /**
   * Gets the image coordinate system.
   * @return the image coordinate system
   */
  public ImageCoordSystem getCoords();

  /**
   * Sets the image coordinate system.
   * @param coords the image coordinate system
   */
  public void setCoords(ImageCoordSystem coords);

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
