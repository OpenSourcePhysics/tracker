package org.opensourcephysics.cabrillo.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JOptionPane;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.DataTrackSupport;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.Job;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.Tool;

/**
 * A Remote Tool that passes incoming Data and messages to a DataTrack with the same name or ID.
 * If no DataTrack is found, the Data is passed to the TFrame's selected TrackerPanel.
 * 
 * @author Douglas Brown
 */
public class DataTrackTool extends UnicastRemoteObject implements Tool {
	
	/* Define serialVersionUID so RMI serialization can occur reliably.
	The value of this must never change even in future releases */
  private static final long serialVersionUID = 1L;
  
  private TFrame frame;
  private TreeMap<Integer, Tool> replyToTools = new TreeMap<Integer, Tool>();
  private TreeMap<Integer, String> jarPaths = new TreeMap<Integer, String>();
  
  /**
   * Constructor for a TFrame.
   *
   * @param tFrame the TFrame
   * @throws RemoteException
   */
	public DataTrackTool(TFrame tFrame) throws RemoteException {
		frame = tFrame;		
	}

  /**
   * Sends a job to this tool and specifies a tool to reply to. The job xml must
   * define either a Data object or DataTrackSupport.Message object. Data must
   * include (x, y) coordinates and and may define other properties as well. The optional
   * replyTo Tool may also be a (JPanel) source control panel for the DataTrack.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {
    // read the job's XML into an XMLControl
    XMLControl control = new XMLControlElement();
    control.readXML(job.getXML());
    if (control.failedToRead()) {
      return;
    }
    
    // get the source ID and check for handshake
		int sourceID = control.getInt("sourceID"); //$NON-NLS-1$
  	if (control.getBoolean("handshake")) { //$NON-NLS-1$
  		// save replyTo tool
			replyToTools.put(sourceID, replyTo);
  		// save jarPath
			jarPaths.put(sourceID, control.getString("jar_path")); //$NON-NLS-1$
			// send handshake reply
	  	job.setXML(control.toXML());
			replyTo.send(job, null);
			return;
  	}
  	
  	// get the target TrackerPanel
		TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
		if (trackerPanel==null) {
			trackerPanel = new TrackerPanel();
			frame.addTab(trackerPanel);
		}
		
		// set video properties
  	if (control.getPropertyNames().contains("video")) { //$NON-NLS-1$
  		String path = control.getString("video"); //$NON-NLS-1$
  		File videoFile = findFile(path, sourceID);
  		if (videoFile==null || !videoFile.exists()) {
        int result = JOptionPane.showConfirmDialog(trackerPanel,
        		TrackerRes.getString("DataTrackTool.Dialog.VideoNotFound.Message1") //$NON-NLS-1$
    				+" \""+path+"\"" //$NON-NLS-1$ //$NON-NLS-2$
        		+"\n"+TrackerRes.getString("DataTrackTool.Dialog.VideoNotFound.Message2"), //$NON-NLS-1$ //$NON-NLS-2$
    				TrackerRes.getString("DataTrackTool.Dialog.VideoNotFound.Title"),  //$NON-NLS-1$
    				JOptionPane.ERROR_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
          java.io.File[] files = VideoIO.getChooserFiles("open video");  //$NON-NLS-1$
          if (files!=null && files.length>0) {
          	videoFile = files[0];
          }
        }
  		}
  		if (videoFile!=null) {
	    	OSPLog.fine("importing video file "+videoFile.getAbsolutePath()); //$NON-NLS-1$
  			TrackerIO.importVideo(videoFile, trackerPanel, null);
  		}
  	}
  	if (control.getPropertyNames().contains("videoStartFrame")) { //$NON-NLS-1$
  		int start = control.getInt("videoStartFrame"); //$NON-NLS-1$
  		trackerPanel.getPlayer().getVideoClip().setStartFrameNumber(start);
  	}
  	if (control.getPropertyNames().contains("videoEndFrame")) { //$NON-NLS-1$
  		int end = control.getInt("videoEndFrame"); //$NON-NLS-1$
  		trackerPanel.getPlayer().getVideoClip().setEndFrameNumber(end);
  	}
  	if (control.getPropertyNames().contains("videoStepSize")) { //$NON-NLS-1$
  		int size = control.getInt("videoStepSize"); //$NON-NLS-1$
  		trackerPanel.getPlayer().getVideoClip().setStepSize(size);
  	}
  	if (control.getPropertyNames().contains("stepNumber")) { //$NON-NLS-1$
  		int step = control.getInt("stepNumber"); //$NON-NLS-1$
  		trackerPanel.getPlayer().setStepNumber(step);
  	}
  	if (control.getPropertyNames().contains("frameNumber")) { //$NON-NLS-1$
  		int frame = control.getInt("frameNumber"); //$NON-NLS-1$
  		int step = trackerPanel.getPlayer().getVideoClip().frameToStep(frame);
  		trackerPanel.getPlayer().setStepNumber(step);
  	}
  	if (control.getPropertyNames().contains("deleteTracks")) { //$NON-NLS-1$
  		String[] trackNames = (String[])control.getObject("deleteTracks"); //$NON-NLS-1$
  		for (String next: trackNames) {
  			ParticleDataTrack track = findParticleDataTrack(trackerPanel, next, -1);
  			if (track!=null) {
  				track.delete();
  			}
  		}
  	}
  	if (control.getPropertyNames().contains("trk")) { //$NON-NLS-1$
  		String path = control.getString("trk"); //$NON-NLS-1$
  		File trkFile = findFile(path, sourceID);
  		if (trkFile==null || !trkFile.exists()) {
        int result = JOptionPane.showConfirmDialog(trackerPanel,
        		TrackerRes.getString("DataTrackTool.Dialog.FileNotFound.Message1") //$NON-NLS-1$
    				+" \""+path+"\"" //$NON-NLS-1$ //$NON-NLS-2$
        		+"\n"+TrackerRes.getString("DataTrackTool.Dialog.VideoNotFound.Message2"), //$NON-NLS-1$ //$NON-NLS-2$
    				TrackerRes.getString("DataTrackTool.Dialog.FileNotFound.Title"),  //$NON-NLS-1$
    				JOptionPane.ERROR_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
          java.io.File[] files = TrackerIO.getChooserFiles("open trk");  //$NON-NLS-1$
          if (files!=null && files.length>0) {
          	trkFile = files[0];
          }
        }
  		}
  		if (trkFile!=null) {
  	    XMLControlElement trkControl = new XMLControlElement(trkFile.getAbsolutePath());
  	    Class<?> type = trkControl.getObjectClass();
  	    if (!TrackerPanel.class.equals(type)) {
  	      JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
  	          TrackerRes.getString("DataTrackTool.Dialog.InvalidTRK.Message") //$NON-NLS-1$
  	  				+ ": \""+ trkFile.getAbsolutePath()+"\"", //$NON-NLS-1$ //$NON-NLS-2$
  	      		TrackerRes.getString("DataTrackTool.Dialog.InvalidTRK.Title"), //$NON-NLS-1$
  	      		JOptionPane.WARNING_MESSAGE);
  	    }
  	    else {
  	    	OSPLog.fine("loading TRK file "+trkFile.getAbsolutePath()); //$NON-NLS-1$
	        trackerPanel.changed = true;
	        trkControl.loadObject(trackerPanel);
	        trackerPanel.defaultFileName = XML.getName(path);
	        trackerPanel.openedFromPath = trkFile.getAbsolutePath();
	        trackerPanel.setDataFile(trkFile);
	        Tracker.addRecent(trkFile.getAbsolutePath(), false); // add at beginning
  	    }
  		}
  	}
  	  	
  	// get the data, if any
		Data data = (Data)control.getObject("data"); //$NON-NLS-1$
		boolean append = control.getBoolean("append"); //$NON-NLS-1$
		
  	// get the target DataTrack
    DataTrack dataTrack = loadData(trackerPanel, data, append);	
    if (dataTrack==null && data!=null) {
    	dataTrack = trackerPanel.importData(data, replyTo);
    }
    if (dataTrack==null) {
    	String name = control.getString("dataName"); //$NON-NLS-1$
    	int dataID = control.getInt("dataID"); //$NON-NLS-1$
    	dataTrack = findParticleDataTrack(trackerPanel, name, dataID);
    	if (dataTrack!=null) {
    		try {
					dataTrack.setData(data, replyTo);
				} catch (Exception e) {
				}
    	}
    }
    
    // set DataTrack properties
    if (dataTrack!=null) {
    	if (control.getPropertyNames().contains("useDataTime") && dataTrack.getVideoPanel()!=null) { //$NON-NLS-1$
	    	boolean useTrackTime = control.getBoolean("useDataTime"); //$NON-NLS-1$
    		VideoPlayer player = dataTrack.getVideoPanel().getPlayer();
    		player.getClipControl().setTimeSource(useTrackTime? dataTrack: null);
    		player.refresh();
    		if (dataTrack instanceof ParticleDataTrack) {
    			((ParticleDataTrack)dataTrack).refreshInitialTime();
    		}
    	}

    }
  }
  
  /**
   * Sends a message to a replyTo tool in the form of a String-to-String mapping.
   *
   * @param id the replyTo identifier
   * @param message the information to send
   * @return true if sent successfully
   */
  public boolean reply(int id, Map<String, String> message) {
  	Tool tool = replyToTools.get(id);
  	if (tool==null) return false;
		// get message control and set properties based on info map
		XMLControl control = DataTrackSupport.getMessageControl(id);
		for (String key: message.keySet()) {
			control.setValue(key, message.get(key));
		}
  	try {
			tool.send(new LocalJob(control.toXML()), null);
		} catch (RemoteException e) {
			return false;
		}
  	return true;
  }
    
  /**
   * Informs all replyTo tools that Tracker is exiting.
   */
  protected void trackerExiting() {
  	XMLControl control = new XMLControlElement();
  	control.setValue("exiting", true); //$NON-NLS-1$
  	Job job = new LocalJob(control.toXML());
  	try {
			for (Tool tool: replyToTools.values()) {
				tool.send(job, this);
			}
		} catch (RemoteException e) {
		}
  }
  
  /**
   * Attempts to find a file specified by path and source ID.
   * Extracts the file from the source jar if required.
   * 
   * @param path the file path, usually relative
   * @param ID the source ID
   * @return the file, or null if not found
   */
  private File findFile(String path, int ID) {
		File file = null;
		// try to load file directly from path
		Resource res = ResourceLoader.getResource(path);
		if (res==null) {
			String jarPath = jarPaths.get(ID);
			if (jarPath!=null) {
	  		// try to load file from path relative to jar path
				String target = XML.getResolvedPath(path, XML.getDirectoryPath(jarPath));
	  		res = ResourceLoader.getResource(target);
	  		if (res==null) {
  	  		// try to find and extract file entry in the jar file
	  			String name = XML.getName(path);
  				JarEntry entry = null;
					try {
						JarFile jar = new JarFile(jarPath);
						for (Enumeration<JarEntry> en = jar.entries(); en.hasMoreElements();) {
							JarEntry next = en.nextElement();
							if (!next.isDirectory() && next.getName().endsWith(name)) {
								entry = next;
								break;
							}
						}
						jar.close();
					} catch (Exception e) {
					}
  				if (entry!=null) {	  					
  					String source = jarPath+"!/"+entry.getName(); //$NON-NLS-1$
  					source = ResourceLoader.getURIPath(source);
  					file = ResourceLoader.extractFileFromZIP(source, new File(target), false);
  				}
	  		}
			}
		}
		if (file==null && res!=null && res.getFile()!=null) {
			file = res.getFile();
		}
		return file;
  }
  
  /**
   * Finds an existing ParticleDataTrack with a specified name and/or Data ID.
   * 
   * @param trackerPanel the TrackerPanel to search
   * @param name the desired name (may be null)
   * @param dataID the Data ID
   * @return the ParticleDataTrack, or null if none found
   */
  private ParticleDataTrack findParticleDataTrack(TrackerPanel trackerPanel, String name, int dataID) {  	
  	if (trackerPanel!=null) {
    	if (name==null || name.trim().equals("")) { //$NON-NLS-1$
    		name = TrackerRes.getString("ParticleDataTrack.New.Name"); //$NON-NLS-1$
    	}
    	name = name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
    	TTrack track = trackerPanel.getTrack(name);
    	if (track!=null && track instanceof ParticleDataTrack) {
    		return (ParticleDataTrack)track;
    	}
  		for (ParticleDataTrack dataTrack: trackerPanel.getDrawables(ParticleDataTrack.class)) {
  			Data existingData = dataTrack.getData();
  			if (existingData!=null && dataID==existingData.getID()) {
  				return dataTrack;
  			}
  		}
  	}
    return null;
  }
  
  /**
   * Loads data into an existing DataTrack in a TrackerPanel.
   * 
   * @param trackerPanel the TrackerPanel
   * @param data the Data to load
   * @param append true to append data
   * @return the loaded DataTrack, or null if no DataTrack found
   */
  private DataTrack loadData(TrackerPanel trackerPanel, Data data, boolean append) {
  	if (data==null || trackerPanel==null) return null;
  	
  	// look for existing ParticleDataTrack with matching name or data ID
		ParticleDataTrack dataTrack = findParticleDataTrack(trackerPanel, data.getName(), data.getID());
  	if (dataTrack!=null) {
			try {
				if (append) {
					// following call throws exception if (x, y) data not found
  				dataTrack.appendData(data);
  				// display the last point appended
  				VideoPlayer player = trackerPanel.getPlayer();
  				VideoClip videoClip = player.getVideoClip();
  				DataClip dataClip = dataTrack.getDataClip();
  				dataClip.setClipLength(-1); // set clip length to data length
  				int dataEndFrame = dataTrack.getStartFrame()+dataClip.getDataLength()-1;
  				player.setStepNumber(videoClip.frameToStep(dataEndFrame));
				}
				else {
					// following call throws exception if (x, y) data not found
					dataTrack.setData(data);
				}
			} catch (Exception e) {
				// inform user
				JOptionPane.showMessageDialog(frame, 
						TrackerRes.getString("DataTrackTool.Dialog.InvalidData.Message"), //$NON-NLS-1$
						TrackerRes.getString("DataTrackTool.Dialog.InvalidData.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}
  	}
  	return dataTrack;
  }
  
//______________________________  static methods  ____________________________
  
  /**
   * Determines if a jar file is a (likely) data source.
   *
   * @param jarPath the path to a jar file
   * @return true if the jar contains the DataTrackSupport class
   */
  public static boolean isDataSource(String jarPath) {
		try {
			JarFile jar = new JarFile(jarPath);
			String classPath = DataTrackSupport.class.getName().replace(".", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			JarEntry entry = jar.getJarEntry(classPath+".class"); //$NON-NLS-1$
			jar.close();
			return entry!=null;
		} catch (IOException ex) {
		}
		return false;
  }
  
  /**
   * Launches a data source and optionally requests that it send data.
   *
   * @param jarPath the path to a data source jar file
   * @param requestData true to request data
   */
  public static void launchDataSource(String jarPath, boolean requestData) {
  	if (!isDataSource(jarPath)) {
  		// inform user
  		String jarName = TrackerRes.getString("TActions.Action.DataTrack.Unsupported.JarFile") //$NON-NLS-1$
  				+ " \""+XML.getName(jarPath)+"\" "; //$NON-NLS-1$ //$NON-NLS-2$
			JOptionPane.showMessageDialog(null, 
					jarName+TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Message")+".", //$NON-NLS-1$ //$NON-NLS-2$
					TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
  		return;
  	}
		// assemble the command
		final ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("java"); //$NON-NLS-1$
		cmd.add("-jar"); //$NON-NLS-1$
		cmd.add(jarPath);

		// create ProcessBuilder to execute the command
		final ProcessBuilder builder = new ProcessBuilder(cmd);
		
		if (requestData) {
			// set DATA_REQUESTED environment variable
			Map<String, String> env = builder.environment();
			env.put("DATA_REQUESTED", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// log the command
		String command = ""; //$NON-NLS-1$
		for (String next: cmd) {
			command += next + " "; //$NON-NLS-1$
		}
		OSPLog.config(command);
							
		// start the process
		startProcess(builder);
  }
  
  /**
   * Starts a ProcessBuilder and handles its output and error streams.
   *
   * @param builder the ProcessBuilder
   */
  private static void startProcess(final ProcessBuilder builder) {
		// start the process and wait for it to finish
		Runnable runner = new Runnable() {
			public void run() {
				try {
					Process process = builder.start();
					// read output stream from the process--important so process doesn't block
	        InputStream is = process.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line;
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }
			    br.close();
	        
					int result = process.waitFor();
					// if process returns with exit code > 0, print it's error stream
					if (result > 0) {
						isr = new InputStreamReader(process.getErrorStream());
						br = new BufferedReader(isr);
		        while ((line = br.readLine()) != null) {
	            System.err.println(line);
		        }
				    br.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}					
			}
		};
		
		new Thread(runner).start();  	
  }
  
}
