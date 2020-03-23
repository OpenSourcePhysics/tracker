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
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.TreeSet;

import javax.swing.event.SwingPropertyChangeSupport;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This manages point and vector transformations between imagespace and
 * worldspace coordinates. Imagespace coordinates of a point refer to its
 * pixel position (to sub-pixel precision) relative to the top left corner
 * of an image. Worldspace coordinates of the point are its <i>scaled</i> position
 * relative to a world reference frame <i>origin</i> and <i>axes</i>.
 * Transformations between coordinate spaces depend on the scale
 * (image units per world unit), the origin (image position of the
 * origin of the world reference frame) and the x-axis direction
 * (angle of the world x-axis measured ccw from the image x-axis).
 * Any or all of these may vary with frame number.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ImageCoordSystem {

  // instance fields
	
	  protected boolean ignoreUpdateRequests;// for tracker ReferenceFrame

  private int length;
  protected PropertyChangeSupport support;
  private Point2D point = new Point2D.Double();
  private TransformArray toImage, toWorld;
  private DoubleArray scaleX, scaleY;
  private DoubleArray originX, originY;
  private DoubleArray cosine, sine;
  TreeSet<Integer> keyFrames = new TreeSet<Integer>();
  protected boolean firePropChange = true;
  private boolean isAdjusting = false;
  private boolean fixedOrigin = true;
  private boolean fixedAngle = true;
  private boolean fixedScale = true;
  private boolean locked = false;
  private boolean updateToKeyFrames = true;

  /**
   * Constructs an ImageCoordSystem with a default initial array length.
   */
  public ImageCoordSystem() {
    this(10);
  }

  /**
   * Constructs an ImageCoordSystem with a specified initial array length.
   *
   * @param length the initial length
   */
  public ImageCoordSystem(int length) {
    this.length = length;
    toImage = new TransformArray(length);
    toWorld = new TransformArray(length);
    scaleX = new DoubleArray(length, 1);
    scaleY = new DoubleArray(length, 1);
    originX = new DoubleArray(length, 0);
    originY = new DoubleArray(length, 0);
    cosine = new DoubleArray(length, 1);
    sine = new DoubleArray(length, 0);
    support = new SwingPropertyChangeSupport(this);
    updateAllTransforms();
  }

  /**
   * Adds a PropertyChangeListener to this coordinate system.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to this coordinate system.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener from this coordinate system.
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
   * Sets the locked property. When locked, no changes are allowed.
   *
   * @param locked <code>true</code> to lock the coordinate system
   */
  public void setLocked(boolean locked) {
    this.locked = locked;
    support.firePropertyChange("locked", null, new Boolean(locked)); //$NON-NLS-1$
  }

  /**
   * Gets the locked property. When locked, no changes are allowed.
   *
   * @return <code>true</code> if this is locked
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Sets all origins, angles and scales to those at the specified frame.
   *
   * @param n the frame number
   */
  public void setAllValuesToFrame(int n) {
    setAllOriginsXY(getOriginX(n), getOriginY(n));
    setAllCosineSines(getCosine(n), getSine(n));
  	setAllScalesXY(getScaleX(n), getScaleY(n));
  	keyFrames.clear();
  }

  /**
   * Sets the fixed origin property. When it is fixed, it is in the same
   * position at all times. When setting fixed origin to true, this sets
   * all origins to the position of the origin at frame 0.
   *
   * @param fixed <code>true</code> to fix the origin
   */
  public void setFixedOrigin(boolean fixed) {
    setFixedOrigin(fixed, 0);
  }

  /**
   * Sets the fixed origin property. When it is fixed, it is in the same
   * position at all times. When setting fixed origin to true, this sets
   * all origins to the position of the origin at frame n.
   *
   * @param fixed <code>true</code> to fix the origin
   * @param n the frame number
   */
  public void setFixedOrigin(boolean fixed, int n) {
    if (fixed && fixedAngle && fixedScale) {
    	keyFrames.clear();
    }
    if(fixedOrigin==fixed) {
      return;
    }
    fixedOrigin = fixed;
    if(fixed) {
      setAllOriginsXY(getOriginX(n), getOriginY(n));
    }
    support.firePropertyChange("fixed_origin", !fixed, fixed); //$NON-NLS-1$
  }

  /**
   * Gets the fixed origin property.
   *
   * @return <code>true</code> if origin is fixed
   */
  public boolean isFixedOrigin() {
    return fixedOrigin;
  }

  /**
   * Sets the fixed angle property. When it is fixed, it is the same
   * at all times. When setting fixed angle to true, this sets
   * all angles to the angle at frame 0.
   *
   * @param fixed <code>true</code> to fix the angle
   */
  public void setFixedAngle(boolean fixed) {
    setFixedAngle(fixed, 0);
  }

  /**
   * Sets the fixed angle property. When it is fixed, it is the same
   * at all times. When setting fixed angle to true, this sets
   * all angles to the angle at frame n.
   *
   * @param fixed <code>true</code> to fix the angle
   * @param n the frame number
   */
  public void setFixedAngle(boolean fixed, int n) {
    if (fixed && fixedOrigin && fixedScale) {
    	keyFrames.clear();
    }
    if(fixedAngle==fixed) {
      return;
    }
    fixedAngle = fixed;
    if(fixed) {
      setAllCosineSines(getCosine(n), getSine(n));
    }
    support.firePropertyChange("fixed_angle", !fixed, fixed); //$NON-NLS-1$
  }

  /**
   * Gets the fixed angle property.
   *
   * @return <code>true</code> if angle is fixed
   */
  public boolean isFixedAngle() {
    return fixedAngle;
  }

  /**
   * Sets the fixed scale property. When it is fixed, it is the same
   * at all times. When setting fixed scale to true, this sets
   * all scales to the scale at frame 0.
   *
   * @param fixed <code>true</code> to fix the scale
   */
  public void setFixedScale(boolean fixed) {
    fixedScale = fixed;
  }

  /**
   * Sets the fixed scale property. When it is fixed, it is the same
   * at all times. When setting fixed scale to true, this sets
   * all scales to the scale at frame n.
   *
   * @param fixed <code>true</code> to fix the scale
   * @param n the frame number
   */
  public void setFixedScale(boolean fixed, int n) {
    if (fixed && fixedOrigin && fixedAngle) {
    	keyFrames.clear();
    }
    if(fixedScale==fixed) {
      return;
    }
    fixedScale = fixed;
    if(fixed) {
      setAllScalesXY(getScaleX(n), getScaleY(n));
    }
    support.firePropertyChange("fixed_scale", !fixed, fixed); //$NON-NLS-1$
  }

  /**
   * Gets the fixed scale property.
   *
   * @return <code>true</code> if scale is fixed
   */
  public boolean isFixedScale() {
    return fixedScale;
  }
  
  /**
   * Gets the scale factor (image units per world unit) along the image
   * x-axis (width direction) for the specified frame number.
   *
   * @param n the frame number
   * @return the x-axis scale factor
   */
  public double getScaleX(int n) {
    return scaleX.get(n);
  }

  /**
   * Gets the scale factor (image units per world unit) along the image
   * y-axis (height direction) for the specified frame number.
   *
   * @param n the frame number
   * @return the y-axis scale factor
   */
  public double getScaleY(int n) {
    return scaleY.get(n);
  }

  /**
   * Sets the scale factor (image units per world unit) along the image
   * x-axis (width direction) for the specified frame number.
   *
   * @param n the frame number
   * @param value the x scale factor
   */
  public void setScaleX(int n, double value) {
    if(isLocked()) {
      return;
    }
    if(isFixedScale()) {
      setAllScalesX(value);
      return;
    }
    if(n>=length) {
      setLength(n+1);
    }
    if (updateToKeyFrames) {
    	// end frame is just before first key frame following n
    	int end = length-1;
    	for (int next: keyFrames) {
    		if (next>n) {
    			end = next-1;
    			break;
    		}
    	}
    	setScalesX(n, end, value);
    	keyFrames.add(n);
    	return;
    }
    if(scaleX.set(n, value)) {
    	try {
        updateTransforms(n);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Sets the scale factor (image units per world unit) along the image
   * x-axis (width direction) for all frames.
   *
   * @param value the x scale factor
   */
  public void setAllScalesX(double value) {
    if(isLocked()) {
      return;
    }
    if(scaleX.fill(value)) {
      updateAllTransforms();
    }
  }

  /**
   * Sets the horizontal scale factor for all frames in a specified range.
   *
   * @param start the start frame
   * @param end the end frame
   * @param value the x scale factor
   */
  public void setScalesX(int start, int end, double value) {
    if(isLocked()) {
      return;
    }
    if(scaleX.fill(value, start, end)) {
      updateTransforms(start, end);
    }
  }

  /**
   * Sets the scale factor (image units per world unit) along the image
   * y-axis (height direction) for the specified frame number.
   *
   * @param n the frame number
   * @param value the y scale factor
   */
  public void setScaleY(int n, double value) {
    if(isLocked()) {
      return;
    }
    if(isFixedScale()) {
      setAllScalesY(value);
      return;
    }
    if(n>=length) {
      setLength(n+1);
    }
    if (updateToKeyFrames) {
    	// end frame is just before first key frame following n
    	int end = length-1;
    	for (int next: keyFrames) {
    		if (next>n) {
    			end = next-1;
    			break;
    		}
    	}
    	setScalesY(n, end, value);
    	keyFrames.add(n);
    	return;
    }
    if(scaleY.set(n, value)) {
    	try {
        updateTransforms(n);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Sets the scale factor (image units per world unit) along the image
   * y-axis (height direction) for all frames.
   *
   * @param value the y scale factor
   */
  public void setAllScalesY(double value) {
    if(isLocked()) {
      return;
    }
    if(scaleY.fill(value)) {
      updateAllTransforms();
    }
  }

  /**
   * Sets the vertical scale factor for all frames in a specified range.
   *
   * @param start the start frame
   * @param end the end frame
   * @param value the y scale factor
   */
  public void setScalesY(int start, int end, double value) {
    if(isLocked()) {
      return;
    }
    if(scaleY.fill(value, start, end)) {
      updateTransforms(start, end);
    }
  }

  /**
   * Sets the scale factors (image units per world unit) along the
   * x-axis and y-axis of the image for the specified frame number.
   *
   * @param n the frame number
   * @param valueX the x scale factor
   * @param valueY the y scale factor
   */
  public void setScaleXY(int n, double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    if(isFixedScale()) {
      setAllScalesXY(valueX, valueY);
      return;
    }
    if(n>=length) {
      setLength(n+1);
    }
    if (updateToKeyFrames) {
    	// end frame is just before first key frame following n
    	int end = length-1;
    	for (int next: keyFrames) {
    		if (next>n) {
    			end = next-1;
    			break;
    		}
    	}
    	setScalesXY(n, end, valueX, valueY);
    	keyFrames.add(n);
    	return;
    }
    boolean changed = scaleX.set(n, valueX);
    changed = scaleY.set(n, valueY)||changed;
    if(changed) {
    	try {
        updateTransforms(n);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Sets the scale factors (image units per world unit) along the
   * x-axis and y-axis of the image for all frames.
   *
   * @param valueX the x scale factor
   * @param valueY the y scale factor
   */
  public void setAllScalesXY(double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    boolean changed = scaleX.fill(valueX);
    changed = scaleY.fill(valueY)||changed;
    if(changed) {
      updateAllTransforms();
    }
  }

  /**
   * Sets the scale factors for all frames in a specified range.
   *
   * @param start the start frame
   * @param end the end frame
   * @param valueX the x scale factor
   * @param valueY the y scale factor
   */
  public void setScalesXY(int start, int end, double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    boolean changed = scaleX.fill(valueX, start, end);
    changed = scaleY.fill(valueY, start, end) || changed;
    if(changed) {
      updateTransforms(start, end);
    }
  }

  /**
   * Gets the image x position of the world origin for the specified
   * frame number.
   *
   * @param n the frame number
   * @return the image x position of the world origin
   */
  public double getOriginX(int n) {
    return originX.get(n);
  }

  /**
   * Gets the image y position of the world origin for the specified
   * frame number.
   *
   * @param n the frame number
   * @return the image y position of the world origin
   */
  public double getOriginY(int n) {
    return originY.get(n);
  }

  /**
   * Sets the image position of the world origin for the specified
   * frame number.
   *
   * @param n the frame number
   * @param valueX the image x position of the world origin
   * @param valueY the image y position of the world origin
   */
  public void setOriginXY(int n, double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    if(isFixedOrigin()) {
      setAllOriginsXY(valueX, valueY);
      return;
    }
    if(n>=length) {
      setLength(n+1);
    }
    if (updateToKeyFrames) {
    	// end frame is just before first key frame following n
    	int end = length-1;
    	for (int next: keyFrames) {
    		if (next>n) {
    			end = next-1;
    			break;
    		}
    	}
    	setOriginsXY(n, end, valueX, valueY);
    	keyFrames.add(n);
    	return;
    }
    boolean changed = originX.set(n, valueX);
    changed = originY.set(n, valueY)||changed;
    if(changed) {
    	try {
        updateTransforms(n);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Sets the image position of the world origin for all frames.
   *
   * @param valueX the image x position of the world origin
   * @param valueY the image y position of the world origin
   */
  public void setAllOriginsXY(double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    boolean changed = originX.fill(valueX);
    changed = originY.fill(valueY)||changed;
    if(changed) {
      updateAllTransforms();
    }
  }

  /**
   * Sets the image position of the world origin for all frames 
   * in a specified range.
   *
   * @param start the start frame
   * @param end the end frame
   * @param valueX the image x position of the world origin
   * @param valueY the image y position of the world origin
   */
  public void setOriginsXY(int start, int end, double valueX, double valueY) {
    if(isLocked()) {
      return;
    }
    boolean changed = originX.fill(valueX, start, end);
    changed = originY.fill(valueY, start, end) || changed;
    if(changed) {
      updateTransforms(start, end);
    }
  }

  /**
   * Gets the cosine of the angle of the world x-axis measured
   * ccw from the image x-axis for the specified frame number.
   *
   * @param n the frame number
   * @return the cosine of the angle
   */
  public double getCosine(int n) {
    return cosine.get(n);
  }

  /**
   * Gets the sine of the angle of the world x-axis measured
   * ccw from the image x-axis for the specified frame number.
   *
   * @param n the frame number
   * @return the sine of the angle
   */
  public double getSine(int n) {
    return sine.get(n);
  }

  /**
   * Sets the cosine and sine of the angle of the world x-axis measured
   * ccw from the image x-axis for the specified frame number.
   *
   * @param n the frame number
   * @param cos the cosine of the angle
   * @param sin the sine of the angle
   */
  public void setCosineSine(int n, double cos, double sin) {
    if(isLocked()) {
      return;
    }
    if(isFixedAngle()) {
      setAllCosineSines(cos, sin);
      return;
    }
    if(n>=length) {
      setLength(n+1);
    }
    if (updateToKeyFrames) {
    	// end frame is just before first key frame following n
    	int end = length-1;
    	for (int next: keyFrames) {
    		if (next>n) {
    			end = next-1;
    			break;
    		}
    	}
    	setCosineSines(n, end, cos, sin);
    	keyFrames.add(n);
    	return;
    }
    double d = Math.sqrt(cos*cos+sin*sin);
    boolean changed;
    if(d==0) {
      changed = cosine.set(n, 1);
      changed = sine.set(n, 0)||changed;
    } else {
      changed = cosine.set(n, cos/d);
      changed = sine.set(n, sin/d)||changed;
    }
    if(changed) {
    	try {
        updateTransforms(n);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Sets the cosine and sine of the angle of the world x-axis measured
   * ccw from the image x-axis for all frames.
   *
   * @param cos the cosine of the angle
   * @param sin the sine of the angle
   */
  public void setAllCosineSines(double cos, double sin) {
    if(isLocked()) {
      return;
    }
    double d = Math.sqrt(cos*cos+sin*sin);
    boolean changed;
    if(d==0) {
      changed = cosine.fill(1);
      changed = sine.fill(0)||changed;
    } else {
      changed = cosine.fill(cos/d);
      changed = sine.fill(sin/d)||changed;
    }
    if(changed) {
      updateAllTransforms();
    }
  }

  /**
   * Sets the cosine and sine for all frames in a specified range.
   *
   * @param start the start frame
   * @param end the end frame
   * @param cos the cosine of the angle
   * @param sin the sine of the angle
   */
  public void setCosineSines(int start, int end, double cos, double sin) {
    if(isLocked()) {
      return;
    }
    double d = Math.sqrt(cos*cos+sin*sin);
    boolean changed;
    if(d==0) {
      changed = cosine.fill(1, start, end);
      changed = sine.fill(0, start, end) || changed;
    } else {
      changed = cosine.fill(cos/d, start, end);
      changed = sine.fill(sin/d, start, end) || changed;
    }
    if(changed) {
      updateTransforms(start, end);
    }
  }

  /**
   * Gets the angle of the world x-axis measured ccw from the image
   * x-axis for the specified frame number.
   *
   * @param n the frame number
   * @return the angle in radians
   */
  public double getAngle(int n) {
    return Math.atan2(getSine(n), getCosine(n));
  }

  /**
   * Sets the angle of the world x-axis measured ccw from the image
   * x-axis for the specified frame number.
   *
   * @param n the frame number
   * @param theta the angle in radians
   */
  public void setAngle(int n, double theta) {
    if(isLocked()) {
      return;
    }
    setCosineSine(n, Math.cos(theta), Math.sin(theta));
  }

  /**
   * Sets the angle of the world x-axis measured ccw from the image
   * x-axis for all frames.
   *
   * @param theta the angle in radians
   */
  public void setAllAngles(double theta) {
    if(isLocked()) {
      return;
    }
    setAllCosineSines(Math.cos(theta), Math.sin(theta));
  }

  /**
   * Sets the length of this image coordinate system.
   *
   * @param count the total number of frames
   */
  public void setLength(int count) {
    if(isLocked()) {
      return;
    }
    length = count;
    toImage.setLength(length);
    toWorld.setLength(length);
    scaleX.setLength(length);
    scaleY.setLength(length);
    originX.setLength(length);
    originY.setLength(length);
    cosine.setLength(length);
    sine.setLength(length);
  }

  /**
   * Gets the length of this image coordinate system.
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Converts the specified image position to a world
   * x position for the specified frame number.
   *
   * @param n the frame number
   * @param imageX the image x position
   * @param imageY the image y position
   * @return the x coordinate in world space
   */
  public double imageToWorldX(int n, double imageX, double imageY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(imageX, imageY);
    toWorld.get(n).transform(point, point);
    return point.getX();
  }

  /**
   * Converts the specified image position to a world
   * y position for the specified frame number.
   *
   * @param n the frame number
   * @param imageX the image x position
   * @param imageY the image y position
   * @return the y coordinate in world space
   */
  public double imageToWorldY(int n, double imageX, double imageY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(imageX, imageY);
    toWorld.get(n).transform(point, point);
    return point.getY();
  }

  /**
   * Converts the specified world position to an image
   * x position for the specified frame number.
   *
   * @param n the frame number
   * @param worldX the world x position
   * @param worldY the world y position
   * @return the x coordinate in image space
   */
  public double worldToImageX(int n, double worldX, double worldY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(worldX, worldY);
    toImage.get(n).transform(point, point);
    return point.getX();
  }

  /**
   * Converts the specified world position to an image
   * y position for the specified frame number.
   *
   * @param n the frame number
   * @param worldX the world x position
   * @param worldY the world y position
   * @return the y coordinate in image space
   */
  public double worldToImageY(int n, double worldX, double worldY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(worldX, worldY);
    toImage.get(n).transform(point, point);
    return point.getY();
  }

  /**
   * Converts the specified image vector components to a world
   * vector x component for the specified frame number.
   *
   * @param n the frame number
   * @param imageX the image x components
   * @param imageY the image y components
   * @return the vector x component in world space
   */
  public double imageToWorldXComponent(int n, double imageX, double imageY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(imageX, imageY);
    toWorld.get(n).deltaTransform(point, point);
    return point.getX();
  }

  /**
   * Converts the specified image vector components to a world
   * vector y component for the specified frame number.
   *
   * @param n the frame number
   * @param imageX the image x components
   * @param imageY the image y components
   * @return the vector y component in world space
   */
  public double imageToWorldYComponent(int n, double imageX, double imageY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(imageX, imageY);
    toWorld.get(n).deltaTransform(point, point);
    return point.getY();
  }

  /**
   * Converts the specified world vector components to an image
   * vector x component for the specified frame number.
   *
   * @param n the frame number
   * @param worldX the world x position
   * @param worldY the world y position
   * @return the vector x component in image space
   */
  public double worldToImageXComponent(int n, double worldX, double worldY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(worldX, worldY);
    toImage.get(n).deltaTransform(point, point);
    return point.getX();
  }

  /**
   * Converts the specified world vector components to an image
   * vector y component for the specified frame number.
   *
   * @param n the frame number
   * @param worldX the world x position
   * @param worldY the world y position
   * @return the vector y component in image space
   */
  public double worldToImageYComponent(int n, double worldX, double worldY) {
    if(n>=length) {
      setLength(n+1);
    }
    point.setLocation(worldX, worldY);
    toImage.get(n).deltaTransform(point, point);
    return point.getY();
  }

  /**
   * Gets a copy of the affine transform used to convert from worldspace
   * to imagespace for the specified frame number.
   *
   * @param n the frame number
   * @return the worldspace to imagespace transform
   */
  public AffineTransform getToImageTransform(int n) {
    if(n>=length) {
      setLength(n+1);
    }
    return(AffineTransform) toImage.get(n).clone();
  }

  /**
   * Gets a copy of the affine transform used to convert from imagespace
   * to worldspace for the specified frame number.
   *
   * @param n the frame number
   * @return the imagespace to worldspace transform
   */
  public AffineTransform getToWorldTransform(int n) {
    if(n>=length) {
      setLength(n+1);
    }
    return (AffineTransform)toWorld.get(n).clone();
  }

  /**
   * Sets the adjusting flag.
   *
   * @param adjusting true if adjusting
   */
  public void setAdjusting(boolean adjusting) {
  	if (isAdjusting==adjusting)
  		return;
  	isAdjusting = adjusting;
		support.firePropertyChange("adjusting", null, adjusting); //$NON-NLS-1$
  }

  /**
   * Gets the adjusting flag.
   *
   * @return true if adjusting
   */
  public boolean isAdjusting() {
  	return isAdjusting;
  }

  /**
   * Gets the set of key frames. This returns the actual Set, not a copy, so the entries
   * can be converted when exporting a video clip/tracker panel combination.
   *
   * @return the Set
   */
  public TreeSet<Integer> getKeyFrames() {
  	return keyFrames;
  }

  //_____________________________ static methods _______________________________

  /**
   * Returns an XML.ObjectLoader to save and load ImageCoordSystem data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    XML.setLoader(FrameData.class, new FrameDataLoader());
    return new Loader();
  }

  /**
   * A class to save and load ImageCoordSystem data.
   */
  public static class Loader implements XML.ObjectLoader {
    /**
     * Saves ImageCoordSystem data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the ImageCoordSystem object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      ImageCoordSystem coords = (ImageCoordSystem) obj;
      control.setValue("fixedorigin", coords.isFixedOrigin()); //$NON-NLS-1$
      control.setValue("fixedangle", coords.isFixedAngle());   //$NON-NLS-1$
      control.setValue("fixedscale", coords.isFixedScale());   //$NON-NLS-1$
      control.setValue("locked", coords.isLocked());           //$NON-NLS-1$
      
      // save frame data: frame 0 and key frames only
      int count = coords.getLength();
      if(coords.isFixedAngle()&&coords.isFixedOrigin()&&coords.isFixedScale()) {
        count = 1;
      }
      FrameData[] data = new FrameData[count];
      for(int n = 0; n<count; n++) {
      	if (n==0 || coords.keyFrames.contains(n)) {
      		data[n] = new FrameData(coords, n);
      	}
      }
      control.setValue("framedata", data); //$NON-NLS-1$
    }

    /**
     * Creates a new ImageCoordSystem.
     *
     * @param control the control
     * @return the new ImageCoordSystem
     */
    public Object createObject(XMLControl control) {
      return new ImageCoordSystem();
    }

    /**
     * Loads an ImageCoordSystem with data from an XMLControl.
     *
     * @param control the control
     * @param obj the ImageCoordSystem object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ImageCoordSystem coords = (ImageCoordSystem) obj;
      coords.setLocked(false);
      // load fixed origin, angle and scale
      coords.setFixedOrigin(control.getBoolean("fixedorigin")); //$NON-NLS-1$
      coords.setFixedAngle(control.getBoolean("fixedangle"));   //$NON-NLS-1$
      coords.setFixedScale(control.getBoolean("fixedscale"));   //$NON-NLS-1$
      // load frame data
      FrameData[] data = (FrameData[]) control.getObject("framedata"); //$NON-NLS-1$
      coords.setLength(Math.max(coords.getLength(), data.length));
      for(int n = 0; n<data.length; n++) {
      	if (data[n]==null) continue;
        coords.setOriginXY(n, data[n].xo, data[n].yo);
        coords.setAngle(n, data[n].an*Math.PI/180); // convert from degrees to radians
        coords.setScaleXY(n, data[n].xs, data[n].ys);
      }
      // load locked
      coords.setLocked(control.getBoolean("locked")); //$NON-NLS-1$
      return obj;
    }

  }

  //___________________________ private methods ___________________________

  /**
   * Updates all transforms used to convert between drawing spaces.
   */
  protected void updateAllTransforms() {
    try {
      // don't fire property changes for individual updates
      firePropChange = false;
      for(int i = 0; i<length; i++) {
        updateTransforms(i);
      }
      firePropChange = true;
      // fire property change for overall updates
      support.firePropertyChange("transform", null, null); //$NON-NLS-1$
    } catch(NoninvertibleTransformException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Updates the transforms used to convert between drawing spaces
   * for the specified frame number. The toImage transform first rotates
   * about the world origin by an angle determined by cos and sin, then
   * scales to image units, then translates to the image origin. The
   * toWorld transform is the inverse.
   *
   * @param n the frame number
   * @throws NoninvertibleTransformException
   */
  private void updateTransforms(int n) throws NoninvertibleTransformException {
    // update toImage transform
    AffineTransform at = toImage.get(n);
    double tx = originX.get(n);
    double ty = originY.get(n);
    double sx = scaleX.get(n);
    double sy = scaleY.get(n);
    double cos = cosine.get(n);
    double sin = sine.get(n);
    at.setTransform(sx*cos, -sy*sin, -sx*sin, -sy*cos, tx, ty);
    // toWorld is inverse of toImage
    toWorld.get(n).setTransform(at.createInverse());
    // fire property change
    if(firePropChange) {
      support.firePropertyChange("transform", null, new Integer(n)); //$NON-NLS-1$
    }
  }

  /**
   * Updates a range of transforms.
   *
   * @param start the start frame number
   * @param end the end frame number
   */
  private void updateTransforms(int start, int end) {
    try {
      // don't fire property changes for individual updates
      firePropChange = false;
      for(int i = start; i<length; i++) {
        updateTransforms(i);
      }
      firePropChange = true;
      // fire property change for overall updates
      support.firePropertyChange("transform", null, null); //$NON-NLS-1$
    } catch(NoninvertibleTransformException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Inner class containing the coords data for a single frame number.
   */
  public static class FrameData {
    double xo, yo, an, xs, ys;

    FrameData() {

    /** empty block */
    }

    FrameData(ImageCoordSystem coords, int n) {
      xo = coords.getOriginX(n);
      yo = coords.getOriginY(n);
      an = coords.getAngle(n)*180/Math.PI; // angle in degrees
      xs = coords.getScaleX(n);
      ys = coords.getScaleY(n);
    }

  }

  /**
   * A class to save and load a FrameData.
   */
  private static class FrameDataLoader implements XML.ObjectLoader {
    public void saveObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      control.setValue("xorigin", data.xo); //$NON-NLS-1$
      control.setValue("yorigin", data.yo); //$NON-NLS-1$
      control.setValue("angle", data.an);   //$NON-NLS-1$ // angle in degrees
      control.setValue("xscale", data.xs);  //$NON-NLS-1$
      control.setValue("yscale", data.ys);  //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new FrameData();
    }

    public Object loadObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      data.xo = control.getDouble("xorigin");           //$NON-NLS-1$
      data.yo = control.getDouble("yorigin");           //$NON-NLS-1$
      data.an = control.getDouble("angle"); 						//$NON-NLS-1$  // angle in degrees
      data.xs = control.getDouble("xscale");            //$NON-NLS-1$
      data.ys = control.getDouble("yscale");            //$NON-NLS-1$
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
