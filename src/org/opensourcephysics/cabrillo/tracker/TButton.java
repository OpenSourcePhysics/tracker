/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.event.*;

import javax.swing.*;

import org.opensourcephysics.tools.FontSizer;

/**
 * A button based on VideoPlayer.PlayerButton that is used throughout Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TButton extends JButton {
	
  private int trackID;
  private boolean hidePopup = false;
  private JPopupMenu popup;
  protected String context = "track"; //$NON-NLS-1$
  protected boolean alwaysShowBorder;

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
        hidePopup = popup!=null && popup.isVisible();
        TTrack track = getTrack();
      	if (Tracker.showHints && track!=null && track.tp!=null) {
        	if (track.tp.getSelectedTrack() == track)
        		track.tp.setMessage(track.getMessage());
        	else {
          	String s = track.getClass().getSimpleName()+" " //$NON-NLS-1$
              	+ track.getName() + " (" //$NON-NLS-1$
                + TrackerRes.getString("TTrack.Unselected.Hint")+")"; //$NON-NLS-1$ //$NON-NLS-2$
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
        TTrack track = getTrack();
    		if (track!=null && track.tp!=null
    				&& track != track.tp.getSelectedTrack()) {
    			track.tp.setSelectedTrack(track);
    			track.tp.setSelectedPoint(null);
          track.tp.selectedSteps.clear();
//        	hidePopup = true;
        }
    	}

			@Override
			public void mouseClicked(MouseEvent e) {
    		if (!TButton.this.isEnabled()) 
    			return;
				popup = getPopup();
				if (popup != null) {
					if (e.getClickCount() == 2)
						hidePopup = false;
					if (hidePopup) {
						hidePopup = false;
						popup.setVisible(false);
					} else {
						hidePopup = true;
						popup.show(TButton.this, 0, TButton.this.getHeight());
					}
				}
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
   * @param on the selected state icon
   */
  public TButton(Icon off, Icon on) {
		this();
		setIcons(off, on);
  }
  
  /**
   * Sets the icons for selected and unselected states.
   *
   * @param off the unselected state icon
   * @param on the selected state icon
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
   * @return the track
   */
  public TTrack getTrack() {
  	return TTrack.getTrack(trackID);
  }
  
	/**
	 * Gets the TMenuBar's trackMenu popup menu to display. If a track is associated with this button, the
	 * track menu is returned, but subclasses can override this method to return any
	 * popup menu.
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
	
	protected void showPopup() {
		popup = getPopup();
		if (popup != null) {
			hidePopup = true;
			popup.show(TButton.this, 0, TButton.this.getHeight());
		}
		
	}

	protected void alwaysShowBorder(boolean showBorder) {
		alwaysShowBorder = showBorder;		
		setOpaque(alwaysShowBorder);
		setBorderPainted(alwaysShowBorder);
	}

}
