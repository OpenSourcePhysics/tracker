/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays track views selected from a dropdown list. This is an abstract
 * class and cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class TrackChooserTView extends JPanel implements TView {

	// instance fields
	protected TrackerPanel trackerPanel;
	protected Map<Object, TTrack> tracks = new HashMap<Object, TTrack>(); // maps dropdown items to track
	protected Map<TTrack, TrackView> trackViews; // maps track to its trackView
	protected TTrack selectedTrack;
	protected boolean refreshing;

	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
	private JComboBox<Object[]> trackComboBox;
	private JPanel noData;
	private JLabel noDataLabel;
	
	
	protected void setNodataLabel(String text) {
		noDataLabel.setText(text);
	}
	/**
	 * Constructs a TrackChooserView for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	protected TrackChooserTView(TrackerPanel panel) {
		super(new CardLayout());
		if (panel == null) {
			// just a place-holder 
			return;
		}
		trackerPanel = panel;
		init();
		setBackground(panel.getBackground());
		// create combobox with custom renderer for tracks
		trackComboBox = new JComboBox<Object[]>() {
			// override getMaximumSize method so has same height as chooser button
			@Override
			public Dimension getMaximumSize() {
				return TViewChooser.getButtonMaxSize(this,
						new Dimension(getPreferredSize().width, super.getMaximumSize().height),
						getMinimumSize().height);
			}
		};
		trackComboBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 1));
		toolbarComponents.add(trackComboBox);
		// custom cell renderer for dropdown items
		TrackRenderer renderer = new TrackRenderer();
		trackComboBox.setRenderer(renderer);
		// add ActionListener to select a track and display its trackview
		trackComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropDownAction();
			}
		});
		// create the noData panel
		noData = new JPanel();
		noDataLabel = new JLabel();
		Font font = new JTextField().getFont();
		noDataLabel.setFont(font);
		noData.add(noDataLabel);
		noData.setBackground(getBackground());
		noData.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (OSPRuntime.isPopupTrigger(e)) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Dialog.Button.Help") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
					helpItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (TrackChooserTView.this.getViewType() == TView.VIEW_TABLE) {
								trackerPanel.getTFrame().showHelp("datatable", 0); //$NON-NLS-1$
							} else {
								trackerPanel.getTFrame().showHelp("plot", 0); //$NON-NLS-1$
							}
						}
					});
					popup.add(helpItem);
					FontSizer.setFonts(popup, FontSizer.getLevel());
					popup.show(noData, e.getX(), e.getY());
				}
			}
		});
	}

	protected void dropDownAction() {
		if (refreshing)
			return;
		// show the trackView for the selected track
		Object item = trackComboBox.getSelectedItem();
		TTrack track = tracks.get(item);
//if (track==selectedTrack) return;
		String name = (String) ((Object[]) item)[1];
		if (track != null) {
			trackerPanel.changed = true;
			TrackView trackView = getTrackView(track);
			// remove step propertyChangeListeners from prev selected track
			TTrack prevTrack = selectedTrack;
			TrackView prevView = null;
			if (prevTrack != null) {
				prevView = getTrackView(prevTrack);
				prevTrack.removeStepListener(prevView);
				if (prevView instanceof PlotTrackView) {
					PlotTrackView plotView = (PlotTrackView) prevView;
					for (TrackPlottingPanel plot : plotView.plots) {
						for (TTrack guest : plot.guests) {
							guest.removeStepListener(prevView);
						}
					}
				}
			}
			// add step propertyChangeListener to new track
			track.addStepListener(trackView);
			if (trackView instanceof PlotTrackView) {
				PlotTrackView plotView = (PlotTrackView) trackView;
				for (TrackPlottingPanel plot : plotView.plots) {
					for (TTrack guest : plot.guests) {
						guest.addStepListener(trackView);
					}
				}
			}
			selectedTrack = track;
			Step step = trackerPanel.getSelectedStep();
			if (step != null && step.getTrack() == track)
				trackView.refresh(step.getFrameNumber(), DataTable.MODE_TRACK_CHOOSE);
			else
				trackView.refresh(trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_CHOOSE);
			CardLayout layout = (CardLayout) getLayout();
			layout.show(this, name);
			TFrame.repaintT(this);
			firePropertyChange(TView.PROPERTY_TVIEW_TRACKVIEW, trackView, prevView);
			// inform track views
			PropertyChangeEvent event = new PropertyChangeEvent(this, TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
					null, track);
			Iterator<TTrack> it = trackViews.keySet().iterator();
			while (it.hasNext()) {
				TTrack nextTrack = it.next();
				TrackView next = trackViews.get(nextTrack);
				next.propertyChange(event);
			}
		}
	}

	/**
	 * Refreshes the dropdown list and track views.
	 */
	@Override
	public void refresh() {
		if (Tracker.timeLogEnabled)
			Tracker.logTime(getClass().getSimpleName() + hashCode() + " refresh"); //$NON-NLS-1$
		refreshing = true;
		// get previously selected track
		TTrack selectedTrack = getSelectedTrack();
		TTrack defaultTrack = null;
		// get views and rebuild for all tracks on trackerPanel
		Map<TTrack, TrackView> newViews = new HashMap<TTrack, TrackView>();
		removeAll(); // removes views from card layout
		tracks.clear();
		trackComboBox.removeAllItems();
		for (TTrack track : trackerPanel.getTracks()) {
			// include only viewable tracks
			if (!track.isViewable())
				continue;
			if (defaultTrack == null) {
				defaultTrack = track;
			}
			TrackView trackView = getTrackView(track);
			if (trackView == null)
				trackView = createTrackView(track);
			trackView.refreshGUI();
			newViews.put(track, trackView);
			String trackName = track.getName("point"); //$NON-NLS-1$
			Object[] item = new Object[] { trackView.getIcon(), trackName };
			trackComboBox.addItem(item);
			add(trackView, trackName);
			tracks.put(item, track);
		}
		validate();
		trackViews = newViews;
		// select previously selected track, if any
		refreshing = false;
		setSelectedTrack(selectedTrack == null || getTrackView(selectedTrack) == null
				? defaultTrack : selectedTrack);
		trackComboBox.setToolTipText(TrackerRes.getString("TrackChooserTView.DropDown.Tooltip")); //$NON-NLS-1$
	}

	/**
	 * Refreshes the menus.
	 */
	abstract protected void refreshMenus();
	
	protected void getMenuItems() {
		
	}

	/**
	 * Determines if the specified track is currently displayed.
	 * 
	 * @param track the track
	 * @return true if this TView is displayed and the track is selected
	 */
	protected boolean isTrackViewDisplayed(TTrack track) {
		return (track == getSelectedTrack() && TViewChooser.isSelectedView(this));
	}

	/**
	 * Initializes this view
	 */
	@Override
	public void init() {
		cleanup();
		// add this listener to tracker panel
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
		trackerPanel.addPropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this);
		trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this);
		trackerPanel.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_RADIANANGLES, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_FUNCTION, this);
		// add this listener to tracks
		for (TTrack track : trackerPanel.getTracks()) {
			addTrackListener(track);
		}
	}

	private void addTrackListener(TTrack track) {
		// OOPS!
//		track.addPropertyChangeListener("stepnumber", this);
//		track.addPropertyChangeListener("image", this);
		track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this);
		track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this);
		track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this);
//		track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this);
	}

	private void removeTrackListener(TTrack track) {
//		track.removePropertyChangeListener("stepnumber", this);
//		track.removePropertyChangeListener("image", this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this);
//		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this);
	}

	/**
	 * Cleans up this view
	 */
	@Override
	public void cleanup() {
		// remove this listener from tracker panel
		if (trackerPanel == null)
			return;
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
		trackerPanel.removePropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this);
		trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this);
		trackerPanel.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_RADIANANGLES, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_FUNCTION, this);
		// remove this listener from tracks
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			removeTrackListener(track);
		}
	}

	/**
	 * Disposes of the view
	 */
	@Override
	public void dispose() {
		cleanup();
		if (trackViews == null)
			return;
		for (TTrack next : trackViews.keySet()) {
			trackViews.get(next).dispose();
		}
		trackViews.clear();
		tracks.clear();
		setSelectedTrack(null);
		remove(noData);
		trackerPanel = null;
	}

	@Override
	public void finalize() {
		OSPLog.finest(getClass().getSimpleName() + " recycled by garbage collector");
	}

	/**
	 * Gets the tracker panel containing the tracks
	 *
	 * @return the tracker panel
	 */
	@Override
	public TrackerPanel getTrackerPanel() {
		return trackerPanel;
	}

	/**
	 * Gets the selected track
	 *
	 * @return the track
	 */
	public TTrack getSelectedTrack() {
		return selectedTrack;
	}

	/**
	 * Sets the selected track
	 *
	 * @param track the track to be selected
	 */
	public void setSelectedTrack(TTrack track) {
		OSPLog.debug("TrackChooser.setSelected " + track);
		if (track == null) {
			String msg;
			switch (getViewType()) {
			case TView.VIEW_TABLE:
				msg = "TableTView.Label.NoData"; //$NON-NLS-1$
				noDataLabel.setText(TrackerRes.getString(msg));
				break;
			default:
			case TView.VIEW_PLOT:
				msg = "PlotTView.Label.NoData"; //$NON-NLS-1$
				noDataLabel.setText(TrackerRes.getString(msg));
				break;
			}
			add(noData, "noData");
			selectedTrack = null;
			return;
		}
		if (!track.isViewable() || !trackerPanel.containsTrack(track))
			return;
		if (track == selectedTrack && tracks.get(trackComboBox.getSelectedItem()) == track) {
			// just refresh the selected TrackView
			getTrackView(selectedTrack).refresh(trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_SELECT);
			return;
		}
		Iterator<Object> it = tracks.keySet().iterator();
		while (it.hasNext()) {
			Object item = it.next();
			if (tracks.get(item) == track) {
				OSPLog.debug("TrackChooser.setSelected found " + track);
				removeTrackListener(track);
				addTrackListener(track);
				// select the track dropdown item
				trackComboBox.setSelectedItem(item);
				break;
			}
		}
	}

	/**
	 * Gets the track view for the specified track
	 *
	 * @param track the track to be viewed
	 * @return the track view
	 */
	public TrackView getTrackView(TTrack track) {
		return (trackViews == null ? null : trackViews.get(track));
	}

	/**
	 * Gets the name of the view
	 *
	 * @return the name of this view
	 */
	@Override
	public abstract String getViewName();

	/**
	 * Gets the toolbar components
	 *
	 * @return an ArrayList of components to be added to a toolbar at the
	 * top of the view. This includes View buttton 
	 */
	@Override
	public ArrayList<Component> getToolBarComponents() {
		toolbarComponents.clear();
		TrackView trackView = getTrackView(getSelectedTrack());
		if (trackView != null) {
			toolbarComponents.add(trackView.getViewButton());
		}
		if (trackComboBox.getItemCount() > 0) {
			toolbarComponents.add(trackComboBox);
		}
		if (trackView != null) {
			toolbarComponents.addAll(trackView.getToolBarComponents());
		}
		return toolbarComponents;
	}

	/**
	 * Returns true if this view is in a custom state.
	 *
	 * @return true if in a custom state, false if in the default state
	 */
	@Override
	public boolean isCustomState() {
		if (tracks.size() > 1) {
			// custom state if selected track is not the first in trackerPanel
			for (TTrack track : trackerPanel.getUserTracks()) {
				if (!track.isViewable())
					continue;
				if (track != selectedTrack)
					return true;
				break;
			}
		}
		for (Iterator<TTrack> it = trackViews.keySet().iterator(); it.hasNext();) {
			TrackView view = trackViews.get(it.next());
			if (view.isCustomState())
				return true;
		}
		return false;
	}

	/**
	 * Responds to property change events. This receives the following events:
	 * "track", "transform" from trackerPanel; "name", "color", footprint" and
	 * "data" from selected track.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		TTrack track;
		TFrame frame;
		TrackView view;
		String name = e.getPropertyName();
		switch (name) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			// track has been added
			track = (TTrack) e.getOldValue();
			if (track != null) {
				removeTrackListener(track);
				view = trackViews.get(track);
				if (view != null) {
					view.dispose();
					trackViews.remove(track);
				}
			}
			refresh();
			frame = trackerPanel.getTFrame();
			if (frame != null)
				TFrame.repaintT(frame);
			// select a newly added track
			track = (TTrack) e.getNewValue();
			if (track != null)
				setSelectedTrack(track);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR: // tracks have been cleared
			for (Integer n : TTrack.activeTracks.keySet()) {
				track = TTrack.activeTracks.get(n);
				removeTrackListener(track);
				if ((view = trackViews.get(track)) != null) {
					view.dispose();
					trackViews.remove(track);
				}
			}
			refresh();
			frame = trackerPanel.getTFrame();
			if (frame != null)
				TFrame.repaintT(frame);
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM: // coords have changed
			if ((track = getSelectedTrack()) != null && (view = getTrackView(track)) != null) {
				// if track is a particle model, ignore if coords are adjusting
				if (track instanceof ParticleModel) {
					ImageCoordSystem coords = trackerPanel.getCoords();
					if (coords.isAdjusting())
						return;
				}
				Step step = track.getStep(trackerPanel.getSelectedPoint(), trackerPanel);
				view.refresh(step == null ? trackerPanel.getFrameNumber() : step.getFrameNumber(), DataTable.MODE_TRACK_TRANSFORM);
			}
			break;
		case TTrack.PROPERTY_TTRACK_DATA: // data structure has changed
			if ((track = getSelectedTrack()) != null && (view = getTrackView(track)) != null) {
				view.refresh(trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_DATA);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_FUNCTION: // data function has changed
		case TFrame.PROPERTY_TFRAME_RADIANANGLES:
			// angle units have changed
			// refresh views of all tracks
			for (TTrack t : trackerPanel.getTracks()) {
				if ((view = getTrackView(t)) != null) {
					view.refreshGUI();
					view.refresh(trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_FUNCTION);
				}
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
			// step number has changed
			if ((track = getSelectedTrack()) != null && (view = getTrackView(track)) != null) {
				view.refresh(trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_STEP);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE:
			// video image has changed
//			if ((track = getSelectedTrack()) != null && (view = getTrackView(track)) != null) {
//				view.refresh(trackerPanel.getFrameNumber());
//			}
			break;
		case TTrack.PROPERTY_TTRACK_COLOR:
		case TTrack.PROPERTY_TTRACK_NAME:
		case TTrack.PROPERTY_TTRACK_FOOTPRINT:
			// track property has changed
			refresh();
		}
	}

	/**
	 * Creates a view for the specified track
	 *
	 * @param track the track to be viewed
	 * @return the track view
	 */
	protected abstract TrackView createTrackView(TTrack track);

	/**
	 * Gets a track with the specified name
	 *
	 * @param name the name of the track
	 * @return the track
	 */
	protected TTrack getTrack(String name) {
		for (TTrack track : trackerPanel.getTracks()) {
			if (track.getName().equals(name))
				return track;
		}
		return null;
	}

	@Override
	public void paint(Graphics g) {
		// from TFrame.repaint();
		super.paint(g);
	}
	
	@Override
	public void repaint() {
		// from CardLayout reshape
		if (trackerPanel != null && trackerPanel.isPaintable())
			super.repaint();
	}


}
