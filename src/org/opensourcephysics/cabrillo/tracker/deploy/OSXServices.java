package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.File;
import java.util.ArrayList;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.cabrillo.tracker.Tracker;
import org.opensourcephysics.cabrillo.tracker.TrackerIO;

/**
 * A class to handle apple events in OSX. Requires the stub classes in AppleJavaExtensions.jar 
 * to compile in Windows
 *
 * @author Douglas Brown
 */
public class OSXServices implements com.apple.eawt.AboutHandler, 
	com.apple.eawt.QuitHandler, com.apple.eawt.PreferencesHandler, com.apple.eawt.OpenFilesHandler {

	public static ArrayList<File> filesToOpen = new ArrayList<File>();
	Tracker tracker;
	
	public OSXServices() {
		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
		application.setDockIconImage(Tracker.getOSXDockImage());
		application.setAboutHandler(this);
		application.setQuitHandler(this);
		application.setPreferencesHandler(this);
	}
	
	public void setTracker(Tracker app) {		
		tracker = app;
		TFrame frame = tracker.getFrame();
		for (File file: filesToOpen) {
			TrackerIO.open(file, frame);
		}
		filesToOpen.clear();
	}
	
	public void handleAbout(com.apple.eawt.AppEvent.AboutEvent e) {
		Tracker.showAboutTracker();
	}
	
	// pig this needs work
	public void openFiles(com.apple.eawt.AppEvent.OpenFilesEvent e) {
		filesToOpen.addAll(e.getFiles());
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
