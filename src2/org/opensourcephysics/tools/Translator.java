/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
public interface Translator {
  /**
   * Gets the localized value of a property for the specified class.
   * If no localized value is found, the key is returned.
   *
   * @param type the class requesting the localized value
   * @param key the string to localize
   * @return the localized string
   */
  public String getProperty(Class<?> type, String key);

  /**
   * Gets the localized value of a property for the specified class.
   * If no localized value is found, the defaultValue is returned.
   *
   * @param type the class requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @return the localized string
   */
  public String getProperty(Class<?> type, String key, String defaultValue);

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the key is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @return the localized string
   */
  public String getProperty(Object obj, String key);

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the defaultValue is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @return the localized string
   */
  public String getProperty(Object obj, String key, String defaultValue);

  /**
   * Associates an object with a class for property lookup purposes.
   *
   * @param obj the object needing translations
   * @param type the class
   */
  public void associate(Object obj, Class<?> type);

  /**
   * Shows the properties for the specified class.
   *
   * @param type the class
   */
  public void showProperties(Class<?> type);

  /**
   * Sets the visibility.
   *
   * @param visible true to set this visible
   */
  public void setVisible(boolean visible);

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
