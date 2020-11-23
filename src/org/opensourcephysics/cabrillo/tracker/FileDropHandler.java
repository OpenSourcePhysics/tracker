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
import java.io.File;
import java.util.List;

import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;

/**
 * A TransferHandler for opening video, trk and zip files via DragNDrop.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class FileDropHandler extends TransferHandler {

	static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String"; //$NON-NLS-1$

	static FileFilter dataFilter = TrackerIO.trkFileFilter;
	TFrame frame;
	DataFlavor uriListFlavor; // for Linux
//	List<File> dropList;
//	Component dropComponent;
	// DropTargetListener dropListener = new DropListener();

	/**
	 * Constructor.
	 * 
	 * @param frame the TFrame that will be the drop target
	 */
	public FileDropHandler(TFrame frame) {
		this.frame = frame;
		try {
			uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
		} catch (ClassNotFoundException e) {
			// not possible - it's java.lang.String
		}
	}

	Boolean isDropOK = null;

	/**
	 * Check to see that we can import this file. It if is NOT a video-type
	 * file (mp4, jpg, etc) then set the drop action to COPY rather than MOVE.
	 * 
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		return (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		return (canImport(support) && TrackerIO.loadFiles(frame, getFileList(support.getTransferable()),
				support.getComponent() instanceof TrackerPanel ? (TrackerPanel) support.getComponent() : null));
	}

	/**
	 * Gets the file list from a Transferable.
	 * 
	 * Since Java 7 there is no issue with Linux.
	 * 
	 * @param t the Transferable
	 * @return a List of files
	 */
	@SuppressWarnings("unchecked")
	private List<File> getFileList(Transferable t) {
		try {
			return (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (Exception e) {
			return null;
		}

//    // expected data is a List of Files
//  	List<File> fileList = null;  	
//    try {
//      // get the data from the Transferable
//    	// BH always check for List<File> first, as it will have the data.
//        fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
//        if (fileList == null) {
//        	//if (OSPRuntime.isLinux()) {
//    		fileList = uriListToFileList((String) t.getTransferData(uriListFlavor));    		
//    	}
//    } catch (Exception ex) {
//    }
//  	return fileList;
	}


// BH unnecessary since Java 7
//	/**
//	 * Converts a URIList (String) to a List of files.
//	 * @param data the URIList String
//	 * @return a List of files
//	 */
//  private static List<File> uriListToFileList(String data) {
//    List<File> list = new ArrayList<File>();
//    StringTokenizer st = new StringTokenizer(data, "\r\n"); //$NON-NLS-1$
//    for (; st.hasMoreTokens();) {
//      String s = st.nextToken();
//      if (s.startsWith("#")) { //$NON-NLS-1$
//        // skip comments
//        continue;
//      }
//      try {
//        URI uri = new URI(s);
//        File file = new File(uri);
//        list.add(file);
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }
//    return list;
//  }
//  
//	/**
//	 * BH - Why is this necessary? Never seen this....
//	 * 
//	 * Inner DropTargetListener to reset the dropList to null when entering, exiting
//	 * or dropping on a drop target.
//	 */
//  private class DropListener extends DropTargetAdapter {
//  	
//  	@Override
//	  public void dragEnter(DropTargetDragEvent dtde) {
//	    dropList = null;
//	  }
//
//  	@Override
//	  public void dragExit(DropTargetEvent dte) {
//	    dropList = null;
//	  }
//
//  	@Override
//		public void drop(DropTargetDropEvent e) {
//  		dropList = null;
//		}
//
//  }

}
