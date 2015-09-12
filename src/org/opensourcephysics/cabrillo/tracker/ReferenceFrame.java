/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;

import org.opensourcephysics.media.core.*;

/**
 * A ReferenceFrame is an image coordinate system with its origin
 * determined by the position of a PointMass.
 *
 * @author Douglas Brown
 */

public class ReferenceFrame extends ImageCoordSystem
                            implements PropertyChangeListener {

  // instance fields
  private PointMass originTrack;
  private ImageCoordSystem coords; // parent coords
  private boolean lockEnabled = false;
  private boolean originLocked;

  /**
   * Constructs a ReferenceFrame with a default initial length.
   *
   * @param coords the image coordinate system providing angle and scale data
   * @param originTrack the point mass providing origin data
   */
  public ReferenceFrame(ImageCoordSystem coords, PointMass originTrack) {
    super(coords.getLength());
    setFixedOrigin(false);
    setFixedAngle(coords.isFixedAngle());
    setFixedScale(coords.isFixedScale());
    coords.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    originTrack.addPropertyChangeListener("step", this); //$NON-NLS-1$
    originTrack.addPropertyChangeListener("steps", this); //$NON-NLS-1$
    this.coords = coords;
    this.originTrack = originTrack;
    for (int n = 0; n < coords.getLength(); n++) {
      setScaleXY(n, coords.getScaleX(n), coords.getScaleY(n));
      setCosineSine(n, coords.getCosine(n),  coords.getSine(n));
    }
    setOrigins();
    lockEnabled = true;
  }

  /**
   * Overrides ImageCoordSystem setFixedOrigin method. Origin is never
   * fixed for a reference frame.
   *
   * @param fixed ignored
   * @param n the frame number
   */
  public void setFixedOrigin(boolean fixed, int n) {
    super.setFixedOrigin(false, n);
  }

  /**
   * Overrides setLocked method.
   *
   * @param locked <code>true</code> to lock the coordinate system
   */
  public void setLocked(boolean locked) {
    if (locked) {
      originLocked = originTrack.isLocked();
      originTrack.setLocked(true);
    }
    else {
      originTrack.setLocked(originLocked);
    }
    coords.setLocked(locked);
    super.setLocked(locked);
  }

  /**
   * Overrides isLocked method.
   *
   * @return <code>true</code> if this is locked
   */
  public boolean isLocked() {
    return lockEnabled && coords.isLocked();
  }

  /**
   * Responds to property change events. ReferenceFrame receives the
   * following events: "step" and "mass" from PointMass (origin), and
   * "transform" from ImageCoordSystem (angle and scale).
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("step") || name.equals("steps")) // from PointMass //$NON-NLS-1$ //$NON-NLS-2$
      setOrigins();
    else if (name.equals("transform")) {  // from ImageCoordSystem //$NON-NLS-1$
      Integer integer = (Integer)e.getNewValue();
      if (integer != null) {
        int n = integer.intValue();
        setScaleXY(n, coords.getScaleX(n), coords.getScaleY(n));
        setCosineSine(n, coords.getCosine(n),  coords.getSine(n));
        if (originTrack.isEmpty() && n == 0) setOrigins();
      }
      else {
        for (int n = 0; n < coords.getLength(); n++) {
          setScaleXY(n, coords.getScaleX(n), coords.getScaleY(n));
          setCosineSine(n, coords.getCosine(n),  coords.getSine(n));
        }
        if (originTrack.isEmpty()) setOrigins();
      }
    }
  }

  /**
   * Gets the parent image coordinate system. The parent coords are returned
   * after setting its angles and scales to match this.
   *
   * @return the parent image coordinate system
   */
  public ImageCoordSystem getCoords() {
    coords.removePropertyChangeListener("transform", this); //$NON-NLS-1$
    coords.setFixedAngle(isFixedAngle());
    coords.setFixedScale(isFixedScale());
    for (int n = 0; n < coords.getLength(); n++) {
      coords.setScaleXY(n, getScaleX(n), getScaleY(n));
      coords.setCosineSine(n, getCosine(n),  getSine(n));
    }
    coords.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    return coords;
  }

  /**
   * Gets the origin track of this reference frame.
   *
   * @return the point mass supplying origin data
   */
  public PointMass getOriginTrack() {
    return originTrack;
  }

  /**
   * Sets the origins of the image coordinate system.
   */
  protected void setOrigins() {
    firePropChange = false;
    // find starting origin position
    double x = coords.getOriginX(0); // in case origin is empty
    double y = coords.getOriginY(0);
    for (int n = 0; n < coords.getLength(); n++) {
      Step step = originTrack.getStep(n);
      if (step != null) {
        TPoint p = ((PositionStep)step).getPosition();
        x = p.getX();
        y = p.getY();
        break;
      }
    }
    // set coord system origins
    for (int n = 0; n < coords.getLength(); n++) {
      Step step = originTrack.getStep(n);
      if (step != null) {
        TPoint p = ((PositionStep)step).getPosition();
        x = p.getX();
        y = p.getY();
      }
      setOriginXY(n, x, y);
    }
    firePropChange = true;
    // fire property change for overall updates
    support.firePropertyChange("transform", null, null); //$NON-NLS-1$
  }

}
