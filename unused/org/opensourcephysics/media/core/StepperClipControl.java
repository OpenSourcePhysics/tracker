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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XML;

/**
 * This is a ClipControl that displays every step in a video clip.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class StepperClipControl extends ClipControl {
  // instance fields
  private javax.swing.Timer timer;
  private double frameDuration = 100; // milliseconds
  private boolean playing = false;
  private boolean readyToStep = true;
  private boolean stepDisplayed = true;
  private int minDelay = 10;          // milliseconds
  private int maxDelay = 5000;        // milliseconds

  /**
   * Constructs a StepperClipControl object.
   *
   * @param videoClip the video clip
   */
  protected StepperClipControl(VideoClip videoClip) {
    super(videoClip);
    videoClip.addPropertyChangeListener(this);
    if(video!=null) {
      video.addPropertyChangeListener(this);
      if(video.getFrameCount()>1 && video.getDuration()>0) {
        double ti = video.getFrameTime(video.getStartFrameNumber());
        double tf = video.getFrameTime(video.getEndFrameNumber());
        int count = video.getEndFrameNumber()-video.getStartFrameNumber();
        if((count!=0)&&(tf-ti)>0) {
          frameDuration = (int) (tf-ti)/count;
        }
      }
    }
//    int delay = (int) (frameDuration*clip.getStepSize());
//    delay = Math.min(delay, maxDelay);
//    delay = Math.max(delay, minDelay);
    timer = new javax.swing.Timer(getTimerDelay(), new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        readyToStep = true;
        step();
      }

    });
    timer.setRepeats(false);
    // set coalesce to false to avoid combining events when trying to go too fast
    timer.setCoalesce(false);
  }

  /**
   * Plays the clip.
   */
  public void play() {
    if(clip.getStepCount()==1) {
      return;
    }
    playing = true;
    readyToStep = true;
    if(stepNumber==clip.getStepCount()-1) {
      setStepNumber(0);
    } else {
      step();
    }
    support.firePropertyChange("playing", null, new Boolean(true)); //$NON-NLS-1$
  }

  /**
   * Stops at the next step.
   */
  public void stop() {
    timer.stop();
    readyToStep = true;
    stepDisplayed = true;
    playing = false;
    support.firePropertyChange("playing", null, new Boolean(false)); //$NON-NLS-1$
  }

  /**
   * Steps forward one step.
   */
  public void step() {
    if((stepNumber>=clip.getStepCount()-1)&&!looping) {
      stop();
    } else if(stepDisplayed&&(!playing||readyToStep)) {
      stepDisplayed = false;
      if(playing) {
        readyToStep = false;
        timer.restart();
      }
      if(stepNumber<clip.getStepCount()-1) {
        setStepNumber(stepNumber+1);
      } else if(looping) {
        setStepNumber(0);
      }
    }
  }

  /**
   * Steps back one step.
   */
  public void back() {
    if(stepDisplayed&&(stepNumber>0)) {
      stepDisplayed = false;
      setStepNumber(stepNumber-1);
    }
  }

  /**
   * Sets the step number.
   *
   * @param n the desired step number
   */
  public void setStepNumber(int n) {
    n = Math.max(0, n);
    n = Math.min(clip.getStepCount()-1, n);
    if(n==stepNumber && clip.stepToFrame(n)==getFrameNumber()) {
      return;
    }
    if (video==null) {
      super.setStepNumber(n);
      stepDisplayed = true;
      support.firePropertyChange("stepnumber", null, new Integer(n)); //$NON-NLS-1$
    } 
    else {
      int end = video.getEndFrameNumber();
      final int m = clip.stepToFrame(n)+clip.getFrameShift();
      final int stepNum = n;
      if (m>end) {
        super.setStepNumber(n);
        video.setVisible(false);
        stepDisplayed = true;
        support.firePropertyChange("stepnumber", null, new Integer(n)); //$NON-NLS-1$
      }
      else {
        video.setVisible(videoVisible);
	      Runnable runner = new Runnable() {
	        public void run() {
	          if (videoFrameNumber==m) {
	            stepDisplayed = true;
	          } 
	          else {
	            if (video.getFrameNumber()==m) { // setting frame number will have no effect
	              StepperClipControl.super.setStepNumber(stepNum);
	              stepDisplayed = true;
	              support.firePropertyChange("stepnumber", null, new Integer(stepNum)); //$NON-NLS-1$	            	
	            }
	            else {
	            	video.setFrameNumber(m);
	            }
	          }
	        }
	
	      };
	      SwingUtilities.invokeLater(runner);
      }
    }
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
//    if (video==null) {
//	    int delay = (int) (getMeanFrameDuration()*clip.getStepSize()/rate);
//	    delay = Math.min(delay, maxDelay);
//	    delay = Math.max(delay, minDelay);
	    timer.setInitialDelay(getTimerDelay());
//    }
    support.firePropertyChange("rate", null, new Double(rate)); //$NON-NLS-1$
  }

  /**
   * Gets the average frame duration in milliseconds (for calculations).
   *
   * @return the frame duration in milliseconds
   */
  public double getMeanFrameDuration() {
  	if (video!=null && video.getDuration()>0) {
      int count = video.getEndFrameNumber()-video.getStartFrameNumber();
      if(count!=0) {
        double ti = video.getFrameTime(video.getStartFrameNumber());
        double tf = video.getFrameTime(video.getEndFrameNumber());
        return timeStretch*(tf-ti)/count;
      }
      return timeStretch*video.getDuration()/video.getFrameCount();
  	}
    return frameDuration;
  }

  /**
   * Sets the frame duration.
   *
   * @param duration the desired frame duration in milliseconds
   */
  public void setFrameDuration(double duration) {
    duration = Math.abs(duration);
    if(duration==0 || duration==getMeanFrameDuration()) {
      return;
    }
    if (video!=null && video instanceof ImageVideo) {
    	ImageVideo iVideo = (ImageVideo)video;
    	iVideo.setFrameDuration(duration);
	    frameDuration = duration;
	    timer.setInitialDelay(getTimerDelay());
    }
    else if (video!=null && video.getDuration()>0) {
	    double ti = video.getFrameTime(video.getStartFrameNumber());
	    double tf = video.getFrameTime(video.getEndFrameNumber());
	    int count = video.getEndFrameNumber()-video.getStartFrameNumber();
	    if(count!=0) {
	      timeStretch = duration*count/(tf-ti);
	    }
    }
    else {
	    frameDuration = duration;
	    timer.setInitialDelay(getTimerDelay());
    }
    support.firePropertyChange("frameduration", null, new Double(duration)); //$NON-NLS-1$
  }

  /**
   * Turns on/off looping.
   *
   * @param loops <code>true</code> to turn looping on
   */
  public void setLooping(boolean loops) {
    if(loops==looping) {
      return;
    }
    looping = loops;
    support.firePropertyChange("looping", null, new Boolean(loops)); //$NON-NLS-1$
  }

  /**
   * Gets the playing status.
   *
   * @return <code>true</code> if playing
   */
  public boolean isPlaying() {
    return playing;
  }

  /**
   * Gets the current time in milliseconds measured from step 0.
   *
   * @return the current time
   */
  public double getTime() {
  	if (video!=null && video.getDuration()>0) {
      int n = video.getFrameNumber();
      double videoTime = video.getFrameTime(n);
      int m = clip.stepToFrame(getStepNumber())+clip.getFrameShift();
      if (m>video.getFrameCount()-1) {
        int extra = m-video.getFrameCount()+1;
        videoTime = video.getFrameTime(video.getFrameCount()-1)+extra*frameDuration;
      }
      return(videoTime-video.getStartTime())*timeStretch;
  	}
    return stepNumber*frameDuration*clip.getStepSize();
  }

  /**
   * Gets the start time of the specified step measured from step 0.
   *
   * @param stepNumber the step number
   * @return the step time
   */
  public double getStepTime(int stepNumber) {
  	if (video!=null && video.getDuration()>0) {
      int n = clip.stepToFrame(stepNumber)+clip.getFrameShift();
      double videoTime = video.getFrameTime(n);
      if (n>video.getFrameCount()-1) {
      	int extra = n-video.getFrameCount()+1;
      	videoTime = video.getFrameTime(video.getFrameCount()-1)+extra*frameDuration;
      }
      return (videoTime-video.getStartTime())*timeStretch;
  	}
    return stepNumber*frameDuration*clip.getStepSize();
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("stepsize")) {    								// from video clip //$NON-NLS-1$
      timer.setInitialDelay(getTimerDelay());
    } 
    else if(name.equals("framenumber")) {           // from video //$NON-NLS-1$
    	int n = ((Integer) e.getNewValue()).intValue();
      stepDisplayed = true;
      if(n!=videoFrameNumber) {
        super.setFrameNumber(n-clip.getFrameShift());
        support.firePropertyChange("stepnumber", null, new Integer(stepNumber)); //$NON-NLS-1$
      }
      if(playing) {
        step();
      }
    }
    else super.propertyChange(e);
  }

  /**
   * Gets the timer delay.
   *
   * @return the timer delay in milliseconds
   */
  private int getTimerDelay() {
  	double duration = frameDuration;
  	if (video!=null && video.getDuration()>0) {
      int count = video.getEndFrameNumber()-video.getStartFrameNumber();
      if(count!=0) {
        double ti = video.getFrameTime(video.getStartFrameNumber());
        double tf = video.getFrameTime(video.getEndFrameNumber());
        duration = (tf-ti)/count;
      }
      else duration = video.getDuration()/video.getFrameCount();
  	}
    int delay = (int) (duration*clip.getStepSize()/rate);
    delay = Math.min(delay, maxDelay);
    delay = Math.max(delay, minDelay);
    return delay;
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
