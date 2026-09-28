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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.opensourcephysics.media.core.VideoIO;

  /**
   * A class to create thumbnail images of videos.
   */
public class CVThumbnailTool {
	
	private static final CVThumbnailTool THUMBNAIL_TOOL = new CVThumbnailTool();
	private static final int TARGET_FRAME_NUMBER = 6;
	
	private BufferedImage thumbnail;
	private Graphics2D g;
	private boolean finished;
	private int frameNumber;
	private BufferedImage overlay;
	private Dimension dim;
  
  /**
   * "Starts" this tool so minijar will include it
   * 
   * BH: now called by XuggleVideoType creating this class using reflection.
   */
  public static void start() {
  }
  
  static {
	  start();
  }
  
  /**
   * Attempts to create a new thumbnail image.
   * @param dim the dimension of the image
   * @param pathToVideo the path to the source video file
   * @return the thumbnail image, or null if failed
   */
  public static synchronized BufferedImage createThumbnailImage(Dimension dim, String pathToVideo) {
  	THUMBNAIL_TOOL.initialize(dim);
    java.io.InputStream videoStream = null;
		try {			
			videoStream = new FileInputStream(pathToVideo);
		} catch (FileNotFoundException e) {
      System.err.println("Error: file not found: "+pathToVideo);
      return null;
		}
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoStream);
		Java2DFrameConverter converter = new Java2DFrameConverter();
  	 try {
       grabber.start();
       int frameCount = grabber.getLengthInVideoFrames();
       // Set the target frame number
       THUMBNAIL_TOOL.frameNumber = TARGET_FRAME_NUMBER >= frameCount? 0: TARGET_FRAME_NUMBER;
       grabber.setFrameNumber(THUMBNAIL_TOOL.frameNumber);      
       Frame frame = grabber.grabImage();       
       
       if (frame != null) {
           BufferedImage bi = converter.convert(frame);
           THUMBNAIL_TOOL.createThumbnailFromImage(bi);
       } else {
           System.out.println("Could not grab frame");
       }
        grabber.stop();
      
   } catch (Exception e) {
       e.printStackTrace();
   }
    return THUMBNAIL_TOOL.thumbnail;
  }
  
  /**
   * Attempts to create a new thumbnail file.
   * @param dim the dimension of the image
   * @param pathToVideo the path to the source video file 
   * @param pathToThumbnail the path to the desired thumbnail file
   * @return the thumbnail file, or null if failed
   */
  public static synchronized File createThumbnailFile(Dimension dim, String pathToVideo, String pathToThumbnail) {
  	BufferedImage thumb = createThumbnailImage(dim, pathToVideo);
    return VideoIO.writeImageFile(thumb, pathToThumbnail);
  }
  
  public BufferedImage createThumbnailFromImage(BufferedImage image) {
  	if (!isFinished()) {
      
      double widthFactor = dim.getWidth()/image.getWidth();
      double heightFactor = dim.getHeight()/image.getHeight();
      double factor = Math.min(widthFactor, heightFactor);
      
      // determine actual dimensions of thumbnail
      int w = (int)(image.getWidth()*factor);
      int h = (int)(image.getHeight()*factor);
      
  		thumbnail = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
  		g = thumbnail.createGraphics();
      AffineTransform transform = AffineTransform.getScaleInstance(factor, factor);
      g.setTransform(transform); // shrink video image
      g.drawImage(image, 0, 0, null);
      
      if (overlay!=null) {
	      g.scale(1/factor, 1/factor); // draw overlay at full scale
	      
        // determine the inset and translate the image
        Rectangle2D bounds = new Rectangle2D.Float(0, 0, overlay.getWidth(), overlay.getHeight());
        double ht = bounds.getHeight();
        g.translate(0.5*ht, thumbnail.getHeight()-1.5*ht);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.drawImage(overlay, 0, 0, null);

      }
//      frameNumber++;
//      finished = frameNumber>=TARGET_FRAME_NUMBER;
  	}
    return thumbnail;
  }
    
  private void initialize(Dimension dimension) {
  	dim = dimension;
		finished = false;
		frameNumber = 0;
//    try {
//    	String imageFile = "C:/Program Files (x86)/Tracker/tracker_icon.png";
//    	overlay = ImageIO.read(new File(imageFile));
//	  } 
//	  catch (IOException e) {
//	      e.printStackTrace();
//	      throw new RuntimeException("Could not open file");
//	  }

	}
		
  private boolean isFinished() {
		return finished;
	}
		
}
