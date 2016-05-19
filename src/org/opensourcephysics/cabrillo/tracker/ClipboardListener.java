/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2015  Douglas Brown
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
   * Processes the Transferable contents.
   * 
   * @param t a Transferable.
   */
  public void processContents(Transferable t) {
  	if (TrackerIO.dataCopiedToClipboard) {
  		TrackerIO.dataCopiedToClipboard = false;
  		return;
  	}
    if (t==null || !t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
    	return;
    }
		int tab = frame.getSelectedTab();
		TrackerPanel trackerPanel = frame.getTrackerPanel(tab);
		if (trackerPanel!=null) {
	  	try {
				String dataString = (String)t.getTransferData(DataFlavor.stringFlavor);
				if (dataString!=null) {
					DatasetManager data = DataTool.parseData(dataString, null);
					if (data!=null) {
			      DataTrack dt = trackerPanel.importData(data, null);
			      if (dt instanceof ParticleDataTrack) {
			      	ParticleDataTrack pdt = (ParticleDataTrack)dt;
			      	pdt.prevDataString = dataString;
			      }
			    }
				}
			} catch (Exception ex) {
			}
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