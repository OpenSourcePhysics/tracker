/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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
 * <http://physlets.org/tracker/>.
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
	String trackUndoXML;
	HashSet<String[]> undoStepStates = new HashSet<String[]>(); // stepState is {track name, frame number, xml step state}
	HashSet<Step> removedSteps = new HashSet<Step>(); // steps removed after being changed (undo steps states retained)
	boolean saveUndoStates = false;
	boolean isModified = false;
	HashSet<TTrack> tracks = new HashSet<TTrack>();
	
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
   * @return true if added
   */
	@Override
  public boolean add(Step step) {
		if (!(step instanceof PositionStep)) return false;
    boolean added = super.add(step);
    isModified = added;

    if (added && !removedSteps.contains(step)) {
	    // add initial step state to stepStates
    	String xml = new XMLControlElement(step).toXML();
    	String[] data = {step.getTrack().getName(), String.valueOf(step.getFrameNumber()), xml};
    	String[] match = null;
    	for (String[] next: undoStepStates) {
    		if (next[0].equals(data[0]) && next[1].equals(data[1])) {
    			match = next;
    			break;
    		}
    	}
    	if (match==null) {
	    	undoStepStates.add(data);
    	}
    	if (trackUndoXML==null) {
   			TTrack track = step.getTrack();
  			trackUndoXML = new XMLControlElement(track).toXML();
  		}
    }
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
    if (removed) {
    	isModified = true;
    	PositionStep stepp = (PositionStep)step;
    	if (!isChanged()) {
		    // remove step state from stepStates
	    	String frameNum = String.valueOf(stepp.getFrameNumber());
	    	String[] match = null;
	    	for (String[] next: undoStepStates) {
	    		if (next[0].equals(stepp.getTrack().getName()) && next[1].equals(frameNum)) {
	    			match = next;
	    			break;
	    		}
	    	}
	    	if (match!=null) {
	    		undoStepStates.remove(match);
	    	}
    	}
    	else {
    		// stepp may have been changed so add to removedSteps
    		removedSteps.add(stepp);
    	}
    	if (stepp.getPoints()[0]==trackerPanel.getSelectedPoint()) {
    		trackerPanel.setSelectedPoint(null);    		
        trackerPanel.selectedSteps.clear();
    	}
    	if (this.isEmpty()) {
    		clear();
    	}
    }
    return removed;
  }
	
  /**
   * Clears this set.
   */
	@Override
  public void clear() {
		if (changed && (!this.isEmpty() || !removedSteps.isEmpty())) {
  		TTrack[] tracks = getTracks();
  		if (tracks.length==1 && getTrackUndoControl()!=null) {
    		Undo.postTrackEdit(tracks[0], getTrackUndoControl());    		        			
  		}
  		else {
    		Undo.postStepSetEdit(this, getStepsUndoControl());    		        			
  		}			
		}
		// erase all steps
		for (Step next: this) {
			next.erase();
		}
		super.clear();
		undoStepStates.clear();
		removedSteps.clear();
		trackUndoXML = null;
		changed = false;
		isModified = false;
  }
	
  /**
   * Sets the changed property. When true, the steps in this set have been changed.
   *
   * @param changed true if changed
   */
	public void setChanged(boolean changed) {
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
	public XMLControl getStepsUndoControl() {
		saveUndoStates = true;
		XMLControl control = new XMLControlElement(this);
		saveUndoStates = false;
		return control;
	}
	
  /**
   * Gets the XMLControl that defines the Undo state for the track.
   *
   * @return the track XMLControl
   */
	public XMLControl getTrackUndoControl() {
		return trackUndoXML==null? null: new XMLControlElement(trackUndoXML);
	}
	
  /**
   * Returns all tracks associated with the steps.
   *
   * @return a track array
   */
	public TTrack[] getTracks() {
		tracks.clear();
  	for (Step step: this) {
  		if (step.getTrack()!=null) {
  			tracks.add(step.getTrack());
  		}
  	}
  	return tracks.toArray(new TTrack[tracks.size()]);
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
      String[][] stepsData;
      if (steps.saveUndoStates) {
      	stepsData = steps.undoStepStates.toArray(new String[steps.undoStepStates.size()][]);
      }
      else {
      	// save current states of all steps and removed steps
      	stepsData = new String[steps.size() + steps.removedSteps.size()][];
	      int i = 0;
	      for (Step step: steps) {
	      	String xml = new XMLControlElement(step).toXML();
	      	String[] data = {step.getTrack().getName(), String.valueOf(step.getFrameNumber()), xml};
	      	stepsData[i++] = data;
	      }
	      for (Step step: steps.removedSteps) {
	      	String xml = new XMLControlElement(step).toXML();
	      	String[] data = {step.getTrack().getName(), String.valueOf(step.getFrameNumber()), xml};
	      	stepsData[i++] = data;
	      }
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
