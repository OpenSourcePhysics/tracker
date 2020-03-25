/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;

/**
 * DrawingDialog: a dialog that contains a drawing panel.
 * Copyright:    Copyright (c) 2002
 * @author       Wolfgang Christian
 * @author       Paco Esquembre
 * @version 1.0
 */
public class DrawingDialog extends OSPDialog {
  protected DrawingPanel drawingPanel;

  /**
   * DrawingDialog constructor specifying the DrawingPanel that will be placed in
   * the center of the content pane.
   *
   * @param _ownerFrame
   * @param _drawingPanel
   */
  public DrawingDialog(Frame _ownerFrame, DrawingPanel _drawingPanel) {
    super(_ownerFrame, "Drawing Dialog", false); //$NON-NLS-1$
    if(OSPRuntime.appletMode) {
      keepHidden = true;
    }
    drawingPanel = _drawingPanel;
    Container contentPane = getContentPane();
    if(drawingPanel!=null) {
      contentPane.add(drawingPanel, BorderLayout.CENTER);
    }
    setSize(300, 300);
    setVisible(true);
  }

  /**
   * Sets the drawing panel into the center of the frame.
   * @param _drawingPanel
   */
  public void setDrawingPanel(DrawingPanel _drawingPanel) {
    if(drawingPanel!=null) {
      getContentPane().remove(drawingPanel);
    }
    drawingPanel = _drawingPanel;
    if(drawingPanel!=null) {
      getContentPane().add(drawingPanel, BorderLayout.CENTER);
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
