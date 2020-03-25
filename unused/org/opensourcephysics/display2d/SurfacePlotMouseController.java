/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import org.opensourcephysics.display.DrawingPanel;

public class SurfacePlotMouseController implements MouseListener, MouseMotionListener {
  Object surfacePlot;
  DrawingPanel drawingPanel;

  /**
   * Constructor SurfacePlotMouseController
   * @param drawingPanel
   * @param surfacePlot
   */
  public SurfacePlotMouseController(DrawingPanel drawingPanel, Object surfacePlot) {
    this.surfacePlot = surfacePlot;
    this.drawingPanel = drawingPanel;
  }

  public void mouseEntered(MouseEvent e) {
    // empty 
  }

  public void mouseExited(MouseEvent e) {
    // empty 
  }

  public void mouseReleased(MouseEvent e) {
    if(surfacePlot instanceof SurfacePlot) {
      ((SurfacePlot) surfacePlot).mouseReleased(e, drawingPanel);
    } else if(surfacePlot instanceof ComplexSurfacePlot) {
      ((ComplexSurfacePlot) surfacePlot).mouseReleased(e, drawingPanel);
    }
  }

  public void mouseClicked(MouseEvent e) {
    // empty 
  }

  public void mousePressed(MouseEvent e) {
    if(surfacePlot instanceof SurfacePlot) {
      ((SurfacePlot) surfacePlot).mousePressed(e, drawingPanel);
    } else if(surfacePlot instanceof ComplexSurfacePlot) {
      ((ComplexSurfacePlot) surfacePlot).mousePressed(e, drawingPanel);
    }
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseDragged(MouseEvent e) {
    if(surfacePlot instanceof SurfacePlot) {
      ((SurfacePlot) surfacePlot).mouseDragged(e, drawingPanel);
    } else if(surfacePlot instanceof ComplexSurfacePlot) {
      ((ComplexSurfacePlot) surfacePlot).mouseDragged(e, drawingPanel);
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
