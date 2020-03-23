/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingFrame;

/**
 * <p>Title: DrawingFrame3D</p>
 * <p>Description: DrawingFrame3D is the recommended frame for a DrawingPanel3D</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface DrawingFrame3D {
  // ---------------------------------
  // Adding the panel
  // ---------------------------------

  /**
   *  Adds the drawing panel to the frame. The panel is added to the center
   *  of the frame's content pane.
   *
   * @param  drawingPanel
   */
  public void setDrawingPanel3D(DrawingPanel3D drawingPanel);

  /**
   * Gets the DrawingPanel3D
   * @return DrawingPanel3D
   */
  public DrawingPanel3D getDrawingPanel3D();

  // ---------------------------------
  // Accessing the JFrame inside it
  // ---------------------------------

  /**
   * Getting the pointer to the real JFrame in it
   * @return JFrame
   */
  public javax.swing.JFrame getJFrame();

  /**
   * Showing and hiding the frame.
   * Usually equals to getJFrame().setVisible(visibility);
   * @param visibility boolean
   */
  public void setVisible(boolean visibility);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static class Loader implements org.opensourcephysics.controls.XML.ObjectLoader {
    public Object createObject(XMLControl control) {
      DrawingFrame frame = new DrawingFrame();
      frame.setTitle(control.getString("title"));                                    //$NON-NLS-1$
      frame.setLocation(control.getInt("location x"), control.getInt("location y")); //$NON-NLS-1$ //$NON-NLS-2$
      frame.setSize(control.getInt("width"), control.getInt("height"));              //$NON-NLS-1$ //$NON-NLS-2$
      if(control.getBoolean("showing")) { //$NON-NLS-1$
        frame.setVisible(true);
      }
      return frame;
    }

    public void saveObject(XMLControl control, Object obj) {
      DrawingFrame3D frame3D = (DrawingFrame3D) obj;
      javax.swing.JFrame frame = frame3D.getJFrame();
      control.setValue("title", frame.getTitle());                    //$NON-NLS-1$
      control.setValue("showing", frame.isShowing());                 //$NON-NLS-1$
      control.setValue("location x", frame.getLocation().x);          //$NON-NLS-1$
      control.setValue("location y", frame.getLocation().y);          //$NON-NLS-1$
      control.setValue("width", frame.getSize().width);               //$NON-NLS-1$
      control.setValue("height", frame.getSize().height);             //$NON-NLS-1$
      control.setValue("drawing panel", frame3D.getDrawingPanel3D()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      DrawingFrame3D frame3D = ((DrawingFrame3D) obj);
      javax.swing.JFrame frame = frame3D.getJFrame();
      DrawingPanel3D panel = frame3D.getDrawingPanel3D();
      panel.removeAllElements();
      XMLControl panelControl = control.getChildControl("drawing panel"); //$NON-NLS-1$
      panelControl.loadObject(panel);
      panel.repaint();
      frame.setTitle(control.getString("title"));                                    //$NON-NLS-1$
      frame.setLocation(control.getInt("location x"), control.getInt("location y")); //$NON-NLS-1$ //$NON-NLS-2$
      frame.setSize(control.getInt("width"), control.getInt("height"));              //$NON-NLS-1$ //$NON-NLS-2$
      if(control.getBoolean("showing")) { //$NON-NLS-1$
        frame.setVisible(true);
      }
      return obj;
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
