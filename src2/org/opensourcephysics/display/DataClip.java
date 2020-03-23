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
package org.opensourcephysics.display;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This defines a subset of data elements called steps. The steps are the elements
 * with array index that meets the conditions:
 *   1. index >= startIndex
 *   2. index <= startIndex + (clipLength-1)*stride 
 *   3. (index-startIndex) % stride = 0.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataClip {
  private int dataLength = 2;  
	private int startIndex = 0;
	private int clipLength = 0;
  private int stride = 1;
  private PropertyChangeSupport support;
  private boolean isAdjusting = false;
  

  /**
   * Constructs a DataClip.
   *
   * @param dataLength the number of data elements in the Data object
   */
  public DataClip() {
    support = new SwingPropertyChangeSupport(this);
  }
  
  /**
   * Sets the data length (number of data elements in the Data object).
   *
   * @param length the data length   
   */
  public void setDataLength(int length) {
  	length = Math.max(length, 1);
  	dataLength = length;
  }

  /**
   * Gets the data length (number of data elements in the Data object).
   *
   * @return the data length   
   */
  public int getDataLength() {
  	return dataLength;
  }

  /**
   * Sets the clip length (number of video frames on which the data
   * is displayed). If the clip length is set to zero or less then
   * it is reported as equal to the entire data length.
   *
   * @param clipLength the desired clip length   
   * @return the resulting clip length
   */
  public int setClipLength(int length) {
  	int prev = getClipLength();
  	// check limits
  	length = Math.min(length, dataLength);
  	// if full length, set to zero
  	if (length==dataLength) {
  		length = 0;
  	}
  	clipLength = length;
    support.firePropertyChange("clip_length", prev, getClipLength()); //$NON-NLS-1$
    return getClipLength();
  }

  /**
   * Gets the clip length (number of video frames on which the data
   * is displayed unless limited by the video clip or stride).
   *
   * @return the clip length
   */
  public int getClipLength() {
  	if (clipLength<=0) return dataLength;
  	return clipLength;
  }

  /**
   * Sets the start index.
   *
   * @param start the desired start index
   * @return the resulting start index
   */
  public int setStartIndex(int start) {
  	int prev = getStartIndex();
    // check limits
  	start = Math.max(start, 0);
    start = Math.min(start, dataLength-1);
    
    startIndex = start;
    support.firePropertyChange("start_index", prev, start); //$NON-NLS-1$
    return getStartIndex();
  }

  /**
   * Gets the start index.
   *
   * @return the start index
   */
  public int getStartIndex() {
    return startIndex;
  }

  /**
   * Sets the stride (number of data elements per step).
   *
   * @param stride the desired stride
   * @return the resulting stride
   */
  public int setStride(int stride) {
  	int prev = getStride();
  	// check limits
    stride = Math.min(stride, dataLength-1);
    stride = Math.max(stride, 1);
    
    this.stride = stride;
    support.firePropertyChange("stride", prev, stride); //$NON-NLS-1$
    return getStride();
  }

  /**
   * Gets the stride (number of data elements per step).
   *
   * @return the stride
   */
  public int getStride() {
    return stride;
  }
  
  /**
   * Gets the available clip length (step count). A step is available if it's index
   * is less than the data length. Available clip length <= clip length
   *
   * @return the index
   */
  public int getAvailableClipLength() {
  	int stepCount = getClipLength();
    for (int i = stepCount-1; i>0; i--) {
    	// look for first step with index less than data length
      if (stepToIndex(i)<dataLength) {
        return i+1;
      }
    }
    return 1;
  }

  /**
   * Converts step number to data index.
   *
   * @param stepNumber the step number
   * @return the data index
   */
  public int stepToIndex(int stepNumber) {
    return startIndex+stepNumber*stride;
  }

  /**
   * Converts data index to step number. An index that falls
   * between two steps maps to the previous step.
   *
   * @param index the data index
   * @return the step number
   */
  public int indexToStep(int index) {
    return (index-startIndex)/stride;
  }

  /**
   * Determines whether the specified index is a step index.
   *
   * @param n the index number
   * @return <code>true</code> if the index is a step index
   */
  public boolean includesIndex(int n) {
  	int stepCount = getClipLength();
    for(int i = 0; i<stepCount; i++) {
      if(stepToIndex(i)==n) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the adjusting flag.
   *
   * @param adjusting true if adjusting
   */
  public void setAdjusting(boolean adjusting) {
  	if (isAdjusting==adjusting)
  		return;
  	isAdjusting = adjusting;
		support.firePropertyChange("adjusting", null, adjusting); //$NON-NLS-1$
  }

  /**
   * Gets the adjusting flag.
   *
   * @return true if adjusting
   */
  public boolean isAdjusting() {
  	return isAdjusting;
  }

  /**
   * Adds a PropertyChangeListener to this video clip.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to this video clip.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener from this video clip.
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
   * Returns an XML.ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
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
      DataClip clip = (DataClip) obj;
      control.setValue("start", clip.getStartIndex()); //$NON-NLS-1$
      control.setValue("clip_length", clip.getClipLength()); //$NON-NLS-1$
      control.setValue("stride", clip.getStride()); //$NON-NLS-1$
      control.setValue("data_length", clip.getDataLength()); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new DataClip();
    }

    /**
     * Loads a VideoClip with data from an XMLControl.
     *
     * @param control the XMLControl
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	DataClip clip = (DataClip) obj;
    	clip.setDataLength(control.getInt("data_length")); //$NON-NLS-1$
    	clip.setStartIndex(control.getInt("start")); //$NON-NLS-1$
    	clip.setClipLength(control.getInt("clip_length")); //$NON-NLS-1$
    	clip.setStride(control.getInt("stride")); //$NON-NLS-1$
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
