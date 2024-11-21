/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import javax.swing.JDialog;
import javax.swing.undo.*;

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
@SuppressWarnings("serial")
public class Undo {

	// static fields
	protected static Map<Integer, Undo> undomap = new HashMap<Integer, Undo>();

	// instance fields
	protected UndoableEditSupport undoSupport;
	protected MyUndoManager undoManager;

	private TFrame frame;

	private Integer panelID;

	/**
	 * Private constructor.
	 * @param panel 
	 */
	private Undo(TrackerPanel panel) {
		this.frame = panel.getTFrame();
		this.panelID = panel.getID();

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
		String desc = TrackerRes.getString("TMenuBar.MenuItem.Undo"); //$NON-NLS-1$
		UndoableEdit edit = getUndo(panel).undoManager.getUndoEdit();
		if (edit != null) {
			desc += " " + edit.getPresentationName(); //$NON-NLS-1$
		}
		return desc;
	}

	/**
	 * Undoes the most recently posted edit for the specified panel.
	 * 
	 * @param panel the TrackerPanel
	 */
	public static void undo(TrackerPanel panel) {
		// check last edit and (if trackEdit) modify its redo state with current state
		// of track
		if (!getUndo(panel).undoManager.canRedo()) {
			UndoableEdit lastEdit = getUndo(panel).undoManager.getUndoEdit();
			if (lastEdit != null && lastEdit instanceof TrackEdit) {
				TrackEdit trackEdit = (TrackEdit) lastEdit;
				String name = trackEdit.trackName;
				TTrack track = panel.getTrack(name);
				if (track != null) {
					trackEdit.redo = new XMLControlElement(track).toXML();
				}
			}
		}
		getUndo(panel).undoManager.undo();
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
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
		String desc = TrackerRes.getString("TMenuBar.MenuItem.Redo"); //$NON-NLS-1$
		UndoableEdit edit = getUndo(panel).undoManager.getRedoEdit();
		if (edit != null) {
			desc += " " + edit.getPresentationName(); //$NON-NLS-1$
		}
		return desc;
	}

	/**
	 * Redoes the most recently undone edit for the specified panel.
	 * 
	 * @param panel the TrackerPanel
	 */
	public static void redo(TrackerPanel panel) {
		getUndo(panel).undoManager.redo();
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
		panel.repaint();
	}

//_____________________ private and protected methods ______________________

	/**
	 * Posts an undoable edit for a deleted track.
	 * 
	 * @param track the track
	 */
	protected static void postTrackDelete(TTrack track) {
		TrackerPanel panel = track.tp;
		if (panel == null)
			return;
		UndoableEdit edit = getUndo(panel).new TrackDelete(panel, track);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a TrackerPanel cleared of tracks.
	 * 
	 * @param panel the TrackerPanel that has been cleared
	 * @param xml   a list of XML strings describing the cleared tracks
	 */
	protected static void postTrackClear(TrackerPanel panel, List<String> xml) {
		UndoableEdit edit = getUndo(panel).new TrackClear(panel, xml);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a changed track.
	 * 
	 * @param track   the changed track
	 * @param control an XMLControl with the previous state of the track
	 */
	protected static void postTrackEdit(TTrack track, XMLControl control) {
		TrackerPanel panel = track.tp;
		if (panel == null)
			return;
		UndoableEdit edit = getUndo(panel).new TrackEdit(track, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for multiple changed tracks.
	 * 
	 * @param tracksAndXMLControls list of array elements, each element = {track,
	 *                             track's previous state}
	 */
	protected static void postMultiTrackEdit(ArrayList<Object[]> tracksAndXMLControls) {
		if (tracksAndXMLControls == null || tracksAndXMLControls.size() == 0)
			return;
		TTrack track = (TTrack) tracksAndXMLControls.get(0)[0];
		TrackerPanel panel = track.tp;
		if (panel == null)
			return;

		UndoableEdit edit = null;
		for (Object[] next : tracksAndXMLControls) {
			track = (TTrack) next[0];
			XMLControl control = (XMLControl) next[1];
			if (edit == null) { // create track edit for first track
				edit = getUndo(panel).new TrackEdit(track, control);
			} else { // create compound edit for subsequent tracks
				UndoableEdit trackEdit = getUndo(panel).new TrackEdit(track, control);
				edit = getUndo(panel).new CompoundEdit(trackEdit, edit);
			}
		}
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a changed step.
	 * 
	 * @param step    the changed step
	 * @param control an XMLControl with the previous state of the track
	 */
	protected static void postStepEdit(Step step, XMLControl control) {
		TrackerPanel panel = step.getTrack().tp;
		if (panel == null)
			return;
		UndoableEdit edit = getUndo(panel).new StepEdit(step, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a changed StepSet.
	 * 
	 * @param steps   the changed StepSet
	 * @param control an XMLControl with the previous state of the StepSet
	 */
	protected static void postStepSetEdit(StepSet steps, XMLControl control) {
		TrackerPanel panel = steps.panel();
		TTrack track = null;
		boolean singleTrack = true;
		for (Step step : steps) {
			if (step.getTrack() != null) {
				if (track == null)
					track = step.getTrack();
				else { // track not null, so compare
					if (track != step.getTrack()) {
						singleTrack = false;
						break;
					}
				}
			}
		}
		UndoableEdit edit;
		if (track != null && singleTrack) {
			edit = getUndo(panel).new TrackEdit(track, control);
		} else {
			edit = getUndo(panel).new StepSetEdit(steps, control);
		}
		getUndo(panel).undoSupport.postEdit(edit);
		steps.setChanged(false); // prevents clear() method from saving another undoable edit
		steps.clear();
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);	}

	/**
	 * Posts an undoable edit for a changed coordinate system.
	 * 
	 * @param panel   the TrackerPanel with the changed coords
	 * @param control an XMLControl with the previous state of the coords
	 */
	protected static void postCoordsEdit(TrackerPanel panel, XMLControl control) {
		UndoableEdit edit = getUndo(panel).new CoordsEdit(panel, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);	}

	/**
	 * Posts a compound undoable edit for tracks that control the coords.
	 * 
	 * @param track         the changed track
	 * @param trackControl  an XMLControl with the previous state of the track
	 * @param coordsControl an XMLControl with the previous state of the coords
	 */
	protected static void postTrackAndCoordsEdit(TTrack track, XMLControl trackControl, XMLControl coordsControl) {
		TrackerPanel panel = track.tp;
		if (panel == null)
			return;
		// coords edit first!
		UndoableEdit edit1 = getUndo(panel).new CoordsEdit(panel, coordsControl);
		UndoableEdit edit2 = getUndo(panel).new TrackEdit(track, trackControl);
		UndoableEdit compound = getUndo(panel).new CompoundEdit(edit1, edit2);
		getUndo(panel).undoSupport.postEdit(compound);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);	}

	/**
	 * Posts an undoable edit for an edited image video.
	 * 
	 * @param panel the TrackerPanel with the new video clip
	 * @param paths paths to the video files
	 * @param index step number at which edit occured
	 * @param step  step number after edit occured
	 * @param added true if a frame was added
	 */
	protected static void postImageVideoEdit(TrackerPanel panel, String[] paths, int index, int step, boolean added) {
		UndoableEdit edit = getUndo(panel).new ImageVideoEdit(panel, paths, index, step, added);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);	}

	/**
	 * Posts an undoable edit for a replaced video clip.
	 * 
	 * @param panel   the TrackerPanel with the new video clip
	 * @param control an XMLControl describing the previous video clip
	 */
	protected static void postVideoReplace(TrackerPanel panel, XMLControl control) {
		UndoableEdit edit = getUndo(panel).new VideoReplace(panel, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a video filter deletion.
	 * 
	 * @param panel  the TrackerPanel with the new video clip
	 * @param filter the deleted filter
	 */
	protected static void postFilterDelete(TrackerPanel panel, Filter filter) {
		UndoableEdit edit = getUndo(panel).new FilterDelete(panel, filter);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a change to a video filter.
	 * 
	 * @param panel   the TrackerPanel with the new video clip
	 * @param filter  the filter
	 * @param control an XMLControl with the previous state of the filter
	 */
	protected static void postFilterEdit(TrackerPanel panel, Filter filter, XMLControl control) {
		UndoableEdit edit = getUndo(panel).new FilterEdit(panel, filter, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);	
	}

	/**
	 * Posts an undoable edit for a TrackerPanel cleared of video filters.
	 * 
	 * @param panel the TrackerPanel that has been cleared
	 * @param xml   a list of XML strings describing the cleared tracks
	 */
	protected static void postFilterClear(TrackerPanel panel, List<String> xml) {
		UndoableEdit edit = getUndo(panel).new FilterClear(panel, xml);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	/**
	 * Posts an undoable edit for a footprint, name or color change.
	 * 
	 * @param track   the track with the changed property
	 * @param control an XMLControl with the previous state of the footprint
	 */
	protected static void postTrackDisplayEdit(TTrack track, XMLControl control) {
		TrackerPanel panel = track.tp;
		if (panel == null)
			return;
		UndoableEdit edit = getUndo(panel).new TrackDisplayEdit(track, control);
		getUndo(panel).undoSupport.postEdit(edit);
		panel.refreshMenus(TMenuBar.REFRESH_UNDO);
	}

	private static Undo getUndo(TrackerPanel panel) {
		Undo undo = undomap.get(panel.getID());
		if (undo == null) {
			undo = new Undo(panel);
			undomap.put(panel.getID(), undo);
		}
		return undo;
	}

	/**
	 * Returns XMLControl for a VideoClip after adding absolutePath of video
	 */
	public static XMLControl getXMLControl(VideoClip clip) {
		XMLControl control = new XMLControlElement(clip);
		if (clip.getVideo() != null) {
			// add absolute path to video
			String fullpath = (String)clip.getVideo().getProperty("absolutePath");
			XMLControl child = control.getChildControl("video");
			if (child != null) 
				child.setValue("absolutePath", fullpath);
		}
		return control;
	}


//______________________ inner UndoableEdit classes ______________________

	/**
	 * A class to undo/redo track changes.
	 */
	protected class TrackEdit extends TEdit {

		String trackName, trackType;
		boolean isTextColumn = false;

		private TrackEdit(TTrack track, XMLControl control) {
			super(track.tp, track, control);
			isTextColumn = control.getBoolean("isTextColumn");
			trackName = track.getName();
			String s = track.getClass().getSimpleName();
			trackType = TrackerRes.getString(s + ".Name"); //$NON-NLS-1$
			if (trackType.startsWith("!")) { //$NON-NLS-1$
				trackType = s;
			}
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			TTrack track = panel().getTrack(trackName);
			if (track == null)
				return;
			// turn off view refreshing until finished
			TrackChooserTView.ignoreRefresh = true;
			control.loadObject(track);
			track.erase();
			TrackChooserTView.ignoreRefresh = false;
			if (isTextColumn)
				track.firePropertyChange(TTrack.PROPERTY_TTRACK_TEXTCOLUMN, null, null);
			else
				track.firePropertyChange(TTrack.PROPERTY_TTRACK_STEPS, TTrack.HINT_STEP_ADDED_OR_REMOVED, null);
//			track.notifyUndoLoaded();
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ trackType;
		}

	}

	/**
	 * A class to undo/redo step changes.
	 */
	protected class StepEdit extends TEdit {

		Step step;
		String trackType;

		private StepEdit(Step step, XMLControl control) {
			super(step.getTrack().tp, step, control);
			this.step = step;
			String s = step.getTrack().getClass().getSimpleName();
			trackType = TrackerRes.getString(s + ".Name"); //$NON-NLS-1$
			if (trackType.startsWith("!")) { //$NON-NLS-1$
				trackType = s;
			}
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			control.loadObject(step);
			step.erase();
			panel().refreshTrackBar();
			//TTrackBar.getTrackbar(panel).refresh();
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ trackType;
		}

	}

	/**
	 * A class to undo/redo footprint, color and name changes.
	 */
	protected class TrackDisplayEdit extends TEdit {

		String undoName, redoName;
		String trackName, trackType;

		private TrackDisplayEdit(TTrack track, XMLControl control) {
			super(track.tp, new TrackProperties(track), control);
			control = new XMLControlElement(undo);
			TrackProperties props = (TrackProperties) control.loadObject(null);
			undoName = track.getName();
			redoName = props.name;
			String s = track.getClass().getSimpleName();
			trackType = TrackerRes.getString(s + ".Name"); //$NON-NLS-1$
			if (trackType.startsWith("!")) { //$NON-NLS-1$
				trackType = s;
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			trackName = undoName;
			super.undo();
		}

		@Override
		public void redo() throws CannotUndoException {
			trackName = redoName;
			super.redo();
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			TrackProperties props = (TrackProperties) control.loadObject(null);
			TTrack track = panel().getTrack(trackName);
			if (track == null)
				return;
			track.setName(props.name);
			if (props.colors != null) {
				if (props.colors.length == 1) {
					track.setColor(props.colors[0]);
				} else if (track instanceof ParticleDataTrack) {
					((ParticleDataTrack) track).setAllColors(props.colors);
				}
			}
			if (props.footprints != null) {
				if (props.footprints.length == 1) {
					track.setFootprint(props.footprints[0]);
				} else if (track instanceof ParticleDataTrack) {
					((ParticleDataTrack) track).setAllFootprints(props.footprints);
				}
			}
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ trackType;
		}

	}

	/**
	 * A class to undo/redo stepset changes.
	 */
	protected class StepSetEdit extends TEdit {

		private StepSetEdit(StepSet steps, XMLControl control) {
			super(steps.panel(), steps, control);
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			StepSet steps = new StepSet(frame, panelID);
			control.loadObject(steps);
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("Undo.Description.Steps"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo coords changes.
	 */
	protected class CoordsEdit extends TEdit {

		private CoordsEdit(TrackerPanel panel, XMLControl control) {
			super(panel, panel.getCoords(), control);
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			ImageCoordSystem coords = panel().getCoords();
			control.loadObject(coords);
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("TMenuBar.Menu.Coords"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo image video edits.
	 */
	protected class ImageVideoEdit extends AbstractUndoableEdit {

		String[] paths; // image path
		int n; // add/remove index
		int step; // post-removal step number
		boolean added; // true if original edit was an addImage

		private ImageVideoEdit(TrackerPanel panel, String[] imagePaths, int index, int step, boolean added) {
			paths = imagePaths;
			n = index;
			this.step = step;
			this.added = added;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (added)
				removeImages();
			else
				addImages();
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			if (added)
				addImages();
			else
				removeImages();
		}

		private void addImages() {
			if (paths == null || paths.length == 0)
				return;
			try {
				int index = n;
				for (int i = 0; i < paths.length; i++) {
					String path = paths[i];
					if (path == null)
						continue;
					TrackerPanel panel = panel();
					ImageVideo imageVid = (ImageVideo) panel.getVideo();
					imageVid.insert(path, index, false);
					VideoClip clip = panel.getPlayer().getVideoClip();
					clip.setStepCount(imageVid.getFrameCount());
					int step = panel.getPlayer().getVideoClip().frameToStep(index++);
					panel.getPlayer().setStepNumber(step);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void removeImages() {
			TrackerPanel panel = panel();
			if (panel.getVideo() instanceof ImageVideo) {
				ImageVideo imageVid = (ImageVideo) panel.getVideo();
				for (int i = 0; i < paths.length; i++) {
					imageVid.remove(n);
				}
				int len = imageVid.getFrameCount();
				VideoClip clip = panel.getPlayer().getVideoClip();
				clip.setStepCount(len);
				panel.getPlayer().setStepNumber(step);
			}
		}

		@Override
		public String getPresentationName() {
			if (added) {
				return TrackerRes.getString("Undo.Description.Add") + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ TrackerRes.getString("Undo.Description.Images"); //$NON-NLS-1$
			}
			return TrackerRes.getString("Undo.Description.Remove") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("Undo.Description.Images"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo video replacements.
	 */
	protected class VideoReplace extends TEdit {

		private VideoReplace(TrackerPanel panel, XMLControl control) {
			super(panel, panel.getPlayer().getVideoClip(), control);
			redo = getXMLControl(panel.getPlayer().getVideoClip()).toXML();
		}

		@Override
		public void undo() throws CannotUndoException {
			// if ImageVideo, save invalid images
			Video video = panel().getVideo();
			if (video instanceof ImageVideo) {
				((ImageVideo) video).saveInvalidImages();
			}
			// refresh redo state
			redo = getXMLControl(panel().getPlayer().getVideoClip()).toXML();
			super.undo();
		}

		@Override
		public void redo() throws CannotUndoException {
			// if ImageVideo, save invalid images
			Video video = panel().getVideo();
			if (video instanceof ImageVideo) {
				((ImageVideo) video).saveInvalidImages();
			}
			// refresh undo state
			undo = getXMLControl(panel().getPlayer().getVideoClip()).toXML();
			super.redo();
		}

		@Override
		protected void load(String xml) {
			// clear filters from old video
			TrackerPanel panel = panel();
			Video video = panel.getVideo();
			if (video != null) {
				TActions.clearFiltersAction(panel, false);
			}
			XMLControl control = new XMLControlElement(xml);
			VideoClip clip = (VideoClip) control.loadObject(null);
			Video newVid = clip.getVideo();
			if (VideoIO.loadIncrementally && newVid != null 
					&& newVid instanceof IncrementallyLoadable) {
				// load one last time to finalize
				control.loadObject(clip); //$NON-NLS-1$
			}
			panel.getPlayer().setVideoClip(clip);
			video = panel.getVideo();
			if (video != null) {
				for (Filter filter : video.getFilterStack().getFilters()) {
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

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Replace") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("Undo.Description.Video"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo changes to objects associated with a TrackerPanel. The
	 * constructor takes the TrackerPanel, the object AFTER being changed, and an
	 * XMLControl storing the state of the object BEFORE the changes.
	 */
	protected abstract class TEdit extends AbstractUndoableEdit {

		String undo; // xml string
		String redo; // xml string

		protected TFrame frame;
		protected Integer panelID;

		protected TEdit(TrackerPanel panel, Object obj, XMLControl control) {
			this.frame = panel.getTFrame();
			this.panelID = panel.getID();
			undo = control.toXML();
			control = new XMLControlElement(obj);
			redo = control.toXML();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			load(undo);
		}

		@Override
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

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			editA.undo();
			editB.undo();
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			editA.redo();
			editB.redo();
		}

		@Override
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
		String trackType;

		private TrackDelete(TrackerPanel panel, TTrack track) {
			XMLControl control = new XMLControlElement(track);
			xml = control.toXML();
			String s = track.getClass().getSimpleName();
			trackType = TrackerRes.getString(s + ".Name"); //$NON-NLS-1$
			if (trackType.startsWith("!")) { //$NON-NLS-1$
				trackType = s;
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			XMLControl control = new XMLControlElement(xml);
			TTrack track = (TTrack) control.loadObject(null);
			TrackerPanel panel = panel();
			panel.addTrack(track);
			trackID = track.getID();
			panel.requestFocus();
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			TTrack track = TTrack.getTrack(trackID);
			track.delete(false);
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Delete") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ trackType;
		}

	}

	/**
	 * A class to undo/redo clearing tracks.
	 */
	protected class TrackClear extends AbstractUndoableEdit {

		List<String> xml;

		private TrackClear(TrackerPanel trackerPanel, List<String> xml) {
			this.xml = xml;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Iterator<String> it = xml.iterator();
			TrackerPanel panel = panel();
			while (it.hasNext()) {
				XMLControl control = new XMLControlElement(it.next());
				TTrack track = (TTrack) control.loadObject(null);
				panel.addTrack(track);
			}
			panel.requestFocus();
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			panel().clearTracks();
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Clear") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("Undo.Description.Tracks"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo filter deletion.
	 */
	protected class FilterDelete extends AbstractUndoableEdit {

		String xml;
		int i;
		Filter filter;
		String filterName;

		private FilterDelete(TrackerPanel trackerPanel, Filter filter) {
			super();
			xml = new XMLControlElement(filter).toXML();
			i = panel().getVideo().getFilterStack().lastIndexRemoved();
			filterName = filter.getClass().getSimpleName();
			int j = filterName.indexOf("Filter"); //$NON-NLS-1$
			if (j > 0 && j < filterName.length() - 1) {
				filterName = filterName.substring(0, j);
			}
			filterName = MediaRes.getString("VideoFilter." + filterName); //$NON-NLS-1$
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			TrackerPanel panel = panel();
			Video video = panel.getVideo();
			if (video != null) {
				XMLControl control = new XMLControlElement(xml);
				filter = (Filter) control.loadObject(null);
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

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			Video video = panel().getVideo();
			if (video != null) {
				filter.setVideoPanel(null);
				TMenuBar menubar = panel().getMenuBar(true);
				menubar.refreshing = true; // prevents posting another undoable edit
				video.getFilterStack().removeFilter(filter);
				menubar.refreshing = false;
				i = video.getFilterStack().lastIndexRemoved();
				filter = null; // eliminate all references to deleted filter
			}
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Delete") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("Undo.Description.Filter"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo filter clearing.
	 */
	protected class FilterClear extends AbstractUndoableEdit {

		List<String> xml;

		private FilterClear(TrackerPanel trackerPanel, List<String> xml) {
			this.xml = xml;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			TrackerPanel panel = panel();
			Video video = panel.getVideo();
			if (video != null) {
				for (String next : xml) {
					XMLControl control = new XMLControlElement(next);
					Filter filter = (Filter) control.loadObject(null);
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

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			TrackerPanel panel = panel();
			Video video = panel.getVideo();
			if (video != null) {
				FilterStack stack = video.getFilterStack();
				for (Filter filter : stack.getFilters()) {
					PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
					if (track != null) {
						panel.removeTrack(track);
						track.dispose();
					}
				}
				stack.clear();
			}
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Clear") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TrackerRes.getString("TMenuBar.MenuItem.VideoFilters"); //$NON-NLS-1$
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
			if (j > 0 && j < filterType.length() - 1) {
				filterType = filterType.substring(0, j);
			}
			filterType = MediaRes.getString("VideoFilter." + filterType); //$NON-NLS-1$
		}

		@Override
		protected void load(String xml) {
			XMLControl control = new XMLControlElement(xml);
			TrackerPanel panel = panel();
			Video video = panel.getVideo();
			if (video != null) {
				ArrayList<Filter> filters = video.getFilterStack().getFilters();
				if (filterIndex < 0 || filterIndex >= filters.size())
					return;
				Filter filter = filters.get(filterIndex);
				control.loadObject(filter);
				JDialog inspector = filter.getInspector();
				if (inspector != null) {
					inspector.setVisible(true);
				}
				VideoClip clip = panel.getPlayer().getVideoClip();
				panel.getPlayer().setStepNumber(clip.frameToStep(frameNumber));
			}
		}

		@Override
		public String getPresentationName() {
			return TrackerRes.getString("Undo.Description.Edit") + " " + filterType; //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	/**
	 * An UndoManager that exposes it's edits.
	 */
	static class MyUndoManager extends UndoManager {
		public UndoableEdit getUndoEdit() {
			return this.editToBeUndone();
		}
		
		public UndoableEdit getRedoEdit() {
			return this.editToBeRedone();
		}
		
	}
	
	protected TrackerPanel panel() {
		return (frame == null ? null : frame.getTrackerPanelForID(panelID));
	}


}


