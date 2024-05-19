/*
 * The org.opensourcephysics.media.xuggle package provides Xuggle
 * services including implementations of the Video and VideoRecorder interfaces.
 *
 * Copyright (c) 2024  Douglas Brown and Wolfgang Christian.
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
package org.opensourcephysics.media.xuggle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.DoubleArray;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.IncrementallyLoadable;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoAdapter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.media.mov.MovieVideo;
import org.opensourcephysics.media.mov.SmoothPlayable;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

/**
 * A class to display videos using the Xuggle library. Xuggle in turn uses
 * FFMpeg as its video engine.
 * 
 * Support for B-Frames added by Bob Hanson 2021.01.10. As discussed at
 * https://groups.google.com/g/xuggler-users/c/PNggEjfsBcg by Art Clarke:
 * 
 * <quote> B-frames are frames that, in order to decode, you need P-frames from
 * the future. They are used in the H264 and Theora codecs for more compression,
 * but it means that a future P-frame (with a higher PTS) may show up with a DTS
 * before a past B-frame (with a lower PTS).
 * 
 * and
 * 
 * The trick is that we report the DTS of frames (i.e. when they need to be
 * decoded) not the PTS (presentation time stamps) in the indexes. DTS will
 * always be in-order but PTS will not. </quote>
 * 
 * 
 * Also adds imageCache to improve performance. Initially set to 50 images, but
 * ALWAYS includes any !isComplete() pictures. These images are always
 * precalculated.
 * 
 */
public class XuggleVideo extends MovieVideo implements SmoothPlayable, IncrementallyLoadable {

//	private final static int FRAME = 1;
//	private final static int PREVFRAME = 0;

	static {
		IContainer.make(); // throws exception if xuggle not available
		XuggleThumbnailTool.start();
    	XuggleMovieVideoType.register();
	}

	/**
	 * a cache of images for fast recall; always all incomplete images and up to
	 * CACHE_MAX images total
	 */
	private BufferedImage[] imageCache;

	/**
	 * for debugging, 0, meaning "just the incomplete frames"; for general purposes,
	 * up to CACHE_MAX images.
	 */
	private int cacheMax = 0;

	private RandomAccessFile raf;

	// maps frame number to timestamp of displayed packet (last packet loaded)
	private Long[] packetTimeStamps;
	// maps frame number to timestamp of key packet (first packet loaded)
	private Long[] keyTimeStamps;
	// array of frame start times in milliseconds

	private IContainer container;
	private IStreamCoder videoDecoder;
	private IVideoResampler resampler;
	private IPacket packet;
	private IVideoPicture picture;
	private double timebase;
	private IConverter converter;

	// all of the following used during loading only
	private ArrayList<Long> packetTSList;
	private ArrayList<Long> keyTSList;
	private ArrayList<BufferedImage> imageList;
	private int index = 0;
	private long keyTimeStamp = Long.MIN_VALUE;
	
	// deprecated private int[] frameRefs;

	private int packetCount;

	/**
	 * The firstDisplayPacket is the index of the first displayable video frame.
	 * When the firstDisplayPacket > 0, it means that there are B-Frames(?) that
	 * precede it which must be decoded in order to display the firstDisplayPacket.
	 */
	private int firstDisplayPacket = 0;

	
	/**
	 * true when firstDisplayPacket > 0; indicating that early B(-like?) frames must
	 * be decoded. This seems to disallow key frames. But I can't be sure. Maybe
	 * they just need to be decoded with the key frames themselves. I have not
	 * figured that part out.
	 */
//	private boolean haveBFrames;	

	private int streamIndex = -1;
	private long keyTS0 = Long.MIN_VALUE;
//	private long keyTS1 = Long.MIN_VALUE;

	/**
	 * systemStartPlayTime is the system time when play starts
	 */
	private long systemStartPlayTime;
	/**
	 * frameStartPlayTime is the frame time where play starts
	 */
	private double frameStartPlayTime;
	
	private boolean playSmoothly = false;
	
	
	/**
	 * times from this engine for its reading of the starting times;
	 * distinct from startTimes, which may come from another creator
	 * via a video XMLControlElement
	 */
	protected double[] rawStartTimes;

	/**
	 * 
	 * Currently not called.
	 * 
	 * Gets the number of the last frame before the specified time in milliseconds for the "raw"
	 * start times - the ones for this engine, not the ones from controls.
	 * 
	 * If rawStartTimes is null (always the case in JavaScript), then just use startTimes.
	 * 
	 * It is not clear yet whether 
	 * 
	 * A 0.1-ms slop is added to ensure that double rounding errors are not involved 
	 *
	 * @param time the time in milliseconds
	 * @return the frame number, or -1 if not found
	 */
	protected int getRawFrameNumberBefore(double time) {
		if (rawStartTimes == null)
			return getFrameNumberBefore(time);
		time += TIME_SLOP_MS;
		for (int i = 0; i < rawStartTimes.length; i++) {
			if (time < rawStartTimes[i]) {
				return i - 1;
			}
		}
		return (time < rawDuration * 1000 ? rawStartTimes.length - 1 : -1);
	}


	/**
	 * Initializes this video and loads a video file specified by name
	 *
	 * @param fileName the name of the video file
	 * @param control 
	 * @throws IOException
	 */
	XuggleVideo(String fileName, XMLControl control) throws IOException {
		super(fileName, null, control);
		// create and open a Xuggle container
		// set properties
		OSPLog.finest("Xuggle video loading " + path + " local?: " + isLocal); //$NON-NLS-1$ //$NON-NLS-2$
		if (isExport) {
			// no need to actually load anthing?
			return;
		}
//	    startFailDetection();
//		stopFailDetection();
		//frameRefs = new int[] { -1, -1 };
		frameCount = -1;
		this.control = control;
		String err = openContainer();
		if (err != null) {
			dispose();
			throw new IOException(err);
		}
		packetTSList = new ArrayList<Long>();
		keyTSList = new ArrayList<Long>();
		frameTimes = new ArrayList<Double>();
		firePropertyChange(PROPERTY_VIDEO_PROGRESS, fileName, 0);
		firstDisplayPacket = 0;
		if (!VideoIO.loadIncrementally) {
			// NOT just dropping a video
			// step thru container quickly and find all video frames
			while (loadMoreFrames(500)) {
			}
		}

	}

	@Override
	protected void finalizeLoading() throws IOException {
		//stopFailDetection();
		// throw IOException if no frames were loaded
		packetCount = frameCount = packetTSList.size();
		if (packetCount == firstDisplayPacket) {
			firePropertyChange(PROPERTY_VIDEO_PROGRESS, path, null);
			dispose();
			throw new IOException("packets loaded but no complete picture"); //$NON-NLS-1$
		}

		System.out.println(
				"XuggleVideo found " + firstDisplayPacket + " incomplete out of " + packetCount + " total packets");

//		// create imageCache
		if (cacheMax > 0) {
		int nImages = imageList.size();
		imageCache = new BufferedImage[nImages + firstDisplayPacket];
		for (int i = 0, p = firstDisplayPacket; i < nImages; i++)
			imageCache[p++] = imageList.get(i);
			// no longer need imageList
			imageList = null;
		}
		packetTimeStamps = packetTSList.toArray(new Long[packetCount]);
		keyTimeStamps = keyTSList.toArray(new Long[packetCount]);
		// no longer need packetTSList and keyTSList
		packetTSList = null;
		keyTSList = null;
		// set initial video clip properties
		startFrameNumber = 0;
		frameCount = packetCount - firstDisplayPacket;
		endFrameNumber = frameCount - 1;
		// create startTimes array
		rawDuration = container.getDuration()/1e6;
		setStartTimes();
		rawStartTimes = startTimes;
		rawFrameCount = frameCount;
		if (control != null) {
			// overwrite startTimes, rawDuration, and frameCount with control's 
			control = setFromControl(control);			
		}
		if (control == null) {
			rawStartTimes = null;
		} else {
			setForControl();
		}

//		if (imageCache.length > firstDisplayPacket)
//			setImage(imageCache[firstDisplayPacket]);

		fullyLoaded = true;
		seekToStart();	
		loadPictureFromNextPacket();
		BufferedImage img = getImage(0);
		firePropertyChange(PROPERTY_VIDEO_PROGRESS, path, null);
		if (img == null) {
			dispose();
			throw new IOException("No images"); //$NON-NLS-1$
		}
		setImage(img);
	}

	private void setForControl() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean loadMoreFrames(int n) throws IOException {
		if (isFullyLoaded())
			return false;
		System.out.println("Xuggle.loadMoreFrames");
		int finalIndex = index + n;
		long lastDTS = Long.MIN_VALUE;
		boolean haveImages = false;
		while (index < finalIndex && container.readNextPacket(packet) >= 0) {
			if (VideoIO.isCanceled()) {
				//stopFailDetection();
				firePropertyChange(PROPERTY_VIDEO_PROGRESS, path, null);
				// clean up
				dispose();
				throw new IOException("Canceled by user"); //$NON-NLS-1$
			}
			if (isCurrentStream()) {
				// wouldn't we want to exit the while loop if the stream has changed?
				long dts = packet.getTimeStamp(); // decode time stamp
				if (keyTimeStamp == Long.MIN_VALUE || packet.isKeyPacket()) {
					keyTimeStamp = dts;
				}
				int offset = 0;
				int size = packet.getSize();
//				System.out.println("XV " + imageList.size());
				while (offset < size) {
					// decode the packet into the picture
					int bytesDecoded = videoDecoder.decodeVideo(picture, packet, offset);
					// check for errors
					if (bytesDecoded < 0)
						break;
					offset += bytesDecoded;
					if (!picture.isComplete()) {
						System.out.println("!! XuggleVideo picture was incomplete! dts=" + dts + " index=" + index);
						if (!haveImages)
							firstDisplayPacket++;
						continue;
					}
				}
				if (dts == lastDTS)
					continue;
				lastDTS = dts;
				// save valid buffered images for cache
				boolean isComplete = picture.isComplete();
				if (isComplete)
					haveImages = true;
				if (cacheMax > 0 && isComplete && imageList.size() < cacheMax - firstDisplayPacket) {
					imageList.add(getBufferedImage());
				}

//				dumpImage(containerFrame, getBufferedImage(), "C");				
//				System.out.println(index + " dts=" + dts + " kts=" + keyTimeStamp + " "
//						+ packet.getFormattedTimeStamp() + " " + picture.getFormattedTimeStamp() + " " + picture.isComplete());

				packetTSList.add(dts);
				keyTSList.add(keyTimeStamp);
				if (keyTS0 == Long.MIN_VALUE)
					keyTS0 = dts;
				frameTimes.add((dts - keyTS0) * timebase);
				firePropertyChange(PROPERTY_VIDEO_PROGRESS, path, index);
				//frameRefs[FRAME] = index++;
			}
		}
		boolean continuing = (index == finalIndex);
		if (!continuing && !isFullyLoaded()) {
			finalizeLoading();
		}
		return continuing;
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
		int n = getFrameNumber() + 1;
		playing = true;
		firePropertyChange(Video.PROPERTY_VIDEO_PLAYING, null, new Boolean(true));
		startPlayingAtFrame(n);
	}

	/**
	 * Stops the video.
	 */
	@Override
	public void stop() {
		playing = false;
		firePropertyChange(Video.PROPERTY_VIDEO_PLAYING, null, new Boolean(false));
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
		BufferedImage bi = getImage(n = getFrameNumber());
		if (bi == null)
			return;
		rawImage = bi;
		invalidateVideoAndFilter();
		notifyFrame(n, false);
		if (isPlaying()) {
			SwingUtilities.invokeLater(() -> {
				continuePlaying();
			});
		}
	}

	/**
	 * Sets the relative play rate. Overrides VideoAdapter method.
	 *
	 * @param rate the relative play rate.
	 */
	@Override
	public void setRate(double rate) {
		super.setRate(rate);
		if (isPlaying()) {
			startPlayingAtFrame(getFrameNumber());
		}
	}

	/**
	 * Gets the duration of the media, including a time for the last frame
	 * 
	 * From XuggleVideo code, now also for JSMovieVideo
	 * 
	 * <pre>
	 * 
	 * // ....[0][1][2]...[startFrame][i]...[j][endFrame]...[frameCount-1]
	 * // ....|-----------------duration---------------------------------]
	 * // ....^..^..^..^..^...........^..^..^..^.........^..^ startTimes[i]
	 * // ............................|--| frameDuration[i]
	 * // ..................................................|------------|
	 * // ..................................................frameDuration[frameCoumt-1]
	 * // (note that final frame duration is defined 
	 * // as frameDuration[frameCount-2] here)
	 * 
	 * </pre>
	 *
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
	 * Sets the playSmoothly flag.
	 * 
	 * @param smooth true to play smoothly
	 */
	@Override
	public void setSmoothPlay(boolean smooth) {
		playSmoothly = smooth;
	}

	/**
	 * Gets the playSmoothly flag.
	 * 
	 * @return true if playing smoothly
	 */
	@Override
	public boolean isSmoothPlay() {
		return playSmoothly;
	}

	/**
	 * Disposes of this video.
	 */
	@Override
	public void dispose() {
		// System.out.println("XuggleVideo.dispose");
		super.dispose();
		disposeXuggle();
	}

	private void disposeXuggle() {
		if (raf != null) {
			try {
				// System.err.println("XuggleVideo.dispose path =" + path);
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			raf = null;
		}

		if (videoDecoder != null) {
			videoDecoder.close();
			videoDecoder.delete();
			videoDecoder = null;
		}
		if (picture != null) {
			picture.delete();
			picture = null;
		}
		if (packet != null) {
			packet.delete();
			packet = null;
		}
		if (container != null) {
			if (container.isOpened())
				container.close();
			container.delete();
			container = null;
		}
		if (newPic != null) {
			newPic.delete();
			newPic = null;
		}
		if (converter != null) {
			converter.delete();
			converter = null;
		}
		if (resampler != null) {
			resampler.delete();
			resampler = null;
		}

		frameTimes = null;

		imageCache = null;

		streamIndex = firstDisplayPacket = -1;
		rawImage = null;
		resampler = null;

		keyTS0 = /* keyTS1 = */ Long.MIN_VALUE;
	}

//______________________________  private methods _________________________

	/**
	 * Sets the system and frame start times.
	 * 
	 * @param frameNumber the frame number at which playing will start
	 */
	private void startPlayingAtFrame(int frameNumber) {
		systemStartPlayTime = System.currentTimeMillis();
		frameStartPlayTime = getFrameTime(frameNumber);
		setFrameNumber(frameNumber);
	}

	/**
	 * Plays the next time-appropriate frame at the current rate.
	 */
	protected void continuePlaying() {
		int n = getFrameNumber();
		int endNo = getEndFrameNumber();
		if (n < endNo) {
			long elapsedTime = System.currentTimeMillis() - systemStartPlayTime;
			double frameTime = frameStartPlayTime + getRate() * elapsedTime;
			int frameToPlay = getFrameNumberBefore(frameTime);
			System.out.println("xug.contin. "+ n + "/ " + frameToPlay + " /" + endNo);
			while (frameToPlay > -1 && frameToPlay <= n) {
				elapsedTime = System.currentTimeMillis() - systemStartPlayTime;
				frameTime = frameStartPlayTime + getRate() * elapsedTime;
				frameToPlay = getFrameNumberBefore(frameTime);
				System.out.println("xug.contin. "+ n + "/ " + frameToPlay + " /" + endNo);
			}
			if (frameToPlay == -1) {
				frameToPlay = endNo;
			}
			setFrameNumber(frameToPlay);
		} else if (looping) {
			startPlayingAtFrame(getStartFrameNumber());
		} else {
			stop();
		}
	}


	@SuppressWarnings("deprecation")
	private String openContainer() {
		disposeXuggle();
		container = IContainer.make();
		if (isLocal) {
			try {
				// System.err.println("XV opening " + path);
				raf = new RandomAccessFile(path, "r"); //$NON-NLS-1$

			} catch (FileNotFoundException e) {
			}
		} else {
			raf = null;
			System.out.println("!!XuggleVideo path should be local!" + path);
		}
		if ((raf == null ? container.open(path, IContainer.Type.READ, null)
				: container.open(raf, IContainer.Type.READ, null)) < 0) {
			return "Container could not be opened for " + path;
		}
		if (streamIndex < 0) {
			// find the first video stream in the container
			int nStreams = container.getNumStreams();
			for (int i = 0; i < nStreams; i++) {
				IStream nextStream = container.getStream(i);
				// get the pre-configured decoder that can decode this stream
				IStreamCoder coder = nextStream.getStreamCoder();
				// get the type of stream from the coder's codec type
				if (coder.getCodecType().equals(ICodec.Type.CODEC_TYPE_VIDEO)) {
					streamIndex = i;
					// System.out.println("XuggleVideo Stream index set to " + i);
					videoDecoder = coder;
					timebase = nextStream.getTimeBase().getValue();
					break;
				}
			}
			if (streamIndex < 0) {
				return "no video stream found in " + path;
			}
		} else {
			videoDecoder = container.getStream(streamIndex).getStreamCoder();
			timebase = container.getStream(streamIndex).getTimeBase().getValue();
		}
		if (videoDecoder.open() < 0) {
			// deprecated to open(options,options) but there is no obvious way to 
			// make that the same.
			// https://github.com/artclarke/xuggle-xuggler/blob/master/generated/java/com/xuggle/xuggler/IStreamCoder.java
			return "unable to open video decoder in " + path;
		}
		newPicture();
		packet = IPacket.make();
//		preLoadContainer();
//		seekToStart(); // DB 2/27/22 commented out to fix AVI black images bug
		return null;
	}

	private void newPicture() {
		picture = IVideoPicture.make(videoDecoder.getPixelType(), videoDecoder.getWidth(), videoDecoder.getHeight());
	}

	/**
	 * uses seekKeyFrame: 
	 *  
	 * Seeks to timestamp in the container.
	 * 
	 * Seeking will be done so that the point from which all active streams can be
	 * presented successfully will be closest to targetTimeStamp and within
	 * minTimeStamp/maxTimeStamp.
	 * 
	 * If flags contain SEEK_FLAG_BYTE, then all time stamps are in bytes and are
	 * the file position (this may not be supported by all demuxers). If flags
	 * contain SEEK_FLAG_FRAME, then all time stamps are in frames in the stream
	 * with streamIndex (this may not be supported by all demuxers). Otherwise all
	 * time stamps are in units of the stream selected by stream_index or if
	 * stream_index is -1, in microseconds. If flags contain SEEK_FLAG_ANY, then
	 * non-keyframes are treated as keyframes (this may not be supported by all
	 * demuxers). If flags contain SEEK_FLAG_BACKWARDS, then we will attempt to
	 * search backwards in the container (this may not be supported by all demuxers
	 * and file protocols).
	 * 
	 * This is part of the new seek API which is still under construction. It may
	 * change in future Xuggler versions.
	 * 
	 * Parameters: streamIndex - index of the stream which is used as time base
	 * reference minTimeStamp - smallest acceptable time stamp. targetTimeStamp -
	 * target time stamp. maxTimeStamp - largest acceptable time stamp. flags - A
	 * bitmask of the SEEK_FLAG_* flags, or 0 to turn all flags off. Returns: >=0 on
	 * success, error code otherwise Since: 3.4
	 * 
	 */
	@Override
	protected boolean seekMS(double timeMS) {
		// forward only? "closest"? AFTER or BEFORE?
		long timestamp = timeSecToTimeStamp(timeMS * 1000);
		if (container.seekKeyFrame(streamIndex, Long.MIN_VALUE, timestamp, Long.MAX_VALUE, IContainer.SEEK_FLAG_ANY) < 0)
			return false;		
		return true;
	}

	@Override
	protected BufferedImage getImageForMSTimePoint(double timeMS) {
		seekMS(timeMS);
		if (!loadPictureFromNextPacket()) {
			return null;
		}
		return (picture.isComplete() ? getBufferedImage() : null);
	}

	private long timeSecToTimeStamp(double timeSec) {
		return (long) (timeSec / timebase);
	}

	private boolean seekToStart() {
		// initial time stamps can be negative. See
		// https://physlets.org/tracker/library/experiments/projectile_model.zip
		return (container.seekKeyFrame(-1, Long.MIN_VALUE, 0, Long.MAX_VALUE,
				IContainer.SEEK_FLAG_BACKWARDS) >= 0);
	}

//	private void preLoadContainer() {
//		int n = 0;
//		int nIncomplete = 0;
//		while (container.readNextPacket(packet) >= 0) {
//			if (isCurrentStream()) {
//				long dts = packet.getTimeStamp();
////				if (keyTS0 == Long.MIN_VALUE)
////					keyTS0 = dts; 
//				int offset = 0;
//				int size = packet.getSize();
//				while (offset < size) {
//					// decode the packet into the picture
//					int bytesDecoded = videoDecoder.decodeVideo(picture, packet, offset);
//					// check for errors
//					if (bytesDecoded < 0)
//						break;
//					offset += bytesDecoded;
//				}
////				System.out.println(n + " : dts="  + packet.getTimeStamp() + " dts="  + packet.getDts() + " "  + packet.getFormattedTimeStamp() + " " + picture.isComplete());
//				if (picture.isComplete()) {
//					if (keyTS1 == Long.MIN_VALUE)
//						keyTS1 = dts; 
//				} else {
////					haveBFrames = true;
//					nIncomplete++;
//				}
//				n++;
//
//			}
//		}
////		if (frameCount < 0)
////			frameCount = n;
//		if (firstDisplayPacket < 0)
//			firstDisplayPacket = nIncomplete;
//	}

	private static String DEBUG_DIR = "c:/temp/tmp/";

	private void dumpImage(int i, BufferedImage bi, String froot) {
		if (DEBUG_DIR == null)
			return;
		try {
			String ii = "00" + i;
			File outputfile = new File(DEBUG_DIR + froot + ii.substring(ii.length() - 2) + ".png");
			ImageIO.write(bi, "png", outputfile);
			System.out.println("XuggleVideo " + outputfile + " created");
		} catch (IOException e) {
		}

	}

	/**
	 * Sets the initial image.
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

//	/**
//	 * Determines if a packet is a key packet.
//	 *
//	 * @param packet the packet
//	 * @return true if packet is a key in the video stream
//	 */
//	private boolean isKeyPacket() {
//		return (isCurrentStream() && packet.isKeyPacket());
//	}

	/**
	 * Determines if a packet is a video packet.
	 *
	 * @param packet the packet
	 * @return true if packet is in the video stream
	 */
	private boolean isCurrentStream() {
		return (packet.getStreamIndex() == streamIndex);
	}

	/**
	 * Loads a picture for a given key timestamp.
	 *
	 * @param keyTS the key timestamp in stream timebase units
	 * @return true if loaded successfully
	 */
	private boolean loadPictureForKeyTimeStamp(long keyTS) {
		long dts = packet.getTimeStamp();
		// if current packet, we are done;
		// positive delta means key is ahead of us
		long delta = keyTS - dts;
		if (delta == 0) {
			return true;
		}
		// if first packet, reset the container
		if (keyTS == keyTimeStamps[0]) {
			resetContainer();
			loadPictureFromNextPacket();
			return true;
		}
		// DB 5-7-2021 changed def of seekTS since for many videos this searches from
		// START
		// when stepping back, making it SUPER slow in long videos
//		long seekTS = (delta < 0 && haveBFrames ? keyTS0 : keyTS);
		long seekTS = keyTS;
		// if delta is negative, seek backwards;
		// if positive and more than a second, seek forward
		boolean doReset = ((delta < 0 || delta > packet.getTimeBase().getDenominator()) && container
				.seekKeyFrame(streamIndex, seekTS, seekTS, seekTS, delta < 0 ? IContainer.SEEK_FLAG_BACKWARDS : 0) < 0);
		// allow for a second pass with a container reset between two passes, or, if not
		// found here, a reset first and only one pass
		if (doReset)
			resetContainer();
		while (container.readNextPacket(packet) >= 0) {
			dts = packet.getTimeStamp();
			if (dts == keyTS) {
				loadPictureFromPacket();
				return true;
			}
			if (firstDisplayPacket > 0 && dts < keyTS) {
				loadPictureFromPacket();
			}
// shouldn't be possible
//			if (isCurrentStream() && dts > keyTS) {
//				// unlikely to go this far. 
//				if (nPasses++ > 1)
//					break;
//				resetContainer();
//			}
		}
		// unlikely to be possible
		return false;
	}

	/**
	 * Resets the container to the beginning.
	 */
	private void resetContainer() {
		// seek backwards--this will fail for streamed web videos
		// System.out.println("resetting container");
		if (!seekToStart()) {
			openContainer();
		}
	}

	/**
	 * Loads the Xuggle picture with all data needed to display a specified frame.
	 *
	 * @param frameNumber the Tracker frame number
	 * @return true if loaded successfully
	 */
	private BufferedImage loadPictureForFrame(int frameNumber) {
		int index = frameNumberToContainerIndex(frameNumber);
		long targetTS = packetTimeStamps[index];
		// check to see if seek is needed
		long currentTS = packet.getTimeStamp();
		long keyTS = keyTimeStamps[index];
		boolean justLoadNext = (currentTS >= keyTS && currentTS < targetTS);
		if (currentTS != targetTS || !isCurrentStream()) {
			// frame is not already loaded
			if (justLoadNext ? loadPictureFromNextPacket() : loadPictureForKeyTimeStamp(keyTS)) {
				// scan to appropriate packet
				while (isCurrentStream() && (currentTS = packet.getTimeStamp()) != targetTS) {
					loadPictureFromNextPacket();
				}
			}
		}
		System.out.println("XuggleVideo.loadPicture " + picture.isComplete() + " index=" + index + " cts=" + currentTS
				+ " firstDisplay=" + firstDisplayPacket + " codec=" + videoDecoder.getCodecID());
		return (picture.isComplete() ? getBufferedImage() : null);
	}

	private int frameNumberToContainerIndex(int n) {
		return (n + firstDisplayPacket) % packetCount;
	}

//	/**
//	 * Gets the frame number for a specified timestamp.
//	 *
//	 * @param timeStamp the timestamp in stream timebase units
//	 * @return the frame number, or -1 if not found
//	 */
//	private int getContainerFrame(long timeStamp) {
//		for (int i = 0; i < frameCount; i++) {
//			if (packetTimeStamps[i] == timeStamp)
//				return i;
//		}
//		return -1;
//	}

	/**
	 * Gets the BufferedImage for a specified Tracker video frame.
	 *
	 * @param frameNumber the Tracker frame number (zero-based)
	 * @return the image, or null if failed to load
	 */
	private BufferedImage getImage(int frameNumber) {
		if (frameNumber < 0 || frameNumber >= frameCount)
			return null;
		int index = frameNumberToContainerIndex(frameNumber);
		BufferedImage bi = (imageCache != null && index < imageCache.length ? imageCache[index] : null);
		return (bi == null ? loadPictureForFrame(frameNumber) : bi);
	}

	IVideoPicture newPic;

	private boolean fullyLoaded;

	/**
	 * Gets the BufferedImage for the current Xuggle picture.
	 *
	 * @return the image, or null if unable to resample
	 */
	private BufferedImage getBufferedImage() {
		// if needed, convert picture into BGR24 format
		if (picture.getPixelType() == IPixelFormat.Type.BGR24) {
			newPic = picture;
		} else {
			if (resampler == null) {
				resampler = IVideoResampler.make(picture.getWidth(), picture.getHeight(), IPixelFormat.Type.BGR24,
						picture.getWidth(), picture.getHeight(), picture.getPixelType());
				if (resampler == null) {
					OSPLog.warning("Could not create color space resampler"); //$NON-NLS-1$
					return null;
				}
				newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
			}
			if (resampler.resample(newPic, picture) < 0 || newPic.getPixelType() != IPixelFormat.Type.BGR24) {
				OSPLog.warning("Could not encode video as BGR24"); //$NON-NLS-1$
				return null;
			}
		}

		// use IConverter to convert picture to buffered image
		if (converter == null) {
			converter = ConverterFactory.createConverter(
					ConverterFactory.findRegisteredConverter(ConverterFactory.XUGGLER_BGR_24).getDescriptor(), newPic);
		}
		BufferedImage image = converter.toImage(newPic);
		// garbage collect to play smoothly--but slows down playback speed
		// significantly!
		if (playSmoothly)
			System.gc();
		return image;
	}

	/**
	 * Loads the next video packet in the container into the current Xuggle picture.
	 *
	 * @return true if successfully loaded
	 */
	private boolean loadPictureFromNextPacket() {
		while (container.readNextPacket(packet) >= 0) {
			if (isCurrentStream()) {
				return loadPictureFromPacket();
			}
			// should never get here
		}
		return false;
	}

	/**
	 * Loads the current video packet into the IPicture object.
	 *
	 * @param packet the packet
	 * @return true if successfully loaded
	 */
	private boolean loadPictureFromPacket() {
		int offset = 0;
		int size = packet.getSize();
		while (offset < size) {
			// decode the packet into the picture
			int bytesDecoded = videoDecoder.decodeVideo(picture, packet, offset);

			// check for errors
			if (bytesDecoded < 0)
				return false;

			offset += bytesDecoded;
			if (picture.isComplete()) {
				break;
			}
		}
//		System.out.println("loadPictureFromPacket " + picture.isComplete() + " " + packet.getFormattedTimeStamp() 
//		+ " " + packet.getTimeStamp() + " " + picture.getFormattedTimeStamp());
		return true;
	}

	@Override
	public String getTypeName() {
		return MovieFactory.ENGINE_XUGGLE;
	}

	@Override
	public int getLoadedFrameCount() {
		return index;
	}

	@Override
	public boolean isFullyLoaded() {
		return fullyLoaded;
	}

	@Override
	public int getLoadableFrameCount() {
		return endFrameNumber + 1;
	}

	@Override
	public void setLoadableFrameCount(int n) {
		endFrameNumber = n - 1;
	}

	/**
	 * Returns an XML.ObjectLoader to save and load XuggleVideo data.
	 *
	 * @return the object loader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load XuggleVideo data.
	 */
	static public class Loader extends MovieVideo.Loader {

		@Override
		protected VideoAdapter createVideo(XMLControl control, String path) throws IOException {
			XuggleVideo video = new XuggleVideo(path, control);
			setVideo(path, video, MovieFactory.ENGINE_XUGGLE);
			return video;
		}
	}

	@Override
	protected String getPlatform() {
		return PLATFORM_JAVA;
	}


}
