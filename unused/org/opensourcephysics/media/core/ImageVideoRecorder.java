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
package org.opensourcephysics.media.core;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is an image video recorder that uses scratch files.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ImageVideoRecorder extends ScratchVideoRecorder {
  // instance fields
  protected int frameCount;
	private String tempFileBasePath;
	private String tempFileType = "png"; //$NON-NLS-1$
	private String[] savedFilePaths;

  /**
   * Constructs a default ImageVideoRecorder object.
   */
  public ImageVideoRecorder() {
    super(new ImageVideoType());
  }

  /**
   * Constructs a ImageVideoRecorder object for a specific image type.
   * @param type the image type
   */
  public ImageVideoRecorder(ImageVideoType type) {
    super(type);
    String ext = type.getDefaultExtension();
    if (ext!=null)
    	tempFileType = ext;
  }

  /**
   * Gets the video.
   *
   * @return the video
   * @throws IOException
   */
  public Video getVideo() throws IOException {
    if (saveFile!=null) {
	    if (!isSaved) saveScratch();
	    if (savedFilePaths!=null && savedFilePaths.length>0) {
	    	ImageVideo video = new ImageVideo(savedFilePaths[0], savedFilePaths.length>1);    		
		    video.setFrameDuration(frameDuration);
		    return video;
		}	    
    }
    return null;
  }

  /**
   * Saves all video images to a numbered sequence of files.
   *
   * @param fileName the file name basis for images
   * @return the full path of the first image in the sequence
   * @throws IOException
   */
  public String saveVideo(String fileName) throws IOException {
    if(fileName==null) {
      return saveVideoAs();
    }
    setFileName(fileName);
    if(saveFile==null) {
      throw new IOException("Read-only file"); //$NON-NLS-1$
    }
    saveScratch();
    return (savedFilePaths==null || savedFilePaths.length==0)? 
    		null : savedFilePaths[0];
  }
  
  /**
   * Sets the expected frame count.
   *
   * @param n the expected frame count
   */
  public void setExpectedFrameCount(int n) {
  	frameCount = n;
  }

  /**
   * Discards the current video and resets the recorder to a ready state.
   */
  @Override
  public void reset() {
  	frameCount = 0;
    deleteTempFiles();
    super.reset();
  }

  /**
   * Called by the garbage collector when this recorder is no longer in use.
   */
	@Override
  protected void finalize() {
  	reset();
  }
  

  //________________________________ protected methods _________________________________

  /**
   * Required by ScratchVideoRecorder, but unused.
   */
  protected void saveScratch() throws IOException {
		if (!hasContent) return;
		// if chooser was used, check the fileFilter for video type
		if (chosenExtension!=null && !(chooser.getFileFilter() instanceof VideoFileFilter))
			return;
		
		// copy temp files or open and re-encode if needed
		synchronized (tempFiles) {
			String fileName = saveFile.getAbsolutePath();
			savedFilePaths = getFileNames(fileName, tempFiles.size());
			for (int i = 0; i < tempFiles.size(); i++) {
				String path = savedFilePaths[i];
				File tempFile = tempFiles.get(i);
				if (!tempFile.exists()) {
					savedFilePaths = null;
					throw new IOException("temp image file not found"); //$NON-NLS-1$
				}
				if (ext==tempFileType) {
					// copy images
					File targetFile = new File(path);
					VideoIO.copyFile(tempFile, targetFile);
				}
				else {
					// open and encode images in desired format
					BufferedImage image = ResourceLoader.getBufferedImage(tempFile.getAbsolutePath());
					if (image==null) {
						throw new IOException("unable to load temp image file"); //$NON-NLS-1$
					}
		      javax.imageio.ImageIO.write(image, ext, 
		      		new BufferedOutputStream(new FileOutputStream(path)));
				}
			}
		}
		deleteTempFiles();
	    isSaved = true;
		hasContent = false;
		canRecord = false;
		
	    if (savedFilePaths!=null && savedFilePaths.length>0) {		
			// save xml description of the video (for frame duration)
			Video video = getVideo();
			XMLControl control = new XMLControlElement(video);
			String fileName = savedFilePaths[0];
			fileName = XML.stripExtension(fileName)+".xml"; //$NON-NLS-1$
			control.write(fileName);
	    }
  }

  /**
   * Starts the video recording process.
   *
   * @return true if video recording successfully started
   */
  protected boolean startRecording() {
    if(dim==null) {
      if(frameImage!=null) {
        dim = new Dimension(frameImage.getWidth(null), frameImage.getHeight(null));
      } else {
        return false;
      }
    }
		try {
			tempFileBasePath = XML.stripExtension(scratchFile.getAbsolutePath());
		} catch (Exception e) {
			return false;
		}
    return true;
  }

//  /**
//   * Appends a frame to the current video. Note: this creates a new
//   * BufferedImage each time a frame is appended and can use lots of
//   * memory in a hurry.
//   *
//   * @param image the image to append
//   * @return true if image successfully appended
//   */
//  protected boolean append(Image image) {
//    int w = image.getWidth(null);
//    int h = image.getHeight(null);
//    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//    Graphics2D g = bi.createGraphics();
//    g.drawImage(image, 0, 0, null);
//    images.add(bi);
//    return true;
//  }
  /**
   * Appends a frame to the current video by saving the image in a tempFile.
   *
   * @param image the image to append
   * @return true if image successfully appended
   */
	@Override
	protected boolean append(Image image) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		if (dim==null) {
			dim = new Dimension(w, h);
		}
		// can't append images that are different size than first
		if (dim.width!=w || dim.height!=h)
			return false;
		// convert to BufferedImage if needed
		if (!(image instanceof BufferedImage)) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);			
			img.getGraphics().drawImage(image, 0, 0, null);
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
   * Return the file that will be saved if the specified file is selected.
   * This is needed by ImageVideoRecorder since it strips and/or appends digits
   * to the selected file name.
   *
   * @param file the file selected with the chooser
   * @return the file (or first file) to be saved
   */
  protected File getFileToBeSaved(File file) {
    // determine number of digits to append to file names
    int n = frameCount>0? frameCount: tempFiles.size();
    // if single or no image, return file itself
    if(n<=1) {
      return file;
    }
    String fileName = file.getAbsolutePath();
    // get appended number
    int i = getAppendedNumber(fileName);
    // get base
    String base = getBase(fileName);
    if(i>0) {
      fileName = base+((n+i<10) ? String.valueOf(i) : ((n+i<100)&&(i<10)) ? "0"+i : ( //$NON-NLS-1$
        n+i<100) ? String.valueOf(i) : ((n+i<1000)&&(i<10)) ? "00"+i : ((             //$NON-NLS-1$
          n+i<1000)&&(i<100)) ? "0"+i : (                                             //$NON-NLS-1$
            n+i<1000) ? String.valueOf(i) : (i<10) ? "000"+i : (                      //$NON-NLS-1$
              i<100) ? "00"+i : (                                                     //$NON-NLS-1$
                i<1000) ? "0"+i :                                                     //$NON-NLS-1$
                  String.valueOf(i));
    } else {
      fileName = base+((n<10) ? "0" : (                                               //$NON-NLS-1$
        n<100) ? "00" : (                                                             //$NON-NLS-1$
          n<1000) ? "000" :                                                           //$NON-NLS-1$
            "0000");                                                                  //$NON-NLS-1$
    }
    if (ext!=null)
    	fileName += "."+ext; //$NON-NLS-1$
    return new File(fileName);
  }

  /**
   * Saves images to a numbered sequence of jpg files.
   *
   * @param fileName the file name basis for images
   * @param images the images to save
   * @return the paths of the saved images
   * @throws IOException
   */
  protected static String[] saveImages(String fileName, BufferedImage[] images) throws IOException {    
  	String[] fileNames = getFileNames(fileName, images.length);
    for(int i = 0; i<images.length; i++) {
    	String next = fileNames[i];
      javax.imageio.ImageIO.write(images[i], ext, 
      		new BufferedOutputStream(new FileOutputStream(next)));
    }
    return fileNames;
  }
  
  protected static String[] getFileNames(String fileName, int length) {
    if(length==1) return new String[] {fileName};
    // determine number of digits to append to file names
    int k = getAppendedNumber(fileName);
    ArrayList<String> paths = new ArrayList<String>();
    int digits = (length+k<10) ? 1 : (length+k<100) ? 2 : (length+k<1000) ? 3 : 4;
    // get base
    String base = getBase(fileName);
    for(int i = 0; i<length; i++) {
      // append numbers and save images
      String num = String.valueOf(i+k);
      if((digits==2)&&(i+k<10)) {
        num = "0"+num;             //$NON-NLS-1$
      } else if((digits==3)&&(i+k<10)) {
        num = "00"+num;            //$NON-NLS-1$
      } else if((digits==3)&&(i+k<100)) {
        num = "0"+num;             //$NON-NLS-1$
      } else if((digits==4)&&(i+k<10)) {
        num = "000"+num;           //$NON-NLS-1$
      } else if((digits==4)&&(i+k<100)) {
        num = "00"+num;            //$NON-NLS-1$
      } else if((digits==4)&&(i+k<1000)) {
        num = "0"+num;             //$NON-NLS-1$
      }
      fileName = base+num+"."+ext; //$NON-NLS-1$
      paths.add(fileName);
    }
    return paths.toArray(new String[0]);  	
  }

  protected static String getBase(String path) {
    String base = XML.stripExtension(path);
    // strip off digits at end, if any
    int len = base.length();
    int digits = 1;
    for(; digits<len; digits++) {
      try {
        Integer.parseInt(base.substring(len-digits));
      } catch(NumberFormatException ex) {
        break;
      }
    }
    digits--; // failed at digits, so go back one
    if(digits==0) { // no number found
      return base;
    }
    return base.substring(0, len-digits);
  }

  protected static int getAppendedNumber(String path) {
    String base = XML.stripExtension(path);
    // look for appended number at end, if any
    int len = base.length();
    int digits = 1;
    int n = 0;
    for(; digits<len; digits++) {
      try {
        n = Integer.parseInt(base.substring(len-digits));
      } catch(NumberFormatException ex) {
        break;
      }
    }
    return n;
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
