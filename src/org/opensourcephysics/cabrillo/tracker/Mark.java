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

import java.awt.*;

/**
 * A Mark draws itself and has rectangular bounds. A track Step
 * has a Mark for each tracker panel on which it is drawn. The Mark
 * is created by the Step's Footprint.
 *
 * @author Douglas Brown
 */
public interface Mark {

  /**
   * Draws this object.
   *
   * @param g the Graphics2D context
   * @param highlighted <code>true</code> to draw a highlighted version
   */
  public void draw(Graphics2D g, boolean highlighted);

  /**
   * Gets the bounds of this object.
   *
   * @param highlighted <code>true</code> to get the highlighted bounds
   * @return the bounding rectangle
   */
  public Rectangle getBounds(boolean highlighted);

}
