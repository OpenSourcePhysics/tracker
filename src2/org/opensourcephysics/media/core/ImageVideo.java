/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is a Video assembled from one or more still images.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ImageVideo extends VideoAdapter {
  // instance fields
  protected Component observer = new JPanel();             // image observer
  protected BufferedImage[] images = new BufferedImage[0]; // image array
  protected String[] paths = new String[0];                // relative image paths
  protected boolean readOnly = false;									   // true if images are only loaded from files as needed
  protected double deltaT = 100;													 // frame duration in milliseconds

  /**
   * Creates an ImageVideo and loads a named image or image sequence.
   *
   * @param imageName the name of the image file
   * @throws IOException
   */
  public ImageVideo(String imageName) throws IOException {
  	readOnly = true;
    append(imageName);
  }

  /**
   * Creates an ImageVideo and loads a named image or image sequence.
   *
   * @param imageName the name of the image file
   * @param sequence true to automatically load image sequence, if any
   * @throws IOException
   */
  public ImageVideo(String imageName, boolean sequence) throws IOException {
    this(imageName, sequence, true);
  }

  /**
   * Creates an ImageVideo and loads a named image or image sequence.
   *
   * @param imageName the name of the image file
   * @param sequence true to automatically load image sequence, if any
   * @param fileBased true if images will be loaded from files only as needed
   * @throws IOException
   */
  public ImageVideo(String imageName, boolean sequence, boolean fileBased) throws IOException {
  	readOnly = fileBased;
    append(imageName, sequence);
  }

  /**
   * Creates an ImageVideo from an image.
   *
   * @param image the image
   */
  public ImageVideo(Image image) {
    if(image!=null) {
      insert(new Image[] {image}, 0, null);
    }
  }

  /**
   * Creates an ImageVideo from an image array.
   *
   * @param images the image array
   */
  public ImageVideo(Image[] images) {
    if((images!=null)&&(images.length>0)&&(images[0]!=null)) {
      insert(images, 0, null);
    }
  }

  /**
   * Overrides VideoAdapter setFrameNumber method.
   *
   * @param n the desired frame number
   */
  public void setFrameNumber(int n) {
    super.setFrameNumber(n);
    rawImage = getImageAtFrame(getFrameNumber(), rawImage);
    isValidImage = false;
    isValidFilteredImage = false;
    firePropertyChange("framenumber", null, new Integer(getFrameNumber())); //$NON-NLS-1$
  }
  
  /**
   * Gets the current video time in milliseconds.
   *
   * @return the current time in milliseconds, or -1 if not known
   */
  public double getTime() {
    return getFrameNumber()*deltaT;
  }

  /**
   * Sets the frame duration in milliseconds.
   *
   * @param millis the desired frame duration in milliseconds
   */
  public void setFrameDuration(double millis) {  	
  	deltaT = millis;
  }

  /**
   * Sets the video time in milliseconds.
   *
   * @param millis the desired time in milliseconds
   */
  public void setTime(double millis) {  	
  	int frameNum = (int)(millis/deltaT);
  	frameNum = Math.max(frameNum, 0);
  	frameNum = Math.min(frameNum, getFrameCount()-1);
  	setFrameNumber(frameNum);
  }

  /**
   * Gets the start time in milliseconds.
   *
   * @return the start time in milliseconds, or -1 if not known
   */
  public double getStartTime() {
    return 0;
  }

  /**
   * Sets the start time in milliseconds. NOTE: the actual start time
   * is normally set to the beginning of a frame.
   *
   * @param millis the desired start time in milliseconds
   */
  public void setStartTime(double millis) {
  	/** not implemented */
  }

  /**
   * Gets the end time in milliseconds.
   *
   * @return the end time in milliseconds, or -1 if not known
   */
  public double getEndTime() {
    return getDuration();
  }

  /**
   * Sets the end time in milliseconds. NOTE: the actual end time
   * is set to the end of a frame.
   *
   * @param millis the desired end time in milliseconds
   */
  public void setEndTime(double millis) {
  	/** not implemented */
  }

  /**
   * Gets the duration of the video.
   *
   * @return the duration of the video in milliseconds, or -1 if not known
   */
  @Override
  public double getDuration() {
    return length()*deltaT;
  }

  /**
   * Gets the start time of the specified frame in milliseconds.
   *
   * @param n the frame number
   * @return the start time of the frame in milliseconds, or -1 if not known
   */
  public double getFrameTime(int n) {
    return n*deltaT;
  }

  /**
   * Gets the image array.
   *
   * @return the image array
   */
  public Image[] getImages() {
    return images;
  }

  /**
   * Appends the named image or image sequence to the end of this video.
   * This method will ask user whether to load sequences, if any.
   *
   * @param imageName the image name
   * @throws IOException
   */
  public void append(String imageName) throws IOException {
    insert(imageName, length());
  }

  /**
   * Appends the named image or image sequence to the end of this video.
   *
   * @param imageName the image name
   * @param sequence true to automatically load image sequence, if any
   * @throws IOException
   */
  public void append(String imageName, boolean sequence) throws IOException {
    insert(imageName, length(), sequence);
  }

  /**
   * Inserts the named image or image sequence at the specified index.
   * This method will ask user whether to load sequences, if any.
   *
   * @param imageName the image name
   * @param index the index
   * @throws IOException
   */
  public void insert(String imageName, int index) throws IOException {
    Object[] array = loadImages(imageName, true, // ask user for confirmation
      true);                                     // allow sequences, if any
    Image[] images = (Image[]) array[0];
    if(images.length>0) {
      String[] paths = (String[]) array[1];
      insert(images, index, paths);
    }
  }

  /**
   * Inserts the named image or image sequence at the specified index.
   *
   * @param imageName the image name
   * @param index the index
   * @param sequence true to automatically load image sequence, if any
   * @throws IOException
   */
  public void insert(String imageName, int index, boolean sequence) throws IOException {
    Object[] array = loadImages(imageName, false, // don't ask user for confirmation
      sequence);
    Image[] images = (Image[]) array[0];
    if(images.length>0) {
      String[] paths = (String[]) array[1];
      insert(images, index, paths);
    }
  }

  /**
   * Inserts an image at the specified index.
   *
   * @param image the image
   * @param index the index
   */
  public void insert(Image image, int index) {
    if(image==null) {
      return;
    }
    insert(new Image[] {image}, index, null);
  }

  /**
   * Removes the image at the specified index.
   *
   * @param index the index
   * @return the path of the image, or null if none removed
   */
  public String remove(int index) {
  	if (readOnly) return null;
    int len = images.length;
    if((len==1)||(len<=index)) {
      return null; // don't remove the only image
    }
    String removed = paths[index];
    BufferedImage[] newArray = new BufferedImage[len-1];
    System.arraycopy(images, 0, newArray, 0, index);
    System.arraycopy(images, index+1, newArray, index, len-1-index);
    images = newArray;
    String[] newPaths = new String[len-1];
    System.arraycopy(paths, 0, newPaths, 0, index);
    System.arraycopy(paths, index+1, newPaths, index, len-1-index);
    paths = newPaths;
    if(index<len-1) {
      rawImage = getImageAtFrame(index, rawImage);
    } else {
      rawImage = getImageAtFrame(index-1, rawImage);
    }
    frameCount = images.length;
    endFrameNumber = frameCount-1;
    Dimension newDim = getSize();
    if((newDim.height!=size.height)||(newDim.width!=size.width)) {
      this.firePropertyChange("size", size, newDim); //$NON-NLS-1$
      size = newDim;
      refreshBufferedImage();
    }
    return removed;
  }
  
  /**
   * Gets the size of this video.
   *
   * @return the maximum size of the images
   */
  public Dimension getSize() {
    int w = images[0].getWidth(observer);
    int h = images[0].getHeight(observer);
    for(int i = 1; i<images.length; i++) {
      w = Math.max(w, images[i].getWidth(observer));
      h = Math.max(h, images[i].getHeight(observer));
    }
    return new Dimension(w, h);
  }

  /**
   * Returns true if all of the images are associated with files.
   *
   * @return true if all images are file-based
   */
  public boolean isFileBased() {
    return getValidPaths().length==paths.length;
  }

  /**
   * Returns true if all images are loaded into memory.
   *
   * @return true if editable
   */
  public boolean isEditable() {
    return !readOnly;
  }

  /**
   * Sets the editable property.
   * @param edit true to edit
   * @throws IOException 
   */
  public void setEditable(boolean edit) throws IOException {
  	if (edit && isEditable()) return; // already editable
  	if (!edit && !isEditable()) return; // already uneditable
  	// get path to first image
  	String imagePath = paths[0];
    readOnly = !edit;							  	
  	// reset arrays
  	if (readOnly)
	    paths = new String[0];  	
  	images = new BufferedImage[0];
    System.gc();
    append(imagePath, true);
  }

  /**
   * Allows user to save invalid images, if any.
   *
   * @return true if saved
   */
  public boolean saveInvalidImages() {
    // collect invalid paths and images
    ArrayList<String> pathList = new ArrayList<String>();
    ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();
    for(int i = 0; i<paths.length; i++) {
      if(paths[i].equals("")) { //$NON-NLS-1$
        pathList.add(paths[i]);
        imageList.add(images[i]);
      }
    }
    if(pathList.isEmpty()) {
      return true;
    }
    // offer to save invalid paths
    int approved = JOptionPane.showConfirmDialog(null, MediaRes.getString("ImageVideo.Dialog.UnsavedImages.Message1")+XML.NEW_LINE //$NON-NLS-1$
      +MediaRes.getString("ImageVideo.Dialog.UnsavedImages.Message2"), //$NON-NLS-1$
        MediaRes.getString("ImageVideo.Dialog.UnsavedImages.Title"),   //$NON-NLS-1$
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    // if approved, use chooser to select file, save images and return true
    if(approved==JOptionPane.YES_OPTION) {
      try {
        ImageVideoRecorder recorder = new ImageVideoRecorder();
        recorder.setExpectedFrameCount(imageList.size());
        File file = recorder.selectFile();
        if(file==null) {
          return false;
        }
        String fileName = file.getAbsolutePath();
        BufferedImage[] imagesToSave = imageList.toArray(new BufferedImage[0]);
        String[] pathArray = ImageVideoRecorder.saveImages(fileName, imagesToSave);
        int j = 0;
        for(int i = 0; i<paths.length; i++) {
          if(paths[i].equals("")) { //$NON-NLS-1$
            paths[i] = pathArray[j++];
          }
        }
        if (getProperty("name")==null) {                     //$NON-NLS-1$
          setProperty("name", XML.getName(fileName));        //$NON-NLS-1$
          setProperty("path", fileName);                     //$NON-NLS-1$
          setProperty("absolutePath", fileName); //$NON-NLS-1$
        }
        return true;
      } catch(IOException ex) {
        ex.printStackTrace();
      }
    }
    // if declined, return false
    return false;
  }
  
  //_______________________ private/protected methods ____________________________
  
  /**
   * Gets the image for a specified frame number.
   *
   * @param frameNumber the frame number
   * @param defaultImage the image to return if no other found
   */
  private Image getImageAtFrame(int frameNumber, Image defaultImage) {
    if (readOnly && frameNumber<paths.length) {
      if(!paths[frameNumber].equals("")) {//$NON-NLS-1$
        Image image = ResourceLoader.getImage(paths[frameNumber]);
        if (image!=null) return image;
      }
    }
    else if (frameNumber<images.length && images[frameNumber]!=null) {
	    return images[frameNumber];
    }
    return defaultImage;
  }
  
  private int length() {
  	if (readOnly) return paths.length;
  	return images.length;
  }

  /**
   * Loads an image or image sequence specified by name. This returns
   * an Object[] containing an Image[] at index 0 and a String[] at index 1.
   *
   * @param imagePath the image path
   * @param alwaysAsk true to always ask for sequence confirmation
   * @param sequence true to automatically load sequences (if not alwaysAsk)
   * @return an array of loaded images and their corresponding paths
   * @throws IOException
   */
  private Object[] loadImages(String imagePath, boolean alwaysAsk, boolean sequence) throws IOException {
    Resource res = ResourceLoader.getResource(imagePath);
    if(res==null) {
      throw new IOException("Image "+imagePath+" not found"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    Image image = res.getImage();
    if(image==null) {
      throw new IOException("\""+imagePath+"\" is not an image"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if(getProperty("name")==null) {                       //$NON-NLS-1$
      setProperty("name", XML.getName(imagePath));        //$NON-NLS-1$
      setProperty("path", imagePath);                     //$NON-NLS-1$
      setProperty("absolutePath", res.getAbsolutePath()); //$NON-NLS-1$
    }
    if(!alwaysAsk&&!sequence) {
      Image[] images = new Image[] {image};
      String[] paths = new String[] {imagePath};
      return new Object[] {images, paths};
    }
    ArrayList<String> pathList = new ArrayList<String>();
    pathList.add(imagePath);
    // look for image sequence (numbered image names)
    String name = XML.getName(imagePath);
    String extension = ""; //$NON-NLS-1$
    int i = imagePath.lastIndexOf('.');
    if((i>0)&&(i<imagePath.length()-1)) {
      extension = imagePath.substring(i).toLowerCase();
      imagePath = imagePath.substring(0, i); // now free of extension
    }
    int len = imagePath.length();
    int n = 0;
    // first find the number of digits in name end
    int digits = 1;
    for(; digits<len; digits++) {
      try {
        n = Integer.parseInt(imagePath.substring(len-digits));
      } catch(NumberFormatException ex) {
        break;
      }
    }
    digits--; // failed at digits, so go back one
    if(digits==0) { // no number found, so load single image
      Image[] images = new Image[] {image};
      String[] paths = new String[] {imagePath+extension};
      return new Object[] {images, paths};
    }
    // image name ends with number, so look for sequence
    ArrayList<Image> imageList = new ArrayList<Image>();
    imageList.add(image);
    int limit = 10;
    digits = Math.min(digits, 4);
    switch(digits) {
       case 1 :
         limit = 10;
         break;
       case 2 :
         limit = 100;
         break;
       case 3 :
         limit = 1000;
         break;
       case 4 :
         limit = 10000;
    }
    String root = imagePath.substring(0, len-digits);
    try {
      boolean asked = false;
      while(n<limit-1) {
        n++;
        // fill with leading zeros if nec
        String num = String.valueOf(n);
        int zeros = digits-num.length();
        for(int k = 0; k<zeros; k++) {
          num = "0"+num;                                                                                                                               //$NON-NLS-1$
        }
        imagePath = root+num+extension;
        // load images only if not loading as needed
        if (!readOnly || imageList.isEmpty()) {
	        image = ResourceLoader.getImage(imagePath);
	        if(image==null) {
	          break;
	        }
        }
        // if loading as needed, just check that the resource exists
        else if(ResourceLoader.getResource(imagePath)==null)
        	break;
        if(!asked&&alwaysAsk) {
          asked = true;
          // strip path from image name
          int response = JOptionPane.showOptionDialog(null, "\""+name+"\" "+MediaRes.getString("ImageVideo.Dialog.LoadSequence.Message")+XML.NEW_LINE+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            MediaRes.getString("ImageVideo.Dialog.LoadSequence.Query"),                                                                                                                                                                       //$NON-NLS-1$
              MediaRes.getString("ImageVideo.Dialog.LoadSequence.Title"),                                                                                                                                                                     //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {MediaRes.getString("ImageVideo.Dialog.LoadSequence.Button.SingleImage"), MediaRes.getString("ImageVideo.Dialog.LoadSequence.Button.AllImages")}, //$NON-NLS-1$ //$NON-NLS-2$
                  MediaRes.getString("ImageVideo.Dialog.LoadSequence.Button.AllImages")); //$NON-NLS-1$
          if(response==JOptionPane.YES_OPTION) {
            break;
          }
        }
        // always add first image to list, but later images only if not loading as needed
        if (!readOnly || imageList.isEmpty()) {
        	imageList.add(image);
        }
        pathList.add(imagePath);
      }
    } catch(NumberFormatException ex) {
      ex.printStackTrace();
    } 
    Image[] images = imageList.toArray(new Image[0]);
    String[] paths = pathList.toArray(new String[0]);
    return new Object[] {images, paths};
  }

  /**
   * Returns the valid paths (i.e., those that are not "").
   * Invalid paths are associated with pasted images rather than files.
   *
   * @return the valid paths
   */
  public String[] getValidPaths() {
    ArrayList<String> pathList = new ArrayList<String>();
    for(int i = 0; i<paths.length; i++) {
      if(!paths[i].equals("")) {//$NON-NLS-1$
        pathList.add(paths[i]); 
      }
    }
    return pathList.toArray(new String[0]);
  }

  /**
   * Returns the valid paths (i.e., those that are not "") relative to a bae path.
   * Invalid paths are associated with pasted images rather than files.
   * 
   * @param base a base path
   * @return the valid relative paths
   */
  protected String[] getValidPathsRelativeTo(String base) {
    ArrayList<String> pathList = new ArrayList<String>();
    for(int i = 0; i<paths.length; i++) {
      if(!paths[i].equals("")) { //$NON-NLS-1$
        pathList.add(XML.getPathRelativeTo(paths[i], base));
      }
    }
    return pathList.toArray(new String[0]);
  }

  /**
   * Inserts images starting at the specified index.
   *
   * @param newImages an array of images
   * @param index the insertion index
   * @param imagePaths array of image file paths.
   */
  protected void insert(Image[] newImages, int index, String[] imagePaths) {
  	if (readOnly && imagePaths==null) return;
    int len = length();
    index = Math.min(index, len); // in case some prev images not successfully loaded
    int n = newImages.length;
    // convert new images to BufferedImage if nec
    BufferedImage[] buf = new BufferedImage[n];
    for(int i = 0; i<newImages.length; i++) {
      Image im = newImages[i];
      if(im instanceof BufferedImage) {
        buf[i] = (BufferedImage) im;
      } else {
        int w = im.getWidth(null);
        int h = im.getHeight(null);
        buf[i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        buf[i].createGraphics().drawImage(im, 0, 0, null);
      }
    }
    // insert new images
    BufferedImage[] newArray = new BufferedImage[len+n];
    System.arraycopy(images, 0, newArray, 0, index);
    System.arraycopy(buf, 0, newArray, index, n);
    System.arraycopy(images, index, newArray, index+n, len-index);
    images = newArray;
    // create empty paths if null
    if(imagePaths==null) {
      imagePaths = new String[newImages.length];
      for(int i = 0; i<imagePaths.length; i++) {
        imagePaths[i] = ""; //$NON-NLS-1$
      }
    }
    // insert new paths
    n = imagePaths.length;
    String[] newPaths = new String[len+n];
    System.arraycopy(paths, 0, newPaths, 0, index);
    System.arraycopy(imagePaths, 0, newPaths, index, n);
    System.arraycopy(paths, index, newPaths, index+n, len-index);
    paths = newPaths;
    rawImage = getImageAtFrame(index, rawImage);
    frameCount = length();
    endFrameNumber = frameCount-1;
    if(coords==null) {
      size = new Dimension(rawImage.getWidth(observer), rawImage.getHeight(observer));
      refreshBufferedImage();
      // create coordinate system and relativeAspects
      coords = new ImageCoordSystem(frameCount);
      coords.addPropertyChangeListener(this);
      aspects = new DoubleArray(frameCount, 1);
    } else {
      coords.setLength(frameCount);
      aspects.setLength(frameCount);
    }
    Dimension newDim = getSize();
    if((newDim.height!=size.height)||(newDim.width!=size.width)) {
      this.firePropertyChange("size", size, newDim); //$NON-NLS-1$
      size = newDim;
      refreshBufferedImage();
    }
  }

  //______________________________ static XML.Loader_________________________  

  /**
   * Returns an XML.ObjectLoader to save and load ImageVideo data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load ImageVideo data.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves ImageVideo data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the ImageVideo object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      ImageVideo video = (ImageVideo) obj;
      String base = (String) video.getProperty("base"); //$NON-NLS-1$
      String[] paths = video.getValidPathsRelativeTo(base);
      if(paths.length>0) {
        control.setValue("paths", paths);   //$NON-NLS-1$
        control.setValue("path", paths[0]); //$NON-NLS-1$
      }
      if(!video.getFilterStack().isEmpty()) {
        control.setValue("filters", video.getFilterStack().getFilters()); //$NON-NLS-1$
      }
      control.setValue("delta_t", video.deltaT); //$NON-NLS-1$
    }

    /**
     * Creates a new ImageVideo.
     *
     * @param control the control
     * @return the new ImageVideo
     */
    public Object createObject(XMLControl control) {
      String[] paths = (String[]) control.getObject("paths"); //$NON-NLS-1$
      // legacy code that opens single image or sequence
      if(paths==null) {
        try {
          String path = control.getString("path");      //$NON-NLS-1$
          boolean seq = control.getBoolean("sequence"); //$NON-NLS-1$
          if(path!=null) {
            ImageVideo vid = new ImageVideo(path, seq);
            return vid;
          }
        } catch(IOException ex) {
          ex.printStackTrace();
          return null;
        }
      }
      // pre-2007 code
      boolean[] sequences = (boolean[]) control.getObject("sequences"); //$NON-NLS-1$
      if(sequences!=null) {
        try {
          ImageVideo vid = new ImageVideo(paths[0], sequences[0]);
          for(int i = 1; i<paths.length; i++) {
            vid.append(paths[i], sequences[i]);
          }
          return vid;
        } catch(Exception ex) {
          ex.printStackTrace();
          return null;
        }
      }
      // 2007+ code
      if(paths.length==0) {
        return null;
      }
      ImageVideo vid = null;
      ArrayList<String> badPaths = null;
      for(int i = 0; i<paths.length; i++) {
        try {
          if(vid==null) {
            vid = new ImageVideo(paths[i], false);
          } else {
            vid.append(paths[i], false);
          }
        } catch(Exception ex) {
          if(badPaths==null) {
            badPaths = new ArrayList<String>();
          }
          badPaths.add("\""+paths[i]+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      if(badPaths!=null) {
        String s = badPaths.get(0);
        for(int i = 1; i<badPaths.size(); i++) {
          s += ", "+badPaths.get(i);                                                                      //$NON-NLS-1$
        }
        JOptionPane.showMessageDialog(null, MediaRes.getString("ImageVideo.Dialog.MissingImages.Message") //$NON-NLS-1$
                                      +":\n"+s,                                                           //$NON-NLS-1$
                                        MediaRes.getString("ImageVideo.Dialog.MissingImages.Title"),      //$NON-NLS-1$
                                          JOptionPane.WARNING_MESSAGE);
      }
      if(vid==null) {
        return null;
      }
      vid.rawImage = vid.images[0];
      Collection<?> filters = Collection.class.cast(control.getObject("filters")); //$NON-NLS-1$
      if(filters!=null) {
        vid.getFilterStack().clear();
        Iterator<?> it = filters.iterator();
        while(it.hasNext()) {
          Filter filter = (Filter) it.next();
          vid.getFilterStack().addFilter(filter);
        }
      }
    	String path = paths[0];
    	String ext = XML.getExtension(path);
      VideoType type = VideoIO.getVideoType("image", ext); //$NON-NLS-1$
      if (type!=null)
      	vid.setProperty("video_type", type); //$NON-NLS-1$
      vid.deltaT = control.getDouble("delta_t"); //$NON-NLS-1$
      return vid;
    }

    /**
     * This does nothing, but is required by the XML.ObjectLoader interface.
     *
     * @param control the control
     * @param obj the ImageVideo object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      return obj;
    }

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
