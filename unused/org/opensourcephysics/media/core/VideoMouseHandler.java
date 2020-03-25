/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.InteractiveMouseHandler;
import org.opensourcephysics.display.InteractivePanel;

/**
 * This is a general purpose mouse handler for a video panel drawing TShapes.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoMouseHandler implements InteractiveMouseHandler {
  // instance fields
  Interactive iad = null;
  TPoint p = null;
  Rectangle bounds;

  /**
   * Handles a mouse action for a video panel.
   *
   * @param panel the video panel
   * @param e the mouse event
   */
  public void handleMouseAction(InteractivePanel panel, MouseEvent e) {
    if(!(panel instanceof VideoPanel)) {
      return;
    }
    VideoPanel vidPanel = (VideoPanel) panel;
    switch(vidPanel.getMouseAction()) {
       case InteractivePanel.MOUSE_PRESSED :
         if((iad!=null)&&(iad instanceof TPoint)) {
           p = (TPoint) iad;
           bounds = p.getBounds(vidPanel);
           p.setXY(vidPanel.getMouseX(), vidPanel.getMouseY());
           if(bounds!=null) {
             bounds.add(p.getBounds(vidPanel));
             vidPanel.repaint(bounds);
           } else {
             vidPanel.repaint();
           }
         }
         break;
       case InteractivePanel.MOUSE_RELEASED :
         iad = p = null;
         break;
       case InteractivePanel.MOUSE_DRAGGED :
         if(p!=null) {
           bounds = p.getBounds(vidPanel);
           p.setXY(vidPanel.getMouseX(), vidPanel.getMouseY());
           if(bounds!=null) {
             bounds.add(p.getBounds(vidPanel));
             vidPanel.repaint(bounds);
           } else {
             vidPanel.repaint();
           }
         }
         break;
       case InteractivePanel.MOUSE_MOVED :
         if(vidPanel.isDrawingInImageSpace()) {
           iad = vidPanel.getInteractive();
         }
         if((iad!=null)&&(iad instanceof TPoint)) {
           vidPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         } else {
           vidPanel.setMouseCursor(Cursor.getDefaultCursor());
         }
         break;
    }
    if(p==null) {
      vidPanel.hideMouseBox();
    } else {
      p.showCoordinates(vidPanel);
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
