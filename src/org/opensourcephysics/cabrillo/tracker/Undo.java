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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import javax.swing.JDialog;
import javax.swing.undo.*;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.media.core.*;

/**
 * A class to handle undo/redo operations for Tracker.
 *
 * @author Douglas Brown
 */
public class Undo {

	// static fields
  protected static Map<TrackerPanel, Undo> undomap = new HashMap<TrackerPanel, Undo>();
	
	// instance fields
  protected UndoableEditSupport undoSupport;
  protected MyUndoManager undoManager;

  /**
   * Private constructor.
   */
  private Undo() {
    // set up the undo system
    undoManager = new MyUndoManager();
//    undoManager.setLimit(20);
    undoSupport = new UndoableEditSupport();
    undoSupport.addUndoableEditListener(undoManager);
    XML.setLoader(TrackProperties.class, TrackProperties.getLoader());
  }

  /**
   * Reports whether an undoable edit is available for the specified panel.
   * 
   * @param panel the TrackerPanel
   * @return true if an undoable edit is available
   */
  public static boolean canUndo(TrackerPanel panel) {
  	return getUndo(panel).undoManager.canUndo();
  }

  /**
   * Gets the undoable edit description for the specified panel.
   * 
   * @param panel the TrackerPanel
   * @return the undoable edit description
   */
  public static String getUndoDescription(TrackerPanel panel) {
  	return getUndo(panel).undoManager.getUndoPresentationName();
  }

  /**
   * Undoes the most recently posted edit for the specified panel.
   * 
   * @param panel the TrackerPanel
   */
  public static void undo(TrackerPanel panel) {
  	// check last edit and (if trackEdit) modify its redo state with current state of track
  	if (!getUndo(panel).undoManager.canRedo()) {
	  	UndoableEdit lastEdit = getUndo(panel).undoManager.getLastEdit();
  		if (lastEdit!=null && lastEdit instanceof TrackEdit) {
  			TrackEdit trackEdit = (TrackEdit)lastEdit;
  			String name = trackEdit.trackName;
  			TTrack track = panel.getTrack(name);
  			if (track!=null) {
  				trackEdit.redo = new XMLControlElement(track).toXML();
  			}
  		}
  	}
  	getUndo(panel).undoManager.undo();
  	refreshMenus(panel);
  	panel.repaint();
  }

  /**
   * Reports whether an undoable edit is available for the specified panel.
   * 
   * @param panel the TrackerPanel
   * @return true if an undoable edit is available
   */
  public static boolean canRedo(TrackerPanel panel) {
  	return getUndo(panel).undoManager.canRedo();
  }

  /**
   * Gets the redoable edit description for the specified panel.
   * 
   * @param panel the TrackerPanel
   * @return the redoable edit description
   */
  public static String getRedoDescription(TrackerPanel panel) {
  	return getUndo(panel).undoManager.getRedoPresentationName();
  }

  /**
   * Redoes the most recently undone edit for the specified panel.
   * 
   * @param panel the TrackerPanel
   */
  public static void redo(TrackerPanel panel) {
  	getUndo(panel).undoManager.redo();
  	refreshMenus(panel);
  	panel.repaint();
  }
  
//_____________________ private and protected methods ______________________
  
  /**
   * Posts an undoable edit for a deleted track.
   * 
   * @param track the track
   */
  protected static void postTrackDelete(TTrack track) {
  	TrackerPanel panel = track.trackerPanel;
  	if (panel == null) return;
  	UndoableEdit edit = getUndo(panel).new TrackDelete(panel, track); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a TrackerPanel cleared of tracks.
   * 
   * @param panel the TrackerPanel that has been cleared
   * @param xml a list of XML strings describing the cleared tracks
   */
  protected static void postTrackClear(TrackerPanel panel, List<String> xml) {
  	UndoableEdit edit = getUndo(panel).new TrackClear(panel, xml); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a changed track.
   * 
   * @param track the changed track
   * @param control an XMLControl with the previous state of the track
   */
  protected static void postTrackEdit(TTrack track, XMLControl control) {
  	TrackerPanel panel = track.trackerPanel;
  	if (panel == null) return;
  	UndoableEdit edit = getUndo(panel).new TrackEdit(track, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for multiple changed tracks.
   * 
   * @param tracksAndXMLControls list of array elements, each element = {track, track's previous state}
   */
  protected static void postMultiTrackEdit(ArrayList<Object[]> tracksAndXMLControls) {
  	if (tracksAndXMLControls==null || tracksAndXMLControls.size()==0) return;
		TTrack track = (TTrack)tracksAndXMLControls.get(0)[0];
   	TrackerPanel panel = track.trackerPanel;
  	if (panel == null) return;
  	
  	UndoableEdit edit = null;
  	for (Object[] next: tracksAndXMLControls) {
  		track = (TTrack)next[0];
	  	XMLControl control = (XMLControl)next[1];
  		if (edit==null) { // create track edit for first track
  			edit = getUndo(panel).new TrackEdit(track, control);   			
  		}
  		else { // create compound edit for subsequent tracks
  	  	UndoableEdit trackEdit = getUndo(panel).new TrackEdit(track, control); 
  			edit = getUndo(panel).new CompoundEdit(trackEdit, edit);
  		}
  	}
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a changed step.
   * 
   * @param step the changed step
   * @param control an XMLControl with the previous state of the track
   */
  protected static void postStepEdit(Step step, XMLControl control) {
  	TrackerPanel panel = step.getTrack().trackerPanel;
  	if (panel == null) return;
  	UndoableEdit edit = getUndo(panel).new StepEdit(step, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a changed StepSet.
   * 
   * @param steps the changed StepSet
   * @param control an XMLControl with the previous state of the StepSet
   */
  protected static void postStepSetEdit(StepSet steps, XMLControl control) {
  	TrackerPanel panel = steps.trackerPanel;
  	TTrack track = null;
  	boolean singleTrack = true;
  	for (Step step: steps) {
  		if (step.getTrack()!=null) {
  			if (track==null) track = step.getTrack();
  			else { // track not null, so compare
  				if (track!=step.getTrack()) {
  					singleTrack = false;
  					break;
  				}
  			}
  		}
  	}
  	UndoableEdit edit;
  	if (track!=null && singleTrack) {
    	edit = getUndo(panel).new TrackEdit(track, control); 
  	}
  	else {
  		edit = getUndo(panel).new StepSetEdit(steps, control); 
  	}
  	getUndo(panel).undoSupport.postEdit(edit);
  	steps.setChanged(false); // prevents clear() method from saving another undoable edit
  	steps.clear();
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a changed coordinate system.
   * 
   * @param panel the TrackerPanel with the changed coords
   * @param control an XMLControl with the previous state of the coords
   */
  protected static void postCoordsEdit(TrackerPanel panel, XMLControl control) {
  	UndoableEdit edit = getUndo(panel).new CoordsEdit(panel, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts a compound undoable edit for tracks that control the coords.
   * 
   * @param track the changed track
   * @param trackControl an XMLControl with the previous state of the track
   * @param coordsControl an XMLControl with the previous state of the coords
   */
  protected static void postTrackAndCoordsEdit(TTrack track, XMLControl trackControl, XMLControl coordsControl) {
  	TrackerPanel panel = track.trackerPanel;
  	if (panel == null) return;
  	// coords edit first!
  	UndoableEdit edit1 = getUndo(panel).new CoordsEdit(panel, coordsControl); 
  	UndoableEdit edit2 = getUndo(panel).new TrackEdit(track, trackControl); 
  	UndoableEdit compound = getUndo(panel).new CompoundEdit(edit1, edit2); 
  	getUndo(panel).undoSupport.postEdit(compound);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for an edited image video.
   * 
   * @param panel the TrackerPanel with the new video clip
   * @param paths paths to the video files
   * @param index step number at which edit occured
   * @param step step number after edit occured
   * @param added true if a frame was added
   */
  protected static void postImageVideoEdit(TrackerPanel panel, 
  				String[] paths, int index, int step, boolean added) {
  	UndoableEdit edit = getUndo(panel).new ImageVideoEdit(
  					panel, paths, index, step, added); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a replaced video clip.
   * 
   * @param panel the TrackerPanel with the new video clip
   * @param control an XMLControl describing the previous video clip
   */
  protected static void postVideoReplace(TrackerPanel panel, XMLControl control) {
  	UndoableEdit edit = getUndo(panel).new VideoReplace(panel, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a video filter deletion.
   * 
   * @param panel the TrackerPanel with the new video clip
   * @param filter the deleted filter
   */
  protected static void postFilterDelete(TrackerPanel panel, Filter filter) {
  	UndoableEdit edit = getUndo(panel).new FilterDelete(panel, filter); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a change to a video filter.
   * 
   * @param panel the TrackerPanel with the new video clip
   * @param filter the  filter
   * @param control an XMLControl with the previous state of the filter
   */
  protected static void postFilterEdit(TrackerPanel panel, Filter filter, XMLControl control) {
  	UndoableEdit edit = getUndo(panel).new FilterEdit(panel, filter, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a TrackerPanel cleared of video filters.
   * 
   * @param panel the TrackerPanel that has been cleared
   * @param xml a list of XML strings describing the cleared tracks
   */
  protected static void postFilterClear(TrackerPanel panel, List<String> xml) {
  	UndoableEdit edit = getUndo(panel).new FilterClear(panel, xml); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  /**
   * Posts an undoable edit for a footprint, name or color change.
   * 
   * @param track the track with the changed property
   * @param control an XMLControl with the previous state of the footprint
   */
  protected static void postTrackDisplayEdit(TTrack track, XMLControl control) {
  	TrackerPanel panel = track.trackerPanel;
  	if (panel == null) return;
  	UndoableEdit edit = getUndo(panel).new TrackDisplayEdit(track, control); 
  	getUndo(panel).undoSupport.postEdit(edit);
  	refreshMenus(panel);
  }

  private static Undo getUndo(TrackerPanel panel) {
  	Undo undo = undomap.get(panel);
  	if (undo == null) {
  		undo = new Undo();
  		undomap.put(panel, undo);
  	}
  	return undo;
  }

  private static void refreshMenus(TrackerPanel panel) {
  	TMenuBar menubar = TMenuBar.getMenuBar(panel);
    if (menubar != null) menubar.refresh();
  }

//______________________ inner UndoableEdit classes ______________________
  
  /**
   * A class to undo/redo track changes.
   */
  protected class TrackEdit extends TEdit {
  	
  	String trackName, trackType;

  	private TrackEdit(TTrack track, XMLControl control) {
  		super(track.trackerPanel, track, control);
  		trackName = track.getName();
    	String s = track.getClass().getSimpleName();
    	trackType = TrackerRes.getString(s+".Name"); //$NON-NLS-1$
    	if (trackType.startsWith("!")) { //$NON-NLS-1$
    		trackType = s;
    	}
    }

  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
   	  TTrack track = panel.getTrack(trackName);
  	  control.loadObject(track);
  	  track.erase();
  	  track.firePropertyChange("steps", null, null); //$NON-NLS-1$
  	  // TrackEdit is also used for text column edits
  	  track.firePropertyChange("text_column", null, null); //$NON-NLS-1$
    }
    
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+trackType; 
    }

  }

  /**
   * A class to undo/redo step changes.
   */
  protected class StepEdit extends TEdit {

  	Step step;
  	String trackType;
  	
  	private StepEdit(Step step, XMLControl control) {
  		super(step.getTrack().trackerPanel, step, control);
  		this.step = step;
  		String s = step.getTrack().getClass().getSimpleName();
    	trackType = TrackerRes.getString(s+".Name"); //$NON-NLS-1$
    	if (trackType.startsWith("!")) { //$NON-NLS-1$
    		trackType = s;
    	}   
    }

  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
  	  control.loadObject(step);
  	  step.erase();
  	  TTrackBar.getTrackbar(panel).refresh();
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+trackType;
    }

  }

  /**
   * A class to undo/redo footprint, color and name changes.
   */
  protected class TrackDisplayEdit extends TEdit {

  	String undoName, redoName;
  	String trackName, trackType;
  	
  	private TrackDisplayEdit(TTrack track, XMLControl control) {
  		super(track.trackerPanel, new TrackProperties(track), control);
  	  control = new XMLControlElement(undo);
  	  TrackProperties props = (TrackProperties)control.loadObject(null);
  		undoName = track.getName();
  		redoName = props.name;
  		String s = track.getClass().getSimpleName();
    	trackType = TrackerRes.getString(s+".Name"); //$NON-NLS-1$
    	if (trackType.startsWith("!")) { //$NON-NLS-1$
    		trackType = s;
    	}   
    }

    public void undo() throws CannotUndoException {
    	trackName = undoName;
    	super.undo();
    }

    public void redo() throws CannotUndoException {
    	trackName = redoName;
    	super.redo();
    }
    
  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
  	  TrackProperties props = (TrackProperties)control.loadObject(null);
   	  TTrack track = panel.getTrack(trackName);
  	  track.setName(props.name);
  	  if (props.colors!=null) {
  	  	if (props.colors.length==1) {
	    	  track.setColor(props.colors[0]);
	  	  }
  	  	else if (track instanceof ParticleDataTrack) {
  	  		((ParticleDataTrack)track).setAllColors(props.colors);
  	  	}
  	  }
  	  if (props.footprints!=null) {
  	  	if (props.footprints.length==1) {
  	  	  track.setFootprint(props.footprints[0]);
	  	  }
  	  	else if (track instanceof ParticleDataTrack) {
  	  		((ParticleDataTrack)track).setAllFootprints(props.footprints);
  	  	}
  	  }
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+trackType;
    }

  }

  /**
   * A class to undo/redo stepset changes.
   */
  protected class StepSetEdit extends TEdit {
  	
  	private StepSetEdit(StepSet steps, XMLControl control) {
  		super(steps.trackerPanel, steps, control);
    }

  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
   	  StepSet steps = new StepSet(panel);
  	  control.loadObject(steps);
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("Undo.Description.Steps"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo coords changes.
   */
  protected class CoordsEdit extends TEdit {
  	
  	private CoordsEdit(TrackerPanel panel, XMLControl control) {
  		super(panel, panel.getCoords(), control);
    }

  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
   	  ImageCoordSystem coords = panel.getCoords();
  	  control.loadObject(coords);
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("TMenuBar.Menu.Coords"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo image video edits.
   */
  protected class ImageVideoEdit extends AbstractUndoableEdit {
  	
  	String[] paths; // image path
  	TrackerPanel panel;
  	int n; // add/remove index
  	int step; // post-removal step number
  	boolean added; // true if original edit was an addImage
  	
  	private ImageVideoEdit(TrackerPanel panel, 
    				String[] imagePaths, int index, int step, boolean added) {
  		this.panel = panel;
  		paths = imagePaths;
  		n = index;
  		this.step = step;
  		this.added = added;
    }

    public void undo() throws CannotUndoException {
    	super.undo();
    	if (added) removeImages();
    	else addImages();
    }

    public void redo() throws CannotUndoException {
    	super.redo();
    	if (added) addImages();
    	else removeImages();
    }
    
  	private void addImages() {
  		if (paths == null || paths.length==0) return;
			try {
				int index = n;
				for (int i = 0; i < paths.length; i++) {
					String path = paths[i];
					if (path == null) continue;
					ImageVideo imageVid = (ImageVideo)panel.getVideo();
					imageVid.insert(path, index, false);
					VideoClip clip = panel.getPlayer().getVideoClip();
					clip.setStepCount(imageVid.getFrameCount());
					int step = panel.getPlayer().getVideoClip().frameToStep(index++);
					panel.getPlayer().setStepNumber(step);
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
    }
  	
  	private void removeImages() {
  		if (panel.getVideo() instanceof ImageVideo) {
    		ImageVideo imageVid = (ImageVideo)panel.getVideo();
  			for (int i = 0; i < paths.length; i++) {
  				imageVid.remove(n);
  			}
      	int len = imageVid.getFrameCount();
  			VideoClip clip = panel.getPlayer().getVideoClip();
  			clip.setStepCount(len);
  			panel.getPlayer().setStepNumber(step);
  		}
    }
  	
    public String getPresentationName() {
    	if (added) {
	      return TrackerRes.getString("Undo.Description.Add")+" " //$NON-NLS-1$ //$NON-NLS-2$
	      		+TrackerRes.getString("Undo.Description.Images"); //$NON-NLS-1$
    	}
      return TrackerRes.getString("Undo.Description.Remove")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("Undo.Description.Images"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo video replacements.
   */
  protected class VideoReplace extends TEdit {
  	
  	private VideoReplace(TrackerPanel panel, XMLControl control) {
  		super(panel, panel.getPlayer().getVideoClip(), control);
    }

    public void undo() throws CannotUndoException {
    	// if ImageVideo, save invalid images
    	Video video = panel.getVideo();
    	if (video instanceof ImageVideo) {
    		((ImageVideo)video).saveInvalidImages();
    	}
    	// refresh redo state
    	redo = new XMLControlElement(panel.getPlayer().getVideoClip()).toXML();
    	super.undo();
    }

    public void redo() throws CannotUndoException {
    	// if ImageVideo, save invalid images
    	Video video = panel.getVideo();
    	if (video instanceof ImageVideo) {
    		((ImageVideo)video).saveInvalidImages();
    	}
    	// refresh undo state
    	undo = new XMLControlElement(panel.getPlayer().getVideoClip()).toXML();
    	super.redo();
    }
    
  	protected void load(String xml) {
  		// clear filters from old video
    	Video video = panel.getVideo();
    	if (video!=null) {
    		TActions.getAction("clearFilters", panel).actionPerformed(null); //$NON-NLS-1$
    	}
   	  XMLControl control = new XMLControlElement(xml);
   	  VideoClip clip = (VideoClip)control.loadObject(null);
      panel.getPlayer().setVideoClip(clip);
    	video = panel.getVideo();
      if (video!=null) {
      	for (Filter filter: video.getFilterStack().getFilters()) {
      		filter.setVideoPanel(panel);
        	if (filter.inspectorX != Integer.MIN_VALUE) {
        		filter.inspectorVisible = true;
        		if (panel.visibleFilters == null) {
        			panel.visibleFilters = new HashMap<Filter, Point>();
        		}
        		Point p = new Point(filter.inspectorX, filter.inspectorY);
        		panel.visibleFilters.put(filter, p);	
        	}
      	}
      }
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Replace")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("Undo.Description.Video"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo changes to objects associated with a TrackerPanel.
   * The constructor takes the TrackerPanel, the object AFTER being changed,
   * and an XMLControl storing the state of the object BEFORE the changes.
   */
  protected abstract class TEdit extends AbstractUndoableEdit {
  	
  	String undo; // xml string
  	String redo; // xml string
  	TrackerPanel panel;
  	
  	protected TEdit(TrackerPanel panel, Object obj, XMLControl control) {
    	this.panel = panel;
    	undo = control.toXML();
  	  control = new XMLControlElement(obj);
  	  redo = control.toXML();
    }

    public void undo() throws CannotUndoException {
    	super.undo();
    	load(undo);
    }

    public void redo() throws CannotUndoException {
    	super.redo();
    	load(redo);
    }
    
    abstract void load(String xml);
  }

  /**
   * A class to undo/redo a pair of UndoableEdits.
   */
  protected class CompoundEdit extends AbstractUndoableEdit {
  	
  	UndoableEdit editA;
  	UndoableEdit editB;
  	
  	protected CompoundEdit(UndoableEdit edit1, UndoableEdit edit2) {
  		editA = edit1;
  		editB = edit2;
    }

    public void undo() throws CannotUndoException {
    	super.undo();
    	editA.undo();
    	editB.undo();
    }

    public void redo() throws CannotUndoException {
    	super.redo();
    	editA.redo();
    	editB.redo();
    }
    
  	
    public String getPresentationName() {
      return editA.getPresentationName();
    }

  }

  /**
   * A class to undo/redo track deletion.
   */
  protected class TrackDelete extends AbstractUndoableEdit {
  	
  	String xml;
  	int trackID;
  	TrackerPanel panel;
  	String trackType;

    private TrackDelete(TrackerPanel panel, TTrack track) {
    	XMLControl control = new XMLControlElement(track);
    	xml = control.toXML();
    	this.panel = panel;
  		String s = track.getClass().getSimpleName();
    	trackType = TrackerRes.getString(s+".Name"); //$NON-NLS-1$
    	if (trackType.startsWith("!")) { //$NON-NLS-1$
    		trackType = s;
    	}   
    }

    public void undo() throws CannotUndoException {
    	super.undo();
    	XMLControl control = new XMLControlElement(xml);
    	TTrack track = (TTrack)control.loadObject(null);
      panel.addTrack(track);
      trackID = track.getID();
      panel.requestFocus();
    }

    public void redo() throws CannotUndoException {
    	super.redo();
    	TTrack track = TTrack.getTrack(trackID);
      panel.removeTrack(track);
    }
    
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Delete")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+trackType;
    }

  }

  /**
   * A class to undo/redo clearing tracks.
   */
  protected class TrackClear extends AbstractUndoableEdit {
  	
  	List<String> xml;
  	TrackerPanel panel;

    private TrackClear(TrackerPanel trackerPanel, List<String> xml) {
    	this.xml = xml;
    	panel = trackerPanel;
    }

    public void undo() throws CannotUndoException {
    	super.undo();
    	Iterator<String> it = xml.iterator();
    	while (it.hasNext()) {
      	XMLControl control = new XMLControlElement(it.next());
      	TTrack track = (TTrack)control.loadObject(null);
        panel.addTrack(track);    		
    	}
      panel.requestFocus();
    }

    public void redo() throws CannotUndoException {
    	super.redo();
      panel.clearTracks();
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Clear")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("Undo.Description.Tracks"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo filter deletion.
   */
  protected class FilterDelete extends AbstractUndoableEdit {
  	
  	String xml;
  	TrackerPanel panel;
  	int i;
  	Filter filter;
  	String filterName;

    private FilterDelete(TrackerPanel trackerPanel, Filter filter) {
    	xml = new XMLControlElement(filter).toXML();
    	panel = trackerPanel;
      i = panel.getVideo().getFilterStack().lastIndexRemoved();
      filterName = filter.getClass().getSimpleName();
      int j = filterName.indexOf("Filter"); //$NON-NLS-1$
      if (j>0 && j<filterName.length()-1) {
      	filterName = filterName.substring(0, j);
      }
      filterName = MediaRes.getString("VideoFilter."+filterName); //$NON-NLS-1$
    }

    public void undo() throws CannotUndoException {
    	super.undo();
      Video video = panel.getVideo();
      if (video != null) {
        XMLControl control = new XMLControlElement(xml);
        filter = (Filter)control.loadObject(null);
        filter.setVideoPanel(panel);
        video.getFilterStack().insertFilter(filter, i);    		
      	if (filter.inspectorX != Integer.MIN_VALUE) {
      		filter.inspectorVisible = true;
      		if (panel.visibleFilters == null) {
      			panel.visibleFilters = new HashMap<Filter, Point>();
      		}
      		Point p = new Point(filter.inspectorX, filter.inspectorY);
      		panel.visibleFilters.put(filter, p);	
      	}
//      	if (filter.inspectorX != Integer.MIN_VALUE) {
//          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//          TFrame frame = panel.getTFrame();
//      		JDialog inspector = filter.getInspector();
//    			int x = Math.max(filter.inspectorX + frame.getLocation().x, 0);
//    			x = Math.min(x, dim.width-inspector.getWidth());
//    			int y = Math.max(filter.inspectorY + frame.getLocation().y, 0);
//    			y = Math.min(y, dim.height-inspector.getHeight());
//        	inspector.setLocation(x, y);
//      		inspector.setVisible(true);
//      	}
      }
    }

    public void redo() throws CannotUndoException {
    	super.redo();
      Video video = panel.getVideo();
      if (video != null) {
        filter.setVideoPanel(null);
        TMenuBar menubar = TMenuBar.getMenuBar(panel);
        menubar.refreshing = true; // prevents posting another undoable edit
        video.getFilterStack().removeFilter(filter);
        menubar.refreshing = false;
        i = video.getFilterStack().lastIndexRemoved();
        filter = null; // eliminate all references to deleted filter
      }
    }
    
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Delete")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("Undo.Description.Filter"); //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo filter clearing.
   */
  protected class FilterClear extends AbstractUndoableEdit {
  	
  	List<String> xml;
  	TrackerPanel panel;

    private FilterClear(TrackerPanel trackerPanel, List<String> xml) {
    	this.xml = xml;
    	panel = trackerPanel;
    }

    public void undo() throws CannotUndoException {
    	super.undo();
      Video video = panel.getVideo();
      if (video != null) {
      	for (String next: xml) {
        	XMLControl control = new XMLControlElement(next);
        	Filter filter = (Filter)control.loadObject(null);
          filter.setVideoPanel(panel);
          video.getFilterStack().addFilter(filter);    		
        	if (filter.inspectorX != Integer.MIN_VALUE) {
        		filter.inspectorVisible = true;
        		if (panel.visibleFilters == null) {
        			panel.visibleFilters = new HashMap<Filter, Point>();
        		}
        		Point p = new Point(filter.inspectorX, filter.inspectorY);
        		panel.visibleFilters.put(filter, p);	
        	}
      	}
      }
    }

    public void redo() throws CannotUndoException {
    	super.redo();
      Video video = panel.getVideo();
      if (video != null) {
      	FilterStack stack = video.getFilterStack();
      	for (Filter filter: stack.getFilters()) {
        	PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
      		if (track!=null) {
      			panel.removeTrack(track);
      			track.dispose();
      		}
      	}
        stack.clear();
      }
    }
    
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Clear")+" " //$NON-NLS-1$ //$NON-NLS-2$
      		+TrackerRes.getString("TMenuBar.MenuItem.VideoFilters"); //$NON-NLS-1$
    }

  }
  
  /**
   * A class to undo/redo filter edit.
   */
  protected class FilterEdit extends TEdit {
  	
  	int filterIndex;
  	int frameNumber;
  	String filterType;
  	
    private FilterEdit(TrackerPanel panel, Filter filter, XMLControl control) {
    	super(panel, filter, control);
    	filterIndex = panel.getVideo().getFilterStack().getFilters().indexOf(filter);
    	frameNumber = panel.getFrameNumber();
      filterType = filter.getClass().getSimpleName();
      int j = filterType.indexOf("Filter"); //$NON-NLS-1$
      if (j>0 && j<filterType.length()-1) {
      	filterType = filterType.substring(0, j);
      }
      filterType = MediaRes.getString("VideoFilter."+filterType); //$NON-NLS-1$
    }

  	protected void load(String xml) {
   	  XMLControl control = new XMLControlElement(xml);
      Video video = panel.getVideo();
      if (video != null) {      	
        ArrayList<Filter> filters = video.getFilterStack().getFilters();
        if (filterIndex<0 || filterIndex>=filters.size()) return;
        Filter filter = filters.get(filterIndex);
	  	  control.loadObject(filter);
	  	  JDialog inspector = filter.getInspector();
	  	  if (inspector!=null) {
	  	  	inspector.setVisible(true);
	  	  }
	  	  VideoClip clip = panel.getPlayer().getVideoClip();
	  	  panel.getPlayer().setStepNumber(clip.frameToStep(frameNumber));
      }
    }
  	
    public String getPresentationName() {
      return TrackerRes.getString("Undo.Description.Edit")+" "+filterType; //$NON-NLS-1$ //$NON-NLS-2$
    }

  }
  
}

/**
 * An UndoManager that exposes it's edits.
 */
class MyUndoManager extends UndoManager {
	public UndoableEdit getLastEdit() {
		return this.lastEdit();
	}
	
	public UndoableEdit getNextEdit() {
		return this.editToBeUndone();
	}
	
}

/**
 * A class used for name, footprint and color edits.
 */
class TrackProperties {
	String name;
	String[] footprints;
	Color[] colors;
	
	TrackProperties(TTrack track) {
		name = track.getName();
		if (track instanceof ParticleDataTrack) {
			ParticleDataTrack dt = (ParticleDataTrack)track;
			ArrayList<ParticleDataTrack> points = dt.allPoints();
			colors = new Color[points.size()+1];
			colors[colors.length-1] = dt.getModelFootprint().getColor();
			for (int i=0; i<points.size(); i++) {
				ParticleDataTrack next = points.get(i);
				colors[i] = next.getColor();
			}
			footprints = new String[points.size()+1];
			footprints[footprints.length-1] = dt.getModelFootprintName();
			for (int i=0; i<points.size(); i++) {
				ParticleDataTrack next = points.get(i);
				footprints[i] = next.getFootprintName();
			}
		}
		else {
			footprints = new String[] {track.getFootprintName()};
			colors = new Color[] {track.getColor()};
		}
	}
	
	TrackProperties(String name, String[] footprints, Color[] colors) {
		this.name = name;
		this.footprints = footprints;
		this.colors = colors;
	}
	
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
			TrackProperties props = (TrackProperties)obj;
			control.setValue("name", props.name); //$NON-NLS-1$
			control.setValue("footprints", props.footprints); //$NON-NLS-1$
			control.setValue("colors", props.colors); //$NON-NLS-1$
		}

		@Override
		public Object createObject(XMLControl control) {
			String name = control.getString("name"); //$NON-NLS-1$
			String[] footprints = (String[])control.getObject("footprints"); //$NON-NLS-1$
			Color[] colors = (Color[])control.getObject("colors"); //$NON-NLS-1$
			return new TrackProperties(name, footprints, colors);
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			return obj;
		}
  
  }
}

