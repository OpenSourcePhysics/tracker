/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.ResizableIcon;

/**
 * This represents a resource obtained from a URL or File.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class Resource {
  private static String encoding = "UTF-8"; //$NON-NLS-1$
  private static Charset defaultCharset = Charset.forName(encoding);

  private URL url;
  private File file;
  private boolean isAnImage = true;
  private ImageIcon icon;
  private String string;
  private AudioClip clip;
  private BufferedImage image;
  private String zipContent;
  private URL contentURL;
  
  /**
   * Gets the character set used for reading input streams.
   *
   * @return the character set
   */
  public static Charset getCharset() { return defaultCharset; }
  
  /**
   * Constructs a resource from a url.
   *
   * @param url the URL
   */
  public Resource(URL url) {
    this.url = url;
  }

  /**
   * Constructs a resource from a file.
   *
   * @param file the file
   */
  public Resource(File file) {
    this.file = file;
  }

  /**
   * Constructs a resource from a ZIP url with content.
   *
   * @param zipURL the URL
   * @param content the path of the contents relative to the ZIP file
   */
  protected Resource(URL zipURL, String content) {
    this.url = zipURL;
    zipContent = content;
    if (content!=null) try {
    	String path = zipURL.toExternalForm()+"!/"+content; //$NON-NLS-1$
			contentURL = new URL(path);
		} catch (MalformedURLException ex) {
		}
  }

  /**
   * Gets the absolute path.
   *
   * @return the absolute path
   */
  public String getAbsolutePath() {
    if(getFile()!=null) {
      try {
        return XML.forwardSlash(getFile().getCanonicalPath());
      } catch(IOException ex) {
        ex.printStackTrace();
      }
      return getFile().getAbsolutePath();
    }
    if(getURL()!=null) {
      String path = getURL().toExternalForm();
//      return ResourceLoader.getNonURIPath(path);
      return path;
    }
    return null;
  }

  /**
   * Gets the url associated with this resource.
   *
   * @return the URL
   */
  public URL getURL() {
    if (url==null && file!=null) {
      String path = getAbsolutePath();
      try {
        if(path.startsWith("/")) {      //$NON-NLS-1$
          url = new URL("file:"+path);  //$NON-NLS-1$
        } else {
          url = new URL("file:/"+path); //$NON-NLS-1$
        }
      } catch(MalformedURLException ex) {
        ex.printStackTrace();
      }
    }
    if (contentURL!=null) {
    	return contentURL;
    }
    return url;
  }

  /**
   * Gets the file associated with this resource.
   *
   * @return the File
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets an object of the specified type. Currently the only types
   * recognized are String and ImageIcon.
   *
   * @param type the desired class type
   * @return the object, or null
   */
  public Object getObject(Class<?> type) {
    if(ImageIcon.class.equals(type)) {
      return getIcon();
    }
    if(String.class.equals(type)) {
      return getString();
    }
    return null;
  }

  /**
   * Opens an InputStream.
   *
   * @return the stream
   */
  public InputStream openInputStream() {
    if (getFile()!=null) {
      try {
        return new FileInputStream(getFile());
      } catch(FileNotFoundException ex) {
        ex.printStackTrace();
      }
    }
    if (url!=null) {
      try {
      	if (zipContent!=null) {
          BufferedInputStream bufIn = new BufferedInputStream(url.openStream());
          ZipInputStream input = new ZipInputStream(bufIn);
          ZipEntry zipEntry=null;
          while ((zipEntry=input.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) continue;
            String filename = zipEntry.getName();
            if (zipContent.contains(filename)) {
            	return input;
            }
          }
      		
      	}
        return getURL().openStream();
      } catch(IOException ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Opens a BufferedReader for the default character set (UTF-8).
   *
   * @return the reader
   */
  public BufferedReader openReader() {
    return openReader(defaultCharset);
  }

  /**
   * Opens a BufferedReader for a specified character set.
   * @param charset the character set
   * @return the reader
   */
  public BufferedReader openReader(java.nio.charset.Charset charset) {
    InputStream stream = openInputStream();
    if(stream==null) {
      return null;
    }
    return new BufferedReader(new InputStreamReader(stream, charset));
  }

  /**
   * Gets an Icon.
   *
   * @return the icon
   */
  public Icon getIcon() {
    if((icon==null)&&isAnImage) {
      icon = new ImageIcon(getURL());
      if(icon.getIconWidth()<1) {
        icon = null;
        isAnImage = false;
        return null;
      }
    }
    return new ResizableIcon(icon);
  }

  /**
   * Gets an Image.
   *
   * @return the image
   */
  public Image getImage() {
    Icon icon = getIcon();
    if(icon!=null) {
    	ResizableIcon ico = (ResizableIcon)icon;
    	ImageIcon imageIcon = (ImageIcon)ico.getBaseIcon();
      return imageIcon.getImage();
    }
    return null;
  }

  /**
   * Gets a buffered image.
   *
   * @return the image
   */
  public BufferedImage getBufferedImage() {
    return getBufferedImage(BufferedImage.TYPE_INT_RGB);
  }

  /**
   * Gets a buffered image.
   * @param bufferedImageType one of the types defined by the BufferedImage class
   * @return the image
   */
  public BufferedImage getBufferedImage(int bufferedImageType) {
    if(isAnImage && (image==null || image.getType()!=bufferedImageType)) {
      Image im = getImage();
      if(im==null) {
        isAnImage = false;
      } else {
        image = new BufferedImage(im.getWidth(null), im.getHeight(null), bufferedImageType);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(im, 0, 0, null);
      }
    }
    return image;
  }

  /**
   * Gets a String.
   *
   * @return the string
   */
  public String getString() {
    if(string==null) {
      StringBuffer buffer = new StringBuffer();
      try {
        BufferedReader in = new BufferedReader(openReader());
        String line = in.readLine();
        while(line!=null) {
          buffer.append(line+XML.NEW_LINE);
          line = in.readLine();
        }
        in.close();
      } catch(IOException ex) {
        ex.printStackTrace();
      }
      string = buffer.toString();
    }
    return string;
  }

  /**
   * Gets a String encoded with a specified encoding.
   * @param encoding the encoding (eg "ISO-8859-1" for properties files)
   * @return the string
   */
  public String getString(String encoding) {
    if(string==null) {    	
      StringBuffer buffer = new StringBuffer();
      try {
      	Charset charset = Charset.forName(encoding);
      	BufferedReader in = new BufferedReader(openReader(charset));
        String line = in.readLine();
        while(line!=null) {
          buffer.append(line+XML.NEW_LINE);
          line = in.readLine();
        }
        in.close();
      } catch(IOException ex) {
        ex.printStackTrace();
      }
      string = buffer.toString();
    }
    return string;
  }

  /**
   * Gets an AudioClip.
   *
   * @return the audio clip
   */
  public AudioClip getAudioClip() {
    if((clip==null)&&(getURL()!=null)) {
      clip = Applet.newAudioClip(getURL());
    }
    return clip;
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
