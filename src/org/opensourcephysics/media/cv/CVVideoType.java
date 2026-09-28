/*
 * The org.opensourcephysics.media.frame package provides video
 * frame services including implementations of the Video and VideoRecorder interfaces
 * using Xuggle (Java) and JS (JavaScript -- our minimal implementation).
 *
 * Copyright (c) 2026  Douglas Brown and Wolfgang Christian.
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
package org.opensourcephysics.media.cv;

import java.io.File;
import java.io.IOException;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.media.gif.GifVideoType;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.media.mov.MovieVideoType;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This implements the VideoType interface with the CVVideo type.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class CVVideoType extends MovieVideoType {
	
	public static void main(String[] args) {
		CVVideoType type = new CVVideoType();
		System.out.println("pig testing "+type);
	}

	public static void register() {
		// Registers JavaCV video types with VideoIO class.
		// Executes once only, via this static initializer.
		// Each extension is {record, read} (record = null if not recordable)
		String[][] EXTENSIONS = { 
				{ null, "avi" }, //$NON-NLS-1$
				{ null, "flv" }, //$NON-NLS-1$ //$NON-NLS-2$
				{ null, "mov" }, //$NON-NLS-1$ //$NON-NLS-2$
				{ "mp4", "mp4" }, //$NON-NLS-1$ //$NON-NLS-2$
				{ null, "wmv" } //$NON-NLS-1$ //$NON-NLS-2$
		};
		
		for (String[] ext : EXTENSIONS) {
			boolean isRecordable = (ext[0] != null);
			String containerType = ext[1];
			String[] extensions = new String[] { ext[isRecordable ? 0 : 1] };
			VideoFileFilter filter = new VideoFileFilter(containerType, extensions); // $NON-NLS-1$
			MovieVideoType vidType = new CVVideoType(filter);
			vidType.setRecordable(isRecordable);
			VideoIO.addVideoType(vidType);
			ResourceLoader.addExtractExtension(ext[0]);
		}
	}



	
	/**
	 * Constructor attempts to load a movie class the first time used. This will
	 * throw an error if movies are not available.
	 */
	public CVVideoType() {
		super();
	}

	/**
	 * Constructor with a file filter for a specific container type.
	 * 
	 * @param filter the file filter
	 */
	public CVVideoType(VideoFileFilter filter) {
		super(filter);
	}

	/**
	 * Gets the name and/or description of this type.
	 *
	 * @return a description
	 */
	@Override
	public String getDescription() {
		if (singleTypeFilter != null)
			return singleTypeFilter.getDescription();
		return MediaRes.getString("CVVideoType.Description"); //$NON-NLS-1$
	}

	/**
	 * Return true if the specified video is this type.
	 *
	 * @param video the video
	 * @return true if the video is this type
	 */
	@Override
	public boolean isType(Video video) {
		if (!video.getClass().equals(CVVideo.class))
			return false;
		if (singleTypeFilter == null)
			return true;
		String name = (String) video.getProperty("name"); //$NON-NLS-1$
		return singleTypeFilter.accept(new File(name));
	}

	@Override
	public Video getVideo(String name, String basePath, XMLControl control) {
		Video video = null;
		try {
			video = new CVVideo(XML.getResolvedPath(name, basePath), control);
			video.setProperty("video_type", this); //$NON-NLS-1$
		} catch (IOException ex) {
			OSPLog.fine(getDescription() + ": " + ex.getMessage()); //$NON-NLS-1$
		}
		return video;
	}

	/**
	 * Gets a CV video recorder.
	 *
	 * @return the video recorder
	 */
	@Override
	public VideoRecorder getRecorder() {
		return new CVVideoRecorder(this);
	}

	@Override
	public String getTypeName() {
		return MovieFactory.ENGINE_CV;
	}

	@Override
	public String toString() {
		return _toString();
	}
	
}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2026 The Open Source Physics project
 * https://www.compadre.org/osp
 */
