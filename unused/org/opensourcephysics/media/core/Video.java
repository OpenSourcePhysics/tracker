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

import java.beans.PropertyChangeListener;

/**
 * This defines methods to control a video image sequence.
 * Individual images within the sequence are referred to as frames.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface Video extends InteractiveImage, Playable, Trackable, PropertyChangeListener {
  /**
   * Steps forward in the video.
   */
  public void step();

  /**
   * Steps backward in the video.
   */
  public void back();

  /**
   * Gets the total number of frames.
   * @return the number of frames in the image sequence
   */
  public int getFrameCount();

  /**
   * Gets the current frame number.
   * @return the number of the current frame
   */
  public int getFrameNumber();

  /**
   * Sets the frame number.
   * @param n a number between getStartFrameNumber() and getEndFrameNumber()
   * @see #getStartFrameNumber
   * @see #getEndFrameNumber
   */
  public void setFrameNumber(int n);

  /**
   * Gets the start frame number.
   * @return the number of the start frame
   * @see #getEndFrameNumber
   */
  public int getStartFrameNumber();

  /**
   * Sets the start frame number.
   * @param n a number between 0 and getEndFrameNumber()
   * @see #setEndFrameNumber
   */
  public void setStartFrameNumber(int n);

  /**
   * Gets the end frame number.
   * @return the number of the end frame
   * @see #getStartFrameNumber
   */
  public int getEndFrameNumber();

  /**
   * Sets the end frame number.
   * @param n a number between getStartFrameNumber() and getFrameCount()
   * @see #setStartFrameNumber
   */
  public void setEndFrameNumber(int n);

  /**
   * Gets the start time of the specified frame in milliseconds.
   * @param n the frame number
   * @return the start time of the frame in milliseconds
   */
  public double getFrameTime(int n);

  /**
   * Gets the duration of the specified frame in milliseconds.
   * @param n the frame number
   * @return the duration of the frame in milliseconds
   */
  public double getFrameDuration(int n);

  /**
   * Sets x position of UL corner of the specified video frame
   * in world units.
   *
   * @param n the video frame number
   * @param x the world x position
   */
  public void setFrameX(int n, double x);

  /**
   * Sets y position of UL corner of the specified video frame
   * in world units.
   *
   * @param n the video frame number
   * @param y the world y position
   */
  public void setFrameY(int n, double y);

  /**
   * Sets the x and y position of the UL corner of the
   * specified video frame in world units.
   *
   * @param n the video frame number
   * @param x the world x position
   * @param y the world y position
   */
  public void setFrameXY(int n, double x, double y);

  /**
   * Sets the relative aspect of the specified video frame.
   * The pixel aspect of an image is the ratio of its pixel width to height.
   * Its world aspect is the ratio of width to height in world units.
   * For example, a 320 x 240 pixel image has a pixel aspect of 4/3.
   * If its relative aspect is set to 2, then the world aspect of the image will be 8/3.
   * This means that if the image width is set to 16, its height will be 6.
   * Conversely, if its height is set to 10, its width will be 8/3 x 10 = 26.666.
   *
   * @param n the video frame number
   * @param relativeAspect the world aspect of the image relative to its pixel aspect.
   */
  public void setFrameRelativeAspect(int n, double relativeAspect);

  /**
   * Sets the width of the specified video frame in world units. This
   * method also sets the height using the relative aspect.
   *
   * @param n the video frame number
   * @param width the width in world units
   */
  public void setFrameWidth(int n, double width);

  /**
   * Sets the height of the specified video frame in world units. This
   * method also sets the width using the relative aspect.
   *
   * @param n the video frame number
   * @param height the height in world units
   */
  public void setFrameHeight(int n, double height);

  /**
   * Sets the angle in radians of the specified video frame measured ccw
   * from the world x-axis.
   *
   * @param n the video frame number
   * @param angle the angle n radians
   */
  public void setFrameAngle(int n, double angle);

  /**
   * Disposes of this video.
   */
  public void dispose();

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
