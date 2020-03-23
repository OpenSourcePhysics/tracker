/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.davidson.applets;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.controls.MainFrame;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.Renderable;
import org.opensourcephysics.display.TextFrame;

/**
 * FrameApplet displays a JFrame from an application in an HTML page.
 * This Applet can be used to run applications as applets.  Security restrictions may cause some
 * programs to malfunction if the jar file is not signed.
 *
 *@version    0.9 beta
 *@author     Wolfgang Christian
 *@created    December 10, 2006
 */
public class FrameApplet extends JApplet implements Renderable {
  JFrame mainFrame = null;
  String targetClassName;
  String contentName;
  ArrayList<Frame> newFrames = new ArrayList<Frame>();
  ArrayList<Frame> existingFrames = new ArrayList<Frame>();
  String[] args = null;
  Renderable renderPanel;
  boolean singleFrame = false;

  /**
   *  Gets the parameter attribute of the ApplicationApplet object
   *
   * @param  key  Description of Parameter
   * @param  def  Description of Parameter
   * @return      The parameter value
   */
  public String getParameter(String key, String def) {
    return((getParameter(key)!=null) ? getParameter(key) : def);
  }

  /**
   *  Initializes the applet
   */
  public void init() {
    super.init();
    if(getParameter("showLog", "false").toLowerCase().trim().equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      OSPLog.showLog();
    }
    OSPRuntime.applet = this;
    OSPRuntime.appletMode = true; // main frame will be embedded; other frames are hidden
    String xmldata = getParameter("xmldata", null); //$NON-NLS-1$
    if(xmldata!=null) {
      args = new String[1];
      args[0] = xmldata;
    }
    targetClassName = getParameter("target", null); //$NON-NLS-1$
    if(targetClassName==null) {
      targetClassName = getParameter("app", null); //$NON-NLS-1$
    }
    contentName = getParameter("content", null);                                        //$NON-NLS-1$
    singleFrame = getParameter("singleframe", "false").trim().equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void start() {
    if(mainFrame!=null) {
      return;
    }
    createTarget();
    if(contentName!=null) {
      Frame[] frame = Frame.getFrames();
      for(int i = 0, n = frame.length; i<n; i++) {
        if((frame[i] instanceof JFrame)&&frame[i].getName().equalsIgnoreCase(contentName)) {
          mainFrame = (JFrame) frame[i];
          break;
        }
      }
    }
    if(mainFrame==null) {
      System.out.println("Main frame not found."); //$NON-NLS-1$
      return;
    }
    removeWindowListeners(mainFrame);
    mainFrame.setVisible(false);
    Container content = mainFrame.getContentPane();
    if((mainFrame instanceof OSPFrame)&&((OSPFrame) mainFrame).isAnimated()) { // look for animated content
      renderPanel = (Renderable) GUIUtils.findInstance(content, Renderable.class);
    }
    if(mainFrame instanceof OSPFrame) {
      ((OSPFrame) mainFrame).setKeepHidden(true);
    } else {
      mainFrame.dispose(); // don't need this frame
    }
    getRootPane().setContentPane(content);
    getRootPane().requestFocus();
    if(!singleFrame) {
      OSPRuntime.appletMode = false; // all frames will now be shown
      for(int i = 0, n = newFrames.size(); i<n; i++) {
        if((newFrames.get(i) instanceof OSPFrame)&&(newFrames.get(i)!=mainFrame)) {
          ((OSPFrame) newFrames.get(i)).setKeepHidden(false);
        }
      }
      GUIUtils.showDrawingAndTableFrames();
    }
  }

  private void removeWindowListeners(Window frame) {
    WindowListener[] wl = frame.getWindowListeners();
    for(int i = 0, n = wl.length; i<n; i++) { // remove window listeners because many windows will call exit(0) when closing
      mainFrame.removeWindowListener(wl[i]);
    }
  }

  public BufferedImage render() {
    if(renderPanel!=null) {
      return renderPanel.render();
    }
    return null;
  }

  public BufferedImage render(BufferedImage image) {
    if(renderPanel!=null) {
      renderPanel.render(image);
    }
    return image;
  }

  /**
   * Determines whether the specified class is launchable.
   *
   * @param type the launch class to verify
   * @return <code>true</code> if the class is launchable
   */
  static boolean isLaunchable(Class<?> type) {
    if(type==null) {
      return false;
    }
    try {
      // throws exception if method not found; return value not used
      type.getMethod("main", new Class[] {String[].class}); //$NON-NLS-1$
      return true;
    } catch(NoSuchMethodException ex) {
      return false;
    }
  }

  /**
   *  Destroys the applet's resources.
   */
  public void destroy() {
    disposeOwnedFrames();
    mainFrame = null;
    super.destroy();
  }

  private Class<?> createTarget() {
    Class<?> type = null;
    // create the class loader
    // ClassLoader classLoader = URLClassLoader.newInstance(new URL[] {getCodeBase()});
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      type = classLoader.loadClass(targetClassName);
    } catch(ClassNotFoundException ex1) {
      System.err.println("Class not found: "+targetClassName); //$NON-NLS-1$
      return null;
    }
    if(!isLaunchable(type)) {
      System.err.println("Main method not found in "+targetClassName); //$NON-NLS-1$
      return null;
    }
    // get the exisitng frames
    Frame[] frame = Frame.getFrames();
    existingFrames.clear();
    for(int i = 0, n = frame.length; i<n; i++) {
      existingFrames.add(frame[i]);
    }
    // load html data file
    String htmldata = getParameter("htmldata", null); //$NON-NLS-1$
    if(htmldata!=null) {
      TextFrame htmlframe = new TextFrame(htmldata, type);
      htmlframe.setVisible(true);
    }
    // launch the app by invoking main method
    try {
      Method m = type.getMethod("main", new Class[] {String[].class}); //$NON-NLS-1$
      m.invoke(type, new Object[] {args});
      frame = Frame.getFrames();
      for(int i = 0, n = frame.length; i<n; i++) {
        if((mainFrame==null)&&(frame[i] instanceof MainFrame)) {
          mainFrame = ((MainFrame) frame[i]).getMainFrame();           // assume this is the main application frame
        }
      }
      for(int i = 0, n = frame.length; i<n; i++) {                     //make sure we do not exit
        if((frame[i] instanceof JFrame)&&((JFrame) frame[i]).getDefaultCloseOperation()==JFrame.EXIT_ON_CLOSE) {
          ((JFrame) frame[i]).setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
          if(mainFrame==null) {
            mainFrame = (JFrame) frame[i];                             // assume this is the main application frame
          }
        }
        if(!existingFrames.contains(frame[i])) {
          // manage new frames
          newFrames.add(frame[i]);
        }
      }
    } catch(NoSuchMethodException ex) {
      System.err.println(ex);
    } catch(InvocationTargetException ex) {
      System.err.println(ex);
    } catch(IllegalAccessException ex) {
      System.err.println(ex);
    }
    if((newFrames.size()>0)&&(mainFrame==null)&&(newFrames.get(0) instanceof JFrame)) {
      mainFrame = (JFrame) newFrames.get(0); // assume this is the main application frame
    }
    return type;
  }

  private void disposeOwnedFrames() {
    Frame frame[] = Frame.getFrames();
    for(int i = 0, n = frame.length; i<n; i++) {
      if((frame[i] instanceof JFrame)&&((JFrame) frame[i]).getDefaultCloseOperation()==JFrame.EXIT_ON_CLOSE) {
        ((JFrame) frame[i]).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      }
      if(!existingFrames.contains(frame[i])) { // remove any frames that have been created by this applet
        frame[i].setVisible(false);
        removeWindowListeners(frame[i]);
        frame[i].dispose();
      }
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
