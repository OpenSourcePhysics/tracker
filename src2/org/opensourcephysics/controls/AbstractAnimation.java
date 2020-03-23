/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.Collection;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import org.opensourcephysics.display.OSPFrame;

/**
 * AbstractAnimation is a template for simple animations.
 *
 * Implement the doStep method to create an animation.  This method is called from the run method and when
 * the stepAnimation button is pressed.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractAnimation implements Animation, Runnable {
  protected OSPFrame mainFrame;                                        // the main frame that closed the program
  protected Control control;                                           // the model's control
  protected volatile Thread animationThread;
  protected int delayTime = 100;                                       // time between animation steps in milliseconds
  protected Timer      swingTimer;                                     // replaces thread in JavaScript simulations
  long t0 = System.currentTimeMillis();                                // system clock at start of last time step

  /** Field decimalFormat can be used to display time and other numeric values. */
  protected DecimalFormat decimalFormat = new DecimalFormat("0.00E0"); // default numeric format for messages //$NON-NLS-1$

  /**
   * Sets the Control for this model and initializes the control's values.
   *
   * @param control
   */
  public void setControl(Control control) {
    this.control = control;
    mainFrame = null;
    if(control!=null) {
      if(control instanceof MainFrame) {
        mainFrame = ((MainFrame) control).getMainFrame();
      }
      control.setLockValues(true);
      resetAnimation(); // sets the control's default values
      control.setLockValues(false);
      if(control instanceof Frame) {
        ((Frame) control).pack();
      }
    }
  }

  /**
   * Sets the preferred delay time in ms between animation steps.
   * @param delay
   */
  public void setDelayTime(int delay) {
    delayTime = delay;
  }

  /**
   * Gets the preferred delay time in ms between animation steps.
   * @return
   */
  public int getDelayTime() {
    return delayTime;
  }

  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPFrame getMainFrame() {
    return mainFrame;
  }

  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPApplication getOSPApp() {
    if(control instanceof MainFrame) {
      return((MainFrame) control).getOSPApp();
    }
    return null;
  }

  /**
   * Adds a child frame that depends on the main frame.
   * Child frames are closed when this frame is closed.
   *
   * @param frame JFrame
   */
  public void addChildFrame(JFrame frame) {
    if((mainFrame==null)||(frame==null)) {
      return;
    }
    mainFrame.addChildFrame(frame);
  }

  /**
   * Clears the child frames from the main frame.
   */
  public void clearChildFrames() {
    if(mainFrame==null) {
      return;
    }
    mainFrame.clearChildFrames();
  }

  /**
   * Gets a copy of the ChildFrames collection.
   * @return Collection
   */
  public Collection<JFrame> getChildFrames() {
    return mainFrame.getChildFrames();
  }

  /**
   * Gets the Control.
   *
   * @return the control
   */
  public Control getControl() {
    return control;
  }

  /**
   * Initializes the animation by reading parameters from the control.
   */
  public void initializeAnimation() {
    control.clearMessages();
  }

  /**
   * Does an animation step.
   */
  abstract protected void doStep();

  /**
   * Stops the animation.
   *
   * Sets animationThread to null and waits for a join with the animation thread.
   */
  public synchronized void stopAnimation() {
    if(animationThread==null) { // animation thread is already dead
     if(org.opensourcephysics.js.JSUtil.isJS && swingTimer!=null) swingTimer.stop();
      return;
    }
    Thread tempThread = animationThread; // local reference
    animationThread = null; // signal the animation to stop
    if(org.opensourcephysics.js.JSUtil.isJS) {
    	if(swingTimer!=null) swingTimer.stop();
    	return;
    }
	
    if(Thread.currentThread()==tempThread) {
      return; // cannot join with own thread so return
    }         // another thread has called this method in order to stop the animation thread
    try {                     // guard against an exception in applet mode
      tempThread.interrupt(); // get out of a sleep state
      tempThread.join(1000);  // wait up to 1 second for animation thread to stop
    } catch(Exception e) {
      // System.out.println("excetpion in stop animation"+e);
    }
  }

  /**
   * Determines if the animation is running.
   *
   * @return boolean
   */
  public final boolean isRunning() {
    return animationThread!=null;
  }

  /**
   * Steps the animation.
   */
  public synchronized void stepAnimation() {
    if(animationThread!=null) {
      stopAnimation();
    }
    doStep();
  }
  
	/**
	 * Create the timer and perform one time step
	 */
	protected void createSwingTimer() {
		long t1=System.currentTimeMillis();
		int myDelay=(int)(t1 -t0);         // optimal delay based on last execution time 
		myDelay=Math.min(delayTime, myDelay);  // do not wait longer than requested delay
		myDelay=Math.max(5,myDelay);      // but wait a minimum of 5 ms.
		t0=t1;                             //save current time
		//System.out.println("my delay ="+myDelay);  // debugging code
		swingTimer = new Timer(myDelay, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				swingTimer = null;
				doStep();
				if( animationThread!=null) {
					createSwingTimer();  // start another step if simulation is running
					swingTimer.start();
				}
			}

		});
		swingTimer.setRepeats(false);
	}

  /**
   * Starts the animation.
   *
   * Use this method to start a timer or a thread.
   */
  public synchronized void startAnimation() {
    if(animationThread!=null) {
      return; // animation is running
    }
    animationThread = new Thread(this);
    if (org.opensourcephysics.js.JSUtil.isJS) {
        createSwingTimer();
        swingTimer.start();
        return;
    }
    
    animationThread.setPriority(Thread.NORM_PRIORITY);
    //animationThread.setPriority(Thread.MAX_PRIORITY);   // for testing
    //animationThread.setPriority(Thread.MIN_PRIORITY);   // for testing
    animationThread.setDaemon(true);
    animationThread.start(); // start the animation
  }

  /**
   * Resets the animation to a predefined state.
   */
  public void resetAnimation() {
    if(animationThread!=null) {
      stopAnimation(); // make sure animation is stopped
    }
    control.clearMessages();
  }

  /**
   * Implementation of Runnable interface.  DO NOT access this method directly.
   */
  public void run() {
	 if(org.opensourcephysics.js.JSUtil.isJS) {
		 System.err.println("JavaScript error.  Thread run method called in Abstract Animation.");
	    return;
	}
    long sleepTime = delayTime;
    while(animationThread==Thread.currentThread()) {
      long currentTime = System.currentTimeMillis();
      doStep();
      // adjust the sleep time to try and achieve a constant animation rate
      // some VMs will hang if sleep time is less than 10
      sleepTime = Math.max(10, delayTime-(System.currentTimeMillis()-currentTime));
      try {
        Thread.sleep(sleepTime);
      } catch(InterruptedException ie) {}
    }
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new OSPAnimationLoader();
  }

  /**
   * Default XMLLoader to save and load data for Simulations.
   */
  static class OSPAnimationLoader extends XMLLoader {
    /**
     * Performs the calculate method when a Calculation is loaded.
     *
     * @param control the control
     * @param obj the object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ((Animation) obj).initializeAnimation();
      return obj;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
