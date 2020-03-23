/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.opensourcephysics.display.OSPRuntime;

public class EjsRes {
  private static final String BUNDLE_NAME = "org.opensourcephysics.resources.ejs.ejs_res"; //$NON-NLS-1$
  static ResourceBundle res;

  private EjsRes() {}

  static {
    String language = Locale.getDefault().getLanguage();
    Locale resourceLocale = Locale.ENGLISH;
    for(Locale locale : OSPRuntime.getInstalledLocales()) {
      if(locale.getLanguage().equals(language)) {
        resourceLocale = locale;
        break;
      }
    }
    res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);
  }

  public static void setLocale(Locale locale) {
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  public static String getString(String key) {
    try {
      return res.getString(key);
    } catch(MissingResourceException e) {
      return '!'+key+'!';
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
