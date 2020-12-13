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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;

/**
 * A TransferHandler for opening video, trk and zip files via DragNDrop.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class DataDropHandler extends TransferHandler {

	TFrame frame;
	

	/**
	 * Constructor.
	 * 
	 * @param frame the TFrame that will be the drop target
	 */
	public DataDropHandler(TFrame frame) {
		this.frame = frame;
	}

	Boolean isDropOK = null;

	/**
	 * Check to see that we can import this file. It if is NOT a video-type
	 * file (mp4, jpg, etc) then set the drop action to COPY rather than MOVE.
	 * 
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		 return (support.getTransferable().isDataFlavorSupported(DataFlavor.plainTextFlavor));
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		try {
			frame.getSelectedPanel().importDataAsync((String) t.getTransferData(DataFlavor.plainTextFlavor), null, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
