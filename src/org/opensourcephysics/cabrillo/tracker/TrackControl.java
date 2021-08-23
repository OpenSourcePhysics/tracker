/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a dialog containing buttons for all user tracks.
 *
 * @author Douglas Brown
 */
public class TrackControl extends JDialog implements OSPRuntime.Disposable, PropertyChangeListener {

	private static final String[] panelProps = { TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
			TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, TTrack.PROPERTY_TTRACK_MASS, TTrack.PROPERTY_TTRACK_FOOTPRINT,
			TTrack.PROPERTY_TTRACK_DATA, };

// static fields
	protected static Map<Integer, TrackControl> panelTrackcontrols = new HashMap<>();

	// instance fields

	protected TFrame frame;
	protected Integer panelID;

	protected JPopupMenu popup;
	protected JPanel trackBarPanel;
	protected JToolBar[] trackBars = new JToolBar[0];
	protected boolean positioned = false;
	protected int trackCount;
	protected boolean wasVisible;
	protected KeyListener shiftKeyListener;
	protected TButton newTrackButton;

	private ComponentListener myFollower;

	/**
	 * Gets the track control for the specified tracker panel.
	 *
	 * @param panel the tracker panel to control
	 * @return the track control
	 */
	public static synchronized TrackControl getControl(TrackerPanel panel) {
		TrackControl control = panelTrackcontrols.get(panel.getID());
		if (control == null) {
			control = new TrackControl(panel);
			panelTrackcontrols.put(panel.getID(), control);
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
		panelID = panel.getID();
		frame = panel.getTFrame();
			
		// create GUI
		trackBarPanel = new JPanel();
		setContentPane(trackBarPanel);
		shiftKeyListener = new KeyAdapter() {
			// transfers focus to trackerPanel for marking
			@Override
			public void keyPressed(KeyEvent e) {
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					trackerPanel.requestFocus();
					trackerPanel.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
					mainView.keyAdapter.keyPressed(e);
					trackerPanel.requestFocus();
					trackerPanel.requestFocusInWindow();
				}
			}
		};
		newTrackButton = new TButton() {

			@Override
			protected JPopupMenu getPopup() {
				TMenuBar.refreshPopup(panel, TMenuBar.POPUPMENU_TRACKCONTROL_TRACKS, popup);
				return popup;
			}
		};
		setResizable(false);
		pack();
		popup = new JPopupMenu();
		panel.addListeners(panelProps, this);
		myFollower = frame.addFollower(this, null);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = Math.max(150, dim.width);
		return dim;
	}

	@Override
	public void setVisible(boolean vis) {
		if (panelID == null)
			return;
		if (!positioned && vis) {
			positionForFrame();
		}
		if (vis && trackCount == 0 && !isEmpty())
			refresh();
		super.setVisible(vis);
		wasVisible = vis;
		TToolBar toolbar = frame.getToolBar(panelID, false);
		if (toolbar != null)
			toolbar.trackControlButton.setSelected(vis);
	}

	private void positionForFrame() {
		if (positioned)
			return;
		if (!frame.isVisible())
			return;
		Point p = frame.getLocationOnScreen();
		setLocation(p.x + frame.getWidth() / 2 - getWidth() / 2, p.y + 90);
		positioned = true;
	}

	/**
	 * Responds to property change events from TrackerPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TFrame.PROPERTY_TFRAME_TAB:
			TrackerPanel p = (TrackerPanel) e.getNewValue();
			if (p == null)
				return;
			if (p.getID() == panelID) {
				setVisible(wasVisible);
			} else {
				boolean vis = wasVisible;
				setVisible(false);
				wasVisible = vis;
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) {
				// track has been deleted, so remove all listeners from it
				((TTrack) e.getOldValue()).removeListenerNCF(this);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR: // //$NON-NLS-1$
			for (Integer n : TTrack.panelActiveTracks.keySet()) {
				TTrack.panelActiveTracks.get(n).removeListenerNCF(this);
			}
			return;
		}
		refresh();
	}

	/**
	 * Return true if this has no track buttons.
	 *
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return false;
//    if (trackCount>0) return false;
//    ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
//    return tracks.isEmpty();
	}

	/**
	 * Refreshes buttons and vectors.
	 */
	protected void refresh() {
		if (panelID == null)
			return;
		setTitle(TrackerRes.getString("TrackControl.Name")); //$NON-NLS-1$
		if (TToolBar.pointmassOffIcon instanceof ResizableIcon) {
			ResizableIcon icon = (ResizableIcon) TToolBar.pointmassOffIcon;
			if (icon.getBaseIcon() instanceof ImageIcon) {
				ImageIcon imgIcon = (ImageIcon) icon.getBaseIcon();
				setIconImage(imgIcon.getImage());
			}
			;
		}

		int perbar = 4;
		ArrayList<TTrack> tracks = frame.getTrackerPanelForID(panelID).getUserTracks();
		for (int i = 0; i < trackBars.length; i++) {
			trackBars[i].removeAll();
		}
		int barCount = 1 + tracks.size() / perbar;
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

		// add new track button first
		newTrackButton.setText(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
		newTrackButton.setToolTipText(TrackerRes.getString("TrackControl.Button.NewTrack.ToolTip")); //$NON-NLS-1$
		FontSizer.setFont(newTrackButton);
		trackBars[0].add(newTrackButton);

		// add listeners to all tracks and count the mass tracks
		trackCount = 0;
		TTrack track = null;
		Iterator<TTrack> it = tracks.iterator();
		while (it.hasNext()) {
			int barIndex = (trackCount + 1) / perbar;
			track = it.next();
			// listen to tracks for property changes that affect icon or name
			track.removeListenerNCF(this);
			track.addListenerNCF(this);
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
//   if (trackCount==0)
//    	setVisible(false);
		if (frame != null) {
			frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
		}
	}

	/**
	 * Disposes of this track control.
	 */
	@Override
	public void dispose() {
		System.out.println("TrackControl.dispose " + panelID);
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.removeListeners(panelProps, this);
			if (frame != null) {
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
				frame.removeComponentListener(myFollower);
				myFollower = null;
			}
			panelTrackcontrols.remove(panelID);
			trackerPanel.trackControl = null;
			ArrayList<TTrack> tracks = trackerPanel.getTracks();
			for (int i = tracks.size(); --i >= 0;) { // : TTrack.activeTracks.keySet()) {
				tracks.get(i).removeListenerNCF(this);
			}
			trackerPanel = null;
		}
		super.dispose();
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}

}
