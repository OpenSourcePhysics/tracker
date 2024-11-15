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
 * <https://opensourcephysics.github.io/tracker/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

/**
 * This is the main video view for Tracker. 
 * It draws the TrackerPanel in imageSpace and
 * puts the video player in a detachable toolbar.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class MainTView extends ZoomTView {

	// instance fields
	private JToolBar playerBar;
	
	/**
	 * Constructs a main view of a tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	public MainTView(TrackerPanel panel) {
		super(panel);
		// add trackbar north		
		TTrackBar tbar = panel.getTrackBar(true);
		if (tbar != null)
			add(tbar, BorderLayout.NORTH);
		// add player in playerBar
		playerBar = new JToolBar();
		add(playerBar, BorderLayout.SOUTH);
		playerBar.setFloatable(false);
		panel.getPlayer().setBorder(null);
		panel.setPlayerVisible(false);
		playerBar.add(panel.getPlayer());
	}

	@Override
	protected boolean doResized() {
		if (!super.doResized())
			return false;
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		TToolBar tbar = trackerPanel.getToolBar(false);
		if (tbar != null)
			tbar.refreshZoomButton();
		trackerPanel.eraseAll();
		return true;
	}

	@Override
	JPopupMenu getPopupMenu() {
		//OSPLog.debug("MainTView.getPopupMenu " + Tracker.allowMenuRefresh);
		if (!Tracker.allowMenuRefresh)
			return null;
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (Tracker.isZoomInCursor(trackerPanel.getCursor()) || Tracker.isZoomOutCursor(trackerPanel.getCursor())) {
			return null;
		}
		return trackerPanel.updateMainPopup();
	}

	/**
	 * Gets the toolbar containing the player.
	 *
	 * @return the player toolbar
	 */
	public JToolBar getPlayerBar() {
		return playerBar;
	}

	@Override
	public void dispose() {
		super.dispose();
		// dispose of floating player, if any
		// note main view not finalized when player is floating
		Container frame = playerBar.getTopLevelAncestor();
		if (frame instanceof JDialog) {
			frame.removeAll();
			((JDialog) frame).dispose();
		}
		playerBar.removeAll();
		playerBar = null;
	}

	@Override
	public String getViewName() {
		return TrackerRes.getString("TFrame.View.Video"); //$NON-NLS-1$
	}

	@Override
	public Icon getViewIcon() {
		return Tracker.getResourceIcon("video_on.gif", true); //$NON-NLS-1$
	}

	@Override
	public int getViewType() {
		return TView.VIEW_MAIN;
	}
	

}
