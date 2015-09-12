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

/**
 * A Derivative implements an algorithm for finding a first, second or both derivatives.
 *
 * @author Douglas Brown
 */
public interface Derivative {

  /**
   * Evaluates the derivative(s).
   * 
   * Input data:
   *    data[0] = parameters (int[] {spill, start, stepsize, count})
   *    data[1] = xData (double[])
   *    data[2] = yData (double[])
   *    data[3] = validData (boolean[])
   *    
   * Returned result:
   *    result[0] = firstDerivX (double[]) may be null
   *    result[1] = firstDerivY (double[]) may be null
   *    result[2] = secondDerivX (double[]) may be null
   *    result[3] = secondDerivY (double[]) may be null
   *    
   * Note: result values may be NaN if no derivative could be determined
   *
   * @param data the input data
   * @return Object[] result
   */
  public Object[] evaluate(Object[] data);

}
