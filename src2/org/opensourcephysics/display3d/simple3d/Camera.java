/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.numerics.Quaternion;
import org.opensourcephysics.numerics.Transformation;
import org.opensourcephysics.numerics.VectorMath;

public class Camera implements org.opensourcephysics.display3d.core.Camera {
  static private final double ratioToScreen = 2.5, ratioToFocus = 2.0;
  static private final double[] vertical = {0, 0, 1};
  static final int CHANGE_ANY = 0;
  static final int CHANGE_MODE = 1;
  static final int CHANGE_POSITION = 2;
  static final int CHANGE_FOCUS = 3;
  static final int CHANGE_ROTATION = 4;
  static final int CHANGE_SCREEN = 5;
  static final int CHANGE_ANGLES = 6;
  // Configuration variables
  private int projectionMode = org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE;
  private double posX, posY, posZ;
  private double focusX, focusY, focusZ;
  private double distanceToScreen, rotationAngle = 0;
  private double alpha = 0.0, beta = 0.0;
  // Implementation variables
  private double distanceToFocus, panelMaxSizeConstant;
  double cosAlpha = 1, sinAlpha = 0, cosBeta = 1, sinBeta = 0;
  private double cosRot = 1, sinRot = 0;
  private double[] e1, e2, e3;
  private Projection projection = new Projection();
  private Quaternion rotation = new Quaternion(1, 0, 0, 0);

  /**
   * The DrawingPanel3D to which it belongs.
   * This is needed to report to it any change that implies a call to update()
   */
  private DrawingPanel3D panel;

  Camera(DrawingPanel3D aPanel) {
    this.panel = aPanel;
  }

  /**
   * Only for the use of the XMLLoader for DrawingPanel3D!
   * Sets the panel of this camera
   * @param aPanel DrawingPanel3D
   */
  void setPanel(DrawingPanel3D aPanel) {
    this.panel = aPanel;
  }

  // -----------------------------
  // Implementation of Camera
  // ----------------------------
  public void setProjectionMode(int mode) {
    projectionMode = mode;
    if(panel!=null) {
      panelMaxSizeConstant = panel.getMaximum3DSize()*0.01;
      panel.cameraChanged(CHANGE_MODE);
    }
  }

  final public int getProjectionMode() {
    return projectionMode;
  }

  public void reset() {
    double[] center = panel.getCenter();
    focusX = center[0];
    focusY = center[1];
    focusZ = center[2];
    panelMaxSizeConstant = panel.getMaximum3DSize();
    rotationAngle = 0;
    cosRot = 1;
    sinRot = 0;
    distanceToScreen = ratioToScreen*panelMaxSizeConstant;
    distanceToFocus = ratioToFocus*panelMaxSizeConstant;
    posX = center[0]+distanceToFocus;
    posY = center[1];
    posZ = center[2];
    alpha = 0;
    cosAlpha = 1;
    sinAlpha = 0;
    beta = 0;
    cosBeta = 1;
    sinBeta = 0;
    e1 = new double[] {-1, 0, 0};
    e2 = new double[] {0, 1, 0};
    e3 = new double[] {0, 0, 1};
    panelMaxSizeConstant *= 0.01;
    panel.cameraChanged(CHANGE_ANY);
  }

  public void setXYZ(double x, double y, double z) {
    posX = x;
    posY = y;
    posZ = z;
    updateCamera(CHANGE_POSITION);
  }

  public void setXYZ(double[] point) {
    setXYZ(point[0], point[1], point[2]);
  }

  final public double getX() {
    return posX;
  }

  final public double getY() {
    return posY;
  }

  final public double getZ() {
    return posZ;
  }

  public void setFocusXYZ(double x, double y, double z) {
    focusX = x;
    focusY = y;
    focusZ = z;
    updateCamera(CHANGE_FOCUS);
  }

  public void setFocusXYZ(double[] point) {
    setFocusXYZ(point[0], point[1], point[2]);
  }

  final public double getFocusX() {
    return focusX;
  }

  final public double getFocusY() {
    return focusY;
  }

  final public double getFocusZ() {
    return focusZ;
  }

  public void setRotation(double angle) {
    rotationAngle = angle;
    cosRot = Math.cos(rotationAngle/2);
    sinRot = Math.sin(rotationAngle/2);
    updateCamera(CHANGE_ROTATION);
  }

  final public double getRotation() {
    return rotationAngle;
  }

  public void setDistanceToScreen(double distance) {
    distanceToScreen = distance;
    if(panel!=null) {
      panel.cameraChanged(CHANGE_SCREEN);
    }
  }

  final public double getDistanceToScreen() {
    return distanceToScreen;
  }

  public void setAzimuth(double angle) {
    alpha = angle;
    cosAlpha = Math.cos(alpha);
    sinAlpha = Math.sin(alpha);
    updateCamera(CHANGE_ANGLES);
  }

  final public double getAzimuth() {
    return alpha;
  }

  public void setAltitude(double angle) {
    beta = angle;
    if(beta<-Math.PI/2) {
      beta = -Math.PI/2;
    } else if(beta>Math.PI/2) {
      beta = Math.PI/2;
    }
    cosBeta = Math.cos(beta);
    sinBeta = Math.sin(beta);
    updateCamera(CHANGE_ANGLES);
  }

  final public double getAltitude() {
    return beta;
  }

  public void setAzimuthAndAltitude(double azimuth, double altitude) {
    alpha = azimuth;
    beta = altitude;
    if(beta<-Math.PI/2) {
      beta = -Math.PI/2;
    } else if(beta>Math.PI/2) {
      beta = Math.PI/2;
    }
    cosAlpha = Math.cos(alpha);
    sinAlpha = Math.sin(alpha);
    cosBeta = Math.cos(beta);
    sinBeta = Math.sin(beta);
    updateCamera(CHANGE_ANGLES);
  }

  final public Transformation getTransformation() {
    return projection;
  }

  public void copyFrom(org.opensourcephysics.display3d.core.Camera camera) {
    projectionMode = camera.getProjectionMode();
    if(panel!=null) {
      panelMaxSizeConstant = panel.getMaximum3DSize()*0.01;
    }
    posX = camera.getX();
    posY = camera.getY();
    posZ = camera.getZ();
    focusX = camera.getFocusX();
    focusY = camera.getFocusY();
    focusZ = camera.getFocusZ();
    rotationAngle = camera.getRotation();
    cosRot = Math.cos(rotationAngle/2);
    sinRot = Math.sin(rotationAngle/2);
    distanceToScreen = camera.getDistanceToScreen();
    updateCamera(CHANGE_ANY);
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  private void updateCamera(int change) {
    switch(change) {
       case CHANGE_POSITION :
       case CHANGE_FOCUS :
         distanceToFocus = computeCameraVectors();
         alpha = Math.atan2(-e1[1], -e1[0]);
         beta = Math.atan2(-e1[2], Math.abs(e1[0]));
         cosAlpha = Math.cos(alpha);
         sinAlpha = Math.sin(alpha);
         cosBeta = Math.cos(beta);
         sinBeta = Math.sin(beta);
         break;
       case CHANGE_ROTATION :
         computeCameraVectors(); // e2 and e3 are different (rotated)
         break;
       case CHANGE_ANGLES :
         posX = focusX+distanceToFocus*cosBeta*cosAlpha;
         posY = focusY+distanceToFocus*cosBeta*sinAlpha;
         posZ = focusZ+distanceToFocus*sinBeta;
         computeCameraVectors();
         break;
       case CHANGE_ANY :
         distanceToFocus = computeCameraVectors();
         alpha = Math.atan2(-e1[1], -e1[0]);
         beta = Math.atan2(-e1[2], Math.abs(e1[0]));
         cosAlpha = Math.cos(alpha);
         sinAlpha = Math.sin(alpha);
         cosBeta = Math.cos(beta);
         sinBeta = Math.sin(beta);
         computeCameraVectors(); // e2 and e3 are different (rotated)
         break;
    }
    if(panel!=null) {
      panel.cameraChanged(change);
    }
  }

  private double computeCameraVectors() {
    e1 = new double[] {focusX-posX, focusY-posY, focusZ-posZ};
    double magnitudeE1 = VectorMath.magnitude(e1);
    for(int i = 0; i<e1.length; i++) {
      e1[i] /= magnitudeE1;
    }
    e2 = VectorMath.cross3D(e1, vertical);
    double magnitude = VectorMath.magnitude(e2);
    for(int i = 0; i<e2.length; i++) {
      e2[i] /= magnitude;
    }
    e3 = VectorMath.cross3D(e2, e1);
    magnitude = VectorMath.magnitude(e3);
    for(int i = 0; i<e3.length; i++) {
      e3[i] /= magnitude;
    }
    // Finally apply the rotation
    rotation.setCoordinates(cosRot, e1[0]*sinRot, e1[1]*sinRot, e1[2]*sinRot);
    rotation.direct(e2);
    rotation.direct(e3);
    return magnitudeE1;
  }

  // -------------------------------------
  // Projection methods
  // -------------------------------------

  /**
   * Whether the projection mode is three-dimensional
   * @return boolean
   */
  boolean is3dMode() {
    switch(projectionMode) {
       case MODE_PLANAR_XY :
       case MODE_PLANAR_XZ :
       case MODE_PLANAR_YZ :
         return false;
       default :
         return true;
    }
  }

  /**
   * Computes the projection of a size at a given point.
   * For internal use of DrawingPanel3D only
   */
  double[] projectSize(double[] p, double[] size, double[] pixelSize) {
    switch(projectionMode) {
       case MODE_PLANAR_XY :
         pixelSize[0] = size[0];
         pixelSize[1] = size[1];
         return pixelSize;
       case MODE_PLANAR_XZ :
         pixelSize[0] = size[0];
         pixelSize[1] = size[2];
         return pixelSize;
       case MODE_PLANAR_YZ :
         pixelSize[0] = size[1];
         pixelSize[1] = size[2];
         return pixelSize;
       case MODE_NO_PERSPECTIVE :
       case MODE_PERSPECTIVE_OFF :
         pixelSize[0] = Math.max(size[0], size[1]);
         pixelSize[1] = size[2];
         return pixelSize;
       default :
       case MODE_PERSPECTIVE :
       case MODE_PERSPECTIVE_ON :
         double factor = (p[0]-posX)*e1[0]+(p[1]-posY)*e1[1]+(p[2]-posZ)*e1[2];
         if(Math.abs(factor)<panelMaxSizeConstant) {
           factor = panelMaxSizeConstant; // Avoid division by zero
         }
         factor = distanceToScreen/factor;
         pixelSize[0] = Math.max(size[0], size[1])*factor;
         pixelSize[1] = size[2]*factor;
         return pixelSize;
    }
  }

  private class Projection implements org.opensourcephysics.numerics.Transformation {
    public Object clone() {
      try {
        return super.clone();
      } catch(CloneNotSupportedException exc) {
        exc.printStackTrace();
        return null;
      }
    }

    public double[] direct(double[] p) {
      switch(projectionMode) {
         case MODE_PLANAR_XY :
           p[0] = p[0]-focusX;
           p[1] = p[1]-focusY;
           p[2] = 1.0-(p[2]-focusZ)/distanceToFocus;
           return p;
         case MODE_PLANAR_XZ : {
           double aux = p[1];
           p[0] = p[0]-focusX;
           p[1] = p[2]-focusZ;
           p[2] = 1.0-(aux-focusY)/distanceToFocus;
           return p;
         }
         case MODE_PLANAR_YZ : {
           double aux = p[0];
           p[0] = p[1]-focusY;
           p[1] = p[2]-focusZ;
           p[2] = 1.0-(aux-focusX)/distanceToFocus;
           return p;
         }
         case MODE_NO_PERSPECTIVE :
         case MODE_PERSPECTIVE_OFF : {
           p[0] -= posX;
           p[1] -= posY;
           p[2] -= posZ;
           double aux1 = VectorMath.dot(p, e1);
           double aux2 = VectorMath.dot(p, e2);
           p[1] = VectorMath.dot(p, e3);
           p[0] = aux2;
           p[2] = aux1/distanceToFocus;
           return p;
         }
         default :
         case MODE_PERSPECTIVE :
         case MODE_PERSPECTIVE_ON : {
           p[0] -= posX;
           p[1] -= posY;
           p[2] -= posZ;
           double factor = VectorMath.dot(p, e1), aux1 = factor;
           if(Math.abs(factor)<panelMaxSizeConstant) {
             factor = panelMaxSizeConstant; // Avoid division by zero
           }
           // if (aux1<0) aux1 = Double.NaN;
           factor = distanceToScreen/factor;
           double aux2 = VectorMath.dot(p, e2)*factor;
           p[1] = VectorMath.dot(p, e3)*factor;
           p[0] = aux2;
           p[2] = aux1/distanceToFocus;
           return p;
         }
      }
    }

    public double[] inverse(double[] point) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public static XML.ObjectLoader getLoader() {
    return new CameraLoader();
  }

  protected static class CameraLoader extends org.opensourcephysics.display3d.core.Camera.Loader {
    public Object createObject(XMLControl control) {
      return new Camera((DrawingPanel3D) null);
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
