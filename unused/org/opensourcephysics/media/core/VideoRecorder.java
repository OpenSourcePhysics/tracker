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
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

/**
 * This defines methods for creating a video from a series of images.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface VideoRecorder {
  /**
   * Creates a new video to which frames can be added.
   * @throws IOException
   */
  public void createVideo() throws IOException;

  /**
   * Creates a new video with the specified file name.
   *
   * @param fileName name of the file to which the video will be written
   * @throws IOException
   */
  public void createVideo(String fileName) throws IOException;

  /**
   * Sets the size of the video.
   *
   * @param dimension the dimensions of the new video
   */
  public void setSize(Dimension dimension);

  /**
   * Sets the time duration per frame for subsequent added frames.
   * @param millis the duration per frame in milliseconds
   */
  public void setFrameDuration(double millis);

  /**
   * Adds a video frame to the current video.
   *
   * @param image the image to be drawn on the video frame.
   * @throws IOException
   */
  public void addFrame(Image image) throws IOException;

  /**
   * Gets the current video. Ends editing.
   * @throws IOException
   * @return the active video as a Video object
   */
  public Video getVideo() throws IOException;

  /**
   * Saves the video to the current file.
   * @return the full path of the saved file
   * @throws IOException
   */
  public String saveVideo() throws IOException;

  /**
   * Saves the video to the specified file.
   * @param fileName the file name to be saved
   * @return the full path of the saved file
   * @throws IOException
   */
  public String saveVideo(String fileName) throws IOException;

  /**
   * Saves the video to a file selected with a chooser.
   * @return the full path of the saved file
   * @throws IOException
   */
  public String saveVideoAs() throws IOException;

  /**
   * Gets the current file name. May be null.
   * @return the path of the current destination video file
   */
  public String getFileName();

  /**
   * Sets the file name. May be null.
   * @param path the file name
   */
  public void setFileName(String path);

  /**
   * Discards the current video and resets the recorder to a ready state.
   */
  public void reset();

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
