/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.HashSet;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;

/**
 * A StepSet is a HashSet of Steps that can be saved in an XMLControl.
 *
 * @author Douglas Brown
 */
public class StepSet extends HashSet<Step> {
	
	TrackerPanel trackerPanel;
	boolean changed;
	XMLControl undoControl;
	
  /**
   * Constructs a StepSet.
   *
   * @param panel the TrackerPanel that draws the Steps
   */
	public StepSet(TrackerPanel panel) {
		trackerPanel = panel;
	}
	
  /**
   * Adds a step to this set.
   *
   * @param step the step to add
   */
	@Override
  public boolean add(Step step) {
		if(!(step instanceof PositionStep)) return false;
    boolean added = super.add(step);
    if (added) setChanged(false); // triggers creation of new undo XMLControl
    return added;
  }
	
  /**
   * Removes a step from this set.
   *
   * @param step the step to remove
   */
	@Override
	public boolean remove(Object step) {
		if(!(step instanceof PositionStep)) return false;
    boolean removed = super.remove(step);
    if (removed) setChanged(false); // triggers creation of new undo XMLControl
    return removed;
  }
	
  /**
   * Sets the changed property. When true, the steps in this set have been changed.
   * The first time this is called on an "unchanged" set a new XMLControl is created
   * that defines the Undo state for the set prior to changing.
   *
   * @param changed true if changed
   */
	public void setChanged(boolean changed) {
		// when changed for the first time, save state
		if (changed && !this.changed) {
			undoControl = new XMLControlElement(this);
		}
		this.changed = changed;
	}
	
  /**
   * Gets the changed property. When true, the steps in this set have been changed.
   *
   * @return true if changed
   */
	public boolean isChanged() {
		return changed;
	}
	
  /**
   * Gets the XMLControl that defines the Undo state for this set.
   *
   * @return the undo XMLControl
   */
	public XMLControl getUndoControl() {
		return undoControl;
	}
	
	
  /**
   * Returns an ObjectLoader to save and load data for this class.
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
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      StepSet steps = (StepSet) obj;
      // assemble array of String[3]: each element is {track name, frame number, xml step state}
      String[][] stepsData = new String[steps.size()][3];
      int i = 0;
      for (Step step: steps) {
      	String xml = new XMLControlElement(step).toXML();
      	String[] data = {step.getTrack().getName(), String.valueOf(step.getFrameNumber()), xml};
      	stepsData[i++] = data;
      }
      control.setValue("steps", stepsData); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	// this loader is not intended to be used to create new steps,
    	// but only for undo/redo stepset edits.
      return null;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      StepSet steps = (StepSet) obj;
      String[][] stepsData = (String[][])control.getObject("steps"); //$NON-NLS-1$
      TrackerPanel panel = steps.trackerPanel;
      for (String[] next: stepsData) {
      	TTrack track = panel.getTrack(next[0]);
      	if (track!=null) {
      		int n = Integer.parseInt(next[1]);
      		Step step = track.getStep(n);
      		if (step!=null) {
      			String xml = next[2];
            if(xml.indexOf(XML.CDATA_PRE)!=-1) {
            	xml = xml.substring(xml.indexOf(XML.CDATA_PRE)+XML.CDATA_PRE.length(), xml.indexOf(XML.CDATA_POST));
            }
      			XMLControl stepControl = new XMLControlElement(xml);
      			stepControl.loadObject(step);
      			step.erase();
      		}
      	}
      }
    	return obj;
    }
  }

}
