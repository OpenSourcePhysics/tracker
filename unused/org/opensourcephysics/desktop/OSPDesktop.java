/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.desktop;

/**
 * OSPDesktop invokes the java.awt.Desktop API using reflection for Java 1.5 compatibility.
 *
 * @author Wolfgang Christian
 */
public class OSPDesktop {
  static final String desktopClassName = "java.awt.Desktop"; //$NON-NLS-1$
  static boolean desktopSupported;

  static {
    java.lang.reflect.Method m;
    try {
      m = Class.forName(desktopClassName).getMethod("isDesktopSupported", (Class<?>[]) null); //$NON-NLS-1$
      desktopSupported = (Boolean) m.invoke(null, (Object[]) null);
    } catch(Exception e) {
      //e.printStackTrace();]
      desktopSupported = false;
    }
  }

  /**
   * Display a URL in the system browser.
   *
   * Attempts to open URL with desktop API if available;  attempts Ostermiller code otherwise.
   *
   * @return true if successful
   */
  public static boolean displayURL(String url) {
    try {
      if(!org.opensourcephysics.desktop.OSPDesktop.browse(url)) {
        // try the old way
        org.opensourcephysics.desktop.ostermiller.Browser.init();
        org.opensourcephysics.desktop.ostermiller.Browser.displayURL(url);
      }
      return true;
    } catch(Exception e1) {
      return false;
    }
  }

  /**
   * Determines if the desktop API is supported.
   *
   * @return true if the desktop API is supported.
   */
  static public boolean isDesktopSupported() {
    return desktopSupported;
  }

  /**
   * Launches the default browser to display a URI.
   *
   * @param uriName
   */
  static public boolean browse(String uriName) {
    if(!desktopSupported) {
      return false;
    }
    try {
      return browse(new java.net.URI(uriName));
    } catch(Exception e) {
      //e.printStackTrace();
      return false;
    }
  }

  /**
   * Launches the default browser to display a URI.
   *
   * @param uri
   * @return true if successful
   */
  static public boolean browse(java.net.URI uri) {
    if(!desktopSupported||(uri==null)) {
      return false;
    }
    java.lang.reflect.Method m;
    Class<?>[] parameters = new Class[] {java.net.URI.class};
    try {
      m = Class.forName(desktopClassName).getMethod("getDesktop", (Class<?>[]) null); //$NON-NLS-1$
      Object desktop = m.invoke(null, (Object[]) null);
      m = Class.forName(desktopClassName).getMethod("browse", parameters);            //$NON-NLS-1$
      Object[] args = {uri};
      m.invoke(desktop, args);
      return true;
    } catch(Exception e) {
      //e.printStackTrace();
      return false;
    }
  }

  /**
   * Launches the default email program with the given address.
   * @param addr
   * @return true if successful
   */
  static public boolean mail(String addr) {
    if(!desktopSupported||(addr==null)) {
      return false;
    }
    java.lang.reflect.Method m;
    Class<?>[] parameters = new Class[] {java.net.URI.class};
    try {
      java.net.URI uri = new java.net.URI("mailto:"+addr.trim());                     //$NON-NLS-1$
      m = Class.forName(desktopClassName).getMethod("getDesktop", (Class<?>[]) null); //$NON-NLS-1$
      Object desktop = m.invoke(null, (Object[]) null);
      m = Class.forName(desktopClassName).getMethod("mail", parameters);              //$NON-NLS-1$
      Object[] args = {uri};
      m.invoke(desktop, args);
      return true;
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Launches the default email program without a recipient.
   * @return true if successful
   */
  static public boolean mail() {
    if(!desktopSupported) {
      return false;
    }
    java.lang.reflect.Method m;
    try {
      m = Class.forName(desktopClassName).getMethod("getDesktop", (Class<?>[]) null); //$NON-NLS-1$
      Object desktop = m.invoke(null, (Object[]) null);
      m = Class.forName(desktopClassName).getMethod("mail", (Class<?>[]) null);       //$NON-NLS-1$
      m.invoke(desktop, (Object[]) null);
      return true;
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Launches the default application to edit a file.
   *
   * @param file
   */
  static public boolean edit(java.io.File file) {
    if(!desktopSupported) {
      return false;
    }
    java.lang.reflect.Method m;
    Class<?>[] parameters = new Class[] {java.io.File.class};
    try {
      m = Class.forName(desktopClassName).getMethod("getDesktop", (Class<?>[]) null); //$NON-NLS-1$
      Object desktop = m.invoke(null, (Object[]) null);
      m = Class.forName(desktopClassName).getMethod("edit", parameters);              //$NON-NLS-1$
      Object[] args = {file};
      m.invoke(desktop, args);
      return true;
    } catch(Exception e) {
      //e.printStackTrace();
      return false;
    }
  }

  /**
   * Launches the default application to edit a file.
   *
   * @param file
   */
  static public boolean open(java.io.File file) {
    if(!desktopSupported) {
      return false;
    }
    java.lang.reflect.Method m;
    Class<?>[] parameters = new Class[] {java.io.File.class};
    try {
      m = Class.forName(desktopClassName).getMethod("getDesktop", (Class<?>[]) null); //$NON-NLS-1$
      Object desktop = m.invoke(null, (Object[]) null);
      m = Class.forName(desktopClassName).getMethod("open", parameters);              //$NON-NLS-1$
      Object[] args = {file};
      m.invoke(desktop, args);
      return true;
    } catch(Exception e) {
      //e.printStackTrace();
      return false;
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
