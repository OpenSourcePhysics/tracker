package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;

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
	
	/**
	 * Constructor used by TrackerStarter, which uses the DockIcon & OpenFileHandler
	 */
	public OSXServices() {
		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
		application.setOpenFileHandler(this);
		ImageIcon icon = new ImageIcon(
	      Tracker.class.getResource("resources/images/tracker_icon_256.png")); //$NON-NLS-1$
		application.setDockIconImage(icon.getImage());
	}
	
	/**
	 * Constructor used by Tracker, which uses the DockIcon, AboutHandler, QuitHandler
	 * and PreferencesHandler.
	 * 
	 * @param app the Tracker application
	 */
	public OSXServices(Tracker app) {
		tracker = app;
		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
		application.setAboutHandler(this);
		application.setQuitHandler(this);
		application.setPreferencesHandler(this);
		ImageIcon icon = new ImageIcon(
	      Tracker.class.getResource("resources/images/tracker_icon_256.png")); //$NON-NLS-1$
		application.setDockIconImage(icon.getImage());
	}
	
	/**
	 * AboutHandler
	 */
	public void handleAbout(com.apple.eawt.AppEvent.AboutEvent e) {
		Tracker.showAboutTracker();
	}
	
	/**
	 * OpenFilesHandler
	 */
	public void openFiles(com.apple.eawt.AppEvent.OpenFilesEvent e) {
		List<File> files = e.getFiles();
		String[] args = new String[files.size()];
		for (int i=0; i<args.length; i++) {
			args[i] = files.get(i).getAbsolutePath();
		}
		TrackerStarter.launchTracker(args);					 							
	}
	
	/**
	 * QuitHandler
	 */
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
	
	/**
	 * PreferencesHandler
	 */
	public void handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent e) {
		if (tracker!=null) {
			TFrame frame = tracker.getFrame();
			frame.showPrefsDialog();
		}
	}

}
