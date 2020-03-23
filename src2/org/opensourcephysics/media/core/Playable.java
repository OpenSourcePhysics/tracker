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
 * This defines methods used to control time-based media.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface Playable {
  /**
   * Plays the media.
   */
  public void play();

  /**
   * Stops the media.
   */
  public void stop();

  /**
   * Resets the media.
   */
  public void reset();

  /**
   * Gets the current media time in milliseconds.
   *
   * @return the current time in milliseconds
   */
  public double getTime();

  /**
   * Sets the media time in milliseconds.
   *
   * @param millis the desired time in milliseconds
   */
  public void setTime(double millis);

  /**
   * Gets the start time in milliseconds.
   *
   * @return the start time in milliseconds
   */
  public double getStartTime();

  /**
   * Sets the start time in milliseconds.
   *
   * @param millis the desired start time in milliseconds
   */
  public void setStartTime(double millis);

  /**
   * Gets the end time in milliseconds.
   *
   * @return the end time in milliseconds
   */
  public double getEndTime();

  /**
   * Sets the end time in milliseconds.
   *
   * @param millis the desired end time in milliseconds
   */
  public void setEndTime(double millis);

  /**
   * Sets the time to the start time.
   */
  public void goToStart();

  /**
   * Sets the time to the end time.
   */
  public void goToEnd();

  /**
   * Gets the duration of the media.
   *
   * @return the duration of the media in milliseconds
   */
  public double getDuration();

  /**
   * Gets the rate at which the media plays relative to its normal rate.
   *
   * @return the relative play rate. A rate of 1.0 plays at the normal rate.
   */
  public double getRate();

  /**
   * Sets the rate at which the media plays relative to its normal rate.
   *
   * @param rate the relative play rate. A rate of 1.0 plays at the normal rate.
   */
  public void setRate(double rate);

  /**
   * Starts and stops the media.
   *
   * @param playing <code>true</code> starts the media, and
   * <code>false</code> stops it
   */
  public void setPlaying(boolean playing);

  /**
   * Gets whether the media is playing.
   *
   * @return <code>true</code> if the media is playing
   */
  public boolean isPlaying();

  /**
   * Sets the looping behavior of the media.
   * When true, the media restarts when reaching the end.
   *
   * @param looping <code>true</code> if the media is looping
   */
  public void setLooping(boolean looping);

  /**
   * Gets the looping behavior of the media.
   * When true, the video restarts when reaching the end.
   *
   * @return <code>true</code> if the media is looping
   */
  public boolean isLooping();

  /**
   * Adds a PropertyChangeListener to this object.
   *
   * @param listener the listener requesting property change notification
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Adds a PropertyChangeListener to this object.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the listener requesting property change notification
   */
  void addPropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Removes a PropertyChangeListener from this object.
   *
   * @param listener the listener requesting removal
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a PropertyChangeListener from this object.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the listener requesting removal
   */
  void removePropertyChangeListener(String property, PropertyChangeListener listener);

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
