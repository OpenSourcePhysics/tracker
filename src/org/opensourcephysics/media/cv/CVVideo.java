/*
 * The org.opensourcephysics.media.cv package provides CV
 * implementations of the Video and VideoRecorder interfaces.
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
 * 
 */
package org.opensourcephysics.media.cv;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.DoubleArray;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoAdapter;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.media.mov.MovieVideo;

import javajs.async.AsyncSwingWorker;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.ffmpeg.global.avutil;

/**
 * A class to display videos using the JavaCV libraries.
 * 
 * Also adds imageCache to improve performance.
 * 
 */
public class CVVideo extends MovieVideo {

	static {
			CVVideoType.register();
			CVThumbnailTool.start();
	}

	// a cache of images for fast recall
	private BufferedImage[] imageCache;
	// maximum number of cached images; for debugging use 0
	private int cacheMax = 0;

	private FFmpegFrameGrabber grabber;
	private Java2DFrameConverter imageConverter;
	private Frame videoFrame;
	private	long frameDelay;
	private javax.swing.Timer playerTimer;
	private long prevSystemTime;
	private long timerDelay;
	  	
	// following used during loading only
	private ArrayList<BufferedImage> imageList;	
	
	/**
	 * Initializes this video and loads a video file specified by name
	 *
	 * @param fileName the name of the video file
	 * @throws IOException
	 */
	CVVideo(String fileName, XMLControl control) throws IOException {
		super(fileName, null, control); // sets "path", "Name", "absolutepath", url, isLocal

		// set properties
		OSPLog.finest("CV video loading " + path + " local?: " + isLocal); //$NON-NLS-1$ //$NON-NLS-2$
		if (isExport) {
			// no need to actually load anything?
			return;
		}
    // Get the video file as an InputStream--pig use ResourceLoader?
//    java.io.InputStream videoStream = getClass().getResourceAsStream(fileName);
    java.io.InputStream videoStream = null;
		try {			
			videoStream = new FileInputStream(path);
		} catch (FileNotFoundException e) {
      System.err.println("Error: file not found: "+path);
      return;
		}

		// create grabber and set up for fast loading
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		grabber = new FFmpegFrameGrabber(videoStream);
    grabber.setOption("hwaccel", "cuda"); // use hardware decoding if possible
    grabber.setAudioStream(-1); // ignore audio streams
    
    imageConverter = new Java2DFrameConverter();
    if (cacheMax > 0)
    	imageList = new ArrayList<BufferedImage>();
    try {
      grabber.start();
      frameCount = grabber.getLengthInFrames();	
      rawDuration = grabber.getLengthInTime() / 1000000.0;
      frameDelay = (long) (1000 / grabber.getFrameRate());
  		// set initial video clip properties
  		startFrameNumber = 0;
  		endFrameNumber = frameCount - 1;

      frameTimes = new ArrayList<Double>();
      // get first frame to display and establish image dimensions
			videoFrame = grabber.grabImage();
			BufferedImage img = imageConverter.convert(videoFrame);
			setImage(img);  // must call setImage() once
			
			// fill frameTimes with predicted times based on frame number and frame dt
      double dt = rawDuration / (double)frameCount;
      for (int i = 0; i < frameCount; i++) {
				int n = Math.round(Math.round(dt *i * 1000000.));
				frameTimes.add(Double.valueOf(n / 1000000.));
			}
			setStartTimes();

      
      // create Runnable to step thru video using separate grabber
      Runnable runner = new Runnable() {
      	@Override
				public void run() {
    			try (java.io.InputStream videoStream2 = new FileInputStream(path);
        		FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(videoStream2);
            Java2DFrameConverter imageConverter2 = new Java2DFrameConverter()) {			
    				
	     			frameTimes.clear();
	     			Frame videoFrame2;
	     			grabber2.start();
	     			for (int i = 0; i < frameCount; i++) {
      				videoFrame2 = grabber2.grabImage();
       				if (imageList != null && i < cacheMax) {
      					// must copy ImageConverter image as it is reused for every frame
      					imageList.add(copyImage(imageConverter2.convert(videoFrame2)));					
      				}
      				Long t = grabber2.getTimestamp();      				
      				frameTimes.add(Double.valueOf(t/1000000.0));
//      				firePropertyChange(PROPERTY_VIDEO_PROGRESS, fileName, i);
      			}
	    			finalizeLoading();
    			} catch (Exception e) {
    				e.printStackTrace();
    	      return;
    			}
      	}
      };
      
//			// use AsyncSwingWorker to run the runner in the background
//			new AsyncSwingWorker(null, null, 10, 0, 1) { // 10 ms delay
//
//				@Override
//				public void initAsync() {
//				}
//
//				@Override
//				public int doInBackgroundAsync(int i) {
//					runner.run();
//					return 1;
//				}
//
//				@Override
//				public void doneAsync() {
////					debugCache();
//				}
//
//			}.execute();
      
   } catch (Exception e) {
			e.printStackTrace();
    }
   
    createPlayerTimer();    
		this.control = control;

	}
	
	private BufferedImage copyImage(BufferedImage src) {
    // Create a new blank image with the same dimensions and type
    BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
    
    // Draw the source image onto the new image
    Graphics2D g2d = copy.createGraphics();
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();
    
    return copy;
}

	@Override
	protected void finalizeLoading() throws IOException {
		// create imageCache
		if (cacheMax > 0) {
			int nImages = imageList.size();
			imageCache = new BufferedImage[nImages];
			for (int i = 0; i < nImages; i++)
				imageCache[i] = imageList.get(i);
				// no longer need imageList
			imageList = null;
		}
		// recreate startTimes array
//		setStartTimes();

//		rawFrameCount = frameCount;
//		if (control != null) {
//			// overwrite startTimes, rawDuration, and frameCount with control's 
//			control = setFromControl(control);			
//		}
	}

	void debugCache() {
		if (imageCache != null) {
			for (int i = 0; i < imageCache.length; i++) {
				dumpImage(i, imageCache[i], "img");
			}
		}
	}

	/**
	 * Plays the video at the current rate. Overrides VideoAdapter method.
	 */
	@Override
	public void play() {
		if (getFrameCount() == 1) {
			return;
		}
		if (!playerTimer.isRunning()) {
			prevSystemTime = System.currentTimeMillis();
			playing = true;
			if (getFrameNumber() >= getEndFrameNumber()) {
				setFrameNumber(getStartFrameNumber());
			}
			playerTimer.start();
			firePropertyChange(Video.PROPERTY_VIDEO_PLAYING, null, Boolean.TRUE); // $NON-NLS-1$
		}
	}

	/**
	 * Stops the video.
	 */
	@Override
	public void stop() {
		if (playerTimer.isRunning()) {
			playing = false;
			playerTimer.stop();
			firePropertyChange(Video.PROPERTY_VIDEO_PLAYING, null, Boolean.FALSE); // $NON-NLS-1$
		}
	}

	/**
	 * Sets the frame number. Overrides VideoAdapter setFrameNumber method.
	 *
	 * @param n the desired frame number
	 */
	@Override
	public void setFrameNumber(int n) {
		if (n == frameNumber)
			return;
		super.setFrameNumber(n);
		BufferedImage bi = getImageAtFrame(frameNumber); 
		rawImage = bi==null? rawImage: bi;
		invalidateVideoAndFilter();
		notifyFrame(frameNumber, false);
	}
	
	private BufferedImage getImageAtFrame(int frameNum) {
		if (imageCache != null && frameNum < imageCache.length) {
			return imageCache[frameNum];
		}
		else {
			try {
				if (frameNumber == 0) {
					grabber.restart();
					videoFrame = grabber.grabImage();	            
					return imageConverter.convert(videoFrame);	
				}
				else {
					grabber.setFrameNumber(frameNumber-1);
					videoFrame = grabber.grabImage();
		 			return imageConverter.convert(videoFrame);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @return the duration of the media in milliseconds or -1 if no video, or 100
	 *         if one frame
	 */
	@Override
	public double getFrameCountDurationMS() {
		int n = getFrameCount();
		if (n == 1)
			return 100; // arbitrary duration for single-frame video!
		return rawDuration * 1000;
	}

	/**
	 * Disposes of this video.
	 */
	@Override
	public void dispose() {
		System.out.println("CVVideo.dispose");
		super.dispose();
	}

//______________________________  private methods _________________________

	/**
	 * Creates the timer.
	 */
	private void createPlayerTimer() {
		playerTimer = new javax.swing.Timer(0, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!playing) {
					stop();
					playerTimer.stop();
					return;
				}
				int n = getFrameNumber();
				int targetFrameNum = n + 1; 
				if (videoClip != null) {
					int stepNum = videoClip.frameToStep(n);
					targetFrameNum = videoClip.stepToFrame(stepNum + 1);
				}
				setTimerDelayAndGoTo(targetFrameNum);
			}			
		});
	}

	private void setTimerDelayAndGoTo(int targetFrameNum) {
		if (prevSystemTime <= 0) {
			prevSystemTime = System.currentTimeMillis();
		}
		else {
			playerTimer.stop();
			
			long t = System.currentTimeMillis();
			long elapsed = t - prevSystemTime;
			prevSystemTime = t;
			
			long stepDelay = videoClip == null? frameDelay: frameDelay * videoClip.getStepSize();
			long targetDelay = Math.round(stepDelay / getRate());
			long diff = targetDelay - elapsed;
			long delay = timerDelay + Math.round(diff);
			delay = Math.max(0, delay);
			timerDelay = delay;
			playerTimer.setInitialDelay(Math.round(delay));
			playerTimer.restart();

			if (targetFrameNum <= getEndFrameNumber()) {
				setFrameNumber(targetFrameNum);
			} else if (looping) {
				setFrameNumber(getStartFrameNumber());
			} else {
				stop();
			}
		}
	}

	@Override
	protected boolean seekMS(double timeMS) {
		// not used
		return false;
	}

	private static String DEBUG_DIR = "c:/temp/tmp/";

	private void dumpImage(int i, BufferedImage bi, String froot) {
		if (DEBUG_DIR == null) {
			System.err.println("No image cache!");
			return;
		}
		try {
			String ii = "00" + i;
			File outputfile = new File(DEBUG_DIR + froot + ii.substring(ii.length() - 2) + ".png");
			ImageIO.write(bi, "png", outputfile);
			System.out.println("CVVideo " + outputfile + " created");
		} catch (IOException e) {
		}

	}

	/**
	 * Sets the initial image, size, coords, aspects.
	 *
	 * @param image the image
	 */
	private void setImage(BufferedImage image) {
		rawImage = image;
		size.width = image.getWidth();
		size.height = image.getHeight();
		refreshBufferedImage();
		// create coordinate system and relativeAspects
		coords = new ImageCoordSystem(frameCount);
		coords.addPropertyChangeListener(this);
		aspects = new DoubleArray(frameCount, 1);
	}

	@Override
	public String getTypeName() {
		return MovieFactory.ENGINE_CV;
	}

	/**
	 * Returns an XML.ObjectLoader to save and load AVPVideo data.
	 *
	 * @return the object loader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load CVVideo data.
	 */
	static public class Loader extends MovieVideo.Loader {

		@Override
		protected VideoAdapter createVideo(XMLControl control, String path) throws IOException {
			CVVideo video = new CVVideo(path, control);
			setVideo(path, video, MovieFactory.ENGINE_CV);
			return video;
		}
		
		@Override
		public void saveObject(XMLControl control, Object obj) {
			super.saveObject(control, obj);
			control.setValue("start_times", null); // unneeded for CVVideos??
		}

	}

	@Override
	protected String getPlatform() {
		return PLATFORM_JAVA;
	}


	@Override
	protected BufferedImage getImageForMSTimePoint(double timeSec) {
		// not used
		return null;
	}


}
