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
import javax.swing.SwingUtilities;
import org.opensourcephysics.controls.XML;

/**
 * This is a ClipControl that uses the video itself for timing.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoClipControl extends ClipControl {
  /**
   * Constructs a VideoClipControl.
   *
   * @param videoClip the video clip
   */
  protected VideoClipControl(VideoClip videoClip) {
    super(videoClip);
    video.addPropertyChangeListener(this);
  }

  /**
   * Plays the clip.
   */
  public void play() {
    video.play();
  }

  /**
   * Stops at the next step.
   */
  public void stop() {
    video.stop();
  }

  /**
   * Steps forward one step.
   */
  public void step() {
    video.stop();
    setStepNumber(stepNumber+1);
  }

  /**
   * Steps back one step.
   */
  public void back() {
    video.stop();
    setStepNumber(stepNumber-1);
  }

  /**
   * Sets the step number.
   *
   * @param n the desired step number
   */
  public void setStepNumber(int n) {
    if(n==stepNumber && clip.stepToFrame(n)==getFrameNumber()) {
      return;
    }
    n = Math.max(0, n);
    final int stepNum = Math.min(clip.getStepCount()-1, n);
    Runnable runner = new Runnable() {
      public void run() {
        int m = clip.stepToFrame(stepNum)+clip.getFrameShift();
        video.setFrameNumber(m);
      }

    };
    SwingUtilities.invokeLater(runner);
  }

  /**
   * Gets the step number.
   *
   * @return the current step number
   */
  public int getStepNumber() {
    return clip.frameToStep(video.getFrameNumber());
  }

  /**
   * Sets the play rate.
   *
   * @param newRate the desired rate
   */
  public void setRate(double newRate) {
    if((newRate==0)||(newRate==rate)) {
      return;
    }
    rate = Math.abs(newRate);
    video.setRate(rate);
  }

  /**
   * Gets the play rate.
   *
   * @return the current rate
   */
  public double getRate() {
    return video.getRate();
  }

  /**
   * Turns on/off looping.
   *
   * @param loops <code>true</code> to turn looping on
   */
  public void setLooping(boolean loops) {
    if(loops==isLooping()) {
      return;
    }
    video.setLooping(loops);
  }

  /**
   * Gets the looping status.
   *
   * @return <code>true</code> if looping is on
   */
  public boolean isLooping() {
    return video.isLooping();
  }

  /**
   * Gets the current frame number.
   *
   * @return the frame number
   */
  public int getFrameNumber() {
  	int n = video.getFrameNumber()-clip.getFrameShift();
  	n = Math.max(0, n); // can't be negative
  	return n;
  }

  /**
   * Gets the playing status.
   *
   * @return <code>true</code> if playing
   */
  public boolean isPlaying() {
    return video.isPlaying();
  }

  /**
   * Gets the current time in milliseconds measured from step 0.
   *
   * @return the current time
   */
  public double getTime() {
    int n = video.getFrameNumber();
    return(video.getFrameTime(n)-video.getStartTime())*timeStretch;
  }

  /**
   * Gets the start time of the specified step measured from step 0.
   *
   * @param stepNumber the step number
   * @return the step time
   */
  public double getStepTime(int stepNumber) {
    int n = clip.stepToFrame(stepNumber);
    return(video.getFrameTime(n)-video.getStartTime())*timeStretch;
  }

  /**
   * Sets the frame duration.
   *
   * @param duration the desired frame duration in milliseconds
   */
  public void setFrameDuration(double duration) {
    if(duration==0) {
      return;
    }
    duration = Math.abs(duration);
    double ti = video.getFrameTime(video.getStartFrameNumber());
    double tf = video.getFrameTime(video.getEndFrameNumber());
    int count = video.getEndFrameNumber()-video.getStartFrameNumber();
    if(count!=0) {
      timeStretch = duration*count/(tf-ti);
    }
    support.firePropertyChange("frameduration", null, new Double(duration)); //$NON-NLS-1$
  }

  /**
   * Gets the mean frame duration in milliseconds.
   *
   * @return the frame duration in milliseconds
   */
  public double getMeanFrameDuration() {
    int count = video.getEndFrameNumber()-video.getStartFrameNumber();
    if(count!=0) {
      double ti = video.getFrameTime(video.getStartFrameNumber());
      double tf = video.getFrameTime(video.getEndFrameNumber());
      return timeStretch*(tf-ti)/count;
    }
    return timeStretch*video.getDuration()/video.getFrameCount();
  }

  /**
   * Responds to property change events. VideoClipControl listens for the
   * following events: "playing", "looping", "rate" and "framenumber" from Video.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("framenumber")) {                        // from Video //$NON-NLS-1$
      int n = ((Integer) e.getNewValue()).intValue();
      if(n==videoFrameNumber) {
        super.setFrameNumber(n-clip.getFrameShift());
        return;
      }
      super.setFrameNumber(n-clip.getFrameShift());
      Integer nInt = new Integer(stepNumber);
      support.firePropertyChange("stepnumber", null, nInt); // to VideoPlayer //$NON-NLS-1$
    } 
    else if(name.equals("playing")) {                     // from Video //$NON-NLS-1$
//      boolean playing = ((Boolean) e.getNewValue()).booleanValue();
//      if(!playing) {
//        setStepNumber(stepNumber+1);
//      }
      support.firePropertyChange(e);                        // to VideoPlayer
    } 
    else if(name.equals("rate")                           // from Video //$NON-NLS-1$
           || name.equals("looping")) {                     // from Video //$NON-NLS-1$
      support.firePropertyChange(e);                        // to VideoPlayer
    }
    else super.propertyChange(e);
  }

  /**
   * Removes this listener from the video so it can be garbage collected.
   */
  public void dispose() {
    video.removePropertyChangeListener(this);
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
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
