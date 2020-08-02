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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a dialog containing buttons for all user tracks.
 *
 * @author Douglas Brown
 */
public class TrackControl extends JDialog
    implements PropertyChangeListener {

  // static fields
  protected static Map<TrackerPanel, TrackControl> controls = new HashMap<TrackerPanel, TrackControl>();

  // instance fields
  protected JPopupMenu popup;
  protected TrackerPanel trackerPanel;
  protected JPanel trackBarPanel;
  protected JToolBar[] trackBars = new JToolBar[0];
  protected boolean positioned = false;
  protected int trackCount;
  protected boolean isVisible;
  protected KeyListener shiftKeyListener;
  
  /**
   * Gets the track control for the specified tracker panel.
   *
   * @param panel the tracker panel to control
   * @return the track control
   */
  public static synchronized TrackControl getControl(TrackerPanel panel) {
    TrackControl control = controls.get(panel);
    if (control == null) {
      control = new TrackControl(panel);
      controls.put(panel, control);
      panel.trackControl = control;
      
    }
    return control;
  }

  /**
   * Private constructor.
   *
   * @param panel the tracker panel to control
   */
  private TrackControl(TrackerPanel panel) {
    super(panel.getTFrame(), false);
    // create GUI
    trackBarPanel = new JPanel();
    setContentPane(trackBarPanel);
    shiftKeyListener = new KeyAdapter() {
    	// transfers focus to trackerPanel for marking
      @Override
	public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
        	trackerPanel.requestFocus();
        	trackerPanel.requestFocusInWindow();
        }
        else if (e.getKeyCode() == KeyEvent.VK_A) {
        	MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
        	mainView.keyAdapter.keyPressed(e);
        	trackerPanel.requestFocus();
        	trackerPanel.requestFocusInWindow();
        }
      }
    };
    setResizable(false);
    pack();
    popup = new JPopupMenu();
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this); //$NON-NLS-1$
//    trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
    TFrame frame = trackerPanel.getTFrame();
	frame.addFollower(this, null);
  }

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
    dim.width = Math.max(150, dim.width);
    return dim;
	}
	
	@Override
	public void setVisible(boolean vis) {
		if (trackerPanel == null)
			return;
		if (!positioned && vis) {
			TFrame frame = trackerPanel.getTFrame();
			if (!frame.isVisible())
				return;
			Point p = frame.getLocationOnScreen();
			setLocation(p.x + frame.getWidth() / 2 - getWidth() / 2, p.y + 90);
			positioned = true;
		}
		if (vis && trackCount == 0 && !isEmpty())
			refresh();
		super.setVisible(vis);
		isVisible = vis;
		TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
		toolbar.trackControlButton.setSelected(vis);
	}
	
	/**
	 * Responds to property change events from TrackerPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		
		String name = e.getPropertyName();
		switch (name) {
		case TFrame.PROPERTY_TFRAME_TAB :
			if (e.getNewValue() == trackerPanel) {
				setVisible(isVisible);
			} else {
				boolean vis = isVisible;
				setVisible(false);
				isVisible = vis;
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) {
			// track has been deleted, so remove all listeners from it
			TTrack track = (TTrack) e.getOldValue();
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:																							// //$NON-NLS-1$
			for (Integer n : TTrack.activeTracks.keySet()) {
				TTrack track = TTrack.activeTracks.get(n);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
			}
			return;
		}
		refresh();
	}

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }

  /**
   * Disposes of this track control.
   */
  @Override
public void dispose() {
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this); //$NON-NLS-1$
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); //$NON-NLS-1$
      }
      controls.remove(trackerPanel);
      trackerPanel.trackControl = null;
      trackerPanel = null;
      for (Integer n: TTrack.activeTracks.keySet()) {
      	TTrack track = TTrack.activeTracks.get(n);
	      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); //$NON-NLS-1$
	      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
	      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); //$NON-NLS-1$
      }
    }
    super.dispose();
  }

  /**
   * Return true if this has no track buttons.
   *
   * @return true if empty
   */
  public boolean isEmpty() {
    if (trackCount>0) return false;
    ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
    return tracks.isEmpty();
  }

  /**
   * Refreshes buttons and vectors.
   */
  protected void refresh() {
  	if (trackerPanel==null) return;
    setTitle(TrackerRes.getString("TrackControl.Name")); //$NON-NLS-1$
    int perbar = 4;
    ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
    for (int i = 0; i < trackBars.length; i++) {
      trackBars[i].removeAll();
    }
    int barCount = (tracks.size()+perbar-1)/perbar;
    trackBarPanel.removeAll();
		trackBarPanel.setLayout(new GridLayout(barCount, 1));
    if (barCount > trackBars.length) {
      JToolBar[] newBars = new JToolBar[barCount];
      System.arraycopy(trackBars, 0, newBars, 0, trackBars.length);
      for (int i = trackBars.length; i < barCount; i++) {
        newBars[i] = new JToolBar();
        newBars[i].setFloatable(false);
      }
      trackBars = newBars;
    }
    for (int i = 0; i < barCount; i++) {
    	trackBarPanel.add(trackBars[i]);
    }
    // add listeners to all tracks and count the mass tracks
    trackCount = 0;
    TTrack track = null;
    Iterator<TTrack> it = tracks.iterator();
    while (it.hasNext()) {
    	int barIndex = trackCount/perbar;
      track = it.next();
      // listen to tracks for property changes that affect icon or name
      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); //$NON-NLS-1$
      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
      track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); //$NON-NLS-1$
      track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); //$NON-NLS-1$
      track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
      track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); //$NON-NLS-1$
      // make the track buttons
      TButton button = new TButton(track);
      button.addKeyListener(shiftKeyListener);
      trackBars[barIndex].add(button);
      trackCount++;
    }
    FontSizer.setFonts(this);
    pack();
   TFrame.repaintT(this);
//   setVisible(isVisible && trackCount > 0);
//   if (trackCount == 0)
//	   isVisible = true;
   if (trackCount==0)
    	setVisible(false);
    TFrame frame = trackerPanel.getTFrame();
    if (frame != null) {
      frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); //$NON-NLS-1$
      frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); //$NON-NLS-1$
    }
  }

}

