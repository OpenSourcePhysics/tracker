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

import java.awt.event.*;

import javax.swing.*;

import org.opensourcephysics.tools.FontSizer;

/**
 * A button based on VideoPlayer.PlayerButton that is used throughout Tracker.
 *
 * @author Douglas Brown
 */
public class TButton extends JButton {
	
	protected TTrack track;
  private boolean hidePopup = false;
  private JPopupMenu popup;
	private Icon onIcon, offIcon;

  /**
   * Constructs a TButton.
   */
  public TButton() {
		setOpaque(false);
		setBorderPainted(false);
    addMouseListener(new MouseAdapter() {
    	public void mouseEntered(MouseEvent e) {
    		setBorderPainted(true);
        hidePopup = popup!=null && popup.isVisible();
      	if (Tracker.showHints && track!=null && track.trackerPanel!=null) {
        	if (track.trackerPanel.getSelectedTrack() == track)
        		track.trackerPanel.setMessage(track.getMessage());
        	else {
          	String s = track.getClass().getSimpleName()+" " //$NON-NLS-1$
              	+ track.getName() + " (" //$NON-NLS-1$
                + TrackerRes.getString("TTrack.Unselected.Hint")+")"; //$NON-NLS-1$ //$NON-NLS-2$
          	track.trackerPanel.setMessage(s);
        	}
      	}    		
    	}

    	public void mouseExited(MouseEvent e) {
    		setBorderPainted(false);
    	}

    	public void mousePressed(MouseEvent e) {
    		if (track!=null && track.trackerPanel!=null
    				&& track != track.trackerPanel.getSelectedTrack()) {
        	track.trackerPanel.setSelectedTrack(track);
        	track.trackerPanel.setSelectedPoint(null);
        	hidePopup = true;
        }
    	}

    	public void mouseClicked(MouseEvent e) {
    		popup = getPopup();
    		if (popup!=null) {
    			if (e.getClickCount()==2)
    				hidePopup = false;
	        if (hidePopup) {
	          hidePopup = false;
	          popup.setVisible(false);
	        }
	        else {
	          hidePopup = true;
	          popup.show(TButton.this, 0, TButton.this.getHeight());
	        }
    		}
    	}
    });
  }
  
  /**
   * Constructs an icon-only TButton with an AbstractAction.
   *
   * @param action the AbstractAction
   */
  public TButton(AbstractAction action) {
  	this();
  	addActionListener(action);
  	setIcon((Icon)action.getValue(Action.SMALL_ICON));
  }  
  
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
   * Sets the icon. Overrides JButton method.
   *
   * @param icon the icon
   */
  public void setIcon(Icon icon) {
    super.setIcon(icon);
    setSelectedIcon(icon);
//    setRolloverSelectedIcon(icon); 
  }
  
  /**
   * Sets the icons for selected and unselected states.
   *
   * @param off the unselected state icon
   * @param on the selected state icon
   */
  public void setIcons(Icon off, Icon on) {
		onIcon = on;
		offIcon = off;
		setIcon(off);
  }
  
  /**
   * Sets the selected state. Overrides JButton method.
   *
   * @param selected true to select
   */
  public void setSelected(boolean selected) {
  	super.setSelected(selected);
  	if (selected && onIcon !=null) {
  		setIcon(onIcon);
  	}
  	else if (!selected && offIcon !=null) {
  		setIcon(offIcon);
  	}
  }
    	
  /**
   * Sets the track associated with this button.
   *
   * @param track the track
   */
  public void setTrack(TTrack track) {
  	this.track = track;
  	if (track!=null) {
		  setIcon(track.getFootprint().getIcon(21, 16));
		  setText(track.getName()); 
		  setToolTipText(TrackerRes.getString("TButton.Track.ToolTip") //$NON-NLS-1$
		  		+" "+track.getName());  //$NON-NLS-1$
  	}
  	else {
		  setIcon(null);
		  setText(" "); //$NON-NLS-1$
		  setToolTipText(null);
  	}
  }
  
  /**
   * Gets the track associated with this button.
   * @return the track
   */
  public TTrack getTrack() {
  	return track;
  }
  
  /**
   * Gets a popup menu to display. If a track is associated with this button,
   * the track menu is returned, but subclasses can override this method
   * to return any popup menu.
   *
   * @return the popup menu, or null if none
   */
  protected JPopupMenu getPopup() {
  	if (track!=null && track.trackerPanel!=null) {
    	JMenu trackMenu = track.getMenu(track.trackerPanel);
    	FontSizer.setFonts(trackMenu, FontSizer.getLevel());
  		return trackMenu.getPopupMenu();
  	}
  	return null;
  }

}
