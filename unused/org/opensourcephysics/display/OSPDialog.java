/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import javax.swing.JApplet;
import javax.swing.JDialog;

/**
 * OSPDialog is a standard dialog that can remain hidden in applet mode.
 *
 * Copyright:    Copyright (c) 2002
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class OSPDialog extends JDialog {
  static int topx = 10;
  static int topy = 100;

  /** Set <I>true</I> if the program is an applet. */
  public static boolean appletMode = false;

  /** Field myApplet provides a static reference to an applet context
   * so that the document base and code base can be obtained in applet mode.
   */
  public static JApplet applet;

  /** The thread group that created this object.*/
  public ThreadGroup constructorThreadGroup = Thread.currentThread().getThreadGroup();
  protected boolean keepHidden = false;
  protected BufferStrategy strategy;

  /**
   * Constricts a dialog that can be kept hidden in applets.
   *
   * @param owner Dialog
   * @param title String
   * @param modal boolean
   */
  public OSPDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    if(appletMode) {
      keepHidden = true;
    }
    setLocation(topx, topy);
    Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    topx = Math.min(topx+20, (int) d.getWidth()-100);
    topy = Math.min(topy+20, (int) d.getHeight()-100);
  }

  /**
   * OSPDialog constructor with a title.
   * @param title
   */
  public OSPDialog(String title) {
    this(null, title, false);
  }

  /**
   * OSPDialog constructor.
   */
  public OSPDialog() {
    this(""); //$NON-NLS-1$
  }

  public void setSize(int width, int height) {
    super.setSize(width, height);
    validate();
  }

  /**
   * Shows the frame on the screen if the keep hidden flag is false.
   *
   * @deprecated
   */
  public void show() {
    if(!keepHidden) {
      super.show();
    }
  }

  /**
   * Sets the keepHidden flag.
   *
   * @param _keepHidden
   */
  public void setKeepHidden(boolean _keepHidden) {
    keepHidden = _keepHidden;
    // setVisible(!keepHidden);
    if(keepHidden) {
      this.setVisible(false);
    }
  }

  /**
   * Reads the keepHidden flag.
   *
   */
  public boolean isKeepHidden() {
    return keepHidden;
  }

  /**
   * Gets the ThreadGroup that constructed this frame.
   *
   * @return the ThreadGroup
   */
  public ThreadGroup getConstructorThreadGroup() {
    return constructorThreadGroup;
  }

  /**
   * Creates a BufferStrategy based on the capabilites of the hardware.
   */
  protected void createBufferStrategy() {
    createBufferStrategy(2);
    strategy = this.getBufferStrategy();
  }

  /**
   * Renders the frame using the current BufferStrategy.
   */
  public void render() {
    if((strategy)==null) {
      createBufferStrategy();
    }
    Graphics g = strategy.getDrawGraphics();
    paintComponents(g);
    g.dispose();
    strategy.show();
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
