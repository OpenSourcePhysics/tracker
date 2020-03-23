/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.gif package provides GIF services
 * including implementations of the Video and VideoRecorder interfaces.
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
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.gif;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.opensourcephysics.media.core.ScratchVideoRecorder;

/**
 * This is a gif video recorder that uses scratch files.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class GifVideoRecorder extends ScratchVideoRecorder {
  // instance fields
  private AnimatedGifEncoder encoder = new AnimatedGifEncoder();

  /**
   * Constructs a GifVideoRecorder object.
   */
  public GifVideoRecorder() {
    super(new GifVideoType());
  }

  /**
   * Sets the time duration per frame.
   *
   * @param millis the duration per frame in milliseconds
   */
  public void setFrameDuration(double millis) {
    super.setFrameDuration(millis);
    encoder.setDelay((int) frameDuration);
  }

  /**
   * Gets the encoder used by this recorder. The encoder has methods for
   * setting a transparent color, setting a repeat count (0 for continuous play),
   * and setting a quality factor.
   *
   * @return the gif encoder
   */
  public AnimatedGifEncoder getGifEncoder() {
    return encoder;
  }

  //________________________________ protected methods _________________________________

  /**
   * Saves the video to the current scratchFile.
   */
  protected void saveScratch() {
    encoder.finish();
  }

  /**
   * Starts the video recording process.
   *
   * @return true if video recording successfully started
   */
  protected boolean startRecording() {
    if((dim==null)&&(frameImage!=null)) {
      dim = new Dimension(frameImage.getWidth(null), frameImage.getHeight(null));
    }
    if(dim!=null) {
      encoder.setSize(dim.width, dim.height);
    }
    encoder.setRepeat(0);
    return encoder.start(scratchFile.getAbsolutePath());
  }

  /**
   * Appends a frame to the current video.
   *
   * @param image the image to append
   * @return true if image successfully appended
   */
  protected boolean append(Image image) {
    BufferedImage bi;
    if(image instanceof BufferedImage) {
      bi = (BufferedImage) image;
    } 
    else {
      if(dim==null) {
        return false;
      }
      bi = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = bi.createGraphics();
      g.drawImage(image, 0, 0, null);
    }
    encoder.addFrame(bi);
    return true;
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
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
