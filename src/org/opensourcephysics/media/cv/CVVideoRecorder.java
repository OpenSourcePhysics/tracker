/*
 * The org.opensourcephysics.media.xuggle package provides Xuggle
 * services including implementations of the Video and VideoRecorder interfaces.
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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.ScratchVideoRecorder;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A class to record videos using the CV video engine.
 * The CVVideoType can record only mp4 format with codec H.264
 */
public class CVVideoRecorder extends ScratchVideoRecorder {
	
	/**
   * Constructs a CVVideoRecorder.
	 * @param type the video type
   */
  public CVVideoRecorder(CVVideoType type) {
    super(type);
  }

  /**
   * Discards the current video and resets the recorder to a ready state.
   */
	@Override
  public void reset() {
    deleteTempFiles();
    super.reset();
    scratchFile = null;
  }

  /**
   * Called by the garbage collector when this recorder is no longer in use.
   */
	@Override
  protected void finalize() {
  	reset();
  }
  
  /**
   * Appends a frame to the current video by saving the image in a tempFile.
   *
   * @param image the image to append
   * @return true if image successfully saved
   */
	@Override
	protected boolean append(Image image) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		if (dim==null || (!hasContent && (dim.width!=w || dim.height!=h))) {
			dim = new Dimension(w, h);
		}
		// resize and/or convert to BufferedImage if needed
		if (dim.width!=w || dim.height!=h || !(image instanceof BufferedImage)) {
			BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);			
			int x = (dim.width-w)/2;
			int y = (dim.height-h)/2;
			img.getGraphics().drawImage(image, x, y, null);
			image = img;
		}
		BufferedImage source = (BufferedImage)image;
		String fileName = tempFileBasePath+"_"+tempFiles.size()+".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
    try {
			ImageIO.write(source, tempFileType, new BufferedOutputStream(
			    new FileOutputStream(fileName)));
		} catch (Exception e) {
			return false;
		}
		File imageFile = new File(fileName);
		if (imageFile.exists()) {
			synchronized (tempFiles) {
				tempFiles.add(imageFile);
			}
			imageFile.deleteOnExit();
		}
		return true;
	}
	
  /**
   * Saves the video to the current scratchFile.
   * 
   * @throws IOException
   */
	@Override
	protected void saveScratch() throws IOException {
		FileFilter fileFilter	=	videoType.getDefaultFileFilter();
		if (!hasContent || !(fileFilter instanceof VideoFileFilter))
			return;
		
    try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(scratchFile.getAbsolutePath(), 
    		dim.width, dim.height); Java2DFrameConverter converter = new Java2DFrameConverter()) {
      // Configure video encoding parameters
      recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // H.264 codec
      recorder.setFormat("mp4");                        // Output container
      recorder.setFrameRate(30);                       // Frames per second
      recorder.setVideoBitrate(2000000);                // 2 Mbps bitrate (adjust for quality)
      recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // Universally playable pixel format

      // Start the recording process
      recorder.start();

    	synchronized (tempFiles) {
  			for (File imageFile: tempFiles) {
  				if (!imageFile.exists())
  					throw new IOException("temp image file not found"); //$NON-NLS-1$
  				BufferedImage image = ResourceLoader.getBufferedImage(imageFile.getAbsolutePath(), BufferedImage.TYPE_3BYTE_BGR);
  				if (image==null || image.getType()!=BufferedImage.TYPE_3BYTE_BGR) {
  					throw new IOException("unable to load temp image file"); //$NON-NLS-1$
  				}
          Frame frame = converter.convert(image);         
          // Record the frame into the video file
          recorder.record(frame); 				
  			}
  		}

  } catch (Exception e) {
      System.err.println("Error occurred during video recording.");
      e.printStackTrace();
  }

    deleteTempFiles();
		hasContent = false;
		canRecord = false;
	}

  /**
   * Starts the video recording process.
   *
   * @return true if video recording successfully started
   */
	@Override
	protected boolean startRecording() {
		try {
			tempFileBasePath = XML.stripExtension(scratchFile.getAbsolutePath());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public String getCodec() {
		return "H.264";
	}
	
}
