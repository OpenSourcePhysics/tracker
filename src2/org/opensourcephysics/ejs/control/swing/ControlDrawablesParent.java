/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.util.Enumeration;
import java.util.Vector;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.ejs.control.NeedsUpdate;

/**
 * A container to hold Drawables.
 * The base class for ControlDrawingParent, ControlDrawingPanel3D and ControlPlottingPanel
 * Its visual MUST be a (subclass of) DrawingPanel
 * It is prepared for interaction, if the visual is interactive
 */
public abstract class ControlDrawablesParent extends ControlSwingElement implements NeedsUpdate {
  // List of children that need to do something before repainting the panel
  private Vector<NeedsPreUpdate> preupdateList = new Vector<NeedsPreUpdate>();

  /**
   * Constructor ControlDrawablesParent
   * @param _visual
   */
  public ControlDrawablesParent(Object _visual) {
    super(_visual);
  }

  public void update() { // Ensure it will be updated
    // First prepare children that need to do something
    for(Enumeration<NeedsPreUpdate> e = preupdateList.elements(); e.hasMoreElements(); ) {
      e.nextElement().preupdate();
    }
    // Now render
    ((DrawingPanel) getVisual()).render();
    // ((DrawingPanel) getVisual()).repaint(); // OSP Update July 2003
  }

  public void addToPreupdateList(NeedsPreUpdate _child) {
    // System.out.println ("Adding "+_child);
    preupdateList.add(_child);
  }

  public void removeFromPreupdateList(NeedsPreUpdate _child) {
    preupdateList.remove(_child);
  }

  // ------------------------------------------------
  // This prepares it for interaction within Ejs
  // ------------------------------------------------
  public ControlDrawable getSelectedDrawable() {
    return null;
  }

} // End of class

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
