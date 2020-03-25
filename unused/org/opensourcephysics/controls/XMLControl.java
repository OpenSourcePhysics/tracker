/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.io.Reader;
import java.io.Writer;

/**
 * This defines methods for storing data in an xml control element.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface XMLControl extends Control, XMLProperty {
  /**
   * Gets the type of the specified property.
   *
   * @param name the property name
   * @return the type
   */
  public String getPropertyType(String name);

  /**
   * Gets the name of the object class for which this element stores data.
   *
   * @return the object class name
   */
  public String getObjectClassName();

  /**
   * Gets the class for which this stores data.
   *
   * @return the class
   */
  public Class<?> getObjectClass();

  /**
   * Saves an object's data in this XMLControl.
   *
   * @param obj the object to save.
   */
  public void saveObject(Object obj);

  /**
   * Loads an object with data from this XMLControl.
   *
   * @param obj the object to load
   * @return the loaded object
   */
  public Object loadObject(Object obj);

  /**
   * Reads the control from an xml document with the specified name.
   *
   * @param name the name
   * @return the full name of the opened document or null if failed
   */
  public String read(String name);

  /**
   * Reads the control from a Reader.
   *
   * @param reader the Reader
   */
  public void read(Reader reader);

  /**
   * Reads the control from an xml string.
   *
   * @param xml the xml string
   */
  public void readXML(String xml);

  /**
   * Returns true if the most recent read operation failed.
   *
   * @return <code>true</code> if the most recent read operation failed
   */
  public boolean failedToRead();

  /**
   * Writes the control as an xml document with the specified name.
   *
   * @param fileName the file name
   * @return the full name of the saved document or null if failed
   */
  public String write(String fileName);

  /**
   * Writes the control to a Writer.
   *
   * @param writer the Writer
   */
  public void write(Writer writer);

  /**
   * Returns this control as an xml string.
   *
   * @return the xml string
   */
  public String toXML();

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
