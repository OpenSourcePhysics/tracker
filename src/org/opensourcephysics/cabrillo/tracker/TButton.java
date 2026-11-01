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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;

/**
 * A button based on VideoPlayer.PlayerButton that is used throughout Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TButton extends JButton {

  private static final int LONG_PRESS_DURATION = 750; // millisec
	private int trackID;
	private boolean hidePopup = false;
	private JPopupMenu popup;
	protected String context = "track"; //$NON-NLS-1$
	protected boolean alwaysShowBorder;
	protected int alignPopup = LEFT;
	protected boolean keepPopupOpenOnRelease;
	private Timer longPressTimer;
	private Runnable shortClick, longPress;

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
				setBorderPainted(true);
				hidePopup = popup != null && popup.isVisible();
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
				if (!alwaysShowBorder)
					setBorderPainted(false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (longPress != null || shortClick != null) {
	        longPressTimer.restart();
				}

				TTrack track = getTrack();
				if (track != null && track.tp != null && track != track.tp.getSelectedTrack()) {
					track.tp.setSelectedTrack(track);
					track.tp.setSelectedPoint(null);
					track.tp.selectedSteps.clear();
//        	hidePopup = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (shortClick != null 
					&& SwingUtilities.isLeftMouseButton(e) 
					&& longPressTimer.isRunning()) {
              // timer is still running so this is a short click
              longPressTimer.stop();
              shortClick.run();
        }
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (!TButton.this.isEnabled())
					return;
				if (shortClick != null || longPress != null)
					return;
				showPopup(e);
			}
		});
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
	 * Align a component's right edge with the reference component 
	 * or within the visible viewport.
	 */
	private void alignComponentRight(JComponent c, JComponent ref) {
		if (OSPRuntime.jsutil != null)
			OSPRuntime.jsutil.alignComponentRight(c, ref, 0);
		// SwingJS menus are rendered outside the frame. Constrain the actual
		// DOM bounds because Swing popup sizes can differ from the rendered size.
		/** @j2sNative

var owner=javax.swing.SwingUtilities.getWindowAncestor$java_awt_Component(ref);
if (owner != null) {
var node=c.秘getUI$().domNode;
var bounds=owner.秘getUI$().domNode.getBoundingClientRect();
var rect=node.getBoundingClientRect();
var x=Math.max(bounds.left, Math.min(rect.left, bounds.right - rect.width));
var y=Math.max(bounds.top, Math.min(rect.top, bounds.bottom - rect.height));
node.style.left=((parseFloat(node.style.left) || 0) + x - rect.left) + "px";
node.style.top=((parseFloat(node.style.top) || 0) + y - rect.top) + "px";
}
		 */
		{}
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
		popup = getPopup();
		if (popup != null) {
			if (e != null && e.getClickCount() == 2)
				hidePopup = false;
			if (hidePopup) {
				hidePopup = false;
				popup.setVisible(false);
			} else {
				hidePopup = true;
				int popupWidth = popup.getPreferredSize().width;
				int offset = alignPopup == LEFT ? 0 : getWidth() - popupWidth;
				popup.show(TButton.this, offset, getHeight());
				if (OSPRuntime.isJS && alignPopup == RIGHT) {
					alignComponentRight(popup, TButton.this);
				}
				if (OSPRuntime.isJS && keepPopupOpenOnRelease)
					keepPopupOpenUntilSelection();
			}
		}
	}



	/** Keeps the view list open through the release of its opening long press. */
	private void keepPopupOpenUntilSelection() {
		/** @j2sNative
var popup = this.popup;
var node = popup.秘getUI$().domNode;
var button = this;
if (popup._trackerPopupCleanup) popup._trackerPopupCleanup();
var widget = popup.$ulTop && popup.$ulTop.data("ui-j2smenu");
var collapse = widget && widget.collapseAll;
// This list closes on selection or an outside press, not a hover/blur timer.
if (widget) widget.collapseAll = function() {};
// Window activation can schedule a global menu close after this list opens.
// Dismiss this list only through its explicit selection/outside-press handlers.
var popupUI = swingjs.plaf.JSPopupMenuUI;
var closeMenusNow = popupUI.closeAllMenusNOW$;
var keepViewListOpen = function() {};
popupUI.closeAllMenusNOW$ = keepViewListOpen;
var openingRelease = true;
var observer;
var cleanup = function() {
 document.removeEventListener("pointerdown", press, true);
 document.removeEventListener("mousedown", press, true);
 document.removeEventListener("touchstart", press, true);
 document.removeEventListener("pointerup", release, true);
 document.removeEventListener("mouseup", release, true);
 document.removeEventListener("touchend", release, true);
 document.removeEventListener("click", click, true);
 document.removeEventListener("keydown", key, true);
 if (observer) observer.disconnect();
 for (var i = 0; i < items.length; i++)
  if (items[i].removeActionListener$java_awt_event_ActionListener)
   items[i].removeActionListener$java_awt_event_ActionListener(itemListener);
 if (widget) widget.collapseAll = collapse;
 if (popupUI.closeAllMenusNOW$ === keepViewListOpen)
  popupUI.closeAllMenusNOW$ = closeMenusNow;
 delete popup._trackerPopupCleanup;
};
var close = function() {
 cleanup();
 button.hidePopup = false;
 popup.setVisible$Z(false);
 // Swing's visibility flag can already be false after touch activation.
 // Force the native menu to hide as well.
 if (J2S.Swing && J2S.Swing.hideMenu) J2S.Swing.hideMenu(popup, true);
};
var press = function(e) {
 openingRelease = false;
 if (!node.contains(e.target)) close();
};
var release = function(e) {
 if (!openingRelease) return;
 // Consume only the release of the press that opened this list.
 // Disarm first so releasing the Swing button cannot fire its normal action.
 var model = button.getModel$();
 model.setArmed$Z(false);
 model.setPressed$Z(false);
 if (e.cancelable) e.preventDefault();
 e.stopImmediatePropagation();
};
var click = function(e) {
 if (openingRelease) {
  e.stopImmediatePropagation();
  return;
 }
 var item = e.target.closest(".ui-j2smenu-item");
 if (item && node.contains(item) && !item.closest(".ui-state-disabled")) {
  // Let the menu item perform its action before dismissing the list.
  setTimeout(close, 0);
 }
};
var key = function(e) { if (e.key === "Escape") close(); };
// Touch menu items can fire their Swing action without a DOM click.
var items = popup.getComponents$();
var itemListener = {
 actionPerformed$java_awt_event_ActionEvent: function() { close(); },
 equals$O: function(other) { return this === other; }
};
for (var i = 0; i < items.length; i++)
 if (items[i].addActionListener$java_awt_event_ActionListener)
  items[i].addActionListener$java_awt_event_ActionListener(itemListener);
popup._trackerPopupCleanup = cleanup;
document.addEventListener("pointerdown", press, true);
document.addEventListener("mousedown", press, true);
document.addEventListener("touchstart", press, true);
document.addEventListener("pointerup", release, true);
document.addEventListener("mouseup", release, true);
document.addEventListener("touchend", release, true);
document.addEventListener("click", click, true);
document.addEventListener("keydown", key, true);
observer = new MutationObserver(function() {
 if (!node.isConnected || node.style.display === "none") cleanup();
});
observer.observe(node, {attributes:true, attributeFilter:["style"]});
		 */
		{}
	}

	protected void alwaysShowBorder(boolean showBorder) {
		alwaysShowBorder = showBorder;
		setOpaque(alwaysShowBorder);
		setBorderPainted(alwaysShowBorder);
	}
	
	protected void setRunnablesForShortAndLongPress(Runnable shortc, Runnable longp) {
		shortClick = shortc;
		longPress = longp;
		if ((shortClick != null || longPress != null) && longPressTimer == null) {
			longPressTimer = new Timer(LONG_PRESS_DURATION, e -> {
        if (longPress != null)
        	longPress.run();
	    });
	    longPressTimer.setRepeats(false);
		}
	}

}
