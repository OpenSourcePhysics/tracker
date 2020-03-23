/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.util.List;

/**
 * This defines methods for storing data in an xml property element.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface XMLProperty {
  /**
   * Gets the property name.
   *
   * @return a name
   */
  public String getPropertyName();

  /**
   * Gets the property type.
   *
   * @return the type
   */
  public String getPropertyType();

  /**
   * Gets the property class.
   *
   * @return the class
   */
  public Class<?> getPropertyClass();

  /**
   * Gets the immediate parent property.
   *
   * @return the type
   */
  public XMLProperty getParentProperty();

  /**
   * Gets the level of this property relative to root.
   *
   * @return the non-negative integer level
   */
  public int getLevel();

  /**
   * Gets the property content of this property.
   *
   * @return a list of strings and XMLProperties
   */
  public List<Object> getPropertyContent();

  /**
   * Gets the named XMLControl child of this property. May return null.
   *
   * @param name the property name
   * @return the XMLControl
   */
  public XMLControl getChildControl(String name);

  /**
   * Gets the XMLControl children of this property.
   *
   * @return an array of XMLControls
   */
  public XMLControl[] getChildControls();

  /**
   * Sets the value of this property if property type is primitive or string.
   * This does nothing for other property types.
   *
   * @param stringValue the string value of a primitive or string property
   */
  public void setValue(String stringValue);

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
