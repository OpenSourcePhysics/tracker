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

/**
 * This manages an array of doubles.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DoubleArray {
  // instance fields
  private double[] map;
  private int length;

  /**
   * Constructs a DoubleArray object.
   * @param initialLength the initial length of the array
   * @param initialValue the initial value of all array elements
   */
  public DoubleArray(int initialLength, double initialValue) {
    length = initialLength;
    map = new double[length];
    fill(initialValue, 0);
  }

  /**
   * Gets the specified array element.
   * @param n the array index
   * @return the value at the specified index
   */
  public double get(int n) {
    if(n>=length) {
      setLength(n+1);
    }
    return map[n];
  }

  /**
   * Sets the specified array element.
   * @param n the array index
   * @param value the new value of the element
   * @return true if element was changed
   */
  public boolean set(int n, double value) {
    if(n>=length) {
      setLength(n+1);
    }
    boolean changed = map[n]!=value;
    map[n] = value;
    return changed;
  }

  /**
   * Sets the length of the array.
   * @param newLength the new length of the array
   */
  public void setLength(int newLength) {
    if((newLength==length)||(newLength<1)) {
      return;
    }
    double[] newMap = new double[newLength];
    System.arraycopy(map, 0, newMap, 0, Math.min(newLength, length));
    map = newMap;
    if(newLength>length) {
      double val = map[length-1];
      int n = length;
      length = newLength;
      fill(val, n);
    } else {
      length = newLength;
    }
  }

  /**
   * Fills elements of the array with the specified value.
   * @param value the value
   * @return true if at least one element was changed
   */
  public boolean fill(double value) {
    boolean changed = false;
    for(int n = length-1; n>=0; n--) {
      changed = changed||(map[n]!=value);
      map[n] = value;
    }
    return changed;
  }

  /**
   * Fills a subset of elements of the array with the specified value.
   * @param value the value
   * @param start the first index
   * @param end the last index
   * @return true if at least one element was changed
   */
  public boolean fill(double value, int start, int end) {
    boolean changed = false;
    end = Math.min(end, length-1);
    start = Math.max(0, start);
    for(int n=end; n>=start; n--) {
      changed = changed||(map[n]!=value);
      map[n] = value;
    }
    return changed;
  }

  //__________________________ private methods ___________________________

  /**
   * Fills elements of the the array with the specified value,
   * starting from a specified index.
   * @param value the value
   * @param startFrame the starting array index
   */
  private void fill(double value, int startFrame) {
    for(int n = startFrame; n<length; n++) {
      map[n] = value;
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
