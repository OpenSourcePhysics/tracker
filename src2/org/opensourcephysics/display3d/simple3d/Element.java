/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.util.ArrayList;
import java.util.Iterator;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display3d.core.interaction.InteractionEvent;
import org.opensourcephysics.display3d.core.interaction.InteractionListener;
import org.opensourcephysics.numerics.Matrix3DTransformation;
import org.opensourcephysics.numerics.Quaternion;
import org.opensourcephysics.numerics.Transformation;

/**
 *
 * <p>Title: Element</p>
 *
 * <p>Interaction: An Element includes the following targets:</p>
 * <ul>
 *   <li> TARGET_POSITION : Allows the element to be repositioned
 *   <li> TARGET_SIZE : Allows the element to be resized
 * </ul>
 * The actual position (and implementation) of the target depends on the
 * element.
 *
 * <p>Copyright: Open Source Physics project</p>
 *
 * @author Francisco Esquembre
 * @version June 2005
 */
public abstract class Element implements org.opensourcephysics.display3d.core.Element {
  static final int SENSIBILITY = 5;                     // The accuracy for the mouse to find a point on the screen
  // Configuration variables
  private boolean visible = true;                       // is the object visible?
  private double x = 0.0, y = 0.0, z = 0.0;             // position of the element
  private double sizeX = 1.0, sizeY = 1.0, sizeZ = 1.0; // the size of the element in each dimension
  private String name = "";                             //$NON-NLS-1$
  private Transformation transformation = null;
  private Style style = new Style(this);
  private Group group = null;
  private double factorX = 1.0;
  private double factorY = 1.0;
  private double factorZ = 1.0;
  // Implementation variables
  private boolean elementChanged = true, needsToProject = true;
  private DrawingPanel3D panel;
  // Variables for interaction
  private ArrayList<InteractionListener> listeners = new ArrayList<InteractionListener>();
  protected final InteractionTarget targetPosition = new InteractionTarget(this, TARGET_POSITION);
  protected final InteractionTarget targetSize = new InteractionTarget(this, TARGET_SIZE);

  /**
   * Returns the DrawingPanel3D in which it (or its final ancestor group)
   * is displayed.
   * @return DrawingPanel3D
   */
  final public DrawingPanel3D getDrawingPanel3D() {
    Element el = this;
    while(el.group!=null) {
      el = el.group;
    }
    return el.panel;
  }

  /**
   * To be used internally by DrawingPanel3D only! Sets the panel for this element.
   * @param _panel DrawingPanel3D
   */
  void setPanel(DrawingPanel3D _panel) {
    this.panel = _panel;
    this.factorX = _panel.getScaleFactorX();
    this.factorY = _panel.getScaleFactorY();
    this.factorZ = _panel.getScaleFactorZ();
    elementChanged = true;
  }

  /**
   * Returns the group to which the element belongs
   * @return Group Returns null if it doesn't belong to a group
   */
  final Group getGroup() {
    return group;
  }

  /**
   * Returns the top group to which the element belongs
   * @return Group Returns null if it doesn't belong to a group
   *
  final Group getTopGroup() {
    Group gr = group;
    if(gr==null) {
      return null;
    }
    while(gr.getGroup()!=null) {
      gr = gr.getGroup();
    }
    return gr;
  }
*/

  /**
   * To be used internally by Group only! Sets the group of this element.
   * @param _group Group
   */
  void setGroup(Group _group) {
    this.group = _group;
    elementChanged = true;
  }

  protected int getAxesMode() {
    if(panel!=null) {
      return panel.getAxesMode();
    }
    return org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XYZ;
  }

  // ----------------------------------------
  // Name of the element
  // ----------------------------------------
  public void setName(String aName) {
    this.name = aName;
  }

  final public String getName() {
    return this.name;
  }

  // ----------------------------------------
  // Position of the element
  // ----------------------------------------
  public void setX(double x) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.y = x*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.z = x*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.y = x*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.z = x*this.factorZ;
         break;
       default :
         this.x = x*this.factorX;
    }
    elementChanged = true;
  }

  final public double getX() {
    return this.x/this.factorX;
  }

  public void setY(double y) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.z = y*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.x = y*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.x = y*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.z = y*this.factorZ;
         break;
       default :
         this.y = y*this.factorY;
    }
    elementChanged = true;
  }

  final public double getY() {
    return this.y/this.factorY;
  }

  public void setZ(double z) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.y = z*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.y = z*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.x = z*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.x = z*this.factorX;
         break;
       default :
         this.z = z*this.factorZ;
    }
    elementChanged = true;
  }

  final public double getZ() {
    return this.z/this.factorZ;
  }

  public void setXYZ(double x, double y, double z) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.x = x*this.factorX;
         this.z = y*this.factorZ;
         this.y = z*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.y = x;
         this.x = y;
         this.z = z;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.z = x*this.factorZ;
         this.x = y*this.factorX;
         this.y = z*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.y = x*this.factorY;
         this.z = y*this.factorZ;
         this.x = z*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.z = x*this.factorZ;
         this.y = y*this.factorY;
         this.x = z*this.factorX;
         break;
       default :
         this.x = x*this.factorX;
         this.y = y*this.factorY;
         this.z = z*this.factorZ;
    }
    elementChanged = true;
  }

  public void setXYZ(double[] pos) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.x = pos[0]*this.factorX;
         this.z = pos[1]*this.factorZ;
         if(pos.length>=3) {
           this.y = pos[2]*this.factorY;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.y = pos[0]*this.factorY;
         this.x = pos[1]*this.factorX;
         if(pos.length>=3) {
           this.z = pos[2]*this.factorZ;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.z = pos[0]*this.factorZ;
         this.x = pos[1]*this.factorX;
         if(pos.length>=3) {
           this.y = pos[2]*this.factorY;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.y = pos[0]*this.factorY;
         this.z = pos[1]*this.factorZ;
         if(pos.length>=3) {
           this.x = pos[2]*this.factorX;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.z = pos[0]*this.factorZ;
         this.y = pos[1]*this.factorY;
         if(pos.length>=3) {
           this.x = pos[2]*this.factorX;
         }
         break;
       default :
         this.x = pos[0]*this.factorX;
         this.y = pos[1]*this.factorY;
         if(pos.length>=3) {
           this.z = pos[2]*this.factorZ;
         }
    }
    elementChanged = true;
  }

  /**
   * Returns the extreme points of a box that contains the element.
   * @param min double[] A previously allocated double[3] array that will hold
   * the minimum point
   * @param max double[] A previously allocated double[3] array that will hold
   * the maximum point
   */
  void getExtrema(double[] min, double[] max) {
    min[0] = -0.5;
    max[0] = 0.5;
    min[1] = -0.5;
    max[1] = 0.5;
    min[2] = -0.5;
    max[2] = 0.5;
    sizeAndToSpaceFrame(min);
    sizeAndToSpaceFrame(max);
  }

  // ----------------------------------------
  // Size of the element
  // ----------------------------------------
  public void setSizeX(double sizeX) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.sizeY = sizeX*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.sizeZ = sizeX*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.sizeY = sizeX*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.sizeZ = sizeX*this.factorZ;
         break;
       default :
         this.sizeX = sizeX*this.factorY;
    }
    elementChanged = true;
  }

  final public double getSizeX() {
    return this.sizeX;
  }

  public void setSizeY(double sizeY) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.sizeZ = sizeY*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.sizeX = sizeY*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.sizeX = sizeY*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.sizeZ = sizeY*this.factorZ;
         break;
       default :
         this.sizeY = sizeY*this.factorY;
    }
    elementChanged = true;
  }

  final public double getSizeY() {
    return this.sizeY;
  }

  public void setSizeZ(double sizeZ) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.sizeY = sizeZ*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.sizeY = sizeZ*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.sizeX = sizeZ*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.sizeX = sizeZ*this.factorX;
         break;
       default :
         this.sizeZ = sizeZ*this.factorZ;
    }
    elementChanged = true;
  }

  final public double getSizeZ() {
    return this.sizeZ;
  }

  public void setSizeXYZ(double sizeX, double sizeY, double sizeZ) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.sizeX = sizeX*this.factorX;
         this.sizeZ = sizeY*this.factorY;
         this.sizeY = sizeZ*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.sizeY = sizeX*this.factorY;
         this.sizeX = sizeY*this.factorX;
         this.sizeZ = sizeZ*this.factorZ;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.sizeZ = sizeX*this.factorZ;
         this.sizeX = sizeY*this.factorX;
         this.sizeY = sizeZ*this.factorY;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.sizeY = sizeX*this.factorY;
         this.sizeZ = sizeY*this.factorZ;
         this.sizeX = sizeZ*this.factorX;
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.sizeZ = sizeX*this.factorZ;
         this.sizeY = sizeY*this.factorY;
         this.sizeX = sizeZ*this.factorX;
         break;
       default :
         this.sizeX = sizeX*this.factorX;
         this.sizeY = sizeY*this.factorY;
         this.sizeZ = sizeZ*this.factorZ;
    }
    elementChanged = true;
  }

  public void setSizeXYZ(double[] size) {
    switch(getAxesMode()) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         this.sizeX = size[0]*this.factorX;
         this.sizeZ = size[1]*this.factorZ;
         if(size.length>=3) {
           this.sizeY = size[2]*this.factorY;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         this.sizeY = size[0]*this.factorY;
         this.sizeX = size[1]*this.factorX;
         if(size.length>=3) {
           this.sizeZ = size[2]*this.factorZ;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         this.sizeZ = size[0]*this.factorZ;
         this.sizeX = size[1]*this.factorX;
         if(size.length>=3) {
           this.sizeY = size[2]*this.factorY;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         this.sizeY = size[0]*this.factorY;
         this.sizeZ = size[1]*this.factorZ;
         if(size.length>=3) {
           this.sizeX = size[2]*this.factorX;
         }
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         this.sizeZ = size[0]*this.factorZ;
         this.sizeY = size[1]*this.factorY;
         if(size.length>=3) {
           this.sizeX = size[2]*this.factorX;
         }
         break;
       default :
         this.sizeX = size[0]*this.factorX;
         this.sizeY = size[1]*this.factorY;
         if(size.length>=3) {
           this.sizeZ = size[2]*this.factorZ;
         }
    }
    elementChanged = true;
  }

  /**
   * Returns the diagonal size of the element, i.e., Math.sqrt(sizeX*sizeX+sizeY*sizeY+sizeZ*sizeZ)
   * @return double
   */
  final double getDiagonalSize() {
    return Math.sqrt(sizeX*sizeX+sizeY*sizeY+sizeZ*sizeZ);
  }

  /**
   * Returns whether the element has changed significantly.
   * This can be used by implementing classes to help improve performance.
   * @return boolean
   */
  final boolean hasChanged() {
    Element el = this;
    while(el!=null) {
      if(el.elementChanged) {
        return true;
      }
      el = el.group;
    }
    return false;
  }

  /**
   * Whether this element has changed position, size, transformation or style
   * since the last time it was displayed.
   * @return boolean
   */
  boolean getElementChanged() {
    return elementChanged;
  }

  /**
   * Tells the element whether it has a change that demands some
   * kind of computation or, the other way round, someone took
   * care of all possible changes.
   * Typically used by subclasses when they change something or
   * make all needed computations.
   * @param change Whether the element has changed
   */
  final void setElementChanged(boolean change) {
    elementChanged = change;
  }

  // -------------------------------------
  // Visibility and style
  // -------------------------------------
  public void setVisible(boolean _visible) {
    this.visible = _visible;
  }

  final public boolean isVisible() {
    return this.visible;
  }

  /**
   * Returns the real visibility status of the element, which will be false if
   * it belongs to an invisible group
   * @return boolean
   */
  final protected boolean isReallyVisible() {
    Element el = this.group;
    while(el!=null) {
      if(!el.visible) {
        return false;
      }
      el = el.group;
    }
    return this.visible;
  }

  final public org.opensourcephysics.display3d.core.Style getStyle() {
    return this.style;
  }

  /**
   * Gets the real Style. This is more convenient and improves performance (a liiittle bit)
   * @return Style
   */
  final Style getRealStyle() {
    return this.style;
  }

  /**
   * Used by Style to notify possible changes.
   * @param styleThatChanged int
   */
  final void styleChanged(int styleThatChanged) {
    elementChanged = true;
  }

  // ----------------------------------------
  // Transformation of the element
  // ----------------------------------------
  public Transformation getTransformation() {
    if(transformation==null) {
      return null;
    }
    return(Transformation) transformation.clone();
  }

  public void setTransformation(org.opensourcephysics.numerics.Transformation transformation) {
    if(transformation==null) {
      this.transformation = null;
    } else {
      this.transformation = (Transformation) transformation.clone();
      if(transformation instanceof Quaternion) {
        Quaternion q = (Quaternion) this.transformation;
        double[] coordsBuffer = q.getCoordinates();
        //Switch the Axis Mode
        switch(getAxesMode()) {
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XYZ :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[1], coordsBuffer[2], coordsBuffer[3]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[1], coordsBuffer[3], coordsBuffer[2]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[2], coordsBuffer[1], coordsBuffer[3]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[2], coordsBuffer[3], coordsBuffer[1]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[3], coordsBuffer[1], coordsBuffer[2]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[3], coordsBuffer[2], coordsBuffer[1]);
             break;
           default :
             q.setCoordinates(coordsBuffer[0], coordsBuffer[1], coordsBuffer[2], coordsBuffer[3]);
        }
        this.transformation = new Quaternion(q);
      } else {
        Matrix3DTransformation mt = (Matrix3DTransformation) this.transformation;
        double[] q = new double[4];
        mt.toQuaternion(q);
        Quaternion qf = new Quaternion(q);
        //Switch the Axis Mode
        switch(getAxesMode()) {
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XYZ :
             qf.setCoordinates(q[0], q[1], q[2], q[3]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
             qf.setCoordinates(q[0], q[1], q[3], q[2]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
             qf.setCoordinates(q[0], q[2], q[1], q[3]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
             qf.setCoordinates(q[0], q[2], q[3], q[1]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
             qf.setCoordinates(q[0], q[3], q[1], q[2]);
             break;
           case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
             qf.setCoordinates(q[0], q[3], q[2], q[1]);
             break;
           default :
             qf.setCoordinates(q[0], q[1], q[2], q[3]);
        }
        this.transformation = new Quaternion(qf);
      }
    }
    elementChanged = true;
  }

  public double[] toSpaceFrame(double[] vector) {
    if(transformation!=null) {
      transformation.direct(vector);
    }
    vector[0] += x;
    vector[1] += y;
    vector[2] += z;
    Element el = group;
    while(el!=null) {
      vector[0] *= el.sizeX;
      vector[1] *= el.sizeY;
      vector[2] *= el.sizeZ;
      if(el.transformation!=null) {
        el.transformation.direct(vector);
      }
      vector[0] += el.x;
      vector[1] += el.y;
      vector[2] += el.z;
      el = el.group;
    }
    return vector;
  }

  public double[] toBodyFrame(double[] vector) throws UnsupportedOperationException {
    java.util.ArrayList<Element> elList = new java.util.ArrayList<Element>();
    Element el = this;
    do {
      elList.add(el);
      el = el.group;
    } while(el!=null);
    for(int i = elList.size()-1; i>=0; i--) { // Done in the reverse order
      el = elList.get(i);
      vector[0] -= el.x;
      vector[1] -= el.y;
      vector[2] -= el.z;
      if(el.transformation!=null) {
        el.transformation.inverse(vector);
      }
      if(el!=this) {
        if(el.sizeX!=0.0) {
          vector[0] /= el.sizeX;
        }
        if(el.sizeY!=0.0) {
          vector[1] /= el.sizeY;
        }
        if(el.sizeZ!=0.0) {
          vector[2] /= el.sizeZ;
        }
      }
    }
    return vector;
  }

  /**
   * Translates a point of the standard (0,0,0) to (1,1,1) element
   * to its real spatial coordinate. Thus, if the point has a coordinate of 1,
   * the result will be the size of the element.
   * @param vector the vector to be converted
   */
  final void sizeAndToSpaceFrame(double[] vector) {
    vector[0] *= sizeX;
    vector[1] *= sizeY;
    vector[2] *= sizeZ;
    toSpaceFrame(vector);
  }

  // ----------------------------------------------------
  // Needed by the drawing mechanism
  // ----------------------------------------------------

  /**
   * Returns an array of Objects3D to sort according to its distance and draw.
   */
  abstract Object3D[] getObjects3D();

  /**
   * Draws a given Object3D (indicated by its index).
   */
  abstract void draw(java.awt.Graphics2D g, int index);

  /**
   * Sketches the drawable
   */
  abstract void drawQuickly(java.awt.Graphics2D g);

  /**
   * Tells the element whether it should reproject its points because the panel
   * has changed its projection parameters. Or, the other way round,
   * if someone (typically methods in subclasses) took care of this already.
   */
  void setNeedToProject(boolean _need) {
    needsToProject = _need;
  }

  /**
   * Whether the element needs to project
   * @return boolean
   * @see #setNeedToProject(boolean)
   */
  final boolean needsToProject() {
    return needsToProject;
  }

  // ---------------------------------
  // Implementation of core.InteractionSource
  // ---------------------------------
  public org.opensourcephysics.display3d.core.interaction.InteractionTarget getInteractionTarget(int target) {
    switch(target) {
       case TARGET_POSITION :
         return targetPosition;
       case TARGET_SIZE :
         return targetSize;
    }
    return null;
  }

  public void addInteractionListener(InteractionListener listener) {
    if((listener==null)||listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  public void removeInteractionListener(InteractionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Invokes the interactionPerformed() methods on the registered
   * interaction listeners.
   * @param event InteractionEvent
   */
  final void invokeActions(InteractionEvent event) {
    Iterator<InteractionListener> it = listeners.iterator();
    while(it.hasNext()) {
      it.next().interactionPerformed(event);
    }
  }

  // TODO : make this method abstract

  /**
   * Gets the target that is under the (x,y) position of the screen
   * @param x int
   * @param y int
   * @return InteractionTarget
   */
  protected InteractionTarget getTargetHit(int x, int y) {
    return null;
  }

  /**
   * Returns the body coordinates of the specified hotspot
   * @return double[]
   */
  protected double[] getHotSpotBodyCoordinates(InteractionTarget target) {
    if(target==targetPosition) {
      return new double[] {0, 0, 0};
    }
    if(target==targetSize) {
      double[] c = new double[] {1, 1, 1};
      if(sizeX==0) {
        c[0] = 0.0;
      }
      if(sizeY==0) {
        c[1] = 0.0;
      }
      if(sizeZ==0) {
        c[2] = 0.0;
      }
      return c;
    }
    return null;
  }

  /**
   * This method returns the coordinates of the given target.
   * @param target InteractionTarget
   * @return double[]
   */
  final double[] getHotSpot(InteractionTarget target) {
    double[] coordinates = getHotSpotBodyCoordinates(target);
    if(coordinates!=null) {
      sizeAndToSpaceFrame(coordinates);
    }
    return coordinates;
  }

  /**
   * This method updates the position or size of the element
   * according to the position of the 3D cursor during the interaction.
   * Notice that, for targetSize, if any of the sizes of the element
   * is zero, this dimension cannot be changed.
   * @param target InteractionTarget The target interacted
   * @param point double[] The position of the cursor during the interaction
   */
  final void updateHotSpot(InteractionTarget target, double[] point) {
    Element gr = this.group; //getTopGroup();
    switch(target.getType()) {
       case org.opensourcephysics.display3d.core.Element.TARGET_POSITION :
         if(target.getAffectsGroup()&&(gr!=null)) { // Move the whole group
           double[] origin = getHotSpot(target);
           gr.setXYZ(gr.x+point[0]-origin[0], gr.y+point[1]-origin[1], gr.z+point[2]-origin[2]);
         } else {                                   // Move only the element
           double[] coordinates = new double[] {point[0], point[1], point[2]};
           groupInverseTransformations(coordinates);
           double[] origin = getHotSpotBodyCoordinates(target);
           origin[0] *= sizeX;
           origin[1] *= sizeY;
           origin[2] *= sizeZ;
           if(transformation!=null) {
             transformation.direct(origin);
           }
           setXYZ(coordinates[0]-origin[0], coordinates[1]-origin[1], coordinates[2]-origin[2]);
         }
         break;
       case org.opensourcephysics.display3d.core.Element.TARGET_SIZE :
         if(target.getAffectsGroup()&&(gr!=null)) { // Resize the whole group
           double[] coordinates = new double[] {point[0], point[1], point[2]};
           coordinates[0] -= gr.x;
           coordinates[1] -= gr.y;
           coordinates[2] -= gr.z;
           if(gr.transformation!=null) {
             gr.transformation.inverse(coordinates);
           }
           double[] origin = getHotSpotBodyCoordinates(target);
           elementDirectTransformations(origin);
           // If any of the dimensions is zero, a division by zero would occur.
           // Not dividing is not enough.
           if(origin[0]!=0) {
             coordinates[0] /= origin[0];
           } else {
             coordinates[0] = gr.sizeX;
           }
           if(origin[1]!=0) {
             coordinates[1] /= origin[1];
           } else {
             coordinates[1] = gr.sizeY;
           }
           if(origin[2]!=0) {
             coordinates[2] /= origin[2];
           } else {
             coordinates[2] = gr.sizeZ;
           }
           gr.setSizeXYZ(coordinates);
         } else {                                   // Resize only the element
           double[] coordinates = new double[] {point[0], point[1], point[2]};
           groupInverseTransformations(coordinates);
           coordinates[0] -= x;
           coordinates[1] -= y;
           coordinates[2] -= z;
           if(transformation!=null) {
             transformation.inverse(coordinates);
           }
           double[] origin = getHotSpotBodyCoordinates(target);
           for(int i = 0; i<3; i++) {
             if(origin[i]!=0) {
               coordinates[i] /= origin[i];
             }
           }
           setSizeXYZ(coordinates);
         }
    }
  }

  /**
   * All the inverse transformations of toBodyFrame except that of the
   * element itself
   * @param vector double[]
   * @throws UnsupportedOperationException
   */
  private final void groupInverseTransformations(double[] vector) throws UnsupportedOperationException {
    java.util.ArrayList<Element> elList = new java.util.ArrayList<Element>();
    Element el = this.group;
    while(el!=null) {
      elList.add(el);
      el = el.group;
    }
    for(int i = elList.size()-1; i>=0; i--) { // Done in the reverse order
      el = elList.get(i);
      vector[0] -= el.x;
      vector[1] -= el.y;
      vector[2] -= el.z;
      if(el.transformation!=null) {
        el.transformation.inverse(vector);
      }
      if(el.sizeX!=0.0) {
        vector[0] /= el.sizeX;
      }
      if(el.sizeY!=0.0) {
        vector[1] /= el.sizeY;
      }
      if(el.sizeZ!=0.0) {
        vector[2] /= el.sizeZ;
      }
    }
  }

  /**
   * All the direct transformations of sizeAndToSpaceFrame except that of the
   * top group
   * @param vector double[]
   */
  private final void elementDirectTransformations(double[] vector) {
    Element el = this;
    do {
      if(el.sizeX!=0) {
        vector[0] *= el.sizeX;
      }
      if(el.sizeY!=0) {
        vector[1] *= el.sizeY;
      }
      if(el.sizeZ!=0) {
        vector[2] *= el.sizeZ;
      }
      if(el.transformation!=null) {
        el.transformation.direct(vector);
      }
      vector[0] += el.x;
      vector[1] += el.y;
      vector[2] += el.z;
      el = el.group;
    } while((el!=null)&&(el.group!=null));
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public void loadUnmutableObjects(XMLControl control) {
    style = (Style) control.getObject("style"); //$NON-NLS-1$
    style.setElement(this);
    elementChanged = true;
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
