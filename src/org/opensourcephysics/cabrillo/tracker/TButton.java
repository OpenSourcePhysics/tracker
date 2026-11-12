/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2026 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;

/**
 * A button based on VideoPlayer.PlayerButton that is used throughout Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TButton extends JButton {

	static final Border EMPTY = BorderFactory.createEmptyBorder(7, 3, 7, 3);
	static final Border ETCHED = BorderFactory.createEtchedBorder();

	/**
	 * A subclass of JMenuItem that causes the menu to stay open after clicked
	 * by bypassing the MenuSelectionManager and informing JSMenuItemUI that 
	 * clicking this item should not close the menu.
	 * 
	 * Used specifically for the [|>] button in TToolBar and TViewChooser
	 */
	static class TStickyMenuItem extends JMenuItem {

		public TStickyMenuItem(String name, Icon icon) {
			super(name, icon);
			// for JavaScript, we are telling JSMenuItemUI that 
			// we need special handling so that the HTML menu is not
			// to be closed upon release
			putClientProperty("_j2sStickyMenuItem", Boolean.TRUE);
		}
		
		@Override
		protected void processMouseEvent(MouseEvent e) {
			// for Java and JavaScript, no need to signal release except to actuate the click
			if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				// Prevent the parent MenuSelectionManager from clearing the menu selection path
				doClick(0); // snappier; no thread delay - doClick() gives 68-ms hold time; MSM uses doClick(0)
			} else {
				super.processMouseEvent(e);
			}
		}		

	}
	
	/**
	 * A button class to handle popups that need to be "sticky" -- responding to short and
	 * long presses differently.
	 * 
	 * specifically for TViewChooser.viewChooserChooseViewButton and
	 * TTrackBar.trackBarChooseViewButton
	 * 
	 * Consolidates short/long press actions in this single subclass. 
	 * Standard TButtons do not need this. 
	 * 
	 * @author hanso
	 *
	 */
	static class TStickyPopupButton extends TButton {
		
		private static final int LONG_PRESS_DURATION = 750; // millisec
		private final static Icon RIGHT_ARROW_ICON = Tracker.getResourceIcon("right_arrow.gif", true); //$NON-NLS-1$; 
		private TFrame frame;
		private Integer panelID;

		TStickyPopupButton(TFrame frame, Integer panelID, String name) {
			super(RIGHT_ARROW_ICON);
			setName(name);
			this.frame = frame;
			this.panelID = panelID;
			alignPopup = TButton.RIGHT;
			setBorder(BorderFactory.createCompoundBorder(ETCHED, EMPTY));
			longPressTimer = new Timer(LONG_PRESS_DURATION, e -> {
				showPopup(null);
				
			});
			longPressTimer.setRepeats(false);
		}
		
		private Timer longPressTimer; 

		@Override
		protected boolean checkLongPress(MouseEvent e) {
			if (e == null) {
				// unnecessary				AIPatch.keepPopupOpenUntilSelection(this, this.popup);
			} else {
				if (popup == null)
					popup = getPopup();
				switch (e.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					if (e.isPopupTrigger()) {
						// this is for MacOS and Linux
						if (longPressTimer.isRunning())
							longPressTimer.stop();
						showPopup(null);
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						longPressTimer.restart();
					}
					break;
				case MouseEvent.MOUSE_RELEASED:
					if (e.isPopupTrigger()) {
						// this is for Windows 
						if (longPressTimer.isRunning())
							longPressTimer.stop();
						showPopup(null);
					} else if (SwingUtilities.isLeftMouseButton(e) && longPressTimer.isRunning()) {
						// timer is still running so this is a short click
						// If the popup is visible, then we just shut it; otherwise we show it.
						longPressTimer.stop();
						if (hidePopup) {
							showPopup(e);
						} else {
							frame.maximizeNextView(frame.getTrackerPanelForID(panelID));
							hidePopup = true;
						}
					}
					break;
				case MouseEvent.MOUSE_CLICKED:
					break;
				}
			}
			return true;
		}
		
		@Override
		protected JPopupMenu getPopup() {
			return (frame.currentMenuBar == null ? null : frame.currentMenuBar.refreshPanesPopup());
		}

	}
	private int trackID;
	protected boolean hidePopup = false;
	protected JPopupMenu popup;
	protected String context = "track"; //$NON-NLS-1$
	protected boolean alwaysShowBorder;
	protected int alignPopup = LEFT;
	protected boolean hideContentWhenExit = false;

	/**
	 * Constructs a TButton.
	 */
	public TButton() {
		setOpaque(false);
		setBorderPainted(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!TButton.this.isEnabled())
					return;
				hidePopup = (popup != null && popup.isVisible());
				setBorderPainted(true);
				setContentAreaFilled(true);
				TTrack track = getTrack();
				if (Tracker.showHints && track != null && track.tp != null) {
					if (track.tp.getSelectedTrack() == track)
						track.tp.setMessage(track.getMessage());
					else {
						String s = track.getClass().getSimpleName() + " " //$NON-NLS-1$
								+ track.getName() + " (" //$NON-NLS-1$
								+ TrackerRes.getString("TTrack.Unselected.Hint") + ")"; //$NON-NLS-1$ //$NON-NLS-2$
						track.tp.setMessage(s);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!alwaysShowBorder) {
					setBorderPainted(false);
					setContentAreaFilled(!hideContentWhenExit);
				}
	}

			@Override
			public void mousePressed(MouseEvent e) {
				checkLongPress(e);
				TTrack track = getTrack();
				if (track != null && track.tp != null && track != track.tp.getSelectedTrack()) {
					track.tp.setSelectedTrack(track);
					track.tp.setSelectedPoint(null);
					track.tp.selectedSteps.clear();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				checkLongPress(e);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled() || checkLongPress(e))
					return;
				showPopup(e);
			}
		});
	}

	protected boolean checkLongPress(MouseEvent e) {
		// TStickyPopupButton only
		return false;
	}

	//  /**
//   * Constructs an icon-only TButton with an AbstractAction.
//   *
//   * @param action the AbstractAction
//   */
//  public TButton(AbstractAction action) {
//  	this();
//  	addActionListener(action);
//  	setIcon((Icon)action.getValue(Action.SMALL_ICON));
//  }  
//  
	/**
	 * Constructs a TButton with a TTrack.
	 *
	 * @param track the track
	 */
	public TButton(TTrack track) {
		this();
		setTrack(track);
	}

	/**
	 * Constructs a TButton with an Icon.
	 *
	 * @param icon the icon
	 */
	public TButton(Icon icon) {
		this();
		setIcon(icon);
	}

	/**
	 * Constructs a TButton with icons for selected and unselected states.
	 *
	 * @param off the unselected state icon
	 * @param on  the selected state icon
	 */
	public TButton(Icon off, Icon on) {
		this();
		setIcons(off, on);
	}

	/**
	 * Sets the icons for selected and unselected states.
	 *
	 * @param off the unselected state icon
	 * @param on  the selected state icon
	 */
	public void setIcons(Icon off, Icon on) {
		super.setIcon(off);
		setSelectedIcon(on);
	}

	/**
	 * Sets the track associated with this button.
	 *
	 * @param track the track
	 */
	public void setTrack(TTrack track) {
		if (track == null) {
			trackID = -1;
			setIcon(null);
			setText(" "); //$NON-NLS-1$
			setToolTipText(null);
		} else {
			trackID = track.getID();
			setIcon(track.getIcon(21, 16, context));
			setText(track.getName(context));
			setToolTipText(TrackerRes.getString("TButton.Track.ToolTip") //$NON-NLS-1$
					+ " " + track.getName(context)); //$NON-NLS-1$
			FontSizer.setFont(this);
		}
	}

	/**
	 * Gets the track associated with this button.
	 * 
	 * @return the track
	 */
	public TTrack getTrack() {
		return TTrack.getTrack(trackID);
	}

	/**
	 * Gets the TMenuBar's trackMenu popup menu to display. If a track is associated
	 * with this button, the track menu is returned, but subclasses can override
	 * this method to return any popup menu.
	 *
	 * @return the popup menu, or null if none
	 */
	protected JPopupMenu getPopup() {
		TTrack track = getTrack();
		if (track != null && track.tp != null) {
			JMenu trackMenu = track.getMenu(track.tp, new JMenu());
			FontSizer.setFonts(trackMenu, FontSizer.getLevel());
			return trackMenu.getPopupMenu();
		}
		return null;
	}

	/**
	 * Shows the popup menu, if any.
	 * 
	 * @param e a MouseEvent, or null
	 */
	protected void showPopup(MouseEvent e) {
		if (e == null) {
			hidePopup = false;
		}
		if (popup == null)
			popup = getPopup();
		if (popup != null) {
			if (e != null && e.getClickCount() == 2) {
				hidePopup = false;
			}
			if (hidePopup) {
				hidePopup = false;
				popup.setVisible(false);
			} else {
				hidePopup = true;
				int popupWidth = popup.getPreferredSize().width;
				int offset = alignPopup == LEFT ? 0 : getWidth() - popupWidth;
				popup.show(TButton.this, offset, getHeight());
				if (OSPRuntime.isJS && alignPopup == RIGHT) {
					GUIUtils.alignComponentRight(popup, TButton.this);
				}
				if (OSPRuntime.isJS)
					checkLongPress(null);
			}
		}
	}

	protected void alwaysShowBorder(boolean showBorder) {
		alwaysShowBorder = showBorder;
		setOpaque(alwaysShowBorder);
		setBorderPainted(alwaysShowBorder);
	}

}
