/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

/**
* Title:        Contour
* Description:  The class <code>LineAccumulator</code> accumulates line drawing information and
* then draws all accumulated lines together. It is used as contour accumulator
* in the contour plot.
*
* The contour plot uses some code from the Surface Plotter package by Yanto Suryono.
*
* @author       Wolfgang Christian
* @version 1.0
*/
public final class ContourAccumulator {
  private ArrayList<LineRecord> accumulator;

  /**
   * The constructor of <code>LineAccumulator</code>
   */
  ContourAccumulator() {
    accumulator = new ArrayList<LineRecord>();
  }

  /**
   * Adds a line to the accumulator.
   *
   * @param x1 the first point's x coordinate
   * @param y1 the first point's y coordinate
   * @param x2 the second point's x coordinate
   * @param y2 the second point's y coordinate
   */
  public synchronized void addLine(int x1, int y1, int x2, int y2) {
    accumulator.add(new LineRecord(x1, y1, x2, y2));
  }

  /**
   * Clears accumulator.
   */
  public synchronized void clearAccumulator() {
    accumulator.clear();
  }

  /**
   * Draws all accumulated lines.
   *
   * @param g the graphics context to draw
   */
  public void drawAll(Graphics g) {
    ArrayList<LineRecord> tempList = null;
    synchronized(this) {
      tempList = new ArrayList<LineRecord>(accumulator);
    }
    Iterator<LineRecord> it = tempList.iterator();
    while(it.hasNext()) {
      LineRecord line = it.next();
      g.drawLine(line.x1, line.y1, line.x2, line.y2);
    }
  }

}

/**
 * Represents a stright line.
 * Used by <code>LineAccumulator</code> class.
 *
 * @see LineAccumulator
 */
class LineRecord extends Object {
  /**
   * @param x1 the first point's x coordinate
   */
  public int x1;

  /**
   * @param y1 the first point's y coordinate
   */
  public int y1;

  /**
   * @param x2 the second point's x coordinate
   */
  public int x2;

  /**
   * @param y2 the second point's y coordinate
   */
  public int y2;

  /**
   * The constructor of <code>LineRecord</code>
   *
   * @param x1 the first point's x coordinate
   * @param y1 the first point's y coordinate
   * @param x2 the second point's x coordinate
   * @param y2 the second point's y coordinate
   */
  LineRecord(int x1, int y1, int x2, int y2) {
    super();
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
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
