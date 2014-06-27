/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2014  Douglas Brown
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

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This provides static methods for managing video and text input/output.
 *
 * @author Douglas Brown
 */
public class TrackerIO extends VideoIO {

	protected static final String TAB="\t", SPACE=" ",  //$NON-NLS-1$ //$NON-NLS-2$
  		COMMA=",", SEMICOLON=";"; //$NON-NLS-1$ //$NON-NLS-2$
  protected static FileFilter zipFileFilter, trkFileFilter, trzFileFilter, videoAndTrkFileFilter;
  protected static String defaultDelimiter = TAB;  // tab delimiter by default
  protected static String delimiter = defaultDelimiter;
  protected static Map<String, String> delimiters = new TreeMap<String, String>();
  protected static Map<String, String> customDelimiters = new TreeMap<String, String>();
  protected static boolean isffmpegError = false;
  protected static TFrame theFrame;
  protected static PropertyChangeListener ffmpegListener;
  protected static boolean loadInSeparateThread = true;
  protected static Set<MonitorDialog> monitors = new HashSet<MonitorDialog>();
  protected static double defaultBadFrameTolerance = 0.2;

  static {
  	ffmpegListener = new PropertyChangeListener() {
    	public void propertyChange(PropertyChangeEvent e) {
    		if (e.getPropertyName().equals("ffmpeg_error")) { //$NON-NLS-1$
    			if (!isffmpegError) { // first error thrown
    				isffmpegError = true;
    				if (!Tracker.warnXuggleError) {
    					if (e.getNewValue()!=null) {
	  						String s = e.getNewValue().toString();
	  						int n = s.indexOf("]"); //$NON-NLS-1$
	  						if (n>-1) s = s.substring(n+1);
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
    	    	if (n>-1) {
    	    		error = error.substring(n+1).trim();
    	    	}
     	    	box.add(new JLabel("  ")); //$NON-NLS-1$
     	    	JLabel erLabel = new JLabel("\""+error+"\""); //$NON-NLS-1$ //$NON-NLS-2$
     	    	erLabel.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));
    	    	box.add(erLabel);
     	    	box.add(new JLabel("  ")); //$NON-NLS-1$
     	      box.add(new JLabel(TrackerRes.getString("TrackerIO.Dialog.ErrorFFMPEG.Message2"))); //$NON-NLS-1$
    	    	
    	    	box.add(new JLabel("  ")); //$NON-NLS-1$
    	    	box.setBorder(BorderFactory.createEmptyBorder(20, 15, 0, 15));
    	    	
    				final JDialog dialog = new JDialog(theFrame, false);
    				JPanel contentPane = new JPanel(new BorderLayout());
    				dialog.setContentPane(contentPane);
    				contentPane.add(box, BorderLayout.CENTER);
    		    JButton closeButton = new JButton(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
    		    closeButton.setForeground(new Color(0, 0, 102));
    		    closeButton.addActionListener(new ActionListener() {
    		      public void actionPerformed(ActionEvent e) {
    		        dialog.setVisible(false);
    		      }
    		    });
    		    JButton dontShowAgainButton = new JButton(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox")); //$NON-NLS-1$
    		    dontShowAgainButton.setForeground(new Color(0, 0, 102));
    		    dontShowAgainButton.addActionListener(new ActionListener() {
    		      public void actionPerformed(ActionEvent e) {
    	    			Tracker.warnXuggleError = false;
    		        dialog.setVisible(false);
    		      }
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
    	}
    };
    OSPLog.getOSPLog().addPropertyChangeListener(ffmpegListener);
    zipFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        String extension = VideoIO.getExtension(f);
        if ("zip".equals(extension)) return true; //$NON-NLS-1$
        return false;
      }
      public String getDescription() {
      	return TrackerRes.getString("TrackerIO.ZipFileFilter.Description"); //$NON-NLS-1$
      }
    };
    trkFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (zipFileFilter.accept(f)) return true;
        if (trzFileFilter.accept(f)) return true;
        if (f.isDirectory()) return true;
        String extension = VideoIO.getExtension(f);
        if ("trk".equals(extension)) return true; //$NON-NLS-1$
        return false;
      }
      public String getDescription() {
      	return TrackerRes.getString("TrackerIO.DataFileFilter.Description"); //$NON-NLS-1$
      }
    };
    trzFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        String extension = VideoIO.getExtension(f);
        if ("trz".equals(extension)) return true; //$NON-NLS-1$
        return false;
      }
      public String getDescription() {
      	return TrackerRes.getString("TrackerIO.ZIPResourceFilter.Description"); //$NON-NLS-1$
      }
    };
    videoAndTrkFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (trkFileFilter.accept(f)) return true; // also accepts zip and trz
        if (videoFileFilter.accept(f)) return true;
        return false;
      }
      public String getDescription() {
      	return TrackerRes.getString("TrackerIO.VideoAndDataFileFilter.Description"); //$NON-NLS-1$
      }
    };
    delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Tab"), TAB); //$NON-NLS-1$
    delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Space"), SPACE); //$NON-NLS-1$
    delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Comma"), COMMA); //$NON-NLS-1$
    delimiters.put(TrackerRes.getString("TrackerIO.Delimiter.Semicolon"), SEMICOLON); //$NON-NLS-1$
  }
  
  /**
   * private constructor to prevent instantiation
   */
  private TrackerIO() {/** empty block */}

  /**
   * Writes TrackerPanel data to the specified file. If the file is null
   * it brings up a chooser.
   *
   * @param file the file to write to
   * @param trackerPanel the TrackerPanel
   * @return the file written to, or null if not written
   */
  public static File save(File file, TrackerPanel trackerPanel) {
  	trackerPanel.restoreViews();
  	getChooser().setAcceptAllFileFilterUsed(false);
  	chooser.addChoosableFileFilter(trkFileFilter);
  	chooser.setAccessory(null);
  	if (file==null && trackerPanel.getDataFile()==null) {
	  	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
	  	if (clip.getVideo()!=null || clip.getVideoPath()!=null) {
	  		File dir = new File(clip.getVideoPath()).getParentFile();
	  		chooser.setCurrentDirectory(dir);
	    }
  	}
  	
  	boolean isNew = file==null;
  	file = VideoIO.save(file, trackerPanel, 
  			TrackerRes.getString("TrackerIO.Dialog.SaveTab.Title")); //$NON-NLS-1$
  	chooser.removeChoosableFileFilter(trkFileFilter);
  	chooser.setAcceptAllFileFilterUsed(true);
  	if (isNew && file!=null) {
      Tracker.addRecent(XML.getAbsolutePath(file), false); // add at beginning
      TMenuBar.getMenuBar(trackerPanel).refresh();
  	}
  	return file;
  }
  
  /**
   * Saves a tabset in the specified file. If the file is null
   * this brings up a chooser.
   *
   * @param file the file to write to
   * @param frame the TFrame 
   * @return the file written to, or null if not written
   */
  public static File saveTabset(File file, TFrame frame) {
  	// count tabs with data files or unchanged (newly opened) videos
  	int n = 0;
  	for (int i = 0; i < frame.getTabCount(); i++) {
  		TrackerPanel trackerPanel = frame.getTrackerPanel(i);
  		if (trackerPanel.getDataFile()!=null) {
  			n++;
  			continue;
  		}
  		Video video = trackerPanel.getVideo();
  		if (!trackerPanel.changed && video!=null) {
    		String path = (String)video.getProperty("absolutePath"); //$NON-NLS-1$
    		if (path!=null) {
    			n++;
    			continue;
    		}
  		}
  		// notify user that tab must be saved in order be in tabset
      int selected = JOptionPane.showConfirmDialog(frame,
          TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message1") //$NON-NLS-1$
          	+" "+i+" (\""+frame.getTabTitle(i)+"\") "  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          	+TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message2")+XML.NEW_LINE  //$NON-NLS-1$
          	+TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Message3"),  //$NON-NLS-1$
          TrackerRes.getString("TrackerIO.Dialog.TabMustBeSaved.Title"),                    //$NON-NLS-1$
          JOptionPane.YES_NO_CANCEL_OPTION);
      if(selected==JOptionPane.CANCEL_OPTION) {
        return null;
      }
      else if(selected!=JOptionPane.YES_OPTION) {
        continue;
      }
    	getChooser().setAccessory(null);
      File newFile = VideoIO.save(null, trackerPanel, 
      		TrackerRes.getString("TrackerIO.Dialog.SaveTab.Title")); //$NON-NLS-1$
      if (newFile==null) {
      	return null;
      }
      Tracker.addRecent(XML.getAbsolutePath(newFile), false); // add at beginning
  		n++;
  	} 
  	// abort if no data files
  	if (n==0) {
	    JOptionPane.showMessageDialog(frame,
	        TrackerRes.getString("TrackerIO.Dialog.NoTabs.Message"),  //$NON-NLS-1$
	        TrackerRes.getString("TrackerIO.Dialog.NoTabs.Title"),  //$NON-NLS-1$ 
	        JOptionPane.WARNING_MESSAGE);
	    return null;
  	}
  	// if file is null, use chooser to get a file
    if(file==null) {
    	File[] files = getChooserFiles("save tabset"); //$NON-NLS-1$
    	if (files==null || !canWrite(files[0])) return null;
    	file = files[0];
    }
    frame.tabsetFile = file;
    XMLControl xmlControl = new XMLControlElement(frame);
    xmlControl.write(XML.getAbsolutePath(file));
    Tracker.addRecent(XML.getAbsolutePath(file), false); // add at beginning
		TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
    TMenuBar.getMenuBar(trackerPanel).refresh();
    return file;
  }

  /**
   * Displays a file chooser and returns the chosen files.
   *
   * @param type may be open, open video, save, insert image, export file, 
   * 				import file, save tabset
   * @return the files, or null if no files chosen
   */
  public static File[] getChooserFiles(String type) {
    JFileChooser chooser = getChooser();
    int result = JFileChooser.CANCEL_OPTION;
    // open tracker or video file
  	if (type.toLowerCase().equals("open")) { //$NON-NLS-1$
	    chooser.setMultiSelectionEnabled(false);
	    chooser.setAccessory(videoEnginePanel);
	    videoEnginePanel.reset();
	    chooser.setAcceptAllFileFilterUsed(true);
    	chooser.addChoosableFileFilter(videoAndTrkFileFilter);
      chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title"));        //$NON-NLS-1$
	    result = chooser.showOpenDialog(null);
    	File file = chooser.getSelectedFile();
	    chooser.removeChoosableFileFilter(videoAndTrkFileFilter); 
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
	    if(result==JFileChooser.APPROVE_OPTION) {
	      return new File[] {file};
	    }
	    return null;
  	}  
    // open any file
  	if (type.toLowerCase().equals("open any")) { //$NON-NLS-1$
	    chooser.setMultiSelectionEnabled(false);
      chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title"));        //$NON-NLS-1$
	    result = chooser.showOpenDialog(null);
    	File file = chooser.getSelectedFile();
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
	    if(result==JFileChooser.APPROVE_OPTION) {
	      return new File[] {file};
	    }
	    return null;
  	}  
    if(type.toLowerCase().equals("open video")) { // open video //$NON-NLS-1$
      chooser.setMultiSelectionEnabled(false);
	    chooser.setAccessory(videoEnginePanel);
	    videoEnginePanel.reset();
      chooser.setAcceptAllFileFilterUsed(true);
      chooser.addChoosableFileFilter(videoFileFilter);
      chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Open.Title"));        //$NON-NLS-1$
      result = chooser.showOpenDialog(null);
    	File file = chooser.getSelectedFile();
      chooser.removeChoosableFileFilter(videoFileFilter);
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
	    if(result==JFileChooser.APPROVE_OPTION) {
	      return new File[] {file};
	    }
	    return null;
    } 
    if (type.toLowerCase().equals("save")) { // save a file //$NON-NLS-1$
	    chooser.setAccessory(null);
    	// note this sets no file filters nor title
      chooser.setMultiSelectionEnabled(false);
      result = chooser.showSaveDialog(null);
    	File file = chooser.getSelectedFile();
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
	    if(result==JFileChooser.APPROVE_OPTION && canWrite(file)) {
	      return new File[] {file};
	    }
	    return null;
    } 
  	// import elements from a tracker file
  	if (type.toLowerCase().equals("import file")) { //$NON-NLS-1$
	    chooser.setAccessory(null);
	    chooser.setMultiSelectionEnabled(false);
	    chooser.setAcceptAllFileFilterUsed(true);
    	chooser.addChoosableFileFilter(trkFileFilter);
      chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.Import.Title")); //$NON-NLS-1$
	    result = chooser.showOpenDialog(null);
    	File file = chooser.getSelectedFile();
	    chooser.removeChoosableFileFilter(trkFileFilter); 
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
	    if(result==JFileChooser.APPROVE_OPTION) {
	      return new File[] {file};
	    }
	    return null;
  	} 
  	// saves a tabset file
  	if (type.toLowerCase().equals("save tabset")) { //$NON-NLS-1$
	    chooser.setAccessory(null);
	    chooser.setAcceptAllFileFilterUsed(false);
      chooser.addChoosableFileFilter(trkFileFilter);
      chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.SaveTabset.Title"));        //$NON-NLS-1$
      String filename = "";                //$NON-NLS-1$
      File file = new File(filename+"."+defaultXMLExt);                                      //$NON-NLS-1$
      String parent = XML.getDirectoryPath(filename);
      if(!parent.equals("")) {                                                          //$NON-NLS-1$
        XML.createFolders(parent);
        chooser.setCurrentDirectory(new File(parent));
      }
      chooser.setSelectedFile(file);
      result = chooser.showSaveDialog(null);
    	file = chooser.getSelectedFile();
	    chooser.removeChoosableFileFilter(trkFileFilter); 
      chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
      if(result==JFileChooser.APPROVE_OPTION) {
        if(!defaultXMLExt.equals(getExtension(file))) {
          filename = XML.stripExtension(file.getPath());
          file = new File(filename+"."+defaultXMLExt); //$NON-NLS-1$
        }
        return new File[] {file};
      }   
      return null;
  	}  	
  	return VideoIO.getChooserFiles(type);
  }
  
  /**
   * Determines if a file can be written. If the file exists, the user is prompted
   * for approval to overwrite.
   *
   * @param file the file to check
   * @return true if the file can be written 
   */
  public static boolean canWrite(File file) {
    if (file.exists() && !file.canWrite()) {
  		JOptionPane.showMessageDialog(null, 
  				ControlsRes.getString("Dialog.ReadOnly.Message"),  //$NON-NLS-1$
  				ControlsRes.getString("Dialog.ReadOnly.Title"),  //$NON-NLS-1$
  				JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    if (file.exists()) {
      int selected = JOptionPane.showConfirmDialog(null,
        "\"" + file.getName() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
        + TrackerRes.getString("TrackerIO.Dialog.ReplaceFile.Message"), //$NON-NLS-1$
        TrackerRes.getString("TrackerIO.Dialog.ReplaceFile.Title"),  //$NON-NLS-1$
        JOptionPane.YES_NO_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a video from a specified path. May return null.
   * Overrides VideoIO method.
   *
   * @param path the path
   * @param vidType a requested video type (may be null)
   * @return the video
   */
  public static Video getVideo(String path, VideoType vidType) {
  	boolean logConsole = OSPLog.isConsoleMessagesLogged();
  	if (!Tracker.warnXuggleError)
  		OSPLog.setConsoleMessagesLogged(false); 
  	if (path.startsWith("file:")) //$NON-NLS-1$
  		path = ResourceLoader.getNonURIPath(path);
    Video video = VideoIO.getVideo(path, vidType);
		OSPLog.setConsoleMessagesLogged(logConsole);
    return video;
  }

  /**
   * Loads data or a video from a specified path into a TrackerPanel.
   *
   * @param path the absolute path of a file or url
   * @param existingPanel a TrackerPanel to load (may be null)
   * @param frame the frame for the TrackerPanel
   * @param vidType a preferred VideoType (may be null)
   * @param desktopFiles a list of HTML and/or PDF files to open on the desktop (may be null)
   */
  private static void open(String path, TrackerPanel existingPanel, TFrame frame, VideoType vidType, ArrayList<String> desktopFiles) {
  	OSPLog.finer("opening "+path); //$NON-NLS-1$
  	String rawPath = path;
  	path = ResourceLoader.getURIPath(path);

  	isffmpegError  = false;
  	theFrame = frame;
		VideoIO.setCanceled(false);
  	// prevent circular references when loading tabsets
		String nonURIPath = ResourceLoader.getNonURIPath(path);
		if (rawPath.startsWith("//") && nonURIPath.startsWith("/") && !nonURIPath.startsWith("//")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			nonURIPath = "/"+nonURIPath; //$NON-NLS-1$
  	if (frame.loadedFiles.contains(nonURIPath)) {
    	OSPLog.finest("path already loaded "+nonURIPath); //$NON-NLS-1$
  		return;
  	}
  	frame.loadedFiles.add(nonURIPath);
  	if (!path.startsWith("http:")) //$NON-NLS-1$
			path = nonURIPath;

  	// create progress monitor  	
  	String fileName = XML.getName(path);
  	File testFile = new File(fileName);
    TrackerPanel trackerPanel = existingPanel==null? new TrackerPanel(): existingPanel;
    boolean panelChanged = trackerPanel.changed;
  	// create progress monitor
  	MonitorDialog monitorDialog = new MonitorDialog(frame, path);
  	monitorDialog.setVisible(true);
  	monitors.add(monitorDialog);
    
  	String xmlPath = null;
    if(videoFileFilter.accept(testFile)) { 
    	OSPLog.finest("opening video path "+path); //$NON-NLS-1$
			// download web videos to the OSP cache
			if (path.startsWith("http:")) { //$NON-NLS-1$
  			String name = XML.getName(path);
  			name = ResourceLoader.getNonURIPath(name);
  			File localFile = ResourceLoader.downloadToOSPCache(path, name, false);
				if (localFile!=null) {
					path = localFile.toURI().toString();
				}
			}

    	// attempt to load video
			VideoType requestedType = vidType;
    	Video video = getVideo(path, vidType);
    	monitorDialog.stop();
      if (video==null && !VideoIO.isCanceled()) {
      	// video failed to load
        // determine if other engines are available for the video extension
        ArrayList<VideoType> otherEngines = new ArrayList<VideoType>();
        String engine = VideoIO.getEngine();
        if (requestedType==null) {
	        String ext = XML.getExtension(path);        
	        if (!engine.equals(VideoIO.ENGINE_XUGGLE)) {
	        	VideoType xuggleType = VideoIO.getVideoType("Xuggle", ext); //$NON-NLS-1$
	        	if (xuggleType!=null) otherEngines.add(xuggleType);
	        }
	        if (!engine.equals(VideoIO.ENGINE_QUICKTIME)) {
	        	VideoType qtType = VideoIO.getVideoType("QT", ext); //$NON-NLS-1$
	        	if (qtType!=null) otherEngines.add(qtType);
	        }
        }
        if (otherEngines.isEmpty()) {
          monitorDialog.close();
	        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
	        		MediaRes.getString("VideoIO.Dialog.BadVideo.Message")+"\n\n"+path, //$NON-NLS-1$ //$NON-NLS-2$
	        		MediaRes.getString("VideoClip.Dialog.BadVideo.Title"),                                          //$NON-NLS-1$
	            JOptionPane.WARNING_MESSAGE); 
        }
        else {
      		// provide immediate way to open with other engines
        	engine = VideoIO.ENGINE_NONE.equals(engine)? MediaRes.getString("VideoIO.Engine.None"): //$NON-NLS-1$
        			VideoIO.ENGINE_XUGGLE.equals(engine)? MediaRes.getString("XuggleVideoType.Description"): //$NON-NLS-1$
        			MediaRes.getString("QTVideoType.Description"); //$NON-NLS-1$
        	String message = MediaRes.getString("VideoIO.Dialog.TryDifferentEngine.Message1")+" ("+engine+")."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	message += "\n"+MediaRes.getString("VideoIO.Dialog.TryDifferentEngine.Message2"); //$NON-NLS-1$ //$NON-NLS-2$
        	message += "\n"+MediaRes.getString("VideoIO.Dialog.Label.Path")+": "+path; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	ArrayList<String> optionList = new ArrayList<String>();
        	for (VideoType next: otherEngines) {
        		if (next.getClass().getSimpleName().equals("XuggleVideoType")) { //$NON-NLS-1$
        			optionList.add(MediaRes.getString("XuggleVideoType.Description")); //$NON-NLS-1$
        		}
        		else if (next.getClass().getSimpleName().equals("QTVideoType")) { //$NON-NLS-1$
        			optionList.add(MediaRes.getString("QTVideoType.Description")); //$NON-NLS-1$
        		}
        	}
        	optionList.add(MediaRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    			Object[] options = optionList.toArray(new String[optionList.size()]);
    			int response = JOptionPane.showOptionDialog(frame, message,
    					MediaRes.getString("VideoClip.Dialog.BadVideo.Title"), //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    			if (response>=0 && response<options.length-1) {
    				VideoType desiredType = otherEngines.get(response);
    				monitorDialog.restart();
    	    	video = getVideo(path, desiredType);
    	    	if (video==null && !VideoIO.isCanceled()) {
    	    		// failed again
    	    		monitorDialog.close();
    	    		JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
    	        		MediaRes.getString("VideoIO.Dialog.BadVideo.Message")+"\n\n"+path, //$NON-NLS-1$ //$NON-NLS-2$
    	        		MediaRes.getString("VideoClip.Dialog.BadVideo.Title"), //$NON-NLS-1$
    	            JOptionPane.WARNING_MESSAGE); 
    	    	}
    			}
        }
      }
			if (video==null) {
				monitorDialog.close();
				return;
			}
      if (!VideoIO.isCanceled()) {
      	if (monitorDialog.isVisible()) 
      		monitorDialog.setProgress(85);
      	vidType = (VideoType)video.getProperty("video_type"); //$NON-NLS-1$
      	OSPLog.finer(video.getProperty("path")+" opened as "+ //$NON-NLS-1$ //$NON-NLS-2$
      			vidType.getClass().getSimpleName()+" "+vidType.getDescription()); //$NON-NLS-1$
      	trackerPanel.frame = frame;
        if (VideoIO.isCanceled()) return;
        
        frame.addTab(trackerPanel);
      	if (monitorDialog.isVisible()) 
      		monitorDialog.setProgress(95);
        JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
        pane.setDividerLocation(frame.defaultRightDivider);
        TMenuBar.getMenuBar(trackerPanel).refresh();
        trackerPanel.setVideo(video);
        // panel is changed if video imported into existing trackerPanel
        panelChanged = existingPanel!=null;
        if (video.getFrameCount() == 1) {
          trackerPanel.getPlayer().getVideoClip().setStepCount(10);
        }
        // if new trackerPanel, move coords origin to center of video
        if (existingPanel==null) {
	        ImageCoordSystem coords  = trackerPanel.getCoords();
	        coords.setAllOriginsXY(video.getWidth()/2, video.getHeight()/2);
        }
        trackerPanel.repaint();
        frame.setSelectedTab(trackerPanel);
        monitorDialog.close();
        // check for video frames with durations that vary by 20% from average
        if (Tracker.warnVariableDuration)
        	findBadVideoFrames(trackerPanel, defaultBadFrameTolerance, 
        			true, true, true); // show dialog only if bad frames found, and include "don't show again" button
      } 
    } 
    else { // load data from zip, trz or trk file
			Map<String, String> pageViewTabs = new HashMap<String, String>(); // pageView tabs that display html files

    	if (zipFileFilter.accept(testFile) || trzFileFilter.accept(testFile)) {
    		monitorDialog.stop();
  			String name = XML.getName(ResourceLoader.getNonURIPath(path));
  			// download web files to OSP cache
  			boolean isWebPath = path.startsWith("http:/"); //$NON-NLS-1$
  			if (isWebPath) {
					File localFile = ResourceLoader.downloadToOSPCache(path, name, false);
					if (localFile!=null) {
						// set path to downloaded file
						path = localFile.toURI().toString();
			    	OSPLog.finest("downloaded zip file: "+path); //$NON-NLS-1$
					} 				
  			}
	  			
				ArrayList<String> trkFiles = new ArrayList<String>(); // all trk files found in zip
				final ArrayList<String> htmlFiles = new ArrayList<String>(); // supplemental html files found in zip
				final ArrayList<String> pdfFiles = new ArrayList<String>(); // all pdf files found in zip
				String trkForTFrame = null;
				
				// sort the zip file contents
	  		Set<String> contents = ResourceLoader.getZipContents(path);
				for (String next: contents) {
					if (next.endsWith(".trk")) { //$NON-NLS-1$
						String s = ResourceLoader.getURIPath(path+"!/"+next); //$NON-NLS-1$
			    	OSPLog.finest("found trk file "+s); //$NON-NLS-1$
						trkFiles.add(s);
					}
					else if (next.endsWith(".pdf")) { //$NON-NLS-1$
						pdfFiles.add(next);
					}
					else if (next.endsWith(".html") || next.endsWith(".htm")) { //$NON-NLS-1$ //$NON-NLS-2$
						// exclude HTML info files (name "<zipname>_info")
						String baseName = XML.stripExtension(name);
						String nextName = XML.getName(next);
						if (XML.stripExtension(nextName).equals(baseName+"_info"))  //$NON-NLS-1$
							continue;
						
						htmlFiles.add(next);
					}
				}
				if (trkFiles.isEmpty() && pdfFiles.isEmpty() && htmlFiles.isEmpty()) {
					String s = TrackerRes.getString("TFrame.Dialog.LibraryError.Message"); //$NON-NLS-1$
      		JOptionPane.showMessageDialog(frame, 
      				s+" \""+name+"\".", //$NON-NLS-1$ //$NON-NLS-2$
      				TrackerRes.getString("TFrame.Dialog.LibraryError.Title"), //$NON-NLS-1$
      				JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// find page view filenames in TrackerPanel xmlControls
				// also look for trk for TFrame
				if (!trkFiles.isEmpty()) {
					ArrayList<String> trkNames = new ArrayList<String>();
					for (String next: trkFiles) {
						trkNames.add(XML.stripExtension(XML.getName(next)));
						XMLControl control = new XMLControlElement(next);
						if (control.getObjectClassName().endsWith("TrackerPanel")) { //$NON-NLS-1$
							findPageViewFiles(control, pageViewTabs);
						}
						else if (trkForTFrame==null
								&& control.getObjectClassName().endsWith("TFrame")) { //$NON-NLS-1$
							trkForTFrame = next;
						}
					}
					if (!htmlFiles.isEmpty()) {
						// remove page view HTML files
						String[] paths = htmlFiles.toArray(new String[htmlFiles.size()]);
						for (String htmlPath: paths) {
							boolean isPageView = false;
							for (String page: pageViewTabs.keySet()) {
								isPageView = isPageView || htmlPath.endsWith(page);
							}
							if (isPageView) {
								htmlFiles.remove(htmlPath);
							}
							// discard HTML <trkname>_info files
							for (String trkName: trkNames) {
								if (htmlPath.contains(trkName+"_info.")) { //$NON-NLS-1$
									htmlFiles.remove(htmlPath);
								}								
							}
						}
					}
					if (trkForTFrame!=null) {
						trkFiles.clear();
						trkFiles.add(trkForTFrame);
					}
				}
				
				// unzip pdf/html files into temp directory and open on desktop
				final ArrayList<String> tempFiles = new ArrayList<String>();		
				if (!htmlFiles.isEmpty() || !pdfFiles.isEmpty()) {
					File temp = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$			
					Set<File> files = ResourceLoader.unzip(path, temp, true);
					for (File next : files) {
						next.deleteOnExit();
		        // add PDF and HTML files to tempFiles
						String relPath = XML.getPathRelativeTo(next.getPath(), temp.getPath());
						if (pdfFiles.contains(relPath) || htmlFiles.contains(relPath)) {
							String tempPath = ResourceLoader.getURIPath(next.getAbsolutePath());
							tempFiles.add(tempPath);
						}
					}
		  		Runnable runner = new Runnable() {
						public void run() {
			        for (String path: tempFiles) {
			        	OSPDesktop.displayURL(path);
			        }
						}
					};
					new Thread(runner).start();
				}
				// load trk files into Tracker
	  		if (!VideoIO.isCanceled()) {
	        monitorDialog.close();
	      	open(trkFiles, frame, tempFiles);
	        Tracker.addRecent(nonURIPath, false); // add at beginning
	      	return;
	  		}
        monitorDialog.close();
	  		return;
    	}
    	// load data from trk file
      XMLControlElement control = new XMLControlElement();
      xmlPath = control.read(path);
      if (VideoIO.isCanceled()) return;
      monitorDialog.stop();
    	if (monitorDialog.isVisible()) 
    		monitorDialog.setProgress(20);
      Class<?> type = control.getObjectClass();
      if(TrackerPanel.class.isAssignableFrom(type)) {
        XMLControl child = control.getChildControl("videoclip"); //$NON-NLS-1$
        if (child != null) {
        	int count = child.getInt("video_framecount"); //$NON-NLS-1$
          child = child.getChildControl("video"); //$NON-NLS-1$
          if (child != null) {
          	String vidPath = child.getString("path"); //$NON-NLS-1$
          	monitorDialog.setName(vidPath);
          	monitorDialog.setFrameCount(count);
          }
        }


        // TODO the line below needs to finish (in SwingWorker?) before continuing?
        control.loadObject(trackerPanel);
        
        
        trackerPanel.frame = frame;
        trackerPanel.defaultFileName = XML.getName(path);
        trackerPanel.openedFromPath = path;

        // find page view files and add to TrackerPanel.pageViewFilePaths
				findPageViewFiles(control, trackerPanel.pageViewFilePaths);
        
        if (desktopFiles!=null) {
        	for (String s: desktopFiles) {
        		trackerPanel.supplementalFilePaths.add(s);
        	}
        }
        trackerPanel.setDataFile(new File(ResourceLoader.getNonURIPath(path)));
      	if (monitorDialog.isVisible()) 
      		monitorDialog.setProgress(80);
        if (VideoIO.isCanceled()) return;
        frame.addTab(trackerPanel);
      	if (monitorDialog.isVisible()) 
      		monitorDialog.setProgress(90);
        frame.setSelectedTab(trackerPanel);
        frame.showTrackControl(trackerPanel);
        frame.showNotes(trackerPanel);
        frame.refresh();
      } 
      else if(TFrame.class.isAssignableFrom(type)) {
        monitorDialog.close();
      	control.loadObject(frame);
      	rawPath = XML.forwardSlash(rawPath); 
        Tracker.addRecent(ResourceLoader.getNonURIPath(rawPath), false); // add at beginning
        trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
        if (trackerPanel!=null)
        	TMenuBar.getMenuBar(trackerPanel).refresh();
      	return;
      } 
      else if(!control.failedToRead()) {
        monitorDialog.close();
        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
        		"\""+XML.getName(path)+"\" "+   //$NON-NLS-1$ //$NON-NLS-2$
        		MediaRes.getString("VideoIO.Dialog.XMLMismatch.Message"), //$NON-NLS-1$
            MediaRes.getString("VideoIO.Dialog.XMLMismatch.Title"),   //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
        return;
      } 
      else {
        monitorDialog.close();
        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
        		MediaRes.getString("VideoIO.Dialog.BadFile.Message")+  //$NON-NLS-1$
        		ResourceLoader.getNonURIPath(path));
        return;
      }
    }
    
    monitorDialog.close();
  	rawPath = XML.forwardSlash(rawPath);
  	if (xmlPath!=null && 
  			(xmlPath.contains(".zip!") ||   //$NON-NLS-1$
  			xmlPath.contains(".trz!") ||   //$NON-NLS-1$
  			xmlPath.contains(".jar!"))) { //$NON-NLS-1$
  		rawPath = XML.forwardSlash(xmlPath);
  	}
    Tracker.addRecent(ResourceLoader.getNonURIPath(rawPath), false); // add at beginning
    TMenuBar.getMenuBar(trackerPanel).refresh();
    TTrackBar.refreshMemoryButton();
    trackerPanel.changed = panelChanged;
  }

  /**
   * Loads data or a video from a specified file into a new TrackerPanel.
   * If file is null, a file chooser is displayed.
   *
   * @param file the file to be loaded (may be null)
   * @param frame the frame for the TrackerPanel
   */
  public static void open(File file, final TFrame frame) {
  	VideoType selectedType = null;
  	if(file==null) {
      File[] files = getChooserFiles("open"); //$NON-NLS-1$
      if(files!=null) {
        file = files[0];
        selectedType = videoEnginePanel.getSelectedVideoType();
      }
    }
    if(file==null) {
    	OSPLog.finer("no file to open"); //$NON-NLS-1$
      return;
    }
    
  	frame.loadedFiles.clear();
    // open in separate background thread if flagged
    final String path = XML.getAbsolutePath(file);
    final VideoType vidType = selectedType;
    Runnable runner = new Runnable() {
    	public void run() {
      	OSPLog.finest("opening File"); //$NON-NLS-1$
        open(path, null, frame, vidType, null);
      }
    };
    if (loadInSeparateThread) {
      Thread opener = new Thread(runner);
      opener.setPriority(Thread.NORM_PRIORITY);
      opener.setDaemon(true);
      opener.start(); 
    }
    else runner.run();
  }

  /**
   * Loads data or a video from a specified url into a new TrackerPanel.
   *
   * @param url the url to be loaded
   * @param frame the frame for the TrackerPanel
   */
  public static void open(URL url, final TFrame frame) {
    if(url==null) {
      return;
    }
    final String path = url.toExternalForm();
  	OSPLog.finest("opening URL"); //$NON-NLS-1$
  	open(path, frame);
  }

  /**
   * Loads a set of trk or video files into new TrackerPanels.
   *
   * @param urlPaths an array of URL paths to be loaded
   * @param frame the frame for the TrackerPanels
   * @param desktopFiles supplemental HTML and PDF files to load on the desktop
   */
  public static void open(final Collection<String> urlPaths, final TFrame frame, final ArrayList<String> desktopFiles) {
    if(urlPaths==null || urlPaths.isEmpty()) {
      return;
    }
  	frame.loadedFiles.clear();
    // open in separate background thread if flagged
    Runnable runner = new Runnable() {
    	public void run() {
    		for (String path: urlPaths) {
			  	OSPLog.finest("opening URL "+path); //$NON-NLS-1$
	        open(path, null, frame, null, desktopFiles);
    		}
      }
    };
    if (loadInSeparateThread) {
      Thread opener = new Thread(runner);
      opener.setPriority(Thread.NORM_PRIORITY);
      opener.setDaemon(true);
      opener.start(); 
    }
    else runner.run();
  }

  /**
   * Loads data or a video from a specified path into a new TrackerPanel.
   *
   * @param path the path
   * @param frame the frame for the TrackerPanel
   */
  public static void open(final String path, final TFrame frame) {
  	frame.loadedFiles.clear();
    // open in separate background thread if flagged
    Runnable runner = new Runnable() {
    	public void run() {
        open(path, null, frame, null, null);
      }
    };
    if (loadInSeparateThread) {
      Thread opener = new Thread(runner);
      opener.setPriority(Thread.NORM_PRIORITY);
      opener.setDaemon(true);
      opener.start(); 
    }
    else runner.run();
  }

  /**
   * Imports xml data into a tracker panel from a file selected with a chooser. 
   * The user selects the elements to import with a ListChooser.
   *
   * @param trackerPanel the tracker panel
   * @return the file
   */
  public static File importFile(TrackerPanel trackerPanel) {
    File[] files = getChooserFiles("import file"); //$NON-NLS-1$
    if (files == null) {
      return null;
    }
    File file = files[0];
  	OSPLog.fine("importing from "+file); //$NON-NLS-1$
    XMLControlElement control = new XMLControlElement(file.getAbsolutePath());
    Class<?> type = control.getObjectClass();
    if (TrackerPanel.class.equals(type)) {
      // create the list chooser
      ListChooser dialog = new ListChooser(
          TrackerRes.getString("TrackerIO.Dialog.Import.Title"), //$NON-NLS-1$
          TrackerRes.getString("TrackerIO.Dialog.Import.Message"), //$NON-NLS-1$
          trackerPanel);
      // choose the elements and load the tracker panel
      if (choose(control, dialog)) {
        trackerPanel.changed = true;
        control.loadObject(trackerPanel);
      }
    }
		else {
      JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
          TrackerRes.getString("TrackerPanel.Dialog.LoadFailed.Message") //$NON-NLS-1$
  				+ " "+ XML.getName(XML.getAbsolutePath(file)), //$NON-NLS-1$
      		TrackerRes.getString("TrackerPanel.Dialog.LoadFailed.Title"), //$NON-NLS-1$
      		JOptionPane.WARNING_MESSAGE);
      return null;
		}
    TTrackBar.refreshMemoryButton();
    return file;
  }

  /**
   * Imports chooser-selected video to the specified tracker panel.
   *
   * @param trackerPanel the tracker panel
   */
  public static void importVideo(final TrackerPanel trackerPanel) {
    JFileChooser chooser = getChooser();
    chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.ImportVideo.Title")); //$NON-NLS-1$
    File[] files = getChooserFiles("open video"); //$NON-NLS-1$
    if (files==null || files.length==0) {
      return;
    }
    final VideoType vidType = videoEnginePanel.getSelectedVideoType();
    // open in separate background thread if flagged
    final File theFile = files[0];
    Runnable runner = new Runnable() {
    	public void run() {
      	TrackerIO.importVideo(theFile, trackerPanel, vidType);            
      	OSPLog.finest("completed importing file "+theFile); //$NON-NLS-1$
      }
    };
    if (loadInSeparateThread) {
      Thread opener = new Thread(runner);
      opener.setPriority(Thread.NORM_PRIORITY);
      opener.setDaemon(true);
      opener.start(); 
    }
    else runner.run();
  }

  /**
   * Imports a video file to the specified tracker panel.
   *
   * @param file the video file
   * @param trackerPanel the tracker panel
   * @param vidType the preferred video type
   */
  public static void importVideo(File file, TrackerPanel trackerPanel, VideoType vidType) {
  	String path = XML.getAbsolutePath(file);
  	OSPLog.finest("importing file: "+path);  	 //$NON-NLS-1$
  	TFrame frame = trackerPanel.getTFrame();
  	frame.loadedFiles.clear();
  	open(path, trackerPanel, frame, vidType, null);
  }
  
  /**
   * Checks for video frames with durations that vary from the mean.
   * @param trackerPanel the TrackerPanel to check
   * @param tolerance the unacceptable variation limit
   * @param showDialog true to display the results in a dialog
   * @param onlyIfFound true to display the dialog only if problems are found
   * @param showSetDefaultButton true to show the "Don't show again" button
   * @return an array of frames with odd durations
   */
  public static ArrayList<Integer> findBadVideoFrames(
  		TrackerPanel trackerPanel, double tolerance, boolean showDialog, 
  		boolean onlyIfFound, boolean showSetDefaultButton) {
		ArrayList<Integer> outliers = new ArrayList<Integer>(); 
  	Video video = trackerPanel.getVideo();
  	if (video==null)
  		return outliers;
		double dur = video.getDuration();
		boolean done = false;
		while (!done) {
			int i=0;
			double frameDur = dur/(video.getFrameCount()-outliers.size());
	  	for (; i<video.getFrameCount(); i++) {
		  	double err = Math.abs(frameDur-video.getFrameDuration(i))/frameDur;
	  		if (err>tolerance && !outliers.contains(i)) {
	  			dur -= video.getFrameDuration(i);
	  			outliers.add(i);
	  			break;
	  		}
	  	}
	  	done = (i==video.getFrameCount());
		}
//		double frameDur = dur/(video.getFrameCount()-outliers.size());
		if (outliers.contains(video.getFrameCount()-1)) {
			outliers.remove(new Integer(video.getFrameCount()-1));
		}
		if (showDialog) {
			String message = TrackerRes.getString("TrackerIO.Dialog.DurationIsConstant.Message"); //$NON-NLS-1$
			int messageType = JOptionPane.INFORMATION_MESSAGE; 
			if (outliers.isEmpty() && onlyIfFound) {
				return outliers;
			}
			if (!outliers.isEmpty()) {
				messageType = JOptionPane.WARNING_MESSAGE;
				// get last bad frame
				int last = outliers.get(outliers.size()-1);
				
				// find longest section of good frames
				int maxClear = -1;
				int start=0, end=0;
				int prevBadFrame = -1;
				for (Integer i: outliers) {
					int clear = i-prevBadFrame-2;
					if (clear>maxClear) {
						start=prevBadFrame+1;
						end = i-1;
						maxClear = clear;
						prevBadFrame = i;
					}
				}
				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
				if (clip.getEndFrameNumber()-last-1>maxClear) {
					start = last+1;
					end = clip.getEndFrameNumber();
				}
				// assemble message
		    NumberFormat format = NumberFormat.getInstance();
		    format.setMaximumFractionDigits(2);
		    format.setMinimumFractionDigits(2);
				message = TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message1"); //$NON-NLS-1$
				message += " "+(int)(tolerance*100)+"%."; //$NON-NLS-1$ //$NON-NLS-2$
				message += "\n"+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message2");  //$NON-NLS-1$//$NON-NLS-2$
				message += "\n"+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message3"); //$NON-NLS-1$ //$NON-NLS-2$
				message += "\n\n"+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Message4"); //$NON-NLS-1$ //$NON-NLS-2$
				for (Integer i: outliers) {
					message += " "+i; //$NON-NLS-1$
					if (i<last)
						message += ","; //$NON-NLS-1$
				}
				message += "\n\n"+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Recommended")+":  " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Start")+" "+start+",  " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+TrackerRes.getString("TrackerIO.Dialog.DurationVaries.End")+" "+end+"\n "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
			}
			String close = TrackerRes.getString("Dialog.Button.OK"); //$NON-NLS-1$
			String dontShow = TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox"); //$NON-NLS-1$
			String[] buttons = showSetDefaultButton? new String[] {dontShow, close}: new String[] {close};
			int response = JOptionPane.showOptionDialog(theFrame, message, 
					TrackerRes.getString("TrackerIO.Dialog.DurationVaries.Title"),  //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, messageType, null, 
					buttons, close);
			if (response>=0 && response<buttons.length && buttons[response].equals(dontShow)) {
				Tracker.warnVariableDuration = false;
			}
		}
		return outliers;  	
  }

  /**
   * Inserts chooser-selected images into an ImageVideo on a TrackerPanel.
   *
   * @param trackerPanel the TrackerPanel
   * @param startIndex the insertion index
   * @return an array of inserted files
   */
  public static File[] insertImagesIntoVideo(TrackerPanel trackerPanel, int startIndex) {
    JFileChooser chooser = getChooser();
    chooser.setDialogTitle(TrackerRes.getString("TrackerIO.Dialog.AddImage.Title")); //$NON-NLS-1$
    File[] files = getChooserFiles("insert image"); //$NON-NLS-1$
    return insertImagesIntoVideo(files, trackerPanel, startIndex);
  }

  /**
   * Inserts file-based images into an ImageVideo on a TrackerPanel.
   *
   * @param files array of image files
   * @param trackerPanel the TrackerPanel
   * @param startIndex the insertion index
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
  				ImageVideo imageVid = (ImageVideo)trackerPanel.getVideo();
  				imageVid.insert(file.getAbsolutePath(), startIndex, files.length==1);
  				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  				clip.setStepCount(imageVid.getFrameCount());
  				trackerPanel.getPlayer().setStepNumber(clip.frameToStep(startIndex++));
  			}
  			catch (IOException ex) {
  				ex.printStackTrace();
  			}
  		}
  		else {
  			String s = TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Message1"); //$NON-NLS-1$
  			if (i < files.length-1) {
  				s += XML.NEW_LINE + TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Message2"); //$NON-NLS-1$
          int result = JOptionPane.showConfirmDialog(trackerPanel, 
      				"\""+file+"\" " + s, //$NON-NLS-1$ //$NON-NLS-2$
      				TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Title"),  //$NON-NLS-1$
      				JOptionPane.WARNING_MESSAGE);
          if (result != JOptionPane.YES_OPTION) {
          	if (i == 0) return null;
          	File[] inserted = new File[i];
          	System.arraycopy(files, 0, inserted, 0, i);
            TTrackBar.refreshMemoryButton();
          	return inserted;
          }
  			}
  			else { // bad file is last one in array
  				JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
      				"\""+file+"\" " + s, //$NON-NLS-1$ //$NON-NLS-2$
      				TrackerRes.getString("TrackerIO.Dialog.NotAnImage.Title"),  //$NON-NLS-1$
      				JOptionPane.WARNING_MESSAGE);
        	if (i == 0) return null;
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
  public static File exportFile(TrackerPanel trackerPanel) {
    // create an XMLControl
    XMLControl control = new XMLControlElement(trackerPanel);
    // create a list chooser
    ListChooser dialog = new ListChooser(TrackerRes.getString("TrackerIO.Dialog.Export.Title"), //$NON-NLS-1$
        TrackerRes.getString("TrackerIO.Dialog.Export.Message"), //$NON-NLS-1$
        trackerPanel);
    // choose the elements
    if (choose(control, dialog)) {
      File[] files = getChooserFiles("export file"); //$NON-NLS-1$
      if (files == null) {
        return null;
      }
      File file = files[0];
      if (!defaultXMLExt.equals(getExtension(file))) {
      	String filename = XML.stripExtension(file.getPath());
        file = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
      }
      if (!canWrite(file)) return null;
      try {
        Writer writer = new FileWriter(file);
        control.write(writer);
        return file;
      }
      catch (IOException ex) {
      	ex.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Displays a ListChooser with choices from the specified control.
   * Modifies the control and returns true if the OK button is clicked.
   *
   * @param control the XMLControl
   * @param dialog the dialog
   * @return <code>true</code> if OK button is clicked
   */
  public static boolean choose(XMLControl control, ListChooser dialog) {
    // create the lists
    ArrayList<XMLControl> choices = new ArrayList<XMLControl>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<XMLControl> originals = new ArrayList<XMLControl>();
    ArrayList<XMLProperty> primitives = new ArrayList<XMLProperty>(); // non-object properties
    // add direct child controls except clipcontrol and toolbar
    XMLControl[] children = control.getChildControls();
    for (int i = 0; i < children.length; i++) {
      originals.add(children[i]);
      if (children[i].getPropertyName().equals("clipcontrol")) continue; //$NON-NLS-1$
      if (children[i].getPropertyName().equals("toolbar")) continue; //$NON-NLS-1$
      choices.add(children[i]);
      String name = children[i].getPropertyName();
      if (name.equals("coords")) { //$NON-NLS-1$
      	name = TrackerRes.getString("TMenuBar.MenuItem.Coords"); //$NON-NLS-1$
      }
      else if (name.equals("videoclip")) { //$NON-NLS-1$
      	name = TrackerRes.getString("TMenuBar.MenuItem.VideoClip"); //$NON-NLS-1$
      }
      names.add(name);
    }
    // add track controls and gather primitives
    Iterator<Object> it = control.getPropertyContent().iterator();
    while (it.hasNext()) {
      XMLProperty prop = (XMLProperty)it.next();
      if ("tracks".indexOf(prop.getPropertyName()) != -1) { //$NON-NLS-1$
        children = prop.getChildControls();
        for (int i = 0; i < children.length; i++) {
          if (children[i].getObjectClass()==TapeMeasure.class) continue;
          if (children[i].getObjectClass()==CoordAxes.class) continue;
          choices.add(children[i]);
          names.add(children[i].getPropertyName());
          originals.add(children[i]);
        }
      }
      else if (!prop.getPropertyType().equals("object")) { //$NON-NLS-1$
        primitives.add(prop);
      }
    }
    // show the dialog for user input and make changes if approved
    if (dialog.choose(choices, names)) {
      // remove primitives from control
    	for (XMLProperty prop: primitives) {
        control.setValue(prop.getPropertyName(), null);
    	}
      control.getPropertyContent().removeAll(primitives);
      // compare choices with originals and remove unwanted object content
      for (XMLControl next: originals) {
        if (!choices.contains(next)) {
          XMLProperty prop = next.getParentProperty();
          XMLProperty parent = prop.getParentProperty();
          if (parent == control) {
            control.setValue(prop.getPropertyName(), null);
          }
          parent.getPropertyContent().remove(prop);
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Copies an xml string representation of the specified object to the
   * system clipboard.
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
   * Pastes a new object into the specified tracker panel from an xml string
   * on the system clipboard.
   *
   * @param trackerPanel the tracker panel
   */
  public static void pasteXML(TrackerPanel trackerPanel) {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable data = clipboard.getContents(null);
      XMLControl control = new XMLControlElement();
      control.readXML((String)data.getTransferData(DataFlavor.stringFlavor));
      Class<?> type = control.getObjectClass();
      if (TTrack.class.isAssignableFrom(type)) {
      	TTrack track = (TTrack)control.loadObject(null);
      	if (track != null) {
          trackerPanel.addTrack(track);
          trackerPanel.setSelectedTrack(track);
      	}
      }
      else if (ImageCoordSystem.class.isAssignableFrom(type)) {
        XMLControl state = new XMLControlElement(trackerPanel.getCoords());
      	control.loadObject(trackerPanel.getCoords());
        Undo.postCoordsEdit(trackerPanel, state);
      }
      else if (VideoClip.class.isAssignableFrom(type)) {
      	VideoClip clip = (VideoClip)control.loadObject(null);
      	if (clip != null) {
      		VideoClip prev = trackerPanel.getPlayer().getVideoClip();
      		XMLControl state = new XMLControlElement(prev);
          trackerPanel.getPlayer().setVideoClip(clip);
      		Undo.postVideoReplace(trackerPanel, state);
      	}
      }
      else if (TrackerPanel.class.isAssignableFrom(type)) {
        control.loadObject(trackerPanel);
      }
    }
    catch (Exception ex) {
    }
  }

  /**
   * Copies data in the specified datatable to the system clipboard.
   *
   * @param table the datatable to copy
   * @param asFormatted true to retain table formatting
   * @param header the table header
   */
  public static void copyTable(DataTable table, boolean asFormatted, String header) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringBuffer buf = getData(table, asFormatted);
    // replace spaces with underscores in header (must be single string)
    header = header.replace(' ', '_');
    if (!header.endsWith(XML.NEW_LINE))
    	header += XML.NEW_LINE;
    StringSelection stringSelection = new StringSelection(header+buf.toString());
    clipboard.setContents(stringSelection, stringSelection);
  }

  /**
   * Copies the specified image to the system clipboard.
   *
   * @param image the image to copy
   */
  public static void copyImage(Image image) {
  	TransferImage transfer = new TransferImage(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transfer, null);
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
        Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
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
   * @param table the datatable containing the data
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
      if (table.isRowNumberVisible() && selectedColumns[j] == 0) continue;
      buf.append(table.getColumnName(selectedColumns[j]));
      if (j < selectedColumns.length - 1)
        buf.append(delimiter); // add delimiter after each column except the last
    }
    buf.append(XML.NEW_LINE);
    java.text.DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
    nf.applyPattern("0.000000000E0"); //$NON-NLS-1$
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
        }
        else {
          value = table.getValueAt(selectedRows[i], selectedColumns[j]);
          if (value != null) {
            if (value instanceof Number) {
            	value = nf.format(value);
            }
            else if (value instanceof java.util.Date) {
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
    if (restoreRows!=null) {
      // restore previous selection state
      table.clearSelection();
      for (int row: restoreRows)
      	table.addRowSelectionInterval(row, row);
      for (int col: restoreColumns)
      	table.addColumnSelectionInterval(col, col);   	
    }
    return buf;
  }

  /**
   * Sets the delimiter for copied or exported data
   *
   * @param delimiter the delimiter
   */
  public static void setDelimiter(String delimiter) {
  	if (delimiter!=null)
  		TrackerIO.delimiter = delimiter;
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
    	// by default, use delimiter itself for key (used for display purposes--could be description)
    	TrackerIO.customDelimiters.put(custom, custom);
    }
  }

  /**
   * Removes a custom delimiter from the collection of delimiters
   *
   * @param custom the delimiter to remove
   */
  public static void removeCustomDelimiter(String custom) {
  	if (TrackerIO.getDelimiter().equals(custom))
  		setDelimiter(TrackerIO.defaultDelimiter);
  	String selected = null;
  	for (String key: customDelimiters.keySet()) {
  		if (customDelimiters.get(key).equals(custom))
  			selected = key;
  	}
  	if (selected!=null)
  		customDelimiters.remove(selected);
  }
  
  /**
   * Finds page view file paths in an XMLControl and maps the page view path to the URL path
   * of the file. If the page view path refers to a file inside a trk, zip or jar file, then 
   * all files in the jar are extracted and the URL path points to the extracted HTML file.
   * This ensures that the HTML page can be opened on the desktop.
   */
  private static void findPageViewFiles(XMLControl control, Map<String, String> pageViewFiles) {
		// extract page view filenames from control xml
		String xml = control.toXML();
		// basic unit is a tab with title and text
		String token = "PageTView$TabView"; //$NON-NLS-1$
		int j = xml.indexOf(token);
		while (j>-1) { // found page view tab
			xml = xml.substring(j+token.length());
			// get text and check if it is a loadable path
			token = "<property name=\"text\" type=\"string\">"; //$NON-NLS-1$
			j = xml.indexOf(token);
			String path = xml.substring(j+token.length());
			j = path.indexOf("</property>"); //$NON-NLS-1$
			path = path.substring(0, j);
			if (path.endsWith(".html") || path.endsWith(".htm")) { //$NON-NLS-1$ //$NON-NLS-2$
				Resource res = ResourceLoader.getResource(path);
				if (res!=null) {
					// found an HTML file, so add it to the map
					String urlPath = res.getURL().toExternalForm();
					String zipPath = ResourceLoader.getNonURIPath(res.getAbsolutePath());
					int n = zipPath.indexOf("!/"); //$NON-NLS-1$
					// extract files from jar, zip or trz files into temp directory
					if (n>0) {
						File target = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
						zipPath = zipPath.substring(0, n);
						ResourceLoader.unzip(zipPath, target, true); // overwrite
						target = new File(target, path);
						if (target!=null && target.exists()) {
							res = ResourceLoader.getResource(target.getAbsolutePath());
							urlPath = res.getURL().toExternalForm();
						}
						else {
							path = null;
						}
					}
					if (path!=null) {
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
   * ComponentImage class for printing and copying images of components.
   * This is adapted from code in SnapshotTool and DrawingPanel
   */
  static class ComponentImage implements Printable {
    private BufferedImage image;
    Component c;

    ComponentImage(Component comp) {
      c = comp;
      if (comp instanceof JFrame) 
      	comp = ((JFrame)comp).getContentPane();
      else if (comp instanceof JDialog) 
      	comp = ((JDialog)comp).getContentPane();
      int w = (comp.isVisible()) ? comp.getWidth() : comp.getPreferredSize().width;
      int h = (comp.isVisible()) ? comp.getHeight() : comp.getPreferredSize().height;
      image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      if (comp instanceof Renderable) 
      	image=((Renderable)comp).render(image);
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
        } catch(PrinterException pe) {
          JOptionPane.showMessageDialog(c,
               TrackerRes.getString("TActions.Dialog.PrintError.Message"), //$NON-NLS-1$
               TrackerRes.getString("TActions.Dialog.PrintError.Title"), //$NON-NLS-1$
               JOptionPane.ERROR_MESSAGE);
        }
      }
    	
    }

    /**
     * Implements Printable.
     * @param g the printer graphics 
     * @param pageFormat the format
     * @param pageIndex the page number
     * @return status code
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
       if(pageIndex>=1) { // only one page available
          return Printable.NO_SUCH_PAGE;
       }
       if(g==null) {
          return Printable.NO_SUCH_PAGE;
       }
       Graphics2D g2 = (Graphics2D) g;
       double scalex = pageFormat.getImageableWidth()/image.getWidth();
       double scaley = pageFormat.getImageableHeight()/image.getHeight();
       double scale = Math.min(scalex, scaley);
       scale = Math.min(scale, 1.0); // don't magnify images--only reduce if nec
       g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
       g2.scale(scale, scale);
       g2.drawImage(image, 0, 0, null);
       return Printable.PAGE_EXISTS;
    }

  }
}

class MonitorDialog extends JDialog {
	
	JProgressBar monitor;
  Timer timer;
  int frameCount = Integer.MIN_VALUE;

	MonitorDialog(TFrame frame, String path) {
		super(frame, false);
		setName(path);
  	JPanel contentPane = new JPanel(new BorderLayout());
  	setContentPane(contentPane);
  	monitor = new JProgressBar(0, 100);
  	monitor.setValue(0);
  	monitor.setStringPainted(true);
  	// make timer to step progress forward slowly
    timer = new Timer(300, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (!isVisible()) return;
      	int progress = monitor.getValue()+1;
      	if (progress<=20)
      		monitor.setValue(progress);
      }
    });
		timer.setRepeats(true);
		this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
      	VideoIO.setCanceled(true);
      }
		});
//  	// give user a way to close unwanted dialog: double-click
//  	addMouseListener(new MouseAdapter() {
//  		public void mouseClicked(MouseEvent e) {
//  			if (e.getClickCount()==2) {
//        	close();
//  			}
//  		}
//  	});
    JPanel progressPanel = new JPanel(new BorderLayout());
    progressPanel.setBorder(BorderFactory.createEmptyBorder(4, 30, 8, 30));
    progressPanel.add(monitor, BorderLayout.CENTER);
    progressPanel.setOpaque(false);
    JLabel label = new JLabel(TrackerRes.getString("Tracker.Splash.Loading") //$NON-NLS-1$
    		+" \""+XML.getName(path)+"\""); //$NON-NLS-1$ //$NON-NLS-2$
    JPanel labelbar = new JPanel();
    labelbar.add(label);
    JButton cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		VideoIO.setCanceled(true);
      	close();
      }
    });
    JPanel buttonbar = new JPanel();
    buttonbar.add(cancelButton);
    contentPane.add(labelbar, BorderLayout.NORTH);
    contentPane.add(progressPanel, BorderLayout.CENTER);
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    FontSizer.setFonts(contentPane, FontSizer.getLevel());
    pack();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - getBounds().width) / 2;
    int y = (dim.height - getBounds().height) / 2;
    setLocation(x, y);
		timer.start();
	}
	
	void stop() {
		timer.stop();
	}
	
	void restart() {
  	monitor.setValue(0);
  	frameCount = Integer.MIN_VALUE;
  	// restart timer
    timer.start();
	}
	
	void setProgress(int progress) {
		monitor.setValue(progress);
	}
	
	void setFrameCount(int count) {
		frameCount = count;
	}
	
	int getFrameCount() {
		return frameCount;
	}
	
	void close() {
		timer.stop();
		setVisible(false);
		TrackerIO.monitors.remove(this);
		dispose();
	}
	

}

/**
 * Transferable class for copying images to the system clipboard.
 */
class TransferImage implements Transferable {
  private Image image;

  TransferImage(Image image) {
    this.image = image;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DataFlavor.imageFlavor};
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return DataFlavor.imageFlavor.equals(flavor);
  }

  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException {
    if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
    return image;
  }
}

