/*
 * The org.opensourcephysics.media.xuggle package provides Xuggle
 * services including implementations of the Video and VideoRecorder interfaces.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * For additional information and documentation on Open Source Physics,
 * please see <https://www.compadre.org/osp/>.
 */
package org.opensourcephysics.media.xuggle;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This registers Xuggle with VideoIO so it can be used to open and record videos.
 *
 * @author Wolfgang Christian, Douglas Brown
 * @version 1.0
 */
public class XuggleIO {
	
	private static boolean initialized; // BH avoids second call (first is from reflection in Tracker)
	
	/**
   * Registers Xuggle video types with VideoIO class.
   */
  static public void registerWithVideoIO(){ // add Xuggle video types, if available
    if (initialized)
    	return;
    initialized = true;
	  String xugglehome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
    if (xugglehome == null)
    	xugglehome = "Xuggle";
    if (xugglehome!=null) {
      try {
    	  VideoIO.addVideoEngine(new XuggleVideoType());

        // add common video types
      	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
        	VideoFileFilter filter = new VideoFileFilter(ext, new String[] {ext});
        	XuggleVideoType xuggleType = new XuggleVideoType(filter);
        	// avi not recordable with xuggle
          if (ext.equals("avi")) { //$NON-NLS-1$
          	xuggleType.setRecordable(false);
          }
          VideoIO.addVideoType(xuggleType);
          ResourceLoader.addExtractExtension(ext);
      	} 
      	// add additional xuggle types
      	// FLV
        VideoFileFilter filter = new VideoFileFilter("flv", new String[] {"flv"}); //$NON-NLS-1$ //$NON-NLS-2$
        VideoIO.addVideoType(new XuggleVideoType(filter));
        ResourceLoader.addExtractExtension("flv"); //$NON-NLS-1$
      	// WMV
      	filter = new VideoFileFilter("asf", new String[] {"wmv"}); //$NON-NLS-1$ //$NON-NLS-2$
        VideoIO.addVideoType(new XuggleVideoType(filter));
        ResourceLoader.addExtractExtension("wmv"); //$NON-NLS-1$
      	// DV
      	filter = new VideoFileFilter("dv", new String[] {"dv"}); //$NON-NLS-1$ //$NON-NLS-2$
      	XuggleVideoType vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("dv"); //$NON-NLS-1$
      	// MTS
      	filter = new VideoFileFilter("mts", new String[] {"mts"}); //$NON-NLS-1$ //$NON-NLS-2$
      	vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("mts"); //$NON-NLS-1$
      	// M2TS
      	filter = new VideoFileFilter("m2ts", new String[] {"m2ts"}); //$NON-NLS-1$ //$NON-NLS-2$
      	vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("m2ts"); //$NON-NLS-1$
      	// MPG
      	filter = new VideoFileFilter("mpg", new String[] {"mpg"}); //$NON-NLS-1$ //$NON-NLS-2$
      	vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("mpg"); //$NON-NLS-1$
      	// MOD
      	filter = new VideoFileFilter("mod", new String[] {"mod"}); //$NON-NLS-1$ //$NON-NLS-2$
      	vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("mod"); //$NON-NLS-1$
      	// OGG
      	filter = new VideoFileFilter("ogg", new String[] {"ogg"}); //$NON-NLS-1$ //$NON-NLS-2$
      	vidType = new XuggleVideoType(filter);
      	vidType.setRecordable(false);
      	VideoIO.addVideoType(vidType);
        ResourceLoader.addExtractExtension("ogg"); //$NON-NLS-1$
        ResourceLoader.addExtractExtension("mod"); //$NON-NLS-1$
      	// WEBM unsupported by Xuggle
      }
      catch (Throwable ex) { // Xuggle not working
    	  System.out.println(ex.getStackTrace());
      	OSPLog.config("Xuggle exception: "+ex.toString()); //$NON-NLS-1$
      }    	
  	
    }
    else {
    	OSPLog.config("Xuggle not installed? (XUGGLE_HOME not found)"); //$NON-NLS-1$
    }
  }
  
}
