/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import javax.swing.ImageIcon;

import org.opensourcephysics.tools.ResourceLoader;

/**
 * Some utility functions
 */
public class Utils {
  static private java.util.Hashtable<String, ImageIcon> cacheImages = new java.util.Hashtable<String, ImageIcon>();

  static public boolean fileExists(String _codebase, String _filename) {
    if(_filename==null) {
      return false;
    }
    if(cacheImages.get(_filename)!=null) {
      return true;
    }
    if(_codebase!=null) {
      if(_codebase.startsWith("file:")) {              //$NON-NLS-1$
        _codebase = "file:///"+_codebase.substring(6); //$NON-NLS-1$
      }
      if(!_codebase.endsWith("/")) {                   //$NON-NLS-1$
        _codebase += "/";                              //$NON-NLS-1$
      }
    }
    int index = _filename.indexOf('+');
    if(index>=0) {
      return fileExistsInJar(_codebase, _filename.substring(0, index), _filename.substring(index+1));
    } else if(_codebase==null) {
      java.io.File file = new java.io.File(_filename);
      return file.exists();
    } else {
      try {
        java.net.URL url = new java.net.URL(_codebase+_filename);
        java.io.InputStream stream = url.openStream();
        stream.close();
        return true;
      } catch(Exception exc) {
        return false;
      }
    }
  }

  static public boolean fileExistsInJar(String _codebase, String _jarFile, String _filename) {
    if((_filename==null)||(_jarFile==null)) {
      return false;
    }
    java.io.InputStream inputStream = null;
    java.util.jar.JarInputStream jis=null;
    try {
      if(_codebase==null) {
        inputStream = new java.io.FileInputStream(_jarFile);
        jis = new java.util.jar.JarInputStream(inputStream);
      } else {
        java.net.URL url = new java.net.URL(_codebase+_jarFile);
        inputStream = url.openStream();
        jis = new java.util.jar.JarInputStream(inputStream);
      }
      
      while(true) {
        java.util.jar.JarEntry je = jis.getNextJarEntry();
        if(je==null) {
          break;
        }
        if(je.isDirectory()) {
          continue;
        }
        if(je.getName().equals(_filename)) {
          jis.close();
          inputStream.close();
          return true;
        }
      }
      jis.close();
      inputStream.close();
    } catch(Exception exc) {
      return false;
    }
    return false;
  }

  static public javax.swing.ImageIcon icon(String _codebase, String _gifFile) {
    return icon(_codebase, _gifFile, true);
  }

  static public javax.swing.ImageIcon icon(String _codebase, String _gifFile, boolean _verbose) {
    if(_gifFile==null) {
      return null;
    }
    // System.out.println ("Reading from "+_codebase+" :"+_gifFile);
    javax.swing.ImageIcon icon = cacheImages.get(_gifFile);
    if(icon!=null) {
      return icon;
    }
    if(_codebase!=null) {
      if(_codebase.startsWith("file:")) {              //$NON-NLS-1$
        _codebase = "file:///"+_codebase.substring(6); //$NON-NLS-1$
      }
      if(!_codebase.endsWith("/")) {                   //$NON-NLS-1$
        _codebase += "/";                              //$NON-NLS-1$
      }
    }
    int index = _gifFile.indexOf('+');
    if(index>=0) {
      icon = iconJar(_codebase, _gifFile.substring(0, index), _gifFile.substring(index+1), _verbose);
    } else if(_codebase==null) {
      // System.out.println ("Reading from "+_codebase+" :"+_gifFile);
      java.io.File file = new java.io.File(_gifFile);
      if(file.exists()) {
        icon = new javax.swing.ImageIcon(_gifFile);
      }
      if(icon==null) {
      	// code modified by Doug Brown June 2015 to get ImageIcon from ResourceLoader
      	javax.swing.Icon resIcon = ResourceLoader.getIcon(_gifFile);
      	if (resIcon!=null && resIcon instanceof org.opensourcephysics.display.ResizableIcon) {
      		resIcon = ((org.opensourcephysics.display.ResizableIcon)resIcon).getBaseIcon(); 
	        icon = (ImageIcon)resIcon;
      	}
      }
    } else {
      // System.out.println ("Reading from "+_codebase+" :"+_gifFile);
      try {
        java.net.URL url = new java.net.URL(_codebase+_gifFile);
        icon = new javax.swing.ImageIcon(url);
      } catch(Exception exc) {
        if(_verbose) {
          exc.printStackTrace();
        }
        icon = null;
      }
    }
    if((icon==null)||(icon.getIconHeight()<=0)) {
      if(_verbose) {
        System.out.println("Unable to load image "+_gifFile); //$NON-NLS-1$
      }
    } else {
      cacheImages.put(_gifFile, icon);
    }
    return icon;
  }

  static private byte[] enormous = new byte[100000]; // Maximum size for a gif file in a Jar

  static public javax.swing.ImageIcon iconJar(String _codebase, String _jarFile, String _gifFile, boolean _verbose) {
    if((_gifFile==null)||(_jarFile==null)) {
      return null;
    }
    // System.out.println ("Jar Reading from "+_codebase+" + "+_jarFile+":"+_gifFile);
    javax.swing.ImageIcon icon = null;
    java.io.InputStream inputStream = null;
    java.util.jar.JarInputStream jis=null;
    try {
      if(_codebase==null) {
        inputStream = new java.io.FileInputStream(_jarFile);
        jis = new java.util.jar.JarInputStream(inputStream);
      } else {
        java.net.URL url = new java.net.URL(_codebase+_jarFile);
        inputStream = url.openStream();
        jis = new java.util.jar.JarInputStream(inputStream);
      }
      boolean done = false;
      byte[] b = null;
      while(!done) {
        java.util.jar.JarEntry je = jis.getNextJarEntry();
        if(je==null) {
          break;
        }
        if(je.isDirectory()) {
          continue;
        }
        if(je.getName().equals(_gifFile)) {
          // System.out.println ("Found entry "+je.getName());
          long size = (int) je.getSize();
          // System.out.println ("Size is "+size);
          int rb = 0;
          int chunk = 0;
          while(chunk>=0) {
            chunk = jis.read(enormous, rb, 255);
            if(chunk==-1) {
              break;
            }
            rb += chunk;
          }
          size = rb;
          // System.out.println ("Real Size is "+size);
          b = new byte[(int) size];
          System.arraycopy(enormous, 0, b, 0, (int) size);
          done = true;
        }
      }
      icon = new javax.swing.ImageIcon(b);
      jis.close();
    } catch(Exception exc) {
      if(_verbose) {
        exc.printStackTrace();
      }
      icon = null;
    }
    return icon;
  }

} // end of class

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
