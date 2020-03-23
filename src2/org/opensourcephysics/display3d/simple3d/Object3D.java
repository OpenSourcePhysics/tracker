/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;

/**
 * <p>Title: Object3D class </p>
 * <p>Description: An Object3D is a minimal 3D graphical piece
 * that an Element can draw. The whole business consists is asking
 * each Element about its list of Object3D pieces, sorting them, and
 * finally draw them from back to front</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
class Object3D {
  private final Element element;
  private int index;
  private double distance = Double.NaN;

  /**
   * Gets the element that contains this Object3D
   * @return Element
   */
  final Element getElement() {
    return this.element;
  }

  /**
   * Gets the reference number of this Object3D in its parent list of objects
   * @return int
   */
  final int getIndex() {
    return this.index;
  }

  /**
   * Sets the reference number of this Object3D in its parent list of objects.
   * Only subclasses should use this.
   * @param _index
   */
  protected final void setIndex(int _index) {
    this.index = _index;
  }

  /**
   * Sets the distance to this Object3D
   * This distance provides a criterion to determine the order in
   * which to draw all Objects3D in a DrawingPanel3D.
   * If Double.isNaN(distance), the Object3D will be ignored
   * by the panel. This can be used by Drawables3D to hide a given
   * Object3D.
   * This number is also used to modify the color of an object, so that
   * objects far away look darker and objects closer look brighter.
   * @param aDistance double
   */
  final void setDistance(double aDistance) {
    this.distance = aDistance;
  }

  /**
   * Gets the distance to this Object3D
   * @return double
   * @see #setDistance()
   */
  final double getDistance() {
    return this.distance;
  }

  /**
   * Constructor for this Object3D
   * @param _element The Element this Object3D is part of
   * @param _index    An integer that identifies the object within the drawable
   */
  public Object3D(Element _element, int _index) {
    this.element = _element;
    this.index = _index;
    this.distance = Double.NaN;
  }

  /**
   * A comparator class to be used by DrawingPanel3D to sort Object3D s
   */
  static class Comparator3D implements java.util.Comparator<Object3D> {
    public int compare(Object3D o1, Object3D o2) {
      // try {
      if(o1.distance>o2.distance) {
        return -1;
      } else if(o1.distance<o2.distance) {
        return +1;
      } else {
        return 0;
      }
      // } catch (Exception _e) { return 0; } // Sometimes a NullPointerException happens
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
