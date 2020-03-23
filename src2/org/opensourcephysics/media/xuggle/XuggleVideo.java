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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.DoubleArray;
import org.opensourcephysics.media.core.Filter;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.VideoAdapter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

/**
 * A class to display videos using the Xuggle library. Xuggle in turn
 * uses FFMpeg as its video engine.
 */
public class XuggleVideo extends VideoAdapter {
	
	IContainer container;
  int streamIndex = -1;
  IStreamCoder videoCoder;
  IVideoResampler resampler;
  IPacket packet;
  IVideoPicture picture;
  IStream stream;
  IRational timebase;
  IConverter converter;
  // maps frame number to timestamp of displayed packet (last packet loaded)
  Map<Integer, Long> frameTimeStamps = new HashMap<Integer, Long>();
  // maps frame number to timestamp of key packet (first packet loaded)
  Map<Integer, Long> keyTimeStamps = new HashMap<Integer, Long>();
  // array of frame start times in milliseconds
  private double[] startTimes;
  private long systemStartPlayTime;
  private double frameStartPlayTime;
  private boolean playSmoothly = true;
  private int frame, prevFrame;
  private Timer failDetectTimer;
	
  /**
   * Creates a XuggleVideo and loads a video file specified by name
   *
   * @param fileName the name of the video file
   * @throws IOException
   */
  public XuggleVideo(final String fileName) throws IOException {
    Frame[] frames = Frame.getFrames();
    for(int i = 0, n = frames.length; i<n; i++) {
       if (frames[i].getName().equals("Tracker")) { //$NON-NLS-1$
      	 addPropertyChangeListener("progress", (PropertyChangeListener)frames[i]); //$NON-NLS-1$
      	 addPropertyChangeListener("stalled", (PropertyChangeListener)frames[i]); //$NON-NLS-1$
      	 break;
       }
    }
    // timer to detect failures
		failDetectTimer = new Timer(6000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (frame==prevFrame) {
      		firePropertyChange("stalled", null, fileName); //$NON-NLS-1$
      		failDetectTimer.stop();
      	}
      	prevFrame = frame;
      }
    });
		failDetectTimer.setRepeats(true);
    load(fileName);
  }

  /**
   * Plays the video at the current rate. Overrides VideoAdapter method.
   */
  public void play() {
    if(getFrameCount()==1) {
      return;
    }
    int n = getFrameNumber()+1;
    playing = true;
	  support.firePropertyChange("playing", null, new Boolean(true)); //$NON-NLS-1$
	  startPlayingAtFrame(n);
  }

  /**
   * Stops the video.
   */
  public void stop() {
  	playing = false;
    support.firePropertyChange("playing", null, new Boolean(false)); //$NON-NLS-1$
  }

  /**
   * Sets the frame number. Overrides VideoAdapter setFrameNumber method.
   *
   * @param n the desired frame number
   */
  public void setFrameNumber(int n) {
  	if (n==getFrameNumber()) return;
    super.setFrameNumber(n);
    BufferedImage bi = getImage(getFrameNumber());
    if (bi!=null) {
	    rawImage = bi;
	    isValidImage = false;
	    isValidFilteredImage = false;
	    firePropertyChange("framenumber", null, new Integer(getFrameNumber())); //$NON-NLS-1$
	    if (isPlaying()) {
	      Runnable runner = new Runnable() {
	      	public void run() {
	  	    	continuePlaying();
	      	}
	      };
	      SwingUtilities.invokeLater(runner);
	    }
    }
  }
  
  /**
   * Gets the start time of the specified frame in milliseconds.
   *
   * @param n the frame number
   * @return the start time of the frame in milliseconds, or -1 if not known
   */
  public double getFrameTime(int n) {
    if((n>=startTimes.length)||(n<0)) {
      return -1;
    }
    return startTimes[n];
  }

  /**
   * Gets the current frame time in milliseconds.
   *
   * @return the current time in milliseconds, or -1 if not known
   */
  public double getTime() {
    return getFrameTime(getFrameNumber());
  }

  /**
   * Sets the frame number to (nearly) a desired time in milliseconds.
   *
   * @param millis the desired time in milliseconds
   */
  public void setTime(double millis) {
    millis = Math.abs(millis);
    for(int i = 0; i<startTimes.length; i++) {
      double t = startTimes[i];
      if(millis<t) { // find first frame with later start time
        setFrameNumber(i-1);
        break;
      }
    }
  }

  /**
   * Gets the start frame time in milliseconds.
   *
   * @return the start time in milliseconds, or -1 if not known
   */
  public double getStartTime() {
    return getFrameTime(getStartFrameNumber());
  }

  /**
   * Sets the start frame to (nearly) a desired time in milliseconds.
   *
   * @param millis the desired start time in milliseconds
   */
  public void setStartTime(double millis) {
    millis = Math.abs(millis);
    for(int i = 0; i<startTimes.length; i++) {
      double t = startTimes[i];
      if(millis<t) { // find first frame with later start time
        setStartFrameNumber(i-1);
        break;
      }
    }
  }

  /**
   * Gets the end frame time in milliseconds.
   *
   * @return the end time in milliseconds, or -1 if not known
   */
  public double getEndTime() {
    int n = getEndFrameNumber();
    if (n<getFrameCount()-1)
    	return getFrameTime(n+1);
    return getDuration();
  }

  /**
   * Sets the end frame to (nearly) a desired time in milliseconds.
   *
   * @param millis the desired end time in milliseconds
   */
  public void setEndTime(double millis) {
    millis = Math.abs(millis);
    millis = Math.min(getDuration(), millis);
    for(int i = 0; i<startTimes.length; i++) {
      double t = startTimes[i];
      if(millis<t) { // find first frame with later start time
        setEndFrameNumber(i-1);
        break;
      }
    }
  }

  /**
   * Gets the duration of the video.
   *
   * @return the duration of the video in milliseconds, or -1 if not known
   */
  public double getDuration() {
    int n = getFrameCount()-1;
    if (n==0) return 100; // arbitrary duration for single-frame video!
    // assume last and next-to-last frames have same duration
    double delta = getFrameTime(n)-getFrameTime(n-1);
    return getFrameTime(n)+delta;
  }

  /**
   * Sets the relative play rate. Overrides VideoAdapter method.
   *
   * @param rate the relative play rate.
   */
  public void setRate(double rate) {
  	super.setRate(rate);
  	if (isPlaying()) {
  		startPlayingAtFrame(getFrameNumber());
  	}
  }

  /**
   * Disposes of this video.
   */
  public void dispose() {
    super.dispose();
    if (videoCoder!=null) {
			videoCoder.close();
			videoCoder.delete();
			videoCoder = null;
    }
    if (stream!=null) {
			stream.delete();
			stream = null;
    }
		if (picture!=null) {
			picture.delete();
			picture = null;
			packet.delete();
			packet = null;
		}
    if (container!=null) {
			container.close();
			container.delete();
			container = null;
	  }
  }
  
  /**
   * Sets the playSmoothly flag.
   * 
   * @param smooth true to play smoothly
   */
  public void setSmoothPlay(boolean smooth) {
  	playSmoothly = smooth;
  }
  
  /**
   * Gets the playSmoothly flag.
   * 
   * @return true if playing smoothly
   */
  public boolean isSmoothPlay() {
  	return playSmoothly;
  }

//______________________________  private methods _________________________

  /**
   * Sets the system and frame start times.
   * 
   * @param frameNumber the frame number at which playing will start 
   */
  private void startPlayingAtFrame(int frameNumber) {
    // systemStartPlayTime is the system time when play starts
    systemStartPlayTime = System.currentTimeMillis();
    // frameStartPlayTime is the frame time where play starts
    frameStartPlayTime = getFrameTime(frameNumber);
    setFrameNumber(frameNumber);
  }

  /**
   * Plays the next time-appropriate frame at the current rate.
   */
  private void continuePlaying() {
  	int n = getFrameNumber();
  	if (n<getEndFrameNumber()) {
    	long elapsedTime = System.currentTimeMillis()-systemStartPlayTime;
    	double frameTime = frameStartPlayTime+getRate()*elapsedTime;
    	int frameToPlay = getFrameNumberBefore(frameTime);
    	while (frameToPlay>-1 && frameToPlay<=n) {
	    	elapsedTime = System.currentTimeMillis()-systemStartPlayTime;
	    	frameTime = frameStartPlayTime+getRate()*elapsedTime;
	    	frameToPlay = getFrameNumberBefore(frameTime);
    	}
    	if (frameToPlay==-1) {
    		frameToPlay = getEndFrameNumber();
    	}
    	setFrameNumber(frameToPlay);
    } 
  	else if(looping) {
  		startPlayingAtFrame(getStartFrameNumber());
    } 
  	else {
      stop();
    }  	
  }

  /**
   * Gets the number of the last frame before the specified time.
   *
   * @param time the time in milliseconds
   * @return the frame number, or -1 if not found
   */
	private int getFrameNumberBefore(double time) {
		for (int i = 0; i < startTimes.length; i++) {
			if (time < startTimes[i])
				return i-1;
		}
		// if not found, see if specified time falls in last frame
		int n = startTimes.length-1;
    // assume last and next-to-last frames have same duration
    double endTime = 2*startTimes[n]-startTimes[n-1];
		if (time < endTime)
			return n;
		return -1;
	}
	
  /**
   * Loads a video specified by name.
   *
   * @param fileName the video file name
   * @throws IOException
   */
	@SuppressWarnings("deprecation")
	private void load(String fileName) throws IOException {
    Resource res = ResourceLoader.getResource(fileName);
    if (res==null) {
    	throw new IOException("unable to create resource for "+fileName); //$NON-NLS-1$
    }    
    // create and open a Xuggle container
    URL url = res.getURL();
    boolean isLocal = url.getProtocol().toLowerCase().indexOf("file")>-1; //$NON-NLS-1$
    String path = isLocal? res.getAbsolutePath(): url.toExternalForm();
    OSPLog.finest("Xuggle video loading "+path+" local?: "+isLocal); //$NON-NLS-1$ //$NON-NLS-2$
  	container = IContainer.make();
  	if (isLocal) {  // random access file handles non-ascii unicode characters
	  	RandomAccessFile raf = new RandomAccessFile(path, "r"); //$NON-NLS-1$
	    if (container.open(raf, IContainer.Type.READ, null) < 0) {
				dispose();
	      throw new IOException("unable to open "+fileName); //$NON-NLS-1$
	    }
  	}
  	else if (container.open(path, IContainer.Type.READ, null) < 0) {
			dispose();
	    throw new IOException("unable to open "+fileName); //$NON-NLS-1$  		
  	}
    
    // find the first video stream in the container
    for (int i = 0; i < container.getNumStreams(); i++) {
      IStream nextStream = container.getStream(i);
      // get the pre-configured decoder that can decode this stream
      IStreamCoder coder = nextStream.getStreamCoder();      
      // get the type of stream from the coder's codec type
      if (coder.getCodecType().equals(ICodec.Type.CODEC_TYPE_VIDEO)) {
      	stream = nextStream;
      	streamIndex = i;
      	videoCoder = coder;
      	timebase = stream.getTimeBase().copy();
      	break;
      }      
    }
    
    // check that a video stream was found
    if (streamIndex == -1) {
			dispose();
      throw new IOException("no video stream found in "+fileName); //$NON-NLS-1$
    }
    
    // check that coder opens
    if (videoCoder.open() < 0) {
			dispose();
      throw new IOException("unable to open video decoder for "+fileName); //$NON-NLS-1$
    }
    
    // set properties
    setProperty("name", XML.getName(fileName));         //$NON-NLS-1$
    setProperty("absolutePath", res.getAbsolutePath());              //$NON-NLS-1$
    if(fileName.indexOf(":")==-1) {                       //$NON-NLS-1$
	    // if name is relative, path is name
      setProperty("path", XML.forwardSlash(fileName));    //$NON-NLS-1$
    } else {
      // else path is relative to user directory
      setProperty("path", XML.getRelativePath(fileName)); //$NON-NLS-1$
    }

    // set up frame data using temporary container
		IContainer tempContainer = IContainer.make();   
  	if (isLocal) {
	  	RandomAccessFile tempRaf = new RandomAccessFile(path, "r"); //$NON-NLS-1$
	    tempContainer.open(tempRaf, IContainer.Type.READ, null);
  	}
  	else {
	    tempContainer.open(container.getURL(), IContainer.Type.READ, null); 		
  	}
    IStream tempStream = tempContainer.getStream(streamIndex);
    IStreamCoder tempCoder = tempStream.getStreamCoder();
    tempCoder.open();
    
    IVideoPicture tempPicture = IVideoPicture.make(tempCoder.getPixelType(),
    		tempCoder.getWidth(), tempCoder.getHeight());
    IPacket tempPacket = IPacket.make();
		long keyTimeStamp = Long.MIN_VALUE;
	  long startTimeStamp = Long.MIN_VALUE;
		ArrayList<Double> seconds = new ArrayList<Double>();
		firePropertyChange("progress", fileName, 0); //$NON-NLS-1$
		frame = prevFrame = 0;		
		failDetectTimer.start();		
		// step thru container and find all video frames
		while (tempContainer.readNextPacket(tempPacket)>=0) {
			if (VideoIO.isCanceled()) {
				failDetectTimer.stop();		
				firePropertyChange("progress", fileName, null); //$NON-NLS-1$
				// clean up temporary objects
				tempCoder.close();
				tempCoder.delete();
				tempStream.delete();
				tempPicture.delete();
				tempPacket.delete();
				tempContainer.close();
				tempContainer.delete();
				dispose();
				throw new IOException("Canceled by user"); //$NON-NLS-1$
			}
			if (isVideoPacket(tempPacket)) {
				if (keyTimeStamp == Long.MIN_VALUE || tempPacket.isKeyPacket()) {
					keyTimeStamp = tempPacket.getTimeStamp();
				}
				int offset = 0;
		    while(offset < tempPacket.getSize()) {
		      // decode the packet into the picture
		      int bytesDecoded = tempCoder.decodeVideo(tempPicture, tempPacket, offset);
		      // check for errors
		      if (bytesDecoded < 0)
		      	break;
		      offset += bytesDecoded;
		      if (tempPicture.isComplete()) {
						if (startTimeStamp == Long.MIN_VALUE) {
							startTimeStamp = tempPacket.getTimeStamp();
						}
						frameTimeStamps.put(frame, tempPacket.getTimeStamp());
						seconds.add(Double.valueOf(tempPacket.getTimeStamp()-startTimeStamp)*timebase.getValue());
						keyTimeStamps.put(frame, keyTimeStamp);
						firePropertyChange("progress", fileName, frame); //$NON-NLS-1$
						frame++;
		      }
		    }
			}
		}
		// clean up temporary objects
		tempCoder.close();
		tempCoder.delete();
		tempStream.delete();
		tempPicture.delete();
		tempPacket.delete();
		tempContainer.close();
		tempContainer.delete();
		
		// throw IOException if no frames were loaded
		if (frameTimeStamps.size()==0) {
			firePropertyChange("progress", fileName, null); //$NON-NLS-1$
			failDetectTimer.stop();		
			dispose();
			throw new IOException("packets loaded but no complete picture"); //$NON-NLS-1$
		}

		// set initial video clip properties
    frameCount = frameTimeStamps.size();
    startFrameNumber = 0;
    endFrameNumber = frameCount-1;
    // create startTimes array
    startTimes = new double[frameCount];
    startTimes[0] = 0;
    for(int i = 1; i<startTimes.length; i++) {
      startTimes[i] = seconds.get(i)*1000;
    }

    // initialize packet, picture and image
    picture = IVideoPicture.make(videoCoder.getPixelType(),
        videoCoder.getWidth(), videoCoder.getHeight());
  	packet = IPacket.make();
		loadNextPacket();
		BufferedImage img = getImage(0);
		if (img==null) {
			for (int i=1; i<frameTimeStamps.size(); i++) {
				img = getImage(i);
				if (img!=null) break;
			}
		}
		firePropertyChange("progress", fileName, null); //$NON-NLS-1$
		failDetectTimer.stop();		
		if (img==null) {
			dispose();
			throw new IOException("No images"); //$NON-NLS-1$
		}
		setImage(img);
  }
  
  /**
   * Reloads the current video.
   *
   * @throws IOException
   */
  @SuppressWarnings("deprecation")
	private void reload() throws IOException {
  	String url = container.getURL();
  	container.close();
		videoCoder.close();
		videoCoder.delete();		
		stream.delete();
    boolean isLocal = url.toLowerCase().indexOf("file:")>-1; //$NON-NLS-1$
    String path = isLocal? ResourceLoader.getNonURIPath(url): url;
  	container = IContainer.make();
  	if (isLocal) {
	  	RandomAccessFile raf = new RandomAccessFile(path, "r"); //$NON-NLS-1$
	    container.open(raf, IContainer.Type.READ, null);
  	}
  	else {
	    container.open(path, IContainer.Type.READ, null);  		
  	}    
  	stream = container.getStream(streamIndex);
  	videoCoder = stream.getStreamCoder();  
    videoCoder.open();
  }

  /**
   * Sets the initial image.
   *
   * @param image the image
   */
  private void setImage(BufferedImage image) {
    rawImage = image;
    size = new Dimension(image.getWidth(), image.getHeight());
    refreshBufferedImage();
    // create coordinate system and relativeAspects
    coords = new ImageCoordSystem(frameCount);
    coords.addPropertyChangeListener(this);
    aspects = new DoubleArray(frameCount, 1);
  }

  /**
   * Determines if a packet is a key packet.
   *
   * @param packet the packet
   * @return true if packet is a key in the video stream
   */
  private boolean isKeyPacket(IPacket packet) {
		if (isVideoPacket(packet)
				&& packet.isKeyPacket()) {
			return true;
		} 
  	return false;
  }
  
  /**
   * Determines if a packet is a video packet.
   *
   * @param packet the packet
   * @return true if packet is in the video stream
   */
  private boolean isVideoPacket(IPacket packet) {
		if (packet.getStreamIndex() == streamIndex) {
			return true;
		} 
  	return false;
  }
  
  /**
   * Returns the key packet with the specified timestamp.
   *
   * @param timestamp the timestamp in stream timebase units
   * @return the packet, or null if none found
   */
  private IPacket getKeyPacket(long timestamp) {
  	// compare requested timestamp with current packet
  	long delta = timestamp-packet.getTimeStamp();
  	// if delta is zero, return packet
  	if (delta==0) {
  		return packet;
  	}
  	// if delta is positive and short, step forward
  	IRational timebase = packet.getTimeBase();
  	int shortTime = timebase.getDenominator(); // one second
  	if (delta>0 && delta<shortTime) {
  		while (container.readNextPacket(packet)>=0) {
  			if (isKeyPacket(packet)
  					&& packet.getTimeStamp() == timestamp) {
  				return packet;
  			}
  			if (isVideoPacket(packet) && packet.getTimeStamp()>timestamp) {
  				delta = timestamp-packet.getTimeStamp();
  				break;
  			}
  		}
  	}
  	// if delta is positive and long, seek forward
  	if (delta>0 && container.seekKeyFrame(streamIndex,
  			timestamp, timestamp, timestamp, 0)>=0) {
  		while (container.readNextPacket(packet)>=0) {
  			if (isKeyPacket(packet)
  					&& packet.getTimeStamp() == timestamp) {
  				return packet;
  			} 
  			if (isVideoPacket(packet) && packet.getTimeStamp()>timestamp) {
  				delta = timestamp-packet.getTimeStamp();
  				break;
  			}
      }  		
  	}
  	// if delta is negative, seek backward
  	if (getFrameNumber(timestamp)==0) {
  		resetContainer();
  		return packet;
  	}
  	if (delta<0 && container.seekKeyFrame(streamIndex,
  			timestamp, timestamp, timestamp, IContainer.SEEK_FLAG_BACKWARDS)>=0) {
  		while (container.readNextPacket(packet)>=0) {
  			if (isKeyPacket(packet) && isVideoPacket(packet)
  					&& packet.getTimeStamp() == timestamp) {
  				return packet;
  			} 
  			if (isVideoPacket(packet) && packet.getTimeStamp()>timestamp) {
  				delta = timestamp-packet.getTimeStamp();
  				break;
  			}
      }  		
  	}
  	 
  	// if all else fails, reopen container and step forward
  	resetContainer();
		while (container.readNextPacket(packet)>=0) {
			if (isKeyPacket(packet)
					&& packet.getTimeStamp() == timestamp) {
				return packet;
			} 
			if (isVideoPacket(packet) && packet.getTimeStamp()>timestamp) {
				break;
			}
    }  		
  	
  	// if still not found, return null
  	return null;
  }

  /**
   * Gets the key packet needed to display a specified frame.
   *
   * @param frameNumber the frame number
   * @return the packet, or null if none found
   */
	private IPacket getKeyPacketForFrame(int frameNumber) {
		long keyTimeStamp = keyTimeStamps.get(frameNumber);
		return getKeyPacket(keyTimeStamp);
	}
	
  /**
   * Loads the Xuggle picture with all data needed to display a specified frame.
   *
   * @param frameNumber the frame number to load
   * @return true if loaded successfully
   */
	private boolean loadPicture(int frameNumber) {
		// check to see if seek is needed
		long currentTS = packet.getTimeStamp();
		long targetTS = getTimeStamp(frameNumber);
		long keyTS = keyTimeStamps.get(frameNumber);
		if (currentTS==targetTS && isVideoPacket(packet)) {
			// frame is already loaded
			return picture.isComplete();
		}
		if (currentTS>=keyTS && currentTS<targetTS) {
			// no need to seek--just step forward
			if (loadNextPacket()) {
				int n = getFrameNumber(packet);
				while (n > -2 && n < frameNumber) {
					if (loadNextPacket()) {
						n = getFrameNumber(packet);
					}
					else return false;
				}
			}
			else return false;
		}
		else if (getKeyPacketForFrame(frameNumber)!=null) {
			if (loadPacket(packet)) {
				int n = getFrameNumber(packet);
				while (n > -2 && n < frameNumber) {
					if (loadNextPacket()) {
						n = getFrameNumber(packet);
					}
					else return false;
				}
			}
			else return false;
		}
		return picture.isComplete();
	}
	
  /**
   * Gets the timestamp for a specified frame.
   *
   * @param frameNumber the frame number
   * @return the timestamp in stream timebase units
   */
	private long getTimeStamp(int frameNumber) {
		return frameTimeStamps.get(frameNumber);
	}

  /**
   * Gets the frame number for a specified timestamp.
   *
   * @param timeStamp the timestamp in stream timebase units
   * @return the frame number, or -1 if not found
   */
	private int getFrameNumber(long timeStamp) {
		for (int i = 0; i < frameTimeStamps.size(); i++) {
			long ts = frameTimeStamps.get(i);
			if (ts == timeStamp)
				return i;
		}
		return -1;
	}
	
  /**
   * Gets the frame number for a specified packet.
   *
   * @param packet the packet
   * @return the frame number, or -2 if not a video packet
   */
	private int getFrameNumber(IPacket packet) {
		if (packet.getStreamIndex() != streamIndex)
			return -2;
		return getFrameNumber(packet.getTimeStamp());
	}
	
  /**
   * Gets the BufferedImage for a specified frame.
   *
   * @param frameNumber the frame number
   * @return the image, or null if failed to load
   */
	private BufferedImage getImage(int frameNumber) {
		if (frameNumber<0 || frameNumber>=frameTimeStamps.size()) {
			return null;
		}
		if (loadPicture(frameNumber)) {
	    // convert picture to buffered image and display
	  	return getBufferedImage(picture);
		}
		return null;
	}
	
  /**
   * Gets the BufferedImage for a specified Xuggle picture.
   *
   * @param picture the picture
   * @return the image, or null if unable to resample
   */
	private BufferedImage getBufferedImage(IVideoPicture picture) {
    // if needed, convert picture into BGR24 format
		if (picture.getPixelType() != IPixelFormat.Type.BGR24) {
			if (resampler == null) {
	      resampler = IVideoResampler.make(
	      		picture.getWidth(), picture.getHeight(), IPixelFormat.Type.BGR24,
	      		picture.getWidth(), picture.getHeight(), picture.getPixelType());
	      if (resampler == null) {
	      	OSPLog.warning("Could not create color space resampler"); //$NON-NLS-1$
	      	return null;
	      }
			}
      IVideoPicture newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),
          picture.getWidth(), picture.getHeight());
      if (resampler.resample(newPic, picture) < 0
      		|| newPic.getPixelType() != IPixelFormat.Type.BGR24) {
      	OSPLog.warning("Could not encode video as BGR24"); //$NON-NLS-1$
      	return null;
      }
      picture = newPic;
		}

		// use IConverter to convert picture to buffered image
		if (converter==null) {
			ConverterFactory.Type type = ConverterFactory.findRegisteredConverter(
					ConverterFactory.XUGGLER_BGR_24);	  	
			converter = ConverterFactory.createConverter(type.getDescriptor(), picture);			
		}
		BufferedImage image = converter.toImage(picture);
		// garbage collect to play smoothly--but slows down playback speed significantly!
		if (playSmoothly)
			System.gc();
  	return image;		
	}

  /**
   * Loads the next video packet in the container into the current Xuggle picture.
   *
   * @return true if successfully loaded
   */
	private boolean loadNextPacket() {
		while (container.readNextPacket(packet)>=0) {
			if (isVideoPacket(packet)) {
//				long timeStamp = packet.getTimeStamp();
//				System.out.println("loading next packet at "+timeStamp+": "+packet.getSize());
				return loadPacket(packet);
			}
    }
		return false;	
	}

  /**
   * Loads a video packet into the current Xuggle picture.
   *
   * @param packet the packet
   * @return true if successfully loaded
   */
	private boolean loadPacket(IPacket packet) {
		int offset = 0;
		int size = packet.getSize();
    while(offset < size) {
      // decode the packet into the picture
      int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
      // check for errors
      if (bytesDecoded < 0)
        return false;
      
      offset += bytesDecoded;
      if (picture.isComplete()) {
      	return true;
      }
    }
    return true;
	}

  /**
   * Resets the container to the beginning.
   */
  private void resetContainer() {
    // seek backwards--this will fail for streamed web videos
    if (container.seekKeyFrame(-1, // stream index -1 ==> seek to microseconds
        Long.MIN_VALUE, 0, Long.MAX_VALUE,
        IContainer.SEEK_FLAG_BACKWARDS)>=0) {
    	loadNextPacket();
    }
    else {
	    try {
				reload();
				loadNextPacket();
			} catch (IOException e) {
		  	OSPLog.warning("Container could not be reset"); //$NON-NLS-1$   	
			}
    }
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
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves XuggleVideo data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the XuggleVideo object to save
     */
    public void saveObject(XMLControl control, Object obj) {
    	XuggleVideo video = (XuggleVideo) obj;
      String base = (String) video.getProperty("base");            //$NON-NLS-1$
      String absPath = (String) video.getProperty("absolutePath"); //$NON-NLS-1$
      control.setValue("path", XML.getPathRelativeTo(absPath, base)); //$NON-NLS-1$
      if(!video.getFilterStack().isEmpty()) {
        control.setValue("filters", video.getFilterStack().getFilters()); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new XuggleVideo.
     *
     * @param control the control
     * @return the new XuggleVideo
     */
    public Object createObject(XMLControl control) {
      try {
      	String path = control.getString("path"); //$NON-NLS-1$
      	String ext = XML.getExtension(path);
      	XuggleVideo video = new XuggleVideo(path);
        VideoType xuggleType = VideoIO.getVideoType(VideoIO.ENGINE_XUGGLE, ext);
        if (xuggleType!=null)
        	video.setProperty("video_type", xuggleType); //$NON-NLS-1$
        return video;
      } catch(IOException ex) {
      	OSPLog.fine(ex.getMessage());
        return null;
      }
    }

    /**
     * Loads properties into a XuggleVideo.
     *
     * @param control the control
     * @param obj the XuggleVideo object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	XuggleVideo video = (XuggleVideo) obj;
      Collection<?> filters = (Collection<?>) control.getObject("filters"); //$NON-NLS-1$
      if(filters!=null) {
        video.getFilterStack().clear();
        Iterator<?> it = filters.iterator();
        while(it.hasNext()) {
          Filter filter = (Filter) it.next();
          video.getFilterStack().addFilter(filter);
        }
      }
      return obj;
    }
  }

}
