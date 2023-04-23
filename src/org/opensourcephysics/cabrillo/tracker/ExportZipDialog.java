/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.LaunchBuilder;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.LibraryResource;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.AsyncFileChooser;

/**
 * A dialog for exporting/saving Tracker ZIP files. Steps are: 1. create temp
 * folder in target directory which will contain all files to be zipped 2. write
 * or copy the video clip(s) to a video subfolder 3. write or copy HTML pages,
 * stylesheets and image files into html and image subfolders 4. write the
 * converted Tracker data file(s) in the temp folder 5. zip the temp folder 6.
 * delete temp folder
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")

public class ExportZipDialog extends JDialog implements PropertyChangeListener {

	/**
	 * A class to contain the export operation for a specific file, particularly
	 * useful for asynchronous operation.
	 * 
	 * @author hansonr
	 *
	 */
	private class Export {

		private String name;
		private String originalVideoPath;
		private String videoTarget;
		private String trkPath;
		private int tabID;

		private ArrayList<File> zipList;

		private ExportVideoDialog exporter;
		private PropertyChangeListener listener;
		
		private String vidDir;

		protected Export(ArrayList<File> zipList, String name, String originalPath, 
				String trkPath, String videoTarget, ExportVideoDialog exporter,
				TrackerPanel panel) {
			this.name = name;
			this.zipList = zipList;
			this.originalVideoPath = originalPath;
			this.videoTarget = videoTarget;
			this.trkPath = trkPath;
			this.exporter = exporter;
			this.tabID = panel.getID();
		}

		protected void export() {
			OSPRuntime.showStatus("Exporting  " + (name == null ? "tab" : name));
			// set the waiting flag
			// render the video (also sets VideoIO preferred extension to this one)
			vidDir = getTempDirectory() + videoSubdirectory;
			if (exporter != null) {
				VideoType vidType = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
				VideoIO.ZipImageVideoType zipType = vidType instanceof VideoIO.ZipImageVideoType?
						(VideoIO.ZipImageVideoType) vidType: null;
				if (zipType != null) {
					// don't zip images inside TRZ files
					vidType = zipType.getImageVideoType();
				}
				String extension = vidType.getDefaultExtension();
				videoTarget = getVideoTarget(XML.getName(trkPath), extension);
//				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(tabID);
				exporter.setTrackerPanel(trackerPanel);
				exporter.setFormat((String) formatDropdown.getSelectedItem());
				// listen for cancel or saved events
				listener = new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						videoTarget = null; // set videoTarget to null if video_cancelled
						exporter.removePropertyChangeListener(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOSAVED,
								listener); // $NON-NLS-1$
						exporter.removePropertyChangeListener(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOCANCELED,
								listener); // $NON-NLS-1$
						if (e.getPropertyName().equals(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOSAVED)) { // $NON-NLS-1$
							// videoPath is new value from event (different from original path for image
							// videos)
							videoTarget = e.getNewValue().toString();
							finalizeExport();
						} else {
							exportCanceled();
						}
					}
				};
				exporter.addPropertyChangeListener(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOSAVED, listener); // $NON-NLS-1$
				exporter.addPropertyChangeListener(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOCANCELED, listener); // $NON-NLS-1$
				exporter.exportFullSizeVideo(videoTarget, trkPath);
				return;
			}
			
			// if source is an image video, then copy/extract additional image files
//			TrackerPanel panel = frame.getTrackerPanelForID(panelID);
			TrackerPanel panel = frame.getTrackerPanelForID(tabID);
			Video vid = panel.getVideo();
			if (vid instanceof ImageVideo) {
				ImageVideo imageVid = (ImageVideo) vid;
				String[] paths = imageVid.getValidPaths();
				// first path is originalPath relative to base
				int n = originalVideoPath.indexOf(XML.getName(paths[0]));
				if (n > 0) {
					String base = originalVideoPath.substring(0, n);
					for (String path : paths) {
						String name = XML.getName(path);
						path = base + name;
						String vidPath = vidDir + File.separator + name;
						File target = new File(vidPath); // $NON-NLS-1$
						if (path.equals(originalVideoPath)) {
							videoTarget = videoSubdirectory + File.separator + name;
						} else if (!createTarget(path, target)) {
							return;
						}
						zipList.add(target);
					}
				}
			}
			finalizeExport();
		}

		protected void finalizeExport() {
			// video, if any, should be ready at this point
			if (videoTarget != null) {
				// add video file(s) to ziplist
				File vidFile = new File(videoTarget);
				if (vidFile.exists())
					zipList.add(vidFile);
				else {
					vidFile = new File(getTempDirectory(), videoTarget);
					if (vidFile.exists())
						zipList.add(vidFile);
				}
			
				// deal with image videos
				if (!"".equals(videoSubdirectory)) { //$NON-NLS-1$
					// delete XML file, if any, from video directory
					File xmlFile = null;
					for (File next : new File(vidDir).listFiles()) {
						if (next.getName().endsWith(".xml") && next.getName().startsWith(targetName)) { //$NON-NLS-1$
							xmlFile = next;
						}
						if (next != vidFile && next != xmlFile)
							zipList.add(next);
					}
					if (xmlFile != null) {
//						XMLControl control = new XMLControlElement(xmlFile);
//						if (control.getObjectClassName().endsWith("ImageVideo")) { //$NON-NLS-1$
//							String[] paths = (String[]) control.getObject("paths"); //$NON-NLS-1$
//							String base = control.getBasepath();
//							if (base == null)
//								base = vidDir;
//							for (String path : paths) {
//								zipList.add(new File(vidDir + File.separator + path));
//							}
//						}
						xmlFile.delete();
					}
				}
			}

			// create and modify TrackerPanel XMLControl
			TrackerPanel panel = frame.getTrackerPanelForID(tabID);
			XMLControl control = new XMLControlElement(panel);
			// modify video path, clip settings of XMLControl
			if (exporter != null) {
				modifyControlForClip(control);
			} else if (panel.getVideo() != null) {
				XMLControl videoControl = control.getChildControl("videoclip").getChildControl("video"); //$NON-NLS-1$ //$NON-NLS-2$
				if (videoControl != null) {
					String vidPath = XML.forwardSlash(videoTarget);
					videoControl.setValue("path", vidPath); //$NON-NLS-1$
					// change "paths" too
					videoControl.setValue("paths", null); //$NON-NLS-1$ // eliminates unneeded list of images
//					String[] paths = (String[])videoControl.getObject("paths");
//					if (paths != null && paths.length > 0) {
//						String base = XML.getDirectoryPath(vidPath);
//						for (int i = 0; i < paths.length; i++) {
//							String name = XML.getName(paths[i]);
//							paths[i] = base + ("".equals(base)? "": "/") + name;
//						}
//						videoControl.setValue("paths", paths); //$NON-NLS-1$
//					}
				}
			}

			// add local HTML files to zipList and modify XMLControl accordingly
			ArrayList<String> htmlPaths = getHTMLPaths(control);
			if (!htmlPaths.isEmpty()) {
				String xml = control.toXML();
				for (String nextHTMLPath : htmlPaths) {
					String path = copyAndAddHTMLPage(nextHTMLPath, zipList);
					if (path != null) {
						xml = substitutePathInText(xml, nextHTMLPath, path, ">", "<"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				control = new XMLControlElement(xml);
			}

			// write XMLControl to TRK file and add to zipList
			// add to ziplist unless it is a duplicate
			zipList.add(new File(control.write(trkPath)));
			nextExport(zipList);
		}

		/**
		 * Modifies a TrackerPanel XMLControl to work with a trimmed video clip.
		 * 
		 * @param control the XMLControl to be modified
		 */
		private void modifyControlForClip(XMLControl control) {
//			TrackerPanel panel = frame.getTrackerPanelForID(panelID);
			TrackerPanel panel = frame.getTrackerPanelForID(tabID);
			VideoPlayer player = panel.getPlayer();

			// videoclip--convert frame count, start frame, step size and frame shift but
			// not start time or step count
			XMLControl clipXMLControl = control.getChildControl("videoclip"); //$NON-NLS-1$
			VideoClip realClip = player.getVideoClip();
			clipXMLControl.setValue("video_framecount", clipXMLControl.getInt("stepcount")); //$NON-NLS-1$ //$NON-NLS-2$
			clipXMLControl.setValue("startframe", 0); //$NON-NLS-1$
			clipXMLControl.setValue("stepsize", 1); //$NON-NLS-1$
//	    clipXMLControl.setValue("frameshift", 0); //$NON-NLS-1$
			if (videoTarget != null) {
				// modify videoControl with correct video type, and add delta_t for image videos
				VideoType videoType = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
				String trkDir = getTempDirectory();
				String relPath = XML.getPathRelativeTo(videoTarget, trkDir);

				Video newVideo = videoType.getVideo(XML.getName(videoTarget), vidDir, null);
				clipXMLControl.setValue("video", newVideo); //$NON-NLS-1$

				XMLControl videoControl = clipXMLControl.getChildControl("video"); //$NON-NLS-1$
				if (videoControl != null) {
					videoControl.setValue("path", relPath); //$NON-NLS-1$
					videoControl.setValue("filters", null); //$NON-NLS-1$
					if (videoType instanceof ImageVideoType) {
						videoControl.setValue("paths", null); //$NON-NLS-1$ // eliminates unneeded full list of image
																// files
						videoControl.setValue("delta_t", player.getMeanStepDuration()); //$NON-NLS-1$

					}
				}
			}

			// clipcontrol
			XMLControl clipControlControl = control.getChildControl("clipcontrol"); //$NON-NLS-1$
			clipControlControl.setValue("delta_t", player.getMeanStepDuration()); //$NON-NLS-1$
			clipControlControl.setValue("frame", 0); //$NON-NLS-1$

			// imageCoordSystem
			XMLControl coordsControl = control.getChildControl("coords"); //$NON-NLS-1$
			Object[] array = (Object[]) coordsControl.getObject("framedata"); //$NON-NLS-1$
//			ImageCoordSystem.FrameData[] coordKeyFrames = (ImageCoordSystem.FrameData[]) array;
			Map<Integer, Integer> newFrameNumbers = new TreeMap<Integer, Integer>();
			int newFrameNum = setNewFrameNumbersCoord(realClip, array, newFrameNumbers);
			ImageCoordSystem.FrameData[] newKeyFrames = new ImageCoordSystem.FrameData[newFrameNum + 1];
			for (Integer k : newFrameNumbers.keySet()) {
				newKeyFrames[k] = (ImageCoordSystem.FrameData) array[newFrameNumbers.get(k)];
			}
			coordsControl.setValue("framedata", newKeyFrames); //$NON-NLS-1$

			// tracks
			// first remove bad models, if any
			if (!badModels.isEmpty()) {
				ArrayList<?> tracks = ArrayList.class.cast(control.getObject("tracks")); //$NON-NLS-1$
				for (Iterator<?> it = tracks.iterator(); it.hasNext();) {
					TTrack track = (TTrack) it.next();
					if (badModels.contains(track)) {
						it.remove();
					}
				}
				control.setValue("tracks", tracks); //$NON-NLS-1$
			}
			// then modify frame references in track XMLcontrols
			for (XMLProperty next : control.getPropsRaw()) {
				XMLProperty prop = (XMLProperty) next;
				if (prop.getPropertyName().equals("tracks")) { //$NON-NLS-1$
					for (Object obj : prop.getPropertyContent()) {
						// every item is an XMLProperty
						XMLProperty item = (XMLProperty) obj;
						// the content of each item is the track control
						XMLControl trackControl = (XMLControl) item.getPropertyContent().get(0);
						Class<?> trackType = trackControl.getObjectClass();
						if (PointMass.class.equals(trackType)) {
							array = (Object[]) trackControl.getObject("framedata"); //$NON-NLS-1$
							//PointMass.FrameData[] pointMassKeyFrames = (PointMass.FrameData[]) array;
							newFrameNum = setNewFrameNumbersPointVector(realClip, array, newFrameNumbers);
							PointMass.FrameData[] newData = new PointMass.FrameData[newFrameNum + 1];
							int[] keys = new int[newFrameNumbers.size()];
							int index = 0;
							for (Integer k : newFrameNumbers.keySet()) {
								keys[index] = k;
								newData[k] = (PointMass.FrameData) array[newFrameNumbers.get(k)];
								index++;
							}
							trackControl.setValue("framedata", newData); //$NON-NLS-1$							
							trackControl.setValue("keyFrames", keys); //$NON-NLS-1$							
						}

						else if (Vector.class.isAssignableFrom(trackType)) {
							array = (Object[]) trackControl.getObject("framedata"); //$NON-NLS-1$
							newFrameNum = setNewFrameNumbersPointVector(realClip, array, newFrameNumbers);
							Vector.FrameData[] newKeys = new Vector.FrameData[newFrameNum + 1];
							for (Integer k : newFrameNumbers.keySet()) {
								newKeys[k] = (Vector.FrameData) array[newFrameNumbers.get(k)];
								newKeys[k].independent = newKeys[k].xc != 0 || newKeys[k].yc != 0;
							}
							trackControl.setValue("framedata", newKeys); //$NON-NLS-1$
						}

						else if (ParticleModel.class.isAssignableFrom(trackType)) {
							updateRange(realClip, trackControl, "start_frame", "end_frame");
						}

						else if (Calibration.class.equals(trackType) || OffsetOrigin.class.equals(trackType)) {
							array = (Object[]) trackControl.getObject("world_coordinates"); //$NON-NLS-1$
							newFrameNum = setNewFrameNumbersCalibration(realClip, array, newFrameNumbers);
							double[][] newKeys = new double[newFrameNum + 1][];
							for (Integer k : newFrameNumbers.keySet()) {
								newKeys[k] = (double[]) array[newFrameNumbers.get(k)];
							}
							trackControl.setValue("world_coordinates", newKeys); //$NON-NLS-1$
						}

						else if (CircleFitter.class.equals(trackType)) {
							updateRange(realClip, trackControl, "absolute_start", "absolute_end");
							// change and trim keyframe numbers
							array = (Object[]) trackControl.getObject("framedata"); //$NON-NLS-1$
							ArrayList<double[]> newKeyFrameData = new ArrayList<double[]>();
							newFrameNumbers.clear();
							newFrameNum = 0;
							for (int i = 0; i < array.length; i++) {
								if (array[i] == null)
									continue;
								double[] stepData = (double[]) array[i];
								int keyFrameNum = (int) stepData[0];
								newFrameNum = realClip.frameToStep(keyFrameNum);
								if (newFrameNum > realClip.getLastFrameNumber()
										|| newFrameNum < realClip.getFirstFrameNumber())
									continue;
								// change frame number in step data and add to the new key frame data
								stepData[0] = newFrameNum;
								newKeyFrameData.add(stepData);
//	    	        	newFrameNumbers.put(newFrameNum, i);  // maps to stepData index       	
							}
//	    	        double[][] newKeys = new double[newFrameNum+1][];
//	    	        for (Integer k: newFrameNumbers.keySet()) {
//	    	        	double[] stepData = keyFrameData[newFrameNumbers.get(k)];
//	    	        	newKeys[k] = keyFrameData[newFrameNumbers.get(k)];
//	    	        }
							double[][] newKeyData = newKeyFrameData.toArray(new double[newKeyFrameData.size()][]);
							trackControl.setValue("framedata", newKeyData); //$NON-NLS-1$
						}

						else if (TapeMeasure.class.equals(trackType)) {
							array = (Object[]) trackControl.getObject("framedata"); //$NON-NLS-1$
							if (array.length > 0) {
								newFrameNum = setNewFrameNumbersTape(realClip, array, newFrameNumbers);
								TapeMeasure.FrameData[] newKeys = new TapeMeasure.FrameData[newFrameNum + 1];
								for (Integer k : newFrameNumbers.keySet()) {
									newKeys[k] = (TapeMeasure.FrameData) array[newFrameNumbers.get(k)];
								}
								trackControl.setValue("framedata", newKeys); //$NON-NLS-1$
							}
						}

						else if (Protractor.class.equals(trackType)) {
							array = (Object[]) trackControl.getObject("framedata"); //$NON-NLS-1$
							newFrameNumbers.clear();
							newFrameNum = 0;
							int nonNullIndex = 0;
							for (int i = 0; i < array.length; i++) {
								if (i > realClip.getEndFrameNumber())
									break;
								if (array[i] != null) {
									nonNullIndex = i;
								}
								if (!realClip.includesFrame(i))
									continue;
								newFrameNum = realClip.frameToStep(i); // new frame number equals step number
								if (nonNullIndex > -1) {
									newFrameNumbers.put(newFrameNum, nonNullIndex);
									nonNullIndex = -1;
								} else {
									newFrameNumbers.put(newFrameNum, i);
								}
							}
							double[][] newKeys = new double[newFrameNum + 1][];
							for (Integer k : newFrameNumbers.keySet()) {
								newKeys[k] = (double[]) array[newFrameNumbers.get(k)];
							}
							trackControl.setValue("framedata", newKeys); //$NON-NLS-1$
						}
					}
				}

			}
		}


	}

	/**
	 * Why the difference? The coordinate system MUST have data for frame 0
	 * plus any later frames where the coordinate system changes. This is
	 * essentially the same as the keyframe requirements for calibration points
	 * and tape below.
	 * 
	 * @param realClip
	 * @param array
	 * @param newFrameNumbers
	 * @return
	 */
	protected static int setNewFrameNumbersCoord(VideoClip realClip, Object[] array, Map<Integer, Integer> newFrameNumbers) {
		newFrameNumbers.clear();
		int newFrameNum = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null)
				continue;
			if (i >= realClip.getEndFrameNumber())
				break;
			newFrameNum = Math.max(realClip.frameToStep(i), 0);
			if (i > realClip.getStartFrameNumber() && !realClip.includesFrame(i))
				newFrameNum++;
			newFrameNumbers.put(newFrameNum, i);
		}
		return newFrameNum;
	}

	/**
	 * why the difference here?  PointMass and Vector can have non-null frameData
	 * elements anywhere without problem. So we only need to convert the frame
	 * numbers of each non-null element in the clip.
	 * 
	 * @param realClip
	 * @param array
	 * @param newFrameNumbers
	 * @return
	 */
	public static int setNewFrameNumbersPointVector(VideoClip realClip, Object[] array, Map<Integer, Integer> newFrameNumbers) {
		newFrameNumbers.clear();
		int newFrameNum = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null || !realClip.includesFrame(i))
				continue;
			newFrameNum = realClip.frameToStep(i);
			newFrameNumbers.put(newFrameNum, i);
		}
		return newFrameNum;
	}

	/**
	 * why the difference? Calibration Points (when not fixed) use keyframes, 
	 * meaning that each step is a copy of the previous step unless a new
	 * keyframe is defined at that step. So we have to make sure there is a
	 * keyFrame at step 0. It does seem like this, the tape and the coords
	 * could have the same end?
	 * 
	 * @param realClip
	 * @param array
	 * @param newFrameNumbers
	 * @return
	 */
	public static int setNewFrameNumbersCalibration(VideoClip realClip, Object[] array, Map<Integer, Integer> newFrameNumbers) {
		newFrameNumbers.clear();
		int newFrameNum = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) // note we're not checking if it's in the clip
				// because we have to make sure we set the 0 keyframe
				continue;
			newFrameNum = realClip.frameToStep(i);
			// newFrameNum may be negative if keyframe < startFrame
			newFrameNum = Math.max(0, newFrameNum);
			newFrameNumbers.put(newFrameNum, i);
		}
		return newFrameNum;
	}

	/**
	 * Why the difference? Tapes also use keyframes. 
	 * Not sure why I use nonNullIndex in this method!
	 * 
	 * @param realClip
	 * @param array
	 * @param newFrameNumbers
	 * @return
	 */
	public static int setNewFrameNumbersTape(VideoClip realClip, Object[] array, Map<Integer, Integer> newFrameNumbers) {
		newFrameNumbers.clear();
		int newFrameNum = 0;
		int nonNullIndex = 0;
		for (int i = 0; i <= realClip.getEndFrameNumber(); i++) {
			if (i < array.length && array[i] != null) {
				nonNullIndex = i;
			}
			if (!realClip.includesFrame(i))
				continue;
			int n = realClip.frameToStep(i); // new frame number equals step number
			if (nonNullIndex > -1) {
				newFrameNumbers.put(n, nonNullIndex);
				newFrameNum = n;
				nonNullIndex = -1;
			} else if (i < array.length) {
				newFrameNumbers.put(n, i);
				newFrameNum = n;
			}
		}
		return newFrameNum;
	}

	protected static void updateRange(VideoClip realClip, XMLControl trackControl, String start, String end) {
		int frameNum = trackControl.getInt(start);
		if (frameNum > 0) {
			int newStartFrameNum = realClip.frameToStep(frameNum);
			// start frame should round up
			if (frameNum > realClip.getStartFrameNumber() && !realClip.includesFrame(frameNum))
				newStartFrameNum++;
			trackControl.setValue(start, newStartFrameNum);
		}
		frameNum = trackControl.getInt(end);
		if (frameNum > 0) {
			int newEndFrameNum = realClip.frameToStep(frameNum);
			// end frame should round down
			trackControl.setValue(end, newEndFrameNum);
		}
	}

	protected void nextExport(ArrayList<File> zipList) {
		if (exportIterator != null && exportIterator.hasNext()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					exportIterator.next().export();
				}

			}).start();
			return;
		}
		exportIterator = null;
		addFiles(zipList);
		saveZip(zipList);
	}

	protected void exportCanceled() {
		exportIterator = null;
		OSPLog.debug("Export canceled");
	}

	private static final String EXPANDED = "expanded";

	static {
		TFrame.haveExportDialog = true;
	}
	// static fields
	private static Map<Integer, ExportZipDialog> zipDialogs = new IdentityHashMap<>();
	protected static String videoSubdirectory = "videos"; //$NON-NLS-1$
	protected static String htmlSubdirectory = "html"; //$NON-NLS-1$
	protected static String imageSubdirectory = "images"; //$NON-NLS-1$
	protected static Color labelColor = new Color(0, 0, 102);
	protected static String preferredExtension = VideoIO.DEFAULT_VIDEO_EXTENSION; // JPG
	protected static boolean trimToClip = false;
	protected static int maxLineLength = 30, minWidth = 350;

	// instance fields
	protected ExportVideoDialog videoExporter;
	private TFrame frame;
	private Integer panelID;

	protected Icon openIcon;
	protected JPanel titlePanel, descriptionPanel, tabsPanel, videoPanel, metaPanel, thumbnailPanel, supportFilesPanel,
			advancedPanel;
	protected JPanel thumbnailImagePanel;
	protected Box titleTitleBox, descriptionTitleBox, tabsTitleBox, videoTitleBox, metaTitleBox, thumbTitleBox,
			supportFilesTitleBox, advancedTitleBox;
	protected Box metaFieldsBox, advancedFieldsBox, supportFilesBox;
	protected JLabel titleLabel, descriptionLabel, descriptionInfoLabel, tabsLabel, tabsInfoLabel, videoLabel,
			videoInfoLabel;
	protected JLabel metaLabel, metaInfoLabel, thumbLabel, thumbInfoLabel, supportFilesLabel, supportFilesInfoLabel,
			advancedLabel, advancedInfoLabel;
	protected JButton descriptionButton, tabsButton, videoButton, metaButton, thumbButton, supportFilesButton,
			advancedButton;
	protected JButton saveButton, closeButton, thumbnailButton, loadHTMLButton, helpButton;
	protected JComboBox<Object> formatDropdown;
	protected ArrayList<EntryField> tabTitleFields = new ArrayList<EntryField>();
	protected ArrayList<JCheckBox> tabCheckboxes = new ArrayList<JCheckBox>();
	protected JLabel authorLabel, contactLabel, keywordsLabel;
	protected JLabel thumbnailDisplay, urlLabel, htmlLabel;
	protected JCheckBox clipCheckbox, showThumbnailCheckbox;
	protected ArrayList<JLabel> labels = new ArrayList<JLabel>();
	protected EntryField titleField, authorField, contactField, keywordsField, urlField, htmlField;
	protected String targetName, targetDirectory, targetVideo, targetExtension;
	protected JTextArea filelistPane, descriptionPane;
	protected ArrayList<File> addedFiles = new ArrayList<File>();
	protected ArrayList<String> fileNames = new ArrayList<String>();
	protected JList<String> fileList;
	protected JButton addButton, removeButton;
	protected DefaultListModel<String> fileListModel;
	protected FileFilter recentAddFilesFilter;
	protected VideoListener videoExportListener;
	protected XMLControl control;
	protected boolean addThumbnail = true;
	protected ArrayList<ParticleModel> badModels; // particle models with start frames not included in clip
	protected String videoIOPreferredExtension;
	protected boolean isVisible, isOpenInTracker;
	private Iterator<Export> exportIterator;
	private File lastTRZ = new File("");

	/**
	 * Returns an ExportZipDialog for a TrackerPanel.
	 * 
	 * @param panel the TrackerPanel
	 * @return the ExportZipDialog
	 */
	public static synchronized ExportZipDialog getDialog(TrackerPanel panel) {
		ExportZipDialog dialog = zipDialogs.get(panel.getID());

		if (dialog == null) {
			dialog = new ExportZipDialog(panel);
			zipDialogs.put(panel.getID(), dialog);
			dialog.setResizable(false);
			dialog.frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, dialog); // $NON-NLS-1$
			dialog.setFontLevel(FontSizer.getLevel());
			dialog.control = new XMLControlElement(panel);
			dialog.addThumbnail = true;
			dialog.htmlField.setText(dialog.htmlField.getDefaultText());
			dialog.htmlField.setForeground(dialog.htmlField.getEmptyForeground());
			dialog.htmlField.setBackground(Color.white);
			if (panel.openedFromPath != null) {
				File htmlFile = new File(panel.openedFromPath);
				if (TrackerIO.trzFileFilter.accept(htmlFile)) {
					String baseName = XML.stripExtension(XML.getName(panel.openedFromPath));

					// find "added files" in the TRZ
					try {
						// BH avoiding ZipFile here
						ZipInputStream zipFile = new ZipInputStream(new FileInputStream(panel.openedFromPath));
						ZipEntry nextEntry;
						while ((nextEntry = zipFile.getNextEntry()) != null) {
							// ignore entries pointing to subdirectories other than html
							String name = XML.forwardSlash(nextEntry.getName());
							if (name.contains("/")) { //$NON-NLS-1$
								if (!name.contains("html/") || name.contains("_info.")) { //$NON-NLS-1$ //$NON-NLS-2$
									continue;
								}
							}
							// ignore thumbnails
							if (name.contains("_thumbnail")) { //$NON-NLS-1$
								continue;
							}

							String path = panel.openedFromPath + "!/" + name; //$NON-NLS-1$
							File file = new File(path);
							// ignore TRK files
							if (TrackerIO.trkFileFilter.accept(file)) {
								continue;
							}
							if (!dialog.addedFiles.contains(file)) {
								dialog.addedFiles.add(file);
							}
						}
						dialog.refreshFileList();
						dialog.refreshSupportFilesGUI();
						zipFile.close();
					} catch (IOException e) {
					}

					// refresh fields from HTML
					String htmlPath = panel.openedFromPath + "!/html/" + baseName + "_info.html"; //$NON-NLS-1$ //$NON-NLS-2$
					htmlFile = new File(htmlPath);
					dialog.refreshFieldsFromHTML(htmlFile);
				}
			}
			String currentTabTitle = ""; //$NON-NLS-1$
			for (int i = 0; i < dialog.frame.getTabCount(); i++) {
				String next = dialog.frame.getTabTitle(i);
				if (dialog.frame.getTrackerPanelForTab(i) == panel) {
					currentTabTitle = next;
				}
			}
			if ("".equals(dialog.titleField.getText())) { //$NON-NLS-1$
				dialog.titleField.setText(XML.stripExtension(currentTabTitle));
			}
			dialog.titleField.requestFocusInWindow();
			dialog.refreshFormatDropdown();
		}

		return dialog;
	}

	/**
	 * Returns true if an ExportZipDialog exists for a TrackerPanel.
	 * 
	 * @param panel the TrackerPanel
	 * @return true if the ExportZipDialog exists
	 */
	public static boolean hasDialog(TrackerPanel panel) {
		return zipDialogs.get(panel.getID()) != null;
	}

	/**
	 * Sets the font level of a single ExportZipDialog.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		// refresh the dropdowns
		int n = formatDropdown.getSelectedIndex();
		Object[] items = new Object[formatDropdown.getItemCount()];
		for (int i = 0; i < items.length; i++) {
			items[i] = formatDropdown.getItemAt(i);
		}
		DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>(items);
		formatDropdown.setModel(model);
		formatDropdown.setSelectedItem(n);
		// reset label sizes
		Font font = titleLabel.getFont();
		int w = 0;
		for (Iterator<JLabel> it = labels.iterator(); it.hasNext();) {
			JLabel next = it.next();
			Rectangle2D rect = font.getStringBounds(next.getText() + " ", OSPRuntime.frc); //$NON-NLS-1$
			w = Math.max(w, (int) rect.getWidth() + 1);
		}
		int h = titleField.getMinimumSize().height;
		Dimension labelSize = new Dimension(w, h);
		for (Iterator<JLabel> it = labels.iterator(); it.hasNext();) {
			JLabel next = it.next();
			next.setPreferredSize(labelSize);
		}
		pack();
	}

	@Override
	public void setVisible(boolean vis) {
		if (panelID == null)
			return;
		if (vis) {
			refreshGUI();
		}
		isVisible = vis;
		super.setVisible(vis);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = Math.max(dim.width, (int) (((1 + (FontSizer.getFactor() - 1) * 0.6) * minWidth)));
		return dim;
	}

	/**
	 * Responds to property change events from TrackerPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(TFrame.PROPERTY_TFRAME_TAB)) { // $NON-NLS-1$
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			if (!frame.isRemovingAll()) {
				if (e.getNewValue() == trackerPanel) {
					setVisible(isVisible);
					return;
				}
				if (e.getNewValue() == null && e.getOldValue() == trackerPanel) {
					// tab was removed, so dispose
					clear(trackerPanel);
					return;
				}
			}
			boolean vis = isVisible;
			setVisible(false);
			isVisible = vis;
		}
	}

	/**
	 * Disposes of a zip dialog for a TrackerPanel.
	 * 
	 * @param panel the TrackerPanel
	 */
	public static synchronized void clear(TrackerPanel panel) {
		ExportZipDialog dialog = zipDialogs.remove(panel.getID());
		if (dialog != null) {
			dialog.setVisible(false);
			dialog.frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, dialog); // $NON-NLS-1$
			dialog.panelID = null;
			dialog.frame = null;
		}
	}

	/**
	 * Sets the font level of all ExportZipDialogs.
	 * 
	 * @param level the font level
	 */
	public static void setFontLevels(int level) {
		for (ExportZipDialog d : zipDialogs.values()) {
			d.setFontLevel(level);
		}
	}

	// _____________________________ private methods ____________________________

	/**
	 * Private constructor.
	 *
	 * @param panel a TrackerPanel
	 */
	private ExportZipDialog(TrackerPanel panel) {
		super(panel.getTFrame(), false);
		frame = panel.getTFrame();
		panelID = panel.getID();
//		videoExporter = ExportVideoDialog.getDialog(panel);
		createGUI();
		refreshGUI();
		// center dialog on the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - getBounds().width) / 2;
		int y = (dim.height - getBounds().height) / 2;
		setLocation(x, y);
	}

	/**
	 * Creates the visible components of this dialog.
	 */
	private void createGUI() {
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		openIcon = Tracker.getResourceIcon("open.gif", true);
		Color color = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
		if (color != null)
			UIManager.put("ComboBox.disabledForeground", color); //$NON-NLS-1$
		videoExportListener = new VideoListener();

		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		Border toolbarBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);

		// title panel
		titlePanel = new JPanel(new BorderLayout());
		titleTitleBox = Box.createHorizontalBox();
		titleLabel = new JLabel();
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		titleTitleBox.add(titleLabel);
		titleField = new EntryField(30);
		Box space = Box.createHorizontalBox();
		space.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
		space.add(titleField);
		titleTitleBox.add(space);
		titlePanel.add(titleTitleBox, BorderLayout.NORTH);

		// description panel
		descriptionPanel = new JPanel(new BorderLayout());
		descriptionTitleBox = Box.createHorizontalBox();
		descriptionLabel = new JLabel();
		descriptionLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		descriptionTitleBox.add(descriptionLabel);
		descriptionInfoLabel = new JLabel();
		descriptionInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		descriptionInfoLabel.setFont(descriptionInfoLabel.getFont().deriveFont(Font.PLAIN));
		descriptionTitleBox.add(descriptionInfoLabel);
		descriptionTitleBox.add(Box.createHorizontalGlue());
		descriptionButton = new TButton();
		descriptionButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		descriptionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = descriptionPanel.getName();
				descriptionPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				descriptionButton.requestFocusInWindow();
			}
		});
		descriptionButton.setContentAreaFilled(false);
		descriptionTitleBox.add(descriptionButton);
		descriptionPanel.add(descriptionTitleBox, BorderLayout.NORTH);

		// description pane
		descriptionPane = new JTextArea();
		descriptionPane.setLineWrap(true);
		descriptionPane.setWrapStyleWord(true);
		descriptionPane.getDocument().putProperty("parent", descriptionPane); //$NON-NLS-1$
		descriptionPane.getDocument().addDocumentListener(EntryField.documentListener);
		descriptionPane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				descriptionPane.setBackground(Color.white);
				refreshDescriptionGUI();
			}
		});

		// tabs panel
		tabsPanel = new JPanel(new BorderLayout());
		tabsTitleBox = Box.createHorizontalBox();
		tabsLabel = new JLabel();
		tabsLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		tabsTitleBox.add(tabsLabel);
		tabsInfoLabel = new JLabel();
		tabsInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		tabsInfoLabel.setFont(tabsInfoLabel.getFont().deriveFont(Font.PLAIN));
		tabsTitleBox.add(tabsInfoLabel);
		tabsTitleBox.add(Box.createHorizontalGlue());
		tabsButton = new TButton();
		tabsButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		tabsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = tabsPanel.getName();
				tabsPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				tabsButton.requestFocusInWindow();
			}
		});
		tabsButton.setContentAreaFilled(false);
		tabsTitleBox.add(tabsButton);
		tabsPanel.add(tabsTitleBox, BorderLayout.NORTH);

		// video panel
		videoPanel = new JPanel(new BorderLayout());
		clipCheckbox = new JCheckBox();
		clipCheckbox.setSelected(trimToClip);
//	  clipCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 6));
		clipCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshVideosGUI();
				clipCheckbox.requestFocusInWindow();
				trimToClip = clipCheckbox.isSelected();
			}
		});
		formatDropdown = new JComboBox<Object>(TrackerIO.getVideoFormats()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				dim.height = titleField.getPreferredSize().height;
				return dim;
			}
		};
		formatDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshVideosGUI();
				formatDropdown.requestFocusInWindow();
			}
		});
		formatDropdown.setRenderer(new FormatRenderer());
		videoTitleBox = Box.createHorizontalBox();
		videoLabel = new JLabel();
		videoLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		videoTitleBox.add(videoLabel);
		videoInfoLabel = new JLabel();
		videoInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		videoInfoLabel.setFont(videoInfoLabel.getFont().deriveFont(Font.PLAIN));
		videoTitleBox.add(videoInfoLabel);
		videoTitleBox.add(Box.createHorizontalGlue());
		videoButton = new TButton();
		videoButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		videoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = videoPanel.getName();
				videoPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				videoButton.requestFocusInWindow();
			}
		});
		videoButton.setContentAreaFilled(false);
		videoTitleBox.add(videoButton);
		videoPanel.add(videoTitleBox, BorderLayout.NORTH);

		// meta panel
		metaPanel = new JPanel(new BorderLayout());
		metaTitleBox = Box.createHorizontalBox();
		metaLabel = new JLabel();
		metaLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		metaTitleBox.add(metaLabel);
		metaInfoLabel = new JLabel();
		metaInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		metaInfoLabel.setFont(metaInfoLabel.getFont().deriveFont(Font.PLAIN));
		metaTitleBox.add(metaInfoLabel);
		metaTitleBox.add(Box.createHorizontalGlue());
		metaButton = new TButton();
		metaButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		metaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = metaPanel.getName();
				metaPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				metaButton.requestFocusInWindow();
			}
		});
		metaButton.setContentAreaFilled(false);
		metaTitleBox.add(metaButton);

		// thumbnail panel
		thumbnailPanel = new JPanel(new BorderLayout());
		thumbTitleBox = Box.createHorizontalBox();
		thumbLabel = new JLabel();
		thumbLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		thumbTitleBox.add(thumbLabel);
		thumbInfoLabel = new JLabel();
		thumbInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		thumbInfoLabel.setFont(thumbInfoLabel.getFont().deriveFont(Font.PLAIN));
		thumbTitleBox.add(thumbInfoLabel);
		thumbTitleBox.add(Box.createHorizontalGlue());
		thumbButton = new TButton();
		thumbButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		thumbButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = thumbnailPanel.getName();
				thumbnailPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				thumbButton.requestFocusInWindow();
			}
		});
		thumbButton.setContentAreaFilled(false);
		thumbTitleBox.add(thumbButton);

		// supportFiles panel
		supportFilesPanel = new JPanel(new BorderLayout());
		supportFilesTitleBox = Box.createHorizontalBox();
		supportFilesLabel = new JLabel();
		supportFilesLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		supportFilesTitleBox.add(supportFilesLabel);
		supportFilesInfoLabel = new JLabel();
		supportFilesInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		supportFilesInfoLabel.setFont(supportFilesInfoLabel.getFont().deriveFont(Font.PLAIN));
		supportFilesTitleBox.add(supportFilesInfoLabel);
		supportFilesTitleBox.add(Box.createHorizontalGlue());
		supportFilesButton = new TButton();
		supportFilesButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		supportFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = supportFilesPanel.getName();
				supportFilesPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				supportFilesButton.requestFocusInWindow();
			}
		});
		supportFilesButton.setContentAreaFilled(false);
		supportFilesTitleBox.add(supportFilesButton);
		// file list
		fileListModel = new DefaultListModel<>();
		fileList = new JList<String>(fileListModel);
		fileList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				removeButton.setEnabled(fileList.getSelectedValue() != null);
			}
		});
		// Add/remove buttons
		addButton = new TButton() {
			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		addButton.setContentAreaFilled(false);
		addButton.setForeground(labelColor);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show file chooser to add support files
				JFileChooser chooser = TrackerIO.getChooser();
				chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.AddFile.Title")); //$NON-NLS-1$
				chooser.addChoosableFileFilter(LaunchBuilder.getPDFFilter());
				chooser.addChoosableFileFilter(LaunchBuilder.getHTMLFilter());

				if (recentAddFilesFilter != null) {
					chooser.setFileFilter(recentAddFilesFilter);
				} else {
					chooser.setFileFilter(LaunchBuilder.getPDFFilter());
				}

				TrackerIO.getChooserFilesAsync(frame, "open any", new Function<File[], Void>() { //$NON-NLS-1$

					@Override
					public Void apply(File[] files) {
						recentAddFilesFilter = chooser.getFileFilter();
						chooser.removeChoosableFileFilter(LaunchBuilder.getHTMLFilter());
						chooser.removeChoosableFileFilter(LaunchBuilder.getPDFFilter());
						if (files == null) {
							return null;
						}
						if (!addedFiles.contains(files[0])) {
							addedFiles.add(files[0]);
							refreshFileList();
							refreshSupportFilesGUI();
						}
						return null;
					}

				});
			}
		});
		removeButton = new TButton();
		removeButton.setContentAreaFilled(false);
		removeButton.setForeground(labelColor);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = (String) fileList.getSelectedValue();
				if (name != null) {
					for (Iterator<File> it = addedFiles.iterator(); it.hasNext();) {
						File next = it.next();
						if (name.equals(next.getName())) {
							it.remove();
							break;
						}
					}
					refreshFileList();
					refreshSupportFilesGUI();
				}
			}
		});
		final Box buttonbox = Box.createVerticalBox();
		buttonbox.add(addButton, BorderLayout.NORTH);
		buttonbox.add(removeButton, BorderLayout.SOUTH);
		JScrollPane scroller = new JScrollPane(fileList) {
			@Override
			public Dimension getPreferredSize() {
				int h = buttonbox.getPreferredSize().height;
				return new Dimension(10, h);
			}
		};
		Box box = Box.createHorizontalBox();
		box.add(scroller);
		box.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 4));
		supportFilesBox = Box.createHorizontalBox();
		supportFilesBox.add(buttonbox);
		supportFilesBox.add(box);

		// advanced panel
		advancedPanel = new JPanel(new BorderLayout());
		advancedTitleBox = Box.createHorizontalBox();
		advancedLabel = new JLabel();
		advancedLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
		advancedTitleBox.add(advancedLabel);
		advancedInfoLabel = new JLabel();
		advancedInfoLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		advancedInfoLabel.setFont(advancedInfoLabel.getFont().deriveFont(Font.PLAIN));
		advancedTitleBox.add(advancedInfoLabel);
		advancedTitleBox.add(Box.createHorizontalGlue());
		advancedButton = new TButton();
		advancedButton.setToolTipText(TrackerRes.getString("ExportZipDialog.Button.Expand.Tooltip")); //$NON-NLS-1$
		advancedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = advancedPanel.getName();
				advancedPanel.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				advancedButton.requestFocusInWindow();
			}
		});
		advancedButton.setContentAreaFilled(false);
		advancedTitleBox.add(advancedButton);
		advancedPanel.add(advancedTitleBox, BorderLayout.NORTH);

		// button bar
		JPanel buttonbar = new JPanel();
		helpButton = new JButton();
		helpButton.setForeground(new Color(0, 0, 102));
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.showHelp("zip", 0); //$NON-NLS-1$
			}
		});
		saveButton = new JButton();
		saveButton.setForeground(labelColor);
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveZipAs();
			}
		});

		closeButton = new JButton();
		closeButton.setForeground(labelColor);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonbar.add(helpButton);
		buttonbar.add(saveButton);
		buttonbar.add(closeButton);

		// metadata and advanced
		metaFieldsBox = Box.createVerticalBox();
		advancedFieldsBox = Box.createVerticalBox();

		// HTML file
		htmlLabel = new JLabel();
		htmlField = new EntryField(30);
		htmlField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		htmlField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshAdvancedGUI();
				htmlField.requestFocusInWindow();
			}
		});
		loadHTMLButton = new TButton(openIcon);
		loadHTMLButton.setContentAreaFilled(false);
		loadHTMLButton.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		loadHTMLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = TrackerIO.getChooser();
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.OpenHTML.Title")); //$NON-NLS-1$
				chooser.setFileFilter(LaunchBuilder.getHTMLFilter());

				File[] files = TrackerIO.getChooserFilesAsync(frame, "open any", new Function<File[], Void>() { //$NON-NLS-1$

					@Override
					public Void apply(File[] files) {
						if (files == null) {
							return null;
						}
						htmlField.setText(XML.getRelativePath(files[0].getPath()));
						refreshFieldsFromHTML(files[0]);
						refreshGUI();
						return null;
					}
				});
				chooser.removeChoosableFileFilter(LaunchBuilder.getHTMLFilter());
				if (files == null)
					return; // cancelled by user
			}

		});
		JToolBar htmlbar = new JToolBar();
		htmlbar.setBorder(toolbarBorder);
		htmlbar.setFloatable(false);
		htmlbar.setOpaque(false);
		htmlbar.add(htmlLabel);
		htmlbar.add(htmlField);
		htmlbar.add(loadHTMLButton);

		// author
		authorLabel = new JLabel();
		authorField = new EntryField(30);
		authorField.setText(panel.author);
		authorField.setBackground(Color.white);
		authorField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		authorField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshMetadataGUI();
				frame.getTrackerPanelForID(panelID).author = authorField.getText().trim();
				authorField.requestFocusInWindow();
			}
		});
		JToolBar authorbar = new JToolBar();
		authorbar.setBorder(toolbarBorder);
		authorbar.setFloatable(false);
		authorbar.setOpaque(false);
		authorbar.add(authorLabel);
		authorbar.add(authorField);

		// contact
		contactLabel = new JLabel();
		contactField = new EntryField(30);
		contactField.setText(panel.contact);
		contactField.setBackground(Color.white);
		contactField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		contactField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshMetadataGUI();
				frame.getTrackerPanelForID(panelID).contact = contactField.getText().trim();
				contactField.requestFocusInWindow();
			}
		});
		JToolBar contactbar = new JToolBar();
		contactbar.setBorder(toolbarBorder);
		contactbar.setFloatable(false);
		contactbar.setOpaque(false);
		contactbar.add(contactLabel);
		contactbar.add(contactField);
		// keywords
		keywordsLabel = new JLabel();
		keywordsField = new EntryField(30);
		keywordsField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		keywordsField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshMetadataGUI();
				keywordsField.requestFocusInWindow();
			}
		});
		JToolBar keywordsbar = new JToolBar();
		keywordsbar.setBorder(toolbarBorder);
		keywordsbar.setFloatable(false);
		keywordsbar.setOpaque(false);
		keywordsbar.add(keywordsLabel);
		keywordsbar.add(keywordsField);
		// URL
		urlLabel = new JLabel();
		urlField = new EntryField(30);
		urlField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
		urlField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshAdvancedGUI();
				urlField.requestFocusInWindow();
			}
		});
		JToolBar urlbar = new JToolBar();
		urlbar.setBorder(toolbarBorder);
		urlbar.setFloatable(false);
		urlbar.setOpaque(false);
		urlbar.add(urlLabel);
		urlbar.add(urlField);

		metaFieldsBox.add(authorbar);
		metaFieldsBox.add(contactbar);
		metaFieldsBox.add(keywordsbar);
		advancedFieldsBox.add(urlbar);
		advancedFieldsBox.add(htmlbar);

		// thumbnail panel
		thumbnailDisplay = new JLabel();
		Border line = BorderFactory.createLineBorder(Color.black);
		Border empty = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		thumbnailDisplay.setBorder(BorderFactory.createCompoundBorder(empty, line));
		thumbnailButton = new JButton();
		thumbnailButton.setForeground(labelColor);
		thumbnailButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ThumbnailDialog.getDialog(frame.getTrackerPanelForID(panelID), false).setVisible(true);
			}
		});
		thumbnailImagePanel = new JPanel();
		thumbnailImagePanel.add(thumbnailDisplay);
		showThumbnailCheckbox = new JCheckBox();
		showThumbnailCheckbox.setSelected(false);
		showThumbnailCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showThumbnailCheckbox.isSelected()) {
					refreshThumbnailImage();
					thumbnailPanel.add(thumbnailImagePanel, BorderLayout.SOUTH);
				} else {
					thumbnailPanel.remove(thumbnailImagePanel);
				}
				pack();
				TFrame.repaintT(ExportZipDialog.this);
			}
		});
		if (showThumbnailCheckbox.isSelected()) {
			thumbnailPanel.add(thumbnailImagePanel, BorderLayout.SOUTH);
		}
		ThumbnailDialog dialog = ThumbnailDialog.getDialog(panel, false);
		dialog.addPropertyChangeListener("accepted", new PropertyChangeListener() { //$NON-NLS-1$
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				refreshThumbnailImage();
			}
		});

		// assemble
		JPanel northCenterPanel = new JPanel(new BorderLayout());
		JPanel northUpper = new JPanel(new BorderLayout());
		JPanel northLower = new JPanel(new BorderLayout());
		northCenterPanel.add(northUpper, BorderLayout.NORTH);
		northCenterPanel.add(northLower, BorderLayout.SOUTH);
		JPanel southCenterPanel = new JPanel(new BorderLayout());
		JPanel southUpper = new JPanel(new BorderLayout());
		JPanel southLower = new JPanel(new BorderLayout());
		southCenterPanel.add(southUpper, BorderLayout.NORTH);
		southCenterPanel.add(southLower, BorderLayout.CENTER);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(northCenterPanel, BorderLayout.NORTH);
		centerPanel.add(southCenterPanel, BorderLayout.CENTER);

		contentPane.add(titlePanel, BorderLayout.NORTH);
		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(buttonbar, BorderLayout.SOUTH);

		northUpper.add(descriptionPanel, BorderLayout.NORTH);
		northUpper.add(tabsPanel, BorderLayout.SOUTH);
		northLower.add(videoPanel, BorderLayout.NORTH);
		northLower.add(metaPanel, BorderLayout.SOUTH);
		southUpper.add(thumbnailPanel, BorderLayout.NORTH);
		southUpper.add(supportFilesPanel, BorderLayout.SOUTH);
		southLower.add(advancedPanel, BorderLayout.NORTH);

		labels.add(authorLabel);
		labels.add(contactLabel);
		labels.add(keywordsLabel);
		labels.add(urlLabel);
		labels.add(htmlLabel);

		Border etch = BorderFactory.createEtchedBorder();
		titlePanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		descriptionPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		tabsPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		videoPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		thumbnailPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		metaPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		supportFilesPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));
		advancedPanel.setBorder(BorderFactory.createCompoundBorder(empty, etch));

		MouseListener openCloseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				JComponent source = (JComponent) e.getSource();
				if (source == descriptionLabel)
					source = descriptionPanel; // workaround
				String name = source.getName();
				source.setName(name == null ? EXPANDED : null); // $NON-NLS-1$
				refreshGUI();
				if (source == descriptionPanel)
					descriptionButton.requestFocusInWindow();
				if (source == tabsPanel)
					tabsButton.requestFocusInWindow();
				if (source == videoPanel)
					videoButton.requestFocusInWindow();
				if (source == metaPanel)
					metaButton.requestFocusInWindow();
				if (source == thumbnailPanel)
					thumbButton.requestFocusInWindow();
				if (source == supportFilesPanel)
					supportFilesButton.requestFocusInWindow();
				if (source == advancedPanel)
					advancedButton.requestFocusInWindow();
			}
		};
		descriptionPanel.addMouseListener(openCloseListener);
		descriptionLabel.addMouseListener(openCloseListener);
		tabsPanel.addMouseListener(openCloseListener);
		videoPanel.addMouseListener(openCloseListener);
		thumbnailPanel.addMouseListener(openCloseListener);
		metaPanel.addMouseListener(openCloseListener);
		supportFilesPanel.addMouseListener(openCloseListener);
		advancedPanel.addMouseListener(openCloseListener);

	}

	/**
	 * Refreshes the visible components of this dialog.
	 */
	private void refreshGUI() {
		// refresh strings
		String title = TrackerRes.getString("ZipResourceDialog.Title"); //$NON-NLS-1$
		setTitle(title);

		// buttons
		clipCheckbox.setText(TrackerRes.getString("ZipResourceDialog.Checkbox.TrimVideo")); //$NON-NLS-1$
		helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$
		saveButton.setText(TrackerRes.getString("ExportZipDialog.Button.SaveZip.Text") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
		thumbnailButton.setText(TrackerRes.getString("ZipResourceDialog.Button.ThumbnailSettings") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		showThumbnailCheckbox.setText(TrackerRes.getString("ZipResourceDialog.Checkbox.PreviewThumbnail")); //$NON-NLS-1$
		addButton.setText(TrackerRes.getString("Dialog.Button.Add") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		removeButton.setText(TrackerRes.getString("Dialog.Button.Remove")); //$NON-NLS-1$
		removeButton.setEnabled(fileList.getSelectedValue() != null);

		// labels
		htmlLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.HTML")); //$NON-NLS-1$
		titleLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Title") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		descriptionLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Description")); //$NON-NLS-1$
		authorLabel.setText(TrackerRes.getString("PropertiesDialog.Label.Author")); //$NON-NLS-1$
		contactLabel.setText(TrackerRes.getString("PropertiesDialog.Label.Contact")); //$NON-NLS-1$
		keywordsLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Keywords")); //$NON-NLS-1$
		urlLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Link")); //$NON-NLS-1$

		// tooltips
		htmlLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.HTML") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		htmlField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.HTML") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		titleLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Title")); //$NON-NLS-1$
		titleField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Title")); //$NON-NLS-1$
		descriptionLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Description")); //$NON-NLS-1$
		descriptionPane.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Description")); //$NON-NLS-1$
		authorLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Author")); //$NON-NLS-1$
		authorField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Author")); //$NON-NLS-1$
		contactLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Contact")); //$NON-NLS-1$
		contactField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Contact")); //$NON-NLS-1$
		keywordsLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Keywords")); //$NON-NLS-1$
		keywordsField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Keywords")); //$NON-NLS-1$
		urlLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Link")); //$NON-NLS-1$
		urlField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Link")); //$NON-NLS-1$
		clipCheckbox.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.TrimVideo")); //$NON-NLS-1$
		thumbnailButton.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.ThumbnailSettings")); //$NON-NLS-1$
		loadHTMLButton.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.LoadHTML")); //$NON-NLS-1$

		// set label sizes
		Font font = titleLabel.getFont();
		int w = 0;
		for (Iterator<JLabel> it = labels.iterator(); it.hasNext();) {
			JLabel next = it.next();
			FontSizer.setFonts(next, FontSizer.getLevel());
			Rectangle2D rect = font.getStringBounds(next.getText() + " ", OSPRuntime.frc); //$NON-NLS-1$
			w = Math.max(w, (int) rect.getWidth() + 1);
		}
		int h = authorField.getMinimumSize().height;
		Dimension labelSize = new Dimension(w, h);
		for (Iterator<JLabel> it = labels.iterator(); it.hasNext();) {
			JLabel next = it.next();
			next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
			next.setPreferredSize(labelSize);
			next.setHorizontalAlignment(SwingConstants.TRAILING);
			next.setAlignmentY(Box.TOP_ALIGNMENT);
		}

		// set html field properties
		String path = htmlField.getText().trim();
		Resource res = null;
		if (!path.equals(htmlField.getDefaultText()) && !path.equals("")) { //$NON-NLS-1$
			res = ResourceLoader.getResource(path);
			htmlField.setForeground(res == null ? Color.red : EntryField.defaultForeground);
		}
		htmlField.setBackground(Color.white);

		// set url field properties
		path = urlField.getText().trim();
		if (!path.equals("")) { //$NON-NLS-1$
			try {
				new URL(path); // throws exception if malformed
				urlField.setForeground(EntryField.defaultForeground);
			} catch (MalformedURLException e) {
				urlField.setForeground(Color.red);
			}
		}

		// enable/disable urlField and descriptionPane
		urlField.setEnabled(res == null);
		urlLabel.setEnabled(res == null);
		descriptionPane.setEnabled(res == null);

		if (panelID != null) {
			refreshDescriptionGUI();
			refreshTabsGUI();
			refreshVideosGUI();
			refreshMetadataGUI();
			refreshThumbnailGUI();
			refreshSupportFilesGUI();
			refreshAdvancedGUI();
		}
		pack();
		TFrame.repaintT(this);
	}

	/**
	 * Refreshes the thumbnail image based on the current ThumbnailDialog settings.
	 */
	private void refreshThumbnailImage() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		ThumbnailDialog thumbnailDialog = ThumbnailDialog.getDialog(trackerPanel, false);
		BufferedImage image = thumbnailDialog.getThumbnail();
		thumbnailDisplay.setIcon(new ImageIcon(image));
		pack();
	}

	/**
	 * Refreshes the Description GUI
	 */
	private void refreshDescriptionGUI() {
		String title = TrackerRes.getString("ZipResourceDialog.Label.Description"); //$NON-NLS-1$
		descriptionLabel.setText(title + ":"); //$NON-NLS-1$

		String info = descriptionPane.getText().trim();
		if ("".equals(info)) { // no description //$NON-NLS-1$
			info = TrackerRes.getString("ExportZipDialog.Border.Title.None"); //$NON-NLS-1$
		} else if (info.length() > maxLineLength) {
			info = info.substring(0, maxLineLength) + "..."; //$NON-NLS-1$
		}
		descriptionInfoLabel.setText(info);

		descriptionPanel.removeAll();
		descriptionPanel.add(descriptionTitleBox, BorderLayout.NORTH);
		if (descriptionPanel.getName() != null) {
			descriptionButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			FontSizer.setFonts(descriptionPane, FontSizer.getLevel());
			JScrollPane scroller = new JScrollPane(descriptionPane) {
				@Override
				public Dimension getPreferredSize() {
					int w = super.getPreferredSize().width;
					return new Dimension(w, 60);
				}
			};
			Box box = Box.createHorizontalBox();
			box.add(scroller);
			box.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			descriptionPanel.add(box, BorderLayout.SOUTH);
		} else {
			descriptionButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the Tabs GUI
	 */
	private void refreshTabsGUI() {
		String title = TrackerRes.getString("ExportZipDialog.Border.Title.Tabs"); //$NON-NLS-1$
		tabsLabel.setText(title + ":"); //$NON-NLS-1$
		// get list of current tab titles and the TrackerPanel tab title
		String currentTabTitle = null;
		int currentTabNumber = 0;
		ArrayList<String> currentTabs = new ArrayList<String>();
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		for (int i = 0; i < frame.getTabCount(); i++) {
			String next = frame.getTabTitle(i);
			currentTabs.add(next);
			if (frame.getTrackerPanelForTab(i) == trackerPanel) {
				currentTabTitle = next;
				currentTabNumber = i;
			}
		}

		// add/remove checkboxes and entry fields if needed
		final String equalsign = " ="; //$NON-NLS-1$
		if (tabCheckboxes.size() > currentTabs.size()) {
			// remove tabs
			ArrayList<JCheckBox> tempboxes = new ArrayList<JCheckBox>();
			ArrayList<EntryField> tempfields = new ArrayList<EntryField>();
			// collect checkboxes and entry fields found in current tabs
			for (int i = 0; i < tabCheckboxes.size(); i++) {
				String s = tabCheckboxes.get(i).getText();
				if (s.endsWith(equalsign)) {
					s = s.substring(0, equalsign.length());
				}
				if (currentTabs.contains(s)) {
					tempboxes.add(tabCheckboxes.get(i));
					tempfields.add(tabTitleFields.get(i));
				}
			}
			tabCheckboxes = tempboxes;
			tabTitleFields = tempfields;
		}
		if (tabCheckboxes.size() < currentTabs.size()) {
			// add new tab checkboxes and fields at end
			// compare current tab names with previous existing tabs
			for (int i = 0; i < tabCheckboxes.size(); i++) {
				JCheckBox existing = tabCheckboxes.get(i);
				if (!existing.getText().equals(currentTabs.get(i) + equalsign)) {
					EntryField field = tabTitleFields.get(i);
					// strip extension for initial tab title
					String s = XML.stripExtension(currentTabs.get(i));
					if (!s.equals(TrackerRes.getString("TrackerPanel.NewTab.Name"))) { //$NON-NLS-1$
						field.setText(s);
					}
					field.setBackground(Color.WHITE);
				}
			}
			// add new checkboxes and entry fields
			for (int i = tabCheckboxes.size(); i < frame.getTabCount(); i++) {
				JCheckBox cb = new JCheckBox();
				cb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						refreshGUI();
						// request focus
						JCheckBox cb = (JCheckBox) e.getSource();
						cb.requestFocusInWindow();
					}
				});
				if (i == frame.getSelectedTab()) {
					cb.setSelected(true);
				}
				tabCheckboxes.add(cb);
				EntryField field = new EntryField() {
					@Override
					public Dimension getMaximumSize() {
						Dimension dim = super.getMaximumSize();
						dim.height = titleField.getPreferredSize().height;
						return dim;
					}
				};
				field.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						refreshGUI();
						// request focus
						EntryField field = (EntryField) e.getSource();
						field.requestFocusInWindow();
					}
				});
				// strip extension for initial tab title
				String s = XML.stripExtension(currentTabs.get(i));
				if (!s.equals(TrackerRes.getString("TrackerPanel.NewTab.Name"))) { //$NON-NLS-1$
					field.setText(s);
				}
				tabTitleFields.add(field);
				field.setBackground(Color.WHITE);
			}
		}

		// assemble tabs panel and update info label
		tabsPanel.removeAll();
		tabsPanel.add(tabsTitleBox, BorderLayout.NORTH);

		Box stack = Box.createVerticalBox();
		int selectedCount = 0;
		for (int i = 0; i < frame.getTabCount(); i++) {
			Box box = Box.createHorizontalBox();
			box.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
			stack.add(box);
			JLabel label = new JLabel(currentTabs.get(i));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
			JCheckBox checkbox = tabCheckboxes.get(i);
			// disable the checkbox of the current tab--always selected
			checkbox.setEnabled(i != currentTabNumber);
			EntryField field = tabTitleFields.get(i);
			field.setEnabled(checkbox.isSelected());

			box.add(checkbox);
			box.add(label);
			if (checkbox.isSelected()) {
				label.setText(label.getText() + equalsign);
				box.add(field);
			} else {
				box.add(Box.createHorizontalGlue());
			}

			if (checkbox.isSelected()) {
				selectedCount++;
				String tabName = tabTitleFields.get(i).getText().trim();
				if (tabName.length() == 0) {
					tabName = currentTabs.get(i);
				}
			}
		}

		String strippedTabTitle = XML.stripExtension(currentTabTitle);
		tabsInfoLabel.setText(selectedCount > 1 ? strippedTabTitle + " + " + (selectedCount - 1) : //$NON-NLS-1$
				strippedTabTitle);

		if (tabsPanel.getName() != null) {
			tabsButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			FontSizer.setFonts(stack, FontSizer.getLevel());
			tabsPanel.add(stack, BorderLayout.SOUTH);
		} else {
			tabsButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the Videos GUI
	 */
	private void refreshVideosGUI() {
		String title = TrackerRes.getString("ZipResourceDialog.Border.Title.Video"); //$NON-NLS-1$
		videoLabel.setText(title + ":"); //$NON-NLS-1$
		String info = ""; //$NON-NLS-1$
		if (!clipCheckbox.isEnabled()) { // no videos
			info = TrackerRes.getString("ExportZipDialog.Border.Title.None"); //$NON-NLS-1$
		} else {
			VideoType format = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
			if (format != null) {
				String ext = format.getDefaultExtension();
				info = (clipCheckbox.isSelected()
						? TrackerRes.getString("ExportZipDialog.Border.Title.TrimToClip") + " " + ext //$NON-NLS-1$ //$NON-NLS-2$
						: TrackerRes.getString("ExportZipDialog.Border.Title.CopyOriginal")); //$NON-NLS-1$
			}
		}
		videoInfoLabel.setText(info);

		// enable if any tabs have videos
		boolean hasVideo = frame.getTrackerPanelForID(panelID).getVideo() != null;
		for (int i = 0; i < frame.getTabCount(); i++) {
			hasVideo = hasVideo || (frame.getTrackerPanelForTab(i) != null && frame.getTrackerPanelForTab(i).getVideo() != null);
		}
		clipCheckbox.setEnabled(hasVideo);
		if (!hasVideo) {
			clipCheckbox.setSelected(false);
		}
		formatDropdown.setEnabled(clipCheckbox.isSelected());
		// clean and reassemble
		videoPanel.removeAll();
		videoPanel.add(videoTitleBox, BorderLayout.NORTH);

		if (videoPanel.getName() != null) { // show expanded options
			videoButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			Box panel = Box.createHorizontalBox();
			panel.add(clipCheckbox);
			panel.add(formatDropdown);
			panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 4));
			FontSizer.setFonts(panel, FontSizer.getLevel());
			videoPanel.add(panel, BorderLayout.SOUTH);
		} else { // set restore icon
			videoButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the Metadata GUI
	 */
	private void refreshMetadataGUI() {
		String title = TrackerRes.getString("ExportZipDialog.Label.Metadata.Text"); //$NON-NLS-1$
		metaLabel.setText(title + ":"); //$NON-NLS-1$

		// get info: metadata categories
		String info = ""; //$NON-NLS-1$
		// authorField, contactField, keywordsField, urlField, titleField, htmlField
		if (authorField.getText().trim().length() > 0) {
			info += TrackerRes.getString("PropertiesDialog.Label.Author") + ", "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (contactField.getText().trim().length() > 0) {
			info += TrackerRes.getString("PropertiesDialog.Label.Contact") + ", "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (keywordsField.getText().trim().length() > 0) {
			info += TrackerRes.getString("ZipResourceDialog.Label.Keywords") + ", "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (info.length() == 0) {
			info = TrackerRes.getString("ExportZipDialog.Border.Title.None"); //$NON-NLS-1$
		} else {
			info = info.substring(0, info.length() - 2);
		}
		metaInfoLabel.setText(info);

		metaPanel.removeAll();
		metaPanel.add(metaTitleBox, BorderLayout.NORTH);
		if (metaPanel.getName() != null) {
			metaButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			FontSizer.setFonts(metaFieldsBox, FontSizer.getLevel());
			metaPanel.add(metaFieldsBox, BorderLayout.SOUTH);
		} else {
			metaButton.setIcon(TViewChooser.RESTORE_ICON);
		}

	}

	/**
	 * Refreshes the Thumbnail GUI
	 */
	protected void refreshThumbnailGUI() {
		String title = TrackerRes.getString("ZipResourceDialog.Border.Title.Thumbnail"); //$NON-NLS-1$
		thumbLabel.setText(title + ":"); //$NON-NLS-1$

		Dimension dim = ThumbnailDialog.getDialog(frame.getTrackerPanelForID(panelID), false).getThumbnailSize();
		String info = dim.width + " x " + dim.height; //$NON-NLS-1$
		thumbInfoLabel.setText(info);

		thumbnailPanel.removeAll();
		thumbnailPanel.add(thumbTitleBox, BorderLayout.NORTH);
		if (thumbnailPanel.getName() != null) {
			thumbButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			JPanel panel = new JPanel();
			panel.add(thumbnailButton);
			panel.add(showThumbnailCheckbox);
			FontSizer.setFonts(panel, FontSizer.getLevel());
			thumbnailPanel.add(panel, BorderLayout.CENTER);
			if (showThumbnailCheckbox.isSelected()) {
				refreshThumbnailImage();
				thumbnailPanel.add(thumbnailImagePanel, BorderLayout.SOUTH);
			}
		} else {
			thumbButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the SupportFiles GUI
	 */
	private void refreshSupportFilesGUI() {
		String title = TrackerRes.getString("ExportZipDialog.Border.Title.SupportFiles"); //$NON-NLS-1$
		supportFilesLabel.setText(title + ":"); //$NON-NLS-1$

		String info = fileNames.size() + ""; //$NON-NLS-1$
		if (fileNames.size() == 0) {
			info = TrackerRes.getString("ExportZipDialog.Border.Title.None"); //$NON-NLS-1$
		}
		supportFilesInfoLabel.setText(info);

		supportFilesPanel.removeAll();
		supportFilesPanel.add(supportFilesTitleBox, BorderLayout.NORTH);

		if (supportFilesPanel.getName() != null) {
			supportFilesButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			FontSizer.setFonts(supportFilesBox, FontSizer.getLevel());
			supportFilesPanel.add(supportFilesBox, BorderLayout.SOUTH);
		} else {
			supportFilesButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the Advanced GUI
	 */
	private void refreshAdvancedGUI() {
		String title = TrackerRes.getString("ExportZipDialog.Label.Advanced.Text"); //$NON-NLS-1$
		advancedLabel.setText(title + ":"); //$NON-NLS-1$

		String info = ""; //$NON-NLS-1$
		if (urlField.getText().trim().length() > 0) {
			info += TrackerRes.getString("ZipResourceDialog.Label.Link") + ", "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (htmlField.getText().trim().length() > 0) {
			info += TrackerRes.getString("ZipResourceDialog.Label.HTML") + ", "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (info.length() == 0) {
			info = TrackerRes.getString("ExportZipDialog.Border.Title.None"); //$NON-NLS-1$
		} else {
			info = info.substring(0, info.length() - 2);
		}
		advancedInfoLabel.setText(info);

		advancedPanel.removeAll();
		advancedPanel.add(advancedTitleBox, BorderLayout.NORTH);

		if (advancedPanel.getName() != null) {
			advancedButton.setIcon(TViewChooser.MAXIMIZE_ICON);
			FontSizer.setFonts(advancedFieldsBox, FontSizer.getLevel());
			advancedPanel.add(advancedFieldsBox, BorderLayout.SOUTH);
		} else {
			advancedButton.setIcon(TViewChooser.RESTORE_ICON);
		}
	}

	/**
	 * Refreshes the file list.
	 */
	private void refreshFileList() {
		fileListModel.clear();
		fileNames.clear();
		for (File next : addedFiles) {
			fileNames.add(next.getName());
		}
		for (String next : fileNames) {
			fileListModel.addElement(next);
		}
	}

	/**
	 * Refreshes the format dropdown.
	 */
	private void refreshFormatDropdown() {
		TrackerIO.refreshVideoFormats();
		String selected = TrackerIO.getVideoFormat(preferredExtension);
		formatDropdown.removeAllItems();
  	String zipped = MediaRes.getString("ZipImageVideoType.Description.Zipped");
		for (Object format : TrackerIO.getVideoFormats()) {
			String desc = (String)format;
			if (!desc.startsWith(zipped))
				formatDropdown.addItem(format);
		}
		formatDropdown.setSelectedItem(selected);
	}

	/**
	 * Refreshes the text fields by reading the HTML code of a file.
	 * 
	 * @param htmlFile the file to read
	 */
	private void refreshFieldsFromHTML(File htmlFile) {
		String html = ResourceLoader.getString(htmlFile.getAbsolutePath());
		if (html == null)
			return;
		String title = ResourceLoader.getTitleFromHTMLCode(html);
		if (title != null) {
			titleField.setText(title);
			titleField.setBackground(Color.white);
		}
		ArrayList<String[]> metadata = LibraryBrowser.getMetadataFromHTML(html);
		for (int i = metadata.size() - 1; i >= 0; i--) {
			// go backwards so if multiple authors, first one in the list is only one
			// changed
			String[] next = metadata.get(i);
			String key = next[0];
			String value = next[1];
			if (LibraryResource.META_AUTHOR.toLowerCase().contains(key.toLowerCase())) {
				if ("".equals(authorField.getText().trim())) { //$NON-NLS-1$
					authorField.setText(value);
					authorField.setBackground(Color.white);
				}
			} else if (LibraryResource.META_CONTACT.toLowerCase().contains(key.toLowerCase())) {
				if ("".equals(contactField.getText().trim())) { //$NON-NLS-1$
					contactField.setText(value);
					contactField.setBackground(Color.white);
				}
			} else if (LibraryResource.META_KEYWORDS.toLowerCase().contains(key.toLowerCase())) {
				keywordsField.setText(value);
				keywordsField.setBackground(Color.white);
			} else if ("description".contains(key.toLowerCase())) { //$NON-NLS-1$
				descriptionPane.setText(value);
				descriptionPane.setBackground(Color.white);
			} else if ("url".contains(key.toLowerCase())) { //$NON-NLS-1$
				urlField.setText(value);
				urlField.setBackground(Color.white);
			}
		}
	}

	/**
	 * Add videos and TRK files to the zip list.
	 * 
	 * @param zipList the list of files to be zipped
	 */
	private void addVideosAndTRKs(final ArrayList<File> zipList) {
		// save VideoIO preferred export format
		videoIOPreferredExtension = VideoIO.getPreferredExportExtension();

		// process TrackerPanels according to checkbox status
		int nTabs = 0;
		for (int i = 0; i < tabCheckboxes.size(); i++) {
			if (tabCheckboxes.get(i).isSelected())
				nTabs++;
		}
		// collect trkPaths to prevent duplicate path names
		ArrayList<String> trkPaths = new ArrayList<>();
		ArrayList<Export> exports = new ArrayList<>();
		for (int i = 0; i < tabCheckboxes.size(); i++) {
			JCheckBox box = tabCheckboxes.get(i);
			if (!box.isSelected())
				continue;
			TrackerPanel panel = frame.getTrackerPanelForTab(i);
			if (panel == null)
				return;
			// get tab title to add to video and TRK names
			String tabTitle = (i >= tabTitleFields.size() ? null : tabTitleFields.get(i).getText().trim());
			if ("".equals(tabTitle) && nTabs == 1) { //$NON-NLS-1$
				tabTitle = titleField.getText().trim();
			}
			String trkPath = getTRKTarget(tabTitle, trkPaths);
			String originalPath = null;
			ExportVideoDialog exporter = null;
			String videoPath = null;

			// export or copy video, if any
			Video vid = panel.getVideo();
			if (vid != null) {
				originalPath = (String) vid.getProperty("absolutePath"); //$NON-NLS-1$
				if (clipCheckbox.isSelected()) {
					// export video clip using videoExporter
					// define the path for the exported video
					if (videoExporter == null)
						videoExporter = ExportVideoDialog.getVideoDialog(panel);
					exporter = videoExporter;
				} else if (originalPath != null) { // $NON-NLS-1$
					// copy or extract original video to target directory
					videoPath = videoSubdirectory + File.separator + XML.getName(originalPath);
					String tempPath = getTempDirectory() + videoPath; // $NON-NLS-1$
					// check if target video file already exists
					boolean videoexists = new File(tempPath).exists();
					
					// deal with zipped images
					String[] imagePaths = VideoIO.getZippedImagePaths(originalPath); // absolute
					if (imagePaths != null) {
						videoexists = new File(imagePaths[0]).exists();						
					}
					if (!videoexists) {
						// must copy video file(s)
						new File(getTempDirectory() + videoSubdirectory).mkdirs();
						if (imagePaths != null) {
							originalPath = imagePaths[0];
							for (int k = 0; k < imagePaths.length; k++) {
								String vidPath = videoSubdirectory + File.separator + XML.getName(imagePaths[k]);
								tempPath = getTempDirectory() + vidPath; // $NON-NLS-1$
								if (k == 0)
									videoPath = tempPath;
								if (!createTarget(imagePaths[k], new File(tempPath)))
									return;
							}
						}
						else if (!createTarget(originalPath, new File(tempPath)))
							return;
					}
				}
			}
			exports.add(new Export(zipList, tabTitle, originalPath, 
					trkPath, videoPath, exporter, panel));
		}
		exportIterator = exports.iterator();
	}

	protected boolean createTarget(String path, File target) {
		if (copyOrExtractFile(path, target))
			return true;
		javax.swing.JOptionPane.showMessageDialog(ExportZipDialog.this,
				TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Message"), //$NON-NLS-1$
				TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Title"), //$NON-NLS-1$
				javax.swing.JOptionPane.ERROR_MESSAGE);
		return false;
	}

	/**
	 * Adds "added files" to the zip list
	 * 
	 * @param zipList the list of files to be zipped
	 */
	private void addFiles(ArrayList<File> zipList) {

		// add "added files"
		// some may be in zip or TRZ files
		for (File file : addedFiles) {
			String path = file.getAbsolutePath();
			boolean isHTML = XML.getExtension(path).startsWith("htm"); //$NON-NLS-1$
			if (isHTML) {
				copyAndAddHTMLPage(path, zipList);
			} else {
				String dir = getTempDirectory();
				File targetFile = new File(dir, XML.getName(path));
				if (copyOrExtractFile(path, targetFile)) {
					zipList.add(targetFile);
				}
			}
		}

	}

	/**
	 * Saves a zip resource to a target defined with a file chooser
	 */
	protected void saveZipAs() {
		String description = descriptionPane.getText().trim();
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (!"".equals(description) && "".equals(trackerPanel.getDescription())) { //$NON-NLS-1$ //$NON-NLS-2$
			trackerPanel.setDescription(description);
			trackerPanel.hideDescriptionWhenLoaded = true;
		}
		// if saving clip, warn if there are particle models with start frames not
		// included in clip
		if (clipCheckbox.isSelected()) {
			badModels = getModelsNotInClips();
			if (!badModels.isEmpty()) {
				// show names of bad models and offer to exclude them from export
				String names = ""; //$NON-NLS-1$
				for (ParticleModel next : badModels) {
					if (!"".equals(names)) { //$NON-NLS-1$
						names += ", "; //$NON-NLS-1$
					}
					names += "'" + next.getName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				int response = javax.swing.JOptionPane.showConfirmDialog(frame,
						TrackerRes.getString("ZipResourceDialog.BadModels.Message1") //$NON-NLS-1$
								+ "\n" + TrackerRes.getString("ZipResourceDialog.BadModels.Message2") //$NON-NLS-1$ //$NON-NLS-2$
								+ "\n" + TrackerRes.getString("ZipResourceDialog.BadModels.Message3") //$NON-NLS-1$ //$NON-NLS-2$
								+ "\n\n" + names //$NON-NLS-1$
								+ "\n\n" + TrackerRes.getString("ZipResourceDialog.BadModels.Question"), //$NON-NLS-1$ //$NON-NLS-2$
						TrackerRes.getString("ZipResourceDialog.BadModels.Title"), //$NON-NLS-1$
						javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
				if (response != javax.swing.JOptionPane.YES_OPTION) {
					return;
				}
			}
		}
		else { // copying video
			// warn if image videos include non-sequence images
			Integer[] badImageVideoTabs = getTabsWithUnexportableImages();
			if (badImageVideoTabs.length > 0) {
				// show names of bad models and offer to exclude them from export
				String names = ""; //$NON-NLS-1$
				for (Integer next : badImageVideoTabs) {
					if (!"".equals(names)) { //$NON-NLS-1$
						names += ", "; //$NON-NLS-1$
					}
					names += "'" + frame.getTabTitle(next) + "'"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				int response = javax.swing.JOptionPane.showConfirmDialog(frame,
						TrackerRes.getString("ExportZipDialog.BadImageVideos.Message1") //$NON-NLS-1$
								+ "\n" + TrackerRes.getString("ExportZipDialog.BadImageVideos.Message2") //$NON-NLS-1$ //$NON-NLS-2$
								+ "\n" + TrackerRes.getString("ExportZipDialog.BadImageVideos.Message3") //$NON-NLS-1$ //$NON-NLS-2$
								+ "\n\n" + names //$NON-NLS-1$
								+ "\n\n" + TrackerRes.getString("ExportZipDialog.BadImageVideos.Question"), //$NON-NLS-1$ //$NON-NLS-2$
						TrackerRes.getString("ExportZipDialog.BadImageVideos.Title"), //$NON-NLS-1$
						javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
				if (response != javax.swing.JOptionPane.YES_OPTION) {
					return;
				}
				
			}
		}

		// define the target filename and create empty zip list
		ArrayList<File> zipList = defineTarget();
		if (zipList == null)
			return;
		setVisible(false);

		// use separate thread to add files to the ziplist and create the TRZ file
		new Thread(new Runnable() {
			@Override
			public void run() {
				saveZipAction(zipList);
			}
		}).start();
	}

	/**
	 * Saves a zip resource containing the files in the list.
	 * 
	 * @param zipList the list of files to be zipped
	 */
	private void saveZip(ArrayList<File> zipList) {
		// define zip target and compress with JarTool
		File target = new File(getZIPTarget());
		if (JarTool.compress(zipList, target, null)) {
			ResourceLoader.removeFromZipCache(target.getPath());
			// offer to open the newly created zip file
//			if (!isOpenInTracker)
				openNewZip(target.getAbsolutePath());
			// delete temp directory after short delay
			if (!OSPRuntime.isJS) {
				OSPRuntime.trigger(1000, (e) -> {
					ResourceLoader.deleteFile(new File(getTempDirectory()));
				});
			}
		}
	}

	/**
	 * Writes an HTML info file from scratch, using the current field text.
	 */
	private File writeHTMLInfo(String thumbPath, String redirectPath) {
		File htmlTarget = new File(getHTMLDirectory());
		htmlTarget.mkdirs();
		htmlTarget = new File(htmlTarget, targetName + "_info.html"); //$NON-NLS-1$
		thumbPath = XML.getPathRelativeTo(thumbPath, getHTMLDirectory());
		String title = titleField.getText().trim();
		String description = descriptionPane.getText().trim();
		String author = authorField.getText().trim();
		String contact = contactField.getText().trim();
		String keywords = keywordsField.getText().trim();
		String uri = urlField.getText().trim();

		Map<String, String> metadata = new TreeMap<String, String>();
		if (!"".equals(author)) //$NON-NLS-1$
			metadata.put("author", author); //$NON-NLS-1$
		if (!"".equals(contact)) //$NON-NLS-1$
			metadata.put("contact", contact); //$NON-NLS-1$
		if (!"".equals(keywords)) //$NON-NLS-1$
			metadata.put("keywords", keywords); //$NON-NLS-1$
		if (!"".equals(description)) //$NON-NLS-1$
			metadata.put("description", description); //$NON-NLS-1$
		if (!"".equals(uri)) //$NON-NLS-1$
			metadata.put("URL", uri); //$NON-NLS-1$

		String htmlCode = LibraryResource.getHTMLCode(title, LibraryResource.TRACKER_TYPE, thumbPath, description,
				author, contact, uri, null, metadata);

		// insert redirect comment immediately after <html> tag
		if (redirectPath != null) {
			String comment = "\n<!--redirect: " + redirectPath + "-->"; //$NON-NLS-1$ //$NON-NLS-2$
			int n = htmlCode.indexOf("<html>"); //$NON-NLS-1$
			htmlCode = htmlCode.substring(0, n + 6) + comment + htmlCode.substring(n + 6);
		}

		return writeFile(htmlCode, htmlTarget);
	}

	/**
	 * Writes a text file.
	 * 
	 * @param text   the text
	 * @param target the File to write
	 * @return the written File, or null if failed
	 */
	private File writeFile(String text, File target) {
		try {
			FileWriter fout = new FileWriter(target);
			fout.write(text);
			fout.close();
			return target;
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * Writes a thumbnail image to the temp directory and adds it to the zip list.
	 * 
	 * @param zipList the list of files to be zipped
	 */
	private void saveZipAction(ArrayList<File> zipList) {
		// use ThumbnailDialog to write image to temp folder and add to zip list
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		ThumbnailDialog dialog = ThumbnailDialog.getDialog(trackerPanel, false);
		String ext = dialog.getFormat();
		String thumbPath = getTempDirectory() + targetName + "_thumbnail." + ext; //$NON-NLS-1$
		File thumbnail = dialog.saveThumbnail(thumbPath);
		if (thumbnail != null) {
			zipList.add(thumbnail);
			addHTMLInfo(thumbPath, zipList);
		}
		addVideosAndTRKs(zipList);
		nextExport(zipList);
	}

	/**
	 * Copies, downloads or extracts a file to a target.
	 * 
	 * @param filePath   the path
	 * @param targetFile the target file
	 * @return true if successful
	 */
	private boolean copyOrExtractFile(String filePath, File targetFile) {
		String lowercase = filePath.toLowerCase();
		// if file is on server, download it
		if (ResourceLoader.isHTTP(filePath)) {
			targetFile = ResourceLoader.download(filePath, targetFile, false);
		}
		// if file is in zip or jar, then extract it
		else if (lowercase.contains("trz!") || lowercase.contains("jar!") || lowercase.contains("zip!")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			targetFile = ResourceLoader.extract(filePath, targetFile);
		}
		// otherwise copy it
		else
			ResourceLoader.copyFile(new File(filePath), targetFile, 100000);
		return targetFile.exists();
	}

	/**
	 * Adds an appropriate HTML info file to the temp directory and adds it to the
	 * zip list. This looks first for a file specified in the htmlField, then for a
	 * file with name "zipName_info.html", and failing that writes a file from
	 * scratch.
	 * 
	 * @param thumbPath the path to the thumbnail image
	 * @param zipList   the list of files to be zipped
	 * @return true if succeeds
	 */
	private boolean addHTMLInfo(String thumbPath, ArrayList<File> zipList) {
		// see if HTML info resource is defined in htmlField
		Resource res = ResourceLoader.getResource(htmlField.getText().trim());
		if (res == null && !OSPRuntime.isJS) {
			// look for HTML info resource in target directory
			File[] files = new File(targetDirectory).listFiles();
			boolean added = false;
			for (File next : files) {
				String name = XML.stripExtension(next.getName());
				String ext = XML.getExtension(next.getName());
				if ("html".equals(ext) || "htm".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$
					if (name.equals(targetName) || name.equals(targetName + "_info")) { //$NON-NLS-1$
						// look first in added files
						for (File file : addedFiles) {
							added = added || file.getName().equals(next.getName());
						}
						if (!added) {
							// offer to add HTML to zip
							int response = javax.swing.JOptionPane.showConfirmDialog(frame,
									TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Message1") //$NON-NLS-1$
											+ " \"" + next.getName() + "\"\n" //$NON-NLS-1$ //$NON-NLS-2$
											+ TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Message2"), //$NON-NLS-1$
									TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Title"), //$NON-NLS-1$
									javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
							if (response == javax.swing.JOptionPane.YES_OPTION) {
								res = ResourceLoader.getResource(next.getAbsolutePath());
							}
						}
					}
				}
			}
		}

		String redirect = null; // used below in writeHTMLInfo method
		if (res != null) {
			if (res.getFile() != null) {
				// resource is a local file, so write temp target and add to zip list
				String html = res.getString();
				if (html != null && html.trim().startsWith("<!DOCTYPE html")) { //$NON-NLS-1$
					File htmlTarget = writeTempHTMLTarget(html, res);
					if (htmlTarget != null) {
						String path = copyAndAddHTMLPage(htmlTarget.getAbsolutePath(), zipList);
						if (!htmlTarget.equals(res.getFile()))
							htmlTarget.delete();
						return path != null;
					}
				}
			} else { // resource is a web file
				redirect = res.getAbsolutePath();
			}
		}

		// write HTML info from scratch
		// if user has not defined a project name (title), then use targetName
		boolean empty = "".equals(titleField.getText().trim()); //$NON-NLS-1$
		if (empty) {
			titleField.setText(targetName);
		}
		File htmlTarget = writeHTMLInfo(thumbPath, redirect);
		if (empty) {
			titleField.setText(""); //$NON-NLS-1$
			titleField.setBackground(Color.white);
		}
		if (htmlTarget == null)
			return false;
		if (!"".equals(htmlSubdirectory)) { //$NON-NLS-1$
			htmlTarget = htmlTarget.getParentFile();
		}
		zipList.add(htmlTarget);
		return true;
	}

	/**
	 * Returns a list of particle models with start frames not included in the video
	 * clips. These models cannot be exported.
	 */
	private ArrayList<ParticleModel> getModelsNotInClips() {
		ArrayList<ParticleModel> allModels = new ArrayList<ParticleModel>();
		// process TrackerPanels according to checkbox status
		for (int i = 0; i < tabCheckboxes.size(); i++) {
			JCheckBox box = tabCheckboxes.get(i);
			if (!box.isSelected())
				continue;
			TrackerPanel panel = frame.getTrackerPanelForTab(i);
			if (panel == null)
				continue;
			VideoClip clip = panel.getPlayer().getVideoClip();
			ArrayList<ParticleModel> models = panel.getDrawablesTemp(ParticleModel.class);
			for (Iterator<ParticleModel> it = models.iterator(); it.hasNext();) {
				ParticleModel model = it.next();
				if (clip.includesFrame(model.getStartFrame())) {
					it.remove();
				}
			}
			models.clear();
			allModels.addAll(models);
		}
		return allModels;
	}

	/**
	 * Returns a list of tabs with image videos that have non-sequentially numbered images.
	 */
	private Integer[] getTabsWithUnexportableImages() {
		ArrayList<Integer> tabs = new ArrayList<Integer>();
		// process TrackerPanels according to checkbox status
		for (int i = 0; i < tabCheckboxes.size(); i++) {
			JCheckBox box = tabCheckboxes.get(i);
			if (!box.isSelected())
				continue;
			TrackerPanel panel = frame.getTrackerPanelForTab(i);
			if (panel == null)
				continue;
			Video video = panel.getVideo();
			if (video instanceof ImageVideo) {
				ImageVideo iv = (ImageVideo)video;
				if (!iv.isFileBased()) {
					if (!iv.saveInvalidImages()) {
						tabs.add(i);
						continue;
					}
				}
				String[] paths = iv.getValidPaths();
				if (paths.length > 1) {
					String imagePath = paths[0];
					for (int k = 1; k < paths.length; k++) {
						String next = ImageVideo.getNextImagePathInSequence(imagePath);
						if (!paths[k].equals(next)) {
							tabs.add(i);
							break;
						}
						imagePath = paths[k];
					}				
				}
			}
		}
		return tabs.toArray(new Integer[tabs.size()]);
	}

	/**
	 * Returns a list of local HTML paths found in the specified XMLControl.
	 * 
	 * @param control XMLControl for a TrackerPanel
	 * @return the list
	 */
	private ArrayList<String> getHTMLPaths(XMLControl control) {
		ArrayList<String> pageViews = new ArrayList<String>(); // html pages used in page views
		// extract page view HTML paths
		String xml = control.toXML();
		int j = xml.indexOf("PageTView$TabView"); //$NON-NLS-1$
		while (j > -1) { // page view exists
			xml = xml.substring(j + 17);
			String s = "<property name=\"text\" type=\"string\">"; //$NON-NLS-1$
			j = xml.indexOf(s);
			if (j > -1) {
				xml = xml.substring(j + s.length());
				j = xml.indexOf("</property>"); //$NON-NLS-1$
				String text = xml.substring(0, j);
				Resource res = ResourceLoader.getResource(text);
				if (res != null && res.getFile() != null) { // exclude web files
					pageViews.add(text);
				}
			}
			j = xml.indexOf("PageTView$TabView"); //$NON-NLS-1$
		}
		return pageViews;
	}

	/**
	 * Returns a list of local image paths found in the specified HTML document.
	 * 
	 * @param html     the HTML code
	 * @param basePath the absolute path to the parent directory of the HTML file
	 * @param pre      a String that should precede image paths
	 * @param post     a String that should follow image paths
	 * @return the list
	 */
	private ArrayList<String> getImagePaths(String html, String basePath, String pre, String post) {
		ArrayList<String> images = new ArrayList<String>();
		// extract image paths from html text
		int j = html.indexOf(pre);
		while (j > -1) { // image reference found
			html = html.substring(j + pre.length());
			j = html.indexOf(post);
			if (j > -1) {
				String text = html.substring(0, j); // the image path specified in the html itself
				String path = XML.getResolvedPath(text, basePath);
				Resource res = ResourceLoader.getResource(path);
				if (res != null && res.getFile() != null) { // exclude web files
					images.add(text);
				}
			}
			j = html.indexOf(pre);
		}
		return images;
	}

	/**
	 * Copies an HTML file to the temp directory and adds the copy to the target
	 * list.
	 * 
	 * @param htmlPath the path to the original HTML file
	 * @param zipList  the list of files to be zipped
	 * @return the relative path to the copy, or null if failed
	 */
	private String copyAndAddHTMLPage(String htmlPath, ArrayList<File> zipList) {
		// read html text
		String html = null;
		Resource res = ResourceLoader.getResource(htmlPath);
		if (res != null) {
			html = res.getString();
		}
		if (html != null) {
			String htmlBasePath = XML.getDirectoryPath(htmlPath);
			// get target directory
			File htmlTarget = new File(getHTMLDirectory());
			htmlTarget.mkdirs();
			// add image files
			String pre = "<img src=\""; //$NON-NLS-1$
			String post = "\""; //$NON-NLS-1$
			ArrayList<String> imagePaths = getImagePaths(html, htmlBasePath, pre, post);
			if (!imagePaths.isEmpty()) {
				// copy images into target directory and modify html text
				File imageDir = new File(getImageDirectory());
				imageDir.mkdirs();
				for (String next : imagePaths) {
					String path = XML.getResolvedPath(next, htmlBasePath);
					res = ResourceLoader.getResource(path);
					// copy image and determine its path relative to target
					File imageTarget = new File(imageDir, XML.getName(next));
					if (res.getFile() != null) {
						ResourceLoader.copyFile(res.getFile(), imageTarget);
					}
					path = XML.getPathRelativeTo(imageTarget.getAbsolutePath(), getHTMLDirectory());
					html = substitutePathInText(html, next, path, pre, post);
				}
				zipList.add(imageDir);
			}

			// if local stylesheet is found, copy it
			String css = ResourceLoader.getStyleSheetFromHTMLCode(html);
			if (css != null && !ResourceLoader.isHTTP(css)) {
				res = ResourceLoader.getResource(XML.getResolvedPath(css, htmlBasePath));
				if (res != null) {
					// copy css file into HTMLTarget directory
					String cssName = XML.getName(css);
					File cssTarget = new File(htmlTarget, XML.getName(cssName));
					ResourceLoader.copyFile(res.getFile(), cssTarget);
					// substitute cssName in html
					html = substitutePathInText(html, css, cssName, "\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			// write modified html text into target file
			htmlTarget = new File(htmlTarget, XML.getName(htmlPath));
			try {
				FileWriter fout = new FileWriter(htmlTarget);
				fout.write(html);
				fout.close();
				String relPath = XML.getPathRelativeTo(htmlTarget.getAbsolutePath(), getTempDirectory());
				if (!"".equals(htmlSubdirectory)) { //$NON-NLS-1$
					htmlTarget = htmlTarget.getParentFile();
				}
				zipList.add(htmlTarget);
				return relPath;
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Substitutes one path string for another in a body of text so long as the path
	 * is preceded by the "pre" string and followed by the "post" string.
	 * 
	 * @param text     the body of text
	 * @param prevPath the path to replace
	 * @param newPath  the new path
	 * @param pre      a String that precedes the path
	 * @param post     a String that follows the path
	 * @return the modified text
	 */
	private String substitutePathInText(String text, String prevPath, String newPath, String pre, String post) {
		if (prevPath.equals(newPath))
			return text;
		int i = text.indexOf(pre + prevPath + post);
		while (i > 0) {
			text = text.substring(0, i + pre.length()) + newPath + text.substring(i + pre.length() + prevPath.length());
			i = text.indexOf(pre + prevPath + post);
		}
		return text;
	}

	/**
	 * Offers to open a newly saved zip file.
	 *
	 * @param path the path to the zip file
	 */
	private void openNewZip(final String path) {
		// BH Can't reload a saved file in SwingJS
		if (OSPRuntime.isJS)
			return;
		Runnable runner1 = new Runnable() {
			@Override
			public void run() {
				int response = javax.swing.JOptionPane.showConfirmDialog(frame,
						TrackerRes.getString("ZipResourceDialog.Complete.Message1") //$NON-NLS-1$
								+ " \"" + XML.getName(path) + "\".\n" //$NON-NLS-1$ //$NON-NLS-2$
								+ TrackerRes.getString("ZipResourceDialog.Complete.Message2"), //$NON-NLS-1$
						TrackerRes.getString("ZipResourceDialog.Complete.Title"), //$NON-NLS-1$
						javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
				if (response == javax.swing.JOptionPane.YES_OPTION) {
					Runnable runner = new Runnable() {
						@Override
						public void run() {
							frame.doOpenExportedAndUpdateLibrary(path);
						}
					};
					SwingUtilities.invokeLater(runner);
				}
			}
		};
		SwingUtilities.invokeLater(runner1);
	}

	/**
	 * Uses a file chooser to define a new target name and directory.
	 *
	 * @return empty List<File> to fill with files to be zipped
	 */
	protected ArrayList<File> defineTarget() {
		if (lastTRZ == null || lastTRZ.getName().trim().length() < 2) {
			String title = titleField.getText().trim();
			if (!"".equals(title)) {
				lastTRZ = new File(title);
			}
			else {
				String tabtitle = frame.getTabTitle(frame.getSelectedTab());			
				if (!"".equals(tabtitle)) {
					lastTRZ = new File(tabtitle);
				}
				else
					lastTRZ = new File("");
			}
		}
		// show file chooser to get directory and zip name
		AsyncFileChooser chooser = TrackerIO.getChooser();
		chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.SaveZip.Title")); //$NON-NLS-1$
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(VideoIO.trzFileFilter);
		chooser.setFileFilter(VideoIO.trzFileFilter);
//    String title = titleField.getText().trim().replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
//    if (!"".equals(title)) { //$NON-NLS-1$
//    	chooser.setSelectedFile(new File(title+".trz")); //$NON-NLS-1$
//    }
		chooser.setAccessory(null);
		chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(lastTRZ);
		int result = chooser.showSaveDialog(null);
		if (result != JFileChooser.APPROVE_OPTION) {
			chooser.setSelectedFile(lastTRZ = new File("")); //$NON-NLS-1$
			chooser.resetChoosableFileFilters();
			return null;
		}
		File chooserFile = lastTRZ = chooser.getSelectedFile();
		// DB Sep 23 2021 now we CAN overwrite TRZ files since not caching streams
		// check that target is not currently open in Tracker--can't overwrite open TRZ
		isOpenInTracker = false;
		if (!OSPRuntime.isJS && chooserFile.exists()) {
			for (int i = 0; i < frame.getTabCount(); i++) {
				String path = frame.getTrackerPanelForTab(i).openedFromPath;
				if (path != null && path.equals(XML.forwardSlash(chooserFile.getPath()))) {
					isOpenInTracker = true;
//					javax.swing.JOptionPane.showMessageDialog(frame,
//							TrackerRes.getString("ExportZipDialog.Dialog.CannotOverwrite.Message"), //$NON-NLS-1$
//							TrackerRes.getString("ExportZipDialog.Dialog.CannotOverwrite.Title"), //$NON-NLS-1$
//							javax.swing.JOptionPane.WARNING_MESSAGE);
//					return defineTarget();
				}
			}
		}
		if (!TrackerIO.canWrite(chooserFile)) {
			return null;
		}
		chooser.resetChoosableFileFilters();

		// define target filename and check for reserved characters, including spaces
		targetName = XML.stripExtension(chooserFile.getName());
		String[] reserved = new String[] { "/", "\\", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<", ">", "\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"|", ":", "*", "%" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		for (String next : reserved) {
			if (targetName.indexOf(next) > -1) {
				String list = ""; //$NON-NLS-1$
				for (int i = 1; i < reserved.length; i++) {
					list += "    " + reserved[i]; //$NON-NLS-1$
				}
				javax.swing.JOptionPane.showMessageDialog(frame,
						TrackerRes.getString("ZipResourceDialog.Dialog.BadFileName.Message") + "\n" + list, //$NON-NLS-1$ //$NON-NLS-2$
						TrackerRes.getString("ZipResourceDialog.Dialog.BadFileName.Title"), //$NON-NLS-1$
						javax.swing.JOptionPane.WARNING_MESSAGE);
				return null;
			}
		}

		// define target directory and extension
		targetDirectory = chooserFile.getParent() + "/"; //$NON-NLS-1$
		targetExtension = "trz"; //$NON-NLS-1$
		String ext = XML.getExtension(chooserFile.getName());

		// check for duplicate file if target extension not used
		if (!targetExtension.equals(ext)) {
			File file = new File(XML.stripExtension(chooserFile.getAbsolutePath()) + "." + targetExtension); //$NON-NLS-1$
			if (!TrackerIO.canWrite(file))
				return null;
		}

		// clear target video and return empty list
		targetVideo = null;
		return new ArrayList<File>() {
			@Override
			public boolean add(File f) {
				if (!contains(f))
					super.add(f);
				return true;
			}
		};
	}

	private String getTRKTarget(String tabTitle, ArrayList<String> existingTabTitles) {
		String path = null;
		String tempDir = getTempDirectory();
		if (tabTitle == null || "".equals(tabTitle.trim())) { //$NON-NLS-1$
			path = tempDir + targetName;
		} else {
			path = tempDir + targetName + "_" + tabTitle; //$NON-NLS-1$
		}
		int append = 0;
		int len = path.length();
		while (existingTabTitles.contains(path)) {
			append++;
			path = path.substring(0, len) + append;
		}
		existingTabTitles.add(path);
		return path + ".trk"; //$NON-NLS-1$
	}

	private String getVideoTarget(String trkName, String extension) {
		String vidDir = getTempDirectory() + videoSubdirectory;
		new File(vidDir).mkdirs();
		String videoName = XML.stripExtension(trkName) + "." + extension; //$NON-NLS-1$
		return vidDir + File.separator + videoName; // $NON-NLS-1$
	}

	private String getZIPTarget() {
		return targetDirectory + targetName + "." + targetExtension; //$NON-NLS-1$
	}

	private String getHTMLDirectory() {
		return getTempDirectory() + htmlSubdirectory + "/"; //$NON-NLS-1$
	}

	private String getImageDirectory() {
		return getTempDirectory() + imageSubdirectory + "/"; //$NON-NLS-1$
	}

	private String tempDir;

	private String getTempDirectory() {
		if (tempDir == null)
			tempDir = new File(System.getProperty("java.io.tmpdir"), "tracker" + new Random().nextInt()).toString()
					+ File.separator;
		return tempDir;
	}

	protected class VideoListener implements PropertyChangeListener {

		ArrayList<File> target;
		ExportVideoDialog dialog;

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ExportVideoDialog.PROPERTY_EXPORTVIDEO_VIDEOSAVED) && target != null) { // $NON-NLS-1$
				// event's new value is saved file name (differ from original target name for
				// image videos)
				targetVideo = e.getNewValue().toString();
				// save video extension
				preferredExtension = XML.getExtension(targetVideo);
				// restore VideoIO preferred extension
				VideoIO.setPreferredExportExtension(videoIOPreferredExtension);

//	      saveZip(target);
			}
			// clean up ExportVideoDialog
			if (dialog != null) {
				dialog.removePropertyChangeListener("video_saved", videoExportListener); //$NON-NLS-1$
				dialog.removePropertyChangeListener("video_cancelled", videoExportListener); //$NON-NLS-1$
			}
		}

		void setTargetList(ArrayList<File> list) {
			target = list;
		}

		void setDialog(ExportVideoDialog evd) {
			dialog = evd;
		}
	}

	/**
	 * Replaces metadata in HTML code based on current text in metadata fields and
	 * writes the result to a temporary file that is added to the jar, then deleted.
	 * 
	 * @param htmlCode the HTML code
	 * @param res      the (local File) Resource that is the source of the html code
	 * @return the modified code
	 */
	private File writeTempHTMLTarget(String htmlCode, Resource res) {
		if (htmlCode == null || res.getFile() == null)
			return null;
		String title = ResourceLoader.getTitleFromHTMLCode(htmlCode);
		String newTitle = titleField.getText().trim();
		if (!"".equals(newTitle) && !newTitle.equals(title)) { //$NON-NLS-1$
			title = "<title>" + title + "</title>"; //$NON-NLS-1$ //$NON-NLS-2$
			newTitle = "<title>" + newTitle + "</title>"; //$NON-NLS-1$ //$NON-NLS-2$
			htmlCode = htmlCode.replace(title, newTitle);
		}
		ArrayList<String[]> metadata = LibraryBrowser.getMetadataFromHTML(htmlCode);
		for (String type : LibraryResource.META_TYPES) {
			String newValue = type.equals(LibraryResource.META_AUTHOR) ? authorField.getText().trim()
					: type.equals(LibraryResource.META_CONTACT) ? contactField.getText().trim()
							: type.equals(LibraryResource.META_KEYWORDS) ? keywordsField.getText().trim() : null;
			String prevValue = null;
			String key = null;
			boolean found = false;
			for (String[] next : metadata) {
				if (found)
					break;
				key = next[0];
				if (type.toLowerCase().contains(key.toLowerCase())) {
					found = true;
					prevValue = next[1];
				}
			}
			if (!found)
				key = type.toLowerCase();
			htmlCode = replaceMetadataInHTML(htmlCode, key, prevValue, newValue);
		}
		File htmlTarget = res.getFile().getParentFile();
		htmlTarget = new File(htmlTarget, targetName + "_info.html"); //$NON-NLS-1$
		htmlTarget = writeFile(htmlCode, htmlTarget);
		return htmlTarget;
	}

	/**
	 * Replaces metadata in HTML code based on current text in metadata fields.
	 * 
	 * @param htmlCode the HTML code
	 * @return the modified code
	 */
	private String replaceMetadataInHTML(String htmlCode, String name, String prevValue, String newValue) {
		if (newValue == null || newValue.trim().equals("")) //$NON-NLS-1$
			return htmlCode;
		if (prevValue == null) {
			// write new line
			int n = htmlCode.indexOf("<meta name="); // start of first metadata tag found //$NON-NLS-1$
			if (n < 0)
				n = htmlCode.indexOf("</head"); //$NON-NLS-1$
			if (n > -1) {
				String newCode = "<meta name=\"" + name + "\" content=\"" + newValue + "\">\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				htmlCode = htmlCode.substring(0, n) + newCode + htmlCode.substring(n, htmlCode.length());
			}
		} else if (!"".equals(newValue) && !newValue.equals(prevValue)) { //$NON-NLS-1$
			prevValue = "meta name=\"" + name + "\" content=\"" + prevValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			newValue = "meta name=\"" + name + "\" content=\"" + newValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			htmlCode = htmlCode.replace(prevValue, newValue);
		}
		return htmlCode;
	}

	/**
	 * A JTextField for editing ZipResourceDialog fields.
	 */
	protected static class EntryField extends JTextField {

		static Color defaultForeground = new JTextField().getForeground();

		EntryField() {
			getDocument().putProperty("parent", this); //$NON-NLS-1$
			addFocusListener(focusListener);
			addActionListener(actionListener);
			getDocument().addDocumentListener(documentListener);
		}

		EntryField(int width) {
			super(width);
			getDocument().putProperty("parent", this); //$NON-NLS-1$
			addFocusListener(focusListener);
			addActionListener(actionListener);
			getDocument().addDocumentListener(documentListener);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
			dim.width = Math.max(dim.width, 25);
			dim.width = Math.min(dim.width, 100);
			dim.width += 4;
			return dim;
		}

		protected String getDefaultText() {
			return null;
		}

		protected Color getEmptyForeground() {
			return Color.gray;
		}

		static DocumentListener documentListener = new DocumentAdapter() {
			@Override
			public void documentChanged(DocumentEvent e) {
				JTextComponent field = (JTextComponent) e.getDocument().getProperty("parent"); //$NON-NLS-1$
				field.setBackground(Color.yellow);
				field.setForeground(defaultForeground);
			}
		};

		static FocusListener focusListener = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				EntryField field = (EntryField) e.getSource();
				if (field.getDefaultText() != null && field.getText().equals(field.getDefaultText())) {
					field.setText(null);
					field.setForeground(defaultForeground);
				}
//      	field.selectAll();
				field.setBackground(Color.white);
			}

			@Override
			public void focusLost(FocusEvent e) {
				EntryField field = (EntryField) e.getSource();
				boolean fire = field.getBackground() == Color.yellow;
				if (field.getDefaultText() != null && "".equals(field.getText())) { //$NON-NLS-1$
					field.setText(field.getDefaultText());
					field.setForeground(field.getEmptyForeground());
				} else {
					field.setForeground(defaultForeground);
				}
				field.setBackground(Color.white);
				if (fire)
					field.fireActionPerformed();
			}
		};

		static ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EntryField field = (EntryField) e.getSource();
				field.setBackground(Color.white);
				field.setForeground(defaultForeground);
			}
		};

	}

	/**
	 * A DocumentListener adapter.
	 */
	protected static class DocumentAdapter implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		/**
		 * Called when any DocumentListener method is invoked
		 * 
		 * @param e - the DocumentEvent from the original DocumentListener method
		 */
		public void documentChanged(DocumentEvent e) {
		}
	}

	/**
	 * A class to render labels for video formats.
	 */
	class FormatRenderer extends JLabel implements ListCellRenderer<Object> {

		FormatRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 0));
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object val, int index, boolean selected,
				boolean hasFocus) {

			if (selected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (val != null && val instanceof String) {
				String s = (String) val;
				int i = s.indexOf("("); //$NON-NLS-1$
				if (i > -1) {
					s = s.substring(0, i - 1);
				}
				setText(s);
			}
			return this;
		}

	}

	public static void thumbnailDialogClosed(TrackerPanel trackerPanel) {
		ExportZipDialog d = getDialog(trackerPanel);
		if (d.isVisible) {
			d.refreshThumbnailGUI();
		}
	}

}
