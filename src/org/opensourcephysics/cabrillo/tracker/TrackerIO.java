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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.zip.ZipEntry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.controls.ListChooser;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.Renderable;
import org.opensourcephysics.media.core.AsyncVideoI;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.LibraryResource;
import org.opensourcephysics.tools.LibraryTreePanel;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.AsyncDialog;
import javajs.async.AsyncFileChooser;
import javajs.async.AsyncSwingWorker;
import javajs.async.SwingJSUtils.Performance;

/**
 * This provides static methods for managing video and text input/output.
 *
 * @author Douglas Brown
 */
public class TrackerIO extends VideoIO {

	public interface TrackerMonitor {

		void stop();

		void setFrameCount(int count);

		void close();

		int getFrameCount();

		void setProgressAsync(int progress);

		void restart();

		String getName();

		void setTitle(String title);

	}

	/**
	 * delimiters
	 */
	protected static final String TAB = "\t", SPACE = " ", //$NON-NLS-1$ //$NON-NLS-2$
			COMMA = ",", SEMICOLON = ";"; //$NON-NLS-1$ //$NON-NLS-2$

	public static final Runnable NULL_RUNNABLE = () -> {};

	protected static SingleExtFileFilter zipFileFilter, trkFileFilter, trzFileFilter;
	protected static SingleExtFileFilter videoAndTrkFileFilter, txtFileFilter, jarFileFilter;

	/**
	 * TAB, SPACE, COMMA, or SEMICOLON
	 */
	protected static Map<String, String> delimiters = new TreeMap<String, String>();
	protected static String defaultDelimiter = TAB; // tab delimiter by default
	protected static String delimiter = defaultDelimiter;
	protected static Map<String, String> customDelimiters = new TreeMap<String, String>();

	protected static boolean isffmpegError = false;
	protected static TFrame theFrame;
	protected static PropertyChangeListener ffmpegListener;
	private static boolean loadInSeparateThread = true;
	
//	public static boolean isLoadInSeparateThread() {
//		return loadInSeparateThread;
//	}
//
// BH 2020.11.21 was from TFrame.Loader, but it is better to always use a separate thread.
//	public static void setLoadInSeparateThread(String why, boolean b) {
//	  loadInSeparateThread = b;
//	  OSPLog.debug("TrackerIO set loading " + loadInSeparateThread + " for " + why);
//	}
	
	private static Set<TrackerMonitor> monitors = new HashSet<>();
	protected static double defaultBadFrameTolerance = 0.2;
	protected static boolean dataCopiedToClipboard;

	static {
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			ffmpegListener = (e) -> {
					if (e.getPropertyName().equals("ffmpeg_error")) { //$NON-NLS-1$
						if (!isffmpegError) { // first error thrown
							isffmpegError = true;
							if (!Tracker.warnXuggleError) {
								if (e.getNewValue() != null) {
									String s = e.getNewValue().toString();
									int n = s.indexOf("]"); //$NON-NLS-1$
									if (n > -1)
										s = s.substring(n + 1);
									s += TrackerRes.getString("TrackerIO.ErrorFFMPEG.LogMessage"); //$NON-NLS-1$
									OSPLog.warning(s);
								}
								return;
							}
							// warn user that a Xuggle error has occurred
							Box box = Box.createVerticalBox();
							box.add(new JLabel(TrackerRes.getString("TrackerIO.Dialog.ErrorFFMPEG.Message1"))); //$NON-NLS-1$
							String error = e.getNewValue().toString();
							int n = error.lastIndexOf("]"); //$NON-NLS-1$
							if (n > -1) {
								error = error.substring(n + 1).trim();
							}
							box.add(new JLabel("  ")); //$NON-NLS-1$
							JLabel erLabel = new JLabel("\"" + error + "\""); //$NON-NLS-1$ //$NON-NLS-2$
							erLabel.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));
							box.add(erLabel);
							box.add(new JLabel("  ")); //$NON-NLS-1$
							box.add(new JLabel(TrackerRes.getString("TrackerIO.Dialog.ErrorFFMPEG.Message2"))); //$NON-NLS-1$

							box.add(new JLabel("  ")); //$NON-NLS-1$
							box.setBorder(BorderFactory.createEmptyBorder(20, 15, 0, 15));

							JDialog dialog = new JDialog(theFrame, false);
							JPanel contentPane = new JPanel(new BorderLayout());
							dialog.setContentPane(contentPane);
							contentPane.add(box, BorderLayout.CENTER);
							JButton closeButton = new JButton(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
							closeButton.setForeground(new Color(0, 0, 102));
							closeButton.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									dialog.setVisible(false);
								}
							});
							JButton dontShowAgainButton = new JButton(
									TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox")); //$NON-NLS-1$
							dontShowAgainButton.setForeground(new Color(0, 0, 102));
							dontShowAgainButton.addActionListener((ee) -> {
								Tracker.warnXuggleError = false;
								dialog.setVisible(false);
							});
							JPanel buttonbar = new JPanel();
							buttonbar.add(dontShowAgainButton);
							buttonbar.add(closeButton);
							buttonbar.setBorder(BorderFactory.createEtchedBorder());
							contentPane.add(buttonbar, BorderLayout.SOUTH);
							FontSizer.setFonts(dialog, FontSizer.getLevel());
							dialog.pack();
							dialog.setTitle(TrackerRes.getString("TrackerIO.Dialog.ErrorFFMPEG.Title")); //$NON-NLS-1$
							// center dialog on the screen
							Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
							int x = (dim.width - dialog.getBounds().width) / 2;
							int y = (dim.height - dialog.getBounds().height) / 2;
							dialog.setLocation(x, y);
							dialog.setVisible(true);
						}
					}
			};
			OSPLog.getOSPLog().addPropertyChangeListener(ffmpegListener);
		}
		zipFileFilter = new SingleExtFileFilter("zip", TrackerRes.getString("TrackerIO.ZipFileFilter.Description")); //$NON-NLS-1$ //$NON-NLS-2$
		trzFileFilter = new SingleExtFileFilter("trz", TrackerRes.getString("TrackerIO.ZIPResourceFilter.Description")); //$NON-NLS-1$ //$NON-NLS-2$
		txtFileFilter = new SingleExtFileFilter("txt", TrackerRes.getString("TrackerIO.TextFileFilter.Description")); //$NON-NLS-1$ //$NON-NLS-2$
		jarFileFilter = new SingleExtFileFilter("jar", TrackerRes.getString("TrackerIO.JarFileFilter.Description")); //$NON-NLS-1$ //$NON-NLS-2$
		trkFileFilter = new SingleExtFileFilter("trk", TrackerRes.getString("TrackerIO.DataFileFilter.Description")) { //$NON-NLS-1$ //$NON-NLS-2$

			@Override
			public boolean accept(File f, boolean checkDir) {
				return (checkDir && f.isDirectory() || zipFileFilter.accept(f, false) || trzFileFilter.accept(f, false)
						|| super.accept(f, false));
			}

		};

		videoAndTrkFileFilter = new SingleExtFileFilter(null,
				TrackerRes.getString("TrackerIO.VideoAndDataFileFilter.Description")) { //$NON-NLS-1$
			@Override
			public boolean accept(File f, boolean checkDir) {
				return (checkDir && f.isDirectory() || trkFileFilter.accept(f, false)
						|| videoFileFilter.accept(f, false) || super.accept(f, false));
			}
		};

		delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Tab"), TAB); //$NON-NLS-1$
		delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Space"), SPACE); //$NON-NLS-1$
		delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Comma"), COMMA); //$NON-NLS-1$
		delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Semicolon"), SEMICOLON); //$NON-NLS-1$
	}

	protected static TreeSet<String> videoFormatDescriptions // alphabetical
			= new TreeSet<>();

	protected static HashMap<String, VideoType> videoFormats // name to VideoType
			= new HashMap<>();

	public static String selectedVideoFormat;

	/**
	 * private constructor to prevent instantiation
	 */
	private TrackerIO() {
		/** empty block */
	}

	/**
	 * Writes TrackerPanel data to the specified file. If the file is null it brings
	 * up a chooser.
	 *
	 * @param file         the file to write to
	 * @param trackerPanel the TrackerPanel
	 * @return the file written to, or null if not written
	 */
	public static File save(File file, TrackerPanel trackerPanel) {
		trackerPanel.restoreViews();
		getChooser().setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(trkFileFilter);
		chooser.setAccessory(null);
		if (file == null && trackerPanel.getDataFile() == null) {
			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
			if (clip.getVideoPath() != null) {
				File dir = new File(clip.getVideoPath()).getParentFile();
				chooser.setCurrentDirectory(dir);
			}
		}

		boolean isNew = file == null;
		file = save(file, trackerPanel, TrackerRes.getString("TrackerIO.Dialog.SaveTab.Title")); //$NON-NLS-1$
		chooser.removeChoosableFileFilter(trkFileFilter);
		chooser.setAcceptAllFileFilterUsed(true);
		if (isNew && file != null) {
			Tracker.addRecent(XML.getAbsolutePath(file), false); // add at beginning
			TMenuBar.refreshMenus(trackerPanel, TMenuBar.REFRESH_TRACKERIO_SAVE);
		}
		return file;
	}

	/**
	 * Saves a tabset in the specified file. If the file is null this brings up a
	 * chooser.
	 *
	 * @param file  the file to write to
	 * @param frame the TFrame
	 * @return the file written to, or null if not written
	 */
	public static File saveTabset(File file, TFrame frame) {
		// count tabs with data files or unchanged (newly opened) videos
		int n = 0;
		for (int i = 0; i < frame.getTabCount(); i++) {
			TrackerPanel trackerPanel = frame.getTrackerPanel(i);
			if (trackerPanel.getDataFile() != null) {
				n++;
				continue;
			}
			Video video = trackerPanel.getVideo();
			if (!trackerPanel.changed && video != null) {
				String path = (String) video.getProperty("absolutePath"); //$NON-NLS-1$
				if (path != null) {
					n++;
					continue;
				}
			}
			// notify user that tab must be saved in order be in tabset
			int selected = JOptionPane.showConfirmDialog(frame,
					TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message1") //$NON-NLS-1$
							+ " " + i + " (\"" + frame.getTabTitle(i) + "\") " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message2") + XML.NEW_LINE //$NON-NLS-1$
							+ TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message3"), //$NON-NLS-1$
					TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Title"), //$NON-NLS-1$
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (selected == JOptionPane.CANCEL_OPTION) {
				return null;
			} else if (selected != JOptionPane.YES_OPTION) {
				continue;
			}
			getChooser().setAccessory(null);
			File newFile = save(null, trackerPanel, TrackerRes.getString("TrackerIO.Dialog.SaveTab.Title")); //$NON-NLS-1$
			if (newFile == null) {
				return null;
			}
			Tracker.addRecent(XML.getAbsolutePath(newFile), false); // add at beginning
			n++;
		}
		// abort if no data files
		if (n == 0) {
			JOptionPane.showMessageDialog(frame, TrackerRes.getString("TrackerIO.Dialog.NoTabs.Message"), //$NON-NLS-1$
					TrackerRes.getString("TrackerIO.Dialog.NoTabs.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			return null;
		}
		// if file is null, use chooser to get a file
		if (file == null) {
			File[] files = getChooserFiles("save tabset"); //$NON-NLS-1$
			if (files == null || !canWrite(files[0]))
				return null;
			file = files[0];
		}
		frame.tabsetFile = file;
		XMLControl xmlControl = new XMLControlElement(frame);
		xmlControl.write(XML.getAbsolutePath(file));
		Tracker.addRecent(XML.getAbsolutePath(file), false); // add at beginning
		TMenuBar.refreshMenus(frame.getSelectedPanel(), TMenuBar.REFRESH_TRACKERIO_SAVETABSET);
		return file;
	}

	private static Runnable resetChooser = () -> {
			chooser.resetChoosableFileFilters();
			chooser.setSelectedFile(null);
	};

	/**
	 * A Stop-gap method to allow Java-only functionality.
	 * 
	 * @param type
	 * @return
	 */
	@Deprecated
	public static File[] getChooserFiles(String type) {
		return getChooserFilesAsync(type, null);
	}

	/**
	 * Displays a file chooser and returns the chosen files.
	 *
	 * @param type may be open, open video, save, insert image, export file, import
	 *             file, save tabset, open data, open trk
	 * @return the files, or null if no files chosen
	 */
	public static File[] getChooserFilesAsync(String type, Function<File[], Void> processFiles) {

		// BH Java will run all this synchronously anyway.
		AsyncFileChooser chooser = getChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setAccessory(null);

		Runnable okOpen = () -> {
			if (processFiles != null) {
				File[] files = chooser.getSelectedFiles();
				File file = chooser.getSelectedFile();
				resetChooser.run();
				processFiles.apply(files != null && files.length > 0 ? files 
						: file != null ? new File[] { file } 
						: null);
			}
		};

		Runnable okSave = () -> {
			File file = chooser.getSelectedFile();
			resetChooser.run();
			if (canWrite(file))
				processFiles.apply(new File[] { file });
		};

		File ret = null;
		boolean isSave = false;
		switch (type.toLowerCase()) {
		case "open": //$NON-NLS-1$
			chooser.addChoosableFileFilter(videoAndTrkFileFilter);
			chooser.setFileFilter(videoAndTrkFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "open trk": //$NON-NLS-1$
			// open tracker file
			chooser.addChoosableFileFilter(trkFileFilter);
			chooser.setFileFilter(trkFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "open any": //$NON-NLS-1$
			// open any file
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "open video": // open video //$NON-NLS-1$
			chooser.addChoosableFileFilter(videoFileFilter);
			chooser.setFileFilter(videoFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "open data": // open text data file //$NON-NLS-1$
			chooser.addChoosableFileFilter(txtFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.OpenData.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "open ejs": // open ejs //$NON-NLS-1$
			chooser.addChoosableFileFilter(jarFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.OpenEJS.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "insert images": //$NON-NLS-1$
			chooser.setMultiSelectionEnabled(true);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(imageFileFilter);
			chooser.setSelectedFile(new File("")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "import file": //$NON-NLS-1$
			// import elements from a tracker file
			chooser.addChoosableFileFilter(trkFileFilter);
			chooser.setFileFilter(trkFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Import.Title")); //$NON-NLS-1$
			chooser.showOpenDialog(null, okOpen, resetChooser);
			break;
		case "export file": //$NON-NLS-1$
			// export elements to a tracker file
			isSave = true;
			chooser.addChoosableFileFilter(trkFileFilter);
			chooser.setFileFilter(trkFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Export.Title")); //$NON-NLS-1$
			chooser.showSaveDialog(null, okSave, resetChooser);
			break;
		case "save thumbnail":
			isSave = true;
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setDialogTitle(TrackerRes.getString("ThumbnailDialog.Chooser.SaveThumbnail.Title")); //$NON-NLS-1$
			if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return null;
			File f = chooser.getSelectedFile();
			// no, don't reset the chooser resetChooser.run();
			return (f == null ? null : new File[] { f });
		case "save data": //$NON-NLS-1$
			isSave = true;
			chooser.resetChoosableFileFilters();
			chooser.setDialogTitle(TrackerRes.getString("ExportDataDialog.Chooser.SaveData.Title")); //$NON-NLS-1$
			chooser.showSaveDialog(null, okSave, resetChooser);
			break;
		case "save tabset": //$NON-NLS-1$
			isSave = true;
			// save a tabset file
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(trkFileFilter);
			chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.SaveTabset.Title")); //$NON-NLS-1$
			String filename = ""; //$NON-NLS-1$
			File file = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
			String parent = XML.getDirectoryPath(filename);
			if (!parent.equals("")) { //$NON-NLS-1$
				XML.createFolders(parent);
				chooser.setCurrentDirectory(new File(parent));
			}
			chooser.setSelectedFile(file);
			chooser.showSaveDialog(null, () -> {
				resetChooser.run();
				if (processFiles != null) {
					processFiles.apply(new File[] { fixXML(chooser) });
				}
			}, resetChooser);
			ret = (processFiles != null || chooser.getSelectedOption() != JFileChooser.APPROVE_OPTION ? null
					: fixXML(chooser));
			break;
		default:
			return VideoIO.getChooserFilesAsync(type, processFiles);
		}
		ret = processChoose(chooser, ret, processFiles != null);
		if (processFiles == null) {
			resetChooser.run();
		}
		return (ret == null || isSave && !canWrite(ret) ? null : new File[] { ret });
	}

	protected static File fixXML(AsyncFileChooser chooser) {
		File file = chooser.getSelectedFile();
		if (!defaultXMLExt.equals(getExtension(file))) {
			String filename = XML.stripExtension(file.getPath());
			File f = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
			if (OSPRuntime.isJS) {
				// BH transfer the bytes
				OSPRuntime.jsutil.setFileBytes(f, OSPRuntime.jsutil.getBytes(file));
				OSPRuntime.cacheJSFile(f, true);
			}
			file = f;
		}
		return file;
	}

	/**
	 * Displays a file chooser and returns the chosen file, adding or changing the
	 * extension to match the specified extension.
	 *
	 * @param extension the extension
	 * @return the file, or null if no file chosen
	 */
	public static File getChooserFileForExtension(String extension) {
		if (extension != null && !extension.trim().equals("")) { //$NON-NLS-1$
			extension = extension.trim().toLowerCase();
		} else {
			extension = null;
		}
		String ext = extension;
		getChooser().setDialogTitle(MediaRes.getString("VideoIO.Dialog.SaveVideoAs.Title")); //$NON-NLS-1$
		chooser.resetChoosableFileFilters();
		chooser.setAccessory(null);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(ext != null);
		if (ext != null) {
			FileFilter fileFilter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f == null)
						return false;
					if (f.isDirectory())
						return true;
					if (ext.equals(getExtension(f)))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					String file = TrackerRes.getString("TMenuBar.Menu.File").toLowerCase(); //$NON-NLS-1$
					return ext.toUpperCase() + " " + file + " (." + ext + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			};
			chooser.addChoosableFileFilter(fileFilter);
			chooser.setFileFilter(fileFilter);
		}
		int result = chooser.showSaveDialog(null);
		File file = chooser.getSelectedFile();
		chooser.resetChoosableFileFilters();
		chooser.setSelectedFile(new File("")); //$NON-NLS-1$
		if (file == null)
			return null;
		if (result == JFileChooser.APPROVE_OPTION) {
			if (ext != null && !ext.equals(XML.getExtension(file.getName()))) {
				String path = file.getAbsolutePath();
				path = XML.stripExtension(path) + "." + ext; //$NON-NLS-1$
				file = new File(path);
			}
			if (!canWrite(file)) {
				return null;
			}
			return file;
		}
		return null;
	}

	/**
	 * From FileDropHandler
	 * 
	 * @param fileList
	 * @param targetPanel
	 * @return
	 */
	public static boolean loadFiles(TFrame frame, List<File> fileList, TrackerPanel targetPanel) {
		List<String> list = new ArrayList<String>();
		try {
			// define frameNumber for insertions
			// load the files
			int frameNumber = -1;
			int nf = fileList.size();
			boolean haveOneVideo = (fileList.size() == 1 && isVideo(fileList.get(0)));
			for (int j = 0; j < nf; j++) {
				final File file = fileList.get(j);
				OSPRuntime.cacheJSFile(file, true);
				OSPLog.debug("file to load: " + file.getAbsolutePath()); //$NON-NLS-1$
				// load a new tab unless file is video and there is a trackerPanel to import it
				if (!haveOneVideo) {
					// could be a video file or a directory of images
					list.add(XML.getAbsolutePath(file));
				} else if (targetPanel == null) {
					list.add(XML.getAbsolutePath(file));
				} else {
					// import video
					if (targetPanel.getVideo() instanceof ImageVideo && isImageFile(file)) {
						if (frameNumber < 0) {
							frameNumber = 0;
							targetPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							if (targetPanel.getVideo() != null) {
								frameNumber = targetPanel.getVideo().getFrameNumber();
							}
						}
						// if targetPanel has image video and file is image, add after current frame
						File[] added = insertImagesIntoVideo(new File[] { file }, targetPanel,
								frameNumber + 1);
						frameNumber += added.length;
					} else {
						// if targetPanel not null and file is video then import
						// open in separate background thread
						// final TFrame frame = targetPanel.getTFrame();
						// final int n = frame.getTab(targetPanel);
						Runnable runner = new Runnable() {
							@Override
							public void run() {
								// TrackerPanel trackerPanel = frame.getTrackerPanel(n);
								importVideo(file.getAbsolutePath(), targetPanel, null);/// NULL_RUNNABLE);
							}
						};
						run("TFrame.loadFiles", runner); 
					}
				} 
//				else {
//					// else inform user that file is not acceptable
//					JOptionPane.showMessageDialog(this, "\"" + file.getName() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
//							+ TrackerRes.getString("FileDropHandler.Dialog.BadFile.Message"), //$NON-NLS-1$
//							TrackerRes.getString("FileDropHandler.Dialog.BadFile.Title"), //$NON-NLS-1$
//							JOptionPane.WARNING_MESSAGE);
//				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (list.isEmpty()) {
				frame.setCursor(Cursor.getDefaultCursor());
			} else {
				openFiles(frame, list, () -> {
					frame.setCursor(Cursor.getDefaultCursor());
				});
			}
		}
		return true;
	}

	/**
	 * Loads data or a video from a specified file into a new TrackerPanel. If file
	 * is null, a file chooser is displayed.
	 *
	 * @param file  the file to be loaded (may be null)
	 * @param frame the frame for the TrackerPanel
	 * @param whenDone to do when completed, or null
	 */
	public static void openFileFromDialog(File file, TFrame frame, Runnable whenDone) {
		// TFrame.doOpenFileFromDialog()
		// TFrame.doOpenExportedAndUpdateLibrary (Java only)
		// ExportVideoDialog (Java only)
		OSPLog.debug("TrackerIO openTabFileAsync " + file); //$NON-NLS-1$
		if (file == null) {
			getChooserFilesAsync("open", (files) -> {
					File f = null;
					if (files != null) {
						f = files[0];
					}
					if (f == null) {
						OSPLog.finer("no file to open"); //$NON-NLS-1$
					} else {
//						if (!frame.haveContent()) {
//							frame.removeTabNow(0);
//						}
						openFiles(frame, listOf(f), whenDone);
					}
					return null;
			});
		} else {
			openFiles(frame, listOf(file), whenDone);
		}
	}

	/**
	 * Open one or more files sequentially. 
	 * 
	 * @param frame
	 * @param files
	 * @param whenDone
	 */
	static void openFiles(TFrame frame, List<String> files, Runnable whenDone) {
		// TFrame.Loader.loadObject
		// TrackerIO.loadFiles (From FileDropHandler)
		// TrackerIO.openFileFromDialog
		frame.loadedFiles.clear();
		String path = (files.size() == 1 ? files.get(0) : null);
		if (path != null && (path.contains("/OSP/Cache/") || !trzFileFilter.accept(new File(path), false)))
			path = null;
		String path0 = path;
		startLoading(files, null, frame, () -> {
			if (path0 != null)
				addToLibrary(frame, path0);
		});
	}
	
	/**
	 * Loads data or a video from a specified path into a TrackerPanel. The initiator for AsyncLoader
	 *
	 * @param paths         a list of absolute paths of a file or url to open sequentially
	 * @param existingPanel a TrackerPanel to load (only for a video; may be null)
	 * @param frame         the frame for the TrackerPanel
	 * @param desktopFiles  a list of HTML and/or PDF files to open on the desktop
	 *                      (may be null)
	 */
	private static void startLoading(List<String> paths, TrackerPanel existingPanel, TFrame frame,
		  Runnable whenDone) {
		// importVideo 
		// openFromLibary
		// ..TFrame.openLibraryResource
		// openFiles
		// ..TFrame.Loader.loadObject
		// ..TrackerIO.loadFiles (From FileDropHandler)
		// ..TrackerIO.openFileFromDialog
		// openURL
		// ..TFrame.doOpenURL
		OSPLog.debug("TrackerIO openTabPathAsync " + paths); //$NON-NLS-1$
		new AsyncLoader(paths, existingPanel, frame, whenDone).execute();
	}

	
//
//	/**
//	 * Loads data or a video from a specified url into a new TrackerPanel.
//	 *
//	 * @param url   the url to be loaded
//	 * @param frame the frame for the TrackerPanel
//	 */
//	public static void open(URL url, TFrame frame) {
//		if (url != null) {
//			String path = url.toExternalForm();
//			OSPLog.debug("TrackerIO opening URL"); //$NON-NLS-1$
//			openAsync(path, frame, null);
//		}
//	}

	/**
	 * Called by open-addToLibrary, importVideo, openAllCollection, openTabFileAsyncFinally
	 * @param name
	 * @param r
	 */
	static void run(String name, Runnable r) {
		OSPLog.debug("TrackerIO run loading " + loadInSeparateThread + " for " + name);
		if (loadInSeparateThread) {
			Thread t = new Thread(r);
			t.setName(name);
			t.setPriority(Thread.NORM_PRIORITY);
			t.setDaemon(true);
			t.start();
		} else {
			r.run();
		}
	}

//	/**
//	 * Loads data or a video from a specified path into a new TrackerPanel.
//	 *
//	 * @param path  the path
//	 * @param frame the frame for the TrackerPanel
//	 */
//	public static void open(String path, TFrame frame) {
//		openAsync(path, frame, null);
//	}
	
	public static void openURL(String path, TFrame frame, Runnable whenDone) {
		// TFrame.doOpenURL
		frame.loadedFiles.clear();
		OSPLog.debug("TrackerIO open " + path); //$NON-NLS-1$
		startLoading(listOf(path), null, frame, () -> {
			if (trzFileFilter.accept(new File(path), false)
					&& !ResourceLoader.isHTTP(path) && !path.contains("/OSP/Cache/")) {
				addToLibrary(frame, path);
			}
			if (whenDone != null)
				whenDone.run();
		});
	}

	/**
	 * Loads a set of trk, trz, zip, or video files into one or more new
	 * TrackerPanels (tabs).
	 * @param uriPaths     an array of URL paths to be loaded
	 * @param frame        the frame for the TrackerPanels
	 */
	public static void openFromLibrary(List<String> uriPaths, TFrame frame, Runnable whenDone) {
		// TFrame.openLibraryResource
		if (uriPaths == null || uriPaths.isEmpty()) {
			return;
		}
		frame.loadedFiles.clear();
// BH I may have disabled this for testing only.
//		Runnable whenDone = (trzPath != null ? () -> {
//				TFrame.repaintT(frame);
//				if (!trzPath.contains("/OSP/Cache/") && !ResourceLoader.isHTTP(trzPath)) {
//					OSPLog.debug("TrackerIO adding to library " + trzPath); //$NON-NLS-1$
//					addToLibrary(frame, trzPath);
//				}
//			} : null); // BH Q: Could be null_runable ? better: add whenDone to this method?

		// open in separate background thread if flagged
			// from TFrame.openLibaryResource
		startLoading(uriPaths, null, frame, whenDone);
	}

	private static void addToLibrary(TFrame frame, String path) {
		// add local non-cached TRZ files to library browser recent collection
		// BH! Q: this was effectively TRUE -- "any directory is OK" why?

		if (!OSPRuntime.autoAddLibrary) {
			OSPLog.debug("skipping TrackerIO addToLibrary " + path); //$NON-NLS-1$
			return;
		}

		run("addToLibrary", () -> {

				frame.getLibraryBrowser().open(path);
//			      frame.getLibraryBrowser().setVisible(true); 
				Timer timer = new Timer(1000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						LibraryTreePanel treePanel = frame.getLibraryBrowser().getSelectedTreePanel();
						if (treePanel != null) {
							treePanel.refreshSelectedNode();
						}
					}
				});
				timer.setRepeats(false);
				timer.start();
		});
	}

	/**
	 * Imports xml data into a tracker panel from a file selected with a chooser.
	 * The user selects the elements to import with a ListChooser.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public static void importFile(TrackerPanel trackerPanel) {
		getChooserFilesAsync("import file", new Function<File[], Void>() {

			@Override
			public Void apply(File[] files) {
				if (files != null)
					importXMLAction(trackerPanel, files[0]);
				return null;
			}
		});
	}

	protected static void importXMLAction(TrackerPanel trackerPanel, File file) {
		OSPLog.fine("importing from " + file); //$NON-NLS-1$
		XMLControlElement control = new XMLControlElement(file.getAbsolutePath());
		Class<?> type = control.getObjectClass();
		if (TrackerPanel.class.equals(type)) {
			// choose the elements and load the tracker panel
			choose(trackerPanel, control, false, () -> {
					trackerPanel.changed = true;
					control.loadObject(trackerPanel);
					TTrackBar.refreshMemoryButton();
			});
		} else {
			JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
					TrackerRes.getString("TrackerPanel.Dialog.LoadFailed.Message") //$NON-NLS-1$
							+ " " + XML.getName(XML.getAbsolutePath(file)), //$NON-NLS-1$
					TrackerRes.getString("TrackerPanel.Dialog.LoadFailed.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Saves a video to a file by copying the original. If the file is null, a
	 * fileChooser is used to pick one.
	 *
	 * @param trackerPanel the tracker panel with the video
	 * @return the saved file, or null if not saved
	 */
	public static File saveVideo(File file, TrackerPanel trackerPanel) {
		Video video = trackerPanel.getVideo();
		if (video == null)
			return null;
		if (video instanceof ImageVideo) {
			boolean saved = ((ImageVideo) video).saveInvalidImages();
			if (!saved)
				return null;
		}
		String source = (String) video.getProperty("absolutePath"); //$NON-NLS-1$
		String extension = XML.getExtension(source);
		if (file == null) {
			File target = getChooserFileForExtension(extension);
			if (target == null)
				return null;
			return saveVideo(target, trackerPanel);
		}
		boolean success = ResourceLoader.copyAllFiles(new File(source), file);
		if (success) {
			Tracker.addRecent(XML.getAbsolutePath(file), false); // add at beginning
			TMenuBar.refreshMenus(trackerPanel, TMenuBar.REFRESH_TRACKERIO_SAVEVIDEO);
			return file;
		}
		return null;
	}

	/**
	 * Imports chooser-selected video to the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public static void importVideo(TrackerPanel trackerPanel, Runnable whenDone) {
		JFileChooser chooser = getChooser();
		chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.ImportVideo.Title")); //$NON-NLS-1$
		// 2020.04.03 DB changed chooser to async
		getChooserFilesAsync("open video", new Function<File[], Void>() {//$NON-NLS-1$

			@Override
			public Void apply(File[] files) {
				File file = (files == null ? null : files[0]);
				if (file != null) {
					OSPRuntime.cacheJSFile(file, true);
					run("importVideo", () -> {
							importVideo(file.getAbsolutePath(), trackerPanel, whenDone);
					});
				}
				return null;
			}

		});
//    File[] files = getChooserFiles("open video"); //$NON-NLS-1$
//    if (files==null || files.length==0) {
//      return;
//    }
//    // open in separate background thread if flagged
//    File theFile = files[0];
//    Runnable importVideoRunner = new Runnable() {
//			public void run() {
//				TrackerIO.importVideo(theFile, trackerPanel, null);
//				OSPLog.debug("TrackerIO completed importing file " + theFile); //$NON-NLS-1$
//			}
//	    };
//    if (loadInSeparateThread) {
//      Thread importVideoOpener = new Thread(importVideoRunner);
//      importVideoOpener.setName("importVideo");
//      
//      importVideoOpener.setPriority(Thread.NORM_PRIORITY);
//      importVideoOpener.setDaemon(true);
//      importVideoOpener.start(); 
//    }
//    else importVideoRunner.run();
	}

//	/**
//	 * Imports a video file to the specified tracker panel.
//	 *
//	 * @param file         the video file
//	 * @param trackerPanel the tracker panel
//	 * @param vidType      the preferred video type (may be null)
//	 */
//	public static void importVideo(File file, TrackerPanel trackerPanel, VideoType vidType, Runnable whenDone) {
//		importVideo(XML.getAbsolutePath(file), trackerPanel, vidType, whenDone);
//	}

	public static void importVideo(String path, TrackerPanel trackerPanel, Runnable whenDone) {
		OSPLog.debug("TrackerIO importing file: " + path); //$NON-NLS-1$
		TFrame frame = trackerPanel.getTFrame();
		frame.loadedFiles.clear();
		startLoading(listOf(path), trackerPanel, frame, whenDone);
	}

	static List<String> listOf(String path) {
		List<String> list = new ArrayList<>();
		list.add(path);
		return list;
	}

	static List<String> listOf(File f) {
		List<String> list = new ArrayList<>();
		list.add(XML.getAbsolutePath(f));
		return list;
	}

	/**
	 * Checks for video frames with durations that vary from the mean.
	 * 
	 * @param trackerPanel         the TrackerPanel to check
	 * @param tolerance            the unacceptable variation limit
	 * @param showDialog           true to display the results in a dialog
	 * @param onlyIfFound          true to display the dialog only if problems are found
	 * @param showSetDefaultButton true to show the "Don't show again" button
	 * @return a BitSet indicating frames with odd durations
	 */
	public static BitSet findBadVideoFrames(TrackerPanel trackerPanel, double tolerance, boolean showDialog,
			boolean onlyIfFound, boolean showSetDefaultButton) {
		BitSet outliers = new BitSet();
//		if (OSPRuntime.isJS)
//			return outliers;
		Video video = trackerPanel.getVideo();
		if (video == null)
			return outliers;
		double videoDurMS = video.getDuration();
		double frameDur = 0;
		int nFrames = video.getFrameCount();
		for (int i = 0; i < nFrames; i++) {
			if (i == 0)
				frameDur = videoDurMS / (nFrames - outliers.cardinality());
			if (outliers.get(i))
				continue;
			double durMS = video.getFrameDuration(i);
			double err = Math.abs(frameDur - durMS) / frameDur;
			if (err > tolerance) {
				videoDurMS -= durMS;
				outliers.set(i);
				OSPLog.debug("Frame " + i +" duration " + durMS + " outlier for average " + frameDur);
				// restart
				i = -1;
			}
		}
		outliers.clear(nFrames - 1);
		if (showDialog) {
			NumberFormat format = NumberFormat.getInstance();
			String message = TrackerRes.getString("TrackerIO.Dialog.DurationIsConstant.Message"); //$NON-NLS-1$
			int messageType = JOptionPane.INFORMATION_MESSAGE;
			if (outliers.isEmpty() && onlyIfFound) {
				return outliers;
			}
			if (!outliers.isEmpty()) {
				messageType = JOptionPane.WARNING_MESSAGE;
				// get last bad frame
				int last = outliers.length() - 1;
				// find longest section of good frames
				int maxClear = -1;
				int start = 0, end = 0;
				int prevBadFrame = -1;
				for (int i = outliers.nextSetBit(0); i >= 0; i = outliers.nextSetBit(i + 1)) {
					int clear = i - prevBadFrame - 2;
					if (clear > maxClear) {
						start = prevBadFrame + 1;
						end = i - 1;
						maxClear = clear;
						prevBadFrame = i;
					}
				}
				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
				if (clip.getEndFrameNumber() - last - 1 > maxClear) {
					start = last + 1;
					end = clip.getEndFrameNumber();
				}
				// assemble message
				format.setMaximumFractionDigits(2);
				format.setMinimumFractionDigits(2);
				message = TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message1"); //$NON-NLS-1$
				message += " " + (int) (tolerance * 100) + "%."; //$NON-NLS-1$ //$NON-NLS-2$
				message += "\n" + TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message2"); //$NON-NLS-1$//$NON-NLS-2$
				message += "\n" + TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message3"); //$NON-NLS-1$ //$NON-NLS-2$
				message += "\n\n" + TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message4"); //$NON-NLS-1$ //$NON-NLS-2$
				for (int i = outliers.nextSetBit(0); i >= 0; i = outliers.nextSetBit(i + 1)) {
					message += " " + i + " (" + video.getFrameDuration(i) + "ms)"; //$NON-NLS-1$
					if (i < last)
						message += ","; //$NON-NLS-1$
				}
				message += "\n\n" + TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Recommended") + ":  " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Start") + " " + start + ",  " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ TrackerRes.getString("TrackerIO.Dialog.DurationVaries.End") + " " + end + "\n "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else { // all frames have identical durations
				format.setMaximumFractionDigits(2);
				format.setMinimumFractionDigits(2);
				frameDur = trackerPanel.getPlayer().getClipControl().getMeanFrameDuration();
				message += ": " + format.format(frameDur) + "ms"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String close = TrackerRes.getString("Dialog.Button.OK"); //$NON-NLS-1$
			String dontShow = TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox"); //$NON-NLS-1$
			String[] buttons = showSetDefaultButton ? new String[] { dontShow, close } : new String[] { close };
			new AsyncDialog().showOptionDialog(theFrame, message,
					TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Title"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, messageType, null, buttons, close, (e) -> {
						int response = e.getID();
						if (response >= 0 && response < buttons.length && buttons[response].equals(dontShow)) {
							Tracker.warnVariableDuration = false;
						}
					});
		}
		return outliers;
	}

	/**
	 * Inserts chooser-selected images into an ImageVideo on a TrackerPanel.
	 *
	 * @param trackerPanel the TrackerPanel
	 * @param startIndex   the insertion index
	 * @return an array of inserted files
	 */
	public static void insertImagesIntoVideo(TrackerPanel trackerPanel, int startIndex) {
		JFileChooser chooser = getChooser();
		chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.AddImage.Title")); //$NON-NLS-1$
		getChooserFilesAsync("insert images", new Function<File[], Void>() {

			@Override
			public Void apply(File[] files) {
				if (files == null || files.length == 0)
					return null;
				String[] paths = new String[files.length];
				for (int i = 0; i < paths.length; i++) {
					paths[i] = files[i].getPath();
				}
				Undo.postImageVideoEdit(trackerPanel, paths, startIndex, trackerPanel.getPlayer().getStepNumber(),
						true);
				insertImagesIntoVideo(files, trackerPanel, startIndex);
				return null;
			}
		});
	}

	/**
	 * Inserts file-based images into an ImageVideo on a TrackerPanel.
	 *
	 * @param files        array of image files
	 * @param trackerPanel the TrackerPanel
	 * @param startIndex   the insertion index
	 * @return an array of inserted files
	 */
	public static File[] insertImagesIntoVideo(File[] files, TrackerPanel trackerPanel, int startIndex) {
		if (files == null) {
			return null;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			// insert images in image video
			if (imageFileFilter.accept(file)) {
				try {
					ImageVideo imageVid = (ImageVideo) trackerPanel.getVideo();
					imageVid.insert(file.getAbsolutePath(), startIndex, files.length == 1);
					VideoClip clip = trackerPanel.getPlayer().getVideoClip();
					clip.setStepCount(imageVid.getFrameCount());
					trackerPanel.getPlayer().setStepNumber(clip.frameToStep(startIndex++));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else {
				String s = TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Message1"); //$NON-NLS-1$
				if (i < files.length - 1) {
					s += XML.NEW_LINE + TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Message2"); //$NON-NLS-1$
					int result = JOptionPane.showConfirmDialog(trackerPanel, "\"" + file + "\" " + s, //$NON-NLS-1$ //$NON-NLS-2$
							TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Title"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					if (result != JOptionPane.YES_OPTION) {
						if (i == 0)
							return null;
						File[] inserted = new File[i];
						System.arraycopy(files, 0, inserted, 0, i);
						TTrackBar.refreshMemoryButton();
						return inserted;
					}
				} else { // bad file is last one in array
					JOptionPane.showMessageDialog(trackerPanel.getTFrame(), "\"" + file + "\" " + s, //$NON-NLS-1$ //$NON-NLS-2$
							TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Title"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					if (i == 0)
						return null;
					File[] inserted = new File[i];
					System.arraycopy(files, 0, inserted, 0, i);
					TTrackBar.refreshMemoryButton();
					return inserted;
				}
			}
		}
		TTrackBar.refreshMemoryButton();
		return files;
	}

	/**
	 * Exports xml data from the specified tracker panel to a file selected with a
	 * chooser. Displays a dialog with choices of items to export.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return the file
	 */
	public static void exportXMLFile(TrackerPanel trackerPanel) {
		// create an XMLControl

		XMLControl control = new XMLControlElement(trackerPanel);
		choose(trackerPanel, control, true, () -> {

				getChooser().setSelectedFile(
						new File(MediaRes.getString("VideoIO.FileName.Untitled") + "." + defaultXMLExt)); //$NON-NLS-1$ $NON-NLS-2$
				getChooserFilesAsync("export file", (files) -> {
						if (files == null) {
							return null;
						}
						File file = files[0];
						if (!defaultXMLExt.equals(getExtension(file))) {
							String filename = XML.stripExtension(file.getPath());
							file = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
						}
						if (canWrite(file))
							try {
								control.write(new FileWriter(file));
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						return null;
				});
		});
	}

	/**
	 * Displays a ListChooser with choices from the specified control. Modifies the
	 * control and returns true if the OK button is clicked.
	 * 
	 * @param trackerPanel
	 * @param control      the XMLControl
	 * @param dialog       the dialog
	 * @param isExport
	 * @param ok           run only if not canceled
	 */
	public static void choose(TrackerPanel trackerPanel, XMLControl control, boolean isExport, Runnable ok) {
		// create the lists

		ArrayList<XMLControl> choices = new ArrayList<XMLControl>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<XMLControl> originals = new ArrayList<XMLControl>();
		ArrayList<XMLProperty> primitives = new ArrayList<XMLProperty>(); // non-object properties
		// add direct child controls except clipcontrol and toolbar
		XMLControl vidClipControl = null, vidControl = null;
		XMLControl[] children = control.getChildControls();
		for (int i = 0; i < children.length; i++) {
			String name = children[i].getPropertyName();
			if (name.equals("coords")) { //$NON-NLS-1$
				name = TrackerRes.getString("TMenuBar.MenuItem.Coords"); //$NON-NLS-1$
			} else if (name.equals("videoclip")) { //$NON-NLS-1$
				name = TrackerRes.getString("TMenuBar.MenuItem.VideoClip"); //$NON-NLS-1$
				vidControl = children[i].getChildControl("video"); //$NON-NLS-1$
				if (vidControl != null) {
					vidClipControl = children[i];
					originals.add(vidControl);
					choices.add(vidControl);
					names.add(name + " " + TrackerRes.getString("TrackerIO.Export.Option.WithoutVideo")); //$NON-NLS-1$//$NON-NLS-2$
					name = name + " " + TrackerRes.getString("TrackerIO.Export.Option.WithVideo"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			originals.add(children[i]);
			if (name.equals("clipcontrol")) //$NON-NLS-1$
				continue;
			if (name.equals("toolbar")) //$NON-NLS-1$
				continue;
			choices.add(children[i]);
			names.add(name);
		}
		// add track controls and gather primitives
		Iterator<XMLProperty> it = control.getPropsRaw().iterator();
		while (it.hasNext()) {
			XMLProperty prop = it.next();
			if ("tracks".indexOf(prop.getPropertyName()) != -1) { //$NON-NLS-1$
				children = prop.getChildControls();
				for (int i = 0; i < children.length; i++) {
					choices.add(children[i]);
					names.add(children[i].getPropertyName());
					originals.add(children[i]);
				}
			} else if (prop.getPropertyType() == XMLProperty.TYPE_OBJECT) { //$NON-NLS-1$
				primitives.add(prop);
			}
		}
		// show the dialog for user input and make changes if approved
		XMLControl vControl = vidControl, vClipControl = vidClipControl;
		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// remove primitives from control
				if (e.getID() != ActionEvent.ACTION_PERFORMED) {
					return;
				}
				for (XMLProperty prop : primitives) {
					control.setValue(prop.getPropertyName(), null);
				}
				control.getPropertyContent().removeAll(primitives);
				// compare choices with originals and remove unwanted object content
				boolean removeVideo = false;
				for (XMLControl next : originals) {
					if (next == vControl) {
						removeVideo = choices.contains(next);
						continue;
					} else if (next == vClipControl) {
						if (!choices.contains(next)) {
							if (removeVideo) {
								// remove video from clip property
								XMLProperty prop = vControl.getParentProperty();
								vClipControl.setValue("video", null); //$NON-NLS-1$
								vClipControl.getPropertyContent().remove(prop);
							} else {
								// remove video clip property entirely
								XMLProperty prop = next.getParentProperty();
								control.setValue(prop.getPropertyName(), null);
								control.getPropertyContent().remove(prop);
							}
						}
						continue;
					} else if (!choices.contains(next)) {
						XMLProperty prop = next.getParentProperty();
						XMLProperty parent = prop.getParentProperty();
						if (parent == control) {
							control.setValue(prop.getPropertyName(), null);
						}
						parent.getPropertyContent().remove(prop);
					}
				}
				// if no tracks are selected, eliminate tracks property
				boolean deleteTracks = true;
				for (Object next : control.getPropertyContent()) {
					XMLProperty prop = (XMLProperty) next;
					if ("tracks".indexOf(prop.getPropertyName()) > -1) { //$NON-NLS-1$
						deleteTracks = prop.getChildControls().length == 0;
					}
				}
				if (deleteTracks) {
					control.setValue("tracks", null); //$NON-NLS-1$
				}
				ok.run();
			}

		};

		ListChooser dialog = (isExport ?
		// create a list chooser
				new ListChooser(TrackerRes.getString("TrackerIO.Dialog.Export.Title"), //$NON-NLS-1$
						TrackerRes.getString("TrackerIO.Dialog.Export.Message"), //$NON-NLS-1$
						trackerPanel, listener)
				: new ListChooser(TrackerRes.getString("TrackerIO.Dialog.Import.Title"), //$NON-NLS-1$
						TrackerRes.getString("TrackerIO.Dialog.Import.Message"), //$NON-NLS-1$
						trackerPanel, listener));

		dialog.choose(choices, names, null, null, null, null);
	}

	/**
	 * Copies an xml string representation of the specified object to the system
	 * clipboard.
	 *
	 * @param obj the object to copy
	 */
	public static void copyXML(Object obj) {
		XMLControl control = new XMLControlElement(obj);
		StringSelection data = new StringSelection(control.toXML());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(data, data);
	}

	/**
	 * Copies data in the specified datatable to the system clipboard.
	 *
	 * @param table       the datatable to copy
	 * @param asFormatted true to retain table formatting
	 * @param header      the table header
	 */
	public static void copyTable(DataTable table, boolean asFormatted, String header) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringBuffer buf = getData(table, asFormatted);
		// replace spaces with underscores in header (must be single string)
		header = header.replace(' ', '_');
		if (!header.endsWith(XML.NEW_LINE))
			header += XML.NEW_LINE;
		StringSelection stringSelection = new StringSelection(header + buf.toString());
		clipboard.setContents(stringSelection, stringSelection);
		dataCopiedToClipboard = true;
	}

	/**
	 * Copies the specified image to the system clipboard.
	 *
	 * @param image the image to copy
	 */
	public static void copyImage(Image image) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferImage(image), null);
	}

	/**
	 * Returns the image on the clipboard, if any.
	 *
	 * @return the image, or null if none found
	 */
	public static Image getClipboardImage() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
				return image;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the data selected by the user in a datatable. This method is modified
	 * from the org.opensourcephysics.display.DataTableFrame getSelectedData method.
	 *
	 * @param table       the datatable containing the data
	 * @param asFormatted true to retain table formatting
	 * @return a StringBuffer containing the data.
	 */
	public static StringBuffer getData(DataTable table, boolean asFormatted) {
		StringBuffer buf = new StringBuffer();
		// get selected data
		int[] selectedRows = table.getSelectedRows();
		int[] selectedColumns = table.getSelectedColumns();
		// if no data is selected, select all
		int[] restoreRows = null;
		int[] restoreColumns = null;
		if (selectedRows.length == 0) {
			table.selectAll();
			restoreRows = selectedRows;
			restoreColumns = selectedColumns;
			selectedRows = table.getSelectedRows();
			selectedColumns = table.getSelectedColumns();
		}
		// copy column headings
		for (int j = 0; j < selectedColumns.length; j++) {
			// ignore row heading
			if (table.isRowNumberVisible() && selectedColumns[j] == 0)
				continue;
			buf.append(table.getColumnName(selectedColumns[j]));
			if (j < selectedColumns.length - 1)
				buf.append(delimiter); // add delimiter after each column except the last
		}
		buf.append(XML.NEW_LINE);
		java.text.DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		nf.applyPattern("0.000000000E0"); //$NON-NLS-1$
		nf.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
		java.text.DateFormat df = java.text.DateFormat.getInstance();
		for (int i = 0; i < selectedRows.length; i++) {
			for (int j = 0; j < selectedColumns.length; j++) {
				int temp = table.convertColumnIndexToModel(selectedColumns[j]);
				if (table.isRowNumberVisible()) {
					if (temp == 0) { // don't copy row numbers
						continue;
					}
				}
				Object value = null;
				if (asFormatted) {
					value = table.getFormattedValueAt(selectedRows[i], selectedColumns[j]);
				} else {
					value = table.getValueAt(selectedRows[i], selectedColumns[j]);
					if (value != null) {
						if (value instanceof Number) {
							value = nf.format(value);
						} else if (value instanceof java.util.Date) {
							value = df.format(value);
						}
					}
				}
				if (value != null) {
					buf.append(value);
				}
				if (j < selectedColumns.length - 1)
					buf.append(delimiter); // add delimiter after each column except the last
			}
			buf.append(XML.NEW_LINE); // new line after each row
		}
		if (restoreRows != null) {
			// restore previous selection state
			table.clearSelection();
			for (int row : restoreRows)
				table.addRowSelectionInterval(row, row);
			for (int col : restoreColumns)
				table.addColumnSelectionInterval(col, col);
		}
		return buf;
	}

	/**
	 * Sets the delimiter for copied or exported data
	 *
	 * @param d the delimiter
	 */
	public static void setDelimiter(String d) {
		if (d != null)
			delimiter = d;
	}

	/**
	 * Gets the delimiter for copied or exported data
	 *
	 * @return the delimiter
	 */
	public static String getDelimiter() {
		return delimiter;
	}

	/**
	 * Adds a custom delimiter to the collection of delimiters
	 *
	 * @param custom the delimiter to add
	 */
	public static void addCustomDelimiter(String custom) {
		if (!delimiters.values().contains(custom)) { // don't add a standard delimiter
			// by default, use delimiter itself for key (used for display purposes--could be
			// description)
			customDelimiters.put(custom, custom);
		}
	}

	/**
	 * Removes a custom delimiter from the collection of delimiters
	 *
	 * @param custom the delimiter to remove
	 */
	public static void removeCustomDelimiter(String custom) {
		if (getDelimiter().equals(custom))
			setDelimiter(defaultDelimiter);
		String selected = null;
		for (String key : customDelimiters.keySet()) {
			if (customDelimiters.get(key).equals(custom))
				selected = key;
		}
		if (selected != null)
			customDelimiters.remove(selected);
	}

	/**
	 * Finds page view file paths in an XMLControl and maps the page view path to
	 * the URL path of the file. If the page view path refers to a file inside a
	 * trk, zip or jar file, then all files in the jar are extracted and the URL
	 * path points to the extracted HTML file. This ensures that the HTML page can
	 * be opened on the desktop.
	 */
	private static void findPageViewFiles(XMLControl control, Map<String, String> pageViewFiles) {
		// extract page view filenames from control xml
		String xml = control.toXML();
		// basic unit is a tab with title and text
		String token = "PageTView$TabView"; //$NON-NLS-1$
		int j = xml.indexOf(token);
		while (j > -1) { // found page view tab
			xml = xml.substring(j + token.length());
			// get text and check if it is a loadable path
			token = "<property name=\"text\" type=\"string\">"; //$NON-NLS-1$
			j = xml.indexOf(token);
			String path = xml.substring(j + token.length());
			j = path.indexOf("</property>"); //$NON-NLS-1$
			path = path.substring(0, j);
			if (path.endsWith(".html") || path.endsWith(".htm")) { //$NON-NLS-1$ //$NON-NLS-2$
				Resource res = ResourceLoader.getResource(path);
				if (res != null) {
					// found an HTML file, so add it to the map
					String urlPath = res.getURL().toExternalForm();
					if (OSPRuntime.unzipFiles) {
						String zipPath = ResourceLoader.getNonURIPath(res.getAbsolutePath());
						int n = zipPath.indexOf("!/"); //$NON-NLS-1$
						// extract files from jar, zip or trz files into temp directory
						if (n > 0) {
							File target = new File(OSPRuntime.tempDir); // $NON-NLS-1$
							zipPath = zipPath.substring(0, n);
							ResourceLoader.unzip(zipPath, target, true); // overwrite
							target = new File(target, path);
							if (target.exists()) {
								res = ResourceLoader.getResource(target.getAbsolutePath());
								urlPath = res.getURL().toExternalForm();
							} else {
								path = null;
							}
						}
					}
					if (path != null) {
						pageViewFiles.put(path, urlPath);
					}
				}
			}

			// look for the next tab
			token = "PageTView$TabView"; //$NON-NLS-1$
			j = xml.indexOf(token);
		}

	}

	/**
	 * ComponentImage class for printing and copying images of components. This is
	 * adapted from code in SnapshotTool and DrawingPanel
	 */
	static class ComponentImage implements Printable {
		private BufferedImage image;
		Component c;

		ComponentImage(Component comp) {
			c = comp;
			if (comp instanceof JFrame)
				comp = ((JFrame) comp).getContentPane();
			else if (comp instanceof JDialog)
				comp = ((JDialog) comp).getContentPane();
			int w = (comp.isVisible()) ? comp.getWidth() : comp.getPreferredSize().width;
			int h = (comp.isVisible()) ? comp.getHeight() : comp.getPreferredSize().height;
			image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			if (comp instanceof Renderable)
				image = ((Renderable) comp).render(image);
			else {
				java.awt.Graphics g = image.getGraphics();
				comp.paint(g);
				g.dispose();
			}
		}

		BufferedImage getImage() {
			return image;
		}

		void copyToClipboard() {
			copyImage(image);
		}

		/** Implements Printable */
		public void print() {
			PrinterJob printerJob = PrinterJob.getPrinterJob();
			PageFormat format = new PageFormat();
			java.awt.print.Book book = new java.awt.print.Book();
			book.append(this, format);
			printerJob.setPageable(book);
			if (printerJob.printDialog()) {
				try {
					printerJob.print();
				} catch (PrinterException pe) {
					JOptionPane.showMessageDialog(c, TrackerRes.getString("TActions.Dialog.PrintError.Message"), //$NON-NLS-1$
							TrackerRes.getString("TActions.Dialog.PrintError.Title"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}

		/**
		 * Implements Printable.
		 * 
		 * @param g          the printer graphics
		 * @param pageFormat the format
		 * @param pageIndex  the page number
		 * @return status code
		 */
		@Override
		public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
			if (pageIndex >= 1) { // only one page available
				return Printable.NO_SUCH_PAGE;
			}
			if (g == null) {
				return Printable.NO_SUCH_PAGE;
			}
			Graphics2D g2 = (Graphics2D) g;
			double scalex = pageFormat.getImageableWidth() / image.getWidth();
			double scaley = pageFormat.getImageableHeight() / image.getHeight();
			double scale = Math.min(scalex, scaley);
			scale = Math.min(scale, 1.0); // don't magnify images--only reduce if nec
			g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
			g2.scale(scale, scale);
			g2.drawImage(image, 0, 0, null);
			return Printable.PAGE_EXISTS;
		}

	}

	static class AsyncLoader extends AsyncSwingWorker implements TrackerMonitor {

		private final List<String> paths;
		private final TrackerPanel existingPanel;
		private final TFrame frame;
		private final ArrayList<String> desktopFiles = new ArrayList<>();
		private final long t0;

		

		private static final int TYPE_UNK = 0;
		private static final int TYPE_TRZ = 1;
		private static final int TYPE_TRK = 2;
		private static final int TYPE_FRAME = 3;
		private static final int TYPE_VIDEO = 4;
		private static final int TYPE_UNSUPPORTED_VIDEO = 5;

		private boolean panelChanged;
		private TrackerPanel trackerPanel;
		private String rawPath;
		private String nonURIPath;
		private XMLControlElement control;
		private String path, path0;
		private int type = TYPE_UNK;
		private int frameCount;
		private String name;
		private String title; // BH TODO
		private boolean stopped; // BH TODO
		private String xmlPath, xmlPath0;
		private Runnable whenDone;
		private List<VideoPanel> panelList = new ArrayList<>();

		/**
		 * 
		 * @param paths  or more paths to load in sequence
		 * @param existingPanel  (video only)
		 * @param frame
		 * @param whenDone
		 */
		public AsyncLoader(List<String> paths, TrackerPanel existingPanel, TFrame frame, Runnable whenDone) {
			super(frame, paths.get(0), (whenDone == null ? 0 : 10), 0, 100);
			path = path0 = name = paths.remove(0);
			this.paths = paths;
			isAsync = (delayMillis > 0);
			this.existingPanel = existingPanel;
			this.frame = frame;
			this.whenDone = whenDone;
			monitors.add(this);
			OSPLog.debug(Performance.timeCheckStr("TrackerIO.asyncLoad start " + paths, Performance.TIME_MARK));
			t0 = Performance.now(0);
		}

		@Override
		public void initAsync() {
			setupLoader();
		}

		private boolean setupLoader() {
			xmlPath = null;
			trackerPanel = null;
			title = null;
			stopped = false;
			panelChanged = false;
			nonURIPath = null;
			frameCount = 0;
			control = null;
			rawPath = path;
			OSPLog.debug("TrackerIO.AsyncLoader: " + path);			
			path = ResourceLoader.getURIPath(path);
			isffmpegError = false;
			theFrame = frame;
			setCanceled(false);
			// prevent circular references when loading tabsets
			nonURIPath = ResourceLoader.getNonURIPath(path);
			if (rawPath.startsWith("//") && nonURIPath.startsWith("/") && !nonURIPath.startsWith("//")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				nonURIPath = "/" + nonURIPath; //$NON-NLS-1$
			if (frame.loadedFiles.contains(nonURIPath)) {
				OSPLog.debug("TrackerIO path already loaded " + nonURIPath); //$NON-NLS-1$
				return false;
			}
			frame.loadedFiles.add(nonURIPath);
			if (!ResourceLoader.isHTTP(path))
				path = nonURIPath;

//			// create progress monitor
//			monitorDialog = new MonitorDialog(frame, path);
//			monitorDialog.setVisible(true);
//			monitors.add(monitorDialog);

			// load data from zip or trz file
			
			boolean isTRZ = ResourceLoader.isJarZipTrz(path, false);
			
			if (isTRZ || path.indexOf("&TrackerSet=") >= 0) {
				type = TYPE_TRZ;
				trackerPanel = frame.getCleanTrackerPanel();
				frame.holdPainting(true);
				return true;
			}

			
			// BH note - this file is not likely to exist without its pathname.
			// changed to just check extensions, not if directory (which requires an
			// existence check)
			File testFile = new File(XML.getName(path));			
			if (videoFileFilter.accept(testFile, false)) {
				type = TYPE_VIDEO;
				trackerPanel = (existingPanel == null ? frame.getCleanTrackerPanel() : existingPanel);
				panelChanged = trackerPanel.changed;
				return true;
			}
			
			// check for unsupported video type
			for (String ext : VideoIO.KNOWN_VIDEO_EXTENSIONS) {
				if (path.endsWith("." + ext)) {
					type = TYPE_UNSUPPORTED_VIDEO;
					return true;
				}
			}

			// load data from TRK file
			control = new XMLControlElement();
			xmlPath = control.read(path);
			if (path.equals(path0))
				xmlPath0 = xmlPath;
			if (isCanceled()) {
				cancelAsync();
				return false;
			}
			Class<?> ctype = control.getObjectClass();

			if (TrackerPanel.class.isAssignableFrom(ctype)) {
				type = TYPE_TRK;
				trackerPanel = frame.getCleanTrackerPanel();
				return true;
			}

			if (TFrame.class.isAssignableFrom(ctype)) {
				type = TYPE_FRAME;
				return true;
			}
			// FAILURE
			if (control.failedToRead()) {
				JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
						MediaRes.getString("VideoIO.Dialog.BadFile.Message") + //$NON-NLS-1$
								ResourceLoader.getNonURIPath(path));
			} else {
				JOptionPane.showMessageDialog(trackerPanel.getTFrame(), "\"" + XML.getName(path) + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
						MediaRes.getString("VideoIO.Dialog.XMLMismatch.Message"), //$NON-NLS-1$
						MediaRes.getString("VideoIO.Dialog.XMLMismatch.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}
			setCanceled(true);
			cancelAsync();
			return false;
		}
		@Override
		public int doInBackgroundAsync(int progress) {
			if (frame.libraryBrowser != null && frame.libraryBrowser.isCancelled())
				return 100;
			OSPLog.debug(Performance.timeCheckStr("TrackerIO.asyncLoad " + type + " start " + progress + " " + paths,
					Performance.TIME_MARK));
			// type is set in setupLoader, from initAsync()
			switch (type) {
			case TYPE_FRAME:
				progress = loadFrame(progress);
				break;
			case TYPE_TRZ:
				progress = loadTRZ(progress);
				break;
			case TYPE_TRK:
				progress = loadTRK(progress);
				break;
			case TYPE_VIDEO:
				progress = loadVideo(progress);
				break;
			case TYPE_UNSUPPORTED_VIDEO:
				VideoIO.handleUnsupportedVideo(path, XML.getExtension(path), null, trackerPanel, "TrackerIO.unsupp video-asyncLoad");
			default:
				return 100;
			}
			OSPLog.debug(Performance.timeCheckStr("TrackerIO.asyncLoad " + type + " end " + progress + " " + path,
					Performance.TIME_MARK));
			if (progress == 100) {
				if (paths.size() > 0) {
					path = paths.remove(0);
					if (setupLoader())
						progress = 0;
				} else {
					// remove the initial tab if there is more than one tab now
					frame.removeEmptyTab(1);
				}
				
			}
			return progress;
		}

		@Override
		public void doneAsync() {
			
			if (path.equals(path0)) {
				doneLoading();
			}
		}

		private int loadFrame(int progress) {
			// loadObject will initiate its own loader.
			
//			Tracker.addRecent(ResourceLoader.getNonURIPath(XML.forwardSlash(rawPath)), false); // add at beginning
			frame.whenObjectLoadingComplete = new Function<List<String>, Void>() {

				@Override
				public Void apply(List<String> files) {
					if (files.size() > 0) {
						File dataFile = new File(files.remove(0));
						paths.addAll(files);
						Runnable done = whenDone;
						whenDone = new Runnable() {

							@Override
							public void run() {
								frame.setSelectedTab(dataFile);								
								if (done != null)
									done.run();
							}
							
						};
					}
//					TMenuBar.refreshMenus(frame.getSelectedPanel(), TMenuBar.REFRESH_TRACKERIO_OPENFRAME);
					return null;
				}
				
			};
			control.loadObject(frame);
			return 100;
		}

		private int loadTRZ(int progress) {
			// create progress monitor
			Map<String, String> pageViewTabs = new HashMap<String, String>(); 
			// pageView tabs that display html files
			String name = XML.getName(ResourceLoader.getNonURIPath(path));
			// download web files to OSP cache
			boolean isWebPath = ResourceLoader.isHTTP(path);
			if (isWebPath) {
				File localFile = ResourceLoader.downloadToOSPCache(path, name, false);
				if (localFile != null) {
					// set path to downloaded file
					path = localFile.toURI().toString();
					OSPLog.debug("TrackerIO downloaded zip file: " + path); //$NON-NLS-1$
				}
			}

			ArrayList<String> trkFiles = new ArrayList<String>(); // all trk files found in zip
			ArrayList<String> htmlFiles = new ArrayList<String>(); // supplemental html files found in zip
			ArrayList<String> pdfFiles = new ArrayList<String>(); // all pdf files found in zip
			ArrayList<String> otherFiles = new ArrayList<String>(); // other files found in zip
			String trkForTFrame = null;

			// sort the zip file contents
			Map<String, ZipEntry> contents = ResourceLoader.getZipContents(path);
			// first determine baseName shared by thumbnail, html and (usually) zip file
			// eg example.trz, example_info.html, example_thumbnail.png
			String baseName = XML.stripExtension(name);  // first guess: filename
			for (String next : contents.keySet()) {
				if (next.indexOf("_thumbnail") > -1) {
					String thumb = XML.getName(next);
					baseName = thumb.substring(0, thumb.indexOf("_thumbnail"));
				}
			}
			for (String next : contents.keySet()) {
				if (next.endsWith(".trk")) { //$NON-NLS-1$
					String s = ResourceLoader.getURIPath(path + "!/" + next); //$NON-NLS-1$
					OSPLog.debug("TrackerIO found trk file " + s); //$NON-NLS-1$
					trkFiles.add(s);
				} else if (next.endsWith(".pdf")) { //$NON-NLS-1$
					pdfFiles.add(next);
				} else if (next.endsWith(".html") || next.endsWith(".htm")) { //$NON-NLS-1$ //$NON-NLS-2$
					// handle HTML info files (name "<basename>_info")
					String nextName = XML.getName(next);
					if (XML.stripExtension(nextName).equals(baseName + "_info")) { //$NON-NLS-1$
						continue;
					}
					// add non-info html files to list
					htmlFiles.add(next);
				}
				// collect other files in top directory except thumbnails and videos
				else if (next.indexOf("thumbnail") == -1 && next.indexOf("/") == -1
						&& !isKnownVideoExtension(next)) { //$NON-NLS-1$ //$NON-NLS-2$
					String s = ResourceLoader.getURIPath(path + "!/" + next); //$NON-NLS-1$
					OSPLog.debug("TrackerIO found other file " + s); //$NON-NLS-1$
					otherFiles.add(next);
				}
			}
			if (trkFiles.isEmpty() && pdfFiles.isEmpty() && htmlFiles.isEmpty() && otherFiles.isEmpty()) {
				String s = TrackerRes.getString("TFrame.Dialog.LibraryError.Message"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(frame, s + " \"" + name + "\".", //$NON-NLS-1$ //$NON-NLS-2$
						TrackerRes.getString("TFrame.Dialog.LibraryError.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
				return 100;
			}

			// find page view filenames in TrackerPanel xmlControls
			// also look for trk for TFrame
			boolean haveHTML = !htmlFiles.isEmpty();
			if (!trkFiles.isEmpty()) {
				ArrayList<String> trkNames = new ArrayList<String>();
				for (String next : trkFiles) {
					trkNames.add(XML.stripExtension(XML.getName(next)));
					try {
						String data = new String(ResourceLoader.getZipEntryBytes(path, next, null));
						String className = XMLControlElement.getClassName(data);
						if (className.endsWith("TrackerPanel")) { //$NON-NLS-1$
							if (haveHTML)
								findPageViewFiles(new XMLControlElement(data), pageViewTabs);
						} else if (trkForTFrame == null && className.endsWith("TFrame")) { //$NON-NLS-1$
							trkForTFrame = next;
						}
					} catch (IOException e) {
					}
				}
				if (!htmlFiles.isEmpty()) {
					// remove page view HTML files
					String[] paths = htmlFiles.toArray(new String[htmlFiles.size()]);
					for (String htmlPath : paths) {
						boolean isPageView = false;
						for (String page : pageViewTabs.keySet()) {
							isPageView = isPageView || htmlPath.endsWith(page);
						}
						if (isPageView) {
							htmlFiles.remove(htmlPath);
						}
						// discard HTML <trkname>_info files
						for (String trkName : trkNames) {
							if (htmlPath.contains(trkName + "_info.")) { //$NON-NLS-1$
								htmlFiles.remove(htmlPath);
							}
						}
					}
				}
				if (trkForTFrame != null) {
					trkFiles.clear();
					trkFiles.add(trkForTFrame);
				}
			}

			// unzip pdf/html/other files into temp directory and open on desktop
			ArrayList<String> tempFiles = new ArrayList<String>();
			if (!htmlFiles.isEmpty() || !pdfFiles.isEmpty() || !otherFiles.isEmpty()) {
				if (OSPRuntime.unzipFiles) {

					File temp = new File(OSPRuntime.tempDir); // $NON-NLS-1$
					Set<File> files = ResourceLoader.unzip(path, temp, true);
					for (File next : files) {
						next.deleteOnExit();
						// add PDF/HTML/other files to tempFiles
						System.out.println(next);
						String relPath = XML.getPathRelativeTo(next.getPath(), temp.getPath());
						if (pdfFiles.contains(relPath) || htmlFiles.contains(relPath) || otherFiles.contains(relPath)) {
							String tempPath = ResourceLoader.getURIPath(next.getAbsolutePath());
							tempFiles.add(tempPath);
						}
					}
				} else {
					tempFiles.addAll(htmlFiles);
					tempFiles.addAll(pdfFiles);
					tempFiles.addAll(otherFiles);
				}
				// open tempfiles on the desktop
				if (OSPRuntime.skipDisplayOfPDF) {
				} else {
					Thread displayURLOpener = new Thread(() -> {
						for (String relpath : tempFiles) {
								OSPDesktop.displayURL(OSPRuntime.unzipFiles ? relpath : path + "!/" + relpath);
						}
					});
					displayURLOpener.setName("displayURLOpener");
					displayURLOpener.start();
				}
			}
			// load trk files into Tracker
			if (!isCanceled()) {
				// add path to recent files
				if (path.equals(path0))
					Tracker.addRecent(nonURIPath, false); // add at beginning
				paths.addAll(trkFiles);
				desktopFiles.addAll(tempFiles);
			}
			return 100;
		}

		private int loadTRK(int progress) {
//			XMLControl child = control.getChildControl("videoclip"); //$NON-NLS-1$
//			if (child != null) {
//				int count = child.getInt("video_framecount"); //$NON-NLS-1$
//				child = child.getChildControl("video"); //$NON-NLS-1$
//				if (child != null) {
//					String vidPath = child.getString("path"); //$NON-NLS-1$
//					monitorDialog.setName(vidPath);
//					monitorDialog.setFrameCount(count);
//				}
//			}
			panelList.add(trackerPanel);
			trackerPanel = (TrackerPanel) control.loadObject(trackerPanel, this);
			trackerPanel.setIgnoreRepaint(true);

			// find page view files and add to TrackerPanel.pageViewFilePaths
			findPageViewFiles(control, trackerPanel.pageViewFilePaths);

			while (desktopFiles.size() > 0) {
				trackerPanel.supplementalFilePaths.add(desktopFiles.remove(0));
			}
			if (ResourceLoader.isJarZipTrz(xmlPath,  true)) {
				String parent = xmlPath.substring(0, xmlPath.indexOf("!")); //$NON-NLS-1$
				parent = ResourceLoader.getNonURIPath(parent); // strip protocol
				String parentName = XML.stripExtension(XML.getName(parent));
				String tabName = XML.stripExtension(XML.getName(xmlPath));
				if (tabName.startsWith(parentName) && parentName.length() + 1 < tabName.length()) {
					tabName = tabName.substring(parentName.length() + 1, tabName.length());
				}
				trackerPanel.openedFromPath = parent;
				trackerPanel.defaultFileName = tabName;

				String html = ResourceLoader.getString(parent + "!/html/" + parentName + "_info.html"); //$NON-NLS-1$ //$NON-NLS-2$
				if (html != null) {
					ArrayList<String[]> metadata = LibraryBrowser.getMetadataFromHTML(html);
					for (int i = 0; i < metadata.size(); i++) {
						String[] meta = metadata.get(i);
						String key = meta[0];
						String value = meta[1];
						if (trackerPanel.author == null
								&& LibraryResource.META_AUTHOR.toLowerCase().contains(key.toLowerCase())) {
							trackerPanel.author = value;
						} else if (trackerPanel.contact == null
								&& LibraryResource.META_CONTACT.toLowerCase().contains(key.toLowerCase())) {
							trackerPanel.contact = value;
						}
					}
				}
			} else {
				trackerPanel.defaultFileName = XML.getName(path);
				trackerPanel.openedFromPath = path;
				trackerPanel.setDataFile(new File(ResourceLoader.getNonURIPath(path)));
			}

//			if (monitorDialog.isVisible())
//				monitorDialog.setProgress(80);
			if (isCanceled())
				return 100;
			frame.addTab(trackerPanel, null);
//			if (monitorDialog.isVisible())
//				monitorDialog.setProgress(90);
			frame.setSelectedTab(trackerPanel);
			frame.showTrackControl(trackerPanel);
			// BH ah, but asynchronous load may not have been completed yet.
//			frame.showNotes(trackerPanel);
			trackerPanel.setIgnoreRepaint(false);
//			frame.refresh();
			if (control.failedToRead()) {
				JOptionPane.showMessageDialog(trackerPanel.getTFrame(), "\"" + XML.getName(path) + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
						TrackerRes.getString("TrackerIO.Dialog.ReadFailed.Message"), //$NON-NLS-1$
						TrackerRes.getString("TrackerIO.Dialog.ReadFailed.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}

			checkDone(false);
			
			return 100;
		}

		void checkDone(boolean b) {
			if (b == (trackerPanel.getVideo() instanceof AsyncVideoI)) {
				if (panelList.size() == 0 && paths.size() == 0)
					doneLoading();
			}
		}

		private int loadVideo(int progress) {
			// check for unsupported MP4 videos
			if ((path.toLowerCase().endsWith("mp4"))
					&& !VideoIO.isLoadableMP4(path, (codec) -> {
						VideoIO.handleUnsupportedVideo(path, "mp4", codec, trackerPanel, "TrackerIO.asyncLoad");
					})) 
				return 100;
			
			trackerPanel.setTFrame(frame);
			OSPLog.debug("TrackerIO opening video path " + path); //$NON-NLS-1$
			// download web videos to the OSP cache
			if (ResourceLoader.isHTTP(path)) {
				String name = ResourceLoader.getNonURIPath(XML.getName(path));
				File localFile = ResourceLoader.downloadToOSPCache(path, name, false);
				if (localFile != null) {
					path = localFile.toURI().toString();
				}
			}

			// attempt to load video
			boolean logConsole = OSPLog.isConsoleMessagesLogged();
			if (!Tracker.warnXuggleError)
				OSPLog.setConsoleMessagesLogged(false);
			Video video = getVideo(path, null);
			OSPLog.setConsoleMessagesLogged(logConsole);
//			monitorDialog.stop();
			if (isCanceled()) {
				cancelAsync();
				// monitorDialog.close();
				return 100;
			}
			if (video == null) {
				// unable to load video
				if (frame.libraryBrowser != null) 
					frame.libraryBrowser.setMessage(null, null);
				String codec = VideoIO.getVideoCodec(path);
				VideoIO.handleUnsupportedVideo(path, XML.getExtension(path), codec, trackerPanel, "OpenTabPathVideo null video");
				cancelAsync();
				// monitorDialog.close();
				return 100;
			}
			// if (monitorDialog.isVisible())
			// monitorDialog.setProgress(85);
			VideoType vidType = (VideoType) video.getProperty("video_type"); //$NON-NLS-1$
			OSPLog.finer(video.getProperty("path") + " opened as " + //$NON-NLS-1$ //$NON-NLS-2$
					vidType.getClass().getSimpleName() + " " + vidType.getDescription()); //$NON-NLS-1$
			if (isCanceled())
				return 100;
			if (video instanceof AsyncVideoI) {
				video.addPropertyChangeListener(AsyncVideoI.PROPERTY_ASYNCVIDEOI_READY, new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						video.removePropertyChangeListener(AsyncVideoI.PROPERTY_ASYNCVIDEOI_READY, this);
						finalizeVideoLoading(video);
					}

				});
			} else {
				finalizeVideoLoading(video);
			}
			return 100;

		}

		private void finalizeVideoLoading(Video video) {
			frame.addTab(trackerPanel, null);
//			if (monitorDialog.isVisible())
//				monitorDialog.setProgress(95);
			JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
			pane.setDividerLocation(TFrame.DEFAULT_RIGHT_DIVIDER);

			// BH ?? TMenuBar.refreshMenus(trackerPanel, TMenuBar.REFRESH_BEFORESETVIDEO);
			trackerPanel.setVideo(video);
			// panel is changed if video imported into existing trackerPanel
			panelChanged = (trackerPanel == existingPanel);
			if (video.getFrameCount() == 1) {
				trackerPanel.getPlayer().getVideoClip().setStepCount(10);
			}
			// if new trackerPanel, move coords origin to center of video
			if (existingPanel == null) {
				ImageCoordSystem coords = trackerPanel.getCoords();
				coords.setAllOriginsXY(video.getWidth() / 2, video.getHeight() / 2);
			}
			TFrame.repaintT(trackerPanel);
			frame.setSelectedTab(trackerPanel);
//			monitorDialog.close();
			// check for video frames with durations that vary by 20% from average
			if (Tracker.warnVariableDuration)
				findBadVideoFrames(trackerPanel, defaultBadFrameTolerance, true, true, true);
			// show dialog only if bad frames found, and include "don't show again" button
		}

		public void finalized(VideoPanel trackerPanel) {
			panelList.remove(trackerPanel);
			if (panelList.size() == 0 && paths.size() == 0 && trackerPanel.getVideo() instanceof AsyncVideoI) {
				doneLoading();
			}
		}

		private void doneLoading() {
//			monitorDialog.close();
			if (xmlPath0 != null && !ResourceLoader.isJarZipTrz(xmlPath0,  true)) { //$NON-NLS-1$
				Tracker.addRecent(ResourceLoader.getNonURIPath(XML.forwardSlash(xmlPath0)), false); // add at beginning
			}

			TTrackBar.refreshMemoryButton();

			switch (type) {
			case TYPE_VIDEO:
				trackerPanel.changed = panelChanged;
				// fall through
			case TYPE_TRK:
				frame.clearHoldPainting();
				trackerPanel.notifyLoadingComplete();
				frame.refresh();
//				TFrame.repaintT(trackerPanel);

			}
			OSPLog.debug(Performance.timeCheckStr("TrackerIO.asyncLoad done " + path, Performance.TIME_MARK));
			OSPLog.debug("!!! " + Performance.now(t0) + " AyncLoad " + path);

			if (whenDone == null) {
			} else {
				SwingUtilities.invokeLater(() -> {
					whenDone.run();
				});

			}

		}

		@Override
		public void stop() {
			stopped = true;
			frame.clearHoldPainting();
		}

		@Override
		public void setFrameCount(int count) {
			frameCount = count;
		}

		@Override
		public void close() {
			cancelAsync();
			setProgress(100);
		}

		@Override
		public void cancelAsync() {
			super.cancelAsync();
			frame.clearHoldPainting();
		}

		@Override
		public int getFrameCount() {
			return frameCount;
		}

		@Override
		public void restart() {
			setProgress(0);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}

		public TFrame getFrame() {
			return frame;
		}

	}

//	static class MonitorDialog extends JDialog implements TrackerMonitor {
//
//		JProgressBar monitor;
//		Timer timer;
//		int frameCount = Integer.MIN_VALUE;
//
//		MonitorDialog(TFrame frame, String path) {
//			super(frame, false);
//			setName(path);
//			JPanel contentPane = new JPanel(new BorderLayout());
//			setContentPane(contentPane);
//			monitor = new JProgressBar(0, 100);
//			monitor.setValue(0);
//			monitor.setStringPainted(true);
//			// make timer to step progress forward slowly
//			timer = new Timer(300, new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					if (!isVisible())
//						return;
//					int progress = monitor.getValue() + 1;
//					if (progress <= 20)
//						monitor.setValue(progress);
//				}
//			});
//			timer.setRepeats(true);
//			this.addWindowListener(new WindowAdapter() {
//				@Override
//				public void windowClosing(WindowEvent e) {
//					VideoIO.setCanceled(true);
//				}
//			});
////	  	// give user a way to close unwanted dialog: double-click
////	  	addMouseListener(new MouseAdapter() {
////	  		public void mouseClicked(MouseEvent e) {
////	  			if (e.getClickCount()==2) {
////	        	close();
////	  			}
////	  		}
////	  	});
//			JPanel progressPanel = new JPanel(new BorderLayout());
//			progressPanel.setBorder(BorderFactory.createEmptyBorder(4, 30, 8, 30));
//			progressPanel.add(monitor, BorderLayout.CENTER);
//			progressPanel.setOpaque(false);
//			JLabel label = new JLabel(TrackerRes.getString("Tracker.Splash.Loading") //$NON-NLS-1$
//					+ " \"" + XML.getName(path) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//			JPanel labelbar = new JPanel();
//			labelbar.add(label);
//			JButton cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
//			cancelButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					VideoIO.setCanceled(true);
//					close();
//				}
//			});
//			JPanel buttonbar = new JPanel();
//			buttonbar.add(cancelButton);
//			contentPane.add(labelbar, BorderLayout.NORTH);
//			contentPane.add(progressPanel, BorderLayout.CENTER);
//			contentPane.add(buttonbar, BorderLayout.SOUTH);
//			FontSizer.setFonts(contentPane, FontSizer.getLevel());
//			pack();
//			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//			int x = (dim.width - getBounds().width) / 2;
//			int y = (dim.height - getBounds().height) / 2;
//			setLocation(x, y);
//			timer.start();
//		}
//
//		@Override
//		public void stop() {
//			timer.stop();
//		}
//
//		@Override
//		public void restart() {
//			monitor.setValue(0);
//			frameCount = Integer.MIN_VALUE;
//			// restart timer
//			timer.start();
//		}
//
//		@Override
//		public void setProgressAsync(int progress) {
//			monitor.setValue(progress);
//		}
//
//		@Override
//		public void setFrameCount(int count) {
//			frameCount = count;
//		}
//
//		@Override
//		public int getFrameCount() {
//			return frameCount;
//		}
//
//		@Override
//		public void close() {
//			timer.stop();
//			setVisible(false);
//			monitors.remove(this);
//			dispose();
//		}
//
//	}

	/**
	 * Transferable class for copying images to the system clipboard.
	 */
	static class TransferImage implements Transferable {
		private Image image;

		TransferImage(Image image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return image;
		}
	}

	static void closeMonitor(String fileName) {
		for (TrackerMonitor monitor : monitors) {
			if (fileName == null) {
				monitor.close();
			} else if (XML.forwardSlash(monitor.getName()).endsWith(XML.forwardSlash(fileName))) {
				monitor.close();
				monitors.remove(monitor);
				return;
			}
		}
		monitors.clear();
	}

	static void setProgress(String name, String string, int framesLoaded) {
		for (TrackerMonitor monitor : monitors) {
			String monitorName = XML.forwardSlash(monitor.getName());
			if (monitorName.endsWith(name)) {
				int progress;
				if (monitor.getFrameCount() != Integer.MIN_VALUE) {
					progress = 20 + (int) (framesLoaded * 60.0 / monitor.getFrameCount());
				} else {
					progress = 20 + ((framesLoaded / 20) % 60);
				}
				monitor.setProgressAsync(progress);
				monitor.setTitle(
						TrackerRes.getString("TFrame.ProgressDialog.Title.FramesLoaded") + ": " + framesLoaded); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
		}
	}

	protected static Object[] getVideoFormats() {
		return videoFormatDescriptions.toArray();
	}

	/**
	 * Refreshes the format set.
	 */
	public static void refreshVideoFormats() {
		videoFormats.clear();
		videoFormatDescriptions.clear();
		// eliminate xuggle types if VideoIO engine is NONE
		for (VideoType next : getVideoTypes(true)) {
			String desc = next.getDescription();
			videoFormats.put(desc, next);
			videoFormatDescriptions.add(desc);
		}
	}

	public static String getVideoFormat(String preferredExtension) {
		String selected = selectedVideoFormat;
		boolean hasSelected = false;
		String preferred = null;
		for (String format : videoFormatDescriptions) {
			if (format.equals(selected))
				hasSelected = true;
			if (preferred == null && format.contains("." + preferredExtension)) { //$NON-NLS-1$
				preferred = format;
			}
		}
		return (preferred == null && hasSelected ? selected : preferred);
	}
	
	private static FileFilter[] imageFilters;

	private static FileFilter videoFilter;

//	public static boolean haveVideo(List<File> files) {
//		return (files != null && files.size() == 1 && isVideo(files.get(0)));
//	}


	/**
	 * Returns true if the specified file is an image.
	 * 
	 * @param file the File
	 * @return true if an image
	 */
	public static boolean isImageFile(File file) {
		if (imageFilters == null)
			imageFilters = new ImageVideoType().getFileFilters();
		for (int i = 0; i < imageFilters.length; i++) {
			if (imageFilters[i].accept(file))
				return true;
		}
		return false;
	}

	public static boolean isVideo(File f) {
		if (videoFilter == null)
			videoFilter = new VideoFileFilter();
		return videoFilter.accept(f);
	}

}
