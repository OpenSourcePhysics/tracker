package org.opensourcephysics.media;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.cabrillo.tracker.Tracker;
import org.opensourcephysics.display.OSPRuntime;

import javajs.api.js.HTML5Applet;

/**
 * When this class is statically initialized using new TrackerCamera(), 
 *  j2s/_ES6/tracker-camera-import.js is loaded. It creates
 *  
 *  Tracker.TrackerCameraImporter.createDialog(), which is 
 *  called in the instance using SwingUtilities.invokeLater() to make sure
 *  that we have processed the script file. 
 * 
 *  When the 
 * 
 * @author hanso
 *
 */
public class TrackerCamera {
	static {
		// once-only loading of tracker-camera-import.js
		String path = null;
		try {
			/**
			 * Import _ES6/tracker-camera-import.js We just load a small JavaScript piece
			 * 
			 * @j2sNative
			 * 
			 * 
			 */
			{
			}
		} catch (Throwable t) {
			System.err.println("Could not load " + path);
			//
		}

	}

	
	public TrackerCamera(TFrame frame) {
	 @SuppressWarnings("unused")
	HTML5Applet applet = OSPRuntime.jsutil.getAppletForComponent(frame);
	 Tracker app = null;	
	 TrackerCamera me = this;
		// If tracker-camera-import.js has not been loaded, 
	    // then create it and run openDialog() asynchronously and returrn.
			/**
			 * @j2sNative
			 * 
			 *  app = applet.app;
			 *  if (!J2S.TrackerCameraImporter) {
			 * 	  var path = applet._j2sPath +
			 *      "/_ES6/tracker-camera-import.js"; 
			 *    $.getScript(path, function(){me.openDialog$O(app)});
			 *    return;
			 *  }
			 */
	me.openDialog(app);
	}
	
	void openDialog(Object app) {
		/**
		 * @j2sNative
		 * 
		 * 			J2S.TrackerCameraImporter.createDialog(app);
		 */
	}

}