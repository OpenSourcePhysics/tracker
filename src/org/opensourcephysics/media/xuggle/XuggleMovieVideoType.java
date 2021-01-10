/*
 * The org.opensourcephysics.media.frame package provides video
 * frame services including implementations of the Video and VideoRecorder interfaces
 * using Xuggle (Java) and JS (JavaScript -- our minimal implementation).
 *
 * Copyright (c) 2021  Douglas Brown and Wolfgang Christian.
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
import java.io.File;
import java.io.IOException;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.media.mov.MovieVideoType;

/**
 * This implements the VideoType interface with a Xuggle or JS type.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XuggleMovieVideoType extends MovieVideoType {
  
	/**
	 * Constructor attempts to load a movie class the first time used. This will
	 * throw an error if movies are not available.
	 */
	public XuggleMovieVideoType() {
		super();
	}

  /**
   * Constructor with a file filter for a specific container type.
   * 
   * @param filter the file filter 
   */
  public XuggleMovieVideoType(VideoFileFilter filter) {
	  super(filter);
  }

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  @Override
public String getDescription() {
  	if (singleTypeFilter!=null)
  		return singleTypeFilter.getDescription();
    return MediaRes.getString("XuggleVideoType.Description"); //$NON-NLS-1$
  }
  
  /**
   * Return true if the specified video is this type.
   *
   * @param video the video
   * @return true if the video is this type
   */
  @Override
public boolean isType(Video video) {
  	if (!video.getClass().equals(XuggleVideo.class)) return false;
  	if (singleTypeFilter==null) return true;
  	String name = (String)video.getProperty("name"); //$NON-NLS-1$
  	return singleTypeFilter.accept(new File(name));
  }

  /**
   * Opens a named video as a XuggleVideo. Overrrides MovieVideoType.
   *
   * @param name the name of the video
   * @return a new Xuggle video
   */
	@Override
	public Video getVideo(String path) {
	  return getVideo(path, null);
  }

	@Override
	public Video getVideo(String name, String basePath) {
		XuggleVideo video;
		try {
			video = new XuggleVideo(XML.getResolvedPath(name, basePath));
			video.setProperty("video_type", this); //$NON-NLS-1$
		} catch (IOException ex) {
			OSPLog.fine(getDescription() + ": " + ex.getMessage()); //$NON-NLS-1$
			video = null;
		}
		invalid = (video == null);
		return video;
	}

  /**
   * Gets a Xuggle video recorder.
   *
   * @return the video recorder
   */
  @Override
public VideoRecorder getRecorder() {
  	return new XuggleVideoRecorder(this);  	
  }

  @Override
public String getTypeName() {
		return MovieFactory.ENGINE_XUGGLE; 
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
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
 * Copyright (c) 2021  The Open Source Physics project
 *                     https://www.compadre.org/osp
 */
