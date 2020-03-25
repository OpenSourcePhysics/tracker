package org.opensourcephysics.media.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.Timer;
import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.Job;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.Tool;

/**
 * A class to support the RMI transfer of data to the Tracker DataTrackTool.
 *
 * @author Douglas Brown
 */
public class DataTrackSupport {

	public static final String HOST = "localhost"; //$NON-NLS-1$
	public static final int PORT = 1099;
	private static Tool remoteTool;
	private static Tool supportTool;
	private static XMLControl messageControl;
	
	private static Timer timer;
  private static PropertyChangeSupport support = new SwingPropertyChangeSupport(new Object());
  private static HashSet<String> dataNames = new HashSet<String>();
  private static boolean connected;
  
  // private constructor to prevent instantiation
  private DataTrackSupport() {}
  
  /**
   * Connects to Tracker and adds a PropertyChangeListener to listen for
   * Tracker messages and requests. This returns false if Tracker is
   * unavailable. The PropertyChangeListener is notified with a "tracker_ready"
   * PropertyChangeEvent when Tracker is ready to receive data.  
   *
   * @param id a number to identify the data source (typically hashcode())
   * @param listener the PropertyChangeListener to notify when the remote tool is ready
   * @return true if Tracker is available
   */
  public static boolean connect(final int id, PropertyChangeListener listener) {
  	if (!isTrackerAvailable()) return false;
  	
  	// don't add the same listener more than once
	  support.removePropertyChangeListener(listener);
	  support.addPropertyChangeListener(listener);
	  
	  
  	// see if Tracker is running (remote tool available)
  	if (getRemoteTool()!=null) {
  		// if already connected, return immediately
  		if (connected) {
  			return true;
  		}
  		// otherwise send handshake message and return
  		return sendHandshake(id);
  	}
  	
  	// if not running, launch Tracker in separate VM  	
		// get the path to the tracker.jar file
		String trackerHome = (String)OSPRuntime.getPreference("TRACKER_HOME"); //$NON-NLS-1$
		String trackerPath = new File(trackerHome, "tracker.jar").getAbsolutePath(); //$NON-NLS-1$
		
		// assemble the command
		final ArrayList<String> cmd = new ArrayList<String>();
		cmd.add("java"); //$NON-NLS-1$
		cmd.add("-classpath"); //$NON-NLS-1$
		cmd.add(trackerPath);
		cmd.add("org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter"); //$NON-NLS-1$

		// create ProcessBuilder to execute the command
		final ProcessBuilder builder = new ProcessBuilder(cmd);
		
		// log the command
		String launchMessage = "launching Tracker with command "; //$NON-NLS-1$
		for (String next: cmd) {
			launchMessage += next + " "; //$NON-NLS-1$
		}
		OSPLog.config(launchMessage);
		
		// start the process
		startProcess(builder);
									
		// start timer to look for remoteTool and send handshake message
		if (timer==null) {
			timer = new Timer(500, new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	if (getRemoteTool()!=null) {
	      		timer.stop();
	      		sendHandshake(id);
	      	}
	      }
      });
		}		
		timer.setInitialDelay(1000);
		timer.setRepeats(true);
		timer.start();
		return true;
  }
  
  /**
   * Sends data to the remote DataTrackTool. Data must include (x, y) positions.
   *
   * @param id a number to identify the data source (typically hashcode())
   * @param data the Data object to send
   * @return true if the data was sent successfully
   */
  public static boolean sendData(int id, Data data) {
  	if (data==null) return false;
  	Map<String, Object> message = new TreeMap<String, Object>();
  	message.put("data", data); //$NON-NLS-1$  	
  	return sendMessage(id, message);
  }
  
  /**
   * Sends previously sent Data with newly appended values. 
   * Note: DataTrackTool reads only the appended values. If previously
   * sent values have changed, use the sendData method instead.
   * 
   * Note: Data should contain previously sent data plus appended data,
   * not appended data only.
   *
   * @param id a number to identify the data source (typically hashcode())
   * @param data the Data with appended values
   * @return true if the data was sent successfully
   */
  public static boolean sendAppendedData(int id, Data data) {
  	if (data==null) return false;
  	if (!dataNames.contains(data.getName())) {
  		return sendData(id, data);
  	}
  	Map<String, Object> message = new TreeMap<String, Object>();
  	message.put("data", data); //$NON-NLS-1$  	
  	message.put("append", true); //$NON-NLS-1$
  	return sendMessage(id, message);
  }
  
  /**
   * Sends a message to the remote DataTrackTool in the form of a String-to-Object mapping.
   * The message may include Data as a "data"-to-Data mapping.
   *
   * @param id a number to identify the data source (typically hashcode())
   * @param message the message to send
   * @return true if sent successfully
   */
  public static boolean sendMessage(int id, Map<String, Object> message) {
  	Tool tool = getRemoteTool();
  	if (tool==null) return false;
		// get message control and set properties based on info map
		XMLControl control = getMessageControl(id);
		Data data = null;
		for (String key: message.keySet()) {
			Object value = message.get(key);
			if (key.equals("data")) { //$NON-NLS-1$
				data = (Data)value;
			}
			control.setValue(key, value);
		}
  	try {
			tool.send(new LocalJob(control.toXML()), getSupportTool());
		} catch (RemoteException e) {
			return false;
		}
  	if (data!=null) {
    	dataNames.add(data.getName());
  	}
  	return true;
  }
    
  /**
   * Determines if a DataTrack-enabled Tracker is available on this machine.
   * This returns true if a tracker.jar file is found in the OSP preference
   * TRACKER_HOME. Requires Tracker version 5.00 (build-date 2015) or above.
   *
   * @return true if Tracker is available (whether or not it is running)
   */
  public static boolean isTrackerAvailable() {
		String trackerHome = (String)OSPRuntime.getPreference("TRACKER_HOME"); //$NON-NLS-1$
		if (trackerHome==null) {
			return false;
		}
		File file = new File(trackerHome, "tracker.jar"); //$NON-NLS-1$
		if (!file.exists()) {
			return false;
		}
		try {
			JarFile jar = new JarFile(file);
	    Manifest mf = jar.getManifest();
	    jar.close();
	    Attributes attributes = mf.getMainAttributes();
	    for (Object obj : attributes.keySet()) {
	    	String key = obj.toString();
	       if (key.contains("Build-Date")) { //$NON-NLS-1$
		       String val = attributes.getValue(key);
		       int year = Integer.parseInt(val.substring(val.length()-4, val.length()));
		       if (year<2015) return false;
	       }
	    }
		} catch (Exception e) {
			return false;
		}
		return true;
  }
  
  /**
   * Determines if data was requested by Tracker when the current process was started.
   * EJS models should check this when first launched and send data if true.
   *
   * @return true if data was requested
   */
  public static boolean isDataRequested() {
  	return System.getenv("DATA_REQUESTED")!=null; //$NON-NLS-1$
  }
  
//____________________________ private methods _______________________________
  
  /**
   * Sends a handshake message.
   *
   * @param id a number to identify the data source (typically hashcode())
   */
  private static boolean sendHandshake(int id) {
		XMLControl control = getMessageControl(id);
		control.setValue("handshake", true); //$NON-NLS-1$
		control.setValue("jar_path", OSPRuntime.getLaunchJarPath()); //$NON-NLS-1$
  	try {
			remoteTool.send(new LocalJob(control.toXML()), getSupportTool());
		} catch (RemoteException e) {
			return false;
		}
		return true;
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
  
  /**
   * Gets a remote copy of the tracker.DataTrackTool.
   *
   * @return the remote tool, or null if not available
   */
  private static Tool getRemoteTool() {
		if (remoteTool==null) {
			try {
				Registry registry = LocateRegistry.getRegistry(HOST, PORT);
				remoteTool = (Tool)registry.lookup("DataTrackTool"); //$NON-NLS-1$
				return remoteTool;
			} catch (Exception ex) {
			}
		}
  	return remoteTool;
  }
  
  /**
   * Gets the support tool.
   *
   * @return the support tool
   */
  private static Tool getSupportTool() {
  	if (supportTool==null) {
  		try {
				supportTool = new SupportTool();
			} catch (RemoteException e) {
			}
  	}
  	return supportTool;
  }
  
//____________________________  static methods  _______________________________
  
  /**
   * Clears and returns the message XMLControl.
   *
   * @return the XMLCntrol
   */
  public static XMLControl getMessageControl(int id) {
  	if (messageControl==null) {
  		messageControl = new XMLControlElement(new Message());
  	}
  	for (String name: messageControl.getPropertyNames()) {
  		messageControl.setValue(name, null);
  	}
  	messageControl.setValue("sourceID", id); //$NON-NLS-1$
  	return messageControl;
  }
    
//____________________________  inner classes  _______________________________
  
  /**
   * A remote Tool sent to Tracker so it can communicate with data clients.
   */
  private static class SupportTool extends UnicastRemoteObject implements Tool {
  	
    /**
     * Constructor required to throw RemoteException.
     */
		protected SupportTool() throws RemoteException {
			super();
		}

		@Override
		public void send(Job job, Tool replyTo) throws RemoteException {
	    XMLControl control = new XMLControlElement();
	    control.readXML(job.getXML());
	    if (control.failedToRead()) return;
	    int sourceID = control.getInt("sourceID"); //$NON-NLS-1$
	    if (control.getBoolean("handshake")) { //$NON-NLS-1$
	    	connected = true;
	    	support.firePropertyChange("tracker_ready", sourceID, null); //$NON-NLS-1$
	    }
	    else if (control.getBoolean("exiting")) { //$NON-NLS-1$
	    	remoteTool = null;
	    	connected = false;
	    	support.firePropertyChange("tracker_exited", null, null); //$NON-NLS-1$
	    }

		}
  	
  }
  
  
  /**
   * A class used to send messages.
   */
  private static class Message {}
  
}
