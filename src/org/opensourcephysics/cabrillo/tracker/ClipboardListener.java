/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2018  Douglas Brown
 * 
 * Tracker is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tracker is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tracker; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston MA 02111-1307 USA or view the license online at
 * <http://www.gnu.org/copyleft/gpl.html>
 * 
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.tools.DataTool;

/**
 * A class to paste data into a TrackerPanel automatically whenever 
 * delimited text data is copied to the clipboard by an external application.
 * Based on code written by Marc Weber, 2005.
 *
 * @author Douglas Brown
 */
class ClipboardListener extends Thread implements ClipboardOwner {
	
  private Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
  private TFrame frame;
  private boolean running = true;
  private TrackerPanel targetPanel;
  
  /**
   * Constructor
   * 
   * @param frame a TFrame.
   */
  public ClipboardListener(TFrame frame) {
  	super();
  	this.frame = frame;
  }
  
  @Override
  public void start() {
  	super.start();
		Transferable contents = sysClip.getContents(this);
		processContents(contents);
  }
  
  @Override
  public void run() {
    Transferable trans = sysClip.getContents(this);
    takeOwnership(trans);
    while(running) {
      try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
    }
  }
  
  @Override
  public void lostOwnership(Clipboard c, Transferable t) {
  	if (!running) return;
  	boolean success = false;
  	while (!success) {
	    try {
	      Thread.sleep(200);
	    } catch(Exception e) {
	    }    
	    try {
				Transferable contents = sysClip.getContents(this);
				processContents(contents);
				success = true;
				takeOwnership(contents);
			} catch (Exception e) {
			}
  	}
  }
  
  /**
   * Immediately processes the clipboard contents, targeting a specified TrackerPanel.
   * 
   * @param target the target TrackerPanel
   */
  public void processContents(TrackerPanel target) {
  	targetPanel = target;
		Transferable contents = sysClip.getContents(this);
		processContents(contents);
  }  
  
  /**
   * Processes the Transferable contents.
   * 
   * @param t a Transferable.
   */
  public void processContents(Transferable t) {
  	// if Tracker itself copied the data, ignore it
  	if (TrackerIO.dataCopiedToClipboard) {
  		TrackerIO.dataCopiedToClipboard = false;
  		return;
  	}
  	// if no String data on the clipboard, return
    if (t==null || !t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
    	return;
    }
  	try {
			String dataString = (String)t.getTransferData(DataFlavor.stringFlavor);
			if (dataString!=null) {
				TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
				if (targetPanel!=null) {
					trackerPanel = targetPanel;
					targetPanel = null;
				}
				if (trackerPanel==null) return;
				DataTrack dt = ParticleDataTrack.getTrackForDataString(dataString, trackerPanel);
				// if track exists with the same data string, return
				if (dt!=null) {
					// clipboard data has already been pasted
					return;
				}
				// parse the data and find data track
				DatasetManager data = DataTool.parseData(dataString, null);
				if (data!=null) {
					String dataName = data.getName().replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$;
					boolean foundMatch = false;
					ArrayList<DataTrack> dataTracks = trackerPanel.getDrawables(DataTrack.class);
					for (DataTrack next: dataTracks) {
						if (!(next instanceof ParticleDataTrack)) continue;
						ParticleDataTrack track = (ParticleDataTrack)next;
						String trackName = track.getName("model"); //$NON-NLS-1$
						if (trackName.equals(dataName) || ("".equals(dataName) &&  //$NON-NLS-1$
								trackName.equals(TrackerRes.getString("ParticleDataTrack.New.Name")))) { //$NON-NLS-1$
							// found the data track
							foundMatch = true;
							if (track.isAutoPasteEnabled()) {
								// set new data immediately
				  			track.setData(data);
				  			track.prevDataString = dataString;
							}
							else {
								// set pending data
								track.setPendingDataString(dataString);
							}
							break;
						}
					}
					// if no matching track was found then create new track
					if (!foundMatch && frame.alwaysListenToClipboard) {
						dt = trackerPanel.importData(data, null);	
						if (dt!=null && dt instanceof ParticleDataTrack) {
							ParticleDataTrack track = (ParticleDataTrack)dt;
							track.prevDataString = track.pendingDataString = dataString;
						}
					}
		    }
			}
		} catch (Exception ex) {
		}
  }
  
  /**
   * Takes ownership of the clipboard.
   * 
   * @param t a Transferable.
   */
  public void takeOwnership(Transferable t) {
    sysClip.setContents(t, this);
  }
  
  /**
   * Stops this thread.
   */
  public void end() {
  	running = false;
  }
  
}