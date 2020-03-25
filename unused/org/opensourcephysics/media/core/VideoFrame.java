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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This is a DrawingFrame with video menu items.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoFrame extends DrawingFrame {
  // instance fields
  protected Action openAction;
  protected Action saveAction;
  protected Action openVideoAction;
  protected JMenuItem openItem;
  protected JMenuItem saveAsItem;
  protected JMenuItem saveItem;
  protected JMenuItem openVideoItem;
  protected JMenuItem exitItem;

  /**
   *  VideoFrame constructor specifying the VideoPanel that will be placed
   *  in the center of the content pane.
   *
   * @param  vidPanel the VideoPanel
   */
  public VideoFrame(VideoPanel vidPanel) {
    this(MediaRes.getString("VideoFrame.Title"), vidPanel); //$NON-NLS-1$
  }

  /**
   *  VideoFrame constructor specifying the title and the VideoPanel that
   *  will be placed in the center of the content pane.
   *
   * @param  title the frame title
   * @param  vidPanel the VideoPanel
   */
  public VideoFrame(String title, VideoPanel vidPanel) {
    super(title, vidPanel);
    if(!OSPRuntime.appletMode) {
      createActions();
      modifyMenuBar();
    }
  }

  /**
   *  Modifies the menu bar.
   */
  protected void modifyMenuBar() {
    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // add open Item
    openItem = fileMenu.insert(openAction, 0);
    openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
    // add saveAsItem
    saveAsItem = new JMenuItem(MediaRes.getString("VideoFrame.MenuItem.SaveAs")); //$NON-NLS-1$
    fileMenu.insert(saveAsItem, 1);
    saveAsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VideoIO.save(null, (VideoPanel) drawingPanel);
      }

    });
    // add saveItem
    saveItem = fileMenu.insert(saveAction, 2);
    saveItem.setAccelerator(KeyStroke.getKeyStroke('S', keyMask));
    // add separator before PrintItem inherited from DrawingPanel
    fileMenu.insertSeparator(5);
    // add separator after PrintItem
    fileMenu.addSeparator();
    // add exitItem
    exitItem = new JMenuItem(MediaRes.getString("VideoFrame.MenuItem.Exit")); //$NON-NLS-1$
    fileMenu.add(exitItem);
    exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }

    });
    setJMenuBar(getJMenuBar());
  }

  protected void createActions() {
    openAction = new AbstractAction(MediaRes.getString("VideoFrame.MenuItem.Open"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        VideoIO.open(null, (VideoPanel) drawingPanel);
      }

    };
    saveAction = new AbstractAction(MediaRes.getString("VideoFrame.MenuItem.Save"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        VideoPanel vidPanel = (VideoPanel) drawingPanel;
        VideoIO.save(vidPanel.getDataFile(), vidPanel);
      }

    };
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
