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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * A ClipControl controls a VideoClip. This is an abstract
 * class that cannot be instantiated directly.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public abstract class ClipControl implements PropertyChangeListener {
  // instance fields
  protected int stepNumber = 0;
  protected int videoFrameNumber = 0;
  protected final VideoClip clip;
  protected Video video;
  protected double rate = 1;
  protected boolean looping = false;
  protected PropertyChangeSupport support;
  protected double timeStretch = 1;
  protected DataTrack timeSource;
  protected double savedFrameDuration;
  public boolean videoVisible = true;

  /**
   * Returns an instance of ClipControl.
   *
   * @param clip the video clip
   * @return an appropriate clip control
   */
  public static ClipControl getControl(VideoClip clip) {
    Video video = clip.getVideo();
    if(clip.isPlayAllSteps()||(video==null)||(video instanceof ImageVideo)) {
      return new StepperClipControl(clip);
    }
    return new VideoClipControl(clip);
  }

  /**
   * Constructs a ClipControl object. This is an abstract class that
   * cannot be instantiated directly.
   *
   * @param videoClip the video clip
   */
  protected ClipControl(VideoClip videoClip) {
    clip = videoClip;
    video = clip.getVideo();
    support = new SwingPropertyChangeSupport(this);
  }

  /**
   * Gets the clip that is controlled by this clip control.
   *
   * @return the clip
   */
  public VideoClip getVideoClip() {
    return clip;
  }

  /**
   * Plays the clip.
   */
  public void play() {

  /** implemented by subclasses */
  }

  /**
   * Stops at the next step.
   */
  public void stop() {

  /** implemented by subclasses */
  }

  /**
   * Steps forward one step.
   */
  public void step() {

  /** implemented by subclasses */
  }

  /**
   * Steps back one step.
   */
  public void back() {
  	
  /** implemented by subclasses */
  }

  /**
   * Sets the frame number.
   *
   * @param n the desired frame number
   */
  public void setFrameNumber(int n) {
  	if (clip.includesFrame(n)) {
	    stepNumber = clip.frameToStep(n);
	    n = Math.max(0, n+clip.getFrameShift());
	    videoFrameNumber = n;
  	}
  }

  /**
   * Sets the step number.
   *
   * @param n the desired step number
   */
  public void setStepNumber(int n) {
    stepNumber = n;
    n = Math.max(0, clip.stepToFrame(n)+clip.getFrameShift());
    videoFrameNumber = n;
  }

  /**
   * Gets the step number.
   *
   * @return the current step number
   */
  public int getStepNumber() {
    return stepNumber;
  }

  /**
   * Sets the play rate.
   *
   * @param newRate the desired rate
   */
  public void setRate(double newRate) {
    rate = newRate;
  }

  /**
   * Gets the play rate.
   *
   * @return the current rate
   */
  public double getRate() {
    return rate;
  }

  /**
   * Turns on/off looping.
   *
   * @param loops <code>true</code> to turn looping on
   */
  public void setLooping(boolean loops) {
    looping = loops;
  }

  /**
   * Gets the looping status.
   *
   * @return <code>true</code> if looping is on
   */
  public boolean isLooping() {
    return looping;
  }

  /**
   * Gets the current frame number.
   *
   * @return the frame number
   */
  public int getFrameNumber() {
  	int n = videoFrameNumber-clip.getFrameShift();
  	n = Math.max(0, n); // can't be negative
  	return n;
  }

  /**
   * Gets the DataTrack time source, if any.
   *
   * @return the time source (may be null)
   */
  public DataTrack getTimeSource() {
  	return timeSource;
  }

  /**
   * Sets the time source to a DataTrack.
   *
   * @param source the time source (may be null)
   */
  public void setTimeSource(DataTrack source) {
  	DataTrack prev = timeSource;
  	timeSource = source;
  	if (prev==null && timeSource!=null) {
  		clip.savedStartTime = clip.isDefaultStartTime? Double.NaN: clip.getStartTime();
  		clip.startTimeIsSaved = true;
  		savedFrameDuration = getMeanFrameDuration();
  	}
  	else if (prev!=null && timeSource==null) {
  		clip.setStartTime(clip.savedStartTime);
  		clip.startTimeIsSaved = false;
  		setFrameDuration(savedFrameDuration);
  	}
  	if (timeSource!=null && timeSource.isTimeDataAvailable()) {
  		clip.setStartTime(timeSource.getVideoStartTime()*1000); // convert to ms
  		setFrameDuration(timeSource.getFrameDuration()*1000); // convert to ms
  	}
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("startframe")) {  // from video clip //$NON-NLS-1$
    	int n = getFrameNumber();
	    stepNumber = clip.frameToStep(n);
    }
  }

  /**
   * Gets the playing status.
   *
   * @return <code>true</code> if playing
   */
  public abstract boolean isPlaying();

  /**
   * Gets the current time in milliseconds measured from step 0.
   *
   * @return the current time
   */
  public abstract double getTime();

  /**
   * Gets the start time of the specified step measured from step 0.
   *
   * @param stepNumber the step number
   * @return the step time
   */
  public abstract double getStepTime(int stepNumber);

  /**
   * Sets the frame duration.
   *
   * @param duration the desired frame duration in milliseconds
   */
  public abstract void setFrameDuration(double duration);

  /**
   * Gets the average frame duration in milliseconds.
   *
   * @return the mean frame duration in milliseconds
   */
  public abstract double getMeanFrameDuration();

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property of interest
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  /**
   * Empty dispose method.
   */
  public void dispose() {
  /** implemented by subclasses */
  }
	
  /**
   * Determines if a DataTrack is actively providing time data.
   */
	public static boolean isTimeSource(DataTrack track) {
		if (track.getVideoPanel()==null) return false;
		return track==track.getVideoPanel().getPlayer().getClipControl().getTimeSource();
	}
	
  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves object data in an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      ClipControl clipControl = (ClipControl) obj;
      control.setValue("rate", clipControl.getRate());                 //$NON-NLS-1$
      control.setValue("delta_t", clipControl.getTimeSource()!=null?   //$NON-NLS-1$
      		clipControl.savedFrameDuration: clipControl.getMeanFrameDuration());
      if(clipControl.isLooping()) {
        control.setValue("looping", true); //$NON-NLS-1$
      }
      control.setValue("frame", clipControl.getFrameNumber()); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return null;
    }

    /**
     * Loads a VideoClip with data from an XMLControl.
     *
     * @param control the element
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ClipControl clipControl = (ClipControl) obj;
      // set rate
      double rate = control.getDouble("rate"); //$NON-NLS-1$
      if(rate!=Double.NaN) {
        clipControl.setRate(rate);
      }
      // set dt
      double dt = control.getDouble("delta_t"); //$NON-NLS-1$
      if(dt!=Double.NaN) {
        clipControl.setFrameDuration(dt);
      }
      // set looping and playing
      clipControl.setLooping(control.getBoolean("looping")); //$NON-NLS-1$
      // set frame number
      if (control.getPropertyNames().contains("frame")) { //$NON-NLS-1$
      	int n = control.getInt("frame"); //$NON-NLS-1$
      	n = clipControl.getVideoClip().frameToStep(n);
      	clipControl.setStepNumber(n);
      }
      return obj;
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