/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * String resources for media classes.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class MediaRes {
  // static fields
  private static String BUNDLE_NAME = "org.opensourcephysics.resources.media.video"; //$NON-NLS-1$
  static Locale resourceLocale = Locale.getDefault();
  static ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);

  /**
   * Private constructor to prevent instantiation.
   */
  private MediaRes() {
  	/** empty block */
  }

  /**
   * Gets the localized value of a string. If no localized value is found, the
   * key is returned surrounded by exclamation points.
   *
   * @param key the string to localize
   * @return the localized string
   */
  public static String getString(String key) {
    try {
      return res.getString(key);
    } catch(MissingResourceException ex) {
      return "!"+key+"!"; //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Sets the locale.
   *
   * @param locale the locale
   */
  public static void setLocale(Locale locale) {
  	resourceLocale = locale;
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    System.out.println("MediaRes Locale set for " + res.getLocale() + " " + res.getBaseBundleName());
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
