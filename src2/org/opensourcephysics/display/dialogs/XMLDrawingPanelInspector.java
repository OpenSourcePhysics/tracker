/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.dialogs;
import java.awt.Dimension;
import javax.swing.JDialog;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.DrawingPanel;

/**
 * XMLDrawingPanelInspector displays properties of the drawing panel using the OSP XML framework.
 * @author Doug Brown
 * @author Wolfgang Christian
 * @version 1.0
 */
public class XMLDrawingPanelInspector extends JDialog {
  static XMLDrawingPanelInspector inspector;

  /**
   * Constructor XMLDrawingPanelInspector
   */
  public XMLDrawingPanelInspector() {
    setSize(new Dimension(600, 300));
  }

  public static XMLDrawingPanelInspector getInspector(DrawingPanel dp) {
    if(inspector==null) {
      inspector = new XMLDrawingPanelInspector();
    }
    XMLControl control = new XMLControlElement(dp);
    XMLTreePanel treePanel = new XMLTreePanel(control);
    inspector.setContentPane(treePanel);
    inspector.setVisible(true);
    return inspector;
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
