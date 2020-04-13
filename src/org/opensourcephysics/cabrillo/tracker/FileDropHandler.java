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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.VideoFileFilter;

/**
 * A TransferHandler for opening video, trk and zip files via DragNDrop.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class FileDropHandler extends TransferHandler {
	
  static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String"; //$NON-NLS-1$
	
  static FileFilter dataFilter = TrackerIO.trkFileFilter;
	static FileFilter videoFilter = new VideoFileFilter();
	static FileFilter[] imageFilters = new ImageVideoType().getFileFilters();
	
	TFrame frame;
	DataFlavor uriListFlavor; // for Linux
//	List<File> dropList;
//	Component dropComponent;
	//DropTargetListener dropListener = new DropListener();
	
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
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {

		System.out.println("action=" + support.getDropAction() + support.getComponent().getClass().getName());
		if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return false;
		List<File> dropList = getFileList(support.getTransferable());
//		Component dropComponent;
//				// BH no longer necessary see since Java 7
//				// https://stackoverflow.com/questions/811248/how-can-i-use-drag-and-drop-in-swing-to-get-file-path
//				// || (OSPRuntime.isLinux()&&
//				//|| support.isDataFlavorSupported(uriListFlavor)
//				) {
//			if (dropList == null) {// && haveFileFlavor) {
//				try {
//					Transferable t = support.getTransferable();
//					dropList = getFileList(t);
//					dropComponent = support.getComponent();
////					dropComponent.getDropTarget().addDropTargetListener(dropListener);
//				} catch (Exception ex) {
//				}
//			}
//			System.out.println("Is this necessary??");
		if (!haveVideo(support.getComponent(), dropList))
			support.setDropAction(TransferHandler.COPY);
		return true;
	}
  
	private boolean haveVideo(Component c, List<File> dropList) {
		return (dropList != null && dropList.size() == 1 
				&& c instanceof TrackerPanel
				&& videoFilter.accept((File) dropList.get(0)));
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		List<File> fileList = getFileList(support.getTransferable());
		try {
			// define frameNumber for insertions
			int frameNumber = 0;
			// get target tracker panel, if any
			TrackerPanel targetPanel = (support.getComponent() instanceof TrackerPanel
					? (TrackerPanel) support.getComponent()
					: null);
			if (targetPanel != null) {
				targetPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (targetPanel.getVideo() != null) {
					frameNumber = targetPanel.getVideo().getFrameNumber();
				}
			}
			// load the files
			for (int j = 0; j < fileList.size(); j++) {
				final File file = (File) fileList.get(j);
				OSPLog.finest("dropped file: " + file.getAbsolutePath()); //$NON-NLS-1$
				// if dropAction is COPY then open in new tab
				if (support.getDropAction() == TransferHandler.COPY) {
					TrackerIO.open(file, frame);
				} else if (targetPanel != null && targetPanel.getVideo() instanceof ImageVideo && isImageFile(file)) {
					// if targetPanel has image video and file is image, add after current frame
					File[] added = TrackerIO.insertImagesIntoVideo(new File[] { file }, targetPanel, frameNumber + 1);
					frameNumber += added.length;
				} else if (targetPanel != null && videoFilter.accept(file)) {
					// if targetPanel not null and file is video then import
					// open in separate background thread
					final TFrame frame = targetPanel.getTFrame();
					final int n = frame.getTab(targetPanel);
					Runnable runner = new Runnable() {
						public void run() {
							TrackerPanel trackerPanel = frame.getTrackerPanel(n);
							TrackerIO.importVideo(file, trackerPanel, null);
						}
					};
					if (TrackerIO.loadInSeparateThread) {
						Thread opener = new Thread(runner);
						opener.setPriority(Thread.NORM_PRIORITY);
						opener.setDaemon(true);
						opener.start();
					} else {
						runner.run();
					}
				} else {
					// else inform user that file is not acceptable
					JOptionPane.showMessageDialog(frame, "\"" + file.getName() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
							+ TrackerRes.getString("FileDropHandler.Dialog.BadFile.Message"), //$NON-NLS-1$
							TrackerRes.getString("FileDropHandler.Dialog.BadFile.Title"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			frame.setCursor(Cursor.getDefaultCursor());
		}
		return true;
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

	/**
	 * Returns true if the specified file is an image.
	 * @param file the File
	 * @return true if an image
	 */
  private boolean isImageFile(File file) {
		for (int i = 0; i < imageFilters.length; i++) {
			if (imageFilters[i].accept(file)) return true;
		}
  	return false;
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

