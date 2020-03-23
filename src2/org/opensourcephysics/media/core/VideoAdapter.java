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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This provides basic implementations of all Video methods. Subclasses should
 * provide a raw image for display--see ImageVideo or GifVideo for an example.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoAdapter implements Video {
  // instance fields
  protected Image rawImage;              // raw image from video source
  protected Dimension size;              // image pixel dimensions
  protected BufferedImage bufferedImage; // offscreen buffered image copy
  protected BufferedImage filteredImage; // filtered image
  protected int frameCount;
  protected int frameNumber;
  protected int startFrameNumber;
  protected int endFrameNumber;
  protected double rate = 1;
  protected boolean playing = false;
  protected boolean looping = false;
  protected double minX, maxX, minY, maxY;
  protected boolean mouseEnabled = false;
  protected boolean visible = true;
  protected boolean isMeasured = false;
  protected boolean isValidMeasure = false;
  protected boolean widthDominates = true;
  protected boolean isValidImage = false;
  protected boolean isValidFilteredImage = false;
  protected ImageCoordSystem coords;
  protected DoubleArray aspects;
  protected PropertyChangeSupport support;
  protected HashMap<String, Object> properties = new HashMap<String, Object>();
  protected FilterStack filterStack = new FilterStack();
  protected Raster clearRaster;

  /**
   * Protected constructor creates an empty VideoAdapter
   */
  protected VideoAdapter() {
    initialize();
  }

  /**
   * Draws the video image on the panel.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    if(((panel instanceof VideoPanel)&&((VideoPanel) panel).isDrawingInImageSpace())||isMeasured) {
      AffineTransform gat = g2.getTransform(); // save graphics transform
      g2.transform(panel.getPixelTransform()); // world to screen
      if(panel instanceof VideoPanel) {
        VideoPanel vidPanel = (VideoPanel) panel;
        if(!vidPanel.isDrawingInImageSpace()) {
          // use video panel's coords for vid to world transform
          ImageCoordSystem coords = vidPanel.getCoords();
          g2.transform(coords.getToWorldTransform(frameNumber));
        }
      } 
      else { // not a video panel, so draw in world space
        // use this video's coords for vid to world transform
        g2.transform(coords.getToWorldTransform(frameNumber));
      }
      // draw the video or filtered image
      if(filterStack.isEmpty()||!filterStack.isEnabled()) {
        g2.drawImage(rawImage, 0, 0, panel);
      } else {
        g2.drawImage(getImage(), 0, 0, panel);
      }
      g2.setTransform(gat);                    // restore transform
    } 
    else { // center image in panel if not measured
      double centerX = (panel.getXMax()+panel.getXMin())/2;
      double centerY = (panel.getYMax()+panel.getYMin())/2;
      int xoffset = panel.xToPix(centerX)-size.width/2;
      int yoffset = panel.yToPix(centerY)-size.height/2;
      // draw the video or filtered image
      if(filterStack.isEmpty()||!filterStack.isEnabled()) {
        g2.drawImage(rawImage, xoffset, yoffset, panel);
      } else {
        g2.drawImage(getImage(), xoffset, yoffset, panel);
      }
    }
  }

  /**
   * Shows or hides the video.
   *
   * @param visible <code>true</code> to show the video
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
    firePropertyChange("videoVisible", null, new Boolean(visible)); //$NON-NLS-1$
  }

  /**
   * Gets the visibility of the video.
   *
   * @return <code>true</code> if the video is visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Gets the minimum x needed to draw this object.
   *
   * @return minimum x
   */
  public double getXMin() {
    if(!isValidMeasure) {
      findMinMaxValues();
    }
    return minX;
  }

  /**
   * Gets the maximum x needed to draw this object.
   *
   * @return maximum x
   */
  public double getXMax() {
    if(!isValidMeasure) {
      findMinMaxValues();
    }
    return maxX;
  }

  /**
   * Gets the minimum y needed to draw this object.
   *
   * @return minimum y
   */
  public double getYMin() {
    if(!isValidMeasure) {
      findMinMaxValues();
    }
    return minY;
  }

  /**
   * Gets the maximum y needed to draw this object.
   *
   * @return maximum y
   */
  public double getYMax() {
    if(!isValidMeasure) {
      findMinMaxValues();
    }
    return maxY;
  }

  /**
   * Reports whether information is available to set min/max values.
   *
   * @return <code>true</code> if min/max values are valid
   */
  public boolean isMeasured() {
    return isMeasured;
  }

  /**
   * Gets the current video image after applying enabled filters.
   *
   * @return the current video image with filters applied
   */
  public BufferedImage getImage() {
    if(!isValidImage) { // bufferedImage needs refreshing
      isValidImage = true;
      Graphics g = bufferedImage.createGraphics();
      bufferedImage.setData(clearRaster);
      g.drawImage(rawImage, 0, 0, null);
    }
    if(filterStack.isEmpty()||!filterStack.isEnabled()) {
      return bufferedImage;
    } else if(!isValidFilteredImage) { // filteredImage needs refreshing
      isValidFilteredImage = true;
      filteredImage = filterStack.getFilteredImage(bufferedImage);
    }
    return filteredImage;
  }

  /**
   * Returns this video if enabled.
   *
   * @param panel the drawing panel
   * @param xpix the x coordinate in pixels
   * @param ypix the y coordinate in pixels
   * @return this if enabled, otherwise null
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!mouseEnabled) {
      return null;
    }
    return this;
  }

  /**
   * Sets whether this responds to mouse hits.
   *
   * @param enabled <code>true</code> if this responds to mouse hits.
   */
  public void setEnabled(boolean enabled) {
    mouseEnabled = enabled;
  }

  /**
   * Gets whether this responds to mouse hits.
   *
   * @return <code>true</code> if this responds to mouse hits.
   */
  public boolean isEnabled() {
    return mouseEnabled;
  }

  /**
   * Sets x position of upper left corner of the specified video frame
   * in world units.
   *
   * @param n the video frame number
   * @param x the world x position
   */
  public void setFrameX(int n, double x) {
    setFrameXY(n, x, coords.imageToWorldY(n, 0, 0));
  }

  /**
   * Sets x position of upper left corner of all video frames
   * in world units.
   *
   * @param x the world x position
   */
  public void setX(double x) {
    for(int n = 0; n<frameCount; n++) {
      setFrameX(n, x);
    }
  }

  /**
   * Sets y position of upper left corner of the specified video frame
   * in world units.
   *
   * @param n the video frame number
   * @param y the world y position
   */
  public void setFrameY(int n, double y) {
    setFrameXY(n, coords.imageToWorldX(n, 0, 0), y);
  }

  /**
   * Sets y position of upper left corner of all video frames
   * in world units.
   *
   * @param y the world y position
   */
  public void setY(double y) {
    for(int n = 0; n<frameCount; n++) {
      setFrameY(n, y);
    }
  }

  /**
   * Gets x position of upper left corner of the current video frame
   * in world units.
   *
   * @return the world x position
   */
  public double getX() {
    return coords.imageToWorldX(frameNumber, 0, 0);
  }

  /**
   * Gets y position of upper left corner of the current video frame
   * in world units.
   *
   * @return the world y position
   */
  public double getY() {
    return coords.imageToWorldY(frameNumber, 0, 0);
  }

  /**
   * Sets the x and y position of the UL corner of the specified video
   * frame in world units.
   *
   * @param n the video frame number
   * @param x the world x position
   * @param y the world y position
   */
  public void setFrameXY(int n, double x, double y) {
    double sin = coords.getSine(n);
    double cos = coords.getCosine(n);
    double tx = coords.getScaleX(n)*(y*sin-x*cos);
    double ty = coords.getScaleY(n)*(y*cos+x*sin);
    coords.setOriginXY(n, tx, ty);
  }

  /**
   * Sets the x and y position of the UL corner of all video frames
   * in world units.
   *
   * @param x the world x position
   * @param y the world y position
   */
  public void setXY(double x, double y) {
    for(int n = 0; n<frameCount; n++) {
      setFrameXY(n, x, y);
    }
  }

  /**
   * Sets the relative aspect of the specified video frame. Relative
   * aspect is the ratio of the world aspect to the pixel aspect of
   * the image. The pixel aspect is the ratio of image width to height
   * in pixels, and world aspect is the ratio of world width to height
   * in world units. For example, a 320 x 240 pixel movie has a pixel
   * aspect of 1.33. If relative aspect is 2, then the world aspect
   * will be 2.67. So if the video's width is 16 wu, its height will
   * be 6 wu. Or if its height is 10 wu, its width will be 26.67 wu.
   *
   * @param n the video frame number
   * @param relativeAspect the desired relative aspect
   */
  public void setFrameRelativeAspect(int n, double relativeAspect) {
    if((relativeAspect<0.001)||(relativeAspect>1000)) {
      return;
    }
    aspects.set(n, Math.abs(relativeAspect));
    if(isMeasured) {
      if(widthDominates) {
        setFrameWidth(n, size.width/coords.getScaleX(n));
      } else {
        setFrameHeight(n, size.height/coords.getScaleY(n));
      }
    }
  }

  /**
   * Sets the relative aspect of all video frames. Relative
   * aspect is the ratio of the world aspect to the pixel aspect of
   * the image. The pixel aspect is the ratio of image width to height
   * in pixels, and world aspect is the ratio of world width to height
   * in world units. For example, a 320 x 240 pixel movie has a pixel
   * aspect of 1.33. If relative aspect is 2, then the world aspect
   * will be 2.67. So if the video's width is 16 wu, its height will
   * be 6 wu. Or if its height is 10 wu, its width will be 26.67 wu.
   *
   * @param relativeAspect the desired relative aspect
   */
  public void setRelativeAspect(double relativeAspect) {
    for(int n = 0; n<frameCount; n++) {
      setFrameRelativeAspect(n, relativeAspect);
    }
  }

  /**
   * Gets the relative aspect of the current video frame.
   *
   * @return the relative aspect of the current image.
   */
  public double getRelativeAspect() {
    return aspects.get(frameNumber);
  }

  /**
   * Sets the width of the specified video frame in world units. Also sets
   * the height using the relative aspect.
   *
   * @param n the video frame number
   * @param width the width in world units
   * @see #setRelativeAspect
   */
  public void setFrameWidth(int n, double width) {
    if(width==0) {
      return;
    }
    width = Math.abs(width);
    // save x and y since setting width invalidates them
    double x = coords.imageToWorldX(n, 0, 0);
    double y = coords.imageToWorldY(n, 0, 0);
    double scaleX = size.width/width;
    coords.setScaleX(n, scaleX);
    coords.setScaleY(n, scaleX*aspects.get(n));
    widthDominates = true;
    // restore x and y to their correct values
    setFrameXY(n, x, y);
  }

  /**
   * Sets the width of all video frames in world units. Also sets
   * the heights using the relative aspect.
   *
   * @param width the width in world units
   * @see #setRelativeAspect
   */
  public void setWidth(double width) {
    for(int n = 0; n<frameCount; n++) {
      setFrameWidth(n, width);
    }
  }

  /**
   * Gets the current width of the video frame.
   *
   * @return the width of the video image
   */
  public double getWidth() {
    return size.width/coords.getScaleX(frameNumber);
  }

  /**
   * Sets the height of the specified video frame in world units. Also sets
   * the width using the relative aspect.
   *
   * @param n the video frame number
   * @param height the height in world units
   * @see #setRelativeAspect
   */
  public void setFrameHeight(int n, double height) {
    if(height==0) {
      return;
    }
    height = Math.abs(height);
    // save x and y since setting width invalidates them
    double x = coords.imageToWorldX(n, 0, 0);
    double y = coords.imageToWorldY(n, 0, 0);
    double scaleY = size.height/height;
    coords.setScaleY(n, scaleY);
    coords.setScaleX(n, scaleY/aspects.get(n));
    widthDominates = false;
    // restore x and y to their correct values
    setFrameXY(n, x, y);
  }

  /**
   * Sets the height of all video frames in world units. Also sets
   * the widths using the relative aspect.
   *
   * @param height the height in world units
   * @see #setRelativeAspect
   */
  public void setHeight(double height) {
    for(int n = 0; n<frameCount; n++) {
      setFrameHeight(n, height);
    }
  }

  /**
   * Gets the current height of the video frame.
   *
   * @return the height of the video image
   */
  public double getHeight() {
    return size.height/coords.getScaleY(frameNumber);
  }

  /**
   * Sets the angle in radians of the specified video frame measured ccw
   * from the world x-axis. This results in a rotation only.
   *
   * @param n the video frame number
   * @param theta the angle in radians
   */
  public void setFrameAngle(int n, double theta) {
    // save x and y since setting angle invalidates them
    double x = coords.imageToWorldX(n, 0, 0);
    double y = coords.imageToWorldY(n, 0, 0);
    double cos = Math.cos(theta);
    double sin = Math.sin(theta);
    coords.setCosineSine(n, cos, -sin);
    setFrameXY(n, x, y); // restore x and y to their correct values
  }

  /**
   * Sets the angle in radians of all video frames measured ccw
   * from the world x-axis. This results in a rotation only.
   *
   * @param theta the angle in radians
   */
  public void setAngle(double theta) {
    for(int n = 0; n<frameCount; n++) {
      setFrameAngle(n, theta);
    }
  }

  /**
   * Gets the angle in radians of the curent video frame measured ccw
   * from the world x-axis.
   *
   * @return the angle in radians
   */
  public double getAngle() {
    return -coords.getAngle(frameNumber);
  }

  /**
   * Steps the video forward one frame.
   */
  public void step() {
    stop();
    setFrameNumber(frameNumber+1);
  }

  /**
   * Steps the video back one frame.
   */
  public void back() {
    stop();
    setFrameNumber(frameNumber-1);
  }

  /**
   * Gets the total number of video frames.
   *
   * @return the number of video frames
   */
  public int getFrameCount() {
    return frameCount;
  }

  /**
   * Gets the current video frame number.
   *
   * @return the current frame number
   */
  public int getFrameNumber() {
    return frameNumber;
  }

  /**
   * Sets the video frame number.
   *
   * @param n the desired frame number
   */
  public void setFrameNumber(int n) {
    if(n==frameNumber) {
      return;
    }
    n = Math.min(n, endFrameNumber);
    n = Math.max(n, startFrameNumber);
    firePropertyChange("nextframe", null, n); //$NON-NLS-1$
    frameNumber = n;
  }

  /**
   * Gets the start frame number.
   *
   * @return the start frame number
   * @see #getEndFrameNumber
   */
  public int getStartFrameNumber() {
    return startFrameNumber;
  }

  /**
   * Sets the start frame number.
   *
   * @param n the desired start frame number
   * @see #setEndFrameNumber
   */
  public void setStartFrameNumber(int n) {
    if(n==startFrameNumber) {
      return;
    }
    n = Math.max(0, n);
    startFrameNumber = Math.min(endFrameNumber, n);
    firePropertyChange("startframe", null, new Integer(startFrameNumber)); //$NON-NLS-1$
  }

  /**
   * Gets the end frame number.
   *
   * @return the end frame number
   * @see #getStartFrameNumber
   */
  public int getEndFrameNumber() {
    return endFrameNumber;
  }

  /**
   * Sets the end frame number.
   *
   * @param n the desired end frame number,
   * @see #setStartFrameNumber
   */
  public void setEndFrameNumber(int n) {
    if(n==endFrameNumber) {
      return;
    }
    if(frameCount>1) {
      n = Math.min(frameCount-1, n);
    }
    endFrameNumber = Math.max(startFrameNumber, n);
    firePropertyChange("endframe", null, new Integer(endFrameNumber)); //$NON-NLS-1$
  }

  /**
   * Gets the start time of the specified frame in milliseconds.
   *
   * @param n the frame number
   * @return the start time of the frame in milliseconds, or -1 if not known
   */
  public double getFrameTime(int n) {
    return -1;
  }

  /**
   * Gets the duration of the specified frame in milliseconds.
   *
   * @param n the frame number
   * @return the duration of the frame in milliseconds
   */
  public double getFrameDuration(int n) {
    if(frameCount==1) {
      return getDuration();
    }
    if(n==frameCount-1) {
      return getDuration()-getFrameTime(n);
    }
    return getFrameTime(n+1)-getFrameTime(n);
  }

  /**
   * Plays the video at the current rate.
   */
  public void play() {
    playing = true;
  }

  /**
   * Stops the video.
   */
  public void stop() {
    playing = false;
  }

  /**
   * Stops the video and resets it to the start time.
   */
  public void reset() {
    stop();
    setFrameNumber(startFrameNumber);
  }

  /**
   * Gets the current video time in milliseconds.
   *
   * @return the current time in milliseconds, or -1 if not known
   */
  public double getTime() {
    return -1;
  }

  /**
   * Sets the video time in milliseconds.
   *
   * @param millis the desired time in milliseconds
   */
  public void setTime(double millis) {

  /** implemented by subclasses */
  }

  /**
   * Gets the start time in milliseconds.
   *
   * @return the start time in milliseconds, or -1 if not known
   */
  public double getStartTime() {
    return -1;
  }

  /**
   * Sets the start time in milliseconds. NOTE: the actual start time
   * is normally set to the beginning of a frame.
   *
   * @param millis the desired start time in milliseconds
   */
  public void setStartTime(double millis) {

  /** implemented by subclasses */
  }

  /**
   * Gets the end time in milliseconds.
   *
   * @return the end time in milliseconds, or -1 if not known
   */
  public double getEndTime() {
    return -1;
  }

  /**
   * Sets the end time in milliseconds. NOTE: the actual end time
   * is set to the end of a frame.
   *
   * @param millis the desired end time in milliseconds
   */
  public void setEndTime(double millis) {

  /** implemented by subclasses */
  }

  /**
   * Gets the duration of the video.
   *
   * @return the duration of the video in milliseconds, or -1 if not known
   */
  public double getDuration() {
    return -1;
  }

  /**
   * Sets the frame number to the start frame.
   */
  public void goToStart() {
    setFrameNumber(startFrameNumber);
  }

  /**
   * Sets the frame number to the end frame.
   */
  public void goToEnd() {
    setFrameNumber(endFrameNumber);
  }

  /**
   * Starts and stops the video.
   *
   * @param playing <code>true</code> starts the video, and
   * <code>false</code> stops it
   */
  public void setPlaying(boolean playing) {
    if(playing) {
      play();
    } else {
      stop();
    }
  }

  /**
   * Gets the playing state of this video.
   *
   * @return <code>true</code> if the video is playing
   */
  public boolean isPlaying() {
    return playing;
  }

  /**
   * Sets the looping state of this video.
   * If true, the video restarts when reaching the end.
   *
   * @param loops <code>true</code> if the video loops
   */
  public void setLooping(boolean loops) {
    if(looping==loops) {
      return;
    }
    looping = loops;
    firePropertyChange("looping", null, new Boolean(looping)); //$NON-NLS-1$
  }

  /**
   * Gets the looping state of the video.
   * If true, the video restarts when reaching the end.
   *
   * @return <code>true</code> if the video loops
   */
  public boolean isLooping() {
    return looping;
  }

  /**
   * Sets the relative play rate. Relative play rate is the ratio
   * of a video's play rate to its preferred ("normal") play rate.
   *
   * @param rate the relative play rate.
   */
  public void setRate(double rate) {
    rate = Math.abs(rate);
    if((rate==this.rate)||(rate==0)) {
      return;
    }
    this.rate = rate;
  }

  /**
   * Gets the relative play rate. Relative play rate is the ratio
   * of a video's play rate to its preferred ("normal") play rate.
   *
   * @return the relative play rate.
   */
  public double getRate() {
    return rate;
  }

  /**
   * Sets the image coordinate system used to convert from
   * imagespace to worldspace.
   *
   * @param coords the image coordinate system
   */
  public void setCoords(ImageCoordSystem coords) {
    if(coords==this.coords) {
      return;
    }
    this.coords.removePropertyChangeListener(this);
    coords.addPropertyChangeListener(this);
    this.coords = coords;
    isMeasured = true;
    isValidMeasure = false;
    firePropertyChange("coords", null, coords); //$NON-NLS-1$
  }

  /**
   * Gets the image coordinate system.
   *
   * @return the image coordinate system
   */
  public ImageCoordSystem getCoords() {
    return coords;
  }

  /**
   * Sets the filter stack.
   *
   * @param stack the new filter stack
   */
  public void setFilterStack(FilterStack stack) {
    filterStack.removePropertyChangeListener(this);
    filterStack = stack;
    filterStack.addPropertyChangeListener(this);
  }

  /**
   * Gets the filter stack.
   *
   * @return the filter stack
   */
  public FilterStack getFilterStack() {
    return filterStack;
  }

  /**
   * Sets a user property of the video.
   *
   * @param name the name of the property
   * @param value the value of the property
   */
  public void setProperty(String name, Object value) {
    if(name.equals("measure")) { //$NON-NLS-1$
      isValidMeasure = false;
    } else {
      properties.put(name, value);
    }
  }

  /**
   * Gets a user property of the video. May return null.
   *
   * @param name the name of the property
   * @return the value of the property
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * Gets a collection of user property names for the video.
   *
   * @return a collection of property names
   */
  public Collection<String> getPropertyNames() {
    return properties.keySet();
  }

  /**
   * Adds a PropertyChangeListener to this video.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to this video.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener from this video.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  /**
   * Disposes of this video.
   */
  public void dispose() {
  	if (coords!=null)
  		coords.removePropertyChangeListener(this);
    getFilterStack().setInspectorsVisible(false);
  }

  /**
   * Responds to property change events. VideoAdapter listens for the following
   * events: "transform" from ImageCoordSystem and "image" from
   * FilterStack.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if(e.getSource()==coords) {             // "transform"
      isMeasured = true;
      isValidMeasure = false;
    } else if(e.getSource()==filterStack) { // "image"
      isValidFilteredImage = false;
      support.firePropertyChange(e);
    }
  }

  //____________________________ protected methods ____________________________

  /**
   * Sends a PropertyChangeEvent to registered listeners. No event is sent
   * if oldVal and newVal are equal, unless they are both null.
   *
   * @param property the name of the property that has changed
   * @param oldVal the value of the property before the change (may be null)
   * @param newVal the value of the property after the change (may be null)
   */
  protected void firePropertyChange(String property, Object oldVal, Object newVal) {
    support.firePropertyChange(property, oldVal, newVal);
  }

  @Override
  protected void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" resources released by garbage collector"); //$NON-NLS-1$
  }

  //_______________________________ protected methods _________________________

  /**
   * Initialize this video.
   */
  protected void initialize() {
    support = new SwingPropertyChangeSupport(this);
    filterStack.addPropertyChangeListener(this);
  }

  /**
   * Refreshes the BufferedImage based on current size.
   * Creates a new image if needed.
   */
  protected void refreshBufferedImage() {
    if((bufferedImage==null)||(bufferedImage.getWidth()!=size.width)||(bufferedImage.getHeight()!=size.height)) {
      bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      int clear = new Color(0, 0, 0, 0).getRGB();
      int[] rgb = new int[size.width*size.height];
      for(int i = 0; i<rgb.length; i++) {
        rgb[i] = clear;
      }
      bufferedImage.setRGB(0, 0, size.width, size.height, rgb, 0, size.width);
      clearRaster = bufferedImage.getData();
      isValidImage = false;
    }
  }

  /**
   * Finds the min and max values of x and y.
   */
  protected void findMinMaxValues() {
    VideoClip clip = (VideoClip) getProperty("videoclip"); //$NON-NLS-1$
    // check all four corner positions of every frame in the current clip
    Point2D corner = new Point2D.Double(0, 0);             // top left
    int start = 0;
    if(clip!=null) {
      start = clip.getStartFrameNumber();
    }
    AffineTransform at = coords.getToWorldTransform(start);
    at.transform(corner, corner);
    maxX = minX = corner.getX();
    maxY = minY = corner.getY();
    int stepCount = frameCount;
    if(clip!=null) {
      stepCount = clip.getStepCount();
    }
    for(int n = 0; n<stepCount; n++) {
      if(clip==null) {
        at = coords.getToWorldTransform(n);
      } else {
        at = coords.getToWorldTransform(clip.stepToFrame(n));
      }
      for(int i = 0; i<4; i++) {
        switch(i) {
           case 0 :
             corner.setLocation(0, 0);
             break;
           case 1 :
             corner.setLocation(size.width, 0);
             break;
           case 2 :
             corner.setLocation(0, size.height);
             break;
           case 3 :
             corner.setLocation(size.width, size.height);
        }
        at.transform(corner, corner);
        minX = Math.min(corner.getX(), minX);
        maxX = Math.max(corner.getX(), maxX);
        minY = Math.min(corner.getY(), minY);
        maxY = Math.max(corner.getY(), maxY);
      }
    }
    isValidMeasure = true;
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
