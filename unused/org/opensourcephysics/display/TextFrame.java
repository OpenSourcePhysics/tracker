/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

public class TextFrame extends JFrame {
  HyperlinkListener hyperlinkListener;
  JTextPane textPane = new JTextPane() {
    public void paintComponent(Graphics g) {
      if(OSPRuntime.antiAliasText) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = g2.getRenderingHints();
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }
      super.paintComponent(g);
    }

  };
  JScrollPane textScroller;

  /**
   * Constructs an empty TextFrame.
   */
  public TextFrame() {
    this(null, null);
  }

  /**
   * Constructs the TextFrame with the given html resource.
   *
   * @param resourceName String
   */
  public TextFrame(String resourceName) {
    this(resourceName, null);
  }

  /**
   * Constructs the HTMLFrame with the given html resource at the given location.
   * The location is relative to the given class.
   *
   * @param resourceName String
   * @param location
   */
  public TextFrame(String resourceName, Class<?> location) {
    setSize(300, 300);
    textPane.setEditable(false);
    textScroller = new JScrollPane(textPane);
    setContentPane(textScroller);
    if(resourceName!=null) {
      loadResource(resourceName, location);
    }
  }

  public JTextPane getTextPane() {
    return textPane;
  }

  /**
   * Enables hyperlinks.  Hyperlink anchors load in the text pane.
   */
  public void enableHyperlinks() {
    if(hyperlinkListener!=null) { // remove old listener
      textPane.removeHyperlinkListener(hyperlinkListener);
    }
    hyperlinkListener = new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
          try {
            textPane.setPage(e.getURL());
          } catch(IOException ex) {}
        }
      }

    };
    textPane.addHyperlinkListener(hyperlinkListener);
  }

  /**
   * Enables hyperlinks.  Hyperlink anchors load in the native browser.
   */
  public void enableDesktopHyperlinks() {
    if(hyperlinkListener!=null) { // remove old listener
      textPane.removeHyperlinkListener(hyperlinkListener);
    }
    hyperlinkListener = new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
          try {
            if(!org.opensourcephysics.desktop.OSPDesktop.browse(e.getURL().toURI())) {
              // try the old way
              org.opensourcephysics.desktop.ostermiller.Browser.init();
              org.opensourcephysics.desktop.ostermiller.Browser.displayURL(e.getURL().toString());
            }
          } catch(Exception e1) {}
        }
      }

    };
    textPane.addHyperlinkListener(hyperlinkListener);
  }

  /**
   * Enables hyperlinks.  Hyperlink anchors load in the native browser.
   */
  public void disableHyperlinks() {
    if(hyperlinkListener!=null) { // remove old listener
      textPane.removeHyperlinkListener(hyperlinkListener);
    }
    hyperlinkListener = null;
  }

  public boolean loadResource(String resourceName, Class<?> location) {
    Resource res = null;
    try {
      res = ResourceLoader.getResource(resourceName, location);
    } catch(Exception ex) {
      OSPLog.fine("Error getting resource: "+resourceName); //$NON-NLS-1$
      return false;
    }
    if(res==null) {
      OSPLog.fine("Resource not found: "+resourceName); //$NON-NLS-1$
      return false;
    }
    try {
      textPane.setPage(res.getURL());
    } catch(IOException ex) {
      OSPLog.fine("Resource not loadeded: "+resourceName); //$NON-NLS-1$
      return false;
    }
    setTitle(resourceName);
    return true;
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
