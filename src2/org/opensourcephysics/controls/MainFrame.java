/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.util.Collection;
import javax.swing.JFrame;
import org.opensourcephysics.display.OSPFrame;

/**
 * A MainFrame contains the primary user interface for a program.
 *
 * The main frame closes all child windows when closed and will usually exit when it is closed.
 * An OSP program should have only one main frame.
 *
 * @author W. Christian
 * @version 1.0
 */
public interface MainFrame {
  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPFrame getMainFrame();

  /**
   * Gets the OSP Application that is controlled by this frame.
   * @return
   */
  public OSPApplication getOSPApp();

  /**
   * Adds a child frame that depends on the main frame.
   * Child frames are closed when this frame is closed.
   *
   * @param frame JFrame
   */
  public void addChildFrame(JFrame frame);

  /**
   * Clears the child frames from the main frame.
   */
  public void clearChildFrames();

  /**
   * Gets a copy of the ChildFrames collection.
   * @return Collection
   */
  public Collection<JFrame> getChildFrames();

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
