/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.PlottingPanel;

/**
 * A DrawingFrame with an empty drawing panel.
 *
 * Programs should add drawables to this frame using the addDrawable method.
 * ClearData removes all drawables from this frame.
 *
 * @author W. Christian
 * @version 1.0
 */
public class DisplayFrame extends DrawingFrame {
  /**
   * Constructs the DisplayFrame with the given title.
   *
   * @param title String
   */
  public DisplayFrame(String title) {
    super(new InteractivePanel());
    setTitle(title);
    setAnimated(true);
    setAutoclear(true);
    drawingPanel.setSquareAspect(true); // objects retain their shape
  }

  /**
 * Constructs the DisplayFrame with the given labels and title.
 *
 * @param xlabel String
 * @param ylabel String
 * @param title String
 */
  public DisplayFrame(String xlabel, String ylabel, String title) {
    super(new PlottingPanel(xlabel, ylabel, null));
    setTitle(title);
    setAnimated(true);
    setAutoclear(true);
    drawingPanel.setSquareAspect(true); // objects retain their shape
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
