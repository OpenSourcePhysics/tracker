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

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays track views selected from a dropdown list. This is an abstract
 * class and cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
public abstract class TrackChooserTView extends JPanel implements TView {

  // instance fields
  protected TrackerPanel trackerPanel;
  protected Map<TTrack, TrackView> trackViews = new HashMap<TTrack, TrackView>(); // maps track to its trackView
  protected Map<Object, TTrack> tracks = new HashMap<Object, TTrack>(); // maps dropdown items to track
  protected JComboBox dropdown;
  protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
  protected boolean refreshing;
  protected TTrack selectedTrack;
  protected JPanel noData;
  protected JLabel noDataLabel;

  /**
   * Constructs a TrackChooserView for the specified tracker panel.
   *
   * @param panel the tracker panel
   */
  protected TrackChooserTView(TrackerPanel panel) {
    super(new CardLayout());
    trackerPanel = panel;
    init();
    setBackground(panel.getBackground());
    // create combobox with custom renderer for tracks
    dropdown = new JComboBox() {
    	// override getMaximumSize method so has same height as chooser button
	    public Dimension getMaximumSize() {
	      Dimension dim = super.getMaximumSize();
	      Dimension preferred = getPreferredSize();
	      dim.width = preferred.width;
	      Dimension min = getMinimumSize();
	    	Container c = getParent().getParent();
	  		if (c instanceof TViewChooser) {
	  			int h = ((TViewChooser)c).chooserButton.getHeight();
	  			dim.height = Math.max(h, min.height);
	  		}
	      return dim;
	    }    	
    };
    dropdown.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 1));
    toolbarComponents.add(dropdown);
    // custom cell renderer for dropdown items
    TrackRenderer renderer= new TrackRenderer();
    dropdown.setRenderer(renderer);
    // add ActionListener to select a track and display its trackview
    dropdown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (refreshing) return;
        // show the trackView for the selected track
        Object item = dropdown.getSelectedItem();
        TTrack track = tracks.get(item);
//        if (track==selectedTrack) return;
        String name = (String)((Object[])item)[1];
        if (track != null) {
          trackerPanel.changed = true;
          TrackView trackView = getTrackView(track);
          // remove step propertyChangeListener from prev selected track
          TTrack prevTrack = selectedTrack;
          TrackView prevView = null;
          if (prevTrack != null) {
            prevView = getTrackView(prevTrack);
            prevTrack.removePropertyChangeListener("step", prevView); //$NON-NLS-1$
            prevTrack.removePropertyChangeListener("steps", prevView); //$NON-NLS-1$
            if (prevView instanceof PlotTrackView) {
            	PlotTrackView plotView = (PlotTrackView)prevView;
            	TrackPlottingPanel plot = plotView.plots[0];
            	for (TTrack guest: plot.guests) {
            		guest.removePropertyChangeListener("step", prevView); //$NON-NLS-1$
            		guest.removePropertyChangeListener("steps", prevView); //$NON-NLS-1$          		
            	}
            }
          }
          // add step propertyChangeListener to new track
          track.addPropertyChangeListener("step", trackView); //$NON-NLS-1$
          track.addPropertyChangeListener("steps", trackView); //$NON-NLS-1$
          if (trackView instanceof PlotTrackView) {
          	PlotTrackView plotView = (PlotTrackView)trackView;
          	TrackPlottingPanel plot = plotView.plots[0];
          	for (TTrack guest: plot.guests) {
          		guest.addPropertyChangeListener("step", trackView); //$NON-NLS-1$
          		guest.addPropertyChangeListener("steps", trackView); //$NON-NLS-1$          		
          	}
          }
          selectedTrack = track;
          Step step = trackerPanel.getSelectedStep();
          if (step !=  null && step.getTrack() == track)
            trackView.refresh(step.getFrameNumber());
          else
            trackView.refresh(trackerPanel.getFrameNumber());
          CardLayout layout = (CardLayout)getLayout();
          layout.show(TrackChooserTView.this, name);
          repaint();
          firePropertyChange("trackview", trackView, prevView); //$NON-NLS-1$
          // inform track views
          PropertyChangeEvent event = 
          	new PropertyChangeEvent(this, "track", null, track); //$NON-NLS-1$
          Iterator<TTrack> it = trackViews.keySet().iterator();
          while (it.hasNext()) {
          	TTrack nextTrack = it.next();
          	TrackView next = trackViews.get(nextTrack);
          	next.propertyChange(event);
          }
        }
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
      public void mousePressed(MouseEvent e) {
      	if (OSPRuntime.isPopupTrigger(e)) {
        	JPopupMenu popup = new JPopupMenu();
          JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Dialog.Button.Help")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
          helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (TrackChooserTView.this instanceof TableTView) {
            		trackerPanel.getTFrame().showHelp("datatable", 0); //$NON-NLS-1$
            	}
            	else {
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

  /**
   * Refreshes the dropdown list and track views.
   */
  public void refresh() {
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh"); //$NON-NLS-1$
    refreshing = true;
    // get previously selected track
    TTrack selectedTrack = getSelectedTrack();
    TTrack defaultTrack = null;
    // get views and rebuild for all tracks on trackerPanel
    Map<TTrack, TrackView> newViews = new HashMap<TTrack, TrackView>();
    removeAll(); // removes views from card layout
    tracks.clear();
    dropdown.removeAllItems();
    for (TTrack track: trackerPanel.getTracks()) {
      // include only viewable tracks
      if (!track.isViewable()) continue;
      if (defaultTrack == null) {
        defaultTrack = track;
      }
      TrackView trackView = getTrackView(track);
      if (trackView == null) trackView = createTrackView(track);
      trackView.refreshGUI();
      newViews.put(track, trackView);
      Object item = new Object[] {trackView.getIcon(), track.getName()};
      dropdown.addItem(item);
      add(trackView, track.getName());
      tracks.put(item, track);
    }
    validate();
    trackViews = newViews;
   // select previously selected track, if any
    refreshing = false;
    if (selectedTrack != null && getTrackView(selectedTrack) != null) {
      setSelectedTrack(selectedTrack);
    }
    else setSelectedTrack(defaultTrack);
    dropdown.setToolTipText(TrackerRes.getString("TrackChooserTView.DropDown.Tooltip")); //$NON-NLS-1$
  }

  /**
   * Refreshes the configuration.
   */
  protected void refreshMenus() {  	
  }
  
  /**
   * Initializes this view
   */
  public void init() {
    cleanup();
    // add this listener to tracker panel
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("image", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("data", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("radian_angles", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("function", this); //$NON-NLS-1$
    // add this listener to tracks
    for (TTrack track: trackerPanel.getTracks()) {
      track.addPropertyChangeListener("name", this); //$NON-NLS-1$
      track.addPropertyChangeListener("color", this); //$NON-NLS-1$
      track.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
      track.addPropertyChangeListener("data", this); //$NON-NLS-1$
    }
  }

  /**
   * Cleans up this view
   */
  public void cleanup() {
    // remove this listener from tracker panel
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("transform", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("image", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("data", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("radian_angles", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("function", this); //$NON-NLS-1$
    // remove this listener from tracks
    for (TTrack track: trackerPanel.getTracks()) {
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      track.removePropertyChangeListener("data", this); //$NON-NLS-1$
    }
  }

  /**
   * Gets the tracker panel containing the tracks
   *
   * @return the tracker panel
   */
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
  	if (track == null) {
  		add(noData, "noData"); //$NON-NLS-1$
  		selectedTrack = null;
  		return;
  	}
    if (!track.isViewable() || !trackerPanel.containsTrack(track)) return;
  	if (track==selectedTrack 
  			&& tracks.get(dropdown.getSelectedItem())==track) return;
    Iterator<Object> it = tracks.keySet().iterator();
    while (it.hasNext()) {
      Object item = it.next();
      if (tracks.get(item) == track) {
        // be sure listeners are registered once only
        track.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
        track.removePropertyChangeListener("image", this); //$NON-NLS-1$
        track.removePropertyChangeListener("name", this); //$NON-NLS-1$
        track.removePropertyChangeListener("color", this); //$NON-NLS-1$
        track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
        track.removePropertyChangeListener("data", this); //$NON-NLS-1$
        track.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
        track.addPropertyChangeListener("image", this); //$NON-NLS-1$
        track.addPropertyChangeListener("name", this); //$NON-NLS-1$
        track.addPropertyChangeListener("color", this); //$NON-NLS-1$
        track.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
        track.addPropertyChangeListener("data", this); //$NON-NLS-1$
        // select the track dropdown item
        dropdown.setSelectedItem(item);
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
    return trackViews.get(track);
  }

  /**
   * Gets the name of the view
   *
   * @return the name of this view
   */
  public abstract String getViewName();

  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
    toolbarComponents.clear();
    TrackView trackView = getTrackView(getSelectedTrack());
    if (trackView != null) {
      toolbarComponents.add(trackView.getViewButton());
    }
    if (dropdown.getItemCount() > 0) {
      toolbarComponents.add(dropdown);
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
  public boolean isCustomState() {
  	if (tracks.size()>1) {
  		// custom state if selected track is not the first in trackerPanel
  		for (TTrack track: trackerPanel.getUserTracks()) {
        if (!track.isViewable()) continue;
        if (track!=selectedTrack) return true;
        break;
  		}
  	}
  	for (Iterator<TTrack> it = trackViews.keySet().iterator(); it.hasNext();) {
    	TrackView view = trackViews.get(it.next());
  		if (view.isCustomState()) return true;
  	}
  	return false;
  }

  /**
   * Responds to property change events. This receives the following
   * events: "track", "transform" from trackerPanel; "name", "color", footprint"
   * and "data" from selected track.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("track")) {               // track has been added or removed //$NON-NLS-1$
      TTrack track = (TTrack)e.getOldValue();
      if (track != null) {
        track.removePropertyChangeListener("name", this); //$NON-NLS-1$
        track.removePropertyChangeListener("color", this); //$NON-NLS-1$
        track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
        track.removePropertyChangeListener("data", this); //$NON-NLS-1$
        TrackView view = trackViews.get(track);
        if (view != null) view.dispose();
      }
    	refresh();
    	TFrame frame = trackerPanel.getTFrame();
    	if (frame != null) frame.repaint();
      // select a newly added track
      track = (TTrack)e.getNewValue();
      if (track != null) setSelectedTrack(track);
    }
    else if (name.equals("transform")) {             // coords have changed //$NON-NLS-1$      
    	TTrack track = getSelectedTrack();
      if (track != null && getTrackView(track) != null) {
      	// if track is a particle model, ignore if coords are adjusting
      	if (track instanceof ParticleModel) {
      		ImageCoordSystem coords = trackerPanel.getCoords();
      		if (coords.isAdjusting()) return;
      	}
        TrackView trackView = getTrackView(track);
        trackView.refresh(trackerPanel.getFrameNumber());
      }
    }
    else if (name.equals("data")) {             // data has changed //$NON-NLS-1$
      TTrack track = getSelectedTrack();
      if (track != null && getTrackView(track) != null) {
        TrackView trackView = getTrackView(track);
        trackView.refresh(trackerPanel.getFrameNumber());
      }
    }
    else if (name.equals("function") // DataFunction has changed //$NON-NLS-1$
    		|| name.equals("radian_angles")) { // angle units have changed //$NON-NLS-1$
      // refresh views of all tracks
      for (TTrack track: trackerPanel.getTracks()) {
    		if (getTrackView(track) != null) {
          TrackView trackView = getTrackView(track);
          trackView.refreshGUI();
          trackView.refresh(trackerPanel.getFrameNumber());
        }
    	}
    }
    else if (name.equals("stepnumber") || //$NON-NLS-1$
             name.equals("image")) { // video image has changed //$NON-NLS-1$
      TTrack track = getSelectedTrack();
      if (track != null && getTrackView(track) != null) {
        TrackView trackView = getTrackView(track);
        trackView.refresh(trackerPanel.getFrameNumber());
      }
    }
    else if (name.equals("color") || //$NON-NLS-1$
             name.equals("name") || //$NON-NLS-1$
             name.equals("footprint")) { //$NON-NLS-1$
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
    for (TTrack track: trackerPanel.getTracks()) {
      if (track.getName().equals(name)) return track;
    }
    return null;
  }

}
