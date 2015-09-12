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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

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
  private TrackControl(final TrackerPanel panel) {
    super(panel.getTFrame(), false);
    // create GUI
    trackBarPanel = new JPanel();
    setContentPane(trackBarPanel);
    shiftKeyListener = new KeyAdapter() {
    	// transfers focus to trackerPanel for marking
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
        	trackerPanel.requestFocus();
        	trackerPanel.requestFocusInWindow();
        }
      }
    };
    setResizable(false);
    pack();
    popup = new JPopupMenu();
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("mass", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("data", this); //$NON-NLS-1$
  }

	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
    dim.width = Math.max(150, dim.width);
    return dim;
	}
	
	public void setVisible(boolean vis) {
		TFrame frame = trackerPanel.getTFrame();
		if (!positioned && vis) {
			if (frame.isVisible()) {
				MainTView view = frame.getMainView(trackerPanel);
				Point p = view.getLocationOnScreen();
		    setLocation(p.x, p.y);
		    positioned = true;
			}
			else return;
		}
  	if (vis && trackCount==0 && !isEmpty())
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
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
      if (e.getNewValue() == trackerPanel) {
        setVisible(isVisible);
      }
      else {
        boolean vis = isVisible;
        setVisible(false);
        isVisible = vis;
      }
    }
    refresh();
  }

  /**
   * Disposes of this dialog.
   */
  public void dispose() {
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("mass", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("data", this); //$NON-NLS-1$
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
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
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.addPropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
      track.addPropertyChangeListener("color", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      track.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
      // make the track buttons
      TButton button = new TButton(track);
      button.addKeyListener(shiftKeyListener);
      trackBars[barIndex].add(button);
      trackCount++;
    }
  	FontSizer.setFonts(this, FontSizer.getLevel());
    pack();
    repaint();
    if (trackCount==0)
    	setVisible(false);
    TFrame frame = trackerPanel.getTFrame();
    if (frame != null) {
      frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
    }
  }

}

