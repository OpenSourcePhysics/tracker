package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.File;
import java.util.List;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.cabrillo.tracker.Tracker;

/**
 * A class to handle apple events in OSX. Requires the stub classes in AppleJavaExtensions.jar 
 * to compile in Windows
 *
 * @author Douglas Brown
 */
public class OSXServices implements com.apple.eawt.AboutHandler, 
	com.apple.eawt.QuitHandler, com.apple.eawt.PreferencesHandler, com.apple.eawt.OpenFilesHandler {

	Tracker tracker;
	OSPSocket socket;
	
	public OSXServices(Tracker app) {
		tracker = app;
		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
		application.setDockIconImage(Tracker.getOSXDockImage());
		application.setAboutHandler(this);
		application.setQuitHandler(this);
		application.setPreferencesHandler(this);
	}
	
	public OSXServices(OSPSocket app) {
		socket = app;
		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
		application.setDockIconImage(Tracker.getOSXDockImage());
		application.setOpenFileHandler(this);
	}
	
	public void handleAbout(com.apple.eawt.AppEvent.AboutEvent e) {
		Tracker.showAboutTracker();
	}
	
	// pig this needs work
	public void openFiles(com.apple.eawt.AppEvent.OpenFilesEvent e) {
//		if (socket!=null && socket.isServer && socket.clientReady) {
		if (socket!=null) {
			List<File> files = e.getFiles();
			String command = OSPSocket.OPEN;
	    String separator = System.getProperty("path.separator"); //$NON-NLS-1$
			for (File next: files) {
				command += next.getAbsolutePath()+separator;
			}
			try {
				socket.send(command);
			} catch (Exception ex) {
			}
		}
	}
	
	public void handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent e,
			com.apple.eawt.QuitResponse response) {
		if (tracker!=null) {
		  TFrame frame = tracker.getFrame();
		  if (frame != null) {
		    for (int i = 0; i < frame.getTabCount(); i++) {
		    	// save tabs in try/catch block so always closes
		      try {
						if (!frame.getTrackerPanel(i).save()) {
						  response.cancelQuit();
						}
					} catch (Exception ex) {
					}
		    }
		  }
		}
		response.performQuit();
	}
	
	public void handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent e) {
		if (tracker!=null) {
			TFrame frame = tracker.getFrame();
			frame.showPrefsDialog();
		}
	}
	
}
