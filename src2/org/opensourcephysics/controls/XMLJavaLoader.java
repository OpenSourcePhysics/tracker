/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * This is an ObjectLoader implementation that uses the Java XMLEncoder and
 * XMLDecoder classes to save and load data.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XMLJavaLoader implements XML.ObjectLoader {
  private ByteArrayOutputStream out;
  private BufferedOutputStream buf;

  /**
   * Constructs the loader.
   */
  public XMLJavaLoader() {
    out = new ByteArrayOutputStream();
    buf = new BufferedOutputStream(out);
  }

  /**
   * Saves XMLEncoder data for an object in the specified XMLControl.
   *
   * @param control the control
   * @param obj the object
   */
  public void saveObject(XMLControl control, Object obj) {
    // buf.flush();
    XMLEncoder enc = new XMLEncoder(buf);
    enc.writeObject(obj);
    enc.close();
    String xml = out.toString();
    control.setValue("java_xml", xml); //$NON-NLS-1$
  }

  /**
   * Creates a new object if the class type has a no-arg constructor.
   *
   * @param control the control
   * @return the new object
   */
  public Object createObject(XMLControl control) {
    String xml = control.getString("java_xml"); //$NON-NLS-1$
    InputStream in;
    try {
      in = new ByteArrayInputStream(xml.getBytes("UTF-8")); //$NON-NLS-1$
    } catch(UnsupportedEncodingException ex) {
      in = new ByteArrayInputStream(xml.getBytes());
    }
    XMLDecoder dec = new XMLDecoder(new BufferedInputStream(in));
    Object obj = dec.readObject();
    dec.close();
    return obj;
  }

  /**
   * Loads an object with data from an XMLControl.
   *
   * @param control the control
   * @param obj the object
   */
  public Object loadObject(XMLControl control, Object obj) {
    return createObject(control);
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
